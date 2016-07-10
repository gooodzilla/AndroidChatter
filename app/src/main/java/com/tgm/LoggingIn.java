package com.tgm;


import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.tgm.interfacer.Manager;
import com.tgm.serve.MessagingService;


public class LoggingIn extends Activity {

    protected static final int NOT_CONNECTED_TO_SERVICE = 0;
	protected static final int FILL_BOTH_USERNAME_AND_PASSWORD = 1;
	public static final String AUTHENTICATION_FAILED = "0";
	public static final String FRIEND_LIST = "FRIEND_LIST";
	protected static final int MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT = 2 ;
	protected static final int NOT_CONNECTED_TO_NETWORK = 3;

	private EditText usernameText;//поле имя пользователя
    private EditText passwordText;//имя пароля

    private Manager imService;

    public static final int SIGN_UP_ID = Menu.FIRST;
    public static final int EXIT_APP_ID = Menu.FIRST + 1;

	public MaterialProgressBar mpb;
	private ImageButton sync;
	private EditText et;



    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
			// Вызывается, когда соединение с сервисом было установлено

            imService = ((MessagingService.IMBinder)service).getService();

            if (imService.isUserAuthenticated() == true)
            {
            	Intent i = new Intent(LoggingIn.this, ListOfFriends.class);
				startActivity(i);
				LoggingIn.this.finish();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // когда соединение прервано
        	imService = null;
            Toast.makeText(LoggingIn.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };




    //когда активити создается
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	startService(new Intent(LoggingIn.this, MessagingService.class));


        setContentView(R.layout.loggin_in);
        setTitle("Login");

		mpb = (MaterialProgressBar) findViewById(R.id.progress);



        Button loginButton = (Button) findViewById(R.id.btnLogin);
		Button regButton = (Button) findViewById(R.id.btnReg);
		sync = (ImageButton) findViewById(R.id.imageView2);

		sync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(0);
			}
		});


        usernameText = (EditText) findViewById(R.id.username);//получаем имя
        passwordText = (EditText) findViewById(R.id.password);        //пароль

		loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0)
			{					//если нет соедниения
				if (imService == null) {
					Toast.makeText(getApplicationContext(),R.string.not_connected_to_service, Toast.LENGTH_LONG).show();
					//showDialog(NOT_CONNECTED_TO_SERVICE);
					return;
				}
				else if (imService.isNetworkConnected() == false)
				{
					Toast.makeText(getApplicationContext(),R.string.not_connected_to_network, Toast.LENGTH_LONG).show();
					//showDialog(NOT_CONNECTED_TO_NETWORK);

				}
				//если есть соединение

				else if (usernameText.length() > 0 &&
					passwordText.length() > 0)
				{
					mpb.setVisibility(View.VISIBLE);

					Thread loginThread = new Thread(){
						private Handler handler = new Handler();
						@Override
						public void run() {
							String result = null;
							try {
								result = imService.authenticateUser(usernameText.getText().toString(), passwordText.getText().toString());
							} catch (UnsupportedEncodingException e) {

								e.printStackTrace();
							}
							if (result == null || result.equals(AUTHENTICATION_FAILED))
							{

								handler.post(new Runnable(){
									public void run() {
										Toast.makeText(getApplicationContext(),R.string.make_sure_username_and_password_correct, Toast.LENGTH_LONG).show();

										//showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
									}
								});

							}
							else {//запускаем новое активити со списком друзей
								handler.post(new Runnable(){
									public void run() {
										Intent i = new Intent(LoggingIn.this, ListOfFriends.class);
										//i.putExtra(FRIEND_LIST, result);						
										startActivity(i);
										LoggingIn.this.finish();
									}
								});

							}

						}
					};
					loginThread.start();

				}
				else {
					/*
					 * если нет совпадения имя - пароль
					 */
					Toast.makeText(getApplicationContext(),R.string.fill_both_username_and_password, Toast.LENGTH_LONG).show();
					//showDialog(FILL_BOTH_USERNAME_AND_PASSWORD);
				}
			}
        });

        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(LoggingIn.this, SigningUp.class);
                startActivity(i);

            }
        });



    }



	@Override
	protected void onPause()
	{
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		bindService(new Intent(LoggingIn.this, MessagingService.class), mConnection , Context.BIND_AUTO_CREATE);

		super.onResume();
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		boolean result = super.onCreateOptionsMenu(menu);
//
//		 menu.add(0, SIGN_UP_ID, 0, R.string.sign_up);
//		 menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);
//
//
//		return result;
//	}
//
//	@Override
//	public boolean onMenuItemSelected(int featureId, MenuItem item) {
//
//		switch(item.getItemId())
//	    {
//	    	case SIGN_UP_ID:
//	    		Intent i = new Intent(LoggingIn.this, SigningUp.class);
//	    		startActivity(i);
//	    		return true;
//	    	case EXIT_APP_ID:
//
//	    		return true;
//	    }
//
//	    return super.onMenuItemSelected(featureId, item);
//	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0:
				et = new EditText(this);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(et);
				builder.setMessage("IP адрес \n текущий: " + imService.getIP())
						.setPositiveButton("Принять",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										imService.setIP(et.getText().toString());

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





}