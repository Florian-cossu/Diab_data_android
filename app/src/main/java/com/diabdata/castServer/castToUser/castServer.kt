package com.diabdata.castServer.castToUser

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.text.format.Formatter
import androidx.core.app.NotificationCompat
import com.diabdata.castServer.AuthPlugin
import com.diabdata.data.DiabDataDatabase
import com.diabdata.utils.data.GsonFactory
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import com.diabdata.shared.R as R

class CastToUserServerService : Service() {

    private var server: EmbeddedServer<*, *>? = null

    companion object {
        const val PORT = 8742
        const val CHANNEL_ID = "cast_server_channel"
        const val NOTIF_ID = 2001
        const val ACTION_STOP = "com.diabdata.cast.STOP_SERVER"

        var isRunning: Boolean = false
            private set

        var serverUrl: String? = null
            private set

        var authToken: String? = null
            private set

        fun start(context: Context) {
            val intent = Intent(context, CastToUserServerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CastToUserServerService::class.java)
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
        val db = DiabDataDatabase.getDatabase(this)
        serverUrl = "http://$ip:$PORT"

        authToken = java.util.UUID.randomUUID().toString()

        server = embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, GsonConverter(GsonFactory.create(prettyPrint = true)))
            }
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.ContentType)
                allowHeader(HttpHeaders.Authorization)
            }
            routing {
                get("/ping") {
                    call.respond(mapOf("message" to "successfully connected to DiabData server", "statusCode" to "200"))
                }

                route("/api") {
                    install(AuthPlugin)

                    get("/user") {
                        val userInfo = db.userDetailsDao().getUserDetails().first()
                        call.respond(userInfo ?: mapOf("message" to "No user profile found"))
                    }
                }

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
        val stopIntent = Intent(this, CastToUserServerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.cast_to_desktop_icon_vector)
            .setContentTitle("Casting data")
            .setContentText("Server running — $serverUrl\ntoken: $authToken")
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