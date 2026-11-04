plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.xposedkit)
}

android {
    namespace = "cc.meteormc.xposedkit.template.xposed"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":api"))
}