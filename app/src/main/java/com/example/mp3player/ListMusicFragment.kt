package com.example.mp3player

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mp3player.adaptersMusic.Music
import com.example.mp3player.adaptersRV.MyAdaptersMusic
import com.example.mp3player.databinding.FragmentListMusicBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListMusicFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentListMusicBinding
    private lateinit var listMusic: ArrayList<Music>
    private lateinit var myAdaptersMusic: MyAdaptersMusic
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListMusicBinding.inflate(inflater, container, false)
        Dexter.withActivity(requireActivity())
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getAllAudio()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts(
                            "package",
                            requireContext().packageName,
                            null
                        )
                        intent.data = uri

                        startActivity(intent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission")
                        .setMessage("Iltimos musiqalaringizniyuklash uchun ruhsat bering")
                        .setNegativeButton(
                            "Ruxsat bermaslik",
                            DialogInterface.OnClickListener { dialogInterface, _ ->
                                dialogInterface.dismiss()
                                p1?.cancelPermissionRequest()
                            })
                        .setPositiveButton(
                            "Ruxast berish",
                            DialogInterface.OnClickListener { dialogInterface, _ ->
                                dialogInterface.dismiss()
                                p1?.continuePermissionRequest()
                            })
                        .show()
                }
            }).check()
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListMusicFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun getAllAudio() {
        val listMusic = ArrayList<Music>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val where: String = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val cursor1: Cursor? =
            activity?.contentResolver?.query(uri, null, where, null, null)
        if (cursor1 != null) {
            while (cursor1.moveToNext()) {
                val artist: String =
                    cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album: String =
                    cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val title: String =
                    cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val songUri: String =
                    cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumId: Long =
                    cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val duration: Int =
                    cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val imagePath: Uri = Uri.parse("content://media/external/audio/albumart")
                val imagePathUri = ContentUris.withAppendedId(imagePath, albumId)

                val music = Music(
                    title,
                    album,
                    artist,
                    duration.toString(),
                    uri.toString(),
                    albumId.toString(),
                    imagePathUri.toString()
                )
                listMusic.add(music)
                myAdaptersMusic =
                    MyAdaptersMusic(listMusic, object : MyAdaptersMusic.OnItemClickItemListener {
                        override fun onItemClickDialog(position: Int, music: Music) {
                            val bundle = Bundle()
                            bundle.putSerializable("song", music)
                            bundle.putInt("position", position)
                            bundle.putInt("size", listMusic.size)
                            findNavController().navigate(R.id.homeFragment)
                        }
                    })
                binding.rvMusic.adapter = myAdaptersMusic
            }
        }
    }
}