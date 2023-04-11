package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-28
 */
@Mapper
public interface PurchasePriceHistoryMapper extends BaseMapper<PurchasePriceHistoryEntity> {

    /**
     * @description: 查询采购价目历史表报价
     * @author Will
     * @date: 2023/4/11 9:52
     * @param params
     * @return List<PurchaseTaxPriceViewDTO>
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getHistoryTaxPrice(@Param("params") PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO params);
}
