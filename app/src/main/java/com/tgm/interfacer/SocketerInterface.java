package com.tgm.interfacer;


public interface SocketerInterface {
	
	public String sendHttpRequest(String params);//отправляет hhtp запрос доступин ли пользователь
	public int startListening(int port);//начинает слушать порт
	public void stopListening();//остановка прослушки порта
	public void exit();
	public int getListeningPort();
	public void setHost(String ip);
	public  String getHost ();

}
