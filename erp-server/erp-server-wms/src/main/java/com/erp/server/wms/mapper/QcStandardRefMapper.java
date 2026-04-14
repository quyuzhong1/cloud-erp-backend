package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcStandardRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcStandardRefDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
 */
@Mapper
public interface QcStandardRefMapper extends BaseMapper<QcStandardRefEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcStandardRefDTO.ListDTO> paging(Page query, @Param("params") QcStandardRefDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") QcStandardRefDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<QcStandardRefDTO.ListDTO> listExport(@Param("params") QcStandardRefDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcStandardRefDTO.TabListDTO> tabList(@Param("params") QcStandardRefDTO.PagingParamDTO searchParam);
}
