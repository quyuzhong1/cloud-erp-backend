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
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoOutstockEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.OsDeliveryChangeListTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.wms.mapper.SoDeliveryNoticeMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OperateLogService operateLogService;
    
    @Resource
    private CommonService commonService;

    @Resource
    private SoOutstockService soOutstockService;

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
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
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
            if (OsDeliveryChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
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
    public String add(SoDeliveryNoticeDTO.Add dto) {
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getDeliveryOrgId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity supplierEntity = scmTaskFeign.getOrderSupplierByOrderId(dto.getCarrierId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getSourceId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FHTZ, BusinessNoTypeEnum.CODE_FHTZ.getCode()));
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        BeanMapperUtils.copy(soInfoEntity, soDeliveryNoticeEntity);
        soDeliveryNoticeEntity.setCode(code);
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setSourceType(dto.getSourceType());
        soDeliveryNoticeEntity.setDeliveryOrgId(dto.getDeliveryOrgId());
        soDeliveryNoticeEntity.setDeliveryOrgName(sysAccountingCompanyEntity.getCompanyName());
        soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
        soDeliveryNoticeEntity.setCarrierName(supplierEntity.getSupplierName());
        soDeliveryNoticeEntity.setWarehouseId(dto.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        soDeliveryNoticeEntity.setReceiverName(dto.getReceiverName());
        soDeliveryNoticeEntity.setTelNumber(dto.getTelNumber());
        soDeliveryNoticeEntity.setDeliveryModeDict(dto.getDeliveryModeDict());
        soDeliveryNoticeEntity.setReceiveAddress(dto.getReceiveAddress());
        this.save(soDeliveryNoticeEntity);
        soDeliveryNoticeDetailService.add(dto, soDeliveryNoticeEntity.getId());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个发货通知单【%s】", code), ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), soDeliveryNoticeEntity.getId(), "新增操作");
        return soDeliveryNoticeEntity.getId();
    }

    @Override
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getDeliveryOrgId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity supplierEntity = scmTaskFeign.getOrderSupplierByOrderId(dto.getCarrierId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getSourceId());
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        BeanMapperUtils.copy(soInfoEntity, soDeliveryNoticeEntity);
        soDeliveryNoticeEntity.setId(dto.getId());
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setDeliveryOrgId(dto.getDeliveryOrgId());
        soDeliveryNoticeEntity.setDeliveryOrgName(sysAccountingCompanyEntity.getCompanyName());
        soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
        soDeliveryNoticeEntity.setCarrierName(supplierEntity.getSupplierName());
        soDeliveryNoticeEntity.setWarehouseId(dto.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        soDeliveryNoticeEntity.setReceiverName(dto.getReceiverName());
        soDeliveryNoticeEntity.setTelNumber(dto.getTelNumber());
        soDeliveryNoticeEntity.setDeliveryModeDict(dto.getDeliveryModeDict());
        soDeliveryNoticeEntity.setReceiveAddress(dto.getReceiveAddress());
        boolean flag = this.saveOrUpdate(soDeliveryNoticeEntity);
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
        BeanMapperUtils.copy(soDeliveryNoticeEntity, viewDTO);
        //创库保存详情表的集合
        List<SoDeliveryNoticeDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.getDetailByMainId(id);
        SoInfoEntity soInfoEntity = soInfoService.getById(soDeliveryNoticeEntity.getSourceId());
        BeanMapperUtils.copy(soInfoEntity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
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
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
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
        operateLogService.batchAddModuleOperateLog("提交了一个收货单【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "提交操作");

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
        } else {
            //审核不通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(SoDeliveryNoticeEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个收货单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    @Override
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

        //下推入库单不能反审核
        deliveryNoticeEntityList.forEach(req -> {
            List<SoOutstockEntity> soOutstockEntityList = soOutstockService.getSoOutstockBySourceId(req.getId());
            if (CollectionUtils.isNotEmpty(soOutstockEntityList)) {
                throw new ServiceException(ApiError.ERROR_92004);
            }

        });
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个收货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(SoDeliveryNoticeDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<SoDeliveryNoticeDTO.GenerateSoDeliveryView> generateStockInView(List<String> ids) {
        return null;
    }

    @Override
    public Boolean generateSoDeliverySave(List<SoDeliveryNoticeDTO.GenerateSoDeliveryView> list) {
        return null;
    }
}
