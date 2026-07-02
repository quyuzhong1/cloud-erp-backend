package com.erp.server.dmp.push.service;

import com.common.business.service.SuperService;
import com.common.core.entity.BaseEntity;

import java.util.List;
import java.util.Map;

public interface CommonService {

    Map<String, Object> makeApiFieldMap(Map<String,Object> map, String apiPlatformId, Integer moduleType);

    List<Map<String, Object>> makeApiFieldList(List<Map<String, Object>> list, String apiPlatformId, Integer moduleType);

    <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, List<String> keyFieldNames);
}
