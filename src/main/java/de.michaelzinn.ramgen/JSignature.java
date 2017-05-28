package de.michaelzinn.ramgen;

import de.michaelzinn.ramgen.json.JsonSignature;
import de.michaelzinn.ravr.Ravr;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import static de.michaelzinn.ramgen.FunctionUtils.evens;
import static de.michaelzinn.ramgen.FunctionUtils.odds;
import static de.michaelzinn.ravr.Ravr.applyTuple;


/**
 * A Java method signature, limited to the relevant parts
 *
 * Created by michael on 26.05.17.
 */
@Getter
@AllArgsConstructor
public class JSignature {

    List<String> generics;
    String type; String name; List<JParameter> parameters;


    public int getArity() {
        return parameters.size();
    }

    public static JSignature def(List<String> generics, String type, String name, List<String> rawParameters) {
        List<JParameter> parameters =
                evens(rawParameters).zip(odds(rawParameters))
                .map(applyTuple((l, r) -> new JParameter(l, r)));

        return new JSignature(generics, type, name, parameters);
    }

    public static JSignature fromJsonSignature(JsonSignature jsonSig) {
        List<JParameter> parameters =
                evens(jsonSig.parameters).zip(odds(jsonSig.parameters))
                .map(applyTuple((l, r) -> new JParameter(l, r)));

        return new JSignature(jsonSig.generics, jsonSig.type, jsonSig.name, parameters);
    }

}
