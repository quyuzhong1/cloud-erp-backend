package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.PlatformOutboundDTO;
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
import com.common.core.enums.RuleCompareEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FileUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.excel.B2bCustomerPackingImportExcelDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileReq;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.B2bCustomerPackingEntity;
import com.erp.server.wms.service.B2bCustomerPackingService;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.B2BDeliveryPushTypeEnum;
import com.erp.model.wms.enums.CancelStatusEnum;
import com.erp.model.wms.enums.HandleResultEnum;
import com.erp.model.wms.enums.InterceptStatusEnum;
import com.erp.model.wms.enums.SoB2bDeliveryInterceptStatusEnum;
import com.erp.model.wms.enums.SoB2bDeliveryInterceptSourceTypeEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.model.wms.enums.WarehouseOperationTypeEnum;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.model.wms.resolver.B2bThirdDeliveryStatusResolver;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.listener.B2bCustomerPackingExcelListener;
import com.erp.server.wms.mapper.B2bThirdDeliveryMapper;
import com.erp.server.wms.service.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
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
    private B2bCustomerPackingService b2bCustomerPackingService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private OverseasProviderService overseasProviderService;
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
    @Resource
    private SoB2bDeliveryInterceptService soB2bDeliveryInterceptService;
    @Lazy
    @Resource
    private B2bThirdDeliveryService service;
    @Lazy
    @Resource
    private B2bThirdDeliveryService proxyService;
    @Resource
    private CfgThirdWarehouseOperationDescriptionService cfgThirdWarehouseOperationDescriptionService;
    @Resource
    private CfgThirdWarehouseOperationDescriptionValueService cfgThirdWarehouseOperationDescriptionValueService;

    @Resource
    private LogisticsProductFeign logisticsProductFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_SECONDS = 10000;
    private static final String GOOD_CANG_ORDER_PACKING_ATTACHMENT = "ORDER_PACKING_ATTACHMENT";
    private static final Set<Integer> ALLOWED_LABELS_PER_BOX = new HashSet<>(Arrays.asList(0, 1, 2, 4));
    private static final String CANCEL_ACCEPTED_QUERY_FAILED_MSG = "拦截请求已提交三方仓，立即查询状态失败，请稍后刷新确认拦截结果";

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
        normalizePackingFields(b2bThirdDeliveryEntity, addDTO);
        boolean goodCangWarehouse = isGoodCangWarehouse(b2bThirdDeliveryEntity.getDeliveryWarehouseId());
        validatePacking(b2bThirdDeliveryEntity, addDTO.getDetailList(), addDTO.getPackingDetailList(), goodCangWarehouse);

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
        b2bCustomerPackingService.batchSave(b2bThirdDeliveryEntity.getId(), enrichPackingDetailList(addDTO.getDetailList(), addDTO.getPackingDetailList()));
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
        if (!isApiPushDelivery(entity)) {
            return;
        }
        //推送本地消息表
        DmpPushTaskEntity pushTaskEntity = syncB2bThirdWarehouseService.syncB2bThirdWarehouse(entity, detailEntityList, operate);
        if (Objects.isNull(pushTaskEntity)) {
            log.info("B2B三方发货单走本地消息模式，无需发送中台任务, code={}, operate={}", entity.getCode(), operate);
            return;
        }
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
        normalizePackingFields(b2bThirdDeliveryEntity, addOrUpdateDTO);
        boolean goodCangWarehouse = isGoodCangWarehouse(b2bThirdDeliveryEntity.getDeliveryWarehouseId());
        validatePacking(b2bThirdDeliveryEntity, addOrUpdateDTO.getDetailList(), addOrUpdateDTO.getPackingDetailList(), goodCangWarehouse);
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
        b2bCustomerPackingService.batchSave(b2bThirdDeliveryEntity.getId(), enrichPackingDetailList(addOrUpdateDTO.getDetailList(), addOrUpdateDTO.getPackingDetailList()));
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
        List<String> soIds = records.stream()
                .map(B2bThirdDeliveryDTO.PagingViewDTO::getSoId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<String> soDetailIds = records.stream()
                .map(B2bThirdDeliveryDTO.PagingViewDTO::getSoDetailId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> salesPlatformOrderCodeMap = new HashMap<>();
        Map<String, String> customerPOMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(soIds)) {
            List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(soIds);
            if (CollectionUtils.isNotEmpty(soInfoList)) {
                salesPlatformOrderCodeMap = soInfoList.stream()
                        .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                        .collect(Collectors.toMap(SoInfoEntity::getId, item -> CharSequenceUtil.blankToDefault(item.getPlatformOrderCode(), CharSequenceUtil.EMPTY), (v1, v2) -> v1));
            }
        }
        if (CollectionUtils.isNotEmpty(soDetailIds)) {
            List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(soDetailIds);
            if (CollectionUtils.isNotEmpty(soDetailList)) {
                customerPOMap = soDetailList.stream()
                        .filter(item -> CharSequenceUtil.isNotBlank(item.getId()) && CharSequenceUtil.isNotBlank(item.getCustomerPO()))
                        .collect(Collectors.toMap(SoDetailEntity::getId, SoDetailEntity::getCustomerPO, (v1, v2) -> v1));
            }
        }
        Map<String, String> finalSalesPlatformOrderCodeMap = salesPlatformOrderCodeMap;
        Map<String, String> finalCustomerPOMap = customerPOMap;
        records.forEach(e -> {
            if (CharSequenceUtil.isBlank(e.getSalesPlatformOrderCode())) {
                e.setSalesPlatformOrderCode(finalSalesPlatformOrderCodeMap.getOrDefault(e.getSoId(), CharSequenceUtil.EMPTY));
            }
            if (CharSequenceUtil.isBlank(e.getCustomerPO())) {
                e.setCustomerPO(finalCustomerPOMap.getOrDefault(e.getSoDetailId(), CharSequenceUtil.EMPTY));
            }
            e.setStatusName(ThirdDeliveryStatusEnum.getName(e.getStatus()));
            String warehouseOperationType = e.getWarehouseOperationType();
            if (CharSequenceUtil.isBlank(warehouseOperationType)) {
                e.setWarehouseOperationTypeName(CharSequenceUtil.EMPTY);
            } else {
                List<String> operationTypeList = Arrays.asList(warehouseOperationType.split(","));
                List<String> operationTypeNameList = operationTypeList.stream().map(WarehouseOperationTypeEnum::getName).collect(Collectors.toList());
                e.setWarehouseOperationTypeName(String.join(",", operationTypeNameList));
            }
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
            B2bThirdDeliveryDTO.ViewDTO viewDTO = soInfoFeign.getB2bThirdDeliveryView(dto);
            if (Objects.nonNull(viewDTO)) {
                viewDTO.setPlatformOrderCode(CharSequenceUtil.EMPTY);
                fillThirdWarehouseProvider(viewDTO);
            }
            return viewDTO;
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
            fillPackingView(viewDTO, entity, detailEntityList);
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
            fillThirdWarehouseProvider(viewDTO);
            return viewDTO;
        }
    }

    private void fillThirdWarehouseProvider(B2bThirdDeliveryDTO.ViewDTO viewDTO) {
        if (Objects.isNull(viewDTO) || CharSequenceUtil.isBlank(viewDTO.getDeliveryWarehouseId())) {
            return;
        }
        OverseasProviderWarehouseEntity overseasProviderWarehouse = overseasProviderWarehouseService.getByWarehouseId(viewDTO.getDeliveryWarehouseId());
        if (Objects.isNull(overseasProviderWarehouse)) {
            return;
        }
        OverseasProviderEntity overseasProvider = overseasProviderService.getById(overseasProviderWarehouse.getMainId());
        if (Objects.isNull(overseasProvider)) {
            return;
        }
        // Keep legacy field behavior while exposing explicit provider fields for new frontend checks.
        viewDTO.setThirdWarehouseCode(overseasProvider.getCode());
        viewDTO.setThirdWarehouseProviderCode(overseasProvider.getCode());
        viewDTO.setThirdWarehouseProviderName(overseasProvider.getName());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @DistributeLocker(keyName = "id")
    public BatchResultDTO updateStatus(String id, String status, String errorMsg, String platformOrderCode, String remark, String trackNo, LocalDateTime deliveryTime) {
        B2bThirdDeliveryEntity old = this.getById(id);
        if (Objects.isNull(old)) {
            log.warn("单据【{}】不存在", id);
            return null;
        }
        boolean statusChanged = !Objects.equals(status, old.getStatus());
        boolean shouldGenerateOutstock = ThirdDeliveryStatusEnum.SHIPPED.getCode().equals(status)
                && isMissingSoOutstock(old);
        boolean shouldClearErrorMessage = statusChanged
                && !ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(status)
                && CharSequenceUtil.isNotBlank(old.getErrorMessage())
                && CharSequenceUtil.isBlank(errorMsg);
        boolean fieldChanged = shouldClearErrorMessage
                || isFieldValueChanged(old.getPlatformOrderCode(), platformOrderCode)
                || isFieldValueChanged(old.getRemark(), remark)
                || isFieldValueChanged(old.getTrackNo(), trackNo)
                || isFieldValueChanged(old.getErrorMessage(), errorMsg)
                || (Objects.nonNull(deliveryTime) && !Objects.equals(old.getDeliveryTime(), deliveryTime));
        if (!statusChanged && !fieldChanged && !shouldGenerateOutstock) {
            return null;
        }
        if (statusChanged || fieldChanged) {
            this.lambdaUpdate()
                    .set(statusChanged, B2bThirdDeliveryEntity::getStatus, status)
                    .set(CharSequenceUtil.isNotBlank(errorMsg), B2bThirdDeliveryEntity::getErrorMessage, errorMsg)
                    .set(shouldClearErrorMessage, B2bThirdDeliveryEntity::getErrorMessage, CharSequenceUtil.EMPTY)
                    .set(CharSequenceUtil.isNotBlank(platformOrderCode), B2bThirdDeliveryEntity::getPlatformOrderCode, platformOrderCode)
                    .set(CharSequenceUtil.isNotBlank(remark), B2bThirdDeliveryEntity::getRemark, remark)
                    .set(CharSequenceUtil.isNotBlank(trackNo), B2bThirdDeliveryEntity::getTrackNo, trackNo)
                    .set(Objects.nonNull(deliveryTime), B2bThirdDeliveryEntity::getDeliveryTime, deliveryTime)
                    .eq(B2bThirdDeliveryEntity::getId, id).update();
            B2bThirdDeliveryEntity newEntity = this.getById(id);
            operateLogService.addModuleOperateLogByObj(old, newEntity, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), id, "更新操作");
        }
        if (!statusChanged && !shouldGenerateOutstock) {
            return null;
        }
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

    private boolean isMissingSoOutstock(B2bThirdDeliveryEntity entity) {
        return Objects.nonNull(entity)
                && CharSequenceUtil.isNotBlank(entity.getCode())
                && Objects.isNull(soOutstockService.getBySourceCode(entity.getCode()));
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
    @DistributeLocker(keyName = "id")
    public BatchResultDTO deliveryIntercept(String id, String remark) {
        B2bThirdDeliveryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "B2B三方发货单");
        }
        // 只有待发货、异常订单允许发货拦截
        if (!canStartDeliveryIntercept(entity.getStatus())) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_INTERCEPT_ONLY_WAIT_SHIPPED);
        }
        if (isApiPushDelivery(entity)) {
            createB2bDeliveryIntercept(entity, remark, Boolean.TRUE);
            ThirdWarehouseCancelFbaOutboundReq cancelReq = BeanUtil.toBean(syncB2bThirdWarehouseService.newSyncDataToThirdWarehouseCancel(entity), ThirdWarehouseCancelFbaOutboundReq.class);
            proxyService.cancelFbaOutbound(cancelReq);
            BatchResultDTO resultDTO = buildDeliveryInterceptResult(id, entity.getCode());
            String msg = CharSequenceUtil.format("用户【{}】同步发起发货拦截，结果：{}", UserContext.getDefaultLoginUser().getUserName(), resultDTO.getMsg());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "发货拦截");
            return resultDTO;
        } else {
            // 手工发货类型不生成B2B拦截单，直接取消发货
            updateStatus(id, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", "", "", "", null);
            String msg = CharSequenceUtil.format("用户【{}】提交发货拦截申请成功,拦截成功", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "发货拦截");
        }
        return BatchResultDTO.success(id, entity.getCode(), "提交发货拦截成功");
    }

    private BatchResultDTO buildDeliveryInterceptResult(String id, String code) {
        SoB2bDeliveryInterceptEntity interceptEntity = soB2bDeliveryInterceptService.getLatestBySourceId(id);
        if (Objects.nonNull(interceptEntity)
                && SoB2bDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(interceptEntity.getHandleStatus())) {
            if (HandleResultEnum.SUCCESS.getCode().equals(interceptEntity.getHandleResult())) {
                return BatchResultDTO.success(id, code, "操作成功");
            }
            if (HandleResultEnum.FAILURE.getCode().equals(interceptEntity.getHandleResult())) {
                String msg = CharSequenceUtil.blankToDefault(interceptEntity.getHandleRemark(), "发货拦截失败");
                return BatchResultDTO.fail(id, code, msg);
            }
        }

        B2bThirdDeliveryEntity latestEntity = this.getById(id);
        if (Objects.nonNull(latestEntity)) {
            if (ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(latestEntity.getStatus())) {
                return BatchResultDTO.success(id, code, "操作成功");
            }
            if (ThirdDeliveryStatusEnum.INTERCEPTING.getCode().equals(latestEntity.getStatus())) {
                String msg = Objects.nonNull(interceptEntity) && CharSequenceUtil.isNotBlank(interceptEntity.getHandleRemark())
                        ? interceptEntity.getHandleRemark()
                        : "操作成功，等待拦截结果";
                return BatchResultDTO.success(id, code, msg);
            }
            String msg = CharSequenceUtil.blankToDefault(latestEntity.getErrorMessage(), "发货拦截失败");
            return BatchResultDTO.fail(id, code, msg);
        }
        return BatchResultDTO.fail(id, code, "发货拦截失败");
    }

    @Override
    public BatchResultDTO manualDelivery(B2bThirdDeliveryEntity entity) {
        if (isApiPushDelivery(entity)) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_MANUAL_ONLY_B2B_DISABLED);
        }
        if (!ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.SO_THIRD_DELIVERY_ONLY_WAIT_SHIPPED);
        }
        //更新状态为已发货 并生成出库单
        BatchResultDTO resultDTO = proxyService.updateStatus(entity.getId(), ThirdDeliveryStatusEnum.SHIPPED.getCode(), "", "", entity.getRemark(), "", LocalDateTime.now());
        if (Objects.nonNull(resultDTO) && resultDTO.getSuccess() && CharSequenceUtil.isNotBlank(resultDTO.getId())) {
            //提审销售出库单
            try {
                proxyService.submitApprove(resultDTO.getId());
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
        b2bCustomerPackingService.deleteByMainIds(Collections.singletonList(entity.getId()));
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
        responses.forEach(r -> {
            XxlJobHelper.log("b2b出库单编码code={}", r.getCode());
        });

        XxlJobHelper.log("开始更新{}平台B2B三方仓发货单状态", providerCode);
        List<B2bThirdDeliveryEntity> list = lambdaQuery().in(B2bThirdDeliveryEntity::getCode, codeList).list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (B2bThirdDeliveryEntity entity : list) {
            ThirdWarehouseQueryFbaOutboundResponse response = responses.stream().filter(r -> r.getCode().equals(entity.getCode())).findFirst().orElse(null);
            if (Objects.isNull(response)) {
                continue;
            }
            try {
                this.handleResultData(entity.getId(), response);
            } catch (Exception e) {
                XxlJobHelper.log("更新{}平台B2B三方发货单状态失败,id={},code={},error={}", providerCode, entity.getId(), entity.getCode(), e.getMessage(), e);
                log.error("更新{}平台B2B三方发货单状态失败,id={},code={}", providerCode, entity.getId(), entity.getCode(), e);
            }
        }
    }

    @Override
    public void handleResultData(String id, ThirdWarehouseQueryFbaOutboundResponse response) {
        log.info("处理B2B三方发货单状态,id={},response={}", id, JSONUtil.toJsonStr(response));
        B2bThirdDeliveryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            log.warn("处理B2B三方发货单状态失败，单据不存在,id={}", id);
            return;
        }
        if (Objects.isNull(response) || CharSequenceUtil.isBlank(response.getStatus())) {
            log.warn("处理B2B三方发货单状态失败，返回状态为空,id={}", id);
            return;
        }
        //订单已取消直接返回
        /**
         * 以下状态自动变更为取消发货，有拦截标识时清空拦截标识，记录拦截成功
         * EXCEPTION：出库异常
         * DISCARD：已作废
         * PROBLEM：问题件
         */
        String deliveryTimeStr = response.getDeliveryTimeStr();
        LocalDateTime deliveryTime = parseDeliveryTime(deliveryTimeStr);
        if (ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(response.getStatus())) {
            if (!canTransitToCancelDelivery(entity.getStatus())) {
                log.info("忽略取消发货状态回写,id={},code={},currentStatus={},thirdStatus={}", id, response.getCode(), entity.getStatus(), response.getStatus());
                return;
            }
            //拦截成功，更新B2B三方发货单状态 取消发货
            proxyService.updateStatus(id, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
            soB2bDeliveryInterceptService.handleResultBySourceId(id, HandleResultEnum.SUCCESS.getCode(), "海外仓取消出库成功", response.getTrackNo(), "");
        } else if (ThirdDeliveryStatusEnum.SHIPPED.getCode().equals(response.getStatus())) {
            if (!canTransitToShipped(entity.getStatus())) {
                log.info("忽略已发货状态回写,id={},code={},currentStatus={},thirdStatus={}", id, response.getCode(), entity.getStatus(), response.getStatus());
                return;
            }
            //已发货，更新B2B三方发货单状态 生成销售出库单
            BatchResultDTO resultDTO = proxyService.updateStatus(id, ThirdDeliveryStatusEnum.SHIPPED.getCode(), "", response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
            String soOutstockCode = "";
            if (Objects.nonNull(resultDTO) && resultDTO.getSuccess() && CharSequenceUtil.isNotBlank(resultDTO.getId())) {
                //提审销售出库单
                try {
                    proxyService.submitApprove(resultDTO.getId());
                    SoOutstockEntity soOutstockEntity = soOutstockService.getById(resultDTO.getId());
                    soOutstockCode = Objects.nonNull(soOutstockEntity) ? soOutstockEntity.getCode() : "";
                } catch (Exception e) {
                    soOutstockService.updateRemarkById(resultDTO.getId(), "自动审核失败" + e.getMessage());
                }
            }
            if (ThirdDeliveryStatusEnum.INTERCEPTING.getCode().equals(entity.getStatus())) {
                soB2bDeliveryInterceptService.handleResultBySourceId(id, HandleResultEnum.FAILURE.getCode(), "海外仓已出库，拦截失败", response.getTrackNo(), soOutstockCode);
            }
        } else if (ThirdDeliveryStatusEnum.INTERCEPTING.getCode().equals(response.getStatus())) {
            if (!canTransitToIntercepting(entity.getStatus())) {
                log.info("忽略拦截中状态回写,id={},code={},currentStatus={},thirdStatus={}", id, response.getCode(), entity.getStatus(), response.getStatus());
                return;
            }
            //拦截中 记录拦截标识
            proxyService.updateStatus(id, ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), "", response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
        }  else if (ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(response.getStatus())) {
            if (!canTransitToExceptionOrder(entity.getStatus())) {
                log.info("忽略异常订单状态回写,id={},code={},currentStatus={},thirdStatus={}", id, response.getCode(), entity.getStatus(), response.getStatus());
                return;
            }
            String errorMsg = getThirdWarehouseErrorMsg(response);
            proxyService.updateStatus(id, ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode(), errorMsg, response.getPlatformOrderCode(), "", response.getTrackNo(), deliveryTime);
            soB2bDeliveryInterceptService.handleResultBySourceId(id, HandleResultEnum.FAILURE.getCode(), CharSequenceUtil.blankToDefault(errorMsg, "海外仓出库异常"), response.getTrackNo(), "");
            operateLogService.addModuleOperateLog("海外仓出库异常，系统应拦截，为保证发货时效运营要求不予拦截，直接海外仓后台修改提交，异常信息:【" + errorMsg + "】", ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), id, "出库异常");
        } else {
            log.info("不操作的状态,id={},code={},status={}", id, response.getCode(), response.getStatus());
        }
    }
    @Override
    @DistributeLocker(keyName = "dto.referenceNo")
    public void syncOutboundStatus(PlatformOutboundDTO dto) {
        if (Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getReferenceNo())) {
            return;
        }
        String referenceNo = dto.getReferenceNo();
        if (!referenceNo.startsWith(BusinessNoConstant.SFFH)) {
            log.warn("B2B三方仓出库状态消息忽略，referenceNo={}", referenceNo);
            return;
        }
        B2bThirdDeliveryEntity entity = this.getLatestByCode(referenceNo);
        if (Objects.isNull(entity)) {
            log.warn("未找到B2B三方发货单，referenceNo={}", referenceNo);
            return;
        }
        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setCode(referenceNo);
        String providerCode = CharSequenceUtil.blankToDefault(dto.getProvider(), dto.getPlatform());
        response.setPlatform(providerCode);
        response.setPlatformOrderCode(dto.getOrderCode());
        response.setTrackNo(dto.getTrackNo());
        response.setPlatformOriginalStatus(dto.getThirdOrderStatus());
        response.setStatus(resolveMqSyncStatus(providerCode, dto));
        response.setErrorType(dto.getAbnormalProblemReason());
        response.setErrorReason(dto.getAbnormalProblemReason());
        response.setDeliveryTimeStr(Objects.nonNull(dto.getOutBoundTime()) ? dto.getOutBoundTime().toString() : null);
        if (CharSequenceUtil.isBlank(response.getStatus())) {
            log.info("B2B三方仓出库状态消息忽略，ERP状态为空，referenceNo={}, provider={}, thirdOrderStatus={}",
                    referenceNo, dto.getProvider(), dto.getThirdOrderStatus());
            return;
        }
        this.handleResultData(entity.getId(), response);
    }

    private String resolveMqSyncStatus(String providerCode, PlatformOutboundDTO dto) {
        String resolvedStatus = B2bThirdDeliveryStatusResolver.resolveErpStatus(providerCode, dto.getThirdOrderStatus());
        return CharSequenceUtil.blankToDefault(resolvedStatus, dto.getOrderStatus());
    }


    @Override
    public List<B2bThirdDeliveryEntity> listBySoIds(List<String> soIds) {
        if (CollUtil.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(B2bThirdDeliveryEntity::getSoId, soIds).ne(B2bThirdDeliveryEntity::getStatus, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode()).list();
    }

    @Override
    public B2bThirdDeliveryEntity getLatestByCode(String code) {
        if (CharSequenceUtil.isBlank(code)) {
            return null;
        }
        return lambdaQuery().eq(B2bThirdDeliveryEntity::getCode, code)
                .orderByDesc(B2bThirdDeliveryEntity::getCreateTime)
                .last("limit 1")
                .one();
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
    public void createFbaOutbound(ThirdWarehouseCreateFbaOutboundReq req) {
        String sourceId = req.getSourceId();
        B2bThirdDeliveryEntity entity = this.getById(sourceId);
        if (Objects.isNull(entity)) {
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_NOT_EXIST_GENERIC.getMsg(), req.getSourceCode()), "", "", "", null);
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
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), ApiError.COMMON_NO_DELIVERY_SKU.getMsg(), "", "", "", null);
            return;
        }
        req.setItems(items);

        ThirdWarehouseService thirdWarehouseService = thirdWarehouseRegistry.getHandler(req.getThirdWarehouseProvideCode());
        if (Objects.isNull(thirdWarehouseService)) {
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_PROVIDER_SERVICE_NOT_ENABLED.getMsg(), req.getThirdWarehouseProvideCode()), "", "", "", null);
            return;
        }
        ApiResult<String> fbaOutboundBill = createFbaOutboundBill(thirdWarehouseService, req, entity.getPlatformOrderCode(), 0);
        if (fbaOutboundBill.isSuccess()) {
            // 创建成功
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", fbaOutboundBill.getData(), "", "", null);
            return;
        }
        // 创建失败，尝试查询是否实际已创建成功
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getReferenceNo()));
        if (CharSequenceUtil.isNotBlank(entity.getPlatformOrderCode())) {
            queryOutboundReq.setPlatformOrderCodeList(Collections.singletonList(entity.getPlatformOrderCode()));
        }
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());

        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult = thirdWarehouseService.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());

        if(Objects.equals(PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode(),req.getThirdWarehouseProvideCode())) {
            B2bThirdDeliveryDTO.ConvertDTO convertDTO = convertData(queryResult);
            if (queryResult.isSuccess() && Objects.nonNull(queryResult.getData()) && !queryResult.getData().isEmpty()) {
                updateZhongBaoStatus(sourceId, queryResult.getData().get(0).getStatus(), "", convertDTO.getPlatformOrderCode(), "", convertDTO.getTrackNo(), convertDTO.getDeliveryTime());
                return;
            } else {
                updateZhongBaoStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), fbaOutboundBill.getMsg(), "", "", "",null);
                return;
            }
        }

        B2bThirdDeliveryDTO.ConvertDTO convertDTO = convertData(queryResult);
        if (CharSequenceUtil.isNotBlank(convertDTO.getPlatformOrderCode())) {
            // 查询发现订单实际已创建成功
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", convertDTO.getPlatformOrderCode(), "", convertDTO.getTrackNo(), convertDTO.getDeliveryTime());
        } else {
            // 确认创建失败
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), fbaOutboundBill.getMsg(), "", "", convertDTO.getTrackNo(), convertDTO.getDeliveryTime());
        }
    }

    public B2bThirdDeliveryDTO.ConvertDTO convertData(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult){
        B2bThirdDeliveryDTO.ConvertDTO convertDTO = new B2bThirdDeliveryDTO.ConvertDTO();
        convertDTO.setPlatformOrderCode(getPlatformOrderCode(queryResult));
        convertDTO.setTrackNo(getTrackNo(queryResult));
        convertDTO.setDeliveryTime(getDeliveryTime(queryResult));
        return convertDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO updateZhongBaoStatus(String id, String status, String errorMsg, String platformOrderCode, String remark, String trackNo, LocalDateTime deliveryTime) {
        B2bThirdDeliveryEntity old = this.getById(id);
        if (Objects.isNull(old)) {
            log.warn("单据【{}】不存在", id);
            return null;
        }
        if (status.equals(old.getStatus())) {
            return null;
        }

        if (Objects.equals(ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(),status)) {
            //-1=>已取消：数大臣自动发起拦截，走拦截逻辑接口取消订单；
            // 如果订单已经是拦截中，则直接拦截成功，发货单变更为取消发货，订单变更为审核不通过-待配货
            service.updateStatus(id, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", platformOrderCode, remark, trackNo, deliveryTime);
        }else if (Objects.equals(ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode(),status)){
            //-2=>异常：数大臣单据不做状态变更，但是三方发货单需要增加操作日志记录详情：海外仓出库异常，系统应拦截，
            // 为保证发货时效运营要求不予拦截，直接海外仓后台修改提交，异常信息【errorReason】
            operateLogService.addModuleOperateLog("海外仓出库异常，系统应拦截，为保证发货时效运营要求不予拦截，直接海外仓后台修改提交，异常信息:【" + errorMsg + "】", ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), id, "出库异常");
        }else if (Objects.equals(ThirdDeliveryStatusEnum.SHIPPED.getCode(),status)){
            //5=>已出库：数大臣自动变更B2B三方发货单和订单已发货，并生成出库单
            BatchResultDTO resultDTO = service.updateStatus(id, ThirdDeliveryStatusEnum.SHIPPED.getCode(), errorMsg, platformOrderCode, remark, trackNo, deliveryTime);
            if (Objects.nonNull(resultDTO) && resultDTO.getSuccess()) {
                String outstockId = resultDTO.getId();
                SoOutstockEntity soOutstockEntity = soOutstockService.getById(outstockId);
                if (Objects.isNull(soOutstockEntity)) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "销售出库单");
                }
                return BatchResultDTO.success(soOutstockEntity.getId(), soOutstockEntity.getCode(), "销售出库单生成");
            }
        }else if (Objects.equals(ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),status)
                || Objects.equals(ThirdDeliveryStatusEnum.CREATING.getCode(),status)
                || Objects.equals(ThirdDeliveryStatusEnum.INTERCEPTING.getCode(),status)){
            //1=>草稿,2=>待审核,3=>已审核,4=>待出库：数大臣单据不做状态变更
            return BatchResultDTO.success();
        } else if (Objects.equals(ThirdDeliveryStatusEnum.FAILED.getCode(),status)){
            //zhongbao创建失败
            service.updateStatus(id, ThirdDeliveryStatusEnum.FAILED.getCode(), errorMsg, "", "", "", null);
            log.warn("三方接口创建失败,id【{}】", id);
        }
        return BatchResultDTO.success();
    }

    private LocalDateTime getDeliveryTime(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult) {
        if (queryResult.isSuccess() && CollUtil.isNotEmpty(queryResult.getData())) {
            return parseDeliveryTime(queryResult.getData().get(0).getDeliveryTimeStr());
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

    private ApiResult<String> createFbaOutboundBill(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req, String knownPlatformOrderCode, final int retryCount) {
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getReferenceNo()));
        if (CharSequenceUtil.isNotBlank(knownPlatformOrderCode)) {
            queryOutboundReq.setPlatformOrderCodeList(Collections.singletonList(knownPlatformOrderCode));
        }
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());
        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> listApiResult = service.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());
        String platformOrderCode = getPlatformOrderCode(listApiResult);
        if (CharSequenceUtil.isNotBlank(platformOrderCode)) {
            return ApiResult.success(platformOrderCode);
        }

        try {
            prepareCreateFbaOutboundAttachment(service, req);
            preparePackingShipmentFiles(service, req);
            return service.createFbaOutboundBill(req, req.getAuthId());
        } catch (Exception e) {
            log.warn("第{}次执行失败: {}", retryCount + 1, e.getMessage());

            if (retryCount + 1 < MAX_RETRY_COUNT) {
                try {
                    log.info("{}秒后进行第{}次重试", RETRY_DELAY_SECONDS / 1000, retryCount + 2);
                    Thread.sleep(RETRY_DELAY_SECONDS);
                    return createFbaOutboundBill(service, req, knownPlatformOrderCode, retryCount + 1);
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

    private void prepareCreateFbaOutboundAttachment(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req) {
        if (isValidAttachmentId(req.getFileId())) {
            return;
        }
        if (!needUploadAttachment(req.getThirdWarehouseProvideCode())) {
            return;
        }
        if (StrUtil.isBlank(req.getFileUrl()) && StrUtil.isBlank(req.getFileName()) && StrUtil.isBlank(req.getFileBase64())) {
            return;
        }
        if (StrUtil.isBlank(req.getFileBase64())) {
            throw new ServiceException("B2B三方仓附件内容为空，无法上传附件");
        }

        ThirdWarehouseUploadFileReq uploadFileReq = new ThirdWarehouseUploadFileReq();
        uploadFileReq.setAuthId(req.getAuthId());
        uploadFileReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());
        uploadFileReq.setOrderCode(req.getReferenceNo());
        uploadFileReq.setFileData(req.getFileBase64());
        uploadFileReq.setFileUrl(req.getFileUrl());
        uploadFileReq.setFileName(req.getFileName());
        if (PlatformDictEnum.ANTU.getCode().equalsIgnoreCase(req.getThirdWarehouseProvideCode())
         || PlatformDictEnum.SPT.getCode().equalsIgnoreCase(req.getThirdWarehouseProvideCode())) {
            uploadFileReq.setFileType(getAttachmentExtension(req));
            uploadFileReq.setModule("order_attach");
        } else if (PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(req.getThirdWarehouseProvideCode())) {
            uploadFileReq.setFileType(GOOD_CANG_ORDER_PACKING_ATTACHMENT);
        }

        ApiResult<ThirdWarehouseUploadFileResponse> uploadFileResult = service.uploadFile(uploadFileReq, req.getAuthId());
        if (!uploadFileResult.isSuccess() || Objects.isNull(uploadFileResult.getData()) || Objects.isNull(uploadFileResult.getData().getAttachId())) {
            throw new ServiceException("上传B2B三方仓附件失败:{}", uploadFileResult.getMsg());
        }
        req.setFileId(String.valueOf(uploadFileResult.getData().getAttachId()));
        if (PlatformDictEnum.ANTU.getCode().equalsIgnoreCase(req.getThirdWarehouseProvideCode())
                || PlatformDictEnum.SPT.getCode().equalsIgnoreCase(req.getThirdWarehouseProvideCode())) {
            req.setFileType(getAttachmentExtension(req));
        }
    }

    private boolean isValidAttachmentId(String attachmentId) {
        return StrUtil.isNotBlank(attachmentId) && !"null".equalsIgnoreCase(attachmentId.trim());
    }

    private boolean needUploadAttachment(String providerCode) {
        return PlatformDictEnum.ANTU.getCode().equalsIgnoreCase(providerCode)
                || PlatformDictEnum.SPT.getCode().equalsIgnoreCase(providerCode)
                || PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(providerCode);
    }

    private String getAttachmentExtension(ThirdWarehouseCreateFbaOutboundReq req) {
        String fileType = FileUtil.getFileExtension(req.getFileName());
        if (StrUtil.isNotBlank(fileType)) {
            return fileType;
        }
        return FileUtil.getFileExtension(req.getFileUrl());
    }

    private void preparePackingShipmentFiles(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req) {
        if (!PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(req.getThirdWarehouseProvideCode())
                || CollUtil.isEmpty(req.getPackingDetailList())) {
            return;
        }
        Map<Integer, ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem> boxHeadMap = new LinkedHashMap<>();
        for (ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem item : req.getPackingDetailList()) {
            if (item.getBoxSeq() != null && !boxHeadMap.containsKey(item.getBoxSeq())) {
                boxHeadMap.put(item.getBoxSeq(), item);
            }
        }
        for (ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem boxHead : boxHeadMap.values()) {
            if (CollUtil.isNotEmpty(boxHead.getShipmentFileList())) {
                preparePackingShipmentFileList(service, req, boxHead);
                continue;
            }
            if (Objects.nonNull(boxHead.getShipmentFileId()) || StrUtil.isBlank(boxHead.getShipmentFileBase64())) {
                continue;
            }
            Integer shipmentFileId = uploadPackingShipmentFile(service, req, boxHead);
            for (ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem item : req.getPackingDetailList()) {
                if (Objects.equals(item.getBoxSeq(), boxHead.getBoxSeq())) {
                    item.setShipmentFileId(shipmentFileId);
                }
            }
        }
    }

    private void preparePackingShipmentFileList(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req,
                                                ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem boxHead) {
        for (ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem shipmentFile : boxHead.getShipmentFileList()) {
            if (Objects.nonNull(shipmentFile.getShipmentFileId()) || StrUtil.isBlank(shipmentFile.getShipmentFileBase64())) {
                continue;
            }
            shipmentFile.setShipmentFileId(uploadPackingShipmentFile(service, req, shipmentFile));
        }
        for (ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem item : req.getPackingDetailList()) {
            if (Objects.equals(item.getBoxSeq(), boxHead.getBoxSeq())) {
                item.setShipmentFileList(boxHead.getShipmentFileList());
            }
        }
    }

    private Integer uploadPackingShipmentFile(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req,
                                              ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem boxHead) {
        ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem shipmentFile = ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem.builder()
                .shipmentFileBase64(boxHead.getShipmentFileBase64())
                .shipmentFileUrl(boxHead.getShipmentFileUrl())
                .shipmentFileName(boxHead.getShipmentFileName())
                .build();
        return uploadPackingShipmentFile(service, req, shipmentFile);
    }

    private Integer uploadPackingShipmentFile(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req,
                                              ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem shipmentFile) {
        ThirdWarehouseUploadFileReq uploadFileReq = new ThirdWarehouseUploadFileReq();
        uploadFileReq.setAuthId(req.getAuthId());
        uploadFileReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());
        uploadFileReq.setOrderCode(req.getReferenceNo());
        uploadFileReq.setFileData(shipmentFile.getShipmentFileBase64());
        uploadFileReq.setFileUrl(shipmentFile.getShipmentFileUrl());
        uploadFileReq.setFileName(shipmentFile.getShipmentFileName());
        uploadFileReq.setFileType("SHIPMENT_LABEL_ATTACHMENT");
        ApiResult<ThirdWarehouseUploadFileResponse> uploadFileResult = service.uploadFile(uploadFileReq, req.getAuthId());
        if (!uploadFileResult.isSuccess() || Objects.isNull(uploadFileResult.getData()) || Objects.isNull(uploadFileResult.getData().getAttachId())) {
            throw new ServiceException("上传B2B装箱货件标签失败:{}", uploadFileResult.getMsg());
        }
        return uploadFileResult.getData().getAttachId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancelFbaOutbound(ThirdWarehouseCancelFbaOutboundReq req) {
        String sourceId = req.getSourceId();
        B2bThirdDeliveryEntity entity = this.getById(sourceId);
        if (Objects.isNull(entity)) {
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_NOT_EXIST_GENERIC.getMsg(), req.getSourceCode()), "", "", "", null);
            return;
        }
        String fallbackStatus = CharSequenceUtil.blankToDefault(entity.getStatus(), ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode());

        ThirdWarehouseService thirdWarehouseService = thirdWarehouseRegistry.getHandler(req.getThirdWarehouseProvideCode());
        if (Objects.isNull(thirdWarehouseService)) {
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.COMMON_PROVIDER_SERVICE_NOT_ENABLED.getMsg(), req.getThirdWarehouseProvideCode()), "", "", "", null);
            return;
        }
        ApiResult<String> fbaOutboundBill = cancelFbaOutboundBill(thirdWarehouseService, req, 0);
        if (!fbaOutboundBill.isSuccess()) {
            // 创建失败
            proxyService.updateStatus(sourceId, fallbackStatus, "", "", "", "", null);
            String msg = "三方仓拦截请求失败：" + CharSequenceUtil.blankToDefault(fbaOutboundBill.getMsg(), "海外仓取消出库失败");
            soB2bDeliveryInterceptService.handleResultBySourceId(sourceId, HandleResultEnum.FAILURE.getCode(), msg, entity.getTrackNo(), "");
            return;
        }

        if (B2bThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode().equalsIgnoreCase(fbaOutboundBill.getData())) {
            proxyService.updateStatus(sourceId, fallbackStatus, "", "", "", "", null);
            String msg = "三方仓已返回拦截失败："
                    + CharSequenceUtil.blankToDefault(fbaOutboundBill.getMsg(), "海外仓取消出库失败");
            soB2bDeliveryInterceptService.handleResultBySourceId(sourceId, HandleResultEnum.FAILURE.getCode(),
                    msg, entity.getTrackNo(), "");
            return;
        }

        if (B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode().equalsIgnoreCase(fbaOutboundBill.getData())) {
            proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", req.getOrderCode(), "", entity.getTrackNo(), null);
            soB2bDeliveryInterceptService.handleResultBySourceId(sourceId, HandleResultEnum.SUCCESS.getCode(), "海外仓取消出库成功", entity.getTrackNo(), "");
            return;
        }

        proxyService.updateStatus(sourceId, ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), "", "", "", "", null);
        tryHandleCancelFbaOutboundResult(sourceId, thirdWarehouseService, req);
    }

    private void tryHandleCancelFbaOutboundResult(String sourceId, ThirdWarehouseService thirdWarehouseService, ThirdWarehouseCancelFbaOutboundReq req) {
        if (CharSequenceUtil.isBlank(req.getErpOrderCode())) {
            return;
        }
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getErpOrderCode()));
        if (CharSequenceUtil.isNotBlank(req.getOrderCode())) {
            queryOutboundReq.setPlatformOrderCodeList(Collections.singletonList(req.getOrderCode()));
        }
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());

        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult;
        try {
            queryResult = thirdWarehouseService.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());
        } catch (Exception e) {
            log.warn("取消B2B三方出库后立即查询状态失败,sourceId={},erpOrderCode={},msg={}", sourceId, req.getErpOrderCode(), e.getMessage());
            markCancelAcceptedQueryFailed(sourceId, e.getMessage());
            return;
        }
        if (!queryResult.isSuccess() || CollUtil.isEmpty(queryResult.getData())) {
            String queryMsg = Objects.nonNull(queryResult) ? queryResult.getMsg() : "";
            markCancelAcceptedQueryFailed(sourceId, queryMsg);
            return;
        }
        ThirdWarehouseQueryFbaOutboundResponse response = queryResult.getData().get(0);
        if (Objects.isNull(response) || CharSequenceUtil.isBlank(response.getStatus())) {
            markCancelAcceptedQueryFailed(sourceId, "查询结果为空");
            return;
        }
        this.handleResultData(sourceId, response);
    }

    private String getThirdWarehouseErrorMsg(ThirdWarehouseQueryFbaOutboundResponse response) {
        if (Objects.isNull(response)) {
            return CharSequenceUtil.EMPTY;
        }
        return CharSequenceUtil.blankToDefault(response.getErrorReason(), response.getErrorType());
    }

    private void markCancelAcceptedQueryFailed(String sourceId, String queryErrorMsg) {
        SoB2bDeliveryInterceptEntity entity = soB2bDeliveryInterceptService.getLatestBySourceId(sourceId);
        if (Objects.isNull(entity) || SoB2bDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(entity.getHandleStatus())) {
            return;
        }
        String msg = CANCEL_ACCEPTED_QUERY_FAILED_MSG;
        if (CharSequenceUtil.isNotBlank(queryErrorMsg)) {
            msg = msg + "，查询失败原因：" + queryErrorMsg;
        }
        entity.setHandleRemark(msg);
        soB2bDeliveryInterceptService.updateById(entity);
        operateLogService.addModuleOperateLog("拦截请求已提交三方仓，但立即查询状态失败，备注：" + msg,
                ModuleTypeEnum.SO_B2B_DELIVERY_INTERCEPT.getCode(), entity.getId(), "状态查询失败");
    }

    @Override
    public List<B2bThirdDeliveryDTO.OtherWarehouseOperationDescriptionDTO> listWarehouseOperationDescription(B2bThirdDeliveryDTO.ThirdWarehousePlatformDTO thirdWarehousePlatformDTO) {
        List<B2bThirdDeliveryDTO.OtherWarehouseOperationDescriptionDTO> resultList = new ArrayList<>();
        List<CfgThirdWarehouseOperationDescriptionEntity> queryList = cfgThirdWarehouseOperationDescriptionService.lambdaQuery()
                .eq(CfgThirdWarehouseOperationDescriptionEntity::getThirdWarehouseCode, thirdWarehousePlatformDTO.getThirdWarehouse())
                .list();
        List<String> idList = queryList.stream()
                .filter(item -> Objects.equals(item.getInputType(), WarsehouseOperationDescriptionEnum.DROP_DOWN.getCode()))
                .map(CfgThirdWarehouseOperationDescriptionEntity::getId)
                .collect(Collectors.toList());
        List<CfgThirdWarehouseOperationDescriptionValueEntity> valueList = new ArrayList<>();
        if (!idList.isEmpty()) {
            // 下拉框值
            valueList = cfgThirdWarehouseOperationDescriptionValueService.lambdaQuery()
                    .in(CfgThirdWarehouseOperationDescriptionValueEntity::getMainId, idList)
                    .list();
        }

        for (CfgThirdWarehouseOperationDescriptionEntity entity : queryList) {
            B2bThirdDeliveryDTO.OtherWarehouseOperationDescriptionDTO resultDTO = new B2bThirdDeliveryDTO.OtherWarehouseOperationDescriptionDTO();
            BeanUtils.copyProperties(entity,resultDTO);
            resultDTO.setThirdWarehouse(entity.getThirdWarehouseCode());
            resultDTO.setOperationTypeName(WarehouseOperationTypeEnum.getName(entity.getOperationType()));
            // 过滤出当前主表ID对应的下拉框值
            List<CfgThirdWarehouseOperationDescriptionValueEntity> currentValues = valueList.stream()
                    .filter(value -> Objects.equals(value.getMainId(), entity.getId()))
                    .collect(Collectors.toList());
            List<B2bThirdDeliveryDTO.InputValueDTO> inputValueDTOS = BeanMapperUtils.copyList(B2bThirdDeliveryDTO.InputValueDTO.class, currentValues);
            resultDTO.setInputValueList(inputValueDTOS);
            resultList.add(resultDTO);
        }
        return resultList;
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

        boolean isZhongBao = Boolean.FALSE;
        OverseasProviderWarehouseEntity overseasProviderWarehouse = overseasProviderWarehouseService.getByWarehouseId(commonDTO.getDeliveryWarehouseId());
        if (Objects.nonNull(overseasProviderWarehouse)) {
            OverseasProviderEntity overseasProvider = overseasProviderService.getById(overseasProviderWarehouse.getMainId());
            if (Objects.nonNull(overseasProvider)) {
                if (Objects.equals(PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode(),overseasProvider.getCode())) {
                    isZhongBao = Boolean.TRUE;
                }
            }
        }

        if (isZhongBao) {
            if (CollUtil.isNotEmpty(warehouseOperationTypeDTOList)) {
                for (B2bThirdDeliveryDTO.WarehouseOperationTypeDTO warehouseOperationTypeDTO : warehouseOperationTypeDTOList) {
                    if (Objects.equals(warehouseOperationTypeDTO.getWarehouseOperationType(), ZhongBaoOperationDescriptionEnum.LIMIT_PLATE_NUM.getCode())) {
                        // 正则表达式：匹配正整数（无前导零）
                        if (!Pattern.matches("^[1-9]\\d*$", warehouseOperationTypeDTO.getOperationDesc())) {
                            throw new IllegalArgumentException("操作值必须是正整数（如：1, 2, 3...）");
                        }
                    }

                    if (Objects.equals(warehouseOperationTypeDTO.getWarehouseOperationType(), ZhongBaoOperationDescriptionEnum.LIMIT_PLATE_HEIGHT.getCode())
                            || Objects.equals(warehouseOperationTypeDTO.getWarehouseOperationType(), ZhongBaoOperationDescriptionEnum.LIMIT_PLATE_WEIGHT.getCode())) {
                        try {
                            BigDecimal number = new BigDecimal(warehouseOperationTypeDTO.getOperationDesc());

                            // 检查是否大于0
                            if (number.compareTo(BigDecimal.ZERO) <= 0) {
                                throw new IllegalArgumentException("操作值必须大于0");
                            }

                            //保留三位小数
                            BigDecimal processedNumber = number.setScale(3, RoundingMode.DOWN);
                            warehouseOperationTypeDTO.setOperationDesc(processedNumber.toString());

                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("操作值必须是有效的数字格式");
                        }
                    }
                }

                String operationDesc = warehouseOperationTypeDTOList.stream()
                        .map(B2bThirdDeliveryDTO.WarehouseOperationTypeDTO::getOperationDesc)
                        .collect(Collectors.joining(","));
                String warehouseOperationType = warehouseOperationTypeDTOList.stream()
                        .map(B2bThirdDeliveryDTO.WarehouseOperationTypeDTO::getWarehouseOperationType)
                        .collect(Collectors.joining(","));
                b2bThirdDeliveryEntity.setOperationDesc(operationDesc);
                b2bThirdDeliveryEntity.setWarehouseOperationType(warehouseOperationType);

            }
        } else {
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

    private boolean canStartDeliveryIntercept(String currentStatus) {
        return isOneOf(currentStatus,
                ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),
                ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode());
    }

    private boolean canTransitToShipped(String currentStatus) {
        return isOneOf(currentStatus,
                ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),
                ThirdDeliveryStatusEnum.INTERCEPTING.getCode(),
                ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode(),
                ThirdDeliveryStatusEnum.SHIPPED.getCode());
    }

    private boolean canTransitToCancelDelivery(String currentStatus) {
        return isOneOf(currentStatus,
                ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),
                ThirdDeliveryStatusEnum.INTERCEPTING.getCode(),
                ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode(),
                ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode());
    }

    private boolean canTransitToIntercepting(String currentStatus) {
        return isOneOf(currentStatus,
                ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),
                ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode(),
                ThirdDeliveryStatusEnum.INTERCEPTING.getCode());
    }

    private boolean canTransitToExceptionOrder(String currentStatus) {
        return isOneOf(currentStatus,
                ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),
                ThirdDeliveryStatusEnum.INTERCEPTING.getCode(),
                ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode());
    }

    private boolean isOneOf(String currentStatus, String... statusList) {
        return Arrays.stream(statusList).anyMatch(status -> Objects.equals(status, currentStatus));
    }

    private boolean isFieldValueChanged(String oldValue, String newValue) {
        return CharSequenceUtil.isNotBlank(newValue) && !Objects.equals(oldValue, newValue);
    }

    private boolean isApiPushDelivery(B2bThirdDeliveryEntity entity) {
        if (Objects.isNull(entity)) {
            return false;
        }
        if (CharSequenceUtil.isNotBlank(entity.getPushType())) {
            return Objects.equals(B2BDeliveryPushTypeEnum.API.getCode(), entity.getPushType());
        }
        return Boolean.TRUE.equals(entity.getIsApiDelivery());
    }

    private void createB2bDeliveryIntercept(B2bThirdDeliveryEntity entity, String remark, Boolean isApiType) {
        List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        SoB2bDeliveryInterceptDTO.AddDTO addDTO = new SoB2bDeliveryInterceptDTO.AddDTO();
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setSourceType(Boolean.TRUE.equals(isApiType) ? SoB2bDeliveryInterceptSourceTypeEnum.API.getCode() : SoB2bDeliveryInterceptSourceTypeEnum.MANUAL.getCode());
        addDTO.setSoId(entity.getSoId());
        addDTO.setSoCode(entity.getSoCode());
        addDTO.setBillType(OrderTypeEnum.B2B.getCode());
        addDTO.setThirdDeliveryCode(entity.getCode());
        addDTO.setThirdWarehouseOrderCode(entity.getPlatformOrderCode());
        addDTO.setLogisticsChannelId(entity.getLogisticsChannelId());
        addDTO.setLogisticsChannelName(entity.getLogisticsChannelName());
        addDTO.setTransportNo(entity.getTrackNo());
        addDTO.setRemark(remark);
        if (Boolean.TRUE.equals(isApiType)) {
            addDTO.setHandleStatus(SoB2bDeliveryInterceptStatusEnum.WAIT_HANDLE.getStatus());
        } else {
            addDTO.setHandleStatus(SoB2bDeliveryInterceptStatusEnum.HANDLE.getStatus());
            addDTO.setHandleResult(HandleResultEnum.SUCCESS.getCode());
            addDTO.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
            addDTO.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
            addDTO.setHandleUserId(UserContext.getDefaultLoginUser().getUid());
            addDTO.setHandleUserName(UserContext.getDefaultLoginUser().getUserName());
            addDTO.setHandleTime(LocalDateTime.now());
        }
        List<SoB2bDeliveryInterceptDetailDTO.AddDTO> detailDTOList = detailEntityList.stream().map(item -> {
            SoB2bDeliveryInterceptDetailDTO.AddDTO detailDTO = new SoB2bDeliveryInterceptDetailDTO.AddDTO();
            detailDTO.setSkuId(item.getDeliverySkuId());
            detailDTO.setSkuNo(item.getDeliverySkuNo());
            detailDTO.setDeliveryQty(item.getDeliveryQty());
            detailDTO.setWarehouseId(entity.getDeliveryWarehouseId());
            detailDTO.setWarehouseName(entity.getDeliveryWarehouseName());
            detailDTO.setWarehouseLocation("");
            detailDTO.setSourceDetailId(item.getId());
            return detailDTO;
        }).collect(Collectors.toList());
        addDTO.setDetailList(detailDTOList);
        soB2bDeliveryInterceptService.add(addDTO);
    }

    private LocalDateTime parseDeliveryTime(String deliveryTimeStr) {
        if (StrUtil.isBlank(deliveryTimeStr)) {
            return null;
        }
        List<DateTimeFormatter> formatterList = Arrays.asList(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        for (DateTimeFormatter formatter : formatterList) {
            try {
                return LocalDateTime.parse(deliveryTimeStr, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    public void downloadPackingTemplate(HttpServletResponse response) {
        String excelPath = "excel/b2bCustomerPackingTemplate.xlsx";
        String fileName = "B2B客户装箱明细导入模板";
        try {
            ExcelUtil.downloadTemplate(excelPath, fileName, response);
        } catch (Exception e) {
            log.error("下载装箱明细模板失败", e);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public B2bCustomerPackingDTO.ImportDTO importPackingDetail(B2bCustomerPackingDTO.PackingExcelImportDTO excelImportDTO) {
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainId(excelImportDTO.getSoId());
        List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> detailList = getPackingImportDetailList(excelImportDTO.getSoId(), soDetailList);
        B2bCustomerPackingExcelListener listener = new B2bCustomerPackingExcelListener(detailList);
        try {
            EasyExcel.read(excelImportDTO.getExcelFile().getInputStream(), B2bCustomerPackingImportExcelDTO.class, listener).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入装箱明细失败", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ExcelCommonException e) {
            log.error("导入装箱明细格式错误", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        B2bCustomerPackingDTO.ImportDTO importDTO = new B2bCustomerPackingDTO.ImportDTO();
        importDTO.setSuccessList(listener.getSuccessList());
        List<B2bCustomerPackingImportExcelDTO> errorList = listener.getErrorList();
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "B2B客户装箱明细导入错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, B2bCustomerPackingImportExcelDTO.class);
            if (!file.isDirectory()) {
                try {
                    importDTO.setErrorUrl(FastDFSClientUtil.uploadFile(file, fileName));
                } finally {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException e) {
                        log.warn("删除装箱明细导入错误临时文件失败，file={}", file.getAbsolutePath());
                    }
                }
            }
        }
        return importDTO;
    }

    private List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> getPackingImportDetailList(String soId, List<SoDetailEntity> soDetailList) {
        if (CollUtil.isEmpty(soDetailList)) {
            return Collections.emptyList();
        }
        B2bThirdDeliveryDTO.ViewQueryDTO viewQueryDTO = new B2bThirdDeliveryDTO.ViewQueryDTO();
        viewQueryDTO.setSoId(soId);
        viewQueryDTO.setSoDetailIds(soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList()));
        B2bThirdDeliveryDTO.ViewDTO viewDTO = soInfoFeign.getB2bThirdDeliveryView(viewQueryDTO);
        if (Objects.nonNull(viewDTO) && CollUtil.isNotEmpty(viewDTO.getDetailList())) {
            return viewDTO.getDetailList().stream().map(e -> {
                com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO dto = new com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO();
                dto.setSkuId(e.getSkuId());
                dto.setSkuNo(e.getSkuNo());
                dto.setProductName(e.getProductName());
                dto.setSaleQty(e.getSaleQty());
                dto.setDeliveryQty(e.getDeliveryQty());
                dto.setPerBoxQty(e.getPerBoxQty());
                dto.setDeliverySkuId(e.getDeliverySkuId());
                dto.setDeliverySkuNo(e.getDeliverySkuNo());
                dto.setWarehousePlatformSku(e.getWarehousePlatformSku());
                dto.setBoxQty(e.getBoxQty());
                dto.setBoxSpecNo(e.getBoxSpecNo());
                return dto;
            }).collect(Collectors.toList());
        }
        return enrichPackingImportDetailList(toPackingImportDetailList(soDetailList), soDetailList);
    }

    private List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> toPackingImportDetailList(List<SoDetailEntity> detailEntities) {
        if (CollUtil.isEmpty(detailEntities)) {
            return Collections.emptyList();
        }
        return detailEntities.stream().map(e -> {
            com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO dto = new com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO();
            dto.setSkuId(e.getSkuId());
            dto.setSkuNo(e.getSkuNo());
            dto.setProductName(e.getProductName());
            dto.setSaleQty(e.getQty());
            dto.setDeliveryQty(e.getDeliveryQty());
            dto.setPerBoxQty(e.getPerBoxQty());
            dto.setDeliverySkuId(e.getDeliverySkuId());
            dto.setDeliverySkuNo(e.getDeliverySkuNo());
            dto.setWarehousePlatformSku(CharSequenceUtil.blankToDefault(e.getPlatformSkuNo(), e.getDeliverySkuNo()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> enrichPackingImportDetailList(
            List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> detailList,
            List<SoDetailEntity> soDetailList) {
        if (CollUtil.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        Map<String, String> productNameMap = getProductNameMap(detailList);
        Map<String, String> warehouseSkuMap = getWarehouseSkuMap(soDetailList);
        detailList.forEach(e -> {
            if (CharSequenceUtil.isBlank(e.getProductName())) {
                e.setProductName(CharSequenceUtil.blankToDefault(productNameMap.get(e.getSkuId()), productNameMap.get(e.getDeliverySkuId())));
            }
            if (CharSequenceUtil.isBlank(e.getWarehousePlatformSku())) {
                e.setWarehousePlatformSku(warehouseSkuMap.get(e.getSkuNo()));
            }
        });
        return detailList;
    }

    private Map<String, String> getProductNameMap(List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> detailList) {
        Set<String> skuIds = new HashSet<>();
        detailList.forEach(e -> {
            if (CharSequenceUtil.isNotBlank(e.getSkuId())) {
                skuIds.add(e.getSkuId());
            }
            if (CharSequenceUtil.isNotBlank(e.getDeliverySkuId())) {
                skuIds.add(e.getDeliverySkuId());
            }
        });
        Map<String, String> productNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuIds)) {
            List<ProductDetailEntity> productDetailList;
            try {
                productDetailList = plmTaskFeign.getByIdList(new ArrayList<>(skuIds));
            } catch (Exception e) {
                log.warn("B2B装箱导入获取产品名称失败，跳过产品名称补全, skuIds={}", skuIds, e);
                return productNameMap;
            }
            if (CollUtil.isNotEmpty(productDetailList)) {
                for (ProductDetailEntity productDetail : productDetailList) {
                    if (CharSequenceUtil.isNotBlank(productDetail.getId()) && CharSequenceUtil.isNotBlank(productDetail.getName())) {
                        productNameMap.putIfAbsent(productDetail.getId(), productDetail.getName());
                    }
                }
            }
        }
        return productNameMap;
    }

    private Map<String, String> getWarehouseSkuMap(List<SoDetailEntity> soDetailList) {
        Map<String, String> warehouseSkuMap = new HashMap<>();
        if (CollUtil.isNotEmpty(soDetailList)) {
            for (SoDetailEntity soDetail : soDetailList) {
                String warehousePlatformSku = CharSequenceUtil.blankToDefault(soDetail.getPlatformSkuNo(), soDetail.getDeliverySkuNo());
                if (CharSequenceUtil.isNotBlank(soDetail.getSkuNo()) && CharSequenceUtil.isNotBlank(warehousePlatformSku)) {
                    warehouseSkuMap.putIfAbsent(soDetail.getSkuNo(), warehousePlatformSku);
                }
            }
        }
        return warehouseSkuMap;
    }

    private void normalizePackingFields(B2bThirdDeliveryEntity entity, B2bThirdDeliveryDTO.CommonDTO commonDTO) {
        String packingType = CharSequenceUtil.blankToDefault(commonDTO.getPackingType(), B2bPackingTypeEnum.WAREHOUSE_SELF.getCode());
        entity.setPackingType(packingType);
        if (B2bPackingTypeEnum.WAREHOUSE_SELF.getCode().equals(packingType)) {
            entity.setLabelsPerBox(0);
            commonDTO.setLabelsPerBox(0);
        } else {
            entity.setLabelsPerBox(commonDTO.getLabelsPerBox() != null ? commonDTO.getLabelsPerBox() : 0);
        }
    }

    private boolean isGoodCangWarehouse(String deliveryWarehouseId) {
        if (CharSequenceUtil.isBlank(deliveryWarehouseId)) {
            return false;
        }
        OverseasProviderEntity overseasProvider = overseasProviderService.getByWarehouseId(deliveryWarehouseId);
        return Objects.nonNull(overseasProvider) && PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(overseasProvider.getCode());
    }

    private void validatePacking(B2bThirdDeliveryEntity entity,
                                 List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> detailList,
                                 List<B2bCustomerPackingDTO.AddDTO> packingDetailList,
                                 boolean goodCang) {
        String packingType = CharSequenceUtil.blankToDefault(entity.getPackingType(), B2bPackingTypeEnum.WAREHOUSE_SELF.getCode());
        Integer labelsPerBox = entity.getLabelsPerBox() != null ? entity.getLabelsPerBox() : 0;

        if (B2bPackingTypeEnum.WAREHOUSE_SELF.getCode().equals(packingType)) {
            if (!Objects.equals(labelsPerBox, 0)) {
                throw new ServiceException("仓库自主装箱时每箱张贴货件标签数必须为0");
            }
        } else if (B2bPackingTypeEnum.requiresPackingDetail(packingType)) {
            if (!ALLOWED_LABELS_PER_BOX.contains(labelsPerBox) || labelsPerBox == 0) {
                throw new ServiceException("客户指定装箱或已暂存箱发货时每箱张贴货件标签数必填且只能为1、2或4");
            }
        }

        if (!goodCang && CollUtil.isNotEmpty(packingDetailList)) {
            throw new ServiceException("仅谷仓仓库支持装箱明细");
        }

        if (goodCang && B2bPackingTypeEnum.requiresPackingDetail(packingType) && CollUtil.isEmpty(packingDetailList)) {
            throw new ServiceException("装箱明细不能为空");
        }

        if (CollUtil.isEmpty(packingDetailList)) {
            return;
        }

        Set<String> allowedSkuNos = detailList.stream()
                .map(com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO::getSkuNo)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Integer> deliveryQtyBySku = detailList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getSkuNo()))
                .collect(Collectors.groupingBy(com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO::getSkuNo,
                        Collectors.summingInt(e -> e.getDeliveryQty() != null ? e.getDeliveryQty() : 0)));

        List<String> qtyErrors = new ArrayList<>();
        Map<String, Integer> packingQtyBySku = new HashMap<>();

        for (int i = 0; i < packingDetailList.size(); i++) {
            B2bCustomerPackingDTO.AddDTO box = packingDetailList.get(i);
            if (box.getBoxSeq() == null) {
                throw new ServiceException("装箱明细第{}箱序号不能为空", i + 1);
            }
            if (B2bPackingTypeEnum.PRE_STAGED_BOX.getCode().equals(packingType) && CharSequenceUtil.isBlank(box.getBoxMarkNo())) {
                throw new ServiceException("装箱明细序号【{}】箱唛号不能为空", box.getBoxSeq());
            }
            if (CharSequenceUtil.length(box.getBoxMarkNo()) > 50) {
                throw new ServiceException("箱唛号长度不能超过50");
            }
            if (CharSequenceUtil.length(box.getBoxMarkRefNo()) > 50) {
                throw new ServiceException("箱唛参考号长度不能超过50");
            }
            if (CharSequenceUtil.length(box.getLabelingRequirement()) > 200) {
                throw new ServiceException("贴标要求长度不能超过200");
            }
            if (CharSequenceUtil.isNotBlank(box.getLabelSize()) && !B2bPackingLabelSizeEnum.isValid(box.getLabelSize())) {
                throw new ServiceException("标签尺寸不合法");
            }
            if (labelsPerBox > 0 && CollUtil.isEmpty(box.getAttachList())) {
                throw new ServiceException("每箱张贴货件标签数大于0时货件标签必填");
            }
            if (CollUtil.isEmpty(box.getPackingLineList())) {
                throw new ServiceException("装箱明细序号【{}】SKU不能为空", box.getBoxSeq());
            }
            for (B2bCustomerPackingDTO.LineAddDTO row : box.getPackingLineList()) {
                if (CharSequenceUtil.isBlank(row.getSkuNo())) {
                    throw new ServiceException("装箱明细序号【{}】SKU不能为空", box.getBoxSeq());
                }
                if (!allowedSkuNos.contains(row.getSkuNo())) {
                    throw new ServiceException("装箱明细SKU【{}】不在产品明细中", row.getSkuNo());
                }
                if (row.getPackingQty() == null || row.getPackingQty() <= 0) {
                    throw new ServiceException("装箱明细SKU【{}】装箱数量必须为正整数", row.getSkuNo());
                }
                packingQtyBySku.merge(row.getSkuNo(), row.getPackingQty(), Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> entry : packingQtyBySku.entrySet()) {
            int deliveryQty = deliveryQtyBySku.getOrDefault(entry.getKey(), 0);
            if (!Objects.equals(entry.getValue(), deliveryQty)) {
                qtyErrors.add(CharSequenceUtil.format("SKU【{}】装箱数量【{}】不等于发货数量【{}】", entry.getKey(), entry.getValue(), deliveryQty));
            }
        }
        if (CollUtil.isNotEmpty(qtyErrors)) {
            throw new ServiceException(String.join("；", qtyErrors));
        }
    }

    private List<B2bCustomerPackingDTO.AddDTO> enrichPackingDetailList(
            List<com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> detailList,
            List<B2bCustomerPackingDTO.AddDTO> packingDetailList) {
        if (CollUtil.isEmpty(packingDetailList)) {
            return Collections.emptyList();
        }
        Map<String, com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO> skuMap = detailList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getSkuNo()))
                .collect(Collectors.toMap(com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO::getSkuNo, e -> e, (a, b) -> a));
        Map<String, Integer> saleQtyMap = detailList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getSkuNo()))
                .collect(Collectors.groupingBy(com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO::getSkuNo,
                        Collectors.summingInt(e -> e.getSaleQty() != null ? e.getSaleQty() : 0)));
        int sort = 0;
        for (B2bCustomerPackingDTO.AddDTO box : packingDetailList) {
            for (B2bCustomerPackingDTO.LineAddDTO row : box.getPackingLineList()) {
                com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO.AddDTO product = skuMap.get(row.getSkuNo());
                if (product != null) {
                    row.setSkuId(product.getSkuId());
                    row.setProductName(product.getProductName());
                    row.setWarehousePlatformSku(product.getWarehousePlatformSku());
                }
                row.setSaleQty(saleQtyMap.getOrDefault(row.getSkuNo(), 0));
                if (row.getSort() == null) {
                    row.setSort(sort++);
                }
            }
        }
        return packingDetailList;
    }

    private void fillPackingView(B2bThirdDeliveryDTO.ViewDTO viewDTO, B2bThirdDeliveryEntity entity,
                                 List<B2bThirdDeliveryDetailEntity> detailEntityList) {
        viewDTO.setPackingType(CharSequenceUtil.blankToDefault(entity.getPackingType(), B2bPackingTypeEnum.WAREHOUSE_SELF.getCode()));
        viewDTO.setPackingTypeName(B2bPackingTypeEnum.getName(viewDTO.getPackingType()));
        viewDTO.setLabelsPerBox(entity.getLabelsPerBox() != null ? entity.getLabelsPerBox() : 0);
        boolean goodCang = isGoodCangWarehouse(entity.getDeliveryWarehouseId());
        viewDTO.setShowPackingDetail(goodCang);
        if (!goodCang) {
            viewDTO.setPackingDetailList(Collections.emptyList());
            return;
        }
        List<B2bCustomerPackingEntity> packingEntities = b2bCustomerPackingService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isEmpty(packingEntities)) {
            viewDTO.setPackingDetailList(Collections.emptyList());
            return;
        }
        Map<String, Integer> saleQtyBySku = detailEntityList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getSkuNo()))
                .collect(Collectors.groupingBy(B2bThirdDeliveryDetailEntity::getSkuNo,
                        Collectors.summingInt(e -> e.getSaleQty() != null ? e.getSaleQty() : 0)));
        List<String> packingIds = packingEntities.stream().map(B2bCustomerPackingEntity::getId).collect(Collectors.toList());
        List<WmsAttachmentDTO.UpdateDTO> allAttach = wmsAttachmentService.getByBusinessIds(packingIds, ModuleTypeEnum.B2B_CUSTOMER_PACKING_LABEL.getCode());
        Map<String, List<WmsAttachmentDTO.UpdateDTO>> attachMap = allAttach.stream().collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getBusinessId));
        Map<Integer, List<B2bCustomerPackingEntity>> packingGroup = packingEntities.stream()
                .collect(Collectors.groupingBy(B2bCustomerPackingEntity::getBoxSeq, LinkedHashMap::new, Collectors.toList()));
        List<B2bCustomerPackingDTO.ViewDTO> packingViewList = new ArrayList<>();
        for (Map.Entry<Integer, List<B2bCustomerPackingEntity>> entry : packingGroup.entrySet()) {
            List<B2bCustomerPackingEntity> boxEntities = entry.getValue();
            B2bCustomerPackingEntity boxHead = B2bCustomerPackingServiceImpl.getBoxHead(boxEntities, entry.getKey()).orElse(boxEntities.get(0));
            B2bCustomerPackingDTO.ViewDTO boxDTO = new B2bCustomerPackingDTO.ViewDTO();
            boxDTO.setMainId(boxHead.getMainId());
            boxDTO.setBoxSeq(boxHead.getBoxSeq());
            boxDTO.setBoxMarkNo(boxHead.getBoxMarkNo());
            boxDTO.setBoxMarkRefNo(boxHead.getBoxMarkRefNo());
            boxDTO.setLabelSize(boxHead.getLabelSize());
            boxDTO.setLabelingRequirement(boxHead.getLabelingRequirement());
            boxDTO.setAttachList(attachMap.getOrDefault(boxHead.getId(), Collections.emptyList()));
            List<B2bCustomerPackingDTO.LineViewDTO> lineList = boxEntities.stream().map(e -> {
                B2bCustomerPackingDTO.LineViewDTO dto = new B2bCustomerPackingDTO.LineViewDTO();
                dto.setId(e.getId());
                dto.setMainId(e.getMainId());
                dto.setBoxSeq(e.getBoxSeq());
                dto.setSkuId(e.getSkuId());
                dto.setSkuNo(e.getSkuNo());
                dto.setProductName(e.getProductName());
                dto.setSaleQty(saleQtyBySku.getOrDefault(e.getSkuNo(), e.getSaleQty()));
                dto.setPackingQty(e.getPackingQty());
                dto.setWarehousePlatformSku(e.getWarehousePlatformSku());
                dto.setSort(e.getSort());
                return dto;
            }).collect(Collectors.toList());
            boxDTO.setPackingLineList(lineList);
            packingViewList.add(boxDTO);
        }
        viewDTO.setPackingDetailList(packingViewList);
    }
}
