

package com.tgm;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tgm.interfacer.Manager;
import com.tgm.serve.MessagingService;


public class FriendAdder extends AppCompatActivity {


    private static EditText mFriendUserNameText;

    private static Manager mImService;

    private static final int TYPE_FRIEND_USERNAME = 0;
    private static final String LOG_TAG = "AddFriend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.friendfinderadder);

        final ActionBar actionBar = getActionBar();
        if (actionBar == null)
            return;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        actionBar.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.actionbar_bg));
        actionBar.setDisplayHomeAsUpEnabled(true);


        setTitle(getString(R.string.add_new_friend));

        Button mAddFriendButton = (Button)findViewById(R.id.addFriend);
        mFriendUserNameText = (EditText)findViewById(R.id.newFriendUsername);

        mAddFriendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                addNewFriend();
             Log.e("KUKU", "TUT");
             finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, MessagingService.class);
        if (mConnection != null) {
            bindService(intent, mConnection , Context.BIND_AUTO_CREATE);
        } else {
            Log.e(LOG_TAG, "onResume: mConnection is null");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mConnection != null) {
            unbindService(mConnection);
        } else {
            Log.e(LOG_TAG, "onResume: mConnection is null");
        }
    }

//    @Override
//    public void onClick(View view) {
//         if (view == mAddFriendButton) {
//            addNewFriend();
//             Log.e("KUKU", "TUT");
//             finish();
//        } else {
//            Log.e(LOG_TAG, "onClick: view clicked is unknown");
//        }
//    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mImService = ((MessagingService.IMBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mImService != null) {
                mImService = null;
            }

            Toast.makeText(FriendAdder.this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        }
    };


    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FriendAdder.this);
        if (id == TYPE_FRIEND_USERNAME) {
            builder.setTitle(R.string.add_new_friend)
                   .setMessage(R.string.type_friend_username)
                   .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int whichButton) {
                           // TODO
                       }
                   });
        }

        return builder.create();
     }

    private void addNewFriend() {
        if (mFriendUserNameText.length() > 0) {

            Thread thread = new Thread() {
                @Override
                public void run() {

                    mImService.addNewFriendRequest(mFriendUserNameText.getText().toString());
                }
            };
            thread.start();


            Toast.makeText(FriendAdder.this, R.string.request_sent, Toast.LENGTH_SHORT).show();

            finish();
        } else {
            Log.e(LOG_TAG, "addNewFriend: username length (" + mFriendUserNameText.length() + ") is < 0");
            Toast.makeText(FriendAdder.this, R.string.type_friend_username, Toast.LENGTH_LONG).show();
        }
    }
}