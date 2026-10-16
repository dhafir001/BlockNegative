package com.corechiper.blocknegative

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartProtection = findViewById<Button>(R.id.btnStartProtection)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        btnStartProtection.setOnClickListener {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
            } else {
                startVpnService()
            }
        }

        btnAccessibility.setOnClickListener {
            Toast.makeText(this, "Aktifkan AppLockService di menu Aksesibilitas", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, DnsVpnService::class.java)
        startService(intent)
        findViewById<TextView>(R.id.txtStatus).text = "Status: Proteksi Aktif & DNS Terkunci"
        Toast.makeText(this, "Proteksi BlockNegative Aktif!", Toast.LENGTH_SHORT).show()
    }
}
