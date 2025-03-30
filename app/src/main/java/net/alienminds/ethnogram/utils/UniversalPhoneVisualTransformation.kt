package net.alienminds.ethnogram.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation


class UniversalPhoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }

        if (digits.length < 3) {
            return TransformedText(AnnotatedString(digits), OffsetMapping.Identity)
        }

        return when {
            digits.startsWith("994") -> genericTransform(digits, "+994", "##-##-##-##")
            digits.startsWith("374") -> genericTransform(digits, "+374", "##-##-##-##")
            digits.startsWith("375") -> genericTransform(digits, "+375", "##-###-##-##")
            digits.startsWith("996") -> genericTransform(digits, "+996", "##-###-##-##")
            digits.startsWith("992") -> genericTransform(digits, "+992", "##-###-##-##")
            digits.startsWith("998") -> genericTransform(digits, "+998", "##-###-##-##")
            digits.startsWith("79") || digits.startsWith("89") -> transformRussia(digits)
            else -> transformKorea(digits) // fallback всегда на Корею
        }
    }

    private fun transformRussia(digits: String): TransformedText {
        val raw = if (digits.startsWith("8")) "7" + digits.drop(1) else digits
        val masked = raw.take(11)

        val formatted = buildString {
            append("+7 ")
            masked.drop(1).forEachIndexed { index, char ->
                when (index) {
                    0 -> append("($char")
                    2 -> append("$char) ")
                    5, 7 -> append("-$char")
                    else -> append(char)
                }
            }
        }

        val offsetMapping = createOffsetMapping(masked, formatted, "+7 (###) ###-##-##")
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    private fun transformKorea(raw: String): TransformedText {
        val is010 = raw.startsWith("010")
        val is82 = raw.startsWith("82")

        // Ограничиваем пользовательский ввод
        val baseDigits = raw.take(if (is010) 11 else 12)

        // Добавляем код страны если нужно, финальный максимум — 82 + 10 цифр
        val digits = if (is010) "82" + baseDigits.drop(1) else baseDigits
        val localDigits = when {
            is010 || is82 -> digits.drop(2) // убираем код страны для отображения
            else -> digits
        }

        val formatted = buildString {
            append("+82 ")
            localDigits.forEachIndexed { i, c ->
                when (i) {
                    2, 6 -> append("-$c")
                    else -> append(c)
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    is010 -> listOf(5, 6, 7, 9, 10, 11, 13, 14, 15, 16, 17)
                        .getOrNull(offset)
                        ?.coerceAtMost(formatted.length)
                        ?: formatted.length

                    is82 -> {
                        val shift = 4 + (if (offset >= 2) 1 else 0) + (if (offset >= 6) 1 else 0)
                        (offset + shift).coerceAtMost(formatted.length)
                    }

                    else -> {
                        // fallback без префикса
                        val shift = 4 + (if (offset >= 2) 1 else 0) + (if (offset >= 6) 1 else 0)
                        (offset + shift).coerceAtMost(formatted.length)
                    }
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    is010 -> when (offset) {
                        in 0..5 -> 0
                        in 6..6 -> 1
                        in 7..8 -> 2
                        in 9..9 -> 3
                        in 10..10 -> 4
                        in 11..12 -> 5
                        in 13..13 -> 6
                        in 14..14 -> 7
                        in 15..15 -> 8
                        in 16..16 -> 9
                        in 17..17 -> 10
                        else -> raw.length
                    }

                    is82 -> when (offset) {
                        in 0..4 -> 0
                        in 5..5 -> 1
                        in 6..6 -> 2
                        in 7..8 -> 3
                        in 9..9 -> 4
                        in 10..10 -> 5
                        in 11..12 -> 6
                        in 13..13 -> 7
                        in 14..14 -> 8
                        in 15..15 -> 9
                        in 16..16 -> 10
                        else -> raw.length
                    }

                    else -> {
                        // fallback без префикса — отображаемый текст такой же, как и digits, с форматированием
                        when (offset) {
                            in 0..4 -> 0
                            in 5..5 -> 1
                            in 6..6 -> 2
                            in 7..8 -> 3
                            in 9..9 -> 4
                            in 10..10 -> 5
                            in 11..12 -> 6
                            in 13..13 -> 7
                            in 14..14 -> 8
                            in 15..15 -> 9
                            in 16..16 -> 10
                            else -> raw.length
                        }
                    }
                }.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    private fun genericTransform(raw: String, code: String, pattern: String): TransformedText {
        val maxDigits = pattern.count { it == '#' }
        val digits = raw.take(code.drop(1).length + maxDigits)

        val formatted = buildString {
            append("$code ")
            var digitIndex = code.drop(1).length
            var usedDigits = 0

            for (char in pattern) {
                if (char == '#') {
                    if (digitIndex < digits.length) {
                        append(digits[digitIndex++])
                        usedDigits++
                    }
                } else {
                    // Добавлять только если дальше есть цифры
                    if (usedDigits < digits.length - code.drop(1).length) {
                        append(char)
                    }
                }
            }
        }

        val offsetMapping = createOffsetMapping(digits, formatted, "$code $pattern")
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }


    private fun createOffsetMapping(original: String, transformed: String, mask: String): OffsetMapping {
        val originalToTransformed = mutableMapOf<Int, Int>()
        val transformedToOriginal = mutableMapOf<Int, Int>()

        var originalIndex = 0
        var transformedIndex = 0

        while (originalIndex < original.length && transformedIndex < transformed.length) {
            if (mask.getOrNull(transformedIndex) == '#') {
                originalToTransformed[originalIndex] = transformedIndex
                transformedToOriginal[transformedIndex] = originalIndex
                originalIndex++
            }
            transformedIndex++
        }

        return object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return originalToTransformed[offset] ?: transformed.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transformedToOriginal[offset] ?: original.length
            }
        }
    }
}
fun phoneToFbPhone(phone: String):String {
    return when {
        phone.take(2) == "82" -> "+82${phone.drop(2).take(10)}"
        phone.take(3) == "010" -> "+82${phone.drop(1).take(10)}"
        phone.take(2) == "89" || phone.take(2) == "79" -> "+79${phone.drop(2).take(9)}"
        phone.take(3) == "994" || phone.take(3) == "374" -> "+${phone.take(11)}"
        phone.take(3) == "375" || phone.take(3) == "996" || phone.take(3) == "998"
                || phone.take(3) == "992" -> "+${phone.take(12)}"
        else -> "+82${phone.take(10)}"
    }
}