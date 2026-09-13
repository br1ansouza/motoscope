package dev.br1ansouza.motoscope.protocol.elm327

class ElmFrameAccumulator {
    private val buffer = StringBuilder()

    fun append(chunk: String): List<String> {
        buffer.append(chunk)
        if (!buffer.contains(PROMPT)) return emptyList()

        val parts = buffer.toString().split(PROMPT)
        buffer.setLength(0)
        buffer.append(parts.last())
        return parts.dropLast(1)
    }

    fun pending(): String = buffer.toString()

    fun reset() = buffer.setLength(0)

    private companion object {
        const val PROMPT = '>'
    }
}
