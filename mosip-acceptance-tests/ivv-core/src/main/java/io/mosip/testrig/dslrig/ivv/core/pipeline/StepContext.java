package io.mosip.testrig.dslrig.ivv.core.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import lombok.Getter;

/**
 * Bound view of a DSL step after {@link StepBinder#bind(Scenario.Step)} — scenario variables
 * resolved, outputs collected for {@link PipelineStep#publishOutputs()}.
 */
@Getter
public class StepContext {
    private final Scenario.Step step;
    private final List<String> boundParameters;
    private final String outVarName;
    private final Map<String, String> outputs = new HashMap<>();

    public StepContext(Scenario.Step step, List<String> boundParameters) {
        this.step = step;
        this.boundParameters = Collections.unmodifiableList(new ArrayList<>(boundParameters));
        this.outVarName = step.getOutVarName();
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
        if (index < 0 || index >= boundParameters.size()) {
            return null;
        }
        return boundParameters.get(index);
    }
}
