package dev.br1ansouza.motoscope.core.telemetry

fun interface MonotonicClock {
    fun millis(): Long
}
