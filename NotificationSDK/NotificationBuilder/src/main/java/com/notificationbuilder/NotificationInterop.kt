package com.notificationbuilder

import android.app.NotificationManager
import android.os.Build
import android.text.Html
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat

internal object NotificationInterop {

    fun showNotification(notificationManager: NotificationManager, id: Int?, notification: NotificationCompat.Builder): Int {
        val key = NotificationExtender.getKey(notification.extras)
        var id = id ?: Utils.getRandomInt()

        if (key != null) {
            id = key.hashCode()
            notificationManager.notify(key.toString(), id, notification.build())
        } else {
            notificationManager.notify(id, notification.build())
        }

        return id
    }

    fun cancelNotification(notificationManager: NotificationManager, notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getActiveNotifications(notificationManager: NotificationManager): List<NotificationExtender> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return ArrayList()
        }

        return notificationManager.activeNotifications
            .map { NotificationExtender(it) }
            .filter { it.valid }
    }

    fun buildNotification(notify: NotificationBuilder, payload: RawNotification): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(notify.context, payload.alerting.channelKey)
            // Ensures that this notification is marked as a Notify notification.
            .extend(NotificationExtender())
            // The color of the RawNotification Icon, App_Name and the expanded chevron.
            .setColor(payload.header.color)
            // The RawNotification icon.
            .setSmallIcon(payload.header.icon)
            // The text that is visible to the right of the app name in the notification header.
            .setSubText(payload.header.headerText)
            // Show the relative timestamp next to the application name.
            .setShowWhen(payload.header.showTimestamp)
            // Dismiss the notification on click?
            .setAutoCancel(payload.meta.cancelOnClick)
            // Set the click handler for the notifications
            .setContentIntent(payload.meta.clickIntent)
            // Set the handler in the event that the notification is dismissed.
            .setDeleteIntent(payload.meta.clearIntent)
            // The category of the notification which allows android to prioritize the
            // notification as required.
            .setCategory(payload.meta.category)
            // Set the key by which this notification will be grouped.
            .setGroup(payload.meta.group)
            // Set whether or not this notification is only relevant to the current device.
            .setLocalOnly(payload.meta.localOnly)
            // Set whether this notification is sticky.
            .setOngoing(payload.meta.sticky)
            // The duration of time after which the notification is automatically dismissed.
            .setTimeoutAfter(payload.meta.timeout)

        if (payload.progress.showProgress) {
            if (payload.progress.enablePercentage) builder.setProgress(100,payload.progress.progressPercent,false)
            else builder.setProgress(0,0,true)
        }

        // Standard notifications have the collapsed title and text.
        if (payload.content is Payload.Content.Standard) {
            // This is the title of the RawNotification.
            builder.setContentTitle(payload.content.title)
                // THis is the text of the 'collapsed' RawNotification.
                .setContentText(payload.content.text)
        }

        if (payload.content is Payload.Content.SupportsLargeIcon) {
            // Sets the large icon of the notification.
            builder.setLargeIcon(payload.content.largeIcon)
        }

        // Attach all the actions.
        payload.actions?.forEach {
            builder.addAction(it)
        }

        // Attach alerting options.
        payload.alerting.apply {
            // Register the default alerting. Applies channel configuration on API >= 26.
            NotificationChannelInterop.with(this)

            // The visibility of the notification on the lockscreen.
            builder.setVisibility(lockScreenVisibility)

            // The lights of the notification.
            if (lightColor != NotificationBuilder.NO_LIGHTS) {
                builder.setLights(lightColor, 500, 2000)
            }

            // Manual specification of the priority. According to the documentation, this is only
            // one of the factors that affect the notifications priority and that this behaviour may
            // differ on different platforms.
            // It seems that the priority is also affected by the sound that is set for the
            // notification as such we'll wrap the behaviour of the sound and also of the vibration
            // to prevent the notification from being reclassified to a different priority.
            // This doesn't seem to be the case for API >= 26, however, a future PR should tackle
            // API nuances and ensure that behaviour has been tested.
            builder.priority = channelImportance

            // If the notification's importance is normal or greater then we configure
            if (channelImportance >= NotificationBuilder.IMPORTANCE_NORMAL) {
                // The vibration pattern.
                vibrationPattern
                    .takeIf { it.isNotEmpty() }
                    ?.also {
                        builder.setVibrate(it.toLongArray())
                    }

                // A custom alerting sound.
                builder.setSound(sound)
            }
        }


        var style: NotificationCompat.Style? = null

        if (style == null) {
            style = setStyle(builder, payload.content)
        }

        builder.setStyle(style)

        return builder
    }

    private fun setStyle(builder: NotificationCompat.Builder, content: Payload.Content): NotificationCompat.Style? {
        return when (content) {
            is Payload.Content.Default -> {
                // Nothing to do here. There is no expanded text.
                null
            }
            is Payload.Content.TextList -> {
                NotificationCompat.InboxStyle().also { style ->
                    content.lines.forEach { style.addLine(it) }
                }
            }
            is Payload.Content.BigText -> {
                // Override the behavior of the second line.
                builder.setContentText(Utils.getAsSecondaryFormattedText((content.text
                    ?: "").toString()))

                val formattedExpandedText = content.expandedText?.let {
                    "<font color='#3D3D3D'>$it</font><br>"
                } ?: ""

                val bigText: CharSequence = Html.fromHtml(formattedExpandedText + content.bigText?.replace("\n".toRegex(), "<br>"))

                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
            }
            is Payload.Content.BigPicture -> {
                NotificationCompat.BigPictureStyle()
                    // This is the second line in the 'expanded' notification.
                    .setSummaryText(content.expandedText ?: content.text)
                    // This is the picture below.
                    .bigPicture(content.image)
                    .bigLargeIcon(null)

            }
            is Payload.Content.Message -> {
                NotificationCompat.MessagingStyle(content.userDisplayName)
                    .setConversationTitle(content.conversationTitle)
                    .also { s ->
                        content.messages.forEach { s.addMessage(it.text, it.timestamp, it.sender) }
                    }
            }
        }
    }
}