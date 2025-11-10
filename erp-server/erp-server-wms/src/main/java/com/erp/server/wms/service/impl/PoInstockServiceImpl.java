package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.constant.UserStateConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.srm.feign.SrmPoReconciliationFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeStockInService;
import com.erp.server.wms.mapper.PoInstockMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PO_IN_STOCK;

/**
 * 采购入库单 服务实现类
 *
 * @author will
 * @since 2023-04-10
 */
@Slf4j
@Service
public class PoInstockServiceImpl extends SuperServiceImpl<PoInstockMapper, PoInstockEntity> implements PoInstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private SyncKingdeeStockInService syncKingdeeStockInService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private SubcontractIssueService subcontractIssueService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private AbstractWdtService abstractWdtService;

    @Resource
    private SrmPoReconciliationFeign srmPoReconciliationFeign;


    @Override
    public PagingVO<PoInstockDTO.ListDTO> paging(PagingDTO<PoInstockDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        if (CollectionUtils.isNotEmpty(pagingDTO.getParams().getApproveStatusList())) {
            pagingDTO.getParams().setInvalidStatus(Boolean.FALSE);
        }
        IPage<PoInstockDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<PoInstockDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandlePurchaseStockIn(records);
        return new PagingVO(pageData);
    }


    @Override
    public List<PoInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<PoInstockDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            PoInstockDTO.SearchParamDTO searchParamDTO = new PoInstockDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            PoInstockDTO.ListStatusCountDTO resultDTO = new PoInstockDTO.ListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            searchParamDTO.setInvalidStatus(Boolean.FALSE);
            if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public PoInstockEntity add(PoInstockDTO.AddDTO dto, Boolean isNotCheck) {
        PoInstockEntity entity = new PoInstockEntity();
        BeanMapperUtils.copy(dto, entity);
        //添加采购订单默认值
        addDefaultPurchaseData(dto.getPurchaseOrderId(), entity);
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(), dto.getStockInUserId(), dto.getDeliveryWarehouseId(), entity);
        log.info("采购入库单新增");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGRK, BusinessNoTypeEnum.CODE_CGRK.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGRK);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个采购入库单【%s】", code), ModuleTypeEnum.PO_INSTOCK.getCode(), entity.getId(), "新增操作");
            //新增明细
            poInstockDetailService.add(dto.getDetails(), entity.getId(), dto.getSourceType(), isNotCheck);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PoInstockDTO.UpdateDTO dto) {
        PoInstockEntity entity = new PoInstockEntity();
        BeanMapperUtils.copy(dto, entity);
        List<PoInstockDetailDTO.UpdateDTO> details = dto.getDetails();
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(), dto.getStockInUserId(), dto.getDeliveryWarehouseId(), entity);

        log.info("采购入库单修改，id=【{}】", dto.getId());

        //添加日志
        PoInstockEntity old = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PO_INSTOCK.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        poInstockDetailService.update(details, entity.getId(), old.getSourceType());
        return Boolean.TRUE;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public PoInstockEntity addAndSubmit(PoInstockDTO.AddDTO dto) {
        //新增
        PoInstockEntity entity = this.add(dto, Boolean.FALSE);

        //提交
        this.submit(Collections.singletonList(entity.getId()));
        return entity;
    }

    /**
     * 当质检单 质检类型为b2b 是
     * 批量生成入库单
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-24 15:08
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<PoInstockDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        for (PoInstockDTO.AddDTO item : list) {
            PoInstockEntity entity = new PoInstockEntity();
            BeanMapperUtils.copy(item, entity);
            entity.setApproveStatus(approveStatus);
            //添加采购订单默认值
            addDefaultPurchaseData(item.getPurchaseOrderId(), entity);
            //处理数据id
            doOpHandleDataId(item.getStockInDeptId(), item.getStockInUserId(), item.getDeliveryWarehouseId(), entity);
            log.info("采购入库单新增");
            //生成单号
//            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGRK, BusinessNoTypeEnum.CODE_CGRK.getCode()));
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGRK);
            entity.setCode(code);
            //新增主表数据
            boolean save = this.save(entity);
            if (save) {
                //操作日志
                operateLogService.addModuleOperateLog(String.format("新增了一个采购入库单【%s】", code), ModuleTypeEnum.PO_INSTOCK.getCode(), entity.getId(), "新增操作");
                //新增明细
                poInstockDetailService.add(item.getDetails(), entity.getId(), item.getSourceType(), Boolean.FALSE);
            }
        }

        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PoInstockDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Collections.singletonList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PoInstockEntity> list = getList(ids);
        submitList(ids, list);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitList(List<String> ids, List<PoInstockEntity> list) {
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //本次下推入库明细信息
        List<PoInstockDetailEntity> thisDetailList = poInstockDetailService.listByMainIds(ids);
        List<String> podIds = thisDetailList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());

        //存在收货单,且收货单下面的质检单未质检完成则不允许提交
        checkQcInfo(list,thisDetailList);
        //校验入库明细与收货单数量
        checkInstockDetail(list,thisDetailList);
        //已下推入库明细信息
        List<PoInstockDetailEntity> hasDetailList = poInstockDetailService.listDetailByPodIds(podIds);

        //查询退货明细
        List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);

        //采购订单数量校验
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isNotEmpty(purchaseOrderDetailList)) {
            for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : purchaseOrderDetailList) {

                //已入库数量
                Integer hasInstockQty = hasDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                //本次入库数量
                Integer thisInstockQty = thisDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                //入库明细
                String poInstockId = thisDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(PoInstockDetailEntity::getMainId).findFirst().orElse("");
                //采购单
                String poCode = list.stream().filter(obj -> obj.getId().equals(poInstockId)).map(PoInstockEntity::getPurchaseOrderCode).findFirst().orElse("");

                //退货单补货数量
                Integer returnQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                    returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
                }

                //入库完成
                if (purchaseOrderDetailEntity.getPurchaseQty() + returnQty == hasInstockQty.intValue()) {
                    throw new ServiceException(ApiError.ERROR_99075, poCode, purchaseOrderDetailEntity.getSkuNo());
                }
                //未入库完成，但剩余数量不够
                if (thisInstockQty > purchaseOrderDetailEntity.getPurchaseQty() - hasInstockQty.intValue() + returnQty) {
                    throw new ServiceException(ApiError.ERROR_99074, poCode, purchaseOrderDetailEntity.getSkuNo(), purchaseOrderDetailEntity.getPurchaseQty() - hasInstockQty.intValue() + returnQty);
                }
            }
        }

        log.info("采购入库单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个采购入库单【%s】", ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "提交操作");
    }

    private void checkInstockDetail(List<PoInstockEntity> list, List<PoInstockDetailEntity> thisDetailList) {
        if (CollectionUtils.isEmpty(thisDetailList) || CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> podIds = thisDetailList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<String> receiveIds = list.stream().map(PoInstockEntity::getSourceId).distinct().collect(Collectors.toList());
        //收货列表
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        //退货数量
//        List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
        //根据收货明细id获取退货列表
        List<WarehouseReceiveDTO.PoReturnDetailDTO> returnDetailDTOS = poReturnDetailService.listReturnOrderDetailByReceiveIds(receiveIds);
        thisDetailList.forEach(poInstockDetailEntity -> {
            //未关联采购收货单，提审不校验
            PoInstockEntity poInstockEntity = list.stream().filter(e -> e.getId().equals(poInstockDetailEntity.getMainId()) && SourceTypeEnum.PO_RECEIVE.getCode().equals(e.getSourceType()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(poInstockEntity)){
                //收货单已收数量（已审核）
                List<WarehouseReceiveDetailEntity> receiveList = receiveDetailList.stream()
                        .filter(req -> CharSequenceUtil.isNotBlank(req.getPurchaseOrderDetailId()) &&  CharSequenceUtil.isNotBlank(req.getMainId()) && CharSequenceUtil.isNotBlank(req.getId())
                                && req.getPurchaseOrderDetailId().equals(poInstockDetailEntity.getPurchaseOrderDetailId())
                                && req.getMainId().equals(poInstockEntity.getSourceId())
                                && req.getId().equalsIgnoreCase(poInstockDetailEntity.getSourceDetailId())
                                && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(receiveList)){
                    Integer receiveQty = receiveList.stream().map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    //已退货数量 根据收货单获取对应退货明细
                    Integer returnQty = returnDetailDTOS.stream().filter(e -> CharSequenceUtil.isNotBlank(poInstockDetailEntity.getSourceDetailId()) && CharSequenceUtil.isNotBlank(poInstockDetailEntity.getPurchaseOrderDetailId())
                                    && CharSequenceUtil.isNotBlank(poInstockDetailEntity.getSkuId())
                                    && poInstockDetailEntity.getSourceDetailId().equals(e.getReceiveDetailId())
                                    && poInstockDetailEntity.getPurchaseOrderDetailId().equals(e.getPurchaseOrderDetailId())
                                    && poInstockDetailEntity.getSkuId().equals(e.getSkuId())
                                    && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()))
                            .map(WarehouseReceiveDTO.PoReturnDetailDTO::getReturnQty).reduce(MathUtil.ZERO,Integer::sum);
                    //最大入库数量
                    Integer maxInstockQty = receiveQty - returnQty;
                    if (poInstockDetailEntity.getStockInQty() > maxInstockQty){
                        throw new ServiceException(String.format("SKU【%s】入库数量不能大于"+ maxInstockQty, poInstockDetailEntity.getSkuNo()));
                    }
                }
            }
        });
    }

    @Override
    public PoInstockDTO.ViewDTO view(String id) {
        PoInstockDTO.ViewDTO dto = new PoInstockDTO.ViewDTO();

        //主表信息
        PoInstockEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        BeanMapperUtils.copy(entity, dto);

        //状态名称
        dto.setApproveStatusName(ApproveStatusEnum.getName(dto.getApproveStatus()));
        //采购信息
        PurchaseOrderDTO.GetOneDTO purchaseOrderDTO = scmTaskFeign.getByOrderId(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderDTO)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        dto.setPurchaseOrderCode(purchaseOrderDTO.getCode());
        WarehouseReceiveEntity receiveEntity = warehouseReceiveService.getById(entity.getSourceId());
        if (ObjectUtil.isNotEmpty(receiveEntity)) {
            dto.setSourceCode(receiveEntity.getCode());
        }
        //采购组织
        dto.setPurchaseOrgName(purchaseOrderDTO.getPurchaseOrgName());

        //明细信息
        List<PoInstockDetailEntity> entityDetails = poInstockDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98002);
        }
        List<PoInstockDetailDTO.ViewDTO> details = BeanMapperUtils.copyList(PoInstockDetailDTO.ViewDTO.class, entityDetails);
        List<String> skuIds = entityDetails.stream().map(PoInstockDetailEntity::getSkuId).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        //采购订单明细
        List<String> podIds = entityDetails.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //采购订单明细下所有入库数据
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailByPodIds(podIds);

        //查询退货明细
        List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);

        //收货单明细
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getDeliveryWarehouseId()));

        details.forEach(obj -> {
            if (CollectionUtils.isNotEmpty(skuVOList)) {
                SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                obj.setProductName(skuVO.getSkuName());
                obj.setSpuNo(skuVO.getSpuNo());
                obj.setUnitName(skuVO.getUnitName());
                obj.setVariantProperty(skuVO.getVariantProperty());
            }
            if (CollectionUtils.isNotEmpty(purchaseOrderDetailList)) {
                Integer purchaseQty = purchaseOrderDetailList.stream().filter(e -> e.getId().equals(obj.getPurchaseOrderDetailId())).map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setPurchaseQty(purchaseQty);
            }

            //退货单补货数量
            Integer returnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            if (CollectionUtils.isNotEmpty(poInstockDetailList)) {
                Integer effectiveStockInQty = poInstockDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setEffectiveStockInQty(effectiveStockInQty);
                obj.setUnStockInQty(obj.getPurchaseQty() - effectiveStockInQty + returnQty);
            }

            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                Integer receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
            }
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(obj.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            obj.setWarehouseLocationName(warehouseLocationEntity.getName());
            //库存状态默认可用
            obj.setInventoryStatusName(InventoryStatusEnum.USABLE.getName());
            obj.setFirstMassProductName(FirstMassProductTypeEnum.getName(obj.getFirstMassProduct()));
            //采购明细
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().
                    filter(p -> p.getId().equals(obj.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (Objects.nonNull(purchaseOrderDetailEntity)) {

                //含税单价
                BigDecimal taxPrice = purchaseOrderDetailEntity.getTaxPrice();
                //税率
                BigDecimal taxRate = purchaseOrderDetailEntity.getTaxRate();
                //获取到未税的值
                BigDecimal price = MathUtil.getUntaxed(taxPrice, taxRate,4);
                obj.setPrice(price);
                //入库数量 就是实收数量
                Integer stockInQty = obj.getStockInQty();
                //金额=未税价格*实收数量
                BigDecimal amount = MathUtil.multiplyWithTwo(price, stockInQty);
                obj.setAmount(amount);
                //价税合计=含税单价*实收数量
                BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, stockInQty);
                taxRate = MathUtil.multiplyWithTwo(taxRate, MathUtil.BigDecimal_100);
                obj.setTaxPrice(taxPrice);
                String taxRateStr = taxRate.toString().concat("%");
                obj.setTaxRate(taxRate);
                obj.setTaxRateStr(taxRateStr);
                obj.setTaxAmount(taxAmount);
                obj.setCurrency(purchaseOrderDetailEntity.getCurrency());
                obj.setCurrencySymbol(purchaseOrderDetailEntity.getCurrencySymbol());
            }

        });
        dto.setDetails(details);

        //查询采购供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = scmTaskFeign.getOrderSupplierByOrderId(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        PoInstockDTO.SupplierDTO supplierDTO = new PoInstockDTO.SupplierDTO();
        supplierDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
        supplierDTO.setSupplierName(purchaseOrderSupplierEntity.getSupplierName());
        supplierDTO.setSupplierContactId(purchaseOrderSupplierEntity.getSupplierContactId());
        if (CharSequenceUtil.isNotBlank(purchaseOrderSupplierEntity.getSupplierContactId())) {
            SupplierContactEntity supplierContactById = scmTaskFeign.getSupplierContactById(purchaseOrderSupplierEntity.getSupplierContactId());
            if (ObjectUtil.isNotEmpty(supplierContactById)) {
                supplierDTO.setSupplierContactName(supplierContactById.getPerson());
            }
        }

        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isNotEmpty(supplierEntity)) {
            supplierDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        }
        dto.setSupplierDTO(supplierDTO);


        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<PoInstockEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("采购入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        poInstockDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的采购入库单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(PoInstockEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "删除操作");
        //删除发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<PoInstockEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("采购入库单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(PoInstockEntity::getId, ids)
                .set(PoInstockEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PoInstockEntity::getInvalidTime, LocalDateTime.now())
                .set(PoInstockEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个采购入库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "作废操作");

        //作废发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(PoInstockEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //当前登陆人,启用流程后可删除
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (CharSequenceUtil.equals(entity.getCreateUserId(),userInfo.getUid()) && !CharSequenceUtil.equals(entity.getCreateUserId(), UserStateConstants.USER_SYSTEM_ID)) {
            throw new ServiceException(ApiError.WORKFLOW_APPROVE_CREATE_APPROVE_DIFF,userInfo.getUserName());
        }
        List<PoInstockEntity> list = Collections.singletonList(entity);
        //采购入库单明细
        List<PoInstockDetailEntity> detailList = poInstockDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }

        log.info("采购入库单【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
        String msg = "";
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE.getStatus());
            //子件校验数量-入库-推送金蝶
            autoInStockSubcontractChild(entity);
            //自动生成委外发料单
            autoGenerateSubcontractIssue(Collections.singletonList(entity));
            // 更新库存（需区分有无收货单）
            updateInventoryTransCore(Collections.singletonList(entity));
            //填入产品首批量产入库时间
            setFirstMassInstock(Collections.singletonList(entity.getId()));
            //更新采购入库单明细对应采购订单明细的执行状态
            updatePodArrivalState(Collections.singletonList(entity.getId()));
            //修改采购收货单入库状态
            warehouseReceiveService.updateReceiveInStockStatus(Collections.singletonList(entity));
            //审核生成采购对账单
            autoGeneratePoReconciliation(entity,detailList);

            //审核通过发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());

            //同步旺店通
            list.forEach(obj -> syncApproveInStockToWdt(obj, SyncOperateEnum.OPERATE_APPROVE));
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购入库单【%s】", ApproveTypeEnum.getName(type), entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.PO_INSTOCK.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 生成采购对账单信息
     * @author will
     * @date 2025/6/12 17:21
     * @param entity
     * @param poInstockDetailList
     * @return void
     */
    private void autoGeneratePoReconciliation (PoInstockEntity entity,List<PoInstockDetailEntity> poInstockDetailList) {
        //收货单信息
        List<String> sourceDetailIdList = poInstockDetailList.stream().map(PoInstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<WarehouseReceiveDTO.ReceiveSourceDTO> receiveList = warehouseReceiveService.listReceiveSourceByDetailIds(sourceDetailIdList);
        Map<String, WarehouseReceiveDTO.ReceiveSourceDTO> receiveMap = CollUtil.isEmpty(receiveList) ? new HashMap<>() : receiveList.stream().collect(Collectors.toMap(WarehouseReceiveDTO.ReceiveSourceDTO::getDetailId, Function.identity()));


        List<PoReconciliationDetailDTO.AddDTO> addList = new ArrayList<>();
        for (PoInstockDetailEntity poInstockDetailEntity : poInstockDetailList) {
            PoReconciliationDetailDTO.AddDTO addDTO = new PoReconciliationDetailDTO.AddDTO();
            //送货单信息
            WarehouseReceiveDTO.ReceiveSourceDTO receiveSourceDTO = receiveMap.get(poInstockDetailEntity.getSourceDetailId());
            if (ObjectUtil.isNotEmpty(receiveSourceDTO) && CharSequenceUtil.equals(receiveSourceDTO.getSourceType(),SourceTypeEnum.DELIVERY_ORDER.getCode())) {
                addDTO.setDeliveryId(receiveSourceDTO.getSourceId());
                addDTO.setDeliveryCode(receiveSourceDTO.getSourceCode());
                addDTO.setDeliveryDetailId(receiveSourceDTO.getSourceDetailId());
            }
            addDTO.setPoId(entity.getPurchaseOrderId());
            addDTO.setPoCode(entity.getPurchaseOrderCode());
            addDTO.setPoDetailId(poInstockDetailEntity.getPurchaseOrderDetailId());
            addDTO.setSupplierId(entity.getSupplierId());
            addDTO.setSupplierName(entity.getSupplierName());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceDetailId(poInstockDetailEntity.getId());
            addDTO.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
            addDTO.setBusinessStatus(PoReturnConfirmStatusEnum.CONFIRM.getCode());
            addDTO.setDate(entity.getStockInDate());
            addDTO.setSkuId(poInstockDetailEntity.getSkuId());
            addDTO.setQty(poInstockDetailEntity.getStockInQty());
            addDTO.setTaxPrice(poInstockDetailEntity.getTaxPrice());
            addDTO.setSettleOrgId(entity.getReceiveOrgId());
            addDTO.setCurrency(poInstockDetailEntity.getCurrency());
            addDTO.setRemark(poInstockDetailEntity.getRemark());
            addList.add(addDTO);
        }

        //自动生成功能系统标识
        Boolean originalValue = UserContext.getIsUserSystem();
        UserContext.setIsUserSystem(Boolean.TRUE);
        try {
            srmPoReconciliationFeign.add(addList);
        } finally {
            //恢复系统标识
            UserContext.setIsUserSystem(originalValue);
        }
    }

    /**
     * 子件校验并入库
     * @param poInstockEntity
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoInStockSubcontractChild(PoInstockEntity poInstockEntity) {
        //校验入库单是否是父级委外订单
        if (!SubcontractTypeEnum.ENUM_PARENT.getCode().equals(poInstockEntity.getSubcontractType())){
            return;
        }
        //采购入库单明细-父级
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listByMainIds(Collections.singletonList(poInstockEntity.getId()));
        if (CollectionUtils.isEmpty(poInstockDetailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }
        //查询采购订单记录-父级
        List<PurchaseOrderEntity> purchaseOrderEntities = scmTaskFeign.listPurchaseOrderByIds(Collections.singletonList(poInstockEntity.getPurchaseOrderId()));
        if (CollectionUtils.isEmpty(purchaseOrderEntities)){
            log.error(ApiError.ERROR_98025.msg);
            return;
        }
        List<String> sourceIds = purchaseOrderEntities.stream().filter(e -> Objects.nonNull(e) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(e.getSubcontractType())).map(PurchaseOrderEntity::getSourceId).distinct().collect(Collectors.toList());
        //全部采购订单记录
        List<PurchaseOrderEntity> purchaseOrderEntityList = scmTaskFeign.listPoBySourceIds(sourceIds);
        //全部采购订单明细
        List<String> poIds = purchaseOrderEntityList.stream().map(PurchaseOrderEntity::getId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.listByPurchaseOrderIds(poIds);
        //子件采购订单ids
        List<String> childPoIds = purchaseOrderEntityList.stream().filter(e -> Objects.nonNull(e) && SubcontractTypeEnum.ENUM_CHILD.getCode().equals(e.getSubcontractType())).map(PurchaseOrderEntity::getId).distinct().collect(Collectors.toList());
        //子件采购订单明细
        List<PurchaseOrderDetailEntity> childPurchaseOrderDetailEntityList = purchaseOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e)
                && CollectionUtils.isNotEmpty(childPoIds) && childPoIds.contains(e.getPurchaseOrderId())).collect(Collectors.toList());
        List<String> childPoDetailIds = childPurchaseOrderDetailEntityList.stream().filter(Objects::nonNull).map(PurchaseOrderDetailEntity::getId).distinct().collect(Collectors.toList());
        //子件的委外订单ids
        List<String> childSubIds = purchaseOrderEntityList.stream().filter(e -> Objects.nonNull(e) && SubcontractTypeEnum.ENUM_CHILD.getCode().equals(e.getSubcontractType())
        && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).map(PurchaseOrderEntity::getSourceId).distinct().collect(Collectors.toList());
        //子件的采购入库单记录
        List<PoInstockEntity> childPoInstockList = this.lambdaQuery().in(PoInstockEntity::getPurchaseOrderId, childPoIds).list();
        //子件采购入库单明细
        List<PoInstockDetailEntity> childPoInstockDetailList = poInstockDetailService.listDetailByPodIds(childPoDetailIds);
        //查询bom信息
        List<String> skuIds = poInstockDetailList.stream().map(PoInstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        //查询子件的委外订单明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailEntityList = scmTaskFeign.listSubcontractDetailByMainIds(childSubIds);
        List<PoInstockDetailEntity> needApproveDetailList = new ArrayList<>();
        //遍历父级采购明细匹配子件入库记录
        for (PoInstockDetailEntity poDetailEntity : poInstockDetailList){
            //采购订单明细-成品
            PurchaseOrderDetailEntity purchaseOrderDetail = purchaseOrderDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), poDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (Objects.isNull(purchaseOrderDetail)) {
                log.error(ApiError.ERROR_98026.msg);
                continue;
            }
            //子级委外订单明细
            List<SubcontractOrderDetailEntity> subDetailList = subcontractOrderDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentId(), purchaseOrderDetail.getSourceDetailId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(subDetailList)) {
                log.error(ApiError.ERROR_98070.msg);
                continue;
            }
            //获取委外订单中的sku版本及用量
            for (SubcontractOrderDetailEntity subcontractOrderDetailEntity : subDetailList){
                if (Objects.isNull(subcontractOrderDetailEntity.getIsGenerateInStock()) || !subcontractOrderDetailEntity.getIsGenerateInStock()){
                    continue;
                }

                //父级SKU和子级SKU之间的用量
                Integer quantity = bomList.stream()
                        .filter(obj -> subcontractOrderDetailEntity.getBomVersion().equals(obj.getBomVersion()) && obj.getSkuId().equals(subcontractOrderDetailEntity.getSkuId()) && obj.getParentSkuId().equals(poDetailEntity.getSkuId()))
                        .map(BomChildrenSkuDTO::getQuantity).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(quantity)) {
                    log.error(ApiError.ERROR_95166.msg);
                    continue;
                }
                //匹配对应采购订单明细记录
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = childPurchaseOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getSourceDetailId()) && e.getSourceDetailId().equals(subcontractOrderDetailEntity.getId())).findFirst().orElse(null);
                if (Objects.isNull(purchaseOrderDetailEntity)){
                    log.error(CharSequenceUtil.format("未找到入库单【{}】中SKU【{}】对应的采购订单明细记录"), poInstockEntity.getCode(), subcontractOrderDetailEntity.getSkuNo());

                    continue;
                }
                //应入库数量
                int stockInQty = poDetailEntity.getStockInQty() * quantity;
                List<PoInstockDetailEntity> poInstockDetailEntityList = childPoInstockDetailList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(subcontractOrderDetailEntity.getSkuId())
                        && CharSequenceUtil.isNotBlank(e.getPurchaseOrderDetailId()) && e.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())
                        && e.getStockInQty() == stockInQty).collect(Collectors.toList());
                //记录要入库的委外订单
                if (CollUtil.isEmpty(poInstockDetailEntityList)) {
                    log.error(CharSequenceUtil.format("未找到入库单【{}】中SKU【{}】数量【{}】的采购入库单明细"), poInstockEntity.getCode(), subcontractOrderDetailEntity.getSkuNo(),stockInQty);
                    continue;
                }
                for (PoInstockDetailEntity poInstockDetailEntity : poInstockDetailEntityList){
                //入库单状态检查
                PoInstockEntity poInstockEntity1 = childPoInstockList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(poInstockDetailEntity.getMainId())).findFirst().orElse(null);
                if (Objects.isNull(poInstockEntity1)){
                    log.error(CharSequenceUtil.format("未找到入库单【{}】中SKU【{}】数量【{}】的采购入库单"), poInstockEntity.getCode(), subcontractOrderDetailEntity.getSkuNo(),stockInQty);
                    continue;
                }
                if (ApproveStatusEnum.APPROVE.getStatus().equals(poInstockEntity1.getApproveStatus()) || poInstockEntity1.getInvalidStatus()){
                    log.error(CharSequenceUtil.format("已审核或已作废的采购入库单【{}】不能进行委外自动入库"), poInstockEntity1.getCode());
                    continue;
                }
                needApproveDetailList.add(poInstockDetailEntity);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(needApproveDetailList)){
            //自动生成功能系统标识
            Boolean originalValue = UserContext.getIsUserSystem();
            UserContext.setIsUserSystem(Boolean.TRUE);

            List<String> poInstockDetailIds = needApproveDetailList.stream().map(PoInstockDetailEntity::getId).distinct().collect(Collectors.toList());
            List<String> poInstockIds = needApproveDetailList.stream().map(PoInstockDetailEntity::getMainId).distinct().collect(Collectors.toList());
            //需要自动入库的子件入库单
            List<PoInstockEntity> poInstockEntityList = this.listByIds(poInstockIds);
            List<PoInstockDetailEntity> poInstockDetailEntityList = poInstockDetailService.listByMainIds(poInstockIds);
            for (PoInstockEntity entity : poInstockEntityList){
                List<PoInstockDetailEntity> detailEntityList = poInstockDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(entity.getId())).collect(Collectors.toList());
                //明细是否都在符合条件的自动审核订单中
                List<PoInstockDetailEntity> notExistDetailList = detailEntityList.stream().filter(e -> Objects.nonNull(e) && !poInstockDetailIds.contains(e.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(notExistDetailList)){
                    List<String> skuNoList = notExistDetailList.stream().map(PoInstockDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
                    log.error(CharSequenceUtil.format("采购入库单【{}】中SKU【{}】没有可入库记录",entity.getCode(), String.join(",",skuNoList)));
                    continue;
                }
                //自动提交审核
                if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(entity.getApproveStatus()) || ApproveStatusEnum.REJECT.getCode().equals(entity.getApproveStatus())) {
                    //提交
                    Boolean submit = this.submit(Collections.singletonList(entity.getId()));
                    if (!submit) {
                        throw new ServiceException(ApiError.ERROR_1042,"子件入库单");
                    }
                    //审核
                    entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getCode());
                }
                BatchResultDTO approve = this.approve(entity, ApproveTypeEnum.PASS.getStatus(), "系统自动审核", false);
                if (!approve.getSuccess()) {
                    throw new ServiceException(ApiError.ERROR_BILL_APPROVE,"子件入库单");
                }
            }
            //恢复系统标识
            UserContext.setIsUserSystem(originalValue);
        }
    }

    /**
     * 将审核通过的采购入库单转换为其他入库单推送到旺店通
     *
     * @param entity 采购入库单 PoInstockEntity
     * @param syncOperateEnum 操作代码 审核/反审核
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncApproveInStockToWdt(PoInstockEntity entity, SyncOperateEnum syncOperateEnum) {
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getDeliveryWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        List<PoInstockDetailEntity> detailList = poInstockDetailService.listByMainId(entity.getId());
        if(detailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }
        List<CreateOtherStockinRequest.GoodsList> goodsList = new ArrayList<>(detailList.size());
        for (PoInstockDetailEntity detailEntity : detailList) {
            CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getStockInQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getDeliveryWarehouseId());
            goodsList.add(goods);
        }
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_INSTOCK);
    }

    private void setFirstMassInstock(List<String> ids) {
        //填入产品首批量产入库时间
        List<FirstMassInstockDTO> firstMassInstockList = baseMapper.listFirstMassInstock(ids);
        List<String> skuIds = firstMassInstockList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIds);
        //获取到没有设置入库日期的sku
        List<String> skuIdList = productDetailEntityList.stream().filter(req -> req.getFirstMassProductDate() == null).map(req -> req.getId()).collect(Collectors.toList());
        List<ProductDetailEntity> skuEntityList = new ArrayList<>();
        for (String skuId : skuIdList) {
            FirstMassInstockDTO firstMassInstockDTO = firstMassInstockList.stream().filter(req -> req.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(firstMassInstockDTO)) {
                ProductDetailEntity skuEntity = new ProductDetailEntity();
                skuEntity.setId(skuId);
                skuEntity.setFirstMassProductDate(firstMassInstockDTO.getFirstMassProductDate());
                skuEntityList.add(skuEntity);
            }
        }
        plmTaskFeign.updateProductDetailBatch(skuEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(PoInstockEntity entity,List<PoReturnEntity> returnEntityList,List<SubcontractIssueEntity> issueEntityList) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        List<PoInstockEntity> list = Collections.singletonList(entity);
        //判断是否已经下推退货单
        if (CollectionUtils.isNotEmpty(returnEntityList)) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99014.msg);
        }
        //判断是否已经生成委外发料单
        if (CollectionUtils.isNotEmpty(issueEntityList)) {
            String subcontractOrderCodes = issueEntityList.stream().map(SubcontractIssueEntity::getSubcontractOrderCode).collect(Collectors.joining(","));
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),String.format(ApiError.ERROR_PO_INSTOCK_PUSH_SUBCONTRACT_ISSUE.msg, subcontractOrderCodes));
        }
        //校验是否存在
        checkPoReconciliation(entity);

        log.info("采购入库单反审核，id=【{}】", entity.getId());
        //更新单据为待提交
        updateApproveStatus(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //更新采购入库单明细对应采购订单明细的执行状态
        updatePodArrivalState(Collections.singletonList(entity.getId()));
        // 回滚库存
        InventoryUnApproveDTO dto = new InventoryUnApproveDTO(InventorySourceTypeEnum.PURCHASE_STOCK_IN, entity.getId());
        inventoryTransCoreService.unApprove(dto);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个采购入库单【%s】", entity.getCode()), ModuleTypeEnum.PO_INSTOCK.getCode(), entity.getId(), "反审核操作");
        //反审核通过发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        //修改采购收货单入库状态
        warehouseReceiveService.updateReceiveInStockStatus(Collections.singletonList(entity));

        //反审核通过发送旺店通
        list.forEach(obj -> syncDisApproveInStockToWdt(obj, SyncOperateEnum.OPERATE_DISAPPROVE));
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 验证采购对账单是否存在
     * @author will
     * @date 2025/6/13 17:35
     * @param entity
     * @return void
     */
    private void checkPoReconciliation (PoInstockEntity entity) {
      List<PoReconciliationDetailEntity> list =  FeignQuery.create(PoReconciliationDetailEntity.class)
              .eq(PoReconciliationDetailEntity::getSourceId,entity.getId())
              .list();
         if (CollUtil.isEmpty(list)) {
              return;
        }
        long count = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getMainId())).count();
        if (count > 0) {
          throw new ServiceException(ApiError.ERROR_PO_INSTOCK_PUSH_PO_RECONCILIATION);
        }
        //对账单删除
        List<String> sourceDetailIdList = list.stream().map(PoReconciliationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        srmPoReconciliationFeign.deleteDetailBySourceDetailIdList(sourceDetailIdList);
    }

    /**
     * 将反审核通过的采购入库单转换为其他出库推送给旺店通
     *
     * @param entity 采购入库单
     * @param syncOperateEnum
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncDisApproveInStockToWdt(PoInstockEntity entity, SyncOperateEnum syncOperateEnum) {
        List<PoInstockDetailEntity> detailList = poInstockDetailService.listByMainId(entity.getId());
        if(detailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }

        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getDeliveryWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }

        List<CreateOtherStockoutRequest.GoodsList> goodsList = new ArrayList<>(detailList.size());
        for (PoInstockDetailEntity detailEntity : detailList) {
            CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getStockInQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getDeliveryWarehouseId());
            goodsList.add(goods);
        }

        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_OUTSTOCK);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto) {
        List<String> ids = dto.getIds();
        //根据ids查询
        List<PoInstockEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购入库单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("采购入库单【%s】取消流程", ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(PoInstockDTO.ExportParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购入库单数据", EXPORT_WMS_PO_IN_STOCK.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = baseMapper.viewGeneratePurchaseReturnOrder(ids);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> skuIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            return list;
        }
        List<String> podIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = list.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getDeliveryWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        List<String> resultIds = new ArrayList<>();
        for (PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO dto : list) {
            //来源类型
            dto.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
            dto.setProductName(productName);


            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(dto.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
            dto.setPurchaseOrderDetailId(detailEntity.getId());
            //币别
            dto.setCurrency(detailEntity.getCurrency());
            //币种符号
            dto.setCurrencySymbol(detailEntity.getCurrencySymbol());
            //单价
            dto.setTaxPrice(detailEntity.getTaxPrice());
            //相同采购单号清空后面数据的采购单号和供应商
            boolean contains = resultIds.contains(dto.getPurchaseOrderId());

            if (contains) {
                dto.setPurchaseOrderCode(null);
                dto.setSupplierName(null);
                continue;
            }
            resultIds.add(dto.getPurchaseOrderId());
            //仓位信息
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> Objects.nonNull(e) && e.getWarehouseId().equals(dto.getDeliveryWarehouseId())
                    && e.getCode().equals(dto.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            dto.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
        return list;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> list = dto.getList();
        //查询实退数量
        List<String> sourceIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceId).collect(Collectors.toList());
        List<String> sourceDetailIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceDetailId).collect(Collectors.toList());
        //来源单据为采购入库单
        List<PoInstockEntity> sourceList = this.listByIds(sourceIds);
        if (CollectionUtils.isEmpty(sourceList)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        long count = sourceList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99012);
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //采购入库单
        List<PoInstockDetailEntity> sourceDetailList = poInstockDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(sourceDetailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }
        //采购订单
        List<String> poIdList = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getPurchaseOrderId).collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(poIdList);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        List<PurchaseReturnOrderDTO.AddDTO> addList = new ArrayList<>();

        Map<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSourceId().concat(obj.getReturnMode())));
        for (Map.Entry<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> entry : map.entrySet()) {
            List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> value = entry.getValue();
            PurchaseReturnOrderDTO.AddDTO addDTO = new PurchaseReturnOrderDTO.AddDTO();

            PoInstockDTO.GeneratePurchaseReturnOrderDTO purchaseReturnOrderDTO = value.get(0);
            //采购入库单
            PoInstockEntity poInstockEntity = sourceList.stream().filter(obj -> obj.getId().equals(purchaseReturnOrderDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(poInstockEntity)) {
                throw new ServiceException(ApiError.ERROR_98050);
            }
            //采购订单
            PurchaseOrderEntity purchaseOrderEntity = purchaseOrderList.stream().filter(obj -> obj.getId().equals(purchaseReturnOrderDTO.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98025);
            }

            BeanMapperUtils.copy(poInstockEntity, addDTO);
            addDTO.setBillDate(LocalDate.now());
            addDTO.setSourceType(purchaseReturnOrderDTO.getSourceType());
            addDTO.setSourceId(purchaseReturnOrderDTO.getSourceId());
            addDTO.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (PoInstockDTO.GeneratePurchaseReturnOrderDTO detail : value) {
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
                //验证退货数量
                Integer stockInQty = sourceDetailList.stream().filter(obj -> obj.getId().equals(detail.getSourceDetailId())).map(e -> e.getStockInQty()).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(stockInQty)) {
                    throw new ServiceException(1, String.format("SKU【%s】未找到对应数量", detail.getSkuNo()));
                }
                if (MathUtil.compareTo(detail.getRealityReturnQty(), stockInQty) > 0) {
                    throw new ServiceException(1, String.format("SKU【%s】实退数量不能大于【%s】", detail.getSkuNo(), stockInQty));
                }
                BeanMapperUtils.copy(detail, addDetailDTO);
                addDetailDTO.setPurchaseOrderDetailId(detail.getPurchaseOrderDetailId());
                addDetailDTO.setReturnQty(detail.getRealityReturnQty());
                addDetailDTO.setReturnPrice(detail.getTaxPrice());
                addDetailList.add(addDetailDTO);
            }
            addDTO.setReturnMode(purchaseReturnOrderDTO.getReturnMode());
            addDTO.setPurchasePriceDetailList(addDetailList);
            addDTO.setReturnUserId(purchaseReturnOrderDTO.getReturnUserId());
            addDTO.setReturnOrgId(CharSequenceUtil.isBlank(poInstockEntity.getReceiveOrgId()) ? userInfo.getUid() : poInstockEntity.getReceiveOrgId());
            addDTO.setReturnWarehouseId(poInstockEntity.getDeliveryWarehouseId());
            addList.add(addDTO);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            addList.forEach(obj -> poReturnService.add(obj));
        }

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddPurchaseStockIn(List<PoInstockDTO.AddDTO> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return Boolean.FALSE;
        }
        resultList.forEach(obj -> add(obj, Boolean.FALSE));
        return Boolean.TRUE;
    }

    /**
     * 根据来源Id查询入库单
     *
     * @param sourceId sourceId
     * @return com.erp.model.wms.entity.PurchaseStockInEntity
     * @Author Luo_WG
     * @Date 2023/4/18 10:30
     **/
    @Override
    public List<PoInstockEntity> getStockInBySourceId(String sourceId) {
        return lambdaQuery().eq(PoInstockEntity::getSourceId, sourceId).list();
    }

    @Override
    public List<PoInstockEntity> getStockInBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PoInstockEntity::getSourceId, sourceIds).list();
    }

    /**
     * 修改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     **/
    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(PoInstockEntity::getId, id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), PoInstockEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public List<PoInstockDTO.SupplierInstockInfoDTO> getInstockInfoBySupplierIds(PoInstockDTO.SupplierInstockParamDTO dto) {
        return this.baseMapper.getInstockInfoBySupplierIds(dto.getSupplierIds(), dto.getDateList());
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().in(PoInstockEntity::getId, ids)
                .set(PoInstockEntity::getApproveUserId, userInfo.getUid())
                .set(PoInstockEntity::getApproveUserName, userInfo.getUserName())
                .set(PoInstockEntity::getApproveStatus, approveStatus)
                .set(PoInstockEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PoInstockEntity::getId, ids)
                .set(PoInstockEntity::getApproveStatus, approveStatus)
                .set(PoInstockEntity::getApproveUserId, "")
                .set(PoInstockEntity::getApproveUserName, "")
                .set(PoInstockEntity::getApproveTime, null)
                .update();
    }

    /**
     * 根据ids查询数据
     */
    private List<PoInstockEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PoInstockEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        return list;
    }

    /**
     * 新增添加默认值
     */
    private void addDefaultPurchaseData(String purchaseOrderId, PoInstockEntity entity) {
        //查询采购订单信息
        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(purchaseOrderId);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        entity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        entity.setPurchaseUserId(purchaseOrderEntity.getPurchaseUserId());
        entity.setPurchaseUserName(purchaseOrderEntity.getPurchaseUserName());
        entity.setPurchaseDeptId(purchaseOrderEntity.getPurchaseDeptId());
        entity.setPurchaseDeptName(purchaseOrderEntity.getPurchaseDeptName());
        entity.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        entity.setReceiveOrgName(purchaseOrderEntity.getReceiveOrgName());

        //查询采购供应商
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        entity.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
        entity.setSupplierName(purchaseOrderSupplierEntity.getSupplierName());
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String stockInDeptId, String stockInUserId, String deliveryWarehouseId, PoInstockEntity entity) {

        //入库员
        if (CharSequenceUtil.isNotBlank(stockInUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(stockInUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            entity.setStockInUserName(userDTO.getUserName());
        }
        //入库部门
        if (CharSequenceUtil.isNotBlank(stockInDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(stockInDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setStockInDeptName(depart.getName());
        }
        //仓库
        if (CharSequenceUtil.isNotBlank(deliveryWarehouseId)) {
            //仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Collections.singletonList(deliveryWarehouseId));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDeliveryWarehouseName(warehouseName);
        }
        //更新委外标识
        String purchaseOrderId = entity.getPurchaseOrderId();
        PurchaseOrderDTO.GetOneDTO getOneDTO = scmTaskFeign.getByOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(getOneDTO)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        entity.setSubcontractType(getOneDTO.getSubcontractType());
        //采购订单类型
        entity.setPurchaseType(getOneDTO.getType());

        //来源编码
        if (CharSequenceUtil.equals(entity.getSourceType(),SourceTypeEnum.PURCHASE_ORDER.getCode())) {
            entity.setSourceCode(getOneDTO.getCode());
        } else if (CharSequenceUtil.equals(entity.getSourceType(),SourceTypeEnum.PO_RECEIVE.getCode())) {
            WarehouseReceiveEntity receiveEntity = warehouseReceiveService.getById(entity.getSourceId());
            if (ObjectUtil.isNotEmpty(receiveEntity)) {
                entity.setSourceCode(receiveEntity.getCode());
            }
        }
    }

    /**
     * 根据采购单获取入库数量
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseStockInDTO.GetStockInQty>
     * @Author Luo_WG
     * @Date 2023/4/18 19:36
     **/
    @Override
    public List<PoInstockDTO.GetStockInQty> getStockInQty(List<String> ids) {
        return baseMapper.getStockInQty(ids);
    }

    @Override
    public List<PoInstockDTO.OrderRefStockInDTO> purchaseOrderRefStockIn(String purchaseOrderId) {
        List<PoInstockDTO.OrderRefStockInDTO> list = baseMapper.purchaseOrderRefStockIn(purchaseOrderId);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        //获取采购单详情表id集合
        List<String> skuIds = list.stream().map(PoInstockDTO.OrderRefStockInDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
        for (PoInstockDTO.OrderRefStockInDTO dto : list) {
            dto.setApproveStatusName(ApproveStatusEnum.getName(dto.getApproveStatus()));
            dto.setInvalidStatusName(InvalidStatusEnum.getName(dto.getInvalidStatus()));
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(dto.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            dto.setProductName(productName);
        }
        return list;
    }

    @Override
    public Boolean generateStockIn(PurchaseOrderDTO.ListGenerateStockInDTO dto) {
        //保存信息
        List<PurchaseOrderDTO.GenerateStockInDTO> list = dto.getList();
        List<String> ids = list.stream().map(PurchaseOrderDTO.GenerateStockInDTO::getPurchaseOrderId).collect(Collectors.toList());

        //订单信息集合
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            log.error("未找到订单信息，ids={}", JSONUtil.toJsonStr(ids));
            throw new ServiceException(ApiError.ERROR_98025);
        }
        long count = purchaseOrderList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98054);
        }

        //订单明细信息集合
        List<String> detailIdList = list.stream().map(PurchaseOrderDTO.GenerateStockInDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(detailIdList);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            log.error("未找到订单明细信息，detailIdList={}", JSONUtil.toJsonStr(detailIdList));
            throw new ServiceException(ApiError.ERROR_98026);
        }


        List<String> stockInUserIds = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getStockInUserId())).map(PurchaseOrderDTO.GenerateStockInDTO::getStockInUserId).collect(Collectors.toList());
        List<SysDepartmentUserNumberDTO> sysDepartmentUserNumberList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(stockInUserIds)) {
            sysDepartmentUserNumberList = sysUserFeign.listDeptUserByUserIdList(stockInUserIds);
        }


        Map<String, List<PurchaseOrderDTO.GenerateStockInDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDTO.GenerateStockInDTO::getPurchaseOrderId));
        List<PoInstockDTO.AddDTO> resultList = new ArrayList<>();
        for (Map.Entry<String, List<PurchaseOrderDTO.GenerateStockInDTO>> entry : map.entrySet()) {
            PoInstockDTO.AddDTO addDTO = new PoInstockDTO.AddDTO();
            String purchaseOrderId = entry.getKey();
            List<PurchaseOrderDTO.GenerateStockInDTO> value = entry.getValue();

            //订单信息
            PurchaseOrderEntity entity = purchaseOrderList.stream().filter(obj -> obj.getId().equals(purchaseOrderId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                log.error("未找到订单明细信息，id={}", purchaseOrderId);
                throw new ServiceException(ApiError.ERROR_98025);
            }
            addDTO.setPurchaseOrderId(purchaseOrderId);
            addDTO.setSourceId(purchaseOrderId);
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
            addDTO.setDeliveryWarehouseId(entity.getDeliveryWarehouseId());
            addDTO.setStockInUserId(value.get(0).getStockInUserId());
            //部门
            if (CollectionUtils.isNotEmpty(sysDepartmentUserNumberList)) {
                String deptId = sysDepartmentUserNumberList.stream().filter(obj -> obj.getUserId().equals(value.get(0).getStockInUserId())).map(SysDepartmentUserNumberDTO::getDepartmentId).findFirst().orElse("");
                addDTO.setStockInDeptId(deptId);
            }
            addDTO.setStockInDate(value.get(0).getStockInDate());
            List<PoInstockDetailDTO.AddDTO> details = new ArrayList<>();
            for (PurchaseOrderDTO.GenerateStockInDTO generateStockInDTO : value) {
                PoInstockDetailDTO.AddDTO addDetailDTO = new PoInstockDetailDTO.AddDTO();

                //订单明细数据校验
                String skuNos = purchaseOrderDetailList.stream().filter(obj -> !CharSequenceUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                                && !CharSequenceUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                                && !CharSequenceUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus())
                        )
                        .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                if (CharSequenceUtil.isNotBlank(skuNos)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,entity.getCode(),skuNos);
                }

                addDetailDTO.setSourceDetailId(generateStockInDTO.getPurchaseOrderDetailId());
                addDetailDTO.setPurchaseOrderDetailId(generateStockInDTO.getPurchaseOrderDetailId());
                addDetailDTO.setStockInQty(generateStockInDTO.getStockInQty());
                addDetailDTO.setExceedQty(generateStockInDTO.getExceedQty());
                addDetailDTO.setRemark(generateStockInDTO.getRemark());
                addDetailDTO.setWarehouseLocation(generateStockInDTO.getWarehouseLocation());
                addDetailDTO.setFirstMassProduct(generateStockInDTO.getFirstMassProduct());
                details.add(addDetailDTO);
            }
            addDTO.setDetails(details);
            resultList.add(addDTO);
        }
        Boolean add = this.batchAddPurchaseStockIn(resultList);
        return add;
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandlePurchaseStockIn(List<PoInstockDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(PoInstockDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(ids);

        //采购收货主表信息
        List<String> instockSourceIdList = records.stream().map(PoInstockDTO.ListDTO::getInstockSourceId).collect(Collectors.toList());
        List<WarehouseReceiveEntity> warehouseReceiveList = warehouseReceiveService.listByIds(instockSourceIdList);

        //采购入库明细ids
        List<String> podIds = records.stream().map(PoInstockDTO.ListDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        //采购退货单
        List<String> returnIds = records.stream().filter(e -> Objects.nonNull(e) && SourceTypeEnum.PO_RETURN.getCode().equals(e.getSourceType())).map(PoInstockDTO.ListDTO::getSourceId).distinct().collect(Collectors.toList());
        List<PoReturnEntity> poReturnEntityList = null;
        if (CollectionUtils.isNotEmpty(returnIds)){
            poReturnEntityList = poReturnService.listByIds(returnIds);
        }
        List<String> warehouseIds = records.stream().map(req -> req.getDeliveryWarehouseId()).distinct().collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        for (PoInstockDTO.ListDTO obj : records) {
            SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(obj.getSkuId())).findFirst().orElse(null);
            //产品名称
            if (Objects.nonNull(skuVO)) {
                obj.setProductName(skuVO.getSkuName());
            }
            //送货单号
            String deliveryCode = warehouseReceiveList.stream().filter(e -> e.getSourceType().equals(SourceTypeEnum.DELIVERY_ORDER.getCode()) && CharSequenceUtil.equals(obj.getInstockSourceId(), e.getId())).map(WarehouseReceiveEntity::getSourceCode).findFirst().orElse("");
            obj.setDeliveryCode(deliveryCode);

            //收货数量
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                Integer receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
            }
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            //含税单价
            BigDecimal taxPrice = obj.getTaxPrice();
            //税率
            BigDecimal taxRate = obj.getTaxRate();
            //获取到未税的值
            BigDecimal price = MathUtil.getUntaxed(taxPrice, taxRate,4);
            obj.setPrice(price);
            //入库数量 就是实收数量
            Integer stockInQty = obj.getStockInQty();
            //金额=未税价格*实收数量
            BigDecimal qty = new BigDecimal(stockInQty);
            BigDecimal amount = MathUtil.multiplyWithTwo(price, qty, 4);
            obj.setAmount(amount);
            //价税合计=含税单价*实收数量
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, qty, 4);
            obj.setTaxAmount(taxAmount);
            taxRate = MathUtil.multiplyWithTwo(taxRate, MathUtil.BigDecimal_100);
            String taxRateStr = taxRate.toString().concat("%");
            obj.setTaxRateStr(taxRateStr);
            //仓位名称
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(obj.getDeliveryWarehouseId())
                    && req.getCode().equals(obj.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            obj.setWarehouseLocationName(warehouseLocationEntity.getName());
            //采购订单类型
            obj.setPurchaseTypeName(PurchaseOrderTypeEnum.getNameByCode(obj.getPurchaseType()));
            //退货方式
            if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(obj.getPurchaseType()) && CollectionUtils.isNotEmpty(poReturnEntityList)){
                PoReturnEntity poReturnEntity = poReturnEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(obj.getSourceId())).findFirst().orElse(null);
                if (Objects.nonNull(poReturnEntity)){
                    obj.setReturnMode(poReturnEntity.getReturnMode());
                    obj.setReturnModeName(ReturnModeEnum.getName(poReturnEntity.getReturnMode()));
                }
            }
        }
    }

    /**
     * @param list
     * @description:更新库存
     * @author zhangchunlin
     * @date: 2023/5/23 15:19
     */
    public void updateInventoryTransCore(List<PoInstockEntity> list) {
        List<String> ids = list.stream().map(PoInstockEntity::getId).distinct().collect(Collectors.toList());
        Map<String, PoInstockEntity> mainMap = list.stream().collect(Collectors.toMap(PoInstockEntity::getId, Function.identity()));
        List<PoInstockDetailEntity> detailEntityList = poInstockDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException("未找到入库单明细信息");
        }
        Map<String, List<PoInstockDetailEntity>> detailMap = detailEntityList.stream().collect(Collectors.groupingBy(PoInstockDetailEntity::getMainId));
        List<PoInstockDetailEntity> receiveDetailList = Lists.newArrayList();// 有收货单的明细
        List<PoInstockDetailEntity> noReceiveDetailList = Lists.newArrayList();// 无收货单的明细
        Map<String, PoInstockEntity> receiveMap = Maps.newHashMap();// 有收货单的主单
        Map<String, PoInstockEntity> noReceiveMap = Maps.newHashMap();// 无收货单的主单
        // 此处注意，需区分有无收货单
        mainMap.forEach((mainId, poInstockEntity) -> {
            String sourceType = poInstockEntity.getSourceType();
            SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getByCode(sourceType);
            List<PoInstockDetailEntity> details = detailMap.get(mainId);
            if (Objects.equals(SourceTypeEnum.PO_RECEIVE, sourceTypeEnum)) { // 有收货单
                receiveMap.put(mainId, poInstockEntity);
                receiveDetailList.addAll(details);
            } else {// 无收货单
                noReceiveMap.put(mainId, poInstockEntity);
                noReceiveDetailList.addAll(details);
            }
        });
        // 有收货单的库存处理
        receiveInventory(receiveMap, receiveDetailList);
        // 无收货单的库存处理
        noReceiveInventory(noReceiveMap, noReceiveDetailList);
    }

    /**
     * 采购入库（有收货单）
     *
     * @param receiveMap
     * @param receiveDetailList
     */
    public void receiveInventory(Map<String, PoInstockEntity> receiveMap,
                                 List<PoInstockDetailEntity> receiveDetailList) { // 采购入库（有收货单）
        if (CollUtil.isNotEmpty(receiveDetailList)) {
            InventoryInOutStockDTO receiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            receiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_INSTOCK_REC.getCode());
            List<InOutStockDTO> receiveMembers = Lists.newArrayList();
            receiveDetailList.forEach(detail -> {
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_STOCK_IN);
                PoInstockEntity poInstockEntity = receiveMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(poInstockEntity.getId());
                inOutStockDTO.setSourceCode(poInstockEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(poInstockEntity.getStockInDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setQty(detail.getStockInQty());
                inOutStockDTO.setWarehouseId(poInstockEntity.getDeliveryWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                receiveMembers.add(inOutStockDTO);
            });
            receiveInventoryInOutStockDTO.setParamList(receiveMembers);
            inventoryTransCoreService.approveByType(receiveInventoryInOutStockDTO);
        }
    }

    /**
     * 采购入库（无收货单）
     *
     * @param noReceiveMap
     * @param noReceiveDetailList
     */
    public void noReceiveInventory(Map<String, PoInstockEntity> noReceiveMap,
                                   List<PoInstockDetailEntity> noReceiveDetailList) { // 采购入库（无收货单）
        if (CollUtil.isNotEmpty(noReceiveDetailList)) {
            InventoryInOutStockDTO noReceiveInventoryInOutStockDTO = new InventoryInOutStockDTO();
            noReceiveInventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.PO_INSTOCK_UNREC.getCode());
            List<InOutStockDTO> noReceiveMembers = Lists.newArrayList();
            noReceiveDetailList.forEach(detail -> {
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_STOCK_IN);
                PoInstockEntity poInstockEntity = noReceiveMap.get(detail.getMainId());
                inOutStockDTO.setSourceId(poInstockEntity.getId());
                inOutStockDTO.setSourceCode(poInstockEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(poInstockEntity.getStockInDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setQty(detail.getStockInQty());
                inOutStockDTO.setWarehouseId(poInstockEntity.getDeliveryWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                noReceiveMembers.add(inOutStockDTO);
            });
            noReceiveInventoryInOutStockDTO.setParamList(noReceiveMembers);
            inventoryTransCoreService.approveByType(noReceiveInventoryInOutStockDTO);
        }
    }

    /**
     * @param detailList
     * @description: 质检信息校验
     * @author Will
     * @date: 2023/7/3 11:33
     */
    private void checkQcInfo(List<PoInstockEntity> list,List<PoInstockDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(detailList)) {
            return;
        }
        /**
         * 1、入库单是收货单下推则根据收货单明细查询质检单
         * 2、入库单非收货单下推则根据采购订单明细查询收货单后查询质检单
         * 3、入库单非收货单下推且采购订单没有对应收货单，则根据采购订单明细查询质检单
         */
        List<String> receiveDetailIdList = new ArrayList<>();
        List<String> podIdList = new ArrayList<>();

        for (PoInstockEntity poInstockEntity : list) {
            //明细
            List<PoInstockDetailEntity> poInstockDetailList = detailList.stream().filter(obj -> obj.getMainId().equals(poInstockEntity.getId())).collect(Collectors.toList());
            //收货单下推的采购订单
            if (SourceTypeEnum.PO_RECEIVE.getCode().equals(poInstockEntity.getSourceType())) {
                List<String> idList = poInstockDetailList.stream().map(PoInstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
                receiveDetailIdList.addAll(idList);
                continue;
            }
            //非收货单下推的入库单
            List<String> idList = poInstockDetailList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
            podIdList.addAll(idList);
        }
        List<WarehouseReceiveDetailEntity> resultReceiveDetailList = new ArrayList<>();
        //收货单下推对应的收货明细
        if (CollectionUtils.isNotEmpty(receiveDetailIdList)) {
            List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listByIds(receiveDetailIdList);
            resultReceiveDetailList.addAll(receiveDetailList);
        }
        //不含收货单的采购订单明细
        List<String> notHasPodIdList = new ArrayList<>();
        //非收货单下推对应的收货明细
        if (CollectionUtils.isNotEmpty(podIdList)) {
            //收货信息
            List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIdList);
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                List<String> receivePodIdList = receiveDetailList.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
                notHasPodIdList = podIdList.stream().filter(obj -> !receivePodIdList.contains(obj)).collect(Collectors.toList());
            } else {
                notHasPodIdList = podIdList;
            }
            resultReceiveDetailList.addAll(receiveDetailList);
        }

        //质检信息
        List<QcInfoEntity> qcInfoList = getReceiveQcInfo(resultReceiveDetailList,notHasPodIdList);
        if (CollectionUtils.isEmpty(qcInfoList)) {
            return;
        }
        List<QcInfoEntity> resultList = qcInfoList.stream().filter(obj -> QcBillStatusEnum.DRAFT.equals(obj.getQcStatus()) || QcBillStatusEnum.WAIT_QC.equals(obj.getQcStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(resultList)) {
            String qcCodes = resultList.stream().map(QcInfoEntity::getPurchaseOrderCode).distinct().collect(Collectors.joining());
            throw new ServiceException(new ApiResult(1, String.format("采购订单【%s】未质检完成不支持审核", qcCodes)));
        }

    }

    /**
     * @description: 查询收货单质检信息
     * @author Will
     * @date: 2023/11/23 15:06
     * @param resultReceiveDetailList
     * @return List<QcInfoEntity>
     */
    @Override
    public List<QcInfoEntity> getReceiveQcInfo (List<WarehouseReceiveDetailEntity> resultReceiveDetailList, List<String> notHasPodIdList) {

        if (CollectionUtils.isEmpty(resultReceiveDetailList) && CollectionUtils.isEmpty(notHasPodIdList)) {
            return Collections.emptyList();
        }
        //收货单明细id
        List<String> receiveDetailIds = resultReceiveDetailList.stream().map(WarehouseReceiveDetailEntity::getId).distinct().collect(Collectors.toList());

        notHasPodIdList.addAll(receiveDetailIds);
        //质检信息
        List<QcInfoEntity> qcInfoList = qcInfoService.listQCBySourceDetailIds(notHasPodIdList);
        return qcInfoList;
    }

    @Override
    public PagingVO<PoInstockDTO.ListDTO> exportPoInStock(PagingDTO<PoInstockDTO.ExportParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<PoInstockDTO.ListDTO> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            doOpHandlePurchaseStockIn(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<PoInstockDTO.PdaPagingView> PdaPaging(PagingDTO<PoInstockDTO.PdaSearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        PoInstockDTO.PdaSearchParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setStockInDateList(dateList);
        }
        IPage<PoInstockDTO.PdaPagingView> pageData = this.baseMapper.pdaPaging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<PoInstockDTO.PdaPagingView> records = pageData.getRecords();

        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());

        //采购单id
        List<String> purchaseOrderIds = records.stream().map(req -> req.getPurchaseOrderId()).collect(Collectors.toList());
        //查询详情
        List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listByMainIds(ids);

        List<String> podIds = poInstockDetailEntities.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //存在收货单,且收货单下面的质检单未质检完成则不允许提交
        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        List<String> receiveIds = receiveDetailList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        receiveIds.addAll(purchaseOrderIds);
        //质检信息
        List<QcInfoEntity> qcInfoList = qcInfoService.listQCBySourceIds(receiveIds);

        for (PoInstockDTO.PdaPagingView record : records) {

            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<PoInstockDetailEntity> detailEntities = poInstockDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<PoInstockDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, PoInstockDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            List<QcInfoEntity> resultList = qcInfoList.stream().filter(obj -> obj.getSourceId().equals(record.getSourceId())).collect(Collectors.toList());
            List<QcInfoEntity> qcFinishList = resultList.stream().filter(obj -> (QcBillStatusEnum.EXEMPTION.equals(obj.getQcStatus()) || QcBillStatusEnum.FINISH_QC.equals(obj.getQcStatus()))).collect(Collectors.toList());
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
    public List<PoInstockDTO.PdaPoInStockCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<PoInstockDTO.PdaPoInStockCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            PoInstockDTO.SearchParamDTO pagingParamDTO = new PoInstockDTO.SearchParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            PoInstockDTO.PdaPoInStockCountDTO resultDTO = new PoInstockDTO.PdaPoInStockCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setStockInDateList(dateList);
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String pdaAdd(PoInstockDTO.AddDTO dto, Boolean aFalse) {

        List<PoInstockDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        List<PoInstockDetailDTO.AddDTO> detailList = dto.getDetails();
        List<String> poReceiveDetailIds = detailList.stream().map(PoInstockDetailDTO.AddDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailEntities = warehouseReceiveDetailService.listByIds(poReceiveDetailIds);
        List<WarehouseReceiveDetailEntity> detailEntityListByMainId = warehouseReceiveDetailService.listDetailByMainIds(Collections.singletonList(dto.getSourceId()));

        for (PoInstockDetailDTO.AddDTO addDTO : detailList) {
            WarehouseReceiveDetailEntity detailEntity = receiveDetailEntities.stream().filter(req -> req.getId().equals(addDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_RECEIVE_DETAIL_SKU_NOT_EXIST, addDTO.getSkuNo());
            }
            List<PoInstockDetailDTO.AddDTO> addList = detailList.stream().filter(req -> req.getSkuNo().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            List<WarehouseReceiveDetailEntity> detailEntityList = detailEntityListByMainId.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            //校验sku是否有重复，重复需要拆单
            if (detailEntityList.size() > addList.size()) {
                List<String> pordIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
                List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailBySourceDetailIds(pordIds);
                Integer stockInQty = addDTO.getStockInQty();
                for (WarehouseReceiveDetailEntity entity : detailEntityList) {
                    //收货数量
                    Integer receiveQty = detailEntityList.stream().map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    //已签收数量
                    Integer alreadyStockInQty = poInstockDetailEntities.stream().filter(obj -> obj.getSourceDetailId().equals(entity.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (stockInQty > receiveQty - alreadyStockInQty) {
                        throw new ServiceException(String.format("SKU【%s】实收数量不能超过未入库量", detailEntity.getSkuNo()));

                    }
                    if (alreadyStockInQty >= entity.getReceiveQty()) {
                        continue;
                    }
                    PoInstockDetailDTO.AddDTO addSkuDTO = new PoInstockDetailDTO.AddDTO();
                    addSkuDTO.setSourceDetailId(entity.getId());
                    addSkuDTO.setPurchaseOrderDetailId(entity.getPurchaseOrderDetailId());
                    addSkuDTO.setWarehouseLocation(addDTO.getWarehouseLocation());
                    addSkuDTO.setSkuNo(addDTO.getSkuNo());
                    addSkuDTO.setExceedQty(addDTO.getExceedQty());
                    addSkuDTO.setRemark(addDTO.getRemark());
                    if (stockInQty > (entity.getReceiveQty() - alreadyStockInQty) && !detailEntityList.get(detailEntityList.size() - 1).getId().equals(entity.getId())) {
                        stockInQty = stockInQty - (entity.getReceiveQty() - alreadyStockInQty);
                        addSkuDTO.setStockInQty(entity.getReceiveQty() - alreadyStockInQty);
                        addDTOList.add(addSkuDTO);
                    } else {
                        addSkuDTO.setStockInQty(stockInQty);
                        addDTOList.add(addSkuDTO);
                        break;
                    }
                    addDTO.setExceedQty(0);
                }
            } else {
                addDTOList.add(addDTO);
            }
        }
        dto.setDetails(addDTOList);
        PoInstockEntity entity = this.add(dto, aFalse);
        return entity.getId();
    }

    @Override
    public Boolean pdaUpdate(PoInstockDTO.UpdateDTO dto) {
        List<PoInstockDetailDTO.UpdateDTO> updateDTOList = new ArrayList<>();
        List<PoInstockDetailDTO.UpdateDTO> detailList = dto.getDetails();
        List<String> poReceiveDetailIds = detailList.stream().map(PoInstockDetailDTO.UpdateDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailEntities = warehouseReceiveDetailService.listByIds(poReceiveDetailIds);
        List<WarehouseReceiveDetailEntity> detailEntityListByMainId = warehouseReceiveDetailService.listDetailByMainIds(Collections.singletonList(dto.getSourceId()));
        List<PoInstockDetailEntity> instockDetailEntities = poInstockDetailService.listByMainId(dto.getId());
        List<String> poInstockIds = instockDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        for (PoInstockDetailDTO.UpdateDTO updateDTO : detailList) {
            WarehouseReceiveDetailEntity detailEntity = receiveDetailEntities.stream().filter(req -> req.getId().equals(updateDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_RECEIVE_DETAIL_SKU_NOT_EXIST, updateDTO.getSkuNo());
            }

            List<WarehouseReceiveDetailEntity> detailEntityList = detailEntityListByMainId.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            //校验sku是否有重复，重复需要拆单
            if (detailEntityList.size() > MathUtil.ONE) {
                List<String> pordIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
                List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailBySourceDetailIds(pordIds);
                Integer stockInQty = updateDTO.getStockInQty();
                for (WarehouseReceiveDetailEntity entity : detailEntityList) {
                    //收货数量
                    Integer receiveQty = detailEntityList.stream().map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    //已签收数量
                    Integer alreadyStockInQty = poInstockDetailEntities.stream().filter(obj -> obj.getSourceDetailId().equals(entity.getId()) && !poInstockIds.contains(obj.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (stockInQty > receiveQty - alreadyStockInQty) {
                        throw new ServiceException(String.format("SKU【%s】实收数量不能超过未入库量", detailEntity.getSkuNo()));
                    }
                    if (alreadyStockInQty >= entity.getReceiveQty()) {
                        continue;
                    }
                    PoInstockDetailDTO.UpdateDTO updateSkuDTO = new PoInstockDetailDTO.UpdateDTO();
                    updateSkuDTO.setSourceDetailId(entity.getId());
                    updateSkuDTO.setPurchaseOrderDetailId(entity.getPurchaseOrderDetailId());
                    updateSkuDTO.setWarehouseLocation(updateDTO.getWarehouseLocation());
                    updateSkuDTO.setSkuNo(updateDTO.getSkuNo());
                    updateSkuDTO.setExceedQty(updateDTO.getExceedQty());
                    updateSkuDTO.setRemark(updateDTO.getRemark());
                    if (stockInQty > (entity.getReceiveQty() - alreadyStockInQty) && !detailEntityList.get(detailEntityList.size() - 1).getId().equals(entity.getId())) {
                        stockInQty = stockInQty - (entity.getReceiveQty() - alreadyStockInQty);
                        updateSkuDTO.setStockInQty(entity.getReceiveQty() - alreadyStockInQty);
                        updateDTOList.add(updateSkuDTO);
                    } else {
                        updateSkuDTO.setStockInQty(stockInQty);
                        updateDTOList.add(updateSkuDTO);
                        break;
                    }
                    updateSkuDTO.setExceedQty(0);
                }
            } else {
                updateDTOList.add(updateDTO);
            }
        }
        dto.setDetails(updateDTOList);
        return this.update(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pdaAddAndSubmit(PoInstockDTO.AddDTO dto) {
        //新增
        String id = this.pdaAdd(dto, Boolean.FALSE);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Collections.singletonList(id));
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaUpdateAndSubmit(PoInstockDTO.UpdateDTO dto) {
        //修改
        this.pdaUpdate(dto);
        //提交
        return this.submit(Collections.singletonList(dto.getId()));
    }

    @Override
    public PoInstockDTO.ViewDTO pdaView(String id) {
        PoInstockDTO.ViewDTO viewDTO = this.view(id);
        //明细信息
        List<PoInstockDetailEntity> entityDetails = poInstockDetailService.listByMainId(id);

        //采购订单明细
        List<String> podIds = entityDetails.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }

        Map<String, PoInstockDetailDTO.ViewDTO> collect = viewDTO.getDetails().stream().collect(Collectors.groupingBy(n -> n.getSkuNo() + "-" + n.getWarehouseLocation(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            Integer purchaseQty = purchaseOrderDetailList.stream().filter(obj -> obj.getSkuNo().equals(m.get(0).getSkuNo())).map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);

//            int purchaseQty = m.stream().mapToInt(PoInstockDetailDTO.ViewDTO::getPurchaseQty).sum();
            int receiveQty = m.stream().mapToInt(PoInstockDetailDTO.ViewDTO::getReceiveQty).sum();
            int exceedQty = m.stream().mapToInt(PoInstockDetailDTO.ViewDTO::getExceedQty).sum();
            int stockInQty = m.stream().mapToInt(PoInstockDetailDTO.ViewDTO::getStockInQty).sum();
            int unStockInQty = m.stream().mapToInt(PoInstockDetailDTO.ViewDTO::getUnStockInQty).sum();
            String podId = m.stream().max(Comparator.comparing(PoInstockDetailDTO.ViewDTO::getId)).map(PoInstockDetailDTO.ViewDTO::getId).get();
            PoInstockDetailDTO.ViewDTO updateDTO = new PoInstockDetailDTO.ViewDTO();
            BeanMapper.copy(m.get(MathUtil.ZERO), updateDTO);
            updateDTO.setId(podId);
            updateDTO.setPurchaseQty(purchaseQty);
            updateDTO.setReceiveQty(receiveQty);
            updateDTO.setStockInQty(stockInQty);
            updateDTO.setExceedQty(exceedQty);
            updateDTO.setUnStockInQty(unStockInQty);
            return updateDTO;
        })));

        List<PoInstockDetailDTO.ViewDTO> viewDTOS = new ArrayList<>();
        for (Map.Entry<String, PoInstockDetailDTO.ViewDTO> stringUpdateDTOEntry : collect.entrySet()) {
            viewDTOS.add(stringUpdateDTOEntry.getValue());
        }
        viewDTO.setDetails(viewDTOS);
        return viewDTO;
    }

    /**
     * 统计数量
     *
     * @param dto
     * @return com.erp.model.wms.dto.PoInstockDTO.PagingTotalDTO
     * @author yl
     * @date 2023-10-24 12:11
     */
    @Override
    public PoInstockDTO.PagingTotalDTO pagingTotal(PoInstockDTO.SearchParamDTO dto) {
        if (CollectionUtils.isNotEmpty(dto.getApproveStatusList())) {
            dto.setInvalidStatus(Boolean.FALSE);
        }
        PoInstockDTO.PagingTotalDTO result = this.baseMapper.pagingTotal(dto);
        return result;
    }


    /**
     * @description: 自动生成委外发料单
     * @author Will
     * @date: 2024/1/12 12:27
     * @param list
     */
    private void autoGenerateSubcontractIssue (List<PoInstockEntity> list) {

        //校验入库单是否是父级委外订单
        List<PoInstockEntity> resultList = list.stream().filter(obj -> SubcontractTypeEnum.ENUM_PARENT.getCode().equals(obj.getSubcontractType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }

        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.SUBCONTRACT_ISSUE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return;
        }
        CfgSettingValueDTO.SubcontractIssueSettingDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.SubcontractIssueSettingDTO.class);
        //不自动生成
        if (CfgSettingCreateTypeEnum.NOT_AUTO_CREATE.getCode().equals(dto.getCreateType())) {
            return;
        }
        //判断是否自动审核
        Boolean isApprove = CfgSettingCreateTypeEnum.AUTO_CREATE_APPROVE.getCode().equals(dto.getCreateType()) ? Boolean.TRUE : Boolean.FALSE;

        List<String> poInIds = resultList.stream().map(PoInstockEntity::getId).collect(Collectors.toList());
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listByMainIds(poInIds);
        if (CollectionUtils.isEmpty(poInstockDetailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }

        //委外订单明细父级SKU信息ids
        List<String> parentPodIds = poInstockDetailList.stream().filter(obj -> poInIds.contains(obj.getMainId())).map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());

        log.info("根据父级SKU采购订单ID查询子级SKU信息，parentPodIds = {}", parentPodIds);
        //采购订单明细信息
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(parentPodIds);

        //采购订单
        List<String> poIdList = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(poIdList);

        //父级委外订单信息
        List<String> subDetailIdList = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listChildSubcontractDetailByIds(subDetailIdList);

        //查询bom信息
        List<String> skuIds = poInstockDetailList.stream().map(PoInstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);

        for (PoInstockEntity poInstockEntity : resultList) {
            //入库明细信息
            List<PoInstockDetailEntity> thisPoDetailList = poInstockDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), poInstockEntity.getId())).collect(Collectors.toList());
            SubcontractIssueDTO.AddDTO addDTO = new SubcontractIssueDTO.AddDTO();

            //采购订单关联的委外订单id
            String sourceId = purchaseOrderList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), poInstockEntity.getPurchaseOrderId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getSourceId())).orElse("");
            addDTO.setSubcontractOrderId(sourceId);
            addDTO.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
            addDTO.setSourceId(poInstockEntity.getId());
            addDTO.setType(SubcontractIssueTypeEnum.NORMAL.getCode());
            addDTO.setDate(LocalDate.now());
            addDTO.setSupplierId(poInstockEntity.getSupplierId());

            for (PoInstockDetailEntity detailEntity : thisPoDetailList) {
                //采购订单明细
                PurchaseOrderDetailEntity poDetailEntity = purchaseOrderDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(poDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                //子级委外订单明细
                List<SubcontractOrderDetailEntity> subDetailList = subcontractOrderDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentId(), poDetailEntity.getSourceDetailId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(subDetailList)) {
                    throw new ServiceException(ApiError.ERROR_98070);
                }
                List<SubcontractIssueDetailDTO.AddDTO> detailList = new ArrayList<>();
                for (SubcontractOrderDetailEntity childSubDetail : subDetailList) {
                    SubcontractIssueDetailDTO.AddDTO addDetailDTO = new SubcontractIssueDetailDTO.AddDTO();
                    addDetailDTO.setSubcontractOrderDetailId(childSubDetail.getId());
                    addDetailDTO.setSourceDetailId(detailEntity.getId());
                    //父级SKU和子级SKU之间的用量
                    Integer quantity = bomList.stream()
                            .filter(obj -> childSubDetail.getBomVersion().equals(obj.getBomVersion()) && obj.getSkuId().equals(childSubDetail.getSkuId()) && obj.getParentSkuId().equals(detailEntity.getSkuId()))
                            .map(BomChildrenSkuDTO::getQuantity).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(quantity)) {
                        throw new ServiceException(ApiError.ERROR_95166);
                    }
                    addDetailDTO.setIssueQty(detailEntity.getStockInQty() * quantity);
                    addDetailDTO.setWarehouseId(childSubDetail.getWarehouseId());
                    addDetailDTO.setWarehouseLocation(childSubDetail.getWarehouseLocation());
                    detailList.add(addDetailDTO);
                }
                addDTO.setDetailList(detailList);
                //自动新增
                SubcontractIssueDTO.AutoAddDTO autoAddDTO = new SubcontractIssueDTO.AutoAddDTO(addDTO, isApprove);
                subcontractIssueService.autoAdd(autoAddDTO);
            }
        }
    }

    /**
     * @description: 更新采购订单执行状态
     * @author Will
     * @date: 2024/3/22 11:56
     * @param ids
     */
    private void updatePodArrivalState (List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listByMainIds(ids);
        List<String> podIds = poInstockDetailList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(podIds)) {
            //修改到货状态
            poReturnService.updateArrivalState(podIds);
        }
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<PoInstockEntity> list,String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeStockInService.syncDataToKingdee(obj, operate);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submitEntity(PoInstockEntity entity) {
        submitList(Collections.singletonList(entity.getId()), Collections.singletonList(entity));
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO deleteEntity(PoInstockEntity entity) {
        //待提交允许删除
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        log.info("采购入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        poInstockDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的采购入库单", UserContext.getDefaultLoginUser().getUserName(), Stream.of(entity).map(PoInstockEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "删除操作");
        //删除发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        boolean update = this.removeByIds(ids);
        if (update){
           return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        } else {
           return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO invalidEntity(PoInstockEntity entity, String reason) {
        //非待提交和审核不通过不能作废
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = Stream.of(entity).filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("采购入库单作废，ids=【{}】", JSONUtil.toJsonStr(entity.getId()));

        //更新
        lambdaUpdate().in(PoInstockEntity::getId, entity.getId())
                .set(PoInstockEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PoInstockEntity::getInvalidTime, LocalDateTime.now())
                .set(PoInstockEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个采购入库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "作废操作");

        //作废发送金蝶
        sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_INVALID.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(PoInstockEntity entity) {
        //审核中允许审核
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购入库单撤销流程，id=【{}】", entity.getId());

        //撤销现有流程
        workflowFeign.cancelProcess(Collections.singletonList(entity.getId()));

        //更新单据为待提交
        updateApproveStatus(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("采购入库单【%s】取消流程", ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "取消流程操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    public List<PoInstockDTO.PoInStockInfoDTO> getPoStockInByParams(PoInstockDTO.PoInStockParamDTO dto) {
        if(Objects.isNull(dto) || CollUtil.isEmpty(dto.getSkuIds())){
            return Collections.emptyList();
        }
        return baseMapper.getPoStockInByParams(dto);
    }
}
