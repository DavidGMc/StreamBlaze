package com.androidavid.streamblaze

import com.androidavid.streamblaze.RadioStation
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONArray
import org.json.JSONException

class RadioRepository {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60) // 1 hora
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun getRadioStations(callback: (Result<List<RadioStation>>) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val jsonString = remoteConfig.getString("radio_stations")
                val stations = parseRadioStationsJson(jsonString)
                callback(Result.Success(stations))
            } else {
                callback(Result.Error(task.exception ?: Exception("Failed to fetch remote config")))
            }
        }
    }

    private fun parseRadioStationsJson(jsonString: String): List<RadioStation> {
        val radioStations = mutableListOf<RadioStation>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val url = jsonObject.getString("url")
                val imageUrl = jsonObject.getString("imageUrl")
                radioStations.add(RadioStation(name, url, imageUrl))

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return radioStations
    }
}