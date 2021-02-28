package variables;

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

import org.mosip.dataprovider.util.DataProviderConstants;

public final class VariableManager {
	public static String CONFIG_PATH = DataProviderConstants.RESOURCE+"config/";
	public static String NS_DEFAULT = "mosipdefault";
//	public static String NS_PREREG = "prereg";
//	public static String NS_REGCLIENT = "regclient";
	
	public static String NS_MASTERDATA = "masterdata";

	private static String VAR_SUBSTITUE_PATTERN = "\\{\\{%s\\}\\}";
	private static String VAR_FIND_PATTERN = "\\{\\{[_a-zA-Z]+[0-9]*[\\.]?[_a-zA-Z]+[0-9]*\\}\\}";
	
	
//	static Hashtable<String, Hashtable<String,Object>> varNameSpaces;
	
	static Hashtable<String, Cache<String,Object>> varNameSpaces;
	static MutableConfiguration<String, Object> cacheConfig;
	static CacheManager cacheManager;
	static boolean bInit= false;
	
	static {
		Init();				
	}
	
	public static void Init() {
		
		if(bInit) return;
		 //resolve a cache manager
		 CachingProvider cachingProvider = Caching.getCachingProvider();
		 cacheManager = cachingProvider.getCacheManager();

		 //configure the cache
		 cacheConfig =
		    new MutableConfiguration<String, Object>()
		    .setTypes( String.class, Object.class)
		    .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
		    .setStatisticsEnabled(true);

		 //create the cache
		
		 varNameSpaces = new Hashtable<String, Cache<String,Object>>();
			//load predefined variables
			
			 Cache<String,Object> cache = cacheManager.createCache ( NS_DEFAULT, cacheConfig);
			 varNameSpaces.put(NS_DEFAULT, cache);
			
		//	cache = cacheManager.createCache(NS_PREREG, cacheConfig);
		//	varNameSpaces.put(NS_PREREG, cache);
			
			cache = cacheManager.createCache(NS_MASTERDATA, cacheConfig);
			varNameSpaces.put(NS_MASTERDATA, cache);
			
		//	cache = cacheManager.createCache(NS_REGCLIENT, cacheConfig);
		//	varNameSpaces.put(NS_REGCLIENT, cache);
			
		//	loadNamespaceFromPropertyFile(CONFIG_PATH+"prereg.properties", NS_PREREG);
			loadNamespaceFromPropertyFile(CONFIG_PATH+"default.properties", NS_DEFAULT);
			loadNamespaceFromPropertyFile(CONFIG_PATH+"masterdata.properties", NS_MASTERDATA);
		//	loadNamespaceFromPropertyFile(CONFIG_PATH+"regclient.properties", NS_REGCLIENT);
			bInit = true;
		
	}
	static Cache<String, Object> createNameSpace(String namespaceName) {
		Cache<String,Object> ht = null;
		try {
			ht = varNameSpaces.get(namespaceName);
		}catch(Exception e) {}
		if(ht == null) {
			ht  = cacheManager.createCache(namespaceName, cacheConfig);
			varNameSpaces.put(namespaceName, ht);
		}
		return ht;
	}
	public static Object setVariableValue(String namespaceName, String varName, Object value) {
		Cache<String,Object> ht = createNameSpace(namespaceName);
		ht.put(varName, value);
		return value;
	}
	public static Object setVariableValue(String varName, Object value) {

		return setVariableValue(NS_DEFAULT, varName, value);
	}
	public static String[] findVariables(String text) {

		HashSet<String> set = new HashSet<String>();
		
		Pattern pattern = Pattern.compile(VAR_FIND_PATTERN);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String extract = text.substring(matcher.start(), matcher.end());
			//Hack - Strip leading and trailing {{ }}
			if(extract != null && extract.startsWith("{{"))
				extract = extract.substring(2);
			if(extract != null && extract.endsWith("}}"))
				extract = extract.substring(0, extract.length()-2);
			
			set.add(extract.trim());
		
		}
		String[] a= new String[set.size()];
		return  set.toArray(a);
	}
	public static Object getVariableValue(String namespaceName, String varName) {
		Cache<String,?> ht = null;
		Object ret = null;
		try {
			ht =  varNameSpaces.get(namespaceName);
		}catch(Exception e) {}
		if(ht != null) {
			ret =  ht.get(varName);
		}
		return ret;
	}
	public static Object getVariableValue(String varName) {
		return getVariableValue(NS_DEFAULT, varName);
	}
	public static Boolean loadNamespaceFromPropertyFile(String propFile, String nameSpaceName) {
		Boolean bRet = false;
		Properties props = new Properties();
		try {
			props.load( new FileInputStream(propFile));
			
			props.forEach( (key,value) -> {
				setVariableValue(nameSpaceName, key.toString(), value);
				System.out.println(nameSpaceName+"."+ key.toString()+"="+ value.toString());
			});
			bRet = true;
			
		} catch (IOException e) {
			
		}
		
		return bRet;
	}
	/*
	 * Variables are embedded inside a text in following format {{varname}}
	 */
	static String substituteVaraiable(String text, String varName, String varValue) {
		String formatVarName = String.format( VAR_SUBSTITUE_PATTERN, varName);
		return text.replaceAll( formatVarName, varValue);
		
	}
	/* Get list of variables used in the text
	 * variable naming -> namespace.varname
	 * if no namespace specified then 'default' is assumed
	 * Get value for that variable and substitute the value for all instance of variable in the text
	 */
	public static String substituteVariables(String text) {
		String [] vars = findVariables(text);
		for(String v: vars) {
			String [] parts = v.split("\\.");
			String nameSpace = null;
			String value =null;
			String varName = v.trim();
			if(parts.length > 1) {
				nameSpace = parts[0].trim();
				varName = parts[1].trim();
				value = (String) getVariableValue(nameSpace, varName);
			}
			else
				value = (String) getVariableValue(varName);
			value = value ==null ? "": value;
			text = substituteVaraiable(text, v, value);
			
		}
		return text;
	}
	public static void main(String[] args) {
		
	//	loadNamespaceFromPropertyFile("resource/default.properties", "default");
	//	loadNamespaceFromPropertyFile("resource/test1.properties", "location");
	//	testJCache();
		String text = "{ \"Name\" : \"welcome to {{country}} and {{default.var2}} and {{location.address}}\" }";

		String[] vars = findVariables(text);
		for(String v: vars) {
			System.out.println(v);
		}
		//String v1 = (String) getVariableValue("default","country");
		String t1 = substituteVariables(text);
		System.out.println(t1);
		
	
	}
	public static void testJCache() {

		 //resolve a cache manager
		 CachingProvider cachingProvider = Caching.getCachingProvider();
		 CacheManager cacheManager = cachingProvider.getCacheManager();

		 //configure the cache
		 MutableConfiguration<String, Object> config =
		    new MutableConfiguration<String, Object>()
		    .setTypes( String.class, Object.class)
		    .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_HOUR))
		    .setStatisticsEnabled(true);

		 //create the cache
		 Cache<String, Object> cache = cacheManager.createCache("simpleCache", config);

		 //cache operations
		 String key = "key";
		 Integer value1 = 1;
		 cache.put("key", value1);
		// Integer value2 = (Integer) cache.get(key);
		 cache.remove(key);
		 
	}

}
