package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.SoReturnPrestockDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 预入库单详情表 Mapper 接口
 * </p>
 *
 * @author auto
 * @since 2026-06-30
 */
@Mapper
public interface SoReturnPrestockDetailMapper extends BaseMapper<SoReturnPrestockDetailEntity> {

    /**
     * 根据主表 ID 列表查询详情行
     *
     * @param mainIds 主表 ID 列表
     * @return 详情行列表
     */
    List<SoReturnPrestockDetailEntity> listByMainIds(@Param("mainIds") List<String> mainIds);
}
