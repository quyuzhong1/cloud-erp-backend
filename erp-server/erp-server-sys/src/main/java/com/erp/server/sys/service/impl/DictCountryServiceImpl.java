package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.sys.mapper.DictCountryMapper;
import com.erp.server.sys.service.DictCountryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 国家字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictCountryServiceImpl extends SuperServiceImpl<DictCountryMapper, DictCountryEntity> implements DictCountryService {

    @Override
    public List<DictCountryDTO.ListDTO> listCountry() {
        List<DictCountryEntity> list = this.lambdaQuery().orderByDesc(DictCountryEntity::getIndex).list();
        return BeanMapper.copyList(list, DictCountryDTO.ListDTO.class);
    }
}
