package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.PilotApplicationDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 试产申请 Mapper 接口
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
@Mapper
public interface PilotApplicationMapper extends BaseMapper<PilotApplicationEntity> {

    /**
     * 分页查询
     *
     * @param query
     * @param params
     * @param approveStatus
     * @param orderStatus
     * @param approveUserId
     * @return
     */
    IPage<PilotApplicationDTO.ListDTO> paging(Page query, @Param("params") PilotApplicationDTO.PagingParamDTO params, @Param("approveStatus") String approveStatus, @Param("orderStatus") String orderStatus, @Param("approveUserId") String approveUserId);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") PilotApplicationDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     *
     * @param params
     * @param approveStatus
     * @param orderStatus
     * @param approveUserId
     * @return
     */
    List<PilotApplicationDTO.ListDTO> listExportByParams(@Param("params") PilotApplicationDTO.ExportDTO params, @Param("approveStatus") String approveStatus, @Param("orderStatus") String orderStatus, @Param("approveUserId") String approveUserId);

    /**
     * tab统计
     * @param approveStatus 审核状态
     * @param orderStatus 订单状态
     * @param approveUserId 审核人ID
     * @date: 2024-08-28
     * @author: tanmujin
     */
    int tabList(@Param("approveStatus") String approveStatus, @Param("orderStatus") String orderStatus, @Param("approveUserId") String approveUserId);

    /**
     * 根据ids查询
     * @param ids
     * @return
     * @date: 2024-08-28
     * @author: tanmujin
     */
    List<PilotApplicationDTO.ListDTO> listExportByIds(@Param("ids") List<String> ids);

    IPage<PilotApplicationDTO.ListDTO> pagingByParam(Page query, @Param("params") PilotApplicationDTO.PagingParamDTO params);

    List<PilotApplicationDTO.ListDTO> exportList(@Param("params") PilotApplicationDTO.ExportDTO params);
}
