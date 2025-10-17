package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.AssetNoticeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.AssetNoticeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Mapper
public interface AssetNoticeMapper extends BaseMapper<AssetNoticeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetNoticeDTO.ListDTO> paging(Page query, @Param("params") AssetNoticeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetNoticeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AssetNoticeDTO.ListDTO> listExport(@Param("params") AssetNoticeDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetNoticeDTO.TabListDTO> tabList(@Param("params") AssetNoticeDTO.PagingParamDTO searchParam);
}
