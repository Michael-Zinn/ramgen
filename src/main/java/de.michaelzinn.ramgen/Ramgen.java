package de.michaelzinn.ramgen;

import de.michaelzinn.ramgen.json.*;
import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import lombok.val;


import java.util.function.Predicate;

import static de.michaelzinn.ramgen.JSignature.def;
import static de.michaelzinn.ramgen.ThreadingMacro.*;
import static de.michaelzinn.ravr.Ravr.*;
import static io.vavr.API.*;


/**
 * Some Java code that uses Ravr to generate some Java code that's used by Ravr.
 * <p>
 * Created by michael on 26.05.17.
 */
public class Ramgen {

    // shortcut
    static <A> List<A> L(A... list) {
        return List.of(list);
    }


    static Function1<JSignature, String> generateGenerics = pipe(
            JSignature::getGenerics,
            joinOption(", "),
            option -> option.map(g -> "<" + g + ">\n"),
            defaultTo("")
    );

    /*
    static String generateGenerics(JSignature sig) {

        return pipe(
                JSignature::getGenerics,
                joinOption(", "),
                option -> option.map(g -> "<" + g + ">\n"),
                defaultTo("")
        ).apply(sig);

    }
    */

    static String generatePartialParameters(List<Boolean> placeholders, JSignature sig) {
        return pipe(
                map(applyTuple((Boolean keep, JParameter param) ->
                        (keep ? param.getType() : "Placeholder") + " " + param.getName()
                )),
                join(", ")
        ).apply(placeholders.zip(sig.parameters));
    }

    static boolean not(Boolean b) {
        return !b;
    }

    static Predicate<Boolean> not() {
        return b -> !b;
    }

    static int countFalse(List<Boolean> booleans) {
        return booleans.count(Ramgen::not);
    }

    static List<JParameter> unused(List<Boolean> placeholders, JSignature sig) {
        // don't look at this!
        placeholders = placeholders.appendAll(Binary.repeat(false, 100));

        return placeholders.zip(sig.parameters)
                .filter(t -> !t._1)
                .map(Tuple2::_2);
    }

    static List<JParameter> placeholdered(List<Boolean> placeholders, JSignature sig) {
        return placeholders.zip(sig.parameters)
                .map(applyTuple((use, param) ->
                        new JParameter(use ? param.type : "Placeholder", (use ? "" : "_") + param.name)
                ));
    }

    static List<JParameter> used(List<Boolean> placeholders, JSignature sig) {
        return placeholders.zip(sig.parameters)
                .filter(t -> t._1)
                .map(Tuple2::_2);
    }

    static String generateParam(JParameter p) {
        return p.getType() + " " + p.getName();
    }

    static String generateReturnType(List<JParameter> parameters, String returnType) {
        return (parameters.size() == 1 && returnType.equals("Boolean"))
                ? "Predicate<" + parameters.get(0).getType() + "> "
                : "Function" + parameters.size() +
                "<" + join(", ", parameters.map(JParameter::getType)) + ", " + returnType + "> ";

    }

    static String generatePartialTypes(List<Boolean> placeholders, JSignature sig) {

        int suffix = countFalse(placeholders) + sig.getArity() - placeholders.size();

        return "public static " + generateGenerics.apply(sig) +
                generateReturnType(unused(placeholders, sig), sig.type) +
                sig.name + "(" + join(", ", map(Ramgen::generateParam, placeholdered(placeholders, sig))) + ") {\n" +
                (all(not(), placeholders)
                        ? "\treturn Ravr::" + sig.name + ";\n"
                        : "\treturn (" + join(", ", map(JParameter::getName, unused(placeholders, sig))) +
                        ") -> " + sig.name + "(" + join(", ", map(JParameter::getName, sig.parameters)) + ");\n") +
                "}\n\n";
    }


    static String toCode(JSignature sig) {

        val strGenerics = t(
                sig
                , JSignature::getGenerics
                , joinOption(", ")
                , option -> option.map((String g) -> " <" + g + ">\n")
                , defaultTo("")
        );

        val strParams = t(
                sig
                , JSignature::getParameters
                , map((JParameter p) -> p.type + " " + p.name)
                , join(", ")
        );

        return "public static" + strGenerics +
                sig.name + "(" + strParams + ") {\n";

    }

    static int intPow(int x, int exponent) {
        return exponent == 0 ? 1 : x * intPow(x, exponent - 1);
    }

    static List<String> partialize(JSignature sig) {

        List<Integer> paramCount = List.rangeClosed(0, sig.getArity());

        List<Tuple2<Integer, List<Integer>>> combinationsInt = paramCount.map(x ->
                Tuple.of(x, List.range(0, intPow(2, x))));

        List<List<Boolean>> binaries = combinationsInt.flatMap(applyTuple((count, combinations) ->
                combinations.isEmpty() ? List.of(List.empty()) :
                        combinations.map(c -> Binary.toLittleEndianBooleans(count, c))
        ));

        List<List<Boolean>> wtf = binaries.init();

        return wtf.map(it -> generatePartialTypes(it, sig)).reverse();

        /*
        val generateCode = pipe(
                (Integer x) -> Binary.toLittleEndianBooleans(sig.getArity(), x),
                y -> generatePartialTypes(y, sig)
        );

        return join("\n\n", List.range(0, sig.getArity() + 1)
                .flatMap(pcount -> List.range(0, intPow(2, pcount) - 1))
                .map(generateCode)
        );
        */

    }

