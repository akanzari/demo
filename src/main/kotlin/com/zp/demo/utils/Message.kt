package com.zp.demo.utils

import java.time.Instant

interface Command {
    val timestamp: Instant
}

interface Event {
    val timestamp: Instant
}
