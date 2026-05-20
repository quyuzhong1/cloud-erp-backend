package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcDefectEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcDefectDTO;
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
public interface QcDefectMapper extends BaseMapper<QcDefectEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcDefectDTO.ListDTO> paging(Page query, @Param("params") QcDefectDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") QcDefectDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<QcDefectDTO.ListDTO> listExport(@Param("params") QcDefectDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcDefectDTO.TabListDTO> tabList(@Param("params") QcDefectDTO.PagingParamDTO searchParam);
}
