package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;

/**
 * <p>
 * 销量去噪信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleSalesDenoisingService extends SuperService<CfgRuleSalesDenoisingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleSalesDenoisingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleSalesDenoisingDTO.UpdateDTO dto);


}
