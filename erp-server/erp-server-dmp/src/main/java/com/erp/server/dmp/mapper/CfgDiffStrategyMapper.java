package com.erp.server.dmp.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.model.dmp.entity.CfgDiffStrategyEntity;


/**
 * <p>
 * 差异策略配置基础信息 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
@Mapper
public interface CfgDiffStrategyMapper extends BaseMapper<CfgDiffStrategyEntity> {

	IPage<CfgDiffStrategyDTO.ViewDTO> paging(Page query, @Param("params") CfgDiffStrategyDTO.PagingParamDTO params);
}
