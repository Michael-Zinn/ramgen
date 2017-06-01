package de.michaelzinn.ramgen.java;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by michael on 26.05.17.
 */
@Getter
@AllArgsConstructor
public class JParameter {
    String type, name;

    @Override
    public String toString() {
        return type + ", " + name;
    }
}
