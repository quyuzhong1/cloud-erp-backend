package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
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
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.PurchaseStockExportExcelDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购入库单 服务实现类
 *
 * @author will
 * @since 2023-04-10
 */
@Slf4j
@Service
public class PurchaseStockInServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStockInEntity> implements PurchaseStockInService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Override
    public PagingVO<PurchaseStockInDTO.ListDTO> paging(PagingDTO<PurchaseStockInDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseStockInDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseStockInDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandlePurchaseStockIn(records);
        List<String> list = new ArrayList<>();
        //清空明细数据
        records.forEach(obj -> {
            boolean contains = list.contains(obj.getId());
            if (contains) {
                obj.setCode(null);
                obj.setSupplierName(null);
                obj.setDeliveryWarehouseName(null);
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }


    @Override
    public List<PurchaseStockInDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<PurchaseStockInDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            PurchaseStockInDTO.SearchParamDTO searchParamDTO = new PurchaseStockInDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            PurchaseStockInDTO.ListStatusCountDTO resultDTO = new PurchaseStockInDTO.ListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(PurchaseStockInDTO.AddDTO dto) {
        PurchaseStockInEntity entity = new PurchaseStockInEntity();
        BeanMapperUtils.copy(dto, entity);
        //添加采购订单默认值
        addDefaultPurchaseData(dto.getPurchaseOrderId(), entity);
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(), dto.getStockInUserId(), dto.getDeliveryWarehouseId(), entity);
        log.info("采购入库单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGRK, BusinessNoTypeEnum.CODE_CGRK.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购入库单【%s】", code), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), entity.getId(), "新增操作");
            //新增明细
            purchaseStockInDetailService.add(dto.getDetails(), entity.getId(), dto.getSourceType());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseStockInDTO.UpdateDTO dto) {
        PurchaseStockInEntity entity = new PurchaseStockInEntity();
        BeanMapperUtils.copy(dto, entity);
        List<PurchaseStockInDetailDTO.UpdateDTO> details = dto.getDetails();
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(), dto.getStockInUserId(), dto.getDeliveryWarehouseId(), entity);

        log.info("采购入库单修改，id=【{}】", dto.getId());

        //添加日志
        PurchaseStockInEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseStockInDetailService.update(details, entity.getId(), old.getSourceType());
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(PurchaseStockInDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        return id;
    }

    /**
     * 当质检单 质检类型为b2b 是
     * 批量生成入库单
     * @author yl
     * @date 2023-04-24 15:08
     * @param list
     * @return java.lang.Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<PurchaseStockInDTO.AddDTO> list) {

        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseStockInDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("采购入库单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购入库单【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public PurchaseStockInDTO.ViewDTO view(String id) {
        PurchaseStockInDTO.ViewDTO dto = new PurchaseStockInDTO.ViewDTO();

        //主表信息
        PurchaseStockInEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        BeanMapperUtils.copy(entity, dto);

        //明细信息
        List<PurchaseStockInDetailEntity> entityDetails = purchaseStockInDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98002);
        }
        List<PurchaseStockInDetailDTO.ViewDTO> details = BeanMapperUtils.copyList(PurchaseStockInDetailDTO.ViewDTO.class, entityDetails);
        List<String> skuIds = entityDetails.stream().map(PurchaseStockInDetailEntity::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        //采购订单明细
        List<String> podIds = entityDetails.stream().map(PurchaseStockInDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //收货单明细
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);

        details.forEach(obj -> {
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                obj.setProductName(productName);
            }
            if (CollectionUtils.isNotEmpty(purchaseOrderDetailList)) {
                Integer purchaseQty = purchaseOrderDetailList.stream().filter(e -> e.getId().equals(obj.getPurchaseOrderDetailId())).map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setPurchaseQty(purchaseQty);
            }

            if (CollectionUtils.isNotEmpty(entityDetails)) {
                Integer hasStockQty = entityDetails.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId()) && !obj.getId().equals(e.getId())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setHasStockInQty(hasStockQty);
                obj.setUnStockInQty(obj.getPurchaseQty() - hasStockQty);
            }

            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                Integer receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
            }

        });
        dto.setDetails(details);

        //查询采购供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = scmTaskFeign.getOrderSupplierByOrderId(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        PurchaseStockInDTO.SupplierDTO supplierDTO = new PurchaseStockInDTO.SupplierDTO();
        supplierDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
        supplierDTO.setSupplierContactId(purchaseOrderSupplierEntity.getSupplierContactId());

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
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("采购入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        purchaseStockInDetailService.removeByMainIds(ids);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
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
        lambdaUpdate().in(PurchaseStockInEntity::getId, ids)
                .set(PurchaseStockInEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PurchaseStockInEntity::getInvalidTime, LocalDateTime.now())
                .set(PurchaseStockInEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个采购入库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        String type = baseApproveParamDTO.getType();

        log.info("采购入库单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购入库单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), pairList, "审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        //判断是否已经下推退货单
        List<PurchaseReturnOrderEntity> purchaseReturnOrderList = purchaseReturnOrderService.listBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(purchaseReturnOrderList)) {
            throw new ServiceException(ApiError.ERROR_99014);
        }

        log.info("采购入库单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个采购入库单【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购入库单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购入库单【%s】取消流程", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<PurchaseStockInDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        doOpHandlePurchaseStockIn(list);
        List<PurchaseStockExportExcelDTO> resultList = BeanMapperUtils.copyList(PurchaseStockExportExcelDTO.class, list);

        String fileName = "采购入库单数据";
        try {
            ExcelUtil.export(fileName, "采购入库单数据", resultList, PurchaseStockExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = baseMapper.viewGeneratePurchaseReturnOrder(ids);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> skuIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            return list;
        }
        List<String> podIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }

        List<String> resultIds = new ArrayList<>();
        for (PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO dto : list) {
            //来源类型
            dto.setSourceType(SourceTypeEnum.PURCHASE_RETURN_ORDER.getType());
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
            dto.setProductName(productName);

            //币种符号
            String currencySymbol = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(dto.getPurchaseOrderDetailId())).map(PurchaseOrderDetailEntity::getCurrencySymbol).findFirst().orElse(null);
            dto.setCurrencySymbol(currencySymbol);
            //相同采购单号清空后面数据的采购单号和供应商
            boolean contains = list.contains(dto.getPurchaseOrderId());
            if (contains) {
                dto.setPurchaseOrderCode(null);
                dto.setSupplierName(null);
                continue;
            }
            resultIds.add(dto.getPurchaseOrderId());
        }
        return list;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        List<PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO> list = dto.getList();
        //查询实退数量
        List<String> sourceIds = list.stream().map(PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO::getSourceId).collect(Collectors.toList());
        List<String> sourceDetailIds = list.stream().map(PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO::getSourceDetailId).collect(Collectors.toList());
        //来源单据为采购入库单
        List<PurchaseStockInEntity> sourceList = this.listByIds(sourceIds);
        if (CollectionUtils.isEmpty(sourceList)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        long count = sourceList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99012);
        }

        List<PurchaseStockInDetailEntity> sourceDetailList = purchaseStockInDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(sourceDetailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }
        List<PurchaseReturnOrderDTO.AddDTO> addList = new ArrayList<>();

        Map<String, List<PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO::getSourceId));
        for (Map.Entry<String, List<PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO>> entry : map.entrySet()) {
            String sourceId = entry.getKey();
            List<PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO> value = entry.getValue();
            PurchaseReturnOrderDTO.AddDTO addDTO = new PurchaseReturnOrderDTO.AddDTO();
            //采购单
            PurchaseStockInEntity purchaseStockInEntity = sourceList.stream().filter(obj -> obj.getSourceId().equals(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseStockInEntity)) {
                throw new ServiceException(ApiError.ERROR_98050);
            }
            BeanMapperUtils.copy(purchaseStockInEntity, addDTO);
            addDTO.setSourceType(value.get(0).getSourceType());
            addDTO.setSourceId(sourceId);
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO detail : value) {
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
                addDetailList.add(addDetailDTO);
            }
            addDTO.setPurchasePriceDetailList(addDetailList);
            addList.add(addDTO);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            addList.forEach(obj -> purchaseReturnOrderService.add(obj));
        }

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddPurchaseStockIn(List<PurchaseStockInDTO.AddDTO> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return Boolean.FALSE;
        }
        resultList.forEach(obj -> add(obj));
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
    public List<PurchaseStockInEntity> getStockInBySourceId(String sourceId) {
        return lambdaQuery().eq(PurchaseStockInEntity::getSourceId, sourceId).list();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(PurchaseStockInEntity::getId, ids)
                .set(PurchaseStockInEntity::getApproveUserId, userInfo.getUid())
                .set(PurchaseStockInEntity::getApproveUserName, userInfo.getUserName())
                .set(PurchaseStockInEntity::getApproveStatus, approveStatus)
                .set(PurchaseStockInEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(PurchaseStockInEntity::getId, ids)
                .set(PurchaseStockInEntity::getApproveStatus, approveStatus)
                .set(PurchaseStockInEntity::getApproveUserId, "")
                .set(PurchaseStockInEntity::getApproveUserName, "")
                .set(PurchaseStockInEntity::getApproveTime, null)
                .update();
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseStockInEntity::getId, ids)
                .set(PurchaseStockInEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseStockInEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseStockInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        return list;
    }

    /**
     * 新增添加默认值
     */
    private void addDefaultPurchaseData(String purchaseOrderId, PurchaseStockInEntity entity) {
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
    private void doOpHandleDataId(String stockInDeptId, String stockInUserId, String deliveryWarehouseId, PurchaseStockInEntity entity) {

        //入库员
        if (StringUtils.isNotBlank(stockInUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(stockInUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setStockInUserName(userDTO.getUserName());
        }
        //入库部门
        if (StringUtils.isNotBlank(stockInDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(stockInDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setStockInDeptName(depart.getName());
        }
        //仓库
        if (StringUtils.isNotBlank(deliveryWarehouseId)) {
            //仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(deliveryWarehouseId));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDeliveryWarehouseName(warehouseName);
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
    public List<PurchaseStockInDTO.GetStockInQty> getStockInQty(List<String> ids) {
        return baseMapper.getStockInQty(ids);
    }

    @Override
    public List<PurchaseStockInDTO.OrderRefStockInDTO> purchaseOrderRefStockIn(String purchaseOrderId) {
        List<PurchaseStockInDTO.OrderRefStockInDTO> list = baseMapper.purchaseOrderRefStockIn(purchaseOrderId);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        //获取采购单详情表id集合
        List<String> skuIds = list.stream().map(PurchaseStockInDTO.OrderRefStockInDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
        for (PurchaseStockInDTO.OrderRefStockInDTO dto : list) {
            dto.setApproveStatusName(ApproveStatusEnum.getName(dto.getApproveStatus()));
            dto.setInvalidStatusName(InvalidStatusEnum.getName(dto.getInvalidStatus()));
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(dto.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            dto.setProductName(productName);
        }
        return list;
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandlePurchaseStockIn(List<PurchaseStockInDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(PurchaseStockInDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);

        //采购入库明细ids
        List<String> podIds = records.stream().map(PurchaseStockInDTO.ListDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);

        for (PurchaseStockInDTO.ListDTO obj : records) {
            //产品名称
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                obj.setProductName(productName);
            }
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                Integer receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReceiveQty(receiveQty);
            }
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
        }
    }
}
