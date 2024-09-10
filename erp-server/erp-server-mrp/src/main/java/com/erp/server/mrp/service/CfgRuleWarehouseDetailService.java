package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;

import java.util.List;

/**
 * <p>
 * 仓库（规则设置）明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-24
 */
public interface CfgRuleWarehouseDetailService extends SuperService<CfgRuleWarehouseDetailEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-24
    * @param cfgLocalWarehouseList
    * @return
    */
    Boolean update(List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalWarehouseList,String mainId,String type);

    /**
     * 根据主表id集合查询
     * @author will
     * @date 2024/8/24 15:54
     * @param mainIdList
     * @return List<CfgRuleWarehouseDetailEntity>
     */
    List<CfgRuleWarehouseDetailEntity> listByMainIdList(List<String> mainIdList);

    /**
     * 根据主表id集合和类型查询
     * @author will
     * @date 2024/8/24 15:54
     * @param mainIdList
     * @return List<CfgRuleWarehouseDetailEntity>
     */
    List<CfgRuleWarehouseDetailEntity> listByWarehouseType(Boolean isVirtual,List<String> mainIdList,String type);
    /**
     * 根据主表id集合查询详情数据
     * @author will
     * @date 2024/8/24 16:42
     * @param mainIdList
     * @return List<ViewDTO>
     */
    List<CfgRuleWarehouseDetailDTO.ViewDTO> listViewByMainIdList(List<String> mainIdList);
    /**
     * 根据仓库、虚拟仓库、平台、类型查询
     * @author will
     * @date 2024/9/10 18:56
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @param dictPlatformList
     * @param type
     * @return List<CfgRuleWarehouseDetailEntity>
     */
    List<CfgRuleWarehouseDetailEntity> listRefreshVirtual(List<String> warehouseIdList, List<String> virtualWarehouseIdList, List<String> dictPlatformList, String type);
}
