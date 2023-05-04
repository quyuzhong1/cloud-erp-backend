package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
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
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcInfoMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Slf4j
public class QcInfoServiceImpl extends SuperServiceImpl<QcInfoMapper, QcInfoEntity> implements QcInfoService {


    @Resource
    private QcProductService qcProductService;

    @Resource
    private QcResultService qcResultService;


    @Resource
    private WarehouseService warehouseService;

    @Resource
    private QcReportDetailService qcReportDetailService;

    @Resource
    private QcRemarkService qcRemarkService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PoInstockService purchaseStorageService;

    @Resource
    private CommonService commonService;


    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PoInstockDetailService poInstockDetailService;


    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;


    @Resource
    private QcReportService qcReportService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    /**
     * 保存 质检单
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QcInfoDTO.SaveOrUpdateDTO dto) {
        //质检单
        QcInfoEntity bill = new QcInfoEntity();
        String code = "";
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        } else {
            QcInfoEntity qc = this.getById(billId);
            if (Objects.isNull(qc)) {
                throw new ServiceException(ApiError.ERROR_99015);
            }
            code = qc.getCode();
        }
        BeanMapper.copy(dto, bill);
        bill.setId(billId);
        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill);
        //检查质检数量
        checkQcQty(dto.getQcInfo(), dto.getId(), dto.getPurchaseOrderId(), dto.getQcProduct().getSkuId());

        String purchaseOrderDetailId = dto.getQcInfo().getPurchaseOrderDetailId();
        //检查采购价目明细
        checkPurchaseOrderDetailId(dto.getPurchaseOrderId(), purchaseOrderDetailId);

        QcBillStatusEnum waitQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.WAIT_QC.getCode());
        bill.setQcStatus(waitQc);
        if (StringUtils.isBlank(code)) {
            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
        }
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
            qcResultService.add(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcRemarkService.add(billId, dto.getRemarkList());
        }
        return result;
    }

    @Override
    public List<QcInfoEntity> listByPoIds(List<String> poIds) {
        return lambdaQuery().in(QcInfoEntity::getPurchaseOrderId, poIds).list();
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
    public QcInfoDTO.ViewDTO view(String id) {
        QcInfoEntity bill = this.getById(id);
        if (Objects.isNull(bill)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        QcInfoDTO.ViewDTO view = new QcInfoDTO.ViewDTO();
        BeanMapper.copy(bill, view);

        //产品信息
        QcProductDTO.ViewDTO qcProduct = qcProductService.getByMainId(id);
        view.setQcProduct(qcProduct);

        //质检信息
        QcResultDTO.ViewDTO qcInfo = qcResultService.getByMainId(id);
        view.setQcInfo(qcInfo);
        qcInfo.setQcFinishTime(bill.getQcFinishTime());

        //质检报告 信息
        List<QcReportDetailDTO.ViewDTO> reportDetailList = qcReportDetailService.getByMainId(id);
        view.setReportDetailList(reportDetailList);

        //质检备注
        List<QcRemarkDTO.AddDTO> remarkList = qcRemarkService.getByMainId(id);
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
    public PagingVO<QcInfoDTO.PagingViewDTO> paging(PagingDTO<QcInfoDTO.PagingParamDTO> dto) {
        QcInfoDTO.PagingParamDTO params = dto.getParams();
        String searchType = params.getSearchType();
        //如果等于所有
        if (searchType.equals(SearchType.ALL)) {
            params.setSearchType("");
        }
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<QcInfoDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
        List<String> supplierIdList = list.stream().map(QcInfoDTO.PagingViewDTO::getSupplierId).collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(QcInfoDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
        List<String> warehouseIdList = list.stream().map(QcInfoDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        List<String> billIdList = list.stream().map(QcInfoDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<QcRemarkEntity> billRemarkList = qcRemarkService.getByMainIdList(billIdList);

        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (QcInfoDTO.PagingViewDTO item : list) {
            QcBillStatusEnum billStatusEnum = item.getQcStatus();
            item.setQcStatusName(billStatusEnum != null ? billStatusEnum.getName() : "");
            //是否内检
            Boolean isInside = item.getIsInside();
            String isInsideType = isInside != null && isInside ? "内部检验" : "外部检验";
            item.setInsideType(isInsideType);
            QcTypeEnum qcTypeEnum = item.getQcType();
            item.setQcTypeName(qcTypeEnum != null ? qcTypeEnum.getName() : "");
            String handleModeDict = item.getHandleModeDict();
            String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setHandleModeName(handleModeName);
            QcResultEnum qcResultEnum = item.getQcResult();
            item.setQcResultName(qcResultEnum != null ? qcResultEnum.getName() : "");
            String skuId = item.getSkuId();
            String skuName = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            item.setSkuName(skuName);

            String skuNo = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);

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
    public void exportQcBill(QcInfoDTO.ExportDTO dto, HttpServletResponse response) {

        String searchType = dto.getSearchType();
        //如果等于所有
        if (searchType.equals(SearchType.ALL)) {
            dto.setSearchType("");
        }
        List<QcInfoDTO.PagingViewDTO> viewList = baseMapper.getExport(dto);
        List<QcBillExportExcelDTO> resultList = new ArrayList<>(viewList.size());
        if (CollectionUtils.isNotEmpty(viewList)) {
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            List<String> supplierIdList = viewList.stream().map(QcInfoDTO.PagingViewDTO::getSupplierId).collect(Collectors.toList());
            List<String> skuIdList = viewList.stream().map(QcInfoDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
            List<String> warehouseIdList = viewList.stream().map(QcInfoDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
            List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
            List<String> billIdList = viewList.stream().map(QcInfoDTO.PagingViewDTO::getId).collect(Collectors.toList());
            List<QcRemarkEntity> billRemarkList = qcRemarkService.getByMainIdList(billIdList);
            List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
            for (QcInfoDTO.PagingViewDTO item : viewList) {
                QcBillExportExcelDTO excelDTO = new QcBillExportExcelDTO();
                BeanMapper.copy(item, excelDTO);
                BigDecimal qcGoodRate = item.getQcGoodRate();
                excelDTO.setQcGoodRate(qcGoodRate != null ? qcGoodRate.toString() + "%" : "");

                BigDecimal qcBadRate = item.getQcBadRate();
                excelDTO.setQcBadRate(qcBadRate != null ? qcBadRate.toString() + "%" : "");
                QcBillStatusEnum billStatusEnum = item.getQcStatus();
                excelDTO.setQcStatusName(billStatusEnum != null ? billStatusEnum.getName() : "");
                QcTypeEnum qcTypeEnum = item.getQcType();
                excelDTO.setQcTypeName(qcTypeEnum != null ? qcTypeEnum.getName() : "");
                String handleModeDict = item.getHandleModeDict();
                String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setHandleModeName(handleModeName);
                QcResultEnum qcResultEnum = item.getQcResult();
                excelDTO.setQcResultName(qcResultEnum != null ? qcResultEnum.getName() : "");
                String skuId = item.getSkuId();
                String skuName = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
                excelDTO.setSkuName(skuName);

                String skuNo = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
                excelDTO.setSkuNo(skuNo);

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
                excelDTO.setInsideType(isInside != null && isInside ? "内部检验" : "外部检验");
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
    public Boolean finish(QcInfoDTO.SaveOrUpdateDTO dto) {
        String id = dto.getId();
        QcInfoEntity bill = this.getById(id);
        if (Objects.isNull(bill)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }

        //质检信息
        QcResultDTO.AddDTO qcInfo = dto.getQcInfo();

        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();

        String purchaseOrderDetailId = dto.getQcInfo().getPurchaseOrderDetailId();
        //检查采购价目明细
        checkPurchaseOrderDetailId(purchaseOrderId, purchaseOrderDetailId);

        Boolean isExist = StringUtils.isNotBlank(purchaseOrderId);
        //检查质检数量
        checkQcQty(qcInfo, dto.getId(), dto.getPurchaseOrderId(), dto.getQcProduct().getSkuId());
        //质检单
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        String code = bill.getCode();
        BeanMapper.copy(dto, bill);

        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill);
        if (StringUtils.isBlank(code)) {
            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
            bill.setCode(code);
        }
        bill.setId(billId);
        bill.setQcFinishTime(LocalDateTime.now());
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
            qcResultService.add(billId, qcInfo);
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcRemarkService.add(billId, dto.getRemarkList());

            //质检类型
            String qcType = qcInfo.getQcType();
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
     * 处理相关数据
     *
     * @param qcUserId
     * @param qcDeptId
     * @param entity
     */
    private void HandleData(String qcUserId, String qcDeptId, QcInfoEntity entity) {

        //质检员
        if (StringUtils.isNotBlank(qcUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(qcUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setQcUserName(userDTO.getUserName());
        }
        //质检部门
        if (StringUtils.isNotBlank(qcDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(qcDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setQcDeptName(depart.getName());
        }
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
    private void autoStockInBill(String billId, QcResultDTO.AddDTO qcInfo, String purchaseOrderId, String warehouseId) {
        PoInstockDTO.AddDTO dto = new PoInstockDTO.AddDTO();
        List<PoInstockDetailDTO.AddDTO> details = new ArrayList<>(1);
        PoInstockDetailDTO.AddDTO detail = new PoInstockDetailDTO.AddDTO();
        detail.setExceedQty(0);
        detail.setStockInQty(qcInfo.getTotalQty());
        detail.setSourceDetailId(qcInfo.getId());
        detail.setPurchaseOrderDetailId(qcInfo.getPurchaseOrderDetailId());
        details.add(detail);
        dto.setDetails(details);
        dto.setSourceId(billId);
        dto.setSourceType(SourceTypeEnum.QC_BILL.getCode());
        dto.setPurchaseOrderId(purchaseOrderId);
        dto.setDeliveryWarehouseId(warehouseId);
        String userId = commonService.getUserInfo().getUid();
        dto.setStockInUserId(userId);
        //获取部门信息
        SysDepartmentUserNumberDTO depart = sysUserFeign.getDeptByUserId(userId);
        dto.setStockInDeptId(depart.getDepartmentId());
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
     * 完成质检，免检
     * 当是 质检类型为b2b 质检的时候
     * 自动批量完成入库单
     *
     * @return void
     * @author yl
     * @date 2023-04-20 14:55
     */
    private void autoBatchStockInBill(List<String> idList) {
        String b2bQcType = QcTypeEnum.B2B_OUTSIDE_QC.getCode();
        List<QcResultDTO.StockInDTO> stockInList = qcResultService.getStockIn(idList);
        //只要有采购订单的以及是b2b质检类型
        stockInList = stockInList.stream().filter(s -> StringUtils.isNotBlank(s.getPurchaseOrderId()) &&
                b2bQcType.equals(s.getQcType())).collect(Collectors.toList());

        String sourceType = SourceTypeEnum.QC_BILL.getCode();
        String userId = commonService.getUserInfo().getUid();
        //获取部门信息
        SysDepartmentUserNumberDTO depart = sysUserFeign.getDeptByUserId(userId);

        //以质检单
        Map<String, List<QcResultDTO.StockInDTO>> map = stockInList.stream().collect(Collectors.groupingBy(QcResultDTO.StockInDTO::getMainId));
        List<PoInstockDTO.AddDTO> addList = new ArrayList<>(map.size());

        for (Map.Entry<String, List<QcResultDTO.StockInDTO>> entry : map.entrySet()) {
            String mainId = entry.getKey();
            List<QcResultDTO.StockInDTO> qcList = entry.getValue();
            PoInstockDTO.AddDTO addStockIn = new PoInstockDTO.AddDTO();
            addStockIn.setSourceId(mainId);
            addStockIn.setSourceType(sourceType);
            addStockIn.setStockInUserId(userId);
            addStockIn.setStockInDeptId(depart.getDepartmentId());
            addStockIn.setPurchaseOrderId(qcList.get(0).getPurchaseOrderId());
            List<PoInstockDetailDTO.AddDTO> details = new ArrayList<>(qcList.size());
            for (QcResultDTO.StockInDTO qcItem : qcList) {
                PoInstockDetailDTO.AddDTO detailAdd = new PoInstockDetailDTO.AddDTO();
                detailAdd.setPurchaseOrderDetailId(qcItem.getPurchaseOrderDetailId());
                detailAdd.setSourceDetailId(qcItem.getId());
                detailAdd.setStockInQty(qcItem.getTotalQty());
                detailAdd.setExceedQty(0);
                details.add(detailAdd);
            }
            addList.add(addStockIn);
        }
        //批量生成 入库单
        purchaseStorageService.batchAdd(addList);
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
    public Boolean draft(QcInfoDTO.SaveOrUpdateDTO dto) {
        //质检单
        QcInfoEntity bill = new QcInfoEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        bill.setId(billId);
        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill);
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
            qcResultService.add(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcRemarkService.add(billId, dto.getRemarkList());
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
    public Boolean exemption(QcInfoDTO.SaveOrUpdateDTO dto) {
        String id = dto.getId();
        QcInfoEntity bill = this.getById(id);
        if (Objects.isNull(bill)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        //质检信息
        QcResultDTO.AddDTO qcInfo = dto.getQcInfo();
        //采购订单id
        String purchaseOrderId = dto.getPurchaseOrderId();
        String purchaseOrderDetailId = dto.getQcInfo().getPurchaseOrderDetailId();
        //检查采购价目明细
        checkPurchaseOrderDetailId(purchaseOrderId, purchaseOrderDetailId);
        //免检设置为0
        qcInfo.setQcBadQty(0);
        qcInfo.setQcGoodQty(0);
        qcInfo.setQcQty(0);
        Boolean isExist = StringUtils.isNotBlank(purchaseOrderId);
        //检查质检数量
        checkQcQty(qcInfo, dto.getId(), dto.getPurchaseOrderId(), dto.getQcProduct().getSkuId());

        String code = bill.getCode();
        BeanMapper.copy(dto, bill);
        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill);
        bill.setId(id);
        bill.setQcFinishTime(LocalDateTime.now());
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
            qcProductService.add(id, dto.getQcProduct());
            //质检信息 暂存
            qcResultService.add(id, qcInfo);
            //质检报告 暂存
            qcReportDetailService.add(id, dto.getReportDetailList());
            //质检备注暂存
            qcRemarkService.add(id, dto.getRemarkList());

            //质检类型
            String qcType = qcInfo.getQcType();
            String b2bQc = QcTypeEnum.B2B_OUTSIDE_QC.getCode();
            //当是 b2b 质检的时候 生成入库单
            if (b2bQc.equals(qcType) && isExist) {
                //生成入库单
                autoStockInBill(id, qcInfo, purchaseOrderId, warehouseId);
            }

            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("完成一个免检质检单【%s】", code), ModuleTypeEnum.QC_ORDER.getCode(), id, "新增操作");
        }
        return result;
    }

    private void checkPurchaseOrderDetailId(String purchaseOrderId, String purchaseOrderDetailId) {
        //当采购订单id 不为空的时候 采购明细也不能为空
        if (StringUtils.isNotBlank(purchaseOrderId)) {
            if (StringUtils.isBlank(purchaseOrderDetailId)) {
                throw new ServiceException(ApiError.ERROR_99006);
            }
        }
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
        List<QcInfoEntity> qcList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(qcList)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        long count = qcList.stream().filter(s -> !s.getQcStatus().getCode().equals(qcStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99018);
        }
        //批量检查
        batchCheckQcQty(qcList, false);
        LocalDateTime now = LocalDateTime.now();
        //质检状态
        QcBillStatusEnum finishQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.FINISH_QC.getCode());
        for (QcInfoEntity item : qcList) {
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
        Boolean result = this.updateBatchById(qcList);
        if (result) {
            //自动完成入库单
            this.autoBatchStockInBill(ids);
        }
        return result;

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
        List<QcInfoEntity> qcList = this.listByIds(ids);
        long count = qcList.stream().filter(s -> !s.getQcStatus().getCode().equals(qcStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99020);
        }
        //批量检查
        batchCheckQcQty(qcList, true);
        LocalDateTime now = LocalDateTime.now();
        //质检状态
        QcBillStatusEnum exemption = QcBillStatusEnum.getByCode(QcBillStatusEnum.EXEMPTION.getCode());
        for (QcInfoEntity item : qcList) {
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
            qcResultService.updateQcQty(ids);

            //自动完成入库单
            this.autoBatchStockInBill(ids);
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
        List<QcInfoEntity> qcList = this.listByIds(ids);
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
        List<QcInfoEntity> qcList = this.listByIds(ids);
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
        Boolean result = this.removeByIds(ids);
        qcResultService.removeByMainIds(ids);
        qcRemarkService.removeByMainIds(ids);
        qcReportDetailService.removeByMainIds(ids);
        qcProductService.removeByMainIds(ids);
        return result;
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
        List<QcInfoEntity> qcList = this.listByIds(ids);
        List<String> statusList = new ArrayList<>(2);
        statusList.add(QcBillStatusEnum.EXEMPTION.getCode());
        statusList.add(QcBillStatusEnum.FINISH_QC.getCode());
        long count = qcList.stream().filter(s -> !statusList.contains(s.getQcStatus().getCode())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99023);
        }
        //入库的
        List<PoInstockEntity> stockInList = purchaseStorageService.getStockInBySourceIds(ids);
        long stockInCount = stockInList.stream().filter(s -> !s.getInvalidStatus()).count();
        if (stockInCount > 0) {
            throw new ServiceException(ApiError.ERROR_99027);
        }

        //退货单
        List<PurchaseReturnOrderEntity> returnList = purchaseReturnOrderService.listBySourceIds(ids);
        long returnCount = returnList.stream().filter(s -> !s.getInvalidStatus()).count();
        if (returnCount > 0) {
            throw new ServiceException(ApiError.ERROR_99028);
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
    public Boolean assign(QcInfoDTO.AssignDTO dto) {
        List<String> ids = dto.getIds();
        String qcUserId = dto.getQcUserId();
        List<QcInfoEntity> qcList = this.listByIds(ids);
        String waitQcCode = QcBillStatusEnum.WAIT_QC.getCode();
        long count = qcList.stream().filter(q -> !q.getQcStatus().getCode().equals(waitQcCode)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99060);
        }

        SysDepartmentUserNumberDTO userInfo = sysUserFeign.getDeptByUserId(qcUserId);
        for (QcInfoEntity item : qcList) {
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
    public Boolean updateHandleMode(QcResultDTO.UpdateHandleModeDTO dto) {
        List<String> ids = dto.getIds();
        List<QcInfoEntity> qcList = this.listByIds(ids);
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
        return qcResultService.updateHandleMode(ids, handleModeDict);
    }


    /**
     * 获取tab 类型数量
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.QcBillDTO.TabListDTO>
     * @author yl
     * @date 2023-04-21 16:48
     */
    @Override
    public List<QcInfoDTO.TabListDTO> tabList() {
        List<QcInfoEntity> list = this.list();
        List<QcInfoDTO.TabListDTO> resultList = new ArrayList<>(4);
        QcInfoDTO.TabListDTO all = new QcInfoDTO.TabListDTO();
        all.setCount(list.size());
        all.setSearchType(SearchType.ALL);
        all.setTypeName("全部");
        resultList.add(all);
        QcInfoDTO.TabListDTO waitQc = new QcInfoDTO.TabListDTO();
        String waitQcType = QcBillStatusEnum.WAIT_QC.getCode();
        waitQc.setCount((int) list.stream().filter(l -> waitQcType.equals(l.getQcStatus().getCode())).count());
        waitQc.setSearchType(waitQcType);
        waitQc.setTypeName(QcBillStatusEnum.WAIT_QC.getName());
        resultList.add(waitQc);

        QcInfoDTO.TabListDTO finishQc = new QcInfoDTO.TabListDTO();
        String finishQcType = QcBillStatusEnum.FINISH_QC.getCode();
        finishQc.setCount((int) list.stream().filter(l -> finishQcType.equals(l.getQcStatus().getCode())).count());
        finishQc.setSearchType(finishQcType);
        finishQc.setTypeName(QcBillStatusEnum.FINISH_QC.getName());
        resultList.add(finishQc);

        QcInfoDTO.TabListDTO cancelQc = new QcInfoDTO.TabListDTO();
        String cancelQcType = QcBillStatusEnum.CANCEL.getCode();
        cancelQc.setCount((int) list.stream().filter(l -> cancelQcType.equals(l.getQcStatus().getCode())).count());
        cancelQc.setSearchType(cancelQcType);
        cancelQc.setTypeName(QcBillStatusEnum.CANCEL.getName());
        resultList.add(cancelQc);
        return resultList;
    }


    /**
     * 下推 退货单 显示
     *
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     * @author yl
     * @date 2023-04-23 12:07
     */
    @Override
    public List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids) {
        List<QcInfoEntity> qcInfoList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(qcInfoList)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        String finishQcCode = QcBillStatusEnum.FINISH_QC.getCode();
        long count = qcInfoList.stream().filter(f -> !f.getQcStatus().getCode().equals(finishQcCode)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99029);
        }
        List<QcResultEntity> qcResultList = qcResultService.getByMainIdList(ids);
        long handleCount = qcResultList.stream().filter(r -> !r.getHandleModeDict().equals(WmsConstant.QC_RESULT_HANDLE_MODE)).count();
        if (handleCount > 0) {
            throw new ServiceException(ApiError.ERROR_99029);
        }

        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = baseMapper.viewGeneratePurchaseReturnOrder(ids);
        List<String> skuIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        //采购订单id
        List<String> poIdList = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getPurchaseOrderId).collect(Collectors.toList());
        //采购订单明细id
        List<String> podIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        //获取到订单采购信息
        List<PurchaseOrderDTO.PurchaseOrderInfoDTO> purchaseOrderList = scmTaskFeign.getByOrderIds(poIdList);
        List<PoInstockDetailEntity> stockInSkuList = poInstockDetailService.listDetailByPodIds(podIds);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        stockInSkuList = stockInSkuList.stream().filter(s -> approveStatus.equals(s.getApproveStatus())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        for (PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO dto : list) {
            //来源类型
            dto.setSourceType(SourceTypeEnum.QC_BILL.getCode());
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
            dto.setProductName(productName);
            dto.setSourceDetailId(dto.getPurchaseOrderDetailId());
            PurchaseOrderDTO.PurchaseOrderInfoDTO purchaseOrder = purchaseOrderList.stream().filter(P -> P.getPurchaseOrderId().equals(dto.getPurchaseOrderId())).findFirst().orElse(null);
            if (purchaseOrder != null) {
                dto.setSupplierName(purchaseOrder.getSupplierName());
                dto.setDeliveryWarehouseName(purchaseOrder.getDeliveryWarehouseName());
            }
            Integer qty = stockInSkuList.stream().filter(s -> s.getSkuId().equals(dto.getSkuId()) &&
                    dto.getPurchaseOrderDetailId().equals(s.getPurchaseOrderDetailId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getStockInQty())).orElse(0);
            dto.setStockInQty(qty);
            //币种符号
            String currencySymbol = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(dto.getPurchaseOrderDetailId())).map(PurchaseOrderDetailEntity::getCurrencySymbol).findFirst().orElse(null);
            dto.setCurrencySymbol(currencySymbol);

            //单价
            BigDecimal taxPrice = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(dto.getPurchaseOrderDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getTaxPrice())).orElse(BigDecimal.ZERO);
            dto.setTaxPrice(taxPrice);

            //相同采购单号清空后面数据的采购单号和供应商
            boolean contains = list.contains(dto.getPurchaseOrderId());
            if (contains) {
                dto.setPurchaseOrderCode(null);
                dto.setSupplierName(null);
                continue;
            }
        }

        return list;
    }


    /**
     * 下推退货单
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-24 9:35
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generatePurchaseReturnOrder(PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        List<String> ids = dto.getList().stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceId).collect(Collectors.toList());
        List<QcInfoEntity> qcInfoList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(qcInfoList)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        String finishQcCode = QcBillStatusEnum.FINISH_QC.getCode();

        long count = qcInfoList.stream().filter(f -> !f.getQcStatus().getCode().equals(finishQcCode)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99029);
        }
        List<QcResultEntity> qcResultList = qcResultService.getByMainIdList(ids);
        long handleCount = qcResultList.stream().filter(r -> !r.getHandleModeDict().equals(WmsConstant.QC_RESULT_HANDLE_MODE)).count();
        if (handleCount > 0) {
            throw new ServiceException(ApiError.ERROR_99029);
        }


        LoginUser userInfo = commonService.getUserInfo();
        List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> list = dto.getList();
        //采购订单明细id
        List<String> podIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceDetailId).collect(Collectors.toList());
        //采购订单id集合
        List<String> poIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getPurchaseOrderId).collect(Collectors.toList());

        List<PurchaseOrderEntity> purchaseOrderDbList = scmTaskFeign.listPurchaseOrderByIds(poIds);

        //收获
        List<WarehouseReceiveEntity> receiveList = warehouseReceiveService.listByPurchaseOrderIds(poIds);

        List<PoInstockDetailEntity> stockInSkuList = poInstockDetailService.listDetailByPodIds(podIds);
        List<PurchaseReturnOrderDTO.AddDTO> addList = new ArrayList<>();

        String qcBill = SourceTypeEnum.QC_BILL.getCode();
        Map<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> map = list.stream().collect(Collectors.groupingBy(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceId));

        for (Map.Entry<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> entry : map.entrySet()) {
            String sourceId = entry.getKey();
            List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> value = entry.getValue();

            PurchaseReturnOrderDTO.AddDTO addDTO = new PurchaseReturnOrderDTO.AddDTO();
            //质检单
            QcInfoEntity qcInfoEntity = qcInfoList.stream().filter(obj -> obj.getId().equals(sourceId)).findFirst().orElse(null);
            if (Objects.isNull(qcInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_99015);
            }

            addDTO.setSourceType(qcBill);
            addDTO.setSourceId(sourceId);
            //退货详情
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            PoInstockDTO.GeneratePurchaseReturnOrderDTO purchaseReturnOrderDTO = value.get(0);
            //采购订单id
            String purchaseOrderId = purchaseReturnOrderDTO.getPurchaseOrderId();

            for (PoInstockDTO.GeneratePurchaseReturnOrderDTO detail : value) {
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
                //验证退货数量
                Integer stockInQty = stockInSkuList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detail.getSourceDetailId())).map(e -> e.getStockInQty()).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(stockInQty)) {
                    throw new ServiceException(1, String.format("SKU【%s】未找到对应数量", detail.getSkuNo()));
                }
                if (MathUtil.compareTo(detail.getRealityReturnQty(), stockInQty) > 0) {
                    throw new ServiceException(1, String.format("SKU【%s】实退数量不能大于【%s】", detail.getSkuNo(), stockInQty));
                }
                BeanMapperUtils.copy(detail, addDetailDTO);
                addDetailDTO.setPurchaseOrderDetailId(detail.getPurchaseOrderDetailId());
                addDetailDTO.setReturnQty(detail.getRealityReturnQty());
                addDetailList.add(addDetailDTO);
            }
            addDTO.setReturnMode(purchaseReturnOrderDTO.getReturnMode());
            addDTO.setPurchasePriceDetailList(addDetailList);
            addDTO.setReturnUserId(userInfo.getUid());

            addDTO.setPurchaseOrderId(qcInfoEntity.getPurchaseOrderId());
            String receiveOrgId = receiveList.stream().filter(r -> r.getPurchaseOrderId().equals(purchaseOrderId)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getReceiveOrgId())).orElse("");
            addDTO.setReturnOrgId(receiveOrgId);
            addDTO.setReturnWarehouseId(qcInfoEntity.getWarehouseId());
            addDTO.setReturnRemark(purchaseReturnOrderDTO.getRemark());
            addDTO.setSupplierId(qcInfoEntity.getSupplierId());

            //采购订单
            PurchaseOrderEntity purchaseOrderInfo = purchaseOrderDbList.stream().filter(p -> p.getId().equals(purchaseOrderId)).findFirst().orElse(null);
            String purchaseUserId = "";
            if (purchaseOrderInfo != null) {
                purchaseUserId = purchaseOrderInfo.getPurchaseUserId();
            }
            addDTO.setPurchaseUserId(purchaseUserId);
            addList.add(addDTO);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            addList.forEach(obj -> purchaseReturnOrderService.add(obj));
        }
        return Boolean.TRUE;
    }


    /**
     * 入库单自动下推质检单
     *
     * @param dto
     * @return
     * @author yl
     * @date 2023-04-26 9:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoReceiveToQcDTO(List<QcInfoDTO.ReceiveToQcDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return Boolean.TRUE;
        }
        List<QcInfoEntity> addQcList = new ArrayList<>(dto.size());
        //质检结果
        List<QcResultEntity> addQcResultList = new ArrayList<>(dto.size());

        //质检产品
        List<QcProductEntity> addQcProductList = new ArrayList<>(dto.size());

        //质检报告
        List<QcReportDetailEntity> addQcReportDetailList = new ArrayList<>(dto.size());


        //质检类型
        List<String> qcTypeList = dto.stream().map(QcInfoDTO.ReceiveToQcDTO::getQcType).collect(Collectors.toList());
        //质检类型的集合
        List<QcReportDTO.ListDTO> list = qcReportService.listByQcType(qcTypeList);
        Map<String, List<QcReportDTO.ListDTO>> qcTypeMap = list.stream().collect(Collectors.groupingBy(QcReportDTO.ListDTO::getQcType));
        String qcUserId = commonService.getUserInfo().getUid();
        String qcUserName = commonService.getUserInfo().getUserName();
        String departId = "";
        String departName = "";
        //质检员
        if (StringUtils.isNotBlank(qcUserId)) {
            SysDepartmentUserNumberDTO userDTO = sysUserFeign.getDeptByUserId(qcUserId);
            departId = userDTO.getDepartmentId();
            departName = userDTO.getDepartmentName();
        }


        for (QcInfoDTO.ReceiveToQcDTO item : dto) {
            QcInfoEntity qcInfo = new QcInfoEntity();
            String id = IdWorker.getIdStr();
            qcInfo.setId(id);
            qcInfo.setPurchaseOrderCode(item.getPurchaseOrderCode());
            qcInfo.setPurchaseOrderId(item.getPurchaseOrderId());
            qcInfo.setSupplierId(item.getSupplierId());
            qcInfo.setWarehouseId(item.getDeliveryWarehouseId());
            qcInfo.setQcDeptId(departId);
            qcInfo.setQcDeptName(departName);
            qcInfo.setQcUserId(qcUserId);
            qcInfo.setQcUserName(qcUserName);
            qcInfo.setSourceId(item.getSourceId());
            qcInfo.setSourceType(item.getSourceType());
            addQcList.add(qcInfo);
            //质检结果
            QcResultEntity qcResult = new QcResultEntity();
            qcResult.setMainId(id);
            String qcType = item.getQcType();
            Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
            qcResult.setQcType(item.getQcType());
            qcResult.setIsInside(isInside);
            qcResult.setTotalQty(item.getTotalQty());
            qcResult.setPurchaseOrderDetailId(item.getPurchaseOrderDetailId());
            addQcResultList.add(qcResult);

            //质检产品
            QcProductEntity qcProduct = new QcProductEntity();
            BeanMapper.copy(item, qcProduct);
            qcProduct.setMainId(id);
            addQcProductList.add(qcProduct);

            //质检报告信息
            List<QcReportDTO.ListDTO> reportList = qcTypeMap.get(qcType);
            if (CollectionUtils.isNotEmpty(reportList)) {
                for (QcReportDTO.ListDTO report : reportList) {
                    QcReportDetailEntity qcReportDetail = new QcReportDetailEntity();
                    qcReportDetail.setMainId(id);
                    qcReportDetail.setQcReportId(report.getQcReportId());
                    addQcReportDetailList.add(qcReportDetail);
                }

            }

        }
        //添加质检单
        Boolean batchQc = this.saveBatch(addQcList);
        if (batchQc) {
            qcProductService.saveBatch(addQcProductList);
            qcResultService.saveBatch(addQcResultList);
            qcReportDetailService.saveBatch(addQcReportDetailList);
        }
        return batchQc;
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
    private void batchCheckQcQty(List<QcInfoEntity> qcList, Boolean isExemption) {
        if (CollectionUtils.isNotEmpty(qcList)) {
            qcList.sort(Comparator.comparing(QcInfoEntity::getCreateTime, Comparator.reverseOrder()));
            List<String> ids = qcList.stream().map(QcInfoEntity::getId).collect(Collectors.toList());
            //质检信息
            List<QcResultEntity> qcInfoList = qcResultService.getByMainIdList(ids);

            //质检产品信息
            List<QcProductEntity> qcProductList = qcProductService.getByMainIdList(ids);
            List<String> purOrderIds = qcList.stream().map(QcInfoEntity::getPurchaseOrderId).collect(Collectors.toList());
            //采购订单明细id集合
            List<String> podIds = qcInfoList.stream().map(QcResultEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
            //获取到对应的 订单明细
            List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
            /**
             * 根据采访订单id集合
             * 获取到已质检数量
             */
            List<QcResultDTO.QcQtyDTO> qcQtyList = qcResultService.getPurOrderIds(purOrderIds);

            for (QcInfoEntity item : qcList) {
                String mainId = item.getId();
                QcResultEntity qcInfo = qcInfoList.stream().filter(q -> q.getMainId().equals(mainId)).findFirst().orElse(null);
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
                            ).mapToInt(QcResultDTO.QcQtyDTO::getTotalQty).sum();
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
    private void checkQcQty(QcResultDTO.AddDTO qcInfo, String mainId, String purchaseOrderId, String skuId) {
        //校验质检不良+合格不能超过质检数量
        if (qcInfo != null) {
            Integer goodQty = qcInfo.getQcGoodQty() != null ? qcInfo.getQcGoodQty() : 0;
            Integer badQty = qcInfo.getQcBadQty() != null ? qcInfo.getQcBadQty() : 0;
            Integer qcQty = qcInfo.getQcQty() != null ? qcInfo.getQcQty() : 0;
            //质检总量
            Integer totalQty = qcInfo.getTotalQty() != null ? qcInfo.getTotalQty() : 0;
            if (goodQty + badQty > qcQty) {
                throw new ServiceException(ApiError.ERROR_99016);
            }
            //采购订单详情id
            String orderDetailId = qcInfo.getPurchaseOrderDetailId();
            //当采购订单不为空的时候
            if (StringUtils.isNotBlank(orderDetailId)) {
                List<String> podIds = Arrays.asList(orderDetailId);
                //获取到对应的 订单明细
                List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
                //采购的订单数量
                Integer purchaseSkuQty = purOrderDetailList.stream().filter(p -> p.getSkuId().equals(skuId)).
                        mapToInt(PurchaseOrderDetailEntity::getPurchaseQty).sum();
                /**
                 * 根据采访订单id集合
                 * 获取到已质检数量
                 */
                List<QcResultDTO.QcQtyDTO> qcQtyList = qcResultService.getPurOrderIds(Arrays.asList(purchaseOrderId));
                //已完成的质检
                Integer finishQcQty = qcQtyList.stream().filter(q ->
                        q.getSkuId().equals(skuId) && !q.getMainId().equals(mainId)
                ).mapToInt(QcResultDTO.QcQtyDTO::getTotalQty).sum();
                if (finishQcQty + totalQty > purchaseSkuQty) {
                    throw new ServiceException(ApiError.ERROR_99017);
                }
            }
        }

    }


}
