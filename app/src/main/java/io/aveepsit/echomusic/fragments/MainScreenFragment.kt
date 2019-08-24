package io.aveepsit.echomusic.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import io.aveepsit.echomusic.R
import io.aveepsit.echomusic.Songs
import io.aveepsit.echomusic.adapters.MainScreenAdapter
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
class MainScreenFragment : Fragment() {

    // Components Includes
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null

    var _mainScreenAdapter: MainScreenAdapter? = null
    var getSongsList: ArrayList<Songs>? = null
    object Statified {
        var mediaPlayer : MediaPlayer?=null
        var trackPosition: Int = 0
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        // Initialize the View Components
        visibleLayout = view?.findViewById(R.id.visibleLayout)
        noSongs = view?.findViewById(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById(R.id.songTitleMainScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.contentMain)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        when (switcher) {
            R.id.action_sort_recent -> {
                val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("action_sort_ascending", false)
                editor?.putBoolean("action_sort_recent", true)
                editor?.apply()
                if(getSongsList != null)
                    Collections.sort(getSongsList, Songs.Statified.dateComaparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
            R.id.action_sort_ascending -> {
                val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("action_sort_ascending", true)
                editor?.putBoolean("action_sort_recent", false)
                editor?.apply()
                if(getSongsList != null)
                    Collections.sort(getSongsList, Songs.Statified.nameComparatot)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val action_sort = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = action_sort?.getBoolean("action_sort_ascending", true)
        val action_sort_recent = action_sort?.getBoolean("action_sort_recent", false)
        getSongsList = getSongsFromPhone()

        if(getSongsList != null) {
            _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
//          recyclerView.adapter = io.aveepsit.echomusic.adapters.MainScreenAdapter()
            recyclerView?.layoutManager = LinearLayoutManager(myActivity)
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter

            if(action_sort_ascending!!) {
                Collections.sort(getSongsList, Songs.Statified.nameComparatot)
                _mainScreenAdapter?.notifyDataSetChanged()
            } else if(action_sort_recent!!) {
                Collections.sort(getSongsList, Songs.Statified.dateComaparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        } else {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }

        bottomBarSetup()
        bottomBarClickHandler()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                val currentId = songCursor.getLong(songId)
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentData = songCursor.getString(songData)
                val currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup() {
        try {
            songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                SongPlayingFragment.Staticated.onSongComplete()
                songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
            })
            if (SongPlayingFragment.Statified.currentSongHelper?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {
        nowPlayingBottomBar?.setOnClickListener {

            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer

            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putLong("songId", SongPlayingFragment.Statified.currentSongHelper?.songId as Long)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("MainScrnBottomBar", "success")

            songPlayingFragment.arguments = args
            fragmentManager!!.beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragmentMainBottomBar")
                .commit()
        }

        playPauseButton?.setOnClickListener {
            if (SongPlayingFragment.Statified.currentSongHelper?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                Statified.trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
                SongPlayingFragment.Statified.currentSongHelper?.isPlaying = false
            } else {
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(Statified.trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
                SongPlayingFragment.Statified.currentSongHelper?.isPlaying = true

            }
        }
    }

}
