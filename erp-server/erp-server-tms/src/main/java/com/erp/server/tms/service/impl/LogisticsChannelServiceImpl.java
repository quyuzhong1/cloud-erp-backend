package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.UnitEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateRefChannelEntity;
import com.erp.model.tms.enums.PaperSizeEnum;
import com.erp.server.tms.mapper.LogisticsChannelMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

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
    private CommonService commonService;

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
    private ShippingTemplateRefChannelService shippingTemplateRefChannelService;


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
        shippingTemplateRefChannelService.addRef(channelId,templateId);
        //平台物流映射
        logisticsMappingService.add(channelId, addDTO.getMappingList());
        //面单设置 打印类型
        logisticsPrintTypeService.add(channelId, addDTO.getPrintTypeList());
        //物流地址
        logisticsChannelAddressService.add(channelId, addDTO.getAddressList());
        //发货限制 黑名单
        logisticsChannelBlacklistService.add(channelId, addDTO.getBlackList());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "物流渠道单", logisticsChannelEntity.getCode());
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
        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsChannelEntity.getCode(), "物流渠道单");
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelEntity, null, logisticsChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel() {
        return baseMapper.listLogisticsChannel();
    }

    @Override
    public List<LogisticsChannelDTO.BaseDTO> listBaseBySourceIdList(List<String> sourceIdList) {
        List<LogisticsChannelEntity> list = this.listDbBySourceIdList(sourceIdList);
        List<LogisticsChannelDTO.BaseDTO> resultList = new ArrayList<>(list.size());
        List<String> channelIdList = list.stream().map(LogisticsChannelEntity::getId).collect(Collectors.toList());
        List<ShippingTemplateRefChannelEntity> shippingTemplateList = shippingTemplateRefChannelService.listChannelIdList(channelIdList);
        for (LogisticsChannelEntity item : list) {
            LogisticsChannelDTO.BaseDTO base = new LogisticsChannelDTO.BaseDTO();
            base.setCode(item.getCode());
            base.setDisabled(item.getDisabled());
            base.setId(item.getId());
            base.setName(item.getName());
            base.setSourceId(item.getSourceId());
            base.setSortingCode(item.getSortingCode());
            Integer effectiveTime = item.getEffectiveTime();
            String timeUnit = item.getEffectiveTimeUnit();
            String timeUnitName = UnitEnum.getName(timeUnit);
            base.setEffectiveTimeStr(effectiveTime.toString().concat(timeUnitName));
            String id = item.getId();
            String ShippingTemplateName = shippingTemplateList.stream().filter(s -> s.getLogisticsChannelId().equals(id)).
                    map(ShippingTemplateRefChannelEntity::getShippingTemplateName).findFirst().orElse("");
            base.setShippingTemplateName(ShippingTemplateName);
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

    private List<LogisticsChannelEntity> listDbBySourceIdList(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsChannelEntity::getSourceId, sourceIdList).list();
    }

    private List<LogisticsChannelEntity> listDbBySourceId(String sourceId) {
        return this.lambdaQuery().eq(LogisticsChannelEntity::getSourceId, sourceId).orderByDesc(LogisticsChannelEntity::getCreateTime).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsChannelEntity logisticsChannelEntity) {
        String sourceId = logisticsChannelEntity.getSourceId();
        LogisticsSupplierEntity logisticsSupplier = logisticsSupplierService.getById(sourceId);
        if (Objects.isNull(logisticsSupplier)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        //纸张大小
        String paperSize = logisticsChannelEntity.getPaperSize();
        PaperSizeEnum paperSizeEnum = PaperSizeEnum.getByCode(paperSize);
        if (Objects.nonNull(paperSizeEnum)) {
            logisticsChannelEntity.setPaperLength(paperSizeEnum.getLength());
            logisticsChannelEntity.setPaperWidth(paperSizeEnum.getWidth());
        }

    }
}
