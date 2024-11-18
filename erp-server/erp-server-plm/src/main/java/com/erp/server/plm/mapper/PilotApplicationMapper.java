package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.entity.PilotApplicationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    IPage<PilotApplicationDTO.ListDTO> pagingByParam(Page<PilotApplicationDTO.PagingParamDTO> query, @Param("params") PilotApplicationDTO.PagingParamDTO params);

    List<PilotApplicationDTO.ListDTO> exportList(@Param("params") PilotApplicationDTO.ExportDTO params);
}
