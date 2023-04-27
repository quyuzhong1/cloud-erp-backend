package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.InventoryDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Classname: InventoryDetailMapper
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:01
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface InventoryDetailMapper  extends BaseMapper<InventoryDetailEntity> {
}
