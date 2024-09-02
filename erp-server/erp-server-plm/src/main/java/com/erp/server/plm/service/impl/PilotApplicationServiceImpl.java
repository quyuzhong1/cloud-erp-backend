package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.tms.enums.PilotApplicationTabEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.PurchaseApplicationFeign;
import com.erp.rpc.wms.feign.SupplierFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.plm.mapper.PilotApplicationMapper;
import com.erp.server.plm.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
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

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PilotApplicationDTO.AddDTO addDTO) {
        PilotApplicationEntity pilotApplicationEntity = new PilotApplicationEntity();
        BeanMapperUtils.copy(addDTO, pilotApplicationEntity);

        // 数据处理
        handleData(pilotApplicationEntity);

        log.info("开始新增试产申请");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SCLC);
        pilotApplicationEntity.setCode(code);
        boolean save = super.save(pilotApplicationEntity);
        if(!save) {
            throw new ServiceException("试产申请保存失败");
        }

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
            detailDTO.setMainId(pilotApplicationEntity.getId());
            //todo skuID
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
        PilotApplicationEntity pilotApplicationEntity =  BeanMapperUtils.map(PilotApplicationEntity.class, updateDTO);

        // 数据处理
        handleData(pilotApplicationEntity);
        log.info("编辑 开始修改试产申请数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(pilotApplicationEntity);
        if(!save) {
            throw new ServiceException("试产申请保存失败");
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
        Optional<AdvanceQueryDTO> tab = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().filter(item -> item.getField().equals("tab")).findFirst();
        if(!tab.isPresent()){
            throw new ServiceException("缺少tab参数");
        }
        IPage<PilotApplicationDTO.ListDTO> pageData = pagingQuery(query, pagingParamDTO, (String) tab.get().getValue());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
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
        int allCount = this.baseMapper.tabList(null, null, null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.ALL.getCode(), PilotApplicationTabEnum.ALL.getName(), allCount));

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

        int stockInCount = this.baseMapper.tabList(ApproveStatusEnum.APPROVE.getCode(), PilotApplicationTabEnum.STOCK_IN.getCode(), null);
        list.add(new PilotApplicationDTO.TabListDTO(PilotApplicationTabEnum.STOCK_IN.getCode(), PilotApplicationTabEnum.STOCK_IN.getName(), stockInCount));

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
        // 记录操作日志
        log.info("提交 开始记录试产申请日志数据，id：【{}】", id);
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
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        PilotApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(PilotApplicationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
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
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(PilotApplicationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到试产申请数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        pilotApplicationDetailService.lambdaUpdate().eq(PilotApplicationDetailEntity::getMainId, entity.getId()).remove();
        pilotApplicationRefTaskService.lambdaUpdate().eq(PilotApplicationRefTaskEntity::getMainId, entity.getId()).remove();
        // 删除主单数据
        log.info("删除 开始删除试产申请主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除试产申请日志数据，id：【{}】", id);
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
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);
        log.info("撤销 开始修改试产申请状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
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
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public PilotApplicationDTO.ViewDTO view(String id) {
        PilotApplicationEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到试产申请数据"));
        List<PilotApplicationDetailEntity> productDetailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, id).list();
        List<PilotApplicationRefTaskEntity> taskList = pilotApplicationRefTaskService.lambdaQuery().eq(PilotApplicationRefTaskEntity::getMainId, id).list();

        // 数据填充处理
        PilotApplicationDTO.ViewDTO view = fillOne(entity, productDetailList, taskList);
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

    private PilotApplicationDTO.ViewDTO fillOne(PilotApplicationEntity pilotApplicationEntity, List<PilotApplicationDetailEntity> productDetailList, List<PilotApplicationRefTaskEntity> refTaskList) {
        if (ObjectUtil.isEmpty(pilotApplicationEntity)) {
            return null;
        }
        //产品任务
        List<String> taskIds = refTaskList.stream().map(item -> item.getTaskId()).distinct().collect(Collectors.toList());
        List<ProjectTaskDTO.SimpleViewDTO> taskList = projectTaskService.listSimpleViewByIds(taskIds);
        Map<String, ProjectTaskDTO.SimpleViewDTO> taskMap = taskList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));

        PilotApplicationDTO.ViewDTO view = BeanMapperUtils.map(PilotApplicationDTO.ViewDTO.class, pilotApplicationEntity);
        List<PilotApplicationDetailDTO.ViewDTO> detailViewList = BeanMapper.copyList(productDetailList, PilotApplicationDetailDTO.ViewDTO.class);
        List<PilotApplicationRefTaskDTO.ViewDTO> taskViewList = BeanMapper.copyList(taskList, PilotApplicationRefTaskDTO.ViewDTO.class);
        //处理产品明细
        for (PilotApplicationDetailDTO.ViewDTO detailDTO : detailViewList) {
            detailDTO.setMainSupplierName("");
            detailDTO.setSecondSupplierName("");
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
            //todo 填充关联任务信息
            taskDTO.setProductName(entity.getProductName());
            taskDTO.setSkuId("");
            taskDTO.setSkuNo("");
            taskDTO.setSpu(entity.getSpuNo());
            taskDTO.setStatus(entity.getStatus());
            taskDTO.setStatusName(TaskStateEnum.getName(entity.getStatus()));
        }
        view.setApproveStatusName(ApproveStatusEnum.getName(view.getApproveStatus()));
        view.setProductDetailList(detailViewList);
        view.setTaskList(taskViewList);
        //什么记录
        List<ApproveNodeRecordVO> approveHistoryList = workflowFeign.listHistoryTaskByProcessId(pilotApplicationEntity.getProcessId());
        view.setApproveFlowList(approveHistoryList);
        //操作日志
        SysLogSelectDTO sysLogSelectDTO = new SysLogSelectDTO();
        sysLogSelectDTO.setBusinessId(pilotApplicationEntity.getId());
        List<SysLogShowDTO> sysLogShowList = sysLogService.listSysLog(sysLogSelectDTO);
        view.setOperateLogList(sysLogShowList);
        return view;
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

        for(PilotApplicationDTO.ListDTO item : list) {
            item.setApproveStatusName(ApproveStatusEnum.getName(item.getApproveStatus()));
            item.setOrderStatusName(PilotApplicationTabEnum.getName(item.getOrderStatus()));
            item.setProductName(productDetailMap.get(item.getSkuId()));
            item.setMainSupplierName(supplierMap.containsKey(item.getMainSupplierId()) ? supplierMap.get(item.getMainSupplierId()).getName() : "");
            item.setApproveUserName(userMap.get(item.getApproveUserId()));
            item.setCreateUserName(userMap.get(item.getCreateUserId()));
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
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PilotApplicationEntity pilotApplicationEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public BatchResultDTO pushPurchaseApplication(PilotApplicationDTO.PushPurchaseApplicationDTO dto, FindUserDTO userInfo) {
        PurchaseApplicationDTO.AddDTO paramDto = getPurchaseAddDTO(userInfo);
        return purchaseApplicationFeign.add(paramDto);
    }

    private static PurchaseApplicationDTO.AddDTO getPurchaseAddDTO(FindUserDTO userInfo) {
        List<PurchaseApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
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
            if(purchaseMap.containsKey(detailEntity.getSkuId())){
                //待申请量=批准数量-已下推的SKU申请累计申请量（查询采购申请单中该sku已申请的数量）
                Integer qty = purchaseMap.get(detailEntity.getSkuId());
                int spareApplyQty = detailEntity.getApproveQty() - qty;
                dto.setSpareApplyQty(spareApplyQty);
            }else {
                dto.setSpareApplyQty(detailEntity.getApproveQty());
            }
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    public BatchResultDTO pushAndSubmitPurchaseApplication(PilotApplicationDTO.PushPurchaseApplicationDTO dto, FindUserDTO findUserDTO) {
        PurchaseApplicationDTO.AddDTO paramDto = getPurchaseAddDTO(findUserDTO);
        return purchaseApplicationFeign.addAndSubmit(paramDto);
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
        return Collections.emptyList();
    }
}
