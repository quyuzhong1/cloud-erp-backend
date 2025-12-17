package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;

import java.util.List;

/**
 * <p>
 * 产品采购变更价 明细表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
public interface PurchasePriceChangeDetailService extends SuperService<PurchasePriceChangeDetailEntity> {


    /**
     * 根据变更表id 获取明细
     *
     * @param id
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-28 14:35
     */
    List<PurchasePriceChangeDetailDTO.ViewDTO> getByPriceChangeId(String id);

    /**
     * 添加明细
     * @author yl
     * @date 2023-03-28 16:05
     * @param id
     * @param purchasePriceChangeDetailList
     * @return void
     */
    void addPriceChangeDetail(String id, List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList);

    /**
     * 审核通过后 需要修改采购价目详情表的数据
     * @author yl
     * @date 2023-03-28 19:06
     * @param purchasePriceChangeList
     * @return void
     */
    void updatePurchasePriceDetail(List<PurchasePriceChangeEntity> purchasePriceChangeList);

    
    /**
     * 修改变更价目详情信息
     * @author yl
     * @date 2023-03-29 9:20
     * @param id
     * @param purchasePriceChangeDetailList
     * @return void
     */
    void updatePriceChangeDetail(String id, List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceChangeDetailList);

    
    /**
     * 根据供应商id获取到已变更区间数据
     * @author yl
     * @date 2023-04-06 10:01
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     */
    List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(String supplierId);
    /**
     * @description:
     * @author Will
     * @date: 2023/4/24 20:02
     * @param purchasePriceChangeId
     * @return List<PurchasePriceChangeDetailEntity>
     */
    List<PurchasePriceChangeDetailEntity> listByPurchasePriceChangeId(String purchasePriceChangeId);

    /**
     * 根据采购价目详情表id查询变更详情
     * @Author Luo_WG
     * @Date 2024/1/9 15:00
     * @param purchasePriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.PurchasePriceChangeDetailEntity>
     **/
    List<PurchasePriceChangeDetailEntity> listByPurchasePriceDetailIds(List<String> purchasePriceDetailIds);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/9/22 15:09
     * @param ids
     * @param remark
     */
    void updateDetailRemark(List<String> ids, String remark);

    /**
     * 根据主表id集合获取对应明细
     * @author yl
     * @date 2023-10-24 10:09
     * @param mainIdList
     * @return java.util.List<com.erp.model.scm.entity.PurchasePriceChangeDetailEntity>
     */
    List<PurchasePriceChangeDetailEntity> listByMainIdList(List<String> mainIdList);

    /**
     * 获取最新的变更明细
     * @author will
     * @date 2025/8/5 17:57
     * @param purchasePriceDetailId
     * @param id
     * @return PurchasePriceChangeDetailEntity
     */
    PurchasePriceChangeDetailEntity getLatest(String purchasePriceDetailId, String id);
}
