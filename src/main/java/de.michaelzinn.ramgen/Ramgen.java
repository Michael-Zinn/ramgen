package de.michaelzinn.ramgen;

import de.michaelzinn.ramgen.java.JFunction;
import de.michaelzinn.ramgen.java.JParameter;
import de.michaelzinn.ramgen.java.JSignature;
import de.michaelzinn.ramgen.json.*;
import io.vavr.API;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import lombok.val;

import java.util.function.Function;
import java.util.function.Predicate;

import static de.michaelzinn.ravr.Placeholder.__;
import static de.michaelzinn.ravr.Ravr.*;
import static io.vavr.API.*;


/**
 * Some Java code that uses Ravr to generate some Java code that's used by Ravr.
 * <p>
 * Created by michael on 26.05.17.
 */
public class Ramgen {

    static Function<JSignature, String> generateGenerics = pipe(
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
        return doWith(placeholders
                .zipWith(sig.getParameters()
                ,(Boolean keep, JParameter param) ->
                        (keep ? param.getType() : "Placeholder") + " " + param.getName()
                )
                ,join(", ")
        );
    }

    static List<JParameter> unused(List<Boolean> placeholders, JSignature sig) {
        // don't look at this!
        placeholders = placeholders.appendAll(Binary.repeat(false, 100));

        return placeholders.zip(sig.getParameters())
                .filter(t -> !t._1)
                .map(Tuple2::_2);
    }

    static List<JParameter> placeholdered(List<Boolean> placeholders, JSignature sig) {
        return placeholders.zip(sig.getParameters())
                .map(applyTuple((use, param) ->
                        new JParameter(use ? param.getType() : "Placeholder", (use ? "" : "_") + param.getName())
                ));
    }

    static List<JParameter> used(List<Boolean> placeholders, JSignature sig) {
        return placeholders.zip(sig.getParameters())
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

        int suffix = count(not(), placeholders) + sig.getArity() - placeholders.size();

        return "public static " + generateGenerics.apply(sig) +
                generateReturnType(unused(placeholders, sig), sig.getType()) +
                sig.getName() + "(" + join(", ", map(Ramgen::generateParam, placeholdered(placeholders, sig))) + ") {\n" +
                (all(not(), placeholders)
                        ? "\treturn Ravr::" + sig.getName() + ";\n"
                        : "\treturn (" + join(", ", map(JParameter::getName, unused(placeholders, sig))) +
                        ") -> " + sig.getName() + "(" + join(", ", map(JParameter::getName, sig.getParameters())) + ");\n") +
                "}";
    }


    static String toCode(JSignature sig) {

        val strGenerics = doWith(sig,
                JSignature::getGenerics,
                joinOption(", "),
                option -> option.map((String g) -> " <" + g + ">\n"),
                defaultTo("")
        );

        val strParams = doWith(sig,
                JSignature::getParameters,
                map((JParameter p) -> p.getType() + " " + p.getName()),
                join(", ")
        );

        return "public static" + strGenerics +
                sig.getName() + "(" + strParams + ") {\n";

    }

    static int intPow(int x, int exponent) {
        return exponent == 0 ? 1 : x * intPow(x, exponent - 1);
    }

    static List<String> partialize(JFunction jFunction) {
        JSignature sig = jFunction.getSignature();

        List<Integer> paramCount = List.rangeClosed(0, sig.getArity());

        List<Tuple2<Integer, List<Integer>>> combinationsInt = paramCount.map(x ->
                Tuple.of(x, List.range(0, intPow(2, x))));

        List<List<Boolean>> binaries = combinationsInt.flatMap(applyTuple((count, combinations) ->
                combinations.isEmpty() ? List.of(List.empty()) :
                        combinations.map(c -> Binary.toLittleEndianBooleans(count, c))
        ));

        List<List<Boolean>> wtf = binaries.init();

        List<List<Boolean>> wtf2 = null;
        switch (jFunction.getGenerate()) {
            case ALL:
                wtf2 = wtf;
                break;
            case NONE:
                wtf2 = List.empty();
                break;
            case UNIQUE:
                wtf2 = wtf.filter(any(x -> x));
                break;
            default:
                wtf2 = wtf; // implies generate at the moment.
        }


        return wtf2.map(it -> generatePartialTypes(it, sig)).reverse();


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
    static List<String> pipeParameterTypes = pipeTypes.zipWith(pipeTypes.tail(), (a, b) -> "Function<" + a + ", " + b + ">");
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
                "Function<A, " + pipeTypes.get(size) + "> pipe(\n" +
                generatePipeParameters(size) + "\n" +
                ") {\n" +
                generatePipeImplementation(size) + "\n" +
                "}\n";

    }

