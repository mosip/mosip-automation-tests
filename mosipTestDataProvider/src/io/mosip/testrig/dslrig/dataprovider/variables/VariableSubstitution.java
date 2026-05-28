package io.mosip.testrig.dslrig.dataprovider.variables;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code {{variable}}} and {@code {{namespace.name}}} placeholders against {@link VariableManager}.
 */
public final class VariableSubstitution {

	private static final Pattern VAR_FIND_PATTERN = Pattern
			.compile("\\{\\{([_a-zA-Z][-_a-zA-Z0-9]*(?:\\.[_a-zA-Z][-_a-zA-Z0-9]*)*)\\}\\}");

	private VariableSubstitution() {
	}

	public static String[] findVariables(String text) {
		if (text == null || text.isEmpty()) {
			return new String[0];
		}
		Set<String> names = new LinkedHashSet<>();
		Matcher matcher = VAR_FIND_PATTERN.matcher(text);
		while (matcher.find()) {
			names.add(matcher.group(1).trim());
		}
		return names.toArray(new String[0]);
	}

	/**
	 * Replaces every {@code {{...}}} reference in {@code text} for which a value can be resolved.
	 * Unresolved placeholders are left unchanged.
	 */
	public static String substituteAll(String text, String contextKey) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		String result = text;
		Matcher matcher = VAR_FIND_PATTERN.matcher(text);
		while (matcher.find()) {
			String varRef = matcher.group(1).trim();
			String value = resolve(contextKey, varRef);
			if (value != null) {
				result = substituteOne(result, varRef, value);
			}
		}
		return result;
	}

	public static String substituteOne(String text, String varName, String varValue) {
		if (text == null || varName == null || varValue == null) {
			return text;
		}
		String placeholder = "{{" + varName + "}}";
		return text.replace(placeholder, varValue);
	}

	static String resolve(String contextKey, String varRef) {
		if (varRef == null || varRef.isBlank()) {
			return null;
		}
		Object direct = lookup(contextKey, varRef);
		if (direct != null) {
			return direct.toString();
		}
		int dot = varRef.indexOf('.');
		if (dot > 0) {
			String namespace = varRef.substring(0, dot);
			String name = varRef.substring(dot + 1);
			Object namespaced = lookup(resolveNamespace(namespace), name);
			if (namespaced != null) {
				return namespaced.toString();
			}
		}
		Object fallback = lookup(VariableManager.NS_DEFAULT, varRef);
		return fallback != null ? fallback.toString() : null;
	}

	private static String resolveNamespace(String namespace) {
		if (namespace == null) {
			return VariableManager.NS_DEFAULT;
		}
		if ("default".equalsIgnoreCase(namespace) || "mosipdefault".equalsIgnoreCase(namespace)) {
			return VariableManager.NS_DEFAULT;
		}
		return namespace;
	}

	private static Object lookup(String namespace, String name) {
		if (namespace == null || name == null || name.isBlank()) {
			return null;
		}
		try {
			return VariableManager.getVariableValue(namespace, name);
		} catch (Exception ignored) {
			return null;
		}
	}
}
