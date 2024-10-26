package com.androidavid.streamblaze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidavid.streamblaze.RadioStation
import com.androidavid.streamblaze.Result

class RadioViewModel(private val radioRepository: RadioRepository) : ViewModel() {
    private val _radioStations = MutableLiveData<List<RadioStation>?>()
    val radioStations: LiveData<List<RadioStation>?> = _radioStations

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    init {
        loadRadioStations()
    }

    fun loadRadioStations() {
        radioRepository.getRadioStations { result ->
            when (result) {
                is Result.Success -> {
                    _radioStations.postValue(result.data)
                    _error.postValue(null)
                }
                is Result.Error -> {
                    _error.postValue(result.exception.message)
                }
            }
        }
    }


    fun setPlaying(isPlaying: Boolean) {
        _isPlaying.postValue(isPlaying)
    }
}