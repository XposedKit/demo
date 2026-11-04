package cc.meteormc.xposedkit.template.xposed

import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.xposedkit.template.xposed.hooker.TemplateHooker

@ModuleRegister(
    targetApi = 102,
    staticScope = true
)
object XposedKitTemplate : XposedModule {
    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage) return
        val context = HookerContext(param.classLoader)
        TemplateHooker.installHook(context)
    }
}