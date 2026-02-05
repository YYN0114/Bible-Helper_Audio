package cn.elijah.biblehelper;


import java.util.ArrayList;
import cn.elijah.biblehelper.R;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends ActivityGroup {

	
	public static MainActivity instance = null;
	

	
	private LinearLayout mBigTab1,mBigTab2,mBigTab3,mBigTab4;
	
	private LinearLayout mTabLayout;
	
	private ArrayList<View> mTabViews;
	
	private ImageView mTabImg;// 动画图片
	private ImageView mTab1,mTab2,mTab3,mTab4;
	public int currIndex = 0;// 当前页卡编号
	private int one;//单个水平动画位移
	//重写Application，作为全部变量配置文件
	private Config _config;


   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

       setContentView(R.layout.main_biblehelper);
       
       //获得自定义的应用程序Config
       _config = (Config)getApplication(); 
       
       //保存实例
       _config.SetMainActivity(this);
       
        //启动activity时不自动弹出软键盘
       getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
       instance = this;
       
       //底部4个图片按钮的实例
       mTab1 = (ImageView) findViewById(R.id.img_weixin);
       mTab2 = (ImageView) findViewById(R.id.img_address);
       mTab3 = (ImageView) findViewById(R.id.img_friends);
       mTab4 = (ImageView) findViewById(R.id.img_settings);
       //设置点击事件
       mTab1.setOnClickListener(new MyOnClickListener(0));
       mTab2.setOnClickListener(new MyOnClickListener(1));
       mTab3.setOnClickListener(new MyOnClickListener(2));
       mTab4.setOnClickListener(new MyOnClickListener(3));
       
       //如果只是底部4个图片按钮点击，很难操作。为了使点击范围更大，将外部的布局也增加了click事件
       mBigTab1 = (LinearLayout) findViewById(R.id.ll_tab1);
       mBigTab2 = (LinearLayout) findViewById(R.id.ll_tab2);
       mBigTab3 = (LinearLayout) findViewById(R.id.ll_tab3);
       mBigTab4 = (LinearLayout) findViewById(R.id.ll_tab4);
       //点击事件
       mBigTab1.setOnClickListener(new MyOnClickListener(0));
       mBigTab2.setOnClickListener(new MyOnClickListener(1));
       mBigTab3.setOnClickListener(new MyOnClickListener(2));
       mBigTab4.setOnClickListener(new MyOnClickListener(3));

       //底部动画效果图片的实例
       mTabImg = (ImageView) findViewById(R.id.img_tab_now);
       
       
     //设置最底部移动光标图片的宽度，及动画效果移动的距离
       ReSizeImage();
       

		//获得4个TAB内容页的实例
		//阅读圣经
       View view1 = getLocalActivityManager().startActivity(
               "Main_Tab_Readbible",
               new Intent(MainActivity.this, Main_Tab_Readbible.class))
               .getDecorView();
       
       //历史记录
       View view2 = getLocalActivityManager().startActivity(
               "Main_Tab_History",
               new Intent(MainActivity.this, Main_Tab_History.class))
               .getDecorView();
       
       //系统设置
       View view3 = getLocalActivityManager().startActivity(
               "Main_Tab_Config",
               new Intent(MainActivity.this, Main_Tab_Config.class))
               .getDecorView();
       
       //推荐应用
       View view4 = getLocalActivityManager().startActivity(
               "Main_Tab_AD",
               new Intent(MainActivity.this, Main_Tab_AD.class))
               .getDecorView();

	   //将4个页面加入列表，供切换使用
	   mTabViews = new ArrayList<View>();
	   mTabViews.add(view1);
	   mTabViews.add(view2);
	   mTabViews.add(view3);
	   mTabViews.add(view4);
		
	   //获得tab容器的实例  			
	   mTabLayout = (LinearLayout)findViewById(R.id.tablayout);
	
	   //切换到第一页
	   ChangeTabLayout(0);
   }
   
   public void ChangePage(int index)
   {
		//如果当前页码与将要切换的页码相等，则不切换
		if(currIndex == index)
		{
			return;
		}
		else
		{
			//切换页面
			ChangeTabLayout(index);
			//动画效果
			SetTabAnimation(index);

			//当前页
			currIndex = index;
			
			if(currIndex == 1)
			{
				if(_config.GetMain_Tab_History() != null)
				{
					_config.GetMain_Tab_History().FillListView();
				}
					
			}
			
			if(currIndex == 2)
			{
				if(_config.GetMain_Tab_Config() != null)
				{
					_config.GetMain_Tab_Config().ReloadSetting();
				}
					
			}
			
			if(currIndex == 3)
			{
				if(_config.GetInstanceMain_Tab_AD() != null)
				{
					LinearLayout adViewLayout = _config.GetInstanceMain_Tab_AD().adViewLayout;

					if(adViewLayout != null)
					{
						   adViewLayout.removeAllViews();
					}
					
				}
						
	
			}
			
		}
		
   }
   
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
     super.onConfigurationChanged(newConfig);

   //设置最底部移动光标图片的宽度，及动画效果移动的距离
     ReSizeImage();
 
     if(currIndex != 0)
     {
    	 int oldIndex = currIndex;
    	 
		//切换页面
		ChangeTabLayout(0);
		//动画效果
		SetTabAnimation(0);

		currIndex = 0;
			
			
		//切换页面
		ChangeTabLayout(oldIndex);
		//动画效果
		SetTabAnimation(oldIndex);
		
		currIndex = oldIndex;
			
     }
   }

   //设置最底部移动光标图片的宽度，及动画效果移动的距离
   private void ReSizeImage()
   {
	 //获取屏幕当前分辨率
       Display currDisplay = getWindowManager().getDefaultDisplay();
       int displayWidth = currDisplay.getWidth();
       //int displayHeight = currDisplay.getHeight();
       
       //设置水平动画平移大小
       one = displayWidth/4; 
       //设置mTabImg的宽度，适应不同分辨率
       mTabImg.setLayoutParams(new RelativeLayout.LayoutParams(
				one , RelativeLayout.LayoutParams.MATCH_PARENT));		
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
			
			ChangePage(index);
		}
	};
   
	//切换TAB页
	public void ChangeTabLayout(int arg0)
	{
		mTabLayout.removeAllViews();
		mTabLayout.addView(mTabViews.get(arg0)); 
	}
	
	//下方菜单的动画效果
	public void SetTabAnimation(int arg0)
	{
		switch (arg0) {
		case 0:
			mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_pressed));
			if (currIndex == 1) {
				mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
			} else if (currIndex == 2) {
				mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
			}
			else if (currIndex == 3) {
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
			}
			break;
		case 1:
			mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_pressed));
			if (currIndex == 0) {
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
			} else if (currIndex == 2) {
				mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
			}
			else if (currIndex == 3) {
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
			}
			break;
		case 2:
			mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_pressed));
			if (currIndex == 0) {
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
			} else if (currIndex == 1) {
				mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
			}
			else if (currIndex == 3) {
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
			}
			break;
		case 3:
			mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_pressed));
			if (currIndex == 0) {
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
			} else if (currIndex == 1) {
				mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
			}
			else if (currIndex == 2) {
				mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
			}
			break;
		}
		
		Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);
		
		animation.setFillAfter(true);// True:图片停在动画结束位置
		animation.setDuration(150);
		mTabImg.startAnimation(animation);
		
		animation = null;
		
	}
	

	//退出按键-----------------------
	
	public void Exit()
	{

    		//询问是否退出
    		AlertDialog.Builder builder = new Builder(this);  
            builder.setMessage("是否退出程序？");  
            builder.setTitle("提示");  
            builder.setPositiveButton("确认",  
            new android.content.DialogInterface.OnClickListener() {
                @Override  
                public void onClick(DialogInterface dialog, int which) {
                	
                	//停止播放
                	if(_config.GetVoicePanelActivity().mediaPlayer != null)
                	{
                		_config.GetVoicePanelActivity().mediaPlayer.stop();
                		//重置进度条
                		_config.GetVoicePanelActivity().seekBar.setProgress(0);
                	}
                	
                	dialog.dismiss();
                	
                	MainActivity.instance.finish();//关闭Activity
                	
                	android.os.Process.killProcess(android.os.Process.myPid()); //获取PID

                	System.exit(0); //常规java、c#的标准退出法，返回值为0代表正常退出

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

	
    //退出按键-----------------------
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
	            && event.getAction() == KeyEvent.ACTION_DOWN 
	            && event.getRepeatCount() == 0) {
	        //具体的操作代码 
		    Exit();	    	
	    }

	    return super.dispatchKeyEvent(event);
	}
	
}