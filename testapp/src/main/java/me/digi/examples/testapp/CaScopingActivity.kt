package me.digi.examples.testapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ca_scoping.*
import me.digi.sdk.entities.*

class CaScopingActivity : AppCompatActivity() {

    val test = DMEScope()
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ca_scoping)

        ca_scoping.visibility = View.GONE

        next.setOnClickListener {
            val intent = Intent(this, CaFlow::class.java)

            if(enable_ca_scoping.isChecked) {
                getData()
                intent.putExtra("DMEScope", gson.toJson(test))
            }

            startActivity(intent)
        }

        enable_ca_scoping.setOnCheckedChangeListener { _, b ->
            if(b)
                ca_scoping.visibility = View.VISIBLE
            else
                ca_scoping.visibility = View.GONE
        }
    }

    private fun getData(){
        val serviceObjectTypesSocial : MutableList<DMEServiceObjectType> = mutableListOf()
        val serviceObjectTypeHealth : MutableList<DMEServiceObjectType> = mutableListOf()

        if(post.isChecked)
            serviceObjectTypesSocial.add(DMEServiceObjectType(2))

        if(comment.isChecked)
            serviceObjectTypesSocial.add(DMEServiceObjectType(7))

        if(media.isChecked)
            serviceObjectTypesSocial.add(DMEServiceObjectType(1))

        if(activity.isChecked)
            serviceObjectTypeHealth.add(DMEServiceObjectType(300))

        if(daily_activity.isChecked)
            serviceObjectTypeHealth.add(DMEServiceObjectType(301))

        if(achievement.isChecked)
            serviceObjectTypeHealth.add(DMEServiceObjectType(302))

        if(sleep.isChecked)
            serviceObjectTypeHealth.add(DMEServiceObjectType(303))

        val serviceTypesSocial: MutableList<DMEServiceType> = mutableListOf()
        val serviceTypesHealth: MutableList<DMEServiceType> = mutableListOf()

        if(facebook.isChecked)
            serviceTypesSocial.add(DMEServiceType(1, serviceObjectTypesSocial))

        if(instagram.isChecked)
            serviceTypesSocial.add(DMEServiceType(40, serviceObjectTypesSocial))

        if(twitter.isChecked)
            serviceTypesSocial.add(DMEServiceType(3, serviceObjectTypesSocial))

        if(pinterest.isChecked)
            serviceTypesSocial.add(DMEServiceType(9, serviceObjectTypesSocial))

        if(flickr.isChecked)
            serviceTypesSocial.add(DMEServiceType(12, serviceObjectTypesSocial))

        if(fitbit.isChecked)
            serviceTypesHealth.add(DMEServiceType(18, serviceObjectTypeHealth))

        if(garmin.isChecked)
            serviceTypesHealth.add(DMEServiceType(21, serviceObjectTypeHealth))

        if(google.isChecked)
            serviceTypesHealth.add(DMEServiceType(33, serviceObjectTypeHealth))

        val serviceGroups: MutableList<DMEServiceGroup> = mutableListOf()


        if(social.isChecked)
            serviceGroups.add(DMEServiceGroup(1, serviceTypesSocial))

        if(health.isChecked)
            serviceGroups.add(DMEServiceGroup(4, serviceTypesHealth))

        test.serviceGroups = serviceGroups

        test.timeRanges = listOf(DMETimeRange(null, null, null, "all"))
    }

}