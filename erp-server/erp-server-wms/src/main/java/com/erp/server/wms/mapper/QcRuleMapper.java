package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.model.wms.entity.QcRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 质检规则 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Mapper
public interface QcRuleMapper extends BaseMapper<QcRuleEntity> {

    IPage<QcRuleDTO.PagingViewDTO> paging(Page query, @Param("params")   QcRuleDTO.PagingParamDTO params);
}
