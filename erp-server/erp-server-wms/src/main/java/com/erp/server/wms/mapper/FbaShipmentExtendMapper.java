package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.FbaShipmentExtendEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.FbaShipmentExtendDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * FBA拣货扩展表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-12-24
 */
@Mapper
public interface FbaShipmentExtendMapper extends BaseMapper<FbaShipmentExtendEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<FbaShipmentExtendDTO.ListDTO> paging(Page query, @Param("params") FbaShipmentExtendDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") FbaShipmentExtendDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<FbaShipmentExtendDTO.ListDTO> listExport(@Param("params") FbaShipmentExtendDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<FbaShipmentExtendDTO.TabListDTO> tabList(@Param("params") FbaShipmentExtendDTO.PagingParamDTO searchParam);
}
