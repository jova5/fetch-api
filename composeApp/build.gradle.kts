import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

kotlin {
  jvm {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(compose.materialIconsExtended)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.kotlinx.serializationJson)
      implementation(libs.grpc.netty.shaded)
      implementation(libs.grpc.protobuf)
      implementation(libs.grpc.stub)
      implementation(libs.grpc.kotlin.stub)
      implementation(libs.protobuf.java)
      implementation(libs.protobuf.java.util)
      implementation(libs.sqldelight.sqlite.driver)
      implementation(libs.sqlite)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.java)
      implementation(libs.ktor.client.logging)
      implementation(libs.ktor.client.encoding)
      implementation(libs.jna)
      implementation(libs.jna.platform)
      implementation(libs.jewel.int.ui.decorated.window)
      implementation(libs.codeeditor)
      implementation(libs.jsoup)
      implementation(libs.reorderable)
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

sqldelight {
  databases {
    create("FetchApiDatabase") {
      packageName.set("ba.fluxor.fetchapi.db")
      dialect(libs.sqldelight.sqlite.dialect)
      deriveSchemaFromMigrations.set(false)
      srcDirs("src/jvmMain/sqldelight")
    }
  }
}

compose.desktop {
  application {
    mainClass = "ba.fluxor.fetchapi.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "ba.fluxor.fetchapi"
      packageVersion = "1.0.0"
    }
  }
}
