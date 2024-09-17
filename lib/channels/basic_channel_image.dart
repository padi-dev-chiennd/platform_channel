import 'package:flutter/services.dart';

class BasicChannelImage {
  static const _basicMessageChannel =
    BasicMessageChannel<dynamic>('platformImageDemo', StandardMessageCodec(),);
  static Future<Uint8List> getImage() async {
    // Gửi tin nhắn để yêu cầu hình ảnh từ nền tảng bằng BasicMessageChannel.
    final reply = await _basicMessageChannel.send('getImage') as Uint8List?;

    if (reply == null) {
      throw PlatformException(
        code: 'Error',
        message: 'Failed to load Platform Image',
        details: null,
      );
    }
    return reply;
  }
}