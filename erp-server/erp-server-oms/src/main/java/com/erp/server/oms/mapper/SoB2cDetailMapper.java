package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.wms.dto.ReportOrderDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2C销售订单明细表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cDetailMapper extends BaseMapper<SoB2cDetailEntity> {

    List<SoB2cDetailDTO.OutstockDTO> listOutstockByMainId(@Param("mainId") String mainId);
    /**
     * @description: 查询待发货数量
     * @author Will
     * @date: 2024/1/31 12:04
     * @param paramDTO
     * @return List<WaitDeliveryQtyDTO>
     */
    List<SoB2cDetailDTO.WaitDeliveryQtyDTO> listWaitDeliveryQty(@Param("paramDTO") SoB2cDetailDTO.WaitDeliveryParamDTO paramDTO);

    List<SoB2cDetailEntity> listContainDeleted(@Param("ids") List<String> ids);

    void updateContainDeleted(@Param("ids") List<String> revertDetailIds);
    /**
     * 查询所有虚拟仓B2C销售订单数据
     * @author will
     * @date 2024/9/26 17:26
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoB2cDetail();
}
