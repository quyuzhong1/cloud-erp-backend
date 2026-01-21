package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.common.core.entity.BaseEntity;

import java.util.List;

/**
 * @author Lambda
 * @Classname CommonService

 * @Date 2023-05-11 19:21
 * @Created by yl
 */
public interface CommonService {


    <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, List<String> keyFieldNames);
}
