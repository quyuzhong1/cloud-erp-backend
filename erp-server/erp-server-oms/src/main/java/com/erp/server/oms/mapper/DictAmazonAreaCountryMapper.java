package com.erp.server.oms.mapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.DictAmazonAreaCountryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Mapper
public interface DictAmazonAreaCountryMapper extends BaseMapper<DictAmazonAreaCountryEntity> {
    /**
     * 获取地区信息
     * @return
     */
    List<BaseDropDownDTO.CommonDTO> areaList();

    /**
     * 获取国家
     * @param areaList
     * @return
     */
    List<BaseDropDownDTO.CommonDTO> listCountryByArea(@Param("areaList") List<String> areaList);
}
