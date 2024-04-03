package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 自发货费用 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
@Mapper
public interface LogisticsBillCostMapper extends BaseMapper<LogisticsBillCostEntity> {
    /**
     * @description: tab列表
     * @author Will
     * @date: 2023/11/13 15:56
     * @param permissionSql
     * @return List<TabListDTO>
     */
    List<LogisticsBillCostDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/13 16:02
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<LogisticsBillCostDTO.ListDTO> paging(Page query,@Param("params") LogisticsBillCostDTO.PagingParamDTO params);
    /**
     * @description: 导出查询
     * @author Will
     * @date: 2023/11/13 16:23
     * @param params
     * @return List<ListDTO>
     */
    List<LogisticsBillCostDTO.ListDTO> listByExportExcel(@Param("params") LogisticsBillCostDTO.ExportExcelParamDTO params);

    /**
     * 根据销售出库单 获取销售出库单自发货费用列表
     * @param ids
     * @return
     */
    List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(@Param("ids") List<String> ids);
}
