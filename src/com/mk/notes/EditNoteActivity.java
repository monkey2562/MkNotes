package com.mk.notes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EditNoteActivity extends ListActivity implements OnClickListener{
	Button btnSave,btnCancel,btnTakePicture,btnTakeVideo;
	EditText etName,etContent;
	NotesDB notesDB;
	SQLiteDatabase dbWrite,dbRead;
	private int noteId;
	private String currentPath;
	
	private static final int REQUEST_CODE_TAKE_PICTURE = 1;
	private static final int REQUEST_CODE_TAKE_VIDEO = 2;
	private MediaAdapter adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_note);
		notesDB = new NotesDB(this);
		dbWrite = notesDB.getWritableDatabase();
		dbRead = notesDB.getReadableDatabase();
		
		etName = (EditText) findViewById(R.id.etName);
		etContent = (EditText) findViewById(R.id.etContent);
		
		//读取媒体资料列表
		adapter = new MediaAdapter(this);
		setListAdapter(adapter);
		
		noteId = getIntent().getIntExtra(NotesDB.COLUMN_NAME_ID, -1);
		if(noteId > -1){//大于-1 修改操作
			etName.setText(getIntent().getStringExtra(NotesDB.COLUMN_NAME_NOTE_NAME));
			etContent.setText(getIntent().getStringExtra(NotesDB.COLUMN_NAME_NOTE_CONTENT));
			
			Cursor c = dbRead.query(NotesDB.TABLE_NAME_MEDIA, null, NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID + "=?",
					new String[]{noteId+""}, null, null, null);
			while (c.moveToNext()) {
				MediaListCellData data = new MediaListCellData(c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_MEDIA_PATH)),
						c.getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID)));
				adapter.add(data);
			}
			adapter.notifyDataSetChanged();
		}
		
		
		btnSave = (Button) findViewById(R.id.btnSave);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
		btnTakeVideo = (Button) findViewById(R.id.btnTakeVideo);
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnTakePicture.setOnClickListener(this);
		btnTakeVideo.setOnClickListener(this);
		
		
		
		
	}

	@Override
	protected void onDestroy() {
		dbRead.close();
		dbWrite.close();
		super.onDestroy();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_note, menu);
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		MediaListCellData data = adapter.getItem(position);
		Intent intent ;
		switch (data.type) {
		case MediaType.PHOTO:
			intent = new Intent(this,PhotoViewerActivity.class);
			intent.putExtra(PhotoViewerActivity.EXTRA_PATH, data.path);
			startActivity(intent);
			break;
		case MediaType.VIDEO:
			intent = new Intent(this,VideoViewerActivity.class);
			intent.putExtra(VideoViewerActivity.EXTRA_PATH, data.path);
			startActivity(intent);
			break;

		default:
			break;
		}
		
		
		
		
		super.onListItemClick(l, v, position, id);
		
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		File file;
		
		switch (v.getId()) {
		case R.id.btnSave:
			saveMedia(saveNote());
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.btnCancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.btnTakePicture:
			//调用系统相机
			file = new File(getMediaDir(),System.currentTimeMillis()+".jpg");
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			currentPath = file.getAbsolutePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			break;
		case R.id.btnTakeVideo:
			//调用系统相机
			file = new File(getMediaDir(),System.currentTimeMillis()+".mp4");
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			currentPath = file.getAbsolutePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
			break;
		}
	}
	
	public File getMediaDir(){
		File dir = new File(Environment.getExternalStorageDirectory(),"pictures");
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir ;
	}
	
	public void saveMedia(int noteId){
		MediaListCellData data;
		ContentValues values;
		for (int i = 0; i < adapter.getCount(); i++) {
			data = adapter.getItem(i);
			if(data.id <=-1){
				values = new ContentValues();
				values.put(NotesDB.COLUMN_NAME_MEDIA_PATH, data.path);
				values.put(NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID, noteId);
				dbWrite.insert(NotesDB.TABLE_NAME_MEDIA, null, values);
			}
		}
		
	}
	
	public int saveNote(){
		ContentValues values = new ContentValues();
		values.put(NotesDB.COLUMN_NAME_NOTE_NAME, etName.getText().toString());
		values.put(NotesDB.COLUMN_NAME_NOTE_CONTENT, etContent.getText().toString());
		values.put(NotesDB.COLUMN_NAME_NOTE_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		if(noteId > -1){
			dbWrite.update(NotesDB.TABLE_NAME_NOTES, values, NotesDB.COLUMN_NAME_ID + "=?", new String[]{noteId+""});
			return noteId;
		} else {
			return (int)dbWrite.insert(NotesDB.TABLE_NAME_NOTES, null, values);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_TAKE_PICTURE:
		case REQUEST_CODE_TAKE_VIDEO:
			if(resultCode == RESULT_OK){
				adapter.add(new MediaListCellData(currentPath));
				adapter.notifyDataSetChanged();
			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	static class MediaAdapter extends BaseAdapter{
		private Context context;
		private List<MediaListCellData> list = new ArrayList<EditNoteActivity.MediaListCellData>();
		public MediaAdapter(Context context){
			this.context = context;
		}
		
		public void add(MediaListCellData data){
			list.add(data);
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public MediaListCellData getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.activity_media_list_cell, null);
			}
			
			MediaListCellData data = getItem(position);
			ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
			TextView tvPath = (TextView) convertView.findViewById(R.id.tvPath);
			ivIcon.setImageResource(data.iconId);
			tvPath.setText(data.path);
			return convertView;
		}
		
	}
	
	static class MediaListCellData{
		int type = 0;
		int id = -1;
		String path = "";
		int iconId = R.drawable.ic_launcher;
		
		public MediaListCellData(String path){
			this.path = path;
			if(path.endsWith(".jpg")){
				this.iconId = R.drawable.dly;
				this.type = MediaType.PHOTO;
			}else if(path.endsWith(".mp4")){
				this.iconId = R.drawable.dz;
				this.type = MediaType.VIDEO;
			}
		}
		
		public MediaListCellData(String path, int id){
			this(path);
			this.id = id;
		}
	}
	
	static class MediaType{
		static final int PHOTO = 1;
		static final int VIDEO = 2;
	}
}
