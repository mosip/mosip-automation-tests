package io.mosip.testrig.dslrig.ivv.core.dsl;

import lombok.Getter;

/**
 * Declarative binding for one positional step parameter (defaults, required flag, optional type).
 */
@Getter
public final class ParameterSpec {

    private final int index;
    private final String label;
    private final ParameterType type;
    private final String defaultValue;
    private final boolean required;

    private ParameterSpec(int index, String label, ParameterType type, String defaultValue, boolean required) {
        this.index = index;
        this.label = label != null && !label.isBlank() ? label : "param[" + index + "]";
        this.type = type != null ? type : ParameterType.STRING;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public static ParameterSpec required(int index) {
        return new ParameterSpec(index, null, ParameterType.STRING, null, true);
    }

    public static ParameterSpec required(int index, String label) {
        return new ParameterSpec(index, label, ParameterType.STRING, null, true);
    }

    public static ParameterSpec optional(int index, String defaultValue) {
        return new ParameterSpec(index, null, ParameterType.STRING, defaultValue, false);
    }

    public static ParameterSpec typed(int index, ParameterType type, boolean required) {
        return new ParameterSpec(index, null, type, null, required);
    }

    public static ParameterSpec typed(int index, String label, ParameterType type, boolean required) {
        return new ParameterSpec(index, label, type, null, required);
    }

    public static ParameterSpec typed(int index, String label, ParameterType type, String defaultValue) {
        return new ParameterSpec(index, label, type, defaultValue, defaultValue == null);
    }
}
