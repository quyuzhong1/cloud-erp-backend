package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_DELIVERY_NOTICE;

/**
 * <p>
 * 发货通知单主表明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
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
    private SoOutstockService soOutstockService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;


    @Resource
    private InventoryService inventoryService;


    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private CfgRuleOutService cfgRuleOutService;
    @Resource
    private CfgRulePickingStagingService  cfgRulePickingStagingService;

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private PackingTaskService packingTaskService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Override
    public PagingVO<SoDeliveryNoticeDTO.PagingView> paging(PagingDTO<SoDeliveryNoticeDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
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
        Map<String,Integer> qtyMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                if (StringUtils.isNotBlank(obj.getPackingStatus())) {
                    obj.setPackingStatusName(PackingTaskStatusEnum.getName(obj.getPackingStatus()));
                }else{
                    obj.setPackingStatus(PackingTaskStatusEnum.WAIT.getCode());
                    obj.setPackingStatusName(PackingTaskStatusEnum.WAIT.getName());
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                if (obj.getDeliveryStatus() != null && obj.getDeliveryStatus()) {
                    obj.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
                } else {
                    obj.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
                }

                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                obj.setUnit(productDetailEntity.getUnitName());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                //如果装箱数量大于发货数量，拆分处理
                if(Objects.nonNull(obj.getDeliveryQty()) && Objects.nonNull(obj.getPackingQty()) && obj.getPackingQty() > obj.getDeliveryQty()){
                    String key = obj.getId() + obj.getSkuId();
                    if(qtyMap.containsKey(key)){
                        Integer reduceQty = qtyMap.get(key);
                        if(reduceQty > obj.getDeliveryQty()){
                            obj.setPackingQty(obj.getDeliveryQty());
                            qtyMap.put(key,reduceQty - obj.getDeliveryQty());
                        }else{
                            obj.setPackingQty(reduceQty);
                            qtyMap.put(key,0);
                        }
                    }else{
                        qtyMap.put(key,obj.getPackingQty() - obj.getDeliveryQty());
                        obj.setPackingQty(obj.getDeliveryQty());
                    }
                }
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
            pagingParam.setPermissionSql(dto.getPermissionSql());
            SoDeliveryNoticeDTO.StatusCountDTO resultDTO = new SoDeliveryNoticeDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (OsDeliveryChangeListTypeEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.UN_SHIPPED.getCode().equals(item.getCode())) {
                pagingParam.setDeliveryStatus(Boolean.FALSE);
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.COMPLETE_SHIPMENT.getCode().equals(item.getCode())) {
                pagingParam.setDeliveryStatus(Boolean.TRUE);
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
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(dto.getSourceId());
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FHTZ, BusinessNoTypeEnum.CODE_FHTZ.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHTZ);
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        soDeliveryNoticeEntity.setType(soInfoEntity.getOrderType());
        soDeliveryNoticeEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        soDeliveryNoticeEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        soDeliveryNoticeEntity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                soDeliveryNoticeEntity.setSalesDeptName(dept.getName());
            }
        }
        //虚拟仓库
        soDeliveryNoticeEntity.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());

        soDeliveryNoticeEntity.setSellerId(soInfoEntity.getSellerId());
        soDeliveryNoticeEntity.setSellerName(soInfoEntity.getSellerName());
        soDeliveryNoticeEntity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soDeliveryNoticeEntity.setCustomerName(customerInfoEntity.getName());
        soDeliveryNoticeEntity.setReceiverName(soInfoEntity.getReceiverName());
        soDeliveryNoticeEntity.setTelNumber(soInfoEntity.getTelNumber());
        soDeliveryNoticeEntity.setReceiveAddress(soInfoEntity.getReceiveAddress());
        soDeliveryNoticeEntity.setDeliveryModeDict(soInfoEntity.getDeliveryMode());
        soDeliveryNoticeEntity.setRequireDate(soInfoEntity.getRequireDate());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(soInfoEntity.getWarehouseOrgName());
        soDeliveryNoticeEntity.setCode(code);
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setSourceType(dto.getSourceType());
        if (dto.getPlanDeliveryDate() != null) {
            soDeliveryNoticeEntity.setPlanDeliveryDate(dto.getPlanDeliveryDate());
        }
        soDeliveryNoticeEntity.setTrackNo(dto.getTrackNo());
        if (StringUtils.isNotBlank(dto.getCarrierId())) {
            //获取采购单供应商信息
            SupplierEntity supplierById = scmTaskFeign.getSupplierById(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierName(supplierById.getName());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        soDeliveryNoticeEntity.setWarehouseOrgId(soInfoEntity.getWarehouseOrgId());
        soDeliveryNoticeEntity.setWarehouseOrgName(soInfoEntity.getWarehouseOrgName());
        this.save(soDeliveryNoticeEntity);
        soDeliveryNoticeDetailService.add(dto, soDeliveryNoticeEntity.getId());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个发货通知单【%s】", code), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), soDeliveryNoticeEntity.getId(), "新增操作");
        //生成装箱任务
        packingTaskService.addPackingByB2BDelivery(soDeliveryNoticeEntity);
        return soDeliveryNoticeEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        // 判断是否已生成拣货单
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(dto.getId()));
        if (CollectionUtils.isNotEmpty(views)) {
            throw new ServiceException(ApiError.ERROR_99140);
        }
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(dto.getSourceId());
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        soDeliveryNoticeEntity.setType(soInfoEntity.getOrderType());
        soDeliveryNoticeEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        soDeliveryNoticeEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        soDeliveryNoticeEntity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                soDeliveryNoticeEntity.setSalesDeptName(dept.getName());
            }
        }
        soDeliveryNoticeEntity.setSellerId(soInfoEntity.getSellerId());
        soDeliveryNoticeEntity.setSellerName(soInfoEntity.getSellerName());
        soDeliveryNoticeEntity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soDeliveryNoticeEntity.setCustomerName(customerInfoEntity.getName());
        soDeliveryNoticeEntity.setReceiverName(soInfoEntity.getReceiverName());
        soDeliveryNoticeEntity.setTelNumber(soInfoEntity.getTelNumber());
        soDeliveryNoticeEntity.setReceiveAddress(soInfoEntity.getReceiveAddress());
        soDeliveryNoticeEntity.setDeliveryModeDict(soInfoEntity.getDeliveryMode());
        soDeliveryNoticeEntity.setRequireDate(soInfoEntity.getRequireDate());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(soInfoEntity.getWarehouseOrgName());
        soDeliveryNoticeEntity.setId(dto.getId());
        SoDeliveryNoticeEntity entity = this.getById(dto.getId());
        soDeliveryNoticeEntity.setApproveStatus(entity.getApproveStatus());
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setTrackNo(dto.getTrackNo());
        if (StringUtils.isNotBlank(dto.getCarrierId())) {
            //获取采购单供应商信息
            SupplierEntity supplierById = scmTaskFeign.getSupplierById(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierName(supplierById.getName());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        soDeliveryNoticeEntity.setWarehouseOrgId(soInfoEntity.getWarehouseOrgId());
        soDeliveryNoticeEntity.setWarehouseOrgName(soInfoEntity.getWarehouseOrgName());
        boolean flag = this.updateById(soDeliveryNoticeEntity);
        //操作日志
        SoDeliveryNoticeEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, soDeliveryNoticeEntity, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), soDeliveryNoticeEntity.getId(), "", "");

        //soDeliveryNoticeDetailService.update(dto);
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
        viewDTO.setTypeName(BillTypeEnum.getName(viewDTO.getType()));
        if (viewDTO.getDeliveryStatus()) {
            viewDTO.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
        } else {
            viewDTO.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
        }

        viewDTO.setDeliveryModeDictName(DeliveryModeEnum.getName(soInfoEntity.getDeliveryMode()));
        List<CustomerAddressEntity> customerAddressEntities = customerFeign.listCustomerAddressByIds(Arrays.asList(soInfoEntity.getReceiveAddressId()));
        CustomerAddressEntity customerAddressEntity = customerAddressEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getReceiveAddressId())).findFirst().orElse(new CustomerAddressEntity());
        viewDTO.setReceiveAddress(customerAddressEntity.getAddress());
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

            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(deliveryNoticeDetailEntity.getId()));
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
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoDeliveryNoticeEntity entity, String type, String comment, Boolean isNeedProcess) {
        //判断是否是审核中的状态
        if (!Boolean.FALSE.equals(entity.getInvalidStatus()) || !ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException("只有未作废和审核中的数据允许审核");
        }
        PackingTaskEntity taskEntity = packingTaskService.getBySourceCode(entity.getCode());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException("未生成装箱任务，不允许审核");
        }
        //检查出库配置，是否需要状态
        CfgRuleOutDTO.CfgOverweightDetailDTO cfgOverweightDetailDTO = cfgRuleOutService.getCfgOverweightDetailDTOByType(taskEntity.getSourceType());
        if(Objects.nonNull(cfgOverweightDetailDTO) && cfgOverweightDetailDTO.isCheckStatusWhenApprove()){
            if(!(taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()) && taskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode()))){
                throw new ServiceException("{已装箱+全部称重}才能审核通过");
            }
        }

        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoDeliveryNoticeEntity::getApproveUserId, userInfo.getUid())
                    .set(SoDeliveryNoticeEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoDeliveryNoticeEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoDeliveryNoticeEntity::getId, entity.getId())
                    .update();

            //销售通知明细信息
            List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeDetailService.listDetailByMainIds(Arrays.asList(entity.getId()));
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_99044);
            }

            //针对拣货单进行库存的多退少补
            handleApproveVirtualInventoryQty(entity);
        } else {
            //审核不通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoDeliveryNoticeEntity::getId, entity.getId())
                    .update();
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个发货通知单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoDeliveryNoticeEntity entity) {
        //已审核支持反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程

        //下推出库单不能反审核
        List<SoOutstockEntity> soOutstockEntityList = soOutstockService.listBySourceId(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(soOutstockEntityList)) {
            throw new ServiceException(ApiError.ERROR_92004);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoDeliveryNoticeEntity::getId, entity.getId())
                .update();
//        //回滚库存
//        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_DELIVERY_NOTICE, ids);
//        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
//        //删除拣货详情
//        pickingDetailService.deleteBySourceId(ids);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个发货通知单【%s】", entity.getCode()), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
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
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】撤销流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "撤销流程操作");

        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单明细");
        }
        //审核不通过 待提交可以作废
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        pickingListsService.exist(ids);

        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(deliveryNoticeEntityList.stream().map(SoDeliveryNoticeEntity::getCode).collect(Collectors.toList()));
        deliveryNoticeEntityList.forEach(v->{
            PackingTaskEntity taskEntity = taskEntityList.stream().filter(t->t.getSourceCode().equals(v.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(taskEntity) && !taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.UNPACKED.getCode())){
                throw new ServiceException("装箱中&已装箱不允许作废");
            }
        });

        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoDeliveryNoticeEntity::getInvalidRemark, remark)
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");

        //发货通知单释放库存
        handleUnLockVirtualInventory(deliveryNoticeEntityList,soDeliveryNoticeDetailList);
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单明细");
        }
        pickingListsService.exist(ids);
        //待提交支持删除
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(deliveryNoticeEntityList.stream().map(SoDeliveryNoticeEntity::getCode).collect(Collectors.toList()));
        deliveryNoticeEntityList.forEach(v->{
            PackingTaskEntity taskEntity = taskEntityList.stream().filter(t->t.getSourceCode().equals(v.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(taskEntity) && !taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.UNPACKED.getCode())){
                throw new ServiceException("装箱中&已装箱不允许删除");
            }
        });
        //删除装箱任务
        taskEntityList.forEach(v->packingTaskService.delete(v));
        //删除详情表
        soDeliveryNoticeDetailService.delete(ids);

        boolean flag = this.removeByIds(ids);

        //释放冻结库存
        handleUnLockVirtualInventory(deliveryNoticeEntityList,soDeliveryNoticeDetailList);
        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoDeliveryNoticeDTO.PagingParam dto) {
        downloadTaskFeign.saveDownloadTask("销售发货通知单", EXPORT_WMS_SO_DELIVERY_NOTICE.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
     * 下推销售出库单
     *
     * @param id id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO generateSoDeliverySave(String id) {
        SoDeliveryNoticeEntity entity = getById(id);
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(entity.getSourceId());
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(soInfoEntity.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_99105);
        }
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySourceId(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_99129);
        }
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> noInventorySkuIds = noInventorySku.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> entityList = soDeliveryNoticeDetailService.listNoInventoryOrPicking(id, noInventorySkuIds);
        long closeCount = entityList.stream().filter(SoDeliveryNoticeDetailEntity::getIsClose).count();
        if (closeCount > 0) {
            throw new ServiceException(ApiError.ERROR_98068);
        }
        boolean allNoInventorySku = Boolean.FALSE;
        if(CollectionUtils.isNotEmpty(entityList)){
            allNoInventorySku = entityList.stream().allMatch(v -> noInventorySkuIds.contains(v.getSkuId()));
        }
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(id));
        if (Boolean.FALSE.equals(allNoInventorySku) && CollectionUtils.isEmpty(views)) {
            throw new ServiceException(ApiError.ERROR_99101, entity.getCode());
        }
        SoInfoEntity info = soInfoFeign.getSoInfoById(entity.getSourceId());
        List<SoInfoDTO.CustomerDTO> customerDTOS = soInfoFeign.listSoCustomer(Collections.singletonList(entity.getSourceId()));
        SoInfoDTO.CustomerDTO customerDTO = customerDTOS.stream().filter(v -> v.getCustomerId().equals(info.getCustomerId())).findFirst().orElse(new SoInfoDTO.CustomerDTO());

        //是否中转
        CfgRuleOutDTO.MatchTransferDTO transferDTO = new CfgRuleOutDTO.MatchTransferDTO();
        CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
        ruleDTO.setType(StockOutTransferTypeEnum.B2B.getCode());
        ruleDTO.setReceiveCountry(customerDTO.getCountryId());
        ruleDTO.setFromWarehouse(entity.getWarehouseId());
        transferDTO.setMatchTransferRuleDTO(ruleDTO);
        transferDTO.setWarehouseId(entity.getWarehouseId());
        CfgRuleOutDTO.MatchTransferResultDTO resultDTO = cfgRuleOutService.matchTransferAndWarehouse(transferDTO);
        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        SoOutstockDTO.AddDTO addDTO = new SoOutstockDTO.AddDTO();
        String batchNo = "";
        String warehouseId;
        if (Boolean.TRUE.equals(resultDTO.getIsTransit())) {
            batchNo = IdUtil.getSnowflake().nextIdStr();
            if (Boolean.FALSE.equals(allNoInventorySku)) {
                generateTransferInfo(entity, batchNo, entityList, warehouseStagingList, noInventorySkuIds, resultDTO.getTransitWarehouseId());
            }
            warehouseId = resultDTO.getTransitWarehouseId();
        }else {
            warehouseId = entity.getWarehouseId();
        }
        //详情id s
        List<String> detailIds = entityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentDbList = wmsAttachmentService.getByBusinessIds(detailIds);
        //获取到销售退货单 下推列表
        addDTO.buildAddDTO(entity);
        addDTO.setWarehouseId(warehouseId);
        addDTO.setCustomerOrderNo(soInfoEntity.getCustomerOrderNo());
        List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>();
        addDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        for (SoDeliveryNoticeDetailEntity item : entityList) {
            SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
            //附件信息
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentDbList.stream().filter(a -> a.getBusinessId().equals(item.getId())).collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            detail.setAttachNameList(attachmentNameList);
            detail.setSoDetailId(item.getSourceDetailId());
            detail.setSourceDetailId(item.getId());
            detail.setSkuId(item.getSkuId());
            detail.setSkuNo(item.getSkuNo());
            detail.setWarehouseId(warehouseId);
            if (Boolean.TRUE.equals(resultDTO.getIsTransit())) {
                detail.setWarehouseLocation("");
            }else {
                detail.setAttachUrlList(attachmentUrlList);
                // 获取仓库暂存区默认配置
                CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                        .filter(staging -> PickingBillTypeEnum.B2B.getCode().equals(staging.getBillType()))
                        .filter(staging -> staging.getWarehouseId().equals(warehouseId))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
                detail.setWarehouseLocation(pickingStaging.getWarehouseLocation());
            }
            if (noInventorySkuIds.contains(item.getSkuId())) {
                detail.setActualQty(item.getDeliveryQty());
                detail.setPlanQty(item.getDeliveryQty());
            }else {
                detail.setActualQty(item.getPickingQty());
                detail.setPlanQty(item.getPickingQty());
            }
            detail.setAttachNameList(attachmentNameList);
            detail.setAttachUrlList(attachmentUrlList);
            detailList.add(detail);
        }
        addDTO.setBatchNo(batchNo);
        addDTO.setDetailList(detailList);
        //销售出库单保存下推单据
        String outId = soOutstockService.add(addDTO);
        return BatchResultDTO.success(outId, "", "下推成功");
    }

    private void generateTransferInfo(SoDeliveryNoticeEntity entity, String batchNo, List<SoDeliveryNoticeDetailEntity> entityList, List<CfgRulePickingStagingEntity> warehouseStagingList, List<String> noInventorySkuIds, String warehouseId) {
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        //获取仓库信息
        if (ObjectUtil.isEmpty(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        TransferInfoDTO.AddDTO transferDto = new TransferInfoDTO.AddDTO();
        transferDto.setType(TransferTypeEnum.CROSS_ORG.getCode());
        transferDto.setBillDate(LocalDate.now());
        transferDto.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        transferDto.setInOrgId(warehouse.getOrgId());
        transferDto.setOutOrgId(entity.getWarehouseOrgId());
        transferDto.setSourceId(entity.getId());
        transferDto.setSourceCode(entity.getCode());
        transferDto.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        transferDto.setBatchNo(batchNo);
        List<TransferInfoDetailDTO.AddDTO> detailList = getAddDTOS(entity, entityList, warehouseId, warehouseStagingList, noInventorySkuIds);
        transferDto.setDetailList(detailList);
        transferInfoService.addAndApprove(transferDto);
    }

    private static List<TransferInfoDetailDTO.AddDTO> getAddDTOS(SoDeliveryNoticeEntity entity, List<SoDeliveryNoticeDetailEntity> entityList, String warehouseId, List<CfgRulePickingStagingEntity> warehouseStagingList, List<String> noInventorySkuIds) {
            // 获取仓库暂存区默认配置
        CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                .filter(staging -> PickingBillTypeEnum.B2B.getCode().equals(staging.getBillType()))
                .filter(staging -> staging.getWarehouseId().equals(entity.getWarehouseId()))
                .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
        List<TransferInfoDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity view : entityList) {
            TransferInfoDetailDTO.AddDTO transferInfoDetail = new TransferInfoDetailDTO.AddDTO();
            transferInfoDetail.setSkuId(view.getSkuId());
            transferInfoDetail.setSkuNo(view.getSkuNo());
            if (noInventorySkuIds.contains(view.getSkuId())) {
                transferInfoDetail.setQty(view.getDeliveryQty());
            }else {
                transferInfoDetail.setQty(view.getPickingQty());
            }
            transferInfoDetail.setOutWarehouseLocation(pickingStaging.getWarehouseLocation());
            transferInfoDetail.setOutWarehouseId(entity.getWarehouseId());
            transferInfoDetail.setInWarehouseId(warehouseId);
            transferInfoDetail.setSourceDetailId(view.getId());
            detailList.add(transferInfoDetail);
        }
        return detailList;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateDeliverySave(List<SoInfoDTO.GenerateDeliveryView> list) {
        List<String> soIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getSoId).distinct().collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getWarehouseId).distinct().collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO paramDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        // 产品属性为费用或服务的sku忽略库存计算
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        for (String soId : soIdList) {
            SoDeliveryNoticeDTO.Add add = new SoDeliveryNoticeDTO.Add();
            add.setSourceId(soId);
            add.setSourceType(SourceTypeEnum.SO_INFO.getCode());
            List<SoInfoDTO.GenerateDeliveryView> viewList = list.stream().filter(req -> req.getSoId().equals(soId)).collect(Collectors.toList());
            List<SoDeliveryNoticeDetailDTO.Add> detailList = new ArrayList<>();
            for (SoInfoDTO.GenerateDeliveryView view : viewList) {
                add.setWarehouseId(view.getWarehouseId());
                add.setPlanDeliveryDate(view.getPlanDeliveryDate());
                SoDeliveryNoticeDetailDTO.Add detailAdd = new SoDeliveryNoticeDetailDTO.Add();
                detailAdd.setDeliveryQty(view.getDeliveryQty());
                detailAdd.setRemark(view.getRemark());
                detailAdd.setSourceDetailId(view.getDetailId());
                detailAdd.setAttachNameList(view.getAttachmentNameList());
                detailAdd.setAttachUrlList(view.getAttachmentUrlList());
                detailList.add(detailAdd);
            }
            add.setDetailList(detailList);
            this.add(add);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoDeliveryNoticeDTO.PagingView> listSoReturnDetailBySourceId(String sourceId) {
        List<SoDeliveryNoticeDTO.PagingView> pagingViews = baseMapper.listSoReturnDetailBySourceId(sourceId);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoDeliveryNoticeDTO.PagingView obj : pagingViews) {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            if (obj.getDeliveryStatus() != null && obj.getDeliveryStatus()) {
                obj.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
            } else {
                obj.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
            }
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setSalesQty(soDetailEntity.getQty());
            obj.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            obj.setCustomerName(customerInfoEntity.getName());
        }
        return pagingViews;
    }


    /**
     * 根据销售 销售订单ids 获取是否有下推的单据
     *
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:27
     */
    @Override
    public Integer getPushDownBySourceIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return 0;
        }
        //发货通知的
        Integer deliveryNoticeCount = this.lambdaQuery().
                in(SoDeliveryNoticeEntity::getSourceId, soIds).
                eq(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.FALSE).
                count();

        Integer soOutStockCount = soOutstockService.getPushDownCountBySoIds(soIds);
        return deliveryNoticeCount + soOutStockCount;
    }

    @Override
    public Map<String,Long> getPushDownDeliveryNoticeCnt(List<String> soIds) {
        if(CollUtil.isEmpty(soIds)) {
            return Maps.newHashMap();
        }
        List<SoDeliveryNoticeEntity> deliveryNoticeList = this.lambdaQuery()
                .in(SoDeliveryNoticeEntity::getSourceId, soIds)
                .eq(SoDeliveryNoticeEntity::getSourceType, SourceTypeEnum.SO_INFO.getCode())
                .eq(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.FALSE).list();

        if(CollUtil.isEmpty(deliveryNoticeList)) {
            return Maps.newHashMap();
        }
        return deliveryNoticeList.stream().collect(Collectors.groupingBy(SoDeliveryNoticeEntity::getSourceId, Collectors.counting()));
    }

    @Override
    public List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> pdaList(SoDeliveryNoticeDTO.PdaSoDeliveryNoticeParam dto) {
        List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> list = baseMapper.pdaList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> dnIds = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainIds(dnIds);
        List<String> dndIds = noticeDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());

        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(dndIds);
        //获取未全部入库的采购收货详情id
        List<String> receiveDetailIds = new ArrayList<>();
        soOutstockDetailEntities.stream().collect(Collectors.groupingBy(n -> n.getSourceDetailId(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            int stockInQty = m.stream().mapToInt(SoOutstockDetailEntity::getActualQty).sum();
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(m.get(MathUtil.ZERO).getSourceDetailId())).findFirst().orElse(new SoDeliveryNoticeDetailEntity());
            if (stockInQty < soDeliveryNoticeDetailEntity.getDeliveryQty()) {
                receiveDetailIds.add(m.get(MathUtil.ZERO).getSourceDetailId());
            }
            return m;
        })));

        List<String> collect = soOutstockDetailEntities.stream().map(req -> req.getSourceDetailId()).distinct().collect(Collectors.toList());
        List<String> ids = dndIds.stream().filter(poid -> !collect.contains(poid)).collect(Collectors.toList());
        receiveDetailIds.addAll(ids);

        if (CollectionUtils.isEmpty(receiveDetailIds)) {
            return new ArrayList<>();
        }

        //根据未发货的发货通知单详情id获取未入库收货单id
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listByIds(receiveDetailIds);
        List<String> notAllsoOutstockDetailIds = detailEntityList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        //获取到未发货的发货通知单返回数据
        List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> soDeliveryNoticeList = list.stream().filter(req -> notAllsoOutstockDetailIds.contains(req.getId())).collect(Collectors.toList());
        soDeliveryNoticeList.sort(Comparator.comparing(SoDeliveryNoticeDTO.PdaSoDeliveryNotice::getCode).reversed());
        soDeliveryNoticeList.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return soDeliveryNoticeList;
    }

    /**
     * 根据来源ids 获取数据
     *
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeEntity>
     * @author yl
     * @date 2023-08-30 19:23
     */
    @Override
    public List<SoDeliveryNoticeEntity> listBySourceIdList(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDeliveryNoticeEntity::getSourceId,sourceIds).list();
    }

    /**
     * PDA:根据发货通知单获取详情
     * @Author Luo_WG
     * @Date 2023/9/6 18:10
     * @param id
     * @return java.lang.Boolean
     **/
    @Override
    public List<SoOutstockDTO.GenerateSoOutstockViewDTO> pdaDeliveryDetail(String id) {
        SoDeliveryNoticeEntity entity = this.getById(id);
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        //获取到销售退货单 下推列表
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        List<SoOutstockDTO.GenerateSoOutstockViewDTO> resultList = baseMapper.listGenerateSoOutstockView(Arrays.asList(id), soDeliveryNotice);
        long closeCount = resultList.stream().filter(s -> s.getIsClose()).count();
        if (closeCount > 0) {
            throw new ServiceException(ApiError.ERROR_98068);
        }

        //详情id s
        List<String> detailIds = resultList.stream().map(SoOutstockDTO.GenerateSoOutstockViewDTO::getSourceDetailId).distinct().collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(entity.getWarehouseId()));
        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentDbList = wmsAttachmentService.getByBusinessIds(detailIds);
        List<String> skuIds = resultList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SoOutstockDTO.GenerateSoOutstockViewDTO item : resultList) {
            item.setSourceType(soDeliveryNotice);
            String detailId = item.getSourceDetailId();
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            item.setVariantProperty(skuVO.getVariantProperty());
            item.setDeliveryQty(item.getQty());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(item.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            item.setWarehouseLocationName(warehouseLocationEntity.getName());
            //附件信息
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentDbList.stream().filter(a -> a.getBusinessId().equals(detailId)).collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            item.setAttachNameList(attachmentNameList);
            item.setAttachUrlList(attachmentUrlList);
        }
        //销售出库单保存下推单据
        return resultList;
    }

    @Override
    public SoDeliveryNoticeDTO.View pdaView(String id) {
        SoDeliveryNoticeDTO.View view = this.view(id);
        List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateSoOutstockViewDTOS = pdaDeliveryDetail(id);
        List<SoDeliveryNoticeDetailDTO.View> soDeliveryNoticeDetailDTOS = BeanMapper.copyList(generateSoOutstockViewDTOS, SoDeliveryNoticeDetailDTO.View.class);
        view.setDetailList(soDeliveryNoticeDetailDTOS);
        return view;
    }


    @Override
    public SoDeliveryNoticeEntity getDeliveryNoticeBySourceId(String sourceId) {
        if (StringUtils.isEmpty(sourceId)){
            return null;
        }
        return this.lambdaQuery().eq(SoDeliveryNoticeEntity::getSourceId, sourceId).eq(SoDeliveryNoticeEntity::getIsDeleted, false)
                .last("limit 1").one();
    }

    @Override
    public List<WarehouseLocationMoveDTO.GenPickToSkuMove> generatePickingList(SoDeliveryNoticeDTO.GeneratePickingDTO picking) {
        SoDeliveryNoticeEntity soDeliveryNotice = getById(picking.getId());
        if (ObjectUtil.isEmpty(soDeliveryNotice)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        if (ApproveStatusEnum.APPROVE.getStatus().equals(soDeliveryNotice.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_99160);
        }
        List<SoDeliveryNoticeDetailEntity> details = soDeliveryNoticeDetailService.list(Wrappers.<SoDeliveryNoticeDetailEntity>lambdaQuery()
                .eq(SoDeliveryNoticeDetailEntity::getMainId, picking.getId())
                .in(SoDeliveryNoticeDetailEntity::getId, picking.getDetailIds())
        );
        //判断sku是否被他人生成了拣货单
        boolean checkUnpickedQty = details.stream().allMatch(detail -> (detail.getDeliveryQty() - detail.getPickingQty()) > 0);
        if (Boolean.FALSE.equals(checkUnpickedQty)) {
            throw new ServiceException(ApiError.UNPICKED_QUANTITY_SHORTAGE);
        }
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(soDeliveryNotice.getSourceId());
        PickingListsDTO.AddDTO addDTO = new PickingListsDTO.AddDTO();
        addDTO.setBillType(PickingBillTypeEnum.B2B.getCode());
        addDTO.setCustomerId(soDeliveryNotice.getCustomerId());
        addDTO.setSourceId(soDeliveryNotice.getId());
        CustomerInfoEntity customerInfoEntity = customerFeign.getCustomerById(soDeliveryNotice.getCustomerId());
        if(Objects.nonNull(customerInfoEntity)){
            addDTO.setCountryCode(customerInfoEntity.getCountryId());
        }
        addDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        addDTO.setSourceCode(soDeliveryNotice.getCode());
        List<SoDeliveryNoticeDetailEntity> updateDetails = new ArrayList<>();
        List<PickingDetailDTO.AddDTO> detailList = picking.getDetailIds().stream()
                .map(id -> {
                    SoDeliveryNoticeDetailEntity detailEntity = details.stream().filter(v -> v.getId().equals(id))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货通知单明细"));
                    SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(v -> v.getId().equals(detailEntity.getSourceDetailId()))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单明细"));
                    PickingDetailDTO.AddDTO detail = new PickingDetailDTO.AddDTO(soDeliveryNotice.getWarehouseId(),
                            soDeliveryNotice.getWarehouseName(),
                            detailEntity.getSkuId(),
                            detailEntity.getSkuNo(),
                            detailEntity.getDeliveryQty() - detailEntity.getPickingQty(),
                            detailEntity.getId(),soDetailEntity.getBomVersion()
                    );
                    detailEntity.setPickingQty(detailEntity.getDeliveryQty());
                    updateDetails.add(detailEntity);
                    return detail;
                }).collect(Collectors.toList());
        addDTO.setDetails(detailList);

        // 执行拣货规则
        List<WarehouseLocationMoveDTO.GenPickToSkuMove> moves = requisitionApplicationService.genPickToSkuMove(soDeliveryNotice.getWarehouseId(), soDeliveryNotice.getWarehouseName(), addDTO);
        if(CollectionUtils.isNotEmpty(moves)){
            return moves;
        }
        pickingListsService.add(addDTO);
        soDeliveryNoticeDetailService.updateBatchById(updateDetails);
        return Collections.emptyList();
    }

    @Override
    public PagingVO<SoDeliveryNoticeDTO.PickingViewDTO> generatePickingView(PagingDTO<String> page) {
        //判断是否存在下游单据，已有下游单据就不能再生成拣货单
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySourceId(Collections.singletonList(page.getParams()));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_99110, "销售出库单");
        }
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkus = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        IPage<SoDeliveryNoticeDTO.PickingViewDTO> picking = baseMapper.pagingPicking(new Page<>(page.getCurrPage(), page.getPageSize()), page.getParams(), ignoreInventorySkus);
        return new PagingVO<>(picking);
    }

    @Override
    public void writeBackData(List<String> sourceDetailIds) {
        List<SoDeliveryNoticeDetailEntity> detailEntities = soDeliveryNoticeDetailService.listByIds(sourceDetailIds);
        List<PickingDetailEntity> pickingDetailEntities = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getSourceDetailId, sourceDetailIds));
        SoDeliveryNoticeEntity entity = getById(detailEntities.get(0).getMainId());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(entity.getSourceId());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailBySourceIds(Collections.singletonList(entity.getSourceId()));
        // 回写数量，处理组合数据
        List<String> skuIds = detailEntities.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        Map<String, Integer> skuQty = soDetailEntities.stream().collect(Collectors.toMap(SoDetailEntity::getSkuId, SoDetailEntity::getQty, Math::addExact));
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        for (SoDeliveryNoticeDetailEntity detailEntity : detailEntities) {
            SoDetailEntity soDetailEntity = soDetailEntities.stream()
                    .filter(v -> v.getId().equals(detailEntity.getSourceDetailId()))
                    .findFirst()
                    .orElse(new SoDetailEntity());
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(soDetailEntity.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).findFirst().orElse(null);
            if (!org.springframework.util.ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                Integer qty = pickingDetailEntities.stream()
                        .filter(v -> v.getSourceDetailId().equals(detailEntity.getId()))
                        .filter(v -> v.getSkuId().equals(bomChildrenSkuDTO.getSkuId()))
                        .map(PickingDetailEntity::getQty)
                        .reduce(0, Math::addExact);
                detailEntity.setPickingQty(qty / Optional.ofNullable(bomChildrenSkuDTO.getQuantity()).orElse(1));
            }else {
                Integer qty = pickingDetailEntities.stream()
                        .filter(v -> v.getSourceDetailId().equals(detailEntity.getId()))
                        .filter(v -> v.getSkuId().equals(detailEntity.getSkuId()))
                        .map(PickingDetailEntity::getQty)
                        .reduce(0, Math::addExact);
                detailEntity.setPickingQty(qty);
            }
            if (detailEntity.getDeliveryQty() < detailEntity.getPickingQty()) {
                throw new ServiceException(ApiError.ERROR_99127, detailEntity.getSkuNo());
            }

        }
        // 增加当次拣货数量和
        soDeliveryNoticeDetailService.updateBatchById(detailEntities);
    }

    @Override
    public void updatePackingStatus(String id, String packingStatus) {
        lambdaUpdate().set(SoDeliveryNoticeEntity::getPackingStatus, packingStatus)
                .eq(SoDeliveryNoticeEntity::getId, id)
                .update();
    }

    @Override
    public BatchResultDTO generatePackingTask(SoDeliveryNoticeEntity entity) {
        packingTaskService.addPackingByB2BDelivery(entity);
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"");
    }

    @Override
    public SoDeliveryNoticeEntity getByCode(String code) {
        if (StringUtils.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(SoDeliveryNoticeEntity::getCode, code).last("limit 1").one();
    }

    @Override
    public List<SoDeliveryNoticeEntity> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDeliveryNoticeEntity::getCode, codes).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO handleErrorData(String id) {
        SoDeliveryNoticeEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            return BatchResultDTO.fail(id, "", "未找到发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeDetailService.listDetailByMainIds(Arrays.asList(id));
        if (CollectionUtils.isEmpty(detailList)) {
            return BatchResultDTO.fail(id, entity.getCode(), "未找到发货通知单明细");
        }
        if (entity.getInvalidStatus()) {
            return BatchResultDTO.fail(id, entity.getCode(), "发货通知单已作废");
        }
        if (StrUtil.isBlank(entity.getVirtualWarehouseId())) {
            return BatchResultDTO.fail(id, entity.getCode(), "发货通知单无虚拟仓");
        }
        //未审核数据
        /**
         * 1、按现有逻辑进行冻结数量转移
         */
        if (!StrUtil.equals(entity.getApproveStatus(),ApproveStatusEnum.APPROVE.getCode())) {
            //虚拟库存扣减
            soDeliveryNoticeDetailService.handleVirtualInventory(id,detailList);

            lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                    .eq(SoDeliveryNoticeEntity::getId,id)
                    .update();
         return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
        }
        //已审核数据
        /**
         * 0、添加一条可用
         * 1、先根据发货数量减少可用添加冻结
         * 2、根据拣货数量进行多退少补
         * 3、判断是否存在直接调拨单，存在则根据直接调拨单进行出库
         * 4、无直接调拨单则根据销售出库单进行出库
         */

        //默认入库一条可用
        List<VirtualInventoryStockDTO.OutInStockDTO> inUsableList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(detailEntity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(entity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            //发货数量
            outInStockDTO.setQty(detailEntity.getDeliveryQty());
            inUsableList.add(outInStockDTO);
        }
        //默认入库一条可用
        if (CollectionUtils.isNotEmpty(inUsableList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.IN_USABLE.getCode());
            stockParamDTO.setParamList(inUsableList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }


        //减少可用添加冻结
        List<VirtualInventoryStockDTO.OutInStockDTO> addList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(detailEntity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(entity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            //发货数量
            outInStockDTO.setQty(detailEntity.getDeliveryQty());
            addList.add(outInStockDTO);
        }
        //补货
        if (CollectionUtils.isNotEmpty(addList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            stockParamDTO.setParamList(addList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
        //针对拣货单进行库存的多退少补
        handleApproveVirtualInventoryQty(entity);

        //判断是否存在直接调拨单（中转的情况会存在直接调拨单）
        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            //审核通过的直接调拨单
            transferInfoList = transferInfoList.stream().filter(obj -> StrUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(transferInfoList)) {
                List<String> mainIdList = transferInfoList.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
                List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listByMainIds(mainIdList);
                if (ObjectUtil.isEmpty(transferInfoDetailList)) {
                    throw new ServiceException("未找到直接调拨单明细信息");
                }
                transferInfoService.updateVirtualInventoryTransCore(transferInfoList,transferInfoDetailList);
            }

            lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                    .eq(SoDeliveryNoticeEntity::getId,id)
                    .update();
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
        }

        //根据销售出库单出库
        List<SoOutstockEntity> soOutstockList = soOutstockService.listBySourceId(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(soOutstockList)) {

            //审核通过的直接调拨单
            soOutstockList = soOutstockList.stream().filter(obj -> StrUtil.equals(obj.getApproveStatus().getCode(),ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());
            //无数据则直接返回
            if (CollectionUtils.isEmpty(soOutstockList)) {
                lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                        .eq(SoDeliveryNoticeEntity::getId,id)
                        .update();
                return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
            }

            List<String> mainIdList = soOutstockList.stream().map(SoOutstockEntity::getId).distinct().collect(Collectors.toList());
            List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(mainIdList);
            if (ObjectUtil.isEmpty(soOutstockDetailList)) {
                throw new ServiceException("未找到销售出库单明细信息");
            }

            //减少可用添加冻结
            List<VirtualInventoryStockDTO.OutInStockDTO> outList = new ArrayList<>();
            for (SoOutstockDetailEntity outstockDetailEntity : soOutstockDetailList) {
                SoOutstockEntity outstockEntity = soOutstockList.stream().filter(obj -> StrUtil.equals(obj.getId(), outstockDetailEntity.getMainId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(outstockEntity)) {
                    throw new ServiceException("销售出库单未找到");
                }
                //虚拟仓为空或者中转过来的出库单不生成流水
                if (StrUtil.isBlank(outstockDetailEntity.getVirtualWarehouseId()) || StrUtil.isNotBlank(outstockEntity.getBatchNo())) {
                    continue;
                }
                VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSourceId(outstockEntity.getId());
                outInStockDTO.setSourceCode(outstockEntity.getCode());
                outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_OUTSTOCK);
                outInStockDTO.setSourceDetailId(outstockDetailEntity.getId());
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSkuId(outstockDetailEntity.getSkuId());
                outInStockDTO.setSkuNo(outstockDetailEntity.getSkuNo());
                outInStockDTO.setWarehouseId(outstockDetailEntity.getWarehouseId());
                outInStockDTO.setVirtualWarehouseId(outstockDetailEntity.getVirtualWarehouseId());
                //出库数量
                outInStockDTO.setQty(outstockDetailEntity.getActualQty());
                outList.add(outInStockDTO);
            }
            if (CollectionUtils.isNotEmpty(outList)) {
                VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
                stockParamDTO.setParamList(outList);
                stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getCode());
                virtualInventoryTransCoreService.approve(stockParamDTO);
            }
        }
        lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                .eq(SoDeliveryNoticeEntity::getId,id)
                .update();
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 处理虚拟仓数量
     * @author will
     * @date 2024/7/25 21:38
     * @param entity
     */
    private void handleApproveVirtualInventoryQty (SoDeliveryNoticeEntity entity) {
        //无虚拟仓则不需要库存扣减
        if (StrUtil.isBlank(entity.getVirtualWarehouseId())) {
            return;
        }

        //发货通知单明细信息
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainId(entity.getId());

        //多退，退回冻结添加可用
        List<VirtualInventoryStockDTO.OutInStockDTO> subList = new ArrayList<>();

        //少补，减少可用添加冻结
        List<VirtualInventoryStockDTO.OutInStockDTO> addList = new ArrayList<>();

        for (SoDeliveryNoticeDetailEntity detailEntity : soDeliveryNoticeDetailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(detailEntity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(entity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            //发货数量
            Integer deliveryQty = detailEntity.getDeliveryQty();
            //拣货数量
            Integer pickingQty = detailEntity.getPickingQty();
            //上次发货数量
            Integer lastPickingQty = detailEntity.getLastPickingQty();
            if (pickingQty > deliveryQty) {
                throw new ServiceException(StrUtil.format("发货通知单【{}】SKU【{}】拣货数量【{}】不能大于发货数量【{}】",entity.getCode(),detailEntity.getSkuNo(),pickingQty,deliveryQty));
            }
            //数量
            Integer qty = pickingQty - lastPickingQty;
            outInStockDTO.setQty(Math.abs(qty));
            if (MathUtil.compareTo(qty,MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            //本次拣货数量大于上次拣货数量则需要补货
            if (pickingQty > lastPickingQty) {
                addList.add(outInStockDTO);
            } else {
                subList.add(outInStockDTO);
            }
            detailEntity.setLastPickingQty(pickingQty);
        }
        //补货
        if (CollectionUtils.isNotEmpty(addList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            stockParamDTO.setParamList(addList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
        //退货
        if (CollectionUtils.isNotEmpty(subList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_LESS.getCode());
            stockParamDTO.setParamList(subList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }

        //更新上次拣货数量
        soDeliveryNoticeDetailService.updateBatchById(soDeliveryNoticeDetailList);
    }

    /**
     * 处理虚拟库存数据
     * @author will
     * @date 2024/6/14 10:39
     * @param deliveryNoticeEntityList
     * @param detailList
     */
    private void handleUnLockVirtualInventory(List<SoDeliveryNoticeEntity> deliveryNoticeEntityList, List<SoDeliveryNoticeDetailEntity> detailList) {
        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();

        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {

            //发货通知单主表信息
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = deliveryNoticeEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_NOT_EXIST);
            }
            //发货通知单参数
            handleSoDeliveryNoticeParam(detailEntity, soDeliveryNoticeEntity,paramList);
        }
        //发货通知单扣减库存
        if (CollectionUtils.isNotEmpty(paramList)) {
            //扣减可用
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(paramList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_LESS.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
    }

    /**
     * 发货通知单参数
     * @author will
     * @date 2024/7/16 19:58
     * @param detailEntity
     * @param soDeliveryNoticeEntity
     * @return OutInStockDTO
     */
    private void handleSoDeliveryNoticeParam (SoDeliveryNoticeDetailEntity detailEntity,SoDeliveryNoticeEntity soDeliveryNoticeEntity
            ,List<VirtualInventoryStockDTO.OutInStockDTO> paramList) {
        //无虚拟仓不扣库存
        if (StrUtil.isBlank(soDeliveryNoticeEntity.getVirtualWarehouseId())) {
            return;
        }
        //拣货数据已被删无需退回库存
        if (MathUtil.compareTo(detailEntity.getLastPickingQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return;
        }
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
        outInStockDTO.setSourceId(soDeliveryNoticeEntity.getId());
        outInStockDTO.setSourceCode(soDeliveryNoticeEntity.getCode());
        outInStockDTO.setSourceDetailId(detailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(detailEntity.getSkuId());
        outInStockDTO.setSkuNo(detailEntity.getSkuNo());
        outInStockDTO.setQty(detailEntity.getLastPickingQty());
        outInStockDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
        paramList.add(outInStockDTO);
    }


    @Override
    public PagingVO<SoDeliveryNoticeDTO.PagingView> exportSoDeliveryNotice(PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {

        Page<SoDeliveryNoticeDTO.PagingView> pagingViews = baseMapper.soDeliveryNoticeExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //获取sku的id集合
        List<String> skuIdList = pagingViews.getRecords().stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.getRecords().stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoDeliveryNoticeDTO.PagingView pagingView : pagingViews.getRecords()) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            if (pagingView.getDeliveryStatus()) {
                pagingView.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
            } else {
                pagingView.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
            }
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            pagingView.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
            pagingView.setDeliveryStatusName(pagingView.getDeliveryStatus() ? "已发货" : "未发货");
        }
        return new PagingVO<>(pagingViews);
    }
}
