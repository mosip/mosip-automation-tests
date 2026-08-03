package io.mosip.testrig.dslrig.packetcreator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.mosip.testrig.dslrig.dataprovider.cache.PersonaTemplateCache;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.cache.PacketTemplateCache;

/**
 * Shared reset/clear logic for {@link io.mosip.testrig.dslrig.packetcreator.controller.ContextController}
 * and {@link io.mosip.testrig.dslrig.packetcreator.controller.RunCacheController}.
 */
@Service
public class ContextResetService {

	private static final Logger logger = LoggerFactory.getLogger(ContextResetService.class);

	private final ContextUtils contextUtils;
	private final APIRequestUtil apiRequestUtil;

	public ContextResetService(ContextUtils contextUtils, APIRequestUtil apiRequestUtil) {
		this.contextUtils = contextUtils;
		this.apiRequestUtil = apiRequestUtil;
	}

	/**
	 * Clears run-scoped MOSIP cache ({@code urlBase + run_context}), in-memory auth tokens,
	 * and packet-creator token map. Used after the test suite via {@code /runCache/clear}.
	 *
	 * <p>Only clears entries scoped to {@code contextKey}: this codebase runs scenarios in
	 * parallel, and a global clear here would evict other in-flight contexts' warm template
	 * caches (see {@code APIRequestUtil.clearToken}, which documents the same hazard).
	 * {@code PersonaParseCache} is keyed by file path + mtime rather than context, so it does
	 * not need (or support) a context-scoped clear.
	 */
	public void clearRunScopedCache(String contextKey) {
		apiRequestUtil.clearRunScopedCache(contextKey);
		PacketTemplateCache.clearContext(contextKey);
		PersonaTemplateCache.clearContext(contextKey);
		logger.info("Run-scoped cache cleared for context {}", contextKey);
	}

	/**
	 * Full context reset: temp packet folders, run cache, and the entire context variable namespace.
	 * Used by {@code /resetContextData} when a scenario needs a clean slate.
	 */
	public void resetContextData(String contextKey) throws Exception {
		contextUtils.clearPacketGenFolders(contextKey);
		clearRunScopedCache(contextKey);
		SchemaUtil.invalidateContext(contextKey);
		VariableManager.deleteNameSpace(contextKey);
		logger.info("Context data reset for context {}", contextKey);
	}
}
