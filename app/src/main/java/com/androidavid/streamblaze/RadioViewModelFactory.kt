package com.androidavid.streamblaze



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RadioViewModelFactory(private val radioRepository: RadioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RadioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RadioViewModel(radioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}