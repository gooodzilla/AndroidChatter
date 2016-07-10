package com.tgm.toolBox;

import com.tgm.typo.InfoOfMessage;


public class MessageController 
{
	
	private static InfoOfMessage[] messagesInfo = null;
	//сеттер информации о сообщении
	public static void setMessagesInfo(InfoOfMessage[] messageInfo)
	{
		MessageController.messagesInfo = messageInfo;
	}

	public static InfoOfMessage checkMessage(String username)
	{
		InfoOfMessage result = null;
		if (messagesInfo != null) 
		{
			for (int i = 0; i < messagesInfo.length;) 
			{
				
					result = messagesInfo[i];
					break;
								
			}			
		}		
		return result;
	}

	public static InfoOfMessage getMessageInfo(String username) 
	{
		InfoOfMessage result = null;
		if (messagesInfo != null) 
		{
			for (int i = 0; i < messagesInfo.length;) 
			{
					result = messagesInfo[i];
					break;
							
			}			
		}		
		return result;
	}

	public static InfoOfMessage[] getMessagesInfo() {
		return messagesInfo;
	}

}
