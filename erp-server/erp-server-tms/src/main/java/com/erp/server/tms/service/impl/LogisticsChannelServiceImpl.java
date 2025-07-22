package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DeliveryTypeEnum;
import com.erp.model.tms.enums.LogisticsMappingTypeEnum;
import com.erp.model.tms.enums.PaperSizeEnum;
import com.erp.model.tms.enums.UnDeliverableDecisionEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.mapper.LogisticsChannelMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流渠道表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsChannelServiceImpl extends SuperServiceImpl<LogisticsChannelMapper, LogisticsChannelEntity> implements LogisticsChannelService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private LogisticsMappingService logisticsMappingService;

    @Resource
    private LogisticsPrintTypeService logisticsPrintTypeService;

    @Resource
    private LogisticsChannelAddressService logisticsChannelAddressService;

    @Resource
    private LogisticsChannelBlacklistService logisticsChannelBlacklistService;

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Resource
    private ShippingTemplateRefChannelService shippingTemplateRefChannelService;

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsChannelConstraintService logisticsChannelConstraintService;

    @Resource
    private LogisticsChannelWarehouseService logisticsChannelWarehouseService;

    @Resource
    private TmsCarrierService tmsCarrierService;
    @Resource
    private LogisticsChannelRemotePostcodeService logisticsChannelRemotePostcodeService;

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsChannelDTO.AddDTO addDTO) {
        LogisticsChannelEntity logisticsChannelEntity = new LogisticsChannelEntity();
        BeanMapperUtils.copy(addDTO, logisticsChannelEntity);
        // 数据处理
        handleData(logisticsChannelEntity);
        boolean save = super.save(logisticsChannelEntity);
        if (!save) {
            throw new ServiceException("物流渠道单保存失败");
        }
        String channelId = logisticsChannelEntity.getId();
        //模板id
        String templateId = addDTO.getShippingTemplateId();
        //保存模板和渠道的关系表
        shippingTemplateRefChannelService.addRef(channelId, templateId);
        //平台物流映射
        logisticsMappingService.add(channelId, addDTO.getMappingList());
        // 仓库映射
        logisticsMappingService.addWarehouseMapping(channelId, addDTO.getWarehouseMappingList());
        //面单设置 打印类型
        logisticsPrintTypeService.add(channelId, addDTO.getPrintTypeList());
        //物流地址
        logisticsChannelAddressService.add(channelId, addDTO.getAddressList());
        //发货限制 黑名单
        logisticsChannelBlacklistService.add(channelId, addDTO.getBlackList());
        //仓库设置
        logisticsChannelWarehouseService.batchUpdate(channelId, addDTO.getWarehouseDTO());
        //邮编组设置
        logisticsChannelRemotePostcodeService.batchUpdate(channelId, addDTO.getRemotePostcodeIdList());
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流渠道单", logisticsChannelEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), logisticsChannelEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsChannelEntity.getId(), logisticsChannelEntity.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsChannelDTO.UpdateDTO updateDTO) {
        LogisticsChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道单"));
        LogisticsChannelEntity logisticsChannelEntity = BeanMapperUtils.map(LogisticsChannelEntity.class, updateDTO);
        handleData(logisticsChannelEntity);
        boolean save = super.updateById(logisticsChannelEntity);
        if (!save) {
            throw new ServiceException("物流渠道更新失败");
        }
        String channelId = updateDTO.getId();
        //平台物流映射
        logisticsMappingService.update(channelId, updateDTO.getMappingList());
        //仓库物流映射
        logisticsMappingService.warehouseUpdate(channelId, updateDTO.getWarehouseMappingList());

        //面单设置 打印类型
        logisticsPrintTypeService.update(channelId, updateDTO.getPrintTypeList());
        //物流地址
        logisticsChannelAddressService.update(channelId, updateDTO.getAddressList());
        //发货限制 黑名单
        logisticsChannelBlacklistService.update(channelId, updateDTO.getBlackList());
        //模板id
        String templateId = updateDTO.getShippingTemplateId();
        //保存模板和渠道的关系表
        shippingTemplateRefChannelService.addRef(channelId, templateId);
        //仓库设置
        logisticsChannelWarehouseService.batchUpdate(channelId, updateDTO.getWarehouseDTO());
        //邮编组设置
        logisticsChannelRemotePostcodeService.batchUpdate(channelId, updateDTO.getRemotePostcodeIdList());

        // 记录主单操作日志
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsChannelEntity.getCode(), "物流渠道单");
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelEntity, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), logisticsChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(LogisticsChannelDTO.ParamDTO dto) {
        return baseMapper.listLogisticsChannel(dto);
    }

    @Override
    public List<LogisticsChannelDTO.BaseDTO> listBaseByMainIdList(List<String> mainIdList, LogisticsSupplierDTO.PagingParamDTO params) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        List<LogisticsChannelEntity> list = baseMapper.listByMainIdsAndName(mainIdList, params);
        List<LogisticsChannelDTO.BaseDTO> resultList = new ArrayList<>(list.size());
        List<String> channelIdList = list.stream().map(LogisticsChannelEntity::getId).collect(Collectors.toList());
        List<ShippingTemplateRefChannelEntity> shippingTemplateList = shippingTemplateRefChannelService.listChannelIdList(channelIdList);
        for (LogisticsChannelEntity item : list) {
            LogisticsChannelDTO.BaseDTO base = LogisticsChannelConverter.INSTANCE.convertToChannelDTO(item);
            String effectiveTime = item.getEffectiveTime();
            String timeUnit = item.getEffectiveTimeUnit();
            String timeUnitName = EnumMessage.getNameByCode(UnitEnum.TimeUnitEnum.class, timeUnit);
            base.setEffectiveTimeStr(effectiveTime.concat(timeUnitName));
            String id = item.getId();
            String ShippingTemplateName = shippingTemplateList.stream().filter(s -> s.getLogisticsChannelId().equals(id)).
                    map(ShippingTemplateRefChannelEntity::getShippingTemplateName).findFirst().orElse("");
            base.setShippingTemplateName(ShippingTemplateName);
            //获取服务商编号
            LogisticsAuthEntity authEntity = logisticsAuthService.getByMainId("", item.getMainId());
            if (ObjectUtil.isNotEmpty(authEntity)) {
                String logisticsPlatform = authEntity.getLogisticsPlatform();
                base.setLogisticsPlatform(logisticsPlatform);
                String printDelivery = LogisticsPlatformEnum.getByCode(logisticsPlatform).getPrintDelivery();
                if ("N".equals(printDelivery)) {
                    base.setIsPrintPlatform(Boolean.FALSE);
                } else {
                    base.setIsPrintPlatform(Boolean.TRUE);
                }
            } else {
                base.setIsPrintPlatform(Boolean.TRUE);
            }

            resultList.add(base);
        }
        return resultList;
    }


    @Override
    public LogisticsChannelDTO.ViewDTO view(String id) {
        LogisticsChannelEntity channelEntity = this.getById(id);
        if (Objects.isNull(channelEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        LogisticsChannelDTO.ViewDTO view = new LogisticsChannelDTO.ViewDTO();
        BeanMapperUtils.copy(channelEntity, view);
        List<ShippingTemplateRefChannelEntity> templateRefChannelList = shippingTemplateRefChannelService.listChannelIdList(Arrays.asList(id));
        String shippingTemplateId = "";
        String shippingTemplateName = "";
        if (CollectionUtils.isNotEmpty(templateRefChannelList)) {
            shippingTemplateId = templateRefChannelList.get(0).getMainId();
            shippingTemplateName = templateRefChannelList.get(0).getShippingTemplateName();
        }
        view.setShippingTemplateId(shippingTemplateId);
        view.setShippingTemplateName(shippingTemplateName);
        //运费超限达标比例展示调整
        if (Objects.nonNull(view.getShipmentOverLimitRate())){
            view.setShipmentOverLimitRate(view.getShipmentOverLimitRate().stripTrailingZeros());
        }
        /**
         * 物流映射列表
         */
        List<LogisticsMappingDTO.ViewDTO> mappingList = logisticsMappingService.listByChannelIdAndType(id, LogisticsMappingTypeEnum.PLATFORM.getCode());
        mappingListFillData(mappingList);

        /**
         * 物流映射列表
         */
        List<LogisticsMappingDTO.ViewDTO> warehouseMappingList = logisticsMappingService.listByChannelIdAndType(id, LogisticsMappingTypeEnum.WAREHOUSE.getCode());

        /**
         * 打印标签类型
         */
        List<LogisticsPrintTypeDTO.ViewDTO> printTypeList = logisticsPrintTypeService.listByChannelId(id);


        /**
         * 地址列表
         */
        List<LogisticsChannelAddressDTO.ViewDTO> addressList = logisticsChannelAddressService.listByChannelId(id);

        /**
         * 发货限制列表
         */
        List<LogisticsChannelBlacklistDTO.ViewDTO> blackList = logisticsChannelBlacklistService.listByChannelId(id);

        /**
         * 仓库设置
         */
        LogisticsChannelWarehouseDTO.ViewDTO warehouseDTO = logisticsChannelWarehouseService.getByChannelId(id);

        /**
         * 邮编组设置
         */
        LogisticsChannelRemotePostcodeDTO.ViewDTO remotePostcodeDTO = logisticsChannelRemotePostcodeService.getByChannelId(id);

        view.setAddressList(addressList);
        view.setBlackList(blackList);
        view.setMappingList(mappingList);
        view.setWarehouseMappingList(warehouseMappingList);
        view.setPrintTypeList(printTypeList);
        view.setWarehouseDTO(warehouseDTO);
        view.setRemotePostcodeIdList(remotePostcodeDTO.getRemotePostcodeIdList());
        view.setRemotePostcodeNameList(remotePostcodeDTO.getRemotePostcodeNameList());
        return view;
    }


    /**
     * mappingList 填充数据
     */
    private void mappingListFillData(List<LogisticsMappingDTO.ViewDTO> mappingList) {

        List<TmsCarrierEntity> carrierList = null;
        if (!mappingList.stream().allMatch(e-> StringUtils.isBlank(e.getCarrierCode()))){
            carrierList = tmsCarrierService.list();
        }
        List<String> saleChannelIds = mappingList.stream().map(LogisticsMappingDTO.ViewDTO::getPlatformLogisticsChannelId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsSaleChannelEntity> logisticsSaleChannelEntities = CollUtil.isNotEmpty(saleChannelIds) ? logisticsSaleChannelService.listByIds(saleChannelIds) : Collections.emptyList();
        Map<String, String> saleChannelMap = logisticsSaleChannelEntities.stream().collect(Collectors.toMap(LogisticsSaleChannelEntity::getId, LogisticsSaleChannelEntity::getCnName));

        for (LogisticsMappingDTO.ViewDTO viewDTO : mappingList) {
            viewDTO.setPlatformLogisticsChannelName(saleChannelMap.get(viewDTO.getPlatformLogisticsChannelId()));
            if (StringUtils.isBlank(viewDTO.getCarrierCode())) {
                continue;
            }
            carrierList.stream()
                    .filter(e -> e.getSalesPlatform().equalsIgnoreCase(viewDTO.getSalesPlatform()) && e.getCode().equalsIgnoreCase(viewDTO.getCarrierCode()))
                    .findFirst()
                    .ifPresent(carrierEntity -> viewDTO.setCarrierName(carrierEntity.getName()));
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        LogisticsChannelEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            return BatchResultDTO.fail(id,id, "不存在");
        }
        List<SoB2cLogisticsEntity> b2cLogisticsList = soB2cFeign.listSoB2cLogisticsByChannelId(id);
        if(CollectionUtils.isNotEmpty(b2cLogisticsList)){
            return BatchResultDTO.fail(id,entity.getCode(), ApiError.ERROR_CHANNEL_QUOTE.msg);
        }
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), entity.getId(), "删除盘点计划单数据");
        removeById(id);
        List<String> channelIdList = Arrays.asList(id);
        //平台物流映射
        logisticsMappingService.removeByChannelIdList(channelIdList);
        //面单设置 打印类型
        logisticsPrintTypeService.removeByChannelIdList(channelIdList);
        //物流地址
        logisticsChannelAddressService.removeByChannelIdList(channelIdList);
        //发货限制 黑名单
        logisticsChannelBlacklistService.removeByChannelIdList(channelIdList);
        //删除模板和渠道的关系表
        shippingTemplateRefChannelService.removeRef(channelIdList);
        //删除偏远邮编组和渠道的关系表
        logisticsChannelRemotePostcodeService.removeByChannelIdList(channelIdList);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        LogisticsChannelEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        Boolean dbDisabled = entity.getDisabled();
        if (dbDisabled.equals(disabled)) {
            throw new ServiceException("存在相同状态");
        }
        entity.setDisabled(disabled);
        this.updateById(entity);
        String msg = CharSequenceUtil.format("用户【{}】运费模板【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "物流渠道", disabled ? "停用" : "启用");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), entity.getId(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DISABLED);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainIdList(List<String> mainIdList) {
        List<LogisticsChannelEntity> channelList = this.listDbByMainIdList(mainIdList);
        List<String> channelIdList = channelList.stream().map(LogisticsChannelEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(channelIdList)) {
            this.removeByIds(channelIdList);
        }
        //平台物流映射
        logisticsMappingService.removeByChannelIdList(channelIdList);
        //面单设置 打印类型
        logisticsPrintTypeService.removeByChannelIdList(channelIdList);
        //物流地址
        logisticsChannelAddressService.removeByChannelIdList(channelIdList);
        //发货限制 黑名单
        logisticsChannelBlacklistService.removeByChannelIdList(channelIdList);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copy(String id) {
        LogisticsChannelEntity channel = this.getById(id);
        if (Objects.isNull(channel)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        String addChannelId = IdWorker.getIdStr();
        channel.setId(addChannelId);
        Boolean result = this.save(channel);

        //平台物流映射
        logisticsMappingService.copy(id, addChannelId);
        //面单设置 打印类型
        logisticsPrintTypeService.copy(id, addChannelId);
        //物流地址
        logisticsChannelAddressService.copy(id, addChannelId);
        //发货限制 黑名单
        logisticsChannelBlacklistService.copy(id, addChannelId);
        //删除偏远邮编组和渠道的关系表
        logisticsChannelRemotePostcodeService.copy(id, addChannelId);
        return result;
    }


    @Override
    public List<BaseDropDownDTO.DisabledDTO> listAll() {
        List<LogisticsChannelEntity> list = this.lambdaQuery().orderByAsc(LogisticsChannelEntity::getDisabled).list();
        List<BaseDropDownDTO.DisabledDTO> resultList = LogisticsChannelConverter.INSTANCE.convertByChannelDown(list);
        Collections.sort(resultList, Comparator.comparing(BaseDropDownDTO.DisabledDTO::getDisabled));

        return resultList;
    }

    @Override
    public List<BaseIdDTO.CodeDTO> listBySupplierId(String supplierId) {
        return baseMapper.listBySupplierId(supplierId);
    }

    @Override
    public List<LogisticsChannelEntity> listByAddressId(String addressId) {
        return this.baseMapper.listByAddressId(addressId);
    }

    @Override
    public List<LogisticsChannelEntity> listBySyncSourceIds(List<String> syncSourceIdList, String mainId) {
        if (CollectionUtils.isEmpty(syncSourceIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(LogisticsChannelEntity::getMainId, mainId)
                .in(CollectionUtils.isNotEmpty(syncSourceIdList),LogisticsChannelEntity::getSyncSourceId, syncSourceIdList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIdList(List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return;
        }
        this.removeByIds(channelIdList);
        //平台物流映射
        logisticsMappingService.removeByChannelIdList(channelIdList);
        //面单设置 打印类型
        logisticsPrintTypeService.removeByChannelIdList(channelIdList);
        //物流地址
        logisticsChannelAddressService.removeByChannelIdList(channelIdList);
        //发货限制 黑名单
        logisticsChannelBlacklistService.removeByChannelIdList(channelIdList);
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listByLogisticsSupplierId(String mainId) {
        List<LogisticsChannelEntity> channelList = this.listDbByMainIdList(Arrays.asList(mainId));
        List<BaseDropDownDTO.DisabledDTO> resultList = LogisticsChannelConverter.INSTANCE.convertByChannelDown(channelList);
        return resultList;
    }

    @Override
    public LogisticsChannelDTO.BaseDTO getInfoById(String channelId) {
        LogisticsChannelEntity entity = this.getById(channelId);
        if (Objects.isNull(entity)) {
            throw  new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        LogisticsChannelDTO.BaseDTO baseDTO = new LogisticsChannelDTO.BaseDTO();
        BeanMapperUtils.copy(entity, baseDTO);
        String mainId = entity.getMainId();
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(mainId);
        if (Objects.nonNull(supplierEntity)) {
            baseDTO.setLogisticsSupplierName(supplierEntity.getSupplierName());
            baseDTO.setLogisticsSupplierShortName(supplierEntity.getShortName());
            baseDTO.setLogisticsSupplierId(supplierEntity.getSupplierId());
            baseDTO.setLogisticsType(supplierEntity.getType().getCode());
            baseDTO.setLogisticsTypeName(supplierEntity.getType().getName());
        }
        return baseDTO;
    }

    @Override
    public List<LogisticsChannelDTO.BaseDTO> listChannelInfoById(List<String> channelIds) {
        List<LogisticsChannelEntity> logisticsChannelEntities = this.listByIds(channelIds);
        if (CollectionUtils.isEmpty(logisticsChannelEntities)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        List<LogisticsChannelDTO.BaseDTO> baseDTOS = BeanMapper.copyList(logisticsChannelEntities, LogisticsChannelDTO.BaseDTO.class);
        List<String> mainIds = baseDTOS.stream().map(req -> req.getMainId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        List<LogisticsSupplierEntity> logisticsSupplierEntities = logisticsSupplierService.listByIds(mainIds);
        for (LogisticsChannelDTO.BaseDTO baseDTO : baseDTOS) {
            baseDTO.setLogisticsSupplierId(baseDTO.getMainId());
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntities.stream().filter(req -> req.getId().equals(baseDTO.getMainId())).findFirst().orElse(null);
            if (Objects.nonNull(logisticsSupplierEntity)) {
                baseDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
                baseDTO.setLogisticsSupplierShortName(logisticsSupplierEntity.getShortName());
                baseDTO.setSupplierId(logisticsSupplierEntity.getSupplierId());

            }
        }
        return baseDTOS;
    }

    /**
     * 匹配 原渠道名称 和 平台渠道名称获取列表
     *
     * @param channelName
     * @return
     */
    @Override
    public List<LogisticsChannelEntity> getChannelByName(String channelName) {
        List<LogisticsChannelEntity> list = baseMapper.getChannelByName(channelName);
        return list;
    }

    @Override
    public List<LogisticsChannelDTO.LogisticsPlatformDTO> listChannelPlatform(List<String> channelIdList) {
        if(CollectionUtils.isEmpty(channelIdList)){
            return Collections.emptyList();
        }
        return baseMapper.listChannelPlatform(channelIdList);
    }

    @Override
    public List<LogisticsChannelDTO.ProvideChannelDTO> getProvideChannel(List<String> channelCodeList, List<String> provideNameList) {
        return baseMapper.getProvideChannel(channelCodeList,provideNameList);
    }

    @Override
    public LogisticsChannelDTO.LogisticsChannelConstraintDTO getLogisticsChannelConstraint(String channelId, String country) {
        LogisticsChannelDTO.LogisticsChannelConstraintDTO result = new LogisticsChannelDTO.LogisticsChannelConstraintDTO();
        if(StringUtils.isBlank(channelId)){
            return result;
        }
        if (StringUtils.isNotBlank(channelId)){
            LogisticsAuthEntity authEntity = logisticsAuthService.getByChannelId(channelId);
            if (Objects.nonNull(authEntity)){
                result.setLogisticsPlatform(authEntity.getLogisticsPlatform());
            }
        }
        //先通过国家+渠道获取
        LogisticsChannelConstraintEntity logisticsChannelConstraintEntity = logisticsChannelConstraintService.getByChannelAndCountry(channelId,country);
        if(Objects.nonNull(logisticsChannelConstraintEntity)){
            BeanUtil.copyProperties(logisticsChannelConstraintEntity,result);
            return result;
        }
        //国家维度获取不到，通过渠道获取
        LogisticsChannelEntity logisticsChannelEntity = this.getById(channelId);
        if(Objects.nonNull(logisticsChannelEntity)){
            BeanUtil.copyProperties(logisticsChannelEntity,result);
            result.setChannelId(logisticsChannelEntity.getId());
            return result;
        }
        return result;
    }

    @Override
    public List<BaseDropDownDTO.Tree> tree(Boolean filterDisabled, String type) {
        List<BaseDropDownDTO.DisabledDTO> supplierDTOList = logisticsSupplierService.listAll(false);
        List<BaseDropDownDTO.Tree> result = BeanUtil.copyToList(supplierDTOList,BaseDropDownDTO.Tree.class);
        if(filterDisabled){
            result = result.stream().filter(v->!v.getDisabled()).collect(Collectors.toList());
        }
        if(StringUtils.isNotBlank(type)){
            result = result.stream().filter(v->type.equals(v.getType())).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(result)){
            return new ArrayList<>();
        }
        // 使用 Comparator 对 disabled 属性进行排序
        Collections.sort(result, Comparator.comparing(BaseDropDownDTO.Tree::getDisabled));
        List<String> supplierList = result.stream().map(BaseDropDownDTO.Tree::getCode).collect(Collectors.toList());
        List<LogisticsChannelEntity> childrenList = this.listDbByMainIdList(supplierList);
        if(filterDisabled){
            childrenList = childrenList.stream().filter(v->!v.getDisabled()).collect(Collectors.toList());
        }
        Map<String,List<LogisticsChannelEntity>> channelMap = childrenList.stream().collect(Collectors.groupingBy(LogisticsChannelEntity::getMainId));
        for (BaseDropDownDTO.Tree tree : result) {
            List<LogisticsChannelEntity> channelList = channelMap.get(tree.getCode());
            if(CollectionUtils.isEmpty(channelList)){
                tree.setChildTreeList(new ArrayList<>());
                continue;
            }
            List<BaseDropDownDTO.ChildTree> childList = new ArrayList<>();
            for (LogisticsChannelEntity channel : channelList) {
                BaseDropDownDTO.ChildTree child = BaseDropDownDTO.ChildTree.builder()
                        .code(channel.getId())
                        .value(channel.getName())
                        .disabled(channel.getDisabled())
                        .build();
                childList.add(child);
            }
            Collections.sort(childList, Comparator.comparing(BaseDropDownDTO.ChildTree::getDisabled));
            tree.setChildTreeList(childList);
        }
        return result;
    }

    @Override
    public List<LogisticsChannelEntity> listByName(List<String> channelNameList) {
        if(CollectionUtils.isEmpty(channelNameList)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(LogisticsChannelEntity::getName, channelNameList).list();
    }

    private List<LogisticsChannelEntity> listDbByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsChannelEntity::getMainId, mainIdList).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsChannelEntity logisticsChannelEntity) {
        String mainId = logisticsChannelEntity.getMainId();
        LogisticsSupplierEntity logisticsSupplier = logisticsSupplierService.getById(mainId);
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }

        //纸张大小
        String paperSize = logisticsChannelEntity.getPaperSize();
        PaperSizeEnum paperSizeEnum = PaperSizeEnum.getByCode(paperSize);
        if (Objects.nonNull(paperSizeEnum)) {
            logisticsChannelEntity.setPaperLength(paperSizeEnum.getLength());
            logisticsChannelEntity.setPaperWidth(paperSizeEnum.getWidth());
        }
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal maxCustomsAmount = logisticsChannelEntity.getMaxCustomsAmount();
        if (Objects.isNull(maxCustomsAmount)) {
            maxCustomsAmount = zero;
        }
        logisticsChannelEntity.setMaxCustomsAmount(maxCustomsAmount);
        BigDecimal minCustomsAmount = logisticsChannelEntity.getMinCustomsAmount();
        if (Objects.isNull(minCustomsAmount)) {
            minCustomsAmount = zero;
        }
        logisticsChannelEntity.setMinCustomsAmount(minCustomsAmount);
        BigDecimal maxWeight = logisticsChannelEntity.getMaxWeight();
        if (Objects.isNull(maxWeight)) {
            maxWeight = zero;
        }
        logisticsChannelEntity.setMaxWeight(maxWeight);
        BigDecimal maxHeight = logisticsChannelEntity.getMaxHeight();
        if (Objects.isNull(maxHeight)) {
            maxHeight = zero;
        }
        logisticsChannelEntity.setMaxHeight(maxHeight);
        BigDecimal maxLength = logisticsChannelEntity.getMaxLength();
        if (Objects.isNull(maxLength)) {
            maxLength = zero;
        }
        logisticsChannelEntity.setMaxLength(maxLength);
        BigDecimal maxWidth = logisticsChannelEntity.getMaxWidth();
        if (Objects.isNull(maxWidth)) {
            maxWidth = zero;
        }
        logisticsChannelEntity.setMaxWidth(maxWidth);
        //长宽高单个值不能为空，需大于0
        if (logisticsChannelEntity.getMaxHeight().compareTo(BigDecimal.ZERO) == 0 || logisticsChannelEntity.getMaxLength().compareTo(BigDecimal.ZERO) == 0
                || logisticsChannelEntity.getMaxWidth().compareTo(BigDecimal.ZERO) == 0){
            //存在空值，校验是否存在非空值，存在则报错
            if (logisticsChannelEntity.getMaxHeight().compareTo(BigDecimal.ZERO) != 0 || logisticsChannelEntity.getMaxLength().compareTo(BigDecimal.ZERO) != 0
                    || logisticsChannelEntity.getMaxWidth().compareTo(BigDecimal.ZERO) != 0){
                throw new ServiceException(ApiError.ERROR_LOGISTICS_MAX_LIMIT_NOT_EMPTY);
            }
        }
        String code = logisticsChannelEntity.getCode();
        if (StringUtils.isNotBlank(code)) {
            LogisticsAuthEntity auth = logisticsAuthService.getByMainId("", mainId);
            String platform = Objects.nonNull(auth) ? auth.getLogisticsPlatform() : "";
            //根据销售平台和渠道code 获取到原生的渠道
            LogisticsSaleChannelEntity saleChannel = logisticsSaleChannelService.getByPlatform(platform, code);
            if (Objects.isNull(saleChannel) && !LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(platform)
                    && !LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(platform)
                    && !LogisticsPlatformEnum.TIK_TOK_FULLY.getCode().equals(platform)
                    && !LogisticsPlatformEnum.CAINIAO.getCode().equals(platform)) {
                throw new ServiceException(ApiError.ERROR_SALES_CHANNEL_NOT_EXIST, logisticsChannelEntity.getName());
            }
        }
        //设置默认值
        String trackQueryType = logisticsChannelEntity.getTrackQueryType();
        if (StringUtils.isBlank(trackQueryType) && StringUtils.isBlank(logisticsChannelEntity.getId())){
            logisticsChannelEntity.setTrackQueryType(TrackQueryTypeEnum.TRANSPORT_NO.getCode());
        }
        String undeliverableDecision = logisticsChannelEntity.getUndeliverableDecision();
        if (StringUtils.isBlank(undeliverableDecision) && StringUtils.isBlank(logisticsChannelEntity.getId())){
            logisticsChannelEntity.setUndeliverableDecision(UnDeliverableDecisionEnum.DESTROY.getCode());
        }
    }

    @Override
    public LogisticsChannelDTO.SignShipDTO getScaleChannelByChannelById(String logisticsChannelId, String dictPlatform) {
        LogisticsChannelEntity channelEntity = this.getById(logisticsChannelId);
        if (null == channelEntity){
            throw new ServiceException(ApiError.NOT_EXIST, "物流渠道id："+logisticsChannelId+"");
        }
        if (StringUtils.isBlank(dictPlatform)){
            throw new ServiceException("关联的销售平台不能为空");
        }
        if(dictPlatform.equals(PlatformDictEnum.TE_MU.getCode()) || dictPlatform.equals(PlatformDictEnum.RAKUTEN.getCode()) || dictPlatform.equals(PlatformDictEnum.EBAY.getCode())){
            return new LogisticsChannelDTO.SignShipDTO();
        }
        List<LogisticsMappingDTO.ViewDTO> mappingList = logisticsMappingService.listByChannelIdAndType(logisticsChannelId, LogisticsMappingTypeEnum.PLATFORM.getCode());
        if (CollectionUtils.isEmpty(mappingList)){
            throw new ServiceException("物流渠道关联的销售平台物流渠道为空");
        }
        LogisticsMappingDTO.ViewDTO viewDTO = mappingList.stream().filter(e -> e.getSalesPlatform().equalsIgnoreCase(dictPlatform)).findFirst().orElse(null);
        if (null == viewDTO){
            throw new ServiceException("物流渠道关联无对应销售平台物流渠道");
        }
        LogisticsSaleChannelEntity entity = logisticsSaleChannelService.getById(viewDTO.getPlatformLogisticsChannelId());
        if (null == entity){
            throw new ServiceException("对应销售平台物流渠道信息不存在");
        }
        // 承运商代号
        String carrierCode = viewDTO.getCarrierCode();
         // 承运商轨迹查询地址(部分速卖通物流渠道必填)
        String logisticsTrackUrl = "";
        if (StringUtils.isNotBlank(carrierCode)){
            TmsCarrierEntity carrierEntity = tmsCarrierService.getByCodeAndSalesPlatform(carrierCode, dictPlatform);
            if (null != carrierEntity){
                logisticsTrackUrl = carrierEntity.getLogisticsTrackUrl();
            }
        }
        return new LogisticsChannelDTO.SignShipDTO(entity.getId(),
                logisticsChannelId,channelEntity.getName(),
                entity.getCode(),
                entity.getCnName(),
                viewDTO.getOrderDeliveryMarkType(),
                carrierCode,
                logisticsTrackUrl
        );
    }
    @Override
    public PagingVO<LogisticsChannelDTO.PagingSelectDTO> pagingSelect(PagingDTO<LogisticsChannelDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        LogisticsChannelDTO.SelectDTO params = dto.getParams();
        IPage<LogisticsChannelDTO.PagingSelectDTO> pagResult = baseMapper.pagingSelect(query, params);
//        List<LogisticsChannelDTO.PagingSelectDTO> records = pagResult.getRecords();
        //排序
//        List<LogisticsChannelDTO.PagingSelectDTO> list = records.stream().sorted(Comparator.comparing(LogisticsChannelDTO.PagingSelectDTO::getDisabled)).collect(Collectors.toList());
//        pagResult.setRecords(list);
        return new PagingVO<>(pagResult);
    }
    /**
     * 根据主表id，更新启用状态
     * @param channelIds
     * @param status
     */
    @Override
    public void updateStatusByIds(List<String> channelIds, Boolean status) {
        if (CollectionUtils.isEmpty(channelIds)){
            return;
        }
        this.lambdaUpdate().in(LogisticsChannelEntity::getId, channelIds).set(LogisticsChannelEntity::getDisabled, status).update();
    }

    /**
     * 发货配置
     * @param dto
     */
    @Override
    public void deliverySetting(LogisticsChannelDTO.DeliveryDTO dto) {
        LogisticsChannelEntity old = this.getById(dto.getId());
        if (null == old){
            throw new ServiceException(ApiError.NOT_EXIST, "物流渠道");
        }
        //更新配置
        this.lambdaUpdate().eq(LogisticsChannelEntity::getId, dto.getId())
                .set(LogisticsChannelEntity::getDeliveryType, dto.getDeliveryType())
                .set(LogisticsChannelEntity::getUndeliverableDecision, dto.getUndeliverableDecision()).update();
        String msgFormat = "由【%s】改为【%s】";
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】修改渠道【{}】发货方式【{}】不可达处理【{}】", UserContext.getDefaultLoginUser().getUserName(),old.getCode(),
                String.format(msgFormat,DeliveryTypeEnum.getName(old.getDeliveryType()), DeliveryTypeEnum.getName(dto.getDeliveryType())),
                String.format(msgFormat, UnDeliverableDecisionEnum.getName(old.getUndeliverableDecision()), UnDeliverableDecisionEnum.getName(dto.getUndeliverableDecision())));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), old.getId(), "发货配置");
    }

    @Override
    public List<LogisticsChannelEntity> listByMainId(String id) {
        if (StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(LogisticsChannelEntity::getMainId, id).list();
    }

    @Override
    public List<LogisticsChannelDTO.WarnReportDTO> getWarnReportByChannel(LogisticsBillDetailQueryDTO query) {
        return baseMapper.getWarnReportByChannel(query);
    }

    @Override
    public PagingVO<LogisticsChannelDTO.PagingViewDTO> paging(PagingDTO<LogisticsChannelDTO.PagingParamDTO> dto) {
        LogisticsChannelDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<LogisticsChannelDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<LogisticsChannelDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<LogisticsChannelDTO.PagingViewDTO> list = pageData.getRecords();
        for (LogisticsChannelDTO.PagingViewDTO pagingViewDTO : list) {
            pagingViewDTO.setTypeName(pagingViewDTO.getType().getName());
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public List<LogisticsChannelDTO.ChannelWarehouseDTO> listChannelWarehouse(String platform, String authStatus, String warehousePlatformType, Boolean disabled) {
        return baseMapper.listChannelWarehouse(platform,authStatus,warehousePlatformType,disabled);
    }

    @Override
    public Boolean estimateIsOutOfRangeDelivery(String logisticsChannelId, String country, String postCode) {
        if(StringUtils.isBlank(logisticsChannelId) || StringUtils.isBlank(country) || StringUtils.isBlank(postCode)){
            return false;
        }
        return baseMapper.estimateIsOutOfRangeDelivery(logisticsChannelId,country,postCode);
    }

    @Override
    public void platformSignSetting(LogisticsChannelDTO.PlatformSignSettingDTO dto) {
        LogisticsChannelEntity old = this.getById(dto.getId());
        if (null == old){
            throw new ServiceException(ApiError.NOT_EXIST, "物流渠道");
        }
        //更新配置
        this.lambdaUpdate().eq(LogisticsChannelEntity::getId, dto.getId())
                .set(LogisticsChannelEntity::getIsPlatformShip, dto.getIsPlatformShip()).update();
        String msgFormat = "由【%s】改为【%s】";
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】修改渠道【{}】平台标发【{}】", UserContext.getDefaultLoginUser().getUserName(),old.getCode(),
                String.format(msgFormat,old.getIsPlatformShip()?"是":"否", dto.getIsPlatformShip()?"是":"否"));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), old.getId(), "平台标发");
    }

    @Override
    public List<LogisticsChannelDTO.WarehouseChannelDTO> listWarehouseChannel() {
        //查询所有有匹配系统仓库的海外仓
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class).eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode()).list();
        if(CollectionUtils.isEmpty(overseasProviderEntityList)){
            return new ArrayList<>();
        }
        List<String> authIds = overseasProviderEntityList.stream().map(OverseasProviderEntity::getId).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .in(OverseasProviderWarehouseEntity::getMainId, authIds)
                .ne(OverseasProviderWarehouseEntity::getWarehouseId, "")
                .eq(OverseasProviderWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        if(CollectionUtils.isEmpty(overseasProviderWarehouseEntityList)){
            return new ArrayList<>();
        }
        //查询物流渠道(存在物流-仓库-渠道配置数据)
        List<LogisticsChannelDTO.WarehouseChannelDTO> warehouseChannelDTOS = baseMapper.listWarehouseChannel();
        //查询配置物流平台
        List<LogisticsChannelDTO.WarehouseChannelDTO> warehouseChannelDTOS1 = getDictChannel(overseasProviderEntityList, overseasProviderWarehouseEntityList);
        if (Objects.isNull(warehouseChannelDTOS)){
            warehouseChannelDTOS = warehouseChannelDTOS1;
        }else {
            warehouseChannelDTOS.addAll(warehouseChannelDTOS1);
        }
        if (CollectionUtils.isEmpty(warehouseChannelDTOS)) {
            return Collections.emptyList();
        }
        List<String> warehouseIdList = overseasProviderWarehouseEntityList.stream()
                .map(OverseasProviderWarehouseEntity::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = FeignQuery.getByIds(WarehouseEntity.class,warehouseIdList);
        Map<String, String> warehouseMap = warehouseEntities.stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (oldValue, newValue) -> oldValue));
        warehouseChannelDTOS = warehouseChannelDTOS.stream().filter(v->{
            OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseEntityList.stream()
                    .filter(e -> e.getId().equals(v.getOverseasWarehouseId()))
                    .findFirst()
                    .orElse(null);
            if(Objects.isNull(overseasProviderWarehouseEntity)){
                return false;
            }
            v.setWarehouseId(overseasProviderWarehouseEntity.getWarehouseId());
            String warehouseName = warehouseMap.get(v.getWarehouseId());
            if (StringUtils.isBlank(warehouseName)){
                return false;
            }
            v.setWarehouseName(warehouseName);
            return true;
        }).collect(Collectors.toList());
        return warehouseChannelDTOS;
    }

    private List<LogisticsChannelDTO.WarehouseChannelDTO> getDictChannel(List<OverseasProviderEntity> overseasProviderEntityList, List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList) {
        List<LogisticsChannelDTO.WarehouseChannelDTO> warehouseChannelDTOS1 = new ArrayList<>();
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(Collections.singletonList("thirdWarehouseChannel"));
        if (CollUtil.isNotEmpty(dictBasicEntityList)){
            List<String> platformCodeList = dictBasicEntityList.stream().map(DictBasicEntity::getCode).distinct().collect(Collectors.toList());
            //根据物流平台查询渠道列表
            List<LogisticsChannelDTO.PlatformChannelDTO> platformChannelDTOS = logisticsChannelService.listByPlatformCode(platformCodeList);
            List<OverseasProviderEntity> dictProviderList = overseasProviderEntityList.stream().filter(e -> platformCodeList.contains(e.getCode())).collect(Collectors.toList());
            dictProviderList.forEach(e ->{
                //海外仓列表
                List<OverseasProviderWarehouseEntity> warehouseEntityList = overseasProviderWarehouseEntityList.stream().filter(f -> e.getId().equals(f.getMainId())).collect(Collectors.toList());
                warehouseEntityList.forEach(f ->{
                    LogisticsChannelDTO.WarehouseChannelDTO warehouseChannelDTO = new LogisticsChannelDTO.WarehouseChannelDTO();
                    warehouseChannelDTO.setOverseasWarehouseId(f.getId());
                    warehouseChannelDTO.setChannelDTOList(platformChannelDTOS.stream().filter(g -> g.getPlatformCode().equals(e.getCode())).collect(Collectors.toList()));
                    warehouseChannelDTOS1.add(warehouseChannelDTO);
                });
            });
        }
        return warehouseChannelDTOS1;
    }

    @Override
    public List<LogisticsChannelDTO.PlatformChannelDTO> listByPlatformCode(List<String> platformCodeList) {
        if (CollUtil.isEmpty(platformCodeList)){
            return Collections.emptyList();
        }
        return baseMapper.listByPlatformCode(platformCodeList);
    }
}
