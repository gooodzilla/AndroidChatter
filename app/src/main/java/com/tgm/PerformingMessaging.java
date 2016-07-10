package com.tgm;


import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tgm.interfacer.Manager;
import com.tgm.serve.MessagingService;
import com.tgm.toolBox.ControllerOfFriend;
import com.tgm.toolBox.StorageManipulater;
import com.tgm.typo.InfoOfFriend;
import com.tgm.typo.InfoOfMessage;
import com.tgm.typo.InfoStatus;
import com.tgm.typo.Messages;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class PerformingMessaging extends AppCompatActivity {

	private Toolbar mActionBarToolbar;
	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	public String username;
	private EditText messageText;
	private EditText messageHistoryText;
	private FloatingActionButton sendMessageButton;
	private Manager imService = null;
	private InfoOfFriend friend = new InfoOfFriend();
	private StorageManipulater localstoragehandler;
	private Cursor dbCursor;

	private ArrayList<Messages> convList;
	private ChatAdapter adp;

	EmojiconEditText emojiconEditText;
	ImageView emojiButton;
	ImageView submitButton;
	EmojIconActions emojIcon;
	View rootView;
	TextView timeago;
	TextView status;
	private ListView list;
	String statusoffr = " (офлайн)";

	public String decryptMes(String mes,String s1,String s2) throws NoSuchAlgorithmException {
		Log.i("DECR  ", mes+s1+s2);
		ArrayList <String> list1 = new ArrayList<String>();
		list1.add(s1);
		list1.add(s2);
		Collections.sort(list1);

		s1 = list1.get(0) + list1.get(1);
		list1.clear();

		try {
			String messageAfterDecrypt = AESCrypt.decrypt(s1, mes);
			Log.i(" Descryption: ", messageAfterDecrypt);
			return messageAfterDecrypt;
		}catch (GeneralSecurityException e){
			Log.i(" Descryption: ", "ERROR");
			e.printStackTrace();
			return null;
			//handle error - could be due to incorrect password or tampered encryptedMsg
		}

	}


	private ServiceConnection mConnection = new ServiceConnection() {


		public void onServiceConnected(ComponentName className, IBinder service) {
			imService = ((MessagingService.IMBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			imService = null;
			Toast.makeText(PerformingMessaging.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};

	protected void updateToolbar(boolean stat)
	{
		if(stat == true) {
			mActionBarToolbar.setTitle(friend.userName) ;
			mActionBarToolbar.setSubtitle("онлайн");
		}
		else {
			mActionBarToolbar.setTitle(friend.userName) ;
			mActionBarToolbar.setSubtitle("офлайн");
		}

    //    Log.e("Keyboard",friend.status.toString());

		mActionBarToolbar.setLogo(R.drawable.ic_account_circle_white_48dp);//TODO get user avatar
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{


		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.chat);

		mActionBarToolbar = (Toolbar) findViewById(R.id.include);
		setSupportActionBar(mActionBarToolbar);



		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
		}



		rootView = findViewById(R.id.root_view);
		emojiButton = (ImageView) findViewById(R.id.emoji_btn);
		emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);

		emojIcon=new EmojIconActions(this,rootView,emojiconEditText,emojiButton);
		emojIcon.ShowEmojIcon();
		emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
			@Override
			public void onKeyboardOpen() {
				Log.e("Keyboard","open");
			}

			@Override
			public void onKeyboardClose() {
				Log.e("Keyboard","close");
			}
		});


		convList = new ArrayList<Messages>();


		list = (ListView) findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);



				
	//	messageHistoryText = (EditText) findViewById(R.id.messageHistory);
		
		messageText = (EditText) findViewById(R.id.emojicon_edit_text);
        messageText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		
		//messageText.requestFocus();
		
		sendMessageButton = (FloatingActionButton) findViewById(R.id.btnSend);
		
		Bundle extras = this.getIntent().getExtras();


		
		friend.userName = extras.getString(InfoOfFriend.USERNAME);
		friend.ip = extras.getString(InfoOfFriend.IP);
		friend.port = extras.getString(InfoOfFriend.PORT);

//		Log.w("un",  friend.userName);
//		Log.w("ip",  friend.ip);
//		Log.w("port",  friend.port);

		InfoOfFriend iof[] = ControllerOfFriend.getFriendsInfo();
		for (int i = 0; i < iof.length; i++) {
			if(iof[i].userName.equals(friend.userName))
			{
				if (iof[i].status == InfoStatus.ONLINE)
				updateToolbar(true);
				else updateToolbar(false);
			};
		}



		String msg = extras.getString(InfoOfMessage.MESSAGETEXT);
		String y = extras.getString(InfoOfMessage.SENDT);

//		Log.w("msg",  msg);
//		Log.w("y",  y);


		adp.notifyDataSetChanged();

		
	//	EditText friendUserName = (EditText) findViewById(R.id.friendUserName);
	//	friendUserName.setText(friend.userName);
		
		
		localstoragehandler = new StorageManipulater(this);
		dbCursor = localstoragehandler.get(friend.userName, MessagingService.USERNAME );
		String lastmsg = null;
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
		dbCursor.moveToFirst();

		    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
		    {
		        noOfScorer++;

				//this.appendToMessageHistory(dbCursor.getString(2), dbCursor.getString(3));
				Messages msgs = new Messages(dbCursor.getString(2),dbCursor.getString(3),dbCursor.getString(4));


				try {
					if(msgs.getSender().equals(MessagingService.USERNAME)){
						Log.w("msg",  msgs.getMsg());
					Log.w("2",  msgs.getSender());
					Log.w("3",  friend.userName);

						msgs.setMsg(decryptMes(msgs.getMsg(),msgs.getSender(),friend.userName));}
					else
					msgs.setMsg(decryptMes(msgs.getMsg(),msgs.getSender(),MessagingService.USERNAME));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				lastmsg = msgs.getMsg();
				convList.add(msgs);
		        dbCursor.moveToNext();
		    }
		}


		adp.notifyDataSetChanged();

		if (msg != null && !msg.equals(lastmsg))
		{

			//this.appendToMessageHistory(friend.userName , msg);

			Log.w("msg",  msg);
			Log.w("y",  y);
			Messages msgs = new Messages(friend.userName ,msg,y);
			Log.w("SECOND BLOCK!!!!!!",  msgs.getMsg());
			msgs.setMsg(msgs.getMsg());

			convList.add(msgs);
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName+msg).hashCode());
		}

        for (int i = 0; i < convList.size(); i++) {
            Log.w("send",  convList.get(i).getSender());
            Log.w("msg",  convList.get(i).getMsg());
        }

		adp.notifyDataSetChanged();

		sendMessageButton.setOnClickListener(new View.OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = messageText.getText();

				if (message.length()>0) 
				{		
					//appendToMessageHistory(imService.getUsername(), message.toString());
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime());
					Messages msgs = new Messages(imService.getUsername(), message.toString(),timeStamp );

					convList.add(msgs);

					try {
						localstoragehandler.insert(imService.getUsername(), friend.userName,
                                imService.encryptMes(message.toString(),imService.getUsername(),friend.userName));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}

					messageText.setText("");

					adp.notifyDataSetChanged();

					InfoOfFriend iof[] = ControllerOfFriend.getFriendsInfo();
					for (int i = 0; i < iof.length; i++) {
						if(iof[i].userName.equals(friend.userName))
						{
							if (iof[i].status == InfoStatus.ONLINE)
								updateToolbar(true);
							else updateToolbar(false);
						};
					}

					Thread thread = new Thread(){					
						public void run() {
							try {
								if (imService.sendMessage(imService.getUsername(), friend.userName,
										imService.encryptMes(message.toString(),imService.getUsername(),friend.userName)) == null)
								{
									
									handler.post(new Runnable(){	

										public void run() {

									        Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

											
											//showDialog(MESSAGE_CANNOT_BE_SENT);										
										}
										
									});
								}
							} catch (UnsupportedEncodingException e) {
								Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							}
						}						
					};
					thread.start();
										
				}
				
			}});
		
		messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
			
			
		});
				
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id)
		{
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.message_cannot_be_sent;
		break;
		}
		
		if (message == -1)
		{
			return null;
		}
		else
		{
			return new AlertDialog.Builder(PerformingMessaging.this)       
			.setMessage(message)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {


				}
			})        
			.create();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		
		ControllerOfFriend.setActiveFriend(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(PerformingMessaging.this, MessagingService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(MessagingService.TAKE_MESSAGE);

		
		registerReceiver(messageReceiver, i);
		
		ControllerOfFriend.setActiveFriend(friend.userName);		
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(InfoOfMessage.USERID);			
			String message = extra.getString(InfoOfMessage.MESSAGETEXT);
            String time = extra.getString(InfoOfMessage.SENDT);

			InfoOfFriend iof[] = ControllerOfFriend.getFriendsInfo();
			for (int i = 0; i < iof.length; i++) {
				if(iof[i].userName.equals(friend.userName))
				{
					if (iof[i].status == InfoStatus.ONLINE)
						updateToolbar(true);
					else updateToolbar(false);
				};
			}


            if (username != null && message != null)
			{
				if (friend.userName.equals(username)) {
					//appendToMessageHistory(username, message);

					Messages msgs = new Messages(username ,message,time);
					try {
						msgs.setMsg(decryptMes(msgs.getMsg(),msgs.getSender(), MessagingService.USERNAME));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					convList.add(msgs);
                    adp.notifyDataSetChanged();

						localstoragehandler.insert(username,imService.getUsername(),  message.toString());


				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(PerformingMessaging.this,  username + " отправил '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();


	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (localstoragehandler != null) {
	    	localstoragehandler.close();
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	}

	private class ChatAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			return convList.size();
		}

		@Override
		public Object getItem(int position) {
			return convList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {

			Messages msgs = (Messages) getItem(position);
            if (friend.userName.equals(msgs.getSender()))
            v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);
			else
                v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);

            TextView lbl = (TextView) v.findViewById(R.id.lbl1);


            lbl.setText(DateUtils.getRelativeDateTimeString(PerformingMessaging.this, msgs.getSendt()
                            .getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(msgs.getMsg());

//            lbl = (TextView) v.findViewById(R.id.lbl3);
//            lbl.setText("Отправлено");

			return v;
		}
	}




}
