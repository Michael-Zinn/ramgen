package de.michaelzinn.ramgen;

import de.michaelzinn.ramgen.java.JFunction;
import de.michaelzinn.ramgen.java.JParameter;
import de.michaelzinn.ramgen.java.JSignature;
import de.michaelzinn.ramgen.json.*;
import de.michaelzinn.ramgen.macro.Macro;
import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.val;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.michaelzinn.ravr.Placeholder.__;
import static de.michaelzinn.ravr.Ravr.*;
import static io.vavr.API.*;


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
            mapᐸOptionᐳ(generics -> "<" + generics + ">\n"),
            defaultTo("")
    );


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
                .map(apply((use, param) ->
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


    static int intPow(int x, int exponent) {
        return exponent == 0 ? 1 : x * intPow(x, exponent - 1);
    }

    // TODO get this to work in Ravr
    public static <T1, T2, R>
    R apply(BiFunction<T1, T2, R> f, Tuple2<T1, T2> tuple) {
        return tuple.apply(f);
    }

    // TODO get this to work in Ravr
    public static <T1, T2, R>
    Function<Tuple2<T1, T2>, R> apply(BiFunction<T1, T2, R> f) {
        return tuple -> tuple.apply(f);
    }


    static String partialize(JFunction jFunction) {
        JSignature sig = jFunction.getSignature();

        List<Integer> paramCount = List.rangeClosed(0, sig.getArity());

        List<Tuple2<Integer, List<Integer>>> combinationsInt = paramCount.map(x ->
                Tuple.of(x, List.range(0, intPow(2, x))));

        List<List<Boolean>> binaries = combinationsInt.flatMap(apply((count, combinations) ->
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
        return join("", repeat(multiplier, string));
    }

    static String rpad(String string, String padding, int size) {
        return string + ruby(padding, size - string.length());
    }

    static String separator(String text) {
        return rpad("// " + text + " ", "/", 120);
    }

    abstract class ParameterNameGenerator implements Function4<Integer, Integer, String, String, String> {

        abstract public String generateParam(Integer paramCount, Integer paramIndex, String paramType, String paramName);

        @Override
        public String apply(Integer integer, Integer integer2, String s, String s2) {
            return generateParam(integer, integer2, s, s2);
        }

    }


    /*
    // TODO to ravr
    static <T, C extends Comparable<C>>
    Function<List<T>, List<T>> sortBy(Function<T, C> by) {
        return list -> sortBy(by, list);
    }
    */

    // TODO ravr
    static <T>
    Function<List<T>, List<T>> uniq() {
        // TODO this destroys the order?
        return list -> list.toSet().toList();
    }

    static Function<?, String> toStr() {
        return Object::toString;
    }

    public static <T1, T2, T3, R>
    R apply(Function3<? super T1, ? super T2, ? super T3, ? extends R> f, Tuple3<T1, T2, T3> tuple) {
        return tuple.apply(f);
    }

    /*
    Madness.

    public static <A, B>
    B __(Function<A, B> f, A a) {
        return f.apply(a);
    }

    map( (customer), constraints)

    void bla() {
        val addPlayer = __(adjustCellIfElse, cellContainsGoal, Cell.PLAYER_ON_GOAL, Cell.PLAYER_ON_EMPTY);
    }
    */

    static List<String> listTupleGenerics(int count) {
        val p = rangeC(1, count).map(Object::toString);
        val types = map(concat("T"), p);
        //val commaTypes = join(", ", types);

        return types;//commaTypes;
    }

    static String rawTupleGenerics(int count) {
        return join(", ", listTupleGenerics(count));
    }

    static String completeTupleGenerics(int count) {
        val generics = "<" + rawTupleGenerics(count) + ", R>";
        return generics;
    }

    static String _apply() {
        return doWith(
                rangeC(1, 8),
                map(i -> "public static " + completeTupleGenerics(i) +
                        "\nR apply(Function" + i + completeTupleGenerics(i) + " f, Tuple" + i + "<" + rawTupleGenerics(i) + "> tuple) {\n" +
                        "\treturn tuple.apply(f);\n}"
                ),
                join("\n\n")
        );
    }

    static List<JFunction> _applyCode() {
        return doWith(
                rangeC(1, 8),
                map(i -> new JFunction(
                        JFunction.Generate.UNIQUE,
                        new JSignature(
                                listTupleGenerics(i).append("R"),
                                "R",
                                "apply",
                                List(
                                        new JParameter("Function" + i + "<" + rawTupleGenerics(i) + ", R>", "f"),
                                        new JParameter("Tuple" + i + "<" + rawTupleGenerics(i) + ">", "tuple")
                                )
                        ),
                        JFunction.Status.WORKS,
                        "Works on tuples instead of list."
                ))
        );
    }

    static Macro DO_WITH = Macro.of(
            List(),
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
            List(),
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
            List(),
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
            List(),
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
                List(),
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

    static Macro _orDefault = Macro.of(
            List(
                    "Returns the first parameter that is not null.",
                    "",
                    "The last parameter must not be null.",
                    "",
                    "@param nullables",
                    "@param defaultValue Must not be null.",
                    "@return First parameter that is not null"
            ),
            List("T"),

            (macro, i, max) -> "T",
            "orDefault",
            List(),
            (macro, i, max) -> "@Nullable T nullable" + (i + 1),
            (macro, i, max) -> List("T defaultValue"),

            (macro, i, max) -> Tuple.of("\t\treturn\n", "\t\t\tdefaultValue;\n"),
            "",
            (macro, i, max) -> Tuple.of("", "\t\t\tnullable" + (i + 1) + " != null ? nullable" + (i + 1) + " :\n")
    );

    static List<String> letJavadoc = List(
            "Threads the first parameter through the following function parameters.",
            "",
            "Aborts and returns null when any step in the sequence returns null, otherwise it returns the result of the last function or void if the last parameter is a Consumer.",
            "",
            "@param t Input value, may be null.",
            "@param fs A typed sequence of functions.",
            "@return Result or null if the last parameter is a function, otherwise void"
    );

    static Macro _letFunctions = Macro.of(
            letJavadoc,
            enterpriseGenerics,

            (macro, i, max) -> "@Nullable " + macro.getGenericNames().get(i),
            "let",
            List("@Nullable T t"),
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return "Function<? super " + in + ", @Nullable " + out + "> f_" + in + "_" + out;
            },
            (macro, i, max) -> List(),

            (macro, i, max) -> {
                String in = macro.getGenericNames().get(max - 1);
                String out = macro.getGenericNames().get(max);
                String varName = in.toLowerCase();
                return Tuple.of(
                        "\t\tif(t == null) return null;\n",
                        "\t\treturn f_" + in + "_" + out + ".apply(" + varName + ");\n"
                );
            },
            "",
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                String varName = out.toLowerCase();
                return
                        Tuple.of(
                                "",
                                i == max ?
                                        "" :
                                        "\t\t@Nullable " + out + " " + varName + " = f_" + in + "_" + out + ".apply(" + in.toLowerCase() + ");\n" +
                                                "\t\tif(" + varName + " == null) return null;\n");
            }
    );

    static Macro _letConsumer = Macro.of(
            letJavadoc,
            enterpriseGenerics,

            (macro, i, max) -> "void",
            "let",
            List("@Nullable T t"),
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                return "Function<? super " + in + ", @Nullable " + out + "> f_" + in + "_" + out;
            },
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);

                return List("Consumer<? super " + in + "> c_" + in);
            },

            (macro, i, max) -> {
                String in = macro.getGenericNames().get(max);
                String varName = in.toLowerCase();
                return Tuple.of(
                        "\t\tif(t == null) return;\n",
                        "\t\tc_" + in + ".accept(" + varName + ");\n"
                );
            },
            "",
            (macro, i, max) -> {
                String in = macro.getGenericNames().get(i);
                String out = macro.getGenericNames().get(i + 1);
                String varName = out.toLowerCase();
                return Tuple.of("", "\t\t@Nullable " + out + " " + varName + " = f_" + in + "_" + out + ".apply(" + in.toLowerCase() + ");\n" +
                        "\t\tif(" + varName + " == null) return;\n");
            }
    );

    public static <A>
    List<A> times(Function<Integer, A> fn, Integer n) {
        return range(0, n).map(fn);
    }

    static List<String> toList(String s) {
        return times(n -> s.charAt(n) + "", s.length());
    }

    static String simplify(String s) {
        return doWith(s,
                toLower(),
                Ramgen::toList,
                map(ifElse(contains(__, toList("abcdefghijklmnopqrstuvwxyz0123456789")),
                        identity(),
                        always(" ")
                )),
                join("")
        );
    }

    static java.util.Map<String, Object> defs = new HashMap<>();


    public static <P1, R>
    void defn(String functionName, Function<P1, R> function) {
        defs.put(functionName, function);
    }

    public static <P1, P2, R>
    void defn(String functionName, BiFunction<P1, P2, R> function) {
        defs.put(functionName, function);
    }

    public static <P1, R>
    R c(String functionName, P1 parameter1) {
        return ((Function<P1, R>) defs.get(functionName)).apply(parameter1);
    }

    public static <P1, P2, R>
    R c(String functionName, P1 parameter1, P2 parameter2) {
        return ((BiFunction<P1, P2, R>) defs.get(functionName)).apply(parameter1, parameter2);
    }

    public static Integer add(Object x, Object y) {
        return (Integer) x + (Integer) y;
    }

    public static void main(String[] args) {

        JsonData jsonData = ReadJson.getData();

        List<JsonRamdaDoc> ramdaDoc = ReadJson.getRamdaDoc();

        List<String> ramdaNames = ramdaDoc.map(JsonRamdaDoc::getName);

        List<String> allNames = doWith(jsonData,
                JsonData::getFunctions,
                map(pipe(
                        JsonFunction::getSignature,
                        JsonSignature::getName
                )),
                concat(__, ramdaNames),
                uniq(),
                without(jsonData.getHide()),
                sortBy(Ramgen::simplify)
        );

        Map<String, JsonFunction> functionMap = jsonData.getFunctions().toMap(fs -> fs.getSignature().getName(), x -> x);

        /*
        String allNamesString = pipe(
                (List<String> l) -> l.sortBy(toUpper()),
                join("\n")
        ).apply(allNames);
        */

        Map<String, String> statusIdIcon = jsonData.status.toMap(JsonStatus::getId, JsonStatus::getIcon);

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
                                                jsonData.blacklist.contains(missingName) ? "rejected" : "missing"
                                        ).get() + " | " + missingName + " |   |"

                                )
                        ),
                        join("\n")
                );

        List<JFunction> functions = jsonData
                .getFunctions()
                .map(JFunction::of)
                .appendAll(_applyCode());

        Map<String, JFunction> javaFunctionMap = functions.toMap(jsonFunction -> Tuple.of(jsonFunction.getSignature().getName(), jsonFunction));


        String generatedFunctions = doWith(jsonData,
                JsonData::getFunctions,
                map(JFunction::of),
                concat(_applyCode()),
                sortBy(pipe(JFunction::getSignature, JSignature::getName)),
                map(Ramgen::partialize),
                join("\n\n")
        );


        // macros
        String macros = doWith(
                List(
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
                _apply(),
                generatedFunctions,
                separator("TYPE ALIGNED SEQUENCE FUNCTIONS"),
                macros
        ));


        String null4jmacros = doWith(
                List(
                        _orDefault.expand(9),
                        _letFunctions.expand(9),
                        _letConsumer.expand(8)
                ),
                join("\n\n\n\n")
                //code -> "/*\n" + code + "\n*/"
        );

        println(null4jmacros);

        //*
        //println(generatedCode);
        /*/
        println(markdownTable);
        //*/

        //println(_apply());

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

        // lol
        defn("add", (Integer x, Integer y) ->
                x + y );

        defn("fib", (Integer n) ->
                Match(n).of(
                        Case($(0), 1),
                        Case($(1), 1),
                        Case($(), o ->
                                c("add",
                                        c("fib", subtract(n, 2)),
                                        c("fib", n - 1)))));

        rangeC(1, 10)
                .map(x -> c("fib", x))
                .forEach(System.out::println);

    }
}
