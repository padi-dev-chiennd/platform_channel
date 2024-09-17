import 'package:flutter/services.dart';

class EventChannelTimer {
  static const _eventChannelCustom = EventChannel('eventChannelTimer');

  static Stream<int> get timerValue {
// Sử dụng phương thức getBroadcastStream để tạo luồng sự kiện từ phía nền tảng.
    // Ánh xạ các sự kiện động thành số nguyên khi chúng được nhận.
    return _eventChannelCustom.receiveBroadcastStream().map(
          (dynamic event) => event as int,
    );
  }

}
