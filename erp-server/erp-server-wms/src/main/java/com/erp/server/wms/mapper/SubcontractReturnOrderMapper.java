package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SubcontractReturnOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SubcontractReturnOrderDTO;
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
public interface SubcontractReturnOrderMapper extends BaseMapper<SubcontractReturnOrderEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SubcontractReturnOrderDTO.ListDTO> paging(Page query, @Param("params") SubcontractReturnOrderDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SubcontractReturnOrderDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SubcontractReturnOrderDTO.ListDTO> listExport(@Param("params") SubcontractReturnOrderDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SubcontractReturnOrderDTO.TabListDTO> tabList(@Param("params") SubcontractReturnOrderDTO.PagingParamDTO searchParam);
}
