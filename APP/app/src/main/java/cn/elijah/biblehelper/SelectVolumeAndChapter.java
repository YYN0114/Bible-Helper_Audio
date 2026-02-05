package cn.elijah.biblehelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.elijah.biblehelper.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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


public class SelectVolumeAndChapter extends Activity {

	
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
    	setContentView(R.layout.select_volume_and_chapter);
	       
	       //获得自定义的应用程序Config
	       _config = (Config)getApplication(); 
	       
	       //保存实例
	       _config.SetInstanceSelectVolumeAndChapter(this);
	       
	       
	       //标头
	       InitTextView();
	       //旧约、新约切换页
	       InitViewPager();
	       
			//旧约书卷名数字
			final String mStrsVolume_OT[] = new String[39];
			//旧约书卷包含章数
			final int mIntsChapterCount_OT[] = new int[39];
			GetVolumeNameArray(0,mStrsVolume_OT,mIntsChapterCount_OT); 
	       
			//新约书卷名数字
			final String mStrsVolume_NT[] = new String[27];
			//新约书卷包含章数
			final int mIntsChapterCount_NT[] = new int[27];
			GetVolumeNameArray(1,mStrsVolume_NT,mIntsChapterCount_NT); 
	       
	       //为旧约listview填充数据
	       mListViewVolumeName_OT = (ListView)mViewsList.get(0).findViewById(R.id.listViewVolume) ;
	       mListViewVolumeName_OT.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				
				//实例化AlertDialog，将章数显示到里面
				//某一卷书所包含的章数，供选择
				String strsChapterIndex[] = new String[mIntsChapterCount_OT[position]]; 
				
				//填充章数 索引数组
				FillArrayChapterIndex(strsChapterIndex,strsChapterIndex.length);

				
				 //设置卷ID
	            _config.setVolumeID(position+1);

	            
				//显示对话框菜单
				showListDialog(strsChapterIndex,mStrsVolume_OT[position]);

			}
           });

	       mListViewVolumeName_NT = (ListView)mViewsList.get(1).findViewById(R.id.listViewVolume) ;
	       mListViewVolumeName_NT.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{

				//实例化AlertDialog，将章数显示到里面
				//某一卷书所包含的章数，供选择
				String strsChapterIndex[] = new String[mIntsChapterCount_NT[position]];
				//填充章数 索引数组
				FillArrayChapterIndex(strsChapterIndex,strsChapterIndex.length);
				
				 //设置卷ID
	            _config.setVolumeID(40 + position);
	            
				//显示对话框菜单
				showListDialog(strsChapterIndex,mStrsVolume_NT[position]);
			}
           });
	       

	       //旧约ListView
	       MyAdapterVolume adapter_ot = new MyAdapterVolume(SelectVolumeAndChapter.this,mStrsVolume_OT,mIntsChapterCount_OT);
	       mListViewVolumeName_OT.setAdapter(adapter_ot);

	       //新约ListView
	       MyAdapterVolume adapter_nt = new MyAdapterVolume(SelectVolumeAndChapter.this,mStrsVolume_NT,mIntsChapterCount_NT);
           mListViewVolumeName_NT.setAdapter(adapter_nt);

	       //设置显示旧约页，激活tab页的样式
	       mPager.setCurrentItem(1);
	       mPager.setCurrentItem(0);

    }
    
    //填充章数 数组
    private void FillArrayChapterIndex(String[] strs,int length)
    {
		//填充章数 数组
		for(int i=0;i<length;i++)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("第 ");
			sb.append(i + 1);
			sb.append(" 章");
			
			strs[i] = sb.toString();

			sb.delete(0, sb.length());
			sb = null;
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

	            //关闭选择章的窗体
    	       if(_config.GetInstanceSelectVolumeAndChapter() != null)
    	       {
    	    	   _config.GetInstanceSelectVolumeAndChapter().finish();   
    	       }

    	       
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
    
	
    public int GetVolumeNameArray(int OldOrNew, String[] outStrsResult, int[] outIntsResult)
    {
    	//返回数据个数
    	int iResult = 0;
    	//清空数组
    	Arrays.fill(outStrsResult, "");
    	Arrays.fill(outIntsResult, 0);
    	
        //创建DatabaseHelper对象  
        DatabaseHelper dbHelper = new DatabaseHelper(SelectVolumeAndChapter.this, _config.GetStrDatabaseNameForDeployment());
        // 得到一个SQLiteDatabase对象  
        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

        String strSQL = "select [SN], [ChapterNumber], [NewOrOld], [FullName]  from [BibleID] where  [NewOrOld]=" + OldOrNew + " order by [SN] asc ";
        Cursor cursor = sqliteDatabase.rawQuery(strSQL,null);
        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
        while (cursor.moveToNext())
        {
        	//书卷全名
        	outStrsResult[iResult] = cursor.getString(cursor.getColumnIndex("FullName"));
        	//书卷包含的章数
        	outIntsResult[iResult] = cursor.getInt(cursor.getColumnIndex("ChapterNumber"));
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
	    

		 public MyAdapterVolume(Context context, String mStrsVolume[], int mIntsChapterCount[]) 
		 {
			 mContext = context;

			 _mStrsVolume = mStrsVolume;
			 _mIntsChapterCount = mIntsChapterCount;

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
            	convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_volume, null);
            }

            //旧约，包含的书卷名
            TextView mTextView1 = (TextView) convertView.findViewById(R.id.ItemViewVolumeName);
			mTextView1.setTextColor(Color.BLACK);
			mTextView1.setText(_mStrsVolume[position]);

			//旧约，书卷中包含的章数
			TextView mTextView2 = (TextView) convertView.findViewById(R.id.ItemViewChapterCount);
			mTextView2.setTextColor(Color.GRAY);

			StringBuilder sb = new StringBuilder();
			sb.append("共");
			sb.append(_mIntsChapterCount[position]);
			sb.append("章");
			
			mTextView2.setText(sb.toString());
			
			sb.delete(0, sb.length());
			sb = null;
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

	public void onButtonBackClick(View v) {  

		this.finish();
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
            	t1.setTextColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.white));
            	t2.setTextColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.black));
            	
            	mBigTab1.setBackgroundColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.tab_indicator_text));
            	mBigTab2.setBackgroundColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.darker_gray));
            }
            else
            {
            	//切换到第二页
            	t1.setTextColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.black));
            	t2.setTextColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.white));

            	mBigTab1.setBackgroundColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.darker_gray));
            	mBigTab2.setBackgroundColor(SelectVolumeAndChapter.this.getResources().getColor(android.R.color.tab_indicator_text));
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

    
	