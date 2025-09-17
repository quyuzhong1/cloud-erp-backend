package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleRecipientDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品领用单 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Mapper
public interface SampleRecipientMapper extends BaseMapper<SampleRecipientEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleRecipientDTO.ListDTO> paging(Page query, @Param("params") SampleRecipientDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleRecipientDTO.PagingParamDTO params);
//
//    /**
//    * 导出Excel查询
//    * @param params
//    * @return
//    */
//    List<SampleRecipientDTO.ListDTO> listExport(@Param("params") SampleRecipientDTO.ExportDTO params);

    /**
    * 分页导出Excel查询
    * @param query 分页参数
    * @param params 查询参数
    * @return
    */
    IPage<SampleRecipientDTO.ListDTO> listExport(Page query, @Param("params") SampleRecipientDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleRecipientDTO.TabListDTO> tabList(@Param("params") SampleRecipientDTO.PagingParamDTO searchParam);

    /**
     * 获取所有状态的统计数量（一个SQL查询完成）
     * @param permissionSql 权限SQL
     * @return 状态统计列表
     */
    List<SampleRecipientDTO.TabListDTO> getAllStatusCounts(@Param("permissionSql") String permissionSql);

    /**
     * 移动端分页查询
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SampleRecipientDTO.ListDTO> pagingApp(Page query, @Param("params") SampleRecipientDTO.PagingParamDTO params);
}
