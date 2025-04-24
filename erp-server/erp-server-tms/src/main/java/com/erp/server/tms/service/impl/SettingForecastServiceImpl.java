package com.erp.server.tms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.tms.dto.SettingForecastChannelDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.TransferLogisticsAuthStatusEnum;
import com.erp.server.tms.mapper.SettingForecastMapper;
import com.erp.server.tms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    @Resource
    private SettingForecastChannelService settingForecastChannelService;

    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;

    @Override
    public List<SettingForecastDTO.ListDTO> listAll() {
        List<SettingForecastEntity> list = this.lambdaQuery().orderByAsc(SettingForecastEntity::getId).list();
        List<SettingForecastDTO.ListDTO> resultList = BeanMapperUtils.copyList(SettingForecastDTO.ListDTO.class, list);
        //数据处理
        handleListData(resultList);
        return resultList;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        List<SettingForecastEntity> dbList = this.list();
        if (CollectionUtils.isEmpty(list) && CollectionUtils.isNotEmpty(dbList)) {
            this.delete(dbList.stream().map(SettingForecastEntity::getId).collect(Collectors.toList()));
            return true;
        }
        List<SettingForecastEntity> saveOrUpdateList = BeanMapperUtils.copyList(SettingForecastEntity.class, list);
        handleData(saveOrUpdateList);
        //存在的
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.delete(deleteIdList);
        }
        // 数据处理
        boolean save = this.saveOrUpdateBatch(saveOrUpdateList);
        if (!save) {
            throw new ServiceException("保存失败！");
        }
        //新增渠道
        List<SettingForecastChannelDTO.AddDTO> addDTOList = saveOrUpdateList.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getLogisticsChannelIdList())).map(obj -> new SettingForecastChannelDTO.AddDTO(obj.getId(), obj.getLogisticsChannelIdList())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addDTOList)) {
            settingForecastChannelService.add(addDTOList);
        }
        return save;
    }

    @Override
    public Boolean delete(List<String> idList) {
        List<String> deleteIdList = idList.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            //删除渠道
            settingForecastChannelService.deleteByMainIdList(deleteIdList);
            return this.removeByIds(deleteIdList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listLogisticsSupplier() {
        List<BaseDropDownDTO.DisabledDTO> list = logisticsSupplierService.listAll(false);
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
            forecastStatus.setTransferLogisticsChannelId(entity.getTransferLogisticsChannelId());
            forecastStatus.setTransferLogisticsChannelName(entity.getTransferLogisticsChannelName());
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
        SettingForecastEntity entity = this.lambdaQuery().eq(SettingForecastEntity::getLogisticsSupplierId, logisticsSupplierId).last(SqlConstants.LIMIT_1).one();
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
    public SettingForecastEntity getSettingForecastByLogisticsSupplierId(String logisticsSupplierId) {
        return this.lambdaQuery().eq(SettingForecastEntity::getLogisticsSupplierId, logisticsSupplierId).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    public List<SettingForecastEntity> getSettingForecastByLogisticsSupplierIdList(List<String> logisticsSupplierId) {
        return this.lambdaQuery().in(SettingForecastEntity::getLogisticsSupplierId, logisticsSupplierId).list();
    }


    private SettingForecastDTO.SaveOrUpdateDTO getSaveOrUpdateByDb(SettingForecastEntity entity) {
        SettingForecastDTO.SaveOrUpdateDTO dto = new SettingForecastDTO.SaveOrUpdateDTO();
        dto.setId(entity.getId());
        dto.setLogisticsSupplierId(entity.getLogisticsSupplierId());
        dto.setIsMustPackage(entity.getIsMustPackage());
        dto.setIsMustTransfer(entity.getIsMustTransfer());
        dto.setEnablePackageTime(entity.getEnablePackageTime());
        dto.setEnableTransferTime(entity.getEnableTransferTime());
        dto.setTransferLogisticsSupplierId(entity.getTransferLogisticsSupplierId());

        List<SettingForecastChannelEntity> settingForecastChannelList = settingForecastChannelService.listByMainIdList(Arrays.asList(dto.getId()));
        //渠道id集合
        List<String> logisticsChannelIdList = settingForecastChannelList.stream().filter(obj -> CharSequenceUtil.equals(dto.getId(), obj.getMainId()))
                .map(SettingForecastChannelEntity::getLogisticsChannelId).collect(Collectors.toList());
        dto.setLogisticsChannelIdList(logisticsChannelIdList);
        dto.setTransferLogisticsChannelId(entity.getTransferLogisticsChannelId());
        dto.setIsAutoForecast(entity.getIsAutoForecast());
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

        //中转商ids
        List<String> transferSupplierIdList = list.stream().map(SettingForecastEntity::getTransferLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = CollectionUtils.isNotEmpty(logisticsSupplierIdList) ? logisticsSupplierService.listByIds(logisticsSupplierIdList) : Collections.emptyList();
        List<TransferLogisticsSupplierDTO.AuthDTO> transferLogisticsSupplierList = CollectionUtils.isNotEmpty(transferSupplierIdList) ? transferLogisticsSupplierService.listAuthByMainIds(transferSupplierIdList) : Collections.emptyList();

        //中转渠道id
        List<String> transferChannelIdList = list.stream().map(SettingForecastEntity::getTransferLogisticsChannelId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<TransferLogisticsChannelEntity> transferLogisticsChannelEntityList = transferLogisticsChannelService.listByIds(transferChannelIdList);
        for (SettingForecastEntity item : list) {
            String logisticsSupplierId = item.getLogisticsSupplierId();
            String logisticsSupplierName = logisticsSupplierList.stream().filter(l -> l.getId().equals(logisticsSupplierId)).
                    findFirst().map(LogisticsSupplierEntity::getSupplierName).orElse("");
            item.setLogisticsSupplierName(logisticsSupplierName);

            if(item.getIsMustTransfer() && (StringUtils.isBlank(item.getTransferLogisticsSupplierId()) || StringUtils.isBlank(item.getTransferLogisticsChannelId()))){
                throw new ServiceException("强制中转，必须选择中转物流商和渠道");
            }
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
            TransferLogisticsChannelEntity transferLogisticsChannelEntity = transferLogisticsChannelEntityList.stream().filter(l -> l.getId().equals(item.getTransferLogisticsChannelId())).findFirst().orElse(null);
            if(Objects.nonNull(transferLogisticsChannelEntity)){
                item.setTransferLogisticsChannelName(transferLogisticsChannelEntity.getName());
            }

            if(item.getIsAutoForecast() && !item.getIsMustTransfer()){
                throw new ServiceException("启用订单自动预报，必须启用强制中转报关");
            }
        }

    }

    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SettingForecastEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SettingForecastEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());

    }

    /**
     * @description: 列表数据处理
     * @author Will
     * @date: 2024/4/2 15:31
     * @param resultList
     */
    private void handleListData (List<SettingForecastDTO.ListDTO> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<String> mainIdList = resultList.stream().map(SettingForecastDTO.ListDTO::getId).collect(Collectors.toList());
        List<SettingForecastChannelEntity> settingForecastChannelList = settingForecastChannelService.listByMainIdList(mainIdList);

        for (SettingForecastDTO.ListDTO listDTO : resultList) {
            //渠道id集合
            List<String> logisticsChannelIdList = settingForecastChannelList.stream().filter(obj -> CharSequenceUtil.equals(listDTO.getId(), obj.getMainId()))
                    .map(SettingForecastChannelEntity::getLogisticsChannelId).collect(Collectors.toList());
            listDTO.setLogisticsChannelIdList(logisticsChannelIdList);
            //渠道名称集合
            List<String> logisticsChannelNameList = settingForecastChannelList.stream().filter(obj -> CharSequenceUtil.equals(listDTO.getId(), obj.getMainId()))
                    .map(SettingForecastChannelEntity::getLogisticsChannelName).collect(Collectors.toList());
            listDTO.setLogisticsChannelNameList(logisticsChannelNameList);
        }
    }
}
