package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcNoticeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcNoticeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 质检通知单 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-04-21
 */
@Mapper
public interface QcNoticeMapper extends BaseMapper<QcNoticeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcNoticeDTO.ListDTO> paging(Page query, @Param("params") QcNoticeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") QcNoticeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    IPage<QcNoticeDTO.ListDTO> listExport(Page query,@Param("params") QcNoticeDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcNoticeDTO.TabListDTO> tabList(@Param("params") QcNoticeDTO.PagingParamDTO searchParam);
    List<QcNoticeDTO.TabListDTO> tabQcStatusList(@Param("params") QcNoticeDTO.PagingParamDTO searchParam);

    List<QcNoticeDTO.QcInfoView> listQcInfoView(@Param("ids") List<String> ids);

    List<QcNoticeDTO.QcInfoFullView> listQcInfoViewByCode(@Param("code") String code);
}
