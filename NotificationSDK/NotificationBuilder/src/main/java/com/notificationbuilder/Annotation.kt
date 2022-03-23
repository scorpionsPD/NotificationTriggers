package com.notificationbuilder

import androidx.annotation.IntDef

/**
 * Denotes features which are considered experimental and are subject to change without notice.
 */
annotation class Experimental

@DslMarker
annotation class NotifyScopeMarker

@Retention(AnnotationRetention.SOURCE)
@IntDef(NotificationBuilder.IMPORTANCE_MIN,
    NotificationBuilder.IMPORTANCE_LOW,
    NotificationBuilder.IMPORTANCE_NORMAL,
    NotificationBuilder.IMPORTANCE_HIGH,
    NotificationBuilder.IMPORTANCE_MAX)
annotation class NotificationImportance