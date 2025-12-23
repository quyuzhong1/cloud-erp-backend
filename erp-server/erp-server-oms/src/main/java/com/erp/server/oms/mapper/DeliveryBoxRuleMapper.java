package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
@Mapper
public interface DeliveryBoxRuleMapper extends BaseMapper<DeliveryBoxRuleEntity> {

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<DeliveryBoxRuleDTO.ListDTO> paging(Page query, @Param("params") DeliveryBoxRuleDTO.PagingParamDTO params);
}
