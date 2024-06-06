package com.erp.server.dmp.push.service;

import java.util.List;
import java.util.Map;

public interface CommonService {

    Map<String, Object> makeApiFieldMap(Map<String,Object> map, String apiPlatformId, Integer moduleType);

    List<Map<String, Object>> makeApiFieldList(List<Map<String, Object>> list, String apiPlatformId, Integer moduleType);
}
