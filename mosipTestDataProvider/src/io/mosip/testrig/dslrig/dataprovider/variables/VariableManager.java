package io.mosip.testrig.dslrig.dataprovider.variables;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static String CONFIG_PATH = DataProviderConstants.RESOURCE + "config/";
	public static String NS_DEFAULT = "mosipdefault";

	private static String VAR_SUBSTITUE_PATTERN = "\\{\\{%s\\}\\}";
	private static String VAR_FIND_PATTERN = "\\{\\{[_a-zA-Z]+[0-9]*[\\.]?[_a-zA-Z]+[0-9]*\\}\\}";

	static Hashtable<String, Cache<String, Object>> varNameSpaces;
	static MutableConfiguration<String, Object> cacheConfig;
	static CacheManager cacheManager;
	static boolean bInit = false;

	static {
		Init(NS_DEFAULT);
	}

	public static boolean isInit() {
		return bInit;
	}

	public static void Init(String contextKey) {

		if (bInit)
			return;
		// resolve a cache manager
		CachingProvider cachingProvider = Caching.getCachingProvider();
		cacheManager = cachingProvider.getCacheManager();

		// configure the cache
		cacheConfig = new MutableConfiguration<String, Object>().setTypes(String.class, Object.class)
				.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY)).setStatisticsEnabled(true);

		// create the cache
		if (varNameSpaces == null) {
			varNameSpaces = new Hashtable<String, Cache<String, Object>>();
			// load predefined variables

			Cache<String, Object> cache = cacheManager.createCache(contextKey, cacheConfig);
			varNameSpaces.put(contextKey, cache);

		}
		CONFIG_PATH = DataProviderConstants.RESOURCE + "config/";
		Boolean bret = loadNamespaceFromPropertyFile(CONFIG_PATH + "default.properties", NS_DEFAULT);
		bInit = bret;

	}

	static Cache<String, Object> createNameSpace(String contextKey) {
		Cache<String, Object> ht = null;
		try {
			ht = varNameSpaces.get(contextKey);
		} catch (Exception e) {
		}
		if (ht == null) {
			ht = cacheManager.createCache(contextKey, cacheConfig);
			varNameSpaces.put(contextKey, ht);
		}
		return ht;
	}

	public static Object setVariableValue(String contextKey, String varName, Object value) {
		Cache<String, Object> ht = createNameSpace(contextKey);
		ht.put(varName, value);
		return value;
	}

	public static String[] findVariables(String text) {

		HashSet<String> set = new HashSet<String>();

		Pattern pattern = Pattern.compile(VAR_FIND_PATTERN);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String extract = text.substring(matcher.start(), matcher.end());
		
			if (extract != null && extract.startsWith("{{"))
				extract = extract.substring(2);
			if (extract != null && extract.endsWith("}}"))
				extract = extract.substring(0, extract.length() - 2);

			set.add(extract.trim());

		}
		String[] a = new String[set.size()];
		return set.toArray(a);
	}

	public static Object getVariableValue(String contextKey, String varName) {

		if (!bInit)
			Init(contextKey);

		Cache<String, ?> ht = null;
		Object ret = null;
		try {
			ht = varNameSpaces.get(contextKey);

			if (ht != null) {
				ret = ht.get(varName);
				return ret;
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
				logger.info(contextKey , ".{}" , key.toString() , "={}" , value.toString());
			});
			bRet = true;

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return bRet;
	}
	
	public static String deleteNameSpace(String contextKey) {
        try {
        	printAllContents();
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

	
	static String substituteVaraiable(String text, String varName, String varValue) {
		String formatVarName = String.format(VAR_SUBSTITUE_PATTERN, varName);
		return text.replaceAll(formatVarName, varValue);

	}

	public static void main(String[] args) {

		String text = "{ \"Name\" : \"welcome to {{country}} and {{default.var2}} and {{location.address}}\" }";

		String[] vars = findVariables(text);
		for (String v : vars) {
			logger.info(v);
		}

	}

	public static void testJCache() {

		// resolve a cache manager
		CachingProvider cachingProvider = Caching.getCachingProvider();
		CacheManager cacheManager = cachingProvider.getCacheManager();

		// configure the cache
		MutableConfiguration<String, Object> config = new MutableConfiguration<String, Object>()
				.setTypes(String.class, Object.class)
				.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_HOUR)).setStatisticsEnabled(true);

		// create the cache
		Cache<String, Object> cache = cacheManager.createCache("simpleCache", config);

		// cache operations
		String key = "key";
		Integer value1 = 1;
		cache.put("key", value1);
		cache.remove(key);

	}

}
