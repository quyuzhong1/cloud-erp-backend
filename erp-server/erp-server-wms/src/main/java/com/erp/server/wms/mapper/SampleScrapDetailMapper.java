package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 样品报废单明细表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Mapper
public interface SampleScrapDetailMapper extends BaseMapper<SampleScrapDetailEntity> {

    @MapKey("skuNo")
    Map<String, Integer> listBySku(@Param("id") String id ,@Param("skuNos") List<String> skuNos);


}
