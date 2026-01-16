package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleAdjustmentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleAdjustmentInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品调整单 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
@Mapper
public interface SampleAdjustmentInfoMapper extends BaseMapper<SampleAdjustmentInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleAdjustmentInfoDTO.ListDTO> paging(Page query, @Param("params") SampleAdjustmentInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleAdjustmentInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SampleAdjustmentInfoDTO.ListDTO> listExport(@Param("params") SampleAdjustmentInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleAdjustmentInfoDTO.TabListDTO> tabList(@Param("params") SampleAdjustmentInfoDTO.PagingParamDTO searchParam);

    /**
     * 移动端分页查询
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SampleAdjustmentInfoDTO.ListDTO> pagingApp(Page query, @Param("params") SampleAdjustmentInfoDTO.PagingParamDTO params);
}
