package com.erp.server.mrp.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;

/**
 * <p>
 * 仓库（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-24
 */
public interface CfgRuleWarehouseService extends SuperService<CfgRuleWarehouseEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-24
    * @param dto
    * @return
    */
    Boolean update(CfgRuleWarehouseDTO.UpdateDTO dto);

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 15:48
     * @param platformType
     * @return ViewDTO
     */
    CfgRuleWarehouseDTO.ViewDTO view(String platformType);
    /**
     * 平台类型
     * @author will
     * @date 2024/8/26 9:38
     * @param platformType
     * @return CfgRuleWarehouseEntity
     */
    CfgRuleWarehouseEntity getByPlatformType(String platformType);
}
