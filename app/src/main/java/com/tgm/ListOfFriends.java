package com.tgm;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.util.Swappable;
import com.tgm.interfacer.Manager;
import com.tgm.serve.MessagingService;
import com.tgm.toolBox.ControllerOfFriend;
import com.tgm.toolBox.StorageManipulater;
import com.tgm.typo.InfoOfFriend;
import com.tgm.typo.InfoOfMessage;
import com.tgm.typo.InfoStatus;

import com.appeaser.sublimenavigationviewlibrary.OnNavigationMenuEventListener;
import com.appeaser.sublimenavigationviewlibrary.SublimeBaseMenuItem;
import com.appeaser.sublimenavigationviewlibrary.SublimeMenu;
import com.appeaser.sublimenavigationviewlibrary.SublimeNavigationView;
import com.tgm.typo.Messages;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class  ListOfFriends extends AppCompatActivity
{
	private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	private Manager imService = null;
	private FriendListAdapter friendAdapter;
	private DynamicListView list;
    SublimeNavigationView snv;
	private StorageManipulater localstoragehandler;
	private Cursor dbCursor;
	private TextView nameofusr;
    private MenuItem searchMenuItem;
	private Toolbar mActionBarToolbar;
	private static final String TOOLBAR_TEXTVIEW_FIELD_NAME = "mTitleTextView";
	private static final String TOOLBAR_NAV_BTN_FIELD_NAME = "mNavButtonView";
	private TextView tvToolbarTitle;
	public String ownusername = new String();
	private static EditText mFriendUserNameText;
    private MaterialSearchView mSearchView;
    private ImageButton btnToolbarButton;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
    private TextView empty;
	public MaterialProgressBar mpb;


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


	//список друзей
	private class FriendListAdapter extends BaseAdapter implements Swappable
	{

		String msg = "";
		String datemsg = "";

		boolean flag = false;
		private String namesArray [];

		class ViewHolder {
			TextView text;//имя друга
			TextView mes;
			TextView date;
			TextView newmes;
			View status;
			LinearLayout llRemoveCheckMark;
			ImageView icon;//статус
			boolean flag = false;
			int unrmesg = 0;

		}
		private LayoutInflater mInflater;
		private Bitmap mAvatar;

		private Context context;

		private InfoOfFriend friends[];
		String text = "";


		public FriendListAdapter(Context context) {
			super();
			this.context = context;

			mInflater = LayoutInflater.from(context);

			mAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_account_circle_white_48dp);
			Log.i("!!!!!!!!!!!!!!!!!!!!!", "CONSTRUKTER");


		}

		private void clearRemoveModes() {
			for (InfoOfFriend item : friends){
				item.isRemoveMode = false;
			}
		}

		public void removeItems(HashSet<Integer> financeItemsToRemove) {
			//TODO
			}


		public void saveOrder() {
		//TODO
		}

		public void orderByAlphabet() {
			//TODO
		}
		//сеттер
		public void setFriendList(InfoOfFriend[] friends)

		{
			namesArray = new String[friends.length];
			for (int i = 0; i < friends.length; i++) {
				namesArray[i] = friends[i].userName;
			}
			this.friends = friends;
		}

		public String[]	getNamesArray ()
		{
			return namesArray;
		}

		public void setFriendNew(String name, String newmsg, String newdate)
		{
			for (int i = 0; i < getCount(); i++) {
				if(friends[i].equals(name))
				{
					msg = newmsg;
					datemsg = newdate;
					break;}
			}

		}

		public int getPosition (String name)
		{
			for (int i = 0; i < friends.length; i++) {
				if (friends[i].userName.equals(name))
				{

					return i;
				}
			}
			return 0;
		}

//получаем количество
		public int getCount() {

			return friends.length;
		}
		
//выбираем друга
		public InfoOfFriend getItem(int position) {

			return friends[position];
		}

		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public void swapItems(int i1, int i2) {
//			Collections.swap(friends, i1, i2);
			//notifyDataSetChanged();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public View getView(int position, View convertView, ViewGroup parent) {



			Log.i("!!!!!!!!!!!!!!!!!!!!!", "GETVIEW");
			InfoOfFriend friend = getItem(position);
			ViewHolder holder;



			dbCursor = localstoragehandler.get(friend.userName, MessagingService.USERNAME );


			Log.i("username", friend.userName);
			Log.i("meserv username", MessagingService.USERNAME);


			if (dbCursor.getCount() > 0) {
				dbCursor.moveToLast();
			}

			Log.i("CURSOR!!!!!!!", String.valueOf(dbCursor.getCount()));




			//TODO затем при получении сообщения (flag!!)

			//localstoragehandler.close();

			if (convertView == null )
			{
				convertView = mInflater.inflate(R.layout.friend_list_screen, null);

				// создает ViewHolder и хранит ссылки на два дочерних view  с которыми хотим связать данные
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.name);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.mes = (TextView) convertView.findViewById(R.id.msg);
				holder.status = (View) convertView.findViewById(R.id.status);
				if(!msg.isEmpty()) holder.newmes = (TextView) convertView.findViewById(R.id.newmes);
				holder.llRemoveCheckMark = (LinearLayout) convertView.findViewById(R.id.llRemoveCheckMark);

				convertView.setTag(holder);
			}   
			else {

				holder = (ViewHolder) convertView.getTag();
			}

            holder.date.setText("");
            holder.mes.setText("");
				if(dbCursor.getCount() > 0 ) {
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
					Date d = null;


					try {
						if(msg.isEmpty())d = format.parse(dbCursor.getString(4));
						else d = format.parse(datemsg);
					} catch (ParseException e) {
						e.printStackTrace();
					}


					// привязка данных
//					if (unrmesg != 0) {
//						holder.newmes.setText(unrmesg);
//						holder.newmes.setVisibility(View.VISIBLE);
//					}
					holder.date.setText((DateUtils.getRelativeDateTimeString(ListOfFriends.this, d
									.getTime(), DateUtils.DAY_IN_MILLIS,
							DateUtils.DAY_IN_MILLIS, 0)));

					if(msg.isEmpty())
					{
                        Log.w("msg",  dbCursor.getString(3));
                        Log.w("2",  friend.userName);
                        Log.w("3",  MessagingService.USERNAME);
						try {
							holder.mes.setText(decryptMes(dbCursor.getString(3),MessagingService.USERNAME,friend.userName)   );
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
					}
					else
					{
						holder.mes.setText(msg);
						holder.newmes.setText("1");
						holder.newmes.setVisibility(View.VISIBLE);
					}
				}
            else
                {
                    holder.date.setText("");
                    holder.mes.setText("");
                }


			holder.text.setText(friend.userName);
			holder.icon.setImageBitmap(mAvatar);
			holder.status.setBackgroundColor(getResources().getColor(friend.status == InfoStatus.ONLINE ? R.color.main_color_green : R.color.gray_light));



			 msg = "";
			 datemsg = "";
			 flag = false;


			return convertView;
		}

	}

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("Broadcast receiver ", "received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();



				if (action.equals(MessagingService.FRIEND_LIST_UPDATED))
				{
					// taking friend List from broadcast
					//String rawFriendList = extra.getString(FriendInfo.FRIEND_LIST);
					//FriendList.this.parseFriendInfo(rawFriendList);
					ListOfFriends.this.updateData(ControllerOfFriend.getFriendsInfo(),
												ControllerOfFriend.getUnapprovedFriendsInfo());
					
				}
				else if (action.equals(MessagingService.TAKE_MESSAGE))
				{
					String username = extra.getString(InfoOfMessage.USERID);
                String message = extra.getString(InfoOfMessage.MESSAGETEXT);
                String time = extra.getString(InfoOfMessage.SENDT);

					friendAdapter.notifyDataSetChanged();


				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((MessagingService.IMBinder)service).getService();      
			
			InfoOfFriend[] friends = ControllerOfFriend.getFriendsInfo(); //imService.getLastRawFriendList();
			if (friends != null) {    			
				ListOfFriends.this.updateData(friends, null); // parseFriendInfo(friendList);
			}
			
//			setTitle("Telekilogramm");
//			setTitleColor(R.color.white);
			//nameofusr.setText(imService.getUsername());
			ownusername = imService.getUsername();
            if(ownusername!=null) nameofusr.setText(ownusername);


		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(ListOfFriends.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	protected void onCreate(Bundle savedInstanceState) 
	{
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_friends);

		mpb = (MaterialProgressBar) findViewById(R.id.progress);
		mpb.setVisibility(View.GONE);

        snv = (SublimeNavigationView) findViewById(R.id.navigation_view);

		nameofusr = (TextView) (snv.getHeaderView()).findViewById(R.id.tvNamePlate);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mActionBarToolbar);
		mActionBarToolbar.setTitle("Telekilogramm");
		mActionBarToolbar.setTitleTextColor(Color.WHITE);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);


		mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,mActionBarToolbar,
                R.string.drawer_open,R.string.drawer_close){
            public void onDrawerClosed(View view) {

            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        public void onDrawerOpened(View drawerView) {

        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        empty = (TextView) findViewById(R.id.empty2);
        empty.setVisibility(View.VISIBLE);




		if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }



//TODO подумать для возможно адаптера, 2 ситации, 1) в момент входа мы получаем адаптер и в нем пилим из склайт 2
// TODO 2) при получении сообщения обновляем весь адаптер или какую то часть хз
//		localstoragehandler = new StorageManipulater(this);
//		dbCursor = localstoragehandler.get(friend.userName, MessagingService.USERNAME );
//
//		if (dbCursor.getCount() > 0){
//			int noOfScorer = 0;
//			dbCursor.moveToFirst();
//			while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount())
//			{
//				noOfScorer++;
//
//				//this.appendToMessageHistory(dbCursor.getString(2), dbCursor.getString(3));
//				Messages msgs = new Messages(dbCursor.getString(2),dbCursor.getString(3),dbCursor.getString(4));
//
//				convList.add(msgs);
//				dbCursor.moveToNext();
//			}
//		}
//		localstoragehandler.close();


//		localstoragehandler = new StorageManipulater(this);
//		dbCursor = localstoragehandler.get(InfoOfFriend.USERNAME, MessagingService.USERNAME );
//		dbCursor.moveToLast();
//
//		localstoragehandler.close();

		localstoragehandler = new StorageManipulater(ListOfFriends.this);


		if (list == null) {

			list = (DynamicListView) findViewById(R.id.listfr);
            empty.setVisibility(View.GONE);

			friendAdapter = new FriendListAdapter(ListOfFriends.this);
			friendAdapter.setFriendList(ControllerOfFriend.getFriendsInfo());

			list.setAdapter(friendAdapter);
			list.enableDragAndDrop();


            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
											@Override
											public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

												mpb.setVisibility(View.VISIBLE);
												Intent i = new Intent(ListOfFriends.this, PerformingMessaging.class);
												InfoOfFriend friend = friendAdapter.getItem(position);
												i.putExtra(InfoOfFriend.USERNAME, friend.userName);
												i.putExtra(InfoOfFriend.PORT, friend.port);
												i.putExtra(InfoOfFriend.IP, friend.ip);
												startActivity(i);



											}
										}
			);
		}

		mSearchView = (MaterialSearchView) findViewById(R.id.search_view);
		mSearchView.setVoiceSearch(true);
