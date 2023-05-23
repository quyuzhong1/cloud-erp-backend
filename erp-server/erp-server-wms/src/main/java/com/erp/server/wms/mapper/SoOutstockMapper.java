package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单出库单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoOutstockMapper extends BaseMapper<SoOutstockEntity> {

    IPage<SoOutstockDTO.PagingViewDTO> paging(Page query, @Param("params") SoOutstockDTO.PagingParamDTO params,@Param("approveList") List<String> approveList);

    List<SoOutstockDTO.PagingViewDTO> listExport(@Param("params") SoOutstockDTO.ExportDTO dto,@Param("approveList") List<String> approveList);

    List<InOutStockDTO> listInventoryInOut(@Param("ids") List<String> idList);

    List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(@Param("soId") String soId);
}
