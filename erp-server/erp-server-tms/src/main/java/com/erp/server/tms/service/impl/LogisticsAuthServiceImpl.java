package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsAuthDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.server.tms.mapper.LogisticsAuthMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 物流授权表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsAuthServiceImpl extends SuperServiceImpl<LogisticsAuthMapper, LogisticsAuthEntity> implements LogisticsAuthService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Autowired
    private LogisticsSupplierService logisticsSupplierService;

    @Autowired
    private LogisticsAuthFieldService logisticsAuthFieldService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsAuthDTO.AddDTO addDTO) {
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(addDTO.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        LogisticsAuthEntity logisticsAuthEntity = new LogisticsAuthEntity();
        BeanMapperUtils.copy(addDTO, logisticsAuthEntity);
        // 数据处理
        handleData(logisticsAuthEntity);
        boolean save = super.save(logisticsAuthEntity);
        if (!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        supplierEntity.setAuthTime(LocalDateTime.now());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.ALREADY.getCode());
        logisticsSupplierService.updateById(supplierEntity);
        //保存或者修改授权字段
        logisticsAuthFieldService.saveOrUpdateAuthField(logisticsAuthEntity.getId(), addDTO.getFieldMap());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流授权单", logisticsAuthEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, logisticsAuthEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(logisticsAuthEntity.getId(), logisticsAuthEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsAuthDTO.UpdateDTO updateDTO) {
        LogisticsAuthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权单"));
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(updateDTO.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }

        LogisticsAuthEntity logisticsAuthEntity = BeanMapperUtils.map(LogisticsAuthEntity.class, updateDTO);
        // 数据处理
        handleData(logisticsAuthEntity);
        boolean save = super.updateById(logisticsAuthEntity);
        if (!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        supplierEntity.setAuthTime(LocalDateTime.now());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.ALREADY.getCode());
        //保存或者修改授权字段
        logisticsAuthFieldService.saveOrUpdateAuthField(logisticsAuthEntity.getId(), updateDTO.getFieldMap());
        return Boolean.TRUE;
    }

    @Override
    public LogisticsAuthDTO.ViewDTO view(String id) {
        LogisticsAuthEntity authEntity = this.getById(id);
        if (Objects.isNull(authEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权");
        }
        LogisticsAuthDTO.ViewDTO view = new LogisticsAuthDTO.ViewDTO();
        BeanMapperUtils.copy(authEntity, view);
        List<LogisticsAuthFieldEntity> authFieldList = logisticsAuthFieldService.listByLogisticsAuthId(id);
        Map<String, String> map = new HashMap<>();
        for (LogisticsAuthFieldEntity item : authFieldList) {
            map.put(item.getFieldCode(), item.getFieldValue());
        }
        view.setFieldMap(map);
        return view;
    }

    @Override
    public BatchResultDTO cancel(String id) {
        LogisticsAuthEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权");
        }
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(entity.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        String  authStatus=supplierEntity.getAuthStatus();
        if(!LogisticsAuthStatusEnum.ALREADY.getCode().equals(authStatus)){
            throw new ServiceException(ApiError.ERROR_CANCEL_CONDITION);
        }
        this.removeById(id);
        return BatchResultDTO.success(supplierEntity.getId(), supplierEntity.getSupplierName(), OperationTypeEnum.UPDATE_STATUS);

    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsAuthEntity logisticsAuthEntity) {
        // TODO 验证数据 & 数据赋值
        String mainId = logisticsAuthEntity.getMainId();
        String logisticsPlatform = logisticsAuthEntity.getLogisticsPlatform();
        LogisticsSupplierEntity logisticsSupplier = logisticsSupplierService.getById(mainId);
        LogisticsAuthEntity authEntity = this.getByMainIdAndPlatform(logisticsAuthEntity.getId(), mainId, logisticsPlatform);
        if (Objects.nonNull(authEntity)) {
            throw new ServiceException("物流商该平台授权信息已存在");
        }
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException("物流商不存在");
        }
        logisticsAuthEntity.setName(logisticsSupplier.getSupplierName());

    }

    /**
     * @param id
     * @param mainId
     * @param logisticsPlatform
     * @return
     */
    private LogisticsAuthEntity getByMainIdAndPlatform(String id, String mainId, String logisticsPlatform) {
        return this.lambdaQuery().ne(StringUtils.isNotBlank(id), LogisticsAuthEntity::getId, id).
                eq(LogisticsAuthEntity::getMainId, mainId).
                eq(LogisticsAuthEntity::getLogisticsPlatform, logisticsPlatform).
                last("LIMIT 1").one();
    }
}
