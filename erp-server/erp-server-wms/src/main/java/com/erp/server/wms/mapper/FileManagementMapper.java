package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 文件管理 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Mapper
public interface FileManagementMapper extends BaseMapper<FileManagementEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<FileManagementDTO.ListDTO> paging(Page query, @Param("params") FileManagementDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") FileManagementDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<FileManagementDTO.ListDTO> listExport(@Param("params") FileManagementDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<FileManagementDTO.TabListDTO> tabList(@Param("params") FileManagementDTO.PagingParamDTO searchParam);
}
