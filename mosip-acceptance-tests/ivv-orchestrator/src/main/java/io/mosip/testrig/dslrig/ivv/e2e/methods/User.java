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
		int detailsParamIndex = 2;
		if (step.getParameters().size() >= 2) {
			user = step.getParameters().get(1);
			if (!user.contains("@@") && step.getParameters().size() >= 3 && usesCombinedUserPassword(calltype)) {
				String passwordParam = step.getParameters().get(2);
				if (passwordParam != null && !passwordParam.startsWith("$$")) {
					String pwdPart = passwordParam.startsWith("@@") ? passwordParam.substring(2) : passwordParam;
					user = user + "@@" + pwdPart;
					detailsParamIndex = 3;
				}
			}
			if (user.contains("@@")) {
				String userDetails[] = user.split("@@", 2);
				indexOfUser = userDetails[0];
				user = userDetails[0];
				if (user.contains("dsl-0"))
					user = BaseTestCase.currentModule+"-" + dslConfigManager.getUserAdminName();
				else
					user = dslConfigManager.getUserAdminName().substring(0,
							dslConfigManager.getUserAdminName().length() - 1) + user;
				pwd = userDetails.length > 1 ? userDetails[1] : null;
				if (pwd == null || pwd.isBlank()) {
					pwd = dslConfigManager.getproperty("admin_password");
				}
				if (userDetails.length > 2) {
					zone = userDetails[2];
				}
			}
			if (user.startsWith("$$")) {
				map = step.getScenario().getVariables();
				user = map.get("userid" + indexOfUser);

			}
		}
		if (step.getParameters().size() > detailsParamIndex) {
			String detailsParam = step.getParameters().get(detailsParamIndex);
			if (detailsParam.contains("$$uin")) {
				uin = detailsParam;
				uin = step.getScenario().getVariables().get(uin);
				map.put("uin", uin);
			}

			id = detailsParam;
			if (id.startsWith("$$")) {
				map2 = step.getScenario().getVariables();
				map.putAll(map2);
				map.put("userid", user);
				map.put("userpassword", pwd);
			}
		}
		BaseTestCase.dslUser = user;
		if(pwd!=null)
		BaseTestCase.dslUserPwd = pwd;

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
			KeycloakUserManager.createUsers(user, pwd, attrmap);
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
			KeycloakUserManager.createUsers(user, pwd,  attrmap2);
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

		case "UPDATE_UIN":
			HashMap<String, List<String>> attrmap1 = new HashMap<String, List<String>>();
			List<String> list1 = new ArrayList<String>();
			String val1 = map.get("uin") != null ? map.get("uin") : "11000000";
			list1.add(val1);
			attrmap1.put("individualId", list1);


			KeycloakUserManager.removeUser(user);
			KeycloakUserManager.createUsers(user, pwd,  attrmap1);
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
			HashMap<String, List<String>> attrmap3 = new HashMap<String, List<String>>();
			List<String> list3 = new ArrayList<String>();
			String val3 = map.get("$$uin") != null ? map.get("$$uin") : "11000000";
			list3.add(val3);
			attrmap3.put("individualId", list3);
			KeycloakUserManager.createUsers(user, pwd,  attrmap3);
			HashMap<String, String> userdetails3 = new HashMap<String, String>();
			userdetails3.put("user", user);
			userdetails3.put("pwd", pwd);
			step.getScenario().getVariables().putAll(userdetails3);	
			break;

		}

	}

	/** DSL actions whose 2nd argument is {@code userIndex@@password}, not a flag or center index. */
	private static boolean usesCombinedUserPassword(String calltype) {
		return "ADD_User".equals(calltype) || "ADD_User_External_Packet".equals(calltype)
				|| "UPDATE_UIN".equals(calltype) || "ADD_WOREMOVE_User".equals(calltype)
				|| "DELETE_CENTERMAPPING".equals(calltype);
	}

	/**
	 * Legacy JSON {@code e2e_User(ADD_User, masterdata-0@@pwd)}: {@code admin_userName} without last char + user token
	 * (e.g. {@code solid0} + {@code masterdata-0} → {@code solidmasterdata-0}). Gherkin {@code dsl-0} is the same token.
	 * Numeric tokens ({@code 0@@}, {@code 1@@}) use the suffix as-is ({@code solid0}, {@code solid1}).
	 */
}
