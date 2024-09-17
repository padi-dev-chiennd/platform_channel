
import 'dart:convert';
import 'package:flutter/services.dart';
import 'dart:ui' as ui;
class CustomBinaryMessenger {
  static Future<void> givenValue(String data) async {
    final WriteBuffer buffer = WriteBuffer();
    // Convert the given data string into UTF-8 bytes.
    final List<int> utf8Bytes = utf8.encode(data);
    // Convert the UTF-8 bytes into an Uint8List.
    final Uint8List utf8Int8List = Uint8List.fromList(utf8Bytes);
    // Đặt Uint8List vào bộ đệm.
    buffer.putUint8List(utf8Int8List);
// Lấy dữ liệu tin nhắn nhị phân cuối cùng từ bộ đệm.
    final ByteData message = buffer.done();

    await Messenger().send('foo', message);
    return;
  }
}

class Messenger implements BinaryMessenger {
  @override
  //Xử lý các tin nhan nền tang đến. Trong truong hợp này, nó đưa ra một lỗi k dc hỗ trợ.
  Future<void> handlePlatformMessage(
      String channel, ByteData? data, PlatformMessageResponseCallback? callback) {
    throw UnsupportedError("This platform message handling is not supported.");
  }
  @override
// Gửi tin nhắn nhị phân tới nền tảng bằng cách sử dụng 'ui.PlatformDispatcher'.
  Future<ByteData?>? send(String channel, ByteData? message) {
// Sử dung 'ui.PlatformDispatcher' để gửi tin nhan nền tang và xử lý lenh gọi lại
    ui.PlatformDispatcher.instance.sendPlatformMessage(channel, message, (data) {});
    return null;
  }
  @override
// Đặt trình xử lý cho tin nhắn đến. Trong trường hợp này, nó đưa ra một lỗi không được hỗ trợ.
  void setMessageHandler(String channel, MessageHandler? handler) {
    throw UnsupportedError("Setting message handler is not supported.");
  }
}

