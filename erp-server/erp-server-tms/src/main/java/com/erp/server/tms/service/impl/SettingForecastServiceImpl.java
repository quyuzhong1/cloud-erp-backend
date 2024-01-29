package com.erp.server.tms.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.validator.ValidList;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.model.tms.enums.TransferLogisticsAuthStatusEnum;
import com.erp.server.tms.mapper.SettingForecastMapper;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.SettingForecastService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Override
    public List<SettingForecastDTO.ListDTO> listAll() {
        List<SettingForecastEntity> list = this.list();
        List<SettingForecastDTO.ListDTO> resultList = BeanMapperUtils.copyList(SettingForecastDTO.ListDTO.class, list);
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        List<SettingForecastEntity> dbList = this.list();
        if (CollectionUtils.isEmpty(list)) {
            this.removeByIds(dbList.stream().map(SettingForecastEntity::getId).collect(Collectors.toList()));
            return true;
        }
        List<SettingForecastEntity> saveOrUpdateList = BeanMapperUtils.copyList(SettingForecastEntity.class, list);
        handleData(saveOrUpdateList);
        //存在的
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        // 数据处理
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
    public SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(String logisticsChannelId, LocalDateTime orderTime) {
        if (StringUtils.isBlank(logisticsChannelId)) {
            return null;
        }
        SettingForecastEntity entity = baseMapper.getByLogisticsChannelId(logisticsChannelId);
        if (Objects.nonNull(entity)) {
            SettingForecastDTO.ForecastStatusDTO forecastStatus = new SettingForecastDTO.ForecastStatusDTO();
            LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(logisticsChannelId);
            if (Objects.nonNull(logisticsChannel)) {
                forecastStatus.setLogisticsChannelName(logisticsChannel.getName());
            }
            if (Objects.isNull(orderTime)) {
                orderTime = LocalDateTime.now();
            }
            String packageStatus = PackageStatusEnum.NOT.getCode();
            String transferStatus = TransferStatusEnum.NOT.getCode();
            //是否强制组包
            Boolean isMustPackage = entity.getIsMustPackage();
            LocalDateTime enablePackageTime = entity.getEnablePackageTime();
            if (isMustPackage) {
                if (Objects.isNull(enablePackageTime)) {
                    packageStatus = PackageStatusEnum.WAIT.getCode();
                } else {
                    if (orderTime.compareTo(enablePackageTime) > 0) {
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
                    if (orderTime.compareTo(enablePackageTime) > 0) {
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

    @Override
    public Boolean checkIsUpdate(ValidList<SettingForecastDTO.SaveOrUpdateDTO> list) {
        long count = list.stream().filter(l -> StringUtils.isBlank(l.getId())).count();
        if (count > 0) {
            return Boolean.TRUE;
        }
        List<SettingForecastEntity> dbList = this.list();
        for (SettingForecastDTO.SaveOrUpdateDTO item : list) {
            String id = item.getId();
            SettingForecastEntity entity = dbList.stream().filter(l -> l.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                return Boolean.TRUE;
            }
            SettingForecastDTO.SaveOrUpdateDTO dbDTO = getSaveOrUpdateByDb(entity);
            String dbJson = JSONUtil.toJsonStr(dbDTO);
            String paramJson = JSONUtil.toJsonStr(item);
            if (!dbJson.equals(paramJson)) {
                return Boolean.TRUE;
            }

        }
        return Boolean.FALSE;
    }

    private SettingForecastDTO.SaveOrUpdateDTO getSaveOrUpdateByDb(SettingForecastEntity entity) {
        SettingForecastDTO.SaveOrUpdateDTO dto = new SettingForecastDTO.SaveOrUpdateDTO();
        dto.setId(entity.getId());
        dto.setLogisticsSupplierId(entity.getLogisticsSupplierId());
        dto.setIsMustPackage(entity.getIsMustPackage());
        dto.setIsMustTransfer(entity.getIsMustTransfer());
        dto.setEnablePackageTime(entity.getEnablePackageTime());
        dto.setEnableTransferTime(entity.getEnableTransferTime());
        return dto;
    }


    /**
     * 处理数据
     *
     * @param list
     */
    private void handleData(List<SettingForecastEntity> list) {
        //物流商ids
        List<String> logisticsSupplierIdList = list.stream().map(SettingForecastEntity::getLogisticsSupplierId).collect(Collectors.toList());
        //中转商ids
        List<String> transferSupplierIdList = list.stream().map(SettingForecastEntity::getTransferLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = CollectionUtils.isNotEmpty(logisticsSupplierIdList) ? logisticsSupplierService.listByIds(logisticsSupplierIdList) : Collections.emptyList();

        List<TransferLogisticsSupplierDTO.AuthDTO> transferLogisticsSupplierList = CollectionUtils.isNotEmpty(transferSupplierIdList) ? transferLogisticsSupplierService.listAuthByMainIds(transferSupplierIdList) : Collections.emptyList();
        for (SettingForecastEntity item : list) {
            String logisticsSupplierId = item.getLogisticsSupplierId();
            String logisticsSupplierName = logisticsSupplierList.stream().filter(l -> l.getId().equals(logisticsSupplierId)).
                    findFirst().map(LogisticsSupplierEntity::getSupplierName).orElse("");
            item.setLogisticsSupplierName(logisticsSupplierName);

            String transferLogisticsSupplierId = item.getTransferLogisticsSupplierId();
            TransferLogisticsSupplierDTO.AuthDTO authDTO = transferLogisticsSupplierList.stream().filter(l -> l.getMainId().equals(transferLogisticsSupplierId)).findFirst().orElse(null);
            if (Objects.isNull(authDTO)) {
                throw new ServiceException(logisticsSupplierName + " 对应的中转物流商不存在");
            }
            String authStatus = authDTO.getAuthStatus();
            String already = TransferLogisticsAuthStatusEnum.ALREADY.getCode();
            String supplierName = authDTO.getSupplierName();
            String declarePlatform = authDTO.getLogisticsPlatform();
            if (!already.equals(authStatus) || StringUtils.isBlank(declarePlatform)) {
                throw new ServiceException(supplierName + " 未授权，请重新授权");
            }
            item.setTransferLogisticsSupplierName(supplierName);
            item.setDeclarePlatform(declarePlatform);
        }

    }

    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SettingForecastEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SettingForecastEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());

    }


}
