package io.mosip.testrig.dslrig.ivv.core.dsl;

/**
 * Variable layers consulted in order: {@link #STEP} → {@link #SCENARIO} → {@link #GLOBAL}.
 */
public enum VariableScope {
    GLOBAL,
    SCENARIO,
    STEP
}
