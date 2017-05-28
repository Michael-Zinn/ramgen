package de.michaelzinn.ramgen.json;

import lombok.Data;

/**
 * Created by michael on 27.05.17.
 */
@Data
public class JsonFunction {
    public JsonSignature signature;
    public String generate; // "all", "nothing", "unique"
    public String status;
    public String comment;
}
