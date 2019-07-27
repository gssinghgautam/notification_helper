package com.ohphish.notification_helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ohphish.notification_helper.event.NotificationEvent;
import com.ohphish.notification_helper.model.NotificationHelper;
import com.ohphish.notification_helper.utils.NotificationDebugUtils;
import com.ohphish.notification_helper.utils.NotificationListenerUtils;
import com.ohphish.notification_helper.utils.NotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * NotificationHelperPlugin
 */
public class NotificationHelperPlugin implements MethodCallHandler, PluginRegistry.NewIntentListener, PluginRegistry.ActivityResultListener {

    private static final String METHOD_CHANNEL = "notification_helper";
    private static final String STREAM_CHANNEL = "notification_stream";
    private static final String ENABLE_NOTIFICATION_SERVICE = "enableNotificationService";
    private static final String CHECK_NOTIFICATION_SERVICE = "checkNotificationService";
    private static final String DISPLAY_TOAST = "displayToast";
    private static final String SHOW_NOTIFICATION = "showNotification";
    private static final String LAUNCH_DND_SETTINGS = "launchDndSettings";
    private final Registrar registrar;
    private Result result;
    private EventChannel.EventSink onNotificationEvent;

    private NotificationHelperPlugin(Registrar registrar) {
        this.registrar = registrar;
        this.registrar.addNewIntentListener(this);
        MethodChannel channel = new MethodChannel(registrar.messenger(), METHOD_CHANNEL);
        channel.setMethodCallHandler(this);
        registrar.addActivityResultListener(this);
        EventBus.getDefault().register(this);
        create(registrar);
    }


    private void create(Registrar registrar) {
        final EventChannel onNotificationEventChannel = new EventChannel(registrar.messenger(), STREAM_CHANNEL);
        onNotificationEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink eventSink) {
                onNotificationEvent = eventSink;
            }

            @Override
            public void onCancel(Object o) {

            }
        });
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {

        new NotificationHelperPlugin(registrar);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case CHECK_NOTIFICATION_SERVICE:
                checkIfNotificationServiceIsEnabled(result);
                break;
            case ENABLE_NOTIFICATION_SERVICE:
                enableNotificationService(result);
                break;
            case DISPLAY_TOAST:
                String message = call.argument("message");
                String length = call.argument("length");
                displayToast(message, "long".equalsIgnoreCase(length) ? 1 : 0);
                break;
            case SHOW_NOTIFICATION:
                String title = call.argument("title");
                String body = call.argument("body");

                NotificationHelper.showNotification(registrar.context(), title, body);

                break;
            case LAUNCH_DND_SETTINGS:
                String args = call.argument("args");
                int interruptFilter;
                String filterOn;
                if ("enable".equalsIgnoreCase(args)) {
                    interruptFilter = NotificationManager.INTERRUPTION_FILTER_ALARMS;
                    filterOn = "1";
                } else {
                    interruptFilter = NotificationManager.INTERRUPTION_FILTER_ALL;
                    filterOn = "0";
                }
                NotificationListenerUtils.changeInterruptionFiler(registrar.context(), interruptFilter);
                result.success(filterOn);
                break;
            default:
                result.notImplemented();
        }

        /*if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else {
            result.notImplemented();
        }*/
    }

    private void checkIfNotificationServiceIsEnabled(Result result) {
        String enabledNotificationListeners =
                android.provider.Settings.Secure.getString(registrar.context().getContentResolver(),
                        "enabled_notification_listeners");
        boolean isEnabled = enabledNotificationListeners != null && enabledNotificationListeners.contains(registrar.context().getPackageName());

        result.success(isEnabled ? 1 : 0);
    }

    private void enableNotificationService(Result result) {
        registrar.activity().startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 12);
        this.result = result;
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        return false;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("TAG", "onActivityResult: " + requestCode);
        if (requestCode == 12) {
            checkIfNotificationServiceIsEnabled(result);
        }
        return false;
    }

    private void displayToast(String message, int length) {
        Toast.makeText(registrar.context(), message, length).show();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(NotificationEvent event) {
        NotificationDebugUtils.printNotification(event.getSbn());

        ApplicationInfo appInfo = ((ApplicationInfo) event.getSbn().getNotification().extras.get("android.appInfo"));
        if (appInfo != null) {
            String appName = registrar.context().getPackageManager().getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName;
            String title = NotificationDebugUtils.getTitle(event.getSbn().getNotification());
            String body = NotificationDebugUtils.getMessageContent(event.getSbn().getNotification());
            String notificationId = String.valueOf(event.getSbn().getId());
            String createdAt = String.valueOf(event.getSbn().getNotification().when);
            String postedAt = String.valueOf(event.getSbn().getPostTime());

            final Map<String, String> dataMap = new HashMap<>();
            dataMap.put("appName", appName);
            dataMap.put("packageName", packageName);
            dataMap.put("title", title);
            dataMap.put("body", body);
            dataMap.put("notificationId", notificationId);
            dataMap.put("createdAt", createdAt);
            dataMap.put("postedAt", postedAt);

            if (onNotificationEvent != null) {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                onNotificationEvent.success(dataMap);
                            }
                        });
            }
        }
    }

}
