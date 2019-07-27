package com.ohphish.notification_helper;

import android.app.PendingIntent;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.ohphish.notification_helper.event.NotificationEvent;
import com.ohphish.notification_helper.services.BaseNotificationListener;
import com.ohphish.notification_helper.utils.NotificationDebugUtils;
import com.ohphish.notification_helper.utils.NotificationUtils;

import org.greenrobot.eventbus.EventBus;

import static com.ohphish.notification_helper.utils.NotificationDebugUtils.getMessageContent;
import static com.ohphish.notification_helper.utils.NotificationDebugUtils.getTitle;

public class NotificationService extends BaseNotificationListener {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected boolean shouldAppBeAnnounced(StatusBarNotification sbn) {
        return true;
    }

    @Override
    protected void onNotificationPosted(StatusBarNotification sbn, String dismissKey) {

        EventBus.getDefault().post(new NotificationEvent(sbn));
        NotificationDebugUtils.printNotification(sbn);
        Toast.makeText(this, NotificationDebugUtils.getTitle(sbn.getNotification()), Toast.LENGTH_LONG).show();

        String message = getMessageContent(sbn.getNotification()).toLowerCase();
        String msgToSend = "";
        switch (message) {
            case "@ohphish":
                msgToSend = "Welcome To OhPhish. \n\n@phishing - To Launch Email Phishing.\n\n@smshing - To Launch SMS Phishing. \n\n@vishing - To Launch Call Phishing";
                break;
            case "@phishing":
                msgToSend = "Email Phishing. \n\nPlease provide your login credential.\n\n ";
                break;
            case "@smshing":
                msgToSend = "Sms Phishing \n\nPlease provide your login credential.\n\n ";
                break;
            case "@vishing":
                msgToSend = "Call Phishing \n\nPlease provide your login credential.\n\n ";
                break;
        }

        try {
            NotificationUtils.getQuickReplyAction(sbn.getNotification(), "com.whatsapp")
                    .sendReply(getApplicationContext(), msgToSend);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        /*
        if (getTitle(sbn.getNotification()).replaceAll("\\s", "").equalsIgnoreCase("+917059335546")) {
        } else if (getTitle(sbn.getNotification()).equalsIgnoreCase("+91 70593 35546 @ \uD83D\uDC6CF.R.I.E.N.D.S.\uD83D\uDC6C")) {
            try {
                NotificationUtils.getQuickReplyAction(sbn.getNotification(), "com.whatsapp")
                        .sendReply(getApplicationContext(), "Badal Madarchod");
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        Log.d("SENDER", getTitle(sbn.getNotification()).replaceAll("\\s", ""));*/
    }
}
