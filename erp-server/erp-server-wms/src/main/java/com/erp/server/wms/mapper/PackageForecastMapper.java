package com.erp.server.wms.mapper;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 组包预报表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Mapper
public interface PackageForecastMapper extends BaseMapper<PackageForecastEntity> {

    /**
     * tab
     * @return
     */
    List<PackageForecastDTO.TabListDTO> tabList();

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<PackageForecastDTO.PagingViewDTO> paging(Page query, @Param("params") PackageForecastDTO.PagingParamDTO params);

    /**
     * 导出
     * @param dto
     * @return
     */
    List<PackageForecastDTO.ExportViewDTO> listExcel(@Param("params") PackageForecastDTO.ExportDTO dto);
    Page<PackageForecastDTO.ExportViewDTO> listExcel(@Param("page") Page<PackageForecastDTO.ExportViewDTO> page, @Param("params") PackageForecastDTO.ExportDTO dto);

    /**
     * 获取订单查询列表（速卖通订单更新使用）
     * @return
     */
    List<PackageForecastEntity> getAliExpressHandoverList(@Param("dateTime") DateTime dateTime);
}
