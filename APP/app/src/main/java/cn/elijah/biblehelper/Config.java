package cn.elijah.biblehelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.annotation.SuppressLint;
import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.ListView;

@SuppressLint("SdCardPath")
public class Config extends Application {

	//存储书卷名称数组
	private String _strsVolumeName[] = null;

	 //圣经经文数组
	 private String _strsBibleContent[] = null;

	 //将要显示的小节数
	 private int _iVerseCount = 0;
	 //卷ID
	 private int _iVolumeID = 1;
	 //章ID
	 private int _iChapterID = 1;
	 //节ID，如果有节ID，则定位到这个节
	 private int _iVerseID = 1;
	 //显示的标题
	 private String _strVolumeName = "";

	 //部署使用的数据库的名字
	 private String _DatabaseNameForDeployment="";

	 //数据库在手机里的路径
	 private String _DatabasePathForDeployment="";
	    
	 //显示经文的ListView
	 private ListView _ListViewBibleContent;
		
	 //广告窗体实例
	 private Main_Tab_AD _instanceMain_Tab_AD = null;
	 
	 //Main_Tab_Readbible窗体实例
	 private Main_Tab_Readbible _instanceMain_Tab_Readbible = null;
	 
	 //SelectVolumeAndChapter窗体实例
	 private SelectVolumeAndChapter _instanceSelectVolumeAndChapter = null;
	 
	 private MainActivity _MainActivity = null;
	 
	 //圣经语音MP3文件路径
	 private String _InternetMP3FolderPath = "";
	 
	 //是否允许自动下载音频和歌词
	 private boolean _EnableAutoDownloadVoiceFile = false;
	 
	 //语音和歌词文件存储的目录，前面还带有SDCARD或者其他目录
	 private String _VoiceFileStorageFolder = "";

	 
	 //播放面板实例
	 private VoicePanelActivity _VoicePanelActivity = null;
	 
	 
	 //本卷书 是否有下一章
	 private boolean _IsHasNextChapter;
	 
	 //本眷属是否有上一章
	 private boolean _IsHasPrevChapter;
	 
	 //是否正在下载
	 private boolean _IsDownloading;
	 
	 //历史记录主界面
	 private Main_Tab_History _Main_Tab_History = null;
	 
	 
	 //经文字号大小
	 private int _LectionFontSize;
	 
	 //最后阅读书卷ID和章ID
	 private String _recentlyReadingVolumeIDAndChapterID;
	 
	 
	 //系统设置主界面
	 private Main_Tab_Config _Main_Tab_Config = null;
	 
	 //书签界面
	 private BookmarkActivity _BookmarkActivity = null;
	 
	 public BookmarkActivity GetBookmarkActivity()
	 {
		 return this._BookmarkActivity;
	 }
	 
	 public void SetBookmarkActivity(BookmarkActivity obj)
	 {
		 this._BookmarkActivity = obj;
	 }
	 
	 public Main_Tab_Config GetMain_Tab_Config()
	 {
		 return this._Main_Tab_Config;
	 }
	 
	 public void SetMain_Tab_Config(Main_Tab_Config obj)
	 {
		 this._Main_Tab_Config = obj;
	 }
	 

	    @Override
	    public void onCreate() {
	        
	        super.onCreate();
	        
	        //初始化全局变量
	        //数据库目录
	        SetStrDatabasePathForDeployment("/data/data/cn.elijah.biblehelper/databases/");
	        //数据库名称
	        SetStrDatabaseNameForDeployment("bible_unv.db");

	        //检查数据库是否存在
	        boolean dbExist = checkDataBase();
	        if(dbExist)
	        {
	        }
	        else
	        {
	     	   //不存在就把raw里的数据库写入手机
	            try{
	                copyDataBase();
	            }catch(IOException e){
	                throw new Error("Error copying database");
	            }
	        }

	        //------------------------------------------------
	        
	        //最大是176节，这里定义176长度的数组，反复使用
	        setStrsBibleContent(new String[176]);
	        
	        //小节 数
	        setVerseCount(0);
	        
	        SetInternetMP3FolderPath("https://raw.githubusercontent.com/YYN0114/Bible-Helper_Audio/refs/heads/master/audio");
	        
	        SetVoiceFileStorageFolder("BibleHelper");
	        
	        //正在下载，锁
	        SetIsDownloading(false);


	        //从数据库中读取配置数据，设置到全局变量中
	        ReadConfigFromDatabase();
	        

	      //------------------------------------------------
	        //存储书卷名字，为 GetStrVolumeName() 服务
	        _strsVolumeName = new String[66];

	        //数据库实例
			DatabaseHelper dbHelper = new DatabaseHelper(Config.this, this.GetStrDatabaseNameForDeployment());

			// 得到一个SQLiteDatabase对象  
	        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

	        String strSQL = "select [FullName]   from [BibleID]  order by [SN]";

	        Cursor cursor = sqliteDatabase.rawQuery(strSQL, null);
	        

	        int i=0;
	        while(cursor.moveToNext())
	        {
	        	_strsVolumeName[i] = cursor.getString(cursor.getColumnIndex("FullName"));
	        	i++;
	        }
	        
	        
	        //------------------------------------------------
	        //尝试将创世纪第一章的mp3文件拷贝到程序目录中
	        try{
	        	copyMP3File();
	        }catch(IOException e){
	        	throw new Error("Error copying mp3 file");
	        }
	    }
	    
	    

