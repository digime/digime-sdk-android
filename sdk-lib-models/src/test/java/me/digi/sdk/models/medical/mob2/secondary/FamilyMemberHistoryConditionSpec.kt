/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TFamilyMemberHistoryCondition

class FamilyMemberHistoryConditionSpec : ModelTest<FamilyMemberHistoryCondition>(FamilyMemberHistoryCondition::class.java) {
    override val emptyTest: FamilyMemberHistoryCondition? = FamilyMemberHistoryCondition(
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<FamilyMemberHistoryCondition?, String>> =
            listOf(
                    Pair(
                            TFamilyMemberHistoryCondition.obj,
                            TFamilyMemberHistoryCondition.json
                    )
            )
}