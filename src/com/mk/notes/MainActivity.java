package com.mk.notes;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;


public class MainActivity extends ListActivity {
	private SimpleCursorAdapter adapter;
	private Button btnAdd;
	NotesDB dbHelper;
	SQLiteDatabase dbReader,dbWriter;
	public static final int REQUEST_CODE_ADD_NOTE = 1;
	public static final int REQUEST_CODE_EDIT_NOTE = 2;
	private OnItemLongClickListener listViewItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			new AlertDialog.Builder(MainActivity.this).setTitle("删除").setMessage("是否删除？").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Cursor cursor = adapter.getCursor();
					cursor.moveToPosition(position);
					int itemId = cursor.getInt(cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID));
					//删除NOTE表数据
					dbWriter.delete(NotesDB.TABLE_NAME_NOTES, NotesDB.COLUMN_NAME_ID + "=?", new String[]{itemId+""});
					//删除文件以及MEDIA表数据
					Cursor c = dbReader.query(NotesDB.TABLE_NAME_MEDIA, null, NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID + "=?", new String[]{itemId+""}, null, null, null);
					while (c.moveToNext()) {
						String path = c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_MEDIA_PATH));
						new File(path).delete();
					}
					dbWriter.delete(NotesDB.TABLE_NAME_MEDIA, NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID + "=?", new String[]{itemId+""});
					refreshListView();
				}
			}).show();
			return true;
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new NotesDB(this);
        dbReader = dbHelper.getReadableDatabase();
        dbWriter = dbHelper.getWritableDatabase();
        Cursor cursor = dbReader.query(NotesDB.TABLE_NAME_NOTES, null,null, null, null, null, null);
        
        adapter = new SimpleCursorAdapter(this, R.layout.activity_main_item, cursor, 
        		new String[]{NotesDB.COLUMN_NAME_NOTE_NAME,NotesDB.COLUMN_NAME_NOTE_DATE}, 
        		new int[]{R.id.tvName,R.id.tvDate});
        setListAdapter(adapter);
        
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
				startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
			}
		});
        
        getListView().setOnItemLongClickListener(listViewItemLongClickListener);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Cursor cursor = adapter.getCursor();
    	cursor.moveToPosition(position);
    	int itemId = cursor.getInt(cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID));
    	String name = cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_NAME));
    	String content = cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_CONTENT));
//    	cursor = dbReader.query(NotesDB.TABLE_NAME_NOTES, null, NotesDB.COLUMN_NAME_ID +"=?", new String[]{itemId+""}, 
//    			null, null, null);
		Intent intent = new Intent(MainActivity.this,EditNoteActivity.class);
		intent.putExtra(NotesDB.COLUMN_NAME_ID, itemId);
		intent.putExtra(NotesDB.COLUMN_NAME_NOTE_NAME, name);
		intent.putExtra(NotesDB.COLUMN_NAME_NOTE_CONTENT, content);
		startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
    		
    	
    	super.onListItemClick(l, v, position, id);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
		case REQUEST_CODE_ADD_NOTE:
		case REQUEST_CODE_EDIT_NOTE:
			if(resultCode == Activity.RESULT_OK){
				refreshListView();
			}
			break;

		default:
			break;
		}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
	
	private void refreshListView(){
		Cursor cursor = dbReader.query(NotesDB.TABLE_NAME_NOTES, null, null, null, null, null, null);
		adapter.changeCursor(cursor);
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	
}
