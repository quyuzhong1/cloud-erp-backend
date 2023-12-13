package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.mapper.LogisticsSaleChannelMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.LogisticsService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

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
    @Resource
    private LogisticsRegistry logisticsRegistry;

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
        queryWrapper.last("limit 1");
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
    public List<SaleChannelDTO> listByType(String platformType) {
        return baseMapper.listByType(platformType);
    }

    @Override
    public List<LogisticsSaleChannelEntity> listByDataSource(String platformType, String overseasWarehouseId, Integer status) {
        if (StringUtils.isNotEmpty(platformType) && StringUtils.isNotEmpty(overseasWarehouseId) && Objects.isNull(status)) return Collections.EMPTY_LIST;
        LambdaQueryWrapper<LogisticsSaleChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(platformType)){
            queryWrapper.eq(LogisticsSaleChannelEntity::getLogisticsPlatform, platformType);
        }
        if (StringUtils.isNotEmpty(overseasWarehouseId)){
            queryWrapper.eq(LogisticsSaleChannelEntity::getOverseasWarehouseId, overseasWarehouseId);
        }
        if (Objects.nonNull(status)){
            queryWrapper.eq(LogisticsSaleChannelEntity::getChannelStatus, status);
        }
        queryWrapper.eq(LogisticsSaleChannelEntity::getIsDeleted, false);
        List<LogisticsSaleChannelEntity> logisticsSaleChannelEntities = baseMapper.selectList(queryWrapper);
        return logisticsSaleChannelEntities;
    }

    /**
     *
     * @param logisticsPlatform
     *
     * @return
     */
    @Override
    public List<LogisticsSaleChannelEntity> listByLogisticsPlatform(String logisticsPlatform) {

        return baseMapper.listByLogisticsPlatform(logisticsPlatform);
    }

    @Override
    public LogisticsSaleChannelEntity getByPlatform(String platform, String code) {
        return this.lambdaQuery().eq(LogisticsSaleChannelEntity::getCode,code).eq(LogisticsSaleChannelEntity::getLogisticsPlatform,platform).last("LIMIT 1").one();
    }

    @Async("tmsExecutor")
    @Override
    public void asyncUpdateSaleChannel(Map<String, String> authMap) {
        if (Objects.isNull(authMap)) return;
        if(StringUtils.isBlank(authMap.get("logisticsPlatform"))) return;
        LogisticsService service = logisticsRegistry.getHandler(authMap.get("logisticsPlatform"));
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        chanelQueryVO.setAuthMap(authMap);
        ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
        if (channels.isSuccess()) {
            channels.getData().forEach(logisticsSaleChannelEntity -> {
                this.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
            });
        } else {
            log.error("同步渠道异常：{}",channels.getMsg());
        }
    }



    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsSaleChannelEntity logisticsSaleChannelEntity) {
    // TODO 验证数据 & 数据赋值
    }


}
