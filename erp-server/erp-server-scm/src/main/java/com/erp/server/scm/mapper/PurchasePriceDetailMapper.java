package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品采购价格明细表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface PurchasePriceDetailMapper extends BaseMapper<PurchasePriceDetailEntity> {
    /**
     * @description: 根据供应商id和skuId查询有效单价
     * @author Will
     * @date: 2023/3/27 9:41
     * @param dto
     * @return PurchaseTaxPriceViewDTO
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(@Param("params") PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto);

    
    /**
     * 根据供应商id 获取到对应明细
     * @author yl
     * @date 2023-04-06 9:52
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     */
    List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(@Param("supplierId") String supplierId,@Param("statusList") List<String> statusList,@Param("purchasePriceId") String purchasePriceId);
}
