package io.aveepsit.echomusic.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import io.aveepsit.echomusic.R
import io.aveepsit.echomusic.Songs
import io.aveepsit.echomusic.activities.MainActivity
import io.aveepsit.echomusic.fragments.SongPlayingFragment

class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>() {

    var songDetails : ArrayList<Songs>? = null
    var mContext : Context? = null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(songDetails.isNullOrEmpty()) {
            return 0
        } else {
            return songDetails!!.count()
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artist

        holder.contentRow?.setOnClickListener({
            Toast.makeText(mContext, "Hey "+songObject?.songTitle, Toast.LENGTH_SHORT).show()
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("songTitle", songObject?.songTitle)
            args.putString("path", songObject?.songData)
            args.putLong("songId", songObject?.songID as Long)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.setArguments(args)
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .commit()
        })
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackTitle : TextView? = null
        var trackArtist : TextView? = null
        var contentRow : RelativeLayout? = null

        init {
            trackTitle = view.findViewById<TextView>(R.id.trackTitle)
            trackArtist = view.findViewById<TextView>(R.id.trackArtist)
            contentRow = view.findViewById<RelativeLayout>(R.id.contentRow)
        }
    }
}