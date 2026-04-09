package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.SkuBizStatisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.SkuBizStatisticsDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * sku业务统计表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
 */
@Mapper
public interface SkuBizStatisticsMapper extends BaseMapper<SkuBizStatisticsEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SkuBizStatisticsDTO.ListDTO> paging(Page query, @Param("params") SkuBizStatisticsDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SkuBizStatisticsDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SkuBizStatisticsDTO.ListDTO> listExport(@Param("params") SkuBizStatisticsDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SkuBizStatisticsDTO.TabListDTO> tabList(@Param("params") SkuBizStatisticsDTO.PagingParamDTO searchParam);
}
