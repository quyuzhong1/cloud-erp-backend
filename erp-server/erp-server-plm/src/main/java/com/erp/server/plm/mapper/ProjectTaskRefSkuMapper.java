package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductTaskRefSkuDTO;
import com.erp.model.plm.entity.ProjectTaskRefSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 14:51
 */
@Mapper
public interface ProjectTaskRefSkuMapper extends BaseMapper<ProjectTaskRefSkuEntity> {

    /**
     * @description: 查询关联的任务和sku名称
     * @author Will
     * @date: 2022/11/21 10:02
     * @param productId
     * @return List<ProjectTaskRefSkuDTO>
     */
    List<ProductTaskRefSkuDTO> getTaskRefSkuName(@Param("productId") String productId);
}
