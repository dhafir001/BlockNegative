package com.corechiper.blocknegative

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest
import java.util.UUID

class LockOverlayActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_overlay)

        playDeclineSound()

        val textTitle = findViewById<TextView>(R.id.txtTitle)
        val inputPass = findViewById<EditText>(R.id.edtPassword)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)
        val btnSurrender = findViewById<Button>(R.id.btnSurrender)

        textTitle.text = "AKSES DIBLOKIR\nCoreChiper Defense System"

        val deviceSecret = generateDeviceKey()

        btnUnlock.setOnClickListener {
            val input = inputPass.text.toString().trim()
            if (input == deviceSecret) {
                finish()
            } else {
                playDeclineSound()
                Toast.makeText(this, "Kombinasi salah. Sistem terkunci rapat.", Toast.LENGTH_SHORT).show()
                inputPass.text.clear()
            }
        }

        btnSurrender.setOnClickListener {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finishAffinity()
        }
    }

    private fun playDeclineSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.decline)
            mediaPlayer?.start()
        } catch (_: Exception) {}
    }

    private fun generateDeviceKey(): String {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "FALLBACK"
        val raw = androidId + UUID.randomUUID().toString()
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(raw.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finishAffinity()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