	    //从数据库读取配置
	    public void ReadConfigFromDatabase()
	    {

	    	//数据库实例
			DatabaseHelper dbHelper = new DatabaseHelper(Config.this, this.GetStrDatabaseNameForDeployment());

			// 得到一个SQLiteDatabase对象  
	        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();  

	        String sql = "select [isBigFont], [isAutoDownfile], [selectBible]  from [settings]";

	        Cursor cursor = sqliteDatabase.rawQuery(sql, null);
	        if(cursor.moveToNext())
	        {

	        	// isBigFont=是否显示大字号（0=不大字号 1=大字号 ）

	        	if(cursor.getInt(cursor.getColumnIndex("isBigFont")) == 1)
	        	{
	            	//字号：大
	        		this.SetLectionFontSize(40);
	        	}
	        	else
	        	{
	        		//字号：普通
	        		this.SetLectionFontSize(20);
	        	}
	        	
	        	
	        	//isAutoDownFile int=是否自动下载文件（0=不自动下载 1=自动下载）
	        	if(cursor.getInt(cursor.getColumnIndex("isAutoDownfile")) == 1)
	        	{
	                this.SetEnableAutoDownloadVoiceFile(true);
	        	}
	        	else
	        	{
	        		//禁止下载
	        		this.SetEnableAutoDownloadVoiceFile(false);
	        	}
	        	
	        	//selectBible=最后选择的卷-章（1-2）1代表卷的ID，2代表章的ID，-代表分割
	        	this.SetRecentlyReadingVolumeIDAndChapterID(cursor.getString(cursor.getColumnIndex("selectBible")));
	        	if(!this.GetRecentlyReadingVolumeIDAndChapterID().equalsIgnoreCase(""))
	        	{
	        		String strVolumeID = this.GetRecentlyReadingVolumeIDAndChapterID().substring(0,this.GetRecentlyReadingVolumeIDAndChapterID().indexOf('-'));
	        		String strChapterID = this.GetRecentlyReadingVolumeIDAndChapterID().substring(this.GetRecentlyReadingVolumeIDAndChapterID().indexOf('-') + 1);
	        		
	                setVolumeID(Integer.parseInt(strVolumeID));
	                setChapterID(Integer.parseInt(strChapterID));
	                
	                setVerseID(1);
	        	}


	        }
	    	
	    	
	    }
	    
	    
	    //写入配置到数据库
	    public void WriteConfigToDatabase()
	    {
	    	// isBigFont=是否 大字号
	    	//（0=普通，1=大 ）
	    	int isBigFont = 0;
	    	if(this.GetLectionFontSize() == 20)
	    	{
	    		//字号：普通
	    		isBigFont = 0;
			}
			else
			{
	    		//字号：大
				isBigFont = 1;
			}

	    	//isAutoDownFile int=是否自动下载文件（0=不自动下载 1=自动下载）
	    	int isAutoDownFile = 0;
		   	if(this.GetEnableAutoDownloadVoiceFile())
		   	{
		   		isAutoDownFile = 1;
		   	}
		   	else
		   	{
		   		//禁止自动下载
		   		isAutoDownFile = 0;
		   	}


			//数据库实例
			DatabaseHelper dbHelper = new DatabaseHelper(Config.this, this.GetStrDatabaseNameForDeployment());

			// 得到一个SQLiteDatabase对象  
			SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
			String sql = "update [settings] set [isBigFont]="+isBigFont + ", " +
	       		"[isAutoDownFile]=" + isAutoDownFile + "  where [SN]=0";

			sqliteDatabase.execSQL(sql);

	   }
	    
	 
	 
	 public String GetRecentlyReadingVolumeIDAndChapterID()
	 {
		 return this._recentlyReadingVolumeIDAndChapterID;
	 }
	 
	 public void SetRecentlyReadingVolumeIDAndChapterID(String str)
	 {
		 this._recentlyReadingVolumeIDAndChapterID = str;
	 }
	 
