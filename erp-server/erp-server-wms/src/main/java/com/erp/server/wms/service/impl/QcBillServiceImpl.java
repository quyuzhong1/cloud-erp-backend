package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.PagingDTO;
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
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.QcBillExportExcelDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.QcBillEntity;
import com.erp.model.wms.entity.QcBillRemarkEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    private PurchaseStockInService   purchaseStockInService;

    /**
     * 暂存 质检单
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
            qcProductService.draft(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.draft(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.draft(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.draft(billId, dto.getRemarkList());
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
        //检查质检数量
        checkQcQty(qcInfo, dto.getPurchaseOrderId(), dto.getQcProduct().getSkuId());
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
            qcProductService.draft(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.draft(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.draft(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.draft(billId, dto.getRemarkList());

            //质检类型
            String qcType = qcInfo.getQcType().getCode();
            String b2bQc = QcTypeEnum.B2B_OUTSIDE_QC.getCode();
            //当是 b2b 质检的时候 生成入库单
            if(b2bQc.equals(qcType)){

            }
        }
        return result;


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
            qcProductService.draft(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.draft(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.draft(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.draft(billId, dto.getRemarkList());
        }
        return result;
    }

    /**
     * 检查质检数量
     *
     * @param qcInfo
     * @return void
     * @author yl
     * @date 2023-04-20 10:34
     */
    private void checkQcQty(QcInfoDTO.AddDTO qcInfo, String purchaseOrderId, String skuId) {
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
                Integer finishQcQty = qcQtyList.stream().filter(q -> q.getSkuId().equals(skuId)).
                        mapToInt(QcInfoDTO.QcQtyDTO::getTotalQty).sum();
                if (finishQcQty + totalQty > purchaseSkuQty) {
                    throw new ServiceException(ApiError.ERROR_99017);
                }
            }
        }

    }


}
