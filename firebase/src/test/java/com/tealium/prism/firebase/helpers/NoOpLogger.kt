package com.tealium.prism.firebase.helpers

import com.tealium.prism.core.api.logger.LogLevel
import com.tealium.prism.core.api.logger.Logger

/** Silent [Logger] used by unit tests. */
internal class NoOpLogger : Logger {
    override fun shouldLog(level: LogLevel): Boolean = false
    override fun log(level: LogLevel, category: String, message: String) = Unit
    override fun log(level: LogLevel, category: String, message: String, vararg args: Any?) = Unit
    override fun log(level: LogLevel, category: String, message: () -> String) = Unit
    override fun trace(category: String, message: String) = Unit
    override fun trace(category: String, message: String, vararg args: Any?) = Unit
    override fun trace(category: String, message: () -> String) = Unit
    override fun debug(category: String, message: String) = Unit
    override fun debug(category: String, message: String, vararg args: Any?) = Unit
    override fun debug(category: String, message: () -> String) = Unit
    override fun info(category: String, message: String) = Unit
    override fun info(category: String, message: String, vararg args: Any?) = Unit
    override fun info(category: String, message: () -> String) = Unit
    override fun warn(category: String, message: String) = Unit
    override fun warn(category: String, message: String, vararg args: Any?) = Unit
    override fun warn(category: String, message: () -> String) = Unit
    override fun error(category: String, message: String) = Unit
    override fun error(category: String, message: String, vararg args: Any?) = Unit
    override fun error(category: String, message: () -> String) = Unit
}
