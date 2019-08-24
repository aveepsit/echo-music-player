package io.aveepsit.echomusic.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import io.aveepsit.echomusic.Database.EchoDatabase
import io.aveepsit.echomusic.R
import io.aveepsit.echomusic.Songs
import io.aveepsit.echomusic.adapters.FavoriteAdapter


/**
 * A simple [Fragment] subclass.
 *
 */
class FavoriteFragment : Fragment() {

    var myActivity: Activity?= null
    var getSongsList: ArrayList<Songs>? = null
    var favoriteContent: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
        var trackPosition: Int = 0
    }


    // Component Initialization
    var noFavorite : TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var recyclerView : RecyclerView? = null
    var songTitle : TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        noFavorite = view?.findViewById(R.id.noFavorite)
        nowPlayingBottomBar= view?.findViewById(R.id.hiddenBarFabScreen)
        playPauseButton= view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.favoriteRecycler)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)


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
        favoriteContent = EchoDatabase(myActivity)
        display_favorites_by_searching()
        bottomBarSetup()
        bottomBarClickHandler()
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
            args.putString("FavBottomBar", "success")

            songPlayingFragment.arguments = args
            fragmentManager!!.beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragmentFavBottomBar")
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

    fun display_favorites_by_searching() {
        if(favoriteContent?.checkSize() as Int > 0) {
            refreshList = ArrayList<Songs>()
            var fetListFromDevice = getSongsFromPhone()
            if(fetListFromDevice != null){
                for(i in 0..fetListFromDevice.size - 1) {
                    if(favoriteContent?.checkIfIDExists(fetListFromDevice.get(i).songID.toInt() as Int) as Boolean) {
                        refreshList?.add(fetListFromDevice.get(i))
                    }
                }
            }
            if(refreshList != null) {
                noFavorite?.visibility = View.INVISIBLE
                var favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                recyclerView?.layoutManager = LinearLayoutManager(activity)
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            } else {
                recyclerView?.visibility = View.INVISIBLE
                noFavorite?.visibility = View.VISIBLE
            }

        } else {
            recyclerView?.visibility = View.INVISIBLE
            noFavorite?.visibility = View.VISIBLE
        }
    }

}
