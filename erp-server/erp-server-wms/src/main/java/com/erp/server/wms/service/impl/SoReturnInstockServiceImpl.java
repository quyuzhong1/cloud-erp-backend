package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.SOReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoReturnInstockMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退货入库单
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnInstockServiceImpl extends SuperServiceImpl<SoReturnInstockMapper, SoReturnInstockEntity> implements SoReturnInstockService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Override
    public PagingVO<SoReturnInstockDTO.PagingView> paging(PagingDTO<SoReturnInstockDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnInstockDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnInstockDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnInstockDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = records.stream().map(SoReturnInstockDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnMainIds);

        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setType(null);
                    obj.setCustomerName(null);
                    obj.setInventoryOrgName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setReturnTypeDict(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                obj.setType(BillTypeEnum.getName(obj.getType()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                Integer returnQty = soReturnReceiveDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(obj.getSourceDetailId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoReturnReceiveDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(obj.getSourceDetailId()) && detail.getSkuId().equals(obj.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
                obj.setMustQty(returnQty);
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setDeliveryQty(actualQty);
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnInstockDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SOReturnChangeListTypeEnum[] values = SOReturnChangeListTypeEnum.values();
        List<SoReturnInstockDTO.StatusCountDTO> list = new ArrayList<>();
        for (SOReturnChangeListTypeEnum item : values) {
            SoReturnInstockDTO.PagingParam pagingParam = new SoReturnInstockDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            SoReturnInstockDTO.StatusCountDTO resultDTO = new SoReturnInstockDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (SOReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SOReturnChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SOReturnChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
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
    public String add(SoReturnInstockDTO.Add dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        SoReturnInstockEntity entity = new SoReturnInstockEntity();
        entity.setType(soInfoEntity.getOrderType());
        entity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        entity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        entity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                entity.setSalesDeptName(dept.getName());
            }
        }
        entity.setSoId(soInfoEntity.getId());
        entity.setSoCode(soInfoEntity.getCode());
        entity.setSellerId(soInfoEntity.getSellerId());
        entity.setSellerName(soInfoEntity.getSellerName());
        entity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        entity.setInventoryOrgId(soInfoEntity.getWarehouseOrgId());
        entity.setInventoryOrgName(soInfoEntity.getWarehouseOrgName());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THTZ, BusinessNoTypeEnum.CODE_THTZ.getCode()));
        entity.setCode(code);
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setBillDate(dto.getBillDate());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        entity.setWarehouseName(warehouseEntity.getName());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            //获取用户信息
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        this.save(entity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货入库单【%s】", code), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "新增操作");

        soReturnInstockDetailService.add(dto, entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnInstockDTO.Update dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        SoReturnInstockEntity entity = new SoReturnInstockEntity();
        entity.setType(soInfoEntity.getOrderType());
        entity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        entity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        entity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                entity.setSalesDeptName(dept.getName());
            }
        }
        entity.setSoId(soInfoEntity.getId());
        entity.setSoCode(soInfoEntity.getCode());
        entity.setSellerId(soInfoEntity.getSellerId());
        entity.setSellerName(soInfoEntity.getSellerName());
        entity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        entity.setInventoryOrgId(soInfoEntity.getWarehouseOrgId());
        entity.setInventoryOrgName(soInfoEntity.getWarehouseOrgName());
        entity.setId(dto.getId());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setBillDate(dto.getBillDate());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        entity.setWarehouseName(warehouseEntity.getName());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            //获取用户信息
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        //操作日志
        SoReturnInstockEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, entity, ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "", "");

        boolean flag = this.updateById(entity);

        soReturnInstockDetailService.update(dto);
        return flag;
    }

    @Override
    public SoReturnInstockDTO.View view(String id) {
        SoReturnInstockDTO.View viewDTO = new SoReturnInstockDTO.View();
        SoReturnInstockEntity entity = this.getById(id);
        //创库保存详情表的集合
        List<SoReturnInstockDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainId(id);
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(entity.getSourceId());
        BeanMapperUtils.copy(soReturnEntity, viewDTO);
        BeanMapperUtils.copy(entity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnInstockDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(Arrays.asList(entity.getSourceId()));
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        viewDTO.setCustomerName(customerInfoEntity.getName());
        //退货单id
        List<SoReturnEntity> returnEntityList = soReturnFeign.listByIds(Arrays.asList(viewDTO.getSourceId()));
        //销售单id
        List<String> soIds = returnEntityList.stream().map(SoReturnEntity::getSourceId).collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        //获取退货单id
        List<String> returnMainIds = returnEntityList.stream().map(SoReturnEntity::getId).distinct().collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnMainIds);

        for (SoReturnInstockDetailEntity detailEntity : detailEntityList) {
            SoReturnInstockDetailDTO.View detailView = new SoReturnInstockDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSoId().equals(soDetailEntity.getMainId()) && detail.getSkuId().equals(detailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setDeliveryQty(actualQty);
            //获取退货数量
            Integer returnQty = returnDetailEntityList.stream().filter(req -> req.getId().equals(detailEntity.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setMustQty(returnQty);
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> detailEntity.getSourceDetailId().equals(req.getSourceDetailId()) && req.getSkuId().equals(detailEntity.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setReceiveQty(receiveQty);
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoReturnInstockEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoReturnInstockDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoReturnInstockDTO.Update dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //判断是否是审核中的状态
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(baseApproveParamDTO.getType())) {
            LoginUser userInfo = commonService.getUserInfo();
            //审核通过
            lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnInstockEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnInstockEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnInstockEntity::getApproveTime, LocalDateTime.now())
                    .in(SoReturnInstockEntity::getId, ids)
                    .update();

            //更新库存
            inventoryTransCore(entityList);
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(SoReturnInstockEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售退货通知单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //已审核支持反审核
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        //TODO 待加审核流程

        //已下推质检单，不能反审核
        List<QcInfoEntity> qcBySourceId = qcInfoService.listQCBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(qcBySourceId)) {
            throw new ServiceException(ApiError.ERROR_99042);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnInstockEntity::getId, ids)
                .update();

        //回滚库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_RETURN_INSTOCK, ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnInstockEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】撤销流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "撤销流程操作");

        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核不通过 待提交可以作废
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnInstockEntity::getInvalidRemark, remark)
                .in(SoReturnInstockEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //待提交支持删除
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除详情表
        soReturnInstockDetailService.delete(ids);
        boolean flag = this.removeByIds(ids);
        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoReturnInstockDTO.PagingParam dto, HttpServletResponse response) {
        List<SoReturnInstockDTO.PagingView> pagingViews = baseMapper.soReturnInstockExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoReturnInstockDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = pagingViews.stream().map(SoReturnInstockDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);

        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoReturnInstockDTO.PagingView pagingView : pagingViews) {
            pagingView.setApproveStatus(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            pagingView.setReturnTypeDict(ReturnTypeEnum.getName(pagingView.getReturnTypeDict()));
            pagingView.setType(ReturnTypeEnum.getName(pagingView.getType()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(pagingView.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            pagingView.setDeliveryQty(actualQty);
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soReturnInstock.xlsx";
        String name = "销售退货入库单";
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoReturnInstockSave(List<SoReturnInstockDTO.GenerateSoReturnInstockView> list) {
        Boolean flag = Boolean.FALSE;
        List<String> soReceiveIdList = list.stream().map(SoReturnInstockDTO.GenerateSoReturnInstockView::getMainId).distinct().collect(Collectors.toList());
        long count = soReturnReceiveDetailService.listByIds(soReceiveIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92032);
        }
        for (String id : soReceiveIdList) {
            List<SoReturnInstockDTO.GenerateSoReturnInstockView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            QcInfoEntity qcInfoEntity = qcInfoService.getById(id);
            SoReturnReceiveEntity receiveEntity = soReturnReceiveService.getById(qcInfoEntity.getSourceId());
            SoReturnInstockDTO.Add dto = new  SoReturnInstockDTO.Add();
            dto.setSourceId(id);
            dto.setSourceType(SourceTypeEnum.QC_BILL.getCode());
            dto.setWarehouseId(qcInfoEntity.getWarehouseId());
            dto.setWarehouseKeeperId(receiveEntity.getWarehouseKeeperId());
            List<SoReturnInstockDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnInstockDTO.GenerateSoReturnInstockView view : viewList) {
                SoReturnInstockDetailDTO.Add detailAddDTO = new SoReturnInstockDetailDTO.Add();
                detailAddDTO.setRealQty(view.getRealQty());
                detailAddDTO.setReturnTypeDict(view.getReturnTypeDict());
                detailAddDTO.setReturnReasonDict(view.getReturnReasonDict());
                detailAddDTO.setWarehouseLocation(view.getWarehouseLocation());
                detailAddDTO.setRemark(view.getRemark());
                detailAddDTO.setSourceDetailId(view.getId());
                detailList.add(detailAddDTO);
            }
            dto.setDetailList(detailList);
            String noticeId = this.add(dto);
            if (StringUtils.isNotBlank(noticeId)) {
                flag = Boolean.TRUE;
            }
        }
        return flag;
    }

    /**
     * 更新库存
     * @Author Luo_WG
     * @Date 2023/5/24 11:25
     * @param entityList
     * @return void
     **/
    private void inventoryTransCore(List<SoReturnInstockEntity> entityList) {
        for (SoReturnInstockEntity entity : entityList) {
            List<InOutStockDTO> inOutStockList = new ArrayList<>();
            List<SoReturnInstockDetailEntity> returnInstockDetailEntities = soReturnInstockDetailService.listDetailByMainId(entity.getId());
            for (SoReturnInstockDetailEntity detailEntity : returnInstockDetailEntities) {
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.SO_RETURN_INSTOCK);
                inOutStockDTO.setSourceId(entity.getId());
                inOutStockDTO.setSourceCode(entity.getCode());
                inOutStockDTO.setSourceDetailId(detailEntity.getId());
                inOutStockDTO.setBillDate(LocalDate.now());
                inOutStockDTO.setSkuId(detailEntity.getSkuId());
                inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
                inOutStockDTO.setQty(detailEntity.getRealQty());
                inOutStockDTO.setWarehouseId(entity.getWarehouseId());
                inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
                inOutStockList.add(inOutStockDTO);
            }
            //添加冻结库存
            InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
            inventoryInOutStockDTO.setMembers(inOutStockList);
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_RETURN_INSTOCK.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
    }
}
