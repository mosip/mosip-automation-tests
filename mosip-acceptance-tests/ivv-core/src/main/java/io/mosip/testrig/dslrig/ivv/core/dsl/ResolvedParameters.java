package io.mosip.testrig.dslrig.ivv.core.dsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import lombok.Getter;

/**
 * Immutable list of parameter values after {@link DslRuntime#resolve(List)}.
 */
@Getter
public final class ResolvedParameters {

    private final List<String> values;

    ResolvedParameters(List<String> values) {
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    public List<String> asList() {
        return values;
    }

    public int size() {
        return values.size();
    }

    public String get(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    public String require(int index) throws RigInternalError {
        String v = get(index);
        if (v == null || v.isBlank()) {
            throw new RigInternalError("Required parameter at index " + index + " is missing or empty");
        }
        return v;
    }

    public int getInt(int index) throws RigInternalError {
        try {
            return Integer.parseInt(require(index));
        } catch (NumberFormatException e) {
            throw new RigInternalError("Parameter at index " + index + " is not a valid int: [" + get(index) + "]");
        }
    }

    public boolean getBoolean(int index) throws RigInternalError {
        String v = require(index);
        if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
            return Boolean.parseBoolean(v);
        }
        throw new RigInternalError("Parameter at index " + index + " is not a valid boolean: [" + v + "]");
    }
}
