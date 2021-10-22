package hardkernel.Updater.Logic

import android.os.SystemProperties

object SysProperty {
    fun get(key: String, default: Int): Int {
        return SystemProperties.getInt(key, default)
    }
    fun get(key: String, default: String): String {
        return SystemProperties.get(key, default)
    }

    fun get (key: String, default: Boolean): Boolean {
        return SystemProperties.getBoolean(key, default)
    }

    fun set (key: String, value: String) {
        SystemProperties.set(key, value)
    }
}