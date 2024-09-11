package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.QcInsideTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.QcBillExportExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.aspect.DataPermissionAspect;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcInfoMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.query.QcInfoQueryHandler;
import com.erp.server.wms.service.*;
import com.erp.server.wms.utils.QcUtils;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_DAILY_QC_BILL;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_BILL;

/**
 * <p>
 * 质检单表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@RefreshScope
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
    private PoInstockService poInstockService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PoInstockDetailService poInstockDetailService;


    @Resource
    private PoReturnService poReturnService;


    @Resource
    private QcReportService qcReportService;


    //收货单
    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    //收货明细
    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Autowired
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private PoReturnDetailService poReturnDetailService;
    @Resource
    private ProductDetailService productDetailService;

    @Value("${fdfs.publicUrl:''}")
    private String filePublicUrl;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private QcInfoQueryHandler qcInfoQueryHandler;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

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
        //校验 【箱规-长宽高】必须大于等于【包装尺寸-长宽高】【为空则忽略不校验】【长，宽，高分开校验】
        QcProductDTO.AddDTO qcProduct = dto.getQcProduct();
        if (ObjectUtils.isNotEmpty(qcProduct)) {
           compareDimensions(qcProduct.getBoxLength(), qcProduct.getProductLength(), ApiError.ERROR_LENGTH_BOX_LITTER_THAN_PRODUCT);
           compareDimensions(qcProduct.getBoxWidth(), qcProduct.getProductWidth(), ApiError.ERROR_WIDTH_BOX_LITTER_THAN_PRODUCT);
           compareDimensions(qcProduct.getBoxHeight(), qcProduct.getProductHeight(), ApiError.ERROR_HEIGHT_BOX_LITTER_THAN_PRODUCT);
        }

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
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill, dto.getSourceType(), dto.getSourceId());

        //采购订单明细
        String purchaseOrderDetailId = dto.getQcInfo().getPurchaseOrderDetailId();
        String skuId = dto.getQcProduct().getSkuId();
        List<PurchaseOrderDetailEntity> purOrderDetailList = Collections.emptyList();
        //当采购订单明细id 为空的时候 sku id 不能为空
        if (StringUtils.isBlank(purchaseOrderDetailId)) {
            if (StringUtils.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
        } else {
            List<String> podIds = Arrays.asList(purchaseOrderDetailId);
            //获取到对应的 订单明细
            purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
            skuId = purOrderDetailList.stream().filter(p -> p.getId().equals(purchaseOrderDetailId))
                    .map(PurchaseOrderDetailEntity::getSkuId).findFirst().orElse("");
        }
        if (StringUtils.isBlank(skuId)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }

        //检查质检数量
        checkQcQty(dto.getQcInfo(), dto.getId(), dto.getPurchaseOrderId(), skuId);

        //检查采购价目明细
        checkPurchaseOrderDetailId(dto.getPurchaseOrderId(), purchaseOrderDetailId);

        QcBillStatusEnum waitQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.WAIT_QC.getCode());
        bill.setQcStatus(waitQc);
        if (StringUtils.isBlank(code)) {
            code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QC);
        }
        bill.setCode(code);
        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();
        if (StringUtils.isNotBlank(purchaseOrderId)) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                //采购订单验证
                String skuNos = purOrderDetailList.stream().filter(obj -> !StrUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus()))
                        .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                if (StrUtil.isNotBlank(skuNos)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,purchaseOrder.getCode(),skuNos);
                }
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                bill.setWarehouseId(purchaseOrder.getDeliveryWarehouseId());
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }

        }
        String sourceDetailId = dto.getSourceDetailId();
        bill.setSourceDetailId(sourceDetailId);

        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct(), skuId);
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

        // 退货签收单处理
        if(Objects.equals(view.getSourceType(), SourceTypeEnum.SO_RETURN_RECEIVE.getCode())) {
          SoReturnReceiveEntity soReturnReceiveEntity =  soReturnReceiveService.getById(view.getSourceId());
          if(Objects.nonNull(soReturnReceiveEntity)) {
              view.setPurchaseOrderCode(soReturnReceiveEntity.getCode());
              view.setPurchaseOrderId("");
          }
        }

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
        params.setPermissionSql(dto.getPermissionSql());
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

        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        // 退货签收单单号显示
        List<String> soReturnReceiveIds = list.stream().filter(r->Objects.equals(r.getSourceType(), SourceTypeEnum.SO_RETURN_RECEIVE.getCode())).map(QcInfoDTO.PagingViewDTO::getSourceId).distinct().collect(Collectors.toList());
        List<SoReturnReceiveEntity> receiveReturnReceiveList = Lists.newArrayList();
        if(CollUtil.isNotEmpty(soReturnReceiveIds)) {
            receiveReturnReceiveList = soReturnReceiveService.listByIds(soReturnReceiveIds);
        }
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
            String qcSampleResult = QcReCheckResultEnum.getByCode(item.getQcSampleResult());
            item.setQcSampleResultName(StrUtils.isNotEmpty(qcSampleResult) ? qcSampleResult : "");
            item.setRemark(remark);

            // 退换签收单号处理
            if(Objects.equals(item.getSourceType(),  SourceTypeEnum.SO_RETURN_RECEIVE.getCode())) {
                SoReturnReceiveEntity soReturnReceiveEntity =  receiveReturnReceiveList.stream().filter(req -> req.getId().equals(item.getSourceId())).findFirst().orElse(new SoReturnReceiveEntity());
                item.setPurchaseOrderCode(soReturnReceiveEntity.getCode());
            }


        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出质检单信息
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-04-19 18:38
     */
    @Override
    public void exportQcBill(QcInfoDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("质检单数据", EXPORT_WMS_QC_BILL.getCode(), dto);
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
        String skuId = dto.getQcProduct().getSkuId();
        List<PurchaseOrderDetailEntity> purOrderDetailList = Collections.emptyList();
        //当采购订单明细id 为空的时候 sku id 不能为空
        if (StringUtils.isBlank(purchaseOrderDetailId)) {
            if (StringUtils.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
        } else {
            List<String> podIds = Arrays.asList(purchaseOrderDetailId);
            //获取到对应的 订单明细
            purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
            skuId = purOrderDetailList.stream().filter(p -> p.getId().equals(purchaseOrderDetailId))
                    .map(PurchaseOrderDetailEntity::getSkuId).findFirst().orElse("");
        }
        if (StringUtils.isBlank(skuId)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }

        //检查质检数量
        checkQcQty(qcInfo, dto.getId(), dto.getPurchaseOrderId(), skuId);
        //质检单
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        String code = bill.getCode();
        BeanMapper.copy(dto, bill);

        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill, dto.getSourceType(), dto.getSourceId());
        if (StringUtils.isBlank(code)) {
//            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
            code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QC);
            bill.setCode(code);
        }
        bill.setId(billId);
        bill.setQcFinishTime(LocalDateTime.now());
        QcBillStatusEnum finishQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.FINISH_QC.getCode());
        bill.setQcStatus(finishQc);
        String warehouseId = dto.getWarehouseId();
        if (isExist) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                //采购订单验证
                String skuNos = purOrderDetailList.stream().filter(obj -> !StrUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus()))
                        .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                if (StrUtil.isNotBlank(skuNos)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,purchaseOrder.getCode(),skuNos);
                }
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }
        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct(), skuId);
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
            //新品首批回填SKU的尺寸信息
            updateProductPack(Arrays.asList(billId));

            //异步发送通知
            qcResultService.sendQcResultMsg(Arrays.asList(billId));
            operateLogService.addModuleOperateLog(String.format("新增了一个质检单【%s】", code), ModuleTypeEnum.QC_ORDER.getCode(), billId, "新增操作");
        }
        return result;
    }

    /**
     * @description:回填产品信息
     * @author Will
     * @date: 2023/9/20 11:21
     * @param billIdList
     */
    @Override
    public void updateProductPack (List<String> billIdList) {
        /**
         * 采购订单为新品首批、并且质检完成后减产品尺寸、外箱尺寸、产品净重、外形重量
         */
        if (CollectionUtils.isEmpty(billIdList)) {
            return;
        }
        List<QcInfoEntity> qcInfoEntityList = this.listByIds(billIdList);
        if (CollectionUtils.isEmpty(qcInfoEntityList)) {
            return;
        }
        //是否存在权限
        Boolean isExist = isExistAuth(billIdList, "wms:qcBill:updateProductPack", "qc_user_id");
        if (!isExist) {
            return;
        }
        //采购信息
        List<String> poIdList = qcInfoEntityList.stream().filter(obj -> StringUtils.isNotBlank(obj.getPurchaseOrderId())).map(QcInfoEntity::getPurchaseOrderId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(poIdList)) {
            return;
        }
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(poIdList);

        //质检产品信息
        List<String> qcIdList = qcInfoEntityList.stream().map(QcInfoEntity::getId).collect(Collectors.toList());
        List<QcProductEntity> qcProductList = qcProductService.getByMainIdList(qcIdList);

        List<ProductPackDTO> productPactList = new ArrayList<>();

        for (QcInfoEntity qcInfoEntity :  qcInfoEntityList) {
            PurchaseOrderEntity purchaseOrderEntity = purchaseOrderList.stream().filter(obj -> obj.getId().equals(qcInfoEntity.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                continue;
            }
            if (!purchaseOrderEntity.getIsFirstMassProduct()) {
                continue;
            }
            QcProductEntity qcProductEntity = qcProductList.stream().filter(obj -> obj.getMainId().equals(qcInfoEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(qcProductEntity)) {
                throw new ServiceException(ApiError.ERROR_99015);
            }
            long count = productPactList.stream().filter(obj -> obj.getSkuId().equals(qcProductEntity.getSkuId())).count();
            if (count > 0) {
                continue;
            }
            ProductPackDTO productPackDTO = new ProductPackDTO();
            productPackDTO.setSkuId(qcProductEntity.getSkuId());
            productPackDTO.setSkuNo(qcProductEntity.getSkuNo());
            productPackDTO.setProductLength(LengthConverterUtil.cmToMm(qcProductEntity.getProductLength()));
            productPackDTO.setProductWidth(LengthConverterUtil.cmToMm(qcProductEntity.getProductWidth()));
            productPackDTO.setProductHeight(LengthConverterUtil.cmToMm(qcProductEntity.getProductHeight()));
            productPackDTO.setBoxQty(new BigDecimal(qcProductEntity.getBoxQty()));
            productPackDTO.setBoxWeight(qcProductEntity.getBoxWeight());
            productPackDTO.setNetWeight(qcProductEntity.getProductNetWeight());
            productPactList.add(productPackDTO);
        }
        if (CollectionUtils.isEmpty(productPactList)){
            return;
        }
        //plm回填信息
        plmTaskFeign.backFillPackaging(productPactList);
        //异步发送通知
        qcResultService.sendQcBackFillPackaging(productPactList);
    }

    /**
     * @description: 是否存在权限
     * @author Will
     * @date: 2023/10/7 16:21
     * @param billIdList
     * @param menuCode
     * @param menuTableField
     * @return Boolean
     */
    private Boolean isExistAuth (List<String> billIdList,String menuCode,String menuTableField) {
        //判断是否有权限回填产品信息
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(userInfo.getUid());
        UserRequestPermissionsDTO userRequestPermissions = new UserRequestPermissionsDTO();
        List<String> roleIdList = sysUserFeign.getRoleIdList(userInfo.getUid());
        if (roleIdList.contains("1")) {
            userRequestPermissions.setPermissionsCode(menuCode);
            userRequestPermissions.setDataScope(DataPermissionAspect.DATA_SCOPE_ALL);
        } else {
            userRequestPermissions = requestPermissionsList
                    .stream()
                    .filter(p -> p.getPermissionsCode().equals(menuCode))
                    .findFirst()
                    .orElse(null);
        }
        if (ObjectUtils.isEmpty(userRequestPermissions)) {
            return Boolean.FALSE;
        }
        List<String> userList = sysUserFeign.getDepUserList(userInfo.getUid());
        List<String> users = new ArrayList<>();
        List<?> objects = this.listByIds(billIdList);
        for (Object object : objects) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(object));

            if (StringUtils.isBlank(menuTableField)) {
                return Boolean.FALSE;
            }
            String[] tableFields = menuTableField.split(",");
            for (String tableField : tableFields) {
                Object o = jsonObject.get(StrUtils.underlineToCamel(tableField, true));
                if (o == null) {
                    continue;
                }
                users.addAll(Arrays.asList(o.toString().split(",")));
            }
        }
        if (DataPermissionAspect.DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return Boolean.TRUE;
        } else if (DataPermissionAspect.DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            long containsUserCount = users.stream().filter(u -> userList.contains(u)).count();
            if (containsUserCount == 0) {
                return Boolean.FALSE;
            }
        } else if (DataPermissionAspect.DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (!users.contains(userInfo.getUid())) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 处理相关数据
     *
     * @param qcUserId
     * @param qcDeptId
     * @param entity
     */
    private void HandleData(String qcUserId, String qcDeptId, QcInfoEntity entity, String sourceType, String sourceId) {
        //质检员
        if (StringUtils.isNotBlank(qcUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(qcUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
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

        if (SourceTypeEnum.PO_RECEIVE.getCode().equals(sourceType)) {
            WarehouseReceiveEntity info = warehouseReceiveService.getById(sourceId);
            entity.setSourceCode(info.getCode());
        } else if (SourceTypeEnum.PURCHASE_ORDER.getCode().equals(sourceType)) {
            PurchaseOrderEntity info = scmTaskFeign.getPurchaseOrderById(sourceId);
            entity.setSourceCode(info.getCode());
        } else if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(sourceType)) {
            SoReturnReceiveEntity info = soReturnReceiveService.getById(sourceId);
            entity.setSourceCode(info.getCode());
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
        dto.setSourceType(SourceTypeEnum.QC_INFO.getCode());
        dto.setPurchaseOrderId(purchaseOrderId);
        dto.setDeliveryWarehouseId(warehouseId);
        String userId = UserContext.getDefaultLoginUser().getUid();
        dto.setStockInUserId(userId);
        //获取部门信息
        SysDepartmentUserNumberDTO depart = sysUserFeign.getDeptByUserId(userId);
        dto.setStockInDeptId(depart.getDepartmentId());
        //生成结果
        String createResultId = poInstockService.addAndSubmit(dto);
        if (StringUtils.isNotBlank(createResultId)) {
            PoInstockEntity entity = poInstockService.getById(createResultId);
            poInstockService.approve(entity,ApproveTypeEnum.PASS.getStatus(),"", null);
        }
    }


    /**
     * 完成质检，免检
     * 当是 质检类型为b2b 质检的时候 或者来源是采购收货并且收货对应的质检单已全部质检完成
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

        String sourceType = SourceTypeEnum.QC_INFO.getCode();
        String userId = UserContext.getDefaultLoginUser().getUid();
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
            addStockIn.setDetails(details);
            addList.add(addStockIn);
        }
        List<QcInfoEntity> qcInfoEntityList = listByIds(idList);
        List<String> receiveIdList = qcInfoEntityList.stream().filter(v->SourceTypeEnum.PO_RECEIVE.getCode().equals(v.getSourceType())).map(QcInfoEntity::getSourceId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(receiveIdList)){
            warehouseReceiveService.generateStockInWhenQcFinish(receiveIdList);
        }
        //批量生成 入库单
        poInstockService.batchAdd(addList);
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean draft(QcInfoDTO.SaveOrUpdateDTO dto) {
        //质检单
        QcInfoEntity bill = new QcInfoEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        bill.setId(billId);
        //校验 【箱规-长宽高】必须大于等于【包装尺寸-长宽高】【为空则忽略不校验】【长，宽，高分开校验】
        QcProductDTO.AddDTO qcProduct = dto.getQcProduct();
        if (ObjectUtils.isNotEmpty(qcProduct)) {
            compareDimensions(qcProduct.getBoxLength(), qcProduct.getProductLength(), ApiError.ERROR_LENGTH_BOX_LITTER_THAN_PRODUCT);
            compareDimensions(qcProduct.getBoxWidth(), qcProduct.getProductWidth(), ApiError.ERROR_WIDTH_BOX_LITTER_THAN_PRODUCT);
            compareDimensions(qcProduct.getBoxHeight(), qcProduct.getProductHeight(), ApiError.ERROR_HEIGHT_BOX_LITTER_THAN_PRODUCT);
        }
        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill, dto.getSourceType(), dto.getSourceId());
        String skuId = dto.getQcProduct().getSkuId();
        String purchaseOrderDetailId = dto.getQcInfo().getPurchaseOrderDetailId();
        //当采购订单明细id 不为空的时候
        if (StringUtils.isNotBlank(purchaseOrderDetailId)) {
            List<String> podIds = Arrays.asList(purchaseOrderDetailId);
            //获取到对应的 订单明细
            List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
            skuId = purOrderDetailList.stream().filter(p -> p.getId().equals(purchaseOrderDetailId))
                    .map(PurchaseOrderDetailEntity::getSkuId).findFirst().orElse("");
        }
        //采购订单
        String purchaseOrderId = dto.getPurchaseOrderId();
        if (StringUtils.isNotBlank(purchaseOrderId)) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }

        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(billId, dto.getQcProduct(), skuId);
            //质检信息 暂存
            qcResultService.add(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.add(billId, dto.getReportDetailList());
            //质检备注暂存
            qcRemarkService.add(billId, dto.getRemarkList());
            operateLogService.addModuleOperateLog("新增了一个暂存质检单", ModuleTypeEnum.QC_ORDER.getCode(), billId, "暂存操作");
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
    @Transactional(rollbackFor = Exception.class)
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
        String skuId = dto.getQcProduct().getSkuId();
        List<PurchaseOrderDetailEntity> purOrderDetailList = Collections.emptyList();

        //当采购订单明细id 为空的时候 sku id 不能为空
        if (StringUtils.isBlank(purchaseOrderDetailId)) {
            if (StringUtils.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
        } else {
            List<String> podIds = Arrays.asList(purchaseOrderDetailId);
            //获取到对应的 订单明细
            purOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
            skuId = purOrderDetailList.stream().filter(p -> p.getId().equals(purchaseOrderDetailId))
                    .map(PurchaseOrderDetailEntity::getSkuId).findFirst().orElse("");
        }
        if (StringUtils.isBlank(skuId)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }

        //检查质检数量
        checkQcQty(qcInfo, dto.getId(), dto.getPurchaseOrderId(), skuId);

        String code = bill.getCode();
        BeanMapper.copy(dto, bill);
        //处理相关数据
        HandleData(dto.getQcUserId(), dto.getQcDeptId(), bill, dto.getSourceType(), dto.getSourceId());
        bill.setId(id);
        bill.setQcFinishTime(LocalDateTime.now());
        QcBillStatusEnum exemption = QcBillStatusEnum.getByCode(QcBillStatusEnum.EXEMPTION.getCode());
        bill.setQcStatus(exemption);
        String warehouseId = dto.getWarehouseId();
        if (isExist) {
            PurchaseOrderDTO.GetOneDTO purchaseOrder = scmTaskFeign.getByOrderId(purchaseOrderId);
            if (purchaseOrder != null) {
                //采购订单验证
                String skuNos = purOrderDetailList.stream().filter(obj -> !StrUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                                && !StrUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus()))
                        .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                if (StrUtil.isNotBlank(skuNos)) {
                    throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,purchaseOrder.getCode(),skuNos);
                }
                PurchaseOrderSupplierDTO.UpdateDTO supplierInfo = purchaseOrder.getPurchaseOrderSupplierDTO();
                if (supplierInfo != null) {
                    bill.setSupplierId(supplierInfo.getSupplierId());
                }
                bill.setPurchaseOrderCode(purchaseOrder.getCode());
            }
        }
        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.add(id, dto.getQcProduct(), skuId);
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

            //异步发送通知
            qcResultService.sendQcResultMsg(Arrays.asList(id));
            //操作日志
            operateLogService.addModuleOperateLog(String.format("完成一个免检质检单【%s】", code), ModuleTypeEnum.QC_ORDER.getCode(), id, "新增操作");
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
//                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QC);
                item.setCode(code);
            }
        }
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("质检单【%s】完成操作", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "完成操作");
        Boolean result = this.updateBatchById(qcList);
        if (result) {
            //自动完成入库单
            this.autoBatchStockInBill(ids);

            //异步发送通知
            qcResultService.sendQcResultMsg(ids);

            //新品首批回填SKU的尺寸信息
            updateProductPack(ids);
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
//        long count = qcList.stream().filter(s -> !s.getQcStatus().getCode().equals(qcStatus)).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_99020);
//        }
        //批量检查
        batchCheckQcQty(qcList, true);
        LocalDateTime now = LocalDateTime.now();
        //质检状态
        QcBillStatusEnum exemption = QcBillStatusEnum.getByCode(QcBillStatusEnum.EXEMPTION.getCode());
        for (QcInfoEntity item : qcList) {
            item.setQcFinishTime(now);
            item.setQcStatus(exemption);
            if (StringUtils.isBlank(item.getCode())) {
//                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QC, BusinessNoTypeEnum.CODE_QC.getCode()));
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QC);
                item.setCode(code);
            }
        }
        Boolean result = this.updateBatchById(qcList);
        if (result) {
            //批量去更新 质检数量
            qcResultService.updateQcQty(ids);

            //自动完成入库单
            this.autoBatchStockInBill(ids);

            //异步发送通知
            qcResultService.sendQcResultMsg(ids);
        }
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("质检单【%s】免检操作", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "免检操作");
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
        operateLogService.batchAddModuleOperateLog("质检单【%s】取消操作", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "取消操作");
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
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的质检单", UserContext.getDefaultLoginUser().getUserName(), qcList.stream().map(QcInfoEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.QC_ORDER.getCode(), pairList, "删除操作");
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
        List<PoInstockEntity> stockInList = poInstockService.getStockInBySourceIds(ids);
        long stockInCount = stockInList.stream().filter(s -> !s.getInvalidStatus()).count();
        if (stockInCount > 0) {
            throw new ServiceException(ApiError.ERROR_99027);
        }

        //退货单
        List<PoReturnEntity> returnList = poReturnService.listBySourceIds(ids);
        long returnCount = returnList.stream().filter(s -> !s.getInvalidStatus()).count();
        if (returnCount > 0) {
            throw new ServiceException(ApiError.ERROR_99028);
        }

        //TODO 工作流要处理
        QcBillStatusEnum waitQc = QcBillStatusEnum.getByCode(QcBillStatusEnum.WAIT_QC.getCode());
        qcList.forEach(q -> q.setQcStatus(waitQc));
        //操作日志
        List<Pair<String, String>> pairList = qcList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("质检单【%s】取消流程", ModuleTypeEnum.QC_ORDER.getCode(), pairList, "取消流程操作");
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
            throw new ServiceException(ApiError.ERROR_98060);
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
        operateLogService.batchAddModuleOperateLog("质检单【%s】分配质检员" + userInfo.getUserName(), ModuleTypeEnum.QC_ORDER.getCode(), pairList, "分配操作");
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
        operateLogService.batchAddModuleOperateLog("质检单【%s】更新处理措施" + handleModeName, ModuleTypeEnum.QC_ORDER.getCode(), pairList, "更新处理措施操作");
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
    public List<QcInfoDTO.TabListDTO> tabList(PermissionsDTO dto) {
        QcBillStatusEnum[] values = QcBillStatusEnum.values();
        List<QcInfoDTO.TabListDTO> list = new ArrayList<>();
        for (QcBillStatusEnum item : values) {
            if (QcBillStatusEnum.DRAFT.equals(item) || QcBillStatusEnum.EXEMPTION.equals(item)) {
                continue;
            }
            PurchaseOrderDTO.SearchParamDTO searchParamDTO = new PurchaseOrderDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            QcInfoDTO.TabListDTO resultDTO = new QcInfoDTO.TabListDTO();
            String tabSql = qcInfoQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
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
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
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
        //采购收货
        List<WarehouseReceiveDetailEntity> receiveDetails = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIds);
        //质检退货
        List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        for (PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO dto : list) {
            //来源类型
            dto.setSourceType(SourceTypeEnum.QC_INFO.getCode());
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
            dto.setProductName(productName);
            dto.setSourceDetailId(dto.getPurchaseOrderDetailId());
            PurchaseOrderDTO.PurchaseOrderInfoDTO purchaseOrder = purchaseOrderList.stream().filter(P -> P.getPurchaseOrderId().equals(dto.getPurchaseOrderId())).findFirst().orElse(null);
            if (purchaseOrder != null) {
                dto.setSupplierName(purchaseOrder.getSupplierName());
                dto.setDeliveryWarehouseName(purchaseOrder.getDeliveryWarehouseName());
                dto.setDeliveryWarehouseId(purchaseOrder.getDeliveryWarehouseId());
            }
            //入库数量
            Integer stockInQty = stockInSkuList.stream().filter(s -> s.getSkuId().equals(dto.getSkuId()) &&
                    dto.getPurchaseOrderDetailId().equals(s.getPurchaseOrderDetailId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getStockInQty())).orElse(0);
            dto.setStockInQty(stockInQty);
            //质检单关联的收货单存在时 汇总单条收货单，不存在时，汇总全部采购收货单
            List<WarehouseReceiveDetailEntity> receiveDetailList = receiveDetails.stream()
                    .filter(req -> req.getMainId().equals(dto.getReceiveId())
                            && SourceTypeEnum.PO_RECEIVE.getCode().equals(dto.getReceiveType())
                            && dto.getSkuId().equals(req.getSkuId())
                            && req.getId().equals(dto.getSourceDetailId())).collect(Collectors.toList());
            Integer receiveQty;
            if (CollectionUtils.isEmpty(receiveDetailList)){
                //收货单已收数量
                receiveQty = receiveDetails.stream()
                        .filter(req -> req.getPurchaseOrderDetailId().equals(dto.getPurchaseOrderDetailId()))
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }else {
                //质检单关联的收货单
                receiveQty = receiveDetailList.stream()
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //退货数量
            Integer returnQty = returnOrderDetailList.stream().filter(e -> Objects.equals(e.getPurchaseOrderDetailId(), dto.getPurchaseOrderDetailId())
                            && dto.getSourceId().equals(e.getSourceId())
                            && ReturnOrderSourceEnum.QC.getCode().equals(e.getSourceType())
                            && !ApproveStatusEnum.REJECT.getStatus().equals(e.getApproveStatus()))
                    .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //收货数量 = 收货单已收数量 - 已退数量
            dto.setReceiveQty(receiveQty - returnQty);

            //币种符号
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(dto.getPurchaseOrderDetailId())).findFirst().orElse(null);
            dto.setCurrency(detailEntity.getCurrency());
            dto.setCurrencySymbol(detailEntity.getCurrencySymbol());
            //单价
            BigDecimal taxPrice = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(dto.getPurchaseOrderDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getTaxPrice())).orElse(BigDecimal.ZERO);
            dto.setTaxPrice(taxPrice);

            //不良数
            Integer badQty = qcResultList.stream().filter(q -> q.getMainId().equals(dto.getSourceId())).mapToInt(QcResultEntity::getQcBadQty).sum();

            dto.setReturnQty(badQty);
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
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-24 9:35
     * 一个校验失败，则都下推失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> generatePurchaseReturnOrder(List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> list) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        List<String> ids = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceId).collect(Collectors.toList());
        List<QcInfoEntity> qcInfoList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(qcInfoList)) {
            resultDTOS.add(BatchResultDTO.fail(String.join(",",ids), "", ApiError.ERROR_99015.msg));
//            throw new ServiceException(ApiError.ERROR_99015);
            return resultDTOS;
        }
        String finishQcCode = QcBillStatusEnum.FINISH_QC.getCode();

//        long count = qcInfoList.stream().filter(f -> !f.getQcStatus().getCode().equals(finishQcCode)).count();
        List<QcInfoEntity> qcStatus = qcInfoList.stream().filter(f -> !f.getQcStatus().getCode().equals(finishQcCode)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(qcStatus)) {
//            throw new ServiceException(ApiError.ERROR_99029);
            qcStatus.forEach(qcInfoEntity -> {
                resultDTOS.add(BatchResultDTO.fail(qcInfoEntity.getId(), qcInfoEntity.getCode(), ApiError.ERROR_99029.msg));
            });
            return resultDTOS;
        }
        List<QcResultEntity> qcResultList = qcResultService.getByMainIdList(ids);
//        long handleCount = qcResultList.stream().filter(r -> !r.getHandleModeDict().equals(WmsConstant.QC_RESULT_HANDLE_MODE)).count();
        List<QcResultEntity> qchHandles = qcResultList.stream().filter(r -> !r.getHandleModeDict().equals(WmsConstant.QC_RESULT_HANDLE_MODE)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(qchHandles)) {
            qchHandles.forEach(qcResultEntity -> {
                String code = "";
                QcInfoEntity qcInfoEntity = qcInfoList.stream().filter(e -> e.getId().equals(qcResultEntity.getMainId())).findFirst().orElse(null);
                if (Objects.nonNull(qcInfoEntity)){
                    code = qcInfoEntity.getCode();
                }
                resultDTOS.add(BatchResultDTO.fail(qcResultEntity.getMainId(), code, ApiError.ERROR_99029.msg));
            });
            return resultDTOS;
//            throw new ServiceException(ApiError.ERROR_99029);
        }


        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //采购订单id集合
        List<String> poIds = list.stream().map(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getPurchaseOrderId).collect(Collectors.toList());

        List<PurchaseOrderEntity> purchaseOrderDbList = scmTaskFeign.listPurchaseOrderByIds(poIds);

        //收货数量
        List<WarehouseReceiveEntity> receiveList = warehouseReceiveService.listByPurchaseOrderIds(poIds);
        //收货单ids
        List<String> receiveIds = receiveList.stream().map(WarehouseReceiveEntity::getId).collect(Collectors.toList());
        //收货sku 明细信息
        List<WarehouseReceiveDetailEntity> receiveSkuList = warehouseReceiveDetailService.listDetailByMainIds(receiveIds);
        List<PurchaseReturnOrderDTO.AddDTO> addList = new ArrayList<>();

        String qcBill = SourceTypeEnum.QC_INFO.getCode();
        Map<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> map = list.stream().collect(Collectors.groupingBy(PoInstockDTO.GeneratePurchaseReturnOrderDTO::getSourceId));
        //质检单主表循环
        for (Map.Entry<String, List<PoInstockDTO.GeneratePurchaseReturnOrderDTO>> entry : map.entrySet()) {
            String sourceId = entry.getKey();
            List<PoInstockDTO.GeneratePurchaseReturnOrderDTO> value = entry.getValue();

            PurchaseReturnOrderDTO.AddDTO addDTO = new PurchaseReturnOrderDTO.AddDTO();
            //质检单
            QcInfoEntity qcInfoEntity = qcInfoList.stream().filter(obj -> obj.getId().equals(sourceId)).findFirst().orElse(null);
            if (Objects.isNull(qcInfoEntity)) {
                resultDTOS.add(BatchResultDTO.fail(sourceId, "", ApiError.ERROR_99015.msg));
//                throw new ServiceException(ApiError.ERROR_99015);
                continue;
            }

            addDTO.setBillDate(LocalDate.now());
            addDTO.setSourceType(qcBill);
            addDTO.setSourceId(sourceId);
            //退货详情
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            PoInstockDTO.GeneratePurchaseReturnOrderDTO purchaseReturnOrderDTO = value.get(0);
            //采购订单id
            String purchaseOrderId = purchaseReturnOrderDTO.getPurchaseOrderId();
            //质检单明细表循环
            for (PoInstockDTO.GeneratePurchaseReturnOrderDTO detail : value) {
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
                //收货单的ids
                List<String> receiveIdList = receiveList.stream().filter(r -> r.getPurchaseOrderId().equals(detail.getPurchaseOrderId())).map(WarehouseReceiveEntity::getId).collect(Collectors.toList());
                //验证 sku 收货总数量
                Integer receiveQty = receiveSkuList.stream().filter(
                        obj -> obj.getSkuId().equals(detail.getSkuId()) &&
                                receiveIdList.contains(obj.getMainId())
                ).mapToInt(e -> e.getReceiveQty()).sum();
                if (receiveQty == 0) {
                    resultDTOS.add(BatchResultDTO.fail(qcInfoEntity.getId(), qcInfoEntity.getCode(), String.format("SKU【%s】未找到对应收货数量", detail.getSkuNo())));
                    continue;
//                    throw new ServiceException(1, String.format("SKU【%s】未找到对应收货数量", detail.getSkuNo()));
                }
                if (MathUtil.compareTo(detail.getRealityReturnQty(), receiveQty) > 0) {
                    resultDTOS.add(BatchResultDTO.fail(qcInfoEntity.getId(), qcInfoEntity.getCode(), String.format("SKU【%s】实退总数量不能大于【%s】", detail.getSkuNo(), receiveQty)));
                    continue;
//                    throw new ServiceException(1, String.format("SKU【%s】实退数量不能大于【%s】", detail.getSkuNo(), receiveQty));
                }
                //退货单：质检单下推质检退货单【入库数量改为收货数量】【校验退货数量不能大于已收货数量】【下推新增校验】
                if (MathUtil.compareTo(detail.getRealityReturnQty(), detail.getReceiveQty()) > 0) {
                    resultDTOS.add(BatchResultDTO.fail(qcInfoEntity.getId(), qcInfoEntity.getCode(), String.format("SKU【%s】实退数量不能大于【%s】", detail.getSkuNo(), detail.getReceiveQty())));
                    continue;
//                    throw new ServiceException(1, String.format("SKU【%s】实退数量不能大于【%s】", detail.getSkuNo(), receiveQty));
                }
                BeanMapperUtils.copy(detail, addDetailDTO);
                addDetailDTO.setPurchaseOrderDetailId(detail.getPurchaseOrderDetailId());
                addDetailDTO.setReturnQty(detail.getRealityReturnQty());
//                addDetailDTO.setWarehouseLocation(detail.getWarehouseLocation());
                addDetailDTO.setReturnPrice(detail.getTaxPrice());
                addDetailList.add(addDetailDTO);
            }
            addDTO.setReturnMode(purchaseReturnOrderDTO.getReturnMode());
            addDTO.setPurchasePriceDetailList(addDetailList);
            addDTO.setReturnUserId(purchaseReturnOrderDTO.getReturnUserId());

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
                addDTO.setPurchaseOrgId(purchaseOrderInfo.getPurchaseOrgId());
            }
            addDTO.setPurchaseUserId(purchaseUserId);
            addList.add(addDTO);
        }
        Boolean success = resultDTOS.stream().allMatch(BatchResultDTO::getSuccess);
        if (CollectionUtils.isNotEmpty(addList) && success) {
            addList.forEach(obj -> poReturnService.add(obj));
        }
        return resultDTOS;
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
        String qcUserId = "";
        String qcUserName = "";
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
            qcInfo.setSourceCode(item.getSourceCode());
            qcInfo.setSourceDetailId(item.getSourceDetailId());
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
     * 销售退货签收单自动下推质检单
     * @Author Luo_WG
     * @Date 2023/8/3 10:07
     * @param dto
     * @return java.lang.Boolean
     **/
    public Boolean autoSoReturnReceiveToQcDTO(List<QcInfoDTO.SoReturnReceiveToQcDTO>  dto) {
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
        List<String> qcTypeList = dto.stream().map(QcInfoDTO.SoReturnReceiveToQcDTO::getQcType).collect(Collectors.toList());
        //质检类型的集合
        List<QcReportDTO.ListDTO> list = qcReportService.listByQcType(qcTypeList);
        Map<String, List<QcReportDTO.ListDTO>> qcTypeMap = list.stream().collect(Collectors.groupingBy(QcReportDTO.ListDTO::getQcType));
        String qcUserId = "";
        String qcUserName = "";
        String departId = "";
        String departName = "";
        //质检员
        if (StringUtils.isNotBlank(qcUserId)) {
            SysDepartmentUserNumberDTO userDTO = sysUserFeign.getDeptByUserId(qcUserId);
            departId = userDTO.getDepartmentId();
            departName = userDTO.getDepartmentName();
        }


        for (QcInfoDTO.SoReturnReceiveToQcDTO item : dto) {
            QcInfoEntity qcInfo = new QcInfoEntity();
            String id = IdWorker.getIdStr();
            qcInfo.setId(id);
            qcInfo.setPurchaseOrderCode("");
            qcInfo.setPurchaseOrderId("");
            qcInfo.setSupplierId(item.getSupplierId());
            qcInfo.setWarehouseId(item.getWarehouseId());
            qcInfo.setQcDeptId(departId);
            qcInfo.setQcDeptName(departName);
            qcInfo.setQcUserId(qcUserId);
            qcInfo.setQcUserName(qcUserName);
            qcInfo.setSourceId(item.getSourceId());
            qcInfo.setSourceType(item.getSourceType());
            qcInfo.setSourceCode(item.getSourceCode());
            qcInfo.setSourceDetailId(item.getSourceDetailId());
            addQcList.add(qcInfo);
            //质检结果
            QcResultEntity qcResult = new QcResultEntity();
            qcResult.setMainId(id);
            String qcType = item.getQcType();
            Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
            qcResult.setQcType(item.getQcType());
            qcResult.setIsInside(isInside);
            qcResult.setTotalQty(item.getTotalQty());
            qcResult.setPurchaseOrderDetailId("");
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
     * 退货签收单下推质检单-保存
     *
     * @param receiveIds receiveIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/5/23 14:05
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnReceiveGenerateQCSave(List<String> receiveIds) {
        List<QcInfoDTO.ReceiveGenerateQcView> receiveGenerateQcViews = soReturnReceiveService.receiveGenerateQcView(receiveIds);

        List<QcInfoEntity> addQcList = new ArrayList<>(receiveIds.size());
        //质检结果
        List<QcResultEntity> addQcResultList = new ArrayList<>(receiveIds.size());

        //质检产品
        List<QcProductEntity> addQcProductList = new ArrayList<>(receiveIds.size());

        //质检报告
        List<QcReportDetailEntity> addQcReportDetailList = new ArrayList<>(receiveIds.size());

        //质检类型的集合
        List<QcReportDTO.ListDTO> list = qcReportService.listByQcType(Arrays.asList(QcTypeEnum.RETURN_QC.getCode()));
        Map<String, List<QcReportDTO.ListDTO>> qcTypeMap = list.stream().collect(Collectors.groupingBy(QcReportDTO.ListDTO::getQcType));
        String qcUserId = UserContext.getDefaultLoginUser().getUid();
        String qcUserName = UserContext.getDefaultLoginUser().getUserName();
        String departId = "";
        String departName = "";
        //质检员
        if (StringUtils.isNotBlank(qcUserId)) {
            SysDepartmentUserNumberDTO userDTO = sysUserFeign.getDeptByUserId(qcUserId);
            departId = userDTO.getDepartmentId();
            departName = userDTO.getDepartmentName();
        }

        for (QcInfoDTO.ReceiveGenerateQcView item : receiveGenerateQcViews) {
            QcInfoEntity qcInfo = new QcInfoEntity();
            String id = IdWorker.getIdStr();
            qcInfo.setId(id);
            qcInfo.setPurchaseOrderCode("");
            qcInfo.setPurchaseOrderId("");
            qcInfo.setSupplierId("");
            qcInfo.setWarehouseId(item.getWarehouseId());
            qcInfo.setQcDeptId(departId);
            qcInfo.setQcDeptName(departName);
            qcInfo.setQcUserId(qcUserId);
            qcInfo.setQcUserName(qcUserName);
            qcInfo.setSourceId(item.getMainId());
            qcInfo.setSourceCode(item.getCode());
            qcInfo.setSourceDetailId(item.getId());
            qcInfo.setSourceType(SourceTypeEnum.SO_RETURN_RECEIVE.getCode());
            addQcList.add(qcInfo);
            //质检结果
            QcResultEntity qcResult = new QcResultEntity();
            qcResult.setMainId(id);
            String qcType = QcTypeEnum.RETURN_QC.getCode();
            Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
            qcResult.setQcType(qcType);
            qcResult.setIsInside(isInside);
            qcResult.setTotalQty(item.getReceiveQty());
            qcResult.setPurchaseOrderDetailId("");
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
     * 下推退货入库单-列表查询
     *
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.GenerateSoReturnInstockView>
     * @Author Luo_WG
     * @Date 2023/5/23 15:52
     **/
    @Override
    public List<SoReturnInstockDTO.GenerateSoReturnInstockView> generateSoReturnInstockView(List<String> ids) {

        List<SoReturnInstockDTO.GenerateSoReturnInstockView> list = baseMapper.generateSoReturnInstockView(ids);
        String finishQcCode = QcBillStatusEnum.FINISH_QC.getCode();
        String exemptionCode = QcBillStatusEnum.EXEMPTION.getCode();

        long count = list.stream().filter(f -> !f.getQcStatus().equals(finishQcCode) && !f.getQcStatus().equals(exemptionCode)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98067);
        }
        long receiveCount = list.stream().filter(req -> !req.getSourceType().equals(SourceTypeEnum.SO_RETURN_RECEIVE.getCode())).count();
        if (receiveCount > 0) {
            throw new ServiceException(ApiError.ERROR_98066);
        }

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnInstockDTO.GenerateSoReturnInstockView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //退货签收单明细表id
        List<String> receiveDetailIds = list.stream().map(SoReturnInstockDTO.GenerateSoReturnInstockView::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(receiveDetailIds);

        List<String> returnDetailIds = soReturnReceiveDetailEntities.stream().map(SoReturnReceiveDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByIds(returnDetailIds);
        //销售单明细id
        List<String> soDetailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);

        //签收单id
        List<String> receiveIds = list.stream().map(SoReturnInstockDTO.GenerateSoReturnInstockView::getSourceId).collect(Collectors.toList());
        List<SoReturnReceiveEntity> soReturnReceiveEntities = soReturnReceiveService.listByIds(receiveIds);
        List<String> returnIds = soReturnReceiveEntities.stream().map(SoReturnReceiveEntity::getSourceId).collect(Collectors.toList());
        List<SoReturnEntity> returnEntityList = soReturnFeign.listByIds(returnIds);
        //销售单id
        List<String> soIds = returnEntityList.stream().map(SoReturnEntity::getSourceId).collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        for (SoReturnInstockDTO.GenerateSoReturnInstockView view : list) {

            //拿到签收单id
            SoReturnReceiveEntity soReturnReceiveEntity = soReturnReceiveEntities.stream().filter(req -> req.getId().equals(view.getSourceId())).findFirst().orElse(new SoReturnReceiveEntity());
            SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(view.getSourceDetailId())).findFirst().orElse(new SoReturnReceiveDetailEntity());
            SoReturnEntity soReturnEntity = returnEntityList.stream().filter(req -> req.getId().equals(soReturnReceiveEntity.getSourceId())).findFirst().orElse(new SoReturnEntity());
            //退货方式
            String returnTypeDict = soReturnReceiveDetailEntity.getReturnTypeDict();
            if (StringUtils.isNotBlank(returnTypeDict)) {
                view.setReturnTypeDictName(ReturnTypeEnum.getName(returnTypeDict));
                view.setReturnTypeDict(returnTypeDict);
            }
            //退货原因
            String returnReason = soReturnReceiveDetailEntity.getReturnReasonDict();
            if (StringUtils.isNotBlank(returnReason)) {
                view.setReturnReasonDictName(ReturnReasonEnum.getName(returnReason));
                view.setReturnReasonDict(returnReason);
            }
            view.setId(view.getId());
            view.setMainId(view.getId());
            view.setSourceId(soReturnReceiveEntity.getSourceId());
            view.setSourceDetailId(view.getSourceDetailId());
            view.setSourceCode(soReturnEntity.getCode());
            if (StringUtils.isBlank(soReturnReceiveEntity.getSourceCode())) {
                view.setCode(soReturnReceiveEntity.getCode());
            } else {
            view.setCode(soReturnReceiveEntity.getSourceCode());
            }
            view.setCustomerId(soReturnReceiveEntity.getCustomerId());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(view.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            view.setCustomerName(customerInfoEntity.getName());
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(req -> req.getId().equals(soReturnReceiveDetailEntity.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(req -> req.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            view.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSoId().equals(soDetailEntity.getMainId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            view.setDeliveryQty(actualQty);
            view.setMustQty(soReturnReceiveDetailEntity.getReturnQty());
            view.setReceiveQty(soReturnReceiveDetailEntity.getReceiveQty());
            view.setRealQty(soReturnReceiveDetailEntity.getReceiveQty());
            if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnTypeDict())) {
                view.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));
                view.setReturnTypeDict(soReturnDetailEntity.getReturnTypeDict());
            }
            if (StringUtils.isNotBlank(soReturnDetailEntity.getReturnReasonDict())) {
                view.setReturnReasonDictName(ReturnReasonEnum.getName(soReturnDetailEntity.getReturnReasonDict()));
                view.setReturnReasonDict(soReturnDetailEntity.getReturnReasonDict());
            }
            view.setWarehouseId(view.getWarehouseId());
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(view.getWarehouseId()));
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                view.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
            }
            view.setBillDate(LocalDate.now());
        }
        return list;
    }

    /**
     * 根据来源id查询质检单
     *
     * @param sourceId sourceId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/5/10 18:12
     **/
    @Override
    public List<QcInfoEntity> listQCBySourceId(String sourceId) {
        return lambdaQuery().eq(QcInfoEntity::getSourceId, sourceId).list();
    }

    /**
     * 根据来源ids查询质检单
     *
     * @param sourceIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/5/10 18:12
     **/
    @Override
    public List<QcInfoEntity> listQCBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(QcInfoEntity::getSourceId, sourceIds).list();
    }

    @Override
    public List<QcInfoEntity> listQCBySourceIdsAndType(List<String> sourceIds, String sourceType) {
        return lambdaQuery().in(QcInfoEntity::getSourceId, sourceIds).eq(QcInfoEntity::getSourceType,sourceType).list();
    }

    @Override
    public List<QcInfoEntity> listQCBySourceDetailIds(List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return new ArrayList<>();
        }
        return lambdaQuery().in(QcInfoEntity::getSourceDetailId, sourceDetailIds).list();
    }

    @Override
    public QcInfoDTO.PurchaseQcInfoDTO getQcInfoByPurchaseOrder(QcInfoDTO.PurchaseQcParamDTO dto) {
        return this.baseMapper.getQcInfoByPurchaseOrder(dto);
    }

    @Override
    public void exportDailyExcel(QcInfoDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("质检日报数据", EXPORT_WMS_DAILY_QC_BILL.getCode(), dto);
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
            //采购单id 集合
            List<String> purOrderIds = qcList.stream().map(QcInfoEntity::getPurchaseOrderId).collect(Collectors.toList());

            //获取到对应的 订单明细
            List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listByPurchaseOrderIds(purOrderIds);
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
                                // 关闭验证
                                // throw new ServiceException(ApiError.ERROR_99019);
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
            if (qcQty > totalQty) {
                throw new ServiceException(ApiError.ERROR_99073);
            }

            if (goodQty + badQty > qcQty) {
                throw new ServiceException(ApiError.ERROR_99016);
            }

            List<PurchaseOrderDetailEntity> purOrderDetailList = scmTaskFeign.listByPurchaseOrderIds(Arrays.asList(purchaseOrderId));

            //当采购订单详情不为空的时候
            if (CollectionUtils.isNotEmpty(purOrderDetailList)) {
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
                    // 关闭验证
                    // throw new ServiceException(ApiError.ERROR_99017);
                }
            }
        }

    }

    private List<QcInfoDTO.QcDailyReportDTO> fillQcDailyRptData(List<QcInfoDTO.DailyListDTO> dataList) {
        List<QcInfoDTO.QcDailyReportDTO> resultList = new ArrayList<>(dataList.size());
        if (CollectionUtils.isNotEmpty(dataList)) {
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            List<String> supplierIdList = dataList.stream().map(QcInfoDTO.DailyListDTO::getSupplierId).collect(Collectors.toList());
            List<String> skuIdList = dataList.stream().map(QcInfoDTO.DailyListDTO::getSkuId).collect(Collectors.toList());
            List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
            List<String> billIdList = dataList.stream().map(QcInfoDTO.DailyListDTO::getId).collect(Collectors.toList());
            List<QcRemarkEntity> billRemarkList = qcRemarkService.getByMainIdList(billIdList);
            List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
            List<String> billIds = dataList.stream().map(QcInfoDTO.DailyListDTO::getId).distinct().collect(Collectors.toList());
            List<PurchaseOrderEntity> poList = scmTaskFeign.listPurchaseOrderByIds(billIds);

            List<String> productIdList = dataList.stream().map(QcInfoDTO.DailyListDTO::getProductId).collect(Collectors.toList());

            // 产品图片、箱唛
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(productIdList);
            Map<String, List<WmsAttachmentDTO.UpdateDTO>> attachmentMap = attachmentList.stream().collect(Collectors.groupingBy(v -> v.getBusinessId() + "_" + v.getType()));

            // 不良附图
            List<String> qcResultIdList = dataList.stream().map(QcInfoDTO.DailyListDTO::getQcResultId).collect(Collectors.toList());
            List<WmsAttachmentDTO.UpdateDTO> badAttachmentList = wmsAttachmentService.getByBusinessIds(qcResultIdList);
            Map<String, List<WmsAttachmentDTO.UpdateDTO>> badAttachmentMap = badAttachmentList.stream().collect(Collectors.groupingBy(v -> v.getBusinessId() + "_" + v.getType()));

            // 报告
            Map<String, List<QcReportDetailDTO.ViewDTO>> reportMap = qcReportDetailService.getByMainIds(billIdList);

            for (QcInfoDTO.DailyListDTO item : dataList) {
                QcInfoDTO.QcDailyReportDTO qcDailyReportDTO = new QcInfoDTO.QcDailyReportDTO();

                qcDailyReportDTO.setQcDate(item.getQcDate());
                // 是否内检
                Boolean isInside = item.getIsInside();
                qcDailyReportDTO.setQcInsideType(Objects.equals(isInside, Boolean.TRUE) ? QcInsideTypeEnum.INSIDE_QC.getCode() : QcInsideTypeEnum.OUTSIDE_QC.getCode());
                qcDailyReportDTO.setQcInsideTypeName(Objects.equals(isInside, Boolean.TRUE) ? QcInsideTypeEnum.INSIDE_QC.getName() : QcInsideTypeEnum.OUTSIDE_QC.getName());

                String skuNo = skuVOList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
                qcDailyReportDTO.setSkuNo(skuNo);

                // 是否新品
                Boolean isFirstMassProduct = poList.stream().filter(p -> p.getId().equals(item.getPurchaseOrderId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getIsFirstMassProduct())).orElse(Boolean.FALSE);
                qcDailyReportDTO.setIsFirstMassProduct(isFirstMassProduct);
                qcDailyReportDTO.setFirstMassProductName(Objects.equals(qcDailyReportDTO.getIsFirstMassProduct(), Boolean.TRUE) ? ProductTypeEnum.NEW_PRODUCTS.getName() : ProductTypeEnum.OLD_PRODUCTS.getName());

                String supplierId = item.getSupplierId();
                String supplierName = supplierList.stream().filter(s -> s.getId().equals(supplierId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                qcDailyReportDTO.setSupplierName(supplierName);

                String skuId = item.getSkuId();
                String skuName = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
                qcDailyReportDTO.setProductName(skuName);

                // 产品图片
                String productTypeKey = item.getProductId() + "_" + WmsConstant.QC_PRODUCT;
                if (attachmentMap.containsKey(productTypeKey)) {
                    List<WmsAttachmentDTO.UpdateDTO> attachList = attachmentMap.get(productTypeKey);
                    List<String> attachmentUrls = attachList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
                    qcDailyReportDTO.setProductImgUrl(attachmentUrls);
                }

                // 箱唛图片
                String productBoxKey = item.getProductId() + "_" + WmsConstant.QC_BOX;
                if (attachmentMap.containsKey(productBoxKey)) {
                    List<WmsAttachmentDTO.UpdateDTO> attachList = attachmentMap.get(productBoxKey);
                    List<String> attachmentUrls = attachList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
                    qcDailyReportDTO.setBoxMarkImgUrl(attachmentUrls);
                }

                // 不良附件
                String qcBadKey = item.getQcResultId() + "_" + WmsConstant.BAD;
                if (badAttachmentMap.containsKey(qcBadKey)) {
                    List<WmsAttachmentDTO.UpdateDTO> attachList = badAttachmentMap.get(qcBadKey);
                    qcDailyReportDTO.setBadAttachments(attachList);
                }

                // 报告
                List<QcReportDetailDTO.ViewDTO> reportList = reportMap.get(item.getId());

                qcDailyReportDTO.setReportList(reportList);

                qcDailyReportDTO.setTotalQty(item.getTotalQty());

                QcResultEnum qcResultEnum = item.getQcResult();
                qcDailyReportDTO.setQcResultName(qcResultEnum != null ? qcResultEnum.getName() : "");
                qcDailyReportDTO.setQcUserName(item.getQcUserName());
                qcDailyReportDTO.setQcQty(item.getQcQty());
                qcDailyReportDTO.setQcBadQty(item.getQcBadQty());

                BigDecimal qcBadRate = item.getQcBadRate();
                qcDailyReportDTO.setQcBadRate(qcBadRate != null ? qcBadRate.toString() + "%" : "");

                String qcProblemDict = item.getQcProblemDict();
                qcDailyReportDTO.setQcProblemDict(qcProblemDict);
                String qcProblemName = dictList.stream().filter(d -> d.getValue().equals(qcProblemDict)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                qcDailyReportDTO.setQcProblemName(qcProblemName);

                String handleModeDict = item.getHandleModeDict();
                String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                qcDailyReportDTO.setHandleModeName(handleModeName);

                qcDailyReportDTO.setHandleResultName(qcDailyReportDTO.getQcResultName());
                qcDailyReportDTO.setBadDescription(item.getBadDescription());

                qcDailyReportDTO.setProductLength(item.getProductLength());
                qcDailyReportDTO.setProductWidth(item.getProductWidth());
                qcDailyReportDTO.setProductHeight(item.getProductHeight());
                qcDailyReportDTO.setBoxLength(item.getBoxLength());
                qcDailyReportDTO.setBoxWidth(item.getBoxWidth());
                qcDailyReportDTO.setBoxHeight(item.getBoxHeight());
                qcDailyReportDTO.setProductNetWeight(item.getProductNetWeight());
                qcDailyReportDTO.setBoxWeight(item.getBoxWeight());
                qcDailyReportDTO.setFullBoxQty(item.getBoxQty());

                String remark = billRemarkList.stream().filter(r -> r.getMainId().equals(item.getId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getRemark())).orElse("");
                qcDailyReportDTO.setRemark(remark);

                resultList.add(qcDailyReportDTO);
            }
        }
        return resultList;
    }

    /**
     * 生成供应商报表excel
     *
     * @param resultList
     * @param response
     */
    private void generateDailyRptExcel(List<QcInfoDTO.QcDailyReportDTO> resultList, HttpServletResponse response) {
        OutputStream outputStream = null;
        // 声明一个工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFCellStyle contentCellStyle = wb.createCellStyle();
        // 水平居左
        contentCellStyle.setAlignment(HorizontalAlignment.LEFT);
        //垂直居中
        contentCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //自动换行
        contentCellStyle.setWrapText(true);
        //下边框
        contentCellStyle.setBorderBottom(BorderStyle.THIN);
        //左边框
        contentCellStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        contentCellStyle.setBorderTop(BorderStyle.THIN);
        //右边框
        contentCellStyle.setBorderRight(BorderStyle.THIN);

        // 超链接样式
        XSSFCellStyle hyperContentCellStyle = wb.createCellStyle();
        // 水平居左
        hyperContentCellStyle.setAlignment(HorizontalAlignment.LEFT);
        //垂直居中
        hyperContentCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //自动换行
        hyperContentCellStyle.setWrapText(true);
        //下边框
        hyperContentCellStyle.setBorderBottom(BorderStyle.THIN);
        //左边框
        hyperContentCellStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        hyperContentCellStyle.setBorderTop(BorderStyle.THIN);
        //右边框
        hyperContentCellStyle.setBorderRight(BorderStyle.THIN);

        Font hyperFont = wb.createFont();
        hyperFont.setFontHeightInPoints((short) 12);
        hyperFont.setColor(IndexedColors.BLUE.getIndex());
        hyperFont.setUnderline(Font.U_SINGLE);
        hyperContentCellStyle.setFont(hyperFont);

        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 13);
        CellStyle titleStyle = wb.createCellStyle();
        // 设置水平居中
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        // 设置垂直对齐的样式为居中对齐;
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        // 下边框
        titleStyle.setBorderBottom(BorderStyle.THIN);
        // 左边框
        titleStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        titleStyle.setBorderTop(BorderStyle.THIN);
        // 右边框
        titleStyle.setBorderRight(BorderStyle.THIN);

        CellStyle titleNoBorderStyle = wb.createCellStyle();
        //设置水平居中
        titleNoBorderStyle.setAlignment(HorizontalAlignment.LEFT);
        //设置垂直对齐的样式为居中对齐;
        titleNoBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleNoBorderStyle.setFont(titleFont);

        // 创建sheet页
        XSSFSheet sheet = wb.createSheet("质检日报");
        sheet.setDefaultColumnWidth(1 * 256);
        sheet.setColumnWidth(0, 16 * 256);
        sheet.setColumnWidth(2, 20 * 256);
        sheet.setColumnWidth(6, 30 * 256);
        sheet.setColumnWidth(7, 30 * 256);
        sheet.setColumnWidth(19, 20 * 256);
        sheet.setColumnWidth(21, 20 * 256);

        int rowNo = 0;
        // 标题
        QcUtils.createQcDailyRptTitle(rowNo, sheet, titleStyle);

        // 内容行
        QcUtils.createQcDailyRptContent(rowNo, resultList, filePublicUrl, sheet, wb, contentCellStyle, hyperContentCellStyle);

        String fileName = StrUtil.format("质检日报数据{}.xlsx", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            outputStream = response.getOutputStream();
            wb.write(outputStream);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

    }

    @Override
    public List<SysUserInfoEntity> listQcUser() {
        List<SysUserInfoEntity> sysUserInfoEntities = sysUserFeign.listUserByDept("品质中心");
        return sysUserInfoEntities;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reQcSample(QcInfoDTO.ReQcDTO dto) {
        dto.setIds(dto.getIds().stream().distinct().collect(Collectors.toList()));
        List<QcResultEntity> qcResultlist = qcResultService.getByMainIdList(dto.getIds());
        Map<String,QcResultEntity> qcResultMap = qcResultlist.stream().collect(Collectors.toMap(QcResultEntity::getMainId, Function.identity()));
        dto.getIds().stream().forEach(id->{
            QcInfoEntity qcInfoEntity = super.getById(id);
            Optional.ofNullable(qcInfoEntity).orElseThrow(()->new ServiceException("质检单信息不存在"));

            // 只有已质检才允许操作
            QcBillStatusEnum qcBillStatusEnum = qcInfoEntity.getQcStatus();
            if(!Objects.equals(qcBillStatusEnum, QcBillStatusEnum.FINISH_QC)) {
                throw new ServiceException("只有已质检才允许操作");
            }

            // 是否库内抽检为是才可以操作
            QcResultEntity qcResultEntity = qcResultMap.get(id);
            if(Objects.isNull(qcResultEntity) || !Objects.equals(qcResultEntity.getIsInsideQc(), Boolean.TRUE)) {
                throw new ServiceException("只有库内抽检为是才允许操作");
            }
            // 添加备注
            if(StrUtils.isNotEmpty(dto.getRemark())) {
                QcRemarkEntity qcRemarkEntity = new QcRemarkEntity();
                qcRemarkEntity.setMainId(id);
                qcRemarkEntity.setRemark(dto.getRemark());
                qcRemarkService.save(qcRemarkEntity);
            }
        });
        // 更新质检复检抽检结果
        qcResultService.updateQcSampleResult(dto.getIds(), dto.getQcSampleResult());
    }

    @Override
    public void repairQcInfoSourceCode() {
        List<QcInfoEntity> list = this.list();
        for (QcInfoEntity qcInfoEntity : list) {
            if (SourceTypeEnum.PO_RECEIVE.getCode().equals(qcInfoEntity.getSourceType())) {
                WarehouseReceiveEntity info = warehouseReceiveService.getById(qcInfoEntity.getSourceId());
                lambdaUpdate().set(QcInfoEntity::getSourceCode, info.getCode()).eq(QcInfoEntity::getId, qcInfoEntity.getId()).update();
            } else if (SourceTypeEnum.PURCHASE_ORDER.getCode().equals(qcInfoEntity.getSourceType())) {
                PurchaseOrderEntity info = scmTaskFeign.getPurchaseOrderById(qcInfoEntity.getSourceId());
                lambdaUpdate().set(QcInfoEntity::getSourceCode, info.getCode()).eq(QcInfoEntity::getId, qcInfoEntity.getId()).update();
            } else if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(qcInfoEntity.getSourceType())) {
                SoReturnReceiveEntity info = soReturnReceiveService.getById(qcInfoEntity.getSourceId());
                lambdaUpdate().set(QcInfoEntity::getSourceCode, info.getCode()).eq(QcInfoEntity::getId, qcInfoEntity.getId()).update();
            }
        }

    }

    @Override
    public List<QcInfoDTO.QcReceiveResultDTO> getQcReceiveResult(List<String> purchaseDetailIds) {
        if(CollectionUtils.isEmpty(purchaseDetailIds)){
            return new ArrayList<>();
        }
        return baseMapper.getQcReceiveResult(purchaseDetailIds);
    }

    @Override
    public Integer countTotalNotQc(QcEffectivenessDTO.CountQcParamDTO qcParamDTO) {
        return baseMapper.countTotalNotQc(qcParamDTO);
    }

    @Override
    public PagingVO<QcInfoDTO.QcDailyReportDTO> exportDailyQcBill(PagingDTO<QcInfoDTO.ExportDTO> dto) {

        // 查询数据
        Page<QcInfoDTO.DailyListDTO> page = baseMapper.getDailyExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        // 填充数据
        List<QcInfoDTO.QcDailyReportDTO> resultList = fillQcDailyRptData(page.getRecords());
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public PagingVO<QcBillExportExcelDTO> exportQcBill(PagingDTO<QcInfoDTO.ExportDTO> dto) {

        Page<QcInfoDTO.PagingViewDTO> page = baseMapper.getExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        List<QcBillExportExcelDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            List<String> supplierIdList = page.getRecords().stream().map(QcInfoDTO.PagingViewDTO::getSupplierId).collect(Collectors.toList());
            List<String> skuIdList = page.getRecords().stream().map(QcInfoDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);
            List<String> warehouseIdList = page.getRecords().stream().map(QcInfoDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
            List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
            List<String> billIdList = page.getRecords().stream().map(QcInfoDTO.PagingViewDTO::getId).collect(Collectors.toList());
            List<QcRemarkEntity> billRemarkList = qcRemarkService.getByMainIdList(billIdList);
            List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
            for (QcInfoDTO.PagingViewDTO item : page.getRecords()) {
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
                excelDTO.setIsInsideQcName(Objects.equals(item.getIsInsideQc(), Boolean.TRUE) ? "是" : "否");
                String qcSampleResult = QcReCheckResultEnum.getByCode(item.getQcSampleResult());
                excelDTO.setQcSampleResultName(StrUtils.isNotEmpty(qcSampleResult) ? qcSampleResult : "-");
                resultList.add(excelDTO);
            }

        }
        return new PagingVO<>(resultList, (int)page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 比较尺寸
     *
     * @author hyj
     * @date 2024/5/10 9:05
     * @param larger   大尺寸
     * @param smaller  小尺寸
     * @param apiError 报错信息
     */
    private void compareDimensions(BigDecimal larger, BigDecimal smaller, ApiError apiError) {
        if (Objects.nonNull(larger) && larger.compareTo(BigDecimal.ZERO) > 0
                && Objects.nonNull(smaller) && smaller.compareTo(BigDecimal.ZERO) > 0) {
            if (larger.compareTo(smaller) < 0) {
                throw new ServiceException(apiError);
            }
        }
    }
}
