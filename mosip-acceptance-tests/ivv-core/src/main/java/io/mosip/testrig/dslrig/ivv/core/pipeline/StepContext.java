package io.mosip.testrig.dslrig.ivv.core.pipeline;

import java.util.HashMap;
import java.util.Map;

import io.mosip.testrig.dslrig.ivv.core.dsl.DslRuntime;
import io.mosip.testrig.dslrig.ivv.core.dsl.ResolvedParameters;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import lombok.Getter;

/**
 * Bound view of a DSL step after {@link StepBinder#bind(Scenario.Step, io.mosip.testrig.dslrig.ivv.core.dtos.Store)}.
 */
@Getter
public class StepContext {
    private final Scenario.Step step;
    private final DslRuntime dsl;
    private final ResolvedParameters parameters;
    private final String outVarName;
    private final Map<String, String> outputs = new HashMap<>();

    public StepContext(Scenario.Step step, DslRuntime dsl, ResolvedParameters parameters) {
        this.step = step;
        this.dsl = dsl;
        this.parameters = parameters;
        this.outVarName = step != null ? step.getOutVarName() : null;
    }

    public void putOutput(String key, String value) {
        if (key != null && value != null) {
            outputs.put(key, value);
        }
    }

    public void putOutputs(Map<String, String> values) {
        if (values != null) {
            outputs.putAll(values);
        }
    }

    public String boundParameter(int index) {
        return parameters != null ? parameters.get(index) : null;
    }
}
