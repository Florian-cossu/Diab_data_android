package com.diabdata.castServer.castToUser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.text.format.Formatter
import androidx.core.app.NotificationCompat
import com.diabdata.castServer.AuthPlugin
import com.diabdata.castServer.utils.computeTrend
import com.diabdata.data.DiabDataDatabase
import com.diabdata.models.UserDetails
import com.diabdata.shared.R
import com.diabdata.utils.data.GsonFactory
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.GsonConverter
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

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

        authToken = (100000..999999).random().toString()

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
                    val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                    if (token != null && token == authToken) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "authenticated"))
                    } else if (token != null) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("message" to "invalid token"))
                    } else {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "reachable"))
                    }
                }

                route("/api") {
                    install(AuthPlugin)

                    get("/user") {
                        val userInfo = db.userDetailsDao().getUserDetails().first()
                        call.respond(userInfo ?: mapOf("message" to "No user profile found"))
                    }

                    put("/updateUser") {
                        val updatedUser = call.receive<UserDetails>()
                        db.userDetailsDao().upsertUserDetails(updatedUser)
                        call.respond(HttpStatusCode.OK, mapOf("message" to "User updated"))
                    }

                    get("/user/photo") {
                        val userInfo = db.userDetailsDao().getUserDetails().first()
                        val path = userInfo?.profilePhotoPath

                        if (path == null) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("message" to "No photo path")
                            )
                            return@get
                        }

                        val file = java.io.File(path)
                        if (!file.exists()) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("message" to "Photo file not found")
                            )
                            return@get
                        }

                        val bytes = file.readBytes()
                        val base64 =
                            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        val extension = file.extension.lowercase()
                        val mimeType = when (extension) {
                            "png" -> "image/png"
                            "webp" -> "image/webp"
                            "svg" -> "image/svg+xml"
                            else -> "image/jpeg"
                        }

                        call.respond(
                            mapOf(
                                "photo" to "data:$mimeType;base64,$base64"
                            )
                        )
                    }

                    post("/shutdown") {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "server shutting down"))

                        kotlinx.coroutines.delay(500)
                        val intent = Intent(this@CastToUserServerService, CastToUserServerService::class.java).apply {
                            action = ACTION_STOP
                        }
                        this@CastToUserServerService.stopSelf()
                    }

                    post("/dashboard") {
                        val today = LocalDateTime.now()

                        val dates = db.importantDateDao().getAllImportantDates().first()

                        // Poids sur 1 an
                        val allWeights =
                            db.weightDao().getWeightsSince(today.toLocalDate().minusYears(1))
                                .first()
                        val latestWeight = allWeights.maxByOrNull { it.date }?.let { latest ->
                            mapOf(
                                "value" to latest.value,
                                "date" to latest.date.toString(),
                                "trend" to computeTrend(
                                    entries = allWeights,
                                    valueExtractor = { it.value },
                                    dateExtractor = { it.date }
                                )
                            )
                        }

                        val allHba1c =
                            db.hba1cDao().getHBA1CEntriesSince(today.toLocalDate().minusYears(1))
                                .first()
                        val latestHba1c = allHba1c.maxByOrNull { it.date }?.let { latest ->
                            mapOf(
                                "value" to latest.value,
                                "date" to latest.date.toString(),
                                "trend" to computeTrend(
                                    entries = allHba1c,
                                    valueExtractor = { it.value },
                                    dateExtractor = { it.date }
                                )
                            )
                        }

                        val appointments =
                            db.appointmentDao().getUpcomingAppointmentsFlow(today).first()
                        val treatments =
                            db.treatmentDao().getUpcomingExpirationDatesFlow(today.toLocalDate())
                                .first()

                        call.respond(
                            mapOf(
                                "importantDates" to dates,
                                "latestWeight" to latestWeight,
                                "latestHba1c" to latestHba1c,
                                "upcomingAppointments" to appointments,
                                "activeTreatments" to treatments
                            )
                        )
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