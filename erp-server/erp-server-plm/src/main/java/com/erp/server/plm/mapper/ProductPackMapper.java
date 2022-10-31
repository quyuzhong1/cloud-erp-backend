package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.ProductPack
 */
@Mapper
public interface ProductPackMapper extends BaseMapper<ProductPackEntity> {
    /**
     * @Description 产品包装信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 15:43
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductPackShowDTO> list(@Param("productId") String productId);
}




