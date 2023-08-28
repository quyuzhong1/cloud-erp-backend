package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 订单审核规则 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Mapper
public interface RuleOrderApprovalMapper extends BaseMapper<RuleOrderApprovalEntity> {

    /**
     * 审核订单分页
     * @param query
     * @param params
     * @return
     */
    IPage<RuleOrderApprovalDTO.PagingViewDTO> paging(Page query, @Param("params") RuleOrderApprovalDTO.PagingParamDTO params);
}
