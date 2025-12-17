package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsChannelWarehouseDTO;
import com.erp.model.tms.entity.LogisticsChannelWarehouseEntity;
import com.erp.model.tms.enums.LogisticsChannelWarehouseTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.tms.mapper.LogisticsChannelWarehouseMapper;
import com.erp.server.tms.service.LogisticsChannelWarehouseService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 渠道仓库设置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-05-23
 */
@Slf4j
@Service
public class LogisticsChannelWarehouseServiceImpl extends SuperServiceImpl<LogisticsChannelWarehouseMapper, LogisticsChannelWarehouseEntity> implements LogisticsChannelWarehouseService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchUpdate(String channelId,LogisticsChannelWarehouseDTO.BatchUpdateDTO addDTO) {
        //原数据
        LogisticsChannelWarehouseDTO.ViewDTO oldDTO = getByChannelId(channelId);

        String type = addDTO.getType();
        List<String> warehouseIdList = addDTO.getWarehouseIdList();
        List<LogisticsChannelWarehouseDTO.AddDTO> addList = new ArrayList<>();
        if (LogisticsChannelWarehouseTypeEnum.ENUM_ALL.getCode().equals(type)) {
            addList.add(new LogisticsChannelWarehouseDTO.AddDTO("", channelId, type));
        }
        if (LogisticsChannelWarehouseTypeEnum.ENUM_PART.getCode().equals(type)) {
            if (CollectionUtils.isEmpty(warehouseIdList)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_WAREHOUSE_NOT_NULL);
            }
            List<LogisticsChannelWarehouseDTO.AddDTO> warehouseList = warehouseIdList.stream().map(e -> new LogisticsChannelWarehouseDTO.AddDTO(e, channelId, type)).collect(Collectors.toList());
            addList.addAll(warehouseList);
        }
        //删除
        this.removeByChannelIdList(Arrays.asList(channelId));
        //新增
        if (CollectionUtils.isNotEmpty(addList)) {
            List<LogisticsChannelWarehouseEntity> resultList = BeanMapperUtils.copyList(LogisticsChannelWarehouseEntity.class, addList);
            this.saveBatch(resultList);
        }
        //新增数据直接返回
        if (CharSequenceUtil.isNotBlank(oldDTO.getType()) ) {
            return new BaseResultDTO.AddDTO(channelId, channelId);
        }

        //日志
        if (!CharSequenceUtil.equals(oldDTO.getType(),addDTO.getType())) {
            String msg = CharSequenceUtil.format("仓库配置类型由【{}】变更为【{}】", LogisticsChannelWarehouseTypeEnum.getName(oldDTO.getType()),LogisticsChannelWarehouseTypeEnum.getName(addDTO.getType()));
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), channelId, "编辑操作");
        }
        return new BaseResultDTO.AddDTO(channelId, channelId);
    }

    @Override
    public LogisticsChannelWarehouseDTO.ViewDTO getByChannelId(String id) {

        List<LogisticsChannelWarehouseEntity> list = listByChannelIdList(Arrays.asList(id));
        if (CollectionUtils.isEmpty(list)) {
            return new LogisticsChannelWarehouseDTO.ViewDTO();
        }
        LogisticsChannelWarehouseDTO.ViewDTO viewDTO = new LogisticsChannelWarehouseDTO.ViewDTO();
        viewDTO.setLogisticsChannelId(id);
        viewDTO.setType(list.get(0).getType());
        //全部指定则无需返回店铺信息
        if (ShopAuthTypeEnum.ENUM_ALL.getCode().equals(list.get(0).getType())) {
            return viewDTO;
        }
        List<String> warehouseIdList = list.stream().map(LogisticsChannelWarehouseEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = wmsWarehouseFeign.listByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> warehouseNameList = warehouseList.stream().map(WarehouseDTO.ListDTO::getName).collect(Collectors.toList());

        viewDTO.setWarehouseIdList(warehouseIdList);
        viewDTO.setWarehouseNameList(warehouseNameList);
        return viewDTO;
    }

    /**
     * @description: 根据物流渠道id删除
     * @author Will
     * @date: 2024/5/23 10:07
     * @param channelIdList
     */
    private void removeByChannelIdList (List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return;
        }
        this.lambdaUpdate().in(LogisticsChannelWarehouseEntity::getLogisticsChannelId, channelIdList).remove();
    }

    /**
     * @description: 根据物流渠道id集合查询
     * @author Will
     * @date: 2024/5/23 10:30
     * @param channelIdList
     * @return List<LogisticsChannelWarehouseEntity>
     */
    private List<LogisticsChannelWarehouseEntity> listByChannelIdList (List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return Collections.EMPTY_LIST;
        }
        return this.lambdaQuery().in(LogisticsChannelWarehouseEntity::getLogisticsChannelId, channelIdList).list();
    }

}
