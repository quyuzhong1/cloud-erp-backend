package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.InventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Classname: InventoryMapper
 * @Description: TODO
 * @CreateTime: 2023-04-25  15:30
 * @Author: zhangchunlin
 */
@Mapper
@Repository
public interface InventoryMapper extends BaseMapper<InventoryEntity> {
}
