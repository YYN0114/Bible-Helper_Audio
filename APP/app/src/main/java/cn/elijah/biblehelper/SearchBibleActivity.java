package cn.elijah.biblehelper;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SearchBibleActivity extends Activity {


	//重写Application，作为全部变量配置文件
	private Config _config;

	//搜索范围
	private Spinner _spinnerSearchRange;

	//搜索结果
	private ListView _listViewSearchResult;

	//搜索结果的listview适配
	private MyAdapterVolume _adapterSearch;
	
	//搜索结果的经文数组
	private String[] _strsSearchLection;
	
	//搜索结果，书卷ID
	private int[] _intsSearchResultVolumeID;
	
	//搜索结果，书卷名称
	private String[] _strsSearchResultVolumeName;
	
	//搜索结果，章ID
	private int[] _intsSearchResultChapterID;
	
	//搜索结果，节ID
	private int[] _intsSearchResultVerseID;
	
	//关键字textbox
	private EditText _editTextKeyword;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.search_bible);
	       
	       //获得自定义的应用程序Config
	       _config = (Config)getApplication(); 

	        _editTextKeyword = (EditText)findViewById(R.id.editTextKeyword);
	       //焦点事件
	       _editTextKeyword.setOnFocusChangeListener(new OnFocusChangeListener() {
	    	   @Override
				public void onFocusChange(View v, boolean hasFocus) {   

	    	   }
	       });
  
	       //下拉列表的数据
	       String[] strsSearchRange = GetSearchRange();

	       ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.simple_dropdown_item_1line,strsSearchRange);
  
	       //搜索范围
	       _spinnerSearchRange =  (Spinner)findViewById(R.id.spinnerSearchRange);
	       
	       _spinnerSearchRange.setAdapter(adapter);
	       
	       _spinnerSearchRange.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}  
		});

	       //listview实例
	       _listViewSearchResult = (ListView)findViewById(R.id.listViewSearchResult);
	       _listViewSearchResult.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				
				int iVolumeID = _intsSearchResultVolumeID[position];
				int iChapterID = _intsSearchResultChapterID[position];
				int iVerseID = _intsSearchResultVerseID[position];
				
				//跳转到书卷，章
				_config.setVolumeID(iVolumeID);
				_config.setChapterID(iChapterID);
				_config.SetStrVolumeName(_strsSearchResultVolumeName[position]);
				
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

	  }

	  //得到搜索范围字符串数组
	  private String[] GetSearchRange()
	  {
		  String[] strsResult = new String[69];
		  
		  strsResult[0] = "搜索全书";
		  strsResult[1] = "旧约";
		  strsResult[2] = "新约";

			//由卷ID得到已读的章的阅读次数数组
			DatabaseHelper dbHelper = new DatabaseHelper(SearchBibleActivity.this, _config.GetStrDatabaseNameForDeployment());

			// 得到一个SQLiteDatabase对象  
	        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

	        String sql = "select fullname from [bibleid] order by [SN]";
	        Cursor cursor = sqliteDatabase.rawQuery(sql, null);

	        int i = 3;
	        while(cursor.moveToNext())
	        {
	        	strsResult[i] = cursor.getString(cursor.getColumnIndex("FullName"));
	        	i++;
	        }


		  return strsResult;
	  }

	//点击搜索按钮
	public void onSearchButtonClicked(View v) 
	{
		if(_editTextKeyword.getText().toString().length()<=0)
		{
			Toast.makeText(getApplicationContext(), "请输入要搜索的内容", Toast.LENGTH_SHORT).show();
		}
		else
		{
			_strsSearchLection = GetSearchResult(_editTextKeyword.getText().toString(),_spinnerSearchRange.getSelectedItemPosition());
			
	       _adapterSearch = new MyAdapterVolume(SearchBibleActivity.this,_strsSearchLection);
	       
	       _listViewSearchResult.setAdapter(_adapterSearch);
		       
			_adapterSearch.notifyDataSetChanged();


			//关闭软键盘
 	       ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SearchBibleActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 	       
 	       
		}
	}
	
	//得到搜索结果
	private String[] GetSearchResult(String strKeyword,int selectedRow)
	{
		String strSQL ="";
		
		String[] strsResult = null;
		switch (selectedRow) {
		case 0://全书
			strSQL = "select FULLNAME,VOLUMESN,CHAPTERSN,VERSESN,LECTION from  BIBLE  inner join BIBLEID ON BIBLE.VOLUMESN=BIBLEID.SN where lection like   '%" + strKeyword + "%'  order by id";
			break;
		case 1://旧约
			strSQL = "select FULLNAME,VOLUMESN,CHAPTERSN,VERSESN,LECTION from  BIBLE  inner join BIBLEID ON BIBLE.VOLUMESN=BIBLEID.SN where lection like '%" + strKeyword + "%'  and neworold=0 order by id";
			break;
		case 2://新约
			strSQL = "select FULLNAME,VOLUMESN,CHAPTERSN,VERSESN,LECTION from  BIBLE  inner join BIBLEID ON BIBLE.VOLUMESN=BIBLEID.SN where lection like '%" + strKeyword + "%'  and neworold=1 order by id";
			break;
		default:
			//其他，根据书卷ID搜索
			int volumeID = selectedRow-3+1; 
			strSQL = "select FULLNAME,VOLUMESN,CHAPTERSN,VERSESN,LECTION from  BIBLE  inner join BIBLEID ON BIBLE.VOLUMESN=BIBLEID.SN where lection like '%" + strKeyword + "%'  and VOLUMESN=" + volumeID + "  order by id";
			break;
		}

	  	//由卷ID得到已读的章的阅读次数数组
		DatabaseHelper dbHelper = new DatabaseHelper(SearchBibleActivity.this, _config.GetStrDatabaseNameForDeployment());

		// 得到一个SQLiteDatabase对象  
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        Cursor cursor = sqliteDatabase.rawQuery(strSQL, null);
        
        int iCount = cursor.getCount();
		if(iCount>0)
		{
	        strsResult = new String[iCount];
	        
			_intsSearchResultVolumeID = new int[iCount];
			_intsSearchResultChapterID = new int[iCount];
			_intsSearchResultVerseID = new int[iCount];
			_strsSearchResultVolumeName = new String[iCount];
			
	        StringBuilder sbTemp = new StringBuilder();
	        
	        int i=0;
	
	        while(cursor.moveToNext())
	        {
	        	String strLection = cursor.getString(cursor.getColumnIndex("Lection"));
	        	strLection = strLection.replace(strKeyword, "</font><font color='red'>" + strKeyword + "</font><font color='black'>");
	
	        	//书卷名称
	        	_strsSearchResultVolumeName[i] = cursor.getString(cursor.getColumnIndex("FullName"));
	        	
	        	//书卷ID
	        	_intsSearchResultVolumeID[i] = cursor.getInt(cursor.getColumnIndex("VolumeSN"));
	        	
	        	//章ID
	        	int iCHAPTERSN = cursor.getInt(cursor.getColumnIndex("ChapterSN"));
	        	_intsSearchResultChapterID[i] = iCHAPTERSN;

	        	//节ID
	        	int iVERSESN = cursor.getInt(cursor.getColumnIndex("VerseSN"));
	        	_intsSearchResultVerseID[i] = iVERSESN;
	        	
	        	//清空一下stringbuilder
	        	sbTemp.delete(0, sbTemp.length());
	        	sbTemp.append("<font color='green'>");
	        	sbTemp.append(cursor.getString(cursor.getColumnIndex("FullName")));
	        	sbTemp.append("</font>&nbsp;<font color='black'>");
	        	sbTemp.append(iCHAPTERSN);
	        	sbTemp.append(":");
	        	sbTemp.append(iVERSESN);
	        	sbTemp.append("&nbsp;");
	        	sbTemp.append(strLection);
	        	sbTemp.append("</font>");
	        	
	        	strsResult[i] = sbTemp.toString();
	        	
	        	i++;
	        }
		}
		return strsResult;
	}


	public void onButtonBackClick(View v) {  
		this.finish();
	}
		
	
	 //为选择书卷listview使用的适配器
		
	private class MyAdapterVolume extends BaseAdapter 
	{        
	    //书卷名数字
	    private String _mStrsLection[] = null;


		public MyAdapterVolume(Context context, String StrsLection[]) 
		{
			mContext = context;
			_mStrsLection = StrsLection;
		}

		 @Override 
        public int getCount()
        {      
			 if(_mStrsLection == null)
			 {
				 return 0;
			 }
			 else
			 {
				 return _mStrsLection.length;				 
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
        public View getView(int position, View convertView, ViewGroup parent) 
        { 
            // position就是位置从0开始，convertView是Spinner,ListView中每一项要显示的view 
            // 通常return 的view也就是convertView 
            // parent就是父窗体了，也就是Spinner,ListView,GridView了.

            if (convertView == null) 
            {
            	convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_lection, null);
            }
            
          	if(_mStrsLection != null)
          	{
	            TextView mTextView1 = (TextView) convertView.findViewById(R.id.ItemViewBibleContent);
	            mTextView1.setText(Html.fromHtml(_mStrsLection[position]));
	            mTextView1.setTextSize(_config.GetLectionFontSize());
          	}
          	
			return convertView;

        } 
        private Context mContext;
	 }
		
		
	
}