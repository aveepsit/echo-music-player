package io.aveepsit.echomusic.activities

import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import io.aveepsit.echomusic.R
import io.aveepsit.echomusic.adapters.NavigationDrawerAdapter
import io.aveepsit.echomusic.fragments.MainScreenFragment

class MainActivity : AppCompatActivity() {
    var navigationDrawerIconsList: ArrayList<String> = arrayListOf("All Songs", "Favorites",
        "Settings", "About Us")
    var images_for_navdrawer = intArrayOf(
        R.drawable.navigation_allsongs,
        R.drawable.navigation_favorites,
        R.drawable.navigation_settings,
        R.drawable.navigation_aboutus)
    object Stratified {
        var drawerLayout: DrawerLayout? = null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        MainActivity.Stratified.drawerLayout = findViewById(R.id.drawer_layout)
        var toggle = ActionBarDrawerToggle(
            this@MainActivity, MainActivity.Stratified.drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        MainActivity.Stratified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
            .commit()

        var _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconsList,
            images_for_navdrawer, this)
        _navigationAdapter.notifyDataSetChanged()

        val navigation_recycler_view = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()
        navigation_recycler_view.adapter = _navigationAdapter
        navigation_recycler_view.setHasFixedSize(true)
    }

    override fun onStart() {
        super.onStart()
    }
}
