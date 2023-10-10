package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.OrderCategoryDTO;
import com.erp.model.oms.entity.OrderCategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单分类表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Mapper
public interface OrderCategoryMapper extends BaseMapper<OrderCategoryEntity> {

    /**
     * 订单分类分页
     * @author yl
     * @date 2023-08-25 15:37
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.OrderCategoryDTO.PagingViewDTO>
     */
    IPage<OrderCategoryDTO.PagingViewDTO> paging(Page query, @Param("params") OrderCategoryDTO.PagingParamDTO params);

    /**
     * 获取明细
     * @return
     */
    List<OrderCategoryDTO.ListDTO> listDetail();
}
