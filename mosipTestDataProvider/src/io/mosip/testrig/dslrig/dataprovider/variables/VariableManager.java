package io.mosip.testrig.dslrig.dataprovider.variables;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;

public final class VariableManager {
	private static final Logger logger = LoggerFactory.getLogger(VariableManager.class);
	public static String CONFIG_PATH = DataProviderConstants.getResource() + "config/";
	public static String NS_DEFAULT = "mosipdefault";

	static ConcurrentHashMap<String, Cache<String, Object>> varNameSpaces;
	static MutableConfiguration<String, Object> cacheConfig;
	static CacheManager cacheManager;
	static boolean bInit = false;

	static {
		Init();
	}

	public static boolean isInit() {
		return bInit;
	}

	public static synchronized void Init() {
		synchronized (VariableManager.class) {
			if (bInit)
				return;

			CachingProvider cachingProvider = Caching.getCachingProvider();
			cacheManager = cachingProvider.getCacheManager();

			cacheConfig = new MutableConfiguration<String, Object>()
					.setTypes(String.class, Object.class)
					.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
					.setStatisticsEnabled(true);

			if (varNameSpaces == null) {
				varNameSpaces = new ConcurrentHashMap<>();
				Cache<String, Object> cache = cacheManager.createCache(NS_DEFAULT, cacheConfig);
				varNameSpaces.put(NS_DEFAULT, cache);
			}
			CONFIG_PATH = DataProviderConstants.getResource() + "config/";
			Boolean bret = loadNamespaceFromPropertyFile(CONFIG_PATH + "default.properties", NS_DEFAULT);
			bInit = bret;
		}
	}

	static Cache<String, Object> createNameSpace(String contextKey) {
		return varNameSpaces.computeIfAbsent(contextKey, key -> {
			synchronized (cacheManager) {
				Cache<String, Object> existing = cacheManager.getCache(key, String.class, Object.class);
				if (existing != null) {
					return existing;
				}
				return cacheManager.createCache(key, cacheConfig);
			}
		});
	}

	public static Object setVariableValue(String contextKey, String varName, Object value) {
		Cache<String, Object> ht = createNameSpace(contextKey);
		ht.put(varName, value);
		return value;
	}

	public static boolean removeVariableValue(String contextKey, String varName) {
		Cache<String, Object> ht = varNameSpaces.get(contextKey);
		if (ht == null) {
			return false;
		}
		return ht.remove(varName);
	}

	public static Object appendVariableValue(String contextKey,String varName,Object newValue){
		if (newValue==null || newValue.toString().isEmpty())
			return null;
	Cache<String,Object> ht=createNameSpace(contextKey);
	Object existingValue=getVariableValue(contextKey,varName);
	String newVal=newValue.toString();
	if(existingValue==null){
		ht.put(varName,newVal);
		return newVal;
	}else{
		String existing=existingValue.toString();
		if(!java.util.Arrays.asList(existing.split(",")).contains(newVal)){
			existing=existing+","+newVal;
			ht.put(varName,existing);
		}
		return existing;
	}
}

	public static String[] findVariables(String text) {
		return VariableSubstitution.findVariables(text);
	}

	/**
	 * Replaces all {@code {{variable}}} placeholders using values from {@code contextKey} and
	 * {@link #NS_DEFAULT} (including {@code {{default.name}}} aliases).
	 */
	public static String substituteAll(String text, String contextKey) {
		return VariableSubstitution.substituteAll(text, contextKey);
	}

	public static Object getVariableValue(String contextKey, String varName) {
		if (!bInit)
			Init();
		Cache<String, ?> ht = varNameSpaces.get(contextKey);
		Object ret = null;
		try {
			if (ht != null) {
				ret = ht.get(varName);
				if (ret == null && contextKey.equalsIgnoreCase(NS_DEFAULT)) {

					loadNamespaceFromPropertyFile(CONFIG_PATH + "default.properties", NS_DEFAULT);
					ht = varNameSpaces.get(contextKey);
					ret = ht.get(varName);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return ret;
	}

	public static Boolean loadNamespaceFromPropertyFile(String propFile, String contextKey) {
		Boolean bRet = false;
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(propFile)) {
			props.load(fis);

			props.forEach((key, value) -> {
				setVariableValue(contextKey, key.toString(), value);
				logger.info(contextKey, ".{}", key.toString(), "={}", value.toString());
			});
			bRet = true;

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return bRet;
	}

	public static String deleteNameSpace(String contextKey) {
		try {
			if (logger.isDebugEnabled()) {
				printAllContents();
			}
			Cache<String, Object> cache = varNameSpaces.remove(contextKey);
			if (cache != null) {
				synchronized (cacheManager) {
					cacheManager.destroyCache(contextKey);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return "false";
		}
		return "true";
	}

	public static void printAllContents() {
		if (!logger.isDebugEnabled()) {
			return;
		}
		StringBuffer s = new StringBuffer();
		for (String nameSpace : varNameSpaces.keySet()) {
			Cache<String, Object> cache = varNameSpaces.get(nameSpace);
			s.append("Contents of Namespace: " + nameSpace + "\\n");
			for (Cache.Entry<String, Object> entry : cache) {
				String varName = entry.getKey();
				Object value = entry.getValue();
				s.append(varName + " = " + value + "\\n");
			}
		}
		logger.info(s.toString());
	}

	@Deprecated
	static String substituteVaraiable(String text, String varName, String varValue) {
		return VariableSubstitution.substituteOne(text, varName, varValue);
	}

	public static void main(String[] args) {

		String text = "{ \"Name\" : \"welcome to {{country}} and {{default.var2}} and {{location.address}}\" }";

		String[] vars = findVariables(text);
		for (String v : vars) {
			logger.info(v);
		}

	}

	public static void testJCache() {


		CachingProvider cachingProvider = Caching.getCachingProvider();
		CacheManager cacheManager = cachingProvider.getCacheManager();


		MutableConfiguration<String, Object> config = new MutableConfiguration<String, Object>()
				.setTypes(String.class, Object.class)
				.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_HOUR)).setStatisticsEnabled(true);


		Cache<String, Object> cache = cacheManager.createCache("simpleCache", config);


		String key = "key";
		Integer value1 = 1;
		cache.put("key", value1);
		cache.remove(key);

	}

}
