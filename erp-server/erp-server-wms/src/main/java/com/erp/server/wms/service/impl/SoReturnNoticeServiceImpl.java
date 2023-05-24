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
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SOReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoReturnNoticeMapper;
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
 * 退货通知单
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnNoticeServiceImpl extends SuperServiceImpl<SoReturnNoticeMapper, SoReturnNoticeEntity> implements SoReturnNoticeService {
    @Resource
    private SoReturnNoticeDetailService soReturnNoticeDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private WarehouseService warehouseService;

    /**
     * 根据退货单获取销售单已出库数量
     * @Author Luo_WG
     * @Date 2023/5/19 12:21
     * @param returnId
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     **/
    private  List<SoOutstockDetailEntity> listSoOutstockByReturnId(String returnId) {
        //获取退货详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainId(returnId);
        //获取销售订单明细表id
        List<String> soDetailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //根据销售单详情id获取发货通知单详情信息
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIds);
        //获取发货通知单明细表id
        List<String> deliveryNoticeDetailIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        soDetailIds.addAll(deliveryNoticeDetailIdList);
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(soDetailIds);
        return soOutstockDetailEntities;
    }

    @Override
    public PagingVO<SoReturnNoticeDTO.PagingView> paging(PagingDTO<SoReturnNoticeDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnNoticeDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnNoticeDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = records.stream().map(SoReturnNoticeDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);

        //根据销售单详情id获取发货通知单详情信息
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(detailIds);
        //获取发货通知单明细表id
        List<String> deliveryNoticeDetailIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        detailIds.addAll(deliveryNoticeDetailIdList);
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(detailIds);

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setCustomerName(null);
                    obj.setInventoryOrgName(null);
                    obj.setApproveStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setInvalidStatus(null);
                }
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setReturnTypeDictName(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(obj.getSourceDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setDeliveryQty(actualQty);
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SOReturnChangeListTypeEnum[] values = SOReturnChangeListTypeEnum.values();
        List<SoReturnNoticeDTO.StatusCountDTO> list = new ArrayList<>();
        for (SOReturnChangeListTypeEnum item : values) {
            SoReturnNoticeDTO.PagingParam pagingParam = new SoReturnNoticeDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            SoReturnNoticeDTO.StatusCountDTO resultDTO = new SoReturnNoticeDTO.StatusCountDTO();
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
    public String add(SoReturnNoticeDTO.Add dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());
        SoReturnNoticeEntity entity = new SoReturnNoticeEntity();
        //获取用户信息
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(userDTO.getUserId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        entity.setType(soInfoEntity.getOrderType().getCode());
        entity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        entity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        entity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                entity.setSalesDeptName(dept.getName());
            }
        }
        entity.setSellerId(soInfoEntity.getSellerId());
        entity.setSellerName(soInfoEntity.getSellerName());
        entity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        entity.setWarehouseId(soInfoEntity.getWarehouseId());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(soInfoEntity.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            entity.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }

        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THTZ, BusinessNoTypeEnum.CODE_THTZ.getCode()));
        entity.setCode(code);
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            //获取用户信息
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        this.save(entity);
        soReturnNoticeDetailService.add(dto, entity.getId());
        return soReturnEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnNoticeDTO.Update dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());
        SoReturnNoticeEntity entity = new SoReturnNoticeEntity();
        //获取用户信息
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(userDTO.getUserId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        entity.setType(soInfoEntity.getOrderType().getCode());
        entity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        entity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        entity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                entity.setSalesDeptName(dept.getName());
            }
        }
        entity.setSellerId(soInfoEntity.getSellerId());
        entity.setSellerName(soInfoEntity.getSellerName());
        entity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        entity.setWarehouseId(soInfoEntity.getWarehouseId());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(soInfoEntity.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            entity.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        entity.setId(dto.getId());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        }
        boolean flag = this.updateById(entity);
        //操作日志
        SoReturnNoticeEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, entity, ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "", "");

        soReturnNoticeDetailService.update(dto);
        return flag;
    }

    @Override
    public SoReturnNoticeDTO.View view(String id) {
        SoReturnNoticeDTO.View viewDTO = new SoReturnNoticeDTO.View();
        SoReturnNoticeEntity entity = this.getById(id);
        //创库保存详情表的集合
        List<SoReturnNoticeDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnNoticeDetailEntity> detailEntityList = soReturnNoticeDetailService.listDetailByMainId(id);
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(entity.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        BeanMapperUtils.copy(soReturnEntity, viewDTO);
        BeanMapperUtils.copy(entity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnNoticeDetailEntity::getSkuId).collect(Collectors.toList());
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
        viewDTO.setBillDate(soReturnEntity.getBillDate());
        viewDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(soInfoEntity.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            viewDTO.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        for (SoReturnNoticeDetailEntity detailEntity : detailEntityList) {
            SoReturnNoticeDetailDTO.View detailView = new SoReturnNoticeDetailDTO.View();
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
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        List<SoReturnNoticeEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoReturnNoticeEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoReturnNoticeDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoReturnNoticeDTO.Update dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<SoReturnNoticeEntity> entityList = this.listByIds(ids);
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
            lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnNoticeEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnNoticeEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnNoticeEntity::getApproveTime, LocalDateTime.now())
                    .in(SoReturnNoticeEntity::getId, ids)
                    .update();
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(SoReturnNoticeEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售退货通知单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<SoReturnNoticeEntity> entityList = this.listByIds(ids);
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

        //下推签收单不能反审核
        List<SoReturnReceiveEntity> soReturnNoticeEntities = soReturnReceiveService.listBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(soReturnNoticeEntities)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnNoticeEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoReturnNoticeEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnNoticeEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】撤销流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "撤销流程操作");

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoReturnNoticeEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnNoticeEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnNoticeEntity::getInvalidRemark, remark)
                .in(SoReturnNoticeEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoReturnNoticeEntity> entityList = this.listByIds(ids);
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
        soReturnNoticeDetailService.delete(ids);
        boolean flag = this.removeByIds(ids);
        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoReturnNoticeDTO.PagingParam dto, HttpServletResponse response) {
        List<SoReturnNoticeDTO.PagingView> pagingViews = baseMapper.soReturnNoticeExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoReturnNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = pagingViews.stream().map(SoReturnNoticeDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);

        //根据销售单详情id获取发货通知单详情信息
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(detailIds);
        //获取发货通知单明细表id
        List<String> deliveryNoticeDetailIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        detailIds.addAll(deliveryNoticeDetailIdList);
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(detailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();

        for (SoReturnNoticeDTO.PagingView pagingView : pagingViews) {
            pagingView.setApproveStatus(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(pagingView.getSourceDetailId()) && pagingView.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            pagingView.setDeliveryQty(actualQty);
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soReturnNoticeExport.xlsx";
        String name = "销售退货通知单";
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoReturnNoticeSave(List<SoReturnDTO.GenerateSoReturnNoticeView> list) {
        Boolean flag = Boolean.TRUE;
        List<String> soReturnIdList = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getMainId).distinct().collect(Collectors.toList());
        List<String> soDetailIdList = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getSourceDetailId).distinct().collect(Collectors.toList());
        long count = soReturnFeign.listByIds(soReturnIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92014);
        }
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIdList);
        if (CollectionUtils.isEmpty(soDetailEntities)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }

        for (String id : soReturnIdList) {
            List<SoReturnDTO.GenerateSoReturnNoticeView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            String sourceId = viewList.stream().filter(req -> req.getMainId().equals(id)).map(SoReturnDTO.GenerateSoReturnNoticeView::getSourceId).distinct().findFirst().orElse("");
            SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(sourceId);
            SoReturnNoticeDTO.Add dto = new SoReturnNoticeDTO.Add();
            dto.setSourceId(id);
            dto.setSourceType(SourceTypeEnum.SO_RETURN.getCode());
            dto.setInventoryOrgId(soInfoEntity.getWarehouseOrgId());
            List<SoReturnNoticeDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnDTO.GenerateSoReturnNoticeView view : viewList) {
                SoReturnNoticeDetailDTO.Add detailAddDTO = new SoReturnNoticeDetailDTO.Add();
                detailAddDTO.setReturnQty(view.getReturnQty());
                detailAddDTO.setReturnReasonDict(view.getReturnReasonDict());
                detailAddDTO.setReturnTypeDict(view.getReturnTypeDict());
                detailAddDTO.setRemark(view.getRemark());
                detailAddDTO.setSourceDetailId(view.getId());
                detailList.add(detailAddDTO);
            }
            dto.setDetailList(detailList);
            String noticeId = this.add(dto);
            if (StringUtils.isBlank(noticeId)) {
                flag = Boolean.FALSE;
            }
        }
        return flag;
    }

    @Override
    public List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> generateSoDeliveryView(List<String> ids) {
        List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> list = baseMapper.generateSoDeliveryView(ids);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (SoReturnNoticeDTO.GenerateSoReturnReceiveView view : list) {
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(view.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            view.setSalesQty(soDetailEntity.getQty());
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
        }
        return list;
    }

    @Override
    public List<SoReturnNoticeEntity> listBySourceId(List<String> sourceIds) {
        return lambdaQuery().in(SoReturnNoticeEntity::getSourceId, sourceIds).list();
    }
}
