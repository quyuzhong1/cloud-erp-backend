package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
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
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_RECEIVE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Luo_WG
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
    private WorkflowFeign workflowFeign;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private QcRuleService qcRuleService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryOrderFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
/*
    @Resource
    private SyncKingdeePoReceiveService syncKingdeePoReceiveService;*/

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
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
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

        //查询质检单
        List<String> receiveIds = records.stream().map(WarehouseReceiveDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //质检信息
        List<QcInfoEntity> qcInfoList = qcInfoService.listQCBySourceIdsAndType(receiveIds,SourceTypeEnum.PO_RECEIVE.getCode());
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                //设置入库状态名称
                obj.setInStockStatusName(InstockStatusEnum.getByCode(obj.getInStockStatus()));
                List<QcInfoEntity> resultList = qcInfoList.stream().filter(v -> v.getSourceId().equals(obj.getId())).collect(Collectors.toList());
                if(resultList.stream().allMatch(v->Objects.isNull(v.getQcStatus()) || QcBillStatusEnum.DRAFT.equals(v.getQcStatus())|| QcBillStatusEnum.WAIT_QC.equals(v.getQcStatus())|| QcBillStatusEnum.CANCEL.equals(v.getQcStatus()))){
                    obj.setQcStatus(PdaQclStatusEnum.WAIT_QC.getCode());
                    obj.setQcStatusName(PdaQclStatusEnum.WAIT_QC.getName());
                }else if(resultList.stream().allMatch(v->QcBillStatusEnum.EXEMPTION.equals(v.getQcStatus()) || QcBillStatusEnum.FINISH_QC.equals(v.getQcStatus()))){
                    obj.setQcStatus(PdaQclStatusEnum.FINISH_QC.getCode());
                    obj.setQcStatusName(PdaQclStatusEnum.FINISH_QC.getName());
                }else{
                    obj.setQcStatus(PdaQclStatusEnum.PARTIAL_QC.getCode());
                    obj.setQcStatusName(PdaQclStatusEnum.PARTIAL_QC.getName());
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                    obj.setProductName(productDetailEntity.getName());
                }

                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(purchaseOrderDetailEntity)) {
                    obj.setPurchaseQty(purchaseOrderDetailEntity.getPurchaseQty());
                }
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
        List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> dbList = this.baseMapper.listCount(dto);
        List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> list = new ArrayList<>();
        PageListTypeEnum[] values = PageListTypeEnum.values();
        for (PageListTypeEnum item : values) {
            String dbStatus = "";
            if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                dbStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
            }
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                dbStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
            }
            if (PageListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                dbStatus = ApproveStatusEnum.APPROVE.getStatus();
            }
            if (PageListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                dbStatus = ApproveStatusEnum.REJECT.getStatus();
            }
            String finalDbStatus = dbStatus;
            WarehouseReceiveDTO.WarehouseReceiveCountDTO dbDTO = dbList.stream().filter(v->v.getType().equals(finalDbStatus)).findFirst().orElse(null);
            WarehouseReceiveDTO.WarehouseReceiveCountDTO result =new WarehouseReceiveDTO.WarehouseReceiveCountDTO();
            result.setType(item.getCode());
            if(Objects.nonNull(dbDTO)){
                result.setCount(dbDTO.getCount());
            }else{
                result.setCount(0);
            }
            list.add(result);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public WarehouseReceiveEntity add(WarehouseReceiveDTO.AddDTO dto) {
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        //获取供应商信息
        SupplierEntity supplier = scmTaskFeign.getSupplierById(orderSupplierByOrderId.getSupplierId());
        if(!supplier.getSrmDisabled() && !dto.getGenerateByDelivery()){
            throw new ServiceException(ApiError.RECEIVE_SHOULD_GENERATE_BY_DELIVERY,supplier.getName());
        }

        //获取用户信息
        FindUserDTO userDTO = null;
        if (CharSequenceUtil.isNotBlank(dto.getReceiveUserId())) {
            userDTO = sysUserFeign.getUserByUserId(dto.getReceiveUserId());
        }
        //获取用户部门
        SysDepartmentDTO departmentDTO = null;
        if (CharSequenceUtil.isNotBlank(dto.getReceiveDeptId())) {
            departmentDTO = sysUserFeign.getUserDeptById(dto.getReceiveDeptId());
        }

        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(purchaseOrderEntity.getReceiveOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGSH, BusinessNoTypeEnum.CODE_CGSH.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGSH);
        //设置收货单主表
        WarehouseReceiveEntity warehouseReceiveEntity = new WarehouseReceiveEntity();
        warehouseReceiveEntity.setSubcontractType(purchaseOrderEntity.getSubcontractType());
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
            warehouseReceiveEntity.setReceiveDeptName(departmentDTO.getName());
        }
        warehouseReceiveEntity.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        warehouseReceiveEntity.setReceiveOrgName(sysAccountingCompanyEntity.getCompanyName());
        warehouseReceiveEntity.setBillDate(dto.getBillDate());
        warehouseReceiveEntity.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        if (ObjectUtil.isNotEmpty(warehouseEntity)) {
            warehouseReceiveEntity.setDeliveryWarehouseName(warehouseEntity.getName());
        }
        warehouseReceiveEntity.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        warehouseReceiveEntity.setPurchaseUserName(purchaseOrderEntity.getPurchaseUserName());
        warehouseReceiveEntity.setSourceId(dto.getSourceId());
        warehouseReceiveEntity.setSourceCode(dto.getSourceCode());
        warehouseReceiveEntity.setSourceType(dto.getSourceType());
        //保存主表信息
        this.save(warehouseReceiveEntity);

        //保存详情信息
        warehouseReceiveDetailService.add(dto, warehouseReceiveEntity.getId());

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个收货单【%s】", code), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), warehouseReceiveEntity.getId(), "新增操作");

        return warehouseReceiveEntity;
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
        FindUserDTO sysUserDTO = sysUserFeign.getUserByUserId(dto.getReceiveUserId());

        //获取用户部门
        SysDepartmentDTO departmentDTO = null;
        if (CharSequenceUtil.isNotBlank(dto.getReceiveDeptId())) {
            departmentDTO = sysUserFeign.getUserDeptById(dto.getReceiveDeptId());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());

        WarehouseReceiveEntity entity = new WarehouseReceiveEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setReceiveUserName(sysUserDTO.getUserName());
        if (departmentDTO != null) {
            entity.setReceiveDeptName(departmentDTO.getName());
        }
        entity.setDeliveryWarehouseName(warehouseEntity.getName());
        //更新收货单主表信息
        this.updateById(entity);

        //操作日志
        WarehouseReceiveEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, entity, ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), entity.getId(), "", "");
        //更新收货单详情表信息
        Boolean flag = warehouseReceiveDetailService.update(dto);

        return flag;
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
        viewDTO.setSupplierContactId(orderSupplierByOrderId.getSupplierContactId());
        viewDTO.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        viewDTO.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        viewDTO.setPurchaseDeptId(purchaseOrderEntity.getPurchaseDeptId());

        //查询发货单
        DeliveryOrderEntity deliveryOrderEntity = srmDeliveryOrderFeign.listByIds(Collections.singletonList(warehouseReceiveEntity.getSourceId())).stream().findFirst().orElse(new DeliveryOrderEntity());
        viewDTO.setDeliveryCode(deliveryOrderEntity.getCode());

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
        List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(detailId);
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);

        //获取发货数量
        List<DeliveryOrderDetailEntity> deliveryOrderDetailEntityList = srmDeliveryOrderFeign.listDetailByDetailSourceIds(detailId);
        //采购订单明细下所有入库数据
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailByPodIds(detailId);
        for (WarehouseReceiveDetailEntity warehouseReceiveDetailEntity : detail) {
            WarehouseReceiveDetailDTO.ViewDTO detailView = new WarehouseReceiveDetailDTO.ViewDTO();
            BeanMapperUtils.copy(warehouseReceiveDetailEntity, detailView);
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(detailView.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer receive = detailEntitieList.stream().filter(obj -> obj.getSkuId().equals(warehouseReceiveDetailEntity.getSkuId()) && obj.getPurchaseOrderDetailId().equals(warehouseReceiveDetailEntity.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setUnReceiveQty(purchaseOrderDetailEntity.getPurchaseQty() + returnQty - receive);

            Integer deliveryQty = deliveryOrderDetailEntityList.stream().filter(v->v.getSourceDetailId().equals(purchaseOrderDetailEntity.getId())).mapToInt(DeliveryOrderDetailEntity::getDeliveryQty).sum();
            detailView.setDeliveryQty(deliveryQty);

            //获取sku信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detailView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }

            if (CollectionUtils.isNotEmpty(poInstockDetailList)) {
                Integer effectiveStockInQty = poInstockDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(warehouseReceiveDetailEntity.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setEffectiveStockInQty(effectiveStockInQty);
                detailView.setUnStockInQty(purchaseOrderDetailEntity.getPurchaseQty() - effectiveStockInQty + returnQty);
            }
            detailView.setPurchaseQty(purchaseOrderDetailEntity.getPurchaseQty());
            detailView.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());
            detailView.setProductName(productDetailEntity.getName());
            detailView.setFirstMassProduct(purchaseOrderDetailEntity.getFirstMassProduct());
            detailView.setFirstMassProductName(FirstMassProductTypeEnum.getName(purchaseOrderDetailEntity.getFirstMassProduct()));
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
        operateLogService.batchAddModuleOperateLog("提交了一个收货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
                .set(WarehouseReceiveEntity::getApproveUserId, "")
                .set(WarehouseReceiveEntity::getApproveUserName, "")
                .set(WarehouseReceiveEntity::getApproveTime, null)
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
    public WarehouseReceiveEntity addAndSubmit(WarehouseReceiveDTO.AddDTO dto) {
        WarehouseReceiveEntity entity = this.add(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        entity = this.getById(entity.getId());
        this.submitEntity(entity);
        return entity;
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
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(dto.getId()));
    }

    /**
     * 批量审核
     *
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(WarehouseReceiveEntity entity, String type, String comment, Boolean isNeedProcess,List<WarehouseReceiveDetailEntity> receiveDetailList) {
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过
            lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(WarehouseReceiveEntity::getApproveUserId, userInfo.getUid())
                    .set(WarehouseReceiveEntity::getApproveUserName, userInfo.getUserName())
                    .set(WarehouseReceiveEntity::getApproveTime, LocalDateTime.now())
                    .eq(WarehouseReceiveEntity::getId, entity.getId())
                    .update();
            //根据条件生成质检单
            createQcBill(Collections.singletonList(entity.getId()));
            // 更新库存数据
            updateInventoryTransCore(Collections.singletonList(entity));
            List<String> podIds = receiveDetailList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getPurchaseOrderDetailId())).map(obj -> obj.getPurchaseOrderDetailId()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(podIds)) {
                //修改到货状态
                poReturnService.updateArrivalState(podIds);
            }
            //修改发货单确认状态
            List<String> detailIdsByDeliverySource = receiveDetailList.stream().map(WarehouseReceiveDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
            if(PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode().equals(entity.getSourceType()) && CollectionUtils.isNotEmpty(detailIdsByDeliverySource)){
                srmDeliveryOrderFeign.confirmReceiveStatus(detailIdsByDeliverySource);
            }
        } else {
            //审核不通过
            lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .set(WarehouseReceiveEntity::getApproveUserId, userInfo.getUid())
                    .set(WarehouseReceiveEntity::getApproveUserName, userInfo.getUserName())
                    .set(WarehouseReceiveEntity::getApproveTime, LocalDateTime.now())
                    .eq(WarehouseReceiveEntity::getId, entity.getId())
                    .update();
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个收货单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
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
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listByPurchaseOrderIds(purchaseOrderIds);
        String sourceType = SourceTypeEnum.PO_RECEIVE.getCode();
        for (QcInfoDTO.ReceiveToQcDTO item : qcList) {
            String skuId = item.getSkuId();
            ProductVO.ProductPackVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(new ProductVO.ProductPackVO());
            SkuVO productDetailEntity = skuNoList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(new SkuVO());
            item.setSourceType(sourceType);
            item.setProductGrade(sku.getProductGrade());
            item.setSaleMethod(productDetailEntity.getSaleMethod());
            item.setVariantProperty(sku.getVariantProperty());
            item.setBoxHeight(sku.getBoxHeight());
            item.setBoxLength(sku.getBoxLength());
            item.setBoxWeight(sku.getBoxWeight());
            item.setBoxWidth(sku.getBoxWidth());
            item.setProductHeight(sku.getProductHeight());
            item.setProductLength(sku.getProductLength());
            item.setProductWidth(sku.getProductWidth());
            item.setProductNetWeight(sku.getProductNetWeight());
            PurchaseOrderDetailEntity entity = purchaseOrderDetailEntities.stream().filter(p -> p.getId().equals(item.getPurchaseOrderDetailId())).
                    findFirst().orElse(new PurchaseOrderDetailEntity());
            item.setFirstMassProduct(entity.getFirstMassProduct());

        }
        //添加质检单的
        List<QcInfoDTO.ReceiveToQcDTO> addList = new ArrayList<>(qcList.size());
        //获取到审核通过的 且启用的质检规则
        List<QcRuleEntity> qcRuleList = qcRuleService.listByApprove();
        //新品质检
        String newProduct = QcTypeEnum.NEW_PRODUCT_STOCK_IN.getCode();
        List<String> productGradeList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(newProduct)).map(QcRuleEntity::getProductGradeKey).collect(Collectors.toList());
        String newProductGrade = String.join(",", productGradeList);

        //销售方式
        List<String> saleMethodList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(newProduct)).map(QcRuleEntity::getSaleMethod).collect(Collectors.toList());
        String newSaleMethod = String.join(",", saleMethodList);

        //入库质检
        String stockIn = QcTypeEnum.STOCK_IN.getCode();
        List<String> stockInProductGradeList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(stockIn)).map(QcRuleEntity::getProductGradeKey).collect(Collectors.toList());
        String stockInProductGrade = String.join(",", stockInProductGradeList);

        List<String> stockInSaleMethodList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(stockIn)).map(QcRuleEntity::getSaleMethod).collect(Collectors.toList());
        String stockInSaleMethod = String.join(",", stockInSaleMethodList);
        //新品 并且符合等级的
        List<QcInfoDTO.ReceiveToQcDTO> newProductList = qcList.stream().filter(q -> !FirstMassProductTypeEnum.SUBSEQUENT_BATCH.getCode().equals(q.getFirstMassProduct())).collect(Collectors.toList());
        for (QcInfoDTO.ReceiveToQcDTO newItem : newProductList) {
            //为空所有的加，等级为空用销售方式，销售方式为空用等级
            if ((CharSequenceUtil.isNotBlank(newProductGrade) && CharSequenceUtil.isNotBlank(newSaleMethod))) {
                QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                BeanMapper.copy(newItem, newQc);
                newQc.setQcType(newProduct);
                addList.add(newQc);
            } else if (CharSequenceUtil.isBlank(newProductGrade)) {
                if (CharSequenceUtil.isNotBlank(newItem.getSaleMethod())) {
                    String[] split = newItem.getSaleMethod().split(",");
                    for (String s : split) {
                        if (newSaleMethod.contains(s)) {
                            QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                            BeanMapper.copy(newItem, newQc);
                            newQc.setQcType(newProduct);
                            addList.add(newQc);
                            break;
                        }
                    }
                }
            } else if (CharSequenceUtil.isBlank(newSaleMethod)) {
                if (CharSequenceUtil.isNotBlank(newItem.getProductGrade())) {
                    String[] split = newItem.getProductGrade().split(",");
                    for (String s : split) {
                        if (newProductGrade.contains(s)) {
                            QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                            BeanMapper.copy(newItem, newQc);
                            newQc.setQcType(newProduct);
                            addList.add(newQc);
                            break;
                        }
                    }
                }
            } else {
                //包含的时候就要弄
                if (newProductGrade.contains(newItem.getProductGrade()) && newSaleMethod.contains(newItem.getSaleMethod())) {
                    QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                    BeanMapper.copy(newItem, newQc);
                    newQc.setQcType(newProduct);
                    addList.add(newQc);
                }
            }
        }
        //旧品 并且符合等级的
        List<QcInfoDTO.ReceiveToQcDTO> stockInProductList = qcList.stream().filter(q -> FirstMassProductTypeEnum.SUBSEQUENT_BATCH.getCode().equals(q.getFirstMassProduct())).collect(Collectors.toList());
        for (QcInfoDTO.ReceiveToQcDTO stockInItem : stockInProductList) {
            if ((CharSequenceUtil.isNotBlank(stockInProductGrade) && CharSequenceUtil.isNotBlank(stockInSaleMethod))) {
                QcInfoDTO.ReceiveToQcDTO stockInQc = new QcInfoDTO.ReceiveToQcDTO();
                BeanMapper.copy(stockInItem, stockInQc);
                stockInQc.setQcType(stockIn);
                addList.add(stockInQc);
            } else if (CharSequenceUtil.isBlank(stockInProductGrade)) {
                if (CharSequenceUtil.isNotBlank(stockInItem.getSaleMethod())) {
                    String[] split = stockInItem.getSaleMethod().split(",");
                    for (String s : split) {
                        if (stockInSaleMethod.contains(s)) {
                            QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                            BeanMapper.copy(stockInItem, newQc);
                            newQc.setQcType(stockIn);
                            addList.add(newQc);
                            break;
                        }
                    }
                }
            } else if (CharSequenceUtil.isBlank(stockInSaleMethod)) {
                if (CharSequenceUtil.isNotBlank(stockInItem.getProductGrade())) {
                    String[] split = stockInItem.getProductGrade().split(",");
                    for (String s : split) {
                        if (stockInProductGrade.contains(s)) {
                            QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                            BeanMapper.copy(stockInItem, newQc);
                            newQc.setQcType(stockIn);
                            addList.add(newQc);
                            break;
                        }
                    }
                }
            } else {
                //包含的时候就要弄
                if (stockInProductGrade.contains(stockInItem.getProductGrade()) && stockInSaleMethod.contains(stockInItem.getSaleMethod())) {
                    QcInfoDTO.ReceiveToQcDTO newQc = new QcInfoDTO.ReceiveToQcDTO();
                    BeanMapper.copy(stockInItem, newQc);
                    newQc.setQcType(stockIn);
                    addList.add(newQc);
                }
            }
        }
        //自动生成功能系统标识
        Boolean originalValue = UserContext.getIsUserSystem();
        UserContext.setIsUserSystem(Boolean.TRUE);
        qcInfoService.autoReceiveToQcDTO(addList);
        //恢复系统标识
        UserContext.setIsUserSystem(originalValue);
    }

    /**
     * 批量反审核
     *
     * @param entity
     * @param receiveDetailList
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(WarehouseReceiveEntity entity,List<WarehouseReceiveDetailEntity> receiveDetailList) {
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99003.msg);
        }
        //下推入库单不能反审核
        List<PoInstockEntity> stockInBySourceId = poInstockService.getStockInBySourceId(entity.getId());
        List<PoInstockEntity> collect = stockInBySourceId.stream().filter(obj -> InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99011.msg);
        }
        List<QcInfoEntity> qcList = qcInfoService.listQCBySourceId(entity.getId());
        if (CollectionUtils.isNotEmpty(qcList)) {
            String codes = qcList.stream().map(QcInfoEntity::getCode).collect(Collectors.joining(","));
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),CharSequenceUtil.format(ApiError.ERROR_99042.msg, codes));
        }
        //修改状态为待提交
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .set(WarehouseReceiveEntity::getApproveUserId, "")
                .set(WarehouseReceiveEntity::getApproveUserName, "")
                .set(WarehouseReceiveEntity::getApproveTime, null)
                .eq(WarehouseReceiveEntity::getId, entity.getId())
                .update();

        List<String> podIds = receiveDetailList.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(podIds)) {
            //修改到货状态
            poReturnService.updateArrivalState(podIds);
        }

        // 更新库存数据，回扣库存
        InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
        inventoryUnApproveDTO.setSourceType(InventorySourceTypeEnum.WAREHOUSE_RECEIVE);
        inventoryUnApproveDTO.setBillId(entity.getId());
        inventoryTransCoreService.unApprove(inventoryUnApproveDTO);
        //修改发货单确认状态
        List<String> detailIdsByDeliverySource = receiveDetailList.stream().map(WarehouseReceiveDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if(PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode().equals(entity.getSourceType()) && CollectionUtils.isNotEmpty(detailIdsByDeliverySource)){
            srmDeliveryOrderFeign.unConfirmReceiveStatus(detailIdsByDeliverySource);
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个收货单【%s】",entity.getCode()), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
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
        operateLogService.batchAddModuleOperateLog("收货单【%s】取消流程", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "取消流程操作");

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
        operateLogService.batchAddModuleOperateLog("作废了一个收货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "作废操作");
        List<String> purchaseOrderIds = warehouseReceiveList.stream().map(req -> req.getPurchaseOrderId()).distinct().collect(Collectors.toList());
        //作废发送金蝶
//        warehouseReceiveList.forEach(obj -> syncKingdeePoReceiveService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_INVALID.getCode()));
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(ids);
        //如果是收货单下推的，删除时去掉收货单的收获状态和收货数量
        List<String> idByDeliverySource = warehouseReceiveList.stream().filter(v-> PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode().equals(v.getSourceType())).map(WarehouseReceiveEntity::getId).distinct().collect(Collectors.toList());
        List<String> detailIdsByDeliverySource = receiveDetailList.stream().filter(v->idByDeliverySource.contains(v.getMainId())).map(WarehouseReceiveDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(detailIdsByDeliverySource)){
            srmDeliveryOrderFeign.cancelReceive(detailIdsByDeliverySource);
        }
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

    @Transactional(rollbackFor = Exception.class)
    @Override
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
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(ids);

        //删除详情表
        warehouseReceiveDetailService.delete(ids);

        boolean flag = this.removeByIds(ids);

        List<String> purchaseOrderIds = warehouseReceiveList.stream().map(req -> req.getPurchaseOrderId()).distinct().collect(Collectors.toList());

        //审核通过发送金蝶
//        warehouseReceiveList.forEach(obj -> syncKingdeePoReceiveService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));

        //如果是收货单下推的，删除时去掉收货单的收获状态和收货数量
        List<String> idByDeliverySource = warehouseReceiveList.stream().filter(v-> PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode().equals(v.getSourceType())).map(WarehouseReceiveEntity::getId).distinct().collect(Collectors.toList());
        List<String> detailIdsByDeliverySource = receiveDetailList.stream().filter(v->idByDeliverySource.contains(v.getMainId())).map(WarehouseReceiveDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(detailIdsByDeliverySource)){
            srmDeliveryOrderFeign.cancelReceive(detailIdsByDeliverySource);
        }

        //删除主表
        return flag;
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
    public Boolean exportExcel(WarehouseReceiveDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("仓库收货单", EXPORT_WMS_WAREHOUSE_RECEIVE.getCode(), dto);
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
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //仓位信息
        List<String> warehouseIds = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getDeliveryWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIds(warehouseIds);
        //获取sku的id集合
        List<String> skuIdList = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<ProductDetailEntity> byIdList = plmTaskFeign.getByIdList(skuIdList);

        //采购明细
        List<String> podIds = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);

        List<PoInstockDetailEntity> stockInDetailEntityListBySource = poInstockDetailService.listDetailBySourceDetailIds(ids);

        List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listBySourceDetailIds(ids);
        List<String> poInIds = generateStockInViewDTOS.stream().map(WarehouseReceiveDTO.GenerateStockInViewDTO::getId).collect(Collectors.toList());
        //入库数据
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailBySourceDetailIds(poInIds);
        List<String> list = new ArrayList<>();
        generateStockInViewDTOS.forEach(req -> {
            boolean contains = list.contains(req.getId());
            if (contains) {
                req.setPurchaseOrderCode(null);
                req.setSupplierName(null);
                return;
            }
            PurchaseOrderDetailEntity entity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(req.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            String warehouseLocation = Optional.ofNullable(entity.getWarehouseLocation()).orElse("");
            req.setWarehouseLocation(warehouseLocation);
            //仓位信息填充
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getCode().equals(warehouseLocation)
                    && e.getWarehouseId().equals(req.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseLocationEntity());
            req.setWarehouseLocationName(warehouseLocationEntity.getName());
            ProductDetailEntity productDetailEntity = byIdList.stream().filter(obj -> req.getSkuId().equals(obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            req.setProductName(productDetailEntity.getName());
            req.setStockInDate(LocalDate.now());
            req.setStockInUserId(userInfo.getUid());
            req.setStockInUserName(userInfo.getUserName());
            req.setFirstMassProduct(entity.getFirstMassProduct());
            req.setFirstMassProductName(FirstMassProductTypeEnum.getName(entity.getFirstMassProduct()));

            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(req.getPurchaseOrderDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer reduce = stockInDetailEntityListBySource.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(req.getPurchaseOrderDetailId()) && obj.getSkuId().equals(req.getSkuId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            if (req.getReceiveQty() - reduce <= 0) {
                req.setUnStockInQty(0);
                req.setStockInQty(0);
            } else {
                req.setStockInQty(req.getReceiveQty() - (reduce - returnQty));
                Integer effectiveStockInQty = poInstockDetailList.stream().filter(e -> e.getSourceDetailId().equals(req.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                req.setUnStockInQty(req.getReceiveQty() - effectiveStockInQty + returnQty);
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
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<String> collect = dtos.stream().map(WarehouseReceiveDTO.GenerateStockInDTO::getMainId).distinct().collect(Collectors.toList());
        //获取用户信息
        FindUserDTO userDTO = sysUserFeign.getUserByUserId(userInfo.getUid());
        if (ObjectUtil.isEmpty(userDTO)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        //获取用户部门
        SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(userInfo.getUid());
        for (String id : collect) {

            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listDetailByMainIds(Collections.singletonList(id));

            WarehouseReceiveEntity entity = this.getById(id);
            if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
                throw new ServiceException(ApiError.ERROR_98057);
            }
            PoInstockDTO.AddDTO addDTO = new PoInstockDTO.AddDTO();
            addDTO.setSourceId(id);
            addDTO.setSourceCode(entity.getSourceCode());
            addDTO.setSourceType(SourceTypeEnum.PO_RECEIVE.getCode());
            WarehouseReceiveEntity warehouseReceiveEntity = this.getById(id);

            addDTO.setPurchaseOrderId(warehouseReceiveEntity.getPurchaseOrderId());
            addDTO.setDeliveryWarehouseId(warehouseReceiveEntity.getDeliveryWarehouseId());
            addDTO.setStockInUserId(warehouseReceiveEntity.getReceiveUserId());
            addDTO.setStockInDeptId(deptByUserId.getDepartmentId());

            List<String> detailList = dtos.stream().filter(req -> req.getMainId().equals(id)).map(WarehouseReceiveDTO.GenerateStockInDTO::getId).collect(Collectors.toList());
            List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailBySourceDetailIds(detailList);
            //设置明细
            List<PoInstockDetailDTO.AddDTO> detailDTOList = new ArrayList<>();
            dtos.forEach(req -> {
                if (req.getMainId().equals(id)) {
                    Integer stockInQty = stockInDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(req.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                    WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = detailEntityList.stream().filter(obj -> obj.getId().equals(req.getId())).findFirst().orElse(null);

                    if (stockInQty + req.getStockInQty() > warehouseReceiveDetailEntity.getReceiveQty()) {
                        throw new ServiceException(ApiError.ERROR_99041.code, String.format(ApiError.ERROR_99041.msg, warehouseReceiveDetailEntity.getSkuNo()));
                    }
                    addDTO.setStockInDate(req.getStockInDate());
                    PoInstockDetailDTO.AddDTO detailDTO = new PoInstockDetailDTO.AddDTO();
                    detailDTO.setStockInQty(req.getStockInQty());
                    detailDTO.setExceedQty(req.getExceedQty());
                    detailDTO.setRemark(req.getRemark());
                    detailDTO.setWarehouseLocation(req.getWarehouseLocation());
                    detailDTO.setSourceDetailId(req.getId());
                    detailDTO.setPurchaseOrderDetailId(req.getPurchaseOrderDetailId());
                    detailDTO.setFirstMassProduct(req.getFirstMassProduct());
                    detailDTOList.add(detailDTO);
                }
            });
            addDTO.setDetails(detailDTOList);

            poInstockService.add(addDTO, Boolean.FALSE);
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
     * 根据采购订单ids 获取收获数据
     *
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseReceiveEntity>
     * @author yl
     * @date 2023-04-27 18:31
     */
    @Override
    public List<WarehouseReceiveEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        if (CollectionUtils.isEmpty(purchaseOrderIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(WarehouseReceiveEntity::getPurchaseOrderId, purchaseOrderIds).list();
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
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //生成下推签收单
        List<PurchaseOrderDTO.GenerateReceiveDTO> list = dto.getList();
        //采购订单明细Ids
        List<String> purchaseOrderDetailIds = list.stream().map(PurchaseOrderDTO.GenerateReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(purchaseOrderDetailIds);

        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        //已确认和送货中允许下推收货单
        long executionStatusCount = purchaseOrderDetailList.stream().filter(obj -> !ExecutionStatusEnum.CONFIRM.getCode().equals(obj.getExecutionStatus())
                && !ExecutionStatusEnum.DELIVERY.getCode().equals(obj.getExecutionStatus())).count();
        if (executionStatusCount > 0) {
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
            //采购订单
            PurchaseOrderEntity entity = purchaseOrderList.stream().filter(obj -> obj.getId().equals(generateReceiveDTO.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98025);
            }
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
                        //订单明细数据校验
                        String skuNos = purchaseOrderDetailList.stream().filter(obj -> !CharSequenceUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                                        && !CharSequenceUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                                        && !CharSequenceUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus())
                                )
                                .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                        if (CharSequenceUtil.isNotBlank(skuNos)) {
                            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,entity.getCode(),skuNos);
                        }

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

    @Override
    public List<WarehouseReceiveDTO.SupplierReceiveInfoDTO> getReceiveInfoBySupplierIds(WarehouseReceiveDTO.SupplierReceiveParamDTO dto) {
        return this.baseMapper.getReceiveInfoBySupplierIds(dto.getSupplierIds(), dto.getDateList());
    }

    /**
     * 更新库存数据
     *
     * @param list
     */
    public void updateInventoryTransCore(List<WarehouseReceiveEntity> list) {
        List<String> ids = list.stream().map(WarehouseReceiveEntity::getId).distinct().collect(Collectors.toList());
        Map<String, WarehouseReceiveEntity> receiveMap = list.stream().collect(Collectors.toMap(WarehouseReceiveEntity::getId, Function.identity()));
        List<WarehouseReceiveDetailEntity> details = warehouseReceiveDetailService.listDetailByMainIds(ids);
        if (CollUtil.isEmpty(details)) {
            throw new ServiceException("未找到收货单明细数据");
        }
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_RECEIVE.getCode());
        List<InOutStockDTO> members = Lists.newArrayList();
        for (WarehouseReceiveDetailEntity detail : details) {
            WarehouseReceiveEntity warehouseReceiveEntity = receiveMap.get(detail.getMainId());
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.WAREHOUSE_RECEIVE);
            inOutStockDTO.setSourceId(warehouseReceiveEntity.getId());
            inOutStockDTO.setSourceCode(warehouseReceiveEntity.getCode());
            inOutStockDTO.setSourceDetailId(detail.getId());
            inOutStockDTO.setBillDate(warehouseReceiveEntity.getBillDate());
            inOutStockDTO.setSkuId(detail.getSkuId());
            inOutStockDTO.setSkuNo(detail.getSkuNo());
            inOutStockDTO.setQty(detail.getReceiveQty());
            inOutStockDTO.setWarehouseId(warehouseReceiveEntity.getDeliveryWarehouseId());
            inOutStockDTO.setWarehouseLocation("");
            members.add(inOutStockDTO);
        }
        inventoryInOutStockDTO.setParamList(members);
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    @Override
    public PagingVO<WarehouseReceiveDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<WarehouseReceiveDTO.PdaPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        WarehouseReceiveDTO.PdaPagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDate(dateList);
        }
        IPage<WarehouseReceiveDTO.PdaPagingViewDTO> pageData = this.baseMapper.pdaPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<WarehouseReceiveDTO.PdaPagingViewDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listDetailByMainIds(ids);
        for (WarehouseReceiveDTO.PdaPagingViewDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<WarehouseReceiveDetailEntity> detailEntities = detailEntityList.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<WarehouseReceiveDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, WarehouseReceiveDTO.PdaItemDTO.class);
            List<String> skuList = itemDTOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
            record.setDetailCount(skuList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<WarehouseReceiveDTO.PdaPoReceiveCountDTO> pdaListCount(PermissionsDTO dto) {

        List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> dbList = this.baseMapper.listCount(dto);
        List<WarehouseReceiveDTO.PdaPoReceiveCountDTO> list = new ArrayList<>();
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        for (PdaTabFlagEnum item : values) {
            List<String> dbStatusList = new ArrayList<>();
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                dbStatusList = Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus());
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                dbStatusList = Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus());
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                dbStatusList =Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus());
            }
            List<String> finalDbStatusList = dbStatusList;
            List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> dbDTO = dbList.stream().filter(v-> finalDbStatusList.contains(v.getType())).collect(Collectors.toList());
            WarehouseReceiveDTO.PdaPoReceiveCountDTO result =new WarehouseReceiveDTO.PdaPoReceiveCountDTO();
            result.setTabFlag(item.getCode());
            if(CollectionUtils.isNotEmpty(dbDTO)){
                result.setCount(dbDTO.stream().mapToInt(WarehouseReceiveDTO.WarehouseReceiveCountDTO::getCount).sum());
            }else{
                result.setCount(0);
            }
            list.add(result);
        }

        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pdaAdd(WarehouseReceiveDTO.AddDTO dto) {
        List<WarehouseReceiveDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        List<String> poReceiveDetailIds = warehouseReceiveDetailList.stream().map(WarehouseReceiveDetailDTO.AddDTO::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.listPurchaseOrderDetailById(poReceiveDetailIds);
        List<PurchaseOrderDetailEntity> detailEntityListByPoId = scmTaskFeign.listByPurchaseOrderIds(Collections.singletonList(dto.getPurchaseOrderId()));
        List<DeliveryOrderDetailEntity> allDeliveryOrderDetailList = srmDeliveryOrderFeign.listDetailByDetailSourceIds(poReceiveDetailIds);

        for (WarehouseReceiveDetailDTO.AddDTO addDTO : warehouseReceiveDetailList) {
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailEntityList.stream().filter(req -> req.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_DETAIL_SKU_NOT_EXIST, addDTO.getSkuNo());
            }
            List<PurchaseOrderDetailEntity> detailEntityList = detailEntityListByPoId.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            List<DeliveryOrderDetailEntity> deliveryOrderDetailist = allDeliveryOrderDetailList.stream().filter(req -> req.getSourceDetailId().equals(detailEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(deliveryOrderDetailist)){
                int deliveryQty = deliveryOrderDetailist.stream().mapToInt(DeliveryOrderDetailEntity::getDeliveryQty).sum();
                if(addDTO.getReceiveQty() > deliveryQty){
                    throw new ServiceException(ApiError.RECEIVE_QTY_ERROR);
                }
            }

            //校验sku是否有重复，重复需要拆单
            if (detailEntityList.size() > MathUtil.ONE) {
                List<String> podIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
                List<WarehouseReceiveDetailEntity> receiveDetailEntities = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
                List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
                Integer receiveQty = addDTO.getReceiveQty();
                for (PurchaseOrderDetailEntity entity : detailEntityList) {
                    //采购数量
                    Integer purchaseQty = detailEntityList.stream().map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
                    //已签收数量
                    Integer alreadyReceiveQty = receiveDetailEntities.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(entity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    //退货补货数量
                    Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(entity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (receiveQty > purchaseQty + returnQty - alreadyReceiveQty) {
                        throw new ServiceException(ApiError.ERROR_99054.code, String.format(ApiError.ERROR_99054.msg, entity.getSkuNo()));
                    }
                    if (alreadyReceiveQty >= entity.getPurchaseQty() + returnQty) {
                        continue;
                    }
                    WarehouseReceiveDetailDTO.AddDTO addSkuDTO = new WarehouseReceiveDetailDTO.AddDTO();
                    addSkuDTO.setPurchaseOrderDetailId(entity.getId());
                    addSkuDTO.setExceedQty(addDTO.getExceedQty());
                    addSkuDTO.setRemark(addDTO.getRemark());
                    addSkuDTO.setSkuNo(addDTO.getSkuNo());
                    if (receiveQty > (entity.getPurchaseQty() - alreadyReceiveQty) && !detailEntityList.get(detailEntityList.size()-1).getId().equals(entity.getId())) {
                        receiveQty = receiveQty - (entity.getPurchaseQty() - alreadyReceiveQty);
                        addSkuDTO.setReceiveQty(entity.getPurchaseQty() - alreadyReceiveQty);
                        addDTOList.add(addSkuDTO);
                    } else {
                        addSkuDTO.setReceiveQty(receiveQty);
                        addDTOList.add(addSkuDTO);
                        break;
                    }
                    addDTO.setExceedQty(0);
                }
            } else {
                addDTOList.add(addDTO);
            }
        }
        dto.setWarehouseReceiveDetailList(addDTOList);
        return this.add(dto).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaUpdate(WarehouseReceiveDTO.UpdateDTO dto) {
        List<WarehouseReceiveDetailDTO.UpdateDTO> addDTOList = new ArrayList<>();
        List<WarehouseReceiveDetailDTO.UpdateDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        List<String> poReceiveDetailIds = warehouseReceiveDetailList.stream().map(WarehouseReceiveDetailDTO.UpdateDTO::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.listPurchaseOrderDetailById(poReceiveDetailIds);
        List<PurchaseOrderDetailEntity> detailEntityListByPoId = scmTaskFeign.listByPurchaseOrderIds(Collections.singletonList(dto.getPurchaseOrderId()));
        for (WarehouseReceiveDetailDTO.UpdateDTO updateDTO : warehouseReceiveDetailList) {
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailEntityList.stream().filter(req -> req.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_DETAIL_SKU_NOT_EXIST, updateDTO.getSkuNo());
            }
            List<PurchaseOrderDetailEntity> detailEntityList = detailEntityListByPoId.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            //校验sku是否有重复，重复需要拆单
            if (detailEntityList.size() > MathUtil.ONE) {
                List<String> podIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
                List<WarehouseReceiveDetailEntity> receiveDetailEntities = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
                List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
                Integer receiveQty = updateDTO.getReceiveQty();
                for (PurchaseOrderDetailEntity entity : detailEntityList) {
                    //采购数量
                    Integer purchaseQty = detailEntityList.stream().map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
                    //已签收数量
                    Integer alreadyReceiveQty = receiveDetailEntities.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(entity.getId()) && !obj.getMainId().equals(dto.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    //退货补货数量
                    Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(entity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (receiveQty > purchaseQty + returnQty - alreadyReceiveQty) {
                        throw new ServiceException(ApiError.ERROR_99054.code, String.format(ApiError.ERROR_99054.msg, entity.getSkuNo()));
                    }
                    if (alreadyReceiveQty >= entity.getPurchaseQty() + returnQty) {
                        continue;
                    }

                    WarehouseReceiveDetailDTO.UpdateDTO updateSkuDTO = new WarehouseReceiveDetailDTO.UpdateDTO();
                    updateSkuDTO.setPurchaseOrderDetailId(entity.getId());
                    updateSkuDTO.setMainId(updateDTO.getMainId());
                    updateSkuDTO.setExceedQty(updateDTO.getExceedQty());
                    updateSkuDTO.setRemark(updateDTO.getRemark());
                    updateSkuDTO.setSkuNo(updateDTO.getSkuNo());
                    if (receiveQty > (entity.getPurchaseQty() - alreadyReceiveQty) && !detailEntityList.get(detailEntityList.size()-1).getId().equals(entity.getId())) {
                        receiveQty = receiveQty - (entity.getPurchaseQty() - alreadyReceiveQty);
                        updateSkuDTO.setReceiveQty(entity.getPurchaseQty() - alreadyReceiveQty);
                        addDTOList.add(updateSkuDTO);
                    } else {
                        updateSkuDTO.setReceiveQty(receiveQty);
                        addDTOList.add(updateSkuDTO);
                        break;
                    }
                    updateDTO.setExceedQty(0);
                }

            } else {
                addDTOList.add(updateDTO);
            }
        }
        dto.setWarehouseReceiveDetailList(addDTOList);
        return this.update(dto);
    }

    @Override
    public WarehouseReceiveDTO.ViewDTO pdaView(String id) {
        WarehouseReceiveDTO.ViewDTO viewDTO = new WarehouseReceiveDTO.ViewDTO();
        WarehouseReceiveEntity warehouseReceiveEntity = this.getById(id);
        if (ObjectUtils.isEmpty(warehouseReceiveEntity)) {
            throw new ServiceException(ApiError.ERROR_99009);
        }
        BeanMapperUtils.copy(warehouseReceiveEntity, viewDTO);
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(warehouseReceiveEntity.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        List<PurchaseOrderDetailEntity> detailEntityList = scmTaskFeign.listByPurchaseOrderIds(Collections.singletonList(purchaseOrderEntity.getId()));
        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(warehouseReceiveEntity.getSupplierId());
        viewDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setSupplierContactId(orderSupplierByOrderId.getSupplierContactId());
        //查询发货单
        DeliveryOrderEntity deliveryOrderEntity = srmDeliveryOrderFeign.listByIds(Collections.singletonList(warehouseReceiveEntity.getSourceId())).stream().findFirst().orElse(new DeliveryOrderEntity());
        viewDTO.setDeliveryCode(deliveryOrderEntity.getCode());

        SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(orderSupplierByOrderId.getSupplierContactId());
        if (ObjectUtils.isNotEmpty(supplierContactById)) {
            viewDTO.setSupplierContactName(supplierContactById.getPerson());
        }
        viewDTO.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        viewDTO.setPurchaseUserName(purchaseOrderEntity.getPurchaseUserName());
        viewDTO.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        viewDTO.setReceiveOrgName(purchaseOrderEntity.getReceiveOrgName());
        viewDTO.setPurchaseDeptId(purchaseOrderEntity.getPurchaseDeptId());
        viewDTO.setPurchaseDeptName(purchaseOrderEntity.getPurchaseDeptName());

        //创库保存详情表的集合
        List<WarehouseReceiveDetailDTO.ViewDTO> detailViewDTOS = new ArrayList<>();
        //根据收货单主表id获取详情信息
        List<WarehouseReceiveDetailEntity> detail = warehouseReceiveDetailService.getDetailByMainId(id);
        //获取sku的id集合
        List<String> skuNoList = detail.stream().map(WarehouseReceiveDetailEntity::getSkuNo).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        //获取采购单详情的id集合
        List<String> detailId = detail.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //获取收货单详情的id集合
        List<String> receiveIds = detail.stream().map(WarehouseReceiveDetailEntity::getId).collect(Collectors.toList());
        //获取收货数量
        List<WarehouseReceiveDetailEntity> detailEntitieList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(detailId);
        List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(detailId);
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(detailId);
        //采购订单明细下所有入库数据
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailBySourceDetailIds(receiveIds);
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(warehouseReceiveEntity.getDeliveryWarehouseId()));

        //获取发货数量
        List<DeliveryOrderDetailEntity> deliveryOrderDetailEntityList = srmDeliveryOrderFeign.listDetailByDetailSourceIds(detailId);

        for (WarehouseReceiveDetailEntity warehouseReceiveDetailEntity : detail) {
            WarehouseReceiveDetailDTO.ViewDTO detailView = new WarehouseReceiveDetailDTO.ViewDTO();
            BeanMapperUtils.copy(warehouseReceiveDetailEntity, detailView);
            //获取采购单详情
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(entityClass -> entityClass.getId().equals(detailView.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer receive = detailEntitieList.stream().filter(obj -> obj.getSkuId().equals(warehouseReceiveDetailEntity.getSkuId()) && obj.getPurchaseOrderDetailId().equals(warehouseReceiveDetailEntity.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setUnReceiveQty(purchaseOrderDetailEntity.getPurchaseQty() + returnQty - receive);

            Integer deliveryQty = deliveryOrderDetailEntityList.stream().filter(v->v.getSourceDetailId().equals(purchaseOrderDetailEntity.getId())).mapToInt(DeliveryOrderDetailEntity::getDeliveryQty).sum();
            detailView.setDeliveryQty(deliveryQty);
            //获取sku信息
            SkuVO skuVO = skuList.stream().filter(entityClass -> entityClass.getSkuId().equals(detailView.getSkuId())).findFirst().orElse(new SkuVO());
            detailView.setVariantProperty(skuVO.getVariantProperty());
            detailView.setPurchaseQty(purchaseOrderDetailEntity.getPurchaseQty());
            detailView.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());
            detailView.setProductName(skuVO.getSkuName());
            detailView.setWarehouseLocation(purchaseOrderDetailEntity.getWarehouseLocation());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(purchaseOrderDetailEntity.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            detailView.setWarehouseLocationName(warehouseLocationEntity.getName());
            Integer effectiveStockInQty = poInstockDetailList.stream().filter(e -> e.getSourceDetailId().equals(warehouseReceiveDetailEntity.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setEffectiveStockInQty(effectiveStockInQty);
            detailView.setFirstMassProduct(purchaseOrderDetailEntity.getFirstMassProduct());
            detailView.setFirstMassProductName(FirstMassProductTypeEnum.getName(purchaseOrderDetailEntity.getFirstMassProduct()));
            detailView.setUnStockInQty(detailView.getReceiveQty() - effectiveStockInQty + returnQty);

            detailViewDTOS.add(detailView);
        }

        Map<String, WarehouseReceiveDetailDTO.ViewDTO> collect = detailViewDTOS.stream().collect(Collectors.groupingBy(n -> n.getSkuNo(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            Integer purchaseQty = detailEntityList.stream().filter(obj -> obj.getSkuNo().equals(m.get(0).getSkuNo())).map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);

//            int purchaseQty = m.stream().mapToInt(WarehouseReceiveDetailDTO.ViewDTO::getPurchaseQty).sum();
            int receiveQty = m.stream().mapToInt(WarehouseReceiveDetailDTO.ViewDTO::getReceiveQty).sum();
            int unReceiveQty = m.stream().mapToInt(WarehouseReceiveDetailDTO.ViewDTO::getUnReceiveQty).sum();
            int exceedQty = m.stream().mapToInt(WarehouseReceiveDetailDTO.ViewDTO::getExceedQty).sum();
            int unStockInQty = m.stream().mapToInt(WarehouseReceiveDetailDTO.ViewDTO::getUnStockInQty).sum();
            String podId = m.stream().max(Comparator.comparing(WarehouseReceiveDetailDTO.ViewDTO::getId)).map(WarehouseReceiveDetailDTO.ViewDTO::getId).get();
            WarehouseReceiveDetailDTO.ViewDTO updateDTO = new WarehouseReceiveDetailDTO.ViewDTO();
            BeanMapper.copy(m.get(MathUtil.ZERO), updateDTO);
            updateDTO.setId(podId);
            updateDTO.setPurchaseQty(purchaseQty);
            updateDTO.setReceiveQty(receiveQty);
            updateDTO.setUnReceiveQty(unReceiveQty);
            updateDTO.setExceedQty(exceedQty);
            updateDTO.setUnStockInQty(unStockInQty);
            return updateDTO;
        })));

        List<WarehouseReceiveDetailDTO.ViewDTO> viewDTOS = new ArrayList<>();
        for (Map.Entry<String, WarehouseReceiveDetailDTO.ViewDTO> stringUpdateDTOEntry : collect.entrySet()) {
            viewDTOS.add(stringUpdateDTOEntry.getValue());
        }
        viewDTO.setWarehouseReceiveDetailList(viewDTOS);
        return viewDTO;
    }

    @Override
    public List<WarehouseReceiveDTO.PdaPoReceive> pdaList(WarehouseReceiveDTO.PdaPoReceiveParam dto) {
        List<WarehouseReceiveDTO.PdaPoReceive> list = baseMapper.pdaList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> porIds = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailEntitieList = warehouseReceiveDetailService.listDetailByMainIds(porIds);
        List<String> pordIds = receiveDetailEntitieList.stream().map(req -> req.getId()).collect(Collectors.toList());

        List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailBySourceDetailIds(pordIds);

        //获取未全部入库的采购收货详情id
        List<String> receiveDetailIds = new ArrayList<>();
        Map<String, List<PoInstockDetailEntity>> collect1 = poInstockDetailEntities.stream().collect(Collectors.groupingBy(PoInstockDetailEntity::getPurchaseOrderDetailId, Collectors.collectingAndThen(Collectors.toList(), m -> {
            int stockInQty = m.stream().mapToInt(PoInstockDetailEntity::getStockInQty).sum();
            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = receiveDetailEntitieList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(m.get(MathUtil.ZERO).getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (Objects.nonNull(warehouseReceiveDetailEntity)) {
                if (stockInQty < warehouseReceiveDetailEntity.getReceiveQty()) {
                    receiveDetailIds.add(m.get(MathUtil.ZERO).getSourceDetailId());
                }
            }
            return m;
        })));


        //根据未入库采购收货单详情id获取未入库收货单id
        List<String> collect = poInstockDetailEntities.stream().map(PoInstockDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<String> ids = pordIds.stream().filter(poid -> !collect.contains(poid)).collect(Collectors.toList());
        receiveDetailIds.addAll(ids);

        if (CollectionUtils.isEmpty(receiveDetailIds)) {
            return new ArrayList<>();
        }

        List<WarehouseReceiveDetailEntity> receiveDetailEntities = warehouseReceiveDetailService.listByIds(receiveDetailIds);
        List<String> notAllReceivePoReceiveId = receiveDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        //获取到未入库采购收货单返回数据
        List<WarehouseReceiveDTO.PdaPoReceive> poReceiveList = list.stream().filter(req -> notAllReceivePoReceiveId.contains(req.getId())).collect(Collectors.toList());
        poReceiveList.sort(Comparator.comparing(WarehouseReceiveDTO.PdaPoReceive::getCode).reversed());
        list.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return poReceiveList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaAddAndSubmit(WarehouseReceiveDTO.AddDTO dto) {
        String id = this.pdaAdd(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaUpdateAndSubmit(WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean update = this.pdaUpdate(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(dto.getId()));
    }

    @Override
    public PagingVO<List<WarehouseReceiveDTO.WaitInStockPaging>> waitInStockPaging(PagingDTO<WarehouseReceiveDTO.WaitInStockPagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        WarehouseReceiveDTO.WaitInStockPagingParam params = pagingParamDTO.getParams();

        IPage<WarehouseReceiveDTO.WaitInStockPaging> pageData = this.baseMapper.pdaWaitInStockPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<WarehouseReceiveDTO.WaitInStockPaging> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listDetailByMainIds(ids);
        List<String> receiveIds = records.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        //质检信息
        List<QcInfoEntity> qcInfoList = qcInfoService.listQCBySourceIds(receiveIds);
        qcInfoList = qcInfoList.stream().filter(req -> CharSequenceUtil.isNotBlank(req.getSourceDetailId())).collect(Collectors.toList());
        for (WarehouseReceiveDTO.WaitInStockPaging record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
             List<WarehouseReceiveDetailEntity> detailEntities = detailEntityList.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<WarehouseReceiveDTO.PdaWaitInStockItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, WarehouseReceiveDTO.PdaWaitInStockItemDTO.class);
            List<String> skuList = itemDTOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
            record.setDetailCount(skuList.size());
            List<QcInfoEntity> resultList = qcInfoList.stream().filter(obj -> obj.getSourceId().equals(record.getId())).collect(Collectors.toList());
            List<QcInfoEntity> qcFinishList = resultList.stream().filter(obj ->(QcBillStatusEnum.EXEMPTION.equals(obj.getQcStatus()) || QcBillStatusEnum.FINISH_QC.equals(obj.getQcStatus()))).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(qcFinishList) || CollectionUtils.isEmpty(resultList)) {
                record.setQcStatus(PdaQclStatusEnum.WAIT_QC.getCode());
                record.setQcStatusName(PdaQclStatusEnum.WAIT_QC.getName());
            } else if (resultList.size() > qcFinishList.size()) {
                record.setQcStatus(PdaQclStatusEnum.PARTIAL_QC.getCode());
                record.setQcStatusName(PdaQclStatusEnum.PARTIAL_QC.getName());
            } else if (qcFinishList.size() >= detailEntities.size()) {
                record.setQcStatus(PdaQclStatusEnum.FINISH_QC.getCode());
                record.setQcStatusName(PdaQclStatusEnum.FINISH_QC.getName());
            } else {
                record.setQcStatus(PdaQclStatusEnum.PARTIAL_QC.getCode());
                record.setQcStatusName(PdaQclStatusEnum.PARTIAL_QC.getName());

            }
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<WarehouseReceiveDTO.WaitInStockCountDTO> waitInStockListCount(PermissionsDTO dto) {
        WaitInStockFlagEnum[] values = WaitInStockFlagEnum.values();
        List<WarehouseReceiveDTO.WaitInStockCountDTO> list = new ArrayList<>();
        for (WaitInStockFlagEnum item : values) {
            WarehouseReceiveDTO.PagingParamDTO pagingParamDTO = new WarehouseReceiveDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            WarehouseReceiveDTO.WaitInStockCountDTO resultDTO = new WarehouseReceiveDTO.WaitInStockCountDTO();
            Integer count = MathUtil.ZERO;
            if (WaitInStockFlagEnum.WAIT_INSTOCK_QC.getCode().equals(item.getCode())) {
                pagingParamDTO.setTabFlag(WaitInStockFlagEnum.WAIT_INSTOCK_QC.getCode());
                count = this.baseMapper.waitInStockListCount(pagingParamDTO);
            }
            if (WaitInStockFlagEnum.WAIT_INSTOCK_NOT_QC.getCode().equals(item.getCode())) {
                pagingParamDTO.setTabFlag(WaitInStockFlagEnum.WAIT_INSTOCK_NOT_QC.getCode());
                count = this.baseMapper.waitInStockListCount(pagingParamDTO);
            }
            if (WaitInStockFlagEnum.ALL.getCode().equals(item.getCode())) {
                count = this.baseMapper.waitInStockListCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    /**
     * 更改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeId
     * @return void
     * @author yl
     * @date 2023-08-14 17:47
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(WarehouseReceiveEntity::getId, id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), WarehouseReceiveEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    @Override
    public SupplierCountDTO countOrderBySupplierId(String supplierId) {
        //获取系统配置
        CfgSettingEntity cfgSetting = cfgSettingService.getByKey(CfgSettingEnum.PO_RECONCILIATION.getCode());
        if (ObjectUtil.isEmpty(cfgSetting) || ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
            log.info("无生成对账单数据");
            return SupplierCountDTO.builder().count(0).localDate(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())).build();
        }
        CfgSettingValueDTO.PoReconciliationSettingDTO dto = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.PoReconciliationSettingDTO.class);
        LocalDate startTime = null;
        LocalDate endTime = null;
        //判断 周期类型
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getReconciliationType())){
            startTime = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            endTime = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        }else if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getReconciliationType())){
            LocalDate currentDate = LocalDate.now();
            String endDate = dto.getEndDate();
            if (StringUtils.isEmpty(endDate) || !NumberUtil.isInteger(endDate)){
                endTime = currentDate.with(TemporalAdjusters.lastDayOfMonth());
                return SupplierCountDTO.builder().count(0).localDate(endTime).build();
            }
            int end = Integer.parseInt(endDate);
            String endStr = String.valueOf(end);
            if (end < 10){
                endStr = "0" + end;
            }
            int dayOfMonth = currentDate.getDayOfMonth();
            String nowMonth = currentDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (dayOfMonth > end){
                //23-下月
                // 获取上个月的日期
                LocalDate beforeMonthDate = currentDate.minusMonths(-1);
                String formattedMonth = beforeMonthDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                startTime = LocalDate.parse(nowMonth + "-" + endStr, formatter);
                endTime = LocalDate.parse(formattedMonth + "-" + endStr, formatter);
            }else {
                //上月 -23
                // 获取上个月的日期
                LocalDate lastMonthDate = currentDate.minusMonths(1);
                String formattedMonth = lastMonthDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                startTime = LocalDate.parse(formattedMonth + "-" + endStr, formatter);
                endTime = LocalDate.parse(nowMonth + "-" + endStr, formatter);
            }
            endTime = endTime.plusDays(-1);
        }
        //根据时间进行查询
        Integer count = baseMapper.countOrderBySupplierId(supplierId,startTime,endTime);
        return SupplierCountDTO.builder().count(count).localDate(endTime).build();
    }

    @Override
    public List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIds(List<String> purchaseOrderIds) {
        if (CollectionUtils.isEmpty(purchaseOrderIds)){
            return Collections.emptyList();
        }
        return baseMapper.getReceiveListByPurchaseOrderIds(purchaseOrderIds);
    }

    @Override
    public List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIdsAll(List<String> purchaseOrderIds) {
        if (CollectionUtils.isEmpty(purchaseOrderIds)){
            return Collections.emptyList();
        }
        return baseMapper.getReceiveListByPurchaseOrderIdsAll(purchaseOrderIds);
    }
    @Override
    public List<WarehouseReceiveEntity> listReceiveBySourceTypeAndIds(WarehouseReceiveDTO.SourceParamDTO dto) {
        if(CharSequenceUtil.isBlank(dto.getSourceType()) || CollectionUtils.isEmpty(dto.getSourceIds())){
            return new ArrayList<>();
        }
        return this.lambdaQuery().eq(WarehouseReceiveEntity::getSourceType,dto.getSourceType()).in(WarehouseReceiveEntity::getSourceId,dto.getSourceIds()).list();
    }


    @Override
    public void updateReceiveInStockStatus(List<PoInstockEntity> list) {
        //修改采购收货单入库状态
        List<WarehouseReceiveDetailEntity> detailEntities = new ArrayList<>();

        list.forEach(poInstockEntity->{
            //获取明细
            List<WarehouseReceiveDetailEntity> receiveDetailEntities = warehouseReceiveDetailService.listDetailByMainIds(Collections.singletonList(poInstockEntity.getSourceId()));
            receiveDetailEntities.forEach(item->{
                //获取收货数量
                Integer receiveQty = item.getReceiveQty();
                if (Objects.nonNull(receiveQty) && receiveQty > 0) {
                    //查询收货单关联的SKU明细的下推的入库单的入库数量【单据已审核】
                    WarehouseReceiveDetailEntity detailEntity = null;
                    Integer instockQty = baseMapper.getQty(poInstockEntity.getSourceId(),item.getId());
                    detailEntity= new WarehouseReceiveDetailEntity();
                    if (Objects.isNull(instockQty)||instockQty==0) {
                        detailEntity.setInStockStatus(InstockStatusEnum.NOT_IN_STOCK.getCode());
                    }else{
                        if (instockQty<receiveQty){
                            detailEntity.setInStockStatus(InstockStatusEnum.PARTIALLY_IN_STOCK.getCode());
                        }
                        if (instockQty.equals(receiveQty)){
                            detailEntity.setInStockStatus(InstockStatusEnum.FULLY_IN_STOCK.getCode());
                        }
                    }
                    detailEntity.setId(item.getId());
                    detailEntities.add(detailEntity);
                }
            });

        });
        if (CollectionUtils.isNotEmpty(detailEntities)) {
            warehouseReceiveDetailService.updateBatchById(detailEntities);
        }
    }

    @Override
    public void instockStatusCleanJob() {
        //获取所有采购收货单入库状态为0的单据
        Integer count = baseMapper.getCount();
        Integer size = 500;
        Integer page = count / size;
        List<List<WarehouseReceiveDetailEntity>> wrdLists = new ArrayList<>();
        for (int i=0;i<=page;i++){
            List<WarehouseReceiveDetailEntity> viewDTOS = new ArrayList<>();
            IPage<WarehouseReceiveDTO.PagingViewDTO> viewDTOIPage = baseMapper.pageDetail(new Page(i+1, size), new WarehouseReceiveDTO.PagingParamDTO());
            List<WarehouseReceiveDTO.PagingViewDTO> list = viewDTOIPage.getRecords();
            if (CollectionUtils.isEmpty(list)){
                continue;
            }
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    WarehouseReceiveDetailEntity viewDTO = null;
                    //获取收货数量
                    Integer receiveQty = item.getReceiveQty();
                    if (Objects.nonNull(receiveQty) && receiveQty > 0) {
                        //查询收货单关联的SKU明细的下推的入库单的入库数量【单据已审核】
                        Integer instockQty = baseMapper.getQty(item.getId(),item.getDetailId());
                        viewDTO= new WarehouseReceiveDetailEntity();
                        if (Objects.isNull(instockQty)||instockQty==0) {
                            viewDTO.setInStockStatus(InstockStatusEnum.NOT_IN_STOCK.getCode());
                        }else{
                            if (instockQty<receiveQty){
                                viewDTO.setInStockStatus(InstockStatusEnum.PARTIALLY_IN_STOCK.getCode());
                            }
                            if (instockQty.equals(receiveQty)){
                                viewDTO.setInStockStatus(InstockStatusEnum.FULLY_IN_STOCK.getCode());
                            }
                        }
                        viewDTO.setId(item.getDetailId());
                        viewDTOS.add(viewDTO);
                    }
                });
                if (CollectionUtils.isNotEmpty(viewDTOS)){
                    wrdLists.add(viewDTOS);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(wrdLists)){
            wrdLists.forEach(wrdList->{
                wrdList.forEach(wrd->{
                    warehouseReceiveDetailService.updateInfo(wrd);
                });
            });
        }
    }

    @Override
    public PagingVO<WarehouseReceiveExportExcelDTO> exportWarehouseReceive(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<WarehouseReceiveExcelDTO> page = baseMapper.warehouseReceiveExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //获取sku的id集合
        List<String> skuIdList = page.getRecords().stream().map(WarehouseReceiveExcelDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //查询质检单
        List<String> receiveIds = page.getRecords().stream().map(WarehouseReceiveExcelDTO::getId).distinct().collect(Collectors.toList());
        //质检信息
        List<QcInfoEntity> qcInfoList = qcInfoService.listQCBySourceIdsAndType(receiveIds,SourceTypeEnum.PO_RECEIVE.getCode());
        List<WarehouseReceiveExportExcelDTO> exportExcelDTOS = new ArrayList<>();
        page.getRecords().forEach(obj -> {
            //设置入库状态名称
            obj.setInStockStatusName(InstockStatusEnum.getByCode(obj.getInStockStatus()));
            List<QcInfoEntity> resultList = qcInfoList.stream().filter(v -> v.getSourceId().equals(obj.getId())).collect(Collectors.toList());
            if(resultList.stream().allMatch(v->Objects.isNull(v.getQcStatus()) || QcBillStatusEnum.DRAFT.equals(v.getQcStatus())|| QcBillStatusEnum.WAIT_QC.equals(v.getQcStatus())|| QcBillStatusEnum.CANCEL.equals(v.getQcStatus()))){
                obj.setQcStatus(PdaQclStatusEnum.WAIT_QC.getCode());
                obj.setQcStatusName(PdaQclStatusEnum.WAIT_QC.getName());
            }else if(resultList.stream().allMatch(v->QcBillStatusEnum.EXEMPTION.equals(v.getQcStatus()) || QcBillStatusEnum.FINISH_QC.equals(v.getQcStatus()))){
                obj.setQcStatus(PdaQclStatusEnum.FINISH_QC.getCode());
                obj.setQcStatusName(PdaQclStatusEnum.FINISH_QC.getName());
            }else{
                obj.setQcStatus(PdaQclStatusEnum.PARTIAL_QC.getCode());
                obj.setQcStatusName(PdaQclStatusEnum.PARTIAL_QC.getName());
            }
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
        return new PagingVO<>(exportExcelDTOS, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public WarehouseReceiveDTO.PagingTotalDTO pagingTotal(WarehouseReceiveDTO.PagingParamDTO dto) {
        dto.setPermissionSql(dto.getPermissionSql());
        WarehouseReceiveDTO.PagingTotalDTO pagingTotalDTO = this.baseMapper.pagingTotal(dto);
        return pagingTotalDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submitEntity(WarehouseReceiveEntity mainEntity) {
        //未作废、待提交、审核不通过才可以提交
        long count = Stream.of(mainEntity).filter(entity -> !entity.getInvalidStatus()
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count <= 0 ) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(mainEntity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个收货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(WarehouseReceiveEntity::getId, mainEntity.getId())
                .set(WarehouseReceiveEntity::getApproveUserId, "")
                .set(WarehouseReceiveEntity::getApproveUserName, "")
                .set(WarehouseReceiveEntity::getApproveTime, null)
                .update();
        return BatchResultDTO.success(mainEntity.getId(), mainEntity.getCode(), OperationTypeEnum.SUBMIT);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalidEntity(WarehouseReceiveEntity entity, String remark) {
        //审核不通过 待提交可以作废
        long count = Stream.of(entity).filter(e -> !e.getInvalidStatus()
                && (e.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || e.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count <= 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        List<String> ids = Collections.singletonList(entity.getId());

        //修改状态为待提交
        lambdaUpdate().set(WarehouseReceiveEntity::getInvalidStatus, Boolean.TRUE)
                .set(WarehouseReceiveEntity::getInvalidRemark, remark)
                .set(WarehouseReceiveEntity::getInvalidTime, LocalDateTime.now())
                .in(WarehouseReceiveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个收货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "作废操作");

        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(ids);
        //如果是收货单下推的，删除时去掉收货单的收获状态和收货数量
        List<String> idByDeliverySource = Stream.of(entity).filter(v-> PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode().equals(v.getSourceType())).map(WarehouseReceiveEntity::getId).distinct().collect(Collectors.toList());
        List<String> detailIdsByDeliverySource = receiveDetailList.stream().filter(v->idByDeliverySource.contains(v.getMainId())).map(WarehouseReceiveDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(detailIdsByDeliverySource)){
            srmDeliveryOrderFeign.cancelReceive(detailIdsByDeliverySource);
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcessEntity(WarehouseReceiveEntity entity) {
        //审核中可以撤销
        long count = Stream.of(entity).filter(e -> !e.getInvalidStatus()
                && e.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count <= 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        List<String> ids = Collections.singletonList(entity.getId());

        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(WarehouseReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(WarehouseReceiveEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList =  Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("收货单【%s】取消流程", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "取消流程操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deleteEntity(WarehouseReceiveEntity entity) {
        //待提交支持删除
        long count = Stream.of(entity).filter(e -> !e.getInvalidStatus()
                && e.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();

        if (count <= 0 ) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());

        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(ids);

        //删除详情表
        warehouseReceiveDetailService.delete(ids);

        boolean flag = this.removeByIds(ids);

        //如果是收货单下推的，删除时去掉收货单的收获状态和收货数量
        List<String> idByDeliverySource = Stream.of(entity).filter(v-> PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode().equals(v.getSourceType())).map(WarehouseReceiveEntity::getId).distinct().collect(Collectors.toList());
        List<String> detailIdsByDeliverySource = receiveDetailList.stream().filter(v->idByDeliverySource.contains(v.getMainId())).map(WarehouseReceiveDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(detailIdsByDeliverySource)){
            srmDeliveryOrderFeign.cancelReceive(detailIdsByDeliverySource);
        }
        if (flag){
            return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        }
    }

    @Override
    public List<WarehouseReceiveDTO.ReceiveSourceDTO> listReceiveSourceByDetailIds(List<String> idList) {
        if (CollUtil.isEmpty(idList)) {
            return Collections.emptyList();
        }
        return baseMapper.listReceiveSourceByDetailIds(idList);
    }
}
