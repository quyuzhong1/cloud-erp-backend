package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.entity.LogisticsChannelAddressEntity;
import com.erp.model.tms.entity.LogisticsChannelBlacklistEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.mapper.LogisticsChannelBlacklistMapper;
import com.erp.server.tms.service.LogisticsChannelBlacklistService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsChannelBlacklistDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 渠道黑名单表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsChannelBlacklistServiceImpl extends SuperServiceImpl<LogisticsChannelBlacklistMapper, LogisticsChannelBlacklistEntity> implements LogisticsChannelBlacklistService {


    @Autowired
    private SysDictFeign sysDictFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(String channelId, List<LogisticsChannelBlacklistDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsChannelBlacklistEntity> blacklistList = BeanMapperUtils.copyList(LogisticsChannelBlacklistEntity.class, list);
        // 数据处理
        handleData(blacklistList);
        blacklistList.forEach(b -> b.setLogisticsChannelId(channelId));
        boolean save = super.saveBatch(blacklistList);
        if (!save) {
            throw new ServiceException("渠道黑名单表保存失败");
        }
        return save;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(String channelId,List<LogisticsChannelBlacklistDTO.UpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsChannelBlacklistEntity> updateList = BeanMapperUtils.copyList(LogisticsChannelBlacklistEntity.class, list);
        updateList.forEach(b -> b.setLogisticsChannelId(channelId));
        // 数据处理
        handleData(updateList);

        List<LogisticsChannelBlacklistEntity> dbList = this.listDbByChannelId(channelId);
        List<String> updateIdList = updateList.stream().filter(u -> StringUtils.isNotBlank(u.getId())).
                map(LogisticsChannelBlacklistEntity::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream().filter(d -> !updateIdList.contains(d.getId())).map(LogisticsChannelBlacklistEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(updateList);

    }

    @Override
    public List<LogisticsChannelBlacklistDTO.ViewDTO> listByChannelId(String channelId) {
        List<LogisticsChannelBlacklistEntity> dbList = this.listDbByChannelId(channelId);
        return BeanMapperUtils.copyList(LogisticsChannelBlacklistDTO.ViewDTO.class,dbList);
    }

    public List<LogisticsChannelBlacklistEntity> listDbByChannelId(String channelId) {
        return this.lambdaQuery().eq(LogisticsChannelBlacklistEntity::getLogisticsChannelId, channelId).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(List<LogisticsChannelBlacklistEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //国家
        List<String> countryIdList = new ArrayList<>(2);
        List<String> cityIdList = new ArrayList<>(2);
        for (LogisticsChannelBlacklistEntity item : list) {
            String country = item.getCountry();
            if (!countryIdList.contains(country)) {
                countryIdList.add(country);
            }
            String city = item.getCity();
            cityIdList.add(city);
            String province = item.getProvince();
            cityIdList.add(province);
            String district = item.getDistrict();
            cityIdList.add(district);
        }
        List<DictCountryEntity> countryList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(countryIdList)) {
            countryList = sysDictFeign.listCountryByIds(countryIdList);
        }
        List<DictCityEntity> cityList = new ArrayList<>();
        cityIdList = cityIdList.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cityIdList)) {
            cityList = sysDictFeign.listCityByIdList(cityIdList);
        }

        for (LogisticsChannelBlacklistEntity item : list) {
            String country = item.getCountry();
            //国家名称
            String countryName = countryList.stream().filter(c -> c.getId().equals(country)).
                    map(DictCountryEntity::getNameCn).findFirst().orElse("");
            item.setCountryName(countryName);
            String province = item.getProvince();
            //省名称
            String provinceName = cityList.stream().filter(c -> c.getId().equals(province)).
                    map(DictCityEntity::getName).findFirst().orElse("");
            item.setProvinceName(provinceName);

            String city = item.getCity();
            //城市名称
            String cityName = cityList.stream().filter(c -> c.getId().equals(city)).
                    map(DictCityEntity::getName).findFirst().orElse("");
            item.setCityName(cityName);

            String district = item.getDistrict();
            //区名称
            String districtName = cityList.stream().filter(c -> c.getId().equals(district)).
                    map(DictCityEntity::getName).findFirst().orElse("");
            item.setDistrictName(districtName);

        }


    }
}
