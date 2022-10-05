package com.example.mp3player.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.example.mp3player.R
import com.example.mp3player.adaptersMusic.Music
import com.example.mp3player.adaptersRV.MyAdaptersMusic
import com.example.mp3player.databinding.FragmentListMusicBinding
import com.example.mp3player.serviceMVP.LoginPresenterImp
import com.example.mp3player.serviceMVP.LoginView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class ListMusicFragment : Fragment(), LoginView {

    private lateinit var binding: FragmentListMusicBinding
    private var myAdaptersMusic: MyAdaptersMusic? = null
    private val presenter = LoginPresenterImp()
    private lateinit var  player: ExoPlayer
    private lateinit var recordAudioPermissionLauncher: ActivityResultLauncher<String>
    private var defaultStatusColor: Int = 0
    private var recordAudioPermission: String = Manifest.permission.RECORD_AUDIO
    private val TAG = "ListMusicFragment"
    private var repeatMode = 0
    private var allSong = ArrayList<Music>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentListMusicBinding.inflate(inflater, container, false)

        defaultStatusColor = requireActivity().window.statusBarColor

        requireActivity().window.navigationBarColor =
            ColorUtils.setAlphaComponent(defaultStatusColor, 199)

        recordAudioPermissionLauncher = registerForActivityResult(
            RequestPermission()
        ) { granted: Boolean ->
            if (granted && player.isPlaying) {
                activateAudioVisualiser()
            } else {
                userResponseOnRecordAudioPerm()
            }
        }

        presenter.attachView(view = this@ListMusicFragment)
        presenter.getContext(requireContext())
        presenter.getActivity(activity)
        presenter.addPermission()

        player = ExoPlayer.Builder(requireContext()).build()

        playerControls()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu,menu)
        val menuItem:MenuItem = menu.findItem(R.id.searchBtn)
        val searView:SearchView = menuItem.actionView as SearchView

        searchSong(searView)
    }

    private fun searchSong(searView: SearchView) {
        searView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText!!.toLowerCase())
                return false
            }

        })
    }



    override fun showMusics(list: List<Music>) {
        if (list.isEmpty()) {
            Toast.makeText(requireContext(), "Not songs", Toast.LENGTH_SHORT).show()
        } else {
            allSong = list as ArrayList<Music>
            myAdaptersMusic =
                MyAdaptersMusic(
                    list,
                    object : MyAdaptersMusic.OnItemClickItemListener {
                        override fun onItemClickDialog() {
                            binding.playerViewLayoutCustomer.playerView.visibility = View.VISIBLE
                            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED){
                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    player
                )



            myAdaptersMusic!!.filterSong(allSong)

            binding.rvMusic.adapter = myAdaptersMusic
        }
    }

    private fun filterSongs(query: String) : ArrayList<Music> {
        val filteredList = ArrayList<Music>()

        if (allSong.size > 0){
            for (music in allSong) {
                if (music.name.toLowerCase().contains(query)){
                    filteredList.add(music)
                }
            }
        }

        return filteredList

    }

    override fun showError(message: String) {
        TODO("Not yet implemented")
    }


    override fun onPermissionDenied() {
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




    private fun activateAudioVisualiser() {
        if (ContextCompat.checkSelfPermission(requireContext(),recordAudioPermission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), Array(1){ Manifest.permission.RECORD_AUDIO}, 0)
            return
        }

        binding.playerViewLayoutCustomer.visualizer.setColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
        binding.playerViewLayoutCustomer.visualizer.setDensity(100F)
        binding.playerViewLayoutCustomer.visualizer.setPlayer(player.audioSessionId)
    }


    private fun userResponseOnRecordAudioPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(recordAudioPermission)) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Requesting to show Audio Visualizer")

                builder.setMessage("Allow this app to display audio visualizer when music is playing")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                builder.setPositiveButton("Yes") { _, _ ->
                    recordAudioPermissionLauncher.launch(recordAudioPermission)
                }
                builder.setNegativeButton("No") { _, _ ->
                    Toast.makeText(requireContext(), "you dined", Toast.LENGTH_LONG).show()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            } else {
                Toast.makeText(requireContext(), "you dined else if", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("WrongConstant")
    private fun playerControls() {
        binding.playerViewLayoutCustomer.songNameView.isSelected = true
        binding.homeSongNameView.isSelected = true

        binding.playerViewLayoutCustomer.playerCloseBtn.setOnClickListener { exitPlayerView() }
        binding.playerViewLayoutCustomer.playListBtn.setOnClickListener { exitPlayerView() }

        binding.homeControlWrapper.setOnClickListener {
            showPlayerView()
        }

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                binding.playerViewLayoutCustomer.songNameView.text = mediaItem?.mediaMetadata?.title
                binding.homeSongNameView.text = mediaItem?.mediaMetadata?.title

                binding.playerViewLayoutCustomer.progressView.text = getReadableTime(player.currentPosition.toInt())
                binding.playerViewLayoutCustomer.seekbar.progress = player.currentPosition.toInt()
                binding.playerViewLayoutCustomer.seekbar.max = player.duration.toInt()
                binding.playerViewLayoutCustomer.duration.text = getReadableTime(player.duration.toInt())
                binding.playerViewLayoutCustomer.playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline_24,0,0,0)
                binding.homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0)

                showCurrentArtWork()

                updatePlayerPositionProgress()

                binding.playerViewLayoutCustomer.artWorkView.animation = loadRotation()

                activateAudioVisualiser()

                updatePlayerColors()

                if (!player.isPlaying){
                    player.play()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == ExoPlayer.STATE_READY){
                    binding.playerViewLayoutCustomer.songNameView.text = player.currentMediaItem?.mediaMetadata?.title
                    binding.homeSongNameView.text = player.currentMediaItem?.mediaMetadata?.title
                    binding.playerViewLayoutCustomer.progressView.text = getReadableTime(player.currentPosition.toInt())
                    binding.playerViewLayoutCustomer.duration.text = getReadableTime(player.duration.toInt())
                    binding.playerViewLayoutCustomer.seekbar.max = player.duration.toInt()
                    binding.playerViewLayoutCustomer.seekbar.progress = player.currentPosition.toInt()

                    binding.playerViewLayoutCustomer.playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline_24,0,0,0)
                    binding.homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0)

                    showCurrentArtWork()

                    updatePlayerPositionProgress()

                    binding.playerViewLayoutCustomer.artWorkView.animation = loadRotation()

                    activateAudioVisualiser()

                    updatePlayerColors()

                } else {
                    binding.playerViewLayoutCustomer.playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_outline_24,0,0,0)
                    binding.homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_arrow_24,0,0,0)
                }
            }
        })

        binding.playerViewLayoutCustomer.skipNextBtn.setOnClickListener{ skipToNextSong() }
        binding.homeSkipNextBtn.setOnClickListener{ skipToNextSong()}

        binding.playerViewLayoutCustomer.skipPreviousBtn.setOnClickListener{ skipToPreviousSong()}
        binding.homeSkipPreviousBtn.setOnClickListener{ skipToPreviousSong()}

        binding.playerViewLayoutCustomer.playPauseBtn.setOnClickListener{ playOrPauseBtn()}
        binding.homePlayPauseBtn.setOnClickListener{ playOrPauseBtn()}
        
        binding.playerViewLayoutCustomer.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var onProgressValue = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onProgressValue = seekBar!!.progress

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (player.playbackState == ExoPlayer.STATE_READY){
                    seekBar!!.progress = onProgressValue
                    binding.playerViewLayoutCustomer.progressView.text = getReadableTime(
                        onProgressValue
                    )
                    player.seekTo(onProgressValue.toLong())
                }
            }

        })

        binding.playerViewLayoutCustomer.repeatModeBtn.setOnClickListener{
            when (repeatMode) {
                1 -> {
                    player.repeatMode = ExoPlayer.STATE_READY
                    repeatMode = 2
                    binding.playerViewLayoutCustomer.repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_repeat_one_24,0,0,0)
                }
                2 -> {
                    player.shuffleModeEnabled = true
                    player.repeatMode = ExoPlayer.STATE_READY
                    repeatMode = 3
                    binding.playerViewLayoutCustomer.repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_shuffle_24,0,0,0)
                }
                3 -> {
                    player.repeatMode = ExoPlayer.STATE_READY
                    player.shuffleModeEnabled = false
                    repeatMode = 1
                    binding.playerViewLayoutCustomer.repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_repeat_24,0,0,0)
                }
            }
        }

    }

    private fun playOrPauseBtn() {
        if (player.isPlaying){
            player.pause()

            binding.playerViewLayoutCustomer.playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_outline_24,0,0,0)
            binding.homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_arrow_24,0,0,0)
            binding.playerViewLayoutCustomer.artWorkView.clearAnimation()
        }else {
            player.play()
            binding.playerViewLayoutCustomer.playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline_24,0,0,0)
            binding.homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0)
            binding.playerViewLayoutCustomer.artWorkView.startAnimation(loadRotation())
        }

        updatePlayerColors()
    }

    private fun skipToPreviousSong() {
        if (player.hasPreviousMediaItem()){
            player.seekToPrevious()
        }
    }

    private fun skipToNextSong() {
        if (player.hasNextMediaItem()){
            player.seekToNext()
        }
    }

    private fun loadRotation(): Animation {
        val rotateAnimation = RotateAnimation(
            0F,
            360F,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.duration = 10000
        rotateAnimation.repeatCount = Animation.INFINITE
        return rotateAnimation
    }

    private fun updatePlayerPositionProgress() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (player.isPlaying){
                binding.playerViewLayoutCustomer.progressView.text = getReadableTime(player.currentPosition.toInt())
                binding.playerViewLayoutCustomer.seekbar.progress = player.currentPosition.toInt()
            }

            updatePlayerPositionProgress()

        }, 1000)
    }


    private fun showCurrentArtWork() {
        binding.playerViewLayoutCustomer.artWorkView.setImageURI(player.currentMediaItem?.mediaMetadata?.artworkUri)

        if (binding.playerViewLayoutCustomer.artWorkView.drawable == null){
            binding.playerViewLayoutCustomer.artWorkView.setImageResource(R.drawable.art_image_work)
        }else{
            Log.d(TAG, "showCurrentArtWork: ${player.currentMediaItem?.mediaMetadata?.artworkUri}")
        }
    }






    private fun getReadableTime(duration: Int): String {
        val time:String
        val hrs:Int = duration/(1000*60)
        val min:Int = (duration%(1000*60*60))/(1000*60)
        val secs:Int = (((duration%(1000*60*60))%(1000*60*60))%(1000*60))/1000

        time = if (hrs<1){
            ("$min:$secs")
        }else{
            ("$hrs:$min:$secs")
        }
        return time
    }




    private fun showPlayerView() {
        binding.playerViewLayoutCustomer.playerView.visibility = View.VISIBLE
        updatePlayerColors()
    }





    private fun updatePlayerColors() {

        if (binding.playerViewLayoutCustomer.playerView.visibility == View.GONE)
            return

        var bitmapDrawable:BitmapDrawable? = binding.playerViewLayoutCustomer.artWorkView.drawable as BitmapDrawable
        if (bitmapDrawable == null){
            bitmapDrawable = ContextCompat.getDrawable(requireContext(),R.drawable.art_image_work) as BitmapDrawable
        }

        assert(bitmapDrawable != null)
        var btn:Bitmap = bitmapDrawable.bitmap

        binding.playerViewLayoutCustomer.blurImageView.setImageBitmap(btn)
        binding.playerViewLayoutCustomer.blurImageView.setBlur(4)

        Palette.from(btn).generate {pallate ->
            if (pallate != null){
                var swatch:Palette.Swatch? = pallate.darkMutedSwatch!!
                if (swatch == null){
                    swatch = pallate.darkMutedSwatch
                    if (swatch == null){
                        swatch = pallate.dominantSwatch
                    }
                }

                assert(swatch != null)
                val titleTextColor:Int = swatch!!.titleTextColor
                val bodyTexColor = swatch.bodyTextColor
                val rgbColor:Int = swatch.rgb

                requireActivity().window.statusBarColor = rgbColor
                requireActivity().window.navigationBarColor = rgbColor

                binding.playerViewLayoutCustomer.songNameView.setTextColor(titleTextColor)
                binding.playerViewLayoutCustomer.playerCloseBtn.compoundDrawables[0].setTint(titleTextColor)
                binding.playerViewLayoutCustomer.progressView.setTextColor(bodyTexColor)
                binding.playerViewLayoutCustomer.duration.setTextColor(bodyTexColor)

                binding.playerViewLayoutCustomer.repeatModeBtn.compoundDrawables[0].setTint(bodyTexColor)
                binding.playerViewLayoutCustomer.skipPreviousBtn.compoundDrawables[0].setTint(bodyTexColor)
                binding.playerViewLayoutCustomer.skipNextBtn.compoundDrawables[0].setTint(bodyTexColor)
                binding.playerViewLayoutCustomer.playPauseBtn.compoundDrawables[0].setTint(titleTextColor)
                binding.playerViewLayoutCustomer.playListBtn.compoundDrawables[0].setTint(bodyTexColor)
            }
        }
    }





    private fun exitPlayerView() {
        binding.playerViewLayoutCustomer.playerView.visibility = View.INVISIBLE
        requireActivity().window.statusBarColor = defaultStatusColor
        requireActivity().window.navigationBarColor =
            ColorUtils.setAlphaComponent(defaultStatusColor, 199)
    }





    override fun onDestroy() {
        super.onDestroy()
        if (player.isPlaying) {
            player.stop()
        }
        player.release()
    }

}