package com.erp.server.scm.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.dto.excel.PurchaseChangeExportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InstockForcastPoChangeDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryCloseRecordFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseChangeService;
import com.erp.server.scm.mapper.PurchaseChangeMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_CHANGE;

/**
 * <p>
 * 销售需求明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@Service
public class PurchaseChangeServiceImpl extends SuperServiceImpl<PurchaseChangeMapper, PurchaseChangeEntity> implements PurchaseChangeService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseChangeDetailService purchaseChangeDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Autowired
    private InventoryFeign inventoryFeign;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    @Autowired
    private SyncKingdeePurchaseChangeService syncKingdeePurchaseChangeService;


    @Autowired
    private InventoryCloseRecordFeign inventoryCloseRecordFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<PurchaseChangeDTO.ListDTO> paging(PagingDTO<PurchaseChangeDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseChangeDTO.ListDTO> records = pageData.getRecords();
        //格式化变更数据
        formatPurchaseChange(records);
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseChangeDTO.ListDTO> list(BaseIdDTO dto) {
        List<PurchaseChangeDTO.ListDTO> list = baseMapper.list(dto);
        //格式化变更数据
        formatPurchaseChange(list);
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(PurchaseChangeDTO.AddDTO dto) {
        PurchaseChangeEntity entity = new PurchaseChangeEntity();
        BeanMapperUtils.copy(dto,entity);
        log.info("采购变更单新增");
        //数据验证及赋值
        checkPurchaseChange(dto.getPurchaseOrderId(),entity);
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.POC, BusinessNoTypeEnum.CODE_POC.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_POC);
        entity.setCode(code);
        //处理数据id
        doOpHandleDataId(dto.getChangeUserId(),dto.getChangeDeptId(),entity);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //采购变更操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购变更单【%s】",code), ModuleTypeEnum.PURCHASE_CHANGE.getCode(),entity.getId(),"新增操作");
           //采购订单操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("生成了一个采购变更单【%s】",code), ModuleTypeEnum.PURCHASE_ORDER.getCode(),entity.getPurchaseOrderId(),"采购变更");
            //新增明细
            purchaseChangeDetailService.add(dto.getDetails(),entity.getId());
        }

        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseChangeDTO.UpdateDTO dto) {
        PurchaseChangeEntity entity = new PurchaseChangeEntity();
        BeanMapperUtils.copy(dto,entity);
        //处理数据id
        doOpHandleDataId(dto.getChangeUserId(),dto.getChangeDeptId(),entity);
        log.info("采购变更单修改，id=【{}】", dto.getId());

        //操作日志
        PurchaseChangeEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old,entity,ModuleTypeEnum.PURCHASE_CHANGE.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseChangeDetailService.update(dto.getDetails(),entity.getId());
        return Boolean.TRUE;
    }

    @Override
    public PurchaseChangeDTO.ViewDTO view(String id) {
        PurchaseChangeDTO.ViewDTO dto = new PurchaseChangeDTO.ViewDTO();

        //主表信息
        PurchaseChangeEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98042);
        }
        BeanMapperUtils.copy(entity,dto);

        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
           throw new ServiceException(ApiError.ERROR_98025);
        }
        //采购订单号
        dto.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        //供应商信息
        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        PurchaseOrderSupplierDTO.UpdateDTO supplierDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        BeanMapperUtils.copy(supplierEntity,supplierDTO);
        dto.setSupplierDTO(supplierDTO);

        //明细信息
        List<PurchaseChangeDetailEntity> entityDetails = purchaseChangeDetailService.listByPurchaseChangeIds(Arrays.asList(id));
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98043);
        }
        List<PurchaseChangeDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(PurchaseChangeDetailDTO.UpdateDTO.class, entityDetails);
        dto.setDetails(details);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids,String reason) {
        //根据ids查询
        List<PurchaseChangeEntity> list = getList(ids);
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
        List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = list.stream().flatMap(obj -> Stream.of(new InventoryClosedRecordDTO.ClosedParamDTO(obj.getReceiveOrgId(),obj.getChangeDate())
                        ,new InventoryClosedRecordDTO.ClosedParamDTO(obj.getPurchaseOrgId(),obj.getChangeDate()))).
                distinct().collect(Collectors.toList());
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);

        log.info("采购变更作废，ids=【{}】", JSONUtil.toJsonStr(ids));
        //更新作废状态
        updateInvalidStatus(ids,reason);

        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个采购变更【%s】，作废原因：".concat(reason), ModuleTypeEnum.PURCHASE_CHANGE.getCode(),pairList,"作废操作");
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(PurchaseChangeEntity entity, String type, String comment, Boolean isNeedProcess,
                                  List<PurchaseChangeDetailEntity> purchaseChangeDetailEntityList,
                                  List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList,
                                  List<PoReturnDetailEntity> returnDetailEntityList,
                                  List<WarehouseReceiveDetailEntity> receiveDetailEntityList) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //验证存货核算是否关账
        List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = new ArrayList<>(2);
        closedParamList.add(new InventoryClosedRecordDTO.ClosedParamDTO(entity.getReceiveOrgId(),entity.getChangeDate()));
        closedParamList.add(new InventoryClosedRecordDTO.ClosedParamDTO(entity.getPurchaseOrgId(),entity.getChangeDate()));
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);
        log.info("采购变更单【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //更新单据状态(后面有流程了可删)
            updateApproveStatusForApprove(entity.getId(),ApproveStatusEnum.APPROVE.getStatus());
            //验证并更新采购订单原有数据
            updatePurchaseOrderData(entity,purchaseChangeDetailEntityList);
            //更新采购申请单生成PO类型
            purchaseOrderService.updateCreatePoType(Collections.singletonList(entity.getPurchaseOrderId()));
            //修改到货状态
            purchaseChangeDetailEntityList.forEach(req -> {
                Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(req.getPurchaseOrderDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

                Integer receiveQty = receiveDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(req.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);

                Integer purchaseQty = req.getQty();
                if (returnQty > receiveQty) {
                    throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, req.getSkuNo()));
                }

                approveArrivalState(returnQty, receiveQty, purchaseQty, req.getPurchaseOrderDetailId());
            });

            // 更新库存信息
            updateInventoryTransCore(purchaseChangeDetailEntityList, purchaseOrderDetailEntityList);

            //推送金蝶
            DmpPushTaskEntity pushTaskEntity = syncKingdeePurchaseChangeService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
                }
            });
        }
        //审核不通过
        if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(entity.getId(),ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购变更单【%s】",ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.PURCHASE_CHANGE.getCode(),entity.getId(),"审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 判断到货状态
     *
     * @param returnQty  退货数量
     * @param receiveQty 收货数量
     * @param purchaseQty 采购数量
     * @param id          采购明细id
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/4/20 18:47
     **/
    private Boolean approveArrivalState(Integer returnQty, Integer receiveQty, Integer purchaseQty, String id) {
        String executionStatus = "";
        if (receiveQty - returnQty <= MathUtil.ZERO) {
            //已确认
            executionStatus = ExecutionStatusEnum.CONFIRM.getCode();
        } else if (receiveQty - returnQty > MathUtil.ZERO && receiveQty - returnQty < purchaseQty) {
            //送货中
            executionStatus = ExecutionStatusEnum.DELIVERY.getCode();
        } else {
            //已完成
            executionStatus = ExecutionStatusEnum.FINISH.getCode();
        }
        PurchaseOrderDetailEntity purchaseOrderDetailEntity = new PurchaseOrderDetailEntity();
        purchaseOrderDetailEntity.setId(id);
        purchaseOrderDetailEntity.setExecutionStatus(executionStatus);
        Boolean flag = purchaseOrderDetailService.updateById(purchaseOrderDetailEntity);
        return flag;
    }

    @Override
    public Boolean exportExcel(PurchaseChangeDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购变更数据", EXPORT_SCM_PURCHASE_CHANGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseChangeEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        log.info("采购变更单提交，ids=【{}】", JSONUtil.toJsonStr(ids));
        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购变更单【%s】", ModuleTypeEnum.PURCHASE_CHANGE.getCode(),pairList,"提交操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchaseChangeDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseChangeDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseChangeEntity> list = getList(ids);

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购申请单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatus(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购变更单【%s】取消流程", ModuleTypeEnum.PURCHASE_CHANGE.getCode(),pairList,"取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public List<ListStatusCountDTO.PurchaseChangeCountDTO> listCount(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<ListStatusCountDTO.PurchaseChangeCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item: values) {
            PurchaseChangeDTO.SearchParamDTO searchParamDTO = new PurchaseChangeDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            ListStatusCountDTO.PurchaseChangeCountDTO resultDTO = new ListStatusCountDTO.PurchaseChangeCountDTO();
            Integer count = MathUtil.ZERO;
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(PurchaseChangeEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchaseChangeEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public List<PurchaseChangeEntity> listByPoIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        List<PurchaseChangeEntity> list = lambdaQuery().in(PurchaseChangeEntity::getPurchaseOrderId, ids)
                .eq(PurchaseChangeEntity::getInvalidStatus, Boolean.FALSE)
                .list();
        return list;
    }

    @Override
    public PagingVO<PurchaseChangeExportExcelDTO> exportPurchaseChange(PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        Page<PurchaseChangeExportExcelDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }


    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String changeUserId, String changeDeptId, PurchaseChangeEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(changeUserId)) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(changeUserId);
            if (ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            entity.setChangeUserName(purchaseUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(changeDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(changeDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setChangeDeptName(depart.getName());
        }
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseChangeEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseChangeEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids,String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseChangeEntity::getId,ids)
                .set(PurchaseChangeEntity::getApproveStatus,approveStatus)
                .set(PurchaseChangeEntity::getApproveUserId,"")
                .set(PurchaseChangeEntity::getApproveUserName,"")
                .set(PurchaseChangeEntity::getApproveTime,null)
                .update();
    }


    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(String id,String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().eq(PurchaseChangeEntity::getId,id)
                .set(PurchaseChangeEntity::getApproveUserId,userInfo.getUid())
                .set(PurchaseChangeEntity::getApproveUserName,userInfo.getUserName())
                .set(PurchaseChangeEntity::getApproveStatus,approveStatus)
                .set(PurchaseChangeEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * @description: 更新作废状态
     * @author Will
     * @date: 2023/3/30 18:09
     * @param ids
     * @param reason
     */
    private void updateInvalidStatus(List<String> ids,String reason) {
        //更新
        lambdaUpdate().in(PurchaseChangeEntity::getId,ids)
                .set(PurchaseChangeEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PurchaseChangeEntity::getInvalidTime, LocalDateTime.now())
                .set(PurchaseChangeEntity::getInvalidRemark,reason)
                .update();
    }

    /**
     * @description: 审核通过更新采购订单数据
     * @author Will
     * @date: 2023/3/31 16:33
     * @param purchaseChangeEntity
     * @param purchaseChangeDetailList
     */
    private void updatePurchaseOrderData (PurchaseChangeEntity purchaseChangeEntity,List<PurchaseChangeDetailEntity> purchaseChangeDetailList) {
        if (CollectionUtils.isEmpty(purchaseChangeDetailList)) {
            throw new ServiceException(ApiError.ERROR_98043);
        }
        //审核时明细数量验证
        purchaseChangeDetailService.checkPurchasePrice(purchaseChangeDetailList, purchaseChangeEntity.getId());

        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = new ArrayList<>();
        for (PurchaseChangeDetailEntity detailEntity : purchaseChangeDetailList) {
            PurchaseOrderDetailEntity entity = new PurchaseOrderDetailEntity();
            entity.setId(detailEntity.getPurchaseOrderDetailId());
            entity.setPurchaseQty(detailEntity.getQty());
            entity.setTaxPrice(detailEntity.getPrice());
            entity.setPurchaseAmount(detailEntity.getAmount());
            entity.setTaxRate(detailEntity.getTaxRate());
            purchaseOrderDetailList.add(entity);
        }
        purchaseOrderDetailService.updateBatchById(purchaseOrderDetailList);
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_TAG.getName(), purchaseOrderDetailList, IdUtil.simpleUUID());
    }

    /**
     * 数据验证
     */
    private void checkPurchaseChange (String purchaseOrderId,PurchaseChangeEntity entity) {
        //采购订单
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(purchaseOrderId);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(purchaseOrderEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98045);
        }
        //采购供应商
        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        entity.setSupplierId(supplierEntity.getSupplierId());
        entity.setSupplierName(supplierEntity.getSupplierName());
        entity.setDeliveryWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());
        entity.setDeliveryWarehouseName(purchaseOrderEntity.getDeliveryWarehouseName());
        entity.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
        entity.setPurchaseOrgName(purchaseOrderEntity.getPurchaseOrgName());
        entity.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        entity.setReceiveOrgName(purchaseOrderEntity.getReceiveOrgName());
        entity.setIsFirstMassProduct(purchaseOrderEntity.getIsFirstMassProduct());
    }
    /**
     * @description: 格式化列表数据
     * @author Will
     * @date: 2023/4/3 15:16
     * @param records
     */
    private void formatPurchaseChange( List<PurchaseChangeDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //查询流程id判断是否存在流程
        records.forEach(obj -> {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
        });
    }

    /**
     * 采购变更单库存变更（需要计算差额，原采购订单已经增加了在途（没有待检的时候））
     * @param purchaseChangeDetailList 变更单明细
     */
    public void updateInventoryTransCore(List<PurchaseChangeDetailEntity> purchaseChangeDetailList, List<PurchaseOrderDetailEntity> originPurchaseOrderDetailEntityList) {
        List<InstockForcastDTO.PoChangeDTO> dataList = Lists.newArrayList();
        Map<String,PurchaseOrderDetailEntity> detailOrderMap = originPurchaseOrderDetailEntityList.stream().collect(Collectors.toMap(PurchaseOrderDetailEntity::getId, Function.identity()));
        for(PurchaseChangeDetailEntity purchaseChangeDetailEntity : purchaseChangeDetailList) {
            InstockForcastDTO.PoChangeDTO dto = new InstockForcastDTO.PoChangeDTO();

            String detailOrderId = purchaseChangeDetailEntity.getPurchaseOrderDetailId();
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = detailOrderMap.get(detailOrderId);
            dto.setPurchaseOrderId(purchaseOrderDetailEntity.getPurchaseOrderId());
            List<InstockForcastPoChangeDetailDTO.AddDTO> members = Lists.newArrayList();
            InstockForcastPoChangeDetailDTO.AddDTO addDTO = new InstockForcastPoChangeDetailDTO.AddDTO();
            addDTO.setPurchaseOrderDetailId(detailOrderId);
            addDTO.setSkuId(purchaseChangeDetailEntity.getSkuId());
            addDTO.setSkuNo(purchaseChangeDetailEntity.getSkuNo());
            addDTO.setOriginQty(purchaseOrderDetailEntity.getPurchaseQty());
            // 新的采购订单明细采购数量
            addDTO.setQty(purchaseChangeDetailEntity.getQty());
            addDTO.setExecutionStatus(purchaseOrderDetailEntity.getExecutionStatus());
            members.add(addDTO);
            dto.setMembers(members);
            dataList.add(dto);
        }
        inventoryFeign.poChangeBatch(dataList);
    }

}
