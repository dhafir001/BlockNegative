package com.corechiper.blocknegative

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class DnsVpnService : VpnService(), Runnable {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        val LOCAL_BLACKLIST = hashSetOf(
            "facebook.com",
            "web.facebook.com",
            "m.facebook.com",
            "tiktok.com",
            "xnxx.com",
            "xvideos.com",
            "pornhub.com",
            "redtube.com",
            "rule34.xxx",
            "onlyfans.com",
            "chaturbate.com",
            "stripchat.com",
            "youporn.com",
            "tnaflix.com",
            "tube8.com",
            "spankbang.com"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, createNotification())
        if (!isRunning) {
            isRunning = true
            Thread(this, "BlockNegative-Engine").start()
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "BlockNegative_Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "CoreChiper Shield Protection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("BlockNegative Aktif")
            .setContentText("DNS Engine dikunci oleh CoreChiper. Akses terlindungi.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    private fun playAlertSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.decline)
            mediaPlayer?.start()
        } catch (_: Exception) {}
    }

    override fun run() {
        try {
            val builder = Builder()
            builder.setSession("CoreChiper Shield")
            builder.addAddress("10.0.0.2", 32)
            
            // Cloudflare Family Safe DNS (Adult & Malware filter bawaan)
            builder.addDnsServer("1.1.1.3")
            builder.addDnsServer("1.0.0.3")
            builder.addRoute("1.1.1.3", 32)
            builder.addRoute("1.0.0.3", 32)

            vpnInterface = builder.establish()
            val input = FileInputStream(vpnInterface?.fileDescriptor)
            val output = FileOutputStream(vpnInterface?.fileDescriptor)
            val packet = ByteBuffer.allocate(32767)

            while (isRunning) {
                val length = input.read(packet.array())
                if (length > 0) {
                    val packetData = packet.array().take(length).toByteArray()
                    val payload = String(packetData, Charsets.ISO_8859_1)

                    var blocked = false
                    for (domain in LOCAL_BLACKLIST) {
                        if (payload.contains(domain, ignoreCase = true)) {
                            blocked = true
                            playAlertSound()
                            showBlockedAlert(domain)
                            break
                        }
                    }

                    if (!blocked) {
                        output.write(packet.array(), 0, length)
                    }
                    packet.clear()
                }
            }
        } catch (_: Exception) {
        } finally {
            try { vpnInterface?.close() } catch (_: Exception) {}
        }
    }

    private fun showBlockedAlert(domain: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alert = NotificationCompat.Builder(this, "BlockNegative_Channel")
            .setContentTitle("Peringatan CoreChiper!")
            .setContentText("Akses ke domain terlarang ($domain) telah digagalkan.")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), alert)
    }

    override fun onDestroy() {
        isRunning = false
        mediaPlayer?.release()
        super.onDestroy()
    }
}
