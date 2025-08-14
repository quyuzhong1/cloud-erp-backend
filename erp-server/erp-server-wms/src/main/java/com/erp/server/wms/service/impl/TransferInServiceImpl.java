package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.SearchType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.PutawayStatusEnum;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeTransferInService;
import com.erp.server.wms.mapper.TransferInMapper;
import com.erp.server.wms.service.*;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_IN;

/**
 * <p>
 * 分布式调入单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class TransferInServiceImpl extends SuperServiceImpl<TransferInMapper, TransferInEntity> implements TransferInService {


    @Resource
    private TransferOutDetailService transferOutDetailService;

    @Resource
    private TransferInDetailService transferInDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private AbstractWdtService abstractWdtService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SyncKingdeeTransferInService syncKingdeeTransferInService;

    @Resource
    private TransferOutService transferOutService;

    @Resource
    private QcNoticeService qcNoticeService;
    @Resource
    private QcNoticeDetailService qcNoticeDetailService;

    @Override
    public List<TransferInDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<TransferInDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<TransferInDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = approveCountList.stream().mapToInt(TransferInDTO.ApproveCountDTO::getCount).sum();
        TransferInDTO.TabListDTO all = new TransferInDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(SearchType.ALL);
        resultList.add(all);
        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        TransferInDTO.TabListDTO waitApprove = new TransferInDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setTabFlag(ing);
        waitApprove.setTabFlagName(ApproveStatusEnum.APPROVE_ING.getName());
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        TransferInDTO.TabListDTO approve = new TransferInDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setTabFlag(approveStatus);
        approve.setTabFlagName(ApproveStatusEnum.APPROVE.getName());
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        TransferInDTO.TabListDTO reject = new TransferInDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setTabFlag(rejectStatus);
        reject.setTabFlagName(ApproveStatusEnum.REJECT.getName());
        resultList.add(reject);
        return resultList;
    }

    /**
     * 下推单据保存
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-26 11:33
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTransferIn(ValidList<TransferInDTO.ViewGenerateTransferInDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        Map<String, List<TransferInDTO.ViewGenerateTransferInDTO>> map = list.stream().collect(Collectors.groupingBy(TransferInDTO.ViewGenerateTransferInDTO::getSourceId));
        List<TransferInDTO.AddDTO> addList = new ArrayList<>(map.size());
        //这个是调出详情id
        List<String> outDetailIds = list.stream().map(TransferInDTO.ViewGenerateTransferInDTO::getSourceDetailId).collect(Collectors.toList());
        //调出单详情
        List<TransferOutDetailEntity> outDetailList = CollectionUtils.isNotEmpty(outDetailIds) ? transferOutDetailService.listByIds(outDetailIds) : Collections.emptyList();
        String sourceType = SourceTypeEnum.TRANSFER_OUT.getCode();
        List<String> inWarehouseIds = list.stream().map(TransferInDTO.ViewGenerateTransferInDTO::getInWarehouseId).collect(Collectors.toList());
        List<String> outWarehouseIds = list.stream().map(TransferInDTO.ViewGenerateTransferInDTO::getOutWarehouseId).collect(Collectors.toList());
        inWarehouseIds.addAll(outWarehouseIds);
        List<WarehouseEntity> warehouseList = CollectionUtils.isNotEmpty(inWarehouseIds) ? warehouseService.listByIds(inWarehouseIds) : Collections.emptyList();
        for (Map.Entry<String, List<TransferInDTO.ViewGenerateTransferInDTO>> entry : map.entrySet()) {
            //来源id
            String sourceId = entry.getKey();
            List<TransferInDTO.ViewGenerateTransferInDTO> generateInfoList = entry.getValue();
            TransferInDTO.ViewGenerateTransferInDTO viewGenerate = generateInfoList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getSourceCode())).findFirst().orElse(null);
            if (viewGenerate != null) {
                TransferInDTO.AddDTO addDTO = new TransferInDTO.AddDTO();
                String inWarehouseId = viewGenerate.getInWarehouseId();
                addDTO.setInWarehouseId(inWarehouseId);
                String inWarehouseName = warehouseList.stream().filter(w -> w.getId().equals(inWarehouseId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                addDTO.setInWarehouseName(inWarehouseName);
                String outWarehouseId = viewGenerate.getOutWarehouseId();
                addDTO.setOutWarehouseId(outWarehouseId);
                String outWarehouseName = warehouseList.stream().filter(w -> w.getId().equals(outWarehouseId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                addDTO.setOutWarehouseName(outWarehouseName);
                addDTO.setSourceCode(viewGenerate.getSourceCode());
                addDTO.setSourceId(sourceId);
                addDTO.setSourceType(sourceType);
                addDTO.setTransferDirection(viewGenerate.getTransferDirection());
                addDTO.setTransferType(viewGenerate.getTransferType());
                addDTO.setBillDate(LocalDate.now());
                List<TransferInDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (TransferInDTO.ViewGenerateTransferInDTO item : generateInfoList) {
                    TransferInDetailDTO.AddDTO detail = new TransferInDetailDTO.AddDTO();
                    detail.setOutWarehouseLocation(item.getOutWarehouseLocation());
                    detail.setInWarehouseLocation(item.getInWarehouseLocation());
                    detail.setSkuId(item.getSkuId());
                    detail.setSourceDetailId(item.getSourceDetailId());
                    detail.setSkuNo(item.getSkuNo());
                    Integer planQty = item.getPlanQty();
                    String sourceDetailId = item.getSourceDetailId();
                    Integer outQty = outDetailList.stream().filter(o -> o.getId().equals(sourceDetailId)).findFirst().
                            flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(0);
                    if (planQty > outQty) {
                        throw new ServiceException(ApiError.ERROR_99065);
                    }
                    detail.setQty(planQty);
                    detail.setPlanQty(planQty);
                    detail.setRemark(item.getRemark());
                    detail.setTransitDamageQty(0);
                    detailList.add(detail);
                }
                addDTO.setDetailList(detailList);
                addList.add(addDTO);
            }
        }

        return this.batchAdd(addList);
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.TransferInDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-26 16:04
     */
    @Override
    public PagingVO<TransferInDTO.PagingViewDTO> paging(PagingDTO<TransferInDTO.PagingParamDTO> dto) {
        TransferInDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //根据搜索类型获取到审核状态
        IPage pageData = baseMapper.paging(query, params);
        List<TransferInDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> skuIdList = list.stream().map(TransferInDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (TransferInDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            TransferDirectionEnum transferDirection = item.getTransferDirection();
            item.setTransferDirectionName(transferDirection.getName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
        }
        return new PagingVO<>(pageData);
    }

    /**
     * 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-26 16:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<TransferInEntity> list = this.listByIds(ids);
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(rejectStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveInfo(list, ApproveStatusEnum.APPROVE_ING, "");
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", BillApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.TRANSFER_IN.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }

    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-26 16:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto, TransferInEntity entity) {
        if (!CharSequenceUtil.equals(ApproveStatusEnum.APPROVE_ING.getStatus(),entity.getApproveStatus().getCode())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        //调用审核流程
        approveProcess(entity, dto);
        // 操作日志
        String msg = format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "分步式调入单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_IN.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"审核成功");
    }

   /**
    * 流程审核
    * @author will
    * @date 2025/4/22 15:18
    * @param entity
    * @param dto
    * @return void
    */
    private void approveProcess(TransferInEntity entity , ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.TRANSFER_IN.getCode());
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

    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025/5/27 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(TransferInEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<TransferInDetailEntity> detailList = transferInDetailService.lambdaQuery().eq(TransferInDetailEntity::getMainId,entity.getId()).list();
        if (CollUtil.isNotEmpty(detailList)) {
            variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        }
        return variablesMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, TransferInEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.FALSE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        LoginUser user = UserContext.getDefaultLoginUser();
        Boolean result = this.updateApproveInfo(Arrays.asList(entity), approveStatus,user.getUserName());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (dto.getType().equals(ApproveType.PASS)) {
            handleData(entity);
            //如果来源是质检通知单的，则回填质检通知单的上架数量和上架状态
            this.updateQcNoticePutaway(entity,Boolean.TRUE);
        }
        return Boolean.TRUE;
    }

    //如果来源是质检通知单的，则回填质检通知单的上架数量和上架状态
    private void updateQcNoticePutaway(TransferInEntity transferInEntity, Boolean approve) {
        LocalDateTime nowTime = LocalDateTime.now();
        //调出单
        TransferOutEntity transferOutEntity = transferOutService.getById(transferInEntity.getSourceId());
        //调出单来源是质检通知单
        if(Objects.equals(transferOutEntity.getSourceType(),SourceTypeEnum.QC_NOTICE.getCode())){
            //调入单明细
            List<TransferInDetailEntity> transferInDetailList = transferInDetailService.lambdaQuery().in(TransferInDetailEntity::getMainId, transferInEntity.getId()).list();
            //调出单明细
            List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listByMainId(transferOutEntity.getId());
            Map<String, TransferOutDetailEntity> transferOutDetailMap = transferOutDetailList.stream().collect(Collectors.toMap(TransferOutDetailEntity::getId, t -> t,(o1,o2)->o1));
            //质检通知单
            QcNoticeEntity qcNotice = qcNoticeService.getById(transferOutEntity.getSourceId());
            //质检通知单明细
            List<QcNoticeDetailEntity> qcNoticeDetailList = qcNoticeDetailService.listByMainIds(Collections.singletonList(qcNotice.getId()));
            Map<String, QcNoticeDetailEntity> qcNoticeDetailMap = qcNoticeDetailList.stream().collect(Collectors.toMap(QcNoticeDetailEntity::getId, t -> t, (o1, o2) -> o1));

            for (TransferInDetailEntity transferInDetailEntity : transferInDetailList) {
                String sourceDetailId = transferInDetailEntity.getSourceDetailId();
                //本次上架数量
                Integer qty = transferInDetailEntity.getQty();
                if(transferOutDetailMap.containsKey(sourceDetailId)){
                    TransferOutDetailEntity transferOutDetailEntity = transferOutDetailMap.get(sourceDetailId);
                    //质检通知单明细id
                    String sourceDetailId1 = transferOutDetailEntity.getSourceDetailId();
                    if(qcNoticeDetailMap.containsKey(sourceDetailId1)){
                        QcNoticeDetailEntity qcNoticeDetailEntity = qcNoticeDetailMap.get(sourceDetailId1);
                        Integer putawayQty = qcNoticeDetailEntity.getPutawayQty();

                        if(approve){
                            //上架数量
                            qcNoticeDetailEntity.setPutawayQty(putawayQty+qty);
                            //上架时间
                            qcNoticeDetailEntity.setPutawayDate(nowTime);
                            //上架状态
                            if(qcNoticeDetailEntity.getPutawayQty().equals(qcNoticeDetailEntity.getQcQty()) ){
                                qcNoticeDetailEntity.setPutawayStatus(PutawayStatusEnum.FINISH.getCode());
                            }else {
                                qcNoticeDetailEntity.setPutawayStatus(PutawayStatusEnum.PART.getCode());
                            }
                        }else {
                            //上架数量
                            qcNoticeDetailEntity.setPutawayQty(putawayQty-qty);
                            //上架状态
                            if(qcNoticeDetailEntity.getPutawayQty().equals(0) ){
                                qcNoticeDetailEntity.setPutawayStatus(PutawayStatusEnum.WAIT.getCode());
                            }else {
                                qcNoticeDetailEntity.setPutawayStatus(PutawayStatusEnum.PART.getCode());
                            }
                        }
                        qcNoticeDetailService.updateById(qcNoticeDetailEntity);
                    }
                }
            }
        }
    }

    /**
     * 方法说明
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-05-29 14:35
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleData(TransferInEntity entity) {
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.TRANSFER_IN.getCode());
        List<InOutStockDTO> members = baseMapper.listInventoryInOut(Collections.singletonList(entity.getId()));
        InventorySourceTypeEnum transferIn = InventorySourceTypeEnum.TRANSFER_IN;
        members.stream().forEach(m -> m.setSourceType(transferIn));
        if (CollectionUtils.isNotEmpty(members)) {
            inventoryInOutStockDTO.setParamList(members);
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
        //推送旺店通
        syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
        //推送金蝶
        syncApproveInfoToKingdee(entity,SyncOperateEnum.OPERATE_APPROVE);
    }

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-26 19:00
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<TransferInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.TRANSFER_IN.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        Boolean result = this.updateApproveInfo(list, ApproveStatusEnum.WAIT_SUBMIT, "");
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("分布式调入单【%s】取消流程", ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "取消流程操作");
        return result;

    }

    /**
     * 删除分布是调入单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-26 19:05
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails) {
        List<TransferInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        List<TransferInEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (TransferInEntity entity : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) || entity.getInvalidStatus()){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(TransferInEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        Boolean result = this.removeByIds(removeIdList);
        if (result) {
            //添加日志
            String content = "删除分布式调入单[%s]";
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "删除");
            //删除明细
            transferInDetailService.removeByMainIdList(removeIdList);
            //推送金蝶
            removeList.forEach(obj -> syncApproveInfoToKingdee(obj,SyncOperateEnum.OPERATE_DELETE));
        }else {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }
        
        // 返回成功结果
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        List<TransferInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除分布式调入单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "删除");
            //删除明细
            transferInDetailService.removeByMainIdList(ids);
            //推送金蝶
            list.forEach(obj -> syncApproveInfoToKingdee(obj,SyncOperateEnum.OPERATE_DELETE));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deleteEntity(TransferInEntity entity) {
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        if (!entity.getApproveStatus().getStatus().equals(waitSubmitStatus) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除分布式调入单[%s]";
            List<Pair<String, String>> pairList = Collections.singletonList(new Pair<>(entity.getId(), entity.getCode()));
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "删除");
            //删除明细
            transferInDetailService.removeByMainIdList(ids);
            //推送金蝶
            syncApproveInfoToKingdee(entity, SyncOperateEnum.OPERATE_DELETE);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "删除失败");
        }
    }

    @Override
    public Map<String, TransferInEntity> mapByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<TransferInEntity> list = this.listByIds(ids);
        return list.stream().collect(Collectors.toMap(TransferInEntity::getId, Function.identity()));
    }

    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 9:47
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(TransferInEntity entity) {
        List<String> ids = Collections.singletonList(entity.getId());
        List<TransferInEntity> list = Collections.singletonList(entity);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveInfo(list, waitSubmitStatus, "");
        //反审核
        if (result) {
            InventoryBatchUnApproveDTO batchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.TRANSFER_IN, ids);
            inventoryTransCoreService.batchUnApprove(batchUnApproveDTO);
            //推送旺店通
            syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
            //推送金蝶
            syncApproveInfoToKingdee(entity,SyncOperateEnum.OPERATE_DISAPPROVE);
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), rejectPairList, "状态变更");
            //如果来源是质检通知单的，则回填质检通知单的上架数量和上架状态
            this.updateQcNoticePutaway(entity,Boolean.FALSE);

        }
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    /**
     * 作废
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 9:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<TransferInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        String waitSubmitStatus = BillApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String draftStatus = BillApproveStatusEnum.DRAFT.getStatus();
        String rejectStatus = BillApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(3);
        statusList.add(waitSubmitStatus);
        statusList.add(draftStatus);
        statusList.add(rejectStatus);
        long invalidCount = list.stream().filter(d -> !d.getInvalidStatus()).count();
        if (invalidCount != list.size()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        lambdaUpdate().in(TransferInEntity::getId, ids).
                set(TransferInEntity::getInvalidStatus, Boolean.TRUE).update();
        //推送金蝶
        list.forEach(obj -> syncApproveInfoToKingdee(obj,SyncOperateEnum.OPERATE_INVALID));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售订单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "作废");
        return Boolean.TRUE;

    }


    /**
     * 导出数据
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 10:04
     */
    @Override
    public Boolean exportExcel(TransferInDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("分布式调入订单列表", EXPORT_WMS_TRANSFER_IN.getCode(), dto);
        return Boolean.TRUE;

    }

    /**
     * 分布是调入详情
     *
     * @param id
     * @return com.erp.model.wms.dto.TransferInDTO.ViewDTO
     * @author yl
     * @date 2023-05-29 10:52
     */
    @Override
    public TransferInDTO.ViewDTO view(String id) {
        TransferInEntity transferIn = this.getById(id);
        if (Objects.isNull(transferIn)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        TransferInDTO.ViewDTO viewDTO = new TransferInDTO.ViewDTO();
        BeanMapper.copy(transferIn, viewDTO);
        ApproveStatusEnum approveStatusEnum = transferIn.getApproveStatus();
        viewDTO.setApproveStatusName(approveStatusEnum.getName());
        String inWarehouseId = transferIn.getInWarehouseId();
        String outWarehouseId = transferIn.getOutWarehouseId();
        List<String> warehouseIds = Arrays.asList(inWarehouseId, outWarehouseId);
        List<WarehouseEntity> warehouseList = CollectionUtils.isNotEmpty(warehouseIds) ? warehouseService.listByIds(warehouseIds) : Collections.emptyList();
        //调入仓库
        WarehouseEntity inWarehouse = warehouseList.stream().filter(w -> w.getId().equals(inWarehouseId)).
                findFirst().orElse(null);
        List<String> orgIdList = new ArrayList<>(2);
        String inOrgId = "";
        if (inWarehouse != null) {
            inOrgId = inWarehouse.getOrgId();
        }
        viewDTO.setInOrgId(inOrgId);
        //调出仓库
        WarehouseEntity outWarehouse = warehouseList.stream().filter(w -> w.getId().equals(outWarehouseId)).
                findFirst().orElse(null);
        String outOrgId = "";
        if (outWarehouse != null) {
            outOrgId = outWarehouse.getOrgId();
        }
        viewDTO.setOutOrgId(outOrgId);
        orgIdList.add(outOrgId);
        orgIdList.add(inOrgId);
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        String finalOutOrgId = outOrgId;
        String outOrgName = orgList.stream().filter(o -> o.getId().equals(finalOutOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setOutOrgName(outOrgName);
        String finalInOrgId = inOrgId;
        String inOrgName = orgList.stream().filter(o -> o.getId().equals(finalInOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setInOrgName(inOrgName);
        List<TransferInDetailDTO.ViewDTO> detailList = transferInDetailService.listByMainId(id);
        //增加仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList1 = detailList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(inWarehouseId, obj.getInWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList2 = detailList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(outWarehouseId, obj.getOutWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = Stream.concat(paramList1.stream(), paramList2.stream()).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        detailList.forEach(viewDTO1 -> {
            WarehouseLocationEntity inWarehouseLocation = warehouseLocationEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(inWarehouseId) && inWarehouseId.equals(e.getWarehouseId()) && viewDTO1.getInWarehouseLocation().equals(e.getCode())).findFirst().orElse(new WarehouseLocationEntity());
            viewDTO1.setInWarehouseLocationName(inWarehouseLocation.getName());
            WarehouseLocationEntity outWarehouseLocation = warehouseLocationEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(outWarehouseId) && outWarehouseId.equals(e.getWarehouseId()) && viewDTO1.getOutWarehouseLocation().equals(e.getCode())).findFirst().orElse(new WarehouseLocationEntity());
            viewDTO1.setOutWarehouseLocationName(outWarehouseLocation.getName());
        });
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }

    /**
     * 更改分布式调入单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-29 14:02
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateTransferIn(TransferInDTO.UpdateDTO dto) {
        String id = dto.getId();
        TransferInEntity transferIn = this.getById(id);
        if (Objects.isNull(transferIn)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        String outWarehouseId = dto.getOutWarehouseId();
        if (!transferIn.getOutWarehouseId().equals(outWarehouseId)) {
            throw new ServiceException(ApiError.ERROR_99067);
        }
        String code = transferIn.getCode();
        //详情
        List<TransferInDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        transferInDetailService.checkQty(detailList);
        //旧的
        TransferInEntity old = new TransferInEntity();
        BeanMapper.copy(transferIn, old);
        BeanMapper.copy(dto, transferIn);
        transferIn.setCode(code);
        String inWarehouseId = dto.getInWarehouseId();
        WarehouseDTO.UpdateDTO warehouse = warehouseService.detailWithCache(inWarehouseId);
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (CharSequenceUtil.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                transferIn.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        transferIn.setInWarehouseName(warehouse.getName());
        Boolean updateResult = this.updateById(transferIn);
        if (updateResult) {
            operateLogService.addModuleOperateLogByObj(old, transferIn, ModuleTypeEnum.TRANSFER_IN.getCode(), id, "", "");
            transferInDetailService.updateDetailList(id, dto.getDetailList());
            return id;
        }

        return "";
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 14:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(TransferInDTO.UpdateDTO dto) {
        String id = this.updateTransferIn(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(id));
    }

    @Override
    public List<TransferInEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery()
                .in(TransferInEntity::getSourceId, sourceIds)
                .eq(TransferInEntity::getInvalidStatus, Boolean.FALSE)
                .list();
    }

    @Override
    public PagingVO<TransferInDTO.PagingViewDTO> exportTransferIn(PagingDTO<TransferInDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        //获取导出数据
        Page<TransferInDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        List<String> skuIdList = page.getRecords().stream().map(TransferInDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (TransferInDTO.PagingViewDTO item : page.getRecords()) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            TransferDirectionEnum transferDirection = item.getTransferDirection();
            item.setTransferDirectionName(transferDirection.getName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
        }
        return new PagingVO<>(page);
    }

    @Override
    public Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(TransferInEntity::getId,businessId)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId),TransferInEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateApproveInfo(List<TransferInEntity> list, ApproveStatusEnum approveStatus, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (TransferInEntity item : list) {
                item.setApproveStatus(approveStatus);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return Boolean.TRUE;
    }


    /**
     * 批量添加数据
     *
     * @param addList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<TransferInDTO.AddDTO> addList) {
        if (CollectionUtils.isEmpty(addList)) {
            return Boolean.FALSE;
        }
        addList.forEach(obj -> add(obj));
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public String add(TransferInDTO.AddDTO dto) {
        String id = IdWorker.getIdStr();
        TransferInEntity transferIn = new TransferInEntity();
        BeanMapper.copy(dto, transferIn);
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (CharSequenceUtil.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                transferIn.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        transferIn.setId(id);
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FBDR, BusinessNoTypeEnum.CODE_FBDR.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FBDR);
        transferIn.setCode(code);
        Boolean addResult = this.save(transferIn);
        //添加成功
        if (addResult) {
            transferInDetailService.add(id, dto.getDetailList());
            //添加日志
            String content = String.format("新增了一个{%s}-分布式调入单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), id, "新增操作");
            return id;
        }
        return "";
    }


    /**
     * 分步式调入单推审核送旺店通
     * @author will
     * @date 2025/4/22 14:21
     * @param entity
     * @param syncOperateEnum
     * @return void
     */
    private void syncApproveInfoToWdt(TransferInEntity entity, SyncOperateEnum syncOperateEnum) {
        //查询分步式调入单明细数据
        List<TransferInDetailDTO.ViewDTO> transferDetailList = transferInDetailService.listByMainId(entity.getId());
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getInWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        //审核转其他入库单
        List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
        for (TransferInDetailDTO.ViewDTO detailEntity : transferDetailList) {
            CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
            inGoods.setSpecNo(detailEntity.getSkuNo());
            inGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
            inGoods.setPositionNo(CharSequenceUtil.isNotBlank(detailEntity.getInWarehouseLocation()) ? detailEntity.getInWarehouseLocation() : "");
            inGoods.setWarehouseId(entity.getInWarehouseId());
            inGoods.setRemark(detailEntity.getRemark());
            inGoodsList.add(inGoods);
        }
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
    }

    /**
     * 分步式调入单推反审核送旺店通
     * @author will
     * @date 2025/4/22 14:24
     * @param entity
     * @param syncOperateEnum
     * @return void
     */
    private void syncDisApproveInfoToWdt(TransferInEntity entity, SyncOperateEnum syncOperateEnum) {
        //查询调拨单明细数据
        List<TransferInDetailDTO.ViewDTO> transferDetailList = transferInDetailService.listByMainId(entity.getId());
        if(transferDetailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getInWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        //每个调入仓转换为一个其他出库单
        List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
        for (TransferInDetailDTO.ViewDTO detailEntity : transferDetailList) {
            CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
            outGoods.setSpecNo(detailEntity.getSkuNo());
            outGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
            outGoods.setPositionNo(CharSequenceUtil.isNotBlank(detailEntity.getInWarehouseLocation()) ? detailEntity.getInWarehouseLocation() : "");
            outGoods.setWarehouseId(entity.getInWarehouseId());
            outGoods.setRemark(detailEntity.getRemark());
            outGoodsList.add(outGoods);
        }
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), outGoodsList, SourceTypeEnum.OTHER_OUTSTOCK);
    }

    /**
     * 推送金蝶
     * @author will
     * @date 2025/4/22 16:34
     * @param entity
     * @param syncOperateEnum
     * @return void
     */
    private void syncApproveInfoToKingdee(TransferInEntity entity, SyncOperateEnum syncOperateEnum) {

        //直接调拨单明细
        List<TransferInDetailEntity> transferInDetailList = transferInDetailService.listByMainIdList(Collections.singletonList(entity.getId()));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();
        transferInDetailList = CollUtil.isNotEmpty(transferInDetailList) ? transferInDetailList.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
        //删除或者非服务sku不为空时推金蝶
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(syncOperateEnum.getCode()) || CollUtil.isNotEmpty(transferInDetailList)){
            syncKingdeeTransferInService.syncDataToKingdee(entity,transferInDetailList, syncOperateEnum.getCode());
        }
    }

    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }

}
