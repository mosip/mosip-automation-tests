package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.KeycloakUserManager;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.MachineHelper;
import io.mosip.testrig.dslrig.ivv.orchestrator.UserHelper;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class User extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(User.class);

	UserHelper userHelper = new UserHelper();

	static {
		if (dslConfigManager.IsDebugEnabled())
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
		MachineHelper machineHelper = new MachineHelper();


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
				if (user.contains("dsl-0"))
					user = "dsl-" + dslConfigManager.getUserAdminName();
				else
					user = dslConfigManager.getUserAdminName().substring(0,
							dslConfigManager.getUserAdminName().length() - 1) + user;
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
			if(zone==null) {
			zone = userHelper.getZoneOfUser(user);
			}
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
			AdminTestUtil.getRequiredField();
			step.getScenario().getVariables().putAll(userdetails);

			break;
			
		case "ADD_User_External_Packet":
			HashMap<String, String> userdetails2 = new HashMap<String, String>();
			try {
			HashMap<String, List<String>> attrmap2 = new HashMap<String, List<String>>();
			List<String> list2 = new ArrayList<String>();
			String val2 = map.get("uin") != null ? map.get("uin") : "11000000";
			list2.add(val2);
			attrmap2.put("individualId", list2);
			KeycloakUserManager.createUsers(user, pwd, "roles", attrmap2);
			userdetails2.put("user" + indexOfUser, user);
			userdetails2.put("pwd", pwd);
			String publicKey = machineHelper.createPublicKey();
			Map<String, String> result = machineHelper.getIdAndRegCenterIdByPublicKey(publicKey);
			userdetails2.put("centerId" + indexOfUser, result.get("regCenterId"));
			userdetails2.put("zoneCode", result.get("zoneCode"));
			userdetails2.put("langCode", BaseTestCase.languageCode);
			userdetails2.put("id", result.get("id"));
			userdetails2.put("userid", user);
			AdminTestUtil.getRequiredField();
			userHelper.deleteCenterMapping(user);
			if (zone == null) {
				zone = userHelper.getZoneOfUser(user);
			}
			userHelper.deleteZoneMapping(user, zone);
			userHelper.createZoneMapping(userdetails2, user);
			userHelper.activateZoneMapping(user, "T");
			userHelper.createCenterMapping(user, userdetails2, Integer.parseInt(indexOfUser));
			userHelper.activateCenterMapping(user, "T");
			}catch(Exception e) {
				logger.error("Unable to find External Machine daetils to maps with user "+e.getMessage());
			}
			step.getScenario().getVariables().putAll(userdetails2);
			break;
			
		case "DELETE_User":
			KeycloakUserManager.removeUser(user);

			break;

		case "ADD_WOREMOVE_User":
			HashMap<String, List<String>> attrmap3 = new HashMap<String, List<String>>();
			List<String> list3 = new ArrayList<String>();
			String val3 = map.get("$$uin") != null ? map.get("$$uin") : "11000000";
			list3.add(val3);
			attrmap3.put("individualId", list3);
			KeycloakUserManager.createUsers(user, pwd, "roles", attrmap3);
			HashMap<String, String> userdetails3 = new HashMap<String, String>();
			userdetails3.put("user", user);
			userdetails3.put("pwd", pwd);
			step.getScenario().getVariables().putAll(userdetails3);	
			break;

		}

	}

}
