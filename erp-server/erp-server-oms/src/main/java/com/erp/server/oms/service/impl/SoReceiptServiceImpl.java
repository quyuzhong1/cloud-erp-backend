package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.AttachDTO;
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
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.OmsAttachmentEntity;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.model.scm.enums.ContractInfoStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
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
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoReceiptDTO.AddDTO addDTO) {
        SoReceiptEntity soReceiptEntity = new SoReceiptEntity();
        BeanMapperUtils.copy(addDTO, soReceiptEntity);

        // 数据处理
        handleData(soReceiptEntity);

        List<SoReceiptDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        //求和总收款金额
        if(CollectionUtils.isEmpty(detailList)){
            throw new ServiceException("收款单明细不能为空");
        }
        BigDecimal totalAmount = detailList.stream().map(SoReceiptDetailDTO.AddDTO::getReceiptAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        soReceiptEntity.setReceiptAmount(totalAmount);
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
        SoReceiptEntity soReceiptEntity =  BeanMapperUtils.map(SoReceiptEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(soReceiptEntity);
        List<SoReceiptDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        //求和总收款金额
        if(CollectionUtils.isEmpty(detailList)){
            throw new ServiceException("收款单明细不能为空");
        }
        BigDecimal totalAmount = detailList.stream().map(SoReceiptDetailDTO.UpdateDTO::getReceiptAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        soReceiptEntity.setReceiptAmount(totalAmount);

        log.info("编辑 开始修改收款单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soReceiptEntity);
        if(!save) {
            throw new ServiceException("收款单保存失败");
        }
        //更新明细
        soReceiptDetailService.updateDetail(soReceiptEntity,addOrUpdateDTO.getDetailList());
        // 保存附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<AttachDTO> list = addOrUpdateDTO.getAttachmentList();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(v->v.setBusinessId(soReceiptEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(list, tableName.value());
        }

        // 记录主单操作日志
        log.info("编辑 开始记录收款单日志数据，单号：【{}】", soReceiptEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soReceiptEntity.getCode(), "收款单");
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
        log.info("提交 开始修改收款单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动收款单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录收款单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "收款单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
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
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "收款单", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "审核操作");
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
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(null);
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
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "收款单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SoReceiptEntity entity) {
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
        SoReceiptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到收款单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除收款单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除收款单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "收款单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除收款单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SoReceiptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到收款单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改收款单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "收款单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoReceiptEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public List<SoReceiptDTO.SoInfoAndReceiptDTO> listSoReceiptBySoCode(SoReceiptDTO.SoSearchDTO dto) {
        if(StringUtils.isBlank(dto.getCustomerId())){
            throw new ServiceException("客户不能为空");
        }
        List<SoReceiptDTO.SoInfoAndReceiptDTO> soInfoAndReceiptDTOList = baseMapper.listSoReceiptBySoCode(dto);
        soInfoAndReceiptDTOList.forEach(v->v.setApproveStatus(ApproveStatusEnum.getName(v.getApproveStatus())));
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
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(null);
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
        //查询主记录附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<OmsAttachmentEntity> omsAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(Arrays.asList(entity.getId()),tableName.value());
        List<AttachDTO> attachDTOList = BeanMapper.copyList(omsAttachmentEntities, AttachDTO.class);
        data.setAttachDTOList(attachDTOList);

        //查询明细记录附件
        TableName detailTableName = SoReceiptDetailEntity.class.getDeclaredAnnotation(TableName.class);
        List<String> detailIds = detailList.stream().map(SoReceiptDetailEntity::getId).collect(Collectors.toList());
        List<OmsAttachmentEntity> detailAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(detailIds,tableName.value());
        // 属性赋值
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());

        //查询销售订单信息
        List<String> soCodes = detailList.stream().map(SoReceiptDetailEntity::getSoCode).distinct().collect(Collectors.toList());
        SoReceiptDTO.SoSearchDTO dto = new SoReceiptDTO.SoSearchDTO();
        dto.setCustomerId(entity.getCustomerId());
        dto.setSoCodeList(soCodes);
        List<SoReceiptDTO.SoInfoAndReceiptDTO> soInfoDTOS = this.listSoReceiptBySoCode(dto);
        List<SoReceiptDetailDTO.ViewDTO> detailViewList = new ArrayList<>();
        for (SoReceiptDetailEntity soReceiptDetailEntity : detailList) {
            SoReceiptDetailDTO.ViewDTO viewDTO = BeanMapperUtils.map(SoReceiptDetailDTO.ViewDTO.class, soReceiptDetailEntity);
            List<OmsAttachmentEntity> detailAttachList = detailAttachmentEntities.stream().filter(v -> v.getBusinessId().equals(soReceiptDetailEntity.getId())).collect(Collectors.toList());
            List<AttachDTO> detailAttachDTOList = BeanMapper.copyList(detailAttachList, AttachDTO.class);
            viewDTO.setAttachmentList(detailAttachDTOList);

            SoReceiptDTO.SoInfoAndReceiptDTO soInfoAndReceiptDTO = soInfoDTOS.stream().filter(v -> v.getSoCode().equals(soReceiptDetailEntity.getSoCode())).findFirst().orElse(new SoReceiptDTO.SoInfoAndReceiptDTO());
            viewDTO.setApproveStatus(soInfoAndReceiptDTO.getApproveStatus());
            viewDTO.setApproveStatusName(ApproveStatusEnum.getName(soInfoAndReceiptDTO.getApproveStatus()));
            viewDTO.setRemainReceiptAmount(soInfoAndReceiptDTO.getRemainReceiptAmount());
            DictBasicEntity receiveMethod  = receiveMethodList.stream().filter(v -> v.getValue().equals(soReceiptDetailEntity.getDictReceiptMethod())).findFirst().orElse(null);
            if(ObjectUtil.isNotEmpty(receiveMethod)) {
                viewDTO.setDictReceiptMethodName(receiveMethod.getName());
            }
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

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        for(SoReceiptDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            DictBasicEntity receiveMethod = receiveMethodList.stream().filter(v -> v.getValue().equals(data.getDictReceiptMethod())).findFirst().orElse(null);
            if(ObjectUtil.isNotEmpty(receiveMethod)) {
                data.setDictReceiptMethodName(receiveMethod.getName());
            }
            data.setIsPostedStr(ObjectUtil.isNotEmpty(data.getIsPosted()) && data.getIsPosted() ? "是" : "否");
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

    /**
    * 新增修改处理数据
    */
    private void handleData(SoReceiptEntity soReceiptEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
