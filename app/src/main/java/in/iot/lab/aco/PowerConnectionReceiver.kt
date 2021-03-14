package `in`.iot.lab.aco

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import kotlin.math.floor


class PowerConnectionReceiver(private val ToMatch: Float) : BroadcastReceiver() {

    @SuppressLint("SetTextI18n")
    override fun onReceive(context: Context, batteryStatus: Intent) {
        val status: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val acCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

        val batteryPct: Double = batteryStatus.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            floor(level * 100 / scale.toDouble())
        }
        if(batteryPct >= ToMatch){
            if(isCharging) {
                val temp = BluetoothManager()
                temp.send(0)
                context.sendBroadcast(Intent("CHARGE DONE"));
                context.unregisterReceiver(this)
            }
            else {
                context.sendBroadcast(Intent("NOT CHARGING"));
                context.unregisterReceiver(this)
            }
        }
    }
}