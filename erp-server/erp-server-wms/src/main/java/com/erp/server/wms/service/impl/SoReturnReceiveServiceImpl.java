package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.SoB2cReturnReasonEnum;
import com.erp.model.oms.enums.SoB2cReturnTypeEnum;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoReturnReceiveMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_RETURN_RECEIVE;

/**
 * 采购退货签收单
 * @author Luo_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveServiceImpl extends SuperServiceImpl<SoReturnReceiveMapper, SoReturnReceiveEntity> implements SoReturnReceiveService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SoReturnNoticeService soReturnNoticeService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private QcRuleService qcRuleService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<SoReturnReceiveDTO.PagingView> paging(PagingDTO<SoReturnReceiveDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        if (CollectionUtils.isNotEmpty(pagingParamDTO.getParams().getApproveStatusList())) {
            pagingParamDTO.getParams().setInvalidStatus(Boolean.FALSE);
        }
        IPage<SoReturnReceiveDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnReceiveDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnReceiveDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = records.stream().map(SoReturnReceiveDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,detailIds);
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                if(obj.getType().equals("B2C")){
                    obj.setReturnTypeDictName(SoB2cReturnTypeEnum.getName(obj.getReturnTypeDict()));
                    SoB2cReturnDetailEntity soB2cReturnDetailEntity = soB2cReturnDetailEntityList.stream().filter(v->v.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoB2cReturnDetailEntity());
                    obj.setSalesQty(soB2cReturnDetailEntity.getSaleQty());
                }else{
                    SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
                    SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                    obj.setSalesQty(soDetailEntity.getQty());
                    obj.setReturnTypeDictName(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                }
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnReceiveDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SoReturnChangeListTypeEnum[] values = SoReturnChangeListTypeEnum.values();
        List<SoReturnReceiveDTO.StatusCountDTO> list = new ArrayList<>();
        for (SoReturnChangeListTypeEnum item : values) {
            SoReturnReceiveDTO.PagingParam pagingParam = new SoReturnReceiveDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            pagingParam.setInvalidStatus(Boolean.FALSE);
            SoReturnReceiveDTO.StatusCountDTO resultDTO = new SoReturnReceiveDTO.StatusCountDTO();
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
    public String add(SoReturnReceiveDTO.Add dto) {
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THQS, BusinessNoTypeEnum.CODE_THQS.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_THQS);
        //获取组织信息
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(dto.getSalesOrgId()));
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(dto.getSellerId(), dto.getWarehouseKeeperId()));
        //获取客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(Arrays.asList(dto.getCustomerId()));
        //获取部门信息
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(Arrays.asList(dto.getSalesDeptId()));
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());

        SoReturnReceiveEntity entity = new SoReturnReceiveEntity();
        entity.setType(dto.getType());
        entity.setSalesOrgId(dto.getSalesOrgId());
        String orgName = orgList.stream().filter(o -> dto.getSalesOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setSalesOrgName(orgName);
        entity.setSalesDeptId(dto.getSalesDeptId());
        String deptName = departmentList.stream().filter(o -> dto.getSalesDeptId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setSalesDeptName(deptName);
        entity.setSellerId(dto.getSellerId());
        String userName = userList.stream().filter(d -> d.getUserId().equals(dto.getSellerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
        entity.setSellerName(userName);
        entity.setCustomerId(dto.getCustomerId());
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(dto.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        entity.setReturnDate(dto.getReturnDate());
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        //如果有退货订单号
        if (StringUtils.isNotBlank(dto.getSourceId())) {
            if(entity.getType().equals("B2C")){
                SoB2cReturnEntity soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class,dto.getSourceId());
                if(Objects.nonNull(soB2cReturnEntity)){
                    entity.setSourceCode(soB2cReturnEntity.getCode());
                    entity.setSoCode(soB2cReturnEntity.getSoCode());
                    entity.setSoId(soB2cReturnEntity.getSoId());
                }
            }else{
                //获取退货单信息
                SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
                if (ObjectUtil.isEmpty(soReturnEntity)) {
                    throw new ServiceException(ApiError.ERROR_92023);
                }
                //获取销售单信息
                SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
                entity.setSourceCode(soReturnEntity.getCode());
                entity.setSoCode(soInfoEntity.getCode());
                entity.setSoId(soInfoEntity.getId());
            }
        }

        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            entity.setWarehouseId(warehouseList.get(MathUtil.ZERO).getId());
            entity.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        entity.setCode(code);
        entity.setSourceId(dto.getSourceId());
        entity.setSourceType(dto.getSourceType());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        entity.setBillDate(dto.getBillDate());
        String warehouseKeeperUserName = userList.stream().filter(d -> d.getUserId().equals(dto.getWarehouseKeeperId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
        entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        entity.setWarehouseKeeperName(warehouseKeeperUserName);
        this.save(entity);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货签收单【%s】", code), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), entity.getId(), "新增操作");

        soReturnReceiveDetailService.add(dto, entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        //获取组织信息
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(dto.getSalesOrgId()));
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(dto.getSellerId(), dto.getWarehouseKeeperId()));
        //获取客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(Arrays.asList(dto.getCustomerId()));
        //获取部门信息
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(Arrays.asList(dto.getSalesDeptId()));
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());

        SoReturnReceiveEntity entity = new SoReturnReceiveEntity();
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setSalesOrgId(dto.getSalesOrgId());
        String orgName = orgList.stream().filter(o -> dto.getSalesOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setSalesOrgName(orgName);
        entity.setSalesDeptId(dto.getSalesDeptId());
        String deptName = departmentList.stream().filter(o -> dto.getSalesDeptId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setSalesDeptName(deptName);
        entity.setSellerId(dto.getSellerId());
        String userName = userList.stream().filter(d -> d.getUserId().equals(dto.getSellerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
        entity.setSellerName(userName);
        entity.setCustomerId(dto.getCustomerId());
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(dto.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        entity.setReturnDate(dto.getReturnDate());
        if(!entity.getReturnLogisticCode().equals(dto.getReturnLogisticCode())){
            operateLogService.addModuleOperateLog(StrUtil.format("退货物流单号从{}修改为{}",entity.getReturnLogisticCode(),dto.getReturnLogisticCode()), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), entity.getId(), "编辑");
        }
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        //如果有退货订单号
        if (StringUtils.isNotBlank(dto.getSourceId())) {
            //获取退货单信息
            SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
            if (ObjectUtil.isEmpty(soReturnEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
            //获取销售单信息
            SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
/*            entity.setType(soReturnEntity.getType());
            entity.setSalesOrgId(soReturnEntity.getSalesOrgId());
            entity.setSalesOrgName(soReturnEntity.getSalesOrgName());
            entity.setSalesDeptId(soReturnEntity.getSalesDeptId());
            if (StringUtils.isNotBlank(soReturnEntity.getSalesDeptId())) {
                SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soReturnEntity.getSalesDeptId());
                if (dept != null) {
                    entity.setSalesDeptName(dept.getName());
                }
            }
            entity.setSellerId(soReturnEntity.getSellerId());
            entity.setSellerName(soReturnEntity.getSellerName());
            entity.setCustomerId(soReturnEntity.getCustomerId());
            entity.setCustomerName(customerInfoEntity.getName());
            entity.setReturnDate(soReturnEntity.getBillDate());*/
            entity.setSourceCode(soReturnEntity.getCode());
            entity.setSoCode(soInfoEntity.getCode());
            entity.setSoId(soInfoEntity.getId());
        }

        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            entity.setWarehouseId(warehouseList.get(MathUtil.ZERO).getId());
            entity.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
        }
        entity.setSourceId(dto.getSourceId());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        entity.setBillDate(dto.getBillDate());
        String warehouseKeeperUserName = userList.stream().filter(d -> d.getUserId().equals(dto.getWarehouseKeeperId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
        entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        entity.setWarehouseKeeperName(warehouseKeeperUserName);
        //操作日志
        SoReturnReceiveEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, entity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), entity.getId(), "", "");

        boolean save = this.updateById(entity);

        soReturnReceiveDetailService.update(dto);
        return save;
    }

    @Override
    public SoReturnReceiveDTO.View view(String id) {
        SoReturnReceiveDTO.View viewDTO = new SoReturnReceiveDTO.View();
        SoReturnReceiveEntity entity = this.getById(id);
        //创库保存详情表的集合
        List<SoReturnReceiveDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnReceiveDetailEntity> detailEntityList = soReturnReceiveDetailService.listDetailByMainId(id);
        BeanMapperUtils.copy(entity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnReceiveDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIdList);
        //退货单详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(Arrays.asList(entity.getSourceId()));
        List<String> sourceDetailIds = detailEntityList.stream().map(SoReturnReceiveDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,sourceDetailIds);
        SoB2cReturnEntity soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class,entity.getSourceId());
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setTypeName(BillTypeEnum.getName(viewDTO.getType()));
        for (SoReturnReceiveDetailEntity detailEntity : detailEntityList) {
            SoReturnReceiveDetailDTO.View detailView = new SoReturnReceiveDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            //产品sku信息
            SkuVO productDetailEntity = skuInfoByIds.stream().filter(entityClass -> entityClass.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailView.setProductName(productDetailEntity.getSkuName());
            detailView.setVariantProperty(productDetailEntity.getVariantProperty());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());

            if(entity.getType().equals("B2C")){
                SoB2cReturnDetailEntity soB2cReturnDetailEntity = soB2cReturnDetailEntityList.stream().filter(v->v.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoB2cReturnDetailEntity());
                detailView.setSalesQty(soB2cReturnDetailEntity.getSaleQty());
                if(Objects.nonNull(soB2cReturnEntity)){
                    detailView.setReturnTypeDictName(SoB2cReturnTypeEnum.getName(soB2cReturnEntity.getType()));
                    detailView.setReturnReasonDictName(SoB2cReturnReasonEnum.getName(soB2cReturnEntity.getReason()));
                }
            }else{
                //销售单信息
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                detailView.setSalesQty(soDetailEntity.getQty());

                if (StringUtils.isNotBlank(detailEntity.getReturnTypeDict())) {
                    detailView.setReturnTypeDictName(ReturnTypeEnum.getName(detailEntity.getReturnTypeDict()));
                } else {
                    if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnTypeDict())) {
                        detailView.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));
                    }
                }
                if (StringUtils.isNotBlank(detailEntity.getReturnReasonDict())) {
                    detailView.setReturnReasonDictName(ReturnReasonEnum.getName(detailEntity.getReturnReasonDict()));
                } else {
                    if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnReasonDict())) {
                        detailView.setReturnReasonDictName(ReturnReasonEnum.getName(soReturnDetailEntity.getReturnReasonDict()));
                    }
                }
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
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        operateLogService.batchAddModuleOperateLog("提交了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoReturnReceiveEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoReturnReceiveDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoReturnReceiveDTO.Update dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoReturnReceiveEntity entity, String type, String comment, Boolean isNeedProcess) {
        //判断是否是审核中的状态
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnReceiveEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnReceiveEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnReceiveEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoReturnReceiveEntity::getId, entity.getId())
                    .update();

            //根据条件生成质检单
            createQcBill(Collections.singletonList(entity.getId()));
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoReturnReceiveEntity::getId, entity.getId())
                    .update();
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个销售退货通知单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 生成质检单
     * @Author Luo_WG
     * @Date 2023/8/2 17:07
     * @param ids
     * @return void
     **/
    private void createQcBill(List<String> ids) {
        List<QcInfoDTO.SoReturnReceiveToQcDTO> qcList = baseMapper.getQcList(ids);
        List<String> skuIds = qcList.stream().map(QcInfoDTO.SoReturnReceiveToQcDTO::getSkuId).collect(Collectors.toList());
        //获取到sku 信息
        List<ProductVO.ProductPackVO> skuList = plmTaskFeign.getProductPackBySkuIds(skuIds);
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
        String sourceType = SourceTypeEnum.SO_RETURN_RECEIVE.getCode();
        for (QcInfoDTO.SoReturnReceiveToQcDTO item : qcList) {
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

        }
        //添加质检单的
        List<QcInfoDTO.SoReturnReceiveToQcDTO> addList = new ArrayList<>(qcList.size());
        //获取到审核通过的 且启用的质检规则
        List<QcRuleEntity> qcRuleList = qcRuleService.listByApprove();
        //退货质检
        String returnQc = QcTypeEnum.RETURN_QC.getCode();
        List<String> productGradeList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(returnQc)).map(QcRuleEntity::getProductGradeKey).collect(Collectors.toList());
        String newProductGrade = String.join(",", productGradeList);

        //销售方式
        List<String> saleMethodList = qcRuleList.stream().filter(r -> r.getQcType().getCode().equals(returnQc)).map(QcRuleEntity::getSaleMethod).collect(Collectors.toList());
        String newSaleMethod = String.join(",", saleMethodList);

        for (QcInfoDTO.SoReturnReceiveToQcDTO newItem : qcList) {
            //为空所有的加，等级为空用销售方式，销售方式为空用等级
            if ((StringUtils.isNotBlank(newProductGrade) && StringUtils.isNotBlank(newSaleMethod))) {
                QcInfoDTO.SoReturnReceiveToQcDTO newQc = new QcInfoDTO.SoReturnReceiveToQcDTO();
                BeanMapper.copy(newItem, newQc);
                newQc.setQcType(returnQc);
                addList.add(newQc);
            } else if (StringUtils.isBlank(newProductGrade)) {
                if (StringUtils.isNotBlank(newItem.getSaleMethod())) {
                    String[] split = newItem.getSaleMethod().split(",");
                    for (String s : split) {
                        if (newSaleMethod.contains(s)) {
                            QcInfoDTO.SoReturnReceiveToQcDTO newQc = new QcInfoDTO.SoReturnReceiveToQcDTO();
                            BeanMapper.copy(newItem, newQc);
                            newQc.setQcType(returnQc);
                            addList.add(newQc);
                            break;
                        }
                    }
                }

            } else if (StringUtils.isBlank(newSaleMethod)) {
                if (StringUtils.isNotBlank(newItem.getProductGrade())) {
                    String[] split = newItem.getProductGrade().split(",");
                    for (String s : split) {
                        if (newProductGrade.contains(s)) {
                            QcInfoDTO.SoReturnReceiveToQcDTO newQc = new QcInfoDTO.SoReturnReceiveToQcDTO();
                            BeanMapper.copy(newItem, newQc);
                            newQc.setQcType(returnQc);
                            addList.add(newQc);
                            break;
                        }
                    }
                }
            } else {
                if (newProductGrade.contains(newItem.getProductGrade()) && newSaleMethod.contains(newItem.getSaleMethod())) {
                    QcInfoDTO.SoReturnReceiveToQcDTO newQc = new QcInfoDTO.SoReturnReceiveToQcDTO();
                    BeanMapper.copy(newItem, newQc);
                    newQc.setQcType(returnQc);
                    addList.add(newQc);
                }
            }
        }
        qcInfoService.autoSoReturnReceiveToQcDTO(addList);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoReturnReceiveEntity entity) {
        //已审核支持反审核
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99003.msg);
        }
        //下推质检单不能反审核
        List<QcInfoEntity> qcList = qcInfoService.listQCBySourceIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(qcList)) {
            String codes = qcList.stream().map(QcInfoEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_99042,codes);
        }
        //下推退货入库单不能反审核
        List<SoReturnInstockEntity> soReturnInstockEntityList = soReturnInstockService.listBySourceIds(Collections.singletonList(entity.getId())).stream().filter(req -> req.getInvalidStatus().equals(InvalidStatusEnum.NOT_VOIDED.getStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(soReturnInstockEntityList)) {
            String codes = soReturnInstockEntityList.stream().map(SoReturnInstockEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_RETURN_ORDER_PUSHED,codes);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoReturnReceiveEntity::getId, entity.getId())
                .update();
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个销售退货通知单【%s】",entity.getCode()), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnReceiveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("退货签收单【%s】撤销流程", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "撤销流程操作");

        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnReceiveEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnReceiveEntity::getInvalidRemark, remark)
                .in(SoReturnReceiveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个退货签收单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        soReturnReceiveDetailService.delete(ids);
        boolean flag = this.removeByIds(ids);
        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoReturnReceiveDTO.PagingParam dto) {
        downloadTaskFeign.saveDownloadTask("销售退货签收单", EXPORT_WMS_SO_RETURN_RECEIVE.getCode(), dto);
        return true;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoReturnReceiveSave(List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> list) {
        Boolean flag = Boolean.TRUE;
        List<String> soReturnNoticeIdList = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getMainId).distinct().collect(Collectors.toList());
        long count = soReturnNoticeService.listByIds(soReturnNoticeIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92021);
        }
        for (String id : soReturnNoticeIdList) {
            List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            SoReturnNoticeEntity noticeEntity = soReturnNoticeService.getById(id);
            SoReturnReceiveDTO.Add dto = new SoReturnReceiveDTO.Add();
            dto.setSourceType(SourceTypeEnum.SO_RETURN_NOTICE.getCode());
            dto.setWarehouseId(noticeEntity.getWarehouseId());
            dto.setInventoryOrgId(noticeEntity.getInventoryOrgId());
            dto.setWarehouseKeeperId(noticeEntity.getWarehouseKeeperId());
            dto.setReturnDate(noticeEntity.getBillDate());
            dto.setBillDate(LocalDate.now());
            dto.setType(noticeEntity.getType());
            dto.setCustomerId(noticeEntity.getCustomerId());
            dto.setSalesOrgId(noticeEntity.getSalesOrgId());
            dto.setSalesDeptId(noticeEntity.getSalesDeptId());
            dto.setSellerId(noticeEntity.getSellerId());
            dto.setReturnLogisticCode(viewList.get(0).getReturnLogisticCode());
            List<SoReturnReceiveDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnNoticeDTO.GenerateSoReturnReceiveView view : viewList) {
                dto.setSourceId(view.getSourceId());
                SoReturnReceiveDetailDTO.Add detailAddDTO = new SoReturnReceiveDetailDTO.Add();
                detailAddDTO.setSkuId(view.getSkuId());
                detailAddDTO.setSkuNo(view.getSkuNo());
                detailAddDTO.setReturnQty(view.getReturnQty());
                detailAddDTO.setReceiveQty(view.getReceiveQty());
                detailAddDTO.setRemark(view.getRemark());
                detailAddDTO.setSourceDetailId(view.getSourceDetailId());
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
    public List<QcInfoDTO.ReceiveGenerateQcView> receiveGenerateQcView(List<String> ids) {
        List<QcInfoDTO.ReceiveGenerateQcView> list = baseMapper.receiveGenerateQcView(ids);
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(approve)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        return list;
    }

    @Override
    public List<SoReturnReceiveEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery().eq(SoReturnReceiveEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoReturnReceiveEntity::getSourceId, ids).list();
    }

    @Override
    public List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> generateSoReturnInstockView(List<String> ids) {
        List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> list = baseMapper.generateSoReturnInstockView(ids);
        long receiveCount = list.stream().filter(req -> !req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).count();
        if (receiveCount > 0) {
            throw new ServiceException(ApiError.ERROR_99081);
        }

/*        List<QcInfoEntity> qcInfoEntities = qcInfoService.listQCBySourceIds(ids);
        long qcCount = qcInfoEntities.stream().filter(req -> QcBillStatusEnum.FINISH_QC.equals(req.getQcStatus()) || QcBillStatusEnum.EXEMPTION.equals(req.getQcStatus())).count();
        if (qcCount != qcInfoEntities.size()) {
            throw new ServiceException(ApiError.ERROR_99082);
        }*/

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        List<String> warehouseIds = list.stream().map(SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);

        //退货签收单明细表id
        List<String> receiveDetailIds = list.stream().map(SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView::getId).collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(receiveDetailIds);
        List<String> returnDetailIds = soReturnReceiveDetailEntities.stream().map(SoReturnReceiveDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByIds(returnDetailIds);
        //销售单明细id
        List<String> soDetailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
        //签收单id
        List<String> receiveIds = list.stream().map(SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView::getMainId).distinct().collect(Collectors.toList());
        List<SoReturnReceiveEntity> soReturnReceiveEntities = this.listByIds(receiveIds);
        List<String> returnIds = soReturnReceiveEntities.stream().map(SoReturnReceiveEntity::getSourceId).collect(Collectors.toList());
        List<SoReturnEntity> returnEntityList = soReturnFeign.listByIds(returnIds);
        //销售单id
        List<String> soIds = returnEntityList.stream().map(SoReturnEntity::getSourceId).collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        for (SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView view : list) {
            if (StringUtils.isNotBlank(view.getReturnTypeDict())) {
                view.setReturnTypeDictName(ReturnTypeEnum.getName(view.getReturnTypeDict()));
            }
            if (StringUtils.isNotBlank(view.getReturnReasonDict())) {
                view.setReturnReasonDictName(ReturnReasonEnum.getName(view.getReturnReasonDict()));
            }
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(view.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            view.setCustomerName(customerInfoEntity.getName());
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
            WarehouseDTO.UpdateDTO warehouseDto = warehouseList.stream().filter(req -> req.getId().equals(view.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            view.setWarehouseName(warehouseDto.getName());
            view.setInstockDate(LocalDate.now());
            SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(view.getId())).findFirst().orElse(new SoReturnReceiveDetailEntity());

            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(req -> req.getId().equals(soReturnReceiveDetailEntity.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(req -> req.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            view.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSoId().equals(soDetailEntity.getMainId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            view.setDeliveryQty(actualQty);
            view.setMustQty(soReturnReceiveDetailEntity.getReturnQty());
            view.setReceiveQty(soReturnReceiveDetailEntity.getReceiveQty());
            view.setRealQty(soReturnReceiveDetailEntity.getReceiveQty());
        }
        return list;
    }

    @Override
    public List<SoReturnReceiveEntity> listByIds(List<String> ids) {
        return lambdaQuery().in(SoReturnReceiveEntity::getId, ids).list();
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaDisApprove(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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

        //下推质检单不能反审核
        List<QcInfoEntity> qcList = qcInfoService.listQCBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(qcList)) {
            String codes = qcList.stream().map(QcInfoEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_99042,codes);
        }

        //下推退货入库单不能反审核
        List<SoReturnInstockEntity> soReturnInstockEntityList = soReturnInstockService.listBySourceIds(ids).stream().filter(req -> req.getInvalidStatus().equals(InvalidStatusEnum.NOT_VOIDED.getStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(soReturnInstockEntityList)) {
            String codes = soReturnInstockEntityList.stream().map(SoReturnInstockEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_RETURN_ORDER_PUSHED,codes);
        }

        //修改状态为待提交
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnReceiveEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), pairList, "反审核操作");

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoReturnReceiveDTO.PagingView> exportSoReturnReceive(PagingDTO<SoReturnReceiveDTO.PagingParam> dto) {

        Page<SoReturnReceiveDTO.PagingView> pagingViews = baseMapper.soReturnReceiveExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //获取sku的id集合
        List<String> skuIdList = pagingViews.getRecords().stream().map(SoReturnReceiveDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取退货单id
        List<String> returnMainIds = pagingViews.getRecords().stream().map(SoReturnReceiveDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        //退货单详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(returnMainIds);
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoReturnReceiveDTO.PagingView pagingView : pagingViews.getRecords()) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            pagingView.setReturnTypeDictName(ReturnTypeEnum.getName(pagingView.getReturnTypeDict()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
        }
        return new PagingVO<>(pagingViews);
    }

    @Override
    public PagingVO<SoReturnReceiveDTO.PdaPagingView> pdaPaging(PagingDTO<SoReturnReceiveDTO.PdaPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        SoReturnReceiveDTO.PdaPagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }
        IPage<SoReturnReceiveDTO.PdaPagingView> pageData = this.baseMapper.pdaPaging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<SoReturnReceiveDTO.PdaPagingView> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<SoReturnReceiveDetailEntity> detailEntityList = soReturnReceiveDetailService.listDetailByMainIds(ids);
        for (SoReturnReceiveDTO.PdaPagingView record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<SoReturnReceiveDetailEntity> detailEntities = detailEntityList.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<SoReturnReceiveDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, SoReturnReceiveDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnReceiveDTO.PdaSoReturnReceiveCount> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<SoReturnReceiveDTO.PdaSoReturnReceiveCount> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            SoReturnReceiveDTO.PagingParam pagingParamDTO = new SoReturnReceiveDTO.PagingParam();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            SoReturnReceiveDTO.PdaSoReturnReceiveCount resultDTO = new SoReturnReceiveDTO.PdaSoReturnReceiveCount();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public List<SoReturnReceiveDTO.PdaSoReceive> pdaList(SoReturnReceiveDTO.PdaSoReceiveParam dto) {
        List<SoReturnReceiveDTO.PdaSoReceive> list = baseMapper.pdaList(dto);

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> srrId = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> detailEntityList = soReturnReceiveDetailService.listDetailByMainIds(srrId);
        List<String> detailIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> returnInstockDetailEntities = soReturnInstockDetailService.listDetailBySourceDetailIds(detailIds);
        //获取未全部入库的销售退货签收单详情id
        List<String> soReturnReceiveDetailIds = new ArrayList<>();

        returnInstockDetailEntities.stream().collect(Collectors.groupingBy(n -> n.getSourceDetailId(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            int realQty = m.stream().mapToInt(SoReturnInstockDetailEntity::getRealQty).sum();
            SoReturnReceiveDetailEntity detailEntity = detailEntityList.stream().filter(req -> req.getId().equals(m.get(MathUtil.ZERO).getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(detailEntity)) {
                if (realQty < detailEntity.getReceiveQty()) {
                    soReturnReceiveDetailIds.add(m.get(MathUtil.ZERO).getSourceDetailId());
                }
            }
            return m;
        })));
        List<String> collect = returnInstockDetailEntities.stream().map(req -> req.getSourceDetailId()).distinct().collect(Collectors.toList());
        List<String> ids = detailIds.stream().filter(poid -> !collect.contains(poid)).collect(Collectors.toList());
        soReturnReceiveDetailIds.addAll(ids);

        if (CollectionUtils.isEmpty(soReturnReceiveDetailIds)) {
            return new ArrayList<>();
        }
        //根据未全部入库的销售退货签收单详情id获取签收单id
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listByIds(soReturnReceiveDetailIds);
        List<String> notAllReceivePoOrderId = soReturnReceiveDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        //获取到未全部入库的销售退货签收单返回数据
        List<SoReturnReceiveDTO.PdaSoReceive> soReceiveList = list.stream().filter(req -> notAllReceivePoOrderId.contains(req.getId())).collect(Collectors.toList());

        soReceiveList.sort(Comparator.comparing(SoReturnReceiveDTO.PdaSoReceive::getCode).reversed());
        soReceiveList.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return soReceiveList;
    }
}
