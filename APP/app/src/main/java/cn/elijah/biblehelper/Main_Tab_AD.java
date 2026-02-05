package cn.elijah.biblehelper;


import cn.elijah.biblehelper.R;
import android.app.ActivityGroup;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;


public class Main_Tab_AD extends ActivityGroup {
	  

	//关于我们的布局
	public LinearLayout adViewLayout = null;
    
	//重写Application，作为全部变量配置文件
	private Config _config;
	
	
	
	@Override
	   public void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.main_tab_ad); 
			
	       //获得自定义的应用程序Config
	       _config = (Config)getApplication(); 
	       
	       //保存实例
	       _config.SetInstanceMain_Tab_AD(this);

	        //关于我们网页 
	        WebView webView = (WebView)findViewById(R.id.webViewAboutUs);
	        webView.getSettings().setSupportZoom(true);

	        webView.loadUrl("file:///android_asset/about.htm"); 

	 }

	 

		//设置标题栏右侧按钮
		public void btnmainright(View v) {  

	     }  	
	    
		//设置标题栏中间文字
		public void btnmaincenter(View v) {  

	     }  
	 
		//设置标题栏右侧按钮
		public void btnmainleft(View v) {  

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
		
}