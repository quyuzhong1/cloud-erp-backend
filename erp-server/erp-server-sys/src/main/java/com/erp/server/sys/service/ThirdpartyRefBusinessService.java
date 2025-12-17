package com.erp.server.sys.service;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 第三方平台与业务对接关联表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-19
 */
public interface ThirdpartyRefBusinessService extends SuperService<ThirdpartyRefBusinessEntity> {



    List<ThirdpartyRefBusinessEntity> listByBusinessIds(List<String> idList , String businessType);

    /**
     * 根据业务id获取
     * @param businessId
     * @return
     */
    ThirdpartyRefBusinessEntity getByBusinessId(String businessId , String businessType);

    /**
     * 根据业务id删除
     * @param id
     */
    void removeByBusinessId(String businessId , String businessType);
}
