package hardkernel.Updater.Logic

import android.util.Log

object ServerManager {
    private val OFFICIAL: String
    private val MIRROR: String
    private var CUSTOM: String?

    private val OFFICIAL_PROPERTY = "ro.url.official"
    private val MIRROR_PROPERTY = "ro.url.mirror"
    private val CUSTOM_PROPERTY = "persist.update.server.custom"
    private val CURRENT_SERVER_PROPERTY = "persist.update.server.current"

    private val DEFAULT_SERVER = "https://dn.odroid.com/RK3568/ODROID-M1/Android/11/"
    private val DEFAULT_MIRROR_SERVER = "https://www.odroid.in/mirror/dn.odroid.com/RK3568/ODROID-M1/Android/11/"

    enum class Server {
        Official,
        Mirror,
        Custom
    }
    private var url: String?
    private var current: Server

    init {
        OFFICIAL = SysProperty.get(OFFICIAL_PROPERTY, DEFAULT_SERVER)
        MIRROR = SysProperty.get(MIRROR_PROPERTY, DEFAULT_MIRROR_SERVER)
        CUSTOM = SysProperty.get(CUSTOM_PROPERTY, "")

        val server = SysProperty.get(CURRENT_SERVER_PROPERTY,"Official")

        url = when (server) {
            Server.Official.name -> {
                current = Server.Official
                OFFICIAL
            }
            Server.Mirror.name -> {
                current = Server.Mirror
                MIRROR
            }
            Server.Custom.name -> {
                current = Server.Custom
                CUSTOM
            }
            else -> {
                Log.d(this.javaClass.name, "Wrong URL")
                throw IllegalArgumentException("Wrong URL")
                ""
            }
        }
    }

    private fun saveCurrent(server: Server) {
        current = server
        SysProperty.set(CURRENT_SERVER_PROPERTY, server.name)
    }

    fun getCustomURL(): String? {
        return CUSTOM
    }

    fun getURL(): String? {
        return url
    }

    fun getCurrent(): Server {
        return current
    }

    fun setOfficial() {
        url = OFFICIAL
        saveCurrent(Server.Official)
    }

    fun setMirror() {
        url = MIRROR
        saveCurrent(Server.Mirror)
    }

    fun setCustom(url: String) {
        this.url = url
        SysProperty.set(CUSTOM_PROPERTY, url)
        saveCurrent(Server.Custom)
    }

    fun getOfficialURL(): String {
        return OFFICIAL
    }

    fun getMirrorURL(): String {
        return MIRROR
    }
}
