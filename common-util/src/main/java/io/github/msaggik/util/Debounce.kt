package io.github.msaggik.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DebounceMode {
    LEADING,
    TRAILING
}

/**
 * Creates a debounced version of the given [action] function, restricting how often it can be invoked.
 *
 * This is useful for scenarios like text input, rapid click events, or scroll events where you want
 * to reduce the frequency of expensive operations such as API calls, UI updates, or recomputations.
 *
 * @param T The type of value passed to the [action].
 * @param delayMillis The debounce interval in milliseconds. Calls made within this period
 *        are either ignored or delayed, depending on the selected [mode].
 * @param mode Determines how the debounce behaves:
 * - [DebounceMode.LEADING] invokes [action] immediately on the first call, then suppresses
 *   subsequent calls for [delayMillis] (similar to throttling).
 * - [DebounceMode.TRAILING] waits until no calls have been made for [delayMillis], then invokes [action]
 *   with the most recent value.
 * @param action The function to be invoked according to the debounce logic.
 *
 * @return A lambda function that can be called repeatedly with a [T] value,
 *         but will execute [action] only as determined by the debounce [mode].
 *
 * @see DebounceMode
 *
 * ### Example usage:
 * ```
 * val debouncedSearch = scope.debounce<String>(
 *     delayMillis = 350,
 *     mode = DebounceMode.TRAILING
 * ) { query ->
 *     viewModel.performSearch(query)
 * }
 *
 * editText.doAfterTextChanged {
 *     debouncedSearch(it.toString())
 * }
 * ```
 */
fun <T> CoroutineScope.debounce(
    delayMillis: Long,
    mode: DebounceMode = DebounceMode.TRAILING,
    action: (T) -> Unit
): (T) -> Unit {
    var job: Job? = null

    return { value: T ->
        when (mode) {
            DebounceMode.LEADING -> {
                if (job == null) {
                    action(value)
                    job = launch {
                        delay(delayMillis)
                        job = null
                    }
                }
            }

            DebounceMode.TRAILING -> {
                job?.cancel()
                job = launch {
                    delay(delayMillis)
                    action(value)
                    job = null
                }
            }
        }
    }
}