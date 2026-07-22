package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.MessageUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.InventorySyncModeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.common.business.enums.SubcontractTypeEnum;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.SoReturnStockImportExcelDTO;
import com.erp.model.wms.dto.excel.SoReturnStockUpdateImportExcelDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsBillCostFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.convert.SoB2cReturnInstockConverter;
import com.erp.server.wms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.wms.kingdee.SyncSoReturnInstockService;
import com.erp.server.wms.listener.SoReturnStockExcelListener;
import com.erp.server.wms.listener.SoReturnStockUpdateExcelListener;
import com.erp.server.wms.mapper.SoReturnInstockMapper;
import com.erp.server.wms.service.*;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.erp.wms.aliexpress.model.returnorder.AliexpressReturnInstockDTO;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_RETURN_IN_STOCK;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_WMS_SO_RETURN_IN_STOCK;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_WMS_SO_RETURN_IN_STOCK_OVERWRITE;

/**
 * 退货入库单
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoReturnInstockServiceImpl extends SuperServiceImpl<SoReturnInstockMapper, SoReturnInstockEntity> implements SoReturnInstockService {

    private static final int SO_RETURN_INSTOCK_IMPORT_MAX_ROWS = 5000;

    /** 批量更新主表导入行数上限（单事务落库，与新增导入分开限制） */
    private static final int SO_RETURN_INSTOCK_IMPORT_UPDATE_MAX_ROWS = 500;

    private static final int IMPORT_UPDATE_BATCH_SIZE = 500;

    @Lazy
    @Resource
    private SoReturnInstockService selfService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

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
    private PlatformTransactionManager transactionManager;

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
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FileFeign fileFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private AbstractWdtService abstractWdtService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private LogisticsBillCostFeign logisticsBillCostFeign;

    @Resource
    private SyncSoReturnInstockService syncSoReturnInstockService;

    @Resource
    private SoReturnNoticeService soReturnNoticeService;
    @Resource
    private AuthDataFeign authDataFeign;
    @Resource
    private SoB2cReturnFeign soB2cReturnFeign;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Override
    public PagingVO<SoReturnInstockDTO.PagingView> paging(PagingDTO<SoReturnInstockDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(getPermissionSql(pagingParamDTO.getPermissionSql()));
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        query.setSearchCount(Boolean.FALSE);
        Integer totalCount = this.baseMapper.pagingCount(pagingParamDTO.getParams());
        IPage<SoReturnInstockDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        pageData.setTotal(ObjectUtils.isEmpty(totalCount) ? MathUtil.ZERO : totalCount);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<SoReturnInstockDTO.PagingView> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnInstockDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, String> skuMap = CollUtil.isNotEmpty(skuVOS) ? skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName)) : Collections.emptyMap();
        //获取退货单id
        List<String> sourceIds = records.stream().map(SoReturnInstockDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        List<String> b2cSoIds = records.stream().map(SoReturnInstockDTO.PagingView::getSoId).distinct().collect(Collectors.toList());
        List<String> returnMainIds = records.stream().map(SoReturnInstockDTO.PagingView::getSoReturnId).distinct().collect(Collectors.toList());
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = CollUtil.isNotEmpty(returnMainIds) ? soReturnFeign.listDetailByMainIds(returnMainIds) : Collections.emptyList();
        Map<String, Integer> returnQtyMap = CollUtil.isNotEmpty(returnDetailEntityList) ? returnDetailEntityList.stream().collect(Collectors.toMap(SoReturnDetailEntity::getId, SoReturnDetailEntity::getReturnQty)) : Collections.emptyMap();
        Map<String, String> sourceDetailMap = CollUtil.isNotEmpty(returnDetailEntityList) ? returnDetailEntityList.stream().collect(Collectors.toMap(SoReturnDetailEntity::getId, SoReturnDetailEntity::getSourceDetailId)) : Collections.emptyMap();
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = CollUtil.isNotEmpty(detailIds) ? soInfoFeign.listSoDetailByIds(detailIds) : Collections.emptyList();
        Map<String, SoDetailEntity> detailEntityMap = CollUtil.isNotEmpty(soDetailEntities) ? soDetailEntities.stream().collect(Collectors.toMap(SoDetailEntity::getId, Function.identity())) : Collections.emptyMap();

        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnMainIds);
        Map<String, Integer> receiveQtyMap2 = CollUtil.isNotEmpty(soReturnReceiveDetailEntities) ? soReturnReceiveDetailEntities.stream()
                .filter(detail -> CharSequenceUtil.isNotBlank(detail.getSourceDetailId())
                        && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()))
                .collect(Collectors.groupingBy(
                        detail -> detail.getSourceDetailId() + "-" + detail.getSkuId(),
                        Collectors.summingInt(SoReturnReceiveDetailEntity::getReceiveQty)
                )) : Collections.emptyMap();
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntitiesSourceIds = soReturnReceiveDetailService.listDetailByMainIds(sourceIds);
        Map<String, Integer> receiveQtyMap = CollUtil.isNotEmpty(soReturnReceiveDetailEntitiesSourceIds) ? soReturnReceiveDetailEntitiesSourceIds.stream()
                .filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()))
                .collect(Collectors.toMap(SoReturnReceiveDetailEntity::getId, SoReturnReceiveDetailEntity::getReceiveQty, Integer::sum)) : Collections.emptyMap();

        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        soIds.addAll(b2cSoIds);
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        Map<String, Integer> actualQtyMap = CollUtil.isNotEmpty(soOutstockDetailEntities) ? soOutstockDetailEntities.stream()
                .filter(detail -> CharSequenceUtil.isNotBlank(detail.getSoId())
                        && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()))
                .collect(Collectors.groupingBy(
                        detail -> detail.getSoId() + "-" + detail.getSkuId(),
                        Collectors.summingInt(SoOutstockDetailEntity::getActualQty)
                )) : Collections.emptyMap();
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        Map<String, String> platformCodeMap = CollUtil.isNotEmpty(soB2cEntityList) ? soB2cEntityList.stream().collect(Collectors.toMap(SoB2cEntity::getId, SoB2cEntity::getPlatformCode)) : Collections.emptyMap();

        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(soIds);
        Map<String, Integer> soB2cDetailEntityMap = CollUtil.isNotEmpty(soB2cDetailEntityList) ? soB2cDetailEntityList.stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getMainId() + "-" + detail.getSkuId(),
                        Collectors.summingInt(SoB2cDetailEntity::getQty))) : Collections.emptyMap();

        List<String> customerIds = records.stream().map(SoReturnInstockDTO.PagingView::getCustomerId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = CollUtil.isNotEmpty(customerIds) ? customerFeign.listCustomerByIds(customerIds) :Collections.emptyList();
        Map<String, String> customerMap = CollUtil.isNotEmpty(customerInfoEntities) ? customerInfoEntities.stream().collect(Collectors.toMap(CustomerInfoEntity::getId, CustomerInfoEntity::getName)) : Collections.emptyMap();
        //客户归属平台
        Map<String, String> customerPlatformMap = CollUtil.isNotEmpty(customerInfoEntities) ? customerInfoEntities.stream()
                .filter(c -> CharSequenceUtil.isNotBlank(c.getPlatformType()))
                .collect(Collectors.toMap(CustomerInfoEntity::getId, CustomerInfoEntity::getPlatformType, (a, b) -> a)) : Collections.emptyMap();
        //B2C售后单平台（type=B2C时按soReturnId取so_b2c_return.dict_platform）
        List<String> b2cReturnIds = records.stream()
                .filter(r -> BillTypeEnum.B2C.getCode().equals(r.getType()) && CharSequenceUtil.isNotBlank(r.getSoReturnId()))
                .map(SoReturnInstockDTO.PagingView::getSoReturnId).distinct().collect(Collectors.toList());
        List<SoB2cReturnEntity> b2cReturnEntities = CollUtil.isNotEmpty(b2cReturnIds) ? soB2cReturnFeign.listByIds(b2cReturnIds) : Collections.emptyList();
        Map<String, String> b2cReturnPlatformMap = CollUtil.isNotEmpty(b2cReturnEntities) ? b2cReturnEntities.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getDictPlatform()))
                .collect(Collectors.toMap(SoB2cReturnEntity::getId, SoB2cReturnEntity::getDictPlatform, (a, b) -> a)) : Collections.emptyMap();

        // B2C售后明细退货数量，及按售后明细累计的入库实退数量（用于补齐 mustQty / remainShouldQty）
        List<String> b2cReturnDetailIds = records.stream()
                .filter(r -> BillTypeEnum.B2C.getCode().equals(r.getType()) && CharSequenceUtil.isNotBlank(r.getSoReturnDetailId()))
                .map(SoReturnInstockDTO.PagingView::getSoReturnDetailId).distinct().collect(Collectors.toList());
        List<SoB2cReturnDetailEntity> b2cReturnDetailEntities = CollUtil.isNotEmpty(b2cReturnDetailIds)
                ? FeignQuery.getByIds(SoB2cReturnDetailEntity.class, b2cReturnDetailIds) : Collections.emptyList();
        Map<String, Integer> b2cReturnQtyMap = CollUtil.isNotEmpty(b2cReturnDetailEntities) ? b2cReturnDetailEntities.stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getId()) && Objects.nonNull(d.getReturnQty()))
                .collect(Collectors.toMap(SoB2cReturnDetailEntity::getId, SoB2cReturnDetailEntity::getReturnQty, (a, b) -> a)) : Collections.emptyMap();
        List<SoReturnInstockDetailEntity> b2cInstockDetailEntities = CollUtil.isNotEmpty(b2cReturnDetailIds)
                ? soReturnInstockDetailService.listDetailBySoReturnDetailIds(b2cReturnDetailIds) : Collections.emptyList();
        Map<String, Integer> b2cInstockRealQtyMap = CollUtil.isNotEmpty(b2cInstockDetailEntities) ? b2cInstockDetailEntities.stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getSoReturnDetailId()))
                .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSoReturnDetailId,
                        Collectors.summingInt(d -> d.getRealQty() != null ? d.getRealQty() : MathUtil.ZERO))) : Collections.emptyMap();
        // 无售后关联时：按 sourceDetailId 累计有效入库实退（与落库口径一致）
        List<String> b2cSourceDetailIds = records.stream()
                .filter(r -> BillTypeEnum.B2C.getCode().equals(r.getType())
                        && CharSequenceUtil.isBlank(r.getSoReturnDetailId())
                        && CharSequenceUtil.isNotBlank(r.getSourceDetailId()))
                .map(SoReturnInstockDTO.PagingView::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> b2cInstockBySourceDetailIds = CollUtil.isNotEmpty(b2cSourceDetailIds)
                ? soReturnInstockDetailService.listDetailBySourceDetailIds(b2cSourceDetailIds) : Collections.emptyList();
        Map<String, Integer> b2cSourceDetailRealQtyMap = CollUtil.isNotEmpty(b2cInstockBySourceDetailIds) ? b2cInstockBySourceDetailIds.stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getSourceDetailId()))
                .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSourceDetailId,
                        Collectors.summingInt(d -> d.getRealQty() != null ? d.getRealQty() : MathUtil.ZERO))) : Collections.emptyMap();

        //查询审核流程
        List<String> ids = records.stream().map(SoReturnInstockDTO.PagingView::getId).distinct().collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_RETURN_INSTOCK.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(ApiError.WF_CUR_APPROVER_QUERY_FAILED, listApiResult.getMsg());
        }
        Map<String, String> approveNameMap = listApiResult.getData().stream().collect(Collectors.groupingBy(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId, Collectors.mapping(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName, Collectors.joining(","))));

        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setProductName(skuMap.get(obj.getSkuId()));
                obj.setCustomerName(customerMap.get(obj.getCustomerId()));
                //平台：优先取新增/修改时已落库的dict_platform；老数据为空时按原有逻辑临时计算兜底
                String platform = obj.getDictPlatform();
                if (CharSequenceUtil.isBlank(platform)) {
                    platform = resolveDictPlatform(obj.getType(), b2cReturnPlatformMap.get(obj.getSoReturnId()), customerPlatformMap.get(obj.getCustomerId()));
                }
                obj.setDictPlatform(platform);
                obj.setDictPlatformName(CharSequenceUtil.isNotBlank(platform) ? PlatformDictEnum.getNameByCode(platform) : null);
                if (BillTypeEnum.B2C.getCode().equals(obj.getType())) {
                    obj.setSalesQty(soB2cDetailEntityMap.getOrDefault(obj.getSoId() + "-" + obj.getSkuId(), 0));
                    obj.setReturnTypeDict(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                    obj.setPlatformOrderCode(CharSequenceUtil.isNotBlank(obj.getPlatformOrderCode()) ? obj.getPlatformOrderCode() : platformCodeMap.get(obj.getSoId()));
                    obj.setDeliveryQty(actualQtyMap.getOrDefault(obj.getSoId() + "-" + obj.getSkuId(), 0));
                    if (obj.getReceiveQty() == 0) {
                        obj.setReceiveQty(receiveQtyMap.getOrDefault(obj.getSourceDetailId(), MathUtil.ZERO));
                    }
                    // B2C：应退取售后明细退货数量；无售后取 mustQty（仅 null 时用签收兜底）；剩余应退按售后明细或 sourceDetailId 累计入库
                    Integer b2cReturnQty = b2cReturnQtyMap.get(obj.getSoReturnDetailId());
                    boolean hasAuthoritativeReturnQty = Objects.nonNull(b2cReturnQty);
                    if (Objects.isNull(b2cReturnQty)) {
                        b2cReturnQty = obj.getMustQty();
                    }
                    if (b2cReturnQty == null && CharSequenceUtil.isBlank(obj.getSoReturnDetailId())
                            && obj.getReceiveQty() != null) {
                        b2cReturnQty = obj.getReceiveQty();
                    }
                    if (Objects.nonNull(b2cReturnQty)) {
                        int instockQty;
                        if (CharSequenceUtil.isNotBlank(obj.getSoReturnDetailId())) {
                            instockQty = b2cInstockRealQtyMap.getOrDefault(obj.getSoReturnDetailId(), MathUtil.ZERO);
                        } else if (CharSequenceUtil.isNotBlank(obj.getSourceDetailId())) {
                            instockQty = b2cSourceDetailRealQtyMap.getOrDefault(obj.getSourceDetailId(), MathUtil.ZERO);
                        } else {
                            instockQty = obj.getRealQty() != null ? obj.getRealQty() : MathUtil.ZERO;
                        }
                        // 无售后单权威应退数量时，历史数据可能落成偏小/为0的 mustQty；以累计入库为下限，避免展示负数剩余应退
                        if (!hasAuthoritativeReturnQty && b2cReturnQty < instockQty) {
                            b2cReturnQty = instockQty;
                        }
                        obj.setMustQty(b2cReturnQty);
                        obj.setRemainShouldQty(b2cReturnQty - instockQty);
                    }
                } else {
                    String sourceDetailId = sourceDetailMap.get(obj.getSoReturnDetailId());
                    SoDetailEntity soDetailEntity = detailEntityMap.getOrDefault(sourceDetailId, new SoDetailEntity());
                    obj.setSalesQty(soDetailEntity.getQty());
                    obj.setReturnTypeDict(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                    if (CharSequenceUtil.isNotBlank(soDetailEntity.getMainId())) {
                        //获取退货数量
                        obj.setMustQty(returnQtyMap.getOrDefault(obj.getSoReturnDetailId(), MathUtil.ZERO));
                        obj.setDeliveryQty(actualQtyMap.getOrDefault(soDetailEntity.getMainId() + "-" + obj.getSkuId(), 0) * soDetailEntity.getPerBoxQty());
                        obj.setReceiveQty(receiveQtyMap2.getOrDefault(obj.getSourceDetailId() + "-" + obj.getSkuId(), MathUtil.ZERO));
                    } else {
                        obj.setReceiveQty(receiveQtyMap.getOrDefault(obj.getSourceDetailId(), MathUtil.ZERO));
                    }
                }
                //最新审核人
                obj.setApproveUserName(CharSequenceUtil.blankToDefault(approveNameMap.get(obj.getId()), obj.getApproveUserName()));
                obj.setType(BillTypeEnum.getName(obj.getType()));
            });
        }
    }

    /**
     * 计算平台字典值：B2C取售后单自身平台，否则（B2B或B2C售后单未取到平台时）取客户归属平台，可能为空
     */
    private String resolveDictPlatform(String type, String soB2cReturnDictPlatform, String customerPlatformType) {
        String platform = BillTypeEnum.B2C.getCode().equals(type) ? soB2cReturnDictPlatform : null;
        if (CharSequenceUtil.isBlank(platform)) {
            platform = customerPlatformType;
        }
        return platform;
    }

    private String getPermissionSql(String permissionSql) {
        //构造店铺权限
        String shopPermissionSql = authDataFeign.getShopPermissionSql("sri.shop_id");
        if (CharSequenceUtil.isAllNotBlank(permissionSql, shopPermissionSql)) {
            permissionSql = permissionSql + " AND ((sri.type = 'B2C' " + shopPermissionSql + ") OR (sri.type = 'B2B') OR (sri.type = 'AfterSale'))";
        } else if (CharSequenceUtil.isNotBlank(shopPermissionSql)) {
            permissionSql = " AND ((sri.type = 'B2C' " + shopPermissionSql + ") OR (sri.type = 'B2B') OR (sri.type = 'AfterSale'))";
        }
        return permissionSql;
    }

    @Override
    public List<SoReturnInstockDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SoReturnChangeListTypeEnum[] values = SoReturnChangeListTypeEnum.values();
        List<SoReturnInstockDTO.StatusCountDTO> list = new ArrayList<>();
        String permissionSql = getPermissionSql(dto.getPermissionSql());
        SoReturnInstockDTO.PagingParam pagingParam = new SoReturnInstockDTO.PagingParam();
        pagingParam.setPermissionSql(permissionSql);
        pagingParam.setInvalidStatus(Boolean.FALSE);
        List<ApproveStatusQtyDTO> tabList = this.baseMapper.listCount(pagingParam);
        Map<String, Integer> tabMap = tabList.stream().collect(Collectors.toMap(ApproveStatusQtyDTO::getApproveStatus, ApproveStatusQtyDTO::getCount));
        for (SoReturnChangeListTypeEnum item : values) {
            SoReturnInstockDTO.StatusCountDTO resultDTO = new SoReturnInstockDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (SoReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                count = tabMap.get(ApproveStatusEnum.APPROVE_ING.getStatus());
            }
            if (SoReturnChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                count = tabMap.get(ApproveStatusEnum.APPROVE.getStatus());
            }
            if (SoReturnChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                count = tabMap.get(ApproveStatusEnum.APPROVE.getStatus());
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
        return buildAndSaveInstock(dto).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoReturnInstockEntity addReturnEntity(SoReturnInstockDTO.Add dto) {
        return buildAndSaveInstock(dto);
    }

    private SoReturnInstockEntity buildAndSaveInstock(SoReturnInstockDTO.Add dto) {
        //校验数据
        List<SoReturnInstockDetailDTO.Add> detailList = dto.getDetailList();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException("明细不能为空");
        } else {
            boolean allMatch = detailList.stream().allMatch(v -> v.getRealQty() != null && v.getRealQty() > 0);
            if (Boolean.FALSE.equals(allMatch)) {
                throw new ServiceException("退货数量不能小于1");
            }
        }

        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSTH);
        SoReturnInstockEntity entity = new SoReturnInstockEntity();

        //退货单id
        String soReturnId = dto.getSoReturnId();
        //B2C售后单，用于平台字段计算（B2C取售后单平台）
        SoB2cReturnEntity soB2cReturnEntity = null;
        //当退货单不为空的时候
        if (CharSequenceUtil.isNotBlank(soReturnId)) {
            if (BillTypeEnum.B2C.getCode().equals(dto.getType())) {
                soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class, dto.getSoReturnId());
                dto.setShopId(soB2cReturnEntity.getShopId());
                //获取销售单信息
                SoB2cEntity soB2cEntity = FeignQuery.getById(SoB2cEntity.class, soB2cReturnEntity.getSoId());
                ShopInfoEntity shopInfoEntity = FeignQuery.getById(ShopInfoEntity.class, soB2cReturnEntity.getShopId());
                if (Objects.nonNull(soB2cEntity)) {
                    dto.setPlatformOrderCode(soB2cEntity.getPlatformCode());
                    SoOutstockEntity soOutstock = soOutstockService.getBySoId(soB2cEntity.getId());
                    if (Objects.nonNull(soOutstock)) {
                        dto.setSellerId(soOutstock.getSellerId());
                        dto.setSalesDeptId(soOutstock.getSalesDeptId());
                        dto.setSalesOrgId(soOutstock.getSalesOrgId());
                    }
                    dto.setExchangeRate(soB2cEntity.getExchangeRate());
                    dto.setCurrency(soB2cEntity.getCurrency());
                    dto.setCurrencySymbol(CurrencyEnum.getSymbolByCode(soB2cEntity.getCurrency()));
                }
                if (Objects.nonNull(shopInfoEntity) && CharSequenceUtil.isNotBlank(shopInfoEntity.getCustomerId())) {
                    dto.setCustomerId(shopInfoEntity.getCustomerId());
                    CustomerInfoEntity customerInfoEntity = FeignQuery.getById(CustomerInfoEntity.class, shopInfoEntity.getCustomerId());
                    if (Objects.nonNull(customerInfoEntity)) {
                        dto.setSellerId(CharSequenceUtil.isBlank(dto.getSellerId()) ? customerInfoEntity.getSellerId() : dto.getSellerId());
                        dto.setSalesDeptId(CharSequenceUtil.isBlank(dto.getSalesDeptId()) ? customerInfoEntity.getSalesDeptId() : dto.getSalesDeptId());
                        dto.setSalesOrgId(CharSequenceUtil.isBlank(dto.getSalesOrgId()) ? customerInfoEntity.getUseOrgId() : dto.getSalesOrgId());
                    }
                }
                entity.setSoId(soB2cReturnEntity.getSoId());
                entity.setSoCode(soB2cReturnEntity.getSoCode());
            } else {
                SoReturnEntity soReturn = soReturnFeign.getSoReturnById(soReturnId);
                if (!Objects.isNull(soReturn)) {
                    dto.setSoReturnId(soReturnId);
                    dto.setSoReturnCode(soReturn.getCode());
                    dto.setSellerId(soReturn.getSellerId());
                    dto.setCustomerId(soReturn.getCustomerId());
                    dto.setSalesDeptId(soReturn.getSalesDeptId());
                    dto.setWarehouseId(soReturn.getWarehouseId());
                    dto.setSalesOrgId(soReturn.getSalesOrgId());
                    dto.setType(soReturn.getType());

                    //对应的就是销售订单id
                    String soId = soReturn.getSourceId();
                    if (CharSequenceUtil.isNotBlank(soId)) {
                        SoInfoEntity soInfo = soInfoFeign.getSoInfoById(soId);
                        if (!Objects.isNull(soInfo)) {
                            entity.setSoId(soInfo.getId());
                            entity.setSoCode(soInfo.getCode());
                            //B2B订单平台订单编码，与soId/soCode一样直接写入entity
                            entity.setPlatformOrderCode(soInfo.getPlatformOrderCode());
                        }
                    }
                }
            }
        } else {
            //根据客户查询组装、部门、销售员
            CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, dto.getCustomerId());
            if (ObjectUtil.isNotEmpty(customerInfo)) {
                Optional.of(customerInfo).ifPresent(customerInfoEntity -> {
                    dto.setSalesDeptId(customerInfoEntity.getSalesDeptId());
                    dto.setSellerId(customerInfoEntity.getSellerId());
                    dto.setSalesOrgId(customerInfoEntity.getUseOrgId());
                });
            }
            if (CharSequenceUtil.isNotBlank(dto.getSourceId())) {
                SoReturnReceiveEntity soReturnReceiveEntity = soReturnReceiveService.getById(dto.getSourceId());
                if (null != soReturnReceiveEntity) {
                    dto.setSellerId(soReturnReceiveEntity.getSellerId());
                    dto.setCustomerId(soReturnReceiveEntity.getCustomerId());
                    dto.setSalesDeptId(soReturnReceiveEntity.getSalesDeptId());
                    dto.setSellerId(soReturnReceiveEntity.getSellerId());
                    dto.setSalesOrgId(soReturnReceiveEntity.getSalesOrgId());
                    dto.setType(soReturnReceiveEntity.getType());
                }
            }
        }
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(dto.getSellerId(), dto.getWarehouseKeeperId()));
        //获取客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(Collections.singletonList(dto.getCustomerId()));
        //获取部门信息
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(Collections.singletonList(dto.getSalesDeptId()));
        //获取核算公司
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Collections.singletonList(dto.getDetailList().get(0).getWarehouseId()));
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getDetailList().get(0).getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        //获取组织信息
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(dto.getSalesOrgId(), updateDTO.getOrgId()));
        entity.setType(dto.getType());
        entity.setShopId(dto.getShopId());
        entity.setSalesOrgId(dto.getSalesOrgId());
        String orgName = orgList.stream().filter(o -> CharSequenceUtil.isNotBlank(dto.getSalesOrgId()) && dto.getSalesOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
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
        //平台：B2C取售后单平台，否则取客户归属平台（可能为空）
        entity.setDictPlatform(resolveDictPlatform(dto.getType(), Objects.nonNull(soB2cReturnEntity) ? soB2cReturnEntity.getDictPlatform() : null, customerInfoEntity.getPlatformType()));
        entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        String warehouseKeeperUserName = userList.stream().filter(d -> d.getUserId().equals(dto.getWarehouseKeeperId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
        entity.setWarehouseKeeperName(warehouseKeeperUserName);
        entity.setInventoryOrgId(updateDTO.getOrgId());
        String warehouseOrgName = orgList.stream().filter(o -> updateDTO.getOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setInventoryOrgName(warehouseOrgName);
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        entity.setSoReturnId(dto.getSoReturnId());
        entity.setSoReturnCode(dto.getSoReturnCode());
        //平台订单编码：分支内已直接写入entity时不覆盖，否则用dto兜底
        if (CharSequenceUtil.isBlank(entity.getPlatformOrderCode())) {
            entity.setPlatformOrderCode(dto.getPlatformOrderCode());
        }
        entity.setThirdCode(dto.getThirdCode());
        entity.setSourceCode(dto.getSourceCode());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceId(dto.getSourceId());
        entity.setCode(code);
        entity.setBillDate(dto.getBillDate());
        //币种
        entity.setCurrency(dto.getCurrency());
        entity.setCurrencySymbol(dto.getCurrencySymbol());
        this.save(entity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货入库单【%s】", code), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "新增操作");

        soReturnInstockDetailService.add(dto, entity.getId());
        return entity;
    }

    @Override
    public List<SoReturnInstockDTO.ReturnLogisticPrefill> queryByReturnLogisticCode(String returnLogisticCode) {
        if (CharSequenceUtil.isBlank(returnLogisticCode)) {
            return Collections.emptyList();
        }
        // 两侧按物流单号查询全部命中（可能多条）
        List<SoReturnEntity> b2bReturnList = soReturnFeign.listByReturnLogisticCode(returnLogisticCode);
        List<SoB2cReturnEntity> b2cReturnList = soB2cReturnFeign.listByReturnLogisticCode(returnLogisticCode);
        if (CollUtil.isEmpty(b2bReturnList) && CollUtil.isEmpty(b2cReturnList)) {
            // 未命中，保持人工录入
            return Collections.emptyList();
        }

        // 批量预取B2B/B2C依赖数据，避免在循环内逐条查询Feign/DB（N+1）
        ReturnLogisticPrefillContext context = buildReturnLogisticPrefillContext(b2bReturnList, b2cReturnList);

        List<SoReturnInstockDTO.ReturnLogisticPrefill> result = new ArrayList<>();
        if (CollUtil.isNotEmpty(b2bReturnList)) {
            for (SoReturnEntity b2bReturn : b2bReturnList) {
                SoReturnInstockDTO.ReturnLogisticPrefill prefill = new SoReturnInstockDTO.ReturnLogisticPrefill();
                prefill.setReturnLogisticCode(returnLogisticCode);
                fillPrefillByB2bReturn(prefill, b2bReturn, context);
                result.add(prefill);
            }
        }
        if (CollUtil.isNotEmpty(b2cReturnList)) {
            for (SoB2cReturnEntity b2cReturn : b2cReturnList) {
                SoReturnInstockDTO.ReturnLogisticPrefill prefill = new SoReturnInstockDTO.ReturnLogisticPrefill();
                prefill.setReturnLogisticCode(returnLogisticCode);
                fillPrefillByB2cReturn(prefill, b2cReturn, context);
                result.add(prefill);
            }
        }
        // 名称兜底统一批量补全（销售员/销售部门/销售组织）
        batchFillPrefillSalesNames(result);
        // 合并两类售后单，按创建时间倒序，供前端选择
        result.sort(Comparator.comparing(SoReturnInstockDTO.ReturnLogisticPrefill::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * 按批量维度一次性预取B2B/B2C售后单反查所需的所有关联数据
     */
    private ReturnLogisticPrefillContext buildReturnLogisticPrefillContext(List<SoReturnEntity> b2bReturnList,
                                                                             List<SoB2cReturnEntity> b2cReturnList) {
        ReturnLogisticPrefillContext context = new ReturnLogisticPrefillContext();

        // B2B：批量取源销售单（用于平台订单号）
        List<String> b2bSourceIds = b2bReturnList.stream().map(SoReturnEntity::getSourceId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(b2bSourceIds)) {
            context.soInfoMap = soInfoFeign.listSoInfoByIds(b2bSourceIds).stream()
                    .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                    .collect(Collectors.toMap(SoInfoEntity::getId, Function.identity(), (a, b) -> a));
        }

        // 以下Map为"已出库数量"/"签收数量"/"剩余应退累计"批量预取所需的聚合集合，B2B与B2C共用同一套查询与计算口径
        List<String> outstockSoIds = new ArrayList<>();
        List<String> receiveSourceIds = new ArrayList<>();
        List<String> allSkuIds = new ArrayList<>();
        List<String> allReturnDetailIds = new ArrayList<>();

        // B2B：批量取售后单明细 + 关联销售单明细（用于产品名/销售数量/已出库数量的单箱系数）
        List<String> b2bReturnIds = b2bReturnList.stream().map(SoReturnEntity::getId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(b2bReturnIds)) {
            List<SoReturnDetailEntity> b2bAllDetails = soReturnFeign.listDetailByMainIds(b2bReturnIds);
            context.b2bDetailsByMainId = b2bAllDetails.stream()
                    .filter(d -> CharSequenceUtil.isNotBlank(d.getMainId()))
                    .collect(Collectors.groupingBy(SoReturnDetailEntity::getMainId));
            allSkuIds.addAll(b2bAllDetails.stream().map(SoReturnDetailEntity::getSkuId)
                    .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
            allReturnDetailIds.addAll(b2bAllDetails.stream().map(SoReturnDetailEntity::getId)
                    .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
            List<String> soDetailIds = b2bAllDetails.stream().map(SoReturnDetailEntity::getSourceDetailId)
                    .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(soDetailIds)) {
                context.soDetailById = soInfoFeign.listSoDetailByIds(soDetailIds).stream()
                        .filter(d -> CharSequenceUtil.isNotBlank(d.getId()))
                        .collect(Collectors.toMap(SoDetailEntity::getId, Function.identity(), (a, b) -> a));
                outstockSoIds.addAll(context.soDetailById.values().stream().map(SoDetailEntity::getMainId)
                        .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
            }
            receiveSourceIds.addAll(b2bReturnIds);
        }

        if (CollUtil.isNotEmpty(b2cReturnList)) {
            // B2C：批量取关联销售单 + 销售出库单
            List<String> b2cSoIds = b2cReturnList.stream().map(SoB2cReturnEntity::getSoId)
                    .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(b2cSoIds)) {
                context.soB2cMap = soB2cFeign.listByIds(b2cSoIds).stream()
                        .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                        .collect(Collectors.toMap(SoB2cEntity::getId, Function.identity(), (a, b) -> a));
                context.soOutstockMap = soOutstockService.listBySoIds(b2cSoIds).stream()
                        .filter(e -> CharSequenceUtil.isNotBlank(e.getSoId()))
                        .collect(Collectors.toMap(SoOutstockEntity::getSoId, Function.identity(), (a, b) -> a));
                outstockSoIds.addAll(b2cSoIds);
            }

            // B2C：批量取店铺，再据此批量取客户档案
            List<String> shopIds = b2cReturnList.stream().map(SoB2cReturnEntity::getShopId)
                    .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(shopIds)) {
                context.shopInfoMap = FeignQuery.getByIds(ShopInfoEntity.class, shopIds).stream()
                        .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                        .collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity(), (a, b) -> a));
                List<String> customerIds = context.shopInfoMap.values().stream().map(ShopInfoEntity::getCustomerId)
                        .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
                if (CollUtil.isNotEmpty(customerIds)) {
                    context.customerInfoMap = FeignQuery.getByIds(CustomerInfoEntity.class, customerIds).stream()
                            .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                            .collect(Collectors.toMap(CustomerInfoEntity::getId, Function.identity(), (a, b) -> a));
                }
            }

            // B2C：批量取全部售后单明细
            List<String> b2cReturnIds = b2cReturnList.stream().map(SoB2cReturnEntity::getId)
                    .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(b2cReturnIds)) {
                List<SoB2cReturnDetailDTO.ViewDTO> allDetails = soB2cReturnFeign.listDetailByMainIds(b2cReturnIds);
                context.b2cDetailsByMainId = allDetails.stream()
                        .filter(d -> CharSequenceUtil.isNotBlank(d.getMainId()))
                        .collect(Collectors.groupingBy(SoB2cReturnDetailDTO.ViewDTO::getMainId));
                allSkuIds.addAll(allDetails.stream().map(SoB2cReturnDetailDTO.ViewDTO::getSkuId)
                        .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
                allReturnDetailIds.addAll(allDetails.stream().map(SoB2cReturnDetailDTO.ViewDTO::getId)
                        .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
                receiveSourceIds.addAll(b2cReturnIds);
            }
        }

        // 产品名称：B2B/B2C全部SKU批量查询
        List<String> distinctSkuIds = allSkuIds.stream().distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(distinctSkuIds)) {
            context.skuNameById = plmTaskFeign.listSkuProductByIds(distinctSkuIds).stream()
                    .filter(s -> CharSequenceUtil.isNotBlank(s.getSkuId()))
                    .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName, (a, b) -> a));
        }

        // 剩余应退累计：按售后/退货单明细id批量累计历史已入库实退数量（同一售后单分批多次生成退货入库单场景，与 view()/paging() 口径一致），B2B/B2C共用
        List<String> distinctReturnDetailIds = allReturnDetailIds.stream().distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(distinctReturnDetailIds)) {
            context.instockRealQtyBySoReturnDetailId = soReturnInstockDetailService.listDetailBySoReturnDetailIds(distinctReturnDetailIds).stream()
                    .filter(d -> CharSequenceUtil.isNotBlank(d.getSoReturnDetailId()))
                    .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSoReturnDetailId,
                            Collectors.summingInt(d -> d.getRealQty() != null ? d.getRealQty() : MathUtil.ZERO)));
        }

        // 已出库数量：按 soId+"-"+skuId 汇总出库单明细实发数量（仅approve，与 view()/paging() 口径一致），B2B/B2C共用
        List<String> distinctOutstockSoIds = outstockSoIds.stream().distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(distinctOutstockSoIds)) {
            context.outstockActualQtyBySoIdSku = soOutstockDetailService.listDetailBySoIds(distinctOutstockSoIds).stream()
                    .filter(d -> CharSequenceUtil.isNotBlank(d.getSoId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(d.getApproveStatus()))
                    .collect(Collectors.groupingBy(d -> d.getSoId() + "-" + d.getSkuId(),
                            Collectors.summingInt(d -> d.getActualQty() != null ? d.getActualQty() : MathUtil.ZERO)));
        }

        // 签收数量：按售后/退货单明细id汇总签收单明细数量（仅approve，与 view()/paging() 口径一致），B2B/B2C共用
        List<String> distinctReceiveSourceIds = receiveSourceIds.stream().distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(distinctReceiveSourceIds)) {
            context.receiveQtyByReturnDetailId = soReturnReceiveDetailService.listDetailBySourceIds(distinctReceiveSourceIds).stream()
                    .filter(d -> CharSequenceUtil.isNotBlank(d.getSourceDetailId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(d.getApproveStatus()))
                    .collect(Collectors.groupingBy(SoReturnReceiveDetailEntity::getSourceDetailId,
                            Collectors.summingInt(d -> d.getReceiveQty() != null ? d.getReceiveQty() : MathUtil.ZERO)));
        }

        return context;
    }

    /**
     * B2B售后单带出：客户/销售组织/部门/销售员/币种，名称随售后单直接带出
     */
    private void fillPrefillByB2bReturn(SoReturnInstockDTO.ReturnLogisticPrefill prefill, SoReturnEntity soReturn,
                                         ReturnLogisticPrefillContext context) {
        prefill.setSoReturnId(soReturn.getId());
        prefill.setSoReturnCode(soReturn.getCode());
        prefill.setCreateTime(soReturn.getCreateTime());
        prefill.setType(soReturn.getType());
        prefill.setSourceType(SourceTypeEnum.SO_RETURN.getCode());
        prefill.setCustomerId(soReturn.getCustomerId());
        prefill.setCustomerName(soReturn.getCustomerName());
        prefill.setSalesOrgId(soReturn.getSalesOrgId());
        prefill.setSalesOrgName(soReturn.getSalesOrgName());
        prefill.setSalesDeptId(soReturn.getSalesDeptId());
        prefill.setSalesDeptName(soReturn.getSalesDeptName());
        prefill.setSellerId(soReturn.getSellerId());
        prefill.setSellerName(soReturn.getSellerName());
        prefill.setCurrency(soReturn.getCurrency());
        prefill.setCurrencySymbol(CharSequenceUtil.isNotBlank(soReturn.getCurrencySymbol())
                ? soReturn.getCurrencySymbol() : CurrencyEnum.getSymbolByCode(soReturn.getCurrency()));
        // 平台订单号：取关联销售单（已批量预取）
        if (CharSequenceUtil.isNotBlank(soReturn.getSourceId())) {
            SoInfoEntity soInfo = context.soInfoMap.get(soReturn.getSourceId());
            if (Objects.nonNull(soInfo)) {
                prefill.setPlatformOrderCode(soInfo.getPlatformOrderCode());
            }
        }

        // 明细：带出全部SKU，应退数量用退货单明细自身的退货数量(return_qty)，均取自批量预取的Map
        List<SoReturnDetailEntity> details = context.b2bDetailsByMainId.get(soReturn.getId());
        if (CollUtil.isEmpty(details)) {
            return;
        }
        List<SoReturnInstockDTO.PrefillDetail> prefillDetails = new ArrayList<>(details.size());
        for (SoReturnDetailEntity d : details) {
            SoReturnInstockDTO.PrefillDetail pd = new SoReturnInstockDTO.PrefillDetail();
            pd.setSoReturnDetailId(d.getId());
            pd.setSkuId(d.getSkuId());
            pd.setSkuNo(d.getSkuNo());
            pd.setPlatformSkuNo(d.getPlatformSkuNo());
            pd.setProductName(context.skuNameById.get(d.getSkuId()));
            SoDetailEntity soDetail = context.soDetailById.get(d.getSourceDetailId());
            pd.setSalesQty(Objects.nonNull(soDetail) ? soDetail.getQty() : null);
            pd.setMustQty(d.getReturnQty());
            // 剩余应退 = 应退数量 - 历史已入库实退数量累计（同一退货单分批多次生成退货入库单场景）
            Integer mustQty = d.getReturnQty();
            if (mustQty != null) {
                int instockQty = context.instockRealQtyBySoReturnDetailId.getOrDefault(d.getId(), MathUtil.ZERO);
                pd.setRemainShouldQty(mustQty - instockQty);
            }
            // 已出库数量：按销售单soId+skuId汇总，再乘以单箱数量（与 view() 口径一致）；签收数量：按退货单明细id汇总
            if (Objects.nonNull(soDetail) && CharSequenceUtil.isNotBlank(soDetail.getMainId())) {
                int actualQty = context.outstockActualQtyBySoIdSku.getOrDefault(soDetail.getMainId() + "-" + d.getSkuId(), MathUtil.ZERO);
                int perBoxQty = Objects.nonNull(soDetail.getPerBoxQty()) ? soDetail.getPerBoxQty() : 1;
                pd.setDeliveryQty(actualQty * perBoxQty);
            } else {
                pd.setDeliveryQty(MathUtil.ZERO);
            }
            pd.setReceiveQty(context.receiveQtyByReturnDetailId.getOrDefault(d.getId(), MathUtil.ZERO));
            prefillDetails.add(pd);
        }
        prefill.setDetailList(prefillDetails);
    }

    /**
     * B2C售后单带出：客户(店铺→客户)、销售组织/部门/销售员(出库单优先,客户档案兜底)、币种，并带出全部SKU明细
     */
    private void fillPrefillByB2cReturn(SoReturnInstockDTO.ReturnLogisticPrefill prefill, SoB2cReturnEntity soB2cReturn,
                                         ReturnLogisticPrefillContext context) {
        prefill.setSoReturnId(soB2cReturn.getId());
        prefill.setSoReturnCode(soB2cReturn.getCode());
        prefill.setCreateTime(soB2cReturn.getCreateTime());
        prefill.setType(BillTypeEnum.B2C.getCode());
        prefill.setSourceType(SourceTypeEnum.SO_B2C_RETURN.getCode());
        prefill.setShopId(soB2cReturn.getShopId());
        prefill.setCurrency(soB2cReturn.getCurrency());
        prefill.setCurrencySymbol(CurrencyEnum.getSymbolByCode(soB2cReturn.getCurrency()));

        // 销售单：平台订单号 + 销售组织/部门/销售员（与 add() 口径一致，均取自批量预取的Map）
        SoB2cEntity soB2c = context.soB2cMap.get(soB2cReturn.getSoId());
        if (Objects.nonNull(soB2c)) {
            prefill.setPlatformOrderCode(soB2c.getPlatformCode());
            if (CharSequenceUtil.isBlank(prefill.getCurrency())) {
                prefill.setCurrency(soB2c.getCurrency());
                prefill.setCurrencySymbol(CurrencyEnum.getSymbolByCode(soB2c.getCurrency()));
            }
            SoOutstockEntity soOutstock = context.soOutstockMap.get(soB2c.getId());
            if (Objects.nonNull(soOutstock)) {
                prefill.setSellerId(soOutstock.getSellerId());
                prefill.setSalesDeptId(soOutstock.getSalesDeptId());
                prefill.setSalesOrgId(soOutstock.getSalesOrgId());
            }
        }

        // 客户：店铺 → 客户id → 客户档案，缺失的销售信息用客户档案兜底
        ShopInfoEntity shopInfo = context.shopInfoMap.get(soB2cReturn.getShopId());
        if (Objects.nonNull(shopInfo) && CharSequenceUtil.isNotBlank(shopInfo.getCustomerId())) {
            prefill.setCustomerId(shopInfo.getCustomerId());
            CustomerInfoEntity customerInfo = context.customerInfoMap.get(shopInfo.getCustomerId());
            if (Objects.nonNull(customerInfo)) {
                prefill.setCustomerName(customerInfo.getName());
                prefill.setSellerId(CharSequenceUtil.isBlank(prefill.getSellerId()) ? customerInfo.getSellerId() : prefill.getSellerId());
                prefill.setSalesDeptId(CharSequenceUtil.isBlank(prefill.getSalesDeptId()) ? customerInfo.getSalesDeptId() : prefill.getSalesDeptId());
                prefill.setSalesOrgId(CharSequenceUtil.isBlank(prefill.getSalesOrgId()) ? customerInfo.getUseOrgId() : prefill.getSalesOrgId());
            }
        }

        // 明细：带出全部SKU，应退数量用退货数量(return_qty)，均取自批量预取的Map
        List<SoB2cReturnDetailDTO.ViewDTO> details = context.b2cDetailsByMainId.get(soB2cReturn.getId());
        if (CollUtil.isEmpty(details)) {
            return;
        }
        List<SoReturnInstockDTO.PrefillDetail> prefillDetails = new ArrayList<>(details.size());
        for (SoB2cReturnDetailDTO.ViewDTO d : details) {
            SoReturnInstockDTO.PrefillDetail pd = new SoReturnInstockDTO.PrefillDetail();
            pd.setSoReturnDetailId(d.getId());
            pd.setSkuId(d.getSkuId());
            pd.setSkuNo(d.getSkuNo());
            pd.setPlatformSkuNo(d.getPlatformSkuNo());
            pd.setProductName(context.skuNameById.get(d.getSkuId()));
            pd.setSalesQty(d.getSaleQty());
            pd.setMustQty(d.getReturnQty());
            // 剩余应退 = 应退数量 - 历史已入库实退数量累计（同一售后单分批多次生成退货入库单场景）
            Integer mustQty = d.getReturnQty();
            if (mustQty != null) {
                int instockQty = context.instockRealQtyBySoReturnDetailId.getOrDefault(d.getId(), MathUtil.ZERO);
                pd.setRemainShouldQty(mustQty - instockQty);
            }
            // 已出库数量 / 签收数量，口径与 view()/paging() 保持一致
            pd.setDeliveryQty(context.outstockActualQtyBySoIdSku.getOrDefault(soB2cReturn.getSoId() + "-" + d.getSkuId(), MathUtil.ZERO));
            pd.setReceiveQty(context.receiveQtyByReturnDetailId.getOrDefault(d.getId(), MathUtil.ZERO));
            prefillDetails.add(pd);
        }
        prefill.setDetailList(prefillDetails);
    }

    /**
     * 销售组织/部门/销售员名称批量补全（复用 add() 中的 sysUserFeign 查询口径，一次性批量查询避免逐条Feign调用）
     */
    private void batchFillPrefillSalesNames(List<SoReturnInstockDTO.ReturnLogisticPrefill> prefillList) {
        if (CollUtil.isEmpty(prefillList)) {
            return;
        }
        List<String> sellerIds = prefillList.stream()
                .filter(p -> CharSequenceUtil.isNotBlank(p.getSellerId()) && CharSequenceUtil.isBlank(p.getSellerName()))
                .map(SoReturnInstockDTO.ReturnLogisticPrefill::getSellerId).distinct().collect(Collectors.toList());
        Map<String, String> sellerNameMap = CollUtil.isNotEmpty(sellerIds) ? sysUserFeign.getUserListByUserIds(sellerIds).stream()
                .filter(u -> CharSequenceUtil.isNotBlank(u.getUserId()))
                .collect(Collectors.toMap(FindUserDTO::getUserId, u -> CharSequenceUtil.emptyToDefault(u.getUserName(), ""), (a, b) -> a))
                : Collections.emptyMap();

        List<String> deptIds = prefillList.stream()
                .filter(p -> CharSequenceUtil.isNotBlank(p.getSalesDeptId()) && CharSequenceUtil.isBlank(p.getSalesDeptName()))
                .map(SoReturnInstockDTO.ReturnLogisticPrefill::getSalesDeptId).distinct().collect(Collectors.toList());
        Map<String, String> deptNameMap = CollUtil.isNotEmpty(deptIds) ? sysUserFeign.listDeptByIds(deptIds).stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getId()))
                .collect(Collectors.toMap(SysDepartmentEntity::getId, d -> CharSequenceUtil.emptyToDefault(d.getName(), ""), (a, b) -> a))
                : Collections.emptyMap();

        List<String> orgIds = prefillList.stream()
                .filter(p -> CharSequenceUtil.isNotBlank(p.getSalesOrgId()) && CharSequenceUtil.isBlank(p.getSalesOrgName()))
                .map(SoReturnInstockDTO.ReturnLogisticPrefill::getSalesOrgId).distinct().collect(Collectors.toList());
        Map<String, String> orgNameMap = CollUtil.isNotEmpty(orgIds) ? sysUserFeign.getAccountingCompanyList(orgIds).stream()
                .filter(o -> CharSequenceUtil.isNotBlank(o.getId()))
                .collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, o -> CharSequenceUtil.emptyToDefault(o.getName(), ""), (a, b) -> a))
                : Collections.emptyMap();

        for (SoReturnInstockDTO.ReturnLogisticPrefill prefill : prefillList) {
            if (CharSequenceUtil.isNotBlank(prefill.getSellerId()) && CharSequenceUtil.isBlank(prefill.getSellerName())) {
                prefill.setSellerName(sellerNameMap.getOrDefault(prefill.getSellerId(), ""));
            }
            if (CharSequenceUtil.isNotBlank(prefill.getSalesDeptId()) && CharSequenceUtil.isBlank(prefill.getSalesDeptName())) {
                prefill.setSalesDeptName(deptNameMap.getOrDefault(prefill.getSalesDeptId(), ""));
            }
            if (CharSequenceUtil.isNotBlank(prefill.getSalesOrgId()) && CharSequenceUtil.isBlank(prefill.getSalesOrgName())) {
                prefill.setSalesOrgName(orgNameMap.getOrDefault(prefill.getSalesOrgId(), ""));
            }
        }
    }

    /**
     * queryByReturnLogisticCode 批量预取的依赖数据容器
     */
    private static class ReturnLogisticPrefillContext {
        Map<String, SoInfoEntity> soInfoMap = Collections.emptyMap();
        Map<String, SoB2cEntity> soB2cMap = Collections.emptyMap();
        Map<String, SoOutstockEntity> soOutstockMap = Collections.emptyMap();
        Map<String, ShopInfoEntity> shopInfoMap = Collections.emptyMap();
        Map<String, CustomerInfoEntity> customerInfoMap = Collections.emptyMap();
        Map<String, List<SoB2cReturnDetailDTO.ViewDTO>> b2cDetailsByMainId = Collections.emptyMap();
        // B2B售后单明细，按退货单(so_return.id)分组
        Map<String, List<SoReturnDetailEntity>> b2bDetailsByMainId = Collections.emptyMap();
        // B2B销售单明细，按id索引（so_return_detail.source_detail_id -> so_detail），用于取soId/单箱数量/销售数量
        Map<String, SoDetailEntity> soDetailById = Collections.emptyMap();
        Map<String, String> skuNameById = Collections.emptyMap();
        // 按售后/退货单明细id累计的历史已入库实退数量，用于计算剩余应退数量（跨批次退货入库单累计），B2B/B2C共用
        Map<String, Integer> instockRealQtyBySoReturnDetailId = Collections.emptyMap();
        // 按 soId+"-"+skuId 汇总的已出库数量（仅统计approve，与 view()/paging() 口径一致），B2B/B2C共用
        Map<String, Integer> outstockActualQtyBySoIdSku = Collections.emptyMap();
        // 按售后/退货单明细id汇总的签收数量（仅统计approve，与 view()/paging() 口径一致），B2B/B2C共用
        Map<String, Integer> receiveQtyByReturnDetailId = Collections.emptyMap();
    }

    // ===================== matchAndCreateByReturnLogisticCode：按退货物流单号匹配售后单并生成退货入库单 =====================

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<PlatformReturnInstockDTO.Detail> matchAndCreateByReturnLogisticCode(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
        List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
        if (CollUtil.isEmpty(details)) {
            return Collections.emptyList();
        }
        String returnLogisticCode = dto.getReturnLogisticCode();
        List<SoReturnEntity> b2bReturnList = soReturnFeign.listByReturnLogisticCode(returnLogisticCode);
        List<SoB2cReturnEntity> b2cReturnList = soB2cReturnFeign.listByReturnLogisticCode(returnLogisticCode);
        if (CollUtil.isEmpty(b2bReturnList) && CollUtil.isEmpty(b2cReturnList)) {
            // 两侧均未命中，原样返回交由调用方继续走下一分支
            return details;
        }

        // B2C 数据完整性预校验：店铺/客户信息缺失的售后单不参与匹配，其明细视为未匹配，交由后续分支兜底
        ReturnLogisticB2cContext b2cContext = buildReturnLogisticB2cContext(b2cReturnList);
        List<SoB2cReturnEntity> validB2cReturnList = b2cReturnList.stream()
                .filter(v -> {
                    ShopInfoEntity shopInfo = b2cContext.shopInfoMap.get(v.getShopId());
                    return Objects.nonNull(shopInfo) && CharSequenceUtil.isNotBlank(shopInfo.getCustomerId())
                            && b2cContext.customerInfoMap.containsKey(shopInfo.getCustomerId());
                })
                .collect(Collectors.toList());
        if (validB2cReturnList.size() != b2cReturnList.size()) {
            log.warn("[海外仓退货入库-物流单号匹配] 部分B2C售后单店铺/客户信息缺失，不参与匹配：returnLogisticCode={}", returnLogisticCode);
        }

        Map<String, SoReturnEntity> b2bReturnMap = b2bReturnList.stream()
                .collect(Collectors.toMap(SoReturnEntity::getId, Function.identity(), (a, b) -> a));
        Map<String, SoB2cReturnEntity> b2cReturnMap = validB2cReturnList.stream()
                .collect(Collectors.toMap(SoB2cReturnEntity::getId, Function.identity(), (a, b) -> a));
        // 批量预取B2B候选售后单对应客户的平台归属，用于回填退货入库单dictPlatform
        Map<String, CustomerInfoEntity> b2bCustomerInfoMap = buildCustomerInfoMapByIds(
                b2bReturnList.stream().map(SoReturnEntity::getCustomerId).collect(Collectors.toList()));

        List<ReturnGapDetail> gapDetails = buildReturnGapDetails(b2bReturnList, validB2cReturnList);
        if (CollUtil.isEmpty(gapDetails)) {
            // 候选售后单均已完全入库（无缺口），视为未匹配
            return details;
        }
        Map<String, List<ReturnGapDetail>> gapDetailsBySkuId = gapDetails.stream()
                .collect(Collectors.groupingBy(g -> g.skuId));

        Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMapByPlatformSku = buildSkuMappingByPlatformSku(dto);

        // mainId -> 分配到的入库明细行；用LinkedHashMap保证生成顺序与匹配顺序一致，便于排查
        Map<String, List<SoReturnInstockDetailEntity>> instockDetailsByMainId = new LinkedHashMap<>();
        List<PlatformReturnInstockDTO.Detail> remaining = new ArrayList<>();

        for (PlatformReturnInstockDTO.Detail detail : details) {
            int mustQty = Objects.nonNull(detail.getMustQty()) ? detail.getMustQty() : 0;
            SkuMappingDTO.MappingSkuViewDTO skuView = mustQty > 0 ? skuMapByPlatformSku.get(detail.getProductSku()) : null;
            List<ReturnGapDetail> candidates = Objects.isNull(skuView) ? null : gapDetailsBySkuId.get(skuView.getProductSkuId());
            if (CollUtil.isEmpty(candidates)) {
                // 未映射到SKU，或该SKU无任何有缺口的候选售后单：整行未匹配
                remaining.add(detail);
                continue;
            }

            int receiveQty = Objects.nonNull(detail.getReceiveQty()) ? detail.getReceiveQty() : 0;
            int realQty = Objects.nonNull(detail.getRealQty()) ? detail.getRealQty() : 0;
            int mustQtyLeft = mustQty;
            int receiveQtyLeft = receiveQty;
            int realQtyLeft = realQty;
            // 记录本行最后一次分配产生的入库明细，用于在应退数量分配完毕后补齐按比例向下取整产生的舍入余数
            SoReturnInstockDetailEntity lastInstockDetail = null;
            for (ReturnGapDetail candidate : candidates) {
                if (mustQtyLeft <= 0) {
                    break;
                }
                if (candidate.gapQty <= 0) {
                    continue;
                }
                int allocateQty = Math.min(mustQtyLeft, candidate.gapQty);
                // 签收/实退数量按应退数量的分配比例向下取整分摊，剩余部分随mustQtyLeft一起进入下一候选或最终剩余，保证总量守恒
                int allocateReceiveQty = Math.min(receiveQtyLeft, proportionalFloor(receiveQty, allocateQty, mustQty));
                int allocateRealQty = Math.min(realQtyLeft, proportionalFloor(realQty, allocateQty, mustQty));

                SoReturnInstockDetailEntity instockDetail = new SoReturnInstockDetailEntity();
                instockDetail.setSkuId(skuView.getProductSkuId());
                instockDetail.setSkuNo(skuView.getProductSkuNo());
                instockDetail.setPlatformSkuNo(detail.getProductSku());
                instockDetail.setMustQty(allocateQty);
                instockDetail.setReceiveQty(allocateReceiveQty);
                instockDetail.setRealQty(allocateRealQty);
                instockDetail.setWarehouseId(warehouseEntity.getId());
                instockDetail.setWarehouseName(warehouseEntity.getName());
                instockDetail.setRemark(dto.getReason());
                // 退货类型/原因优先取匹配到的售后单，缺失时兜底用平台推送的退货类型（WEGO 默认"其他"）
                instockDetail.setReturnTypeDict(CharSequenceUtil.emptyToDefault(candidate.returnTypeDict, dto.getReturnType()));
                instockDetail.setReturnReasonDict(CharSequenceUtil.emptyToDefault(candidate.returnReasonDict, ""));
                instockDetail.setSoReturnDetailId(candidate.detailId);
                instockDetailsByMainId.computeIfAbsent(candidate.mainId, k -> new ArrayList<>()).add(instockDetail);

                candidate.gapQty -= allocateQty;
                mustQtyLeft -= allocateQty;
                receiveQtyLeft -= allocateReceiveQty;
                realQtyLeft -= allocateRealQty;
                lastInstockDetail = instockDetail;
            }
            // 应退数量（mustQty）已在候选间全部分配完毕，但签收/实退数量按比例向下取整可能残留舍入余数，
            // 此时不会再进入下方"remaining"未匹配分支承接，需补齐到本行最后一条入库明细，保证总量守恒
            if (mustQtyLeft <= 0 && Objects.nonNull(lastInstockDetail) && (receiveQtyLeft > 0 || realQtyLeft > 0)) {
                lastInstockDetail.setReceiveQty(lastInstockDetail.getReceiveQty() + receiveQtyLeft);
                lastInstockDetail.setRealQty(lastInstockDetail.getRealQty() + realQtyLeft);
            }
            if (mustQtyLeft > 0) {
                PlatformReturnInstockDTO.Detail remainingDetail = new PlatformReturnInstockDTO.Detail();
                remainingDetail.setThirdBarcode(detail.getThirdBarcode());
                remainingDetail.setProductSku(detail.getProductSku());
                remainingDetail.setThirdId(detail.getThirdId());
                remainingDetail.setMustQty(mustQtyLeft);
                remainingDetail.setReceiveQty(receiveQtyLeft);
                remainingDetail.setRealQty(realQtyLeft);
                remaining.add(remainingDetail);
            }
        }

        if (CollUtil.isEmpty(instockDetailsByMainId)) {
            // 未能分配到任何候选售后单
            return details;
        }

        SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
        // 循环内只收集需流转状态的 B2C 售后单，建单完成后统一 updateBatch，避免逐单 Feign 写拉长全局事务
        List<SoB2cReturnEntity> b2cStatusUpdateList = new ArrayList<>();
        for (Map.Entry<String, List<SoReturnInstockDetailEntity>> entry : instockDetailsByMainId.entrySet()) {
            String mainId = entry.getKey();
            List<SoReturnInstockDetailEntity> detailEntityList = entry.getValue();
            SoReturnEntity b2bReturn = b2bReturnMap.get(mainId);
            if (Objects.nonNull(b2bReturn)) {
                SoReturnInstockEntity instockEntity = buildInstockEntityFromB2bReturn(dto, warehouseEntity, company, b2bReturn, b2bCustomerInfoMap);
                this.addByThirdWarehouse(instockEntity, detailEntityList);
                continue;
            }
            SoB2cReturnEntity b2cReturn = b2cReturnMap.get(mainId);
            if (Objects.isNull(b2cReturn)) {
                // 理论上不会发生：gapDetails 仅来自 b2bReturnMap/b2cReturnMap 的key集合
                log.error("[海外仓退货入库-物流单号匹配] 内部状态异常，找不到对应售后单：mainId={}", mainId);
                continue;
            }
            SoReturnInstockEntity instockEntity = buildInstockEntityFromB2cReturn(dto, warehouseEntity, company, b2cReturn, b2cContext);
            // B2C售后单若为"待退货"，同步流转为"已退货"（先改内存，循环外批量落库）
            if (SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(b2cReturn.getStatus())) {
                b2cReturn.setStatus(SoB2cReturnStatusEnum.RETURNED.getCode());
                b2cStatusUpdateList.add(b2cReturn);
            }
            this.addByThirdWarehouse(instockEntity, detailEntityList);
        }
        if (CollUtil.isNotEmpty(b2cStatusUpdateList)) {
            for (List<SoB2cReturnEntity> batch : CollUtil.split(b2cStatusUpdateList, IMPORT_UPDATE_BATCH_SIZE)) {
                soB2cReturnFeign.updateBatch(batch);
            }
        }
        return remaining;
    }

    /**
     * 按比例向下取整分摊：totalQty * allocateBase / totalBase
     */
    private int proportionalFloor(int totalQty, int allocateBase, int totalBase) {
        if (totalBase <= 0 || totalQty <= 0) {
            return 0;
        }
        return (int) Math.floor(totalQty * (double) allocateBase / totalBase);
    }

    /**
     * 批量取B2B/B2C候选售后单明细，按已审核入库数量计算剩余缺口（缺口<=0的行剔除），
     * 跨B2B/B2C全局按售后单创建时间升序排列（createTime → mainId → detailId），
     * 保证同SKU多候选场景下的分配顺序与业务口径一致，不再因分段收集导致B2B整体优先于B2C。
     */
    private List<ReturnGapDetail> buildReturnGapDetails(List<SoReturnEntity> b2bReturnList, List<SoB2cReturnEntity> b2cReturnList) {
        List<ReturnGapDetail> result = new ArrayList<>();
        if (CollUtil.isNotEmpty(b2bReturnList)) {
            List<String> b2bReturnIds = b2bReturnList.stream().map(SoReturnEntity::getId).collect(Collectors.toList());
            List<SoReturnDetailEntity> b2bDetailList = soReturnFeign.listDetailByMainIds(b2bReturnIds);
            if (CollUtil.isNotEmpty(b2bDetailList)) {
                Map<String, Integer> instockQtyMap = sumApprovedInstockQtyByReturnDetailId(
                        b2bDetailList.stream().map(SoReturnDetailEntity::getId).collect(Collectors.toList()));
                Map<String, LocalDateTime> mainCreateTimeMap = b2bReturnList.stream()
                        .collect(Collectors.toMap(SoReturnEntity::getId, SoReturnEntity::getCreateTime, (a, b) -> a));
                for (SoReturnDetailEntity d : b2bDetailList) {
                    if (CharSequenceUtil.isBlank(d.getSkuId())) {
                        continue;
                    }
                    int returnQty = Objects.nonNull(d.getReturnQty()) ? d.getReturnQty() : 0;
                    int gap = returnQty - instockQtyMap.getOrDefault(d.getId(), 0);
                    if (gap > 0) {
                        ReturnGapDetail gapDetail = new ReturnGapDetail();
                        gapDetail.mainId = d.getMainId();
                        gapDetail.detailId = d.getId();
                        gapDetail.skuId = d.getSkuId();
                        gapDetail.gapQty = gap;
                        gapDetail.createTime = mainCreateTimeMap.get(d.getMainId());
                        // B2B退货类型/原因取自退货单明细
                        gapDetail.returnTypeDict = d.getReturnTypeDict();
                        gapDetail.returnReasonDict = d.getReturnReasonDict();
                        result.add(gapDetail);
                    }
                }
            }
        }
        if (CollUtil.isNotEmpty(b2cReturnList)) {
            List<String> b2cReturnIds = b2cReturnList.stream().map(SoB2cReturnEntity::getId).collect(Collectors.toList());
            List<SoB2cReturnDetailDTO.ViewDTO> b2cDetailList = soB2cReturnFeign.listDetailByMainIds(b2cReturnIds);
            if (CollUtil.isNotEmpty(b2cDetailList)) {
                Map<String, Integer> instockQtyMap = sumApprovedInstockQtyByReturnDetailId(
                        b2cDetailList.stream().map(SoB2cReturnDetailDTO.ViewDTO::getId).collect(Collectors.toList()));
                Map<String, LocalDateTime> mainCreateTimeMap = b2cReturnList.stream()
                        .collect(Collectors.toMap(SoB2cReturnEntity::getId, SoB2cReturnEntity::getCreateTime, (a, b) -> a));
                // B2C退货类型/原因在售后单主表上，按 mainId 取用
                Map<String, SoB2cReturnEntity> b2cReturnMap = b2cReturnList.stream()
                        .collect(Collectors.toMap(SoB2cReturnEntity::getId, Function.identity(), (a, b) -> a));
                for (SoB2cReturnDetailDTO.ViewDTO d : b2cDetailList) {
                    if (CharSequenceUtil.isBlank(d.getSkuId())) {
                        continue;
                    }
                    int returnQty = Objects.nonNull(d.getReturnQty()) ? d.getReturnQty() : 0;
                    int gap = returnQty - instockQtyMap.getOrDefault(d.getId(), 0);
                    if (gap > 0) {
                        ReturnGapDetail gapDetail = new ReturnGapDetail();
                        gapDetail.mainId = d.getMainId();
                        gapDetail.detailId = d.getId();
                        gapDetail.skuId = d.getSkuId();
                        gapDetail.gapQty = gap;
                        gapDetail.createTime = mainCreateTimeMap.get(d.getMainId());
                        // B2C退货类型/原因取自售后单主表（type/reason）
                        SoB2cReturnEntity b2cReturn = b2cReturnMap.get(d.getMainId());
                        if (Objects.nonNull(b2cReturn)) {
                            gapDetail.returnTypeDict = b2cReturn.getType();
                            gapDetail.returnReasonDict = b2cReturn.getReason();
                        }
                        result.add(gapDetail);
                    }
                }
            }
        }
        // 跨B2B/B2C全局排序，保证同SKU分配时早创建的售后单优先，而非B2B整体排在B2C之前
        result.sort(Comparator
                .comparing((ReturnGapDetail g) -> g.createTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(g -> g.mainId, Comparator.nullsLast(String::compareTo))
                .thenComparing(g -> g.detailId, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    /**
     * 按售后单明细id统计已审核入库单的实退数量之和；退货入库单明细的审核状态挂在主表，需先查明细再按主表id过滤已审核
     */
    private Map<String, Integer> sumApprovedInstockQtyByReturnDetailId(List<String> soReturnDetailIds) {
        if (CollUtil.isEmpty(soReturnDetailIds)) {
            return Collections.emptyMap();
        }
        List<SoReturnInstockDetailEntity> instockDetailList = soReturnInstockDetailService.lambdaQuery()
                .in(SoReturnInstockDetailEntity::getSoReturnDetailId, soReturnDetailIds)
                .list();
        if (CollUtil.isEmpty(instockDetailList)) {
            return Collections.emptyMap();
        }
        List<String> mainIds = instockDetailList.stream().map(SoReturnInstockDetailEntity::getMainId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        Set<String> approvedMainIds = this.lambdaQuery()
                .in(SoReturnInstockEntity::getId, mainIds)
                .eq(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                .list().stream().map(SoReturnInstockEntity::getId).collect(Collectors.toSet());
        return instockDetailList.stream()
                .filter(d -> approvedMainIds.contains(d.getMainId()))
                .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSoReturnDetailId,
                        Collectors.summingInt(d -> Objects.nonNull(d.getRealQty()) ? d.getRealQty() : 0)));
    }

    /**
     * 平台SKU -> SKU映射，复用listing/SKU映射关系查询，未映射到的SKU不出现在返回结果中
     */
    private Map<String, SkuMappingDTO.MappingSkuViewDTO> buildSkuMappingByPlatformSku(PlatformReturnInstockDTO dto) {
        List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
        if (CollUtil.isEmpty(details)) {
            return Collections.emptyMap();
        }
        List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(platformSkuNoList)) {
            return Collections.emptyMap();
        }
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        paramDTO.setAuthId(dto.getAuthId());
        paramDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
        return skuMappingFeign.listByPlatformSkuNoAndPlatform(paramDTO).stream()
                .filter(v -> CharSequenceUtil.isNotBlank(v.getPlatformSkuNo()) && CharSequenceUtil.isNotBlank(v.getProductSkuId()))
                .collect(Collectors.toMap(SkuMappingDTO.MappingSkuViewDTO::getPlatformSkuNo, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量预取B2C候选售后单所需的店铺→客户→销售部门依赖数据。
     * 销售部门通过 {@code sysUserFeign.listDeptByIds} 一次拉取，避免后续按售后单循环单条 Feign。
     */
    private ReturnLogisticB2cContext buildReturnLogisticB2cContext(List<SoB2cReturnEntity> b2cReturnList) {
        ReturnLogisticB2cContext context = new ReturnLogisticB2cContext();
        if (CollUtil.isEmpty(b2cReturnList)) {
            return context;
        }
        List<String> shopIds = b2cReturnList.stream().map(SoB2cReturnEntity::getShopId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(shopIds)) {
            return context;
        }
        context.shopInfoMap = FeignQuery.getByIds(ShopInfoEntity.class, shopIds).stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                .collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity(), (a, b) -> a));
        List<String> customerIds = context.shopInfoMap.values().stream().map(ShopInfoEntity::getCustomerId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(customerIds)) {
            context.customerInfoMap = FeignQuery.getByIds(CustomerInfoEntity.class, customerIds).stream()
                    .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                    .collect(Collectors.toMap(CustomerInfoEntity::getId, Function.identity(), (a, b) -> a));
        }
        List<String> salesDeptIds = context.customerInfoMap.values().stream()
                .map(CustomerInfoEntity::getSalesDeptId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(salesDeptIds)) {
            List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(salesDeptIds);
            if (CollUtil.isNotEmpty(deptList)) {
                context.deptNameMap = deptList.stream()
                        .filter(d -> CharSequenceUtil.isNotBlank(d.getId()))
                        .collect(Collectors.toMap(SysDepartmentEntity::getId,
                                d -> CharSequenceUtil.emptyToDefault(d.getName(), ""), (a, b) -> a));
            }
        }
        return context;
    }

    /**
     * 按客户id批量查询客户档案，忽略空白id，找不到的id不出现在返回结果中
     */
    private Map<String, CustomerInfoEntity> buildCustomerInfoMapByIds(List<String> customerIds) {
        List<String> distinctIds = customerIds.stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctIds)) {
            return Collections.emptyMap();
        }
        return FeignQuery.getByIds(CustomerInfoEntity.class, distinctIds).stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getId()))
                .collect(Collectors.toMap(CustomerInfoEntity::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 由匹配到的B2B售后单构建退货入库单主表：客户/组织/销售员等字段直接取自售后单自身
     */
    private SoReturnInstockEntity buildInstockEntityFromB2bReturn(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity,
                                                                    SysAccountingCompanyEntity company, SoReturnEntity b2bReturn,
                                                                    Map<String, CustomerInfoEntity> customerInfoMap) {
        SoReturnInstockEntity entity = new SoReturnInstockEntity();
        entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveUserName("system");
        entity.setBillDate(dto.getPutawayLocalDate());
        entity.setInventoryOrgId(warehouseEntity.getOrgId());
        entity.setInventoryOrgName(Objects.nonNull(company) ? company.getCompanyName() : "");
        entity.setWarehouseKeeperId(warehouseEntity.getChargeId());
        entity.setType(OrderTypeEnum.B2B.getCode());
        entity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
        entity.setThirdCode(dto.getPlatformReturnOrderNo());
        entity.setCreated(dto.getCreateTime());
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        entity.setSoReturnId(b2bReturn.getId());
        entity.setSoReturnCode(b2bReturn.getCode());
        // 本分支专属语义：来源编号=匹配到的售后单自身的销售单号，与平台仓分支(sourceCode=dto.getUniqueId())不同
        entity.setSourceCode(b2bReturn.getSourceCode());
        entity.setPlatformOrderCode(b2bReturn.getPlatformOrderCode());
        entity.setCustomerId(b2bReturn.getCustomerId());
        entity.setCustomerName(b2bReturn.getCustomerName());
        entity.setSalesOrgId(b2bReturn.getSalesOrgId());
        entity.setSalesOrgName(b2bReturn.getSalesOrgName());
        entity.setSalesDeptId(b2bReturn.getSalesDeptId());
        entity.setSalesDeptName(b2bReturn.getSalesDeptName());
        entity.setSellerId(b2bReturn.getSellerId());
        entity.setSellerName(b2bReturn.getSellerName());
        entity.setCurrency(b2bReturn.getCurrency());
        entity.setCurrencySymbol(CharSequenceUtil.isNotBlank(b2bReturn.getCurrencySymbol())
                ? b2bReturn.getCurrencySymbol() : CurrencyEnum.getSymbolByCode(b2bReturn.getCurrency()));
        // 平台：B2B无自身平台字段，取客户归属平台（可能为空）
        CustomerInfoEntity customerInfo = customerInfoMap.get(b2bReturn.getCustomerId());
        entity.setDictPlatform(resolveDictPlatform(entity.getType(), null,
                Objects.nonNull(customerInfo) ? customerInfo.getPlatformType() : null));
        return entity;
    }

    /**
     * 由匹配到的B2C售后单构建退货入库单主表：客户走店铺→客户档案，组织/部门/销售员随客户档案带出（与既有buildPlatformSoReturnInstockEntity口径一致）
     */
    private SoReturnInstockEntity buildInstockEntityFromB2cReturn(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity,
                                                                    SysAccountingCompanyEntity company, SoB2cReturnEntity b2cReturn,
                                                                    ReturnLogisticB2cContext context) {
        ShopInfoEntity shopInfo = context.shopInfoMap.get(b2cReturn.getShopId());
        CustomerInfoEntity customerInfo = context.customerInfoMap.get(shopInfo.getCustomerId());

        SoReturnInstockEntity entity = new SoReturnInstockEntity();
        entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveUserName("system");
        entity.setBillDate(dto.getPutawayLocalDate());
        entity.setInventoryOrgId(warehouseEntity.getOrgId());
        entity.setInventoryOrgName(Objects.nonNull(company) ? company.getCompanyName() : "");
        entity.setWarehouseKeeperId(warehouseEntity.getChargeId());
        entity.setType(OrderTypeEnum.B2C.getCode());
        entity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
        entity.setThirdCode(dto.getPlatformReturnOrderNo());
        entity.setCreated(dto.getCreateTime());
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        entity.setShopId(b2cReturn.getShopId());
        entity.setSoReturnId(b2cReturn.getId());
        entity.setSoReturnCode(b2cReturn.getCode());
        // 本分支专属语义：来源编号=匹配到的售后单自身的销售单号，与平台仓分支(sourceCode=dto.getUniqueId())不同
        entity.setSourceCode(b2cReturn.getSoCode());
        entity.setPlatformOrderCode(b2cReturn.getPlatformOrderNo());
        entity.setCurrency(CharSequenceUtil.isNotBlank(b2cReturn.getCurrency()) ? b2cReturn.getCurrency()
                : CharSequenceUtil.emptyToDefault(shopInfo.getSettlementCurrency(), CurrencyEnum.CNY.getCurrencyCode()));
        entity.setCurrencySymbol(CurrencyEnum.getSymbolByCode(entity.getCurrency()));
        entity.setSalesOrgId(customerInfo.getUseOrgId());
        entity.setSalesOrgName(customerInfo.getUseOrgName());
        entity.setCustomerId(shopInfo.getCustomerId());
        entity.setCustomerName(customerInfo.getName());
        // 销售部门名称已在 ReturnLogisticB2cContext 批量预取，此处仅读 Map，避免循环内单条 Feign
        if (CharSequenceUtil.isNotBlank(customerInfo.getSalesDeptId())
                && context.deptNameMap.containsKey(customerInfo.getSalesDeptId())) {
            entity.setSalesDeptId(customerInfo.getSalesDeptId());
            entity.setSalesDeptName(context.deptNameMap.get(customerInfo.getSalesDeptId()));
        }
        entity.setSellerId(customerInfo.getSellerId());
        entity.setSellerName(customerInfo.getSellerName());
        // 平台：B2C取售后单自身平台，否则取客户归属平台（可能为空）
        entity.setDictPlatform(resolveDictPlatform(entity.getType(), b2cReturn.getDictPlatform(), customerInfo.getPlatformType()));
        return entity;
    }

    /**
     * 候选售后单明细的剩余入库缺口（缺口 = 售后单退货数量 - 已审核入库数量），跨B2B/B2C统一表示，随分配过程递减
     */
    private static class ReturnGapDetail {
        String mainId;
        String detailId;
        String skuId;
        int gapQty;
        /** 售后单主表创建时间，用于跨B2B/B2C全局按创建时间升序分配 */
        LocalDateTime createTime;
        /** 匹配到的售后单退货类型字典值（B2B取明细，B2C取售后单主表 type），用于回写退货入库单明细 */
        String returnTypeDict;
        /** 匹配到的售后单退货原因字典值（B2B取明细，B2C取售后单主表 reason），用于回写退货入库单明细 */
        String returnReasonDict;
    }

    /**
     * matchAndCreateByReturnLogisticCode 中B2C分支所需的批量预取依赖（店铺→客户→销售部门）
     */
    private static class ReturnLogisticB2cContext {
        Map<String, ShopInfoEntity> shopInfoMap = Collections.emptyMap();
        Map<String, CustomerInfoEntity> customerInfoMap = Collections.emptyMap();
        /** 销售部门 id → 名称（来自 listDeptByIds） */
        Map<String, String> deptNameMap = Collections.emptyMap();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnInstockDTO.Update dto) {
        //校验数据
        List<SoReturnInstockDetailDTO.Update> detailList = dto.getDetailList();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException("明细不能为空");
        } else {
            boolean allMatch = detailList.stream().allMatch(v -> v.getRealQty() != null && v.getRealQty() > 0);
            if (Boolean.FALSE.equals(allMatch)) {
                throw new ServiceException("退货数量不能小于1");
            }
        }

        String id = dto.getId();
        SoReturnInstockEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.PO_RETURN_INBOUND_NOT_FOUND);
        }
        // 历史Entity
        SoReturnInstockEntity byId = new SoReturnInstockEntity();
        BeanMapper.copy(entity, byId);

        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        String warehouseKeeperName = "";
        if (CharSequenceUtil.isNotBlank(warehouseKeeperId)) {
            //获取用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Collections.singletonList(warehouseKeeperId));
            if (CollectionUtils.isNotEmpty(userList)) {
                warehouseKeeperName = userList.get(0).getUserName();
            }
        }
        // 海外仓退货存在未关联订单，允许修改客户；此处查询的customerInfo在下方计算dictPlatform时会复用，避免重复Feign调用
        CustomerInfoEntity customerInfoFromEdit = null;
        if (SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode().equalsIgnoreCase(entity.getSourceType()) || SourceTypeEnum.SELF_ADD.getCode().equalsIgnoreCase(entity.getSourceType())) {
            customerInfoFromEdit = customerFeign.getCustomerById(dto.getCustomerId());
            if (null == customerInfoFromEdit) {
                ServiceException.runError(ApiError.CUSTOMER_NOT_FOUND);
            }
            CustomerInfoEntity customerInfo = customerInfoFromEdit;
            entity.setCustomerId(dto.getCustomerId());
            entity.setCustomerName(customerInfo.getName());
            entity.setSellerId(customerInfo.getSellerId());
            entity.setSellerName(customerInfo.getSellerName());
            if (StringUtils.isNotBlank(customerInfo.getSalesDeptId())) {
                SysDepartmentDTO department = sysUserFeign.getUserDeptById(customerInfo.getSalesDeptId());
                if (null != department) {
                    entity.setSalesDeptId(customerInfo.getSalesDeptId());
                    entity.setSalesDeptName(department.getName());
                }
            }
            // 销售组织
            List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(customerInfo.getUseOrgId()));
            if (CollectionUtils.isEmpty(orgList)) {
                ServiceException.runError("销售组织不存在");
            }
            String salesOrgName = orgList.stream().filter(o -> customerInfo.getUseOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            entity.setSalesOrgName(salesOrgName);
            entity.setSalesOrgId(customerInfo.getUseOrgId());
            // 币种
            entity.setCurrencySymbol(dto.getCurrencySymbol());
            entity.setCurrency(dto.getCurrency());
        }

        //获取核算公司
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(dto.getDetailList().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList()));
        WarehouseDTO.UpdateDTO updateDTO = CollectionUtils.isEmpty(warehouseList) ? new WarehouseDTO.UpdateDTO() : warehouseList.get(0);
        //获取组织信息
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(updateDTO.getOrgId()));
        entity.setWarehouseKeeperId(warehouseKeeperId);
        entity.setWarehouseKeeperName(warehouseKeeperName);
        entity.setInventoryOrgId(updateDTO.getOrgId());
        String warehouseOrgName = orgList.stream().filter(o -> updateDTO.getOrgId().equals(o.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setInventoryOrgName(warehouseOrgName);
        entity.setBillDate(dto.getBillDate());
        entity.setType(dto.getType());
        //平台：B2C取售后单平台，否则取客户归属平台（可能为空）；客户/类型在上方"海外仓退货"分支可能已变更，此处按更新后的entity重新计算
        SoB2cReturnEntity soB2cReturnEntityForPlatform = BillTypeEnum.B2C.getCode().equals(entity.getType()) && CharSequenceUtil.isNotBlank(entity.getSoReturnId())
                ? FeignQuery.getById(SoB2cReturnEntity.class, entity.getSoReturnId()) : null;
        //上方"海外仓退货允许改客户"分支已按相同客户id查询过customerInfo，此处直接复用，避免重复Feign调用；
        //非该分支时 Feign 也可能返回 null（客户停用/删除等），平台字段允许为空，不得直接解引用
        CustomerInfoEntity customerInfoForPlatform = Objects.nonNull(customerInfoFromEdit) && customerInfoFromEdit.getId().equals(entity.getCustomerId())
                ? customerInfoFromEdit
                : (CharSequenceUtil.isNotBlank(entity.getCustomerId()) ? customerFeign.getCustomerById(entity.getCustomerId()) : null);
        String customerPlatformType = Objects.nonNull(customerInfoForPlatform) ? customerInfoForPlatform.getPlatformType() : null;
        entity.setDictPlatform(resolveDictPlatform(entity.getType(),
                Objects.nonNull(soB2cReturnEntityForPlatform) ? soB2cReturnEntityForPlatform.getDictPlatform() : null,
                customerPlatformType));
        if (!entity.getReturnLogisticCode().equals(dto.getReturnLogisticCode())) {
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("退货物流单号从{}修改为{}", entity.getReturnLogisticCode(), dto.getReturnLogisticCode()), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "编辑");
        }
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        //操作日志
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
        List<String> returnDetailIds = detailEntityList.stream().map(SoReturnInstockDetailEntity::getSoReturnDetailId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<SkuVO> productDetailEntitys = plmTaskFeign.listSkuProductByIds(skuIdList);
        //退货单
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainIds(Collections.singletonList(entity.getSoReturnId()));
        List<SoB2cReturnDetailEntity> returnB2cDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class, returnDetailIds);
        SoB2cReturnEntity soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class, entity.getSoReturnId());
        // B2C：按售后明细 / 来源明细累计入库实退，用于详情实时重算剩余应退货数量
        List<String> b2cReturnDetailIdsForRemain = returnDetailIds.stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> b2cInstockByReturnDetailIds = BillTypeEnum.B2C.getCode().equals(entity.getType()) && CollUtil.isNotEmpty(b2cReturnDetailIdsForRemain)
                ? soReturnInstockDetailService.listDetailBySoReturnDetailIds(b2cReturnDetailIdsForRemain) : Collections.emptyList();
        Map<String, Integer> b2cInstockRealQtyMap = CollUtil.isNotEmpty(b2cInstockByReturnDetailIds) ? b2cInstockByReturnDetailIds.stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getSoReturnDetailId()))
                .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSoReturnDetailId,
                        Collectors.summingInt(d -> d.getRealQty() != null ? d.getRealQty() : MathUtil.ZERO))) : Collections.emptyMap();
        List<String> b2cSourceDetailIdsForRemain = BillTypeEnum.B2C.getCode().equals(entity.getType())
                ? detailEntityList.stream()
                .filter(d -> CharSequenceUtil.isBlank(d.getSoReturnDetailId()) && CharSequenceUtil.isNotBlank(d.getSourceDetailId()))
                .map(SoReturnInstockDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList())
                : Collections.emptyList();
        List<SoReturnInstockDetailEntity> b2cInstockBySourceDetailIds = CollUtil.isNotEmpty(b2cSourceDetailIdsForRemain)
                ? soReturnInstockDetailService.listDetailBySourceDetailIds(b2cSourceDetailIdsForRemain) : Collections.emptyList();
        Map<String, Integer> b2cSourceDetailRealQtyMap = CollUtil.isNotEmpty(b2cInstockBySourceDetailIds) ? b2cInstockBySourceDetailIds.stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getSourceDetailId()))
                .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSourceDetailId,
                        Collectors.summingInt(d -> d.getRealQty() != null ? d.getRealQty() : MathUtil.ZERO))) : Collections.emptyMap();
        //销售单详情id集合
        List<String> detailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setTypeName(BillTypeEnum.getName(viewDTO.getType()));
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        viewDTO.setCustomerName(customerInfoEntity.getName());
        //平台：优先取新增/修改时已落库的dict_platform（已由BeanMapperUtils.copy(entity, viewDTO)带出）；老数据为空时按原有逻辑临时计算兜底
        String platform = viewDTO.getDictPlatform();
        if (CharSequenceUtil.isBlank(platform)) {
            platform = resolveDictPlatform(entity.getType(), Objects.nonNull(soB2cReturnEntity) ? soB2cReturnEntity.getDictPlatform() : null, customerInfoEntity.getPlatformType());
        }
        viewDTO.setDictPlatform(platform);
        viewDTO.setDictPlatformName(CharSequenceUtil.isNotBlank(platform) ? PlatformDictEnum.getNameByCode(platform) : null);
        if (CharSequenceUtil.isNotBlank(entity.getSoReturnCode())) {
            viewDTO.setSourceCode(entity.getSoReturnCode());
        }
        //退货单id
        List<SoReturnEntity> returnEntityList = soReturnFeign.listByIds(Collections.singletonList(viewDTO.getSoReturnId()));
        //销售单id
        List<String> soIds = returnEntityList.stream().map(SoReturnEntity::getSourceId).collect(Collectors.toList());
        if (Objects.nonNull(soB2cReturnEntity)) {
            soIds.add(soB2cReturnEntity.getSoId());
        }
        //汇率
        viewDTO.setExchangeRate(detailEntityList.get(0).getExchangeRate());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(soIds);
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(detailEntityList.stream().map(SoReturnInstockDetailEntity::getWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntitieList = soReturnReceiveDetailService.listDetailBySourceIds(Collections.singletonList(viewDTO.getSoReturnId()));
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
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream()
                    .filter(req -> req.getCode().equals(detailView.getWarehouseLocation()) && req.getWarehouseId().equals(detailView.getWarehouseId()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            detailView.setWarehouseLocationName(warehouseLocationEntity.getName());
            if (CharSequenceUtil.isBlank(detailView.getWarehouseId())) {
                detailView.setWarehouseId(viewDTO.getWarehouseId());
                detailView.setWarehouseName(viewDTO.getWarehouseName());
            }
            if (BillTypeEnum.B2C.getCode().equals(entity.getType())) {
                SoB2cReturnDetailEntity soB2cReturnDetailEntity = returnB2cDetailEntityList.stream().filter(v -> v.getId().equals(detailEntity.getSoReturnDetailId())).findFirst().orElse(new SoB2cReturnDetailEntity());
                detailView.setSalesQty(soB2cReturnDetailEntity.getSaleQty());
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSkuId().equals(detailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setDeliveryQty(actualQty);
                // 应退：优先售后明细退货数量；无售后关联时取落库 mustQty；仅 mustQty 为 null 时用签收数量兜底
                Integer returnQty = soB2cReturnDetailEntity.getReturnQty();
                boolean hasAuthoritativeReturnQty = returnQty != null;
                if (returnQty == null) {
                    returnQty = detailEntity.getMustQty();
                }
                if (returnQty == null && CharSequenceUtil.isBlank(detailEntity.getSoReturnDetailId())
                        && detailEntity.getReceiveQty() != null) {
                    returnQty = detailEntity.getReceiveQty();
                }
                // 剩余应退货数量 = 退货数量 - 累计入库（有售后按 soReturnDetailId；无售后按 sourceDetailId；皆空则本行实退）
                if (returnQty != null) {
                    int instockQty;
                    if (CharSequenceUtil.isNotBlank(detailEntity.getSoReturnDetailId())) {
                        instockQty = b2cInstockRealQtyMap.getOrDefault(detailEntity.getSoReturnDetailId(), MathUtil.ZERO);
                    } else if (CharSequenceUtil.isNotBlank(detailEntity.getSourceDetailId())) {
                        instockQty = b2cSourceDetailRealQtyMap.getOrDefault(detailEntity.getSourceDetailId(), MathUtil.ZERO);
                    } else {
                        instockQty = detailEntity.getRealQty() != null ? detailEntity.getRealQty() : MathUtil.ZERO;
                    }
                    // 无售后单权威应退数量时，历史数据可能落成偏小/为0的 mustQty；以累计入库为下限，避免展示负数剩余应退
                    if (!hasAuthoritativeReturnQty && returnQty < instockQty) {
                        returnQty = instockQty;
                    }
                    detailView.setRemainShouldQty(returnQty - instockQty);
                }
                detailView.setMustQty(returnQty);
                Integer receiveQty = soReturnReceiveDetailEntitieList.stream().filter(req -> detailEntity.getSourceDetailId().equals(req.getId()) && req.getSkuId().equals(detailEntity.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setReceiveQty(receiveQty);
                detailView.setReturnTypeDictName(ReturnTypeEnum.getName(detailEntity.getReturnTypeDict()));
                detailView.setReturnReasonDictName(ReturnReasonEnum.getName(detailEntity.getReturnReasonDict()));
            } else {
                SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(detail -> detail.getId().equals(detailEntity.getSoReturnDetailId())).findFirst().orElse(new SoReturnDetailEntity());
                if (CharSequenceUtil.isNotBlank(viewDTO.getSoReturnId())) {
                    //销售单信息
                    SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                    detailView.setSalesQty(soDetailEntity.getQty());
                    Integer actualQty = soOutstockDetailEntities.stream()
                            .filter(detail -> detail.getSoId().equals(soDetailEntity.getMainId()) && detail.getSkuId().equals(detailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()))
                            .map(item -> item.getActualQty() * soDetailEntity.getPerBoxQty())
                            .reduce(MathUtil.ZERO, Integer::sum);
                    detailView.setDeliveryQty(actualQty);

                    //获取退货数量
                    Integer returnQty = returnDetailEntityList.stream().filter(req -> req.getId().equals(detailEntity.getSoReturnDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    detailView.setMustQty(returnQty);
                    Integer receiveQty = soReturnReceiveDetailEntitieList.stream().filter(req -> detailEntity.getSoReturnDetailId().equals(req.getSourceDetailId()) && req.getSkuId().equals(detailEntity.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    detailView.setReceiveQty(receiveQty);
                } else {
                    Integer receiveQty = soReturnReceiveDetailEntitieList.stream().filter(req -> detailEntity.getSourceDetailId().equals(req.getId()) && req.getSkuId().equals(detailEntity.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    detailView.setReceiveQty(receiveQty);
                }

                if (CharSequenceUtil.isNotBlank(detailEntity.getReturnTypeDict())) {
                    detailView.setReturnTypeDictName(ReturnTypeEnum.getName(detailEntity.getReturnTypeDict()));
                } else {
                    if (CharSequenceUtil.isNotBlank(soReturnDetailEntity.getReturnTypeDict())) {
                        detailView.setReturnTypeDictName(ReturnTypeEnum.getName(soReturnDetailEntity.getReturnTypeDict()));
                    }
                }
                if (CharSequenceUtil.isNotBlank(detailEntity.getReturnReasonDict())) {
                    detailView.setReturnReasonDictName(ReturnReasonEnum.getName(detailEntity.getReturnReasonDict()));
                } else {
                    if (CharSequenceUtil.isNotBlank(soReturnDetailEntity.getReturnReasonDict())) {
                        detailView.setReturnReasonDictName(ReturnReasonEnum.getName(soReturnDetailEntity.getReturnReasonDict()));
                    }
                }
            }
            viewDTO.setExchangeRate(detailEntity.getExchangeRate());
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(SoReturnInstockEntity entity, Boolean isNeedProcess) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }

        //未作废、待提交、审核不通过才可以提交
        if (Boolean.TRUE.equals(entity.getInvalidStatus()) || (!entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                && !entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        if (isNeedProcess) {
            startProcess(entity);
        }

        //操作日志
        operateLogService.addModuleOperateLog(String.format("提交了一个销售退货通知单【%s】", entity.getCode()), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .set(SoReturnInstockEntity::getApproveUserId, "")
                .set(SoReturnInstockEntity::getApproveUserName, "")
                .set(SoReturnInstockEntity::getApproveTime, null)
                .eq(SoReturnInstockEntity::getId, entity.getId())
                .update();
        pushThirdWarehouse(Collections.singletonList(entity.getId()), Collections.singletonList(entity));
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Author will
     * @Date 2025/10/22 12:07
     **/

    public void startProcess(SoReturnInstockEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * variablesMap值赋值
     *
     * @param entity
     * @return Map<String, Object>
     * @author will
     * @date 2025/10/22 10:51
     */
    private Map<String, Object> getVariablesMap(SoReturnInstockEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<SoReturnInstockDetailEntity> detailList = soReturnInstockDetailService.lambdaQuery().eq(SoReturnInstockDetailEntity::getMainId, entity.getId()).list();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_RETURN_INBOUND_DETAIL_REQUIRED);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList, Map.class));
        return variablesMap;
    }

    private void pushThirdWarehouse(List<String> ids, List<SoReturnInstockEntity> entityList) {
        //如果是速卖通菜鸟仓，推送至第三方仓
        List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainIds(ids);
        List<String> warehouseIds = detailEntityList.stream().map(SoReturnInstockDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasProviderWarehouseDTOList = overseasProviderWarehouseService.listByWarehouseIdList(warehouseIds);
        for (SoReturnInstockEntity soReturnInstockEntity : entityList) {
            SoReturnInstockDetailEntity soReturnInstockDetailEntity = detailEntityList.stream()
                    .filter(detail -> detail.getMainId().equals(soReturnInstockEntity.getId()))
                    .findFirst().orElse(null);
            if (Objects.isNull(soReturnInstockDetailEntity) || StringUtils.isBlank(soReturnInstockDetailEntity.getWarehouseId())) {
                continue;
            }
            OverseasProviderWarehouseDTO.ViewDTO viewDTO = overseasProviderWarehouseDTOList.stream().filter(
                    overseasProviderWarehouseDTO -> overseasProviderWarehouseDTO.getWarehouseId().equals(soReturnInstockDetailEntity.getWarehouseId())
            ).findFirst().orElse(null);
            if (Objects.isNull(viewDTO)) {
                continue;
            }
            if (viewDTO.getProviderCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())) {
                WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
                wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.CAINIAO.getCode());
                wmsPushMsgEntity.setSourceType(SourceTypeEnum.CAINIAO_SO_RETURN_INSTOCK.getCode());
                wmsPushMsgEntity.setSourceId(soReturnInstockEntity.getId());
                wmsPushMsgEntity.setSourceCode(soReturnInstockEntity.getCode());
                wmsPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
                wmsPushMsgEntity.setPushData(JSON.toJSONString(DmpOutputConstant.getQuerySyncMap()));

                wmsPushMsgService.save(wmsPushMsgEntity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SoReturnInstockDTO.Add dto) {
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.BILL_SAVE_FAILED);
        }
        SoReturnInstockEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PO_RETURN_INBOUND_NOT_FOUND);
        }
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(SoReturnInstockDTO.Update dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }
        SoReturnInstockEntity entity = this.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PO_RETURN_INBOUND_NOT_FOUND);
        }
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoReturnInstockEntity entity, String type, String comment, Boolean isNeedProcess) {
        //判断是否是审核中的状态
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY.getMsg());
        }
        //如果是菜鸟仓的情况，不能手动审核
        List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainId(entity.getId());
        List<String> warehouseIds = detailEntityList.stream().map(SoReturnInstockDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasProviderWarehouseDTOList = overseasProviderWarehouseService.listByWarehouseIdList(warehouseIds);
        if (CollectionUtils.isNotEmpty(overseasProviderWarehouseDTOList)) {
            OverseasProviderWarehouseDTO.ViewDTO viewDTO = overseasProviderWarehouseDTOList.get(0);
            if (Objects.nonNull(viewDTO) && OmsPlatformEnum.CAI_NIAO.getCode().equals(viewDTO.getProviderCode())) {
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), "菜鸟仓退货入库单不允许手动审核");
            }
        }
        //调用审核流程
        approveProcess(entity, new ApproveOneDTO(entity.getId(), type, comment));

        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个销售退货通知单【%s】", ApproveTypeEnum.getName(type), entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 审核流程调用
     *
     * @param entity
     * @param dto
     * @return void
     * @author will
     * @date 2025/10/22 16:23
     */
    private void approveProcess(SoReturnInstockEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> listApiResult = workflowFeign.approve(approveDTO);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
        }
        ProcessManagementDTO.ApproveResultDTO data = listApiResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public Boolean approveEnd(ApproveOneDTO dto, SoReturnInstockEntity entity) {

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //意见
        if (ApproveTypeEnum.PASS.getStatus().equals(dto.getType())) {
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

            //推送数帝云
            this.syncToSdyHandler(Arrays.asList(entity), SyncOperateEnum.OPERATE_APPROVE.getCode());

        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .set(SoReturnInstockEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnInstockEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnInstockEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoReturnInstockEntity::getId, entity.getId())
                    .update();
        }
        return Boolean.TRUE;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoReturnInstockEntity entity, Boolean isPushKingDee) {
        //已审核支持反审核
        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY.getMsg());
        }
        // 取消三方仓退货入库单不允许反审核
//        if(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode().equals(entity.getSourceType()) && isPushKingDee){
//            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"三方仓退货入库单不允许反审核");
//        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .set(SoReturnInstockEntity::getApproveUserId, "")
                .set(SoReturnInstockEntity::getApproveUserName, "")
                .set(SoReturnInstockEntity::getApproveTime, null)
                .eq(SoReturnInstockEntity::getId, entity.getId())
                .update();
        //回滚库存
        InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO(InventorySourceTypeEnum.SO_RETURN_INSTOCK, entity.getId());
        inventoryTransCoreService.unApprove(inventoryUnApproveDTO);
        //反审核发送金蝶
        if (isPushKingDee) {
            //发送金蝶
            sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        this.syncToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);

        if (isPushKingDee) {
            //推送数帝云
            this.syncToSdyHandler(Arrays.asList(entity), SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }

        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个销售退货通知单【%s】", entity.getCode()), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto) {
        List<String> ids = dto.getIds();
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        //审核中可以撤销
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .set(SoReturnInstockEntity::getApproveUserId, "")
                .set(SoReturnInstockEntity::getApproveUserName, "")
                .set(SoReturnInstockEntity::getApproveTime, null)
                .in(SoReturnInstockEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】撤销流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "撤销流程操作");

        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<SoReturnInstockEntity> approveList = lambdaQuery().in(SoReturnInstockEntity::getId, ids).eq(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus()).list();
        if (CollUtil.isNotEmpty(approveList)) {
            approveList.forEach(entity -> this.disApprove(entity, false));
        }

        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        //审核不通过 待提交可以作废
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.BILL_VOID_ALLOWED_STATUS_ONLY);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnInstockEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnInstockEntity::getInvalidRemark, remark)
                .in(SoReturnInstockEntity::getId, ids)
                .update();

        //发送金蝶
        sendPushTask(entityList, SyncOperateEnum.OPERATE_INVALID.getCode());

        //推送数帝云
        this.syncToSdyHandler(entityList, SyncOperateEnum.OPERATE_INVALID.getCode());

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoReturnInstockEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        //待提交支持删除
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.BILL_DELETE_ALLOWED_STATUS_ONLY);
        }

        //获取需要推送数帝云的数据
        List<SoReturnInstockDetailEntity> detailAllList = new ArrayList<>();
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = soReturnInstockDetailService.listDetailByMainIds(ids);
        detailAllList.addAll(soReturnInstockDetailEntityList);

        //发送金蝶
        sendPushTask(entityList, SyncOperateEnum.OPERATE_DELETE.getCode());

        //推送数帝云
        this.syncToSdyHandler(entityList, SyncOperateEnum.OPERATE_DELETE.getCode());

        //删除详情表
        soReturnInstockDetailService.delete(ids);
        //删除主表
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO deleteEntity(SoReturnInstockEntity entity) {
        //待提交支持删除
        if (entity.getInvalidStatus() || !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_DELETE_ALLOWED_STATUS_ONLY);
        }
        List<String> ids = Collections.singletonList(entity.getId());

        //获取需要推送数帝云的数据
        List<SoReturnInstockDetailEntity> detailAllList = new ArrayList<>();
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = soReturnInstockDetailService.listDetailByMainIds(ids);
        detailAllList.addAll(soReturnInstockDetailEntityList);

        //发送金蝶
        sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());

        //推送数帝云
        this.syncToSdyHandler(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());

        //删除详情表
        soReturnInstockDetailService.delete(ids);
        //删除主表
        boolean result = this.removeByIds(ids);
        if (result) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "删除失败");
        }
    }

    @Override
    public Boolean exportExcel(SoReturnInstockDTO.PagingParam dto) {
        downloadTaskFeign.saveDownloadTask("销售退货入库单", EXPORT_WMS_SO_RETURN_IN_STOCK.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean qcGenerateSoReturnInstockSave(List<SoReturnInstockDTO.GenerateSoReturnInstockView> list) {
        Boolean flag = Boolean.FALSE;
        List<String> soReceiveIdList = list.stream().map(SoReturnInstockDTO.GenerateSoReturnInstockView::getMainId).distinct().collect(Collectors.toList());
        long count = soReturnReceiveDetailService.listByIds(soReceiveIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.SO_DELIVERY_RETURN_SIGN_APPROVED_REQUIRED_PUSH_INBOUND);
        }

        for (String id : soReceiveIdList) {
            List<SoReturnInstockDTO.GenerateSoReturnInstockView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            QcInfoEntity qcInfoEntity = qcInfoService.getById(id);
            SoReturnReceiveEntity receiveEntity = soReturnReceiveService.getById(qcInfoEntity.getSourceId());
            SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailService.getById(qcInfoEntity.getSourceDetailId());
            SoReturnInstockDTO.Add dto = new SoReturnInstockDTO.Add();
            /*if (CharSequenceUtil.isBlank(receiveEntity.getSourceId())) {
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
                if (CharSequenceUtil.isBlank(receiveEntity.getSourceId())) {
                    detailAddDTO.setSourceDetailId(view.getSourceDetailId());
                } else {
                    detailAddDTO.setSourceDetailId(view.getSourceDetailId());
                    detailAddDTO.setSoReturnDetailId(soReturnReceiveDetailEntity.getSourceDetailId());
                }
                detailAddDTO.setExchangeRate(Objects.nonNull(view.getExchangeRate()) ? view.getExchangeRate() : soReturnReceiveDetailEntity.getExchangeRate());
                if (Objects.equals(soReturnReceiveDetailEntity.getReceiveQty(), view.getRealQty())) {
                    detailAddDTO.setReturnAmount(soReturnReceiveDetailEntity.getReturnAmount());
                    detailAddDTO.setTaxReturnAmount(soReturnReceiveDetailEntity.getTaxReturnAmount());
                    detailAddDTO.setReturnAmountLocalCurrency(soReturnReceiveDetailEntity.getReturnAmountLocalCurrency());
                    detailAddDTO.setTaxReturnAmountLocalCurrency(soReturnReceiveDetailEntity.getTaxReturnAmountLocalCurrency());
                } else {
                    detailAddDTO.setReturnAmount(soReturnNoticeService.calReturnAmount(soReturnReceiveDetailEntity.getReturnAmount(), soReturnReceiveDetailEntity.getReceiveQty(), view.getRealQty()));
                    detailAddDTO.setTaxReturnAmount(soReturnNoticeService.calReturnAmount(soReturnReceiveDetailEntity.getTaxReturnAmount(), soReturnReceiveDetailEntity.getReceiveQty(), view.getRealQty()));
                    detailAddDTO.setReturnAmountLocalCurrency(soReturnNoticeService.calLocalCurrency(soReturnReceiveDetailEntity.getExchangeRate(), detailAddDTO.getReturnAmount()));
                    detailAddDTO.setTaxReturnAmountLocalCurrency(soReturnNoticeService.calLocalCurrency(soReturnReceiveDetailEntity.getExchangeRate(), detailAddDTO.getTaxReturnAmount()));
                }
                detailList.add(detailAddDTO);
            }
            dto.setDetailList(detailList);
            String noticeId = this.add(dto);
            if (CharSequenceUtil.isNotBlank(noticeId)) {
                flag = Boolean.TRUE;
            }
        }
        return flag;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean receiveGenerateSoReturnInstockSave(List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> list) {
        Boolean flag = Boolean.FALSE;
        List<String> soReceiveIdList = list.stream().map(SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView::getMainId).distinct().collect(Collectors.toList());
        long count = soReturnReceiveService.listByIds(soReceiveIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.SO_DELIVERY_RETURN_SIGN_APPROVED_REQUIRED_PUSH_INBOUND);
        }
        for (String id : soReceiveIdList) {
            List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            SoReturnReceiveEntity soReturnReceiveEntity = soReturnReceiveService.getById(id);
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByMainId(id);
            SoReturnInstockDTO.Add dto = new SoReturnInstockDTO.Add();
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
            dto.setReturnLogisticCode(viewList.get(0).getReturnLogisticCode());

            List<SoReturnInstockDetailDTO.Add> detailList = new ArrayList<>();
            for (SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView view : viewList) {
                SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailEntities.stream().filter(v -> v.getId().equals(view.getId())).findFirst().orElse(null);
                dto.setBillDate(view.getInstockDate());
                SoReturnInstockDetailDTO.Add detailAddDTO = new SoReturnInstockDetailDTO.Add();
                detailAddDTO.setSkuId(view.getSkuId());
                detailAddDTO.setSkuNo(view.getSkuNo());
                detailAddDTO.setReceiveQty(view.getReceiveQty());
                detailAddDTO.setRealQty(view.getRealQty());
                detailAddDTO.setWarehouseLocation(view.getWarehouseLocation());
                detailAddDTO.setRemark(view.getRemark());
                detailAddDTO.setWarehouseId(soReturnReceiveEntity.getWarehouseId());
                detailAddDTO.setSourceDetailId(view.getId());
                if (CharSequenceUtil.isNotBlank(soReturnReceiveEntity.getSourceId())) {
                    detailAddDTO.setSoReturnDetailId(view.getSourceDetailId());
                }
                detailAddDTO.setReturnTypeDict(view.getReturnTypeDict());
                detailAddDTO.setReturnReasonDict(view.getReturnReasonDict());
                detailAddDTO.setIsChildSkuNo(view.getIsChildSkuNo());
                detailAddDTO.setExchangeRate(soReturnReceiveDetailEntity.getExchangeRate());
                if (Objects.equals(soReturnReceiveDetailEntity.getReceiveQty(), view.getRealQty())) {
                    detailAddDTO.setReturnAmount(soReturnReceiveDetailEntity.getReturnAmount());
                    detailAddDTO.setTaxReturnAmount(soReturnReceiveDetailEntity.getTaxReturnAmount());
                    detailAddDTO.setReturnAmountLocalCurrency(soReturnReceiveDetailEntity.getReturnAmountLocalCurrency());
                    detailAddDTO.setTaxReturnAmountLocalCurrency(soReturnReceiveDetailEntity.getTaxReturnAmountLocalCurrency());
                } else {
                    detailAddDTO.setReturnAmount(soReturnNoticeService.calReturnAmount(soReturnReceiveDetailEntity.getReturnAmount(), soReturnReceiveDetailEntity.getReceiveQty(), view.getRealQty()));
                    detailAddDTO.setTaxReturnAmount(soReturnNoticeService.calReturnAmount(soReturnReceiveDetailEntity.getTaxReturnAmount(), soReturnReceiveDetailEntity.getReceiveQty(), view.getRealQty()));
                    detailAddDTO.setReturnAmountLocalCurrency(soReturnNoticeService.calLocalCurrency(soReturnReceiveDetailEntity.getExchangeRate(), detailAddDTO.getReturnAmount()));
                    detailAddDTO.setTaxReturnAmountLocalCurrency(soReturnNoticeService.calLocalCurrency(soReturnReceiveDetailEntity.getExchangeRate(), detailAddDTO.getTaxReturnAmount()));
                }
                dto.setCurrency(soReturnReceiveEntity.getCurrency());
                dto.setCurrencySymbol(soReturnReceiveEntity.getCurrencySymbol());
                dto.setCustomerId(view.getCustomerId());
                detailList.add(detailAddDTO);
            }
            dto.setDetailList(detailList);
            String noticeId = this.add(dto);
            if (CharSequenceUtil.isNotBlank(noticeId)) {
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
                if (Boolean.TRUE.equals(detailEntity.getDefectiveProductFlag())) {
                    inOutStockDTO.setInventoryStatus(InventoryStatusEnum.DEFECTIVE_PRODUCT);
                }
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
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), SoReturnInstockEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货入库单【%s】", instockEntity.getCode()), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), instockEntity.getId(), "新增操作");
        return soReturnInstockDetailService.saveBatch(detailEntityList);
    }

    @Override
    public PagingVO<SoReturnInstockDTO.PdaPagingView> PdaPaging(PagingDTO<SoReturnInstockDTO.PdaPagingParam> pagingParamDTO) {
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
        params.setPermissionSql(getPermissionSql(pagingParamDTO.getPermissionSql()));
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
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<SoReturnInstockDTO.PdaSoReturnInstockCountDTO> list = new ArrayList<>();
        String permissionSql = getPermissionSql(dto.getPermissionSql());
        SoReturnInstockDTO.PagingParam pagingParamDTO = new SoReturnInstockDTO.PagingParam();
        pagingParamDTO.setPermissionSql(permissionSql);
        pagingParamDTO.setInvalidStatus(Boolean.FALSE);
        List<ApproveStatusQtyDTO> tabList = this.baseMapper.listCount(pagingParamDTO);
        Map<String, Integer> tabMap = tabList.stream().collect(Collectors.toMap(ApproveStatusQtyDTO::getApproveStatus, ApproveStatusQtyDTO::getCount));
        for (PdaTabFlagEnum item : values) {
            SoReturnInstockDTO.PdaSoReturnInstockCountDTO resultDTO = new SoReturnInstockDTO.PdaSoReturnInstockCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = tabMap.get(ApproveStatusEnum.WAIT_SUBMIT.getStatus()) + tabMap.get(ApproveStatusEnum.REJECT.getStatus());
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = tabMap.get(ApproveStatusEnum.APPROVE_ING.getStatus());
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                count = tabMap.get(ApproveStatusEnum.APPROVE.getStatus());
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String pdaAdd(SoReturnInstockDTO.Add dto) {
        if (CharSequenceUtil.isNotBlank(dto.getSoReturnId())) {
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
        if (CharSequenceUtil.isNotBlank(dto.getSoReturnId())) {
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
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<SoReturnInstockDetailEntity> viewList = list.stream().filter(obj -> obj.getIsSubContract()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(viewList)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<String> mainIds = viewList.stream().map(SoReturnInstockDetailEntity::getMainId).collect(Collectors.toList());
        List<SoReturnInstockEntity> mainList = this.listByIds(mainIds);
        if (CollectionUtils.isEmpty(mainList)) {
            throw new ServiceException(ApiError.PO_RETURN_INBOUND_NOT_FOUND);
        }

        List<String> skuIds = viewList.stream().map(SoReturnInstockDetailEntity::getSkuId).collect(Collectors.toList());
        //bom信息
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.BOM_NOT_FOUND);
        }
        //sku信息
        List<String> allSkuIdList = bomList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(allSkuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
        }
        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = viewList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(paramList);

        //根据sku、仓库、仓位合并显示
        List<SoReturnInstockDTO.ViewGenerateMachineInfoDTO> resultList = new ArrayList<>();
        for (SoReturnInstockEntity entity : mainList) {
            List<SoReturnInstockDetailEntity> detailEntityList = viewList.stream().filter(v -> v.getMainId().equals(entity.getId())).collect(Collectors.toList());
            Map<String, List<SoReturnInstockDetailEntity>> detailMap = detailEntityList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId() == null ? "" : obj.getWarehouseId()).concat(obj.getWarehouseLocation())));

            for (Map.Entry<String, List<SoReturnInstockDetailEntity>> entry : detailMap.entrySet()) {
                SoReturnInstockDetailEntity detailEntity = entry.getValue().get(0);
                SoReturnInstockDTO.ViewGenerateMachineInfoDTO viewDTO = new SoReturnInstockDTO.ViewGenerateMachineInfoDTO();

                if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
                    throw new ServiceException(ApiError.SO_RETURN_INSTOCK_NOT_GENERATE, entity.getCode());
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
                    throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
                }
                viewDTO.setProductName(skuVO.getSkuName());
                viewDTO.setVariantProperty(skuVO.getVariantProperty());

                //即时库存
                Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getWarehouseId(), viewDTO.getSkuId(), viewDTO.getWarehouseLocation());

                List<BomChildrenSkuDTO> childList = bomList.stream().filter(obj -> obj.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childList)) {
                    throw new ServiceException(ApiError.BOM_CHILD_NOT_FOUND);
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
                        throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
                    }
                    SoReturnInstockDTO.ViewGenerateMachineInfoDTO viewChildDTO = new SoReturnInstockDTO.ViewGenerateMachineInfoDTO();
                    BeanMapperUtils.copy(viewDTO, viewChildDTO);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean generateMachineInfo(ValidList<SoReturnInstockDTO.GenerateMachineInfoDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }

        /**
         * 1、一个单据生成一个加工单
         * 2、同仓库、sku、仓位生成一个加工单明细
         */
        List<String> skuIds = list.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getChildSkuId())).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
        }

        //bom信息
        List<BomChildrenSkuDTO> bomList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.BOM_NOT_FOUND);
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
                    throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
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
                        throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
                    }
                    //BOM信息
                    BomChildrenSkuDTO bomChildrenSkuDTO = bomList.stream().filter(obj -> obj.getParentSkuId().equals(subComponentsDTO.getSkuId())
                                    && obj.getSkuId().equals(subComponentsDTO.getChildSkuId())
                                    && obj.getBomVersion().equals(subComponentsDTO.getBomVersion()))
                            .findFirst().orElse(null);

                    if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                        throw new ServiceException(ApiError.BOM_CHILD_NOT_FOUND);
                    }
                    addSubComponentsDTO.setSkuId(subComponentsDTO.getChildSkuId());
                    addSubComponentsDTO.setSkuNo(child.getSkuNo());
                    addSubComponentsDTO.setWarehouseId(subComponentsDTO.getWarehouseId());
                    addSubComponentsDTO.setWarehouseLocation(subComponentsDTO.getWarehouseLocation());
                    addSubComponentsDTO.setQty(addDetailDTO.getQty() * bomChildrenSkuDTO.getQuantity());
                    //子SKU处理
                    MachineSubComponentsDTO.HandleDetailDTO handleDetailDTO = new MachineSubComponentsDTO.HandleDetailDTO();
                    BeanMapperUtils.copy(subComponentsDTO, handleDetailDTO);
                    addSubComponentsDTO.setHandleType(subComponentsDTO.getHandleType());
                    addSubComponentsDTO.setHandleDetail(JSONUtil.toJsonStr(handleDetailDTO));
                    subComponentsList.add(addSubComponentsDTO);
                }
                addDetailDTO.setSubComponentsList(subComponentsList);
                addDetailList.add(addDetailDTO);
            }
            addDTO.setDetailList(addDetailList);
            MachineInfoEntity entity = machineInfoService.add(addDTO);
            ids.add(entity.getId());
        }

        //自动生成功能系统标识
        Boolean originalValue = UserContext.getIsUserSystem();
        UserContext.setIsUserSystem(Boolean.TRUE);

        //自动提交
        Boolean submit = machineInfoService.submit(ids);
        if (!submit) {
            throw new ServiceException(ApiError.BILL_SUBMIT_FAILED, SourceTypeEnum.SO_RETURN_INSTOCK.getName());
        }
        //自动审核
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MachineInfoEntity> entityList = machineInfoService.listByIds(ids);
        for (String id : ids) {
            MachineInfoEntity entity = entityList.stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.approve(entity, ApproveType.PASS, "", null));
            } catch (Exception e) {
                log.error("加工单审核失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        //自动生成直接调拨单或者采购退货单
        generateSubordinateOrder(ids);

        //恢复系统标识
        UserContext.setIsUserSystem(originalValue);
        return Boolean.TRUE;
    }

    /**
     * @param ids
     * @description: 生成下级单据
     * @author Will
     * @date: 2023/8/28 16:50
     */
    private void generateSubordinateOrder(List<String> ids) {

        List<MachineInfoEntity> list = machineInfoService.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.WH_SUBCONTRACT_PROCESS_ORDER_NOT_FOUND);
        }
        List<MachineDetailEntity> detailList = machineDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.WH_SUBCONTRACT_PROCESS_ORDER_DETAIL_NOT_FOUND);
        }
        List<String> detailIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> subComponentsList = machineSubComponentsService.listByDetailIds(detailIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.WH_SUBCONTRACT_PROCESS_ORDER_CHILD_DETAIL_NOT_FOUND);
        }
        for (MachineInfoEntity entity : list) {
            //明细
            List<MachineDetailEntity> detailEntityList = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.WH_SUBCONTRACT_PROCESS_ORDER_DETAIL_NOT_FOUND);
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
                    generatePoReturnOrder(entity, value);


                }
                if (MachineHandleTypeEnum.MOVE_WAREHOUSE.getCode().equals(handleType)) {
                    //移仓，同调入、调出库存组织生成直接调拨单
                    generateTransferInfo(entity, value);
                }
            }
        }

    }

    /**
     * @param entity
     * @param list
     * @description: 生成直接调拨单
     * @author Will
     * @date: 2023/8/28 17:36
     */
    private void generateTransferInfo(MachineInfoEntity entity, List<MachineSubComponentsEntity> list) {
        /**
         * 移仓，同调入、调出库存组织生成直接调拨单
         */
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> warehouseIds = list.stream().map(obj -> JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseIds)) {
            throw new ServiceException(ApiError.WH_PARAM_NOT_FOUND);
        }
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.WH_PARAM_NOT_FOUND);
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
            addDTO.setType(CharSequenceUtil.equals(addDTO.getInOrgId(), addDTO.getOutOrgId()) ? TransferTypeEnum.IN_ORG.getCode() : TransferTypeEnum.CROSS_ORG.getCode());
            addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
            addDTO.setRemark(CharSequenceUtil.format("加工单（拆卸）【{}】自动生成直接调拨单", entity.getCode()));
            //sku、仓库、仓位分组
            Map<String, List<MachineSubComponentsEntity>> childMap = value.stream()
                    .collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId().concat(CharSequenceUtil.isNotBlank(obj.getWarehouseLocation()) ? obj.getWarehouseLocation() : "")
                            .concat(CharSequenceUtil.isNotBlank(JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseLocation()) ? JSONUtil.toBean(obj.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class).getChildWarehouseLocation() : ""))));

            List<TransferInfoDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (Map.Entry<String, List<MachineSubComponentsEntity>> childEntry : childMap.entrySet()) {
                List<MachineSubComponentsEntity> childValue = childEntry.getValue();
                MachineSubComponentsEntity subComponentsEntity = childValue.get(0);
                //子件处理详情
                MachineSubComponentsDTO.HandleDetailDTO subHandleDetailDTO = JSONUtil.toBean(subComponentsEntity.getHandleDetail(), MachineSubComponentsDTO.HandleDetailDTO.class);
                TransferInfoDetailDTO.AddDTO addDetailDTO = new TransferInfoDetailDTO.AddDTO();
                BeanMapperUtils.copy(subComponentsEntity, addDetailDTO);
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
            List<TransferInfoEntity> transferInfoList = transferInfoService.listByIds(ids);
            if (CollUtil.isNotEmpty(transferInfoList)) {
                //提交
                transferInfoList.forEach(obj -> transferInfoService.submit(obj, Boolean.TRUE));
            }
        }
    }

    /**
     * @param entity
     * @param list
     * @description: 生成采购退货单
     * @author Will
     * @date: 2023/8/28 17:36
     */
    private void generatePoReturnOrder(MachineInfoEntity entity, List<MachineSubComponentsEntity> list) {
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
            addDTO.setReturnRemark(CharSequenceUtil.format("加工单（拆卸）【{}】自动生成采购退货单", entity.getCode()));
            Map<String, List<MachineSubComponentsEntity>> childMap = value.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(CharSequenceUtil.isNotBlank(obj.getWarehouseLocation()) ? obj.getWarehouseLocation() : "")));
            List<PurchaseReturnOrderDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (Map.Entry<String, List<MachineSubComponentsEntity>> childEntry : childMap.entrySet()) {
                List<MachineSubComponentsEntity> childValue = childEntry.getValue();
                MachineSubComponentsEntity subComponentsEntity = childValue.get(0);
                PurchaseReturnOrderDetailDTO.AddDTO addDetailDTO = new PurchaseReturnOrderDetailDTO.AddDTO();
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
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.BILL_SAVE_FAILED);
        }
        SoReturnInstockEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PO_RETURN_INBOUND_NOT_FOUND);
        }
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pdaUpdateAndSubmit(SoReturnInstockDTO.Update dto) {
        Boolean update = this.pdaUpdate(dto);
        if (!update) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }
        SoReturnInstockEntity entity = this.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PO_RETURN_INBOUND_NOT_FOUND);
        }
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    public PagingVO<SoReturnInstockDTO.SearchDTO> pagingSelect(PagingDTO<SoReturnInstockDTO.SelectDTO> searchDTO) {
        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        SoReturnInstockDTO.SelectDTO params = searchDTO.getParams();
        IPage<SoReturnInstockDTO.SearchDTO> pagResult = baseMapper.b2cPagingSelect(query, params);
        return new PagingVO<>(pagResult);
    }

    @Override
    public SoReturnInstockEntity getByThirdCode(String thirdCode) {
        if (CharSequenceUtil.isBlank(thirdCode)) {
            return null;
        }
        return lambdaQuery().eq(SoReturnInstockEntity::getThirdCode, thirdCode).last("limit 1").one();
    }

    @Override
    public List<SoReturnInstockEntity> listByThirdCode(String thirdCode) {
        if (CharSequenceUtil.isBlank(thirdCode)) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SoReturnInstockEntity::getThirdCode, thirdCode).list();
    }

    @Override
    public SoReturnInstockEntity getBySourceId(String sourceId) {
        if (CharSequenceUtil.isBlank(sourceId)) {
            return null;
        }
        return lambdaQuery().eq(SoReturnInstockEntity::getSourceId, sourceId).last("limit 1").one();
    }

    @Override
    public void addByThirdWarehouse(SoReturnInstockEntity soReturnInstockEntity, List<SoReturnInstockDetailEntity> detailEntityList) {
        fillThirdWarehouseDetailPrice(soReturnInstockEntity, detailEntityList);
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> persistByThirdWarehouse(soReturnInstockEntity, detailEntityList));
    }

    /**
     * 三方仓入库持久化，须通过 {@link #addByThirdWarehouse} 调用，不可绕过价格补全直接调用。
     */
    private void persistByThirdWarehouse(SoReturnInstockEntity soReturnInstockEntity, List<SoReturnInstockDetailEntity> detailEntityList) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSTH);
        soReturnInstockEntity.setCode(code);
        this.save(soReturnInstockEntity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("三方仓新增销售退货入库单【%s】", code), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), soReturnInstockEntity.getId(), "新增操作");

        detailEntityList.forEach(v -> v.setMainId(soReturnInstockEntity.getId()));
        soReturnInstockDetailService.saveBatch(detailEntityList);
        // 无客户信息不审核通过
        if (StringUtils.isBlank(soReturnInstockEntity.getCustomerId())) {
            operateLogService.addModuleOperateLog(String.format("三方仓销售退货入库单【%s】无客户信息不自动审核通过", code), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), soReturnInstockEntity.getId(), "审核操作");
            return;
        }
        //没有仓库不审核
        if (detailEntityList.stream().anyMatch(v -> StringUtils.isBlank(v.getWarehouseId()))) {
            return;
        }
        //审核
        this.approve(soReturnInstockEntity, ApproveTypeEnum.PASS.getStatus(), "三方仓新增自动审核通过", false);
    }

    /**
     * 三方仓拉取退货入库时，平台侧通常不带单价/汇率，需关联销售订单明细补全。
     * 含 Feign 远程调用，须在 {@link #addByThirdWarehouse} 事务开启前执行。
     */
    private void fillThirdWarehouseDetailPrice(SoReturnInstockEntity main, List<SoReturnInstockDetailEntity> detailEntityList) {
        if (CollectionUtils.isEmpty(detailEntityList) || !OrderTypeEnum.B2C.getCode().equals(main.getType())
                || CharSequenceUtil.isBlank(main.getSoId())) {
            return;
        }
        List<SoB2cDetailEntity> soB2cDetailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(main.getSoId()));
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            return;
        }
        Map<String, SoB2cReturnDetailDTO.ViewDTO> returnDetailMap = loadSoB2cReturnDetailMap(main.getSoReturnId());
        for (SoReturnInstockDetailEntity detail : detailEntityList) {
            if (Objects.nonNull(detail.getPrice()) && detail.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                continue;
            }
            SoB2cDetailEntity b2cDetail = resolveSoB2cDetail(detail, soB2cDetailList, returnDetailMap);
            if (Objects.isNull(b2cDetail)) {
                continue;
            }
            fillDetailPrice(detail, b2cDetail.getPrice(), BigDecimal.ZERO, b2cDetail.getPrice(), b2cDetail.getExchangeRate());
        }
        if (CharSequenceUtil.isNotBlank(main.getCurrency()) && CharSequenceUtil.isBlank(main.getCurrencySymbol())) {
            main.setCurrencySymbol(CurrencyEnum.getSymbolByCode(main.getCurrency()));
        }
    }

    private Map<String, SoB2cReturnDetailDTO.ViewDTO> loadSoB2cReturnDetailMap(String soReturnId) {
        if (CharSequenceUtil.isBlank(soReturnId)) {
            return Collections.emptyMap();
        }
        List<SoB2cReturnDetailDTO.ViewDTO> returnDetailList = soB2cReturnFeign.listDetailByMainIds(Collections.singletonList(soReturnId));
        if (CollectionUtils.isEmpty(returnDetailList)) {
            return Collections.emptyMap();
        }
        return returnDetailList.stream()
                .collect(Collectors.toMap(SoB2cReturnDetailDTO.ViewDTO::getId, Function.identity(), (left, right) -> left));
    }

    private SoB2cDetailEntity resolveSoB2cDetail(SoReturnInstockDetailEntity detail, List<SoB2cDetailEntity> soB2cDetailList,
                                                 Map<String, SoB2cReturnDetailDTO.ViewDTO> returnDetailMap) {
        if (CharSequenceUtil.isNotBlank(detail.getSoReturnDetailId())) {
            SoB2cReturnDetailDTO.ViewDTO returnDetail = returnDetailMap.get(detail.getSoReturnDetailId());
            if (Objects.nonNull(returnDetail) && CharSequenceUtil.isNotBlank(returnDetail.getSoDetailId())) {
                SoB2cDetailEntity matched = soB2cDetailList.stream()
                        .filter(item -> CharSequenceUtil.equals(item.getId(), returnDetail.getSoDetailId()))
                        .findFirst()
                        .orElse(null);
                if (Objects.nonNull(matched)) {
                    return matched;
                }
            }
        }
        if (CharSequenceUtil.isNotBlank(detail.getSkuId())) {
            SoB2cDetailEntity matched = soB2cDetailList.stream()
                    .filter(item -> CharSequenceUtil.equals(item.getSkuId(), detail.getSkuId()))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(matched)) {
                return matched;
            }
        }
        if (CharSequenceUtil.isNotBlank(detail.getPlatformSkuNo())) {
            return soB2cDetailList.stream()
                    .filter(item -> CharSequenceUtil.equals(item.getPlatformSkuNo(), detail.getPlatformSkuNo()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public SoReturnInstockDTO.View pdaView(String id) {
        SoReturnInstockDTO.View view = this.view(id);
        if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(view.getSourceType())) {
            List<SoReturnReceiveEntity> soReturnReceiveEntities = soReturnReceiveService.listByIds(Collections.singletonList(view.getSourceId()));
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
     * @param list
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     */
    private void sendPushTask(List<SoReturnInstockEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoReturnService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                dmpMqFeign.sendTask(resultList);
//            }
//        });
    }

    private void syncToWdt(SoReturnInstockEntity entity, SyncOperateEnum operateEnum) {

        List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainId(entity.getId());

        Set<String> warehouseIdSet = detailEntityList.stream().map(SoReturnInstockDetailEntity::getWarehouseId).collect(Collectors.toSet());
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if (mappingList.isEmpty()) {
            return;
        }
        Map<String, String> thirdWarehouseMap = mappingList.stream()
                .filter(item -> CharSequenceUtil.isBlank(item.getInventorySyncMode()) || !Objects.equals(InventorySyncModeEnum.INVENTORY.getCode(), item.getInventorySyncMode()))
                .collect(Collectors.toMap(ThirdMappingDTO.WarehouseMappingDTO::getSysWarehouseId, ThirdMappingDTO.WarehouseMappingDTO::getThirdWarehouseCode));
        //过滤掉没有第三方仓库映射的明细
        detailEntityList = detailEntityList.stream().filter(v -> thirdWarehouseMap.containsKey(v.getWarehouseId())).collect(Collectors.toList());
        Map<String, List<SoReturnInstockDetailEntity>> collectByWarehouseId = detailEntityList.stream().collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getWarehouseId));
        for (Map.Entry<String, List<SoReturnInstockDetailEntity>> entry : collectByWarehouseId.entrySet()) {
            String warehouseId = entry.getKey();
            if (SyncOperateEnum.OPERATE_APPROVE.equals(operateEnum)) {
                List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
                for (SoReturnInstockDetailEntity detailEntity : entry.getValue()) {
                    CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
                    inGoods.setSpecNo(detailEntity.getSkuNo());
                    inGoods.setNum(BigDecimal.valueOf(detailEntity.getRealQty()));
                    inGoods.setPositionNo(detailEntity.getWarehouseLocation());
                    inGoods.setWarehouseId(warehouseId);
                    inGoods.setRemark(detailEntity.getRemark());
                    inGoodsList.add(inGoods);
                }
                abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
            }
            if (SyncOperateEnum.OPERATE_DISAPPROVE.equals(operateEnum)) {
                List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
                for (SoReturnInstockDetailEntity detailEntity : entry.getValue()) {
                    CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
                    outGoods.setSpecNo(detailEntity.getSkuNo());
                    outGoods.setNum(BigDecimal.valueOf(detailEntity.getRealQty()));
                    outGoods.setPositionNo(detailEntity.getWarehouseLocation());
                    outGoods.setWarehouseId(warehouseId);
                    outGoods.setRemark(detailEntity.getRemark());
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

    @Override
    public List<SoReturnInstockEntity> queryToSdy(LocalDate startDate, LocalDate endDate, Integer pageSize, int offset) {
        return baseMapper.queryToSdy(startDate, endDate, pageSize, offset);
    }

    @Override
    public void refreshPriceFields(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<SoReturnInstockEntity> mainList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(mainList)) {
            return;
        }
        List<SoReturnInstockDetailEntity> detailList = soReturnInstockDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        List<String> b2cSoIds = mainList.stream()
                .filter(item -> OrderTypeEnum.B2C.getCode().equals(item.getType()))
                .map(SoReturnInstockEntity::getSoId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoB2cDetailEntity> b2cDetailList = CollectionUtils.isEmpty(b2cSoIds) ? Collections.emptyList() : soB2cFeign.listDetailByMainIds(b2cSoIds);
        b2cDetailList = Objects.isNull(b2cDetailList) ? Collections.emptyList() : b2cDetailList;
        Map<String, Map<String, List<SoB2cDetailEntity>>> b2cDetailMap = CollectionUtils.isEmpty(b2cDetailList) ? Collections.emptyMap()
                : b2cDetailList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getMainId()) && CharSequenceUtil.isNotBlank(item.getSkuId()))
                .collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId,
                Collectors.groupingBy(SoB2cDetailEntity::getSkuId)));

        List<String> soReturnIds = mainList.stream()
                .map(SoReturnInstockEntity::getSoReturnId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoReturnDetailEntity> returnDetailList = CollectionUtils.isEmpty(soReturnIds) ? Collections.emptyList() : soReturnFeign.listDetailByMainIds(soReturnIds);
        returnDetailList = Objects.isNull(returnDetailList) ? Collections.emptyList() : returnDetailList;
        List<String> soIds = mainList.stream()
                .filter(item -> !OrderTypeEnum.B2C.getCode().equals(item.getType()))
                .map(SoReturnInstockEntity::getSoId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isEmpty(soIds) ? Collections.emptyList() : soInfoFeign.listSoDetailByMainIds(soIds);
        soDetailList = Objects.isNull(soDetailList) ? Collections.emptyList() : soDetailList;

        Map<String, SoReturnInstockEntity> mainMap = mainList.stream()
                .collect(Collectors.toMap(SoReturnInstockEntity::getId, Function.identity(), (a, b) -> a));

        for (SoReturnInstockDetailEntity detail : detailList) {
            SoReturnInstockEntity main = mainMap.get(detail.getMainId());
            if (Objects.isNull(main)) {
                continue;
            }
            if (OrderTypeEnum.B2C.getCode().equals(main.getType())) {
                SoB2cDetailEntity b2cDetail = findB2cDetail(b2cDetailMap, main.getSoId(), detail.getSkuId());
                BigDecimal price = Objects.nonNull(b2cDetail) ? b2cDetail.getPrice() : detail.getPrice();
                BigDecimal exchangeRate = Objects.nonNull(b2cDetail) ? b2cDetail.getExchangeRate() : detail.getExchangeRate();
                fillDetailPrice(detail, price, BigDecimal.ZERO, price, exchangeRate);
                continue;
            }
            if (SourceTypeEnum.SO_RETURN_INSTOCK.getCode().equals(main.getSourceType()) || CharSequenceUtil.isNotBlank(main.getThirdCode())) {
                BigDecimal price = Objects.nonNull(detail.getPrice()) ? detail.getPrice() : MathUtil.divide(detail.getReturnAmount(), BigDecimal.valueOf(defaultRealQty(detail)));
                fillDetailPrice(detail, price, BigDecimal.ZERO, price, detail.getExchangeRate());
                continue;
            }
            SoDetailEntity soDetail = findSourceSoDetail(detail, returnDetailList, soDetailList);
            if (Objects.nonNull(soDetail)) {
                fillDetailPrice(detail, soDetail.getPrice(), soDetail.getTaxRate(), soDetail.getTaxPrice(),
                        Objects.nonNull(detail.getExchangeRate()) ? detail.getExchangeRate() : soDetail.getExchangeRate());
            }
        }
        selfService.persistRefreshedPriceFields(detailList);
    }

    /**
     * 与 {@link #refreshPriceFields(List)} 配套的事务写入，独立事务避免长时间持有连接。
     * 仅在已完成 Feign 远程查询、价格补全后调用。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void persistRefreshedPriceFields(List<SoReturnInstockDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        soReturnInstockDetailService.updateBatchById(detailList);
    }

    private SoB2cDetailEntity findB2cDetail(Map<String, Map<String, List<SoB2cDetailEntity>>> b2cDetailMap, String soId, String skuId) {
        Map<String, List<SoB2cDetailEntity>> detailMap = b2cDetailMap.get(soId);
        if (Objects.isNull(detailMap) || detailMap.isEmpty()) {
            return null;
        }
        List<SoB2cDetailEntity> detailList = detailMap.get(skuId);
        if (CollectionUtils.isEmpty(detailList)) {
            return null;
        }
        if (detailList.size() > 1) {
            log.warn("B2C退货入库价格补全匹配到同一销售订单下重复SKU明细，soId={}, skuId={}, count={}，默认取第一条",
                    soId, skuId, detailList.size());
        }
        return detailList.get(0);
    }

    private SoDetailEntity findSourceSoDetail(SoReturnInstockDetailEntity detail, List<SoReturnDetailEntity> returnDetailList, List<SoDetailEntity> soDetailList) {
        String sourceDetailId = returnDetailList.stream()
                .filter(item -> CharSequenceUtil.equals(item.getId(), detail.getSoReturnDetailId()))
                .map(SoReturnDetailEntity::getSourceDetailId)
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst()
                .orElse(detail.getSourceDetailId());
        SoDetailEntity soDetail = soDetailList.stream()
                .filter(item -> CharSequenceUtil.equals(item.getId(), sourceDetailId))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(soDetail)) {
            return soDetail;
        }
        return soDetailList.stream()
                .filter(item -> CharSequenceUtil.equals(item.getSkuId(), detail.getSkuId()))
                .findFirst()
                .orElse(null);
    }

    private void fillDetailPrice(SoReturnInstockDetailEntity detail, BigDecimal price, BigDecimal taxRate, BigDecimal taxPrice, BigDecimal exchangeRate) {
        BigDecimal safePrice = Objects.nonNull(price) ? price : BigDecimal.ZERO;
        BigDecimal safeTaxPrice = Objects.nonNull(taxPrice) ? taxPrice : safePrice;
        BigDecimal safeExchangeRate = exchangeRate;
        if (Objects.isNull(safeExchangeRate)) {
            log.warn("退货入库价格补全汇率为空，fallback 到 1，detailId={}, skuId={}", detail.getId(), detail.getSkuId());
            safeExchangeRate = BigDecimal.ONE;
        }
        BigDecimal realQty = BigDecimal.valueOf(defaultRealQty(detail));
        detail.setPrice(safePrice);
        detail.setTaxRate(Objects.nonNull(taxRate) ? taxRate : BigDecimal.ZERO);
        detail.setTaxPrice(safeTaxPrice);
        detail.setExchangeRate(safeExchangeRate);
        detail.setReturnAmount(MathUtil.multiplyWithFour(safePrice, realQty));
        detail.setTaxReturnAmount(MathUtil.multiplyWithFour(safeTaxPrice, realQty));
        detail.setReturnAmountLocalCurrency(MathUtil.multiplyWithFour(detail.getReturnAmount(), safeExchangeRate));
        detail.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithFour(detail.getTaxReturnAmount(), safeExchangeRate));
    }

    private Integer defaultRealQty(SoReturnInstockDetailEntity detail) {
        return Objects.nonNull(detail.getRealQty()) ? detail.getRealQty() : MathUtil.ZERO;
    }

    @Override
    public void downloadTemplate(String importType, HttpServletResponse response) {
        String path = "excel/soReturnInstockTemplate.xlsx";
        if (ImportTypeEnum.UPDATE.getCode().equalsIgnoreCase(importType)) {
            path = "excel/soReturnInstockUpdateTemplate.xlsx";
        }
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error(" downloadTemplate 下载失败 e={}", e.getMessage());
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        boolean isUpdate = ImportTypeEnum.UPDATE.getCode().equalsIgnoreCase(dto.getImportType());
        String taskName = isUpdate ? "销售退货入库单批量更新" : "销售退货入库单导入";
        String eventCode = isUpdate
                ? IMPORT_WMS_SO_RETURN_IN_STOCK_OVERWRITE.getCode()
                : IMPORT_WMS_SO_RETURN_IN_STOCK.getCode();
        try {
            downloadTaskFeign.saveImportTask(taskName, eventCode, dto);
        } catch (Exception e) {
            log.error("创建销售退货入库单导入任务失败", e);
            throw new ServiceException(ApiError.SO_RETURN_INSTOCK_IMPORT_TASK_CREATE_FAILED);
        }
        return Boolean.TRUE;
    }

    @Override
    public void importSoReturnInstock(BaseDTO.ImportDTO dto) {
        if (ImportTypeEnum.UPDATE.getCode().equalsIgnoreCase(dto.getImportType())) {
            importSoReturnInstockUpdate(dto);
        } else {
            importSoReturnInstockAdd(dto);
        }
    }

    public void importSoReturnInstockAdd(BaseDTO.ImportDTO dto) {
        setImportUserContext(dto);
        SoReturnStockExcelListener excelListenerUtil = new SoReturnStockExcelListener();
        byte[] bytes = downloadImportFile(dto.getFileUrl());
        readImportExcel(bytes, SoReturnStockImportExcelDTO.class, excelListenerUtil, "销售退货入库单导入");
        List<SoReturnStockImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        assertImportRowLimit(excelDateList.size());
        List<SoReturnStockImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        List<SoReturnStockImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        handleImportSoReturnstockFile(successList, errorList);
        finishImportTask(dto, excelDateList.size(), errorList, SoReturnStockImportExcelDTO.class, "销售退货入库单导入错误信息.xlsx");
    }

    public void importSoReturnInstockUpdate(BaseDTO.ImportDTO dto) {
        setImportUserContext(dto);
        SoReturnStockUpdateExcelListener excelListenerUtil = new SoReturnStockUpdateExcelListener();
        byte[] bytes = downloadImportFile(dto.getFileUrl());
        readImportExcel(bytes, SoReturnStockUpdateImportExcelDTO.class, excelListenerUtil, "销售退货入库单批量更新");
        List<SoReturnStockUpdateImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        assertImportRowLimit(excelDateList.size(), SO_RETURN_INSTOCK_IMPORT_UPDATE_MAX_ROWS);
        List<SoReturnStockUpdateImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        List<SoReturnStockUpdateImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        handleImportSoReturnstockUpdateFile(excelDateList, successList, errorList);
        finishImportTask(dto, excelDateList.size(), errorList, SoReturnStockUpdateImportExcelDTO.class, "销售退货入库单批量更新错误信息.xlsx");
    }

    private byte[] downloadImportFile(String fileUrl) {
        byte[] bytes = fileFeign.downloadFile(fileUrl);
        if (bytes == null || bytes.length == 0) {
            throw new ServiceException(ApiError.FILE_DOWNLOAD_FAILED, fileUrl);
        }
        return bytes;
    }

    private <T> void readImportExcel(byte[] bytes, Class<T> headClass, ReadListener<T> listener, String bizName) {
        try {
            EasyExcel.read(new ByteArrayInputStream(bytes), headClass, listener).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("{}格式错误！", bizName, e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        } catch (ExcelAnalysisException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ServiceException) {
                throw (ServiceException) cause;
            }
            log.error("{}解析失败！", bizName, e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}文件读取失败！", bizName, e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        }
    }

    /**
     * 一次性拉取所有核算组织（含已禁用），由调用方按 disabled 字段自行决定是否过滤。
     */
    private List<BaseIdDTO.CodeDTO> listAllAccountingCompanyList() {
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(new ArrayList<>());
        return companyList == null ? Collections.emptyList() : companyList;
    }

    private List<DictCurrencyEntity> listCurrencySafe() {
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        return currencyList == null ? Collections.emptyList() : currencyList;
    }

    private void setImportUserContext(BaseDTO.ImportDTO dto) {
        if (CharSequenceUtil.isBlank(dto.getUserId())) {
            return;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Collections.singletonList(dto.getUserId()));
        if (CollectionUtils.isEmpty(userList)) {
            return;
        }
        FindUserDTO findUserDTO = userList.get(0);
        LoginUser user = new LoginUser();
        user.setUid(findUserDTO.getUserId());
        user.setUserName(findUserDTO.getUserName());
        user.setRealName(findUserDTO.getRealName());
        user.setUserAccount(findUserDTO.getMobile());
        user.setMobile(findUserDTO.getMobile());
        UserContext.setLoginUser(user);
    }

    private void assertImportRowLimit(int rowCount) {
        assertImportRowLimit(rowCount, SO_RETURN_INSTOCK_IMPORT_MAX_ROWS);
    }

    private void assertImportRowLimit(int rowCount, int maxRows) {
        if (rowCount > maxRows) {
            if (maxRows == SO_RETURN_INSTOCK_IMPORT_UPDATE_MAX_ROWS) {
                throw new ServiceException(ApiError.COMMON_IMPORT_SIZE_EXCEED_LIMIT, maxRows);
            }
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_SIZE);
        }
    }

    private void finishImportTask(BaseDTO.ImportDTO dto, int totalCount, List<?> errorList, Class<?> errorClass, String errorFileName) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(totalCount);
        String url = "";
        int errorCount = CollectionUtils.isEmpty(errorList) ? 0 : errorList.size();
        if (errorCount > 0) {
            try {
                File file = ExcelUtil.exportFile(errorFileName, "error", errorList, errorClass);
                if (!file.isDirectory()) {
                    url = FastDFSClientUtil.uploadFile(file, errorFileName);
                }
            } catch (Exception e) {
                log.error("销售退货入库单导入错误文件上传失败，taskId={}", dto.getTaskId(), e);
            }
        }
        if (errorCount > 0 && CharSequenceUtil.isBlank(url)) {
            importResultDTO.setRemark(MessageUtils.getMessage(
                    ApiError.FILE_IMPORT_TASK_FINISH_EXPORT_FAILED,
                    errorCount, MessageUtils.getMessage(ApiError.FILE_EXPORT_ERROR_DATA_FAILED)));
        } else if (errorCount > 0) {
            importResultDTO.setRemark(MessageUtils.getMessage(
                    ApiError.FILE_IMPORT_TASK_FINISH, errorCount));
        } else {
            importResultDTO.setRemark(MessageUtils.getMessage(
                    ApiError.FILE_IMPORT_TASK_FINISH_ALL_SUCCESS));
        }
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        updateImportTaskResult(dto.getTaskId(), importResultDTO);
    }

    private void updateImportTaskResult(String taskId, BaseDTO.ImportResultDTO importResultDTO) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                downloadTaskFeign.updateTask(importResultDTO);
                return;
            } catch (Exception e) {
                log.error("销售退货入库单导入任务状态更新失败，taskId={}，attempt={}", taskId, attempt + 1, e);
            }
        }
        BaseDTO.ImportResultDTO fallback = new BaseDTO.ImportResultDTO();
        fallback.setTaskId(taskId);
        fallback.setCount(importResultDTO.getCount());
        fallback.setErrorUrl(importResultDTO.getErrorUrl());
        fallback.setFinishTime(LocalDateTime.now());
        fallback.setStatus(FileTaskStatusEnum.FINISH.getCode());
        String originalRemark = CharSequenceUtil.blankToDefault(importResultDTO.getRemark(), "");
        String syncFailedMsg = MessageUtils.getMessage(ApiError.FILE_IMPORT_TASK_STATUS_UPDATE_FAILED);
        fallback.setRemark(CharSequenceUtil.isBlank(originalRemark)
                ? syncFailedMsg
                : originalRemark + "；" + syncFailedMsg);
        try {
            downloadTaskFeign.updateTask(fallback);
        } catch (Exception e) {
            // 业务导入已完成，勿抛异常以免 Feign/MQ 入口误判整批失败并触发重试
            log.warn("销售退货入库单导入业务已完成，但任务状态同步失败（含兜底），taskId={}，{}",
                    taskId, MessageUtils.getMessage(ApiError.FILE_IMPORT_TASK_STATUS_UPDATE_FAILED), e);
        }
    }

    /**
     * 批量更新主表：全部校验通过后才落库
     */
    private void handleImportSoReturnstockUpdateFile(List<SoReturnStockUpdateImportExcelDTO> allList,
                                                     List<SoReturnStockUpdateImportExcelDTO> successList,
                                                     List<SoReturnStockUpdateImportExcelDTO> errorList) {
        markDuplicateImportUpdateRows(allList, successList, errorList);

        List<String> codeList = allList.stream()
                .map(SoReturnStockUpdateImportExcelDTO::getCode)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, SoReturnInstockEntity> entityMap = CollUtil.isEmpty(codeList)
                ? Collections.emptyMap()
                : lambdaQuery().in(SoReturnInstockEntity::getCode, codeList).list().stream()
                .collect(Collectors.toMap(SoReturnInstockEntity::getCode, Function.identity(), (a, b) -> a));

        List<String> customerNameList = allList.stream()
                .map(SoReturnStockUpdateImportExcelDTO::getCustomerName)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = Collections.emptyList();
        if (CollUtil.isNotEmpty(customerNameList)) {
            customerInfoList = FeignQuery.create(CustomerInfoEntity.class)
                    .in(CustomerInfoEntity::getName, customerNameList)
                    .eq(CustomerInfoEntity::getDisabled, Boolean.FALSE)
                    .eq(CustomerInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .list();
        }
        Map<String, List<CustomerInfoEntity>> customerMap = customerInfoList.stream()
                .collect(Collectors.groupingBy(CustomerInfoEntity::getName));

        // 一次拉全量核算公司：
        //  - inventoryOrgMap（name → CodeDTO）：用户在模版手填"库存组织"做匹配，按 !disabled 过滤，禁用组织走校验"未能找到库存组织"
        //  - salesOrgByIdMap（id → CodeDTO）：按客户档案 useOrgId 反查销售组织名，不过滤 disabled，
        //    避免历史客户挂在已禁用组织时 salesOrgName 被清空
        List<BaseIdDTO.CodeDTO> allCompanyList = listAllAccountingCompanyList();
        Map<String, BaseIdDTO.CodeDTO> inventoryOrgMap = allCompanyList.stream()
                .filter(org -> !Boolean.TRUE.equals(org.getDisabled()))
                .collect(Collectors.toMap(BaseIdDTO.CodeDTO::getName, Function.identity(), (a, b) -> a));
        Map<String, BaseIdDTO.CodeDTO> salesOrgByIdMap = allCompanyList.stream()
                .collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, Function.identity(), (a, b) -> a));

        List<DictCurrencyEntity> currencyList = listCurrencySafe();

        // 与 /customer/pagingSelect 选客户后的 UI 行为保持一致：变更客户时刷新 销售组织/销售部门/销售员
        // 这里批量预加载，避免逐行 Feign 调用
        Set<String> salesDeptIdSet = customerInfoList.stream()
                .map(CustomerInfoEntity::getSalesDeptId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, SysDepartmentEntity> salesDeptByIdMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(salesDeptIdSet)) {
            List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(new ArrayList<>(salesDeptIdSet));
            if (CollectionUtils.isNotEmpty(deptList)) {
                salesDeptByIdMap = deptList.stream()
                        .collect(Collectors.toMap(SysDepartmentEntity::getId, Function.identity(), (a, b) -> a));
            }
        }

        Map<String, BigDecimal> monthRateCache = new HashMap<>();
        for (SoReturnStockUpdateImportExcelDTO row : allList) {
            List<String> rowErrors = validateImportUpdateRow(row, entityMap, customerMap, inventoryOrgMap, currencyList, monthRateCache);
            for (String rowError : rowErrors) {
                appendImportUpdateError(row, rowError);
            }
            if (CharSequenceUtil.isNotBlank(row.getErrorMsg())) {
                if (!errorList.contains(row)) {
                    errorList.add(row);
                }
            }
        }
        successList.clear();
        successList.addAll(allList.stream()
                .filter(row -> CharSequenceUtil.isBlank(row.getErrorMsg()))
                .collect(Collectors.toList()));

        if (CollUtil.isNotEmpty(errorList)) {
            // review-skip #3: 产品需求 — 批量更新采用「全有全无」策略，任一行校验失败则整批不落库（与新增导入按组部分成功不同）；
            // 已通过 markImportUpdateBatchAbortedRows 向合格行追加「本批存在其他错误，整批未处理」提示，避免用户误以为已更新
            markImportUpdateBatchAbortedRows(successList, errorList);
            return;
        }

        List<Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>>> toUpdateList = new ArrayList<>();
        for (SoReturnStockUpdateImportExcelDTO row : successList) {
            SoReturnInstockEntity entity = entityMap.get(row.getCode());
            if (ObjectUtil.isEmpty(entity)) {
                continue;
            }
            List<CustomerInfoEntity> customers = customerMap.get(row.getCustomerName());
            if (CollUtil.isEmpty(customers)) {
                continue;
            }
            BaseIdDTO.CodeDTO inventoryOrg = inventoryOrgMap.get(row.getInventoryOrgName());
            if (ObjectUtil.isEmpty(inventoryOrg)) {
                continue;
            }
            SoReturnInstockEntity oldEntity = new SoReturnInstockEntity();
            BeanMapper.copy(entity, oldEntity);
            applyImportUpdate(entity, row, customers.get(0), inventoryOrg, currencyList,
                    salesOrgByIdMap, salesDeptByIdMap);
            toUpdateList.add(Pair.create(row, Pair.create(oldEntity, entity)));
        }

        if (CollUtil.isEmpty(toUpdateList)) {
            return;
        }
        if (appendImportUpdateExchangeRateErrorsBeforePersist(toUpdateList, monthRateCache, errorList)) {
            List<SoReturnStockUpdateImportExcelDTO> pendingRows = toUpdateList.stream()
                    .map(Pair::getFirst)
                    .filter(row -> !errorList.contains(row))
                    .collect(Collectors.toList());
            markImportUpdateBatchAbortedRows(pendingRows, errorList);
            return;
        }
        if (appendImportUpdateStatusErrorsBeforePersist(toUpdateList, errorList)) {
            List<SoReturnStockUpdateImportExcelDTO> pendingRows = toUpdateList.stream()
                    .map(Pair::getFirst)
                    .filter(row -> !errorList.contains(row))
                    .collect(Collectors.toList());
            markImportUpdateBatchAbortedRows(pendingRows, errorList);
            return;
        }
        List<Pair<SoReturnInstockEntity, SoReturnInstockEntity>> updatePairs = toUpdateList.stream()
                .map(Pair::getSecond)
                .collect(Collectors.toList());
        try {
            selfService.persistAllImportUpdate(updatePairs, monthRateCache);
        } catch (Exception e) {
            log.error("销售退货入库单批量更新落库失败", e);
            markImportUpdatePersistFailedRows(toUpdateList, errorList, resolveImportPersistErrorMsg(e));
        }
    }

    /**
     * 批量更新主表：全部校验通过后在单事务内落库
     * review-skip #2: 产品需求 — 批量更新整批单事务原子落库，保证要么全部更新成功要么全部回滚；
     * 主表/明细写库内部已按 IMPORT_UPDATE_BATCH_SIZE(500) 分批 SQL，与外层单事务策略 intentionally 不同
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistAllImportUpdate(List<Pair<SoReturnInstockEntity, SoReturnInstockEntity>> updatePairs,
                                       Map<String, BigDecimal> monthRateCache) {
        if (CollUtil.isEmpty(updatePairs)) {
            return;
        }
        Set<String> mainIdsNeedDetailSync = new HashSet<>();
        for (Pair<SoReturnInstockEntity, SoReturnInstockEntity> pair : updatePairs) {
            SoReturnInstockEntity oldEntity = pair.getFirst();
            SoReturnInstockEntity entity = pair.getSecond();
            if (!CharSequenceUtil.equals(oldEntity.getInventoryOrgId(), entity.getInventoryOrgId())
                    || !CharSequenceUtil.equals(oldEntity.getCurrency(), entity.getCurrency())) {
                mainIdsNeedDetailSync.add(entity.getId());
            }
        }
        Map<String, List<SoReturnInstockDetailEntity>> detailMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(mainIdsNeedDetailSync)) {
            List<SoReturnInstockDetailEntity> allDetails = soReturnInstockDetailService.listDetailByMainIds(
                    new ArrayList<>(mainIdsNeedDetailSync));
            detailMap = allDetails.stream()
                    .collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getMainId));
        }
        List<SoReturnInstockDetailEntity> detailsToUpdate = new ArrayList<>();
        List<SoReturnInstockEntity> mainEntitiesToUpdate = new ArrayList<>();
        for (Pair<SoReturnInstockEntity, SoReturnInstockEntity> pair : updatePairs) {
            prepareImportUpdate(pair.getFirst(), pair.getSecond(), monthRateCache, detailMap,
                    detailsToUpdate, mainEntitiesToUpdate);
        }
        batchUpdateImportMainEntities(mainEntitiesToUpdate);
        batchUpdateImportDetails(detailsToUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistAllImportAdd(List<SoReturnInstockDTO.ImportAddBundle> toAddList) {
        for (SoReturnInstockDTO.ImportAddBundle bundle : toAddList) {
            persistImportAdd(bundle);
        }
    }

    private void persistImportAdd(SoReturnInstockDTO.ImportAddBundle bundle) {
        SoReturnInstockDTO.Add dto = bundle.getAdd();
        WarehouseDTO.ListDTO warehouse = bundle.getWarehouse();

        List<SoReturnInstockDetailDTO.Add> detailList = dto.getDetailList();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_RETURN_INBOUND_DETAIL_REQUIRED);
        }
        boolean allMatch = detailList.stream().allMatch(v -> v.getRealQty() != null && v.getRealQty() > 0);
        if (!allMatch) {
            throw new ServiceException(ApiError.SO_RETURN_INSTOCK_IMPORT_RETURN_QTY_INVALID);
        }

        dto.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSTH);
        SoReturnInstockEntity entity = new SoReturnInstockEntity();
        entity.setType(dto.getType());
        entity.setShopId(dto.getShopId());
        entity.setSalesOrgId(CharSequenceUtil.blankToDefault(bundle.getSalesOrgId(), ""));
        entity.setSalesOrgName(CharSequenceUtil.blankToDefault(bundle.getSalesOrgName(), ""));
        entity.setSalesDeptId(CharSequenceUtil.blankToDefault(bundle.getSalesDeptId(), ""));
        entity.setSalesDeptName(CharSequenceUtil.blankToDefault(bundle.getSalesDeptName(), ""));
        entity.setSellerId(CharSequenceUtil.blankToDefault(bundle.getSellerId(), ""));
        entity.setSellerName(CharSequenceUtil.blankToDefault(bundle.getSellerName(), ""));
        entity.setCustomerId(bundle.getCustomerId());
        entity.setCustomerName(bundle.getCustomerName());
        entity.setWarehouseKeeperId(CharSequenceUtil.blankToDefault(dto.getWarehouseKeeperId(), ""));
        entity.setWarehouseKeeperName("");
        entity.setInventoryOrgId(warehouse != null ? CharSequenceUtil.blankToDefault(warehouse.getOrgId(), "") : "");
        entity.setInventoryOrgName(CharSequenceUtil.blankToDefault(bundle.getInventoryOrgName(), ""));
        entity.setReturnLogisticCode(dto.getReturnLogisticCode());
        entity.setSoReturnId(dto.getSoReturnId());
        entity.setSoReturnCode(dto.getSoReturnCode());
        entity.setPlatformOrderCode(dto.getPlatformOrderCode());
        entity.setThirdCode(dto.getThirdCode());
        entity.setSourceCode(dto.getSourceCode());
        entity.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
        entity.setSourceId(dto.getSourceId());
        entity.setCode(code);
        entity.setBillDate(dto.getBillDate());
        entity.setCurrency(dto.getCurrency());
        entity.setCurrencySymbol(dto.getCurrencySymbol());
        this.save(entity);

        operateLogService.addModuleOperateLog(
                CharSequenceUtil.format("新增了一个销售退货入库单【{}】", code),
                ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "新增操作");
        persistImportAddDetails(bundle, entity.getId());
    }

    private void persistImportAddDetails(SoReturnInstockDTO.ImportAddBundle bundle, String mainId) {
        SoReturnInstockDTO.Add dto = bundle.getAdd();
        Map<String, String> platformSkuBySkuNo = bundle.getPlatformSkuBySkuNo() != null
                ? bundle.getPlatformSkuBySkuNo() : Collections.emptyMap();
        Map<String, String> warehouseNameById = bundle.getWarehouseNameById() != null
                ? bundle.getWarehouseNameById() : Collections.emptyMap();
        Map<String, Boolean> subContractBySkuId = bundle.getSubContractBySkuId() != null
                ? bundle.getSubContractBySkuId() : Collections.emptyMap();
        List<SoReturnInstockDetailEntity> detailEntities = new ArrayList<>();
        for (SoReturnInstockDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
            detailEntity.setMainId(mainId);
            detailEntity.setSkuId(detailDto.getSkuId());
            detailEntity.setSkuNo(detailDto.getSkuNo());
            detailEntity.setRealQty(detailDto.getRealQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            detailEntity.setWarehouseId(detailDto.getWarehouseId());
            detailEntity.setWarehouseName(CharSequenceUtil.blankToDefault(
                    warehouseNameById.get(detailDto.getWarehouseId()), ""));
            if (CharSequenceUtil.isNotBlank(detailDto.getPlatformSkuNo())) {
                detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            } else {
                detailEntity.setPlatformSkuNo(CharSequenceUtil.blankToDefault(
                        platformSkuBySkuNo.get(detailDto.getSkuNo()), ""));
            }
            detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
            detailEntity.setReturnAmount(detailDto.getReturnAmount());
            detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            detailEntity.setPrice(detailDto.getPrice());
            detailEntity.setTaxPrice(detailDto.getTaxPrice());
            detailEntity.setTaxRate(detailDto.getTaxRate());
            detailEntity.setExchangeRate(detailDto.getExchangeRate() != null
                    ? detailDto.getExchangeRate() : dto.getExchangeRate());
            if (Boolean.TRUE.equals(subContractBySkuId.get(detailDto.getSkuId()))) {
                detailEntity.setIsSubContract(Boolean.TRUE);
            }
            detailEntities.add(detailEntity);
        }
        if (!soReturnInstockDetailService.saveBatch(detailEntities)) {
            throw new ServiceException(ApiError.SO_RETURN_INSTOCK_IMPORT_PERSIST_FAILED);
        }
    }

    private void enrichImportAddDetailContext(List<SoReturnInstockDTO.ImportAddBundle> bundles,
                                              Map<String, String> warehouseNameById) {
        if (CollUtil.isEmpty(bundles)) {
            return;
        }
        Map<String, String> warehouseNameMap = warehouseNameById != null ? warehouseNameById : Collections.emptyMap();
        List<String> allSkuIds = bundles.stream()
                .flatMap(bundle -> bundle.getAdd().getDetailList().stream())
                .map(SoReturnInstockDetailDTO.Add::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Boolean> subContractBySkuId = Collections.emptyMap();
        if (CollUtil.isNotEmpty(allSkuIds)) {
            List<PurchaseOrderDetailEntity> purchaseOrderDetails = scmTaskFeign.getLatestByCrtTime(allSkuIds);
            if (CollectionUtils.isNotEmpty(purchaseOrderDetails)) {
                subContractBySkuId = purchaseOrderDetails.stream()
                        .filter(item -> SubcontractTypeEnum.ENUM_PARENT.getCode().equals(item.getSubcontractType()))
                        .collect(Collectors.toMap(PurchaseOrderDetailEntity::getSkuId, item -> Boolean.TRUE, (a, b) -> a));
            }
        }
        for (SoReturnInstockDTO.ImportAddBundle bundle : bundles) {
            bundle.setWarehouseNameById(warehouseNameMap);
            bundle.setSubContractBySkuId(subContractBySkuId);
        }
        Map<String, Map<String, String>> platformSkuByCustomerId = loadImportAddPlatformSkuByCustomer(bundles);
        for (SoReturnInstockDTO.ImportAddBundle bundle : bundles) {
            if (CharSequenceUtil.isBlank(bundle.getCustomerId())) {
                bundle.setPlatformSkuBySkuNo(Collections.emptyMap());
                continue;
            }
            Map<String, String> customerPlatformSkuMap = platformSkuByCustomerId.getOrDefault(
                    bundle.getCustomerId(), Collections.emptyMap());
            if (CollUtil.isEmpty(customerPlatformSkuMap)) {
                bundle.setPlatformSkuBySkuNo(Collections.emptyMap());
                continue;
            }
            Map<String, String> bundlePlatformSkuMap = bundle.getAdd().getDetailList().stream()
                    .map(SoReturnInstockDetailDTO.Add::getSkuNo)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .filter(customerPlatformSkuMap::containsKey)
                    .collect(Collectors.toMap(Function.identity(), customerPlatformSkuMap::get, (a, b) -> a));
            bundle.setPlatformSkuBySkuNo(bundlePlatformSkuMap);
        }
    }

    private Map<String, Map<String, String>> loadImportAddPlatformSkuByCustomer(
            List<SoReturnInstockDTO.ImportAddBundle> bundles) {
        Map<String, Map<String, String>> platformSkuByCustomerId = new HashMap<>();
        Map<String, List<SoReturnInstockDTO.ImportAddBundle>> bundlesByCustomerId = bundles.stream()
                .filter(bundle -> CharSequenceUtil.isNotBlank(bundle.getCustomerId()))
                .collect(Collectors.groupingBy(SoReturnInstockDTO.ImportAddBundle::getCustomerId));
        for (Map.Entry<String, List<SoReturnInstockDTO.ImportAddBundle>> entry : bundlesByCustomerId.entrySet()) {
            List<String> skuNos = entry.getValue().stream()
                    .flatMap(bundle -> bundle.getAdd().getDetailList().stream())
                    .map(SoReturnInstockDetailDTO.Add::getSkuNo)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(skuNos)) {
                platformSkuByCustomerId.put(entry.getKey(), Collections.emptyMap());
                continue;
            }
            SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
            skuParamDTO.setCutomerId(entry.getKey());
            skuParamDTO.setSkuNoList(skuNos);
            List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);
            if (CollectionUtils.isEmpty(productSkuInfoList)) {
                platformSkuByCustomerId.put(entry.getKey(), Collections.emptyMap());
                continue;
            }
            platformSkuByCustomerId.put(entry.getKey(), productSkuInfoList.stream()
                    .collect(Collectors.toMap(SkuMappingDTO.ProductSkuInfoDTO::getSkuNo,
                            item -> CharSequenceUtil.blankToDefault(item.getPlatformSkuNo(), ""),
                            (a, b) -> a)));
        }
        return platformSkuByCustomerId;
    }

    private void updateImportAddSkuOccupyStatus(List<SoReturnInstockDTO.ImportAddBundle> bundles) {
        if (CollUtil.isEmpty(bundles)) {
            return;
        }
        List<String> skuIds = bundles.stream()
                .flatMap(bundle -> bundle.getAdd().getDetailList().stream())
                .map(SoReturnInstockDetailDTO.Add::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    private List<String> validateImportUpdateRow(SoReturnStockUpdateImportExcelDTO row,
                                                 Map<String, SoReturnInstockEntity> entityMap,
                                                 Map<String, List<CustomerInfoEntity>> customerMap,
                                                 Map<String, BaseIdDTO.CodeDTO> inventoryOrgMap,
                                                 List<DictCurrencyEntity> currencyList,
                                                 Map<String, BigDecimal> monthRateCache) {
        List<String> errorMsgList = new ArrayList<>();
        SoReturnInstockEntity entity = entityMap.get(row.getCode());
        if (ObjectUtil.isEmpty(entity)) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CODE_NOT_FOUND));
        } else {
            if (Boolean.TRUE.equals(entity.getInvalidStatus())
                    || !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus())) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_UPDATE_STATUS_INVALID));
            } else if (!SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode().equalsIgnoreCase(entity.getSourceType())
                    && !SourceTypeEnum.SELF_ADD.getCode().equalsIgnoreCase(entity.getSourceType())) {
                // review-skip: 产品需求 — 提示文案「三方仓/手工建单」，与 SourceTypeEnum 展示名 intentionally 不一致
                errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_SOURCE_TYPE_FORBIDDEN));
            }
        }
        if (CollUtil.isEmpty(customerMap.get(row.getCustomerName()))) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CUSTOMER_NOT_FOUND));
        } else if (customerMap.get(row.getCustomerName()).size() > 1) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CUSTOMER_DUPLICATE,
                    row.getCustomerName()));
        } else if (ObjectUtil.isNotEmpty(entity)
                && isMappedToReturnOrder(entity)
                && !CharSequenceUtil.equals(entity.getCustomerName(), row.getCustomerName())) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CUSTOMER_CHANGE_FORBIDDEN));
        }
        if (ObjectUtil.isEmpty(inventoryOrgMap.get(row.getInventoryOrgName()))) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_INVENTORY_ORG_NOT_FOUND));
        }
        DictCurrencyEntity matchedCurrency = null;
        if (CharSequenceUtil.isNotBlank(row.getCurrencyStr())) {
            matchedCurrency = currencyList.stream()
                    .filter(obj -> obj.getName().equals(row.getCurrencyStr()) || obj.getId().equals(row.getCurrencyStr()))
                    .findFirst().orElse(null);
            if (matchedCurrency == null) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CURRENCY_NOT_FOUND));
            }
        }
        // 币种变更场景：按"入库日期"月份预校验汇率；仅对可落库的单据调用 Feign
        if (isImportUpdateEntityUpdatable(entity)
                && matchedCurrency != null
                && !CharSequenceUtil.equals(entity.getCurrency(), matchedCurrency.getId())) {
            LocalDate billDate = row.getBillDate() != null ? row.getBillDate()
                    : (entity.getBillDate() != null ? entity.getBillDate() : LocalDate.now());
            String currency = matchedCurrency.getId();
            if (!CurrencyEnum.CNY.getCurrencyCode().equals(currency)) {
                loadImportMonthRate(billDate, currency, monthRateCache, false, false);
                String rateError = buildImportExchangeRateMissingMsg(billDate, currency, row.getCurrencyStr(), monthRateCache);
                if (CharSequenceUtil.isNotBlank(rateError)) {
                    errorMsgList.add(rateError);
                }
            }
        }
        return errorMsgList;
    }

    /**
     * 落库前按行二次校验汇率（仅读缓存，不调 Feign）；缺汇率写入 errorMsg 并整批 abort
     *
     * @return true 表示存在汇率错误，调用方应终止落库
     */
    private boolean appendImportUpdateExchangeRateErrorsBeforePersist(
            List<Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>>> toUpdateList,
            Map<String, BigDecimal> monthRateCache,
            List<SoReturnStockUpdateImportExcelDTO> errorList) {
        boolean hasError = false;
        for (Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>> item : toUpdateList) {
            SoReturnStockUpdateImportExcelDTO row = item.getFirst();
            SoReturnInstockEntity oldEntity = item.getSecond().getFirst();
            SoReturnInstockEntity entity = item.getSecond().getSecond();
            if (CharSequenceUtil.equals(oldEntity.getCurrency(), entity.getCurrency())) {
                continue;
            }
            LocalDate billDate = entity.getBillDate() != null ? entity.getBillDate() : LocalDate.now();
            String currency = CharSequenceUtil.blankToDefault(entity.getCurrency(), CurrencyEnum.CNY.getCurrencyCode());
            String rateError = buildImportExchangeRateMissingMsg(billDate, currency, row.getCurrencyStr(), monthRateCache);
            if (CharSequenceUtil.isBlank(rateError)) {
                continue;
            }
            appendImportUpdateError(row, rateError);
            if (!errorList.contains(row)) {
                errorList.add(row);
            }
            hasError = true;
        }
        return hasError;
    }

    /**
     * 落库前二次校验单据状态与 version，避免异步任务执行窗口内并发审批/作废导致覆盖
     *
     * @return true 表示存在状态/版本冲突，调用方应终止落库
     */
    private boolean appendImportUpdateStatusErrorsBeforePersist(
            List<Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>>> toUpdateList,
            List<SoReturnStockUpdateImportExcelDTO> errorList) {
        List<String> codeList = toUpdateList.stream()
                .map(item -> item.getFirst().getCode())
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(codeList)) {
            return false;
        }
        Map<String, SoReturnInstockEntity> freshEntityMap = lambdaQuery()
                .in(SoReturnInstockEntity::getCode, codeList)
                .list()
                .stream()
                .collect(Collectors.toMap(SoReturnInstockEntity::getCode, Function.identity(), (a, b) -> a));
        boolean hasError = false;
        for (Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>> item : toUpdateList) {
            SoReturnStockUpdateImportExcelDTO row = item.getFirst();
            SoReturnInstockEntity snapshotEntity = item.getSecond().getFirst();
            SoReturnInstockEntity entityToUpdate = item.getSecond().getSecond();
            SoReturnInstockEntity freshEntity = freshEntityMap.get(row.getCode());
            if (ObjectUtil.isEmpty(freshEntity)) {
                appendImportUpdateError(row, MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CODE_NOT_FOUND));
                if (!errorList.contains(row)) {
                    errorList.add(row);
                }
                hasError = true;
                continue;
            }
            if (!isImportUpdateEntityUpdatable(freshEntity)) {
                appendImportUpdateError(row, MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_UPDATE_STATUS_INVALID));
                if (!errorList.contains(row)) {
                    errorList.add(row);
                }
                hasError = true;
                continue;
            }
            if (!ObjectUtil.equal(snapshotEntity.getVersion(), freshEntity.getVersion())) {
                appendImportUpdateError(row, MessageUtils.getMessage(ApiError.BILL_DATA_LOCKED));
                if (!errorList.contains(row)) {
                    errorList.add(row);
                }
                hasError = true;
                continue;
            }
            entityToUpdate.setVersion(freshEntity.getVersion());
        }
        return hasError;
    }

    /**
     * 从 monthRateCache 只读校验汇率；缺失时返回国际化错误文案，否则返回 null
     */
    private String buildImportExchangeRateMissingMsg(LocalDate billDate, String currency, String currencyDisplay,
                                                    Map<String, BigDecimal> monthRateCache) {
        if (CurrencyEnum.CNY.getCurrencyCode().equals(
                CharSequenceUtil.blankToDefault(currency, CurrencyEnum.CNY.getCurrencyCode()))) {
            return null;
        }
        BigDecimal monthRate = loadImportMonthRate(billDate, currency, monthRateCache, false, true);
        if (monthRate != null && MathUtil.compareTo(monthRate, BigDecimal.ZERO) != MathUtil.ZERO) {
            return null;
        }
        String display = CharSequenceUtil.isNotBlank(currencyDisplay) ? currencyDisplay : currency;
        return MessageUtils.getMessage(ApiError.COMMON_EXCHANGE_RATE_NOT_EXIST,
                billDate.format(DateTimeFormatter.ofPattern("yyyy-MM")), display);
    }

    /**
     * 基于全表检测重复单号，重复行追加提示并移出 successList
     */
    private void markDuplicateImportUpdateRows(List<SoReturnStockUpdateImportExcelDTO> allList,
                                               List<SoReturnStockUpdateImportExcelDTO> successList,
                                               List<SoReturnStockUpdateImportExcelDTO> errorList) {
        Set<String> duplicateCodes = allList.stream()
                .filter(row -> CharSequenceUtil.isNotBlank(row.getCode()))
                .collect(Collectors.groupingBy(SoReturnStockUpdateImportExcelDTO::getCode, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(duplicateCodes)) {
            return;
        }
        for (SoReturnStockUpdateImportExcelDTO row : allList) {
            if (!duplicateCodes.contains(row.getCode())) {
                continue;
            }
            appendImportUpdateError(row, MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_DUPLICATE_CODE));
            if (!errorList.contains(row)) {
                errorList.add(row);
            }
        }
        successList.removeIf(row -> duplicateCodes.contains(row.getCode()));
    }

    /**
     * 本批存在错误时，已通过校验但未落库的行追加整批未处理提示
     */
    private void markImportUpdateBatchAbortedRows(List<SoReturnStockUpdateImportExcelDTO> successList,
                                                  List<SoReturnStockUpdateImportExcelDTO> errorList) {
        for (SoReturnStockUpdateImportExcelDTO row : successList) {
            if (errorList.contains(row)) {
                continue;
            }
            appendImportUpdateError(row, MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_BATCH_ABORT));
            errorList.add(row);
        }
    }

    /**
     * 同组存在错误时，已通过校验但未落库的行追加整组未处理提示
     */
    private void markImportAddGroupAbortedRows(List<SoReturnStockImportExcelDTO> groupRows,
                                               List<SoReturnStockImportExcelDTO> errorList) {
        for (SoReturnStockImportExcelDTO row : groupRows) {
            if (errorList.contains(row)) {
                continue;
            }
            appendImportAddError(row, MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_BATCH_ABORT));
            errorList.add(row);
        }
    }

    private void markImportAddPersistFailedRows(List<SoReturnInstockDTO.ImportAddBundle> toAddList,
                                                List<SoReturnStockImportExcelDTO> errorList,
                                                String persistErrorMsg) {
        for (SoReturnInstockDTO.ImportAddBundle bundle : toAddList) {
            for (SoReturnStockImportExcelDTO row : bundle.getExcelRows()) {
                appendImportAddError(row, persistErrorMsg);
                if (!errorList.contains(row)) {
                    errorList.add(row);
                }
            }
        }
    }

    private void markImportUpdatePersistFailedRows(
            List<Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>>> toUpdateList,
            List<SoReturnStockUpdateImportExcelDTO> errorList,
            String persistErrorMsg) {
        for (Pair<SoReturnStockUpdateImportExcelDTO, Pair<SoReturnInstockEntity, SoReturnInstockEntity>> item : toUpdateList) {
            SoReturnStockUpdateImportExcelDTO row = item.getFirst();
            appendImportUpdateError(row, persistErrorMsg);
            if (!errorList.contains(row)) {
                errorList.add(row);
            }
        }
    }

    private String resolveImportPersistErrorMsg(Exception e) {
        String msg;
        if (e instanceof ServiceException && CharSequenceUtil.isNotBlank(e.getMessage())) {
            msg = e.getMessage();
        } else {
            msg = MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_PERSIST_FAILED);
        }
        if (msg.length() > 200) {
            return msg.substring(0, 200);
        }
        return msg;
    }

    private void enrichImportAddBundles(List<SoReturnInstockDTO.ImportAddBundle> bundles,
                                        List<BaseIdDTO.CodeDTO> allCompanyList) {
        if (CollUtil.isEmpty(bundles)) {
            return;
        }
        Set<String> salesDeptIdSet = bundles.stream()
                .map(SoReturnInstockDTO.ImportAddBundle::getSalesDeptId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, SysDepartmentEntity> salesDeptByIdMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(salesDeptIdSet)) {
            List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(new ArrayList<>(salesDeptIdSet));
            if (CollectionUtils.isNotEmpty(deptList)) {
                salesDeptByIdMap = deptList.stream()
                        .collect(Collectors.toMap(SysDepartmentEntity::getId, Function.identity(), (a, b) -> a));
            }
        }
        List<BaseIdDTO.CodeDTO> companyList = allCompanyList != null ? allCompanyList : Collections.emptyList();
        Map<String, BaseIdDTO.CodeDTO> orgByIdMap = companyList.stream()
                .collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, Function.identity(), (a, b) -> a));
        for (SoReturnInstockDTO.ImportAddBundle bundle : bundles) {
            SysDepartmentEntity salesDept = CharSequenceUtil.isNotBlank(bundle.getSalesDeptId())
                    ? salesDeptByIdMap.get(bundle.getSalesDeptId()) : null;
            bundle.setSalesDeptName(salesDept != null ? CharSequenceUtil.blankToDefault(salesDept.getName(), "") : "");
            BaseIdDTO.CodeDTO salesOrg = CharSequenceUtil.isNotBlank(bundle.getSalesOrgId())
                    ? orgByIdMap.get(bundle.getSalesOrgId()) : null;
            bundle.setSalesOrgName(salesOrg != null ? CharSequenceUtil.blankToDefault(salesOrg.getName(), "") : "");
            if (bundle.getWarehouse() != null && CharSequenceUtil.isNotBlank(bundle.getWarehouse().getOrgId())) {
                BaseIdDTO.CodeDTO inventoryOrg = orgByIdMap.get(bundle.getWarehouse().getOrgId());
                bundle.setInventoryOrgName(inventoryOrg != null
                        ? CharSequenceUtil.blankToDefault(inventoryOrg.getName(), "") : "");
            }
        }
    }

    /**
     * 落库前预校验并填充汇率；失败返回错误文案，成功返回 null
     */
    private List<SoReturnInstockDTO.ImportAddBundle> prepareImportAddExchangeRates(
            List<SoReturnInstockDTO.ImportAddBundle> bundles,
            List<SoReturnStockImportExcelDTO> errorList) {
        if (CollUtil.isEmpty(bundles)) {
            return Collections.emptyList();
        }
        Map<String, BigDecimal> monthRateCache = new HashMap<>();
        List<SoReturnInstockDTO.ImportAddBundle> readyList = new ArrayList<>();
        for (SoReturnInstockDTO.ImportAddBundle bundle : bundles) {
            String exchangeError = resolveImportAddExchangeRateError(bundle, monthRateCache);
            if (CharSequenceUtil.isNotBlank(exchangeError)) {
                for (SoReturnStockImportExcelDTO row : bundle.getExcelRows()) {
                    appendImportAddError(row, exchangeError);
                    if (!errorList.contains(row)) {
                        errorList.add(row);
                    }
                }
                continue;
            }
            readyList.add(bundle);
        }
        return readyList;
    }

    private String resolveImportAddExchangeRateError(SoReturnInstockDTO.ImportAddBundle bundle,
                                                     Map<String, BigDecimal> monthRateCache) {
        SoReturnInstockDTO.Add add = bundle.getAdd();
        LocalDate billDate = add.getBillDate() != null ? add.getBillDate() : LocalDate.now();
        String currency = CharSequenceUtil.blankToDefault(add.getCurrency(), CurrencyEnum.CNY.getCurrencyCode());
        BigDecimal exchangeRate = loadImportMonthRate(billDate, currency, monthRateCache, false, false);
        if (exchangeRate == null || MathUtil.compareTo(exchangeRate, BigDecimal.ZERO) == MathUtil.ZERO) {
            if (CurrencyEnum.CNY.getCurrencyCode().equals(currency)) {
                exchangeRate = MathUtil.BigDecimal_1;
            } else {
                return MessageUtils.getMessage(ApiError.COMMON_EXCHANGE_RATE_NOT_EXIST,
                        billDate.format(DateTimeFormatter.ofPattern("yyyy-MM")), currency);
            }
        }
        applyImportAddExchangeRateToBundle(bundle, exchangeRate);
        return null;
    }

    private void applyImportAddExchangeRateToBundle(SoReturnInstockDTO.ImportAddBundle bundle,
                                                    BigDecimal exchangeRate) {
        SoReturnInstockDTO.Add add = bundle.getAdd();
        add.setExchangeRate(exchangeRate);
        for (SoReturnInstockDetailDTO.Add detailDto : add.getDetailList()) {
            detailDto.setExchangeRate(exchangeRate);
            if (detailDto.getReturnAmount() != null && detailDto.getReturnAmountLocalCurrency() == null) {
                detailDto.setReturnAmountLocalCurrency(
                        soReturnNoticeService.calLocalCurrency(exchangeRate, detailDto.getReturnAmount()));
            }
            if (detailDto.getTaxReturnAmount() != null && detailDto.getTaxReturnAmountLocalCurrency() == null) {
                detailDto.setTaxReturnAmountLocalCurrency(
                        soReturnNoticeService.calLocalCurrency(exchangeRate, detailDto.getTaxReturnAmount()));
            }
        }
    }

    private void appendImportAddError(SoReturnStockImportExcelDTO row, String msg) {
        List<String> errorMsgList = parseImportUpdateErrorMsg(row.getErrorMsg());
        if (errorMsgList.contains(msg)) {
            return;
        }
        errorMsgList.add(msg);
        row.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
    }

    private void appendImportUpdateError(SoReturnStockUpdateImportExcelDTO row, String msg) {
        List<String> errorMsgList = parseImportUpdateErrorMsg(row.getErrorMsg());
        if (errorMsgList.contains(msg)) {
            return;
        }
        errorMsgList.add(msg);
        row.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
    }

    private List<String> parseImportUpdateErrorMsg(String errorMsg) {
        if (CharSequenceUtil.isBlank(errorMsg)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String part : errorMsg.split("；")) {
            if (CharSequenceUtil.isBlank(part)) {
                continue;
            }
            int idx = part.indexOf('、');
            if (idx > 0 && idx < part.length() - 1) {
                result.add(part.substring(idx + 1));
            } else {
                result.add(part);
            }
        }
        return result;
    }

    private boolean isImportUpdateEntityUpdatable(SoReturnInstockEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return false;
        }
        if (Boolean.TRUE.equals(entity.getInvalidStatus())
                || !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus())) {
            return false;
        }
        return SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode().equalsIgnoreCase(entity.getSourceType())
                || SourceTypeEnum.SELF_ADD.getCode().equalsIgnoreCase(entity.getSourceType());
    }

    private boolean isMappedToReturnOrder(SoReturnInstockEntity entity) {
        return CharSequenceUtil.isNotBlank(entity.getSoReturnCode()) || CharSequenceUtil.isNotBlank(entity.getSoReturnId());
    }

    private void applyImportUpdate(SoReturnInstockEntity entity,
                                   SoReturnStockUpdateImportExcelDTO row,
                                   CustomerInfoEntity customerInfo,
                                   BaseIdDTO.CodeDTO inventoryOrg,
                                   List<DictCurrencyEntity> currencyList,
                                   Map<String, BaseIdDTO.CodeDTO> salesOrgByIdMap,
                                   Map<String, SysDepartmentEntity> salesDeptByIdMap) {
        // 仅当 "退货客户" 在本次导入中真正发生变更时，才走 /customer/pagingSelect 等价路径刷新 销售员/销售部门/销售组织；
        // 否则模版里的客户列只是用来定位行，不应该把客户主数据上的销售员/部门/组织变化静默回写到单据。
        boolean customerChanged = !CharSequenceUtil.equals(entity.getCustomerName(), customerInfo.getName());
        if (customerChanged && !isMappedToReturnOrder(entity)) {
            entity.setCustomerId(customerInfo.getId());
            entity.setCustomerName(customerInfo.getName());
            entity.setSellerId(CharSequenceUtil.blankToDefault(customerInfo.getSellerId(), ""));
            entity.setSellerName(CharSequenceUtil.blankToDefault(customerInfo.getSellerName(), ""));
            entity.setSalesDeptId(CharSequenceUtil.blankToDefault(customerInfo.getSalesDeptId(), ""));
            SysDepartmentEntity salesDept = CharSequenceUtil.isNotBlank(customerInfo.getSalesDeptId())
                    ? salesDeptByIdMap.get(customerInfo.getSalesDeptId()) : null;
            entity.setSalesDeptName(salesDept != null ? CharSequenceUtil.blankToDefault(salesDept.getName(), "") : "");
            entity.setSalesOrgId(CharSequenceUtil.blankToDefault(customerInfo.getUseOrgId(), ""));
            BaseIdDTO.CodeDTO salesOrg = CharSequenceUtil.isNotBlank(customerInfo.getUseOrgId())
                    ? salesOrgByIdMap.get(customerInfo.getUseOrgId()) : null;
            entity.setSalesOrgName(salesOrg != null ? CharSequenceUtil.blankToDefault(salesOrg.getName(), "") : "");
        }
        // 模版中其它字段独立判断更新，即使客户没变也允许调整
        entity.setInventoryOrgId(inventoryOrg.getId());
        entity.setInventoryOrgName(inventoryOrg.getName());
        if (CharSequenceUtil.isNotBlank(row.getCurrencyStr())) {
            DictCurrencyEntity dictCurrencyEntity = currencyList.stream()
                    .filter(obj -> obj.getName().equals(row.getCurrencyStr()) || obj.getId().equals(row.getCurrencyStr()))
                    .findFirst()
                    .orElse(null);
            if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                entity.setCurrencySymbol(dictCurrencyEntity.getSymbol());
                entity.setCurrency(dictCurrencyEntity.getId());
            }
        }
        if (row.getBillDate() != null) {
            entity.setBillDate(row.getBillDate());
        }
        if (CharSequenceUtil.isNotBlank(row.getTypeCode())) {
            entity.setType(row.getTypeCode());
        }
        if (CharSequenceUtil.isNotBlank(row.getReturnLogisticCode())) {
            entity.setReturnLogisticCode(row.getReturnLogisticCode());
        }
    }

    private void prepareImportUpdate(SoReturnInstockEntity oldEntity, SoReturnInstockEntity entity,
                                     Map<String, BigDecimal> monthRateCache,
                                     Map<String, List<SoReturnInstockDetailEntity>> detailMap,
                                     List<SoReturnInstockDetailEntity> detailsToUpdate,
                                     List<SoReturnInstockEntity> mainEntitiesToUpdate) {
        boolean inventoryOrgChanged = !CharSequenceUtil.equals(oldEntity.getInventoryOrgId(), entity.getInventoryOrgId());
        boolean currencyChanged = !CharSequenceUtil.equals(oldEntity.getCurrency(), entity.getCurrency());
        operateLogService.addModuleOperateLogByObj(oldEntity, entity, ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "", "");
        if (!CharSequenceUtil.equals(oldEntity.getCustomerName(), entity.getCustomerName())) {
            operateLogService.addModuleOperateLog(
                    CharSequenceUtil.format("批量导入更新：退货客户从【{}】修改为【{}】", oldEntity.getCustomerName(), entity.getCustomerName()),
                    ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "编辑");
        }
        mainEntitiesToUpdate.add(entity);
        if (inventoryOrgChanged || currencyChanged) {
            applyImportUpdateDetailChanges(entity, inventoryOrgChanged, currencyChanged, monthRateCache, detailMap, detailsToUpdate);
        }
    }

    private void batchUpdateImportMainEntities(List<SoReturnInstockEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        for (int i = 0; i < entities.size(); i += IMPORT_UPDATE_BATCH_SIZE) {
            int end = Math.min(i + IMPORT_UPDATE_BATCH_SIZE, entities.size());
            List<SoReturnInstockEntity> batch = entities.subList(i, end);
            if (!this.updateBatchById(new ArrayList<>(batch))) {
                throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
            }
        }
    }

    private void applyImportUpdateDetailChanges(SoReturnInstockEntity entity,
                                                boolean inventoryOrgChanged,
                                                boolean currencyChanged,
                                                Map<String, BigDecimal> monthRateCache,
                                                Map<String, List<SoReturnInstockDetailEntity>> detailMap,
                                                List<SoReturnInstockDetailEntity> detailsToUpdate) {
        List<SoReturnInstockDetailEntity> detailList = detailMap.getOrDefault(entity.getId(), Collections.emptyList());
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        if (inventoryOrgChanged) {
            for (SoReturnInstockDetailEntity detail : detailList) {
                detail.setWarehouseId("");
                detail.setWarehouseName("");
                detail.setWarehouseLocation("");
            }
        }
        if (currencyChanged) {
            BigDecimal exchangeRate = resolveImportExchangeRate(entity, monthRateCache);
            for (SoReturnInstockDetailEntity detail : detailList) {
                detail.setCurrency(entity.getCurrency());
                detail.setExchangeRate(exchangeRate);
                if (detail.getReturnAmount() != null) {
                    detail.setReturnAmountLocalCurrency(soReturnNoticeService.calLocalCurrency(exchangeRate, detail.getReturnAmount()));
                }
                if (detail.getTaxReturnAmount() != null) {
                    detail.setTaxReturnAmountLocalCurrency(soReturnNoticeService.calLocalCurrency(exchangeRate, detail.getTaxReturnAmount()));
                }
            }
        }
        detailsToUpdate.addAll(detailList);
    }

    private void batchUpdateImportDetails(List<SoReturnInstockDetailEntity> detailsToUpdate) {
        if (CollUtil.isEmpty(detailsToUpdate)) {
            return;
        }
        for (int i = 0; i < detailsToUpdate.size(); i += IMPORT_UPDATE_BATCH_SIZE) {
            int end = Math.min(i + IMPORT_UPDATE_BATCH_SIZE, detailsToUpdate.size());
            List<SoReturnInstockDetailEntity> batch = detailsToUpdate.subList(i, end);
            if (!soReturnInstockDetailService.updateBatchById(new ArrayList<>(batch))) {
                throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
            }
        }
    }

    private BigDecimal resolveImportExchangeRate(SoReturnInstockEntity entity, Map<String, BigDecimal> monthRateCache) {
        LocalDate billDate = entity.getBillDate() != null ? entity.getBillDate() : LocalDate.now();
        String currency = CharSequenceUtil.blankToDefault(entity.getCurrency(), CurrencyEnum.CNY.getCurrencyCode());
        return loadImportMonthRate(billDate, currency, monthRateCache, true, true);
    }

    /**
     * @param throwIfMissing 缓存缺失或汇率为 0 时是否抛异常
     * @param cacheOnly      true 时仅读缓存（落库阶段使用，避免事务内 Feign）；false 时允许 Feign 回填缓存
     */
    private BigDecimal loadImportMonthRate(LocalDate billDate, String currency,
                                           Map<String, BigDecimal> monthRateCache,
                                           boolean throwIfMissing, boolean cacheOnly) {
        String normalizedCurrency = CharSequenceUtil.blankToDefault(currency, CurrencyEnum.CNY.getCurrencyCode());
        if (CurrencyEnum.CNY.getCurrencyCode().equals(normalizedCurrency)) {
            return MathUtil.BigDecimal_1;
        }
        String cacheKey = buildImportMonthRateCacheKey(billDate, normalizedCurrency);
        BigDecimal exchangeRate;
        if (cacheOnly) {
            exchangeRate = monthRateCache.get(cacheKey);
        } else {
            exchangeRate = monthRateCache.computeIfAbsent(cacheKey,
                    k -> dmpTaskFeign.getMonthRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), normalizedCurrency));
        }
        if (throwIfMissing && (exchangeRate == null || MathUtil.compareTo(exchangeRate, BigDecimal.ZERO) == MathUtil.ZERO)) {
            throw new ServiceException(ApiError.COMMON_EXCHANGE_RATE_NOT_EXIST,
                    billDate.format(DateTimeFormatter.ofPattern("yyyy-MM")), normalizedCurrency);
        }
        return exchangeRate;
    }

    private static String buildImportMonthRateCacheKey(LocalDate billDate, String currency) {
        return billDate.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "_" + currency;
    }

    @Override
    public AliexpressReturnInstockDTO newSyncDataToCaiNiao(SoReturnInstockEntity entity, List<SoReturnInstockDetailEntity> detailEntityList, String syncOperate) {
        String warehouseId = detailEntityList.get(0).getWarehouseId();
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouseEntity)) {
            log.warn("新建菜鸟退货入库单失败，未找到仓库");
            return null;
        }
        OverseasProviderEntity overseasProviderEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(warehouseId);
        if (Objects.isNull(overseasProviderEntity)) {
            log.warn("新建菜鸟退货入库单失败，未找到三方仓配置");
            return null;
        }
        OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByWarehouseId(warehouseId);
        if (Objects.isNull(overseasProviderWarehouseEntity)) {
            log.warn("新建菜鸟退货入库单失败，未找到三方仓仓库");
            return null;
        }
        Map<String, Object> authMap = overseasProviderEntity.getAuthJson();
        String shopId = authMap.get("shopId").toString();
        AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
        AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
        aliexpressAuthDTO.setUrl(authMap.get("baseUrl").toString());
        aliexpressAuthDTO.setAppKey(authMap.get("clientId").toString());
        aliexpressAuthDTO.setAppSecret(authMap.get("clientSecret").toString());
        aliexpressAuthDTO.setShopId(authMap.get("shopId").toString());
        aliexpressAuthDTO.setAccessToken(aliExpressShopInfoDTO.getToken());
        aliexpressAuthDTO.setOwnerCode(overseasProviderEntity.getOwnerCode());

        List<String> skuIds = detailEntityList.stream().map(v -> v.getSkuId()).collect(Collectors.toList());
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setAuthId(overseasProviderEntity.getId());
        listingInfoParamDTO.setSkuIdList(skuIds);
        List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

        List<AliexpressReturnInstockDTO.OrderLines> orderLines = new ArrayList<>();
        for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : detailEntityList) {
            SkuMappingDTO.MappingSkuViewDTO mappingSkuViewDTO = mappingSkuViewDTOList.stream()
                    .filter(v -> v.getProductSkuId().equals(soReturnInstockDetailEntity.getSkuId()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(mappingSkuViewDTO)) {
                continue;
            }
            AliexpressReturnInstockDTO.OrderLines orderLine = AliexpressReturnInstockDTO.OrderLines.builder()
                    .itemCode(mappingSkuViewDTO.getPlatformSkuNo())
                    .itemId(mappingSkuViewDTO.getPlatformSkuId())
                    .planQty(soReturnInstockDetailEntity.getRealQty())
                    .inventoryType("1")
                    .ownerCode(overseasProviderEntity.getOwnerCode())
                    .build();
            orderLines.add(orderLine);
        }
        if (CollectionUtils.isEmpty(orderLines)) {
            log.warn("新建菜鸟退货入库单失败，未找到有效的sku映射关系");
            return null;
        }
        AliexpressReturnInstockDTO aliexpressInboundDTO = AliexpressReturnInstockDTO.builder()
                .aliexpressAuthDTO(aliexpressAuthDTO)
                .returnOrder(AliexpressReturnInstockDTO.ReturnOrder.builder()
                        .orderType("THRK")
                        .returnOrderCode(entity.getCode())
                        .ownerCode(overseasProviderEntity.getOwnerCode())
                        .senderInfo(AliexpressReturnInstockDTO.ReturnOrder.SenderInfoDTO.builder()
                                .detailAddress(warehouseEntity.getAddress())
                                .build())
                        .warehouseCode(overseasProviderWarehouseEntity.getPlatformWarehouseCode())
                        .build())
                .OrderLines(orderLines)
                .build();
        return aliexpressInboundDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO returnInstockSave(SoB2cReturnEntity soB2cReturnEntity, List<SoB2cReturnDetailDTO.ViewDTO> detailEntityList, List<SoB2cReturnDTO.ReturnInstockDTO> returnInstockDTOS, SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> b2cDetailEntityList) {
        SoB2cReturnDTO.ReturnInstockDTO instockDTO = returnInstockDTOS.get(0);
        WarehouseEntity warehouseEntity = warehouseService.getById(instockDTO.getWarehouseId());
        if (Objects.isNull(warehouseEntity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "仓库【" + instockDTO.getWarehouseName() + "】");
        }
        //构建退货入库单新增数据
        SoReturnInstockDTO.Add add = SoB2cReturnInstockConverter.INSTANCE.soB2cReturnEntityToAdd(instockDTO);
        add.setWarehouseKeeperId(warehouseEntity.getChargeId());
        //构建明细数据
        List<SoReturnInstockDetailDTO.Add> detailList = SoB2cReturnInstockConverter.INSTANCE.soB2cReturnDetailEntityToAdd(returnInstockDTOS);
        detailList.forEach(detail -> {
            detailEntityList.stream().filter(e -> e.getId().equals(detail.getSoReturnDetailId())).findFirst().ifPresent(f -> {
                detail.setIsCheckReceiveQty(Boolean.FALSE);
                detail.setRemark(f.getRemark());
                detail.setReturnReasonDict(soB2cReturnEntity.getReason());
                b2cDetailEntityList.stream().filter(h -> h.getId().equals(f.getSoDetailId())).findFirst().ifPresent(g -> {
                    detail.setPrice(g.getPrice());
                    detail.setTaxRate(BigDecimal.ZERO);
                    detail.setTaxPrice(g.getPrice());
                    detail.setReturnAmount(MathUtil.multiplyWithFour(g.getPrice(), BigDecimal.valueOf(detail.getRealQty())));
                    detail.setTaxReturnAmount(MathUtil.multiplyWithFour(g.getPrice(), BigDecimal.valueOf(detail.getRealQty())));
                    detail.setWarehouseId(instockDTO.getWarehouseId());
                    detail.setPlatformSkuNo(g.getPlatformSkuNo());
                    detail.setExchangeRate(g.getExchangeRate());
                    detail.setReturnAmountLocalCurrency(MathUtil.multiplyWithFour(g.getExchangeRate(), detail.getReturnAmount()));
                    detail.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithFour(g.getExchangeRate(), detail.getTaxReturnAmount()));
                });
            });
        });
        add.setDetailList(detailList);
        this.add(add);
        return BatchResultDTO.success(soB2cReturnEntity.getId(), soB2cReturnEntity.getCode(), "下推成功");
    }

    /**
     * 导出数据处理
     *
     * @param successList
     * @param errorList
     * @return void
     * @author will
     * @date 2025/4/24 19:53
     */
    private void handleImportSoReturnstockFile(List<SoReturnStockImportExcelDTO> successList, List<SoReturnStockImportExcelDTO> errorList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        Map<String, List<SoReturnStockImportExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(obj -> obj.getCustomerName().concat(obj.getWarehouseName()).concat(obj.getBillDateStr()).concat(obj.getTypeName()).concat(CharSequenceUtil.isNotBlank(obj.getReturnLogisticCode()) ? obj.getReturnLogisticCode() : "").concat(CharSequenceUtil.isNotBlank(obj.getThirdCode()) ? obj.getThirdCode() : "").concat(CharSequenceUtil.isNotBlank(obj.getPlatformOrderCode()) ? obj.getPlatformOrderCode() : "")));

        List<String> customerNameList = successList.stream().map(SoReturnStockImportExcelDTO::getCustomerName).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = FeignQuery.create(CustomerInfoEntity.class).in(CustomerInfoEntity::getName, customerNameList).eq(CustomerInfoEntity::getDisabled, Boolean.FALSE).eq(CustomerInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus()).list();
        Map<String, List<CustomerInfoEntity>> customerMap = customerInfoList.stream().collect(Collectors.groupingBy(CustomerInfoEntity::getName));

        //币种
        List<DictCurrencyEntity> viewList = listCurrencySafe();

        //sku
        List<String> skuNoList = successList.stream().map(SoReturnStockImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNoList);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity()));

        //仓库
        List<String> warehosueNameList = successList.stream().map(SoReturnStockImportExcelDTO::getWarehouseName).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(warehosueNameList);
        Map<String, WarehouseDTO.ListDTO> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, Function.identity()));
        Map<String, String> warehouseNameById = warehouseList.stream()
                .collect(Collectors.toMap(WarehouseDTO.ListDTO::getId, WarehouseDTO.ListDTO::getName, (a, b) -> a));

        //仓位
        List<String> warehousIdList = warehouseList.stream().map(WarehouseDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<String> warehouseLocationNameList = successList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getWarehouseLocationName())).map(SoReturnStockImportExcelDTO::getWarehouseLocationName).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdsAndNameList(warehousIdList, warehouseLocationNameList);
        Map<String, WarehouseLocationEntity> warehouseLocationMap = warehouseLocationList.stream().collect(Collectors.toMap(obj -> CharSequenceUtil.format("{}-{}", obj.getWarehouseId(), obj.getName()), Function.identity()));

        List<SoReturnInstockDTO.ImportAddBundle> toPersistList = new ArrayList<>();

        for (Map.Entry<String, List<SoReturnStockImportExcelDTO>> entry : map.entrySet()) {
            List<SoReturnStockImportExcelDTO> value = entry.getValue();
            SoReturnStockImportExcelDTO excelDTO = value.get(0);
            List<String> headerErrors = new ArrayList<>();
            SoReturnInstockDTO.Add add = new SoReturnInstockDTO.Add();

            List<CustomerInfoEntity> customerInfoEntityList = customerMap.get(excelDTO.getCustomerName());
            if (CollUtil.isEmpty(customerInfoEntityList)) {
                headerErrors.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_ADD_CUSTOMER_NOT_FOUND,
                        excelDTO.getCustomerName()));
            } else if (customerInfoEntityList.size() > 1) {
                headerErrors.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_CUSTOMER_DUPLICATE,
                        excelDTO.getCustomerName()));
            } else {
                add.setCustomerId(customerInfoEntityList.get(0).getId());
            }
            DictCurrencyEntity dictCurrencyEntity = viewList.stream()
                    .filter(obj -> obj.getName().equals(excelDTO.getCurrencyStr()) || obj.getId().equals(excelDTO.getCurrencyStr()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dictCurrencyEntity)) {
                headerErrors.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_ADD_CURRENCY_NOT_FOUND,
                        excelDTO.getCurrencyStr()));
            } else {
                add.setCurrencySymbol(dictCurrencyEntity.getSymbol());
                add.setCurrency(dictCurrencyEntity.getId());
            }
            if (CollUtil.isNotEmpty(headerErrors)) {
                String headerErrorMsg = FieldValidUtil.getMsgSort(headerErrors);
                for (SoReturnStockImportExcelDTO row : value) {
                    row.setErrorMsg(headerErrorMsg);
                    errorList.add(row);
                }
                continue;
            }
            add.setBillDate(excelDTO.getBillDate());
            add.setType(OrderTypeEnum.getCodeByName(excelDTO.getTypeName()));
            add.setReturnLogisticCode(excelDTO.getReturnLogisticCode());
            add.setPlatformOrderCode(excelDTO.getPlatformOrderCode());
            add.setThirdCode(excelDTO.getThirdCode());

            List<SoReturnInstockDetailDTO.Add> detailList = new ArrayList<>();
            boolean groupHasError = false;
            for (SoReturnStockImportExcelDTO soReturnStockImportExcelDTO : value) {
                List<String> rowErrors = new ArrayList<>();
                SoReturnInstockDetailDTO.Add addDetail = new SoReturnInstockDetailDTO.Add();
                SkuVO skuVO = skuMap.get(soReturnStockImportExcelDTO.getSkuNo());
                if (ObjectUtil.isEmpty(skuVO) || !ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(skuVO.getStatus())) {
                    rowErrors.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_ADD_SKU_NOT_FOUND,
                            soReturnStockImportExcelDTO.getSkuNo()));
                } else {
                    addDetail.setSkuId(skuVO.getSkuId());
                    addDetail.setSkuNo(soReturnStockImportExcelDTO.getSkuNo());
                }
                WarehouseDTO.ListDTO warehouseEntity = warehouseMap.get(soReturnStockImportExcelDTO.getWarehouseName());
                if (ObjectUtil.isEmpty(warehouseEntity) || warehouseEntity.getDisabled()
                        || !ApproveStatusEnum.APPROVE.getStatus().equals(warehouseEntity.getApproveStatus().getCode())) {
                    rowErrors.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_NOT_FOUND,
                            soReturnStockImportExcelDTO.getWarehouseName()));
                } else {
                    addDetail.setWarehouseId(warehouseEntity.getId());
                }
                addDetail.setReturnTypeDict(ReturnTypeEnum.getCode(soReturnStockImportExcelDTO.getReturnType()));
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationMap.get(CharSequenceUtil.format("{}-{}",
                        ObjectUtil.isEmpty(warehouseEntity) ? "" : warehouseEntity.getId(),
                        soReturnStockImportExcelDTO.getWarehouseLocationName()));
                if (ObjectUtil.isEmpty(warehouseLocationEntity)
                        && CharSequenceUtil.isNotBlank(soReturnStockImportExcelDTO.getWarehouseLocationName())) {
                    rowErrors.add(MessageUtils.getMessage(
                            ApiError.SO_RETURN_INSTOCK_IMPORT_ADD_WAREHOUSE_LOCATION_NOT_FOUND,
                            soReturnStockImportExcelDTO.getWarehouseName(),
                            soReturnStockImportExcelDTO.getWarehouseLocationName()));
                }
                if (ObjectUtil.isNotEmpty(warehouseLocationEntity)
                        && CharSequenceUtil.isNotBlank(soReturnStockImportExcelDTO.getWarehouseLocationName())) {
                    addDetail.setWarehouseLocation(warehouseLocationEntity.getCode());
                }
                addDetail.setRemark(soReturnStockImportExcelDTO.getRemark());
                String realQtyStr = soReturnStockImportExcelDTO.getRealQtyStr();
                if (!StrUtils.isInteger(realQtyStr) || Integer.parseInt(realQtyStr.trim()) < 1) {
                    rowErrors.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_RETURN_QTY_INVALID));
                } else {
                    addDetail.setRealQty(Integer.parseInt(realQtyStr.trim()));
                }
                if (CollUtil.isNotEmpty(rowErrors)) {
                    soReturnStockImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(rowErrors));
                    errorList.add(soReturnStockImportExcelDTO);
                    groupHasError = true;
                    continue;
                }
                addDetail.setReturnAmount(MathUtil.valueOf(soReturnStockImportExcelDTO.getReturnAmountStr()));
                addDetail.setTaxReturnAmount(MathUtil.valueOf(soReturnStockImportExcelDTO.getTaxReturnAmountStr()));
                addDetail.setReturnReasonDict(ReturnReasonEnum.getCodeByName(soReturnStockImportExcelDTO.getReturnReasonDictStr()));
                detailList.add(addDetail);
            }
            if (groupHasError) {
                markImportAddGroupAbortedRows(value, errorList);
                continue;
            }
            if (CollUtil.isEmpty(detailList)) {
                continue;
            }
            add.setSellerId(customerInfoEntityList.get(0).getSellerId());
            add.setSalesDeptId(customerInfoEntityList.get(0).getSalesDeptId());
            add.setSalesOrgId(customerInfoEntityList.get(0).getUseOrgId());
            add.setWarehouseId(detailList.get(0).getWarehouseId());
            add.setDetailList(detailList);
            WarehouseDTO.ListDTO warehouseEntity = warehouseMap.get(excelDTO.getWarehouseName());
            CustomerInfoEntity customerInfo = customerInfoEntityList.get(0);
            toPersistList.add(new SoReturnInstockDTO.ImportAddBundle(add, new ArrayList<>(value),
                    customerInfo.getId(), customerInfo.getName(),
                    customerInfo.getSellerId(), customerInfo.getSellerName(),
                    customerInfo.getSalesDeptId(), customerInfo.getUseOrgId(),
                    warehouseEntity));
        }

        if (CollUtil.isEmpty(toPersistList)) {
            return;
        }
        List<BaseIdDTO.CodeDTO> allCompanyList = listAllAccountingCompanyList();
        enrichImportAddBundles(toPersistList, allCompanyList);
        enrichImportAddDetailContext(toPersistList, warehouseNameById);
        List<SoReturnInstockDTO.ImportAddBundle> readyToPersistList = prepareImportAddExchangeRates(toPersistList, errorList);
        if (CollUtil.isEmpty(readyToPersistList)) {
            return;
        }

        List<SoReturnInstockDTO.ImportAddBundle> persistedBundles = new ArrayList<>();
        for (SoReturnInstockDTO.ImportAddBundle bundle : readyToPersistList) {
            try {
                selfService.persistAllImportAdd(Collections.singletonList(bundle));
                persistedBundles.add(bundle);
            } catch (Exception e) {
                log.error("销售退货入库单导入落库失败", e);
                markImportAddPersistFailedRows(Collections.singletonList(bundle), errorList,
                        resolveImportPersistErrorMsg(e));
            }
        }
        // review-skip #1: 产品需求 — SKU 占用为落库后非关键后置步骤，失败仅 warn 日志，不回写 errorList、不追加任务 remark；
        // 原因：单据已创建即视为导入成功，若标失败用户会重复导入产生重复单；占用状态可后续人工/定时补偿
        if (CollUtil.isNotEmpty(persistedBundles)) {
            try {
                updateImportAddSkuOccupyStatus(persistedBundles);
            } catch (Exception e) {
                log.warn("销售退货入库单导入落库已成功，但SKU占用状态更新失败，不影响已创建单据", e);
            }
        }
    }

    public void syncToSdyHandler(List<SoReturnInstockEntity> list, String operate) {
        for (SoReturnInstockEntity entity : list) {
            List<SoReturnInstockDetailEntity> detailEntities = soReturnInstockDetailService.listDetailByMainId(entity.getId());
            syncSoReturnInstockService.syncDataToSdy(entity,
                    detailEntities,
                    operate);
        }
    }

    @Override
    @DataIdempotent(keyIdName = "entity.code", businessType = "generateLogisticsBill")
    public BatchResultDTO generateLogisticsBill(SoReturnInstockEntity entity) {
        String returnLogisticCode = entity.getReturnLogisticCode();
        if (StringUtils.isBlank(returnLogisticCode)) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "退货物流单号为空，不允许下推自发货费用");
        }

        if (!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "单据状态不为已审核，不允许下推自发货费用");
        }
        String id = entity.getId();
        String sourceType = SourceTypeEnum.SO_RETURN_INSTOCK.getCode();
        List<LogisticsBillEntity> logisticsBillEntityList = FeignQuery.create(LogisticsBillEntity.class)
                .eq(LogisticsBillEntity::getOutstockId, id)
                .eq(LogisticsBillEntity::getSourceType, sourceType)
                .list();
        if (CollUtil.isNotEmpty(logisticsBillEntityList)) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "已关联生成自发货费用，不允许重复下推");
        }

        logisticsBillCostFeign.generateLogisticsBill(entity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("退货入库【%s】下推物流单", entity.getCode()), ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), id, "下推操作");
        return BatchResultDTO.success(id, entity.getCode(), "操作成功");
    }

    @Override
    public Map<String, SoReturnInstockEntity> mapByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SoReturnInstockEntity> list = this.listByIds(ids);
        return list.stream().collect(Collectors.toMap(SoReturnInstockEntity::getId, Function.identity()));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO deleteByIds(SoReturnInstockEntity entity, List<SoReturnInstockDetailEntity> returnDetails) {
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.BILL_DELETE_ALLOWED_STATUS_ONLY.getMsg());
        }
        //发送金蝶
        sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());
        //推送数帝云
        this.syncToSdyHandler(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除详情表
        soReturnInstockDetailService.delete(Collections.singletonList(entity.getId()));
        //删除主表
        // 执行批量删除
        boolean result = this.removeByIds(Collections.singletonList(entity.getId()));
        if (!result) {
            throw new ServiceException(ApiError.BILL_DELETE_FAILED);
        }
        // 添加批量操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】销售退货入库单", UserContext.getDefaultLoginUser().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), entity.getId(), "删除操作");
        // 返回成功结果
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
    }
}
