package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.common.core.entity.BaseEntity;

import java.util.List;

public interface CommonService {

    <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, List<String> keyFieldNames);
}