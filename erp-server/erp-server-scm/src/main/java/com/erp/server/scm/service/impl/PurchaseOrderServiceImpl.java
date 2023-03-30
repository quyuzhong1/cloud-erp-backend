package com.erp.server.scm.service.impl;

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
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchaseOrderExportExcelDTO;
import com.erp.model.scm.dto.excel.PurchaseOrderImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
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

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> paging(PagingDTO<PurchaseOrderDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<PurchaseOrderDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> ids = records.stream().map(PurchaseOrderDTO.ListDTO::getId).collect(Collectors.toList());
            //查询流程id判断是否存在流程 TODO

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setCreateUserName(null);
                    return;
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(PurchaseOrderDTO.AddDTO dto) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getPurchaseUserId(),dto.getPurchaseDeptId(),dto.getPurchaseOrgId(),dto.getDeliveryWarehouseId(),entity);
        log.info("采购订单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PL, BusinessNoTypeEnum.CODE_PL.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购单【%s】",code), ModuleTypeEnum.PURCHASE_ORDER.getCode(),entity.getId(),"新增操作");
            //新增明细
            purchaseOrderDetailService.add(dto.getDetails(),entity.getId());
            //新增供应商信息
            purchaseOrderSupplierService.add(dto.getPurchaseOrderSupplierDTO());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseOrderDTO.UpdateDTO dto) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkUpdateDetailsRepeatSku(dto.getDetails(),dto.getId());
        //处理数据id
        doOpHandleDataId(dto.getPurchaseUserId(),dto.getPurchaseDeptId(),dto.getPurchaseOrgId(),dto.getDeliveryWarehouseId(),entity);

        log.info("采购订单修改，id=【{}】", dto.getId());
        
        //操作日志
        PurchaseOrderEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old,entity,ModuleTypeEnum.PURCHASE_ORDER.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseOrderDetailService.update(dto.getDetails(),entity.getId());
        //供应商数据
        purchaseOrderSupplierService.update(dto.getPurchaseOrderSupplierDTO());
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
        BeanMapperUtils.copy(entity,dto);

        //供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.listByPurchaseOrderId(id);
        PurchaseOrderSupplierDTO.UpdateDTO supplierUpdateDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        if (ObjectUtils.isEmpty(supplierUpdateDTO)) {
            throw new ServiceException(ApiError.ERROR_98031);
        }
        BeanMapperUtils.copy(purchaseOrderSupplierEntity,supplierUpdateDTO);
        dto.setPurchaseOrderSupplierDTO(supplierUpdateDTO);

        //明细信息
        List<PurchaseOrderDetailEntity> entityDetails = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<PurchaseOrderDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(PurchaseOrderDetailDTO.UpdateDTO.class, entityDetails);
        dto.setDetails(details);
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
        return Boolean.TRUE;
    }

    @Override
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

        log.info("采购申请单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO

            //更新单据状态(后面有流程了可删)
            updateApproveStatusForApprove(ids,ApproveStatusEnum.APPROVE.getStatus());
        }
        //审核不通过
        if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids,ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购订单",ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"审核操作");
    }

    @Override
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
        updateApproveStatus(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个采购订单【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购申请单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatus(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购申请单【%s】取消流程", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean finishDelivery(List<String> ids) {
        //ids为采购订单明细id集合
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        long count = purchaseOrderDetailList.stream().filter(obj -> !ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode().equals(obj.getArrivalStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98035);
        }
        //查询签收单数量放些明细中的采购数量 TODO

        //更新明细中的交货状态
        purchaseOrderDetailService.updateArrivalStatusByIds(ArrivalStatusEnum.ARRIVED.getCode(),ids);
        return Boolean.TRUE;
    }
    @Override
    public Boolean purchaseChange(String id) {
        return null;
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
        PurchaseOrderSupplierEntity purchaseOrderSupplier = purchaseOrderSupplierService.getById(id);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
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
            BeanMapperUtils.copy(purchaseOrderDetailEntity,detailDTO);
            //明细数据处理
            detailDTO.setUnitName("个");
            details.add(detailDTO);
        }
        exportPdfDTO.setDetails(details);
        return exportPdfDTO;
    }

    @Override
    public PurchaseOrderDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        //查询所有启用核算公司
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();

        PurchaseOrderExcelListener excelListenerUtil = new PurchaseOrderExcelListener(skuList,skuIds,companyList);

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
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
        List<PurchaseOrderExportExcelDTO> exportExcelList = baseMapper.listExportExcel(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/purchaseOrderExport.xlsx";
        String name = "采购订单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(exportExcelList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseOrderEntity> list = getList(ids);
        //待提交并且未作废允许提交
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        log.info("采购订单提交，ids=【{}】", JSONUtil.toJsonStr(ids));
        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购订单【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"提交操作");
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
    public Boolean updateAndSubmit(PurchaseOrderDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public List<ListStatusCountDTO.PurchaseOrderCountDTO> listCount() {
        PurchaseListTypeEnum[] values = PurchaseListTypeEnum.values();
        List<ListStatusCountDTO.PurchaseOrderCountDTO> list = new ArrayList<>();
        for (PurchaseListTypeEnum item: values) {
            PurchaseOrderDTO.SearchParamDTO dto = new PurchaseOrderDTO.SearchParamDTO();
            ListStatusCountDTO.PurchaseOrderCountDTO resultDTO = new ListStatusCountDTO.PurchaseOrderCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            if (PurchaseListTypeEnum.TO_BE_CREATE.getCode().equals(item.getCode())) {
                dto.setArrivalStatusList(Arrays.asList(ArrivalStatusEnum.NON_ARRIVAL.getCode(),ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()));
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            if (PurchaseListTypeEnum.CREATED.getCode().equals(item.getCode())) {
                dto.setArrivalStatusList(Arrays.asList(ArrivalStatusEnum.ARRIVED.getCode()));
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            if (PurchaseListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
               count = this.baseMapper.listCount(dto);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
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

        //更新
        lambdaUpdate().in(PurchaseOrderEntity::getId,ids)
                .set(PurchaseOrderEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PurchaseOrderEntity::getInvalidTime, LocalDateTime.now())
                .set(PurchaseOrderEntity::getInvalidRemark,reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个采购订单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"作废操作");
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

        List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewList = new ArrayList<>();
        for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : detailList) {
            PurchaseOrderDTO.ViewGenerateReceiveDTO viewDTO = new PurchaseOrderDTO.ViewGenerateReceiveDTO();
            BeanMapperUtils.copy(purchaseOrderDetailEntity,viewDTO);
            PurchaseOrderEntity purchaseOrderEntity = list.stream().filter(obj -> obj.getId().equals(purchaseOrderDetailEntity.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                log.error("下推签收单，未找到对应采购明细");
                throw new ServiceException(ApiError.ERROR_98025);
            }
            viewDTO.setId(purchaseOrderEntity.getId());
            viewDTO.setPurchaseOrderDetailId(purchaseOrderDetailEntity.getId());
            viewDTO.setCode(purchaseOrderEntity.getCode());
            //供应商信息
            PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierList.stream().filter(obj -> obj.getPurchaseOrderId().equals(purchaseOrderEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
                log.error("下推签收单，未找到对应供应商信息");
                throw new ServiceException(ApiError.ERROR_98036);
            }
            viewDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
            viewDTO.setSupplierName(purchaseOrderSupplierEntity.getSupplierName());
            //交货数量 TODO

            viewList.add(viewDTO);
        }
        return viewList;
    }

    @Override
    public Boolean generateReceive(PurchaseOrderDTO.ListGenerateReceiveDTO dto) {
        //生成下推签收单 TODO

        return Boolean.TRUE;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String purchaseUserId,String purchaseDeptId,String purchaseOrgId,String deliveryWarehouseId,PurchaseOrderEntity entity) {
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
        //采购组织
        if (StringUtils.isNotBlank(purchaseOrgId)) {
            List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(purchaseOrgId));
            if (CollectionUtils.isEmpty(accountingCompanyList)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setPurchaseOrgName(accountingCompanyList.get(0).getName());
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
    private void updateApproveStatus(List<String> ids,String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseOrderEntity::getId,ids)
                .set(PurchaseOrderEntity::getApproveStatus,approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids,String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(PurchaseOrderEntity::getId,ids)
                .set(PurchaseOrderEntity::getApproveUserId,userInfo.getUid())
                .set(PurchaseOrderEntity::getApproveUserName,userInfo.getUserName())
                .set(PurchaseOrderEntity::getApproveStatus,approveStatus)
                .set(PurchaseOrderEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseOrderEntity>  getList(List<String> ids) {
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
        for (Map.Entry<String, List<PurchaseOrderDetailDTO.AddDTO>> entry: map.entrySet()) {
            List<PurchaseOrderDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }
    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<PurchaseOrderDetailDTO.UpdateDTO> list, String purchaseOrderId) {
        Map<String, List<PurchaseOrderDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailDTO.UpdateDTO>> entry: map.entrySet()) {
            List<PurchaseOrderDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
            PurchaseOrderDetailEntity entity = purchaseOrderDetailService.getByPurchaseOrderIdAndSkuId(purchaseOrderId, entry.getKey());
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(value.get(0).getId())) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】已存在")));
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
        for (Map.Entry<String, List<PurchaseApplicationRefPoDTO.ListDTO>> entry: map.entrySet()) {
            String purchaseApplicationDetailId = entry.getKey();
            List<PurchaseApplicationRefPoDTO.ListDTO> value = entry.getValue();

            PurchaseApplicationDetailEntity update = new PurchaseApplicationDetailEntity();
            update.setId(purchaseApplicationDetailId);
            //采购数量
            Integer purchaseQty = value.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            //申请数量
            Integer applyQty = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(purchaseApplicationDetailId)).map(PurchaseApplicationDetailEntity::getApplyQty).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(applyQty)) {
                log.error(String.format("采购申请单明细Id【%s】不存在！",purchaseApplicationDetailId));
                throw new ServiceException(ApiError.ERROR_98017);
            }
            if (MathUtil.compareTo(purchaseQty,MathUtil.ZERO) == 0) {
                update.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            } else if (MathUtil.compareTo(applyQty,purchaseQty) > 0) {
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


}
