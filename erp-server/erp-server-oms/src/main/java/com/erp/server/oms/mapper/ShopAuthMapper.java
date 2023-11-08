package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 店铺授权表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Mapper
public interface ShopAuthMapper extends BaseMapper<ShopAuthEntity> {
    List<ShopAuthEntity> getShopeeShopList(@Param("type") String type,@Param("status") String status);
}
