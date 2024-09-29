package com.erp.server.tms.mapper;

import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 头程费用SKU分摊明细 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Mapper
public interface FirstMileSkuCostAllocationDetailMapper extends BaseMapper<FirstMileSkuCostAllocationDetailEntity> {
    /**
     * 根据分摊记录主表获取所有分摊记录明细
     * @param mainIds
     * @return
     */
    List<FirstMileSkuCostAllocationDetailEntity> listByMainIds(@Param("mainIds") List<String> mainIds);
}
