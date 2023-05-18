package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;

/**
 * <p>
 * 销售订单变更 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoChangeService extends SuperService<SoChangeEntity> {

    
    /**
     * 添加销售订单
     * @author yl
     * @date 2023-05-18 11:54
     * @param dto
     * @return java.lang.String
     */
    String add(SoChangeDTO.AddDTO dto);
}
