package de.espend.idea.vuejs.index.externalizer;

import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see com.jetbrains.php.lang.psi.stubs.indexes.PhpTraitUsageIndex
 * @see com.jetbrains.php.lang.psi.stubs.indexes.StringSetDataExternalizer
 */
public class StringListDataExternalizer implements DataExternalizer<List<String>> {

    public static StringListDataExternalizer INSTANCE = new StringListDataExternalizer();

    public synchronized void save(@NotNull DataOutput out, List<String> value) throws IOException {
        out.writeInt(value.size());

        for (String s : value) {
            EnumeratorStringDescriptor.INSTANCE.save(out, s);
        }
    }

    public synchronized List<String> read(@NotNull DataInput in) throws IOException {
        List<String> set = new ArrayList<>();

        for(int r = in.readInt(); r > 0; --r) {
            set.add(EnumeratorStringDescriptor.INSTANCE.read(in));
        }

        return set;
    }
}