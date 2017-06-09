package de.michaelzinn.ramgen.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.collection.List;
import io.vavr.jackson.datatype.VavrModule;
import lombok.SneakyThrows;

import java.io.File;

/**
 * Created by michael on 27.05.17.
 */
public class ReadJson {


    static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new VavrModule());
        return mapper;
    }

    /*
    private static String path = "/Users/michaelzinn/projects/ramgen/";
    /*/
    private static String path = "/home/michael/IdeaProjects/ramgen/";
    //*/

    @SneakyThrows
    static <T> T slurp(String file, Class<T> clazz) {
        return mapper().readValue(new File(path + file+".json"), clazz);
    }
    @SneakyThrows
    static <T> T slurp(String file, TypeReference<T> clazz) {
        return mapper().readValue(new File(path + file+".json"), clazz);
    }

    public static JsonData getData() {
        return slurp("data", JsonData.class);
    }

    public static List<JsonRamdaDoc> getRamdaDoc() {
        return slurp("ramdadoc", new TypeReference<List<JsonRamdaDoc>>(){});
    }



}
