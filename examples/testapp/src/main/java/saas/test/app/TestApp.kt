package saas.test.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import saas.test.app.framework.di.dataAccessModule
import saas.test.app.framework.di.repositoriesModule
import saas.test.app.framework.di.useCasesModule
import saas.test.app.framework.di.viewModelsModule
import saas.test.app.framework.utils.AppConst
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.io.File

class TestApp : Application() {

    companion object {
        lateinit var instance: TestApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        instance = this

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(applicationContext)
            modules(
                dataAccessModule,
                repositoriesModule,
                useCasesModule,
                viewModelsModule
            )
        }
    }

    fun clearData() {
        val appDir = File(cacheDir.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (subDirectory in children) {
                if (subDirectory != "lib" && subDirectory != "shared_prefs") {
                    deleteDir(File(appDir, subDirectory))
                    Timber.i("%s DELETED", "File /data/data/APP_PACKAGE/$subDirectory")
                }
            }

            /**
             * Will clear out shared preferences (only it's data, not the file)
             */
            val masterPreferences = getSharedPreferences(
                AppConst.SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            ).edit()
            masterPreferences.clear().apply()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children) {
                val success = deleteDir(File(dir, i))
                if (!success) return false
            }
        }

        return dir!!.delete()
    }

    fun triggerAppReload(context: Context) {
        val packageManager: PackageManager = context.packageManager
        val intent: Intent? = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName: ComponentName? = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}