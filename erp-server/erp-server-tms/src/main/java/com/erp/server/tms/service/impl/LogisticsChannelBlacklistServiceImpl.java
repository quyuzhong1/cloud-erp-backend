package com.erp.server.tms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsChannelBlacklistDTO;
import com.erp.model.tms.entity.LogisticsChannelBlacklistEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.tms.mapper.LogisticsChannelBlacklistMapper;
import com.erp.server.tms.service.LogisticsChannelBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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


    @Resource
    private SysDictFeign sysDictFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(String channelId, List<LogisticsChannelBlacklistDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        // 数据处理
        List<LogisticsChannelBlacklistEntity> blacklistList = handleData(channelId, list);
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
    public Boolean update(String channelId, List<LogisticsChannelBlacklistDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        // 数据处理
        List<LogisticsChannelBlacklistEntity> updateList = handleData(channelId, list);
        this.removeByChannelIdList(Arrays.asList(channelId));
        return this.saveBatch(updateList);

    }

    @Override
    public List<LogisticsChannelBlacklistDTO.ViewDTO> listByChannelId(String channelId) {
        List<LogisticsChannelBlacklistEntity> dbList = this.listDbByChannelId(channelId);
        Map<String, List<LogisticsChannelBlacklistEntity>> map = dbList.stream().
                collect(Collectors.groupingBy(LogisticsChannelBlacklistEntity::getCountry));
        List<LogisticsChannelBlacklistDTO.ViewDTO> resultList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<LogisticsChannelBlacklistEntity>> item : map.entrySet()) {
            LogisticsChannelBlacklistDTO.ViewDTO view = new LogisticsChannelBlacklistDTO.ViewDTO();
            view.setCountry(item.getKey());
            List<LogisticsChannelBlacklistEntity> cityList = item.getValue();
            Boolean isNotEmpty=CollectionUtils.isNotEmpty(cityList);
            view.setCountry(isNotEmpty?cityList.get(0).getCountry():"");
            view.setCountryName(isNotEmpty?cityList.get(0).getCountryName():"");
            if(isNotEmpty){
                view.setCityList(BeanMapperUtils.copyList(LogisticsChannelBlacklistDTO.CommonViewDTO.class,cityList));
            }
            resultList.add(view);

        }
        return resultList;
    }

    @Override
    public void removeByChannelIdList(List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return;
        }
        this.lambdaUpdate().in(LogisticsChannelBlacklistEntity::getLogisticsChannelId, channelIdList).remove();
    }

    @Override
    public void copy(String channelId, String addChannelId) {
        List<LogisticsChannelBlacklistEntity> list = listDbByChannelId(channelId);
        if (CollectionUtils.isNotEmpty(list)) {
//            List<LogisticsChannelBlacklistEntity> addList = BeanMapperUtils.copyList(LogisticsChannelBlacklistEntity.class, list);
            list.forEach(obj -> {
                obj.setLogisticsChannelId(addChannelId);
                obj.setId(IdWorker.getIdStr());
            });
            this.saveBatch(list);
        }
    }

    public List<LogisticsChannelBlacklistEntity> listDbByChannelId(String channelId) {
        return this.lambdaQuery().eq(LogisticsChannelBlacklistEntity::getLogisticsChannelId, channelId).list();
    }

    /**
     * 处理添加的数据
     *
     * @param list
     */
    private List<LogisticsChannelBlacklistEntity> handleData(String channelId, List<LogisticsChannelBlacklistDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<LogisticsChannelBlacklistEntity> addList = new ArrayList<>(10);
        List<String> countryIdList = list.stream().map(LogisticsChannelBlacklistDTO.AddDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(countryIdList)) {
            countryList = sysDictFeign.listCountryByIds(countryIdList);
        }
        List<String> cityIdList = new ArrayList<>(10);
        for (LogisticsChannelBlacklistDTO.AddDTO item : list) {
            String country = item.getCountry();
            List<LogisticsChannelBlacklistDTO.CommonDTO> cityList = item.getCityList();
            for (LogisticsChannelBlacklistDTO.CommonDTO common : cityList) {
                LogisticsChannelBlacklistEntity add = new LogisticsChannelBlacklistEntity();
                add.setCountry(country);
                add.setLogisticsChannelId(channelId);
                String city = common.getCity();
                add.setCity(city);
                cityIdList.add(city);
                String province = common.getProvince();
                add.setProvince(province);
                cityIdList.add(province);
                String district = common.getDistrict();
                add.setDistrict(district);
                cityIdList.add(district);
                addList.add(add);
            }
        }
        cityIdList = cityIdList.stream().distinct().collect(Collectors.toList());
        List<DictCityEntity> cityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cityIdList)) {
            cityList = sysDictFeign.listCityByIdList(cityIdList);
        }
        for (LogisticsChannelBlacklistEntity item : addList) {
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
        return addList;
    }


}
