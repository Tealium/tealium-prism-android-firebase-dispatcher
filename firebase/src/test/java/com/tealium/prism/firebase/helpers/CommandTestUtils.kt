package com.tealium.prism.firebase.helpers

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.misc.TealiumResult

/**
 * Executes [command] with [payload] and returns the resulting [TealiumResult].
 *
 * Commands in this module are synchronous so the callback fires before [Command.execute]
 * returns, which makes this safe to call on the test thread.
 */
internal fun runCommand(command: Command, payload: DataObject): TealiumResult<Unit> {
    var captured: TealiumResult<Unit>? = null
    command.execute(payload) { captured = it }
    return checkNotNull(captured) { "Command '${command.name}' did not invoke callback" }
}
