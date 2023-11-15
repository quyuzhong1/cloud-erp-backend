package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.server.tms.mapper.LogisticsSaleChannelMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 销售平台物流渠道表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
@Slf4j
@Service
public class LogisticsSaleChannelServiceImpl extends SuperServiceImpl<LogisticsSaleChannelMapper, LogisticsSaleChannelEntity> implements LogisticsSaleChannelService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsSaleChannelDTO.AddDTO addDTO) {
        LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity();
        BeanMapperUtils.copy(addDTO, logisticsSaleChannelEntity);

        // 数据处理
        handleData(logisticsSaleChannelEntity);

        log.info("开始新增销售平台物流渠道单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        logisticsSaleChannelEntity.setCode(code);
        boolean save = super.save(logisticsSaleChannelEntity);
        if(!save) {
            throw new ServiceException("销售平台物流渠道单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "销售平台物流渠道单" , logisticsSaleChannelEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsSaleChannelEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsSaleChannelEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsSaleChannelDTO.UpdateDTO updateDTO) {
        LogisticsSaleChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销售平台物流渠道单"));
        LogisticsSaleChannelEntity logisticsSaleChannelEntity =  BeanMapperUtils.map(LogisticsSaleChannelEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsSaleChannelEntity);
        log.info("编辑 开始修改销售平台物流渠道单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(logisticsSaleChannelEntity);
        if(!save) {
            throw new ServiceException("销售平台物流渠道单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销售平台物流渠道单日志数据，单号：【{}】", logisticsSaleChannelEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsSaleChannelEntity.getCode(), "销售平台物流渠道单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsSaleChannelEntity, null, logisticsSaleChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean saveOrUpdateSaleChannel(LogisticsSaleChannelEntity logisticsSaleChannelEntity) {
        //检查数据是否存在
        LogisticsSaleChannelEntity one = lambdaQuery().eq(LogisticsSaleChannelEntity::getLogisticsPlatform, logisticsSaleChannelEntity.getLogisticsPlatform())
                .eq(LogisticsSaleChannelEntity::getCode, logisticsSaleChannelEntity.getCode()).last("limit 1").one();
        if (Objects.nonNull(one)){
            logisticsSaleChannelEntity.setId(one.getId());
        }
        return this.saveOrUpdate(logisticsSaleChannelEntity);
    }

    @Override
    public List<SaleChannelDTO> listByType(String platformType) {
        return baseMapper.listByType(platformType);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsSaleChannelEntity logisticsSaleChannelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