//		mSearchView.setEnabled(true);

		mSearchView.setCursorDrawable(R.drawable.color_cursor_white);
		mSearchView.setSuggestions(friendAdapter.getNamesArray());
		mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Intent i = new Intent(ListOfFriends.this, PerformingMessaging.class);
				InfoOfFriend friend = friendAdapter.getItem(friendAdapter.getPosition(query));
				i.putExtra(InfoOfFriend.USERNAME, friend.userName);
				i.putExtra(InfoOfFriend.PORT, friend.port);
				i.putExtra(InfoOfFriend.IP, friend.ip);
				startActivity(i);
				Log.i("CLICK!!!!", "Item checked");
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				//Do some magic
				return false;
			}
		});

		mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
			@Override
			public void onSearchViewShown() {
				//Do some magic
			}

			@Override
			public void onSearchViewClosed() {
				//Do some magic
			}
		});

		snv.setNavigationMenuEventListener(new OnNavigationMenuEventListener() {
			@Override
			public boolean onNavigationMenuEvent(Event event,
												 SublimeBaseMenuItem menuItem) {
				int id = menuItem.getItemId();
				switch (event) {
					case CHECKED:
						Log.i("", "Item checked");
						break;
					case UNCHECKED:
						Log.i("", "Item unchecked");
						break;
					case GROUP_EXPANDED:
						Log.i("", "Group expanded");
						break;
					case GROUP_COLLAPSED:
						Log.i("", "Group collapsed");
						break;
					default:
						menuItem.setChecked(!menuItem.isChecked());
						break;
				}
				if(id == R.id.addfriendMenu){
					showDialog(0);
				return true;
				}

                if(id == R.id.Unapr_fr) {
                    Intent i = new Intent(ListOfFriends.this, WaitingListFriends.class);
                    startActivity(i);
                }

				if(id == R.id.settings) {
					Intent i = new Intent(ListOfFriends.this, SettingsActivity.class);
					startActivity(i);
				}

				if(id == R.id.feedback) {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("message/rfc822");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"mostwanted007@mail.ru"});
					i.putExtra(Intent.EXTRA_SUBJECT, "Feedback TgM");
					i.putExtra(Intent.EXTRA_TEXT   , "Привет, твое приложение безупречно!");
					try {
						startActivity(Intent.createChooser(i, "Send mail..."));
					} catch (ActivityNotFoundException ex) {
						Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
					}
				}


				if(id == R.id.share) {
					final Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					String textToSend = "Привет, я использую TgM для обмена сообщениями, добавь меняЮ мой ник" + ownusername;
					intent.putExtra(Intent.EXTRA_TEXT, textToSend);
					try {
						startActivity(Intent.createChooser(intent, "Описание действия"));
					} catch (ActivityNotFoundException ex) {
						Toast.makeText(getApplicationContext(), "Some error", Toast.LENGTH_SHORT).show();
					}
				}
				if(id == R.id.help) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ListOfFriends.this);
					builder.setTitle("О приложении")
							.setMessage("TgM\n" +
									"Приложение для защищенного обмена сообщениями\n" +
									"Разработчик: Студент гр. ПО-21 Гудин Е.Р.\"")
							.setIcon(R.drawable.ic_launcher)
							.setCancelable(true)
							.setNegativeButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}

				if(id == R.id.exit) {
					imService.exit();
				finish();
				return true;
				}
				return true;
			}

		});


	}
	public void updateData(InfoOfFriend[] friends, InfoOfFriend[] unApprovedFriends)
	{

		if (friends != null) {
			Log.w("DRDRDRDRDRDR", "hgjhgjghgjhgjhgjhgjhgh");
			friendAdapter.setFriendList(friends);
            empty.setVisibility(View.GONE);

			friendAdapter.notifyDataSetChanged();

		}
		Log.w("DRDRDRDRDRDR", "NO");
		if (unApprovedFriends != null) 
		{
			NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			if (unApprovedFriends.length > 0)
			{					
				String tmp = new String();
				for (int j = 0; j < unApprovedFriends.length; j++) {
					tmp = tmp.concat(unApprovedFriends[j].userName).concat(",");			
				}
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		    	.setSmallIcon(R.drawable.notification)
		    	.setContentTitle(getText(R.string.new_friend_request_exist));
				/*Notification notification = new Notification(R.drawable.stat_sample, 
						getText(R.string.new_friend_request_exist),
						System.currentTimeMillis());*/

				Intent i = new Intent(this, WaitingListFriends.class);
				i.putExtra(InfoOfFriend.FRIEND_LIST, tmp);				

				PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
						i, 0);

				mBuilder.setContentText("Новый запрос друбы");
				/*notification.setLatestEventInfo(this, getText(R.string.new_friend_request_exist),
												"You have new friend request(s)", 
												contentIntent);*/
				
				mBuilder.setContentIntent(contentIntent);

				
				NM.notify(R.string.new_friend_request_exist, mBuilder.build());			
			}
			else
			{
				NM.cancel(R.string.new_friend_request_exist);			
			}
		}

	}


