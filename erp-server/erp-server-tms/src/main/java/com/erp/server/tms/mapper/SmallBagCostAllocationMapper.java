package com.erp.server.tms.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;


/**
 * <p>
 * 小包费用分摊 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-12-02
 */
@Mapper
public interface SmallBagCostAllocationMapper extends BaseMapper<SmallBagCostAllocationEntity> {
	
	IPage<ListDTO> paging(Page query,@Param("params") PagingParamDTO params);
	
	List<SmallBagCostAllocationDTO.TabListDTO> tabList(@Param("params") com.common.business.dto.base.PermissionsDTO params);
	
}
