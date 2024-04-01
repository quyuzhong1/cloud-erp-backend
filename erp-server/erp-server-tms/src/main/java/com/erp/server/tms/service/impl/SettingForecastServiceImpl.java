package com.erp.server.tms.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
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
        if (CollectionUtils.isEmpty(list) && CollectionUtils.isNotEmpty(dbList)) {
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
        List<BaseDropDownDTO.DisabledDTO> list = logisticsSupplierService.listAll();
        return list;
    }

    @Override
    public SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(SettingForecastDTO.FindSettingForecastDTO dto) {
        if (Objects.isNull(dto) || StringUtils.isBlank(dto.getLogisticsChannelId())) {
            return null;
        }
        String logisticsChannelId = dto.getLogisticsChannelId();
        LocalDateTime orderTime = dto.getOrderTime();
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
                    if (orderTime.compareTo(enableTransferTime) > 0) {
                        transferStatus = TransferStatusEnum.WAIT.getCode();
                    }
                }
            }

            forecastStatus.setPackageStatus(packageStatus);
            forecastStatus.setTransferStatus(transferStatus);
            String declarePlatform = entity.getDeclarePlatform();
            forecastStatus.setDeclarePlatform(declarePlatform);
            LogisticsPlatformEnum declarePlatformEnum = LogisticsPlatformEnum.getByCode(declarePlatform);
            String declarePlatformName = Objects.nonNull(declarePlatformEnum) ? declarePlatformEnum.getName() : "";
            forecastStatus.setDeclarePlatformName(declarePlatformName);
            return forecastStatus;
        }

        return null;
    }

    @Override
    public Boolean checkIsUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        long count = list.stream().filter(l -> StringUtils.isBlank(l.getId())).count();
        if (count > 0) {
            return Boolean.TRUE;
        }
        List<SettingForecastEntity> dbList = this.list();
        if (dbList.size() != list.size()) {
            return Boolean.TRUE;
        }
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

    @Override
    public SettingForecastDTO.ForecastStatusDTO getByLogisticsSupplier(SettingForecastDTO.FindByLogisticsSupplierDTO dto) {
        String logisticsSupplierId = dto.getLogisticsSupplierId();
        if (Objects.isNull(dto) || StringUtils.isBlank(logisticsSupplierId)) {
            return null;
        }

        LocalDateTime orderTime = dto.getOrderTime();
        SettingForecastEntity entity = this.lambdaQuery().eq(SettingForecastEntity::getLogisticsSupplierId, logisticsSupplierId).last("LIMIT 1").one();
        if (Objects.nonNull(entity)) {
            SettingForecastDTO.ForecastStatusDTO forecastStatus = new SettingForecastDTO.ForecastStatusDTO();
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
            String declarePlatform = entity.getDeclarePlatform();
            forecastStatus.setDeclarePlatform(declarePlatform);
            LogisticsPlatformEnum declarePlatformEnum = LogisticsPlatformEnum.getByCode(declarePlatform);
            String declarePlatformName = Objects.nonNull(declarePlatformEnum) ? declarePlatformEnum.getName() : "";
            forecastStatus.setDeclarePlatformName(declarePlatformName);
            return forecastStatus;
        }

        return null;

    }

    @Override
    public SettingForecastEntity getSettingForecastByLogisticsSupplierId(String logisticsSupplierId) {
        return this.lambdaQuery().eq(SettingForecastEntity::getLogisticsSupplierId, logisticsSupplierId).last("LIMIT 1").one();
    }

    /**
     * 根据渠道id查询
     */
    @Override
    public List<SettingForecastEntity> listByLogisticsChannelIdList (List<String> logisticsChannelIdList) {
        if (CollectionUtils.isEmpty(logisticsChannelIdList)) {
            return Collections.EMPTY_LIST;
        }
        return this.lambdaQuery().in(SettingForecastEntity::getLogisticsChannelId,logisticsChannelIdList).list();
    }

    private SettingForecastDTO.SaveOrUpdateDTO getSaveOrUpdateByDb(SettingForecastEntity entity) {
        SettingForecastDTO.SaveOrUpdateDTO dto = new SettingForecastDTO.SaveOrUpdateDTO();
        dto.setId(entity.getId());
        dto.setLogisticsSupplierId(entity.getLogisticsSupplierId());
        dto.setLogisticsChannelId(entity.getLogisticsChannelId());
        dto.setIsMustPackage(entity.getIsMustPackage());
        dto.setIsMustTransfer(entity.getIsMustTransfer());
        dto.setEnablePackageTime(entity.getEnablePackageTime());
        dto.setEnableTransferTime(entity.getEnableTransferTime());
        dto.setTransferLogisticsSupplierId(entity.getTransferLogisticsSupplierId());
        return dto;
    }


    /**
     * 处理数据
     *
     * @param list
     */
    private void handleData(List<SettingForecastEntity> list) {
        //物流商ids
        List<String> logisticsSupplierIdList = list.stream().map(SettingForecastEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());
        if (list.size() != logisticsSupplierIdList.size()) {
            throw new ServiceException("存在重复的物流商");
        }
        //中转商ids
        List<String> transferSupplierIdList = list.stream().map(SettingForecastEntity::getTransferLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = CollectionUtils.isNotEmpty(logisticsSupplierIdList) ? logisticsSupplierService.listByIds(logisticsSupplierIdList) : Collections.emptyList();

        //渠道ids
        List<String> logisticsChannelIdList = list.stream().map(SettingForecastEntity::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.listByIds(logisticsChannelIdList);

        //根据渠道查询

        List<SettingForecastEntity> settingForecastList = this.listByLogisticsChannelIdList(logisticsChannelIdList);

        List<TransferLogisticsSupplierDTO.AuthDTO> transferLogisticsSupplierList = CollectionUtils.isNotEmpty(transferSupplierIdList) ? transferLogisticsSupplierService.listAuthByMainIds(transferSupplierIdList) : Collections.emptyList();
        for (SettingForecastEntity item : list) {
            //渠道验证
            if (StrUtil.isNotBlank(item.getLogisticsChannelId())) {
                //对应渠道
                String logisticsChannelName = logisticsChannelList.stream().filter(obj -> StrUtil.equals(obj.getId(), item.getLogisticsChannelId())).findFirst().map(LogisticsChannelEntity::getName).orElse("");
                item.setLogisticsChannelName(logisticsChannelName);

                //验证渠道是否已使用
                long count = settingForecastList.stream().filter(obj -> StrUtil.equals(obj.getLogisticsChannelId(), item.getLogisticsChannelId()) && !StrUtil.equals(obj.getId(), item.getId())).count();
                if (count > 0) {
                    throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_EXIST,logisticsChannelName);
                }
            }

            String logisticsSupplierId = item.getLogisticsSupplierId();
            String logisticsSupplierName = logisticsSupplierList.stream().filter(l -> l.getId().equals(logisticsSupplierId)).
                    findFirst().map(LogisticsSupplierEntity::getSupplierName).orElse("");
            item.setLogisticsSupplierName(logisticsSupplierName);

            String transferLogisticsSupplierId = item.getTransferLogisticsSupplierId();
            //如果为空就不校验
            if (StringUtils.isBlank(transferLogisticsSupplierId)) {
                continue;
            }
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
