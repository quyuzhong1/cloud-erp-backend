package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Slf4j
@Service
public class WarehouseReceiveServiceImpl extends SuperServiceImpl<WarehouseReceiveMapper, WarehouseReceiveEntity> implements WarehouseReceiveService {

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
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private PurchaseStockInService purchaseStockInService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

    @Resource
    private QcRuleService qcRuleService;

    @Resource
    private QcInfoService qcInfoService;

    /**
     * 主页分页查询
     *
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     **/
    @Override
    public PagingVO<WarehouseReceiveDTO.PagingViewDTO> paging(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<WarehouseReceiveDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<WarehouseReceiveDTO.PagingViewDTO> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(WarehouseReceiveDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = records.stream().map(WarehouseReceiveDTO.PagingViewDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);

        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setPurchaseOrderCode(null);
                    obj.setSupplierName(null);
                    obj.setApproveStatusName(null);
                    obj.setApproveStatus(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                obj.setPurchaseQty(purchaseOrderDetailEntity.getPurchaseQty());
                obj.setProductName(productDetailEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
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
    public List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            WarehouseReceiveDTO.PagingParamDTO pagingParamDTO = new WarehouseReceiveDTO.PagingParamDTO();
            pagingParamDTO.setParam(dto.getParam());
            WarehouseReceiveDTO.WarehouseReceiveCountDTO resultDTO = new WarehouseReceiveDTO.WarehouseReceiveCountDTO();
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
    public String add(WarehouseReceiveDTO.AddDTO dto) {
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        //获取用户信息
        SysUserDTO userDTO = null;
        if (StringUtils.isNotBlank(dto.getReceiveDeptId())) {
            userDTO = sysUserFeign.getSysUserById(dto.getReceiveUserId());
        }
        //获取用户部门
        SysDepartmentUserNumberDTO departmentDTO = null;
        if (StringUtils.isNotBlank(dto.getReceiveDeptId())) {
            departmentDTO = sysUserFeign.getDeptByUserId(dto.getReceiveUserId());
        }

        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(purchaseOrderEntity.getReceiveOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGSH, BusinessNoTypeEnum.CODE_CGSH.getCode()));
        //设置收货单主表
        WarehouseReceiveEntity warehouseReceiveEntity = new WarehouseReceiveEntity();

        warehouseReceiveEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        warehouseReceiveEntity.setCode(code);
        warehouseReceiveEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
        warehouseReceiveEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        warehouseReceiveEntity.setSupplierId(orderSupplierByOrderId.getSupplierId());
        warehouseReceiveEntity.setSupplierName(orderSupplierByOrderId.getSupplierName());
        warehouseReceiveEntity.setReceiveUserId(dto.getReceiveUserId());
        if (userDTO != null) {
            warehouseReceiveEntity.setReceiveUserName(userDTO.getUserName());
        }
        warehouseReceiveEntity.setReceiveDeptId(dto.getReceiveDeptId());
        if (departmentDTO != null) {
            warehouseReceiveEntity.setReceiveDeptName(departmentDTO.getDepartmentName());
        }
        warehouseReceiveEntity.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        warehouseReceiveEntity.setReceiveOrgName(sysAccountingCompanyEntity.getCompanyName());
        warehouseReceiveEntity.setBillDate(dto.getBillDate());
        warehouseReceiveEntity.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        warehouseReceiveEntity.setDeliveryWarehouseName(warehouseEntity.getName());
        warehouseReceiveEntity.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        warehouseReceiveEntity.setPurchaseUserName(purchaseOrderEntity.getPurchaseUserName());
        warehouseReceiveEntity.setCreateUserId(dto.getCreateUserId());
        warehouseReceiveEntity.setCreateUserName(dto.getCreateUserId());
        warehouseReceiveEntity.setUpdateUserId(dto.getUpdateUserId());
        warehouseReceiveEntity.setUpdateUserName(dto.getUpdateUserName());
        //保存主表信息
        this.save(warehouseReceiveEntity);

        //保存详情信息
        warehouseReceiveDetailService.add(dto, warehouseReceiveEntity.getId());

        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("新增了一个收货单【%s】", code), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), warehouseReceiveEntity.getId(), "新增操作");

        //修改到货状态
        updateArrivalState(warehouseReceiveEntity);
        return warehouseReceiveEntity.getId();
    }

    //修改到货状态
    private void updateArrivalState(WarehouseReceiveEntity warehouseReceiveEntity) {
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailByOrderId(warehouseReceiveEntity.getPurchaseOrderId());
        List<String> podIds = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        List<WarehouseReceiveDetailEntity> detailByMainId = warehouseReceiveDetailService.getDetailByMainId(warehouseReceiveEntity.getId());

        for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : purchaseOrderDetailEntities) {
                    /*if (!purchaseOrderDetailEntity.getArrivalStatus().equals(ArrivalStatusEnum.ARRIVED.getCode())) {

                    }*/
            Integer reduce = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
//            Integer thisReduce = detailByMainId.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);

            if (reduce > purchaseOrderDetailEntity.getPurchaseQty()) {
                throw new ServiceException(ApiError.ERROR_98058);
            }
            getArrivalState(reduce, purchaseOrderDetailEntity.getPurchaseQty(), purchaseOrderDetailEntity.getId());
        }
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
    public Boolean update(WarehouseReceiveDTO.UpdateDTO dto) {
        //根据用户id获取用户信息
        SysUserDTO sysUserDTO = sysUserFeign.getSysUserById(dto.getReceiveUserId());
        //获取用户部门
        SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(dto.getReceiveDeptId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());

        WarehouseReceiveEntity entity = new WarehouseReceiveEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setReceiveUserName(sysUserDTO.getUserName());
        entity.setReceiveDeptName(departmentDTO.getName());
        entity.setDeliveryWarehouseName(warehouseEntity.getName());
        //更新收货单主表信息
        this.updateById(entity);

        //操作日志
        WarehouseReceiveEntity byId = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(byId, entity, ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), entity.getId(), "", "");

        //更新收货单详情表信息
        return warehouseReceiveDetailService.update(dto);
    }

