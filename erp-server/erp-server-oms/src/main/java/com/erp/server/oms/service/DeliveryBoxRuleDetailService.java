package com.erp.server.oms.service;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleDetailEntity;
import com.common.business.dto.base.*;

import java.util.List;

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

    /**
     * 新增明细
     */
    Boolean save(List<DeliveryBoxRuleDetailDTO.AddDTO> deliveryBoxRuleDetailDTOList, String deliveryBoxRuleId);

    /**
     * 批量作废明细
     * @param entity
     * @param remark
     * @return
     */
    BatchResultDTO invalid(DeliveryBoxRuleDetailEntity entity,String remark);


}
