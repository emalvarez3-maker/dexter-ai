/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.ancla.tasks

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/*
 * Encargos: things Dexter does in the background while the user keeps using the phone ("averigua X
 * y avísame"), now or on a schedule, optionally only speaking up when a condition holds. This file
 * is the contract every part builds on; it has no Android code, so all of it is unit-tested.
 */

enum class EncargoState {
    /** Waiting for its turn or for its scheduled time. */
    QUEUED,
    RUNNING,

    /** Stopped because a step needs the user's own yes; nothing is confirmed in the background. */
    WAITING_USER,
    DONE,
    FAILED,

    /** The user stopped it; [Encargo.lastResult] holds the wrap-up of what was done. */
    STOPPED
}

/** When a scheduled encargo runs: once on [date], every day, or on [weekdays], at each of [times]. */
data class Recurrence(
    val kind: Kind,
    /** Minutes after midnight, local time (8:00 is 480); at least one. */
    val times: List<Int>,
    val weekdays: Set<DayOfWeek> = emptySet(),
    val date: LocalDate? = null
) {
    enum class Kind { ONCE, DAILY, WEEKLY }
}

/** One thing that happened while an encargo ran: a tool's result, a note, something the user added. */
data class EncargoStep(val time: Long, val text: String)

/** What a run cost: model round trips, tokens and wall time. */
data class Usage(val calls: Int = 0, val inputTokens: Long = 0, val outputTokens: Long = 0, val millis: Long = 0) {
    operator fun plus(o: Usage) = Usage(calls + o.calls, inputTokens + o.inputTokens, outputTokens + o.outputTokens, millis + o.millis)
}

data class Encargo(
    val id: String,
    /** What the user asked for, in their words. */
    val prompt: String,
    /** A few words for lists and notifications. */
    val title: String,
    val created: Long,
    val state: EncargoState = EncargoState.QUEUED,
    /** Null: runs once, right away. */
    val recurrence: Recurrence? = null,
    /** "solo si va a llover": the user is told only when it holds. Blank: always. */
    val condition: String = "",
    /** Speak up at most once per calendar day, however often it runs. */
    val atMostDaily: Boolean = false,
    val paused: Boolean = false,
    val lastRun: Long = 0,
    val lastNotified: Long = 0,
    /** What Dexter found or did the last time it ran; shown to the user. */
    val lastResult: String = "",
    /** The key figure of the last run ("$1.02", "lluvia"), to compare with next time. */
    val lastValue: String = "",
    /** The last run's steps, newest last; capped at [MAX_STEPS_KEPT]. */
    val steps: List<EncargoStep> = emptyList(),
    /** The last run's cost. */
    val usage: Usage = Usage()
) {
    val scheduled get() = recurrence != null
    val active get() = state == EncargoState.RUNNING || state == EncargoState.QUEUED

    companion object {
        const val MAX_STEPS_KEPT = 40

        /** Model round trips one run may take, so a background task can't burn the day's quota. */
        const val MAX_MODEL_STEPS = 6

        /** Encargos running at the same time. */
        const val MAX_RUNNING = 3

        /** What a scheduled run answers when its condition doesn't hold: the user isn't told. */
        const val NOTHING = "NADA"
    }
}

/** The encargos kept on the phone (encrypted by its implementation). Safe to call from any thread. */
interface EncargoBook {
    /** Every encargo, newest first. */
    fun all(): List<Encargo>
    fun get(id: String): Encargo?

    /** Adds [encargo] or replaces the one with its id. */
    fun put(encargo: Encargo)
    fun remove(id: String)

    /** Changes whenever the encargos may have changed; cheap, for a screen's poll. */
    fun version(): Long
}

/** What the user (on screen) and Dexter (by voice) can do with encargos. */
interface EncargoControl {
    /** Creates an encargo and, without a [recurrence], starts it at once. Returns it. */
    fun create(prompt: String, title: String, recurrence: Recurrence? = null, condition: String = "", atMostDaily: Boolean = false): Encargo
    fun runNow(id: String)

    /** Something the user adds while it runs; the agent reads it before its next step. */
    fun add(id: String, text: String)
    fun stop(id: String)
    fun setPaused(id: String, paused: Boolean)
    fun remove(id: String)
}

/** Where every model call's cost is recorded (a ledger by day and model). */
fun interface UsageSink {
    fun record(encargoId: String?, model: String, inputTokens: Long, outputTokens: Long, time: Long)
}

/**
 * The agents report each model call here. A run executes on one thread, which names its encargo in
 * [encargo], so calls are counted per encargo without the agents knowing about encargos at all.
 */
object AgentUsage {
    @Volatile var sink: UsageSink? = null

    /** The encargo the current thread is running, or null for an ordinary conversation turn. */
    val encargo = ThreadLocal<String?>()

    private val running = ConcurrentHashMap<String, Usage>()

    fun report(model: String, inputTokens: Long, outputTokens: Long, now: Long = System.currentTimeMillis()) {
        val id = encargo.get()
        if (id != null) running.merge(id, Usage(1, inputTokens, outputTokens)) { a, b -> a + b }
        runCatching { sink?.record(id, model, inputTokens, outputTokens, now) }
    }

    /** What [id]'s run has cost so far; [take] also clears it, when the run ends. */
    fun of(id: String): Usage = running[id] ?: Usage()

    fun take(id: String): Usage = running.remove(id) ?: Usage()
}
