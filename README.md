# omni-mvi ![](https://img.shields.io/badge/mvi_version-1.7.5-004475) ![](https://img.shields.io/badge/coverage-90%25-004475)
Omni MVI is a light weight set of tools inspired by [Orbit](https://orbit-mvi.org) that allows Kotlin/Java developer turn any object into a MVI like object.

## Installation
In order to include omni-mvi, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:core:$version")
    implementation("net.asere.omni.mvi:mvi:$version")
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
        post(ListEffect.ShowMessage(it.requireMessage()))
    }
    reduce { copy(loading = true) }
    val repos = getRepositories(currentState.currentPage)
    reduce { copy(loading = false, items = items + repos.items) }
}
```

The `intent` DSL allows you execute controlled block of code and grants you special access to state and effects publication, also lets you react to errors caught during execution.

- onError: This block is called when an exception is caught
- reduce: Post a state to the view
- post: Post an effect to the view

### Nesting intents
Intents are jobs. When you invoke an intent you are executing a coroutine job. You are free to nest intents invocations.

### Intent invocation
When intents are invoked they do it under `CoroutineStart.DEFAULT` start configuration, which in normal circumstances, will immediately execute the coroutine. Intents could also be delayed just as you would do with jobs:

```kotlin
private fun fetchDataAfter(time: Int) = intent {
    val pendingIntent = intentJob(start = CoroutineStart.LAZY) {
        Log.d("Lazy", "Job executed after $time")
    }
    delay(time)
    pendingIntent.start()
}
```
Intents could be directly nested or called.
```kotlin
private fun performFetch() = intentJob(start = CoroutineStart.LAZY) {
    Log.d("Lazy", "Fetch job executed")
}

private fun fetchDataAfter(time: Int) = intent {
    val pendingIntent = performFetch()
    delay(time)
    pendingIntent.start()
}
```
### Intent's job
Intent function does not return the job running the intent. Use `intentJob` extension function, it performs the exact same as normal `intent` but returns its job.

### Observing
If you are using standard JVM you can observe states and effects using `observeState()` and `observeEffect()` intent functions. Both extension functions require a lambda, this lambda will be called whenever a value is emitted.

## Actions
Omni-MVI offers you an alternative way of intents execution. By using the `Action` API you can centralise the invocation flow, avoiding any unwanted access to the host. A `StateContainer` does not provide any way of `Action` execution. To allow this make your `Host` implement `ActionContainerHost`.

```kotlin
class ListViewModel(
    private val getRepositories: GetRepositories,
    private val searchRepositories: SearchRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>
```

`ActionContainerHost` expects a third generic parameter called Action. This parameter defines the handled action data-type.

In order to build an `ActionContainer` just call `.onAction()` funtion on any container and provide its callback. Once an action is called the callback will be invoked. `onAction()` will turn any container into an action container.

```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).onAction(::onAction)

