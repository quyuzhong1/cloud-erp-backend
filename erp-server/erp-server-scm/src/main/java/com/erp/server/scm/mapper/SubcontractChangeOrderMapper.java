package com.erp.server.scm.mapper;
import com.erp.model.scm.entity.SubcontractChangeOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.scm.dto.SubcontractChangeOrderDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 委外变更单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Mapper
public interface SubcontractChangeOrderMapper extends BaseMapper<SubcontractChangeOrderEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SubcontractChangeOrderDTO.ListDTO> paging(Page query, @Param("params") SubcontractChangeOrderDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SubcontractChangeOrderDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SubcontractChangeOrderDTO.ListDTO> listExport(@Param("params") SubcontractChangeOrderDTO.ExportDTO params);

}
