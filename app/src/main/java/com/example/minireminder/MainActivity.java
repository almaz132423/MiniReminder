package com.example.minireminder;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.*;
import java.util.*;
public class MainActivity extends Activity {
 LinearLayout list; JSONArray tasks; int purple=Color.rgb(105,79,230); final String KEY="tasks";
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView label(String text,int size,int color){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);return v;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(245,243,255));getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},1);try{tasks=new JSONArray(getPreferences(0).getString(KEY,"[]"));}catch(Exception e){tasks=new JSONArray();}draw();}
 void save(){getPreferences(0).edit().putString(KEY,tasks.toString()).apply();}
 void draw(){LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(22),dp(30),dp(22),dp(18));root.setBackgroundColor(Color.rgb(248,247,255));setContentView(root);
 TextView heading=label("Мой день ✨",32,Color.rgb(35,30,65));heading.setTypeface(null,Typeface.BOLD);root.addView(heading);
 TextView sub=label("Маленькие шаги — большие результаты",14,Color.GRAY);sub.setPadding(0,dp(6),0,dp(22));root.addView(sub);
 Button add=new Button(this);add.setText("＋  Новое напоминание");add.setTextColor(Color.WHITE);add.setAllCaps(false);add.setTextSize(16);add.setBackground(bg(purple,18));root.addView(add,new LinearLayout.LayoutParams(-1,dp(60)));add.setOnClickListener(v->createDialog());
 TextView title=label("Ваши напоминания",20,Color.rgb(35,30,65));title.setTypeface(null,Typeface.BOLD);title.setPadding(0,dp(28),0,dp(14));root.addView(title);
 ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);refresh();}
 void refresh(){list.removeAllViews();if(tasks.length()==0){TextView empty=label("Пока пусто ☁️\nДобавьте первое напоминание",17,Color.GRAY);empty.setGravity(Gravity.CENTER);empty.setPadding(0,dp(85),0,0);list.addView(empty);return;}
 for(int j=0;j<tasks.length();j++){final int idx=j;JSONObject o=tasks.optJSONObject(j);if(o==null)continue;LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(18),dp(15),dp(18),dp(12));card.setBackground(bg(Color.WHITE,18));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(12);list.addView(card,cp);
 boolean done=o.optBoolean("done");TextView name=label((done?"✓  ":"◯  ")+o.optString("title"),18,done?Color.GRAY:Color.rgb(35,30,65));name.setTypeface(null,Typeface.BOLD);card.addView(name);TextView when=label(new SimpleDateFormat("dd.MM.yyyy • HH:mm",Locale.getDefault()).format(new Date(o.optLong("time"))),13,purple);when.setPadding(dp(27),dp(7),0,dp(4));card.addView(when);
 LinearLayout actions=new LinearLayout(this);card.addView(actions);Button toggle=new Button(this);toggle.setText(done?"Вернуть":"Готово");toggle.setAllCaps(false);actions.addView(toggle,new LinearLayout.LayoutParams(0,dp(48),1));toggle.setOnClickListener(v->{try{o.put("done",!done);}catch(Exception ignored){}save();refresh();});Button del=new Button(this);del.setText("Удалить");del.setAllCaps(false);actions.addView(del,new LinearLayout.LayoutParams(0,dp(48),1));del.setOnClickListener(v->{int id=o.optInt("id");AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);Intent intent=new Intent(this,ReminderReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(this,id,intent,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(pi!=null){am.cancel(pi);pi.cancel();}JSONArray next=new JSONArray();for(int k=0;k<tasks.length();k++)if(k!=idx)next.put(tasks.opt(k));tasks=next;save();refresh();});}}
 void createDialog(){EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Например: Выпить воду");LinearLayout wrap=new LinearLayout(this);wrap.setPadding(dp(20),0,dp(20),0);wrap.addView(input);new AlertDialog.Builder(this).setTitle("Новое напоминание ✨").setView(wrap).setNegativeButton("Отмена",null).setPositiveButton("Далее",(d,w)->{String text=input.getText().toString().trim();if(text.isEmpty()){Toast.makeText(this,"Введите название",Toast.LENGTH_SHORT).show();return;}Calendar c=Calendar.getInstance();DatePickerDialog picker=new DatePickerDialog(this,(view,y,m,day)->{c.set(y,m,day);TimePickerDialog time=new TimePickerDialog(this,(tv,h,min)->{c.set(Calendar.HOUR_OF_DAY,h);c.set(Calendar.MINUTE,min);c.set(Calendar.SECOND,0);if(c.getTimeInMillis()<=System.currentTimeMillis()){Toast.makeText(this,"Выберите будущее время",Toast.LENGTH_LONG).show();return;}int id=(int)(System.currentTimeMillis()&0x7fffffff);JSONObject o=new JSONObject();try{o.put("title",text);o.put("time",c.getTimeInMillis());o.put("id",id);o.put("done",false);}catch(Exception ignored){}tasks.put(o);save();schedule(id,text,c.getTimeInMillis());refresh();},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true);time.show();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));picker.getDatePicker().setMinDate(System.currentTimeMillis()-1000);picker.show();}).show();}
 void schedule(int id,String title,long time){Intent intent=new Intent(this,ReminderReceiver.class);intent.putExtra("id",id);intent.putExtra("title",title);PendingIntent pi=PendingIntent.getBroadcast(this,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi);}
}