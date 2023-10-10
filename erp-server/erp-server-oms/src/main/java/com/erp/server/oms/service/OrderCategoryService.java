package com.erp.server.oms.service;

import com.common.business.dto.base.BaseChildDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.OrderCategoryDTO;
import com.erp.model.oms.entity.OrderCategoryEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 订单分类表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
public interface OrderCategoryService extends SuperService<OrderCategoryEntity> {

    /**
     * 添加订单分类
     * @author yl
     * @date 2023-08-25 14:53
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(OrderCategoryDTO.AddDTO dto);

    /**
     * 更改订单分类
     * @author yl
     * @date 2023-08-25 15:09
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateCategory(OrderCategoryDTO.UpdateDTO dto);

    /**
     * 订单分类分页
     * @author yl
     * @date 2023-08-25 15:33
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.OrderCategoryDTO.PagingViewDTO>
     */
    PagingVO<OrderCategoryDTO.PagingViewDTO> paging(PagingDTO<OrderCategoryDTO.PagingParamDTO> dto);

    
    /**
     *
     * 订单分类详情
     * @author yl
     * @date 2023-08-25 15:52
     * @param id
     * @return com.erp.model.oms.dto.OrderCategoryDTO.ViewDTO
     */
    OrderCategoryDTO.ViewDTO view(String id);

    /**
     * 更改订单分类
     * @author yl
     * @date 2023-08-25 16:01
     * @param orderCategory
     * @param disabled
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO updateStatus(OrderCategoryEntity orderCategory, Boolean disabled);

    /**
     * 订单分类的树结构
     * @return
     */
    List<BaseChildDTO.ListChildTreeDTO> tree();
}
