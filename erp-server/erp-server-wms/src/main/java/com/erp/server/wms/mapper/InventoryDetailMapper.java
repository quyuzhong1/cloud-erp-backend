package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.InventoryDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @Classname: InventoryDetailMapper
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:01
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface InventoryDetailMapper  extends BaseMapper<InventoryDetailEntity> {

    /**
     * 修改库存明细表数量
     * @param id
     * @param qty
     * @param version
     * @return
     */
    int updateQtyById(@Param(value = "id") String id, @Param(value = "qty") Integer qty, @Param(value = "version") Integer version,
                      @Param(value = "updateTime") LocalDateTime updateTime, @Param(value = "updateUserId") String updateUserId, @Param(value = "updateUserName") String updateUserName);

}
