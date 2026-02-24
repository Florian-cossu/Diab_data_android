package com.diabdata.castServer

import com.diabdata.castServer.castToUser.CastToUserServerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

val AuthPlugin = createRouteScopedPlugin("AuthPlugin") {
    onCall { call ->
        // Pas de vérification sur /ping (pour tester la connexion)
        if (call.request.path() == "/ping") return@onCall

        val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
        if (token != CastToUserServerService.authToken) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
            return@onCall
        }
    }
}