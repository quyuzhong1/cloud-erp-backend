package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 中台销售订单表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
@Mapper
public interface DmpSoInfoMapper extends BaseMapper<DmpSoInfoEntity> {

    /**
     *
     */
    List<DmpSoInfoEntity> findSoMissingDetail(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("sourceSystem") String sourceSystem,
                                              @Param("nextLevelId") String nextLevelId);
}
