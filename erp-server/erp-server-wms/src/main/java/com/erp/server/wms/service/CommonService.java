package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.common.business.vo.LoginUser;
import com.common.core.entity.BaseEntity;
import com.erp.model.scm.entity.SupplierEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yl
 * @Classname CommonService

 * @Date 2023-03-15 11:50
 * @Created by yl
 */
public interface CommonService {

	
    /**
     * 获取用户信息
     * @author yl
     * @date 2023-03-15 11:58
     * @param
     * @return com.common.business.vo.LoginUser
     */
    public LoginUser getUserInfo();

    /**
     * @description: 获取当前审核人
     * @author Will
     * @date: 2023/8/2 16:37
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);

    /**
     * @description: 获取供应商信息
     * @author Will
     * @date: 2023/8/2 16:37
     * @param
     * @return SupplierEntity
     */
    SupplierEntity getSupplierEntity();

    @Transactional(rollbackFor = Exception.class)
    <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, String keyFieldName);
}
