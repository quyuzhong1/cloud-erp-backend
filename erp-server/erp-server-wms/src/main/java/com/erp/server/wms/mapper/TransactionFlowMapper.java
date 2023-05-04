package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.TransactionFlowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @Classname: TransactionFlowMapper
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:44
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface TransactionFlowMapper extends BaseMapper<TransactionFlowEntity> {

    /**
     * 修改交易流水为已反审核
     * @param id
     * @param version
     * @return
     */
    int updateUnapprovedById(@Param(value = "id") String id, @Param(value = "version") Integer version,
                      @Param(value = "updateTime") LocalDateTime updateTime, @Param(value = "updateUserId") String updateUserId, @Param(value = "updateUserName") String updateUserName);


}
