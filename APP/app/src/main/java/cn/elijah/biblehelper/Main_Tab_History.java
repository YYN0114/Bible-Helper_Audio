package cn.elijah.biblehelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.elijah.biblehelper.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class Main_Tab_History extends Activity {

	
    private ViewPager mPager;//页卡内容
    private List<View> mViewsList; // Tab页面列表

    private TextView t1, t2;// 页卡头标
    
    private LinearLayout mBigTab1,mBigTab2;
    

    //旧约，卷名ListView控件
    private ListView mListViewVolumeName_OT;
    
    //新约，卷名ListView控件
    private ListView mListViewVolumeName_NT;
    

	//重写Application，作为全部变量配置文件
	private Config _config;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main_tab_history);
	    
	       //获得自定义的应用程序Config
	       _config = (Config)getApplication(); 
	       
	       //保存实例
	       _config.SetMain_Tab_History(this);
	       

	       //标头
	       InitTextView();
	       //旧约、新约切换页
	       InitViewPager();
	       
			
	       FillListView();
	       
	       //设置显示旧约页，激活tab页的样式
	       mPager.setCurrentItem(1);
	       mPager.setCurrentItem(0);

    }
    
    public void FillListView()
    {
    	//旧约书卷名数字
		final String mStrsVolume_OT[] = new String[39];
		//旧约书卷包含章数
		final int mIntsChapterCount_OT[] = new int[39];
		//旧约书卷已读章数
		final int mIntsReadChapterCount_OT[] = new int[39];
		//填充数据
		GetVolumeNameArray(0,mStrsVolume_OT,mIntsChapterCount_OT,mIntsReadChapterCount_OT); 
       
		//新约书卷名数字
		final String mStrsVolume_NT[] = new String[27];
		//新约书卷包含章数
		final int mIntsChapterCount_NT[] = new int[27];
		//旧约书卷已读章数
		final int mIntsReadChapterCount_NT[] = new int[27];
		//填充数据
		GetVolumeNameArray(1,mStrsVolume_NT,mIntsChapterCount_NT,mIntsReadChapterCount_NT); 
       
		//为旧约listview填充数据
       mListViewVolumeName_OT = (ListView)mViewsList.get(0).findViewById(R.id.listViewVolume) ;
       mListViewVolumeName_OT.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
		{
			//实例化AlertDialog，将章数显示到里面
			//某一卷书所包含的章数，供选择
			String strsChapterIndexAndReadCount[] = new String[mIntsChapterCount_OT[position]]; 
			
			//填充章数 索引数组
			FillArrayChapterIndex(strsChapterIndexAndReadCount,strsChapterIndexAndReadCount.length,position+1);

			
			 //设置卷ID
            _config.setVolumeID(position+1);

            
			//显示对话框菜单
			showListDialog(strsChapterIndexAndReadCount,mStrsVolume_OT[position]);

		}
       });

       mListViewVolumeName_NT = (ListView)mViewsList.get(1).findViewById(R.id.listViewVolume) ;
       mListViewVolumeName_NT.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
		{
			//实例化AlertDialog，将章数显示到里面
			//某一卷书所包含的章数，供选择
			String strsChapterIndexAndReadCount[] = new String[mIntsChapterCount_NT[position]];
			//填充章数 索引数组
			FillArrayChapterIndex(strsChapterIndexAndReadCount,strsChapterIndexAndReadCount.length,40 + position);
			
			 //设置卷ID
            _config.setVolumeID(40 + position);
            
			//显示对话框菜单
			showListDialog(strsChapterIndexAndReadCount,mStrsVolume_NT[position]);
		}
       });
       

       //旧约ListView
       MyAdapterVolume adapter_ot = new MyAdapterVolume(Main_Tab_History.this,mStrsVolume_OT,mIntsChapterCount_OT,mIntsReadChapterCount_OT);
       mListViewVolumeName_OT.setAdapter(adapter_ot);
       adapter_ot.notifyDataSetChanged();
       
       //新约ListView
       MyAdapterVolume adapter_nt = new MyAdapterVolume(Main_Tab_History.this,mStrsVolume_NT,mIntsChapterCount_NT,mIntsReadChapterCount_NT);
       mListViewVolumeName_NT.setAdapter(adapter_nt);
       adapter_nt.notifyDataSetChanged();
    }
    
    
    
    //填充章数 数组
    private void FillArrayChapterIndex(String[] strs,int length, int volumeID)
    {
    	
		//填充章数 数组
		for(int i=0;i<length;i++)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("第 ");
			sb.append(i + 1);
			sb.append(" 章   未阅读");
			
			strs[i] = sb.toString();

			sb.delete(0, sb.length());
			sb = null;
		}

    	//由卷ID得到已读的章的阅读次数数组
		DatabaseHelper dbHelper = new DatabaseHelper(Main_Tab_History.this, _config.GetStrDatabaseNameForDeployment());

		// 得到一个SQLiteDatabase对象  
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        String sql = "SELECT CHAPTERSN,  [count] as ReadCount FROM biblestatistics where VOLUMESN=" + volumeID + "  order by [id]";
        Cursor cursor = sqliteDatabase.rawQuery(sql, null);
        while(cursor.moveToNext())
        {
        	int iIndex = cursor.getInt(cursor.getColumnIndex("CHAPTERSN"));
        	int iCount = cursor.getInt(cursor.getColumnIndex("ReadCount"));
        	strs[iIndex-1] = strs[iIndex-1].replace("未阅读", "已阅读 "+ iCount + " 次");
        }

    }
    

    private void showListDialog(final String[] items, final String strVolumeName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("跳转到 " + strVolumeName);
        /**
         * 1、public Builder setItems(int itemsId, final OnClickListener
         * listener) itemsId表示字符串数组的资源ID，该资源指定的数组会显示在列表中。 2、public Builder
         * setItems(CharSequence[] items, final OnClickListener listener)
         * items表示用于显示在列表中的字符串数组
         */
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
        	@Override
        	public void onClick(final DialogInterface dialog, int which)
        	{

        		//填充经文的listview
        		
        		_config.setChapterID(which + 1);
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

	           dialog.cancel();

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

	/**
	 * OldOrNew - 0 旧约,1 新约
	 * outStrsResult - 书卷名称
	 * outIntsResult - 书卷的章数
	 * outIntsReadChapterCount - 书卷中已读的章数
	 */
    public int GetVolumeNameArray(int OldOrNew, String[] outStrsResult, int[] outIntsResult, int[] outIntsReadChapterCount)
    {
    	//返回数据个数
    	int iResult = 0;
    	//清空数组
    	Arrays.fill(outStrsResult, "");
    	Arrays.fill(outIntsResult, 0);
    	Arrays.fill(outIntsReadChapterCount, 0);
    	
        //创建DatabaseHelper对象  
        DatabaseHelper dbHelper = new DatabaseHelper(Main_Tab_History.this, _config.GetStrDatabaseNameForDeployment());
        // 得到一个SQLiteDatabase对象  
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        String args[] = {String.valueOf(OldOrNew)};
        String sql = "SELECT bibleid.chapternumber,bibleid.fullname,count(biblestatistics.CHAPTERSN)  as ReadChapterCount FROM bibleid LEFT JOIN biblestatistics ON bibleid.sn=biblestatistics.volumesn  where bibleid.newOrOld=?   group by bibleid.fullname  order by bibleid.SN " ;
        Cursor cursor = sqliteDatabase.rawQuery(sql, args);

        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
        while (cursor.moveToNext())
        {
        	//书卷全名
        	outStrsResult[iResult] = cursor.getString(cursor.getColumnIndex("FullName"));
        	//书卷包含的章数
        	outIntsResult[iResult] = cursor.getInt(cursor.getColumnIndex("ChapterNumber"));
        	//已读的章数
        	outIntsReadChapterCount[iResult] = cursor.getInt(cursor.getColumnIndex("ReadChapterCount"));
            iResult++;
        }

        return iResult;
    }
	


    //为选择书卷listview使用的适配器
	private class MyAdapterVolume extends BaseAdapter 
	{        
	    //书卷名数字
	    private String _mStrsVolume[] = null;
	    //书卷包含章数
	    private int _mIntsChapterCount[] = null;
	    //当前卷 已读章数
	    private int _mIntsReadChapterCount[] = null;
	    

		 public MyAdapterVolume(Context context, String mStrsVolume[], int mIntsChapterCount[], int mIntsReadChapterCount[]) 
		 {
			 mContext = context;

			 _mStrsVolume = mStrsVolume;
			 _mIntsChapterCount = mIntsChapterCount;
			 _mIntsReadChapterCount = mIntsReadChapterCount;
		 }
		 
		 @Override 
        public int getCount()
        {      
			 return _mStrsVolume.length;
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
            	convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_history_volume, null);

            }

            TextProgressBar textProgressBar = (TextProgressBar)convertView.findViewById(R.id.ItemViewHistoryProgressBar);

            //设置最大值，共有多少章
            textProgressBar.setMax(_mIntsChapterCount[position]);
            //设置当前值，非 百分比
            textProgressBar.setText(_mIntsReadChapterCount[position], _mStrsVolume[position]);

			return convertView;

        } 
        private Context mContext;
	 }

	/*
     * 初始化头标
     */
    private void InitTextView() {
        t1 = (TextView) findViewById(R.id.tv1);
        t2 = (TextView) findViewById(R.id.tv2);

        t1.setOnClickListener(new MyOnClickListener(0));
        t2.setOnClickListener(new MyOnClickListener(1));

        //如果只是底部4个图片按钮点击，很难操作。为了使点击范围更大，将外部的布局也增加了click事件
        mBigTab1 = (LinearLayout) findViewById(R.id.ll_tab1);
        mBigTab2 = (LinearLayout) findViewById(R.id.ll_tab2);
        
        mBigTab1.setOnClickListener(new MyOnClickListener(0));
        mBigTab2.setOnClickListener(new MyOnClickListener(1));        
    }
    
	
	public void onButtonClearHistoryClick(View v) {  

		//提示，是否清除这些历史记录

		AlertDialog.Builder builder = new Builder(Main_Tab_History.this);  
        builder.setMessage("是否删除所有的历史记录？");  
        builder.setTitle("提示");  
        builder.setPositiveButton("确认",  
        new android.content.DialogInterface.OnClickListener() {
            @Override  
            public void onClick(DialogInterface dialog, int which) {
        		//清空历史记录
        		DatabaseHelper dbHelper = new DatabaseHelper(Main_Tab_History.this, _config.GetStrDatabaseNameForDeployment());

        		// 得到SQLiteDatabase对象  
                SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

                String strSQL = "delete FROM biblestatistics";
                sqliteDatabase.execSQL(strSQL);

                //刷新主界面
                FillListView();
                
                dialog.dismiss();
            }  
        });  
        builder.setNegativeButton("取消",  
        new android.content.DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  

                dialog.dismiss();  
            }  
        });  
        builder.create().show();
	}
	
	/**
     * 头标点击监听
*/
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    };
	
    /**
     * 初始化ViewPager
*/
    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.tabpager);
        mViewsList = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        mViewsList.add(mInflater.inflate(R.layout.select_tab_listview, null));
        mViewsList.add(mInflater.inflate(R.layout.select_tab_listview, null));

        PagerAdapter MyPagerAdapter = new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return mViewsList.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager)container).removeView(mViewsList.get(position));
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager)container).addView(mViewsList.get(position));
				return mViewsList.get(position);
			}
		};
		
        mPager.setAdapter(MyPagerAdapter);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
    }
    
    
    /**
     * 页卡切换监听
     */
    public class MyOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            //切换到第一页
            if(arg0 == 0)
            {
            	t1.setTextColor(Main_Tab_History.this.getResources().getColor(android.R.color.white));
            	t2.setTextColor(Main_Tab_History.this.getResources().getColor(android.R.color.black));
            	
            	mBigTab1.setBackgroundColor(Main_Tab_History.this.getResources().getColor(android.R.color.tab_indicator_text));
            	mBigTab2.setBackgroundColor(Main_Tab_History.this.getResources().getColor(android.R.color.darker_gray));
            }
            else
            {
            	//切换到第二页
            	t1.setTextColor(Main_Tab_History.this.getResources().getColor(android.R.color.black));
            	t2.setTextColor(Main_Tab_History.this.getResources().getColor(android.R.color.white));

            	mBigTab1.setBackgroundColor(Main_Tab_History.this.getResources().getColor(android.R.color.darker_gray));
            	mBigTab2.setBackgroundColor(Main_Tab_History.this.getResources().getColor(android.R.color.tab_indicator_text));
            }
            
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
    
    //退出按键-----------------------
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

	    return super.dispatchKeyEvent(event);
	}
    
}

    
	