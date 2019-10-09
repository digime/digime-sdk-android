/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TOrganization

class OrganizationSpec : ModelTest<Organization>(Organization::class.java) {
    override val emptyTest: Organization? = Organization(
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Organization?, String>> =
            listOf(
                    Pair(
                            TOrganization.obj,
                            TOrganization.json
                    )
            )
}