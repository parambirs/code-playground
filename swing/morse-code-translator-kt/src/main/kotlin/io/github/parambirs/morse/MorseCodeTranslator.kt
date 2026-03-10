package io.github.parambirs.morse

object MorseCodeTranslator {
    private val englishToMorseLib = mapOf(
        "A" to ".-",
        "B" to "-...",
        "C" to "-.-.",
        "D" to "-..",
        "E" to ".",
        "F" to "..-.",
        "G" to "--.",
        "H" to "....",
        "I" to "..",
        "J" to ".---",
        "K" to "-.-",
        "L" to ".-..",
        "M" to "--",
        "N" to "-.",
        "O" to "---",
        "P" to ".--.",
        "Q" to "--.-",
        "R" to ".-.",
        "S" to "...",
        "T" to "-",
        "U" to "..-",
        "V" to "...-",
        "W" to ".--",
        "X" to "-..-",
        "Y" to "-.--",
        "Z" to "--..",

        "0" to "-----",
        "1" to ".----",
        "2" to "..---",
        "3" to "...--",
        "4" to "....-",
        "5" to ".....",
        "6" to "-....",
        "7" to "--...",
        "8" to "---..",
        "9" to "----.",

        "." to ".-.-.-",
        "," to "--..--",
        "?" to "..--..",
        ":" to "---...",
        "-" to "-....-",
        "@" to ".--.-.",
        "eror" to "........",
    )

    // fill the morseToEnglishLib
    private val morseToEnglishLib = englishToMorseLib.map { (key, value) -> value to key }.toMap()

    fun englishWordToMorseWord(englishWord: String): String {
        val buffer = StringBuilder()
        for (word in englishWord.split(Regex("[ \n]"))) {
            for (c in word.toCharArray()) {
                buffer.append(englishToMorseLib[c.uppercase()] ?: "?? ")
                buffer.append(" ")
            }
            buffer.append(" / ")
        }
        return buffer.toString()
    }

    fun morseWordToEnglishWord(morseWord: String): String {
        val buffer = StringBuilder()
        for (word in morseWord.split(Regex("[\\s\\n]"))) {
            when {
                word.isEmpty() -> continue
                word == "/" || word == "|" -> buffer.append(" ")
                else ->
                    buffer.append(morseToEnglishLib[word]?.lowercase() ?: "?? ")
            }
        }
        return buffer.toString()
    }
}
