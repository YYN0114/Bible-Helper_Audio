package cn.elijah.biblehelper;


import java.util.Arrays;
import cn.elijah.biblehelper.R;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class Main_Tab_Readbible extends ActivityGroup {


	private Button mButtonCenter;
	
	//重写Application，作为全部变量配置文件
	private Config _config;

	@Override
	   public void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.main_tab_readbible);

	       //获得自定义的应用程序Config
	       _config = (Config)getApplication(); 
	       
	       
	       //保存当前实体
	       _config.SetInstanceMain_Tab_Readbible(this);
	       
	       
			//得到语音面板
			_linearLayoutVoicePanel = (LinearLayout)findViewById(R.id.ll_voice_panel);
			

           //中间 经文 章的标题按钮
           mButtonCenter = (Button)findViewById(R.id.button_center);
	       
	       //显示经文的listview
	       ListView listViewBibleContent = (ListView)findViewById(R.id.listViewBibleContent);

	       
		   View loadMoreView = getLayoutInflater().inflate(R.layout.layout_next_chapter, null); 
		  	final Button loadMoreNextButton = (Button) loadMoreView.findViewById(R.id.ButtonNextChapter); 
		       loadMoreNextButton.setOnClickListener(new OnClickListener() {
		           @Override
		           public void onClick(View v) {

		        	   //跳转到下一章
		        	   //得到当前的按钮状态，如果按钮为可见，说明可以有下一章	
		        	   //如果有下一章
		        	   if(_config.GetIsHasNextChapter())
		        	   {
		        		   //章ID增长1
		        		   _config.setChapterID(_config.getChapterID() + 1);
		        		   
		        		   FillListView();
		        		   
							//如果面板打开状态，则尝试播放新章节
							if(_linearLayoutVoicePanel.getVisibility()==0)
							{								
								if(_config.GetVoicePanelActivity().seekBar != null)
								{
									_config.GetVoicePanelActivity().seekBar.setProgress(0);
									_config.GetVoicePanelActivity().seekBar.setMax(0);
								}
								
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
										_config.GetVoicePanelActivity().seekBar.setMax(0);
									}

									_config.GetVoicePanelActivity().UpdatePlaybuttonIconStatus();
								}
							}
		        	   }
		        	   
		       }
		   });

		       
		       final Button loadMorePrevButton = (Button) loadMoreView.findViewById(R.id.ButtonPrevChapter); 
		       loadMorePrevButton.setOnClickListener(new OnClickListener() {
		           @Override
		           public void onClick(View v) {

		        	   //跳转到上一章
		        	   //得到当前的按钮状态，如果按钮为可见，说明可以有下一章		        	   

		        	   //如果有下一章
		        	   if(_config.GetIsHasPrevChapter())
		        	   {
		        		   //章ID增长1
		        		   _config.setChapterID(_config.getChapterID() - 1);
		        		   
		        		   FillListView();
		        		   
							//如果面板打开状态，则尝试播放新章节
							if(_linearLayoutVoicePanel.getVisibility()==0)
							{
								
								if(_config.GetVoicePanelActivity().seekBar != null)
								{
									_config.GetVoicePanelActivity().seekBar.setProgress(0);
									_config.GetVoicePanelActivity().seekBar.setMax(0);
								}
								
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
										_config.GetVoicePanelActivity().seekBar.setMax(0);
									}

									_config.GetVoicePanelActivity().UpdatePlaybuttonIconStatus();
								}
							}
		        	   }

		       }
		   });

	       //先设置footerview，在setadapter
	       listViewBibleContent.addFooterView(loadMoreView);

		       
	       //将显示经文的listview存储为全局变量，等待选择经文后刷新
	       _config.SetListViewBibleContent(listViewBibleContent);

	       if(_config.GetListViewBibleContent() != null)
	    	{
	    	   //初始化，创世纪
	    	   FillListView();

		       _config.GetListViewBibleContent().setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) 
				{
					//弹出菜单
					showLongClickMenu(position);
					return true;
				}
	           });

	           //默认选中第一行，以保存当前状态。。。这个。。
		       SelectItemIndex(0);

	    	}
	       

	       //设置语音控制面板，包含播放按钮，播放进度条，下载进度条
	       LinearLayout linearLayoutVoicePanel = (LinearLayout)findViewById(R.id.ll_voice_panel);
	       
	       View control1 = getLocalActivityManager().startActivity(
	               "VoicePanelActivity",
	               new Intent(Main_Tab_Readbible.this, VoicePanelActivity.class))
	               .getDecorView();
	       
	       
	       linearLayoutVoicePanel.addView(control1);

	 }



	//搜索 按钮
	public void btnmainright(View v) {

		Intent _intentSearch = new Intent (Main_Tab_Readbible.this,SearchBibleActivity.class);
		startActivity(_intentSearch);
     }  	
    
	//设置标题栏中间文字
	public void onButtonCenterClicked(View v) {
		Intent intent = new Intent (Main_Tab_Readbible.this,SelectVolumeAndChapter.class);			
		startActivity(intent);
     }  


	public LinearLayout _linearLayoutVoicePanel = null;
	
	//语音按钮
	public void onButtonVoiceClicked(View v)
	{
		//显示或隐藏
	    if(_linearLayoutVoicePanel.getVisibility() == 0)
        {
	    	//隐藏
	    	_linearLayoutVoicePanel.setVisibility(View.GONE);
	    	//获取播放界面实例
	    	if(_config.GetVoicePanelActivity() != null )
	    	{
	    		//停止播放
	    		if(_config.GetVoicePanelActivity().mediaPlayer != null && 
	    				_config.GetVoicePanelActivity().mediaPlayer.isPlaying())
	    		{
	    			_config.GetVoicePanelActivity().mediaPlayer.pause();
	    		}

		    	//更新播放按钮图标状态
		    	_config.GetVoicePanelActivity().UpdatePlaybuttonIconStatus();
	    	}
        }
	    else
	    {
	    	//显示
	    	_linearLayoutVoicePanel.setVisibility(View.VISIBLE);
	    	
	    	//获取播放界面实例
	    	if(_config.GetVoicePanelActivity() != null )
	    	{
				if(_config.GetVoicePanelActivity().seekBar.getProgress() > 0)
				{
					_config.GetVoicePanelActivity().mediaPlayer.start();
				}
				else
				{
		    		//尝试播放
			    	_config.GetVoicePanelActivity().AutoPlay();
				}
				
				//更新播放按钮图标状态
				_config.GetVoicePanelActivity().UpdatePlaybuttonIconStatus();
	    	}
	    }
	    
	    
	    
	}
	
	//标记历史记录，设置为已读+1
	public void MarkHistory(int VolumeID, int ChapterID)
	{
		DatabaseHelper dbHelper = new DatabaseHelper(Main_Tab_Readbible.this, _config.GetStrDatabaseNameForDeployment());

		// 得到一个SQLiteDatabase对象  
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        String sql = "SELECT [count] as ReadCount FROM biblestatistics where VOLUMESN=" + VolumeID +" and CHAPTERSN=" + ChapterID;
        Cursor cursor = sqliteDatabase.rawQuery(sql, null);

        //有记录
        if (cursor.moveToNext())
        {
        	int iReadCount = cursor.getInt(cursor.getColumnIndex("ReadCount"));
        	//增加一次
        	iReadCount++;
        	
        	String strSQL = "update biblestatistics set [count]=" + iReadCount + " where VOLUMESN=" + VolumeID +" and CHAPTERSN=" + ChapterID;
        	sqliteDatabase.execSQL(strSQL);
        }
        else
        {
        	//无记录
        	String strSQL = "insert into biblestatistics (VOLUMESN,CHAPTERSN,[COUNT]) values (" +VolumeID+"," + ChapterID + ",1)";
        	sqliteDatabase.execSQL(strSQL);
        }
        
        
        //保存最近阅读的书卷和章节
        _config.WriteRecentlyReadingToDatabase();
        
	}
	
    /**
    * 由卷ID和章ID得到经文数组
    */
    public int GetLectionByVolumeIDandChapterID(int VolumeID, int ChapterID,String[] outStrsResult)
    {
    	//返回数据个数
    	int iResult = 0;
    	//清空数组
    	Arrays.fill(outStrsResult, "");
    	
        //创建DatabaseHelper对象  
        DatabaseHelper dbHelper = new DatabaseHelper(Main_Tab_Readbible.this, _config.GetStrDatabaseNameForDeployment());  
        // 得到一个SQLiteDatabase对象  
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        String strSQL = "select [ID], [VolumeSN], [ChapterSN], [Lection] from [Bible]  where  VolumeSN="+ VolumeID +"  and ChapterSN="+ ChapterID +" order by [ID] asc"; 
        Cursor cursor = sqliteDatabase.rawQuery(strSQL,null);
        
        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
        while (cursor.moveToNext())
        {
        	outStrsResult[iResult] = cursor.getString(cursor.getColumnIndex("Lection"));  
            iResult++;
        }


        //判断是否加“下一章”按钮
        int iChapterCount = 0;
        
        // 
        strSQL = "select [SN], [ChapterNumber] from [BibleID] where [SN]=" +VolumeID +"  order by [SN] asc"; 
        
        cursor = sqliteDatabase.rawQuery(strSQL, null);
        
        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
        if (cursor.moveToNext())
        {
        	iChapterCount = cursor.getInt(cursor.getColumnIndex("ChapterNumber"));  
        }
        
       	Button buttonNextChapter = null;
    	buttonNextChapter = (Button)_config.GetListViewBibleContent().findViewById(R.id.ButtonNextChapter);
       
    	if(buttonNextChapter != null)
    	{
    	    if(iChapterCount == ChapterID)
            {
        		buttonNextChapter.setVisibility(View.GONE);
        		_config.SetIsHasNextChapter(false);
            }
    	    else
    	    {
        		buttonNextChapter.setVisibility(View.VISIBLE);
        		_config.SetIsHasNextChapter(true);
    	    }
    	}

    	Button ButtonPrevChapter = null;
    	ButtonPrevChapter = (Button)_config.GetListViewBibleContent().findViewById(R.id.ButtonPrevChapter);
       
    	if(ButtonPrevChapter != null)
    	{
    	    if(1 == ChapterID)
            {
    	    	ButtonPrevChapter.setVisibility(View.GONE);
        		_config.SetIsHasPrevChapter(false);
            }
    	    else
    	    {
    	    	ButtonPrevChapter.setVisibility(View.VISIBLE);
        		_config.SetIsHasPrevChapter(true);
    	    }
    	}
    	
    	
        
        return iResult;
    }
	
    

    
    //填充经文的listview
    public void FillListView()
    {

 	   //重置下载状态
 	   _config.SetIsDownloading(false);
 	   
    	//设置章ID
        _config.setVerseID(1);
        _config.SetStrVolumeName(_config.GetVolumeNameByID(_config.getVolumeID()));


        //更新标题
        StringBuilder sb = new StringBuilder();
        sb.append(_config.GetStrVolumeName());
		sb.append(" 第");
		sb.append(_config.getChapterID());
		sb.append("章");
        //设置标题栏
        mButtonCenter.setText(sb.toString());
		sb.delete(0, sb.length());
		sb = null;


		//更新经文内容，并刷新控件的数据源
        int iCount = _config.GetInstanceMain_Tab_Readbible().GetLectionByVolumeIDandChapterID(_config.getVolumeID(),_config.getChapterID(),_config.getStrsBibleContent());

        //如果第一节里面有经文，则继续显示
        if(iCount > 0 && _config.getStrsBibleContent()[0].length()>0)
        {
	 	   //设置获取到的小节数
	 	   _config.setVerseCount(iCount);

	 	   //刷新经文列表
	 	   HeaderViewListAdapter listAdapter = (HeaderViewListAdapter) _config.GetListViewBibleContent().getAdapter();  //首先先将listView强制转换为HeaderViewListAdapter
		   
		   MyAdapterLection myAdapterLection = null;
		   
		   //设置ListView
		   if(listAdapter == null)
		   {
			   myAdapterLection = new MyAdapterLection(Main_Tab_Readbible.this);
		   }
		   else
		   {
			   //通过HeaderViewListAdapter 转换为自定义的adapter
			   myAdapterLection = (MyAdapterLection)listAdapter.getWrappedAdapter();
		   }
	
		   _config.GetListViewBibleContent().setAdapter(myAdapterLection);
	
		   //刷新
		   myAdapterLection.notifyDataSetChanged();
	   
	       //默认选中经文列表的第一行
	        this.SelectItemIndex(0);
	        
	        //更新历史记录
	        MarkHistory(_config.getVolumeID(),_config.getChapterID());
	        
        }
    

    }
    
    
    /**
     * 选中第几行，从0开始
    */
    public void SelectItemIndex(int i)
    {
        //默认选中第一行，以保存当前状态。。。这个。。
        if(_config.GetListViewBibleContent().getCount()>0)
        {
     	   _config.GetListViewBibleContent().setSelection(i);
        }
    }
    


    //退出按键-----------------------

	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

	    return super.dispatchKeyEvent(event);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	
	//显示长按菜单窗口
	private void showLongClickMenu(final int selectedIndex)
	{
		String items[] = new String[]{"复制小节","复制整章","加入书签","管理书签"};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("快捷操作");

		builder.setItems(items, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(final DialogInterface dialog, int which)
			{
				switch(which)
				{
				case 0:
					String strTitle = "";
					String strTextCopy = "";
					
					//复制小节
					//卷名+章号+节号
					int iVerseID = selectedIndex+1;
					 strTitle = _config.GetStrVolumeName() + " " + _config.getChapterID() + ":" + iVerseID;

					 strTextCopy = strTitle + " " +  _config.getStrsBibleContent()[selectedIndex];
					
					ClipboardManager clipboard1 = (ClipboardManager) getSystemService(Main_Tab_History.CLIPBOARD_SERVICE);
					clipboard1.setText(strTextCopy);

					Toast.makeText(Main_Tab_Readbible.this,"复制成功", Toast.LENGTH_LONG).show();
					
					break;
				case 1:
					//复制整章
					StringBuilder sbTextCopy = new StringBuilder();
					
					sbTextCopy.append(_config.GetStrVolumeName());
					sbTextCopy.append(" 第 ");
					sbTextCopy.append(_config.getChapterID());
					sbTextCopy.append(" 章\r\n");
					
					 for(int i=0;i<_config.getVerseCount();i++)
					 {
						 sbTextCopy.append(i+1);
						 sbTextCopy.append(" ");
						 sbTextCopy.append(_config.getStrsBibleContent()[i]);
						 sbTextCopy.append("\r\n");
					 }
					 
					ClipboardManager clipboard2 = (ClipboardManager) getSystemService(Main_Tab_History.CLIPBOARD_SERVICE);
					clipboard2.setText(sbTextCopy.toString());

					Toast.makeText(Main_Tab_Readbible.this,"复制成功", Toast.LENGTH_LONG).show();
					
					sbTextCopy.delete(0, sbTextCopy.length());
					sbTextCopy = null;
					
					break;
				case 2:
					//加入书签
					
			        int iVerseIDForBookmark = selectedIndex+1;
			        
					String strSQL ="insert into  [BibleLabels]   ([name],[volumesn],[chaptersn],[versesn]) values ('"+ _config.GetStrVolumeName() +"', "+ _config.getVolumeID() +", "+ _config.getChapterID() +", "+ iVerseIDForBookmark +") ";

				  	//由卷ID得到已读的章的阅读次数数组
					DatabaseHelper dbHelper = new DatabaseHelper(Main_Tab_Readbible.this, _config.GetStrDatabaseNameForDeployment());
			        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  
			        sqliteDatabase.execSQL(strSQL);

			        Toast.makeText(Main_Tab_Readbible.this,"添加书签成功", Toast.LENGTH_LONG).show();

					break;
				case 3:
					//管理书签
					Intent intentBookmark = new Intent (Main_Tab_Readbible.this,BookmarkActivity.class);
					startActivity(intentBookmark);

					break;
				}

				//关闭窗口
		        Handler handler = new Handler();
		        Runnable runnable = new Runnable()
		        {
		          @Override
		           public void run()
		           {
		               // 调用AlertDialog类的dismiss()方法关闭对话框，也可以调用cancel()方法。
		            	  dialog.dismiss();
		               }
		           };
		
		           handler.post(runnable);
		      }
		   });
		
		    builder.
		    setNegativeButton("取 消", new DialogInterface.OnClickListener() { 
		    @Override 
		    public void onClick(DialogInterface dialog, int which) { 
		        	dialog.dismiss();
		        } 
		    });
		    
		
		    builder.create().show();
		}

	
 	
}