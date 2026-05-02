package com.example.shiftmark

import java.util.UUID

data class Timestamp(
    val id: String = UUID.randomUUID().toString(),
    val time: String,
    var title: String = "",
    var notes: String = ""
)