	 public int GetLectionFontSize()
	 {
		 return this._LectionFontSize;
	 }
	 
	 public void SetLectionFontSize(int size)
	 {
		 this._LectionFontSize = size;
	 }
	 
	 public Main_Tab_History GetMain_Tab_History()
	 {
		 return this._Main_Tab_History;
	 }
	 
	 public void SetMain_Tab_History(Main_Tab_History obj)
	 {
		 this._Main_Tab_History = obj;
	 }
	 
	 
	 public MainActivity GetMainActivity()
	 {
		 return this._MainActivity;
	 }
	 
	 public void SetMainActivity(MainActivity obj)
	 {
		 this._MainActivity = obj;
	 }
	 
	 public boolean GetIsDownloading()
	 {
		 return this._IsDownloading;
	 }
	 
	 public void SetIsDownloading(boolean b)
	 {
		 this._IsDownloading = b;
	 }
	 
	 public boolean GetIsHasPrevChapter()
	 {
		 return this._IsHasPrevChapter;
	 }
	 
	 public void SetIsHasPrevChapter(boolean bIsHas)
	 {
		 this._IsHasPrevChapter = bIsHas;
	 }
	 
	 public boolean GetIsHasNextChapter()
	 {
		 return this._IsHasNextChapter;
	 }
	 
	 public void SetIsHasNextChapter(boolean bIsHas)
	 {
		 this._IsHasNextChapter = bIsHas;
	 }
	 
	 public VoicePanelActivity GetVoicePanelActivity()
	 {
		 return this._VoicePanelActivity;
	 }
	 
	 public void SetVoicePanelActivity(VoicePanelActivity obj)
	 {
		 this._VoicePanelActivity = obj;		 
	 }
	 
	 public String GetVoiceFileStorageFolder()
	 {
		 return this._VoiceFileStorageFolder;
	 }
	 
	 public void SetVoiceFileStorageFolder(String str)
	 {
		 this._VoiceFileStorageFolder = str;
	 }
	 
	 public boolean GetEnableAutoDownloadVoiceFile()
	 {
		 return this._EnableAutoDownloadVoiceFile;
	 }
	 
	 public void SetEnableAutoDownloadVoiceFile(boolean b)
	 {
		 this._EnableAutoDownloadVoiceFile = b;
	 }
	 
	 public String GetInternetMP3FolderPath()
	 {
		 return this._InternetMP3FolderPath;
	 }
	 
	 public void SetInternetMP3FolderPath(String str)
	 {
		 this._InternetMP3FolderPath = str;
	 }

	 
	 public SelectVolumeAndChapter GetInstanceSelectVolumeAndChapter()
	 {
		 return this._instanceSelectVolumeAndChapter;
	 }
	 
	 public void SetInstanceSelectVolumeAndChapter(SelectVolumeAndChapter instance)
	 {
		 this._instanceSelectVolumeAndChapter = instance;
	 }
	 
	 public void SetInstanceMain_Tab_AD(Main_Tab_AD instance)
	 {
		 _instanceMain_Tab_AD = instance;
	 }
	 
	 public Main_Tab_AD GetInstanceMain_Tab_AD()
	 {
		 return this._instanceMain_Tab_AD;
	 }
	 
	 public Main_Tab_Readbible GetInstanceMain_Tab_Readbible()
	 {
		 return this._instanceMain_Tab_Readbible;
	 }
	 
	 public void SetInstanceMain_Tab_Readbible(Main_Tab_Readbible instance)
	 {
		 this._instanceMain_Tab_Readbible = instance;
	 }
	 
	 public ListView GetListViewBibleContent()
	 {
		 return this._ListViewBibleContent;
	 }
	 
	 public void SetListViewBibleContent(ListView listview)
	 {
		 this._ListViewBibleContent = listview;
	 }


	public String GetStrDatabasePathForDeployment()
	{ 
		return this._DatabasePathForDeployment;
	}

	public void SetStrDatabasePathForDeployment(String str)
	{
		this._DatabasePathForDeployment = str;
	}
	 
    public String GetStrDatabaseNameForDeployment()
    {
    	return this._DatabaseNameForDeployment;
    }
    
    public void SetStrDatabaseNameForDeployment(String str)
    {
    	this._DatabaseNameForDeployment = str;
    }

    public String[] getStrsBibleContent(){
        return _strsBibleContent;
    }
    
    public void setStrsBibleContent(String[] strs){
        this._strsBibleContent = strs;
    }

    public int getVerseCount()
    {
    	return _iVerseCount;
    }
    
    public void setVerseCount(int i)
    {
    	this._iVerseCount = i;
    }
    
    public int getVolumeID()
    {
    	return _iVolumeID;
    }
    
