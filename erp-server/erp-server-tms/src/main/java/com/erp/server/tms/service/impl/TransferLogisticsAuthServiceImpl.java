package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TransferLogisticsAuthDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthFieldEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.TransferLogisticsAuthMapper;
import com.erp.server.tms.service.*;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 中转报关服务商授权表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferLogisticsAuthServiceImpl extends SuperServiceImpl<TransferLogisticsAuthMapper, TransferLogisticsAuthEntity> implements TransferLogisticsAuthService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;

    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;

    @Resource
    private TransferLogisticsAuthFieldService transferLogisticsAuthFieldService;

    @Lazy
    @Resource
    private AsyncService asyncService;

    @Lazy
    @Resource
    private TransferLogisticsAuthServiceImpl service;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferLogisticsAuthDTO.AddDTO addDTO) {
        TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierService.getById(addDTO.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "中传报关服务商");
        }
        TransferLogisticsAuthEntity logisticsAuthEntity = new TransferLogisticsAuthEntity();
        BeanMapperUtils.copy(addDTO, logisticsAuthEntity);
        // 数据处理
        handleData(logisticsAuthEntity);
        boolean save = super.save(logisticsAuthEntity);
        if (!save) {
            throw new ServiceException("中传报关服务商授权保存失败");
        }
        supplierEntity.setAuthTime(LocalDateTime.now());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.ALREADY.getCode());
        transferLogisticsSupplierService.updateById(supplierEntity);
        //保存或者修改授权字段
        transferLogisticsAuthFieldService.saveOrUpdateAuthField(logisticsAuthEntity.getId(), addDTO.getFieldMap());
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流授权单", logisticsAuthEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, logisticsAuthEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(logisticsAuthEntity.getId(), logisticsAuthEntity.getName());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.UpdateDTO update(TransferLogisticsAuthDTO.UpdateDTO updateDTO) {
        TransferLogisticsAuthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权单"));
        TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierService.getById(updateDTO.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }

        TransferLogisticsAuthEntity logisticsAuthEntity = BeanMapperUtils.map(TransferLogisticsAuthEntity.class, updateDTO);
        // 数据处理
        handleData(logisticsAuthEntity);
        boolean save = super.updateById(logisticsAuthEntity);
        if (!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        supplierEntity.setAuthTime(LocalDateTime.now());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.ALREADY.getCode());
        transferLogisticsSupplierService.updateById(supplierEntity);
        //保存或者修改授权字段
        transferLogisticsAuthFieldService.saveOrUpdateAuthField(logisticsAuthEntity.getId(), updateDTO.getFieldMap());
        return new BaseResultDTO.UpdateDTO(logisticsAuthEntity.getId(), logisticsAuthEntity.getId());

    }

    @Override
    public TransferLogisticsAuthDTO.ViewDTO view(String mainId) {
        TransferLogisticsAuthEntity authEntity = this.getByMainId("", mainId);
        if (Objects.isNull(authEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权");
        }
        TransferLogisticsAuthDTO.ViewDTO view = new TransferLogisticsAuthDTO.ViewDTO();
        BeanMapperUtils.copy(authEntity, view);
        List<TransferLogisticsAuthFieldEntity> authFieldList = transferLogisticsAuthFieldService.listByLogisticsAuthId(authEntity.getId());
        Map<String, String> map = new HashMap<>();
        for (TransferLogisticsAuthFieldEntity item : authFieldList) {
            map.put(item.getFieldCode(), item.getFieldValue());
        }
        view.setFieldMap(map);
        return view;
    }

    @Override
    public TransferLogisticsAuthEntity getByMainId(String id, String mainId) {
        return this.lambdaQuery().ne(StringUtils.isNotBlank(id), TransferLogisticsAuthEntity::getId, id).eq(TransferLogisticsAuthEntity::getMainId, mainId).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    public List<TransferLogisticsAuthEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(TransferLogisticsAuthEntity::getMainId, mainIds).list();
    }

    @Override
    public TransferLogisticsSupplierDTO.AuthDTO getAuthByChannelId(String channelId) {
        return baseMapper.getAuthByChannelId(channelId);
    }

    @Override
    public ApiResult<Object>authLogistics(String id, String logisticsPlatform) {
        TransferLogisticsService logisticService = transferLogisticsRegistry.getHandler(logisticsPlatform);
        if (Objects.isNull(logisticService)){
            return ApiResult.error(-1,"功能未开发");
        }
        Map<String, String> authConfig = this.getLogisticsAuthConfig(id, logisticsPlatform);
        if (CollectionUtils.isEmpty(authConfig)){
            return ApiResult.error(-1,"未找到配置信息");
        }
        return logisticService.authorization(authConfig);
    }

    @Override
    public ApiResult<Object>authLogistics(String logisticsPlatform, Map<String, String> authConfig) {
        TransferLogisticsService logisticService = transferLogisticsRegistry.getHandler(logisticsPlatform);
        if (Objects.isNull(logisticService)){
            return ApiResult.error(-1,"功能未开发");
        }
        return logisticService.authorization(authConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLogisticsAuthStatus(String mainId, String authStatus) {
        TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierService.getById(mainId);
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        supplierEntity.setAuthStatus(authStatus);
        transferLogisticsSupplierService.updateById(supplierEntity);
    }

    public TransferLogisticsAuthEntity getDbByMainId(String mainId){
        return this.lambdaQuery().eq(TransferLogisticsAuthEntity::getMainId, mainId).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String mainId) {
        TransferLogisticsAuthEntity entity = this.getDbByMainId(mainId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权");
        }
        TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierService.getById(mainId);
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        String authStatus = supplierEntity.getAuthStatus();
        if (!LogisticsAuthStatusEnum.ALREADY.getCode().equals(authStatus)) {
            throw new ServiceException(ApiError.ERROR_CANCEL_CONDITION);
        }

        service.removeById(entity.getId());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.NOT.getCode());
        transferLogisticsSupplierService.updateById(supplierEntity);
        return BatchResultDTO.success(supplierEntity.getId(), supplierEntity.getSupplierName(), OperationTypeEnum.UPDATE_STATUS);

    }

    @Override
    public TransferLogisticsAuthDTO.ViewDTO getViewByChannelId(String channelId) {
        TransferLogisticsChannelEntity channelEntity = transferLogisticsChannelService.getById(channelId);
        if (Objects.nonNull(channelEntity)) {
            return this.view(channelEntity.getMainId());

        }

        return new TransferLogisticsAuthDTO.ViewDTO();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(TransferLogisticsAuthEntity transferLogisticsAuthEntity) {
        // 验证数据 & 数据赋值
        String mainId = transferLogisticsAuthEntity.getMainId();
        TransferLogisticsSupplierEntity logisticsSupplier = transferLogisticsSupplierService.getById(mainId);
        TransferLogisticsAuthEntity authEntity = this.getByMainId(transferLogisticsAuthEntity.getId(), mainId);
        if (Objects.nonNull(authEntity)) {
            throw new ServiceException("物流商该平台授权信息已存在");
        }
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException("物流商不存在");
        }
        transferLogisticsAuthEntity.setName(logisticsSupplier.getSupplierName());

    }

    @Override
    public Map<String, String> getTransferLogisticsAuthConfig(String authId,String logisticsPlatform) {
        Map<String, String> map = new HashMap<>();
        List<TransferLogisticsAuthFieldEntity> fieldEntities = null;
        if (StringUtils.isNoneBlank(authId)) {
            map.put("id", authId);
            TransferLogisticsAuthEntity authEntity = this.getById(authId);
            if (Objects.isNull(authEntity)){
                return Collections.emptyMap();
            }
            map.put("logisticsPlatform", authEntity.getLogisticsPlatform());
            fieldEntities = transferLogisticsAuthFieldService.listByLogisticsAuthId(authId);
        }
        if (Objects.nonNull(fieldEntities) && CollectionUtils.isNotEmpty(fieldEntities)) {
            fieldEntities.forEach(logisticsAuthFieldEntity -> {
                map.put(logisticsAuthFieldEntity.getFieldCode(), logisticsAuthFieldEntity.getFieldValue());
            });
        }
        return map;
    }

    @Override
    public List<Map<String, String>> getTransferLogisticsAuthByPlatform(String platform) {
        if (StringUtils.isBlank(platform)) return Collections.emptyList();
        List<TransferLogisticsAuthEntity> list = lambdaQuery().eq(TransferLogisticsAuthEntity::getLogisticsPlatform, platform)
                .eq(TransferLogisticsAuthEntity::getIsDeleted, false).list();
        if (CollectionUtils.isEmpty(list)) return Collections.emptyList();
        List<Map<String, String>> mapList = new ArrayList<>(list.size());
        list.forEach(logisticsAuthEntity -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", logisticsAuthEntity.getId());
            map.put("logisticsPlatform", platform);
            List<TransferLogisticsAuthFieldEntity> fieldEntities = transferLogisticsAuthFieldService.listByLogisticsAuthId(logisticsAuthEntity.getId());
            if (CollectionUtils.isNotEmpty(fieldEntities)) {
                fieldEntities.forEach(logisticsAuthFieldEntity -> {
                    map.put(logisticsAuthFieldEntity.getFieldCode(), logisticsAuthFieldEntity.getFieldValue());
                });
                mapList.add(map);
            }
        });
        return mapList;
    }

    @Override
    public void syncUpdateSaleChannel(String logisticsPlatform, String authId, String mainId) {
        asyncService.asyncUpdateTransferLogisticsChannel(logisticsPlatform, authId, mainId);
    }

    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId,String logisticsPlatform) {
        return getTransferLogisticsAuthConfig(authId,logisticsPlatform);
    }
}
