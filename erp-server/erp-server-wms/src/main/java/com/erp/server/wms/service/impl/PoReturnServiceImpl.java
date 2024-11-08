package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.rpc.srm.feign.SrmPoReconciliationFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeReturnOrderService;
import com.erp.server.wms.mapper.PoReturnMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PURCHASE_RETURN_ORDER;

/**
 * <p>
 * 采购退货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-07
 */
@Slf4j
@Service
@RefreshScope
public class PoReturnServiceImpl extends SuperServiceImpl<PoReturnMapper, PoReturnEntity> implements PoReturnService {

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SyncKingdeeReturnOrderService syncKingdeeReturnOrderService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Autowired
    private QcInfoService qcInfoService;

    @Autowired
    private PoInstockService poInstockService;

    @Autowired
    private MachineInfoService machineInfoService;

    @Autowired
    private WmsAttachmentService wmsAttachmentService;

    @Autowired
    private CfgSettingService cfgSettingService;

    @Autowired
    private SysPostFeign sysPostFeign;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    @Autowired
    private SrmCfgSettingFeign srmCfgSettingFeign;

    @Autowired
    private SrmPoReconciliationFeign srmPoReconciliationFeign;

    @Autowired
    private SupplierFeign supplierFeign;

    @Value("${companyCode}")
    private String companyCode;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;

    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInStockService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private AbstractWdtService abstractWdtService;
    @Resource
    private SubcontractReturnService subcontractReturnService;
    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;
    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;
//    @Resource
//    private PoReturnService service;
    /**
     * 主页分页查询
     *
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     **/
    @Override
    public PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> paging(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PurchaseReturnOrderDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        //明细数据
        List<PurchaseReturnOrderDTO.PagingViewDTO> records = pageData.getRecords();

        if (CollectionUtils.isNotEmpty(records)) {
            fillList(records);
        }
        return new PagingVO(pageData);
    }

