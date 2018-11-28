package com.kazakago.stepreleasedispatcher.config

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Types

class DoubleListAdapter : JsonAdapter<List<Double>>() {

    companion object {
        val FACTORY = Factory { type, _, _ ->
            val doubleListType = Types.newParameterizedType(List::class.java, Double::class.javaObjectType)
            if (type === doubleListType) {
                return@Factory DoubleListAdapter()
            }
            null
        }
    }

    override fun fromJson(reader: JsonReader): List<Double> {
        return reader.nextString().trim().split(",").map { it.toDouble() }
    }

    override fun toJson(writer: JsonWriter, value: List<Double>?) {
        writer.value(value?.joinToString(",") ?: "")
    }

}
