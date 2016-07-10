/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tgm.serve;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;


import com.tgm.AESCrypt;
import com.tgm.LoggingIn;
import com.tgm.PerformingMessaging;
import com.tgm.R;
import com.tgm.comm.Socketer;
import com.tgm.interfacer.Manager;
import com.tgm.interfacer.SocketerInterface;
import com.tgm.interfacer.Updater;
import com.tgm.toolBox.ControllerOfFriend;
import com.tgm.toolBox.StorageManipulater;
import com.tgm.toolBox.MessageController;
import com.tgm.toolBox.HandlerXML;
import com.tgm.typo.InfoOfFriend;
import com.tgm.typo.InfoOfMessage;


import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;





/**
 * Это пример реализации приложения, которое работает локально
 * в том же процессе, что и приложение.
 */
public class MessagingService extends Service implements Manager, Updater {
//	private NotificationManager mNM;
	
	public static String USERNAME;
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String MESSAGE_LIST_UPDATED = "Take Message List";
	public ConnectivityManager conManager = null; 
	private final int UPDATE_TIME_PERIOD = 15000;

	private String rawFriendList = new String();
	private String rawMessageList = new String();

	SocketerInterface socketOperator = new Socketer(this);

	private final IBinder mBinder = new IMBinder();
	private String username;
	private String password;
	private boolean authenticatedUser = false;
	 // таймер обновления данных от сервера
	private Timer timer;






    public String encryptMes(String mes,String s1,String s2) throws NoSuchAlgorithmException {
        Log.i("ENCR  ", mes+s1+s2);
		ArrayList <String> list = new ArrayList<String>();
		list.add(s1);
		list.add(s2);
		Collections.sort(list);

		s1 = list.get(0) + list.get(1);
        list.clear();

		try {
			String encryptedMsg = AESCrypt.encrypt(s1, mes);
			//Log.i(" encryption: ", encryptedMsg);
			return encryptedMsg;
		}catch (GeneralSecurityException e){
			return null;
			//handle error - could be due to incorrect password or tampered encryptedMsg
		}



//        Log.i("S1!!!!!!!!!!!!!!!!!!!  ", s1);
//        try {
//            String crypted = AES.cipher(s1, mes);
//            Log.i("ENC!!!!!!!!!!!!!  ", crypted);
//            return crypted;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }

//        String iv = "fedcba9876543210";
//        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
//        SecretKeySpec skeySpec = new SecretKeySpec(AESCrypt.md5(s1).getBytes(), "AES");
//





//
//
//		crypto.setSeed(s1);
//        String temp = crypto.encrypt(mes);
//        Log.i("S1  ",temp );
//        crypto.setSeed(s1);
//        temp = crypto.decrypt(temp);
//        Log.i("S1  ",temp );
//
//
//
//
//		return crypto.encrypt(mes);
	}

    public String decryptMes(String mes,String s1,String s2) throws NoSuchAlgorithmException {
        Log.i("DECR  ", mes+s1+s2);
		ArrayList <String> list1 = new ArrayList<String>();
		list1.add(s1);
		list1.add(s2);
		Collections.sort(list1);

        s1 = list1.get(0) + list1.get(1);
        list1.clear();




//        Log.i("S1!!!!!!!!!!!!!!!!!!!  ", s1);
//        try {
//            String crypted = AES.decipher(s1, mes);
//            Log.i("DEC!!!!!!!!!!!!!  ", crypted);
//            return crypted;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }


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



//		Log.i("S1  ", s1);
//		crypto.setSeed(s1);
//        Log.i("S1  ", crypto.decrypt(mes));
//		return crypto.decrypt(mes);
	}

	@Override
	public void setIP(String ip) {
		this.socketOperator.setHost(ip);
	}

	@Override
	public String getIP() {
	 return	this.socketOperator.getHost();
	}


	private StorageManipulater localstoragehandler;
	
	private NotificationManager mNM;

	public class IMBinder extends Binder {
		public Manager getService() {
			return MessagingService.this;
		}
		
	}
	   
