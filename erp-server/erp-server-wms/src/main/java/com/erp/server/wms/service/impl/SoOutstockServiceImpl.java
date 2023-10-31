package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
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
    private CustomerFeign customerFeign;

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

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private RedisService redisService;

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

    public List<SoOutstockEntity> listDbBySoIds(@RequestBody List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoOutstockEntity::getSoId, soIds).list();
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
        //销售订单详情集合
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(dto.getSoId()));
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException("销售订单详情不存在");
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
        //销售订单的含税销售金额折扣前
        BigDecimal soAmount = BigDecimal.ZERO;
        for (SoDetailEntity soDetail : soDetailList) {
            BigDecimal taxAmountBefore = soDetail.getTaxAmountBefore();
            soAmount = soAmount.add(Objects.isNull(taxAmountBefore) ? BigDecimal.ZERO : taxAmountBefore);
        }
        //出库金额
        BigDecimal outStockAmount = BigDecimal.ZERO;
        //通知单详情
        List<String> noticeDetailIdList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getSourceDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = CollectionUtils.isNotEmpty(noticeDetailIdList) ? soDeliveryNoticeDetailService.listByIds(noticeDetailIdList) : Collections.emptyList();
        for (SoOutstockDetailDTO.AddDTO item : detailList) {
            //sku id
            String skuId = item.getSkuId();
            //实发数量
            Integer actualQty = item.getActualQty();
            String sourceDetailId = item.getSourceDetailId();
            String soDetailId = soDeliveryNoticeDetailEntityList.stream().filter(d -> d.getId().equals(sourceDetailId)).
                    map(SoDeliveryNoticeDetailEntity::getSourceDetailId).findFirst().orElse("");

            BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getPrice).orElse(BigDecimal.ZERO);
            //税率
            BigDecimal taxRate = soDetailList.stream().filter(s ->  s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getTaxRate).orElse(BigDecimal.ZERO);

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);

            outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
        }
        //销售订单折扣额
        BigDecimal discountAmount = soInfo.getDiscountAmount();
        //折扣总额占比
        BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
        //整单折扣额
        BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);

        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setSoCode(soInfo.getCode());
        soOutstock.setCustomerId(soInfo.getCustomerId());
        soOutstock.setId(id);
        soOutstock.setTotalDiscountAmount(totalDiscountAmount);
        soOutstock.setOrderType(soInfo.getOrderType());
        List<SoOutstockDetailDTO.AddDTO> addDetailList = dto.getDetailList();
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSCK, BusinessNoTypeEnum.CODE_XSCK.getCode()));
        soOutstock.setCode(code);
        soOutstock.setSalesDeptId(soInfo.getSalesDeptId());
        soOutstock.setWarehouseOrgId(soInfo.getWarehouseOrgId());
        soOutstock.setWarehouseOrgName(soInfo.getWarehouseOrgName());
        //仓库id
        String warehouseId = dto.getWarehouseId();

        String warehouseKeeperId = dto.getWarehouseKeeperId();
        //用户信息
        if (StringUtils.isNotBlank(warehouseKeeperId) || StringUtils.isNotBlank(dto.getSellerId())) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId, dto.getSellerId()));

            if (CollectionUtils.isNotEmpty(userList)) {
                //仓管员
                String warehouseKeeperName = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setWarehouseKeeperName(warehouseKeeperName);

                //销售员
                String sellerName = userList.stream().filter(obj -> obj.getUserId().equals(dto.getSellerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setSellerName(sellerName);
            }
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        // 出库日期
        soOutstock.setBillDate(LocalDate.now());
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
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus), "", null);
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
        List<CustomerInfoEntity> customerList = customerFeign.listCustomerByIds(Arrays.asList(soOutstock.getCustomerId()));

        //国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        if (CollectionUtils.isNotEmpty(countryList) && CollectionUtils.isNotEmpty(customerList)) {
            String countryName = countryList.stream().filter(obj -> obj.getId().equals(customerList.get(0).getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            result.setCountryId(customerList.get(0).getCountryId());
            result.setCountryName(countryName);
        }

        if (StringUtils.isNotBlank(result.getCarrierId())) {
            //获取采购单供应商信息
            SupplierEntity supplierById = scmTaskFeign.getSupplierById(result.getCarrierId());
            result.setCarrierName(supplierById.getName());
        }

        String soId = soOutstock.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
        if (soInfo != null) {
            result.setCustomerName(soInfo.getCustomerName());
            result.setSoCode(soInfo.getCode());
            result.setSoRemark(soInfo.getSoRemark());
            result.setReceiveAddress(soInfo.getReceiveAddress());
            result.setReceiverName(soInfo.getReceiverName());
            result.setDeliveryModeName(soInfo.getDeliveryModeName());
            result.setRequireDate(soInfo.getRequireDate());
            result.setTelNumber(soInfo.getTelNumber());
            result.setTypeName(soInfo.getOrderTypeName());
            result.setSellerId(soInfo.getSellerId());
            result.setSellerName(soInfo.getSellerName());
            result.setSalesDeptId(soInfo.getSalesDeptId());
            result.setSalesDeptName(soInfo.getSalesDeptName());
            result.setSalesOrgName(soInfo.getSalesOrgName());
        }
        List<SoOutstockDetailDTO.ViewDTO> detailList = soOutstockDetailService.listByMainId(id, soOutstock.getWarehouseId());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(soOutstock.getWarehouseId()));
        for (SoOutstockDetailDTO.ViewDTO viewDTO : detailList) {
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(viewDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
            list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));

        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        Boolean result = this.updateApproveStatus(list, approveStatus, userName, LocalDateTime.now());
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

        //这个是销售出库单id
        List<String> allList = list.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        //发货通知单的 id
        List<SoOutstockEntity> noticeSoOutstockList = list.stream().filter(s -> s.getSourceType().equals(soDeliveryNotice)).
                collect(Collectors.toList());
        //发货通知单的 id
        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();

        //这个是所有的销售订单管理的发货通知单id
        List<SoDeliveryNoticeEntity> allDeliveryNoticeList = soDeliveryNoticeService.listBySourceIdList(soIds);
        List<String> allDeliveryNoticeIds = allDeliveryNoticeList.stream().map(SoDeliveryNoticeEntity::getId).collect(Collectors.toList());
        //发货通知单详情
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(allDeliveryNoticeIds);
        List<String> deliveryNoticeDetailIdList = soDeliveryNoticeDetailList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
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
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockDetailService.listDetailBySoDetailIds(deliveryNoticeDetailIdList);
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
        }
        if (CollectionUtils.isNotEmpty(members)) {
            inventoryInOutStockDTO.setParamList(members);
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
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        //发货通知单的
        List<SoOutstockEntity> noticeSoOutstockList = list.stream().filter(s -> s.getSourceType().equals(soDeliveryNotice)).
                collect(Collectors.toList());

        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();

        //这个是所有的销售订单管理的发货通知单id
        List<SoDeliveryNoticeEntity> allDeliveryNoticeList = soDeliveryNoticeService.listBySourceIdList(soIds);
        List<String> allDeliveryNoticeIds = allDeliveryNoticeList.stream().map(SoDeliveryNoticeEntity::getId).collect(Collectors.toList());
        //发货通知单详情
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(allDeliveryNoticeIds);
        List<String> deliveryNoticeDetailIdList = soDeliveryNoticeDetailList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        for (SoDeliveryNoticeEntity item : noticeList) {
            item.setDeliveryStatus(Boolean.FALSE);
        }
        //更改发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);

        //这个是销售订单的 这个要统计 存在多个
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockDetailService.listDetailBySoDetailIds(deliveryNoticeDetailIdList);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto, Boolean isPushKingDee) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
        //审核通过
        //待提交
        if (!isPushKingDee) {
            list = list.stream().filter(x -> ApproveStatusEnum.APPROVE.equals(x.getApproveStatus())).collect(Collectors.toList());
        } else {
            long count = list.stream().filter(s -> !ApproveStatusEnum.APPROVE.equals(s.getApproveStatus())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_98014);
            }
        }
        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> ApproveStatusEnum.APPROVE.equals(s.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.WAIT_SUBMIT, "", null);
        //反审核
        if (result) {
            //反审核
            InventoryBatchUnApproveDTO batchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, ids);
            inventoryTransCoreService.batchUnApprove(batchUnApproveDTO);
            handleDisApproveData(list);
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), rejectPairList, "状态变更");
            if (isPushKingDee) {
                //审核通过发送金蝶
                list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
            }
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
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "", null);
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
            //审核通过发送金蝶
            list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
        list.forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_INVALID.getCode()));
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

        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        SoOutstockDTO.TabListDTO waitSubmit = new SoOutstockDTO.TabListDTO();
        int waitSubmitCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(waitSubmitStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitSubmit.setCount(waitSubmitCount);
        waitSubmit.setSearchType(SearchType.WAIT_SUBMIT);
        resultList.add(waitSubmit);

        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoOutstockDTO.TabListDTO waitApprove = new SoOutstockDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        SoOutstockDTO.TabListDTO approve = new SoOutstockDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoOutstockDTO.TabListDTO reject = new SoOutstockDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(rejectStatus)).findFirst().
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
        params.setNeSourceType(SourceTypeEnum.SAL_OUTSTOCK.getCode());
        params.setPermissionSql(dto.getPermissionSql());
        String searchType = params.getSearchType();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        if (CollectionUtils.isNotEmpty(approveList)) {
            params.setInvalidStatus(Boolean.FALSE);
        }
        //处理国家数据
        handleCountryIdList(params);
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

        //客户信息
        List<String> customerIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = customerFeign.listCustomerByIds(customerIdList);
        //部门id集合
        List<String> deptIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSalesDeptId).collect(Collectors.toList());
        List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(deptIdList);

        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        List<String> flagList = new ArrayList<>();
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();

        //销售明细
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(soIdList);

        //销售订单下所有通知单
        List<String> soDetailIdList = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIdList);
        //销售出库单
        List<String> noticeDetailIdList = soDeliveryNoticeDetailList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutStockDetailList = soOutstockDetailService.listDetailBySourceDetailId(noticeDetailIdList);

        for (SoOutstockDTO.PagingViewDTO item : list) {
            boolean contains = flagList.contains(item.getId());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            //部门id
            String salesDeptId = item.getSalesDeptId();
            String salesDeptName = deptList.stream().filter(d -> d.getId().equals(salesDeptId)).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(salesDeptName);
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


            //销售通知单明细
            String sourceDetailId = soDeliveryNoticeDetailList.stream().filter(obj -> obj.getId().equals(item.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSourceDetailId())).orElse("");
            //销售订单明细 (sku对应)
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(sourceDetailId)).findFirst().orElse(null);

            //同一个销售明细已下推出库数量合计
            List<String> thisNoticeDetailIdList = soDeliveryNoticeDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(sourceDetailId)).map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
            Integer totalQty = soOutStockDetailList.stream().filter(obj -> thisNoticeDetailIdList.contains(obj.getSourceDetailId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);

            BigDecimal price = soDetailEntity.getPrice();
            item.setPrice(price);
            item.setCnyPrice(MathUtil.multiply(price, soDetailEntity.getExchangeRate()));
            item.setTaxPrice(MathUtil.multiply(price, MathUtil.add(MathUtil.BigDecimal_100, soDetailEntity.getTaxRate())).divide(MathUtil.BigDecimal_100));
            item.setCnyTaxPrice(MathUtil.multiply(item.getTaxPrice(), soDetailEntity.getExchangeRate()));
            //单SKU价税合计(本位币)=SKU的价税合计(本位币)*(出库数量/销售订单数量)
            //最后一笔价税合计(本位币)=总价税合计(本位币)-价税合计SKU累计(本位币)

            BigDecimal cnyTaxAmount = MathUtil.divide(MathUtil.multiply(soDetailEntity.getAllAmountLocalCurrency(), item.getActualQty())
                    , MathUtil.valueOf(soDetailEntity.getQty().toString())).setScale(2, BigDecimal.ROUND_DOWN);
            //判断销售明细数量是否下推完
            if (MathUtil.compareTo(soDetailEntity.getQty(), totalQty) == MathUtil.ZERO) {
                String detailId = soOutStockDetailList.stream().filter(obj -> thisNoticeDetailIdList.contains(obj.getSourceDetailId()))
                        .max(Comparator.comparing(SoOutstockDetailEntity::getId)).map(SoOutstockDetailEntity::getId).orElse("");
                if (item.getDetailId().equals(detailId)) {
                    BigDecimal otherAmount = soOutStockDetailList.stream().filter(obj -> !obj.getId().equals(item.getDetailId()) && thisNoticeDetailIdList.contains(obj.getSourceDetailId())).map(obj -> MathUtil.divide(MathUtil.multiply(soDetailEntity.getAllAmountLocalCurrency(), obj.getActualQty()),
                            MathUtil.valueOf(soDetailEntity.getQty().toString())).setScale(2, BigDecimal.ROUND_DOWN)).reduce(BigDecimal.ZERO, BigDecimal::add);
                    cnyTaxAmount = MathUtil.subtract(soDetailEntity.getAllAmountLocalCurrency(), otherAmount);
                }
            }
            item.setCnyTaxAmount(cnyTaxAmount);

            //国家id
            if (CollectionUtils.isNotEmpty(customerList)) {
                String countryId = customerList.stream().filter(obj -> obj.getId().equals(item.getCustomerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCountryId())).orElse("");
                item.setCountryId(countryId);
            }

            //国家名称
            if (CollectionUtils.isNotEmpty(countryList)) {
                String countryName = countryList.stream().filter(obj -> obj.getId().equals(item.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                item.setCountryName(countryName);
            }
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
                item.setBillDate(null);
                item.setCreateUserName("");
                item.setCreateTime(null);
                item.setCustomerOrderNo("");
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
        dto.setNeSourceType(SourceTypeEnum.SAL_OUTSTOCK.getCode());
        //处理国家数据
        handleCountryIdList(dto);
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
        //部门id集合
        List<String> deptIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSalesDeptId).collect(Collectors.toList());
        List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(deptIdList);
        //客户信息
        List<String> customerIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = customerFeign.listCustomerByIds(customerIdList);
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();

        //销售明细
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(soIdList);

        //销售订单下所有通知单
        List<String> soDetailIdList = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIdList);
        //销售出库单
        List<String> noticeDetailIdList = soDeliveryNoticeDetailList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutStockDetailList = soOutstockDetailService.listDetailBySourceDetailId(noticeDetailIdList);

        for (SoOutstockDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            String sourceType = item.getSourceType();
            String sourceCode = soDeliveryNotice.equals(sourceType) ? item.getSourceCode() : "";
            //部门id
            String salesDeptId = item.getSalesDeptId();
            String salesDeptName = deptList.stream().filter(d -> d.getId().equals(salesDeptId)).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(salesDeptName);
            item.setSourceCode(sourceCode);
            SoInfoDTO.CustomerDTO soInfo = soCustomerList.stream().filter(s -> s.getId().equals(soId)).findFirst().orElse(new SoInfoDTO.CustomerDTO());
            item.setOrderTypeName(soInfo.getOrderTypeName());
            item.setSalesOrgName(soInfo.getSalesOrgName());
            item.setCustomerName(soInfo.getCustomerName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);

            //销售通知单明细
            String sourceDetailId = soDeliveryNoticeDetailList.stream().filter(obj -> obj.getId().equals(item.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSourceDetailId())).orElse("");
            //销售订单明细 (sku对应)
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(sourceDetailId)).findFirst().orElse(null);

            //同一个销售明细已下推出库数量合计
            List<String> thisNoticeDetailIdList = soDeliveryNoticeDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(sourceDetailId)).map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
            Integer totalQty = soOutStockDetailList.stream().filter(obj -> thisNoticeDetailIdList.contains(obj.getSourceDetailId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);

            BigDecimal price = soDetailEntity.getPrice();
            item.setPrice(price);
            item.setCnyPrice(MathUtil.multiply(price, soDetailEntity.getExchangeRate()));
            item.setTaxPrice(MathUtil.multiply(price, MathUtil.add(MathUtil.BigDecimal_100, soDetailEntity.getTaxRate())).divide(MathUtil.BigDecimal_100));
            item.setCnyTaxPrice(MathUtil.multiply(item.getTaxPrice(), soDetailEntity.getExchangeRate()));
            //单SKU价税合计(本位币)=SKU的价税合计(本位币)*(出库数量/销售订单数量)
            //最后一笔价税合计(本位币)=总价税合计(本位币)-价税合计SKU累计(本位币)

            BigDecimal cnyTaxAmount = MathUtil.divide(MathUtil.multiply(soDetailEntity.getAllAmountLocalCurrency(), item.getActualQty())
                    , MathUtil.valueOf(soDetailEntity.getQty().toString())).setScale(2, BigDecimal.ROUND_DOWN);
            //判断销售明细数量是否下推完
            if (MathUtil.compareTo(soDetailEntity.getQty(), totalQty) == MathUtil.ZERO) {
                String detailId = soOutStockDetailList.stream().filter(obj -> thisNoticeDetailIdList.contains(obj.getSourceDetailId()))
                        .max(Comparator.comparing(SoOutstockDetailEntity::getId)).map(SoOutstockDetailEntity::getId).orElse("");
                if (item.getDetailId().equals(detailId)) {
                    BigDecimal otherAmount = soOutStockDetailList.stream().filter(obj -> !obj.getId().equals(item.getDetailId())).map(obj -> MathUtil.divide(MathUtil.multiply(soDetailEntity.getAllAmountLocalCurrency(), obj.getActualQty()),
                            MathUtil.valueOf(soDetailEntity.getQty().toString())).setScale(2, BigDecimal.ROUND_DOWN)).reduce(BigDecimal.ZERO, BigDecimal::add);
                    cnyTaxAmount = MathUtil.subtract(soDetailEntity.getAllAmountLocalCurrency(), otherAmount);
                }
            }
            item.setCnyTaxAmount(cnyTaxAmount);

            //国家id
            if (CollectionUtils.isNotEmpty(customerList)) {
                String countryId = customerList.stream().filter(obj -> obj.getId().equals(item.getCustomerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCountryId())).orElse("");
                item.setCountryId(countryId);
            }

            //国家名称
            if (CollectionUtils.isNotEmpty(countryList)) {
                String countryName = countryList.stream().filter(obj -> obj.getId().equals(item.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                item.setCountryName(countryName);
            }
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

        //销售订单详情集合
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(dto.getSoId()));
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException("销售订单详情不存在");
        }

        //检查出库数量
        soOutstockDetailService.checkOutQty(dto.getWarehouseId(), dto.getSoId(), dto.getSourceId(), sourceType, detailList);
        String code = soOutstock.getCode();
        LocalDate billDate = dto.getBillDate();
        //旧的
        SoOutstockEntity old = new SoOutstockEntity();
        BeanMapper.copy(soOutstock, old);

        String soId = dto.getSoId();
        SoInfoEntity soInfo = soInfoFeign.getSoInfoById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }

        //销售订单的总金额
        BigDecimal soAmount = BigDecimal.ZERO;
        for (SoDetailEntity soDetail : soDetailList) {
            BigDecimal taxAmountBefore = soDetail.getTaxAmountBefore();
            soAmount = soAmount.add(Objects.isNull(taxAmountBefore) ? BigDecimal.ZERO : taxAmountBefore);
        }
        //通知单详情
        List<String> noticeDetailIdList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getSourceDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = CollectionUtils.isNotEmpty(noticeDetailIdList) ? soDeliveryNoticeDetailService.listByIds(noticeDetailIdList) : Collections.emptyList();
        //出库金额
        BigDecimal outStockAmount = BigDecimal.ZERO;
        for (SoOutstockDetailDTO.UpdateDTO item : detailList) {
            //sku id
            String skuId = item.getSkuId();
            //实发数量
            Integer actualQty = item.getActualQty();

            String sourceDetailId = item.getSourceDetailId();
            String soDetailId = soDeliveryNoticeDetailEntityList.stream().filter(d -> d.getId().equals(sourceDetailId)).
                    map(SoDeliveryNoticeDetailEntity::getSourceDetailId).findFirst().orElse("");

            BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getPrice).orElse(BigDecimal.ZERO);
            //税率
            BigDecimal taxRate = soDetailList.stream().filter(s ->  s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getTaxRate).orElse(BigDecimal.ZERO);

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);
            outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
        }
        //销售订单折扣额
        BigDecimal discountAmount = soInfo.getDiscountAmount();
        //折扣总额占比
        BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
        //整单折扣额
        BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);

        BeanMapper.copy(dto, soOutstock);
        soOutstock.setCode(code);
        soOutstock.setTotalDiscountAmount(totalDiscountAmount);
        // 出库日期
        soOutstock.setBillDate(billDate);
        soOutstock.setSoCode(soInfo.getCode());
        soOutstock.setCustomerId(soInfo.getCustomerId());
        soOutstock.setOrderType(soInfo.getOrderType());
        soOutstock.setWarehouseOrgId(soInfo.getWarehouseOrgId());
        soOutstock.setWarehouseOrgName(soInfo.getWarehouseOrgName());
        soOutstock.setSalesDeptId(soInfo.getSalesDeptId());

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
        List<String> soIdList = list.stream().map(SoOutstockDTO.GenerateSoOutstockViewDTO::getSoId).collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(soIdList);
        if (CollectionUtils.isEmpty(soInfoList)) {
            throw new ServiceException(ApiError.ERROR_92016);
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
                //客户订单号
                String customerOrderNo = soInfoList.stream().filter(obj -> obj.getId().equals(generateInfo.getSoId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCustomerOrderNo())).orElse("");

                add.setSoId(generateInfo.getSoId());
                add.setSourceId(generateInfo.getSourceId());
                add.setSourceCode(generateInfo.getSourceCode());
                add.setSourceType(generateInfo.getSourceType());
                add.setCarrierId(generateInfo.getCarrierId());
                add.setPlanDeliveryDate(generateInfo.getPlanDeliveryDate());
                add.setWarehouseId(generateInfo.getWarehouseId());
                add.setTrackNo(generateInfo.getTrackNo());
                add.setSellerId(generateInfo.getSellerId());
                add.setCustomerOrderNo(customerOrderNo);
                List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (SoOutstockDTO.GenerateSoOutstockViewDTO item : generateInfoList) {
                    SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
                    detail.setSourceDetailId(item.getSourceDetailId());
                    detail.setSkuId(item.getSkuId());
                    detail.setRemark(item.getRemark());
                    detail.setWarehouseLocation(item.getWarehouseLocation());
                    detail.setActualQty(item.getQty());
                    detail.setPlanQty(item.getQty());
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
        List<String> soIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getSoId).collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(soIdList);
        if (CollectionUtils.isEmpty(soInfoList)) {
            log.info("销售订单不存在，soIdList = {}", soInfoList);
            throw new ServiceException(ApiError.ERROR_92016);
        }

        Map<String, List<SoInfoDTO.GenerateDeliveryView>> map = list.stream().collect(Collectors.groupingBy(SoInfoDTO.GenerateDeliveryView::getSoId));
        List<SoOutstockDTO.AddDTO> addList = new ArrayList<>(map.size());
        String sourceType = SourceTypeEnum.SO_INFO.getCode();
        for (Map.Entry<String, List<SoInfoDTO.GenerateDeliveryView>> entry : map.entrySet()) {
            //来源id
            String soId = entry.getKey();
            List<SoInfoDTO.GenerateDeliveryView> generateInfoList = entry.getValue();

            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> obj.getId().equals(soId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soInfoEntity)) {
                log.info("销售订单不存在，soId = {}", soId);
                throw new ServiceException(ApiError.ERROR_92016);
            }
            SoInfoDTO.GenerateDeliveryView generateInfo = generateInfoList.stream().filter(g -> StringUtils.isNotBlank(g.getSoId())).findFirst().orElse(null);
            if (generateInfo != null) {
                SoOutstockDTO.AddDTO add = new SoOutstockDTO.AddDTO();
                add.setSoId(soId);
                add.setSourceId(soId);
                add.setSourceCode(generateInfo.getSoCode());
                add.setSourceType(sourceType);
                add.setPlanDeliveryDate(generateInfo.getPlanDeliveryDate());
                add.setWarehouseId(generateInfo.getWarehouseId());
                add.setCustomerOrderNo(soInfoEntity.getCustomerOrderNo());
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
        List<String> approveList = new ArrayList<>(4);
        // 待提交
        if (SearchType.WAIT_SUBMIT.equals(searchType)) {
            approveList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
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
    private Boolean updateApproveStatus(List<SoOutstockEntity> list, ApproveStatusEnum statusEnum, String approveUserName, LocalDateTime approveTime) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoOutstockEntity item : list) {
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
                item.setApproveTime(approveTime);
                if (approveTime != null) {
                    item.setActualDeliveryDate(approveTime.toLocalDate());
                }
            }
            return this.updateBatchById(list);
        }
        return true;
    }

    /**
     * 修改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     **/
    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SoOutstockEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SoOutstockEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    /**
     * 根据code 获取到销售出库单信息
     *
     * @param code
     * @return com.erp.model.wms.entity.SoOutstockEntity
     * @author yl
     * @date 2023-06-28 10:17
     */
    @Override
    public String getByCode(String code) {
        LambdaQueryWrapper<SoOutstockEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoOutstockEntity::getCode, code);
        queryWrapper.last("LIMIT 1");
        SoOutstockEntity entity = this.getOne(queryWrapper);
        if (!Objects.isNull(entity)) {
            String id = entity.getId();
            return id;
        }
        return "";
    }

    @Override
    public Boolean pagingUpdate(SoOutstockDTO.PagingUpdateDTO dto) {
        List<SoOutstockEntity> list = this.listByIds(dto.getIdList());
        if (ObjectUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        boolean update = lambdaUpdate().in(SoOutstockEntity::getId, dto.getIdList())
                .set(SoOutstockEntity::getTrackNo, dto.getTrackNo())
                .update();
        return update;
    }

    @Override
    public List<SoOutstockDTO.PrintDTO> print(List<String> ids) {
        List<SoOutstockDTO.PrintDTO> printDTOList = new ArrayList<>();
        List<SoOutstockEntity> soOutstockEntities = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //获取客户id集合
        List<String> customerIds = soOutstockEntities.stream().map(SoOutstockEntity::getCustomerId).distinct().collect(Collectors.toList());
        //根据客户id集合查询客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(customerIds);
        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(ids);
        //获取销售单id集合
        List<String> soList = soOutstockEntities.stream().map(SoOutstockEntity::getSoId).collect(Collectors.toList());
        //获取销售单集合
        List<SoInfoEntity> soInfoEntities = soInfoFeign.listSoInfoByIds(soList);
        //获取sku的id集合
        List<String> skuIdList = soOutstockDetailEntities.stream().map(SoOutstockDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //根据skuId查询sku信息
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //获取销售单详情id
        List<String> SoDeliveryNoticeDetailIds = soOutstockDetailEntities.stream().map(SoOutstockDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listByIds(SoDeliveryNoticeDetailIds);
        List<String> soDetailIds = noticeDetailEntities.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
        List<String> receiveAddressId = soInfoEntities.stream().map(SoInfoEntity::getReceiveAddressId).collect(Collectors.toList());
        List<CustomerAddressEntity> customerAddressEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(receiveAddressId)) {
            customerAddressEntities.addAll(customerFeign.ListCustomerAddressByIds(receiveAddressId));
        }
        for (SoOutstockEntity soOutstockEntity : soOutstockEntities) {
            //根据客户id获取客户信息
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soOutstockEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            //根据销售单id获取销售单信息
            SoInfoEntity soInfoEntity = soInfoEntities.stream().filter(req -> req.getId().equals(soOutstockEntity.getSoId())).findFirst().orElse(new SoInfoEntity());
            SoOutstockDTO.PrintDTO printDTO = new SoOutstockDTO.PrintDTO();
            printDTO.setCustomerName(customerInfoEntity.getName());
            printDTO.setSellerName(soInfoEntity.getSellerName());
            CustomerAddressEntity customerAddressEntity = customerAddressEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getReceiveAddressId())).findFirst().orElse(new CustomerAddressEntity());
            printDTO.setReceiveAddress(customerAddressEntity.getAddress());
            printDTO.setTelNumber(soInfoEntity.getTelNumber());
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailEntities.stream().filter(req -> req.getMainId().equals(soOutstockEntity.getId())).collect(Collectors.toList());
            printDTO.setSumNumber(soOutstockDetailEntityList.stream().mapToInt(SoOutstockDetailEntity::getActualQty).sum());
            soOutstockDetailEntityList.sort(Comparator.comparing(SoOutstockDetailEntity::getId));
            List<SoOutstockDTO.PrintDetailDTO> printDetailDTOList = new ArrayList<>();
            for (SoOutstockDetailEntity soOutstockDetailEntity : soOutstockDetailEntityList) {
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDeliveryNoticeDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(req -> req.getId().equals(soDeliveryNoticeDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                SoOutstockDTO.PrintDetailDTO printDetailDTO = new SoOutstockDTO.PrintDetailDTO();
                printDetailDTO.setPlatformSkuNo(soDetailEntity.getPlatformSkuNo());
                printDetailDTO.setProductSkuNo(soOutstockDetailEntity.getSkuNo());
                SkuVO skuVO = skuList.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                printDetailDTO.setProductName(skuVO.getSkuName());
                printDetailDTO.setRemark(soOutstockDetailEntity.getRemark());
                printDetailDTO.setQty(soOutstockDetailEntity.getActualQty());
                printDetailDTOList.add(printDetailDTO);
            }

            printDTO.setPrintDetailList(printDetailDTOList);
            printDTOList.add(printDTO);
        }
        return printDTOList;
    }

    @Override
    public List<String> getIdsByTemp() {
        return baseMapper.getIdsByTemp();
    }


    /**
     * 金蝶同步到系统
     *
     * @param soOutstock 销售出库单
     * @param detailList 销售出库详情
     * @param flagId     已存在的flagId
     * @return void
     * @author yl
     * @date 2023-07-21 14:44
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void handleKingdeeToErp(SoOutstockEntity soOutstock, List<SoOutstockDetailEntity> detailList, String flagId) {
        if (StringUtils.isNotBlank(flagId)) {
            //回滚库存
            InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, Arrays.asList(flagId));
            inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
            this.removeById(flagId);
            soOutstockDetailService.removeByMainIdList(Arrays.asList(flagId));
        }

        //保存销售出库单
        this.save(soOutstock);
        //保存销售出库单详情
        soOutstockDetailService.saveBatch(detailList);
    }


    /**
     * @param params
     * @description: 处理国家字段
     * @author Will
     * @date: 2023/7/24 14:01
     */
    private void handleCountryIdList(SoOutstockDTO.PagingParamDTO params) {
        if (CollectionUtils.isNotEmpty(params.getCountryIdList())) {
            List<CustomerInfoEntity> customerList = customerFeign.listByCountryIdList(params.getCountryIdList());
            if (CollectionUtils.isEmpty(customerList)) {
                return;
            }
            List<String> customerIdList = customerList.stream().map(CustomerInfoEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(params.getCustomerIdList())) {
                params.setCustomerIdList(customerIdList);
            } else {
                List<String> newCustomerIdList = customerIdList.stream().filter(obj -> params.getCustomerIdList().contains(obj)).collect(Collectors.toList());
                params.setCustomerIdList(newCustomerIdList);
            }
        }

    }

    @Override
    public PagingVO<SoOutstockDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<SoOutstockDTO.PdaPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        SoOutstockDTO.PdaPagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setActualDeliveryDateList(dateList);
        }
        IPage<SoOutstockDTO.PdaPagingViewDTO> pageData = this.baseMapper.pdaPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<SoOutstockDTO.PdaPagingViewDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(ids);
        for (SoOutstockDTO.PdaPagingViewDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<SoOutstockDetailEntity> detailEntities = soOutstockDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<SoOutstockDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, SoOutstockDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoOutstockDTO.PdaCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<SoOutstockDTO.PdaCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            SoOutstockDTO.PagingParamDTO pagingParamDTO = new SoOutstockDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            SoOutstockDTO.PdaCountDTO resultDTO = new SoOutstockDTO.PdaCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setActualDeliveryDateList(dateList);
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String pdaAdd(SoOutstockDTO.AddDTO dto) {
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainId(dto.getSourceId());
        for (SoOutstockDetailDTO.AddDTO addDTO : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(addDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_SOOUTSTOCK_DETAIL_SKU_NOT_EXIST, addDTO.getSkuNo());
            }
        }
        return this.add(dto);
    }

    @Override
    public String pdaUpdate(SoOutstockDTO.UpdateDTO dto) {
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainId(dto.getSourceId());
        for (SoOutstockDetailDTO.UpdateDTO updateDTO : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(updateDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_SOOUTSTOCK_DETAIL_SKU_NOT_EXIST, updateDTO.getSkuNo());
            }
        }
        return this.updateSoOutstock(dto);
    }


    @Override
    public Boolean pdaAddAndSubmit(SoOutstockDTO.AddDTO dto) {
        String id = this.pdaAdd(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }

    @Override
    public Boolean pdaUpdateAndSubmit(SoOutstockDTO.UpdateDTO dto) {
        String id = this.pdaUpdate(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void tempRepairHistoryDb() {
        List<SoInfoDTO.ListDTO> soList = soInfoFeign.listRepairHistoryDb();
        List<String> soIdList = soList.stream().map(SoInfoDTO.ListDTO::getSoId).collect(Collectors.toList());
        //销售出库单
        List<SoOutstockEntity> soOutstockList = this.listDbBySoIds(soIdList);
        List<String> soOutstockIdList = soOutstockList.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
        //销售出库详情
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(soOutstockIdList);

        //通知单详情
        List<String> noticeDetailIdList = soOutstockDetailList.stream().map(SoOutstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = CollectionUtils.isNotEmpty(noticeDetailIdList) ? soDeliveryNoticeDetailService.listByIds(noticeDetailIdList) : Collections.emptyList();

        for (SoOutstockEntity item : soOutstockList) {
            String soId = item.getSoId();
            String id = item.getId();
            List<SoInfoDTO.ListDTO> soDetailList = soList.stream().filter(s -> s.getSoId().equals(soId))
                    .collect(Collectors.toList());
            //销售订单的总金额
            BigDecimal soAmount = BigDecimal.ZERO;
            for (SoInfoDTO.ListDTO soDetail : soDetailList) {
                BigDecimal taxAmountBefore = soDetail.getTaxAmountBefore();
                soAmount = soAmount.add(Objects.isNull(taxAmountBefore) ? BigDecimal.ZERO : taxAmountBefore);
            }
            List<SoOutstockDetailEntity> detailList = soOutstockDetailList.stream().
                    filter(d -> d.getMainId().equals(id)).collect(Collectors.toList());
            //出库金额
            BigDecimal outStockAmount = BigDecimal.ZERO;
            for (SoOutstockDetailEntity itemDetail : detailList) {
                //sku id
                String skuId = itemDetail.getSkuId();

                String sourceDetailId = itemDetail.getSourceDetailId();
                String soDetailId = soDeliveryNoticeDetailEntityList.stream().filter(d -> d.getId().equals(sourceDetailId)).
                        map(SoDeliveryNoticeDetailEntity::getSourceDetailId).findFirst().orElse("");

                BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                        findFirst().map(SoInfoDTO.ListDTO::getPrice).orElse(BigDecimal.ZERO);
                //税率
                BigDecimal taxRate = soDetailList.stream().filter(s ->  s.getId().equals(soDetailId)).
                        findFirst().map(SoInfoDTO.ListDTO::getTaxRate).orElse(BigDecimal.ZERO);

                //实发数量
                Integer actualQty = itemDetail.getActualQty();


                BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
                BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);

                outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
            }
            //销售订单折扣额
            BigDecimal discountAmount = soDetailList.get(0).getDiscountAmount();
            //折扣总额占比
            BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
            //整单折扣额
            BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);
            item.setTotalDiscountAmount(totalDiscountAmount);

        }
        this.updateBatchById(soOutstockList);


    }

    @Override
    public List<SoOutstockEntity> listByTrackNo(String trackNo) {
        if (StringUtils.isBlank(trackNo)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE).
                like(SoOutstockEntity::getTrackNo, trackNo).list();
    }
}
