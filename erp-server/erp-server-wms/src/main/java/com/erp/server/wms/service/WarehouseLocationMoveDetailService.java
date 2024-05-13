package com.erp.server.wms.service;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 仓位移动明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
public interface WarehouseLocationMoveDetailService extends SuperService<WarehouseLocationMoveDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @param mainId
    * @return
    */
    void add(WarehouseLocationMoveDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @param mainId
    * @return
    */
    Boolean update(WarehouseLocationMoveDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主表id查询详情
     * @Author Luo_WG
     * @Date 2023/8/24 18:18
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity>
     **/
    List<WarehouseLocationMoveDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id删除详情
     * @Author Luo_WG
     * @Date 2023/8/24 18:18
     * @param mainId
     * @return java.lang.Boolean
     **/
     Boolean removeByMainId(String mainId);
}
