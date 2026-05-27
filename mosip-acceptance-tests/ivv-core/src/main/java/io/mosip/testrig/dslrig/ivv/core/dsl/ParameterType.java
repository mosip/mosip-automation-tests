package io.mosip.testrig.dslrig.ivv.core.dsl;

/**
 * Optional coercion applied after reference resolution.
 */
public enum ParameterType {
    STRING,
    INT,
    LONG,
    BOOLEAN;

    public Object coerce(String value, String label) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        switch (this) {
            case STRING:
                return trimmed;
            case INT:
                try {
                    return Integer.parseInt(trimmed);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Parameter '" + label + "' is not a valid int: [" + value + "]");
                }
            case LONG:
                try {
                    return Long.parseLong(trimmed);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Parameter '" + label + "' is not a valid long: [" + value + "]");
                }
            case BOOLEAN:
                if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                    return Boolean.parseBoolean(trimmed);
                }
                throw new IllegalArgumentException("Parameter '" + label + "' is not a valid boolean: [" + value + "]");
            default:
                return trimmed;
        }
    }

    public String coerceToString(String value, String label) throws IllegalArgumentException {
        Object coerced = coerce(value, label);
        return coerced == null ? null : coerced.toString();
    }
}
