package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class GdumpListener implements OnClickListener {

	private static final String TAG = LdumpListener.class.getName();
	//private static final int TEST_CNT = 50;
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";

	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;
	public ContentValues[] mContentValues;
	Context cv;

	public GdumpListener(TextView _tv, ContentResolver _cr) {
		mTextView = _tv;
		mContentResolver = _cr;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
		
	//	mContentValues = initTestValues();
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	
	
	
	@Override
	public void onClick(View v) {
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	
	private class Task extends AsyncTask<Void, String , Void>
	{
		@Override
		protected Void doInBackground(Void... params){
			Cursor resultCursor = mContentResolver.query(mUri, null,
					"gdump", null, null);
			
		
			resultCursor.moveToFirst();
			
			//mTextView.append("Number of keys"+resultCursor.getCount());
			while(resultCursor.isAfterLast()==false){
				String key= resultCursor.getString(0);
				String value= resultCursor.getString(1);
				resultCursor.moveToNext();
				publishProgress((key+":"+value));
			}
			
			return null;
		   }
		
		protected void onProgressUpdate(String...strings) {
			mTextView.append(strings[0]);
			mTextView.append("\n");
			mTextView.append("---------");

			mTextView.append("\n");
			return;
		}
		
	}
	
}