    /**
     * 查询详情
     *
     * @param id id
     * @return com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     **/
    @Override
    public WarehouseReceiveDTO.ViewDTO view(String id) {
        WarehouseReceiveDTO.ViewDTO viewDTO = new WarehouseReceiveDTO.ViewDTO();
        WarehouseReceiveEntity warehouseReceiveEntity = this.getById(id);
        BeanMapperUtils.copy(warehouseReceiveEntity, viewDTO);
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(warehouseReceiveEntity.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());

        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(warehouseReceiveEntity.getSupplierId());
        viewDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setIsFirstMassProduct(purchaseOrderEntity.getIsFirstMassProduct());
        viewDTO.setSupplierContactId(orderSupplierByOrderId.getSupplierContactId());
        viewDTO.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        viewDTO.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        viewDTO.setPurchaseDeptId(purchaseOrderEntity.getPurchaseDeptId());

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
        //获取收货数量
        List<WarehouseReceiveDetailEntity> detailEntitieList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(detailId);
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
        for (WarehouseReceiveDetailEntity warehouseReceiveDetailEntity : detail) {
            WarehouseReceiveDetailDTO.ViewDTO detailView = new WarehouseReceiveDetailDTO.ViewDTO();
            BeanMapperUtils.copy(warehouseReceiveDetailEntity, detailView);
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(detailView.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            Integer receive = detailEntitieList.stream().filter(obj -> obj.getSkuId().equals(warehouseReceiveDetailEntity.getSkuId()) && obj.getPurchaseOrderDetailId().equals(warehouseReceiveDetailEntity.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setUnReceiveQty(purchaseOrderDetailEntity.getPurchaseQty() - receive);

            //获取sku信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detailView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setPurchaseQty(purchaseOrderDetailEntity.getPurchaseQty());
            detailView.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());
            detailView.setProductName(productDetailEntity.getName());
            detailViewDTOS.add(detailView);
        }
        viewDTO.setWarehouseReceiveDetailList(detailViewDTOS);
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(warehouseReceiveList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != warehouseReceiveList.size()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程
        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个收货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
                .update();
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
    public Boolean addAndSubmit(WarehouseReceiveDTO.AddDTO dto) {
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
    public Boolean updateAndSubmit(WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean update = this.update(dto);
        if (update) {
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //判断是否是审核中的状态
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != warehouseReceiveList.size()) {
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

            //根据条件生成质检单
            createQcBill(ids);

        } else {
            //审核不通过
            lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(WarehouseReceiveEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个收货单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "审核操作");



        return Boolean.TRUE;
    }

    /**
     * 生成质检单
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2023-04-25 19:22
     */
    private void createQcBill(List<String> ids) {
        List<QcInfoDTO.ReceiveToQcDTO> qcList = baseMapper.getQcList(ids);
        List<String> skuIds = qcList.stream().map(QcInfoDTO.ReceiveToQcDTO::getSkuId).collect(Collectors.toList());
        List<String> purchaseOrderIds = qcList.stream().map(QcInfoDTO.ReceiveToQcDTO::getPurchaseOrderId).collect(Collectors.toList());
        //获取到sku 信息
        List<ProductVO.ProductPackVO> skuList = plmTaskFeign.getProductPackBySkuIds(skuIds);
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(purchaseOrderIds);
        for (QcInfoDTO.ReceiveToQcDTO item : qcList) {
            String skuId = item.getSkuId();
            String purchaseOrderId = item.getPurchaseOrderId();
            ProductVO.ProductPackVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(new ProductVO.ProductPackVO());
            item.setProductGrade(sku.getProductGrade());
            item.setVariantProperty(sku.getVariantProperty());
            item.setBoxHeight(sku.getBoxHeight());
            item.setBoxLength(sku.getBoxHeight());
            item.setBoxWeight(sku.getBoxWeight());
            item.setBoxWidth(sku.getBoxWidth());
            item.setProductHeight(sku.getProductHeight());
            item.setProductLength(sku.getProductLength());
            item.setProductWidth(sku.getProductWidth());
            item.setProductNetWeight(sku.getProductNetWeight());
            Boolean isFirstMassProduct = purchaseOrderList.stream().filter(p -> p.getId().equals(purchaseOrderId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getIsFirstMassProduct())).orElse(false);
            item.setIsFirstMassProduct(isFirstMassProduct);

        }
        //添加质检单的
        List<QcInfoDTO.ReceiveToQcDTO> addList = new ArrayList<>(qcList.size());
        //获取到审核通过的 且启用的质检规则
        List<QcRuleEntity> qcRuleList = qcRuleService.listByApprove();
        //新品质检
        String newProduct = QcTypeEnum.NEW_PRODUCT_STOCK_IN.getCode();
        List<String> productGradeList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(newProduct)).map(QcRuleEntity::getProductGradeKey).collect(Collectors.toList());
        String newProductGrade = String.join(",", productGradeList);

        //入库质检
        String stockIn = QcTypeEnum.STOCK_IN.getCode();
        List<String> stockInProductGradeList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(stockIn)).map(QcRuleEntity::getProductGradeKey).collect(Collectors.toList());
        String stockInProductGrade = String.join(",", stockInProductGradeList);
        //新品 并且符合等级的
        List<QcInfoDTO.ReceiveToQcDTO> newProductList = qcList.stream().filter(q -> q.getIsFirstMassProduct() && newProductGrade.contains(q.getProductGrade())).collect(Collectors.toList());
        for (QcInfoDTO.ReceiveToQcDTO newItem : newProductList) {
            QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
            BeanMapper.copy(newItem, newQc);
            newQc.setQcType(newProduct);
            addList.add(newQc);
        }
        //旧品 并且符合等级的
        List<QcInfoDTO.ReceiveToQcDTO> stockInProductList = qcList.stream().filter(q -> !q.getIsFirstMassProduct() && stockInProductGrade.contains(q.getProductGrade())).collect(Collectors.toList());
        for (QcInfoDTO.ReceiveToQcDTO stockInItem : stockInProductList) {
            QcInfoDTO.ReceiveToQcDTO stockInQc = new QcInfoDTO.ReceiveToQcDTO();
            BeanMapper.copy(stockInItem, stockInQc);
            stockInQc.setQcType(stockIn);
            addList.add(stockInQc);
        }
        qcInfoService.autoReceiveToQcDTO(addList);
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //已审核支持反审核
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
        ).count();

        if (count != warehouseReceiveList.size()) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        //TODO 待加审核流程

        //下推入库单不能反审核
        warehouseReceiveList.forEach(req -> {
            List<PurchaseStockInEntity> stockInBySourceId = purchaseStockInService.getStockInBySourceId(req.getId());
            if (CollectionUtils.isNotEmpty(stockInBySourceId)) {
                throw new ServiceException(ApiError.ERROR_99011);
            }
        });

        //修改状态为待提交
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
                .update();

        warehouseReceiveList.stream().peek(req -> {
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailByOrderId(req.getPurchaseOrderId());
            List<String> podIds = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
//            List<WarehouseReceiveDetailEntity> detailByMainId = warehouseReceiveDetailService.getDetailByMainId(req.getId());

            for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : purchaseOrderDetailEntities) {
                    /*if (!purchaseOrderDetailEntity.getArrivalStatus().equals(ArrivalStatusEnum.ARRIVED.getCode())) {

                    }*/
                Integer reduce = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
//                Integer thisReduce = detailByMainId.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);

                getArrivalState(reduce, purchaseOrderDetailEntity.getPurchaseQty(), purchaseOrderDetailEntity.getId());

            }
        });

        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个收货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "反审核操作");

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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = warehouseReceiveList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != warehouseReceiveList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("收货单【%s】取消流程", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "取消流程操作");

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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
        lambdaUpdate().set(WarehouseReceiveEntity::getInvalidStatus, Boolean.TRUE)
                .set(WarehouseReceiveEntity::getInvalidRemark, remark)
                .set(WarehouseReceiveEntity::getInvalidTime, LocalDateTime.now())
                .in(WarehouseReceiveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = warehouseReceiveList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个收货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "作废操作");
        warehouseReceiveList.forEach(req -> {
            //修改到货状态
            updateArrivalState(req);
        });
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
        List<WarehouseReceiveEntity> warehouseReceiveList = this.listByIds(ids);
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
        warehouseReceiveDetailService.delete(ids);

        boolean flag = this.removeByIds(ids);

        warehouseReceiveList.forEach(req -> {
            //修改到货状态
            updateArrivalState(req);
        });

        //删除主表
        return flag;
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
    public Boolean exportExcel(WarehouseReceiveDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<WarehouseReceiveExcelDTO> warehouseReceiveExcelDTOS = baseMapper.warehouseReceiveExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = warehouseReceiveExcelDTOS.stream().map(WarehouseReceiveExcelDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<WarehouseReceiveExportExcelDTO> exportExcelDTOS = new ArrayList<>();
        warehouseReceiveExcelDTOS.forEach(obj -> {

            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            obj.setProductName(productDetailEntity.getName());
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            WarehouseReceiveExportExcelDTO warehouseReceiveExportExcelDTO = new WarehouseReceiveExportExcelDTO();
            BeanMapperUtils.copy(obj, warehouseReceiveExportExcelDTO);
            exportExcelDTOS.add(warehouseReceiveExportExcelDTO);
        });


        String fileName = "仓库收货单";
        try {
            ExcelUtil.export(fileName, "仓库收货单", exportExcelDTOS, WarehouseReceiveExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    /**
     * 下推入库单列表查询
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.GenerateStockInViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/14 14:24
     **/
    @Override
    public List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(List<String> ids) {
        List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInViewDTOS = baseMapper.generateStockInView(ids);
        LoginUser userInfo = commonService.getUserInfo();

        //获取sku的id集合
        List<String> skuIdList = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<ProductDetailEntity> byIdList = plmTaskFeign.getByIdList(skuIdList);

        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //获取收货单详情
        List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);

        List<String> collect = detailEntityList.stream().map(WarehouseReceiveDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseStockInDetailEntity> stockInDetailEntityListBySource = purchaseStockInDetailService.listDetailBySourceDetailIds(collect);

        //退货信息
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(orderDetailIds);
        List<String> list = new ArrayList<>();
        generateStockInViewDTOS.forEach(req -> {
            boolean contains = list.contains(req.getId());
            if (contains) {
                req.setPurchaseOrderCode(null);
                req.setSupplierName(null);
                return;
            }
            ProductDetailEntity productDetailEntity = byIdList.stream().filter(obj -> req.getSkuId().equals(obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            req.setProductName(productDetailEntity.getName());
            req.setStockInDate(LocalDate.now());
            req.setStockInUserId(userInfo.getUid());
            req.setStockInUserName(userInfo.getUserName());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(req.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            Integer reduce = stockInDetailEntityListBySource.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(req.getPurchaseOrderDetailId()) && obj.getSkuId().equals(req.getSkuId())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            if (req.getReceiveQty() - reduce <= 0) {
                req.setUnStockInQty(0);
                req.setStockInQty(0);
            } else {
                req.setStockInQty(req.getReceiveQty() - reduce);
                req.setUnStockInQty(req.getReceiveQty() - reduce);
            }

            req.setReceiveQty(req.getReceiveQty());
            req.setExceedQty(req.getExceedQty());

        });
        return generateStockInViewDTOS;
    }

    /**
     * 下推入库单
     *
     * @param dtos dtos
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateStockIn(List<WarehouseReceiveDTO.GenerateStockInDTO> dtos) {
        LoginUser userInfo = commonService.getUserInfo();
        List<String> collect = dtos.stream().map(WarehouseReceiveDTO.GenerateStockInDTO::getMainId).distinct().collect(Collectors.toList());
        //获取用户信息
        SysUserDTO userDTO = sysUserFeign.getSysUserById(userInfo.getUid());
        if (ObjectUtil.isEmpty(userDTO)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        //获取用户部门
        SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(userInfo.getUid());
        for (String id : collect) {
            WarehouseReceiveEntity entity = this.getById(id);
            if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
                throw new ServiceException(ApiError.ERROR_98057);
            }
            PurchaseStockInDTO.AddDTO addDTO = new PurchaseStockInDTO.AddDTO();
            addDTO.setSourceId(id);
            addDTO.setSourceType(SourceTypeEnum.WAREHOUSE_RECEIVE.getCode());
            WarehouseReceiveEntity warehouseReceiveEntity = this.getById(id);

            addDTO.setPurchaseOrderId(warehouseReceiveEntity.getPurchaseOrderId());
            addDTO.setDeliveryWarehouseId(warehouseReceiveEntity.getDeliveryWarehouseId());
            addDTO.setStockInUserId(warehouseReceiveEntity.getReceiveUserId());
            addDTO.setStockInDeptId(deptByUserId.getDepartmentId());
            //设置明细
            List<PurchaseStockInDetailDTO.AddDTO> detailDTOList = new ArrayList<>();
            dtos.forEach(req -> {
                if (req.getMainId().equals(id)) {
                    PurchaseStockInDetailDTO.AddDTO detailDTO = new PurchaseStockInDetailDTO.AddDTO();
                    detailDTO.setStockInQty(req.getStockInQty());
                    detailDTO.setExceedQty(req.getExceedQty());
                    detailDTO.setWarehouseLocationId(req.getDeliveryWarehouseId());
                    detailDTO.setRemark(req.getRemark());
                    detailDTO.setSourceDetailId(req.getId());
                    detailDTO.setPurchaseOrderDetailId(req.getPurchaseOrderDetailId());
                    detailDTOList.add(detailDTO);
                }
            });
            addDTO.setDetails(detailDTOList);
            purchaseStockInService.add(addDTO);
        }
        return true;
    }

    /**
     * 采购订单-关联的收货单据
     *
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     **/
    @Override
    public List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(String purchaseOrderId) {
        List<WarehouseReceiveDTO.OrderRefReceiveDTO> orderRefReceiveDTOS = baseMapper.purchaseOrderRefReceive(purchaseOrderId);
        if (CollectionUtils.isEmpty(orderRefReceiveDTOS)) {
            return new ArrayList<>();
        }
        //获取采购单详情表id集合
        List<String> orderDetailIds = orderRefReceiveDTOS.stream().map(WarehouseReceiveDTO.OrderRefReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        for (WarehouseReceiveDTO.OrderRefReceiveDTO orderRefReceiveDTO : orderRefReceiveDTOS) {
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(orderRefReceiveDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            orderRefReceiveDTO.setProductName(purchaseOrderDetailEntity.getProductName());
            orderRefReceiveDTO.setApproveStatusName(ApproveStatusEnum.getName(orderRefReceiveDTO.getApproveStatus()));
            orderRefReceiveDTO.setInvalidStatusName(InvalidStatusEnum.getName(orderRefReceiveDTO.getInvalidStatus()));
        }
        return orderRefReceiveDTOS;
    }

    /**
     * 采购订单-下推收货单保存按钮
     *
     * @param dto dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 13:54
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateReceive(PurchaseOrderDTO.ListGenerateReceiveDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        //生成下推签收单
        List<PurchaseOrderDTO.GenerateReceiveDTO> list = dto.getList();
        //采购订单明细Ids
        List<String> purchaseOrderDetailIds = list.stream().map(PurchaseOrderDTO.GenerateReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(purchaseOrderDetailIds);
        ;
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        long arrivalStatusCount = purchaseOrderDetailList.stream().filter(obj -> ArrivalStatusEnum.ARRIVED.getCode().equals(obj.getArrivalStatus())).count();
        if (arrivalStatusCount > 0) {
            throw new ServiceException(ApiError.ERROR_98041);
        }

        List<String> purchaseOrderIds = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(purchaseOrderIds);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        long approveStatusCount = purchaseOrderList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (approveStatusCount > 0) {
            throw new ServiceException(ApiError.ERROR_98040);
        }
        List<String> listSign = new ArrayList<>();

        for (PurchaseOrderDTO.GenerateReceiveDTO generateReceiveDTO : list) {
            boolean contains = listSign.contains(generateReceiveDTO.getId());
            if (!contains) {
                WarehouseReceiveDTO.AddDTO addDTO = new WarehouseReceiveDTO.AddDTO();
                addDTO.setPurchaseOrderId(generateReceiveDTO.getId());
                addDTO.setPurchaseOrderCode(generateReceiveDTO.getCode());
                addDTO.setReceiveUserId(generateReceiveDTO.getReceiveUserId());
                SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(generateReceiveDTO.getReceiveUserId());
                addDTO.setReceiveDeptId(deptByUserId.getDepartmentId());
                addDTO.setBillDate(generateReceiveDTO.getBillDate());
                addDTO.setDeliveryWarehouseId(generateReceiveDTO.getDeliveryWarehouseId());

                List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = new ArrayList<>();
                for (PurchaseOrderDTO.GenerateReceiveDTO receiveDTO : list) {
                    if (generateReceiveDTO.getId().equals(receiveDTO.getId())) {
                        WarehouseReceiveDetailDTO.AddDTO detailAddDTO = new WarehouseReceiveDetailDTO.AddDTO();
                        detailAddDTO.setReceiveQty(receiveDTO.getReceiveQty());
                        detailAddDTO.setExceedQty(receiveDTO.getExceedQty());
                        detailAddDTO.setRemark(receiveDTO.getRemark());
                        detailAddDTO.setPurchaseOrderDetailId(receiveDTO.getPurchaseOrderDetailId());
                        warehouseReceiveDetailList.add(detailAddDTO);
                    }
                }
                addDTO.setWarehouseReceiveDetailList(warehouseReceiveDetailList);
                addDTO.setCreateUserId(userInfo.getUid());
                addDTO.setCreateUserName(userInfo.getUserName());
                addDTO.setUpdateUserId(userInfo.getUid());
                addDTO.setUpdateUserName(userInfo.getUserName());
                add(addDTO);
                listSign.add(generateReceiveDTO.getId());
            }
        }
        return Boolean.TRUE;
    }


    /**
     * 判断到货状态
     *
     * @param receiveQty  到货数量
     * @param purchaseQty 采购数量
     * @param id          采购明细id
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/4/20 18:47
     **/
    private Boolean getArrivalState(Integer receiveQty, Integer purchaseQty, String id) {
        String arrivalStatus = "";
        //未到货
        if (receiveQty <= MathUtil.ZERO) {
            arrivalStatus = ArrivalStatusEnum.NON_ARRIVAL.getCode();
        } else if (receiveQty > MathUtil.ZERO && receiveQty < purchaseQty) {
            //部分到货
            arrivalStatus = ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode();
        } else {
            //已到货
            arrivalStatus = ArrivalStatusEnum.ARRIVED.getCode();
        }
        PurchaseOrderDetailEntity purchaseOrderDetailEntity = new PurchaseOrderDetailEntity();
        purchaseOrderDetailEntity.setId(id);
        purchaseOrderDetailEntity.setArrivalStatus(arrivalStatus);
        purchaseOrderDetailEntity.setArrivalTime(LocalDateTime.now());
        Boolean flag = scmTaskFeign.updatePurchaseOrderDetailById(purchaseOrderDetailEntity);
        return flag;
    }

    public static void main(String[] args) {
        List<String> strList = new ArrayList<>();
        strList.add("s,a,b");

        List<String> str1List = new ArrayList<>();
        str1List.add("s,b,c,d");

        String dd = "S,A,B,C,D";
        System.out.println(dd.contains("A"));
    }
}
