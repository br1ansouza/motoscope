package dev.br1ansouza.motoscope.protocol.elm327

import org.junit.Assert.assertEquals
import org.junit.Test

class ElmResponseParserTest {
    @Test
    fun echoOfTheSentCommandIsDiscarded() {
        val response = ElmResponseParser.parse("010C\r410C1AF8\r", sentCommand = "010C")

        assertEquals(ElmResponse.Data(listOf(0x41, 0x0C, 0x1A, 0xF8)), response)
    }

    @Test
    fun spacesInTheHexPayloadAreIgnored() {
        assertEquals(
            ElmResponse.Data(listOf(0x41, 0x0C, 0x1A, 0xF8)),
            ElmResponseParser.parse("41 0C 1A F8\r")
        )
    }

    @Test
    fun acknowledgementIsRecognized() {
        assertEquals(ElmResponse.Ok, ElmResponseParser.parse("OK\r"))
    }

    @Test
    fun absenceOfDataIsNotAFailure() {
        assertEquals(ElmResponse.NoData, ElmResponseParser.parse("NO DATA\r"))
    }

    @Test
    fun searchingAloneIsTransient() {
        assertEquals(ElmResponse.Searching, ElmResponseParser.parse("SEARCHING...\r"))
    }

    @Test
    fun searchingBeforeThePayloadIsDropped() {
        assertEquals(
            ElmResponse.Data(listOf(0x41, 0x0C, 0x1A, 0xF8)),
            ElmResponseParser.parse("SEARCHING...\r410C1AF8\r")
        )
    }

    @Test
    fun successfulBusInitBeforeThePayloadIsDropped() {
        assertEquals(
            ElmResponse.Data(listOf(0x41, 0x05, 0x5C)),
            ElmResponseParser.parse("BUS INIT: OK\r41055C\r")
        )
    }

    @Test
    fun failedBusInitIsReportedInsteadOfSwallowed() {
        val response = ElmResponseParser.parse("BUS INIT: ...ERROR\r")

        assertEquals(ElmFailure.BUS_INIT_FAILED, (response as ElmResponse.Failure).kind)
    }

    @Test
    fun adapterFailuresAreClassified() {
        val cases = mapOf(
            "UNABLE TO CONNECT\r" to ElmFailure.UNABLE_TO_CONNECT,
            "CAN ERROR\r" to ElmFailure.CAN_ERROR,
            "DATA ERROR\r" to ElmFailure.DATA_ERROR,
            "BUS ERROR\r" to ElmFailure.BUS_ERROR,
            "BUFFER FULL\r" to ElmFailure.BUFFER_FULL,
            "STOPPED\r" to ElmFailure.STOPPED,
            "LV RESET\r" to ElmFailure.LOW_POWER,
            "?\r" to ElmFailure.UNKNOWN_COMMAND,
            "ERROR\r" to ElmFailure.GENERIC_ERROR
        )

        cases.forEach { (frame, expected) ->
            val response = ElmResponseParser.parse(frame)
            assertEquals(frame, expected, (response as ElmResponse.Failure).kind)
        }
    }

    @Test
    fun adapterTextKeepsItsOriginalCase() {
        assertEquals(ElmResponse.Text("ELM327 v1.5"), ElmResponseParser.parse("ELM327 v1.5\r"))
        assertEquals(ElmResponse.Text("13.9V"), ElmResponseParser.parse("13.9V\r"))
    }

    @Test
    fun segmentedResponseIsReportedInsteadOfGuessed() {
        val frame = "0:410C1AF8112233\r1:4455667788\r"

        assertEquals(ElmResponse.Unhandled(frame), ElmResponseParser.parse(frame))
    }

    @Test
    fun truncatedHexIsReportedInsteadOfGuessed() {
        assertEquals(ElmResponse.Unhandled("410C1A F"), ElmResponseParser.parse("410C1A F\r"))
    }

    @Test
    fun emptyFrameIsReportedInsteadOfGuessed() {
        assertEquals(ElmResponse.Unhandled("\r\r"), ElmResponseParser.parse("\r\r"))
    }
}
