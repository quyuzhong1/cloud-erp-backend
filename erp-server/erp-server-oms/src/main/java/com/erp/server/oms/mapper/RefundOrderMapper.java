package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.model.oms.entity.RefundOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 退款订单 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrderEntity> {
    /**
     * 退款订单
     * @author yl
     * @date 2023-08-25 14:12
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     */
    IPage<RefundOrderDTO.PagingViewDTO> paging(Page query, @Param("params")RefundOrderDTO.PagingParamDTO params);
}
