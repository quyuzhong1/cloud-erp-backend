package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单出库单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class SoOutstockServiceImpl extends SuperServiceImpl<SoOutstockMapper, SoOutstockEntity> implements SoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Resource
    private CommonService commonService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;

    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoOutstockEntity::getSourceId, ids).list();
    }

    @Override
    public List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds) {
        return lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoOutstockEntity::getSoId, soIds).list();
    }

    /**
     * 添加销售出库单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-19 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoOutstockDTO.AddDTO dto) {
        //TODO 对应检查数量
        String id = IdWorker.getIdStr();
        //来源类型
        String sourceType = dto.getSourceType();
        if (StringUtils.isBlank(sourceType)) {
            sourceType = SourceTypeEnum.SELF_ADD.getCode();
        }
        String sourceId = dto.getSourceId();
        List<SoOutstockDetailDTO.AddDTO> detailList = dto.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }
        //检查出库数量
        List<SoOutstockDetailDTO.UpdateDTO> checkList = BeanMapper.copyList(detailList, SoOutstockDetailDTO.UpdateDTO.class);
        soOutstockDetailService.checkOutQty(dto.getWarehouseId(), dto.getSoId(), sourceId, sourceType, checkList);
        //销售订单
        String soId = dto.getSoId();
        SoInfoEntity soInfo = soInfoFeign.getSoInfoById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setSoCode(soInfo.getCode());
        soOutstock.setCustomerId(soInfo.getCustomerId());
        soOutstock.setId(id);
        soOutstock.setOrderType(soInfo.getOrderType());
        List<SoOutstockDetailDTO.AddDTO> addDetailList = dto.getDetailList();
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSCK, BusinessNoTypeEnum.CODE_XSCK.getCode()));
        soOutstock.setCode(code);

        soOutstock.setWarehouseOrgId(soInfo.getWarehouseOrgId());
        soOutstock.setWarehouseOrgName(soInfo.getWarehouseOrgName());
        //仓库id
        String warehouseId = dto.getWarehouseId();
        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                soOutstock.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        Boolean addResult = this.save(soOutstock);
        //添加成功
        if (addResult) {
            soOutstockDetailService.add(id, addDetailList);
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "新增操作");
            return id;
        }

        return "";
    }

    /**
     * 批量提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:34
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoOutstockEntity> list = this.listByIds(ids);
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
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
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus), "");
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
        }
        return result;
    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:42
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SoOutstockDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 销售出库单详情
     *
     * @param id
     * @return com.erp.model.wms.dto.SoOutstockDTO.ViewDTO
     * @author yl
     * @date 2023-05-19 10:45
     */
    @Override
    public SoOutstockDTO.ViewDTO view(String id) {
        SoOutstockEntity soOutstock = this.getById(id);
        if (Objects.isNull(soOutstock)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        SoOutstockDTO.ViewDTO result = new SoOutstockDTO.ViewDTO();
        BeanMapper.copy(soOutstock, result);
        ApproveStatusEnum approveStatus = soOutstock.getApproveStatus();
        result.setApproveStatusName(approveStatus.getName());
        String soId = soOutstock.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
        if (soInfo != null) {
            result.setCustomerName(soInfo.getCustomerName());
            result.setSoCode(soInfo.getCode());
            result.setReceiveAddress(soInfo.getReceiveAddress());
            result.setReceiverName(soInfo.getReceiverName());
            result.setDeliveryModeName(soInfo.getDeliveryModeName());
            result.setRequireDate(soInfo.getRequireDate());
            result.setTelNumber(soInfo.getTelNumber());
            result.setTypeName(soInfo.getOrderTypeName());
            result.setSellerName(soInfo.getSellerName());
            result.setSalesDeptId(soInfo.getSalesDeptId());
            result.setSalesDeptName(soInfo.getSalesDeptName());
            result.setSalesOrgName(soInfo.getSalesOrgName());

        }
        List<SoOutstockDetailDTO.ViewDTO> detailList = soOutstockDetailService.listByMainId(id, soOutstock.getWarehouseId());
        result.setDetailList(detailList);
        return result;
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 11:42
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        String content = "";
        String userName = commonService.getUserInfo().getUserName();
        Boolean isPass = dto.getType().equals(ApproveType.PASS);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.APPROVE;
        //TODO 需要做什么 释放冻结 销售订单的发货状态
        if (isPass) {
            //审核通过
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
            //审核通过发送金蝶
            list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));

        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        Boolean result = this.updateApproveStatus(list, approveStatus, userName);
        if (result) {
            //处理对应数据
            if (isPass) {
                handleData(list);
            }
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
        }

        return result;
    }


    /**
     * 处理数据
     * 需要更改发货状态
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-05-22 20:01
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleData(List<SoOutstockEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //销售订单的id
        List<String> soIds = list.stream().map(SoOutstockEntity::getSoId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(soIds);
        List<String> soDetailIdList = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<String> allList = list.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        //发货通知单的 id
        List<SoOutstockEntity> noticeSoOutstockList = list.stream().filter(s -> s.getSourceType().equals(soDeliveryNotice)).
                collect(Collectors.toList());

        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();
        for (SoDeliveryNoticeEntity item : noticeList) {
            String deliveryNoticeId = item.getId();
            SoOutstockEntity noticeSoOutstock = noticeSoOutstockList.stream().filter(o -> o.getSourceId().equals(deliveryNoticeId)).findFirst().orElse(null);
            if (noticeSoOutstock != null) {
                //更新打包时间
                item.setPackDate(noticeSoOutstock.getPackDate());
                item.setActualDeliveryDate(noticeSoOutstock.getActualDeliveryDate());
            }
            item.setDeliveryStatus(Boolean.TRUE);
        }
        //更改打包日期 以及发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);

        //这个是销售订单的 这个要统计 存在多个
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockDetailService.listDetailBySoDetailIds(soDetailIdList);
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        soOutstockDetailList = soOutstockDetailList.stream().filter(s -> s.getApproveStatus().equals(approveStatus)).collect(Collectors.toList());
        //分组
        Map<String, List<SoOutstockDetailDTO.DeliveryQtyDTO>> map = soOutstockDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailDTO.DeliveryQtyDTO::getSoDetailId));
        List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<SoOutstockDetailDTO.DeliveryQtyDTO>> entry : map.entrySet()) {
            SoDetailDTO.UpdateDeliveryStatusDTO param = new SoDetailDTO.UpdateDeliveryStatusDTO();
            String soDetailId = entry.getKey();
            param.setId(soDetailId);
            //已发货数量
            Integer alreadyDeliveryQty = entry.getValue().stream().mapToInt(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).sum();
            param.setAlreadyDeliveryQty(alreadyDeliveryQty);
            paramList.add(param);

        }
        soInfoFeign.updateDeliveryStatus(paramList);
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        List<InOutStockDTO> members = baseMapper.listInventoryInOut(allList);
        for (InOutStockDTO member : members) {
            member.setSourceType(InventorySourceTypeEnum.SO_OUTSTOCK);
            member.setBillDate(LocalDate.now());
        }
        if (CollectionUtils.isNotEmpty(members)) {
            inventoryInOutStockDTO.setMembers(members);
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
    }


    /**
     * 处理反审核的数据
     *
     * @param list
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleDisApproveData(List<SoOutstockEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //销售订单的id
        List<String> soIds = list.stream().map(SoOutstockEntity::getSoId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(soIds);
        List<String> soDetailIdList = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        //发货通知单的
        List<SoOutstockEntity> noticeSoOutstockList = list.stream().filter(s -> s.getSourceType().equals(soDeliveryNotice)).
                collect(Collectors.toList());

        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();
        for (SoDeliveryNoticeEntity item : noticeList) {
            item.setDeliveryStatus(Boolean.FALSE);
        }
        //更改发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);

        //这个是销售订单的 这个要统计 存在多个
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockDetailService.listDetailBySoDetailIds(soDetailIdList);
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //分组
        Map<String, List<SoOutstockDetailDTO.DeliveryQtyDTO>> map = soOutstockDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailDTO.DeliveryQtyDTO::getSoDetailId));
        List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<SoOutstockDetailDTO.DeliveryQtyDTO>> entry : map.entrySet()) {
            SoDetailDTO.UpdateDeliveryStatusDTO param = new SoDetailDTO.UpdateDeliveryStatusDTO();
            String soDetailId = entry.getKey();
            param.setId(soDetailId);
            //已发货数量
            Integer alreadyDeliveryQty = entry.getValue().stream().filter(s -> s.getApproveStatus().equals(approveStatus)).
                    mapToInt(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).sum();
            param.setAlreadyDeliveryQty(alreadyDeliveryQty);
            paramList.add(param);

        }
        soInfoFeign.updateDeliveryStatus(paramList);

    }


    /**
     * 反审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:10
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
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

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        //反审核 TODO 需要做什么
        if (result) {
            //反审核
            InventoryBatchUnApproveDTO batchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, ids);
            inventoryTransCoreService.batchUnApprove(batchUnApproveDTO);
            handleDisApproveData(list);
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");

            //审核通过发送金蝶
            list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode()));
        }
        return result;
    }

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:13
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销流程
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("销售出库单【%s】取消流程", ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "取消流程操作");
        return result;

    }


    /**
     * 删除销售出库单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
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
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "删除");
            //删除明细
            soOutstockDetailService.removeByMainIdList(ids);

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
     * @date 2023-05-19 14:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(waitSubmitStatus);
        statusList.add(rejectStatus);
        long invalidCount = list.stream().filter(d -> !d.getInvalidStatus()).count();
        if (invalidCount != list.size()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        lambdaUpdate().in(SoOutstockEntity::getId, ids).
                set(SoOutstockEntity::getInvalidStatus, Boolean.TRUE).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售出库单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "作废");

        //作废发送金蝶
        list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_INVALID.getCode()));
        return Boolean.TRUE;

    }


    /**
     * 获取tab
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.TabListDTO>
     * @author yl
     * @date 2023-05-19 14:23
     */
    @Override
    public List<SoOutstockDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<SoOutstockDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<SoOutstockDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = approveCountList.stream().mapToInt(SoOutstockDTO.ApproveCountDTO::getCount).sum();
        SoOutstockDTO.TabListDTO all = new SoOutstockDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);


        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoOutstockDTO.TabListDTO waitApprove = new SoOutstockDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        SoOutstockDTO.TabListDTO approve = new SoOutstockDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoOutstockDTO.TabListDTO reject = new SoOutstockDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
        resultList.add(reject);
        return resultList;


    }

    /**
     * 分页列表
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-22 8:56
     */
    @Override
    public PagingVO<SoOutstockDTO.PagingViewDTO> paging(PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        SoOutstockDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        String searchType = params.getSearchType();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        IPage pageData = baseMapper.paging(query, params, approveList);
        List<SoOutstockDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> soIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSoId).collect(Collectors.toList());
        List<SoInfoDTO.CustomerDTO> soCustomerList = soInfoFeign.listSoCustomer(soIdList);
        //sku id
        List<String> skuIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        List<String> flagList = new ArrayList<>();
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        for (SoOutstockDTO.PagingViewDTO item : list) {
            boolean contains = flagList.contains(item.getId());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            String sourceType = item.getSourceType();
            String sourceCode = soDeliveryNotice.equals(sourceType) ? item.getSourceCode() : "";
            item.setSourceCode(sourceCode);
            SoInfoDTO.CustomerDTO soInfo = soCustomerList.stream().filter(s -> s.getId().equals(soId)).findFirst().orElse(new SoInfoDTO.CustomerDTO());
            item.setOrderTypeName(soInfo.getOrderTypeName());
            item.setSalesOrgName(soInfo.getSalesOrgName());
            item.setCustomerName(soInfo.getCustomerName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
            if (contains) {
                item.setCode("");
                item.setSoCode("");
                item.setSourceCode("");
                item.setOrderType("");
                item.setOrderTypeName("");
                item.setApproveStatusName("");
                item.setCustomerName("");
                item.setWarehouseOrgName("");
                item.setSalesOrgName("");
                item.setPlanDeliveryDate(null);
                item.setPackDate(null);
                item.setActualDeliveryDate(null);
                item.setCreateUserName("");
                item.setCreateTime(null);
            }
            flagList.add(item.getId());
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出销售出库单
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-22 11:41
     */
    @Override
    public Boolean exportExcel(SoOutstockDTO.ExportDTO dto, HttpServletResponse response) {
        String searchType = dto.getSearchType();
        List<String> approveList = listBySearchType(searchType);
        //获取导出数据
        List<SoOutstockDTO.PagingViewDTO> list = baseMapper.listExport(dto, approveList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }

        List<String> soIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSoId).collect(Collectors.toList());
        List<SoInfoDTO.CustomerDTO> soCustomerList = soInfoFeign.listSoCustomer(soIdList);
        //sku id
        List<String> skuIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        for (SoOutstockDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            String sourceType = item.getSourceType();
            String sourceCode = soDeliveryNotice.equals(sourceType) ? item.getSourceCode() : "";
            item.setSourceCode(sourceCode);
            SoInfoDTO.CustomerDTO soInfo = soCustomerList.stream().filter(s -> s.getId().equals(soId)).findFirst().orElse(new SoInfoDTO.CustomerDTO());
            item.setOrderTypeName(soInfo.getOrderTypeName());
            item.setSalesOrgName(soInfo.getSalesOrgName());
            item.setCustomerName(soInfo.getCustomerName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soOutstock.xlsx";
        String name = "销售订单出库列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售订单出库导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * 修改
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-22 18:00
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSoOutstock(SoOutstockDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoOutstockEntity soOutstock = this.getById(id);
        if (Objects.isNull(soOutstock)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        List<SoOutstockDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        String sourceType = soOutstock.getSourceType();
        if (StringUtils.isBlank(sourceType)) {
            sourceType = SourceTypeEnum.SELF_ADD.getCode();
        }
        //检查出库数量
        soOutstockDetailService.checkOutQty(dto.getWarehouseId(), dto.getSoId(), dto.getSourceId(), sourceType, detailList);
        String code = soOutstock.getCode();
        //旧的
        SoOutstockEntity old = new SoOutstockEntity();
        BeanMapper.copy(soOutstock, old);

        String soId = dto.getSoId();
        SoInfoEntity soInfo = soInfoFeign.getSoInfoById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setCode(code);
        soOutstock.setSoCode(soInfo.getCode());
        soOutstock.setCustomerId(soInfo.getCustomerId());
        soOutstock.setOrderType(soInfo.getOrderType());
        soOutstock.setWarehouseOrgId(soInfo.getWarehouseOrgId());
        soOutstock.setWarehouseOrgName(soInfo.getWarehouseOrgName());

        //仓库id
        String warehouseId = dto.getWarehouseId();
        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                soOutstock.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        Boolean updateResult = this.updateById(soOutstock);
        if (updateResult) {
            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, soOutstock, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "", "");
            soOutstockDetailService.updateDetail(id, detailList);
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
     * @date 2023-05-22 19:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(SoOutstockDTO.UpdateDTO dto) {
        String id = this.updateSoOutstock(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }


    /**
     * 销售出库单保存下推单据
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-23 14:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addPushDownNo(List<SoOutstockDTO.GenerateSoOutstockViewDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        Map<String, List<SoOutstockDTO.GenerateSoOutstockViewDTO>> map = list.stream().collect(Collectors.groupingBy(SoOutstockDTO.GenerateSoOutstockViewDTO::getSourceId));
        List<SoOutstockDTO.AddDTO> addList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<SoOutstockDTO.GenerateSoOutstockViewDTO>> entry : map.entrySet()) {
            //来源id
            String sourceId = entry.getKey();
            List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateInfoList = entry.getValue();
            SoOutstockDTO.GenerateSoOutstockViewDTO generateInfo = generateInfoList.stream().filter(g -> StringUtils.isNotBlank(g.getSourceCode())).findFirst().orElse(null);
            if (generateInfo != null) {
                SoOutstockDTO.AddDTO add = new SoOutstockDTO.AddDTO();
                add.setSoId(generateInfo.getSoId());
                add.setSourceId(generateInfo.getSourceId());
                add.setSourceCode(generateInfo.getSourceCode());
                add.setSourceType(generateInfo.getSourceType());
                add.setCarrierId(generateInfo.getCarrierId());
                add.setPlanDeliveryDate(generateInfo.getPlanDeliveryDate());
                add.setWarehouseId(generateInfo.getWarehouseId());
                add.setTrackNo(generateInfo.getTrackNo());
                List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (SoOutstockDTO.GenerateSoOutstockViewDTO item : generateInfoList) {
                    SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
                    detail.setSourceDetailId(item.getSourceDetailId());
                    detail.setSkuId(item.getSkuId());
                    detail.setRemark(item.getRemark());
                    detail.setWarehouseLocation(item.getWarehouseLocation());
                    detail.setActualQty(item.getDeliveryQty());
                    detail.setPlanQty(item.getDeliveryQty());
                    detail.setAttachNameList(item.getAttachNameList());
                    detail.setAttachUrlList(item.getAttachUrlList());
                    detailList.add(detail);
                }
                add.setDetailList(detailList);
                addList.add(add);
            }

        }
        return this.batchAdd(addList);
    }


    /**
     * 销售订单获取销售出库单的数据
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.SoRefDTO>
     * @author yl
     * @date 2023-05-23 18:37
     */
    @Override
    public List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(String soId) {
        SoInfoDTO.CustomerDTO soCustomer = soInfoFeign.getSoBaseById(soId);
        List<SoOutstockDTO.SoRefDTO> resultList = baseMapper.listSoRefSoOutstockBySoId(soId);
        List<String> skuIdList = resultList.stream().map(SoOutstockDTO.SoRefDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        for (SoOutstockDTO.SoRefDTO item : resultList) {
            String skuId = item.getSkuId();
            LocalDate actualDeliveryDate = item.getActualDeliveryDate();
            item.setOutStockDate(actualDeliveryDate);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            item.setOrderType(soCustomer.getOrderType());
            item.setOrderTypeName(soCustomer.getOrderTypeName());
            item.setSalesOrgName(soCustomer.getSalesOrgName());
            item.setCustomerId(soCustomer.getCustomerId());
            item.setCustomerName(soCustomer.getCustomerName());
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            String productName = "";
            String unit = "";
            if (sku != null) {
                productName = sku.getSkuName();
                unit = sku.getUnitName();
            }
            item.setProductName(productName);
            item.setUnit(unit);
        }
        return resultList;
    }


    /**
     * 保存销售订单下推销售出库单
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoSave(ValidList<SoInfoDTO.GenerateDeliveryView> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        Map<String, List<SoInfoDTO.GenerateDeliveryView>> map = list.stream().collect(Collectors.groupingBy(SoInfoDTO.GenerateDeliveryView::getSoId));
        List<SoOutstockDTO.AddDTO> addList = new ArrayList<>(map.size());
        String sourceType = SourceTypeEnum.SO_INFO.getCode();
        for (Map.Entry<String, List<SoInfoDTO.GenerateDeliveryView>> entry : map.entrySet()) {
            //来源id
            String soId = entry.getKey();
            List<SoInfoDTO.GenerateDeliveryView> generateInfoList = entry.getValue();
            SoInfoDTO.GenerateDeliveryView generateInfo = generateInfoList.stream().filter(g -> StringUtils.isNotBlank(g.getSoId())).findFirst().orElse(null);
            if (generateInfo != null) {
                SoOutstockDTO.AddDTO add = new SoOutstockDTO.AddDTO();
                add.setSoId(soId);
                add.setSourceId(soId);
                add.setSourceCode(generateInfo.getSoCode());
                add.setSourceType(sourceType);
                add.setPlanDeliveryDate(generateInfo.getPlanDeliveryDate());
                add.setWarehouseId(generateInfo.getWarehouseId());
                List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (SoInfoDTO.GenerateDeliveryView item : generateInfoList) {
                    SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
                    detail.setSourceDetailId(item.getDetailId());
                    detail.setSkuId(item.getSkuId());
                    detail.setRemark(item.getRemark());
                    detail.setActualQty(item.getDeliveryQty());
                    detail.setPlanQty(item.getDeliveryQty());
                    detail.setAttachNameList(item.getAttachmentNameList());
                    detail.setAttachUrlList(item.getAttachmentUrlList());
                    detail.setWarehouseLocation("");
                    detailList.add(detail);
                }
                add.setDetailList(detailList);
                addList.add(add);
            }
        }
        return this.batchAdd(addList);
    }

    /**
     * @return
     * @parms
     * @author yl
     * @date
     */
    @Override
    public Integer getPushDownCountBySoIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return 0;
        }
        Integer count = this.lambdaQuery().in(SoOutstockEntity::getSoId, soIds).
                eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE).
                count();
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<SoOutstockDTO.AddDTO> addList) {
        if (CollectionUtils.isEmpty(addList)) {
            return Boolean.FALSE;
        }
        addList.forEach(obj -> add(obj));
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
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return
     */
    private Boolean updateApproveStatus(List<SoOutstockEntity> list, ApproveStatusEnum statusEnum, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoOutstockEntity item : list) {
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return true;
    }

    /**
     * 修改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     **/
    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String syncOperate) {
        return this.lambdaUpdate()
                .eq(SoOutstockEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), SoOutstockEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), SoOutstockEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId), SoOutstockEntity::getSyncKingdeeId, syncKingdeeId)
                .set(StringUtils.isNotBlank(syncOperate), SoOutstockEntity::getSyncOperate, syncOperate)
                .update();
    }

}
