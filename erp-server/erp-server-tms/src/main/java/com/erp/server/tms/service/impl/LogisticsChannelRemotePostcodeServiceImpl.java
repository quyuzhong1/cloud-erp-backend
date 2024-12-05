package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.tms.dto.LogisticsChannelRemotePostcodeDTO;
import com.erp.model.tms.entity.LogisticsChannelRemotePostcodeEntity;
import com.erp.model.tms.entity.RemotePostcodeEntity;
import com.erp.model.tms.enums.LogisticsChannelWarehouseTypeEnum;
import com.erp.server.tms.mapper.LogisticsChannelRemotePostcodeMapper;
import com.erp.server.tms.service.LogisticsChannelRemotePostcodeService;
import com.erp.server.tms.service.RemotePostcodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 渠道邮编组设置表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2024-12-05
 */
@Slf4j
@Service
public class LogisticsChannelRemotePostcodeServiceImpl extends SuperServiceImpl<LogisticsChannelRemotePostcodeMapper, LogisticsChannelRemotePostcodeEntity> implements LogisticsChannelRemotePostcodeService {

    @Resource
    private RemotePostcodeService remotePostcodeService;


    @Override
    public LogisticsChannelRemotePostcodeDTO.ViewDTO getByChannelId(String id) {
        LogisticsChannelRemotePostcodeDTO.ViewDTO viewDTO = new  LogisticsChannelRemotePostcodeDTO.ViewDTO();
        viewDTO.setLogisticsChannelId(id);
        List<LogisticsChannelRemotePostcodeEntity> list = lambdaQuery().eq(LogisticsChannelRemotePostcodeEntity::getLogisticsChannelId, id).list();
        if(CollUtil.isNotEmpty(list)){
            if(list.get(0).getType().equals(ShopAuthTypeEnum.ENUM_ALL.getCode())){
                viewDTO.setType(ShopAuthTypeEnum.ENUM_ALL.getCode());
            }else {
                List<String> remotePostcodeIdList = list.stream().map(LogisticsChannelRemotePostcodeEntity::getRemotePostcodeId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                List<RemotePostcodeEntity> remotePostcodeEntity = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, remotePostcodeIdList).list();
                List<String> remotePostcodeNameList = remotePostcodeEntity.stream().map(RemotePostcodeEntity::getName).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                viewDTO.setRemotePostcodeIdList(remotePostcodeIdList);
                viewDTO.setRemotePostcodeNameList(remotePostcodeNameList);
                viewDTO.setType(ShopAuthTypeEnum.ENUM_PART.getCode());
            }
        }
        return viewDTO;
    }

    @Override
    public BaseResultDTO.AddDTO batchUpdate(String channelId, LogisticsChannelRemotePostcodeDTO.ViewDTO remotePostcodeDTO) {
        LogisticsChannelRemotePostcodeDTO.ViewDTO oldDTO = getByChannelId(channelId);

        String type = remotePostcodeDTO.getType();
        List<String> remotePostcodeIdList = remotePostcodeDTO.getRemotePostcodeIdList();
        List<LogisticsChannelRemotePostcodeDTO.AddDTO> addList = new ArrayList<>();
        if (LogisticsChannelWarehouseTypeEnum.ENUM_ALL.getCode().equals(type)) {
            addList.add(new LogisticsChannelRemotePostcodeDTO.AddDTO("", channelId, type));
        }else {
            if (CollectionUtils.isEmpty(remotePostcodeIdList)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_WAREHOUSE_NOT_NULL);
            }
            List<LogisticsChannelRemotePostcodeDTO.AddDTO> warehouseList = remotePostcodeIdList.stream().map(e -> new LogisticsChannelRemotePostcodeDTO.AddDTO(e, channelId, type)).collect(Collectors.toList());
            addList.addAll(warehouseList);
        }

        //删除
        this.removeByChannelIdList(Arrays.asList(channelId));
        //新增
        if (CollectionUtils.isNotEmpty(addList)) {
            List<LogisticsChannelRemotePostcodeEntity> resultList = BeanMapperUtils.copyList(LogisticsChannelRemotePostcodeEntity.class, addList);
            this.saveBatch(resultList);
        }
        return new BaseResultDTO.AddDTO(channelId, channelId);
    }

    @Override
    public void removeByChannelIdList(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        this.lambdaUpdate().in(LogisticsChannelRemotePostcodeEntity::getLogisticsChannelId, list).remove();
    }

    @Override
    public void copy(String id, String addChannelId) {
        List<LogisticsChannelRemotePostcodeEntity> list = lambdaQuery().eq(LogisticsChannelRemotePostcodeEntity::getLogisticsChannelId, id).list();
        if(CollUtil.isNotEmpty(list)){
            list.forEach(obj -> {
                obj.setLogisticsChannelId(addChannelId);
                obj.setId(IdWorker.getIdStr());
            });
            this.saveBatch(list);
        }
    }
}
