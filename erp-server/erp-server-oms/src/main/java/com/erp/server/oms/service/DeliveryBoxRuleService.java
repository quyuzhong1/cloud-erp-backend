package com.erp.server.oms.service;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.common.business.service.SuperService;
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
public interface DeliveryBoxRuleService extends SuperService<DeliveryBoxRuleEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryBoxRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    Boolean update(DeliveryBoxRuleDTO.UpdateDTO dto);


    /**
     * 删除
     * @author wtr
     * @date: 2025-11-24
     * @param dto
     * @return
     */
    List<BatchResultDTO> deleteByIds(BaseIdsDTO.IdsDTO dto);

    Boolean importFile(BaseDTO.ImportDTO dto);
}
