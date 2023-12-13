package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.entity.DictCityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Mapper
public interface DictCityMapper extends BaseMapper<DictCityEntity> {

    List<DictCityEntity> listByIdList(@Param("idList") List<String> idList);
}
