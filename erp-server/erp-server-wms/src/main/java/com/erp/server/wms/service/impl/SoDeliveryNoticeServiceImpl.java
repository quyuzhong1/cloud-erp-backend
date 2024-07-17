package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.StateEnumValue;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.IdUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
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
        return soDeliveryNoticeEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
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

        soDeliveryNoticeDetailService.update(dto);
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
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        SoDeliveryNoticeEntity entity = this.getById(dto.getId());
        //判断是否是审核中的状态
        if (!Boolean.FALSE.equals(entity.getInvalidStatus()) || !ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(dto.getType())) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoDeliveryNoticeEntity::getApproveUserId, userInfo.getUid())
                    .set(SoDeliveryNoticeEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoDeliveryNoticeEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoDeliveryNoticeEntity::getId, dto.getId())
                    .update();

            //销售通知明细信息
            List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeDetailService.listDetailByMainIds(Arrays.asList(dto.getId()));
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_99044);
            }

            //虚拟库存扣减
            handleVirtualInventory(Arrays.asList(entity),detailList);

        } else {
            //审核不通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoDeliveryNoticeEntity::getId, dto.getId())
                    .update();
        }
        //操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 处理虚拟库存数据
     * @author will
     * @date 2024/6/14 10:39
     * @param deliveryNoticeEntityList
     * @param detailList
     */
    private void handleVirtualInventory (List<SoDeliveryNoticeEntity> deliveryNoticeEntityList,List<SoDeliveryNoticeDetailEntity> detailList) {
        //销售明细
        List<String> sourceDetailIdList = detailList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailIdList);

        //销售订单信息
        List<String> mainIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(mainIdList);

        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> soParamList = new ArrayList<>();

        //发货通知单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {

            //销售明细
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }

            //销售订单
            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }

            //发货通知单主表信息
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = deliveryNoticeEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_NOT_EXIST);
            }

            //销售订单参数
            VirtualInventoryStockDTO.OutInStockDTO soOutInStockDTO = handleSoParam(soInfoEntity, soDetailEntity, detailEntity, soDeliveryNoticeEntity);

            //发货通知单参数
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = handleSoDeliveryNoticeParam(detailEntity, soDeliveryNoticeEntity);

            //无虚拟仓不扣库存
            if (StrUtil.isBlank(soDeliveryNoticeEntity.getVirtualWarehouseId())) {
                continue;
            }

            //添加发货通知单参数
            paramList.add(outInStockDTO);

            //添加销售订单参数
            if (MathUtil.compareTo(soOutInStockDTO.getQty(),MathUtil.ZERO) != MathUtil.ZERO) {
                soParamList.add(soOutInStockDTO);
            }
        }
        //销售订单扣减库存
        if (CollectionUtils.isNotEmpty(soParamList)) {
            //扣减冻结，添加可用
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(soParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_UNLOCK.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);

            //更新销售订单冻结数量
            List<SoDetailDTO.UpdateFrozenQtyDTO> updateList = soParamList.stream().map(obj -> new SoDetailDTO.UpdateFrozenQtyDTO(obj.getSourceDetailId(), obj.getQty())).collect(Collectors.toList());
            soInfoFeign.updateFrozenQty(updateList);
        }

        //发货通知单扣减库存
        if (CollectionUtils.isNotEmpty(paramList)) {
            //扣减可用
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(paramList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE.getCode());
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
    private VirtualInventoryStockDTO.OutInStockDTO handleSoDeliveryNoticeParam (SoDeliveryNoticeDetailEntity detailEntity,SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
        outInStockDTO.setSourceId(soDeliveryNoticeEntity.getId());
        outInStockDTO.setSourceCode(soDeliveryNoticeEntity.getCode());
        outInStockDTO.setSourceDetailId(detailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(detailEntity.getSkuId());
        outInStockDTO.setSkuNo(detailEntity.getSkuNo());
        outInStockDTO.setQty(detailEntity.getDeliveryQty());
        outInStockDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
        return  outInStockDTO;
    }

    /**
     * 销售订单参数
     * @author will
     * @date 2024/7/16 19:47
     * @param soInfoEntity
     * @param soDetailEntity
     * @param detailEntity
     * @param soDeliveryNoticeEntity
     * @return OutInStockDTO
     */
    private VirtualInventoryStockDTO.OutInStockDTO handleSoParam(SoInfoEntity soInfoEntity,SoDetailEntity soDetailEntity,SoDeliveryNoticeDetailEntity detailEntity,SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        boolean isFrozen = soDetailEntity.getFrozenQty() > detailEntity.getDeliveryQty();
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        if (isFrozen) {
            outInStockDTO.setQty(detailEntity.getDeliveryQty());
        } else {
            outInStockDTO.setQty(soDetailEntity.getFrozenQty());
        }
        outInStockDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
        return outInStockDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        SoDeliveryNoticeEntity entity = this.getById(id);
        //已审核支持反审核
        if (!Boolean.FALSE.equals(entity.getInvalidStatus()) || !ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程

        //下推出库单不能反审核
        List<SoOutstockEntity> soOutstockEntityList = soOutstockService.listBySourceId(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(soOutstockEntityList)) {
            throw new ServiceException(ApiError.ERROR_92004);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoDeliveryNoticeEntity::getId, id)
                .update();
//        //回滚库存
//        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_DELIVERY_NOTICE, ids);
//        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
//        //删除拣货详情
//        pickingDetailService.deleteBySourceId(ids);

        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_DELIVERY_NOTICE, Arrays.asList(id));

        //回滚虚拟库存
        virtualInventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
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
        pickingListsService.exist(ids);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        pickingListsService.exist(ids);
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


    /**
     * 下推销售出库单
     *
     * @param id id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateSoDeliverySave(String id) {
        SoDeliveryNoticeEntity entity = getById(id);
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(views)) {
            throw new ServiceException(ApiError.ERROR_99101, entity.getCode());
        }
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(entity.getSourceId());
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(soInfoEntity.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_99105);
        }
        List<SoDeliveryNoticeDetailEntity> entityList = soDeliveryNoticeDetailService.listDetailByMainId(id);
        long closeCount = entityList.stream().filter(SoDeliveryNoticeDetailEntity::getIsClose).count();
        if (closeCount > 0) {
            throw new ServiceException(ApiError.ERROR_98068);
        }
        SoOutstockDTO.AddDTO addDTO = new SoOutstockDTO.AddDTO();
        String batchNo = "";
        String warehouseId;
        //todo 判断是否需要中转
        Boolean isTransit = Boolean.TRUE;
        if (isTransit) {
            batchNo = IdUtil.getSnowflake().nextIdStr();
            warehouseId = generateTransferInfo(entity, batchNo, views);
        }else {
            warehouseId = entity.getWarehouseId();
        }
        //详情id s
        List<String> detailIds = entityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentDbList = wmsAttachmentService.getByBusinessIds(detailIds);
        //获取到销售退货单 下推列表
        addDTO.buildAddDTO(entity);
        addDTO.setCustomerOrderNo(soInfoEntity.getCustomerOrderNo());
        List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (PickingListsDTO.SourceView item : views) {
            SoDeliveryNoticeDetailEntity detailEntity = entityList.stream()
                    .filter(v -> v.getId().equals(item.getSourceDetailId()))
                    .findFirst()
                    .orElse(new SoDeliveryNoticeDetailEntity());
            //附件信息
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentDbList.stream().filter(a -> a.getBusinessId().equals(detailEntity.getId())).collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
            detail.setSoDetailId(detailEntity.getSourceDetailId());
            detail.setSourceDetailId(item.getSourceDetailId());
            detail.setSkuId(item.getSkuId());
            detail.setSkuNo(item.getSkuNo());
            detail.setRemark(detailEntity.getRemark());
            detail.setWarehouseId(warehouseId);
            if (isTransit) {
                detail.setWarehouseLocation("");
            }else {
                detail.setWarehouseLocation(item.getWarehouseLocation());
            }
            detail.setActualQty(item.getQty());
            detail.setPlanQty(item.getQty());
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

    private String generateTransferInfo(SoDeliveryNoticeEntity entity, String batchNo, List<PickingListsDTO.SourceView> views) {
        String warehouseId;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, CfgSettingEnum.TRANSIT_SETTING.getCode())
                .eq(CfgSettingEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未配置中转设置仓库");
        }
        CfgSettingValueDTO.TransitSettingDTO transitSettingDTO = BeanUtil.toBean(list.get(0).getDataJson(), CfgSettingValueDTO.TransitSettingDTO.class);
        if (CharSequenceUtil.isBlank(transitSettingDTO.getWarehouseId())) {
            throw new ServiceException("中转设置仓库不能为空");
        }
        warehouseId = transitSettingDTO.getWarehouseId();
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
        List<TransferInfoDetailDTO.AddDTO> detailList = getAddDTOS(entity, views, warehouseId);
        transferDto.setDetailList(detailList);
        transferInfoService.addAndApprove(transferDto);
        return warehouseId;
    }

    private static List<TransferInfoDetailDTO.AddDTO> getAddDTOS(SoDeliveryNoticeEntity entity, List<PickingListsDTO.SourceView> views, String warehouseId) {
        List<TransferInfoDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (PickingListsDTO.SourceView view : views) {
            TransferInfoDetailDTO.AddDTO transferInfoDetail = new TransferInfoDetailDTO.AddDTO();
            transferInfoDetail.setSkuId(view.getSkuId());
            transferInfoDetail.setSkuNo(view.getSkuNo());
            transferInfoDetail.setQty(view.getQty());
            transferInfoDetail.setOutWarehouseLocation(view.getStagingLocation());
            transferInfoDetail.setOutWarehouseId(entity.getWarehouseId());
            transferInfoDetail.setInWarehouseId(warehouseId);
            transferInfoDetail.setSourceDetailId(view.getSourceDetailId());
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
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryService.listSkuInventory(paramDTO);

        // 产品属性为费用或服务的sku忽略库存计算
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

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

                if(ignoreInventorySkuIds.contains(view.getSkuId())) {
                    log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", view.getSkuId(), view.getSkuNo());
                } else {
                    //即时库存
                    Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(view.getSkuId()) && s.getWarehouseId().equals(view.getWarehouseId())).mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
                    if (view.getDeliveryQty() > curInventoryQty) {
                        throw new ServiceException(ApiError.ERROR_99070, view.getSkuNo());
                    }
                }
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
    public void generatePickingList(SoDeliveryNoticeDTO.GeneratePickingDTO picking) {
        SoDeliveryNoticeEntity soDeliveryNotice = getById(picking.getId());
        if (ObjectUtil.isEmpty(soDeliveryNotice)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
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
        PickingListsDTO.AddDTO addDTO = new PickingListsDTO.AddDTO();
        addDTO.setBillType(PickingBillTypeEnum.B2B.getCode());
        addDTO.setCustomerId(soDeliveryNotice.getCustomerId());
        addDTO.setSourceId(soDeliveryNotice.getId());
        addDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        addDTO.setSourceCode(soDeliveryNotice.getCode());
        List<SoDeliveryNoticeDetailEntity> updateDetails = new ArrayList<>();
        List<PickingDetailDTO.AddDTO> detailList = picking.getDetailIds().stream()
                .map(id -> {
                    SoDeliveryNoticeDetailEntity detailEntity = details.stream().filter(v -> v.getId().equals(id))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_400));
                    PickingDetailDTO.AddDTO detail = new PickingDetailDTO.AddDTO(soDeliveryNotice.getWarehouseId(),
                            soDeliveryNotice.getWarehouseName(),
                            detailEntity.getSkuId(),
                            detailEntity.getSkuNo(),
                            detailEntity.getDeliveryQty() - detailEntity.getPickingQty(),
                            detailEntity.getId()
                    );
                    detailEntity.setPickingQty(detailEntity.getDeliveryQty());
                    updateDetails.add(detailEntity);
                    return detail;
                }).collect(Collectors.toList());
        addDTO.setDetails(detailList);
        pickingListsService.add(addDTO);
        soDeliveryNoticeDetailService.updateBatchById(updateDetails);
    }

    @Override
    public List<SoDeliveryNoticeDTO.PickingViewDTO> generatePickingView(String id) {
        //判断是否存在下游单据，已有下游单据就不能再生成拣货单
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySourceId(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_99110, "销售出库单");
        }
        List<SoDeliveryNoticeDetailEntity> details = soDeliveryNoticeDetailService.listDetailByMainId(id);
        List<SoDeliveryNoticeDTO.PickingViewDTO> result = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity detail : details) {
            if (detail.getDeliveryQty() - detail.getPickingQty() <= 0){
                continue;
            }
            SoDeliveryNoticeDTO.PickingViewDTO viewDTO = new SoDeliveryNoticeDTO.PickingViewDTO();
            viewDTO.setDetailId(detail.getId());
            viewDTO.setSkuId(detail.getSkuId());
            viewDTO.setSkuNo(detail.getSkuNo());
            viewDTO.setPlanQty(detail.getDeliveryQty());
            viewDTO.setPickedQuantity(detail.getPickingQty());
            viewDTO.setUnpickedQuantity(detail.getDeliveryQty() - detail.getPickingQty());
            result.add(viewDTO);
        }
        return result;
    }

    @Override
    public void writeBackData(String sourceId) {
        List<SoDeliveryNoticeDetailEntity> detailEntities = soDeliveryNoticeDetailService.listDetailByMainId(sourceId);
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(sourceId));
        Map<String, Integer> detailQtyMap = views.stream()
                .collect(Collectors.toMap(PickingListsDTO.SourceView::getSkuId, PickingListsDTO.SourceView::getQty, Integer::sum));
        for (SoDeliveryNoticeDetailEntity detailEntity : detailEntities) {
            int qty = Optional.ofNullable(detailQtyMap.get(detailEntity.getSkuId())).orElse(0);
            if (qty > detailEntity.getDeliveryQty()) {
                detailEntity.setPickingQty(detailEntity.getDeliveryQty());
                detailQtyMap.put(detailEntity.getSkuId(), qty - detailEntity.getDeliveryQty());
            } else {
                detailEntity.setPickingQty(qty);
                detailQtyMap.put(detailEntity.getSkuId(), 0);
            }
        }
        soDeliveryNoticeDetailService.updateBatchById(detailEntities);
    }
}
