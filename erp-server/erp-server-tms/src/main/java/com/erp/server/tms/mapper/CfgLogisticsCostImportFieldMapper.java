package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 费用项配置字段基础表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Mapper
public interface CfgLogisticsCostImportFieldMapper extends BaseMapper<CfgLogisticsCostImportFieldEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgLogisticsCostImportFieldDTO.ListDTO> paging(Page query, @Param("params") CfgLogisticsCostImportFieldDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgLogisticsCostImportFieldDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgLogisticsCostImportFieldDTO.ListDTO> listExport(@Param("params") CfgLogisticsCostImportFieldDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgLogisticsCostImportFieldDTO.TabListDTO> tabList(@Param("params") CfgLogisticsCostImportFieldDTO.PagingParamDTO searchParam);
}
