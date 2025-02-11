package com.erp.model.dmp.constant;

import java.util.HashMap;
import java.util.Map;

public class DmpOutputConstant {
	private DmpOutputConstant(){
		
	}
	
	public static final String IS_QUERY_SYNC = "isQuerySync";
	
	private static final Map<String, Object> QUERY_SYNC_MAP = new HashMap<>();
	
	static {
		QUERY_SYNC_MAP.put(IS_QUERY_SYNC, true);
	}
	
	public static Map<String, Object> getQuerySyncMap() {
		return QUERY_SYNC_MAP;
	}
}
