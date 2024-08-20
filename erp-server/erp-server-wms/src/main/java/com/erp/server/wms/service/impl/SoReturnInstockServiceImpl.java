package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.wms.mapper.SoReturnInstockMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 退货入库单
 *
 * @author Luo_WG
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
    private MachineInfoService machineInfoService;

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
    private QcInfoService qcInfoService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private SyncKingdeeSoReturnService syncKingdeeSoReturnService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInStockService;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private AbstractWdtService abstractWdtService;

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

        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntitiesSourceIds = soReturnReceiveDetailService.listDetailByMainIds(returnMainIds);
        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));


                obj.setType(BillTypeEnum.getName(obj.getType()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                obj.setReturnTypeDict(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                if (StringUtils.isNotBlank(soDetailEntity.getMainId())) {
                    //获取退货数量
                    Integer returnQty = returnDetailEntityList.stream().filter(req -> req.getId().equals(obj.getSoReturnDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setMustQty(returnQty);
                    Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setDeliveryQty(actualQty);
                    Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> obj.getSourceDetailId().equals(req.getSourceDetailId()) && req.getSkuId().equals(obj.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setReceiveQty(receiveQty);
                } else {
                    Integer receiveQty = soReturnReceiveDetailEntitiesSourceIds.stream().filter(req -> obj.getSourceDetailId().equals(req.getId()) && req.getSkuId().equals(obj.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setReceiveQty(receiveQty);
                }

                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                if(StringUtils.isNotBlank(customerInfoEntity.getName())){
                    obj.setCustomerName(customerInfoEntity.getName());
                }
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnInstockDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SoReturnChangeListTypeEnum[] values = SoReturnChangeListTypeEnum.values();
        List<SoReturnInstockDTO.StatusCountDTO> list = new ArrayList<>();
        for (SoReturnChangeListTypeEnum item : values) {
            SoReturnInstockDTO.PagingParam pagingParam = new SoReturnInstockDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            pagingParam.setInvalidStatus(Boolean.FALSE);
            SoReturnInstockDTO.StatusCountDTO resultDTO = new SoReturnInstockDTO.StatusCountDTO();
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
    public String add(SoReturnInstockDTO.Add dto) {
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSTH, BusinessNoTypeEnum.CODE_XSTH.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSTH);
        SoReturnInstockEntity entity = new SoReturnInstockEntity();

        //退货单id
        String soReturnId = dto.getSoReturnId();
        //当退货单不为空的时候
        if (StringUtils.isNotBlank(soReturnId)) {
            SoReturnEntity soReturn = soReturnFeign.getSoReturnById(soReturnId);
            //对应的就是销售订单id
            String soId = soReturn.getSourceId();
            if (StringUtils.isNotBlank(soId)) {
                SoInfoEntity soInfo = soInfoFeign.getSoInfoById(soId);
                if (!Objects.isNull(soInfo)) {
                    dto.setSellerId(soInfo.getSellerId());
                    dto.setCustomerId(soInfo.getCustomerId());
                    dto.setSalesDeptId(soInfo.getSalesDeptId());
                    dto.setSellerId(soInfo.getSellerId());
                    dto.setWarehouseId(soReturn.getWarehouseId());
                    dto.setSalesOrgId(soInfo.getSalesOrgId());
                    dto.setType(soInfo.getOrderType());
                    entity.setSoId(soInfo.getId());
                    entity.setSoCode(soInfo.getCode());
                }
            }
            if (!Objects.isNull(soReturn)) {
                dto.setSoReturnId(soReturnId);
                dto.setSoReturnCode(soReturn.getCode());
            }

        } else {
            if (StringUtils.isNotBlank(dto.getSourceId())) {
                SoReturnReceiveEntity soReturnReceiveEntity = soReturnReceiveService.getById(dto.getSourceId());
                dto.setSellerId(soReturnReceiveEntity.getSellerId());
                dto.setCustomerId(soReturnReceiveEntity.getCustomerId());
                dto.setSalesDeptId(soReturnReceiveEntity.getSalesDeptId());
                dto.setSellerId(soReturnReceiveEntity.getSellerId());
                dto.setSalesOrgId(soReturnReceiveEntity.getSalesOrgId());
                dto.setType(soReturnReceiveEntity.getType());
            }
        }

        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(dto.getSellerId(), dto.getWarehouseKeeperId()));
        //获取客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(Arrays.asList(dto.getCustomerId()));
        //获取部门信息
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(Arrays.asList(dto.getSalesDeptId()));
        //获取核算公司
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        //获取组织信息
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(dto.getSalesOrgId(), updateDTO.getOrgId()));
        entity.setType(dto.getType());
        entity.setSalesOrgId(dto.getSalesOrgId());
        String orgName = orgList.stream().filter(o -> StringUtils.isNotBlank(dto.getSalesOrgId()) && dto.getSalesOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
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
        entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        String warehouseKeeperUserName = userList.stream().filter(d -> d.getUserId().equals(dto.getWarehouseKeeperId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
        entity.setWarehouseKeeperName(warehouseKeeperUserName);
        entity.setInventoryOrgId(updateDTO.getOrgId());
        String warehouseOrgName = orgList.stream().filter(o -> updateDTO.getOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setInventoryOrgName(warehouseOrgName);

        entity.setSoReturnId(dto.getSoReturnId());
        entity.setSoReturnCode(dto.getSoReturnCode());
        entity.setSourceCode(dto.getSourceCode());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceId(dto.getSourceId());
        entity.setCode(code);
        entity.setBillDate(dto.getBillDate());
        this.save(entity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货入库单【%s】", code), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "新增操作");

        soReturnInstockDetailService.add(dto, entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnInstockDTO.Update dto) {
        String id = dto.getId();
        SoReturnInstockEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_99083);
        }
        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        String warehouseKeeperName = "";
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //获取用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId));
            if (CollectionUtils.isNotEmpty(userList)) {
                warehouseKeeperName = userList.get(0).getUserName();
            }
        }

        //获取核算公司
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(dto.getDetailList().stream().map(v->v.getWarehouseId()).collect(Collectors.toList()));
        WarehouseDTO.UpdateDTO updateDTO = CollectionUtils.isEmpty(warehouseList)?new WarehouseDTO.UpdateDTO():warehouseList.get(0);
        //获取组织信息
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(updateDTO.getOrgId()));
        entity.setWarehouseKeeperId(warehouseKeeperId);
        entity.setWarehouseKeeperName(warehouseKeeperName);
        entity.setInventoryOrgId(updateDTO.getOrgId());
        String warehouseOrgName = orgList.stream().filter(o -> updateDTO.getOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setInventoryOrgName(warehouseOrgName);
        entity.setBillDate(dto.getBillDate());
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

        BeanMapperUtils.copy(entity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnInstockDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> productDetailEntitys = plmTaskFeign.listSkuProductByIds(skuIdList);
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(Arrays.asList(entity.getSoReturnId()));
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setTypeName(BillTypeEnum.getName(viewDTO.getType()));
        viewDTO.setType(BillTypeEnum.getName(viewDTO.getType()));
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        viewDTO.setCustomerName(customerInfoEntity.getName());
        if (StringUtils.isNotBlank(entity.getSoReturnCode())) {
            viewDTO.setSourceCode(entity.getSoReturnCode());
        }
        //退货单id
        List<SoReturnEntity> returnEntityList = soReturnFeign.listByIds(Arrays.asList(viewDTO.getSoReturnId()));
        //销售单id
        List<String> soIds = returnEntityList.stream().map(SoReturnEntity::getSourceId).collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        //获取退货单id
        List<String> returnMainIds = returnEntityList.stream().map(SoReturnEntity::getId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(detailEntityList.stream().map(SoReturnInstockDetailEntity::getWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntitieList = soReturnReceiveDetailService.listDetailByMainIds(Arrays.asList(viewDTO.getSourceId()));
        for (SoReturnInstockDetailEntity detailEntity : detailEntityList) {
            SoReturnInstockDetailDTO.View detailView = new SoReturnInstockDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            //产品sku信息
            SkuVO productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailView.setProductName(productDetailEntity.getSkuName());
            detailView.setSpuNo(productDetailEntity.getSpuNo());
            detailView.setUnitName(productDetailEntity.getUnitName());
            detailView.setVariantProperty(productDetailEntity.getVariantProperty());
            detailView.setWarehouseLocation(detailEntity.getWarehouseLocation());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(detailEntity.getSoReturnDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            if (StringUtils.isNotBlank(viewDTO.getSoReturnId())) {
                //销售单信息
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                detailView.setSalesQty(soDetailEntity.getQty());
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSoId().equals(soDetailEntity.getMainId()) && detail.getSkuId().equals(detailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setDeliveryQty(actualQty);

                //获取退货数量
                Integer returnQty = returnDetailEntityList.stream().filter(req -> req.getId().equals(detailEntity.getSoReturnDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setMustQty(returnQty);
                Integer receiveQty = soReturnReceiveDetailEntitieList.stream().filter(req -> detailEntity.getSourceDetailId().equals(req.getId()) && req.getSkuId().equals(detailEntity.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setReceiveQty(receiveQty);
            } else {
                Integer receiveQty = soReturnReceiveDetailEntitieList.stream().filter(req -> detailEntity.getSourceDetailId().equals(req.getId()) && req.getSkuId().equals(detailEntity.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setReceiveQty(receiveQty);
            }

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
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream()
                    .filter(req -> req.getCode().equals(detailView.getWarehouseLocation()) && req.getWarehouseId().equals(detailView.getWarehouseId()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            detailView.setWarehouseLocationName(warehouseLocationEntity.getName());
            if(StringUtils.isBlank(detailView.getWarehouseId())){
                detailView.setWarehouseId(viewDTO.getWarehouseId());
                detailView.setWarehouseName(viewDTO.getWarehouseName());
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SoReturnInstockDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    public BatchResultDTO approve(SoReturnInstockEntity entity, String type, String comment, Boolean isNeedProcess) {
        //判断是否是审核中的状态
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnInstockEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnInstockEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnInstockEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoReturnInstockEntity::getId, entity.getId())
                    .update();

            //更新库存
            inventoryTransCore(Collections.singletonList(entity));

            //发送金蝶
            sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_APPROVE.getCode());
            this.syncToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
        }else {
            //审核不通过
            lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoReturnInstockEntity::getId, entity.getId())
                    .update();
        }

        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个销售退货通知单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoReturnInstockEntity entity, Boolean isPushKingDee) {
        //已审核支持反审核
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99003.msg);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoReturnInstockEntity::getId, entity.getId())
                .update();
        //回滚库存
        InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO(InventorySourceTypeEnum.SO_RETURN_INSTOCK, entity.getId());
        inventoryTransCoreService.unApprove(inventoryUnApproveDTO);
        //反审核发送金蝶
        if (isPushKingDee) {
            //发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        this.syncToWdt(entity,SyncOperateEnum.OPERATE_DISAPPROVE);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个销售退货通知单【%s】",entity.getCode()), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
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

        //发送金蝶
        sendPushTask(entityList,SyncOperateEnum.OPERATE_INVALID.getCode());
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
        //发送金蝶
        sendPushTask(entityList,SyncOperateEnum.OPERATE_DELETE.getCode());
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
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnMainIds);

        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntitiesSourceIds = soReturnReceiveDetailService.listDetailByMainIds(returnMainIds);
        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoReturnInstockDTO.PagingView obj : pagingViews) {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setReturnTypeDict(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
            obj.setType(BillTypeEnum.getName(obj.getType()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setSalesQty(soDetailEntity.getQty());
            if (StringUtils.isNotBlank(soDetailEntity.getMainId())) {
                //获取退货数量
                Integer returnQty = returnDetailEntityList.stream().filter(req -> req.getId().equals(obj.getSoReturnDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setMustQty(returnQty);
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setDeliveryQty(actualQty);
                Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> obj.getSourceDetailId().equals(req.getSourceDetailId()) && req.getSkuId().equals(obj.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
            } else {
                Integer receiveQty = soReturnReceiveDetailEntitiesSourceIds.stream().filter(req -> obj.getSourceDetailId().equals(req.getId()) && req.getSkuId().equals(obj.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
            }
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            if(StringUtils.isNotBlank(customerInfoEntity.getName())){
                obj.setCustomerName(customerInfoEntity.getName());
            }
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
    public Boolean qcGenerateSoReturnInstockSave(List<SoReturnInstockDTO.GenerateSoReturnInstockView> list) {
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
            SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailService.getById(qcInfoEntity.getSourceDetailId());
            SoReturnInstockDTO.Add dto = new SoReturnInstockDTO.Add();
            /*if (StringUtils.isBlank(receiveEntity.getSourceId())) {
                dto.setSourceCode(receiveEntity.getCode());
                dto.setSourceId(receiveEntity.getId());
                dto.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
            } else {
                dto.setSourceCode(receiveEntity.getSourceCode());
                dto.setSourceId(receiveEntity.getSourceId());
                dto.setSourceType(SourceTypeEnum.SO_RETURN.getCode());
            }*/
            dto.setSourceCode(receiveEntity.getCode());
            dto.setSourceId(receiveEntity.getId());
            dto.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
            dto.setSoReturnId(receiveEntity.getSourceId());
            dto.setSoReturnCode(receiveEntity.getSourceCode());
            dto.setCustomerId(receiveEntity.getCustomerId());
            dto.setSalesOrgId(receiveEntity.getSalesOrgId());
            dto.setSalesDeptId(receiveEntity.getSalesDeptId());
            dto.setSellerId(receiveEntity.getSellerId());
            dto.setWarehouseId(qcInfoEntity.getWarehouseId());
            dto.setWarehouseKeeperId(receiveEntity.getWarehouseKeeperId());
            dto.setType(receiveEntity.getType());
            List<SoReturnInstockDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnInstockDTO.GenerateSoReturnInstockView view : viewList) {
                dto.setBillDate(view.getBillDate());
                SoReturnInstockDetailDTO.Add detailAddDTO = new SoReturnInstockDetailDTO.Add();
                detailAddDTO.setSkuId(view.getSkuId());
                detailAddDTO.setRealQty(view.getRealQty());
                detailAddDTO.setReceiveQty(view.getReceiveQty());
                detailAddDTO.setWarehouseId(qcInfoEntity.getWarehouseId());
                //退货类型
                detailAddDTO.setReturnTypeDict(view.getReturnTypeDict());
                //退货原因
                detailAddDTO.setReturnReasonDict(view.getReturnReasonDict());
                detailAddDTO.setWarehouseLocation(view.getWarehouseLocation());
                detailAddDTO.setRemark(view.getRemark());
                if (StringUtils.isBlank(receiveEntity.getSourceId())) {
                    detailAddDTO.setSourceDetailId(view.getSourceDetailId());
                } else {
                    detailAddDTO.setSourceDetailId(view.getSourceDetailId());
                    detailAddDTO.setSoReturnDetailId(soReturnReceiveDetailEntity.getSourceDetailId());
                }
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

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean receiveGenerateSoReturnInstockSave(List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> list) {
        Boolean flag = Boolean.FALSE;
        List<String> soReceiveIdList = list.stream().map(SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView::getMainId).distinct().collect(Collectors.toList());
        long count = soReturnReceiveDetailService.listByIds(soReceiveIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92032);
        }

        for (String id : soReceiveIdList) {
            List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            SoReturnReceiveEntity soReturnReceiveEntity = soReturnReceiveService.getById(id);
            SoReturnInstockDTO.Add dto = new SoReturnInstockDTO.Add();
            //等于空表示无退货单的下推
/*            if (StringUtils.isBlank(soReturnReceiveEntity.getSourceId())) {
                dto.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
                dto.setSourceCode(soReturnReceiveEntity.getCode());
                dto.setSourceId(id);
            } else {
                dto.setSourceType(SourceTypeEnum.SO_RETURN.getCode());
                dto.setSourceCode(soReturnReceiveEntity.getSourceCode());
                dto.setSourceId(soReturnReceiveEntity.getSourceId());
            }*/
            dto.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
            dto.setSourceCode(soReturnReceiveEntity.getCode());
            dto.setSourceId(id);
            dto.setSoReturnId(soReturnReceiveEntity.getSourceId());
            dto.setSoReturnCode(soReturnReceiveEntity.getSourceCode());
            dto.setCustomerId(soReturnReceiveEntity.getCustomerId());
            dto.setSalesOrgId(soReturnReceiveEntity.getSalesOrgId());
            dto.setSalesDeptId(soReturnReceiveEntity.getSalesDeptId());
            dto.setSellerId(soReturnReceiveEntity.getSellerId());
            dto.setWarehouseId(soReturnReceiveEntity.getWarehouseId());
            dto.setWarehouseKeeperId(soReturnReceiveEntity.getWarehouseKeeperId());
            dto.setType(soReturnReceiveEntity.getType());

            List<SoReturnInstockDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView view : viewList) {
                dto.setBillDate(view.getInstockDate());
                SoReturnInstockDetailDTO.Add detailAddDTO = new SoReturnInstockDetailDTO.Add();
                detailAddDTO.setSkuId(view.getSkuId());
                detailAddDTO.setReceiveQty(view.getReceiveQty());
                detailAddDTO.setRealQty(view.getRealQty());
                detailAddDTO.setWarehouseLocation(view.getWarehouseLocation());
                detailAddDTO.setRemark(view.getRemark());
                detailAddDTO.setWarehouseId(soReturnReceiveEntity.getWarehouseId());

                if (StringUtils.isBlank(soReturnReceiveEntity.getSourceId())) {
                    detailAddDTO.setSourceDetailId(view.getId());
                } else {
                    detailAddDTO.setSourceDetailId(view.getId());
                    detailAddDTO.setSoReturnDetailId(view.getSourceDetailId());
                }

                detailAddDTO.setReturnTypeDict(view.getReturnTypeDict());
                detailAddDTO.setReturnReasonDict(view.getReturnReasonDict());
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
     *
     * @param entityList
     * @return void
     * @Author Luo_WG
     * @Date 2023/5/24 11:25
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
                // 调整为入库日期 fix by zhangchunlin at 2023-07-17
                inOutStockDTO.setBillDate(entity.getBillDate());
                inOutStockDTO.setSkuId(detailEntity.getSkuId());
                inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
                inOutStockDTO.setQty(detailEntity.getRealQty());
                inOutStockDTO.setWarehouseId(detailEntity.getWarehouseId());
                inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
                inOutStockList.add(inOutStockDTO);
            }
            //添加冻结库存
            InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
            inventoryInOutStockDTO.setParamList(inOutStockList);
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_RETURN_INSTOCK.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
    }

    @Override
    public List<SoReturnInstockEntity> listByCode(List<String> codeList) {
        return lambdaQuery().in(SoReturnInstockEntity::getCode, codeList).list();
    }

    @Override
    public List<SoReturnInstockEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(SoReturnInstockEntity::getSourceId, sourceIds).list();
    }


    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SoReturnInstockEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SoReturnInstockEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        return lambdaUpdate().set(SoReturnInstockEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnInstockEntity::getId, ids)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Boolean saveKingdeeSoReturn(SoReturnInstockEntity instockEntity, List<SoReturnInstockDetailEntity> detailEntityList, List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            //回滚库存
            InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_RETURN_INSTOCK, ids);
            inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
            this.deleteByIds(ids);
            soReturnInstockDetailService.delete(ids);
        }
        this.save(instockEntity);
        return soReturnInstockDetailService.saveBatch(detailEntityList);
    }

    @Override
    public PagingVO<SoReturnInstockDTO.PdaPagingView> PdaPaging(PagingDTO<SoReturnInstockDTO.PdaPagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        SoReturnInstockDTO.PdaPagingParam params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }
        IPage<SoReturnInstockDTO.PdaPagingView> pageData = this.baseMapper.pdaPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<SoReturnInstockDTO.PdaPagingView> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<SoReturnInstockDetailEntity> returnInstockDetailEntities = soReturnInstockDetailService.listDetailByMainIds(ids);
        for (SoReturnInstockDTO.PdaPagingView record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<SoReturnInstockDetailEntity> detailEntities = returnInstockDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<SoReturnInstockDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, SoReturnInstockDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnInstockDTO.PdaSoReturnInstockCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<SoReturnInstockDTO.PdaSoReturnInstockCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            SoReturnInstockDTO.PagingParam pagingParamDTO = new SoReturnInstockDTO.PagingParam();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            SoReturnInstockDTO.PdaSoReturnInstockCountDTO resultDTO = new SoReturnInstockDTO.PdaSoReturnInstockCountDTO();
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
    public String pdaAdd(SoReturnInstockDTO.Add dto) {
        if (StringUtils.isNotBlank(dto.getSoReturnId())) {
            dto.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
            //获取来源详情id
            List<String> detailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(detailIds);
            //如果没有退货单，获取收货单的来源
            if (ObjectUtils.isEmpty(soReturnDetailEntities)) {
                List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(detailIds);
                for (SoReturnInstockDetailDTO.Add addDetailDto : dto.getDetailList()) {
                    SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(addDetailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(soReturnReceiveDetailEntity)) {
                        addDetailDto.setSourceDetailId(soReturnReceiveDetailEntity.getId());
                        addDetailDto.setSoReturnDetailId(soReturnReceiveDetailEntity.getSourceDetailId());
                    }
                }
            }
        } else {
            dto.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
        }
        return this.add(dto);
    }

    @Override
    public Boolean pdaUpdate(SoReturnInstockDTO.Update dto) {
        if (StringUtils.isNotBlank(dto.getSoReturnId())) {
            //获取来源详情id
            List<String> detailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(detailIds);

            if (ObjectUtils.isEmpty(soReturnDetailEntities)) {
                List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(detailIds);
                for (SoReturnInstockDetailDTO.Update addDetailDto : dto.getDetailList()) {
                    SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(addDetailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(soReturnReceiveDetailEntity)) {
                        addDetailDto.setSourceDetailId(soReturnReceiveDetailEntity.getId());
                        addDetailDto.setSoReturnDetailId(soReturnReceiveDetailEntity.getSourceDetailId());
                    }
                }
            }
        }
        return this.update(dto);
    }

    @Override
    public List<SoReturnInstockDTO.ViewGenerateMachineInfoDTO> viewGenerateMachineInfo(List<String> ids) {
        List<SoReturnInstockDetailEntity> list = soReturnInstockDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<SoReturnInstockDetailEntity> viewList = list.stream().filter(obj -> obj.getIsSubContract()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(viewList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<String> mainIds = viewList.stream().map(SoReturnInstockDetailEntity::getMainId).collect(Collectors.toList());
        List<SoReturnInstockEntity> mainList = this.listByIds(mainIds);
        if (CollectionUtils.isEmpty(mainList)) {
            throw new ServiceException(ApiError.ERROR_99083);
        }

        List<String> skuIds = viewList.stream().map(SoReturnInstockDetailEntity::getSkuId).collect(Collectors.toList());
        //bom信息
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        //sku信息
        List<String> allSkuIdList = bomList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(allSkuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = viewList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(paramList);

        //根据sku、仓库、仓位合并显示
        List<SoReturnInstockDTO.ViewGenerateMachineInfoDTO> resultList = new ArrayList<>();
        for (SoReturnInstockEntity entity : mainList) {
            List<SoReturnInstockDetailEntity> detailEntityList = viewList.stream().filter(v->v.getMainId().equals(entity.getId())).collect(Collectors.toList());
            Map<String, List<SoReturnInstockDetailEntity>> detailMap = detailEntityList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId() == null?"":obj.getWarehouseId()).concat(obj.getWarehouseLocation())));

            for (Map.Entry<String, List<SoReturnInstockDetailEntity>> entry : detailMap.entrySet()) {
                SoReturnInstockDetailEntity detailEntity = entry.getValue().get(0);
                SoReturnInstockDTO.ViewGenerateMachineInfoDTO viewDTO = new SoReturnInstockDTO.ViewGenerateMachineInfoDTO();

                if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
                    throw new ServiceException(ApiError.ERROR_SO_RETURN_INSTOCK_NOT_GENERATE,entity.getCode());
                }
                //事务类型默认拆卸
                viewDTO.setWorkType(WorkTypeEnum.DISASSEMBLE.getCode());
                viewDTO.setSkuId(detailEntity.getSkuId());
                viewDTO.setId(entity.getId());
                viewDTO.setCode(entity.getCode());
                viewDTO.setSkuNo(detailEntity.getSkuNo());
                viewDTO.setWarehouseId(detailEntity.getWarehouseId());
                viewDTO.setWarehouseName(detailEntity.getWarehouseName());
                viewDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
                if (CollectionUtils.isNotEmpty(warehouseLocationList)) {
                    String warehouseLocationName = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(viewDTO.getWarehouseId()) && obj.getCode().equals(viewDTO.getWarehouseLocation())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    viewDTO.setWarehouseLocationName(warehouseLocationName);
                }
                //产品信息
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                viewDTO.setProductName(skuVO.getSkuName());
                viewDTO.setVariantProperty(skuVO.getVariantProperty());

                //即时库存
                Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getWarehouseId(), viewDTO.getSkuId(), viewDTO.getWarehouseLocation());

                List<BomChildrenSkuDTO> childList = bomList.stream().filter(obj -> obj.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childList)) {
                    throw new ServiceException(ApiError.ERROR_95166);
                }
                viewDTO.setBomVersion(childList.get(0).getBomVersion());
                viewDTO.setCurInventoryQty(curInventoryQty);
                viewDTO.setQty(curInventoryQty);
                viewDTO.setChildLength(childList.size());
                Boolean childHidden = false;
                //显示按明细维度显示数据
                for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                    //产品信息
                    SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(childrenSkuDTO.getSkuId())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(childSkuVO)) {
                        throw new ServiceException(ApiError.ERROR_95084);
                    }
                    SoReturnInstockDTO.ViewGenerateMachineInfoDTO viewChildDTO = new SoReturnInstockDTO.ViewGenerateMachineInfoDTO();
                    BeanMapperUtils.copy(viewDTO,viewChildDTO);
                    if (!childHidden) {
                        childHidden = Boolean.TRUE;
                        viewChildDTO.setChildHidden(childHidden);
                    }
                    viewChildDTO.setChildSkuId(childrenSkuDTO.getSkuId());
                    viewChildDTO.setChildSkuNo(childrenSkuDTO.getSkuNo());
                    viewChildDTO.setQuantity(childrenSkuDTO.getQuantity());
                    viewChildDTO.setChildQty(curInventoryQty * childrenSkuDTO.getQuantity());
                    //默认退供应商
                    viewChildDTO.setHandleType(MachineHandleTypeEnum.RETURN_SUPPLIER.getCode());
                    viewChildDTO.setChildWarehouseId(viewDTO.getWarehouseId());
                    viewChildDTO.setChildWarehouseLocation(viewDTO.getWarehouseLocation());
                    viewChildDTO.setChildWarehouseLocationName(viewDTO.getWarehouseLocationName());
                    viewChildDTO.setChildSupplierId(childSkuVO.getSupplierId());
                    resultList.add(viewChildDTO);
                }

            }
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean generateMachineInfo(ValidList<SoReturnInstockDTO.GenerateMachineInfoDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        /**
         * 1、一个单据生成一个加工单
         * 2、同仓库、sku、仓位生成一个加工单明细
         */
        List<String> skuIds = list.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getChildSkuId())).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //bom信息
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        List<String> ids = new ArrayList<>();

        Map<String, List<SoReturnInstockDTO.GenerateMachineInfoDTO>> map = list.getList().stream().collect(Collectors.groupingBy(SoReturnInstockDTO.GenerateMachineInfoDTO::getId));
        for (Map.Entry<String, List<SoReturnInstockDTO.GenerateMachineInfoDTO>> entry : map.entrySet()) {
            List<SoReturnInstockDTO.GenerateMachineInfoDTO> value = entry.getValue();
            MachineInfoDTO.AddDTO addDTO = new MachineInfoDTO.AddDTO();
            addDTO.setBillDate(LocalDate.now());
            //事务类型默认拆卸
            addDTO.setWorkType(WorkTypeEnum.DISASSEMBLE.getCode());
            addDTO.setWarehouseId(value.get(0).getWarehouseId());
            addDTO.setType(MachineTypeEnum.OUTSOURCING.getCode());
            addDTO.setSourceType(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
            addDTO.setSourceId(entry.getKey());
            addDTO.setSourceCode(value.get(0).getCode());
            List<MachineDetailDTO.AddDTO> addDetailList = new ArrayList<>();

            Map<String, List<SoReturnInstockDTO.GenerateMachineInfoDTO>> detailMap = value.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseLocation())));
            for (Map.Entry<String, List<SoReturnInstockDTO.GenerateMachineInfoDTO>> detailEntry : detailMap.entrySet()) {
                List<SoReturnInstockDTO.GenerateMachineInfoDTO> detailValue = detailEntry.getValue();
                MachineDetailDTO.AddDTO addDetailDTO = new MachineDetailDTO.AddDTO();
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailValue.get(0).getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                addDetailDTO.setSkuId(detailValue.get(0).getSkuId());
                addDetailDTO.setSkuNo(skuVO.getSkuNo());
                addDetailDTO.setWarehouseLocation(detailValue.get(0).getWarehouseLocation());
                addDetailDTO.setQty(detailValue.get(0).getQty());
                addDetailDTO.setReferenceVersion(detailValue.get(0).getBomVersion());
                List<MachineSubComponentsDTO.AddDTO> subComponentsList = new ArrayList<>();
                for (SoReturnInstockDTO.GenerateMachineInfoDTO subComponentsDTO : detailValue) {
                    MachineSubComponentsDTO.AddDTO addSubComponentsDTO = new MachineSubComponentsDTO.AddDTO();
                    //产品信息
                    SkuVO child = skuList.stream().filter(obj -> obj.getSkuId().equals(subComponentsDTO.getChildSkuId())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(child)) {
                        throw new ServiceException(ApiError.ERROR_95084);
                    }
                    //BOM信息
                    BomChildrenSkuDTO bomChildrenSkuDTO = bomList.stream().filter(obj -> obj.getParentSkuId().equals(subComponentsDTO.getSkuId())
                                    && obj.getSkuId().equals(subComponentsDTO.getChildSkuId())
                                    && obj.getBomVersion().equals(subComponentsDTO.getBomVersion()))
                            .findFirst().orElse(null);

                    if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                        throw new ServiceException(ApiError.ERROR_95166);
                    }
                    addSubComponentsDTO.setSkuId(subComponentsDTO.getChildSkuId());
                    addSubComponentsDTO.setSkuNo(child.getSkuNo());
                    addSubComponentsDTO.setWarehouseId(subComponentsDTO.getWarehouseId());
                    addSubComponentsDTO.setWarehouseLocation(subComponentsDTO.getWarehouseLocation());
                    addSubComponentsDTO.setQty(addDetailDTO.getQty() * bomChildrenSkuDTO.getQuantity());
                    //子SKU处理
                    MachineSubComponentsDTO.HandleDetailDTO handleDetailDTO = new MachineSubComponentsDTO.HandleDetailDTO();
                    BeanMapperUtils.copy(subComponentsDTO,handleDetailDTO);
                    addSubComponentsDTO.setHandleType(subComponentsDTO.getHandleType());
                    addSubComponentsDTO.setHandleDetail(JSONUtil.toJsonStr(handleDetailDTO));
                    subComponentsList.add(addSubComponentsDTO);
                }
                addDetailDTO.setSubComponentsList(subComponentsList);
                addDetailList.add(addDetailDTO);
            }
            addDTO.setDetailList(addDetailList);
            String id = machineInfoService.add(addDTO);
            ids.add(id);
        }

        //自动提交
        Boolean submit = machineInfoService.submit(ids);
        if (!submit) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        //自动审核
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MachineInfoEntity> entityList = machineInfoService.listByIds(ids);
        for (String id : ids) {
            MachineInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.approve(entity,ApproveType.PASS,"",null));
            }catch (Exception e){
                log.error("加工单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        //自动生成直接调拨单或者采购退货单
        generateSubordinateOrder(ids);
        return Boolean.TRUE;
    }

    /**
     * @description: 生成下级单据
     * @author Will
     * @date: 2023/8/28 16:50
     * @param ids
     */
    private void generateSubordinateOrder (List<String> ids) {

        List<MachineInfoEntity> list = machineInfoService.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        List<MachineDetailEntity> detailList = machineDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        List<String> detailIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> subComponentsList = machineSubComponentsService.listByDetailIds(detailIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99056);
        }
        for (MachineInfoEntity entity : list) {
            //明细
            List<MachineDetailEntity> detailEntityList = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_99053);
            }
            //子件
            List<String> detailIdList = detailEntityList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
            List<MachineSubComponentsEntity> subList = subComponentsList.stream().filter(obj -> detailIdList.contains(obj.getDetailId())).collect(Collectors.toList());

            Map<String, List<MachineSubComponentsEntity>> map = subList.stream().collect(Collectors.groupingBy(MachineSubComponentsEntity::getHandleType));
            for (Map.Entry<String, List<MachineSubComponentsEntity>> entry : map.entrySet()) {
                String handleType = entry.getKey();
                List<MachineSubComponentsEntity> value = entry.getValue();
                if (MachineHandleTypeEnum.RETURN_SUPPLIER.getCode().equals(handleType)) {
                    //退供应商类型，同仓库、供应商生成采购退货单
                    generatePoReturnOrder(entity,value);


                }
                if (MachineHandleTypeEnum.MOVE_WAREHOUSE.getCode().equals(handleType)) {
                    //移仓，同调入、调出库存组织生成直接调拨单
                    generateTransferInfo(entity,value);
                }
            }
        }

    }
    /**
     * @description: 生成直接调拨单
     * @author Will
     * @date: 2023/8/28 17:36
     * @param entity
     * @param list
     */
    private void generateTransferInfo(MachineInfoEntity entity,List<MachineSubComponentsEntity> list) {
        /**
         * 移仓，同调入、调出库存组织生成直接调拨单
         */
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> warehouseIds = list.stream().map(obj -> JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseIds)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        /**
         * 1、同一加工单下，相同调入、调出组织（仓库、库位可不同）数据生成同一个调拨单
         * 2、基于1条件下，相同sku、调入、调出仓库和库位则可合并明细
         */

        //加工单同一单库存组织相同，根据调出组织分组
        Map<String, List<MachineSubComponentsEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> warehouseList.stream().filter(e -> e.getId().equals(JSONUtil.toBean(obj.getHandleDetail(),
                        MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseId()))
                .findFirst().flatMap(e -> Optional.ofNullable(e.getOrgId())).orElse("")));
        List<String> ids = new ArrayList<>();
        for (Map.Entry<String, List<MachineSubComponentsEntity>> entry : map.entrySet()) {
            List<MachineSubComponentsEntity> value = entry.getValue();
            //调入仓库组织
            MachineSubComponentsDTO.HandleDetailDTO handleDetailDTO = JSONUtil.toBean(value.get(0).getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class);
            String orgId = warehouseList.stream().filter(obj -> obj.getId().equals(handleDetailDTO.getChildWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getOrgId())).orElse("");
            TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
            addDTO.setBillDate(LocalDate.now());
            addDTO.setOutOrgId(entity.getInventoryOrgId());
            addDTO.setInOrgId(orgId);
            addDTO.setType(StrUtil.equals(addDTO.getInOrgId(),addDTO.getOutOrgId()) ? TransferTypeEnum.IN_ORG.getCode() : TransferTypeEnum.CROSS_ORG.getCode());
            addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
            addDTO.setRemark(StrUtil.format("加工单（拆卸）【{}】自动生成直接调拨单",entity.getCode()));
            //sku、仓库、仓位分组
            Map<String, List<MachineSubComponentsEntity>> childMap = value.stream()
                            .collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId().concat(StringUtils.isNotBlank(obj.getWarehouseLocation()) ? obj.getWarehouseLocation() : "")
                            .concat(StringUtils.isNotBlank(JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseLocation()) ? JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseLocation() : "" ))));

            List<TransferInfoDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for ( Map.Entry<String, List<MachineSubComponentsEntity>> childEntry : childMap.entrySet()) {
                List<MachineSubComponentsEntity> childValue = childEntry.getValue();
                MachineSubComponentsEntity subComponentsEntity = childValue.get(0);
                //子件处理详情
                MachineSubComponentsDTO.HandleDetailDTO subHandleDetailDTO = JSONUtil.toBean(subComponentsEntity.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class);
                TransferInfoDetailDTO.AddDTO addDetailDTO = new TransferInfoDetailDTO.AddDTO();
                BeanMapperUtils.copy(subComponentsEntity,addDetailDTO);
                //相同仓库无需生成直接调拨单
                if (subComponentsEntity.getWarehouseId().equals(subHandleDetailDTO.getChildWarehouseId())) {
                    continue;
                }
                addDetailDTO.setOutWarehouseId(subComponentsEntity.getWarehouseId());
                addDetailDTO.setOutWarehouseLocation(subComponentsEntity.getWarehouseLocation());
                addDetailDTO.setInWarehouseId(subHandleDetailDTO.getChildWarehouseId());
                addDetailDTO.setInWarehouseLocation(subHandleDetailDTO.getChildWarehouseLocation());
                //数量
                Integer qty = childValue.stream().map(MachineSubComponentsEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                addDetailDTO.setQty(qty);
                //来源单据明细id
                String sourceIds = childValue.stream().map(MachineSubComponentsEntity::getId).collect(Collectors.joining(","));
                addDetailDTO.setSourceDetailId(sourceIds);
                addDetailList.add(addDetailDTO);
            }
            //存在明细则新增
            if (CollectionUtils.isNotEmpty(addDetailList)) {
                addDTO.setDetailList(addDetailList);
                String id = transferInfoService.add(addDTO);
                ids.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            //提交
            transferInfoService.submit(ids);
        }
    }

    /**
     * @description: 生成采购退货单
     * @author Will
     * @date: 2023/8/28 17:36
     * @param entity
     * @param list
     */
    private void generatePoReturnOrder (MachineInfoEntity entity,List<MachineSubComponentsEntity> list) {
        /**
         * 退供应商类型，同仓库、供应商生成采购退货单
         */
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        /**
         * 1、同一加工单下，相同仓库、供应商数据生成同一个采购退货单
         * 2、基于1条件下，相同sku、库位则可合并明细
         */

        Map<String, List<MachineSubComponentsEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getWarehouseId().concat(JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildSupplierId())));
        for (Map.Entry<String, List<MachineSubComponentsEntity>> entry : map.entrySet()) {
            List<MachineSubComponentsEntity> value = entry.getValue();
            MachineSubComponentsDTO.HandleDetailDTO handleDetailDTO = JSONUtil.toBean(value.get(0).getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class);
            PurchaseReturnOrderDTO.AddDTO addDTO = new PurchaseReturnOrderDTO.AddDTO();
            addDTO.setBillDate(LocalDate.now());
            addDTO.setReturnMode(ReturnModeEnum.DEDUCTION.getCode());
            addDTO.setReturnOrgId(entity.getInventoryOrgId());
            addDTO.setReturnWarehouseId(value.get(0).getWarehouseId());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
            addDTO.setSupplierId(handleDetailDTO.getChildSupplierId());
            addDTO.setReturnUserId(userInfo.getUid());
            addDTO.setReturnRemark(StrUtil.format("加工单（拆卸）【{}】自动生成采购退货单",entity.getCode()));
            Map<String, List<MachineSubComponentsEntity>> childMap = value.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(StringUtils.isNotBlank(obj.getWarehouseLocation()) ? obj.getWarehouseLocation() : "" )));
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (Map.Entry<String, List<MachineSubComponentsEntity>> childEntry : childMap.entrySet()) {
                List<MachineSubComponentsEntity> childValue = childEntry.getValue();
                MachineSubComponentsEntity subComponentsEntity = childValue.get(0);
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO ();
                addDetailDTO.setSkuId(subComponentsEntity.getSkuId());
                addDetailDTO.setSkuNo(subComponentsEntity.getSkuNo());
                addDetailDTO.setWarehouseLocation(subComponentsEntity.getWarehouseLocation());
                //数量
                Integer qty = childValue.stream().map(MachineSubComponentsEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                addDetailDTO.setReturnQty(qty);
                addDetailDTO.setDeductAmountQty(qty);
                //来源单据明细id
                String sourceIds = childValue.stream().map(MachineSubComponentsEntity::getId).collect(Collectors.joining(","));
                addDetailDTO.setSourceDetailId(sourceIds);

                //由于下推的采购退货单无采购组织，现退货单价给0，编辑的时候取报价信息
                addDetailDTO.setReturnPrice(BigDecimal.ZERO);
                addDetailList.add(addDetailDTO);

            }
            addDTO.setPurchasePriceDetailList(addDetailList);
            poReturnService.add(addDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaAddAndSubmit(SoReturnInstockDTO.Add dto) {
        String id = this.pdaAdd(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaUpdateAndSubmit(SoReturnInstockDTO.Update dto) {
        Boolean update = this.pdaUpdate(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public SoReturnInstockDTO.View pdaView(String id) {
        SoReturnInstockDTO.View view = this.view(id);
        if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(view.getSourceType())) {
            List<SoReturnReceiveEntity> soReturnReceiveEntities = soReturnReceiveService.listByIds(Arrays.asList(view.getSourceId()));
            if (CollectionUtils.isNotEmpty(soReturnReceiveEntities)) {
                SoReturnReceiveEntity soReturnReceiveEntity = soReturnReceiveEntities.stream().filter(req -> req.getId().equals(view.getSourceId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soReturnReceiveEntity)) {
                    view.setSourceId(soReturnReceiveEntity.getId());
                    view.setSourceCode(soReturnReceiveEntity.getCode());
                }
            }
        }
        return view;
    }
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<SoReturnInstockEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoReturnService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    private void syncToWdt(SoReturnInstockEntity entity,SyncOperateEnum operateEnum) {

        List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainId(entity.getId());

        Set<String> warehouseIdSet = detailEntityList.stream().map(SoReturnInstockDetailEntity::getWarehouseId).collect(Collectors.toSet());
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        Map<String, String> thirdWarehouseMap = mappingList.stream().collect(Collectors.toMap(ThirdMappingDTO.WarehouseMappingDTO::getSysWarehouseId, ThirdMappingDTO.WarehouseMappingDTO::getThirdWarehouseCode));
        //过滤掉没有第三方仓库映射的明细
        detailEntityList = detailEntityList.stream().filter(v->thirdWarehouseMap.containsKey(v.getWarehouseId())).collect(Collectors.toList());
        Map<String, List<SoReturnInstockDetailEntity>> collectByWarehouseId = detailEntityList.stream().collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getWarehouseId));
        for (Map.Entry<String, List<SoReturnInstockDetailEntity>> entry : collectByWarehouseId.entrySet()) {
            String warehouseId = entry.getKey();
            if(SyncOperateEnum.OPERATE_APPROVE.equals(operateEnum)){
                List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
                for (SoReturnInstockDetailEntity detailEntity : entry.getValue()) {
                    CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
                    inGoods.setSpecNo(detailEntity.getSkuNo());
                    inGoods.setNum(BigDecimal.valueOf(detailEntity.getRealQty()));
                    inGoods.setPositionNo(detailEntity.getWarehouseLocation());
                    inGoods.setWarehouseId(warehouseId);
                    inGoodsList.add(inGoods);
                }
                abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
            }
            if(SyncOperateEnum.OPERATE_DISAPPROVE.equals(operateEnum)){
                List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
                for (SoReturnInstockDetailEntity detailEntity : entry.getValue()) {
                    CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
                    outGoods.setSpecNo(detailEntity.getSkuNo());
                    outGoods.setNum(BigDecimal.valueOf(detailEntity.getRealQty()));
                    outGoods.setPositionNo(detailEntity.getWarehouseLocation());
                    outGoods.setWarehouseId(warehouseId);
                    outGoodsList.add(outGoods);
                }
                abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), outGoodsList, SourceTypeEnum.OTHER_OUTSTOCK);
            }
        }
    }

    /**
     * 生成旺店通中间表数据
     */
    private DmpPushWdtDTO.AddDTO generateWdtStockInInterim(String sourceId, String sourceCode, String operateCode, String warehouseId, String thirdCode, String thirdWarehouseCode, List<? extends CommonCreateBillGoodsReq> inGoods, SourceTypeEnum sourceTypeEnum) {
        DmpPushWdtDTO.AddDTO pushWdtDTO = new DmpPushWdtDTO.AddDTO();
        pushWdtDTO.setSourceId(sourceId);
        pushWdtDTO.setSourceCode(sourceCode);
        pushWdtDTO.setThirdCode(thirdCode);
        pushWdtDTO.setWarehouseId(warehouseId);
        pushWdtDTO.setThirdWarehouseCode(thirdWarehouseCode);
        pushWdtDTO.setThirdType(sourceTypeEnum.getCode());
        pushWdtDTO.setOperateType(operateCode);
        List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(inGoods, DmpPushWdtDetailDTO.class);
        pushWdtDTO.setDetailDTOList(detailDTOList);
        return pushWdtDTO;
    }
}
