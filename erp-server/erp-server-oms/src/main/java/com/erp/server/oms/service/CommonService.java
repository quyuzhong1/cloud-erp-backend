package com.erp.server.oms.service;

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

    /**
     * @description: 获取当前人需要审核的业务ids
     * @author Will
     * @date: 2023/7/5 11:10
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);

    <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, String keyFieldName);
}
