package io.aveepsit.echomusic.fragments


import android.app.Activity
import android.app.usage.StorageStats
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import io.aveepsit.echomusic.CurrentSongHelper
import io.aveepsit.echomusic.Database.EchoDatabase
import io.aveepsit.echomusic.R
import io.aveepsit.echomusic.Songs
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object Statified {
        val MY_PREFS_NAME = "ShakeFeature"

        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var currentSongHelper: CurrentSongHelper? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var audioVisualization: AudioVisualization? = null
        var favoriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

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
        var fab: ImageButton? = null
        var seekbar: SeekBar? = null
        var glView: GLAudioVisualizationView? = null

        var updateSongTime = object : Runnable {
            var getCurrent: Int? = 0
            override fun run() {
                getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(
                    String.format(
                        "%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long)
                        )
                    )
                )
                seekbar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }
        }
    }


    object Staticated {
        var MY_PREF_SHUFFLE = "Shuffle Feature"
        var MY_PREF_LOOP = "Loop Feature"


        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition += 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition =
                    randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition
            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)

            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songId = nextSong?.songID as Long
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )

            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                Statified.currentSongHelper?.isLoop = false

            }
            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(
                    Statified.myActivity,
                    Uri.parse(Statified.currentSongHelper?.songPath)
                )
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favoriteContent?.checkIfIDExists(Statified.currentSongHelper?.songId?.toInt()) as Boolean) {
                Statified.fab?.setImageResource(R.drawable.favorite_on)
            } else {
                Statified.fab?.setImageResource(R.drawable.favorite_off)
            }
        }

        fun playPrevious() {
            if (Statified.currentPosition > 0) {
                Statified.currentPosition -= 1
            }

            var prevSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songId = prevSong?.songID as Long
            Statified.currentSongHelper?.songTitle = prevSong.songTitle
            Statified.currentSongHelper?.songArtist = prevSong.artist
            Statified.currentSongHelper?.songPath = prevSong.songData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )

            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                Statified.currentSongHelper?.isLoop = false
            }
            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(
                    Statified.myActivity,
                    Uri.parse(Statified.currentSongHelper?.songPath)
                )
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favoriteContent?.checkIfIDExists(Statified.currentSongHelper?.songId?.toInt()) as Boolean) {
                Statified.fab?.setImageResource(R.drawable.favorite_on)
            } else {
                Statified.fab?.setImageResource(R.drawable.favorite_off)
            }
        }

        fun onSongComplete() {
            Statified.currentSongHelper?.isPlaying = true

            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else if (Statified.currentSongHelper?.isLoop as Boolean) {
                updateTextViews(
                    Statified.currentSongHelper?.songTitle as String,
                    Statified.currentSongHelper?.songArtist as String
                )

                Statified.mediaPlayer?.reset()
                try {
                    Statified.mediaPlayer?.setDataSource(
                        Statified.myActivity,
                        Uri.parse(Statified.currentSongHelper?.songPath)
                    )
                    Statified.mediaPlayer?.prepare()
                    Statified.mediaPlayer?.start()
                    processInformation(Statified.mediaPlayer as MediaPlayer)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (Statified.favoriteContent?.checkIfIDExists(Statified.currentSongHelper?.songId?.toInt()) as Boolean) {
                    Statified.fab?.setImageResource(R.drawable.favorite_on)
                } else {
                    Statified.fab?.setImageResource(R.drawable.favorite_off)
                }
            } else {
                playNext("PlayNextNormal")
            }

        }

        fun updateTextViews(songTitle: String, songArtist: String) {
            Statified.songTitleView?.setText(songTitle)
            Statified.songArtistView?.setText(songArtist)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.startTimeText?.setText(
                String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long)
                    )
                )
            )
            Statified.endTimeText?.setText(
                String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long)
                    )
                )
            )
            Statified.seekbar?.max = finalTime
            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager =
            Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endtTime)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.glView = view?.findViewById(R.id.visualizer_view)

        Statified.fab?.alpha = 0.8f

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
            }
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualization?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified?.mSensorListener)
    }

    override fun onResume() {
        Statified.audioVisualization?.onResume()
        super.onResume()
        Statified.mSensorManager?.registerListener(
            Statified.mSensorListener,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Statified.audioVisualization?.release()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Statified.favoriteContent = EchoDatabase(Statified.myActivity)

        super.onActivityCreated(savedInstanceState)
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long? = 0
        val prefsForShuffle = Statified.myActivity?.getSharedPreferences(
            Staticated.MY_PREF_SHUFFLE,
            Context.MODE_PRIVATE
        )
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false) as Boolean
        val prefsForLoop = Statified.myActivity?.getSharedPreferences(
            Staticated.MY_PREF_LOOP,
            Context.MODE_PRIVATE
        )
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false) as Boolean

        Statified.currentSongHelper = CurrentSongHelper()
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getLong("songId") as Long
            Statified.currentPosition = arguments?.getInt("songPosition") as Int
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Statified.currentSongHelper?.isPlaying = true
            Statified.currentSongHelper?.isShuffle = isShuffleAllowed
            Statified.currentSongHelper?.isLoop = isLoopAllowed

            Staticated.updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        var fromMainScrnBottomBar = arguments?.get("MainScrnBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaPlayer = FavoriteFragment.Statified.mediaPlayer
        } else if (fromMainScrnBottomBar != null) {
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer
        } else {
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.setOnPreparedListener({
                Statified.mediaPlayer?.start()
                Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
            })
        }


        if (isShuffleAllowed) {
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
        } else {
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        if (isLoopAllowed) {
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)

        }

        clickHandler()


        Statified.mediaPlayer?.setOnCompletionListener {
            run {
                Staticated.onSongComplete()
            }
        }

        val vizualizerHandler =
            DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(vizualizerHandler)

        if (Statified.favoriteContent?.checkIfIDExists(Statified.currentSongHelper?.songId?.toInt()) as Boolean) {
            Statified.fab?.setImageResource(R.drawable.favorite_on)
        } else {
            Statified.fab?.setImageResource(R.drawable.favorite_off)
        }

    }

    fun clickHandler() {
        Statified.fab?.setOnClickListener({
            if (Statified.favoriteContent?.checkIfIDExists(Statified.currentSongHelper?.songId?.toInt()) as Boolean) {
                Statified.favoriteContent?.deleteFavorite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed from Favorite!", Toast.LENGTH_SHORT)
                    .show()
                Statified.fab?.setImageResource(R.drawable.favorite_off)
            } else {
                Statified.fab?.setImageResource(R.drawable.favorite_on)
                Statified.favoriteContent?.storeAsFavorite(
                    Statified.currentSongHelper?.songId?.toInt() as Int,
                    Statified.currentSongHelper?.songArtist as String,
                    Statified.currentSongHelper?.songTitle as String,
                    Statified.currentSongHelper?.songPath as String
                )
                Toast.makeText(Statified.myActivity, "Added to Favorite!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_LOOP,
                Context.MODE_PRIVATE
            )?.edit()
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.currentSongHelper?.isShuffle = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_LOOP,
                Context.MODE_PRIVATE
            )?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.currentSongHelper?.isShuffle = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true

            Staticated.playPrevious()
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }
        })
        Statified.playPauseImageButton?.setOnClickListener({
            if (Statified.currentSongHelper?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaPlayer?.start()
                Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun bindShakeListener() {
        Statified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]
                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = mAccelerationLast - mAccelerationCurrent
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12) {
                    val prefs = Statified.myActivity?.getSharedPreferences(
                        Statified.MY_PREFS_NAME,
                        Context.MODE_PRIVATE
                    )
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")

                    } else {

                    }
                }
            }

        }
    }
}
