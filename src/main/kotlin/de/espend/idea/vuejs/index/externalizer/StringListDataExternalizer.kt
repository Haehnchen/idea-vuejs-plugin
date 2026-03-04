package de.espend.idea.vuejs.index.externalizer

import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import java.io.DataInput
import java.io.DataOutput

class StringListDataExternalizer : DataExternalizer<List<String>> {
    companion object {
        @JvmField
        val INSTANCE: StringListDataExternalizer = StringListDataExternalizer()
    }

    @Synchronized
    override fun save(out: DataOutput, value: List<String>) {
        out.writeInt(value.size)
        for (s in value) {
            EnumeratorStringDescriptor.INSTANCE.save(out, s)
        }
    }

    @Synchronized
    override fun read(`in`: DataInput): List<String> {
        val set = mutableListOf<String>()
        var r = `in`.readInt()
        while (r > 0) {
            set.add(EnumeratorStringDescriptor.INSTANCE.read(`in`))
            r--
        }
        return set
    }
}
