package com.erp.server.oms.service;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleDetailEntity;
import com.common.business.dto.base.*;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
public interface DeliveryBoxRuleDetailService extends SuperService<DeliveryBoxRuleDetailEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryBoxRuleDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    Boolean update(DeliveryBoxRuleDetailDTO.UpdateDTO dto);


}
