package de.michaelzinn.ramgen.json;

import io.vavr.collection.List;
import lombok.Data;

/**
 * Created by michael on 27.05.17.
 */
@Data
public class JsonSignature {
    public List<String> generics;
    public String type;
    public String name;
    public List<String> parameters;
}
