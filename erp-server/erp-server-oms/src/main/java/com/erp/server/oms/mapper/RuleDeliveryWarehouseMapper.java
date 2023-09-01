package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 发货仓库规则表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Mapper
public interface RuleDeliveryWarehouseMapper extends BaseMapper<RuleDeliveryWarehouseEntity> {

    /**
     * 仓库规则分页
     * @param query
     * @param params
     * @return
     */
    IPage<RuleDeliveryWarehouseDTO.PagingViewDTO> paging(Page query, @Param("params") RuleDeliveryWarehouseDTO.PagingParamDTO params);
}
