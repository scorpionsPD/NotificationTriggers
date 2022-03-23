package com.notificationbuilder

import android.annotation.TargetApi
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Fluent API for creating a Notification object.
 */
@NotifyScopeMarker
class NotificationCreator internal constructor(private val notify: NotificationBuilder) {

    private var meta = Payload.Meta()
    private var alerts = NotificationBuilder.defaultConfig.defaultAlerting
    private var header = NotificationBuilder.defaultConfig.defaultHeader.copy()
    private var content: Payload.Content = Payload.Content.Default()
    private var actions: ArrayList<Action>? = null
    private var progress: Payload.Progress = Payload.Progress()

    /**
     * Scoped function for modifying the Metadata of a notification, such as click intents,
     * notification category, and priority among other options.
     */
    fun meta(init: Payload.Meta.() -> Unit): NotificationCreator {
        this.meta.init()

        return this
    }

    /**
     * Scoped function for modifying the Alerting of a notification. This includes visibility,
     * sounds, lights, etc.
     *
     * If an existing key is provided the existing channel is retrieved (API >= AndroidO) and set as the alerting
     * configuration. If the key is new, the channel is created and set as the alerting configuration.
     */
    fun alerting(key: String, init: Payload.Alerts.() -> Unit): NotificationCreator {
        // Clone object and assign the key.
        this.alerts = this.alerts.copy(channelKey = key).also(init)
        return this
    }

    /**
     * Scoped function for modifying the Header of a notification. Specifically, it allows the
     * modification of the notificationIcon, color, the headerText (optional text next to the
     * appName), and finally the notifyChannel of the notification if targeting Android O.
     */
    fun header(init: Payload.Header.() -> Unit): NotificationCreator {
        this.header.init()
        return this
    }

    fun progress(init: Payload.Progress.() -> Unit): NotificationCreator {
        this.progress.init()
        return this
    }

    /**
     * Scoped function for modifying the content of a 'Default' notification.
     */
    fun content(init: Payload.Content.Default.() -> Unit): NotificationCreator {
        this.content = Payload.Content.Default().also(init)
        return this
    }

    /**
     * Scoped function for modifying the content of a 'TextList' notification.
     */
    fun asTextList(init: Payload.Content.TextList.() -> Unit): NotificationCreator {
        this.content = Payload.Content.TextList().also(init)
        return this
    }

    /**
     * Scoped function for modifying the content of a 'BigText' notification.
     */
    fun asBigText(init: Payload.Content.BigText.() -> Unit): NotificationCreator {
        this.content = Payload.Content.BigText().also(init)
        return this
    }

    /**
     * Scoped function for modifying the content of a 'BigPicture' notification.
     */
    fun asBigPicture(init: Payload.Content.BigPicture.() -> Unit): NotificationCreator {
        this.content = Payload.Content.BigPicture().also(init)
        return this
    }

    /**
     * Scoped function for modifying the content of a 'Message' notification.
     */
    fun asMessage(init: Payload.Content.Message.() -> Unit): NotificationCreator {
        this.content = Payload.Content.Message().also(init)
        return this
    }

    /**
     * Scoped function for modifying the 'Actions' of a notification. The transformation
     * relies on adding standard notification Action objects.
     */
    fun actions(init: ArrayList<Action>.() -> Unit): NotificationCreator {
        this.actions = ArrayList<Action>().also(init)
        return this
    }


    /**
     * Return the standard {@see NotificationCompat.Builder} after applying fluent API
     * transformations (if any) from the {@see NotifyCreator} builder object.
     */
    fun asBuilder(): NotificationCompat.Builder {
        return notify.asBuilder(RawNotification(meta, alerts, header, content, actions, progress))
    }

    /**
     * Delegate a {@see Notification.Builder} object to the NotificationInterop class which builds
     * and displays the notification.
     *
     * This is a terminal operation.
     *
     * @param id    An optional integer which will be used as the ID for the notification that is
     *              shown. This argument is ignored if the notification is a NotifyCreator#stackable
     *              receiver is set.
     * @return An integer corresponding to the ID of the system notification. Any updates should use
     * this returned integer to make updates or to cancel the notification.
     */
    @Deprecated(message = "Removed optional argument to alleviate confusion on ID that is used to create notification",
        replaceWith = ReplaceWith(
            "Notify.show()",
            "io.karn.notify.Notify"))
    fun show(id: Int?): Int {
        return notify.show(id, asBuilder())
    }

    /**
     * Delegate a @see{ Notification.Builder} object to the NotificationInterop class which builds
     * and displays the notification.
     *
     * This is a terminal operation.
     *
     * @return An integer corresponding to the ID of the system notification. Any updates should use
     * this returned integer to make updates or to cancel the notification.
     */
    fun show(): Int {
        return notify.show(null, asBuilder())
    }

    /**
     * Cancel an existing notification given an ID.
     *
     * @deprecated Choose to instead use the static function {@see Notify#cancelNotification()}
     * which provides the correct encapsulation of the this `cancel` function.
     */
    @Deprecated(message = "Exposes function under the incorrect API -- NotifyCreator is reserved strictly for notification construction.",
        replaceWith = ReplaceWith(
            "Notify.cancelNotification(context, id)",
            "android.content.Context", "io.karn.notify.Notify"))
    @Throws(NullPointerException::class)
    fun cancel(id: Int) {
        // This should be safe to call from here because the Notify.with(context) function call
        // would have initialized the NotificationManager object. In any case, the function has been
        // annotated as one which can throw a NullPointerException.
        return NotificationBuilder.cancelNotification(id)
    }
}