package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.excel.SalesDemandExportExcelDTO;
import com.erp.model.scm.entity.SalesDemandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售需求主表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface SalesDemandMapper extends BaseMapper<SalesDemandEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/17 14:16
     * @param query
     * @param params
     * @return IPage<listDTO>
     */
    IPage<SalesDemandDTO.ListDTO> paging(Page query,@Param("params") SalesDemandDTO.SearchParamDTO params);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/3/20 11:21
     * @param params
     * @return List<SalesDemandExportExcelDTO>
     */
    List<SalesDemandExportExcelDTO> listExportExcel(@Param("params")SalesDemandDTO.SearchParamDTO params);
    /**
     * @description: 列表数量
     * @author Will
     * @date: 2023/3/27 11:28
     * @param dto
     * @return Integer
     */
    Integer listCount(@Param("params") SalesDemandDTO.SearchParamDTO dto);
}
