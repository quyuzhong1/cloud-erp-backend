package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.TaskChargeDistributionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/29 14:37
 */
@Mapper
public interface TaskChargeDistributionMapper extends BaseMapper<TaskChargeDistributionEntity> {

    List<TaskChargeDistributionEntity> listBySourceAndRoleName(@Param("source") List<Integer>  source,@Param("name") String name);
}
