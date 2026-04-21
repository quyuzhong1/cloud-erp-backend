package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcSamplingCodeRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * GB/T2828.1-2012 批量-样本量字码映射表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
 */
@Mapper
public interface QcSamplingCodeRuleMapper extends BaseMapper<QcSamplingCodeRuleEntity> {
    /**
     * 根据批量数查询映射关系
     */

    QcSamplingCodeRuleEntity selectByLotQty(@Param("lotQty") Integer lotQty);

    /**
     * 根据样本量字码查询映射关系
     * @param sampleCode
     * @return
     */
    QcSamplingCodeRuleEntity getRuleBySampleCode(@Param("sampleCode") String sampleCode);
}
