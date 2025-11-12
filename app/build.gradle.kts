import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
}

android {
    namespace = "com.cookandroid.ai_landaury"
    compileSdk = 34

    val kakaoKey: String = run {
        // 1) 프로젝트 루트의 local.properties
        val fromLocal = gradleLocalProperties(rootDir).getProperty("KAKAO_MAP_KEY")

        // 2) CI나 PC 환경변수
        val fromEnv = System.getenv("KAKAO_MAP_KEY")

        // 3) gradle.properties (전역 혹은 사용자 홈의 ~/.gradle/gradle.properties)
        val fromGradle = project.findProperty("KAKAO_MAP_KEY") as String?

        (fromLocal ?: fromEnv ?: fromGradle ?: "").trim()
    }

    defaultConfig {
        applicationId = "com.cookandroid.ai_landaury"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        if (kakaoKey.isBlank()) {
            throw GradleException(
                    """
                KAKAO_MAP_KEY가 설정되지 않았습니다.
                - local.properties: KAKAO_MAP_KEY=네이티브앱키
                - 또는 환경변수/gradle.properties로 주입
                """.trimIndent()
            )
        }

        // strings.xml 리소스로 주입 → 코드/Manifest에서 @string/kakao_map_api_key 사용
        resValue("string", "kakao_map_api_key", kakaoKey)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Core
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Kakao Maps
    implementation("com.kakao.maps.open:android:2.12.18")
}
