package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.*;
import com.common.business.utils.StringUtil;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.dto.AssetNoticeDTO;
import com.erp.model.plm.entity.AssetPurchaseOrderDetailEntity;
import com.erp.model.plm.entity.AssetPurchaseOrderSupplierEntity;
import java.util.function.Function;
import com.erp.model.plm.enums.AssetApproveStatusEnum;
import com.erp.model.plm.enums.AssetPurchaseOrderTypeEnum;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.enums.ContractStampStatusEnum;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.plm.service.AssetPurchaseOrderDetailService;
import com.erp.server.plm.service.AssetPurchaseOrderSupplierService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.AssetPurchaseOrderEntity;
import com.erp.server.plm.mapper.AssetPurchaseOrderMapper;
import com.erp.server.plm.service.AssetPurchaseOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.AssetPurchaseOrderDTO;
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
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetPurchaseOrderServiceImpl extends SuperServiceImpl<AssetPurchaseOrderMapper, AssetPurchaseOrderEntity> implements AssetPurchaseOrderService {

    @Autowired
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Autowired
    private AssetPurchaseOrderSupplierService assetPurchaseOrderSupplierService;

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SupplierFeign supplierFeign;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private ScmDictFeign scmDictFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetPurchaseOrderDTO.AddDTO addDTO) {
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = new AssetPurchaseOrderEntity();
        BeanMapperUtils.copy(addDTO, assetPurchaseOrderEntity);

        // 数据处理
        handleData(assetPurchaseOrderEntity);

        log.info("资产采购单开始新增");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPO);
        assetPurchaseOrderEntity.setCode(code);

        boolean savePurchaseOrder = super.save(assetPurchaseOrderEntity);
        if (!savePurchaseOrder) {
            throw new ServiceException("资产采购单保存失败");
        }
        AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity = new AssetPurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(addDTO.getAssetPurchaseOrderSupplierDTO(), assetPurchaseOrderSupplierEntity);

        //处理供应商数据
        handleSupplierData(assetPurchaseOrderSupplierEntity, assetPurchaseOrderEntity);

        boolean savePurchaseSupplier = assetPurchaseOrderSupplierService.save(assetPurchaseOrderSupplierEntity);
        if (!savePurchaseSupplier) {
            throw new ServiceException("资产采购单供应商信息报错失败");
        }

        // 新增明细
        assetPurchaseOrderDetailService.add(addDTO.getAssetPurchaseOrderDetailDTO(), assetPurchaseOrderEntity.getId());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产采购单", assetPurchaseOrderEntity.getCode());
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), assetPurchaseOrderEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(assetPurchaseOrderEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetPurchaseOrderDTO.UpdateDTO addOrUpdateDTO) {
        AssetPurchaseOrderEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity = new AssetPurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(addOrUpdateDTO.getAssetPurchaseOrderSupplierDTO(), assetPurchaseOrderSupplierEntity);
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = BeanMapperUtils.map(AssetPurchaseOrderEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetPurchaseOrderEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetPurchaseOrderEntity);
        if (!save) {
            throw new ServiceException("资产采购单保存失败");
        }

        //处理供应商数据
        BeanMapperUtils.copy(addOrUpdateDTO.getAssetPurchaseOrderSupplierDTO(), assetPurchaseOrderSupplierEntity);
        handleSupplierData(assetPurchaseOrderSupplierEntity, assetPurchaseOrderEntity);
        boolean savePurchaseSupplier = assetPurchaseOrderSupplierService.updateById(assetPurchaseOrderSupplierEntity);
        if (!savePurchaseSupplier) {
            throw new ServiceException("资产采购单供应商信息报错失败");
        }

        // 更新明细
        assetPurchaseOrderDetailService.update(addOrUpdateDTO.getAssetPurchaseOrderDetailDTOList(), assetPurchaseOrderEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，单号：【{}】", assetPurchaseOrderEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetPurchaseOrderEntity.getCode(), "");
        operateLogService.addSysLogByUpdate(old, assetPurchaseOrderEntity, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), assetPurchaseOrderEntity.getId(), "资产采购单", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetPurchaseOrderDTO.ListDTO> paging(PagingDTO<AssetPurchaseOrderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetPurchaseOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetPurchaseOrderDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetPurchaseOrderDTO.PagingParamDTO searchParam = new AssetPurchaseOrderDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AssetPurchaseOrderDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(AssetPurchaseOrderDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new AssetPurchaseOrderDTO.TabListDTO(status, AssetApproveStatusEnum.getName(status), 0));
            }
        });
        // 计算合计数量
        list.add(new AssetPurchaseOrderDTO.TabListDTO("all", AssetApproveStatusEnum.ALL.getName(), list.stream().mapToInt(AssetPurchaseOrderDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(AssetPurchaseOrderDTO.ExportDTO param, HttpServletResponse response) {
        List<AssetPurchaseOrderDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/assetPurchaseOrder.xlsx";
        String name = "导出";
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
        AssetPurchaseOrderEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetPurchaseOrderDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetPurchaseOrderDTO.UpdateDTO dto) {
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
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AssetPurchaseOrderEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(AssetPurchaseOrderEntity entity, ApproveOneDTO dto) {
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        AssetPurchaseOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetPurchaseOrderEntity entity) {
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
        AssetPurchaseOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addSysLogBySave(msg, null, entity.getCode(), "删除数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetPurchaseOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetPurchaseOrderEntity::getId, id)
                .set(AssetPurchaseOrderEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(AssetPurchaseOrderEntity::getInvalidRemark, remark)
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "", remark);
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetPurchaseOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    public AssetPurchaseOrderDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetPurchaseOrderEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateContractStampStatus(PurchaseOrderDTO.ContractStampStatusParamsDTO dto) {
        return null;
    }

    @Override
    public AssetPurchaseOrderDTO.ViewDTO view(String id) {
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        AssetPurchaseOrderDTO.ViewDTO data = BeanMapperUtils.map(AssetPurchaseOrderDTO.ViewDTO.class, assetPurchaseOrderEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(AssetPurchaseOrderEntity entity) {
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

    private void fillOne(AssetPurchaseOrderDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(AssetPurchaseOrderEntity::getId, id)
                .set(AssetPurchaseOrderEntity::getApproveUserId, userInfo.getUid())
                .set(AssetPurchaseOrderEntity::getApproveUserName, userInfo.getUserName())
                .set(AssetPurchaseOrderEntity::getApproveStatus, approveStatus)
                .update(new AssetPurchaseOrderEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetPurchaseOrderEntity::getId, id)
                .set(AssetPurchaseOrderEntity::getApproveUserId, "")
                .set(AssetPurchaseOrderEntity::getApproveUserName, "")
                .set(AssetPurchaseOrderEntity::getApproveStatus, approveStatus)
                .update(new AssetPurchaseOrderEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetPurchaseOrderEntity::getId, id)
                .set(AssetPurchaseOrderEntity::getApproveStatus, approveStatus)
                .update(new AssetPurchaseOrderEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<AssetPurchaseOrderDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for (AssetPurchaseOrderDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
            data.setContractStampStatusName(ContractStampStatusEnum.getName(data.getContractStampStatus()));

        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(AssetPurchaseOrderEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(AssetPurchaseOrderEntity assetPurchaseOrderEntity) {
        assetPurchaseOrderEntity.setContractStampStatus(ContractStampStatusEnum.WAIT_SUBMIT.getCode());
        //默认资产采购单
        if (StringUtils.isNotBlank(assetPurchaseOrderEntity.getOrderType())) {
            String orderType = AssetPurchaseOrderTypeEnum.getNameByCode(assetPurchaseOrderEntity.getOrderType());
            if (StringUtils.isNotBlank(orderType)) {
                assetPurchaseOrderEntity.setOrderType(orderType);
            } else {
                assetPurchaseOrderEntity.setOrderType(AssetPurchaseOrderTypeEnum.ASSET_PURCHASE.getCode());
            }
        } else {
            assetPurchaseOrderEntity.setOrderType(AssetPurchaseOrderTypeEnum.ASSET_PURCHASE.getCode());
        }

        if (StringUtils.isBlank(assetPurchaseOrderEntity.getSourceId())) {
            assetPurchaseOrderEntity.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
        }

        assetPurchaseOrderEntity.setInvalidStatus(Boolean.FALSE);

    }

    private void handleSupplierData(AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity, AssetPurchaseOrderEntity assetPurchaseOrderEntity) {
        if (StringUtils.isBlank(assetPurchaseOrderSupplierEntity.getSupplierId())) {
            throw new ServiceException("供应商id不允许为空");
        }

        if (StringUtils.isBlank(assetPurchaseOrderSupplierEntity.getPayMethodId())) {
            throw new ServiceException("结算方式不允许为空");
        }

        if (StringUtils.isBlank(assetPurchaseOrderSupplierEntity.getPaymentCondition())) {
            throw new ServiceException("付款条件不允许为空");
        }

        if (StringUtils.isBlank(assetPurchaseOrderSupplierEntity.getPayee())) {
            throw new ServiceException("账户名称不允许为空");
        }

        //付款条件
        List<BaseDropDownDTO.DisabledDTO> paymentConditionList = scmTaskFeign.listPaymentCondition();
        Map<String, String> paymentConditionMap = paymentConditionList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getCode, BaseDropDownDTO.DisabledDTO::getValue));
        assetPurchaseOrderSupplierEntity.setPaymentConditionName(paymentConditionMap.getOrDefault(assetPurchaseOrderSupplierEntity.getPaymentCondition(), ""));

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getId, DictBasicDTO::getName));
        assetPurchaseOrderSupplierEntity.setPayMethodName(settleDictMap.getOrDefault(assetPurchaseOrderSupplierEntity.getPayMethodId(), ""));

        //收款银行,银行账号
        List<String> idList = new ArrayList<>(1);
        idList.add(assetPurchaseOrderSupplierEntity.getSupplierId());
        List<SupplierDTO.SupplierDefaultDTO> supplierDefaultDTOS = supplierFeign.listDefaultBySupplierIdList(idList);
        SupplierDTO.SupplierDefaultDTO supplierDefaultDTO = supplierDefaultDTOS.get(0);
        assetPurchaseOrderSupplierEntity.setBankName(supplierDefaultDTO.getAccountEntity().getBankName());
        assetPurchaseOrderSupplierEntity.setBankAccount(supplierDefaultDTO.getAccountEntity().getBankAccount());

        assetPurchaseOrderSupplierEntity.setAssetPurchaseOrderId(assetPurchaseOrderEntity.getId());
    }

    @Override
    public List<AssetPurchaseOrderDTO.SelectDTO> selectList(AssetPurchaseOrderDTO.SelectParamDTO paramDTO) {
        // 构建查询条件
        LambdaQueryWrapper<AssetPurchaseOrderEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询已审核的订单
        queryWrapper.eq(AssetPurchaseOrderEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode());

        // 只查询未作废的订单
        queryWrapper.eq(AssetPurchaseOrderEntity::getInvalidStatus, Boolean.FALSE);

        // 关键字查询：支持code模糊查询
        if (StrUtil.isNotBlank(paramDTO.getKeyword())) {
            queryWrapper.like(AssetPurchaseOrderEntity::getCode, paramDTO.getKeyword());
        }

        // 按采购日期倒序排列
        queryWrapper.orderByDesc(AssetPurchaseOrderEntity::getPurchaseDate);

        // 查询数据
        List<AssetPurchaseOrderEntity> entityList = this.list(queryWrapper);

        if (CollUtil.isEmpty(entityList)) {
            return new ArrayList<>();
        }

        // 获取所有订单ID
        List<String> orderIds = entityList.stream()
                .map(AssetPurchaseOrderEntity::getId)
                .collect(Collectors.toList());

        // 批量查询供应商信息
        List<AssetPurchaseOrderSupplierEntity> supplierList = assetPurchaseOrderSupplierService.list(
                new LambdaQueryWrapper<AssetPurchaseOrderSupplierEntity>()
                        .in(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId, orderIds)
        );

        // 构建订单ID到供应商信息的映射
        Map<String, AssetPurchaseOrderSupplierEntity> supplierMap = supplierList.stream()
                .collect(Collectors.toMap(
                        AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        // 转换为DTO
        return entityList.stream().map(entity -> {
            AssetPurchaseOrderDTO.SelectDTO selectDTO = new AssetPurchaseOrderDTO.SelectDTO();
            selectDTO.setId(entity.getId());
            selectDTO.setCode(entity.getCode());
            selectDTO.setPurchaseDate(entity.getPurchaseDate());
            selectDTO.setPurchaseUserName(entity.getPurchaseUserName());
            selectDTO.setApproveStatus(entity.getApproveStatus());
            selectDTO.setApproveStatusName(ApproveStatusEnum.getName(entity.getApproveStatus()));

            // 设置供应商信息
            AssetPurchaseOrderSupplierEntity supplier = supplierMap.get(entity.getId());
            if (supplier != null) {
                selectDTO.setSupplierId(supplier.getSupplierId());
                selectDTO.setSupplierName(supplier.getSupplierName());
            }

            return selectDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AssetPurchaseOrderDTO.DetailForAcceptDTO> queryDetailsForAccept(String assetPurchaseOrderId) {
        if (StrUtil.isBlank(assetPurchaseOrderId)) {
            return new ArrayList<>();
        }

        // 查询资产采购订单明细
        List<AssetPurchaseOrderDetailEntity> detailList = assetPurchaseOrderDetailService.lambdaQuery()
                .eq(AssetPurchaseOrderDetailEntity::getMainId, assetPurchaseOrderId)
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, false)
                .list();

        if (CollUtil.isEmpty(detailList)) {
            return new ArrayList<>();
        }

        // 转换为DTO（数量计算由 FMS 模块负责）
        return detailList.stream().map(detail -> {
            AssetPurchaseOrderDTO.DetailForAcceptDTO dto = new AssetPurchaseOrderDTO.DetailForAcceptDTO();
            dto.setId(detail.getId());
            dto.setSkuId(detail.getAssetId());
            dto.setSkuNo(detail.getAssetCode());
            dto.setProductName(detail.getAssetName());
            dto.setPurchaseQty(detail.getPurchaseQty() != null ? detail.getPurchaseQty().intValue() : 0);
            dto.setIsUrgent(detail.getIsUrgent());
            dto.setRemark(detail.getRemark());
            dto.setMoldCode(detail.getAssetCode()); // 模具编码使用资产编码
            dto.setMoldName(detail.getAssetName()); // 模具名称使用资产名称
            return dto;
        }).collect(Collectors.toList());
    }
}
