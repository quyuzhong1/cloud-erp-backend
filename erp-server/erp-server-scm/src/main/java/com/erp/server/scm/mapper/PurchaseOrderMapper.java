package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.excel.PurchaseOrderExportExcelDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购订单表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrderEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/27 12:33
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseOrderDTO.ListDTO> paging(Page query,@Param("params") PurchaseOrderDTO.SearchParamDTO params);
    /**
     * @description: 导出查询数据
     * @author Will
     * @date: 2023/3/27 16:01
     * @param params
     * @return List<PurchaseOrderExportExcelDTO>
     */
    List<PurchaseOrderExportExcelDTO> listExportExcel(@Param("params") PurchaseOrderDTO.SearchParamDTO params);
    /**
     * @description: 查询列表数量
     * @author Will
     * @date: 2023/3/29 10:11
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") PurchaseOrderDTO.SearchParamDTO params);
    /**
     * @description: 查询采购订单列表
     * @author Will
     * @date: 2023/4/17 9:35
     * @param params
     * @return List<DropDownListDTO>
     */
    List<PurchaseOrderDTO.DropDownListDTO> listPurchaseOrder(@Param("params") PermissionsDTO params);
}
