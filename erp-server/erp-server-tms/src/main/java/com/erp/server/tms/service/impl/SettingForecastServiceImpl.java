package com.erp.server.tms.service.impl;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.server.tms.mapper.SettingForecastMapper;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.SettingForecastService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Override
    public List<SettingForecastDTO.ListDTO> listAll() {
        List<SettingForecastEntity> list = this.list();
        List<SettingForecastDTO.ListDTO> resultList = BeanMapperUtils.copyList(SettingForecastDTO.ListDTO.class, list);
        return resultList;
    }

    @Override
    public Boolean addOrUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        List<SettingForecastEntity> saveOrUpdateList = BeanMapperUtils.copyList(SettingForecastEntity.class, list);
        //存在的
        List<SettingForecastEntity> dbList = this.list();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        // 数据处理
        handleData(saveOrUpdateList);
        return this.saveOrUpdateBatch(saveOrUpdateList);
    }

    @Override
    public Boolean delete(List<String> idList) {
        List<String> deleteIdList = idList.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            return this.removeByIds(deleteIdList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listLogisticsSupplier() {
        List<SettingForecastEntity> dbList = this.list();
        List<String> dbLogisticsSupplierIdList = dbList.stream().map(SettingForecastEntity::getLogisticsSupplierId).collect(Collectors.toList());
        List<BaseDropDownDTO.DisabledDTO> list = logisticsSupplierService.listAll();
        list.forEach(item -> {
            if (dbLogisticsSupplierIdList.contains(item.getCode())) {
                item.setDisabled(true);
            }
        });
        return list;
    }

    @Override
    public SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(String logisticsChannelId) {
        if (StringUtils.isBlank(logisticsChannelId)) {
            return null;
        }
        SettingForecastEntity entity = baseMapper.getByLogisticsChannelId(logisticsChannelId);

        if (Objects.nonNull(entity)) {
            LocalDateTime now = LocalDateTime.now();
            SettingForecastDTO.ForecastStatusDTO forecastStatus = new SettingForecastDTO.ForecastStatusDTO();
            String packageStatus = PackageStatusEnum.NOT.getCode();
            String transferStatus = TransferStatusEnum.NOT.getCode();
            //是否强制组包
            Boolean isMustPackage = entity.getIsMustPackage();
            LocalDateTime enablePackageTime = entity.getEnablePackageTime();
            if (isMustPackage) {
                if (Objects.isNull(enablePackageTime)) {
                    packageStatus = PackageStatusEnum.WAIT.getCode();
                } else {
                    if (now.compareTo(enablePackageTime) > 0) {
                        packageStatus = PackageStatusEnum.WAIT.getCode();
                    }
                }
            }
            //是否强制中转
            Boolean isMustTransfer = entity.getIsMustTransfer();
            LocalDateTime enableTransferTime = entity.getEnableTransferTime();
            if (isMustTransfer) {
                if (Objects.isNull(enableTransferTime)) {
                    transferStatus = TransferStatusEnum.WAIT.getCode();
                } else {
                    if (now.compareTo(enablePackageTime) > 0) {
                        transferStatus = TransferStatusEnum.WAIT.getCode();
                    }
                }
            }

            forecastStatus.setPackageStatus(packageStatus);
            forecastStatus.setTransferStatus(transferStatus);
            return forecastStatus;
        }

        return null;
    }


    /**
     * 处理数据
     *
     * @param list
     */
    private void handleData(List<SettingForecastEntity> list) {
        //物流商ids
        List<String> logisticsSupplierIdList = list.stream().map(SettingForecastEntity::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = CollectionUtils.isNotEmpty(logisticsSupplierIdList) ? logisticsSupplierService.listByIds(logisticsSupplierIdList) : Collections.emptyList();
        for (SettingForecastEntity item : list) {
            String logisticsSupplierId = item.getLogisticsSupplierId();
            String logisticsSupplierName = logisticsSupplierList.stream().filter(l -> l.getId().equals(logisticsSupplierId)).
                    findFirst().map(LogisticsSupplierEntity::getSupplierName).orElse("");
            item.setLogisticsSupplierName(logisticsSupplierName);
        }

    }

    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SettingForecastEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SettingForecastEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());

    }


}
