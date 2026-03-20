package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 质检申请单明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Mapper
public interface QcApplicationDetailMapper extends BaseMapper<QcApplicationDetailEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcApplicationDetailDTO.ListDTO> paging(Page query, @Param("params") QcApplicationDetailDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") QcApplicationDetailDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<QcApplicationDetailDTO.ListDTO> listExport(@Param("params") QcApplicationDetailDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcApplicationDetailDTO.TabListDTO> tabList(@Param("params") QcApplicationDetailDTO.PagingParamDTO searchParam);
}
