package dev.br1ansouza.motoscope.protocol.elm327

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObdResponseReaderTest {
    private val engineSpeed = ElmCommand.ObdRequest(mode = 0x01, pid = 0x0C)

    @Test
    fun payloadIsWhatFollowsTheEchoedModeAndPid() {
        val payload = ObdResponseReader.read(engineSpeed, data(0x41, 0x0C, 0x1A, 0xF8))

        assertEquals(ObdPayload(mode = 0x01, pid = 0x0C, bytes = listOf(0x1A, 0xF8)), payload)
    }

    @Test
    fun answerToAnotherModeIsRejected() {
        assertNull(ObdResponseReader.read(engineSpeed, data(0x42, 0x0C, 0x1A, 0xF8)))
    }

    @Test
    fun answerToAnotherPidIsRejected() {
        assertNull(ObdResponseReader.read(engineSpeed, data(0x41, 0x05, 0x5C)))
    }

    @Test
    fun positiveResponseWithoutPayloadIsRejected() {
        assertNull(ObdResponseReader.read(engineSpeed, data(0x41, 0x0C)))
    }

    @Test
    fun requestWithoutPidKeepsEverythingAfterTheMode() {
        val storedCodes = ElmCommand.ObdRequest(mode = 0x03, pid = null)

        assertEquals(
            ObdPayload(mode = 0x03, pid = null, bytes = listOf(0x01, 0x33)),
            ObdResponseReader.read(storedCodes, data(0x43, 0x01, 0x33))
        )
    }

    private fun data(vararg bytes: Int) = ElmResponse.Data(bytes.toList())
}
