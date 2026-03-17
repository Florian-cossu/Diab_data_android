package com.diabdata.relay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RelayViewModel : ViewModel() {

    private val relayClient = RelayClient()

    // ═══════════════════════════════════════════
    //  STATE exposé à l'UI
    // ═══════════════════════════════════════════

    val connectionState: StateFlow<ConnectionState> = relayClient.connectionState

    val token: String?
        get() = relayClient.currentToken

    val mode: ShareMode?
        get() = relayClient.currentMode

    // ═══════════════════════════════════════════
    //  ACTIONS
    // ═══════════════════════════════════════════

    fun startSharing(mode: ShareMode, durationMinutes: Int = 30) {
        viewModelScope.launch {
            relayClient.startSharing(mode, durationMinutes)
        }
    }

    fun stopSharing() {
        viewModelScope.launch {
            relayClient.stopSharing()
        }
    }

    // ═══════════════════════════════════════════
    //  INCOMING REQUESTS — Le front demande des données
    // ═══════════════════════════════════════════

    init {
        viewModelScope.launch {
            relayClient.incomingRequests.collect { forward ->
                handleRequest(forward)
            }
        }
    }

    private suspend fun handleRequest(forward: ForwardMessage) {
        // TODO: Ici on va :
        // 1. Déchiffrer le payload
        // 2. Parser la route demandée
        // 3. Aller chercher les données dans Room
        // 4. Chiffrer la réponse
        // 5. Renvoyer via le relai

        // Pour l'instant on renvoie un placeholder
        val responsePayload = """{"status":"ok","data":"placeholder"}"""

        relayClient.sendResponse(
            requestId = forward.requestId,
            clientId = forward.clientId,
            payload = responsePayload
        )
    }
}