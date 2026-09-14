package dev.br1ansouza.motoscope.protocol.elm327

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ElmCommandQueueTest {
    @Test
    fun respondsOnTheFirstAttemptWhenTheAdapterAnswers() = runTest {
        val transport = ScriptedTransport(listOf("41 0C 1A F8\r>"))
        val queue = queue(transport)

        val exchange = queue.execute(ElmCommand.ObdRequest(CURRENT_DATA, ENGINE_RPM))

        assertEquals(
            ElmExchange.Completed(ElmResponse.Data(listOf(0x41, 0x0C, 0x1A, 0xF8)), attempts = 1),
            exchange
        )
        assertEquals(listOf("010C\r"), transport.written)
    }

    @Test
    fun givesUpAfterExhaustingTheAttemptsWhenTheAdapterIsSilent() = runTest {
        val transport = ScriptedTransport(emptyList())
        val queue = queue(transport)

        val exchange = queue.execute(ElmCommand.RESET)

        assertEquals(ElmExchange.TimedOut(attempts = MAX_ATTEMPTS), exchange)
        assertEquals(MAX_ATTEMPTS, transport.written.size)
    }

    @Test
    fun resendsAndSucceedsWhenTheFirstAttemptTimesOut() = runTest {
        val transport = ScriptedTransport(listOf(SILENCE, "OK\r>"))
        val queue = queue(transport)

        val exchange = queue.execute(ElmCommand.ECHO_OFF)

        assertEquals(ElmExchange.Completed(ElmResponse.Ok, attempts = 2), exchange)
        assertEquals(listOf("ATE0\r", "ATE0\r"), transport.written)
    }

    @Test
    fun discardsFramesLeftOverFromAPreviousCommand() = runTest {
        val transport = ScriptedTransport(listOf("OK\r>NO DATA\r>", "41 0C 1A F8\r>"))
        val queue = queue(transport)

        assertEquals(
            ElmExchange.Completed(ElmResponse.Ok, attempts = 1),
            queue.execute(ElmCommand.ECHO_OFF)
        )
        assertEquals(0, queue.discardedFrames)

        val second = queue.execute(ElmCommand.ObdRequest(CURRENT_DATA, ENGINE_RPM))

        assertEquals(
            ElmExchange.Completed(ElmResponse.Data(listOf(0x41, 0x0C, 0x1A, 0xF8)), attempts = 1),
            second
        )
        assertEquals(1, queue.discardedFrames)
    }

    @Test
    fun skipsSearchingFramesUpToTheConfiguredLimit() = runTest {
        val transport = ScriptedTransport(
            listOf("SEARCHING...\r>SEARCHING...\r>41 0C 1A F8\r>")
        )
        val queue = queue(transport)

        val exchange = queue.execute(ElmCommand.ObdRequest(CURRENT_DATA, ENGINE_RPM))

        assertEquals(
            ElmExchange.Completed(ElmResponse.Data(listOf(0x41, 0x0C, 0x1A, 0xF8)), attempts = 1),
            exchange
        )
    }

    @Test
    fun stopsSkippingWhenSearchingPassesTheLimit() = runTest {
        val frames = "SEARCHING...\r>".repeat(MAX_SEARCHING_FRAMES + 1)
        val transport = ScriptedTransport(listOf(frames))
        val queue = queue(transport)

        val exchange = queue.execute(ElmCommand.ObdRequest(CURRENT_DATA, ENGINE_RPM))

        assertEquals(ElmExchange.Completed(ElmResponse.Searching, attempts = 1), exchange)
    }

    @Test
    fun serializesConcurrentCommandsInsteadOfInterleavingThem() = runTest {
        val transport = ScriptedTransport(listOf("OK\r>", "OK\r>"))
        val queue = queue(transport)

        queue.execute(ElmCommand.ECHO_OFF)
        queue.execute(ElmCommand.SPACES_OFF)

        assertTrue(transport.written == listOf("ATE0\r", "ATS0\r"))
    }

    private fun queue(transport: ElmTransport) = ElmCommandQueue(
        transport = transport,
        timeoutMillis = TIMEOUT_MILLIS,
        maxAttempts = MAX_ATTEMPTS,
        maxSearchingFrames = MAX_SEARCHING_FRAMES
    )

    private class ScriptedTransport(private val answers: List<String>) : ElmTransport {
        val written = mutableListOf<String>()

        private var sent = 0
        private val pending = ArrayDeque<String>()

        override suspend fun write(text: String) {
            written += text
            answers.getOrNull(sent)?.takeIf { it != SILENCE }?.let(pending::addLast)
            sent++
        }

        override suspend fun read(): String = pending.removeFirstOrNull() ?: awaitCancellation()
    }

    private companion object {
        const val SILENCE = ""
        const val TIMEOUT_MILLIS = 500L
        const val MAX_ATTEMPTS = 3
        const val MAX_SEARCHING_FRAMES = 4
        const val CURRENT_DATA = 0x01
        const val ENGINE_RPM = 0x0C
    }
}
