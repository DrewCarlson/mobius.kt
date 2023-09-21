# Examples

This page covers a few practical examples of Mobius.kt components.
For a simple example of various components, see [Getting Started](getting-started.md).

## User Profile

This example demonstrates a simple Side Effect handler which makes a network request to produce data
for the Model.

### Model

```kotlin
data class UserProfileModel(
    val userId: String? = null,
    val name: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Events

```kotlin
sealed class UserProfileEvent {
    /**
     * An Event to initiate loading of User data.
     */
    data class OnLoadUserProfile(val userId: String) : UserProfileEvent()

    /**
     * An Event modeling a successful network request for User data.
     */
    data class OnUserProfileLoaded(val name: String) : UserProfileEvent()

    /**
     * An Event modeling a failed network request for user data.
     */
    data class OnUserProfileError(val error: String) : UserProfileEvent()
}
```

### Effects

```kotlin
sealed class UserProfileEffect {
    /**
     * An Effect which contains the data required to request user data from the network.
     */
    data class FetchUserProfile(val userId: String) : UserProfileEffect()
}
```

### Update Function

It is recommended that you make use of code generation for sufficiently complex Update functions.
For details on setting it up in your project, see [Code Generation > Update Generator](modules/codegen.md#update-generator).

<details open="open">
<summary>With Code Generation</summary>

```kotlin
@GenerateUpdate
class UserProfileUpdate : Update<UserProfileModel, UserProfileEvent, UserProfileEffect>, UserProfileGeneratedUpdate {

    override fun onLoadUserProfile(event: UserProfileEvent.OnLoadUserProfile): Next<UserProfileModel, UserProfileEffect> {
        return next(
            model.copy(isLoading = true),
            setOf(UserProfileEffect.FetchUserProfile(event.userId))
        )
    }

    override fun onUserProfileLoaded(event: UserProfileEvent.OnUserProfileLoaded): Next<UserProfileModel, UserProfileEffect> {
        return next(model.copy(isLoading = false, name = event.name))
    }

    override fun onUserProfileError(event: UserProfileEvent.OnUserProfileError): Next<UserProfileModel, UserProfileEffect> {
        return next(model.copy(isLoading = false, error = event.error))
    }
}
```
</details>


<details>
<summary>Without Code Generation (Click to expand)</summary>

```kotlin
class UserProfileUpdate : Update<UserProfileModel, UserProfileEvent, UserProfileEffect> {
    override fun update(model: UserProfileModel, event: UserProfileEvent): Next<UserProfileModel, UserProfileEffect> {
        return when (event) {
            is UserProfileEvent.OnLoadUserProfile -> next(
                model.copy(isLoading = true),
                setOf(UserProfileEffect.FetchUserProfile(event.userId))
            )
            is UserProfileEvent.OnUserProfileLoaded -> next(model.copy(isLoading = false, name = event.name))
            is UserProfileEvent.OnUserProfileError -> next(model.copy(isLoading = false, error = event.error))
        }
    }
}
```
</details>

### Effect Handler

If your Effect Handlers are implemented in Kotlin, it is recommended that you use the Coroutines adapter.
See the [Coroutines](modules/coroutines.md) module for more details.

<details open="open">
<summary>With Coroutines</summary>

```kotlin
fun createUserProfileHandler(
    apiClient: ApiClient
) = subtypeEffectHandler<UserProfileEffect, UserProfileEvent> {
    addFunction<UserProfileEffect.FetchUserProfile> { effect ->
        when (val networkResult = apiClient.fetchUserProfile(effect.userId)) {
            is Success -> UserProfileEvent.OnUserProfileLoaded(networkResult.user.name)
            is Failed -> UserProfileEvent.OnUserProfileError(networkResult.errorMessage)
        }
    }
}
```
</details>

For other advance situations, like when writing Effect Handlers in another language,
you can implement Effect Handlers manually.

<details>
<summary>Manual, Without Coroutines (Click to expand)</summary>

```kotlin
class UserProfileHandlerFactory(
    private val apiClient: ApiClient
) : Connection<UserProfileEffect, UserProfileEvent> {
    
    override fun connect(output: Consumer<UserProfileEvent>) {
        return UserProfileHandler(apiClient, output)
    }
}

class UserProfileHandler(
    private val apiClient: ApiClient,
    private val output: Consumer<UserProfileEfvent>
) : Connection<UserProfileEffect> {

    override fun accept(value: UserProfileEffect) {
        when (value) {
            is UserProfileEffect.FetchUserProfile -> fetchUserProfile(value.userId)
        }
    }

    override fun dispose() {
    }
    
    private fun fetchUserProfile(userId: String) {
        val result = when (val networkResult = apiClient.fetchUserProfile(userId)) {
            is Success -> UserProfileEvent.OnUserProfileLoaded(networkResult.user.name)
            is Failed -> UserProfileEvent.OnUserProfileError(networkResult.errorMessage)
        }
        output.accept(result)
    }
}
```
</details>