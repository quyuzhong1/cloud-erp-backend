package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 海外物流商 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface OverseasProviderMapper extends BaseMapper<OverseasProviderEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/11/21 17:27
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.ListDTO>
     **/
    IPage<OverseasProviderDTO.ListDTO> paging(Page query, @Param("params") OverseasProviderDTO.PagingParamDTO params);

    /**
     * 查询携带仓库信息
     * @Author Jim
     * @Date 2023/11/29
     */
    List<OverseasProviderDTO.ListWithWarehouseDTO> selectListWithWarehouse(Boolean notEmptyWarehouseId);

    OverseasProviderDTO.FeignDTO getOverseasWarehouse(@Param("params") OverseasProviderDTO.FeignDTO params);

    IPage<SkuMappingDTO.SyncWarehouseProductView> pageWarehouseProduct(Page query, @Param("params") AdvanceQueryContainer params);
}
