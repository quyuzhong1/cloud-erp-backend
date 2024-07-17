package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 拣货规则表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Mapper
public interface CfgRulePickingMapper extends BaseMapper<CfgRulePickingEntity> {

    IPage<CfgRulePickingDTO.PagingView> paging(@Param("page") Page<CfgRulePickingDTO.PagingView> objectPage,@Param("params") CfgRulePickingDTO.PagingParam params);
}
