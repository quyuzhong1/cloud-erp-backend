package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 头程费用分摊 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Mapper
public interface FirstMileCostAllocationMapper extends BaseMapper<FirstMileCostAllocationEntity> {

    /**
     * 分页统计
     * @param permissionSql
     * @return
     */
    List<FirstMileCostAllocationDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<FirstMileCostAllocationDTO.PagingVO> paging(@Param("query") Page<FirstMileCostAllocationDTO.PagingVO> query, @Param("params") FirstMileCostAllocationDTO.PagingParamDTO params);

    /**
     * 导出查询
     * @param params
     * @return
     */
    List<FirstMileCostAllocationDTO.PagingVO> exportList(@Param("params") FirstMileCostAllocationDTO.PagingParamDTO params);

    /**
     * 根据发货单查询所有账期记录
     * @param sourceCodes
     * @return
     */
    List<FirstMileCostAllocationDTO.PagingVO> listSkuBySourceCodes(@Param("sourceCodes") List<String> sourceCodes);
    /**
     * 根据发货单查询所有账期记录
     * @param sourceIds
     * @return
     */
    List<FirstMileCostAllocationDTO.PagingVO> listBySourceIds(@Param("sourceIds") List<String> sourceIds);

    /**
     * 查询物流单最新的核算月份
     * @param logisticsBillIds 物流单ID
     * @return
     * @date: 2024-08-26
     * @author: tanmujin
     */
    List<FirstMileCostAllocationDTO.LastedAllocMonthDTO> listLastedAllocationMonth(@Param("logisticsBillIds") List<String> logisticsBillIds);

    List<FirstMileCostAllocationEntity> listBySourceIdsAndReportPeriodId(@Param("sourceIds") List<String> sourceIds,@Param("reportPeriodId")  String reportPeriodId);
}
