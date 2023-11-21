package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 海外仓库存 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Mapper
public interface OverseasInventoryMapper extends BaseMapper<OverseasInventoryEntity> {

    IPage<OverseasInventoryDTO.ListDTO> paging(Page<?> query, @Param("params") OverseasInventoryDTO.PagingParamDTO params);
}
