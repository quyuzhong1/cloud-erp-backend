package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.WarehouseAreaInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 库区管理 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-05-29
 */
@Mapper
public interface WarehouseAreaInfoMapper extends BaseMapper<WarehouseAreaInfoEntity> {

    IPage<WarehouseAreaDTO.PagingView> paging(Page<WarehouseAreaDTO.PagingView> pagingViewPage, WarehouseAreaDTO.PagingParam params);
}
