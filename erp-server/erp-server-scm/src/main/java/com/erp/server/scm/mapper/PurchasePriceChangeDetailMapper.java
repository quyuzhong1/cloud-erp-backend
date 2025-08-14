package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品采购变更价 明细表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Mapper
public interface PurchasePriceChangeDetailMapper extends BaseMapper<PurchasePriceChangeDetailEntity> {



    /**
     * 根据供应商id 获取到对应明细
     * @author yl
     * @date 2023-04-06 9:52
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     */
    List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(@Param("supplierId") String supplierId,@Param("statusList") List<String> statusList );
    /**
     * @description:
     * @author Will
     * @date: 2023/4/24 20:05
     * @param purchasePriceChangeId
     * @return List<PurchasePriceChangeDetailEntity>
     */
    List<PurchasePriceChangeDetailEntity> listByPurchasePriceChangeId(@Param("purchasePriceChangeId") String purchasePriceChangeId);

    /**
     * 根据采购价目详情id查询变更详情
     * @Author Luo_WG
     * @Date 2024/1/9 15:04
     * @param purchasePriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.PurchasePriceChangeDetailEntity>
     **/
    List<PurchasePriceChangeDetailEntity> listByPurchasePriceDetailIds(@Param("purchasePriceDetailIds") List<String> purchasePriceDetailIds);
    /**
     * 查询需要验证的调价数据
     * @author will
     * @date 2025/5/7 09:14
     * @param supplierIdList
     * @param statusList
     * @param purchaseOrgId
     * @param skuIdList
     * @return List<PurchasePriceChangeDetailEntity>
     */
    List<PurchasePriceChangeDetailEntity> listCheckPurchasePriceChangeDetail(@Param("supplierIdList") List<String> supplierIdList,@Param("statusList") List<String> statusList,@Param("purchaseOrgId") String purchaseOrgId,@Param("skuIdList") List<String> skuIdList);
    /**
     * 获取最新的采购价变更明细
     * @author will
     * @date 2025/8/5 17:58
     * @param purchasePriceDetailId
     * @param id
     * @return PurchasePriceChangeDetailEntity
     */
    PurchasePriceChangeDetailEntity getLatest(@Param("purchasePriceDetailId")String purchasePriceDetailId,@Param("id") String id);
}
