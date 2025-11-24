package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 推送数据配置明细 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpCfgOutputDetailMapper extends BaseMapper<DmpCfgOutputDetailEntity> {
    /**
     * 根据inputId查询
     * @author will
     * @date 2025/11/24 10:23
     * @param inputId
     * @return DmpCfgOutputDetailEntity
     */
    DmpCfgOutputDetailEntity getDmpCfgOutputDetailByOption(@Param("inputId") String inputId,@Param("nextLevelId") String nextLevelId);
}
