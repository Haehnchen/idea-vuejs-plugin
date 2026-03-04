package de.espend.idea.vuejs.index.externalizer

import com.intellij.util.io.DataExternalizer
import java.io.*

class ObjectStreamDataExternalizer<T : Serializable> : DataExternalizer<T?> {
    override fun save(out: DataOutput, value: T?) {
        val stream = ByteArrayOutputStream()
        val output = ObjectOutputStream(stream)
        output.writeObject(value)

        out.writeInt(stream.size())
        out.write(stream.toByteArray())
    }

    @Suppress("UNCHECKED_CAST")
    override fun read(`in`: DataInput): T? {
        val bufferSize = `in`.readInt()
        val buffer = ByteArray(bufferSize)
        `in`.readFully(buffer, 0, bufferSize)

        val stream = ByteArrayInputStream(buffer)
        val input = ObjectInputStream(stream)

        return try {
            input.readObject() as T
        } catch (_: ClassNotFoundException) {
            null
        } catch (_: ClassCastException) {
            null
        }
    }
}
