import 'dart:async';

import 'package:flutter/services.dart';

class Ttsiflytek {
  static const MethodChannel _channel =
      const MethodChannel('ttsiflytek');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> initTTS({String appid}) async {
    if(appid != null){
      return _channel.invokeMethod('initTTS',{'appid':appid});
    }else{
      return _channel.invokeMethod('initTTS');
    }
  }
  static Future<void> play(String str) async {
    return _channel.invokeMethod('play',{'txt':str});
  }
  static Future<void> cancel() async {
    return _channel.invokeMethod('cancel');
  }
  static Future<void> pause() async {
    return _channel.invokeMethod('pause');
  }
  static Future<void> resume() async {
    return _channel.invokeMethod('resume');
  }
  static Future<void> destroy() async {
    return _channel.invokeMethod('destroy');
  }
}
