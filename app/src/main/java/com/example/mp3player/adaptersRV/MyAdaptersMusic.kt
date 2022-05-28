package com.example.mp3player.adaptersRV

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.R
import com.example.mp3player.adaptersMusic.Music
import com.example.mp3player.databinding.ItemMusicsBinding

class MyAdaptersMusic(var list:ArrayList<Music>, var listener:OnItemClickItemListener) : RecyclerView.Adapter<MyAdaptersMusic.MyViewHolder>() {

    inner class MyViewHolder( var itemMusicsBinding: ItemMusicsBinding):RecyclerView.ViewHolder(itemMusicsBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemUsersBinding =
            ItemMusicsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemUsersBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
         val music:Music = list[position]
        holder.itemMusicsBinding.nameMusic.text = music.albums
        holder.itemMusicsBinding.artistId.text = music.artist
        val im = Uri.parse(music.imagePath)
        if (im == null){
            holder.itemMusicsBinding.imageId.setImageResource(R.drawable.music)
        }else{
            holder.itemMusicsBinding.imageId.setImageURI(Uri.parse(music.imagePath))
        }

        holder.itemMusicsBinding.musicItemJami.setOnClickListener {
            listener.onItemClickDialog(position,music)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickItemListener{
        fun onItemClickDialog(position: Int,music: Music)
   }
}