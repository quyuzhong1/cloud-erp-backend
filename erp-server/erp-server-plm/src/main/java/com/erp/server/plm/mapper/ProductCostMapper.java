package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductCostMapper extends BaseMapper<ProductCostEntity> {
    /**
     * @Description 产品成本信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductCostShowDTO> list(@Param("productId") String productId);
}
 