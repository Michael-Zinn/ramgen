package de.michaelzinn.ramgen.java;

import de.michaelzinn.ramgen.json.JsonFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import io.vavr.API.*;

import static de.michaelzinn.ravr.Ravr.nullTo;

/**
 * Created by michael on 29.05.17.
 */
@Getter
@AllArgsConstructor
public class JFunction {

    public enum Generate {ALL, NONE, UNIQUE}

    public enum Status {WORKS, INCOMPLETE, MISSING, REJECTED, BONUS}

    Generate generate;
    JSignature signature;
    Status status;
    String comment;

    public static JFunction of(JsonFunction jsonFunction) {
        return new JFunction(
                Generate.valueOf(nullTo("all",jsonFunction.getGenerate()).toUpperCase()),
                JSignature.fromJsonSignature(jsonFunction.getSignature()),
                Status.valueOf(jsonFunction.getStatus().toUpperCase()),
                jsonFunction.getComment()
        );
    }
}
