package io.mosip.testrig.dslrig.ivv.core.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import lombok.Getter;

/**
 * Entry point for DSL variable resolution before step {@code run()}.
 * <pre>
 *   ResolvedParameters params = dsl.resolve(step.getParameters());
 *   String rid = params.require(0);
 * </pre>
 */
@Getter
public final class DslRuntime {

    private final VariableStore store;
    private final Scenario.Step step;

    public DslRuntime(VariableStore store, Scenario.Step step) {
        this.store = store;
        this.step = step;
    }

    public static DslRuntime forStep(Scenario.Step step, Store suiteStore) {
        VariableStore variableStore = VariableStore.forStep(step, suiteStore);
        variableStore.clearStep();
        return new DslRuntime(variableStore, step);
    }

    /**
     * Resolves every raw parameter ( {@code $$} refs, readable names, globals) in one pass.
     */
    public ResolvedParameters resolve(List<String> rawParameters) throws RigInternalError {
        List<String> bound = new ArrayList<>();
        if (rawParameters != null) {
            for (String param : rawParameters) {
                bound.add(store.resolveReference(param));
            }
        }
        return new ResolvedParameters(bound);
    }

    /**
     * Resolves parameters and applies {@link ParameterSpec defaults}, required checks, and optional types.
     */
    public ResolvedParameters resolve(List<String> rawParameters, ParameterSpec... specs) throws RigInternalError {
        ResolvedParameters base = resolve(rawParameters);
        if (specs == null || specs.length == 0) {
            return base;
        }
        List<ParameterSpec> specList = Arrays.asList(specs);
        int maxIndex = specList.stream().mapToInt(ParameterSpec::getIndex).max().orElse(-1);
        int size = Math.max(base.size(), maxIndex + 1);
        List<String> merged = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            merged.add(null);
        }
        for (int i = 0; i < base.size(); i++) {
            merged.set(i, base.get(i));
        }
        for (ParameterSpec spec : specList) {
            int i = spec.getIndex();
            while (merged.size() <= i) {
                merged.add(null);
            }
            String value = i < base.size() ? base.get(i) : null;
            if (isBlank(value)) {
                if (spec.getDefaultValue() != null) {
                    value = spec.getDefaultValue();
                } else if (spec.isRequired()) {
                    throw new RigInternalError("Required parameter '" + spec.getLabel() + "' at index " + i
                            + " is missing");
                }
            }
            if (value != null && spec.getType() != ParameterType.STRING) {
                try {
                    value = spec.getType().coerceToString(value, spec.getLabel());
                } catch (IllegalArgumentException e) {
                    throw new RigInternalError(e.getMessage());
                }
            }
            merged.set(i, value);
        }
        return new ResolvedParameters(merged);
    }

    public void put(VariableScope scope, String key, String value) {
        store.put(scope, key, value);
    }

    public void publishStepOutputs(java.util.Map<String, String> outputs) {
        if (outputs != null) {
            store.putAll(VariableScope.SCENARIO, outputs);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
