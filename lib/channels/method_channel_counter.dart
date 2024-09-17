import 'package:flutter/services.dart'; 
class MethodChannelCounter {
  static MethodChannel methodChannel = const MethodChannel('methodChannelDemo');

  static Future<int> increment({required int counterValue}) async {
    final result = await methodChannel.invokeMethod<int>('increment', {'count': counterValue});
    return result!;
  }

  static Future<int> decrement({required int counterValue}) async {
    final result = await methodChannel.invokeMethod<int>('decrement', {'count': counterValue});
    return result!;
  }

  static Future<int> randomValue() async {
    final result = await methodChannel.invokeMethod<int>('random');
    return result!;
  }

  static Future intentNative() async {
    final result = await methodChannel.invokeMethod('intent');
    return result!;
  }
}
