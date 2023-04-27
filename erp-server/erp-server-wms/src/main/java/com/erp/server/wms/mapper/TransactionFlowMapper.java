package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.TransactionFlowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Classname: TransactionFlowMapper
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:44
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface TransactionFlowMapper extends BaseMapper<TransactionFlowEntity> {
}
