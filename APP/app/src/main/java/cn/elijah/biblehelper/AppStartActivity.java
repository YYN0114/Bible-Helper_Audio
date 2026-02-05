
package cn.elijah.biblehelper;

import cn.elijah.biblehelper.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

public class AppStartActivity extends Activity {
    /** Called when the activity is first created. */

    private static final int REQUEST_PERMISSIONS = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.main);

		// 检查并请求权限
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkPermissions();
		} else {
			// Android 6.0以下，直接启动
			startMainActivity();
		}
	}

	private void checkPermissions() {
		String[] permissions = {
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
				android.Manifest.permission.READ_PHONE_STATE
		};

		boolean needRequest = false;
		for (String permission : permissions) {
			if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				needRequest = true;
				break;
			}
		}

		if (needRequest) {
			requestPermissions(permissions, REQUEST_PERMISSIONS);
		} else {
			startMainActivity();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == REQUEST_PERMISSIONS) {
			boolean allGranted = true;
			for (int result : grantResults) {
				if (result != PackageManager.PERMISSION_GRANTED) {
					allGranted = false;
					break;
				}
			}

			if (allGranted) {
				startMainActivity();
			} else {
				Toast.makeText(this, "需要必要权限才能运行应用", Toast.LENGTH_SHORT).show();
				// 即使权限被拒绝，也继续启动应用，某些功能可能不可用
				startMainActivity();
			}
		}
	}

	private void startMainActivity() {
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				Intent intent = new Intent (AppStartActivity.this,MainActivity.class);				
				startActivity(intent);				
				AppStartActivity.this.finish();
			}
		}, 1000);
	}
    
    //ÍË³ö°´¼ü
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

	    return super.dispatchKeyEvent(event);
	}
	
}
