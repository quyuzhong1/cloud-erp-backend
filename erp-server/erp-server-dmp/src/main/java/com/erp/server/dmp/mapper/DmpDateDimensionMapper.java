package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpDateDimensionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 时间维度表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2023-12-08
 */
@Mapper
public interface DmpDateDimensionMapper extends BaseMapper<DmpDateDimensionEntity> {
    void deleteByYear(@Param("year") String year);

    void batchInsertDateDimensions(@Param("list") List<DmpDateDimensionEntity> list);
}
