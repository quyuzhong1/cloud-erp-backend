package com.erp.server.tms.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.ListDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;


/**
 * <p>
 * 中转费用分摊 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
@Mapper
public interface TransferDeclareCostAllocationMapper extends BaseMapper<TransferDeclareCostAllocationEntity> {
	IPage<ListDTO> paging(Page query,@Param("params") PagingParamDTO params);
}
