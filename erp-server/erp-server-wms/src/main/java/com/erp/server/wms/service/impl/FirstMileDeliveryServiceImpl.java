package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TmsDeclareBillFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.convert.FirstMileDeliveryConverter;
import com.erp.server.wms.mapper.FirstMileDeliveryMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private WarehouseLocationService warehouseLocationService;
    @Autowired
    private MachineInfoService machineInfoService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private FbaShipmentService fbaShipmentService;
    @Autowired
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Autowired
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Autowired
    private WmsAttachmentService wmsAttachmentService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private TransferInfoService transferInfoService;
    @Autowired
    private SkuMappingFeign skuMappingFeign;
    @Autowired
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Autowired
    private WmsCartonSpecService wmsCartonSpecService;
    @Autowired
    private WmsCartonService wmsCartonService;
    @Autowired
    private WmsCartonDetailService wmsCartonDetailService;
    @Autowired
    private WmsDeliveryPlanService wmsDeliveryPlanService;
    @Autowired
    private DictBasicService dictBasicService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private TmsDeclareBillFeign tmsDeclareBillFeign;
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
    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private FbaShipmentPackingService fbaShipmentPackingService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileDeliveryDTO.AddDTO addDTO) {
        FirstMileDeliveryEntity firstMileDeliveryEntity = new FirstMileDeliveryEntity();
        BeanMapperUtils.copy(addDTO, firstMileDeliveryEntity);
        String idStr = IdWorker.getIdStr();
        firstMileDeliveryEntity.setId(idStr);
        // 数据处理
        handleData(firstMileDeliveryEntity);

        log.info("开始新增发货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHD);
        firstMileDeliveryEntity.setCode(code);
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货单" , firstMileDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), firstMileDeliveryEntity.getId(), "新增操作");

        //新增详情信息
        firstMileDeliveryDetailService.add(addDTO, firstMileDeliveryEntity.getId());
        //新增装箱任务
//        packingTaskService.addPackingByFirstMileDelivery(firstMileDeliveryEntity);
        return new BaseResultDTO.AddDTO(firstMileDeliveryEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileDeliveryDTO.UpdateDTO updateDTO) {
        FirstMileDeliveryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货单"));
        // 待提交和审核不通过允许修改
        if (!old.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || old.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        FirstMileDeliveryEntity firstMileDeliveryEntity =  BeanMapperUtils.map(FirstMileDeliveryEntity.class, updateDTO);

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
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileDeliveryEntity.getCode(), "发货单");
        operateLogService.addModuleOperateLogByObj(old, firstMileDeliveryEntity, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), firstMileDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
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
    public BatchResultDTO submit(String id) {
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
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录发货单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(FirstMileDeliveryDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO resultAdd = this.add(dto);
        // 提交
        this.submit(resultAdd.getId());
        return resultAdd;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(FirstMileDeliveryDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        FirstMileDeliveryEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        //是否存在下游关联的未作废或未删除的直接调拨单
        Boolean validate = validateExistsTransferInfo(dto.getId());
        if(validate){
            throw new ServiceException(ApiError.ERROR_EXISTS_TRANSFER_INFO);
        }

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
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(entity.getId()));
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
                List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(Arrays.asList(entity.getId()));
                List<MachineInfoEntity> approveMachineInfoEntityList = machineInfoEntityList.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(approveMachineInfoEntityList)) {
                    throw new ServiceException(ApiError.IS_GENERATE_MACHINE, entity.getCode());
                }
            }

            List<String> skuNos = isCombinationList.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);

            //校验组合SKU库存量是否满足调出，否则无法审核通过，提示：SKU【SKU编码】【发货仓】冻结库存不足，无法审核发货单
            for (FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity : isCombinationList) {
                //及时库存
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
                Integer usableInventoryTotal = inventoryService.getInventoryTotal(entity.getInventoryOrgId() ,entity.getDeliveryWarehouseId(), skuVO.getSkuId(), firstMileDeliveryDetailEntity.getWarehouseLocation(), InventoryStatusEnum.FROZEN.getCode());
                if (firstMileDeliveryDetailEntity.getDeliveryQty() > usableInventoryTotal) {
                    throw new ServiceException(ApiError.FBA_DELIVERY_INVENTORY_INSUFFICIENT, firstMileDeliveryDetailEntity.getSkuNo(), entity.getDeliveryWarehouseName());
                }
            }
        }

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单", approveType.getName(), dto.getComment());
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
            if (StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Arrays.asList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WAREHOUSE_CODE_XGWJ_FBA_NOT_EXIST);
                }
                destWarehouse.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                destWarehouse.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }

        //查询在途仓
        WarehouseEntity warehouseEntity = warehouseService.getById(destWarehouse.getOnwayWarehouseId());

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：头程发货单
        addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
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
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Arrays.asList(entity.getId()));

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity detailEntity : detailEntityList) {
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            //映射产品信息
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(detailEntity.getDeliveryQty());
            detailAddDto.setOutWarehouseId(entity.getDeliveryWarehouseId());
            detailAddDto.setOutWarehouseLocation(detailEntity.getWarehouseLocation());
            detailAddDto.setInWarehouseId(warehouseEntity.getId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(detailEntity.getId());
            //如果是备货海外仓
            if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())) {
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

            detailAddDtoList.add(detailAddDto);
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
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        FirstMileDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //如果是FBA货件来源，反审核修改货件发货状态和发货数量
        if (FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(entity.getDemandType())) {
            fbaShipmentService.deliveryDisApprove(entity);
        }

        //如果是发货计划来源，反审核修改发货状态
        if (SourceTypeEnum.DELIVERY_PLAN.getCode().equals(entity.getSourceType())) {
            WmsDeliveryPlanEntity planEntity = wmsDeliveryPlanService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(planEntity)) {
                //如果存在有一个审核通过的发货单，状态都是已发货
                List<FirstMileDeliveryEntity> firstMileDeliveryEntities = this.listBySourceIds(Arrays.asList(entity.getSourceId()));
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(deliveryEntities)) {
                    //如果没有审核通过否发货单，修改发货计划单的发货状态为未发货
                    wmsDeliveryPlanService.updateDeliveryStatus(Arrays.asList(entity.getSourceId()), FbaDeliveryStatusEnum.UN_SHIPPED.getCode());
                }
            }
        }

        //查找发货单下推的分步式调出单自动反审并删除
        List<TransferInfoEntity> transferInfoEntities = transferInfoService.listBySourceIds(Arrays.asList(id));
        //直接调拨单已审核先反审核
        if (CollectionUtils.isNotEmpty(transferInfoEntities)) {
            transferInfoEntities.forEach(transferInfoEntity -> {
                transferInfoService.disApprove(transferInfoEntity, Boolean.FALSE, Boolean.TRUE);
            });
        }
        //直接调拨单审核中先撤销
        List<String> approveIngTransferOutIds = transferInfoEntities.stream().filter(req -> ApproveStatusEnum.APPROVE_ING.getStatus().equals(req.getApproveStatus())).map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(approveIngTransferOutIds)) {
            transferInfoService.cancelProcess(approveIngTransferOutIds);
        }
        //直接调拨单单删除
        List<String> deletedTransferOutIds = transferInfoEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deletedTransferOutIds)) {
            transferInfoService.delete(deletedTransferOutIds);
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(FirstMileDeliveryEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        //已经有签收数量的发货单不允许反审核
        if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(entity.getSourceType())) {
            FbaShipmentEntity shipmentEntity = fbaShipmentService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(shipmentEntity)) {
                List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(entity.getSourceId()));
                List<String> detailIds = fbaShipmentDetailEntities.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
                List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(detailIds);
                int sum = fbaShipmentReceiveEntities.stream().mapToInt(req -> req.getReceiveQty()).sum();
                if (sum > 0) {
                    throw new ServiceException(ApiError.FBA_SHIPMENT_RECEIVE_EXIST);
                }
            }
        }
        //已下推入库单
        OverseasWarehouseInboundEntity inboundEntity = overseasWarehouseInboundService.getBySourceId(entity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
        if (ObjectUtil.isNotEmpty(inboundEntity)) {
            throw new ServiceException(ApiError.GENERATE_INBOUND_NOT_DIS_APPROVE, inboundEntity.getCode());
        }

        //校验下游单据是否生成【包含报关单，物流单】状态为已生成 不可反审核【提示：报关单/物流单[单号]已生成，不可反审核】
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Arrays.asList(entity.getId()));
        if (CollectionUtil.isNotEmpty(tmsFirstMileLogisticEntities)) {
            throw new ServiceException(ApiError.TMS_FIRST_MILE_LOGISTIC_EXISTS, tmsFirstMileLogisticEntities.get(0).getTransportNo());
        }
        List<TmsDeclareBillEntity> tmsDeclareBillEntities = tmsDeclareBillFeign.listBySourceIds(Arrays.asList(entity.getId()));
        if (CollectionUtil.isNotEmpty(tmsDeclareBillEntities)) {
            throw new ServiceException(ApiError.TMS_DECLARE_BILL_EXISTS, tmsDeclareBillEntities.get(0).getCode());
        }

        //是否存在下游关联的未作废或未删除的直接调拨单
        Boolean validate = validateExistsTransferInfo(entity.getId());
        if(validate){
            throw new ServiceException(ApiError.ERROR_EXISTS_TRANSFER_INFO);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(FirstMileDeliveryEntity entity, PackingTaskEntity packingTask) {
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1043);
        }
        if (Objects.nonNull(packingTask)  && !PackingTaskStatusEnum.UNPACKED.getCode().equals(packingTask.getPackingStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"已生成装箱清单且装箱中&已装箱不允许删除");
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
        firstMileDeliveryDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除发货单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
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
           throw new ServiceException(ApiError.ERROR_98005);
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
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        FirstMileDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改发货单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
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
        updateForApprove(entity.getId(), approveStatus.getStatus());


        //审核通过
        if (ApproveType.PASS.equals(dto.getType())) {
            //查询发货详情
            List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(entity.getId()));
            RequisitionApplicationEntity application = requisitionApplicationService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(application)) {
                //如果是FBA货件来源，审核通过修改货件发货状态为已发货
                if (RequisitionApplicationTypeEnum.FBA.getCode().equals(application.getType())) {
                    fbaShipmentService.deliveryStatus(entity);
                } else {
                    //如果是发货计划来源
                    //审核通过修改发货状态为已发货
                    wmsDeliveryPlanService.updateDeliveryStatus(Collections.singletonList(application.getSourceId()), FbaDeliveryStatusEnum.SHIPPED.getCode());
                }
            }

            //如果是备货海外仓
            if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())) {
                //查询是否下推了入库单
                OverseasWarehouseInboundEntity inboundEntity = overseasWarehouseInboundService.getBySourceId(entity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
                if (ObjectUtil.isEmpty(inboundEntity)) {
                    throw new ServiceException(ApiError.NOT_EXISTS_OVERSEAS_WAREHOUSE_INBOUND_NOT_APPROVE);
                }

                //用目的仓查询是否绑定第三方仓
                List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByWarehouseIds(Arrays.asList(entity.getDestWarehouseId()));
                // 查询发货目的仓平台
                OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDestWarehouseId());

                //有对接海外仓API：调用入库单的提交审核，获取审核结果，审核通过后入库单状态为待签收；审核不通过为异常，操作日志记录失败原因，并显示在备注栏
                if (CollectionUtils.isNotEmpty(overseasProviderWarehouseEntities) && Objects.nonNull(providerEntity)) {
                    // 推送第三方发货单审核通过
                    ApiResult<String> resultInfo = overseasWarehouseInboundService.pullThirdOverseasPlatform(providerEntity, inboundEntity, detailEntityList, OverseasVerifyEnum.PASS.getCode());
                    if (200 != resultInfo.getCode()) {
                        log.error("推送第三方仓库【发货单审核】失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                        throw new ServiceException("推送第三方仓库【发货单审核】失败:" + resultInfo.getMsg());
                    }
                    log.info("推送第三方仓库【发货单审核】结果: ={}", JSONUtil.toJsonStr(resultInfo));

                    String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据推送第三方仓库发货审核操作 平台返回结果：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货单", JSONUtil.toJsonStr(resultInfo));
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "发货单审核");


                }

                //入库单状态修改为待签收
                overseasWarehouseInboundService.updateInstockStatus(Arrays.asList(inboundEntity.getId()), OverseasInstockStatusEnum.TO_BE_SIGNED.getCode());
            }

            //匹配中转配置
            CfgRuleOutDTO.MatchTransferRuleDTO matchRuleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
            matchRuleDTO.setType(StockOutTransferTypeEnum.FIRST_MILE.getCode());
            matchRuleDTO.setReceiveCountry(entity.getCountryId());
            matchRuleDTO.setDestWarehouse(entity.getDestWarehouseId());
            matchRuleDTO.setFromWarehouse(entity.getDeliveryWarehouseId());
            Boolean isMatchRule = cfgRuleOutService.matchTransferRule(matchRuleDTO);
            //发货仓与中转仓一致
            CfgSettingValueDTO.TransitSettingDTO transitSettingDTO = getTransitSettingDTO();
            boolean isEqual = transitSettingDTO.getWarehouseId().equals(entity.getDeliveryWarehouseId());
            if (isMatchRule && !isEqual){
                //中转
               generateTransferToUlanzi(entity, detailEntityList);
               generateTransferFromUlanzi(entity, detailEntityList);
            }else {
                generateTransferOut(entity, detailEntityList);
            }

            //走TMS自动生成物流单逻辑
            AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
                    .id(entity.getId())
                    .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_APPROVE)
                    .sourceTypeEnum(SourceTypeEnum.FIRST_MILE_DELIVERY)
                    .firstMileDeliveryEntity(entity)
                    .build();
            try {
                if(FmDeliveryLogisticsStatusEnum.WAIT.equals(entity.getLogisticsStatus())){
                   Boolean autoGenerateResult = tmsFirstMileLogisticFeign.autoGenerateFirstMileLogistic(autoGenerateBillDTO);
                   if(autoGenerateResult){
                       FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                       updateStatusDTO.setIds(Arrays.asList(entity.getId()));
                       updateStatusDTO.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.getCode());
                       this.updateStatus(updateStatusDTO);
                   }
                }
            }catch (Exception e){
                log.error("头程发货单{} 审核后自动生成物流单失败>>>>>>{}", entity.getCode(), e.getMessage());
                throw new ServiceException(StrUtil.format("头程发货单{} 审核后自动生成物流单失败>>>>>>{}", entity.getCode(), e.getMessage()));
            }

            try {
                if(WmsDeclareStatusEnum.WAIT.equals(entity.getDeclareStatus())){
                    Boolean autoGenerateResult = tmsDeclareBillFeign.autoGenerateFirstMileDeclare(autoGenerateBillDTO);
                    if(autoGenerateResult){
                        FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                        updateStatusDTO.setIds(Arrays.asList(entity.getId()));
                        updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
                        this.updateStatus(updateStatusDTO);
                    }
                }
            }catch (Exception e){
                log.error("头程发货单{} 审核后自动生成报关单失败>>>>>>{}", entity.getCode(), e.getMessage());
                throw new ServiceException(StrUtil.format("头程发货单{} 审核后自动生成报关单失败>>>>>>{}", entity.getCode(), e.getMessage()));
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public FirstMileDeliveryDTO.ViewDTO view(String id) {
        //发货单主信息
        FirstMileDeliveryEntity firstMileDeliveryEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到发货单数据"));
        FirstMileDeliveryDTO.ViewDTO data = BeanMapperUtils.map(FirstMileDeliveryDTO.ViewDTO.class, firstMileDeliveryEntity);

        //查询头程物流单
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Arrays.asList(id));

        //发货单详情
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(id));
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
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
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
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(data.getInventoryOrgId()));

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
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(data.getId()));
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
        if (CollectionUtil.isNotEmpty(tmsFirstMileLogisticEntities)) {
            LogisticsBillEntity tmsFirstMileLogisticEntity = tmsFirstMileLogisticEntities.get(0);
            //渠道
            if (StringUtils.isNotBlank(tmsFirstMileLogisticEntity.getChannelId())) {
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
            logisticsViewDTO.setDeliveryTime(tmsFirstMileLogisticEntity.getDeliveryTime());
            //备注
            logisticsViewDTO.setLogisticsRemark(tmsFirstMileLogisticEntity.getRemark());
            //物流运单号
            logisticsViewDTO.setTrackingNoList(Arrays.asList(tmsFirstMileLogisticEntity.getTransportNo()));
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
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(FirstMileDeliveryEntity::getId, id)
            .set(FirstMileDeliveryEntity::getApproveUserId, userInfo.getUid())
            .set(FirstMileDeliveryEntity::getApproveUserName, userInfo.getUserName())
            .set(FirstMileDeliveryEntity::getApproveStatus, approveStatus)
            .set(FirstMileDeliveryEntity::getApproveTime, LocalDateTime.now())
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
            throw new ServiceException(ApiError.WAIT_SUBMIT_GENERATE_MACHINE);
        }

        //只有组合SKU允许下推加工单
        List<FirstMileDeliveryDetailEntity> entities = firstMileDeliveryDetailService.listByMainIds(ids);
        List<FirstMileDeliveryDetailEntity> entityList = entities.stream()
                .filter(req -> Boolean.TRUE.equals(req.getIsCombination()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.COMBINATION_GENERATE_MACHINE);
        }

        //校验是否已经下推过加工单
        List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(ids);
        List<MachineInfoEntity> collect = machineInfoEntityList.stream().filter(req -> InvalidStatusEnum.NOT_VOIDED.getStatus().equals(req.getInvalidStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.EXIST_GENERATE_MACHINE_INFO);
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
        List<ProductBomInfoDTO.skuBomVersion> skuBomVersionList = plmTaskFeign.listBomVersionBySkuNos(skuNos);

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
            ProductBomInfoDTO.skuBomVersion bomVersionObj = skuBomVersionList.stream().filter(req -> req.getSkuNo().equals(view.getSkuNo())).distinct().findFirst().orElse(null);
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
                resultDTOS.add(machineInfoService.approve(entity,ApproveType.PASS,"",null));
            }catch (Exception e){
                log.error("加工单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
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
                MachineDetailDTO.AddDTO addDetailDTO = new MachineDetailDTO.AddDTO();
                SkuVO skuVO = skuVOList.stream().filter(obj -> obj.getSkuNo().equals(view.getSkuNo())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                addDetailDTO.setSkuId(skuVO.getSkuId());
                addDetailDTO.setSkuNo(skuVO.getSkuNo());
                addDetailDTO.setWarehouseLocation(view.getWarehouseLocation());
                addDetailDTO.setQty(view.getAssembleQty());
                addDetailDTO.setReferenceVersion(view.getBomVersion());
                List<MachineSubComponentsDTO.AddDTO> subComponentsList = new ArrayList<>();
                //子件信息
                List<FirstMileDeliveryDTO.SonItem> sonItemList = view.getSonItemList();
                for (FirstMileDeliveryDTO.SonItem sonItem : sonItemList) {
                    MachineSubComponentsDTO.AddDTO addSubComponentsDTO = new MachineSubComponentsDTO.AddDTO();
                    //产品信息
                    SkuVO child = skuVOList.stream().filter(obj -> obj.getSkuNo().equals(sonItem.getSonSkuNo())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(child)) {
                        throw new ServiceException(ApiError.ERROR_95166);
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
            String id = machineInfoService.add(addDTO);
            ids.add(id);
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
        if (CollectionUtils.isEmpty(ids) && StringUtils.isBlank(fbaShipmentCode)) {
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
        List<SkuMappingDTO.ListSkuParamDTO> skuParamDTOList = new ArrayList<>();
        List<DictCountryEntity> dictCountryEntityList = FeignQuery.list(DictCountryEntity.class);
        for (FirstMileDeliveryDTO.ListDTO detailEntity : list) {
            SkuMappingDTO.ListSkuParamDTO paramDTO = new SkuMappingDTO.ListSkuParamDTO();
            paramDTO.setSkuNo(detailEntity.getSkuNo());
            paramDTO.setWarehouseId(detailEntity.getDeliveryWarehouseId());
            skuParamDTOList.add(paramDTO);
        }
        List<SkuMappingDTO.ListSkuDTO> listSkuDTOS = skuMappingFeign.listBySkuNoList(skuParamDTOList);

        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(ids);

        String bomType = BomTypeEnum.COMBINATION.getType();

        Map<String,Integer> qtyMap = new HashMap<>();
        List<String> sourceCodeList = list.stream().map(v->v.getSourceCode()).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(sourceCodeList);
        List<WmsCartonDetailEntity> wmsCartonDetailEntityList = wmsCartonDetailService.listByTaskIds(taskIds);
       // 属性赋值
        for(FirstMileDeliveryDTO.ListDTO data : list) {
            if (StringUtils.isNotBlank(data.getPackingStatus())) {
                data.setPackingStatusName(PackingTaskStatusEnum.getName(data.getPackingStatus()));
            }else{
                //回查要货申请关联的装箱
                PackingTaskEntity packingTaskEntity = packingTaskEntityList.stream().filter(v->v.getSourceCode().equals(data.getSourceCode())).findFirst().orElse(new PackingTaskEntity());
                if(StringUtils.isBlank(packingTaskEntity.getPackingStatus())){
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
            String stockSku = listSkuDTOS.stream()
                    .filter(req -> data.getSkuId().equals(req.getProductSkuId())
                            && data.getDeliveryWarehouseId().equals(req.getWarehouseId()))
                    .distinct().findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getWarehouseSkuNo())).orElse("");
            data.setStockSku(stockSku);

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
            if(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(data.getDemandType())){
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
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(FirstMileDeliveryEntity entity) {

        // 待提交或审核不通过并且未作废允许提交
        if(!entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus()) && !entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
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
                throw new ServiceException(ApiError.SHIPMENT_NOT_EXIST);
            }
            //根据店铺id查询店铺信息
            List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(Arrays.asList(entity.getShopId()));
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
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Arrays.asList(detailEntity.getSkuNo()));

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
            throw new ServiceException(ApiError.ERROR_NOT_FBA_DELIVERY_DETAIL);
        }

        //只有备货类型等于备货海外仓时，才可以下推入库单，否则提示：只有备货海外仓的发货单允许下推入库单
        if (!FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())) {
            throw new ServiceException(ApiError.IS_DEMAND_OVERSEAS_WAREHOUSE_PUSH_DOWN);
        }

        //发货单待审核的数据可以下推海外仓入库单，其他状态下不可操作，否则提示：只有待审核的数据允许下推海外仓入库单
        if (!ApproveStatusEnum.APPROVE_ING.getCode().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.APPROVE_ING_CAN_TO_OVERSEAS_WAREHOUSE_INBOUND);
        }

        //一个发货单只能下推一个入库单，否则提示：已下推入库单，不允许重复操作，已取消除外
        OverseasWarehouseInboundEntity inboundEntity = overseasWarehouseInboundService.getBySourceId(entity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
        if (ObjectUtil.isNotEmpty(inboundEntity)) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST, inboundEntity.getCode());
        }


        //未装箱不能下推入库单
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(Arrays.asList(entity.getCode(),entity.getSourceCode()));
        PackingTaskEntity packingTaskEntity = CollectionUtils.isEmpty(packingTaskEntityList)?null:packingTaskEntityList.get(0);
        if (Objects.isNull(packingTaskEntity) || !PackingTaskStatusEnum.PACKED.getCode().equals(packingTaskEntity.getPackingStatus())) {
            throw new ServiceException(ApiError.NOT_PACKING_NOT_GENERATE_INBOUND);
        }

        // 查询关联目的仓
        String destWarehouseId = entity.getDestWarehouseId();
        if (StringUtils.isBlank(destWarehouseId)) {
            throw new ServiceException("目的仓信息为空");
        }
        // 所属平台:未绑定海外仓为空
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(destWarehouseId);
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();

        //物流信息
        OverseasWarehouseInboundDTO.ViewDTO viewDTO = FirstMileDeliveryConverter.INSTANCE.fmdToOverseasWarehouseInboundView(entity);
        viewDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        viewDTO.setInstockStatus(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode());
        viewDTO.setInstockStatusName(OverseasInstockStatusEnum.TO_BE_SHIPPED.getName());

        //查询头程物流单
        List<LogisticsBillEntity> tmsFirstMileLogisticEntities = tmsFirstMileLogisticFeign.listByOutstockIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(tmsFirstMileLogisticEntities)) {
            LogisticsBillEntity tmsFirstMileLogisticEntity = tmsFirstMileLogisticEntities.get(0);
            viewDTO.setLogisticsMethod(tmsFirstMileLogisticEntity.getShippingMethod());
            viewDTO.setTrackingNo(tmsFirstMileLogisticEntity.getTransportNo());
        }

        // 平台信息
        viewDTO.setDictPlatform(dictPlatform);
        OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(dictPlatform);
        viewDTO.setDictPlatformName(null == platformEnum ? "" : platformEnum.getName());

        //明细信息
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(id));
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
    }

    @Override
    public BatchResultDTO generatePackingTask(FirstMileDeliveryEntity entity) {
        packingTaskService.addPackingByFirstMileDelivery(entity);
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    @Override
    public FirstMileDeliveryEntity getByCode(String code) {
        if (StringUtils.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(FirstMileDeliveryEntity::getCode, code).last("limit 1").one();
    }

    @Override
    public FirstMileDeliveryEntity getBySourceCode(String code) {
        if (StringUtils.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(FirstMileDeliveryEntity::getSourceCode, code).last("limit 1").one();
    }

    @Override
    public List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecordByFbaCode(String fbaShipmentCode) {
        if (StringUtils.isBlank(fbaShipmentCode)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryRecord(null,fbaShipmentCode);
    }


    @Override
    public PagingVO<FirstMileDeliveryDTO.ListDTO> exportFbaDelivery(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        Page<FirstMileDeliveryDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public void exportPackingDetail(PackingTaskDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("发货单装箱清单导出", EXPORT_WMS_FIRST_MILE_PACKING_TASK_DETAIL.getCode(), dto);
    }

    @Override
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> firstMilePackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto) {

        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(dto.getParams().getIds())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        Page<WmsCartonDetailDTO.ListPackingDetailDTO> page = baseMapper.firstMilePackingTaskDetail(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams(),dto.getParams().getIds(), dto.getParams().getPermissionSql());
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //补充数据
        buildPackingDetailTask(page.getRecords());
        //切换为装箱清单导出
        buildPackingDetailExportTask(page.getRecords());
        return new PagingVO<>(page);
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
                if (CollectionUtil.isNotEmpty(deliveryEntityList)) {
                    throw new ServiceException(ApiError.BILL_IS_GENERATE_DECLARE, deliveryEntityList.get(0).getCode());
                }
                lambdaUpdate()
                        .set(FmDeliveryBillTypeEnum.DECLARE.getCode().equals(billType), FirstMileDeliveryEntity::getDeclareStatus, WmsDeclareStatusEnum.NONE.getCode())
                        .in(FirstMileDeliveryEntity::getId, dto.getIds())
                        .update();
            }

            if (FmDeliveryBillTypeEnum.LOGISTICS.getCode().equals(billType)) {
                if (CollectionUtil.isNotEmpty(deliveryEntityLogisticsStatusList)) {
                    throw new ServiceException(ApiError.BILL_IS_GENERATE_LOGISTICS, deliveryEntityLogisticsStatusList.get(0).getCode());
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
        List<String> ids = result.stream().map(FirstMileDeliveryDTO.GenerateLogisticDTO::getOutstockId).collect(Collectors.toList());
        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailList = baseMapper.listPackingDetail(ids);
        Map<String,List<WmsCartonDetailDTO.ListPackingDetailDTO>> packingDetailMap = packingDetailList.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId));
        //设置箱子明细信息
        result.forEach(v->{
            List<WmsCartonDetailDTO.ListPackingDetailDTO> list = packingDetailMap.get(v.getOutstockId());
            v.setPackingDTOList(list);
        });
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateStatus(FirstMileDeliveryDTO.UpdateStatusDTO dto) {
        if(StringUtils.isBlank(dto.getDeclareStatus()) && StringUtils.isBlank(dto.getLogisticsStatus())){
            return false;
        }
        return this.lambdaUpdate()
                .in(FirstMileDeliveryEntity :: getId,dto.getIds())
                .set(StringUtils.isNotBlank(dto.getLogisticsStatus()),FirstMileDeliveryEntity::getLogisticsStatus, dto.getLogisticsStatus())
                .set(StringUtils.isNotBlank(dto.getDeclareStatus()),FirstMileDeliveryEntity::getDeclareStatus,dto.getDeclareStatus())
                .update();

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
        List<String> ids = result.stream().map(TmsDeclareBillDTO.DeliveryDTO::getSourceId).collect(Collectors.toList());
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
            if(CollectionUtils.isNotEmpty(list)){
                List<TmsDeclareBillDTO.PackingDTO> packingDTOList = BeanUtil.copyToList(list,TmsDeclareBillDTO.PackingDTO.class);
                packingDTOList.forEach(t->t.setCode(deliveryDTO.getSourceCode()));
                deliveryDTO.setPackingDTOList(packingDTOList);
            }
            deliveryDTO.setBoxQty(list.size());
            deliveryDTO.setGrossWeight(list.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        //合并相同的sku
        for (TmsDeclareBillDTO.DeliveryDTO deliveryDTO : result) {
            List<TmsDeclareBillDTO.ProductDetail> productDetails = deliveryDTO.getProductDetailList();
            if(productDetails == null){
                productDetails = new ArrayList<>();
            }
            // 根据 skuId 进行分组，并对数量进行求和
            List<TmsDeclareBillDTO.ProductDetail> mergedDetails = new ArrayList<>(productDetails.stream()
                    .collect(Collectors.toMap(
                            TmsDeclareBillDTO.ProductDetail::getSkuId,
                            Function.identity(),
                            (existing, replacement) -> {
                                // 合并数量
                                existing.setQty(existing.getQty() + replacement.getQty());
                                // 其他字段取第一个出现的值
                                return existing;
                            }
                    ))
                    .values());
            mergedDetails.forEach(v->{
                if(Objects.nonNull(v.getPrice())){
                    v.setTotalPrice(v.getPrice().multiply(new BigDecimal(v.getQty())));
                }
            });
            deliveryDTO.setProductDetailList(mergedDetails);
        }
        return result;
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
                .eq(TransferInfoEntity::getSourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())
                .eq(TransferInfoEntity::getInvalidStatus, false));
        return !list.isEmpty();
    }

    /**
     * 生成直接调拨单：发货仓->优蓝子中转仓
     */
    private String generateTransferToUlanzi(FirstMileDeliveryEntity deliveryEntity, List<FirstMileDeliveryDetailEntity> deliveryDetailList) {
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(deliveryEntity.getDestWarehouseId(), deliveryEntity.getDeliveryWarehouseId()));

        //优蓝子中转仓
        CfgSettingValueDTO.TransitSettingDTO transitSettingDTO = getTransitSettingDTO();
        WarehouseEntity warehouseEntity = warehouseService.getOne(new LambdaQueryWrapper<WarehouseEntity>()
                .eq(WarehouseEntity::getId, transitSettingDTO.getWarehouseId())
                .eq(WarehouseEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()));
        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //来源类型
        addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_TO_ULANZI.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(warehouseEntity.getOrgId());
        //调出组织
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> req.getId().equals(deliveryEntity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(deliveryWarehouse.getOrgId());
        //调拨类型
        if (warehouseEntity.getOrgId().equals(deliveryWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(deliveryEntity.getId());
        String batchNo = IdUtil.getSnowflake().nextIdStr();
        addDTO.setSourceCode(deliveryEntity.getCode() + "_" + batchNo);
        addDTO.setBatchNo(batchNo);
        addDTO.setRemark(String.format("发货单【%s】审核通过自动创建", deliveryEntity.getCode()));

        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Arrays.asList(deliveryEntity.getId()));
        //要货申请单号
        String sourceCode = deliveryEntity.getSourceCode();
        RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationService.getOne(new LambdaQueryWrapper<RequisitionApplicationEntity>().eq(RequisitionApplicationEntity::getCode, sourceCode));
        //要货申请单明细
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetails = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(requisitionApplicationEntity.getId()));
        Map<String, String> requisitionApplicationMap = requisitionApplicationDetails.stream().collect(Collectors.toMap(item1 -> item1.getSkuId(), item2 -> item2.getToWarehouseId(), (item1, item2) -> item1));

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity deliveryDetail : deliveryDetailList) {
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            //映射产品信息
            detailAddDto.setSkuId(deliveryDetail.getSkuId());
            detailAddDto.setSkuNo(deliveryDetail.getSkuNo());
            detailAddDto.setQty(deliveryDetail.getDeliveryQty());
            detailAddDto.setOutWarehouseId(deliveryEntity.getDeliveryWarehouseId());
            //调入仓库ID
            String toWarehouseId = requisitionApplicationMap.get(deliveryDetail.getSkuId());
            if(requisitionApplicationEntity.getRequisitionWarehouseId().equals(toWarehouseId)){
                List<CfgRulePickingStagingEntity> stagingList = cfgRulePickingStagingService.list(new LambdaQueryWrapper<CfgRulePickingStagingEntity>().eq(CfgRulePickingStagingEntity::getWarehouseId, requisitionApplicationEntity.getRequisitionWarehouseId()).in(CfgRulePickingStagingEntity::getBillType, PickingBillTypeEnum.firstLegs()));
                if(stagingList.isEmpty()){
                    throw new ServiceException("没有找到暂存仓位");
                }
                detailAddDto.setOutWarehouseLocation(stagingList.get(0).getWarehouseLocation());
            }else {
                detailAddDto.setOutWarehouseLocation("");
            }
            detailAddDto.setInWarehouseId(warehouseEntity.getId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(deliveryDetail.getId());
            //如果是备货海外仓
            if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(deliveryEntity.getDemandType())) {
                //查询已下推的入库单获取入库单号
                OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntities.stream()
                        .filter(req -> req.getSourceId().equals(deliveryEntity.getId())
                                && !OverseasInstockStatusEnum.CANCELED.getCode().equals(req.getInstockStatus())
                        ).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(overseasWarehouseInboundEntity)) {
                    detailAddDto.setRemark(overseasWarehouseInboundEntity.getCode());
                }
            } else {
                detailAddDto.setRemark(deliveryDetail.getFbaShipmentCode());
            }

            detailAddDtoList.add(detailAddDto);
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.addAndApprove(addDTO);
    }

    /**
     * 获取中转仓配置
     */
    private CfgSettingValueDTO.TransitSettingDTO getTransitSettingDTO() {
        CfgSettingEntity cfgSetting = cfgSettingService.getByKey(CfgSettingEnum.TRANSIT_SETTING.getCode());
        if(Objects.isNull(cfgSetting)){
            throw new ServiceException("没有找到优蓝子中转仓配置");
        }
        CfgSettingValueDTO.TransitSettingDTO transitSettingDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.TransitSettingDTO.class);
        if (StrUtil.isBlank(transitSettingDTO.getWarehouseId())) {
            throw new ServiceException("中转设置仓库不能为空");
        }
        return transitSettingDTO;
    }

    /**
     * 生成直接调拨单：优蓝子中转仓->目的仓在途仓
     */
    private String generateTransferFromUlanzi(FirstMileDeliveryEntity entity, List<FirstMileDeliveryDetailEntity> detailEntityList) {
        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        WarehouseEntity destWarehouse = warehouseService.getById(entity.getDestWarehouseId());

        //校验目的仓是否为FBA第三方仓
        List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("warehouseType");
        DictBasicDTO.ListDTO listDTO = warehouseTypes.stream().filter(req -> "FBA".equals(req.getValue())).findFirst().orElse(null);
        //如果是FBA第三方仓
        if (listDTO.getId().equals(destWarehouse.getTypeId())) {

            //如果配置为空时默认为“FBA在途仓-xgwj-fba”
            if (StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Arrays.asList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WAREHOUSE_CODE_XGWJ_FBA_NOT_EXIST);
                }
                destWarehouse.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                destWarehouse.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }

        //目的仓在途仓
        WarehouseEntity destOnWayWarehouse = warehouseService.getById(destWarehouse.getOnwayWarehouseId());

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //来源类型：头程发货单
        addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY_FROM_ULANZI.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(destOnWayWarehouse.getOrgId());
        //蓝子中转仓
        CfgSettingEntity cfgSetting = cfgSettingService.getByKey(CfgSettingEnum.TRANSIT_SETTING.getCode());
        if(Objects.isNull(cfgSetting)){
            throw new ServiceException("没有找到优蓝子中转仓配置");
        }
        CfgSettingValueDTO.TransitSettingDTO transitSettingDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.TransitSettingDTO.class);
        if (StrUtil.isBlank(transitSettingDTO.getWarehouseId())) {
            throw new ServiceException("中转设置仓库不能为空");
        }
        JSONObject dataJson = cfgSetting.getDataJson();
        WarehouseEntity ulanziWarehouse = warehouseService.getOne(new LambdaQueryWrapper<WarehouseEntity>()
                .eq(WarehouseEntity::getId, transitSettingDTO.getWarehouseId())
                .eq(WarehouseEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()));
        addDTO.setOutOrgId(ulanziWarehouse.getOrgId());
        //调拨类型
        if (destOnWayWarehouse.getOrgId().equals(ulanziWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setRemark(String.format("发货单【%s】审核通过自动创建", entity.getCode()));

        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Arrays.asList(entity.getId()));

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity detailEntity : detailEntityList) {
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            //映射产品信息
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(detailEntity.getDeliveryQty());
            detailAddDto.setOutWarehouseId(ulanziWarehouse.getId());
            detailAddDto.setOutWarehouseLocation("");
            detailAddDto.setInWarehouseId(destOnWayWarehouse.getId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(detailEntity.getId());
            //如果是备货海外仓
            if (FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(entity.getDemandType())) {
                //查询已下推的入库单获取入库单号
                OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntities.stream()
                        .filter(req -> req.getSourceId().equals(entity.getId())
                                && !OverseasInstockStatusEnum.CANCELED.getCode().equals(req.getInstockStatus()))
                        .findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(overseasWarehouseInboundEntity)) {
                    detailAddDto.setRemark(overseasWarehouseInboundEntity.getCode());
                }
            } else {
                detailAddDto.setRemark((detailEntity.getFbaShipmentCode()));
            }
            detailAddDtoList.add(detailAddDto);
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.addAndApprove(addDTO);
    }

    /**
     * 查找符合要求的仓位
     * @param warehouseId 仓库ID
     * @param skuId skuId
     * @param deliveryQty 实发数量
     */
    private WarehouseLocationEntity searchWarehouseLocation(String warehouseId, String skuId, Integer deliveryQty, String areaType) {
        List<WarehouseLocationEntity> areaList = warehouseLocationService.list(new LambdaQueryWrapper<WarehouseLocationEntity>()
                .eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.AREA.getCode())
                .eq(WarehouseLocationEntity::getAreaType, areaType)
                .eq(WarehouseLocationEntity::getDisabled, false));
        List<String> areaIds = areaList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<WarehouseLocationEntity> locationList = warehouseLocationService.list(new LambdaQueryWrapper<WarehouseLocationEntity>()
                .in(WarehouseLocationEntity::getParentId, areaIds)
                .eq(WarehouseLocationEntity::getDisabled, false));
        List<String> locationCodes = locationList.stream().map(WarehouseLocationEntity::getCode).collect(Collectors.toList());
        if(locationCodes.isEmpty()){
            throw new ServiceException(ApiError.ERROR_GENERATE_TRANSFER);
        }
        List<InventoryEntity> list = inventoryService.list(new LambdaQueryWrapper<InventoryEntity>()
                .eq(InventoryEntity::getWarehouseId, warehouseId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                .ge(InventoryEntity::getQty, deliveryQty)
                .in(InventoryEntity::getWarehouseLocation, locationCodes));
        if(list.isEmpty()){
            throw new ServiceException(ApiError.ERROR_GENERATE_TRANSFER);
        }
        InventoryEntity inventoryEntity = list.get(0);
        WarehouseLocationEntity locationEntity = locationList.stream().filter(item -> item.getCode().equals(inventoryEntity.getWarehouseLocation())).findFirst().get();
        return locationEntity;
    }

    /**
     * 提交并审核直接调拨单
     */
    private void submitAndApprove(String transferId) {
        TransferInfoEntity entity = transferInfoService.getById(transferId);
        if (StringUtils.isBlank(transferId) || Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_GENERATE_TRANSFER_OUT);
        }
        //提交
        transferInfoService.submit(Arrays.asList(transferId), Boolean.FALSE);
        //审核
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Arrays.asList(transferId));
        baseApproveParamDTO.setType(ApproveType.PASS);
        transferInfoService.approve(entity,ApproveType.PASS,"", null, Boolean.TRUE, Boolean.FALSE);
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

    private void buildPackingDetailExportTask(List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS) {
        List<String> taskIds = listPackingDetailDTOS.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getTaskId).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> taskEntityList = packingTaskService.listByIds(taskIds);
        Map<String, PackingTaskEntity> taskMap = taskEntityList.stream().collect(Collectors.toMap(PackingTaskEntity::getId, Function.identity()));
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = packingTaskService.selectPackingStatusByIds(taskIds, null);
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        listPackingDetailDTOS.forEach(pagingViewDTO -> {
            PackingTaskEntity packingTaskEntity = taskMap.get(pagingViewDTO.getTaskId());
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getTaskId());
            pagingViewDTO.setTaskCode(packingTaskEntity.getCode());
            pagingViewDTO.setSourceCode(packingTaskEntity.getSourceCode());
            pagingViewDTO.setSourceType(packingTaskEntity.getSourceType());
            pagingViewDTO.setSourceTypeName(PickingSourceTypeEnum.getName(packingTaskEntity.getSourceType()));
            if (Objects.nonNull(statusDTO)){
                String packingStatus = com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingTotalStatus(packingStatus);
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingTotalStatus(weightingStatus);
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
                pagingViewDTO.setPackageWeightStr(pagingViewDTO.getPackageWeight().toPlainString() + UnitEnum.WeightUnitEnum.KG.getName());
            }else {
                pagingViewDTO.setPackingTotalStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingTotalStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.UNWEIGHED.getName());
                pagingViewDTO.setPackageWeight(BigDecimal.ZERO);
                pagingViewDTO.setPackageWeightStr("0" + UnitEnum.WeightUnitEnum.KG.getName());
            }
        });
    }
}

