package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 委外发料单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Mapper
public interface SubcontractIssueMapper extends BaseMapper<SubcontractIssueEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SubcontractIssueDTO.ListDTO> paging(Page query, @Param("params") SubcontractIssueDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SubcontractIssueDTO.ListDTO> listExport(@Param("params") SubcontractIssueDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    Integer tabList(@Param("params") SubcontractIssueDTO.PagingParamDTO searchParam);
}
