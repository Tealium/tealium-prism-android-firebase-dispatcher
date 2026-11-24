package com.tealium.prism.firebase.helpers

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.misc.TealiumResult
import org.junit.Assert.assertTrue

/**
 * Executes [command] with [payload] and returns the resulting [TealiumResult].
 *
 * Commands in this module are synchronous so the callback fires before [Command.execute]
 * returns, which makes this safe to call on the test thread.
 *
 * The failure is the one the command raised: an `ExtractionException` is only wrapped in
 * `CommandException.invalidPayload` later, by the `CommandDispatcher`.
 */
internal fun runCommand(command: Command, payload: DataObject): TealiumResult<Unit> {
    var captured: TealiumResult<Unit>? = null
    command.execute(payload) { captured = it }
    return checkNotNull(captured) { "Command '${command.name}' did not invoke callback" }
}

/**
 * Asserts that this result is a failure of type [T], and returns that failure.
 */
internal inline fun <reified T : Throwable> TealiumResult<*>.assertFailure(): T {
    val error = exceptionOrNull()
    assertTrue("Expected ${T::class.java.name} but got $error", error is T)
    return error as T
}
