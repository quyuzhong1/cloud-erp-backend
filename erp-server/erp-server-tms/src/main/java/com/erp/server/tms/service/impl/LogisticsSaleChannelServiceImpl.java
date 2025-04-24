package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.server.tms.mapper.LogisticsSaleChannelMapper;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    @Resource
    private OperateLogService operateLogService;
    @Resource
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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销售平台物流渠道单" , logisticsSaleChannelEntity.getCode());
        
        operateLogService.addModuleOperateLog(msg, null, logisticsSaleChannelEntity.getId(), "新增操作");
        

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
        

        // 记录主单操作日志
            log.info("编辑 开始记录销售平台物流渠道单日志数据，单号：【{}】", logisticsSaleChannelEntity.getCode());
            String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsSaleChannelEntity.getCode(), "销售平台物流渠道单");
        
        operateLogService.addModuleOperateLogByObj(old, logisticsSaleChannelEntity, null, logisticsSaleChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean saveOrUpdateSaleChannel(LogisticsSaleChannelEntity logisticsSaleChannelEntity) {
        LambdaQueryWrapper<LogisticsSaleChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsSaleChannelEntity::getLogisticsPlatform, logisticsSaleChannelEntity.getLogisticsPlatform());
        queryWrapper.eq(LogisticsSaleChannelEntity::getCode, logisticsSaleChannelEntity.getCode());
        if (StringUtils.isNotEmpty(logisticsSaleChannelEntity.getOriginCountry())){
            queryWrapper.eq(LogisticsSaleChannelEntity::getOriginCountry, logisticsSaleChannelEntity.getOriginCountry());
        }
        if (StringUtils.isNotEmpty(logisticsSaleChannelEntity.getDestinationCountry())){
            queryWrapper.eq(LogisticsSaleChannelEntity::getDestinationCountry, logisticsSaleChannelEntity.getDestinationCountry());
        }
        if (StringUtils.isNotEmpty(logisticsSaleChannelEntity.getShipmentMethod())){
            queryWrapper.eq(LogisticsSaleChannelEntity::getShipmentMethod, logisticsSaleChannelEntity.getShipmentMethod());
        }
        if (StringUtils.isNotEmpty(logisticsSaleChannelEntity.getPlatformWarehouseCode())){
            queryWrapper.eq(LogisticsSaleChannelEntity::getPlatformWarehouseCode, logisticsSaleChannelEntity.getPlatformWarehouseCode());
        }
        queryWrapper.eq(LogisticsSaleChannelEntity::getIsDeleted, false);
        queryWrapper.last(SqlConstants.LIMIT_1);
        LogisticsSaleChannelEntity one  = baseMapper.selectOne(queryWrapper);
        //检查数据是否存在
        if (Objects.nonNull(one)){
            logisticsSaleChannelEntity.setId(one.getId());
            logisticsSaleChannelEntity.setUpdateTime(LocalDateTime.now());
            return this.updateById(logisticsSaleChannelEntity);
        }
        return this.save(logisticsSaleChannelEntity);
    }

    @Override
    public void updateSaleChannelByPlatform(String logisticsPlatform,Integer channelStatus) {
        if (Objects.nonNull(channelStatus) && StringUtils.isNotEmpty(logisticsPlatform)){
            lambdaUpdate()
                    .eq(LogisticsSaleChannelEntity::getLogisticsPlatform, logisticsPlatform)
                    .eq(LogisticsSaleChannelEntity::getIsDeleted,false)
                    .set(LogisticsSaleChannelEntity::getChannelStatus, channelStatus).update();
        }
    }

    @Override
    public List<SaleChannelDTO> listByType(String platformType,String servicePlatform) {
        return baseMapper.listByType(platformType,servicePlatform);
    }

    /**
     *
     * @param logisticsPlatform
     *
     * @return
     */
    @Override
    public List<LogisticsSaleChannelEntity> listByLogisticsPlatform(String logisticsPlatform,String servicePlatform) {

        return baseMapper.listByLogisticsPlatform(logisticsPlatform,servicePlatform);
    }

    @Override
    public LogisticsSaleChannelEntity getByPlatform(String platform, String code) {
        return this.lambdaQuery().eq(LogisticsSaleChannelEntity::getCode,code).eq(LogisticsSaleChannelEntity::getLogisticsPlatform,platform).last(SqlConstants.LIMIT_1).one();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsSaleChannelEntity logisticsSaleChannelEntity) {
    
    }


}
