package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.erp.model.mrp.dto.InventoryTotalDTO;
import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.InventoryDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 库存详情 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface ReplenishmentInventoryDetailMapper extends BaseMapper<ReplenishmentInventoryDetailEntity> {

    Page<InventoryDetailVO> inventoryDetail(@Param("page") Page<InventoryDetailVO> page,@Param("param") InventoryTotalDTO params);
}
