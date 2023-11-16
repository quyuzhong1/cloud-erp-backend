package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasDeliveryPlanDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO;

/**
 * <p>
 * 发货计划详情表 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface OverseasDeliveryPlanDetailService extends SuperService<OverseasDeliveryPlanDetailEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasDeliveryPlanDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasDeliveryPlanDetailDTO.UpdateDTO dto);


}
