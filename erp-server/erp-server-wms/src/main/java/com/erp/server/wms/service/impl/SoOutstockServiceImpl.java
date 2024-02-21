package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.enums.TransferOutstockStatusEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.scheduling.annotation.Async;
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
    private DocNoGenHelper docNoGenHelper;


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
    private WorkflowFeign workflowFeign;


    @Resource
    private LogisticsBillFeign logisticsBillFeign;


    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;


    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoOutstockEntity::getSourceId, ids).list();
    }

    @Override
    public List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds) {
        if(CollectionUtils.isEmpty(soIds)){
            return Collections.emptyList();
        }
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

            //实发数量
            Integer actualQty = item.getActualQty();
            String sourceDetailId = item.getSourceDetailId();
            String soDetailId = soDeliveryNoticeDetailEntityList.stream().filter(d -> d.getId().equals(sourceDetailId)).
                    map(SoDeliveryNoticeDetailEntity::getSourceDetailId).findFirst().orElse("");

            BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getPrice).orElse(BigDecimal.ZERO);
            //税率
            BigDecimal taxRate = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getTaxRate).orElse(BigDecimal.ZERO);

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);

            outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
        }

        //销售订单
        String soId = dto.getSoId();
        SoInfoDTO.CustomerDTO soCustomer = soInfoFeign.getSoBaseById(soId);
        if (Objects.isNull(soCustomer)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        //销售订单折扣额
        BigDecimal discountAmount = soCustomer.getDiscountAmount();
        //折扣总额占比
        BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
        //整单折扣额
        BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);

        SoOutstockEntity soOutstock = new SoOutstockEntity();
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSCK);
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setCode(code);
        soOutstock.setId(id);
        soOutstock.setTotalDiscountAmount(totalDiscountAmount);
        soOutstock.setBillDate(LocalDate.now());
        //tob 保存数据修改
        handleSaveOrUpdateDbByB2b(soOutstock, soCustomer);
        List<SoOutstockDetailDTO.AddDTO> addDetailList = dto.getDetailList();
        Boolean addResult = this.save(soOutstock);
        //添加成功
        if (addResult) {
            soOutstockDetailService.add(id, addDetailList, soOutstock.getOrderType());
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "新增操作");
            return id;
        }

        return "";
    }

    /**
     * 处理添加或修改b2b 数据
     * @param soOutstock
     */
    private void handleSaveOrUpdateDbByB2b(SoOutstockEntity soOutstock, SoInfoDTO.CustomerDTO soCustomer ) {
        soOutstock.setSoCode(soCustomer.getCode());
        soOutstock.setCustomerId(soCustomer.getCustomerId());
        soOutstock.setCustomerName(soCustomer.getCustomerName());
        soOutstock.setOrderType(soCustomer.getOrderType());
        soOutstock.setSalesDeptId(soCustomer.getSalesDeptId());
        soOutstock.setWarehouseOrgId(soCustomer.getWarehouseOrgId());
        soOutstock.setWarehouseOrgName(soCustomer.getWarehouseOrgName());
        soOutstock.setSalesOrgId(soCustomer.getSalesOrgId());
        soOutstock.setSalesOrgName(soCustomer.getSalesOrgName());
        soOutstock.setSellerId(soCustomer.getSellerId());
        soOutstock.setCountry(soCustomer.getCountryId());
        //仓库id
        String warehouseId = soCustomer.getWarehouseId();
        String warehouseKeeperId = soOutstock.getWarehouseKeeperId();
        String sellerId = soOutstock.getSellerId();
        //用户信息
        if (StringUtils.isNotBlank(warehouseKeeperId) || StringUtils.isNotBlank(sellerId)) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId, sellerId));

            if (CollectionUtils.isNotEmpty(userList)) {
                //仓管员
                String warehouseKeeperName = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setWarehouseKeeperName(warehouseKeeperName);
                //销售员
                String sellerName = userList.stream().filter(obj -> obj.getUserId().equals(sellerId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setSellerName(sellerName);
            }
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());

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

        //查询是否冻结
        List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
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
        String orderType = soOutstock.getOrderType();
        String b2c = OrderTypeEnum.B2C.getCode();
        Boolean isB2c = b2c.equals(orderType);
        result.setTypeName(OrderTypeEnum.getName(orderType));
        result.setSoCode(soOutstock.getSoCode());
        result.setSellerId(soOutstock.getSellerId());
        if (!isB2c) {
            if (StringUtils.isNotBlank(soId)){
                SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
                if (soInfo != null) {
                    result.setCustomerName(soInfo.getCustomerName());
                    result.setSoRemark(soInfo.getSoRemark());
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
            }
        } else {
            SoB2cDTO.CustomerDTO customer = soB2cFeign.getB2cCustomerById(soId);
            result.setCustomerName(customer.getCustomerName());
            result.setReceiveAddress(customer.getReceiverAddress());
            result.setReceiverName(customer.getReceiverName());
            result.setTelNumber(customer.getTelNumber());
            result.setSellerName(customer.getSellerName());
            result.setSalesOrgName(customer.getSalesOrgName());
            result.setDeliveryModeName(customer.getDeliveryModeName());
            //要货日期通销售订单创建日期
            result.setRequireDate(soOutstock.getPlanDeliveryDate());
            result.setCountryId(customer.getCountry());
            result.setCountryName(customer.getCountryName());
            String salesDeptId = soOutstock.getSalesDeptId();
            if (StringUtils.isNotBlank(salesDeptId)) {
                SysDepartmentDTO department = sysUserFeign.getUserDeptById(salesDeptId);
                if (Objects.nonNull(department)) {
                    result.setSalesDeptName(department.getName());
                }
            }

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
    public BatchResultDTO approve(ApproveOneDTO dto) {
        SoOutstockEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());

        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        if (!ingStatus.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        // 调用流程审核
        approveProcess(entity, dto);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "销售出库单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_OUT_STOCK.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));

    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    public void approveProcess(SoOutstockEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_OUTSTOCK.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        String uid = userInfo.getUid();
        if (StringUtils.isBlank(uid)) {
            uid = "system";
        }
        approveDTO.setUserId(uid);
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            this.approveEnd(dto, entity);
        }
    }

    /**
     * 流程结束
     *
     * @param dto
     * @param entity
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoOutstockEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        String orderType = entity.getOrderType();
        String b2c = OrderTypeEnum.B2C.getCode();
        Boolean isB2c = b2c.equals(orderType);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        entity.setActualDeliveryDate(LocalDateTime.now());
        updateForApprove(entity.getId(), approveStatus.getStatus(), isB2c);

        Boolean isPass = ApproveStatusEnum.APPROVE.equals(approveStatus);
        if (isPass) {
            //审核通过发送金蝶
            if (!isB2c) {
                syncKingdeeSoOutstockService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
                handleData(entity);
            } else {
                handleSoB2cData(entity);
                syncKingdeeSoOutstockService.syncB2cDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            }
            //订单推送dmp
            syncKingdeeSoOutstockService.syncOrderToDmp(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());

            //修改中转报关单订单出库状态
            transferDeclareFeign.updateOutstockStatus(Arrays.asList(entity.getSoId()), TransferOutstockStatusEnum.OUTSTOCK.getCode());
        }
        return Boolean.TRUE;
    }


    /**
     * 处理B2c销售出库单
     *
     * @param entity
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-01 10:50
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleSoB2cData(SoOutstockEntity entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        //这个是销售出库单id
        List<String> allList = Arrays.asList(entity.getId());
        List<InOutStockDTO> members = baseMapper.listInventoryInOut(allList);
        InventorySourceTypeEnum inventorySourceTypeEnum = InventorySourceTypeEnum.SO_OUTSTOCK;
        String sourceType = entity.getSourceType();

        //如果来源类型为b2c发货单就是扣冻结库存
        String soB2cDelivery = SourceTypeEnum.SO_B2C_DELIVERY.getCode();
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        if (soB2cDelivery.equals(sourceType)) {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        } else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK_USABLE.getCode());
        }
        for (InOutStockDTO member : members) {
            member.setSourceType(inventorySourceTypeEnum);
        }
        if (CollectionUtils.isNotEmpty(members)) {
            inventoryInOutStockDTO.setParamList(members);
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
        //物流单添加
        saveLogisticsBill(entity);
    }


    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus, Boolean isB2c) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().eq(SoOutstockEntity::getId, id)
                .set(SoOutstockEntity::getApproveUserName, userInfo.getUserName())
                .set(SoOutstockEntity::getApproveStatus, approveStatus)
                .set(SoOutstockEntity::getApproveTime, LocalDateTime.now())
                .set(SoOutstockEntity::getActualDeliveryDate, LocalDateTime.now())
                .update(new SoOutstockEntity());
    }


    /**
     * 处理数据
     * 需要更改发货状态
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-05-22 20:01
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleData(SoOutstockEntity entity) {
        if (Objects.isNull(entity)) {
            return;
        }


        //这个是销售出库单id
        List<String> allList = Arrays.asList(entity.getId());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        String sourceType = entity.getSourceType();
        //发货通知单的 id
        List<SoOutstockEntity> noticeSoOutstockList = soDeliveryNotice.equals(sourceType) ? Arrays.asList(entity) : Collections.emptyList();
        //发货通知单的 id
        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();

        //发货通知单详情
        for (SoDeliveryNoticeEntity item : noticeList) {
            String deliveryNoticeId = item.getId();
            SoOutstockEntity noticeSoOutstock = noticeSoOutstockList.stream().filter(o -> o.getSourceId().equals(deliveryNoticeId)).findFirst().orElse(null);
            if (noticeSoOutstock != null) {
                //更新打包时间
                item.setPackDate(noticeSoOutstock.getPackDate());
                item.setActualDeliveryDate(noticeSoOutstock.getActualDeliveryDate()!=null?noticeSoOutstock.getActualDeliveryDate().toLocalDate():null);
            }
            item.setDeliveryStatus(Boolean.TRUE);
        }
        //更改打包日期 以及发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(allList);
        List<String> soDetailIdList = soOutstockDetailList.stream().map(SoOutstockDetailEntity::getSoDetailId).collect(Collectors.toList());

        //这个是销售订单的 这个要统计 存在多个
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soDetailList = soOutstockDetailService.listDetailBySoDetailIds(soDetailIdList);
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        soDetailList = soDetailList.stream().filter(s -> s.getApproveStatus().equals(approveStatus)).collect(Collectors.toList());
        //分组
        Map<String, List<SoOutstockDetailDTO.DeliveryQtyDTO>> map = soDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailDTO.DeliveryQtyDTO::getSoDetailId));
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

        //物流单添加
        saveLogisticsBill(entity);

    }

    //TODO 物流单
    @Async("saveLogisticsBill")
    public void saveLogisticsBill(SoOutstockEntity entity) {
        try {
            LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
            addDTO.setOutstockId(entity.getId());
            addDTO.setOutstockCode(entity.getCode());
            addDTO.setSourceCode(entity.getSoCode());
            String soId = entity.getSoId();
            addDTO.setSourceId(soId);
            String orderType = entity.getOrderType();
            String b2cType = OrderTypeEnum.B2C.getCode();
            addDTO.setOrderType(orderType);
            //表明是是b2b
            if (!b2cType.equals(orderType)) {
                SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
                String salesPlatform = PlatformDictEnum.B2B_FOREIGN.getCode();
                if (Objects.nonNull(soInfo)) {
                    addDTO.setShopId(soInfo.getCustomerId());
                    addDTO.setShopName(soInfo.getCustomerName());
                    addDTO.setOrderTime(soInfo.getCreateTime());
                    addDTO.setSalesPlatform(salesPlatform);
                    addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                    //国家id
                    String countryId = soInfo.getCountryId();
                    List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Arrays.asList(countryId));
                    if (CollectionUtils.isNotEmpty(countryList)) {
                        addDTO.setToCountry(countryList.get(0).getNameCn());
                    } else {
                        addDTO.setToCountry("");
                    }
                    addDTO.setCurrency(soInfo.getCurrency());
                }
            } else {
                //表示是b2c
                SoB2cDTO.CustomerDTO customer = soB2cFeign.getB2cCustomerById(soId);
                if (Objects.nonNull(customer)) {
                    addDTO.setShopId(customer.getShopId());
                    addDTO.setShopName(customer.getShopName());
                    //国家
                    String country = customer.getCountry();
                    String countryName = "";
                    if (StringUtils.isNotBlank(country)) {
                        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Arrays.asList(country));
                        if (CollectionUtils.isNotEmpty(countryList)) {
                            countryName = countryList.get(0).getNameCn();
                        }
                    }
                    addDTO.setOrderTime(customer.getPayTime());
                    String dictPlatform = customer.getDictPlatform();
                    addDTO.setSalesPlatform(dictPlatform);
                    addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                    addDTO.setToCountry(countryName);
                    addDTO.setChannelId(customer.getLogisticsChannelId());
                    addDTO.setTransportNo(customer.getTransportNo());
                    String trackNo = customer.getTrackNo();
                    if(StringUtils.isNotBlank(trackNo)){
                        entity.setTrackNo(trackNo);
                    }
                }

            }


            LocalDateTime actualDeliveryDate = entity.getActualDeliveryDate();
            if (Objects.isNull(actualDeliveryDate)) {
                actualDeliveryDate = entity.getBillDate().atStartOfDay();
            }
            //发货时间
            addDTO.setDeliveryTime(actualDeliveryDate);
            //轨迹单号
            String trackNo = entity.getTrackNo();
            List<LogisticsBillDetailDTO.AddDTO> detailList = new ArrayList<>(10);
            if (StringUtils.isNotBlank(trackNo)) {
                for (String no : trackNo.split(",")) {
                    if (StringUtils.isNotBlank(no)) {
                        LogisticsBillDetailDTO.AddDTO addDetail = new LogisticsBillDetailDTO.AddDTO();
                        addDetail.setTrackNo(no);
                        detailList.add(addDetail);
                    }
                }
            }
            addDTO.setDetailList(detailList);
            logisticsBillFeign.addLogisticsBill(addDTO);
        } catch (Exception e) {
            log.error("物流单添加失败: {}", e.getMessage());
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
        List<String> idList = list.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        //发货通知单的
        List<SoOutstockEntity> noticeSoOutstockList = list.stream().filter(s -> s.getSourceType().equals(soDeliveryNotice)).
                collect(Collectors.toList());

        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();
        //发货通知单详情
        for (SoDeliveryNoticeEntity item : noticeList) {
            item.setDeliveryStatus(Boolean.FALSE);
        }
        //更改发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(idList);
        List<String> soDetailIdList = soOutstockDetailList.stream().map(SoOutstockDetailEntity::getSoDetailId).collect(Collectors.toList());
        //这个是销售订单的 这个要统计 存在多个
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soDetailList = soOutstockDetailService.listDetailBySoDetailIds(soDetailIdList);
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //分组
        Map<String, List<SoOutstockDetailDTO.DeliveryQtyDTO>> map = soDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailDTO.DeliveryQtyDTO::getSoDetailId));
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

        //删除物流单
        LogisticsBillDTO.RemoveDTO removeDTO = new LogisticsBillDTO.RemoveDTO();
        removeDTO.setOutstockIdList(idList);
        logisticsBillFeign.removeLogisticsBill(removeDTO);

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
        String b2cType = OrderTypeEnum.B2C.getCode();
        List<SoOutstockEntity> b2cList = list.stream().filter(o -> b2cType.equals(o.getOrderType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(b2cList)) {
            String code = b2cList.stream().map(SoOutstockEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.B2C_SO_OUTSTOCK_NOT_DIS_APPROVE, code);
        }
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

        //查询是否冻结
        List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
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
            List<SoOutstockEntity> haveSoIdList = list.stream().filter(h -> StringUtils.isNotBlank(h.getSoId())).collect(Collectors.toList());
            handleDisApproveData(haveSoIdList);
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), rejectPairList, "状态变更");
            if (isPushKingDee) {
                //B2B 反审核发送金蝶
                haveSoIdList.stream().filter(l -> !b2cType.equals(l.getOrderType())).forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));


            }

            //修改中转报关单订单出库状态
            transferDeclareFeign.updateOutstockStatus(soIds, TransferOutstockStatusEnum.UN_OUTSTOCK.getCode());
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

        //查询是否冻结
        List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        Boolean result = this.removeByIds(ids);
        String b2cType = OrderTypeEnum.B2C.getCode();
        if (result) {
            //添加日志
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "删除");
            //删除明细
            soOutstockDetailService.removeByMainIdList(ids);
            List<SoOutstockEntity> haveSoIdList = list.stream().filter(h -> StringUtils.isNotBlank(h.getSoId())).collect(Collectors.toList());
            //B2B 删除发送金蝶
            haveSoIdList.stream().filter(l -> !b2cType.equals(l.getOrderType())).forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));
            //B2C 删除发送金蝶
            // haveSoIdList.stream().filter(l->b2cType.equals(l.getOrderType())).forEach(obj -> syncKingdeeSoOutstockService.syncB2cDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));

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

        //查询是否冻结
        List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

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
        String b2cType = OrderTypeEnum.B2C.getCode();
        //B2B 作废发送金蝶
        list.stream().filter(l -> !b2cType.equals(l.getOrderType())).forEach(obj -> syncKingdeeSoOutstockService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_INVALID.getCode()));
        //B2C作废发送金蝶
        //list.stream().filter(l->b2cType.equals(l.getOrderType())).forEach(obj -> syncKingdeeSoOutstockService.syncB2cDataToKingdee(obj, SyncOperateEnum.OPERATE_INVALID.getCode()));

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
        //处理分页数据
        fillPaging(list,false);
        return new PagingVO<>(pageData);
    }

    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPaging(List<SoOutstockDTO.PagingViewDTO> list,Boolean isExport) {
        //sku id
        List<String> skuIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //部门id集合
        List<String> deptIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(deptIdList);
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        List<SoB2cEntity> soB2cEntities= Lists.newArrayList();
        if(Objects.nonNull(isExport) && isExport){
            //查询是否有拦截单
            List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
            soB2cEntities = soB2cFeign.listByIds(soIds);
        }
        String b2c = OrderTypeEnum.B2C.getCode();
        for (SoOutstockDTO.PagingViewDTO item : list) {
            //设置拦截标识
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(item.getSoId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                item.setIsIntercept(soB2cEntity.getIsIntercept());
            }

            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            //部门id
            String salesDeptId = item.getSalesDeptId();
            String salesDeptName = deptList.stream().filter(d -> d.getId().equals(salesDeptId)).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(salesDeptName);
            String orderType = item.getOrderType();
            Boolean isB2c = b2c.equals(orderType);

            //国家名称
            String countryName = countryList.stream().filter(obj -> obj.getId().equals(item.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            item.setCountryName(countryName);
            String orderTypeName = OrderTypeEnum.getName(orderType);
            item.setOrderTypeName(orderTypeName);
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal price = item.getPrice();
            item.setPrice(price);
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }
            //销售单价(本位币)
            item.setCnyPrice(MathUtil.multiply(price, exchangeRate));

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);
            //含税单价(本位币)
            item.setCnyTaxPrice(MathUtil.multiply(taxPrice, exchangeRate));
            item.setCurrency(item.getCurrency());
            item.setCurrencySymbol(item.getCurrencySymbol());
            item.setAllAmountLocalCurrency(item.getAllAmountLocalCurrency());


        }
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
        //处理国家数据
        handleCountryIdList(dto);
        //获取导出数据
        List<SoOutstockDTO.PagingViewDTO> list = baseMapper.listExport(dto, approveList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        fillPaging(list,true);
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
        String orderType = soOutstock.getOrderType();
        String b2c = OrderTypeEnum.B2C.getCode();
        Boolean isB2c = b2c.equals(orderType);
        if(isB2c){
           throw new ServiceException("B2C订单不允许修改出库单");
        }
        List<SoDetailEntity> soDetailList = Collections.emptyList();
        if (!isB2c) {
            //销售订单详情集合
            soDetailList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(dto.getSoId()));
            if (CollectionUtils.isEmpty(soDetailList)) {
                throw new ServiceException("销售订单详情不存在");
            }
            //检查出库数量
            soOutstockDetailService.checkOutQty(dto.getWarehouseId(), dto.getSoId(), dto.getSourceId(), sourceType, detailList);
        } else {
            soOutstockDetailService.checkB2cOrderQty(dto.getWarehouseId(), dto.getSoId(), dto.getSourceId(), sourceType, detailList);
        }


        String code = soOutstock.getCode();
        LocalDate billDate = dto.getBillDate();
        //旧的
        SoOutstockEntity old = new SoOutstockEntity();
        BeanMapper.copy(soOutstock, old);

        String soId = dto.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
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
            BigDecimal taxRate = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
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
        handleSaveOrUpdateDbByB2b(soOutstock,soInfo);
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
                    detail.setSoDetailId(item.getSoDetailId());
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
            LocalDateTime actualDeliveryDate = item.getActualDeliveryDate();
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
                    item.setActualDeliveryDate(approveTime);
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean pagingUpdate(SoOutstockDTO.PagingUpdateDTO dto) {
        List<SoOutstockEntity> list = this.listByIds(dto.getIdList());
        if (ObjectUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        boolean update = lambdaUpdate().in(SoOutstockEntity::getId, dto.getIdList())
                .set(SoOutstockEntity::getTrackNo, dto.getTrackNo())
                .update();
        LogisticsBillDTO.UpdateTrackNoDTO updateTrackNoDTO = new LogisticsBillDTO.UpdateTrackNoDTO();
        updateTrackNoDTO.setTrackNo(dto.getTrackNo());
        updateTrackNoDTO.setOutstockIdList(dto.getIdList());
        logisticsBillFeign.updateTrackNo(updateTrackNoDTO);
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
            List<LocalDateTime> dateList = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
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
                pagingParamDTO.setBillDateList(dateList);
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
                BigDecimal taxRate = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
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
        baseMapper.updateBatch(soOutstockList);


    }

    @Override
    public List<SoOutstockEntity> listByTrackNo(String trackNo) {
        if (StringUtils.isBlank(trackNo)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE).
                like(SoOutstockEntity::getTrackNo, trackNo).list();
    }

    @Override
    public SoOutstockDTO.PagingTotalDTO getTotalByQuery(SoOutstockDTO.PagingParamDTO params) {
        String searchType = params.getSearchType();
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        if (CollectionUtils.isNotEmpty(approveList)) {
            params.setInvalidStatus(Boolean.FALSE);
        }
        //处理国家数据
        handleCountryIdList(params);
        SoOutstockDTO.PagingTotalDTO pagingTotalDTO = baseMapper.getTotalByQuery(params, approveList);
        return pagingTotalDTO;
    }

    /**
     * 生成销售出库单
     *
     * @param soB2cId 销售订单id
     * @return
     * @author yl
     * @date 2023-12-11 16:17
     */
    @Override
    public Boolean generateB2cSoOutstock(String soB2cId) {
        SoOutstockEntity outstock = this.getBySoId(soB2cId);
        if (Objects.isNull(outstock)) {
            SoOutstockDTO.GenerateB2cDTO dto = soB2cFeign.getSoOutstockInfoById(soB2cId);
            Boolean result = createB2cSoOutstock(dto);
            return result;
        } else {
            String id = outstock.getId();
            ApproveStatusEnum approveStatus = outstock.getApproveStatus();
            //待提交
            if (ApproveStatusEnum.WAIT_SUBMIT.equals(approveStatus)) {
                this.submit(Arrays.asList(id));
            }
            //审核中
            if (ApproveStatusEnum.APPROVE_ING.equals(approveStatus)) {
                this.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(), ""));
            }
            return Boolean.TRUE;
        }

    }

    @Override
    public Boolean generateB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO) {
        Boolean result = createB2cSoOutstock(generateB2cDTO);
        return result;
    }

    private SoOutstockEntity getBySoId(String soB2cId) {
        return this.lambdaQuery().eq(SoOutstockEntity::getSoId, soB2cId).
                last("LIMIT 1").one();
    }

    /**
     * 创建B2C销售出库单
     * 1.先添加
     * 2.提交审核
     * 3.审核通过
     *
     * @param dto
     * @return
     */
    public Boolean createB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto) {

        String soB2cId = dto.getSoId();
        String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
        String paramJson = JSONUtil.toJsonStr(dto);
        try {
            String id = this.addB2cSoOutstock(dto);
            //表示添加成功
            if (StringUtils.isNotBlank(id)) {
                //提交
                Boolean submitResult = this.submit(Arrays.asList(id));
                if (submitResult) {
                    this.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(), ""));
                }
            }
            SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
            deleteDTO.setMainId(soB2cId);
            deleteDTO.setType(type);
            soB2cFeign.deleteError(deleteDTO);
            return Boolean.TRUE;
        } catch (Exception e) {
            String message = e.getMessage();
            log.error("创建B2C销售出库单失败,soB2cId:{},paramJson:{} 错误信息:{}", soB2cId, paramJson, message);
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(type);
            addError.setMainId(soB2cId);
            addError.setMessage(message);
            addError.setParamJson(paramJson);
            soB2cFeign.addSoB2cError(addError);
        }


        return Boolean.FALSE;
    }

    /**
     * 添加B2C销售出库单
     *
     * @param dto
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-29 11:51
     */
    @Transactional(rollbackFor = Exception.class)
    public String addB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto) {
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
        soOutstockDetailService.checkB2cOrderQty(dto.getWarehouseId(), dto.getSoId(), sourceId, sourceType, checkList);
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        //处理保存或者修改数据
        handleSaveOrUpdateDb(soOutstock);
        BusinessNoTypeEnum businessNoType = BusinessNoTypeEnum.CODE_XSCK;
        String code = docNoGenHelper.generateCode(businessNoType);
        soOutstock.setCode(code);
        // 出库日期
        soOutstock.setBillDate(LocalDate.now());
        Boolean addResult = this.save(soOutstock);
        if (addResult) {
            soOutstockDetailService.add(soOutstock.getId(), detailList, OrderTypeEnum.B2C.getCode());
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), soOutstock.getId(), "新增操作");

            return soOutstock.getId();
        }
        return "";
    }

    @Override
    public Boolean generateB2cSoOutstockByCode(String soB2cCode) {
        SoOutstockEntity outstock = this.getBySoCode(soB2cCode);
        if (Objects.isNull(outstock)) {
            SoOutstockDTO.GenerateB2cDTO dto = soB2cFeign.getSoOutstockInfoByCode(soB2cCode);
            return this.createB2cSoOutstock(dto);
        }
        return Boolean.TRUE;

    }

    private SoOutstockEntity getBySoCode(String soB2cCode) {
        return this.lambdaQuery().eq(SoOutstockEntity::getSoCode, soB2cCode).
                last("LIMIT 1").one();
    }

    private void handleSaveOrUpdateDb(SoOutstockEntity soOutstock) {
        //仓库id
        String warehouseId = soOutstock.getWarehouseId();
        //用户信息
        if (StringUtils.isNotBlank(soOutstock.getSellerId())) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(soOutstock.getSellerId()));
            if (CollectionUtils.isNotEmpty(userList)) {
                //销售员
                String sellerName = userList.stream().filter(obj -> obj.getUserId().equals(soOutstock.getSellerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setSellerName(sellerName);
            }
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        String orgId = soOutstock.getWarehouseOrgId();
        if (StringUtils.isNotBlank(orgId)) {
            SysAccountingCompanyEntity org = sysUserFeign.getCompanyById(orgId);
            if (Objects.nonNull(org)) {
                soOutstock.setWarehouseOrgName(org.getCompanyName());
            }
        }
    }
}
