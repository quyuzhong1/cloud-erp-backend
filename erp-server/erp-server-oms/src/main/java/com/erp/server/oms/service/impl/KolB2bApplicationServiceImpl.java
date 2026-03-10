package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.KolB2bApplicationImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.listener.KolB2bApplicationExcelListener;
import com.erp.server.oms.mapper.KolB2bApplicationMapper;
import com.erp.server.oms.query.KolB2bApplicationQueryHandler;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * B2B寄样申请主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolB2bApplicationServiceImpl extends SuperServiceImpl<KolB2bApplicationMapper, KolB2bApplicationEntity> implements KolB2bApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private KolB2bApplicationDetailService kolB2bApplicationDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private SoDetailService soDetailService;
    @Resource
    private KolFeedbackService kolFeedbackService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private OmsAttachmentService omsAttachmentService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SoInfoService soInfoService;
    @Resource
    private KolB2bApplicationQueryHandler kolB2bApplicationQueryHandler;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private CustomerAddressService customerAddressService;



    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolB2bApplicationDTO.AddDTO addDTO) {
        KolB2bApplicationEntity kolB2bApplicationEntity = new KolB2bApplicationEntity();
        BeanMapperUtils.copy(addDTO, kolB2bApplicationEntity);

        // 数据处理
        handleData(kolB2bApplicationEntity);

        log.info("开始新增B2B寄样申请主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KOLB);
        kolB2bApplicationEntity.setCode(code);
        boolean save = super.save(kolB2bApplicationEntity);
        if(!save) {
            throw new ServiceException("B2B寄样申请主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B寄样申请主单" , kolB2bApplicationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), kolB2bApplicationEntity.getId(), "新增操作");

        //添加明细
        kolB2bApplicationDetailService.add(addDTO.getDetailList(), kolB2bApplicationEntity.getId());
        //保存附件
        Class<KolB2bApplicationEntity> entityClass = KolB2bApplicationEntity.class;
        TableName tableName = entityClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        omsAttachmentService.batchSaveOrUpdate(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), type, kolB2bApplicationEntity.getId());
        return new BaseResultDTO.AddDTO(kolB2bApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolB2bApplicationDTO.UpdateDTO addOrUpdateDTO) {
        KolB2bApplicationEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2B寄样申请主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        if (InvalidStatusEnum.VOIDED.getStatus().equals(old.getInvalidStatus())) {
            throw new ServiceException(ApiError.BILL_VOID_EDIT_FORBIDDEN);
        }

        KolB2bApplicationEntity kolB2bApplicationEntity =  BeanMapperUtils.map(KolB2bApplicationEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolB2bApplicationEntity);
        log.info("编辑 开始修改B2B寄样申请主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kolB2bApplicationEntity);
        if(!save) {
            throw new ServiceException("B2B寄样申请主单保存失败");
        }
        //更新明细信息
        kolB2bApplicationDetailService.update(addOrUpdateDTO.getDetailList(), kolB2bApplicationEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录B2B寄样申请主单日志数据，单号：【{}】", kolB2bApplicationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "B2B寄样申请主单");
        operateLogService.addModuleOperateLogByObj(old, kolB2bApplicationEntity, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), kolB2bApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<KolB2bApplicationDTO.ListDTO> paging(PagingDTO<KolB2bApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<Object> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<KolB2bApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<KolB2bApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        KolB2bApplicationDTO.PagingParamDTO searchParam = new KolB2bApplicationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        KolB2bApplicationTableEnum[] values = KolB2bApplicationTableEnum.values();
        List<KolB2bApplicationDTO.TabListDTO> list = new ArrayList<>();
        Arrays.stream(values).forEach(item -> {
            searchParam.setPermissionSql(param.getPermissionSql());
            KolB2bApplicationDTO.TabListDTO resultDTO = new KolB2bApplicationDTO.TabListDTO();
            String tabSql = kolB2bApplicationQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParam.setSqlMap(map);
            Integer count;
            if (KolB2bApplicationTableEnum.WAIT_SHIPPED.equals(item) || KolB2bApplicationTableEnum.SHIPPED.equals(item)) {
                count = this.baseMapper.tabDetailList(searchParam);
            } else {
                count = this.baseMapper.tabList(searchParam);
            }
            resultDTO.setCount(ObjectUtil.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        });
        return list;
    }

    @Override
    public void exportList(KolB2bApplicationDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("B2B寄样申请导出", FileTaskEventEnum.EXPORT_OMS_KOL_B2B_APPLICATION_REPORT.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        KolB2bApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2B寄样申请主单数据");
        }
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.BILL_VOIDED_CANNOT_SUBMIT);
        }

        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改B2B寄样申请主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动B2B寄样申请主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录B2B寄样申请主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄样申请主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(KolB2bApplicationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(KolB2bApplicationDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        KolB2bApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄样申请主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(KolB2bApplicationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄样申请主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄样申请主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    /**
     * 反审核验证
     * @author will
     * @date 2025/12/2 17:57
     * @param entity
     * @return Boolean
     */
    private Boolean validateDisApprove(KolB2bApplicationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        //校验是否已经下推B2B销售订单
        List<SoInfoEntity> list = soInfoService.listBySourceId(entity.getId());
        if (CollUtil.isNotEmpty(list)) {
            String codes = list.stream().map(SoInfoEntity::getCode).distinct().collect(Collectors.joining(","));
            throw new ServiceException("单据已下推B2B销售订单，订单号：【" + codes + "】，不允许反审核");
        }
        //校验是否已经下推回片登记
        List<KolFeedbackEntity> feedbackList = kolFeedbackService.listBySourceId(entity.getId());
        if (CollUtil.isNotEmpty(feedbackList)) {
            String codes = feedbackList.stream().map(KolFeedbackEntity::getPartnerNickname).distinct().collect(Collectors.joining(","));
            throw new ServiceException("单据已下推回片登记，达人昵称：【" + codes + "】，不允许反审核");
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄样申请主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.BILL_VOIDED_CANNOT_DELETE);
        }

        // 删除主单数据
        log.info("删除 开始删除B2B寄样申请主单主单数据，id：【{}】", id);
        super.removeById(id);

        //删除明细数据
        kolB2bApplicationDetailService.deleteByMainId(id);

        // 删除日志数据
        log.info("删除 开始删除B2B寄样申请主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄样申请主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除B2B寄样申请主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄样申请主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getCode()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getCode()))
                || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.BILL_VOID_ALLOWED_STATUS_ONLY);
        }
        log.info("作废 开始修改B2B寄样申请主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
            .set(KolB2bApplicationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(KolB2bApplicationEntity::getInvalidRemark, remark)
            .set(KolB2bApplicationEntity::getInvalidTime, LocalDateTime.now())
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄样申请主单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "作废操作");
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
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄样申请主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改B2B寄样申请主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄样申请主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        revokeDTO.setSourcePlatform(dto.getSourcePlatform());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, KolB2bApplicationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {

        KolB2bApplicationExcelListener excelListenerUtil = new KolB2bApplicationExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), KolB2bApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        }
        //验证导入数据是否为空
        List<KolB2bApplicationImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        //导入数据处理
        List<KolB2bApplicationImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<KolB2bApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportSuccessList(successList, errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/kolB2bApplicationError.xlsx";
        String name = "kolB2bApplicationError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.FILE_EXPORT_ERROR_DATA_FAILED);
        }
        return Boolean.TRUE;
    }

    @Override
    public KolB2bApplicationDTO.RefBillDTO listRefBill(String id) {
        KolB2bApplicationDTO.RefBillDTO refBillDTO = new KolB2bApplicationDTO.RefBillDTO();
        //寄样申请单信息
        KolB2bApplicationEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_FOUND);
        }
        //寄样申请单明细信息
        List<KolB2bApplicationDetailEntity> detailList = kolB2bApplicationDetailService.listByMainIdList(Collections.singletonList(id));
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
        }
        //销售订单信息
        List<KolB2bApplicationDTO.B2bSoInfoDTO> b2bSoInfoDTOList = soInfoService.listRefBill(id);
        if (CollUtil.isEmpty(b2bSoInfoDTOList)) {
            return refBillDTO;
        }
        //产品信息
        List<String> skuIdList = b2bSoInfoDTOList.stream().map(KolB2bApplicationDTO.B2bSoInfoDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String,String> skuMap = CollUtil.isEmpty(skuList) ? new HashMap<>() :
                skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));

        //仓库信息
        List<String> warehosueIdList = b2bSoInfoDTOList.stream().map(KolB2bApplicationDTO.B2bSoInfoDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehosueList = FeignQuery.getByIds(WarehouseEntity.class, warehosueIdList);
        Map<String,String> warehosueMap = CollUtil.isEmpty(warehosueList) ? new HashMap<>() :
                warehosueList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        for (KolB2bApplicationDTO.B2bSoInfoDTO b2bSoInfoDTO :b2bSoInfoDTOList) {
            //发货状态名称
            b2bSoInfoDTO.setDeliveryStatusName(DeliveryStatusEnum.getName(b2bSoInfoDTO.getDeliveryStatus()));
            //审核状态名称
            b2bSoInfoDTO.setApproveStatusName(ApproveStatusEnum.getName(b2bSoInfoDTO.getApproveStatus()));
            //产品名称
            b2bSoInfoDTO.setProductName(skuMap.get(b2bSoInfoDTO.getSkuId()));
            //仓库名称
            b2bSoInfoDTO.setWarehouseName(warehosueMap.get(b2bSoInfoDTO.getWarehouseId()));
        }
        refBillDTO.setSoInfoList(b2bSoInfoDTOList);
        return refBillDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoInfo(ValidList<KolB2bApplicationDTO.GenerateSoInfoDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<KolB2bApplicationDTO.GenerateSoInfoDTO> listList = list.getList();
        Map<String, KolB2bApplicationDTO.GenerateSoInfoDTO> paramMap = listList.stream().collect(Collectors.toMap(KolB2bApplicationDTO.GenerateSoInfoDTO::getDetailId, obj -> obj));

        //B2B寄样申请单明细信息
        List<String> detailIdList = listList.stream().map(KolB2bApplicationDTO.GenerateSoInfoDTO::getDetailId).distinct().collect(Collectors.toList());
        List<KolB2bApplicationDetailEntity> detailList = kolB2bApplicationDetailService.listByIds(detailIdList);
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
        }

        //B2B寄样申请单主单信息
        List<String> mainIdList = detailList.stream().map(KolB2bApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<KolB2bApplicationEntity> mainList = this.listByIds(mainIdList);
        if (CollUtil.isEmpty(mainList)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_FOUND);
        }
        Map<String, KolB2bApplicationEntity> mainMap = mainList.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, obj -> obj));

        //客户信息
        List<String> customerIdList = mainList.stream().map(KolB2bApplicationEntity::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = customerInfoService.listByIds(customerIdList);
        Map<String, CustomerInfoEntity> customerInfoMap = customerInfoList.stream().collect(Collectors.toMap(CustomerInfoEntity::getId, obj -> obj));


        //校验
        String codes = mainList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getApproveStatus().getCode(), ApproveStatusEnum.APPROVE.getStatus()))
                .map(KolB2bApplicationEntity::getCode).collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(codes)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_APPROVED,codes);
        }
        //销售订单明细
        List<SoDetailEntity> oldSoDetailList = soDetailService.listBySourceDetailIdList(detailIdList);


        Map<String, List<KolB2bApplicationDetailEntity>> detailMap = detailList.stream().collect(Collectors.groupingBy(KolB2bApplicationDetailEntity::getMainId));
        for (Map.Entry<String, List<KolB2bApplicationDetailEntity>> entry : detailMap.entrySet()) {
            String key = entry.getKey();

            //B2B寄样申请单主表数据
            KolB2bApplicationEntity mainEntity = mainMap.get(key);
            if (ObjectUtil.isEmpty(mainEntity)) {
                throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_FOUND);
            }
            //B2B寄样申请单明细表数据
            List<KolB2bApplicationDetailEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
            }


            //客户信息
            CustomerInfoEntity customerInfoEntity = customerInfoMap.get(mainEntity.getCustomerId());
            if (ObjectUtil.isEmpty(customerInfoEntity)) {
                throw new ServiceException(ApiError.CUSTOMER_NOT_FOUND, mainEntity.getCustomerId());
            }

            List<String> thisDetailIdList = value.stream().map(KolB2bApplicationDetailEntity::getId).distinct().collect(Collectors.toList());
            long warehouseCount = list.stream().filter(obj -> thisDetailIdList.contains(obj.getDetailId()))
                    .map(KolB2bApplicationDTO.GenerateSoInfoDTO::getWarehouseId).distinct().count();
            if (warehouseCount > 1) {
                throw new ServiceException(ApiError.SAMPLE_PUSH_WAREHOUSE_MISMATCH, mainEntity.getCode());
            }
            long orgCount = list.stream().filter(obj -> thisDetailIdList.contains(obj.getDetailId()))
                    .map(KolB2bApplicationDTO.GenerateSoInfoDTO::getSoOrgId).distinct().count();
            if (orgCount > 1) {
                throw new ServiceException(ApiError.SAMPLE_PUSH_SALES_ORG_MISMATCH, mainEntity.getCode());
            }

            SoInfoDTO.AddDTO addDTO = new SoInfoDTO.AddDTO();
            BeanUtil.copyProperties(mainEntity,addDTO);
            addDTO.setId(null);
            addDTO.setOrderType(BillTypeEnum.AFTER_SALES.getCode());
            addDTO.setTransactionSubType(OrderSubTypeEnum.INFLUENCER_SAMPLE.getCode());
            LocalDate now = LocalDate.now();
            addDTO.setBillDate(now);
            addDTO.setRequireDate(now);
            addDTO.setSourceId(mainEntity.getId());
            addDTO.setSourceCode(mainEntity.getCode());
            addDTO.setSourceType(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
            addDTO.setSellerId(mainEntity.getApplyUserId());
            addDTO.setSalesDeptId(mainEntity.getApplyDeptId());
            addDTO.setCustomerId(mainEntity.getCustomerId());
            addDTO.setCurrency(customerInfoEntity.getCurrency());
            addDTO.setIsTax(Boolean.FALSE);
            List<SoDetailDTO.AddDTO> soDetailList = new ArrayList<>();
            for (KolB2bApplicationDetailEntity detailEntity : value) {
                KolB2bApplicationDTO.GenerateSoInfoDTO generateSoInfoDTO = paramMap.get(detailEntity.getId());
                if (ObjectUtil.isEmpty(generateSoInfoDTO)) {
                    throw new ServiceException(ApiError.SAMPLE_PUSH_DETAIL_ID_NOT_FOUND,detailEntity.getId());
                }
                //查看对于明细的销售订单明细是否已存在
                oldSoDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), detailEntity.getId()))
                        .findFirst().ifPresent(obj -> {
                    throw new ServiceException(ApiError.SAMPLE_B2B_DETAIL_ALREADY_PUSHED_SO,mainEntity.getCode(),detailEntity.getSkuNo());
                });

                //主表赋值，取明细其一即可
                addDTO.setSalesOrgId(generateSoInfoDTO.getSoOrgId());
                addDTO.setWarehouseId(generateSoInfoDTO.getWarehouseId());

                SoDetailDTO.AddDTO soDetailAddDTO = new SoDetailDTO.AddDTO();
                BeanUtil.copyProperties(detailEntity,soDetailAddDTO);
                soDetailAddDTO.setId(null);
                soDetailAddDTO.setSourceDetailId(detailEntity.getId());
                soDetailList.add(soDetailAddDTO);
            }
            addDTO.setDetailList(soDetailList);
            soInfoService.add(addDTO);
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean generateFeedback(ValidList<KolB2bApplicationDTO.GenerateFeedbackDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<KolB2bApplicationDTO.GenerateFeedbackDTO> listList = list.getList();

        //B2B寄样申请单明细信息
        List<String> detailIdList = listList.stream().map(KolB2bApplicationDTO.GenerateFeedbackDTO::getDetailId).distinct().collect(Collectors.toList());
        List<KolB2bApplicationDetailEntity> detailList = kolB2bApplicationDetailService.listByIds(detailIdList);
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
        }
        Map<String, KolB2bApplicationDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(KolB2bApplicationDetailEntity::getId, obj -> obj));


        //B2B寄样申请单主单信息
        List<String> mainIdList = detailList.stream().map(KolB2bApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<KolB2bApplicationEntity> mainList = this.listByIds(mainIdList);
        if (CollUtil.isEmpty(mainList)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_FOUND);
        }
        Map<String, KolB2bApplicationEntity> mainMap = mainList.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, obj -> obj));

        //校验
        String codes = mainList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getApproveStatus().getCode(), ApproveStatusEnum.APPROVE.getStatus()))
                .map(KolB2bApplicationEntity::getCode).collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(codes)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_APPROVED,codes);
        }
        for (KolB2bApplicationDTO.GenerateFeedbackDTO feedbackDTO : list) {
            KolFeedbackDTO.AddDTO addDTO = new KolFeedbackDTO.AddDTO();

            //B2B寄样申请单明细表数据
            KolB2bApplicationDetailEntity detailEntity = detailMap.get(feedbackDTO.getDetailId());
            if (ObjectUtil.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
            }

            //B2B寄样申请单主表数据
            KolB2bApplicationEntity mainEntity = mainMap.get(detailEntity.getMainId());
            if (ObjectUtil.isEmpty(mainEntity)) {
                throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_NOT_FOUND);
            }
            BeanUtil.copyProperties(feedbackDTO,addDTO);
            addDTO.setSourceId(mainEntity.getId());
            addDTO.setSourceCode(mainEntity.getCode());
            addDTO.setSourceType(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
            addDTO.setSourceDetailId(detailEntity.getId());
            addDTO.setSkuId(detailEntity.getSkuId());
            addDTO.setQty(detailEntity.getQty());
            kolFeedbackService.add(addDTO);
        }
        return Boolean.TRUE;
    }

    @Override
    public void handleImportSuccessList(List<KolB2bApplicationImportExcelDTO> successList, List<KolB2bApplicationImportExcelDTO> errorList) {
        //无成功数据直接返回
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, String> userMap = CollUtil.isEmpty(userList) ? new HashMap<>() :
                userList.stream().collect(Collectors.toMap(FindUserDTO::getUserName, FindUserDTO::getUserId));
        //客户
        List<String> customerList = successList.stream().map(KolB2bApplicationImportExcelDTO::getCustomerName).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = customerInfoService.listByNameList(customerList);
        Map<String, String> customerMap = CollUtil.isEmpty(customerInfoList) ? new HashMap<>() :
                customerInfoList.stream().collect(Collectors.toMap(CustomerInfoEntity::getName, CustomerInfoEntity::getId));

        //产品信息
        List<String> skuNoList = successList.stream().map(KolB2bApplicationImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        Map<String, String> skuVOMap = CollUtil.isEmpty(skuVOList) ? new HashMap<>() :
                skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId));

        //按序号分组
        Map<String, List<KolB2bApplicationImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(KolB2bApplicationImportExcelDTO::getNo));
        for (Map.Entry<String, List<KolB2bApplicationImportExcelDTO>> entry : collect.entrySet()) {
            List<KolB2bApplicationImportExcelDTO> list = entry.getValue();
            KolB2bApplicationImportExcelDTO mainInfo = list.get(0);
            List<String> errorMsgList = new ArrayList<>();

            //客户信息
            String customerId = customerMap.get(mainInfo.getCustomerName());
            if (CharSequenceUtil.isBlank(customerId)) {
                errorMsgList.add(StrUtil.format("客户名称【{}】不存在", mainInfo.getCustomerName()));
            }
            //申请人信息
            String applyUserId = userMap.get(mainInfo.getApplyUserName());
            if (CharSequenceUtil.isBlank(applyUserId)) {
                errorMsgList.add(StrUtil.format("申请人名称【{}】不存在", mainInfo.getApplyUserName()));
            }
            //申请部门信息
            String applyDeptId = deptList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(),mainInfo.getApplyDeptName())).map(SysDepartmentDTO::getId).findFirst().orElse("");
            if (CharSequenceUtil.isBlank(applyDeptId) && CharSequenceUtil.isNotBlank(mainInfo.getApplyDeptName())) {
                errorMsgList.add(StrUtil.format("申请部门名称【{}】不存在", mainInfo.getApplyDeptName()));
            }
            //产品信息
            String skuId = skuVOMap.get(mainInfo.getSkuNo());
            if (CharSequenceUtil.isBlank(skuId))  {
                errorMsgList.add(StrUtil.format("产品编码【{}】不存在", mainInfo.getSkuNo()));
            }

            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                mainInfo.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(mainInfo);
                continue;
            }
            KolB2bApplicationDTO.AddDTO addDTO = new KolB2bApplicationDTO.AddDTO();
            BeanUtil.copyProperties(mainInfo,addDTO);
            addDTO.setCustomerId(customerId);
            addDTO.setApplyUserId(applyUserId);
            addDTO.setApplyDeptId(applyDeptId);
            addDTO.setDate(LocalDateUtil.stringToLocalDateTime(mainInfo.getDateStr()).toLocalDate());
            //明细信息
            List<KolB2bApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (KolB2bApplicationImportExcelDTO detailInfo : list) {
                KolB2bApplicationDetailDTO.AddDTO detailDTO = new KolB2bApplicationDetailDTO.AddDTO();
                BeanUtil.copyProperties(detailInfo, detailDTO);
                detailDTO.setSkuId(skuId);
                detailDTO.setPlanFeedbackDate(CharSequenceUtil.isBlank(detailInfo.getPlanFeedbackDateStr()) ? null : LocalDateUtil.stringToLocalDateTime(detailInfo.getPlanFeedbackDateStr()).toLocalDate());
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);
            try {
                ApplicationContextUtils.getBean(KolB2bApplicationServiceImpl.class).add(addDTO);
            } catch (Exception e) {
                list.forEach(obj -> obj.setErrorMsg("1、" + e.getMessage()));
                errorList.addAll(list);
            }
        }
    }

    @Override
    public KolB2bApplicationDTO.ViewDTO view(String id) {
        KolB2bApplicationEntity kolB2bApplicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到B2B寄样申请主单数据"));
        KolB2bApplicationDTO.ViewDTO data = BeanMapperUtils.map(KolB2bApplicationDTO.ViewDTO.class, kolB2bApplicationEntity);
        data.setApproveStatus(kolB2bApplicationEntity.getApproveStatus().getStatus());
        data.setApproveStatusName(kolB2bApplicationEntity.getApproveStatus().getName());
        // 数据填充处理
        fillOne(data);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(KolB2bApplicationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(KolB2bApplicationEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<KolB2bApplicationDetailEntity> detailList = kolB2bApplicationDetailService.listByMainIdList(Collections.singletonList(entity.getId()));
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));

        //客户名称
        CustomerInfoEntity customerInfo = customerInfoService.getById(entity.getCustomerId());
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            variablesMap.put("customerName", customerInfo.getName());
        }

        //部门名称
        List<SysDepartmentEntity> sysDepartmentList = sysUserFeign.listDeptByIds(Collections.singletonList(entity.getApplyDeptId()));
        if (CollUtil.isNotEmpty(sysDepartmentList)) {
            variablesMap.put("applyDeptName", sysDepartmentList.get(0).getName());
        }
        //申请人名称
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getApplyUserId());
        if (ObjectUtil.isNotEmpty(findUserDTO)) {
            variablesMap.put("applyUserName", findUserDTO.getUserName());
        }
        //收货地址名称
        CustomerAddressEntity addressEntity = customerAddressService.getById(entity.getReceiveAddressId());
        if (ObjectUtil.isNotEmpty(addressEntity)) {
            variablesMap.put("receiveAddressName",addressEntity.getAddress());
        }
        //地址类型名称
        variablesMap.put("addressTypeName", CustomerAddressTypeEnum.getName(entity.getAddressType()));

        return variablesMap;
    }

    /**
     * 单个处理数据填充
     * @author will
     * @date 2025/12/2 17:35
     * @param data
     * @return void
     */
    private void fillOne(KolB2bApplicationDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

        //客户名称
        CustomerInfoEntity customerInfo = customerInfoService.getById(data.getCustomerId());
        if (ObjectUtil.isEmpty(customerInfo)) {
            throw new ServiceException(ApiError.CUSTOMER_NOT_FOUND);
        }
        data.setCustomerName(customerInfo.getName());

        //申请人名称
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(data.getApplyUserId());
        if (ObjectUtil.isNotEmpty(findUserDTO)) {
            data.setApplyUserName(findUserDTO.getUserName());
        }
        //申请部门名称
        List<SysDepartmentEntity> sysDepartmentList = sysUserFeign.listDeptByIds(Collections.singletonList(data.getApplyDeptId()));
        if (CollUtil.isNotEmpty(sysDepartmentList)) {
            data.setApplyDeptName(sysDepartmentList.get(0).getName());
        }
        //地址类型名称
        data.setAddressTypeName(CustomerAddressTypeEnum.getName(data.getAddressType()));
        //查询附件信息
        //获取到附件信息
        List<OmsAttachmentDTO.UpdateDTO> attachmentList = omsAttachmentService.getByBusinessIds(Collections.singletonList(data.getId()));
        //附件地址
        List<String> attachmentUrlList = attachmentList.stream()
                .map(OmsAttachmentDTO.UpdateDTO::getAttachUrl)
                .collect(Collectors.toList());
        //附件名称
        List<String> attachmentNameList = attachmentList.stream()
                .map(OmsAttachmentDTO.UpdateDTO::getAttachName).
                collect(Collectors.toList());
        data.setAttachUrlList(attachmentUrlList);
        data.setAttachNameList(attachmentNameList);

        //查询明细数据
        List<KolB2bApplicationDetailEntity> detailList = kolB2bApplicationDetailService.listByMainIdList(Collections.singletonList(data.getId()));
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SAMPLE_B2B_APPLICATION_DETAIL_NOT_FOUND);
        }

        List<String> skuIdList = detailList.stream().map(KolB2bApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, SkuVO> skuMap = CollUtil.isEmpty(skuVOList) ? new HashMap<>() :
                skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, skuVO -> skuVO));

        List<KolB2bApplicationDetailDTO.ViewDTO> detailDTOList = BeanMapperUtils.copyList(KolB2bApplicationDetailDTO.ViewDTO.class, detailList);
        for (KolB2bApplicationDetailDTO.ViewDTO viewDTO : detailDTOList) {
            //sku信息
            SkuVO skuVO = skuMap.get(viewDTO.getSkuId());
            if (ObjectUtil.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
            }
            viewDTO.setProductName(skuVO.getSkuName());
            viewDTO.setBrandName(skuVO.getBrandName());
        }
        data.setDetailList(detailDTOList);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
            .set(KolB2bApplicationEntity::getApproveUserId, userInfo.getUid())
            .set(KolB2bApplicationEntity::getApproveUserName, userInfo.getUserName())
            .set(KolB2bApplicationEntity::getApproveStatus, approveStatus)
            .set(KolB2bApplicationEntity::getApproveTime, LocalDateTime.now())
            .update(new KolB2bApplicationEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
            .set(KolB2bApplicationEntity::getApproveUserId, "")
            .set(KolB2bApplicationEntity::getApproveUserName, "")
            .set(KolB2bApplicationEntity::getApproveStatus, approveStatus)
            .set(KolB2bApplicationEntity::getApproveTime, null)
            .update(new KolB2bApplicationEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
        .set(KolB2bApplicationEntity::getApproveStatus, approveStatus)
        .update(new KolB2bApplicationEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<KolB2bApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.KOL_B2B_APPLICATION.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.HTTP_UNKNOWN.getCode(), listApiResult.getMsg()));
            }
        }

        //产品信息
        List<String> skuIdList = list.stream().map(KolB2bApplicationDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String,String> skuMap = CollUtil.isEmpty(skuList) ? new HashMap<>() :
                skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String,String> userMap = CollUtil.isEmpty(userList) ? new HashMap<>() :
                userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        Map<String,String> deptMap = CollUtil.isEmpty(deptList) ? new HashMap<>() :
                deptList.stream().collect(Collectors.toMap(SysDepartmentDTO::getId, SysDepartmentDTO::getName));

        //销售订单信息
        List<String> detailIdList = list.stream().map(KolB2bApplicationDTO.ListDTO::getDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soDetailService.listBySourceDetailIdList(detailIdList);
        Map<String,SoDetailEntity> soDetailMap = CollUtil.isEmpty(soDetailList) ? new HashMap<>() :
                soDetailList.stream().collect(Collectors.toMap(SoDetailEntity::getSourceDetailId, soDetailEntity -> soDetailEntity));

        //销售出库单
        List<String> soIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockFeign.listDetailBySoIds(soIdList);

        //回片信息
       List<KolFeedbackDTO.FeedbackQtyDTO> feedbackQtyList =  kolFeedbackService.listFeedbackQtyBySourceDetailIdList(detailIdList);
        Map<String,KolFeedbackDTO.FeedbackQtyDTO> feedbackQtyMap = CollUtil.isEmpty(feedbackQtyList) ? new HashMap<>() :
                feedbackQtyList.stream().collect(Collectors.toMap(KolFeedbackDTO.FeedbackQtyDTO::getSourceDetailId, feedbackQtyDTO -> feedbackQtyDTO));
        // 属性赋值
        for(KolB2bApplicationDTO.ListDTO data : list) {
            //状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));

            //产品名称
            data.setProductName(skuMap.get(data.getSkuId()));
            //申请人名称
            data.setApplyUserName(userMap.get(data.getApplyUserId()));
            //申请部门名称
            data.setApplyDeptName(deptMap.get(data.getApplyDeptId()));
            //发货状态名称 默认未发货，下面赋值
            data.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
            //项目标签
            if (CollUtil.isNotEmpty(data.getProjectTag())) {
                data.setProjectTags(StrUtil.join(",",data.getProjectTag()));
            }
            //销售订单
            String b2bRefStatusName = KolB2bRefStatusEnum.WAIT_GENERATE.getName();
            SoDetailEntity soDetailEntity = soDetailMap.get(data.getDetailId());
            if (ObjectUtil.isNotEmpty(soDetailEntity)) {
                if (CharSequenceUtil.equals(soDetailEntity.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())) {
                    b2bRefStatusName = KolB2bRefStatusEnum.APPROVED.getName();
                } else {
                    b2bRefStatusName = KolB2bRefStatusEnum.WAIT_APPROVE.getName();
                }

                //运单号集合
                List<String> trackNoList = soOutstockDetailList.stream().filter(s -> s.getSoId().equals(soDetailEntity.getMainId())
                                && StringUtils.isNotBlank(s.getTrackNo())).
                        map(SoOutstockDetailEntity::getTrackNo).distinct().collect(Collectors.toList());
                data.setTrackNo(String.join(",", trackNoList));

                //销售出库单已出库数量
                Integer outstockQty = soOutstockDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) && CharSequenceUtil.equals(obj.getSoDetailId(), soDetailEntity.getId()))
                        .map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                data.setOutstockQty(outstockQty);
                data.setDeliveryStatusName(DeliveryStatusEnum.getName(soDetailEntity.getDeliveryStatus()));
            }
            data.setB2bRefStatusName(b2bRefStatusName);

            //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                List<ProcessManagementDTO.CurApproveInfoDTO> curApproveList = listApiResult.getData().stream()
                        .filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(curApproveList)) {
                    String curApproveName = curApproveList.stream()
                            .map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName)
                            .collect(Collectors.joining(","));
                    if (StringUtils.isNotBlank(curApproveName)) {
                        data.setApproveUserName(curApproveName);
                    }
                }
            }

            //回片信息
            KolFeedbackDTO.FeedbackQtyDTO feedbackQtyDTO = feedbackQtyMap.get(data.getDetailId());
            if (ObjectUtil.isNotEmpty(feedbackQtyDTO)) {
                data.setFeedbackQty(feedbackQtyDTO.getFeedbackQty());
                data.setCaptureFeedbackQty(feedbackQtyDTO.getCaptureFeedbackQty());
                data.setFeedbackUrl(feedbackQtyDTO.getFeedbackUrl());
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(KolB2bApplicationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolB2bApplicationEntity kolB2bApplicationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
