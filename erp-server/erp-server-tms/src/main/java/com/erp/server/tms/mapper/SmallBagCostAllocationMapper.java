package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


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
	
	/**
	 * 统计当前分页条件下的小包费用分摊明细总数。
	 *
	 * @param params 分页查询参数，包含动态查询条件、权限 SQL 和排序条件
	 * @return 明细主键去重后的总数
	 */
	Long pagingCount(@Param("params") PagingParamDTO params);

	/**
	 * 按当前查询条件获取分页范围内的明细主键列表。
	 *
	 * @param params 分页查询参数，包含动态查询条件、权限 SQL 和排序条件
	 * @param offset 分页偏移量
	 * @param pageSize 单页记录数
	 * @return 按展示排序得到的明细主键列表
	 */
	List<String> pagingDetailIds(@Param("params") PagingParamDTO params,
	                              @Param("offset") long offset,
	                              @Param("pageSize") long pageSize);

	/**
	 * 根据分页明细主键批量查询展示数据。
	 *
	 * @param detailIds 分页命中的明细主键列表
	 * @return 明细展示数据列表
	 */
	List<ListDTO> selectByDetailIds(@Param("detailIds") List<String> detailIds);
	
	List<SmallBagCostAllocationDTO.TabListDTO> tabList(@Param("params") com.common.business.dto.base.PermissionsDTO params);

    List<SmallBagCostAllocationEntity> listByReportPeriodStr(@Param("reportPeriodStr") String reportPeriodStr, @Param("reportStatus") String reportStatus);
    /**
     * 查询小包费用分摊
     * @author will
     * @date 2025/12/10 14:58
     * @param paramDTO
     * @return List<SmallBagCostDTO>
     */
    List<SmallBagCostAllocationDTO.SmallBagCostDTO> listSmallBagCost(@Param("param")SmallBagCostAllocationDTO.SmallBagCostParamDTO paramDTO);
}
