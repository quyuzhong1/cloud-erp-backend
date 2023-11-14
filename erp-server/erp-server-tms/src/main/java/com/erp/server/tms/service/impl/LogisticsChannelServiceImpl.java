package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
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
import com.erp.model.tms.dto.LogisticsChannelDTO;

import java.util.*;
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
    private ShippingTemplateService shippingTemplateService;


    @GlobalTransactional(rollbackFor = Exception.class)
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

        // 数据处理
        handleData(logisticsChannelEntity);
        log.info("编辑 开始修改物流渠道单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(logisticsChannelEntity);
        if (!save) {
            throw new ServiceException("物流渠道单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录物流渠道单日志数据，单号：【{}】", logisticsChannelEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsChannelEntity.getCode(), "物流渠道单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelEntity, null, logisticsChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel() {
        return baseMapper.listLogisticsChannel();
    }

    @Override
    public List<LogisticsChannelDTO.BaseDTO> listBaseBySourceId(String sourceId) {
        List<LogisticsChannelEntity> list = this.listDbBySourceId(sourceId);
        List<LogisticsChannelDTO.BaseDTO> resultList = new ArrayList<>(list.size());

        for (LogisticsChannelEntity item : list) {
            LogisticsChannelDTO.BaseDTO base = new LogisticsChannelDTO.BaseDTO();
            base.setCode(item.getCode());
            base.setDisabled(item.getDisabled());
        }
        return null;
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
