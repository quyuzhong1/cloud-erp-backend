package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseOrderDetailMapper extends BaseMapper<PurchaseOrderDetailEntity> {

    /**
     * @description: 添加产品数据显示
     * @author Will
     * @date: 2023/4/14 10:33
     * @param params
     * @return ViewProductDTO
     */
    List<PurchaseOrderDetailDTO.ViewProductDTO> viewProduct(@Param("params") PurchaseOrderDetailDTO.ProductSearchParamDTO params);
}
