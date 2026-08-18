# omni-mvi ![](https://img.shields.io/badge/mvi_version-1.9.0-004475) ![](https://img.shields.io/badge/coverage-90%25-004475)
Omni MVI is a lightweight set of tools inspired by [Orbit](https://orbit-mvi.org) that allows Kotlin/Java developers to turn any object into an MVI-like object. It provides a simple and flexible way to manage UI state and side effects in your applications using Kotlin Coroutines.

## Installation
In order to include omni-mvi, add the following dependencies to your project's `build.gradle` file:

```groovy
dependencies {
    // Core library
    implementation("net.asere.omni.mvi:core:$version")
    // MVI features
    implementation("net.asere.omni.mvi:mvi:$version")
    
    // Optional: Android specific extensions (e.g. SaveableStateContainer)
    implementation("net.asere.omni.mvi:android:$version")
    
    // Optional: Plugins
    implementation("net.asere.omni.mvi:lock:$version") // Thread-safe state modifications
    implementation("net.asere.omni.mvi:queue:$version") // Intent queuing
    implementation("net.asere.omni.mvi:pending:$version") // Track pending intents
    implementation("net.asere.omni.mvi:override:$version") // Replace previous intents
    
    // Testing
    testImplementation("net.asere.omni.mvi:test:$version")
}
```
# Getting started

## 1. Create a State, Effect, and Action
First, define the state your UI will represent, the one-off effects (like navigation or toasts) it can emit, and the actions that can be triggered from the UI.

```kotlin
data class MyState(
    val isLoading: Boolean = false,
    val data: String = ""
)

sealed class MyEffect {
    data class ShowToast(val message: String) : MyEffect()
}

sealed class MyAction {
    data object LoadData : MyAction()
    data class Query(val text: String) : MyAction()
}
```

## 2. Implement a `StateContainerHost` and handle Actions
Your ViewModel or presenter should implement `StateContainerHost`. You'll need to provide a `StateContainer` instance. We recommend following an action pattern by creating an `ActionHost` interface or similar to route UI actions through a single entry point.

```kotlin
import net.asere.omni.mvi.*

interface ActionHost<Action> {
    fun on(action: Action)
}

class MyViewModel : StateContainerHost<MyState, MyEffect>, ActionHost<MyAction> {
    
    // Create the container with an initial state
    override val container = stateContainer(MyState())

    // Route actions to intents
    override fun on(action: MyAction) {
        when (action) {
            MyAction.LoadData -> loadData()
            is MyAction.Query -> onQuery(action.text)
        }
    }

    // Launch intents to perform actions
    private fun loadData() = intent {
        // Update state using reduce
        reduce { copy(isLoading = true) }
        
        try {
            val result = repository.fetchData() // suspending call
            reduce { copy(isLoading = false, data = result) }
            // Post an effect
            post(MyEffect.ShowToast("Data loaded!"))
        } catch (e: Exception) {
            reduce { copy(isLoading = false) }
            post(MyEffect.ShowToast("Error loading data"))
        }
    }
    
    private fun onQuery(text: String) = intent {
       // ... Handle query
    }
}
```

## 3. Observe State and Effects
You can observe the state and effects in your View (Activity/Fragment) or Compose UI. And send actions back to the ViewModel.

```kotlin
val viewModel = MyViewModel()

// Observe state
viewModel.observeState { state ->
    println("Current state: $state")
}

// Observe effects
viewModel.observeEffect { effect ->
    when(effect) {
        is MyEffect.ShowToast -> println("Toast: ${effect.message}")
    }
}

// Trigger action from UI
viewModel.on(MyAction.LoadData)
```

## Core Concepts

*   **`StateContainer`**: Holds the current `State` and a flow of `Effect`s.
*   **`StateContainerHost`**: An interface for classes that host a `StateContainer` (typically a ViewModel). It provides the `intent` builder.
*   **`intent { ... }`**: A coroutine builder that runs within an `IntentScope`. It allows you to perform async operations, modify state, and post effects.
*   **`reduce { ... }`**: A function available inside an `intent` block used to update the state synchronously. It receives the current state as `this` and expects you to return the new state.
*   **`post(...)`**: A function available inside an `intent` block to emit one-off side effects.
*   **Actions**: Using an action pattern (`ActionHost`) helps keep your ViewModel API clean and provides a single entry point for all UI interactions.

Check the [Wiki](https://github.com/Asere/omni-mvi/wiki) for more detailed documentation and advanced usage.

# License - MIT

Copyright 2025 Asere.net

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
