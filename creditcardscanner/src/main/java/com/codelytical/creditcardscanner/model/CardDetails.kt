package com.codelytical.creditcardscanner.model

data class CardDetails(
    val number: String?,
    val expirationMonth: String?,
    val expirationYear: String?,
    val cvv: String?,
)