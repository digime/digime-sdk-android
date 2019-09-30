package me.digi.examples.barebonesapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import me.digi.barebonesapp.postbox.PostboxActivity
import me.digi.examples.barebonesapp.consentaccess.ConsentAccessActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenPostboxEx.setOnClickListener {
            val intent = Intent(this, PostboxActivity::class.java)
            startActivity(intent)
        }
        btnOpenCAEx.setOnClickListener {
            val intent = Intent(this, ConsentAccessActivity::class.java)
            startActivity(intent)
        }
    }
}
