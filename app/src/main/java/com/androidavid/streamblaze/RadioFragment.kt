package com.androidavid.streamblaze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import coil.load
import com.androidavid.streamblaze.databinding.FragmentRadioBinding
import com.google.android.material.snackbar.Snackbar

class RadioFragment : Fragment() {
    private lateinit var binding: FragmentRadioBinding
    private lateinit var radioViewModel: RadioViewModel
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var isFirstPlay = true
    private lateinit var localBroadcastManager: LocalBroadcastManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRadioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext())

        // Crear ViewModel
        val factory = RadioViewModelFactory(RadioRepository())
        radioViewModel = ViewModelProvider(requireActivity(), factory)[RadioViewModel::class.java]

        // Obtener los argumentos
        val stationUrl = arguments?.getString("stationUrl") ?: return
        val stationName = arguments?.getString("stationName") ?: "Radio Station"
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        setupUI(stationName, imageUrl)
        setupPlayPauseButton(stationUrl, view)
        setupBroadcastReceiver()
        observePlaybackState()
    }

    private fun setupUI(stationName: String, imageUrl: String) {
        binding.textViewRadioName.text = stationName
        binding.imageViewRadio.load(imageUrl) {
            placeholder(R.drawable.library_music)
            error(R.drawable.library_music)
        }
    }

    private fun setupPlayPauseButton(stationUrl: String, view: View) {
        binding.buttonPlayPause.setOnClickListener {
            val intent = Intent(requireContext(), RadioService::class.java)

            if (radioViewModel.isPlaying.value == true) {
                intent.action = RadioService.ACTION_TOGGLE_PLAY_PAUSE
            } else {
                intent.action = RadioService.ACTION_PLAY
                intent.putExtra("url", stationUrl)
            }

            if (isFirstPlay && radioViewModel.isPlaying.value != true) {
                Snackbar.make(view, "Cargando... espera un momento", Snackbar.LENGTH_LONG).show()
                isFirstPlay = false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        }
    }

    private fun setupBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == RadioService.ACTION_STATUS) {
                    val isPlaying = intent.getBooleanExtra(RadioService.EXTRA_IS_PLAYING, false)
                    radioViewModel.setPlaying(isPlaying)
                    Log.d("RadioFragment", "Received broadcast: isPlaying = $isPlaying")
                }
            }
        }

        // Registrar el receptor con LocalBroadcastManager
        val filter = IntentFilter(RadioService.ACTION_STATUS)
        localBroadcastManager.registerReceiver(broadcastReceiver, filter)
    }

    private fun observePlaybackState() {
        radioViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            updatePlayPauseButton(isPlaying)
            Log.d("RadioFragment", "UI updated: isPlaying = $isPlaying")
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.buttonPlayPause.apply {
            text = if (isPlaying) "Pause" else "Play"
            icon = resources.getDrawable(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                null
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }
}