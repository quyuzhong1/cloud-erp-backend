package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.TransferInEntity;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransferInMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private CommonService commonService;

    @Resource
    private WarehouseService warehouseService;


    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Override
    public List<TransferInDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<TransferInDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<TransferInDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = approveCountList.stream().mapToInt(TransferInDTO.ApproveCountDTO::getCount).sum();
        TransferInDTO.TabListDTO all = new TransferInDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);
        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        TransferInDTO.TabListDTO waitApprove = new TransferInDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        TransferInDTO.TabListDTO approve = new TransferInDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        TransferInDTO.TabListDTO reject = new TransferInDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
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
            TransferInDTO.ViewGenerateTransferInDTO viewGenerate = generateInfoList.stream().filter(g -> StringUtils.isNotBlank(g.getSourceCode())).findFirst().orElse(null);
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
        String searchType = params.getSearchType();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        IPage pageData = baseMapper.paging(query, params, approveList);
        List<TransferInDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> skuIdList = list.stream().map(TransferInDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        List<String> flagList = new ArrayList<>();
        for (TransferInDTO.PagingViewDTO item : list) {
            boolean contains = flagList.contains(item.getId());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            TransferDirectionEnum transferDirection = item.getTransferDirection();
            item.setTransferDirectionName(transferDirection.getName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
            if (contains) {
                item.setId("");
                item.setCode("");
                item.setSourceCode("");
                item.setTransferDirectionName("");
                item.setApproveStatusName("");
                item.setOutWarehouseName("");
                item.setInWarehouseName("");
                item.setCreateUserName("");
                item.setCreateTime(null);
                item.setApproveUserName("");
            }
            flagList.add(item.getId());
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
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.APPROVE_ING);
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
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<TransferInEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        LoginUser user = commonService.getUserInfo();
        if (dto.getType().equals(ApproveType.PASS)) {
            handleData(ids);
            //审核通
            result = this.updateApproveInfo(list, ApproveStatusEnum.APPROVE, user.getUserName());
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            result = this.updateApproveInfo(list, ApproveStatusEnum.REJECT, user.getUserName());
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "状态变更");
        }
        return result;

    }

    /**
     * 方法说明
     *
     * @param idList
     * @return void
     * @author yl
     * @date 2023-05-29 14:35
     */
    public void handleData(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.STEP_INVENTORY_IN.getCode());
        List<InOutStockDTO> members = baseMapper.listInventoryInOut(idList);
        InventorySourceTypeEnum transferIn = InventorySourceTypeEnum.TRANSFER_IN;
        members.stream().forEach(m -> m.setSourceType(transferIn));
        if (CollectionUtils.isNotEmpty(members)) {
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }

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
        //TODO 撤销流程
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.WAIT_SUBMIT);
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

        }
        return result;
    }

    /**
     * 反审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 9:47
     */
    @Override
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<TransferInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99066);
        }
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveIngStatus);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveIngStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, waitSubmitStatus);
        //反审核
        if (result) {
            InventoryBatchUnApproveDTO batchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.TRANSFER_IN, ids);
            inventoryTransCoreService.batchUnApprove(batchUnApproveDTO);


            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.TRANSFER_IN.getCode(), pairList, "状态变更");
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), rejectPairList, "状态变更");
        }
        return result;
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
            throw new ServiceException(ApiError.ERROR_98061);
        }
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        lambdaUpdate().in(TransferInEntity::getId, ids).
                set(TransferInEntity::getInvalidStatus, Boolean.TRUE).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售订单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "作废");
        return Boolean.TRUE;

    }


    /**
     * 导出数据
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 10:04
     */
    @Override
    public Boolean exportExcel(TransferInDTO.ExportDTO dto, HttpServletResponse response) {
        String searchType = dto.getSearchType();
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        //获取导出数据
        List<TransferInDTO.PagingViewDTO> list = baseMapper.listExport(dto, approveList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        List<String> skuIdList = list.stream().map(TransferInDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (TransferInDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            TransferDirectionEnum transferDirection = item.getTransferDirection();
            item.setTransferDirectionName(transferDirection.getName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/transferIn.xlsx";
        String name = "分布式调入订单列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("分布式调入列表导出出错 {}", e);
            return Boolean.FALSE;
        }
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
        //旧的
        TransferInEntity old = new TransferInEntity();
        BeanMapper.copy(transferIn, old);
        BeanMapper.copy(dto, transferIn);
        transferIn.setCode(code);
        String inWarehouseId = dto.getInWarehouseId();
        WarehouseDTO.UpdateDTO warehouse = warehouseService.detailWithCache(inWarehouseId);
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
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
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public List<TransferInEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery()
                .in(TransferInEntity::getSourceId,sourceIds)
                .eq(TransferInEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    private Boolean updateApproveInfo(List<TransferInEntity> list, ApproveStatusEnum approveStatus, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (TransferInEntity item : list) {
                item.setApproveStatus(approveStatus);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return Boolean.TRUE;
    }

    private Boolean updateApproveStatus(List<TransferInEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (TransferInEntity item : list) {
                item.setApproveStatus(statusEnum);
            }
            return this.updateBatchById(list);
        }
        return Boolean.TRUE;
    }

    private List<String> listBySearchType(String searchType) {
        List<String> approveList = new ArrayList<>(3);
        //待审核
        if (SearchType.WAIT_APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }

        //已审核
        if (ApproveStatusEnum.APPROVE.getStatus().equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE.getStatus());
        }

        //审核不通过
        if (ApproveStatusEnum.REJECT.getStatus().equals(searchType)) {
            approveList.add(ApproveStatusEnum.REJECT.getStatus());
        }
        return approveList;
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
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                transferIn.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        transferIn.setId(id);
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FBDR, BusinessNoTypeEnum.CODE_FBDR.getCode()));
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
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }

}
