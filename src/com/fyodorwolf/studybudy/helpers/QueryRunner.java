package com.fyodorwolf.studybudy.helpers;

import android.database.Cursor;
import android.os.AsyncTask;

	public class QueryRunner extends AsyncTask<String,Integer,Cursor>{

		/**
	 * 
	 */
	private DatabaseAdapter _myAdapter;
	private QueryRunnerListener _myListener;
	/**
	 * @param deckActivity
	 */
	public QueryRunner(DatabaseAdapter dbAdapter) {
		this._myAdapter = dbAdapter;
		this._myListener = new QueryRunnerListener(){ 
			@Override public void onPostExcecute(Cursor cards){}
		};
	}
	
	@Override
	protected Cursor doInBackground(String... params) {
		return _myAdapter.getCursor(params[0]);
	}
	
	@Override
	protected void onPostExecute(final Cursor result) {
		_myListener.onPostExcecute(result);
	}

	
	public void setQueryRunnerListener(QueryRunnerListener listener){
		_myListener = listener;
	}
	
	public interface QueryRunnerListener{
		public void onPostExcecute(Cursor cards);
	}
	
}//E:DeckGetter