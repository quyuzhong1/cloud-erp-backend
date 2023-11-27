package com.erp.server.wms.service;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.entity.RequisitionApplicationEntity;

import java.util.List;

/**
 * <p>
 * 要货申请单明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface RequisitionApplicationDetailService extends SuperService<RequisitionApplicationDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @param mainId
    * @return
    */
    void add(RequisitionApplicationDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @param mainId
    * @return
    */
    void update(RequisitionApplicationDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/11/22 19:52
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.RequisitionApplicationDetailEntity>
     **/
    List<RequisitionApplicationDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id查询详情信息
     * @param fromWarehouseId
     * @param fromWarehouseName
     * @param toWarehouseId
     * @param toWarehouseName
     * @param approveQty
     * @param id
     * @return java.lang.Boolean
     */
    Boolean updateTransferWarehouse(String fromWarehouseId, String fromWarehouseName, String toWarehouseId, String toWarehouseName, Integer approveQty, String id);


    /**
     * 根据主表id查询详情信息
     * @param pickingQty
     * @param id
     * @return java.lang.Boolean
     */
    Boolean updateFinishDetailPickingQty(Integer pickingQty, String id);

    /**
     * 根据主表id删除明细信息
     * @Author Luo_WG
     * @Date 2023/11/27 9:25
     * @param mainIds
     * @return void
     **/
    Boolean removeByMainIds(List<String> mainIds);
}
