package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 建议采购 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Mapper
public interface PurchaseSuggestMapper extends BaseMapper<PurchaseSuggestEntity> {
    /**
     * 查询可生成采购建议合并的数据
     * @author will
     * @date 2025/1/6 17:36
     * @param purchaseSuggestEntity
     * @return List<PurchaseSuggestEntity>
     */
    List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(@Param("purchaseSuggestEntity") PurchaseSuggestEntity purchaseSuggestEntity);
}
