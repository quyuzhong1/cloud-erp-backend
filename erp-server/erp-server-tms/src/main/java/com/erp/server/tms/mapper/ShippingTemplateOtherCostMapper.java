package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Mapper
public interface ShippingTemplateOtherCostMapper extends BaseMapper<ShippingTemplateOtherCostEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/10 17:44
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ShippingCalculationDTO.ListDTO> paging(Page query, @Param("params") ShippingCalculationDTO.PagingParamDTO params);
    /**
     * @description: 导出查询
     * @author Will
     * @date: 2023/11/10 17:45
     * @param params
     * @return List<ListDTO>
     */
    List<ShippingCalculationDTO.ListDTO> listByExportExcel( @Param("params")ShippingCalculationDTO.PagingParamDTO params);
    Page<ShippingCalculationDTO.ListDTO> listByExportExcel(@Param("page") Page<ShippingCalculationDTO.ListDTO> page, @Param("params")ShippingCalculationDTO.PagingParamDTO params);

    List<ShippingCalculationDTO.ListDTO> listRefCost(@Param("params")SoB2cDTO.ShippingCalculationDTO params);
}