// Here we handle incoming actions
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
    implementation("net.asere.omni.mvi:core:$version")
    implementation("net.asere.omni.mvi:mvi:$version")
    testImplementation("net.asere.omni.mvi:mvi-test:$version")
}
```

### Constructor testing
If your `Host` executes intents during its construction you can capture its states and effects by running `testConstructor()`:

```kotlin
@Test
fun `On creation must request first page to repository`() = runTest {
    val firstPage = 1
    testConstructor { createViewModel() }.evaluate(relaxed = true) {
        Assert.assertEquals(2, emittedStates.size)
        Assert.assertEquals(emittedStates.first().currentPage, firstPage)
    }
}
```

`testConstructor()` function will construct you host under a controlled context to allow state and effects recording. Call `evaluate` to get inline access to test results.

### Strict evaluation
By default, the evaluation runs not relaxed (`relaxed = false`), this means evaluation will force the validation of each emitted state or effect. Test will not pass if the exact amount or order is not evaluated. Use `expectState` and `expectEffect` to validate them.

```kotlin
@Test
fun `On creation request first page to repository and`() = runTest {
    testConstructor { createViewModel() }.evaluate {
        expectState { copy(currentPage = 1, loading = true) }
        expectState {
            copy(
                loading = false,
                currentPage = fakePagedRepos.currentPage,
                items = fakePagedRepos.items.map { it.asPresentation() }
            )
        }
        expectEffect(ListEffect.ShowMessage("Fetched"))
    }
}
```

### Relaxed evaluation
Evaluation can run relaxed (`relaxed = true`), this mode will not force you to match the exact amount of emitted states and effects.

### Manual assert
If you wish to do manual assertions upon `states` and `effects` you can use `nextState` and `nextEffect` functions. `nextState` provides previous and current state to let you perform a proper evaluation by your own. `nextEffect` provides next emitted effect.
Even relaxed, the evaluation will force you respect the order of emissions.

```kotlin
@Test
fun `On creation request first page to repository and`() = runTest {
    testConstructor { createViewModel() }.evaluate(relaxed = true) {
        coVerify { getRepositories(1) }
        nextState { previous, current ->
            Assert.assertEquals(
                current, previous.copy(
                    currentPage = 1,
                    loading = true
                )
            )
        }
        nextState { previous, current ->
            Assert.assertEquals(current, previous.copy(
                loading = false,
                currentPage = fakePagedRepos.currentPage,
                items = fakePagedRepos.items.map { it.asPresentation() }
            ))
        }
        nextEffect {
            Assert.assertEquals(it, ListEffect.ShowMessage("Fetched"))
        }
    }
}
```

### Accessing emitted states and effects
When relaxed, the evaluation process will not force you validate any emitted `state` or `effect`. Still you can access them through `emittedStates` and `emittedEffects` properties.

### Intents testing
Call `testIntent()` to capture any emitted state or effect. Call `evaluate` to get inline access to resulting data:

```kotlin
@Test
fun `On NextPage intent called should request next page to repository`() = runTest {
    createViewModel().testIntent { nextPage() }.evaluate {
        ...
    }
}
```

### Action testing
Use the `testOn()` function to test host actions:
```kotlin
@Test
fun `On NextPage action called should request next page to repository`() = runTest {
    createViewModel().testOn(ListAction.NextPage).evaluate {
        ...
    }
}
```

### Testing endless intents
In case you want to test long running intents you can always truncate your evaluation to a certain amount of `states`:
```kotlin
@Test
fun `On continues emit intent called should take first 9 states`() = runTest {
    createViewModel().testIntent(
        from = ListState(currentPage = 10),
        take = 9 times state
    ) { continuesEmit() }.evaluate {
        Assert.assertEquals(9, emittedStates.size)
    }
}
```
Or `effects`:
```kotlin
@Test
fun `On continues post intent called should take first 15 effects `() = runTest {
    createViewModel().testIntent(take exactly 15 times effect) { continuesPost() }.evaluate {
        Assert.assertEquals(15, emittedEffects.size)
    }
}
```

Please note you can set the starting state for your intent at testing by setting `from` parameter. 

### Infix
Use the `infix` functions group to declare how many states or effect. You can use it in both ways:

Just passing the value:
```kotlin
testIntent(take exactly 15 times effect) { intentToTest() }
```
Naming the parameter:
```kotlin
testIntent(take = 1 time effect) { intentToTest() }
```

# omni-android ![](https://img.shields.io/badge/mvi_android_version-1.7.3-03DAC5)
Omni Android offers you an interface to interact with composable observers and collectors of state and effect.

## Installation
In order to include omni-mvi-android, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:core:$version")
    implementation("net.asere.omni.mvi:mvi:$version")
    implementation("net.asere.omni.mvi:mvi-android:$version")
}
```
## Observing on lifecycle
When observing from an Android fragment or Activity you should then provide the lifecycle owner, which is `this` in this case:
```kotlin
class MainActivity : AppCompatActivity() {
    
    // Views definitions and ViewModel injection
    ...
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel.observeState(this, onState = ::onState)
        viewModel.observeEffect(this, onEffect = ::onEffect)
    }

    private fun onState(state: LoginState) {
        loginInput.text = state.username
    }
    
    private fun onEffect(effect: LoginEffect) = when (effect) {
        LoginEffect.NavigateHome -> navigateHome()
    }
    
    // Rest of implementations
    ...
}
```

## Observing on composable
You can observe container host effects using composable extension `OnEffect()` where effects collection will always flow under app lifecycle:
```kotlin
viewModel.OnEffect {
    when (it) {
        is ListEffect.ShowMessage -> scaffoldState.showSnackbar(it.text)
    }
}
```
You can observe container host state using the composable extension `state()` which will observe it under app's lifecycle.
```kotlin
val state by viewModel.state()
```

## Saving states
Omni-MVI allows you to save your states to avoid them to be destroyed if the device releases the memory. There is where the `SaveableStateContainer` becomes handy. Use the top level host extension function `saveableStateContainer` and pass an aditional `SavedStateHandle` object in order to build the container.

