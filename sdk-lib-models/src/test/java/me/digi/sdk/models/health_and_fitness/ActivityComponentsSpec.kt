/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.health_and_fitness.*

class LevelSpec : ModelTest<Level>(Level::class.java) {
    override val emptyTest: Level? = Level(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Level?, String>> =
            listOf(
                    Pair(
                            TLevel.obj,
                            TLevel.json
                    )
            )
}

class ManualValuesSpec : ModelTest<ManualValues>(ManualValues::class.java) {
    override val emptyTest: ManualValues? = ManualValues(
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<ManualValues?, String>> =
            listOf(
                    Pair(
                            TManualValues.obj,
                            TManualValues.json
                    )
            )
}

class SourceSpec : ModelTest<Source>(Source::class.java) {
    override val emptyTest: Source? = Source(
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Source?, String>> =
            listOf(
                    Pair(
                            TSource.obj,
                            TSource.json
                    )
            )
}

class DurationSpec : ModelTest<Duration>(Duration::class.java) {
    override val emptyTest: Duration? = Duration(
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Duration?, String>> =
            listOf(
                    Pair(
                            TDuration.obj,
                            TDuration.json
                    )
            )
}

class HeartRateZoneSpec : ModelTest<HeartRateZone>(HeartRateZone::class.java) {
    override val emptyTest: HeartRateZone? = HeartRateZone(
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<HeartRateZone?, String>> =
            listOf(
                    Pair(
                            THeartRateZone.obj,
                            THeartRateZone.json
                    )
            )
}
