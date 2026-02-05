package cn.elijah.biblehelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressLint({ "DefaultLocale", "HandlerLeak" })
public class VoicePanelActivity extends Activity {
	/** Called when the activity is first created. */

	public MediaPlayer mediaPlayer;
	private ImageButton button;
	public SeekBar seekBar;
	private String mp3Path;
	private Config _config;

	@SuppressLint("DefaultLocale")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_voice_panel);

		 //获得自定义的应用程序Config   
		_config = (Config)getApplication(); 


		mediaPlayer = new MediaPlayer();

		button = (ImageButton) findViewById(R.id.buttonplaycontrol);
		
		button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));

		seekBar = (SeekBar) findViewById(R.id.seekbarvoice);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					
					if(mediaPlayer.isPlaying())
					{
						mediaPlayer.seekTo(progress);
					}

				}
			}
		});
 
		
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
				else 
				{
					//进度条大于0，表示可以继续播放
					if(seekBar.getProgress() > 0)
					{
						mediaPlayer.start();
					}
					else
					{
						AutoPlay();
					}

					//更新播放按钮的图标状态
					UpdatePlaybuttonIconStatus();
					
					new Thread(new runable()).start();
				}
				
				//更新播放按钮的图标状态
				UpdatePlaybuttonIconStatus();
				
			}
		});

		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@SuppressLint("DefaultLocale")
			@Override
			public void onCompletion(MediaPlayer mp) {
				//播放结束后

				//更新播放按钮状态
				UpdatePlaybuttonIconStatus();
				
				//重置进度条
				seekBar.setProgress(0);
				
				//如果有下一章
				if(_config.GetIsHasNextChapter())
				{
		 		   //章ID增长1
		 		   _config.setChapterID(_config.getChapterID() + 1);
		 		   //填充列表
		 		   _config.GetInstanceMain_Tab_Readbible().FillListView();
		 	   }
		       else if(_config.getVolumeID() < 66)
		       {
		    	   //没有下一章，判断是否有下一书卷，如果有，则跳转到下一书卷
		    	   //书卷ID加1
		    	  _config.setVolumeID(_config.getVolumeID() + 1);

				   //设置为第1章
				   _config.setChapterID(1);
			
				   //填充列表
				   _config.GetInstanceMain_Tab_Readbible().FillListView();
		       }
		       else
		       {
		    	   //如果没有下一章也没有下一书卷，退出函数
		    	   return;
		       }
				
				//如果有下一章或下一书卷
				//判断是否有MP3文件
				String strVolumeID_00 = String.format("%02d",_config.getVolumeID());
				
				//得到当前章对应的正确的音频文件名称
				String strCorrectMP3FileName = strVolumeID_00  + "-" + _config.getChapterID() + ".mp3";

				//MP3路径
				mp3Path = FileUtil.setMkdir(VoicePanelActivity.this, _config.GetVoiceFileStorageFolder()) + File.separator + strCorrectMP3FileName;

				//判断MP3是否存在
				File mp3FileExists=new File(mp3Path);

				if(mp3FileExists.exists())
				{
				   //开始播放
				   AutoPlay();
				}
				else
				{
					//不存在，提示下载
					//如果用户已经设置为允许自动下载，则不提示
					if(_config.GetEnableAutoDownloadVoiceFile())
					{
					   //开始播放
					   AutoPlay();
					}
					else
					{
						//如果未设置自动下载，则提示
						//提示是否自动下载播放下一章，并自动下载下一章，更改配置文件
						AlertDialog.Builder builder = new Builder(_config.GetInstanceMain_Tab_Readbible());  
				        builder.setMessage("本章播放结束，即将下载下一章，您是否想让圣经助手自动下载并播放下一章的语音？\r\n选择[确认]，允许自动下载。\r\n选择[取消]，停止播放。\r\n您可以在系统设置里重设这个选项。");  
				        builder.setTitle("提示");  
				        builder.setPositiveButton("确认",  
				        new android.content.DialogInterface.OnClickListener() {
				            @Override  
				            public void onClick(DialogInterface dialog, int which) {
				            	//自动跳转到下一章，或下一书卷

				            	//设置为允许自动下载
				            	_config.SetEnableAutoDownloadVoiceFile(true);
				            	
				            	//写入数据库
				            	_config.WriteConfigToDatabase();
				            	
							   //开始播放
							   AutoPlay();
				            }  
				        });  
				        builder.setNegativeButton("取消",  
				        new android.content.DialogInterface.OnClickListener() {  
				            @Override  
				            public void onClick(DialogInterface dialog, int which) {  
				            	//设置为禁止自动下载
				            	_config.SetEnableAutoDownloadVoiceFile(false);


				            	//更新播放按钮图标状态
				            	UpdatePlaybuttonIconStatus();
				            	
				                dialog.dismiss();  
				            }  
				        });  
				        builder.create().show();  
					}
				}

			}
		});
		
		//设置总长0秒
		seekBar.setMax(0);
		
		//播放面板实例，用于在其他窗体判断播放的章节是否相符，音频文件是否存在，以及控制自动播放下一章等
		_config.SetVoicePanelActivity(this);
		
		//找到下载按钮
		_buttonDownload = (Button) this.findViewById(R.id.buttondownload);
		
	}
	
	//更新按钮图标状态
	public void UpdatePlaybuttonIconStatus()
	{
		if (mediaPlayer.isPlaying())
		{
			button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
		}
		else 
		{
			button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
		}
	}

	
		
	/**
	 * 看当前是否是播放的状态，如果否，则播放当前的章的音频。
		如果是，则判断当前播放的音频是否与当前章相符，如相符，则不改变。
		如不符，则播放新的音频。
		
		查看是否有这个mp3文件，如果有的话，
		则自动播放，显示播放时间进度条，并可以手动调节播放进度。
		如果没有这个mp3文件，弹出提示要消耗网络流量，询问是否下载，
		如果选择“是”，显示下载进度条，开始下载，下载完后自动关闭下载进度条，自动开始播放。
		
		开始播放后，提示用户：当语音播放完毕后，程序将自动跳转到下一章，是否允许程序自动联网并下载新章节的语音文件？（这个提示只出现一次，可以在【设置】功能里加上这个设置。）
		如果选择是，则开始下载下一章，这里只提前下载一章的。如果到了一卷的末尾，则下载下一卷书的第一章语音文件。
		当播放完最后一章时，自动跳转到下一卷书的第一章，自动播放。
	 * 
	 */
	@SuppressLint("DefaultLocale")
	public void AutoPlay()
	{
		//如果面板隐藏起来了，则不播放
		//如果正在下载，则不播放
		if(_config.GetInstanceMain_Tab_Readbible()._linearLayoutVoicePanel == null ||
				_config.GetInstanceMain_Tab_Readbible()._linearLayoutVoicePanel.getVisibility()!=0 ||
				_config.GetIsDownloading() == true)
		{
			if(mediaPlayer.isPlaying() == true)
			{
				mediaPlayer.stop();
			}
			seekBar.setProgress(0);
			UpdatePlaybuttonIconStatus();
			
			return;
		}

		//书卷ID字符串，必须2位，从01 至 66
		String strVolumeID_00 = String.format("%02d",_config.getVolumeID());
		
		//得到当前章对应的正确的音频文件名称
		String strCorrectMP3FileName = strVolumeID_00  + "-" + _config.getChapterID() + ".mp3";

		//是否正在播放
		if(mediaPlayer.isPlaying())
		{
			//判断正在播放的与当前显示的章是否相符，不符则尝试播放（下载）正确的当前章的MP3

			//正确的播放路径含文件名的
			String strCorrectMP3FilePath = FileUtil.setMkdir(VoicePanelActivity.this, _config.GetVoiceFileStorageFolder()) + File.separator + strCorrectMP3FileName;
			//如果相符，则不理睬，继续播着~
			if(strCorrectMP3FilePath.equalsIgnoreCase(mp3Path))
			{
			}
			else
			{
				//如果不相符，则停止播放
				if(mediaPlayer.isPlaying() == true)
				{
					mediaPlayer.stop();
				}
				
				seekBar.setProgress(0);
				
				//更新播放按钮的图标状态
				UpdatePlaybuttonIconStatus();

				//重新运行此函数播放正确的MP3
				AutoPlay();
			}
		}
		else
		{
			//尚未播放，或已停止（暂停）
			//得到本机路径
			//MP3路径
			mp3Path = FileUtil.setMkdir(VoicePanelActivity.this, _config.GetVoiceFileStorageFolder()) + File.separator + strCorrectMP3FileName;


			//判断MP3是否存在
			File mp3FileExists=new File(mp3Path);

			if(mp3FileExists.exists() == true)
			{
				//存在，播放
				//成功
				if(ResetMusic(mp3Path) == true)
				{
				}
				else
				{
					//失败
					//如果其中有一个文件不存在，则提示需要下载
					ShowDialog_RequestToDownload();
					//退出函数
					return;
				}

				//设置进度条的总长度
				seekBar.setMax(mediaPlayer.getDuration());
				
				mediaPlayer.start();
				
				//更新播放按钮的图标状态
				UpdatePlaybuttonIconStatus();
				
				//启动更新seekbar的线程
				new Thread(new runable()).start();

				//设置按钮图片
				button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
			}
			else
			{
				//如果其中有一个文件不存在，则提示需要下载
				ShowDialog_RequestToDownload();
			}

		}
	}

	//提示是否下载
	public void ShowDialog_RequestToDownload() 
	{ 
		//如果设置允许自动下载
		if(_config.GetEnableAutoDownloadVoiceFile())
		{
        	//开始下载
        	StartDownloadThread();
		}
		else
		{
			AlertDialog.Builder builder = new Builder(_config.GetInstanceMain_Tab_Readbible());  
	        builder.setMessage("圣经助手将自动下载本章的音频文件，这将使用网络流量，可能产生网络费用，确定要下载吗？");  
	        builder.setTitle("提示");  
	        builder.setPositiveButton("确认",  
	        new android.content.DialogInterface.OnClickListener() {
	            @Override  
	            public void onClick(DialogInterface dialog, int which) {
	            	//更新播放按钮图标状态
	            	UpdatePlaybuttonIconStatus();
	            	
	            	//开始下载
	            	StartDownloadThread();
	            }  
	        });  
	        builder.setNegativeButton("取消",  
	        new android.content.DialogInterface.OnClickListener() {  
	            @Override  
	            public void onClick(DialogInterface dialog, int which) {
	            	
	            	//更新播放按钮图标状态
	            	UpdatePlaybuttonIconStatus();
	            	
	            	
	                dialog.dismiss();  
	            }  
	        });  
	        builder.create().show();  
		}
	}
	

	public boolean ResetMusic(String path) {

		//设置成功
		boolean bResult = false;
		
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			
			//成功
			bResult = true;
			
		} catch (IllegalArgumentException e) {
			
			e.printStackTrace();
		} catch (IllegalStateException e) {
			
			e.printStackTrace();
		} catch (IOException e) {

		}
		
		return bResult;
	}

	class runable implements Runnable {

		@Override
		public void run() {
			
			while (mediaPlayer != null && mediaPlayer.isPlaying()) 
			{
				try {
					Thread.sleep(1000);
					if (seekBar.getMax() > 0) {
						seekBar.setProgress(mediaPlayer.getCurrentPosition());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	//下载--------------------------------------------------
	
	private static final int DOWNLOAD_PREPARE = 0;
	private static final int DOWNLOAD_WORK = 1;
	private static final int DOWNLOAD_OK = 2;
	private static final int DOWNLOAD_ERROR =3;

	/**
	 * 按钮点击事件
	 */

	
	private Button _buttonDownload ;

	
	public void onButtonDownloadClick(View v) {
		
		//停止播放
		if(mediaPlayer.isPlaying() == true)
		{
			mediaPlayer.stop();
		}

		seekBar.setProgress(0);
		
		//更新播放按钮的图标状态
		UpdatePlaybuttonIconStatus();
		
		downloadSize = 0;
		fileSize = 0;
		
		ShowDialog_RequestToDownload();
		
	}
	
	//开启下载线程下载
	public void StartDownloadThread()
	{
		if(_config.GetIsDownloading())
		{
			return;
		}

		_buttonDownload.setEnabled(false);
		
		//停止播放
		if(mediaPlayer.isPlaying())
		{
			mediaPlayer.stop();
		}
		
		seekBar.setProgress(0);
	
		button.setEnabled(false);
		seekBar.setEnabled(false);

		
		Toast.makeText(VoicePanelActivity.this, "开始下载 第 " + _config.getChapterID() + " 章", Toast.LENGTH_SHORT).show();
		new Thread(){
			@Override
			public void run() {
				downloadFileInThread();
				super.run();
			}
		}.start();
	}
	
	
	/**
	 * 文件下载
	 */
	private void downloadFileInThread()
	{
		//设置下载路径
		String strVolumeID_00 = String.format("%02d",_config.getVolumeID());
		String strMP3FileNameForDownload = strVolumeID_00  + "-" + _config.getChapterID() + ".mp3";
		
		
		//得到当前章对应的正确的音频文件名称

		//使用数字卷号格式
		String strCorrectMP3FileName = strVolumeID_00 + "-" + _config.getChapterID() + ".mp3";
		String strDownloadURL_MP3 = _config.GetInternetMP3FolderPath() + File.separator + strCorrectMP3FileName;
		
		//删除已有的文件，重新下载
		mp3Path = FileUtil.setMkdir(VoicePanelActivity.this, _config.GetVoiceFileStorageFolder()) + File.separator + strMP3FileNameForDownload;
		
		//删除mp3
		File mp3FileDelete = new File(mp3Path);

		mp3FileDelete.delete();
		
		//下载mp3
		download_subfunction(strDownloadURL_MP3,strMP3FileNameForDownload);
		

	}
	
	private void download_subfunction(String strDownloadURL,String strFileName)
	{
		try {
			
			strDownloadURL = URLEncoder.encode(strDownloadURL,"utf-8");
			strDownloadURL = strDownloadURL.replaceAll("%3A", ":").replaceAll("%2F", "/");
			
			URL u = new URL(strDownloadURL);
						
			URLConnection conn = u.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			fileSize = conn.getContentLength();
			if(fileSize<1||is==null)
			{
				sendMessage(DOWNLOAD_ERROR);
			}
			else
			{
				sendMessage(DOWNLOAD_PREPARE);
				FileOutputStream fos = new FileOutputStream(getPath(strFileName));
				byte[] bytes = new byte[1024];
				int len = -1;
				
				
				while((len = is.read(bytes))!=-1 &&
						_config.GetInstanceMain_Tab_Readbible()._linearLayoutVoicePanel.getVisibility() == 0 
						)
				{
					fos.write(bytes, 0, len);
					downloadSize+=len;
					sendMessage(DOWNLOAD_WORK);
				}
				
				is.close();
				fos.close();
				
				sendMessage(DOWNLOAD_OK);
			}
		} catch (Exception e) {
			sendMessage(DOWNLOAD_ERROR);

		} 
	}
	
	/**
	 * 文件一共的大小
	 */
	int fileSize = 0;
	/**
	 * 已经下载的大小
	 */
	int downloadSize = 0;
	/**
	 * handler处理消息
	 */
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_PREPARE:
				//设置下载状态
				_config.SetIsDownloading(true);

				break;
			case DOWNLOAD_WORK:

				
				int res = downloadSize*100/fileSize;
				_buttonDownload.setText(""+res+"%");
				break;
			case DOWNLOAD_OK:
				//判断目录下是否有对应的MP3文件，如果有，则尝试播放
				
				downloadSize = 0;
				fileSize = 0;
				
				
				String strVolumeID_00 = String.format("%02d",_config.getVolumeID());
				
				//得到当前章对应的正确的音频文件名称
				String strCorrectMP3FileName = strVolumeID_00  + "-" + _config.getChapterID() + ".mp3";
				
				mp3Path = FileUtil.setMkdir(VoicePanelActivity.this, _config.GetVoiceFileStorageFolder()) + File.separator + strCorrectMP3FileName;

				//判断MP3是否存在
				File mp3FileExists = new File(mp3Path);

				if(mp3FileExists.exists())
				{
					//设置下载状态
					_config.SetIsDownloading(false);
					
					Toast.makeText(VoicePanelActivity.this, "第 " + _config.getChapterID() + " 章 下载完成", Toast.LENGTH_SHORT).show();
					
					//下载完毕，尝试播放
					AutoPlay();
					
					_buttonDownload.setText("下载");
					
					_buttonDownload.setEnabled(true);
					button.setEnabled(true);
					seekBar.setEnabled(true);
				}

				
				break;
			case DOWNLOAD_ERROR:
				//设置下载状态
				_config.SetIsDownloading(false);
				
				Toast.makeText(VoicePanelActivity.this, "下载出错，请检查您的网络状态", Toast.LENGTH_SHORT).show();
				
				_buttonDownload.setEnabled(true);
				button.setEnabled(true);
				seekBar.setEnabled(true);
				
				break;
			}
			super.handleMessage(msg);
		}
	};
	/**
	 * 得到文件的保存路径
	 * @return
	 * @throws IOException
	 */
	private String getPath(String strFileName) throws IOException
	{
		String path = FileUtil.setMkdir(this,_config.GetVoiceFileStorageFolder())+File.separator+strFileName;
		return path;
	}
	/**
	 * 给hand发送消息
	 * @param what
	 */
	private void sendMessage(int what)
	{
		Message m = new Message();
		m.what = what;
		handler.sendMessage(m);
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