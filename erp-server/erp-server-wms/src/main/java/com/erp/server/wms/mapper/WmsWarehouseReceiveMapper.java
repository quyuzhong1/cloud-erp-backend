package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.WmsWarehouseReceiveEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 仓库签收单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface WmsWarehouseReceiveMapper extends BaseMapper<WmsWarehouseReceiveEntity> {

}
