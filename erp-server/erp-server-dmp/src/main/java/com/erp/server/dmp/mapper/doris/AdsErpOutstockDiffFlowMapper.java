package com.erp.server.dmp.mapper.doris;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.model.dmp.entity.doris.AdsErpOutstockDiffFlowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 第三方仓出库单据差异表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-11-12
 */
@Mapper
public interface AdsErpOutstockDiffFlowMapper extends BaseMapper<AdsErpOutstockDiffFlowEntity> {
	IPage<AdsErpOutstockDiffFlowDTO.PagingDTO> paging(Page query, @Param("params") AdsErpOutstockDiffFlowDTO.PagingParamDTO params);
	
	AdsErpOutstockDiffFlowDTO.TotalDTO total(@Param("params") AdsErpOutstockDiffFlowDTO.PagingParamDTO params);
}
