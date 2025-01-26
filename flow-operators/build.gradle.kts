/*
 * Designed and developed by 2025 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.skydoves.flow.operators.Configuration

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  id(libs.plugins.nexus.plugin.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
  val artifactId = "flow-operators"
  coordinates(
    Configuration.artifactGroup,
    artifactId,
    rootProject.extra.get("libVersion").toString()
  )

  pom {
    name.set(artifactId)
    description.set("Flow extensions enable you to create restartable, pausable, or one-shot StateFlow.")
  }
}

kotlin {
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
    macosArm64(),
    macosX64(),
  ).forEach {
    it.binaries.framework {
      baseName = "common"
    }
  }

  androidTarget {
    publishLibraryVariants("release")
  }

  jvm {
    libs.versions.jvmTarget.get().toInt()
    compilations.all {
      kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    all {
      languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
    }
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotlinx.test)
        implementation(libs.turbine)
      }
    }
  }

  explicitApi()
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.flow.operators"

  defaultConfig {
    minSdk = Configuration.minSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {

}
