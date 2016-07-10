package com.tgm.interfacer;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


public interface Manager {
	
	public String getUsername(); //получить имя пользователя
	public String sendMessage(String username,String tousername, String message) throws UnsupportedEncodingException;//сообщение между польз
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException;//спрашивает пароль и логин
	public void messageReceived(String username, String message, String sendt);// когда отправляется сообщение идет анализ
	public boolean isNetworkConnected();
	public boolean isUserAuthenticated();
	public String getLastRawFriendList();
	public void exit();
	public String signUpUser(String usernameText, String passwordText, String email);//регистрация
	public String addNewFriendRequest(String friendUsername);//запрос на дружбу
	public String sendFriendsReqsResponse(String approvedFriendNames,//ответ на запрос дружбы
			String discardedFriendNames);
	public String encryptMes(String mes,String s1,String s2) throws NoSuchAlgorithmException;
	public String decryptMes(String mes,String s1,String s2) throws NoSuchAlgorithmException;
	public void setIP(String ip);
	public String getIP();
	
}
