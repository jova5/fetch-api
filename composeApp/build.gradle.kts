import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.kotlinSerialization)
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
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.kotlinx.serializationJson)
      implementation(libs.grpc.netty.shaded)
      implementation(libs.grpc.protobuf)
      implementation(libs.grpc.stub)
      implementation(libs.grpc.kotlin.stub)
      implementation(libs.protobuf.java)
      implementation(libs.protobuf.java.util)
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
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
