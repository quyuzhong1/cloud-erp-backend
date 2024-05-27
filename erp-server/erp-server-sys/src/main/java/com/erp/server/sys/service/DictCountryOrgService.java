package com.erp.server.sys.service;
import com.erp.model.sys.entity.DictCountryOrgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.DictCountryOrgDTO;

import java.util.List;

/**
 * <p>
 * 国家-组织（政治经济）关系表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-05-21
 */
public interface DictCountryOrgService extends SuperService<DictCountryOrgEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-05-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictCountryOrgDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-05-21
    * @param dto
    * @return
    */
    Boolean update(DictCountryOrgDTO.UpdateDTO dto);

    /**
     * 根据组织编码获取国家组织关系列表
     * @param orgCode
     * @return
     */
    List<DictCountryOrgEntity> listCountryByOrgCode(String orgCode);
}
