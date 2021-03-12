package `in`.iot.lab.aco

import android.content.*
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    lateinit var br: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener {
            actionOnService(Actions.START)
        }
        stopBtn.setOnClickListener {
            actionOnService(Actions.STOP)
        }
    }

    private fun actionOnService(action: Actions) {
        if(getServiceState(applicationContext)==ServiceState.STOPPED && action == Actions.STOP) {
            Toast.makeText(applicationContext,"Service not running!",Toast.LENGTH_SHORT).show()
            return
        }
        else if(getServiceState(applicationContext)==ServiceState.STARTED && action == Actions.START) {
            Toast.makeText(applicationContext,"Service already running!",Toast.LENGTH_SHORT).show()
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
}