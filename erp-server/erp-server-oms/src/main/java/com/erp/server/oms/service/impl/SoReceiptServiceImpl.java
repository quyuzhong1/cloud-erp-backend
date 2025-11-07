package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.PlatformReceiptDTO;
import com.common.business.dto.PlatformReceiptDetailDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.SoReceiptDTO;
import com.erp.model.oms.dto.SoReceiptDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.SoReceiptSourceTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.dht.SyncDhtService;
import com.erp.server.oms.mapper.SoReceiptMapper;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_RECEIPT;

/**
 * <p>
 * 收款单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@Service
public class SoReceiptServiceImpl extends SuperServiceImpl<SoReceiptMapper, SoReceiptEntity> implements SoReceiptService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private SoReceiptDetailService soReceiptDetailService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SyncDhtService syncDhtService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private BankAccountService bankAccountService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoReceiptDTO.AddDTO addDTO) {
        SoReceiptEntity soReceiptEntity = new SoReceiptEntity();
        BeanMapperUtils.copy(addDTO, soReceiptEntity);

        List<SoReceiptDetailDTO.AddDTO> detailList = addDTO.getDetailList();

        //校验明细关联的销售订单组织跟主记录的组织是否一致
        List<String> soIds = detailList.stream().map(SoReceiptDetailDTO.AddDTO::getSoId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(soIds)){
            List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(soIds);
            Set<String> salesOrgIdSet = soInfoEntityList.stream().map(SoInfoEntity::getSalesOrgId).collect(Collectors.toSet());
            if(salesOrgIdSet.size() > 1 || !salesOrgIdSet.contains(soReceiptEntity.getSalesOrgId())){
                throw new ServiceException("明细关联的销售订单组织必须跟收款单的组织一致");
            }
        }


        if(Objects.isNull(addDTO.getReceiptAmount())){
            BigDecimal totalAmount = detailList.stream().map(SoReceiptDetailDTO.AddDTO::getReceiptAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            soReceiptEntity.setReceiptAmount(totalAmount);
        }
        log.info("开始新增收款单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SKD);
        soReceiptEntity.setCode(code);
        boolean save = super.save(soReceiptEntity);
        if(!save) {
            throw new ServiceException("收款单保存失败");
        }
        soReceiptDetailService.addDetail(soReceiptEntity,addDTO.getDetailList());

        // 保存附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<AttachDTO> list = addDTO.getAttachmentList();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(v->v.setBusinessId(soReceiptEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(list, tableName.value());
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "收款单" , soReceiptEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RECEIPT.getCode(), soReceiptEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(soReceiptEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoReceiptDTO.UpdateDTO addOrUpdateDTO) {
        SoReceiptEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "收款单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        //第三方平台的不允许在ERP修改
        if(old.getSourceType().equals(SoReceiptSourceTypeEnum.THIRD.getCode()) && !addOrUpdateDTO.getIsFromPlatform()){
            throw new ServiceException("第三方平台的收款单不允许在ERP修改");
        }
        if(!old.getCustomerId().equals(addOrUpdateDTO.getCustomerId())){
            throw new ServiceException("不允许修改客户");
        }
        if(!old.getSalesOrgId().equals(addOrUpdateDTO.getSalesOrgId())){
            throw new ServiceException("不允许修改组织");
        }

        SoReceiptEntity soReceiptEntity =  BeanMapperUtils.map(SoReceiptEntity.class, addOrUpdateDTO);

        // 数据处理
        List<SoReceiptDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        //校验明细关联的销售订单组织跟主记录的组织是否一致
        List<String> soIds = detailList.stream().map(SoReceiptDetailDTO.UpdateDTO::getSoId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(soIds)){
            List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(soIds);
            Set<String> salesOrgIdSet = soInfoEntityList.stream().map(SoInfoEntity::getSalesOrgId).collect(Collectors.toSet());
            if(salesOrgIdSet.size() > 1 || !salesOrgIdSet.contains(soReceiptEntity.getSalesOrgId())){
                throw new ServiceException("明细关联的销售订单组织必须跟收款单的组织一致");
            }
        }
        //求和总收款金额
        BigDecimal totalAmount;

        //更新明细
        soReceiptDetailService.updateDetail(soReceiptEntity,addOrUpdateDTO.getDetailList(),addOrUpdateDTO.isFromSoUpdate());
        if(addOrUpdateDTO.isFromSoUpdate()){
            List<SoReceiptDetailEntity> soReceiptDetailEntityList = soReceiptDetailService.listByMainIds(Collections.singletonList(soReceiptEntity.getId()));
            totalAmount = soReceiptDetailEntityList.stream().map(SoReceiptDetailEntity::getReceiptAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        }else{
            totalAmount = detailList.stream().map(SoReceiptDetailDTO.UpdateDTO::getReceiptAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if(Objects.isNull(addOrUpdateDTO.getReceiptAmount())){
            soReceiptEntity.setReceiptAmount(totalAmount);
        }
        boolean save = super.updateById(soReceiptEntity);
        if(!save) {
            throw new ServiceException("收款单保存失败");
        }
        // 保存附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<AttachDTO> list = addOrUpdateDTO.getAttachmentList();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(v->v.setBusinessId(soReceiptEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(list, tableName.value());
        }

        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), "收款单");
        operateLogService.addModuleOperateLogByObj(old, soReceiptEntity, ModuleTypeEnum.SO_RECEIPT.getCode(), soReceiptEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SoReceiptDTO.ListDTO> paging(PagingDTO<SoReceiptDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReceiptDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReceiptDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoReceiptDTO.PagingParamDTO searchParam = new SoReceiptDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SoReceiptDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        //待我审核
        //根据单据id查询审核流程
        LoginUser user = UserContext.getNonLoginUser();
        int waitMeApproveCount = 0;
        ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
        dto.setBusinessKey(SourceTypeEnum.SO_RECEIPT.getCode());
        dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        dto.setCurApproveId(user.getUid());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
        if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            waitMeApproveCount = lambdaQuery().eq(SoReceiptEntity::getId, ids).count();
        }

        //生效状态
        Map<String, SoReceiptDTO.TabListDTO> map = list.stream().collect(Collectors.toMap(SoReceiptDTO.TabListDTO::getTabFlag, t -> t));
        List<SoReceiptDTO.TabListDTO> result = new ArrayList<>();

        result.add(new SoReceiptDTO.TabListDTO( ApproveStatusEnum.WAIT_SUBMIT.getCode(), ApproveStatusEnum.WAIT_SUBMIT.getName() , map.get(ApproveStatusEnum.WAIT_SUBMIT.getCode()) == null ? 0 : map.get(ApproveStatusEnum.WAIT_SUBMIT.getCode()).getCount()));
        result.add(new SoReceiptDTO.TabListDTO( ApproveStatusEnum.APPROVE_ING.getCode(), ApproveStatusEnum.APPROVE_ING.getName() , waitMeApproveCount));
        result.add(new SoReceiptDTO.TabListDTO( ApproveStatusEnum.APPROVE.getCode(), ApproveStatusEnum.APPROVE.getName() , map.get(ApproveStatusEnum.APPROVE.getCode()) == null ? 0 : map.get(ApproveStatusEnum.APPROVE.getCode()).getCount()));
        result.add(new SoReceiptDTO.TabListDTO( ApproveStatusEnum.REJECT.getCode(), ApproveStatusEnum.REJECT.getName() , map.get(ApproveStatusEnum.REJECT.getCode()) == null ? 0 : map.get(ApproveStatusEnum.REJECT.getCode()).getCount()));
        return result;
    }

    @Override
    public boolean exportList(SoReceiptDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("收款单", EXPORT_OMS_SO_RECEIPT.getCode(), param);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SoReceiptEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到收款单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format("用户【{}】【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), "收款单");

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RECEIPT.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SoReceiptDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SoReceiptDTO.UpdateDTO dto) {
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
        SoReceiptEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), "收款单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RECEIPT.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SoReceiptEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_RECEIPT.getCode());
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
        SoReceiptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到收款单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        List<SoReceiptDetailEntity> detailEntityList = soReceiptDetailService.listByMainIds(Collections.singletonList(entity.getId()));

        //更新销售订单的收款金额
        List<String> soIds = detailEntityList.stream().map(SoReceiptDetailEntity::getSoId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        soInfoService.updateSoReceiptAmount(soIds);

        // 操作日志
        String msg = StrUtil.format("用户【{}】【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(),  "收款单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RECEIPT.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SoReceiptEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        //第三方平台的不允许在ERP修改
        if(entity.getSourceType().equals(SoReceiptSourceTypeEnum.THIRD.getCode())){
            throw new ServiceException("第三方平台的收款单不允许反审核");
        }
        List<SoReceiptDetailEntity> detailEntityList = soReceiptDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        List<String> soIds = detailEntityList.stream().map(SoReceiptDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(soIds);
        //校验订单是否全部为待提交或审核不通过
        for (SoInfoEntity soInfoEntity : soInfoEntityList) {
            if (!BillApproveStatusEnum.WAIT_SUBMIT.equals(soInfoEntity.getApproveStatus())
             && !BillApproveStatusEnum.REJECT.equals(soInfoEntity.getApproveStatus())) {
                throw new ServiceException(StrUtil.format("订单【{}】状态为【{}】，不允许进行反审核操作", soInfoEntity.getCode(), soInfoEntity.getApproveStatus().getName()));
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SoReceiptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到收款单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        // 删除主单数据
        super.removeById(id);
        soReceiptDetailService.removeByMainId(id);
        String msg = StrUtil.format("用户【{}】【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "收款单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RECEIPT.getCode(), entity.getCode(), "删除收款单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        SoReceiptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到收款单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        String msg = StrUtil.format("用户【{}】【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), "收款单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RECEIPT.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_RECEIPT.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        ApiResult<ProcessManagementDTO.RevokeResultDTO> result = workflowFeign.revokeProcess(revokeDTO);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoReceiptEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        if(Objects.equals(ApproveTypeEnum.PASS.getStatus(), dto.getType())){
            List<SoReceiptDetailEntity> detailEntityList = soReceiptDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            //更新销售订单的收款金额
            List<String> soIds = detailEntityList.stream().map(SoReceiptDetailEntity::getSoId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            soInfoService.updateSoReceiptAmount(soIds);
            //创建推送订货通任务
            syncDhtService.createSyncReceiptTaskToDht(entity,SyncOperateEnum.OPERATE_APPROVE.getCode());
        }

        return Boolean.TRUE;
    }

    @Override
    public List<SoReceiptDTO.SoInfoAndReceiptDTO> listSoReceiptBySoCode(SoReceiptDTO.SoSearchDTO dto) {
        if(StringUtils.isBlank(dto.getCustomerId())){
            throw new ServiceException("客户不能为空");
        }
        List<SoReceiptDTO.SoInfoAndReceiptDTO> soInfoAndReceiptDTOList = baseMapper.listSoReceiptBySoCode(dto);
        soInfoAndReceiptDTOList.forEach(v->v.setApproveStatusName(ApproveStatusEnum.getName(v.getApproveStatus())));
        if(CollectionUtils.isNotEmpty(dto.getSoCodeList())){
            List<SoReceiptDTO.SoInfoAndReceiptDTO> result = new ArrayList<>();
            for (String soCode : dto.getSoCodeList()) {
                SoReceiptDTO.SoInfoAndReceiptDTO soInfoAndReceiptDTO = soInfoAndReceiptDTOList.stream().filter(v -> v.getSoCode().equals(soCode)).findFirst().orElse(new SoReceiptDTO.SoInfoAndReceiptDTO());
                result.add(soInfoAndReceiptDTO);
            }
            return result;
        }

        return soInfoAndReceiptDTOList;
    }

    @Override
    public List<SoReceiptDTO.AmountDTO> queryAmountBySoIds(List<String> soIds) {

        if(CollectionUtils.isNotEmpty(soIds)){
            return baseMapper.queryAmountBySoIds(soIds);
        }
        return Collections.emptyList();
    }

    @Override
    public List<SoReceiptDTO.SoViewDTO> getSoViewDTO(SoInfoEntity soInfo) {
        List<SoReceiptDTO.SoViewDTO> soViewDTOList = baseMapper.getSoViewDTO(soInfo.getId());

        //查询主记录附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<String> ids = soViewDTOList.stream().map(SoReceiptDTO.SoViewDTO::getId).collect(Collectors.toList());
        List<OmsAttachmentEntity> omsAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(ids,tableName.value());

        //查询明细记录附件
        TableName detailTableName = SoReceiptDetailEntity.class.getDeclaredAnnotation(TableName.class);
        List<String> detailIds = soViewDTOList.stream().map(SoReceiptDTO.SoViewDTO::getDetailId).collect(Collectors.toList());
        List<OmsAttachmentEntity> detailAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(detailIds,detailTableName.value());

        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());

        List<BankAccountEntity> bankAccountList = new ArrayList<>();
        List<String> receiveAccountList = soViewDTOList.stream().map(SoReceiptDTO.SoViewDTO::getReceiptAccount).filter(org.apache.commons.lang3.StringUtils::isNotBlank).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(receiveAccountList)) {
            bankAccountList = bankAccountService.listByIds(receiveAccountList);
        }

        for (SoReceiptDTO.SoViewDTO soViewDTO : soViewDTOList) {
            List<OmsAttachmentEntity> attachList = omsAttachmentEntities.stream().filter(v -> v.getBusinessId().equals(soViewDTO.getId())).collect(Collectors.toList());
            List<AttachDTO> attachDTOList = BeanMapper.copyList(attachList, AttachDTO.class);
            soViewDTO.setAttachmentList(attachDTOList);

            List<OmsAttachmentEntity> detailAttachList = detailAttachmentEntities.stream().filter(v -> v.getBusinessId().equals(soViewDTO.getDetailId())).collect(Collectors.toList());
            List<AttachDTO> detailAttachDTOList = BeanMapper.copyList(detailAttachList, AttachDTO.class);
            soViewDTO.setDetailAttachmentList(detailAttachDTOList);

            DictBasicEntity receiveMethod  = receiveMethodList.stream().filter(v -> v.getValue().equals(soViewDTO.getDictReceiptMethod())).findFirst().orElse(null);
            if(ObjectUtil.isNotEmpty(receiveMethod)) {
                soViewDTO.setDictReceiptMethodName(receiveMethod.getName());
            }

            BankAccountEntity bankAccountEntity = bankAccountList.stream().filter(b -> CharSequenceUtil.equals(b.getId(), soViewDTO.getReceiptAccount())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bankAccountEntity)) {
                soViewDTO.setReceiptAccountName(bankAccountEntity.getAccountName());
            }
            soViewDTO.setApproveStatusName(ApproveStatusEnum.getName(soViewDTO.getApproveStatus()));
        }
        return soViewDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateBySo(SoInfoEntity soInfo, String customerId, List<SoReceiptDTO.SoViewDTO> soReceiptDTOList) {
        if(Objects.isNull(soReceiptDTOList)){
            soReceiptDTOList = new ArrayList<>();
        }
        //查询原有
        List<SoReceiptDTO.SoViewDTO> oldList = this.getSoViewDTO(soInfo);
        List<String> oldDetailIds = oldList.stream().map(SoReceiptDTO.SoViewDTO::getDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReceiptDTO.SoViewDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> newDetailIds = soReceiptDTOList.stream().map(SoReceiptDTO.SoViewDTO::getDetailId).filter(StrUtils::isNotEmpty).collect(Collectors.toList());
        List<SoReceiptDetailEntity> soReceiptDetailEntityList = CollectionUtil.isNotEmpty(oldDetailIds)? soReceiptDetailService.listByIds(oldDetailIds):new ArrayList<>();
        List<SoReceiptEntity> soReceiptEntityList = CollectionUtil.isNotEmpty(oldIds)?this.listByIds(oldIds):new ArrayList<>();
        //新增
        List<SoReceiptDTO.SoViewDTO> addList = soReceiptDTOList.stream().filter(v -> StrUtils.isEmpty(v.getDetailId())).collect(Collectors.toList());
        for (SoReceiptDTO.SoViewDTO add : addList) {
            SoReceiptDTO.AddDTO addDTO = new SoReceiptDTO.AddDTO();
            addDTO.setCustomerId(customerId);
            addDTO.setCurrency(soInfo.getCurrency());
            addDTO.setIsPosted(false);
            addDTO.setSourceType(SoReceiptSourceTypeEnum.SO_INFO.getCode());
            addDTO.setAttachmentList(add.getAttachmentList());
            addDTO.setDictReceiptMethod(add.getDictReceiptMethod());
            addDTO.setReceiptAccount(add.getReceiptAccount());
            addDTO.setReceiptDate(add.getReceiptDate());
            addDTO.setSalesOrgId(soInfo.getSalesOrgId());
            addDTO.setSourceType(SoReceiptSourceTypeEnum.SO_INFO.getCode());
            SoReceiptDetailDTO.AddDTO detailAddDTO = new SoReceiptDetailDTO.AddDTO();
            detailAddDTO.setSoId(soInfo.getId());
            detailAddDTO.setSoCode(soInfo.getCode());
            detailAddDTO.setSourceDetailId(soInfo.getId());
            detailAddDTO.setPaymentNo(add.getPaymentNo());
            detailAddDTO.setAttachmentList(add.getDetailAttachmentList());
            detailAddDTO.setRemark(add.getRemark());
            detailAddDTO.setReceiptAmount(add.getReceiptAmount());
            addDTO.setDetailList(Arrays.asList(detailAddDTO));
            this.add(addDTO);
        }
        //更新
        List<SoReceiptDTO.SoViewDTO> updateList = soReceiptDTOList.stream().filter(v -> StrUtils.isNotEmpty(v.getDetailId()) && oldDetailIds.contains(v.getDetailId())).collect(Collectors.toList());
        for (SoReceiptDTO.SoViewDTO update : updateList) {
            SoReceiptDetailEntity soReceiptDetailEntity = soReceiptDetailEntityList.stream().filter(v -> v.getId().equals(update.getDetailId())).findFirst().orElseThrow(() -> new ServiceException("未找到收款单明细数据"));
            SoReceiptEntity soReceiptEntity = soReceiptEntityList.stream().filter(v -> v.getId().equals(soReceiptDetailEntity.getMainId())).findFirst().orElseThrow(() -> new ServiceException("未找到收款单数据"));
            if(!ApproveStatusEnum.allowUpdateStatus(soReceiptEntity.getApproveStatus())) {
                throw new ServiceException("收款单状态不允许修改");
            }
            SoReceiptDTO.UpdateDTO updateDTO = new SoReceiptDTO.UpdateDTO();
            updateDTO.setId(update.getId());
            updateDTO.setAttachmentList(update.getAttachmentList());
            updateDTO.setFromSoUpdate(true);
            updateDTO.setCustomerId(customerId);
            updateDTO.setReceiptDate(update.getReceiptDate());
            updateDTO.setDictReceiptMethod(update.getDictReceiptMethod());
            updateDTO.setReceiptAccount(update.getReceiptAccount());
            updateDTO.setSourceType(SoReceiptSourceTypeEnum.SO_INFO.getCode());
            updateDTO.setSalesOrgId(soInfo.getSalesOrgId());
            SoReceiptDetailDTO.UpdateDTO detailUpdateDTO = new SoReceiptDetailDTO.UpdateDTO();
            detailUpdateDTO.setId(update.getDetailId());
            detailUpdateDTO.setReceiptAmount(update.getReceiptAmount());
            detailUpdateDTO.setPaymentNo(update.getPaymentNo());
            detailUpdateDTO.setRemark(update.getRemark());
            detailUpdateDTO.setAttachmentList(update.getDetailAttachmentList());
            updateDTO.setDetailList(Arrays.asList(detailUpdateDTO));
            this.update(updateDTO);
        }
        //删除
        List<String> delDetailIds = oldDetailIds.stream().filter(v -> !newDetailIds.contains(v)).collect(Collectors.toList());
        List<SoReceiptDetailEntity> deleteDetailList = soReceiptDetailEntityList.stream().filter(v -> delDetailIds.contains(v.getId())).collect(Collectors.toList());
        List<String> delIds = deleteDetailList.stream().map(SoReceiptDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoReceiptEntity> handleDelEntityList = soReceiptEntityList.stream().filter(v -> delIds.contains(v.getId())).collect(Collectors.toList());
        for (SoReceiptDetailEntity deleteDetailEntity : deleteDetailList) {
            SoReceiptEntity soReceiptEntity = handleDelEntityList.stream().filter(v -> v.getId().equals(deleteDetailEntity.getMainId())).findFirst().orElseThrow(() -> new ServiceException("未找到收款单数据"));
            if(!ApproveStatusEnum.allowUpdateStatus(soReceiptEntity.getApproveStatus())) {
                throw new ServiceException("只有待提交或审核不通过的收款单允许删除");
            }
            this.deleteByDetail(soReceiptEntity,deleteDetailEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void autoSubmitBySo(SoInfoEntity entity) {
        //查询包含销售订单的全部收款单
        List<SoReceiptDetailEntity> currencyDetailEntityList = soReceiptDetailService.listBySoId(entity.getId());
        if(CollectionUtils.isEmpty(currencyDetailEntityList)){
            return;
        }
        List<String> mainIds = currencyDetailEntityList.stream().map(SoReceiptDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoReceiptEntity> soReceiptEntityList = this.listByIds(mainIds);
        List<SoReceiptDetailEntity> allDetailEntityList = soReceiptDetailService.listByMainIds(mainIds);
        //查询销售订单信息
        List<String> soIds = allDetailEntityList.stream().map(SoReceiptDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(soIds);
        for (SoReceiptEntity soReceiptEntity : soReceiptEntityList) {
            //待提交和审核不通过的单据进行提交
            if(!ApproveStatusEnum.allowUpdateStatus(soReceiptEntity.getApproveStatus())) {
                continue;
            }
            List<SoReceiptDetailEntity> detailEntityList = allDetailEntityList.stream().filter(v -> v.getMainId().equals(soReceiptEntity.getId())).collect(Collectors.toList());
            List<SoInfoEntity> handleSoInfoList = soInfoEntityList.stream().filter(v -> detailEntityList.stream().map(SoReceiptDetailEntity::getSoId).collect(Collectors.toList()).contains(v.getId())).collect(Collectors.toList());
            //校验订单是否全部为待提交或审核不通过
            if(handleSoInfoList.stream().allMatch(v-> BillApproveStatusEnum.WAIT_SUBMIT.equals(v.getApproveStatus())
                    || BillApproveStatusEnum.REJECT.equals(v.getApproveStatus()))){
                continue;
            }
            operateLogService.addModuleOperateLog(StrUtil.format("销售订单【{}】提交审核，系统自动提交收款单", entity.getCode()), ModuleTypeEnum.SO_RECEIPT.getCode(), soReceiptEntity.getId(), "系统自动提交操作");
            this.submit(soReceiptEntity.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void autoApproveBySo(SoInfoEntity entity) {
        //查询包含销售订单的全部收款单
        List<SoReceiptDetailEntity> currencyDetailEntityList = soReceiptDetailService.listBySoId(entity.getId());
        if(CollectionUtils.isEmpty(currencyDetailEntityList)){
            return;
        }
        List<String> mainIds = currencyDetailEntityList.stream().map(SoReceiptDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoReceiptEntity> soReceiptEntityList = this.listByIds(mainIds);
        List<SoReceiptDetailEntity> allDetailEntityList = soReceiptDetailService.listByMainIds(mainIds);
        //查询销售订单信息
        List<String> soIds = allDetailEntityList.stream().map(SoReceiptDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(soIds);
        for (SoReceiptEntity soReceiptEntity : soReceiptEntityList) {
            //待提交和审核不通过的单据进行提交
            if(!ApproveStatusEnum.APPROVE_ING.equals(soReceiptEntity.getApproveStatus())) {
                continue;
            }
            List<SoReceiptDetailEntity> detailEntityList = allDetailEntityList.stream().filter(v -> v.getMainId().equals(soReceiptEntity.getId())).collect(Collectors.toList());
            List<SoInfoEntity> handleSoInfoList = soInfoEntityList.stream().filter(v -> detailEntityList.stream().map(SoReceiptDetailEntity::getSoId).collect(Collectors.toList()).contains(v.getId())).collect(Collectors.toList());
            //校验订单是否全部审核通过
            if(handleSoInfoList.stream().anyMatch(v-> !BillApproveStatusEnum.APPROVE.equals(v.getApproveStatus()))){
                continue;
            }
            operateLogService.addModuleOperateLog(StrUtil.format("销售订单【{}】审核通过，系统自动审核通过", entity.getCode()), ModuleTypeEnum.SO_RECEIPT.getCode(), soReceiptEntity.getId(), "系统自动提交操作");
            workflowFeign.cancelProcess(Arrays.asList(soReceiptEntity.getId()));
            ApproveOneDTO dto = new ApproveOneDTO();
            dto.setId(soReceiptEntity.getId());
            dto.setType(ApproveTypeEnum.PASS.getStatus());
            this.approveEnd(dto,soReceiptEntity);
        }
    }

    @Override
    public SoReceiptEntity getByThirdSystemAndCode(String thirdSystem, String code) {
        LambdaQueryWrapper<SoReceiptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoReceiptEntity::getThirdSystem,thirdSystem);
        queryWrapper.eq(SoReceiptEntity::getThirdCode,code);
        List<SoReceiptEntity> list = this.list(queryWrapper);
        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    private void deleteByDetail(SoReceiptEntity soReceiptEntity, SoReceiptDetailEntity deleteDetailEntity) {
        soReceiptDetailService.removeByIds(Arrays.asList(deleteDetailEntity.getId()));
        //查询主表下是否还存在明细
        List<SoReceiptDetailEntity> detailList = soReceiptDetailService.listByMainIds(Arrays.asList(soReceiptEntity.getId()));
        //不存在明细，将主记录也删除
        if(CollectionUtils.isEmpty(detailList)) {
            this.removeById(soReceiptEntity.getId());
        }else{
            //存在明细，更新主表总金额
            BigDecimal totalAmount = detailList.stream().map(SoReceiptDetailEntity::getReceiptAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            soReceiptEntity.setReceiptAmount(totalAmount);
            this.updateById(soReceiptEntity);
        }
    }


    @Override
    public SoReceiptDTO.ViewDTO view(String id) {
        SoReceiptEntity soReceiptEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到收款单数据"));
        SoReceiptDTO.ViewDTO data = BeanMapperUtils.map(SoReceiptDTO.ViewDTO.class, soReceiptEntity);
        List<SoReceiptDetailEntity> detailList = soReceiptDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data,soReceiptEntity,detailList);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SoReceiptEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_RECEIPT.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SoReceiptDTO.ViewDTO data,SoReceiptEntity entity,List<SoReceiptDetailEntity> detailList) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        CustomerInfoEntity customerInfo = customerInfoService.getById(entity.getCustomerId());
        if(ObjectUtil.isNotEmpty(customerInfo)) {
            data.setCustomerName(customerInfo.getName());
        }
        data.setApproveStatusName(data.getApproveStatus().getName());

        //查询主记录附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<OmsAttachmentEntity> omsAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(Arrays.asList(entity.getId()),tableName.value());
        List<AttachDTO> attachDTOList = BeanMapper.copyList(omsAttachmentEntities, AttachDTO.class);
        data.setAttachmentList(attachDTOList);

        //查询明细记录附件
        TableName detailTableName = SoReceiptDetailEntity.class.getDeclaredAnnotation(TableName.class);
        List<String> detailIds = detailList.stream().map(SoReceiptDetailEntity::getId).collect(Collectors.toList());
        List<OmsAttachmentEntity> detailAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(detailIds,detailTableName.value());
        // 属性赋值
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());

        //查询销售订单信息
        List<String> soCodes = detailList.stream().map(SoReceiptDetailEntity::getSoCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        SoReceiptDTO.SoSearchDTO dto = new SoReceiptDTO.SoSearchDTO();
        dto.setCustomerId(entity.getCustomerId());
        dto.setSoCodeList(soCodes);
        List<SoReceiptDTO.SoInfoAndReceiptDTO> soInfoDTOS = CollectionUtils.isNotEmpty(soCodes)? this.listSoReceiptBySoCode(dto):new ArrayList<>();
        List<SoReceiptDetailDTO.ViewDTO> detailViewList = new ArrayList<>();
        DictBasicEntity receiveMethod  = receiveMethodList.stream().filter(v -> v.getValue().equals(entity.getDictReceiptMethod())).findFirst().orElse(null);
        if(ObjectUtil.isNotEmpty(receiveMethod)) {
            data.setDictReceiptMethodName(receiveMethod.getName());
        }
        List<BankAccountEntity> bankAccountList = new ArrayList<>();
        List<String> receiveAccountList = Arrays.asList(data.getReceiptAccount());
        if (CollectionUtils.isNotEmpty(receiveAccountList)) {
            bankAccountList = bankAccountService.listByIds(receiveAccountList);
            BankAccountEntity bankAccountEntity = bankAccountList.stream().filter(b -> CharSequenceUtil.equals(b.getId(), data.getReceiptAccount())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bankAccountEntity)) {
                data.setReceiptAccountName(bankAccountEntity.getAccountName());
            }
        }
        for (SoReceiptDetailEntity soReceiptDetailEntity : detailList) {
            SoReceiptDetailDTO.ViewDTO viewDTO = BeanMapperUtils.map(SoReceiptDetailDTO.ViewDTO.class, soReceiptDetailEntity);
            List<OmsAttachmentEntity> detailAttachList = detailAttachmentEntities.stream().filter(v -> v.getBusinessId().equals(soReceiptDetailEntity.getId())).collect(Collectors.toList());
            List<AttachDTO> detailAttachDTOList = BeanMapper.copyList(detailAttachList, AttachDTO.class);
            viewDTO.setAttachmentList(detailAttachDTOList);

            SoReceiptDTO.SoInfoAndReceiptDTO soInfoAndReceiptDTO = soInfoDTOS.stream().filter(v -> v.getSoCode().equals(soReceiptDetailEntity.getSoCode())).findFirst().orElse(new SoReceiptDTO.SoInfoAndReceiptDTO());
            viewDTO.setApproveStatus(soInfoAndReceiptDTO.getApproveStatus());
            viewDTO.setApproveStatusName(ApproveStatusEnum.getName(soInfoAndReceiptDTO.getApproveStatus()));
            viewDTO.setRemainReceiptAmount(soInfoAndReceiptDTO.getRemainReceiptAmount());

            detailViewList.add(viewDTO);
        }
        data.setDetailList(detailViewList);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SoReceiptEntity::getId, id)
            .set(SoReceiptEntity::getApproveUserId, userInfo.getUid())
            .set(SoReceiptEntity::getApproveUserName, userInfo.getUserName())
            .set(SoReceiptEntity::getApproveStatus, approveStatus)
            .set(SoReceiptEntity::getApproveTime, LocalDateTime.now())
            .update(new SoReceiptEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SoReceiptEntity::getId, id)
            .set(SoReceiptEntity::getApproveUserId, "")
            .set(SoReceiptEntity::getApproveUserName, "")
            .set(SoReceiptEntity::getApproveStatus, approveStatus)
            .set(SoReceiptEntity::getApproveTime, null)
            .update(new SoReceiptEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoReceiptEntity::getId, id)
        .set(SoReceiptEntity::getApproveStatus, approveStatus)
        .update(new SoReceiptEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SoReceiptDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
        List<String> receiveAccountList = list.stream().map(SoReceiptDTO.ListDTO::getReceiptAccount).filter(org.apache.commons.lang3.StringUtils::isNotBlank).collect(Collectors.toList());
        List<BankAccountEntity> bankAccountList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(receiveAccountList)) {
            bankAccountList = bankAccountService.listByIds(receiveAccountList);
        }
        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        for(SoReceiptDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            DictBasicEntity receiveMethod = receiveMethodList.stream().filter(v -> v.getValue().equals(data.getDictReceiptMethod())).findFirst().orElse(null);
            if(ObjectUtil.isNotEmpty(receiveMethod)) {
                data.setDictReceiptMethodName(receiveMethod.getName());
            }
            data.setIsPostedStr(ObjectUtil.isNotEmpty(data.getIsPosted()) && data.getIsPosted() ? "是" : "否");
            BankAccountEntity bankAccountEntity = bankAccountList.stream().filter(b -> CharSequenceUtil.equals(b.getId(), data.getReceiptAccount())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bankAccountEntity)) {
                data.setReceiptAccountName(bankAccountEntity.getAccountName());
            }

            //销售组织
            if (StringUtils.isNotBlank(data.getSalesOrgId())) {
                SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(data.getSalesOrgId());
                data.setSalesOrgId(data.getSalesOrgId());
                data.setSalesOrgName(sysAccountingCompanyEntity.getCompanyName());
            }

        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SoReceiptEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePlatformConsumer(PlatformReceiptDTO dto) {
        //查询是否存在
        SoReceiptEntity exist = this.getByThirdSystemAndCode(dto.getThirdSystem(), dto.getCode());
        List<SoReceiptDetailEntity> existList;
        if(exist != null) {
            existList = soReceiptDetailService.listByMainIds(Arrays.asList((exist.getId())));
            //如果是作废，erp单据也要作废
            if(dto.getIsInvalid()){
                if(exist.getInvalidStatus()){
                    log.warn("PlatformReceiptConsumerService.handle 收款单已作废，参数：{}", JSONUtil.toJsonStr(dto));
                    return;
                }
                exist.setInvalidStatus(true);
                exist.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
                this.updateById(exist);
                return;
            }
            //判断平台更新时间有更新
            if(!dto.getPlatformUpdateTime().isAfter(exist.getPlatformUpdateTime())){
                log.warn("平台更新时间没有更新，{}",exist.getCode());
                return;
            }
            //存在判断是否有字段变更
            boolean hasChange = judgeHasChange(exist,existList, dto);
            if(!hasChange){
                log.warn("PlatformReceiptConsumerService.handle 收款单无变化，参数：{}",JSONUtil.toJsonStr(dto));
                return;
            }
            //如果是审核中，撤销审核
            if(ApproveStatusEnum.APPROVE_ING.equals(exist.getApproveStatus())){
                this.cancelProcess(new ApproveDTO.CancelProcessDTO(exist.getId()));
            }
            //如果是已审核，反审核
            if(ApproveStatusEnum.APPROVE.equals(exist.getApproveStatus())){
                this.updateForDisApprove(exist.getId(),ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            //其他状态，直接更新
            SoReceiptDTO.UpdateDTO addOrUpdateDTO = new SoReceiptDTO.UpdateDTO();
            addOrUpdateDTO.setId(exist.getId());
            addOrUpdateDTO.setCurrency(dto.getCurrency());
            addOrUpdateDTO.setIsPosted(dto.getIsPosted());
            addOrUpdateDTO.setPostedAccount(dto.getErpPostedAccount());
            addOrUpdateDTO.setSourceType(SoReceiptSourceTypeEnum.THIRD.getCode());
            addOrUpdateDTO.setPlatformOrderId(dto.getPlatformId());
            addOrUpdateDTO.setAttachmentList(dto.getAttachmentList());
            addOrUpdateDTO.setReceiptDate(dto.getReceiptDate());
            addOrUpdateDTO.setDictReceiptMethod(dto.getErpReceiptMethod());
            addOrUpdateDTO.setReceiptAccount(dto.getErpReceiptAccountId());
            addOrUpdateDTO.setSalesOrgId(dto.getErpSaleOrgId());
            addOrUpdateDTO.setRemark(dto.getRemark());
            addOrUpdateDTO.setThirdCode(dto.getCode());
            addOrUpdateDTO.setReceiptAmount(dto.getAmount());
            addOrUpdateDTO.setThirdSystem(dto.getThirdSystem());
            addOrUpdateDTO.setIsFromPlatform(true);
            addOrUpdateDTO.setCustomerId(dto.getErpCustomerId());
            addOrUpdateDTO.setPlatformCreateTime(dto.getPlatformCreateTime());
            addOrUpdateDTO.setPlatformUpdateTime(dto.getPlatformUpdateTime());
            List<SoReceiptDetailDTO.UpdateDTO> updateDTOList = new ArrayList<>();
            List<PlatformReceiptDetailDTO> detailList = CollectionUtils.isNotEmpty(dto.getDetail())?dto.getDetail():new ArrayList<>();
            for (PlatformReceiptDetailDTO platformReceiptDetailDTO : detailList) {
                SoReceiptDetailEntity existDetail = existList.stream().filter(v -> v.getPlatformDetailId().equals(platformReceiptDetailDTO.getPlatformDetailId())).findFirst().orElse(null);
                SoReceiptDetailDTO.UpdateDTO detailUpdateDTO = new SoReceiptDetailDTO.UpdateDTO();
                if(Objects.nonNull(existDetail)){
                    detailUpdateDTO.setId(existDetail.getId());
                }
                detailUpdateDTO.setReceiptAmount(platformReceiptDetailDTO.getAmount());
                detailUpdateDTO.setRemark(platformReceiptDetailDTO.getRemark());
                detailUpdateDTO.setAttachmentList(platformReceiptDetailDTO.getAttachmentList());
                detailUpdateDTO.setSoCode(platformReceiptDetailDTO.getSoCode());
                detailUpdateDTO.setPlatformDetailCode(platformReceiptDetailDTO.getCode());
                detailUpdateDTO.setSoId(platformReceiptDetailDTO.getErpSoId());
                detailUpdateDTO.setMainId(exist.getId());
                detailUpdateDTO.setPlatformDetailId(platformReceiptDetailDTO.getPlatformDetailId());
                detailUpdateDTO.setAttachmentList(platformReceiptDetailDTO.getAttachmentList());
                updateDTOList.add(detailUpdateDTO);
            }
            addOrUpdateDTO.setDetailList(updateDTOList);
            this.update(addOrUpdateDTO);
            //处理删除的明细
            List<String> platformDetailIds = detailList.stream().map(PlatformReceiptDetailDTO::getPlatformDetailId).collect(Collectors.toList());
            List<SoReceiptDetailEntity> deleteDetailList = existList.stream().filter(v -> !platformDetailIds.contains(v.getPlatformDetailId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(deleteDetailList)){
                soReceiptDetailService.removeByIds(deleteDetailList.stream().map(SoReceiptDetailEntity::getId).collect(Collectors.toList()));
            }
        }else{
            //如果是作废，直接跳过
            if(dto.getIsInvalid()){
                return;
            }
            //新增单据
            SoReceiptDTO.AddDTO addDTO = new SoReceiptDTO.AddDTO();
            addDTO.setCurrency(dto.getCurrency());
            addDTO.setIsPosted(dto.getIsPosted());
            addDTO.setPostedAccount(dto.getErpPostedAccount());
            addDTO.setSourceType(SoReceiptSourceTypeEnum.THIRD.getCode());
            addDTO.setPlatformOrderId(dto.getPlatformId());
            addDTO.setAttachmentList(dto.getAttachmentList());
            addDTO.setReceiptDate(dto.getReceiptDate());
            addDTO.setDictReceiptMethod(dto.getErpReceiptMethod());
            addDTO.setReceiptAccount(dto.getErpReceiptAccountId());
            addDTO.setReceiptAmount(dto.getAmount());
            addDTO.setSalesOrgId(dto.getErpSaleOrgId());
            addDTO.setRemark(dto.getRemark());
            addDTO.setThirdCode(dto.getCode());
            addDTO.setThirdSystem(dto.getThirdSystem());
            addDTO.setCustomerId(dto.getErpCustomerId());
            addDTO.setPlatformCreateTime(dto.getPlatformCreateTime());
            addDTO.setPlatformUpdateTime(dto.getPlatformUpdateTime());
            List<SoReceiptDetailDTO.AddDTO> detailAddDTOList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(dto.getDetail())){
                for (PlatformReceiptDetailDTO platformReceiptDetailDTO : dto.getDetail()) {
                    SoReceiptDetailDTO.AddDTO detailAddDTO = new SoReceiptDetailDTO.AddDTO();
                    detailAddDTO.setReceiptAmount(platformReceiptDetailDTO.getAmount());
                    detailAddDTO.setRemark(platformReceiptDetailDTO.getRemark());
                    detailAddDTO.setAttachmentList(platformReceiptDetailDTO.getAttachmentList());
                    detailAddDTO.setSoCode(platformReceiptDetailDTO.getSoCode());
                    detailAddDTO.setPlatformDetailCode(platformReceiptDetailDTO.getCode());
                    detailAddDTO.setSoId(platformReceiptDetailDTO.getErpSoId());
                    detailAddDTO.setPlatformDetailId(platformReceiptDetailDTO.getPlatformDetailId());
                    detailAddDTOList.add(detailAddDTO);
                }
            }
            addDTO.setDetailList(detailAddDTOList);
            this.add(addDTO);
        }
    }

    @Override
    public List<SoReceiptEntity> listBySoId(String soId) {
        if(StrUtils.isNotEmpty(soId)){
            List<SoReceiptDetailEntity> detailList = soReceiptDetailService.listBySoId(soId);
            if(CollectionUtils.isNotEmpty(detailList)){
                List<String> mainIds = detailList.stream().map(SoReceiptDetailEntity::getMainId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(mainIds)){
                    return this.listByIds(mainIds);
                }
            }
        }
        return new ArrayList<>();
    }

    private boolean judgeHasChange(SoReceiptEntity exist, List<SoReceiptDetailEntity> existList, PlatformReceiptDTO dto) {
        //校验主表字段
        if(!exist.getCustomerId().equals(dto.getErpCustomerId())
                || !exist.getCurrency().equals(dto.getCurrency())
                || !exist.getIsPosted().equals(dto.getIsPosted())
                || !exist.getPostedAccount().equals(dto.getErpPostedAccount())
                || !exist.getRemark().equals(dto.getRemark())
                || !exist.getReceiptAmount().equals(dto.getAmount())
                || !exist.getDictReceiptMethod().equals(dto.getErpReceiptMethod())
                || !exist.getReceiptAccount().equals(dto.getErpReceiptAccountId())
                || !exist.getReceiptDate().equals(dto.getReceiptDate())
                || !exist.getSalesOrgId().equals(dto.getErpSaleOrgId())
        ){
            return true;
        }
        //校验明细
        List<PlatformReceiptDetailDTO> platformReceiptDetailDTOList = dto.getDetail();
        if(existList.size() != platformReceiptDetailDTOList.size()){
            return true;
        }
        //根据明细ID进行匹配
        Map<String, SoReceiptDetailEntity> existDetailMap = existList.stream().collect(Collectors.toMap(SoReceiptDetailEntity::getPlatformDetailId, e->e));
        Map<String, PlatformReceiptDetailDTO> platformDetailMap = platformReceiptDetailDTOList.stream().collect(Collectors.toMap(PlatformReceiptDetailDTO::getPlatformDetailId, e->e));

        //校验ERP存在的明细，平台不存在或者有变化
        for(Map.Entry<String, SoReceiptDetailEntity> entry : existDetailMap.entrySet()){
            String key = entry.getKey();
            SoReceiptDetailEntity existDetail = entry.getValue();
            PlatformReceiptDetailDTO platformDetail = platformDetailMap.get(key);
            if(platformDetail == null){
                return true;
            }
            if(!existDetail.getReceiptAmount().equals(platformDetail.getAmount())
                    || !existDetail.getRemark().equals(platformDetail.getRemark())
                    || !existDetail.getSoCode().equals(platformDetail.getSoCode())
                    || !existDetail.getPlatformDetailCode().equals(platformDetail.getCode())
            ){
                return true;
            }
        }
        //校验平台存在的明细，erp不存在
        for(Map.Entry<String, PlatformReceiptDetailDTO> entry : platformDetailMap.entrySet()){
            String key = entry.getKey();
            PlatformReceiptDetailDTO platformDetail = entry.getValue();
            SoReceiptDetailEntity existDetail = existDetailMap.get(key);
            if(existDetail == null){
                return true;
            }
        }

        return false;
    }

}