    @Override
    public void onCreate() 
    {   	
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         localstoragehandler = new StorageManipulater(this);
        // показывает уведомление о начале
    	conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	new StorageManipulater(this);

		timer = new Timer();   
		
		Thread thread = new Thread()
		{
			@Override
			public void run() {			
				
				
				Random random = new Random();
				int tryCount = 0;
				while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
				{		
					tryCount++; 
					if (tryCount > 10)
					{
						// если невозможно слушать порт каждые 10 сек
						break;
					}
					
				}
			}
		};		
		thread.start();
    
    }

/*
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.local_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
*/	

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}


	/**
	 * Show a notification while this service is running.
	 * @param msg 
	 **/

    private void showNotification(String username, String msg, String sendt ) throws NoSuchAlgorithmException, UnsupportedEncodingException {

    	String title = "Новое сообщение (" + username + ")";
		msg = decryptMes(msg,username,MessagingService.USERNAME);
 				
    	String text =  ((msg.length() < 20) ? msg : msg.substring(0, 20)+ "...");
		Context context = getApplicationContext();
		Resources res = context.getResources();

		Intent i = new Intent(context, PerformingMessaging.class);
		i.putExtra(InfoOfFriend.USERNAME, username);
		i.putExtra(InfoOfMessage.MESSAGETEXT, msg);
		i.putExtra(InfoOfMessage.SENDT, sendt);


		this.getMessageList();

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				i, PendingIntent.FLAG_CANCEL_CURRENT);



    	Notification.Builder mBuilder = new Notification.Builder(context);
		mBuilder.setContentIntent(contentIntent)
    	.setSmallIcon(R.drawable.icon)
				.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_account_circle_white_48dp))
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
    	.setContentTitle(title)
    	.setContentText(text);

		Notification notification = mBuilder.build();
        notification.defaults = Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS;
        Uri ringURI =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.sound = ringURI;

		mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.notify(0, notification);




        
        // запуск активити при выборе уведомления



        // msg.length()>15 ? MSG : msg.substring(0, 15);
