package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;

import java.util.List;

/**
 * <p>
 * 采购退货单明细 服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
public interface PurchaseReturnOrderDetailService extends SuperService<PurchaseReturnOrderDetailEntity> {
    /**
     * @description: 根据来源明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:54
     * @param sourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     */
    List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     * @param dto dto
     * @param id id:主表id
     * @return java.lang.Boolean
     **/
    Boolean add(PurchaseReturnOrderDTO.AddDTO dto, String id);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * 根据主表id删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> mainIds);

    /**
     * 根据主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainId mainId
     * @return java.lang.Boolean
     **/
    List<PurchaseReturnOrderDetailEntity> getDetailByMainId(String mainId);

    List<PurchaseReturnOrderDetailEntity> listWarehouseReceiveByPodIds(List<String> purchaseDetailIds);
}
