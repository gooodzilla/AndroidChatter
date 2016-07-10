package com.tgm.toolBox;

import com.tgm.typo.InfoOfFriend;


public class ControllerOfFriend 
{
	
	private static InfoOfFriend[] friendsInfo = null;// добавленные друзья
	private static InfoOfFriend[] unapprovedFriendsInfo = null;//не добавленные
	private static String activeFriend;//онлайн

	//сеттер
	public static void setFriendsInfo(InfoOfFriend[] friendInfo)
	{
		ControllerOfFriend.friendsInfo = friendInfo;
	}
	
	
	
	public static InfoOfFriend checkFriend(String username, String userKey)
	{
		InfoOfFriend result = null;
		if (friendsInfo != null)
		{
			for (int i = 0; i < friendsInfo.length; i++) 
			{
				if ( friendsInfo[i].userName.equals(username) && 
					 friendsInfo[i].userKey.equals(userKey)
					)
				{
					result = friendsInfo[i];
					break;
				}				
			}
		}		
		return result;
	}
	//сеттер для онлайн пользователей
	public static void setActiveFriend(String friendName){
		activeFriend = friendName;
	}
	//геттер для онлайн пользователей
	public static String getActiveFriend()
	{
		return activeFriend;
	}


//получаем информуцию о друзьях
	public static InfoOfFriend getFriendInfo(String username) 
	{
		InfoOfFriend result = null;
		if (friendsInfo != null) 
		{
			for (int i = 0; i < friendsInfo.length; i++) 
			{
				if ( friendsInfo[i].userName.equals(username) )
				{
					result = friendsInfo[i];
					break;
				}				
			}			
		}		
		return result;
	}


//сеттер не добавленных друзей
	public static void setUnapprovedFriendsInfo(InfoOfFriend[] unapprovedFriends) {
		unapprovedFriendsInfo = unapprovedFriends;		
	}



	public static InfoOfFriend[] getFriendsInfo() {
		return friendsInfo;
	}



	public static InfoOfFriend[] getUnapprovedFriendsInfo() {
		return unapprovedFriendsInfo;
	}
	
	
	

}
