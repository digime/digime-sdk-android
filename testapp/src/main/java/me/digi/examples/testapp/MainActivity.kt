package me.digi.examples.testapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ca_flow.setOnClickListener {
            val intent = Intent(this, CaScopingActivity::class.java)
            startActivity(intent)
        }

        push_data.setOnClickListener {
            val intent = Intent(this, Postbox::class.java)
            startActivity(intent)
        }
    }
}
