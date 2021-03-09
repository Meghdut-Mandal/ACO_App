package `in`.iot.lab.aco

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ServiceStatusUpdate : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private lateinit var chargeDetector: PowerConnectionReceiver

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            setServiceState(this, ServiceState.STARTED)
            val toMatch = intent.getStringExtra("val")
            when (intent.action) {
                Actions.START.name -> startService(toMatch.toString())
                Actions.STOP.name -> stopService()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun startService(toMatch: String) {
        if (isServiceStarted) return
        Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire(10*60*1000L /*10 minutes*/)
                }
            }
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    checkBattery(toMatch)
                }
                delay(1 * 60 * 1000)
            }
        }
    }

    private fun checkBattery(toMatch: String){
        chargeDetector = PowerConnectionReceiver(toMatch.toFloat())
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        this.registerReceiver(chargeDetector, filter)

    }

    private fun stopService() {
        if(!isServiceStarted) return;
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            unregisterReceiver(chargeDetector)
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
           e.printStackTrace()
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
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