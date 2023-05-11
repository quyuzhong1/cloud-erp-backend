package com.erp.server.wms.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductInfoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 产品信息表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProductInfoMapper extends BaseMapper<ProductInfoEntity> {

    /**
     * 获取所有产品信息包括删除，用来同步到DMP
     * @Author Luo_WG
     * @Date 2023/4/19 16:22
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     **/
    List<ProductInfoEntity> getProductInfoAll();
}
