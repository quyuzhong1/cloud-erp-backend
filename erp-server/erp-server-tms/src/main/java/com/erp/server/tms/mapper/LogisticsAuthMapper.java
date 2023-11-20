package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.LogisticsAuthDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 物流授权表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsAuthMapper extends BaseMapper<LogisticsAuthEntity> {

}
