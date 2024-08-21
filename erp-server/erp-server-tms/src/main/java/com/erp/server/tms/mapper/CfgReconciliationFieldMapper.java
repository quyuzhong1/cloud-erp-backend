package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportDTO;
import com.erp.model.tms.entity.CfgReconciliationFieldEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 对账字段配置表 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Mapper
public interface CfgReconciliationFieldMapper extends BaseMapper<CfgReconciliationFieldEntity> {

    /**
     * 列表查詢
     *
     * @param query  query
     * @param params 参数
     */
    IPage<CfgReconciliationFieldDTO.PagingVO> paging(Page<?> query, @Param("params") CfgReconciliationFieldDTO.PagingParamDTO params);

    /**
     * 导出列表查詢
     *
     * @param params 参数
     */
    List<CfgReconciliationFieldExportDTO> listExportExcel(@Param("params") CfgReconciliationFieldDTO.PagingParamDTO params);
    Page<CfgReconciliationFieldExportDTO> listExportExcel(@Param("page") Page<CfgReconciliationFieldExportDTO> page, @Param("params") CfgReconciliationFieldDTO.PagingParamDTO params);
}
