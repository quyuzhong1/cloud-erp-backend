package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.PurchaseReturnOrderMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.SuperServiceImpl;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private ProductOrderFeign productOrderFeign;

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
    private PurchaseStockInService purchaseStockInService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

    /**
     * 主页分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>
     **/
    @Override
    public PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> paging(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
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
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    return;
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                obj.setProductName(productDetailEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(PurchaseReturnOrderDTO.AddDTO dto) {
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = productOrderFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = productOrderFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        //获取用户信息
        SysUserDTO userDTO = sysUserFeign.getSysUserById(dto.getReturnUserId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getReturnOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getReturnWarehouseId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PO, BusinessNoTypeEnum.CODE_PO.getCode()));
        //设置收货单主表
        PurchaseReturnOrderEntity purchaseReturnOrderEntity = new PurchaseReturnOrderEntity();
        purchaseReturnOrderEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        purchaseReturnOrderEntity.setCode(code);
        purchaseReturnOrderEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
        purchaseReturnOrderEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        purchaseReturnOrderEntity.setSupplierId(orderSupplierByOrderId.getSupplierId());
        purchaseReturnOrderEntity.setSupplierName(orderSupplierByOrderId.getSupplierName());
        purchaseReturnOrderEntity.setSupplierContactId(orderSupplierByOrderId.getSupplierContactId());
        purchaseReturnOrderEntity.setSupplierContactName(orderSupplierByOrderId.getContactName());
        purchaseReturnOrderEntity.setReturnUserId(dto.getReturnUserId());
        purchaseReturnOrderEntity.setReturnUserName(userDTO.getUserName());
        purchaseReturnOrderEntity.setReturnOrgId(dto.getReturnOrgId());
        purchaseReturnOrderEntity.setReturnOrgName(sysAccountingCompanyEntity.getCompanyName());
        purchaseReturnOrderEntity.setBillDate(LocalDate.now());
        purchaseReturnOrderEntity.setReturnWarehouseId(dto.getReturnWarehouseId());
        purchaseReturnOrderEntity.setReturnWarehouseName(warehouseEntity.getName());
        purchaseReturnOrderEntity.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        purchaseReturnOrderEntity.setPurchaseUserName(purchaseOrderEntity.getPurchaseUserName());
        purchaseReturnOrderEntity.setSourceId(dto.getSourceId());
        purchaseReturnOrderEntity.setSourceType(dto.getSourceType());
        //保存主表信息
        this.save(purchaseReturnOrderEntity);

        //保存详情信息
        purchaseReturnOrderDetailService.add(dto, purchaseReturnOrderEntity.getId());
        return purchaseReturnOrderEntity.getId();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto) {
        //根据用户id获取用户信息
        SysUserDTO sysUserDTO = sysUserFeign.getSysUserById(dto.getReturnUserId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getReturnWarehouseId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getReturnOrgId());
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = productOrderFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = productOrderFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        //获取供应商联系人信息
        SupplierContactEntity supplierContactEntity = productOrderFeign.getSupplierContactById(dto.getSupplierContactId());
        LambdaUpdateWrapper<PurchaseReturnOrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(PurchaseReturnOrderEntity::getPurchaseOrderId, dto.getPurchaseOrderId());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnMode, dto.getReturnMode());
        updateWrapper.set(PurchaseReturnOrderEntity::getPurchaseOrderCode, dto.getPurchaseOrderCode());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnUserId, dto.getReturnUserId());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnUserName, sysUserDTO.getUserName());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnOrgId, dto.getReturnOrgId());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnOrgName, sysAccountingCompanyEntity.getCompanyName());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnWarehouseId, dto.getReturnWarehouseId());
        updateWrapper.set(PurchaseReturnOrderEntity::getReturnWarehouseName, warehouseEntity.getName());
        updateWrapper.set(PurchaseReturnOrderEntity::getReceiceRemark, dto.getReturnRemark());
        updateWrapper.set(PurchaseReturnOrderEntity::getPurchaseUserId, purchaseOrderEntity.getPurchaseUserId());
        updateWrapper.set(PurchaseReturnOrderEntity::getPurchaseUserName, purchaseOrderEntity.getPurchaseUserName());
        updateWrapper.set(PurchaseReturnOrderEntity::getSupplierId, orderSupplierByOrderId.getSupplierId());
        updateWrapper.set(PurchaseReturnOrderEntity::getSupplierName, orderSupplierByOrderId.getSupplierName());
        updateWrapper.set(PurchaseReturnOrderEntity::getSupplierContactId, dto.getSupplierContactId());
        updateWrapper.set(PurchaseReturnOrderEntity::getSupplierContactName, supplierContactEntity.getPerson());
        updateWrapper.eq(PurchaseReturnOrderEntity::getId, dto.getId());
        //更新收货单主表信息
        this.update(updateWrapper);
        //更新收货单详情表信息
        return purchaseReturnOrderDetailService.update(dto);
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewDTO
     **/
    @Override
    public PurchaseReturnOrderDTO.ViewDTO view(String id) {
        PurchaseReturnOrderDTO.ViewDTO viewDTO = new PurchaseReturnOrderDTO.ViewDTO();
        PurchaseReturnOrderEntity PurchaseReturnOrderEntity = this.getById(id);
        BeanMapperUtils.copy(PurchaseReturnOrderEntity,viewDTO);
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(PurchaseReturnOrderEntity.getReturnWarehouseId());
        //获取采购用户部门
        SysDepartmentDTO purchaseUserDept = sysUserFeign.getUserDeptById(PurchaseReturnOrderEntity.getPurchaseUserId());
        viewDTO.setSupplierAddress(warehouseEntity.getAddress());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setPurchaseUserDeptId(purchaseUserDept.getId());
        viewDTO.setPurchaseUserDeptName(purchaseUserDept.getName());

        if (SourceTypeEnum.QC_BILL.getType().equals(PurchaseReturnOrderEntity.getSourceType())) {
            viewDTO.setSourceTypeName(ReturnOrderSourceEnum.QC.getCode());
        } else {
            viewDTO.setSourceTypeName(ReturnOrderSourceEnum.OTHER.getCode());
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
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = productOrderFeign.listPurchaseOrderDetailById(detailId);
        for (PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity : detail) {
            Integer stockInQty = purchaseStockInDetailService.getStockInQty(purchaseReturnOrderDetailEntity.getPurchaseOrderDetailId());
            PurchaseReturnOrderDetailDTO.ViewDTO detailView = new PurchaseReturnOrderDetailDTO.ViewDTO();
            BeanMapperUtils.copy(purchaseReturnOrderDetailEntity,detailView);
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(detailView.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            detailView.setStockInQty(stockInQty);
            detailView.setTotalPrice(purchaseReturnOrderDetailEntity.getReturnPrice().multiply(BigDecimal.valueOf(Double.valueOf(purchaseReturnOrderDetailEntity.getRealityReturnQty()))));
            detailView.setReturnPrice(purchaseOrderDetailEntity.getTaxPrice());
            //获取sku信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detailView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            detailViewDTOS.add(detailView);
        }
        viewDTO.setPurchasePriceDetailList(detailViewDTOS);
        return viewDTO;
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
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

        if (count == 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程

        //更新审核状态
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
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
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean update = this.update(dto);
        if (update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<PurchaseReturnOrderEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //判断是否是审核中的状态
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count == 0) {
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
        } else {
            //审核不通过
            lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(PurchaseReturnOrderEntity::getId, ids)
                    .update();
        }
        return Boolean.TRUE;
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<PurchaseReturnOrderEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //已审核支持反审核
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
        ).count();

        if (count == 0) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        //TODO 待加审核流程
        //修改状态为待提交
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(@RequestBody @Validated List<String> ids) {
        List<PurchaseReturnOrderEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count == 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(PurchaseReturnOrderEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
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
                ||entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count == 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        //修改状态为待提交
        lambdaUpdate().set(PurchaseReturnOrderEntity::getInvalidStatus, Boolean.TRUE)
                .set(PurchaseReturnOrderEntity::getInvalidRemark, remark)
                .set(PurchaseReturnOrderEntity::getInvalidTime, LocalDateTime.now())
                .in(PurchaseReturnOrderEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
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

        if (count == 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除详情表
        purchaseReturnOrderDetailService.delete(ids);

        //删除主表
        return this.removeByIds(ids);
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    public Boolean exportExcel(@RequestBody PurchaseReturnOrderDTO.PagingParamDTO dto, HttpServletResponse response) {
      /*  List<WarehouseReceiveExportExcelDTO> warehouseReceiveExportExcelDTOS = baseMapper.warehouseReceiveExportExcel(dto);

        //获取sku的id集合
        List<String> skuIdList = warehouseReceiveExportExcelDTOS.stream().map(WarehouseReceiveExportExcelDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        warehouseReceiveExportExcelDTOS.forEach(obj -> {

            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            obj.setProductName(productDetailEntity.getName());
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
        });
        String fileName = "仓库入库单";
        try {
            ExcelUtil.export(fileName, "仓库入库单", warehouseReceiveExportExcelDTOS, WarehouseReceiveExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }*/
        return Boolean.TRUE;
    }


    /**
     * 采购订单-关联的退货订单
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    @Override
    public List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> purchaseOrderRefReturn(String purchaseOrderId) {
        List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> orderRefReceiveDTOS = baseMapper.purchaseOrderRefReceive(purchaseOrderId);
        //获取采购单详情表id集合
        List<String> orderDetailIds = orderRefReceiveDTOS.stream().map(PurchaseReturnOrderDTO.OrderRefReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = productOrderFeign.listPurchaseOrderDetailById(orderDetailIds);
        for (PurchaseReturnOrderDTO.OrderRefReceiveDTO orderRefReceiveDTO : orderRefReceiveDTOS) {
            orderRefReceiveDTO.setApproveStatusName(ApproveStatusEnum.getName(orderRefReceiveDTO.getApproveStatus()));
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(orderRefReceiveDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            orderRefReceiveDTO.setProductName(purchaseOrderDetailEntity.getProductName());
        }
        return orderRefReceiveDTOS;
    }

    /**
     * 获取产品签收数量
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param PurchaseOrderId PurchaseOrderId
     * @param skuId skuId
     * @return java.lang.Integer
     **/
    private Integer getReceiveQty(String PurchaseOrderId, String skuId) {
        return baseMapper.getReceiveQty(PurchaseOrderId, skuId);
    }
}
