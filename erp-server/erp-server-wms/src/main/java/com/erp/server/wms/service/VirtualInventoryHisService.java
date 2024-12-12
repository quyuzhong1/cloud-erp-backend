package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 虚拟仓库存历史信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-10
 */
public interface VirtualInventoryHisService extends SuperService<VirtualInventoryHisEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-10
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addOrUpdate(VirtualInventoryHisDTO.AddDTO dto);


    /**
     * 查询结余库存
     * @author will
     * @date 2024/12/11 10:04
     * @param skuIdList
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @return List<VirtualInventoryHisDTO.VirtualQtyDTO>
     */
    List<VirtualInventoryHisDTO.VirtualQtyDTO> listInventoryQty(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList,LocalDate localDate);
    /**
     * 添加虚拟仓每日库存结余
     * @author will
     * @date 2024/12/11 10:09
     */
    void addVirtualInventoryHis(LocalDate localDate);
}
