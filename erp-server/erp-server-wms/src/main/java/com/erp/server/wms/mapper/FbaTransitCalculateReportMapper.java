package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import com.erp.model.wms.entity.FbaTransitCalculateReportEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * FBA在途核算报表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
 */
@Mapper
public interface FbaTransitCalculateReportMapper extends BaseMapper<FbaTransitCalculateReportEntity> {
    /**
     * 获取上月存在期末在途数量的列表
     * @param lastReportMonth
     * @return
     */
    List<FbaTransitCalculateReportDTO.CalculateDTO> listByTransitAndReportMonth(@Param("lastReportMonth") LocalDate lastReportMonth);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<FbaTransitCalculateReportDTO.ListDTO> paging(@Param("query") Page query,@Param("params") FbaTransitCalculateReportDTO.PagingParamDTO params);
}
