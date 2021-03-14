package `in`.iot.lab.aco

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    lateinit var br: BroadcastReceiver
    lateinit var bluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1011
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filter)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter.isEnabled){
            bluetoothOn()
        }
        else {
            bluetoothOff()
        }

        confirmBtn.setOnClickListener {
            actionOnService(Actions.START)
        }
        stopBtn.setOnClickListener {
            actionOnService(Actions.STOP)
        }

        startDeviceBtn.setOnClickListener {
            BluetoothManager().send(1)
        }
        stopDeviceBtn.setOnClickListener {
            BluetoothManager().send(0)
        }

        turnOnBluetooth.setOnClickListener {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun actionOnService(action: Actions) {
        if(getServiceState(applicationContext)==ServiceState.STOPPED && action == Actions.STOP) {
            Toast.makeText(applicationContext, "Service not running!", Toast.LENGTH_SHORT).show()
            return
        }
        else if(getServiceState(applicationContext)==ServiceState.STARTED && action == Actions.START) {
            Toast.makeText(applicationContext, "Service already running!", Toast.LENGTH_SHORT).show()
            return
        }
        else if(getServiceState(applicationContext)==ServiceState.STOPPED && action == Actions.START && !bluetoothAdapter.isEnabled) {
            Toast.makeText(applicationContext, "Enable bluetooth first!", Toast.LENGTH_SHORT).show()
            return
        }

        val mIntent = Intent(this, ServiceStatusUpdate::class.java)
        val txt:String = txt.text.toString()
        mIntent.putExtra("val", txt)
        mIntent.also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(applicationContext, "Enable bluetooth first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bluetoothOn(){
        turnOnBluetooth.visibility = View.GONE
        bluetoothStatusText.text = "Status: Turned ON"
    }
    @SuppressLint("SetTextI18n")
    private fun bluetoothOff(){
        turnOnBluetooth.visibility = View.VISIBLE
        bluetoothStatusText.text = "Status: Turned OFF"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }

    //region BLUETOOTH BROADCAST RECEIVER
    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> bluetoothOff()
                    BluetoothAdapter.STATE_TURNING_OFF -> bluetoothOff()
                    BluetoothAdapter.STATE_ON -> bluetoothOn()
                    BluetoothAdapter.STATE_TURNING_ON -> bluetoothOn()
                }
            }
        }
    }
    //endregion
}