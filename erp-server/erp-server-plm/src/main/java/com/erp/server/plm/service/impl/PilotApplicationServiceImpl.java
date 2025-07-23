package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
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
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.PurchaseSkuOrgRefEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.tms.enums.PilotApplicationTabEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.mapper.PilotApplicationMapper;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PILOT_APPLICATION;

/**
 * <p>
 * 试产申请 服务实现类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
@Slf4j
@Service
public class PilotApplicationServiceImpl extends SuperServiceImpl<PilotApplicationMapper, PilotApplicationEntity> implements PilotApplicationService {
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private PilotApplicationDetailService pilotApplicationDetailService;
    @Resource
    private PilotApplicationRefTaskService pilotApplicationRefTaskService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private SupplierFeign supplierFeign;
    @Resource
    private PurchaseApplicationFeign purchaseApplicationFeign;
    @Resource
    private ProjectTaskService projectTaskService;
    @Resource
    private WmsTaskFeign warehouseFeign;
    @Resource
    private SysLogService sysLogService;
    @Resource
    private ProductDetailMapper productDetailMapper;
    @Autowired
    private BomSkuService bomSkuService;
    @Resource
    private ProductCostService productCostService;
    @Resource
    private PlmAttachmentService plmAttachmentService;
    @Resource
    private ProcessTaskManagementFeign processTaskManagementFeign;
    @Resource
    private PurchasePriceDetailFeign purchasePriceDetailFeign;
    @Resource
    private PurchaseApplicationDetailFeign purchaseApplicationDetailFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private NoticeMessageService noticeMessageService;
    @Resource
    private ProductPurchaseService productPurchaseService;
    @Resource
    private ProductPackService productPackService;
    @Resource
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;


    private static final String SKUCLASSPATH = String.valueOf(PilotApplicationEntity.class);

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PilotApplicationDTO.AddDTO addDTO) {
        PilotApplicationEntity pilotApplicationEntity = new PilotApplicationEntity();

        // 数据处理
        handleData(addDTO, pilotApplicationEntity);

        log.info("开始新增试产申请");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SCLC);
        pilotApplicationEntity.setCode(code);
        boolean save = super.save(pilotApplicationEntity);
        if (!save) {
            throw new ServiceException("试产申请保存失败");
        }
        //保存附件
        List<PilotApplicationDTO.AttachmentDTO> attachmentList = addDTO.getAttachmentList();
        for (PilotApplicationDTO.AttachmentDTO attachmentDTO : attachmentList) {
            PlmAttachmentEntity entity = new PlmAttachmentEntity();
            entity.setAttachName(attachmentDTO.getAttachName());
            entity.setAttachUrl(attachmentDTO.getAttachUrl());
            entity.setType("pilot_application");
            entity.setBusinessId(pilotApplicationEntity.getId());
            plmAttachmentService.save(entity);
        }
        String format = String.format("用户【%s】新增【试产量产单】单据编号为【%s】", UserContext.getNonLoginUser().getUserName(), code);
        sysLogService.addSysLogBySave(format, "", pilotApplicationEntity.getId(), "");

        //保存产品明细
        saveProductDetail(addDTO, pilotApplicationEntity);

        //保存关联任务
        if (!addDTO.getTaskList().isEmpty()) {
            List<PilotApplicationRefTaskEntity> taskEntityList = new ArrayList<>(addDTO.getTaskList().size());
            addDTO.getTaskList().forEach(task -> taskEntityList.add(new PilotApplicationRefTaskEntity().setMainId(pilotApplicationEntity.getId()).setTaskId(task.getTaskId())));
            boolean saveTask = pilotApplicationRefTaskService.saveBatch(taskEntityList);
            if (!saveTask) {
                throw new ServiceException("关联任务保存失败");
            }
        }

        return new BaseResultDTO.AddDTO(pilotApplicationEntity.getId(), code);
    }

    /**
     * 保存产品明细
     */
    private void saveProductDetail(PilotApplicationDTO.AddDTO addDTO, PilotApplicationEntity pilotApplicationEntity) {
        if (addDTO.getProductDetailList().isEmpty()) {
            throw new ServiceException("产品明细不能为空");
        }
        for (PilotApplicationDetailDTO.AddDTO detailDTO : addDTO.getProductDetailList()) {
            if (StringUtils.isBlank(detailDTO.getMainSupplierId())) {
                throw new ServiceException("一级供应商不能为空");
            }
            //实际含税单价
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceSearchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            priceSearchDTO.setSkuId(detailDTO.getSkuId());
            priceSearchDTO.setSkuNo(detailDTO.getSkuNo());
            priceSearchDTO.setSupplierId(detailDTO.getMainSupplierId());
            priceSearchDTO.setPurchaseQty(detailDTO.getApplyQty());
            try {
                List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailFeign.getTaxPrice(priceSearchDTO);
                for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                    if (detailDTO.getApproveQty() >= priceViewDTO.getMinQty() && detailDTO.getApproveQty() <= priceViewDTO.getMaxQty()) {
                        detailDTO.setActualTaxCost(priceViewDTO.getTaxPrice());
                        break;
                    }
                }
            } catch (Exception e) {
                detailDTO.setActualTaxCost(BigDecimal.ZERO);
            }
            if (detailDTO.getActualTaxCost() == null) {
                detailDTO.setActualTaxCost(BigDecimal.ZERO);
            }

            detailDTO.setMainId(pilotApplicationEntity.getId());
        }
        List<PilotApplicationDetailEntity> entityList = BeanMapper.copyList(addDTO.getProductDetailList(), PilotApplicationDetailEntity.class);
        pilotApplicationDetailService.saveBatch(entityList);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PilotApplicationDTO.UpdateDTO updateDTO) {
        PilotApplicationEntity old = super.getById(updateDTO.getId());
        PilotApplicationEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "试产申请"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(oldEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        // 数据处理
        PilotApplicationEntity pilotApplicationEntity = new PilotApplicationEntity();
        pilotApplicationEntity.setId(updateDTO.getId());
        pilotApplicationEntity.setRemark(updateDTO.getRemark());

        log.info("编辑 开始修改试产申请数据，单号：【{}】", oldEntity.getCode());
        boolean save = super.updateById(pilotApplicationEntity);
        if (!save) {
            throw new ServiceException("试产申请保存失败");
        }
        //保存附件
        if (!updateDTO.getAttachmentList().isEmpty()) {
            List<PilotApplicationDTO.AttachmentDTO> attachmentList = updateDTO.getAttachmentList();
            for (PilotApplicationDTO.AttachmentDTO attachmentDTO : attachmentList) {
                PlmAttachmentEntity entity = new PlmAttachmentEntity();
                entity.setAttachName(attachmentDTO.getAttachName());
                entity.setAttachUrl(attachmentDTO.getAttachUrl());
                entity.setType("pilot_application");
                entity.setBusinessId(updateDTO.getId());
                plmAttachmentService.save(entity);
            }
        }

        //保存产品明细
        if (updateDTO.getProductDetailList().isEmpty()) {
            throw new ServiceException("产品明细不能为空");
        }
        if (!StringUtils.isBlank(pilotApplicationEntity.getId())) {
            updateDTO.getProductDetailList().forEach(item -> item.setMainId(pilotApplicationEntity.getId()));
        }
        List<String> ids = updateDTO.getProductDetailList().stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        pilotApplicationDetailService.lambdaUpdate().eq(PilotApplicationDetailEntity::getMainId, pilotApplicationEntity.getId()).notIn(PilotApplicationDetailEntity::getId, ids).remove();
        List<PilotApplicationDetailEntity> entityList = BeanMapper.copyList(updateDTO.getProductDetailList(), PilotApplicationDetailEntity.class);
        pilotApplicationDetailService.saveOrUpdateBatch(entityList);

        //保存关联任务
        pilotApplicationRefTaskService.lambdaUpdate().eq(PilotApplicationRefTaskEntity::getMainId, pilotApplicationEntity.getId()).remove();
        if (!updateDTO.getTaskList().isEmpty()) {
            List<PilotApplicationRefTaskEntity> taskEntityList = new ArrayList<>(updateDTO.getTaskList().size());
            updateDTO.getTaskList().forEach(task -> taskEntityList.add(new PilotApplicationRefTaskEntity().setMainId(pilotApplicationEntity.getId()).setTaskId(task.getTaskId())));
            boolean saveTask = pilotApplicationRefTaskService.saveBatch(taskEntityList);
            if (!saveTask) {
                throw new ServiceException("关联任务保存失败");
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PilotApplicationDTO.ListDTO> paging(PagingDTO<PilotApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<PilotApplicationDTO.PagingParamDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PilotApplicationDTO.ListDTO> pageData = this.baseMapper.pagingByParam(query, pagingParamDTO.getParams());
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<PilotApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        LoginUser user = UserContext.getNonLoginUser();
        List<PilotApplicationDTO.TabListDTO> list = new ArrayList<>();
        //待提交
        int waitSubmitCount = this.baseMapper.tabList(ApproveStatusEnum.WAIT_SUBMIT.getCode(), null, null,param.getPermissionSql(), null, false);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.WAIT_SUBMIT.getCode(), PilotApplicationTabEnum.WAIT_SUBMIT.getName(), waitSubmitCount));
        //待我审核
        //根据单据id查询审核流程
        int waitMeApproveCount = 0;
        ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
        dto.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
        dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        dto.setCurApproveId(user.getUid());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
        if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            waitMeApproveCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE_ING.getCode(), null, null,param.getPermissionSql(), ids,null);
        }
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.WAIT_ME_APPROVE.getCode(), PilotApplicationTabEnum.WAIT_ME_APPROVE.getName(), waitMeApproveCount));
        //不通过
        int rejectCount = this.baseMapper.tabList(ApproveStatusEnum.REJECT.getCode(), null, null,param.getPermissionSql(), null,false);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.REJECT.getCode(), PilotApplicationTabEnum.REJECT.getName(), rejectCount));
        //未下单
        int notOrderCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.NOT_ORDER.getCode(), null,param.getPermissionSql(), null,null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.NOT_ORDER.getCode(), PilotApplicationTabEnum.NOT_ORDER.getName(), notOrderCount));
        //已下单
        int orderCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.ORDER.getCode(), null,param.getPermissionSql(), null,null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.ORDER.getCode(), PilotApplicationTabEnum.ORDER.getName(), orderCount));
        return list;
    }

    @Override
    public void exportList(PilotApplicationDTO.ExportDTO param) {
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        StringBuilder builder = new StringBuilder();
        builder.append("试产量产单导出").append(date);
        downloadTaskFeign.saveDownloadTask(builder.toString(), EXPORT_PLM_PILOT_APPLICATION.getCode(), param);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        PilotApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"试产申请");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改试产申请状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动试产申请流程，id=：【{}】", entity.getId());
        startProcess(entity);

        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据提交审核", UserContext.getNonLoginUser().getUserName(), entity.getCode());
        this.addLog(entity.getId(), "提交操作", format, "审核状态", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(PilotApplicationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(PilotApplicationDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto, PilotApplicationDTO.ApproveDTO approveDTO) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        PilotApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //保存数据
        if (approveDTO.getProductDetailList() != null && !approveDTO.getProductDetailList().isEmpty()) {
            List<PilotApplicationDetailDTO.ViewDTO> productDetailList = approveDTO.getProductDetailList();
            for (PilotApplicationDetailDTO.ViewDTO productDTO : productDetailList) {
                if (Objects.equals(approveType, ApproveTypeEnum.PASS)) {
                    pilotApplicationDetailService.lambdaUpdate().set(PilotApplicationDetailEntity::getApproveQty, productDTO.getApproveQty()).eq(PilotApplicationDetailEntity::getId, productDTO.getId()).update();
                }
            }
        } else {
            List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, dto.getId()).list();
            if (CollectionUtils.isNotEmpty(detailList)) {
                for (PilotApplicationDetailEntity detailEntity : detailList) {
                    Integer applyQty = detailEntity.getApplyQty();
                    Integer approveQty = detailEntity.getApproveQty();
                    pilotApplicationDetailService.lambdaUpdate().set(PilotApplicationDetailEntity::getApproveQty, approveQty == 0 ? applyQty : approveQty).eq(PilotApplicationDetailEntity::getId, detailEntity.getId()).update();
                }
            }
        }
        // 调用流程审核
        approveProcess(entity, dto, approveDTO);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        //记录日志
        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据审核操作 审核结果：【%s】 审核意见：【%s】", UserContext.getNonLoginUser().getUserName(), entity.getCode(), approveType.getName(), dto.getComment());
        this.addLog(entity.getId(), "审核操作", format, null, null, null);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 回写产品管理--采购信息--一级和二级供应商
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void writeProductPurchaseBack(String id){
        PilotApplicationEntity entity = getById(id);
        //审批通过 回写产品管理--采购信息--一级和二级供应商
        if(null != entity && entity.getApproveStatus().getCode().equals(ApproveStatusEnum.APPROVE.getCode())){
            List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, id).list();
            if(CollectionUtils.isNotEmpty(detailList)){
                for (PilotApplicationDetailEntity detailEntity : detailList) {
                    productPurchaseService.lambdaUpdate()
                            .eq(ProductPurchaseEntity::getSkuId, detailEntity.getSkuId())
                            .set(ProductPurchaseEntity::getMainSupplier, detailEntity.getMainSupplierId())
                            .set(ProductPurchaseEntity::getSecondSupplier, detailEntity.getSecondSupplierId())
                            .update();
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void writeProductPurchaseBackByWork(String id) {
        //审批通过 回写产品管理--采购信息--一级和二级供应商
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, id).list();
        if (CollectionUtils.isNotEmpty(detailList)) {
            for (PilotApplicationDetailEntity detailEntity : detailList) {
                productPurchaseService.lambdaUpdate()
                        .eq(ProductPurchaseEntity::getSkuId, detailEntity.getSkuId())
                        .set(ProductPurchaseEntity::getMainSupplier, detailEntity.getMainSupplierId())
                        .set(ProductPurchaseEntity::getSecondSupplier, detailEntity.getSecondSupplierId())
                        .update();
            }
        }
    }

    @Override
    public void approvePilotApplicationNoticeByWork(String id) {
        PilotApplicationDTO.ApprovePilotNoticeDTO approvePilotNoticeDTO = this.getPilotApplicationNoticeData(id);
        if (null != approvePilotNoticeDTO) {
            noticeMessageService.approvePilotApplicationNotice(approvePilotNoticeDTO.getUserName(), approvePilotNoticeDTO, Boolean.TRUE);
        }
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     * @param baseApproveDTO
     */
    private void approveProcess(PilotApplicationEntity entity, ApproveOneDTO dto, PilotApplicationDTO.ApproveDTO baseApproveDTO) {
        LoginUser userInfo = UserContext.getNonLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        Map<String, Object> variablesMap = getVariablesMap(entity);
        variablesMap.put("attachmentList", baseApproveDTO.getAttachmentList());
        approveDTO.setVariablesMap(variablesMap);
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

    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025-06-09
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(PilotApplicationEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.PILOTAPPLICATION.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);

        //附件
        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(Collections.singletonList(entity.getId()));
        if (ObjectUtil.isNotEmpty(attachmentList)) {
            HashMap<String,Object> attachmentMap = new HashMap<>();
            for (PlmAttachmentEntity obj : attachmentList) {
                attachmentMap.put(obj.getAttachName(), FastDFSClientUtil.publicUrl + obj.getAttachUrl());
            }
            map.put("attachmentMap", attachmentMap);
        }

        List<PilotApplicationDetailDTO.ViewDTO> detailList = new ArrayList<>();
        Object rawList = map.get("productDetailList");

        if (rawList instanceof List) {
            for (Object item : (List<?>) rawList) {
                if (item instanceof PilotApplicationDetailDTO.ViewDTO) {
                    detailList.add((PilotApplicationDetailDTO.ViewDTO) item);
                } else if (item instanceof Map) {
                    // Map转Bean
                    PilotApplicationDetailDTO.ViewDTO viewDTO = BeanUtil.toBean(
                            (Map<?, ?>) item,
                            PilotApplicationDetailDTO.ViewDTO.class
                    );
                    detailList.add(viewDTO);
                } else {
                    log.warn("无法转换的类型: {} 值: {}",
                            item.getClass().getName(), item);
                }
            }
        }
        if (ObjectUtil.isEmpty(detailList)) {
            return map;
        }
        //产品费用
        List<String> skuIds = detailList.stream().map(PilotApplicationDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductCostEntity> productCostEntityList = productCostService.lambdaQuery().in(ProductCostEntity::getSkuId, skuIds).list();

        //产品信息
        List<ProductDetailEntity> productDetailList = productDetailService.listByIds(skuIds);

        //通过Java8取一级供应商和二级供应商
        List<String> supplierIds = detailList.stream()
                .flatMap(detail -> Stream.of(detail.getMainSupplierId(), detail.getSecondSupplierId()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<SupplierEntity> supplierList = FeignQuery.getByIds(SupplierEntity.class,supplierIds);

        for (PilotApplicationDetailDTO.ViewDTO detailEntity : detailList) {
            //目标成本
            Optional<ProductCostEntity> productCostEntityOptional = productCostEntityList.stream().filter(item -> item.getSkuId().equals(detailEntity.getSkuId())).findFirst();
            if (productCostEntityOptional.isPresent()) {
                ProductCostEntity productCostEntity = productCostEntityOptional.get();
                detailEntity.setTargetTaxCost(productCostEntity.getTargetTaxCost());
                detailEntity.setTargetNoTaxCost(productCostEntity.getTargetNoTaxCost());
            }
            //实际成本
            fillActualCost(detailEntity);

            //一级供应商名称
            String mainSupplierName = supplierList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getMainSupplierId())).map(SupplierEntity::getName).findFirst().orElse("");
            detailEntity.setMainSupplierName(mainSupplierName);
            //二级供应商名称
            String secondSupplierName = supplierList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getMainSupplierId())).map(SupplierEntity::getName).findFirst().orElse("");
            detailEntity.setSecondSupplierName(secondSupplierName);
            //产品名称
            String productName = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            detailEntity.setProductName(productName);
        }
        map.put("productDetailList", detailList);
        return map;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到试产申请单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        List<PurchaseApplicationEntity> purchaseAppList = purchaseApplicationFeign.listBySourceIds(Collections.singletonList(id));
        if (!purchaseAppList.isEmpty()) {
            throw new ServiceException("已下推采购申请单，不允许反审核");
        }
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据反审核", UserContext.getNonLoginUser().getUserName(), entity.getCode());
        this.addLog(id, "反审核", format, "审核状态", entity.getApproveStatus().getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private void addLog(String id, String operation, String content, String field, String oldValue, String newValue) {
        SysLogEntity logEntity = new SysLogEntity();
        logEntity.setBusinessId(id);
        logEntity.setOperation(operation);
        logEntity.setContent(content);
        logEntity.setFieldName(field);
        logEntity.setOldValue(oldValue);
        logEntity.setNewValue(newValue);
        sysLogService.addSysLogByOther(logEntity);
    }

    private Boolean validateDisApprove(PilotApplicationEntity entity) {
        // 已审核支持反审核
        if (entity.getApproveStatus().compareTo(ApproveStatusEnum.APPROVE) != 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // 已下推采购申请单，不能反审
        List<PurchaseApplicationEntity> purchaseApplicationList = purchaseApplicationFeign.listBySourceIds(Collections.singletonList(entity.getId()));
        if (!purchaseApplicationList.isEmpty()) {
            throw new ServiceException("已下推采购申请单，不能反审核");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"试产申请"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException("只有待提交并且未作废数据支持删除");
        }
        pilotApplicationDetailService.lambdaUpdate().eq(PilotApplicationDetailEntity::getMainId, entity.getId()).remove();
        pilotApplicationRefTaskService.lambdaUpdate().eq(PilotApplicationRefTaskEntity::getMainId, entity.getId()).remove();
        // 删除主单数据
        log.info("删除 开始删除试产申请主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"试产申请"));
        // 只有审核中的单据允许撤销
        if (entity.getApproveStatus().compareTo(ApproveStatusEnum.APPROVE_ING) != 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据撤销流程", UserContext.getNonLoginUser().getUserName(), entity.getCode());
        this.addLog(id, "撤销操作", format, null, null, null);
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getNonLoginUser().getUid());
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, entity.getId()).list();
        if (CollUtil.isNotEmpty(detailList)) {
            variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        }
        revokeDTO.setVariablesMap(variablesMap);

        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, PilotApplicationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        return updateForApprove(entity.getId(), approveStatus.getStatus());
    }

    @Override
    public PilotApplicationDTO.ViewDTO view(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"试产申请"));

        // 数据填充处理
        PilotApplicationDTO.ViewDTO view = fillOne(entity);
        return view;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(PilotApplicationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private PilotApplicationDTO.ViewDTO fillOne(PilotApplicationEntity pilotApplicationEntity) {
        if (ObjectUtil.isEmpty(pilotApplicationEntity)) {
            return null;
        }
        List<PilotApplicationRefTaskEntity> refTaskList = pilotApplicationRefTaskService.lambdaQuery().eq(PilotApplicationRefTaskEntity::getMainId, pilotApplicationEntity.getId()).list();
        List<PilotApplicationDetailEntity> productDetailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, pilotApplicationEntity.getId()).list();
        //产品任务
        List<String> taskIds = refTaskList.stream().map(item -> item.getTaskId()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<ProjectTaskDTO.SimpleViewDTO> taskList = projectTaskService.listSimpleViewByIds(taskIds);
        Map<String, ProjectTaskDTO.SimpleViewDTO> taskMap = taskList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));

        PilotApplicationDTO.ViewDTO view = new PilotApplicationDTO.ViewDTO();
        List<PilotApplicationDetailDTO.ViewDTO> detailViewList = BeanMapper.copyList(productDetailList, PilotApplicationDetailDTO.ViewDTO.class);
        List<PilotApplicationRefTaskDTO.ViewDTO> taskViewList = BeanMapper.copyList(taskList, PilotApplicationRefTaskDTO.ViewDTO.class);
        //供应商
        List<String> supplierIds = detailViewList.stream().map(item -> item.getMainSupplierId()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<String> secondSupplierIds = detailViewList.stream().map(item -> item.getSecondSupplierId()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        supplierIds.addAll(secondSupplierIds);
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
        //产品费用
        List<String> skuIds = detailViewList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductCostEntity> productCostEntityList = productCostService.lambdaQuery().in(ProductCostEntity::getSkuId, skuIds).list();
        //产品明细
        List<ProductDetailEntity> skuList = productDetailService.lambdaQuery().in(ProductDetailEntity::getId, skuIds).list();
        //附件
        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(Collections.singletonList(pilotApplicationEntity.getId()));

        //批量查询采购价目表
        List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> taxPriceSearchList = new ArrayList<>(detailViewList.size());
        for (PilotApplicationDetailDTO.ViewDTO detailDTO : detailViewList){
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            searchDTO.setPurchaseQty(detailDTO.getApplyQty());
            searchDTO.setSupplierId(detailDTO.getMainSupplierId());
            searchDTO.setSkuId(detailDTO.getSkuId());
            searchDTO.setSkuNo(detailDTO.getSkuNo());
            taxPriceSearchList.add(searchDTO);
        }
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> taxPriceResultList = purchasePriceDetailFeign.listTaxPrice(taxPriceSearchList);
        Map<String, PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> taxPriceResultMap = taxPriceResultList.stream().collect(Collectors.toMap(PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO::getSkuId, e -> e, (o1, o2) -> o1));

        //处理产品明细
        for (PilotApplicationDetailDTO.ViewDTO detailDTO : detailViewList) {
            //一级供应商名称
            detailDTO.setMainSupplierName(supplierMap.containsKey(detailDTO.getMainSupplierId()) ? supplierMap.get(detailDTO.getMainSupplierId()).getName() : "");
            //二级供应商名称
            detailDTO.setSecondSupplierName(supplierMap.containsKey(detailDTO.getSecondSupplierId()) ? supplierMap.get(detailDTO.getSecondSupplierId()).getName() : "");
            //目标成本
            Optional<ProductCostEntity> productCostEntityOptional = productCostEntityList.stream().filter(item -> item.getSkuId().equals(detailDTO.getSkuId())).findFirst();
            if (productCostEntityOptional.isPresent()) {
                ProductCostEntity productCostEntity = productCostEntityOptional.get();
                detailDTO.setTargetTaxCost(productCostEntity.getTargetTaxCost());
                detailDTO.setTargetNoTaxCost(productCostEntity.getTargetNoTaxCost());
            }
            //实际含税单价
            PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO priceViewDTO = taxPriceResultMap.getOrDefault(detailDTO.getSkuId(), null);
            if(Objects.isNull(priceViewDTO)){
                log.error("没有找到价目表：{} {}", detailDTO.getSkuNo(), detailDTO.getMainSupplierName());
            }else {
                detailDTO.setActualTaxCost(priceViewDTO.getTaxPrice());
                BigDecimal divide = priceViewDTO.getTaxRate().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal add = divide.add(BigDecimal.ONE);
                detailDTO.setActualNoTaxCost(priceViewDTO.getTaxPrice().divide(add, 4, RoundingMode.HALF_UP));
            }
            //产品名称
            Optional<ProductDetailEntity> skuOptional = skuList.stream().filter(item -> item.getId().equals(detailDTO.getSkuId())).findFirst();
            skuOptional.ifPresent(sku -> {
                detailDTO.setProductName(sku.getName());
                detailDTO.setStatus(sku.getStatus());
                detailDTO.setStatusName(ProductDetailStatusEnum.getName(sku.getStatus()));
            });
            //类型 试产trial  量产batch
            if(detailDTO.getType().equals(PilotApplicationTypeEnum.TRIAL.getCode())){
                detailDTO.setTypeName(PilotApplicationTypeEnum.TRIAL.getName());
            }else if(detailDTO.getType().equals(PilotApplicationTypeEnum.BATCH.getCode())){
                detailDTO.setTypeName(PilotApplicationTypeEnum.BATCH.getName());
            }
        }
        //处理关联任务
        for (PilotApplicationRefTaskDTO.ViewDTO taskDTO : taskViewList) {
            if (!taskMap.containsKey(taskDTO.getTaskId())) {
                log.error("试产量产单没有找到任务详情:{} {}", taskDTO.getId(), taskDTO.getTaskId());
                continue;
            }
            ProjectTaskDTO.SimpleViewDTO entity = taskMap.get(taskDTO.getTaskId());
            taskDTO.setChargeId(entity.getChargeId());
            taskDTO.setChargeName(entity.getChargeName());
            taskDTO.setName(entity.getName());
            taskDTO.setPhaseId(entity.getPhaseId());
            taskDTO.setPhaseName(entity.getPhaseName());
            taskDTO.setProductId(entity.getProductId());
            taskDTO.setProductName(entity.getProductName());
            taskDTO.setSkuNo(entity.getSkuNoStr());
            taskDTO.setSpuNo(entity.getSpuNo());
            taskDTO.setStatus(entity.getStatus());
            taskDTO.setStatusName(TaskStateEnum.getName(entity.getStatus()));
        }
        view.setId(pilotApplicationEntity.getId());
        view.setCode(pilotApplicationEntity.getCode());
        view.setBillDate(pilotApplicationEntity.getBillDate());
        view.setRemark(pilotApplicationEntity.getRemark());
        view.setApproveStatus(pilotApplicationEntity.getApproveStatus());
        view.setApproveStatusName(view.getApproveStatus().getName());
        view.setProductDetailList(detailViewList);
        view.setTaskList(taskViewList);
        view.setAttachmentList(attachmentList);
        //审核记录
        List<PilotApplicationDTO.AuditorHandleDTO> approveList = this.getApproveProcessList(pilotApplicationEntity);
        view.setApproveFlowList(approveList);
        return view;
    }

    /**
     * 查询采购价目表，设置实际含税单价，实际不含税单价
     */
    private void fillActualCost(PilotApplicationDetailDTO.ViewDTO detailDTO) {
        PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
        searchDTO.setPurchaseQty(detailDTO.getApplyQty());
        searchDTO.setSupplierId(detailDTO.getMainSupplierId());
        searchDTO.setSkuId(detailDTO.getSkuId());
        searchDTO.setSkuNo(detailDTO.getSkuNo());
        try {
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailFeign.getTaxPrice(searchDTO);
            for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                if (detailDTO.getApplyQty() >= priceViewDTO.getMinQty() && detailDTO.getApplyQty() <= priceViewDTO.getMaxQty()) {
                    detailDTO.setActualTaxCost(priceViewDTO.getTaxPrice());
                    BigDecimal divide = priceViewDTO.getTaxRate().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal add = divide.add(BigDecimal.ONE);
                    detailDTO.setActualNoTaxCost(priceViewDTO.getTaxPrice().divide(add, 4, RoundingMode.HALF_UP));
                    break;
                }
            }
        } catch (Exception e) {
            log.error("没有找到价目表：{} {}", detailDTO.getSkuNo(), detailDTO.getMainSupplierName());
        }
    }

    /**
     * 获取审核记录
     */
    private List<PilotApplicationDTO.AuditorHandleDTO> getApproveProcessList(PilotApplicationEntity pilotApplicationEntity) {
        List<ProcessTaskManagementDTO.ApproveHistoryDTO> approveHistoryList = processTaskManagementFeign.listApproveHistory(pilotApplicationEntity.getId());
        approveHistoryList = approveHistoryList.stream().filter(item -> item.getApproveUserName().equals(item.getCurApproveName())).collect(Collectors.toList());
        List<PilotApplicationDTO.AuditorHandleDTO> resultList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ProcessTaskManagementDTO.ApproveHistoryDTO historyDTO : approveHistoryList) {
            PilotApplicationDTO.AuditorHandleDTO auditorHandleDTO = new PilotApplicationDTO.AuditorHandleDTO();
            auditorHandleDTO.setUserId(historyDTO.getApproveUserId());
            auditorHandleDTO.setUserName(historyDTO.getApproveUserName());
            auditorHandleDTO.setResult(historyDTO.getApproveStatus());
            auditorHandleDTO.setResultName(PilotApplicatonApproveHistoryEnum.getName(historyDTO.getApproveStatus()));
            if (historyDTO.getApproveTime() != null) {
                auditorHandleDTO.setTime(historyDTO.getApproveTime().format(dateTimeFormatter));
                String desc = getTimeDesc(auditorHandleDTO);
                auditorHandleDTO.setTimeDesc(desc);
            }
            auditorHandleDTO.setComment(historyDTO.getRemark());
            auditorHandleDTO.setAttachmentList(historyDTO.getAttachmentList());
            resultList.add(auditorHandleDTO);
        }
        return resultList;
    }

    private static String getTimeDesc(PilotApplicationDTO.AuditorHandleDTO auditorHandleDTO) {
        Date now = new Date();
        DateTime handleTime = cn.hutool.core.date.DateUtil.parse(auditorHandleDTO.getTime());
        long betweenHour = cn.hutool.core.date.DateUtil.between(handleTime, now, DateUnit.HOUR);
        String desc;
        if (betweenHour > 24) {
            desc = cn.hutool.core.date.DateUtil.formatBetween(handleTime, now, BetweenFormatter.Level.DAY);
        } else {
            desc = cn.hutool.core.date.DateUtil.formatBetween(handleTime, now, BetweenFormatter.Level.HOUR);
        }
        return desc + "前";
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public Boolean updateForApprove(String id, String approveStatus) {
        LoginUser userInfo = UserContext.getNonLoginUser();
        return this.lambdaUpdate().eq(PilotApplicationEntity::getId, id)
                .set(PilotApplicationEntity::getApproveUserId, userInfo.getUid())
                .set(PilotApplicationEntity::getApproveStatus, approveStatus)
                .set(PilotApplicationEntity::getApproveTime, LocalDateTime.now())
                .update(new PilotApplicationEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(PilotApplicationEntity::getId, id)
                .set(PilotApplicationEntity::getApproveUserId, "")
                .set(PilotApplicationEntity::getApproveStatus, approveStatus)
                .set(PilotApplicationEntity::getApproveTime, null)
                .update(new PilotApplicationEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(PilotApplicationEntity::getId, id)
                .set(PilotApplicationEntity::getApproveStatus, approveStatus)
                .update(new PilotApplicationEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<PilotApplicationDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //用户
        List<String> approveUserIds = list.stream().map(item -> item.getApproveUserId()).distinct().collect(Collectors.toList());
        List<String> createUserIds = list.stream().map(item -> item.getCreateUserId()).distinct().collect(Collectors.toList());
        approveUserIds.addAll(createUserIds);
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(approveUserIds.stream().distinct().collect(Collectors.toList()));
        Map<String, String> userMap = userList.stream().collect(Collectors.toMap(item1 -> item1.getUserId(), item2 -> item2.getUserName()));
        //产品
        List<String> skuIds = list.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = productDetailService.listByIds(skuIds);
        Map<String, String> productDetailMap = productDetailList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2.getName()));
        //供应商
        List<String> supplierIds = list.stream().map(item -> item.getMainSupplierId()).distinct().collect(Collectors.toList());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
        //产品费用
        List<ProductCostEntity> productCostEntityList = productCostService.lambdaQuery().in(ProductCostEntity::getSkuId, skuIds).list();
        //根据单据id查询审核流程
        List<String> ids = list.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessId(ids);
        //采购申请
        List<PurchaseApplicationEntity> purchaseApplicationList = purchaseApplicationFeign.listBySourceIds(ids);
        List<String> purchaseIds = purchaseApplicationList.stream()
                .filter(item -> item.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode()))
                .map(BaseEntity::getId).distinct()
                .collect(Collectors.toList());
        //采购申请明细
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = new ArrayList<>();
        if (!purchaseIds.isEmpty()) {
            purchaseApplicationDetailList = purchaseApplicationDetailFeign.listByMainIds(purchaseIds);
        }
        //采购入库数量
        List<PurchaseApplicationDTO.ListDTO> purchaseList = new ArrayList<>();
        for (PurchaseApplicationDetailEntity detailEntity : purchaseApplicationDetailList) {
            PurchaseApplicationDTO.ListDTO obj = new PurchaseApplicationDTO.ListDTO();
            obj.setPurchaseApplicationDetailId(detailEntity.getId());
            obj.setSkuId(detailEntity.getSkuId());
            obj.setId(detailEntity.getPurchaseApplicationId());
            purchaseList.add(obj);
        }
        purchaseList = purchaseApplicationFeign.listStockInQty(purchaseList);

        //批量查询采购价目表
        List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> taxPriceSearchList = new ArrayList<>(list.size());
        for (PilotApplicationDTO.ListDTO detailDTO : list){
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            searchDTO.setPurchaseQty(detailDTO.getApplyQty());
            searchDTO.setSupplierId(detailDTO.getMainSupplierId());
            searchDTO.setSkuId(detailDTO.getSkuId());
            searchDTO.setSkuNo(detailDTO.getSkuNo());
            taxPriceSearchList.add(searchDTO);
        }
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> taxPriceResultList = purchasePriceDetailFeign.listTaxPrice(taxPriceSearchList);
        Map<String, PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> taxPriceResultMap = taxPriceResultList.stream().collect(Collectors.toMap(PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO::getSkuId, e -> e, (o1, o2) -> o1));

        for (PilotApplicationDTO.ListDTO item : list) {
            item.setInvalidStatusName(Objects.nonNull(item.getInvalidStatus()) && item.getInvalidStatus() ? "已作废" : "未作废");
            item.setApproveStatusName(ApproveStatusEnum.getName(item.getApproveStatus()));
            item.setOrderStatusName(PilotPushPurchaseStatusEnum.getName(item.getOrderStatus()));
            item.setProductName(productDetailMap.get(item.getSkuId()));
            item.setMainSupplierName(supplierMap.containsKey(item.getMainSupplierId()) ? supplierMap.get(item.getMainSupplierId()).getName() : "");
            List<String> curApproveName = processTaskManagementList.stream().filter(req -> req.getBusinessId().equals(item.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String waitApproveUserName = StringUtils.join(curApproveName, ",");
            item.setApproveUserName(waitApproveUserName);
            item.setCreateUserName(userMap.get(item.getCreateUserId()));
            item.setTypeName(PilotApplicationTypeEnum.getName(item.getType()));
            Optional<ProductCostEntity> productCostEntityOptional = productCostEntityList.stream().filter(v -> v.getSkuId().equals(item.getSkuId())).findFirst();
            if (productCostEntityOptional.isPresent()) {
                ProductCostEntity productCostEntity = productCostEntityOptional.get();
                item.setTargetTaxCost(productCostEntity.getTargetTaxCost() != null ? productCostEntity.getTargetTaxCost().toPlainString() : "");
            }
            //实际含税单价
            try {
                PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO priceViewDTO = taxPriceResultMap.getOrDefault(item.getSkuId(), null);
                if(Objects.isNull(priceViewDTO)){
                    item.setActualTaxCost("无价目表");
                }else {
                    item.setActualTaxCost(priceViewDTO.getTaxPrice().toPlainString());
                }
            }catch (Exception e) {
                item.setTargetTaxCost("无价目表");
            }
            //采购申请量
            int applyQty = 0;
            List<PurchaseApplicationDetailEntity> detailEntityList = purchaseApplicationDetailList.stream().filter(r -> null != r.getSourceDetailId() && r.getSourceDetailId().equals(item.getDetailId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(detailEntityList)) {

                for (PurchaseApplicationDetailEntity entity : detailEntityList) {
                    applyQty += null == entity.getApplyQty() ? 0 : entity.getApplyQty();
                }
            }
            item.setPurchaseApplyQty(applyQty);
            //采购入库量
            int stockInQty = 0;
            List<PurchaseApplicationDTO.ListDTO> paList = purchaseList.stream().filter(r -> null != r.getSourceDetailId() && r.getSourceDetailId().equals(item.getDetailId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(paList)) {
                for (PurchaseApplicationDTO.ListDTO entity : paList) {
                    stockInQty += null == entity.getStockInQty() ? 0 : entity.getStockInQty();
                }
            }
            item.setStockInQty(stockInQty);
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(PilotApplicationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //校验包装信息是否完整
        validateProductSize(entity.getId());
        //校验是否有采购价目表
        validatePurchasePriceDetail(entity.getId());

    }

    private void validatePurchasePriceDetail(String id) {
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().in(PilotApplicationDetailEntity::getMainId, id).list();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_95281);
        }
        List<String> skuIdList = detailList.stream().map(PilotApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        Map<String, List<BomDTO.BomSku>> singleBomMap = bomSkuService.getSingleBomInfo(skuIdList).stream()
                .collect(Collectors.groupingBy(BomDTO.BomSku::getParentSkuId));

        for (PilotApplicationDetailEntity detailEntity : detailList) {
            if(singleBomMap.containsKey(detailEntity.getSkuId())){//校验SKU为父级时，且存在BOM类型为单品BOM时，无需校验当前SKU，校验子SKU是否存在报价
                // 校验子 SKU 是否存在报价
                validateChildSkuPrices(detailEntity, singleBomMap);
            }else {
                // 校验当前 SKU 是否存在报价
                validateSkuPrice(detailEntity);
            }
        }
    }

    private void validateChildSkuPrices(PilotApplicationDetailEntity detailEntity, Map<String, List<BomDTO.BomSku>> singleBomMap) {
        List<BomDTO.BomSku> skuList = singleBomMap.get(detailEntity.getSkuId());
        StringBuilder error = new  StringBuilder();
        StringBuilder ruleError = new  StringBuilder();
        for (BomDTO.BomSku childSku : skuList) {
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceSearchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            priceSearchDTO.setSkuId(childSku.getSkuId());
            priceSearchDTO.setSkuNo(childSku.getSkuNo());
            priceSearchDTO.setPurchaseQty(detailEntity.getApplyQty() * childSku.getQty());
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>  taxPriceList = purchasePriceDetailFeign.getTaxPrice(priceSearchDTO);
            if (CollUtil.isEmpty(taxPriceList)) {
                error.append("sku：");
                error.append(priceSearchDTO.getSkuNo());
                error.append(",");
                error.append("数量：");
                error.append(priceSearchDTO.getPurchaseQty());
                error.append(";");
            }else {
                Boolean flag = false;
                for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                    if (priceSearchDTO.getPurchaseQty() >= priceViewDTO.getMinQty() && priceSearchDTO.getPurchaseQty() <= priceViewDTO.getMaxQty()) {
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    ruleError.append("sku：");
                    ruleError.append(priceSearchDTO.getSkuNo());
                    ruleError.append(",");
                    ruleError.append("数量：");
                    ruleError.append(priceSearchDTO.getPurchaseQty());
                    ruleError.append(";");
                }
            }
        }
        if(StringUtils.isNotBlank(error.toString())){
            throw new ServiceException(ApiError.ERROR_95288, error.toString());
        }
        if(StringUtils.isNotBlank(ruleError.toString())){
            throw new ServiceException(ApiError.ERROR_95289, ruleError.toString());
        }
    }

    private void validateSkuPrice(PilotApplicationDetailEntity detailEntity) {
        PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceSearchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
        priceSearchDTO.setSkuId(detailEntity.getSkuId());
        priceSearchDTO.setSkuNo(detailEntity.getSkuNo());
        priceSearchDTO.setSupplierId(detailEntity.getMainSupplierId());
        priceSearchDTO.setPurchaseQty(detailEntity.getApplyQty());
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>  taxPriceList;
        try {
            taxPriceList = purchasePriceDetailFeign.getTaxPrice(priceSearchDTO);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95282, priceSearchDTO.getSkuNo(), priceSearchDTO.getPurchaseQty());
        }
        if (CollUtil.isEmpty(taxPriceList)) {
            throw new ServiceException(ApiError.ERROR_95282, priceSearchDTO.getSkuNo(), priceSearchDTO.getPurchaseQty());
        }
        boolean flag = false;
        for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
            if (priceSearchDTO.getPurchaseQty() >= priceViewDTO.getMinQty() && priceSearchDTO.getPurchaseQty() <= priceViewDTO.getMaxQty()) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_95288, priceSearchDTO.getSkuNo(), priceSearchDTO.getPurchaseQty());
        }
    }

    private void validateProductSize(String id) {
        List<PilotApplicationDetailEntity> list = pilotApplicationDetailService.list(Wrappers.<PilotApplicationDetailEntity>lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, id));
        List<String> skuIdList = list.stream().map(PilotApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductPackEntity> productPackList = productPackService.findBySkuIds(skuIdList);
        List<ProductDetailEntity> entityList = productDetailService.listByIds(skuIdList);
        StringBuilder msg = new StringBuilder();
        for (ProductDetailEntity detailEntity : entityList) {
            StringBuilder errMsg = new StringBuilder();
            /**
             * 当产品销售方式为商品时，包装尺寸、箱规 、毛重、单箱重量、净重、单箱数量不能为空
             */
            ProductPackEntity productPackEntity = productPackList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), detailEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productPackEntity)) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_PRODUCT_PACK_NOT_EXIST.msg,detailEntity.getSkuNo())).append(ProductConstant.HTML_BR);
                continue;
            }
            //包装尺寸
            if (MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getProductLength()) >= 0  || MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getProductWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getProductHeight()) >= 0) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_PRODUCT_SIZE_NOT_EXIST.msg, detailEntity.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //箱规
            if (MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getBoxLength()) >= 0 ||MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getBoxWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getBoxHeight()) >= 0) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_BOX_SIZE_NOT_EXIST.msg, detailEntity.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //毛重
            if (MathUtil.compareTo(productPackEntity.getGrossWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_GROSS_WEIGHT_NOT_EXIST.msg, detailEntity.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //单箱重量
            if (MathUtil.compareTo(productPackEntity.getBoxWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_BOX_WEIGHT_NOT_EXIST.msg, detailEntity.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //净重
            if (MathUtil.compareTo(productPackEntity.getNetWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_NET_WEIGHT_NOT_EXIST.msg, detailEntity.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //单箱数量
            if (MathUtil.compareTo(productPackEntity.getBoxQty(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_BOX_QTY_NOT_EXIST.msg, detailEntity.getSkuNo()));
            }
            if (CharSequenceUtil.isNotBlank(msg)) {
                msg.append("SKU【").append(detailEntity.getSkuNo()).append("】").append(errMsg).append(",");
            }
        }
        if (CharSequenceUtil.isNotBlank(msg)) {
            throw new ServiceException(msg.toString());
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(PilotApplicationDTO.AddDTO addDTO, PilotApplicationEntity entity) {
        entity.setBillDate(addDTO.getBillDate());
        entity.setRemark(addDTO.getRemark());
        entity.setApproveStatus(addDTO.getApproveStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> pushPurchaseApplication(List<PilotApplicationDTO.PushPurchaseApplicationDTO> applicationDTOList) {
        return this.addAndSubmit(applicationDTOList, false);
    }

    private PurchaseApplicationDTO.AddDTO getPurchaseApplicationAddDTO(List<PilotApplicationDTO.PushPurchaseApplicationDTO> applicationDTOList, List<PilotApplicationDetailEntity> padList) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(loginUser.getUid());
        List<String> warehouseIds = applicationDTOList.stream().map(item -> item.getToWarehouseId()).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseFeign.listWarehouseByIds(warehouseIds);
        String sourceId = applicationDTOList.get(0).getId();
        String sourceCode = applicationDTOList.get(0).getCode();
        List<PurchaseApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (PilotApplicationDTO.PushPurchaseApplicationDTO dto : applicationDTOList) {
            if (dto.getPurchaseApplyQty() > dto.getSpareApplyQty()) {
                throw new ServiceException("采购申请量不能大于待申请量");
            }

            PurchaseApplicationDetailDTO.AddDTO purchaseDTO = new PurchaseApplicationDetailDTO.AddDTO();
            purchaseDTO.setApplyQty(dto.getPurchaseApplyQty());
            purchaseDTO.setDestWarehouseId(dto.getToWarehouseId());
            Optional<WarehouseDTO.UpdateDTO> warehouseOptional = warehouseList.stream().filter(item -> item.getId().equals(dto.getToWarehouseId())).findFirst();
            warehouseOptional.ifPresent(updateDTO -> purchaseDTO.setDestWarehouseName(updateDTO.getName()));
            purchaseDTO.setIsUrgent(false);
            purchaseDTO.setMoq(dto.getPurchaseApplyQty());
            purchaseDTO.setPlanDeliveryDate(dto.getPlanDeliveryDate());
            purchaseDTO.setProductName(dto.getProductName());
            purchaseDTO.setPurchaseOrgId(dto.getPurchaseOrgId());
            purchaseDTO.setPurchaseOrgName("");
            purchaseDTO.setSkuId(dto.getSkuId());
            purchaseDTO.setSkuNo(dto.getSkuNo());
            purchaseDTO.setPurchaseApplicationId("");
            purchaseDTO.setSourceDetailId(dto.getDetailId());
            purchaseDTO.setRemark(dto.getRemark());
            purchaseDTO.setFirstMassProduct(dto.getFirstMassProduct());
            detailList.add(purchaseDTO);
            //更新采购申请数量
            PilotApplicationDetailEntity pilotApplicationDetailEntity = padList.stream().filter(r -> r.getId().equals(dto.getDetailId())).findFirst().orElse(null);
            int purchaseApplyQty = 0;
            if (null != pilotApplicationDetailEntity) {
                purchaseApplyQty = pilotApplicationDetailEntity.getPurchaseApplyQty();
            }
            String status = dto.getPurchaseApplyQty() < dto.getSpareApplyQty() ? PilotPushPurchaseStatusEnum.PART_ORDER.getCode() : PilotPushPurchaseStatusEnum.ORDER.getCode();
            pilotApplicationDetailService.lambdaUpdate()
                    .set(PilotApplicationDetailEntity::getPurchaseApplyQty, (dto.getPurchaseApplyQty() + purchaseApplyQty))
                    .set(PilotApplicationDetailEntity::getOrderStatus, status)
                    .eq(PilotApplicationDetailEntity::getId, dto.getDetailId())
                    .update();
        }
        PurchaseApplicationDTO.AddDTO paramDto = new PurchaseApplicationDTO.AddDTO();
        paramDto.setApplyDate(LocalDate.now());
        paramDto.setApplyUserId(findUserDTO.getUserId());
        paramDto.setApplyDeptId(findUserDTO.getDepartmentId());
        paramDto.setDetails(detailList);
        paramDto.setSourceId(sourceId);
        paramDto.setSourceCode(sourceCode);
        paramDto.setSourceType(SourceTypeEnum.PILOT_APPLICATION.getCode());
        return paramDto;
    }

    @Override
    public List<PilotApplicationDTO.PushPurchaseApplicationDTO> viewPurchaseApplication(PilotApplicationDTO.PurchaseApplicationParamDTO paramDTO) {
        //主表
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.listByIds(paramDTO.getDetailIds());
        List<String> ids = detailList.stream().map(PilotApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<PilotApplicationEntity> pilotList = this.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
        for (PilotApplicationEntity entity : pilotList) {
            if (entity.getApproveStatus().compareTo(ApproveStatusEnum.APPROVE) != 0) {
                throw new ServiceException("只有已审核的单据允许下推采购申请单");
            }
        }
        for (PilotApplicationDetailEntity detailEntity : detailList) {
            if (detailEntity.getOrderStatus().equals(PilotPushPurchaseStatusEnum.ORDER.getCode())) {
                throw new ServiceException("已下单，不能下推");
            }
        }
        Map<String, PilotApplicationEntity> pilotMap = pilotList.stream().collect(Collectors.toMap(BaseEntity::getId, item2 -> item2));
        //sku信息
        List<String> skuIds = detailList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.lambdaQuery().in(ProductDetailEntity::getId, skuIds).list();
        //采购组织-sku关系记录
        PurchaseSkuOrgRefDTO.QuerySkuDTO querySkuDTO = new PurchaseSkuOrgRefDTO.QuerySkuDTO();
        querySkuDTO.setSkuIdList(skuIds);
        List<PurchaseSkuOrgRefEntity> skuOrgRefList = scmTaskFeign.getBySkuIdList(querySkuDTO);
        Map<String, ProductDetailEntity> skuMap = skuList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        //根据试产量产单主键id查询采购申请单明细的集合
        List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> purchaseSkuQtyList = purchaseApplicationFeign.listSkuAndQty(ids);
        Map<String, Integer> purchaseSkuQtyMap = purchaseSkuQtyList.stream().collect(Collectors.toMap(item -> item.getSourceId() + ":" + item.getSourceDetailId(), item2 -> item2.getQty()));
        List<PilotApplicationDTO.PushPurchaseApplicationDTO> resultList = new ArrayList<>(detailList.size());
        for (PilotApplicationDetailEntity detailEntity : detailList) {
            PilotApplicationDTO.PushPurchaseApplicationDTO dto = new PilotApplicationDTO.PushPurchaseApplicationDTO();
            dto.setId(detailEntity.getMainId());
            if (pilotMap.containsKey(detailEntity.getMainId())) {
                PilotApplicationEntity entity = pilotMap.get(detailEntity.getMainId());
                dto.setCode(entity.getCode());
            }
            dto.setApplyQty(detailEntity.getApplyQty());
            dto.setApproveQty(detailEntity.getApproveQty());
            dto.setDetailId(detailEntity.getId());
            dto.setSkuId(detailEntity.getSkuId());
            //赋值采购组织
            skuOrgRefList.stream().filter(item -> item.getSkuId().equals(detailEntity.getSkuId())).findFirst().ifPresent(item -> {
                dto.setPurchaseOrgId(item.getPurchaseOrgId());
                dto.setPurchaseOrgName(item.getPurchaseOrgName());
            });
            if (skuMap.containsKey(detailEntity.getSkuId())) {
                ProductDetailEntity sku = skuMap.get(detailEntity.getSkuId());
                dto.setProductName(sku.getName());
                dto.setSkuNo(sku.getSkuNo());
            }
            dto.setType(detailEntity.getType());
            dto.setTypeName(PilotApplicationTypeEnum.getName(detailEntity.getType()));
            if (purchaseSkuQtyMap.containsKey(detailEntity.getMainId() + ":" + detailEntity.getId())) {
                //待申请量=批准数量-已下推的申请量
                //已申请量
                Integer qty = purchaseSkuQtyMap.get(detailEntity.getMainId() + ":" + detailEntity.getId());
                //批准数量
                Integer approveQty = detailEntity.getApproveQty();
                dto.setSpareApplyQty(approveQty - qty);
            } else {
                dto.setSpareApplyQty(detailEntity.getApproveQty());
            }
            resultList.add(dto);
        }
        List<PilotApplicationDTO.PushPurchaseApplicationDTO> collect = resultList.stream().filter(item -> item.getSpareApplyQty() > 0).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new ServiceException("没有可下推的数据");
        }
        return collect;
    }

    @Override
    public List<PilotApplicationDTO.WarehouseDTO> listWarehouse() {
        List<WarehouseDTO.UpdateDTO> list = warehouseFeign.listApproveWarehouse();
        List<PilotApplicationDTO.WarehouseDTO> resultList = new ArrayList<>(list.size());
        list.forEach(item -> resultList.add(new PilotApplicationDTO.WarehouseDTO(item.getId(), item.getName())));
        return resultList;
    }

    @Override
    public List<BaseIdDTO> listPurchaseOrg() {
        return sysUserFeign.listAccountingCompany();
    }

    @Override
    public Boolean addRefTaskBatch(PilotApplicationDTO.RefTaskDTO dto) {
        List<PilotApplicationRefTaskEntity> saveList = new ArrayList<>(dto.getTaskIds().size());
        String id = dto.getId();
        for (String taskId : dto.getTaskIds()) {
            PilotApplicationRefTaskEntity entity = new PilotApplicationRefTaskEntity();
            entity.setTaskId(taskId);
            entity.setMainId(id);
            saveList.add(entity);
        }
        return pilotApplicationRefTaskService.saveBatch(saveList);
    }

    @Override
    public Boolean deleteRefTaskBatch(PilotApplicationDTO.RefTaskDTO dto) {
        return pilotApplicationRefTaskService.lambdaUpdate()
                .eq(PilotApplicationRefTaskEntity::getMainId, dto.getId())
                .in(PilotApplicationRefTaskEntity::getTaskId, dto.getTaskIds())
                .remove();
    }

    @Override
    public Boolean deleteProductBatch(PilotApplicationDTO.ProductDTO dto) {
        return pilotApplicationDetailService.lambdaUpdate()
                .eq(PilotApplicationDetailEntity::getMainId, dto.getId())
                .in(PilotApplicationDetailEntity::getId, dto.getProductDetailIds())
                .remove();
    }

    @Override
    public List<PilotApplicationRefTaskDTO.SimpleListDTO> listRefTask(String id) {
        //todo 查询任务详情
        return Collections.emptyList();
    }

    @Override
    public List<ProductSearchDTO.SkuListDTO> listSkuBySkuNos(ProductSearchDTO.SkuParamDTO skuParamDTO) {
        //已存在数据
        List<String> saleMethodList = skuParamDTO.getSaleMethodList();
        List<String> saleMethodParams = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(saleMethodList)) {
            for (String saleMethod : saleMethodList) {
                saleMethodParams.add(SaleMethodEnum.getNameByCode(Integer.valueOf(saleMethod)));
            }
            skuParamDTO.setSaleMethod(StringUtils.join(saleMethodParams, ","));
        }
        List<ProductSearchDTO.SkuListDTO> list = productDetailMapper.listSkuBySkuNos(skuParamDTO);
        Map<String, String> skuNo2IdMap = list.stream().collect(Collectors.toMap(item -> item.getSkuNo(), item2 -> item2.getSkuId()));

        //产品费用
        List<String> skuIds = list.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductCostEntity> productCostEntityList = productCostService.lambdaQuery().in(ProductCostEntity::getSkuId, skuIds).list();
        List<ProductSearchDTO.SkuListDTO> resultList = new ArrayList<>();
        for (String skuNo : skuParamDTO.getSkuNoList()) {
            ProductSearchDTO.SkuListDTO skuListDTO = list.stream().filter(obj -> obj.getSkuNo().equals(skuNo)).findFirst().orElse(new ProductSearchDTO.SkuListDTO());
            //sku状态名称
            skuListDTO.setStatusName(ProductDetailStatusEnum.getName(skuListDTO.getStatus()));
            //供应商名称
            skuListDTO.setMainSupplier("");
            Optional<ProductCostEntity> productCostEntityOptional = productCostEntityList.stream().filter(item -> item.getSkuId().equals(skuNo2IdMap.get(skuNo))).findFirst();
            if (productCostEntityOptional.isPresent()) {
                ProductCostEntity productCostEntity = productCostEntityOptional.get();
                skuListDTO.setTargetTaxCost(productCostEntity.getTargetTaxCost());
                skuListDTO.setTargetNoTaxCost(productCostEntity.getTargetNoTaxCost());
                skuListDTO.setActualTaxCost(productCostEntity.getActualTaxCost());
                skuListDTO.setActualNoTaxCost(productCostEntity.getActualNoTaxCost());
            }
            resultList.add(skuListDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> pushAndSubmitPurchaseApplication(List<PilotApplicationDTO.PushPurchaseApplicationDTO> applicationDTOList) {
        return this.addAndSubmit(applicationDTOList, true);
    }

    public List<BatchResultDTO> addAndSubmit(List<PilotApplicationDTO.PushPurchaseApplicationDTO> applicationDTOList, Boolean submit) {
        List<String> pilotApplicationIds = applicationDTOList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        List<PilotApplicationEntity> entityList = this.lambdaQuery().in(PilotApplicationEntity::getId, pilotApplicationIds).list();
        List<String> detailIds = applicationDTOList.stream().map(item -> item.getDetailId()).distinct().collect(Collectors.toList());
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().in(PilotApplicationDetailEntity::getId, detailIds).list();
        for (PilotApplicationEntity entity : entityList) {
            if (ObjectUtil.notEqual(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
                throw new ServiceException("只有已审核的单据允许下推采购申请单");
            }
        }
        for (PilotApplicationDetailEntity detailEntity : detailList) {
            if (ObjectUtil.equal(detailEntity.getOrderStatus(), PilotPushPurchaseStatusEnum.ORDER.getCode())) {
                throw new ServiceException("只有未下单或部分下单的SKU允许下推采购申请单");
            }
        }
        //根据单号拆分后下推
        List<BatchResultDTO> resultList = new ArrayList<>();
        Map<String, List<PilotApplicationDTO.PushPurchaseApplicationDTO>> map = applicationDTOList.stream().collect(Collectors.groupingBy(item -> item.getId()));
        Map<String, List<PilotApplicationDetailEntity>> detailMap = detailList.stream().collect(Collectors.groupingBy(PilotApplicationDetailEntity::getMainId));
        for (Map.Entry<String, List<PilotApplicationDTO.PushPurchaseApplicationDTO>> entry : map.entrySet()) {
            PilotApplicationEntity entity = entityList.stream().filter(item -> item.getId().equals(entry.getKey())).findFirst().orElse(new PilotApplicationEntity());
            //以试产量产订单为维度下推采购订单
            PurchaseApplicationDTO.AddDTO paramDto = getPurchaseApplicationAddDTO(entry.getValue(), detailMap.get(entity.getId()));
            BatchResultDTO resultDTO;
            if (submit) {
                resultDTO = purchaseApplicationFeign.addAndSubmit(paramDto);
            } else {
                resultDTO = purchaseApplicationFeign.add(paramDto);
            }
            //记录日志
            String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据下推采购申请单，单号为：【%s】", UserContext.getNonLoginUser().getUserName(), entity.getCode(), resultDTO.getCode());
            this.addLog(entity.getId(), "下推操作", format, null, null, null);
            resultList.add(resultDTO);
        }
        return resultList;
    }

    @Override
    public void updateDetailByPilotApplicationDetailIds(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //更新订单状态
            pilotApplicationDetailService.lambdaUpdate()
                    .set(PilotApplicationDetailEntity::getOrderStatus, entry.getValue())
                    .eq(PilotApplicationDetailEntity::getId, entry.getKey())
                    .update();
        }
    }

    @Override
    public PilotApplicationDTO.ApprovePilotNoticeDTO getPilotApplicationNoticeData(String id) {
        // 在事务提交后执行的方法
        PilotApplicationDTO.ApprovePilotNoticeDTO approvePilotNoticeDTO = null;
        PilotApplicationEntity entity = this.getById(id);
        //获取试产量产明细中的skuId集合
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, id).eq(PilotApplicationDetailEntity::getIsDeleted, Boolean.FALSE).list();
        List<String> skuIds = detailList.stream().map(PilotApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO.ProductChargeInfoDTO> productChargeInfoList = productDetailService.listProductChargeInfoByIds(skuIds);
        if (CollectionUtils.isNotEmpty(productChargeInfoList)) {
            approvePilotNoticeDTO = new PilotApplicationDTO.ApprovePilotNoticeDTO();
            BeanMapperUtils.copy(productChargeInfoList.get(0), approvePilotNoticeDTO);
            //试产量产主键id
            approvePilotNoticeDTO.setId(entity.getId());
            approvePilotNoticeDTO.setCode(entity.getCode());
            //状态
            approvePilotNoticeDTO.setApproveStatus(entity.getApproveStatus());
            //飞书消息通知
            LoginUser loginUser = UserContext.getDefaultLoginUser();
            String userName = loginUser.getUserName();
            approvePilotNoticeDTO.setUserName(userName);
        }
        return approvePilotNoticeDTO;
    }

    @Override
    public void approvePilotApplicationNotice(String id) {
        PilotApplicationDTO.ApprovePilotNoticeDTO approvePilotNoticeDTO = this.getPilotApplicationNoticeData(id);
        if (null != approvePilotNoticeDTO) {
            Boolean sendFlag = Boolean.FALSE;
            if (approvePilotNoticeDTO.getApproveStatus().getStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
                sendFlag = Boolean.TRUE;
            }
            noticeMessageService.approvePilotApplicationNotice(approvePilotNoticeDTO.getUserName(), approvePilotNoticeDTO, sendFlag);
        }
    }


    @Override
    public List<ProductPackViewDTO> listProductPackBySkuIds(List<String> ids) {
        List<ProductPackViewDTO> dtos = productDetailService.listProductPackBySkuIds(ids);
        return dtos.stream().filter(this::verifyComplete).collect(Collectors.toList());
    }

    @Override
    public BatchResultDTO invalid(String id, String remark) {
        PilotApplicationEntity old = Optional.ofNullable(super.getById(id)).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "试产量产单"));
        if (old.getInvalidStatus()) {
            return BatchResultDTO.fail(old.getId(),old.getCode(),ApiError.ERROR_98012.msg);
        }
        //仅支持待提交/审核不通过可作废
        if (!old.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT) && !old.getApproveStatus().equals(ApproveStatusEnum.REJECT)) {
            return BatchResultDTO.fail(old.getId(),old.getCode(),ApiError.ERROR_98005.msg);
        }
        //创建人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //更新成作废状态
        old.setInvalidStatus(Boolean.TRUE);
        old.setInvalidRemark(remark);
        old.setInvalidTime(LocalDateTime.now());
        old.setInvalidUserId(userInfo.getUid());
        old.setInvalidUserName(userInfo.getUserName());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("用户{}，作废了{}，作废原因{}",userInfo.getUserName(), old.getCode(), old.getInvalidRemark());
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(id)
                .setBusinessId(id).setOperation("作废").setContent(msg));
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public BatchResultDTO unInvalid(String id) {
        PilotApplicationEntity old = Optional.ofNullable(super.getById(id)).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "试产量产单"));
        if (!old.getInvalidStatus()) {
            return BatchResultDTO.fail(old.getId(),old.getCode(),"未作废单据不支持取消作废");
        }

        log.info("反作废 开始修改试产量产单状态数据，id：【{}】", id);
        lambdaUpdate().eq(PilotApplicationEntity::getId, id)
                .set(PilotApplicationEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .set(PilotApplicationEntity::getInvalidTime, null)
                .set(PilotApplicationEntity::getInvalidRemark, CharSequenceUtil.EMPTY)
                .set(PilotApplicationEntity::getInvalidUserId, CharSequenceUtil.EMPTY)
                .set(PilotApplicationEntity::getInvalidUserName, CharSequenceUtil.EMPTY)
                .update();
        log.info("反作废 开始记录操作日志，id：【{}】", id);
        // 操作日志
        String msg = StrUtil.format("用户{}，取消作废了{}",UserContext.getDefaultLoginUser().getUserName(), old.getCode());
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(id)
                .setBusinessId(id).setOperation("取消作废").setContent(msg));
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.UN_INVALID);
    }

    @Override
    public List<PilotApplicationEntity> listByCodes(List<String> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return super.list(new LambdaQueryWrapper<PilotApplicationEntity>().in(PilotApplicationEntity::getCode, list).eq( PilotApplicationEntity::getIsDeleted, Boolean.FALSE));
        }
        return Collections.emptyList();
    }

    @Override
    public void updateApproveStatus(PilotApplicationDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        ApproveStatusEnum approveStatus = updateApprovalStatusDTO.getApproveStatus();
        PilotApplicationEntity pilotApplicationEntity = updateApprovalStatusDTO.getEntity();

        lambdaUpdate().eq(PilotApplicationEntity::getId, pilotApplicationEntity.getId())
                .set(PilotApplicationEntity::getApproveUserId, pilotApplicationEntity.getApproveUserId())
                .set(PilotApplicationEntity::getApproveStatus, approveStatus)
                .update(new PilotApplicationEntity());
    }

    /**
     * 校验数据是否完整
     * @param v 参数
     */
    private boolean verifyComplete(ProductPackViewDTO v) {
        //包装尺寸
        if (MathUtil.compareTo(BigDecimal.ZERO, v.getProductLength()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, v.getProductWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, v.getProductHeight()) >= 0) {
            return true;
        }
        //箱规
        if (MathUtil.compareTo(BigDecimal.ZERO, v.getBoxLength()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, v.getBoxWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, v.getBoxHeight()) >= 0) {
            return true;
        }
        //毛重
        if (MathUtil.compareTo(v.getGrossWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
            return true;
        }
        //单箱重量
        if (MathUtil.compareTo(v.getBoxWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
            return true;
        }
        //净重
        if (MathUtil.compareTo(v.getNetWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
            return true;
        }
        //单箱数量
        return MathUtil.compareTo(v.getBoxQty(), MathUtil.ZERO) == MathUtil.ZERO;
    }
}
