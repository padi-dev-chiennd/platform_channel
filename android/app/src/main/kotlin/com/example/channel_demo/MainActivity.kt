package com.example.channel_demo


import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random


class MainActivity : FlutterActivity() {
    private lateinit var reply: BasicMessageChannel.Reply<Any>
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
//        BinaryMessenger
        flutterEngine.dartExecutor.binaryMessenger.setMessageHandler("foo") { message, reply ->
            message?.order(ByteOrder.nativeOrder()) // Ensure proper byte order.
            val data = decodeUtf8String(message!!) // Decode the binary data to UTF-8 string.
            Toast.makeText(this, "Received message from Flutter: $data", Toast.LENGTH_SHORT).show()
            reply.reply(null)
        }

        // MethodChannel to handle communication between Flutter and the platform.
        MethodChannel(flutterEngine.dartExecutor, "methodChannelDemo")
            .setMethodCallHandler { call, result ->
                //call: đại diện cho lệnh gọi phương thức đến như tên phương thức và các đối số từ Flutter
                //result: thể hiện kết quả hoặc phản hồi mà bạn gửi lại cho Flutter sau khi xử lý lệnh gọi phương thức.
                // Truy xuất đối số 'count' từ lệnh gọi phương thức, nếu được cung cấp.
                val count: Int? = call.argument<Int>("count")
                when (call.method) {
                    "random" -> {
                        result.success(Random(System.nanoTime()).nextInt(0, 100))
                    }
                    "intent" -> {
                        val intent = Intent(this,NavigateActivity::class.java)
                        startActivity(intent)
                    }
                    "increment" -> {
                        if (count == null) {
                            result.error("INVALID ARGUMENT", "Invalid Argument", null)
                        } else {
                            result.success(count + 1)
                        }
                    }
                    "decrement" -> {
                        if (count == null) {
                            result.error("INVALID ARGUMENT", "Invalid Argument", null)
                        } else {
                            result.success(count - 1)
                        }
                    }
                    else -> {
                        result.notImplemented()
                    }
                }
            }

        //Event Channel
        EventChannel(flutterEngine.dartExecutor, "eventChannelTimer")
            .setStreamHandler(CustomStreamHandler())

        //BasicMessageChannel
        BasicMessageChannel(flutterEngine.dartExecutor, "platformImageDemo", StandardMessageCodec())
            .setMessageHandler { message, reply ->
                // message: đại diện cho tin nhắn đến từ Flutter
                //result: thể hiện kết quả hoặc phản hồi mà bạn gửi lại cho Flutter sau khi xử lý lệnh gọi phương thức.
                Toast.makeText(this, "Received message from Flutter: $message", Toast.LENGTH_SHORT).show();
                if (message == "getImage") {
                    startCrop()
                    this.reply = reply
                }
            }

        super.configureFlutterEngine(flutterEngine)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            val imageBytes = resultUri?.let { uriToByteArray(it) }
            reply.reply(imageBytes)

        }
    }
    private fun uriToByteArray(uri: Uri): ByteArray {
        val inputStream: InputStream = contentResolver.openInputStream(uri)!!
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

    // Đọc file từ assets và lưu vào file tạm thời
    private fun copyAssetToFile(): Uri {
        val inputStream: InputStream = assets.open("flutter.png")
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "flutter_tmp.png")
        val outputStream = FileOutputStream(file)

        // Ghi dữ liệu từ InputStream vào file
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.close()
        inputStream.close()

        return Uri.fromFile(file)
    }

    private fun startCrop() {
        val sourceUri = copyAssetToFile()
        val destinationUri = Uri.fromFile(File(getCacheDir(), "flutter_cropped.png"))

        // Sử dụng UCrop để cắt ảnh
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(16f, 9f)
            .withMaxResultSize(1080, 1920)
            .start(this)
    }


    private fun decodeUtf8String(byteBuffer: ByteBuffer): String {
        return try {
            val byteArray = ByteArray(byteBuffer.remaining())
            byteBuffer.get(byteArray)
            String(byteArray, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
