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
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Slf4j
@Service
public class WarehouseReceiveServiceImpl extends SuperServiceImpl<WarehouseReceiveMapper, WarehouseReceiveEntity> implements WarehouseReceiveService {

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
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private PurchaseStockInService purchaseStockInService;

    /**
     * 主页分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>
     **/
    @Override
    public PagingVO<WarehouseReceiveDTO.PagingViewDTO> paging(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<WarehouseReceiveDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        //明细数据
        List<WarehouseReceiveDTO.PagingViewDTO> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(WarehouseReceiveDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
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
    public String add(WarehouseReceiveDTO.AddDTO dto) {
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = productOrderFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = productOrderFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        //获取用户信息
        SysUserDTO userDTO = sysUserFeign.getSysUserById(dto.getReceiveUserId());
        //获取用户部门
        SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(dto.getReceiveDeptId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getReceiveOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PO, BusinessNoTypeEnum.CODE_PO.getCode()));
        //设置收货单主表
        WarehouseReceiveEntity warehouseReceiveEntity = new WarehouseReceiveEntity();
        warehouseReceiveEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        warehouseReceiveEntity.setCode(code);
        warehouseReceiveEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
        warehouseReceiveEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        warehouseReceiveEntity.setSupplierId(orderSupplierByOrderId.getSupplierId());
        warehouseReceiveEntity.setSupplierName(orderSupplierByOrderId.getSupplierName());
        warehouseReceiveEntity.setReceiveUserId(dto.getReceiveUserId());
        warehouseReceiveEntity.setReceiveUserName(userDTO.getUserName());
        warehouseReceiveEntity.setReceiveDeptId(dto.getReceiveDeptId());
        warehouseReceiveEntity.setReceiveDeptName(departmentDTO.getName());
        warehouseReceiveEntity.setReceiveOrgId(dto.getReceiveOrgId());
        warehouseReceiveEntity.setReceiveOrgName(sysAccountingCompanyEntity.getCompanyName());
        warehouseReceiveEntity.setBillDate(dto.getBillDate());
        warehouseReceiveEntity.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        warehouseReceiveEntity.setDeliveryWarehouseName(warehouseEntity.getName());
        warehouseReceiveEntity.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        warehouseReceiveEntity.setPurchaseUserName(purchaseOrderEntity.getPurchaseUserName());
        //保存主表信息
        this.save(warehouseReceiveEntity);

        //保存详情信息
        warehouseReceiveDetailService.add(dto, warehouseReceiveEntity.getId());
        return warehouseReceiveEntity.getId();
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
    public Boolean update(WarehouseReceiveDTO.UpdateDTO dto) {
        //根据用户id获取用户信息
        SysUserDTO sysUserDTO = sysUserFeign.getSysUserById(dto.getReceiveUserId());
        //获取用户部门
        SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(dto.getReceiveDeptId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());
        LambdaUpdateWrapper<WarehouseReceiveEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(WarehouseReceiveEntity::getReceiveUserId, dto.getReceiveUserId());
        updateWrapper.set(WarehouseReceiveEntity::getReceiveUserName, sysUserDTO.getUserName());
        updateWrapper.set(WarehouseReceiveEntity::getReceiveDeptId, dto.getReceiveDeptId());
        updateWrapper.set(WarehouseReceiveEntity::getReceiveDeptName, departmentDTO.getName());
        updateWrapper.set(WarehouseReceiveEntity::getDeliveryWarehouseId, dto.getDeliveryWarehouseId());
        updateWrapper.set(WarehouseReceiveEntity::getDeliveryWarehouseName, warehouseEntity.getName());
        updateWrapper.eq(WarehouseReceiveEntity::getId, dto.getId());
        //更新收货单主表信息
        this.update(updateWrapper);
        //更新收货单详情表信息
        return warehouseReceiveDetailService.update(dto);
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO
     **/
    @Override
    public WarehouseReceiveDTO.ViewDTO view(String id) {
        WarehouseReceiveDTO.ViewDTO viewDTO = new WarehouseReceiveDTO.ViewDTO();
        WarehouseReceiveEntity warehouseReceiveEntity = this.getById(id);
        BeanMapperUtils.copy(warehouseReceiveEntity,viewDTO);
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseReceiveEntity.getDeliveryWarehouseId());
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = productOrderFeign.getPurchaseOrderById(warehouseReceiveEntity.getPurchaseOrderId());
        viewDTO.setSupplierAddress(warehouseEntity.getAddress());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setIsFirstMassProduct(purchaseOrderEntity.getIsFirstMassProduct());
        //创库保存详情表的集合
        List<WarehouseReceiveDetailDTO.ViewDTO> detailViewDTOS = new ArrayList<>();
        //根据收货单主表id获取详情信息
        List<WarehouseReceiveDetailEntity> detail = warehouseReceiveDetailService.getDetailByMainId(id);
        //获取sku的id集合
        List<String> skuIdList = detail.stream().map(WarehouseReceiveDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取采购单详情的id集合
        List<String> detailId = detail.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = productOrderFeign.listPurchaseOrderDetailById(detailId);
        for (WarehouseReceiveDetailEntity warehouseReceiveDetailEntity : detail) {
            WarehouseReceiveDetailDTO.ViewDTO detailView = new WarehouseReceiveDetailDTO.ViewDTO();
            BeanMapperUtils.copy(warehouseReceiveDetailEntity,detailView);
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(detailView.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            Integer receiveQty = getReceiveQty(warehouseReceiveEntity.getPurchaseOrderId(), detailView.getSkuId());
            detailView.setNotReceiveQty(purchaseOrderDetailEntity.getPurchaseQty() - receiveQty);
            //获取sku信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detailView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            detailViewDTOS.add(detailView);
        }
        viewDTO.setWarehouseReceiveDetailList(detailViewDTOS);
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(warehouseReceiveList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count == 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程

        //更新审核状态
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
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
    public Boolean addAndSubmit(WarehouseReceiveDTO.AddDTO dto) {
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
    public Boolean updateAndSubmit(WarehouseReceiveDTO.UpdateDTO dto) {
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
            lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(WarehouseReceiveEntity::getApproveUserId, userInfo.getUid())
                    .set(WarehouseReceiveEntity::getApproveUserName, userInfo.getUserName())
                    .set(WarehouseReceiveEntity::getApproveTime, LocalDateTime.now())
                    .in(WarehouseReceiveEntity::getId, ids)
                    .update();
        } else {
            //审核不通过
            lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(WarehouseReceiveEntity::getId, ids)
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
        lambdaUpdate().set(WarehouseReceiveEntity::getInvalidStatus, Boolean.TRUE)
                .set(WarehouseReceiveEntity::getInvalidRemark, remark)
                .set(WarehouseReceiveEntity::getInvalidTime, LocalDateTime.now())
                .in(WarehouseReceiveEntity::getId, ids)
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
        warehouseReceiveDetailService.delete(ids);

        //删除主表
        return this.removeByIds(ids);
    }

    /**
     * 下推入库单列表查询
     * @Author Luo_WG
     * @Date 2023/4/14 14:24
     * @param id id
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.GenerateStockInViewDTO>
     **/
    @Override
    public List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(String id) {
        List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInViewDTOS = baseMapper.generateStockInView(id);
        LoginUser userInfo = commonService.getUserInfo();
        //获取sku的id集合
        List<String> skuIdList = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<ProductDetailEntity> byIdList = plmTaskFeign.getByIdList(skuIdList);

        generateStockInViewDTOS.forEach(req -> {
            ProductDetailEntity productDetailEntity = byIdList.stream().filter(obj -> req.getSkuId().equals(obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            req.setProductName(productDetailEntity.getName());
            req.setStockInDate(LocalDate.now());
            req.setStockInUserName(userInfo.getUid());
            req.setReceiveQty(getReceiveQty(req.getPurchaseOrderId(), req.getSkuId()));
            req.setUnStockInQty(0);
            req.setStockInQty(0);
            req.setExceedQty(0);

        });
        return null;
    }

    /**
     * 下推入库单
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param id id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean generateStockIn(String id) {
        PurchaseStockInDTO.AddDTO addDTO = new PurchaseStockInDTO.AddDTO();
        addDTO.setSourceId(id);
        addDTO.setSourceType(SourceTypeEnum.WAREHOUSE_RECEIVE.getType());
        WarehouseReceiveEntity warehouseReceiveEntity = this.getById(id);

        addDTO.setPurchaseOrderId(warehouseReceiveEntity.getPurchaseOrderId());
        addDTO.setDeliveryWarehouseId(warehouseReceiveEntity.getDeliveryWarehouseId());
        addDTO.setStockInUserId(warehouseReceiveEntity.getReceiveUserId());
        addDTO.setStockInDeptId(warehouseReceiveEntity.getDeliveryWarehouseId());

        //设置明细
        List<PurchaseStockInDetailDTO.AddDTO> detailDTOList = new ArrayList<>();
        List<WarehouseReceiveDetailEntity> detailByMainId = warehouseReceiveDetailService.getDetailByMainId(id);
        detailByMainId.forEach(req -> {
            PurchaseStockInDetailDTO.AddDTO detailDTO = new PurchaseStockInDetailDTO.AddDTO();
            BeanMapperUtils.copy(req,detailDTO);

        });
        addDTO.setDetails(detailDTOList);
        purchaseStockInService.add(addDTO);

        return null;
    }

    /**
     * 采购订单-关联的收货单据
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    @Override
    public List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(String purchaseOrderId) {
        List<WarehouseReceiveDTO.OrderRefReceiveDTO> orderRefReceiveDTOS = baseMapper.purchaseOrderRefReceive(purchaseOrderId);
        //获取采购单详情表id集合
        List<String> orderDetailIds = orderRefReceiveDTOS.stream().map(WarehouseReceiveDTO.OrderRefReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = productOrderFeign.listPurchaseOrderDetailById(orderDetailIds);
        for (WarehouseReceiveDTO.OrderRefReceiveDTO orderRefReceiveDTO : orderRefReceiveDTOS) {
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
