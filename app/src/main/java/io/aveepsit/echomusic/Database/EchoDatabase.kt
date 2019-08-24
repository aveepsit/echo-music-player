package io.aveepsit.echomusic.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.aveepsit.echomusic.Songs
import io.aveepsit.echomusic.fragments.FavoriteFragment

class EchoDatabase : SQLiteOpenHelper {

    object Straticated {
        val DB_NAME = "FavoriteDatabase"
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
        var DB_VERSION = 1
    }

    var _songsList = ArrayList<Songs>()

    override fun onCreate(sqliteDatabase: SQLiteDatabase?) {
        sqliteDatabase?.execSQL("CREATE TABLE " + Straticated.TABLE_NAME + "( " + Straticated.COLUMN_ID + " INTEGER, " + Straticated.COLUMN_SONG_ARTIST
                + " STRING, " + Straticated.COLUMN_SONG_TITLE + " STRING, " + Straticated.COLUMN_SONG_PATH + " STRING);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun storeAsFavorite (id: Int, artist: String, title: String, path: String) {
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Straticated.COLUMN_ID, id)
        contentValues.put(Straticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Straticated.COLUMN_SONG_TITLE, title)
        contentValues.put(Straticated.COLUMN_SONG_PATH, path)
        db.insert(Straticated.TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList() : ArrayList<Songs>? {
        try {
            val db = this.readableDatabase
            val queryParams = "SELECT * FROM " + Straticated.TABLE_NAME
            val csor = db.rawQuery(queryParams, null)
            if (csor.moveToFirst()) {
                do {
                    var _id = csor.getInt(csor.getColumnIndexOrThrow(Straticated.COLUMN_ID))
                    var _artist = csor.getString(csor.getColumnIndexOrThrow(Straticated.COLUMN_SONG_ARTIST))
                    var _title = csor.getString(csor.getColumnIndexOrThrow(Straticated.COLUMN_SONG_TITLE))
                    var _path = csor.getString(csor.getColumnIndexOrThrow(Straticated.COLUMN_SONG_PATH))
                    _songsList.add(Songs(_id.toLong(), _title, _artist, _path, 0))
                } while (csor.moveToNext())
                return _songsList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun checkIfIDExists( _id: Int?): Boolean {
        if (_id == null) {
            return false
        }
        val db = this.readableDatabase
        var query_params = "SELECT * FROM " + Straticated.TABLE_NAME + " WHERE " + Straticated.COLUMN_ID + " = '$_id'"
        var cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            db.close()
            return true
        } else {
            db.close()
            return false
        }
    }

    fun deleteFavorite(_id: Int) {
        val db = this.writableDatabase
        db.delete(Straticated.TABLE_NAME, Straticated.COLUMN_ID + " = " + _id, null)
        db.close()
    }

    fun checkSize() : Int{
        var db = this.readableDatabase
        var query_params = "SELECT COUNT(*) FROM " + Straticated.TABLE_NAME
        var csor = db.rawQuery(query_params, null)
        if(csor.moveToFirst()) {
            var counter = csor.getInt(0)
            return counter
        } else {
            return 0
        }
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(
        context,
        name,
        factory,
        version
    )

    constructor(context: Context?) : super(
        context,
        Straticated.DB_NAME,
        null,
        Straticated.DB_VERSION
    )
}