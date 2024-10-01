package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testng.Reporter;
import org.testng.SkipException;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.GlobalConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class GetPingHealth extends BaseTestCaseUtil implements StepInterface {

	public enum TargetEnvPingHealthState {
		PENDING, RUNNING, COMPLETED
	}

	private static final AtomicReference<TargetEnvPingHealthState> targetEnvPingHealthState = new AtomicReference<>(
			TargetEnvPingHealthState.PENDING);
	private static final Object targetEnvlock = new Object();
	// Counter to track active parallel scenarios running
	// We need to track this to avoid parallel calls
	private static final AtomicInteger targetEnvActiveThreads = new AtomicInteger(0);

	public enum PacketCreatorPingHealthState {
		PENDING, RUNNING, COMPLETED
	}

	private static final AtomicReference<PacketCreatorPingHealthState> packetCreatorPingHealthState = new AtomicReference<>(
			PacketCreatorPingHealthState.PENDING);
	private static final Object packetCreatorlock = new Object();
	private static final AtomicInteger packetCreatorActiveThreads = new AtomicInteger(0);

	private static final Logger logger = Logger.getLogger(GetPingHealth.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String module = getModule();
		try {
			if (module.equalsIgnoreCase("packetcreator")) {
				handlePacketCreatorHealthCheck();
			} else {
				handleTargetEnvHealthCheck();
			}
		} catch (Exception e) {
			this.hasError = true;
			logger.error(e.getMessage());
			resetStateOnError(module);
			throw new RigInternalError("Connection Refused");
		} finally {
			finalizeState(module);
		}
	}

	private String getModule() {
		return (step.getParameters().size() > 0) ? step.getParameters().get(0) : "";
	}

	private void handlePacketCreatorHealthCheck() throws Exception {
		packetCreatorActiveThreads.incrementAndGet();

		synchronized (packetCreatorlock) {
			while (packetCreatorPingHealthState.get() == PacketCreatorPingHealthState.RUNNING) {
				packetCreatorlock.wait();
			}

			if (packetCreatorPingHealthState.get() == PacketCreatorPingHealthState.COMPLETED) {
				Reporter.log("Packet Creator health already checked by another thread.");
				packetCreatorActiveThreads.decrementAndGet();
				return;
			}

			if (packetCreatorPingHealthState.compareAndSet(PacketCreatorPingHealthState.PENDING,
					PacketCreatorPingHealthState.RUNNING)) {
				try {
					executePacketCreatorPingHealthCheck();
					packetCreatorPingHealthState.set(PacketCreatorPingHealthState.COMPLETED);
				} finally {
					packetCreatorlock.notifyAll(); // Ensure all waiting threads are notified
				}
			}
		}

		packetCreatorActiveThreads.decrementAndGet();
	}

	private void handleTargetEnvHealthCheck() throws Exception {
		targetEnvActiveThreads.incrementAndGet();

		synchronized (targetEnvlock) {
			while (targetEnvPingHealthState.get() == TargetEnvPingHealthState.RUNNING) {
				targetEnvlock.wait();
			}

			if (targetEnvPingHealthState.get() == TargetEnvPingHealthState.COMPLETED) {
				Reporter.log("Target Environment health already checked by another thread.");
				targetEnvActiveThreads.decrementAndGet();
				return;
			}

			if (targetEnvPingHealthState.compareAndSet(TargetEnvPingHealthState.PENDING,
					TargetEnvPingHealthState.RUNNING)) {
				try {
					executeTargetEnvPingHealthCheck();
					targetEnvPingHealthState.set(TargetEnvPingHealthState.COMPLETED);
				} finally {
					targetEnvlock.notifyAll(); // Ensure all waiting threads are notified
				}
			}
		}

		targetEnvActiveThreads.decrementAndGet();
	}

	private void resetStateOnError(String module) {
		if (module.equalsIgnoreCase("packetcreator")) {
			synchronized (packetCreatorlock) {
				packetCreatorPingHealthState.set(PacketCreatorPingHealthState.PENDING);
			}
		} else {
			synchronized (targetEnvlock) {
				targetEnvPingHealthState.set(TargetEnvPingHealthState.PENDING);
			}
		}
	}

	private void finalizeState(String module) {
		if (module.equalsIgnoreCase("packetcreator")) {
			finalizePacketCreatorState();
		} else {
			finalizeTargetEnvState();
		}
	}

	private void finalizePacketCreatorState() {
		int remainingPacketCreatorThreads = packetCreatorActiveThreads.decrementAndGet();

		synchronized (packetCreatorlock) {
			if (remainingPacketCreatorThreads == 0) {
				packetCreatorPingHealthState.set(PacketCreatorPingHealthState.PENDING);
				logger.info("All Packet Creator threads done, resetting state to PENDING.");
			}
			packetCreatorlock.notifyAll();
		}
	}

	private void finalizeTargetEnvState() {
		int remainingTargetEnvThreads = targetEnvActiveThreads.decrementAndGet();

		synchronized (targetEnvlock) {
			if (remainingTargetEnvThreads == 0) {
				targetEnvPingHealthState.set(TargetEnvPingHealthState.PENDING);
				logger.info("All Target Env threads done, resetting state to PENDING.");
			}
			targetEnvlock.notifyAll();
		}
	}

	private void executeTargetEnvPingHealthCheck() throws Exception {

		String uri = baseUrl + "/ping/" + !dslConfigManager.isInServiceNotDeployedList(GlobalConstants.ESIGNET);
		Response response = getRequest(uri, "Health Check", step);
		JSONObject res = new JSONObject(response.asString());
		logger.info(res.toString());
		if (res.get("status").equals(true)) {
			logger.info("RESPONSE=" + res.toString());
		} else {
			logger.error("RESPONSE=" + res.toString());
			this.hasError = true;
			targetEnvPingHealthState.set(TargetEnvPingHealthState.PENDING); // Got error, so next thread has to perform
																			// health check
			throw new SkipException("Health check status" + res.toString());
		}
		Reporter.log("Target env status is up and healthy<br>");
	}

	private void executePacketCreatorPingHealthCheck() throws Exception {

		// Check packet creator up or not
		String packetcreatorUri = baseUrl + "/actuator/health";
		String serviceStatus = checkActuatorNoAuth(packetcreatorUri);
		if (!serviceStatus.equalsIgnoreCase("UP")) {
			this.hasError = true;
			packetCreatorPingHealthState.set(PacketCreatorPingHealthState.PENDING); // Got error, so next thread has to
																					// perform health check
			throw new SkipException("Packet creator Not responding");
		} else {
			Reporter.log("Packet creator status is up and healthy<br>");
		}
	}

	public static String checkActuatorNoAuth(String actuatorURL) {
		Response response = null;
		response = given().contentType(ContentType.JSON).get(actuatorURL);
		if (response != null && response.getStatusCode() == 200) {
			logger.info(response.getBody().asString());
			JSONObject jsonResponse = new JSONObject(response.getBody().asString());
			return jsonResponse.getString("status");
		}
		return "No Response";
	}
}
