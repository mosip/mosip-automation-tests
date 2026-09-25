package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;

/**
 * Per-scenario continuation for park/resume. Bound to scenario identity (Store + Scenario),
 * not to a worker thread — so resume on another worker does not mix data across scenarios.
 */
public class ScenarioExecutionState implements Delayed {

	private Scenario scenario;
	private Store store;
	private Properties properties;
	private ExtentTest extentTest;
	private ITestResult testResult;
	private int stepIndex;
	private int jumpBackIndex;
	private int iterationCount;
	private boolean willRetry;
	private boolean afterSuiteClearCacheOnly;
	private long wakeAtMs;
	private CompletableFuture<Void> completion;
	private String lastIdentifier;

	public Scenario getScenario() {
		return scenario;
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public ExtentTest getExtentTest() {
		return extentTest;
	}

	public void setExtentTest(ExtentTest extentTest) {
		this.extentTest = extentTest;
	}

	public ITestResult getTestResult() {
		return testResult;
	}

	public void setTestResult(ITestResult testResult) {
		this.testResult = testResult;
	}

	public int getStepIndex() {
		return stepIndex;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}

	public int getJumpBackIndex() {
		return jumpBackIndex;
	}

	public void setJumpBackIndex(int jumpBackIndex) {
		this.jumpBackIndex = jumpBackIndex;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public void setIterationCount(int iterationCount) {
		this.iterationCount = iterationCount;
	}

	public boolean isWillRetry() {
		return willRetry;
	}

	public void setWillRetry(boolean willRetry) {
		this.willRetry = willRetry;
	}

	public boolean isAfterSuiteClearCacheOnly() {
		return afterSuiteClearCacheOnly;
	}

	public void setAfterSuiteClearCacheOnly(boolean afterSuiteClearCacheOnly) {
		this.afterSuiteClearCacheOnly = afterSuiteClearCacheOnly;
	}

	public long getWakeAtMs() {
		return wakeAtMs;
	}

	public void setWakeAtMs(long wakeAtMs) {
		this.wakeAtMs = wakeAtMs;
	}

	public CompletableFuture<Void> getCompletion() {
		return completion;
	}

	public void setCompletion(CompletableFuture<Void> completion) {
		this.completion = completion;
	}

	public String getLastIdentifier() {
		return lastIdentifier;
	}

	public void setLastIdentifier(String lastIdentifier) {
		this.lastIdentifier = lastIdentifier;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(wakeAtMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed other) {
		return Long.compare(getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
	}
}
