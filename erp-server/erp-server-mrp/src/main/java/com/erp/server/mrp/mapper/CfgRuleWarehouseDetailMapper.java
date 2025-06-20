package com.erp.server.mrp.mapper;

import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 仓库（规则设置）明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-24
 */
@Mapper
public interface CfgRuleWarehouseDetailMapper extends BaseMapper<CfgRuleWarehouseDetailEntity> {
    List<CfgRuleWarehouseDetailEntity> listByParams(@Param("mainIdList") List<String> mainIdList, @Param("permissionSql") String warehousePermissionSql);
}
