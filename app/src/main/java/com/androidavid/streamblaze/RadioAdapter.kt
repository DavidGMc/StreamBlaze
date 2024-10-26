package com.androidavid.streamblaze

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.androidavid.streamblaze.databinding.ItemRadioBinding

class RadioAdapter(private val onClick: (RadioStation) -> Unit) : ListAdapter<RadioStation, RadioAdapter.RadioViewHolder>(DiffCallback()) {

    class RadioViewHolder(private val binding: ItemRadioBinding, private val onClick: (RadioStation) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(station: RadioStation) {
            binding.radioName.text = station.name

            binding.imageUrl.load(station.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.library_music)
                error(R.drawable.library_music)
                transformations(CircleCropTransformation())
            }

            binding.root.setOnClickListener {
                onClick(station)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val binding = ItemRadioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RadioViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<RadioStation>() {
        override fun areItemsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
            return oldItem.streamUrl == newItem.streamUrl
        }

        override fun areContentsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
            return oldItem == newItem
        }
    }
}
