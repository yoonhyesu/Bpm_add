package com.example.bpm_service.heartrate;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.bpm_service.connection.MovieInformationServer;
import com.example.bpm_service.connection.SocialServer;
import com.example.bpm_service.uinfo.ReservationActivity;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Calendar;

public class BpmTransactionService extends Service implements
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener{

    public BpmTransactionService() {
    }

    String IP = "";
    String userId = "";
    String title = "";
    String time = "";

    String hour, minute;

    private static final String TAG = BpmTransactionService.class.getSimpleName();
    Context mContext;
    AlarmManager alarm_manager;

    Intent alarmIntent;
    PendingIntent pendingIntent;
    private static final int REQUEST_CODE = 1111;
    SharedPreferences pref;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId){
        boolean state = intent.getExtras().getBoolean("bpmStart");
        IP = intent.getExtras().getString("IP","");
        userId = intent.getExtras().getString("userId","");
        title = intent.getExtras().getString("title","");
        time = intent.getExtras().getString("time","");

        if(state){
            System.out.println("??????????????? ?????? ??????????????? ?????? ??????");
            Wearable.getMessageClient(this).addListener(this);

            setAlarm(time);
        }else{
            System.out.println("?????? ?????? ?????? ????????? ?????? ??????????????? ?????? ??????");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCapabilityChanged(@NonNull @NotNull CapabilityInfo capabilityInfo) {

    }

    @Override
    public void onDataChanged(@NonNull @NotNull DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(@NonNull @NotNull MessageEvent messageEvent) {

        SocialServer socialServer = new SocialServer(IP);
        try{
            JSONObject json = new JSONObject();
            json.put("title", title);
            json.put("userId", userId);
            json.put("bpm", new String(messageEvent.getData()));

            System.out.println(json.toString());

            socialServer.sendBpm(json.toString());

            Intent intent = new Intent(this,BpmTransactionService.class);
            intent.putExtra("bpmStart", false);
            startService(intent);

            SharedPreferences reservationData = getSharedPreferences("reservationData", MODE_PRIVATE);
            SharedPreferences.Editor editor = reservationData.edit();
            editor.clear();
            editor.commit();
            editor.putBoolean("reservationState", false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setAlarm(String time){
        // Calendar ?????? ??????
        final Calendar calendar = Calendar.getInstance();

        String[] splitTime = time.split("~");

        time = splitTime[0];
        System.out.println(time);

        splitTime = time.split(":");
//        hour = splitTime[0];
//        minute = splitTime[1];

        hour = "17";
        minute = "54";

        // calendar??? ?????? ??????
//        if (Build.VERSION.SDK_INT < 23) {
//            // ?????? ?????????
//            getHourTimePicker = alarm_timepicker.getCurrentHour();
//            getMinuteTimePicker = alarm_timepicker.getCurrentMinute();
//        } else {
//            // ?????? ?????????
//            getHourTimePicker = alarm_timepicker.getHour();
//            getMinuteTimePicker = alarm_timepicker.getMinute();
//        }

        // ?????? ????????? ???????????? ?????? ?????? ??????
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
        calendar.set(Calendar.SECOND, 0);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("set_hour", Integer.parseInt(hour));
        editor.putInt("set_min", Integer.parseInt(minute));
        editor.putString("state", "ALARM_ON");
        editor.commit();

        alarm_manager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(this, ReservationReceiver.class);

        // reveiver??? string ??? ????????????
        alarmIntent.putExtra("state","ALARM_ON");

        // receiver??? ???????????? ?????? ?????? PendingIntent??? ??????????????? ????????? ???, getBroadcast ?????? ???????????? ??????
        // requestCode??? ????????? Alarm??? ?????? ?????? ?????? Alarm??? ??????????????? ???????????? ??????
        pendingIntent = PendingIntent.getBroadcast(this,REQUEST_CODE,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        long currentTime = System.currentTimeMillis(); // ?????? ??????
        //long triggerTime = SystemClock.elapsedRealtime() + 1000*60;
        long triggerTime = calendar.getTimeInMillis(); // ????????? ?????? ??????
        System.out.println(triggerTime);
        System.out.println(currentTime);
        long interval = 1000 * 60 * 60  * 24; // ????????? ??????

        while(currentTime > triggerTime){ // ?????? ???????????? ?????????
            triggerTime += interval; // ????????? ???????????? ??????
        }
        Log.e(TAG, "set Reservation : " + Integer.parseInt(hour) + "??? " + Integer.parseInt(minute) + "???");

        // ?????? ?????? : AlarmManager ?????????????????? set ???????????? ??????????????? ?????? ????????? Alarm??? ???????????? ???
        // RTC_WAKEUP : UTC ??????????????? ???????????? ?????? ???????????? ????????? intent??? ??????, ????????? ??????
        if (Build.VERSION.SDK_INT < 23) {
            if (Build.VERSION.SDK_INT >= 19) {
                alarm_manager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                // ????????????
                alarm_manager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {  // 23 ??????
            alarm_manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            //alarm_manager.set(AlarmManager.RTC_WAKEUP, triggerTime,pendingIntent);
            //?????? ???????????? ?????? ???????????? ??????
            //alarm_manager.setRepeating(AlarmManager.RTC, triggerTime, interval, pendingIntent);
            // interval : ?????? ????????? ?????????????????? ??????
        }

        // Unable to find keycodes for AM and PM.
//        if(getHour33TimePicker > 12){
//            am_pm = "??????";
//            getHourTimePicker = getHourTimePicker - 12;
//        } else {
//            am_pm ="??????";
//        }
    }

    public void releaseAlarm(Context context)  {
        Log.e(TAG, "unregisterAlarm");

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("state", "ALARM_OFF");
        editor.commit();

        // ??????????????? ??????
        alarm_manager.cancel(pendingIntent);
        alarmIntent.putExtra("state","ALARM_OFF");

        // ?????? ??????
        sendBroadcast(alarmIntent);
    }
}