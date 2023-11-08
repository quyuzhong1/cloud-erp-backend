package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 运费模板 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Mapper
public interface ShippingTemplateMapper extends BaseMapper<ShippingTemplateEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/6 16:22
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ShippingTemplateDTO.ListDTO> paging(Page query, @Param("params") ShippingTemplateDTO.PagingParamDTO params);
    /**
     * @description: tab页查询
     * @author Will
     * @date: 2023/11/6 16:48
     * @param permissionSql
     * @return List<TabDTO>
     */
    List<ShippingTemplateDTO.TabListDTO> tabList(@Param("permissionSql")String permissionSql);
    /**
     * @description: 查询导出数据
     * @author Will
     * @date: 2023/11/8 15:53
     * @param params
     * @return List<ListDTO>
     */
    List<ShippingTemplateDTO.ListDTO> listByExportExcel(@Param("params") ShippingTemplateDTO.ExportExcelParamDTO params);
}
