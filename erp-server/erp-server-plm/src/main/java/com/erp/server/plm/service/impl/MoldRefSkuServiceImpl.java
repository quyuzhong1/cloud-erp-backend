package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.dto.excel.MoldRefSkuImportExcelDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.listener.MoldInfoExcelListener;
import com.erp.server.plm.listener.MoldRefSkuExcelListener;
import com.erp.server.plm.service.MoldInfoService;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.ProductDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.erp.server.plm.mapper.MoldRefSkuMapper;
import com.erp.server.plm.service.MoldRefSkuService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 模具关联sku 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-14
 */
@Slf4j
@Service
public class MoldRefSkuServiceImpl extends SuperServiceImpl<MoldRefSkuMapper, MoldRefSkuEntity> implements MoldRefSkuService {
    @Resource
    private OperateLogService sysLogService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private MoldInfoService moldInfoService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Override
    public PagingVO<MoldRefSkuDTO.ListDTO> paging(PagingDTO<MoldRefSkuDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<MoldRefSkuDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<MoldRefSkuDTO.TabListDTO> tabList(PermissionsDTO param) {
        MoldRefSkuDTO.PagingParamDTO searchParam = new MoldRefSkuDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<MoldRefSkuDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();

        List<MoldRefSkuDTO.TabListDTO> result = new ArrayList<>();
        result.add(new MoldRefSkuDTO.TabListDTO("all","全部", 0));
        for (String status : statusList) {
            MoldRefSkuDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldRefSkuDTO.TabListDTO(status, "", 0));
            tabListDTO.setTabFlagName(ApproveStatusEnum.getName(status));
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public void exportList(MoldRefSkuDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具关联SKU导出", EXPORT_PLM_MOLD_REF_SKU.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        MoldRefSkuEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到模具关联sku数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改模具关联sku状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动模具关联sku流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录模具关联sku日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        sysLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        MoldRefSkuEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku", approveType.getName(), dto.getComment());
        sysLogService.addSysLogBySave(msg, "", entity.getId(), "");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(MoldRefSkuEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.MOLD_REF_SKU.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
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

    private Map<String,Object> getVariablesMap(MoldRefSkuEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.MOLD_REF_SKU.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        MoldRefSkuEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具关联sku单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        sysLogService.addSysLogBySave(msg, "", entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(MoldRefSkuEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        MoldRefSkuEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具关联sku数据"));
        // 只有待提交或审核不通过数据支持删除
        if (!(Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.REJECT.getStatus()))) {
            throw new ServiceException(ApiError.ERROR_DELETE);
        }

        // 删除主单数据
        log.info("删除 开始删除模具关联sku主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除模具关联sku日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        sysLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        MoldRefSkuEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具关联sku数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改模具关联sku状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        sysLogService.addSysLogBySave(msg, "", id, "");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.MOLD_REF_SKU.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, MoldRefSkuEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }


    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(MoldRefSkuEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.MOLD_REF_SKU.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(MoldRefSkuDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(MoldRefSkuEntity::getId, id)
            .set(MoldRefSkuEntity::getApproveUserId, userInfo.getUid())
            .set(MoldRefSkuEntity::getApproveUserName, userInfo.getUserName())
            .set(MoldRefSkuEntity::getApproveStatus, approveStatus)
            .set(MoldRefSkuEntity::getApproveTime, LocalDateTime.now())
            .update(new MoldRefSkuEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(MoldRefSkuEntity::getId, id)
            .set(MoldRefSkuEntity::getApproveUserId, "")
            .set(MoldRefSkuEntity::getApproveUserName, "")
            .set(MoldRefSkuEntity::getApproveStatus, approveStatus)
            .set(MoldRefSkuEntity::getApproveTime, null)
            .update(new MoldRefSkuEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(MoldRefSkuEntity::getId, id)
        .set(MoldRefSkuEntity::getApproveStatus, approveStatus)
        .update(new MoldRefSkuEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<MoldRefSkuDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.MOLD_INFO.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        // 属性赋值
        for(MoldRefSkuDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(MoldRefSkuEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 修改单模产量
     * @author jack
     * @date:  2025-10-14
     * @param dto
     * @return ApiResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOutputQtyById(MoldInfoDTO.UpdateQty dto) {
        MoldRefSkuEntity old = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到模具关联sku数据"));
        // 只有待提交数据允许删除
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, old.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, old.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        MoldRefSkuEntity entity = new MoldRefSkuEntity();
        BeanMapper.copy(old,entity);

        entity.setOutputQty(dto.getQty());
        updateById(entity);

        // 记录主单操作日志
        log.info("编辑 开始记录模具关联SKU日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "模具关联SKU");
        sysLogService.addSysLogByUpdate(old,entity,String.valueOf(MoldRefSkuEntity.class), old.getId(), "", msg);
        return true;
    }
    /**
     * 修改用量
     * @author jack
     * @date:  2025-10-14
     * @param dto
     * @return ApiResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSkuQtyById(MoldInfoDTO.UpdateQty dto) {
        MoldRefSkuEntity old = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到模具关联sku数据"));
        // 只有待提交数据允许删除
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, old.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, old.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        MoldRefSkuEntity entity = new MoldRefSkuEntity();
        BeanMapper.copy(old,entity);

        entity.setSkuQty(dto.getQty());
        updateById(entity);

        // 记录主单操作日志
        log.info("编辑 开始记录模具关联SKU日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "模具关联SKU");
        sysLogService.addSysLogByUpdate(old,entity,String.valueOf(MoldRefSkuEntity.class), old.getId(), "", msg);
        return true;
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入模具关联SKU", IMPORT_PLM_MOLD_REF_SKU.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importMoldRefSku(BaseDTO.ImportDTO dto) {
        //已审核且未作废的模具关联SKU
        List<MoldInfoEntity> moldInfoEntities = moldInfoService.lambdaQuery()
                .eq(MoldInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
                .eq(MoldInfoEntity::getInvalidStatus, Boolean.FALSE)
                .eq(MoldInfoEntity::getIsDeleted, Boolean.FALSE)
                .list();
        Map<String, MoldInfoEntity> moldInfoMap = moldInfoEntities.stream().collect(Collectors.toMap(MoldInfoEntity::getCode, Function.identity(), (o1, o2) -> o1));

        //已审核的 ,非资产属性的 SKU
        List<SkuVO> skuList = productDetailService.searchSku(null);
        Map<String, SkuVO> skuMap = skuList.stream().filter(e -> StringUtils.isNotBlank(e.getPropertyName()) && !Objects.equals(e.getPropertyName(), ProductConstant.PRODUCT_PROPERTY_ASSET))
                .collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity(), (o1, o2) -> o1));

        //所有模具关联SKU记录
        List<MoldRefSkuEntity> allList = list();
        // 生成 moldCode + skuNo 的 Set
        Set<String> moldCodeSkuNoSet = allList.stream()
                .map(entity -> entity.getMoldCode() + ":" + entity.getSkuNo())
                .collect(Collectors.toSet());

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter( e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        MoldRefSkuExcelListener excelListenerUtil = new MoldRefSkuExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),moldCodeSkuNoSet,skuMap,moldInfoMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), MoldRefSkuImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<MoldRefSkuImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "模具关联SKU错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, MoldRefSkuImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(List<MoldRefSkuImportExcelDTO> successList, List<MoldRefSkuImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        MoldRefSkuServiceImpl bean = ApplicationContextUtils.getBean(MoldRefSkuServiceImpl.class);

        List<MoldRefSkuEntity> moldRefSkuEntities = BeanMapper.copyList(successList, MoldRefSkuEntity.class);

        if(CollUtil.isNotEmpty(moldRefSkuEntities)){
            bean.saveBatch(moldRefSkuEntities);
            // 操作日志
            List<OperateLogEntity> sysLogEntityList = new LinkedList<>();
            for (MoldRefSkuEntity moldRefSkuEntity : moldRefSkuEntities) {
                sysLogEntityList.add(new OperateLogEntity().setContent(StrUtil.format("新增模具【{}】关联SKU【{}】", moldRefSkuEntity.getMoldCode(),moldRefSkuEntity.getSkuNo())).setBusinessId(moldRefSkuEntity.getId()));
            }
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
        }
    }

}
