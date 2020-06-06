package jp.gr.java_conf.foobar.testmaker.service.di

import com.google.firebase.Timestamp
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class TimestampJsonAdapter : JsonAdapter<Timestamp>() {

    @Synchronized
    @Throws(Exception::class)
    override fun fromJson(reader: JsonReader): Timestamp {
        var secs = 0L
        var nanos = 0
        reader.beginObject()
        if (reader.nextName() == "secs_since_epoch") {
            secs = reader.nextLong()
        }
        if (reader.nextName() == "nanos_since_epoch") {
            nanos = reader.nextInt()
        }
        reader.endObject()
        return Timestamp(secs, nanos)
    }

    @Synchronized
    @Throws(Exception::class)
    override fun toJson(writer: JsonWriter, value: Timestamp?) {
        writer.value(value.toString())
    }
}
