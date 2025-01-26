<h1 align="center">Flow Operators</h1></br>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/skydoves/flow-operators/actions/workflows/android.yml"><img alt="Build Status" 
  src="https://github.com/skydoves/flow-operators/actions/workflows/android.yml/badge.svg"/></a>
  <a href="https://github.com/skydoves"><img alt="Profile" src="https://skydoves.github.io/badges/skydoves.svg"/></a>
  <a href="https://github.com/doveletter"><img alt="Profile" src="https://skydoves.github.io/badges/dove-letter.svg"/></a>
</p><br>

<p align="center">🌊 Flow operators enable you to create restartable, pausable, or one-shot StateFlow, and they support KMP. </p>

## Flow Operators

Flow operators that enable you to create restartable, pausable, or one-shot `StateFlow` instances, allowing you to customize and control additional behaviors for `StateFlow` based on your specific use case. Why are flow operators useful? You can manage state and control data streams efficiently in complex scenarios. You can more about the reasons in [Loading Initial Data on Android Part 2: Clear All Your Doubts](https://medium.com/proandroiddev/loading-initial-data-part-2-clear-all-your-doubts-0f621bfd06a0).

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/flow-operators.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22flow-operators%22)

### Version Catalog

If you're using Version Catalog, you can configure the dependency by adding it to your `libs.versions.toml` file as follows:

```toml
[versions]
#...
flowOperators = "0.1.0"

[libraries]
#...
flow-operators = { module = "com.github.skydoves:flow-operators", version.ref = "flowOperators" }
```

### Gradle
Add the dependency below to your **module**'s `build.gradle.kts` file:

```gradle
dependencies {
    implementation("com.github.skydoves:flow-operators:$version")
    
    // if you're using Version Catalog
    implementation(libs.flow.operators)
}
```

For Kotlin Multiplatform, add the dependency below to your **module**'s `build.gradle.kts` file:

```gradle
sourceSets {
    val commonMain by getting {
        dependencies {
            implementation("com.github.skydoves:flow-operators:$version")
        }
    }
}
```

### RestartableStateFlow

`RestartableStateFlow` extends both `StateFlow` and `Restartable`, allowing the upstream flow to restart its emission. It behaves like a regular `StateFlow` but includes the ability to reset and restart the emission process when necessary.

Consider a scenario where you load initial data using a delegate property, as shown in the example below, instead of initiating the load inside `LaunchedEffect` or `ViewModel.init()`. (For more details, check out [Loading Initial Data in LaunchedEffect vs. ViewModel](https://medium.com/proandroiddev/loading-initial-data-in-launchedeffect-vs-viewmodel-f1747c20ce62)).

```kotlin
val posters: StateFlow<List<Poster>> = mainRepository.fetchPostersFlow()
.filter { it.isSuccess }
.mapLatest { it.getOrThrow() }
.restartableStateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
)
```

By using a delegate property, the data is loaded only when the first subscription occurs, preventing the unnecessary immediate execution of tasks and avoiding unintended side effects that might arise from `ViewModel.init()` or `LaunchedEffect`. However, another challenge arises: you may need to reload the data due to scenarios like refreshing, recovering from errors during the initial load, or other reasons. In such cases, you can seamlessly restart the upstream flow using `RestartableStateFlow`.

```kotlin
class MainViewModel(mainRepository: MainRepository) : ViewModel() {

  private val restartablePoster: RestartableStateFlow<List<Poster>> = mainRepository.fetchPostersFlow()
    .filter { it.isSuccess }
    .mapLatest { it.getOrThrow() }
    .restartableStateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val posters: StateFlow<List<Poster>> = restartablePoster // don't expose the Restartable interface to the outside

  fun refresh() = restartablePoster.restart()
}
```

Now, you can easily restart the upstream flow and reload the initial task using the delegated property.

```kotlin
@Composable
private fun Main(mainViewModel: MainViewModel) {
  val posters by mainViewModel.posters.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(6.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    Button(onClick = { mainViewModel.restart() }) {
      Text(text = "restart")
    }

    Text(text = posters.toString())
  }
}
```

### PausableStateFlow

`PausableStateFlow` extends both `StateFlow` and `PausableStateFlow`, enabling the upstream flow to pause and resume its emission. It retains the functionality of a standard `StateFlow` while adding controls for pausing and resuming emissions as needed.

The core concept of `PausableStateFlow` is similar to `RestartableStateFlow`, but with an added capability: it allows you to pause and resume listening to the upstream flow. This can be particularly useful in scenarios where you want to temporarily stop processing updates from an upstream flow, such as real-time location updates, Bluetooth connection status, animations, or other continuous events. 

While paused, any new subscribers to the `PausableStateFlow` will simply receive the latest cached value instead of actively listening to the upstream emissions.

```kotlin
class MainViewModel(mainRepository: MainRepository) : ViewModel() {
  
  private val pausableStateFlow: PausableStateFlow<List<Poster>> =
    mainRepository.fetchPostersFlow()
      .filter { it.isSuccess }
      .mapLatest { it.getOrThrow() }
      .pausableStateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
      )

  val posters: StateFlow<List<Poster>> = pausableStateFlow

  fun pause() = pausableStateFlow.pause()
  
  fun resume() = pausableStateFlow.resume()
}
```

### OnetimeWhileSubscribed

`OnetimeWhileSubscribed` is a `SharingStarted` strategy that ensures the upstream flow emits only once while a subscriber is active. After the initial emission, it remains idle until another active subscription appears.

When converting a cold flow into a hot flow using `stateIn` on Android, a common approach is to use `SharingStarted.WhileSubscribed(5_000)` with the `stateIn` function. The 5-second threshold (5_000) aligns with the ANR (Application Not Responding) timeout limit. If no subscribers remain for longer than 5 seconds, the timeout is exceeded, and the upstream data flow ceases to influence your UI layer.

However, this can lead to another side-effect: if you navigate from Screen A to Screen B and remain on Screen B for over 5 seconds, returning to Screen A will restart the upstream flow. This causes the same task to relaunch, even if it was already completed. To avoid this, you can use `OnetimeWhileSubscribed` as a `SharingStarted` strategy. It ensures the upstream flow is launched only once when the first subscription occurs, and subsequently, only the latest cached value is replayed, avoiding redundant task restarts during screen transitions.

```kotlin
class MainViewModel(mainRepository: MainRepository) : ViewModel() {
  
  private val posters: StateFlow<List<Poster>> =
    mainRepository.fetchPostersFlow()
      .filter { it.isSuccess }
      .mapLatest { it.getOrThrow() }
      .stateIn(
        scope = viewModelScope,
        started = OnetimeWhileSubscribed(5_000),
        initialValue = emptyList(),
      )
}
```

## Find this repository useful? :heart:
Support it by joining __[stargazers](https://github.com/skydoves/flow-operators/stargazers)__ for this repository. :star: <br>
Also, __[follow me](https://github.com/skydoves)__ on GitHub for my next creations! 🤩

# License
```xml
Designed and developed by 2025 skydoves (Jaewoong Eum)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
