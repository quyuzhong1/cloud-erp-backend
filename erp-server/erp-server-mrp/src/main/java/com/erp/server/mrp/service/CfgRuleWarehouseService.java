package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;

import java.util.List;

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
    /**
     * 刷新虚拟仓数据
     * @author will
     * @date 2024/9/3 17:28
     * @param platformType
     */
    void refreshVirtual(String platformType);
    /**
     * 查询是否启用海外仓
     * @author will
     * @date 2024/9/4 11:12
     * @param platformType
     * @return Boolean
     */
    Boolean getIsEnableOverseas(String platformType);
    /**
     * 校验店铺
     * @author will
     * @date 2024/9/13 15:44
     * @param dto
     * @return WarehouseShopDTO
     */
    CfgRuleWarehouseDTO.WarehouseShopDTO checkShop(CfgRuleWarehouseDTO.UpdateDTO dto);
    /**
     * 查询海外仓设置
     * @author will
     * @date 2024/10/29 11:27
     * @param platformType
     * @return List<OverseasWarehouseDTO>
     */
    List<CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO> listOverseasWarehouse(String platformType);
}
