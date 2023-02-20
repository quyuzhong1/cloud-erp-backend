package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.DocsShowDTO;
import com.erp.model.plm.entity.SysDocsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统产品文档 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface SysDocsMapper extends BaseMapper<SysDocsEntity> {

    IPage<DocsShowDTO> paging(Page query, @Param("params") BaseSearchDTO params, @Param("flagState") Integer state);


}
