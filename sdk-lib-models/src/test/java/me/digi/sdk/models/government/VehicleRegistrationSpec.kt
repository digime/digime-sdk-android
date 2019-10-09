/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.government

import me.digi.sdk.models.ModelTest

class VehicleRegistrationSpec : ModelTest<VehicleRegistration>(VehicleRegistration::class.java) {
    override val emptyTest: VehicleRegistration? = null

    override val jsonObjectTests: List<Pair<VehicleRegistration?, String>> =
            listOf(
                    Pair(
                            VehicleRegistration(
                                    "dummyAccountEntityId",
                                    "dummyColour",
                                    1,
                                    2,
                                    "dummyEntityId",
                                    "dummyFuelType",
                                    3,
                                    "dummyId",
                                    4,
                                    "dummyMake",
                                    "dummyModel",
                                    5,
                                    "dummyTransmission"
                            ),
                            """
                                {
                                   "accountentityid":"dummyAccountEntityId",
                                   "colour":"dummyColour",
                                   "createddate":1,
                                   "enginecapacity":2,
                                   "entityid":"dummyEntityId",
                                   "fueltype":"dummyFuelType",
                                   "gearcount":3,
                                   "id":"dummyId",
                                   "manufacturedate":4,
                                   "make":"dummyMake",
                                   "model":"dummyModel",
                                   "registrationdate":5,
                                   "transmission":"dummyTransmission"
                                }
                            """.trimIndent()
                    )
            )
}