package me.digi.examples.ongoing.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.JsonAdapter
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.lang.reflect.Type

@JsonAdapter(Song.Adapter::class)
@Entity
class Song(
    @Expose var genre: String
) {

    @Id var id: Long = 0

    inner class Adapter : JsonDeserializer<Song> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Song {
            val root = json.let { it as? JsonObject } ?: throw IllegalArgumentException()
            val track = root.getAsJsonObject("track")
            val artists = track.getAsJsonArray("artists")
            val genres = artists.mapNotNull { (it as? JsonObject)?.getAsJsonArray("genres")?.map { it.asJsonPrimitive.asString } }.flatten()
            return Song(genres.joinToString(","))
        }
    }
}