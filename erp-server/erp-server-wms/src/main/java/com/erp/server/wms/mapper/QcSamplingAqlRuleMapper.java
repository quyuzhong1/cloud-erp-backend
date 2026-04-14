package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcSamplingAqlRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * GB/T2828.1-2012 AQL判定数主表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
 */
@Mapper
public interface QcSamplingAqlRuleMapper extends BaseMapper<QcSamplingAqlRuleEntity> {
    /**
     * 根据字码+AQL值查询判定数
     */

    QcSamplingAqlRuleEntity selectByCodeAndAql(@Param("sampleCode") String sampleCode, @Param("aqlValue") String aqlValue);
}
