package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * <p>
 * 手工出入库待同步数据表 Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
@Repository
@Mapper
public interface DmpOutInStockMapper extends BaseMapper<DmpOutInStockEntity> {


}
