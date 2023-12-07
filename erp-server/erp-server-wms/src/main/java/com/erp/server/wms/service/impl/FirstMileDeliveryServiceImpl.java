package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.ApproveType;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.wms.convert.FirstMileDeliveryConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.FirstMileDeliveryMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private FirstMileDeliveryLogisticsService firstMileDeliveryLogisticsService;
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
    private LogisticsBillFeign logisticsBillFeign;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private TransferInfoService transferInfoService;
    @Autowired
    private OmsListingInfoFeign omsListingInfoFeign;
    @Autowired
    private SkuMappingFeign skuMappingFeign;
    @Autowired
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Autowired
    private FirstMileCartonService firstMileCartonService;
    @Autowired
    private FirstMileCartonBillService firstMileCartonBillService;
    @Autowired
    private FirstMileCartonDetailService firstMileCartonDetailService;
    @Autowired
    private OverseasDeliveryPlanService overseasDeliveryPlanService;
    @Autowired
    private DictBasicService dictBasicService;
    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "发货单" , firstMileDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), firstMileDeliveryEntity.getId(), "新增操作");

        //新增物流信息
        addDTO.getLogisticsView().setMainId(firstMileDeliveryEntity.getId());
        addDTO.getLogisticsView().setDeliveryCode(code);
        firstMileDeliveryLogisticsService.add(addDTO.getLogisticsView());

        //新增详情信息
        firstMileDeliveryDetailService.add(addDTO, firstMileDeliveryEntity.getId());
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

        //修改物流信息
        updateDTO.getLogisticsView().setMainId(firstMileDeliveryEntity.getId());
        firstMileDeliveryLogisticsService.update(updateDTO.getLogisticsView());

        //修改明细数据
        firstMileDeliveryDetailService.update(updateDTO, firstMileDeliveryEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录发货单日志数据，单号：【{}】", firstMileDeliveryEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), firstMileDeliveryEntity.getCode(), "发货单");
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
    public void exportList(FirstMileDeliveryDTO.ExportDTO param, HttpServletResponse response) {
        List<FirstMileDeliveryDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/fbaDelivery.xlsx";
        String name = "发货单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
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
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单");
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
        //包含组合产品的发货单，必须有关联的下推的加工组装单且加工单审核通过，否则提示：发货单【发货单号】包含组合产品，请先下推加工单并且审核通过后重试
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(entity.getId()));
        List<FirstMileDeliveryDetailEntity> isCombinationList = detailEntityList.stream().filter(req -> req.getIsCombination()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(isCombinationList)) {
            //查询是否包含审核同步的加工单
            List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(Arrays.asList(entity.getId()));
            List<MachineInfoEntity> approveMachineInfoEntityList = machineInfoEntityList.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(approveMachineInfoEntityList)) {
                throw new ServiceException(ApiError.IS_GENERATE_MACHINE, entity.getCode());
            }

            List<String> skuNos = isCombinationList.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);

            //校验组合SKU库存量是否满足调出，否则无法审核通过，提示：SKU【SKU编码】【发货仓】可用库存不足，无法审核发货单
            for (FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity : isCombinationList) {
                //及时库存
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
                Integer usableInventoryTotal = inventoryService.getUsableInventoryTotal(entity.getDeliveryWarehouseId(), skuVO.getSkuId(), firstMileDeliveryDetailEntity.getWarehouseLocation());
                if (firstMileDeliveryDetailEntity.getDeliveryQty() > usableInventoryTotal) {
                    throw new ServiceException(ApiError.FBA_DELIVERY_INVENTORY_INSUFFICIENT, firstMileDeliveryDetailEntity.getSkuNo(), entity.getDeliveryWarehouseName());
                }
            }
        }

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单", approveType.getName(), dto.getComment());
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
        //默认来源类型：FBA货件
        addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
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
        addDTO.setSourceId(entity.getSourceId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setRemark(String.format("发货单【%s】审核通过自动创建", entity.getCode()));

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
            detailAddDtoList.add(detailAddDto);
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(FirstMileDeliveryEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
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
        if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(entity.getSourceType())) {
            FbaShipmentEntity shipmentEntity = fbaShipmentService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(shipmentEntity)) {
                fbaShipmentService.deliveryDisApprove(entity);
            }
        }

        //如果是发货计划来源，反审核修改发货状态
        if (SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode().equals(entity.getSourceType())) {
            OverseasDeliveryPlanEntity planEntity = overseasDeliveryPlanService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(planEntity)) {
                //如果存在有一个审核通过的发货单，状态都是已发货
                List<FirstMileDeliveryEntity> firstMileDeliveryEntities = this.listBySourceIds(Arrays.asList(entity.getSourceId()));
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(deliveryEntities)) {
                    //如果没有审核通过否发货单，修改发货计划单的发货状态为未发货
                    overseasDeliveryPlanService.updateDeliveryStatus(Arrays.asList(entity.getSourceId()), FbaDeliveryStatusEnum.UN_SHIPPED.getCode());
                }
            }
        }

        //查找发货单下推的分步式调出单自动反审并删除
        List<TransferInfoEntity> transferInfoEntities = transferInfoService.listBySourceIds(Arrays.asList(id));
        //分步式调出单已审核先反审核
        List<String> approveTransferOutIds = transferInfoEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(approveTransferOutIds)) {
            transferInfoService.disApprove(approveTransferOutIds, Boolean.TRUE);
        }
        //分步式调出单审核中先撤销
        List<String> approveIngTransferOutIds = transferInfoEntities.stream().filter(req -> ApproveStatusEnum.APPROVE_ING.getStatus().equals(req.getApproveStatus())).map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(approveIngTransferOutIds)) {
            transferInfoService.cancelProcess(approveIngTransferOutIds);
        }
        //分步式调出单删除
        List<String> deletedTransferOutIds = transferInfoEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deletedTransferOutIds)) {
            transferInfoService.delete(deletedTransferOutIds);
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单");
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
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        FirstMileDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1043);
        }
        // 删除物流信息
        firstMileDeliveryLogisticsService.removeByMainIds(Arrays.asList(id));
        // 删除明细数据
        firstMileDeliveryDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除发货单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getCode(), "删除发货单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        FirstMileDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改发货单状态数据，id：【{}】", id);
        lambdaUpdate().eq(FirstMileDeliveryEntity::getId, id)
            .set(FirstMileDeliveryEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(FirstMileDeliveryEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单", remark);
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
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        revokeDTO.setUserId(commonService.getUserInfo().getUid());
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

            //如果是FBA货件来源，审核通过修改货件发货状态为已发货
            if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(entity.getSourceType())) {
                FbaShipmentEntity shipmentEntity = fbaShipmentService.getById(entity.getSourceId());
                if (ObjectUtil.isNotEmpty(shipmentEntity)) {
                    fbaShipmentService.deliveryStatus(entity);
                }
            }
            //如果是发货计划来源
            if (SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode().equals(entity.getSourceType())) {
                //审核通过修改发货状态为已发货
                OverseasDeliveryPlanEntity planEntity = overseasDeliveryPlanService.getById(entity.getSourceId());
                if (ObjectUtil.isNotEmpty(planEntity)) {
                    overseasDeliveryPlanService.updateDeliveryStatus(Arrays.asList(entity.getSourceId()), FbaDeliveryStatusEnum.SHIPPED.getCode());
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

                //有对接海外仓API：调用入库单的提交审核，获取审核结果，审核通过后入库单状态为待签收；审核不通过为异常，操作日志记录失败原因，并显示在备注栏
                if (CollectionUtils.isNotEmpty(overseasProviderWarehouseEntities)) {

                    // 查询发货目的仓平台
                    OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDestWarehouseId());

                    // 推送第三方发货单审核通过
                    ApiResult<String> resultInfo = overseasWarehouseInboundService.pullThirdOverseasPlatform(providerEntity, inboundEntity, detailEntityList, OverseasVerifyEnum.PASS.getCode());
                    if (200 != resultInfo.getCode()) {
                        log.error("推送第三方仓库【发货单审核通过】失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                        throw new ServiceException("推送第三方仓库【发货单审核通过】失败:" + resultInfo.getMsg());
                    }
                    log.info("推送第三方仓库【发货单审核通过】结果: ={}", JSONUtil.toJsonStr(resultInfo));

                    String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据推送第三方仓库发货审核操作 平台返回结果：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "发货单", JSONUtil.toJsonStr(resultInfo));
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "发货单审核");


                }

                //入库单状态修改为待签收
                overseasWarehouseInboundService.updateInstockStatus(Arrays.asList(inboundEntity.getId()), OverseasInstockStatusEnum.TO_BE_SIGNED.getCode());
            }

            //新增直接调拨单
            String transferOutId = generateTransferOut(entity, detailEntityList);
            if (StringUtils.isNotBlank(transferOutId)) {
                //提交
                transferInfoService.submit(Arrays.asList(transferOutId));
                //审核
                BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
                baseApproveParamDTO.setIds(Arrays.asList(transferOutId));
                baseApproveParamDTO.setType(ApproveType.PASS);
                transferInfoService.approve(baseApproveParamDTO, Boolean.TRUE);
            } else {
                throw new ServiceException(ApiError.ERROR_GENERATE_TRANSFER_OUT);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public FirstMileDeliveryDTO.ViewDTO view(String id) {
        //发货单主信息
        FirstMileDeliveryEntity firstMileDeliveryEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到发货单数据"));
        FirstMileDeliveryDTO.ViewDTO data = BeanMapperUtils.map(FirstMileDeliveryDTO.ViewDTO.class, firstMileDeliveryEntity);
        //物流信息
        FirstMileDeliveryLogisticsEntity firstMileDeliveryLogisticsEntity = firstMileDeliveryLogisticsService.listByMainId(id);
        //发货单详情
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, firstMileDeliveryLogisticsEntity, detailEntityList);
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
        startDTO.setUserId(commonService.getUserInfo().getUid());
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
     * @param logisticsEntity 物流信息
     * @param detailEntityList 产品详情信息
     * @return void
     **/
    private void fillOne(FirstMileDeliveryDTO.ViewDTO data, FirstMileDeliveryLogisticsEntity logisticsEntity, List<FirstMileDeliveryDetailEntity> detailEntityList) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

        //获取sku信息
        List<String> skuNoList = detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);

        //根据仓库信息获取核算公司
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(data.getInventoryOrgId()));

        //根据id查询物流单信息
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillFeign.listLogisticsBillVoBySourceIds(Arrays.asList(data.getId()));

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
        FirstMileDeliveryLogisticsDTO.ViewDTO logisticsViewDTO = new FirstMileDeliveryLogisticsDTO.ViewDTO();
        BeanMapper.copy(logisticsEntity, logisticsViewDTO);

        //物流方式名称
        logisticsViewDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(logisticsEntity.getLogisticsMethod()));
        logisticsViewDTO.setLogisticsRemark(logisticsEntity.getRemark());
        List<String> trackNoList = logisticsBillVos.stream().filter(req -> req.getSourceId().equals(data.getId())).map(req -> req.getTrackNo()).collect(Collectors.toList());
        logisticsViewDTO.setTrackingNoList(trackNoList);
        data.setLogisticsView(logisticsViewDTO);

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
//        List<SkuMappingDTO.listStockSkuNoByProductSkuNoView> listStockSkuNoByProductSkuNoViews = omsListingInfoFeign.listBySkuNoList(skuNoList);
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
        }
        data.setDetailList(detailViews);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
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
        List<FirstMileDeliveryDetailEntity> entityList = entities.stream().filter(req -> Boolean.TRUE.equals(req.getIsCombination())).collect(Collectors.toList());
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
            List<Integer> bomVersionList = bomVersionObj.getBomVersionList().stream().map(req -> Integer.valueOf(req)).collect(Collectors.toList());
            Integer bomVersion = Collections.max(bomVersionList);
            view.setBomVersion(String.valueOf(bomVersion));
            List<FirstMileDeliveryDTO.SonItem> sonItemList = new ArrayList<>();

            //查询最新版本的sku子件信息
            List<BomChildrenSkuDTO> bomSonItemList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(view.getSkuId()) && req.getBomVersion().equals(String.valueOf(bomVersion))).collect(Collectors.toList());
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
        }
        return viewList;
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
    public Boolean fbaDeliveryGenerateMachineSubmitAndApprove(List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        List<String> ids = fbaDeliveryGenerateMachine(list);
        //提审
        Boolean submit = machineInfoService.submit(ids);
        //审核
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(ids);
        baseApproveParamDTO.setType(ApproveType.PASS);
        machineInfoService.approve(baseApproveParamDTO);
        return submit;
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
    public List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecordBySourceIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryRecordBySourceIds(ids);
    }

    @Override
    public List<FirstMileDeliveryEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileDeliveryEntity::getSourceId, sourceIds).list();
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
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //根据单据id查询审核流程
        List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(ids);

        //获取库存sku信息
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> ListStockSkuNoByProductSkuIdViews = omsListingInfoFeign.listStockSkuNoByProductSkuIds(skuIdList);

        //查询已下推的海外入库单
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(ids);

        // 属性赋值
        for(FirstMileDeliveryDTO.ListDTO data : list) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(data.getSkuNo())).findFirst().orElse(new SkuVO());

            //库存sku
            String stockSku = ListStockSkuNoByProductSkuIdViews.stream()
                    .filter(req -> req.getProductSkuId().equals(data.getSkuId())
                            && req.getWarehouseId().equals(data.getDeliveryWarehouseId()))
                    .distinct()
                    .findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getStockSku())).orElse("");
            data.setStockSku(stockSku);

            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            //备货类型名称
            data.setDemandTypeName(FbaDemandTypeEnum.getName(data.getDemandType()));
            //作废状态名称
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //装箱状态名称
            data.setPackingStatusName(PackingStatusEnum.getName(data.getPackingStatus()));
            //物流方式名称
            data.setLogisticsMethodName(LogisticsMethodEnum.getName(data.getLogisticsMethod()));
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean packingSave(FirstMileDeliveryDTO.FirstMileCartonAdd dto) {
        //待审核的数据可以上传装箱数据
        FirstMileDeliveryEntity entity = this.getById(dto.getId());
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.APPROVE_ING_IS_PACKING);
        }

        //已装箱的数据，如果未下推入库单，或者下推的入库单待提交时，可以再次修改装箱信息，否则提示：已下推海外仓入库单【单号】，不允许修改装箱数据（装箱页面保存时校验）
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Arrays.asList(dto.getId()));
        long count = overseasWarehouseInboundEntities.stream()
                .filter(req -> !req.getInstockStatus().equals(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode())
                        && !req.getInstockStatus().equals(OverseasInstockStatusEnum.CANCELED.getCode())
                ).count();
        if (count > 0) {
            List<String> codes = overseasWarehouseInboundEntities.stream().map(req -> req.getCode()).distinct().collect(Collectors.toList());
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST_NOT_UPDATE, StringUtils.join(codes, ","));
        }

        //删除原装箱信息
        deleteCarton(dto.getId());

        //新增装箱信息
        for (FirstMileCartonDTO.AddDTO addDTO : dto.getFirstMileCartonList()) {

            //校验必填
            for (FirstMileCartonDetailDTO.AddDTO detail : addDTO.getDetailList()) {
                if (StringUtils.isBlank(detail.getSkuId()) || StringUtils.isBlank(detail.getSkuNo())) {
                    throw new ServiceException(ApiError.PACKING_SKU_IS_NOT_NULL, addDTO.getBoxSpecNo());
                }
                if (detail.getPackQty() == null || detail.getPackQty() <= 0) {
                    throw new ServiceException(ApiError.PACKING_SKU_PACK_QTY_IS_NOT_NULL, addDTO.getBoxSpecNo(), detail.getSkuNo());
                }
                if (addDTO.getBoxQty() == null || addDTO.getBoxQty() <= 0) {
                    throw new ServiceException(ApiError.PACKING_SKU_BOX_QTY_IS_NOT_NULL, addDTO.getBoxSpecNo());
                }
            }

            //新增装箱信息
            firstMileCartonService.add(addDTO, dto.getId());
        }
        //根据主表id分组sku查询发货及待装箱数
        List<FirstMileDeliveryDTO.PackDateDTO> packDateDTOS = firstMileDeliveryDetailService.listPackDate(dto.getId());

        for (FirstMileDeliveryDTO.PackDateDTO packDateDTO : packDateDTOS) {
            //待装箱数量=发货数量-所有已装箱数量
            int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(req -> req.getBoxQty() * req.getPackQty()).sum();
            if (packDateDTO.getDeliveryQty() < packQtySum) {
                throw new ServiceException(ApiError.PACKING_QTY_NOT_GT_WAIT_PACKING_QTY, packDateDTO.getBoxSpecNo(), packDateDTO.getSkuNo());
            }
        }

        //根据主表id分组sku查询发货及待装箱数
        List<FirstMileDeliveryDTO.GroupSkuDTO> groupSkuList = firstMileDeliveryDetailService.listGroupSkuByMainId(dto.getId());

        //当所有产品待装箱数量为0时，状态自动变更为已装箱
        List<FirstMileDeliveryDTO.GroupSkuDTO> groupSkuDTOList = groupSkuList.stream().filter(req -> req.getWaitPackQty() > 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupSkuDTOList)) {
            updatePackingStatus(dto.getId(), PackingStatusEnum.PACKING.getCode());
        } else {
            updatePackingStatus(dto.getId(), PackingStatusEnum.NOT_PACKING.getCode());
        }
        return Boolean.TRUE;
    }

    @Override
    public FirstMileDeliveryDTO.FirstMileCartonView packingView(String id) {
        FirstMileDeliveryEntity entity = this.getById(id);
        FirstMileDeliveryDTO.FirstMileCartonView view = new FirstMileDeliveryDTO.FirstMileCartonView();
        view.setId(entity.getId());
        view.setCode(entity.getCode());

        //已下推入库单，不允许修改装箱信息
        OverseasWarehouseInboundEntity overseasWarehouseInbound = overseasWarehouseInboundService.getBySourceId(entity.getId(),OverseasInstockStatusEnum.CANCELED.getCode());
        if (ObjectUtil.isNotEmpty(overseasWarehouseInbound)) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST, overseasWarehouseInbound.getCode());
        }

        //查询箱规信息
        List<FirstMileCartonEntity> firstMileCartonEntities = firstMileCartonService.listByMainIds(Arrays.asList(id));
        List<FirstMileCartonDTO.ViewDTO> firstMileCartonList = BeanMapper.copyList(firstMileCartonEntities, FirstMileCartonDTO.ViewDTO.class);
        view.setFirstMileCartonList(firstMileCartonList);

        //查询箱规包含的产品信息
        for (FirstMileCartonDTO.ViewDTO viewDTO : firstMileCartonList) {

            //根据主表id分组sku查询发货及待装箱数
            List<FirstMileDeliveryDTO.PackDateDTO> packDateDTOS = firstMileDeliveryDetailService.listPackDate(id);
            List<FirstMileDeliveryDTO.PackDateDTO> packDateDTOList = packDateDTOS.stream().filter(req -> req.getBoxSpecNo().equals(viewDTO.getBoxSpecNo())).collect(Collectors.toList());

            //查询产品信息
            List<String> skuIdList = packDateDTOList.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

            List<FirstMileCartonDetailDTO.ViewDTO> detailList = BeanMapper.copyList(packDateDTOList, FirstMileCartonDetailDTO.ViewDTO.class);
            for (FirstMileCartonDetailDTO.ViewDTO dto : detailList) {
                //待装箱数量=发货数量-所有已装箱数量
                int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).mapToInt(req -> req.getBoxQty() * req.getPackQty()).sum();
                dto.setWaitPackQty(dto.getDeliveryQty() - packQtySum);

                //匹配产品信息，设置中文名
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(skuVO)) {
                    dto.setProductName(skuVO.getSkuName());
                }
            }
            viewDTO.setDetailList(detailList);
        }
        return view;
    }

    @Override
    public FirstMileCartonDTO.ListPackingDTO listPacking(String id) {
        FirstMileDeliveryEntity entity = this.getById(id);
/*        if (PackingStatusEnum.NOT_PACKING.getCode().equals(entity.getPackingStatus())) {
            throw new ServiceException(ApiError.NOT_PACKING_NOT_EXPORT);
        }*/
        FirstMileCartonDTO.ListPackingDTO listPackingDTO = new FirstMileCartonDTO.ListPackingDTO();
        listPackingDTO.setId(entity.getId());
        listPackingDTO.setCode(entity.getCode());

        //获取总箱数
        List<FirstMileCartonEntity> firstMileCartonEntities = firstMileCartonService.listByMainIds(Arrays.asList(id));
        int boxQty = firstMileCartonEntities.stream().mapToInt(FirstMileCartonEntity::getBoxQty).sum();
        listPackingDTO.setBoxQty(boxQty);

        //箱子明细信息
        List<FirstMileCartonDetailDTO.ListPackingDetailDTO> detailList = baseMapper.listPackingDetail(id);
        listPackingDTO.setDetailList(detailList);
        return listPackingDTO;
    }

    @Override
    public void exportPacking(FirstMileDeliveryDTO.ExportDTO dto, HttpServletResponse response) {
        List<FirstMileCartonDTO.ExportPackingDTO> list = baseMapper.exportPacking(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }

        //只有已装箱的发货单可以查看/导出装箱数据
        long count = list.stream().filter(req -> PackingStatusEnum.NOT_PACKING.getCode().equals(req.getPackingStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.NOT_PACKING_NOT_EXPORT);
        }

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/packingExport.xlsx";
        String name = "装箱清单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
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
        if (PackingStatusEnum.NOT_PACKING.getCode().equals(entity.getPackingStatus())) {
            throw new ServiceException(ApiError.NOT_PACKING_NOT_GENERATE_INBOUND, entity.getCode());
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
        FirstMileDeliveryLogisticsEntity logisticsEntity = firstMileDeliveryLogisticsService.listByMainId(id);
        OverseasWarehouseInboundDTO.ViewDTO viewDTO = FirstMileDeliveryConverter.INSTANCE.fmdToOverseasWarehouseInboundView(entity, logisticsEntity);
        viewDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        viewDTO.setTrackingNo(StringUtils.join(logisticsEntity.getTrackingNoList(), ","));
        viewDTO.setInstockStatus(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode());
        viewDTO.setInstockStatusName(OverseasInstockStatusEnum.TO_BE_SHIPPED.getName());
        // 平台信息
        viewDTO.setDictPlatform(dictPlatform);
        OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(dictPlatform);
        viewDTO.setDictPlatformName(null == platformEnum ? "" : platformEnum.getName());

        //明细信息
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(id));
        List<String> skuIdList = firstMileDeliveryDetailEntities.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取库存sku信息
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> ListStockSkuNoByProductSkuIdViews = omsListingInfoFeign.listStockSkuNoByProductSkuIds(skuIdList);
        List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailViewList = FirstMileDeliveryConverter.INSTANCE.fmdToOverseasWarehouseInboundDetailView(firstMileDeliveryDetailEntities);

        //查询已装箱信息
        List<FirstMileDeliveryDTO.PackDateDTO> packDateDTOList = firstMileDeliveryDetailService.listPackDate(entity.getId());

        for (OverseasWarehouseInboundDetailDTO.ViewDTO dto : detailViewList) {
            //装箱数量
            int packQty = packDateDTOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).mapToInt(req -> req.getPackQty() * req.getBoxQty()).sum();
            dto.setPackQty(packQty);

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).findFirst().orElse(new SkuVO());
            dto.setProductName(skuVO.getSkuName());
            dto.setImagesUrl(skuVO.getSkuImagesUrl());

            //库存sku
            SkuMappingDTO.ListStockSkuNoByProductSkuIdView view = ListStockSkuNoByProductSkuIdViews.stream()
                    .filter(req -> req.getProductSkuId().equals(dto.getSkuId())
                            && req.getWarehouseId().equals(viewDTO.getToWarehouseId()))
                    .distinct()
                    .findFirst().orElse(new SkuMappingDTO.ListStockSkuNoByProductSkuIdView());
            dto.setPlatformSkuNo(view.getStockSku());
            dto.setPlatformProductName(view.getStockSkuName());
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
     * 删除原装箱信息
     * @param id
     */
    private void deleteCarton(String id) {
        List<FirstMileCartonEntity> firstMileCartonEntities = firstMileCartonService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(firstMileCartonEntities)) {
            //删除箱子明细信息
            firstMileCartonBillService.deleteByMainIds(Arrays.asList(id));
            //删除原箱包装信息
            firstMileCartonDetailService.deleteByMainIds(Arrays.asList(id));
            //删除原箱信息
            firstMileCartonService.deleteByMainIds(Arrays.asList(id));
        }
    }

    /**
     * 修改装箱状态
     * @Author Luo_WG
     * @Date 2023/12/4 16:17
     * @param id 发货单id
     * @param packingStatus 发货状态
     * @return void
     **/
    private void updatePackingStatus(String id, String packingStatus){
        lambdaUpdate().set(FirstMileDeliveryEntity::getPackingStatus, packingStatus)
                .eq(FirstMileDeliveryEntity::getId, id)
                .update();
    }
}
