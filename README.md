# omni-mvi
Omni MVI is a light weight set of tools inspired by [Orbit](https://orbit-mvi.org) that allows Kotlin/Java developer turn any object into a MVI like object.

## Installation
In order to include omni-mvi, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:1.0")
}
```

## Container
Containers are the core unit of Omni MVI. You can turn any class into a container host by implementing its interface.

```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(), StateContainerHost<ListState, ListEffect, ListAction>
```

Every container host needs 3 generic parameter which define the interaction with the view:

- UiState: defines the state of the view
- SideEffect: defines executions side effects
- UiAction: defines received actions from the view

Once implemented, it will force you override the container. Use the container builder function
`stateContainer()` to create it.

```kotlin
override val container = stateContainer(
    initialState = ListState()
)
```

The `stateContainer()` builder function requires you to pass a mandatory `initialState` and 3 optional arguments:

- onAction: Triggers when ever an action occurs in the view
- coroutineScope: Coroutine scope of intents execution. Defaulted to empty context coroutine scope
- coroutineExceptionHandler: The exception handler used to capture exception on intents execution.

```kotlin
override val container = stateContainer(
    initialState = ListState(),
    onAction = ::onAction,
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
)
```

Once your container is created, your are ready to go. Now you can start listening to incoming actions and react to them.

## Intents
Intents are suspending jobs running under a specific scope which grants access to the state, effects and possible thrown errors. To define a Omni intent you just have to follow the syntax bellow:

```kotlin
private fun fetchContent() = intent {
    onError {
        postEffect(ListEffect.ShowMessage(it.requireMessage()))
    }
    postState { copy(loading = true) }
    val repos = getRepositories(currentState.currentPage)
    postState { copy(loading = false, items = items + repos.items) }
}
```

The `intent` DSL allows you execute controlled block of code and grants you special access to state and effects publication, also lets you react to errors caught during execution.

- onError: This block is called when an exception is caught
- postState: Post a state to the view
- postEffect: Post an effect to the view

### Nesting intents
Intents are jobs. When you invoke an intent you are executing a coroutine job.

### Intent invocation
When intents are invoked they do it under `CoroutineStart.DEFAULT` start configuration, which in normal circumstances, will immediately execute the coroutine. Intents could also be delayed just as you would do with jobs:

```kotlin
private fun fetchDataAfter(time: Int) = intent {
    val pendingIntent = intent(start = CoroutineStart.LAZY) {
        Log.d("Lazy", "Job executed after $time")
    }
    delay(time)
    pendingIntent.start()
}
```
Intents could be directly nested or called.
```kotlin
private fun performFetch() = intent(start = CoroutineStart.LAZY) {
    Log.d("Lazy", "Fetch job executed")
}

private fun fetchDataAfter(time: Int) = intent {
    val pendingIntent = performFetch()
    delay(time)
    pendingIntent.start()
}
```

## Actions
You can deliver actions to your container host through the extension `on()`
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize()
) {
    itemsIndexed(state.items) { index, item ->
        if (index >= state.items.lastIndex - 10) {
            viewModel.on(ListAction.NextPage) // Call next page intent
        }
        RepoItem(repo = item)
    }
}
```

## Testing
As Omni MVI only adds an extra layer of behaviour to underlying coroutines infrastructure you don't need (until now) any special feature for testing. You can implement tests just like you would do it without Omni MVI.

# omni-android
Omni Android offers you an interface to interact with composable observers and collectors of state and effect.

## Installation
In order to include omni-mvi-android, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:1.0")
    implementation("net.asere.omni.mvi:mvi-android:1.0")
}
```

## Observing effects
You can observe container host effects using composable extension `OnEffect()` where effects collection will always flow under app lifecycle:
```kotlin
viewModel.OnEffect {
    when (it) {
        is ListEffect.ShowMessage -> scaffoldState.showSnackbar(it.text)
    }
}
```

## Observing state
You can observe container host state using the composable extension `state()` which will observe it under app's lifecycle.
```kotlin
val state by viewModel.state()
```

# omni-mvi-decorator
Omni MVI Decorator allows you decorate containers and add new feature behaviours to your containers like the ones described below.

## Installation
In order to include omni-mvi-decorator, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:1.0")
    implementation("net.asere.omni.mvi:mvi-decorator:1.0")
}
```

# omni-mvi-lock
Omni MVI Lock is a container host decorator that allows you execute locking intents using `lockIntent()` DSL.

## Installation
In order to include omni-mvi-lock, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:1.0")
    implementation("net.asere.omni.mvi:mvi-decorator:1.0")
    implementation("net.asere.omni.mvi:mvi-lock:1.0")
}
```

## Lock container
Lock container decorates any state container with the purpose of adding its feature to an existing container. To add locking feature to your state container you have to make your current container implement the `LockContainerHost` interface:
```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    StateContainerHost<ListState, ListEffect, ListAction>,
    LockContainerHost<ListState, ListEffect, ListAction>
```
Just like the state container, it will ask you to override its container. For that you will use the `decorate()` extension to create a lock container using its builder function:
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    onAction = ::onAction,
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).decorate { lockContainer(it) }
```

## Lock intent usage
As described in the example below, requests for next page won't execute until ongoing execution finishes. 
IMPORTANT: Locked intents can be identified by passing an id at invocation. If no identifier is provided it will take a default one. Default identifier will be used as long no identifier is provided. Identifiers let you identify what intents you are trying to lock.
```kotlin
// We are using lockIntent since we want this intent to execute only once at a time
private fun fetchContent() = lockIntent {
    onError { // This block is called when the intent execution fails
        postEffect(ListEffect.ShowMessage(it.requireMessage()))
    }
    postState { copy(loading = true) }
    val repos = getRepositories(currentState.currentPage)
    postState {
        copy(loading = false, currentPage = repos.currentPage, items = items + repos.items)
    }
    if (repos.items.isEmpty()) {
        lockIntent() // Lock this intent as the end of the list has been reached
    }
}
```

## Locking/Unlocking an intent
The lock container host allows you to lock/unlock any intent at any time by calling `lockIntent(id)` or `unlockIntent(id)`. Id parameter is optional, if none is provided then the default intent will be handled.

# omni-mvi-override

README STILL IN PROGRESS