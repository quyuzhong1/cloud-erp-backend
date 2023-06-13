package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.InstockForcastEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 入库预报表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Mapper
@Repository
public interface InstockForcastMapper extends BaseMapper<InstockForcastEntity> {

    int updateDeletedById(@Param(value = "id")String id);

}
