package me.digi.examples.ongoing.service

import android.content.Context
import io.objectbox.BoxStore
import me.digi.examples.ongoing.model.MyObjectBox

object ObjectBox {
    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}