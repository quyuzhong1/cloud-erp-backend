package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
}
