package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductPurchaseMapper extends BaseMapper<ProductPurchaseEntity> {
    /**
    * @Description 产品采购信息查询列表
    * @Author Luo_WG
    * @Date 2022/9/23 11:44
    * @param productId:产品信息表id
    * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
    **/
    List<ProductPurchaseShowDTO> list(@Param("productId") String productId);
}




