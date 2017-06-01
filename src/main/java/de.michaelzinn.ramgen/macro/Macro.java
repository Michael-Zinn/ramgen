package de.michaelzinn.ramgen.macro;

import de.michaelzinn.ramgen.java.JParameter;
import de.michaelzinn.ravr.Ravr;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import static de.michaelzinn.ravr.Placeholder.__;
import static de.michaelzinn.ravr.Ravr.*;

/**
 * Created by michael on 01.06.17.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class Macro {


    List<String> genericNames;

    SubMacro<String> typeMacro;
    String name;
    List<String> initialParameters;
    SubMacro parameterMacro;
    List<String> trailingParameters;

    String bodyPre;
    SubMacro<Tuple2<String, String>> bodyMacro;
    String bodyPost;

    public String expandOne(int macroParameterCount) {

        return "\tpublic static " + joinOption(", ", genericNames.take(macroParameterCount + 1)).map(s -> "<" + s + ">\n\t").getOrElse("") +
                typeMacro.expand(this, macroParameterCount, macroParameterCount) + " " +
                name + "(\n\t\t" +
                doWith(
                        List.of(
                                joinOption(", ", initialParameters),
                                doWith(List.range(0, macroParameterCount),
                                        map(i -> parameterMacro.expand(this, i, macroParameterCount)),
                                        joinOption(",\n\t\t")
                                ),
                                joinOption(", ", trailingParameters)
                        ),
                        Ravr::concatOptions,
                        join(",\n\t\t")
                ) + "\n\t) {\n" +
                bodyPre +
                expandBody(macroParameterCount -1, macroParameterCount -1) +
                bodyPost +
                "\t}";
    }

    private String expandBody(int i, int max) {
        if(i == 0) {
            return bodyMacro.expand(this, 0, max).apply((a, b) -> a + b);
        } else {
            return bodyMacro.expand(this, i, max).apply((a, b) -> a + expandBody(i - 1, max) + b);
        }
    }

    public List<String> expand(int maxMacroParameterCount) {
        return List.rangeClosed(1, maxMacroParameterCount).map(this::expandOne);
    }

    @FunctionalInterface
    public interface SubMacro<T> {
        T expand(
                Macro macro,
                int currentIteration,
                int maxIteration
        );
    }

}