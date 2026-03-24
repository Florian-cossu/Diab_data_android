package com.diabdata.widget

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.diabdata.glanceWidget.proto.WidgetState
import java.io.InputStream
import java.io.OutputStream

object WidgetStateSerializer : Serializer<WidgetState> {
    override val defaultValue: WidgetState = WidgetState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): WidgetState {
        try {
            return WidgetState.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: WidgetState, output: OutputStream) {
        t.writeTo(output)
    }
}