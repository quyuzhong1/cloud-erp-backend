package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 海外仓入库单 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Mapper
public interface OverseasWarehouseInboundMapper extends BaseMapper<OverseasWarehouseInboundEntity> {

    /**
     * 分页查询
     * @author Jim
     * @date: 2023-11-21
     */
    IPage<OverseasWarehouseInboundDTO.ListDTO> paging(Page<?> query, OverseasWarehouseInboundDTO.PagingParamDTO params);

    /**
     * 导出查询
     *
     * @author Jim
     * @date: 2023-11-27
     */
    List<OverseasWarehouseInboundDTO.ListDTO> listExportExcel(@Param("params") OverseasWarehouseInboundDTO.ExportDTO params);
    Page<OverseasWarehouseInboundDTO.ListDTO> listExportExcel(@Param("page") Page<OverseasWarehouseInboundDTO.ListDTO> page, @Param("params") OverseasWarehouseInboundDTO.ExportDTO params);

    /**
     * tab页查询状态数量
     * @Author Luo_WG
     * @Date 2023/12/8 14:53
     * @param
     * @return java.util.List<com.erp.model.wms.dto.OverseasWarehouseInboundDTO.CountDTO>
     **/
    List<OverseasWarehouseInboundDTO.CountDTO> tabList(PermissionsDTO dto);
    
    /**
     * 根据条件获取数据对比系统数据
     * @param params
     * @return
     */
    List<Map<String, String>> getDataCompareByCondition(@Param("params") WmsDataCompareTaskDTO.OverseasInboundDTO params);
    
    Integer getDataCompareByConditionCount(@Param("params") WmsDataCompareTaskDTO.OverseasInboundDTO params);
}
