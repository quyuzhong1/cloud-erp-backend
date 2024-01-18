package com.erp.server.tms.service.impl;

import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.server.tms.mapper.SettingForecastMapper;
import com.erp.server.tms.service.SettingForecastService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 预报设置 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-18
 */
@Service
public class SettingForecastServiceImpl extends SuperServiceImpl<SettingForecastMapper, SettingForecastEntity> implements SettingForecastService {

    @Override
    public List<SettingForecastDTO.ListDTO> listAll() {
        List<SettingForecastEntity> list = this.list();
        List<SettingForecastDTO.ListDTO> resultList= BeanMapperUtils.copyList(SettingForecastDTO.ListDTO.class,list);
        return resultList;
    }

    @Override
    public Boolean addOrUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        return null;
    }


}
