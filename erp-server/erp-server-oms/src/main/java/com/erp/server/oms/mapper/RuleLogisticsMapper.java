package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.RuleLogisticsDTO;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 物流规则表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Mapper
public interface RuleLogisticsMapper extends BaseMapper<RuleLogisticsEntity> {

    /**
     * 物流规则分页
     * @param query
     * @param params
     * @return
     */
    IPage<RuleLogisticsDTO.PagingViewDTO> paging(Page query, @Param("params") RuleLogisticsDTO.PagingParamDTO params);
}
