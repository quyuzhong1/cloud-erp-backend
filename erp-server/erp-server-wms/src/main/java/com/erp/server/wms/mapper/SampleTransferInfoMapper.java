package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleTransferInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleTransferInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品转移单主表 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
@Mapper
public interface SampleTransferInfoMapper extends BaseMapper<SampleTransferInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleTransferInfoDTO.ListDTO> paging(Page query, @Param("params") SampleTransferInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleTransferInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询（分页）
    * @param query
    * @param params
    * @return
    */
    IPage<SampleTransferInfoDTO.ListDTO> listExport(Page<SampleTransferInfoDTO.ExportDTO> query, @Param("params") SampleTransferInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleTransferInfoDTO.TabListDTO> tabList(@Param("params") SampleTransferInfoDTO.PagingParamDTO searchParam);
}
