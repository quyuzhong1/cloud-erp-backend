package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoReturnNoticeMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_RETURN_NOTICE;

/**
 * 退货通知单
 * @author Luo_WG
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
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
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

        List<SoReturnEntity> returnEntities = soReturnFeign.listByIds(returnMainIds);
        List<String> soIds = returnEntities.stream().map(SoReturnEntity::getSourceId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(soReturnDetailEntity)) {
                    obj.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));

                    SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                    obj.setSalesQty(soDetailEntity.getQty());
                    Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setDeliveryQty(actualQty);
                }
                obj.setProductName(productDetailEntity.getName());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SoReturnChangeListTypeEnum[] values = SoReturnChangeListTypeEnum.values();
        List<SoReturnNoticeDTO.StatusCountDTO> list = new ArrayList<>();
        for (SoReturnChangeListTypeEnum item : values) {
            SoReturnNoticeDTO.PagingParam pagingParam = new SoReturnNoticeDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            SoReturnNoticeDTO.StatusCountDTO resultDTO = new SoReturnNoticeDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (SoReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SoReturnChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SoReturnChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
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
        entity.setSoId(soInfoEntity.getId());
        entity.setSoCode(soInfoEntity.getCode());
        //获取用户信息
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(userDTO.getUserId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        entity.setSoId(soInfoEntity.getId());
        entity.setSoCode(soInfoEntity.getCode());
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

        entity.setSellerId(soInfoEntity.getSellerId());
        entity.setSellerName(soInfoEntity.getSellerName());
        entity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(soInfoEntity.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            entity.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THTZ, BusinessNoTypeEnum.CODE_THTZ.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_THTZ);
        entity.setCode(code);
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(dto.getSourceType());
        entity.setBillDate(soReturnEntity.getBillDate());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        if (ObjectUtils.isNotEmpty(sysAccountingCompanyEntity)) {
            entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        }
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            //获取用户信息
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        if (ObjectUtil.isNotEmpty(warehouseEntity)) {
            entity.setWarehouseId(dto.getWarehouseId());
            entity.setWarehouseName(warehouseEntity.getName());
        }
        this.save(entity);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货通知单【%s】", code), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "新增操作");

        soReturnNoticeDetailService.add(dto, entity.getId());
        return entity.getId();
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
        entity.setSoId(soInfoEntity.getId());
        entity.setSoCode(soInfoEntity.getCode());
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
        entity.setSellerId(soInfoEntity.getSellerId());
        entity.setSellerName(soInfoEntity.getSellerName());
        entity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(soInfoEntity.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            entity.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        entity.setId(dto.getId());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setBillDate(soReturnEntity.getBillDate());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        if (ObjectUtils.isNotEmpty(sysAccountingCompanyEntity)) {
            entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        }
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        if (ObjectUtil.isNotEmpty(warehouseEntity)) {
            entity.setWarehouseId(dto.getWarehouseId());
            entity.setWarehouseName(warehouseEntity.getName());
        }
        //操作日志
        SoReturnNoticeEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, entity, ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "", "");

        boolean flag = this.updateById(entity);

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
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(entity.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            viewDTO.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        //获取退货单id
        List<SoReturnEntity> returnEntities = soReturnFeign.listByIds(Arrays.asList(entity.getSourceId()));
        List<String> soIds = returnEntities.stream().map(SoReturnEntity::getSourceId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        for (SoReturnNoticeDetailEntity detailEntity : detailEntityList) {
            SoReturnNoticeDetailDTO.View detailView = new SoReturnNoticeDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            detailView.setProductName(productDetailEntity.getName());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(detailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setDeliveryQty(actualQty);
            if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnTypeDict())) {
                detailView.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));
            }
            if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnReasonDict())) {
                detailView.setReturnReasonDictName(ReturnReasonEnum.getName(soReturnDetailEntity.getReturnReasonDict()));
            }
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoReturnNoticeEntity entity, String type, String comment, Boolean isNeedProcess) {
        //判断是否是审核中的状态
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnNoticeEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnNoticeEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnNoticeEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoReturnNoticeEntity::getId, entity.getId())
                    .update();
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoReturnNoticeEntity::getId, entity.getId())
                    .update();
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个销售退货通知单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoReturnNoticeEntity entity) {
        //已审核支持反审核
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99003.msg);
        }
        //TODO 待加审核流程

        //下推签收单不能反审核
        List<SoReturnReceiveEntity> soReturnNoticeEntities = soReturnReceiveService.listBySourceIds(Collections.singletonList(entity.getSourceId()));
        if (CollectionUtils.isNotEmpty(soReturnNoticeEntities)) {
            throw new ServiceException(ApiError.ERROR_99068);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoReturnNoticeEntity::getId, entity.getId())
                .update();

        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个销售退货通知单【%s】", entity.getCode()), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
    public Boolean exportExcel(SoReturnNoticeDTO.PagingParam dto) {
        downloadTaskFeign.saveDownloadTask("销售退货通知单", EXPORT_WMS_SO_RETURN_NOTICE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
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
            SoReturnNoticeDTO.Add dto = new SoReturnNoticeDTO.Add();
            dto.setSourceId(id);
            dto.setSourceType(SourceTypeEnum.SO_RETURN.getCode());
            List<SoReturnNoticeDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnDTO.GenerateSoReturnNoticeView view : viewList) {
                dto.setWarehouseId(view.getWarehouseId());
                dto.setInventoryOrgId(view.getInventoryOrgId());
                SoReturnNoticeDetailDTO.Add detailAddDTO = new SoReturnNoticeDetailDTO.Add();
                detailAddDTO.setReturnQty(view.getReturnQty());
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
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(approve)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        //获取退货单id
        List<String> returnIds = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getSourceId).collect(Collectors.toList());
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnIds);
        List<String> soDetailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnIds);
        for (SoReturnNoticeDTO.GenerateSoReturnReceiveView view : list) {
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(detail -> view.getSourceDetailId().equals(detail.getSourceDetailId()) && detail.getSkuId().equals(view.getSkuId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            //销售退货单
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(view.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            view.setSalesQty(soDetailEntity.getQty());
            view.setReceiveQty(view.getReturnQty() - receiveQty);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
            if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnTypeDict())) {
                view.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));
            }
            if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnReasonDict())) {
                view.setReturnReasonDictName(ReturnReasonEnum.getName(soReturnDetailEntity.getReturnReasonDict()));
            }
        }
        return list;
    }

    @Override
    public List<SoReturnNoticeEntity> listBySourceId(List<String> sourceIds) {
        return lambdaQuery().eq(SoReturnNoticeEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoReturnNoticeEntity::getSourceId, sourceIds).list();
    }

    @Override
    public PagingVO<SoReturnNoticeDTO.PagingView> exportSoReturnNotice(PagingDTO<SoReturnNoticeDTO.PagingParam> dto) {
        Page<SoReturnNoticeDTO.PagingView> pagingViews = baseMapper.soReturnNoticeExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //获取sku的id集合
        List<String> skuIdList = pagingViews.getRecords().stream().map(SoReturnNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = pagingViews.getRecords().stream().map(SoReturnNoticeDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);

        List<SoReturnEntity> returnEntities = soReturnFeign.listByIds(returnMainIds);
        List<String> soIds = returnEntities.stream().map(SoReturnEntity::getSourceId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();

        for (SoReturnNoticeDTO.PagingView obj : pagingViews.getRecords()) {
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            obj.setDeliveryQty(actualQty);
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            obj.setCustomerName(customerInfoEntity.getName());
            obj.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));
        }
        return new PagingVO<>(pagingViews);
    }
}
