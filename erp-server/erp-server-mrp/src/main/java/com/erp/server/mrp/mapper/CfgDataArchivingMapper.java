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

    void moveData(@Param("sourceTable") String sourceTable,@Param("archiveTable") String archiveTable);
    void moveDataByRelId(@Param("sourceTable") String sourceTable,@Param("archiveTable") String archiveTable, @Param("refSql") String refSql);

    void deleteSource(@Param("sourceTable") String sourceTable);
    void deleteSourceByRelId(@Param("sourceTable") String sourceTable, @Param("refSql") String refSql);
}
