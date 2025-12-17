package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.SettingForecastChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.SettingForecastChannelEntity;
import com.erp.server.tms.mapper.SettingForecastChannelMapper;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.SettingForecastChannelService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 预报设置渠道表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-04-02
 */
@Slf4j
@Service
public class SettingForecastChannelServiceImpl extends SuperServiceImpl<SettingForecastChannelMapper, SettingForecastChannelEntity> implements SettingForecastChannelService {
    @Resource
    private LogisticsChannelService logisticsChannelService;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<SettingForecastChannelDTO.AddDTO> addDTOList) {

        //删除原有渠道
        List<String> mainIdList = addDTOList.stream().map(SettingForecastChannelDTO.AddDTO::getMainId).distinct().collect(Collectors.toList());
        deleteDataByMainIdList(mainIdList);

        // 数据处理
        List<SettingForecastChannelEntity> list =   handleData(addDTOList);

        //没数据无需新增
        if (CollectionUtils.isEmpty(list)) {
            return new BaseResultDTO.AddDTO("","");
        }
        log.info("开始新增预报设置渠道单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("预报设置渠道单保存失败");
        }

        return new BaseResultDTO.AddDTO(list.get(0).getId(), list.get(0).getId());
    }


    /**
     * 根据渠道id查询
     */
    @Override
    public List<SettingForecastChannelEntity> listByLogisticsChannelIdList (List<String> logisticsChannelIdList) {
        if (CollectionUtils.isEmpty(logisticsChannelIdList)) {
            return Collections.EMPTY_LIST;
        }
        return this.lambdaQuery().in(SettingForecastChannelEntity::getLogisticsChannelId,logisticsChannelIdList).list();
    }

    @Override
    public List<SettingForecastChannelEntity> listByMainIdList(List<String> mainIdList) {
        if(CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SettingForecastChannelEntity::getMainId,mainIdList).list();
    }

    @Override
    public Boolean deleteByMainIdList(List<String> deleteIdList) {
        if (CollectionUtils.isEmpty(deleteIdList)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(SettingForecastChannelEntity::getMainId,deleteIdList).remove();
    }

    /**
     * @description: 根据主表id删除原有渠道
     * @author Will
     * @date: 2024/4/2 14:49
     * @param mainIdList
     */
    private void deleteDataByMainIdList (List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        this.lambdaUpdate().in(SettingForecastChannelEntity::getMainId,mainIdList).remove();
    }

   /**
    * @description: 新增数据处理
    * @author Will
    * @date: 2024/4/2 15:09
    * @param addDTOList
    * @return List<SettingForecastChannelEntity>
    */
    private List<SettingForecastChannelEntity> handleData(List<SettingForecastChannelDTO.AddDTO> addDTOList) {
        List<SettingForecastChannelEntity> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(addDTOList)) {
            return resultList;
        }
        List<String> allLogisticsChannelIdList = addDTOList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.getLogisticsChannelIdList()))
                .flatMap(obj -> Stream.of(obj.getLogisticsChannelIdList().stream().toArray(String[]::new)))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allLogisticsChannelIdList)) {
            return resultList;
        }
        //根据渠道查询已存在数据
        List<SettingForecastChannelEntity> oldList = this.listByLogisticsChannelIdList(allLogisticsChannelIdList);

        //根据渠道查询物流渠道数据
        List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.listByIds(allLogisticsChannelIdList);
        JSONObject exist = new JSONObject();
        for (SettingForecastChannelDTO.AddDTO addDTO : addDTOList) {
            List<String> logisticsChannelIdList = addDTO.getLogisticsChannelIdList();
            if (CollectionUtils.isEmpty(logisticsChannelIdList)) {
                continue;
            }
            for (String logisticsChannelId : logisticsChannelIdList) {
                SettingForecastChannelEntity entity = new SettingForecastChannelEntity();
                String logisticsChannelName = logisticsChannelList.stream().filter(obj -> CharSequenceUtil.equals(logisticsChannelId, obj.getId())).map(LogisticsChannelEntity::getName).findFirst().orElse(null);
                if (CharSequenceUtil.isBlank(logisticsChannelName)) {
                    log.error("物流渠道不存在，logisticsChannelId={}",logisticsChannelId);
                    throw new ServiceException("选择物流渠道不存在");
                }
                long count = oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsChannelId(), logisticsChannelId)).count();
                if (count > 1) {
                    throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_EXIST,logisticsChannelName);
                }
                if (ObjectUtil.isNotEmpty(exist.get(logisticsChannelId))) {
                    throw new ServiceException(CharSequenceUtil.format("物流渠道【{}】不可重复选择，请选择其他物流渠道",logisticsChannelName));
                }
                entity.setLogisticsChannelName(logisticsChannelName);
                entity.setLogisticsChannelId(logisticsChannelId);
                entity.setMainId(addDTO.getMainId());
                resultList.add(entity);
                //用于判断是否重复选择
                exist.set(logisticsChannelId,logisticsChannelName);
            }
        }
        return resultList;
    }


}
