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
        //BinaryMessenger
//        flutterEngine.dartExecutor.binaryMessenger.setMessageHandler("foo") { message, reply ->
//            message?.order(ByteOrder.nativeOrder()) // Ensure proper byte order.
//            val data = decodeUtf8String(message!!) // Decode the binary data to UTF-8 string.
//            Toast.makeText(this, "Received message from Flutter: $data", Toast.LENGTH_SHORT).show()
//            reply.reply(null)
//        }

        // MethodChannel to handle communication between Flutter and the platform.
        MethodChannel(flutterEngine.dartExecutor, "methodChannelDemo")
            .setMethodCallHandler { call, result ->
                //call: represents the incoming method call like method name, and arguments from Flutter
                //result: represents result or response that you send back to Flutter after handling the method call.
                // Retrieve the 'count' argument from the method call, if provided.
                val count: Int? = call.argument<Int>("count")
                // Determine which method was called from Flutter.
                when (call.method) {
                    // Handle the 'random' method call.
                    "random" -> {
                        // Generate a random number between 0 and 100 and send it back to Flutter as a success result.
                        result.success(Random(System.nanoTime()).nextInt(0, 100))
                    }
                    // Handle the 'increment' and 'decrement' method calls.
                    "increment", "decrement" -> {
                        // Check if the 'count' argument is missing or invalid.
                        if (count == null) {
                            // If the 'count' argument is missing or invalid, send an error result to Flutter.
                            result.error("INVALID ARGUMENT", "Invalid Argument", null)
                        } else {
                            // If the 'count' argument is valid, perform the requested operation.
                            if (call.method == "increment") {
                                // Increment the 'count' and send the updated value to Flutter as a success result.
                                result.success(count + 1)
                            } else {
                                // Decrement the 'count' and send the updated value to Flutter as a success result.
                                result.success(count - 1)
                            }
                        }
                    }
                    // Handle any other method calls that are not implemented.
                    else -> {
                        // Send a "not implemented" result to Flutter.
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
                //message represents the incoming message from Flutter
                //result represents result or response that you send back to Flutter after handling the method call.
                // Toast message indicating the received message from Flutter.
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
            // resultUri chứa Uri của ảnh đã cắt
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
    fun copyAssetToFile(): Uri {
        val inputStream: InputStream = assets.open("flutter.png") // Đọc file từ assets
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "flutter_tmp.png") // File tạm thời
        val outputStream = FileOutputStream(file)

        // Ghi dữ liệu từ InputStream vào file
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.close()
        inputStream.close()

        return Uri.fromFile(file) // Trả về Uri của file tạm thời
    }

    private fun startCrop() {
        val sourceUri = copyAssetToFile() // Lấy Uri của file tạm thời
        val destinationUri = Uri.fromFile(File(getCacheDir(), "flutter_cropped.png")) // Tệp lưu ảnh cắt

        // Sử dụng UCrop để cắt ảnh
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(16f, 9f) // Tỷ lệ cắt 16:9
            .withMaxResultSize(1080, 1920) // Kích thước tối đa
            .start(this) // Bắt đầu cắt ảnh
    }


//    private fun decodeUtf8String(byteBuffer: ByteBuffer): String {
//        return try {
//            val byteArray = ByteArray(byteBuffer.remaining())
//            byteBuffer.get(byteArray)
//            String(byteArray, Charsets.UTF_8)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ""
//        }
//    }
}
