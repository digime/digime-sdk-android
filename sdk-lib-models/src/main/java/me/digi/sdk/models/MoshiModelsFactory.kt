/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models

import com.squareup.moshi.Moshi
import me.digi.sdk.models.government.VehicleTest
import me.digi.sdk.models.health_and_fitness.DistanceUnit
import me.digi.sdk.models.health_and_fitness.LengthUnit
import me.digi.sdk.models.health_and_fitness.WaterUnit
import me.digi.sdk.models.health_and_fitness.WeightUnit
import me.digi.sdk.models.social.Media
import me.digi.sdk.models.social.Post

fun Moshi.Builder.addDigimeModelsAdapters(): Moshi.Builder =
        this
                .add(JFSAccount.Companion.AccountType.Companion)
                .add(Media.Companion.MediaType.Companion)
                .add(VehicleTest.Companion.TestResult.Companion)
                .add(Post.Companion.PostType.Companion)
                .add(WaterUnit.Companion)
                .add(LengthUnit.Companion)
                .add(DistanceUnit.Companion)
                .add(WeightUnit.Companion)