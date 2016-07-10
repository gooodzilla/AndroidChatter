package com.tgm.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import com.tgm.interfacer.Manager;
import com.tgm.interfacer.SocketerInterface;

import android.util.Log;


public class Socketer implements SocketerInterface
{
	// адрес сервера
	//private static final String AUTHENTICATION_SERVER_ADDRESS = "http://192.168.1.80/androidchatter/";
	private static String AUTHENTICATION_SERVER_ADDRESS = "http://192.168.31.100/androidchatter/";
	private int listeningPort = 0;//прослушиваемый порт
	
	private static final String HTTP_REQUEST_FAILED = null;
	
	private HashMap<InetAddress, Socket> sockets = new HashMap<InetAddress, Socket>();
	
	private ServerSocket serverSocket = null;

	private boolean listening;

	private class ReceiveConnection extends Thread {//получение подключения
		Socket clientSocket = null;//создаем соединение
		//КОНСТРУКТОР
		public ReceiveConnection(Socket socket) 
		{
			this.clientSocket = socket;
			Socketer.this.sockets.put(socket.getInetAddress(), socket);//добавляем в коллекцию
		}

		//запуск нового потока
		@Override
		public void run() {
			 try {
	//			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						    new InputStreamReader(
						    		clientSocket.getInputStream()));
				String inputLine;
				
				 while ((inputLine = in.readLine()) != null) 
				 {
					 if (inputLine.equals("exit") == false)
					 {
						 //appManager.messageReceived(inputLine);						 
					 }
					 else
					 {//закрываем соединение
						 clientSocket.shutdownInput();
						 clientSocket.shutdownOutput();
						 clientSocket.close();
						 Socketer.this.sockets.remove(clientSocket.getInetAddress());
					 }						 
				 }		
				
			} catch (IOException e) {
				Log.e("ReceiveConnection.run: ","");
			}			
		}	
	}

	public Socketer(Manager appManager) {	
	}
	
	
	public String sendHttpRequest(String params)
	{		
		URL url;//адрес сервера
		String result = new String();
		try 
		{
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);//получаем адрес
			Log.w("IP", AUTHENTICATION_SERVER_ADDRESS);
			HttpURLConnection connection;

			connection = (HttpURLConnection) url.openConnection();//открываем соедемнение
			connection.setDoOutput(true);
			//отправляем
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			
			out.println(params);
			out.close();
			//читаем
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result = result.concat(inputLine);				
			}
			in.close();			
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}			
		
		if (result.length() == 0) {
			result = HTTP_REQUEST_FAILED;
		}
		Log.i("RESULT", result);
		return result;
		
	
	}

	public int startListening(int portNo) //начинаем слушать порт
	{
		listening = true;
		
		try {
			serverSocket = new ServerSocket(portNo);
			this.listeningPort = portNo;
		} catch (IOException e) {			
			
			e.printStackTrace();
			this.listeningPort = 0;
			return 0;
		}

		while (listening) {
			try {
				new ReceiveConnection(serverSocket.accept()).start();
				
			} catch (IOException e) {
				e.printStackTrace();
				return 2;
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {			
			Log.e("Exception server socket", "Exception when closing server socket");
			return 3;
		}
		
		
		return 1;
	}
	
	//останавливаем прослушку порта
	public void stopListening() 
	{
		this.listening = false;
	}


	public void exit() 
	{			
		for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();) 
		{
			Socket socket = (Socket) iterator.next();
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) 
			{				
			}		
		}
		
		sockets.clear();
		this.stopListening();
	}

//получаем порт
	public int getListeningPort() {
		
		return this.listeningPort;
	}

	@Override
	public void setHost(String ip) {
		this.AUTHENTICATION_SERVER_ADDRESS = "http://" + ip + "/androidchatter/";
	}

	@Override
	public String getHost() {
		return AUTHENTICATION_SERVER_ADDRESS;
	}

}
