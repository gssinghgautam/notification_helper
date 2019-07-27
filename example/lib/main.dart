import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:notification_helper/notification_helper.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _isNotificationEnabled = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  /*Future<void> initPlatformState() async {
    String platformVersion;

    try {
      platformVersion = await NotificationHelper.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }*/

  Future<void> initPlatformState() async {
    bool isNotificationEnabled;
    try {
      isNotificationEnabled =
          await NotificationHelper.isNotificationServiceEnabled;
    } on PlatformException {}

    if (!mounted) return;

    setState(() {
      _isNotificationEnabled = isNotificationEnabled;
    });
  }

  Future<void> enableNotification() async {
    bool isNotificationEnabled;
    try {
      isNotificationEnabled =
          await NotificationHelper.enableNotificationService;
    } on PlatformException {}

    if (!mounted) return;

    setState(() {
      _isNotificationEnabled = isNotificationEnabled;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: RaisedButton(
            onPressed: enableNotification,
            child: Text(_isNotificationEnabled ? "ENABLED" : "ENABLE"),
          ),
        ),
      ),
    );
  }
}
