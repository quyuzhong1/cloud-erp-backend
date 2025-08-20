package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgInventoryAgeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgInventoryAgeDTO;

/**
 * <p>
 * 库龄配置表 服务类
 * </p>
 *
 * @author will
 * @since 2025-08-20
 */
public interface CfgInventoryAgeService extends SuperService<CfgInventoryAgeEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgInventoryAgeDTO.AddDTO dto);

    /**
     * 根据人员查询配置或默认配置
     * @author will
     * @date 2025/8/20 09:59
     * @return CfgInventoryAgeEntity
     */
    CfgInventoryAgeEntity getByUserIdOrDefault();
    /**
     * 查看配置
     * @author will
     * @date 2025/8/20 10:08
     * @return ViewDTO
     */
    CfgInventoryAgeDTO.ViewDTO viewVirtual();
}
