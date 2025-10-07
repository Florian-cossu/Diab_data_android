package com.diabdata.glanceWidget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object WidgetGlanceStateDefinition :
    GlanceStateDefinition<com.diabdata.glanceWidget.proto.WidgetState> {

    // Emplacement du fichier sur disque (peut être constant si tu veux un seul fichier pour tous les widgets)
    override fun getLocation(context: Context, fileKey: String): File {
        val dir = File(context.filesDir, "glance")
        if (!dir.exists()) dir.mkdirs()
        // Utilise un nom fixe (ici "widget_state.pb") pour que le Worker et Glance pointent vers le même fichier
        return File(dir, "widget_state.pb")
    }

    // Retourne l'instance DataStore<WidgetState> — ici on réutilise ton singleton context.widgetDataStore
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<com.diabdata.glanceWidget.proto.WidgetState> {
        return context.widgetDataStore
    }
}
