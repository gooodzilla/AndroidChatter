package com.tgm;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.util.Swappable;
import com.tgm.interfacer.Manager;
import com.tgm.serve.MessagingService;
import com.tgm.toolBox.ControllerOfFriend;
import com.tgm.typo.InfoOfFriend;
import com.tgm.typo.InfoStatus;

import java.util.HashSet;
import android.app.Dialog;

public class WaitingListFriends extends AppCompatActivity {
	
	private static final int APPROVE_SELECTED_FRIENDS_ID = 0;
//	private static final int DISCARD_ID = 1;
	private String[] friendUsernames;
	private Manager imService;
	String approvedFriendNames = new String();
	String discardedFriendNames = new String();
    private Toolbar mActionBarToolbar;
    private FriendListAdapter friendAdapter;
    private ListView list;
	private int pos = 0;
	private TextView empty;
	private class FriendListAdapter extends BaseAdapter implements Swappable
	{


		class ViewHolder {
			TextView text;//имя друга

			View status;

			ImageView icon;//статус

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
			this.friends = friends;
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
			notifyDataSetChanged();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public View getView(int position, View convertView, ViewGroup parent) {




			InfoOfFriend friend = getItem(position);

			ViewHolder holder;

			if (convertView == null )
			{
				convertView = mInflater.inflate(R.layout.unapr_friend_list_screen, null);

				// создает ViewHolder и хранит ссылки на два дочерних view  с которыми хотим связать данные
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.unp_name);
				holder.icon = (ImageView) convertView.findViewById(R.id.unp_icon);
				holder.status = (View) convertView.findViewById(R.id.unp_status);

				convertView.setTag(holder);
			}
			else {

				holder = (ViewHolder) convertView.getTag();
			}



			// привязка данных
			holder.text.setText(friend.userName);
			holder.icon.setImageBitmap(mAvatar);
			holder.status.setBackgroundColor(getResources().getColor(friend.status == InfoStatus.ONLINE ? R.color.main_color_green : R.color.gray_light));

			return convertView;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.unapr_list_friends);
		
		Bundle extras = getIntent().getExtras();

//        if(!extras.getString(InfoOfFriend.FRIEND_LIST).isEmpty()) {
//            String names = extras.getString(InfoOfFriend.FRIEND_LIST);
//            Log.e("FRIENDS LIST  - ", names);
//
//            friendUsernames = names.split(",");

            mActionBarToolbar = (Toolbar) findViewById(R.id.include2);
            setSupportActionBar(mActionBarToolbar);
			mActionBarToolbar.setTitle("Запросы в друзья");
			setTitle("Запросы в друзья");

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);


			empty = (TextView) findViewById(R.id.empty);
			empty.setVisibility(View.VISIBLE);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }

            friendAdapter = new FriendListAdapter(WaitingListFriends.this);
            friendAdapter.setFriendList(ControllerOfFriend.getUnapprovedFriendsInfo());
		Log.w("UNPROVED",ControllerOfFriend.getUnapprovedFriendsInfo().toString());

			if(!friendAdapter.isEmpty())  empty.setVisibility(View.GONE);
            list = (ListView) findViewById(R.id.unp_listfr);

            list.setAdapter(friendAdapter);

            list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					pos = position;
					showDialog(0);
                    return true;
                }
            });


//		setListAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_multiple_choice, friendUsernames));
//
//		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


            NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NM.cancel(R.string.new_friend_request_exist);
//        }
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Запрос дружбы")
                        .setCancelable(false)
                        .setPositiveButton("Принять",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
										approvedFriendNames = approvedFriendNames.concat(friendAdapter.getItem(pos).userName);
										for (int i = 0; i < friendAdapter.getCount(); i++) {
											if(i == pos) continue;
											else discardedFriendNames = discardedFriendNames.concat(friendAdapter.getItem(i).userName);
										}
										friendAdapter.notifyDataSetChanged();
										if(friendAdapter.isEmpty()) empty.setVisibility(View.VISIBLE);
										else empty.setVisibility(View.GONE);

										Thread thread = new Thread(){
											@Override
											public void run() {
												if ( approvedFriendNames.length() > 0 ||
														discardedFriendNames.length() > 0
														)
												{
													imService.sendFriendsReqsResponse(approvedFriendNames, discardedFriendNames);

												}
											}
										};
										thread.start();

										Toast.makeText(WaitingListFriends.this, R.string.request_sent, Toast.LENGTH_SHORT).show();

										finish();

										dialog.cancel();
                                    }
                                })
                        .setNegativeButton("Отклонить",
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
	public boolean onCreateOptionsMenu(Menu menu) {


		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		if (android.R.id.home == item.getItemId())
		{
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


//	@Override
//	public boolean onMenuItemSelected(int featureId, MenuItem item)
//	{
//
//		switch(item.getItemId())
//		{
//			case APPROVE_SELECTED_FRIENDS_ID:
//			{
//				int reqlength = getListAdapter().getCount();
//
//				for (int i = 0; i < reqlength ; i++)
//				{
//					if (getListView().isItemChecked(i)) {
//						approvedFriendNames = approvedFriendNames.concat(friendUsernames[i]).concat(",");
//					}
//					else {
//						discardedFriendNames = discardedFriendNames.concat(friendUsernames[i]).concat(",");
//					}
//				}
//				Thread thread = new Thread(){
//					@Override
//					public void run() {
//						if ( approvedFriendNames.length() > 0 ||
//							 discardedFriendNames.length() > 0
//							)
//						{
//							imService.sendFriendsReqsResponse(approvedFriendNames, discardedFriendNames);
//
//						}
//					}
//				};
//				thread.start();
//
//				Toast.makeText(WaitingListFriends.this, R.string.request_sent, Toast.LENGTH_SHORT).show();
//
//				finish();
//				return true;
//			}
//
//		}
//
//		return super.onMenuItemSelected(featureId, item);
//	}

	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		bindService(new Intent(WaitingListFriends.this, MessagingService.class), mConnection , Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((MessagingService.IMBinder)service).getService();      

			
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(WaitingListFriends.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
}
