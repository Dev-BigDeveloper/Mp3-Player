package com.example.mp3player.adaptersRV

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mp3player.R
import com.example.mp3player.adaptersMusic.Music
import com.example.mp3player.databinding.ItemMusicsBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata

class MyAdaptersMusic(
    private var list: List<Music>,
    private var listener: OnItemClickItemListener,
    private var player: ExoPlayer

) : RecyclerView.Adapter<MyAdaptersMusic.MyViewHolder>() {

    inner class MyViewHolder(var itemMusicsBinding: ItemMusicsBinding) :
        RecyclerView.ViewHolder(itemMusicsBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemUsersBinding =
            ItemMusicsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemUsersBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val music: Music = list[position]

        holder.itemMusicsBinding.nameMusic.text = music.name
        holder.itemMusicsBinding.artistId.text = music.artist

        val imageUri = Uri.parse(music.imagePath)

        if (imageUri != null) {
            Glide
                .with(holder.itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.mp)
                .into(holder.itemMusicsBinding.imageId)
        }

        holder.itemMusicsBinding.musicItemJami.setOnClickListener {
            listener.onItemClickDialog()
            if (!player.isPlaying) {
                player.setMediaItems(getMediaItems(), position, 0)
            } else {
                player.pause()
                player.seekTo(position, 0)
            }

            listener.onItemClickDialog()
            player.prepare()
            player.play()

        }


    }

    private fun getMediaItems(): List<MediaItem> {
        val mediaItems: ArrayList<MediaItem> = ArrayList()
        for (music:Music in list) {
            val mediaItem: MediaItem = MediaItem.Builder()
                .setUri(music.uri)
                .setMediaMetadata(getMetaData(music))
                .build()

            mediaItems.add(mediaItem)
        }

        return mediaItems
    }

    private fun getMetaData(music: Music): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(music.name)
            .setArtworkUri(Uri.parse(music.imagePath))
            .build()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickItemListener {
        fun onItemClickDialog()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterSong(filteredList: List<Music>) {
        list = filteredList
        notifyDataSetChanged()
    }
}