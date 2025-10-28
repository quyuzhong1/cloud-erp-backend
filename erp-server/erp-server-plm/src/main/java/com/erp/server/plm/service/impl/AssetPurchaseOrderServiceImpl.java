package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.ApproveType;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.plm.dto.AssetNoticeDetailDTO;
import com.erp.model.plm.dto.AssetPurchaseOrderDetailDTO;
import com.erp.model.plm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.model.plm.dto.excel.AssetPurchaseOrderImportExcelDTO;
import com.erp.model.plm.entity.*;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.function.Function;
import com.erp.model.plm.enums.AssetApproveStatusEnum;
import com.erp.model.plm.enums.AssetPurchaseOrderTypeEnum;
import com.erp.model.plm.enums.MoldInfoTagEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.KingdeeFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.fms.feign.AssetAceptFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.AssetPurchaseOrderExcelListener;
import com.erp.server.plm.mapper.AssetNoticeDetailMapper;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeAssetPurchaseService;
import com.erp.server.plm.service.*;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ASSET_PURCHASE_ORDER;

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
    private SyncKingdeeAssetPurchaseService syncKingdeeAssetPurchaseService;

    @Autowired
    private AssetPurchaseChangeService assetPurchaseChangeService;

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private AssetNoticeService assetNoticeService;

    @Autowired
    private AssetNoticeDetailService assetNoticeDetailService;

    @Autowired
    private MoldInfoService moldInfoService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired
    private AssetNoticeDetailMapper assetNoticeDetailMapper;

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

    @Autowired
    private AssetAceptFeign assetAceptFeign;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private KingdeeFeign kingdeeFeign;


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
        assetPurchaseOrderDetailService.add(addDTO, assetPurchaseOrderEntity.getId());
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
        assetPurchaseOrderDetailService.update(addOrUpdateDTO, assetPurchaseOrderEntity.getId());

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
        List<AssetPurchaseOrderDTO.TabListDTO> returnList = new ArrayList<>();
        List<AssetPurchaseOrderDTO.TabListDTO> list = baseMapper.tabList(searchParam);


        // 获取状态列表
        List<String> statusList = AssetApproveStatusEnum.getStatusList();

        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(AssetPurchaseOrderDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        for (AssetPurchaseOrderDTO.TabListDTO tabListDTO : list) {
            tabListDTO.setTabFlagName(AssetApproveStatusEnum.getName(tabListDTO.getTabFlag()));
        }

        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new AssetPurchaseOrderDTO.TabListDTO(status, AssetApproveStatusEnum.getName(status), 0));
            }
        });

        // 合计数量要放第一个
        returnList.add(new AssetPurchaseOrderDTO.TabListDTO("all", AssetApproveStatusEnum.ALL.getName() ,list.stream().mapToInt(AssetPurchaseOrderDTO.TabListDTO::getCount).sum()));
        returnList.addAll(list);

        return returnList;
    }

    @Override
    public void exportList(AssetPurchaseOrderDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("资产采购单导出", EXPORT_PLM_ASSET_PURCHASE_ORDER.getCode(), param);
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
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getId(), "资产采购单提交操作");
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
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getCode())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "", approveType.getName(), dto.getComment());
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getId(), "资产采购单审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetPurchaseOrderEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
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

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getId(), "资产采购单反审核操作");

        //发送金蝶
        sendPushTask(Arrays.asList(entity), SyncOperateEnum.OPERATE_DISAPPROVE.getCode());

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetPurchaseOrderEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        LambdaQueryWrapper<AssetPurchaseChangeEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssetPurchaseChangeEntity::getSourceId,entity.getId())
                .eq(AssetPurchaseChangeEntity::getIsDeleted,Boolean.FALSE);

        List<AssetPurchaseChangeEntity> purchaseChangeList = assetPurchaseChangeService.list(lambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(purchaseChangeList)) {
            throw new ServiceException(ApiError.ERROR_95306);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetPurchaseOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getCode(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        //删除明细
        assetPurchaseOrderDetailService.removeById(id,null);

        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getCode(), "删除数据");
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
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getId(), "作废操作");
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

        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    public AssetPurchaseOrderDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        //资产通知单
        LambdaQueryWrapper<AssetNoticeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetNoticeEntity::getIsDeleted,Boolean.FALSE);
        List<AssetNoticeEntity> assetNoticeEntityList = assetNoticeService.list(queryWrapper);

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getName, DictBasicDTO::getId,(o1,o2)->o1));

        //付款条件
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();
        Map<String, String> paymentConditionMap = paymentConditionList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getValue, BaseDropDownDTO.DisabledDTO::getCode,(o1,o2)->o1));

        //sku
        List<SkuVO> skuVOList = productDetailMapper.listAssetProduct();
        //核算公司
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //供应商信息
        List<SupplierEntity> supplierEntitiyList = scmTaskFeign.listSupplier();
        //供应商联系人
        List<SupplierContactEntity> supplierContactEntityList = scmTaskFeign.listSupplierContact();
        //供应商账户
        List<SupplierAccountEntity> supplierAccountEntityList = scmTaskFeign.listSupplierAccount();

        AssetPurchaseOrderExcelListener excelListenerUtil = new AssetPurchaseOrderExcelListener(skuVOList,
                userList,
                deptList,
                companyList,
                assetNoticeEntityList,
                settleDictMap,
                paymentConditionMap,
                supplierEntitiyList,
                supplierContactEntityList,
                supplierAccountEntityList);

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), AssetPurchaseOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<AssetPurchaseOrderImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        AssetPurchaseOrderDTO.ImportDTO importDTO = new AssetPurchaseOrderDTO.ImportDTO();
        //导入数据处理
        List<AssetPurchaseOrderDetailDTO.MoldImportDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<AssetPurchaseOrderImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "模具采购单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetNoticeImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetPurchaseOrderEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        if (dto.getType().equals(ApproveType.PASS)) {
            //发送金蝶
            sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }

    private void sendPushTask(List<AssetPurchaseOrderEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeAssetPurchaseService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    @Override
    public Boolean updateContractStampStatus(AssetPurchaseOrderDTO.ContractStampStatusParamsDTO dto) {
        List<String> ids = dto.getIds();
        if (CollUtil.isNotEmpty(ids) && StringUtils.isNotBlank(dto.getContractStampStatus())) {
            List<AssetPurchaseOrderEntity> assetPurchaseOrderEntityList = new ArrayList<>();
            List<AssetPurchaseOrderEntity> oldList = listByIds(ids);
            assetPurchaseOrderEntityList.addAll(oldList);
            lambdaUpdate()
                    .set(AssetPurchaseOrderEntity::getContractStampStatus, dto.getContractStampStatus())
                    .in(AssetPurchaseOrderEntity::getId, ids)
                    .update();

            //操作日志
            String name = ContractStampStatusEnum.getName(dto.getContractStampStatus());

            for (AssetPurchaseOrderEntity purchaseOrderEntity : assetPurchaseOrderEntityList) {
                AssetPurchaseOrderEntity oldEntity = oldList.stream().filter(old -> old.getId().equals(purchaseOrderEntity.getId())).findFirst().orElse(null);
                String msg = StrUtil.format("{}合同盖章状态由【{}】变更为：【{}】", purchaseOrderEntity.getCode() ,  ContractStampStatusEnum.getName(oldEntity.getContractStampStatus()) , ContractStampStatusEnum.getName(purchaseOrderEntity.getContractStampStatus()));
                operateLogService.addSysLogByUpdate(
                        oldEntity,
                        purchaseOrderEntity,
                        ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(),
                        purchaseOrderEntity.getId(),
                        "资产采购单",
                        msg
                );
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public AssetPurchaseOrderDTO.ViewDTO view(String id) {
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
        AssetPurchaseOrderDTO.ViewDTO data = BeanMapperUtils.map(AssetPurchaseOrderDTO.ViewDTO.class, assetPurchaseOrderEntity);
        // 数据填充处理
        fillOne(data);

        LambdaQueryWrapper<AssetPurchaseOrderDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetPurchaseOrderDetailEntity::getMainId,id)
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted,Boolean.FALSE);
        List<AssetPurchaseOrderDetailEntity> detailList = assetPurchaseOrderDetailService.list(queryWrapper);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_95307);
        }
        List<AssetPurchaseOrderDetailDTO.ViewDTO> dtoList = BeanMapperUtils.copyList(AssetPurchaseOrderDetailDTO.ViewDTO.class, detailList);
        fillViewList(dtoList);
        data.setAssetPurchaseOrderDetailDTOList(dtoList);
        return data;
    }

    public void fillViewList(List<AssetPurchaseOrderDetailDTO.ViewDTO> dtoList){

        for (AssetPurchaseOrderDetailDTO.ViewDTO detailDTO : dtoList) {
            List<AssetNoticeDetailDTO.AssetDetailRefSkuDTO> assetNoticeDetailRefSkuDTOS = assetNoticeDetailMapper.searchMoldRefSkuByAssetId(detailDTO.getAssetId());
            //关联sku信息
            List<AssetPurchaseOrderDetailDTO.AssetDetailRefSkuDTO> assetDetailRefSkuDTOS = BeanMapperUtils.copyList(AssetPurchaseOrderDetailDTO.AssetDetailRefSkuDTO.class, assetNoticeDetailRefSkuDTOS);
            detailDTO.setAssetDetailRefSkuDTOList(assetDetailRefSkuDTOS);
            detailDTO.setTagName(MoldInfoTagEnum.getName(detailDTO.getTag()));
        }

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
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AssetPurchaseOrderDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setContractStampStatusName(ContractStampStatusEnum.getName(data.getContractStampStatus()));
            data.setEndReceiveName(AssetPurchaseOrderReceiveEnum.getName(data.getEndReceive()));
            data.setOrderTypeName(AssetPurchaseOrderTypeEnum.getNameByCode(data.getOrderType()));
            //从资产验收单获取
            Integer acceptQty = assetAceptFeign.getAcceptQtyByDetailId(data.getId());
            BigDecimal parseAcceptQty = acceptQty == null ? BigDecimal.ZERO : new BigDecimal(acceptQty);
            //如果是结束验收状态,待验收数为0
            if (AssetPurchaseOrderReceiveEnum.CLOSE.getCode().equals(data.getEndReceive())) {
                data.setAcceptQty(data.getPurchaseQty().subtract(parseAcceptQty));
                data.setUnAcceptQty(BigDecimal.ZERO);
            } else {
                data.setAcceptQty(parseAcceptQty);
                data.setUnAcceptQty(data.getPurchaseQty().subtract(parseAcceptQty));
            }

        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetPurchaseOrderEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetPurchaseOrderEntity assetPurchaseOrderEntity) {
        assetPurchaseOrderEntity.setContractStampStatus(ContractStampStatusEnum.WAIT_SUBMIT.getCode());
        assetPurchaseOrderEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
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

    private void handleSupplierData(AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity,AssetPurchaseOrderEntity assetPurchaseOrderEntity) {
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
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();
        Map<String, String> paymentConditionMap = paymentConditionList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getCode, BaseDropDownDTO.DisabledDTO::getValue));
        assetPurchaseOrderSupplierEntity.setPaymentConditionName(paymentConditionMap.getOrDefault(assetPurchaseOrderSupplierEntity.getPaymentCondition(),""));

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getId, DictBasicDTO::getName));
        assetPurchaseOrderSupplierEntity.setPayMethodName(settleDictMap.getOrDefault(assetPurchaseOrderSupplierEntity.getPayMethodId(),""));

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

    @Override
    public AssetPurchaseOrderDTO.DetailWithSkuDTO getByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }

        // 查询未删除且审核通过的资产采购订单
        AssetPurchaseOrderEntity entity = this.lambdaQuery()
                .eq(AssetPurchaseOrderEntity::getCode, code)
                .eq(AssetPurchaseOrderEntity::getIsDeleted, false)
                .eq(AssetPurchaseOrderEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                .one();

        if (entity == null) {
            log.warn("未找到已审核通过的资产采购订单，订单号：{}", code);
            return null;
        }

        // 查询订单明细
        List<AssetPurchaseOrderDTO.DetailForAcceptDTO> detailList = queryDetailsForAccept(entity.getId());

        // 查询供应商信息
        AssetPurchaseOrderSupplierEntity supplierEntity = assetPurchaseOrderSupplierService.lambdaQuery()
                .eq(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId, entity.getId())
                .eq(AssetPurchaseOrderSupplierEntity::getIsDeleted, false)
                .one();

        // 封装返回结果
        AssetPurchaseOrderDTO.DetailWithSkuDTO result = new AssetPurchaseOrderDTO.DetailWithSkuDTO();
        result.setId(entity.getId());
        result.setCode(entity.getCode());
        result.setDetailList(detailList);
        
        // 设置供应商信息
        if (supplierEntity != null) {
            result.setSupplierId(supplierEntity.getSupplierId());
            result.setSupplierName(supplierEntity.getSupplierName());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleImportSuccessList(List<AssetPurchaseOrderDetailDTO.MoldImportDTO> successList) throws Exception{
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 按 serialNumber 分组
        Map<String, List<AssetPurchaseOrderDetailDTO.MoldImportDTO>> groupedBySerialNumber = successList.stream()
                .collect(Collectors.groupingBy(AssetPurchaseOrderDetailDTO.MoldImportDTO::getSerialNumber));

        try {
            for (Map.Entry<String, List<AssetPurchaseOrderDetailDTO.MoldImportDTO>> entry : groupedBySerialNumber.entrySet()) {
                String serialNumber = entry.getKey();
                List<AssetPurchaseOrderDetailDTO.MoldImportDTO> moldImportDTOList = entry.getValue();

                if (CollectionUtils.isEmpty(moldImportDTOList)) {
                    continue;
                }

                // 取第一个元素作为主表数据
                AssetPurchaseOrderDetailDTO.MoldImportDTO firstMoldImportDTO = moldImportDTOList.get(0);
                AssetPurchaseOrderEntity entity = new AssetPurchaseOrderEntity();
                entity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPL));
                entity.setSourceType(firstMoldImportDTO.getSourceType());
                entity.setSourceCode(firstMoldImportDTO.getSourceCode());
                entity.setSourceId(firstMoldImportDTO.getSourceId());
                entity.setPurchaseDate(firstMoldImportDTO.getPurchaseDate());
                entity.setPurchaseUserId(firstMoldImportDTO.getPurchaseUserId());
                entity.setPurchaseUserName(firstMoldImportDTO.getPurchaseUserName());
                entity.setPurchaseDeptId(firstMoldImportDTO.getPurchaseDeptId());
                entity.setPurchaseDeptName(firstMoldImportDTO.getPurchaseDeptName());
                entity.setPurchaseOrgId(firstMoldImportDTO.getPurchaseOrgId());
                entity.setPurchaseOrgName(firstMoldImportDTO.getPurchaseOrgName());
                entity.setOrderType(AssetPurchaseOrderTypeEnum.ASSET_PURCHASE.getCode());

                entity.setInvalidStatus(Boolean.FALSE);
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
                entity.setContractStampStatus(ContractStampStatusEnum.WAIT_SUBMIT.getCode());

                // 保存主表
                boolean saveAssetPurchase = super.save(entity);
                if (!saveAssetPurchase) {
                    throw new ServiceException("资产采购单头导入保存失败");
                }

                // 处理供应商数据
                AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity = new AssetPurchaseOrderSupplierEntity();

                BeanUtils.copyProperties(firstMoldImportDTO.getSupplierImportDTO(),assetPurchaseOrderSupplierEntity);
                assetPurchaseOrderSupplierEntity.setAssetPurchaseOrderId(entity.getId());

                boolean savePurchaseSupplier = assetPurchaseOrderSupplierService.save(assetPurchaseOrderSupplierEntity);
                if (!savePurchaseSupplier) {
                    throw new ServiceException("资产采购供应商导入保存失败");
                }

                // 处理明细数据
                List<AssetPurchaseOrderDetailEntity> assetPurchaseOrderDetailEntities = new ArrayList<>();
                for (AssetPurchaseOrderDetailDTO.MoldImportDTO moldImportDTO : moldImportDTOList) {
                    List<AssetPurchaseOrderDetailDTO.MoldDetailImportDTO> moldDetailImportDTOList = moldImportDTO.getMoldDetailImportDTOList();
                    //从价表查询价格
                    List<PurchasePriceDTO.PriceDTO> convertList = convertMoldDetailToPriceDTO(moldDetailImportDTOList,
                            firstMoldImportDTO.getPurchaseOrgId(),
                            firstMoldImportDTO.getSupplierImportDTO().getSupplierId());
                    List<PurchasePriceDTO.PriceDTO> priceDTOList = scmTaskFeign.batchGetPurchasePrice(convertList);
                    if (priceDTOList.isEmpty()) {
                        throw new ServiceException("查询采购价目表失败");
                    }
                    for (AssetPurchaseOrderDetailDTO.MoldDetailImportDTO moldDetailImportDTO : moldDetailImportDTOList) {
                        AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
                        BeanMapperUtils.copy(moldDetailImportDTO, assetPurchaseOrderDetailEntity);
                        assetPurchaseOrderDetailEntity.setMainId(entity.getId()); // 关联主表ID

                        AssetNoticeDetailEntity assetNoticeDetailEntity = assetNoticeDetailService.lambdaQuery()
                                .eq(AssetNoticeDetailEntity::getMainId, firstMoldImportDTO.getSourceId())
                                .eq(AssetNoticeDetailEntity::getIsDeleted, Boolean.FALSE)
                                .eq(AssetNoticeDetailEntity::getAssetCode, moldDetailImportDTO.getAssetCode())
                                .one();
                        if (Objects.isNull(assetNoticeDetailEntity)) {
                            throw new ServiceException("没有查到开模通知单");
                        }
                        assetPurchaseOrderDetailEntity.setSourceDetailId(assetNoticeDetailEntity.getId());

                        PurchasePriceDTO.PriceDTO priceDTO = priceDTOList.stream()
                                .filter(obj -> obj.getSkuId().equals(assetPurchaseOrderDetailEntity.getAssetId()))
                                .findFirst()
                                .orElse(null);

                        MoldInfoEntity moldInfoEntity = moldInfoService.lambdaQuery()
                                .eq(MoldInfoEntity::getCode, moldDetailImportDTO.getAssetCode())
                                .eq(MoldInfoEntity::getIsDeleted, Boolean.FALSE)
                                .one();
                        assetPurchaseOrderDetailEntity.setTag(moldInfoEntity.getTag());

                        assetPurchaseOrderDetailEntity.setTaxPrice(priceDTO.getTaxPrice());
                        assetPurchaseOrderDetailEntity.setTaxRate(priceDTO.getTaxRate());
                        assetPurchaseOrderDetailEntity.setCurrency(priceDTO.getCurrency());
                        assetPurchaseOrderDetailEntity.setCurrencySymbol(priceDTO.getCurrency());
                        assetPurchaseOrderDetailEntity.setTotalAmount(new BigDecimal(priceDTO.getAmount()));

                        assetPurchaseOrderDetailEntities.add(assetPurchaseOrderDetailEntity);
                    }
                }

                // 批量保存明细
                boolean saveDetail = assetPurchaseOrderDetailService.saveBatch(assetPurchaseOrderDetailEntities);
                if (!saveDetail) {
                    throw new ServiceException("资产采购单明细导入保存失败");
                }

                // 记录操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】",
                        UserContext.getDefaultLoginUser().getUserName(),
                        "资产采购单",
                        entity.getCode());
                operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(), entity.getId(), "新增操作");
            }
        } catch (Exception e) {
            throw new ServiceException("资产采购单导入保存失败", e);
        }
    }

    @Override
    public Boolean exportPurchaseContract(String id, HttpServletResponse response) {

        AssetPurchaseOrderDTO.ExportPurchaseContractDTO contractDTO = new AssetPurchaseOrderDTO.ExportPurchaseContractDTO();
        AssetPurchaseOrderEntity purchaseOrderEntity = this.getById(id);

        contractDTO.setCreateTime(purchaseOrderEntity.getCreateTime());
        contractDTO.setApproveUserName(purchaseOrderEntity.getApproveUserName());
        contractDTO.setCode(purchaseOrderEntity.getCode());
        contractDTO.setCreateUserName(purchaseOrderEntity.getCreateUserName());

        //摘要
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getId, DictBasicDTO::getName));
        contractDTO.setSettleMethod(settleDictMap.get("supplierPayMode"));

        //供应商
        AssetPurchaseOrderSupplierEntity supplierEntity = assetPurchaseOrderSupplierService.lambdaQuery()
                .eq(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId, purchaseOrderEntity.getId())
                .one();
        contractDTO.setSupplierName(supplierEntity.getSupplierName());
        List<AssetPurchaseOrderDetailEntity> purchaseOrderDetailEntityList = assetPurchaseOrderDetailService.lambdaQuery()
                .eq(AssetPurchaseOrderDetailEntity::getMainId,purchaseOrderEntity.getId())
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted,Boolean.FALSE)
                .list();

        //计算总数
        BigDecimal sum = purchaseOrderDetailEntityList.stream()
                .map(AssetPurchaseOrderDetailEntity::getPurchaseQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        contractDTO.setSumQty(sum);

        //计算总价
        BigDecimal sumTaxAmount = purchaseOrderDetailEntityList.stream()
                .map(AssetPurchaseOrderDetailEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        contractDTO.setSumTaxAmount(sumTaxAmount);

        List<AssetPurchaseOrderDTO.PurchaseContractDetailDTO> contractDetailList = new ArrayList<>();
        List<String> skuIdList = purchaseOrderDetailEntityList.stream().map(AssetPurchaseOrderDetailEntity::getAssetId).collect(Collectors.toList());
        List<SkuVO> skuList = productDetailService.listSkuProductByIds(skuIdList);
        Integer sort = MathUtil.ZERO;
        for (AssetPurchaseOrderDetailEntity detailEntity : purchaseOrderDetailEntityList) {
            sort++;
            SkuVO skuVO = skuList.stream().filter(req -> req.getSkuId().equals(detailEntity.getAssetId())).findFirst().orElse(new SkuVO());
            AssetPurchaseOrderDTO.PurchaseContractDetailDTO detailDTO = new AssetPurchaseOrderDTO.PurchaseContractDetailDTO();
            detailDTO.setSort(sort);
            detailDTO.setImg("");
            detailDTO.setSkuNo(detailEntity.getAssetCode());
            detailDTO.setProductName(skuVO.getSkuName());
            detailDTO.setRemark(detailEntity.getRemark());
            BigDecimal qty = detailEntity.getPurchaseQty();
            detailDTO.setQty(qty);
            //含税单价
            BigDecimal taxPrice = detailEntity.getTaxPrice();
            detailDTO.setTaxPrice(taxPrice);
            //0.0900
            BigDecimal taxRate = detailEntity.getTaxRate();
            BigDecimal flagTaxRate = BigDecimal.ZERO;
            if (Objects.nonNull(taxRate)) {
                flagTaxRate = MathUtil.multiplyWithTwo(taxRate, MathUtil.BigDecimal_100).setScale(2);
            }
            detailDTO.setTaxRate(flagTaxRate + "%");
            BigDecimal multiplyTax = MathUtil.add(taxRate, MathUtil.BigDecimal_1);
            //未税单价
            BigDecimal price = BigDecimal.ZERO;
            if (Objects.nonNull(taxPrice)) {
                price = MathUtil.divide(taxPrice, multiplyTax);
            }
            detailDTO.setPrice(price);
            //未税金额
            detailDTO.setTotalAmount(MathUtil.multiplyWithTwo(price, qty));
            //单位
            detailDTO.setUnit(skuVO.getUnitName());
            //含税金额
            detailDTO.setTaxAmount(detailEntity.getTotalAmount());
            contractDetailList.add(detailDTO);
        }
        BigDecimal sumAmount = contractDetailList.stream().map(AssetPurchaseOrderDTO.PurchaseContractDetailDTO::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        contractDTO.setSumAmount(sumAmount);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/assetPurchaseContractExport.xlsx";
        String name = "采购单网采合同";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);

        try {
            new ExcelPrintUtils().patchExport(contractDetailList, contractDTO, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("网采合同导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public AssetPurchaseOrderDTO.ExportPdfDTO listPurchaseContractPdf(String id) {
        AssetPurchaseOrderDTO.ExportPdfDTO exportPdfDTO = new AssetPurchaseOrderDTO.ExportPdfDTO();

        AssetPurchaseOrderEntity assetPurchaseOrderEntity = this.getById(id);
        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(assetPurchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_95307);
        }

        List<AssetPurchaseOrderDetailEntity> list = assetPurchaseOrderDetailService.lambdaQuery()
                .eq(AssetPurchaseOrderDetailEntity::getMainId,id)
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted,Boolean.FALSE).list();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_95308);
        }
        //主数据处理
        exportPdfDTO.setCode(assetPurchaseOrderEntity.getCode());
        exportPdfDTO.setCodeStr("合同号：" + assetPurchaseOrderEntity.getCode());
        //采购组织
        exportPdfDTO.setPurchaseOrgName(assetPurchaseOrderEntity.getPurchaseOrgName());
        //甲方签收日期
        exportPdfDTO.setFirstSignDate(assetPurchaseOrderEntity.getCreateTime().toLocalDate());
        //乙方签收日期
        exportPdfDTO.setSecondSignDate(assetPurchaseOrderEntity.getCreateTime().toLocalDate());

        //查询订单供应商信息
        AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplier = assetPurchaseOrderSupplierService.lambdaQuery()
                .eq(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId,id)
                .eq(AssetPurchaseOrderSupplierEntity::getIsDeleted,Boolean.FALSE)
                .one();
        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(assetPurchaseOrderSupplier)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        exportPdfDTO.setSupplierTel(assetPurchaseOrderSupplier.getContactTelNumber());
        // 采购订单供应商付款条件
        String paymentConditionCode = assetPurchaseOrderSupplier.getPaymentCondition();
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            List<BaseDropDownDTO.DisabledDTO> paymentConditionList = scmTaskFeign.listPaymentCondition();
            BaseDropDownDTO.DisabledDTO disabledDTO = paymentConditionList.stream().filter(obj -> obj.getCode().equals(paymentConditionCode)).findFirst().orElse(null);
            if (Objects.nonNull(disabledDTO)) {
                exportPdfDTO.setPaymentConditionName(disabledDTO.getValue());
            }
        }

        //结算方式
        List<DictBasicEntity> payMethodList = scmDictFeign.listDictByIdList(Arrays.asList(assetPurchaseOrderSupplier.getPayMethodId()));
        if (!payMethodList.isEmpty()) {
            exportPdfDTO.setPayMethodName(payMethodList.get(0).getName());
        }

        //原供应商信息
        SupplierEntity supplier = scmTaskFeign.getSupplierById(assetPurchaseOrderSupplier.getSupplierId());
        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商账号信息
        String supplierBankNo = "";
        String supplierBankName = "";
        String supplierAccountName = "";

        if (Objects.nonNull(assetPurchaseOrderSupplier)) {
            supplierBankNo = Objects.nonNull(assetPurchaseOrderSupplier) ? assetPurchaseOrderSupplier.getBankAccount() : "";
            supplierBankName = Objects.nonNull(assetPurchaseOrderSupplier) ? assetPurchaseOrderSupplier.getBankName() : "";
            supplierAccountName = Objects.nonNull(assetPurchaseOrderSupplier) ? assetPurchaseOrderSupplier.getPayee() : "";
        }
        exportPdfDTO.setSupplierBankName(supplierBankName);
        exportPdfDTO.setSupplierBankNo(supplierBankNo);
        exportPdfDTO.setSupplierAccountName(supplierAccountName);

        exportPdfDTO.setSupplierName(supplier.getName());
        exportPdfDTO.setSupplierAddress(supplier.getCompanyAddress());


        //供应商联系人信息
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(assetPurchaseOrderSupplier.getContactId())) {
            SupplierContactEntity supplierContact = scmTaskFeign.getSupplierContactById(assetPurchaseOrderSupplier.getContactId());
            if (ObjectUtils.isEmpty(supplierContact)) {
                throw new ServiceException(ApiError.ERROR_98039);
            }
            exportPdfDTO.setSupplierEmail(supplierContact.getEmail());
            exportPdfDTO.setSupplierContract(supplierContact.getPerson());
        }

        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        DecimalFormat df4 = new DecimalFormat("#,##0.0000");
        //明细物料信息
        List<AssetPurchaseOrderDetailDTO.ExportPdfDTO> details = new ArrayList<>();
        for (AssetPurchaseOrderDetailEntity purchaseOrderDetailEntity : list) {
            AssetPurchaseOrderDetailDTO.ExportPdfDTO detailDTO = new AssetPurchaseOrderDetailDTO.ExportPdfDTO();
            BeanMapperUtils.copy(purchaseOrderDetailEntity, detailDTO);
            //明细数据处理
            detailDTO.setUnitName("个");
            //不含税单价（不含税价格=含税价格/（1+增值税税率））
            detailDTO.setPrice(MathUtil.divide(detailDTO.getTaxPrice(), MathUtil.add(BigDecimal.ONE, detailDTO.getTaxRate())));
            //不含税单价 增加千分位分割
            detailDTO.setPriceStr(df4.format(detailDTO.getPrice()));
            //含税金额 增加千分位分割
            detailDTO.setTaxPriceStr(df4.format(detailDTO.getTaxPrice()));
            //不含税金额
            detailDTO.setNotTaxPurchaseAmount(MathUtil.multiplyWithTwo(detailDTO.getPrice(), detailDTO.getPurchaseQty()).setScale(2, RoundingMode.HALF_UP));
            //不含税金额 增加千分位分割
            detailDTO.setNotTaxPurchaseAmountStr(df2.format(detailDTO.getNotTaxPurchaseAmount()));
            //含税金额 增加千分位分割
            detailDTO.setTotalAmountStr(df2.format(detailDTO.getTotalAmount()));
            detailDTO.setTaxRate(MathUtil.multiplyWithTwo(detailDTO.getTaxRate(), MathUtil.BigDecimal_100));
            details.add(detailDTO);
        }
        //含税金额合计
        BigDecimal totalAmount = details.stream().map(AssetPurchaseOrderDetailDTO.ExportPdfDTO::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        exportPdfDTO.setTotalAmount(totalAmount);
        //含税金额合计  增加千分位分割
        exportPdfDTO.setTotalAmountStr(df2.format(totalAmount));
        //不含税金额合计
        BigDecimal totalNotTaxAmount = details.stream().map(AssetPurchaseOrderDetailDTO.ExportPdfDTO::getNotTaxPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        exportPdfDTO.setTotalNotTaxAmount(totalNotTaxAmount);
        //不含税金额合计  增加千分位分割
        exportPdfDTO.setTotalNotTaxAmountStr(df2.format(totalNotTaxAmount));
        String currency = list.get(0).getCurrency();
        String currencyName = "";
        if (StrUtil.isNotBlank(currency)) {
            currencyName = CurrencyEnum.getNameByCode(currency);
        }
        //将totalNotTaxAmount转换为中文大写
        String totalNotTaxAmountChinese = Convert.digitToChinese(totalAmount.doubleValue());
        exportPdfDTO.setTotalNotTaxAmountChinese(currencyName + totalNotTaxAmountChinese);
        exportPdfDTO.setCurrency(currency);
        exportPdfDTO.setDetails(details);
        return exportPdfDTO;
    }

    @Override
    public ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>> getAcceptByDetailId(String detailId) {
        return assetAceptFeign.getAcceptByDetailId(detailId);
    }

    public static List<PurchasePriceDTO.PriceDTO> convertMoldDetailToPriceDTO(
            List<AssetPurchaseOrderDetailDTO.MoldDetailImportDTO> moldDetailImportDTOList,
            String purchaseOrgId,
            String supplierId) {

        return moldDetailImportDTOList.stream()
                .map(moldDetail -> PurchasePriceDTO.PriceDTO.builder()
                        .purchaseOrgId(purchaseOrgId)
                        .skuId(moldDetail.getAssetId())
                        .supplierId(supplierId)
                        .qty(moldDetail.getPurchaseQty() != null ? moldDetail.getPurchaseQty().intValue() : null)
                        .build())
                .collect(Collectors.toList());
    }

}