    /*
       public static <A, B, C, D, E, F, G, H>
    Function1<A, H> pipe(
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef,
            Function1<F, G> fg,
            Function1<G, H> gh
    ) {
        return a -> gh.apply(fg.apply(ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a)))))));
    }
     */


    // PIPE ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static List<String> pipeTypes = List.of("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));


    static List<String> pipeParameterNames = pipeTypes.zipWith(pipeTypes.tail(), (a, b) -> a.toLowerCase() + b.toLowerCase());
    static List<String> pipeParameterTypes = pipeTypes.zipWith(pipeTypes.tail(), (a, b) -> "Function1<" + a + ", " + b + ">");
    static List<JParameter> pipeParameters = pipeParameterTypes.zipWith(pipeParameterNames, (t, n) -> new JParameter(t, n));

    static String generatePipeGenerics(int size) {
        return "<" + join(", ", pipeTypes.take(size + 1)) + ">";
    }


    static List<String> generatePipeParametersRaw(int size) {
        return pipeParameters.take(size).map(p -> p.getType() + " " + p.getName());
    }

    static String generatePipeParameters(int size) {
        return join(",\n", generatePipeParametersRaw(size));
    }

    static String generateComposeParameters(int size) {
        return join(",\n", generatePipeParametersRaw(size).reverse());
    }

    static String generatePipeCore(int size) {
        return Match(size).of(
                Case($(0), "a"),
                Case($(), x -> pipeParameterNames.get(x - 1) + ".apply(" + generatePipeCore(x - 1) + ")")
        );
    }

    static String generateHepgargarImplementation(int size) {
        return "return " + generatePipeCore(size) + ";";
    }

    static String generatePipeImplementation(int size) {
        return "return a -> " + generatePipeCore(size) + ";";
    }

    static String generatePipe(int size) {
        return "public static " + generatePipeGenerics(size) + "\n" +
                "Function1<A, " + pipeTypes.get(size) + "> pipe(\n" +
                generatePipeParameters(size) + "\n" +
                ") {\n" +
                generatePipeImplementation(size) + "\n" +
                "}\n";

    }

    static String generateHepgargar(int size) {
        return "public static " + generatePipeGenerics(size) + "\n" +
                pipeTypes.get(size) + " t(\n" +
                "A a,\n" +
                generatePipeParameters(size) + "\n" +
                ") {\n" +
                generateHepgargarImplementation(size) + "\n" +
                "}\n";

    }


    static String generateCompose(int size) {
        return "public static " + generatePipeGenerics(size) + "\n" +
                "Function1<A, " + pipeTypes.get(size) + "> compose(\n" +
                generateComposeParameters(size) + "\n" +
                ") {\n" +
                generatePipeImplementation(size) + "\n" +
                "}\n";

    }

    static List<String> generatePipes(int maxSize) {
        return List.rangeClosed(0, maxSize).map(Ramgen::generatePipe);
    }

    static List<String> generateHepgargars(int maxSize) {
        return List.rangeClosed(0, maxSize).map(Ramgen::generateHepgargar);
    }

    static List<String> generateComposes(int maxSize) {
        return List.rangeClosed(0, maxSize).map(Ramgen::generateCompose);
    }

    static <A>
    Function1<List<A>, List<A>> concat(List<A> one) {
        return two -> one.appendAll(two);
    }

    public static void main(String[] args) {

        JsonData data = ReadJson.getData();

        List<JsonRamdaDoc> ramdaDoc = ReadJson.getRamdaDoc();

        List<String> allNames = pipe(
                JsonData::getFunctions,
                map(pipe(
                        JsonFunction::getSignature,
                        JsonSignature::getName
                )),
                concat(ramdaDoc.map(JsonRamdaDoc::getName)),
                List::toSet,
                s -> s.removeAll(data.hide),
                Set::toList,
                l -> l.sortBy(toUpper())
        ).apply(data);

        Map<String, JsonFunction> functionMap = data.getFunctions().toMap(fs -> fs.getSignature().getName(), x -> x);

        /*
        println(pipe(
                (List<String> l) -> l.sortBy(toUpper()),
                join("\n")
        ).apply(allNames));
        */

        Map<String, String> statusIdIcon = data.status.toMap(JsonStatus::getId, JsonStatus::getIcon);

        String markdownTable = "| Status | Function | Note |\n" +
                "|:----:|:--------|:-----|\n" +
                t(
                        allNames,
                        map(ifElse(functionMap::containsKey,
                                pipe(functionMap::get, Option::get, function -> {

                                    val icon = statusIdIcon.get(function.getStatus()).get();
                                    val name = function.signature.name;
                                    val comment = nullTo("", function.comment);

                                    return "| " + icon + " | " + name + " | " + comment + "|";
                                }),
                                missingName -> "| " + statusIdIcon.get(
                                        data.blacklist.contains(missingName) ? "rejected" : "missing"
                                ).get() + " | " + missingName + " |   |"
                        )),
                        join("\n")
                );


        List<String> generatedFunctions = data
                .functions
                .map(JsonFunction::getSignature)
                .map(JSignature::fromJsonSignature)
                .flatMap(Ramgen::partialize); // x -> generatePartialTypes(L(false, true), x))

        println("Generated " + generatedFunctions.size() + " functions.");
        println();
        println(markdownTable);
        //generatedFunctions.forEach(System.out::println);

//        println(join("\n\n", generateHepgargars(10)));
    }
}
