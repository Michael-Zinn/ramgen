package de.michaelzinn.ramgen;

import de.michaelzinn.ramgen.java.JFunction;
import de.michaelzinn.ramgen.java.JParameter;
import de.michaelzinn.ramgen.java.JSignature;
import de.michaelzinn.ramgen.json.*;
import de.michaelzinn.ramgen.macro.Macro;
import io.vavr.Function4;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.val;

import java.util.function.Function;

import static de.michaelzinn.ravr.Placeholder.__;
import static de.michaelzinn.ravr.Ravr.*;
import static io.vavr.API.List;
import static io.vavr.API.println;


/**
 * Some Java code that uses Ravr to generate some Java code that's used by Ravr.
 * <p>
 * Created by michael on 26.05.17.
 */
public class Ramgen {

    static List<String> academicGenerics = List.of("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));

    static List<String> enterpriseGenerics = List.of("TUVWXYZABCDEFGHIJKLMNOPQRS".split(""));

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
                                , (Boolean keep, JParameter param) ->
                                        (keep ? param.getType() : "Placeholder") + " " + param.getName()
                        )
                , join(", ")
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

    static String partialize(JFunction jFunction) {
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


        return join("\n\n",
                wtf2.map(it -> generatePartialTypes(it, sig)).reverse()
        );


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

    abstract class ParameterNameGenerator implements Function4<Integer, Integer, String, String, String> {

        abstract public String generateParam(Integer paramCount, Integer paramIndex, String paramType, String paramName);

        @Override
        public String apply(Integer integer, Integer integer2, String s, String s2) {
            return generateParam(integer, integer2, s, s2);
        }

    }


    // TODO to ravr
    static <T, C extends Comparable<C>>
    Function<List<T>, List<T>> sortBy(Function<T, C> by) {
        return list -> sortBy(by, list);
    }

    // TODO ravr
    static <T>
    Function<List<T>, List<T>> uniq() {
        // TODO this destroys the order?
        return list -> list.toSet().toList();
    }

    static Macro DO_WITH = Macro.of(
            academicGenerics,
            (macro, i, max) -> macro.getGenericNames().get(i),
            "doWith",
            List("A value"),
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return "Function<? super " + in + ", " + out + "> f_" + in + "_" + out;
            },
            (macro, i, max) -> List(),
            (macro, i, max) -> Tuple.of("\t\treturn ", ";\n"),
            "value",
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return Tuple.of("f_" + in + "_" + out + ".apply(", ")");
            }
    );

    static Macro _predicatePipe = Macro.of(
            academicGenerics,
            (macro, i, max) -> "Predicate<A>",
            "pipe_Predicate",
            List(),
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return "Function<? super " + in + ", " + out + "> f_" + in + "_" + out;
            },
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                return List("Predicate<" + in + "> predicate");
            },
            (macro, i, max) -> Tuple.of("\t\treturn value -> predicate.test(", ");\n"),
            "value",
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return Tuple.of("f_" + in + "_" + out + ".apply(", ")");
            }
    );

    static Macro _pipe = Macro.of(
            academicGenerics,
            (macro, i, max) -> "Function<A, " + macro.getGenericNames().get(i) + ">",
            "pipe",
            List(),
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return "Function<? super " + in + ", " + out + "> f_" + in + "_" + out;
            },
            (macro, i, max) -> List(),
            (macro, i, max) -> Tuple.of("\t\treturn value -> ", ";\n"),
            "value",
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return Tuple.of("f_" + in + "_" + out + ".apply(", ")");
            }
    );

    static Macro COMPOSE = Macro.of(
            academicGenerics,
            (macro, i, max) -> "Function<A, " + macro.getGenericNames().get(i) + ">",
            "compose",
            List(),
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(max - i - 1);
                String out = macro.getGenericNames().get(max - i);
                return "Function<? super " + in + ", " + out + "> f_" + in + "_" + out;
            },
            (macro, i, max) -> List(),
            (macro, i, max) -> Tuple.of("\t\treturn value -> ", ";\n"),
            "value",
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return Tuple.of("f_" + in + "_" + out + ".apply(", ")");
            }
    );

    static Macro _pipeK(String m) {
        return Macro.of(
                academicGenerics,
                (macro, i, max) -> "Function<A, " + m + "<" + macro.getGenericNames().get(i) + ">>",
                "pipeK_" + m,
                List(),
                (macro, i, max) -> {
                    String in = macro.getGenericNames().get(i);
                    String out = macro.getGenericNames().get(i + 1);
                    return "Function<? super " + in + ", " + m + "<" + out + ">> f_" + in + "_" + out;
                },
                (macro, i, max) -> List(),
                (macro, i, max) -> Tuple.of("\t\treturn value -> ", ";\n"),
                "value",
                (macro, i, max) -> {
                    String in = macro.getGenericNames().get(i);
                    String out = macro.getGenericNames().get(i + 1);

                    String f = "f_" + in + "_" + out;

                    return i == 0 ?
                            Tuple.of(f + ".apply(", ")") :
                            Tuple.of("", ".flatMap(" + f + ")");
                }
        );
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

        List<JFunction> functions = data.getFunctions().map(JFunction::of);

        Map<String, JFunction> javaFunctionMap = functions.toMap(jsonFunction -> Tuple.of(jsonFunction.getSignature().getName(), jsonFunction));


        String generatedFunctions = doWith(data,
                JsonData::getFunctions,
                map(pipe(
                        tap(f -> println(f.getSignature().getName())),
                        JFunction::of
                )),
                map(Ramgen::partialize),
                join("\n\n")
        );


        // macros
        String macros = doWith(List.of(
                COMPOSE,
                _pipe,
                _predicatePipe,
                DO_WITH
                ),
                concat(__, map(Ramgen::_pipeK, List.of(
                        "List",
                        "Option"
                ))),
                map(macro -> macro.expand(10)),
                join("\n\n\n\n")
        );

        String generatedCode = join("\n\n\n", List(
                separator("PARTIAL APPLICATIONS"),
                generatedFunctions,
                separator("TYPE ALIGNED SEQUENCE FUNCTIONS"),
                macros
        ));

        //println(generatedCode);
        println(markdownTable);

        //println(partialize(javaFunctionMap.get("concatOptions").get()));


        //.flatMap(Ramgen::partialize); // x -> generatePartialTypes(L(false, true), x))

        /*
        p;rintln("Generated " + generatedFunctions.size() + " functions.");
        println();
        println(markdownTable);
        //*/


        /*
        println(separator("COMPOSE"));

        println(join("\n", generateComposes(10)));
        println();
        println();
        println(separator("_pipe"));
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
        //*/

    }
}
