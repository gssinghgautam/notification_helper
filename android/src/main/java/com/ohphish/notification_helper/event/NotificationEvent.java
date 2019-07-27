package com.ohphish.notification_helper.event;

import android.service.notification.StatusBarNotification;

public class NotificationEvent {

    private final StatusBarNotification sbn;

    public NotificationEvent(StatusBarNotification sbn){
        this.sbn = sbn;
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }
}
