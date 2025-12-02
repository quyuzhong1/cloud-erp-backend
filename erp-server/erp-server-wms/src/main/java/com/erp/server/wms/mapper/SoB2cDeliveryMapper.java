package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * b2c发货单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Mapper
public interface SoB2cDeliveryMapper extends BaseMapper<SoB2cDeliveryEntity> {

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/12/14 16:53
     * @param searchParam
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.TabListDTO>
     **/
    List<SoB2cDeliveryDTO.TabListDTO> tabList(@Param("params") SoB2cDeliveryDTO.PagingParamDTO searchParam);

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/12/14 17:03
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoB2cDeliveryDTO.ListDTO>
     **/
    IPage<SoB2cDeliveryDTO.ListDTO> paging(Page query, @Param("params") SoB2cDeliveryDTO.PagingParamDTO params);

    /**
     * 列表查询
     * @param params
     * @return
     */
    List<SoB2cDeliveryDTO.ListDTO> list(@Param("params") SoB2cDeliveryDTO.PagingParamDTO params);
    Page<SoB2cDeliveryDTO.ListDTO> list(@Param("page") Page<SoB2cDeliveryDTO.ListDTO> page, @Param("params") SoB2cDeliveryDTO.PagingParamDTO params);

    List<SoB2cDeliveryDTO.CancelShipmentDTO> cancelShipmentView(@Param("ids") List<String> ids);

    int countShipmentMark(@Param("params") PermissionsDTO params);
    /**
     * 查询b2c流水数据
     * @author will
     * @date 2025/3/31 11:57
     * @return java.util.List<com.erp.model.wms.entity.VirtualFlowRefactorDTO.OutInStockDTO>
     */
    List<VirtualFlowRefactorDTO.OutInStockDTO> rebuildB2cVirtualFlow();
}
