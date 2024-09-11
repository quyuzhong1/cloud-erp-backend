package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.tms.enums.PilotApplicationTabEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.server.plm.mapper.PilotApplicationMapper;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private ProjectInfoService projectInfoService;
    @Resource
    private ProductChangeService productChangeService;
    @Autowired
    private BomSkuService bomSkuService;
    @Resource
    private ProductRefLabelService productRefLabelService;
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
        if(!save) {
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
        if(! addDTO.getTaskList().isEmpty()){
            List<PilotApplicationRefTaskEntity> taskEntityList = new ArrayList<>(addDTO.getTaskList().size());
            addDTO.getTaskList().forEach(task -> taskEntityList.add(new PilotApplicationRefTaskEntity().setMainId(pilotApplicationEntity.getId()).setTaskId(task.getTaskId())));
            boolean saveTask = pilotApplicationRefTaskService.saveBatch(taskEntityList);
            if (! saveTask){
                throw new ServiceException("关联任务保存失败");
            }
        }

        return new BaseResultDTO.AddDTO(pilotApplicationEntity.getId(), code);
    }

    /**
     * 保存产品明细
     */
    private void saveProductDetail(PilotApplicationDTO.AddDTO addDTO, PilotApplicationEntity pilotApplicationEntity) {
        if(addDTO.getProductDetailList().isEmpty()){
            throw new ServiceException("产品明细不能为空");
        }
        for (PilotApplicationDetailDTO.AddDTO detailDTO : addDTO.getProductDetailList()) {
            if(StringUtils.isBlank(detailDTO.getMainSupplierId())){
                throw new ServiceException("一级供应商不能为空");
            }
            //实际含税单价
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceSearchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            priceSearchDTO.setSkuId(detailDTO.getSkuId());
            priceSearchDTO.setSkuNo(detailDTO.getSkuNo());
            priceSearchDTO.setSupplierId(detailDTO.getMainSupplierId());
            priceSearchDTO.setPurchaseQty(detailDTO.getApplyQty());
            try{
                List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailFeign.getTaxPrice(priceSearchDTO);
                for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                    if(detailDTO.getApproveQty() >= priceViewDTO.getMinQty() && detailDTO.getApproveQty() <= priceViewDTO.getMaxQty()){
                        detailDTO.setActualTaxCost(priceViewDTO.getTaxPrice());
                        break;
                    }
                }
            }catch (Exception e){
                detailDTO.setActualTaxCost(BigDecimal.ZERO);
            }
            if(detailDTO.getActualTaxCost() == null){
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
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "试产申请"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        // 数据处理
        PilotApplicationEntity pilotApplicationEntity = new PilotApplicationEntity();
        pilotApplicationEntity.setId(updateDTO.getId());
        pilotApplicationEntity.setRemark(updateDTO.getRemark());

        log.info("编辑 开始修改试产申请数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(pilotApplicationEntity);
        if(!save) {
            throw new ServiceException("试产申请保存失败");
        }
        //保存附件
        if(!updateDTO.getAttachmentList().isEmpty()){
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
        if(updateDTO.getProductDetailList().isEmpty()){
            throw new ServiceException("产品明细不能为空");
        }
        if(! StringUtils.isBlank(pilotApplicationEntity.getId())){
            updateDTO.getProductDetailList().forEach(item -> item.setMainId(pilotApplicationEntity.getId()));
        }
        List<String> ids = updateDTO.getProductDetailList().stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        pilotApplicationDetailService.lambdaUpdate().eq(PilotApplicationDetailEntity::getMainId, pilotApplicationEntity.getId()).notIn(PilotApplicationDetailEntity::getId, ids).remove();
        List<PilotApplicationDetailEntity> entityList = BeanMapper.copyList(updateDTO.getProductDetailList(), PilotApplicationDetailEntity.class);
        pilotApplicationDetailService.saveOrUpdateBatch(entityList);
        
        //保存关联任务
        pilotApplicationRefTaskService.lambdaUpdate().eq(PilotApplicationRefTaskEntity::getMainId, pilotApplicationEntity.getId()).remove();
        if(! updateDTO.getTaskList().isEmpty()){
            List<PilotApplicationRefTaskEntity> taskEntityList = new ArrayList<>(updateDTO.getTaskList().size());
            updateDTO.getTaskList().forEach(task -> taskEntityList.add(new PilotApplicationRefTaskEntity().setMainId(pilotApplicationEntity.getId()).setTaskId(task.getTaskId())));
            boolean saveTask = pilotApplicationRefTaskService.saveBatch(taskEntityList);
            if (! saveTask){
                throw new ServiceException("关联任务保存失败");
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PilotApplicationDTO.ListDTO> paging(PagingDTO<PilotApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PilotApplicationDTO.ListDTO> pageData = this.baseMapper.pagingByParam(query, pagingParamDTO.getParams());
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 分页查询
     * @param query 分页参数
     * @param pagingParamDTO 查询参数
     * @param tabFlag tab标签编码
     * @return
     * @date: 2024-08-28
     * @author: tanmujin
     */
    private IPage<PilotApplicationDTO.ListDTO> pagingQuery(Page query, PagingDTO<PilotApplicationDTO.PagingParamDTO> pagingParamDTO, String tabFlag) {
        LoginUser user = UserContext.getNonLoginUser();
        if(PilotApplicationTabEnum.ALL.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), null, null, null);
        }
        if(PilotApplicationTabEnum.WAIT_SUBMIT.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), ApproveStatusEnum.WAIT_SUBMIT.getCode(), null, null);
        }
        if(PilotApplicationTabEnum.WAIT_ME_APPROVE.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), ApproveStatusEnum.APPROVE_ING.getCode(), null, user.getUid());
        }
        if(PilotApplicationTabEnum.REJECT.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), ApproveStatusEnum.REJECT.getCode(), null, null);
        }
        if(PilotApplicationTabEnum.NOT_ORDER.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.NOT_ORDER.getCode(), null);
        }
        if(PilotApplicationTabEnum.ORDER.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.ORDER.getCode(), null);
        }
        if(PilotApplicationTabEnum.STOCK_IN.getCode().equals(tabFlag)){
            return this.baseMapper.paging(query, pagingParamDTO.getParams(), ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.STOCK_IN.getCode(), null);
        }
        throw new ServiceException("tab参数错误");
    }

    @Override
    public List<PilotApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        LoginUser user = UserContext.getNonLoginUser();
        PilotApplicationDTO.PagingParamDTO searchParam = new PilotApplicationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<PilotApplicationDTO.TabListDTO> list = new ArrayList<>();
        int waitSubmitCount = this.baseMapper.tabList(ApproveStatusEnum.WAIT_SUBMIT.getCode(), null, null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.WAIT_SUBMIT.getCode(), PilotApplicationTabEnum.WAIT_SUBMIT.getName(), waitSubmitCount));

        int waitMeApproveCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE_ING.getCode(), null, user.getUid());
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.WAIT_ME_APPROVE.getCode(), PilotApplicationTabEnum.WAIT_ME_APPROVE.getName(), waitMeApproveCount));

        int rejectCount = this.baseMapper.tabList(ApproveStatusEnum.REJECT.getCode(), null, null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.REJECT.getCode(), PilotApplicationTabEnum.REJECT.getName(), rejectCount));

        int notOrderCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.NOT_ORDER.getCode(), null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.NOT_ORDER.getCode(), PilotApplicationTabEnum.NOT_ORDER.getName(), notOrderCount));

        int orderCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.ORDER.getCode(), null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.ORDER.getCode(), PilotApplicationTabEnum.ORDER.getName(), orderCount));

        return list;
    }

    @Override
    public void exportList(PilotApplicationDTO.ExportDTO param, HttpServletResponse response) {
        Optional<AdvanceQueryDTO> tab = param.getAdvanceQueryDTOList().stream().filter(item -> item.getField().equals("tab")).findFirst();
        if(!tab.isPresent()){
            throw new ServiceException("缺少tab参数");
        }
        List<PilotApplicationDTO.ListDTO> list;
        if(!param.getIds().isEmpty()){
            list = this.baseMapper.listExportByIds(param.getIds());
        }else {
            list = queryExportList(param, (String) tab.get().getValue());
        }

        if(CollUtil.isEmpty(list)) {
            log.warn("导出没有查询到数据：{}", param);
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/pilotApplicationExport.xlsx";
        String name = "试产申请导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    /**
     * 查询需要导出的数据
     * @param param
     * @param tabFlag tab标签
     * @return
     * @date: 2024-08-28
     * @author: tanmujin
     */
    private List<PilotApplicationDTO.ListDTO> queryExportList(PilotApplicationDTO.ExportDTO param, String tabFlag) {
        LoginUser user = UserContext.getNonLoginUser();
        if(PilotApplicationTabEnum.ALL.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, null, null, null);
        }
        if(PilotApplicationTabEnum.WAIT_SUBMIT.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, ApproveStatusEnum.WAIT_SUBMIT.getCode(), null, null);
        }
        if(PilotApplicationTabEnum.WAIT_ME_APPROVE.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, ApproveStatusEnum.APPROVE_ING.getCode(), null, user.getUid());
        }
        if(PilotApplicationTabEnum.REJECT.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, ApproveStatusEnum.REJECT.getCode(), null, null);
        }
        if(PilotApplicationTabEnum.NOT_ORDER.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.NOT_ORDER.getCode(), null);
        }
        if(PilotApplicationTabEnum.ORDER.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.ORDER.getCode(), null);
        }
        if(PilotApplicationTabEnum.STOCK_IN.getCode().equals(tabFlag)){
            return this.baseMapper.listExportByParams(param, ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.STOCK_IN.getCode(), null);
        }
        throw new ServiceException("tab参数错误");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        PilotApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到试产申请数据");
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
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        PilotApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //保存数据
        if(approveDTO.getProductDetailList() != null && !approveDTO.getProductDetailList().isEmpty()){
            List<PilotApplicationDetailDTO.ViewDTO> productDetailList = approveDTO.getProductDetailList();
            for (PilotApplicationDetailDTO.ViewDTO productDTO : productDetailList) {
                if(productDTO.getApproveQty() > productDTO.getApplyQty()){
                    throw new ServiceException("产品审核数量不能大于申请数量：" + productDTO.getSkuNo());
                }
                if(Objects.equals(approveType, ApproveTypeEnum.PASS)){
                    pilotApplicationDetailService.lambdaUpdate().set(PilotApplicationDetailEntity::getApproveQty, productDTO.getApproveQty()).eq(PilotApplicationDetailEntity::getId, productDTO.getId()).update();
                }
            }
        }else {
            List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().in(PilotApplicationDetailEntity::getMainId, approveDTO.getIds()).list();
            for (PilotApplicationDetailEntity detailEntity : detailList) {
                Integer applyQty = detailEntity.getApplyQty();
                Integer approveQty = detailEntity.getApproveQty();
                pilotApplicationDetailService.lambdaUpdate().set(PilotApplicationDetailEntity::getApproveQty, approveQty == 0 ? applyQty : approveQty).eq(PilotApplicationDetailEntity::getId, detailEntity.getId()).update();
            }
        }

        // 调用流程审核
        approveProcess(entity, dto, approveDTO);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        updateForApprove(entity.getId(), approveStatus.getStatus());

        //记录日志
        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据审核操作 审核结果：【%s】 审核意见：【%s】", UserContext.getNonLoginUser().getUserName(), entity.getCode(), approveType.getName(), dto.getComment());
        this.addLog(entity.getId(), "审核操作", format, null, null, null);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
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
        Map<String, Object> map = BeanUtil.beanToMap(entity);
        map.put("attachmentList", baseApproveDTO.getAttachmentList());
        approveDTO.setVariablesMap(map);
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
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到试产申请单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        List<PurchaseApplicationEntity> purchaseAppList = purchaseApplicationFeign.listBySourceIds(Collections.singletonList(id));
        if(! purchaseAppList.isEmpty()){
            throw new ServiceException("已下推采购申请单，不允许反审核");
        }
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据反审核", UserContext.getNonLoginUser().getUserName(), entity.getCode());
        this.addLog(id, "反审核", format, "审核状态", entity.getApproveStatus().getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private void addLog(String id, String operation, String content, String field, String oldValue, String newValue){
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
        if(entity.getApproveStatus().compareTo(ApproveStatusEnum.APPROVE) != 0) {
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
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到试产申请数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException("只有待提交或审核不通过数据支持删除");
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
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到试产申请数据"));
        // 只有审核中的单据允许撤销
        if (entity.getApproveStatus().compareTo(ApproveStatusEnum.APPROVE_ING) != 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据撤销流程", UserContext.getNonLoginUser().getUserName(), entity.getCode());
        this.addLog(id, "撤销操作", format, null, null, null);
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getNonLoginUser().getUid());
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
//        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public PilotApplicationDTO.ViewDTO view(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到试产申请数据"));

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
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
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
        //处理产品明细
        for (PilotApplicationDetailDTO.ViewDTO detailDTO : detailViewList) {
            //一级供应商名称
            detailDTO.setMainSupplierName(supplierMap.containsKey(detailDTO.getMainSupplierId()) ? supplierMap.get(detailDTO.getMainSupplierId()).getName() : "");
            //二级供应商名称
            detailDTO.setSecondSupplierName(supplierMap.containsKey(detailDTO.getSecondSupplierId()) ? supplierMap.get(detailDTO.getSecondSupplierId()).getName() : "");
            //目标成本
            Optional<ProductCostEntity> productCostEntityOptional = productCostEntityList.stream().filter(item -> item.getSkuId().equals(detailDTO.getSkuId())).findFirst();
            if(productCostEntityOptional.isPresent()){
                ProductCostEntity productCostEntity = productCostEntityOptional.get();
                detailDTO.setTargetTaxCost(productCostEntity.getTargetTaxCost());
                detailDTO.setTargetNoTaxCost(productCostEntity.getTargetNoTaxCost());
            }
            //实际成本
            fillActualCost(detailDTO);
            //产品名称
            Optional<ProductDetailEntity> skuOptional = skuList.stream().filter(item -> item.getId().equals(detailDTO.getSkuId())).findFirst();
            skuOptional.ifPresent(sku -> {
                detailDTO.setProductName(sku.getName());
                detailDTO.setStatus(sku.getStatus());
                detailDTO.setStatusName(ProductDetailStatusEnum.getName(sku.getStatus()));
            });
        }
        //处理关联任务
        for (PilotApplicationRefTaskDTO.ViewDTO taskDTO : taskViewList) {
            if(! taskMap.containsKey(taskDTO.getTaskId())){
                log.error("试产量产单没有找到任务详情:{} {}",taskDTO.getId(), taskDTO.getTaskId());
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
        try{
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailFeign.getTaxPrice(searchDTO);
            for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                if(detailDTO.getApplyQty() >= priceViewDTO.getMinQty() && detailDTO.getApplyQty() <= priceViewDTO.getMaxQty()){
                    detailDTO.setActualTaxCost(priceViewDTO.getTaxPrice());
                    BigDecimal divide = priceViewDTO.getTaxRate().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal add = divide.add(BigDecimal.ONE);
                    detailDTO.setActualNoTaxCost(priceViewDTO.getTaxPrice().divide(add, 4, RoundingMode.HALF_UP));
                    break;
                }
            }
        }catch (Exception e){
            log.error("没有找到价目表：{} {}", detailDTO.getSkuNo(), detailDTO.getMainSupplierName());
        }
    }

    /**
     * 获取审核记录
     */
    private List<PilotApplicationDTO.AuditorHandleDTO> getApproveProcessList(PilotApplicationEntity pilotApplicationEntity) {
        List<ProcessTaskManagementDTO.ApproveHistoryDTO> approveHistoryList = processTaskManagementFeign.listApproveHistory(pilotApplicationEntity.getId());
        approveHistoryList = approveHistoryList.stream().filter(item -> item.getApproveTime() != null).filter(item -> !item.getRemark().contains("已将任务转移给")).collect(Collectors.toList());
        approveHistoryList.sort(Comparator.comparing(ProcessTaskManagementDTO.CommonDTO::getApproveTime).reversed());
        List<PilotApplicationDTO.AuditorHandleDTO> resultList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ProcessTaskManagementDTO.ApproveHistoryDTO historyDTO : approveHistoryList) {
            PilotApplicationDTO.AuditorHandleDTO auditorHandleDTO = new PilotApplicationDTO.AuditorHandleDTO();
            auditorHandleDTO.setUserId(historyDTO.getApproveUserId());
            auditorHandleDTO.setUserName(historyDTO.getApproveUserName());
            auditorHandleDTO.setResult(historyDTO.getApproveStatus());
            auditorHandleDTO.setResultName(PilotApplicatonApproveHistoryEnum.getName(historyDTO.getApproveStatus()));
            if(historyDTO.getApproveTime() != null){
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
        if(betweenHour > 24){
            desc = cn.hutool.core.date.DateUtil.formatBetween(handleTime, now, BetweenFormatter.Level.DAY);
        }else {
            desc = cn.hutool.core.date.DateUtil.formatBetween(handleTime, now, BetweenFormatter.Level.HOUR);
        }
        return desc + "前";
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        LoginUser userInfo = UserContext.getNonLoginUser();
        this.lambdaUpdate().eq(PilotApplicationEntity::getId, id)
            .set(PilotApplicationEntity::getApproveUserId, userInfo.getUid())
            .set(PilotApplicationEntity::getApproveStatus, approveStatus)
            .set(PilotApplicationEntity::getApproveTime, LocalDateTime.now())
            .update(new PilotApplicationEntity());
     }

    /**
    * 反审核更新审核信息
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
        if(CollUtil.isEmpty(list)) {
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
//        Map<String, String> purchaseIdMap = purchaseApplicationList.stream().collect(Collectors.toMap(item1 -> item1.getSourceId(), item2 -> item2.getId()));
        //采购申请明细
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = new ArrayList<>();
        if(!purchaseIds.isEmpty()){
            purchaseApplicationDetailList = purchaseApplicationDetailFeign.listByMainIds(purchaseIds);
        }
        for(PilotApplicationDTO.ListDTO item : list) {
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
            if(productCostEntityOptional.isPresent()){
                ProductCostEntity productCostEntity = productCostEntityOptional.get();
                item.setTargetTaxCost(productCostEntity.getTargetTaxCost() != null ? productCostEntity.getTargetTaxCost().toPlainString() : "");
            }
            //实际含税单价
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            searchDTO.setPurchaseQty(item.getApplyQty());
            searchDTO.setSupplierId(item.getMainSupplierId());
            searchDTO.setSkuId(item.getSkuId());
            searchDTO.setSkuNo(item.getSkuNo());
            try{
                List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailFeign.getTaxPrice(searchDTO);
                for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                    if(item.getApplyQty() >= priceViewDTO.getMinQty() && item.getApplyQty() <= priceViewDTO.getMaxQty()){
                        item.setActualTaxCost(priceViewDTO.getTaxPrice().toPlainString());
                        break;
                    }
                }
                if(StringUtils.isBlank(item.getActualTaxCost())){
                    item.setActualTaxCost("无价目表");
                }
            }catch (Exception e){
                item.setActualTaxCost("无价目表");
            }
            //采购申请量
            List<PurchaseApplicationEntity> collect = purchaseApplicationList.stream().filter(obj -> obj.getSourceId().equals(item.getId())).collect(Collectors.toList());
            if(!collect.isEmpty()){
                List<String> purchaseAppIds = collect.stream().map(obj -> obj.getId()).collect(Collectors.toList());
                List<PurchaseApplicationDetailEntity> detailEntityList = purchaseApplicationDetailList.stream()
                        .filter(obj -> purchaseAppIds.contains(obj.getPurchaseApplicationId()) && obj.getSkuId().equals(item.getSkuId()))
                        .collect(Collectors.toList());
                int sum = detailEntityList.stream().mapToInt(PurchaseApplicationDetailEntity::getApplyQty).sum();
                item.setPurchaseApplyQty(sum);
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(PilotApplicationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //校验是否有采购价目表
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().in(PilotApplicationDetailEntity::getMainId, entity.getId()).list();
        for (PilotApplicationDetailEntity detailEntity : detailList) {
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceSearchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            priceSearchDTO.setSkuId(detailEntity.getSkuId());
            priceSearchDTO.setSkuNo(detailEntity.getSkuNo());
            priceSearchDTO.setSupplierId(detailEntity.getMainSupplierId());
            priceSearchDTO.setPurchaseQty(detailEntity.getApplyQty());
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList;
            try{
                taxPriceList = purchasePriceDetailFeign.getTaxPrice(priceSearchDTO);
            }catch (Exception e){
                throw new ServiceException("尚未提交供应商采购价目表，请联系采购开发提交后提审: sku：{}，数量：{}", detailEntity.getSkuNo(), detailEntity.getApplyQty());
            }
            boolean flag = false;
            for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO : taxPriceList) {
                if(detailEntity.getApplyQty() >= priceViewDTO.getMinQty() && detailEntity.getApplyQty() <= priceViewDTO.getMaxQty()){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                throw new ServiceException("尚未提交供应商采购价目表，请联系采购开发提交后提审: sku：{}，数量：{}", detailEntity.getSkuNo(), detailEntity.getApplyQty());
            }
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
        List<String> pilotApplicationIds = applicationDTOList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        List<PilotApplicationEntity> entityList = this.lambdaQuery().in(PilotApplicationEntity::getId, pilotApplicationIds).list();
        List<String> detailIds = applicationDTOList.stream().map(item -> item.getDetailId()).distinct().collect(Collectors.toList());
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().in(PilotApplicationDetailEntity::getId, detailIds).list();
        for (PilotApplicationEntity entity : entityList) {
            if(ObjectUtil.notEqual(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)){
                throw new ServiceException("只有已审核的单据允许下推采购申请单");
            }
        }
        for (PilotApplicationDetailEntity detailEntity : detailList) {
            if(ObjectUtil.equal(detailEntity.getOrderStatus(), PilotPushPurchaseStatusEnum.ORDER.getCode())){
                throw new ServiceException("只有未下单或部分下单的SKU允许下推采购申请单");
            }
        }

        //根据单号拆分后下推
        List<BatchResultDTO> resultList = new ArrayList<>();
        Map<String, List<PilotApplicationDTO.PushPurchaseApplicationDTO>> map = applicationDTOList.stream().collect(Collectors.groupingBy(item -> item.getId()));
        for (Map.Entry<String, List<PilotApplicationDTO.PushPurchaseApplicationDTO>> entry : map.entrySet()) {
            PilotApplicationEntity entity = entityList.stream().filter(item -> item.getId().equals(entry.getKey())).findFirst().orElse(new PilotApplicationEntity());
            //下推
            PurchaseApplicationDTO.AddDTO paramDto = getPurchaseApplicationAddDTO(entry.getValue());
            BatchResultDTO resultDTO = purchaseApplicationFeign.add(paramDto);
            //记录日志
            String format = String.format("用户【%s】单号为【%s】的【试产量产单】单据下推采购申请单，单号为：【%s】", UserContext.getNonLoginUser().getUserName(), entity.getCode(), resultDTO.getCode());
            this.addLog(entity.getId(), "下推操作", format, null, null, null);
            resultList.add(resultDTO);
        }
        return resultList;
    }

    private PurchaseApplicationDTO.AddDTO getPurchaseApplicationAddDTO(List<PilotApplicationDTO.PushPurchaseApplicationDTO> applicationDTOList) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Collections.singletonList(loginUser.getUid()));
        List<String> warehouseIds = applicationDTOList.stream().map(item -> item.getToWarehouseId()).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseFeign.listWarehouseByIds(warehouseIds);
        String sourceId = applicationDTOList.get(0).getId();
        String sourceCode = applicationDTOList.get(0).getCode();
        List<String> skuIds = applicationDTOList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());

        List<PurchaseApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (PilotApplicationDTO.PushPurchaseApplicationDTO dto : applicationDTOList) {
            if(dto.getPurchaseApplyQty() > dto.getSpareApplyQty()){
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
            detailList.add(purchaseDTO);
            //更新采购申请数量
            pilotApplicationDetailService.lambdaUpdate()
                    .set(PilotApplicationDetailEntity::getPurchaseApplyQty, dto.getPurchaseApplyQty())
                    .set(PilotApplicationDetailEntity::getOrderStatus, dto.getPurchaseApplyQty() < dto.getSpareApplyQty() ? PilotPushPurchaseStatusEnum.PART_ORDER.getCode() : PilotPushPurchaseStatusEnum.ORDER.getCode())
                    .eq(PilotApplicationDetailEntity::getId, dto.getDetailId())
                    .update();
        }
        PurchaseApplicationDTO.AddDTO paramDto = new PurchaseApplicationDTO.AddDTO();
        paramDto.setApplyDate(LocalDate.now());
        paramDto.setApplyUserId(userList.get(0).getUserId());
        paramDto.setApplyDeptId(userList.get(0).getDepartmentId());

        //是否新品首批
        List<PurchaseApplicationEntity> purchaseApplicationList = purchaseApplicationFeign.listBySourceIds(Collections.singletonList(sourceId));
        if(purchaseApplicationList.isEmpty()){
            paramDto.setIsFirstMassProduct(Boolean.TRUE);
        }else {
            List<String> mainIds = purchaseApplicationList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
            List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailFeign.listByMainIds(mainIds);
            if(purchaseApplicationDetailList.isEmpty()){
                paramDto.setIsFirstMassProduct(Boolean.TRUE);
            }else {
                paramDto.setIsFirstMassProduct(purchaseApplicationDetailList.stream().anyMatch(item -> skuIds.contains(item.getSkuId())) ? Boolean.FALSE : Boolean.TRUE);
            }
        }
        paramDto.setDetails(detailList);
        paramDto.setSourceId(sourceId);
        paramDto.setSourceCode(sourceCode);
        paramDto.setSourceType(SourceTypeEnum.PILOT_APPLICATION.getCode());
        return paramDto;
    }

    private static PurchaseApplicationDTO.AddDTO getPurchaseAddDTO(FindUserDTO userInfo) {
        List<PurchaseApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
//        PurchaseApplicationDetailDTO.AddDTO
        //todo 补充明细
        PurchaseApplicationDTO.AddDTO paramDto = new PurchaseApplicationDTO.AddDTO();
        paramDto.setApplyDate(LocalDate.now());
        paramDto.setApplyUserId(userInfo.getUserId());
        paramDto.setApplyDeptId(userInfo.getDepartmentId());
        paramDto.setIsFirstMassProduct(Boolean.TRUE);
        paramDto.setDetails(detailList);
        return paramDto;
    }

    @Override
    public List<PilotApplicationDTO.PushPurchaseApplicationDTO> viewPurchaseApplication(PilotApplicationDTO.PurchaseApplicationParamDTO paramDTO) {
        //主表
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.listByIds(paramDTO.getDetailIds());
        List<String> ids = detailList.stream().map(PilotApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<PilotApplicationEntity> pilotList = this.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
        Map<String, PilotApplicationEntity> pilotMap = pilotList.stream().collect(Collectors.toMap(BaseEntity::getId, item2 -> item2));
        //sku信息
        List<String> skuIds = detailList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.lambdaQuery().in(ProductDetailEntity::getId, skuIds).list();
        Map<String, ProductDetailEntity> skuMap = skuList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        //采购申请单
        List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> purchaseList = purchaseApplicationFeign.listSkuAndQty(skuIds);
        Map<String, Integer> purchaseMap = purchaseList.stream().collect(Collectors.toMap(item1 -> item1.getSkuId(), item2 -> item2.getQty()));
        List<PurchaseApplicationEntity> purchaseApplicationList = purchaseApplicationFeign.listBySourceIds(ids);
        Map<String, String> purchaseIdMap = purchaseApplicationList.stream().collect(Collectors.toMap(item -> item.getSourceId(), item2 -> item2.getId()));
        //采购申请单明细
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = new ArrayList<>();
        if(!purchaseApplicationList.isEmpty()){
            List<String> mainIds = purchaseApplicationList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
            purchaseApplicationDetailList = purchaseApplicationDetailFeign.listByMainIds(mainIds);
        }


        List<PilotApplicationDTO.PushPurchaseApplicationDTO> resultList = new ArrayList<>(detailList.size());
        for (PilotApplicationDetailEntity detailEntity : detailList) {
            PilotApplicationDTO.PushPurchaseApplicationDTO dto = new PilotApplicationDTO.PushPurchaseApplicationDTO();
            dto.setId(detailEntity.getMainId());
            if(pilotMap.containsKey(detailEntity.getMainId())){
                PilotApplicationEntity entity = pilotMap.get(detailEntity.getMainId());
                dto.setCode(entity.getCode());
            }
            dto.setApproveQty(detailEntity.getApproveQty());
            dto.setDetailId(detailEntity.getId());
            dto.setSkuId(detailEntity.getSkuId());
            if(skuMap.containsKey(detailEntity.getSkuId())){
                ProductDetailEntity sku = skuMap.get(detailEntity.getSkuId());
                dto.setProductName(sku.getName());
                dto.setSkuNo(sku.getSkuNo());
            }
            dto.setType(detailEntity.getType());
            dto.setTypeName(PilotApplicationTypeEnum.getName(detailEntity.getType()));
            if(purchaseMap.containsKey(detailEntity.getSkuId())){
                //待申请量=批准数量-已下推的SKU申请累计申请量（查询采购申请单中该sku已申请的数量）
                String purchaseApplicationId = purchaseIdMap.get(detailEntity.getMainId());
                PurchaseApplicationDetailEntity purchaseDetailEntity = purchaseApplicationDetailList.stream()
                        .filter(item -> item.getPurchaseApplicationId().equals(purchaseApplicationId))
                        .filter(item -> item.getSkuId().equals(detailEntity.getSkuId()))
                        .findFirst().orElse(new PurchaseApplicationDetailEntity().setApplyQty(0));
                int spareApplyQty = detailEntity.getApproveQty() - purchaseDetailEntity.getApplyQty();
                dto.setSpareApplyQty(spareApplyQty);
            }else {
                dto.setSpareApplyQty(detailEntity.getApproveQty());
            }
            resultList.add(dto);
        }
        return resultList.stream().filter(item -> item.getSpareApplyQty() > 0).collect(Collectors.toList());
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
        List<PilotApplicationRefTaskEntity> list = pilotApplicationRefTaskService.lambdaQuery().eq(PilotApplicationRefTaskEntity::getMainId, id).list();
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

        List<String> supplierIdList = list.stream().filter(obj -> StringUtils.isNotBlank(obj.getMainSupplier())).map(ProductSearchDTO.SkuListDTO::getMainSupplier).distinct().collect(Collectors.toList());
        //供应商名称
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
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
//            ProductSearchDTO.SkuListDTO finalSkuListDTO = skuListDTO;
//            String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(finalSkuListDTO.getMainSupplier())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//            skuListDTO.setMainSupplierName(supplierName);
            Optional<ProductCostEntity> productCostEntityOptional = productCostEntityList.stream().filter(item -> item.getSkuId().equals(skuNo2IdMap.get(skuNo))).findFirst();
            if(productCostEntityOptional.isPresent()){
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
    public BatchResultDTO pushAndSubmitPurchaseApplication(List<PilotApplicationDTO.PushPurchaseApplicationDTO> dtoList) {
        PurchaseApplicationDTO.AddDTO paramDto = getPurchaseApplicationAddDTO(dtoList);
        return purchaseApplicationFeign.addAndSubmit(paramDto);
    }
}
