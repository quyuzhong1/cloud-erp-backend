package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SupplierReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname: SupplierReportMapper
 * @Description: 供应商报表mapper接口
 * @CreateTime: 2023-06-16  16:17
 * @Author: zhangchunlin
 */
@Mapper
public interface SupplierReportMapper {

    /**
     * 分页查询供应商采购信息
     * @param query
     * @param param
     * @return
     */
    IPage<SupplierReportDTO.PagingViewDTO> getPurchasePaging(Page query, @Param("params") SupplierReportDTO.PagingSearchParamDTO param);

    /**
     * 导出查询
     * @param param
     * @return
     */
    List<SupplierReportDTO.PagingViewDTO> exportList(@Param("params") SupplierReportDTO.ExportSearchParamDTO param);
    Page<SupplierReportDTO.PagingViewDTO> exportList(@Param("page") Page<SupplierReportDTO.PagingViewDTO> page, @Param("params") SupplierReportDTO.ExportSearchParamDTO param);

}
