package io.aveepsit.echomusic.fragments


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import io.aveepsit.echomusic.CurrentSongHelper
import io.aveepsit.echomusic.R
import io.aveepsit.echomusic.Songs
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    var myActivity: Activity? = null
    var mediaPlayer: MediaPlayer? = null
    var currentSongHelper: CurrentSongHelper? = null
    var currentPosition: Int = 0
    var fetchSongs: ArrayList<Songs>? = null

    //    Components from XML
    var startTimeText: TextView? = null
    var endTimeText: TextView? = null
    var songArtistView: TextView? = null
    var songTitleView: TextView? = null
    var playPauseImageButton: ImageButton? = null
    var previousImageButton: ImageButton? = null
    var nextImageButton: ImageButton? = null
    var loopImageButton: ImageButton? = null
    var shuffleImageButton: ImageButton? = null
    var seekbar: SeekBar? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        // Inflate the layout for this fragment

        startTimeText = view?.findViewById(R.id.startTime)
        endTimeText = view?.findViewById(R.id.endtTime)
        songArtistView = view?.findViewById(R.id.songArtist)
        songTitleView = view?.findViewById(R.id.songTitle)
        playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        previousImageButton = view?.findViewById(R.id.previousButton)
        nextImageButton = view?.findViewById(R.id.nextButton)
        loopImageButton = view?.findViewById(R.id.loopButton)
        shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        seekbar = view?.findViewById(R.id.seekBar)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long? = 0

        currentSongHelper = CurrentSongHelper()

        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getLong("songId") as Long
            currentPosition = arguments?.getInt("songPosition") as Int
            fetchSongs = arguments?.getParcelableArrayList("songData")

            currentSongHelper?.songId = songId
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songPath = path
            currentSongHelper?.currentPosition = currentPosition
            currentSongHelper?.isPlaying = true
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        clickHandler()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer?.setDataSource(myActivity, Uri.parse(path))
            mediaPlayer?.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer?.setOnPreparedListener({
            mediaPlayer?.start()
        })
        mediaPlayer?.setOnCompletionListener { run{
            onSongComplete()
        } }


    }

    fun clickHandler() {
        shuffleImageButton?.setOnClickListener({
            if(currentSongHelper?.isShuffle as Boolean) {
                currentSongHelper?.isShuffle = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            } else {
                currentSongHelper?.isShuffle = true
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
        })
        loopImageButton?.setOnClickListener({
            if(currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            } else {
                currentSongHelper?.isLoop = true
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                currentSongHelper?.isShuffle = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }
        })
        previousImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true

            playPrevious()
        })
        nextImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        })
        playPauseImageButton?.setOnClickListener({
            if (currentSongHelper?.isPlaying as Boolean) {
                mediaPlayer?.pause()
                currentSongHelper?.isPlaying = false
                playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
//                mediaPlayer?.start()
                currentSongHelper?.isPlaying = true
                playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        mediaPlayer?.setOnCompletionListener { {

        } }
    }

    fun playNext(check: String) {
        if (check.equals("PlayNextNormal", true)) {
            currentPosition += 1
        } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
            var randomObject = Random()
            var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
            currentPosition = randomPosition
        }
        if (currentPosition == fetchSongs?.size) {
            currentPosition = 0
        }
        playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)

        var nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songId = nextSong?.songID as Long
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songArtist = nextSong?.artist
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.currentPosition = currentPosition

        if (currentSongHelper?.isLoop as Boolean) {
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            currentSongHelper?.isLoop = false

        }
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
//            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playPrevious() {
        if (currentPosition > 0) {
            currentPosition -= 1
        }
        playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        if (currentSongHelper?.isLoop as Boolean) {
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            currentSongHelper?.isLoop = false

        }
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
//            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onSongComplete() {
        currentSongHelper?.isPlaying = true

        if (currentSongHelper?.isShuffle as Boolean) {
            playNext("PlayNextLikeNormalShuffle")
        } else if(currentSongHelper?.isLoop as Boolean) {
            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
                mediaPlayer?.prepare()
//                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            playNext("PlayNextNormal")
        }
    }


}
