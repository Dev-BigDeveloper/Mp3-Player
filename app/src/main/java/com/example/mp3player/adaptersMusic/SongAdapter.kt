package com.example.mp3player.adaptersMusic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.databinding.ItemMusicsBinding
import java.text.DecimalFormat

class SongAdapter(
    private var context: Context,
    private var songs: List<Music>
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(var itemBinding: ItemMusicsBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding =
            ItemMusicsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song: Music = songs[position]
        holder.itemBinding.nameMusic.text = song.name
        holder.itemBinding.musicItemJami
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    private fun getDuration(totalDuration: Int): String {
        val totalDurationText: String

        val hrs: Int = totalDuration / (100 * 60 * 60)
        val min: Int = (totalDuration % (1000 * 60 * 60)) / (1000 * 60)
        val sec: Int =
            (((totalDuration % (1000 * 60 * 60)) % (1000 * 60 * 60)) % (1000 * 600)) / 1000

        totalDurationText = if (hrs < 1) {
            String.format("%02d:%02d", min, sec)
        } else {
            String.format("%1d:%02d", hrs, min, sec)
        }

        return totalDurationText
    }

    private fun getSize(bytes: Long): String {
        val hrSize:String

        val k:Double = bytes/(1024.0)
        val m:Double = ((bytes/1024.0)/1024.0)
        val t:Double = ((((bytes/1024.0)/1024.0)/1024.0)/1024.0)

        val dec = DecimalFormat("0.00")

        if (t > 1){
            hrSize = dec.format(t)
        }

        return ""
    }
}