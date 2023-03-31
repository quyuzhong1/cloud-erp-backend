package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.dto.excel.PurchaseChangeExportExcelDTO;
import com.erp.model.scm.entity.PurchaseChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售需求明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseChangeMapper extends BaseMapper<PurchaseChangeEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/31 11:18
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseChangeDTO.ListDTO> paging(Page query,@Param("params") PurchaseChangeDTO.SearchParamDTO params);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/3/31 16:09
     * @param params
     * @return List<PurchaseChangeExportExcelDTO>
     */
    List<PurchaseChangeExportExcelDTO> listExportExcel(@Param("params") PurchaseChangeDTO.SearchParamDTO params);
}
