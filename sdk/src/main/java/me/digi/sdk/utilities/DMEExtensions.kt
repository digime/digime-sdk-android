package me.digi.sdk.utilities

import android.os.Bundle

fun Bundle.toMap() = this.keySet().map { it to this.get(it) }.toMap()