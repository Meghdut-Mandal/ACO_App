package `in`.iot.lab.aco

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val name = "AOC_SERVICE_KEY"
private const val key = "AOC_SERVICE_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    val value = sharedPrefs.getString(key, ServiceState.STOPPED.name)
    return ServiceState.valueOf(value.toString())
}

private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}