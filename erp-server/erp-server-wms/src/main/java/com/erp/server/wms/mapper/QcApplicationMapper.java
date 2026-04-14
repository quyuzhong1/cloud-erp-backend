package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.QcApplicationSrmDTO;
import com.erp.model.wms.entity.QcApplicationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 质检申请单主表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Mapper
public interface QcApplicationMapper extends BaseMapper<QcApplicationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcApplicationDTO.ListDTO> paging(Page query, @Param("params") QcApplicationDTO.PagingParamDTO params);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<QcApplicationSrmDTO.ListDTO> srmPaging(Page query, @Param("params") QcApplicationDTO.PagingParamDTO params);
    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcApplicationDTO.TabListDTO> tabList(@Param("params") QcApplicationDTO.PagingParamDTO searchParam);
    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<QcApplicationDTO.TabListDTO> srmTabList(@Param("params") QcApplicationSrmDTO.TabListParamDTO searchParam);
}