//        mBuilder.setContentIntent(contentIntent);
//        mBuilder.setContentText("Новое сообщение от " + username + ": " + msg);
//
//        mNM.notify((username+msg).hashCode(), mBuilder.build());
    }
	 

	public String getUsername() {
		return this.username;
	}

	
	public String sendMessage(String  username, String  tousername, String message) throws UnsupportedEncodingException 
	{			
		String params = "username="+ URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&to=" + URLEncoder.encode(tousername,"UTF-8") +
						"&message="+ URLEncoder.encode(message,"UTF-8") +
						"&action="  + URLEncoder.encode("sendMessage","UTF-8")+
						"&";		

		return socketOperator.sendHttpRequest(params);		
	}

	
	private String getFriendList() throws UnsupportedEncodingException 	{		
		// после авторизации сервер запрашивает xml friendlist
		
		 rawFriendList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));


		 if (rawFriendList != null) {
			 this.parseFriendInfo(rawFriendList);
		 }
		 return rawFriendList;
	}
	
	private String getMessageList() throws UnsupportedEncodingException 	{

		 rawMessageList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawMessageList != null) {
			 this.parseMessageInfo(rawMessageList);
		 }
		 return rawMessageList;
	}
	
	

	/**
	 * если авторизация успеш возвращает список друзей
	 * если нет возврщает 0
	 * */
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{
		this.username = usernameText;
		this.password = passwordText;	
		
		this.authenticatedUser = false;
		
		String result = this.getFriendList(); //socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		if (result != null && !result.equals(LoggingIn.AUTHENTICATION_FAILED)) 
		{
			this.authenticatedUser = true;
			rawFriendList = result;
			USERNAME = this.username;
			Intent i = new Intent(FRIEND_LIST_UPDATED);					
			i.putExtra(InfoOfFriend.FRIEND_LIST, rawFriendList);
			sendBroadcast(i);
			
			timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					try {					
						//rawFriendList = IMService.this.getFriendList();
						// sending friend list 
						Intent i = new Intent(FRIEND_LIST_UPDATED);
						Intent i2 = new Intent(MESSAGE_LIST_UPDATED);
						String tmp = MessagingService.this.getFriendList();
						String tmp2 = MessagingService.this.getMessageList();
						if (tmp != null) {
							i.putExtra(InfoOfFriend.FRIEND_LIST, tmp);
							sendBroadcast(i);	
							Log.i("friend list broadcast sent ", "");
						
						if (tmp2 != null) {
							i2.putExtra(InfoOfMessage.MESSAGE_LIST, tmp2);
							sendBroadcast(i2);	
							Log.i("friend list broadcast sent ", "");
						}
						}
						else {
							Log.i("friend list returned null", "");
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}			
			}, UPDATE_TIME_PERIOD, UPDATE_TIME_PERIOD);
		}
		
		return result;		
	}

	public void messageReceived(String username, String message,String sendt)
	{				
		
		//FriendInfo friend = FriendController.getFriendInfo(username);
		InfoOfMessage msg = MessageController.checkMessage(username);
		if ( msg != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
		
			i.putExtra(InfoOfMessage.USERID, msg.userid);			
			i.putExtra(InfoOfMessage.MESSAGETEXT, msg.messagetext);
			i.putExtra(InfoOfMessage.SENDT, msg.sendt);
			sendBroadcast(i);
			String activeFriend = ControllerOfFriend.getActiveFriend();
			if (activeFriend == null || activeFriend.equals(username) == false) 
			{
				Log.e("TAKE_MESSAGE ", "");
				localstoragehandler.insert(username,this.getUsername(),
                            message.toString());


				try {
					showNotification(username, message, sendt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			Log.e("TAKE_MESSAGE broadcast sent by im service", "");
		}	
		
	}  
	
	private String getAuthenticateUserParams(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{			
		String params = "username=" + URLEncoder.encode(usernameText,"UTF-8") +
						"&password="+ URLEncoder.encode(passwordText,"UTF-8") +
						"&action="  + URLEncoder.encode("authenticateUser","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&";
		Log.i("PARAMS", params);
		return params;		
	}

	public void setUserKey(String value) 
	{		
	}

	public boolean isNetworkConnected() {
		return conManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isUserAuthenticated(){
		return authenticatedUser;
	}
	
	public String getLastRawFriendList() {		
		return this.rawFriendList;
	}
	
	@Override
	public void onDestroy() {
		Log.i("IMService is being destroyed", "...");
		super.onDestroy();
	}
	
	public void exit() 
	{
		timer.cancel();
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}
	
	public String signUpUser(String usernameText, String passwordText,
			String emailText) 
	{
		String params = "username=" + usernameText +
						"&password=" + passwordText +
						"&action=" + "signUpUser"+
						"&email=" + emailText+
						"&";
		
		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
	}

	public String addNewFriendRequest(String friendUsername) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +
		"&action=" + "addNewFriend" +
		"&friendUserName=" + friendUsername +
		"&";

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
	}

	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +
		"&action=" + "responseOfFriendReqs"+
		"&approvedFriends=" + approvedFriendNames +
		"&discardedFriends=" +discardedFriendNames +
		"&";

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
		
	} 
	
	private void parseFriendInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new HandlerXML(MessagingService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	private void parseMessageInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new HandlerXML(MessagingService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}

	public void updateData(InfoOfMessage[] messages,InfoOfFriend[] friends,
			InfoOfFriend[] unApprovedFriends, String userKey) 
	{
		this.setUserKey(userKey);
		//FriendController.	
		MessageController.setMessagesInfo(messages);
		//Log.i("MESSAGEIMSERVICE","messages.length="+messages.length);
		
		int i = 0;
		while (i < messages.length){
			messageReceived(messages[i].userid,messages[i].messagetext,messages[i].sendt);
			//appManager.messageReceived(messages[i].userid,messages[i].messagetext);
			i++;
		}
		
		
		ControllerOfFriend.setFriendsInfo(friends);
		ControllerOfFriend.setUnapprovedFriendsInfo(unApprovedFriends);
		
	}


	
	
	
	
}