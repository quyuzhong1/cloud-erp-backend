package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProductPlanSearchDTO;
import com.erp.model.plm.entity.ProductPlanEntity;
import com.erp.model.plm.vo.ProductPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:50
 */
@Mapper
public interface ProductPlanMapper  extends BaseMapper<ProductPlanEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/2/21 17:06
     * @param query
     * @param params
     * @return IPage<ProductPlanVO>
     */
    IPage<ProductPlanVO> paging(Page query, @Param("params") ProductPlanSearchDTO params);
}
