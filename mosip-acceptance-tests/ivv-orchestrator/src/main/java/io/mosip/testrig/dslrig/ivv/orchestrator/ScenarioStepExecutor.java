package io.mosip.testrig.dslrig.ivv.orchestrator;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import com.aventstack.extentreports.ExtentTest;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.e2e.methods.ClearRunCache;

/**
 * Runs scenario steps from {@link ScenarioExecutionState#getStepIndex()} until completion
 * or a park yield ({@link ScenarioYieldException}).
 */
public final class ScenarioStepExecutor {

	private static final Logger logger = Logger.getLogger(ScenarioStepExecutor.class);

	public enum Outcome {
		DONE, YIELD
	}

	@FunctionalInterface
	public interface StepFactory {
		StepInterface create(Scenario.Step step) throws Exception;
	}

	private ScenarioStepExecutor() {
	}

	/**
	 * Continues from {@code state.stepIndex}. On yield, advances stepIndex past the Wait step
	 * and sets {@code wakeAtMs}. Store / loop indexes stay on the same state object.
	 */
	public static Outcome runUntilYieldOrDone(ScenarioExecutionState state, StepFactory stepFactory)
			throws Exception {
		Scenario scenario = state.getScenario();
		ExtentTest extentTest = state.getExtentTest();

		for (int stepIndex = state.getStepIndex(); stepIndex < scenario.getSteps().size(); stepIndex++) {
			Scenario.Step step = scenario.getSteps().get(stepIndex);

			String identifier = "> #[Test Step: " + step.getName() + "] [Test Parameters: " + step.getParameters()
					+ "]  [Test outVarName: " + step.getOutVarName() + "] [module: " + step.getModule()
					+ "] [variant: " + step.getVariant() + "]";
			state.setLastIdentifier(identifier);
			logger.info(identifier);

			extentTest.info(identifier + " - running");
			extentTest.info("parameters: " + step.getParameters().toString());
			StepInterface st = stepFactory.create(step);
			st.setExtentInstance(extentTest);
			st.setSystemProperties(state.getProperties());
			st.setState(state.getStore());
			st.setStep(step);

			if (state.isAfterSuiteClearCacheOnly() && !(st instanceof ClearRunCache)) {
				String skipStepMsg = identifier + " - skipped (enableDebug=yes, running only clear run cache)";
				logger.info(skipStepMsg);
				extentTest.skip(skipStepMsg);
				state.setStepIndex(stepIndex + 1);
				continue;
			}

			String stepAction = "e2e_" + step.getName() + step.getParameters();
			stepAction = Orchestrator.trimSpaceWithinSquareBrackets(stepAction);

			if (step.getOutVarName() != null) {
				stepAction = step.getOutVarName() + "=" + stepAction;
			}

			String[] stepParams = Orchestrator.getStepDetails("S_" + step.getScenario().getId() + stepAction);
			if (stepParams == null && step.getScenario().getId().contains("_")) {
				String baseScenarioId = step.getScenario().getId().split("_")[0];
				stepParams = Orchestrator.getStepDetails("S_" + baseScenarioId + stepAction);
			}

			if (!step.getName().contains("loopWindow")) {
				StringBuilder sb = new StringBuilder();
				sb.append(
						"<div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray; background-color: lightgray; width: 100%; padding: 0; margin: 0;' name='headers' rows='3' readonly='true'>");
				sb.append("Step Name: " + step.getName() + "\n");
				if (stepParams != null) {
					sb.append("Step Description: " + stepParams[0] + "\n");
					sb.append("Step Parameters: " + stepParams[1]);
				} else {
					sb.append("Step Description: [ERROR: stepParams is null]\n");
					sb.append("Step Parameters: [ERROR: stepParams is null]");
				}
				sb.append("</textarea></div>");
				Reporter.log(sb.toString());
			}

			if (step.getName().contains("loopWindow")) {
				if (step.getParameters().get(0).contains("START")) {
					state.setJumpBackIndex(stepIndex + 1);
					state.setIterationCount(1);
				} else if (step.getParameters().size() > 1 && step.getParameters().get(0).contains("END")) {
					int loopCount = Integer.parseInt(step.getParameters().get(1));
					if (state.getIterationCount() < loopCount) {
						stepIndex = state.getJumpBackIndex() - 1;
						state.setIterationCount(state.getIterationCount() + 1);
						logger.info("Repeating loop, iteration: " + state.getIterationCount() + " of " + loopCount);
						continue;
					} else {
						logger.info("Loop completed after " + state.getIterationCount() + " iterations.");
					}
				}
			}

			try {
				StepRunner.runLifecycle(st);
			} catch (ScenarioYieldException yield) {
				state.setStore(st.getState());
				state.setStepIndex(stepIndex + 1);
				state.setWakeAtMs(System.currentTimeMillis() + Math.max(0L, yield.getWaitTimeMs()));
				String parkMsg = "Parked scenario " + scenario.getId() + " for " + (yield.getWaitTimeMs() / 1000)
						+ " sec after step " + step.getName();
				logger.info(parkMsg);
				extentTest.info(parkMsg);
				Reporter.log(parkMsg);
				extentTest.pass(identifier + " - parked");
				return Outcome.YIELD;
			}

			if (StepRunner.lifecycleFailed(st)) {
				Orchestrator.failStep(extentTest, identifier, state.isWillRetry(), "Step reported error");
			}
			state.setStore(st.getState());
			extentTest.pass(identifier + " - passed");
			state.setStepIndex(stepIndex + 1);
		}

		return Outcome.DONE;
	}
}
