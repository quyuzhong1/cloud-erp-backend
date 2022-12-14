package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.dto.ShopDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpShopInfo
 */
@Mapper
public interface DmpShopInfoMapper extends BaseMapper<DmpShopInfoEntity> {

    List<ShopDTO> queryShopByPlatformList(@Param("platformSign") String platformSign);
}




