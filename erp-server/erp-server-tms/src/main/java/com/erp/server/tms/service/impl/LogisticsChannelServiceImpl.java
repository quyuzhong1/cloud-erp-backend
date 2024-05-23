package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.PaperSizeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.mapper.LogisticsChannelMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private LogisticsSupplierService logisticsSupplierService;

    @Autowired
    private LogisticsMappingService logisticsMappingService;

    @Autowired
    private LogisticsPrintTypeService logisticsPrintTypeService;

    @Autowired
    private LogisticsChannelAddressService logisticsChannelAddressService;

    @Autowired
    private LogisticsChannelBlacklistService logisticsChannelBlacklistService;

    @Autowired
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Autowired
    private ShippingTemplateRefChannelService shippingTemplateRefChannelService;

    @Autowired
    private LogisticsAuthService logisticsAuthService;

    @Autowired
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsChannelConstraintService logisticsChannelConstraintService;

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
        //面单设置 打印类型
        logisticsPrintTypeService.add(channelId, addDTO.getPrintTypeList());
        //物流地址
        logisticsChannelAddressService.add(channelId, addDTO.getAddressList());
        //发货限制 黑名单
        logisticsChannelBlacklistService.add(channelId, addDTO.getBlackList());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流渠道单", logisticsChannelEntity.getCode());
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

        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsChannelEntity.getCode(), "物流渠道单");
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelEntity, ModuleTypeEnum.LOGISTICS_CHANNEL.getCode(), logisticsChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(LogisticsChannelDTO.ParamDTO dto) {
        return baseMapper.listLogisticsChannel(dto);
    }

    @Override
    public List<LogisticsChannelDTO.BaseDTO> listBaseByMainIdList(List<String> mainIdList, String name) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        List<LogisticsChannelEntity> list = this.lambdaQuery().
                in(LogisticsChannelEntity::getMainId, mainIdList).
                like(StringUtils.isNotBlank(name), LogisticsChannelEntity::getName, name).
                orderByAsc(LogisticsChannelEntity::getDisabled).
                orderByDesc(LogisticsChannelEntity::getCreateTime).
                list();
        List<LogisticsChannelDTO.BaseDTO> resultList = new ArrayList<>(list.size());
        List<String> channelIdList = list.stream().map(LogisticsChannelEntity::getId).collect(Collectors.toList());
        List<ShippingTemplateRefChannelEntity> shippingTemplateList = shippingTemplateRefChannelService.listChannelIdList(channelIdList);
        for (LogisticsChannelEntity item : list) {
            LogisticsChannelDTO.BaseDTO base = new LogisticsChannelDTO.BaseDTO();
            base.setCode(item.getCode());
            base.setDisabled(item.getDisabled());
            base.setId(item.getId());
            base.setName(item.getName());
            base.setMainId(item.getMainId());
            base.setSortingCode(item.getSortingCode());
            String effectiveTime = item.getEffectiveTime();
            String timeUnit = item.getEffectiveTimeUnit();
            String timeUnitName = EnumMessage.getNameByCode(UnitEnum.TimeUnitEnum.class, timeUnit);
            base.setEffectiveTimeStr(effectiveTime.concat(timeUnitName));
            String id = item.getId();
            String ShippingTemplateName = shippingTemplateList.stream().filter(s -> s.getLogisticsChannelId().equals(id)).
                    map(ShippingTemplateRefChannelEntity::getShippingTemplateName).findFirst().orElse("");
            base.setShippingTemplateName(ShippingTemplateName);
            base.setSourceId(item.getSourceId());

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
            new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
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
        /**
         * 物流映射列表
         */
        List<LogisticsMappingDTO.ViewDTO> mappingList = logisticsMappingService.listByChannelId(id);

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
        view.setAddressList(addressList);
        view.setBlackList(blackList);
        view.setMappingList(mappingList);
        view.setPrintTypeList(printTypeList);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        LogisticsChannelEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        List<SoB2cLogisticsEntity> b2cLogisticsList = soB2cFeign.listSoB2cLogisticsByChannelId(id);
        if(CollectionUtils.isNotEmpty(b2cLogisticsList)){
            new ServiceException(ApiError.ERROR_CHANNEL_QUOTE);
        }
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
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

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        LogisticsChannelEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        Boolean dbDisabled = entity.getDisabled();
        if (dbDisabled.equals(disabled)) {
            throw new ServiceException("存在相同状态");
        }
        entity.setDisabled(disabled);
        this.updateById(entity);
        String msg = StrUtil.format("用户【{}】运费模板【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "物流渠道", disabled ? "停用" : "启用");
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
            new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
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
        return this.lambdaQuery().eq(LogisticsChannelEntity::getMainId, mainId).in(LogisticsChannelEntity::getSyncSourceId, syncSourceIdList).list();
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
        }
        return baseDTO;
    }

    @Override
    public List<LogisticsChannelDTO.BaseDTO> listChannelInfoById(List<String> channelIds) {
        List<LogisticsChannelEntity> logisticsChannelEntities = this.listByIds(channelIds);
        if (CollectionUtils.isEmpty(logisticsChannelEntities)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
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
    public List<BaseDropDownDTO.Tree> tree() {
        List<BaseDropDownDTO.DisabledDTO> supplierDTOList = logisticsSupplierService.listAll();
        List<BaseDropDownDTO.Tree> result = BeanUtil.copyToList(supplierDTOList,BaseDropDownDTO.Tree.class);
        if(CollectionUtils.isEmpty(result)){
            return new ArrayList<>();
        }
        // 使用 Comparator 对 disabled 属性进行排序
        Collections.sort(result, Comparator.comparing(BaseDropDownDTO.Tree::getDisabled));
        List<String> supplierList = result.stream().map(BaseDropDownDTO.Tree::getCode).collect(Collectors.toList());
        List<LogisticsChannelEntity> childrenList = this.listDbByMainIdList(supplierList);
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

    @Override
    public LogisticsChannelDTO.SignShipDTO getSignShipInfoByChannelId(String channelId) {
        LogisticsChannelDTO.SignShipDTO signShipDTO = new LogisticsChannelDTO.SignShipDTO();
        LogisticsChannelEntity channelEntity = this.getById(channelId);
        String code = "";
        if (Objects.nonNull(channelEntity)) {
            signShipDTO.setLogisticsChannelId(channelEntity.getId());
            code = channelEntity.getCode();
            signShipDTO.setCode(code);
        }
        LogisticsSaleChannelEntity saleChannelEntity = logisticsSaleChannelService.getByCode(code);
        if (Objects.nonNull(saleChannelEntity)) {
            signShipDTO.setSaleChannelSupplierName(saleChannelEntity.getSupplierName());
        }
        return signShipDTO;

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
            if (Objects.isNull(saleChannel)) {
                throw new ServiceException(ApiError.ERROR_SALES_CHANNEL_NOT_EXIST, logisticsChannelEntity.getName());
            }
        }

    }

    @Override
    public LogisticsChannelDTO.SignShipDTO getScaleChannelByChannelById(String logisticsChannelId, String dictPlatform) {
        LogisticsChannelEntity channelEntity = this.getById(logisticsChannelId);
        if (null == channelEntity){
            throw new ServiceException(ApiError.NOT_EXIST, "物流渠道");
        }
        if (StringUtils.isBlank(dictPlatform)){
            throw new ServiceException("关联的销售平台不能为空");
        }
        List<LogisticsMappingDTO.ViewDTO> mappingList = logisticsMappingService.listByChannelId(logisticsChannelId);
        if (CollectionUtils.isEmpty(mappingList)){
            throw new ServiceException("物流渠道关联的销售平台物流渠道为空");
        }
        LogisticsMappingDTO.ViewDTO viewDTO = mappingList.stream().filter(e -> e.getSalesPlatform().equalsIgnoreCase(dictPlatform)).findFirst().orElse(null);
        if (null == viewDTO){
            throw new ServiceException("物流渠道关联无对应销售平台物流渠道");
        }
        LogisticsSaleChannelEntity entity = logisticsSaleChannelService.getById(viewDTO.getLogisticsSaleChannelId());
        if (null == entity){
            throw new ServiceException("对应销售平台物流渠道信息不存在");
        }
        return new LogisticsChannelDTO.SignShipDTO(entity.getId(),logisticsChannelId, entity.getCode(), entity.getCnName(), viewDTO.getOrderDeliveryMarkType());
    }
}
