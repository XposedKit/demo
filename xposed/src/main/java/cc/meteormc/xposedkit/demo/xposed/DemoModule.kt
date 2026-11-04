package cc.meteormc.xposedkit.demo.xposed

import android.os.Build
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.XposedKit
import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.demo.xposed.hooker.ExampleHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.param.HotReloadingParam
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.xposedkit.param.ProcessLoadedParam
import cc.meteormc.xposedkit.param.SystemServerStartingParam

@ModuleRegister(
    targetApi = 102,
    staticScope = true
)
object DemoModule : XposedModule {
    private const val TAG = "XposedKitTemplate"

    override fun onProcessLoaded(param: ProcessLoadedParam) {
        XLog.d(TAG, "onProcessLoaded: processName=${param.processName}, isSystemServer=${param.isSystemServer}")
        if (param.isHotReload) {
            XLog.d(TAG, "onProcessLoaded: hotReloadInfo=${param.hotReloadInfo}")
        }

        XLog.i(TAG, "--- Framework info ---")
        XLog.i(TAG, "Api version: ${XposedKit.apiVersion}")
        XLog.i(TAG, "Name: ${XposedKit.frameworkName}")
        XLog.i(TAG, "Version: ${XposedKit.frameworkVersion}")
        XLog.i(TAG, "Version code: ${XposedKit.frameworkVersionCode}")
        XLog.i(TAG, "Properties: ${XposedKit.frameworkProperties}")
        XLog.i(TAG, "----------------------")

        XLog.i(TAG, "--- Module info ---")
        XLog.i(TAG, "Source path: ${XposedKit.moduleSource}")
        XLog.i(TAG, "App info: ${XposedKit.moduleAppInfo}")
        XLog.i(TAG, "Package info: ${XposedKit.modulePackageInfo}")
        XLog.i(TAG, "Package name: ${XposedKit.modulePackageName}")
        XLog.i(TAG, "-------------------")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        val appInfo = param.appInfo
        XLog.d(TAG, "onPackageLoaded: processName=${param.processName}, packageName=${param.packageName}")
        XLog.d(TAG, "onPackageLoaded: classLoader=${param.classLoader}, acf=${param.componentFactory}, isFirstPackage=${param.isFirstPackage}")
        XLog.d(TAG, "onPackageLoaded: appInfo=$appInfo, sourceDir=${appInfo.sourceDir}")

        if (!param.isFirstPackage) return
        val context = HookerContext(param.classLoader)
        ExampleHooker.installHook(context)

        XposedKit.registerAppAttachListener(param.packageName) {
            XLog.i(TAG, "Application attached: $it")

            val pm = it.packageManager
            val label = appInfo.loadLabel(pm)
            val packageInfo = pm.getPackageInfo(it.packageName, 0)
            XLog.i(TAG, "--- App info ---")
            XLog.i(TAG, "Name: $label")
            XLog.i(TAG, "Version: ${packageInfo.versionName}")
            XLog.i(TAG, "Version code: ${
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }
            }")
            XLog.i(TAG, "----------------")
        }

        val resources = XposedKit.createModuleResources()
        val str = resources.getString(R.string.hello_world)
        XLog.i(TAG, "Loaded string resource: $str")
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        XLog.d(TAG, "onSystemServerStarting: classLoader=${param.classLoader}")
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        XLog.d(TAG, "onHotReloading: processName=${param.processName} extras=${param.extras}")
        param.savedData = "SavedData"
        return true
    }
}