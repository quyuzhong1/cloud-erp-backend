package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.InventoryFlowOverrideRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 库存流水重算时间范围记录 Mapper 接口
 * </p>
 *
 * @author cloud
 * @since 2024-08-09
 */
@Mapper
public interface InventoryFlowOverrideRecordMapper extends BaseMapper<InventoryFlowOverrideRecordEntity> {

    /**
     *  按照组织分组查询最大结束时间
     *
     * @param inventoryOrgId 库存组织id
     * @return List<InventoryFlowOverrideRecordEntity>
     */
    List<InventoryFlowOverrideRecordEntity> listMaxEndTimeGroupByOrgId(String inventoryOrgId);
}