    public void setVolumeID(int i)
    {
    	this._iVolumeID = i;
    }
    
    
    public int getChapterID()
    {
    	return _iChapterID;
    }
    
    public void setChapterID(int i)
    {
    	this._iChapterID = i;
    }
    
    public int getVerseID()
    {
    	return _iVerseID;
    }
    
    public void setVerseID(int i)
    {
    	this._iVerseID = i;
    }
    
    public String GetStrVolumeName()
    {
    	//如果没有书卷名，则根据书卷ID，从数据库中获取
    	if(this._strVolumeName.length()<=0)
    	{
    		this._strVolumeName = this.GetVolumeNameByID(this.getVolumeID());
    	}
    	
    	return this._strVolumeName;
    }
    
    public void SetStrVolumeName(String str)
    {
    	this._strVolumeName = str;
    }


    //由书卷ID得到书卷名字
    public String GetVolumeNameByID(int volumeID)
    {
    	return _strsVolumeName[volumeID-1];
    }
    
    
    //将最近阅读书卷和章写入数据库
    public void WriteRecentlyReadingToDatabase()
    {
    	//selectBible=最后选择的卷-章（1-2）1代表卷的ID，2代表章的ID，-代表分割
    	String selectBible = this.getVolumeID() + "-" + this.getChapterID();
    	//数据库实例
		DatabaseHelper dbHelper = new DatabaseHelper(Config.this, this.GetStrDatabaseNameForDeployment());
	
		// 得到一个SQLiteDatabase对象  
		SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
		String sql = "update [settings] set  [selectBible]='" + selectBible + "'  where [SN]=0";
	
		sqliteDatabase.execSQL(sql);
    	
    }
    
    
  //------------------------------------------------
	
    /**
     * 判断数据库是否存在
     * @return false or true
     */
    public boolean checkDataBase(){
    	SQLiteDatabase checkDB = null;
    	try{
    		String databaseFilename = this.GetStrDatabasePathForDeployment() + this.GetStrDatabaseNameForDeployment();
    		checkDB =SQLiteDatabase.openDatabase(databaseFilename, null,
    				SQLiteDatabase.OPEN_READONLY);
    	}catch(SQLiteException e){
    		
    	}
    	if(checkDB!=null){
    		checkDB.close();
    	}
    	return checkDB !=null?true:false;
    }
    /**
     * 复制数据库到手机指定文件夹下
     * @throws IOException
     */
    public void copyDataBase() throws IOException{
    	String databaseFilenames =this.GetStrDatabasePathForDeployment()+this.GetStrDatabaseNameForDeployment();
    	File dir = new File(this.GetStrDatabasePathForDeployment());
    	if(!dir.exists())//判断文件夹是否存在，不存在就新建一个
    		dir.mkdir();
    	FileOutputStream os = null;
    	try{
    		os = new FileOutputStream(databaseFilenames);//得到数据库文件的写入流
    	}catch(FileNotFoundException e){
    		e.printStackTrace();
    	}
    	InputStream is = Config.this.getResources().openRawResource(R.raw.bible_unv);//得到数据库文件的数据流
        byte[] buffer = new byte[8192];
        int count = 0;
        try{
        	while((count=is.read(buffer))>0){
        		os.write(buffer, 0, count);
        		os.flush();
        	}
        }catch(IOException e){
        	
        }
        try{
        	is.close();
        	os.close();
        }catch(IOException e){
        	e.printStackTrace();
        }
    }

    
    //拷贝MP3文件
    public void copyMP3File() throws IOException{
    	
    	//01-1.mp3
    	int originalFileID = R.raw.voice_file;
    	
    	//拷贝mp3的目标目录
    	String destinationFilePath = FileUtil.setMkdir(Config.this, GetVoiceFileStorageFolder()) + File.separator + "01-1.mp3";
    	
    	//判断是否存在
    	File fileObj = new File(destinationFilePath);
    	if(!fileObj.exists())
    	{
	    	FileOutputStream os = null;
	    	try{
	    		os = new FileOutputStream(destinationFilePath);//得到MP3文件的写入流
	    	}catch(FileNotFoundException e){
	    		e.printStackTrace();
	    	}
	    	InputStream is = Config.this.getResources().openRawResource(originalFileID);//得到数据库文件的数据流
	        byte[] buffer = new byte[8192];
	        int count = 0;
	        try{
	        	while((count=is.read(buffer))>0){
	        		os.write(buffer, 0, count);
	        		os.flush();
	        	}
	        }catch(IOException e){
	        	
	        }
	        try{
	        	is.close();
	        	os.close();
	        }catch(IOException e){
	        	e.printStackTrace();
	        }
    	}
    }
    
}