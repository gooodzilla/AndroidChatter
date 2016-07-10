package com.tgm.toolBox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StorageManipulater extends SQLiteOpenHelper {

	private static final String TAG = StorageManipulater.class.getSimpleName();
	
	private static final String DATABASE_NAME = "AndroidIM.db";//имя базы данных
	private static final int DATABASE_VERSION = 1;
	
	private static final String _ID = "_id";
	private static final String TABLE_NAME_MESSAGES = "androidim_messages";

	public static final String MESSAGE_RECEIVER = "receiver";
	public static final String MESSAGE_SENDER = "sender";
	public static final String MESSAGE_SENDERT = "senderT";
	private static final String MESSAGE_MESSAGE = "message";
	
	
	private static final String TABLE_MESSAGE_CREATE //добавляем таблицу
	= "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MESSAGES //имя
	+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "//индекс
	+ MESSAGE_RECEIVER + " VARCHAR(25), "//получатель
	+ MESSAGE_SENDER + " VARCHAR(25), "//отправитель
	+ MESSAGE_MESSAGE + " VARCHAR(255), "//сообщение
	+ MESSAGE_SENDERT + " datetime DEFAULT  (datetime('now', 'localtime')));";
	
	private static final String TABLE_MESSAGE_DROP = //удаление таблицы
			"DROP TABLE IF EXISTS "
			+ TABLE_NAME_MESSAGES;
	
	
	public StorageManipulater(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}


	//добавляем таблицу
	@Override
	public void onCreate(SQLiteDatabase db) {
		//db.execSQL(TABLE_MESSAGE_DROP);
		db.execSQL(TABLE_MESSAGE_CREATE);
		
	}

	//удаление таблицы
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Update from: "+ oldVersion + " to:" + newVersion + "; ");
		db.execSQL(TABLE_MESSAGE_DROP);
		onCreate(db);
		
	}
	//добавление в таблицу
	public void insert(String sender, String receiver, String message){
		long rowId = -1;
		try{
			
			SQLiteDatabase db = getWritableDatabase();//позволяет открыть бд
			ContentValues values = new ContentValues();//позволяет добавлять в бд
			values.put(MESSAGE_RECEIVER, receiver);
			values.put(MESSAGE_SENDER, sender);
			values.put(MESSAGE_MESSAGE, message);
			rowId = db.insert(TABLE_NAME_MESSAGES, null, values);
			
		} catch (SQLiteException e){
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId=" + rowId);
		}
		
	}

	//получение информации из бд
	public Cursor get(String sender, String receiver) {

			SQLiteDatabase db = getWritableDatabase();
        //db.execSQL(TABLE_MESSAGE_DROP);
             db.execSQL(TABLE_MESSAGE_CREATE);
			String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "' ORDER BY " + _ID + " ASC";
			return db.rawQuery(SELECT_QUERY,null);
			
			//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");

	}
	
	

}
