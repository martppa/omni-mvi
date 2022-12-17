# omni-mvi ![](https://img.shields.io/badge/mvi_version-1.2.0-004475) ![](https://img.shields.io/badge/coverage-90%25-004475)
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
) : ViewModel(), StateContainerHost<ListState, ListEffect>
```

Most of container hosts need 2 generic parameters which define the interaction with the view:

- State: defines the state of the view
- Effect: defines executions side effects

Once implemented, it will force you override the container. Use the container builder function
`stateContainer()` to create it.

```kotlin
override val container = stateContainer(
    initialState = ListState()
)
```

The `stateContainer()` builder function requires you to pass a mandatory `initialState` and 2 optional arguments:

- coroutineScope: Coroutine scope of intents execution. Defaulted to empty context coroutine scope
- coroutineExceptionHandler: The exception handler used to capture exception on intents execution.

```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
)
```

Once your container is created, your are ready to go. Now you can start calling your intents and react to them.

## Intents
Intents are suspending jobs running under a specific scope which grants access to the state, effects and possible thrown errors. To define an Omni intent you just have to follow the syntax bellow:

```kotlin
fun fetchContent() = intent {
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
Intents are jobs. When you invoke an intent you are executing a coroutine job. You are free to nest intents invocations.

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
Omni-MVI offers you an alternative way of intents execution. By using the `Action` API you can centralise the invocation flow. A `StateContainer` does not provide any way of `Action` execution. To allow this make your `Host` implement `ActionContainerHost`

```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    private val searchRepositories: SearchRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>
```

`ActionContainerHost` expects a third generic parameter called Action. This parameter defines the handled action data-type.

In order to build an `ActionContainer` just call `.onAction()` funtion on any container and provide it's callback. Once an action is called the callback will be invoked.

```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).onAction(::onAction)

// Here we handle incomming actions
private fun onAction(action: ListAction) {
    when (action) {
        ListAction.NextPage -> nextPage()
        ListAction.Retry -> retry()
        is ListAction.Query -> onQuery(action.value)
    }
}
```

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
As Omni MVI only adds an extra layer of behaviour to underlying coroutines infrastructure, you don't need any special feature for testing. You can implement tests just like you would do it without Omni MVI. You can access `state` and `effect` flows located in the `Container`. Make sure it's a `StateContainer` by calling `asStateContainer()` if it's not. However, there's a couple of tools that might be handy in order to test intents or actions.

### Installation
To add Omni-MVI-Test module to your project add the following to your gradle:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:1.0")
    implementation("net.asere.omni.mvi:mvi-test:1.0.0")
}
```

### Constructor testing
If you `Host` executes intents during it's construction you can capture its states and effects by running `testConstructor()`:

```kotlin
@Test
fun `On creation must request first page to repository`() = runTest {
    val firstPage = 1
    testConstructor { createViewModel() }.evaluate {
        coVerify { getRepositories(firstPage) }
        Assert.assertEquals(2, emittedStates.size)
        Assert.assertEquals(emittedStates.first().currentPage, firstPage)
    }
}
```

`testConstructor()` function will construct you host under a controlled context to allow state and effects recording. It returns a `TestResult` object that contains a list of emitted states and a list of emitted effects. Call evaluate to get inline access to it's properties.

### Intents testing
Call `testIntent()` to capture any emitted state or effect. Call `evaluate` to get inline access to resulting data:

```kotlin
@Test
fun `On NextPage intent called should request next page to repository`() = runTest {
    val nextPage = 2
    createViewModel().testIntent { nextPage() }.evaluate {
        coVerify { getRepositories(nextPage) }
        Assert.assertEquals(3, emittedStates.size)
        Assert.assertEquals(emittedStates.first().currentPage, nextPage)
    }
}
```

### Action testing
Use the `testOn()` function to test host actions:
```kotlin
@Test
fun `On NextPage action called should request next page to repository`() = runTest {
    val nextPage = 2
    createViewModel().testOn(ListAction.NextPage).evaluate {
        coVerify { getRepositories(nextPage) }
        Assert.assertEquals(3, emittedStates.size)
        Assert.assertEquals(emittedStates.first().currentPage, nextPage)
    }
}
```

# omni-android ![](https://img.shields.io/badge/mvi_android_version-1.2.0-03DAC5)
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

# omni-mvi-lock ![](https://img.shields.io/badge/mvi_lock_version-1.2.0-11AA00) ![](https://img.shields.io/badge/coverage-23%25-11AA00)
Omni MVI Lock is a container host decorator that allows you execute locking intents using `lockIntent()` DSL.

## Installation
In order to include omni-mvi-lock, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:$version")
    implementation("net.asere.omni.mvi:mvi-lock:$version")
}
```

## Lock container
Lock container decorates any state container with the purpose of adding its feature to an existing container. To add locking feature your state container host must implement the `LockContainerHost` interface:
```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>,
    LockContainerHost<ListState, ListEffect>
```
Just like the state container, it will ask you to override its container. For that you will use the `decorate()` extension to create a lock container using its builder function:
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).decorate { lockContainer(it) }.onAction(::onAction)
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

# omni-mvi-override ![](https://img.shields.io/badge/mvi_override_version-1.2.0-B41B00) ![](https://img.shields.io/badge/coverage-0%25-B41B00)
Omni MVI Override is a container host decorator that allows developers execute any intent replacing any previous ongoing execution of itself.

## Installation
In order to include omni-mvi-override, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:$version)
    implementation("net.asere.omni.mvi:mvi-override:$version")
}
```

## Override container
Omni MVI Override container decorates any state container to add the feature to an existing container. To add override feature your state container host must implement the `OverrideContainerHost` interface:

```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    private val searchRepositories: SearchRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>,
    OverrideContainerHost<ListState, ListEffect> {
```
Once implemented you must override it's container. In order to do it, you can use the decorate extension function together with `overrideContainer()` builder function.
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).decorate { overrideContainer(it) }.onAction(::onAction)
```

## Override intent usage
You can execute an `overrideIntent()` when you wish a block of code to be overridden when executed. You can pass an identifier as parameter to identify the intent's execution you would like to override
```kotlin
private fun onQuery(value: String) = overrideIntent {
    postState { copy(query = value, currentPage = 1) }
    delay(QUERY_DELAY) // Apply a delay to the intent to reduce query rate
    onError { showError(it) } // Executed when an error occurs
    postState { copy(loading = true) }
    val repos = searchRepositories(query = value, currentState.currentPage)
    postState { copy(loading = false, items = repos.items) }
}
```

# omni-mvi-queue ![](https://img.shields.io/badge/mvi_queue_version-1.2.0-6300AA) ![](https://img.shields.io/badge/coverage-0%25-6300AA)
Omni MVI Queue is a container host decorator that allows developers push intents into a queue of execution. Queue intents will be then executed one by one.

## Installation
In order to include omni-mvi-queue, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:mvi:1.0")
    implementation("net.asere.omni.mvi:mvi-queue:$version")
}
```

## Queue container
Omni MVI Queue container decorates any state container to add the feature to an existing container. To add queue feature your state container host must implement the `QueueContainerHost` interface:

```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    private val searchRepositories: SearchRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>,
    QueueContainerHost<ListState, ListEffect> {
```
Override container's host container decorating it using decorate extension together with `queueContainer()` function builder.
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).decorate { queueContainer(it) }.onAction(::onAction)
```
Queue container host will allow you enqueue intents and let them execute each. Whenever you want to enqueue an intent use `queueIntent()` DSL.

# License - MIT

Copyright 2022 Asere.net

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
