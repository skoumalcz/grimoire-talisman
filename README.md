<p align="center">
  <img src="art/logo.svg" width="128px" />
</p>
<p align="center">
    <a href="https://bintray.com/diareuse/grimoire/talisman/"><img src="https://api.bintray.com/packages/diareuse/grimoire/talisman/images/download.svg?version=latest" /></a>
</p>
<h1 align="center">Grimoire<sup>Talisman</sup></h1>

Why Talisman?

* **Safety**

    Safely and correctly catching exceptions is _not hard_, but surely annoying to deal with.
    Introducing `Seal`. `Seal` is designed in a very much same way as `kotlin.Result` except you
    can return it from methods, works regardless of Kotlin version and doesn't require any command
    line flags.

* **Separation of concerns**

    You can try as much as you'd like, however rule of having your classes do one thing and one
    thing only is tempting to break. By using `UseCase` you're no longer tempted by that bad habit.
    As the name implies it's just a use-case, one input - one output. That's it.

* **Extendability**

    All existing APIs in Talisman are designed to be extensible. No more "oh my gawd, I need to use
    this variable but it's internal". Extend existing APIs, submit pull requests. Easy.

* **Reusability**

    Having separated concerns this is already easy but needs to be mentioned. Every bit of code,
    every use-case can be reused everywhere in your app, significantly cutting down on the amount of
    copy pasted code.


## How do I use Talisman?

_Please note that all examples assume import of `com.skoumal.grimoire.talisman.*`. Make sure to
update import settings in IntelliJ (Android Studio) so you're always importing talisman packages
with an *_

### `UseCase`

UseCases are a simple interface with powerful extension methods. Very simple example is to sum all
numbers provided to the interface and return the latest value.

```kotlin
class SumAllAddedNumbers : UseCase<Int, Int> {

    @Volatile
    private var sum: Int = 0

    override suspend fun use(input: Int): Int {
        sum += input
        return sum
    }

}

// and use it like

runBlocking {
    val uc = SumAllAddedNumbers()

    println(uc(1).getOrNull()) // -> 1
    println(uc(2).getOrNull()) // -> 3
    println(uc(3).getOrNull()) // -> 6
}
```

> You can see that invocations on the `uc` variable are not done through `use(input: Int)` method.
That's because we're using an orchestrator mechanism integrated to talisman. You should at all times
call `uc.invoke(input)` or simply `uc(input)` to trigger the orchestrator. You can read more about
what they do [here](talisman/src/main/java/com/skoumal/grimoire/talisman/UseCaseOrchestrator.kt)

Then you will not be always adding simple numbers, but rather requesting some network information
from remote host.

```kotlin
class RegisterUser(
    private val api: MyApiService
) : UseCase<RegisterUser.Input, RegisterUser.Output> {

    class Input(
        // JvmField doesn't generate additional methods (get/set)
        @JvmField val name: String,
        @JvmField val surname: String,
        @JvmField val nickname: String,
        @JvmField val password: String
    )

    class Output(
        @JvmField val token: String
    )

    override suspend fun use(input: Input): Output {
        // throws when encounters any error, and that's ok!
        val result = api.registerUser(
            name = "%s %s".format(input.name, input.surname),
            username = input.nickname,
            password = input.password
        )

        return Output(
            token = result.token
        )
    }

    suspend operator fun invoke(
        name: String,
        surname: String,
        nickname: String,
        password: String
    ) = invoke(
        Input(
            name = name,
            surname = surname,
            nickname = nickname,
            password = password
        )
    )

}

runBlocking {
    val uc = RegisterUser(fetchApi())
    val output = uc(fetchInput())
}
```

> If you want to use internal input like so you might also want to declare operator function so the
UseCase is immediately callable with named params without the need to initialize the `Input` class
explicitly.

Logo by <a href="https://www.flaticon.com/authors/smalllikeart" title="smalllikeart">smalllikeart</a>
