package de.michaelzinn.ramgen.json;

import lombok.Data;

/**
 * Created by michael on 27.05.17.
 */
@Data
public class JsonFunction {
    public String generate; // "all", "none", "unique"
    public JsonSignature signature;
    public String status;
    public String comment;
}
