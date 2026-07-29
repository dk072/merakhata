package com.merakhata.app.data.model

enum class TransactionType {
    YOU_GAVE, // Credit given to customer -> User will receive money (Receivable)
    YOU_GOT   // Payment received from customer -> Reduces customer's debt
}
