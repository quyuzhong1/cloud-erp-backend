package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.entity.OperateLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 操作日志表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLogEntity> {

}
