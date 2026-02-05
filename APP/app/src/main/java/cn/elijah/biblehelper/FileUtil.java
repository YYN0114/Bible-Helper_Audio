package cn.elijah.biblehelper;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.Build;


public class FileUtil {

	
	//检查是否安装了sd卡
	public static boolean checkSDCard()
	{
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			return true;
		}else{
			return false;
		}
	}

	//创建目录
	public static String setMkdir(Context context, String strDownloadFolder)
	{
		String filePath;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// Android 10+ 使用应用私有存储
			filePath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + strDownloadFolder;
		} else if(checkSDCard()) {
			// Android 9及以下使用外部存储
			filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + strDownloadFolder;
		} else {
			// 无SD卡时使用缓存目录
			filePath = context.getCacheDir().getAbsolutePath() + File.separator + strDownloadFolder;
		}
		File file = new File(filePath);
		if(!file.exists())
		{
			file.mkdirs();
		}
		return filePath;
	}
}