
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.jaredsburrows.license")
}

android {
    namespace = "com.v2ray.ang"
    compileSdk = 37

    defaultConfig {
        applicationId = "ru.mchouse.vpn"
        minSdk = 24
        targetSdk = 37
        versionCode = 755
        versionName = "2.3.15"

        val abiFilterList =
            (properties["ABI_FILTERS"] as? String)?.split(';')

        splits {
            abi {
                isEnable = true
                reset()

                if (!abiFilterList.isNullOrEmpty()) {
                    include(*abiFilterList.toTypedArray())
                } else {
                    include(
                        "arm64-v8a",
                        "armeabi-v7a",
                        "x86_64",
                        "x86"
                    )
                }

                isUniversalApk = abiFilterList.isNullOrEmpty()
            }
        }

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions.add("distribution")

    productFlavors {
        create("fdroid") {
            dimension = "distribution"
            applicationIdSuffix = ".fdroid"

            buildConfigField(
                "String",
                "DISTRIBUTION",
                "\"F-Droid\""
            )
        }

        create("playstore") {
            dimension = "distribution"

            buildConfigField(
                "String",
                "DISTRIBUTION",
                "\"Play Store\""
            )
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            )
        }
    }

    applicationVariants.all {
        val variant = this

        val isFdroid =
            variant.productFlavors.any {
                it.name == "fdroid"
            }

        if (isFdroid) {
            val versionCodes = mapOf(
                "armeabi-v7a" to 2,
                "arm64-v8a" to 1,
                "x86" to 4,
                "x86_64" to 3,
                "universal" to 0
            )

            variant.outputs
                .map {
                    it as com.android.build.gradle.internal.api.ApkVariantOutputImpl
                }
                .forEach { output ->

                    val abi =
                        output.getFilter("ABI") ?: "universal"

                    output.outputFileName =
                        "MC-HOUSE-VPN_${variant.versionName}-fdroid_${abi}.apk"

                    if (versionCodes.containsKey(abi)) {
                        output.versionCodeOverride =
                            (100 * variant.versionCode +
                                versionCodes[abi]!!).plus(5000000)
                    } else {
                        return@forEach
                    }
                }
        } else {
            val versionCodes = mapOf(
                "armeabi-v7a" to 4,
                "arm64-v8a" to 4,
                "x86" to 4,
                "x86_64" to 4,
                "universal" to 4
            )

            variant.outputs
                .map {
                    it as com.android.build.gradle.internal.api.ApkVariantOutputImpl
                }
                .forEach { output ->

                    val abi =
                        output.getFilter("ABI") ?: "universal"

                    output.outputFileName =
                        "MC-HOUSE-VPN_${variant.versionName}_${abi}.apk"

                    if (versionCodes.containsKey(abi)) {
                        output.versionCodeOverride =
                            (1000000 * versionCodes[abi]!!)
                                .plus(variant.versionCode)
                    } else {
                        return@forEach
                    }
                }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    androidResources {
        generateLocaleConfig = true

        localeFilters += listOf(
            "en",
            "zh-rCN",
            "zh-rTW",
            "vi",
            "ru",
            "fa",
            "ar",
            "az",
            "bn",
            "bqi-rIR"
        )
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()

            // Preserve original JUnit discovery validation.
            it.systemProperty(
                "junit.platform.discovery.issue.severity.critical",
                "WARNING"
            )
        }
    }
}

dependencies {
    // Core libraries
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.aar", "*.jar")
            )
        )
    )

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Data and storage
    implementation(libs.mmkv.static)
    implementation(libs.gson)
    implementation(libs.okhttp)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // QR code
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.compose)
    implementation(libs.core)

    // Lifecycle and ViewModel
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    // Background work
    implementation(libs.work.runtime.ktx)
    implementation(libs.work.multiprocess)

    // Reorderable lists
    implementation(libs.reorderable)

    // Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
