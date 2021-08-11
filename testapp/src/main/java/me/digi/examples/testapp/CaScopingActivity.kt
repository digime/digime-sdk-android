package me.digi.examples.testapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ca_scoping.*
import me.digi.sdk.entities.*

class CaScopingActivity : AppCompatActivity() {

    val test = CaScope()
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
        val serviceObjectTypesSocial : MutableList<ServiceObjectType> = mutableListOf()
        val serviceObjectTypeHealth : MutableList<ServiceObjectType> = mutableListOf()

        if(post.isChecked)
            serviceObjectTypesSocial.add(ServiceObjectType(2))

        if(comment.isChecked)
            serviceObjectTypesSocial.add(ServiceObjectType(7))

        if(media.isChecked)
            serviceObjectTypesSocial.add(ServiceObjectType(1))

        if(activity.isChecked)
            serviceObjectTypeHealth.add(ServiceObjectType(300))

        if(daily_activity.isChecked)
            serviceObjectTypeHealth.add(ServiceObjectType(301))

        if(achievement.isChecked)
            serviceObjectTypeHealth.add(ServiceObjectType(302))

        if(sleep.isChecked)
            serviceObjectTypeHealth.add(ServiceObjectType(303))

        val serviceTypesSocial: MutableList<ServiceType> = mutableListOf()
        val serviceTypesHealth: MutableList<ServiceType> = mutableListOf()

        if(facebook.isChecked)
            serviceTypesSocial.add(ServiceType(1, serviceObjectTypesSocial))

        if(instagram.isChecked)
            serviceTypesSocial.add(ServiceType(40, serviceObjectTypesSocial))

        if(twitter.isChecked)
            serviceTypesSocial.add(ServiceType(3, serviceObjectTypesSocial))

        if(pinterest.isChecked)
            serviceTypesSocial.add(ServiceType(9, serviceObjectTypesSocial))

        if(flickr.isChecked)
            serviceTypesSocial.add(ServiceType(12, serviceObjectTypesSocial))

        if(fitbit.isChecked)
            serviceTypesHealth.add(ServiceType(18, serviceObjectTypeHealth))

        if(garmin.isChecked)
            serviceTypesHealth.add(ServiceType(21, serviceObjectTypeHealth))

        if(google.isChecked)
            serviceTypesHealth.add(ServiceType(33, serviceObjectTypeHealth))

        val serviceGroups: MutableList<ServiceGroup> = mutableListOf()


        if(social.isChecked)
            serviceGroups.add(ServiceGroup(1, serviceTypesSocial))

        if(health.isChecked)
            serviceGroups.add(ServiceGroup(4, serviceTypesHealth))

        test.serviceGroups = serviceGroups

        test.timeRanges = listOf(TimeRange(null, null, null, "all"))
    }

}