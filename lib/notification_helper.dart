import 'dart:async';

import 'package:flutter/services.dart';

class NotificationHelper {
  static const MethodChannel _channel =
  const MethodChannel('notification_helper');

  static const EventChannel _event = const EventChannel("notification_stream");

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get isNotificationServiceEnabled =>
      _channel
          .invokeMethod('checkNotificationService')
          .then((result) => result == 1);
  
  static Future<String> enableDNDNotification({String args}) async{
    var _dndArgs = {
      "args": args
    };
    String _result = await _channel.invokeMethod("launchDndSettings", _dndArgs);
    return _result;
  }

  static Future<bool> get enableNotificationService =>
      _channel
          .invokeMethod("enableNotificationService")
          .then((result) => result == 1);

  static Future<String> show({String message, String length}) async {
    var _toastValues = {"message": message, "length": length};

    String _result = await _channel.invokeMethod("displayToast", _toastValues);

    return _result;
  }

  static Future<void> showNotification(String title, String body) async {
    var _notificationValues = {"title": title, "body": body};

    await _channel.invokeMethod("showNotification", _notificationValues);
  }

  static Stream<Map> get onNotificationReceived {
    var data = _event.receiveBroadcastStream().map<Map>((element) => element);
    return data;
  }
}
