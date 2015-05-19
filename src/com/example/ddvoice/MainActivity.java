package com.example.ddvoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.ddvoice.util.SystemUiHider;




import com.iflytek.cloud.*;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.example.ddvoice.JsonParser;









import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
//import android.speech.SpeechRecognizer;//�������
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;



/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements OnItemClickListener ,OnClickListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
	
	//����
	
	
	
	public static boolean serviceFlag=false;//��ʾ�Ƿ���һ������� 
	private int mainServiceID=0;//��ʾĿǰ�Ի���������������һ����������
	private int branchServiceID=0;//��ʾĿǰ�Ի��������������������е���һ���֧������
	
	public static JSONObject semantic = null,slots =null,answer=null;public static String operation = null,service=null;
	public static String receiver=null, name = null,price=null,code=null,song = null,keywords=null,content=null,
			url=null,text=null;
	
	
	
	private TextUnderstander mTextUnderstander;// �����������ı������壩��
	
	
	//from SiriCN
	private ProgressDialog mProgressDialog;//������ʾ��
	private MediaPlayer player;//��������
	
	private ListView mListView;
	private ArrayList<SiriListItem> list;
	ChatMsgViewAdapter mAdapter;



	public static  String SRResult="";	//ʶ����
	private static String SAResult="";//����ʶ����
	private static String TAG = MainActivity.class.getSimpleName();
	//Toast��ʾ��Ϣ
	private Toast info;
	//�ı�����
	private TextView textView;
	//����ʶ��
	private SpeechRecognizer mIat;
	// ������дUI
	private RecognizerDialog mIatDialog;
	// ��HashMap�洢��д���
		private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// ��������
		private String mEngineType = SpeechConstant.TYPE_CLOUD;
		private SharedPreferences mSharedPreferences;
		
		
	//����ʶ�������
		private RecognizerListener recognizerListener = new RecognizerListener() { 
			public void onBeginOfSpeech() {
				//info.makeText(getApplicationContext(), "��ʼ˵��", 100).show();
			}	 
			public void onError(SpeechError error) {
				// Tips��
				// �����룺10118(��û��˵��)��������¼����Ȩ�ޱ�������Ҫ��ʾ�û���Ӧ�õ�¼��Ȩ�ޡ�
				// ���ʹ�ñ��ع��ܣ�����+����Ҫ��ʾ�û���������+��¼��Ȩ�ޡ�
				//info.makeText(getApplicationContext(), error.getPlainDescription(true), 1000).show();
				showTip(error.getPlainDescription(true));
				speak("û��������˵����",false);
				//startSpeenchRecognition();
			} 
			public void onEndOfSpeech() {
				//info.makeText(getApplicationContext(), "����˵��", 100).show();
				showTip("����˵��");
			} 
			public void onResult(RecognizerResult results, boolean isLast) {
				//Log.d("dd", results.getResultString());
				printResult(results,isLast);

				if (isLast) {
					// TODO ���Ľ��
				}
			} 
			public void onVolumeChanged(int volume) {
				showTip("�����mic˵������ǰ������С��" + volume);
				//info.makeText(getApplicationContext(), "��ǰ����˵����������С��" + volume, 100).show();
			} 
			public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			}
		};



	
	/**
	 * ��ʼ����������
	 */
	private InitListener mInitListener = new InitListener() {
	
		 
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("��ʼ��ʧ�ܣ������룺" + code);
			}
		}
	};

	//��ʼ�����������ı������壩��
    private InitListener textUnderstanderListener = new InitListener() {
		public void onInit(int code) {
			Log.d(TAG, "textUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		//showTip("��ʼ��ʧ��,�����룺"+code);
				Log.d("dd","��ʼ��ʧ��,�����룺"+code);
        	}
		}
    };
	
	
	//private SemanticAnalysis semanticAnalysis;//�������ʵ��
	
	
  
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		info=Toast.makeText(this, "", Toast.LENGTH_SHORT);
			/*mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("���ڳ�ʼ�������Ժ򡭡� ^_^");
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();*/
			showTip("��ʼ����...");
			//info.makeText(getApplicationContext(), "��ʼ����...", 5).show();
	       // showTip("hai");
			initIflytek();
			initUI();
			speechRecognition();
			//mProgressDialog.dismiss();
			showTip("��ʼ�����");
			//info.makeText(getApplicationContext(), "��ʼ�����", 5).show();
			player = MediaPlayer.create(MainActivity.this, R.raw.lock);
			player.start();
			speak("��ã�����СD�����������������֡�", false);
			//runOnUiThread(new Runnable() {
				//@Override
				//public void run() {
					// TODO Auto-generated method stub
					//xiaoDReaction();//�����Ի�����ϵͳ
				//}
			//});
			
		/*	new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(true)
					xiaoDReaction();
				}});*/
    }
  

    public void xiaoDReaction() {
    
			/*	JSONObject semantic = null,slots =null;String operation = null,service=null;
				String name = null,code=null,song = null,keywords=null,content=null;*/
			
				try {
					JSONObject SAResultJson = new JSONObject(SAResult);
					operation=SAResultJson.optString("operation");
					service=SAResultJson.optString("service");
					semantic=SAResultJson.optJSONObject("semantic");
					answer=SAResultJson.optJSONObject("answer");
					text=answer.optString("text");
					slots=semantic.optJSONObject("slots");
					receiver=slots.optString("receiver");
					name = slots.optString("name");
					price= slots.optString("price");
					code = slots.optString("code");
					song = slots.optString("song");
					keywords=slots.optString("keywords");
					content=slots.optString("content");
					url=slots.optString("url");
				
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//speak("��ȡ����������",false);
					e.printStackTrace();
				}
				SRResult=null;//�ÿ�
				SAResult=null;
				speak("service:"+service+" operation:"+operation,false);
			if(serviceFlag==false){//�������һ������вŽ��з�����ж�
				//speak("�жϷ�������",false);
				switch(service){
				
				
				case "telephone":{//�绰��ط���
					
					switch(operation){
					
						case "CALL":{	//��绰
										//��Ҫ�������绰����code��
										//��ѡ����������name��������category�������������location������Ӫ��operator�����Ŷ�head_num����β��tail_num��
										//���ɶ����ѡ����ȷ����Ҫ����
							//speak("name:"+name+"code:"+code,false);
							CallAction callAction=new CallAction(name,code,this);//Ŀǰ�ɸ������ֻ�绰���벦��绰
							callAction.start();
							break;
						}
						
						case "VIEW":{	//�鿴�绰�����¼
										//��Ҫ������
										//��ѡ������δ�ӵ绰�����Ѳ��绰�����ѽӵ绰��
							CallView callview =new CallView(this);
							callview.start();
							break;
						}
						
						default :break;
					
					}
					
					break;
				}
				
				case "message":{
					
					switch(operation){
					
						case "SEND":{//���Ͷ���
						
							SendMessage sendMessage = new SendMessage(name,code,content,MainActivity.this);
							sendMessage.start();
							break;
						}
						
						case "VIEW":{//�鿴���Ͷ���ҳ��
						
							MessageView messageView=new MessageView(this);
							messageView.start();
							break;
						}
						
						
						
						case "SENDCONTACTS":{//������Ƭ,Ŀǰֻ��ʶ�����ַ�������
							SendContacts sendContacts = new SendContacts(name,receiver,this);
							sendContacts.start();
							break;
						}
						default :break;
					}
			
					break;
				}
				
				case "app":{
					
					switch(operation){
						
						case "LAUNCH":{//��Ӧ��
							OpenAppAction openApp = new OpenAppAction(name,MainActivity.this);
							openApp.start();
							break;
						}
						
						case "QUERY":{//Ӧ����������Ӧ��
							SearchApp searchApp = new SearchApp(price,name,this);
							searchApp.start();
							break;
						}
						
						default:break;
					
					}
					break;
				}
				
				case "website":{
					
					switch(operation){
					
						case "OPEN":{//��ָ����ַ
						
							OpenWebsite openWebsite=new OpenWebsite(url,name,this);
							openWebsite.start();
							break;
						}
					
						default:break;
					}
					
					break;
				}
				
				case "websearch":{
					
					switch(operation){
					
						case "QUERY":{//����
							
							SearchAction searchAction =new SearchAction(keywords,MainActivity.this);
							searchAction.Search();
							break;
						}
						
						default:break;
					
					}
							
					
					break;
				}
				
				case "openQA":{
					
					switch(operation){
					
					case "ANSWER":{//�����ʴ�
						
						OpenQA openQA = new OpenQA(text,this);
						openQA.start();
						
						break;
					}
					
					default:break;
					}
					
					break;
				}
				
				default:{
					speak("��֪����Ҫ������������һ��ʱ���Ҿͻᶮ�ˡ�",false);
					break;
				}
			}
	}//����ĳ����������
}
				
    	//});
			
			
			
	
		/*	if(operation.equals("LAUNCH")){//��Ӧ��
				speak("�õģ�Ϊ������"+name+"...",false);
				OpenAppAction openApp = new OpenAppAction(name,MainActivity.this);
				openApp.runApp();
			}
			if(operation.equals("PLAY")){//�������ֻ���Ƶ
				speak("����֪����ô��...",false);
				if(service.equals("music")){
					PlayAction playAction= new PlayAction(song,MainActivity.this);
					playAction.Play();
				}
				if(service.equals("video")){
					PlayAction playAction= new PlayAction(keywords,MainActivity.this);
					playAction.Play();
				}
			}
			if(operation.equals("QUERY")){//����
				speak("�õģ���������"+keywords+"...",false);
				SearchAction searchAction =new SearchAction(keywords,MainActivity.this);
				searchAction.Search();
			}*/
			
		
	//}
    
    public void initIflytek(){//��ʼѶ������
    	
    	//�ҵ�Siri����
    	findViewById(R.id.voice_input).setOnClickListener(MainActivity.this);
    	//�����û��������ö����ſ���ʹ���������񣬽����ڳ�����ڴ����á�����appid��Ҫ�Լ�ȥ�ƴ�Ѷ����վ���룬����ʹ��Ĭ�ϵĽ�����ҵ��;��
    	SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=553f8bf7");
    
    }
    
    public void initUI(){//��ʼ��UI�Ͳ���
    	SRResult="";
    	list = new ArrayList<SiriListItem>();
		mAdapter = new ChatMsgViewAdapter(this, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setFastScrollEnabled(true);
		registerForContextMenu(mListView);
    }
    
    public void speechRecognition(){//����ʶ���ʼ��
    
    	//1.����SpeechRecognizer���󣬵ڶ��������� ������дʱ��InitListener
    	mIat= SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
    	// ��ʼ����дDialog�����ֻʹ����UI��д���ܣ����贴��SpeechRecognizer
    	mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
    	
    }
    
    public void startSpeenchRecognition(){//����ʶ��
    	player = MediaPlayer.create(MainActivity.this, R.raw.begin);
		player.start();
    	// ��ʾ��д�Ի���
    	mIatDialog.setListener(recognizerDialogListener);
		//mIatDialog.show();
		ret = mIat.startListening(recognizerListener);
		if (ret != ErrorCode.SUCCESS) {
			Log.d(TAG, ""+ret);
			showTip("��дʧ��,�����룺" + ret);
			//info.makeText(getApplicationContext(), "��дʧ��,�����룺" + ret, 100).show();
		}
		
    }
    
    //����ʶ����������
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			printResult(results,isLast);//�õ�ʶ���� 
		}

		/**
		 * ʶ��ص�����.
		 */
		public void onError(SpeechError error) {
			speak(error.getPlainDescription(true),true);
			info.makeText(getApplicationContext(), "error.getPlainDescription(true)", 1000).show();
			//showTip(error.getPlainDescription(true));
		}

	};
	
    
    
    //��ʼ����ʶ��
    private void startSemanticAnalysis(){
    	//semanticAnalysis=new SemanticAnalysis();
    	//SAResult=semanticAnalysis.getSAResult("����������");//��ʼ�������
    	//UnderstanderDemo testSA=new UnderstanderDemo();
    	//SAResult=testSA.startSA("����������");
    	
    	
    	
    	/*Intent SAActivity = new Intent(MainActivity.this,SemanticAnalysis.class);
    	SAActivity.putExtra("SRResult", SRResult);
    	Log.d("dd","ʶ������"+SRResult);
    	startActivityForResult(SAActivity,0 );*/
    
    	//onActivityResult(0, 0, SAActivity);
    	
    /*	Intent SAActivity = new Intent(MainActivity.this,SemanticAnalysis.class);
    	startActivity(SAActivity);
    	
    	SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
    	SAResult=semanticAnalysis.SAResult;
    	speak(SAResult, false);*/
    	
    	
    	// SRResult=MainActivity.SRResult;
 	
 		//Log.d("dd","SRResult:"+SRResult);
 		ret=0 ;
 		
 		mTextUnderstander = TextUnderstander.createTextUnderstander(MainActivity.this, textUnderstanderListener);
 		
 		startAnalysis();
    }
    
  //��ʼ�������
  	private void startAnalysis(){
  		
  		mTextUnderstander.setParameter(SpeechConstant.DOMAIN,  "iat");
  		if(mTextUnderstander.isUnderstanding()){
  			mTextUnderstander.cancel();
  			//showTip("ȡ��");
  			Log.d("dd","ȡ��");
  		}else {
  			ret = mTextUnderstander.understandText(SRResult, textListener);
  			if(ret != 0)
  			{
  				//showTip("�������ʧ��,������:"+ ret);
  				Log.d("dd","�������ʧ��,������:"+ ret);
  			}
  		}
  		/*ret = mTextUnderstander.understandText(SRResult, textListener);
  		if(ret != 0)
  		{
  			showTip("�������ʧ��,������:"+ ret);
  			
  		}*/
  	}
  	 //ʶ��ص�
      private TextUnderstanderListener textListener = new TextUnderstanderListener() {
  		
  		public void onResult(final UnderstanderResult result) {
  	       	runOnUiThread(new Runnable() {
  					
  					public void run() {
  						if (null != result) {
  			            	// ��ʾ
  							//Log.d(TAG, "understander result��" + result.getResultString());
  							String text = result.getResultString();
  							SAResult=text;
  							//Log.d("dd","SAResult:"+SAResult);
  							if (TextUtils.isEmpty(text)) {
  								//Log.d("dd", "understander result:null");
  								//showTip("ʶ��������ȷ��");
  							}
  							//mainActivity.speak();
  							speak(SAResult,false);
  							xiaoDReaction();
  							//finish();
  			            } 
  					}

					

					/*private void dialogueManagement(int mainServiceID,int branchServiceID) {//�Ի�������
						// TODO Auto-generated method stub
						if(mainServiceID==1){
							if(branchServiceID==1){//�����˴�绰���񣬱�Ҫ�����ǡ��绰���롿,��ѡ�����С���������ء�����Ӫ�̡����ŶΡ���β�š���
								//���ɶ����ѡ����ȷ����Ҫ����
								
							}
							if(branchServiceID==2){//�����˲鿴�绰���ż�¼
								
							}
							
						}
						if(mainServiceID==2){//�����˷����ŷ��񣬱�Ҫ�����ǵ绰����Ͷ�������
							
						}
					}*/
  				});
  		}
  		
  		public void onError(SpeechError error) {
  			//showTip("onError Code��"	+ error.getErrorCode());
  			Log.d("dd","onError Code��"	+ error.getErrorCode());
  		}
  	};
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){//��дonActivityResult
        if(requestCode == 0){
            //System.out.println("REQUESTCODE equal");
            if(resultCode == 0){
                 //    System.out.println("RESULTCODE equal");
            	SAResult = data.getStringExtra("SRResult");
            }
        }
    }
    
   
    
    private void printResult(RecognizerResult results,boolean isLast) {
		String text = JsonParser.parseIatResult(results.getResultString());

		//Log.d("dd","text:"+text);
		String sn = null;
		// ��ȡjson����е�sn�ֶ�
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			Log.d("dd","json:"+results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}
		SRResult=resultBuffer.toString();
		if(isLast==true){
		speak(SRResult, true);
		startSemanticAnalysis();
		}
	}
    
    int ret = 0; // �������÷���ֵ
    
	@SuppressWarnings("static-access")
	@Override
	public void onClick(View view) {//����ʶ�����
		
		startSpeenchRecognition();
		
		
		
		
		
		//���´��벻��ֱ�ӷ��������Ȼ����������
		/*info.makeText(getApplicationContext(), "5", 1000).show();
		// TODO Auto-generated method stub
		if(view.getId()==R.id.voice_input){
			//3.��ʼ��д
			
			setParam();
			info.makeText(getApplicationContext(), "��ʼ��д", 1000).show();
				// ����ʾ��д�Ի���
				ret = mIat.startListening(recognizerListener);
				if (ret != ErrorCode.SUCCESS) {
					Log.d(TAG, ""+ret);
					//showTip("��дʧ��,�����룺" + ret);
				} else {
					//showTip("�ɹ�");
				}
			}*/

	}

	public void setParam(){
		// ��ղ���
				mIat.setParameter(SpeechConstant.PARAMS, null);

				// ������д����
				mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				// ���÷��ؽ����ʽ
				mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

				String lag = mSharedPreferences.getString("iat_language_preference",
						"mandarin");
				if (lag.equals("en_us")) {
					// ��������
					mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
				} else {
					// ��������
					mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
					// ������������
					mIat.setParameter(SpeechConstant.ACCENT, lag);
				}
				// ��������ǰ�˵�
				mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
				// ����������˵�
				mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
				// ���ñ�����
				mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
				// ������Ƶ����·��
				mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()
						+ "/iflytek/wavaudio.pcm");
				// ������д����Ƿ�����̬������Ϊ��1��������д�����ж�̬�����ط��ؽ��������ֻ����д����֮�󷵻����ս��
				// ע���ò�����ʱֻ��������д��Ч
				mIat.setParameter(SpeechConstant.ASR_DWA, mSharedPreferences.getString("iat_dwa_preference", "0"));
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	
	
    //from SiriCN
	public void speak(String msg, boolean isSiri) {//
		//info.makeText(getApplicationContext(), "here", 1000).show();
		addToList(msg, isSiri);//��ӵ��Ի��б�
		//if(isHasTTS)
		//mSiriEngine.SiriSpeak(msg);
	}
	
	private void addToList(String msg, boolean isSiri) {
		//
		list.add(new SiriListItem(msg, isSiri));
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(list.size() - 1);
	}
    
	public class SiriListItem {
		String message;
		boolean isSiri;

		public SiriListItem(String msg, boolean siri) {
			message = msg;
			isSiri = siri;
		}
	}
	
	private void showTip(final String str) {
		info.setText(str);
		info.show();
	}
	
	
}
