package com.erp.server.srm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 送货单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Mapper
public interface DeliveryOrderMapper extends BaseMapper<DeliveryOrderEntity> {

    IPage<DeliveryOrderDTO.ListDTO> paging(Page query, @Param("params")DeliveryOrderDTO.ParamDTO params);

    List<DeliveryOrderDTO.StatusListDTO> tabList(@Param("supplierIdList") List<String> supplierIdList);

    List<DeliveryOrderExportExcelDTO> getExportList(@Param("params") DeliveryOrderDTO.ParamDTO dto);

    List<DeliveryOrderDTO.GenerateReceiveListDTO> listGenerateReceive(@Param("ids") List<String> ids);

    DeliveryOrderDTO.TotalInfo pagingTotal(@Param("params")DeliveryOrderDTO.ParamDTO dto);
}
