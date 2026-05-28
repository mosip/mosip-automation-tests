package io.mosip.testrig.dslrig.dataprovider.preparation;

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

/**
 * Dedicated JCache storage for run-scoped masterdata and auth tokens.
 * Keeps {@code md:*} / {@code auth:*} entries out of {@link io.mosip.testrig.dslrig.dataprovider.variables.VariableManager}
 * scenario namespaces.
 */
public final class MasterdataCacheStore {

	private static final Logger logger = LoggerFactory.getLogger(MasterdataCacheStore.class);
	private static final String NAMESPACE_PREFIX = "md_cache:";

	private static final ConcurrentHashMap<String, Cache<String, Object>> CACHES = new ConcurrentHashMap<>();
	private static volatile CacheManager cacheManager;
	private static volatile MutableConfiguration<String, Object> cacheConfig;
	private static volatile boolean initialized;

	private MasterdataCacheStore() {
	}

	private static void ensureInit() {
		if (initialized) {
			return;
		}
		synchronized (MasterdataCacheStore.class) {
			if (initialized) {
				return;
			}
			CachingProvider cachingProvider = Caching.getCachingProvider();
			cacheManager = cachingProvider.getCacheManager();
			cacheConfig = new MutableConfiguration<String, Object>()
					.setTypes(String.class, Object.class)
					.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
					.setStatisticsEnabled(true);
			initialized = true;
		}
	}

	static String toCacheNamespace(String runContextNamespace) {
		if (runContextNamespace == null || runContextNamespace.isBlank()) {
			return NAMESPACE_PREFIX + "default";
		}
		return NAMESPACE_PREFIX + runContextNamespace.trim();
	}

	private static Cache<String, Object> getOrCreateCache(String runContextNamespace) {
		ensureInit();
		String cacheName = toCacheNamespace(runContextNamespace);
		return CACHES.computeIfAbsent(cacheName, name -> {
			synchronized (cacheManager) {
				Cache<String, Object> existing = cacheManager.getCache(name, String.class, Object.class);
				if (existing != null) {
					return existing;
				}
				return cacheManager.createCache(name, cacheConfig);
			}
		});
	}

	public static Object get(String runContextNamespace, String key) {
		if (key == null) {
			return null;
		}
		Cache<String, Object> cache = CACHES.get(toCacheNamespace(runContextNamespace));
		if (cache == null) {
			return null;
		}
		try {
			return MosipDataSetup.fromCacheValue(cache.get(key));
		} catch (Exception e) {
			logger.debug("Masterdata cache read failed for key {}: {}", key, e.getMessage());
			return null;
		}
	}

	public static void put(String runContextNamespace, String key, Object value) {
		if (key == null || value == null) {
			return;
		}
		getOrCreateCache(runContextNamespace).put(key, MosipDataSetup.toCacheValue(value));
	}

	public static void clearNamespace(String runContextNamespace) {
		if (runContextNamespace == null || runContextNamespace.isBlank()) {
			return;
		}
		String cacheName = toCacheNamespace(runContextNamespace);
		Cache<String, Object> cache = CACHES.remove(cacheName);
		if (cache != null) {
			synchronized (cacheManager) {
				try {
					cacheManager.destroyCache(cacheName);
				} catch (Exception e) {
					// Ensure no stale entries leak across runs even when destroy is unsupported/fails.
					try {
						cache.clear();
					} catch (Exception clearException) {
						logger.warn("Failed to clear masterdata cache {} after destroy failure: {}", cacheName,
								clearException.getMessage());
					}
					logger.warn("Failed to destroy masterdata cache {}: {}", cacheName, e.getMessage());
				}
			}
		}
	}
}
