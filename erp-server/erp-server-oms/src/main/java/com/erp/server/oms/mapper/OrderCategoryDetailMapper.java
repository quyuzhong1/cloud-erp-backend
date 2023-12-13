package com.erp.server.oms.mapper;

import com.erp.model.oms.dto.OrderCategoryDetailDTO;
import com.erp.model.oms.entity.OrderCategoryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Mapper
public interface OrderCategoryDetailMapper extends BaseMapper<OrderCategoryDetailEntity> {

    List<OrderCategoryDetailDTO.ListDTO> listOrderCategory();
}
