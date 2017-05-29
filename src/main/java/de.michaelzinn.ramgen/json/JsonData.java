package de.michaelzinn.ramgen.json;

import de.michaelzinn.ravr.Lens;
import io.vavr.collection.List;
import lombok.Getter;

import static de.michaelzinn.ravr.Ravr.lens;

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
