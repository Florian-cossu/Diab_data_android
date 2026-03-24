package com.diabdata.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.diabdata.glanceWidget.proto.WidgetState

val Context.widgetDataStore: DataStore<WidgetState> by dataStore(
    fileName = "widget_state.pb",
    serializer = WidgetStateSerializer
)