package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.entity.CfgRuleOrderHandleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 订单处理规则表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-05-09
 */
@Mapper
public interface CfgRuleOrderHandleMapper extends BaseMapper<CfgRuleOrderHandleEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/5/9 11:35
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<CfgRuleOrderHandleDTO.ListDTO> paging(Page query,@Param("params") CfgRuleOrderHandleDTO.PagingParamDTO params);
}
