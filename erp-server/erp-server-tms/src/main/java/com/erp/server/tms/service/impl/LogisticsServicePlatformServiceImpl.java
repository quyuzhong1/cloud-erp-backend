package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsServicePlatformDTO;
import com.erp.model.tms.entity.LogisticsServicePlatformEntity;
import com.erp.server.tms.mapper.LogisticsServicePlatformMapper;
import com.erp.server.tms.service.LogisticsServicePlatformService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * <p>
 * 物流平台服务表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-04
 */
@Slf4j
@Service
public class LogisticsServicePlatformServiceImpl extends SuperServiceImpl<LogisticsServicePlatformMapper, LogisticsServicePlatformEntity> implements LogisticsServicePlatformService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsServicePlatformDTO.AddDTO addDTO) {
        LogisticsServicePlatformEntity logisticsServicePlatformEntity = new LogisticsServicePlatformEntity();
        BeanMapperUtils.copy(addDTO, logisticsServicePlatformEntity);

        // 数据处理
        handleData(logisticsServicePlatformEntity);

        log.info("开始新增物流平台服务单");
        boolean save = super.save(logisticsServicePlatformEntity);
        if(!save) {
            throw new ServiceException("物流平台服务单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流平台服务单" , logisticsServicePlatformEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, logisticsServicePlatformEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(logisticsServicePlatformEntity.getId(), logisticsServicePlatformEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsServicePlatformDTO.UpdateDTO updateDTO) {
        LogisticsServicePlatformEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流平台服务单"));
        LogisticsServicePlatformEntity logisticsServicePlatformEntity =  BeanMapperUtils.map(LogisticsServicePlatformEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsServicePlatformEntity);
        log.info("编辑 开始修改物流平台服务单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsServicePlatformEntity);
        if(!save) {
            throw new ServiceException("物流平台服务单保存失败");
        }


        // 记录主单操作日志
            log.info("编辑 开始记录物流平台服务单日志数据，id：【{}】", logisticsServicePlatformEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsServicePlatformEntity.getId(), "物流平台服务单");
        
        operateLogService.addModuleOperateLogByObj(old, logisticsServicePlatformEntity, null, logisticsServicePlatformEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsServicePlatformEntity> listByPlatform(String logisticsPlatform) {
        return this.lambdaQuery().eq(LogisticsServicePlatformEntity::getLogisticsPlatform, logisticsPlatform).list();
    }

    @Override
    public List<LogisticsServicePlatformDTO.ServiceNameDTO> listServiceNameByLogisticsPlatform(String logisticsPlatform) {
        List<LogisticsServicePlatformEntity> dbList = listByPlatform(logisticsPlatform);
        List<String> nameList = dbList.stream().map(LogisticsServicePlatformEntity::getServiceName).distinct().collect(Collectors.toList());
        List<LogisticsServicePlatformDTO.ServiceNameDTO> list = new ArrayList<>(nameList.size());
        for (String item : nameList) {
            LogisticsServicePlatformDTO.ServiceNameDTO serviceNameDTO = new LogisticsServicePlatformDTO.ServiceNameDTO();
            serviceNameDTO.setServiceName(item);
            list.add(serviceNameDTO);
        }
        return list;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsServicePlatformEntity logisticsServicePlatformEntity) {

    }
}
