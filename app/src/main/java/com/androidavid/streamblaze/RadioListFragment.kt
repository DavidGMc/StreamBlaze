package com.androidavid.streamblaze

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidavid.streamblaze.RadioAdapter
import com.androidavid.streamblaze.RadioListFragmentDirections
import com.androidavid.streamblaze.RadioRepository
import com.androidavid.streamblaze.RadioViewModel
import com.androidavid.streamblaze.RadioViewModelFactory
import com.androidavid.streamblaze.databinding.FragmentRadioListBinding

class RadioListFragment : Fragment() {

    private lateinit var binding: FragmentRadioListBinding
    private lateinit var radioViewModel: RadioViewModel
    private lateinit var adapter: RadioAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRadioListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = RadioViewModelFactory(RadioRepository())
        radioViewModel = ViewModelProvider(this, factory).get(RadioViewModel::class.java)

        adapter = RadioAdapter { station ->
            val action = RadioListFragmentDirections.actionRadioListFragmentToRadioFragment(
                station.name ?: "Unknown Station",
                station.streamUrl ?: "",
                station.imageUrl ?: ""
            )
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        radioViewModel.radioStations.observe(viewLifecycleOwner) { stations ->
            adapter.submitList(stations)
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val filteredStations = radioViewModel.radioStations.value?.filter {
                    it.name.contains(query, ignoreCase = true) || it.streamUrl.contains(query, ignoreCase = true)
                }
                adapter.submitList(filteredStations)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}

