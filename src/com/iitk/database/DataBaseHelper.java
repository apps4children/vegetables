package com.iitk.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{

	private static String TAG = "DataBaseHelper";
	private static String DB_PATH = "";  
	private static String DB_NAME ="reportdb";
	private SQLiteDatabase mDataBase;  
	private final Context mContext; 
	 
	public DataBaseHelper(Context context)  
	{ 
	    super(context, DB_NAME, null, 1);
	    DB_PATH = "/data/data/" + context.getPackageName() + "/databases/"; 
	    this.mContext = context; 
	}    
	 
	public void createDataBase() throws IOException 
	{ 
	    boolean mDataBaseExist = checkDataBase(); 
	    if(!mDataBaseExist) 
	    { 
	        this.getReadableDatabase(); 
	        this.close(); 
	        try  
	        { 
	            copyDataBase(); 
	            Log.e(TAG, "createDatabase database created"); 
	        }  
	        catch (IOException mIOException)  
	        { 
	            throw new Error("ErrorCopyingDataBase"); 
	        } 
	    } 
	} 
	    //Check if the database exists 
	    private boolean checkDataBase() 
	    { 
	        File dbFile = new File(DB_PATH + DB_NAME); 
	        return dbFile.exists(); 
	    } 
	 
	    //Copy the database from assets 
	    private void copyDataBase() throws IOException 
	    { 
	        InputStream mInput = mContext.getAssets().open(DB_NAME); 
	        String outFileName = DB_PATH + DB_NAME; 
	        OutputStream mOutput = new FileOutputStream(outFileName); 
	        byte[] mBuffer = new byte[1024]; 
	        int mLength; 
	        while ((mLength = mInput.read(mBuffer))>0) 
	        { 
	            mOutput.write(mBuffer, 0, mLength); 
	        } 
	        mOutput.flush(); 
	        mOutput.close(); 
	        mInput.close(); 
	    } 
	 
	    //Open the database, so we can query it 
	    public boolean openDataBase() throws SQLException 
	    { 
	        String mPath = DB_PATH + DB_NAME; 
	        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY); 
	        return mDataBase != null; 
	    } 
	 
	    @Override 
	    public synchronized void close()  
	    { 
	        if(mDataBase != null) 
	            mDataBase.close(); 
	        super.close(); 
	    }
		@Override
		public void onCreate(SQLiteDatabase arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			// TODO Auto-generated method stub
			
		} 

}
