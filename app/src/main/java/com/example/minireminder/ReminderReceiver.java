package com.example.minireminder;
import android.app.*;
import android.content.*;
public class ReminderReceiver extends android.content.BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i) {
  NotificationManager m=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  m.createNotificationChannel(new NotificationChannel("reminders","Напоминания",NotificationManager.IMPORTANCE_HIGH));
  Notification n=new Notification.Builder(c,"reminders").setSmallIcon(android.R.drawable.ic_popup_reminder)
   .setContentTitle(i.getStringExtra("title")).setContentText("Пора выполнить задачу ✨").setAutoCancel(true).build();
  try {m.notify(i.getIntExtra("id",0),n);}catch(SecurityException ignored){}
 }
}