```kotlin
override val container = saveableStateContainer(
    initialState = ListState(),
    savedStateHandle = savedStateHandle,
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
)
```
Make sure your states and member types are parcelable.
```kotlin
@Parcelize
data class ListState(
    val query: String? = null,
    val currentPage: Int = 1,
    val loading: Boolean = false,
    val items: List<RepoModel> = listOf(),
    val error: String = String.empty()
) : Parcelable
```

# omni-mvi-lock ![](https://img.shields.io/badge/mvi_lock_version-1.7.3-11AA00) ![](https://img.shields.io/badge/coverage-23%25-11AA00)
Omni MVI Lock is a container host decorator that allows you execute locking intents using `lockIntent()` DSL.

## Installation
In order to include omni-mvi-lock, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:core:$version")
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
Just like the state container, it will ask you to override its container. For that you will use the `buildLockContainer()` extension. `buildLockContainer()` will turn any `Container` into a `LockContainer`:
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).buildLockContainer().onAction(::onAction)
```

## Lock intent usage
As described in the example below, requests for next page won't execute until ongoing execution finishes. 
IMPORTANT: Locked intents can be identified by passing an id at invocation. If no identifier is provided it will take a default one. Default identifier will be used as long no identifier is provided. Identifiers let you identify what intents you are trying to lock.
```kotlin
// We are using lockIntent since we want this intent to execute only once at a time
private fun fetchContent() = lockIntent {
    onError { // This block is called when the intent execution fails
        post(ListEffect.ShowMessage(it.requireMessage()))
    }
    reduce { copy(loading = true) }
    val repos = getRepositories(currentState.currentPage)
    reduce {
        copy(loading = false, currentPage = repos.currentPage, items = items + repos.items)
    }
    if (repos.items.isEmpty()) {
        lockIntent() // Lock this intent as the end of the list has been reached
    }
}
```

## Locking/Unlocking an intent
The lock container host allows you to lock/unlock any intent at any time by calling `lockIntent(id)` or `unlockIntent(id)`. Id parameter is optional, if none is provided then the default intent will be handled.

# omni-mvi-override ![](https://img.shields.io/badge/mvi_override_version-1.7.3-B41B00) ![](https://img.shields.io/badge/coverage-0%25-B41B00)
Omni MVI Override is a container host decorator that allows developers execute any intent replacing any previous ongoing execution of itself.

## Installation
In order to include omni-mvi-override, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:core:$version")
    implementation("net.asere.omni.mvi:mvi:$version")
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
    OverrideContainerHost<ListState, ListEffect>
```
Once implemented you must override its container. In order to do it, you can use `buildOverrideContainer()` builder function. `buildOverrideContainer()` will turn any `Container` into an `OverrideContainer`.
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).buildOverrideContainer().onAction(::onAction)
```

## Override intent usage
You can execute an `overrideIntent()` when you wish a block of code to be overridden when executed. You can pass an identifier as parameter to identify the intent's execution you would like to override
```kotlin
private fun onQuery(value: String) = overrideIntent {
    reduce { copy(query = value, currentPage = 1) }
    delay(QUERY_DELAY) // Apply a delay to the intent to reduce query rate
    onError { showError(it) } // Executed when an error occurs
    reduce { copy(loading = true) }
    val repos = searchRepositories(query = value, currentState.currentPage)
    reduce { copy(loading = false, items = repos.items) }
}
```

# omni-mvi-queue ![](https://img.shields.io/badge/mvi_queue_version-1.7.3-6300AA) ![](https://img.shields.io/badge/coverage-0%25-6300AA)
Omni MVI Queue is a container host decorator that allows developers push intents into a queue of execution. Queue intents will be then executed one by one.

## Installation
In order to include omni-mvi-queue, add the following dependencies to your project build.gradle file:
```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("net.asere.omni.mvi:core:$version")
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
    QueueContainerHost<ListState, ListEffect>
```
Override container calling `buildQueueContainer()` builder function. `buildQueueContainer()` will turn any `Container` into a `QueueContainer`.
```kotlin
override val container = stateContainer(
    initialState = ListState(),
    coroutineScope = viewModelScope,
    coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
).buildQueueContainer().onAction(::onAction)
```
Queue container host will allow you enqueue intents and let them execute each. Whenever you want to enqueue an intent use `queueIntent()` DSL.

# License - MIT

Copyright 2023 Asere.net

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
