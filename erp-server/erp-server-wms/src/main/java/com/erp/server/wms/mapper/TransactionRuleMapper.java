package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.TransactionRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Classname: TransactionRuleMapper
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:19
 * @Author: zhangchunlin
 */
@Repository
@Mapper
public interface TransactionRuleMapper extends BaseMapper<TransactionRuleEntity> {
}
