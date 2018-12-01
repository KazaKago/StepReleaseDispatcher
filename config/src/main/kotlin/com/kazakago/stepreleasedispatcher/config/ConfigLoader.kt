package com.kazakago.stepreleasedispatcher.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.InputStream

object ConfigLoader {

    private const val preferencesFolder = "preferences"
    private val p12KeyFile = File(preferencesFolder, "key.p12")
    private val configFile = File(preferencesFolder, "config.json")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(DoubleListAdapter.FACTORY)
        .build()
    private val jsonAdapter = moshi.adapter(Config::class.java)

    init {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.createNewFile()
            apply(Config())
        }
    }

    fun load(): Config {
        val json = configFile.readLines().joinToString()
        return jsonAdapter.fromJson(json)!!
    }

    fun apply(config: Config) {
        val json = jsonAdapter.toJson(config)
        configFile.writer().use { it.write(json) }
    }

    fun putP12KeyFile(value: InputStream) {
        p12KeyFile.outputStream().buffered().use { value.copyTo(it) }
    }

    fun getP12KeyFile(): File {
        return p12KeyFile
    }

}