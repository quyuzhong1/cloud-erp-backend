package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.ThirdNoticePushRecordFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.DeliveryDeclareDetailMidFeign;
import com.erp.rpc.tms.feign.TmsDeclareBillFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.wms.convert.FirstMileDeliveryConverter;
import com.erp.server.wms.mapper.FirstMileDeliveryMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FirstMileDeliveryServiceImpl extends SuperServiceImpl<FirstMileDeliveryMapper, FirstMileDeliveryEntity> implements FirstMileDeliveryService {
     @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Lazy
    @Resource
    private MachineInfoService machineInfoService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Lazy
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TransferInfoService transferInfoService;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Lazy
    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;
    @Resource
    private WmsCartonService wmsCartonService;
    @Resource
    private WmsCartonDetailService wmsCartonDetailService;
    @Lazy
    @Resource
    private WmsDeliveryPlanService wmsDeliveryPlanService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private DeliveryDeclareDetailMidFeign deliveryDeclareDetailMidFeign;
    @Resource
    private TmsDeclareBillFeign tmsDeclareBillFeign;
    @Resource
    private com.erp.rpc.tms.feign.CfgSettingFeign tmsCfgSettingFeign;
    @Lazy
    @Resource
    private PackingTaskService packingTaskService;
    @Resource
    private CfgRuleOutService cfgRuleOutService;
    @Resource
    private PickingListsService pickingListsService;
    @Resource
    private PickingDetailService pickingDetailService;
    @Resource
    private CfgRulePickingStagingService cfgRulePickingStagingService;
    @Lazy
    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysPostFeign sysPostFeign;
    @Resource
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;
    @Resource
    private MQProducerService<AutoGenerateBillDTO> firstMileDeclareMqProducerService;
    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;
    @Lazy
    @Resource
    private AwdOutstockService awdOutstockService;
    @Resource
    private AwdOutstockDetailService awdOutstockDetailService;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private ThirdNoticePushRecordFeign thirdNoticePushRecordFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileDeliveryDTO.AddDTO addDTO) {
        normalizeFbtFnSku(addDTO.getDemandType(), addDTO.getDetailList());
        FirstMileDeliveryEntity firstMileDeliveryEntity = new FirstMileDeliveryEntity();
        BeanMapperUtils.copy(addDTO, firstMileDeliveryEntity);
        String idStr = IdWorker.getIdStr();
        firstMileDeliveryEntity.setId(idStr);
        // 数据处理
        handleData(firstMileDeliveryEntity);
        //匹配中转规则
        matchTransferRule(firstMileDeliveryEntity);
        log.info("开始新增发货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHD);
        firstMileDeliveryEntity.setCode(code);
        firstMileDeliveryEntity.setDeclareStatus(WmsDeclareStatusEnum.WAIT);
        boolean save = super.save(firstMileDeliveryEntity);
        if(!save) {
            throw new ServiceException("发货单保存失败");
        }

        //保存附件
        Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
        TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        wmsAttachmentService.batchSave(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), type, idStr);

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货单" , firstMileDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), firstMileDeliveryEntity.getId(), "新增操作");

        //新增详情信息
        firstMileDeliveryDetailService.add(addDTO, firstMileDeliveryEntity.getId());
        //新增装箱任务
//        packingTaskService.addPackingByFirstMileDelivery(firstMileDeliveryEntity);

        // 当前单据提交完成后再检查装箱状态，避免事务未提交时读取不到装箱明细。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.error("自动生成报关明细中间表等待异常：{}", e.getMessage());
                }
                FirstMileDeliveryServiceImpl bean = ApplicationContextUtils.getBean(FirstMileDeliveryServiceImpl.class);
                bean.autoGenerateByPacked(firstMileDeliveryEntity, BillGenerateTimingEnum.AFTER_ADD);
            }
        });
        return new BaseResultDTO.AddDTO(firstMileDeliveryEntity.getId(), code);
    }

    /**
     * 按装箱状态自动生成报关明细中间表
     *
     * @param entity 头程发货单
     * @param billGenerateTimingEnum 单据生成时机
     * @throws ServiceException 自动生成失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public void autoGenerateByPacked( FirstMileDeliveryEntity entity, BillGenerateTimingEnum billGenerateTimingEnum) {
        List<PackingTaskEntity> taskEntityList = packingTaskService.getPackingStatusByFirstMileDelivery(entity);
        // 已装箱才能生成报关明细中间表。
        if (CollectionUtils.isNotEmpty(taskEntityList)) {
            boolean packed = taskEntityList.stream().allMatch(taskEntity -> taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()));
            if(packed){
                try {
                    if(WmsDeclareStatusEnum.WAIT.equals(entity.getDeclareStatus())){
                        registerFirstMileDeclareAutoGenerateTask(entity, billGenerateTimingEnum);
                    }
                }catch (Exception e){
                    log.error("头程发货单{}登记自动生成报关明细任务失败：{}", entity.getCode(), e.getMessage(), e);
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_AUTO_GENERATE_FAILED,
                            "头程发货单", entity.getCode(), e.getMessage());
                }
            }
        }
    }

    /**
     * 提交后发送头程报关自动生成任务，避免审核事务内同步回调 WMS。
     *
     * @param entity 头程发货单
     * @param billGenerateTimingEnum 单据生成时机
     */
    private void registerFirstMileDeclareAutoGenerateTask(FirstMileDeliveryEntity entity, BillGenerateTimingEnum billGenerateTimingEnum) {
        if (Objects.isNull(entity) || Objects.isNull(billGenerateTimingEnum)) {
            return;
        }
        if (!checkFirstMileDeclareAutoGenerateCfg(billGenerateTimingEnum)) {
            return;
        }
        AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
                .id(entity.getId())
                .billGenerateTimingEnum(billGenerateTimingEnum)
                .sourceTypeEnum(SourceTypeEnum.FIRST_MILE_DELIVERY)
                .checkCfg(Boolean.TRUE)
                .build();
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        sendFirstMileDeclareAutoGenerateTask(autoGenerateBillDTO, entity.getCode());
                    } catch (Exception e) {
                        log.error("头程发货单{}提交后发送自动生成报关明细任务失败：{}", entity.getCode(), e.getMessage(), e);
                    }
                }
            });
            return;
        }
    }

    /**
     * 发送头程报关自动生成任务。
     *
     * @param dto 自动生成参数
     * @param code 头程发货单号
     */
    private void sendFirstMileDeclareAutoGenerateTask(AutoGenerateBillDTO dto, String code) {
        SendResult sendResult = firstMileDeclareMqProducerService.syncClassMsg(
                RocketMqTopic.WMS_FIRST_MILE_DECLARE_AUTO_GENERATE_TOPIC,
                RocketMqTagEnum.WMS_FIRST_MILE_DECLARE_AUTO_GENERATE_TAG.getName(),
                dto,
                dto.getId());
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            throw new ServiceException(CharSequenceUtil.format("头程发货单{}发送自动生成报关明细任务失败：{}", code, JSONUtil.toJsonStr(sendResult)));
        }
        log.info("头程发货单{}已发送自动生成报关明细任务", code);
    }

    /**
     * 消费头程报关自动生成任务。
     *
     * @param dto 自动生成参数
     * @return 是否处理成功
     * @throws ServiceException 自动生成失败时抛出
     */
    @Override
    public Boolean consumeDeclareAutoGenerateTask(AutoGenerateBillDTO dto) {
        if (Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getId())) {
            log.warn("头程发货单自动生成报关明细任务参数为空");
            return Boolean.FALSE;
        }
        if (!SourceTypeEnum.FIRST_MILE_DELIVERY.equals(dto.getSourceTypeEnum())) {
            log.warn("头程发货单自动生成报关明细任务来源类型不匹配，id={}, sourceType={}", dto.getId(), dto.getSourceTypeEnum());
            return Boolean.FALSE;
        }
        FirstMileDeliveryEntity entity = super.getById(dto.getId());
        if (Objects.isNull(entity)) {
            log.warn("头程发货单自动生成报关明细任务未找到来源单，id={}", dto.getId());
            return Boolean.TRUE;
        }
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            log.info("头程发货单{}已作废，跳过自动生成报关明细", entity.getCode());
            return Boolean.TRUE;
        }
        if (BillGenerateTimingEnum.AFTER_APPROVE.equals(dto.getBillGenerateTimingEnum())
                && !ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            log.info("头程发货单{}非已审核状态，跳过审核后自动生成报关明细", entity.getCode());
            return Boolean.TRUE;
        }
        if (!WmsDeclareStatusEnum.WAIT.equals(entity.getDeclareStatus())) {
            log.info("头程发货单{}报关状态非待生成，跳过自动生成报关明细", entity.getCode());
            return Boolean.TRUE;
        }
        if (Boolean.TRUE.equals(dto.getCheckCfg())
                && !checkFirstMileDeclareAutoGenerateCfg(dto.getBillGenerateTimingEnum())) {
            log.info("头程发货单{}未开启当前时机自动生成报关明细配置，跳过", entity.getCode());
            return Boolean.TRUE;
        }
        List<PackingTaskEntity> taskEntityList = packingTaskService.getPackingStatusByFirstMileDelivery(entity);
        boolean packed = CollectionUtils.isNotEmpty(taskEntityList)
                && taskEntityList.stream().allMatch(taskEntity -> taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()));
        if (!packed) {
            log.info("头程发货单{}未全部装箱，跳过自动生成报关明细", entity.getCode());
            return Boolean.TRUE;
        }
        Boolean originalValue = UserContext.getIsUserSystem();
        UserContext.setIsUserSystem(Boolean.TRUE);
        try {
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = listBeforePushFmDeclare(
                    new TmsDeclareBillDTO.PushDeclareBeforeParamDTO(Boolean.FALSE, Collections.singletonList(entity.getId())));
            if (CollectionUtils.isEmpty(sourceDetailList)) {
                throw new ServiceException(CharSequenceUtil.format("头程发货单{}未查询到可生成报关明细的来源数据", entity.getCode()));
            }
            Boolean autoGenerateResult = deliveryDeclareDetailMidFeign.autoGenerateMidData(sourceDetailList);
            if (!Boolean.TRUE.equals(autoGenerateResult)) {
                throw new ServiceException(CharSequenceUtil.format("头程发货单{}自动生成报关明细中间表返回失败", entity.getCode()));
            }
            log.info("头程发货单{}自动生成报关明细中间表成功", entity.getCode());
            return Boolean.TRUE;
        } finally {
            UserContext.setIsUserSystem(originalValue);
        }
    }

    /**
     * 检查头程申报自动生成配置
     * <p>
     * 根据指定的单据生成时机枚举，检查系统配置是否允许自动生成头程申报单。
     * 需要满足以下条件：
     * 1. 单据生成时机枚举不为空
     * 2. 自动开单配置存在且未禁用
     * 3. 配置中启用了自动头程申报功能
     * 4. 配置的头程申报生成时机与传入的时机匹配
     * </p>
     *
     * @param billGenerateTimingEnum 单据生成时机枚举，用于匹配配置中的生成时机
     * @return Boolean.TRUE表示满足自动生成条件，Boolean.FALSE表示不满足
     */
    private Boolean checkFirstMileDeclareAutoGenerateCfg(BillGenerateTimingEnum billGenerateTimingEnum) {
        if (Objects.isNull(billGenerateTimingEnum)) {
            return Boolean.FALSE;
        }
        // 获取自动开单配置
        com.erp.model.tms.entity.CfgSettingEntity cfgSettingEntity =
                tmsCfgSettingFeign.getByKey(com.erp.model.tms.enums.CfgSettingEnum.BILL_AUTO_ADD.getCode());
        if (Objects.isNull(cfgSettingEntity) || Boolean.TRUE.equals(cfgSettingEntity.getDisabled())) {
            return Boolean.FALSE;
        }
        // 解析配置数据并验证头程申报自动生成条件
        com.erp.model.tms.dto.CfgSettingValueDTO.BillAutoAddDTO cfg =
                BeanUtil.toBean(cfgSettingEntity.getDataJson(), com.erp.model.tms.dto.CfgSettingValueDTO.BillAutoAddDTO.class);
        return Objects.nonNull(cfg)
                && Boolean.TRUE.equals(cfg.getIsAutoFirstMileDeclare())
                && CharSequenceUtil.equals(cfg.getFirstMileDeclareGenerateTiming(), billGenerateTimingEnum.getCode());
    }

    private void matchTransferRule(FirstMileDeliveryEntity entity) {
        entity.setTransferWarehouseIds(CharSequenceUtil.EMPTY);
        //匹配中转配置
        CfgRuleOutDTO.MatchTransferRuleDTO matchRuleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
        matchRuleDTO.setType(StockOutTransferTypeEnum.FIRST_MILE.getCode());
        matchRuleDTO.setReceiveCountry(entity.getCountryId());
        matchRuleDTO.setDestWarehouse(entity.getDestWarehouseId());
        matchRuleDTO.setFromWarehouse(entity.getDeliveryWarehouseId());
        matchRuleDTO.setSalesOrgId(entity.getInventoryOrgId());
        matchRuleDTO.setDictPlatform("");
        if(StringUtils.isNotBlank(entity.getDestWarehouseId()) || StringUtils.isNotBlank(entity.getDeliveryWarehouseId())){
            List<String> warehouseIds = new ArrayList<>();
            warehouseIds.add(entity.getDestWarehouseId());
            warehouseIds.add(entity.getDeliveryWarehouseId());
            List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(warehouseIds);
            WarehouseEntity fromWarehouse = warehouseEntityList.stream().filter(wh -> wh.getId().equals(entity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
            WarehouseEntity toWarehouse = warehouseEntityList.stream().filter(wh -> wh.getId().equals(entity.getDestWarehouseId())).findFirst().orElse(new WarehouseEntity());
            matchRuleDTO.setFromWarehouseCountry(fromWarehouse.getCountry());
            matchRuleDTO.setFromWarehouseOrg(fromWarehouse.getOrgId());
            matchRuleDTO.setDestWarehouseCountry(toWarehouse.getCountry());
            matchRuleDTO.setDestWarehouseOrg(toWarehouse.getOrgId());
        }


        CfgRuleOutDTO.MatchTransferResultDTO matchTransferResultDTO = cfgRuleOutService.matchTransferRule(matchRuleDTO);
        if (Objects.nonNull(matchTransferResultDTO) && Objects.nonNull(matchTransferResultDTO.getIsTransit()) && matchTransferResultDTO.getIsTransit()){
            if (CollectionUtils.isNotEmpty(matchTransferResultDTO.getTransferWarehouseIdList())){
                entity.setTransferWarehouseIds(String.join(",", matchTransferResultDTO.getTransferWarehouseIdList()));
            }
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileDeliveryDTO.UpdateDTO updateDTO) {
        FirstMileDeliveryEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发货单");
        }
        // 待提交和审核不通过允许修改
        if (!old.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || old.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        normalizeFbtFnSku(updateDTO.getDemandType(), updateDTO.getDetailList());
        FirstMileDeliveryEntity firstMileDeliveryEntity =  BeanMapperUtils.map(FirstMileDeliveryEntity.class, updateDTO);
        if (CollectionUtils.isNotEmpty(updateDTO.getTransferWarehouseIdList())){
            firstMileDeliveryEntity.setTransferWarehouseIds(String.join(",", updateDTO.getTransferWarehouseIdList()));
        }else {
            firstMileDeliveryEntity.setTransferWarehouseIds(CharSequenceUtil.EMPTY);
        }
        // 数据处理
        handleData(firstMileDeliveryEntity);
        log.info("编辑 开始修改发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(firstMileDeliveryEntity);
        if(!save) {
            throw new ServiceException("发货单保存失败");
        }

        //保存附件
        Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
        TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        wmsAttachmentService.batchSaveNotDel(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, updateDTO.getId());

        //修改明细数据
        firstMileDeliveryDetailService.update(updateDTO, firstMileDeliveryEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录发货单日志数据，单号：【{}】", firstMileDeliveryEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileDeliveryEntity.getCode(), "发货单");
        operateLogService.addModuleOperateLogByObj(old, firstMileDeliveryEntity, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), firstMileDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
    }

    private void normalizeFbtFnSku(String demandType, List<? extends FirstMileDeliveryDetailDTO.CommonDTO> detailList) {
        if (!FbaDemandTypeEnum.DEMAND_FBT_WAREHOUSE.getCode().equals(demandType) || CollectionUtils.isEmpty(detailList)) {
            return;
        }
        for (FirstMileDeliveryDetailDTO.CommonDTO detailDTO : detailList) {
            if (detailDTO == null || StrUtil.isNotBlank(detailDTO.getFnSku())) {
                continue;
            }
            detailDTO.setFnSku(StrUtil.blankToDefault(detailDTO.getPlatformSkuNo(), ""));
        }
    }


    @Override
    public PagingVO<FirstMileDeliveryDTO.ListDTO> paging(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FirstMileDeliveryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> countryDropDownByIds(List<String> ids) {
        List<String> idList = Optional.ofNullable(ids).orElse(Collections.emptyList()).stream()
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }

        List<FirstMileDeliveryEntity> deliveryList = this.listByIds(idList);
        if (CollectionUtils.isEmpty(deliveryList) || deliveryList.size() != idList.size()) {
            throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_NOT_FOUND);
        }

        Set<String> countryIdSet = new HashSet<>();
        for (FirstMileDeliveryEntity delivery : deliveryList) {
            if (CharSequenceUtil.isBlank(delivery.getCountryId())) {
                continue;
            }
            countryIdSet.add(delivery.getCountryId());
        }
        if (countryIdSet.size() > 1) {
            throw new ServiceException("所选头程发货单国家不一致");
        }

        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(countryIdSet.iterator().next()));
        if (CollectionUtils.isEmpty(countryList)) {
            throw new ServiceException("国家信息不存在");
        }
        DictCountryEntity country = countryList.get(0);
        return Collections.singletonList(new BaseDropDownDTO.DisabledDTO(country.getId(), country.getNameCn(), country.getDisabled()));
    }

    @Override
    public PagingVO<FirstMileDeliveryDTO.ListFirstMileDTO> pagingFirstMile(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<FirstMileDeliveryDTO.ListFirstMileDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FirstMileDeliveryDTO.ListFirstMileDTO> pageData = this.baseMapper.pagingFirstMile(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillFirstMileList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillFirstMileList(List<FirstMileDeliveryDTO.ListFirstMileDTO> records) {
        if (CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> skuIds = records.stream().map(FirstMileDeliveryDTO.ListFirstMileDTO::getSkuId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)){
            return;
        }
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuVOList)){
            return;
        }
        Map<String, String> skuMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));
//        List<String> deliveryIds = records.stream().map(FirstMileDeliveryDTO.ListFirstMileDTO::getSourceId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
//        List<FirstMileDeliveryDTO.BusinessDTO> businessDTOS = baseMapper.getBusinessCodeByIds(deliveryIds);
        records.forEach(listFirstMileDTO -> {
            if (CharSequenceUtil.isNotBlank(listFirstMileDTO.getSkuId())){
                listFirstMileDTO.setProductName(skuMap.get(listFirstMileDTO.getSkuId()));
            }
//            FirstMileDeliveryDTO.BusinessDTO businessDTO = businessDTOS.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getId(), listFirstMileDTO.getSourceId())).findFirst().orElse(null);
//            if (Objects.nonNull(businessDTO)){
//                listFirstMileDTO.setBusinessCode(businessDTO.getBusinessCode());
//            }
        });
    }

    @Override
    public List<FirstMileDeliveryDTO.TabListDTO> tabList(PermissionsDTO param) {
        FirstMileDeliveryDTO.PagingParamDTO searchParam = new FirstMileDeliveryDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<FirstMileDeliveryDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(FirstMileDeliveryDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new FirstMileDeliveryDTO.TabListDTO(status, 0));
            }
        });
        list.add(new FirstMileDeliveryDTO.TabListDTO("all", list.stream().mapToInt(FirstMileDeliveryDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    @Transactional
    public void exportList(FirstMileDeliveryDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("发货单导出", EXPORT_WMS_FBA_DELIVERY.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id,Boolean isStartProcess) {
        FirstMileDeliveryEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到发货单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改发货单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动发货单流程，id=：【{}】", entity.getId());
        if (isStartProcess) {
            startProcess(entity);
        }

        // 记录操作日志
        log.info("提交 开始记录发货单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "提交操作");

        //发送飞书通知 头程发货单待处理 CfgSettingEnum.FS_FIRSTMILEDELIVERY_WAITHANDLE_NOTICE
        Map<String,String> map = new HashMap<>();
        map.put("code",entity.getCode());
        map.put("createUserId",entity.getCreateUserId());
        map.put("createUserName",entity.getCreateUserName());
        this.sendFirstMileDeliveryMsg(map,CfgSettingEnum.FS_FIRSTMILEDELIVERY_WAITHANDLE_NOTICE);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    public void sendFirstMileDeliveryMsg(Map<String,String> map, CfgSettingEnum type){
        LoginUser loginUser = UserContext.getNonLoginUser();
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.FS_FIRSTMILEDELIVERY_WAITHANDLE_NOTICE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            log.info("未设置飞书要货申请通知配置，无需发送通知");
            return;
        }
        String code = map.get("code");
        String createUserId = map.get("createUserId");
        String createUserName = map.get("createUserName");
        //消息头
        String title = null;
        //消息体
        String msgContent = null;
        switch (type){
            case FS_FIRSTMILEDELIVERY_WAITHANDLE_NOTICE:
                title = String.format(NoticeMsgConstant.FS_FIRSTMILEDELIVERY_SETTING_HEAD);
                msgContent = String.format(NoticeMsgConstant.FS_FIRSTMILEDELIVERY_SETTING_CONTENT, "数大臣", "头程发货单", "头程发货单单据【"+code+"】当前状态审核中，请即时处理",createUserName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                break;
            default:
                return;
        }

        //目前都是创建人，处理人，抄送人员，因此统一处理
        List<String> noticeUserIdList = new ArrayList<>();
        CfgSettingValueDTO.FsRequisitionNoticeDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.FsRequisitionNoticeDTO.class);
        if (CollectionUtils.isNotEmpty(dto.getRoleIdList())) {
            List<String> collect = sysPostFeign.listById(dto.getRoleIdList()).stream().map(SysPostEntity::getPostName).collect(Collectors.toList());
            for (String s : collect) {
                if ("创建人".equals(s)) {
                    noticeUserIdList.add(createUserId);
                }
                if ("处理人".equals(s)) {
                    noticeUserIdList.add(loginUser.getUid());
                }
            }
        }
        //抄送人员
        if (CollectionUtils.isNotEmpty(dto.getUserIdList())) {
            noticeUserIdList.addAll(dto.getUserIdList());
        }
        noticeUserIdList = noticeUserIdList.stream().distinct().collect(Collectors.toList());

        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(noticeUserIdList);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        noticeMsgInfoDTO.setTitle(title);
        noticeMsgInfoDTO.setContent(msgContent);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("消息发送结果失败：{}", com.alibaba.fastjson2.JSONObject.toJSONString(result));
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(FirstMileDeliveryDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO resultAdd = this.add(dto);
        // 提交
        this.submit(resultAdd.getId(),Boolean.TRUE);
        return resultAdd;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(FirstMileDeliveryDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId(),Boolean.TRUE);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        FirstMileDeliveryEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }

        //是否存在下游关联的未作废或未删除的直接调拨单
        Boolean validate = validateExistsTransferInfo(dto.getId());
        if(validate){
            throw new ServiceException(ApiError.WH_EXISTS_TRANSFER_INFO_NOT_CLEAR);
        }
        //要货申请查询
        RequisitionApplicationEntity requisitionApplication = CharSequenceUtil.isNotBlank(entity.getSourceId()) ? requisitionApplicationService.getById(entity.getSourceId()) : null;
        AwdOutstockEntity awdOutstockEntity = CharSequenceUtil.isNotBlank(entity.getSourceId()) ? awdOutstockService.getById(entity.getSourceId()) : null;
        //发货单关联的发后计划类型是三方仓发三方仓，存在组合品校验加工单逻辑时，不校验加工单，可以直接审核 || 来源是awd出库货件
        if (Objects.isNull(awdOutstockEntity)) {
            if (Objects.isNull(requisitionApplication)
                    || !ThirdDeliveryTypeEnum.THIRD_TO_THIRD.getCode().equals(requisitionApplication.getDeliveryType())){
                //已装箱才能审核
                List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(Arrays.asList(entity.getCode(),entity.getSourceCode()));
                if (CollectionUtils.isEmpty(taskEntityList)) {
                    throw new ServiceException("未生成装箱任务，不允许审核");
                }
                PackingTaskEntity taskEntity = taskEntityList.get(0);
                CfgRuleOutDTO.CfgOverweightDetailDTO cfgOverweightDetailDTO = cfgRuleOutService.getCfgOverweightDetailDTOByType(taskEntity.getSourceType());
                if(Objects.nonNull(cfgOverweightDetailDTO) && cfgOverweightDetailDTO.isCheckStatusWhenApprove()){
                    if(!(taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()) && taskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode()))){
                        throw new ServiceException("{已装箱+全部称重}才能审核通过");
                    }
                }
                //包含组合产品的发货单，必须有关联的下推的加工组装单且加工单审核通过，否则提示：发货单【发货单号】包含组合产品，请先下推加工单并且审核通过后重试
                List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
                List<FirstMileDeliveryDetailEntity> isCombinationList = detailEntityList.stream().filter(req -> req.getIsCombination()).collect(Collectors.toList());

                List<String> skuIdList = detailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
                //获取子SKU集合
                List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

                if (CollectionUtils.isNotEmpty(isCombinationList)) {

                //查询多品bom的sku
                List<BomChildrenSkuDTO> sonSkuList = new ArrayList<>();
                for (FirstMileDeliveryDetailEntity detailEntity : isCombinationList) {
                    //查询sku是否存在子SKU
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomChildrenSkuList.stream()
                            .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                                    && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                            ).collect(Collectors.toList());
                    sonSkuList.addAll(bomChildrenSkuDTOS);
                }
                //如果包含了多品bom需要校验，加工单是否审核通过
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(Collections.singletonList(entity.getId()));
                    List<MachineInfoEntity> approveMachineInfoEntityList = machineInfoEntityList.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(approveMachineInfoEntityList)) {
                        throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_CONTAIN_COMBINATION_REQUIRE_MACHINE, entity.getCode());
                    }
                }

                    List<String> skuNos = isCombinationList.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
                    List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);

                //校验组合SKU库存量是否满足调出，否则无法审核通过，提示：SKU【SKU编码】【发货仓】冻结库存不足，无法审核发货单
                for (FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity : isCombinationList) {
                    //及时库存
                    SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())).findFirst().orElse(null);
                    if(Objects.isNull(skuVO)){
                        throw new ServiceException(ApiError.COMMON_SKU_NOT_EXIST_OR_NOT_APPROVE, firstMileDeliveryDetailEntity.getSkuNo());
                    }
                    Integer usableInventoryTotal = inventoryService.getInventoryTotal(entity.getInventoryOrgId() ,entity.getDeliveryWarehouseId(), skuVO.getSkuId(), firstMileDeliveryDetailEntity.getWarehouseLocation(), InventoryStatusEnum.FROZEN.getCode());
                    if (firstMileDeliveryDetailEntity.getDeliveryQty() > usableInventoryTotal) {
                        throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_INVENTORY_INSUFFICIENT, firstMileDeliveryDetailEntity.getSkuNo(), entity.getDeliveryWarehouseName());
                    }
                }
            }
        }
    }

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 生产直接调拨单
     * @Author Luo_WG
     * @Date 2023/12/1 9:20
     * @param entity
     * @param detailEntityList
     * @return java.lang.String
     **/
    private String generateTransferOut(FirstMileDeliveryEntity entity, List<FirstMileDeliveryDetailEntity> detailEntityList) {
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(entity.getDestWarehouseId(), entity.getDeliveryWarehouseId()));

        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream().filter(req -> req.getId().equals(entity.getDestWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());

        //校验目的仓是否为FBA第三方仓
        List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("warehouseType");
        DictBasicDTO.ListDTO listDTO = warehouseTypes.stream().filter(req -> "FBA".equals(req.getValue())).findFirst().orElse(null);
        //如果是FBA第三方仓
        if (listDTO.getId().equals(destWarehouse.getTypeId())) {

            //如果配置为空时默认为“FBA在途仓-xgwj-fba”
            if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Collections.singletonList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WH_CODE_XGWJ_FBA_NOT_EXIST);
                }
                destWarehouse.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                destWarehouse.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.WH_ONWAY_NOT_CONFIGURED);
        }
        RequisitionApplicationEntity requisitionApplication = CharSequenceUtil.isNotBlank(entity.getSourceId()) ? requisitionApplicationService.getById(entity.getSourceId()) : null;
        AwdOutstockEntity awdOutstockEntity = CharSequenceUtil.isNotBlank(entity.getSourceId()) ? awdOutstockService.getById(entity.getSourceId()) : null;

        //查询在途仓
        WarehouseEntity warehouseEntity = warehouseService.getById(destWarehouse.getOnwayWarehouseId());

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();

        if (Objects.nonNull(requisitionApplication) && ThirdDeliveryTypeEnum.THIRD_TO_THIRD.getCode().equals(requisitionApplication.getDeliveryType())){
            addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_TO_THIRD.getCode());
        }else {
            //默认来源类型：头程发货单
            addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        }
        //默认调出日期：当前日期
        addDTO.setBillDate(Objects.nonNull(entity.getDeliveryDate()) ? entity.getDeliveryDate() : LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(warehouseEntity.getOrgId());
        //调出组织
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> req.getId().equals(entity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(deliveryWarehouse.getOrgId());
        //调拨类型
        if (warehouseEntity.getOrgId().equals(deliveryWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setRemark(String.format("发货单【%s】审核通过自动创建", entity.getCode()));


        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Collections.singletonList(entity.getId()));

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity detailEntity : detailEntityList) {
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            //映射产品信息
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(detailEntity.getDeliveryQty());
            detailAddDto.setOutWarehouseId(entity.getDeliveryWarehouseId());
            detailAddDto.setInWarehouseId(warehouseEntity.getId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(detailEntity.getId());
            //如果是备货海外仓
            if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())
            ||FbaDemandTypeEnum.DEMAND_ALIEXPRESS.getCode().equals(entity.getDemandType())) {
                //查询已下推的入库单获取入库单号
                OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntities.stream()
                        .filter(req -> req.getSourceId().equals(entity.getId())
                                && !OverseasInstockStatusEnum.CANCELED.getCode().equals(req.getInstockStatus())
                        ).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(overseasWarehouseInboundEntity)) {
                    detailAddDto.setRemark(overseasWarehouseInboundEntity.getCode());
                }
            } else {
                detailAddDto.setRemark(detailEntity.getFbaShipmentCode());
            }


            if ((Objects.nonNull(requisitionApplication) &&  ThirdDeliveryTypeEnum.THIRD_TO_THIRD.getCode().equals(requisitionApplication.getDeliveryType()))
                    || Objects.nonNull(awdOutstockEntity)){
                //发货单关联的发后计划类型是三方仓发三方仓，调拨时直接调拨可用库存，调拨仓位自动取有库存的仓位
                List<InventoryEntity> availableLocation = getAvailableLocation(detailEntity, entity.getDeliveryWarehouseId());
                if (CollUtil.isEmpty(availableLocation)){
                    throw new ServiceException(CharSequenceUtil.format("仓库【{}】SKU【{}】发货数量【{}】无足够可用库存", deliveryWarehouse.getName(),detailEntity.getSkuNo(),detailEntity.getDeliveryQty()));
                }
                int sum = availableLocation.stream().mapToInt(InventoryEntity::getQty).sum();
                if (sum < detailEntity.getDeliveryQty()){
                    throw new ServiceException(CharSequenceUtil.format("仓库【{}】SKU【{}】发货数量【{}】无足够可用库存【{}】", deliveryWarehouse.getName(),detailEntity.getSkuNo(),detailEntity.getDeliveryQty(),sum));
                }
                Integer deliveryQty = detailEntity.getDeliveryQty();
                for (InventoryEntity inventoryEntity : availableLocation) {
                    if (inventoryEntity.getQty() >= deliveryQty){
                        TransferInfoDetailDTO.AddDTO detailAddDto2 = FirstMileDeliveryConverter.INSTANCE.detailAddDto(detailAddDto);
                        detailAddDto2.setOutWarehouseLocation(inventoryEntity.getWarehouseLocation());
                        detailAddDto2.setQty(deliveryQty);
                        detailAddDtoList.add(detailAddDto2);
                        break;
                    }else {
                        TransferInfoDetailDTO.AddDTO detailAddDto2 = FirstMileDeliveryConverter.INSTANCE.detailAddDto(detailAddDto);
                        detailAddDto2.setOutWarehouseLocation(inventoryEntity.getWarehouseLocation());
                        detailAddDto2.setQty(inventoryEntity.getQty());
                        detailAddDtoList.add(detailAddDto2);
                        deliveryQty = deliveryQty - inventoryEntity.getQty();
                    }
                }
            }else {
                detailAddDto.setOutWarehouseLocation(detailEntity.getWarehouseLocation());
                detailAddDtoList.add(detailAddDto);
            }
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.addAndApprove(addDTO);
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(FirstMileDeliveryEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        entity.setDeliveryDate(dto.getDeliveryDate());
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025/5/27 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(FirstMileDeliveryEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.FIRSTMILEDELIVERY.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> variablesMap = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);

        List<FirstMileDeliveryDetailEntity> detailList = firstMileDeliveryDetailService.lambdaQuery().eq(FirstMileDeliveryDetailEntity::getMainId,entity.getId()).list();
        if (CollUtil.isNotEmpty(detailList)) {
            variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        }
        return variablesMap;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        FirstMileDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //如果是FBA货件来源，反审核修改货件发货状态和发货数量
        if (FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(entity.getDemandType())
                || FbaDemandTypeEnum.DEMAND_FBT_WAREHOUSE.getCode().equals(entity.getDemandType())
                || FbaDemandTypeEnum.DEMAND_AWD_WAREHOUSE.getCode().equals(entity.getDemandType())) {
            fbaShipmentService.deliveryDisApprove(entity);
        }

        //如果是发货计划来源，反审核修改发货状态
        if (SourceTypeEnum.DELIVERY_PLAN.getCode().equals(entity.getSourceType())) {
            WmsDeliveryPlanEntity planEntity = wmsDeliveryPlanService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(planEntity)) {
                //如果存在有一个审核通过的发货单，状态都是已发货
                List<FirstMileDeliveryEntity> firstMileDeliveryEntities = this.listBySourceIds(Collections.singletonList(entity.getSourceId()));
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(deliveryEntities)) {
                    //如果没有审核通过否发货单，修改发货计划单的发货状态为未发货
                    wmsDeliveryPlanService.updateDeliveryStatus(Collections.singletonList(entity.getSourceId()), FbaDeliveryStatusEnum.UN_SHIPPED.getCode());
                }
            }
        }

        //查找发货单下推的分步式调出单自动反审并删除
        List<TransferInfoEntity> transferInfoEntities = transferInfoService.listBySourceIds(Collections.singletonList(id));
        //直接调拨单已审核先反审核
        if (CollectionUtils.isNotEmpty(transferInfoEntities)) {
            transferInfoEntities.forEach(transferInfoEntity -> {
                transferInfoService.disApprove(transferInfoEntity, Boolean.FALSE, Boolean.TRUE);
            });
        }
        //直接调拨单审核中先撤销
        List<String> approveIngTransferOutIds = transferInfoEntities.stream().filter(req -> ApproveStatusEnum.APPROVE_ING.getStatus().equals(req.getApproveStatus())).map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(approveIngTransferOutIds)) {
            transferInfoService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(approveIngTransferOutIds));
        }
        //直接调拨单单删除
        List<String> deletedTransferOutIds = transferInfoEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deletedTransferOutIds)) {
            transferInfoService.delete(deletedTransferOutIds);
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(FirstMileDeliveryEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }

        //已经有签收数量的发货单不允许反审核
        if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(entity.getSourceType())) {
            FbaShipmentEntity shipmentEntity = fbaShipmentService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(shipmentEntity)) {
                List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Collections.singletonList(entity.getSourceId()));
                List<String> detailIds = fbaShipmentDetailEntities.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
                List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(detailIds);
                int sum = fbaShipmentReceiveEntities.stream().mapToInt(req -> req.getReceiveQty()).sum();
                if (sum > 0) {
                    throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_RECEIVE_EXIST_REVERSE_FORBIDDEN);
                }
            }
        }
        //已下推入库单
        OverseasWarehouseInboundEntity inboundEntity = overseasWarehouseInboundService.getBySourceId(entity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
        if (ObjectUtil.isNotEmpty(inboundEntity)) {
            throw new ServiceException(ApiError.WH_OVERSEAS_INBOUND_ALREADY_PUSHED_REVERSE_FORBIDDEN, inboundEntity.getCode());
        }

        //校验下游单据是否生成【包含报关单，物流单】状态为已生成 不可反审核【提示：报关单/物流单[单号]已生成，不可反审核】
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isNotEmpty(tmsFirstMileLogisticEntities)) {
            throw new ServiceException(ApiError.LOGISTICS_ORDER_EXISTS_REVERSE_FORBIDDEN, tmsFirstMileLogisticEntities.get(0).getTransportNo());
        }
        List<TmsDeclareBillEntity> tmsDeclareBillEntities = tmsDeclareBillFeign.listBySourceIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isNotEmpty(tmsDeclareBillEntities)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_EXISTS_REVERSE_FORBIDDEN, tmsDeclareBillEntities.get(0).getCode());
        }

        //是否存在下游关联的未作废或未删除的直接调拨单
        Boolean validate = validateExistsTransferInfo(entity.getId());
        if(validate){
            throw new ServiceException(ApiError.WH_EXISTS_TRANSFER_INFO_NOT_CLEAR);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(FirstMileDeliveryEntity entity, PackingTaskEntity packingTask) {
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_DELETE_STATUS_NOT_ALLOWED);
        }
        if (Objects.nonNull(packingTask)  && !PackingTaskStatusEnum.UNPACKED.getCode().equals(packingTask.getPackingStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"已生成装箱清单且装箱中&已装箱不允许删除");
        }
        List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(Collections.singletonList(entity.getId()));
        if(CollectionUtils.isNotEmpty(machineInfoEntityList)){
            throw new ServiceException("存在关联的加工单{}，头程发货单禁止删除",machineInfoEntityList.stream().map(MachineInfoEntity::getCode).collect(Collectors.toList()));
        }
        //存在下游单据（海外入库单），不能删除
        List<OverseasWarehouseInboundEntity> inboundEntityList = overseasWarehouseInboundService.listBySourceIds(Collections.singletonList(entity.getId()));
        inboundEntityList = inboundEntityList.stream().filter(e -> Objects.nonNull(e) && !Objects.equals(OverseasInstockStatusEnum.CANCELED.getCode(),e.getInstockStatus())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(inboundEntityList)){
            throw new ServiceException("存在下游海外入库单【{}】，禁止删除", inboundEntityList.stream().map(OverseasWarehouseInboundEntity::getCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.joining(",")));
        }

        //校验下游单据是否生成【包含报关单，物流单】状态为已生成 不可反审核【提示：报关单/物流单[单号]已生成，不可反审核】
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isNotEmpty(tmsFirstMileLogisticEntities)) {
            throw new ServiceException(ApiError.LOGISTICS_FIRST_MILE_ORDER_EXISTS_NOT_DEL, tmsFirstMileLogisticEntities.get(0).getTransportNo());
        }
        List<TmsDeclareBillEntity> tmsDeclareBillEntities = tmsDeclareBillFeign.listBySourceIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isNotEmpty(tmsDeclareBillEntities)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_EXISTS_NOT_DEL, tmsDeclareBillEntities.get(0).getCode());
        }
        //删除装箱信息
        if(Objects.nonNull(packingTask)){
            packingTaskService.delete(packingTask);
        }

        String id = entity.getId();
        // 删除fba装箱信息
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listDetailByMainId(id);
        List<String> fbaCodeList = detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        fbaShipmentPackingService.removeByFbaCodeList(fbaCodeList);
        // 删除明细数据
        firstMileDeliveryDetailService.removeByMainIds(Collections.singletonList(id));
        // 删除主单数据
        log.info("删除 开始删除发货单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getCode(), "删除发货单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(FirstMileDeliveryEntity entity, String remark, PackingTaskEntity packingTask) {
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.BILL_VOID_ALLOWED_STATUS_ONLY);
        }
        if (Objects.nonNull(packingTask)  && !PackingTaskStatusEnum.UNPACKED.getCode().equals(packingTask.getPackingStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"已生成装箱清单且装箱中&已装箱不允许删除");
        }
        String id = entity.getId();
        log.info("作废 开始修改发货单状态数据，id：【{}】", id);
        lambdaUpdate().eq(FirstMileDeliveryEntity::getId, id)
            .set(FirstMileDeliveryEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(FirstMileDeliveryEntity::getInvalidRemark, remark)
            .update();
        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        FirstMileDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改发货单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, FirstMileDeliveryEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        //发货时间 默认取审核时传递日期 无值时 获取当天日期
        LocalDate deliveryDate = Objects.nonNull(dto.getDeliveryDate()) ? dto.getDeliveryDate() : LocalDate.now();
        entity.setDeliveryDate(deliveryDate);
        updateForApprove(entity.getId(), approveStatus.getStatus(),deliveryDate);


        //审核通过
        try {
            UserContext.setIsUserSystem(true);
            if (ApproveType.PASS.equals(dto.getType())) {
                //查询发货详情
                List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
                RequisitionApplicationEntity application = requisitionApplicationService.getById(entity.getSourceId());
                if (ObjectUtil.isNotEmpty(application)) {
                    //如果是FBA/FBT/AWD货件来源，审核通过修改货件发货状态和发货数量
                    if (RequisitionApplicationTypeEnum.FBA.getCode().equals(application.getType())
                            || RequisitionApplicationTypeEnum.FBT.getCode().equals(application.getType())
                            || RequisitionApplicationTypeEnum.AWD.getCode().equals(application.getType())) {
                        fbaShipmentService.deliveryStatus(entity);
                    } else {
                        //如果是发货计划来源
                        //审核通过修改发货状态为已发货
                        wmsDeliveryPlanService.updateDeliveryStatus(Collections.singletonList(application.getSourceId()), FbaDeliveryStatusEnum.SHIPPED.getCode());
                    }
                }

                //如果是备货海外仓
                if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())
                        || FbaDemandTypeEnum.DEMAND_ALIEXPRESS.getCode().equals(entity.getDemandType())) {
                    //查询是否下推了入库单
                    OverseasWarehouseInboundEntity inboundEntity = overseasWarehouseInboundService.getBySourceId(entity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
                    if (ObjectUtil.isEmpty(inboundEntity)) {
                        throw new ServiceException(ApiError.WH_OVERSEAS_INBOUND_NOT_FOUND_FOR_APPROVE);
                    }

                    //用目的仓查询是否绑定第三方仓
                    List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByWarehouseIds(Collections.singletonList(entity.getDestWarehouseId()));
                    // 查询发货目的仓平台
                    OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDestWarehouseId());
                    if(Objects.nonNull(providerEntity) && providerEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
                        providerEntity = null;
                    }
                    //有对接海外仓API：调用入库单的提交审核，获取审核结果，审核通过后入库单状态为待签收；审核不通过为异常，操作日志记录失败原因，并显示在备注栏
                    if (CollectionUtils.isNotEmpty(overseasProviderWarehouseEntities) && Objects.nonNull(providerEntity)
                            && !OmsPlatformEnum.JIFENG.getCode().equals(providerEntity.getCode())
                            && !OmsPlatformEnum.WEI_SHI.getCode().equals(providerEntity.getCode())
                            && !OmsPlatformEnum.DA_MAI.getCode().equals(providerEntity.getCode())
                            && !OmsPlatformEnum.OMS_IML.getCode().equals(providerEntity.getCode())
                            && !OmsPlatformEnum.ZHONG_BAO.getCode().equals(providerEntity.getCode())
                            && !OmsPlatformEnum.TONG_YOU.getCode().equals(providerEntity.getCode())) {
                        // 推送第三方发货单审核通过
                        ApiResult<String> resultInfo = overseasWarehouseInboundService.pullThirdOverseasPlatform(providerEntity, inboundEntity, detailEntityList, OverseasVerifyEnum.PASS.getCode());
                        if (200 != resultInfo.getCode()) {
                            log.error("推送第三方仓库【发货单审核】失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                            throw new ServiceException("推送第三方仓库【发货单审核】失败:" + resultInfo.getMsg());
                        }
                        log.info("推送第三方仓库【发货单审核】结果: ={}", JSONUtil.toJsonStr(resultInfo));

                        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据推送第三方仓库发货审核操作 平台返回结果：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单", JSONUtil.toJsonStr(resultInfo));
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "发货单审核");


                    }

                    //入库单状态修改为待签收
                    overseasWarehouseInboundService.updateInstockStatus(Collections.singletonList(inboundEntity.getId()), OverseasInstockStatusEnum.TO_BE_SIGNED.getCode());
                }
                //匹配到规则则进行中转调拨，否则直接生成调拨单
                if (CharSequenceUtil.isNotBlank(entity.getTransferWarehouseIds())){
                    String batchNo = IdUtil.getSnowflake().nextIdStr();
                    List<String> split = StrUtil.split(entity.getTransferWarehouseIds(), ",");
                    //中转循环调拨
                    generateTransferByRule(split,entity, detailEntityList,batchNo);
                }else {
                    generateTransferOut(entity, detailEntityList);
                }

                List<PackingTaskEntity> taskEntityList = packingTaskService.getPackingStatusByFirstMileDelivery(entity);
                //已装箱才能自动生成物流单逻辑
                if (CollectionUtils.isNotEmpty(taskEntityList)) {
                    boolean packed = taskEntityList.stream().allMatch(taskEntity -> taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()));
                    if(packed){
                        //走TMS自动生成物流单逻辑
                        AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
                                .id(entity.getId())
                                .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_APPROVE)
                                .sourceTypeEnum(SourceTypeEnum.FIRST_MILE_DELIVERY)
                                .firstMileDeliveryEntity(entity)
                                .checkCfg(Boolean.TRUE) // 检查配置
                                .build();
                        try {
                            if(FmDeliveryLogisticsStatusEnum.WAIT.equals(entity.getLogisticsStatus())){
                                BatchResultDTO autoGenerateResult;
                                autoGenerateResult = tmsFirstMileLogisticFeign.autoGenerateFirstMileLogistic(autoGenerateBillDTO);
                                if(autoGenerateResult.getSuccess()){
                                    FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                                    updateStatusDTO.setIds(Collections.singletonList(entity.getId()));
                                    updateStatusDTO.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.getCode());
                                    this.updateStatus(updateStatusDTO);
                                }
                            }
                        }catch (Exception e){
                            log.error("头程发货单{} 审核后自动生成物流单失败>>>>>>{}", entity.getCode(), e.getMessage());
                            throw new ServiceException(CharSequenceUtil.format("头程发货单{} 审核后自动生成物流单失败>>>>>>{}", entity.getCode(), e.getMessage()));
                        }

                        if(WmsDeclareStatusEnum.WAIT.equals(entity.getDeclareStatus())){
                            registerFirstMileDeclareAutoGenerateTask(entity, BillGenerateTimingEnum.AFTER_APPROVE);
                        }
                    }
                }
            }
        }finally {
            UserContext.clearIsUserSystem();
        }
        return Boolean.TRUE;
    }



    private void generateTransferByRule(List<String> transferWarehouseIdList, FirstMileDeliveryEntity entity, List<FirstMileDeliveryDetailEntity> detailEntityList,String batchNo) {
        if (CollUtil.isEmpty(transferWarehouseIdList)){
            throw new ServiceException(ApiError.WH_TRANSFER_WAREHOUSE_REQUIRED);
        }
        if (CharSequenceUtil.isBlank(entity.getDeliveryWarehouseId())){
            throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_WAREHOUSE_REQUIRED, entity.getCode());
        }
        //订单调出仓和第一个中转仓一致时从第二个中转仓开始
        boolean firstWarehouseSame = transferWarehouseIdList.get(0).equals(entity.getDeliveryWarehouseId());
        //是否是最后一个调拨到目的仓
        Boolean isLastTransfer = Boolean.FALSE;
        for (int i = 0; i < transferWarehouseIdList.size(); i++) {
            if (firstWarehouseSame && 0 == i){
                continue;//跳过第一个仓库 从第二个开始
            }
            if (i == transferWarehouseIdList.size() -1 && Objects.equals(transferWarehouseIdList.get(i), entity.getDestWarehouseId())){
                isLastTransfer = Boolean.TRUE;
            }
            if (0 == i || firstWarehouseSame){
                addTransferOrder(Boolean.TRUE,entity.getDeliveryWarehouseId(), transferWarehouseIdList.get(i),entity, detailEntityList,batchNo, isLastTransfer, i);
            }else {
                addTransferOrder(Boolean.FALSE, transferWarehouseIdList.get(i - 1), transferWarehouseIdList.get(i),entity, detailEntityList,batchNo, isLastTransfer, i);
            }
        }
        //配置规则中最后一个中转仓 和目的仓一致时 不需要再次进行中转
        Boolean isFirst = Boolean.FALSE;
        String lastWarehouserId = transferWarehouseIdList.get(transferWarehouseIdList.size() -1);
        if (!lastWarehouserId.equals(entity.getDestWarehouseId())){
            addTransferOrder(isFirst, lastWarehouserId, entity.getDestWarehouseId(),entity, detailEntityList,batchNo, Boolean.TRUE, transferWarehouseIdList.size());
        }
    }

    /**
     * 生成中转调拨单
     *
     * @param fromWarehouse
     * @param toWarehouse
     * @param entity
     * @param detailEntityList
     * @param batchNo
     * @param isLastTransfer   最后一次调拨
     * @param index
     */
    private void addTransferOrder(Boolean isFirst, String fromWarehouse, String toWarehouse, FirstMileDeliveryEntity entity,
                                  List<FirstMileDeliveryDetailEntity> detailEntityList, String batchNo, Boolean isLastTransfer, int index) {
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(toWarehouse, fromWarehouse));

        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream().filter(req -> req.getId().equals(toWarehouse)).findFirst().orElse(new WarehouseDTO.UpdateDTO());

        //校验目的仓是否为FBA第三方仓
        List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("warehouseType");
        DictBasicDTO.ListDTO listDTO = warehouseTypes.stream().filter(req -> "FBA".equals(req.getValue())).findFirst().orElse(null);
        //如果是FBA第三方仓
        if (Objects.nonNull(listDTO) && listDTO.getId().equals(destWarehouse.getTypeId())) {

            //如果配置为空时默认为“FBA在途仓-xgwj-fba”
            if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Collections.singletonList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WH_CODE_XGWJ_FBA_NOT_EXIST);
                }
                destWarehouse.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                destWarehouse.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId()) && isLastTransfer) {
            throw new ServiceException(ApiError.WH_ONWAY_NOT_CONFIGURED);
        }

        RequisitionApplicationEntity requisitionApplication = requisitionApplicationService.getById(entity.getSourceId());

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：头程发货单
        if (isLastTransfer){
            //即是第一个，又是最后一个
            if (isFirst && Objects.nonNull(requisitionApplication) && ThirdDeliveryTypeEnum.THIRD_TO_THIRD.getCode().equals(requisitionApplication.getDeliveryType())){
                addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_TO_THIRD.getCode());
            }else {
                addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_FROM_ULANZI.getCode());
            }
        }else if (isFirst && Objects.nonNull(requisitionApplication) && ThirdDeliveryTypeEnum.THIRD_TO_THIRD.getCode().equals(requisitionApplication.getDeliveryType())){
            addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_TRANSFER_TO_THIRD.getCode());
        }else {
            addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_TO_ULANZI.getCode());
        }

        //默认调出日期：默认发货日期
        addDTO.setBillDate(Objects.nonNull(entity.getDeliveryDate()) ? entity.getDeliveryDate() : LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(destWarehouse.getOrgId());
        //调出组织
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> req.getId().equals(fromWarehouse)).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(deliveryWarehouse.getOrgId());
        //调拨类型
        if (destWarehouse.getOrgId().equals(deliveryWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setBatchNo(batchNo);
        addDTO.setIndex(index);
        addDTO.setRemark(String.format("发货单【%s】审核通过自动创建", entity.getCode()));
        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Collections.singletonList(entity.getId()));

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity detailEntity : detailEntityList) {
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            //映射产品信息
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(detailEntity.getDeliveryQty());
            detailAddDto.setOutWarehouseId(fromWarehouse);
            //头程发货单审核通过时，按照中转配置的仓库依次走调拨，最后从最低级的中转仓调拨至在途仓，，调拨时库存状态为冻结
            if (isLastTransfer){
                detailAddDto.setInWarehouseId(destWarehouse.getOnwayWarehouseId());
            }else {
                detailAddDto.setInWarehouseId(toWarehouse);
            }
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(detailEntity.getId());
            //如果是备货海外仓
            if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())
            ||FbaDemandTypeEnum.DEMAND_ALIEXPRESS.getCode().equals(entity.getDemandType())) {
                //查询已下推的入库单获取入库单号
                OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntities.stream()
                        .filter(req -> req.getSourceId().equals(entity.getId())
                                && !OverseasInstockStatusEnum.CANCELED.getCode().equals(req.getInstockStatus())
                        ).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(overseasWarehouseInboundEntity)) {
                    detailAddDto.setRemark(overseasWarehouseInboundEntity.getCode());
                }
            } else {
                detailAddDto.setRemark(detailEntity.getFbaShipmentCode());
            }

            if (isFirst){
                if (ThirdDeliveryTypeEnum.THIRD_TO_THIRD.getCode().equals(requisitionApplication.getDeliveryType())){
                    //发货单关联的发后计划类型是三方仓发三方仓，调拨时直接调拨可用库存，调拨仓位自动取有库存的仓位
                    List<InventoryEntity> availableLocation = getAvailableLocation(detailEntity, fromWarehouse);
                    if (CollUtil.isEmpty(availableLocation)){
                        throw new ServiceException(CharSequenceUtil.format("仓库【{}】SKU【{}】发货数量【{}】无足够可用库存", deliveryWarehouse.getName(),detailEntity.getSkuNo(),detailEntity.getDeliveryQty()));
                    }
                    int sum = availableLocation.stream().mapToInt(InventoryEntity::getQty).sum();
                    if (sum < detailEntity.getDeliveryQty()){
                        throw new ServiceException(CharSequenceUtil.format("仓库【{}】SKU【{}】发货数量【{}】无足够可用库存【{}】", deliveryWarehouse.getName(),detailEntity.getSkuNo(),detailEntity.getDeliveryQty(),sum));
                    }
                    Integer deliveryQty = detailEntity.getDeliveryQty();
                    for (InventoryEntity inventoryEntity : availableLocation) {
                        if (inventoryEntity.getQty() >= deliveryQty){
                            TransferInfoDetailDTO.AddDTO detailAddDto2 = FirstMileDeliveryConverter.INSTANCE.detailAddDto(detailAddDto);
                            detailAddDto2.setOutWarehouseLocation(inventoryEntity.getWarehouseLocation());
                            detailAddDto2.setQty(deliveryQty);
                            detailAddDtoList.add(detailAddDto2);
                            break;
                        }else {
                            TransferInfoDetailDTO.AddDTO detailAddDto2 = FirstMileDeliveryConverter.INSTANCE.detailAddDto(detailAddDto);
                            detailAddDto2.setOutWarehouseLocation(inventoryEntity.getWarehouseLocation());
                            detailAddDto2.setQty(inventoryEntity.getQty());
                            detailAddDtoList.add(detailAddDto2);
                            deliveryQty = deliveryQty - inventoryEntity.getQty();
                        }
                    }
                }else {
                    detailAddDto.setOutWarehouseLocation(detailEntity.getWarehouseLocation());
                    detailAddDtoList.add(detailAddDto);
                }
            }else {
                detailAddDto.setOutWarehouseLocation("");
                detailAddDtoList.add(detailAddDto);
            }
        }
        addDTO.setDetailList(detailAddDtoList);
        transferInfoService.addAndApprove(addDTO);
    }

    private List<InventoryEntity> getAvailableLocation(FirstMileDeliveryDetailEntity deliveryDetail, String fromWarehouseId) {
        return inventoryService.lambdaQuery()
                .eq(InventoryEntity::getSkuId, deliveryDetail.getSkuId())
                .eq(InventoryEntity::getWarehouseId, fromWarehouseId)
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                .orderByDesc(InventoryEntity::getQty)
                .list();
    }

    @Override
    public FirstMileDeliveryDTO.ViewDTO view(String id) {
        //发货单主信息
        FirstMileDeliveryEntity firstMileDeliveryEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到发货单数据"));
        FirstMileDeliveryDTO.ViewDTO data = BeanMapperUtils.map(FirstMileDeliveryDTO.ViewDTO.class, firstMileDeliveryEntity);
        if (CharSequenceUtil.isNotBlank(firstMileDeliveryEntity.getTransferWarehouseIds())){
            List<String> split = StrUtil.split(firstMileDeliveryEntity.getTransferWarehouseIds(), ",");
            data.setTransferWarehouseIdList(split);
        }
        //查询头程物流单
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Collections.singletonList(id));

        //发货单详情
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(id));
        // 数据填充处理
        fillOne(data, tmsFirstMileLogisticEntities, detailEntityList);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(FirstMileDeliveryEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 处理详情字段
     * @Author Luo_WG
     * @Date 2023/11/3 16:05
     * @param data 返回的界面需要的查询列表数据（已映射主表信息）
     * @param tmsFirstMileLogisticEntities 物流信息
     * @param detailEntityList 产品详情信息
     * @return void
     **/
    private void fillOne(FirstMileDeliveryDTO.ViewDTO data, List<LogisticsBillEntity> tmsFirstMileLogisticEntities, List<FirstMileDeliveryDetailEntity> detailEntityList) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

        //获取sku信息
        List<String> skuNoList = detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);

        //根据仓库信息获取核算公司
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(data.getInventoryOrgId()));

        //来源类型名称
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        //设置状态中文名称
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        //备货类型名称
        data.setDemandTypeName(FbaDemandTypeEnum.getName(data.getDemandType()));
        //作废状态名称
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        //库存组织名称
        String orgName = accountingCompanyList.stream().filter(d -> d.getId().equals(data.getInventoryOrgId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        data.setInventoryOrgName(orgName);


        //映射物流信息
        FirstMileDeliveryDTO.ViewLogisticDTO viewLogisticDTO = viewLogistic(data, tmsFirstMileLogisticEntities);
        data.setLogisticsView(viewLogisticDTO);

        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(data.getId()));
        List<String> attachmentUrlList = attachmentList.stream().
                map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).
                collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().
                map(WmsAttachmentDTO.UpdateDTO::getAttachName).
                collect(Collectors.toList());
        data.setAttachUrlList(attachmentUrlList);
        data.setAttachNameList(attachmentNameList);

        //查询已发货的货件信息
        List<String> sourceDetailIdList = detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> entities = firstMileDeliveryDetailService.listBySourceDetailIds(sourceDetailIdList);

        //获取库存sku信息
        List<SkuMappingDTO.ListSkuParamDTO> paramDTOList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity detailEntity : detailEntityList) {
            SkuMappingDTO.ListSkuParamDTO paramDTO = new SkuMappingDTO.ListSkuParamDTO();
            paramDTO.setSkuNo(detailEntity.getSkuNo());
            paramDTO.setWarehouseId(data.getDeliveryWarehouseId());
            paramDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTOList.add(paramDTO);
        }

        List<SkuMappingDTO.ListSkuDTO> listSkuDTOS = skuMappingFeign.listBySkuNoList(paramDTOList);

        //明细信息
        List<FirstMileDeliveryDetailDTO.ViewDTO> detailViews = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity : detailEntityList) {


            FirstMileDeliveryDetailDTO.ViewDTO detailVie = BeanMapperUtils.map(FirstMileDeliveryDetailDTO.ViewDTO.class, firstMileDeliveryDetailEntity);

            //映射产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
            detailVie.setSkuId(skuVO.getSkuId());
            detailVie.setProductName(skuVO.getSkuName());
            detailVie.setImageUrl(skuVO.getSkuImagesUrl());
            //获取已出库数量（排除此单出库数量）
            Integer useDeliveryQty = entities.stream()
                    .filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                            && req.getSourceDetailId().equals(firstMileDeliveryDetailEntity.getSourceDetailId())
                            && !req.getId().equals(firstMileDeliveryDetailEntity.getId()))
                    .mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty)
                    .sum();
            detailVie.setUseDeliveryQty(useDeliveryQty);

            //库存sku
            String stockSku = listSkuDTOS.stream()
                    .filter(req -> req.getProductSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())
                            && data.getDeliveryWarehouseId().equals(req.getWarehouseId()))
                    .distinct()
                    .findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getWarehouseSkuNo())).orElse("");
            detailVie.setStockSku(stockSku);

            detailViews.add(detailVie);
            detailVie.setThirdWarehouseSku(firstMileDeliveryDetailEntity.getPlatformSkuNo());
        }
        data.setDetailList(detailViews);
    }

    /**
     * 组装详情物流信息
     * @param data
     * @param tmsFirstMileLogisticEntities
     */
    private FirstMileDeliveryDTO.ViewLogisticDTO viewLogistic(FirstMileDeliveryDTO.ViewDTO data, List<LogisticsBillEntity> tmsFirstMileLogisticEntities) {
        FirstMileDeliveryDTO.ViewLogisticDTO logisticsViewDTO = new FirstMileDeliveryDTO.ViewLogisticDTO();
        if (CollUtil.isNotEmpty(tmsFirstMileLogisticEntities)) {
            LogisticsBillEntity tmsFirstMileLogisticEntity = tmsFirstMileLogisticEntities.get(0);
            //渠道
            if (CharSequenceUtil.isNotBlank(tmsFirstMileLogisticEntity.getChannelId())) {
                LogisticsChannelDTO.BaseDTO channelInfo = logisticsFeign.getChannelInfoById(tmsFirstMileLogisticEntity.getChannelId());
                if (ObjectUtil.isNotEmpty(channelInfo)) {
                    logisticsViewDTO.setLogisticsChannel(channelInfo.getId());
                    logisticsViewDTO.setLogisticsChannelName(channelInfo.getName());
                }
            }
            //物流方式
            logisticsViewDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(tmsFirstMileLogisticEntity.getShippingMethod()));
            logisticsViewDTO.setLogisticsMethod(tmsFirstMileLogisticEntity.getShippingMethod());
            //发货时间
            logisticsViewDTO.setDeliveryTime(data.getDeliveryDate().atStartOfDay());
            //备注
            logisticsViewDTO.setLogisticsRemark(tmsFirstMileLogisticEntity.getRemark());
            //物流运单号
            logisticsViewDTO.setTrackingNoList(Collections.singletonList(tmsFirstMileLogisticEntity.getTransportNo()));
        }

        //发货地址(取值仓库地址)
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(Arrays.asList(data.getDeliveryWarehouseId(), data.getDestWarehouseId()));
        WarehouseEntity deliveryWarehouse = warehouseEntities.stream().filter(req -> req.getId().equals(data.getDeliveryWarehouseId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(deliveryWarehouse)) {
            logisticsViewDTO.setDeliveryFromAddress(deliveryWarehouse.getAddress());
        }

        //收货地址【FBA取值FBA货件配送地址，第三方仓取值仓库地址】
        if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(data.getSourceType())) {
            FbaShipmentEntity fbaShipmentEntity = fbaShipmentService.getById(data.getSourceId());
            if (ObjectUtil.isNotEmpty(fbaShipmentEntity)) {
                logisticsViewDTO.setDeliveryFromAddress(fbaShipmentEntity.getDeliveryFromAddress());
                logisticsViewDTO.setReceiveToAddress(fbaShipmentEntity.getDeliveryToAddress());
            }
        } else {
            WarehouseEntity destWarehouse = warehouseEntities.stream().filter(req -> req.getId().equals(data.getDestWarehouseId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(destWarehouse)) {
                logisticsViewDTO.setReceiveToAddress(destWarehouse.getAddress());
            }
        }

        return logisticsViewDTO;
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     * @param deliveryDate
     */
    public void updateForApprove(String id, String approveStatus, LocalDate deliveryDate) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(FirstMileDeliveryEntity::getId, id)
            .set(FirstMileDeliveryEntity::getApproveUserId, userInfo.getUid())
            .set(FirstMileDeliveryEntity::getApproveUserName, userInfo.getUserName())
            .set(FirstMileDeliveryEntity::getApproveStatus, approveStatus)
            .set(FirstMileDeliveryEntity::getApproveTime, LocalDateTime.now())
            .set(Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), approveStatus), FirstMileDeliveryEntity::getDeliveryDate, deliveryDate)
            .set(FirstMileDeliveryEntity::getDeliveryStatus, DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode())
            .update(new FirstMileDeliveryEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(FirstMileDeliveryEntity::getId, id)
            .set(FirstMileDeliveryEntity::getApproveUserId, "")
            .set(FirstMileDeliveryEntity::getApproveUserName, "")
            .set(FirstMileDeliveryEntity::getApproveStatus, approveStatus)
            .set(FirstMileDeliveryEntity::getDeliveryStatus, DeliveryStatusEnum.UN_SHIPPED.getCode())
            .set(FirstMileDeliveryEntity::getApproveTime, null)
            .update(new FirstMileDeliveryEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(FirstMileDeliveryEntity::getId, id)
        .set(FirstMileDeliveryEntity::getApproveStatus, approveStatus)
        .update(new FirstMileDeliveryEntity());
    }

    @Override
    public List<FirstMileDeliveryDTO.GenerateMachineView> generateMachineView(List<String> ids) {
        //只有单据为待审核状态允许下推加工单
        List<FirstMileDeliveryEntity> fbaDeliveryEntities = this.listByIds(ids);
        Long aLong = fbaDeliveryEntities.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (aLong > 0) {
            throw new ServiceException(ApiError.WH_SUBCONTRACT_WAIT_SUBMIT_GENERATE_MACHINE_ONLY);
        }

        //只有组合SKU允许下推加工单
        List<FirstMileDeliveryDetailEntity> entities = firstMileDeliveryDetailService.listByMainIds(ids);
        List<FirstMileDeliveryDetailEntity> entityList = entities.stream()
                .filter(req -> Boolean.TRUE.equals(req.getIsCombination()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.WH_SUBCONTRACT_GENERATE_MACHINE_FOR_COMBINATION_ONLY);
        }

        //校验是否已经下推过加工单
        List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(ids);
        List<MachineInfoEntity> collect = machineInfoEntityList.stream().filter(req -> InvalidStatusEnum.NOT_VOIDED.getStatus().equals(req.getInvalidStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.WH_SUBCONTRACT_MACHINE_ALREADY_GENERATED);
        }

        //查询发货单信息
        List<FirstMileDeliveryDTO.GenerateMachineView> viewList = baseMapper.generateMachineView(ids);
        //查询产品sku信息
        List<String> skuNos = viewList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        //查询仓位信息
        List<String> warehouseIds = viewList.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        //查询sku对应的bom版本记录
        List<ProductBomInfoDTO.SkuBomVersion> skuBomVersionList = plmTaskFeign.listBomVersionBySkuNos(skuNos);

        List<String> skuIds = skuVOList.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        //查询历史子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);

        List<FirstMileDeliveryDTO.GenerateMachineView> result = new ArrayList<>();

        for (FirstMileDeliveryDTO.GenerateMachineView view : viewList) {
            //事务类型
            view.setWorkType(WorkTypeEnum.ASSEMBLE.getCode());
            view.setWorkTypeName(WorkTypeEnum.ASSEMBLE.getName());
            //根据sku编号设置产品名称
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(view.getSkuNo())).findFirst().orElse(new SkuVO());
            view.setProductName(skuVO.getSkuName());

            //根据仓位编码设置仓位名称
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(view.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            view.setWarehouseLocationName(warehouseLocationEntity.getName());

            //获取到最新的版本
            ProductBomInfoDTO.SkuBomVersion bomVersionObj = skuBomVersionList.stream().filter(req -> req.getSkuNo().equals(view.getSkuNo())).distinct().findFirst().orElse(null);
            if (ObjectUtil.isEmpty(bomVersionObj)) {
                continue;
            }
            List<BigDecimal> bomVersionList = bomVersionObj.getBomVersionList().stream().map(req -> MathUtil.valueOf(req)).collect(Collectors.toList());
            BigDecimal bomVersion = Collections.max(bomVersionList);
            view.setBomVersion(String.valueOf(bomVersion));
            List<FirstMileDeliveryDTO.SonItem> sonItemList = new ArrayList<>();

            //查询最新版本的sku子件信息
            List<BomChildrenSkuDTO> bomSonItemList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(view.getSkuId())
                            && req.getBomVersion().equals(String.valueOf(bomVersion))
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(bomSonItemList)) {
                continue;
            }

            for (BomChildrenSkuDTO bomDTO : bomSonItemList) {
                FirstMileDeliveryDTO.SonItem sonItem = new FirstMileDeliveryDTO.SonItem();
                sonItem.setId(view.getId());
                //bom用量
                sonItem.setQuantity(bomDTO.getQuantity());
                //子件数量 = 组装数量 * bom用量
                sonItem.setSonQty(view.getAssembleQty() * bomDTO.getQuantity());
                //子件sku
                sonItem.setSonSkuNo(bomDTO.getSkuNo());
                //及时库存
                Integer usableInventoryTotal = inventoryService.getUsableInventoryTotal(view.getWarehouseId(), bomDTO.getSkuId(), view.getWarehouseLocation());
                sonItem.setCurInventoryQty(usableInventoryTotal);
                sonItemList.add(sonItem);
            }
            view.setSonItemList(sonItemList);

            result.add(view);
        }

        return result;
    }

    @Override
    public Boolean fbaDeliveryGenerateMachineSave(List<FirstMileDeliveryDTO.GenerateMachineView> list) {

        List<String> ids = fbaDeliveryGenerateMachine(list);
        if (CollectionUtils.isNotEmpty(ids)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean fbaDeliveryGenerateMachineSubmit(List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        List<String> ids = fbaDeliveryGenerateMachine(list);
        Boolean submit = machineInfoService.submit(ids);
        return submit;
    }

    @Override
    public List<BatchResultDTO> fbaDeliveryGenerateMachineSubmitAndApprove(List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        List<String> ids = fbaDeliveryGenerateMachine(list);
        //提审
        Boolean submit = machineInfoService.submit(ids);
        //审核
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MachineInfoEntity> entityList = machineInfoService.listByIds(ids);
        for (String id : ids) {
            MachineInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                UserContext.setIsUserSystem(true);
                resultDTOS.add(machineInfoService.approve(entity,ApproveType.PASS,"",null));
            }catch (Exception e){
                log.error("加工单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }finally {
                UserContext.clearIsUserSystem();
            }
        }
        return resultDTOS;
    }

    /**
     * 下推加工单
     * @Author Luo_WG
     * @Date 2023/11/7 11:42
     * @param list 下推数据
     * @return java.util.List<java.lang.String>
     **/
    private List<String> fbaDeliveryGenerateMachine(List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        List<String> sonSkuNos = new ArrayList<>();
        for (FirstMileDeliveryDTO.GenerateMachineView generateMachineView : list) {
            List<String> collect = generateMachineView.getSonItemList().stream().map(obj -> obj.getSonSkuNo()).collect(Collectors.toList());
            sonSkuNos.addAll(collect);
        }
        //查询产品sku信息
        List<String> skuNos = list.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        skuNos.addAll(sonSkuNos);
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        //一个发货单多个组合产品，生成一个组装单
        Map<String, List<FirstMileDeliveryDTO.GenerateMachineView>> map = list.stream().collect(Collectors.groupingBy(FirstMileDeliveryDTO.GenerateMachineView::getMainId));
        List<String> ids = new ArrayList<>();
        for (Map.Entry<String, List<FirstMileDeliveryDTO.GenerateMachineView>> entry : map.entrySet()) {
            List<FirstMileDeliveryDTO.GenerateMachineView> value = entry.getValue();
            MachineInfoDTO.AddDTO addDTO = new MachineInfoDTO.AddDTO();
            addDTO.setBillDate(LocalDate.now());
            //事务类型默认组装
            addDTO.setWorkType(WorkTypeEnum.ASSEMBLE.getCode());
            //普通加工单
            addDTO.setType(MachineTypeEnum.ORDINARY.getCode());
            //来源发货单
            addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());

            List<MachineDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (FirstMileDeliveryDTO.GenerateMachineView view : value) {
                addDTO.setSourceId(view.getMainId());
                addDTO.setSourceCode(view.getCode());
                addDTO.setWarehouseId(view.getWarehouseId());
                if(Objects.nonNull(view.getBillDate())){
                    addDTO.setBillDate(view.getBillDate());
                }
                addDTO.setBillDate(view.getBillDate());
                MachineDetailDTO.AddDTO addDetailDTO = new MachineDetailDTO.AddDTO();
                SkuVO skuVO = skuVOList.stream().filter(obj -> obj.getSkuNo().equals(view.getSkuNo())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
                }
                addDetailDTO.setSkuId(skuVO.getSkuId());
                addDetailDTO.setSkuNo(skuVO.getSkuNo());
                addDetailDTO.setWarehouseLocation(view.getWarehouseLocation());
                addDetailDTO.setQty(view.getAssembleQty());
                addDetailDTO.setReferenceVersion(view.getBomVersion());
                addDetailDTO.setRefId(view.getMainId());
                addDetailDTO.setRefCode(view.getCode());
                addDetailDTO.setRefDetailId(view.getId());
                List<MachineSubComponentsDTO.AddDTO> subComponentsList = new ArrayList<>();
                //子件信息
                List<FirstMileDeliveryDTO.SonItem> sonItemList = view.getSonItemList();
                for (FirstMileDeliveryDTO.SonItem sonItem : sonItemList) {
                    MachineSubComponentsDTO.AddDTO addSubComponentsDTO = new MachineSubComponentsDTO.AddDTO();
                    //产品信息
                    SkuVO child = skuVOList.stream().filter(obj -> obj.getSkuNo().equals(sonItem.getSonSkuNo())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(child)) {
                        throw new ServiceException(ApiError.BOM_CHILD_NOT_FOUND);
                    }
                    addSubComponentsDTO.setSkuId(child.getSkuId());
                    addSubComponentsDTO.setSkuNo(child.getSkuNo());
                    addSubComponentsDTO.setWarehouseId(view.getWarehouseId());
                    addSubComponentsDTO.setWarehouseLocation(view.getWarehouseLocation());
                    addSubComponentsDTO.setQty(addDetailDTO.getQty() * sonItem.getQuantity());

                    addSubComponentsDTO.setRemark(String.format("发货单【%s】下推生成加工组装单", view.getCode()));
                    subComponentsList.add(addSubComponentsDTO);
                }
                addDetailDTO.setSubComponentsList(subComponentsList);
                addDetailList.add(addDetailDTO);
            }
            addDTO.setDetailList(addDetailList);
            MachineInfoEntity entity = machineInfoService.add(addDTO);
            ids.add(entity.getId());
        }
        return ids;
    }

    @Override
    public List<FirstMileDeliveryDTO.PrintSonItem> printSonItemDetail(List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        List<String> skuNos = list.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        //查询产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);

        List<String> skuIds = skuVOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        //查询历史子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);

        List<FirstMileDeliveryDTO.PrintSonItem> printSonItemList = new ArrayList<>();
        for (FirstMileDeliveryDTO.GenerateMachineView view : list) {
            FirstMileDeliveryDTO.PrintSonItem printSonItem = new FirstMileDeliveryDTO.PrintSonItem();
            printSonItem.setId(view.getMainId());
            printSonItem.setCode(view.getCode());
            printSonItem.setSkuNo(view.getSkuNo());
            printSonItem.setDeliveryQty(view.getAssembleQty());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(view.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
            printSonItem.setProductName(skuVO.getSkuName());
            //查询最新版本的sku子件信息
            List<BomChildrenSkuDTO> bomSonItemList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuNo().equals(view.getSkuNo()) && req.getBomVersion().equals(view.getBomVersion())).collect(Collectors.toList());
            List<FirstMileDeliveryDTO.PrintSonItemDetail> sonItemList = new ArrayList<>();
            for (BomChildrenSkuDTO bomDTO : bomSonItemList) {
                FirstMileDeliveryDTO.PrintSonItemDetail printSonItemDetail = new FirstMileDeliveryDTO.PrintSonItemDetail();
                printSonItemDetail.setQuantity(bomDTO.getQuantity());
                printSonItemDetail.setSonDeliveryQty(view.getAssembleQty() * bomDTO.getQuantity());
                printSonItemDetail.setSonSkuNo(bomDTO.getSkuNo());
                printSonItemDetail.setSonProductName(bomDTO.getSkuName());
                sonItemList.add(printSonItemDetail);
            }
            printSonItem.setSonItemList(sonItemList);
            printSonItemList.add(printSonItem);
        }
        return printSonItemList;
    }

    @Override
    public List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecordBySourceIds(List<String> ids, String fbaShipmentCode) {
        ids = ids.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids) && CharSequenceUtil.isBlank(fbaShipmentCode)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryRecord(ids,fbaShipmentCode);
    }

    @Override
    public List<FirstMileDeliveryEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileDeliveryEntity::getSourceId, sourceIds).orderByDesc(FirstMileDeliveryEntity::getCreateTime).list();
    }

    @Override
    public List<FirstMileDeliveryEntity> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileDeliveryEntity::getCode, codes).list();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<FirstMileDeliveryDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        //根据单据id查询审核流程
        List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<String> taskIds = list.stream().map(req -> req.getTaskId()).collect(Collectors.toList());
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(ids);
        //查询库存sku
        List<DictCountryEntity> dictCountryEntityList = FeignQuery.list(DictCountryEntity.class);

        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(ids);

        String bomType = BomTypeEnum.COMBINATION.getType();

        Map<String,Integer> qtyMap = new HashMap<>();
        List<String> sourceCodeList = list.stream().map(v->v.getSourceCode()).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(sourceCodeList);
        List<WmsCartonDetailEntity> wmsCartonDetailEntityList = wmsCartonDetailService.listByTaskIds(taskIds);
        //中转仓map
        Map<String, String> warehouseMap =  warehouseService.list().stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        // 属性赋值
        for(FirstMileDeliveryDTO.ListDTO data : list) {
            if (CharSequenceUtil.isNotBlank(data.getPackingStatus())) {
                data.setPackingStatusName(PackingTaskStatusEnum.getName(data.getPackingStatus()));
            }else{
                //回查要货申请关联的装箱
                PackingTaskEntity packingTaskEntity = packingTaskEntityList.stream().filter(v->v.getSourceCode().equals(data.getSourceCode())).findFirst().orElse(new PackingTaskEntity());
                if(CharSequenceUtil.isBlank(packingTaskEntity.getPackingStatus())){
                    data.setPackingStatus(PackingTaskStatusEnum.WAIT.getCode());
                    data.setPackingStatusName(PackingTaskStatusEnum.WAIT.getName());
                }else{
                    data.setPackingStatus(packingTaskEntity.getPackingStatus());
                    data.setPackingStatusName(PackingTaskStatusEnum.getName(packingTaskEntity.getPackingStatus()));
                }
            }
            DictCountryEntity dictCountryEntity = dictCountryEntityList.stream().filter(v->v.getId().equals(data.getCountryId())).findFirst().orElse(null);
            if(Objects.nonNull(dictCountryEntity)){
                data.setCountryName(dictCountryEntity.getNameCn());
            }
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(data.getSkuNo())).findFirst().orElse(new SkuVO());

            //库存sku
            if(FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(data.getDemandType())){
                data.setStockSku(data.getPlatformSkuNo());
            }else{
                data.setStockSku("");
            }

            long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(data.getSkuId())&& bomType.equals(e.getType())).count();

            data.setIsCombination(count > 0);

            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            //备货类型名称
            data.setDemandTypeName(FbaDemandTypeEnum.getName(data.getDemandType()));
            //作废状态名称
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //物流方式名称
            data.setLogisticsMethodName(LogisticsMethodEnum.getName(data.getLogisticsMethod()));
            //物流单状态中文
            data.setLogisticsStatusName(FmDeliveryLogisticsStatusEnum.getName(data.getLogisticsStatus()));
            //报关单状态中文
            data.setDeclareStatusName(WmsDeclareStatusEnum.getName(data.getDeclareStatus()));
            //产品名称
            data.setProductName(skuVO.getSkuName());
            //待审核人
            List<String> curApproveName = processTaskManagementEntities.stream()
                    .filter(req -> req.getBusinessId().equals(data.getId())
                            && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING))
                    .map(ProcessTaskManagementEntity::getCurApproveName)
                    .distinct().collect(Collectors.toList());
            String waitApproveUserName = StringUtils.join(curApproveName, ",");
            data.setWaitApproveUserName(waitApproveUserName);
            //查询已下推的入库单获取入库单号
            OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntities.stream()
                    .filter(req -> req.getSourceId().equals(data.getId())
                            && !OverseasInstockStatusEnum.CANCELED.getCode().equals(req.getInstockStatus())
                    ).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(overseasWarehouseInboundEntity)) {
                data.setOverseasInboundCode(overseasWarehouseInboundEntity.getCode());
            }
            if(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(data.getDemandType())
                    || FbaDemandTypeEnum.DEMAND_FBT_WAREHOUSE.getCode().equals(data.getDemandType())
                    || FbaDemandTypeEnum.DEMAND_AWD_WAREHOUSE.getCode().equals(data.getDemandType())){
                List<WmsCartonDetailEntity> cartonDetailEntityList = wmsCartonDetailEntityList.stream().filter(v->v.getTaskId().equals(data.getTaskId()) && v.getSkuId().equals(data.getSkuId()) && v.getFnSku().equals(data.getFnSku())).collect(Collectors.toList());
                data.setPackingQty(cartonDetailEntityList.stream().mapToInt(v->v.getPackQty()).sum());
            }else{
                List<WmsCartonDetailEntity> cartonDetailEntityList = wmsCartonDetailEntityList.stream().filter(v->v.getTaskId().equals(data.getTaskId()) && v.getSkuId().equals(data.getSkuId()) && v.getFnSku().equals(data.getPlatformSkuNo())).collect(Collectors.toList());
                data.setPackingQty(cartonDetailEntityList.stream().mapToInt(v->v.getPackQty()).sum());
            }

            //如果装箱数量大于发货数量，拆分处理
            if(Objects.nonNull(data.getDeliveryQty()) && Objects.nonNull(data.getPackingQty()) && data.getPackingQty() > data.getDeliveryQty()){
                String key = data.getId() + data.getSkuId()+data.getFnSku();
                if(qtyMap.containsKey(key)){
                    Integer reduceQty = qtyMap.get(key);
                    if(reduceQty > data.getDeliveryQty()){
                        data.setPackingQty(data.getDeliveryQty());
                        qtyMap.put(key,reduceQty - data.getDeliveryQty());
                    }else{
                        data.setPackingQty(reduceQty);
                        qtyMap.put(key,0);
                    }
                }else{
                    qtyMap.put(key,data.getPackingQty() - data.getDeliveryQty());
                    data.setPackingQty(data.getDeliveryQty());
                }
            }
            //中转仓名称
            if (CharSequenceUtil.isNotBlank(data.getTransferWarehouseIds())){
                StringBuilder sb = new StringBuilder();
                List<String> split = CharSequenceUtil.split(data.getTransferWarehouseIds(), ",");
                for (String s : split) {
                    sb.append(warehouseMap.get(s)).append(",");
                }
                data.setTransferWarehouseNames(sb.substring(0, sb.length() - 1));
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(FirstMileDeliveryEntity entity) {

        // 待提交或审核不通过并且未作废允许提交
        if(!entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus()) && !entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(firstMileDeliveryEntity.getSourceType())) {
            FbaShipmentEntity entity = fbaShipmentService.getById(firstMileDeliveryEntity.getSourceId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.FIRST_MILE_FBA_SHIPMENT_NOT_EXIST_BILL);
            }
            //根据店铺id查询店铺信息
            List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(Collections.singletonList(entity.getShopId()));
            //设置店铺的仓位为目的仓
            ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> entity.getShopId().equals(req.getId())).findFirst().orElse(new ShopInfoEntity());
            firstMileDeliveryEntity.setDestWarehouseId(shopInfoEntity.getWarehouseId());
            firstMileDeliveryEntity.setDestWarehouseName(shopInfoEntity.getWarehouseName());
        }
        //根据仓库id查询仓库信息
        List<String> warehouseIds = new ArrayList<>();
        warehouseIds.add(firstMileDeliveryEntity.getDeliveryWarehouseId());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);

        //根据仓库信息获取核算公司
        List<String> orgIds = warehouseEntities.stream().map(req -> req.getOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);

        WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(firstMileDeliveryEntity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
        firstMileDeliveryEntity.setDeliveryWarehouseName(warehouseEntity.getName());

        //设置库存组织
        String orgName = accountingCompanyList.stream().filter(d -> d.getId().equals(warehouseEntity.getOrgId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        firstMileDeliveryEntity.setInventoryOrgId(warehouseEntity.getOrgId());
        firstMileDeliveryEntity.setInventoryOrgName(orgName);
    }

    @Override
    public List<FirstMileDeliveryDTO.SonItem> sonItemDetailByVersion(FirstMileDeliveryDTO.SonItemDetailByVersion dto) {
        FirstMileDeliveryDetailEntity detailEntity = firstMileDeliveryDetailService.getById(dto.getId());
        FirstMileDeliveryEntity entity = this.getById(detailEntity.getMainId());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Collections.singletonList(detailEntity.getSkuNo()));

        //根据单据id查询审核流程
        List<String> skuIds = skuVOList.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<FirstMileDeliveryDTO.SonItem> sonItemList = new ArrayList<>();
        //查询历史子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        //查询最新版本的sku子件信息
        List<BomChildrenSkuDTO> bomSonItemList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuNo().equals(detailEntity.getSkuNo()) && req.getBomVersion().equals(dto.getBomVersion())).collect(Collectors.toList());

        for (BomChildrenSkuDTO bomDTO : bomSonItemList) {
            FirstMileDeliveryDTO.SonItem sonItem = new FirstMileDeliveryDTO.SonItem();
            sonItem.setId(dto.getId());
            //bom用量
            sonItem.setQuantity(bomDTO.getQuantity());
            //子件数量 = 组装数量 * bom用量
            sonItem.setSonQty(detailEntity.getDeliveryQty() * bomDTO.getQuantity());
            //子件sku
            sonItem.setSonSkuNo(bomDTO.getSkuNo());
            //及时库存
            Integer usableInventoryTotal = inventoryService.getUsableInventoryTotal(entity.getDeliveryWarehouseId(), bomDTO.getSkuId(), detailEntity.getWarehouseLocation());
            sonItem.setCurInventoryQty(usableInventoryTotal);
            sonItemList.add(sonItem);
        }
        return sonItemList;
    }

    @Override
    public OverseasWarehouseInboundDTO.ViewDTO getGenerateOverseasWarehouseInboundView(String id) {
        FirstMileDeliveryEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_NOT_FOUND);
        }

        //只有备货类型等于备货海外仓时，才可以下推入库单，否则提示：只有备货海外仓的发货单允许下推入库单
        if (!FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())
        && !FbaDemandTypeEnum.DEMAND_ALIEXPRESS.getCode().equals(entity.getDemandType())) {
            throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_ONLY_FOR_OVERSEAS_WAREHOUSE);
        }

        //发货单待审核的数据可以下推海外仓入库单，其他状态下不可操作，否则提示：只有待审核的数据允许下推海外仓入库单
        if (!ApproveStatusEnum.APPROVE_ING.getCode().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_APPROVE_ONLY_CAN_PUSH_OVERSEAS_INBOUND);
        }

        //一个发货单只能下推一个入库单，否则提示：已下推入库单，不允许重复操作，已取消除外
        OverseasWarehouseInboundEntity inboundEntity = overseasWarehouseInboundService.getBySourceId(entity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
        if (ObjectUtil.isNotEmpty(inboundEntity)) {
            throw new ServiceException(ApiError.WH_INBOUND_EXIST_NOT_REPEAT, inboundEntity.getCode());
        }


        //未装箱不能下推入库单
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(Arrays.asList(entity.getCode(),entity.getSourceCode()));
        PackingTaskEntity packingTaskEntity = CollectionUtils.isEmpty(packingTaskEntityList)?null:packingTaskEntityList.get(0);
        if (Objects.isNull(packingTaskEntity) || !PackingTaskStatusEnum.PACKED.getCode().equals(packingTaskEntity.getPackingStatus())) {
            throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_PACKING_NOT_COMPLETED_CANNOT_GENERATE_INBOUND);
        }

        // 查询关联目的仓
        String destWarehouseId = entity.getDestWarehouseId();
        if (CharSequenceUtil.isBlank(destWarehouseId)) {
            throw new ServiceException("目的仓信息为空");
        }
        // 所属平台:未绑定海外仓为空
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(destWarehouseId);
        if(Objects.nonNull(providerEntity) && providerEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
            providerEntity = null;
        }
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();


        //物流信息
        OverseasWarehouseInboundDTO.ViewDTO viewDTO = FirstMileDeliveryConverter.INSTANCE.fmdToOverseasWarehouseInboundView(entity);
        viewDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        viewDTO.setInstockStatus(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode());
        viewDTO.setInstockStatusName(OverseasInstockStatusEnum.TO_BE_SHIPPED.getName());

        viewDTO.setDemandTypeName(FbaDemandTypeEnum.getName(viewDTO.getDemandType()));
        //查询头程物流单
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(tmsFirstMileLogisticEntities)) {
            LogisticsBillEntity tmsFirstMileLogisticEntity = tmsFirstMileLogisticEntities.get(0);
            viewDTO.setLogisticsMethod(tmsFirstMileLogisticEntity.getShippingMethod());
            viewDTO.setTrackingNo(tmsFirstMileLogisticEntity.getTransportNo());
        }
        List<OverseasProviderWarehouseEntity> entityList = overseasProviderWarehouseService.listByWarehouseIds(Collections.singletonList(destWarehouseId));
        if(CollectionUtils.isNotEmpty(entityList)){
            viewDTO.setOverseasWarehouseId(entityList.get(0).getId());
        }
        // 平台信息
        viewDTO.setDictPlatform(dictPlatform);
        OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(dictPlatform);
        viewDTO.setDictPlatformName(null == platformEnum ? "" : platformEnum.getName());
        //入库类型特殊处理
        if(OmsPlatformEnum.JI_TU.getCode().equals(dictPlatform)) {
            viewDTO.setInstockType(OverseasInstockTypeEnum.SELF_HEADWAY.getCode());
            viewDTO.setInstockTypeName(OverseasInstockTypeEnum.SELF_HEADWAY.getName());
        }
        //明细信息
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(id));
        List<String> skuIdList = firstMileDeliveryDetailEntities.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailViewList = FirstMileDeliveryConverter.INSTANCE.fmdToOverseasWarehouseInboundDetailView(firstMileDeliveryDetailEntities);

        //设置第三方产品名称
        List<String> platformSkuNoList = firstMileDeliveryDetailEntities.stream()
                .map(FirstMileDeliveryDetailEntity::getPlatformSkuNo)
                .distinct()
                .collect(Collectors.toList());
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
        listingInfoParamDTO.setPlatform(dictPlatform);
        List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
        detailViewList.forEach(e->{
            SkuMappingDTO.MappingSkuViewDTO view = mappingSkuViewDTOList.stream().filter(v->v.getPlatformSkuNo().equals(e.getPlatformSkuNo())).findFirst().orElse(new SkuMappingDTO.MappingSkuViewDTO());
            e.setPlatformProductName(view.getPlatformProductName());
        });

        //查询已装箱信息
        List<WmsCartonSpecDTO.PackDateDTO> packDateDTOList = wmsCartonSpecService.listPackDateByPackingTaskId(packingTaskEntity.getId());

        for (OverseasWarehouseInboundDetailDTO.ViewDTO dto : detailViewList) {
            //装箱数量
            int packQty = packDateDTOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).mapToInt(WmsCartonSpecDTO.PackDateDTO::getPackQty).sum();
            if (packQty > dto.getDeliveryQty()) {
                dto.setPackQty(dto.getDeliveryQty());
            } else {
                dto.setPackQty(packQty);
            }

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).findFirst().orElse(new SkuVO());
            dto.setProductName(skuVO.getSkuName());
            dto.setImagesUrl(skuVO.getSkuImagesUrl());
        }

        viewDTO.setDetailList(detailViewList);
        return viewDTO;
    }

    @Override
    public FirstMileDeliveryEntity findBySourceId(String sourceId) {
        return lambdaQuery()
                .eq(FirstMileDeliveryEntity::getSourceId, sourceId)
                .orderByDesc(FirstMileDeliveryEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 修改装箱状态
     * @Author Luo_WG
     * @Date 2023/12/4 16:17
     * @param id 发货单id
     * @param packingStatus 发货状态
     * @return void
     **/
    @Override
    public void updatePackingStatus(String id, String packingStatus){
        lambdaUpdate().set(FirstMileDeliveryEntity::getPackingStatus, packingStatus)
                .eq(FirstMileDeliveryEntity::getId, id)
                .update();
//        if (CharSequenceUtil.equals(packingStatus, PackingTaskStatusEnum.PACKED.getCode())) {
//            FirstMileDeliveryEntity entity = this.getById(id);
//            if (Objects.nonNull(entity)) {
//                autoGenerateByPacked(entity, BillGenerateTimingEnum.AFTER_ADD);
//                if (CharSequenceUtil.equals(entity.getApproveStatus(), com.common.business.enums.ApproveStatusEnum.APPROVE.getCode())) {
//                    autoGenerateByPacked(entity, BillGenerateTimingEnum.AFTER_APPROVE);
//                }
//            }
//        }
    }

    @Override
    public BatchResultDTO generatePackingTask(FirstMileDeliveryEntity entity) {
        packingTaskService.addPackingByFirstMileDelivery(entity);
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    @Override
    public FirstMileDeliveryEntity getByCode(String code) {
        if (CharSequenceUtil.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(FirstMileDeliveryEntity::getCode, code).last("limit 1").one();
    }

    @Override
    public FirstMileDeliveryEntity getBySourceCode(String code) {
        if (CharSequenceUtil.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(FirstMileDeliveryEntity::getSourceCode, code).last("limit 1").one();
    }

    @Override
    public List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecordByFbaCode(String fbaShipmentCode) {
        if (CharSequenceUtil.isBlank(fbaShipmentCode)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryRecord(null,fbaShipmentCode);
    }


    @Override
    public List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailByCodes(List<String> codes,List<String> sourceCodes) {
        if (CollectionUtils.isEmpty(codes) && CollectionUtils.isEmpty(sourceCodes)){
            return Collections.emptyList();
        }
        return baseMapper.listDetailByCodes(codes,sourceCodes);
    }

    @Override
    public List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(FirstMileDeliveryDTO.RequestReceiveDTO dto) {
        //汇总 亚马逊签收报告/第三方仓签收报告签收数量
        List<FirstMileDeliveryDTO.ReceiveDTO> receiveDTOList1 = overseasWarehouseInboundService.countReceiveQtyByParams(dto);
        List<FirstMileDeliveryDTO.ReceiveDTO> receiveDTOList2 =fbaShipmentReceiveService.countReceiveQtyByParams(dto);
        return Stream.concat(receiveDTOList1.stream(),receiveDTOList2.stream()).collect(Collectors.toList());
    }

    @Override
    public List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByIds( List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return baseMapper.getBusinessCodeByIds(ids);
    }


    @Override
    public PagingVO<FirstMileDeliveryDTO.ListDTO> exportFbaDelivery(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<FirstMileDeliveryDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByCodes(List<String> deliveryCodes) {
        if (CollectionUtils.isEmpty(deliveryCodes)){
            return Collections.emptyList();
        }
        return baseMapper.getBusinessCodeByCodes(deliveryCodes);
    }

    @Override
    public void exportBox(PackingTaskDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("箱号对照表清单导出", EXPORT_WMS_FIRST_MILE_PACKING_BOX.getCode(), dto);
    }

    @Override
    public BatchResultDTO updateTransferWarehouse(FirstMileDeliveryEntity entity, List<String> changeIds) {
        //无需校验单据状态，关联的调拨单必须非审核通过、或者无关联的调拨单
        List<TransferInfoEntity> transferInfoEntities = transferInfoService.listBySourceId(entity.getId());
        if (CollectionUtils.isNotEmpty(transferInfoEntities)){
            List<TransferInfoEntity> collect = transferInfoEntities.stream().filter(e -> Objects.nonNull(e) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                List<String> codeList = collect.stream().map(TransferInfoEntity::getCode).distinct().collect(Collectors.toList());
                throw new ServiceException(ApiError.WH_TRANSFER_ALREADY_APPROVED_MODIFY_FORBIDDEN, String.join(",", codeList));
            }
        }
        String transferWarehouseIdList = "";
        if (CollectionUtils.isNotEmpty(changeIds)){
            transferWarehouseIdList = String.join(",", changeIds);
        }
        this.lambdaUpdate().eq(FirstMileDeliveryEntity::getId, entity.getId()).set(FirstMileDeliveryEntity::getTransferWarehouseIds, transferWarehouseIdList).update();
        String msg = "【{}】更新了中转仓配置由【{}】改为【{}】";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, UserContext.getLoginUser().getUserName(),entity.getTransferWarehouseIds(),transferWarehouseIdList), ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "批量修改中转仓配置");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"修改中转仓配置成功");
    }

    @Override
    public void exportPackingDetail(PackingTaskDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("发货单装箱清单导出", EXPORT_WMS_FIRST_MILE_PACKING_TASK_DETAIL.getCode(), dto);
    }

    @Override
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> firstMilePackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto) {

        if (CollectionUtils.isEmpty(dto.getParams().getIds())) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        Page<WmsCartonDetailDTO.ListPackingDetailDTO> page = baseMapper.firstMilePackingTaskDetail(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams(),dto.getParams().getIds(), dto.getParams().getPermissionSql());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        //补充数据
        buildPackingDetailTask(page.getRecords());
        //切换为装箱清单导出
        List<WmsCartonDetailDTO.ListPackingDetailDTO> list = buildPackingDetailExportTask(page.getRecords());
        page.setRecords(list);
        return new PagingVO<>(page);
    }

    @Override
    public WmsCartonSpecDTO.ListPackingDTO listPacking(String id) {
        FirstMileDeliveryEntity firstMileDeliveryEntity = Optional.ofNullable(this.getById(id)).orElseThrow(()->new ServiceException("发货单为空"));
        WmsCartonSpecDTO.ListPackingDTO listPackingDTO = new WmsCartonSpecDTO.ListPackingDTO();
        //三方仓关联装箱任务查，FBA关联货件查
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(Arrays.asList(firstMileDeliveryEntity.getCode(),firstMileDeliveryEntity.getSourceCode()));
        if(CollectionUtils.isEmpty(packingTaskEntityList)){
            return new WmsCartonSpecDTO.ListPackingDTO();
        }
        PackingTaskEntity packingTaskEntity = packingTaskEntityList.get(0);
        PackingTaskDTO.PackedDetailDTO packedDetailDTO = new PackingTaskDTO.PackedDetailDTO();
        if(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(firstMileDeliveryEntity.getDemandType())
                || FbaDemandTypeEnum.DEMAND_FBT_WAREHOUSE.getCode().equals(firstMileDeliveryEntity.getDemandType())
                || FbaDemandTypeEnum.DEMAND_AWD_WAREHOUSE.getCode().equals(firstMileDeliveryEntity.getDemandType())){
            List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listDetailByMainId(id);
            String fbaShipmentCode = detailEntityList.get(0).getFbaShipmentCode();
            List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.listByFbaCodes(Collections.singletonList(fbaShipmentCode));
            List<String> cartonIds = fbaShipmentPackingEntityList.stream().map(v->v.getCartonId()).distinct().collect(Collectors.toList());
            packedDetailDTO.setTaskId(packingTaskEntity.getId());
            packedDetailDTO.setCartonIds(cartonIds);
        }else{
            packedDetailDTO.setTaskId(packingTaskEntity.getId());
        }
        listPackingDTO = packingTaskService.listPacking(packedDetailDTO);
        listPackingDTO.setId(firstMileDeliveryEntity.getId());
        listPackingDTO.setCode(firstMileDeliveryEntity.getCode());
        return listPackingDTO;
    }

    @Override
    public Boolean generateStatusUpdate(FirstMileDeliveryDTO.GenerateStatusUpdateDTO dto) {
        if (CollectionUtils.isEmpty(dto.getIds()) || CollectionUtils.isEmpty(dto.getBillTypes())) {
            return false;
        }
        List<FirstMileDeliveryEntity> deliveryEntities = this.listByIds(dto.getIds());

        List<FirstMileDeliveryEntity> deliveryEntityLogisticsStatusList = deliveryEntities.stream().filter(req -> FmDeliveryLogisticsStatusEnum.FINISH.getCode().equals(req.getLogisticsStatus().getCode())).collect(Collectors.toList());


        for (String billType : dto.getBillTypes()) {
            if (FmDeliveryBillTypeEnum.DECLARE.getCode().equals(billType)) {
                List<FirstMileDeliveryEntity> deliveryEntityList = deliveryEntities.stream().filter(req -> WmsDeclareStatusEnum.FINISH.getCode().equals(req.getDeclareStatus().getCode())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(deliveryEntityList)) {
                    throw new ServiceException(ApiError.BILL_DECLARE_STATUS_GENERATED_NOT_CHANGE_TO_NO_DECLARE, deliveryEntityList.get(0).getCode());
                }
                lambdaUpdate()
                        .set(FmDeliveryBillTypeEnum.DECLARE.getCode().equals(billType), FirstMileDeliveryEntity::getDeclareStatus, WmsDeclareStatusEnum.NONE.getCode())
                        .in(FirstMileDeliveryEntity::getId, dto.getIds())
                        .update();
            }

            if (FmDeliveryBillTypeEnum.LOGISTICS.getCode().equals(billType)) {
                if (CollUtil.isNotEmpty(deliveryEntityLogisticsStatusList)) {
                    throw new ServiceException(ApiError.BILL_LOGISTICS_STATUS_GENERATED_NOT_CHANGE_TO_NO_LOGISTICS, deliveryEntityLogisticsStatusList.get(0).getCode());
                }
                lambdaUpdate()
                        .set(FmDeliveryBillTypeEnum.LOGISTICS.getCode().equals(billType), FirstMileDeliveryEntity::getLogisticsStatus, FmDeliveryLogisticsStatusEnum.NONE.getCode())
                        .in(FirstMileDeliveryEntity::getId, dto.getIds())
                        .update();
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(FirstMileDeliveryDTO.GenerateLogisticReqDTO dto) {
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> result = baseMapper.getGenerateLogisticDTO(dto);
        if(CollectionUtils.isEmpty(result)){
            return new ArrayList<>();
        }
        List<String> outStockIds = result.stream().map(FirstMileDeliveryDTO.GenerateLogisticDTO::getOutstockId).collect(Collectors.toList());
        //会存在已要货申请id下推装箱任务
        List<String> sourceIds = result.stream().map(FirstMileDeliveryDTO.GenerateLogisticDTO::getSourceId).collect(Collectors.toList());
        List<String> ids = Stream.concat(outStockIds.stream(), sourceIds.stream()).distinct().collect(Collectors.toList());
        //FBA来源的要货申请可能有多个发货单，需要根据货件中的装箱id做过滤
        List<FirstMileDeliveryDetailEntity> allDetailEntityList = firstMileDeliveryDetailService.listByMainIds(ids);
        List<String> allFbaShipmentCodes = allDetailEntityList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<FbaShipmentPackingEntity> allFbaShipmentPackingEntityList = fbaShipmentPackingService.listByFbaCodes(allFbaShipmentCodes);
        //处理成map key:发货单id，value:装箱ids
        Map<String,List<String>> packingTaskMap = new HashMap<>();
        for (String id : ids) {
            List<FirstMileDeliveryDetailEntity> detailEntityList = allDetailEntityList.stream().filter(entity -> entity.getMainId().equals(id)).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailEntityList)){
                continue;
            }
            List<String> fbaShipmentCodes = allDetailEntityList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if(CollectionUtils.isEmpty(fbaShipmentCodes)){
                continue;
            }
            List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = allFbaShipmentPackingEntityList.stream().filter(entity -> fbaShipmentCodes.contains(entity.getFbaShipmenCode())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fbaShipmentPackingEntityList)){
                continue;
            }
            List<String> cartonIds = fbaShipmentPackingEntityList.stream().map(FbaShipmentPackingEntity::getCartonId).distinct().collect(Collectors.toList());
            packingTaskMap.put(id,cartonIds);
        }

        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailList = baseMapper.listPackingDetail(ids);
        Map<String,List<WmsCartonDetailDTO.ListPackingDetailDTO>> packingDetailMap = packingDetailList.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId));
        //国家名称填充
        List<DictCountryDTO.ListDTO> listDTOS = sysUserFeign.countryList();
        //设置箱子明细信息
        result.forEach(v->{
            List<WmsCartonDetailDTO.ListPackingDetailDTO> list = packingDetailMap.get(v.getOutstockId());
            if (CollectionUtils.isEmpty(list) && CharSequenceUtil.isNotBlank(v.getSourceId())){
                list = packingDetailMap.get(v.getSourceId());
            }
            //如果有装箱信息，过滤出对应的装箱信息
            if(CollectionUtils.isNotEmpty(list) && packingTaskMap.containsKey(v.getOutstockId())){
                List<String> cartonIds = packingTaskMap.get(v.getOutstockId());
                list = list.stream().filter(e -> cartonIds.contains(e.getBoxId())).collect(Collectors.toList());
            }
            v.setPackingDTOList(list);
            if (CharSequenceUtil.isNotBlank(v.getToCountry())){
                DictCountryDTO.ListDTO listDTO = listDTOS.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getId(), v.getToCountry())).findFirst().orElse(null);
                if (Objects.nonNull(listDTO)){
                    v.setToCountryName(listDTO.getNameCn());
                }
            }
        });
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateStatus(FirstMileDeliveryDTO.UpdateStatusDTO dto) {
        if(CharSequenceUtil.isBlank(dto.getDeclareStatus()) && CharSequenceUtil.isBlank(dto.getLogisticsStatus())){
            return false;
        }
        Boolean updateResult = this.lambdaUpdate()
                .in(FirstMileDeliveryEntity :: getId,dto.getIds())
                .set(CharSequenceUtil.isNotBlank(dto.getLogisticsStatus()),FirstMileDeliveryEntity::getLogisticsStatus, dto.getLogisticsStatus())
                .set(CharSequenceUtil.isNotBlank(dto.getDeclareStatus()),FirstMileDeliveryEntity::getDeclareStatus,dto.getDeclareStatus())
                .update();
        return updateResult;
    }

    @Override
    public List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(FirstMileDeliveryDTO.StatisticsReq dto) {
        return baseMapper.logisticStatistics(dto);
    }

    @Override
    public List<FirstMileDeliveryEntity> advanceQuery(AdvanceQueryContainer advanceQueryContainer) {
        return baseMapper.advanceQuery(advanceQueryContainer);
    }

    @Override
    public List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeclare(TmsDeclareBillDTO.QuerySourceDTO dto) {
        List<TmsDeclareBillDTO.DeliveryDTO> result = baseMapper.getGenerateDeclare(dto);
        if(CollectionUtils.isEmpty(result)){
            return new ArrayList<>();
        }
        List<String> sourceIds = result.stream().map(TmsDeclareBillDTO.DeliveryDTO::getSourceId).collect(Collectors.toList());

        List<FirstMileDeliveryDetailEntity> allDetailEntityList = firstMileDeliveryDetailService.listByMainIds(sourceIds);
        //查询物流产品信息
        List<String> skuIds = allDetailEntityList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailDTO.ProductLogisticDTO> allProductLogisticDTOList = plmTaskFeign.listProductLogisticsByIds(skuIds);
        //装箱信息
        List<String> requisitionIds = result.stream().map(TmsDeclareBillDTO.DeliveryDTO::getRequisitionId).collect(Collectors.toList());
        List<String> ids = result.stream().map(TmsDeclareBillDTO.DeliveryDTO::getSourceId).collect(Collectors.toList());
        ids.addAll(requisitionIds);
        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailList = baseMapper.listPackingDetail(ids);
        Map<String,List<WmsCartonDetailDTO.ListPackingDetailDTO>> packingDetailMap = packingDetailList.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId));

        for (TmsDeclareBillDTO.DeliveryDTO deliveryDTO : result) {
            List<FirstMileDeliveryDetailEntity> detailEntityList = allDetailEntityList.stream().filter(entity -> entity.getMainId().equals(deliveryDTO.getSourceId())).collect(Collectors.toList());
            //处理产品信息
            if(CollectionUtils.isNotEmpty(detailEntityList)){
                //转成MAP，相同sku数量相加
                Map<String,FirstMileDeliveryDetailEntity> detailEntityMap = detailEntityList.stream().collect(Collectors.toMap(FirstMileDeliveryDetailEntity::getSkuId,
                        Function.identity(),(o1, o2)->{
                            FirstMileDeliveryDetailEntity mergeDetail = new FirstMileDeliveryDetailEntity();
                            mergeDetail.setSkuId(o1.getSkuId());
                            mergeDetail.setPlanQty(o1.getPlanQty()+o2.getPlanQty());
                            return mergeDetail;
                        }));
                List<TmsDeclareBillDTO.ProductDetail> productDetailList = new ArrayList<>();
                List<ProductDetailDTO.ProductLogisticDTO> productLogisticDTOList = allProductLogisticDTOList.stream().filter(v->detailEntityMap.containsKey(v.getSkuId())).collect(Collectors.toList());
                for (ProductDetailDTO.ProductLogisticDTO productLogisticDTO : productLogisticDTOList) {
                    FirstMileDeliveryDetailEntity detailEntity = detailEntityMap.get(productLogisticDTO.getSkuId());
                    if(productLogisticDTO.getCombinationDeclareType().equals(CombinationDeclareTypeEnums.SPLIT.getCode()) && productLogisticDTO.getIsCombination()){
                        //拆分申报的组合品，拆成子SKU
                        for (ProductDetailDTO.ProductLogisticDTO logisticDTO : productLogisticDTO.getChildList()) {
                            TmsDeclareBillDTO.ProductDetail productDetail = BeanUtil.copyProperties(logisticDTO,TmsDeclareBillDTO.ProductDetail.class);
                            productDetail.setQty(detailEntity.getPlanQty() * logisticDTO.getChildQty());
                            productDetail.setToCountry(deliveryDTO.getCountry());
                            productDetail.setToCountryName(deliveryDTO.getCountryName());
                            productDetailList.add(productDetail);
                        }
                    }else{
                        TmsDeclareBillDTO.ProductDetail productDetail = BeanUtil.copyProperties(productLogisticDTO,TmsDeclareBillDTO.ProductDetail.class);
                        productDetail.setQty(detailEntity.getPlanQty());
                        productDetail.setToCountry(deliveryDTO.getCountry());
                        productDetail.setToCountryName(deliveryDTO.getCountryName());
                        productDetailList.add(productDetail);
                    }
                }
                deliveryDTO.setNetWeight(productDetailList.stream().filter(v->Objects.nonNull(v.getNetWeight())).map(v->v.getNetWeight().multiply(new BigDecimal(v.getQty())).divide(new BigDecimal(1000),4, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add));
                deliveryDTO.setProductDetailList(productDetailList);
            }

            //设置装箱信息
            List<WmsCartonDetailDTO.ListPackingDetailDTO> list = packingDetailMap.getOrDefault(deliveryDTO.getSourceId(),new ArrayList<>());
            if(CollectionUtils.isEmpty(list)){
                list = packingDetailMap.getOrDefault(deliveryDTO.getRequisitionId(),new ArrayList<>());
            }
            if(CollectionUtils.isNotEmpty(list)){
                List<TmsDeclareBillDTO.PackingDTO> packingDTOList = BeanUtil.copyToList(list,TmsDeclareBillDTO.PackingDTO.class);
                packingDTOList.forEach(t->t.setSourceCode(deliveryDTO.getSourceCode()));
                deliveryDTO.setPackingDTOList(packingDTOList);
            }
            deliveryDTO.setBoxQty(list.size());
            deliveryDTO.setGrossWeight(list.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return result;
    }

    /**
     * 查询用于报关中间表生成的装箱明细
     *
     * @param ids 头程发货单id集合
     * @return 装箱明细集合
     * @throws RuntimeException 查询异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public List<WmsCartonDetailDTO.ListPackingDetailDTO> listDeclarePackingDetail(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeclarePackingDetail(ids);
    }

    @Override
    public int countNotVoided(String id) {
        return count(Wrappers.<FirstMileDeliveryEntity>lambdaQuery()
                .eq(FirstMileDeliveryEntity::getSourceId, id)
                .eq(FirstMileDeliveryEntity::getInvalidStatus, false)
        );
    }

    /**
     * 校验是否存在关联的未删除或未作废的直接调拨单
     * @param deliveryId 发货单ID
     */
    private Boolean validateExistsTransferInfo(String deliveryId) {
        List<TransferInfoEntity> list = transferInfoService.list(new LambdaQueryWrapper<TransferInfoEntity>()
                .eq(TransferInfoEntity::getSourceId, deliveryId)
                .eq(TransferInfoEntity::getInvalidStatus, false));
        return !list.isEmpty();
    }

    /**
     * 填充装箱任务信息
     * @param listPackingDetailDTOS
     */
    private void buildPackingDetailTask(List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS) {
        listPackingDetailDTOS.forEach(listPackingDetailDTO -> {
            listPackingDetailDTO.setPackingStatusName(PackingTaskStatusEnum.getName(listPackingDetailDTO.getPackingStatus()));
            listPackingDetailDTO.setWeightingStatusName(PackingWeightStatusEnum.getName(listPackingDetailDTO.getWeightingStatus()));
            listPackingDetailDTO.setMeasureSourceName(MeasureSourceEnum.getName(listPackingDetailDTO.getMeasureSource()));
            listPackingDetailDTO.setSourceTypeName(PickingSourceTypeEnum.getName(listPackingDetailDTO.getSourceType()));
        });
    }

    private List<WmsCartonDetailDTO.ListPackingDetailDTO> buildPackingDetailExportTask(List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS) {
        List<String> taskIds = listPackingDetailDTOS.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getTaskId).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> taskEntityList = packingTaskService.listByIds(taskIds);
        Map<String, PackingTaskEntity> taskMap = taskEntityList.stream().collect(Collectors.toMap(PackingTaskEntity::getId, Function.identity()));
        //FBA货件新数据过滤掉没绑定的箱
        List<String> fbaCodes = listPackingDetailDTOS.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getBusinessCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(fbaCodes)){
            List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.listByFbaCodes(fbaCodes);
            listPackingDetailDTOS = listPackingDetailDTOS.stream().filter(v->{
                List<FbaShipmentPackingEntity> fbaShipmentPackingEntity = fbaShipmentPackingEntityList.stream().filter(obj->obj.getFbaShipmenCode().equals(v.getBusinessCode())).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(fbaShipmentPackingEntity)){
                    return true;
                }
                List<String> cartonIds = fbaShipmentPackingEntity.stream().map(FbaShipmentPackingEntity::getCartonId).collect(Collectors.toList());
                return cartonIds.contains(v.getId());
            }).collect(Collectors.toList());
        }
        //根据id汇总统计装箱总数量
        Map<String, Integer> boxQtyMap = listPackingDetailDTOS.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId, Collectors.summingInt(WmsCartonDetailDTO.ListPackingDetailDTO::getPackQty)));
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = packingTaskService.selectPackingStatusByIds(taskIds, null);
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        Map<String,Integer> distinctMap = new HashMap<>();
        listPackingDetailDTOS.forEach(pagingViewDTO -> {
            PackingTaskEntity packingTaskEntity = taskMap.get(pagingViewDTO.getTaskId());
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getTaskId());
            Integer totalQty = boxQtyMap.get(pagingViewDTO.getId());
            pagingViewDTO.setTotalQty(totalQty);
            pagingViewDTO.setTaskCode(packingTaskEntity.getCode());
            pagingViewDTO.setSourceCode(packingTaskEntity.getSourceCode());
            pagingViewDTO.setSourceType(packingTaskEntity.getSourceType());
            pagingViewDTO.setSourceTypeName(PickingSourceTypeEnum.getName(packingTaskEntity.getSourceType()));
            if (Objects.nonNull(statusDTO)){
                String packingStatus = CharSequenceUtil.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingTotalStatus(packingStatus);
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = CharSequenceUtil.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingTotalStatus(weightingStatus);
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(CharSequenceUtil.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
                pagingViewDTO.setPackageWeightStr(pagingViewDTO.getPackageWeight().toPlainString());
            }else {
                pagingViewDTO.setPackingTotalStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingTotalStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.UNWEIGHED.getName());
                pagingViewDTO.setPackageWeight(BigDecimal.ZERO);
                pagingViewDTO.setPackageWeightStr("0");
            }
            if(distinctMap.containsKey(pagingViewDTO.getId())){
                //同一个箱子以下字段不重复显示
                pagingViewDTO.setDeliveryCode("");
                pagingViewDTO.setBusinessCode("");
                pagingViewDTO.setTaskCode("");
                pagingViewDTO.setSourceCode("");
                pagingViewDTO.setSourceTypeName("");
                pagingViewDTO.setFbaBoxNo("");
                pagingViewDTO.setBoxNo(null);
                pagingViewDTO.setPackingTotalStatusName("");
                pagingViewDTO.setTotalQty(null);
                pagingViewDTO.setLength(null);
                pagingViewDTO.setWidth(null);
                pagingViewDTO.setHeight(null);
                pagingViewDTO.setPackageWeight(null);
                pagingViewDTO.setPackageWeightStr("");
                pagingViewDTO.setWeightingStatusName("");
                pagingViewDTO.setPackingUserName("");
                pagingViewDTO.setPackingStatusName("");
                pagingViewDTO.setMeasureSourceName("");
            }else {
                distinctMap.put(pagingViewDTO.getId(),1);
            }
        });
        return listPackingDetailDTOS;
    }

    @Override
    public List<FirstMileDeliveryDTO.BusinessDTO> getDeliveryCodeByBusinessCodes(List<String> businessCodes) {
        if (CollectionUtils.isEmpty(businessCodes)){
            return Collections.emptyList();
        }
        return baseMapper.getDeliveryCodeByBusinessCodes(businessCodes);
    }

    @Override
    public List<FbaTransitCalculateReportDTO.DeliveryDTO> listDeliveryByReportMonth(String approveStatus, String sourceType, LocalDate reportMonth, String shipmentCode, String asin, String msku) {
        if (CharSequenceUtil.isAllBlank(approveStatus, sourceType) || Objects.isNull(reportMonth)){
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryByReportMonth(approveStatus, sourceType, reportMonth,shipmentCode,asin,msku);
    }

    @Override
    public List<FirstMileDeliveryDTO.GenerateLogisticDTO> listGenerateLogisticDTO(List<String> deliveryCodes) {
        if(CollUtil.isEmpty(deliveryCodes)){
            return Collections.emptyList();
        }
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> resultList = new ArrayList<>();
        for (String deliveryCode : deliveryCodes) {
            FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
            reqDto.setDeliveryCodeList(Collections.singletonList(deliveryCode));
            List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = getGenerateLogisticDTO(reqDto);
            resultList.addAll(generateLogisticDTO);
        }
        return resultList;
    }

    @Override
    public List<OverseasProviderWarehouseDTO.ProviderDTO> listOverseasProvider(List<String> deliveryIds) {
        if (CollUtil.isEmpty(deliveryIds)){
            return Collections.emptyList();
        }
        return baseMapper.listOverseasProvider(deliveryIds);
    }


    @Override
    public BatchResultDTO retryOutstock(String id) {
        FirstMileDeliveryEntity entity = super.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"发货单不存在");
        }
        if (validateExistsTransferInfo(entity.getId())) {
            throw new ServiceException(ApiError.SO_TRANSFER_NOT_RETRY_OUTSTOCK);
        }
        //查询发货详情
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发货单明细不存在");
        }
        //匹配到规则则进行中转调拨，否则直接生成调拨单
        if (CharSequenceUtil.isNotBlank(entity.getTransferWarehouseIds())){
            String batchNo = identifierGenerator.nextId(new TransferInfoEntity()).toString();
            List<String> split = CharSequenceUtil.split(entity.getTransferWarehouseIds(), ",");
            //中转循环调拨
            generateTransferByRule(split,entity, detailEntityList,batchNo);
        }else {
            generateTransferOut(entity, detailEntityList);
        }
        //日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】重新出库", UserContext.getLoginUser().getUserName()), ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "重新出库");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.GENERATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateFirstMileDeliveryByAwdOutStock(AwdOutstockDTO.GenerateDeliveryDTO dto) {
        FirstMileDeliveryEntity firstMileDeliveryEntity = this.lambdaQuery()
                .eq(FirstMileDeliveryEntity::getSourceId,dto.getId())
                .eq(FirstMileDeliveryEntity::getInvalidStatus,InvalidStatusEnum.NOT_VOIDED.getStatus())
                .one();
        if (Objects.nonNull(firstMileDeliveryEntity)) {
            throw new ServiceException(ApiError.BILL_ALREADY_EXIST,"头程发货单【" + dto.getCode() + "】");
        }

        AwdOutstockEntity awdOutstockEntity = awdOutstockService.getById(dto.getId());
        if (Objects.isNull(awdOutstockEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"AWD出库");
        }

        List<AwdOutstockDetailEntity> awdOutstockDetailEntityList = awdOutstockDetailService.lambdaQuery()
                .eq(AwdOutstockDetailEntity::getMainId, dto.getId())
                .list();

        if (awdOutstockDetailEntityList.isEmpty()) {
            throw new ServiceException(ApiError.BILL_DETAIL_NOT_FOUND,"AWD出库");
        }

        FbaShipmentEntity fbaShipmentEntity = fbaShipmentService.getById(awdOutstockEntity.getFbaShipmentId());
        if (Objects.isNull(fbaShipmentEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "FBA货件");
        }

        List<FbaShipmentDetailEntity> fbaShipmentDetailEntityList = new ArrayList<>();
        for (AwdOutstockDetailEntity awdOutstockDetailEntity : awdOutstockDetailEntityList) {
            if (StringUtils.isBlank(awdOutstockDetailEntity.getSkuId())) {
                throw new ServiceException(ApiError.MAPPING_MSKU_NOT_MAPPING, awdOutstockDetailEntity.getMsku());
            }
            FbaShipmentDetailEntity fbaShipmentDetailEntity = fbaShipmentDetailService.lambdaQuery()
                    .eq(FbaShipmentDetailEntity::getMainId, fbaShipmentEntity.getId())
                    .eq(FbaShipmentDetailEntity::getSkuId, awdOutstockDetailEntity.getSkuId())
                    .one();
            if (Objects.isNull(fbaShipmentDetailEntity)) {
                throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "FBA货件");
            }
            fbaShipmentDetailEntity.setDeliveryQty(awdOutstockDetailEntity.getQty());
            fbaShipmentDetailEntityList.add(fbaShipmentDetailEntity);
        }

        FirstMileDeliveryDTO.AddDTO addDTO = new FirstMileDeliveryDTO.AddDTO();
        List<FirstMileDeliveryDetailDTO.AddDTO> addDetailDTOList = new ArrayList<>();

        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(dto.getShopId());
        if (Objects.nonNull(shopInfoEntity)) {
            addDTO.setDeliveryWarehouseId(StringUtils.isNotBlank(shopInfoEntity.getAwdWarehouseId()) ? shopInfoEntity.getAwdWarehouseId() : "");
            addDTO.setDeliveryWarehouseName(StringUtils.isNotBlank(shopInfoEntity.getAwdWarehouseName()) ? shopInfoEntity.getAwdWarehouseName() : "");
            addDTO.setFulfillmentCenter(shopInfoEntity.getWarehouseName());
            addDTO.setDestWarehouseId(shopInfoEntity.getWarehouseId());
            addDTO.setDestWarehouseName(shopInfoEntity.getWarehouseName());
        }

        List<String> skuIdList = awdOutstockDetailEntityList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)){
            throw new ServiceException(ApiError.PRODUCT_SKU_NOT_FOUND);
        }

        addDTO.setSourceId(dto.getId());
        addDTO.setSourceCode(dto.getCode());
        addDTO.setSourceType(SourceTypeEnum.AWD_OUT_STOCK.getCode());
        addDTO.setDemandType(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode());
        addDTO.setShopId(dto.getShopId());
        addDTO.setShopName(dto.getShopName());
        addDTO.setCountryId(fbaShipmentEntity.getCountryId());
        addDTO.setCountryName(fbaShipmentEntity.getCountryName());


        for (AwdOutstockDetailEntity awdOutstockDetailEntity : awdOutstockDetailEntityList) {
            FirstMileDeliveryDetailDTO.AddDTO firstMildDetailDTO = new FirstMileDeliveryDetailDTO.AddDTO();
            firstMildDetailDTO.setPlatformSpuNo(awdOutstockDetailEntity.getAsin());
            firstMildDetailDTO.setPlatformSkuNo(awdOutstockDetailEntity.getMsku());
            firstMildDetailDTO.setFnSku(awdOutstockDetailEntity.getFnsku());
            firstMildDetailDTO.setSkuId(awdOutstockDetailEntity.getSkuId());
            firstMildDetailDTO.setSkuNo(awdOutstockDetailEntity.getSkuNo());
            firstMildDetailDTO.setPlanQty(awdOutstockDetailEntity.getQty());
            firstMildDetailDTO.setDeclareQty(awdOutstockDetailEntity.getQty());
            firstMildDetailDTO.setDeliveryQty(awdOutstockDetailEntity.getQty());
            firstMildDetailDTO.setSourceDetailId(awdOutstockDetailEntity.getId());
            firstMildDetailDTO.setFbaShipmentCode(awdOutstockEntity.getFbaShipmentCode());
            for (SkuVO skuVO : skuVOList) {
                firstMildDetailDTO.setNetWeight(skuVO.getNetWeight());
                firstMildDetailDTO.setProductSizeLength(skuVO.getProductLength());
                firstMildDetailDTO.setProductSizeWidth(skuVO.getProductWidth());
                firstMildDetailDTO.setProductSizeHeight(skuVO.getProductHeight());
                firstMildDetailDTO.setNetWeight(skuVO.getNetWeight());
            }
            addDetailDTOList.add(firstMildDetailDTO);
        }
        addDTO.setDetailList(addDetailDTOList);
        BaseResultDTO.AddDTO add = this.add(addDTO);
        //提交
        this.submit(add.getId(),Boolean.FALSE);
        //审核
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setId(add.getId());
        approveOneDTO.setDeliveryDate(awdOutstockEntity.getBillDate());
        approveOneDTO.setComment("");
        approveOneDTO.setType(ApproveTypeEnum.PASS.getStatus());
        Boolean originalValue = UserContext.getIsUserSystem();
        UserContext.setIsUserSystem(Boolean.TRUE);
        try {
            approve(approveOneDTO);
            // 更新FBA货件的发货数量
            if (CollectionUtils.isNotEmpty(fbaShipmentDetailEntityList)) {
                fbaShipmentDetailService.updateBatchById(fbaShipmentDetailEntityList);
            }
        }finally {
            //恢复系统标识
            UserContext.setIsUserSystem(originalValue);
        }
        return BatchResultDTO.success(dto.getId(), dto.getCode(), "操作成功");
    }

    @Override
    public PagingVO<FirstMileDeliveryDTO.CancelDeliveryListDTO> cancelDeliveryPaging(PagingDTO<FirstMileDeliveryDTO.CancelDeliveryParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<FirstMileDeliveryDTO.CancelDeliveryListDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FirstMileDeliveryDTO.CancelDeliveryListDTO> pageData = this.baseMapper.cancelDeliveryPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelDelivery(FirstMileDeliveryDTO.CancelDeliveryDTO cancelDeliveryDTO) {

        //查询数据是否已进行重量分摊
        List<FirstMileWeightAllocationEntity> list = FeignQuery.create(FirstMileWeightAllocationEntity.class).eq(FirstMileWeightAllocationEntity::getLogisticsBillId, cancelDeliveryDTO.getLogisticsBillId()).list();
        if (CollUtil.isNotEmpty(list)) {
            return BatchResultDTO.fail(cancelDeliveryDTO.getLogisticsBillId(), cancelDeliveryDTO.getBusinessCode(), "该发货单已进行重量分摊，不能操作取消发货");
        }

        //查询箱子明细数据
        WmsCartonDetailEntity wmsCartonDetailEntity = wmsCartonDetailService.getById(cancelDeliveryDTO.getCartonDetailId());
        if (ObjectUtil.isEmpty(wmsCartonDetailEntity)) {
            return BatchResultDTO.fail(cancelDeliveryDTO.getCartonDetailId(), cancelDeliveryDTO.getSkuNo(), "装箱明细不存在");
        }
        //查询箱子
        WmsCartonSpecEntity old = wmsCartonSpecService.getById(cancelDeliveryDTO.getCartonSpecId());
        if (ObjectUtil.isEmpty(old)) {
            return BatchResultDTO.fail(cancelDeliveryDTO.getCartonSpecId(), cancelDeliveryDTO.getCartonSpecId(), "箱子不存在");
        }
        wmsCartonDetailEntity.setIsCancelRequired(cancelDeliveryDTO.getIsCancelRequired());
        wmsCartonDetailService.updateById(wmsCartonDetailEntity);

        WmsCartonSpecEntity cartonSpecEntity = new WmsCartonSpecEntity();
        BeanUtil.copyProperties(old, cartonSpecEntity);
        cartonSpecEntity.setBoxHeight(cancelDeliveryDTO.getBoxHeight());
        cartonSpecEntity.setBoxWidth(cancelDeliveryDTO.getBoxWidth());
        cartonSpecEntity.setBoxLength(cancelDeliveryDTO.getBoxLength());
        cartonSpecEntity.setPackageWeight(cancelDeliveryDTO.getPackageWeight());
        cartonSpecEntity.setWeightUnit(cancelDeliveryDTO.getWeightUnit());
        wmsCartonSpecService.updateById(cartonSpecEntity);

        //添加日志
        addLogCancelDelivery(cancelDeliveryDTO, old, wmsCartonDetailEntity, cartonSpecEntity);
        return BatchResultDTO.success(wmsCartonDetailEntity.getId(), wmsCartonDetailEntity.getSkuNo(), "操作成功");
    }

    @Override
//    @Async("wmsErpExecutor")
    public void sendMsg(List<String> logisticsBillIds){
        List<LogisticsBillEntity> list = FeignQuery.create(LogisticsBillEntity.class).in(LogisticsBillEntity::getId, logisticsBillIds).list();
        if(CollUtil.isNotEmpty(list)){
            List<String> fmIds = list.stream().map(LogisticsBillEntity::getOutstockId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if(CollUtil.isNotEmpty(fmIds)){
                List<FirstMileDeliveryEntity> firstMileDeliveryEntities = listByIds(fmIds);
                List<String> jsonStrList = new ArrayList<>(firstMileDeliveryEntities.size());
                TableName tableName = FirstMileDeliveryEntity.class.getDeclaredAnnotation(TableName.class);
                for (FirstMileDeliveryEntity firstMileDeliveryEntity : firstMileDeliveryEntities) {
                    Map<String, Object> before = BeanUtil.beanToMap(firstMileDeliveryEntity);
                    Map<String, Object> after = new HashMap<>(before);
                    after.put("table", tableName.value());
                    after.put("P_TAG_IUD", "U");
                    after.put("db", "erp-wms");
                    after.put("cancelDelivery", Boolean.TRUE);
                    Map<String, Map<String, Object>> map = new HashMap<>();
                    map.put("before", before);
                    map.put("after", after);
                    String jsonStr = JSONUtil.toJsonStr(map);
                    jsonStrList.add(jsonStr);
                }
                thirdNoticePushRecordFeign.batchSendMqRecordConsumer(jsonStrList);
            }
        }
    }

    @Override
    public  PagingVO<TmsDeclareBillDTO.NotGenerateDetailDTO> listNotGenerateFmDetailPaging(PagingDTO<TmsDeclareBillDTO.NotGenerateParamDTO> dto) {
        TmsDeclareBillDTO.NotGenerateParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<TmsDeclareBillDTO.NotGenerateDetailDTO> pageData =  baseMapper.listNotGenerateDeclareFmDetail(query,params);
        handleNotGenerateData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 产品添加数据处理
     * @author will
     * @date 2026/4/22 17:56
     * @param list
     */
    private void handleNotGenerateData(List<TmsDeclareBillDTO.NotGenerateDetailDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //查询币别名称
        List<String> currencyList = list.stream().map(TmsDeclareBillDTO.NotGenerateDetailDTO::getDeclareCurrency).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<DictCurrencyEntity> currencyEntityList = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, currencyList).list();

        //查询单位名称
        List<BasicDictEntity> declareUnitList = FeignQuery.create(BasicDictEntity.class).eq(BasicDictEntity::getType, "declareUnit").list();

        //查询原产国名称
        List<String> sourceCountryIdList = list.stream().map(TmsDeclareBillDTO.NotGenerateDetailDTO::getSourceCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> sourceCountryList = sysDictFeign.listCountryByIds(sourceCountryIdList);

        for (TmsDeclareBillDTO.NotGenerateDetailDTO dto : list) {
            //币别名称
            DictCurrencyEntity currencyEntity = currencyEntityList.stream().filter(v -> v.getId().equals(dto.getDeclareCurrency())).findFirst().orElse(null);
            if (Objects.nonNull(currencyEntity)) {
                dto.setDeclareCurrencyName(currencyEntity.getName());
            }
            //报关单位名称
            BasicDictEntity unitEntity = declareUnitList.stream().filter(v -> v.getValue().equals(dto.getDeclareUnit())).findFirst().orElse(null);
            if (Objects.nonNull(unitEntity)) {
                dto.setDeclareUnitName(unitEntity.getName());
            }
            //国家名称
            DictCountryEntity countryEntity = sourceCountryList.stream().filter(v -> v.getId().equals(dto.getSourceCountry())).findFirst().orElse(null);
            if (Objects.nonNull(countryEntity)) {
                dto.setSourceCountryName(countryEntity.getNameCn());
            }
        }
    }


    @Override
    public List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforePushFmDeclare(TmsDeclareBillDTO.PushDeclareBeforeParamDTO dto) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list = baseMapper.listBeforePushFmDeclare(dto.getIds());
        handleDeclareData(list);
        return list;
    }

    @Override
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> listAfterPushFmDeclare(TmsDeclareBillDTO.PushDeclareBeforeParamDTO dto) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list = baseMapper.listBeforePushFmDeclare(dto.getIds());
        return tmsDeclareBillFeign.autoMergeDeclareBillView(new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(dto.getIsMultipleMerge(),list));
    }

    @Override
    public TmsDeclareBillDTO.MergeDeclareBillDTO listAfterPushFmDeclareNoMerge(List<TmsDeclareBillDTO.PushDeclareNoMergeDTO> list) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = baseMapper.listFmDeclareMinSourceDetail(list);
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> declareSourceDetailList = prepareFmMinDeclareSourceDetail(sourceDetailList);
        return buildFmMinMergeDeclareBillList(declareSourceDetailList);
    }
    /**
     * 按产品物流补齐报关字段，并按组合品申报类型拆分 BOM 子件。
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetailList
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> prepareFmMinDeclareSourceDetail(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        if (CollUtil.isEmpty(sourceDetailList)) {
            return Collections.emptyList();
        }
        List<String> skuIdList = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<ProductDetailDTO.ProductLogisticDTO> productLogisticsList = plmTaskFeign.listProductLogisticsByIds(skuIdList);
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = CollUtil.isEmpty(productLogisticsList)
                ? new HashMap<>()
                : productLogisticsList.stream().collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId, item -> item, (a, b) -> a));

        List<String> countryIdList = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getCountryId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<DictCountryEntity> countryList = CollUtil.isEmpty(countryIdList) ? Collections.emptyList() : sysDictFeign.listCountryByIds(countryIdList);
        Map<String, String> countryNameMap = CollUtil.isEmpty(countryList)
                ? new HashMap<>()
                : countryList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn, (a, b) -> a));


        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = new ArrayList<>();
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO : sourceDetailList) {
            if (Objects.isNull(detailDTO)) {
                continue;
            }
            detailDTO.setCountryName(countryNameMap.get(detailDTO.getCountryId()));
            ProductDetailDTO.ProductLogisticDTO productLogisticsDTO = logisticsMap.get(detailDTO.getSkuId());
            if (Objects.nonNull(productLogisticsDTO)
                    && CombinationDeclareTypeEnums.SPLIT.getCode().equals(productLogisticsDTO.getCombinationDeclareType())
                    && Boolean.TRUE.equals(productLogisticsDTO.getIsCombination())
                    && CollUtil.isNotEmpty(productLogisticsDTO.getChildList())) {
                for (ProductDetailDTO.ProductLogisticDTO childLogisticsDTO : productLogisticsDTO.getChildList()) {
                    TmsDeclareBillDTO.SourceDeliveryDetailDTO childDetailDTO = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
                    BeanUtil.copyProperties(detailDTO, childDetailDTO);
                    childDetailDTO.setSkuId(childLogisticsDTO.getSkuId());
                    childDetailDTO.setSkuNo(childLogisticsDTO.getSkuNo());
                    childDetailDTO.setBomVersion(childLogisticsDTO.getBomVersion());
                    childDetailDTO.setBomHistoryId(childLogisticsDTO.getBomHistoryId());
                    childDetailDTO.setQty((detailDTO.getQty() == null ? 0 : detailDTO.getQty()) * (childLogisticsDTO.getChildQty() == null ? 1 : childLogisticsDTO.getChildQty()));
                    fillB2bMinDeclareInfo(childDetailDTO, childLogisticsDTO);
                    result.add(childDetailDTO);
                }
            } else {
                fillB2bMinDeclareInfo(detailDTO, productLogisticsDTO);
                result.add(detailDTO);
            }
        }
        return result;
    }

    /**
     * 填充报关字段。
     * @author will
     * @date 2026/5/9 15:00
     * @param detailDTO
     * @param productLogisticsDTO
     */
    private void fillB2bMinDeclareInfo(TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO,
                                       ProductDetailDTO.ProductLogisticDTO productLogisticsDTO) {
        if (Objects.isNull(detailDTO) || Objects.isNull(productLogisticsDTO)) {
            return;
        }
        detailDTO.setHsCode(productLogisticsDTO.getCustomsCode());
        detailDTO.setProductNameCn(productLogisticsDTO.getDeclareChineseName());
        detailDTO.setDeclareElement(productLogisticsDTO.getDeclareElement());
        detailDTO.setUnit(productLogisticsDTO.getDeclareUnit());
        detailDTO.setUnitPrice(productLogisticsDTO.getPrice());
        detailDTO.setDeclareCurrency(productLogisticsDTO.getDeclareCurrency());
        detailDTO.setDeclareCurrencyName(productLogisticsDTO.getDeclareCurrencyName());
        detailDTO.setDeclareCurrencySymbol(productLogisticsDTO.getDeclareCurrencySymbol());
        detailDTO.setSourceCountry(productLogisticsDTO.getSourceCountry());
        detailDTO.setSourceCountryName(productLogisticsDTO.getSourceCountryName());
        detailDTO.setSourceCargo(StringUtils.defaultIfBlank(productLogisticsDTO.getSourceCargo(), "深圳特区"));
        detailDTO.setExemption(StringUtils.defaultIfBlank(productLogisticsDTO.getExemption(), "照章征税"));
    }

    /**
     * 按报关明细维度组装头程报关单预览，同一明细下保留多箱来源数据。
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetailList 来源箱明细
     * @return 头程报关预览明细
     */
    private TmsDeclareBillDTO.MergeDeclareBillDTO buildFmMinMergeDeclareBillList(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        TmsDeclareBillDTO.MergeDeclareBillDTO billDTO = new TmsDeclareBillDTO.MergeDeclareBillDTO();
        if (CollUtil.isEmpty(sourceDetailList)) {
            return billDTO;
        }
        Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> sourceDetailGroupMap = sourceDetailList.stream()
                .collect(Collectors.groupingBy(this::buildFmMinDeclareGroupKey, LinkedHashMap::new, Collectors.toList()));
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> detailDTOList = new ArrayList<>();
        for (List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceGroup : sourceDetailGroupMap.values()) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO mergeDeclareBillDetailDTO = buildB2bMinMergeDeclareBillDetail(sourceGroup);
            detailDTOList.add(mergeDeclareBillDetailDTO);
        }
        billDTO.setDeclareBillList(detailDTOList);
        return billDTO;
    }

    /**
     * 构建头程报关明细分组键。箱号不参与外层分组，保留在来源明细中用于后续保存中间表。
     *
     * @param detailDTO 来源箱明细
     * @return 报关明细分组键
     */
    private String buildFmMinDeclareGroupKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO) {
        return String.join("|",
                StringUtils.defaultString(detailDTO.getSourceId()),
                StringUtils.defaultString(detailDTO.getSkuId()),
                StringUtils.defaultString(detailDTO.getHsCode()),
                StringUtils.defaultString(detailDTO.getProductNameCn()),
                StringUtils.defaultString(detailDTO.getDeclareElement()),
                StringUtils.defaultString(detailDTO.getUnit()),
                Objects.isNull(detailDTO.getUnitPrice()) ? "" : detailDTO.getUnitPrice().stripTrailingZeros().toPlainString(),
                StringUtils.defaultString(detailDTO.getDeclareCurrency()),
                StringUtils.defaultString(detailDTO.getSourceCountry()),
                StringUtils.defaultString(detailDTO.getCountryId()),
                StringUtils.defaultIfBlank(detailDTO.getSourceCargo(), "深圳特区"),
                StringUtils.defaultIfBlank(detailDTO.getExemption(), "照章征税"));
    }

    private TmsDeclareBillDTO.MergeDeclareBillDetailDTO buildB2bMinMergeDeclareBillDetail(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> detailGroup) {
        TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO = detailGroup.get(0);
        Integer qty = detailGroup.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getQty)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        BigDecimal totalAmount = detailGroup.stream().map(obj -> MathUtil.multiplyWithFour(obj.getUnitPrice(),BigDecimal.valueOf(obj.getQty()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        String businessCode = detailGroup.stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBusinessCode).distinct().collect(Collectors.joining(","));
        String businessDesc = detailGroup.stream()
                .filter(Objects::nonNull)
                .map(item -> StringUtils.isBlank(item.getBoxNo())
                        ? businessCode
                        : CharSequenceUtil.format("{}+{}", businessCode, item.getBoxNo()))
                .distinct()
                .collect(Collectors.joining("、"));

        return TmsDeclareBillDTO.MergeDeclareBillDetailDTO.builder()
                .businessDesc(businessDesc)
                .businessOrderNos(StringUtils.defaultString(businessCode))
                .leadSkuId(detailDTO.getSkuId())
                .skuNo(detailDTO.getSkuNo())
                .hsCode(detailDTO.getHsCode())
                .productNameCn(detailDTO.getProductNameCn())
                .declareElement(detailDTO.getDeclareElement())
                .unit(detailDTO.getUnit())
                .unitPrice(detailDTO.getUnitPrice())
                .qty(qty)
                .totalAmount(totalAmount)
                .sourceCountry(detailDTO.getSourceCountry())
                .sourceCountryName(detailDTO.getSourceCountryName())
                .toCountry(detailDTO.getCountryId())
                .toCountryName(detailDTO.getCountryName())
                .sourceCargo(StringUtils.defaultIfBlank(detailDTO.getSourceCargo(), "深圳特区"))
                .exemption(StringUtils.defaultIfBlank(detailDTO.getExemption(), "照章征税"))
                .declareCurrency(detailDTO.getDeclareCurrency())
                .declareCurrencyName(detailDTO.getDeclareCurrencyName())
                .declareCurrencySymbol(detailDTO.getDeclareCurrencySymbol())
                .mergeRemark("")
                .sourceDeliveryDetailList(detailGroup)
                .build();
    }

    /**
     * 处理报关信息
     * @author will
     * @date 2026/4/27 17:32
     * @param list
     */
    private void handleDeclareData (List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //产品物流信息
        List<String> skuIdList = list.stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductLogisticsEntity> productLogisticsList = FeignQuery.create(ProductLogisticsEntity.class).in(ProductLogisticsEntity::getSkuId, skuIdList).list();
        Map<String, ProductLogisticsEntity> logisticsMap = CollUtil.isEmpty(productLogisticsList) ? new HashMap<>() : productLogisticsList.stream().collect(Collectors.toMap(ProductLogisticsEntity::getSkuId,item -> item));

        //币别明细
        List<DictCurrencyEntity> dictCurrencyList = sysUserFeign.currencyList();
        Map<String, String> currencyMap = CollUtil.isEmpty(dictCurrencyList) ? new HashMap<>() : dictCurrencyList.stream().collect(Collectors.toMap(DictCurrencyEntity::getId,item -> item.getName()));

        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO deliveryDetailDTO : list) {
            ProductLogisticsEntity productLogisticsEntity = logisticsMap.get(deliveryDetailDTO.getSkuId());
            if (Objects.nonNull(productLogisticsEntity)) {
                deliveryDetailDTO.setHsCode(productLogisticsEntity.getCustomsCode());
                deliveryDetailDTO.setProductNameCn(productLogisticsEntity.getDeclareChineseName());
                deliveryDetailDTO.setDeclareElement(productLogisticsEntity.getDeclareElement());
                deliveryDetailDTO.setUnit(productLogisticsEntity.getDeclareUnit());
                deliveryDetailDTO.setUnitPrice(productLogisticsEntity.getDeclarePrice());
                deliveryDetailDTO.setDeclareCurrency(productLogisticsEntity.getDeclareCurrency());
                deliveryDetailDTO.setDeclareCurrencySymbol(productLogisticsEntity.getDeclareCurrencySymbol());
                deliveryDetailDTO.setDeclareCurrencyName(currencyMap.get(productLogisticsEntity.getDeclareCurrency()));
            }
        }
    }


    /**
     * 取消发货添加日志
     * @author will
     * @date 2026/2/2 11:27
     * @param cancelDeliveryDTO
     * @param old
     * @param wmsCartonDetailEntity
     * @param cartonSpecEntity
     * @return void
     */
    private void addLogCancelDelivery (FirstMileDeliveryDTO.CancelDeliveryDTO cancelDeliveryDTO,WmsCartonSpecEntity old,
                                       WmsCartonDetailEntity wmsCartonDetailEntity,WmsCartonSpecEntity cartonSpecEntity) {
        //添加头程物流单操作日志
        StringBuilder logContent = new StringBuilder();
        String formatContent = CharSequenceUtil.format("箱号【{}】SKU【{}】",
                old.getBoxSpecNo(),
                cancelDeliveryDTO.getSkuNo()
        );
        //是否取消分摊
        logContent.append(formatContent);

        String cancelContent = "";
        if (!cancelDeliveryDTO.getIsCancelRequired().equals(wmsCartonDetailEntity.getIsCancelRequired())) {
            cancelContent = CharSequenceUtil.format("【{}】取消分摊",
                    cancelDeliveryDTO.getIsCancelRequired() ? "是" : "否"
            );
            logContent.append(cancelContent);
        }
        //比较装箱重量和尺寸是否有变化
        String sizeContent = "";
        String oldFormat = CharSequenceUtil.format("{}-{}-{}-{}-{}", old.getPackageWeight().stripTrailingZeros().toPlainString(),old.getWeightUnit(), old.getBoxLength().stripTrailingZeros().toPlainString(), old.getBoxWidth().stripTrailingZeros().toPlainString(), old.getBoxHeight().stripTrailingZeros().toPlainString());
        String thisFormat = CharSequenceUtil.format("{}-{}-{}-{}-{}", cancelDeliveryDTO.getPackageWeight(),cancelDeliveryDTO.getWeightUnit(), cancelDeliveryDTO.getBoxLength(), cancelDeliveryDTO.getBoxWidth(), cancelDeliveryDTO.getBoxHeight());
        if (!CharSequenceUtil.equals(oldFormat,thisFormat)) {
            sizeContent = CharSequenceUtil.format("更新[装箱重量/装箱尺寸]由[{}/{}]编辑为[{}/{}]",
                    CharSequenceUtil.format("{}{}",old.getPackageWeight(),old.getWeightUnit()),
                    CharSequenceUtil.format("{}*{}*{}", old.getBoxLength(), old.getBoxWidth(), old.getBoxHeight()),
                    CharSequenceUtil.format("{}{}",cartonSpecEntity.getPackageWeight().stripTrailingZeros().toPlainString(),cartonSpecEntity.getWeightUnit()),
                    CharSequenceUtil.format("{}*{}*{}", cartonSpecEntity.getBoxLength().stripTrailingZeros().toPlainString(), cartonSpecEntity.getBoxWidth().stripTrailingZeros().toPlainString(), cartonSpecEntity.getBoxHeight().stripTrailingZeros().toPlainString())
            );
            logContent.append(sizeContent);
        }
        if (CharSequenceUtil.isNotBlank(cancelContent) || CharSequenceUtil.isNotBlank(sizeContent)) {
            tmsFirstMileLogisticFeign.addFirstMileLogisticLog(new TmsFirstMileLogisticDTO.AddLogDTO(cancelDeliveryDTO.getLogisticsBillId(), "取消发货", logContent.toString()));
        }
    }
}

