package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;


/**
 * <p>
 * 库存关账记录表 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
 */
@Mapper
public interface InventoryClosedRecordMapper extends BaseMapper<InventoryClosedRecordEntity> {
    /**
     * 查询最小关账时间
     * @author will
     * @date 2024/12/19 15:09
     * @return LocalDate
     */
    LocalDate getMinClosedDate();
}
