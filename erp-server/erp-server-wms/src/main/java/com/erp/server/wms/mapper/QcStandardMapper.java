package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.QcStandardEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 质检标准主表 Mapper 接口
 *
 * @author jack
 * @since 2026-03-22
 */
@Mapper
public interface QcStandardMapper extends BaseMapper<QcStandardEntity> {

    List<QcStandardDTO.TabListDTO> tabList(@Param("params") QcStandardDTO.PagingParamDTO params);

    IPage<QcStandardDTO.ListDTO> paging(Page query, @Param("params") QcStandardDTO.PagingParamDTO params);
}
