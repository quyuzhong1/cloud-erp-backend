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
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.TransferDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.OsDeliveryChangeListTypeEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货通知单主表明细表 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoDeliveryNoticeServiceImpl extends SuperServiceImpl<SoDeliveryNoticeMapper, SoDeliveryNoticeEntity> implements SoDeliveryNoticeService {
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;
    
    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OperateLogService operateLogService;
    
    @Resource
    private CommonService commonService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Override
    public PagingVO<SoDeliveryNoticeDTO.PagingView> paging(PagingDTO<SoDeliveryNoticeDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoDeliveryNoticeDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoDeliveryNoticeDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = records.stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setCustomerName(null);
                    obj.setDeliveryOrgName(null);
                    obj.setApproveStatusName(null);
                    obj.setApproveStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setDeliveryStatusDictName(null);
                    obj.setDeliveryStatusDict(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setDeliveryStatusDictName(DeliveryStatusEnum.getName(obj.getDeliveryStatusDict()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                obj.setUnit(productDetailEntity.getUnitName());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoDeliveryNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        OsDeliveryChangeListTypeEnum[] values = OsDeliveryChangeListTypeEnum.values();
        List<SoDeliveryNoticeDTO.StatusCountDTO> list = new ArrayList<>();
        for (OsDeliveryChangeListTypeEnum item : values) {
            SoDeliveryNoticeDTO.PagingParam pagingParam = new SoDeliveryNoticeDTO.PagingParam();
            pagingParam.setParam(dto.getParam());
            SoDeliveryNoticeDTO.StatusCountDTO resultDTO = new SoDeliveryNoticeDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (OsDeliveryChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.UN_SHIPPED.getCode().equals(item.getCode())) {
                pagingParam.setDeliveryStatusDict(DeliveryStatusEnum.UN_SHIPPED.getCode());
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.COMPLETE_SHIPMENT.getCode().equals(item.getCode())) {
                pagingParam.setDeliveryStatusDict(DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode());
                count = this.baseMapper.listCount(pagingParam);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoDeliveryNoticeDTO.Add dto) {
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getDeliveryOrgId());

        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(dto.getSourceId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FHTZ, BusinessNoTypeEnum.CODE_FHTZ.getCode()));
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        BeanMapperUtils.copy(soInfoEntity, soDeliveryNoticeEntity);
        soDeliveryNoticeEntity.setId(null);
        soDeliveryNoticeEntity.setApproveStatus(null);
        soDeliveryNoticeEntity.setCode(code);
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setSourceType(dto.getSourceType());
        soDeliveryNoticeEntity.setDeliveryOrgId(dto.getDeliveryOrgId());
        soDeliveryNoticeEntity.setDeliveryOrgName(sysAccountingCompanyEntity.getCompanyName());

        if (ObjectUtil.isNotEmpty(dto.getCarrierId())) {
            //获取采购单供应商信息
            PurchaseOrderSupplierEntity supplierEntity = scmTaskFeign.getOrderSupplierByOrderId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierName(supplierEntity.getSupplierName());
        }
        soDeliveryNoticeEntity.setWarehouseId(dto.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        //soDeliveryNoticeEntity.setDeliveryModeDict(dto.getDeliveryModeDict());
        //soDeliveryNoticeEntity.setReceiverName(dto.getReceiverName());
        //soDeliveryNoticeEntity.setTelNumber(dto.getTelNumber());
        //soDeliveryNoticeEntity.setReceiveAddress(dto.getReceiveAddress());
        this.save(soDeliveryNoticeEntity);
        soDeliveryNoticeDetailService.add(dto, soDeliveryNoticeEntity.getId());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个发货通知单【%s】", code), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), soDeliveryNoticeEntity.getId(), "新增操作");
        return soDeliveryNoticeEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getDeliveryOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(dto.getSourceId());
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        BeanMapperUtils.copy(soInfoEntity, soDeliveryNoticeEntity);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soDeliveryNoticeEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soDeliveryNoticeEntity.setCustomerName(customerInfoEntity.getName());
        soDeliveryNoticeEntity.setId(dto.getId());
        SoDeliveryNoticeEntity entity = this.getById(dto.getId());
        soDeliveryNoticeEntity.setApproveStatus(entity.getApproveStatus());
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setDeliveryOrgId(dto.getDeliveryOrgId());
        soDeliveryNoticeEntity.setDeliveryOrgName(sysAccountingCompanyEntity.getCompanyName());
        if (ObjectUtil.isNotEmpty(dto.getCarrierId())) {
            //获取采购单供应商信息
            PurchaseOrderSupplierEntity supplierEntity = scmTaskFeign.getOrderSupplierByOrderId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierName(supplierEntity.getSupplierName());
        }
        soDeliveryNoticeEntity.setWarehouseId(dto.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        //soDeliveryNoticeEntity.setDeliveryModeDict(dto.getDeliveryModeDict());
        //soDeliveryNoticeEntity.setReceiverName(dto.getReceiverName());
        //soDeliveryNoticeEntity.setTelNumber(dto.getTelNumber());
        //soDeliveryNoticeEntity.setReceiveAddress(dto.getReceiveAddress());
        boolean flag = this.updateById(soDeliveryNoticeEntity);
        soDeliveryNoticeDetailService.update(dto);
        //操作日志
        SoDeliveryNoticeEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, soDeliveryNoticeEntity, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), soDeliveryNoticeEntity.getId(), "", "");
        return flag;
    }

    @Override
    public SoDeliveryNoticeDTO.View view(String id) {
        SoDeliveryNoticeDTO.View viewDTO = new SoDeliveryNoticeDTO.View();
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = this.getById(id);

        //创库保存详情表的集合
        List<SoDeliveryNoticeDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(id);
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soDeliveryNoticeEntity.getSourceId());
        BeanMapperUtils.copy(soInfoEntity, viewDTO);
        BeanMapperUtils.copy(soDeliveryNoticeEntity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soDeliveryNoticeEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        viewDTO.setCustomerName(customerInfoEntity.getName());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setDeliveryStatusDictName(DeliveryStatusEnum.getName(viewDTO.getDeliveryStatusDict()));
        for (SoDeliveryNoticeDetailEntity deliveryNoticeDetailEntity : detailEntityList) {
            SoDeliveryNoticeDetailDTO.View detailView = new SoDeliveryNoticeDetailDTO.View();
            BeanMapperUtils.copy(deliveryNoticeDetailEntity, detailView);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(deliveryNoticeDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(deliveryNoticeDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setSalesQty(soDetailEntity.getQty());

            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(id));
            List<String> attachmentUrlList = attachmentList.stream().
                    map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().
                    map(WmsAttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            detailView.setAttachUrlList(attachmentUrlList);
            detailView.setAttachNameList(attachmentNameList);
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        List<SoDeliveryNoticeEntity> soDeliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = soDeliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != soDeliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程
        //操作日志
        List<Pair<String, String>> pairList = soDeliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个发货通知单【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoDeliveryNoticeDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoDeliveryNoticeDTO.Update dto) {
        Boolean update = this.update(dto);
        if (update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //判断是否是审核中的状态
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(baseApproveParamDTO.getType())) {
            LoginUser userInfo = commonService.getUserInfo();
            //审核通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoDeliveryNoticeEntity::getApproveUserId, userInfo.getUid())
                    .set(SoDeliveryNoticeEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoDeliveryNoticeEntity::getApproveTime, LocalDateTime.now())
                    .in(SoDeliveryNoticeEntity::getId, ids)
                    .update();

            generatePickingDetail(deliveryNoticeEntityList);
        } else {
            //审核不通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(SoDeliveryNoticeEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个发货通知单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //已审核支持反审核
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
        ).count();
        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        //TODO 待加审核流程

        //下推出库单不能反审核
        List<SoOutstockEntity> soOutstockEntityList = soOutstockService.listBySourceId(ids);
        if (CollectionUtils.isNotEmpty(soOutstockEntityList)) {
            throw new ServiceException(ApiError.ERROR_92004);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();

        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_DELIVERY_NOTICE,ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //删除拣货详情
        pickingDetailService.deleteBySourceId(ids);

        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个发货通知单【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】取消流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核不通过 待提交可以作废
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoDeliveryNoticeEntity::getInvalidRemark, remark)
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");
        /*deliveryNoticeEntityList.forEach(req -> {
            //修改到货状态
            deliveryNoticeEntityList.updateArrivalState(req.getPurchaseOrderId());
        });*/
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //待提交支持删除
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除详情表
        soDeliveryNoticeDetailService.delete(ids);

        boolean flag = this.removeByIds(ids);

    /*    deliveryNoticeEntityList.forEach(req -> {
            //修改到货状态
            purchaseReturnOrderService.updateArrivalState(req.getPurchaseOrderId());
        });*/

        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoDeliveryNoticeDTO.PagingParam dto, HttpServletResponse response) {
        List<SoDeliveryNoticeDTO.PagingView> pagingViews = baseMapper.soDeliveryNoticeExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoDeliveryNoticeDTO.PagingView pagingView : pagingViews) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            pagingView.setDeliveryStatusDictName(DeliveryStatusEnum.getName(pagingView.getDeliveryStatusDict()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            pagingView.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soDeliveryNoticeExport.xlsx";
        String name = "销售发货通知单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pagingViews, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoDeliveryNoticeDTO.GenerateSoDeliveryView> generateStockInView(List<String> ids) {
        List<SoDeliveryNoticeDTO.GenerateSoDeliveryView> generateSoDeliveryViews = baseMapper.generateStockInView(ids);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = generateSoDeliveryViews.stream().map(SoDeliveryNoticeDTO.GenerateSoDeliveryView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        //获取sku的id集合
        List<String> skuIdList = generateSoDeliveryViews.stream().map(SoDeliveryNoticeDTO.GenerateSoDeliveryView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (SoDeliveryNoticeDTO.GenerateSoDeliveryView generateSoDeliveryView : generateSoDeliveryViews) {
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(generateSoDeliveryView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            generateSoDeliveryView.setSalesQty(soDetailEntity.getQty());
            generateSoDeliveryView.setDeliveryQty(soDetailEntity.getQty());
            generateSoDeliveryView.setPlanDeliveryDate(generateSoDeliveryView.getRequireDate());

            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(generateSoDeliveryView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            generateSoDeliveryView.setProductName(productDetailEntity.getName());
        }
        return generateSoDeliveryViews;
    }

    @Override
    public Boolean generateSoDeliverySave(List<SoDeliveryNoticeDTO.GenerateSoDeliveryView> list) {
        return null;
    }

    private void generatePickingDetail(List<SoDeliveryNoticeEntity> list) {
        //生成拣货明细
        List<String> ids = list.stream().map(SoDeliveryNoticeEntity::getId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        //拣货明细集合
        List<PickingDetailDTO.CommonDTO> addList = new ArrayList<>();
        for (SoDeliveryNoticeEntity entity :list) {
            List<SoDeliveryNoticeDetailEntity> detailEntities = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntities)) {
                throw new ServiceException(ApiError.ERROR_99044);
            }
            //获取仓库信息
            WarehouseEntity warehouseEntity = warehouseService.getById(entity.getWarehouseId());

            //获取sku的id集合
            List<String> skuIdList = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
            //根据ids查询sku信息
            List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

            for (SoDeliveryNoticeDetailEntity detailEntity : detailEntities) {

                //查询可用库存生成拣货明细
                PickingDetailDTO.InventoryParamDTO dto = new PickingDetailDTO.InventoryParamDTO(warehouseEntity.getOrgId(),warehouseEntity.getName(),entity.getWarehouseId(),
                        entity.getWarehouseName(),detailEntity.getSkuId(),detailEntity.getSkuNo(),detailEntity.getDeliveryQty());
                List<InventoryEntity> inventoryList = inventoryService.listPickingDetailInventory(dto);

                List<PickingDetailDTO.CommonDTO> pickingDetailList = BeanMapperUtils.copyList(PickingDetailDTO.CommonDTO.class, inventoryList);

                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());

                for (PickingDetailDTO.CommonDTO addDTO : pickingDetailList) {
                    addDTO.setSourceId(entity.getId());
                    addDTO.setSourceCode(entity.getCode());
                    addDTO.setSourceType(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
                    addDTO.setSourceDetailId(detailEntity.getId());
                    addDTO.setUnit(productDetailEntity.getUnitName());
                    addDTO.setWarehouseName(entity.getWarehouseName());
                    addDTO.setOrgName(warehouseEntity.getName());
                }
                addList.addAll(pickingDetailList);
            }
        }
        //添加拣货明细数据
        pickingDetailService.add(addList);
    }
}
