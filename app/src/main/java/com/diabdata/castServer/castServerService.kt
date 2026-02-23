package com.diabdata.castServer

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.text.format.Formatter
import androidx.core.app.NotificationCompat
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.diabdata.shared.R as R

class CastServerService : Service() {

    private var server: EmbeddedServer<*, *>? = null

    companion object {
        const val PORT = 8742
        const val CHANNEL_ID = "cast_server_channel"
        const val NOTIF_ID = 2001
        const val ACTION_STOP = "com.diabdata.cast.STOP_SERVER"

        // Pour observer l'état depuis le Composable
        var isRunning: Boolean = false
            private set

        var serverUrl: String? = null
            private set

        fun start(context: Context) {
            val intent = Intent(context, CastServerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CastServerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        startServer()
        startForeground(NOTIF_ID, buildNotification())
        return START_NOT_STICKY
    }

    private fun startServer() {
        val ip = getLocalIpAddress()
        serverUrl = "http://$ip:$PORT"

        server = embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) {
                gson { setPrettyPrinting() }
            }
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.ContentType)
            }
            routing {
                get("/ping") {
                    call.respondText("DiabData OK")
                }
                // On ajoutera les vraies routes après
            }
        }.start(wait = false)

        isRunning = true
    }

    private fun getLocalIpAddress(): String {
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, CastServerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.cast_to_desktop_icon_vector)
            .setContentTitle("Casting data")
            .setContentText("Server running — $serverUrl")
            .setSilent(true)
            .setOngoing(true)
            .addAction(
                R.drawable.stop_filled_icon_vector,
                "Stop server",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cast Server",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "DiabData local server for data casting"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        server?.stop(1000, 2000)
        server = null
        isRunning = false
        serverUrl = null
        super.onDestroy()
    }
}