package com.snaprelay.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import okhttp3.*
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val apiUrl = "https://YOUR-HTTPS-SERVER.example.com/upload"
    private var bitmap: Bitmap? = null
    private lateinit var preview: ImageView
    private lateinit var send: Button
    private val camera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) { bitmap = r.data?.extras?.get("data") as? Bitmap; preview.setImageBitmap(bitmap); send.isEnabled = bitmap != null }
    }
    override fun onCreate(state: Bundle?) { super.onCreate(state)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,48,32,32); setBackgroundColor(0xFF0A0D14.toInt()) }
        val title = TextView(this).apply { text = "SnapRelay\nCamera → Telegram"; textSize = 28f; setTextColor(0xFFFFFFFF.toInt()) }
        preview = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; setBackgroundColor(0xFF111722.toInt()) }
        val capture = Button(this).apply { text = "Take photo"; setOnClickListener { openCamera() } }
        send = Button(this).apply { text = "Send to Telegram"; isEnabled = false; setOnClickListener { upload() } }
        root.addView(title); root.addView(preview, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(capture); root.addView(send); setContentView(root)
    }
    private fun openCamera() { if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.CAMERA), 42) else camera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE)) }
    private fun upload() { val b = bitmap ?: return; val out = ByteArrayOutputStream(); b.compress(Bitmap.CompressFormat.JPEG, 90, out); val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("caption", "SnapRelay capture").addFormDataPart("photo", "capture.jpg", out.toByteArray().toRequestBody("image/jpeg".toMediaType())).build(); send.isEnabled = false; OkHttpClient().newCall(Request.Builder().url(apiUrl).post(body).build()).enqueue(object: Callback { override fun onFailure(c: Call, e: java.io.IOException) = runOnUiThread { Toast.makeText(this@MainActivity, "Send failed", Toast.LENGTH_LONG).show(); send.isEnabled = true }; override fun onResponse(c: Call, r: Response) = runOnUiThread { Toast.makeText(this@MainActivity, if(r.isSuccessful) "Sent" else "Server error", Toast.LENGTH_LONG).show(); send.isEnabled = true } }) }
}
