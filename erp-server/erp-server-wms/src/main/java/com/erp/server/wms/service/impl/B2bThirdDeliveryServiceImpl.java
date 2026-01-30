package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.B2BDeliveryPushTypeEnum;
import com.erp.model.wms.enums.B2BThirdDeliveryCancelResultEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.model.wms.enums.WarehouseOperationTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.B2bThirdDeliveryMapper;
import com.erp.server.wms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_B2B_THIRD_DELIVERY_REPORT;

/**
 * <p>
 * B2B三方发货单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@Service
public class B2bThirdDeliveryServiceImpl extends SuperServiceImpl<B2bThirdDeliveryMapper, B2bThirdDeliveryEntity> implements B2bThirdDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private B2bThirdDeliveryDetailService b2bThirdDeliveryDetailService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private SyncB2bThirdWarehouseService syncB2bThirdWarehouseService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private CustomerFeign customerFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Lazy
    @Resource
    private B2bThirdDeliveryService service;

    @Resource
    private LogisticsProductFeign logisticsProductFeign;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_SECONDS = 10000;

    @Resource
    private TransactionTemplate transactionTemplate;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(B2bThirdDeliveryDTO.AddDTO addDTO) {
        B2bThirdDeliveryEntity b2bThirdDeliveryEntity = new B2bThirdDeliveryEntity();
        BeanMapperUtils.copy(addDTO, b2bThirdDeliveryEntity);

        // 数据处理
        handleData(b2bThirdDeliveryEntity,addDTO);

        log.info("开始新增B2B三方发货单");
        // 生成单号
        //此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SFFH);
        b2bThirdDeliveryEntity.setCode(code);
        boolean save = super.save(b2bThirdDeliveryEntity);
        if (!save) {
            throw new ServiceException("B2B三方发货单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单", b2bThirdDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), b2bThirdDeliveryEntity.getId(), "新增操作");
        // 新增明细
        List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.batchAdd(b2bThirdDeliveryEntity.getId(), addDTO.getDetailList());
        //新增附件
        wmsAttachmentService.batchSave(addDTO.getAttachList(), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), b2bThirdDeliveryEntity.getId());
        // 发送B2b三方仓推送任务
        sendB2bThirdWarehousePushTask(b2bThirdDeliveryEntity, detailEntityList, SyncOperateEnum.OPERATE_ADD.getCode());
        //冻结库存
        freezeVirtualInventory(b2bThirdDeliveryEntity, detailEntityList);
        return new BaseResultDTO.AddDTO(b2bThirdDeliveryEntity.getId(), code);
    }

    /**
     * 发送B2b三方仓推送任务
     *
     * @param entity
     * @param detailEntityList
     */
    private void sendB2bThirdWarehousePushTask(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList, String operate) {
        entity = this.getById(entity.getId());
        if (Objects.isNull(entity.getIsApiDelivery()) || !entity.getIsApiDelivery()) {
            return;
        }
        //推送本地消息表
        DmpPushTaskEntity pushTaskEntity = syncB2bThirdWarehouseService.syncB2bThirdWarehouse(entity, detailEntityList, operate);
        //推送中台
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void rollbackFreezeVirtualInventory(B2bThirdDeliveryEntity entity) {
        //无虚拟仓库不扣虚拟库存
        if (Objects.isNull(entity) || CharSequenceUtil.isBlank(entity.getVirtualWarehouseId())) {
            return;
        }
        InventoryUnApproveDTO dto = new InventoryUnApproveDTO();
        dto.setBillId(entity.getId());
        dto.setSourceType(InventorySourceTypeEnum.B2B_THIRD_DELIVERY);
        virtualInventoryTransCoreService.unApprove(dto);
        operateLogService.addModuleOperateLog(StrUtil.format("用户【{}】回退【{}】单据单号为【{}】的虚拟库存", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单", entity.getCode()), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "回退虚拟库存");
    }

    /**
     * 冻结虚拟库存
     *
     * @param entity
     * @param detailEntityList
     * @author will
     * @date 2024/6/12 10:58
     */
    private void freezeVirtualInventory(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList) {
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
        for (B2bThirdDeliveryDetailEntity detailEntity : detailEntityList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSourceType(InventorySourceTypeEnum.B2B_THIRD_DELIVERY);
            outInStockDTO.setSourceId(entity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getDeliverySkuId());
            outInStockDTO.setSkuNo(detailEntity.getDeliverySkuNo());
            outInStockDTO.setQty(detailEntity.getBoxQty());
            outInStockDTO.setWarehouseId(entity.getDeliveryWarehouseId());
            if (CharSequenceUtil.isBlank(entity.getVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            paramList.add(outInStockDTO);
        }
        //无虚拟仓库不扣虚拟库存
        if (CollectionUtils.isEmpty(paramList)) {
            return;
        }
        //添加冻结库存
        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
        dto.setParamList(paramList);
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.B2B_THIRD_DELIVERY.getCode());
        //更新库存
        virtualInventoryTransCoreService.approve(dto);
        String msg = StrUtil.format("【{}】单据单号为【{}】创建订单时冻结虚拟库存", "B2B三方发货单", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "冻结虚拟库存");
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(B2bThirdDeliveryDTO.UpdateDTO addOrUpdateDTO) {
        B2bThirdDeliveryEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2B三方发货单"));
        if (!ThirdDeliveryStatusEnum.FAILED.getCode().equals(old.getStatus())) {
            throw new ServiceException("只有创建失败允许编辑");
        }
        B2bThirdDeliveryEntity b2bThirdDeliveryEntity = BeanMapperUtils.map(B2bThirdDeliveryEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(b2bThirdDeliveryEntity,addOrUpdateDTO);
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SFFH);
        b2bThirdDeliveryEntity.setCode(code);
        log.info("编辑 开始修改B2B三方发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(b2bThirdDeliveryEntity);
        if (!save) {
            throw new ServiceException("B2B三方发货单更新失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录B2B三方发货单日志数据，单号：【{}】", b2bThirdDeliveryEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), b2bThirdDeliveryEntity.getCode(), "B2B三方发货单");
        operateLogService.addModuleOperateLogByObj(old, b2bThirdDeliveryEntity, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), b2bThirdDeliveryEntity.getId(), msg);
        // 新增明细
        List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.batchAdd(b2bThirdDeliveryEntity.getId(), addOrUpdateDTO.getDetailList());
        //新增附件
        wmsAttachmentService.batchSave(addOrUpdateDTO.getAttachList(), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), b2bThirdDeliveryEntity.getId());
        //推送本地消息表
        sendB2bThirdWarehousePushTask(b2bThirdDeliveryEntity, detailEntityList, SyncOperateEnum.OPERATE_ADD.getCode());
        //冻结库存
        freezeVirtualInventory(b2bThirdDeliveryEntity, detailEntityList);
        return Boolean.TRUE;
    }

    @Override
    public List<B2bThirdDeliveryDTO.TabListDTO> tabList() {
        List<B2bThirdDeliveryDTO.TabListDTO> list = baseMapper.tabList();
        List<B2bThirdDeliveryDTO.TabListDTO> tabListDTOS = new ArrayList<>();
        for (ThirdDeliveryStatusEnum value : ThirdDeliveryStatusEnum.values()) {
            B2bThirdDeliveryDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(value.getCode())).findFirst().orElse(null);
            if (Objects.nonNull(tabListDTO)) {
                tabListDTO.setTabFlagName(ThirdDeliveryStatusEnum.getName(tabListDTO.getTabFlag()));
                tabListDTOS.add(tabListDTO);
            } else {
                tabListDTOS.add(B2bThirdDeliveryDTO.TabListDTO.builder().tabFlag(value.getCode()).tabFlagName(value.getName()).count(0).build());
            }
        }
        return tabListDTOS;
    }

    @Override
    public PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> paging(PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto) {
        Page<B2bThirdDeliveryDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if (dynamicDataSourceTypeEnum != null) {
            dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        dto.getParams().setDynamicDataSource(dynamicDataSource);
        IPage<B2bThirdDeliveryDTO.PagingViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        List<B2bThirdDeliveryDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillList(List<B2bThirdDeliveryDTO.PagingViewDTO> records) {
        records.forEach(e -> {
            e.setStatusName(ThirdDeliveryStatusEnum.getName(e.getStatus()));
            String warehouseOperationType = e.getWarehouseOperationType();
            List<String> operationTypeList = Arrays.asList(warehouseOperationType.split(","));
            List<String> operationTypeNameList = operationTypeList.stream().map(WarehouseOperationTypeEnum::getName).collect(Collectors.toList());
            e.setWarehouseOperationTypeName(String.join(",", operationTypeNameList));
            e.setDeliveryMethodName(DeliveryModeEnum.getName(e.getDeliveryMethod()));
            e.setPushTypeName(B2BDeliveryPushTypeEnum.getName(e.getPushType()));
        });
    }

    @Override
    public void export(B2bThirdDeliveryDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2B三方发货单", EXPORT_WMS_B2B_THIRD_DELIVERY_REPORT.getCode(), dto);
    }

    @Override
    public B2bThirdDeliveryDTO.ViewDTO view(B2bThirdDeliveryDTO.ViewQueryDTO dto) {
        if (CharSequenceUtil.isBlank(dto.getId())) {
            return soInfoFeign.getB2bThirdDeliveryView(dto);
        } else {
            B2bThirdDeliveryEntity entity = this.getById(dto.getId());
            if (Objects.isNull(entity)) {
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "B2B三方发货单");
            }
            SoInfoEntity soInfoEntity = CharSequenceUtil.isNotBlank(dto.getSoId()) ? soInfoFeign.getSoInfoById(dto.getSoId()) : null;
            List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.listByMainIds(Collections.singletonList(dto.getId()));
            //客户信息要使用销售订单的，不能使用B2B三方发货单的客户信息（销售订单的客户信息可能会发生变化）
            B2bThirdDeliveryDTO.ViewDTO viewDTO = B2bThirdDeliveryConverter.INSTANCE.toB2bThirdDeliveryViewDTO(entity, detailEntityList);
            String operationDesc = entity.getOperationDesc();
            String warehouseOperationType = entity.getWarehouseOperationType();
            viewDTO.setWarehouseOperationTypeDTOList(B2bThirdDeliveryDTO.WarehouseOperationTypeDTO.convert(warehouseOperationType,operationDesc));

            viewDTO.setAttachList(wmsAttachmentService.getByBusinessIds(Collections.singletonList(dto.getId()), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode()));
            if (Objects.nonNull(soInfoEntity)) {
                viewDTO.setReceiverName(soInfoEntity.getReceiverName());
                viewDTO.setTelNumber(soInfoEntity.getTelNumber());
                viewDTO.setCountryId(soInfoEntity.getCountryId());
                viewDTO.setCountryName(soInfoEntity.getCountryName());
                viewDTO.setProvince(soInfoEntity.getProvince());
                viewDTO.setCity(soInfoEntity.getCity());
                viewDTO.setPostCode(soInfoEntity.getPostCode());
                if (CharSequenceUtil.isNotBlank(soInfoEntity.getReceiveAddressId()) && CharSequenceUtil.isBlank(soInfoEntity.getReceiveAddress())) {
                    List<CustomerAddressEntity> customerAddressEntities = customerFeign.listCustomerAddressByIds(Collections.singletonList(soInfoEntity.getReceiveAddressId()));
                    viewDTO.setReceiveAddress(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getAddress() : "");
                    viewDTO.setAddress2(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getAddress2() : "");
                    viewDTO.setAddress3(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getAddress3() : "");
                    viewDTO.setCountryId(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getCountryId() : "");
                    viewDTO.setCountryName(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getCountryName() : "");
                }
                if (!soInfoEntity.getCustomerId().equals(viewDTO.getCustomerId())) {
                    viewDTO.setCustomerName(CharSequenceUtil.EMPTY);
                }
                viewDTO.setCustomerId(soInfoEntity.getCustomerId());
            }
            if (CharSequenceUtil.isNotBlank(viewDTO.getCustomerId()) && CharSequenceUtil.isBlank(viewDTO.getCustomerName())) {
                List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(Collections.singletonList(viewDTO.getCustomerId()));
                viewDTO.setCustomerName(CollUtil.isNotEmpty(customerInfoEntities) ? customerInfoEntities.get(0).getName() : "");
            }
            //同步销售数量
            List<String> soDetailIds = detailEntityList.stream().map(B2bThirdDeliveryDetailEntity::getSoDetailId).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(soDetailIds)) {
                List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
                Map<String, Integer> soDetailMap = soDetailEntities.stream().collect(Collectors.toMap(SoDetailEntity::getId, SoDetailEntity::getQty));
                viewDTO.getDetailList().forEach(item -> {
                    item.setSaleQty(soDetailMap.getOrDefault(item.getSoDetailId(), 0));
                });
            }
            return viewDTO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, String status, String errorMsg, String platformOrderCode, String remark, String trackNo, LocalDateTime deliveryTime) {
        B2bThirdDeliveryEntity old = this.getById(id);
        if (Objects.isNull(old)) {
            log.warn("单据【{}】不存在", id);
            return null;
        }
        if (status.equals(old.getStatus())) {
            return null;
        }
        this.lambdaUpdate().set(B2bThirdDeliveryEntity::getStatus, status)
                .set(CharSequenceUtil.isNotBlank(errorMsg), B2bThirdDeliveryEntity::getErrorMessage, errorMsg)
                .set(CharSequenceUtil.isNotBlank(platformOrderCode), B2bThirdDeliveryEntity::getPlatformOrderCode, platformOrderCode)
                .set(CharSequenceUtil.isNotBlank(remark), B2bThirdDeliveryEntity::getRemark, remark)
                .set(CharSequenceUtil.isNotBlank(trackNo), B2bThirdDeliveryEntity::getTrackNo, trackNo)
                .set(Objects.nonNull(deliveryTime), B2bThirdDeliveryEntity::getDeliveryTime, deliveryTime)
                .eq(B2bThirdDeliveryEntity::getId, id).update();
        B2bThirdDeliveryEntity newEntity = this.getById(id);
        operateLogService.addModuleOperateLogByObj(old, newEntity, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), id, "更新操作");
        if (ThirdDeliveryStatusEnum.FAILED.getCode().equals(status) || ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(status)) {
            //冻结库存释放
            this.rollbackFreezeVirtualInventory(old);
            return null;
        } else if (ThirdDeliveryStatusEnum.SHIPPED.getCode().equals(status)) {
            //生成销售出库单
            BatchResultDTO resultDTO = this.generateB2bThirdDelivery(id);
            if (resultDTO.getSuccess()) {
                String outstockId = resultDTO.getId();
                SoOutstockEntity soOutstockEntity = soOutstockService.getById(outstockId);
                if (Objects.isNull(soOutstockEntity)) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "销售出库单");
                }
                return BatchResultDTO.success(soOutstockEntity.getId(), soOutstockEntity.getCode(), "销售出库单生成");
            }
        }
        return null;
    }

    /**
     * 生成销售出库单
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateB2bThirdDelivery(String id) {
        B2bThirdDeliveryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "B2B三方发货单");
        }
        //只有已发货允许生成销售出库单
        if (!ThirdDeliveryStatusEnum.SHIPPED.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_GENERATE_OUTSTOCK_ONLY_SHIPPED);
        }
        //检查是否已生成销售出库单
        SoOutstockEntity outstockEntity = soOutstockService.getBySourceCode(entity.getCode());
        if (Objects.nonNull(outstockEntity)) {
            return BatchResultDTO.fail(id, entity.getCode(), "销售出库单已存在");
        }
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(entity.getSoId());
        if (Objects.isNull(soInfoEntity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "销售订单");
        }
        SoOutstockDTO.AddDTO addDTO = B2bThirdDeliveryConverter.INSTANCE.toSoOutstockAddDTO(entity, soInfoEntity);
        //承运商
        if (CharSequenceUtil.isNotBlank(entity.getLogisticsChannelId())) {
            LogisticsChannelDTO.BaseDTO channelInfo = logisticsFeign.getChannelInfoById(entity.getLogisticsChannelId());
            if (Objects.nonNull(channelInfo)) {
                addDTO.setCarrierId(channelInfo.getLogisticsSupplierId());
            }
        }
        List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.listByMainIds(Collections.singletonList(id));
        List<String> soDetailIds = detailEntityList.stream().map(B2bThirdDeliveryDetailEntity::getSoDetailId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollUtil.isNotEmpty(soDetailIds) ? soInfoFeign.listSoDetailByIds(soDetailIds) : new ArrayList<>();
        List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>(detailEntityList.size());
        detailEntityList.forEach(e -> {
            //根据soDetailId查询soDetailEntity
            SoDetailEntity soDetailEntity = CollUtil.isNotEmpty(soDetailList) ? soDetailList.stream().filter(e1 -> e1.getId().equals(e.getSoDetailId())).findFirst().orElse(null) : null;
            if (Objects.isNull(soDetailEntity)) {
                log.warn("销售订单明细【{}】不存在", e.getSoDetailId());
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "销售订单明细不存在");
            }
            SoOutstockDetailDTO.AddDTO detailDTO = B2bThirdDeliveryConverter.INSTANCE.toSoOutstockAddDetailDTO(entity, e, soDetailEntity, soInfoEntity);
            detailList.add(detailDTO);
        });
        addDTO.setDetailList(detailList);
        //自动生成功能系统标识
        Boolean originalValue = UserContext.getIsUserSystem();
        UserContext.setIsUserSystem(Boolean.TRUE);
        String outstockId = null;
        try {
            //构建销售出库单数据
            outstockId = soOutstockService.add(addDTO);
            if (CharSequenceUtil.isBlank(outstockId)) {
                throw new ServiceException(ApiError.BILL_DATA_CREATE_FAILED);
            }
        } catch (Exception e) {
            throw new ServiceException(ApiError.BILL_SUBMIT_FAILED, e.getMessage());
        } finally {
            //恢复系统标识
            UserContext.setIsUserSystem(originalValue);
        }
        return BatchResultDTO.success(outstockId, entity.getCode(), "生成销售出库单成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deliveryIntercept(String id, String remark) {
        B2bThirdDeliveryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "B2B三方发货单");
        }
        //只有待发货允许发货拦截
        if (!ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_INTERCEPT_ONLY_WAIT_SHIPPED);
        }
        if (entity.getIsApiDelivery()) {
            //调三方仓
            sendB2bThirdWarehousePushTask(entity, null, SyncOperateEnum.OPERATE_INVALID.getCode());
            updateStatus(id, ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), "", "", "", "", null);
        } else {
            //直接拦截成功
            updateStatus(id, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", "", "", "", null);
            String msg = CharSequenceUtil.format("用户【{}】提交发货拦截申请成功,拦截成功", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "发货拦截");
        }
        return BatchResultDTO.success(id, entity.getCode(), "提交发货拦截成功");
    }

    @Override
    public BatchResultDTO manualDelivery(B2bThirdDeliveryEntity entity) {
        if (entity.getIsApiDelivery()) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_MANUAL_ONLY_B2B_DISABLED);
        }
        if (!ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_ONLY_WAIT_SHIPPED);
        }
        //更新状态为已发货 并生成出库单
        BatchResultDTO resultDTO = service.updateStatus(entity.getId(), ThirdDeliveryStatusEnum.SHIPPED.getCode(), "", "", entity.getRemark(), "", LocalDateTime.now());
        if (Objects.nonNull(resultDTO) && resultDTO.getSuccess() && CharSequenceUtil.isNotBlank(resultDTO.getId())) {
            //提审销售出库单
            try {
                service.submitApprove(resultDTO.getId());
            } catch (Exception e) {
                soOutstockService.updateRemarkById(resultDTO.getId(), "自动审核失败" + e.getMessage());
                return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动发货成功:" + "自动审核失败" + e.getMessage());
            }
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动发货成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(B2bThirdDeliveryEntity entity) {
        //只有创建失败、取消发货允许删除
        if (!ThirdDeliveryStatusEnum.FAILED.getCode().equals(entity.getStatus()) && !ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_DELETE_ONLY_FAILED_OR_CANCELED);
        }
        this.removeById(entity.getId());
        b2bThirdDeliveryDetailService.deleteByMainIds(Collections.singletonList(entity.getId()));
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
    }

    @Override
    public List<B2bThirdDeliveryEntity> queryDeliveryStatus(Boolean isApiDelivery, List<String> statusList) {
        return lambdaQuery().eq(Objects.nonNull(isApiDelivery), B2bThirdDeliveryEntity::getIsApiDelivery, isApiDelivery)
                .in(CollectionUtil.isNotEmpty(statusList), B2bThirdDeliveryEntity::getStatus, statusList).orderByDesc(B2bThirdDeliveryEntity::getCreateTime).list();
    }

    @Override
    public void updateQueryResult(List<ThirdWarehouseQueryFbaOutboundResponse> responses, String providerCode, List<String> codeList) {
        if (CollUtil.isEmpty(responses)) {
            return;
        }
        if (OmsPlatformEnum.DA_MAI.getCode().equals(providerCode)) {
            List<B2bThirdDeliveryEntity> list = lambdaQuery().in(B2bThirdDeliveryEntity::getCode, codeList).list();
            if (CollUtil.isEmpty(list)) {
                return;
            }
            for (B2bThirdDeliveryEntity entity : list) {
                ThirdWarehouseQueryFbaOutboundResponse response = responses.stream().filter(r -> r.getCode().equals(entity.getCode())).findFirst().orElse(null);
                if (Objects.nonNull(response)) {
                    try {
                        this.handleResultData(entity.getId(), response);
                    } catch (Exception e) {
                        XxlJobHelper.log("更新B2B三方发货单状态失败,id={},code={},error={}", entity.getId(), entity.getCode(), e.getMessage(), e);
                    }

                }
            }
        } else {
            XxlJobHelper.log("不支持的三方渠道,providerCode={}", providerCode);
        }
    }

    @Override
    public void handleResultData(String id, ThirdWarehouseQueryFbaOutboundResponse response) {
        XxlJobHelper.log("处理订单状态,id={},response={},", id, JSONUtil.toJsonStr(response));
        //订单已取消直接返回
        /**
         * 以下状态自动变更为取消发货，有拦截标识时清空拦截标识，记录拦截成功
         * EXCEPTION：出库异常
         * DISCARD：已作废
         * PROBLEM：问题件
         */
        String deliveryTimeStr = response.getDeliveryTimeStr();
        LocalDateTime deliveryTime = null;
        if (StrUtil.isNotBlank(deliveryTimeStr)) {
            deliveryTime = LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (    response.getStatus().equals(B2BThirdDeliveryCancelResultEnum.DISCARD.getCode()) ||
                response.getStatus().equals(B2BThirdDeliveryCancelResultEnum.PROBLEM.getCode())) {
            //拦截成功，更新B2B三方发货单状态 取消发货
            service.updateStatus(id, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
        } else if (response.getStatus().equals(B2BThirdDeliveryCancelResultEnum.SUCCESS.getCode())) {
            //已发货，更新B2B三方发货单状态 生成销售出库单
            BatchResultDTO resultDTO = service.updateStatus(id, ThirdDeliveryStatusEnum.SHIPPED.getCode(), "", response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
            if (Objects.nonNull(resultDTO) && resultDTO.getSuccess() && CharSequenceUtil.isNotBlank(resultDTO.getId())) {
                //提审销售出库单
                try {
                    service.submitApprove(resultDTO.getId());
                } catch (Exception e) {
                    soOutstockService.updateRemarkById(resultDTO.getId(), "自动审核失败" + e.getMessage());
                }
            }
        } else if (response.getStatus().equals(B2BThirdDeliveryCancelResultEnum.BLOCK.getCode()) ||
                response.getStatus().equals(B2BThirdDeliveryCancelResultEnum.DISCARD_PROCESSED.getCode())) {
            //拦截中 记录拦截标识
            service.updateStatus(id, ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), "", response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
        }  else if (response.getStatus().equals(B2BThirdDeliveryCancelResultEnum.EXCEPTION.getCode())) {

            operateLogService.addModuleOperateLog("海外仓出库异常，系统应拦截，为保证发货时效运营要求不予拦截，直接海外仓后台修改提交", ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), id, "出库异常");
        } else {
            XxlJobHelper.log("不操作的状态,id={},code={},status={}", id, response.getCode(), response.getStatus());
        }
    }

    @Override
    public List<B2bThirdDeliveryEntity> listBySoIds(List<String> soIds) {
        if (CollUtil.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(B2bThirdDeliveryEntity::getSoId, soIds).ne(B2bThirdDeliveryEntity::getStatus, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode()).list();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void submitApprove(String soOutStockId) {
        SoOutstockEntity soOutstockEntity = soOutstockService.getById(soOutStockId);
        if (Objects.isNull(soOutstockEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "销售出库单");
        }
        BatchResultDTO submit = soOutstockService.submit(soOutstockEntity, Boolean.FALSE);
        if (!submit.getSuccess()) {
            throw new ServiceException(ApiError.BILL_SUBMIT_FAILED, submit.getMsg());
        }
        soOutstockService.approve(new ApproveOneDTO(soOutstockEntity.getId(), ApproveTypeEnum.PASS.getStatus(), "三方仓出库完成出库单自动审核通过", Boolean.FALSE));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createFbaOutbound(ThirdWarehouseCreateFbaOutboundReq req) {
        String sourceId = req.getSourceId();
        B2bThirdDeliveryEntity entity = this.getById(sourceId);
        if (Objects.isNull(entity)) {
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_NOT_EXIST_GENERIC.getMsg(), req.getSourceCode()), "", "", "", null);
            return;
        }
        if (!ThirdDeliveryStatusEnum.CREATING.getCode().equals(entity.getStatus())) {
            log.error("B2BThirdDelivery创建订单失败，状态不是CREATING，当前状态：{}", entity.getStatus());
            return;
        }
        //过滤掉服务费用SKU
        List<ThirdWarehouseCreateFbaOutboundReq.Item> items = req.getItems();
        List<String> skuIdList = items.stream().map(ThirdWarehouseCreateFbaOutboundReq.Item::getSkuId).distinct().collect(Collectors.toList());
        List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listLogisticsProduct(skuIdList);
        items = items.stream().filter(v->{
            LogisticsProductDTO.ProductDTO productDTO = skuInfoList.stream().filter(s->s.getSkuId().equals(v.getSkuId())).findFirst().orElse(null);
            if(Objects.isNull(productDTO)){
                return true;
            }
            if ("费用".equalsIgnoreCase(productDTO.getProperty()) || "服务".equalsIgnoreCase(productDTO.getProperty())){
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(items)){
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), ApiError.COMMON_NO_DELIVERY_SKU.getMsg(), "", "", "", null);
            return;
        }
        req.setItems(items);

        ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(req.getThirdWarehouseProvideCode());
        if (Objects.isNull(service)) {
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_PROVIDER_SERVICE_NOT_ENABLED.getMsg(), req.getThirdWarehouseProvideCode()), "", "", "", null);
            return;
        }
        ApiResult<String> fbaOutboundBill = createFbaOutboundBill(service, req, 0);
        if (fbaOutboundBill.isSuccess()) {
            // 创建成功
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", fbaOutboundBill.getData(), "", "", null);
            return;
        }
        // 创建失败，尝试查询是否实际已创建成功
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getReferenceNo()));
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());

        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult = service.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());

        String platformOrderCode = getPlatformOrderCode(queryResult);
        String trackNo = getTrackNo(queryResult);
        LocalDateTime deliveryTime = getDeliveryTime(queryResult);

        if (CharSequenceUtil.isNotBlank(platformOrderCode)) {
            // 查询发现订单实际已创建成功
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", platformOrderCode, "", trackNo, deliveryTime);
        } else {
            // 确认创建失败
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), fbaOutboundBill.getMsg(), "", "", trackNo, deliveryTime);
        }
    }

    private LocalDateTime getDeliveryTime(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult) {
        if (queryResult.isSuccess() && CollUtil.isNotEmpty(queryResult.getData())) {
            return LocalDateTime.parse(queryResult.getData().get(0).getDeliveryTimeStr(), DateTimeFormatter.ISO_DATE_TIME);
        }
        return null;
    }

    // 提取平台订单号的辅助方法
    private String getPlatformOrderCode(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> result) {
        if (result.isSuccess() && CollUtil.isNotEmpty(result.getData())) {
            return result.getData().get(0).getPlatformOrderCode();
        }
        return null;
    }

    private String getTrackNo(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> result) {
        if (result.isSuccess() && CollUtil.isNotEmpty(result.getData())) {
            return result.getData().get(0).getTrackNo();
        }
        return null;
    }

    private ApiResult<String> createFbaOutboundBill(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req, final int retryCount) {
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getReferenceNo()));
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());
        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> listApiResult = service.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());
        String platformOrderCode = getPlatformOrderCode(listApiResult);
        if (CharSequenceUtil.isNotBlank(platformOrderCode)) {
            return ApiResult.success(platformOrderCode);
        }

        try {
            return service.createFbaOutboundBill(req, req.getAuthId());
        } catch (Exception e) {
            log.warn("第{}次执行失败: {}", retryCount + 1, e.getMessage());

            if (retryCount + 1 < MAX_RETRY_COUNT) {
                try {
                    log.info("{}秒后进行第{}次重试", RETRY_DELAY_SECONDS / 1000, retryCount + 2);
                    Thread.sleep(RETRY_DELAY_SECONDS);
                    return createFbaOutboundBill(service, req, retryCount + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ApiResult.error(-1, "重试被中断");
                }
            } else {
                log.error("已达到最大重试次数{}次，停止重试", MAX_RETRY_COUNT);
                return ApiResult.error(-1, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancelFbaOutbound(ThirdWarehouseCancelFbaOutboundReq req) {
        String sourceId = req.getSourceId();
        B2bThirdDeliveryEntity entity = this.getById(sourceId);
        if (Objects.isNull(entity)) {
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_NOT_EXIST_GENERIC.getMsg(), req.getSourceCode()), "", "", "", null);
            return;
        }

        ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(req.getThirdWarehouseProvideCode());
        if (Objects.isNull(service)) {
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_PROVIDER_SERVICE_NOT_ENABLED.getMsg(), req.getThirdWarehouseProvideCode()), "", "", "", null);
            return;
        }
        ApiResult<String> fbaOutboundBill = cancelFbaOutboundBill(service, req, 0);
        if (fbaOutboundBill.isSuccess()) {
            // 创建成功
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), "", "", "", "", null);
        } else {
            // 创建失败
            this.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", "", "", "", null);
        }
    }

    private ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseService service, ThirdWarehouseCancelFbaOutboundReq req, final int retryCount) {
        try {
            return service.cancelFbaOutboundBill(req, req.getAuthId());
        } catch (Exception e) {
            log.warn("第{}次执行失败: {}", retryCount + 1, e.getMessage());

            if (retryCount + 1 < MAX_RETRY_COUNT) {
                try {
                    log.info("{}秒后进行第{}次重试", RETRY_DELAY_SECONDS / 1000, retryCount + 2);
                    Thread.sleep(RETRY_DELAY_SECONDS);
                    return cancelFbaOutboundBill(service, req, retryCount + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ApiResult.error(-1, "重试被中断");
                }
            } else {
                log.error("已达到最大重试次数{}次，停止重试", MAX_RETRY_COUNT);
                return ApiResult.error(-1, e.getMessage());
            }
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(B2bThirdDeliveryEntity b2bThirdDeliveryEntity, B2bThirdDeliveryDTO.CommonDTO commonDTO) {
        List<B2bThirdDeliveryDTO.WarehouseOperationTypeDTO> warehouseOperationTypeDTOList = commonDTO.getWarehouseOperationTypeDTOList();
        if (CollUtil.isNotEmpty(warehouseOperationTypeDTOList)) {
            String operationDesc = warehouseOperationTypeDTOList.stream()
                    .map(B2bThirdDeliveryDTO.WarehouseOperationTypeDTO::getOperationDesc)
                    .collect(Collectors.joining(","));
            String warehouseOperationType = warehouseOperationTypeDTOList.stream()
                    .map(B2bThirdDeliveryDTO.WarehouseOperationTypeDTO::getWarehouseOperationType)
                    .collect(Collectors.joining(","));
            b2bThirdDeliveryEntity.setOperationDesc(operationDesc);
            b2bThirdDeliveryEntity.setWarehouseOperationType(warehouseOperationType);
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getCountryName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getCountryId())) {
            DictCountryEntity countryEntity = FeignQuery.getById(DictCountryEntity.class, b2bThirdDeliveryEntity.getCountryId());
            b2bThirdDeliveryEntity.setCountryName(Objects.nonNull(countryEntity) ? countryEntity.getNameCn() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getDeliveryWarehouseName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getDeliveryWarehouseId())) {
            WarehouseEntity warehouseEntity = FeignQuery.getById(WarehouseEntity.class, b2bThirdDeliveryEntity.getDeliveryWarehouseId());
            b2bThirdDeliveryEntity.setDeliveryWarehouseName(Objects.nonNull(warehouseEntity) ? warehouseEntity.getName() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getWarehouseOrgName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getWarehouseOrgId())) {
            List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(b2bThirdDeliveryEntity.getWarehouseOrgId()));
            b2bThirdDeliveryEntity.setWarehouseOrgName(CollUtil.isNotEmpty(accountingCompanyList) ? accountingCompanyList.get(0).getName() : "");
        }
        if ((CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getLogisticsChannelName()) || CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getLogisticsChannelCode())) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getLogisticsChannelId())) {
            LogisticsChannelEntity channelEntity = FeignQuery.getById(LogisticsChannelEntity.class, b2bThirdDeliveryEntity.getLogisticsChannelId());
            b2bThirdDeliveryEntity.setLogisticsChannelName(Objects.nonNull(channelEntity) ? channelEntity.getName() : "");
            b2bThirdDeliveryEntity.setLogisticsChannelCode(Objects.nonNull(channelEntity) ? channelEntity.getCode() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getStatus())) {
            b2bThirdDeliveryEntity.setStatus(ThirdDeliveryStatusEnum.CREATING.getCode());
        }
        //标识是否推送api仓库
        if (CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getDeliveryWarehouseId())) {
            List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOS = overseasProviderWarehouseService.listByWarehouseIdList(Collections.singletonList(b2bThirdDeliveryEntity.getDeliveryWarehouseId()));
            Boolean isApiDelivery = Boolean.FALSE;
            if (CollUtil.isNotEmpty(viewDTOS)) {
                OverseasProviderWarehouseDTO.ViewDTO viewDTO = viewDTOS.stream().filter(e -> !e.getDisabled()).findFirst().orElse(null);
                isApiDelivery = Objects.nonNull(viewDTO) ? viewDTO.getIsB2BApiDelivery() : Boolean.FALSE;
                b2bThirdDeliveryEntity.setThirdWarehouseCode(Objects.nonNull(viewDTO) ? viewDTO.getPlatformWarehouseCode() : "");
            }
            b2bThirdDeliveryEntity.setIsApiDelivery(isApiDelivery);
        }
        //状态修改
        if (Objects.isNull(b2bThirdDeliveryEntity.getIsApiDelivery()) || !b2bThirdDeliveryEntity.getIsApiDelivery()) {
            b2bThirdDeliveryEntity.setStatus(ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode());
            b2bThirdDeliveryEntity.setPushType(B2BDeliveryPushTypeEnum.MANUAL.getCode());
        } else {
            b2bThirdDeliveryEntity.setStatus(ThirdDeliveryStatusEnum.CREATING.getCode());
            b2bThirdDeliveryEntity.setPushType(B2BDeliveryPushTypeEnum.API.getCode());
            b2bThirdDeliveryEntity.setErrorMessage(CharSequenceUtil.EMPTY);
        }
        //客户名称
        if (CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getCustomerId()) && CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getCustomerName())) {
            List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(Collections.singletonList(b2bThirdDeliveryEntity.getCustomerId()));
            b2bThirdDeliveryEntity.setCustomerName(CollUtil.isNotEmpty(customerInfoEntities) ? customerInfoEntities.get(0).getName() : "");
        }
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(b2bThirdDeliveryEntity.getSoId());
        if (Objects.nonNull(soInfoEntity) && CharSequenceUtil.isNotBlank(soInfoEntity.getReceiveAddressId()) && CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getReceiveAddress())) {
            List<CustomerAddressEntity> customerAddressEntities = customerFeign.listCustomerAddressByIds(Collections.singletonList(soInfoEntity.getReceiveAddressId()));
            b2bThirdDeliveryEntity.setReceiveAddress(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getAddress() : "");
            b2bThirdDeliveryEntity.setAddress2(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getAddress2() : "");
            b2bThirdDeliveryEntity.setAddress3(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getAddress3() : "");
            b2bThirdDeliveryEntity.setCountryId(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getCountryId() : "");
            b2bThirdDeliveryEntity.setCountryName(CollUtil.isNotEmpty(customerAddressEntities) ? customerAddressEntities.get(0).getCountryName() : "");
        }
    }
}
