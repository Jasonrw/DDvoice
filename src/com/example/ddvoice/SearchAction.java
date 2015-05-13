package com.example.ddvoice;

import android.content.Intent;
import android.net.Uri;



public class SearchAction {
	 MainActivity mActivity;
	 String mKeyword;
	 //String searchEngine;
	
	 public  SearchAction(String name,MainActivity activity)
	  {
		mKeyword = name;
	    mActivity=activity;
	   // searchEngine=engine;
	  }
	 
	 public void Search(){		 
		startWebSearch();	
	 }
	
	 private void startWebSearch()
	  {
		 Intent intent = new Intent();
		//if(searchEngine.contains("�ٶ�")){
			intent.setAction(Intent.ACTION_VIEW);	
			intent.setData(Uri.parse("http://m.baidu.com/s?word="+mKeyword)); 
			intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");//����Ϊϵͳ�Դ����������
		//}else{
		//intent.setAction("android.intent.action.WEB_SEARCH");	
	  //  intent.putExtra("query", mKeyword);	 
		//}
	    mActivity.startActivity(intent);
	  }
}
