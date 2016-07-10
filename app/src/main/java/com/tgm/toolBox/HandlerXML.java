package com.tgm.toolBox;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tgm.interfacer.Updater;
import com.tgm.typo.InfoOfFriend;
import com.tgm.typo.InfoOfMessage;
import com.tgm.typo.InfoStatus;

import android.util.Log;

/*
 * Из XML в список инфо о друзьях
 * Структура
 * <?xml version="1.0" encoding="UTF-8"?>
 * 
 * <friends>
 * 		<user key="..." />
 * 		<friend username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * 		<friend username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * </friends>
 *
 *
 *status == online || status == unApproved
 * */
//позволяет читать и обрабатывать XML
public class HandlerXML extends DefaultHandler
{
		private String userKey = new String();
		private Updater updater;

		//конструктор
		public HandlerXML(Updater updater) {
			super();
			this.updater = updater;
		}

        //вектора
		private Vector<InfoOfFriend> mFriends = new Vector<InfoOfFriend>();//друзей
		private Vector<InfoOfFriend> mOnlineFriends = new Vector<InfoOfFriend>();//онлайн
		private Vector<InfoOfFriend> mUnapprovedFriends = new Vector<InfoOfFriend>();//не принятые
		
		private Vector<InfoOfMessage> mUnreadMessages = new Vector<InfoOfMessage>();//не прочитанные

		
		public void endDocument() throws SAXException 
		{
			InfoOfFriend[] friends = new InfoOfFriend[mFriends.size() + mOnlineFriends.size()];//массив друзей
			InfoOfMessage[] messages = new InfoOfMessage[mUnreadMessages.size()];//массив сообщений
			
			int onlineFriendCount = mOnlineFriends.size();//получаем размер онлайн друзей
			for (int i = 0; i < onlineFriendCount; i++) 
			{				
				friends[i] = mOnlineFriends.get(i);//заносим их в массив
			}
			
						
			int offlineFriendCount = mFriends.size();			//получаем размер офлайн друзей
			for (int i = 0; i < offlineFriendCount; i++) 
			{
				friends[i + onlineFriendCount] = mFriends.get(i);// добавляем их в массив
			}
			
			int unApprovedFriendCount = mUnapprovedFriends.size();//получаем число непринятых
			InfoOfFriend[] unApprovedFriends = new InfoOfFriend[unApprovedFriendCount];//массив не принятых
			
			for (int i = 0; i < unApprovedFriends.length; i++) {
				unApprovedFriends[i] = mUnapprovedFriends.get(i);//добавляем в массив
			}
			
			int unreadMessagecount = mUnreadMessages.size();//получаем число не прочитанных сообщений
			//Log.i("MessageLOG", "mUnreadMessages="+unreadMessagecount );
			for (int i = 0; i < unreadMessagecount; i++) 
			{
				messages[i] = mUnreadMessages.get(i);//добавляем в массив
				Log.i("MessageLOG", "i="+i );
			}
			
			this.updater.updateData(messages, friends, unApprovedFriends, userKey);//обновляем
			super.endDocument();
		}		
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException 
		{				
			if (localName == "friend")
			{
				InfoOfFriend friend = new InfoOfFriend();//создается объект
				friend.userName = attributes.getValue(InfoOfFriend.USERNAME);//объект получает имя
				String status = attributes.getValue(InfoOfFriend.STATUS);//статус
				friend.ip = attributes.getValue(InfoOfFriend.IP);//ip
				friend.port = attributes.getValue(InfoOfFriend.PORT);//порт
				friend.userKey = attributes.getValue(InfoOfFriend.USER_KEY);//ключ
				//friend.expire = attributes.getValue("expire");
				
				if (status != null && status.equals("online"))//если онлайн
				{					
					friend.status = InfoStatus.ONLINE;//присваивается статус онлайн
					mOnlineFriends.add(friend);//добавляеися в список онлайн
				}
				else if (status.equals("unApproved"))//если не принятый
				{
					friend.status = InfoStatus.UNAPPROVED;//присваивается статус
					mUnapprovedFriends.add(friend);//добавляется в список
				}	
				else
				{
					friend.status = InfoStatus.OFFLINE;//в остальных случаях офлайн
					mFriends.add(friend);
				}											
			}
			else if (localName == "user") {
				this.userKey = attributes.getValue(InfoOfFriend.USER_KEY);
			}
			else if (localName == "message") {
				InfoOfMessage message = new InfoOfMessage();
				message.userid = attributes.getValue(InfoOfMessage.USERID);
				message.sendt = attributes.getValue(InfoOfMessage.SENDT);

				message.messagetext = attributes.getValue(InfoOfMessage.MESSAGETEXT);
				Log.i("MessageLOG", message.userid + message.sendt + message.messagetext);
				mUnreadMessages.add(message);
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void startDocument() throws SAXException {			
			this.mFriends.clear();
			this.mOnlineFriends.clear();
			this.mUnreadMessages.clear();
			super.startDocument();
		}
		
		
}

