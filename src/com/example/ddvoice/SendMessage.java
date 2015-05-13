package com.example.ddvoice;

import java.util.List;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

public class SendMessage {
	private String mPerson;
    private String number;
    private String mcontent;
    MainActivity mActivity;
    
    
    public SendMessage(String person,String content,MainActivity activity){
    	 mPerson = person;
    	 mcontent=content;
 	    mActivity=activity;
    }
    
    public void send(){
    	if ((mPerson == null) || (mPerson.equals("")))
	    {
	      
	    }else{
	    	 mPerson=mPerson.trim();
	    	 number=null; 
	    	 if(isPhoneNumber(mPerson)){
	    		 number=mPerson;
	    	 }
	    	 else
	    		 number=getNumberByName(mPerson,mActivity);
	    	 if(number == null)
	         {
	          // mPerson = null;
	           //VoiceFunction fun=new VoiceFunction(mActivity,null);
	          // fun.getVoiceResponse("��ѯ����  ��Ҫ���˭��","contact", handler);
	           mActivity.speak("�Ҳ���"+mPerson, false);
	         }else{	    
	        	 //������
	        	// mActivity.speak("��������"+mPerson+"...", false);
	        	 //Intent intent = new Intent(Intent.ACTION_SENDTO,Uri.parse("tel:" + number,"content:"+mcontent));        	         
	 	        //mActivity.startActivity(intent);
	    		 //new VoiceDialog(mActivity,"����Ҫ����"+mPerson+"\n����Ϊ"+number,rightFn,wrongFn,null);	   
	        	 Log.d("dd","����绰��"+number);
	        	 Log.d("dd","�������ݣ�"+mcontent);
	        	 SmsManager smsManager = SmsManager.getDefault();
	        	 if(mcontent.length() > 70) {
                     List<String> contents = smsManager.divideMessage(mcontent);
                     for(String sms : contents) {
                         smsManager.sendTextMessage(number, null, sms, null, null);
                         insertDB(number,sms);
                     }
                 } else {
                  smsManager.sendTextMessage(number, null, mcontent, null, null);
                  insertDB(number,mcontent);
                 }
	        	 
	        }
	    }
    }
    
    private void insertDB(String number,String content){//�����͵Ķ��Ų���ϵͳ���ݿ��У�ʹ���ڶ��Ž�����ʾ 
    	//////////////////////���׳�null���쳣---�ѽ��--- mActivity.getContentResolver()�ſ���
    	try{
	    	ContentValues values = new ContentValues();
	    	values.put("date", System.currentTimeMillis());
	    	 //�Ķ�״̬              
	        values.put("read", 0);             
	         //1Ϊ�� 2Ϊ��             
	       values.put("type", 2);           
	         //�ʹ����              
	      // values.put("status", -1);
	       values.put("address",number);             
	         //�ʹ�����            
	       values.put("body", content);             
	         //������ſ�    
	      // getContentResolver
	       mActivity.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	       mActivity.speak("���ŷ��ͳɹ�",false);
    	}catch (Exception e) { 
            Log.d("dd", "�������ݿ����⣺"+e.getMessage()); 
    	  }
    }
    
    /*private ContentResolver getContentResolver() {
		// TODO Auto-generated method stub
		return null;
	}*/

	private boolean isPhoneNumber(String num)
	{
		//return num.matches("\\d+$");
		return num.matches("^\\d+\\D?$");
	}
    
    private  String getNumberByName(String name, Context context)
	  {
		 Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, name);
		  ContentResolver  resolver  = context.getContentResolver();
		  Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts._ID}, null, null, null);  
		  if((cursor!=null)&&(cursor.moveToFirst())){
			  int idCoulmn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			  long id = cursor.getLong(idCoulmn);
		      cursor.close();
		      cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  new String[]{"data1"}, "contact_id = ?",  new String[]{Long.toString(id)}, null);
		      if ((cursor != null) && (cursor.moveToFirst()))
		      {
		        int m = cursor.getColumnIndex("data1");
		        String num = cursor.getString(m);
		        cursor.close();
		       return num;
		      }	      
		  }
		  return null;
	  }
}
