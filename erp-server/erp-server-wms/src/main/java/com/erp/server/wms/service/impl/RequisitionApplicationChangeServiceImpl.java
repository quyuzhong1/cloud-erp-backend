package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationTypeEnum;
import com.erp.model.wms.enums.RequisitionChangeTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.RequisitionApplicationChangeMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REQUISITION_APPLICATION_CHANGE;

/**
 * <p>
 * 要货申请变更单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
@Slf4j
@Service
public class RequisitionApplicationChangeServiceImpl extends SuperServiceImpl<RequisitionApplicationChangeMapper, RequisitionApplicationChangeEntity> implements RequisitionApplicationChangeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OmsListingInfoFeign listingInfoFeign;

    @Resource
    @Lazy
    private RequisitionApplicationChangeService requisitionApplicationChangeService;

    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Lazy
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private RequisitionApplicationChangeDetailService detailService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationChangeDTO.ViewDTO addDTO) {
        RequisitionApplicationChangeEntity requisitionApplicationChangeEntity = new RequisitionApplicationChangeEntity();
        List<String> businessDetailIds = addDTO.getViewDetailList().stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getRequisitionDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        this.checkExist(businessDetailIds,addDTO.getBusinessId(), addDTO.getId());
        // 数据处理
        handleData(requisitionApplicationChangeEntity,addDTO);

        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YHBG);
        requisitionApplicationChangeEntity.setCode(code);
        boolean save = super.save(requisitionApplicationChangeEntity);
        if(!save) {
            throw new ServiceException("要货申请变更单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "要货申请变更单" , requisitionApplicationChangeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), requisitionApplicationChangeEntity.getId(), "新增操作");
        detailService.add(requisitionApplicationChangeEntity,addDTO);
        return new BaseResultDTO.AddDTO(requisitionApplicationChangeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationChangeDTO.ViewDTO updateDTO) {
        RequisitionApplicationChangeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请变更单"));
        List<String> businessDetailIds = updateDTO.getViewDetailList().stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getRequisitionDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        this.checkExist(businessDetailIds,updateDTO.getBusinessId(), updateDTO.getId());
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        if(!SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(old.getSourceType())){
            throw new ServiceException("拣货单修改提交生成的变更单，不允许编辑");
        }
        //只会修改明细
        detailService.update(updateDTO,old);

        return Boolean.TRUE;
    }


    @Override
    public PagingVO<RequisitionApplicationChangeDTO.ListDTO> paging(PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RequisitionApplicationChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<RequisitionApplicationChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        RequisitionApplicationChangeDTO.PagingParamDTO searchParam = new RequisitionApplicationChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RequisitionApplicationChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RequisitionApplicationChangeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new RequisitionApplicationChangeDTO.TabListDTO(status,"", 0));
            }
        });
        list.forEach(v->{
            if(v.getTabFlag().equals(ApproveStatusEnum.WAIT_SUBMIT.getCode())){
                v.setTabFlagName("待提交");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.APPROVE_ING.getCode())){
                v.setTabFlagName("待审核");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.APPROVE.getCode())){
                v.setTabFlagName("审核通过");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.REJECT.getCode())){
                v.setTabFlagName("审核不通过");
            }
        });
        list.add(new RequisitionApplicationChangeDTO.TabListDTO("","全部", list.stream().mapToInt(RequisitionApplicationChangeDTO.TabListDTO::getCount).sum()));
        // 定义排序顺序
        Map<String, Integer> orderMap = new HashMap<>();
        orderMap.put(ApproveStatusEnum.WAIT_SUBMIT.getCode(), 0);
        orderMap.put(ApproveStatusEnum.APPROVE_ING.getCode(), 1);
        orderMap.put(ApproveStatusEnum.APPROVE.getCode(), 2);
        orderMap.put(ApproveStatusEnum.REJECT.getCode(), 3);

        // 排序
        list.sort((o1, o2) -> {
            Integer order1 = orderMap.getOrDefault(o1.getTabFlag(), 4); // 4 表示“全部”
            Integer order2 = orderMap.getOrDefault(o2.getTabFlag(), 4);
            return order1.compareTo(order2);
        });
        return list;
    }

    @Override
    public void exportList(RequisitionApplicationChangeDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("要货申请通知变更单导出", EXPORT_WMS_REQUISITION_APPLICATION_CHANGE.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        RequisitionApplicationChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到要货申请变更单数据");
        }
        if(entity.getInvalidStatus()){
            throw new ServiceException("该单据已作废，无法提交");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        startProcess(entity);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), entity.getId(), "提交操作");
        Map<String, String> map = new HashMap<>();
        map.put("code", entity.getCode());
        map.put("createUserId", entity.getCreateUserId());
        map.put("createUserName", entity.getCreateUserName());
        map.put("approveStatus", "提交审核");
        requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_CHANGE_SUBMIT_NOTICE);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(RequisitionApplicationChangeDTO.ViewDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(RequisitionApplicationChangeDTO.ViewDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(String id, List<RequisitionApplicationChangeDTO.ApproveView> approveViewList, String type) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(type);
        RequisitionApplicationChangeEntity entity = getById(id);
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //保存虚拟仓数据
        detailService.updateVirtualWarehouse(approveViewList, approveType);
        // 调用流程审核
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(type);
        approveProcess(entity, approveOneDTO);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单", approveType.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        requisitionApplicationChangeService.sendMsg(id, approveType, entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }


    @Async
    @Override
    public void sendMsg(String id, ApproveTypeEnum approveType, RequisitionApplicationChangeEntity entity) {
        Map<String, String> map = new HashMap<>();
        map.put("code", entity.getCode());
        map.put("createUserId", entity.getCreateUserId());
        map.put("createUserName", entity.getCreateUserName());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT)){
            map.put("approveStatus", "审核不通过");
        }else{
            map.put("approveStatus", "审核通过");
        }
        map.put("approveUserName", UserContext.getDefaultLoginUser().getUserName());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), id));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        //最新待审核人
        if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
            String curApprove = listApiResult.getData().get(0).getCurApproveName();
            if(StringUtils.isNotBlank(curApprove)){
                map.put("approveUserName", curApprove);
            }
        }
        requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_CHANGE_APPROVE_NOTICE);
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(RequisitionApplicationChangeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode());
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
            this.approveEnd(dto, entity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        RequisitionApplicationChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        super.removeById(id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), entity.getCode(), "删除要货申请变更单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        RequisitionApplicationChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, RequisitionApplicationChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(entity.getBusinessId()).orElseThrow(() -> new ServiceException("未找到要货申请数据"));
        if(requisitionApplicationEntity.getStatus().equals(RequisitionApplicationStatusEnum.HANDLE.getStatus())){
            throw new ServiceException("要货申请已完成，不允许审核");
        }
        updateForApprove(entity.getId(), approveStatus.getStatus());
        if (dto.getType().equals(ApproveType.PASS)) {
            List<RequisitionApplicationChangeDetailEntity> detailEntityList = detailService.listByMains(Collections.singletonList(entity.getId()));
            RequisitionApplicationChangeDTO.UpdateVirtualDTO updateVirtualDTO;
            if(entity.getSourceType().equals(SourceTypeEnum.REQUISITION_APPLICATION.getCode())){
                updateVirtualDTO = changeRequisition(entity,detailEntityList);
            }else{
                updateVirtualDTO = changeRequisitionByPicking(entity,detailEntityList);
            }

            //虚拟库存变更
            virtualInventoryChange(updateVirtualDTO,requisitionApplicationEntity);
        }
        return Boolean.TRUE;
    }

    /**
     * 处理拣货单生成的要货申请明细
     * @param entity
     * @param detailEntityList
     * @return
     */
    private RequisitionApplicationChangeDTO.UpdateVirtualDTO changeRequisitionByPicking(RequisitionApplicationChangeEntity entity, List<RequisitionApplicationChangeDetailEntity> detailEntityList) {
        RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(entity.getBusinessId()).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
        List<PickingListsEntity> pickingListsEntityList = pickingListsService.list(new QueryWrapper<PickingListsEntity>().eq("source_id", requisitionApplicationEntity.getId()));
        List<PickingDetailEntity> allDetailList = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getMainId, pickingListsEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList())));
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(requisitionApplicationEntity.getId()));

        List<RequisitionApplicationDetailEntity> updateList = new ArrayList<>();
        List<PickingDetailEntity> updatePickingList = new ArrayList<>();
        for (RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity : detailEntityList) {
            List<PickingDetailEntity> pickingDetailList = allDetailList.stream().filter(v -> v.getSourceDetailId().equals(requisitionApplicationChangeDetailEntity.getBusinessDetailId())).collect(Collectors.toList());
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationDetailEntityList.stream().filter(v -> v.getId().equals(requisitionApplicationChangeDetailEntity.getBusinessDetailId())).findFirst().orElseThrow(()->new ServiceException("{}未找到要货申请单明细数据",requisitionApplicationChangeDetailEntity.getSkuNo()));
            requisitionApplicationDetailEntity.setChangeBeforeQty(requisitionApplicationDetailEntity.getApproveQty());
            requisitionApplicationDetailEntity.setApproveQty(requisitionApplicationChangeDetailEntity.getNewQty());
            requisitionApplicationDetailEntity.setRequisitionQty(requisitionApplicationChangeDetailEntity.getNewQty());
            requisitionApplicationDetailEntity.setVirtualFrozenQty(requisitionApplicationChangeDetailEntity.getNewQty());
            updateList.add(requisitionApplicationDetailEntity);
            List<PickingDetailEntity> notEqualList = pickingDetailList.stream().filter(v->!v.getQty().equals(v.getActualQty()) || !v.getWarehouseLocation().equals(v.getOriginWarehouseLocation())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(notEqualList)){
                Integer allPickQty = pickingDetailList.stream().mapToInt(PickingDetailEntity::getQty).sum();
                if(!allPickQty.equals(requisitionApplicationChangeDetailEntity.getNewQty()) && CollectionUtils.isNotEmpty(pickingDetailList)){
                    PickingDetailEntity pickingDetailEntity = pickingDetailList.get(0);
                    pickingDetailEntity.setChangeBeforeQty(pickingDetailEntity.getQty());
                    pickingDetailEntity.setQty(pickingDetailEntity.getActualQty());
                    updatePickingList.add(pickingDetailEntity);
                }
            }else{
                notEqualList.forEach(v->{
                    v.setChangeBeforeQty(v.getQty());
                    v.setQty(v.getActualQty());
                });
                updatePickingList.addAll(notEqualList);
            }
        }

        requisitionApplicationService.updateByChange(new ArrayList<>(),updateList,new ArrayList<>());
        pickingListsService.updateByChange(updatePickingList, updateList.stream().map(BaseEntity::getId).collect(Collectors.toList()),false );
        return new RequisitionApplicationChangeDTO.UpdateVirtualDTO(new ArrayList<>(),updateList,new ArrayList<>());
    }

    private void virtualInventoryChange(RequisitionApplicationChangeDTO.UpdateVirtualDTO updateVirtualDTO,RequisitionApplicationEntity requisitionApplicationEntity ) {

        List<VirtualInventoryStockDTO.OutInStockDTO> addNoticeParamList = new ArrayList<>();

        List<VirtualInventoryStockDTO.OutInStockDTO> subNoticeParamList = new ArrayList<>();

        List<RequisitionApplicationDetailEntity> addList = updateVirtualDTO.getAddList().stream().filter(v->StringUtils.isNotBlank(v.getFromVirtualWarehouseId())).collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> updateList = updateVirtualDTO.getUpdateList().stream().filter(v->StringUtils.isNotBlank(v.getFromVirtualWarehouseId())).collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> removeList = updateVirtualDTO.getRemoveList().stream().filter(v->StringUtils.isNotBlank(v.getFromVirtualWarehouseId())).collect(Collectors.toList());

        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : addList) {
            Integer diffQty = requisitionApplicationDetailEntity.getApproveQty();
            handleRequisitionAddParam(requisitionApplicationEntity, requisitionApplicationDetailEntity,addNoticeParamList,subNoticeParamList,diffQty);
        }
        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : updateList) {
            Integer diffQty = requisitionApplicationDetailEntity.getApproveQty() - requisitionApplicationDetailEntity.getChangeBeforeQty();
            handleRequisitionAddParam(requisitionApplicationEntity, requisitionApplicationDetailEntity,addNoticeParamList,subNoticeParamList,diffQty);
        }
        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : removeList) {
            Integer diffQty = -requisitionApplicationDetailEntity.getApproveQty();
            handleRequisitionAddParam(requisitionApplicationEntity, requisitionApplicationDetailEntity,addNoticeParamList,subNoticeParamList,diffQty);
        }
        if (CollectionUtils.isNotEmpty(addNoticeParamList)) {
            //添加冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(addNoticeParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        if (CollectionUtils.isNotEmpty(subNoticeParamList)) {
            //减少冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(subNoticeParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_RETURN_HANDLE.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
    }

    private void handleRequisitionAddParam(RequisitionApplicationEntity requisitionApplicationEntity, RequisitionApplicationDetailEntity requisitionApplicationDetailEntity, List<VirtualInventoryStockDTO.OutInStockDTO> addNoticeParamList, List<VirtualInventoryStockDTO.OutInStockDTO> subNoticeParamList, Integer diffQty) {
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.REQUISITION_APPLICATION);
        outInStockDTO.setSourceId(requisitionApplicationEntity.getId());
        outInStockDTO.setSourceCode(requisitionApplicationEntity.getCode());
        outInStockDTO.setSourceDetailId(requisitionApplicationDetailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(requisitionApplicationDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(requisitionApplicationDetailEntity.getSkuNo());
        outInStockDTO.setQty(Math.abs(diffQty));
        outInStockDTO.setWarehouseId(requisitionApplicationEntity.getRequisitionWarehouseId());
        outInStockDTO.setVirtualWarehouseId(requisitionApplicationDetailEntity.getFromVirtualWarehouseId());
        //库存数量为0不添加
        if (MathUtil.compareTo(outInStockDTO.getQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return;
        }
        if (diffQty > MathUtil.ZERO) {
            addNoticeParamList.add(outInStockDTO);
        } else {
            subNoticeParamList.add(outInStockDTO);
        }
    }

    /**
     * 处理要货申请下推变更单审核
     * @param entity
     * @param detailList
     */
    private RequisitionApplicationChangeDTO.UpdateVirtualDTO changeRequisition(RequisitionApplicationChangeEntity entity, List<RequisitionApplicationChangeDetailEntity> detailList) {
        if(CollectionUtils.isEmpty(detailList)){
            return new RequisitionApplicationChangeDTO.UpdateVirtualDTO();
        }
        RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(entity.getBusinessId()).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(requisitionApplicationEntity.getId()));
        List<String> requisitionDetailIds = requisitionApplicationDetailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(requisitionDetailIds);

        List<RequisitionApplicationChangeDetailEntity> sourceDetailList = new ArrayList<>();
        List<RequisitionApplicationDetailEntity> addList = new ArrayList<>();
        List<RequisitionApplicationDetailEntity> updateList = new ArrayList<>();
        List<RequisitionApplicationDetailEntity> deleteList = new ArrayList<>();
        List<PickingDetailEntity> updatePickingList = new ArrayList<>();
        List<String> skuIds = detailList.stream().map(RequisitionApplicationChangeDetailEntity::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);

        for (RequisitionApplicationChangeDetailEntity detail : detailList) {
            if(RequisitionChangeTypeEnum.ADD.getCode().equals(detail.getChangeType())){
                RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = BeanUtil.copyProperties(detail,RequisitionApplicationDetailEntity.class);
                requisitionApplicationDetailEntity.setChangeBeforeQty(0);
                requisitionApplicationDetailEntity.setMainId(requisitionApplicationEntity.getId());
                requisitionApplicationDetailEntity.setSourceDetailId(detail.getId());
                requisitionApplicationDetailEntity.setPlatformSku(detail.getPlatformSkuNo());
                requisitionApplicationDetailEntity.setPlatformSkuName(detail.getPlatformSkuName());
                requisitionApplicationDetailEntity.setPlatformSpu(detail.getPlatformSpu());
                requisitionApplicationDetailEntity.setPlatformFnSku(detail.getFnSku());
                requisitionApplicationDetailEntity.setBomVersion(detail.getBomVersion());
                requisitionApplicationDetailEntity.setApproveQty(detail.getNewQty());
                requisitionApplicationDetailEntity.setRequisitionQty(detail.getNewQty());
                requisitionApplicationDetailEntity.setId(IdWorker.getIdStr());
                requisitionApplicationDetailEntity.setFromVirtualWarehouseId(detail.getFromVirtualWarehouseId());
                requisitionApplicationDetailEntity.setFromVirtualWarehouseName(detail.getFromVirtualWarehouseName());
                requisitionApplicationDetailEntity.setFromWarehouseId(requisitionApplicationEntity.getRequisitionWarehouseId());
                requisitionApplicationDetailEntity.setFromWarehouseName(requisitionApplicationEntity.getRequisitionWarehouseName());
                requisitionApplicationDetailEntity.setToWarehouseId(requisitionApplicationEntity.getRequisitionWarehouseId());
                requisitionApplicationDetailEntity.setToWarehouseName(requisitionApplicationEntity.getRequisitionWarehouseName());
                requisitionApplicationDetailEntity.setVirtualFrozenQty(detail.getNewQty());

                addList.add(requisitionApplicationDetailEntity);
                detail.setSourceDetailId(requisitionApplicationDetailEntity.getId());
                sourceDetailList.add(detail);
            }else if (RequisitionChangeTypeEnum.UPDATE.getCode().equals(detail.getChangeType())){
                RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationDetailEntityList.stream().filter(v -> v.getId().equals(detail.getBusinessDetailId())).findFirst().orElseThrow(()->new ServiceException("{}未找到要货申请单明细数据",detail.getSkuNo()));
                requisitionApplicationDetailEntity.setChangeBeforeQty(requisitionApplicationDetailEntity.getApproveQty());
                requisitionApplicationDetailEntity.setSkuId(detail.getSkuId());
                requisitionApplicationDetailEntity.setSkuNo(detail.getSkuNo());
                requisitionApplicationDetailEntity.setPlatformSku(detail.getPlatformSkuNo());
                requisitionApplicationDetailEntity.setPlatformSkuName(detail.getPlatformSkuName());
                requisitionApplicationDetailEntity.setPlatformSpu(detail.getPlatformSpu());
                requisitionApplicationDetailEntity.setPlatformFnSku(detail.getFnSku());
                requisitionApplicationDetailEntity.setBomVersion(detail.getBomVersion());
                requisitionApplicationDetailEntity.setApproveQty(detail.getNewQty());
                requisitionApplicationDetailEntity.setRequisitionQty(detail.getNewQty());
                requisitionApplicationDetailEntity.setVirtualFrozenQty(detail.getNewQty());
                updateList.add(requisitionApplicationDetailEntity);
                List<BomChildrenSkuDTO> bomChildrenList = bomChildrenSkuList.stream()
                        .filter(req -> req.getParentSkuId().equals(requisitionApplicationDetailEntity.getSkuId())
                                && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                        ).collect(Collectors.toList());
                List<PickingDetailEntity> currentList =  pickingDetailEntityList.stream().filter(v->v.getSourceDetailId().equals(requisitionApplicationDetailEntity.getId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(bomChildrenList)){
                    for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenList) {
                        PickingDetailEntity pickingDetailEntity = currentList.stream().filter(v->v.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().orElse(null);
                        if(Objects.nonNull(pickingDetailEntity)){
                            pickingDetailEntity.setChangeBeforeQty(pickingDetailEntity.getQty());
                            pickingDetailEntity.setQty(detail.getNewQty()*bomChildrenSkuDTO.getQuantity());
                            updatePickingList.add(pickingDetailEntity);
                        }
                    }
                }else{
                    PickingDetailEntity pickingDetailEntity = pickingDetailEntityList.stream().filter(v->v.getSourceDetailId().equals(requisitionApplicationDetailEntity.getId())).findFirst().orElse(null);
                    if(Objects.nonNull(pickingDetailEntity)){
                        Integer originPickQty = currentList.stream().mapToInt(PickingDetailEntity::getQty).sum();
                        pickingDetailEntity.setChangeBeforeQty(pickingDetailEntity.getQty());
                        pickingDetailEntity.setQty(detail.getNewQty() - originPickQty + pickingDetailEntity.getQty());
                        updatePickingList.add(pickingDetailEntity);
                    }
                }
            }else if (RequisitionChangeTypeEnum.DELETE.getCode().equals(detail.getChangeType())){
                RequisitionApplicationDetailEntity deleteEntity = requisitionApplicationDetailEntityList.stream().filter(v -> v.getId().equals(detail.getBusinessDetailId())).findFirst().orElseThrow(()->new ServiceException("{}未找到要货申请单明细数据",detail.getSkuNo()));
                deleteEntity.setChangeBeforeQty(deleteEntity.getRequisitionQty());
                deleteList.add(deleteEntity);
            }
        }

        requisitionApplicationService.updateByChange(addList,updateList,deleteList);
        pickingListsService.updateByChange(updatePickingList, new ArrayList<>(), true);

        return new RequisitionApplicationChangeDTO.UpdateVirtualDTO(addList,updateList,deleteList);
    }

    @Override
    public PagingVO<RequisitionApplicationChangeDTO.ProductDTO> addProductPaging(PagingDTO<RequisitionApplicationChangeDTO.ProductAddDTO> dto) {
        RequisitionApplicationChangeDTO.ProductAddDTO param = dto.getParams();
        RequisitionApplicationEntity requisitionApplicationEntity =requisitionApplicationService.getByIdOpt(param.getRequisitionId()).orElseThrow(() -> new ServiceException("未找到要货申请数据"));
        PagingDTO<ListingInfoDTO.PagingParamDTO> paramDTO = new PagingDTO<>();
        BeanUtil.copyProperties(dto,paramDTO);
        ListingInfoDTO.PagingParamDTO listingParamDTO = new ListingInfoDTO.PagingParamDTO();
        listingParamDTO.setAdvanceQueryDTOList(new ArrayList<>());
        listingParamDTO.setSqlMap(param.getSqlMap());
        if (RequisitionApplicationTypeEnum.FBA.getCode().equals(requisitionApplicationEntity.getType())) {
            listingParamDTO.setShopId(requisitionApplicationEntity.getChannelId());
        } else {
            listingParamDTO.setWarehouseId(requisitionApplicationEntity.getChannelId());
        }
        paramDTO.setParams(listingParamDTO);
        //调用listing接口获取商品信息
        PagingVO<ListingInfoDTO.PageDTO> pagingVO = listingInfoFeign.paging(paramDTO);
        PagingVO<RequisitionApplicationChangeDTO.ProductDTO> result = new PagingVO<>();
        BeanUtil.copyProperties(pagingVO,result);
        if(CollUtil.isEmpty(pagingVO.getList())){
            return result;
        }
        List<ListingInfoDTO.PageDTO> listingList = pagingVO.getList();
        List<RequisitionApplicationChangeDTO.ProductDTO> productList = new ArrayList<>();
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(requisitionApplicationEntity.getId()));
        List<String> skuNos = listingList.stream().map(ListingInfoDTO.PageDTO::getSkuNo).collect(Collectors.toList());
        List<ProductBomInfoDTO.SkuBomVersion> skuBomVersionList = plmTaskFeign.listBomVersionBySkuNos(skuNos);
        for (ListingInfoDTO.PageDTO pageDTO : listingList) {
            RequisitionApplicationChangeDTO.ProductDTO productDTO = new RequisitionApplicationChangeDTO.ProductDTO();
            productDTO.setPlatformSku(pageDTO.getPlatformSku());
            productDTO.setPlatformSkuName(pageDTO.getPlatformSkuName());
            productDTO.setSkuId(pageDTO.getSkuId());
            productDTO.setSkuNo(pageDTO.getSkuNo());
            productDTO.setProductName(pageDTO.getProductName());
            productDTO.setFnSku(pageDTO.getFnSku());
            productDTO.setAsin(pageDTO.getAsin());
            ProductBomInfoDTO.SkuBomVersion skuBomVersion = skuBomVersionList.stream().filter(v->v.getSkuNo().equals(pageDTO.getSkuNo())).findFirst().orElse(null);
            if(Objects.nonNull(skuBomVersion)){
                productDTO.setBomVersion(skuBomVersion.getBomVersionList().stream().max(String::compareTo).orElse(""));
            }
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationDetailEntityList.stream().filter(v->v.getPlatformSku().equals(pageDTO.getPlatformSku())).findFirst().orElse(null);
            //如果能关联到要货申明细，设置为修改类型
            if(Objects.nonNull(requisitionApplicationDetailEntity)){
                productDTO.setRequisitionDetailId(requisitionApplicationDetailEntity.getId());
                productDTO.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                productDTO.setChangeType(RequisitionChangeTypeEnum.UPDATE.getCode());
                productDTO.setChangeTypeName(RequisitionChangeTypeEnum.UPDATE.getName());
                productDTO.setOriginRequisitionQty(requisitionApplicationDetailEntity.getRequisitionQty());
            }else{
                productDTO.setChangeType(RequisitionChangeTypeEnum.ADD.getCode());
                productDTO.setChangeTypeName(RequisitionChangeTypeEnum.ADD.getName());
            }
            productList.add(productDTO);
        }
        List<String> patchPlatformSkuNos = param.getPlatformSkuNoList();
        if(CollUtil.isNotEmpty(patchPlatformSkuNos)){
            List<RequisitionApplicationChangeDTO.ProductDTO> productDTOS = new ArrayList<>();
            for (String patchPlatformSkuNo : patchPlatformSkuNos) {
                RequisitionApplicationChangeDTO.ProductDTO productDTO = productList.stream().filter(v->v.getPlatformSku().equals(patchPlatformSkuNo)).findFirst().orElse(new RequisitionApplicationChangeDTO.ProductDTO());
                productDTOS.add(productDTO);
            }
            result.setList(productDTOS);
        }else{
            result.setList(productList);
        }
        //获取子SKU集合
        List<String> requisitionIds = result.getList().stream().map(RequisitionApplicationChangeDTO.ProductDTO::getRequisitionDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(requisitionIds);
        List<String> skuIds = result.getList().stream().filter(v->StringUtils.isNotBlank(v.getRequisitionDetailId())).map(RequisitionApplicationChangeDTO.ProductDTO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> allBomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        for (RequisitionApplicationChangeDTO.ProductDTO productDTO : result.getList()) {
            if(StringUtils.isBlank(productDTO.getRequisitionDetailId())){
                continue;
            }
            List<BomChildrenSkuDTO> currentBomList = allBomChildrenSkuList.stream().filter(v->v.getParentSkuId().equals(productDTO.getSkuId())&& BomTypeEnum.COMBINATION.getType().equals(v.getType())).collect(Collectors.toList());
            List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(productDTO.getRequisitionDetailId())).collect(Collectors.toList());
            int pickedQty;
            if(CollectionUtils.isNotEmpty(currentBomList) && CollectionUtils.isNotEmpty(currentPickList)){
                Integer bomQty = currentBomList.stream().filter(v->v.getSkuId().equals(currentPickList.get(0).getSkuId())).findFirst().map(BomChildrenSkuDTO::getQuantity).orElse(0);
                pickedQty = bomQty * currentPickList.get(0).getQty();
            }else{
                pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getQty).sum();
            }
            productDTO.setPickQty(pickedQty);
        }
        return result;
    }

    @Override
    public BatchResultDTO invalid(String id, String remark) {
        RequisitionApplicationChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
        // 只有待提交数据允许作废
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException("只有待提交或审核不通过数据支持作废");
        }
        if(entity.getInvalidStatus()){
            throw new ServiceException("该数据已作废");
        }
        // 删除主单数据
        entity.setInvalidStatus(true);
        super.updateById(entity);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 ，备注：{}", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单",remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public List<RequisitionApplicationChangeEntity> listNotHandleByBusinessIds(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(RequisitionApplicationChangeEntity::getBusinessId, ids).eq(RequisitionApplicationChangeEntity::getInvalidStatus, false).ne(RequisitionApplicationChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()).list();
    }

    @Override
    public List<RequisitionApplicationChangeDetailEntity> listNotHandleDetailByBusinessDetailIds(List<String> businessDetailIds) {
        if(CollectionUtils.isEmpty(businessDetailIds)){
            return new ArrayList<>();
        }
        return baseMapper.listNotHandleDetailByBusinessDetailIds(businessDetailIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateByPickingList(PickingListsDTO.AddChangeDTO addChangeDTO, PickingListsEntity entity) {
        RequisitionApplicationChangeEntity requisitionApplicationChangeEntity = new RequisitionApplicationChangeEntity();
        requisitionApplicationChangeEntity.setSourceCode(entity.getCode());
        requisitionApplicationChangeEntity.setBusinessCode(entity.getSourceCode());
        requisitionApplicationChangeEntity.setSourceId(entity.getId());
        requisitionApplicationChangeEntity.setBusinessId(entity.getSourceId());
        requisitionApplicationChangeEntity.setSourceType(SourceTypeEnum.PICKING_LISTS.getCode());
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YHBG);
        requisitionApplicationChangeEntity.setCode(code);
        boolean save = super.save(requisitionApplicationChangeEntity);
        if(!save) {
            throw new ServiceException("要货申请变更单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】修改拣货单自动新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "要货申请变更单" , requisitionApplicationChangeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), requisitionApplicationChangeEntity.getId(), "新增操作");
        detailService.addByPicking(requisitionApplicationChangeEntity, addChangeDTO);

        this.submit(requisitionApplicationChangeEntity.getId());
    }

    @Override
    public List<RequisitionApplicationChangeDTO.ApproveView> approveView(BaseIdsDTO.IdsDTO dto) {
        List<RequisitionApplicationChangeDTO.ApproveView> list = baseMapper.approveView(dto.getIds());
        long count = list.stream().filter(req -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException("只有审核中可以审核");
        }
        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        for (RequisitionApplicationChangeDTO.ApproveView approveView : list) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(approveView.getSkuId())).findFirst().orElse(new SkuVO());
            approveView.setProductName(skuVO.getSkuName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(approveView.getSkuId())
                            && req.getBomVersion().equals(approveView.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equalsIgnoreCase(req.getType())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                approveView.setIsCombination(Boolean.TRUE);
            } else {
                approveView.setIsCombination(Boolean.FALSE);
            }
            approveView.setChangeTypeName(RequisitionChangeTypeEnum.getName(approveView.getChangeType()));
        }
        return list;
    }

    @Override
    public RequisitionApplicationChangeDTO.ViewDTO view(RequisitionApplicationChangeDTO.ViewIdDTO viewIdDTO) {
        String type = viewIdDTO.getType();
        String id = viewIdDTO.getId();
        RequisitionApplicationChangeEntity requisitionApplicationChangeEntity = new RequisitionApplicationChangeEntity();
        RequisitionApplicationEntity requisitionApplicationEntity;
        if("pushDown".equals(type)){
            requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请数据"));
            if(requisitionApplicationEntity.getStatus().equals(RequisitionApplicationStatusEnum.HANDLE.getStatus())){
                throw new ServiceException("要货申请已完成，不允许下推变更单");
            }
            List<PickingListsDTO.SourceView> pickingList = pickingListsService.listBySourceIds(Collections.singletonList(requisitionApplicationEntity.getId()));
            if(CollUtil.isEmpty(pickingList)){
                throw new ServiceException("未生成拣货单的要货申请不允许下推变更单");
            }
        }else{
            requisitionApplicationChangeEntity = this.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
            requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(requisitionApplicationChangeEntity.getBusinessId()).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
        }
        //校验是否存在未处理
        if(CollectionUtils.isNotEmpty(viewIdDTO.getDetailIds())){
            List<RequisitionApplicationChangeDTO.ExistDTO> existDTOList = detailService.checkExist(viewIdDTO.getDetailIds(), new ArrayList<>());
            if(CollectionUtils.isNotEmpty(existDTOList)){
                String existCode = existDTOList.get(0).getCode();
                List<String> existSkuNos = existDTOList.stream().map(RequisitionApplicationChangeDTO.ExistDTO::getSkuNo).collect(Collectors.toList());
                throw new ServiceException("存在处理中的要货申请变更单【{}】，sku【{}】，请等待审核完成后操作",existCode,existSkuNos);
            }
        }
        RequisitionApplicationChangeDTO.ViewDTO result = new RequisitionApplicationChangeDTO.ViewDTO();
        buildView(result, requisitionApplicationChangeEntity, type, requisitionApplicationEntity);
        //处理明细
        List<RequisitionApplicationChangeDTO.ViewDetailDTO> details = new ArrayList<>();
        buildViewDetail(viewIdDTO, details, requisitionApplicationChangeEntity);

        result.setViewDetailList(details);

        return result;
    }

    private void buildViewDetail(RequisitionApplicationChangeDTO.ViewIdDTO viewIdDTO, List<RequisitionApplicationChangeDTO.ViewDetailDTO> details, RequisitionApplicationChangeEntity requisitionApplicationChangeEntity) {

        if(CollectionUtils.isNotEmpty(viewIdDTO.getDetailIds())){
            List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailService.listByIds(viewIdDTO.getDetailIds());
            //查询产品信息
            List<String> skuIdList = requisitionApplicationDetailEntityList.stream().map(RequisitionApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
            for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : requisitionApplicationDetailEntityList) {
                RequisitionApplicationChangeDTO.ViewDetailDTO viewDetailDTO = new RequisitionApplicationChangeDTO.ViewDetailDTO();
                viewDetailDTO.setRequisitionDetailId(requisitionApplicationDetailEntity.getId());
                viewDetailDTO.setSourceDetailId(requisitionApplicationDetailEntity.getId());
                viewDetailDTO.setPlatformSku(requisitionApplicationDetailEntity.getPlatformSku());
                viewDetailDTO.setPlatformSkuName(requisitionApplicationDetailEntity.getPlatformSkuName());
                viewDetailDTO.setSkuId(requisitionApplicationDetailEntity.getSkuId());
                viewDetailDTO.setSkuNo(requisitionApplicationDetailEntity.getSkuNo());
                viewDetailDTO.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
                viewDetailDTO.setAsin(requisitionApplicationDetailEntity.getPlatformSpu());
                viewDetailDTO.setChangeType(RequisitionChangeTypeEnum.UPDATE.getCode());
                viewDetailDTO.setChangeTypeName(RequisitionChangeTypeEnum.UPDATE.getName());
                viewDetailDTO.setOriginRequisitionQty(requisitionApplicationDetailEntity.getRequisitionQty());
                viewDetailDTO.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                viewDetailDTO.setProductName(skuVO.getSkuName());

                details.add(viewDetailDTO);
            }
        }
        List<RequisitionApplicationChangeDetailEntity> detailEntityList = detailService.listByMains(Collections.singletonList(requisitionApplicationChangeEntity.getId()));
        //查询产品信息
        List<String> skuIdList = detailEntityList.stream().map(RequisitionApplicationChangeDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        for (RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity : detailEntityList) {
            RequisitionApplicationChangeDTO.ViewDetailDTO viewDetailDTO = new RequisitionApplicationChangeDTO.ViewDetailDTO();
            viewDetailDTO.setDetailId(requisitionApplicationChangeDetailEntity.getId());
            viewDetailDTO.setRequisitionDetailId(requisitionApplicationChangeDetailEntity.getBusinessDetailId());
            viewDetailDTO.setSourceDetailId(requisitionApplicationChangeDetailEntity.getSourceDetailId());
            viewDetailDTO.setPlatformSku(requisitionApplicationChangeDetailEntity.getPlatformSkuNo());
            viewDetailDTO.setPlatformSkuName(requisitionApplicationChangeDetailEntity.getPlatformSkuName());
            viewDetailDTO.setSkuId(requisitionApplicationChangeDetailEntity.getSkuId());
            viewDetailDTO.setSkuNo(requisitionApplicationChangeDetailEntity.getSkuNo());
            viewDetailDTO.setFnSku(requisitionApplicationChangeDetailEntity.getFnSku());
            viewDetailDTO.setAsin(requisitionApplicationChangeDetailEntity.getPlatformSpu());
            viewDetailDTO.setChangeType(requisitionApplicationChangeDetailEntity.getChangeType());
            viewDetailDTO.setChangeTypeName(RequisitionChangeTypeEnum.getName(requisitionApplicationChangeDetailEntity.getChangeType()));
            viewDetailDTO.setOriginRequisitionQty(requisitionApplicationChangeDetailEntity.getOriginQty());
            viewDetailDTO.setNewRequisitionQty(requisitionApplicationChangeDetailEntity.getNewQty());
            viewDetailDTO.setRemark(requisitionApplicationChangeDetailEntity.getRemark());
            viewDetailDTO.setBomVersion(requisitionApplicationChangeDetailEntity.getBomVersion());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationChangeDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            viewDetailDTO.setProductName(skuVO.getSkuName());
            details.add(viewDetailDTO);
        }

        List<String> requisitionDetailIds = details.stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getRequisitionDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> skuIds = details.stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> allBomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(requisitionDetailIds);
        for (RequisitionApplicationChangeDTO.ViewDetailDTO detail : details) {
            List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(detail.getRequisitionDetailId())).collect(Collectors.toList());
            List<BomChildrenSkuDTO> currentBomList = allBomChildrenSkuList.stream().filter(v->v.getParentSkuId().equals(detail.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(v.getType())).collect(Collectors.toList());
            int pickedQty;
            if(CollectionUtils.isNotEmpty(currentBomList) && CollectionUtils.isNotEmpty(currentPickList)){
                Integer bomQty = currentBomList.stream().filter(v->v.getSkuId().equals(currentPickList.get(0).getSkuId())).findFirst().map(BomChildrenSkuDTO::getQuantity).orElse(0);
                pickedQty = bomQty * currentPickList.get(0).getQty();
            }else{
                pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getQty).sum();
            }
            detail.setPickQty(pickedQty);
        }
    }

    private void buildView(RequisitionApplicationChangeDTO.ViewDTO result, RequisitionApplicationChangeEntity requisitionApplicationChangeEntity, String type, RequisitionApplicationEntity requisitionApplicationEntity) {
        result.setId(requisitionApplicationChangeEntity.getId());
        result.setCode(requisitionApplicationChangeEntity.getCode());
        if("pushDown".equals(type)){
            result.setSourceId(requisitionApplicationEntity.getId());
            result.setSourceCode(requisitionApplicationEntity.getCode());
            result.setBusinessId(requisitionApplicationEntity.getId());
            result.setBusinessCode(requisitionApplicationEntity.getCode());
            result.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            result.setApproveStatusName(ApproveStatusEnum.WAIT_SUBMIT.getName());
            result.setSourceType(SourceTypeEnum.REQUISITION_APPLICATION.getCode());
        }else{
            result.setSourceId(requisitionApplicationChangeEntity.getSourceId());
            result.setSourceCode(requisitionApplicationChangeEntity.getSourceCode());
            result.setSourceType(requisitionApplicationChangeEntity.getSourceType());
            result.setBusinessId(requisitionApplicationChangeEntity.getBusinessId());
            result.setBusinessCode(requisitionApplicationChangeEntity.getBusinessCode());
            result.setApproveStatus(requisitionApplicationChangeEntity.getApproveStatus());
            result.setApproveStatusName(ApproveStatusEnum.getName(requisitionApplicationChangeEntity.getApproveStatus()));
        }
        if(SourceTypeEnum.DELIVERY_PLAN.getCode().equals(requisitionApplicationEntity.getSourceType())){
            result.setDeliveryPlanCode(requisitionApplicationEntity.getSourceCode());
        }
        result.setType(requisitionApplicationEntity.getType());
        result.setTypeName(RequisitionApplicationTypeEnum.getName(requisitionApplicationEntity.getType()));
        result.setChannelId(requisitionApplicationEntity.getChannelId());
        result.setChannelName(requisitionApplicationEntity.getChannelName());
        result.setRequisitionWarehouseId(requisitionApplicationEntity.getRequisitionWarehouseId());
        result.setRequisitionWarehouseName(requisitionApplicationEntity.getRequisitionWarehouseName());
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(RequisitionApplicationChangeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
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
        this.lambdaUpdate().eq(RequisitionApplicationChangeEntity::getId, id)
            .set(RequisitionApplicationChangeEntity::getApproveUserId, userInfo.getUid())
            .set(RequisitionApplicationChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(RequisitionApplicationChangeEntity::getApproveStatus, approveStatus)
            .set(RequisitionApplicationChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new RequisitionApplicationChangeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(RequisitionApplicationChangeEntity::getId, id)
            .set(RequisitionApplicationChangeEntity::getApproveUserId, "")
            .set(RequisitionApplicationChangeEntity::getApproveUserName, "")
            .set(RequisitionApplicationChangeEntity::getApproveStatus, approveStatus)
            .set(RequisitionApplicationChangeEntity::getApproveTime, null)
            .update(new RequisitionApplicationChangeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(RequisitionApplicationChangeEntity::getId, id)
        .set(RequisitionApplicationChangeEntity::getApproveStatus, approveStatus)
        .update(new RequisitionApplicationChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<RequisitionApplicationChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        List<String> ids = list.stream().map(RequisitionApplicationChangeDTO.ListDTO::getId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), obj));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(listApiResult.getMsg());
            }
        }

        List<String> skuIds = list.stream().map(RequisitionApplicationChangeDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        // 属性赋值
        for(RequisitionApplicationChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setChangeTypeName(RequisitionChangeTypeEnum.getName(data.getChangeType()));
            //最新待审核人
            if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if(StringUtils.isNotBlank(curApprove)){
                    data.setApproveUserName(curApprove);
                }
            }
            SkuVO skuVO = skuVOList.stream().filter(v->v.getSkuId().equals(data.getSkuId())).findFirst().orElse(new SkuVO());
            data.setProductName(skuVO.getSkuName());
            if(data.getOriginRequisitionQty().equals(0)){
                data.setOriginRequisitionQty(null);
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(RequisitionApplicationChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationChangeEntity requisitionApplicationChangeEntity, RequisitionApplicationChangeDTO.ViewDTO dto) {
        requisitionApplicationChangeEntity.setSourceCode(dto.getSourceCode());
        requisitionApplicationChangeEntity.setSourceId(dto.getSourceId());
        requisitionApplicationChangeEntity.setBusinessId(dto.getBusinessId());
        requisitionApplicationChangeEntity.setBusinessCode(dto.getBusinessCode());
        requisitionApplicationChangeEntity.setSourceType(dto.getSourceType());
    }


    private void checkExist(List<String> businessDetailIds, String noticeId, String id){
        if(CollectionUtils.isEmpty(businessDetailIds) || StringUtils.isBlank(noticeId)){
            return;
        }
        List<RequisitionApplicationChangeEntity> exist = this.lambdaQuery().ne(StringUtils.isNotBlank(id),RequisitionApplicationChangeEntity::getId,id).eq(RequisitionApplicationChangeEntity::getBusinessId, noticeId).ne(RequisitionApplicationChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()).eq(RequisitionApplicationChangeEntity::getInvalidStatus,false).list();
        if(CollectionUtils.isEmpty(exist)){
            return;
        }
        List<String> mainIds = exist.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<RequisitionApplicationChangeDetailEntity> allExistDetailList = detailService.lambdaQuery().in(RequisitionApplicationChangeDetailEntity::getMainId,mainIds).in(RequisitionApplicationChangeDetailEntity::getBusinessDetailId,businessDetailIds).list();
        if(CollectionUtils.isNotEmpty(allExistDetailList)){
            List<String> skuList = allExistDetailList.stream().map(RequisitionApplicationChangeDetailEntity::getSkuNo).collect(Collectors.toList());
            throw new ServiceException("{}存在未审核且未作废变更单，请勿重复提交",skuList);
        }
    }
}
