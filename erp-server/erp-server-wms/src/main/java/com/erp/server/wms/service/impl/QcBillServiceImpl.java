package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.QcBillEntity;
import com.erp.model.wms.entity.QcBillRemarkEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    /**
     * 暂存 质检单
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        List<QcBillDTO.PagingViewDTO> viewList = baseMapper.getExport(dto);


    }
}