//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//
//		super.onListItemClick(l, v, position, id);
//
//		Intent i = new Intent(this, PerformingMessaging.class);
//		InfoOfFriend friend = friendAdapter.getItem(position);
//		i.putExtra(InfoOfFriend.USERNAME, friend.userName);
//		i.putExtra(InfoOfFriend.PORT, friend.port);
//		i.putExtra(InfoOfFriend.IP, friend.ip);
//		startActivity(i);
//	}



	@Override
	protected void onPause() 
	{
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{

		super.onResume();
		mpb.setVisibility(View.GONE);
		bindService(new Intent(ListOfFriends.this, MessagingService.class), mConnection , Context.BIND_AUTO_CREATE);

		IntentFilter i = new IntentFilter();
		i.addAction(MessagingService.TAKE_MESSAGE);
		i.addAction(MessagingService.FRIEND_LIST_UPDATED);

		registerReceiver(messageReceiver, i);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);

		
		return super.onCreateOptionsMenu(menu);
	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else  return super.onOptionsItemSelected(item);
    }



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    mSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

	}


	private void addNewFriend() {
		if (mFriendUserNameText.length() > 0) {

			Thread thread = new Thread() {
				@Override
				public void run() {

					imService.addNewFriendRequest(mFriendUserNameText.getText().toString());
				}
			};
			thread.start();


			Toast.makeText(ListOfFriends.this, R.string.request_sent, Toast.LENGTH_SHORT).show();

		} else {
			Log.e("ADD FRIEND", "addNewFriend: username length (" + mFriendUserNameText.length() + ") is < 0");
			Toast.makeText(ListOfFriends.this, R.string.type_friend_username, Toast.LENGTH_LONG).show();
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0:
				mFriendUserNameText = new EditText(this);
				android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
				builder.setView(mFriendUserNameText);
				builder.setMessage("Добавить друга")
						.setPositiveButton("Принять",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										addNewFriend();

										dialog.cancel();
									}
								})
						.setNegativeButton("Назад",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										dialog.cancel();
									}
								});

				return builder.create();
			default:
				return null;
		}
	}

    @Override
    public void onBackPressed() {

        if(mDrawerLayout.isDrawerOpen(snv)) mDrawerLayout.closeDrawer(snv);
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
       }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

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
}
