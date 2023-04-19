package com.erp.server.scm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchaseOrderExportExcelDTO;
import com.erp.model.scm.dto.excel.PurchaseOrderImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.listener.PurchaseOrderExcelListener;
import com.erp.server.scm.mapper.PurchaseOrderMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购订单表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@Service
public class PurchaseOrderServiceImpl extends SuperServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity> implements PurchaseOrderService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private CommonService commonService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> paging(PagingDTO<PurchaseOrderDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<PurchaseOrderDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> podIds = records.stream().map(PurchaseOrderDTO.ListDTO::getPurchaseDetailId).collect(Collectors.toList());
            //入库数量
            List<PurchaseStockInDetailEntity> purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                obj.setArrivalStatusName(ArrivalStatusEnum.getNameByCode(obj.getArrivalStatus()));
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
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));


                //获取签收数量
                List<WarehouseReceiveDTO.GetReceiveDTO> receiveQtyList = wmsTaskFeign.getReceiveQty(obj.getId());
                WarehouseReceiveDTO.GetReceiveDTO getReceiveDTO = receiveQtyList.stream().filter(req -> req.getSkuId().equals(obj.getSkuId()) && req.getPurchaseOrderId().equals(obj.getId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(getReceiveDTO)) {
                    obj.setReceiveQty(0);
                } else {
                    obj.setReceiveQty(getReceiveDTO.getReceiveQty());
                }

                //获取待交货数量
                WarehouseReceiveDTO.GetReceiveDTO getDeliveryQty = receiveQtyList.stream().filter(req -> req.getSkuId().equals(obj.getSkuId()) && req.getPurchaseOrderId().equals(obj.getId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(getReceiveDTO)) {
                    obj.setDeliveryQty(0);
                } else {
                    obj.setDeliveryQty(obj.getPurchaseQty() - getDeliveryQty.getReceiveQty());
                }

                //入库数量
                Integer stockInQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                    stockInQty = purchaseStockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus()) ).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO,Integer::sum);
                }
                obj.setStockInQty(stockInQty);
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(PurchaseOrderDTO.AddDTO dto) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto, entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getPurchaseUserId(), dto.getPurchaseDeptId(), dto.getPurchaseOrgId(), dto.getReceiveOrgId(), dto.getDeliveryWarehouseId(), entity);
        log.info("采购订单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PO, BusinessNoTypeEnum.CODE_PO.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购订单【%s】", code), ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "新增操作");
            //新增供应商信息
            purchaseOrderSupplierService.add(dto.getPurchaseOrderSupplierDTO(), entity.getId());
            //新增明细
            purchaseOrderDetailService.add(dto.getDetails(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseOrderDTO.UpdateDTO dto) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto, entity);
        //校验明细是否有重复sku
        checkUpdateDetailsRepeatSku(dto.getDetails(), dto.getId());
        //处理数据id
        doOpHandleDataId(dto.getPurchaseUserId(), dto.getPurchaseDeptId(), dto.getPurchaseOrgId(), dto.getReceiveOrgId(), dto.getDeliveryWarehouseId(), entity);

        log.info("采购订单修改，id=【{}】", dto.getId());

        //操作日志
        PurchaseOrderEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //供应商数据
        purchaseOrderSupplierService.update(dto.getPurchaseOrderSupplierDTO(), entity.getId());
        //更新明细数据
        purchaseOrderDetailService.update(dto.getDetails(), entity.getId());
        return Boolean.TRUE;
    }

    @Override
    public PurchaseOrderDTO.ViewDTO view(String id) {
        PurchaseOrderDTO.ViewDTO dto = new PurchaseOrderDTO.ViewDTO();

        //主表信息
        PurchaseOrderEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        BeanMapperUtils.copy(entity, dto);

        //供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        PurchaseOrderSupplierDTO.UpdateDTO supplierUpdateDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        if (ObjectUtils.isEmpty(supplierUpdateDTO)) {
            throw new ServiceException(ApiError.ERROR_98031);
        }
        BeanMapperUtils.copy(purchaseOrderSupplierEntity, supplierUpdateDTO);
        dto.setPurchaseOrderSupplierDTO(supplierUpdateDTO);

        //明细信息
        List<PurchaseOrderDetailEntity> entityDetails = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<PurchaseOrderDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(PurchaseOrderDetailDTO.UpdateDTO.class, entityDetails);
        details.forEach(obj -> obj.setTaxRate(MathUtil.multiply(obj.getTaxRate(), MathUtil.BigDecimal_100)));
        dto.setDetails(details);
        //流程信息
        List<PurchaseOrderProcessDTO> processList = new ArrayList<>();
        PurchaseOrderProcessOperationEnum[] values = PurchaseOrderProcessOperationEnum.values();
        for (PurchaseOrderProcessOperationEnum item : values) {
            PurchaseOrderProcessDTO processDTO = new PurchaseOrderProcessDTO();
            processDTO.setOperation(item.getName());
            processDTO.setIsArrive(Boolean.FALSE);
            //创建
            if (PurchaseOrderProcessOperationEnum.CREATE.getCode().equals(item.getCode())) {
                processDTO.setUserName(entity.getCreateUserName());
                processDTO.setTime(entity.getCreateTime());
                processDTO.setIsArrive(Boolean.TRUE);
            }
            //审核
            if (PurchaseOrderProcessOperationEnum.APPROVE.getCode().equals(item.getCode())) {
                if (StringUtils.isNotBlank(entity.getApproveUserName())) {
                    processDTO.setIsArrive(Boolean.TRUE);
                }
                processDTO.setUserName(entity.getApproveUserName());
                processDTO.setTime(entity.getApproveTime());
            }
            //签收、签收完成 TODO

            processList.add(processDTO);
        }
        dto.setProcess(processList);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        log.info("采购申请单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除供应商数据
        purchaseOrderSupplierService.deleteByPurchaseOrderIds(ids);
        //删除明细数据
        purchaseOrderDetailService.removeByPurchaseOrderIds(ids);
        //删除主表数据
        this.removeByIds(ids);
        //更新采购申请单的生成状态
        updateCreatePoType(ids);
        //删除采购申请单和订单关联表数据
        purchaseApplicationRefPoService.removeByPurchaseOrderIds(ids);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = baseApproveParamDTO.getType();

        log.info("采购订单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO

            //更新单据状态(后面有流程了可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
        }
        //审核不通过
        if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购订单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);
        //审核中和已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("采购订单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));
        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个采购订单【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购申请单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购申请单【%s】取消流程", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishDelivery(List<String> ids) {
        //ids为采购订单明细id集合
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        long count = purchaseOrderDetailList.stream().filter(obj -> !ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode().equals(obj.getArrivalStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98035);
        }
        //查询签收单数量放些明细中的采购数量 TODO

        //更新明细中的交货状态
        purchaseOrderDetailService.updateArrivalStatusByIds(ArrivalStatusEnum.ARRIVED.getCode(), ids);
        return Boolean.TRUE;
    }

    @Override
    public PurchaseOrderDTO.ExportPdfDTO exportPurchaseContractPdf(String id) {
        PurchaseOrderDTO.ExportPdfDTO exportPdfDTO = new PurchaseOrderDTO.ExportPdfDTO();

        PurchaseOrderEntity purchaseOrderEntity = this.getById(id);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }

        if (ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(purchaseOrderEntity.getApproveStatus()) || ApproveStatusEnum.REJECT.getStatus().equals(purchaseOrderEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98038);
        }

        List<PurchaseOrderDetailEntity> list = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //主数据处理
        exportPdfDTO.setCode(purchaseOrderEntity.getCode());
        //采购组织
        exportPdfDTO.setPurchaseOrgName(purchaseOrderEntity.getPurchaseOrgName());
        //甲方签收日期
        exportPdfDTO.setFirstSignDate(purchaseOrderEntity.getCreateTime().toLocalDate());
        //乙方签收日期
        exportPdfDTO.setSecondSignDate(purchaseOrderEntity.getCreateTime().toLocalDate());

        //查询订单供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplier = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        if (ObjectUtils.isEmpty(purchaseOrderSupplier)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        exportPdfDTO.setSupplierTel(purchaseOrderSupplier.getContactTelNumber());

        //结算方式
        DictBasicEntity payMethod = dictBasicService.getById(purchaseOrderSupplier.getPayMethodId());
        if (ObjectUtils.isNotEmpty(payMethod)) {
            exportPdfDTO.setPayMethodName(payMethod.getName());
        }

        //原供应商信息
        SupplierEntity supplier = supplierService.getById(purchaseOrderSupplier.getSupplierId());
        if (ObjectUtils.isEmpty(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        exportPdfDTO.setSupplierName(supplier.getName());
        exportPdfDTO.setSupplierAddress(supplier.getCompanyAddress());

        //供应商联系人信息
        if (StringUtils.isNotBlank(purchaseOrderSupplier.getSupplierContactId())) {
            SupplierContactEntity supplierContact = supplierContactService.getById(purchaseOrderSupplier.getSupplierContactId());
            if (ObjectUtils.isEmpty(supplierContact)) {
                throw new ServiceException(ApiError.ERROR_98039);
            }
            exportPdfDTO.setSupplierEmail(supplierContact.getEmail());
        }

        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(purchaseOrderEntity.getDeliveryWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.get(0);
        exportPdfDTO.setDeliveryWarehouseAddress(warehouseDTO.getAddress());
        exportPdfDTO.setDeliveryWarehouseTel(warehouseDTO.getContactTelNumber());
        exportPdfDTO.setDeliveryWarehouseContract(warehouseDTO.getContacts());

        //明细物料信息
        List<PurchaseOrderDetailDTO.ExportPdfDTO> details = new ArrayList<>();
        for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : list) {
            PurchaseOrderDetailDTO.ExportPdfDTO detailDTO = new PurchaseOrderDetailDTO.ExportPdfDTO();
            BeanMapperUtils.copy(purchaseOrderDetailEntity, detailDTO);
            //明细数据处理
            detailDTO.setUnitName("个");
            detailDTO.setTaxRate(MathUtil.multiply(detailDTO.getTaxRate(), MathUtil.BigDecimal_100));
            details.add(detailDTO);
        }
        BigDecimal totalAmount = details.stream().map(PurchaseOrderDetailDTO.ExportPdfDTO::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        exportPdfDTO.setTotalAmount(totalAmount);
        exportPdfDTO.setCurrency(list.get(0).getCurrency());
        exportPdfDTO.setDetails(details);
        return exportPdfDTO;
    }

    @Override
    public PurchaseOrderDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, String supplierId, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();

        PurchaseOrderExcelListener excelListenerUtil = new PurchaseOrderExcelListener(skuList, skuIds);

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseOrderImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        PurchaseOrderDetailDTO.ImportDTO importDTO = new PurchaseOrderDetailDTO.ImportDTO();
        //导入数据处理
        List<PurchaseOrderDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseOrderImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理未查询到报价的SKU
        doOpHandleNotExistPrice(excelDateList, successList, errorList, supplierId);

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购订单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchaseOrderImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public Boolean exportExcel(PurchaseOrderDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<PurchaseOrderExportExcelDTO> resultList = baseMapper.listExportExcel(dto);
        String fileName = "采购订单数据";
        try {
            ExcelUtil.export(fileName, "采购订单数据", resultList, PurchaseOrderExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        log.info("采购订单提交，ids=【{}】", JSONUtil.toJsonStr(ids));
        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购订单【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchaseOrderDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseOrderDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public List<ListStatusCountDTO.PurchaseOrderCountDTO> listCount(PermissionsDTO dto) {
        PurchaseListTypeEnum[] values = PurchaseListTypeEnum.values();
        List<ListStatusCountDTO.PurchaseOrderCountDTO> list = new ArrayList<>();
        for (PurchaseListTypeEnum item : values) {
            PurchaseOrderDTO.SearchParamDTO searchParamDTO = new PurchaseOrderDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            ListStatusCountDTO.PurchaseOrderCountDTO resultDTO = new ListStatusCountDTO.PurchaseOrderCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseListTypeEnum.TO_BE_CREATE.getCode().equals(item.getCode())) {
                searchParamDTO.setArrivalStatusList(Arrays.asList(ArrivalStatusEnum.NON_ARRIVAL.getCode(), ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()));
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseListTypeEnum.CREATED.getCode().equals(item.getCode())) {
                searchParamDTO.setArrivalStatusList(Arrays.asList(ArrivalStatusEnum.ARRIVED.getCode()));
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseListTypeEnum.REJECT.getCode().equals(item.getCode())) {
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("采购订单作废，ids=【{}】", JSONUtil.toJsonStr(ids));
        //更新订单作废状态
        updateInvalidStatus(ids, reason);
        //更新采购申请单的生成状态
        updateCreatePoType(ids);
        //删除采购申请单和订单关联表数据
        purchaseApplicationRefPoService.removeByPurchaseOrderIds(ids);
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个采购订单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }


    @Override
    public List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(List<String> ids) {
        //采购订单主表信息
        List<PurchaseOrderEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //采购订单明细信息
        List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailService.listByPurchaseOrderIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //供应商信息
        List<PurchaseOrderSupplierEntity> purchaseOrderSupplierList = purchaseOrderSupplierService.listByPurchaseOrderIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderSupplierList)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }

        LoginUser userInfo = commonService.getUserInfo();
        List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceiveDTOS = baseMapper.viewGenerateReceive(ids);

        List<String> purchaseOrderIdList = viewGenerateReceiveDTOS.stream().map(PurchaseOrderDTO.ViewGenerateReceiveDTO::getId).collect(Collectors.toList());

        List<PurchaseStockInDTO.GetStockInQty> stockInQtyList = wmsTaskFeign.getStockInQty(purchaseOrderIdList);

        //获取sku的id集合
        List<String> skuIdList = viewGenerateReceiveDTOS.stream().map(PurchaseOrderDTO.ViewGenerateReceiveDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (PurchaseOrderDTO.ViewGenerateReceiveDTO viewGenerateReceiveDTO : viewGenerateReceiveDTOS) {
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(req -> req.getId().equals(viewGenerateReceiveDTO.getSkuId())).findFirst().orElse(null);
            viewGenerateReceiveDTO.setProductName(productDetailEntity.getName());
            viewGenerateReceiveDTO.setPlanDeliveryDate(LocalDate.now());
            viewGenerateReceiveDTO.setReceiveUserId(userInfo.getUid());
            viewGenerateReceiveDTO.setReceiveUserName(userInfo.getUserName());
            //获取签收数量
            List<WarehouseReceiveDTO.GetReceiveDTO> receiveQtyList = wmsTaskFeign.getReceiveQty(viewGenerateReceiveDTO.getId());
            WarehouseReceiveDTO.GetReceiveDTO getReceiveDTO = receiveQtyList.stream().filter(obj -> obj.getSkuId().equals(viewGenerateReceiveDTO.getSkuId()) && obj.getPurchaseOrderId().equals(viewGenerateReceiveDTO.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(getReceiveDTO)) {
                viewGenerateReceiveDTO.setReceiveQty(0);
            } else {
                viewGenerateReceiveDTO.setReceiveQty(getReceiveDTO.getReceiveQty());
            }
            viewGenerateReceiveDTO.setUnReceiveQty(viewGenerateReceiveDTO.getPurchaseQty() - viewGenerateReceiveDTO.getReceiveQty());
            viewGenerateReceiveDTO.setExceedQty(0);
        }

        return viewGenerateReceiveDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateReceive(PurchaseOrderDTO.ListGenerateReceiveDTO dto) {
        //生成下推签收单
        List<PurchaseOrderDTO.GenerateReceiveDTO> list = dto.getList();
        //采购订单明细Ids
        List<String> purchaseOrderDetailIds = list.stream().map(PurchaseOrderDTO.GenerateReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        long arrivalStatusCount = purchaseOrderDetailList.stream().filter(obj -> ArrivalStatusEnum.ARRIVED.getCode().equals(obj.getArrivalStatus())).count();
        if (arrivalStatusCount > 0) {
            throw new ServiceException(ApiError.ERROR_98041);
        }

        List<String> purchaseOrderIds = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = this.listByIds(purchaseOrderIds);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        long approveStatusCount = purchaseOrderList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (approveStatusCount > 0) {
            throw new ServiceException(ApiError.ERROR_98040);
        }
        List<WarehouseReceiveDTO.AddDTO> addDTOS = BeanMapperUtils.copyList(WarehouseReceiveDTO.AddDTO.class, list);
        for (WarehouseReceiveDTO.AddDTO addDTO : addDTOS) {
            PurchaseOrderDTO.GenerateReceiveDTO generateReceiveDTO = list.stream().filter(req -> req.getId().equals(addDTO.getPurchaseOrderId())).findFirst().orElse(null);
            addDTO.setPurchaseOrderId(generateReceiveDTO.getId());
            addDTO.setPurchaseOrderCode(generateReceiveDTO.getCode());
            addDTO.setReceiveUserId(generateReceiveDTO.getReceiveUserId());
            SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(generateReceiveDTO.getReceiveUserId());
            addDTO.setReceiveDeptId(deptByUserId.getDepartmentId());
            addDTO.setBillDate(LocalDate.now());
            addDTO.setDeliveryWarehouseId(addDTO.getDeliveryWarehouseId());
            List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = addDTO.getWarehouseReceiveDetailList();
            for (WarehouseReceiveDetailDTO.AddDTO detailAddDTO : warehouseReceiveDetailList) {
                detailAddDTO.setReceiveQty(generateReceiveDTO.getReceiveQty());
                detailAddDTO.setExceedQty(generateReceiveDTO.getExceedQty());
                detailAddDTO.setRemark(generateReceiveDTO.getRemark());
                detailAddDTO.setPurchaseOrderDetailId(generateReceiveDTO.getPurchaseOrderDetailId());
            }
            wmsTaskFeign.addWarehouseReceive(addDTO);
        }
        return Boolean.TRUE;
    }

    @Override
    public PurchaseChangeDTO.ViewDTO viewPurchaseChange(String id) {
        PurchaseChangeDTO.ViewDTO viewDTO = new PurchaseChangeDTO.ViewDTO();
        //采购主表信息
        PurchaseOrderEntity purchaseOrderEntity = this.getById(id);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        viewDTO.setPurchaseOrderId(purchaseOrderEntity.getId());
        viewDTO.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
        viewDTO.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        viewDTO.setIsFirstMassProduct(purchaseOrderEntity.getIsFirstMassProduct());
        viewDTO.setDeliveryWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());
        //采购供应商信息
        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        PurchaseOrderSupplierDTO.UpdateDTO supplierDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        BeanMapperUtils.copy(supplierEntity, supplierDTO);
        viewDTO.setSupplierDTO(supplierDTO);
        viewDTO.setSupplierId(supplierEntity.getSupplierId());
        //采购订单明细
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<PurchaseChangeDetailDTO.UpdateDTO> detailDTOList = new ArrayList<>();
        for (PurchaseOrderDetailEntity detailEntity : purchaseOrderDetailList) {
            PurchaseChangeDetailDTO.UpdateDTO detailDTO = new PurchaseChangeDetailDTO.UpdateDTO();
            detailDTO.setPurchaseOrderDetailId(detailEntity.getId());
            detailDTO.setSkuId(detailEntity.getSkuId());
            detailDTO.setSkuNo(detailEntity.getSkuNo());
            detailDTO.setProductName(detailEntity.getProductName());
            detailDTO.setCurrency(detailEntity.getCurrency());
            detailDTO.setCurrencySymbol(detailEntity.getCurrencySymbol());
            detailDTO.setOldQty(detailEntity.getPurchaseQty());
            detailDTO.setOldPrice(detailEntity.getTaxPrice());
            detailDTO.setOldAmount(detailEntity.getPurchaseAmount());
            detailDTOList.add(detailDTO);
        }
        viewDTO.setDetails(detailDTOList);
        return viewDTO;
    }

    @Override
    public PurchaseOrderDTO.AssociatedDocumentDTO viewAssociatedDocuments(BaseIdDTO dto) {
        PurchaseOrderDTO.AssociatedDocumentDTO resultDTO = new PurchaseOrderDTO.AssociatedDocumentDTO();
        //采购变更单
        List<PurchaseChangeDTO.ListDTO> purchaseChangeList = purchaseChangeService.list(dto);
        resultDTO.setPurchaseChangeList(purchaseChangeList);
        return resultDTO;
    }

    @Override
    public List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(List<String> ids) {
        List<PurchaseOrderDTO.ViewGenerateStockInDTO> resultList = new ArrayList<>();
        List<PurchaseOrderDetailEntity> list = purchaseOrderDetailService.listByPurchaseOrderIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //订单供应商
        List<PurchaseOrderSupplierEntity> supplierList = purchaseOrderSupplierService.listByPurchaseOrderIds(ids);
        if (CollectionUtils.isEmpty(supplierList)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }

        List<String> podIds = list.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);

        //入库信息
        List<PurchaseStockInDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailBySourceDetailIds(podIds);

        for (PurchaseOrderDetailEntity detailEntity : list) {
            PurchaseOrderDTO.ViewGenerateStockInDTO viewGenerateStockInDTO = new PurchaseOrderDTO.ViewGenerateStockInDTO();
            BeanMapperUtils.copy(detailEntity,viewGenerateStockInDTO);
            viewGenerateStockInDTO.setPurchaseOrderDetailId(detailEntity.getId());
            //供应商信息
            PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = supplierList.stream().filter(obj -> obj.getPurchaseOrderId().equals(detailEntity.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
                throw new ServiceException(ApiError.ERROR_98036);
            }
            viewGenerateStockInDTO.setSupplierName(purchaseOrderSupplierEntity.getSupplierName());
            //收货数量
            Integer receiveQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                receiveQty = receiveDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewGenerateStockInDTO.setReceiveQty(receiveQty);
            //未入库数量
            Integer unStockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                unStockInQty = stockInDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewGenerateStockInDTO.setUnStockInQty(detailEntity.getPurchaseQty() - unStockInQty);
            resultList.add(viewGenerateStockInDTO);
        }
        return resultList;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean generateStockIn(PurchaseOrderDTO.ListGenerateStockInDTO dto) {
        //保存信息
        List<PurchaseOrderDTO.GenerateStockInDTO> list = dto.getList();
        List<String> ids = list.stream().map(PurchaseOrderDTO.GenerateStockInDTO::getPurchaseOrderId).collect(Collectors.toList());

        //订单信息集合
        List<PurchaseOrderEntity> purchaseOrderList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            log.error("未找到订单信息，ids={}",JSONUtil.toJsonStr(ids));
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //订单明细信息集合
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByPurchaseOrderIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            log.error("未找到订单明细信息，ids={}",JSONUtil.toJsonStr(ids));
            throw new ServiceException(ApiError.ERROR_98026);
        }

        Map<String, List<PurchaseOrderDTO.GenerateStockInDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDTO.GenerateStockInDTO::getPurchaseOrderId));
        List<PurchaseStockInDTO.AddDTO> resultList = new ArrayList<>();
        for (Map.Entry<String, List<PurchaseOrderDTO.GenerateStockInDTO>> entry : map.entrySet()) {
            PurchaseStockInDTO.AddDTO addDTO = new PurchaseStockInDTO.AddDTO();
            String purchaseOrderId = entry.getKey();
            List<PurchaseOrderDTO.GenerateStockInDTO> value = entry.getValue();

            //订单信息
            PurchaseOrderEntity entity = purchaseOrderList.stream().filter(obj -> obj.getId().equals(purchaseOrderId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                log.error("未找到订单明细信息，id={}",purchaseOrderId);
                throw new ServiceException(ApiError.ERROR_98025);
            }
            addDTO.setPurchaseOrderId(purchaseOrderId);
            addDTO.setSourceId(purchaseOrderId);
            addDTO.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getType());
            addDTO.setDeliveryWarehouseId(entity.getDeliveryWarehouseId());
            addDTO.setStockInDeptId(entity.getPurchaseDeptId());
            addDTO.setStockInUserId(entity.getPurchaseUserId());
            List<PurchaseStockInDetailDTO.AddDTO> details = new ArrayList<>();
            for (PurchaseOrderDTO.GenerateStockInDTO generateStockInDTO : value) {
                PurchaseStockInDetailDTO.AddDTO addDetailDTO = new PurchaseStockInDetailDTO.AddDTO();
                addDetailDTO.setSourceDetailId(generateStockInDTO.getPurchaseOrderDetailId());
                addDetailDTO.setPurchaseOrderDetailId(generateStockInDTO.getPurchaseOrderDetailId());
                addDetailDTO.setStockInQty(generateStockInDTO.getStockInQty());
                addDetailDTO.setExceedQty(generateStockInDTO.getExceedQty());
                details.add(addDetailDTO);
            }
            addDTO.setDetails(details);
            resultList.add(addDTO);
        }
        Boolean add = wmsTaskFeign.batchAddPurchaseStockIn(resultList);
        return add;
    }

    @Override
    public PurchaseOrderDTO.GetOneDTO getPurchaseOrder(String id) {
        PurchaseOrderDTO.GetOneDTO getOneDTO = new PurchaseOrderDTO.GetOneDTO();
        PurchaseOrderEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        BeanMapperUtils.copy(entity, getOneDTO);

        //采购供应商信息
        PurchaseOrderSupplierDTO.UpdateDTO updateDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        BeanMapperUtils.copy(purchaseOrderSupplierEntity, updateDTO);
        getOneDTO.setPurchaseOrderSupplierDTO(updateDTO);
        //供应商地址
        SupplierEntity supplier = supplierService.getById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isNotEmpty(supplier)) {
            getOneDTO.setCompanyAddress(supplier.getCompanyAddress());
            getOneDTO.setSupplierName(supplier.getName());
        }

        return getOneDTO;
    }


    /**
     * 根据采购订单id 获取对应产品信息
     *
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetQcProductDTO
     * @author yl
     * @date 2023-04-17 18:27
     */
    @Override
    public PurchaseOrderDTO.GetQcProductDTO getQcProductInfo(String purchaseOrderId) {
        PurchaseOrderDTO.GetQcProductDTO result = new PurchaseOrderDTO.GetQcProductDTO();
        PurchaseOrderEntity entity = this.getById(purchaseOrderId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        result.setSupplierId(result.getSupplierId());
        result.setSupplierName(result.getSupplierName());
        result.setWarehouseId(result.getWarehouseId());
        result.setWarehouseName(result.getWarehouseName());
        
        //采购订单详情
        List<PurchaseOrderDetailEntity> orderDetailList = purchaseOrderDetailService.listByPurchaseOrderId(purchaseOrderId);
        List<String> skuIdList = orderDetailList.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductVO.ProductPackVO> skuList = plmTaskFeign.getProductPackBySkuIds(skuIdList);
        for(ProductVO.ProductPackVO item:skuList){
           Integer qty=orderDetailList.stream().filter(o->o.getSkuId().equals(item.getSkuId())).findFirst().
                   flatMap(obj-> Optional.ofNullable(obj.getPurchaseQty())).orElse(0);
            item.setQty(qty);
        }
        return result;
    }


    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String purchaseUserId, String purchaseDeptId, String purchaseOrgId, String receiveOrgId, String deliveryWarehouseId, PurchaseOrderEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(purchaseUserId);
            if (ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setPurchaseUserName(purchaseUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(purchaseDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(purchaseDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setPurchaseDeptName(depart.getName());
        }
        List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(purchaseOrgId, receiveOrgId));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9029);
        }
        //采购组织
        if (StringUtils.isNotBlank(purchaseOrgId)) {
            BaseIdDTO baseIdDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(purchaseOrgId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(baseIdDTO)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setPurchaseOrgName(baseIdDTO.getName());
        }
        //收料组织
        if (StringUtils.isNotBlank(receiveOrgId)) {
            BaseIdDTO baseIdDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(receiveOrgId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(baseIdDTO)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setReceiveOrgName(baseIdDTO.getName());
        }

        //仓库
        if (StringUtils.isNotBlank(deliveryWarehouseId)) {
            //仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(deliveryWarehouseId));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDeliveryWarehouseName(warehouseName);
        }
    }


    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getApproveStatus, approveStatus)
                .set(PurchaseOrderEntity::getApproveUserId, "")
                .set(PurchaseOrderEntity::getApproveUserName, "")
                .set(PurchaseOrderEntity::getApproveTime, null)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getApproveUserId, userInfo.getUid())
                .set(PurchaseOrderEntity::getApproveUserName, userInfo.getUserName())
                .set(PurchaseOrderEntity::getApproveStatus, approveStatus)
                .set(PurchaseOrderEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseOrderEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseOrderEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        return list;
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseOrderDetailDTO.AddDTO> list) {
        Map<String, List<PurchaseOrderDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailDTO.AddDTO>> entry : map.entrySet()) {
            List<PurchaseOrderDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }

    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<PurchaseOrderDetailDTO.UpdateDTO> list, String purchaseOrderId) {
        Map<String, List<PurchaseOrderDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailDTO.UpdateDTO>> entry : map.entrySet()) {
            List<PurchaseOrderDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
            PurchaseOrderDetailEntity entity = purchaseOrderDetailService.getByPurchaseOrderIdAndSkuId(purchaseOrderId, entry.getKey());
            if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(value.get(0).getId())) {
                value.forEach(obj -> obj.setId(entity.getId()));
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】已存在")));
            }
        }
    }

    /**
     * 更新采购申请单的生成状态
     */
    private void updateCreatePoType(List<String> purchaseOrderIds) {
        //关联信息
        List<PurchaseApplicationRefPoEntity> list = purchaseApplicationRefPoService.listByPurchaseOrderIds(purchaseOrderIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> purchaseApplicationDetailIds = list.stream().map(PurchaseApplicationRefPoEntity::getPurchaseApplicationDetailId).distinct().collect(Collectors.toList());
        //查询采购订单明细下采购数量
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(new PurchaseApplicationRefPoDTO.SearchParamDTO().setPurchaseApplicationDetailIds(purchaseApplicationDetailIds));
        if (CollectionUtils.isEmpty(refList)) {
            return;
        }
        //查询采购申请单明细申请数量
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(purchaseApplicationDetailIds);
        if (CollectionUtils.isEmpty(purchaseApplicationDetailList)) {
            log.error("采购订单删除，未找到关联采购申请单明细！");
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<PurchaseApplicationDetailEntity> updateList = new ArrayList<>();
        //建采购订单明细按采购申请明细id分组后比较数量
        Map<String, List<PurchaseApplicationRefPoDTO.ListDTO>> map = refList.stream().collect(Collectors.groupingBy(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseApplicationDetailId));
        for (Map.Entry<String, List<PurchaseApplicationRefPoDTO.ListDTO>> entry : map.entrySet()) {
            String purchaseApplicationDetailId = entry.getKey();
            List<PurchaseApplicationRefPoDTO.ListDTO> value = entry.getValue();

            PurchaseApplicationDetailEntity update = new PurchaseApplicationDetailEntity();
            update.setId(purchaseApplicationDetailId);
            //采购数量
            Integer purchaseQty = value.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            //申请数量
            Integer applyQty = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(purchaseApplicationDetailId)).map(PurchaseApplicationDetailEntity::getApplyQty).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(applyQty)) {
                log.error(String.format("采购申请单明细Id【%s】不存在！", purchaseApplicationDetailId));
                throw new ServiceException(ApiError.ERROR_98017);
            }
            if (MathUtil.compareTo(purchaseQty, MathUtil.ZERO) == 0) {
                update.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            } else if (MathUtil.compareTo(applyQty, purchaseQty) > 0) {
                update.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            } else {
                update.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            }
            updateList.add(update);
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            purchaseApplicationDetailService.updateBatchById(updateList);
        }
    }

    /**
     * @param excelDateList
     * @param successList
     * @param errorList
     * @param supplierId
     * @description: 查询供应商报价信息
     * @author Will
     * @date: 2023/3/30 14:58
     */
    private void doOpHandleNotExistPrice(List<PurchaseOrderImportExcelDTO> excelDateList, List<PurchaseOrderDetailDTO.AddDTO> successList, List<PurchaseOrderImportExcelDTO> errorList, String supplierId) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<PurchaseOrderDetailDTO.AddDTO> removeList = new ArrayList<>();
        for (PurchaseOrderDetailDTO.AddDTO addDTO : successList) {
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(addDTO.getPurchaseQty(), addDTO.getSkuId(), addDTO.getSkuNo(), supplierId);
            PurchaseOrderImportExcelDTO purchaseOrderImportExcelDTO = excelDateList.stream().filter(obj -> addDTO.getSkuNo().equals(obj.getSkuNo()) && addDTO.getPurchaseQty().toString().equals(obj.getPurchaseQtyStr())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderImportExcelDTO)) {
                continue;
            }
            Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> pair = purchasePriceDetailService.listPurchaseTaxPriceView(searchDTO);
            String error = pair.getKey();
            //存在错误信息则
            if (StringUtils.isNotBlank(error)) {
                purchaseOrderImportExcelDTO.setErrorMsg("1、".concat(error));
                errorList.add(purchaseOrderImportExcelDTO);
                removeList.add(addDTO);
                continue;
            }
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> value = pair.getValue();
            addDTO.setTaxRate(value.get(0).getTaxRate());
            addDTO.setTaxPrice(value.get(0).getTaxPrice());
            addDTO.setCurrency(value.get(0).getCurrency());
            addDTO.setCurrencySymbol(value.get(0).getCurrencySymbol());
        }
        if (CollectionUtils.isNotEmpty(removeList)) {
            successList.removeAll(removeList);
        }
    }

    /**
     * @param ids
     * @param reason
     * @description: 更新作废状态
     * @author Will
     * @date: 2023/3/30 18:09
     */
    private void updateInvalidStatus(List<String> ids, String reason) {
        //更新
        lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PurchaseOrderEntity::getInvalidTime, LocalDateTime.now())
                .set(PurchaseOrderEntity::getInvalidRemark, reason)
                .update();
    }
}
