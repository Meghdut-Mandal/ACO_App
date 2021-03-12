package `in`.iot.lab.aco

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.nio.charset.Charset
import java.util.*


class BluetoothManager {

    fun send(text: String) {
        val adapter = BluetoothAdapter.getDefaultAdapter();
        val pairedDevices = adapter.bondedDevices
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                var s = device.name
                Log.i("AOC_APP",device.name)
                if (device.name.equals("hc05", ignoreCase = true)) {
                    Thread {
                        val socket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                        val clazz = socket.remoteDevice.javaClass
                        val paramTypes = arrayOf<Class<*>>(Integer.TYPE)
                        val m = clazz.getMethod("createRfcommSocket", *paramTypes)
                        val fallbackSocket = m.invoke(socket.remoteDevice, Integer.valueOf(1)) as BluetoothSocket
                        try {
                            fallbackSocket.connect()
                            val stream = fallbackSocket.outputStream
                            stream.write(text.toByteArray(Charset.forName("UTF-8")))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.start()
                }
            }
        }
    }
}