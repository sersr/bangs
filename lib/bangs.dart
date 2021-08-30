import 'dart:async';
import 'dart:ui' as ui;

import 'package:flutter/painting.dart';
import 'package:flutter/services.dart';

class ViewInsets {
  const ViewInsets({required this.padding, required this.size});
  final EdgeInsets padding;
  final Size size;
  static const zero = ViewInsets(padding: EdgeInsets.zero, size: Size.zero);
}

typedef NavigationChange = void Function(bool isShow, int height);

class Bangs {
  Bangs._() {
    _channel.setMethodCallHandler(methordCallbackHandler);
  }
  static const MethodChannel _channel = MethodChannel('bangs');

  static Bangs bangs = Bangs._();

  NavigationChange? _navigationChangeCallback;

  void setNavigationChangeCallback(NavigationChange? callback) {
    _navigationChangeCallback = callback;
  }

  Future methordCallbackHandler(MethodCall call) async {
    final method = call.method;
    if (method == 'navigationChange') {
      if (_navigationChangeCallback != null) {
        final isShowing = call.arguments['isShowing'];
        final height = call.arguments['height'];
        _navigationChangeCallback!(isShowing, height);
      }
    }
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<ViewInsets> get safePadding async {
    final map = await _channel.invokeMethod('safePadding');
    if (map is! Map) {
      throw Exception('error');
    }
    final uiSize = ui.window.physicalSize;
    final top = map['top'] ?? 0.0;
    final left = map['left'] ?? 0.0;
    final right = map['right'] ?? 0.0;
    final botom = map['bottom'] ?? 0.0;
    final height = map['height'] ?? uiSize.height;
    final width = map['width'] ?? uiSize.width;
    final _padding =
        EdgeInsets.only(top: top, left: left, right: right, bottom: botom) /
            ui.window.devicePixelRatio;
    assert(() {
      print('$_padding');
      return true;
    }());
    final viewInsets = ViewInsets(
        padding: _padding,
        size: Size(width, height) / ui.window.devicePixelRatio);
    return viewInsets;
  }

  static Future<int> get bottomHeight async {
    final height = await _channel.invokeMethod('bottomHeight');
    return height;
  }

  static Future get restart async {
    // await _channel.invokeMethod('restart');
  }
}
