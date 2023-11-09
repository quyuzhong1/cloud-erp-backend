package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 运费模板渠道关联表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Mapper
public interface ShippingTemplateRuleMapper extends BaseMapper<ShippingTemplateRuleEntity> {
    /**
     * @description: 仓库
     * @author Will
     * @date: 2023/11/9 11:36
     * @return List<String>
     */
    List<String> listWarehouseName();
    /**
     * @description:分区
     * @author Will
     * @date: 2023/11/9 11:36
     * @return List<String>
     */
    List<String> listRegionName();
}
