package com.codelytical.creditcardscanner.usecase

import com.codelytical.creditcardscanner.model.CardDetails

object Extractor {

    fun extractData(input: String): CardDetails {
        val lines = input.split("\n")

        val number = extractNumber(lines)
        val (month, year) = extractExpiration(lines)
        val cvv = extractCvv(lines, number)
        return CardDetails(
            number = number,
            expirationMonth = month,
            expirationYear = year,
            cvv = cvv
        )
    }

    private fun extractNumber(lines: List<String>): String? {
        // Clean and prepare lines for processing
        val cleanedLines = lines.map { it.replace(Regex("[^\\d -]"), "") }

        // Regular expression to match a broad range of credit card number formats
        val regex = Regex(pattern = """(?:\d[ -]?){13,19}\d""")

        // Search for a line that matches the credit card number pattern
        val potentialNumbers = cleanedLines.mapNotNull { line ->
            regex.find(line)?.value?.replace(Regex("[ -]"), "") // Normalize the number by removing spaces or dashes
        }

        // Validate extracted numbers using the Luhn algorithm and return the first valid one
        return potentialNumbers.firstOrNull { it.isValidCardNumber() }
    }

    // Extension function to validate a credit card number using the Luhn algorithm
    private fun String.isValidCardNumber(): Boolean {
        val digits = this.map { it.toString().toIntOrNull() ?: return false }
        val sum = digits.reversed().mapIndexed { index, n ->
            if (index % 2 == 1) (n * 2).let { if (it > 9) it - 9 else it } else n
        }.sum()
        println("SUM NUMBER HERE: $sum")
        println("SUM NUMBER HERE MOD: ${sum % 10}")
        return sum % 10 == 0
    }

    private fun extractExpiration(lines: List<String>): Pair<String?, String?> {
        val expirationLine = extractExpirationLine(lines)

        val month = expirationLine?.substring(startIndex = 0, endIndex = 2)
        val year = expirationLine?.substring(startIndex = 3)
        return Pair(month, year)
    }

    private fun extractExpirationLine(lines: List<String>) =
        lines.flatMap { it.split(" ") }
            .firstOrNull { (it.length == 5 || it.length == 7) && it[2] == '/' }


    private fun extractCvv(lines: List<String>, previouslyCapturedCardNumber: String?): String? {
        // Exclude the last 4 digits of the captured card number from being considered as CVV
        val lastFourDigits = previouslyCapturedCardNumber?.takeLast(4)

        val regex = Regex(pattern = """\b\d{3,4}\b""")

        // Search through each line and split them into words to find a match
        return lines.flatMap { it.split(" ") }
            .mapNotNull { word ->
                regex.find(word)?.value
            }
            .firstOrNull {
                it.length == 3 || (it.length == 4 && previouslyCapturedCardNumber?.startsWith("3") == true)
                        && it != lastFourDigits // Ensure the found number is not the last 4 digits of the card number
            }
    }


}