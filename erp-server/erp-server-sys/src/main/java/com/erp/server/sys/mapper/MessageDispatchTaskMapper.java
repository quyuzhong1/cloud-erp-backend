package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.entity.MessageDispatchTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageDispatchTaskMapper extends BaseMapper<MessageDispatchTaskEntity> {

    List<MessageDispatchTaskEntity> listDueTasks(@Param("maxRetryCount") Integer maxRetryCount,
                                           @Param("limit") Integer limit);
}
