package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDetailDTO;
import com.erp.model.wms.dto.ReturnOrderExcelDTO;
import com.erp.model.wms.dto.excel.ReturnOrderExportExcelDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
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
 * @author LUO_WG
 * @since 2023-04-07
 */
@Slf4j
@Service
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
    private PoInstockService poInstockService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SyncKingdeeReturnOrderService syncKingdeeReturnOrderService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;


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
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(PurchaseReturnOrderDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setPurchaseOrderCode(null);
                    obj.setSupplierName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setReturnModeName(ReturnModeEnum.getName(obj.getReturnMode()));
                list.add(obj.getId());
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
        //获取用户信息
        SysUserDTO userDTO = sysUserFeign.getSysUserById(dto.getReturnUserId());
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
        purchaseReturnOrderEntity.setBillDate(LocalDate.now());
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

        SysUserDTO purchaseUser = sysUserFeign.getSysUserById(dto.getPurchaseUserId());
        purchaseReturnOrderEntity.setPurchaseUserId(dto.getPurchaseUserId());
        purchaseReturnOrderEntity.setPurchaseUserName(purchaseUser.getUserName());
        purchaseReturnOrderEntity.setSupplierId(dto.getSupplierId());
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(dto.getSupplierId());
        purchaseReturnOrderEntity.setSupplierName(supplierEntity.getName());
        purchaseReturnOrderEntity.setSupplierContactId(dto.getSupplierContactId());
        if (StringUtils.isNotBlank(dto.getSupplierContactId())) {
            SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(dto.getSupplierContactId());
            purchaseReturnOrderEntity.setSupplierContactName(supplierContactById.getPerson());
        }
        purchaseReturnOrderEntity.setReturnUserName(userDTO.getUserName());
        purchaseReturnOrderEntity.setReturnOrgName(sysAccountingCompanyEntity.getCompanyName());
        purchaseReturnOrderEntity.setBillDate(LocalDate.now());
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


        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(purchaseReturnOrderEntity.getSupplierId());
        viewDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(purchaseReturnOrderEntity.getPurchaseOrderId());

        if (ObjectUtil.isNotEmpty(purchaseOrderEntity)) {
            viewDTO.setPurchaseUserDeptId(purchaseOrderEntity.getPurchaseDeptId());
            viewDTO.setPurchaseUserDeptName(purchaseOrderEntity.getPurchaseDeptName());
        }

        if (SourceTypeEnum.QC_BILL.getCode().equals(purchaseReturnOrderEntity.getSourceType())) {
            viewDTO.setSourceType(ReturnOrderSourceEnum.QC.getCode());
        } else {
            viewDTO.setSourceType(ReturnOrderSourceEnum.OTHER.getCode());
        }
        //创库保存详情表的集合
        List<PurchaseReturnOrderDetailDTO.ViewDTO> detailViewDTOS = new ArrayList<>();
        //根据收货单主表id获取详情信息
        List<PurchaseReturnOrderDetailEntity> detail = purchaseReturnOrderDetailService.getDetailByMainId(id);
        //获取sku的id集合
        List<String> skuIdList = detail.stream().map(PurchaseReturnOrderDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取采购单详情的id集合
        List<String> detailId = detail.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);

        List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(detailId);
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
            detailView.setTotalPrice(purchaseReturnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(purchaseReturnOrderDetailEntity.getReturnQty()))));

            //获取sku信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detailView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            detailView.setVariantProperty(productDetailEntity.getVariantProperty());
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
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(baseApproveParamDTO.getType())) {
            LoginUser userInfo = commonService.getUserInfo();
            //审核通过
            lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(PurchaseReturnOrderEntity::getApproveUserId, userInfo.getUid())
                    .set(PurchaseReturnOrderEntity::getApproveUserName, userInfo.getUserName())
                    .set(PurchaseReturnOrderEntity::getApproveTime, LocalDateTime.now())
                    .in(PurchaseReturnOrderEntity::getId, ids)
                    .update();

            for (PurchaseReturnOrderEntity purchaseReturnOrderEntity : purchaseReturnOrderEntityList) {
                List<PurchaseOrderDetailEntity> list = new ArrayList<>();
                List<PurchaseReturnOrderDetailEntity> detailByMainId = purchaseReturnOrderDetailService.getDetailByMainId(purchaseReturnOrderEntity.getId());
                List<String> detailId = detailByMainId.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
                //退料扣款
                if (purchaseReturnOrderEntity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {
                    List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
                    detailByMainId.forEach(returnOrderDetailEntity -> {
                        PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(returnOrderDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                            purchaseOrderDetailEntity.setPurchaseAmount(purchaseOrderDetailEntity.getPurchaseAmount().subtract(returnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(returnOrderDetailEntity.getReturnQty())))));
                            list.add(purchaseOrderDetailEntity);
                        }

                    });
                    scmTaskFeign.updatePurchaseOrderDetailByIdBatch(list);
                }

                if (StringUtils.isNotBlank(purchaseReturnOrderEntity.getPurchaseOrderId())) {
                    updateArrivalState(purchaseReturnOrderEntity.getPurchaseOrderId());
                }
            }

            // 更新库存信息
            updateInventoryTransCore(purchaseReturnOrderEntityList);
            //审核通过发送金蝶
            purchaseReturnOrderEntityList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));
        } else {
            //审核不通过
            lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(PurchaseReturnOrderEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = purchaseReturnOrderEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购退货单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "审核操作");

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
        //TODO 待加审核流程
        //修改状态为待提交
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();

        for (PurchaseReturnOrderEntity purchaseReturnOrderEntity : purchaseReturnOrderEntityList) {
            List<PurchaseOrderDetailEntity> list = new ArrayList<>();
            List<PurchaseReturnOrderDetailEntity> detailByMainId = purchaseReturnOrderDetailService.getDetailByMainId(purchaseReturnOrderEntity.getId());
            List<String> detailId = detailByMainId.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
            //退料扣款
            if (purchaseReturnOrderEntity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {
                List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
                detailByMainId.forEach(returnOrderDetailEntity -> {
                    PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(returnOrderDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
                    purchaseOrderDetailEntity.setPurchaseAmount(purchaseOrderDetailEntity.getPurchaseAmount().add(returnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(returnOrderDetailEntity.getReturnQty())))));
                    list.add(purchaseOrderDetailEntity);
                });
                scmTaskFeign.updatePurchaseOrderDetailByIdBatch(list);
            }
            if (StringUtils.isNotBlank(purchaseReturnOrderEntity.getPurchaseOrderId())) {
                updateArrivalState(purchaseReturnOrderEntity.getPurchaseOrderId());
            }

        }

        unApproveInventory(purchaseReturnOrderEntityList); // 库存反审核操作

        //操作日志
        List<Pair<String, String>> pairList = purchaseReturnOrderEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个采购退货单【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "反审核操作");
        //审核通过发送金蝶
        purchaseReturnOrderEntityList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode()));
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

        //审核通过发送金蝶
        warehouseReceiveList.forEach(obj -> syncKingdeeReturnOrderService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_INVALID.getCode()));
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
        returnOrderExcelDTOS.forEach(obj -> {

            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setReturnModeName(ReturnModeEnum.getName(obj.getReturnMode()));

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
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            PurchaseReturnOrderDTO.PagingParamDTO pagingParamDTO = new PurchaseReturnOrderDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            PurchaseReturnOrderDTO.ReturnOrderCountDTO resultDTO = new PurchaseReturnOrderDTO.ReturnOrderCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PurchaseChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PurchaseChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
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
                .eq(PurchaseReturnOrderEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), PurchaseReturnOrderEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), PurchaseReturnOrderEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchaseReturnOrderEntity::getSyncKingdeeId, syncKingdeeId)
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchaseReturnOrderEntity::getSyncOperate, syncOperate)
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
     * @param PurchaseOrderId PurchaseOrderId
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/28 11:37
     **/
    @Override
    public void updateArrivalState(String PurchaseOrderId) {
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailByOrderId(PurchaseOrderId);
        List<String> podIds = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseReturnOrderDetailEntity> returnDetailEntityList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIds);
        List<WarehouseReceiveDetailEntity> receiveDetailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        for (PurchaseOrderDetailEntity orderDetailEntity : purchaseOrderDetailEntities) {

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
            scmTaskFeign.updatePurchaseOrderDetailById(purchaseOrderDetailEntity);
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
        // 分3种情况，采购退货(库存退货，退货补货)/采购退货(库存退货，退货退款)/采购退货（质检退货，退货补货）
        Map<String,PurchaseReturnOrderEntity> replenishmentInventoryMap = Maps.newHashMap();//采购退货(库存退货，退货补货)
        Map<String,PurchaseReturnOrderEntity> deductionInventoryMap = Maps.newHashMap();//采购退货(库存退货，退货退款)
        Map<String,PurchaseReturnOrderEntity> replenishmentQcMap = Maps.newHashMap();//采购退货(质检退货，退货补货)

        List<PurchaseReturnOrderDetailEntity> replenishmentInventoryDetailList = Lists.newArrayList();//采购退货(库存退货，退货补货)明细
        List<PurchaseReturnOrderDetailEntity> deductionInventoryDetailList = Lists.newArrayList();//采购退货(库存退货，退货退款)明细
        List<PurchaseReturnOrderDetailEntity> replenishmentQcDetailList = Lists.newArrayList();//采购退货(质检退货，退货补货)明细

        mainMap.forEach((mainId, purchaseReturnOrder)->{
            String sourceType = purchaseReturnOrder.getSourceType();
            if(!Objects.equals(sourceType, SourceTypeEnum.QC_BILL.getCode())) { // 库存退货
                String returnMode = purchaseReturnOrder.getReturnMode(); // 退货方式
                if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())) { // 退货补货
                    replenishmentInventoryMap.put(mainId, purchaseReturnOrder);
                    replenishmentInventoryDetailList.addAll(detailMap.get(mainId));
                } else if (Objects.equals(returnMode, ReturnModeEnum.DEDUCTION.getCode())) { // 退货退款
                    deductionInventoryMap.put(mainId, purchaseReturnOrder);
                    deductionInventoryDetailList.addAll(detailMap.get(mainId));
                }
            } else { // 质检退货
                String returnMode = purchaseReturnOrder.getReturnMode(); // 退货方式
                if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())) { // 退货补货
                    replenishmentQcMap.put(mainId, purchaseReturnOrder);
                    replenishmentQcDetailList.addAll(detailMap.get(mainId));
                }
            }
        });
        replenishmentInventory(replenishmentInventoryMap, replenishmentInventoryDetailList); //采购退货(库存退货，退货补货)
        deductionInventory(deductionInventoryMap, deductionInventoryDetailList);//采购退货(库存退货，退货退款)
        replenishmentQcInventory(replenishmentQcMap, replenishmentQcDetailList);//采购退货(质检退货，退货补货)
    }

    /**
     * 采购退货(库存退货，退货补货) 库存操作
     * @param replenishmentInventoryMap
     * @param replenishmentInventoryDetailList
     */
    public void replenishmentInventory(Map<String,PurchaseReturnOrderEntity> replenishmentInventoryMap,
                                       List<PurchaseReturnOrderDetailEntity> replenishmentInventoryDetailList) { //采购退货(库存退货，退货补货)
        if(CollUtil.isNotEmpty(replenishmentInventoryDetailList)) { //采购退货(库存退货，退货补货)明细
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_REP.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            replenishmentInventoryDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = replenishmentInventoryMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(purchaseReturnOrderEntity.getId());
                inOutStockDTO.setSourceCode(purchaseReturnOrderEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(purchaseReturnOrderEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setQty(detail.getReplenishQty()); // 补货数量
                inOutStockDTO.setWarehouseId(purchaseReturnOrderEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation("");
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setMembers(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
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
                inOutStockDTO.setQty(detail.getDeductAmountQty()); // 扣款数量
                inOutStockDTO.setWarehouseId(purchaseReturnOrderEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation("");
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setMembers(receiveMembers);
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
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RETURN_QC.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            replenishmentQcDetailList.forEach(detail->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER);
                PurchaseReturnOrderEntity purchaseReturnOrderEntity = replenishmentQcMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(purchaseReturnOrderEntity.getId());
                inOutStockDTO.setSourceCode(purchaseReturnOrderEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(purchaseReturnOrderEntity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setQty(detail.getReplenishQty()); // 补货数量
                inOutStockDTO.setWarehouseId(purchaseReturnOrderEntity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation("");
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setMembers(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
        }
    }

    public void unApproveInventory(List<PurchaseReturnOrderEntity> purchaseReturnOrderEntityList) {
        // 只有库存退货、质检退货（退货补货）才需要反审核
        List<String> unApproveIds = Lists.newArrayList();
        for (PurchaseReturnOrderEntity purchaseReturnOrder : purchaseReturnOrderEntityList) {
            String sourceType = purchaseReturnOrder.getSourceType();
            if(!Objects.equals(sourceType, SourceTypeEnum.QC_BILL.getCode())) { // 库存退货
                unApproveIds.add(purchaseReturnOrder.getId());
            } else { // 质检退货
                String returnMode = purchaseReturnOrder.getReturnMode(); // 退货方式
                if(Objects.equals(returnMode, ReturnModeEnum.REPLENISHMENT.getCode())) { // 退货补货
                    unApproveIds.add(purchaseReturnOrder.getId());
                }
            }
        }
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.PURCHASE_RETURN_ORDER, unApproveIds);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
    }

}
