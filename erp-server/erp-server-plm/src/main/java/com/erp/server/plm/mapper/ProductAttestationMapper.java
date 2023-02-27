package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductAttestationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品认证信息表(ProductAttestation)表数据库访问层
 *
 * @author yl
 * @since 2023-02-25 12:56:15
 */
@Mapper
public interface ProductAttestationMapper  extends BaseMapper<ProductAttestationEntity> {


    List<ProductAttestationEntity> getByProductId(@Param("productId") String productId);
}

