package de.michaelzinn.ramgen.json;

import io.vavr.collection.List;
import lombok.Getter;

/**
 * Created by michael on 27.05.17.
 */
@Getter
public class JsonData {
    public String title;
    public String author;

    public List<JsonStatus> status;

    public List<String> hide;
    public List<String> blacklist;

    public List<JsonFunction> functions;
}
