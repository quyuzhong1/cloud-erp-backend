package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.ReturnOrderExportExcelDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeReturnOrderService;
import com.erp.server.wms.mapper.PurchaseReturnOrderMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class PurchaseReturnOrderServiceImpl extends SuperServiceImpl<PurchaseReturnOrderMapper, PurchaseReturnOrderEntity> implements PurchaseReturnOrderService {

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

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

    @Value("${companyCode}")
    private String companyCode;

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
        if (CollectionUtils.isNotEmpty(pagingParamDTO.getParams().getApproveStatusList())) {
            pagingParamDTO.getParams().setInvalidStatus(Boolean.FALSE);
        }
        IPage<PurchaseReturnOrderDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        //明细数据
        List<PurchaseReturnOrderDTO.PagingViewDTO> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(PurchaseReturnOrderDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isNotEmpty(records)) {
            //获取采购单详情的id集合
            List<String> detailId = records.stream().map(PurchaseReturnOrderDTO.PagingViewDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(detailId);
            records.stream().forEach(record->{

                //获取采购单详情
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().filter(entityClass -> entityClass.getId().equals(record.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    record.setReturnPrice(purchaseOrderDetailEntity.getTaxPrice());
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
            });

            List<String> warehouseIds = records.stream().map(req -> req.getReturnWarehouseId()).distinct().collect(Collectors.toList());

            List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);

            records.forEach(obj -> {
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setReturnModeName(ReturnModeEnum.getName(obj.getReturnMode()));
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(obj.getReturnWarehouseId()) && req.getCode().equals(obj.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                obj.setWarehouseLocationName(warehouseLocationEntity.getName());
            });
        }
        return new PagingVO(pageData);
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
        SysUserDTO userDTO = new SysUserDTO();

        //获取用户信息
        if (ObjectUtils.isNotEmpty(dto.getReturnUserId())) {
            userDTO =  sysUserFeign.getSysUserById(dto.getReturnUserId());
        }
        //验证单价必填
        if (ReturnModeEnum.DEDUCTION.getCode().equals(dto.getReturnMode())) {
            long count = dto.getPurchasePriceDetailList().stream().filter(obj -> MathUtil.compareTo(obj.getReturnPrice(), MathUtil.ZERO) == MathUtil.ZERO).count();
            if (count > MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_ORDER_PRICE_IS_NOT_NULL);
            }
        }

        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getReturnOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getReturnWarehouseId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGTH, BusinessNoTypeEnum.CODE_CGTH.getCode()));
        //设置收货单主表
        PurchaseReturnOrderEntity purchaseReturnOrderEntity = new PurchaseReturnOrderEntity();
        BeanMapperUtils.copy(dto, purchaseReturnOrderEntity);
        purchaseReturnOrderEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        purchaseReturnOrderEntity.setCode(code);
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取采购订单主表信息
            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
            purchaseReturnOrderEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
            purchaseReturnOrderEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());

        }

        //获取采购单供应商信息
//        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            SysUserDTO purchaseUser = sysUserFeign.getSysUserById(dto.getPurchaseUserId());
            purchaseReturnOrderEntity.setPurchaseUserId(dto.getPurchaseUserId());
            purchaseReturnOrderEntity.setPurchaseUserName(purchaseUser.getUserName());
        }

        purchaseReturnOrderEntity.setSupplierId(dto.getSupplierId());
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(dto.getSupplierId());
        purchaseReturnOrderEntity.setSupplierName(supplierEntity.getName());
        purchaseReturnOrderEntity.setSupplierContactId(dto.getSupplierContactId());
        if (StringUtils.isNotBlank(dto.getSupplierContactId())) {
            SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(dto.getSupplierContactId());
            purchaseReturnOrderEntity.setSupplierContactName(supplierContactById.getPerson());
        }
        purchaseReturnOrderEntity.setReturnUserName(userDTO != null ? userDTO.getUserName() : "");
        purchaseReturnOrderEntity.setReturnOrgName(sysAccountingCompanyEntity.getCompanyName());
        purchaseReturnOrderEntity.setBillDate(dto.getBillDate());
        purchaseReturnOrderEntity.setReturnWarehouseName(warehouseEntity.getName());

        //保存主表信息
        this.save(purchaseReturnOrderEntity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个采购退货单【%s】", code), ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), purchaseReturnOrderEntity.getId(), "新增操作");

        //保存详情信息
        purchaseReturnOrderDetailService.add(dto, purchaseReturnOrderEntity.getId());
        return purchaseReturnOrderEntity.getId();
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
        SysUserDTO userDTO = sysUserFeign.getSysUserById(dto.getReturnUserId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getReturnOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getReturnWarehouseId());


        //验证单价必填
        if (ReturnModeEnum.DEDUCTION.getCode().equals(dto.getReturnMode())) {
            long count = dto.getPurchasePriceDetailList().stream().filter(obj -> MathUtil.compareTo(obj.getReturnPrice(), MathUtil.ZERO) == MathUtil.ZERO).count();
            if (count > MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_ORDER_PRICE_IS_NOT_NULL);
            }
        }

        //设置收货单主表
        PurchaseReturnOrderEntity purchaseReturnOrderEntity = new PurchaseReturnOrderEntity();
        BeanMapperUtils.copy(dto, purchaseReturnOrderEntity);
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取采购订单主表信息
            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
            purchaseReturnOrderEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
            purchaseReturnOrderEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        }


        //获取采购单供应商信息
