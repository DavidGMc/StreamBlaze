package com.androidavid.streamblaze

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONArray
import org.json.JSONException

object FirebaseRemoteConfigHelper {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60) // 1 hour in seconds
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun fetchRemoteConfig(onComplete: () -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete()
                }
            }
    }

    fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    fun getInt(key: String): Int {
        return remoteConfig.getLong(key).toInt()
    }

    fun getStringList(key: String): List<String> {
        val jsonString = remoteConfig.getString(key)
        val stringList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                stringList.add(jsonArray.getString(i))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return stringList
    }
}
