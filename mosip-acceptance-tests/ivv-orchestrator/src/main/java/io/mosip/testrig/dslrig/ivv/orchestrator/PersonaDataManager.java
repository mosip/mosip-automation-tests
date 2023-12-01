
package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

public final class PersonaDataManager {
	private static final Logger logger = LoggerFactory.getLogger(PersonaDataManager.class);
	public static Hashtable<String, Cache<String, Object>> personaDataCollection;
	static MutableConfiguration<String, Object> cacheConfig;
	static CacheManager cacheManager;
	static boolean bInit = false;

	static {
		Init();
	}

	public static boolean isInit() {
		return bInit;
	}

	public static void Init() {
	    if (bInit) {
	        return;
	    }

	    try {
	        // resolve a cache manager
	        CachingProvider cachingProvider = Caching.getCachingProvider();
	        cacheManager = cachingProvider.getCacheManager();

	        // configure the cache
	        cacheConfig = new MutableConfiguration<String, Object>()
	            .setTypes(String.class, Object.class)
	            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
	            .setStatisticsEnabled(true);

	        // create the cache
	        if (personaDataCollection == null) {
	            personaDataCollection = new Hashtable<String, Cache<String, Object>>();
	        }
	        bInit = true;
	    } catch (Exception e) {
	        // Log the exception
	        logger.error("Error during initialization: " + e.getMessage(), e);
	        // You might also want to throw or handle the exception depending on your use case
	    }
	}

	static Cache<String, Object> createNameSpace(String key) {
		Cache<String, Object> ht = null;
		try {
			ht = personaDataCollection.get(key);
		} catch (Exception e) {
		}
		if (ht == null) {
			ht = cacheManager.createCache(key, cacheConfig);
			personaDataCollection.put(key, ht);
		}
		return ht;
	}

	public static Object setVariableValue(String key, String varName, Object value) {
		Cache<String, Object> ht = createNameSpace(key);
		ht.put(varName, value);
		return value;
	}

	public static Object getVariableValue(String key, String varName) {

		Cache<String, ?> ht = null;
		Object ret = null;
		try {
			ht = personaDataCollection.get(key);

			if (ht != null) {
				ret = ht.get(varName);
				return ret;
			}
		} catch (Exception e) {

			logger.error(e.getMessage());
		}
		return ret;
	}

	public static void main(String[] args) {

	}

}
