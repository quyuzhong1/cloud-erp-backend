package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.OtherOutstockTrackNoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 其他出库单跟踪号映射表 Mapper 接口
 * </p>
 *
 * @author system
 * @since 2025-12-16
 */
@Mapper
public interface OtherOutstockTrackNoMapper extends BaseMapper<OtherOutstockTrackNoEntity> {

    /**
     * 根据出库单ID查询跟踪号列表
     * @param otherOutstockId 出库单ID
     * @return 跟踪号实体列表
     */
    List<OtherOutstockTrackNoEntity> listByOutstockId(@Param("otherOutstockId") String otherOutstockId);

    /**
     * 批量查询出库单的跟踪号
     * @param otherOutstockIds 出库单ID列表
     * @return 跟踪号实体列表
     */
    List<OtherOutstockTrackNoEntity> listByOutstockIds(@Param("otherOutstockIds") List<String> otherOutstockIds);
}
