package com.example.mp3player.serviceMVP

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import com.example.mp3player.adaptersMusic.Music
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class LoginPresenterImp : LoginPresenter {

    private var viewState: WeakReference<LoginView>? = null
    private lateinit var context: Context
    private var requireActivity: FragmentActivity? = null

    fun attachView(view: LoginView) {
        viewState = WeakReference(view)
    }

    fun getContext(contextService: Context) {
        context = contextService
    }

    fun getActivity(activityService: FragmentActivity?) {
        requireActivity = activityService
    }

    override fun addPermission() {
        CoroutineScope(Dispatchers.IO).async {
            Dexter.withContext(requireActivity)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        loadMusics()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            launch(Dispatchers.Main) {
                                viewState?.get()?.onPermissionDenied()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: com.karumi.dexter.listener.PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        launch(Dispatchers.Main) {
                            AlertDialog.Builder(context)
                                .setTitle("Permission")
                                .setMessage("Iltimos musiqalaringizniyuklash uchun ruhsat bering")
                                .setNegativeButton(
                                    "Ruxsat bermaslik"
                                ) { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                    p1?.cancelPermissionRequest()
                                }
                                .setPositiveButton(
                                    "Ruxast berish"
                                ) { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                    p1?.continuePermissionRequest()
                                }
                                .show()
                        }
                    }
                }).check()

        }
    }

    @SuppressLint("Range", "Recycle")
    override fun loadMusics() {
        CoroutineScope(Dispatchers.Main).launch {
            val listMusic = ArrayList<Music>()
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val where: String = MediaStore.Audio.Media.IS_MUSIC + "!=0"

            val cursor1: Cursor? =
                requireActivity?.contentResolver?.query(uri, null, where, null, null)
            if (cursor1 != null) {
                while (cursor1.moveToNext()) {
                    val id:Long = cursor1.getLong(cursor1.getColumnIndex(MediaStore.Audio.Media._ID))
                    val url: String =
                        cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val artist: String =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val album: String =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val title: String =
                        cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val albumId: Long =
                        cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val duration: Int =
                        cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val imagePath: Uri = Uri.parse("content://media/external/audio/albumart")
                    val imagePathUri = ContentUris.withAppendedId(imagePath, albumId)

                    val uriX:Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)

                    val music = Music(
                        title,
                        album,
                        artist,
                        duration.toString(),
                        uri.toString(),
                        url,
                        imagePathUri.toString(),
                        uriX
                    )
                    listMusic.add(music)
                }
            }

            viewState?.get()?.showMusics(listMusic)
        }
    }

    override fun navigateFragment(position: Int) {

    }

    override fun playMusic() {
        
    }
}