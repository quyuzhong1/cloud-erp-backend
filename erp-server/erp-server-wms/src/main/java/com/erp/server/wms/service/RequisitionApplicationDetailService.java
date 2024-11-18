package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;

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
    Boolean updateTransferWarehouse(String fromWarehouseId, String fromWarehouseName, String fromVirtualWarehouseId, String fromVirtualWarehouseName, String toWarehouseId, String toWarehouseName, Integer approveQty, String id);


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
    /**
     * 根据主表id清空虚拟仓库
     * @author will
     * @date 2024/7/2 11:45
     * @param mainId
     */
    void cleanVirtualWarehouseIdByMianId(String mainId);
    /**
     * 查询要货申请数据
     * @author will
     * @date 2024/9/26 16:53
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualRequisitionApplicationDetail();

    /**
     * 根据来源明细id获取明细记录
     * @param sourceDetailIds
     * @return
     */
    List<RequisitionApplicationDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds);
}