//        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        if (StringUtils.isNotBlank(dto.getPurchaseUserId())) {
            SysUserDTO purchaseUser = sysUserFeign.getSysUserById(dto.getPurchaseUserId());
            purchaseReturnOrderEntity.setPurchaseUserName(purchaseUser.getUserName());
        }

        purchaseReturnOrderEntity.setPurchaseUserId(dto.getPurchaseUserId());
        purchaseReturnOrderEntity.setSupplierId(dto.getSupplierId());
        if (StringUtils.isNotBlank(dto.getSupplierId())) {
            SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(dto.getSupplierId());
            purchaseReturnOrderEntity.setSupplierName(supplierEntity.getName());
        }
        purchaseReturnOrderEntity.setSupplierContactId(dto.getSupplierContactId());
        if (StringUtils.isNotBlank(dto.getSupplierContactId())) {
            SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(dto.getSupplierContactId());
            purchaseReturnOrderEntity.setSupplierContactName(supplierContactById.getPerson());
        }
        purchaseReturnOrderEntity.setReturnUserName(userDTO.getUserName());
        purchaseReturnOrderEntity.setReturnOrgName(sysAccountingCompanyEntity.getCompanyName());
        purchaseReturnOrderEntity.setBillDate(dto.getBillDate());
        purchaseReturnOrderEntity.setReturnWarehouseName(warehouseEntity.getName());
        //更新收货单主表信息
        this.updateById(purchaseReturnOrderEntity);

        //操作日志
        PurchaseReturnOrderEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, purchaseReturnOrderEntity, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), purchaseReturnOrderEntity.getId(), "", "");

        //更新收货单详情表信息
        return purchaseReturnOrderDetailService.update(dto, purchaseReturnOrderEntity.getId());
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
        PurchaseReturnOrderEntity purchaseReturnOrderEntity = this.getById(id);
        BeanMapperUtils.copy(purchaseReturnOrderEntity, viewDTO);

        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        if (StringUtils.isNotBlank(purchaseReturnOrderEntity.getPurchaseOrderId())) {
            //获取采购订单主表信息
            PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(purchaseReturnOrderEntity.getPurchaseOrderId());
            viewDTO.setPurchaseUserDeptId(purchaseOrderEntity.getPurchaseDeptId());
            viewDTO.setPurchaseUserDeptName(purchaseOrderEntity.getPurchaseDeptName());
            viewDTO.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
            viewDTO.setPurchaseOrgName(purchaseOrderEntity.getPurchaseOrgName());
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

        if (SourceTypeEnum.QC_INFO.getCode().equals(purchaseReturnOrderEntity.getSourceType())) {
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
        List<PurchaseReturnOrderDetailEntity> detail = purchaseReturnOrderDetailService.getDetailByMainId(id);
        //获取sku的id集合
        List<String> skuNoList = detail.stream().map(PurchaseReturnOrderDetailEntity::getSkuNo).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> detailEntityList = plmTaskFeign.listBySkuNoList(skuNoList);

        //获取采购单详情的id集合
        List<String> detailId = detail.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);

        List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(detailId);

        List<String> skuIdList = detail.stream().map(PurchaseReturnOrderDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseLocationCodeList = detail.stream().map(r->StrUtils.null2EmptyWithTrim(r.getWarehouseLocation())).distinct().collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(Lists.newArrayList(purchaseReturnOrderEntity.getReturnWarehouseId()));
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationCodeList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());

        //查询供应商信息
        List<String> supplierIdList = new ArrayList<>();
        supplierIdList.add(purchaseReturnOrderEntity.getSupplierId());
        List<String> mainSupplierIdList = detail.stream().filter(obj -> StringUtils.isNotBlank(obj.getMainSupplierId())).map(PurchaseReturnOrderDetailEntity::getMainSupplierId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(mainSupplierIdList)) {
            supplierIdList.addAll(mainSupplierIdList);
        }
        List<SupplierEntity>  supplierList = scmTaskFeign.getSupplierByIdList(mainSupplierIdList);

        //主表供应商
        SupplierEntity supplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(purchaseReturnOrderEntity.getSupplierId())).findFirst().orElse(new SupplierEntity());
        viewDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        //可用数量
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(purchaseReturnOrderEntity.getReturnWarehouseId()));

        for (PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity : detail) {
            Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(purchaseReturnOrderDetailEntity.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            PurchaseReturnOrderDetailDTO.ViewDTO detailView = new PurchaseReturnOrderDetailDTO.ViewDTO();
            BeanMapperUtils.copy(purchaseReturnOrderDetailEntity, detailView);
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
            if (ReturnModeEnum.DEDUCTION.getCode().equals(purchaseReturnOrderEntity.getReturnMode())) {
                deductAmountAmount = MathUtil.multiply(detailView.getReturnPrice(),purchaseReturnOrderDetailEntity.getDeductAmountQty());
            } else {
                deductAmountAmount = MathUtil.multiply(detailView.getReturnPrice(),purchaseReturnOrderDetailEntity.getReturnQty());
            }
            detailView.setTotalPrice(deductAmountAmount);
            //获取sku信息
            SkuVO productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getSkuNo().equals(detailView.getSkuNo())).findFirst().orElse(new SkuVO());
            detailView.setProductName(productDetailEntity.getSkuName());
            detailView.setSpuNo(productDetailEntity.getSpuNo());
            detailView.setUnit(productDetailEntity.getUnitName());
            detailView.setVariantProperty(productDetailEntity.getVariantProperty());

            //参考供应商
            SupplierEntity mainSupplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(purchaseReturnOrderDetailEntity.getMainSupplierId())).findFirst().orElse(new SupplierEntity());
            detailView.setMainSupplierName(mainSupplierEntity.getName());

            //根据组织、仓库、sku查询可用库存
            /*
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(purchaseReturnOrderEntity.getReturnWarehouseId(), purchaseReturnOrderDetailEntity.getSkuId());
            detailView.setCurInventoryQty(curInventoryQty);
             */
            //即时库存
            Integer curInventoryQty = skuInventoryList.stream().filter(r ->Objects.equals(r.getSkuId(), purchaseReturnOrderDetailEntity.getSkuId())
                    && Objects.equals(r.getWarehouseId(), purchaseReturnOrderEntity.getReturnWarehouseId())
                    && Objects.equals(r.getWarehouseLocationId(), StrUtils.null2EmptyWithTrim(purchaseReturnOrderDetailEntity.getWarehouseLocation()))).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
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
        List<PurchaseReturnOrderEntity> purchaseReturnOrderEntities = this.listByIds(ids);
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

        //TODO 待加审核流程

        //更新审核状态
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(PurchaseReturnOrderEntity::getId, ids)
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
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<PurchaseReturnOrderEntity> purchaseReturnOrderEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //判断是否是审核中的状态
        long count = purchaseReturnOrderEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != purchaseReturnOrderEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //库存校验
        checkInventoryQty(purchaseReturnOrderEntityList);

        //操作日志
        List<Pair<String, String>> pairList = purchaseReturnOrderEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购退货单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "审核操作");

        LoginUser userInfo = commonService.getUserInfo();
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(baseApproveParamDTO.getType())) {
            //审核通过
            lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(PurchaseReturnOrderEntity::getApproveUserId, userInfo.getUid())
                    .set(PurchaseReturnOrderEntity::getApproveUserName, userInfo.getUserName())
                    .set(PurchaseReturnOrderEntity::getApproveTime, LocalDateTime.now())
                    .in(PurchaseReturnOrderEntity::getId, ids)
                    .update();
            List<PurchaseOrderDetailEntity> list = new ArrayList<>();
            for (PurchaseReturnOrderEntity purchaseReturnOrderEntity : purchaseReturnOrderEntityList) {
                List<PurchaseReturnOrderDetailEntity> detailByMainId = purchaseReturnOrderDetailService.getDetailByMainId(purchaseReturnOrderEntity.getId());
                List<String> detailId = detailByMainId.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
                List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
                if (CollectionUtils.isNotEmpty(purchaseOrderDetailEntities)) {
                    //退料扣款
                    if (purchaseReturnOrderEntity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {

                        detailByMainId.forEach(returnOrderDetailEntity -> {
                            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(returnOrderDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
                            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                                PurchaseOrderDetailEntity entity = new PurchaseOrderDetailEntity ();
                                entity.setId(purchaseOrderDetailEntity.getId());
                                entity.setPurchaseAmount(purchaseOrderDetailEntity.getPurchaseAmount().subtract(returnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(returnOrderDetailEntity.getReturnQty())))));
                                list.add(entity);
                            }

                        });
                    }
                }
            }
            List<String> purchaseOrderIds = purchaseReturnOrderEntityList.stream().filter(req -> StringUtils.isNotBlank(req.getPurchaseOrderId())).map(req -> req.getPurchaseOrderId()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(purchaseOrderIds)) {
                updateArrivalState(purchaseOrderIds, list);
            }
            //自动生成补货采购订单
            autoAddPurchaseOrder(purchaseReturnOrderEntityList);
            // 更新库存信息
            updateInventoryTransCore(purchaseReturnOrderEntityList);
            //审核通过发送金蝶
            purchaseReturnOrderEntityList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));

        } else {
            //审核不通过
            lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .set(PurchaseReturnOrderEntity::getApproveUserId, userInfo.getUid())
                    .set(PurchaseReturnOrderEntity::getApproveUserName, userInfo.getUserName())
                    .set(PurchaseReturnOrderEntity::getApproveTime, LocalDateTime.now())
                    .in(PurchaseReturnOrderEntity::getId, ids)
                    .update();
        }

        return Boolean.TRUE;
    }

    /**
     * 批量反审核
     *
     * @param ids ids
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<PurchaseReturnOrderEntity> purchaseReturnOrderEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //已审核支持反审核
        long count = purchaseReturnOrderEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
        ).count();

        if (count != purchaseReturnOrderEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        //判断是否已生成采购订单
        List<PurchaseOrderEntity> poList = scmTaskFeign.listPoBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(poList)) {
            List<String> sourceIds = poList.stream().map(PurchaseOrderEntity::getSourceId).collect(Collectors.toList());
            String codes = purchaseReturnOrderEntityList.stream().filter(obj -> sourceIds.contains(obj.getId())).map(PurchaseReturnOrderEntity::getCode).distinct().collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(codes)) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_REF_PO,codes);
            }
        }
        //TODO 待加审核流程
        //修改状态为待提交
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .set(PurchaseReturnOrderEntity::getApproveUserId, "")
                .set(PurchaseReturnOrderEntity::getApproveUserName, "")
                .set(PurchaseReturnOrderEntity::getApproveTime, null)
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();
        List<PurchaseOrderDetailEntity> list = new ArrayList<>();
        for (PurchaseReturnOrderEntity purchaseReturnOrderEntity : purchaseReturnOrderEntityList) {

            List<PurchaseReturnOrderDetailEntity> detailByMainId = purchaseReturnOrderDetailService.getDetailByMainId(purchaseReturnOrderEntity.getId());
            List<String> detailId = detailByMainId.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
            if (CollectionUtils.isNotEmpty(purchaseOrderDetailEntities)) {
                //退料扣款
                if (purchaseReturnOrderEntity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {

                    detailByMainId.forEach(returnOrderDetailEntity -> {
                        PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(returnOrderDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
                        PurchaseOrderDetailEntity entity = new PurchaseOrderDetailEntity ();
                        entity.setId(purchaseOrderDetailEntity.getId());
                        entity.setPurchaseAmount(purchaseOrderDetailEntity.getPurchaseAmount().add(returnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(returnOrderDetailEntity.getReturnQty())))));
                        list.add(entity);
                    });
                }

            }
        }
        List<String> purchaseOrderIds = purchaseReturnOrderEntityList.stream().map(req -> req.getPurchaseOrderId()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(purchaseOrderIds)) {
            updateArrivalState(purchaseOrderIds, list);
        }
        unApproveInventory(purchaseReturnOrderEntityList); // 库存反审核操作

        //操作日志
        List<Pair<String, String>> pairList = purchaseReturnOrderEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个采购退货单【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "反审核操作");
        //审核通过发送金蝶
        purchaseReturnOrderEntityList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
        return Boolean.TRUE;
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
        List<PurchaseReturnOrderEntity> purchaseReturnOrderEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = purchaseReturnOrderEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != purchaseReturnOrderEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = purchaseReturnOrderEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
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
        List<PurchaseReturnOrderEntity> warehouseReceiveList = this.listByIds(ids);
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
        lambdaUpdate().set(PurchaseReturnOrderEntity::getInvalidStatus, Boolean.TRUE)
                .set(PurchaseReturnOrderEntity::getInvalidRemark, remark)
                .set(PurchaseReturnOrderEntity::getInvalidTime, LocalDateTime.now())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个采购退货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "作废操作");

        //作废发送金蝶
        warehouseReceiveList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_INVALID.getCode()));
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
        List<PurchaseReturnOrderEntity> warehouseReceiveList = this.listByIds(ids);
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
        purchaseReturnOrderDetailService.delete(ids);
        //审核通过发送金蝶
        warehouseReceiveList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));
        //删除主表
        return this.removeByIds(ids);
    }

    /**
     * 导出
     *
     * @param dto      dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    @Override
    public Boolean exportExcel(PurchaseReturnOrderDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<ReturnOrderExcelDTO> returnOrderExcelDTOS = baseMapper.returnOrderExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = returnOrderExcelDTOS.stream().map(ReturnOrderExcelDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //获取采购单详情的id集合
        List<String> detailId = returnOrderExcelDTOS.stream().map(ReturnOrderExcelDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(detailId);

        returnOrderExcelDTOS.forEach(obj -> {
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().filter(entityClass -> entityClass.getId().equals(obj.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                obj.setReturnPrice(purchaseOrderDetailEntity.getTaxPrice());
            }
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setReturnModeName(ReturnModeEnum.getName(obj.getReturnMode()));
            BigDecimal deductAmountAmount ;
            if (ReturnModeEnum.DEDUCTION.getCode().equals(obj.getReturnMode())) {
                deductAmountAmount = MathUtil.multiply(obj.getReturnPrice(),obj.getDeductAmountQty());
            } else {
                deductAmountAmount = MathUtil.multiply(obj.getReturnPrice(),obj.getReturnQty());
            }
            obj.setDeductAmountAmount(deductAmountAmount);
        });

        List<ReturnOrderExportExcelDTO> returnOrderExportExcelDTOList = BeanMapperUtils.copyList(ReturnOrderExportExcelDTO.class, returnOrderExcelDTOS);

        String fileName = "退货单";
        try {
            ExcelUtil.export(fileName, "退货单", returnOrderExportExcelDTOList, ReturnOrderExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
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
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            PurchaseReturnOrderDTO.PagingParamDTO pagingParamDTO = new PurchaseReturnOrderDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            PurchaseReturnOrderDTO.ReturnOrderCountDTO resultDTO = new PurchaseReturnOrderDTO.ReturnOrderCountDTO();
            Integer count = MathUtil.ZERO;
            if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PageListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PageListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public List<PurchaseReturnOrderEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery()
                .in(PurchaseReturnOrderEntity::getSourceId, sourceIds)
                .eq(PurchaseReturnOrderEntity::getInvalidStatus, Boolean.FALSE)
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
                .eq(PurchaseReturnOrderEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchaseReturnOrderEntity::getSyncKingdeeId, syncKingdeeId)
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
     * @param purchaseOrderIds
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/28 11:37
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void updateArrivalState(List<String> purchaseOrderIds, List<PurchaseOrderDetailEntity> detailEntityList) {
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listByPurchaseOrderIds(purchaseOrderIds);
        List<String> podIds = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseReturnOrderDetailEntity> returnDetailEntityList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIds);
        List<WarehouseReceiveDetailEntity> receiveDetailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);

        //委外订单下采购订单
        List<String> sourceDetailIds = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPodBySourceDetailIds(sourceDetailIds);

        //委外订单
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByIds(sourceDetailIds);

        List<PurchaseOrderDetailEntity> list = new ArrayList<>();
        for (PurchaseOrderDetailEntity orderDetailEntity : purchaseOrderDetailEntities) {
            //结束交货的订单无需变更到货状态
            if (orderDetailEntity.getIsEndReceive()) {
                continue;
            }

            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(orderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer receiveQty = receiveDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(orderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer purchaseQty = orderDetailEntity.getPurchaseQty();

            String arrivalStatus = "";
            //未到货
            if (receiveQty - returnQty <= MathUtil.ZERO) {
                arrivalStatus = ArrivalStatusEnum.NON_ARRIVAL.getCode();
            } else if (receiveQty - returnQty > MathUtil.ZERO && receiveQty - returnQty < purchaseQty) {
                //部分到货
                arrivalStatus = ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode();
            } else {
                //已到货
                arrivalStatus = ArrivalStatusEnum.ARRIVED.getCode();
            }
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = new PurchaseOrderDetailEntity();
            purchaseOrderDetailEntity.setId(orderDetailEntity.getId());
            purchaseOrderDetailEntity.setArrivalStatus(arrivalStatus);
            purchaseOrderDetailEntity.setArrivalTime(LocalDateTime.now());
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
        List<PurchaseOrderDTO.PurchaseOrderInfoDTO> entityList = scmTaskFeign.getByOrderIds(poIds);

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
            addDTO.setSourceId(purchaseReturnOrderDTO.getSourceId());
            addDTO.setPurchaseOrderId(purchaseOrderEntity.getPurchaseOrderId());
            addDTO.setReturnWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());
            addDTO.setReturnRemark(purchaseReturnOrderDTO.getRemark());
            addDTO.setSupplierId(purchaseOrderEntity.getSupplierId());
            addDTO.setReturnMode(purchaseReturnOrderDTO.getReturnMode());
            addDTO.setSourceId(purchaseReturnOrderDTO.getSourceId());
            addDTO.setReturnUserId(purchaseReturnOrderDTO.getReturnUserId());
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (PoInstockDTO.GeneratePurchaseReturnOrderDTO detail : value) {
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
                //验证退货数量
                Integer stockInQty = stockInSkuList.stream().filter(s -> s.getSkuId().equals(detail.getSkuId()) &&
                        detail.getSourceDetailId().equals(s.getPurchaseOrderDetailId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getStockInQty())).orElse(0);
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
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
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
        List<PurchaseReturnOrderEntity> list = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        //判断单据是否审核完成
        String codes = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(PurchaseReturnOrderEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(codes)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_REF_PO_APPROVE,codes);
        }
        //下推采购订单
        autoAddPurchaseOrder(list);
        return Boolean.TRUE;
    }

    /**
     * 更新库存信息
     * @param list
     */
    public void updateInventoryTransCore (List<PurchaseReturnOrderEntity> list) {
        List<String> ids = list.stream().map(PurchaseReturnOrderEntity::getId).distinct().collect(Collectors.toList());
        Map<String,PurchaseReturnOrderEntity> mainMap = list.stream().collect(Collectors.toMap(PurchaseReturnOrderEntity::getId, Function.identity()));
        List<PurchaseReturnOrderDetailEntity> detailEntityList = purchaseReturnOrderDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException("未找到采购退货单明细信息");
        }
        // 明细按主单id分组
        Map<String,List<PurchaseReturnOrderDetailEntity>> detailMap = detailEntityList.stream().collect(Collectors.groupingBy(PurchaseReturnOrderDetailEntity::getMainId));
        // 分3种情况，采购退货(库存退货，退货补货)/采购退货(库存退货，退货退款)/采购退货（质检退货，退货补货）/采购退货（质检退货，退货退款）
        Map<String,PurchaseReturnOrderEntity> replenishmentInventoryMap = Maps.newHashMap();//采购退货(库存退货，退货补货，有采购订单)
        Map<String,PurchaseReturnOrderEntity> deductionInventoryMap = Maps.newHashMap();//采购退货(库存退货，退货退款，有采购订单)
        Map<String,PurchaseReturnOrderEntity> replenishmentQcMap = Maps.newHashMap();//采购退货(质检退货，退货补货)
        Map<String,PurchaseReturnOrderEntity> deductionQcRefundMap = Maps.newHashMap();//采购退货(质检退货，退货退款)
        Map<String,PurchaseReturnOrderEntity> inventoryNoPurchaseMap = Maps.newHashMap();//采购退货(库存退货，无采购订单)

        List<PurchaseReturnOrderDetailEntity> replenishmentInventoryDetailList = Lists.newArrayList();//采购退货(库存退货，退货补货)明细
        List<PurchaseReturnOrderDetailEntity> deductionInventoryDetailList = Lists.newArrayList();//采购退货(库存退货，退货退款)明细
        List<PurchaseReturnOrderDetailEntity> replenishmentQcDetailList = Lists.newArrayList();//采购退货(质检退货，退货补货)明细
        List<PurchaseReturnOrderDetailEntity> deductionQcRefundDetailList = Lists.newArrayList();//采购退货(质检退货，退货退款)明细
        List<PurchaseReturnOrderDetailEntity> inventoryNoPurchaseDetailList = Lists.newArrayList();//采购退货(库存退货，无采购订单)明细

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
    public void replenishmentInventory(Map<String,PurchaseReturnOrderEntity> replenishmentInventoryMap,
                                       List<PurchaseReturnOrderDetailEntity> replenishmentInventoryDetailList) { //采购退货(库存退货，退货补货)
        if(CollUtil.isNotEmpty(replenishmentInventoryDetailList)) { //采购退货(库存退货，退货补货)明细
            replenishmentInventoryDetailList.forEach(detail->{
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = replenishmentInventoryMap.get(detail.getMainId());
                // 使用补货数量 增加的在途
                InOutStockDTO inStockDTO = InOutStockDTO.initByReturnOrder(purchaseReturnOrderEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReplenishQty(), InventoryStatusEnum.IN_TRANSIT);
                InventoryInOutStockRuleDTO inTransitRule = new InventoryInOutStockRuleDTO();
                inTransitRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP.getCode());
                inTransitRule.setParamList(Lists.newArrayList(inStockDTO));
                inTransitRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.IN_TRANSIT, InventoryModeEnum.IN_STOCK)));
                inventoryTransCoreService.approveByRule(inTransitRule);
                // 使用实退数量 减少可用
                InventoryInOutStockRuleDTO usableRule = new InventoryInOutStockRuleDTO();
                usableRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP.getCode());
                InOutStockDTO inOutStockDTO = InOutStockDTO.initByReturnOrder(purchaseReturnOrderEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReturnQty(), InventoryStatusEnum.USABLE);
                usableRule.setParamList(Lists.newArrayList(inOutStockDTO));
                usableRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.USABLE, InventoryModeEnum.OUT_STOCK)));
                inventoryTransCoreService.approveByRule(usableRule);
            });

        }
    }

    /**
     * 采购退货(库存退货，退货退款) 库存操作
     * @param deductionInventoryMap
     * @param deductionInventoryDetailList
     */
    public void deductionInventory(Map<String,PurchaseReturnOrderEntity> deductionInventoryMap,
                                   List<PurchaseReturnOrderDetailEntity> deductionInventoryDetailList) { //采购退货(库存退货，退货退款)
        if(CollUtil.isNotEmpty(deductionInventoryDetailList)) {
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REF.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            deductionInventoryDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = deductionInventoryMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(purchaseReturnOrderEntity.getId());
                inOutStockDTO.setSourceCode(purchaseReturnOrderEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(purchaseReturnOrderEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                // 扣除捕获数量
                inOutStockDTO.setQty(detail.getReturnQty());
                inOutStockDTO.setWarehouseId(purchaseReturnOrderEntity.getReturnWarehouseId());
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
    public void replenishmentQcInventory(Map<String,PurchaseReturnOrderEntity> replenishmentQcMap,
                                         List<PurchaseReturnOrderDetailEntity> replenishmentQcDetailList) { //采购退货(质检退货，退货补货)
        if(CollUtil.isNotEmpty(replenishmentQcDetailList)) {
            replenishmentQcDetailList.forEach(detail->{
                // 使用补货数量 增加的在途
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = replenishmentQcMap.get(detail.getMainId());
                InOutStockDTO inStockDTO = InOutStockDTO.initByReturnOrder(purchaseReturnOrderEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReplenishQty(), InventoryStatusEnum.IN_TRANSIT);
                InventoryInOutStockRuleDTO inTransitRule = new InventoryInOutStockRuleDTO();
                inTransitRule.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_QC.getCode());
                inTransitRule.setParamList(Lists.newArrayList(inStockDTO));
                inTransitRule.setRules(Lists.newArrayList(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.IN_TRANSIT, InventoryModeEnum.IN_STOCK)));
                inventoryTransCoreService.approveByRule(inTransitRule);
                // 使用实退数量 减少待检
                InOutStockDTO outStockDTO = InOutStockDTO.initByReturnOrder(purchaseReturnOrderEntity, detail, InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, detail.getReturnQty(), InventoryStatusEnum.WAIT_QC);
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
    public void deductionQcInventory(Map<String,PurchaseReturnOrderEntity> deductionQcMap,
                                     List<PurchaseReturnOrderDetailEntity> deductionQcDetailList) { //采购退货(质检退货，退货退款)
        if(CollUtil.isNotEmpty(deductionQcDetailList)) {
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_QC_REF.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            deductionQcDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = deductionQcMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(purchaseReturnOrderEntity.getId());
                inOutStockDTO.setSourceCode(purchaseReturnOrderEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(purchaseReturnOrderEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                // 使用实退数量 减少待检
                inOutStockDTO.setQty(detail.getReturnQty());
                inOutStockDTO.setWarehouseId(purchaseReturnOrderEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setParamList(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
        }
    }

    public void unApproveInventory(List<PurchaseReturnOrderEntity> purchaseReturnOrderEntityList) {
        // 只有库存退货、质检退货（退货补货）才需要反审核
        List<String> unApproveIds = Lists.newArrayList();
        for (PurchaseReturnOrderEntity purchaseReturnOrder : purchaseReturnOrderEntityList) {
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
     * @param purchaseReturnOrderEntityList
     */
    private void autoAddPurchaseOrder (List<PurchaseReturnOrderEntity> purchaseReturnOrderEntityList) {
        if (CollectionUtils.isEmpty(purchaseReturnOrderEntityList)) {
            return;
        }
        List<String> ids = purchaseReturnOrderEntityList.stream().map(PurchaseReturnOrderEntity::getId).collect(Collectors.toList());
        //判断是否已生成采购订单
        List<PurchaseOrderEntity> poList = scmTaskFeign.listPoBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(poList)) {
            List<String> sourceIds = poList.stream().map(PurchaseOrderEntity::getSourceId).collect(Collectors.toList());
            String codes = purchaseReturnOrderEntityList.stream().filter(obj -> sourceIds.contains(obj.getId())).map(PurchaseReturnOrderEntity::getCode).distinct().collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(codes)) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_RETURN_REF_PO,codes);
            }
        }
        List<PurchaseReturnOrderEntity> returnList = purchaseReturnOrderEntityList
                .stream()
                .filter(obj -> StringUtils.isBlank(obj.getPurchaseOrderId()) && ReturnModeEnum.REPLENISHMENT.getCode().equals(obj.getReturnMode())
        ).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(returnList)) {
            log.info("采购退货单下存在采购订单或者退货扣款的采购退货单不支持自动生成");
            return;
        }
        log.info("自动生成退货采购订单，退货单号 = {}",returnList.stream().map(PurchaseReturnOrderEntity::getCode).collect(Collectors.joining(",")));

        //部门信息
        List<String> purchaseUserIds = returnList.stream().filter(obj -> StringUtils.isNotBlank(obj.getPurchaseUserId())).map(PurchaseReturnOrderEntity::getPurchaseOrderId).collect(Collectors.toList());
        List<SysDepartmentUserNumberDTO> departList = sysUserFeign.listDeptUserByUserIdList(purchaseUserIds);

        //供应商信息
        List<String> supplierIds = returnList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId())).map(PurchaseReturnOrderEntity::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIds);
        if (CollectionUtils.isEmpty(supplierList)) {
            log.error("未找到供应商信息，supplierIds = {} ",supplierIds);
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商联系人信息
        List<String> supplierContactIds = returnList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierContactId())).map(PurchaseReturnOrderEntity::getSupplierContactId).collect(Collectors.toList());
        List<SupplierContactEntity> supplierContactList = scmTaskFeign.listSupplierContactByIds(supplierContactIds);

        //退货明细信息
        List<String> mainIds = returnList.stream().map(PurchaseReturnOrderEntity::getId).collect(Collectors.toList());
        List<PurchaseReturnOrderDetailEntity> detailList = purchaseReturnOrderDetailService.listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        //主体公司信息
        List<BaseIdDTO.CodeDTO> companyCodeList = sysUserFeign.listAccountingCompanyByCodeList(Arrays.asList(companyCode));
        if (CollectionUtils.isEmpty(companyCodeList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }

        for (PurchaseReturnOrderEntity entity : returnList) {
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
            addDTO.setPurchaseOrgId(companyCodeList.get(0).getId());
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
            List<PurchaseReturnOrderDetailEntity> details = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(details)) {
                log.error("未找到退货明细信息，mainId = {} ",entity.getId());
                throw new ServiceException(ApiError.ERROR_99008);
            }
            List<PurchaseOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (PurchaseReturnOrderDetailEntity detailEntity:details) {
                PurchaseOrderDetailDTO.AddDTO addDetailDTO = new PurchaseOrderDetailDTO.AddDTO();
                addDetailDTO.setSourceDetailId(detailEntity.getId());
                addDetailDTO.setSkuId(detailEntity.getSkuId());
                addDetailDTO.setSkuNo(detailEntity.getSkuNo());
                addDetailDTO.setPurchaseQty(detailEntity.getReplenishQty());
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
    public void inventoryNoPo(Map<String,PurchaseReturnOrderEntity> inventoryMap,
                                       List<PurchaseReturnOrderDetailEntity> inventoryDetailList) { //采购退货(库存退货，无采购单)
        if(CollUtil.isNotEmpty(inventoryDetailList)) { //采购退货(库存退货，无采购单)明细
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP_NO_PURCHASE.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            inventoryDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = inventoryMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(purchaseReturnOrderEntity.getId());
                inOutStockDTO.setSourceCode(purchaseReturnOrderEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(purchaseReturnOrderEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                // 使用实退数量 减少可用
                inOutStockDTO.setQty(detail.getReturnQty());
                inOutStockDTO.setWarehouseId(purchaseReturnOrderEntity.getReturnWarehouseId());
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
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listByMainIds(ids);
        for (PurchaseReturnOrderDTO.PdaPagingViewDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<PurchaseReturnOrderDetailEntity> detailEntities = purchaseReturnOrderDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
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

            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(poDetailIds);
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
                    List<PurchaseReturnOrderDetailEntity> returnDetailEntityList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIds);
                    Integer returnQty = addDTO.getReturnQty();
                    for (PurchaseOrderDetailEntity entity : detailEntityList) {
                        Integer stockInQty = poInstockDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        //已退货
                        Integer alreadyReturnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

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

            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(poDetailIds);
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
                    List<PurchaseReturnOrderDetailEntity> returnDetailEntityList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIds);
                    Integer returnQty = updateDTO.getReturnQty();
                    for (PurchaseOrderDetailEntity entity : detailEntityList) {
                        Integer stockInQty = poInstockDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        //已退货
                        Integer alreadyReturnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !req.getMainId().equals(dto.getId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

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
    private void checkInventoryQty (List<PurchaseReturnOrderEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //采购退货明细信息
        List<String> mainIdList = list.stream().map(PurchaseReturnOrderEntity::getId).collect(Collectors.toList());
        List<PurchaseReturnOrderDetailEntity> detailList = purchaseReturnOrderDetailService.listByMainIds(mainIdList);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        //仓库Id
        List<String> returnWarehouseIdList = list.stream().map(PurchaseReturnOrderEntity::getReturnWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(returnWarehouseIdList);


        // 产品属性为费用或服务的sku忽略库存计算
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        for (PurchaseReturnOrderDetailEntity detailEntity :  detailList) {
            //采购退货信息
            PurchaseReturnOrderEntity entity = list.stream().filter(obj -> obj.getId().equals(detailEntity.getId()) && !Objects.equals(obj.getSourceType(), SourceTypeEnum.QC_INFO.getCode())).findFirst().orElse(null);
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
        List<PurchaseReturnOrderEntity> list = lambdaQuery().eq(PurchaseReturnOrderEntity::getSourceType, ReturnOrderSourceEnum.OTHER.getCode()).list();
        for (PurchaseReturnOrderEntity entity : list) {
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
}
