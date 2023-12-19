package app.entertainment.accentzero.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TABLE_NAME" + " (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_FILENAME TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME");
        onCreate(db);
    }

    fun addAudioRecording(filePath: String) {
        val db = this.writableDatabase
        ContentValues().apply {
            put(COLUMN_FILENAME, filePath)
            db.insert(TABLE_NAME, null, this)
        }
        db.close()
    }

    fun getAllAudioFiles(): List<String> {
        val audioFiles: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_FILENAME FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                audioFiles.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return audioFiles
    }

    fun deleteRecord(filePath: String) {
        writableDatabase?.delete(TABLE_NAME, "${COLUMN_FILENAME}=?", arrayOf(filePath))
    }

    companion object {
        private const val DATABASE_NAME = "AudioRecorder.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "recorded_audio"
        private const val COLUMN_ID = "id"
        const val COLUMN_FILENAME = "filename"
    }
}