    static String generateHepgargar(int size) {
        return "public static " + generatePipeGenerics(size) + "\n" +
                pipeTypes.get(size) + " doWith(\n" +
                "A a,\n" +
                generatePipeParameters(size) + "\n" +
                ") {\n" +
                generateHepgargarImplementation(size) + "\n" +
                "}\n";

    }


    static String generateCompose(int size) {
        return "public static " + generatePipeGenerics(size) + "\n" +
                "Function<A, " + pipeTypes.get(size) + "> compose(\n" +
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

    static String ruby(String string, int multiplier) {
        return doWith(List.range(0, multiplier),
                map(always(string)),
                join("")
        );
    }

    static String rpad(String string, String padding, int size) {
        return string + ruby(padding, size - string.length());
    }

    static String separator(String text) {
        return rpad("// " + text + " ", "/", 120);
    }

    // TODO to ravr
    static <T, C extends Comparable<C>> List<T> sortBy(Function<T, C> by, List<T> list) {
        return list.sortBy(by);//a -> by.apply(a));
    }

    // TODO to ravr
    static <T, C extends Comparable<C>>
    Function<List<T>, List<T>> sortBy(Function<T, C> by) {
        return list -> sortBy(by, list);
    }

    // TODO ravr
    static <T>
    Function<List<T>, List<T>> without(List<T> remove) {
        return list -> list.removeAll(remove);
    }

    // TODO ravr
    static <T>
    Function<List<T>, List<T>> uniq() {
        // TODO this destroys the order?
        return list -> list.toSet().toList();
    }

    public static void main(String[] args) {

        JsonData data = ReadJson.getData();

        List<JsonRamdaDoc> ramdaDoc = ReadJson.getRamdaDoc();

        List<String> ramdaNames = ramdaDoc.map(JsonRamdaDoc::getName);

        List<String> allNames = doWith(data,
                JsonData::getFunctions,
                map(pipe(
                        JsonFunction::getSignature,
                        JsonSignature::getName
                )),
                concat(__, ramdaNames),
                uniq(),
                without(data.getHide()),
                sortBy(toUpper())
        );

        Map<String, JsonFunction> functionMap = data.getFunctions().toMap(fs -> fs.getSignature().getName(), x -> x);

        String allNamesString = pipe(
                (List<String> l) -> l.sortBy(toUpper()),
                join("\n")
        ).apply(allNames);

        Map<String, String> statusIdIcon = data.status.toMap(JsonStatus::getId, JsonStatus::getIcon);

        String markdownTable = "| Status | Function | Note |\n" +
                "|:----:|:--------|:-----|\n" +
                doWith(allNames,
                        map(
                                ifElse(functionMap::containsKey,
                                        pipe(functionMap::get, Option::get, function -> {

                                            val icon = statusIdIcon.get(function.getStatus()).get();
                                            val name = function.signature.getName();
                                            val comment = nullTo("", function.comment);

                                            return "| " + icon + " | " + name + " | " + comment + "|";
                                        }),
                                        missingName -> "| " + statusIdIcon.get(
                                                data.blacklist.contains(missingName) ? "rejected" : "missing"
                                        ).get() + " | " + missingName + " |   |"
                                )
                        ),
                        join("\n")
                );


        List<String> generatedFunctions = doWith(data,
                JsonData::getFunctions,
                map(pipe(
                        tap(f -> println(f.getSignature().getName())),
                        JFunction::of
                )),
                flatMap(Ramgen::partialize)
        );


        //.flatMap(Ramgen::partialize); // x -> generatePartialTypes(L(false, true), x))

        /*
        p;rintln("Generated " + generatedFunctions.size() + " functions.");
        println();
        println(markdownTable);
        //*/


        println();
        println(separator("COMPOSE"));

        println(join("\n", generateComposes(10)));
        println();
        println();
        println(separator("PIPE"));
        println();
        println(join("\n", generatePipes(10)));
        println();
        println();
        println(separator("DO_WITH"));
        println();
        println(join("\n\n", generateHepgargars(10)));
        println();
        println();
        println(separator("PARTIAL APPLICATIONS"));
        println();

        println(join("\n\n", generatedFunctions));

    }
}
