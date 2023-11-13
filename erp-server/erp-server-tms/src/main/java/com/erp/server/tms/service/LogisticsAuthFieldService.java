package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsAuthFieldDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流授权字段值表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsAuthFieldService extends SuperService<LogisticsAuthFieldEntity> {





    /**
     * 保存或者修改授权字段
     *@parms id fieldMap
     *@return
     *@author yl
     *@date 2023-11-09
     */
    void saveOrUpdateAuthField(String authId, Map<String, String> fieldMap);

    /**
     *根据授权id 获取到对应字段
     *@parms authId
     *@return 
     *@author yl
     *@date 2023-11-13
     */
    List<LogisticsAuthFieldEntity> listByLogisticsAuthId(String authId);
}
