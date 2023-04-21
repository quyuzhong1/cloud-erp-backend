package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.QcBillExportExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 质检单表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcBillServiceImpl extends SuperServiceImpl<QcBillMapper, QcBillEntity> implements QcBillService {


    @Resource
    private QcProductService qcProductService;

    @Resource
    private QcInfoService qcInfoService;


    @Resource
    private WarehouseService warehouseService;

    @Resource
    private QcReportDetailService qcReportDetailService;

    @Resource
    private QcBillRemarkService qcBillRemarkService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PurchaseStockInService purchaseStorageService;

    @Resource
    private CommonService commonService;


    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    /**
     * 保存 质检单
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QcBillDTO.SaveOrUpdateDTO dto) {
        //质检单
        QcBillEntity bill = new QcBillEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        bill.setId(billId);

        QcBillStatusEnum waitQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.WAIT_QC.getCode());
        bill.setQcStatus(waitQc);
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
        bill.setCode(code);
        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();
        if (StringUtils.isNotBlank(purchaseOrderId)) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                bill.setWarehouseId(purchaseOrder.getDeliveryWarehouseId());
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }

        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.add(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.add(billId, dto.getRemarkList());
        }
        return result;
    }

    @Override
    public List<QcBillEntity> listByPoIds(List<String> poIds) {
        return lambdaQuery().in(QcBillEntity::getPurchaseOrderId, poIds).list();
    }


    /**
     * 质检单详情
     *
     * @param id
     * @return com.erp.model.wms.dto.QcBillDTO.ViewDTO
     * @author yl
     * @date 2023-04-19 11:53
     */
    @Override
    public QcBillDTO.ViewDTO view(String id) {
        QcBillEntity bill = this.getById(id);
        if (Objects.isNull(bill)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        QcBillDTO.ViewDTO view = new QcBillDTO.ViewDTO();
        BeanMapper.copy(bill, view);
        //产品信息
        QcProductDTO.ViewDTO qcProduct = qcProductService.getByMainId(id);
        view.setQcProduct(qcProduct);

        //质检信息
        QcInfoDTO.ViewDTO qcInfo = qcInfoService.getByMainId(id);
        view.setQcInfo(qcInfo);

        //质检报告 信息
        List<QcReportDetailDTO.ViewDTO> reportDetailList = qcReportDetailService.getByMainId(id);
        view.setReportDetailList(reportDetailList);

        //质检备注
        List<QcRemarkDTO.AddDTO> remarkList = qcBillRemarkService.getByMainId(id);
        view.setRemarkList(remarkList);

        return view;
    }

    /**
     * 质检单分页信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.QcBillDTO.PagingViewDTO>
     * @author yl
     * @date 2023-04-19 15:25
     */
    @Override
    public PagingVO<QcBillDTO.PagingViewDTO> paging(PagingDTO<QcBillDTO.PagingParamDTO> dto) {
        QcBillDTO.PagingParamDTO params = dto.getParams();
        String searchType = params.getSearchType();
        //如果等于所有
        if (searchType.equals(SearchType.ALL)) {
            params.setSearchType("");
        }
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<QcBillDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
        List<String> supplierIdList = list.stream().map(QcBillDTO.PagingViewDTO::getSupplierId).collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(QcBillDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
        List<String> warehouseIdList = list.stream().map(QcBillDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        List<String> billIdList = list.stream().map(QcBillDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<QcBillRemarkEntity> billRemarkList = qcBillRemarkService.getByMainIdList(billIdList);

        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (QcBillDTO.PagingViewDTO item : list) {
            QcBillStatusEnum billStatusEnum = item.getQcStatus();
            item.setQcStatusName(billStatusEnum.getName());
            QcTypeEnum qcTypeEnum = item.getQcType();
            item.setQcTypeName(qcTypeEnum.getName());
            String handleModeDict = item.getHandleModeDict();
            String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setHandleModeName(handleModeName);
            QcResultEnum qcResultEnum = item.getQcResult();
            item.setQcResultName(qcResultEnum.getName());
            String skuId = item.getSkuId();
            String skuName = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            item.setSkuName(skuName);
            String supplierId = item.getSupplierId();
            String supplierName = supplierList.stream().filter(s -> s.getId().equals(supplierId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setSupplierName(supplierName);
            String warehouseId = item.getWarehouseId();
            String warehouseName = warehouseList.stream().filter(w -> w.getId().equals(warehouseId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setWarehouseName(warehouseName);
            String remark = billRemarkList.stream().filter(r -> r.getMainId().equals(item.getId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getRemark())).orElse("");
            item.setRemark(remark);

        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出质检单信息
     *
     * @param dto
     * @param response
     * @return void
     * @author yl
     * @date 2023-04-19 18:38
     */
    @Override
    public void exportQcBill(QcBillDTO.ExportDTO dto, HttpServletResponse response) {

        String searchType = dto.getSearchType();
        //如果等于所有
        if (searchType.equals(SearchType.ALL)) {
            dto.setSearchType("");
        }
        List<QcBillDTO.PagingViewDTO> viewList = baseMapper.getExport(dto);
        List<QcBillExportExcelDTO> resultList = new ArrayList<>(viewList.size());
        if (CollectionUtils.isNotEmpty(viewList)) {
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            List<String> supplierIdList = viewList.stream().map(QcBillDTO.PagingViewDTO::getSupplierId).collect(Collectors.toList());
            List<String> skuIdList = viewList.stream().map(QcBillDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
            List<String> warehouseIdList = viewList.stream().map(QcBillDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
            List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
            List<String> billIdList = viewList.stream().map(QcBillDTO.PagingViewDTO::getId).collect(Collectors.toList());
            List<QcBillRemarkEntity> billRemarkList = qcBillRemarkService.getByMainIdList(billIdList);
            List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
            for (QcBillDTO.PagingViewDTO item : viewList) {
                QcBillExportExcelDTO excelDTO = new QcBillExportExcelDTO();
                BeanMapper.copy(item, excelDTO);
                QcBillStatusEnum billStatusEnum = item.getQcStatus();
                excelDTO.setQcStatusName(billStatusEnum.getName());
                QcTypeEnum qcTypeEnum = item.getQcType();
                excelDTO.setQcTypeName(qcTypeEnum.getName());
                String handleModeDict = item.getHandleModeDict();
                String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setHandleModeName(handleModeName);
                QcResultEnum qcResultEnum = item.getQcResult();
                excelDTO.setQcResultName(qcResultEnum.getName());
                String skuId = item.getSkuId();
                String skuName = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
                excelDTO.setSkuName(skuName);
                String supplierId = item.getSupplierId();
                String supplierName = supplierList.stream().filter(s -> s.getId().equals(supplierId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setSupplierName(supplierName);
                String warehouseId = item.getWarehouseId();
                String warehouseName = warehouseList.stream().filter(w -> w.getId().equals(warehouseId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setWarehouseName(warehouseName);
                String remark = billRemarkList.stream().filter(r -> r.getMainId().equals(item.getId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getRemark())).orElse("");
                excelDTO.setRemark(remark);
                //是否内检
                Boolean isInside = item.getIsInside();
                excelDTO.setInsideType(isInside ? "内部检验" : "外部检验");
                resultList.add(excelDTO);
            }

        }
        String fileName = "质检单数据";
        try {
            ExcelUtil.export(fileName, "质检单数据", resultList, QcBillExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }


    }


    /**
     * 完成质检
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 10:26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finish(QcBillDTO.SaveOrUpdateDTO dto) {
        //质检信息
        QcInfoDTO.AddDTO qcInfo = dto.getQcInfo();
        //当质检信息id 为空的时候
        if (StringUtils.isBlank(qcInfo.getId())) {
            String id = IdWorker.getIdStr();
            qcInfo.setId(id);
        }
        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();
        Boolean isExist = StringUtils.isNotBlank(purchaseOrderId);
        //检查质检数量
        checkQcQty(qcInfo, dto.getId(), dto.getPurchaseOrderId(), dto.getQcProduct().getSkuId());
        //质检单
        QcBillEntity bill = new QcBillEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        String code = bill.getCode();
        if (StringUtils.isBlank(code)) {
            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
            bill.setCode(code);
        }
        bill.setId(billId);
        bill.setQcFinishTime(LocalDate.now());
        QcBillStatusEnum finishQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.FINISH_QC.getCode());
        bill.setQcStatus(finishQc);
        String warehouseId = "";
        if (isExist) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                warehouseId = purchaseOrder.getDeliveryWarehouseId();
                bill.setWarehouseId(warehouseId);
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }
        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.add(billId, qcInfo);
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.add(billId, dto.getRemarkList());

            //质检类型
            String qcType = qcInfo.getQcType().getCode();
            String b2bQc = QcTypeEnum.B2B_OUTSIDE_QC.getCode();
            //当是 b2b 质检的时候 生成入库单
            if (b2bQc.equals(qcType) && isExist) {
                //生成入库单
                autoStockInBill(billId, qcInfo, purchaseOrderId, warehouseId);
            }
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个质检单【%s】", code), ModuleTypeEnum.QC_ORDER.getCode(), billId, "新增操作");
        }
        return result;
    }


    /**
     * 自动生成 入库单
     *
     * @param billId
     * @param qcInfo
     * @param purchaseOrderId
     * @return void
     * @author yl
     * @date 2023-04-20 14:55
     */
    private void autoStockInBill(String billId, QcInfoDTO.AddDTO qcInfo, String purchaseOrderId, String warehouseId) {
        PurchaseStockInDTO.AddDTO dto = new PurchaseStockInDTO.AddDTO();
        List<PurchaseStockInDetailDTO.AddDTO> details = new ArrayList<>(1);
        PurchaseStockInDetailDTO.AddDTO detail = new PurchaseStockInDetailDTO.AddDTO();
        detail.setExceedQty(0);
        detail.setStockInQty(qcInfo.getTotalQty());
        detail.setSourceDetailId(qcInfo.getId());
        detail.setPurchaseOrderDetailId(qcInfo.getPurchaseOrderDetailId());
        details.add(detail);
        dto.setDetails(details);
        dto.setSourceId(billId);
        dto.setSourceType(SourceTypeEnum.QC_BILL.getType());
        dto.setPurchaseOrderId(purchaseOrderId);
        dto.setDeliveryWarehouseId(warehouseId);
        String userId = commonService.getUserInfo().getUid();
        dto.setStockInUserId(userId);
        //生成结果
        String createResultId = purchaseStorageService.addAndSubmit(dto);
        if (StringUtils.isNotBlank(createResultId)) {
            BaseApproveParamDTO approveParam = new BaseApproveParamDTO();
            approveParam.setIds(Arrays.asList(createResultId));
            approveParam.setType(ApproveTypeEnum.PASS.getStatus());
            purchaseStorageService.approve(approveParam);
        }
    }

    /**
     * 暂存
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 14:01
     */
    @Override
    public Boolean draft(QcBillDTO.SaveOrUpdateDTO dto) {
        //质检单
        QcBillEntity bill = new QcBillEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        bill.setId(billId);
        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();
        if (StringUtils.isNotBlank(purchaseOrderId)) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                bill.setWarehouseId(purchaseOrder.getDeliveryWarehouseId());
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }

        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.add(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.add(billId, dto.getRemarkList());
            moduleOperateLogService.addModuleOperateLog("新增了一个暂存质检单", ModuleTypeEnum.QC_ORDER.getCode(), billId, "暂存操作");
        }
        return result;
    }


    /**
     * 免检
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 15:29
     */
    @Override
    public Boolean exemption(QcBillDTO.SaveOrUpdateDTO dto) {
        //质检信息
        QcInfoDTO.AddDTO qcInfo = dto.getQcInfo();
        //当质检信息id 为空的时候
        if (StringUtils.isBlank(qcInfo.getId())) {
            String id = IdWorker.getIdStr();
            qcInfo.setId(id);
        }
        //免检设置为0
        qcInfo.setQcBadQty(0);
        qcInfo.setQcGoodQty(0);
        qcInfo.setQcQty(0);
        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();
        Boolean isExist = StringUtils.isNotBlank(purchaseOrderId);
        //检查质检数量
        checkQcQty(qcInfo, dto.getId(), dto.getPurchaseOrderId(), dto.getQcProduct().getSkuId());
        //质检单
        QcBillEntity bill = new QcBillEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        String code = bill.getCode();
        if (StringUtils.isBlank(code)) {
            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
            bill.setCode(code);
        }
        bill.setId(billId);
        bill.setQcFinishTime(LocalDate.now());
        QcBillStatusEnum exemption = QcBillStatusEnum.getByCode(QcBillStatusEnum.EXEMPTION.getCode());
        bill.setQcStatus(exemption);
        String warehouseId = "";
        if (isExist) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                warehouseId = purchaseOrder.getDeliveryWarehouseId();
                bill.setWarehouseId(warehouseId);
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }
        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.add(billId, qcInfo);
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.add(billId, dto.getRemarkList());

            //质检类型
            String qcType = qcInfo.getQcType().getCode();
            String b2bQc = QcTypeEnum.B2B_OUTSIDE_QC.getCode();
            //当是 b2b 质检的时候 生成入库单
            if (b2bQc.equals(qcType) && isExist) {
                //生成入库单
                autoStockInBill(billId, qcInfo, purchaseOrderId, warehouseId);
            }

            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个免检质检单【%s】", code), ModuleTypeEnum.QC_ORDER.getCode(), billId, "新增操作");
        }
        return result;
    }


    /**
     * 批量完成质检单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 15:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchFinish(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        String qcStatus = QcBillStatusEnum.WAIT_QC.getCode();
        List<QcBillEntity> qcList = this.listByIds(ids);
        long count = qcList.stream().filter(s -> !s.getQcStatus().getCode().equals(qcStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99018);
        }
        //批量检查
        batchCheckQcQty(qcList, false);
        LocalDate now = LocalDate.now();
        //质检状态
        QcBillStatusEnum finishQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.FINISH_QC.getCode());
        for (QcBillEntity item : qcList) {
            item.setQcFinishTime(now);
            item.setQcStatus(finishQc);
            if (StringUtils.isBlank(item.getCode())) {
                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
                item.setCode(code);
            }
        }
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("质检单【%s】完成操作", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "完成操作");
        return this.updateBatchById(qcList);
    }


    /**
     * 批量完成免检
     *
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 16:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchExemption(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        String qcStatus = QcBillStatusEnum.WAIT_QC.getCode();
        List<QcBillEntity> qcList = this.listByIds(ids);
        long count = qcList.stream().filter(s -> !s.getQcStatus().getCode().equals(qcStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99020);
        }
        //批量检查
        batchCheckQcQty(qcList, true);
        LocalDate now = LocalDate.now();
        //质检状态
        QcBillStatusEnum exemption = QcBillStatusEnum.getByCode(QcBillStatusEnum.EXEMPTION.getCode());
        for (QcBillEntity item : qcList) {
            item.setQcFinishTime(now);
            item.setQcStatus(exemption);
            if (StringUtils.isBlank(item.getCode())) {
                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
                item.setCode(code);
            }
        }
        Boolean result = this.updateBatchById(qcList);
        if (result) {
            //批量去更新 质检数量
            qcInfoService.updateQcQty(ids);
        }
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("质检单【%s】免检操作", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "免检操作");
        return result;

    }


    /**
     * 批量取消 质检单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 17:13
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchCancel(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<QcBillEntity> qcList = this.listByIds(ids);
        String qcStatus = QcBillStatusEnum.WAIT_QC.getCode();
        long count = qcList.stream().filter(s -> !s.getQcStatus().getCode().equals(qcStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99021);
        }
        //质检状态
        QcBillStatusEnum cancelQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.CANCEL.getCode());
        qcList.forEach(q -> q.setQcStatus(cancelQc));

        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("质检单【%s】取消操作", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "取消操作");
        return this.updateBatchById(qcList);
    }


    /**
     * 删除质检单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 17:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<QcBillEntity> qcList = this.listByIds(ids);
        List<String> statusList = new ArrayList<>(3);
        statusList.add(QcBillStatusEnum.DRAFT.getCode());
        statusList.add(QcBillStatusEnum.WAIT_QC.getCode());
        statusList.add(QcBillStatusEnum.CANCEL.getCode());
        long count = qcList.stream().filter(s -> !statusList.contains(s.getQcStatus().getCode())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99022);
        }
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);
        return this.removeByIds(ids);
    }


    /**
     * 撤销
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 17:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<QcBillEntity> qcList = this.listByIds(ids);
        List<String> statusList = new ArrayList<>(2);
        statusList.add(QcBillStatusEnum.EXEMPTION.getCode());
        statusList.add(QcBillStatusEnum.FINISH_QC.getCode());
        long count = qcList.stream().filter(s -> !statusList.contains(s.getQcStatus().getCode())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99023);
        }

        //TODO 工作流要处理
        QcBillStatusEnum waitQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.WAIT_QC.getCode());
        qcList.forEach(q -> q.setQcStatus(waitQc));
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("质检单【%s】取消流程", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "取消流程操作");
        return this.updateBatchById(qcList);
    }


    /**
     * 分配质检员
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 17:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assign(QcBillDTO.AssignDTO dto) {
        List<String> ids = dto.getIds();
        String qcUserId = dto.getQcUserId();
        List<QcBillEntity> qcList = this.listByIds(ids);
        SysDepartmentUserNumberDTO userInfo = sysUserFeign.getDeptByUserId(qcUserId);
        for (QcBillEntity item : qcList) {
            item.setQcDeptId(userInfo.getDepartmentId());
            item.setQcDeptName(userInfo.getDepartmentName());
            item.setQcUserId(qcUserId);
            item.setQcUserName(userInfo.getUserName());
        }
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("质检单【%s】分配质检员" + userInfo.getUserName(), ModuleTypeEnum.QC_ORDER.getCode(), pairList, "分配操作");
        return this.updateBatchById(qcList);
    }


    /**
     * 批量更新处理措施
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 19:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateHandleMode(QcInfoDTO.UpdateHandleModeDTO dto) {
        List<String> ids = dto.getIds();
        List<QcBillEntity> qcList = this.listByIds(ids);
        List<String> statusList = new ArrayList<>(2);
        statusList.add(QcBillStatusEnum.WAIT_QC.getCode());
        statusList.add(QcBillStatusEnum.DRAFT.getCode());
        long count = qcList.stream().filter(s -> !statusList.contains(s.getQcStatus().getCode())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99024);
        }
        String handleModeDict = dto.getHandleModeDict();
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
        String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("质检单【%s】更新处理措施" + handleModeName, ModuleTypeEnum.QC_ORDER.getCode(), pairList, "更新处理措施操作");
        return qcInfoService.updateHandleMode(ids,handleModeDict);
    }

    /**
     * 批量完成
     * 质检数量
     *
     * @param qcList
     * @return void
     * @author yl
     * @date 2023-04-20 10:34
     */
    private void batchCheckQcQty(List<QcBillEntity> qcList, Boolean isExemption) {
        if (CollectionUtils.isNotEmpty(qcList)) {

            qcList.sort(Comparator.comparing(QcBillEntity::getCreateTime, Comparator.reverseOrder()));

            List<String> ids = qcList.stream().map(QcBillEntity::getId).collect(Collectors.toList());
            //质检信息
            List<QcInfoEntity> qcInfoList = qcInfoService.getByMainIdList(ids);

            //质检产品信息
            List<QcProductEntity> qcProductList = qcProductService.getByMainIdList(ids);

            List<String> purOrderIds = qcList.stream().map(QcBillEntity::getPurchaseOrderId).collect(Collectors.toList());
            //获取到对应的 订单明细
            List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(purOrderIds);

            /**
             * 根据采访订单id集合
             * 获取到已质检数量
             */
            List<QcInfoDTO.QcQtyDTO> qcQtyList = qcInfoService.getPurOrderIds(purOrderIds);

            for (QcBillEntity item : qcList) {
                String mainId = item.getId();
                QcInfoEntity qcInfo = qcInfoList.stream().filter(q -> q.getMainId().equals(mainId)).findFirst().orElse(null);
                QcProductEntity qcProduct = qcProductList.stream().filter(q -> q.getMainId().equals(mainId)).findFirst().orElse(null);
                if (qcInfo != null) {
                    Integer goodQty = qcInfo.getQcGoodQty();
                    Integer badQty = qcInfo.getQcBadQty();
                    Integer qcQty = qcInfo.getQcQty();
                    if (!isExemption && (goodQty + badQty > qcQty)) {
                        Integer errorCode = ApiError.ERROR_99016.code;
                        String errorMsg = String.format("质检单【%s】, 质检不良数+合格数不能超过质检数量", item.getCode());
                        throw new ServiceException(errorCode, errorMsg);
                    }

                    if (qcProduct != null) {
                        String skuId = qcProduct.getSkuId();
                        //采购订单id
                        String purchaseOrderId = item.getPurchaseOrderId();
                        if (StringUtils.isNotBlank(purchaseOrderId)) {
                            //采购的订单数量
                            Integer purchaseSkuQty = purOrderDetailList.stream().filter(p ->
                                    p.getSkuId().equals(skuId) && p.getPurchaseOrderId().equals(purchaseOrderId)
                            ).mapToInt(PurchaseOrderDetailEntity::getPurchaseQty).sum();

                            //已完成的质检
                            Integer finishQcQty = qcQtyList.stream().filter(q ->
                                    q.getSkuId().equals(skuId) && q.getPurchaseOrderId().equals(purchaseOrderId)
                            ).mapToInt(QcInfoDTO.QcQtyDTO::getTotalQty).sum();
                            if (finishQcQty > purchaseSkuQty) {
                                throw new ServiceException(ApiError.ERROR_99019);
                            }
                        }
                    }


                }

            }

        }
    }


    /**
     * 检查质检数量
     *
     * @param qcInfo
     * @return void
     * @author yl
     * @date 2023-04-20 10:34
     */
    private void checkQcQty(QcInfoDTO.AddDTO qcInfo, String mainId, String purchaseOrderId, String skuId) {
        //校验质检不良+合格不能超过质检数量
        if (qcInfo != null) {
            Integer goodQty = qcInfo.getQcGoodQty();
            Integer badQty = qcInfo.getQcBadQty();
            Integer qcQty = qcInfo.getQcQty();
            //质检总量
            Integer totalQty = qcInfo.getTotalQty();
            if (goodQty + badQty > qcQty) {
                throw new ServiceException(ApiError.ERROR_99016);
            }
            //当采购订单不为空的时候
            if (StringUtils.isNotBlank(purchaseOrderId)) {
                List<String> purOrderIds = Arrays.asList(purchaseOrderId);
                //获取到对应的 订单明细
                List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(purOrderIds);
                //采购的订单数量
                Integer purchaseSkuQty = purOrderDetailList.stream().filter(p -> p.getSkuId().equals(skuId)).
                        mapToInt(PurchaseOrderDetailEntity::getPurchaseQty).sum();
                /**
                 * 根据采访订单id集合
                 * 获取到已质检数量
                 */
                List<QcInfoDTO.QcQtyDTO> qcQtyList = qcInfoService.getPurOrderIds(purOrderIds);
                //已完成的质检
                Integer finishQcQty = qcQtyList.stream().filter(q ->
                        q.getSkuId().equals(skuId) && !q.getMainId().equals(mainId)
                ).mapToInt(QcInfoDTO.QcQtyDTO::getTotalQty).sum();
                if (finishQcQty + totalQty > purchaseSkuQty) {
                    throw new ServiceException(ApiError.ERROR_99017);
                }
            }
        }

    }


}
