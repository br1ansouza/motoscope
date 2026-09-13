package dev.br1ansouza.motoscope.protocol.elm327

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ElmCommandTest {
    @Test
    fun requestWithPidIsTwoHexBytes() {
        assertEquals("010C", ElmCommand.ObdRequest(mode = 0x01, pid = 0x0C).text)
        assertEquals("0105", ElmCommand.ObdRequest(mode = 0x01, pid = 0x05).text)
    }

    @Test
    fun requestWithoutPidCarriesOnlyTheMode() {
        assertEquals("03", ElmCommand.ObdRequest(mode = 0x03, pid = null).text)
    }

    @Test
    fun requestOutsideTheByteRangeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ElmCommand.ObdRequest(mode = 0x100, pid = null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ElmCommand.ObdRequest(mode = 0x01, pid = -1)
        }
    }

    @Test
    fun atCommandWithoutTheAtPrefixIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { ElmCommand.At("Z") }
    }

    @Test
    fun initializationSilencesEchoBeforeAskingForTheProtocol() {
        val texts = ElmInitialization.DEFAULT.map { it.text }

        assertEquals(
            listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0", "ATDP"),
            texts
        )
    }
}
