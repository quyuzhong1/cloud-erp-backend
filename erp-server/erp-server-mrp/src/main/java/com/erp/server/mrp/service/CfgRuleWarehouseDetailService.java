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
     * 根据主表id集合查询详情数据
     * @author will
     * @date 2024/8/24 16:42
     * @param mainIdList
     * @return List<ViewDTO>
     */
    List<CfgRuleWarehouseDetailDTO.ViewDTO> listViewByMainIdList(List<String> mainIdList);
}
