package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.KolB2bApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.KolB2bApplicationDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * B2B寄养申请主表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Mapper
public interface KolB2bApplicationMapper extends BaseMapper<KolB2bApplicationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<KolB2bApplicationDTO.ListDTO> paging(Page query, @Param("params") KolB2bApplicationDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") KolB2bApplicationDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<KolB2bApplicationDTO.ListDTO> listExport(@Param("params") KolB2bApplicationDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<KolB2bApplicationDTO.TabListDTO> tabList(@Param("params") KolB2bApplicationDTO.PagingParamDTO searchParam);
}
