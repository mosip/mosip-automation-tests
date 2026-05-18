package io.mosip.testrig.dslrig.dataprovider.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;


public final class DemographicMissFieldUtil {

	private DemographicMissFieldUtil() {
	}

	public static List<String> expandMissAttributeIds(List<String> raw, List<MosipIDSchema> schema, String contextKey) {
		if (raw == null || raw.isEmpty()) {
			return raw == null ? new ArrayList<>() : new ArrayList<>(raw);
		}
		LinkedHashSet<String> out = new LinkedHashSet<>();
		for (String token : raw) {
			if (token == null) {
				continue;
			}
			String t = token.trim();
			if (t.isEmpty()) {
				continue;
			}
			String tl = t.toLowerCase(Locale.ROOT);
			switch (tl) {
			case "name":
				out.add("fullName");
				out.add("firstName");
				out.add("lastName");
				out.add("middleName");
				addCommaSeparatedContextKeys(out, contextKey, "name");
				break;
			case "dob":
				out.add("dateOfBirth");
				out.add("dob");
				out.add("birthdate");
				addCommaSeparatedContextKeys(out, contextKey, "dob");
				break;
			case "phone":
				out.add("phone");
				out.add("phoneNumber");
				out.add("mobileNumber");
				out.add("mobileno");
				addSchemaIdsMatching(schema, out, "phone", "mobile");
				addCommaSeparatedContextKeys(out, contextKey, "phone");
				break;
			case "email":
				out.add("email");
				out.add("emailId");
				addSchemaIdsMatching(schema, out, "email", "mail");
				addCommaSeparatedContextKeys(out, contextKey, "emailId");
				break;
			case "address":
				addSchemaIdsMatching(schema, out, "address");
				addCommaSeparatedContextKeys(out, contextKey, "addressgroup");
				break;
			default:
				out.add(t);
				break;
			}
		}
		return new ArrayList<>(out);
	}

	private static void addCommaSeparatedContextKeys(LinkedHashSet<String> out, String contextKey, String varKey) {
		Object v = VariableManager.getVariableValue(contextKey, varKey);
		if (v == null) {
			return;
		}
		for (String p : v.toString().split(",")) {
			String s = p.trim();
			if (!s.isEmpty()) {
				out.add(s);
			}
		}
	}

	private static void addSchemaIdsMatching(List<MosipIDSchema> schema, LinkedHashSet<String> out, String... needles) {
		if (schema == null) {
			return;
		}
		for (MosipIDSchema s : schema) {
			if (s == null || s.getId() == null) {
				continue;
			}
			String il = s.getId().toLowerCase(Locale.ROOT);
			for (String needle : needles) {
				if (il.contains(needle.toLowerCase(Locale.ROOT))) {
					out.add(s.getId());
					break;
				}
			}
		}
	}
}
