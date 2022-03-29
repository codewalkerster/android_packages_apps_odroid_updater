package hardkernel.Updater.Logic

// Default package Name - upcatepackage-<model_name>-<BuildNumaber>-<BuildDate>.zip
class PackageName(packageName: String) {
    val version: Int

    private val HEADER = "updatepackage"
    private val MODEL: String = SysProperty.get("ro.build.product", "odroid")

    init {
        val num = parseBuildNumber(packageName)
        if ( num!= null) let {
            version = num
        } else {
            throw Exception("Wrong package")
        }
    }

    private fun parseBuildNumber(packageName: String): Int? {
        val stub = packageName.split("-")
        if (stub.size <= 2)
            return null

        if (stub[0] != HEADER ||
            stub[1] != MODEL)
                return null

        return stub[2].split(".")[0].toInt()
    }
}
