package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * b2c报关对账单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Mapper
public interface TmsB2cDeclareReconciliationMapper extends BaseMapper<TmsB2cDeclareReconciliationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<TmsB2cDeclareReconciliationDTO.ListDTO> paging(Page query, @Param("params") TmsB2cDeclareReconciliationDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<TmsB2cDeclareReconciliationDTO.ListDTO> listExport(@Param("params") TmsB2cDeclareReconciliationDTO.ExportDTO params);
    Page<TmsB2cDeclareReconciliationDTO.ListDTO> listExport(@Param("page") Page<TmsB2cDeclareReconciliationDTO.ListDTO> page, @Param("params") TmsB2cDeclareReconciliationDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<TmsB2cDeclareReconciliationDTO.TabListDTO> tabList(@Param("params") TmsB2cDeclareReconciliationDTO.PagingParamDTO searchParam);
}
