import 'dart:async';

import 'package:flutter/services.dart';

class WalletConnectPlugin {
  static const MethodChannel _channel = MethodChannel('wallet_connect_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> connectWallet(String uri) async =>
      await _channel.invokeMethod('connectWallet', {"uri": uri});

  static Future<void> showBiometricDialog() async =>
      await _channel.invokeMethod('showBiometricDialog');
}
