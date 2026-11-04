package cc.meteormc.xposedkit.demo.xposed.hooker

import android.content.Context
import android.view.View
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.allocate
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.callOriginal
import cc.meteormc.xposedkit.callSpecial
import cc.meteormc.xposedkit.deoptimize
import cc.meteormc.xposedkit.findInstances
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.hook.InvokeCallback
import cc.meteormc.xposedkit.new
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect
import java.io.File
import java.lang.reflect.Modifier

object ExampleHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        String::class.reflect {
            val allocate = type.allocate()
            val instance = type.findInstances().first()
            XLog.d(tag, "allocate object: $allocate")
            XLog.d(tag, "current first instance: $instance")

            nestedClass("Nest")?.reflect {
                XLog.d(tag, "nestedClass: $type")
            }

            val ctor = constructor(Int::class.javaPrimitiveType!!) ?: throw IllegalStateException()
            ctor.deoptimize()
            ctor.new<Any>(Int.MAX_VALUE)
            ctor.call(allocate, 0)
            ctor.callOriginal(allocate, -1)

            val method = method("method") ?: throw IllegalStateException()
            method.deoptimize()
            method.call<View>(instance)
            method.callOriginal<View>(instance)
            method.callSpecial<View>(instance)

            val field = field("FIELD") ?: throw IllegalStateException()
            field.get<Any>(null)
            field.put(null, Any())
        }

        "com.example.Main".reflect {
            XLog.d(tag, "singleton: $singleton")

            type.hookClinitBefore {
                XLog.d(tag, "hookClinitBefore: ${it.instance}")
                it.cancel()
            }

            type.hookClinitAfter {
                XLog.d(tag, "hookClinitAfter")
            }

            constructor {
                modifiers = Modifier.PRIVATE
                annotations = arrayOf(Deprecated::class.java)
                paramCount = 3
            }?.hookBefore(InvokeCallback.PRIORITY_HIGHEST) {
                val file = it.findArg(File::class.java)
                val list = it.findArgIndexed<List<String>>()
                XLog.d(tag, "constructor called: $file, $list")

                val newList = list.value.toMutableList()
                newList.add("item")
                it.args[list.index] = newList
            }

            declaredConstructors.hookAfter {
                XLog.d(tag, "constructor called")
                XLog.d(tag, "result: ${it.result}")
                XLog.d(tag, "exception: ${it.exception}")
            }

            method(String::class.java)?.hookBefore {
                XLog.d(tag, "method called with args: ${it.args.contentToString()}")
                it.callSuper<String>()
                it.callOriginal<String>("abc")
                it.result = "XposedKit"
            }

            method {
                modifiers = Modifier.FINAL
                paramTypes = arrayOf(Context::class.java, null, Any::class.java)
                returnType = Unit::class.javaPrimitiveType
                isSynthetic = true
            }?.hookAfter(InvokeCallback.PRIORITY_LOW) {
                val context = it.findArg<Context>()
                val arg = it.args[1]
                val obj = it.argumentAt<Any>(2)
                XLog.d(tag, "method called: $context, $arg, $obj")
                throw UnsupportedOperationException("throw a custom exception")
            }

            field("com.example.Target".clazz!!)

            field {
                type = String::class.java
                modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
            }?.put(null, "New value")
        }
    }
}