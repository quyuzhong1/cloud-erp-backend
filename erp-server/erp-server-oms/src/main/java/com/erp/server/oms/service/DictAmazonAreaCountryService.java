package com.erp.server.oms.service;
import com.erp.model.oms.entity.DictAmazonAreaCountryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.DictAmazonAreaCountryDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
public interface DictAmazonAreaCountryService extends SuperService<DictAmazonAreaCountryEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    String add(DictAmazonAreaCountryDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    Boolean update(DictAmazonAreaCountryDTO.UpdateDTO dto);
 
    /**
     * 方法说明
     * @author yl
     * @date 2023-08-30 9:40
     * @param
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    List<BaseDropDownDTO.CommonDTO> areaList();

    /**
     * 根据区域获取国家
     * @author yl
     * @date 2023-08-30 10:02
     * @param areaList
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    List<BaseDropDownDTO.CommonDTO> listCountryByArea(List<String> areaList);

    /**
     * 根据国家code
     * @author yl
     * @date 2023-08-30 10:21
     * @param countryCodeList
     * @return java.util.List<com.erp.model.oms.entity.DictAmazonAreaCountryEntity>
     */
    List<DictAmazonAreaCountryEntity> listByCountryCodes(List<String> countryCodeList);
}
