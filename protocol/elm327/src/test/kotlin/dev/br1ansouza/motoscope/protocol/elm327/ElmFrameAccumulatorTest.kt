package dev.br1ansouza.motoscope.protocol.elm327

import org.junit.Assert.assertEquals
import org.junit.Test

class ElmFrameAccumulatorTest {
    private val accumulator = ElmFrameAccumulator()

    @Test
    fun chunkWithoutPromptProducesNoFrame() {
        assertEquals(emptyList<String>(), accumulator.append("41 0C 1A"))
        assertEquals("41 0C 1A", accumulator.pending())
    }

    @Test
    fun frameIsEmittedOnlyWhenThePromptArrives() {
        accumulator.append("41 0C 1A")
        assertEquals(listOf("41 0C 1AF8\r"), accumulator.append("F8\r>"))
        assertEquals("", accumulator.pending())
    }

    @Test
    fun severalFramesInOneChunkAreSplit() {
        val frames = accumulator.append("OK\r>41 0C 1A F8\r>NO DATA\r>")

        assertEquals(listOf("OK\r", "41 0C 1A F8\r", "NO DATA\r"), frames)
        assertEquals("", accumulator.pending())
    }

    @Test
    fun textAfterTheLastPromptStaysPending() {
        val frames = accumulator.append("OK\r>41 0C")

        assertEquals(listOf("OK\r"), frames)
        assertEquals("41 0C", accumulator.pending())
    }

    @Test
    fun resetDiscardsTheIncompleteFrame() {
        accumulator.append("41 0C")
        accumulator.reset()

        assertEquals("", accumulator.pending())
        assertEquals(emptyList<String>(), accumulator.append("SEARCHING..."))
    }
}
