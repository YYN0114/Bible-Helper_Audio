package cn.elijah.biblehelper;

import cn.elijah.biblehelper.R;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

public class Main_Tab_Config extends Activity {

 

    private RadioButton rbLectionFontSize1;  
    private RadioButton rbLectionFontSize2;  
    

    private RadioButton rbAutoDownload1;  
    private RadioButton rbAutoDownload2;  
    
    
	//重写Application，作为全部变量配置文件
	private Config _config;

	
	
	@Override	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_config);

		//获得自定义的应用程序Config
		_config = (Config)getApplication();
		
		//保存实例
		_config.SetMain_Tab_Config(this);
	       
		//字号
		rbLectionFontSize1 = (RadioButton)findViewById(R.id.radioLectionFontSize1);
		rbLectionFontSize2 = (RadioButton)findViewById(R.id.radioLectionFontSize2);
		
		//自动下载
		rbAutoDownload1 = (RadioButton)findViewById(R.id.radioAutoDownload1);
		rbAutoDownload2 = (RadioButton)findViewById(R.id.radioAutoDownload2);
		
		//读取配置并设置到UI
		ReloadSetting();
		
	}
	 
	//保存，向数据库中写入设置
	public void onButtonSaveClick(View v)
	{
		//字号
		if(rbLectionFontSize1.isChecked())
		{
			//一般字号
			_config.SetLectionFontSize(20);
		}
		else
		{
			//大字号
			_config.SetLectionFontSize(40);
		}
		
		
		//自动下载
		if(rbAutoDownload1.isChecked())
		{
			//自动下载
			_config.SetEnableAutoDownloadVoiceFile(true);
		}
		else
		{
			//禁止自动下载
			_config.SetEnableAutoDownloadVoiceFile(false);
		}

		//写入数据库
		_config.WriteConfigToDatabase();
		
		//提示设置成功
		Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
		
	}
	
	//取消，读取数据库值，还原设置选项
	public void onButtonCancelClick(View v)
	{
		ReloadSetting();
		
		
		//提示设置成功
		Toast.makeText(getApplicationContext(), "已经取消", Toast.LENGTH_SHORT).show();
	}
	
	//从配置类config中读取设置的值
	public void ReloadSetting()
	{

    	if(_config.GetLectionFontSize() == 20)
    	{
    		//字号：普通
    		rbLectionFontSize1.setChecked(true);
		}
		else
		{
    		//字号：大
			rbLectionFontSize2.setChecked(true);
		}

    	//isAutoDownFile int=是否自动下载文件（0=不自动下载 1=自动下载）
	   	if(_config.GetEnableAutoDownloadVoiceFile())
	   	{
	   		rbAutoDownload1.setChecked(true);
	   	}
	   	else
	   	{
	   		//禁止自动下载
	   		rbAutoDownload2.setChecked(true);
	   	}
		
		
	}
	
	//点击还原
	public void onButtonRestoreClick(View v)
	{
		
	}
	
	//返回按钮
	public void onButtonBackClick(View v) {  
		this.finish();
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