package com.tgm.typo;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mostw on 17.05.2016.
 */
public class Messages {

    private String msg;
    private String sender;
    private Date sendt;

    public Messages (String sender, String msg, String sendt)
    {
        Log.i("MESSAGES ", sender+sendt+msg);
        this.msg = msg;
        this.sender = sender;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            this.sendt = format.parse(sendt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public Messages (String sender, String msg)
    {
        this.msg = msg;
        this.sender = sender;


    }

    public String getMsg() {
        return msg;
    }

    public String getSender() {
        return sender;
    }

    public Date getSendt() {
        return sendt;
    }

    public void setMsg(String msg) {
        Log.i("MESSAGES ", msg);this.msg = msg;
    }
}
