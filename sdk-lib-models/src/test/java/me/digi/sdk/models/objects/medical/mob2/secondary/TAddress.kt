/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Address
import me.digi.sdk.models.objects.ModelTestObject

object TAddress : ModelTestObject<Address>(
        Address(
                "dummyUse",
                "dummyType",
                "dummyText",
                listOf("dummyLine1", "dummyLine2"),
                "dummyCity",
                "dummyDistrict",
                "dummyState",
                "dummyPostalCode",
                "dummyCountry",
                TPeriod.obj
        ),
        """
        {
            "use":"dummyUse",
            "type":"dummyType",
            "text":"dummyText",
            "line":["dummyLine1","dummyLine2"],
            "city":"dummyCity",
            "district":"dummyDistrict",
            "state":"dummyState",
            "postalcode":"dummyPostalCode",
            "country":"dummyCountry",
            "period":${TPeriod.json}
        }
        """.trimIndent()
)