    private void fillList(List<PurchaseReturnOrderDTO.PagingViewDTO> records) {
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(PurchaseReturnOrderDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());

        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取采购单详情的id集合
        List<String> detailId = records.stream().map(PurchaseReturnOrderDTO.PagingViewDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //获取采购单的id集合
        List<String> purchaseOrderIds = records.stream().map(PurchaseReturnOrderDTO.PagingViewDTO::getPurchaseOrderId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(detailId);
        records.stream().forEach(record->{

            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().filter(entityClass -> entityClass.getId().equals(record.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                record.setReturnPrice(purchaseOrderDetailEntity.getTaxPrice());
            }
            //委外订单号
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(record.getPurchaseSourceType())){
                record.setSubcontractCode(record.getPurchaseSourceCode());
            }
            ReturnOrderSourceEnum returnOrderSourceEnum = Objects.equals(record.getSourceType(), SourceTypeEnum.QC_INFO.getCode()) ?
                    ReturnOrderSourceEnum.QC : ReturnOrderSourceEnum.OTHER;
            record.setReturnOrderSource(returnOrderSourceEnum.getCode());
            record.setReturnOrderSourceName(returnOrderSourceEnum.getName());
            BigDecimal deductAmountAmount ;
            if (ReturnModeEnum.DEDUCTION.getCode().equals(record.getReturnMode())) {
                deductAmountAmount = MathUtil.multiply(record.getReturnPrice(),record.getDeductAmountQty());
            } else {
                deductAmountAmount = MathUtil.multiply(record.getReturnPrice(),record.getReturnQty());
            }
            record.setDeductAmountAmount(deductAmountAmount);

            //是否是组合品
            record.setIsCombinationName(record.getIsCombination() ? "是" : "否");
        });

        List<String> warehouseIds = records.stream().map(req -> req.getReturnWarehouseId()).distinct().collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);

        records.forEach(obj -> {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            //订单确认状态名称
            obj.setConfirmStatusName(PoReturnConfirmStatusEnum.getName(obj.getConfirmStatus()));

            //异常分类名称
            obj.setUnusualTypeName(PoReturnUnusualTypeEnum.getName(obj.getUnusualType()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setReturnModeName(ReturnModeEnum.getName(obj.getReturnMode()));
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(obj.getReturnWarehouseId()) && req.getCode().equals(obj.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            obj.setWarehouseLocationName(warehouseLocationEntity.getName());
        });
    }

    /**
     * 新增
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(PurchaseReturnOrderDTO.AddDTO dto) {
        FindUserDTO userDTO = new FindUserDTO();

        //获取用户信息
        if (ObjectUtils.isNotEmpty(dto.getReturnUserId())) {
            userDTO =  sysUserFeign.getUserByUserId(dto.getReturnUserId());
        }
        //验证单价必填
//        if (ReturnModeEnum.DEDUCTION.getCode().equals(dto.getReturnMode())) {
//            long count = dto.getPurchasePriceDetailList().stream().filter(obj -> MathUtil.compareTo(obj.getReturnPrice(), MathUtil.ZERO) == MathUtil.ZERO).count();
//            if (count > MathUtil.ZERO) {
//                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_ORDER_PRICE_IS_NOT_NULL);
//            }
//        }
        //无关联采购时 退款单价不能为空
        if (StrUtil.isBlank(dto.getPurchaseOrderId())){
            List<PurchaseReturnOrderDetailDTO.AddDTO> collect = dto.getPurchasePriceDetailList().stream().filter(e -> Objects.nonNull(e) && Objects.isNull(e.getReturnPrice())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                throw new ServiceException(ApiError.ERROR_92261);
            }
        }
        //获取核算公司
        List<BaseIdDTO.CodeDTO> companyEntityList = sysUserFeign.getAccountingCompanyList(Arrays.asList(dto.getReturnOrgId(),dto.getPurchaseOrgId()));
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getReturnWarehouseId());
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGTH);
        //设置收货单主表
        PoReturnEntity poReturnEntity = new PoReturnEntity();
        BeanMapperUtils.copy(dto, poReturnEntity);
        if (StrUtil.isBlank(poReturnEntity.getReturnType())){
            poReturnEntity.setReturnType(SourceTypeEnum.SELF_ADD.getCode());
        }
        poReturnEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        poReturnEntity.setCode(code);
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取采购订单主表信息
            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
            poReturnEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
            poReturnEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());

        }

        //获取采购单供应商信息
//        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(dto.getPurchaseUserId());
            poReturnEntity.setPurchaseUserId(dto.getPurchaseUserId());
            poReturnEntity.setPurchaseUserName(purchaseUser.getUserName());
        }

        poReturnEntity.setSupplierId(dto.getSupplierId());
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(dto.getSupplierId());
        poReturnEntity.setSupplierName(supplierEntity.getName());
        poReturnEntity.setSupplierContactId(dto.getSupplierContactId());
        if (StringUtils.isNotBlank(dto.getSupplierContactId())) {
            SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(dto.getSupplierContactId());
            poReturnEntity.setSupplierContactName(supplierContactById.getPerson());
        }
        poReturnEntity.setReturnUserName(userDTO != null ? userDTO.getUserName() : "");
        //退货组织名称
        String returnOrgName = companyEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), dto.getReturnOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        poReturnEntity.setReturnOrgName(returnOrgName);
        //采购组织名称
        String purchaseOrgName = companyEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), dto.getPurchaseOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        poReturnEntity.setPurchaseOrgName(purchaseOrgName);

        poReturnEntity.setBillDate(dto.getBillDate());
        poReturnEntity.setReturnWarehouseName(warehouseEntity.getName());

        //保存主表信息
        this.save(poReturnEntity);
        //记录日志
        String msg = "";
        String sourceType = poReturnEntity.getSourceType();
        if (SourceTypeEnum.AUTO_ADD.getCode().equals(dto.getReturnType())){
            msg = StrUtil.format("从采购退货单【{}】下推生成了委外退料单【{}】采购退货单【{}】", dto.getParentReturnCode(), dto.getChildSubcontractCode(),code);
        }else {
            if(StrUtil.isNotBlank(sourceType) && SourceTypeEnum.PURCHASE_ORDER.getCode().equals(sourceType)){
                msg = StrUtil.format("从采购订单【{}】下推生成了采购退货单【{}】", poReturnEntity.getPurchaseOrderCode(), code);
            }else if (StrUtil.isNotBlank(sourceType) && SourceTypeEnum.QC_INFO.getCode().equals(sourceType)){
                QcInfoEntity qcInfoEntity = qcInfoService.getById(poReturnEntity.getSourceId());
                if (Objects.nonNull(qcInfoEntity)){
                    msg = StrUtil.format("从质检单【{}】下推生成了采购退货单【{}】", qcInfoEntity.getCode(), code);
                }
            }else if (StrUtil.isNotBlank(sourceType) && SourceTypeEnum.PO_INSTOCK.getCode().equals(sourceType)){
                PoInstockEntity poInstockEntity = poInstockService.getById(poReturnEntity.getSourceId());
                if (Objects.nonNull(poInstockEntity)){
                    msg = StrUtil.format("从采购入库单【{}】下推生成了采购退货单【{}】", poInstockEntity.getCode(), code);
                }
            }
        }

        if (StrUtil.isBlank(msg)){
            msg = String.format("新增了一个采购退货单【%s】", code);
        }
        //操作日志
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), poReturnEntity.getId(), "新增操作");

        //保存详情信息
        poReturnDetailService.add(dto, poReturnEntity.getId());
        return poReturnEntity.getId();
    }


    /**
     * 修改
     *
     * @param dto dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto) {
        //获取用户信息
        FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getReturnUserId());
        //获取核算公司
        List<BaseIdDTO.CodeDTO> companyEntityList = sysUserFeign.getAccountingCompanyList(Arrays.asList(dto.getReturnOrgId(),dto.getPurchaseOrgId()));
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getReturnWarehouseId());


        //验证单价必填
//        if (ReturnModeEnum.DEDUCTION.getCode().equals(dto.getReturnMode())) {
//            long count = dto.getPurchasePriceDetailList().stream().filter(obj -> MathUtil.compareTo(obj.getReturnPrice(), MathUtil.ZERO) == MathUtil.ZERO).count();
//            if (count > MathUtil.ZERO) {
//                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_ORDER_PRICE_IS_NOT_NULL);
//            }
//        }
        //无关联采购时 退款单价不能为空
        if (StrUtil.isBlank(dto.getPurchaseOrderId())){
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> collect = dto.getPurchasePriceDetailList().stream().filter(e -> Objects.nonNull(e) && Objects.isNull(e.getReturnPrice())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                throw new ServiceException(ApiError.ERROR_92261);
            }
        }
        //设置收货单主表
        PoReturnEntity poReturnEntity = new PoReturnEntity();
        BeanMapperUtils.copy(dto, poReturnEntity);
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取采购订单主表信息
            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
            poReturnEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
            poReturnEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        }


        //获取采购单供应商信息
//        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        if (StringUtils.isNotBlank(dto.getPurchaseUserId())) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(dto.getPurchaseUserId());
            poReturnEntity.setPurchaseUserName(purchaseUser.getUserName());
        }

        poReturnEntity.setPurchaseUserId(dto.getPurchaseUserId());
        poReturnEntity.setSupplierId(dto.getSupplierId());
        if (StringUtils.isNotBlank(dto.getSupplierId())) {
            SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(dto.getSupplierId());
            if(ObjectUtil.isNotEmpty(supplierEntity)) {
                poReturnEntity.setSupplierName(supplierEntity.getName());
            }
        }
        poReturnEntity.setSupplierContactId(dto.getSupplierContactId());
        if (StringUtils.isNotBlank(dto.getSupplierContactId())) {
            SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(dto.getSupplierContactId());
            if (ObjectUtil.isNotEmpty(supplierContactById)) {
                poReturnEntity.setSupplierContactName(supplierContactById.getPerson());
            }
        }
        poReturnEntity.setReturnUserName(userDTO.getUserName());
        //退货组织名称
        String returnOrgName = companyEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), dto.getReturnOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        poReturnEntity.setReturnOrgName(returnOrgName);
        //采购组织名称
        String purchaseOrgName = companyEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), dto.getPurchaseOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        poReturnEntity.setPurchaseOrgName(purchaseOrgName);

        poReturnEntity.setBillDate(dto.getBillDate());
        poReturnEntity.setReturnWarehouseName(warehouseEntity.getName());
        //更新收货单主表信息
        this.updateById(poReturnEntity);

        //操作日志
        PoReturnEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, poReturnEntity, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), poReturnEntity.getId(), "", "");

        //更新收货单详情表信息
        return poReturnDetailService.update(dto, poReturnEntity.getId());
    }

    /**
     * 查询详情
     *
     * @param id id
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewDTO
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     **/
    @Override
    public PurchaseReturnOrderDTO.ViewDTO view(String id) {
        PurchaseReturnOrderDTO.ViewDTO viewDTO = new PurchaseReturnOrderDTO.ViewDTO();
        PoReturnEntity poReturnEntity = this.getById(id);
        BeanMapperUtils.copy(poReturnEntity, viewDTO);

        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        if (StringUtils.isNotBlank(poReturnEntity.getPurchaseOrderId())) {
            //获取采购订单主表信息
            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(poReturnEntity.getPurchaseOrderId());
            viewDTO.setPurchaseUserDeptId(purchaseOrderEntity.getPurchaseDeptId());
            viewDTO.setPurchaseUserDeptName(purchaseOrderEntity.getPurchaseDeptName());
            viewDTO.setSourceCode(purchaseOrderEntity.getCode());
            SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(viewDTO.getReturnUserId());
            if (ObjectUtils.isNotEmpty(deptByUserId)) {
                viewDTO.setReturnDeptId(deptByUserId.getDepartmentId());
                viewDTO.setReturnDeptName(deptByUserId.getDepartmentName());
            }
        } else {
            List<SysDepartmentUserNumberDTO> sysDepartmentUserNumberDTOS = sysUserFeign.listDeptUserByUserIdList(Arrays.asList(viewDTO.getReturnUserId(), viewDTO.getPurchaseUserId()));
            SysDepartmentUserNumberDTO purchaseUserDeptDTO = sysDepartmentUserNumberDTOS.stream().filter(req -> req.getUserId().equals(viewDTO.getPurchaseUserId())).findFirst().orElse(new SysDepartmentUserNumberDTO());
            viewDTO.setPurchaseUserDeptId(purchaseUserDeptDTO.getDepartmentId());
            viewDTO.setPurchaseUserDeptName(purchaseUserDeptDTO.getDepartmentName());
            SysDepartmentUserNumberDTO returnDeptDTO = sysDepartmentUserNumberDTOS.stream().filter(req -> req.getUserId().equals(viewDTO.getReturnUserId())).findFirst().orElse(new SysDepartmentUserNumberDTO());
            viewDTO.setReturnDeptId(returnDeptDTO.getDepartmentId());
            viewDTO.setReturnDeptName(returnDeptDTO.getDepartmentName());
        }

        if (SourceTypeEnum.QC_INFO.getCode().equals(poReturnEntity.getSourceType())) {
            viewDTO.setSourceType(ReturnOrderSourceEnum.QC.getCode());
            viewDTO.setSourceTypeName(ReturnOrderSourceEnum.QC.getName());
        } else {
            viewDTO.setSourceType(ReturnOrderSourceEnum.OTHER.getCode());
            viewDTO.setSourceTypeName(ReturnOrderSourceEnum.OTHER.getName());
        }
        if (StringUtils.isNotBlank(viewDTO.getReturnMode())) {
            viewDTO.setReturnModeName(ReturnModeEnum.getName(viewDTO.getReturnMode()));
        }

        //创库保存详情表的集合
        List<PurchaseReturnOrderDetailDTO.ViewDTO> detailViewDTOS = new ArrayList<>();
        //根据收货单主表id获取详情信息
        List<PoReturnDetailEntity> detail = poReturnDetailService.getDetailByMainId(id);
        //获取sku的id集合
        List<String> skuNoList = detail.stream().map(PoReturnDetailEntity::getSkuNo).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> detailEntityList = plmTaskFeign.listBySkuNoList(skuNoList);

        //获取采购单详情的id集合
        List<String> detailId = detail.stream().map(PoReturnDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);

        List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(detailId);

        List<String> skuIdList = detail.stream().map(PoReturnDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseLocationCodeList = detail.stream().map(r->StrUtils.null2EmptyWithTrim(r.getWarehouseLocation())).distinct().collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(Lists.newArrayList(poReturnEntity.getReturnWarehouseId()));
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationCodeList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());

        //查询供应商信息
        List<String> supplierIdList = new ArrayList<>();
        supplierIdList.add(poReturnEntity.getSupplierId());
        List<String> mainSupplierIdList = detail.stream().filter(obj -> StringUtils.isNotBlank(obj.getMainSupplierId())).map(PoReturnDetailEntity::getMainSupplierId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(mainSupplierIdList)) {
            supplierIdList.addAll(mainSupplierIdList);
        }
        mainSupplierIdList.add(poReturnEntity.getSupplierId());
        List<SupplierEntity>  supplierList = scmTaskFeign.getSupplierByIdList(mainSupplierIdList);

        //主表供应商
        SupplierEntity supplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(poReturnEntity.getSupplierId())).findFirst().orElse(new SupplierEntity());
        viewDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        //可用数量
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(poReturnEntity.getReturnWarehouseId()));

        for (PoReturnDetailEntity poReturnDetailEntity : detail) {
            Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(poReturnDetailEntity.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            PurchaseReturnOrderDetailDTO.ViewDTO detailView = new PurchaseReturnOrderDetailDTO.ViewDTO();
            BeanMapperUtils.copy(poReturnDetailEntity, detailView);
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(detailView.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                detailView.setReturnPrice(purchaseOrderDetailEntity.getTaxPrice());
                detailView.setPurchaseQty(purchaseOrderDetailEntity.getPurchaseQty());
                detailView.setCurrency(purchaseOrderDetailEntity.getCurrency());
                detailView.setCurrencySymbol(purchaseOrderDetailEntity.getCurrencySymbol());
            }
            detailView.setHasStockInQty(stockInQty);
            BigDecimal deductAmountAmount;
            if (ReturnModeEnum.DEDUCTION.getCode().equals(poReturnEntity.getReturnMode())) {
                deductAmountAmount = MathUtil.multiply(detailView.getReturnPrice(), poReturnDetailEntity.getDeductAmountQty());
            } else {
                deductAmountAmount = MathUtil.multiply(detailView.getReturnPrice(), poReturnDetailEntity.getReturnQty());
            }
            detailView.setTotalPrice(deductAmountAmount);
            //获取sku信息
            SkuVO productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getSkuNo().equals(detailView.getSkuNo())).findFirst().orElse(new SkuVO());
            detailView.setProductName(productDetailEntity.getSkuName());
            detailView.setSpuNo(productDetailEntity.getSpuNo());
            detailView.setUnit(productDetailEntity.getUnitName());
            detailView.setVariantProperty(productDetailEntity.getVariantProperty());

            //参考供应商
            SupplierEntity mainSupplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(poReturnDetailEntity.getMainSupplierId())).findFirst().orElse(new SupplierEntity());
            detailView.setMainSupplierName(mainSupplierEntity.getName());

            //根据组织、仓库、sku查询可用库存
            /*
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(purchaseReturnOrderEntity.getReturnWarehouseId(), purchaseReturnOrderDetailEntity.getSkuId());
            detailView.setCurInventoryQty(curInventoryQty);
             */
            //即时库存
            Integer curInventoryQty = skuInventoryList.stream().filter(r ->Objects.equals(r.getSkuId(), poReturnDetailEntity.getSkuId())
                    && Objects.equals(r.getWarehouseId(), poReturnEntity.getReturnWarehouseId())
                    && Objects.equals(r.getWarehouseLocationId(), StrUtils.null2EmptyWithTrim(poReturnDetailEntity.getWarehouseLocation()))).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            detailView.setCurInventoryQty(curInventoryQty);

            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(detailView.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            detailView.setWarehouseLocationName(warehouseLocationEntity.getName());

            detailViewDTOS.add(detailView);
        }
        viewDTO.setPurchasePriceDetailList(detailViewDTOS);
        return viewDTO;
    }

    /**
     * 提交
     *
     * @param ids ids
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        List<PoReturnEntity> purchaseReturnOrderEntities = this.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseReturnOrderEntities)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = purchaseReturnOrderEntities.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != purchaseReturnOrderEntities.size()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //采购组织不能为空
        String codes = purchaseReturnOrderEntities.stream().filter(obj -> StrUtil.isBlank(obj.getPurchaseOrgId())).map(PoReturnEntity::getCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(codes)) {
            throw new ServiceException(StrUtil.format("采购退货单【{}】采购组织不能为空",codes));
        }
        //迭代1.27.5新增校验 ：校验退货数量不能大于已收货数量(已审核)-已入库数量(已审核)【按照SKU明细校验】
        List<PoReturnDetailEntity> allPoReturnDetailEntities = poReturnDetailService.listByMainIds(ids);
        List<String> podIds = allPoReturnDetailEntities.stream().map(PoReturnDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //采购收货
        List<WarehouseReceiveDetailEntity> receiveDetails = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        receiveDetails = receiveDetails.stream().filter(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());
        //采购入库
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailByPodIds(podIds);
        poInstockDetailList = poInstockDetailList.stream().filter(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());
        List<String> errorCodes = new ArrayList<>();
        //已提交的采购退货
        List<PoReturnDetailEntity> samePurchaseDetailIds = baseMapper.listPoReturnByPoDetailIds(podIds,Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus(),ApproveStatusEnum.APPROVE.getStatus()));
        samePurchaseDetailIds = samePurchaseDetailIds.stream().filter(v->!ids.contains(v.getMainId())).collect(Collectors.toList());
        for (PoReturnEntity purchaseReturnOrderEntity : purchaseReturnOrderEntities) {
            if(!SourceTypeEnum.QC_INFO.getCode().equals(purchaseReturnOrderEntity.getSourceType())){
                continue;
            }
            List<PoReturnDetailEntity> poReturnDetailEntityList = allPoReturnDetailEntities.stream().filter(v->v.getMainId().equals(purchaseReturnOrderEntity.getId())).collect(Collectors.toList());
            for (PoReturnDetailEntity poReturnDetailEntity : poReturnDetailEntityList) {
                Integer receiveQty = receiveDetails.stream().filter(v->v.getPurchaseOrderDetailId().equals(poReturnDetailEntity.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                Integer instockQty = poInstockDetailList.stream().filter(v->v.getPurchaseOrderDetailId().equals(poReturnDetailEntity.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                Integer otherDetailQty = samePurchaseDetailIds.stream().filter(v->v.getPurchaseOrderDetailId().equals(poReturnDetailEntity.getPurchaseOrderDetailId())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                if(poReturnDetailEntity.getReturnQty() > receiveQty-instockQty - otherDetailQty){
                    errorCodes.add(StrUtil.format("采购退货单【{}】sku【{}】退货数量不能大于已收货数量-已入库数量-已提交的质检退货数量【{}】",purchaseReturnOrderEntity.getCode(),poReturnDetailEntity.getSkuNo(),receiveQty-instockQty-otherDetailQty));
                }else{
                    samePurchaseDetailIds.add(poReturnDetailEntity);
                }
                //无关联采购时 退款单价不能为空
                if (StrUtil.isBlank(purchaseReturnOrderEntity.getPurchaseOrderId())){
                    if (Objects.isNull(poReturnDetailEntity.getReturnPrice())){
                        throw new ServiceException(ApiError.ERROR_92262, purchaseReturnOrderEntity.getCode(), poReturnDetailEntity.getSkuNo());
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(errorCodes)){
            throw new ServiceException(errorCodes.toString().replace("[","").replace("]",""));
        }

        //TODO 待加审核流程
        //更新审核状态
        lambdaUpdate().set(PoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(PoReturnEntity::getId, ids)
                .set(PoReturnEntity::getApproveUserId, "")
                .set(PoReturnEntity::getApproveUserName, "")
                .set(PoReturnEntity::getApproveTime, null)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = purchaseReturnOrderEntities.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个采购退货单【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "提交操作");

        return Boolean.TRUE;
    }

    /**
     * 新增提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchaseReturnOrderDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    /**
     * 修改提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    /**
     * 批量审核
     *
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(PoReturnEntity entity, String type, String comment, Boolean isNeedProcess,List<PoReturnDetailEntity> poReturnDetailList) {
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //库存校验
        checkInventoryQty(Collections.singletonList(entity));
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购退货单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), entity.getId(), "审核操作");
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            String confirmStatus = "";
            //查询退货配置
            CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.PO_RETURN.getCode());
            CfgSettingValueDTO.PoReturnSettingDTO poReturnSettingDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.PoReturnSettingDTO.class);
            if (poReturnSettingDTO.getIsReturnConfirm()) {
                confirmStatus = PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus();
            } else {
                confirmStatus = PoReturnConfirmStatusEnum.CONFIRM.getStatus();
            }

            //审核通过
            lambdaUpdate().set(PoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(PoReturnEntity::getApproveUserId, userInfo.getUid())
                    .set(PoReturnEntity::getApproveUserName, userInfo.getUserName())
                    .set(PoReturnEntity::getApproveTime, LocalDateTime.now())
                    .set(PoReturnEntity::getConfirmStatus, confirmStatus)
                    .set(confirmStatus.equals(PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus()), PoReturnEntity::getConfirmDate, null)
                    .set(confirmStatus.equals(PoReturnConfirmStatusEnum.CONFIRM.getStatus()),PoReturnEntity::getConfirmDate, LocalDate.now())
                    .eq(PoReturnEntity::getId, entity.getId())
                    .update();

            List<PurchaseOrderDetailEntity> list = new ArrayList<>();
                List<String> detailId = poReturnDetailList.stream().map(PoReturnDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
                List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
                if (CollectionUtils.isNotEmpty(purchaseOrderDetailEntities)) {
                    //退料扣款
                    if (entity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {

                        poReturnDetailList.forEach(returnOrderDetailEntity -> {
                            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(returnOrderDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
                            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                                PurchaseOrderDetailEntity detail = new PurchaseOrderDetailEntity ();
                                detail.setId(purchaseOrderDetailEntity.getId());
                                detail.setPurchaseAmount(purchaseOrderDetailEntity.getPurchaseAmount().subtract(returnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(returnOrderDetailEntity.getReturnQty())))));
                                list.add(detail);
                            }

                        });
                    }
                }
            entity.setConfirmStatus(confirmStatus);
            entity.setConfirmDate(confirmStatus.equals(PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus()) ? null :LocalDate.now());
            //退货补货退货id集合
            List<String> poReturnIdList = new ArrayList<>();
            if (ReturnModeEnum.REPLENISHMENT.getCode().equals(entity.getReturnMode())){
                poReturnIdList.add(entity.getId());
            }
            //需要更新执行状态的采购订单明细id集合
            List<String> purchaseDetailIdList = new ArrayList<>();

            //审核通过-自动生成-委外退料单，purchaseDetailIdList用于取退货子级采购订单明细更新执行状态
            List<PoReturnEntity> poReturnEntityList1 = autoAddSubcontractReturn(entity, poReturnDetailList, confirmStatus,purchaseDetailIdList);
            if (CollectionUtils.isNotEmpty(poReturnEntityList1)){
                poReturnEntityList1.add(entity);
            }else {
                poReturnEntityList1 = Collections.singletonList(entity);
            }
            //需要更新采购明细执行状态的父级退货单
            List<String> podIds = poReturnDetailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getPurchaseOrderDetailId()) && poReturnIdList.contains(obj.getMainId()))
                    .map(obj -> obj.getPurchaseOrderDetailId()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(podIds)) {
                purchaseDetailIdList.addAll(podIds);
            }

            //更新采购订单明细中的执行状态
            if (CollectionUtils.isNotEmpty(purchaseDetailIdList)) {
                updateArrivalState(purchaseDetailIdList);
            }
            //自动生成补货采购订单
            autoAddPurchaseOrder(poReturnEntityList1, Boolean.TRUE);
            //审核通过生成对账明细
            autoAddPoReconciliationDetail(poReturnEntityList1);
            // 更新库存信息
            updateInventoryTransCore(poReturnEntityList1);
            //发送金蝶
            sendPushTask(poReturnEntityList1,SyncOperateEnum.OPERATE_APPROVE.getCode());
            //发送旺店通
            poReturnEntityList1.forEach(obj -> syncApprovePoReturnToWdt(obj, SyncOperateEnum.OPERATE_APPROVE));
        } else {
            //审核不通过
            lambdaUpdate().set(PoReturnEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .set(PoReturnEntity::getApproveUserId, userInfo.getUid())
                    .set(PoReturnEntity::getApproveUserName, userInfo.getUserName())
                    .set(PoReturnEntity::getApproveTime, LocalDateTime.now())
                    .eq(PoReturnEntity::getId, entity.getId())
                    .update();
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }
    /**
     * 根据退货单类型生成委外退料单
     *
     * @param entity
     * @param poReturnDetailList
     * @param confirmStatus
     */
    public List<PoReturnEntity> autoAddSubcontractReturn(PoReturnEntity entity, List<PoReturnDetailEntity> poReturnDetailList, String confirmStatus,List<String> purchaseDetailIdList) {
        if (Objects.isNull(entity) || CollectionUtils.isEmpty(poReturnDetailList) || StrUtil.isBlank(entity.getPurchaseOrderId())){
            return null;
        }
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(entity.getPurchaseOrderId());
        if (Objects.isNull(purchaseOrderEntity)){
            log.info(StrUtil.format("退货单【{}】未找到关联采购订单", entity.getCode()));
            return null;
        }
        //不是委外订单(成品)不进行下面操作
        if (!PurchaseOrderTypeEnum.ENUM_SUBCONTRACT.getCode().equals(purchaseOrderEntity.getType()) || !SubcontractTypeEnum.ENUM_PARENT.getCode().equals(purchaseOrderEntity.getSubcontractType())){
            return null;
        }
        //质检退货类型退货单无需自动生成
        if (SourceTypeEnum.QC_INFO.getCode().equals(entity.getSourceType())) {
            return null;
        }

        //查询委外订单记录
        String sourceId = purchaseOrderEntity.getSourceId();
        //全部采购订单记录
        List<PurchaseOrderEntity> purchaseOrderEntityList = scmTaskFeign.listPoBySourceIds(Collections.singletonList(sourceId));
        //全部采购订单明细
        List<String> poIds = purchaseOrderEntityList.stream().map(PurchaseOrderEntity::getId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.listByPurchaseOrderIds(poIds);
        //全部委外订单明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailEntityList = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(sourceId));
        //全部委外订单
        List<SubcontractOrderEntity> subcontractOrderEntityList = scmTaskFeign.listSubcontractOrderByIds(Collections.singletonList(sourceId));
        //汇总成品采购订单id
        String purchaseOrderId = entity.getPurchaseOrderId();
        //汇总成品采购订单明细ids
        List<String> purcechaseOrderDetailIds = poReturnDetailList.stream().filter(Objects::nonNull).map(PoReturnDetailEntity::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());
        //成品采购订单明细
        List<PurchaseOrderDetailEntity> parentPurchaseOrderDetailList = purchaseOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CollectionUtils.isNotEmpty(purcechaseOrderDetailIds) && purcechaseOrderDetailIds.contains(e.getId())).collect(Collectors.toList());
        //汇总成品采购明细ids-关联的委外订单明细子件
        List<String> parentSubDetailIds = parentPurchaseOrderDetailList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSourceDetailId())).map(PurchaseOrderDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        //成品委外订单明细
        List<SubcontractOrderDetailEntity> parentSubcontractOrderDetailList = subcontractOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CollectionUtils.isNotEmpty(parentSubDetailIds) && parentSubDetailIds.contains(e.getId())).collect(Collectors.toList());
        //子件委外订单明细
        List<SubcontractOrderDetailEntity> childSubcontractDetailList = subcontractOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CollectionUtils.isNotEmpty(parentSubDetailIds) && parentSubDetailIds.contains(e.getParentId())).collect(Collectors.toList());
        List<String> childSubcontractDetailIds = childSubcontractDetailList.stream().filter(Objects::nonNull).map(SubcontractOrderDetailEntity::getId).distinct().collect(Collectors.toList());
        //子件委外订单
        List<String> childSubcontractIds = childSubcontractDetailList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getMainId())).map(SubcontractOrderDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SubcontractOrderEntity> childSubcontractList = subcontractOrderEntityList.stream().filter(e -> Objects.nonNull(e) && CollectionUtils.isNotEmpty(childSubcontractIds) && childSubcontractIds.contains(e.getId())).collect(Collectors.toList());
        //子件采购订单明细
        List<PurchaseOrderDetailEntity> childPurchaseOrderDetailList = purchaseOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CollectionUtils.isNotEmpty(childSubcontractDetailIds) && childSubcontractDetailIds.contains(e.getSourceDetailId())).collect(Collectors.toList());
        List<String> childPurchaseOrderIds = childPurchaseOrderDetailList.stream().filter(Objects::nonNull).map(PurchaseOrderDetailEntity::getPurchaseOrderId).distinct().collect(Collectors.toList());
        //子件采购订单
        List<PurchaseOrderEntity> childPurchaseOrderList = purchaseOrderEntityList.stream().filter(e -> Objects.nonNull(e) && CollectionUtils.isNotEmpty(childPurchaseOrderIds) && childPurchaseOrderIds.contains(e.getId())).collect(Collectors.toList());
        //获取采购订单关联的供应商
        List<PurchaseOrderSupplierEntity> purchaseOrderSupplierEntityList = scmTaskFeign.listOrderSupplierByOrderIdList(childPurchaseOrderIds);
        //查询bom信息
        List<String> skuIds = poReturnDetailList.stream().map(PoReturnDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        //采购退货子单列表
        List<PurchaseReturnOrderDTO.AddDTO> returnAddDTOList = new ArrayList<>();
        //根据子件采购订单构建 委外退料记录
        List<SubcontractReturnDTO.AddDTO> addDTOS = new ArrayList<>();
        List<PoReturnEntity> poReturnEntityList = new ArrayList<>();
        for (PurchaseOrderEntity orderEntity : childPurchaseOrderList){
            String subcontractType = orderEntity.getSubcontractType();
            //排除非子件采购订单数据
            if (!SubcontractTypeEnum.ENUM_CHILD.getCode().equals(subcontractType)){
                continue;
            }
            SubcontractOrderEntity subcontractOrderEntity = childSubcontractList.stream().filter(e -> e.getId().equals(orderEntity.getSourceId())).findFirst().orElse(null);
            if (Objects.isNull(subcontractOrderEntity)){
                throw new ServiceException(StrUtil.format("自动生成委外退料-采购订单【{}】关联委外订单记录为空",orderEntity.getCode()));
            }
            List<SubcontractOrderDetailEntity> subcontractOrderDetailEntityList1 = childSubcontractDetailList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(subcontractOrderEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(subcontractOrderDetailEntityList1)){
                throw new ServiceException(StrUtil.format("自动生成委外退料-采购订单【{}】关联委外订单明细为空",orderEntity.getCode()));
            }
            //采购订单明细
            List<PurchaseOrderDetailEntity> detailEntityList = childPurchaseOrderDetailList.stream().filter(e -> Objects.nonNull(e) && e.getPurchaseOrderId().equals(orderEntity.getId())).collect(Collectors.toList());
            //过滤空订单明细记录
            if (CollectionUtils.isEmpty(detailEntityList)){
                throw new ServiceException(StrUtil.format("自动生成委外退料-采购订单【{}】关联采购订单明细为空",orderEntity.getCode()));
            }
            List<String> poIdList = detailEntityList.stream().map(PurchaseOrderDetailEntity::getId).distinct().collect(Collectors.toList());
            purchaseDetailIdList.addAll(poIdList);

            //查询采购订单关联的供应商记录
            PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierEntityList.stream().filter(e -> Objects.nonNull(e) && orderEntity.getId().equals(e.getPurchaseOrderId())).findFirst().orElse(null);
            if (Objects.isNull(purchaseOrderSupplierEntity)){
                throw new ServiceException(StrUtil.format("自动生成委外退料-采购订单【{}】关联供应商记录为空",orderEntity.getCode()));
            }
            //构建委外退料单
            SubcontractReturnDTO.AddDTO addDTO = buildSubcontractReturnAddDTO(entity,poReturnDetailList,orderEntity,subcontractOrderEntity,purchaseOrderSupplierEntity,detailEntityList,subcontractOrderDetailEntityList1, bomList, parentSubcontractOrderDetailList);
            addDTOS.add(addDTO);
            //构建采购退货单
            PurchaseReturnOrderDTO.AddDTO addDTO1 = buildPoReturnAddDTO(entity, poReturnDetailList, orderEntity, subcontractOrderEntity, purchaseOrderSupplierEntity, detailEntityList, subcontractOrderDetailEntityList1, bomList, parentSubcontractOrderDetailList);
            addDTO1.setChildSubcontractCode(addDTO.getCode());
            returnAddDTOList.add(addDTO1);
        }
        //创建了委外退料记录
        if (CollectionUtils.isNotEmpty(addDTOS)){
            for (SubcontractReturnDTO.AddDTO addDTO : addDTOS){
                BaseResultDTO.AddDTO add = subcontractReturnService.addAndSubmit(addDTO);
                String id = add.getId();
                if (StringUtils.isBlank(id)) {
                    throw new ServiceException(ApiError.ERROR_1019);
                }
                subcontractReturnService.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(),"系统自动审核"));
            }
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //创建子件采购退货单
        if (CollectionUtils.isNotEmpty(returnAddDTOList)){
            for (PurchaseReturnOrderDTO.AddDTO addDTO : returnAddDTOList){
                //新增
                String id = this.add(addDTO);
                if (StringUtils.isBlank(id)) {
                    throw new ServiceException(ApiError.ERROR_1019);
                }
                //查询提交数据
                PoReturnEntity poReturnEntity = this.getById(id);
                //审核通过
                lambdaUpdate().set(PoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                        .set(PoReturnEntity::getApproveUserId, userInfo.getUid())
                        .set(PoReturnEntity::getApproveUserName, userInfo.getUserName())
                        .set(PoReturnEntity::getApproveTime, LocalDateTime.now())
                        .set(PoReturnEntity::getConfirmStatus, confirmStatus)
                        .set(confirmStatus.equals(PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus()), PoReturnEntity::getConfirmDate, null)
                        .set(confirmStatus.equals(PoReturnConfirmStatusEnum.CONFIRM.getStatus()),PoReturnEntity::getConfirmDate, LocalDate.now())
                        .eq(PoReturnEntity::getId, id)
                        .update();
                poReturnEntityList.add(poReturnEntity);
            }
        }
        return poReturnEntityList;
    }

    /**
     * 构建采购退货单
     * @param entity
     * @param poReturnDetailList
     * @param orderEntity
     * @param subcontractOrderEntity
     * @param purchaseOrderSupplierEntity
     * @param detailEntityList
     * @param subcontractOrderDetailEntityList1
     * @param bomList
     * @param parentSubcontractOrderDetailEntityList
     * @return
     */
    private PurchaseReturnOrderDTO.AddDTO buildPoReturnAddDTO(PoReturnEntity entity, List<PoReturnDetailEntity> poReturnDetailList, PurchaseOrderEntity orderEntity, SubcontractOrderEntity subcontractOrderEntity, PurchaseOrderSupplierEntity purchaseOrderSupplierEntity, List<PurchaseOrderDetailEntity> detailEntityList, List<SubcontractOrderDetailEntity> subcontractOrderDetailEntityList1, List<BomChildrenSkuDTO> bomList, List<SubcontractOrderDetailEntity> parentSubcontractOrderDetailEntityList) {
        PurchaseReturnOrderDTO.AddDTO returnAddDTO = new PurchaseReturnOrderDTO.AddDTO();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        returnAddDTO.setReturnType(SourceTypeEnum.AUTO_ADD.getCode());
        returnAddDTO.setParentReturnCode(entity.getCode());
        returnAddDTO.setReturnMode(entity.getReturnMode());
        returnAddDTO.setReturnOrgId(orderEntity.getReceiveOrgId());
        returnAddDTO.setReturnWarehouseId(orderEntity.getDeliveryWarehouseId());
        returnAddDTO.setBillDate(LocalDate.now());
        returnAddDTO.setPurchaseOrderId(orderEntity.getId());
        returnAddDTO.setPurchaseOrgId(orderEntity.getPurchaseOrgId());
        returnAddDTO.setPurchaseUserId(orderEntity.getPurchaseUserId());
        returnAddDTO.setReturnUserId(userInfo.getUid());
        returnAddDTO.setSourceId(entity.getId());
        returnAddDTO.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
        returnAddDTO.setSupplierContactId(purchaseOrderSupplierEntity.getSupplierContactId());
        returnAddDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
        //明细
        List<PurchaseReturnOrderDetailDTO.AddDTO> purchasePriceDetailList = new ArrayList<>();
        for (PurchaseOrderDetailEntity detail : detailEntityList){
            //按照采购订单明细进行构建退货单
            SubcontractOrderDetailEntity subcontractOrderDetailEntity = subcontractOrderDetailEntityList1.stream().filter(e -> Objects.nonNull(e)
                    && StrUtil.isNotBlank(e.getSkuId()) && e.getSkuId().equals(detail.getSkuId())
                    && StrUtil.isNotBlank(detail.getSourceDetailId()) && detail.getSourceDetailId().equals(e.getId())
            ).findFirst().orElse(null);
            if (Objects.isNull(subcontractOrderDetailEntity)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在委外订单【{}】未找到", orderEntity.getCode(), detail.getSkuNo(),subcontractOrderEntity.getCode()));
            }
            //委外子单关联的父级子单id
            String parentId = subcontractOrderDetailEntity.getParentId();
            if (StrUtil.isBlank(parentId)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在委外订单【{}】关联成品子单明细", orderEntity.getCode(), detail.getSkuNo(),subcontractOrderEntity.getCode()));
            }
            //查询父级skuId
            SubcontractOrderDetailEntity parentSubcontractOrder = parentSubcontractOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(subcontractOrderDetailEntity.getParentId())).findFirst().orElse(null);
            if (Objects.isNull(parentSubcontractOrder)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在委外订单【{}】未找到成品子单明细", orderEntity.getCode(), detail.getSkuNo(),subcontractOrderEntity.getCode()));
            }
            //父级SKU和子级SKU之间的用量
            Integer quantity = bomList.stream()
                    .filter(obj -> subcontractOrderDetailEntity.getBomVersion().equals(obj.getBomVersion()) && obj.getSkuId().equals(subcontractOrderDetailEntity.getSkuId()) && obj.getParentSkuId().equals(parentSubcontractOrder.getSkuId()))
                    .map(BomChildrenSkuDTO::getQuantity).findFirst().orElse(MathUtil.ZERO);
            //成品退货单明细记录
            PoReturnDetailEntity poReturnDetailEntity = poReturnDetailList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(parentSubcontractOrder.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(poReturnDetailEntity)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在退货订单【{}】未找到成品明细", orderEntity.getCode(), detail.getSkuNo(),entity.getCode()));
            }
            PurchaseReturnOrderDetailDTO.AddDTO dto = new PurchaseReturnOrderDetailDTO.AddDTO();
            dto.setCurrency(detail.getCurrency());
            dto.setWarehouseLocation(detail.getWarehouseLocation());
            dto.setReturnPrice(detail.getTaxPrice());
            dto.setReturnQty(poReturnDetailEntity.getReturnQty() * quantity);
            dto.setCurrencySymbol(detail.getCurrencySymbol());
            dto.setDeductAmountQty(poReturnDetailEntity.getDeductAmountQty() * quantity);
            dto.setPurchaseOrderDetailId(detail.getId());
            dto.setReplenishQty(poReturnDetailEntity.getReplenishQty() * quantity);
            dto.setSkuId(detail.getSkuId());
            dto.setSkuNo(detail.getSkuNo());
            dto.setSourceDetailId(poReturnDetailEntity.getId());
            purchasePriceDetailList.add(dto);
        }
        returnAddDTO.setPurchasePriceDetailList(purchasePriceDetailList);
        return returnAddDTO;
    }

    /**
     * 构建委外退料明细新增实体
     *
     * @param entity
     * @param poReturnDetailList
     * @param orderEntity
     * @param subcontractOrderEntity
     * @param purchaseOrderSupplierEntity
     * @param detailEntityList
     * @param subcontractOrderDetailEntityList1
     * @param bomList
     * @param parentSubcontractOrderDetailEntityList
     */
    private SubcontractReturnDTO.AddDTO buildSubcontractReturnAddDTO(PoReturnEntity entity, List<PoReturnDetailEntity> poReturnDetailList,
                                                                     PurchaseOrderEntity orderEntity, SubcontractOrderEntity subcontractOrderEntity,
                                                                     PurchaseOrderSupplierEntity purchaseOrderSupplierEntity,
                                                                     List<PurchaseOrderDetailEntity> detailEntityList,
                                                                     List<SubcontractOrderDetailEntity> subcontractOrderDetailEntityList1,
                                                                     List<BomChildrenSkuDTO> bomList,
                                                                     List<SubcontractOrderDetailEntity> parentSubcontractOrderDetailEntityList) {
        SubcontractReturnDTO.AddDTO addDTO = new SubcontractReturnDTO.AddDTO();
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TLD);
        addDTO.setCode(code);
        addDTO.setBillDate(LocalDate.now());
        addDTO.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
        addDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
        addDTO.setSupplierName(purchaseOrderSupplierEntity.getSupplierName());
        //使用成品退货单生成子件退料单
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setSubcontractOrderId(subcontractOrderEntity.getId());
        addDTO.setSubcontractOrderCode(subcontractOrderEntity.getCode());
        addDTO.setType(SourceTypeEnum.AUTO_ADD.getCode());
        List<SubcontractReturnDetailDTO.AddDTO> detailList = new ArrayList<>(detailEntityList.size());
        for (PurchaseOrderDetailEntity detail : detailEntityList){
            SubcontractOrderDetailEntity subcontractOrderDetailEntity = subcontractOrderDetailEntityList1.stream().filter(e -> Objects.nonNull(e)
                    && StrUtil.isNotBlank(e.getSkuId()) && e.getSkuId().equals(detail.getSkuId())
                    && StrUtil.isNotBlank(detail.getSourceDetailId()) && detail.getSourceDetailId().equals(e.getId())
            ).findFirst().orElse(null);
            if (Objects.isNull(subcontractOrderDetailEntity)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在委外订单【{}】未找到", orderEntity.getCode(), detail.getSkuNo(),subcontractOrderEntity.getCode()));
            }
            SubcontractReturnDetailDTO.AddDTO addDTO1 = new SubcontractReturnDetailDTO.AddDTO();
            addDTO1.setBomVersion(subcontractOrderDetailEntity.getBomVersion());
            //委外子单关联的父级子单id
            String parentId = subcontractOrderDetailEntity.getParentId();
            if (StrUtil.isBlank(parentId)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在委外订单【{}】关联成品子单明细", orderEntity.getCode(), detail.getSkuNo(),subcontractOrderEntity.getCode()));
            }
            //查询父级skuId
            SubcontractOrderDetailEntity parentSubcontractOrder = parentSubcontractOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(subcontractOrderDetailEntity.getParentId())).findFirst().orElse(null);
            if (Objects.isNull(parentSubcontractOrder)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在委外订单【{}】未找到成品子单明细", orderEntity.getCode(), detail.getSkuNo(),subcontractOrderEntity.getCode()));
            }
            //父级SKU和子级SKU之间的用量
            Integer quantity = bomList.stream()
                    .filter(obj -> subcontractOrderDetailEntity.getBomVersion().equals(obj.getBomVersion()) && obj.getSkuId().equals(subcontractOrderDetailEntity.getSkuId()) && obj.getParentSkuId().equals(parentSubcontractOrder.getSkuId()))
                    .map(BomChildrenSkuDTO::getQuantity).findFirst().orElse(MathUtil.ZERO);
            //获取退货单明细记录
            PoReturnDetailEntity poReturnDetailEntity = poReturnDetailList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(parentSubcontractOrder.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(poReturnDetailEntity)){
                throw new ServiceException(StrUtil.format("采购订单【{}】中SKU【{}】在退货订单【{}】未找到成品明细", orderEntity.getCode(), detail.getSkuNo(),entity.getCode()));
            }
            addDTO1.setReturnQty(quantity * poReturnDetailEntity.getReturnQty());
            addDTO1.setQuantity(poReturnDetailEntity.getReturnQty());
            addDTO1.setParentSkuId(parentSubcontractOrder.getSkuId());
            addDTO1.setParentSkuNo(parentSubcontractOrder.getSkuNo());
            addDTO1.setParentSkuUseQty(poReturnDetailEntity.getReturnQty());
            addDTO1.setSkuId(detail.getSkuId());
            addDTO1.setWarehouseId(orderEntity.getDeliveryWarehouseId());
            addDTO1.setWarehouseName(orderEntity.getDeliveryWarehouseName());
            addDTO1.setWarehouseLocation(detail.getWarehouseLocation());
            //成品退货单明细id
            addDTO1.setSourceDetailId(poReturnDetailEntity.getId());
            addDTO1.setSubcontractOrderDetailId(subcontractOrderDetailEntity.getId());
            detailList.add(addDTO1);
        }
        addDTO.setDetailList(detailList);
        return addDTO;
    }

    /**
     * 将审核通过的采购退货单转换为其他出库单推送到旺店通
     *
     * @param entity 采购退货单
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    public void syncApprovePoReturnToWdt(PoReturnEntity entity, SyncOperateEnum syncOperateEnum) {
        if(! "other".equals(entity.getSourceType())){
            log.info("非库存退货单无需推送旺店通：{}", entity);
            return;
        }
        List<PoReturnDetailEntity> detailList = poReturnDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if(detailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }

        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getReturnWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        List<CreateOtherStockoutRequest.GoodsList> goodsList = new ArrayList<>(detailList.size());
        for (PoReturnDetailEntity detailEntity : detailList) {
            CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getReturnQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getReturnWarehouseId());
            goodsList.add(goods);
        }
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_OUTSTOCK);
    }

    /**
     * @description: 新增对账单
     * @author Will
     * @date: 2024/1/25 9:59
     * @param poReturnEntityList
     */
    public void autoAddPoReconciliationDetail ( List<PoReturnEntity> poReturnEntityList) {
        if (CollectionUtils.isEmpty(poReturnEntityList)) {
            return;
        }
        List<String> ids = poReturnEntityList.stream().map(PoReturnEntity::getId).collect(Collectors.toList());
        List<PoReturnDetailEntity> poReturnDetailList = poReturnDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(poReturnDetailList)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        List<PoReconciliationDetailDTO.AddDTO> addList = new ArrayList<>();
        for (PoReturnDetailEntity poReturnDetailEntity : poReturnDetailList) {
            PoReturnEntity entity = poReturnEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), poReturnDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_99008);
            }
            PoReconciliationDetailDTO.AddDTO addDTO = new PoReconciliationDetailDTO.AddDTO();
            addDTO.setPoId(entity.getPurchaseOrderId());
            addDTO.setPoCode(entity.getPurchaseOrderCode());
            addDTO.setPoDetailId(poReturnDetailEntity.getPurchaseOrderDetailId());
            addDTO.setSupplierId(entity.getSupplierId());
            addDTO.setSupplierName(entity.getSupplierName());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceDetailId(poReturnDetailEntity.getId());
            addDTO.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
            addDTO.setBusinessStatus(entity.getConfirmStatus());
            addDTO.setConfirmDate(entity.getConfirmDate());
            addDTO.setSkuId(poReturnDetailEntity.getSkuId());
            addDTO.setReceiveQty(poReturnDetailEntity.getReturnQty() * -1);
            addDTO.setTaxPrice(poReturnDetailEntity.getReturnPrice());
            addDTO.setSettleOrgId(entity.getPurchaseOrgId());
            addDTO.setCurrency(poReturnDetailEntity.getCurrency());
            ReturnOrderSourceEnum returnOrderSourceEnum = Objects.equals(entity.getSourceType(), SourceTypeEnum.QC_INFO.getCode()) ?
                    ReturnOrderSourceEnum.QC : ReturnOrderSourceEnum.OTHER;
            addDTO.setReturnSourceType(returnOrderSourceEnum.getCode());
            addList.add(addDTO);
        }
        srmPoReconciliationFeign.add(addList);
    }

    /**
     * 批量反审核
     *
     * @param entity
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(PoReturnEntity entity,List<PoReturnDetailEntity> detailEntityList) {
        //已审核支持反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99003.msg);
        }
        //判断是否已生成采购订单
        List<PurchaseOrderEntity> poList = scmTaskFeign.listPoBySourceIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(poList)) {
            List<String> poCodeList = poList.stream().map(PurchaseOrderEntity::getCode).distinct().collect(Collectors.toList());
            throw new ServiceException(ApiError.ERROR_92245,String.join(",",poCodeList));
        }
        //判断是否已生成委外退料单
        List<SubcontractReturnEntity> subcontractReturnEntityList = subcontractReturnService.listBySourceIds(Collections.singletonList(entity.getId()));
        List<String> subCodeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subcontractReturnEntityList)) {
            subCodeList = subcontractReturnEntityList.stream().map(SubcontractReturnEntity::getCode).distinct().collect(Collectors.toList());
        }
        //判断是否已生成子件的采购退货单
        List<String> codeList = new ArrayList<>();
        if(StrUtil.isNotBlank(entity.getPurchaseOrderId())){
            PurchaseOrderEntity purchaseOrder = scmTaskFeign.getPurchaseOrderById(entity.getPurchaseOrderId());
            if (Objects.nonNull(purchaseOrder) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(purchaseOrder.getSubcontractType())){
                List<PoReturnEntity> childPoReturnList = this.listBySourceIds(Collections.singletonList(entity.getId()));
                if (CollectionUtils.isNotEmpty(childPoReturnList)){
                    codeList = childPoReturnList.stream().map(PoReturnEntity::getCode).distinct().collect(Collectors.toList());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(subCodeList) && CollectionUtils.isNotEmpty(codeList)){
            throw new ServiceException(ApiError.ERROR_92247,String.join(",",subCodeList), String.join(",",codeList));
        }else if (CollectionUtils.isNotEmpty(subCodeList)){
            throw new ServiceException(ApiError.ERROR_92244,String.join(",",subCodeList));
        }else if (CollectionUtils.isNotEmpty(codeList)){
            throw new ServiceException(ApiError.ERROR_92246,String.join(",",codeList));
        }
        List<String> poReturnDetailIdList = detailEntityList.stream().map(PoReturnDetailEntity::getId).collect(Collectors.toList());
        //对账单删除
        srmPoReconciliationFeign.deleteDetailBySourceDetailIdList(poReturnDetailIdList);

        List<PoReturnEntity> poReturnEntityList = Arrays.asList(entity);
        //TODO 待加审核流程

        //查询退货配置
        String confirmStatus = "";
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.PO_RETURN.getCode());
        CfgSettingValueDTO.PoReturnSettingDTO poReturnSettingDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.PoReturnSettingDTO.class);
        if (poReturnSettingDTO.getIsReturnConfirm()) {
            confirmStatus = PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus();
        } else {
            confirmStatus = PoReturnConfirmStatusEnum.CONFIRM.getStatus();
        }
        //修改状态为待提交
        lambdaUpdate()
                .set(PoReturnEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .set(PoReturnEntity::getApproveUserId, "")
                .set(PoReturnEntity::getApproveUserName, "")
                .set(PoReturnEntity::getApproveTime, null)
                .set(PoReturnEntity::getConfirmStatus, confirmStatus)
                .set(confirmStatus.equals(PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus()), PoReturnEntity::getConfirmDate, null)
                .set(confirmStatus.equals(PoReturnConfirmStatusEnum.CONFIRM.getStatus()),PoReturnEntity::getConfirmDate, LocalDate.now())
                .eq(PoReturnEntity::getId, entity.getId())
                .update();
        List<PurchaseOrderDetailEntity> list = new ArrayList<>();
            List<String> detailId = detailEntityList.stream().map(PoReturnDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
            if (CollectionUtils.isNotEmpty(purchaseOrderDetailEntities)) {
                //退料扣款
                if (entity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {

                    detailEntityList.forEach(returnOrderDetailEntity -> {
                        PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(returnOrderDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
                        PurchaseOrderDetailEntity purchaseOrderDetail = new PurchaseOrderDetailEntity ();
                        purchaseOrderDetail.setId(purchaseOrderDetailEntity.getId());
                        purchaseOrderDetail.setPurchaseAmount(purchaseOrderDetailEntity.getPurchaseAmount().add(returnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(returnOrderDetailEntity.getReturnQty())))));
                        list.add(purchaseOrderDetail);
                    });
                }

            }
        //退货补货退货id集合
        List<String> podIds = new ArrayList<>();
        if (ReturnModeEnum.REPLENISHMENT.getCode().equals(entity.getReturnMode())){
            podIds = detailEntityList.stream().map(PoReturnDetailEntity::getPurchaseOrderDetailId)
                    .distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(podIds)) {
            updateArrivalState(podIds);
        }

        unApproveInventory(Collections.singletonList(entity)); // 库存反审核操作

        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个采购退货单【%s】", entity.getCode()), ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), entity.getId(), "反审核操作");

        //发送旺店通
        poReturnEntityList.forEach(obj -> syncDisApprovePoReturnToWdt(obj, SyncOperateEnum.OPERATE_DISAPPROVE));
        //发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    public List<PoReturnEntity> listByPurchaseOrderIds(List<String> poIds) {
        if (CollectionUtils.isEmpty(poIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PoReturnEntity::getPurchaseOrderId, poIds).list();
    }

    /**
     * 将反审核通过的采购退货单转换为其他入库单推送到旺店通
     *
     * @param entity 采购退货单
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncDisApprovePoReturnToWdt(PoReturnEntity entity, SyncOperateEnum syncOperateEnum) {
        if(! "other".equals(entity.getSourceType())){
            log.info("非库存退货单无需推送旺店通：{}", entity);
            return;
        }
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getReturnWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        List<PoReturnDetailEntity> detailList = poReturnDetailService.getDetailByMainId(entity.getId());
        if(detailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }
        List<CreateOtherStockinRequest.GoodsList> goodsList = new ArrayList<>(detailList.size());
        for (PoReturnDetailEntity detailEntity : detailList) {
            CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getReturnQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getReturnWarehouseId());
            goodsList.add(goods);
        }
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_INSTOCK);
    }

    /**
     * 取消流程
     *
     * @param ids ids
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<PoReturnEntity> poReturnEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = poReturnEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != poReturnEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(PoReturnEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(PoReturnEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = poReturnEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("采购退货单【%s】取消流程", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    /**
     * 批量作废
     *
     * @param ids    ids
     * @param remark remark
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<PoReturnEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核不通过 待提交可以作废
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != warehouseReceiveList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        //修改状态为待提交
        lambdaUpdate().set(PoReturnEntity::getInvalidStatus, Boolean.TRUE)
                .set(PoReturnEntity::getInvalidRemark, remark)
                .set(PoReturnEntity::getInvalidTime, LocalDateTime.now())
                .in(PoReturnEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个采购退货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "作废操作");

        //发送金蝶
        sendPushTask(warehouseReceiveList,SyncOperateEnum.OPERATE_INVALID.getCode());
        return Boolean.TRUE;
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<PoReturnEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //待提交支持删除
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();

        if (count != warehouseReceiveList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除详情表
        poReturnDetailService.delete(ids);

        //发送金蝶
        sendPushTask(warehouseReceiveList,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表
        return this.removeByIds(ids);
    }

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    @Override
    public Boolean exportExcel(PurchaseReturnOrderDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("退货单", EXPORT_WMS_PURCHASE_RETURN_ORDER.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 采购订单-关联的退货订单
     *
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     **/
    @Override
    public List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> purchaseOrderRefReturn(String purchaseOrderId) {
        List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> orderRefReceiveDTOS = baseMapper.purchaseOrderRefReturn(purchaseOrderId);
        if (CollectionUtils.isEmpty(orderRefReceiveDTOS)) {
            return new ArrayList<>();
        }
        //获取采购单详情表id集合
        List<String> orderDetailIds = orderRefReceiveDTOS.stream().map(PurchaseReturnOrderDTO.OrderRefReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        for (PurchaseReturnOrderDTO.OrderRefReceiveDTO orderRefReceiveDTO : orderRefReceiveDTOS) {

            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(orderRefReceiveDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            orderRefReceiveDTO.setReturnModeName(ReturnModeEnum.getName(orderRefReceiveDTO.getReturnMode()));
            orderRefReceiveDTO.setProductName(purchaseOrderDetailEntity.getProductName());
            orderRefReceiveDTO.setApproveStatusName(ApproveStatusEnum.getName(orderRefReceiveDTO.getApproveStatus()));
            orderRefReceiveDTO.setInvalidStatusName(InvalidStatusEnum.getName(orderRefReceiveDTO.getInvalidStatus()));
        }
        return orderRefReceiveDTOS;
    }

    /**
     * 列表状态数量统计
     *
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>
     * @Author Luo_WG
     * @Date 2023/4/17 13:12
     **/
    @Override
    public List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> listCount(PermissionsDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        PoReturnStatusEnum[] values = PoReturnStatusEnum.values();
        List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> list = new ArrayList<>();
        for (PoReturnStatusEnum item : values) {
            PurchaseReturnOrderDTO.PagingParamDTO pagingParamDTO = new PurchaseReturnOrderDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            PurchaseReturnOrderDTO.ReturnOrderCountDTO resultDTO = new PurchaseReturnOrderDTO.ReturnOrderCountDTO();
            Integer count = MathUtil.ZERO;
            if (PoReturnStatusEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PoReturnStatusEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PoReturnStatusEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PoReturnStatusEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PoReturnStatusEnum.WAIT_FOR_ME_HANDLE.getCode().equals(item.getCode())) {
                List<SysPostUserEntity> postUserList = sysPostFeign.getPostUserByUserId(userInfo.getUid());
                List<String> postIds = postUserList.stream().map(req -> req.getPostId()).distinct().collect(Collectors.toList());
                List<PoReturnEntity> entityList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(postIds)) {
                    entityList = lambdaQuery()
                            .in(PoReturnEntity::getUnusualHandleUserId, postIds)
                            .eq(PoReturnEntity::getConfirmStatus, PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode())
                            .list();
                }
                count = entityList.size();
            }

            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public List<PoReturnEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery()
                .in(PoReturnEntity::getSourceId, sourceIds)
                .eq(PoReturnEntity::getInvalidStatus, Boolean.FALSE)
                .list();
    }

    /**
     * 获取退货数量
     *
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     **/
    @Override
    public List<PurchaseReturnOrderDTO.GetReturnQtyDTO> getReturnQty(String purchaseOrderId) {
        return baseMapper.getReturnQty(purchaseOrderId);
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
                .eq(PoReturnEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), PoReturnEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    /**
     * 批量生成退货单
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-25 11:09
     */
    @Override
    public Boolean batchAdd(List<PurchaseReturnOrderDTO.AddDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (PurchaseReturnOrderDTO.AddDTO item : list) {
                this.add(item);
            }
        }
        return Boolean.TRUE;

    }

    /**
     * 修改到货状态
     *
     * @param podIds
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/28 11:37
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void updateArrivalState(List<String> podIds) {
        //采购订单明细
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        //采购退货明细
        List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
        //收货单明细
        List<WarehouseReceiveDetailEntity> receiveDetailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        //入库单明细
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailByPodIds(podIds);

        //委外订单下采购订单
        List<String> sourceDetailIds = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPodBySourceDetailIds(sourceDetailIds);

        //委外订单
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByIds(sourceDetailIds);

        List<PurchaseOrderDetailEntity> list = new ArrayList<>();
        for (PurchaseOrderDetailEntity orderDetailEntity : purchaseOrderDetailEntities) {
            //库存退货的退货补货数量
            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(orderDetailEntity.getId()) 
                            && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                            && !ReturnOrderSourceEnum.QC.getCode().equals(obj.getSourceType())
                            && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode()))
                    .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //整个退货补货数量
            Integer allReturnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(orderDetailEntity.getId())
                            && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                            && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode()))
                    .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //收货数量
            Integer receiveQty = receiveDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(orderDetailEntity.getId())
                            && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()))
                    .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            //入库数量
            Integer inStockQty = poInstockDetailList.stream().filter(obj -> StrUtil.equals(obj.getPurchaseOrderDetailId(), orderDetailEntity.getId())
                            && StrUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()))
                    .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            //采购数量
            Integer purchaseQty = orderDetailEntity.getPurchaseQty();

            /**
             * 执行状态变更节点：
             * 1、收货单审核、反审
             * 2、入库单审核、反审
             * 3、退货单审核、反审
             * 执行状态变更逻辑：
             * 收货单数量并且入库单数量为0，则更新为已确认
             * (收货单数量>0 并且 采购数量+退货补货数量>收货数量)或者(入库单数量>0 并且 采购数量+库存退货的退货补货数量>入库数量)，则更新为送货中
             * (收货单数量>0并且采购数量+退货补货数量=收货数量)或(入库单数量>0并且采购数量+库存退货的退货补货数量=入库数量)，则更新为已完成
             */

            //订单执行状态
            String executionStatus = "";
            if (MathUtil.compareTo(receiveQty,MathUtil.ZERO) == MathUtil.ZERO
                    && MathUtil.compareTo(inStockQty,MathUtil.ZERO) == MathUtil.ZERO) {
               //已确认
               executionStatus = ExecutionStatusEnum.CONFIRM.getCode();
            }
            if((receiveQty > MathUtil.ZERO && MathUtil.add(purchaseQty,allReturnQty) > receiveQty)
                    || (inStockQty > MathUtil.ZERO &&  MathUtil.add(purchaseQty,returnQty) > inStockQty)) {
                //送货中
                executionStatus = ExecutionStatusEnum.DELIVERY.getCode();
            }
            if((receiveQty > MathUtil.ZERO && MathUtil.compareTo(MathUtil.add(purchaseQty,allReturnQty),receiveQty) == MathUtil.ZERO)
                    || (inStockQty > MathUtil.ZERO && MathUtil.compareTo(MathUtil.add(purchaseQty,returnQty),inStockQty)  == MathUtil.ZERO)) {
                //已完成
                executionStatus = ExecutionStatusEnum.FINISH.getCode();
            }
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = new PurchaseOrderDetailEntity();
            purchaseOrderDetailEntity.setId(orderDetailEntity.getId());
            purchaseOrderDetailEntity.setExecutionStatus(executionStatus);
            purchaseOrderDetailEntity.setPurchaseOrderId(orderDetailEntity.getPurchaseOrderId());
            purchaseOrderDetailEntity.setSourceDetailId(orderDetailEntity.getSourceDetailId());
            purchaseOrderDetailEntity.setReceiveQty(receiveQty);

            //查询委外到货状态
            List<String> purchaseOrderDetailIds = purchaseOrderDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(orderDetailEntity.getSourceDetailId())).map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
            Integer subQty = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(orderDetailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(MathUtil.ZERO);
            String subArrivalStatus = warehouseReceiveDetailService.getSubArrivalStatus(purchaseOrderDetailIds, subQty);
            purchaseOrderDetailEntity.setSubArrivalStatus(subArrivalStatus);
            list.add(purchaseOrderDetailEntity);
            scmTaskFeign.updatePoArrivalStatus(purchaseOrderDetailEntity);

        }
    }

    public static void main(String[] args) {
        Integer s = 4444444;
        System.out.println(s == 4444444 );
    }

    /**
     * 下推 退货单
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-08 11:05
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generatePurchaseReturnOrder(PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> list = dto.getList();
        //查询实退数量
        List<String> poIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getPurchaseOrderId).collect(Collectors.toList());
        List<String> podIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PoInstockDetailEntity> stockInSkuList = poInstockDetailService.listDetailByPodIds(podIds);

        List<PurchaseReturnOrderDTO.AddDTO> addList = new ArrayList<>();
        String type = SourceTypeEnum.PURCHASE_ORDER.getCode();
        //采购订单
        List<PurchaseOrderDTO.PurchaseOrderInfoDTO> entityList = scmTaskFeign.getByOrderIds(poIds);

        //采购订单明细
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }

        Map<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSourceId().concat(obj.getReturnMode())));
        for (Map.Entry<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> entry : map.entrySet()) {
            List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> value = entry.getValue();
            PoInstockDTO.GeneratePurchaseReturnOrderDTO purchaseReturnOrderDTO = value.get(0);
            PurchaseReturnOrderDTO.AddDTO addDTO = new PurchaseReturnOrderDTO.AddDTO();
            //采购订单
            PurchaseOrderDTO.PurchaseOrderInfoDTO purchaseOrderEntity = entityList.stream().filter(obj -> obj.getPurchaseOrderId().equals(purchaseReturnOrderDTO.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98025);
            }

            String orgId = entityList.stream().filter(r -> r.getPurchaseOrderId().equals(purchaseReturnOrderDTO.getPurchaseOrderId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getReceiveOrgId())).orElse("");
            addDTO.setReturnOrgId(orgId);
            addDTO.setBillDate(LocalDate.now());
            addDTO.setSourceType(type);
            addDTO.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
            addDTO.setSourceId(purchaseReturnOrderDTO.getSourceId());
            addDTO.setPurchaseOrderId(purchaseOrderEntity.getPurchaseOrderId());
            addDTO.setReturnWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());
            addDTO.setReturnRemark(purchaseReturnOrderDTO.getRemark());
            addDTO.setSupplierId(purchaseOrderEntity.getSupplierId());
            addDTO.setReturnMode(purchaseReturnOrderDTO.getReturnMode());
            addDTO.setSourceId(purchaseReturnOrderDTO.getSourceId());
            addDTO.setReturnUserId(purchaseReturnOrderDTO.getReturnUserId());
            addDTO.setPurchaseUserId(purchaseReturnOrderDTO.getPurchaseUserId());
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (PoInstockDTO.GeneratePurchaseReturnOrderDTO detail : value) {
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
                //订单明细数据校验
                String skuNos = purchaseOrderDetailList.stream().filter(obj -> !StrUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus())
                        )
                        .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                if (StrUtil.isNotBlank(skuNos)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,purchaseOrderEntity.getCode(),skuNos);
                }

                //验证退货数量
                Integer stockInQty = stockInSkuList.stream().filter(s -> s.getSkuId().equals(detail.getSkuId()) &&
                        detail.getSourceDetailId().equals(s.getPurchaseOrderDetailId()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO,Integer::sum);
                if (ObjectUtils.isEmpty(stockInQty)) {
                    throw new ServiceException(1, String.format("SKU【%s】未找到对应数量", detail.getSkuNo()));
                }
                if (MathUtil.compareTo(detail.getRealityReturnQty(), stockInQty) > 0) {
                    throw new ServiceException(1, String.format("SKU【%s】实退数量不能大于【%s】", detail.getSkuNo(), stockInQty));
                }
                BeanMapperUtils.copy(detail, addDetailDTO);
                addDetailDTO.setReturnQty(detail.getRealityReturnQty());
                addDetailDTO.setWarehouseLocation(detail.getWarehouseLocation());
                addDetailDTO.setReturnPrice(detail.getTaxPrice());
                addDetailList.add(addDetailDTO);
            }
            addDTO.setPurchasePriceDetailList(addDetailList);
            addList.add(addDTO);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            for (PurchaseReturnOrderDTO.AddDTO item : addList) {
                this.add(item);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseReturnOrderDTO.SupplierReturnDTO> getReturnInfo(PurchaseReturnOrderDTO.SupplierReturnParamDTO params) {
        return this.baseMapper.getReturnInfo(params);
    }

    @Override
    public String checkSkuInventory(PurchaseReturnOrderDTO.AddDTO dto, List<PurchaseReturnOrderDetailDTO.AddDTO> detailList) {
        // 只有库存退货时会减少可用
        String sourceType = dto.getSourceType();
        if(Objects.equals(sourceType, SourceTypeEnum.QC_INFO.getCode())) {
            log.warn("质检退货，不存在可用缺货");
            return null;
        }
        StringBuffer errmsg = new StringBuffer("");

        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()): Lists.newArrayList();

        List<String> skuIds = detailList.stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));

        String warehouseId = dto.getReturnWarehouseId();
        List<String> warehouseLocationList = detailList.stream().map(r->StrUtils.null2EmptyWithTrim(r.getWarehouseLocation())).distinct().collect(Collectors.toList());

        Map<String, List<PurchaseReturnOrderDetailDTO.AddDTO>> multiInventoryMap = detailList.stream().collect(
                Collectors.groupingBy(r -> r.getSkuId() + "-" + StrUtils.null2EmptyWithTrim(r.getWarehouseLocation()), Collectors.toList()));

        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIds);
        skuInventoryDTO.setWarehouseIdList(Arrays.asList(warehouseId));
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //可用数量
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(warehouseId));

        multiInventoryMap.forEach((key, detailGroupList)->{
            String skuId = detailGroupList.get(0).getSkuId();
            String warehouseLocation = StrUtils.null2EmptyWithTrim(detailGroupList.get(0).getWarehouseLocation());

            String skuNo = "";
            if(skuMap.containsKey(skuId) && CollUtil.isNotEmpty(skuMap.get(skuId))) {
                SkuVO skuVO = skuMap.get(skuId).get(0);
                skuNo = skuVO.getSkuNo();
            }
            // 合计实退数量
            int sumQty = detailGroupList.stream().mapToInt(PurchaseReturnOrderDetailDTO.AddDTO::getReturnQty).sum();
            log.warn("sku id【{}】库位【{}】 合计实退数量【{}】",  warehouseId, skuId, warehouseLocation, sumQty);
            /*
            List<InventoryQtyDTO.SkuInventoryTotalDTO> inventoryList = inventoryService.listSkuInventory(Lists.newArrayList(skuId), warehouseId,
                    warehouseLocation, InventoryStatusEnum.USABLE.getCode());
            Integer curInventoryQty = inventoryList.stream().filter(r -> Objects.equals(r.getSkuId(), skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
             */
            //即时库存
            Integer curInventoryQty = skuInventoryList.stream().filter(r ->Objects.equals(r.getSkuId(), skuId)
                    && Objects.equals(r.getWarehouseId(), warehouseId)
                    && Objects.equals(r.getWarehouseLocationId(), warehouseLocation)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);

            log.warn("仓库id【{}】sku id【{}】库位【{}】 合计实退数量【{}】实时库存数量", warehouseId, skuId, warehouseLocation,
                    sumQty, curInventoryQty);
            Boolean isScarce = curInventoryQty < sumQty;
            if(isScarce && !ignoreInventorySkuIds.contains(skuId)) {
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(warehouseId, (v) -> warehouseService.detailWithCache(v));
                String warehouseName = Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId()) ? warehouseDetail.getName() : "";
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(warehouseLocation)).findFirst().orElse(new WarehouseLocationEntity());


                String msg = StrUtil.format("仓库【{}】仓位【{}】SKU【{}】【缺货：{}个】", warehouseName, warehouseLocationEntity.getName(), skuNo, (sumQty - curInventoryQty));
                errmsg.append(msg).append("</br>");
            }
        });
        return errmsg.toString();
    }

    @Override
    public Boolean autoGeneratePurchaseOrder(BaseIdsDTO.IdsDTO dto) {
        List<PoReturnEntity> list = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        //判断单据是否审核完成
        String codes = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(PoReturnEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(codes)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_REF_PO_APPROVE,codes);
        }
        //下推采购订单
        autoAddPurchaseOrder(list, Boolean.FALSE);
        return Boolean.TRUE;
    }

    /**
     * 更新库存信息
     * @param list
     */
    public void updateInventoryTransCore (List<PoReturnEntity> list) {
        List<String> ids = list.stream().map(PoReturnEntity::getId).distinct().collect(Collectors.toList());
        Map<String, PoReturnEntity> mainMap = list.stream().collect(Collectors.toMap(PoReturnEntity::getId, Function.identity()));
        List<PoReturnDetailEntity> detailEntityList = poReturnDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException("未找到采购退货单明细信息");
        }
        // 明细按主单id分组
        Map<String,List<PoReturnDetailEntity>> detailMap = detailEntityList.stream().collect(Collectors.groupingBy(PoReturnDetailEntity::getMainId));
        // 分3种情况，采购退货(库存退货，退货补货)/采购退货(库存退货，退货退款)/采购退货（质检退货，退货补货）/采购退货（质检退货，退货退款）
        Map<String, PoReturnEntity> replenishmentInventoryMap = Maps.newHashMap();//采购退货(库存退货，退货补货，有采购订单)
        Map<String, PoReturnEntity> deductionInventoryMap = Maps.newHashMap();//采购退货(库存退货，退货退款，有采购订单)
        Map<String, PoReturnEntity> replenishmentQcMap = Maps.newHashMap();//采购退货(质检退货，退货补货)
        Map<String, PoReturnEntity> deductionQcRefundMap = Maps.newHashMap();//采购退货(质检退货，退货退款)
        Map<String, PoReturnEntity> inventoryNoPurchaseMap = Maps.newHashMap();//采购退货(库存退货，无采购订单)

        List<PoReturnDetailEntity> replenishmentInventoryDetailList = Lists.newArrayList();//采购退货(库存退货，退货补货)明细
        List<PoReturnDetailEntity> deductionInventoryDetailList = Lists.newArrayList();//采购退货(库存退货，退货退款)明细
        List<PoReturnDetailEntity> replenishmentQcDetailList = Lists.newArrayList();//采购退货(质检退货，退货补货)明细
        List<PoReturnDetailEntity> deductionQcRefundDetailList = Lists.newArrayList();//采购退货(质检退货，退货退款)明细
        List<PoReturnDetailEntity> inventoryNoPurchaseDetailList = Lists.newArrayList();//采购退货(库存退货，无采购订单)明细

        mainMap.forEach((mainId, purchaseReturnOrder)->{
            String sourceType = purchaseReturnOrder.getSourceType();
            if(!Objects.equals(sourceType, SourceTypeEnum.QC_INFO.getCode())) { // 库存退货
                if(StrUtils.isEmpty(purchaseReturnOrder.getPurchaseOrderId())) {
                    inventoryNoPurchaseMap.put(mainId, purchaseReturnOrder);
                    inventoryNoPurchaseDetailList.addAll(detailMap.get(mainId));
                } else {
                    String returnMode = purchaseReturnOrder.getReturnMode(); // 退货方式
                    if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())) { // 退货补货
                        replenishmentInventoryMap.put(mainId, purchaseReturnOrder);
                        replenishmentInventoryDetailList.addAll(detailMap.get(mainId));
                    } else if (Objects.equals(returnMode, ReturnModeEnum.DEDUCTION.getCode())) { // 退货退款
                        deductionInventoryMap.put(mainId, purchaseReturnOrder);
                        deductionInventoryDetailList.addAll(detailMap.get(mainId));
                    }
                }
            } else { // 质检退货
                String returnMode = purchaseReturnOrder.getReturnMode(); // 退货方式
                if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())) { // 退货补货
                    replenishmentQcMap.put(mainId, purchaseReturnOrder);
                    replenishmentQcDetailList.addAll(detailMap.get(mainId));
                } else if (Objects.equals(returnMode, ReturnModeEnum.DEDUCTION.getCode())) { // 退货退款
                    deductionQcRefundMap.put(mainId, purchaseReturnOrder);
                    deductionQcRefundDetailList.addAll(detailMap.get(mainId));
                }
            }
        });
        replenishmentInventory(replenishmentInventoryMap, replenishmentInventoryDetailList); //采购退货(库存退货，退货补货)
        deductionInventory(deductionInventoryMap, deductionInventoryDetailList);//采购退货(库存退货，退货退款)
        replenishmentQcInventory(replenishmentQcMap, replenishmentQcDetailList);//采购退货(质检退货，退货补货)
        deductionQcInventory(deductionQcRefundMap, deductionQcRefundDetailList);//采购退货(质检退货，退货扣款)
        inventoryNoPo(inventoryNoPurchaseMap, inventoryNoPurchaseDetailList);//采购退货（库存退货， 无采购单）
    }

    /**
     * 采购退货(库存退货，退货补货) 库存操作
     * @param replenishmentInventoryMap
     * @param replenishmentInventoryDetailList
     */
    public void replenishmentInventory(Map<String, PoReturnEntity> replenishmentInventoryMap,
                                       List<PoReturnDetailEntity> replenishmentInventoryDetailList) { //采购退货(库存退货，退货补货)
        if(CollUtil.isNotEmpty(replenishmentInventoryDetailList)) { //采购退货(库存退货，退货补货)明细
            List<InOutStockDTO> inList = new ArrayList<>();
            List<InOutStockDTO> useList = new ArrayList<>();
            replenishmentInventoryDetailList.forEach(detail->{
                PoReturnEntity poReturnEntity = replenishmentInventoryMap.get(detail.getMainId());
                // 使用补货数量 增加的在途
                InOutStockDTO inStockDTO = InOutStockDTO.initByReturnOrder(poReturnEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReplenishQty(), InventoryStatusEnum.IN_TRANSIT);
                inList.add(inStockDTO);
                // 使用实退数量 减少可用
                InOutStockDTO inOutStockDTO = InOutStockDTO.initByReturnOrder(poReturnEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReturnQty(), InventoryStatusEnum.USABLE);
                useList.add(inOutStockDTO);
            });
            if (CollectionUtils.isNotEmpty(inList)){
                InventoryInOutStockRuleDTO inTransitRule = new InventoryInOutStockRuleDTO();
                inTransitRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP.getCode());
                inTransitRule.setParamList(inList);
                inTransitRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.IN_TRANSIT, InventoryModeEnum.IN_STOCK)));
                inventoryTransCoreService.approveByRule(inTransitRule);
            }
            if (CollectionUtils.isNotEmpty(useList)){
                InventoryInOutStockRuleDTO usableRule = new InventoryInOutStockRuleDTO();
                usableRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP.getCode());
                usableRule.setParamList(useList);
                usableRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.USABLE, InventoryModeEnum.OUT_STOCK)));
                inventoryTransCoreService.approveByRule(usableRule);
            }

        }
    }

    /**
     * 采购退货(库存退货，退货退款) 库存操作
     * @param deductionInventoryMap
     * @param deductionInventoryDetailList
     */
    public void deductionInventory(Map<String, PoReturnEntity> deductionInventoryMap,
                                   List<PoReturnDetailEntity> deductionInventoryDetailList) { //采购退货(库存退货，退货退款)
        if(CollUtil.isNotEmpty(deductionInventoryDetailList)) {
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REF.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            deductionInventoryDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PoReturnEntity poReturnEntity = deductionInventoryMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(poReturnEntity.getId());
                inOutStockDTO.setSourceCode(poReturnEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(poReturnEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                // 扣除捕获数量
                inOutStockDTO.setQty(detail.getReturnQty());
                inOutStockDTO.setWarehouseId(poReturnEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setParamList(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
        }
    }


    /**
     * 采购退货(质检退货，退货补货) 库存操作
     * @param replenishmentQcMap
     * @param replenishmentQcDetailList
     */
    public void replenishmentQcInventory(Map<String, PoReturnEntity> replenishmentQcMap,
                                         List<PoReturnDetailEntity> replenishmentQcDetailList) { //采购退货(质检退货，退货补货)
        if(CollUtil.isNotEmpty(replenishmentQcDetailList)) {
            replenishmentQcDetailList.forEach(detail->{
                // 使用补货数量 增加的在途
                PoReturnEntity poReturnEntity = replenishmentQcMap.get(detail.getMainId());
                InOutStockDTO inStockDTO = InOutStockDTO.initByReturnOrder(poReturnEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReplenishQty(), InventoryStatusEnum.IN_TRANSIT);
                InventoryInOutStockRuleDTO inTransitRule = new InventoryInOutStockRuleDTO();
                inTransitRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_QC.getCode());
                inTransitRule.setParamList(Lists.newArrayList(inStockDTO));
                inTransitRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.IN_TRANSIT, InventoryModeEnum.IN_STOCK)));
                inventoryTransCoreService.approveByRule(inTransitRule);
                // 使用实退数量 减少待检
                InOutStockDTO outStockDTO = InOutStockDTO.initByReturnOrder(poReturnEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReturnQty(), InventoryStatusEnum.WAIT_QC);
                InventoryInOutStockRuleDTO waitQcRule = new InventoryInOutStockRuleDTO();
                waitQcRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_QC.getCode());
                waitQcRule.setParamList(Lists.newArrayList(outStockDTO));
                waitQcRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.WAIT_QC, InventoryModeEnum.OUT_STOCK)));
                inventoryTransCoreService.approveByRule(waitQcRule);
            });

        }
    }

    /**
     * 采购退货(质检退货，退货退款) 库存操作
     * @param deductionQcMap
     * @param deductionQcDetailList
     */
    public void deductionQcInventory(Map<String, PoReturnEntity> deductionQcMap,
                                     List<PoReturnDetailEntity> deductionQcDetailList) { //采购退货(质检退货，退货退款)
        if(CollUtil.isNotEmpty(deductionQcDetailList)) {
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_QC_REF.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            deductionQcDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PoReturnEntity poReturnEntity = deductionQcMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(poReturnEntity.getId());
                inOutStockDTO.setSourceCode(poReturnEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(poReturnEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                // 使用实退数量 减少待检
                inOutStockDTO.setQty(detail.getReturnQty());
                inOutStockDTO.setWarehouseId(poReturnEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setParamList(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
        }
    }

    public void unApproveInventory(List<PoReturnEntity> poReturnEntityList) {
        // 只有库存退货、质检退货（退货补货）才需要反审核
        List<String> unApproveIds = Lists.newArrayList();
        for (PoReturnEntity purchaseReturnOrder : poReturnEntityList) {
            String sourceType = purchaseReturnOrder.getSourceType();
            if(!Objects.equals(sourceType, SourceTypeEnum.QC_INFO.getCode())) {
                // 库存退货
                unApproveIds.add(purchaseReturnOrder.getId());
            } else { // 质检退货
                String returnMode = purchaseReturnOrder.getReturnMode();
                // 退货方式
                /*
                if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())) {
                // 退货补货
                    unApproveIds.add(purchaseReturnOrder.getId());
                }
                 */
                // 库存退货、质检退货都需要反审核
                unApproveIds.add(purchaseReturnOrder.getId());
            }
        }
        if(CollUtil.isEmpty(unApproveIds)) {
            return;
        }
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, unApproveIds);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
    }

    /**
     * @description: 自动生成
     * @author Will
     * @date: 2023/8/7 10:48
     * @param poReturnEntityList
     */
    public void autoAddPurchaseOrder (List<PoReturnEntity> poReturnEntityList, Boolean isAuto) {
        if (CollectionUtils.isEmpty(poReturnEntityList)) {
            return;
        }
        List<String> ids = poReturnEntityList.stream().map(PoReturnEntity::getId).collect(Collectors.toList());
        //判断是否已生成采购订单
        List<PurchaseOrderEntity> poList = scmTaskFeign.listPoBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(poList)) {
            List<String> sourceIds = poList.stream().map(PurchaseOrderEntity::getSourceId).collect(Collectors.toList());
            String codes = poReturnEntityList.stream().filter(obj -> sourceIds.contains(obj.getId())).map(PoReturnEntity::getCode).distinct().collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(codes)) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_REF_PO,codes);
            }
        }
        List<PoReturnEntity> returnList = poReturnEntityList
                .stream()
                .filter(obj -> StringUtils.isBlank(obj.getPurchaseOrderId()) && ReturnModeEnum.REPLENISHMENT.getCode().equals(obj.getReturnMode())
        ).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(returnList) && isAuto) {
            log.info("采购退货单下存在采购订单或者退货扣款的采购退货单不支持自动生成");
            return;
        }
        //支持生成退货补货订单
        if (!isAuto){
            returnList = poReturnEntityList;
        }
        log.info("自动生成退货采购订单，退货单号 = {}",returnList.stream().map(PoReturnEntity::getCode).collect(Collectors.joining(",")));

        //部门信息
        List<String> purchaseUserIds = returnList.stream().filter(obj -> StringUtils.isNotBlank(obj.getPurchaseUserId())).map(PoReturnEntity::getPurchaseOrderId).collect(Collectors.toList());
        List<SysDepartmentUserNumberDTO> departList = sysUserFeign.listDeptUserByUserIdList(purchaseUserIds);

        //供应商信息
        List<String> supplierIds = returnList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId())).map(PoReturnEntity::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIds);
        if (CollectionUtils.isEmpty(supplierList)) {
            log.error("未找到供应商信息，supplierIds = {} ",supplierIds);
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商联系人信息
        List<String> supplierContactIds = returnList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierContactId())).map(PoReturnEntity::getSupplierContactId).collect(Collectors.toList());
        List<SupplierContactEntity> supplierContactList = scmTaskFeign.listSupplierContactByIds(supplierContactIds);

        //退货明细信息
        List<String> mainIds = returnList.stream().map(PoReturnEntity::getId).collect(Collectors.toList());
        List<PoReturnDetailEntity> detailList = poReturnDetailService.listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        //主体公司信息
        List<BaseIdDTO.CodeDTO> companyCodeList = sysUserFeign.listAccountingCompanyByCodeList(Arrays.asList(companyCode));
        if (CollectionUtils.isEmpty(companyCodeList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }

        for (PoReturnEntity entity : returnList) {
            PurchaseOrderDTO.AddDTO addDTO = new PurchaseOrderDTO.AddDTO();
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
            addDTO.setType(PurchaseOrderTypeEnum.ENUM_RETURN.getCode());
            addDTO.setPurchaseDate(LocalDate.now());
            addDTO.setPurchaseUserId(entity.getPurchaseUserId());
            //采购部门
            if (CollectionUtils.isNotEmpty(departList)) {
                String deptId = departList.stream().filter(obj -> obj.getUserId().equals(entity.getPurchaseUserId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getDepartmentId())).orElse("");
                addDTO.setPurchaseDeptId(deptId);
            }
            //采购组织
            addDTO.setPurchaseOrgId(entity.getPurchaseOrgId());
            addDTO.setDeliveryWarehouseId("");
            //采购供应商信息
            PurchaseOrderSupplierDTO.AddDTO supplierDTO = new PurchaseOrderSupplierDTO.AddDTO();
            supplierDTO.setSupplierId(entity.getSupplierId());
            supplierDTO.setSupplierContactId(entity.getSupplierContactId());
            //供应商信息
            if (CollectionUtils.isNotEmpty(supplierList)) {
                SupplierEntity supplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(entity.getSupplierId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(supplierEntity)) {
                    log.error("未找到供应商信息，supplierId = {} ",entity.getSupplierId());
                    throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
                }
                supplierDTO.setPaymentCondition(supplierEntity.getPaymentCondition());
                supplierDTO.setPayMethodId(supplierEntity.getPayMethodId());
                supplierDTO.setPayCurrency(supplierEntity.getPayCurrency());
            }
            //供应商联系人信息
            if (CollectionUtils.isNotEmpty(supplierContactList)) {
                SupplierContactEntity supplierContactEntity = supplierContactList.stream().filter(obj -> obj.getId().equals(entity.getSupplierContactId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(supplierContactEntity)) {
                    //联系人电话
                    supplierDTO.setContactTelNumber(supplierContactEntity.getTelNumber());
                }
            }
            addDTO.setPurchaseOrderSupplierDTO(supplierDTO);
            //明细信息
            List<PoReturnDetailEntity> details = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(details)) {
                log.error("未找到退货明细信息，mainId = {} ",entity.getId());
                throw new ServiceException(ApiError.ERROR_99008);
            }
            List<PurchaseOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (PoReturnDetailEntity detailEntity:details) {
                PurchaseOrderDetailDTO.AddDTO addDetailDTO = new PurchaseOrderDetailDTO.AddDTO();
                addDetailDTO.setSourceDetailId(detailEntity.getId());
                addDetailDTO.setSkuId(detailEntity.getSkuId());
                addDetailDTO.setSkuNo(detailEntity.getSkuNo());
                int purchaseQty = 0;
                if (ReturnModeEnum.DEDUCTION.getCode().equals(entity.getReturnMode())){
                    purchaseQty = detailEntity.getDeductAmountQty();
                }else if (ReturnModeEnum.REPLENISHMENT.getCode().equals(entity.getReturnMode())){
                    purchaseQty = detailEntity.getReplenishQty();
                }
                addDetailDTO.setPurchaseQty(purchaseQty);
                addDetailDTO.setPlanDeliveryDate(null);
                addDetailDTO.setRemark(detailEntity.getRemark());
                addDetailDTO.setCurrency(detailEntity.getCurrency());
                addDetailDTO.setCurrencySymbol(detailEntity.getCurrencySymbol());
                addDetailList.add(addDetailDTO);
            }
            addDTO.setDetails(addDetailList);
            //新增采购订单
            String code = scmTaskFeign.addPurchaseOrder(addDTO);
            //操作日志
            operateLogService.addModuleOperateLog(StrUtil.format("下推生成采购订单【{}】", code), ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), entity.getId(), "下推采购订单");
        }
    }

    /**
     * 采购退货(库存退货，无采购单) 库存操作
     * @param inventoryMap
     * @param inventoryDetailList
     */
    public void inventoryNoPo(Map<String, PoReturnEntity> inventoryMap,
                                       List<PoReturnDetailEntity> inventoryDetailList) { //采购退货(库存退货，无采购单)
        if(CollUtil.isNotEmpty(inventoryDetailList)) { //采购退货(库存退货，无采购单)明细
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP_NO_PURCHASE.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            inventoryDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PoReturnEntity poReturnEntity = inventoryMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(poReturnEntity.getId());
                inOutStockDTO.setSourceCode(poReturnEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(poReturnEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                // 使用实退数量 减少可用
                inOutStockDTO.setQty(detail.getReturnQty());
                inOutStockDTO.setWarehouseId(poReturnEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setParamList(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
        }
    }

    @Override
    public PagingVO<PurchaseReturnOrderDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<PurchaseReturnOrderDTO.PdaPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        PurchaseReturnOrderDTO.PdaPagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }
        IPage<PurchaseReturnOrderDTO.PdaPagingViewDTO> pageData = this.baseMapper.pdaPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<PurchaseReturnOrderDTO.PdaPagingViewDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = poReturnDetailService.listByMainIds(ids);
        for (PurchaseReturnOrderDTO.PdaPagingViewDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<PoReturnDetailEntity> detailEntities = purchaseReturnOrderDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<PurchaseReturnOrderDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, PurchaseReturnOrderDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseReturnOrderDTO.PdaReturnOrderCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<PurchaseReturnOrderDTO.PdaReturnOrderCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            PurchaseReturnOrderDTO.PagingParamDTO pagingParamDTO = new PurchaseReturnOrderDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            PurchaseReturnOrderDTO.PdaReturnOrderCountDTO resultDTO = new PurchaseReturnOrderDTO.PdaReturnOrderCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String pdaAdd(PurchaseReturnOrderDTO.AddDTO dto) {
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
            List<String> orderDetailIds = detailList.stream().map(req -> req.getPurchaseOrderDetailId()).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.AddDTO updateDTO : detailList) {
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                    throw new ServiceException(ApiError.PURCHASE_SKU_NOT_EXIST, updateDTO.getSkuNo());
                }
            }

            List<PurchaseReturnOrderDetailDTO.AddDTO> addDTOList = new ArrayList<>();
            List<PurchaseReturnOrderDetailDTO.AddDTO> purchaseReturnOrderDetailList = dto.getPurchasePriceDetailList();
            List<String> poDetailIds = purchaseReturnOrderDetailList.stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.listPurchaseOrderDetailById(poDetailIds);
            List<PurchaseOrderDetailEntity> detailEntityListByPoId = scmTaskFeign.listByPurchaseOrderIds(Arrays.asList(dto.getPurchaseOrderId()));

            List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = poReturnDetailService.listReturnOrderDetailByPodIds(poDetailIds);
            for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : purchaseReturnOrderDetailList) {
                PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailEntityList.stream().filter(req -> req.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(detailEntity)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_DETAIL_SKU_NOT_EXIST, addDTO.getSkuNo());
                }

                List<PurchaseOrderDetailEntity> detailEntityList = detailEntityListByPoId.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
                //校验sku是否有重复，重复需要拆单
                if (detailEntityList.size() > MathUtil.ONE) {
                    List<String> podIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
                    List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailByPodIds(podIds);
                    List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
                    Integer returnQty = addDTO.getReturnQty();
                    for (PurchaseOrderDetailEntity entity : detailEntityList) {
                        Integer stockInQty = poInstockDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        //已退货
                        Integer alreadyReturnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

                        if (alreadyReturnQty >= stockInQty) {
                            continue;
                        }

                        PurchaseReturnOrderDetailDTO.AddDTO addSkuDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
                        addSkuDTO.setPurchaseOrderDetailId(entity.getId());
                        addSkuDTO.setRemark(addDTO.getRemark());
                        addSkuDTO.setSkuId(addDTO.getSkuId());
                        addSkuDTO.setSkuNo(addDTO.getSkuNo());
                        addSkuDTO.setReplenishQty(addDTO.getReplenishQty());
                        addSkuDTO.setDeductAmountQty(addDTO.getDeductAmountQty());
                        addSkuDTO.setSkuNo(addDTO.getSkuNo());
                        if (returnQty > (stockInQty - alreadyReturnQty) && !detailEntityList.get(detailEntityList.size()-1).getId().equals(entity.getId())) {
                            returnQty = returnQty - (stockInQty - alreadyReturnQty);
                            addSkuDTO.setReturnQty(stockInQty - alreadyReturnQty);
                            addDTOList.add(addSkuDTO);
                        } else {
                            addSkuDTO.setReturnQty(returnQty);
                            addDTOList.add(addSkuDTO);
                            break;
                        }
                        addDTO.setReplenishQty(0);
                        addDTO.setDeductAmountQty(0);
                    }
                } else {
                    addDTOList.add(addDTO);
                }
            }
            dto.setPurchasePriceDetailList(addDTOList);
        }
        return this.add(dto);
    }

    @Override
    public Boolean pdaUpdate(PurchaseReturnOrderDTO.UpdateDTO dto) {
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
            List<String> orderDetailIds = detailList.stream().map(req -> req.getPurchaseOrderDetailId()).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                    throw new ServiceException(ApiError.PURCHASE_SKU_NOT_EXIST, updateDTO.getSkuNo());
                }
            }
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> addDTOList = new ArrayList<>();
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> purchaseReturnOrderDetailList = dto.getPurchasePriceDetailList();
            List<String> poDetailIds = purchaseReturnOrderDetailList.stream().map(PurchaseReturnOrderDetailDTO.UpdateDTO::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.listPurchaseOrderDetailById(poDetailIds);
            List<PurchaseOrderDetailEntity> detailEntityListByPoId = scmTaskFeign.listByPurchaseOrderIds(Arrays.asList(dto.getPurchaseOrderId()));

            List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = poReturnDetailService.listReturnOrderDetailByPodIds(poDetailIds);
            for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : purchaseReturnOrderDetailList) {
                PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailEntityList.stream().filter(req -> req.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(detailEntity)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_DETAIL_SKU_NOT_EXIST, updateDTO.getSkuNo());
                }

                List<PurchaseOrderDetailEntity> detailEntityList = detailEntityListByPoId.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
                //校验sku是否有重复，重复需要拆单
                if (detailEntityList.size() > MathUtil.ONE) {
                    List<String> podIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
                    List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailByPodIds(podIds);
                    List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
                    Integer returnQty = updateDTO.getReturnQty();
                    for (PurchaseOrderDetailEntity entity : detailEntityList) {
                        Integer stockInQty = poInstockDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        //已退货
                        Integer alreadyReturnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !req.getMainId().equals(dto.getId())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

                        if (alreadyReturnQty >= stockInQty) {
                            continue;
                        }
                        PurchaseReturnOrderDetailDTO.UpdateDTO updateSkuDTO = new PurchaseReturnOrderDetailDTO.UpdateDTO();
                        updateSkuDTO.setPurchaseOrderDetailId(entity.getId());
                        updateSkuDTO.setRemark(updateDTO.getRemark());
                        updateSkuDTO.setSkuId(updateDTO.getSkuId());
                        updateSkuDTO.setSkuNo(updateDTO.getSkuNo());
                        updateSkuDTO.setReplenishQty(updateDTO.getReplenishQty());
                        updateSkuDTO.setDeductAmountQty(updateDTO.getDeductAmountQty());
                        updateSkuDTO.setSkuNo(updateDTO.getSkuNo());
                        if (returnQty > (stockInQty - alreadyReturnQty) && !detailEntityList.get(detailEntityList.size()-1).getId().equals(entity.getId())) {
                            returnQty = returnQty - (stockInQty - alreadyReturnQty);
                            updateSkuDTO.setReturnQty(stockInQty - alreadyReturnQty);
                            addDTOList.add(updateSkuDTO);
                        } else {
                            updateSkuDTO.setReturnQty(returnQty);
                            addDTOList.add(updateSkuDTO);
                            break;
                        }
                        updateDTO.setReplenishQty(0);
                        updateDTO.setDeductAmountQty(0);
                    }
                } else {
                    addDTOList.add(updateDTO);
                }
            }
            dto.setPurchasePriceDetailList(addDTOList);
        }
        return this.update(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaAddAndSubmit(PurchaseReturnOrderDTO.AddDTO dto) {
        String id = this.pdaAdd(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaUpdateAndSubmit(PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean update = this.pdaUpdate(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }
    /**
     * @description: 库存校验
     * @author Will
     * @date: 2023/9/22 10:12
     * @param list
     */
    private void checkInventoryQty (List<PoReturnEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //采购退货明细信息
        List<String> mainIdList = list.stream().map(PoReturnEntity::getId).collect(Collectors.toList());
        List<PoReturnDetailEntity> detailList = poReturnDetailService.listByMainIds(mainIdList);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        //仓库Id
        List<String> returnWarehouseIdList = list.stream().map(PoReturnEntity::getReturnWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(returnWarehouseIdList);


        // 产品属性为费用或服务的sku忽略库存计算
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        for (PoReturnDetailEntity detailEntity :  detailList) {
            //采购退货信息
            PoReturnEntity entity = list.stream().filter(obj -> obj.getId().equals(detailEntity.getId()) && !Objects.equals(obj.getSourceType(), SourceTypeEnum.QC_INFO.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            if(ignoreInventorySkuIds.contains(detailEntity.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", detailEntity.getSkuId(), detailEntity.getSkuNo());
                continue;
            }
            WarehouseEntity warehouseEntity = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getReturnWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseEntity)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String returnMode = entity.getReturnMode();
            Integer usableQty = inventoryService.getInventoryTotal(warehouseEntity.getOrgId(), warehouseEntity.getId(), detailEntity.getSkuId(), detailEntity.getWarehouseLocation(), InventoryStatusEnum.USABLE.getCode());
            if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())
                    && usableQty < detailEntity.getReplenishQty()) {
                throw new ServiceException(ApiError.ERROR_99070);
            } else if (Objects.equals(returnMode, ReturnModeEnum.DEDUCTION.getCode())
                    && usableQty < detailEntity.getDeductAmountQty()) {
                throw new ServiceException(ApiError.ERROR_99070);
            }
        }
    }

    @Override
    public Boolean dataRepairTemp() {
        List<PoReturnEntity> list = lambdaQuery().eq(PoReturnEntity::getSourceType, ReturnOrderSourceEnum.OTHER.getCode()).list();
        for (PoReturnEntity entity : list) {
            if (StringUtils.isBlank(entity.getSourceId())) {
                entity.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
                this.updateById(entity);
                continue;
            }

            QcInfoEntity qcInfoEntity = qcInfoService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(qcInfoEntity)) {
                entity.setSourceType(SourceTypeEnum.QC_INFO.getCode());
                this.updateById(entity);
                continue;
            }

            PoInstockEntity poInstockEntity = poInstockService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(poInstockEntity)) {
                entity.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
                this.updateById(entity);
                continue;
            }

            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(purchaseOrderEntity)) {
                entity.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
                this.updateById(entity);
                continue;
            }

            MachineInfoEntity machineInfoEntity = machineInfoService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(machineInfoEntity)) {
                entity.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
                this.updateById(entity);
                continue;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PurchaseReturnOrderDTO.SupplierPagingViewDTO> supplierPaging(PagingDTO<PurchaseReturnOrderDTO.SupplierPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());

        //查询登录信息
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if(Objects.isNull(loginUser)){
            throw new ServiceException(ApiError.ERROR_403);
        }

        PurchaseReturnOrderDTO.SupplierPagingParamDTO params = pagingParamDTO.getParams();
        //查询供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierByUid(loginUser.getUid());
        if (ObjectUtils.isNotEmpty(supplier)) {
            params.setSupplierId(supplier.getId());
        }

        //分页查询
        IPage<PurchaseReturnOrderDTO.SupplierPagingViewDTO> pageData = this.baseMapper.supplierPaging(query, params);
        //明细数据
        List<PurchaseReturnOrderDTO.SupplierPagingViewDTO> records = pageData.getRecords();
        //根据ids查询sku信息
        List<String> skuIdList = records.stream().map(PurchaseReturnOrderDTO.SupplierPagingViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (PurchaseReturnOrderDTO.SupplierPagingViewDTO record : records) {
            //退货来源名称
            ReturnOrderSourceEnum returnOrderSourceEnum = Objects.equals(record.getSourceType(), SourceTypeEnum.QC_INFO.getCode()) ?
                    ReturnOrderSourceEnum.QC : ReturnOrderSourceEnum.OTHER;
            record.setReturnOrderSource(returnOrderSourceEnum.getCode());
            record.setReturnOrderSourceName(returnOrderSourceEnum.getName());

            //订单确认状态名称
            record.setConfirmStatusName(PoReturnConfirmStatusEnum.getName(record.getConfirmStatus()));

            //异常分类名称
            record.setUnusualTypeName(PoReturnUnusualTypeEnum.getName(record.getUnusualType()));
            //退货方式名称
            record.setReturnModeName(ReturnModeEnum.getName(record.getReturnMode()));
            //产品信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(record.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            record.setProductName(productDetailEntity.getName());
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseReturnOrderDTO.SupplierTabListDTO> supplierTabList(PermissionsDTO param) {
        PurchaseReturnOrderDTO.SupplierPagingParamDTO searchParam = new PurchaseReturnOrderDTO.SupplierPagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //查询供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierByUid(userInfo.getUid());
        if (ObjectUtils.isNotEmpty(supplier)) {
            searchParam.setSupplierId(supplier.getId());
        }

        List<PurchaseReturnOrderDTO.SupplierTabListDTO> list = baseMapper.supplierTabList(searchParam);
        // 获取状态列表
        List<String> statusList = PoReturnConfirmStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(PurchaseReturnOrderDTO.SupplierTabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new PurchaseReturnOrderDTO.SupplierTabListDTO(status, 0));
            }
        });
        list.add(new PurchaseReturnOrderDTO.SupplierTabListDTO("all", list.stream().mapToInt(PurchaseReturnOrderDTO.SupplierTabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO returnConfirm(String id) {
        PoReturnEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到退货单数据");
        }

        //仅退货状态为待确认可操作
        if (!PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getConfirmStatus())) {
            throw new ServiceException(ApiError.NOT_WAIT_CONFIRM_STATUS);
        }

        LoginUser userInfo = UserContext.getDefaultLoginUser();

        //更新确认状态
        lambdaUpdate()
                .set(PoReturnEntity::getConfirmStatus, PoReturnConfirmStatusEnum.CONFIRM.getStatus())
                .set(PoReturnEntity::getSupplierContactId, userInfo.getUid())
                .set(PoReturnEntity::getSupplierContactName, userInfo.getUserName())
                .set(PoReturnEntity::getConfirmDate, LocalDate.now())
                .eq(PoReturnEntity::getId, id)
                .update();

        //更新对账明细的状态
        PoReconciliationDetailDTO.UpdateBusinessStatusDTO statusDTO = new PoReconciliationDetailDTO.UpdateBusinessStatusDTO(Arrays.asList(id), ConfirmStatusEnum.CONFIRM.getCode());
        srmPoReconciliationFeign.updateBusinessStatusBySourceIdList(statusDTO);

        //操作日志
        String msg = StrUtil.format("用户【{}】操作单号为【{}】的【{}】退货确认状态为已确认", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "退货确认");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), entity.getId(), "退货确认");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "退货确认");
    }

    @Override
    public Boolean unusualFeedback(PurchaseReturnOrderDTO.UnusualFeedbackParamDTO dto) {
        PoReturnEntity entity = this.getById(dto.getId());
        //仅退货状态为待确认可操作
        if (!PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getConfirmStatus())) {
            throw new ServiceException(ApiError.NOT_WAIT_CONFIRM_STATUS);
        }

        //上传附件
        if (!CollectionUtils.isEmpty(dto.getAttachList())) {
            //限制最多上传5个附件
            if (dto.getAttachList().size() > 5) {
                throw new ServiceException(ApiError.ATTACH_QTY_MAX_FIVE);
            }
            //保存附件
            Class<PoReturnEntity> aClass = PoReturnEntity.class;
            TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            wmsAttachmentService.batchSave(dto.getAttachList(), type, dto.getId());
        }

        //查询退货配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.PO_RETURN.getCode());
        if (ObjectUtils.isEmpty(cfgSettingEntity)) {
            throw new ServiceException(ApiError.CFG_SETTING_NOT_EXISTS);
        }
        CfgSettingValueDTO.PoReturnSettingDTO poReturnSettingDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.PoReturnSettingDTO.class);

        String unusualHandleUserId = "";
        if (PoReturnUnusualTypeEnum.QUANTITY_ISSUE.getCode().equals(dto.getUnusualType())) {
            //数量问题取值
            unusualHandleUserId = poReturnSettingDTO.getQtyHandlePostId();

        } else if (PoReturnUnusualTypeEnum.OTHER_ISSUES.getCode().equals(dto.getUnusualType())) {
            //其他问题取值
            unusualHandleUserId = poReturnSettingDTO.getOtherHandlePostId();
        } else {
            throw new ServiceException(ApiError.UNUSUAL_TYPE_NOT_EXISTS);
        }
        //查询岗位名称
        SysPostEntity sysPostEntity = sysPostFeign.getById(unusualHandleUserId);

        //修改
        return lambdaUpdate()
                .set(PoReturnEntity::getUnusualType, dto.getUnusualType())
                .set(PoReturnEntity::getUnusualRemark, dto.getUnusualRemark())
                .set(PoReturnEntity::getUnusualHandleUserId, unusualHandleUserId)
                .set(StringUtils.isNotBlank(sysPostEntity.getPostName()), PoReturnEntity::getUnusualHandleUserName, sysPostEntity.getPostName())
                .eq(PoReturnEntity::getId, dto.getId())
                .update();
    }

    @Override
    public List<PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO> unusualHandleUserOption() {
        List<PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO> resultList = new ArrayList<>();
        //查询退货配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.PO_RETURN.getCode());
        CfgSettingValueDTO.PoReturnSettingDTO poReturnSettingDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.PoReturnSettingDTO.class);
        List<SysPostEntity> sysPostEntities = sysPostFeign.listById(Arrays.asList(poReturnSettingDTO.getOtherHandlePostId(), poReturnSettingDTO.getQtyHandlePostId()));

        //其他问题处理人
        PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO otherDto = new PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO();
        SysPostEntity otherHandlePost = sysPostEntities.stream().filter(req -> poReturnSettingDTO.getOtherHandlePostId().equals(req.getId())).findFirst().orElse(new SysPostEntity());
        otherDto.setId(poReturnSettingDTO.getOtherHandlePostId());
        otherDto.setName(otherHandlePost.getPostName());
        resultList.add(otherDto);

        //数量问题处理人
        PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO qtyDto = new PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO();
        SysPostEntity qtyHandlePost = sysPostEntities.stream().filter(req -> poReturnSettingDTO.getQtyHandlePostId().equals(req.getId())).findFirst().orElse(new SysPostEntity());
        qtyDto.setId(poReturnSettingDTO.getQtyHandlePostId());
        qtyDto.setName(qtyHandlePost.getPostName());
        resultList.add(qtyDto);

        return resultList;
    }

    @Override
    public PurchaseReturnOrderDTO.UnusualFeedbackView unusualFeedbackView(String id) {
        PoReturnEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.PO_RETURN_NOT_EXISTS);
        }
        PurchaseReturnOrderDTO.UnusualFeedbackView result = new PurchaseReturnOrderDTO.UnusualFeedbackView();
        result.setId(id);
        result.setUnusualType(entity.getUnusualType());
        result.setUnusualTypeName(PoReturnUnusualTypeEnum.getName(entity.getUnusualType()));
        result.setUnusualRemark(entity.getUnusualRemark());

        //获取到附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(entity.getId()));
        List<AttachDTO> attachDTOList = BeanMapper.copyList(attachmentList, AttachDTO.class);
        result.setAttachList(attachDTOList);
        return result;
    }

    @Override
    public List<PoReturnEntity> listByApproceAndWaitConfirm() {
        return lambdaQuery().eq(PoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                .eq(PoReturnEntity::getConfirmStatus, PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode()).list();
    }

    @Override
    public PurchaseReturnStatisticsDTO.ResponseDTO statisticsBySupplier(PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO) {
        PurchaseReturnStatisticsDTO.ResponseDTO responseDTO = new PurchaseReturnStatisticsDTO.ResponseDTO();
        responseDTO.setStatisticsMonthDTOList(baseMapper.statisticsBySupplier(returnRequestDTO));
        return responseDTO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void poReturnAutoConfirm() {
        //查询规则设置
        List<CfgSettingDTO.ViewDTO> list = srmCfgSettingFeign.listByKey(ConfigKeyEnum.RETURN_AUTO_CONFIRM.getCode());
        if (CollectionUtils.isEmpty(list)) {
            log.warn("未找到存在采购退货单设置的供应商！");
            return;
        }
        List<String> poReturnIds = baseMapper.listPoReturnAutoConfirm(list);
        if (CollectionUtils.isEmpty(poReturnIds)) {
            return;
        }

        lambdaUpdate().in(PoReturnEntity::getId, poReturnIds)
                .set(PoReturnEntity::getConfirmStatus, PoReturnConfirmStatusEnum.CONFIRM.getCode())
                .set(PoReturnEntity::getConfirmDate, LocalDate.now())
                .update();

        //更新对账明细的状态
        PoReconciliationDetailDTO.UpdateBusinessStatusDTO statusDTO = new PoReconciliationDetailDTO.UpdateBusinessStatusDTO(poReturnIds, ConfirmStatusEnum.CONFIRM.getCode());
        srmPoReconciliationFeign.updateBusinessStatusBySourceIdList(statusDTO);
    }

    @Override
    public PurchaseReturnStatisticsDTO.StatusDTO confirmStatusCountBySupplier(PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO) {
        return  PurchaseReturnStatisticsDTO.StatusDTO.builder()
                .count(lambdaQuery().eq(PoReturnEntity::getSupplierId, returnRequestDTO.getSurpplierId())
                        .eq(PoReturnEntity::getConfirmStatus, returnRequestDTO.getConfirmStatus())
                        .eq(PoReturnEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus()).count())
                .build();
    }

    @Override
    public PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> exportPurchaseReturnOrder(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto) {

        IPage<PurchaseReturnOrderDTO.PagingViewDTO> page = this.baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<PurchaseReturnOrderDTO.SubcontractOrderDTO> listSubcontractOrder(String poId) {
        List<PurchaseOrderEntity> purchaseOrderEntityList = purchaseOrderService.ListPurchaseOrderEntityByIds(Collections.singletonList(poId));
        if (CollectionUtils.isEmpty(purchaseOrderEntityList)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderEntityList.get(0);
        if (StringUtils.isBlank(purchaseOrderEntity.getSubcontractType())) {
            return Collections.emptyList();
        }
        List<PurchaseOrderEntity> purchaseOrderEntityList1 = scmTaskFeign.listPoBySourceIds(Collections.singletonList(purchaseOrderEntity.getSourceId()));
        if (CollectionUtils.isEmpty(purchaseOrderEntityList1)){
            return Collections.emptyList();
        }
        List<String> poIds = purchaseOrderEntityList1.stream().map(PurchaseOrderEntity::getId).distinct().collect(Collectors.toList());
        List<PurchaseReturnOrderDTO.SubcontractOrderDTO> list =  baseMapper.listSubcontractOrder(poIds);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> skuIds = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())).map(PurchaseReturnOrderDTO.SubcontractOrderDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        for (PurchaseReturnOrderDTO.SubcontractOrderDTO viewSubcontractPoDTO : list) {
            //产品名称
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewSubcontractPoDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            viewSubcontractPoDTO.setProductName(productName);
            //单据状态名称
            viewSubcontractPoDTO.setApproveStatusName(ApproveStatusEnum.getName(viewSubcontractPoDTO.getApproveStatus()));
        }
        return list;
    }

    @Override
    public List<PurchasePriceDTO.PriceDTO> batchGetPurchasePrice(List<PurchasePriceDTO.PriceDTO> list) {
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        //采购组织Id
        List<String> purchaseOrgIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getPurchaseOrgId())).map(PurchasePriceDTO.PriceDTO::getPurchaseOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(purchaseOrgIdList);
        if (CollectionUtils.isEmpty(companyList)){
            return Collections.emptyList();
        }
        //skuId
        List<String> skuIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())).map(PurchasePriceDTO.PriceDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            return Collections.emptyList();
        }
        //供应商Id
        List<String> supplierIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSupplierId())).map(PurchasePriceDTO.PriceDTO::getSupplierId).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntityList = scmTaskFeign.getSupplierByIdList(supplierIdList);
        if (CollectionUtils.isEmpty(supplierEntityList)) {
            return Collections.emptyList();
        }
        PurchasePriceDetailDTO.PurchaseTaxPriceBatchSearchDTO dto = new PurchasePriceDetailDTO.PurchaseTaxPriceBatchSearchDTO();
        dto.setSkuIdList(skuIdList);
        dto.setSupplierIdList(supplierIdList);
        dto.setPurchaseOrgIdList(purchaseOrgIdList);
        //查询对应采购价目
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> viewList = baseMapper.batchGetTaxPrice(dto);
        //获取scm采购价目记录
        List<PurchasePriceDTO.PriceDTO> scmViewList = scmTaskFeign.batchGetPurchasePrice(list);

        for (PurchasePriceDTO.PriceDTO priceDTO : list) {
            PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = null;
            if (CollectionUtils.isNotEmpty(viewList)) {
                viewDTO = viewList.stream().filter(obj -> Objects.nonNull(obj)
                                && StrUtil.isNotBlank(obj.getSkuId()) && StrUtil.isNotBlank(priceDTO.getSkuId()) && obj.getSkuId().equals(priceDTO.getSkuId())
                                && StrUtil.isNotBlank(obj.getSupplierId()) && StrUtil.isNotBlank(priceDTO.getSupplierId()) && obj.getSupplierId().equals(priceDTO.getSupplierId())
                                && StrUtil.isNotBlank(obj.getPurchaseOrgId()) && StrUtil.isNotBlank(priceDTO.getPurchaseOrgId()) && StrUtil.equals(obj.getPurchaseOrgId(),priceDTO.getPurchaseOrgId()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(viewDTO)) {
                    priceDTO.setTaxPrice(viewDTO.getTaxPrice());
                    priceDTO.setTaxRate(viewDTO.getTaxRate());
                    priceDTO.setCurrency(viewDTO.getCurrency());
                    priceDTO.setCurrencySymbol(viewDTO.getCurrencySymbol());
                    priceDTO.setAmount(MathUtil.multiply(viewDTO.getTaxPrice(), priceDTO.getQty()).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString());
                }
            }
            if (Objects.isNull(viewDTO) && CollectionUtils.isNotEmpty(scmViewList)){
                //报价信息
                PurchasePriceDTO.PriceDTO priceDTO2 = scmViewList.stream().filter(obj -> Objects.nonNull(obj)
                                && StrUtil.isNotBlank(obj.getSkuId()) && StrUtil.isNotBlank(priceDTO.getSkuId()) && obj.getSkuId().equals(priceDTO.getSkuId())
                                && StrUtil.isNotBlank(obj.getSupplierId()) && StrUtil.isNotBlank(priceDTO.getSupplierId()) && obj.getSupplierId().equals(priceDTO.getSupplierId())
                                && StrUtil.isNotBlank(obj.getPurchaseOrgId()) && StrUtil.isNotBlank(priceDTO.getPurchaseOrgId()) && StrUtil.equals(obj.getPurchaseOrgId(),priceDTO.getPurchaseOrgId()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(priceDTO2)){
                    priceDTO.setTaxPrice(priceDTO2.getTaxPrice());
                    priceDTO.setTaxRate(priceDTO2.getTaxRate());
                    priceDTO.setCurrency(priceDTO2.getCurrency());
                    priceDTO.setCurrencySymbol(priceDTO2.getCurrencySymbol());
                    priceDTO.setAmount(MathUtil.multiply(priceDTO2.getTaxPrice(), priceDTO.getQty()).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString());
                }
            }
        }
        return list;
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    public void sendPushTask(List<PoReturnEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeReturnOrderService.syncDataToKingdee(obj, operate);
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
