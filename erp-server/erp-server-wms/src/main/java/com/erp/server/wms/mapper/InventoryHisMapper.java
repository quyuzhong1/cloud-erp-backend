package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.InventoryHisEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @Classname: InventoryHisMapper
 * @Description: TODO
 * @CreateTime: 2023-04-27  17:02
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface InventoryHisMapper extends BaseMapper<InventoryHisEntity> {

    /**
     * 修改库存历史表数量
     * @param id
     * @param qty
     * @param version
     * @return
     */
    int updateQtyById(@Param(value = "id") String id,@Param(value = "qty") Integer qty,@Param(value = "version") Integer version,
                      @Param(value = "updateTime") LocalDateTime updateTime,@Param(value = "updateUserId") String updateUserId, @Param(value = "updateUserName") String updateUserName);

}
