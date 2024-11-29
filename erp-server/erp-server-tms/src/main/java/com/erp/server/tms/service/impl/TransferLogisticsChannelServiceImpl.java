package com.erp.server.tms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdDTO;
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
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.convert.TransferLogisticsChannelConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.TransferLogisticsChannelMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 中转报关服务商渠道表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferLogisticsChannelServiceImpl extends SuperServiceImpl<TransferLogisticsChannelMapper, TransferLogisticsChannelEntity> implements TransferLogisticsChannelService {
    public static final String Name = "物流渠道单";
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private TransferDeclareService transferDeclareService;
    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;
    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    @Lazy
    private TransferLogisticsChannelServiceImpl service;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferLogisticsChannelDTO.AddDTO addDTO) {
        TransferLogisticsChannelEntity logisticsChannelEntity = new TransferLogisticsChannelEntity();
        BeanMapperUtils.copy(addDTO, logisticsChannelEntity);
        boolean save = super.save(logisticsChannelEntity);
        if (!save) {
            throw new ServiceException("物流渠道单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), Name, logisticsChannelEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_LOGISTICS_CHANNEL.getCode(), logisticsChannelEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsChannelEntity.getId(), logisticsChannelEntity.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferLogisticsChannelDTO.UpdateDTO updateDTO) {
        TransferLogisticsChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, Name));
        TransferLogisticsChannelEntity logisticsChannelEntity = BeanMapperUtils.map(TransferLogisticsChannelEntity.class, updateDTO);

        boolean save = super.updateById(logisticsChannelEntity);
        if (!save) {
            throw new ServiceException("物流渠道更新失败");
        }
        // 记录主单操作日志
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsChannelEntity.getCode(), Name);
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelEntity, ModuleTypeEnum.TRANSFER_LOGISTICS_CHANNEL.getCode(), logisticsChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferLogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(List<String> logisticsSupplierIds) {
        return baseMapper.listLogisticsChannel(logisticsSupplierIds,new ArrayList<>());
    }

    @Override
    public TransferLogisticsChannelDTO.ViewDTO view(String id) {
        TransferLogisticsChannelEntity channelEntity = this.getById(id);
        if (Objects.isNull(channelEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        TransferLogisticsChannelDTO.ViewDTO view = new TransferLogisticsChannelDTO.ViewDTO();
        BeanMapperUtils.copy(channelEntity, view);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        TransferLogisticsChannelEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }

        TransferDeclareEntity declareEntity = transferDeclareService.checkExistByChannelIds(Arrays.asList(id));
        if(ObjectUtil.isNotEmpty(declareEntity)){
            throw new ServiceException(ApiError.ERROR_CHANNEL_QUOTE);
        }
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_LOGISTICS_CHANNEL.getCode(), entity.getId(), "删除盘点计划单数据");

        service.removeById(id);

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        TransferLogisticsChannelEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道");
        }
        Boolean dbDisabled = entity.getDisabled();
        if (dbDisabled.equals(disabled)) {
            throw new ServiceException("存在相同状态");
        }
        entity.setDisabled(disabled);
        this.updateById(entity);
        String msg = CharSequenceUtil.format("用户【{}】修改【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "物流渠道", disabled ? "停用" : "启用");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_LOGISTICS_CHANNEL.getCode(), entity.getId(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DISABLED);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainIdList(List<String> mainIdList) {
        List<TransferLogisticsChannelEntity> transferLogisticsChannelEntities = this.listByMainIds(mainIdList);
        List<String> channelIdList = transferLogisticsChannelEntities.stream().map(TransferLogisticsChannelEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(channelIdList)) {
            TransferDeclareEntity declareEntity = transferDeclareService.checkExistByChannelIds(channelIdList);
            if(ObjectUtil.isNotEmpty(declareEntity)){
                throw new ServiceException(ApiError.ERROR_CHANNEL_QUOTE);
            }

            this.removeByIds(channelIdList);
        }
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listAll() {
        List<TransferLogisticsChannelEntity> list = this.list();
        List<BaseDropDownDTO.DisabledDTO> resultList = TransferLogisticsChannelConverter.INSTANCE.convertByChannelDown(list);
        return resultList;
    }

    @Override
    public List<BaseIdDTO.CodeDTO> listBySupplierId(String supplierId) {
        return baseMapper.listBySupplierId(supplierId);
    }

    @Override
    public List<TransferLogisticsChannelEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferLogisticsChannelEntity::getMainId, mainIds)
                .orderByAsc(TransferLogisticsChannelEntity::getDisabled)
                .orderByDesc(TransferLogisticsChannelEntity::getCreateTime)
                .list();
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listByLogisticsSupplierId(String transferLogisticsSupplierId) {
        List<TransferLogisticsChannelEntity> channelList = this.listByMainIds(Arrays.asList(transferLogisticsSupplierId));
        List<BaseDropDownDTO.DisabledDTO> resultList = TransferLogisticsChannelConverter.INSTANCE.convertByChannelDown(channelList);
        Collections.sort(resultList, Comparator.comparing(BaseDropDownDTO.DisabledDTO::getDisabled));
        return resultList;
    }

    @Override
    public Boolean saveOrUpdateChannel(TransferLogisticsChannelEntity transferLogisticsChannelEntity) {
        LambdaQueryWrapper<TransferLogisticsChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransferLogisticsChannelEntity::getLogisticsPlatform, transferLogisticsChannelEntity.getLogisticsPlatform());
        queryWrapper.eq(TransferLogisticsChannelEntity::getCode, transferLogisticsChannelEntity.getCode());
        queryWrapper.eq(TransferLogisticsChannelEntity::getIsDeleted, false);
        queryWrapper.last(SqlConstants.LIMIT_1);
        TransferLogisticsChannelEntity one  = baseMapper.selectOne(queryWrapper);
        //检查数据是否存在
        if (Objects.nonNull(one)){
            return this.lambdaUpdate()
                    .set(TransferLogisticsChannelEntity::getUpdateTime, LocalDateTime.now())
                    .set(TransferLogisticsChannelEntity::getCode, transferLogisticsChannelEntity.getCode())
                    .set(TransferLogisticsChannelEntity::getName, transferLogisticsChannelEntity.getName())
                    .set(TransferLogisticsChannelEntity::getLogisticsPlatform, transferLogisticsChannelEntity.getLogisticsPlatform())
                    .eq(TransferLogisticsChannelEntity::getId, one.getId())
                    .update();
        } else {
            return this.save(transferLogisticsChannelEntity);
        }
    }

    @Override
    public TransferLogisticsStatusEnum getPlatformTransferStatus(String shippingOrderNo, String transferLogisticsSupplierId) {
        //查询授权信息
        TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", transferLogisticsSupplierId);
        if (ObjectUtil.isEmpty(authEntity)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST);
        }

        TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
        ApiResult<TransferLogisticsOrderDTO> result = service.getOrderByCode(shippingOrderNo, authEntity.getId());
        if (result.getCode() == 200) {
            return result.getData().getOrderStatusEnum();
        }
        return null;
    }

    @Override
    public List<TransferLogisticsChannelDTO.ListSelectDTO> listByTransferChannelIds(List<String> channelIds) {
        return baseMapper.listLogisticsChannel(new ArrayList<>(),channelIds);
    }

    @Override
    public TransferLogisticsChannelDTO.EditDeliveryCountryDTO editDeliveryCountry(String id) {
        TransferLogisticsChannelDTO.EditDeliveryCountryDTO deliveryCountryDTO = new TransferLogisticsChannelDTO.EditDeliveryCountryDTO();
        TransferLogisticsChannelEntity entity = this.getById(id);
        deliveryCountryDTO.setId(entity.getId());
        if (CharSequenceUtil.isBlank(entity.getDeliveryCountry())) {
            return deliveryCountryDTO;
        }
        DictCountryEntity countryEntity = sysUserFeign.getCountryById(entity.getDeliveryCountry());
        if (ObjectUtil.isNotEmpty(countryEntity)) {
            deliveryCountryDTO.setCountryCode(countryEntity.getId());
            deliveryCountryDTO.setCountryName(countryEntity.getNameCn());
        }
        return deliveryCountryDTO;
    }

    @Override
    public Boolean updateDeliveryCountry(TransferLogisticsChannelDTO.EditDeliveryCountryDTO dto) {
        return this.lambdaUpdate()
                .set(TransferLogisticsChannelEntity::getDeliveryCountry, dto.getCountryCode())
                .eq(TransferLogisticsChannelEntity::getId, dto.getId())
                .update();
    }
}
