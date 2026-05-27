package io.mosip.testrig.dslrig.ivv.core.pipeline;

import io.mosip.testrig.dslrig.ivv.core.dsl.DslRuntime;
import io.mosip.testrig.dslrig.ivv.core.dsl.ResolvedParameters;
import io.mosip.testrig.dslrig.ivv.core.dsl.VariableStore;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * Bind phase: resolves step parameters via {@link DslRuntime} before prepare/execute.
 */
public final class StepBinder {

    private StepBinder() {
    }

    public static StepContext bind(Scenario.Step step) throws RigInternalError {
        return bind(step, null);
    }

    public static StepContext bind(Scenario.Step step, Store suiteStore) throws RigInternalError {
        DslRuntime dsl = DslRuntime.forStep(step, suiteStore);
        ResolvedParameters resolved = dsl.resolve(step.getParameters());
        return new StepContext(step, dsl, resolved);
    }

    public static boolean referencesScenarioVariable(Scenario.Step step, String value) {
        if (step == null) {
            return false;
        }
        return VariableStore.forStep(step, null).referencesVariable(value);
    }

    public static String resolveScenarioVariable(Scenario.Step step, String value) throws RigInternalError {
        if (step == null) {
            return value;
        }
        return VariableStore.forStep(step, null).resolveReference(value);
    }

    /** @deprecated Use {@link VariableStore#toDslVariableName(String)}. */
    public static String toDslVariableName(String displayName) {
        return VariableStore.toDslVariableName(displayName);
    }
}
