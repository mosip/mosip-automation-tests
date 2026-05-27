package io.mosip.testrig.dslrig.ivv.core.pipeline;

import io.mosip.testrig.dslrig.ivv.core.dsl.VariableScope;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;

public final class StepOutputs {

    private StepOutputs() {
    }

    public static void publish(StepContext context, Scenario.Step step) {
        if (context == null || step == null || step.getScenario() == null) {
            return;
        }
        if (context.getDsl() != null && !context.getOutputs().isEmpty()) {
            context.getDsl().publishStepOutputs(context.getOutputs());
        } else if (!context.getOutputs().isEmpty()) {
            step.getScenario().getVariables().putAll(context.getOutputs());
        }
        String outVar = context.getOutVarName();
        if (outVar != null && context.getOutputs().containsKey(outVar)) {
            if (context.getDsl() != null) {
                context.getDsl().put(VariableScope.SCENARIO, outVar, context.getOutputs().get(outVar));
            } else {
                step.getScenario().getVariables().put(outVar, context.getOutputs().get(outVar));
            }
        }
    }
}
