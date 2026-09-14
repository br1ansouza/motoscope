package dev.br1ansouza.motoscope.protocol.elm327

data class SupportedPidRange(val base: Int, val pids: List<Int>) {
    init {
        require(base in BYTE_MIN..BYTE_MAX) { "Base fora de faixa: $base." }
        require(pids.all { it in BYTE_MIN..BYTE_MAX }) { "PID fora de faixa em $pids." }
    }

    val nextRangeSupported: Boolean = pids.contains(base + SupportedPids.RANGE_WIDTH)
}

object SupportedPids {
    const val RANGE_WIDTH = 0x20

    val PROBE_ORDER: List<Int> = List(RANGE_COUNT) { it * RANGE_WIDTH }

    fun decode(base: Int, payload: List<Int>): SupportedPidRange? {
        if (payload.size < MASK_BYTES) return null
        val mask = payload.take(MASK_BYTES).fold(0L) { accumulated, byte ->
            (accumulated shl Byte.SIZE_BITS) or byte.toLong()
        }
        val pids = (0 until MASK_BITS)
            .filter { bit -> mask and (1L shl (MASK_BITS - 1 - bit)) != 0L }
            .map { bit -> base + bit + 1 }
        return SupportedPidRange(base, pids)
    }

    private const val RANGE_COUNT = 7
    private const val MASK_BYTES = 4
    private const val MASK_BITS = 32
}
