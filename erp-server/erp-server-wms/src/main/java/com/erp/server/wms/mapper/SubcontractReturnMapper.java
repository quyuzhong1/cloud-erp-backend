package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SubcontractReturnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SubcontractReturnDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 委外退料单 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Mapper
public interface SubcontractReturnMapper extends BaseMapper<SubcontractReturnEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SubcontractReturnDTO.ListDTO> paging(Page query, @Param("params") SubcontractReturnDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SubcontractReturnDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SubcontractReturnDTO.ListDTO> listExport(@Param("params") SubcontractReturnDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SubcontractReturnDTO.TabListDTO> tabList(@Param("params") SubcontractReturnDTO.PagingParamDTO searchParam);
}
