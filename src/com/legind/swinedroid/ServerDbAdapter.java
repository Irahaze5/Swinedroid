package com.legind.swinedroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class ServerDbAdapter {
	
	public static final String KEY_HOST = "host";
	public static final String KEY_PORT = "port";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_ROWID = "_id";
	
	private static final String TAG = "ServerDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
	        "create table servers (_id integer primary key autoincrement, "
	                + "host varchar(128) not null, port int not null, username varchar(128) not null, password varchar(128) not null);";
	
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "servers";
	private static final int DATABASE_VERSION = 3;
	
	private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS servers");
			onCreate(db);
		}
	}
	
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public ServerDbAdapter(Context ctx) {
	    this.mCtx = ctx;
	}
	
	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public ServerDbAdapter open() throws SQLException {
	    mDbHelper = new DatabaseHelper(mCtx);
	    mDb = mDbHelper.getWritableDatabase();
	    return this;
	}
	
	public void close() {
	    mDbHelper.close();
	}
	
	
	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title the title of the note
	 * @param body the body of the note
	 * @return rowId or -1 if failed
	 */
	public long createServer(String host, int port, String username, String password) {
	    ContentValues initialValues = new ContentValues();
	    if(host.length() > 128)
	    	host = host.substring(0,127);
	    if(port > 65535 || port < 1)
	    	port = 65535;
	    if(username.length() > 128)
	    	username = username.substring(0,127);
	    if(password.length() > 128)
	    	password = password.substring(0,127);
	    initialValues.put(KEY_HOST, host);
	    initialValues.put(KEY_PORT, port);
	    initialValues.put(KEY_USERNAME, username);
	    initialValues.put(KEY_PASSWORD, password);
	    return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteServer(long rowId) {
	
	    return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllServers() {
	
	    return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_HOST,
	            KEY_PORT, KEY_USERNAME, KEY_PASSWORD}, null, null, null, null, null);
	}
	
	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException if note could not be found/retrieved
	 */
	public Cursor fetchServer(long rowId) throws SQLException {
	
	    Cursor mCursor =
	
	            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
	                    KEY_HOST, KEY_PORT, KEY_USERNAME, KEY_PASSWORD}, KEY_ROWID + "=" + rowId, null,
	                    null, null, null, null);
	    if (mCursor != null) {
	        mCursor.moveToFirst();
	    }
	    return mCursor;
	
	}
	
	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId id of note to update
	 * @param title value to set note title to
	 * @param body value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateServer(long rowId, String host, int port, String username, String password) {
	    ContentValues args = new ContentValues();
	    if(host.length() > 128)
	    	host = host.substring(0,127);
	    if(port > 65535 || port < 1)
	    	port = 65535;
	    if(username.length() > 128)
	    	username = username.substring(0,127);
	    if(password.length() > 128)
	    	password = password.substring(0,127);
	    args.put(KEY_HOST, host);
	    args.put(KEY_PORT, port);
	    args.put(KEY_USERNAME, username);
	    args.put(KEY_PASSWORD, password);
	
	    return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}