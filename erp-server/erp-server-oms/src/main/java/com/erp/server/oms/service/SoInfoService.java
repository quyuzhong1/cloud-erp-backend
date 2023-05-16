package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;

/**
 * <p>
 * 销售订单信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoInfoService extends SuperService<SoInfoEntity> {

    /**
     * 添加销售订单
     * @author yl
     * @date 2023-05-15 16:28
     * @param dto
     * @return java.lang.String
     */
    String add(SoInfoDTO.AddDTO dto);
}
