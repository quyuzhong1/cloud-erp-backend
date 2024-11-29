package com.erp.server.mrp.mapper;

import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 归档配置 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
@Mapper
public interface CfgDataArchivingMapper extends BaseMapper<CfgDataArchivingEntity> {

    /**
     * 归档表
     */
    void moveData(@Param("sourceTable") String sourceTable,@Param("archiveTable") String archiveTable);
    /**
     * 根据关联sql归档表
     */
    void moveDataByRelId(@Param("sourceTable") String sourceTable,@Param("archiveTable") String archiveTable, @Param("refSql") String refSql);
    /**
     * 删除数据
     */
    void deleteSource(@Param("sourceTable") String sourceTable);
    /**
     * 根据关联sql删除数据
     */
    void deleteSourceByRelId(@Param("sourceTable") String sourceTable, @Param("refSql") String refSql);

    /**
     * 删除建议
     */
    void deleteDeliverySuggest(@Param("sourceId") String sourceId);

    /**
     * 发货建议数据迁移
     */
    void moveDeliverySuggest(@Param("sourceId") String sourceId);
    /**
     * 采购合并建议数据迁移
     */
    void movePurchaseSuggestMerge();
    /**
     * 删除建议
     */
    void deletePurchaseSuggestMerge();
    /**
     * 采购建议数据迁移
     */
    void movePurchaseSuggest(@Param("sourceId") String sourceId);
    /**
     * 删除建议
     */
    void deletePurchaseSuggest(@Param("sourceId") String sourceId);
}
