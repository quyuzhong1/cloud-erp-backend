package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractOrderService;
import com.erp.server.scm.mapper.SubcontractOrderMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 委外订单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractOrderServiceImpl extends SuperServiceImpl<SubcontractOrderMapper, SubcontractOrderEntity> implements SubcontractOrderService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService operateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SyncKingdeeSubcontractOrderService syncKingdeeSubcontractOrderService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WarehouseLocationFeign warehouseLocationFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private SubcontractIssueFeign subcontractIssueFeign;

    @Resource
    private InventoryCloseRecordFeign inventoryCloseRecordFeign;

    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;

    @Override
    public PagingVO<SubcontractOrderDTO.ListDTO> paging(PagingDTO<SubcontractOrderDTO.PagingParamDTO> pagingParamDTO) {
        SubcontractOrderDTO.PagingParamDTO params = pagingParamDTO.getParams();
        params.setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        //列表Tab查询状态处理
        Boolean isFlag = doOpHandleTableParam(params);
        if (!isFlag) {
            return new PagingVO(new Page());
        }
        IPage<SubcontractOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SubcontractOrderDTO.TabListDTO> tabList(PermissionsDTO param) {
        PurchaseTableFlagEnum[] values = PurchaseTableFlagEnum.values();
        List<SubcontractOrderDTO.TabListDTO> list = new ArrayList<>();
        for (PurchaseTableFlagEnum item : values) {
            SubcontractOrderDTO.PagingParamDTO searchParamDTO = new SubcontractOrderDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(param.getPermissionSql());
            SubcontractOrderDTO.TabListDTO resultDTO = new SubcontractOrderDTO.TabListDTO();
            //搜索类型
            searchParamDTO.setSearchType(item.getCode());
            //列表Tab查询状态处理
            Boolean isFlag = doOpHandleTableParam(searchParamDTO);
            Integer count = MathUtil.ZERO;
            if (isFlag) {
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setSearchType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public void exportList(SubcontractOrderDTO.ExportDTO param, HttpServletResponse response) {
        //列表Tab查询状态处理
        Boolean isFlag = doOpHandleTableParam(param);
        if (!isFlag) {
            return;
        }
        List<SubcontractOrderDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/subcontractOrder.xlsx";
        String name = "委外订单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public List<PurchaseOrderDTO.ListDTO> listPurchaseOrderByDetailId(String detailId) {
        List<String> detailIds = new ArrayList<>();
        detailIds.add(detailId);
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetailService.listByParentIds(detailIds);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_98072);
        }
        childList.forEach(obj -> detailIds.add(obj.getId()));
        List<PurchaseOrderDTO.ListDTO> list = purchaseOrderService.listBySourceDetailIds(detailIds);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //列表数据处理
        purchaseOrderService.doOpHandlePurchaseOrder(list);
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishDelivery(List<String> ids, String remark) {
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }
        long count = detailList.stream().filter(obj -> !ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode().equals(obj.getArrivalStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98035);
        }
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listByParentIds(ids);
        if (CollectionUtils.isEmpty(subcontractOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98072);
        }
        List<String> childList = subcontractOrderDetailList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());

        //关联采购订单结束交货
        ids.addAll(childList);
        List<String> podIds = baseMapper.listPodIdsByDetailIds(ids);
        if (CollectionUtils.isNotEmpty(podIds)) {
            purchaseOrderDetailService.finishDelivery(podIds, remark,Boolean.FALSE);
        }
        //委外订单更新到货状态
        subcontractOrderDetailService.updateArrivalStatusByIds(ArrivalStatusEnum.ARRIVED.getCode(),ids,Boolean.TRUE);

        //操作日志
        List<Pair<String, String>> pairList = detailList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("SKU【%s】，结束原因：".concat(StrUtils.null2EmptyWithTrim(remark)), ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "结束交货操作");
        return Boolean.TRUE;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(SubcontractOrderDTO.AddDTO addDTO) {
        SubcontractOrderEntity subcontractOrderEntity = new SubcontractOrderEntity();
        BeanMapperUtils.copy(addDTO, subcontractOrderEntity);
        // 数据处理
        handleData(subcontractOrderEntity);

        log.info("开始新增委外订单");
        // 生成单号
        String code =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SUB);
        subcontractOrderEntity.setCode(code);

        boolean save = super.save(subcontractOrderEntity);
        if(!save) {
           throw new ServiceException("委外订单保存失败");
        }
        //新增明细
        subcontractOrderDetailService.add(addDTO.getDetailList(),subcontractOrderEntity.getId());

        // 操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个委外订单【%s】", subcontractOrderEntity.getCode()), ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), subcontractOrderEntity.getId(), "新增操作");
        return subcontractOrderEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SubcontractOrderDTO.UpdateDTO updateDTO) {
        SubcontractOrderEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException("未找到委外订单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        SubcontractOrderEntity subcontractOrderEntity =  BeanMapperUtils.map(SubcontractOrderEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractOrderEntity);

        log.info("编辑 开始修改委外订单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractOrderEntity);
        if(!save) {
           throw new ServiceException("委外订单保存失败");
        }

        //新增明细
        subcontractOrderDetailService.update(updateDTO.getDetailList(),subcontractOrderEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录委外订单日志数据，单号：【{}】", subcontractOrderEntity.getCode());
        operateLogService.addModuleOperateLogByObj(old, subcontractOrderEntity, ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), subcontractOrderEntity.getId(), "", "");
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
       if (CollUtil.isEmpty(ids)) {
          throw new ServiceException(ApiError.ERROR_98004);
       }
       List<SubcontractOrderEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
          throw new ServiceException("未找到委外订单数据");
       }
       // 待提交或审核不通过并且未作废允许提交
       long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
       if (count > 0) {
          throw new ServiceException(ApiError.ERROR_98010);
       }
        //提交流程
        startProcess(list);

       // 更新单据审核状态
       log.info("提交 开始修改委外订单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
       this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());

       // 记录操作日志
       log.info("提交 开始记录委外订单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
       operateLogService.batchAddModuleOperateLog("提交了一个委外订单【%s】", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(SubcontractOrderDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(Arrays.asList(id));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SubcontractOrderDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(Arrays.asList(dto.getId()));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SubcontractOrderEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //验证存货核算是否关账
        /*List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = Arrays.asList(new InventoryClosedRecordDTO.ClosedParamDTO(entity.getPurchaseOrgId(), entity.getBillDate()),
                new InventoryClosedRecordDTO.ClosedParamDTO(entity.getSubcontractOrgId(), entity.getBillDate()));
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);*/
        //调用审核流程
        approveProcess(entity, dto);

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外订单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SubcontractOrderEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus;
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE;
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
        }
        Boolean result = this.updateForApprove(Arrays.asList(entity.getId()), approveStatus.getStatus());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (dto.getType().equals(ApproveType.PASS)) {
            //自动生成采购订单
            autoGeneratePo(entity.getId());

            //发送金蝶
            sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        SubcontractOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外订单数据"));

        // 已审核支持反审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        //委外变更单
        List<SubcontractChangeEntity> subcontractChangeList = subcontractChangeService.listBySourceIds(Arrays.asList(id));
        //采购订单
        List<PurchaseOrderEntity> purchaseOrderList = purchaseOrderService.listBySourceIds(Arrays.asList(id));

        //委外发料单
        List<SubcontractIssueEntity> subcontractIssueList = subcontractIssueFeign.listBySourceIdList(Arrays.asList(id));

        //判断是否下推委外发料单
        if (CollectionUtils.isNotEmpty(subcontractIssueList)) {
            throw new ServiceException(ApiError.ERROR_SUB_PUSH_ISSUE,entity.getCode(),subcontractIssueList.get(0).getCode());
        }
        // 判断是否存在下推的采购订单
        if (CollectionUtils.isNotEmpty(purchaseOrderList)) {
            throw new ServiceException(ApiError.ERROR_98080,entity.getCode(),purchaseOrderList.get(0).getCode());
        }
        // 判断是否存在下推的变更单
        if (CollectionUtils.isNotEmpty(subcontractChangeList)) {
            throw new ServiceException(ApiError.ERROR_SUB_PUSH_CHANGE,entity.getCode(),subcontractChangeList.get(0).getCode());
        }

        //验证存货核算是否关账
        /*List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = Arrays.asList(new InventoryClosedRecordDTO.ClosedParamDTO(entity.getPurchaseOrgId(), entity.getBillDate()),
                new InventoryClosedRecordDTO.ClosedParamDTO(entity.getSubcontractOrgId(), entity.getBillDate()));
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);*/

        // 更新审核信息
        updateApproveStatus(Arrays.asList(id), ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //发送金蝶
        sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外订单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
       List<SubcontractOrderEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
         throw new ServiceException("未找到委外订单数据");
       }
       // 只有待提交且未作废的数据允许删除
       long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
       if (count > 0) {
         throw new ServiceException(ApiError.ERROR_98009);
       }

       // 删除日志数据
       log.info("删除 开始删除委外订单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       operateLogService.removeByBusinessIds(ids);

       // 删除明细数据（如果有明细数据的话）
       subcontractOrderDetailService.removeByMainIds(ids);
       // 删除主单数据
       log.info("删除 开始删除委外订单主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
       super.removeByIds(ids);

        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelProcess(List<String> ids) {
        List<SubcontractOrderEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外订单数据");
        }
        // 只有待提交的数据允许撤销
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        log.info("撤销 开始修改委外订单状态，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("委外订单【%s】取消流程", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "取消流程操作");
    }

    @Override
    public SubcontractOrderDTO.ViewDTO view(String id) {
        SubcontractOrderEntity subcontractOrderEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外订单数据"));
        SubcontractOrderDTO.ViewDTO data = BeanMapperUtils.map(SubcontractOrderDTO.ViewDTO.class, subcontractOrderEntity);
        //委外订单明细数据
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        //产品信息
        List<String> skuIds = detailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        log.info("查询产品信息，skuId集合：【{}】", JSONUtil.toJsonStr(skuIds));
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //供应商信息
        List<String> supplierIds = detailList.stream().map(SubcontractOrderDetailEntity::getSupplierId).collect(Collectors.toList());
        log.info("查询供应商信息，supplierId集合：【{}】", JSONUtil.toJsonStr(supplierIds));
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);

        //明细父级sku
        List<SubcontractOrderDetailEntity> parentList = detailList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98071);
        }
        List<String> parentSkuIds = parentList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        //BOM信息
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIds);
        if (CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }

        //供应商付款条件
        List<KingdeePaymentConditionEntity> paymentConditionList =  kingdeePaymentConditionService.list();

        //仓位信息
        List<String> warehouseLocationCodeList = detailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getWarehouseLocation())).map(SubcontractOrderDetailEntity::getWarehouseLocation).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = FeignQuery.create(WarehouseLocationEntity.class).in(WarehouseLocationEntity::getCode,warehouseLocationCodeList).list();

        List<SubcontractOrderDetailDTO.ViewDTO> parentDTOList = BeanMapperUtils.copyList(SubcontractOrderDetailDTO.ViewDTO.class, parentList);
        for (SubcontractOrderDetailDTO.ViewDTO viewDTO : parentDTOList) {
            //产品名称
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuName())).orElse("");
            viewDTO.setProductName(productName);

            if (CollectionUtils.isNotEmpty(supplierList)) {
                //供应商名称
                String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(viewDTO.getSupplierId())).findFirst().flatMap(e -> Optional.ofNullable(e.getName())).orElse("");
                viewDTO.setSupplierName(supplierName);
            }

            //根据组织、仓库、sku查询可用库存
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(Arrays.asList(viewDTO.getSkuId()),viewDTO.getWarehouseId(),null);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(viewDTO.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            viewDTO.setCurInventoryQty(curInventoryQty);

            //付款条件
            String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getCode().equals(viewDTO.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            viewDTO.setPaymentConditionName(paymentConditionName);

            //子集SKU
            List<SubcontractOrderDetailEntity> childList = detailList.stream().filter(obj -> obj.getParentId().equals(viewDTO.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childList)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            List<SubcontractOrderDetailDTO.ChildDTO> childDTOList = BeanMapperUtils.copyList(SubcontractOrderDetailDTO.ChildDTO.class, childList);
            for (SubcontractOrderDetailDTO.ChildDTO childViewDTO : childDTOList) {
                //bom信息
                BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> childViewDTO.getBomVersion().equals(obj.getBomVersion()) && obj.getParentSkuId().equals(viewDTO.getSkuId()) && obj.getSkuId().equals(childViewDTO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                    log.error("未找到对应bom子件信息，viewDTO.skuId = 【{}】，childViewDTO = 【{}】，bomChildrenList = 【{}】",viewDTO.getSkuId(),childViewDTO,bomChildrenList);
                    throw new ServiceException(ApiError.ERROR_95163);
                }
                childViewDTO.setQuantity(bomChildrenSkuDTO.getQuantity());

                //产品名称
                String childProductName = skuList.stream().filter(obj -> obj.getSkuId().equals(childViewDTO.getSkuId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuName())).orElse("");
                childViewDTO.setProductName(childProductName);
                if (CollectionUtils.isNotEmpty(supplierList)) {
                    //供应商名称
                    String childSupplierName = supplierList.stream().filter(obj -> obj.getId().equals(childViewDTO.getSupplierId())).findFirst().flatMap(e -> Optional.ofNullable(e.getName())).orElse("");
                    childViewDTO.setSupplierName(childSupplierName);
                }

                //根据组织、仓库、sku查询可用库存
                List<InventoryQtyDTO.SkuInventoryTotalDTO> childSkuInventoryTotalList = listSkuInventoryTotalList(Arrays.asList(childViewDTO.getSkuId()),childViewDTO.getWarehouseId(),childViewDTO.getWarehouseLocation());
                //即时库存
                Integer childCurInventoryQty = childSkuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(childViewDTO.getSkuId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                childViewDTO.setCurInventoryQty(childCurInventoryQty);

                //付款条件
                String childPaymentConditionName = paymentConditionList.stream().filter(obj -> obj.getCode().equals(childViewDTO.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                childViewDTO.setPaymentConditionName(childPaymentConditionName);

                //仓位名称
                String warehouseLocationName = warehouseLocationList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(),childViewDTO.getWarehouseId()) && StrUtil.equals(obj.getCode(), childViewDTO.getWarehouseLocation())).map(WarehouseLocationEntity::getName).findFirst().orElse("");
                childViewDTO.setWarehouseLocationName(warehouseLocationName);
            }

            viewDTO.setChildList(childDTOList);
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setDetailList(parentDTOList);
        return data;
    }

    @Override
    public List<SubcontractOrderDTO.ViewGeneratePoDTO> viewGeneratePo(List<String> ids) {
       if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
       }
        List<SubcontractOrderDTO.ViewGeneratePoDTO> list = baseMapper.viewGeneratePo(ids);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //产品信息
        List<String> skuIds = list.stream().map(SubcontractOrderDTO.ViewGeneratePoDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuPurchaseByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        List<String> sourceDetailIds = list.stream().map(SubcontractOrderDTO.ViewGeneratePoDTO::getSourceDetailId).collect(Collectors.toList());
        //下推采购订单信息
        List<PurchaseOrderDTO.ListDTO> purchaseOrderList = purchaseOrderService.listBySourceDetailIds(sourceDetailIds);

        List<String> parentSkuIds = list.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).map(SubcontractOrderDTO.ViewGeneratePoDTO::getSkuId).collect(Collectors.toList());
        //BOM信息
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIds);
        if (CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }

        //价目查询
        List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> priceList = new ValidList<>();
        list.forEach(e -> {
            priceList.add(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO.builder()
                    .purchaseOrgId(e.getPurchaseOrgId())
                    .purchaseQty(e.getQty())
                    .skuId(e.getSkuId())
                    .skuNo(e.getSkuNo())
                    .supplierId(e.getSupplierId())
                    .build());
        });
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> viewDTOList = purchasePriceDetailService.batchGetTaxPrice(priceList);

        List<SubcontractOrderDTO.ViewGeneratePoDTO> resultList = new ArrayList<>();

        for (SubcontractOrderDTO.ViewGeneratePoDTO dto : list) {

            //bom信息
            if (StringUtils.isNotBlank(dto.getParentId())) {
                String parentSkuId = list.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getParentId())).map(SubcontractOrderDTO.ViewGeneratePoDTO::getSkuId).findFirst().orElse(null);
                BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> obj.getBomVersion().equals(dto.getBomVersion()) && obj.getParentSkuId().equals(parentSkuId) && obj.getSkuId().equals(dto.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                    throw new ServiceException(ApiError.ERROR_95163);
                }
                dto.setQuantity(bomChildrenSkuDTO.getQuantity());
            }
            //报价信息查询
            if (ObjectUtils.isNotEmpty(dto.getIsGift()) && !dto.getIsGift() && StringUtils.isNotBlank(dto.getSupplierId())) {
                //采购单价赋值
                PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = viewDTOList.stream().filter(obj ->
                                obj.getSkuId().equals(dto.getSkuId())
                                && obj.getSupplierId().equals(dto.getSupplierId())
                                && StrUtil.equals(obj.getPurchaseOrgId(),dto.getPurchaseOrgId()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(viewDTO)){
                    dto.setPrice(viewDTO.getTaxPrice());
                    dto.setTaxRate(viewDTO.getTaxRate());
                    dto.setCurrency(viewDTO.getCurrency());
                    dto.setCurrencySymbol(viewDTO.getCurrencySymbol());
                    dto.setAmount(MathUtil.multiply(viewDTO.getTaxPrice(),dto.getQty()).setScale(4, RoundingMode.DOWN));
                }
            }
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            dto.setIsConstitute(Boolean.FALSE);
            if (ObjectUtils.isEmpty(dto.getParentId())) {
                dto.setIsConstitute(Boolean.TRUE);
            }
            dto.setSourceType(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
            dto.setProductName(skuVO.getSkuName());
            dto.setMoq(skuVO.getMoq());
            //待申请数量
            Integer purchaseQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(purchaseOrderList)) {
                 purchaseQty = purchaseOrderList.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getSourceDetailId())).map(PurchaseOrderDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            dto.setApplyQty(dto.getQty() - purchaseQty);
            dto.setQty(dto.getApplyQty());
            //待下推数量为0则无需显示
            if (MathUtil.compareTo(dto.getApplyQty(),MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            resultList.add(dto);
        }
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.ERROR_98092);
        }
        return resultList;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void generatePo(ValidList<SubcontractOrderDTO.GeneratePoDTO> list,Boolean isAuto) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<SubcontractOrderDTO.GeneratePoDTO> addList = list.getList();
        List<SubcontractOrderDTO.GeneratePoAddDTO> resultList = BeanMapperUtils.copyList(SubcontractOrderDTO.GeneratePoAddDTO.class, addList);

        //处理生成数据
        fillGeneratePoDTO(resultList);
        //查询产品信息
        List<String> skuIds = resultList.stream().map(obj -> obj.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //供应商默认联系人
        List<String> supplierIds = list.stream().map(SubcontractOrderDTO.GeneratePoDTO::getSupplierId).collect(Collectors.toList());
        List<SupplierContactEntity> defaultSupplierContactList = supplierContactService.getDefaultBySupplierIdList(supplierIds);

        //供应商
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);

        List<String> poIds = new ArrayList<>();
        Map<String, List<SubcontractOrderDTO.GeneratePoAddDTO>> map = resultList.stream().collect(Collectors.groupingBy(obj -> obj.getSourceId().concat(obj.getSupplierId()).concat(obj.getDeliveryWarehouseId()).concat(obj.getIsParent().toString())));
        for (Map.Entry<String, List<SubcontractOrderDTO.GeneratePoAddDTO>> entry : map.entrySet()) {
            List<SubcontractOrderDTO.GeneratePoAddDTO> value = entry.getValue();
            SubcontractOrderDTO.GeneratePoAddDTO generatePoAddDTO = value.get(0);
            //采购主表
            PurchaseOrderDTO.AddDTO addDTO = new PurchaseOrderDTO.AddDTO();
            BeanMapperUtils.copy(generatePoAddDTO,addDTO);
            addDTO.setPurchaseDate(LocalDate.now());
            //委外类型
            if (generatePoAddDTO.getIsParent()) {
                addDTO.setType(PurchaseOrderTypeEnum.ENUM_SUBCONTRACT.getCode());
                addDTO.setSubcontractType(SubcontractTypeEnum.ENUM_PARENT.getCode());
            } else {
                addDTO.setType(PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode());
                addDTO.setSubcontractType(SubcontractTypeEnum.ENUM_CHILD.getCode());
            }
            //采购供应商
            PurchaseOrderSupplierDTO.AddDTO supplierDTO = new PurchaseOrderSupplierDTO.AddDTO();
            supplierDTO.setSupplierId(generatePoAddDTO.getSupplierId());
            supplierDTO.setPaymentCondition(generatePoAddDTO.getPaymentCondition());

            //付款条件
            SupplierEntity supplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(generatePoAddDTO.getSupplierId()) ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                supplierDTO.setPayMethodId(supplierEntity.getPayMethodId());
            }

            //供应商默认联系人
            if (CollectionUtils.isNotEmpty(defaultSupplierContactList)) {
                SupplierContactEntity supplierContactEntity = defaultSupplierContactList.stream().filter(obj -> obj.getSupplierId().equals(value.get(0).getSupplierId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(supplierContactEntity)) {
                    supplierDTO.setSupplierContactId(supplierContactEntity.getId());
                    supplierDTO.setContactTelNumber(supplierContactEntity.getTelNumber());
                }
            }
            addDTO.setPurchaseOrderSupplierDTO(supplierDTO);

            //采购明细
            List<PurchaseOrderDetailDTO.AddDTO> poDetailList = new ArrayList<>();
            for (SubcontractOrderDTO.GeneratePoAddDTO addDetailDTO : value) {
                PurchaseOrderDetailDTO.AddDTO poDetailAddDTO = new PurchaseOrderDetailDTO.AddDTO();
                //产品信息
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(addDetailDTO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                BeanMapperUtils.copy(skuVO,poDetailAddDTO);
                poDetailAddDTO.setSourceDetailId(addDetailDTO.getSourceDetailId());
                poDetailAddDTO.setProductName(skuVO.getSkuName());
                poDetailAddDTO.setPurchaseQty(addDetailDTO.getQty());
                poDetailAddDTO.setWarehouseLocation(addDetailDTO.getWarehouseLocation());
                poDetailAddDTO.setPlanDeliveryDate(addDetailDTO.getPlanDeliveryDate());
                poDetailAddDTO.setRemark(addDetailDTO.getRemark());
                poDetailAddDTO.setIsGift(addDetailDTO.getIsGift());
                poDetailAddDTO.setPurchaseApplicationId(addDetailDTO.getPurchaseApplicationId());
                poDetailAddDTO.setPurchaseApplicationDetailId(addDetailDTO.getPurchaseApplicationDetailId());
                poDetailAddDTO.setPlanDeliveryDate(addDetailDTO.getPlanDeliveryDate());
                if (!addDetailDTO.getIsGift()) {
                    //供应商报价信息
                    PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(addDetailDTO.getQty(),addDetailDTO.getSkuId(),addDetailDTO.getSkuNo(),addDetailDTO.getSupplierId(),addDetailDTO.getPurchaseOrgId());
                    List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(searchDTO);
                    PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
                    poDetailAddDTO.setCurrency(viewDTO.getCurrency());
                    poDetailAddDTO.setCurrencySymbol(viewDTO.getCurrencySymbol());
                    poDetailAddDTO.setTaxPrice(ObjectUtils.isEmpty(addDetailDTO.getTaxPrice())? viewDTO.getTaxPrice() : addDetailDTO.getTaxPrice());
                    poDetailAddDTO.setDeliveryDay(viewDTO.getDeliveryDay());
                }
                poDetailAddDTO.setPurchaseAmount(MathUtil.multiply(poDetailAddDTO.getTaxPrice(),poDetailAddDTO.getPurchaseQty()));
                poDetailList.add(poDetailAddDTO);
            }
            addDTO.setDetails(poDetailList);
            PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.add(addDTO);
            if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_1019);
            }
            poIds.add(purchaseOrderEntity.getId());
        }

        //采购订单提交审核
        if (CollectionUtils.isNotEmpty(poIds)) {
            //更新采购申请单采购订单创建类型
            purchaseOrderService.updateCreatePoType(poIds);
            //提交
            Boolean submit = purchaseOrderService.submit(poIds,Boolean.FALSE);
            if (!submit) {
                throw new ServiceException(ApiError.ERROR_98076);
            }
            //审核
            for (String poId : poIds) {
                ApproveOneDTO approveOneDTO = new ApproveOneDTO();
                approveOneDTO.setId(poId);
                approveOneDTO.setType(ApproveType.PASS);
                BatchResultDTO approve = purchaseOrderService.approve(approveOneDTO);
                if (!approve.getSuccess()) {
                    throw new ServiceException(ApiError.ERROR_98077);
                }
            }
        }
    }

    /**
     * @description: 处理生成采购订单数据
     * @author Will
     * @date: 2023/6/21 12:15
     * @param resultList
     */
    private void fillGeneratePoDTO (List<SubcontractOrderDTO.GeneratePoAddDTO> resultList) {
        //委外订单主表信息
        List<String> sourceIds = resultList.stream().map(SubcontractOrderDTO.GeneratePoDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractOrderEntity> mainList = this.listByIds(sourceIds);
        if (CollectionUtils.isEmpty(mainList)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }

        //委外订单明细信息
        List<String> sourceDetailIds = resultList.stream().map(SubcontractOrderDTO.GeneratePoDTO::getSourceDetailId).collect(Collectors.toList());
        log.info("查询委外订单明细，detailIds = 【{}】",sourceDetailIds);
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        //仓库信息
        List<String> warehouseIdList = detailList.stream().map(SubcontractOrderDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> updateList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(updateList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        for (SubcontractOrderDTO.GeneratePoAddDTO generatePoDTO :  resultList) {
            //主表
            SubcontractOrderEntity mainEntity = mainList.stream().filter(obj -> obj.getId().equals(generatePoDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(mainEntity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            //明细
            SubcontractOrderDetailEntity detailEntity = detailList.stream().filter(obj -> obj.getId().equals(generatePoDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98070);
            }
            //skuId
            generatePoDTO.setSkuId(detailEntity.getSkuId());
            //仓库
            generatePoDTO.setDeliveryWarehouseId(detailEntity.getWarehouseId());
            //库位
            generatePoDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            //是否是父级sku
            generatePoDTO.setIsParent(StringUtils.isBlank(detailEntity.getParentId()) ? Boolean.TRUE :Boolean.FALSE );
            //采购员
            generatePoDTO.setPurchaseUserId(mainEntity.getPurchaserId());
            //采购部门
            generatePoDTO.setPurchaseDeptId(mainEntity.getDeptId());
            //采购组织
            generatePoDTO.setPurchaseOrgId(mainEntity.getPurchaseOrgId());

            String orgId = updateList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getOrgId())).orElse("");
            if (StringUtils.isBlank(orgId)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            //收料组织
            generatePoDTO.setReceiveOrgId(orgId);
            //新品首批暂时默认
            generatePoDTO.setIsFirstMassProduct(ObjectUtils.isEmpty(generatePoDTO.getIsFirstMassProduct()) ? mainEntity.getIsFirstMassProduct() : generatePoDTO.getIsFirstMassProduct());
            //备注
            generatePoDTO.setRemark(detailEntity.getRemark());
            //付款条件
            generatePoDTO.setPaymentCondition(detailEntity.getPaymentCondition());
            //是否赠品
            generatePoDTO.setIsGift( ObjectUtils.isEmpty(generatePoDTO.getIsGift()) ? detailEntity.getIsGift() : generatePoDTO.getIsGift());
            generatePoDTO.setSupplierId(StringUtils.isBlank(generatePoDTO.getSupplierId()) ? detailEntity.getSupplierId() : generatePoDTO.getSupplierId());
            generatePoDTO.setPurchaseApplicationId(mainEntity.getSourceId());
            generatePoDTO.setPurchaseApplicationDetailId(detailEntity.getSourceDetailId());
        }
    }

    @Override
    public List<SubcontractOrderDTO.ViewAddDetailDTO> viewAddDetail(SubcontractOrderDTO.ViewAddDetailParamDTO dto) {
        List<SubcontractOrderDTO.ViewAddDetailDTO> resultList = new ArrayList<>();
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByMainIdAndSku(dto.getId(), dto.getSkuNoList());
        List<SubcontractOrderDetailEntity> parentList = detailList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98071);
        }

        //查询产品信息
        List<String> skuIds = detailList.stream().map(obj -> obj.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        List<String> sourceDetailIds = detailList.stream().map(obj -> obj.getId()).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> podList = purchaseOrderDetailService.listBySourceDetailIds(sourceDetailIds);
        List<String> podIds = podList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        //入库信息
        List<PoInstockDetailEntity> poInstockDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        for (SubcontractOrderDetailEntity parent : parentList) {
            SubcontractOrderDTO.ViewAddDetailDTO parentDTO = new SubcontractOrderDTO.ViewAddDetailDTO();
            BeanMapperUtils.copy(parent,parentDTO);
            parentDTO.setSourceDetailId(parent.getId());
            String parentProductName = skuList.stream().filter(obj -> obj.getSkuId().equals(parent.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            parentDTO.setProductName(parentProductName);


            List<String> parentPodIds = podList.stream().filter(obj -> obj.getSourceDetailId().equals(parent.getId())).map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(parentPodIds)) {
                //收货数量
                Integer receiveQty = receiveDetailList.stream().filter(obj -> parentPodIds.contains(obj.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                parentDTO.setReceiveQty(receiveQty);
                //入库数量
                Integer instockQty = poInstockDetailList.stream().filter(obj -> parentPodIds.contains(obj.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                parentDTO.setInstockQty(instockQty);
            }

            //子集
            List<SubcontractOrderDetailEntity> childList = detailList.stream().filter(obj -> StringUtils.equals(obj.getParentId(), parent.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childList)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            List<SubcontractOrderDTO.ViewAddDetailDTO> childDTOList = new ArrayList<>();
            for (SubcontractOrderDetailEntity child : childList) {
                SubcontractOrderDTO.ViewAddDetailDTO childDTO = new SubcontractOrderDTO.ViewAddDetailDTO();
                BeanMapperUtils.copy(child,childDTO);
                childDTO.setSourceDetailId(child.getId());
                String childProductName = skuList.stream().filter(obj -> obj.getSkuId().equals(child.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
                childDTO.setProductName(childProductName);

                //采购订单明细id
                List<String> childPodIds = podList.stream().filter(obj -> obj.getSourceDetailId().equals(child.getId())).map(PurchaseOrderDetailEntity::getSourceDetailId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(childPodIds)) {
                    //收货数量
                    Integer receiveQty = receiveDetailList.stream().filter(obj -> childPodIds.contains(obj.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    childDTO.setReceiveQty(receiveQty);
                    //入库数量
                    Integer instockQty = poInstockDetailList.stream().filter(obj -> childPodIds.contains(obj.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                    childDTO.setInstockQty(instockQty);
                }

                childDTOList.add(childDTO);
            }
            parentDTO.setChildList(childDTOList);
            resultList.add(parentDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void invalid(List<String> ids, String remark) {
        List<SubcontractOrderEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外订单数据");
        }
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }

        //验证存货核算是否关账
        /*List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = list.stream().flatMap(obj -> Stream.of(new InventoryClosedRecordDTO.ClosedParamDTO(obj.getSubcontractOrgId(),obj.getBillDate())
                        ,new InventoryClosedRecordDTO.ClosedParamDTO(obj.getPurchaseOrgId(),obj.getBillDate()))).
                distinct().collect(Collectors.toList());
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);*/


        log.info("采购订单作废，ids=【{}】", JSONUtil.toJsonStr(ids));
        //更新订单作废状态
        updateInvalidStatus(ids, remark);

        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个委外订单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "作废操作");
    }

    @Override
    public SubcontractChangeDTO.ViewDTO viewSubcontractChange(String id) {
        SubcontractChangeDTO.ViewDTO viewDTO = new SubcontractChangeDTO.ViewDTO();
        SubcontractOrderEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        BeanMapperUtils.copy(entity,viewDTO);
        viewDTO.setId(null);
        viewDTO.setCode(null);
        viewDTO.setApproveStatus(null);
        viewDTO.setSourceId(entity.getId());
        viewDTO.setSourceCode(entity.getCode());
        viewDTO.setSourceType(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }
        //产品信息
        List<String> skuIds = detailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);


        List<SubcontractOrderDetailEntity> parentDetailList = detailList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());

        //BOM信息
        List<String> parentSkuIds = parentDetailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIds);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }

        List<SubcontractChangeDetailDTO.ViewDTO> parentList = new ArrayList<>();
        for (SubcontractOrderDetailEntity parentEntity : parentDetailList) {
            SubcontractChangeDetailDTO.ViewDTO  parentDTO = BeanMapperUtils.map(SubcontractChangeDetailDTO.ViewDTO.class, parentEntity);
            parentDTO.setId(null);
            parentDTO.setSourceDetailId(parentEntity.getId());
            parentDTO.setOldQty(parentEntity.getQty());
            parentDTO.setOldPrice(parentEntity.getPrice());
            parentDTO.setOldAmount(parentEntity.getAmount());
            parentDTO.setOldDeliveryQty(parentEntity.getDeliveryQty());
            parentDTO.setPrice(null);
            parentDTO.setDeliveryQty(null);
            parentDTO.setAmount(null);
            parentDTO.setRemark(null);
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(parentEntity.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse(null);
                parentDTO.setProductName(productName);
            }
            //子级
            List<SubcontractOrderDetailEntity> childDetailList = detailList.stream().filter(obj -> obj.getParentId().equals(parentEntity.getId())).collect(Collectors.toList());
            List<SubcontractChangeDetailDTO.ChildDTO> childList = new ArrayList<>();
            for (SubcontractOrderDetailEntity childEntity : childDetailList) {
                SubcontractChangeDetailDTO.ChildDTO  childDTO = BeanMapperUtils.map(SubcontractChangeDetailDTO.ChildDTO.class, childEntity);
                childDTO.setId(null);
                childDTO.setSourceDetailId(childEntity.getId());
                childDTO.setOldQty(childEntity.getQty());
                childDTO.setOldPrice(childEntity.getPrice());
                childDTO.setOldAmount(childEntity.getAmount());
                childDTO.setOldDeliveryQty(childEntity.getDeliveryQty());
                childDTO.setPrice(null);
                childDTO.setDeliveryQty(null);
                childDTO.setAmount(null);
                childDTO.setRemark(null);
                //产品名称
                if (CollectionUtils.isNotEmpty(skuList)) {
                    String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(childEntity.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse(null);
                    childDTO.setProductName(productName);
                }

                //bom信息
                BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(parentEntity.getSkuId()) && obj.getSkuId().equals(childEntity.getSkuId()) && obj.getBomVersion().equals(parentEntity.getBomVersion())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                    throw new ServiceException(ApiError.ERROR_95163);
                }
                childDTO.setQuantity(bomChildrenSkuDTO.getQuantity());

                childList.add(childDTO);
            }
            parentDTO.setChildList(childList);
            parentList.add(parentDTO);
        }
        viewDTO.setDetailList(parentList);
        return viewDTO;
    }

    @Override
    public List<SubcontractOrderEntity> listBySourceId(List<String> sourceIds) {
        return lambdaQuery().in(SubcontractOrderEntity::getSourceId,sourceIds).eq(SubcontractOrderEntity::getInvalidStatus, Boolean.FALSE).list();
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SubcontractOrderEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SubcontractOrderEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    /**
     * 根据bom skuId 获取数据
     * @author yl
     * @date 2023-10-12 9:53
     * @param bomSkuId
     * @return java.util.List<com.erp.model.scm.dto.SubcontractOrderDTO.ListDTO>
     */
    @Override
    public List<SubcontractOrderDTO.ListDTO> listByBomSku(String bomSkuId) {
        return baseMapper.listByBomSku(bomSkuId);
    }

    @Override
    public List<SubcontractOrderDTO.ListSelectDTO> listSubcontractOrder() {
        List<SubcontractOrderEntity> list = lambdaQuery().eq(SubcontractOrderEntity::getInvalidStatus, Boolean.FALSE)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<SubcontractOrderDTO.ListSelectDTO> resultList = BeanMapperUtils.copyList(SubcontractOrderDTO.ListSelectDTO.class, list);
        return resultList;
    }
    /**
     * @description: 启动流程
     * @author Will
     * @date: 2023/7/11 12:19
     * @param list
     */
    private void startProcess(List<SubcontractOrderEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }
    /**
     * @description: 审核流程处理
     * @author Will
     * @date: 2023/7/11 12:28
     * @param entity
     * @param dto
     */
    private void approveProcess(SubcontractOrderEntity entity , ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
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

    /**
     * @description: 自动生成采购订单
     * @author Will
     * @date: 2023/6/19 9:59
     * @param id
     */
    private void autoGeneratePo(String id) {

        List<SubcontractOrderDTO.ViewGeneratePoDTO> viewGeneratePoDTOS = viewGeneratePo(Arrays.asList(id));
        if (CollectionUtils.isEmpty(viewGeneratePoDTOS)) {
            return;
        }
        //生成采购订单
        List<SubcontractOrderDTO.ViewGeneratePoDTO> addList = viewGeneratePoDTOS.stream().filter(obj -> obj.getIsGeneratePo()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        List<SubcontractOrderDTO.GeneratePoDTO> resultList = BeanMapperUtils.copyList(SubcontractOrderDTO.GeneratePoDTO.class, addList);
        ValidList<SubcontractOrderDTO.GeneratePoDTO> list = new ValidList<>();
        list.setList(resultList);
        generatePo(list,Boolean.TRUE);
    }

    /**
     * @param ids
     * @param reason
     * @description: 更新作废状态
     * @author Will
     */
    private void updateInvalidStatus(List<String> ids, String reason) {
        //更新
        lambdaUpdate().in(SubcontractOrderEntity::getId, ids)
                .set(SubcontractOrderEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SubcontractOrderEntity::getInvalidTime, LocalDateTime.now())
                .set(SubcontractOrderEntity::getInvalidRemark, reason)
                .update();
    }

    /**
    * 审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    public Boolean updateForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return this.lambdaUpdate().in(SubcontractOrderEntity::getId, ids)
                .set(SubcontractOrderEntity::getApproveUserId, userInfo.getUid())
                .set(SubcontractOrderEntity::getApproveUserName, userInfo.getUserName())
                .set(SubcontractOrderEntity::getApproveStatus, approveStatus)
                .set(SubcontractOrderEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
    * 更新审核状态
    */
    private Boolean updateApproveStatus(List<String> ids, String approveStatus) {
        return lambdaUpdate().in(SubcontractOrderEntity::getId, ids)
                .set(SubcontractOrderEntity::getApproveStatus, approveStatus)
                .set(SubcontractOrderEntity::getApproveUserId, "")
                .set(SubcontractOrderEntity::getApproveUserName, "")
                .set(SubcontractOrderEntity::getApproveTime, null)
                .update();
    }

    /**
     * @description: 列表Tab查询状态处理
     * @author Will
     * @date: 2023/7/11 15:10
     * @param params
     * @return Boolean
     */
    private Boolean doOpHandleTableParam (SubcontractOrderDTO.PagingParamDTO params) {
        List<String> approveStatusList = new ArrayList<>(1);
        List<String> arrivalStatusList = new ArrayList<>(2);
        //待我审核
        if (PurchaseTableFlagEnum.TO_BE_APPROVE.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
            if (CollectionUtils.isEmpty(businessIds)) {
                return Boolean.FALSE;
            }
            params.setIdList(businessIds);
        }
        // 待提交
        if (PurchaseTableFlagEnum.WAIT_SUBMIT.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        //待到货
        if (PurchaseTableFlagEnum.TO_BE_CREATE.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            arrivalStatusList.add(ArrivalStatusEnum.NON_ARRIVAL.getCode());
            arrivalStatusList.add(ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode());
        }
        //已到货
        if (PurchaseTableFlagEnum.CREATED.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            arrivalStatusList.add(ArrivalStatusEnum.ARRIVED.getCode());
        }
        //不通过
        if (PurchaseTableFlagEnum.REJECT.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.REJECT.getStatus());
        }
        if (CollectionUtils.isNotEmpty(approveStatusList)) {
            params.setApproveStatusList(approveStatusList);
        }
        if (CollectionUtils.isNotEmpty(arrivalStatusList)) {
            params.setArrivalStatusList(arrivalStatusList);
        }
        return Boolean.TRUE;
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractOrderDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //产品信息
        List<String> skuIds = list.stream().map(SubcontractOrderDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        //收货数量
        List<String> ids = list.stream().map(SubcontractOrderDTO.ListDTO::getDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> podList = purchaseOrderDetailService.listBySourceDetailIds(ids);
        List<WarehouseReceiveDetailEntity> receiveDetailList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(podList)) {
            List<String> podIds = podList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
            receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SUBCONTRACT_ORDER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.Default.code,listApiResult.getMsg()));
            }
        }
        // 属性赋值
        for(SubcontractOrderDTO.ListDTO data : list) {

            if (CollectionUtils.isNotEmpty(podList)) {
                List<String> podIds = podList.stream().filter(obj -> obj.getSourceDetailId().equals(data.getDetailId())).map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                    Integer receiveQty = receiveDetailList.stream().filter(obj -> podIds.contains(obj.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    data.setReceiveQty(receiveQty);
                }
            }
            //sku信息
            if (CollectionUtils.isNotEmpty(skuList)) {
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(data.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(skuVO)) {
                    data.setProductName(skuVO.getSkuName());
                }
            }
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setArrivalStatusName(ArrivalStatusEnum.getNameByCode(data.getArrivalStatus()));

            //最新审核人
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }

    }

    /**
     * @description: 查询库存
     * @author Will
     * @date: 2023/7/11 14:36
     * @param skuIdList
     * @param warehouseId
     * @param warehouseLocation
     * @return List<SkuInventoryTotalDTO>
     */
    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryTotalList(List<String> skuIdList, String warehouseId,String warehouseLocation) {
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(warehouseId);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        paramDTO.setWarehouseLocationId(warehouseLocation);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        return skuInventoryTotalList;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractOrderEntity entity) {
        //核算公司信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(), entity.getSubcontractOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //人员信息
        if (StringUtils.isNotBlank(entity.getPurchaserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaserId());
            entity.setPurchaserName(findUserDTO.getUserName());

        }

        //部门信息
        if (StringUtils.isNotBlank(entity.getDeptId())) {
            SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(entity.getDeptId());
            entity.setDeptName(sysDepartmentDTO.getName());
        }


        //采购组织名称
        String purchaseOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setPurchaseOrgName(purchaseOrgName);

        //委外组织名称
        String subcontractOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSubcontractOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setSubcontractOrgName(subcontractOrgName);
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<SubcontractOrderEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSubcontractOrderService.syncDataToKingdee(obj, operate);
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

}
