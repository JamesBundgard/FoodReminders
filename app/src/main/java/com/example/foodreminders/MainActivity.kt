package com.example.foodreminders

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var mFoodItems = ArrayList<FoodItem>()
    private val filename = "FoodItems.txt"
    private val delimiter = "~"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mFoodItems = getDataFromStorage()
        generateFoodList()

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            // Handler code here.
            val intent = Intent(this, AddFoodActivity::class.java)
            startActivity(intent)
        }
    }

    fun saveToFile(items: ArrayList<FoodItem>){
        var fileContents = ""
        for (item in items){
            fileContents += item.toString(delimiter)
            fileContents += "\n"
        }
        println("To file: "+fileContents)
        val x = openFileOutput(filename, Context.MODE_PRIVATE)
        x.write(fileContents.toByteArray())
        x.close()
    }

    private fun getDataFromStorage(): ArrayList<FoodItem> {
        val items = ArrayList<FoodItem>()
        try {
            val fileInputStream: FileInputStream? = openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); println("here:"+text); text }() != null) {
                stringBuilder.append(text).append("\n")
            }
            for (item in stringBuilder.toString().split("\n")){
                if (item.split(delimiter).size == 5) items.add(FoodItem.fromString(item, delimiter))
                else println(item)
            }
            fileInputStream?.close()
            inputStreamReader.close()
            bufferedReader.close()
        } catch (e: IOException) {
            println("at get "+e.message)
            saveToFile(mFoodItems)
        }
        return items
    }

    fun getDateFrom(time: String):Date{
        val day = Integer.parseInt(time.split("/")[0])
        val month = Integer.parseInt(time.split("/")[1])-1
        val year = Integer.parseInt(time.split("/")[2])

        val c = Calendar.getInstance()
        c.set(year, month, day)
        return c.time
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateFoodList() {
        try {
            val description = intent.getStringExtra("description")
            val quantity = intent.getStringExtra("quantity").toInt()
            val measurement = intent.getStringExtra("measurement")

            val expiry = getDateFrom(intent.getStringExtra("expiry"))
            val purchased = getDateFrom(intent.getStringExtra("purchase"))

            mFoodItems.add(FoodItem(quantity, measurement, expiry, description, purchased))
            saveToFile(mFoodItems)

        } catch (e: Exception) {
            println(e.message)
        }

        val list: LinearLayout = findViewById(R.id.food_list)
        list.removeAllViews()

        mFoodItems.sortWith(compareBy({it.ExpiryDate.time},{it.ExpiryDate.time}))
        println(mFoodItems[0].toString(""))

        for (item in mFoodItems){
            createNotification(mFoodItems[0])
            val foodView: LinearLayout = createFoodView(this, item)
            foodView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            list.addView(foodView)
        }
    }

    private fun createNotification(item: FoodItem){
        var time = item.ExpiryDate.time - 2*24*60*60*1000 //sends the notification 2 days before
        if (time <= System.currentTimeMillis()+1000) time = System.currentTimeMillis()+5000
        println(time-System.currentTimeMillis())
        NotificationUtils().setNotification(time, this@MainActivity)
        return
    }

    @SuppressLint("SetTextI18n")
    private fun createFoodView(context: Context, item: FoodItem): LinearLayout {
        val block = LinearLayout(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 10, 40, 10)

        val descView = TextView(this)
        descView.layoutParams = params
        descView.text = item.Description
        block.addView(descView)

        val quantView = TextView(this)
        quantView.layoutParams = params
        quantView.text = """${item.Quantity} ${item.Measurement}"""
        block.addView(quantView)

        val expiryView = TextView(this)
        expiryView.layoutParams = params
        println(item.ExpiryDate.toString())
        expiryView.text = android.text.format.DateFormat.format("EEE LLL dd, yyyy", item.ExpiryDate)
        block.addView(expiryView)

        val deleteButton = Button(this)
        deleteButton.layoutParams = params
        deleteButton.setOnClickListener {
            mFoodItems.remove(item)
            saveToFile(mFoodItems)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        deleteButton.text = "Del"
        block.addView(deleteButton)

        return block
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsMenu::class.java)
                startActivity(intent)
                return true
            }
            R.id.reload -> {
                mFoodItems = getDataFromStorage()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val service = Intent(context, NotificationService::class.java)
        service.putExtra("reason", intent.getStringExtra("reason"))
        service.putExtra("timestamp", intent.getLongExtra("timestamp", 0))

        context.startService(service)
    }
}

class NotificationService : IntentService("NotificationService") {
    private lateinit var mNotification: Notification
    private val mNotificationId: Int = 1000

    @SuppressLint("NewApi")
    private fun createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val context = this.applicationContext
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.parseColor("#e8334a")
            notificationChannel.description = getString(R.string.notification_channel_description)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "food.reminders.notification.id"
        const val CHANNEL_NAME = "Food expiry notifications"
    }

    override fun onHandleIntent(intent: Intent?) {

        createChannel()

        var timestamp: Long = 0
        if (intent != null && intent.extras != null) {
            timestamp = intent.extras!!.getLong("timestamp")
        }

        if (timestamp > 0) {

            val context = this.applicationContext
            val notifyIntent = Intent(this, MainActivity::class.java)
            val title = "You have food expiring soon!"
            val message = "Press here to see which items are expiring."

            notifyIntent.putExtra("title", title)
            notifyIntent.putExtra("message", message)
            notifyIntent.putExtra("notification", true)
            notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp

            val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val res = this.resources

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotification = Notification.Builder(this, CHANNEL_ID)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(Notification.BigTextStyle()
                        .bigText(message))
                    .setContentText(message).build()
            }
            else
            {
                mNotification = Notification.Builder(this)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle(title)
                    .setStyle(
                        Notification.BigTextStyle()
                        .bigText(message))
                    .setContentText(message).build()
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // mNotificationId is a unique int for each notification that you must define
            notificationManager.notify(mNotificationId, mNotification)
        }
    }
}


class NotificationUtils {

    fun setNotification(timeInMilliSeconds: Long, activity: Activity) {
        if (timeInMilliSeconds > 0) {
            val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(activity.applicationContext, AlarmReceiver::class.java) // AlarmReceiver1 = broadcast receiver

            alarmIntent.putExtra("reason", "notification")
            alarmIntent.putExtra("timestamp", timeInMilliSeconds)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMilliSeconds

            val pendingIntent = PendingIntent.getBroadcast(activity, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}
