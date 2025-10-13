package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * FBI库存 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FbaInventoryMapper extends BaseMapper<FbaInventoryEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/11/8 15:56
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.FbaDeliveryDTO.ListDTO>
     **/
    IPage<FbaInventoryDTO.ListDTO> paging(Page query, @Param("params") FbaInventoryDTO.PagingParamDTO params);

    /**
     * 导出excel
     * @Author Luo_WG
     * @Date 2023/11/8 17:49
     * @param param
     * @return java.util.List<com.erp.model.wms.dto.FbaInventoryDTO.ListDTO>
     **/
    List<FbaInventoryDTO.ListDTO> listExport(@Param("params") FbaInventoryDTO.ExportDTO param);
    Page<FbaInventoryDTO.ListDTO> listExport(@Param("page") Page<FbaInventoryDTO.ListDTO> page, @Param("params") FbaInventoryDTO.ExportDTO param);

    /**
     * 列表汇总数量
     * @Author Luo_WG
     * @Date 2023/11/9 11:44
     * @param params
     * @return com.erp.model.wms.dto.FbaInventoryDTO.SummaryNumber
     **/
    FbaInventoryDTO.SummaryNumber summaryNumber(@Param("params") FbaInventoryDTO.PagingParamDTO params);

    List<FbaInventoryDTO.InventoryDTO> listFbaInventory(@Param("params") FbaInventoryDTO.QueryDTO queryDTO);
}
