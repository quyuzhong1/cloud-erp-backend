package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleScrapInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品报废单主表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Mapper
public interface SampleScrapInfoMapper extends BaseMapper<SampleScrapInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleScrapInfoDTO.ListDTO> paging(Page query, @Param("params") SampleScrapInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleScrapInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SampleScrapInfoDTO.ListDTO> listExport(@Param("params") SampleScrapInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleScrapInfoDTO.TabListDTO> tabList(@Param("params") SampleScrapInfoDTO.PagingParamDTO searchParam);
}
