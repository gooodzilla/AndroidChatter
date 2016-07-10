package com.tgm.interfacer;
import com.tgm.typo.InfoOfFriend;
import com.tgm.typo.InfoOfMessage;


public interface Updater {
	//обновление
	public void updateData(InfoOfMessage[] messages, InfoOfFriend[] friends, InfoOfFriend[] unApprovedFriends, String userKey);

}
