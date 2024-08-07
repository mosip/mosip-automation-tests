package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.utils.KeycloakUserManager;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.UserHelper;

@Scope("prototype")
@Component
public class User extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(User.class);

	UserHelper userHelper = new UserHelper();

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String id = null;
		String user = null;
		String pwd = null;
		String zone = null;
		String calltype = null;
		int centerNum = 0;
		String indexOfUser = "";
		String uin = "";

		HashMap<String, String> map = new HashMap<String, String>();

		HashMap<String, String> map2 = new HashMap<String, String>();
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError(
					"Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} else {
			calltype = step.getParameters().get(0);

		}
		if (step.getParameters().size() >= 2) {
			user = step.getParameters().get(1);
			if (user.contains("@@")) {
				String userDetails[] = user.split("@@");
				indexOfUser = userDetails[0];
				user = userDetails[0];
				if (user.contains("masterdata-0"))
					user = "masterdata-" + ConfigManager.getUserAdminName();
				else
					user = ConfigManager.getUserAdminName().substring(0, ConfigManager.getUserAdminName().length() - 1)
							+ user;
				pwd = userDetails[1];
				if (userDetails.length == 3) {
					zone = userDetails[2];
				}
			}
			if (user.startsWith("$$")) {
				map = step.getScenario().getVariables();
				user = map.get("userid" + indexOfUser);

			}
		}
		if (step.getParameters().size() >= 3) {
			if (step.getParameters().get(2).contains("$$uin")) {
				uin = step.getParameters().get(2).toString();
				uin = step.getScenario().getVariables().get(uin);
				map.put("uin", uin);
			}
			// here holding activate deeacrivate flag
			id = step.getParameters().get(2);
			if (id.startsWith("$$")) {
				map2 = step.getScenario().getVariables();
				map.putAll(map2);
				map.put("userid", user);
				map.put("userpassword", pwd);
			}
		}

		switch (calltype) {
		case "DELETE_CENTERMAPPING":

			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(map);
			userHelper.deleteCenterMapping(user);
			break;
		case "DELETE_ZONEMAPPING":
			userHelper.deleteZoneMapping(user, zone);
			break;
		case "CREATE_CENTERMAPPING":
			centerNum = Integer.parseInt(id);
			userHelper.createCenterMapping(user, map, centerNum);
			break;

		case "CREATE_ZONEMAPPING":
			userHelper.createZoneMapping(map, user);
			break;
		case "ACTIVATE_CENTERMAPPING":
			userHelper.activateCenterMapping(user, id);
			break;
		case "ACTIVATE_ZONEMAPPING":
			userHelper.activateZoneMapping(user, id);
			break;
		case "CREATE_ZONESEARCH":
			map = userHelper.createZoneSearch(user, map);
			step.getScenario().getVariables().putAll(map);
			break;
		case "ADD_User":
			HashMap<String, List<String>> attrmap = new HashMap<String, List<String>>();
			List<String> list = new ArrayList<String>();
			String val = map.get("uin") != null ? map.get("uin") : "11000000";
			list.add(val);
			attrmap.put("individualId", list);
			KeycloakUserManager.createUsers(user, pwd, "roles", attrmap);
			zone = userHelper.getZoneOfUser(user);
			if (zone != null && zone.equalsIgnoreCase("NOTSET")) {
				zone = userHelper.getLeafZones();
				BaseTestCase.mapUserToZone(user, zone);
				BaseTestCase.mapZone(user);
			}
			HashMap<String, String> userdetails = new HashMap<String, String>();
			userdetails.put("user" + indexOfUser, user);
			userdetails.put("pwd", pwd);
			step.getScenario().getVariables().putAll(userdetails);

			break;

		case "UPDATE_UIN":
			HashMap<String, List<String>> attrmap1 = new HashMap<String, List<String>>();
			List<String> list1 = new ArrayList<String>();
			String val1 = map.get("uin") != null ? map.get("uin") : "11000000";
			list1.add(val1);
			attrmap1.put("individualId", list1);
			// Utilizing the remove user functionality to update the attribute
			// "individualId" with UIN
			KeycloakUserManager.removeUser(user);
			KeycloakUserManager.createUsers(user, pwd, "roles", attrmap1);
			zone = userHelper.getZoneOfUser(user);
			if (zone != null && zone.equalsIgnoreCase("NOTSET")) {
				zone = userHelper.getLeafZones();
				BaseTestCase.mapUserToZone(user, zone);
				BaseTestCase.mapZone(user);
			}
			HashMap<String, String> userdetails1 = new HashMap<String, String>();
			userdetails1.put("user" + indexOfUser, user);
			userdetails1.put("pwd", pwd);
			step.getScenario().getVariables().putAll(userdetails1);

			break;

		case "ADD_WOREMOVE_User":
			HashMap<String, List<String>> attrmap2 = new HashMap<String, List<String>>();
			List<String> list2 = new ArrayList<String>();
			String val2 = map.get("$$uin") != null ? map.get("$$uin") : "11000000";
			list2.add(val2);
			attrmap2.put("individualId", list2);
			KeycloakUserManager.createUsers(user, pwd, "roles", attrmap2);
			HashMap<String, String> userdetails2 = new HashMap<String, String>();
			userdetails2.put("user", user);
			userdetails2.put("pwd", pwd);
			step.getScenario().getVariables().putAll(userdetails2);

			break;

		}

	}

}
