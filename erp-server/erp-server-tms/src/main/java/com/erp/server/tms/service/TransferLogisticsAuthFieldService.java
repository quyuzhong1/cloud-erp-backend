package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferLogisticsAuthFieldDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流授权字段值表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferLogisticsAuthFieldService extends SuperService<TransferLogisticsAuthFieldEntity> {

    /**
     * 保存或者修改授权字段
     * @Author Luo_WG
     * @Date 2024/1/19 11:58
     * @param authId
     * @param fieldMap
     * @return void
     **/
    void saveOrUpdateAuthField(String authId, Map<String, String> fieldMap);

    /**
     * 根据授权id 获取到对应字段
     * @Author Luo_WG
     * @Date 2024/1/19 11:58
     * @param authId
     * @return java.util.List<com.erp.model.tms.entity.LogisticsAuthFieldEntity>
     **/
    List<TransferLogisticsAuthFieldEntity> listByLogisticsAuthId(String authId);

    /**
     * 根据授权id删除授权信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:58
     * @param authId
     **/
    void removeByAuthId(String authId);
}
