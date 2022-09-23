package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.entity.ProductCertificateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.ProductCertificate
 */
@Mapper
public interface ProductCertificateMapper extends BaseMapper<ProductCertificateEntity> {
    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 15:43
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductCertificateShowDTO> list(@Param("productId") String productId);
}




