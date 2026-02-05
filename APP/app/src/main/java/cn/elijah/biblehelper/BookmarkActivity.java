package cn.elijah.biblehelper;



import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BookmarkActivity extends Activity {

	//重写Application，作为全部变量配置文件
	private Config _config;
	
	//书签列表
	private ListView _listViewBookmark;
	
	//搜索结果，书卷ID
	private int[] _intsBookmarkVolumeID;
	
	//搜索结果，书卷名称
	private String[] _strsBookmarkVolumeName;
	
	//搜索结果，章ID
	private int[] _intsBookmarkChapterID;
	
	//搜索结果，节ID
	private int[] _intsBookmarkVerseID;
	
	//书签的数据库中的ID，用于删除
	private int[] _intsBookmarkID_ForDelete;
	

	
	//搜索结果的listview适配
	private MyAdapterVolume _adapterSearch;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmark_manage);

		//获得自定义的应用程序Config       
		_config = (Config)getApplication(); 


		//listview实例
		_listViewBookmark = (ListView)findViewById(R.id.listViewBookmark);
		_listViewBookmark.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				

				
				int iVolumeID = _intsBookmarkVolumeID[position];
				int iChapterID = _intsBookmarkChapterID[position];
				int iVerseID = _intsBookmarkVerseID[position];
				String strVolumeName = _strsBookmarkVolumeName[position];

				//跳转到书卷，章，节
				_config.setVolumeID(iVolumeID);
				_config.setChapterID(iChapterID);
				_config.setVerseID(iVerseID);
				_config.SetStrVolumeName(strVolumeName);


				//填充经文的listview
	     		_config.GetInstanceMain_Tab_Readbible().FillListView();
	
	     		//如果正在播放，则尝试播放新章节
	     		if(_config.GetVoicePanelActivity().mediaPlayer != null && 
	     				_config.GetVoicePanelActivity().mediaPlayer.isPlaying())
	     		{
	     			_config.GetVoicePanelActivity().AutoPlay();
	     		}
	     		else
	     		{
	     			//刷新播放器和界面的状态
					if(_config.GetVoicePanelActivity() != null)
					{
						if(_config.GetVoicePanelActivity().mediaPlayer != null && 
								_config.GetVoicePanelActivity().mediaPlayer.isPlaying() == true)
						{
							_config.GetVoicePanelActivity().mediaPlayer.stop();
						}

						if(_config.GetVoicePanelActivity().seekBar != null)
						{
							_config.GetVoicePanelActivity().seekBar.setProgress(0);
						}

						_config.GetVoicePanelActivity().UpdatePlaybuttonIconStatus();
					}
	     		}

	     		//关闭选择章的窗体
	     		if(_config.GetMainActivity() != null)
	     		{
	     			_config.GetMainActivity().ChangePage(0);
	     		}

	 	       _config.GetInstanceMain_Tab_Readbible().SelectItemIndex(iVerseID - 1);

	 	       //关闭窗体
	 	       finish();	
			}	
		});

		FillListView();

		//保存界面实例
		_config.SetBookmarkActivity(this);
	}

	public void FillListView()
	{
		String strSQL ="select [id], [name], [volumesn], [chaptersn], [versesn]  from  [BibleLabels] order by [id] desc ";

		//查询书签
		DatabaseHelper dbHelper = new DatabaseHelper(BookmarkActivity.this, _config.GetStrDatabaseNameForDeployment());

		// 得到一个SQLiteDatabase对象 
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        Cursor cursor = sqliteDatabase.rawQuery(strSQL, null);

        int iCount = cursor.getCount();
		if(iCount>0)
		{

			_intsBookmarkVolumeID = new int[iCount];
			_intsBookmarkChapterID = new int[iCount];
			_intsBookmarkVerseID = new int[iCount];
			_strsBookmarkVolumeName = new String[iCount];
			_intsBookmarkID_ForDelete = new int[iCount];
			
			
	        int i=0;

	        while(cursor.moveToNext())
	        {
	        	//书卷名称
	        	_strsBookmarkVolumeName[i] = cursor.getString(cursor.getColumnIndex("name"));
	        	
	        	//书卷ID
	        	_intsBookmarkVolumeID[i] = cursor.getInt(cursor.getColumnIndex("volumesn"));
	        	
	        	//章ID
	        	_intsBookmarkChapterID[i] = cursor.getInt(cursor.getColumnIndex("chaptersn"));

	        	//节ID
	        	_intsBookmarkVerseID[i] = cursor.getInt(cursor.getColumnIndex("versesn"));

	        	//书签ID，用于删除
	        	_intsBookmarkID_ForDelete[i] = cursor.getInt(cursor.getColumnIndex("id"));
	        	
	        	i++;
	        }
	        
			
		}
		else
		{
			_intsBookmarkVolumeID = null;
			_intsBookmarkChapterID = null;
			_intsBookmarkVerseID = null;
			_strsBookmarkVolumeName = null;
			_intsBookmarkID_ForDelete = null;
		}


		_adapterSearch = new MyAdapterVolume(BookmarkActivity.this);
		_listViewBookmark.setAdapter(_adapterSearch);
		_adapterSearch.notifyDataSetChanged();
	}

	public void onButtonBackClick(View v) {  
		this.finish();
	}
	
	//为选择书卷listview使用的适配器
	private class MyAdapterVolume extends BaseAdapter 
	{
		public MyAdapterVolume(Context context) 
		{
			mContext = context;
		}

		 @Override 
        public int getCount()
        {      
			 if(_strsBookmarkVolumeName == null)
			 {
				 return 0;
			 }
			 else
			 {
				 return _strsBookmarkVolumeName.length;				 
			 }
        }

        @Override 
        public Object getItem(int arg0) { 
            return arg0; 
        }

        @Override
        public long getItemId(int position) {
            return position; 
        }

        @Override 
        public View getView(final int position, View convertView, ViewGroup parent) 
        { 
            Button buttonDelete = null;

            if (convertView == null) 
            {
            	convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_bookmark, null);
            }

            TextView mTextView1 = (TextView) convertView.findViewById(R.id.ItemViewBookmarkText);

            if(_strsBookmarkVolumeName[position] != null)
            {
	            String strBookmars = _strsBookmarkVolumeName[position] + " " + _intsBookmarkChapterID[position] + ":" + _intsBookmarkVerseID[position];
	            mTextView1.setText(strBookmars);
	            mTextView1.setTextColor(Color.BLACK);
	            mTextView1.setTextSize(20);
          
	            buttonDelete=(Button)convertView.findViewById(R.id.buttonDelete); 
	            //点击事件
	            buttonDelete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						
	
						String strSQL ="delete from  [BibleLabels] where [id]=" + _intsBookmarkID_ForDelete[position] ;
	
					  	//由卷ID得到已读的章的阅读次数数组
						DatabaseHelper dbHelper = new DatabaseHelper(BookmarkActivity.this, _config.GetStrDatabaseNameForDeployment());
				        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  
				        sqliteDatabase.execSQL(strSQL);
	
				        Toast.makeText(BookmarkActivity.this,"删除书签成功", Toast.LENGTH_LONG).show();
				        
				        //刷新界面
				        FillListView();
	
					}
				});
	            
            }
            

			return convertView;
        } 
        private Context mContext;
	 }


}