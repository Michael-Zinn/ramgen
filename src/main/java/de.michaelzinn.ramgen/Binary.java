package de.michaelzinn.ramgen;

import io.vavr.collection.List;

import static de.michaelzinn.ravr.Ravr.always;
import static de.michaelzinn.ravr.Ravr.applyTuple;

/**
 * Created by michael on 26.05.17.
 */
public class Binary {

    // TODO move to Ravr
    public static <A>
    List<A> repeat(A a, int n) {
        return List.range(0, n).map(always(a));
    }

    /**
     *
     * @param digits How many digits the result should have
     * @param number number to convert
     * @return little endian list where false means 0 and true means 1
     */
    public static List<Boolean> toLittleEndianBooleans(Integer digits, Integer number) {

        return List.range(0, digits).map( index ->
                        ((number >> index) & 1) == 1
                );
    }
}
