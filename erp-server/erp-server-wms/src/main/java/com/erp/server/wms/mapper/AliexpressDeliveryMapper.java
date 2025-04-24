package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 速卖通发货单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
 */
@Mapper
public interface AliexpressDeliveryMapper extends BaseMapper<AliexpressDeliveryEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2024/1/26 16:34
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.FbaShipmentDTO.ListDTO>
     **/
    IPage<AliexpressDeliveryDTO.ListDTO> paging(Page<AliexpressDeliveryDTO.ListDTO> query, @Param("params") AliexpressDeliveryDTO.SearchParamDTO params);

    /**
     * 导出excel
     * @Author Luo_WG
     * @Date 2024/1/26 16:54
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.AliexpressDeliveryDTO.ListDTO>
     **/
    List<AliexpressDeliveryDTO.ListDTO> listExportExcel(@Param("params") AliexpressDeliveryDTO.SearchParamDTO dto);

    Page<AliexpressDeliveryDTO.ListDTO> listExportExcel(@Param("page") Page<AliexpressDeliveryDTO.ListDTO> page, @Param("params") AliexpressDeliveryDTO.SearchParamDTO dto);
}
