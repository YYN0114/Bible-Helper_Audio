package cn.elijah.biblehelper;

import cn.elijah.biblehelper.R;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapterLection extends BaseAdapter {
  
	Config _config;
	
		public MyAdapterLection(Context context) 
		{
			mContext = context;
			_config = (Config)mContext.getApplicationContext(); 
		}

		 @Override 
        public int getCount()
        {      
       	return _config.getVerseCount();
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
        public View getView(int position, View convertView, ViewGroup parent) { 
        // position就是位置从0开始，convertView是Spinner,ListView中每一项要显示的view 
    	// 通常return 的view也就是convertView 
    	// parent就是父窗体了，也就是Spinner,ListView,GridView了.
	
	    if (convertView == null) 
	    {
	   	 convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_lection, null);
	    }

	     StringBuilder sb = new StringBuilder();
	     sb.append("<b><font color= '#008200'>");
	     sb.append(String.valueOf(position + 1));
	     sb.append("</font></b>&nbsp;&nbsp;<font color = 'black'>");
	     sb.append(_config.getStrsBibleContent()[position]);
	     sb.append("</font>");
	
         TextView mTextView2 = (TextView) convertView.findViewById(R.id.ItemViewBibleContent);
         mTextView2.setText(Html.fromHtml(sb.toString()));
         mTextView2.setTextSize(_config.GetLectionFontSize());
    
         sb.delete(0, sb.length());
         sb = null;

         return convertView;

    } 
    private Context mContext;
}