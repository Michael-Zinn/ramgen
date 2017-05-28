package de.michaelzinn.ramgen;

import io.vavr.Tuple2;
import io.vavr.collection.List;

import java.util.function.Predicate;

import static de.michaelzinn.ravr.Ravr.applyTuple;
import static de.michaelzinn.ravr.Ravr.pipe;

/**
 * Created by michael on 26.05.17.
 */
public class FunctionUtils {
    public static boolean even(Integer i) {
        return i % 2 == 0;
    }

    public static boolean odd(Integer i) {
        return i % 2 == 1;
    }

    public static <A> List<A> filterIndex(Predicate<Integer> indexFilter, List<A> list) {
        return list
                .zipWithIndex()
                .filter(fuck -> indexFilter.test(fuck._2))
                .map(Tuple2::_1);
    }

    public static <A> List<A> evens(List<A> list) {
        return filterIndex(FunctionUtils::even, list);
    }

    public static <A> List<A> odds(List<A> list) {
        return filterIndex(FunctionUtils::odd, list);
    }
}
