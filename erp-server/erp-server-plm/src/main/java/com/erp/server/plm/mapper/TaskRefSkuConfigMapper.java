package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.TaskRefSkuConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务sku配置关系表(TaskRefSkuConfig)表数据库访问层
 *
 * @author Lambda
 * @since 2022-11-21 14:01:00
 */
@Mapper
public interface TaskRefSkuConfigMapper extends BaseMapper<TaskRefSkuConfigEntity> {


    List<TaskRefSkuConfigEntity> getDisableFieldByProductId(@Param("productId") String productId, @Param("statusList")List<Integer> statusList);
}

