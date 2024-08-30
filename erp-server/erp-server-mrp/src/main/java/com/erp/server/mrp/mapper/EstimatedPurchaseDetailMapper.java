package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.EstimatedPurchaseDTO;
import com.erp.model.mrp.entity.EstimatedPurchaseDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.EstimatedPurchaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 预计采购明细 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface EstimatedPurchaseDetailMapper extends BaseMapper<EstimatedPurchaseDetailEntity> {

    Page<EstimatedPurchaseVO> estimatedPurchase(@Param("page") Page<EstimatedPurchaseVO> page,@Param("params") EstimatedPurchaseDTO params);
}
