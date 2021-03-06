package `in`.iot.lab.aco

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.Toast


class ServiceStatusUpdate : Service() {

    private lateinit var chargeDetector: PowerConnectionReceiver

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val toMatch = intent.getStringExtra("val")
            when (intent.action) {
                Actions.START.name -> startService(toMatch.toString())
                Actions.STOP.name -> stopService()
            }
            val iFilter = IntentFilter()
            iFilter.addAction("CHARGE DONE")
            iFilter.addAction("NOT CHARGING")
            registerReceiver(broadcastReceiver, iFilter);
        }
        return START_NOT_STICKY
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if(intent.action.equals("CHARGE DONE")) stopService()
            else if(intent.action.equals("NOT CHARGING")) stopServiceNotCharging()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun startService(toMatch: String) {
        setServiceState(this, ServiceState.STARTED)
        Toast.makeText(this, "Service Starting!", Toast.LENGTH_SHORT).show()
        checkBattery(toMatch)
    }


    private fun checkBattery(toMatch: String){
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)

        chargeDetector = PowerConnectionReceiver(ToMatch = toMatch.toFloat())
        registerReceiver(chargeDetector, filter)
    }

    private fun stopService() {
        setServiceState(this, ServiceState.STOPPED)
        Toast.makeText(this, "Service Stopping!", Toast.LENGTH_SHORT).show()
        stopForeground(true)
        stopSelf()
        unregisterReceiver(broadcastReceiver)
    }

    private fun stopServiceNotCharging() {
        setServiceState(this, ServiceState.STOPPED)
        Toast.makeText(this, "Device Was not charging! Service Stopping!", Toast.LENGTH_SHORT).show()
        stopForeground(true)
        stopSelf()
        unregisterReceiver(broadcastReceiver)
    }


    @SuppressLint("ServiceCast")
    private fun createNotification(): Notification {
        val notificationChannelId = "AOC SERVICE CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                    notificationChannelId,
                    "AOC Service notifications channel",
                    NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "AOC Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("AOC App")
            .setContentText("The Service is running")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .build()
    }
}