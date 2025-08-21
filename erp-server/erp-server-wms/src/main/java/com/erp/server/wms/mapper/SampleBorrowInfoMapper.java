package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 借用变更单 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Mapper
public interface SampleBorrowInfoMapper extends BaseMapper<SampleBorrowInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleBorrowInfoDTO.ListDTO> paging(Page query, @Param("params") SampleBorrowInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleBorrowInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SampleBorrowInfoDTO.ListDTO> listExport(@Param("params") SampleBorrowInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleBorrowInfoDTO.TabListDTO> tabList(@Param("params") SampleBorrowInfoDTO.PagingParamDTO searchParam);
}
