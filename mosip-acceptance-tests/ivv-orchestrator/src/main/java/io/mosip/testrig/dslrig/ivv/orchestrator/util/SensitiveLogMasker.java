package io.mosip.testrig.dslrig.ivv.orchestrator.util;



import java.util.Locale;

import java.util.Set;

import java.util.regex.Matcher;

import java.util.regex.Pattern;



import org.json.JSONArray;

import org.json.JSONObject;



/**

 * Masks secrets, keys, tokens, and credentials in DSL TestNG report output.

 */

public final class SensitiveLogMasker {



	public static final String MASK = "***";



	private static final Set<String> SENSITIVE_JSON_KEYS = Set.of(

			"password", "passwd", "pwd", "secret", "clientsecret", "client_secret",

			"token", "authtoken", "auth_token", "accesstoken", "access_token",

			"refreshtoken", "refresh_token", "idtoken", "id_token",

			"authorization", "apikey", "api_key", "privatekey", "private_key",

			"secretkey", "secret_key", "sessionkey", "session_key",

			"credential", "credentials", "otp", "pin", "signature", "thumbprint",

			"encrypteddata", "encrypted_data", "decrypteddata", "decrypted_data",

			"zkprivatekey", "zk_private_key", "cookie", "setcookie", "set_cookie",

			"certificate", "cert", "certdata", "cert_data", "keystore", "p12",

			"symmetrickey", "symmetric_key", "rsa", "aeskey", "aes_key");



	private static final Set<String> NON_SENSITIVE_KEY_SUFFIX = Set.of(

			"contextkey", "primarykey", "foreignkey", "pathkey", "mapkey",

			"clusterkey", "registrykey", "machinekey", "centerkey", "testcasekey",

			"idtypekey", "fieldkey", "labelkey");



	private static final Pattern AUTHORIZATION_HEADER = Pattern.compile(

			"(?i)(Authorization\\s*[=:]\\s*)([^\\s,\\]\\}\\n]+)");



	private static final Pattern BEARER_TOKEN = Pattern.compile(

			"(?i)(Bearer\\s+)([A-Za-z0-9\\-._~+/]+=*)");



	private static final Pattern COOKIE_AUTH = Pattern.compile(

			"(?i)(Authorization=)([^;\\s]+)");



	private static final Pattern SENSITIVE_HEADER = Pattern.compile(

			"(?i)((?:Cookie|Set-Cookie|X-API-Key|X-Auth-Token|X-MOSIP-TOKEN|Cookie-Auth)\\s*[=:]\\s*)([^\\s,\\]\\}\\n;]+)");



	private static final Pattern JSON_LIKE_SECRET = Pattern.compile(

			"(?i)(\"(?:password|clientSecret|client_secret|secret|token|access_token|refresh_token|privateKey|private_key|secretKey|secret_key|apiKey|api_key|authorization|sessionKey|session_key|cookie|certificate|cert)\"\\s*:\\s*\")([^\"]*)(\")");



	private static final Pattern QUERY_SECRET = Pattern.compile(

			"(?i)((?:client_secret|clientSecret|secret|token|password|api_key|apiKey|access_token|refresh_token)=)([^&\\s]+)");



	private static final Pattern JWT = Pattern.compile(

			"eyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");



	private static final Pattern PEM_PRIVATE_KEY = Pattern.compile(

			"-----BEGIN(?: [A-Z]+)? PRIVATE KEY-----[\\s\\S]*?-----END(?: [A-Z]+)? PRIVATE KEY-----");



	private SensitiveLogMasker() {

	}



	public static String mask(String text) {

		if (text == null || text.isEmpty()) {

			return text;

		}

		String trimmed = text.trim();

		if (trimmed.startsWith("{")) {

			try {

				return maskJsonObject(new JSONObject(trimmed)).toString(2);

			} catch (Exception ignored) {

				// fall through to pattern masking

			}

		}

		if (trimmed.startsWith("[")) {

			try {

				return maskJsonArray(new JSONArray(trimmed)).toString(2);

			} catch (Exception ignored) {

				// fall through

			}

		}

		return maskPatterns(text);

	}



	private static JSONObject maskJsonObject(JSONObject obj) {

		JSONObject out = new JSONObject();

		for (String key : obj.keySet()) {

			Object value = obj.get(key);

			if (isSensitiveKey(key)) {

				out.put(key, MASK);

			} else if (value instanceof JSONObject) {

				out.put(key, maskJsonObject((JSONObject) value));

			} else if (value instanceof JSONArray) {

				out.put(key, maskJsonArray((JSONArray) value));

			} else if (value instanceof String) {

				out.put(key, mask((String) value));

			} else {

				out.put(key, value);

			}

		}

		return out;

	}



	private static JSONArray maskJsonArray(JSONArray arr) {

		JSONArray out = new JSONArray();

		for (int i = 0; i < arr.length(); i++) {

			Object value = arr.get(i);

			if (value instanceof JSONObject) {

				out.put(maskJsonObject((JSONObject) value));

			} else if (value instanceof JSONArray) {

				out.put(maskJsonArray((JSONArray) value));

			} else if (value instanceof String) {

				out.put(mask((String) value));

			} else {

				out.put(value);

			}

		}

		return out;

	}



	private static boolean isSensitiveKey(String key) {

		if (key == null) {

			return false;

		}

		String normalized = key.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);

		if (SENSITIVE_JSON_KEYS.contains(normalized)) {

			return true;

		}

		if (normalized.contains("secret") || normalized.contains("password")

				|| normalized.endsWith("token") || normalized.contains("privatekey")

				|| normalized.contains("cookie") || normalized.contains("certificate")) {

			return true;

		}

		if (normalized.endsWith("key") && !NON_SENSITIVE_KEY_SUFFIX.contains(normalized)) {

			return normalized.contains("private") || normalized.contains("secret")

					|| normalized.contains("session") || normalized.contains("auth")

					|| normalized.contains("encrypt") || normalized.contains("api")

					|| normalized.contains("symmetric") || normalized.contains("partner");

		}

		return false;

	}



	private static String maskPatterns(String text) {

		String result = text;

		result = PEM_PRIVATE_KEY.matcher(result).replaceAll("-----BEGIN PRIVATE KEY-----" + MASK + "-----END PRIVATE KEY-----");

		result = JWT.matcher(result).replaceAll(MASK);

		result = replaceAll(JSON_LIKE_SECRET, result, 1, 3);

		result = replaceAll(QUERY_SECRET, result, 1, 0);

		result = replaceAll(AUTHORIZATION_HEADER, result, 1, 0);

		result = replaceAll(BEARER_TOKEN, result, 1, 0);

		result = replaceAll(COOKIE_AUTH, result, 1, 0);

		result = replaceAll(SENSITIVE_HEADER, result, 1, 0);

		return result;

	}



	private static String replaceAll(Pattern pattern, String input, int prefixGroup, int suffixGroup) {

		Matcher matcher = pattern.matcher(input);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {

			String replacement = matcher.group(prefixGroup) + MASK;

			if (suffixGroup > 0 && matcher.groupCount() >= suffixGroup) {

				replacement += matcher.group(suffixGroup);

			}

			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));

		}

		matcher.appendTail(sb);

		return sb.toString();

	}

}

