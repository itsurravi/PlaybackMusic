import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Locale
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.sevices)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.protoPlugin)
}
val keystorePropertiesFile = rootProject.file("local.properties")

android {
    namespace = "com.ravisharma.playbackmusic"

    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.ravisharma.playbackmusic"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties().apply {
                    load(keystorePropertiesFile.inputStream())
                }

                storeFile = file(keystoreProperties["KEYSTORE_FILE"] as String)
                storePassword = keystoreProperties["KEYSTORE_PASSWORD"] as String
                keyPassword = keystoreProperties["KEY_PASSWORD"] as String
                keyAlias = keystoreProperties["KEY_ALIAS"] as String
            } else {
                // Fetch values from environment variables
                storeFile = file(System.getenv("KEYSTORE_FILE"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyPassword = System.getenv("KEY_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
            }
            enableV1Signing = false
            enableV2Signing = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            firebaseCrashlytics {
//                mappingFileUploadEnabled = true
//            }
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-d"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = (libs.versions.jvm.get())
    }
}
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val capName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            val kspTaskName = "ksp${capName}Kotlin"
            val protoTaskName = "generate${capName}Proto"

            val kspTask = tasks.named(kspTaskName)
            val protoTask = tasks.named(protoTaskName)

            kspTask.configure {
                dependsOn(protoTask)
            }
        }
    }
}
dependencies {
    implementation(project(":data"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    //Added Dependencies
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore)
    implementation(libs.proto)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    ksp(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)


    implementation(libs.androidx.media)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.coil)

    implementation(libs.com.github.ybq.android.spinKit)
    implementation(libs.com.recyclerview.fastscroll)
    implementation(libs.com.slidinguppanel)
    implementation(libs.com.verticalseekbar)

    implementation(libs.media3Ui)
    implementation(libs.media3ExoPlayer)
    implementation(libs.media3Session)

    implementation(libs.androidx.legacy.supportV4)

    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)

    implementation(libs.google.ads)
    implementation(libs.google.material)
    implementation(libs.google.gson)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    implementation(libs.androidx.core.splash)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.glide)
}
allprojects {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
        }
    }
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protoVersion.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
                val kotlin by registering {
                    option("lite")
                }
            }
        }
    }
}
