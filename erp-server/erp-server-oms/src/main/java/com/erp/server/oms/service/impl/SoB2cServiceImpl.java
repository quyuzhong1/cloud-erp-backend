package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.ThirdConstants;
import com.common.business.constant.*;
import com.common.business.dto.*;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.CommonConstants;
import com.common.core.constant.EnumMessage;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.RulePromptWordEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.SoB2cDTO.PagingParamDTO;
import com.erp.model.oms.dto.excel.B2CSoImportExcelDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.OperateLogEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.transfer.TransferCancelOrderReq;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.LogisticsChannelWarehouseTypeEnum;
import com.erp.model.tms.enums.LogisticsHandoverDocTypeEnum;
import com.erp.model.tms.enums.LogisticsMappingTypeEnum;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressOrderDetail;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.*;
import com.erp.rpc.wms.feign.CfgSettingFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.convert.CustomerInfoConverter;
import com.erp.server.oms.convert.WalmartShipOrderConverter;
import com.erp.server.oms.kingdee.SyncSoB2cService;
import com.erp.server.oms.kingdee.SyncThirdWarehouseService;
import com.erp.server.oms.listener.B2CSoImportExcelListener;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.query.SoB2cQueryHandler;
import com.erp.server.oms.service.*;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyOrderDTO;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyOrderDTO;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import com.sdk.third.lingxing.dto.UpdateOrderDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_ABNORMAL;

/**
 * <p>
 * B2C销售订单表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cService {

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopChannelRefService shopChannelRefService;


    @Resource
    private InventoryFeign inventoryFeign;

    @Lazy
    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Lazy
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Lazy
    @Resource
    private SoB2cReturnService soB2cReturnService;
    @Lazy
    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private SoB2cRefCategoryService soB2cRefCategoryService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Lazy
    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private OrderCategoryDetailService orderCategoryDetailService;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private ShopCostService shopCostService;

    @Resource
    private RuleOrderApprovalService ruleOrderApprovalService;
    @Resource
    private CfgRuleInvoiceService cfgRuleInvoiceService;

    @Resource
    private RuleDeliveryWarehouseService ruleDeliveryWarehouseService;

    @Resource
    private RuleLogisticsService ruleLogisticsService;

    @Lazy
    @Resource
    private SoB2cFinanceService soB2cFinanceService;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private LogisticsBillCostFeign logisticsBillCostFeign;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;

    @Resource
    private ThirdWarehouseFeign thirdWarehouseFeign;

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Lazy
    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    private SoB2cDeliveryInterceptFeign soB2cDeliveryInterceptFeign;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Resource
    private ForecastFeign forecastFeign;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;

    @Resource
    private LogisticsProductFeign logisticsProductFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private SoB2cLabelService soB2cLabelService;

    @Resource
    private SoB2cQueryHandler soB2cQueryHandler;

    @Lazy
    @Resource
    private CustomerB2cService customerB2cService;

    @Resource
    private TransferLogisticsFeign transferLogisticsFeign;

    @Resource
    @Lazy
    private SoB2cServiceImpl soB2cService;

    @Resource
    private SoB2cDeclareProductService soB2cDeclareProductService;
    @Resource
    private CfgRuleDeclareService cfgRuleDeclareService;
    @Resource
    private CfgSettingFeign cfgSettingFeign;

    @Resource
    private CfgRuleOrderHandleService cfgRuleOrderHandleService;


    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;

    @Resource
    private CfgRuleOutFeign cfgRuleOutFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private AliExpressOrderService aliExpressOrderService;
    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private SoB2cExtendService soB2cExtendService;
    @Resource
    private SyncSoB2cService syncSoB2cService;
    @Resource
    private TikTokFullService tikTokFullService;
    @Resource
    private AuthDataFeign authDataFeign;

    @Lazy
    @Resource
    private InvoiceInfoService invoiceInfoService;

    @Resource
    private SoB2cRuleService soB2cRuleService;
    @Lazy
    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;

    @Lazy
    @Resource
    private SoB2cCoreService soB2cCoreService;
    @Lazy
    @Resource
    private SoMultiChannelService soMultiChannelService;
    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;
    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;
    @Resource
    private FileFeign fileFeign;

    @Resource
    private PackagePlanService packagePlanService;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_SECONDS = 10000;

    @Resource
    private SyncThirdWarehouseService syncThirdWarehouseService;

    @Override
    public PagingVO<SoB2cDTO.ListDTO> paging(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO) {
        PagingParamDTO params = pagingParamDTO.getParams();
		params.setPermissionSql(getPermissionSql());
		DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
        	dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        params.setDynamicDataSource(dynamicDataSource);
        List<AdvanceQueryDTO> advanceQueryDTOList = params.getAdvanceQueryDTOList();
        //是否缺货 过滤
        Boolean isOutStock = (Boolean) advanceQueryDTOList.stream().filter(v -> v.getField().equals("isOutStock")).findAny().orElse(new AdvanceQueryDTO()).getValue();
        //是否虚拟仓缺货
        Boolean isVirtualOutStock = (Boolean) advanceQueryDTOList.stream().filter(v -> v.getField().equals("isVirtualOutStock")).findAny().orElse(new AdvanceQueryDTO()).getValue();
        if (Objects.nonNull(isOutStock)) {
            return this.filterIsOutStockList(pagingParamDTO, isOutStock, params.getIsFullyManaged());
        } else if (Objects.nonNull(isVirtualOutStock)) {
            return this.filterIsVirtualOutStockList(pagingParamDTO, isVirtualOutStock, params.getIsFullyManaged());
        } else {
            Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
            IPage<SoB2cDTO.ListDTO> pageData = null;
            if (params.getIsFullyManaged()){
                pageData = this.baseMapper.fullyManagedPaging(query, params, null);
            }else {
            	if(DynamicDataSourceTypeEnum.DORIS.getCode().equals(dynamicDataSource)) {
            		int queryCount = 0;
            		String defaultSql = params.getSqlMap().get("default");
            		boolean unSameCountFlag = true;
        			while(queryCount < 3) {
        				DynamicDataSourceContextHolder.poll();
        				DynamicDataSourceThreadLocal.set(DynamicDataSourceTypeEnum.DORIS);
        	            DynamicDataSourceContextHolder.push(DynamicDataSourceTypeEnum.DORIS.getCode());
        				params.setDynamicDataSource(DynamicDataSourceTypeEnum.DORIS.getCode());
        				params.getSqlMap().put("default", defaultSql);
        				params.setOnlyQueryId(1);
        				pageData = this.baseMapper.paging(query, params, null);
                		if(CollUtil.isEmpty(pageData.getRecords())) {
                			unSameCountFlag = false;
                			break;
                		}
                		int dorisCurrentCount = pageData.getRecords().size();
                		long pageCurrent = pageData.getCurrent();
                        long pages = pageData.getPages();
                        long pageSize = pageData.getSize();
                        long pageTotal = pageData.getTotal();

                		DynamicDataSourceContextHolder.poll();
        				DynamicDataSourceThreadLocal.set(DynamicDataSourceTypeEnum.POSTGRES);
        	            DynamicDataSourceContextHolder.push(DynamicDataSourceTypeEnum.POSTGRES.getCode());
        				params.setDynamicDataSource(DynamicDataSourceTypeEnum.POSTGRES.getCode());
        				params.getSqlMap().put("default", defaultSql + " and sb2c.id in (" + pageData.getRecords().stream().map(d -> d.getId()).collect(Collectors.joining("','", "'", "'")) +")");
        				Page pgQuery = new Page(1, -1 , dorisCurrentCount , false);
        				pageData = this.baseMapper.paging(pgQuery, params, null);
        				if(CollUtil.isNotEmpty(pageData.getRecords()) && (dorisCurrentCount == pageData.getRecords().size())) {
        					pageData.setCurrent(pageCurrent);
        			        pageData.setPages(pages);
        			        pageData.setSize(pageSize);
        			        pageData.setTotal(pageTotal);
        					unSameCountFlag = false;
        					break;
        				}
        				queryCount = queryCount + 1;
        			}
        			if(unSameCountFlag) {
        				params.getSqlMap().put("default", defaultSql);
        				pageData = this.baseMapper.paging(query, params, null);
        			}
            	}else {
            		pageData = this.baseMapper.paging(query, params, null);
            	}
            }
            if (CollUtil.isEmpty(pageData.getRecords())) {
                return new PagingVO(pageData);
            }
            // 数据处理
            fillList(pageData.getRecords(),params.getIsFullyManaged());
            return new PagingVO(pageData);
        }
    }

    private String getPermissionSql() {
    	DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
        	dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        String shopPermissionSql = authDataFeign.getShopPermissionSqlByDynamicDataSource("sb2c.shop_id" , dynamicDataSource);
        shopPermissionSql = CharSequenceUtil.isNotBlank(shopPermissionSql) ? shopPermissionSql : " and 1=1 ";
        String warehousePermissionSql = authDataFeign.getWarehousePermissionSqlByDynamicDataSource("sb2cd.warehouse_id" , dynamicDataSource);
        warehousePermissionSql = CharSequenceUtil.isNotBlank(warehousePermissionSql) ? " and exists (select 1 from so_b2c_detail sb2cd where sb2c.id = sb2cd.main_id and sb2cd.is_deleted=FALSE " + warehousePermissionSql + ")" : " and 1=1 ";
        return CharSequenceUtil.format(" {}  {}", shopPermissionSql, warehousePermissionSql);
    }

    private PagingVO filterIsVirtualOutStockList(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO, Boolean isVirtualOutStock, boolean isFullyManaged) {
        List<AdvanceQueryDTO> advanceQueryDTOList = pagingParamDTO.getParams().getAdvanceQueryDTOList();
        //必须选仓库而且只能选一个
        List<String> warehouseIdList = com.common.business.utils.CollectionUtils.convertStrClzToList(advanceQueryDTOList.stream().filter(v -> v.getField().equals("sb2cd.virtual_warehouse_id") && (v.getCompare().equals(QueryConditionEnum.EQ.getCompareCode()) || v.getCompare().equals(QueryConditionEnum.IN_LIST.getCompareCode()))).findFirst().orElse(new AdvanceQueryDTO()).getValue());
        if (warehouseIdList.size() != 1) {
            throw new ServiceException("选择X缺条件必须选择虚拟仓库且只能选择一个仓库");
        }
        //查询全部数据，过滤出有缺货
        Page query = new Page(1, Integer.MAX_VALUE, false);
        IPage<SoB2cDTO.ListDTO> pageData = null;
        if(isFullyManaged){
            pageData = this.baseMapper.fullyManagedPaging(query, pagingParamDTO.getParams(), Boolean.TRUE);
        }else {
            pageData = this.baseMapper.paging(query, pagingParamDTO.getParams(), Boolean.TRUE);
        }        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData.getRecords(), 0, pagingParamDTO.getPageSize(), pagingParamDTO.getCurrPage());
        }
        List<SoB2cDTO.ListDTO> list = pageData.getRecords();
        // 数据处理
        fillList(list, pagingParamDTO.getParams().getIsFullyManaged());
        if (isVirtualOutStock) {
            for (SoB2cDTO.ListDTO listDTO : list) {
                listDTO.setDetailList(listDTO.getDetailList().stream().filter(v -> v.getDetailLabelDTO().getIsVirtualOutStock() != null && v.getDetailLabelDTO().getIsVirtualOutStock()).collect(Collectors.toList()));
            }
            list = list.stream().filter(v -> CollectionUtils.isNotEmpty(v.getDetailList())).collect(Collectors.toList());
        } else {
            for (SoB2cDTO.ListDTO listDTO : list) {
                listDTO.setDetailList(listDTO.getDetailList().stream().filter(v -> v.getDetailLabelDTO().getIsVirtualOutStock() == null || !v.getDetailLabelDTO().getIsVirtualOutStock()).collect(Collectors.toList()));
            }
            list = list.stream().filter(v -> CollectionUtils.isNotEmpty(v.getDetailList())).collect(Collectors.toList());
        }
        IPage<SoB2cDTO.ListDTO> result = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize(), list.size());
        result.setPages(pagingParamDTO.getCurrPage());
        result.setSize(pagingParamDTO.getPageSize());
        result.setTotal(list.size());
        list = com.common.business.utils.CollectionUtils.paginateList(list, pagingParamDTO.getPageSize(), pagingParamDTO.getCurrPage());
        result.setRecords(list);
        return new PagingVO(result);
    }

    private PagingVO filterIsOutStockList(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO, Boolean isOutStock, boolean isFullyManaged) {
        List<AdvanceQueryDTO> advanceQueryDTOList = pagingParamDTO.getParams().getAdvanceQueryDTOList();
        //必须选仓库而且只能选一个
        List<String> warehouseIdList = com.common.business.utils.CollectionUtils.convertStrClzToList(advanceQueryDTOList.stream().filter(v -> v.getField().equals("sb2cd.warehouse_id") && (v.getCompare().equals(QueryConditionEnum.EQ.getCompareCode()) || v.getCompare().equals(QueryConditionEnum.IN_LIST.getCompareCode()))).findFirst().orElse(new AdvanceQueryDTO()).getValue());
        if (warehouseIdList.size() != 1) {
            throw new ServiceException("选择缺货条件必须选择仓库且只能选择一个仓库");
        }
        //查询全部数据，过滤出有缺货
        Page query = new Page(1, Integer.MAX_VALUE, false);
        IPage<SoB2cDTO.ListDTO> pageData = null;
        if (isFullyManaged){
            pageData = this.baseMapper.fullyManagedPaging(query, pagingParamDTO.getParams(), Boolean.TRUE);
        }else {
            pageData = this.baseMapper.paging(query, pagingParamDTO.getParams(), Boolean.TRUE);
        }        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData.getRecords(), 0, pagingParamDTO.getPageSize(), pagingParamDTO.getCurrPage());
        }
        List<SoB2cDTO.ListDTO> list = pageData.getRecords();
        // 数据处理
        fillList(list, pagingParamDTO.getParams().getIsFullyManaged());
        if (isOutStock) {
            for (SoB2cDTO.ListDTO listDTO : list) {
                listDTO.setDetailList(listDTO.getDetailList().stream().filter(v -> v.getDetailLabelDTO().getIsOutStock() != null && v.getDetailLabelDTO().getIsOutStock()).collect(Collectors.toList()));
            }
            list = list.stream().filter(v -> CollectionUtils.isNotEmpty(v.getDetailList())).collect(Collectors.toList());
        } else {
            for (SoB2cDTO.ListDTO listDTO : list) {
                listDTO.setDetailList(listDTO.getDetailList().stream().filter(v -> v.getDetailLabelDTO().getIsOutStock() == null || !v.getDetailLabelDTO().getIsOutStock()).collect(Collectors.toList()));
            }
            list = list.stream().filter(v -> CollectionUtils.isNotEmpty(v.getDetailList())).collect(Collectors.toList());
        }
        IPage<SoB2cDTO.ListDTO> result = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize(), list.size());
        result.setPages(pagingParamDTO.getCurrPage());
        result.setSize(pagingParamDTO.getPageSize());
        result.setTotal(list.size());
        list = com.common.business.utils.CollectionUtils.paginateList(list, pagingParamDTO.getPageSize(), pagingParamDTO.getCurrPage());
        result.setRecords(list);
        return new PagingVO(result);
    }


    @Override
    public List<SoB2cDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoB2cTabEnum[] values = SoB2cTabEnum.values();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        String permissionSql = getPermissionSql();
        for (SoB2cTabEnum item : values) {
            SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(permissionSql);
            SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
            String tabSql = soB2cQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            //查询店铺设置权限
            Integer count = this.baseMapper.listCount(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cEntity add(SoB2cDTO.AddDTO addDTO, String code) {
        SoB2cEntity soB2cEntity = new SoB2cEntity();
        BeanMapperUtils.copy(addDTO, soB2cEntity);
        //查询支付方式是否需要填写付款时间
        Boolean isFlag = soB2cCoreService.listPayMethodSetting(soB2cEntity);
        if(!isFlag && Objects.isNull(soB2cEntity.getPayTime())){
            throw new ServiceException("付款时间不能为空");
        }
        if (StrUtil.isEmpty(soB2cEntity.getSourceType())) {
            soB2cEntity.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
        }
        String dictPlatform = addDTO.getDictPlatform();
        // 数据处理
        handleData(soB2cEntity, true, true);
        //创建时间
        soB2cEntity.setCreateTime(ObjectUtils.isEmpty(addDTO.getCreateTime()) ? LocalDateTime.now() : addDTO.getCreateTime());
        soB2cEntity.setCode(code);
        log.info("开始新增B2C销售订单表");
        if (StrUtil.isBlank(code)) {
            // 生成单号
            String businessNo = "";
            if (isFullyManagedOrder(dictPlatform)){
                businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSBH);
            }else {
                businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SO_B2C);
            }
            soB2cEntity.setCode(businessNo);
        }
        boolean save = super.save(soB2cEntity);
        if (!save) {
            throw new ServiceException("B2C销售订单表保存失败");
        }
        //计算物流尺寸
        calculateSizeByAdd(addDTO.getLogisticsDTO(), addDTO.getDetailList());
        if (isFullyManagedOrder(dictPlatform)){
            //新增全托管附属信息
            soB2cExtendService.add(addDTO.getExtendDTO(),soB2cEntity);
            //物流信息同步新增运单号
            if (CharSequenceUtil.isBlank(addDTO.getLogisticsDTO().getCode())){
                addDTO.getLogisticsDTO().setCode(addDTO.getLogisticsDTO().getTrackNo());
            }
            if (CharSequenceUtil.isBlank(addDTO.getLogisticsDTO().getTrackNo())){
                addDTO.getLogisticsDTO().setTrackNo(addDTO.getLogisticsDTO().getCode());
            }
        }else {
            if(Objects.isNull(addDTO.getReceiverDTO())){
                throw new ServiceException("订单买家信息不能为空");
            }
            ValidatorUtil.validateEntity(addDTO.getReceiverDTO());
            //如果新增客户
            CustomerB2cEntity cutomer = customerB2cService.getByIdOrName(addDTO.getReceiverDTO().getCustomerId());
            if (Objects.isNull(cutomer)) {
                CustomerB2CDTO.AddDTO dto = buildB2cCustomerAddDTO(addDTO, soB2cEntity.getId());
                try {
                    UserContext.setIsUserSystem(true);
                    String customerId = customerB2cService.add(dto);
                    addDTO.getReceiverDTO().setCustomerId(customerId);
                }finally {
                    UserContext.clearIsUserSystem();
                }
            }
            //新增买家信息
            soB2cReceiverService.add(addDTO.getReceiverDTO(), soB2cEntity);
            //根据渠道和(国家+邮编）判断订单是否超范围配送
            Boolean isOutOfRangeDelivery = estimateIsOutOfRangeDelivery(soB2cEntity.getId(),
                    addDTO.getReceiverDTO().getCountry(),
                    addDTO.getReceiverDTO().getPostCode(),
                    addDTO.getLogisticsDTO().getLogisticsChannelId());
            soB2cEntity.setIsOutOfRangeDelivery(isOutOfRangeDelivery);
        }
        //新增物流信息
        soB2cLogisticsService.add(addDTO.getLogisticsDTO(), soB2cEntity.getId());
        //新增明细
        soB2cDetailService.add(addDTO, soB2cEntity.getId());
        //新增财务信息
        addSoB2cFinance(soB2cEntity);
        //新增订单分类
        if (CollectionUtils.isNotEmpty(addDTO.getCategoryIdList())) {
            List<SoB2cRefCategoryDTO.AddDTO> addList = addDTO.getCategoryIdList().stream().map(obj -> new SoB2cRefCategoryDTO.AddDTO(soB2cEntity.getId(), obj)).collect(Collectors.toList());
            soB2cRefCategoryService.add(addList, soB2cEntity.getId());
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C销售订单表", soB2cEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "新增操作");
        Map<String, Object> map = new HashMap<>();
        //明细信息
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        soB2cEntity.setVersion(0);
        return soB2cEntity;
    }

    /**
     * 判断是否是全托管订单
     * @param platform
     * @return
     */
    @Override
    public boolean isFullyManagedOrder(String platform) {
        List<DictBasicDTO.ViewDTO> dtoList = dictBasicService.getByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
        //是否包含对应的销售平台
        return dtoList.stream().map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList()).contains(platform);
    }

    private CustomerB2CDTO.AddDTO buildB2cCustomerAddDTO(SoB2cDTO.AddDTO addDTO, String id) {
        SoB2cReceiverDTO.AddDTO receiverDTO = addDTO.getReceiverDTO();
        CustomerB2CDTO.AddDTO add = CustomerInfoConverter.INSTANCE.soB2cAddToCustomerBase(addDTO, id);
        add.setApproveStatus(ApproveStatusEnum.APPROVE);
        add.setApproveUserName(UserStateConstants.USER_SYSTEM);
        //联系人信息
        CustomerContactDTO.AddDTO contact = CustomerInfoConverter.INSTANCE.soB2cAddReceiveToContact(receiverDTO);
        add.setContactList(Collections.singletonList(contact));
        //地址信息
        CustomerAddressDTO.AddDTO address = CustomerInfoConverter.INSTANCE.soB2cAddReceiveToAddress(receiverDTO);
        address.setAddress(receiverDTO.getFirstAddress() + receiverDTO.getSecondAddress() + receiverDTO.getFullAddress());
        add.setAddressList(Collections.singletonList(address));
        return add;
    }

    private CustomerB2CDTO.AddDTO buildB2cCustomerUpdateDTO(SoB2cDTO.UpdateDTO updateDTO) {
        SoB2cReceiverDTO.UpdateDTO receiverDTO = updateDTO.getReceiverDTO();
        CustomerB2CDTO.AddDTO add = CustomerInfoConverter.INSTANCE.soB2cUpdateToCustomerBase(updateDTO);
        add.setApproveStatus(ApproveStatusEnum.APPROVE);
        add.setApproveUserName(UserStateConstants.USER_SYSTEM);
        //联系人信息
        CustomerContactDTO.AddDTO contact = CustomerInfoConverter.INSTANCE.soB2cUpdateReceiveToContact(receiverDTO);
        add.setContactList(Collections.singletonList(contact));
        //地址信息
        CustomerAddressDTO.AddDTO address = CustomerInfoConverter.INSTANCE.soB2cUpdateReceiveToAddress(receiverDTO);
        address.setAddress(receiverDTO.getFirstAddress() + receiverDTO.getSecondAddress() + receiverDTO.getFullAddress());
        add.setAddressList(Collections.singletonList(address));
        return add;
    }

    /**
     * 计算物流尺寸
     *
     * @param logisticsDTO
     * @param detailList
     */
    private void calculateSizeByAdd(SoB2cLogisticsDTO.AddDTO logisticsDTO, List<SoB2cDetailDTO.AddDTO> detailList) {
        if (Objects.isNull(logisticsDTO) || CollectionUtils.isEmpty(detailList)) {
            return;
        }
        if ((Objects.nonNull(logisticsDTO.getLength()) && logisticsDTO.getLength().compareTo(BigDecimal.ZERO) != 0)
                && (Objects.nonNull(logisticsDTO.getWidth()) && logisticsDTO.getWidth().compareTo(BigDecimal.ZERO) != 0)
                && (Objects.nonNull(logisticsDTO.getHeight()) && logisticsDTO.getHeight().compareTo(BigDecimal.ZERO) != 0)
                && (Objects.nonNull(logisticsDTO.getWeight()) && logisticsDTO.getWeight().compareTo(BigDecimal.ZERO) != 0)) {
            return;//存在则不重算
        }
        List<String> skuIds = detailList.stream().map(SoB2cDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        //根据明细进行sku拆分
        List<SplitSkuDTO> splitSkuDTOS = this.splitBySoDetail(B2cOrderConverter.INSTANCE.convertAddToDetail(detailList), skuIds, null, true);
        //拆分完成后根据拆分结果进行汇总
        List<String> keyList = new ArrayList<>();
        keyList.add(CalculateSizeEnum.LENGTH.getCode());
        keyList.add(CalculateSizeEnum.WIDTH.getCode());
        keyList.add(CalculateSizeEnum.HEIGHT.getCode());
        keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
        List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
        Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
        if (Objects.isNull(logisticsDTO.getLength()) || logisticsDTO.getLength().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setLength(SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS, collect.get(CalculateSizeEnum.LENGTH.getCode())));
        }
        if (Objects.isNull(logisticsDTO.getWidth()) || logisticsDTO.getWidth().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setWidth(SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS, collect.get(CalculateSizeEnum.WIDTH.getCode())));
        }
        if (Objects.isNull(logisticsDTO.getHeight()) || logisticsDTO.getHeight().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setHeight(SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS, collect.get(CalculateSizeEnum.HEIGHT.getCode())));
        }
        if (Objects.isNull(logisticsDTO.getWeight()) || logisticsDTO.getWeight().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setWeight(SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode())));
        }
    }

    /**
     * 计算物流尺寸
     *
     * @param logisticsDTO
     * @param detailList
     */
    private void calculateSizeByUpdate(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, List<SoB2cDetailDTO.UpdateDTO> detailList) {
        if (Objects.isNull(logisticsDTO) || CollectionUtils.isEmpty(detailList)) {
            return;
        }
        if ((Objects.nonNull(logisticsDTO.getLength()) && logisticsDTO.getLength().compareTo(BigDecimal.ZERO) != 0)
                && (Objects.nonNull(logisticsDTO.getWidth()) && logisticsDTO.getWidth().compareTo(BigDecimal.ZERO) != 0)
                && (Objects.nonNull(logisticsDTO.getHeight()) && logisticsDTO.getHeight().compareTo(BigDecimal.ZERO) != 0)
                && (Objects.nonNull(logisticsDTO.getWeight()) && logisticsDTO.getWeight().compareTo(BigDecimal.ZERO) != 0)) {
            return;//存在则不重算
        }
        List<String> skuIds = detailList.stream().map(SoB2cDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        //根据明细进行sku拆分
        List<SplitSkuDTO> splitSkuDTOS = this.splitBySoDetail(B2cOrderConverter.INSTANCE.convertUpdateToDetail(detailList), skuIds, null, true);
        //拆分完成后根据拆分结果进行汇总
        List<String> keyList = new ArrayList<>();
        keyList.add(CalculateSizeEnum.LENGTH.getCode());
        keyList.add(CalculateSizeEnum.WIDTH.getCode());
        keyList.add(CalculateSizeEnum.HEIGHT.getCode());
        keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
        List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
        Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
        if (Objects.isNull(logisticsDTO.getLength()) || logisticsDTO.getLength().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setLength(SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS, collect.get(CalculateSizeEnum.LENGTH.getCode())));
        }
        if (Objects.isNull(logisticsDTO.getWidth()) || logisticsDTO.getWidth().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setWidth(SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS, collect.get(CalculateSizeEnum.WIDTH.getCode())));
        }
        if (Objects.isNull(logisticsDTO.getHeight()) || logisticsDTO.getHeight().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setHeight(SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS, collect.get(CalculateSizeEnum.HEIGHT.getCode())));
        }
        if (Objects.isNull(logisticsDTO.getWeight()) || logisticsDTO.getWeight().compareTo(BigDecimal.ZERO) == 0) {
            logisticsDTO.setWeight(SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode())));
        }
    }

    @Override
    public List<SplitSkuDTO> getTransferDeclareProductBySoInfo(String soId) {
        SoB2cEntity soB2cEntity = this.getById(soId);
        if (ObjectUtils.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<SoB2cDetailEntity> soB2cDetailEntities = soB2cDetailService.listByMainId(soId);
        if (CollectionUtils.isEmpty(soB2cDetailEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        return splitBySoDetail(soB2cDetailEntities, null, soB2cEntity.getCode(), true);
    }

    @Override
    public List<SplitSkuDTO> getTransferDeclareProductBySoIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        List<SoB2cEntity> soB2cEntityList = this.listByIds(soIds);
        List<SoB2cDetailEntity> allSoB2cDetailEntities = soB2cDetailService.listByMainIds(soIds);
        //检查产品申报信息是否存在，存在就覆盖计算的值
        List<SoB2cDeclareProductDTO.ViewDTO> declareProductList = soB2cDeclareProductService.listViewBySoIds(soIds);
        return splitByBatchSoDetail(allSoB2cDetailEntities, soB2cEntityList, declareProductList,true);
    }

    private List<SplitSkuDTO> splitByBatchSoDetail(List<SoB2cDetailEntity> soB2cDetailEntities, List<SoB2cEntity> soB2cEntityList, List<SoB2cDeclareProductDTO.ViewDTO> declareProductList, boolean judgeCombinationFlag) {
        if (CollectionUtils.isEmpty(soB2cDetailEntities)) {
            return Collections.emptyList();
        }
        //组建订单映射
        Map<String, String> orderMap = soB2cEntityList.stream().collect(Collectors.toMap(SoB2cEntity::getId, SoB2cEntity::getCode));
        List<SplitSkuDTO> splitSkuDTOS = new ArrayList<>();
        List<String> skuIds = soB2cDetailEntities.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        //子sku列表
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //合并 子sku和父级sku获取 全量sku明细
        if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)) {
            skuIds = Stream.concat(skuIds.stream(), bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).filter(StrUtil::isNotEmpty))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(skuIds)){
            return splitSkuDTOS;
        }
        //全量sku的产品明细
        List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listLogisticsProduct(skuIds);
        //sku 产品物流信息map
        Map<String, LogisticsProductDTO.ProductDTO> skuMap = skuInfoList.stream().collect(Collectors.toMap(LogisticsProductDTO.ProductDTO::getSkuId, Function.identity()));
        //sku 父子 map
        Map<String, List<BomChildrenSkuDTO>> skuChildMap = bomChildrenSkuDTOS.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        soB2cDetailEntities.forEach(soB2cDetailEntity -> {
            BomChildrenSkuDTO skuDTO = bomChildrenSkuDTOS.stream().filter(e -> StrUtil.isNotEmpty(e.getParentSkuId())
                            && StrUtil.isNotEmpty(e.getParentSkuNo()) && e.getParentSkuId().equals(soB2cDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            LogisticsProductDTO.ProductDTO productDTO = skuMap.get(soB2cDetailEntity.getSkuId());

            Boolean isCombination = Boolean.FALSE;
            //检查sku是否是组合产品
            if (Objects.nonNull(productDTO) && CombinationDeclareTypeEnums.SPLIT.getCode().equals(productDTO.getCombinationDeclareType())) {
                //申报类型
                isCombination = Boolean.TRUE;
            }
            if (Objects.nonNull(skuDTO) && BomTypeEnum.COMBINATION.getType().equals(skuDTO.getType()) && ( isCombination||!judgeCombinationFlag)){
                if (StrUtil.isNotEmpty(soB2cDetailEntity.getSkuId()) && CollectionUtils.isNotEmpty(skuChildMap.get(soB2cDetailEntity.getSkuId()))){
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS1 = skuChildMap.get(soB2cDetailEntity.getSkuId());
                    //计算比例 汇总 子件 比例数量*实际含税成本 之和
                    BigDecimal totalPrice = bomChildrenSkuDTOS1.stream().map(e ->{
                        LogisticsProductDTO.ProductDTO productDTO1 = skuMap.get(e.getSkuId());
                        Integer quantity = Objects.nonNull(e.getQuantity()) ? e.getQuantity() : 0;
                        BigDecimal actualTaxCost = Objects.nonNull(productDTO1.getActualTaxCost()) ? productDTO1.getActualTaxCost() : BigDecimal.ZERO;
                        return MathUtil.multiplyWithTwo(actualTaxCost,quantity);
                    }).reduce(BigDecimal.ZERO,BigDecimal::add);
                    bomChildrenSkuDTOS1.forEach(bomChildrenSkuDTO -> {
                        LogisticsProductDTO.ProductDTO bomProduct = skuMap.get(bomChildrenSkuDTO.getSkuId());
                        BigDecimal price1 = BigDecimal.ZERO;
                        SoB2cDeclareProductDTO.ViewDTO viewDTO = declareProductList.stream().filter(e -> Objects.nonNull(e) && Objects.nonNull(e.getSkuId())
                                && e.getSkuId().equals(bomChildrenSkuDTO.getSkuId()) && e.getSoId().equals(soB2cDetailEntity.getMainId()) && e.getSoDetailId().equals(soB2cDetailEntity.getId())).findFirst().orElse(null);
                        if (Objects.nonNull(viewDTO) && viewDTO.getPrice().compareTo(BigDecimal.ZERO) > 0){
                            //如果存在申报价格 则使用申报价格
                            price1 = viewDTO.getPrice();
                        }else {
                            Integer quantity = Objects.nonNull(bomChildrenSkuDTO.getQuantity()) ? bomChildrenSkuDTO.getQuantity() : 0;
                            BigDecimal actualTaxCost = Objects.nonNull(bomProduct.getActualTaxCost()) ? bomProduct.getActualTaxCost() : BigDecimal.ZERO;
                            BigDecimal price = Objects.nonNull(soB2cDetailEntity.getPrice()) ? soB2cDetailEntity.getPrice() : BigDecimal.ZERO;
                            price1 = MathUtil.divide(MathUtil.multiplyWithTwo(actualTaxCost,quantity), totalPrice).multiply(price);
                        }
                        splitSkuDTOS.add(SplitSkuDTO.builder()
                                .soId(soB2cDetailEntity.getMainId())
                                .soCode(orderMap.get(soB2cDetailEntity.getMainId()))
                                .skuId(bomChildrenSkuDTO.getSkuId())
                                .skuNo(bomChildrenSkuDTO.getSkuNo())
                                .soDetailId(soB2cDetailEntity.getId())
                                .qty(soB2cDetailEntity.getQty() * bomChildrenSkuDTO.getQuantity())
                                .weight(Objects.nonNull(bomProduct) ? bomProduct.getWeight() : 0)
                                .grossWeight(Objects.nonNull(bomProduct) ? bomProduct.getGrossWeight() : BigDecimal.ZERO)
                                .declareCurrencySymbol(Objects.nonNull(bomProduct) ? bomProduct.getDeclareCurrencySymbol() : "")
                                .isElectric(Objects.nonNull(bomProduct) ? bomProduct.getIsElectric(): Boolean.FALSE)
                                .declareChineseName(Objects.nonNull(bomProduct) ? bomProduct.getDeclareChineseName() : "")
                                .declareEnglishName(Objects.nonNull(bomProduct) ? bomProduct.getDeclareEnglishName() : "")
                                .declarePrice(Objects.nonNull(bomProduct) ? bomProduct.getDeclarePrice() : BigDecimal.ZERO)
                                .destDeclarePrice(Objects.nonNull(bomProduct) ? bomProduct.getDestDeclarePrice() : BigDecimal.ZERO)
                                .destCurrency(Objects.nonNull(bomProduct) ? bomProduct.getDestCurrency() : "")
                                .customsCode(Objects.nonNull(bomProduct) ? bomProduct.getCustomsCode() : "")
                                .declareUnit(Objects.nonNull(bomProduct) ? bomProduct.getDeclareUnit() : "")
                                .declareModel(Objects.nonNull(bomProduct) ? bomProduct.getDeclareModel() : "")
                                .declareElement(Objects.nonNull(bomProduct) ? bomProduct.getDeclareElement() : "")
                                .englishMaterial(Objects.nonNull(bomProduct) ? bomProduct.getEnglishMaterial() : "")
                                .englishUsage(Objects.nonNull(bomProduct) ? bomProduct.getEnglishUsage() : "")
                                .declareCurrency(Objects.nonNull(bomProduct) ? bomProduct.getDeclareCurrency() : "")
                                .currencySymbol(Objects.nonNull(bomProduct) ? bomProduct.getDestCurrencySymbol() : "")
                                .exemption(Objects.nonNull(bomProduct) ? bomProduct.getExemption() : "")
                                .sourceCargo(Objects.nonNull(bomProduct) ? bomProduct.getSourceCargo() : "")
                                .sourceCountry(Objects.nonNull(bomProduct) ? bomProduct.getSourceCountry() : "")
                                .combinationDeclareType(Objects.nonNull(bomProduct) ? bomProduct.getCombinationDeclareType() : "")
                                .productProperty(Objects.nonNull(bomProduct) ? bomProduct.getProductProperty() : "")
                                .productPropertyId(Objects.nonNull(bomProduct) ? bomProduct.getProductPropertyId() : "")
                                .length(Objects.nonNull(bomProduct) ? LengthConverterUtil.mmToCm(bomProduct.getProductLength()) : BigDecimal.ZERO)
                                .width(Objects.nonNull(bomProduct) ? LengthConverterUtil.mmToCm(bomProduct.getProductWidth()) : BigDecimal.ZERO)
                                .height(Objects.nonNull(bomProduct) ? LengthConverterUtil.mmToCm(bomProduct.getProductHeight()) : BigDecimal.ZERO)
                                .currency(soB2cDetailEntity.getCurrency())
                                .price(price1)
                                .build());
                    });
                }
            }else {
                BigDecimal price1 = BigDecimal.ZERO;
                SoB2cDeclareProductDTO.ViewDTO viewDTO = declareProductList.stream().filter(e -> Objects.nonNull(e) && Objects.nonNull(e.getSkuId())
                        && e.getSkuId().equals(soB2cDetailEntity.getSkuId()) && e.getSoId().equals(soB2cDetailEntity.getMainId()) && e.getSoDetailId().equals(soB2cDetailEntity.getId())).findFirst().orElse(null);
                if (Objects.nonNull(viewDTO) && viewDTO.getPrice().compareTo(BigDecimal.ZERO) > 0){
                    //如果存在申报价格 则使用申报价格
                    price1 = viewDTO.getPrice();
                }else {
                    price1 = soB2cDetailEntity.getPrice();
                }
                splitSkuDTOS.add(SplitSkuDTO.builder()
                        .soId(soB2cDetailEntity.getMainId())
                        .soCode(orderMap.get(soB2cDetailEntity.getMainId()))
                        .skuId(soB2cDetailEntity.getSkuId())
                        .skuNo(soB2cDetailEntity.getSkuNo())
                        .soDetailId(soB2cDetailEntity.getId())
                        .qty(soB2cDetailEntity.getQty())
                        .weight(Objects.nonNull(productDTO) ? productDTO.getWeight() : 0)
                        .grossWeight(Objects.nonNull(productDTO) ? productDTO.getGrossWeight() : BigDecimal.ZERO)
                        .declareCurrencySymbol(Objects.nonNull(productDTO) ? productDTO.getDeclareCurrencySymbol() : "")
                        .isElectric(Objects.nonNull(productDTO) ? productDTO.getIsElectric(): Boolean.FALSE)
                        .declareChineseName(Objects.nonNull(productDTO) ? productDTO.getDeclareChineseName() : "")
                        .declareEnglishName(Objects.nonNull(productDTO) ? productDTO.getDeclareEnglishName() : "")
                        .declarePrice(Objects.nonNull(productDTO) ? productDTO.getDeclarePrice() : BigDecimal.ZERO)
                        .destDeclarePrice(Objects.nonNull(productDTO) ? productDTO.getDestDeclarePrice() : BigDecimal.ZERO)
                        .destCurrency(Objects.nonNull(productDTO) ? productDTO.getDestCurrency() : "")
                        .customsCode(Objects.nonNull(productDTO) ? productDTO.getCustomsCode() : "")
                        .declareUnit(Objects.nonNull(productDTO) ? productDTO.getDeclareUnit() : "")
                        .declareModel(Objects.nonNull(productDTO) ? productDTO.getDeclareModel() : "")
                        .declareElement(Objects.nonNull(productDTO) ? productDTO.getDeclareElement() : "")
                        .englishMaterial(Objects.nonNull(productDTO) ? productDTO.getEnglishMaterial() : "")
                        .englishUsage(Objects.nonNull(productDTO) ? productDTO.getEnglishUsage() : "")
                        .declareCurrency(Objects.nonNull(productDTO) ? productDTO.getDeclareCurrency() : "")
                        .currencySymbol(Objects.nonNull(productDTO) ? productDTO.getDestCurrencySymbol() : "")
                        .exemption(Objects.nonNull(productDTO) ? productDTO.getExemption() : "")
                        .sourceCargo(Objects.nonNull(productDTO) ? productDTO.getSourceCargo() : "")
                        .sourceCountry(Objects.nonNull(productDTO) ? productDTO.getSourceCountry() : "")
                        .combinationDeclareType(Objects.nonNull(productDTO) ? productDTO.getCombinationDeclareType() : "")
                        .productProperty(Objects.nonNull(productDTO) ? productDTO.getProductProperty() : "")
                        .productPropertyId(Objects.nonNull(productDTO) ? productDTO.getProductPropertyId() : "")
                        .length(Objects.nonNull(productDTO) ? LengthConverterUtil.mmToCm(productDTO.getProductLength()) : BigDecimal.ZERO)
                        .width(Objects.nonNull(productDTO) ? LengthConverterUtil.mmToCm(productDTO.getProductWidth()) : BigDecimal.ZERO)
                        .height(Objects.nonNull(productDTO) ? LengthConverterUtil.mmToCm(productDTO.getProductHeight()) : BigDecimal.ZERO)
                        .price(price1)
                        .currency(soB2cDetailEntity.getCurrency())
                        .build());
            }
        });
        return splitSkuDTOS;
    }

    /**
     * 申报信息拆分
     *
     * @param soB2cDetailEntities
     * @param soCode
     * @param judgeCombinationFlag
     * @return
     */
    @Override
    public List<SplitSkuDTO> splitBySoDetail(List<SoB2cDetailEntity> soB2cDetailEntities, List<String> skuIds, String soCode, boolean judgeCombinationFlag) {
        if (CollectionUtils.isEmpty(soB2cDetailEntities)) {
            return Collections.emptyList();
        }
        List<SplitSkuDTO> splitSkuDTOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(skuIds)) {
            //skuIds传值时，根据skuIds进行查询，否则取明细列表数据
            skuIds = soB2cDetailEntities.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        }
        //子sku列表
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //合并 子sku和父级sku获取 全量sku明细
        if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)) {
            skuIds = Stream.concat(skuIds.stream(), bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).filter(StrUtil::isNotEmpty))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(skuIds)) {
            return splitSkuDTOS;
        }
        //全量sku的产品明细
        List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listLogisticsProduct(skuIds);
        //sku 产品物流信息map
        Map<String, LogisticsProductDTO.ProductDTO> skuMap = skuInfoList.stream().collect(Collectors.toMap(LogisticsProductDTO.ProductDTO::getSkuId, Function.identity()));
        //sku 父子 map
        Map<String, List<BomChildrenSkuDTO>> skuChildMap = bomChildrenSkuDTOS.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        soB2cDetailEntities.forEach(soB2cDetailEntity -> {
            BomChildrenSkuDTO skuDTO = bomChildrenSkuDTOS.stream().filter(e -> StrUtil.isNotEmpty(e.getParentSkuId())
                    && StrUtil.isNotEmpty(e.getParentSkuNo()) && e.getParentSkuId().equals(soB2cDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            LogisticsProductDTO.ProductDTO productDTO = skuMap.get(soB2cDetailEntity.getSkuId());

            Boolean isCombination = Boolean.FALSE;
            //检查sku是否是组合产品
            if (Objects.nonNull(productDTO) && CombinationDeclareTypeEnums.SPLIT.getCode().equals(productDTO.getCombinationDeclareType())) {
                //申报类型
                isCombination = Boolean.TRUE;
            }
            if (Objects.nonNull(skuDTO) && BomTypeEnum.COMBINATION.getType().equals(skuDTO.getType()) && (isCombination || !judgeCombinationFlag)) {
                if (Objects.nonNull(skuChildMap) && StrUtil.isNotEmpty(soB2cDetailEntity.getSkuId()) && CollectionUtils.isNotEmpty(skuChildMap.get(soB2cDetailEntity.getSkuId()))) {
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS1 = skuChildMap.get(soB2cDetailEntity.getSkuId());
                    bomChildrenSkuDTOS1.forEach(bomChildrenSkuDTO -> {
                        LogisticsProductDTO.ProductDTO bomProduct = skuMap.get(bomChildrenSkuDTO.getSkuId());
                        splitSkuDTOS.add(SplitSkuDTO.builder()
                                .soId(soB2cDetailEntity.getMainId())
                                .soCode(soCode)
                                .skuId(bomChildrenSkuDTO.getSkuId())
                                .skuNo(bomChildrenSkuDTO.getSkuNo())
                                .soDetailId(soB2cDetailEntity.getId())
                                .qty(soB2cDetailEntity.getQty() * bomChildrenSkuDTO.getQuantity())
                                .weight(Objects.nonNull(bomProduct) ? bomProduct.getWeight() : 0)
                                .grossWeight(Objects.nonNull(bomProduct) ? bomProduct.getGrossWeight() : BigDecimal.ZERO)
                                .declareCurrencySymbol(Objects.nonNull(bomProduct) ? bomProduct.getDeclareCurrencySymbol() : "")
                                .isElectric(Objects.nonNull(bomProduct) ? bomProduct.getIsElectric() : Boolean.FALSE)
                                .declareChineseName(Objects.nonNull(bomProduct) ? bomProduct.getDeclareChineseName() : "")
                                .declareEnglishName(Objects.nonNull(bomProduct) ? bomProduct.getDeclareEnglishName() : "")
                                .declarePrice(Objects.nonNull(bomProduct) ? bomProduct.getDeclarePrice() : BigDecimal.ZERO)
                                .destDeclarePrice(Objects.nonNull(bomProduct) ? bomProduct.getDestDeclarePrice() : BigDecimal.ZERO)
                                .destCurrency(Objects.nonNull(bomProduct) ? bomProduct.getDestCurrency() : "")
                                .customsCode(Objects.nonNull(bomProduct) ? bomProduct.getCustomsCode() : "")
                                .declareUnit(Objects.nonNull(bomProduct) ? bomProduct.getDeclareUnit() : "")
                                .declareModel(Objects.nonNull(bomProduct) ? bomProduct.getDeclareModel() : "")
                                .declareElement(Objects.nonNull(bomProduct) ? bomProduct.getDeclareElement() : "")
                                .englishMaterial(Objects.nonNull(bomProduct) ? bomProduct.getEnglishMaterial() : "")
                                .englishUsage(Objects.nonNull(bomProduct) ? bomProduct.getEnglishUsage() : "")
                                .declareCurrency(Objects.nonNull(bomProduct) ? bomProduct.getDeclareCurrency() : "")
                                .currencySymbol(Objects.nonNull(bomProduct) ? bomProduct.getDestCurrencySymbol() : "")
                                .exemption(Objects.nonNull(bomProduct) ? bomProduct.getExemption() : "")
                                .sourceCargo(Objects.nonNull(bomProduct) ? bomProduct.getSourceCargo() : "")
                                .sourceCountry(Objects.nonNull(bomProduct) ? bomProduct.getSourceCountry() : "")
                                .combinationDeclareType(Objects.nonNull(bomProduct) ? bomProduct.getCombinationDeclareType() : "")
                                .productProperty(Objects.nonNull(bomProduct) ? bomProduct.getProductProperty() : "")
                                .productPropertyId(Objects.nonNull(bomProduct) ? bomProduct.getProductPropertyId() : "")
                                .length(Objects.nonNull(bomProduct) ? LengthConverterUtil.mmToCm(bomProduct.getProductLength()) : BigDecimal.ZERO)
                                .width(Objects.nonNull(bomProduct) ? LengthConverterUtil.mmToCm(bomProduct.getProductWidth()) : BigDecimal.ZERO)
                                .height(Objects.nonNull(bomProduct) ? LengthConverterUtil.mmToCm(bomProduct.getProductHeight()) : BigDecimal.ZERO)
                                .build());
                    });
                }
            } else {
                splitSkuDTOS.add(SplitSkuDTO.builder()
                        .soId(soB2cDetailEntity.getMainId())
                        .soCode(soCode)
                        .skuId(soB2cDetailEntity.getSkuId())
                        .skuNo(soB2cDetailEntity.getSkuNo())
                        .soDetailId(soB2cDetailEntity.getId())
                        .qty(soB2cDetailEntity.getQty())
                        .weight(Objects.nonNull(productDTO) ? productDTO.getWeight() : 0)
                        .grossWeight(Objects.nonNull(productDTO) ? productDTO.getGrossWeight() : BigDecimal.ZERO)
                        .declareCurrencySymbol(Objects.nonNull(productDTO) ? productDTO.getDeclareCurrencySymbol() : "")
                        .isElectric(Objects.nonNull(productDTO) ? productDTO.getIsElectric() : Boolean.FALSE)
                        .declareChineseName(Objects.nonNull(productDTO) ? productDTO.getDeclareChineseName() : "")
                        .declareEnglishName(Objects.nonNull(productDTO) ? productDTO.getDeclareEnglishName() : "")
                        .declarePrice(Objects.nonNull(productDTO) ? productDTO.getDeclarePrice() : BigDecimal.ZERO)
                        .destDeclarePrice(Objects.nonNull(productDTO) ? productDTO.getDestDeclarePrice() : BigDecimal.ZERO)
                        .destCurrency(Objects.nonNull(productDTO) ? productDTO.getDestCurrency() : "")
//                        .currency(Objects.nonNull(productDTO) ? productDTO.getDestCurrency() : "")
                        .customsCode(Objects.nonNull(productDTO) ? productDTO.getCustomsCode() : "")
                        .declareUnit(Objects.nonNull(productDTO) ? productDTO.getDeclareUnit() : "")
                        .declareModel(Objects.nonNull(productDTO) ? productDTO.getDeclareModel() : "")
                        .declareElement(Objects.nonNull(productDTO) ? productDTO.getDeclareElement() : "")
                        .englishMaterial(Objects.nonNull(productDTO) ? productDTO.getEnglishMaterial() : "")
                        .englishUsage(Objects.nonNull(productDTO) ? productDTO.getEnglishUsage() : "")
                        .declareCurrency(Objects.nonNull(productDTO) ? productDTO.getDeclareCurrency() : "")
                        .currencySymbol(Objects.nonNull(productDTO) ? productDTO.getDestCurrencySymbol() : "")
                        .exemption(Objects.nonNull(productDTO) ? productDTO.getExemption() : "")
                        .sourceCargo(Objects.nonNull(productDTO) ? productDTO.getSourceCargo() : "")
                        .sourceCountry(Objects.nonNull(productDTO) ? productDTO.getSourceCountry() : "")
                        .combinationDeclareType(Objects.nonNull(productDTO) ? productDTO.getCombinationDeclareType() : "")
                        .productProperty(Objects.nonNull(productDTO) ? productDTO.getProductProperty() : "")
                        .productPropertyId(Objects.nonNull(productDTO) ? productDTO.getProductPropertyId() : "")
                        .length(Objects.nonNull(productDTO) ? LengthConverterUtil.mmToCm(productDTO.getProductLength()) : BigDecimal.ZERO)
                        .width(Objects.nonNull(productDTO) ? LengthConverterUtil.mmToCm(productDTO.getProductWidth()) : BigDecimal.ZERO)
                        .height(Objects.nonNull(productDTO) ? LengthConverterUtil.mmToCm(productDTO.getProductHeight()) : BigDecimal.ZERO)
                        .price(soB2cDetailEntity.getPrice())
                        .currency(soB2cDetailEntity.getCurrency())
                        .build());
            }
        });
        return splitSkuDTOS;
    }

    /**
     * 检查产品是否备案
     * 返回 不在备案产品的sku no list
     * 如果有 说明未备案
     *
     * @param skuNoList
     */
    private List<String> listNotSkuRegistration(List<String> skuNoList, String declarePlatform) {
        if (CollectionUtils.isNotEmpty(skuNoList)) {
            SettingForecastDTO.CheckRegistrationDTO checkRegistration = new SettingForecastDTO.CheckRegistrationDTO();
            checkRegistration.setSkuNoList(skuNoList);
            checkRegistration.setDeclarePlatform(declarePlatform);
            return forecastFeign.listNotRegistrationByParam(checkRegistration);
        }
        return Collections.emptyList();
    }


    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO update(SoB2cDTO.UpdateDTO updateDTO) {
        SoB2cEntity old = super.getById(updateDTO.getId());
        isExist(old);
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(updateDTO.getShopId());
        if(Objects.nonNull(shopInfoEntity) && shopInfoEntity.getDisabled()){
            throw new ServiceException("店铺已禁用，无法修改");
        }
        //未付款数据不能编辑
        soB2cCoreService.checkPayMent(old);

        SoB2cEntity soB2cEntity = BeanMapperUtils.map(SoB2cEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cEntity, true, true);

        log.info("编辑 开始修改B2C销售订单表数据，单号：【{}】", old.getCode());

        boolean save = super.updateById(soB2cEntity);
        if (!save) {
            throw new ServiceException("B2C销售订单表保存失败");
        }
        //清空付款时间
        cleanTime(soB2cEntity);

        String dictPlatform = soB2cEntity.getDictPlatform();
        if (isFullyManagedOrder(dictPlatform)){
            soB2cExtendService.update(updateDTO.getExtendDTO(), soB2cEntity);
            //物流信息同步新增运单号
            if (CharSequenceUtil.isBlank(updateDTO.getLogisticsDTO().getCode())){
                updateDTO.getLogisticsDTO().setCode(updateDTO.getLogisticsDTO().getTrackNo());
            }
            if (CharSequenceUtil.isBlank(updateDTO.getLogisticsDTO().getTrackNo())){
                updateDTO.getLogisticsDTO().setTrackNo(updateDTO.getLogisticsDTO().getCode());
            }
        }else {
            //是否新增b2c客户
            CustomerB2cEntity customer = customerB2cService.getByIdOrName(updateDTO.getReceiverDTO().getCustomerId());
            if (Objects.isNull(customer)) {
                CustomerB2CDTO.AddDTO dto = buildB2cCustomerUpdateDTO(updateDTO);
                String customerId = customerB2cService.add(dto);
                updateDTO.getReceiverDTO().setCustomerId(customerId);
            }
            //修改买家信息
            soB2cReceiverService.update(updateDTO.getReceiverDTO(), soB2cEntity);
            //根据渠道和(国家+邮编）判断订单是否超范围配送
            Boolean isOutOfRangeDelivery = estimateIsOutOfRangeDelivery(soB2cEntity.getId(),
                    updateDTO.getReceiverDTO().getCountry(),
                    updateDTO.getReceiverDTO().getPostCode(),
                    updateDTO.getLogisticsDTO().getLogisticsChannelId());
            soB2cEntity.setIsOutOfRangeDelivery(isOutOfRangeDelivery);
        }
        //计算物流尺寸
        calculateSizeByUpdate(updateDTO.getLogisticsDTO(), updateDTO.getDetailList());
        //修改物流信息
        soB2cLogisticsService.update(updateDTO.getLogisticsDTO(), soB2cEntity.getId());

        //修改明细
        soB2cDetailService.update(updateDTO.getDetailList(), soB2cEntity.getId());
        //修改订单分类
        soB2cRefCategoryService.update(updateDTO.getCategoryIdList(), soB2cEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        //店铺信息
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(Arrays.asList(old.getShopId(), soB2cEntity.getShopId()));
        if (CollectionUtils.isNotEmpty(shopList)) {
            String oldShopName = shopList.stream().filter(obj -> obj.getId().equals(old.getShopId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            old.setShopName(oldShopName);
            String newShopName = shopList.stream().filter(obj -> obj.getId().equals(soB2cEntity.getShopId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            soB2cEntity.setShopName(newShopName);
        }
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, soB2cEntity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);

        return BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 清空时间
     */
    private void cleanTime (SoB2cEntity soB2cEntity) {
        if (ObjectUtil.isNotEmpty(soB2cEntity.getPayTime())) {
            return;
        }
        lambdaUpdate().eq(SoB2cEntity::getId,soB2cEntity.getId()).set(SoB2cEntity::getPayTime,null).update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApproveResultDTO submit(SoB2cEntity entity, SoB2cErrorEntity error,SoB2cLogisticsEntity soB2cLogisticsEntity, Boolean isProcess) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2C销售订单表数据");
        }
        if (Objects.nonNull(error)) {
            throw new ServiceException("订单拉取失败，请手动重试刷新订单后操作");
        }
        validateSubmit(entity);
        String id = entity.getId();
        //物流信息
        if (ObjectUtil.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //物流跟踪号存在值则清除异常
        boolean isCleanError = CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getCode());
        if (isCleanError) {
            //清除异常订单的类型和异常订单表数据
            soB2cErrorService.deleteByMainIds(Collections.singletonList(id));
        }

        // 更新单据审核状态
        log.info("提交 开始修改B2C销售订单表状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus(), isCleanError);
        ApproveResultDTO resultDTO = new ApproveResultDTO();
        Boolean isExistProcess = false;
        log.info("提交 开始启动B2C销售订单表流程，id=：【{}】", entity.getId());
        if (isProcess) {
            isExistProcess = startProcess(entity);
        }
        //如果是已拦截或已冻结修改拦截打标识、冻结状态
        if (entity.getIsIntercept() || entity.getIsFrozen()) {
            SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
            if (entity.getIsIntercept()) {
                interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
            }
            if (entity.getIsFrozen()) {
                interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
            }
            interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getId()));
            this.updateIntercept(interceptUpdateOrderDTO);
        }
        //如果有第三方仓出库异常，清除该异常
        if (SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode().equals(entity.getSignOrderError())) {
            soB2cErrorService.removeErrorOrder(entity.getId(), SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode());
        }

        // 记录操作日志
        log.info("提交 开始记录B2C销售订单表日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交操作");
        resultDTO.setId(entity.getId());
        resultDTO.setCode(entity.getCode());
        resultDTO.setMsg(OperationTypeEnum.SUBMIT.getName() + "成功");
        resultDTO.setSuccess(Boolean.TRUE);
        resultDTO.setIsExistProcess(isExistProcess);
        return resultDTO;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto, Boolean isMatch, String ruleName) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SoB2cEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //校验是否被冻结
        if (entity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, entity.getCode());
        }
        //更换sku走审核流
        if (Objects.nonNull(entity.getIsChangeSku()) && entity.getIsChangeSku()) {
            dto.setIsNeedProcess(Boolean.TRUE);
        }
        // 调用流程审核
        approveProcess(entity, dto, isMatch);
        String approveName = ApproveTypeEnum.REJECT.getName();
        if (Objects.nonNull(approveType)) {
            approveName = approveType.getName();
        }
        // 操作日志
        if(dto.getIsSubmitAutoApprove()){
            String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核规则【{}】 审核意见 ：【{}】", "system", entity.getCode(), "B2C销售订单表", approveName, ruleName, dto.getComment());
            operateLogService.addModuleOperateLogBySystem(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "审核操作");
        }else{
            String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核规则【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C销售订单表", approveName, ruleName, dto.getComment());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "审核操作");
        }

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(SoB2cEntity entity, ApproveOneDTO dto, Boolean isMatch) {
        //无需流程则直接更新状态
        if (ObjectUtil.isNotEmpty(dto.getIsNeedProcess()) && !dto.getIsNeedProcess()) {
            soB2cService.approveEnd(dto, entity, isMatch);
            return;
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        if(PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(entity.getDictPlatform())){
            approveDTO.setBusinessKey(SourceTypeEnum.TIK_TOK_FULLY.getCode());
        }else {
            approveDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
        }
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        String userId = userInfo.getUid();
        if (StringUtils.isBlank(userId)) {
            userId = "0";
        }
        approveDTO.setUserId(userId);
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            soB2cService.approveEnd(dto, entity, isMatch);
        }
    }


    /**
     * 作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        // 待提交或审核不通过并且未作废允许作废
        if (SoB2cInvalidTypeEnum.ENUM_MANUAL.equals(soB2cInvalidTypeEnum)) {
            if ((!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
                throw new ServiceException(ApiError.ERROR_98005);
            }
        }
        //未付款数据不能作废
        soB2cCoreService.checkPayMent(entity);

        log.info("作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SoB2cEntity::getInvalidRemark, remark)
                .set(SoB2cInvalidTypeEnum.ENUM_AUTOMATIC.equals(soB2cInvalidTypeEnum), SoB2cEntity::getRemark, remark)
                .set(SoB2cEntity::getInvalidType, soB2cInvalidTypeEnum.getCode())
                .update();

        //同步数帝云
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(id);
//        syncSoB2cService.syncDataToSdy(entity, soB2cDetailEntityList, SyncOperateEnum.OPERATE_DELETE.getCode());

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C销售订单表", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO unInvalid(String id, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        if (InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_INVALID, entity.getCode());
        }
        if (!soB2cInvalidTypeEnum.getCode().equals(entity.getInvalidType())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_INVALID, entity.getCode(), soB2cInvalidTypeEnum.getName());
        }

        //校验是否被冻结
        if (entity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, entity.getCode());
        }

        log.info("反作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .set(SoB2cEntity::getInvalidType, CharSequenceUtil.EMPTY)
                .set(SoB2cEntity::getInvalidRemark, CharSequenceUtil.EMPTY)
                .update();

        //同步数帝云
//        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(id);
//        syncSoB2cService.syncDataToSdy(entity, soB2cDetailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode());

        log.info("反作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反作废操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "反作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UN_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity, Boolean isMatch) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus(), isMatch,dto);
        if (ApproveStatusEnum.APPROVE.getCode().equals(approveStatus.getStatus())){
            try {
                //生成nf-e发票
                UserContext.setIsUserSystem(true);
                cfgInvoiceSettingDetailService.generateNfeInvoice (entity,InvoiceNodeEnum.AFTER_AUDIT.getCode());
            }finally {
                UserContext.clearIsUserSystem();
            }
        }

        //推送到DMP
//        soB2cService.syncOrderToDmp(entity.getId(), SyncOperateEnum.OPERATE_APPROVE.getCode());

        //同步数帝云
//        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(entity.getId());
//        if(CollUtil.isNotEmpty(soB2cDetailEntityList)) {
//        	syncSoB2cService.syncDataToSdy(entity, soB2cDetailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode());
//        }

        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_UPDATE_REMARK);
        }
        //未付款数据不能操作
        soB2cCoreService.checkPayMent(entity);

        this.lambdaUpdate().eq(SoB2cEntity::getId, id).set(SoB2cEntity::getRemark, remark).update(new SoB2cEntity());
        String msg = CharSequenceUtil.format("订单备注由{}变更为{}", entity.getRemark(), remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "修改订单备注");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新订单备注");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateCategory(String id, SoB2cCategoryTypeEnum typeEnum, List<String> categoryIdList) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_UPDATE_CATEGORY);
        }
        //未付款数据不能操作
        soB2cCoreService.checkPayMent(entity);
        //原分类
        String oldCategoryName = "";
        List<SoB2cRefCategoryEntity> list = soB2cRefCategoryService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(list)) {
            oldCategoryName = list.stream().map(SoB2cRefCategoryEntity::getCategoryName).collect(Collectors.joining(","));
        }
        //添加分类
        String newCategoryName = "";
        List<OrderCategoryDetailEntity> categoryList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(categoryIdList)) {
            categoryList = orderCategoryDetailService.listByIds(categoryIdList);
            newCategoryName = categoryList.stream().map(obj -> obj.getName()).collect(Collectors.joining(","));
        }

        String msg = "";
        if (SoB2cCategoryTypeEnum.ENUM_ADD.equals(typeEnum)) {
            addCategory(categoryIdList, id, categoryList);
            msg = "原分类：【{}】，新增分类：【{}】。";
        }
        if (SoB2cCategoryTypeEnum.ENUM_UPDATE.equals(typeEnum)) {
            updateCategory(categoryIdList, id, categoryList);
            msg = "原分类：【{}】，更新分类：【{}】。";
        }
        if (SoB2cCategoryTypeEnum.ENUM_DELETE.equals(typeEnum)) {
            deleteSelectCategory(id, categoryIdList);
            msg = "原分类：【{}】，删除分类。";
        }
        // 记录操作日志
        log.info("更新分类 开始记录B2C销售订单表日志数据，id：【{}】", id);
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, oldCategoryName, newCategoryName), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "修改订单备注");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新订单分类");
    }

    @Override
    public List<SoB2cDTO.ViewSoB2cDistributionDTO> viewSoB2cDistribution(BaseIdsDTO.IdsDTO dto) {
        //B2C销售订单主表信息
        List<SoB2cEntity> list = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //明细信息
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        List<SoB2cDTO.ViewSoB2cDistributionDTO> resultList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailList) {

            SoB2cEntity soB2cEntity = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soB2cDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            SoB2cDTO.ViewSoB2cDistributionDTO viewDTO = new SoB2cDTO.ViewSoB2cDistributionDTO();
            viewDTO.setId(soB2cEntity.getId());
            viewDTO.setDictPlatform(soB2cEntity.getDictPlatform());
            viewDTO.setShopId(soB2cEntity.getShopId());
            viewDTO.setShopName(soB2cEntity.getShopName());
            viewDTO.setDetailId(soB2cDetailEntity.getId());
            viewDTO.setCode(soB2cEntity.getCode());
            viewDTO.setSourceAmount(soB2cEntity.getAmount());
            viewDTO.setSourceCurrency(soB2cEntity.getCurrency());
            viewDTO.setAmount(MathUtil.multiplyWithTwo(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            viewDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            //物流信息
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.stream().filter(obj -> obj.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            viewDTO.setWeight(soB2cLogisticsEntity.getWeight());
            viewDTO.setLogisticsCode(soB2cLogisticsEntity.getCode());
            viewDTO.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());

            //明细信息
            viewDTO.setSkuId(soB2cDetailEntity.getSkuId());

            viewDTO.setSkuNo(soB2cDetailEntity.getSkuNo());
            viewDTO.setWarehouseId(soB2cDetailEntity.getWarehouseId());
            viewDTO.setWarehouseName(soB2cDetailEntity.getWarehouseName());
            viewDTO.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO saveSoB2cDistribution(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto) {
        ValidatorUtil.validateEntity(dto);
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待配货和配货中订单允许配货
        if (!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(entity.getBillStatus())
                && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_DISTRIBUTION, entity.getCode());
        }
        //只有已审核数据支持配货
        if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_APPROVE_NOT_DISTRIBUTION, entity.getCode());
        }
        //明细信息
        List<SoB2cDTO.SaveSoB2cDistributionDetailDTO> detailList = dto.getDetailList().stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), id)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单明细");
        }

        //筛选订单物流渠道 相同订单不能存在多个渠道
        List<String> channelIds = detailList.stream().filter(e -> StringUtils.isNotBlank(e.getLogisticsChannelId())
                && id.equals(e.getId())).map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getLogisticsChannelId)
                .distinct().collect(Collectors.toList());
        if (channelIds.size() > 1) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_HAS_DIFF_CHANNEL_NOT_DISTRIBUTION);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //存在的物流渠道
        String existChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        //存在的物流单 code
        String code = soB2cLogisticsEntity.getCode();

        /**
         * 是否覆盖
         * 是：按照新选择的物流渠道和仓库下推配货中；如果物流方式跟订单已有的物流不一致，清空物流单号信息，且更新明细仓库
         * 否：新选择的物流渠道和仓库只添加到物流方式和仓库为空的订单，已存在物流方式和仓库的订单不做更改
         */
        Boolean isCover = dto.getIsCover();
        String logisticsChannelId = "";
        //定义物流渠道id
        if (CollectionUtils.isNotEmpty(channelIds)) {
            logisticsChannelId = channelIds.get(0);
        }
        //校验订单是否符合渠道黑名单限制
        checkLogisticsChannelBlacklist(entity.getId(), logisticsChannelId, existChannelId);
        //获取检查备案结果
        SettingForecastDTO.CheckRegistrationResultDTO resultDTO = getCheckRegistrationResult(id, logisticsChannelId);
        String packageStatus = resultDTO.getPackageStatus();
        String transferStatus = resultDTO.getTransferStatus();
        entity.setPackageStatus(packageStatus);
        //未备案的sku
        List<String> notRegistrationSkuNoList = resultDTO.getNotRegistrationSkuNoList();
        Boolean isRegistration = CollectionUtils.isEmpty(notRegistrationSkuNoList);
        //表示未备案
        if (!isRegistration) {
            String skuStr = notRegistrationSkuNoList.stream().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.NOT_UPDATE_CHANNEL_BY_NOT_REGISTRATION, skuStr, resultDTO.getDeclarePlatformName(), resultDTO.getLogisticsChannelName());
        }
        //预报成功不支持配货
        if (StringUtils.isNotBlank(existChannelId) && !existChannelId.equals(logisticsChannelId) && TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus())) {
            throw new ServiceException(CharSequenceUtil.format("B2C销售订单【{}】已预报成功不可更换渠道", entity.getCode()));
        }
        if (!TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus())) {
            entity.setTransferStatus(transferStatus);
        }
        boolean isUpdateTransferStatus = !TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus());
        //修改组包和中转状态
        updatePackageAndTransferStatus(id, packageStatus, transferStatus, isRegistration, isUpdateTransferStatus);

        //如果有物流单号 就要去取消
        if (StringUtils.isNotBlank(code) && !Objects.equals(logisticsChannelId,existChannelId) && soB2cLogisticsEntity.getSourceSystem().equals(SoB2cLogisticSourceSystemEnum.THIRD.getCode())) {
            //已存在的渠道为空
            if (StringUtils.isBlank(existChannelId)) {
                throw new ServiceException(ApiError.CANCEL_LOGISTICS_ID_NOT_EXIST);
            }
            //取消物流单
            LogisticsBillDTO.CancelBillDTO cancelBillDTO = LogisticsBillDTO.CancelBillDTO.builder().
                    channelId(existChannelId).transportNo(code).platformCode(entity.getPlatformCode()).
                    referenceNumber(entity.getCode()).orderId(entity.getId()).shopId(entity.getShopId()).build();
            ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(cancelBillDTO);
            //取消失败
            if (!cancelResult.isSuccess() && cancelResult.getCode() != -1) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_CANCEL_FAI, code);
            } else {
                String msg = CharSequenceUtil.format("取消物流单单号成功,单号:【{}/{}】 ", soB2cLogisticsEntity.getCode(), soB2cLogisticsEntity.getTrackNo());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消物流单");
                soB2cLogisticsEntity.setCode("");
                soB2cLogisticsEntity.setTrackNo("");
                //清空面单信息
                soB2cLabelService.deleteByMainIds(Arrays.asList(id));
                soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_LABEL.getCode());
            }
        }
        //重置物流渠道信息
        soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
        if (StringUtils.isBlank(logisticsChannelId)) {
            soB2cLogisticsEntity.setLogisticsChannelName("");
        } else {
            LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(logisticsChannelId);
            if (Objects.isNull(logisticsChannel)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
            }
            soB2cLogisticsEntity.setLogisticsChannelName(logisticsChannel.getName());
        }
        if (!existChannelId.equals(logisticsChannelId)) {
            soB2cLogisticsEntity.setTransferLogisticsSupplierId("");
            soB2cLogisticsEntity.setTransferLogisticsChannelId("");
        }
        //物流信息更新
        soB2cLogisticsService.updateById(soB2cLogisticsEntity);

        if (CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
            //验证渠道下是否设置了仓库
            List<LogisticsChannelWarehouseEntity> list = FeignQuery.create(LogisticsChannelWarehouseEntity.class)
                    .eq(LogisticsChannelWarehouseEntity::getLogisticsChannelId, soB2cLogisticsEntity.getLogisticsChannelId())
                    .list();
            if (CollectionUtils.isEmpty(list)) {
                throw new ServiceException(CharSequenceUtil.format("渠道【{}】未设置仓库，请先设置仓库", soB2cLogisticsEntity.getLogisticsChannelName()));
            }
            //全部指定直接过，部分指定校验仓库是否一致
            if (CharSequenceUtil.equals(list.get(0).getType(), LogisticsChannelWarehouseTypeEnum.ENUM_PART.getCode())) {
                List<String> warehouseIdList = detailList.stream().map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getWarehouseId).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(warehouseIdList)) {
                    throw new ServiceException("B2C销售订单仓库不能为空");
                }
                List<String> channelWarehouseIdList = list.stream().map(LogisticsChannelWarehouseEntity::getWarehouseId).collect(Collectors.toList());
                Boolean isMatch = Boolean.TRUE;
                for (String warehouseId : warehouseIdList) {
                    if (!channelWarehouseIdList.contains(warehouseId)) {
                        isMatch = Boolean.FALSE;
                        break;
                    }
                }
                //如果仓库没匹配上则进行下一条规则的匹配
                if (!isMatch) {
                    throw new ServiceException(CharSequenceUtil.format("订单【{}】仓库和渠道不存在绑定关系，配货失败！", entity.getCode()));
                }
            }
        }
        //明细仓库更新
        soB2cDetailService.updateWarehouseId(entity, detailList, isCover);
        //订单明细数据
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //无仓库明细
        List<SoB2cDetailEntity> notWarehouseList = soB2cDetailList.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseId())).collect(Collectors.toList());

        //只有订单的物流渠道和仓库都有值才会更新状态
        if ((CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getLogisticsChannelId()) || CharSequenceUtil.isNotBlank(logisticsChannelId))
                && CollectionUtils.isEmpty(notWarehouseList)) {
            //配货中
            String billStatus = SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode();

            Boolean isPlatformWarehouseOrder = entity.hasPlatformWarehouseOrder();
            //是
            if (isPlatformWarehouseOrder) {
                billStatus = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
            }
            //销售订单更新
            entity.setBillStatus(billStatus);
            entity.setAbnormalType("");
            entity.setIsMatchLogisticsRule(Boolean.TRUE);
            this.updateById(entity);

            //根据渠道和(国家+邮编）判断订单是否超范围配送
            Boolean isOutOfRangeDelivery = estimateIsOutOfRangeDelivery(entity.getId(), logisticsChannelId);
            entity.setIsOutOfRangeDelivery(isOutOfRangeDelivery);
        }

        //仓库信息
        List<String> warehouseIdList = detailList.stream().map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getWarehouseId)
                .distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);

        //添加操作日志
        for (SoB2cDTO.SaveSoB2cDistributionDetailDTO saveDTO : detailList) {
            SoB2cDetailEntity detailEntity = soB2cDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), saveDTO.getDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            //仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), saveDTO.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            //操作日志
            String msg = "B2C销售订单配货,订单编号【{}】SKU【{}】,物流渠道【{}】,仓库【{}】";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode(), detailEntity.getSkuNo(), soB2cLogisticsEntity.getLogisticsChannelName(), warehouseName), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "手动配货");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动配货");
    }

    /**
     * 检查订单是否符合物流黑名单限制
     * @param soId
     * @param logisticsChannelId
     * @param existChannelId
     */
    @Override
    public void checkLogisticsChannelBlacklist(String soId, String logisticsChannelId, String existChannelId) {
        //渠道为空或相同则不校验
        if (CharSequenceUtil.isBlank(logisticsChannelId) || Objects.equals(logisticsChannelId,existChannelId)) {
            return;
        }
        LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(logisticsChannelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        //买家信息
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soId);
        if(Objects.isNull(receiverEntity)){
            return;
        }
        //渠道黑名单
        List<LogisticsChannelBlacklistEntity> channelBlackList = logisticsFeign.listChannelBlacklist(Collections.singletonList(logisticsChannelId));
        if(CollUtil.isEmpty(channelBlackList)){
            return;
        }
        //国家
        String countryName = receiverEntity.getCountryName();
        //省份
        String provinceName = receiverEntity.getProvinceName();
        //城市
        String cityName = receiverEntity.getCityName();
        //区县
        String districtName = receiverEntity.getDistrictName();
        //校验列表中是否存在相同国家和省份城市数据，存在则抛出异常
        for (LogisticsChannelBlacklistEntity channelBlacklistEntity : channelBlackList) {
            String country = channelBlacklistEntity.getCountryName();
            String province = channelBlacklistEntity.getProvinceName();
            String city = channelBlacklistEntity.getCityName();
            String district = channelBlacklistEntity.getDistrictName();
            //根据国家省份城市区字段是否存在进行判断是否匹配，为空则不进行向下匹配
            // 国家不匹配直接跳过
            if (!CharSequenceUtil.equals(country, countryName)) {
                continue;
            }
            // 层级校验：国家 -> 省份 -> 城市 -> 区县
            if (CharSequenceUtil.isBlank(country) || !CharSequenceUtil.equals(country, countryName)) {
                continue;
            }
            if (CharSequenceUtil.isBlank(province)){
                throw createBlacklistCountryException(logisticsChannel, country);
            }
            if (!Objects.equals(province, provinceName)){
                continue;
            }
            if(CharSequenceUtil.isBlank(city)){
                throw createBlacklistException(logisticsChannel, country, province, city, district);
            }
            if (!Objects.equals(city, cityName)){
                continue;
            }
            if (CharSequenceUtil.isBlank(district)){
                throw createBlacklistException(logisticsChannel, country, province, city, district);
            }
            if (Objects.equals(district, districtName)){
                throw createBlacklistException(logisticsChannel, country, province, city, district);
            }
        }
    }
    private ServiceException createBlacklistException(LogisticsChannelEntity channel,
                                                      String country, String province, String city, String district) {
        return new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_BLACKLIST,
                channel.getName(), country, province, city, district);
    }
    private ServiceException createBlacklistCountryException(LogisticsChannelEntity channel,
                                                      String country) {
        return new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_COUNTRY_BLACKLIST,
                channel.getName(), country);
    }
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO getLogisticsCodeInner(String id, Boolean isDelivery) {
        String message = "";
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        String paramJson = "";
        String returnJson = "";

        if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_LOGISTICS_CODE, entity.getCode());
        }
        //只有已审核数据支持配货
        if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_APPROVE_NOT_DISTRIBUTION, entity.getCode());
        }
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //wildberris平台订单单独处理
        if(PlatformDictEnum.WILDBERRIES.getCode().equals(entity.getDictPlatform())){
            return getWildberrisLogistics(entity,isDelivery);
        }
        String returnId = entity.getId();
        //如果有物流单号 就要去取消
        if (StringUtils.isNotBlank(soB2cLogisticsEntity.getCode())) {
            if (TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus())) {
                String transferName = "";
                if (StringUtils.isNotBlank(soB2cLogisticsEntity.getTransferLogisticsSupplierId())) {
                    TransferLogisticsSupplierEntity transferLogisticsSupplierEntity = transferLogisticsFeign.getLogisticsSupplierById(soB2cLogisticsEntity.getTransferLogisticsSupplierId());
                    if (Objects.nonNull(transferLogisticsSupplierEntity)) {
                        transferName = transferLogisticsSupplierEntity.getSupplierName();
                    }
                }
                throw new ServiceException(CharSequenceUtil.format("订单信息已预报给{}，请取消订单预报后支持重新获取跟踪号", transferName));
            }
            //已存在的渠道为空
            if (StringUtils.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
                throw new ServiceException(ApiError.CANCEL_LOGISTICS_ID_NOT_EXIST);
            }
            //取消物流单
            LogisticsBillDTO.CancelBillDTO cancelBillDTO = LogisticsBillDTO.CancelBillDTO.builder().
                    channelId(soB2cLogisticsEntity.getLogisticsChannelId()).transportNo(soB2cLogisticsEntity.getCode()).platformCode(entity.getPlatformCode()).
                    referenceNumber(entity.getCode()).orderId(entity.getId()).shopId(entity.getShopId()).build();
            ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(cancelBillDTO);
            //取消失败
            if (!cancelResult.isSuccess() && cancelResult.getCode() != -1) {
                soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
                log.error("{}原因是：{}", ApiError.ERROR_SO_B2C_LOGISTICS_CANCEL_FAIL.msg, cancelResult.getMsg());
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_CANCEL_FAIL, entity.getCode());
            }
            //如果取消物流单 则需要清空物流单信息
            String msg = "取消物流单号，修改单号【{}/{}】改为【/】";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, soB2cLogisticsEntity.getCode(), soB2cLogisticsEntity.getTrackNo()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消物流单号");
            soB2cLogisticsService.updateLogisticsCode(soB2cLogisticsEntity.getMainId(), "", "", "", "");
            //清空面单信息
            soB2cLabelService.deleteByMainIds(Arrays.asList(id));
            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_LABEL.getCode());
        }
        //校验是否存在申报信息，不存在则生成
        List<SoB2cDeclareProductEntity> declareList = soB2cDeclareProductService.listBySoId(id);
        if (CollectionUtils.isEmpty(declareList) && !isFullyManagedOrder(entity.getDictPlatform())) {
            declareRule(id, new HashMap<>(), Boolean.FALSE, false);
        }
        try {
            LogisticsBillDTO.GenerateBillDTO generateBillDTO = makeGenerateBillDTO(entity, soB2cLogisticsEntity);
            paramJson = JSONObject.toJSONString(generateBillDTO);
            LogisticsBillDTO.GenerateBillResultDTO resultDTO = null;
            try {
                //货取物流单号
                resultDTO = logisticsBillFeign.generateBill(generateBillDTO);
            } catch (Exception e) {
                message = e.getMessage();
                //如果异常符合以下条件则取拉取最新的oaid信息
                if (message.contains("decryptPrivacy parameter failed")) {
                    AliExpressOrderDetail orderDetail = aliExpressOrderService.getOrderDetailByOrderIdAndShopId(entity.getPlatformCode(), entity.getShopId());
                    if (null == orderDetail) {
                        log.error("【速卖通标记发货】订单【{}】查询订单详情为空", entity.getPlatformCode());
                        throw new ServiceException("查询订单详情为空");
                    }
                    String oaid = orderDetail.getOaid();
                    JSON extendData = JSONObject.parseObject(entity.getExtendData());
                    Map<String, String> map = JSON.toJavaObject(extendData, Map.class);
                    map.put("oaid", oaid);
                    this.lambdaUpdate().eq(SoB2cEntity::getId, id).
                            set(SoB2cEntity::getExtendData, JSONObject.toJSONString(map)).update();
                    generateBillDTO.setOaid(oaid);
                    //货取物流单号
                    resultDTO = logisticsBillFeign.generateBill(generateBillDTO);
                } else {
                    String code = e instanceof ServiceException ? ((ServiceException) e).getCode().toString() : "";
                    //添加异常信息
                    returnId = soB2cErrorService.generateErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode(), message, paramJson, returnJson,code);
                    log.error("销售订单【{}】 获取物流单失败，异常信息{}", entity.getCode(), message);
                    return BatchResultDTO.fail(returnId, entity.getCode(), message);
                }
            }
            if (Objects.isNull(resultDTO)) {
                throw new ServiceException("下物流单失败");
            }
            String trackNo = resultDTO.getTrackNo();
            if (StringUtils.isBlank(trackNo)) {
                trackNo = "";//重置字段保障运单号和跟踪号一致
            }
            String transportNo = resultDTO.getTransportNo();
            String iossTaxNo = resultDTO.getIossTaxNo();
            String declareOrgId = resultDTO.getDeclareOrgId();
            soB2cLogisticsService.updateLogisticsCode(id, transportNo, trackNo,iossTaxNo,declareOrgId);

            //操作日志
            String msg = "获取物流单号成功，单号【{}/{}】，ioss税号【{}】";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, transportNo, trackNo, iossTaxNo), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "获取物流单号");

            //下单成功发送异步请求保存面单
            if (CharSequenceUtil.isNotBlank(trackNo)) {
                LogisticsBillDTO.PrintLogisticsWaybillDTO waybillDTO = getPlatformWaybill(soB2cLogisticsEntity.getLogisticsChannelId(), entity, transportNo);
                mqProducerService.asyncClassMsg(RocketMqTopic.ASYNC_GET_PLATFORM_LABEL_TOPIC, RocketMqTagEnum.ASYNC_GET_PLATFORM_LABEL_TAG.getName(), waybillDTO, IdUtil.simpleUUID());
            }

            this.lambdaUpdate().eq(SoB2cEntity::getId, id).
                    set(SoB2cEntity::getAbnormalType, "").update(new SoB2cEntity());
            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
            if (Boolean.TRUE.equals(isDelivery)) {
                try {
                    UserContext.setIsUserSystem(true);
                    //提交发货
                    submitDelivery(id, "");
                }catch (Exception e){
                    SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                    addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
                    addError.setParamJson("");
                    addError.setReturnJson("");
                    addError.setMainId(id);
                    addError.setMessage(e.getMessage());
                    soB2cErrorService.add(addError);
                }finally {
                    UserContext.clearIsUserSystem();
                }
            }

            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
            if(resultDTO.getIsPlatformShip()){
                //更新平台已标发
                soB2cDetailList.forEach(v->v.setIsSignShipped(true));
                soB2cDetailService.updateBatchById(soB2cDetailList);
            }
            return BatchResultDTO.success(entity.getId(), transportNo, "获取物流单号");
        } catch (Exception e) {
            message = e.getMessage();
            String code = e instanceof ServiceException ? ((ServiceException) e).getCode().toString() : "";
            //添加异常信息
            returnId = soB2cErrorService.generateErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode(), message, paramJson, returnJson,code);
            log.error("销售订单【{}】 获取物流单失败，异常信息{}", entity.getCode(), message);
        }
        return BatchResultDTO.fail(returnId, entity.getCode(), message);
    }

    private BatchResultDTO getWildberrisLogistics(SoB2cEntity entity, Boolean isDelivery) {
        List<WorkflowTaskRecordEntity> workflowTaskRecordEntities = workflowTaskRecordService.listBySourceId(entity.getId(), WorkflowTaskRecordTypeEnum.SO_B2C_GET_LOGISTICS.getCode());
        if(CollectionUtils.isNotEmpty(workflowTaskRecordEntities)){
            WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
            addTaskDTO.setSourceId(entity.getId());
            addTaskDTO.setSourceCode(entity.getCode());
            addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE); //type
            addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.SO_B2C_GET_LOGISTICS);//subType
            addTaskDTO.setTraceId(workflowTaskRecordEntities.get(0).getTraceId());
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, entity.getId(),2);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RuntimeException(StrUtil.format("获取物流单通过发送任务编排MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return BatchResultDTO.success(entity.getId(), "", "重试获取物流信息");
        }
        //自动生成并完成节点功能
        WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
        addTaskDTO.setSourceId(entity.getId());
        addTaskDTO.setSourceCode(entity.getCode());
        addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE); //type
        addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.SO_B2C_GET_LOGISTICS);//subType
        addTaskDTO.setTraceId(MDC.get("traceId"));

        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("errorType", SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
        addTaskDTO.setFirstNodeInputData(map);
        workflowTaskRecordEntities = workflowTaskRecordService.addTask(addTaskDTO);
        if(CollUtil.isEmpty(workflowTaskRecordEntities)){
            throw new ServiceException(ApiError.NOT_EXIST,DictBasicTypeEnum.WORKFLOW_TASK_NODE.getDesc());
        }
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, entity.getId(),2);
        if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
            throw new RuntimeException(StrUtil.format("获取物流单通过发送任务编排MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return BatchResultDTO.success(entity.getId(), "", "获取物流单号任务编排已生成");
    }

    /**
     * 组装打印面单需要的数据
     *
     * @param logisticsChannelId 订单渠道id
     * @param soB2cEntity        订单信息
     * @param transportNo        物流单号
     * @return
     */
    private LogisticsBillDTO.PrintLogisticsWaybillDTO getPlatformWaybill(String logisticsChannelId, SoB2cEntity soB2cEntity, String transportNo) {
        LogisticsBillDTO.PrintLogisticsWaybillDTO printLogisticsWaybill = new LogisticsBillDTO.PrintLogisticsWaybillDTO();
        printLogisticsWaybill.setChannelId(logisticsChannelId);
        printLogisticsWaybill.setB2cSoId(soB2cEntity.getId());
        printLogisticsWaybill.setDeliveryNo(soB2cEntity.getCode());
        printLogisticsWaybill.setShopId(soB2cEntity.getShopId());
        printLogisticsWaybill.setTransportNo(transportNo);
        return printLogisticsWaybill;
    }


    /**
     * 组装生成物流单数据
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-27
     */
    private LogisticsBillDTO.GenerateBillDTO makeGenerateBillDTO(SoB2cEntity entity, SoB2cLogisticsEntity soB2cLogisticsEntity) {
        LogisticsBillDTO.GenerateBillDTO result = new LogisticsBillDTO.GenerateBillDTO();
        String id = entity.getId();
        result.setCurrency(entity.getCurrency());
        LocalDate billDate = entity.getBillDate();
        if (Objects.isNull(billDate)) {
            billDate = LocalDate.now();
        }
        result.setTrackNo(soB2cLogisticsEntity.getTrackNo());
        result.setOrderTime(billDate.atStartOfDay());
        if (StrUtil.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_ID_NOT_EXIST);
        }
        LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(soB2cLogisticsEntity.getLogisticsChannelId());
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_NOT_EXIST);
        }
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        result.setChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        result.setLogisticType(soB2cLogisticsEntity.getLogisticType());
        result.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        result.setOrderId(id);
        String aliExpress = PlatformDictEnum.ALI_EXPRESS.getCode();
        String logisticsPlatform = auth.getLogisticsPlatform();
        Boolean isAliExpress = aliExpress.equals(logisticsPlatform);
        String shopId = entity.getShopId();
        //扩展字段
        String extendData = entity.getExtendData();
        String oaid = "";
        if (StringUtils.isNotBlank(extendData) && extendData.contains("oaid")) {
            oaid = JSONObject.parseObject(extendData).getOrDefault("oaid", "").toString();
        }
        result.setOaid(oaid);
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        if (isAliExpress) {
            result.setOrderCode(entity.getPlatformCode());
            Map<String, Object> extendDataMap = shopInfoEntity.getExtendData();
            //买家id
            String sellerId = String.valueOf(extendDataMap.get("sellerId"));
            result.setTopUserKey(sellerId);
        } else {
            result.setOrderCode(entity.getCode());
        }
        //增加订单类型传递
        result.setOrderType(entity.getSourceType());
        result.setPlatformCode(entity.getPlatformCode());
        result.setShopId(shopId);
        result.setShopName(entity.getShopName());
        result.setIossTaxNo(shopInfoEntity.getIossTaxNo());
        result.setVoecTaxNo(shopInfoEntity.getVoecTaxNo());
        result.setEoriTaxNo(shopInfoEntity.getEoriTaxNo());
        result.setSalesPlatform(entity.getDictPlatform());
        //包裹号 虾皮
        if (PlatformDictEnum.SHOPEE.getCode().equals(entity.getDictPlatform()) && Objects.nonNull(entity.getLabelJson())) {
            JSONObject jsonObject = JSONObject.parseObject(entity.getLabelJson());
            result.setPackageNumber(jsonObject.getString("package_number"));
        }
        //物流id 美客多
        if ((PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(entity.getDictPlatform()) || PlatformDictEnum.MERCADOLIBRE.getCode().equals(entity.getDictPlatform())) && Objects.nonNull(entity.getLabelJson())
                && (PlatformDictEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform) || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform))) {
            JSONObject jsonObject = JSONObject.parseObject(entity.getLabelJson());
            Long shipmentId = jsonObject.getLong("shipmentId");
            if (Objects.isNull(shipmentId)){
                throw new ServiceException("美客多订单物流id不能为空");
            }
            result.setShipmentId(shipmentId);
        }
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth) && isAliExpress) {
            throw new ServiceException(ApiError.SHOP_NOT_AUTH_ERROR);
        }
        if (Objects.nonNull(shopAuth)) {
            result.setToken(shopAuth.getAccessToken());
        }
        if (!isFullyManagedOrder(entity.getDictPlatform())){
            //买家 收货人信息
            SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(id);
            if (Objects.nonNull(receiverEntity)) {
                LogisticsBillDTO.ReceiverDTO receiverDTO = B2cOrderConverter.INSTANCE.convertReceiver(receiverEntity);
                result.setReceiver(receiverDTO);
                //获取申报组织信息
                SysAccountingCompanyEntity companyEntity = getDeclareOrg(entity, receiverEntity,detailList);
                if (Objects.nonNull(companyEntity)) {
                    result.setDeclareOrgId(companyEntity.getId());
                    result.setDeclareOrgName(companyEntity.getCompanyName());
                    result.setUsciCode(companyEntity.getUsciCode());
                }
            }
            //获取申报信息
            List<SoB2cDeclareProductEntity> declareList = soB2cDeclareProductService.listBySoId(id);
            if (CollectionUtils.isEmpty(declareList)) {
                //申报信息不能为空
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_NOT_EXIST, entity.getCode());
            }
            List<LogisticsProductVO> productVOS = new ArrayList<>(declareList.size());
            if(CollectionUtils.isNotEmpty(detailList)){
                result.setPackageId(detailList.get(0).getPlatformPackageId());
            }
            List<String> skuIdList = declareList.stream().map(SoB2cDeclareProductEntity::getSkuId).distinct().collect(Collectors.toList());
            List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listLogisticsProduct(skuIdList);
            declareList.forEach(soB2cDeclareProductEntity -> {
                LogisticsProductDTO.ProductDTO productDTO = skuInfoList.stream().filter(e -> e.getSkuId().equals(soB2cDeclareProductEntity.getSkuId())).findFirst().orElse(new LogisticsProductDTO.ProductDTO());
                SoB2cDetailEntity soB2cDetail = detailList.stream().filter(e -> soB2cDeclareProductEntity.getSoDetailId().equals(e.getId())).findFirst().orElse(new SoB2cDetailEntity());
                LogisticsProductVO productVO = B2cOrderConverter.INSTANCE.convertDeclareProductVOByEntity(soB2cDeclareProductEntity, soB2cDetail, productDTO);
                //速卖通重置参数
                if (isAliExpress) {
                    if (StringUtils.isNotBlank(soB2cDetail.getPlatformSpuNo())) {
                        productVO.setSkuId(soB2cDetail.getPlatformSpuNo());
                        productVO.setSkuNo(soB2cDetail.getPlatformSkuNo());
                        productVOS.add(productVO);
                    }
                } else {
                    productVOS.add(productVO);
                }
            });
            //过滤产品sku信息
            List<LogisticsProductVO> productVOS2 = productVOS.stream().filter(e -> StringUtils.isNotBlank(e.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(productVOS2)) {
                throw new ServiceException(ApiError.ERROR_92152, entity.getCode());
            }
            //TODO 申报信息校验 测试现在没时间，后面使用再放开
//        checkDeclareInfo(productVOS,entity);
            result.setProductVOS(productVOS2);
            LogisticsBillDTO.PackageDTO packageDTO = B2cOrderConverter.INSTANCE.convertPackage(soB2cLogisticsEntity);
            packageDTO.setCurrency(productVOS2.get(0).getDestCurrency());
            result.setPackageInfo(packageDTO);
        }else {
            List<LogisticsProductVO> productVOS = new ArrayList<>(detailList.size());
            detailList.forEach(detail -> {
                LogisticsProductVO productVO = B2cOrderConverter.INSTANCE.convertProductVOByEntity(detail);
                productVOS.add(productVO);
            });
            result.setProductVOS(productVOS);
            LogisticsBillDTO.PackageDTO packageDTO = B2cOrderConverter.INSTANCE.convertPackage(soB2cLogisticsEntity);
            packageDTO.setCurrency(productVOS.get(0).getDestCurrency());
            result.setPackageInfo(packageDTO);
        }
        return result;
    }

    private SysAccountingCompanyEntity getDeclareOrg(SoB2cEntity entity, SoB2cReceiverEntity receiverEntity, List<SoB2cDetailEntity> detailList) {
        // B2C订单根据中转规则判断是否中转
        CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
        ruleDTO.setType(StockOutTransferTypeEnum.B2C.getCode());
        ruleDTO.setReceiveCountry(receiverEntity.getCountry());
        String deliveryWarehouseId = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).filter(CharSequenceUtil::isNotBlank).findFirst().orElse("");
        ruleDTO.setFromWarehouse(deliveryWarehouseId);
        ruleDTO.setSalesOrgId(entity.getOrgId());
        ruleDTO.setDictPlatform(entity.getDictPlatform());
        CfgRuleOutDTO.MatchTransferResultDTO resultDTO = cfgRuleOutFeign.matchTransferRule(ruleDTO);
        if (Objects.isNull(resultDTO) || Boolean.FALSE.equals(resultDTO.getIsTransit()) || CollUtil.isEmpty(resultDTO.getTransferWarehouseIdList())) {
            return null;
        }
        List<String> transferWarehouseIdList = resultDTO.getTransferWarehouseIdList();
        //获取最后一个仓库
        String lastWarehouseId = transferWarehouseIdList.get(transferWarehouseIdList.size() - 1);
        if (CharSequenceUtil.isEmpty(lastWarehouseId)){
            return null;
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(lastWarehouseId));
        if (CollUtil.isEmpty(warehouseList)){
            return null;
        }
        String orgId = warehouseList.get(0).getOrgId();
        if (CharSequenceUtil.isEmpty(orgId)){
            return null;
        }
        return sysUserFeign.getCompanyById(orgId);
    }

    /**
     * 校验字段必填
     *
     * @param productVOS
     * @param entity
     */
    private void checkDeclareInfo(List<LogisticsProductVO> productVOS, SoB2cEntity entity) {
        if (CollectionUtils.isEmpty(productVOS)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_NOT_EXIST, entity.getCode());
        }
        productVOS.forEach(logisticsProductVO -> {
            if (StrUtil.isEmpty(logisticsProductVO.getSkuNo())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_SKU_NO_NOT_EXIST, entity.getCode());
            }
            if (StrUtil.isEmpty(logisticsProductVO.getDeclareChineseName())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_CN_NAME_NOT_EXIST, logisticsProductVO.getSkuNo());
            }
            if (StrUtil.isEmpty(logisticsProductVO.getDeclareEnglishName())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_EN_NAME_NOT_EXIST, logisticsProductVO.getSkuNo());
            }
            if (Objects.isNull(logisticsProductVO.getDestDeclarePrice()) || logisticsProductVO.getDestDeclarePrice().compareTo(BigDecimal.ZERO) < 1) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_TO_DECLARE_PRICE_NOT_EXIST, logisticsProductVO.getSkuNo());
            }
            if (StrUtil.isEmpty(logisticsProductVO.getDestCurrency())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_TO_DECLARE_CUY_NOT_EXIST, logisticsProductVO.getSkuNo());
            }
            if (StrUtil.isEmpty(logisticsProductVO.getDestCurrencySymbol())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_TO_DECLARE_CUY_SYM_NOT_EXIST, logisticsProductVO.getSkuNo());
            }
            if (Objects.isNull(logisticsProductVO.getWeight()) || logisticsProductVO.getWeight() <= 0) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_ORDER_DECLARE_WEIGHT_NOT_EXIST, logisticsProductVO.getSkuNo());
            }
        });
    }

    @Override
    @DistributeLocker(businessType = RedisKeyConstant.SO_B2C_ORDER_KEY, keyName = "id", waiteTime = 60)
    public BatchResultDTO getLogisticsCode(String id, Boolean isDelivery) {
        BatchResultDTO batchResultDTO = soB2cService.getLogisticsCodeInner(id,isDelivery);
        if(!batchResultDTO.getSuccess()){
            Map<String,Map<String, Object>> map = soB2cErrorService.handleMatchJson(Arrays.asList(batchResultDTO.getId()));
            Map<String,RulePromptWordEntity> rulePromptWordEntityMap = soB2cErrorService.getRulePromptWord(map);
            if(rulePromptWordEntityMap.containsKey(batchResultDTO.getId())){
                RulePromptWordEntity rulePromptWordEntity = rulePromptWordEntityMap.get(batchResultDTO.getId());
                batchResultDTO.setMsg(StrUtil.format("失败原因：{},【解决方案】：{}",CharSequenceUtil.isNotBlank(rulePromptWordEntity.getTips()) ? rulePromptWordEntity.getTips() : batchResultDTO.getMsg(),rulePromptWordEntity.getSolution()));
            }
        }
        return batchResultDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submitDelivery(String id, String channelId) {
        try {
            List<BatchResultDTO> batchResultDTOS = this.autoOrderForecast(Collections.singletonList(id));
            //在提交发货中，清除异常订单报错
            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            if (CollUtil.isNotEmpty(batchResultDTOS) && !batchResultDTOS.get(0).getSuccess()){
                SoB2cErrorDTO.AddDTO dto = new SoB2cErrorDTO.AddDTO();
                dto.setMainId(id);
                dto.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
                dto.setMessage(CharSequenceUtil.format("自动预报失败:{}", batchResultDTOS.get(0).getMsg()));
                soB2cErrorService.add(dto);
                return batchResultDTOS.get(0);
            }
        }catch (Exception e){
            log.error("自动预报失败",e);
            throw new ServiceException("自动预报失败:{}",e.getMessage());
        }
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
           return BatchResultDTO.fail(id,entity.getCode(),ApiError.ERROR_SO_B2C_NOT_EXIST.msg);
        }
        //已作废订单不能提交发货
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
            return BatchResultDTO.success(id,entity.getCode(),CharSequenceUtil.format(ApiError.ERROR_SO_B2C_ORDER_STATUS_ERROR.msg, entity.getCode()));
        }
        //全托管并且是平台订单就进行校验状态
        if (isFullyManagedOrder(entity.getDictPlatform()) && SourceTypeEnum.SO_B2C.getCode().equals(entity.getSourceType())){
            //检查平台状态
            List<FullyOrderDTO.DataDTO.StockupOrdersDTO> stockupOrdersDTOS = tikTokFullService.listOrderByParam(entity.getShopId(), Collections.singletonList(entity.getPlatformCode()));
            if (CollUtil.isNotEmpty(stockupOrdersDTOS)) {
                FullyOrderDTO.DataDTO.StockupOrdersDTO stockupOrdersDTO = stockupOrdersDTOS.stream().filter(e -> e.getCode().equals(entity.getPlatformCode())).findFirst().orElse(null);
                String status = Objects.nonNull(stockupOrdersDTO) ? stockupOrdersDTO.getStatus() : "";
                //更新订单平台状态
                if (CharSequenceUtil.isNotBlank(status)){
                    String code = FullyManagedPlatformStatusEnum.getErpCodeByCode(PlatformDictEnum.TIK_TOK_FULLY.getCode(), status);
                    if (!code.equals(entity.getBillStatus())){
                        this.updatePlatformStatus(entity, code);
                    }
                    if (FullyManagedPlatformStatusEnum.TikTokStatusEnum.INVAILD.getCode().equals(status)){
                        throw new ServiceException(ApiError.ERROR_SO_B2C_PLATFORM_ORDER_STATUS_ERROR, entity.getCode());
                    }
                }
            }
        }
        String soCode = entity.getCode();
        if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SUBMIT_DELIVERY, entity.getCode());
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (Objects.isNull(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //判断订单类型 虾皮/美客多 是否符合线上物流类型
        checkLogisticsParam(entity,logisticsEntity);

        ApproveStatusEnum approveStatusEnum = entity.getApproveStatus();
        if (!ApproveStatusEnum.APPROVE.equals(approveStatusEnum)) {
            throw new ServiceException(ApiError.B2C_APPROVE_DELIVERY, entity.getCode());
        }
        if (entity.getIsCancel()) {
            throw new ServiceException(CharSequenceUtil.format("销售订单【{}】平台已取消，不支持发货", entity.getCode()));
        }
        //查询支付方式是否支持继续发货
        Boolean isFlag = soB2cCoreService.listPayMethodSetting(entity);
        //待付款不能提交发货
        if (CharSequenceUtil.equals(entity.getPayStatus(), SoB2cPayStatusEnum.ENUM_PAYMENT.getCode()) && !isFlag) {
            throw new ServiceException(CharSequenceUtil.format("销售订单【{}】未付款，不支持发货", entity.getCode()));
        }

        //校验是冻结
        if (entity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, entity.getCode());
        }

        //校验中转状态
        if (TransferStatusEnum.FAILURE.getCode().equals(entity.getTransferStatus()) || TransferStatusEnum.WAIT.getCode().equals(entity.getTransferStatus())) {
            throw new ServiceException(CharSequenceUtil.format("{}未成功预报无法提交发货", entity.getCode()));
        }

        //物流渠道
        String logisticsChannelId = logisticsEntity.getLogisticsChannelId();
        //物流单号
        String code = logisticsEntity.getCode();
        /**
         * 未获取物流单号或获取物流单号失败的订单不允许提交发货
         * 缺货订单不允许提交发货
         * 存在多发货仓库的，不允许提交发货
         */
        //B2C销售订单明细信息
        List<SoB2cDetailEntity> list = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        List<String> warehouseIdList = list.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseId())).map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(warehouseIdList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DELIVERY_NOT_EXIST_WAREHOUSE);
        }
        //发货仓库id 集合
        List<String> deliveryWarehouseIdList = list.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        if (deliveryWarehouseIdList.size() > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DELIVERY_WAREHOUSE_COMPLEX, soCode);
        }
        String warehouseId = deliveryWarehouseIdList.get(0);
        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> deliveryWarehouseList = wmsTaskFeign.listWarehouseByIds(deliveryWarehouseIdList);
        if (CollectionUtils.isEmpty(deliveryWarehouseList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DELIVERY_NOT_EXIST_WAREHOUSE);
        }
        //仓库经营类型
        String warehouseManageType = deliveryWarehouseList.get(0).getWarehouseManageType();

        //检测是否是API 对接的仓库
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(deliveryWarehouseIdList);
        Boolean isApi = CollectionUtils.isNotEmpty(overseasWarehouseList);
        //必须要有物流渠道，没有物流单号可以提交发货
        if (isApi) {
            if (StringUtils.isBlank(logisticsChannelId)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_ID_NOT_NULL, soCode);
            }
        } else {
            //必须要有物流渠道和物流单号后才可以提交发货
            if (StringUtils.isBlank(logisticsChannelId) || StringUtils.isBlank(code)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_ID_AND_CODE_NOT_NULL, soCode);
            }
        }
        if(PlatformDictEnum.TIK_TOK.getCode().equals(entity.getDictPlatform()) || PlatformDictEnum.ALI_EXPRESS.getCode().equals(entity.getDictPlatform()) || PlatformDictEnum.SHOPIFY.getCode().equals(entity.getDictPlatform()) || PlatformDictEnum.AMAZON.getCode().equals(entity.getDictPlatform())){
            //物流映射列表
            List<LogisticsMappingDTO.ViewDTO> mappingList = logisticsMappingFeign.listByChannelIdAndType(logisticsChannelId, LogisticsMappingTypeEnum.PLATFORM.getCode());
            if(CollectionUtils.isEmpty(mappingList)){
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_MAPPING_NOT_NULL, soCode,entity.getDictPlatform(),logisticsEntity.getLogisticsChannelName());
            }
            List<LogisticsMappingDTO.ViewDTO> collect = mappingList.stream().filter(v -> null != v.getSalesPlatform() && v.getSalesPlatform().equalsIgnoreCase(entity.getDictPlatform())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(collect)){
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_MAPPING_NOT_NULL, soCode,entity.getDictPlatform(),logisticsEntity.getLogisticsChannelName());
            }
        }
        //库存验证
        checkInventory(entity, list, deliveryWarehouseIdList, warehouseManageType);
        //如果是领星订单，更新领星订单信息
        if(entity.getThirdSystem().equals(PlatformDictEnum.LING_XING.getCode())){
            updateLingXingOrder(entity, list);
        }
        String operate = "提交发货";
        /**
         * 如果是API 对接的仓库
         * 下出库单的命令
         */
        if (isApi) {
            //异步处理
            ThirdWarehouseCreateOutboundPushDTO pushDTO = ThirdWarehouseCreateOutboundPushDTO.builder()
                    .soId(entity.getId())
                    .soCode(entity.getCode())
                    .entity(entity)
                    .warehouseId(warehouseId)
                    .warehouseManageType(warehouseManageType)
                    .logisticsEntity(logisticsEntity)
                    .overseasProviderWarehouse(overseasWarehouseList.get(0))
                    .detailList(list)
                    .newChannelId(channelId)
                    .build();
            sendThirdWarehousePushTask(pushDTO);
            //成功更新订单状态
            this.updateBillStatus(entity.getId(), SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED);
            operate = "异步提交发货";
        } else {
            //生成发货单
            generateSoB2cDeliveryBill(entity, list, logisticsEntity, warehouseManageType);
            //删除异常订单信息
            soB2cErrorService.removeAllTypeErrorOrder(id);
            this.updateBillStatus(id, SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED);
            //操作日志
            String msg = "B2C销售订单【{}】提交发货";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交发货");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), operate);
    }

    private void updatePlatformStatus(SoB2cEntity entity, String status) {
        if (Objects.isNull(entity) ||  CharSequenceUtil.isBlank(status)){
            return;
        }
        this.lambdaUpdate().eq(SoB2cEntity::getId,entity.getId()).set(SoB2cEntity::getPlatformOrderStatus,status).update();
        String msg = "销售订单【{}】平台订单状态由【{}】变更为【{}】";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode(), entity.getPlatformOrderStatus(), status), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交发货");

    }

   /**
    * 推送发票
    * @author will
    * @date 2025/4/24 17:42
    * @param entity
    * @param overseasProviderWarehouse
    * @param logisticsChannelEntity
    * @return void
    */
    private void autoPushInvoice (SoB2cEntity entity,OverseasProviderWarehouseDTO.ViewDTO overseasProviderWarehouse,LogisticsChannelEntity logisticsChannelEntity,ThirdWarehouseCreateOutboundReq createOutboundReq) {
        log.warn("推送发票，参数为=========={}", JSONUtil.toJsonStr(createOutboundReq));
        //物流渠道
        if (ObjectUtil.isEmpty(logisticsChannelEntity) || !logisticsChannelEntity.getIsSendInvoice()) {
            return;
        }
        InvoiceInfoDTO.AttachDTO attachDTO = invoiceInfoService.getNewInvoicedAttachBySoId(entity.getId(), InvoiceInfoInvoiceTypeEnum.NFE.getCode(), AttachmentTypeEnum.INVOICE_INFO_PDF.getCode());
        if (ObjectUtil.isEmpty(attachDTO)) {
            return;
        }
        String logisticsBase64 = "";
        try {
             logisticsBase64 = FileUtil.convertPdfUrlToBase64(FastDFSClientUtil.publicUrl + attachDTO.getAttachUrl());
        } catch (Exception e) {
            throw new ServiceException("base64转换失败");
        }
        if (CharSequenceUtil.isBlank(logisticsBase64)) {
            throw new ServiceException("base64不能为空");
        }
        ThirdWarehouseUploadFileReq thirdWarehouseUploadFileReq = new ThirdWarehouseUploadFileReq();
        thirdWarehouseUploadFileReq.setOrderCode(entity.getCode());
        thirdWarehouseUploadFileReq.setFileData(logisticsBase64);
        thirdWarehouseUploadFileReq.setAuthId(overseasProviderWarehouse.getMainId());
        thirdWarehouseUploadFileReq.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
        //速派通采用other_documents_invoice
        if (PlatformDictEnum.SPT.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())) {
            thirdWarehouseUploadFileReq.setModule("other_documents_invoice");
        }
        ApiResult<ThirdWarehouseUploadFileResponse> uploadFileResponse = thirdWarehouseFeign.uploadFile(thirdWarehouseUploadFileReq);
        if(!uploadFileResponse.isSuccess()){
            throw new ServiceException("上传发票失败{}",uploadFileResponse.getMsg());
        }
        Integer attachId = uploadFileResponse.getData().getAttachId();
        List<ThirdWarehouseCreateOutboundReq.Attach> attachList = CollUtil.isEmpty(createOutboundReq.getAttach()) ? new ArrayList<>() : new ArrayList<>(createOutboundReq.getAttach());
        ThirdWarehouseCreateOutboundReq.Attach attach = new ThirdWarehouseCreateOutboundReq.Attach();
        attach.setFileType(FileTypeEnum.PDF.getCode());
        attach.setAttachId(attachId);
        attachList.add(attach);
        createOutboundReq.setAttach(attachList);
        createOutboundReq.setInvoiceData(logisticsBase64);
    }


    private void checkLogisticsParam(SoB2cEntity entity, SoB2cLogisticsEntity logisticsEntity) {
        String labelJson = entity.getLabelJson();
        SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(labelJson, SoB2cDTO.LabelJsonDTO.class);
        String dictPlatform = entity.getDictPlatform();
        String logisticsChannelId = logisticsEntity.getLogisticsChannelId();
        if (CharSequenceUtil.isNotBlank(labelJsonDTO.getDeliveryType()) && !"non_integrated".equals(labelJsonDTO.getDeliveryType()) && PlatformDictEnum.SHOPEE.getCode().equals(dictPlatform)){
            List<String> deliveryType = Arrays.asList("dropoff","pickup");
            if (!deliveryType.contains(labelJsonDTO.getDeliveryType())){
                throw new ServiceException("【{}】发货类型【{}】不支持提交发货",PlatformDictEnum.getNameByCode(dictPlatform),labelJsonDTO.getDeliveryType());
            }
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
            if (Objects.isNull(auth)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
            }
            if (!dictPlatform.equals(auth.getLogisticsPlatform())){
                throw new ServiceException("【{}】线上物流提交发货必须使用线上物流渠道下单",PlatformDictEnum.getNameByCode(dictPlatform));
            }

        }else if (CharSequenceUtil.isNotBlank(labelJsonDTO.getDeliveryType()) && !"default".equals(labelJsonDTO.getDeliveryType()) && PlatformDictEnum.MERCADOLIBRE.getCode().equals(dictPlatform) ){
            List<String> logisticsType = Arrays.asList(MercadoOrderLogisticTypeEnum.DROP_OFF.getCode(),MercadoOrderLogisticTypeEnum.CROSS_DOCKING.getCode());
            if (!logisticsType.contains(labelJsonDTO.getLogisticType())){
                throw new ServiceException("【{}】物流类型【{}】不支持提交发货",PlatformDictEnum.getNameByCode(dictPlatform),labelJsonDTO.getLogisticType());
            }
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
            if (Objects.isNull(auth)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
            }
            if (!dictPlatform.equals(auth.getLogisticsPlatform())){
                throw new ServiceException("【{}】线上物流提交发货必须使用线上物流渠道下单",PlatformDictEnum.getNameByCode(dictPlatform));
            }
        }else if (CharSequenceUtil.isNotBlank(labelJsonDTO.getDeliveryType()) && !"default".equals(labelJsonDTO.getDeliveryType()) && PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(dictPlatform) ){
            List<String> logisticsType = Arrays.asList(MercadoOrderLogisticTypeEnum.DROP_OFF.getCode(),MercadoOrderLogisticTypeEnum.CROSS_DOCKING.getCode());
            if (!logisticsType.contains(labelJsonDTO.getLogisticType())){
                throw new ServiceException("【{}】物流类型【{}】不支持提交发货",PlatformDictEnum.getNameByCode(dictPlatform),labelJsonDTO.getLogisticType());
            }
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
            if (Objects.isNull(auth)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
            }
            if (!dictPlatform.equals(auth.getLogisticsPlatform())){
                throw new ServiceException("【{}】线上物流提交发货必须使用线上物流渠道下单",PlatformDictEnum.getNameByCode(dictPlatform));
            }
        }
    }

    /**
     * @param entity
     * @param list
     * @param deliveryWarehouseIdList
     * @description: 校验库存
     * @author Will
     * @date: 2024/4/22 14:59
     */
    private void checkInventory(SoB2cEntity entity, List<SoB2cDetailEntity> list, List<String> deliveryWarehouseIdList, String warehouseManageType) {

        // 判断是否需要忽略计算库存的sku
        List<String> ignoreInventorySkuIds = this.getIgnoreSkuIds();

        /**
         * 验证是否可用库存
         * 1、销售套装bom则需要判断子件是否存在库存
         * 2、非销售套装bom依然判断产品sku是否存在库存
         */
        List<SoB2cDeliveryDTO.DeliverySkuDTO> deliverySkuList = listDeliverySku(entity.getShopId(), list, entity.getDictPlatform(), warehouseManageType, true);

        //skuId集合
        List<String> skuIdList = deliverySkuList.stream().map(SoB2cDeliveryDTO.DeliverySkuDTO::getSkuId).distinct().collect(Collectors.toList());
        //查询可用库存
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setWarehouseIdList(deliveryWarehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<InventoryQtyDTO.SkuInventoryTotalDTO> inventoryList = inventoryFeign.listSkuInventoryByParam(skuInventoryDTO);

        for (SoB2cDeliveryDTO.DeliverySkuDTO deliverySkuDTO : deliverySkuList) {
            //费用服务类sku不校验库存
            if (ignoreInventorySkuIds.contains(deliverySkuDTO.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", deliverySkuDTO.getSkuId(), deliverySkuDTO.getSkuNo());
                continue;
            }
            SoB2cDetailEntity soB2cDetailEntity = list.stream().filter(v -> v.getId().equals(deliverySkuDTO.getDetailId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cDetailEntity)) {
                throw new ServiceException("发货sku匹配不到明细");
            }

            // 获取仓库信息，检查是否允许负库存
            List<WarehouseDTO.UpdateDTO> warehouseInfoList = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(soB2cDetailEntity.getWarehouseId()));
            WarehouseDTO.UpdateDTO warehouseInfo = CollectionUtils.isNotEmpty(warehouseInfoList) ? warehouseInfoList.get(0) : null;
            if (Objects.isNull(warehouseInfo)) {
                throw new ServiceException("仓库信息不存在，仓库ID：" + soB2cDetailEntity.getWarehouseId());
            }

            // 如果仓库允许负库存，则跳过库存校验
            if (Boolean.TRUE.equals(warehouseInfo.getAllowNegativeInventory())) {
                log.info("仓库【{}】允许负库存，跳过SKU【{}】的库存校验", warehouseInfo.getName(), soB2cDetailEntity.getSkuNo());
                continue;
            }

            //验证是否存在可用库存
            Integer usableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(deliverySkuDTO.getSkuId()) && obj.getWarehouseId().equals(soB2cDetailEntity.getWarehouseId()))
                    .map(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO, Integer::sum);
            Integer deliveryQty = soB2cDetailEntity.getQty() * deliverySkuDTO.getQty();
            if (MathUtil.compareTo(deliveryQty, usableQty) > MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SKU_NOT_INVENTORY, entity.getCode(), soB2cDetailEntity.getSkuNo(), soB2cDetailEntity.getWarehouseName());
            }
        }
    }

    /**
     * 根据仓库经营类型匹配
     * 生成b2c 发货单
     * 如果SKU是销售套装BOM，需要按照子件+数量生成发货单明细
     * 数量=父件销售数量*BOM用量
     *
     * @param entity
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-26 19:48
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void generateSoB2cDeliveryBill(SoB2cEntity entity, List<SoB2cDetailEntity> list, SoB2cLogisticsEntity soB2cLogisticsEntity, String warehouseManageType) {
        SoB2cDeliveryDTO.AddDTO soB2cDelivery = B2cOrderConverter.INSTANCE.convertDelivery(entity);
        soB2cDelivery.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        soB2cDelivery.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        soB2cDelivery.setTransportNo(soB2cLogisticsEntity.getCode());
        if (OrderLogisticTypeEnum.TRANSIT_WAREHOUSE.getCode().equals(soB2cLogisticsEntity.getLogisticType())) {
            soB2cDelivery.setLogisticType(B2cDeliveryLogisticTypeEnum.TRANSIT_SHIPMENT.getCode());
        }

        List<String> skuIdList = list.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<SoB2cDeliveryDetailDTO.AddDTO> deliveryDetailList = new ArrayList<>(list.size());
        List<SoB2cDeliveryDTO.DeliverySkuDTO> deliverySkuList = listDeliverySku(entity.getShopId(), list, entity.getDictPlatform(), warehouseManageType, true);
        for (SoB2cDetailEntity detailItem : list) {
            String skuId = detailItem.getSkuId();
            //数量
            Integer qty = detailItem.getQty();
            String sourceDetailId = detailItem.getId();
            //查询到对应的数据
            List<SoB2cDeliveryDTO.DeliverySkuDTO> deliveryList = deliverySkuList.stream().filter(d -> detailItem.getPlatformSkuNo().equals(d.getPlatformSkuNo()) && skuId.equals(d.getSourceSkuId())).distinct().collect(Collectors.toList());
            //表示有啊
            if (CollectionUtils.isNotEmpty(deliveryList)) {
                for (SoB2cDeliveryDTO.DeliverySkuDTO deliverySku : deliveryList) {
                    SoB2cDeliveryDetailDTO.AddDTO addDTO = new SoB2cDeliveryDetailDTO.AddDTO();
                    addDTO.setSkuId(deliverySku.getSkuId());
                    addDTO.setSkuNo(deliverySku.getSkuNo());
                    addDTO.setSourceDetailId(sourceDetailId);
                    addDTO.setWarehouseId(deliverySku.getWarehouseId());
                    addDTO.setWarehouseName(detailItem.getWarehouseName());
                    addDTO.setWarehouseLocation(detailItem.getWarehouseLocation());
                    addDTO.setVirtualWarehouseId(detailItem.getVirtualWarehouseId());
                    addDTO.setDeliveryQty(qty * deliverySku.getQty());
                    deliveryDetailList.add(addDTO);
                }
            } else {
                SoB2cDeliveryDetailDTO.AddDTO addDTO = new SoB2cDeliveryDetailDTO.AddDTO();
                addDTO.setSkuId(skuId);
                addDTO.setSkuNo(detailItem.getSkuNo());
                addDTO.setWarehouseId(detailItem.getWarehouseId());
                addDTO.setWarehouseName(detailItem.getWarehouseName());
                addDTO.setWarehouseLocation(detailItem.getWarehouseLocation());
                addDTO.setVirtualWarehouseId(detailItem.getVirtualWarehouseId());
                addDTO.setSourceDetailId(sourceDetailId);
                addDTO.setDeliveryQty(qty);
                deliveryDetailList.add(addDTO);
            }
        }
        soB2cDelivery.setDetailList(deliveryDetailList);
        soB2cDeliveryFeign.addSoB2cDelivery(soB2cDelivery);

    }

    /**
     * 获取到发货的sku 信息
     *
     * @param soDetailList        sku id
     * @param warehouseManageType 仓库的经营类型
     * @param distinctFlag
     * @return
     * @description
     * @date 2024-02-27 15:38
     * @author Lambda
     */
    public List<SoB2cDeliveryDTO.DeliverySkuDTO> listDeliverySku(String shopId, List<SoB2cDetailEntity> soDetailList, String platform, String warehouseManageType, Boolean distinctFlag) {
        List<SoB2cDeliveryDTO.DeliverySkuDTO> resultList = new ArrayList<>(10);
        List<String> skuIds = soDetailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> platformNos = soDetailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());

        ListingInfoParamDTO param = new ListingInfoParamDTO();
        param.setPlatform(platform);
        param.setSkuIdList(skuIds);
        param.setPlatformSkuNoList(platformNos);
        param.setShopIdList(Arrays.asList(shopId));
        param.setIsExpire(false);
        //子件发货
        String singleDelivery = WarehouseDeliveryTypeEnum.SINGLE.getCode();
        //自建
        String selfBuild = WarehouseManageTypeEnum.SELF_BUILD.getCode();
        //是否自建 如果是就是要拆分
        Boolean isSelfBuild = selfBuild.equals(warehouseManageType);
        List<ListingInfoWithSkuMappingDTO> skuMappingList = skuMappingService.findListDto(param);
        //需要拆分的skuId+platformNo
        List<String> wantSplitSkuIdList = new ArrayList<>(10);
        List<String> wantSplitSkuIdAndPlatformList = new ArrayList<>(10);
        for (SoB2cDetailEntity soB2cDetailEntity : soDetailList) {
            String skuId = soB2cDetailEntity.getSkuId();
            ListingInfoWithSkuMappingDTO skuMappingDTO = skuMappingList.stream().filter(s -> skuId.equals(s.getProductSkuId()) && soB2cDetailEntity.getPlatformSkuNo().equals(s.getPlatformSkuNo()) && (soB2cDetailEntity.getPlatformSpuNo().isEmpty() || soB2cDetailEntity.getPlatformSpuNo().equals(s.getPlatformSpuNo()))).findFirst().orElse(null);
            if (Objects.isNull(skuMappingDTO)) {
                if (isSelfBuild) {
                    wantSplitSkuIdList.add(skuId);
                    wantSplitSkuIdAndPlatformList.add(skuId + soB2cDetailEntity.getPlatformSkuNo());
                }
                continue;
            }
            Map<String, String> extendMap = skuMappingDTO.getExtendMap();
            // 表示是空 没有设置走默认
            if (Objects.isNull(extendMap)) {
                if (isSelfBuild) {
                    wantSplitSkuIdList.add(skuId);
                    wantSplitSkuIdAndPlatformList.add(skuId + soB2cDetailEntity.getPlatformSkuNo());
                }
                continue;
            }
            //发货类型
            String deliveryType = extendMap.get(warehouseManageType);
            //表示获取到了
            if (StringUtils.isNotBlank(deliveryType)) {
                //子件发货 就要去拆分
                if (singleDelivery.equals(deliveryType)) {
                    wantSplitSkuIdList.add(skuId);
                    wantSplitSkuIdAndPlatformList.add(skuId + soB2cDetailEntity.getPlatformSkuNo());
                }
            } else {
                if (isSelfBuild) {
                    wantSplitSkuIdList.add(skuId);
                    wantSplitSkuIdAndPlatformList.add(skuId + soB2cDetailEntity.getPlatformSkuNo());
                }
            }
        }

        String combinationType = BomTypeEnum.COMBINATION.getType();
        //获取对应bom 信息
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(wantSplitSkuIdList);

        //不需要拆分的skuId
        for (SoB2cDetailEntity detailEntity : soDetailList) {
            String skuId = detailEntity.getSkuId();
            String platformSkuNo = detailEntity.getPlatformSkuNo();
            String skuNo = detailEntity.getSkuNo();
            if (wantSplitSkuIdAndPlatformList.contains(skuId + platformSkuNo)) {
                //套装的bom
                List<BomChildrenSkuDTO> bomSkuList = bomChildrenSkuList.stream()
                        .filter(req -> req.getParentSkuId().equals(skuId)
                                && combinationType.equals(req.getType())
                        ).collect(Collectors.toList());
                //表示是bom
                if (CollectionUtils.isNotEmpty(bomSkuList)) {
                    for (BomChildrenSkuDTO bomSku : bomSkuList) {
                        SoB2cDeliveryDTO.DeliverySkuDTO deliverySku = new SoB2cDeliveryDTO.DeliverySkuDTO();
                        deliverySku.setSkuId(bomSku.getSkuId());
                        deliverySku.setSkuNo(bomSku.getSkuNo());
                        Integer quantity = bomSku.getQuantity();
                        deliverySku.setQty(quantity);
                        deliverySku.setSourceSkuId(skuId);
                        deliverySku.setSourceSkuNo(skuNo);
                        deliverySku.setPlatformSkuNo(platformSkuNo);
                        deliverySku.setWarehouseId(detailEntity.getWarehouseId());
                        deliverySku.setDetailId(detailEntity.getId());
                        deliverySku.setPlatformDetailId(detailEntity.getSourceDetailId());
                        resultList.add(deliverySku);
                    }
                } else {
                    //表示没有
                    SoB2cDeliveryDTO.DeliverySkuDTO deliverySku = new SoB2cDeliveryDTO.DeliverySkuDTO();
                    deliverySku.setSkuId(skuId);
                    deliverySku.setQty(1);
                    deliverySku.setSourceSkuId(skuId);
                    deliverySku.setSourceSkuNo(skuNo);
                    deliverySku.setSkuNo(skuNo);
                    deliverySku.setPlatformSkuNo(platformSkuNo);
                    deliverySku.setWarehouseId(detailEntity.getWarehouseId());
                    deliverySku.setDetailId(detailEntity.getId());
                    deliverySku.setPlatformDetailId(detailEntity.getSourceDetailId());
                    resultList.add(deliverySku);
                }
            } else {
                SoB2cDeliveryDTO.DeliverySkuDTO deliverySku = new SoB2cDeliveryDTO.DeliverySkuDTO();
                deliverySku.setSkuId(detailEntity.getSkuId());
                deliverySku.setPlatformSkuNo(detailEntity.getPlatformSkuNo());
                deliverySku.setQty(1);
                deliverySku.setSkuNo(skuNo);
                deliverySku.setSourceSkuId(detailEntity.getSkuId());
                deliverySku.setSourceSkuNo(detailEntity.getSkuNo());
                deliverySku.setWarehouseId(detailEntity.getWarehouseId());
                deliverySku.setDetailId(detailEntity.getId());
                deliverySku.setPlatformDetailId(detailEntity.getSourceDetailId());
                resultList.add(deliverySku);
            }
        }

        List<SoB2cDeliveryDTO.DeliverySkuDTO> noSkuNOSkuList = resultList.stream().filter(s -> StringUtils.isBlank(s.getSkuNo())).
                collect(Collectors.toList());

        List<String> noSkuNOSkuIdList = noSkuNOSkuList.stream().
                map(SoB2cDeliveryDTO.DeliverySkuDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> detailList = plmTaskFeign.getByIdList(noSkuNOSkuIdList);

        for (SoB2cDeliveryDTO.DeliverySkuDTO item : resultList) {
            String skuNo = item.getSkuNo();
            String skuId = item.getSkuId();
            //如果是空
            if (StringUtils.isBlank(skuNo)) {
                String wantSkuNo = detailList.stream().filter(s -> s.getId().equals(skuId)).findFirst().
                        map(ProductDetailEntity::getSkuNo).orElse("");
                item.setSkuNo(wantSkuNo);
            }
        }
        if (distinctFlag) {
            resultList = resultList.stream().distinct().collect(Collectors.toList());
        }
        return resultList;
    }

//    @Async("omsErpExecutor")
//    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NEVER)
//    public void asyncThirdWarehouseCreateOutStock(SoB2cEntity entity, String warehouseId, String warehouseManageType, SoB2cLogisticsEntity logisticsEntity, OverseasProviderWarehouseDTO.ViewDTO overseasProviderWarehouse, List<SoB2cDetailEntity> detailList, String newChannelId) {
//        log.warn("B2C订单【{}】下出库单，第三方仓【{}】，物流单【{}】", entity.getCode(), warehouseId, logisticsEntity.getCode());
//        try {
//            //下出库单命令
//            soB2cService.thirdWarehouseCreateOutStock(entity, warehouseId, warehouseManageType, logisticsEntity, overseasProviderWarehouse, detailList, newChannelId);
//        } catch (Exception e) {
//            log.error("B2C订单【{}】下出库单异常>>>{}", entity.getCode(), e.getMessage());
//            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
//            addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
//            addError.setParamJson("");
//            addError.setReturnJson("");
//            addError.setMainId(entity.getId());
//            addError.setMessage(e.getMessage());
//            soB2cErrorService.add(addError);
//            //失败还原订单状态
//            this.updateBillStatus(entity.getId(), SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION);
//        }
//    }
    /**
     *  发送三方仓推送任务
     * @param pushDTO
     */
    private void sendThirdWarehousePushTask(ThirdWarehouseCreateOutboundPushDTO pushDTO) {
        DmpPushTaskEntity pushTaskEntity = syncThirdWarehouseService.syncDataToThirdWarehouse(pushDTO, SyncOperateEnum.OPERATE_ADD.getCode());
        //推送三方仓
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }
    /**
     * 第三方仓下出库单
     *
     * @param
     * @param overseasProviderWarehouse
     * @param newChannelId
     */
    @DistributeLocker(businessType = RedisKeyConstant.SO_B2C_THIRD_DELIVERY_ORDER_KEY,keyName = "entity.id",waiteTime = 60)
    public void thirdWarehouseCreateOutStock(SoB2cEntity entity, String warehouseId, String warehouseManageType, SoB2cLogisticsEntity logisticsEntity, OverseasProviderWarehouseDTO.ViewDTO overseasProviderWarehouse, List<SoB2cDetailEntity> detailList, String newChannelId) {
        entity = soB2cService.getById(entity.getId());
//        if (SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(entity.getBillStatus())){
//            log.warn("B2C订单【{}】已发货，不进行出库单处理", entity.getCode());
//            //已发货不进行处理
//            operateLogService.addModuleOperateLog(CharSequenceUtil.format("已发货，不进行出库单处理", entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交发货");
//            return;
//        }
//        //成功更新订单状态
//        this.updateBillStatus(entity.getId(), SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED);
        //判断渠道是否是海外仓的渠道，否的话判断新的渠道是否是海外仓渠道，否则报错
        Boolean isCheckNewChannel = false;
        String logisticsChannelId = logisticsEntity.getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
        if(Objects.isNull(authDTO) || !authDTO.getLogisticsPlatform().equals(overseasProviderWarehouse.getProviderCode())){
            if(StringUtils.isBlank(newChannelId)){
                throw new ServiceException("渠道不为海外仓{}渠道，请选择海外仓{}渠道",LogisticsPlatformEnum.getNameByCode(overseasProviderWarehouse.getProviderCode()),LogisticsPlatformEnum.getNameByCode(overseasProviderWarehouse.getProviderCode()));
            }
            authDTO = logisticsAuthFeign.getAuthByChannelId(newChannelId);
            if(Objects.isNull(authDTO) || !authDTO.getLogisticsPlatform().equals(overseasProviderWarehouse.getProviderCode())){
                throw new ServiceException("渠道不为海外仓{}渠道，请选择海外仓{}渠道",LogisticsPlatformEnum.getNameByCode(overseasProviderWarehouse.getProviderCode()),LogisticsPlatformEnum.getNameByCode(overseasProviderWarehouse.getProviderCode()));
            }
            isCheckNewChannel = true;
            logisticsChannelId = newChannelId;
        }
        LogisticsChannelEntity channelEntity = logisticsFeign.getChannelById(logisticsChannelId);
        if(isCheckNewChannel){
            //校验新选择的渠道是否符合店铺管理配置的渠道
            shopChannelRefService.checkChannel(entity.getShopId(),newChannelId);
        }
        LogisticsSaleChannelEntity saleChannelEntity = null;
        if(StringUtils.isNotBlank(channelEntity.getSyncSourceId())){
            saleChannelEntity = FeignQuery.getById(LogisticsSaleChannelEntity.class,channelEntity.getSyncSourceId());
        }
        SoB2cLabelEntity soB2cLabelEntity = new SoB2cLabelEntity();
        if(Objects.nonNull(channelEntity.getIsPushLabel()) && channelEntity.getIsPushLabel()){
            List<SoB2cLabelEntity> soB2cLabelEntityList = soB2cLabelService.listSoB2cLabelByMainIds(Collections.singletonList(entity.getId()));
            if(CollectionUtils.isEmpty(soB2cLabelEntityList)){
                throw new ServiceException("订单{}未获取面单",entity.getCode());
            }
            soB2cLabelEntity = soB2cLabelEntityList.get(0);
        }
        //合并相同sku的明细
        Map<String, Integer> sameSkuMap = detailList.stream().collect(Collectors.toMap(v -> v.getId(), SoB2cDetailEntity::getQty, Integer::sum));
        List<SoB2cDeliveryDTO.DeliverySkuDTO> wantSkuList = listDeliverySku(entity.getShopId(), detailList, entity.getDictPlatform(), warehouseManageType, false);
        List<SkuMappingDTO.ListingSkuParamDTO> listSkuParamList = new ArrayList<>();
        String mainId = entity.getId();
        //平台
        String dictPlatform = overseasProviderWarehouse.getProviderCode();
        String warehouseType = RuleTypeEnum.WAREHOUSE.getCode();
        for (SoB2cDeliveryDTO.DeliverySkuDTO item : wantSkuList) {
            SkuMappingDTO.ListingSkuParamDTO listingSkuParam = new SkuMappingDTO.ListingSkuParamDTO();
            listingSkuParam.setDictPlatform(dictPlatform);
            listingSkuParam.setSkuNo(item.getSkuNo());
            listingSkuParam.setSkuId(item.getSkuId());
            listingSkuParam.setWarehouseId(warehouseId);
            listingSkuParam.setType(warehouseType);
            listSkuParamList.add(listingSkuParam);
        }

        ThirdWarehouseCreateOutboundReq createOutboundReq = new ThirdWarehouseCreateOutboundReq();
        SoB2cReceiverEntity receiver = soB2cReceiverService.getByMainId(entity.getId());
        String secondAddress = receiver.getSecondAddress();
        String fullAddress = receiver.getFullAddress();
        String address2;
        if (PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(entity.getDictPlatform())) {
            address2 = secondAddress;
        } else {
            address2 = secondAddress + fullAddress;
        }
        //转化收货人
        ThirdWarehouseCreateOutboundReq.ReceiverInfo receiverInfo = B2cOrderConverter.INSTANCE.convertThirdWarehouseReceiver(receiver);
        receiverInfo.setAddress2(address2);
        //速派通地址3赋值
        if (PlatformDictEnum.SPT.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())
         ||PlatformDictEnum.JIFENG.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())
         ||PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())) {
            receiverInfo.setAddress2(receiver.getSecondAddress());

            receiverInfo.setAddress3(receiver.getFullAddress());
        }
        createOutboundReq.setReceiverInfo(receiverInfo);

        List<SkuMappingDTO.ListSkuResultDTO> platformSkuList = skuMappingService.listBySkuList(listSkuParamList, dictPlatform, warehouseType, warehouseId);
        List<ThirdWarehouseCreateOutboundReq.Item> itemList = new ArrayList<>(detailList.size());

        List<SoB2cDeclareProductEntity> soB2cDeclareProductEntityList = soB2cDeclareProductService.listBySoId(entity.getId());
        for (SoB2cDeliveryDTO.DeliverySkuDTO deliverySkuDTO : wantSkuList) {
            Integer qty = sameSkuMap.get(deliverySkuDTO.getDetailId());
            if (qty == null) {
                throw new ServiceException(CharSequenceUtil.format("发货sku{}查不到原sku", deliverySkuDTO.getSkuNo()));
            }
            Integer baseQty = qty * deliverySkuDTO.getQty();
            /**
             * 海外仓产品SKU
             */
            String platformSku = platformSkuList.stream().filter(s -> s.getSkuId().equals(deliverySkuDTO.getSkuId())).
                    map(SkuMappingDTO.ListSkuResultDTO::getPlatformSkuNo).findFirst().orElseThrow(() -> new ServiceException(CharSequenceUtil.format("{}未配置海外仓sku", deliverySkuDTO.getSkuNo())));
            /**
             * 海外仓产品SKU id
             */
            String platformSkuId = platformSkuList.stream().filter(s -> s.getSkuId().equals(deliverySkuDTO.getSkuId())).
                    map(SkuMappingDTO.ListSkuResultDTO::getPlatformSkuId).findFirst().orElseThrow(() -> new ServiceException(CharSequenceUtil.format("{}未配置海外仓sku", deliverySkuDTO.getSkuNo())));

            ThirdWarehouseCreateOutboundReq.Item outboundReqItem = new ThirdWarehouseCreateOutboundReq.Item();
            outboundReqItem.setQuantity(baseQty);
            outboundReqItem.setProductSku(platformSku);
            outboundReqItem.setSkuId(deliverySkuDTO.getSkuId());
            outboundReqItem.setSkuNo(deliverySkuDTO.getSkuNo());
            outboundReqItem.setSourceSkuId(deliverySkuDTO.getSourceSkuId());
            outboundReqItem.setSourceSkuNo(deliverySkuDTO.getSourceSkuNo());
            outboundReqItem.setPlatformDetailId(deliverySkuDTO.getPlatformDetailId());
            outboundReqItem.setDetailId(deliverySkuDTO.getDetailId());
            outboundReqItem.setProductSkuId(platformSkuId);
            SoB2cDeclareProductEntity soB2cDeclareProductEntity = soB2cDeclareProductEntityList.stream().filter(s -> s.getSkuId().equals(deliverySkuDTO.getSkuId())).findFirst().orElse(null);
            if(Objects.nonNull(soB2cDeclareProductEntity)){
                outboundReqItem.setHsCode(soB2cDeclareProductEntity.getToCustomsCode());
            }
            itemList.add(outboundReqItem);
        }
        String platformWarehouseCode = overseasProviderWarehouse.getPlatformWarehouseCode();
        createOutboundReq.setPlatform(entity.getDictPlatform());
        createOutboundReq.setWarehouseCode(platformWarehouseCode);
        createOutboundReq.setVerify(MathUtil.ONE);
        createOutboundReq.setShopId(entity.getShopId());
        createOutboundReq.setSoCode(entity.getCode());
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(entity.getShopId());
        createOutboundReq.setShopName(shopInfoEntity.getName());
        // 设置EORI税号
        if (CharSequenceUtil.isNotBlank(shopInfoEntity.getEoriTaxNo())) {
            createOutboundReq.setEoriTaxNo(shopInfoEntity.getEoriTaxNo());
        }
        createOutboundReq.setOwnerCode(overseasProviderWarehouse.getOwnerCode());
        createOutboundReq.setPlatformCode(entity.getPlatformCode());
        createOutboundReq.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
        createOutboundReq.setAuthId(overseasProviderWarehouse.getMainId());
        createOutboundReq.setShippingMethod(Objects.isNull(channelEntity) ? "" : channelEntity.getCode());
        createOutboundReq.setShippingMethodName(Objects.isNull(saleChannelEntity) ? "" : saleChannelEntity.getCnName());
        createOutboundReq.setShippingMethodId(Objects.isNull(saleChannelEntity) ? "" : saleChannelEntity.getPlatformChannelId());
        createOutboundReq.setLastMileCarrier(channelEntity.getLastMileCarrier());
        createOutboundReq.setItems(itemList);
        createOutboundReq.setTrackingNo(logisticsEntity.getCode());
        //通过订单处理规则处理参数
        Map<String, Object> map = this.getRuleOrderHandleMap(entity, logisticsChannelId, receiver);
        createOutboundReq = cfgRuleOrderHandleService.handleRuleOrderThirdWarehouse(createOutboundReq, map);

        //查询交接文件配置
        String handoverLabelUrl = "";
        if(StringUtils.isNotBlank(channelEntity.getHandoverDocType())){
            if(LogisticsHandoverDocTypeEnum.HANDOVER_PACKAGE.getCode().equals(channelEntity.getHandoverDocType())){
                PackagePlanEntity packagePlanEntity = packagePlanService.getBySoId(entity.getId());
                if(Objects.isNull(packagePlanEntity) || StringUtils.isBlank(packagePlanEntity.getHandoverLabelUrl())){
                    throw new ServiceException("交接文件不存在，请先生成交接文件");
                }
                String domain = dictBasicService.getByTypeAndValue("fastDfsDomain",BusinessCommonConstants.getEnvironment()+"-fastDfsDomain").getName();

                handoverLabelUrl = domain + packagePlanEntity.getHandoverLabelUrl();
            }
        }
        //查询配置是否推送面单
        if(Objects.nonNull(channelEntity.getIsPushLabel()) && channelEntity.getIsPushLabel()){
            String logisticsLabelBase64 = soB2cLabelEntity.getLogisticsLabelBase64();
            if (CharSequenceUtil.isBlank(logisticsLabelBase64)){
                throw new ServiceException("未找到面单信息");
            }
            ThirdWarehouseUploadFileReq thirdWarehouseUploadFileReq = new ThirdWarehouseUploadFileReq();
            thirdWarehouseUploadFileReq.setOrderCode(entity.getCode());
            thirdWarehouseUploadFileReq.setFileData(logisticsLabelBase64);
            thirdWarehouseUploadFileReq.setAuthId(overseasProviderWarehouse.getMainId());
            thirdWarehouseUploadFileReq.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
            ApiResult<ThirdWarehouseUploadFileResponse> uploadFileResponse = thirdWarehouseFeign.uploadFile(thirdWarehouseUploadFileReq);
            if(!uploadFileResponse.isSuccess()){
                throw new ServiceException("上传面单失败{}",uploadFileResponse.getMsg());
            }
            ThirdWarehouseCreateOutboundReq.Attach attach = new ThirdWarehouseCreateOutboundReq.Attach();
            attach.setFileType(FileTypeEnum.PDF.getCode());
            attach.setAttachId(uploadFileResponse.getData().getAttachId());
            createOutboundReq.setAttach(Collections.singletonList(attach));
            createOutboundReq.setOnlineFlag(true);
            createOutboundReq.setLabelData(logisticsLabelBase64);
            //生成在线url
            if(PlatformDictEnum.JIFENG.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())
             ||PlatformDictEnum.CAINIAO.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())
                    ||PlatformDictEnum.IML.getCode().equalsIgnoreCase(overseasProviderWarehouse.getProviderCode())) {
                String path = FastDFSClientUtil.uploadFile(Base64.getDecoder().decode(logisticsLabelBase64.replace("data:application/pdf;base64,","")),entity.getCode()+".pdf",new HashMap<>());
                String domain = dictBasicService.getByTypeAndValue("fastDfsDomain",BusinessCommonConstants.getEnvironment()+"-fastDfsDomain").getName();
                createOutboundReq.setLabelUrl(domain+path);
            }
        }

        //上传发票

        autoPushInvoice(entity, overseasProviderWarehouse, channelEntity,createOutboundReq);

        createOutboundReq.setCarrierType(channelEntity.getCarrierType());
        log.warn("第三方仓下单请求:{}", JSONUtil.toJsonStr(entity.getCode()));
        ApiResult<String> apiResult = soB2cService.createThirdWarehouseOutbound(entity, warehouseId, createOutboundReq, 0);
        log.warn("第三方仓下单结果:{}", JSONUtil.toJsonStr(apiResult));
        if (!apiResult.isSuccess()){
            //失败还原订单状态
            this.updateBillStatus(entity.getId(), SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION);
            return;
        }
        soB2cErrorService.removeAllTypeErrorOrder(entity.getId());
        String shippingOrderNo = apiResult.getData();
        if (StringUtils.isNotBlank(shippingOrderNo)) {
            this.lambdaUpdate().set(SoB2cEntity::getShippingOrderNo, shippingOrderNo).
                    eq(SoB2cEntity::getId, mainId).update(new SoB2cEntity());
            String msg = CharSequenceUtil.format("创建海外仓出库单成功，单号【{}】", shippingOrderNo);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "创建海外仓出库单");
        }
        //上传面单
        if(Objects.nonNull(channelEntity.getIsPushLabel())
                && channelEntity.getIsPushLabel()
                && (
                PlatformDictEnum.GOOD_CANG.getCode().equals(overseasProviderWarehouse.getProviderCode())
                || PlatformDictEnum.IML.getCode().equals(overseasProviderWarehouse.getProviderCode())
        )){
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
            ThirdWarehouseUploadOrderLabelReq uploadOrderLabelReq = new ThirdWarehouseUploadOrderLabelReq();
            uploadOrderLabelReq.setOrderCode(shippingOrderNo);
            uploadOrderLabelReq.setTrackNo(soB2cLogisticsEntity.getCode());
            uploadOrderLabelReq.setFileUrlList(Arrays.asList(createOutboundReq.getLabelUrl()));
            uploadOrderLabelReq.setFileIdList(Collections.singletonList(createOutboundReq.getAttach().get(0).getAttachId()));
            uploadOrderLabelReq.setAuthId(overseasProviderWarehouse.getMainId());
            uploadOrderLabelReq.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
            ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabelResponse = thirdWarehouseFeign.uploadOrderLabel(uploadOrderLabelReq);
            if(!uploadOrderLabelResponse.isSuccess()){
                throw new ServiceException("推送面单失败{}",uploadOrderLabelResponse.getMsg());
            }
        }
        if(StringUtils.isNotBlank(channelEntity.getHandoverDocType())&& LogisticsHandoverDocTypeEnum.HANDOVER_PACKAGE.getCode().equals(channelEntity.getHandoverDocType())
                && PlatformDictEnum.IML.getCode().equals(overseasProviderWarehouse.getProviderCode())){
            ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq = new ThirdWarehouseUploadHandoverFileReq();
            uploadHandoverFileReq.setOrderCode(shippingOrderNo);
            uploadHandoverFileReq.setDictPlatform(entity.getDictPlatform());
            uploadHandoverFileReq.setFileUrl(handoverLabelUrl);
            int lastSlashIndex = handoverLabelUrl.lastIndexOf("/");
            // 截取最后一个斜杠后面的内容
            String fileName = handoverLabelUrl.substring(lastSlashIndex + 1);
            uploadHandoverFileReq.setFileName(fileName);
            uploadHandoverFileReq.setOwnerCode(overseasProviderWarehouse.getOwnerCode());
            uploadHandoverFileReq.setAuthId(overseasProviderWarehouse.getMainId());
            uploadHandoverFileReq.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
            ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile = thirdWarehouseFeign.uploadHandoverFile(uploadHandoverFileReq);
            if(!uploadHandoverFile.isSuccess()){
                //删除三方仓订单和发货单
                ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryFeign.getLatestBySoId(entity.getId());
                if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
                    thirdWarehouseDeliveryFeign.deleteById(thirdWarehouseDeliveryEntity.getId());
                    ThirdWarehouseCancelOutboundReq req = new ThirdWarehouseCancelOutboundReq();
                    req.setOrderCode(shippingOrderNo);
                    req.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
                    req.setShopId(entity.getShopId());
                    req.setOwnerCode(overseasProviderWarehouse.getOwnerCode());
                    req.setErpOrderCode(thirdWarehouseDeliveryEntity.getCode());
                    req.setWarehouseCode(overseasProviderWarehouse.getPlatformWarehouseCode());
                    req.setReason("推送交接文件失败，取消订单");
                    req.setAuthId(overseasProviderWarehouse.getMainId());
                    ApiResult<String> cancelResult = thirdWarehouseFeign.cancelOutboundOrder(req);
                    if(!cancelResult.isSuccess()){
                        log.error("推送交接文件失败，取消三方仓订单失败，订单号{}，原因{}",shippingOrderNo,cancelResult.getMsg());
                        throw new ServiceException("推送交接文件失败{},同时取消三方仓订单失败，原因{}",uploadHandoverFile.getMsg(),cancelResult.getMsg());
                    }
                }
                throw new ServiceException("推送交接文件失败{}",uploadHandoverFile.getMsg());
            }
        }
        //成功更新订单状态
        this.updateBillStatus(entity.getId(), SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED);
        //操作日志
        String msg = "B2C销售订单【{}】异步提交发货";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交发货");
    }


    public ApiResult<String> createThirdWarehouseOutbound(SoB2cEntity entity, String warehouseId, ThirdWarehouseCreateOutboundReq createOutboundReq, final int retryCount) {
        log.warn("第三方仓下单请求:{}", JSONUtil.toJsonStr(createOutboundReq));
        //如果已存在发货单，不处理
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryFeign.getLatestBySoId(entity.getId());
        if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
            if (CharSequenceUtil.isBlank(entity.getShippingOrderNo())){
                log.warn("订单{}已存在待处理的发货单，但是三方仓发货单为空", entity.getCode());
                //获取三方仓已生成的发货单id
                ThirdWarehouseQueryOutboundReq queryOutboundReq = new ThirdWarehouseQueryOutboundReq();
                queryOutboundReq.setErpOrderCode(thirdWarehouseDeliveryEntity.getCode());
                queryOutboundReq.setThirdWarehouseProvideCode(createOutboundReq.getThirdWarehouseProvideCode());
                queryOutboundReq.setAuthId(createOutboundReq.getAuthId());
                ApiResult<String> stringApiResult = thirdWarehouseFeign.queryOutboundOrder(queryOutboundReq);
                log.warn("查询三方仓发货单结果:{}", JSONUtil.toJsonStr(stringApiResult));
                if (stringApiResult.isSuccess()){
                    return ApiResult.success(stringApiResult.getData());
                }
            }else {
                log.warn("订单{}已存在待处理的发货单，不再重复创建", entity.getCode());
                return ApiResult.success(entity.getShippingOrderNo());
            }
        }
        try {
            if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
                log.warn("订单{}已存在待处理的发货单，使用已存在的发货单", entity.getCode());
                createOutboundReq.setReferenceNo(thirdWarehouseDeliveryEntity.getCode());
            }else {
                String code = soB2cService.addThirdWarehouseDelivery(createOutboundReq, warehouseId, entity);
                log.warn("新增三方仓发货单成功，发货单code:{}", code);
                createOutboundReq.setReferenceNo(code);
            }
        }catch (Exception e){
            log.error("B2C订单【{}】生成三方仓发货单异常>>>{}", entity.getCode(), e.getMessage());
            throw new ServiceException("生成三方仓发货单异常", e.getMessage());
        }
        ApiResult<String> apiResult = null;
        String type = SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode();
        try {
            log.warn("调用三方仓出库单请求:{}", JSONUtil.toJsonStr(createOutboundReq));
            apiResult = thirdWarehouseFeign.createOutboundOrder(createOutboundReq);
            log.warn("调用三方仓出库单结果:{}", JSONUtil.toJsonStr(apiResult));
            if (!apiResult.isSuccess()) {
                String message = "创建出库单异常" + apiResult.getMsg();
                //生成异常订单信息
                soB2cErrorService.generateErrorOrder(entity.getId(), type, message, JSONObject.toJSONString(createOutboundReq), JSONObject.toJSONString(apiResult),apiResult.getCode().toString());
                //标记三方仓发货单为删除
                thirdWarehouseDeliveryFeign.deleteById(thirdWarehouseDeliveryEntity.getId());
            }
            return apiResult;
        }catch (Exception e){
            log.warn("第" + (retryCount + 1) + "次执行失败: " + e.getMessage());
            if (retryCount + 1 < MAX_RETRY_COUNT){
                //重试查询
                try {
                    //等待30秒后重试
                    log.error("10秒后进行第" + (retryCount + 2) + "次重试");
                    Thread.sleep(RETRY_DELAY_SECONDS);
                    apiResult = createThirdWarehouseOutbound(entity, warehouseId, createOutboundReq, retryCount + 1);
                    return apiResult;
                }catch (Exception e1){
                    String message = e.getMessage();
                    //生成异常订单信息
                    soB2cErrorService.generateErrorOrder(entity.getId(), type, message, JSONObject.toJSONString(createOutboundReq), JSONObject.toJSONString(apiResult),apiResult.getCode().toString());
                    //标记三方仓发货单为删除
                    thirdWarehouseDeliveryFeign.deleteById(thirdWarehouseDeliveryEntity.getId());
                    return ApiResult.error( -1, e.getMessage());
                }
            }else {
                log.error("已达到最大重试次数" + MAX_RETRY_COUNT + "次，停止重试");
                String message = "重试创建出库单异常"+ e.getMessage();
                //生成异常订单信息
                soB2cErrorService.generateErrorOrder(entity.getId(), type, message, JSONObject.toJSONString(createOutboundReq), JSONObject.toJSONString(apiResult),apiResult.getCode().toString());
                //标记三方仓发货单为删除
                thirdWarehouseDeliveryFeign.deleteById(thirdWarehouseDeliveryEntity.getId());
                return ApiResult.error( -1, e.getMessage());
            }
        }
    }

    /**
     * 新增三方仓发货单
     *
     * @param createOutboundReq
     * @param warehouseId
     * @param entity
     */
    public String addThirdWarehouseDelivery(ThirdWarehouseCreateOutboundReq createOutboundReq, String warehouseId, SoB2cEntity entity) {
        log.warn("新增三方仓发货单请求:销售订单:{} 请求参数{}", JSONUtil.toJsonStr(entity), JSONUtil.toJsonStr(createOutboundReq));
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        thirdWarehouseDeliveryEntity.setSoCode(entity.getCode());
        thirdWarehouseDeliveryEntity.setSoId(entity.getId());
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WFHD);
        thirdWarehouseDeliveryEntity.setCode(code);
        thirdWarehouseDeliveryEntity.setDictPlatform(entity.getDictPlatform());
        thirdWarehouseDeliveryEntity.setPlatformCode(entity.getPlatformCode());
        thirdWarehouseDeliveryEntity.setThirdWarehousePlatform(createOutboundReq.getThirdWarehouseProvideCode());
        thirdWarehouseDeliveryEntity.setShippingMethod(createOutboundReq.getShippingMethod());
        List<ThirdWarehouseDeliveryDetailEntity> detailEntityList = new ArrayList<>();
        for (ThirdWarehouseCreateOutboundReq.Item item : createOutboundReq.getItems()) {
            ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
            thirdWarehouseDeliveryDetailEntity.setSkuId(item.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSkuNo(item.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setDeliveryQty(item.getQuantity());
            thirdWarehouseDeliveryDetailEntity.setWarehouseId(warehouseId);
            thirdWarehouseDeliveryDetailEntity.setPlatformSkuNo(item.getProductSku());
            thirdWarehouseDeliveryDetailEntity.setPlatformWarehouseCode(createOutboundReq.getWarehouseCode());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuId(item.getSourceSkuId());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuNo(item.getSourceSkuNo());
            thirdWarehouseDeliveryDetailEntity.setSoDetailId(item.getDetailId());
            detailEntityList.add(thirdWarehouseDeliveryDetailEntity);
        }
        thirdWarehouseDeliveryEntity.setDetailEntityList(detailEntityList);
        thirdWarehouseDeliveryFeign.add(thirdWarehouseDeliveryEntity);
        return code;
    }

    private Map<String, Object> getRuleOrderHandleMap(SoB2cEntity entity, String logisticsChannelId, SoB2cReceiverEntity receiverEntity) {
        Map<String, Object> resultMap = new HashMap<>(4);
        resultMap.put("dictPlatform", entity.getDictPlatform());
        resultMap.put("shop", entity.getShopId());
        resultMap.put("destCountry", ObjectUtil.isEmpty(receiverEntity) ? "" : receiverEntity.getCountry());
        resultMap.put("logisticsChannelId", logisticsChannelId);

        //现有规则解析必须包含明细信息
        Map<String, Object> detailMap = new HashMap<>(4);
        detailMap.put("dictPlatform", entity.getDictPlatform());
        detailMap.put("shop", entity.getShopId());
        detailMap.put("destCountry", ObjectUtil.isEmpty(receiverEntity) ? "" : receiverEntity.getCountry());
        detailMap.put("logisticsChannelId", logisticsChannelId);

        resultMap.put("detailList", Arrays.asList(detailMap));
        return resultMap;
    }


    @Override
    public BatchResultDTO deliveryIntercept(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //已打标拦截不支持重复提交
        if (entity.getIsIntercept()) {
            throw new ServiceException(ApiError.IS_EXIST_NOT_INTERCEPT);
        }
        //根据wms是否勾选组包后不允许拦截按钮 进行校验  //若系统配置勾选则中转状态为非无需中转(包含待中转，预报成功。预报失败)
        if (PackageStatusEnum.ALREADY.getCode().equals(entity.getPackageStatus())) {
            //已组包 配置已勾选
            CfgSettingEntity cfgSettingEntity = cfgSettingFeign.getByKey(CfgSettingEnum.DELIVERY_INTERCEPT.getCode());
            if (ObjectUtil.isNotEmpty(cfgSettingEntity) && ObjectUtil.isNotEmpty(cfgSettingEntity.getDataJson())) {
                CfgSettingValueDTO.B2cDeliveryInterceptDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.B2cDeliveryInterceptDTO.class);
                if (Objects.nonNull(dto) && Objects.nonNull(dto.getB2cDeliveryIntercept()) && dto.getB2cDeliveryIntercept()) {
                    throw new ServiceException(ApiError.ERROR_DELIVERY_INTERCEPT_READY_PACKAGED, entity.getCode());
                }
            }
        }
        //平台仓不支持拦截
        if (entity.hasPlatformWarehouseOrder()) {
            throw new ServiceException(ApiError.PLATFORM_WAREHOUSE_ORDER_NOT_INTERCEPT);
        }
        if (PlatformDictEnum.WALMART.getCode().equals(entity.getDictPlatform())) {
            throw new ServiceException(ApiError.PLATFORM_WAREHOUSE_ORDER_NOT_INTERCEPT);
        }
        //销售订单状态只有待发货的订单可以发起拦截
        if (!SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.NOT_DELIVERY_NOT_INTERCEPT);
        }

        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
        if (ObjectUtils.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }

        //新增发货拦截
        BatchResultDTO result = soB2cService.addIntercept(remark, entity, logisticsEntity);
        if(Boolean.TRUE.equals(result.getSuccess())){
            List<SoB2cDeliveryEntity> soB2cDeliveryList = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getSourceId,entity.getId()).list();
            if(CollUtil.isNotEmpty(soB2cDeliveryList)){
                wmsTaskFeign.waveListStatusAutoChange(soB2cDeliveryList.get(0).getId());
            }
        }
        return result;
    }

    /**
     * 新增发货拦截单
     *
     * @param remark
     * @param entity
     * @param logisticsEntity
     * @return void
     * @Author Luo_WG
     * @Date 2024/1/16 18:52
     **/
    public BatchResultDTO addIntercept(String remark, SoB2cEntity entity, SoB2cLogisticsEntity logisticsEntity) {
        //映射拦截单主表信息
        SoB2cDeliveryInterceptDTO.AddDTO addDTO = B2cOrderConverter.INSTANCE.convertIntercept(entity);
        addDTO.setBillType(OrderTypeEnum.B2C.getCode());
        addDTO.setRemark(remark);

        //物流信息
        addDTO.setLogisticsChannelId(logisticsEntity.getLogisticsChannelId());
        addDTO.setLogisticsChannelName(logisticsEntity.getLogisticsChannelName());
        addDTO.setTransportNo(logisticsEntity.getCode());

        //详情
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(entity.getId());
        List<SoB2cDeliveryInterceptDetailDTO.AddDTO> detailList = B2cOrderConverter.INSTANCE.convertInterceptDetail(soB2cDetailEntityList);
        addDTO.setDetailList(detailList);
        if (StringUtils.isBlank(logisticsEntity.getLogisticsChannelId())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST.msg + logisticsEntity.getLogisticsChannelId());
        }
        //检测是否是API 对接的仓库
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(Collections.singletonList(soB2cDetailEntityList.get(0).getWarehouseId()));
        Boolean isApi = CollectionUtils.isNotEmpty(overseasWarehouseList);
        //是否多渠道订单
        boolean isMultiChannelOrder = false;
        SoMultiChannelEntity soMultiChannelEntity = null;
        if (CharSequenceUtil.isNotBlank(entity.getMultiChannelType())){
            soMultiChannelEntity = soMultiChannelService.getBySoId(entity.getId(), Boolean.TRUE);
            isMultiChannelOrder = Objects.nonNull(soMultiChannelEntity);
            addDTO.setLogisticsChannelId(soMultiChannelEntity.getLogisticsChannelId());
            addDTO.setLogisticsChannelName(soMultiChannelEntity.getLogisticsChannelName());
            addDTO.setTransportNo(soMultiChannelEntity.getTrackNo());
        }
        if (isMultiChannelOrder){
            //多渠道订单
            addDTO.setSourceType(SoB2cDeliveryInterceptSourceTypeEnum.API.getCode());
            addDTO.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus());
            //发货拦截
            return soMultiChannelService.deliveryIntercept(soMultiChannelEntity, true, true, remark);
        }else if (isApi) {
            //三方仓直接调接口
            addDTO.setSourceType(SoB2cDeliveryInterceptSourceTypeEnum.API.getCode());
            addDTO.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus());
            LogisticsPlatformEnum platformEnum = LogisticsPlatformEnum.getByCode(overseasWarehouseList.get(0).getProviderCode());
            //API海外物流拦截
            BatchResultDTO resultDTO = this.overseasProviderIntercept(entity, platformEnum, overseasWarehouseList.get(0), remark);
            if (resultDTO.getSuccess()){
                addDTO.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getCode());
                addDTO.setHandleResult(HandleResultEnum.SUCCESS.getCode());
                addDTO.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
                addDTO.setHandleUserName(UserContext.getDefaultLoginUser().getUserName());
                addDTO.setHandleTime(LocalDateTime.now());
                soB2cLogisticsService.cancelLogistic(entity.getId(), Collections.singletonList(entity),Collections.singletonList(logisticsEntity), false);
                soB2cDeliveryInterceptFeign.add(addDTO);
            }
            return resultDTO;
        } else {
            addDTO.setSourceType(SoB2cDeliveryInterceptSourceTypeEnum.SO_B2C.getCode());
            List<SoB2cDeliveryEntity> soB2cDeliveryList = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getSourceId, entity.getId()).ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode()).list();
            if (CollectionUtils.isEmpty(soB2cDeliveryList)) {
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), "未查询到发货单");
            }
            SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryList.get(0);
            //1、新增拦截单
            BaseResultDTO.AddDTO add = soB2cDeliveryInterceptFeign.add(addDTO);
            // 操作日志
            String msg = entity.getIsCancel() ? "平台订单取消,自动发起拦截" : CharSequenceUtil.format("用户【{}】发起【{}】，已冻结单据单号【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货拦截", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "发货拦截");
            //发货单状态为待处理，生成波次，异常（生成波次异常）自动处理，并把结果返回前端
            if (SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(soB2cDelivery.getStatus())
                    || SoB2cDeliveryStatusEnum.GENERATE_WAVE.getCode().equals(soB2cDelivery.getStatus())
                    || (SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(soB2cDelivery.getStatus()) && AbnormalCauseEnum.GENERATION_WAVE.getCode().equals(soB2cDelivery.getAbnormalCause()))) {
                BatchResultDTO handleResult = soB2cDeliveryInterceptFeign.handleSuccess(soB2cDelivery, add.getId());
                if (handleResult.getSuccess()) {
                    return BatchResultDTO.success(entity.getId(), entity.getCode(), "自动拦截成功，订单可以修改后重新提交发货");
                } else {
                    //修改拦截打标识、冻结状态
                    SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
                    interceptUpdateOrderDTO.setIsIntercept(Boolean.TRUE);
                    interceptUpdateOrderDTO.setIsFrozen(Boolean.TRUE);
                    interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getId()));
                    interceptUpdateOrderDTO.setRemark(remark);
                    Boolean flag = this.updateIntercept(interceptUpdateOrderDTO);
                    return BatchResultDTO.fail(entity.getId(), entity.getCode(), "已生成拦截单，请联系物流和仓库处理拦截");
                }
            } else {
                //修改拦截打标识、冻结状态
                SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
                interceptUpdateOrderDTO.setIsIntercept(Boolean.TRUE);
                interceptUpdateOrderDTO.setIsFrozen(Boolean.TRUE);
                interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getId()));
                interceptUpdateOrderDTO.setRemark(remark);
                Boolean flag = this.updateIntercept(interceptUpdateOrderDTO);
            }
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "已生成发货拦截单");
        }

    }

    /**
     * @param soB2cEntity  订单信息
     * @param platformEnum 物流平台枚举
     * @param remark
     * @return com.common.business.dto.base.BatchResultDTO
     * @Author Luo_WG
     * @Date 2024/1/19 11:23
     **/
    private BatchResultDTO overseasProviderIntercept(SoB2cEntity soB2cEntity, LogisticsPlatformEnum platformEnum, OverseasProviderWarehouseDTO.ViewDTO viewDTO, String remark) {
        String referenceCode = soB2cEntity.getCode();
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryFeign.getLatestBySoId(soB2cEntity.getId());
        if (ObjectUtils.isNotEmpty(thirdWarehouseDeliveryEntity) && thirdWarehouseDeliveryEntity.getCode().contains(BusinessNoConstant.WFHD)) {
            referenceCode = thirdWarehouseDeliveryEntity.getCode();
        }
        ThirdWarehouseCancelOutboundReq req = new ThirdWarehouseCancelOutboundReq();
        req.setOrderCode(soB2cEntity.getShippingOrderNo());
        req.setThirdWarehouseProvideCode(platformEnum.getCode());
        req.setShopId(soB2cEntity.getShopId());
        req.setOwnerCode(viewDTO.getOwnerCode());
        req.setErpOrderCode(referenceCode);
        req.setWarehouseCode(viewDTO.getPlatformWarehouseCode());
        req.setReason(remark);
        OverseasProviderEntity overseasProviderEntity = overseasProviderFeign.getByWarehouseId(viewDTO.getWarehouseId());
        if (ObjectUtils.isNotEmpty(overseasProviderEntity)) {
            req.setAuthId(overseasProviderEntity.getId());
        }
        ApiResult<String> stringApiResult = thirdWarehouseFeign.cancelOutboundOrder(req);
        if (stringApiResult.getCode() == 200 && ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode().equals(stringApiResult.getData())) {
            //自动拦截结果确认，拦截成功
            soB2cEntity.setIsIntercept(Boolean.FALSE);
            soB2cEntity.setIsFrozen(Boolean.FALSE);
            soB2cEntity.setApproveStatus(ApproveStatusEnum.REJECT);
            soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            soB2cEntity.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_SUCCESS_REJECT.getCode());
            if(soB2cEntity.getIsCancel()){
                soB2cEntity.setInvalidStatus(Boolean.TRUE);
                soB2cEntity.setInvalidRemark("平台订单取消,拦截成功自动作废");
            }
            this.updateById(soB2cEntity);
            String msg = CharSequenceUtil.format("用户【{}】发起海外仓拦截成功,备注：【{}】", UserContext.getDefaultLoginUser().getUserName(), remark);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "发货拦截");
            if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
                thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
                thirdWarehouseDeliveryFeign.update(thirdWarehouseDeliveryEntity);
            }
            return BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), "三方仓拦截成功");
        } else if ( ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode().equals(stringApiResult.getData())) {
            //拦截中
            soB2cEntity.setIsFrozen(Boolean.TRUE);
            soB2cEntity.setIsIntercept(Boolean.TRUE);
            this.updateById(soB2cEntity);
            String msg = CharSequenceUtil.format("用户【{}】发起海外拦截中,备注：【{}】", UserContext.getDefaultLoginUser().getUserName(), remark);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "发货拦截");
            if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
                thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.INTERCEPTING.getStatus());
                thirdWarehouseDeliveryFeign.update(thirdWarehouseDeliveryEntity);
            }
            return BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "发起拦截中，等待三方仓处理");
        } else {
            //自动拦截结果确认，拦截失败
            String msg = CharSequenceUtil.format("用户【{}】发起海外仓拦截失败，备注：【{}】,原因：【{}】", UserContext.getDefaultLoginUser().getUserName(),remark, stringApiResult.getMsg());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "发货拦截");
            return BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "三方仓拦截失败：" + stringApiResult.getMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelDeliveryIntercept(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //未打标拦截的订单不支持取消拦截
        if (!entity.getIsIntercept()) {
            throw new ServiceException(ApiError.NOT_INTERCEPT_NOT_CANCEL_INTERCEPT);
        }

        //物流商处理状态为空
        List<SoB2cDeliveryInterceptEntity> interceptEntities = soB2cDeliveryInterceptFeign.listBySourceIds(Arrays.asList(id));
        List<SoB2cDeliveryInterceptEntity> interceptList = interceptEntities.stream()
                .filter(req -> StringUtils.isBlank(req.getInterceptStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(interceptList)) {
            throw new ServiceException(ApiError.INTERCEPT_STATUS_IS_NOT_BLANK);
        }

        //订单拦截正在处理或已处理完成，无法取消拦截
        List<SoB2cDeliveryInterceptEntity> interceptStatusList = interceptEntities.stream()
                .filter(req -> InterceptStatusEnum.SUCCESS.getCode().equals(req.getInterceptStatus())
                        || CancelStatusEnum.SUCCESS.getCode().equals(req.getCancelStatus())
                        || StringUtils.isNotBlank(req.getHandleResult())
                ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(interceptStatusList)) {
            throw new ServiceException(ApiError.STATUS_END_NOT_INTERCEPT);
        }

        soB2cDeliveryInterceptFeign.updateHandleStatus(Arrays.asList(entity.getId()), SoB2cDeliveryInterceptStatusEnum.CANCEL.getCode());

        //修改拦截打标识、冻结订单
        SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
        interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
        interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
        interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getId()));
        Boolean flag = this.updateIntercept(interceptUpdateOrderDTO);
        if (flag) {
            // 操作日志
            String msg = CharSequenceUtil.format("用户【{}】取消【{}】，已解冻单据单号【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货拦截", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消发货拦截");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消发货拦截");
    }

    @Override
    public PagingVO<SoB2cDTO.MergeListDTO> mergePaging(PagingDTO<SoB2cDTO.MergePagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
        	dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        pagingParamDTO.getParams().setDynamicDataSource(dynamicDataSource);
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoB2cDTO.MergeListDTO> pageData = this.baseMapper.mergePaging(query, pagingParamDTO.getParams());
        List<SoB2cDTO.MergeListDTO> records = pageData.getRecords();
        fillMergeData(records, pagingParamDTO.getPermissionSql());
        return new PagingVO(pageData);
    }

    @Override
    public Integer mergePagingCount(SoB2cDTO.MergePagingParamDTO pagingParamDTO) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
        	dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        pagingParamDTO.setDynamicDataSource(dynamicDataSource);
        List<Integer> list = this.baseMapper.mergePagingCount(pagingParamDTO);
        return CollectionUtils.isEmpty(list) ? MathUtil.ZERO : list.stream().reduce(MathUtil.ZERO, Integer::sum);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String mergeSave(List<String> ids) {
        if (MathUtil.TWO.intValue() > ids.size()) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_SIZE);
        }

        List<SoB2cEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        // 销售明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //关联关系信息
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);

        List<SoB2cLogisticsEntity> logisticsEntityList = soB2cLogisticsService.listByMainIds(ids);

        for (SoB2cEntity entity : list) {
            if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || entity.getInvalidStatus()
                    || SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(entity.getBillStatus()) || SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(entity.getBillStatus())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SAVE_MERGE_INVALID);
            }
            if (entity.getIsNotMerge()) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_IS_NOT_NEED_MERGE_EXIST, entity.getCode());
            }
            SoB2cLogisticsEntity soB2cLogisticsEntity = logisticsEntityList.stream().filter(v -> v.getMainId().equals(entity.getId())).findFirst().orElse(null);
            if (Objects.nonNull(soB2cLogisticsEntity) && StringUtils.isNotBlank(soB2cLogisticsEntity.getCode())) {
                throw new ServiceException("已获取跟踪号不允许合并");
            }
            //已合并或拆分的单不支持再次合并
            long count = soB2cRefList.stream().filter(obj -> obj.getSourceId().equals(entity.getId()) || obj.getTargetId().equals(entity.getId())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_OPERATE_NOT_MERGE);
            }
            //未付款数据不能操作
            soB2cCoreService.checkPayMent(entity);

            //fba订单不支持合并
            String mainLabelJson = entity.getLabelJson();
            SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(mainLabelJson, SoB2cDTO.LabelJsonDTO.class);
            if ("AFN".equals(labelJsonDTO.getFulfillmentChannel())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_FBA, entity.getCode());
            }
            //菜鸟官方仓订单不支持合并
            long cainiaoCount = soB2cDetailList.stream().filter(obj -> obj.getMainId().equals(entity.getId()) && obj.getLabelJson().contains("cainiaoInternationalWarehouse")).count();
            if (cainiaoCount > 0) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_CAINIAO, entity.getCode());
            }
            //速卖通非已税订单不支持合并
            long taxCount = soB2cDetailList.stream().filter(obj -> obj.getMainId().equals(entity.getId()) && (obj.getLabelJson().contains("U_TAXED") || obj.getLabelJson().contains("I_TAXED"))).count();
            if (taxCount > 0) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_TAX, entity.getCode());
            }
            //shopee订单不支持合并
            if (PlatformDictEnum.SHOPEE.getCode().equals(entity.getDictPlatform())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SHOPEE_NOT_MERGE, entity.getCode());
            }
            //美客多订单不支持合并
            if (PlatformDictEnum.MERCADOLIBRE.getCode().equals(entity.getDictPlatform())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERCADO_NOT_MERGE, entity.getCode());
            }
            //美客多订单不支持合并
            if (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(entity.getDictPlatform())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERCADO_NOT_MERGE, entity.getCode());
            }
            //TikTok订单不支持合并
            if (PlatformDictEnum.TIK_TOK.getCode().equals(entity.getDictPlatform())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_TIKTOK_NOT_MERGE, entity.getCode());
            }
        }

        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //买家信息
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cReceiverService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cReceiverList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }

        //检验合并数据
        checkMergeData(list, soB2cLogisticsList, soB2cReceiverList, soB2cDetailList);

        //新增合并后数据
        SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
        BeanMapperUtils.copy(list.get(0), addDTO);
        BigDecimal totalAmount = list.stream().map(SoB2cEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        addDTO.setAmount(totalAmount);
        // 税后金额合并
        BigDecimal afterTaxAmount = list.stream().map(SoB2cEntity::getAfterTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        addDTO.setAfterTaxAmount(afterTaxAmount);
        //合并后取最小单据日期
        LocalDate billDate = list.stream().map(SoB2cEntity::getBillDate).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setBillDate(billDate);
        //合并后取最小创建日期
        LocalDateTime createTime = list.stream().map(SoB2cEntity::getCreateTime).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setCreateTime(createTime);
        //合并后取最小付款时间
        LocalDateTime payTime = list.stream().map(SoB2cEntity::getPayTime).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setPayTime(payTime);
        addDTO.setSourceType(SourceTypeEnum.SELF_ADD.getCode());

        //物流信息
        SoB2cLogisticsDTO.AddDTO logisticsAddDTO = new SoB2cLogisticsDTO.AddDTO();
        BeanMapperUtils.copy(soB2cLogisticsList.get(0), logisticsAddDTO);

        //查询汇率
        BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), list.get(0).getCurrency());
        if (ObjectUtils.isEmpty(rate) || MathUtil.compareTo(BigDecimal.ZERO, rate) == MathUtil.ZERO) {
            throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, billDate, list.get(0).getCurrency());
        }

        //预估运费(合并后默认转本位币)
        BigDecimal estimatedShippingCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getEstimatedShippingCurrency()) ?
                obj.getEstimatedShippingCost() : MathUtil.multiplyWithTwo(obj.getEstimatedShippingCost(), rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setEstimatedShippingCost(estimatedShippingCost);
        //实际运费(合并后默认转本位币)
        BigDecimal actualShippingCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getActualShippingCurrency()) ?
                obj.getActualShippingCost() : MathUtil.multiplyWithTwo(obj.getActualShippingCost(), rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setActualShippingCost(actualShippingCost);
        //包装辅料费(合并后默认转本位币)
        BigDecimal accessoriesCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getAccessoriesCostCurrency()) ?
                obj.getAccessoriesCost() : MathUtil.multiplyWithTwo(obj.getAccessoriesCost(), rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setAccessoriesCost(accessoriesCost);
        //包装辅料数量
        Integer accessoriesQty = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getAccessoriesQty).reduce(MathUtil.ZERO, Integer::sum);
        logisticsAddDTO.setAccessoriesQty(accessoriesQty);
        //包装辅料净重
        BigDecimal accessoriesNw = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getAccessoriesNw).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setAccessoriesNw(accessoriesNw);
//        //长
//        BigDecimal height = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getHeight).reduce(BigDecimal.ZERO, BigDecimal::add);
//        logisticsAddDTO.setHeight(height);
//        //宽
//        BigDecimal width = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getWidth).reduce(BigDecimal.ZERO, BigDecimal::add);
//        logisticsAddDTO.setWidth(width);
//        //高
//        BigDecimal weight = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
//        logisticsAddDTO.setWeight(weight);

        logisticsAddDTO.setLength(null);
        logisticsAddDTO.setWidth(null);
        logisticsAddDTO.setHeight(null);
        logisticsAddDTO.setWeight(null);
        //操作类型
        addDTO.setOperateType(SoB2cOptionTypeEnum.ENUM_MERGE);
        addDTO.setLogisticsDTO(logisticsAddDTO);
        //买家信息
        SoB2cReceiverDTO.AddDTO receiverAddDTO = new SoB2cReceiverDTO.AddDTO();
        BeanMapperUtils.copy(soB2cReceiverList.get(0), receiverAddDTO);
        addDTO.setReceiverDTO(receiverAddDTO);

        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(ids);
        if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
            List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).distinct().collect(Collectors.toList());
            addDTO.setCategoryIdList(categoryIdList);
        }

        //明细信息
        List<SoB2cDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (SoB2cDetailEntity detailEntity : soB2cDetailList) {
            SoB2cDetailDTO.AddDTO detailAddDTO = new SoB2cDetailDTO.AddDTO();
            BeanMapperUtils.copy(detailEntity, detailAddDTO);
            detailAddDTO.setOperateDetailId(detailEntity.getId());
            // 记录来源明细ID（作为合并订单触发平台标记发货的依据）
            detailAddDTO.setSourceDetailId(detailEntity.getSourceDetailId());
            detailList.add(detailAddDTO);
        }
        // 源单号列表
        String codes = list.stream().map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        // 源平台订单号列表
        String platformCodeListStr = list.stream().map(SoB2cEntity::getPlatformCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(platformCodeListStr) && platformCodeListStr.length() > 1024) {
            throw new ServiceException(ApiError.ERROR_92162);
        }

        String remark;
        if (StringUtils.isBlank(platformCodeListStr)) {
            remark = CharSequenceUtil.format("订单【{}】合并新订单", codes);
        } else {
            remark = CharSequenceUtil.format("订单【{}】合并新订单,平台订单号【{}】 ", codes, platformCodeListStr);
        }
        addDTO.setRemark(remark);
        //合并后的平台订单号
        addDTO.setPlatformCode(platformCodeListStr);
        addDTO.setDetailList(detailList);
        log.info("新增合并后的B2C销售订单，addDTO = {}", addDTO);
        //新增数据
        SoB2cEntity add = this.add(addDTO, null);
        String soId = add.getId();
        //作废原单
        for (String id : ids) {
            //作废
            this.invalid(id, CharSequenceUtil.format("B2C销售订单合并作废，合并后订单【{}】", add.getCode()), SoB2cInvalidTypeEnum.ENUM_MERGE);
        }
        //操作日志
        SoB2cEntity soB2cEntity = this.getById(soId);
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), soB2cEntity.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(CharSequenceUtil.format("合并到新订单【{}】", add.getCode()), ModuleTypeEnum.SO_B2C.getCode(), pairList, "合并订单");
        return soId;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelMerge(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待提交或审核不通过允许取消
        if (!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_CANCEL_MERGE, entity.getCode());
        }
        //关联数据
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listByTargetId(id, SoB2cOptionTypeEnum.ENUM_MERGE);
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CANCEL_MERGE_NOT_EXIST, entity.getCode());
        }
        log.info("删除销售订单数据，id = {}", id);
        //删除合并后的数据
        deleteById(Arrays.asList(id), null);
        //反作废合并前的数据
        List<String> sourceIdList = soB2cRefList.stream().map(SoB2cRefEntity::getSourceId).distinct().collect(Collectors.toList());
        for (String sourceId : sourceIdList) {
            log.info("作废原销售订单数据，id = {}", sourceId);
            unInvalid(sourceId, SoB2cInvalidTypeEnum.ENUM_MERGE);
        }
        //操作日志
        String msg = "B2C销售订单【{}】取消合并";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消合并");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消合并");
    }

    /**
     * 删除订单关联信息
     *
     * @param ids  需要删除的销售订单数据
     * @param code 关联销售订单编码
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 14:09
     */
    @Override
    public void deleteById(List<String> ids, String code) {
        //存在物流单需要先取消物流单
        batchCancelLogistic(ids, code);
        //删除物流信息
        soB2cLogisticsService.deleteByMainIds(ids);
        //删除买家信息
        soB2cReceiverService.deleteByMainIds(ids);
        //删除明细信息
        soB2cDetailService.deleteByMainIds(ids);
        //删除关联关系
        soB2cRefService.deleteByTargetIds(ids);
        //删除分类信息
        soB2cRefCategoryService.deleteByMainIds(ids);
        //删除主表信息
        this.removeByIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);


        //同步数帝云
//        List<SoB2cEntity> soB2cEntities = this.listByIds(ids);
//        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(ids);
//        for (SoB2cEntity soB2cEntity : soB2cEntities) {
//            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
//            syncSoB2cService.syncDataToSdy(soB2cEntity, detailEntityList, SyncOperateEnum.OPERATE_DELETE.getCode());
//        }
    }

    /**
     * 根据销售订单id 批量取消订单
     *
     * @param ids  需要删除的销售订单数据
     * @param code 关联销售订单编码
     */
    private void batchCancelLogistic(List<String> ids, String code) {
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(ids);
        List<String> soIds = soB2cLogisticsEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getCode())
                || CharSequenceUtil.isNotBlank(e.getTrackNo()) || CharSequenceUtil.isNotBlank(e.getLogisticsChannelId()))
                .map(SoB2cLogisticsEntity::getMainId).distinct().collect(Collectors.toList());
        //没有已下单的物流单 不需要取消
        if (CollectionUtils.isEmpty(soIds)) {
            return;
        }
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        for (String id : soIds) {
            SoB2cEntity entity = soB2cEntityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                throw new ServiceException(CharSequenceUtil.format("销售订单【{}】未找到", id));
            }
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(id, e.getMainId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cLogisticsEntity) || StrUtil.isBlank(soB2cLogisticsEntity.getCode())) {
                continue;
            }
            try {
                BatchResultDTO resultDTO = soB2cLogisticsService.cancelLogistic(id, soB2cEntityList, soB2cLogisticsEntityList, true);
                if (!resultDTO.getSuccess()) {
                    throw new ServiceException(resultDTO.getMsg());
                }
            } catch (Exception e) {
                log.error("B2C销售订单取消物流单失败", e);
                if (CharSequenceUtil.isNotBlank(code)) {
                    throw new ServiceException(CharSequenceUtil.format("【{}】的关联子订单【{}}】取消物流单失败，请联系物流同事处理", code, entity.getCode()));
                }
                throw new ServiceException(CharSequenceUtil.format("销售订单【{}}】取消物流单失败，请联系物流同事处理", entity.getCode()));
            }
        }
    }


    @Override
    public SoB2cDTO.ViewDTO view(String id) {
        SoB2cEntity soB2cEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        SoB2cDTO.ViewDTO data = B2cOrderConverter.INSTANCE.convertEntityToViewDTO(soB2cEntity);
        //物流
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsDTO.ViewDTO logisticsDTO = new SoB2cLogisticsDTO.ViewDTO();
        BeanMapperUtils.copy(soB2cLogisticsEntity, logisticsDTO);
        //渠道id
        String logisticsChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        String logisticsChannelName = "";
        if (StringUtils.isNotBlank(logisticsChannelId)) {
            LogisticsChannelEntity channelEntity = logisticsFeign.getChannelById(logisticsChannelId);
            if (Objects.nonNull(channelEntity)) {
                logisticsChannelName = channelEntity.getName();
            }
        }
        List<LogisticsSaleChannelEntity> list = FeignQuery.list(FeignQuery.create(LogisticsSaleChannelEntity.class)
                .eq(LogisticsSaleChannelEntity::getCode, logisticsChannelId)
        );
        if (StringUtils.isBlank(logisticsChannelName)) {
            logisticsChannelName = CollectionUtils.isNotEmpty(list) ? list.get(0).getCnName() : "";
        }

        String transferLogisticsChannelId = soB2cLogisticsEntity.getTransferLogisticsChannelId();
        List<TransferLogisticsChannelDTO.ListSelectDTO> transferInfoList = transferLogisticsFeign.listByTransferChannelIds(Arrays.asList(transferLogisticsChannelId));
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            TransferLogisticsChannelDTO.ListSelectDTO transferInfo = transferInfoList.get(0);
            logisticsDTO.setTransferLogisticsSupplierId(transferInfo.getTransferLogisticsSupplierId());
            logisticsDTO.setTransferLogisticsSupplierName(transferInfo.getTransferLogisticSupplierName());
            logisticsDTO.setTransferLogisticsChannelId(transferInfo.getId());
            logisticsDTO.setTransferLogisticsChannelName(transferInfo.getName());
        }
        logisticsDTO.setLogisticsChannelName(logisticsChannelName);
        //将销售出库单日期赋值到发货日期
        List<SoOutstockEntity> soOutstockEntityList = soOutstockFeign.listBySoIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(soOutstockEntityList) && Objects.nonNull(soOutstockEntityList.get(0).getBillDate())) {
            logisticsDTO.setDeliveryTime(soOutstockEntityList.get(0).getBillDate().atStartOfDay());
        }
        if(CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getDeclareOrgId())){
            SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(soB2cLogisticsEntity.getDeclareOrgId());
            logisticsDTO.setDeclareOrgId(Objects.nonNull(company)?company.getId():"");
            logisticsDTO.setDeclareOrgName(Objects.nonNull(company)?company.getCompanyName():"");
            logisticsDTO.setUsciCode(Objects.nonNull(company)?company.getUsciCode():"");
        }
//        logisticsDTO.setActualShippingCost(logisticsBillCostFeign.getActualLogisticCost(soB2cEntity.getId()));
        data.setLogisticsDTO(logisticsDTO);
        if (isFullyManagedOrder(soB2cEntity.getDictPlatform())){
            //扩展信息
            SoB2cExtendEntity soB2cExtendEntity = soB2cExtendService.getByMainId(id);
            if (Objects.nonNull(soB2cExtendEntity) && StringUtils.isNotBlank(soB2cExtendEntity.getPartitionId())) {
                DictPartitionEntity dictPartitionEntity = FeignQuery.getById(DictPartitionEntity.class,soB2cExtendEntity.getPartitionId());
                if(Objects.nonNull(dictPartitionEntity)){
                    soB2cExtendEntity.setPartitionName(dictPartitionEntity.getName());
                    soB2cExtendEntity.setPartitionCode(dictPartitionEntity.getCode());
                }
            }
            SoB2cExtendDTO.ViewDTO extendDTO = new SoB2cExtendDTO.ViewDTO();
            BeanMapperUtils.copy(soB2cExtendEntity, extendDTO);
            data.setExtendDTO(extendDTO);
        }else {
            //买家
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(id);
            if (ObjectUtils.isEmpty(soB2cReceiverEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
            }
            if(StringUtils.isNotBlank(soB2cReceiverEntity.getPartitionId())){
                DictPartitionEntity dictPartitionEntity = FeignQuery.getById(DictPartitionEntity.class,soB2cReceiverEntity.getPartitionId());
                if(Objects.nonNull(dictPartitionEntity)){
                    soB2cReceiverEntity.setPartitionName(dictPartitionEntity.getName());
                    soB2cReceiverEntity.setPartitionCode(dictPartitionEntity.getCode());
                }
            }
            SoB2cReceiverDTO.ViewDTO receiverDTO = new SoB2cReceiverDTO.ViewDTO();
            BeanMapperUtils.copy(soB2cReceiverEntity, receiverDTO);

            data.setReceiverDTO(receiverDTO);
        }
        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
            List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).distinct().collect(Collectors.toList());
            data.setCategoryIdList(categoryIdList);
        }
        //发货单--提交发货时间、面单打印时间
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryFeign.listBySourceId(Collections.singletonList(id))
                .stream()
                .filter(v -> !v.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode()))
                .sorted(Comparator.comparing(SoB2cDeliveryEntity::getCreateTime).reversed())//降序
                .collect(Collectors.toList());
        ;
        if (CollectionUtils.isNotEmpty(soB2cDeliveryEntities)) {
            SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryEntities.get(0);
            data.setFinishPrintTime(soB2cDeliveryEntity.getFinishPrintTime());
            data.setCreateDeliveryTime(soB2cDeliveryEntity.getCreateTime());
        }

        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //财务信息
        SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceService.getByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }
        List<String> platformSkuNoList = soB2cDetailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByParam(RuleTypeEnum.B2C_PLATFORM.getCode(), null, platformSkuNoList);
        //财务信息
        SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
        dto.setId(soB2cEntity.getId());
        dto.setIsCny(Boolean.TRUE);
        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(soB2cLogisticsEntity);
        dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
        dto.setSoB2cDetailList(soB2cDetailList);
        SoB2cDTO.FinancialInfoDTO financialInfo = this.getFinancialInfo(dto, Boolean.FALSE);
        data.setFinancialInfoDTO(financialInfo);

        List<SoB2cDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(SoB2cDetailDTO.ViewDTO.class, soB2cDetailList);

        List<String> skuIdList = detailList.stream().map(SoB2cDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        detailList.forEach(v -> {
            //子级BOM
            List<BomChildrenSkuDTO> childList = bomChildrenList.stream()
                    .filter(req -> req.getParentSkuId().equals(v.getSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(childList)) {
                v.setIsCombination(true);
            }
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(obj -> {
                return v.getSourcePlatform().equals(SoB2cSourcePlatformEnum.ENUM_THIRD_PLATFORM.getCode())
                        && obj.getPlatformSkuNo().equals(v.getPlatformSkuNo()) && obj.getPlatform().equals(data.getDictPlatform())
                        &&   obj.getPlatformSpuNo().equals(v.getPlatformSpuNo());
            }).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(listingInfoEntity)) {
                v.setImageUrl(listingInfoEntity.getProductImageUrl());
            }
        });
        data.setDetailList(detailList);
        List<SoB2cDeclareProductEntity> declareProductList = soB2cDeclareProductService.listBySoId(id);
        List<SoB2cDeclareProductDTO.ViewDTO> declareProductViewList = BeanMapperUtils.copyList(SoB2cDeclareProductDTO.ViewDTO.class, declareProductList);
        data.setDeclareProductList(declareProductViewList);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/
    public Boolean startProcess(SoB2cEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
        startDTO.setBusinessName(entity.getCode());
        String userId = UserContext.getDefaultLoginUser().getUid();
        startDTO.setUserId(userId);
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }else {
            return result.getData().getIsExistProcess();
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(SoB2cEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        //销售订单编号和物流信息编号都是code，需要区分
        variablesMap.put("soCode", entity.getCode());
        //店铺名称
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(entity.getShopId());
        if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
            variablesMap.put("shopName", shopInfoEntity.getName());
        }
        //销售平台名称
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        if (CollectionUtils.isNotEmpty(dictList)) {
            String name = dictList.stream().filter(obj -> obj.getValue().equals(entity.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            variablesMap.put("dictPlatformName", name);
        }
        //单据子类型
        variablesMap.put("transactionSubTypeName", OrderSubTypeEnum.getName(entity.getTransactionSubType()));
        //单据状态名称
        variablesMap.put("billStatusName", SoB2cBillStatusEnum.getName(entity.getBillStatus()));
        //单据审核状态
        variablesMap.put("approveStatusName", entity.getApproveStatus().getName());

        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<String> skuIdList = detailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(skuIdList);
        Map<String, String> map = CollUtil.isEmpty(skuList) ? new HashMap<>() : skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
        //产品名称
        for (SoB2cDetailEntity detailEntity :detailList) {
            String productName = map.get(detailEntity.getSkuId());
            detailEntity.setProductName(productName);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
        if (ObjectUtil.isNotEmpty(soB2cLogisticsEntity)) {
            variablesMap.put("logisticsDTO",BeanUtil.beanToMap(soB2cLogisticsEntity));
        }

        //买家信息
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(entity.getId());
        if (ObjectUtil.isNotEmpty(soB2cReceiverEntity)) {
            variablesMap.put("receiverDTO",BeanUtil.beanToMap(soB2cReceiverEntity));
        }
        //分类信息
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isNotEmpty(soB2cRefCategoryList)) {
            variablesMap.put("soB2cRefCategoryDTO",BeanUtil.copyToList(soB2cRefCategoryList,Map.class));
        }
        //sku数量
        long skuCount = detailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().count();
        variablesMap.put("skuCount", skuCount);

        //总销售数量
        Integer qtyTotal = detailList.stream().map(SoB2cDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
        variablesMap.put("qtyTotal", qtyTotal);
        return variablesMap;
    }

    /**
     * 查询详情数据处理
     */
    private void fillOne(SoB2cDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(data.getShopId());
        if (ObjectUtils.isNotEmpty(shopInfoEntity)) {
            data.setShopName(shopInfoEntity.getName());
        }

        data.setApproveStatusName(data.getApproveStatus().getName());
        data.setBillStatusName(SoB2cBillStatusEnum.getName(data.getBillStatus()));
        //平台信息
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        if (CollectionUtils.isNotEmpty(dictList)) {
            String name = dictList.stream().filter(obj -> obj.getValue().equals(data.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setDictPlatformName(name);
        }

        //产品信息
        List<String> skuIdList = data.getDetailList().stream().map(SoB2cDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        Map<String, SkuVO> skuVOMap = new HashMap<>();
        List<SkuVO> skuList = plmTaskFeign.listSkuPackByIds(skuIdList);

        //子sku
        List<String> skuIds = skuList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> allBomChildrenSkuDTOList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        allBomChildrenSkuDTOList = allBomChildrenSkuDTOList.stream().filter(v -> BomTypeEnum.COMBINATION.getType().equals(v.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuList)) {
            skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }
        LogisticsSupplierDTO.AuthChannelViewDTO authChannelViewDTO = null;
        if(StringUtils.isNotBlank(data.getLogisticsDTO().getLogisticsChannelId())){
            List<LogisticsSupplierDTO.AuthChannelViewDTO> authChannelViewDTOList = logisticsAuthFeign.listAuthChannelView(Arrays.asList(data.getLogisticsDTO().getLogisticsChannelId()));
            if(CollectionUtils.isNotEmpty(authChannelViewDTOList)){
                authChannelViewDTO = authChannelViewDTOList.get(0);
            }
        }
        if(Objects.nonNull(authChannelViewDTO) && CollectionUtils.isNotEmpty(data.getDetailList())){
            //仓库id
            List<String> warehouseIdList = data.getDetailList().stream().map(SoB2cDetailDTO.ViewDTO::getWarehouseId).distinct().collect(Collectors.toList());
            //获取第三方仓海外信息
            List<OverseasProviderWarehouseDTO.ViewDTO> overseasProviderWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIdList);
            OverseasProviderWarehouseDTO.ViewDTO viewDTO = overseasProviderWarehouseList.stream().filter(obj -> obj.getWarehouseId().equals(data.getDetailList().get(0).getWarehouseId())).findFirst().orElse(null);
            if(Objects.nonNull(viewDTO)){
                data.setIsSelectChannel(!viewDTO.getProviderCode().equals(authChannelViewDTO.getLogisticsPlatform()));
            }
        }
        for (SoB2cDetailDTO.ViewDTO viewDTO : data.getDetailList()) {
            SkuVO skuVO = skuVOMap.get(viewDTO.getSkuId());
//            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
//            if (ObjectUtils.isEmpty(skuVO)) {
//                throw new ServiceException(ApiError.ERROR_95084);
//            }

            viewDTO.setProductName(null == skuVO ? "" : skuVO.getSkuName());
            //组合品的话根据子件计算长宽高重量
            List<BomChildrenSkuDTO> bomChildrenSkuDTOList = allBomChildrenSkuDTOList.stream().filter(v -> skuVO != null && v.getParentSkuId().equals(skuVO.getSkuId())).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(bomChildrenSkuDTOList)) {

                viewDTO.setProductLength(Objects.nonNull(skuVO) ? LengthConverterUtil.mmToCm(skuVO.getProductLength()) : BigDecimal.ZERO);
                viewDTO.setProductHeight(Objects.nonNull(skuVO) ? LengthConverterUtil.mmToCm(skuVO.getProductHeight()) : BigDecimal.ZERO);
                viewDTO.setProductWidth(Objects.nonNull(skuVO) ? LengthConverterUtil.mmToCm(skuVO.getProductWidth()) : BigDecimal.ZERO);
                viewDTO.setGrossWeight(Objects.nonNull(skuVO) ? skuVO.getGrossWeight() : BigDecimal.ZERO);
                viewDTO.setNetWeight(Objects.nonNull(skuVO) ? skuVO.getNetWeight() : BigDecimal.ZERO);
            } else {
                BigDecimal maxLength = bomChildrenSkuDTOList.stream().map(BomChildrenSkuDTO::getLength).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxWidth = bomChildrenSkuDTOList.stream().map(BomChildrenSkuDTO::getWidth).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal totalHeight = bomChildrenSkuDTOList.stream().map(e -> e.getHeight().multiply(new BigDecimal(e.getQuantity()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal totalGrossWeight = bomChildrenSkuDTOList.stream().map(e -> e.getGrossWeight().multiply(new BigDecimal(e.getQuantity()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal totalNetWeight = bomChildrenSkuDTOList.stream().map(e -> e.getNetWeight().multiply(new BigDecimal(e.getQuantity()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                viewDTO.setProductHeight(LengthConverterUtil.mmToCm(totalHeight));
                viewDTO.setProductLength(LengthConverterUtil.mmToCm(maxLength));
                viewDTO.setProductWidth(LengthConverterUtil.mmToCm(maxWidth));
                viewDTO.setGrossWeight(totalGrossWeight);
                viewDTO.setNetWeight(totalNetWeight);
            }
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus, Boolean isMatch,ApproveOneDTO dto) {
        String abnormalType = SoB2cAbnormalTypeEnum.ENUM_MANUAL_REJECT.getCode();
        if (Objects.nonNull(isMatch) && isMatch) {
            abnormalType = SoB2cAbnormalTypeEnum.ENUM_APPROVE_REJECT.getCode();
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        String uid = userInfo.getUid();
        String userName = userInfo.getUserName();
        if(dto.getIsSubmitAutoApprove()){
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserName("system",UserTypeEnum.ERP.code);
            if(Objects.nonNull(findUserDTO)){
                uid = findUserDTO.getUserId();
                userName = findUserDTO.getUserName();
            }
        }
        this.lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getApproveStatus, approveStatus)
                .set(ApproveStatusEnum.REJECT.getStatus().equals(approveStatus), SoB2cEntity::getAbnormalType, abnormalType)
                .set(ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus), SoB2cEntity::getIsMatchOrderRule, Boolean.TRUE)
                .set(ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus), SoB2cEntity::getApproveTime, LocalDateTime.now())
                .set(ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus), SoB2cEntity::getApproveUserId, uid)
                .set(ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus), SoB2cEntity::getApproveUserName, userName)
                .update(new SoB2cEntity());
    }


    /**
     * 更新审核状态
     */
    private void updateApproveStatus(String id, String approveStatus, Boolean isCleanError) {
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getApproveStatus, approveStatus)
                .set(SoB2cEntity::getApproveTime, null)
                .set(SoB2cEntity::getApproveUserId, "")
                .set(SoB2cEntity::getApproveUserName, "")
                .set(SoB2cEntity::getAbnormalType, "")
                .set(isCleanError, SoB2cEntity::getSignOrderError, "")
                .update(new SoB2cEntity());
    }

    /**
     * @param categoryIdList
     * @param mainId
     * @description: 新增分类
     * @author Will
     * @date: 2023/8/22 14:22
     */
    private void addCategory(List<String> categoryIdList, String mainId, List<OrderCategoryDetailEntity> categoryList) {
        List<SoB2cRefCategoryEntity> list = soB2cRefCategoryService.listByMainIds(Arrays.asList(mainId));
        List<SoB2cRefCategoryEntity> addList = new ArrayList<>();
        for (String categoryId : categoryIdList) {
            //如果已存在分类则无需新增
            if (CollectionUtils.isNotEmpty(list)) {
                long count = list.stream().filter(obj -> obj.getCategoryId().equals(categoryId)).count();
                if (count > 0) {
                    log.info("已存在分类，categoryId = {}", categoryId);
                    continue;
                }
            }
            String name = categoryList.stream().filter(obj -> obj.getId().equals(categoryId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            SoB2cRefCategoryEntity entry = new SoB2cRefCategoryEntity();
            entry.setCategoryId(categoryId);
            entry.setCategoryName(name);
            entry.setSoB2cId(mainId);
            addList.add(entry);
        }
        soB2cRefCategoryService.saveBatch(addList);
    }

    /**
     * @param categoryIdList
     * @param mainId
     * @description: 修改分类
     * @author Will
     * @date: 2023/8/22 14:36
     */
    private void updateCategory(List<String> categoryIdList, String mainId, List<OrderCategoryDetailEntity> categoryList) {
        //删除原有分类
        deleteCategory(mainId);
        //新增分类
        addCategory(categoryIdList, mainId, categoryList);
    }

    /**
     * @param mainId
     * @description: 删除已有分类
     * @author Will
     * @date: 2023/8/22 14:30
     */
    private void deleteCategory(String mainId) {
        soB2cRefCategoryService.deleteByMainIds(Arrays.asList(mainId));
    }

    /**
     * @param mainId
     * @description: 删除选择分类
     * @author Will
     * @date: 2023/8/22 14:30
     */
    private void deleteSelectCategory(String mainId, List<String> categoryIdList) {
        soB2cRefCategoryService.deleteByMainIdAndCategoryId(mainId, categoryIdList);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<SoB2cDTO.ListDTO> list, Boolean isFullyManaged) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //店铺
        List<String> shopIdList = list.stream().map(SoB2cDTO.ListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopIdList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        // 国家信息
        List<String> countryList = list.stream()
                .filter(e -> StringUtils.isNotBlank(e.getCountry()))
                .map(SoB2cDTO.ListDTO::getCountry)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(countryList)) {
            countryNameMap = sysDictFeign.listCountryByIds(countryList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }


        List<String> ids = list.stream().map(SoB2cDTO.ListDTO::getId).collect(Collectors.toList());

        List<SoB2cEntity> allList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        List<SoB2cDetailEntity> allDetailList = soB2cDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(allDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //查询流程审核信息
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_CHANGE.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
        }

        List<SoB2cDeclareProductDTO.ViewDTO> declareProductList = soB2cDeclareProductService.listViewBySoIds(ids);
        //产品信息
        List<String> skuIdList = list.stream().flatMap(obj -> Stream.of(allDetailList.stream().map(SoB2cDetailEntity::getSkuId).toArray(String[]::new))).distinct().collect(Collectors.toList());
        Map<String, SkuVO> skuVOMap = new HashMap<>();
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIdList);
        if (!CollectionUtils.isEmpty(skuList)) {
            skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        List<String> childSkuIdList = bomChildrenList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getSkuId()))
                .map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(childSkuIdList)) {
            skuIdList.addAll(childSkuIdList);
        }

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : com.google.common.collect.Lists.newArrayList();

        //仓库id
        List<String> warehouseIdList = allDetailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        //获取第三方仓海外信息
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasProviderWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIdList);
        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode()));
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);

        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);

        //虚拟仓库存
        List<String> virtualWarehouseIdList = allDetailList.stream().map(SoB2cDetailEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        paramDTO.setDictInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
        paramDTO.setSkuIdList(skuIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryFeign.listInventoryQty(paramDTO);
        //虚拟仓库信息
        List<VirtualWarehouseEntity> virtualWarehouseList = CollectionUtils.isEmpty(virtualWarehouseIdList) ? new ArrayList<>() : FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //物流信息
        List<SoB2cLogisticsEntity> logisticsEntityList = soB2cLogisticsService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(logisticsEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        List<String> channelIds = logisticsEntityList.stream().map(SoB2cLogisticsEntity::getLogisticsChannelId).filter(logisticsChannelId ->StringUtils.isNotBlank(logisticsChannelId)).distinct().collect(Collectors.toList());
        List<LogisticsSupplierDTO.AuthChannelViewDTO> authChannelViewDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(channelIds)) {
            authChannelViewDTOList = logisticsAuthFeign.listAuthChannelView(channelIds);
        }
        List<LogisticsBillDTO.LogisticsBillVo> billVos = list.stream()
                .filter(bill -> StringUtils.isNotBlank(bill.getLogisticsCode()))
                .map(bill -> {
                    LogisticsBillDTO.LogisticsBillVo logisticsBillVo = new LogisticsBillDTO.LogisticsBillVo();
                    logisticsBillVo.setSourceId(bill.getId());
                    logisticsBillVo.setTrackNo(bill.getLogisticsCode());
                    return logisticsBillVo;
                }).collect(Collectors.toList());
        //获取运输状态
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> logisticsBillMap = null;
        if (CollectionUtils.isNotEmpty(billVos)) {
            logisticsBillMap = logisticsBillFeign.getTrackStatusByTrackNo(billVos)
                    .stream().collect(Collectors.groupingBy(LogisticsBillDTO.LogisticsBillVo::getTrackNo));
        }
        //财务信息
        List<SoB2cFinanceEntity> soB2cFinanceEntityList = soB2cFinanceService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cFinanceEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }

        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = allDetailList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getSkuNo())).map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(), obj.getWarehouseId(), allList.stream().filter(e -> e.getId().equals(obj.getMainId())).findFirst().flatMap(e -> Optional.ofNullable(e.getDictPlatform())).orElse(""))).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> skuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);
        String bomType = BomTypeEnum.COMBINATION.getType();
        List<String> platformSkuNoList = allDetailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByParam(RuleTypeEnum.B2C_PLATFORM.getCode(), null, platformSkuNoList);
        //中转信息
        List<String> transferLogisticsChannelIdList = list.stream().map(SoB2cDTO.ListDTO::getTransferLogisticsChannelId).distinct().collect(Collectors.toList());
        List<TransferLogisticsChannelDTO.ListSelectDTO> transferInfoList = transferLogisticsFeign.listByTransferChannelIds(transferLogisticsChannelIdList);

        //手动标发标记数据处理
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryFeign.listBySourceId(ids);
        Map<String, String> shippedMap = new HashMap<>();
        Map<String, String> manualMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(soB2cDeliveryEntities)) {
            shippedMap = soB2cDeliveryEntities.stream()
                    .filter(v -> StringUtils.isNotBlank(v.getStatus()) && v.getStatus().equals(SoB2cDeliveryStatusEnum.SHIPPED.getCode()))
                    .collect(Collectors.toMap(SoB2cDeliveryEntity::getSourceId, SoB2cDeliveryEntity::getStatus,(existing, replacement) -> existing ));
            manualMap = soB2cDeliveryEntities.stream()
                    .filter(v -> StringUtils.isNotBlank(v.getShipmentMark())
                            && v.getShipmentMark().equals(ShipmentMarkTypeEnum.MANUAL.getCode()))
                    .collect(Collectors.toMap(
                            SoB2cDeliveryEntity::getSourceId,
                            SoB2cDeliveryEntity::getShipmentMark,
                            (existing, replacement) -> existing ));
        }
        //获取多渠道异常信息
        List<String> soIds = list.stream().filter(e -> SoB2cMultiChannelTypeEnum.AMAZON_FAILED.getCode().equals(e.getMultiChannelType())).map(SoB2cDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<SoMultiChannelEntity> soMultiChannelEntityList = null;
        if (CollUtil.isNotEmpty(soIds)){
            soMultiChannelEntityList = soMultiChannelService.getLastBySoId(soIds, CreateStatusEnum.FAILED.getCode());
        }

        //查询发货单号（so_b2c_delivery 、 third_warehouse_delivery）
        Map<String, String> deliveryCodeMap = soB2cDeliveryFeign.getDeliveryCodeBySourceId(ids);

        // 属性赋值
        for (SoB2cDTO.ListDTO data : list) {
            data.setMultiChannelTypeName(SoB2cMultiChannelTypeEnum.getName(data.getMultiChannelType()));
            if (SoB2cMultiChannelTypeEnum.AMAZON_FAILED.getCode().equals(data.getMultiChannelType())){
                if (CollUtil.isNotEmpty(soMultiChannelEntityList)){
                    SoMultiChannelEntity soMultiChannelEntity = soMultiChannelEntityList.stream().filter(v -> v.getSoId().equals(data.getId())).max(Comparator.comparing(SoMultiChannelEntity::getCreateTime)).orElse(null);
                    if (Objects.nonNull(soMultiChannelEntity)){
                        data.setMultiChannelTypeName(data.getMultiChannelTypeName() + soMultiChannelEntity.getSignOrderError());
                    }
                }
            }
            if (StringUtils.isNotBlank(data.getLogisticsCode()) && Objects.nonNull(logisticsBillMap)) {
                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillMap.get(data.getLogisticsCode());
                if (CollectionUtils.isNotEmpty(logisticsBillVos)) {
                    LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVos.get(0);
                    //运输状态
                    data.setTrackStatus(logisticsBillVo.getTrackStatus());
                    data.setTrackStatusName(logisticsBillVo.getTrackStatusName());
                }
            }
            data.setVatInvoiceStatusName(SoB2cVatStatusEnum.getName(data.getVatInvoiceStatus()));
            data.setNfeInvoiceStatusName(SoB2cNfeStatusEnum.getName(data.getNfeInvoiceStatus()));
            //店铺
            ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> obj.getId().equals(data.getShopId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(shopInfoEntity)) {
                data.setShopName(shopInfoEntity.getName());
            }
            // 国家=买家信息国家
            String countryName = countryNameMap.getOrDefault(data.getCountry(), "");
            data.setCountryName(countryName);

            //重量单位,默认g
            data.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());

            //中转信息
            TransferLogisticsChannelDTO.ListSelectDTO transferInfo = transferInfoList.stream().filter(v -> v.getId().equals(data.getTransferLogisticsChannelId())).findFirst().orElse(null);
            if (Objects.nonNull(transferInfo)) {
                data.setTransferLogisticsSupplierName(transferInfo.getTransferLogisticSupplierName());
                data.setTransferLogisticsChannelName(transferInfo.getName());
            }

            //单据状态
            data.setStatus(data.getBillStatus());
            data.setStatusName(CharSequenceUtil.format("{}-{}", ApproveStatusEnum.getName(data.getApproveStatus()), SoB2cBillStatusEnum.getName(data.getBillStatus())));

            //异常信息名称
            data.setAbnormalTypeName(SoB2cAbnormalTypeEnum.getName(data.getAbnormalType()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(data.getId()) && org.apache.commons.lang3.StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }

            //标签处理
            String label = data.getLabel();
            SoB2cDTO.LabelDTO labelDTO = new SoB2cDTO.LabelDTO();
            labelDTO.setIsIntercept(data.getIsIntercept());
            labelDTO.setIsManual(data.getSourceType().equals(SourceTypeEnum.SELF_ADD.getCode()));

            if (CollectionUtils.isNotEmpty(soB2cRefList)) {
                //合并
                long mergeCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                        && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())
                ).count();
                if (mergeCount > 0) {
                    labelDTO.setRefType(SoB2cOptionTypeEnum.ENUM_MERGE.getCode());
                    labelDTO.setMergeCount(Integer.valueOf(String.valueOf(mergeCount)));
                }
                //拆分
                long splitCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                        && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())
                ).count();
                if (splitCount > 0) {
                    labelDTO.setRefType(SoB2cOptionTypeEnum.ENUM_SPLIT.getCode());

                }
            }
            //主表标签
            if (StringUtils.isNotBlank(label)) {
                SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(label, SoB2cDTO.LabelJsonDTO.class);
                labelDTO.setAliexpressStatus(labelJsonDTO.getAliexpressStatus());
                labelDTO.setAmazonStatus(labelJsonDTO.getAmazonStatus());
                labelDTO.setFulfillmentChannel(labelJsonDTO.getFulfillmentChannel());
                labelDTO.setShipNodeType(labelJsonDTO.getShipNodeType());
                labelDTO.setTikTokStatus(labelJsonDTO.getTikTokStatus());
                labelDTO.setIsRefunded(labelJsonDTO.getIsRefunded());
                labelDTO.setDeliveryType(labelJsonDTO.getDeliveryType());
                labelDTO.setPriorityLevel(labelJsonDTO.getPriorityLevel());
                data.setIsDeliver(labelJsonDTO.getIsDeliver());
            }
            if (isFullyManaged) {
                data.setPlatformOrderStatusName(FullyManagedPlatformStatusEnum.getName(data.getPlatformOrderStatus()));
                //扩展信息
                String extendData = data.getExtendData();
                if (StringUtils.isNotBlank(extendData)) {
                    SoB2cDTO.ExtendDataDTO extendDataDTO = JSONUtil.toBean(extendData, SoB2cDTO.ExtendDataDTO.class);
                    data.setOrderQty(extendDataDTO.getOrderQty());
                    data.setDeliveryQty(extendDataDTO.getDeliveryQty());
                    data.setReceiveQty(extendDataDTO.getReceiveQty());
                    data.setInstockQty(extendDataDTO.getInstockQty());
                    data.setReturnQty(extendDataDTO.getReturnQty());
                }
                //订单来源类型
                data.setOrderSourceTypeName(SoB2cExtendOrderSourceTypeEnum.getName(data.getOrderSourceType()));
                //预警描述
                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(data.getBillStatus()) && Objects.nonNull(data.getDeliveryTime()) && Objects.nonNull(data.getRequiredDeliveryTime())){
                    //已发货
                    LocalDateTime deliveryTime = data.getDeliveryTime();
                    if (deliveryTime.isBefore(data.getRequiredDeliveryTime())){
                        data.setDeliveryWarningDesc("未超时");
                    }else {
                        double hour = LocalDateUtil.calculateHoursWithDecimal(deliveryTime, data.getRequiredDeliveryTime(), 1);
                        data.setWarningHour(BigDecimal.valueOf(hour));
                        data.setDeliveryWarningDesc("已超时" + -hour + "小时");
                    }
                }else if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(data.getBillStatus()) && Objects.nonNull(data.getRequiredDeliveryTime()) && Objects.nonNull(data.getDeliveryWarningTime())){
                    LocalDateTime requiredDeliveryTime = data.getRequiredDeliveryTime();
                    LocalDateTime deliveryWarningTime = data.getDeliveryWarningTime();
                    if (LocalDateTime.now().isBefore(deliveryWarningTime)){
                        data.setDeliveryWarningDesc("暂无预警");
                    }else if (LocalDateTime.now().isBefore(requiredDeliveryTime) && LocalDateTime.now().isAfter(deliveryWarningTime)){
                        double hour = LocalDateUtil.calculateHoursWithDecimal( LocalDateTime.now(), requiredDeliveryTime, 1);
                        data.setWarningHour(BigDecimal.valueOf(hour));
                        data.setDeliveryWarningDesc(hour + "小时后超时");
                    }else if (requiredDeliveryTime.isBefore(LocalDateTime.now())){
                        double hour = LocalDateUtil.calculateHoursWithDecimal(LocalDateTime.now(), requiredDeliveryTime, 1);
                        data.setWarningHour(BigDecimal.valueOf(hour));
                        data.setDeliveryWarningDesc("已超期" + -hour + "小时");
                    }
                }
            }

            //明细信息
            List<SoB2cDetailEntity> detailList = allDetailList.stream().filter(obj -> obj.getMainId().equals(data.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            List<String> warehouseList = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).collect(Collectors.toList());
            long warehouseCount = overseasProviderWarehouseList.stream().filter(o -> warehouseList.contains(o.getWarehouseId())).count();
            Boolean isOverseasProviderWarehouse = warehouseCount > 0;
            data.setIsOverseasProviderWarehouse(isOverseasProviderWarehouse);
            List<SoB2cDetailDTO.ListDTO> soB2cDetailList = BeanMapperUtils.copyList(SoB2cDetailDTO.ListDTO.class, detailList);

            //手动标发标记
            data.setTag(Boolean.FALSE);
            if (shippedMap.containsKey(data.getId())) {

            } else if (manualMap.containsKey(data.getId())) {
                data.setTag(Boolean.TRUE);
            } else {
                data.setTag(data.getIsManualDelivery());
            }

            //获取发货单号
            data.setDeliveryCode(deliveryCodeMap.getOrDefault(data.getId(),""));

            Boolean isCombination = Boolean.FALSE;
            for (SoB2cDetailDTO.ListDTO detailDTO : soB2cDetailList) {
                SkuVO skuVO = skuVOMap.get(detailDTO.getSkuId());

                detailDTO.setProductName(null == skuVO ? "" : skuVO.getSkuName());
                SkuVO.PropertyDTO skuPropertyDTO = null == skuVO ? new SkuVO.PropertyDTO() : null == skuVO.getPropertyDTO() ? new SkuVO.PropertyDTO() : skuVO.getPropertyDTO();
                List<SoB2cDetailDTO.PropertyDTO> propertyDTOList = soB2cDetailService.handlePropertyDTOList(skuPropertyDTO);
                detailDTO.setPropertyDTOList(propertyDTOList);
                //是否是组合SKU
                if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                    long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId()) && bomType.equals(e.getType())).count();
                    if (count > 0) {
                        isCombination = Boolean.TRUE;
                        //设置明细SKU是否组合品
                        detailDTO.setIsCombination(Boolean.TRUE);
                    }
                }

                //设置是否手工添加明细
                Boolean isSelfAdd = Boolean.FALSE;
                if (StringUtils.isNotBlank(detailDTO.getSourcePlatform()) &&
                    SoB2cSourcePlatformEnum.ENUM_SELF_ADD.getCode().equals(detailDTO.getSourcePlatform())) {
                    isSelfAdd = Boolean.TRUE;
                }
                detailDTO.setIsSelfAdd(isSelfAdd);
                //库存SKU
                SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = skuMappingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getProductSkuId(), detailDTO.getSkuId()) && CharSequenceUtil.equals(obj.getWarehouseId(), detailDTO.getWarehouseId())).findFirst().orElse(null);
                String variantProperty = detailDTO.getVariantProperty();
                if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                    detailDTO.setVariantProperty( warehouseListSkuDTO.getVariantProperty());
                }else{
                    detailDTO.setVariantProperty(null == skuVO ? "" : skuVO.getVariantProperty());
                }
                detailDTO.setVariantProperty(detailDTO.getVariantProperty()+variantProperty);
                ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(v -> {
                    return detailDTO.getSourcePlatform().equals(SoB2cSourcePlatformEnum.ENUM_THIRD_PLATFORM.getCode())
                            && v.getPlatformSkuNo().equals(detailDTO.getPlatformSkuNo()) && v.getPlatform().equals(data.getDictPlatform())
                            && v.getPlatformSpuNo().equals(detailDTO.getPlatformSpuNo());
                }).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(listingInfoEntity)) {
                    detailDTO.setImageUrl(listingInfoEntity.getProductImageUrl());
                }
                //扩展信息
                String extendData = detailDTO.getExtendData();
                if (StringUtils.isNotBlank(extendData)) {
                    SoB2cDTO.ExtendDataDTO extendDataDTO = JSONUtil.toBean(extendData, SoB2cDTO.ExtendDataDTO.class);
                    detailDTO.setDeliveryQty(extendDataDTO.getDeliveryQty());
                    detailDTO.setReceiveQty(extendDataDTO.getReceiveQty());
                    detailDTO.setInstockQty(extendDataDTO.getInstockQty());
                    detailDTO.setReturnQty(extendDataDTO.getReturnQty());
                }
                //订单本位币金额
                detailDTO.setSourceAmount(detailDTO.getAmount());
                detailDTO.setSourceCurrency(detailDTO.getCurrency());

                BigDecimal amount = MathUtil.multiplyWithTwo(detailDTO.getSourceAmount(), detailDTO.getExchangeRate());
                detailDTO.setAmount(amount);
                detailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());

                detailDTO.setTaxCost(MathUtil.multiplyWithTwo(detailDTO.getTaxCost(), detailDTO.getQty()));

                //标签处理
                SoB2cDetailDTO.DetailLabelDTO detailLabelDTO = new SoB2cDetailDTO.DetailLabelDTO();
                String detailLabel = detailDTO.getLabelJson();
                if (StringUtils.isNotBlank(detailLabel)) {
                    SoB2cDetailDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(detailLabel, SoB2cDetailDTO.LabelJsonDTO.class);
                    detailLabelDTO.setAlreadyTaxed(labelJsonDTO.getAlreadyTaxed());
                    detailLabelDTO.setLogisticsWarehouseType(labelJsonDTO.getLogisticsWarehouseType());
                    detailLabelDTO.setTagList(labelJsonDTO.getTagList());
                    detailLabelDTO.setIsRefunded(labelJsonDTO.getIsRefunded());
                }
                Integer useableQty = MathUtil.ZERO;
                Integer freezeQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(inventoryList)) {
                    //可用库存
                    useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                            && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                            && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                            .mapToInt(obj -> obj.getInventoryTotal()).sum();
                    //冻结库存
                    freezeQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                            && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                            && InventoryStatusEnum.FROZEN.getCode().equals(obj.getInventoryStatus()))
                            .mapToInt(obj -> obj.getInventoryTotal()).sum();
                }
                detailDTO.setUseableQty(useableQty);
                detailDTO.setFreezeQty(freezeQty);
                //存在仓库则需要判断是否缺货
                if (CharSequenceUtil.isNotBlank(detailDTO.getWarehouseId())) {
                    //缺货订单
                    if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(data.getBillStatus())
                            || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(data.getBillStatus()))) {
                        //实体仓缺货
                        Boolean isOutStock = isOutStock(bomChildrenList, inventoryList, detailDTO, ignoreInventorySkuIds);
                        detailLabelDTO.setIsOutStock(isOutStock);
                    }
                }
                //存在虚拟仓库则判断是否缺货
                if (CharSequenceUtil.isNotBlank(detailDTO.getVirtualWarehouseId())) {
                    String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailDTO.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
                    detailDTO.setVirtualWarehouseName(virtualWarehouseName);
                    //虚拟仓缺货处理
                    isVirtualOutStock(bomChildrenList, virtualInventoryList, detailLabelDTO, detailDTO);
                    //缺货订单
                    if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(data.getBillStatus())
                            || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(data.getBillStatus()))) {
                        detailLabelDTO.setIsOutStock(Boolean.FALSE);
                    }
                }

                detailDTO.setDetailLabelDTO(detailLabelDTO);
                //申报信息
                SoB2cDeclareProductDTO.ViewDTO viewDTO = declareProductList.stream().filter(e -> Objects.nonNull(e)
                        && e.getSkuId().equals(detailDTO.getSkuId())
                        && e.getSoDetailId().equals(detailDTO.getId())).findFirst().orElse(null);
                if (Objects.nonNull(viewDTO)) {
                    detailDTO.setToDeclarePrice(viewDTO.getToDeclarePrice());
                    detailDTO.setToCurrency(viewDTO.getToCurrency());
                    detailDTO.setToCurrencySymbol(viewDTO.getToCurrencySymbol());
                    detailDTO.setDeclareLabel(viewDTO.getDeclareLabel());
                    detailDTO.setDeclareLabelName(viewDTO.getDeclareLabelName());
                }
            }

            SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
            dto.setId(data.getId());
            dto.setIsCny(Boolean.FALSE);
            SoB2cEntity soB2cEntity = allList.stream().filter(obj -> obj.getId().equals(data.getId())).findFirst().orElse(new SoB2cEntity());
            dto.setSoB2cEntity(soB2cEntity);

            //物流信息
            SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceEntityList.stream().filter(obj -> obj.getMainId().equals(data.getId())).findFirst().orElse(new SoB2cFinanceEntity());
            dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
            //物流信息
            SoB2cLogisticsEntity logisticsEntity = logisticsEntityList.stream().filter(obj -> obj.getMainId().equals(data.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
            dto.setSoB2cLogisticsEntity(logisticsEntity);
            dto.setSoB2cDetailList(detailList);
            SoB2cDTO.FinancialInfoDTO financialInfoDTO = getFinancialInfo(dto, Boolean.FALSE);
            data.setTotalProfit(financialInfoDTO.getProfit());
            data.setProfitCurrency(data.getCurrency());
            data.setProfitRate(new BigDecimal(financialInfoDTO.getProfitRate().replace("%", "")));
            //订单发货仓
            data.setFromWarehouseId(CollUtil.isNotEmpty(soB2cDetailList) ? soB2cDetailList.stream().map(SoB2cDetailDTO.ListDTO::getWarehouseId).filter(CharSequenceUtil::isNotBlank).findFirst().orElse(CharSequenceUtil.EMPTY) : CharSequenceUtil.EMPTY);
            data.setFromWarehouseName(CollUtil.isNotEmpty(soB2cDetailList) ? soB2cDetailList.stream().map(SoB2cDetailDTO.ListDTO::getWarehouseName).filter(CharSequenceUtil::isNotBlank).findFirst().orElse(CharSequenceUtil.EMPTY) : CharSequenceUtil.EMPTY);
            data.setDetailList(soB2cDetailList);
            //明细存在一条数据时组合SKU则标识
            labelDTO.setIsCombination(isCombination);
            data.setLabelDTO(labelDTO);

            //物流类型，目前就美客多 虾皮 平台使用
            if (StringUtils.isNotBlank(logisticsEntity.getLogisticType())) {
                data.setLogisticType(logisticsEntity.getLogisticType());
                data.setLogisticTypeName(OrderLogisticTypeEnum.getName(logisticsEntity.getLogisticType()));
            }
            //作废类型
            data.setInvalidTypeName(SoB2cInvalidTypeEnum.getName(data.getInvalidType()));


            LogisticsSupplierDTO.AuthChannelViewDTO authChannelViewDTO = authChannelViewDTOList.stream().filter(obj -> obj.getChannelId().equals(data.getLogisticsChannelId())).findFirst().orElse(null);
            if(Objects.nonNull(authChannelViewDTO) && CollectionUtils.isNotEmpty(data.getDetailList())){
                OverseasProviderWarehouseDTO.ViewDTO viewDTO = overseasProviderWarehouseList.stream().filter(obj -> obj.getWarehouseId().equals(data.getDetailList().get(0).getWarehouseId())).findFirst().orElse(null);
                if(Objects.nonNull(viewDTO)){
                    data.setIsSelectChannel(!viewDTO.getProviderCode().equals(authChannelViewDTO.getLogisticsPlatform()));
                }
            }
        }
    }

    /**
     * @param bomChildrenList
     * @param inventoryList
     * @param detailDTO
     * @return Boolean
     * @description: 判断是否缺货
     * @author Will
     * @date: 2024/4/22 14:46
     */
    private Boolean isOutStock(List<BomChildrenSkuDTO> bomChildrenList, List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList
            , SoB2cDetailDTO.ListDTO detailDTO, List<String> ignoreInventorySkuIds) {
        //判断是否是组合品
        Boolean isCombination = Boolean.FALSE;
        long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(e.getType())).count();
        if (count > 0) {
            isCombination = Boolean.TRUE;
        }

        Boolean isOutStock = Boolean.FALSE;
        //费销售套装bom判断父级SKU是否够使用
        if (!isCombination) {
            return detailDTO.getQty() > detailDTO.getUseableQty() && !ignoreInventorySkuIds.contains(detailDTO.getSkuId());
        }
        //销售套装bom需要判断子件库存是否够使用
        List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId())
                && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            return Boolean.TRUE;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : childList) {
            //可用库存
            Integer childUseableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(childrenSkuDTO.getSkuId())
                    && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                    && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                    .mapToInt(obj -> obj.getInventoryTotal()).sum();
            if ((detailDTO.getQty() * childrenSkuDTO.getQuantity() > childUseableQty) && !ignoreInventorySkuIds.contains(childrenSkuDTO.getSkuId())) {
                isOutStock = Boolean.TRUE;
                break;
            }
        }
        return isOutStock;
    }

    /**
     * 判断虚拟仓是否缺货
     *
     * @param bomChildrenList
     * @param virtualInventoryList
     * @param detailDTO
     * @return Boolean
     * @author will
     * @date 2024/7/22 10:42
     */
    private void isVirtualOutStock(List<BomChildrenSkuDTO> bomChildrenList, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList
            , SoB2cDetailDTO.DetailLabelDTO detailLabelDTO, SoB2cDetailDTO.ListDTO detailDTO) {
        //返回信息
        List<SoB2cDTO.VirtualChildScarceDTO> childScarceList = new ArrayList<>();
        //判断是否是组合品
        Boolean isCombination = Boolean.FALSE;
        long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(e.getType())).count();
        if (count > 0) {
            isCombination = Boolean.TRUE;
        }

        Boolean isVirtualScarce = Boolean.FALSE;
        //费销售套装bom判断父级SKU是否够使用
        if (!isCombination) {
            //虚拟仓是否缺货
            Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), detailDTO.getSkuId())
                    && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), detailDTO.getVirtualWarehouseId())
                    && CharSequenceUtil.equals(obj.getWarehouseId(), detailDTO.getWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .findFirst().orElse(MathUtil.ZERO);
            detailDTO.setVirtualUsableQty(virtualUsableQty);
            detailLabelDTO.setIsVirtualOutStock(detailDTO.getQty() > virtualUsableQty);
            detailDTO.setChildScarceList(childScarceList);
            return;
        }
        //销售套装bom需要判断子件库存是否够使用
        List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId())
                && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            detailLabelDTO.setIsVirtualOutStock(Boolean.TRUE);
            detailDTO.setChildScarceList(childScarceList);
            return;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : childList) {
            SoB2cDTO.VirtualChildScarceDTO scarceDTO = new SoB2cDTO.VirtualChildScarceDTO();
            //虚拟仓是否缺货
            Integer childVirtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), childrenSkuDTO.getSkuId())
                    && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), detailDTO.getVirtualWarehouseId())
                    && CharSequenceUtil.equals(obj.getWarehouseId(), detailDTO.getWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .findFirst().orElse(MathUtil.ZERO);
            if (!isVirtualScarce && (detailDTO.getQty() * childrenSkuDTO.getQuantity() > childVirtualUsableQty)) {
                isVirtualScarce = Boolean.TRUE;
            }
            scarceDTO.setChildUsableQty(childVirtualUsableQty);

            //针对父级可用数量
            double floor = Math.floor((double) childVirtualUsableQty / childrenSkuDTO.getQuantity());
            Integer parentUsableQty = (int) floor;
            scarceDTO.setParentUsableQty(parentUsableQty);

            Integer virtualScarceQty = detailDTO.getQty() * childrenSkuDTO.getQuantity() - childVirtualUsableQty;
            scarceDTO.setVirtualScarceQty(MathUtil.compareTo(virtualScarceQty, MathUtil.ZERO) >= MathUtil.ZERO ? virtualScarceQty : MathUtil.ZERO);
            scarceDTO.setSkuId(childrenSkuDTO.getSkuId());
            scarceDTO.setSkuNo(childrenSkuDTO.getSkuNo());
            scarceDTO.setQuantity(childrenSkuDTO.getQuantity());
            scarceDTO.setBomVersion(childrenSkuDTO.getBomVersion());
            childScarceList.add(scarceDTO);
        }
        detailLabelDTO.setIsVirtualOutStock(isVirtualScarce);
        detailDTO.setChildScarceList(childScarceList);
        if (CollectionUtils.isNotEmpty(childScarceList)) {
            //bom最小可用数
            Integer bomUsableQty = childScarceList.stream().min(Comparator.comparing(SoB2cDTO.VirtualChildScarceDTO::getParentUsableQty)).map(SoB2cDTO.VirtualChildScarceDTO::getParentUsableQty).get();
            detailDTO.setVirtualUsableQty(bomUsableQty);
        }
    }


    /**
     * @param inventoryList
     * @param waitDeliveryQtyList
     * @param ignoreInventorySkuIds
     * @param warehouseId
     * @param qty
     * @param skuMappingDTOList
     * @param warehouseList
     * @param bomChildrenSkuDTOList
     * @return Boolean
     * @description: 明细子件是否缺货
     * @author Will
     * @date: 2024/4/22 15:28
     */
    @Override
    public Boolean isChildOutStock(List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList,
                                   List<SoB2cDetailDTO.WaitDeliveryQtyDTO> waitDeliveryQtyList, List<String> ignoreInventorySkuIds,
                                   SoB2cDetailEntity soB2cDetailEntity, String warehouseId, Integer qty, List<ListingInfoWithSkuMappingDTO> skuMappingDTOList, List<WarehouseDTO.UpdateDTO> warehouseList, List<BomChildrenSkuDTO> bomChildrenSkuDTOList) {
        String skuId = soB2cDetailEntity.getSkuId();
        if (CollectionUtils.isEmpty(inventoryList)) {
            return Boolean.TRUE;
        }
        //通过仓库类型判断校验子件还是父件
        Boolean isCheckParent = true;
        WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(v->v.getId().equals(warehouseId)).findFirst().orElse(null);
        ListingInfoWithSkuMappingDTO skuMappingDTO = skuMappingDTOList.stream().filter(s -> skuId.equals(s.getProductSkuId()) && soB2cDetailEntity.getPlatformSkuNo().equals(s.getPlatformSkuNo()) && (soB2cDetailEntity.getPlatformSpuNo().isEmpty() || soB2cDetailEntity.getPlatformSpuNo().equals(s.getPlatformSpuNo()))).findFirst().orElse(null);

        if(Objects.nonNull(warehouseDTO)&&Objects.nonNull(skuMappingDTO)&&Objects.nonNull(skuMappingDTO.getExtendMap()) && skuMappingDTO.getExtendMap().containsKey(warehouseDTO.getWarehouseManageType())){
            String manageType = warehouseDTO.getWarehouseManageType();
            String deliveryType = skuMappingDTO.getExtendMap().get(manageType);
            if(deliveryType.equals(WarehouseDeliveryTypeEnum.SINGLE.getCode())){
                isCheckParent = false;
            }
        }
        //要校验的子件
        List<BomChildrenSkuDTO> childList = bomChildrenSkuDTOList.stream().filter(e -> e.getParentSkuId().equals(skuId)
                        && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                .collect(Collectors.toList());
        //没有子件，校验父件
        if (CollectionUtils.isEmpty(childList)) {
            isCheckParent = true;
        }
        if(isCheckParent){
            //可用库存
            Integer useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(skuId)
                            && obj.getWarehouseId().equals(warehouseId)
                            && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                    .orElse(MathUtil.ZERO);
            //待发货数量
            Integer waitDeliveryQty = waitDeliveryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId)
                            && CharSequenceUtil.equals(obj.getWarehouseId(), warehouseId))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(MathUtil.ZERO);
            return (qty > (useableQty - waitDeliveryQty)) && !ignoreInventorySkuIds.contains(skuId);
        }else{
            for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                //可用库存
                Integer useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(childrenSkuDTO.getSkuId())
                                && obj.getWarehouseId().equals(warehouseId)
                                && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                        .orElse(MathUtil.ZERO);
                //待发货数量
                Integer waitDeliveryQty = waitDeliveryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId)
                                && CharSequenceUtil.equals(obj.getWarehouseId(), warehouseId))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(MathUtil.ZERO);
                waitDeliveryQty = waitDeliveryQty*childrenSkuDTO.getQuantity();
                int childQty = qty*childrenSkuDTO.getQuantity();
                if((childQty > (useableQty - waitDeliveryQty)) && !ignoreInventorySkuIds.contains(childrenSkuDTO.getSkuId())){
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(SoB2cEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //作废和冻结不支持提交
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus()) || SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_UPDATE_SUBMIT, entity.getCode());
        }
        //未付款数据不支持提交
        soB2cCoreService.checkPayMent(entity);

        if (isFullyManagedOrder(entity.getDictPlatform())){
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(entity.getId());
            //产品单价，非赠品时，校验必填
            for (SoB2cDetailEntity detailEntity : soB2cDetailEntityList){
                if (Objects.equals(detailEntity.getIsGift(), Boolean.FALSE) && (Objects.isNull(detailEntity.getPrice()) || detailEntity.getPrice().compareTo(BigDecimal.ZERO) <= 0)){
                    throw new ServiceException(ApiError.SO_B2C_DETAIL_PRICE_NOT_EXIST, entity.getCode(), detailEntity.getSkuNo());
                }
            }
        }

        return;
    }

    /**
     * 新增修改处理数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void handleData(SoB2cEntity soB2cEntity, Boolean exchangeRateThrow, Boolean checkPayTime) {
        if (ObjectUtils.isEmpty(soB2cEntity)) {
            return;
        }

        if (SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType()) && StringUtils.isBlank(soB2cEntity.getTransactionSubType())) {
            throw new ServiceException(ApiError.TRANSACTION_SUB_TYPE_NOT_NULL, soB2cEntity.getCode());
        }
        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        if (ObjectUtils.isEmpty(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        soB2cEntity.setBillDate(ObjectUtils.isEmpty(soB2cEntity.getBillDate()) ? LocalDate.now() : soB2cEntity.getBillDate());
        soB2cEntity.setCreateTime(ObjectUtils.isEmpty(soB2cEntity.getCreateTime()) ? LocalDateTime.now() : soB2cEntity.getCreateTime());
        LocalDateTime createTime = soB2cEntity.getCreateTime();
        //全托管订单设置币种
        if (isFullyManagedOrder(soB2cEntity.getDictPlatform())){
            if(CharSequenceUtil.isBlank(soB2cEntity.getCurrency())){
                soB2cEntity.setCurrency(shopInfoEntity.getTradeCurrency());
            }
            createTime = soB2cEntity.getPayTime();
        }
        if (StringUtils.isNotBlank(soB2cEntity.getCurrency())) {
            BigDecimal exchangeRate = dmpTaskFeign.getRate(createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
            if (MathUtil.compareTo(exchangeRate, MathUtil.ZERO) == MathUtil.ZERO && exchangeRateThrow) {
                throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
            }
            soB2cEntity.setExchangeRate(null == exchangeRate ? BigDecimal.ZERO : exchangeRate);
        }
        soB2cEntity.setOrgId(shopInfoEntity.getSalesOrgId());
        soB2cEntity.setOrgName(shopInfoEntity.getSalesOrgName());
        // 源单付款状态优先
        if (StringUtils.isBlank(soB2cEntity.getPayStatus())) {
            //付款时间不为空则已付款
            if (ObjectUtils.isNotEmpty(soB2cEntity.getPayTime()) && checkPayTime) {
                soB2cEntity.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            }
        }
    }

    /**
     * @param records
     * @description: 合并列表数据显示处理
     * @author Will
     * @date: 2023/8/23 9:32
     */
    private void fillMergeData(List<SoB2cDTO.MergeListDTO> records, String permissionSql) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //店铺信息
        List<String> shopIdList = records.stream().map(SoB2cDTO.MergeListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        Map<String, ShopInfoEntity> shopMap = shopList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity()));
        //国家信息
        List<String> countryIdList = shopList.stream().map(ShopInfoEntity::getDictCountryCode).collect(Collectors.toList());
        Map<String, String> countryMap = new HashMap<>();
        if (CollUtil.isNotEmpty(countryIdList)){
            List<DictCountryEntity> dictCountryList = sysDictFeign.listCountryByIds(countryIdList);
            if(CollUtil.isNotEmpty(dictCountryList)){
                countryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));
            }
        }

        //平台
        List<String> platformList = records.stream().map(SoB2cDTO.MergeListDTO::getDictPlatform)
                .distinct().collect(Collectors.toList());
        //币别
        List<String> currencyList = records.stream().map(SoB2cDTO.MergeListDTO::getSourceCurrency)
                .distinct().collect(Collectors.toList());
        //买家名称
        List<String> buyerNameList = records.stream().map(SoB2cDTO.MergeListDTO::getBuyerName)
                .distinct().collect(Collectors.toList());
        //平台
        List<String> receiverNameList = records.stream().map(SoB2cDTO.MergeListDTO::getReceiverName)
                .distinct().collect(Collectors.toList());
        //地址1
        List<String> firstAddressList = records.stream().map(SoB2cDTO.MergeListDTO::getFirstAddress).distinct().collect(Collectors.toList());
        //地址2
        List<String> secondAddressList = records.stream().map(SoB2cDTO.MergeListDTO::getSecondAddress).distinct().collect(Collectors.toList());
        //详细地址
        List<String> fullAddressList = records.stream().map(SoB2cDTO.MergeListDTO::getFullAddress).distinct().collect(Collectors.toList());
        //仓库
        List<String> warehouseIdList = records.stream().map(SoB2cDTO.MergeListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //物流方式
        List<String> logisticsChannelIdList = records.stream().map(SoB2cDTO.MergeListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());

        SoB2cDTO.MergeParamDTO mergeParamDTO = new SoB2cDTO.MergeParamDTO();
        mergeParamDTO.setPlatformList(platformList);
        mergeParamDTO.setShopIdList(shopIdList);
        mergeParamDTO.setCurrencyList(currencyList);
        mergeParamDTO.setBuyerNameList(buyerNameList);
        mergeParamDTO.setReceiverNameList(receiverNameList);
        mergeParamDTO.setFirstAddressList(firstAddressList);

        mergeParamDTO.setSecondAddressList(secondAddressList);
        mergeParamDTO.setFullAddressList(fullAddressList);
        mergeParamDTO.setWarehouseIdList(warehouseIdList);
        mergeParamDTO.setLogisticsChannelIdList(logisticsChannelIdList);
        mergeParamDTO.setPermissionSql(permissionSql);
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
        	dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        mergeParamDTO.setDynamicDataSource(dynamicDataSource);
        List<SoB2cDTO.MergeMainDTO> mergeMainList = baseMapper.listMerge(mergeParamDTO);
        if (CollectionUtils.isEmpty(mergeMainList)) {
            return;
        }
        //产品详细
        List<String> skuIdList = mergeMainList.stream().map(SoB2cDTO.MergeMainDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            log.error("未发现产品详细，skuIdList = {}", skuIdList);
            throw new ServiceException(ApiError.ERROR_95084);
        }
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));


        for (SoB2cDTO.MergeListDTO mergeListDTO : records) {

            //店铺信息
            ShopInfoEntity shopInfoEntity = shopMap.getOrDefault(mergeListDTO.getShopId(),null);
            if (ObjectUtils.isEmpty(shopInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92058);
            }
            mergeListDTO.setShopName(shopInfoEntity.getName());
            //国家信息
            mergeListDTO.setCountyName(countryMap.getOrDefault(shopInfoEntity.getDictCountryCode(), CharSequenceUtil.EMPTY));
            //主表数据
            if (CollectionUtils.isNotEmpty(mergeMainList)) {
                List<SoB2cDTO.MergeMainDTO> mainList = mergeMainList.stream().filter(obj -> mergeListDTO.getDictPlatform().equals(obj.getDictPlatform())
                        && mergeListDTO.getShopId().equals(obj.getShopId())
                        && mergeListDTO.getBuyerName().equals(obj.getBuyerName())
                        && mergeListDTO.getSourceCurrency().equals(obj.getSourceCurrency())
                        && mergeListDTO.getReceiverName().equals(obj.getReceiverName())
                        && mergeListDTO.getFirstAddress().equals(obj.getFirstAddress())
                        && mergeListDTO.getSecondAddress().equals(obj.getSecondAddress())
                        && mergeListDTO.getFullAddress().equals(obj.getFullAddress())
                        && mergeListDTO.getWarehouseId().equals(obj.getWarehouseId())
                        && CharSequenceUtil.equals(mergeListDTO.getLogisticsChannelId(), obj.getLogisticsChannelId())
                ).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(mainList) || mainList.size() == 1) {
                    continue;
                }

                List<String> ids = new ArrayList<>();
                for (SoB2cDTO.MergeMainDTO mergeMainDTO : mainList) {
                    if (!ids.contains(mergeMainDTO.getId())) {
                        mergeMainDTO.setIsMain(Boolean.TRUE);
                    }
                    ids.add(mergeMainDTO.getId());
                    //产品名称
                    mergeMainDTO.setProductName(skuMap.getOrDefault(mergeMainDTO.getSkuId(),CharSequenceUtil.EMPTY));
                    //本位币金额
                    mergeMainDTO.setAmount(MathUtil.multiplyWithTwo(mergeMainDTO.getSourceAmount(), mergeMainDTO.getExchangeRate()));
                    mergeMainDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                }
                mergeListDTO.setMainList(mainList);
                //总原币金额
                BigDecimal totalSourceAmount = mainList.stream().map(SoB2cDTO.MergeMainDTO::getSourceAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setSourceAmount(totalSourceAmount);
                //总本位币金额
                BigDecimal totalAmount = mainList.stream().map(obj -> MathUtil.multiplyWithTwo(obj.getSourceAmount(), obj.getExchangeRate()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setAmount(totalAmount);
                mergeListDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                //总重量
                BigDecimal totalWeight = mainList.stream().map(SoB2cDTO.MergeMainDTO::getWeight)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setWeight(totalWeight);
            }
        }
    }

    /**
     * @param list
     * @param soB2cLogisticsList
     * @param soB2cReceiverList
     * @param soB2cDetailList
     * @description: 检验合并数据
     * @author Will
     * @date: 2023/8/23 10:23
     */
    private void checkMergeData(List<SoB2cEntity> list, List<SoB2cLogisticsEntity> soB2cLogisticsList,
                                List<SoB2cReceiverEntity> soB2cReceiverList, List<SoB2cDetailEntity> soB2cDetailList) {
        //销售平台
        long platformCount = list.stream().map(SoB2cEntity::getDictPlatform).distinct().count();
        if (platformCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PLATFORM_CODE_COMPLEX);
        }
        //店铺
        long shopCount = list.stream().map(SoB2cEntity::getShopId).distinct().count();
        if (shopCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_COMPLEX);
        }
        //币别
        long currencyCount = list.stream().map(SoB2cEntity::getCurrency).distinct().count();
        if (currencyCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CURRENCY_COMPLEX);
        }
        //物流方式
        long logisticsMethodCount = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getLogisticsChannelId).distinct().count();
        if (logisticsMethodCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_COMPLEX);
        }

        //买家
        long nameCount = soB2cReceiverList.stream().map(SoB2cReceiverEntity::getName).distinct().count();
        if (nameCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_BUYER_NAME_COMPLEX);
        }
        //收货人
        long receiverNameCount = soB2cReceiverList.stream().map(SoB2cReceiverEntity::getReceiverName).distinct().count();
        if (receiverNameCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NAME_COMPLEX);
        }
        //收货地址
        long addressCount = soB2cReceiverList.stream().map(obj -> StrUtil.join(",", obj.getFirstAddress(), obj.getSecondAddress(), obj.getFullAddress())).distinct().count();
        if (addressCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_ADDRESS_COMPLEX);
        }

        //仓库
        long warehouseCount = soB2cDetailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().count();
        if (warehouseCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_WAREHOUSE_COMPLEX);
        }
    }


    /**
     * @param id
     * @return Boolean
     * @description: 匹配审核规则
     * @author Will
     * @date: 2023/8/24 15:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Boolean> approveRule(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        Map<String, Boolean> resultMap = new HashMap<>();
        SoB2cEntity entity = super.getById(id);
        isExist(entity);
        if (map.isEmpty()) {
            //匹配审核规则
            handleMatchJson(id, detailList, map);
        }
        RuleOrderApprovalDTO.RuleMatchDTO ruleOrderMatchResult = ruleOrderApprovalService.getRuleOrderMatchResult(map);
        //审核规则是否通过
        Boolean approveSuccess = ruleOrderMatchResult.getApproveSuccess();
        Boolean isMatch = Boolean.FALSE;
        Boolean isPass = Boolean.FALSE;
        //匹配审核规则通过,自动提交并审核
        if (Objects.nonNull(approveSuccess) && approveSuccess) {
            isMatch = Boolean.TRUE;
            //更新流转状态和分类信息
            soB2cRefCategoryService.update(ruleOrderMatchResult.getCategoryDetailIdList(), id);
            //自动提交
            SoB2cErrorEntity error = soB2cErrorService.getByMainIdAndType(id, SoB2cErrorTypeEnum.ORDER_FETCH.getCode());
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
            ApproveResultDTO submit = soB2cService.submit(entity, error, soB2cLogisticsEntity, Boolean.FALSE);
            if (!submit.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_1042);
            }
            String approveMsg = "自动审核不通过";
            //审核通过
            if (ApproveType.PASS.equals(ruleOrderMatchResult.getFlowStatus())) {
                isPass = Boolean.TRUE;
                approveMsg = "自动审核通过";
            }
            //自动审核通过
            BatchResultDTO approve = soB2cService.approve(new ApproveOneDTO(id, ruleOrderMatchResult.getFlowStatus(), approveMsg, Boolean.FALSE), isMatch, ruleOrderMatchResult.getRuleName());
            if (!approve.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_94006);
            }
        } else {
            //标识异常并且审核不通过
            updateAbnormalTypeApprove(id,entity.getCode(), ApproveStatusEnum.REJECT, SoB2cAbnormalTypeEnum.ENUM_APPROVE_REJECT);
        }
        resultMap.put("isMatch", isMatch);
        resultMap.put("isPass", isPass);
        return resultMap;
    }

    /**
     * @param id
     * @param detailList
     * @return List<JSONObject>
     * @description: 审核规则匹配字段处理
     * @author Will
     * @date: 2023/11/16 15:27
     */
    @Override
    public Map<String, Object> handleMatchJson(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        SoB2cEntity soB2cEntity = this.getById(id);
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cReceiverEntity receiverEntity = null;
        if (!isFullyManagedOrder(soB2cEntity.getDictPlatform())){
            receiverEntity = soB2cReceiverService.getByMainId(id);
            if (ObjectUtil.isEmpty(receiverEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
            }
        }
        SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceService.getByMainId(id);
        if (ObjectUtil.isEmpty(soB2cFinanceEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }
        List<String> skuIdList = detailList.stream().map(SoB2cDetailEntity::getSkuId).filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        String combination = BomTypeEnum.COMBINATION.getType();
        bomChildrenList = bomChildrenList.stream().filter(b -> combination.equals(b.getType())).collect(Collectors.toList());
        //汇总子sku 和父级sku全量ids
        List<String> childSkuIds = bomChildrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        //汇总skuIds
        if (CollectionUtils.isNotEmpty(childSkuIds) && CollectionUtils.isNotEmpty(skuIdList)){
            skuIdList.addAll(childSkuIds);
        }
        List<ProductDetailDTO.ProductDTO> productList = plmTaskFeign.listProductBySkuIds(skuIdList);
        List<String> warehouseIdList = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> deliveryWarehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        List<String> platformNos = detailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        ListingInfoParamDTO param = new ListingInfoParamDTO();
        param.setPlatform(soB2cEntity.getDictPlatform());
        param.setSkuIdList(skuIdList);
        param.setPlatformSkuNoList(platformNos);
        param.setShopIdList(Arrays.asList(soB2cEntity.getShopId()));
        param.setIsExpire(false);
        List<ListingInfoWithSkuMappingDTO> skuMappingList = skuMappingService.findListDto(param);
        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : com.google.common.collect.Lists.newArrayList();

        /**
         *  已付款且未提交发货且未作废的订单SKU的发货数量
         *  根据SKU、仓库、仓位查询SKU数量
         */
        List<String> detailIdList = detailList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        SoB2cDetailDTO.WaitDeliveryParamDTO paramDTO = new SoB2cDetailDTO.WaitDeliveryParamDTO(skuIdList, warehouseIdList, detailIdList);
        List<SoB2cDetailDTO.WaitDeliveryQtyDTO> waitDeliveryQtyList = soB2cDetailService.listWaitDeliveryQty(paramDTO);

        //含税总成本
        BigDecimal totalTaxCost = detailList.stream().filter(obj -> MathUtil.compareTo(obj.getTaxCost(), MathUtil.ZERO) > MathUtil.ZERO)
                .map(e -> MathUtil.multiplyWithTwo(e.getTaxCost(), e.getQty())).reduce(BigDecimal.ZERO, BigDecimal::add);

        //即时库存数据
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = null;
        if (CollectionUtils.isNotEmpty(warehouseIdList) && CollectionUtils.isNotEmpty(skuIdList)) {
            InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
            skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode()));
            skuInventoryDTO.setWarehouseIdList(warehouseIdList);
            skuInventoryDTO.setSkuIdList(skuIdList);
            inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);
        }

        SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
        dto.setId(soB2cEntity.getId());
        dto.setIsCny(Boolean.TRUE);
        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(logisticsEntity);
        dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
        dto.setSoB2cDetailList(detailList);
        SoB2cDTO.FinancialInfoDTO financialInfo = getFinancialInfo(dto, Boolean.FALSE);
        map.put("id", soB2cEntity.getId());
        map.put("code", soB2cEntity.getCode());
        map.put("dictPayMethod", soB2cEntity.getDictPayMethod());
        Integer goodsTotalQty = detailList.stream().mapToInt(SoB2cDetailEntity::getQty).sum();
        map.put("goodsTotalQty", goodsTotalQty);
        LocalDateTime payTime = soB2cEntity.getPayTime();
        String payTimeStr = Objects.nonNull(payTime) ? LocalDateUtil.formatTime(payTime, DateUtil.fmt) : "";
        map.put("payTime", payTimeStr);

        //产品信息尺寸
        map.put("packageWeight", logisticsEntity.getWeight());
        map.put("packageLength", logisticsEntity.getLength());
        map.put("packageHeight", logisticsEntity.getHeight());
        //产品尺寸(长+宽+高)
        BigDecimal packageSize = logisticsEntity.getLength()
                .add(logisticsEntity.getWeight())
                .add(logisticsEntity.getHeight());
        map.put("packageSize", packageSize);
        //产品尺寸(长+2*宽+2*高)
        BigDecimal packageMultiSize = logisticsEntity.getLength()
                .add(MathUtil.multiplyWithTwo(logisticsEntity.getWeight(), MathUtil.TWO))
                .add(MathUtil.multiplyWithTwo(logisticsEntity.getHeight(), MathUtil.TWO));
        map.put("packageMultiSize", packageMultiSize);

        map.put("shop", soB2cEntity.getShopId());
        map.put("logisticsChannelId", logisticsEntity.getLogisticsChannelId());
        map.put("actualShippingCost", logisticsEntity.getActualShippingCost());
        map.put("estimatedShippingCost", logisticsEntity.getEstimatedShippingCost());
        map.put("dictPlatform", soB2cEntity.getDictPlatform());
        map.put("nfeInvoiceStatus", CharSequenceUtil.isNotBlank(soB2cEntity.getNfeInvoiceStatus()) ? soB2cEntity.getNfeInvoiceStatus() : "");
        //如果是美客多，取订单标签里面的发货类型标识匹配订单规则
        if (PlatformDictEnum.MERCADOLIBRE.getCode().equals(soB2cEntity.getDictPlatform())
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(soB2cEntity.getDictPlatform())) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(soB2cEntity.getLabelJson(), SoB2cDTO.LabelDTO.class);
            map.put("mercadoOrderDeliveryType", labelJsonDTO.getLogisticType());
        }

        //是否买家留言
        Boolean isHavebuyerRemark = !StringUtils.isBlank(soB2cEntity.getBuyerRemark());

        map.put("isHavebuyerRemark", isHavebuyerRemark);
        //主表标签处理
        String mainLabelJson = soB2cEntity.getLabelJson();
        Boolean isAmazonFBA = Boolean.FALSE;
        if (CharSequenceUtil.isNotBlank(mainLabelJson)) {
            SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(mainLabelJson, SoB2cDTO.LabelJsonDTO.class);
            //FBA
            if ("AFN".equals(labelJsonDTO.getFulfillmentChannel())) {
                isAmazonFBA = Boolean.TRUE;
            }
        }
        map.put("isAmazonFBA", isAmazonFBA);
        map.put("packageWidth", logisticsEntity.getWidth());

        //仓库数量
        long warehouseCount = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).filter(StrUtil::isNotBlank).distinct().count();
        map.put("deliveryWarehouseQty", warehouseCount);
        map.put("buyLogisticsChannelId", logisticsEntity.getName());
        map.put("destCountry", Objects.nonNull(receiverEntity) ? receiverEntity.getCountry() : "");
        map.put("destCity", Objects.nonNull(receiverEntity) ? receiverEntity.getCityName() : "");
        map.put("toProvince", Objects.nonNull(receiverEntity) ? receiverEntity.getProvinceName() : "");
        map.put("orderTaxCost", totalTaxCost);
        map.put("amount", MathUtil.multiplyWithTwo(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
        map.put("orderProfitRate", financialInfo.getProfitRateFlag());
        map.put("postCode", Objects.nonNull(receiverEntity) ? receiverEntity.getPostCode() : "");

        List<Map<String, Object>> mapList = new ArrayList<>(detailList.size());
        for (SoB2cDetailEntity detailEntity : detailList) {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("detailId", detailEntity.getId());
            detailMap.put("platformSkuNo", detailEntity.getPlatformSkuNo());
            detailMap.put("platformSpuNo", detailEntity.getPlatformSpuNo());
            detailMap.put("skuQty", detailEntity.getQty());
            detailMap.put("skuId", detailEntity.getSkuId());
            detailMap.put("skuNo", detailEntity.getSkuNo());
            detailMap.put("dictPayMethod", soB2cEntity.getDictPayMethod());
            detailMap.put("goodsTotalQty", goodsTotalQty);
            detailMap.put("payTime", payTimeStr);
            detailMap.put("packageWeight", logisticsEntity.getWeight());
            detailMap.put("packageLength", logisticsEntity.getLength());
            detailMap.put("packageHeight", logisticsEntity.getHeight());
            //产品尺寸(长+宽+高)
            BigDecimal detailPackageSize = logisticsEntity.getLength()
                    .add(logisticsEntity.getWidth())
                    .add(logisticsEntity.getHeight());
            map.put("packageSize", detailPackageSize);
            //产品尺寸(长+2*宽+2*高)
            BigDecimal detailPackageMultiSize = logisticsEntity.getLength()
                    .add(MathUtil.multiplyWithTwo(logisticsEntity.getWidth(), MathUtil.TWO))
                    .add(MathUtil.multiplyWithTwo(logisticsEntity.getHeight(), MathUtil.TWO));
            map.put("packageMultiSize", detailPackageMultiSize);

            //是否缺货
            if (CharSequenceUtil.isNotBlank(detailEntity.getWarehouseId())) {
                Boolean isOutStock = isChildOutStock(inventoryList, waitDeliveryQtyList, ignoreInventorySkuIds,
                        detailEntity, detailEntity.getWarehouseId(), detailEntity.getQty(), skuMappingList, deliveryWarehouseList, bomChildrenList);
                detailMap.put("isOutStock", isOutStock);
            } else {
                detailMap.put("isOutStock", Boolean.TRUE);
            }
            detailMap.put("shop", soB2cEntity.getShopId());
            detailMap.put("logisticsChannelId", logisticsEntity.getLogisticsChannelId());
            detailMap.put("actualShippingCost", logisticsEntity.getActualShippingCost());
            detailMap.put("estimatedShippingCost", logisticsEntity.getEstimatedShippingCost());
            detailMap.put("dictPlatform", soB2cEntity.getDictPlatform());
            detailMap.put("isHavebuyerRemark", isHavebuyerRemark);
            detailMap.put("deliveryWarehouseQty", warehouseCount);
            detailMap.put("sellerLogistics", logisticsEntity.getName());
            detailMap.put("destCountry", Objects.nonNull(receiverEntity) ? receiverEntity.getCountry():"");
            detailMap.put("destCity", Objects.nonNull(receiverEntity) ?receiverEntity.getCityName():"");
            detailMap.put("toProvince", Objects.nonNull(receiverEntity) ?receiverEntity.getProvinceName():"");
            detailMap.put("orderTaxCost", totalTaxCost);
            detailMap.put("amount", MathUtil.multiplyWithTwo(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            detailMap.put("orderProfitRate", financialInfo.getProfitRateFlag());
            detailMap.put("isAmazonFBA", isAmazonFBA);
            detailMap.put("packageWidth", logisticsEntity.getWidth());
            detailMap.put("buyLogisticsChannelId", logisticsEntity.getName());
            detailMap.put("postCode", Objects.nonNull(receiverEntity) ?receiverEntity.getPostCode():"");

            //如果是美客多，取订单标签里面的发货类型标识匹配订单规则
            if (PlatformDictEnum.MERCADOLIBRE.getCode().equals(soB2cEntity.getDictPlatform())
                    || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(soB2cEntity.getDictPlatform())) {
                SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(soB2cEntity.getLabelJson(), SoB2cDTO.LabelDTO.class);
                detailMap.put("mercadoOrderDeliveryType", labelJsonDTO.getLogisticType());
            }

            //明细标签处理
            String detailLabelJson = detailEntity.getLabelJson();
            if (CharSequenceUtil.isNotBlank(detailLabelJson)) {
                SoB2cDetailDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(detailLabelJson, SoB2cDetailDTO.LabelJsonDTO.class);
                //速卖通已税
                if ("U_TAXED".equals(labelJsonDTO.getAlreadyTaxed()) || "I_TAXED".equals(labelJsonDTO.getAlreadyTaxed())) {
                    detailMap.put("isAliExpressTaxOrder", Boolean.TRUE);
                }
                //菜鸟官方仓
                if ("cainiaoInternationalWarehouse".equals(labelJsonDTO.getLogisticsWarehouseType())) {
                    detailMap.put("isAliExpressNewbieWarehouse", Boolean.TRUE);
                }
            }
            //是否是组合SKU
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailEntity.getSkuId())).count();
                if (count > 0) {
                    detailMap.put("isCombinationOrder", Boolean.TRUE);
                }
            }
            ProductDetailDTO.ProductDTO productDTO = productList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDTO)) {
                detailMap.put("category", productDTO.getCategoryId());
                detailMap.put("propertyId", productDTO.getLogisticsPropertyId());
            }
            detailMap.put("deliveryWarehouseId", detailEntity.getWarehouseId());
            detailMap.put("deliveryWarehouseLocation", detailEntity.getWarehouseLocation());
            mapList.add(detailMap);
        }
        map.put("detailList", mapList);
        Object skuNo = getByField("skuNo", mapList);
        map.put("skuNo", skuNo);

        Object sellerSkuNo = getByField("sellerSkuNo", mapList);
        map.put("sellerSkuNo", sellerSkuNo);

        Object platformSkuNo = getByField("platformSkuNo", mapList);
        map.put("platformSkuNo", platformSkuNo);

        Object platformSpuNo = getByField("platformSpuNo", mapList);
        map.put("platformSpuNo", platformSpuNo);

        Object skuQty = getByField("skuQty", mapList);
        map.put("skuQty", skuQty);

        Object deliveryWarehouseId = getByField("deliveryWarehouseId", mapList);
        map.put("deliveryWarehouseId", deliveryWarehouseId);

        Object deliveryWarehouseLocation = getByField("deliveryWarehouseLocation", mapList);
        map.put("deliveryWarehouseLocation", deliveryWarehouseLocation);

        Object category = getByField("category", mapList);
        map.put("category", category);

        Object property = getByField("propertyId", mapList);
        map.put("propertyId", property);

        long isAliExpressTaxOrder = mapList.stream().filter(m -> m != null && m.get("isAliExpressTaxOrder") != null
                && (boolean) m.get("isAliExpressTaxOrder")).count();
        map.put("isAliExpressTaxOrder", isAliExpressTaxOrder > 0);

        long isAliExpressNewbieWarehouse = mapList.stream().filter(m -> m != null && m.get("isAliExpressNewbieWarehouse") != null
                && (boolean) m.get("isAliExpressNewbieWarehouse")).count();
        map.put("isAliExpressNewbieWarehouse", isAliExpressNewbieWarehouse > 0);

        long isCombinationOrder = mapList.stream().filter(m -> m != null && m.get("isCombinationOrder") != null
                && (boolean) m.get("isCombinationOrder")).count();
        map.put("isCombinationOrder", isCombinationOrder > 0);

        long isOutStockCount = mapList.stream().filter(m -> m != null && m.get("isOutStock") != null
                && (boolean) m.get("isOutStock")).count();
        map.put("isOutStock", isOutStockCount > 0);


        return map;
    }

    /**
     * 添加异常标示
     *
     * @param id
     * @param sign
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSignError(String id, String sign) {
        this.lambdaUpdate().
                set(SoB2cEntity::getSignOrderError, sign).
                eq(SoB2cEntity::getId, id).update(new SoB2cEntity());
    }

    /**
     * 清空异常标示
     *
     * @param id
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-20 15:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSignError(String id, String sign) {
        SoB2cEntity soB2cEntity = this.getById(id);
        if (null == soB2cEntity) {
            return;
        }
        String signOrderError = soB2cEntity.getSignOrderError();
        if (!signOrderError.equals(sign)) {
            return;
        }
        // 查询其他历史异常(目前仅支持标记和生成销售出库单)
        SoB2cErrorEntity historyError = soB2cErrorService.lambdaQuery()
                .eq(SoB2cErrorEntity::getMainId, id)
                .in(SoB2cErrorEntity::getType, Arrays.asList(
                        SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode(),
                        SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode(),
                        SoB2cErrorTypeEnum.VIRTUAL_FREEZE_QTY.getCode(),
                        SoB2cErrorTypeEnum.GENERATE_TRANSFER_INFO.getCode()
                ))
                .last(SqlConstants.LIMIT_1)
                .one();
        String newSignOrderError = null == historyError ? "" : historyError.getType();
        this.lambdaUpdate()
                .set(SoB2cEntity::getSignOrderError, newSignOrderError)
                .eq(SoB2cEntity::getId, id)
                .update();
//        this.lambdaUpdate().set(SoB2cEntity::getSignOrderError, "")
//                .eq(SoB2cEntity::getId, id).eq(SoB2cEntity::getSignOrderError, sign).update();
//        SoB2cEntity soB2cEntity = this.getById(id);
//        if (Objects.nonNull(soB2cEntity)) {
//            String signOrderError = soB2cEntity.getSignOrderError();
//            if (signOrderError.equals(sign)) {
//                this.lambdaUpdate().set(SoB2cEntity::getSignOrderError, "").
//                        eq(SoB2cEntity::getId, id).update(new SoB2cEntity());
//            }
//        }
    }


    /**
     * 获取标记发货参数
     *
     * @param soB2cId
     * @return
     */
    @Override
    public SoB2cDTO.SignShipOrderDTO getSignShipParam(String soB2cId) {
        SoB2cDTO.SignShipOrderDTO result = baseMapper.getSignShipParam(soB2cId);
        return result;
    }

    @Override
    public Boolean checkPlatformShipOrder(String soB2cId) {
        SoB2cEntity soB2cEntity = this.getById(soB2cId);
        if (Objects.isNull(soB2cEntity)) {
            ServiceException.runError("未找到B2C销售订单:id={}" + soB2cId);
        }
        //明细如果有非手工单则可以平台标发
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        // 明细存在来源明细ID 并来源属于平台(包括订单拆分和捆绑拆分)
        return soB2cDetailEntityList
                .stream()
                .anyMatch(v -> StringUtils.isNotBlank(v.getSourceDetailId()) && SoB2cSourcePlatformEnum.ENUM_THIRD_PLATFORM.getCode().equalsIgnoreCase(v.getSourcePlatform()));
    }

    @Override
    public BatchResultDTO falseDelivery(String id) {
        SoB2cEntity entity = this.getById(id);
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
        boolean hasLogisticsNO = hasLogisticsNO(entity, logisticsEntity);
        if (!(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(entity.getBillStatus())
                && ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus())) && !hasLogisticsNO) {
            if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(entity.getBillStatus()) || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
                throw new ServiceException(ApiError.DISTRIBUTION_IS_FALSE_DELIVERY);
            } else {
                throw new ServiceException(ApiError.APPROVE_IS_FALSE_DELIVERY);
            }
        }
        List<SoB2cDeliveryEntity> deliveryEntityList = soB2cDeliveryFeign.listBySourceId(Arrays.asList(id));

        String code = logisticsEntity.getCode();
        if (StringUtils.isBlank(code)) {
            throw new ServiceException(ApiError.LOGISTICS_NOT_SUBMIT_NOT_FALSE_DELIVERY);
        }

        for (SoB2cDeliveryEntity deliveryEntity : deliveryEntityList) {
            //手动标发，已发货 的数据不允许操作手动标发
            if (SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(deliveryEntity.getStatus())) {
                throw new ServiceException(ApiError.SO_B2C_DELIVERY_STATUS_NOT_FALSE_DELIVERY, deliveryEntity.getCode());
            }
        }

        String msg = CharSequenceUtil.format("操作单据【{}】手动标发", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), id, "手动标发");
        List<String> deliveryIds = deliveryEntityList.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        //调用第三方平台SDK发货(独立事务)
        try {
            if (this.checkPlatformShipOrder(id)) {
                if (hasLogisticsNO) {
                    soB2cDeliveryFeign.falseDeliveryBySoId(entity.getId());
                } else {
                    soB2cDeliveryFeign.falseDeliveryBatch(deliveryIds);
                }
            }
        } catch (Exception e) {
            log.error("OMS 销售单【{}】 标记发货失败 >>>错误信息{}", entity.getCode(), ExceptionUtil.stacktraceToString(e));
            // 独立异常
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(entity.getId(), SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode(), "", e.getMessage(), ExceptionUtil.stacktraceToString(e), "");
            soB2cErrorService.add(addError);

        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动标发");
    }

    /**
     * B2C销售订单允许只要渠道配置成功、物流单号获取成功后，如果是配货中状态，允许提交手动标发
     *
     * @param entity
     * @param logisticsEntity
     * @return
     */
    private boolean hasLogisticsNO(SoB2cEntity entity, SoB2cLogisticsEntity logisticsEntity) {
        if (Objects.nonNull(logisticsEntity) && StrUtil.isNotBlank(logisticsEntity.getLogisticsChannelId())
                && StrUtil.isNotBlank(logisticsEntity.getCode()) && SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    /**
     * 根据 字段获取值
     *
     * @param fieldCode
     * @return jsonObjectList
     * @author yl
     * @date 2023-12-05 10:25
     */

    private Object getByField(String fieldCode, List<Map<String, Object>> mapList) {
        Set<Object> set = new HashSet<>(mapList.size());
        for (Map<String, Object> map : mapList) {
            Object obj = map.getOrDefault(fieldCode, "");
            if (Objects.nonNull(obj)) {
                set.add(obj);
            }
        }
        if (CollectionUtils.isEmpty(set)) {
            return null;
        } else if (set.size() == 1) {
            return set.stream().findFirst().get();
        } else {
            StringBuilder sb = new StringBuilder();
            for (Object obj : set) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(obj.toString());
            }
            return sb.toString();
        }

    }

    /**
     * @param id                         订单id
     * @param map                        校验的map
     * @param isCheckProductRegistration
     * @return
     * @description 物流规则
     * @author Lambda
     * @create 2023-12-14 15:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public SoB2cDTO.RuleResultDTO logisticsRule(String id, Map<String, Object> map, Boolean isCheckProductRegistration) {
        SoB2cEntity entity = super.getById(id);
        isExist(entity);
        if (map.isEmpty()) {
            List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
            handleMatchJson(id, detailList, map);
        }
        //规则结果
        RuleLogisticsDTO.RuleMatchResultDTO matchResult = ruleLogisticsService.getRuleOrderMatchResult(map);
        Boolean result = Objects.nonNull(matchResult);
        SoB2cDTO.RuleResultDTO resultDTO = new SoB2cDTO.RuleResultDTO();
        Boolean autoGetTrackNo = Boolean.FALSE;
        Boolean autoGetTrackNotOfRangeDelivery = Boolean.FALSE;
        //表示通过
        if (result) {
            //物流商id
            String logisticsChannelId = matchResult.getLogisticsChannelId();
            String logisticsChannelName = matchResult.getLogisticsChannelName();
            resultDTO.setName(matchResult.getName());
            autoGetTrackNo = matchResult.getAutoGetTrackNo();
            autoGetTrackNotOfRangeDelivery = matchResult.getAutoGetTrackNotOfRangeDelivery();
            if (StringUtils.isNotBlank(logisticsChannelId)) {
                SoB2cLogisticsEntity b2cLogistics = soB2cLogisticsService.getByMainId(id);
                if (Objects.nonNull(b2cLogistics)) {
                    //已存在的的渠道
                    String dbLogisticsChannelId = b2cLogistics.getLogisticsChannelId();
                    if (StringUtils.isBlank(dbLogisticsChannelId)) {
                        b2cLogistics.setLogisticsChannelId(logisticsChannelId);
                        b2cLogistics.setLogisticsChannelName(logisticsChannelName);
                        soB2cLogisticsService.updateById(b2cLogistics);
                    }
                }
                //根据渠道和(国家+邮编）判断订单是否超范围配送
                Boolean isOutOfRangeDelivery = estimateIsOutOfRangeDelivery(entity.getId(), logisticsChannelId);
                entity.setIsOutOfRangeDelivery(isOutOfRangeDelivery);
            }
            if (StringUtils.isNotBlank(matchResult.getName())) {
                String msg = CharSequenceUtil.format("自动匹配物流规则成功，规则名称：{}", matchResult.getName());
                log.info("{}{}", entity.getCode(), msg);
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), id, "配货操作");
            }
            if (isCheckProductRegistration) {
                soB2cService.checkProductRegistrationAndUpdate(entity.getId(), "");
            }
            //状态更新为配货中
            updateBillStatusAndMatchLogistics(id, SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION, Boolean.TRUE);
        } else {
            updateLogisticsAbnormalType(id, SoB2cAbnormalTypeEnum.ENUM_DISTRIBUTION_REJECT);
        }

        resultDTO.setIsRuleMatch(result);
        resultDTO.setAutoGetTrackNo(autoGetTrackNo);
        resultDTO.setAutoGetTrackNotOfRangeDelivery(autoGetTrackNotOfRangeDelivery);
        return resultDTO;
    }

    private Boolean estimateIsOutOfRangeDelivery(String soB2cId, String logisticsChannelId) {
        //根据渠道和(国家+邮编）判断订单是否超范围配送
        SoB2cReceiverEntity receiver = soB2cReceiverService.getByMainId(soB2cId);
        Boolean isOutOfRangeDelivery = Boolean.FALSE;
        if(null != receiver){
            isOutOfRangeDelivery =  logisticsFeign.estimateIsOutOfRangeDelivery(logisticsChannelId,receiver.getCountry(),receiver.getPostCode());
        }
        lambdaUpdate().set(SoB2cEntity::getIsOutOfRangeDelivery,isOutOfRangeDelivery).eq(SoB2cEntity::getId, soB2cId).update();
        return isOutOfRangeDelivery;
    }

    private Boolean estimateIsOutOfRangeDelivery(String soB2cId , String country, String postCode ,String logisticsChannelId) {
        Boolean isOutOfRangeDelivery = Boolean.FALSE;
        //根据渠道和(国家+邮编）判断订单是否超范围配送
        if(StringUtils.isNotBlank(country) && StringUtils.isNotBlank(postCode) && StringUtils.isNotBlank(logisticsChannelId)){
            isOutOfRangeDelivery =  logisticsFeign.estimateIsOutOfRangeDelivery(logisticsChannelId,country,postCode);
        }
        lambdaUpdate().set(SoB2cEntity::getIsOutOfRangeDelivery,isOutOfRangeDelivery).eq(SoB2cEntity::getId, soB2cId).update();
        return isOutOfRangeDelivery;
    }

    private static void isExist(SoB2cEntity entity) {
        if (null == entity) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表");
        }
    }

    /**
     * 更新订单状态和配货单状态
     *
     * @param id
     * @param soB2cBillStatusEnum
     * @param isMatchLogisticsRule
     */
    public Boolean updateBillStatusAndMatchLogistics(String id, SoB2cBillStatusEnum soB2cBillStatusEnum, Boolean isMatchLogisticsRule) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getBillStatus, soB2cBillStatusEnum.getCode())
                .set(SoB2cEntity::getIsMatchLogisticsRule, isMatchLogisticsRule)
                .update(new SoB2cEntity());
    }

    @Override
    public SoB2cDTO.CustomerDTO getB2cCustomerById(String soId) {
        SoB2cEntity soB2cEntity = this.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        SoB2cDTO.CustomerDTO b2cCustomer = new SoB2cDTO.CustomerDTO();
        b2cCustomer.setSalesOrgId(soB2cEntity.getOrgId());
        b2cCustomer.setSalesOrgName(soB2cEntity.getOrgName());
        b2cCustomer.setDictPlatform(soB2cEntity.getDictPlatform());
        b2cCustomer.setPayTime(soB2cEntity.getPayTime());
        b2cCustomer.setHasPlatformWarehouseOrder(soB2cEntity.hasPlatformWarehouseOrder());
        //店铺
        String shopId = soB2cEntity.getShopId();
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        SoB2cReceiverEntity receiver = soB2cReceiverService.getByMainId(soId);
        b2cCustomer.setShopId(shopId);
        if (Objects.nonNull(shopInfo) && CharSequenceUtil.isNotBlank(shopInfo.getCustomerId())) {
            CustomerInfoEntity customer = customerInfoService.getCustomerById(shopInfo.getCustomerId());
            b2cCustomer.setSellerId(customer.getSellerId());
            b2cCustomer.setSellerName(customer.getSellerName());
            b2cCustomer.setSalesDeptId(customer.getSalesDeptId());
            b2cCustomer.setCustomerName(customer.getName());
            b2cCustomer.setShopName(shopInfo.getName());
        }
        String country = "";
        String countryName = "";
        if (Objects.nonNull(receiver)) {
            b2cCustomer.setReceiverAddress(receiver.getFullAddress());
            b2cCustomer.setReceiverName(receiver.getReceiverName());
            b2cCustomer.setTelNumber(receiver.getTelNumber());
            country = receiver.getCountry();
            countryName = receiver.getCountryName();
        }
        String deliveryMode = DeliveryModeEnum.DELIVERGOODS.getCode();
        String deliveryModeName = DeliveryModeEnum.DELIVERGOODS.getName();
        b2cCustomer.setDeliveryMode(deliveryMode);
        b2cCustomer.setDeliveryModeName(deliveryModeName);
        if (StringUtils.isBlank(countryName) && StringUtils.isNotBlank(country)) {
            DictCountryEntity countryEntity = sysUserFeign.getCountryById(country);
            if (Objects.nonNull(countryEntity)) {
                countryName = countryEntity.getNameCn();
            }
        }
        b2cCustomer.setCountry(country);
        b2cCustomer.setCountryName(countryName);
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soId);
        if (Objects.nonNull(soB2cLogisticsEntity)) {
            b2cCustomer.setTransportNo(soB2cLogisticsEntity.getCode());
            b2cCustomer.setTrackNo(soB2cLogisticsEntity.getTrackNo());
            b2cCustomer.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
            b2cCustomer.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        }
        return b2cCustomer;
    }

    @Override
    public List<SoB2cDTO.CustomerDTO> listCustomer(List<String> soIdList) {
        if (CollectionUtils.isEmpty(soIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cDTO.CustomerDTO> customerList = baseMapper.listCustomer(soIdList);
        return customerList;

    }

    @Override
    public List<WalmartShipDTO> getWalmartShipOrderParam(String soId) {
        List<WalmartShipDTO> resultList = new ArrayList<>();
        List<String> soIdList = new ArrayList<>();
        //如果是合并的单，要拆开发货
        List<SoB2cRefEntity> soB2cRefEntities = soB2cRefService.listByTargetId(soId, SoB2cOptionTypeEnum.ENUM_MERGE);
        if (CollectionUtils.isNotEmpty(soB2cRefEntities)) {
            List<String> soIds = soB2cRefEntities.stream().map(req -> req.getSourceId()).collect(Collectors.toList());
            soIdList.addAll(soIds);
        } else {
            soIdList.add(soId);
        }

        //检查销售订单是否存在
        List<SoB2cEntity> soB2cEntities = this.listByIds(soIdList);
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //检查销售订单物流信息是否存在
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cLogisticsService.listByMainIds(soIdList);
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }

        //检查销售订单详情是否存在
        List<SoB2cDetailEntity> soB2cDetailEntities = soB2cDetailService.listByMainIds(soIdList);
        if (CollectionUtils.isEmpty(soB2cDetailEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            //映射主表字段
            SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsEntities.stream().filter(req -> soB2cEntity.getId().equals(req.getMainId())).findFirst().orElse(new SoB2cLogisticsEntity());
            WalmartShipDTO walmartShipDTO = WalmartShipOrderConverter.INSTANCE.soB2cEntityToWalmartShipDTO(soB2cEntity, logisticsEntity);

            //物流商编号
            LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthByChannelId(logisticsEntity.getLogisticsChannelId());
            LogisticsPlatformEnum platformEnum = LogisticsPlatformEnum.getByCode(authDTO.getLogisticsPlatform());
            authDTO.setLogisticsPlatform(platformEnum.getCode());

            //映射详情字段
            List<SoB2cDetailEntity> detailList = soB2cDetailEntities.stream()
                    .filter(req -> soB2cEntity.getId().equals(req.getMainId())
                            && SoB2cSourcePlatformEnum.ENUM_THIRD_PLATFORM.getCode().equalsIgnoreCase(req.getSourcePlatform())
                    ).collect(Collectors.toList());
            List<WalmartShipOrderDetailDTO> walmartShipOrderDetailDTOS = WalmartShipOrderConverter.INSTANCE.soB2cDetailEntityToWalmartShipOrderDetail(detailList);
            walmartShipDTO.setDetailList(walmartShipOrderDetailDTOS);

            resultList.add(walmartShipDTO);
        }
        return resultList;
    }

    @Override
    public List<SoB2cEntity> listWarehouseIsEmpty(List<String> soIdList) {
        if (CollectionUtils.isEmpty(soIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listWarehouseIsEmpty(soIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWarehouseByShopId(String id, String shopId) {
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.nonNull(shopInfo)) {
            String warehouseId = shopInfo.getWarehouseId();
            if (StringUtils.isNotBlank(warehouseId)) {
                return soB2cDetailService.updateWarehouseIdByMainId(id, warehouseId, Boolean.TRUE);
            }
        }
        return Boolean.FALSE;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean updatePackageStatus(UpdateStateDTO.UpdateByStrStatusDTO dto) {
        List<String> ids = dto.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            //组包状态
            String status = dto.getStatus();
            return this.lambdaUpdate().set(SoB2cEntity::getPackageStatus, status).
                    in(SoB2cEntity::getId, ids).update(new SoB2cEntity());
        }
        return false;
    }


    @Override
    public Boolean updateTransferStatus(UpdateStateDTO.UpdateByStrStatusDTO dto) {
        //组包状态
        String status = dto.getStatus();
        List<String> ids = dto.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            return this.lambdaUpdate().set(SoB2cEntity::getTransferStatus, status).
                    in(SoB2cEntity::getId, ids).update(new SoB2cEntity());
        }
        return false;
    }


    /**
     * 预报统计
     *
     * @param dto
     * @return
     */
    @Override
    public List<SoB2cDTO.ForecastCountDTO> forecastCount(PermissionsDTO dto) {
        List<SoB2cDTO.ForecastCountDTO> list = new ArrayList<>(2);
        String sql = dto.getPermissionSql();
        if (StringUtils.isBlank(sql)) {
            sql = "";
        }
        SoB2cDTO.ForecastCountDTO packageCountDTO = new SoB2cDTO.ForecastCountDTO();
        //待组包
        String waitPackageStatus = PackageStatusEnum.WAIT.getCode();
        String waitPackageStatusName = PackageStatusEnum.WAIT.getName();
        int waitPackageCount = this.lambdaQuery().eq(SoB2cEntity::getPackageStatus, waitPackageStatus).
                eq(SoB2cEntity::getInvalidStatus, Boolean.FALSE).
                last(sql).count();

        packageCountDTO.setCount(waitPackageCount);
        packageCountDTO.setStatus(waitPackageStatus);
        packageCountDTO.setStatusName(waitPackageStatusName);
        list.add(packageCountDTO);


        SoB2cDTO.ForecastCountDTO transferCountDTO = new SoB2cDTO.ForecastCountDTO();
        //待中转
        String waitTransferStatus = TransferStatusEnum.WAIT.getCode();
        String waitTransferStatusName = TransferStatusEnum.WAIT.getName();
        int waitTransferCount = this.lambdaQuery().eq(SoB2cEntity::getTransferStatus, waitTransferStatus).
                eq(SoB2cEntity::getInvalidStatus, Boolean.FALSE).
                last(sql).count();
        transferCountDTO.setCount(waitTransferCount);
        transferCountDTO.setStatus(waitTransferStatus);
        transferCountDTO.setStatusName(waitTransferStatusName);
        list.add(transferCountDTO);
        return list;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> transferDeclare(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //B2C销售订单主表信息
        List<SoB2cEntity> soB2cList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soB2cList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //先校验基础字段
        List<SoB2cLogisticsEntity> logisticsList = soB2cLogisticsService.listByMainIds(ids);
        List<String> channelIds = logisticsList.stream().map(SoB2cLogisticsEntity::getLogisticsChannelId).
                filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(channelIds)){
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        List<LogisticsChannelDTO.BaseDTO> channelList = logisticsFeign.listChannelInfoById(channelIds);
        List<String> notChannelInfoList = soB2cList.stream().filter(v -> {
            SoB2cLogisticsEntity logisticsEntity = logisticsList.stream().filter(t -> t.getMainId().equals(v.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
            LogisticsChannelDTO.BaseDTO channelBase = channelList.stream().filter(t -> t.getId().equals(logisticsEntity.getLogisticsChannelId())).findFirst().orElse(new LogisticsChannelDTO.BaseDTO());
            return StringUtils.isBlank(logisticsEntity.getLogisticsChannelId()) || StringUtils.isBlank(logisticsEntity.getTransferLogisticsChannelId()) || StringUtils.isBlank(logisticsEntity.getTransferLogisticsSupplierId()) || StringUtils.isBlank(channelBase.getMainId());
        }).map(SoB2cEntity::getCode).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notChannelInfoList)) {
            throw new ServiceException(CharSequenceUtil.format("销售单【{}】没有物流或中转信息", notChannelInfoList));
        }

        //相同物流商，中转商，中转渠道 合并成一条主记录
        List<SoB2cDTO.MergeTransferDTO> mergeTransferDTOList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cList) {
            //上面校验了，这里物流和中转信息不会为空
            SoB2cLogisticsEntity logisticsEntity = logisticsList.stream().filter(t -> t.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
            LogisticsChannelDTO.BaseDTO channelBase = channelList.stream().filter(t -> t.getId().equals(logisticsEntity.getLogisticsChannelId())).findFirst().orElse(new LogisticsChannelDTO.BaseDTO());
            SoB2cDTO.MergeTransferDTO mergeTransferDTO = mergeTransferDTOList.stream().filter(v -> v.getLogisticSupplierId().equals(channelBase.getMainId())
                    && v.getTransferLogisticsSupplierId().equals(logisticsEntity.getTransferLogisticsSupplierId())
                    && v.getTransferChannelId().equals(logisticsEntity.getTransferLogisticsChannelId())).findFirst().orElse(null);
            if (Objects.isNull(mergeTransferDTO)) {
                mergeTransferDTO = new SoB2cDTO.MergeTransferDTO();
                mergeTransferDTO.setTransferChannelId(logisticsEntity.getTransferLogisticsChannelId());
                mergeTransferDTO.setTransferLogisticsSupplierId(logisticsEntity.getTransferLogisticsSupplierId());
                mergeTransferDTO.setLogisticSupplierId(channelBase.getMainId());
                mergeTransferDTO.setLogisticSupplierName(channelBase.getLogisticsSupplierName());
                List<SoB2cEntity> soB2cEntityList = new ArrayList<>();
                soB2cEntityList.add(soB2cEntity);
                mergeTransferDTO.setSoB2cEntityList(soB2cEntityList);
                mergeTransferDTOList.add(mergeTransferDTO);
            } else {
                mergeTransferDTO.getSoB2cEntityList().add(soB2cEntity);
            }
        }

        //已中转的订单
        List<String> soCodeList = soB2cList.stream().map(SoB2cEntity::getCode).collect(Collectors.toList());
        List<TransferDeclareDetailEntity> transferDeclareDetailEntityList = transferDeclareFeign.listBySoCodeList(soCodeList);
        List<String> existCodeList = transferDeclareDetailEntityList.stream().map(TransferDeclareDetailEntity::getSoCode).distinct().collect(Collectors.toList());

        //校验合并后记的记录
        LocalTime now = LocalTime.now();
        List<TransferDeclareDTO.AddDTO> transferDeclareList = new ArrayList<>();
        for (SoB2cDTO.MergeTransferDTO mergeTransferDTO : mergeTransferDTOList) {
            List<SoB2cEntity> soB2cEntityList = mergeTransferDTO.getSoB2cEntityList();
            String alreadyTransfer = TransferStatusEnum.SUCCESS.getCode();
            List<String> alreadyTransferCode = soB2cEntityList.stream().filter(s -> !alreadyTransfer.equals(s.getTransferStatus())).map(SoB2cEntity::getCode).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(alreadyTransferCode)) {
                resultDTOList.add(BatchResultDTO.fail(mergeTransferDTO.getLogisticSupplierId(), mergeTransferDTO.getLogisticSupplierName(), CharSequenceUtil.format("销售单【{}】 不属于预报成功的订单,不能进行入库预报", alreadyTransferCode)));
                continue;
            }
            List<String> alreadyDeclareCode = soB2cEntityList.stream().map(SoB2cEntity::getCode).filter(existCodeList::contains).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(alreadyDeclareCode)) {
                resultDTOList.add(BatchResultDTO.fail(mergeTransferDTO.getLogisticSupplierId(), mergeTransferDTO.getLogisticSupplierName(), CharSequenceUtil.format("销售单【{}】 已进行入库预报，不能重复预报", existCodeList)));
                continue;
            }
            String notPackage = PackageStatusEnum.NOT.getCode();
            //需要组包
            List<String> wantPackageCode = soB2cEntityList.stream().filter(s -> !notPackage.equals(s.getPackageStatus())).map(SoB2cEntity::getCode).collect(Collectors.toList());
            //表示强制组包了 就要去组包预报 去中转了
            if (CollectionUtils.isNotEmpty(wantPackageCode)) {
                resultDTOList.add(BatchResultDTO.fail(mergeTransferDTO.getLogisticSupplierId(), mergeTransferDTO.getLogisticSupplierName(), CharSequenceUtil.format("销售订单【{}】关联强制组包，请在组包预报页面操作中转报关", wantPackageCode)));
                continue;
            }
            String unit = UnitEnum.WeightUnitEnum.G.getCode();
            TransferDeclareDTO.AddDTO addDTO = new TransferDeclareDTO.AddDTO();
            addDTO.setDeliveryLogisticsSupplierId(mergeTransferDTO.getLogisticSupplierId());
            addDTO.setTransferLogisticsSupplierId(mergeTransferDTO.getTransferLogisticsSupplierId());
            addDTO.setTransferChannelId(mergeTransferDTO.getTransferChannelId());
            addDTO.setGenerateTime(now);
            //添加的详情
            List<TransferDeclareDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (SoB2cEntity soB2cEntity : soB2cEntityList) {
                SoB2cLogisticsEntity logisticsEntity = logisticsList.stream().filter(t -> t.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
                TransferDeclareDetailDTO.AddDTO detailAddDTO = new TransferDeclareDetailDTO.AddDTO();
                detailAddDTO.setSoCode(soB2cEntity.getCode());
                detailAddDTO.setSoId(soB2cEntity.getId());
                detailAddDTO.setWeightUnit(unit);
                detailAddDTO.setLogisticsChannelId(logisticsEntity.getLogisticsChannelId());
                detailAddDTO.setPackageWeight(logisticsEntity.getWeight());
                detailAddDTO.setTrackNo(logisticsEntity.getCode());
                detailAddDTO.setShippingOrderNo(soB2cEntity.getShippingOrderNo());
                addDetailList.add(detailAddDTO);
            }
            addDTO.setDetailList(addDetailList);
            transferDeclareList.add(addDTO);
        }

        //需要添加中转报关单数据
        for (TransferDeclareDTO.AddDTO item : transferDeclareList) {
            try {
                //上次批次修改为上传成功
                item.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                item.getDetailList().forEach(v -> v.setOrderUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode()));
                BaseResultDTO.AddDTO result = transferDeclareFeign.add(item);
            } catch (Exception e) {
                log.error("中转报关单生成失败，错误信息 {}", e.getMessage());
            }
        }

        return resultDTOList;
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        SoB2cEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        // 审核中的数据允许撤销
        if (!Objects.equals(ingStatus, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(dto.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
        revokeDTO.setUserId(userId);
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        workflowFeign.revokeProcess(revokeDTO);
        ApproveStatusEnum waitSubmit = ApproveStatusEnum.WAIT_SUBMIT;
        this.updateApproveStatus(dto.getId(), waitSubmit.getStatus(), Boolean.FALSE);
        String msg = "销售订单【{}】撤销流程";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), dto.getId(), "撤销流程");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), "撤销流程");
    }


    /**
     * 反审核
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(String id) {
        SoB2cEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.APPROVE;
        // 已审核的数据才可以反审核
        if (!Objects.equals(approveStatus, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        String billStatus = entity.getBillStatus();
        //待配货
        String waitDistribution = SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
        //配货中
        String inDistribution = SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode();

        List<String> billStatusList = Arrays.asList(waitDistribution, inDistribution);
        if (!billStatusList.contains(billStatus)) {
            throw new ServiceException(ApiError.B2C_NOT_DISAPPROVE);
        }
        //预报成功不支持反审核
        if (TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), CharSequenceUtil.format("B2C销售订单【{}】已预报成功不支持反审核", entity.getCode()));
        }

        ApproveStatusEnum waitSubmit = ApproveStatusEnum.WAIT_SUBMIT;
        this.updateApproveStatus(id, waitSubmit.getStatus(), Boolean.FALSE);
        //删除已生成的申报信息
        soB2cDeclareProductService.removeBySoId(id);
        String msg = "销售订单【{}】反审核流程";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), id, "反审核流程");


        //同步数帝云
//        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(entity.getId());
//        syncSoB2cService.syncDataToSdy(entity, soB2cDetailEntityList, SyncOperateEnum.OPERATE_DISAPPROVE.getCode());

        //推送到DMP
//        syncOrderToDmp(entity.getId(), SyncOperateEnum.OPERATE_DISAPPROVE.getCode());


        return BatchResultDTO.success(entity.getId(), entity.getCode(), "反审核流程");
    }

    /**
     * @param id
     * @param code
     * @param approveStatusEnum
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     * @description: 订单审核规则不通过
     * @author Will
     * @date: 2023/8/24 15:14
     */
    private Boolean updateAbnormalTypeApprove(String id, String code, ApproveStatusEnum approveStatusEnum, SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核规则都不匹配", UserContext.getDefaultLoginUser().getUserName(), code, "B2C销售订单表", approveStatusEnum.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), id, "审核操作");
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(ObjectUtils.isNotEmpty(approveStatusEnum), SoB2cEntity::getApproveStatus, approveStatusEnum.getCode())
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalTypeEnum.getCode())
                .set(SoB2cEntity::getIsMatchOrderRule, Boolean.FALSE)
                .update(new SoB2cEntity());
    }

    /**
     * @param id
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     * @description: 物流规则不通过
     * @author Will
     * @date: 2023/12/14 9:40
     */
    private Boolean updateLogisticsAbnormalType(String id, SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalTypeEnum.getCode())
                .set(SoB2cEntity::getIsMatchLogisticsRule, Boolean.FALSE)
                .update(new SoB2cEntity());
    }


    /**
     * @param id
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     * @description: 仓库规则不通过
     * @author Will
     * @date: 2023/8/24 15:14
     */
    private Boolean updateWarehouseAbnormalType(String id, SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalTypeEnum.getCode())
                .update(new SoB2cEntity());
    }

    /**
     * @param id
     * @param soB2cBillStatusEnum
     * @description: 更新订单状态
     * @author Will
     * @date: 2023/8/24 14:55
     */
    public Boolean updateBillStatus(String id, SoB2cBillStatusEnum soB2cBillStatusEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getBillStatus, soB2cBillStatusEnum.getCode())
                .update(new SoB2cEntity());
    }




    @Override
    public SoB2cDTO.FinancialInfoDTO getFinancialInfoById(SoB2cDTO.FinancialParamDTO dto) {
        //销售订单
        SoB2cEntity soB2cEntity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));

        //物流信息
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(dto.getId());
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //财务信息
        SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceService.getByMainId(dto.getId());
        if (ObjectUtil.isEmpty(soB2cFinanceEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }

        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(dto.getId());
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(logisticsEntity);
        dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
        dto.setSoB2cDetailList(soB2cDetailList);
        SoB2cDTO.FinancialInfoDTO financialInfoDTO = getFinancialInfo(dto, Boolean.FALSE);
        return financialInfoDTO;
    }

    @Override
    public SoB2cDTO.FinancialInfoDTO getFinancialInfo(SoB2cDTO.FinancialParamDTO dto, Boolean isAdd) {

        //销售订单
        SoB2cEntity soB2cEntity = dto.getSoB2cEntity();
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = dto.getSoB2cLogisticsEntity();
        //明细信息
        List<SoB2cDetailEntity> soB2cDetailList = dto.getSoB2cDetailList();
        //财务信息
        SoB2cFinanceEntity soB2cFinanceEntity = dto.getSoB2cFinanceEntity();

        if (ObjectUtils.isEmpty(soB2cFinanceEntity) && !isAdd) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }
        //新增时用新对象
        if (isAdd) {
            soB2cFinanceEntity = new SoB2cFinanceEntity();
        }
        if (Objects.isNull(soB2cFinanceEntity.getShippingCost())){
            soB2cFinanceEntity.setShippingCost(soB2cEntity.getShippingFee());
        }

        //店铺信息
        ShopCostEntity shopCostEntity = shopCostService.getByShopId(soB2cEntity.getShopId());
        //平台费
        BigDecimal platformCost = BigDecimal.ZERO;
        //avt 费
        BigDecimal vatCost = BigDecimal.ZERO;
        //转账费
        BigDecimal paypalCost = BigDecimal.ZERO;

        SoB2cDTO.FinancialInfoDTO financialInfoDTO = new SoB2cDTO.FinancialInfoDTO();
        BeanMapperUtils.copy(soB2cFinanceEntity, financialInfoDTO);

        //商品成本,订单SKU*数量的含税成本价汇总
        BigDecimal itemCost = soB2cDetailList.stream().map(obj -> MathUtil.multiplyWithTwo(obj.getTaxCost(), obj.getQty())).reduce(BigDecimal.ZERO, BigDecimal::add);

        //商品金额
        BigDecimal totalAmount = soB2cDetailList.stream().map(SoB2cDetailEntity::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        //运费收入
        financialInfoDTO.setShippingCost(ObjectUtil.isEmpty(financialInfoDTO.getShippingCost()) ?
                BigDecimal.ZERO : financialInfoDTO.getShippingCost());
        //物流成本
        financialInfoDTO.setLogisticsCost(ObjectUtil.isEmpty(financialInfoDTO.getLogisticsCost()) ?
                BigDecimal.ZERO : financialInfoDTO.getLogisticsCost());

        //判断是否是人民币
        if (ObjectUtils.isNotEmpty(dto.getIsCny()) && dto.getIsCny()) {
            financialInfoDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            financialInfoDTO.setAmount(MathUtil.multiplyWithTwo(totalAmount, soB2cEntity.getExchangeRate()));
            financialInfoDTO.setItemCost(itemCost);
            //运费收入
            financialInfoDTO.setShippingCost(MathUtil.multiplyWithTwo(financialInfoDTO.getShippingCost(), soB2cEntity.getExchangeRate()));
            //物流成本
            financialInfoDTO.setLogisticsCost(MathUtil.multiplyWithTwo(financialInfoDTO.getLogisticsCost(), soB2cEntity.getExchangeRate()));
            //平台费
            financialInfoDTO.setPlatformCost(MathUtil.multiplyWithTwo(financialInfoDTO.getPlatformCost(), soB2cEntity.getExchangeRate()));
        } else {
            financialInfoDTO.setCurrency(soB2cEntity.getCurrency());
            financialInfoDTO.setAmount(totalAmount);
            financialInfoDTO.setItemCost(MathUtil.divide(itemCost, soB2cEntity.getExchangeRate()));
        }

        String platformOption = soB2cFinanceEntity.getPlatformCostType();
        String vatOption = soB2cFinanceEntity.getVatCostType();
        String transferOption = soB2cFinanceEntity.getTransferCostType();
        BigDecimal platformRate = soB2cFinanceEntity.getPlatformRate();
        BigDecimal vatRate = soB2cFinanceEntity.getVatRate();
        BigDecimal transferRate = soB2cFinanceEntity.getTransferRate();
        if (isAdd) {
            if (ObjectUtils.isNotEmpty(shopCostEntity)) {
                platformOption = shopCostEntity.getDictPlatformOption();
                vatOption = shopCostEntity.getDictVatOption();
                transferOption = shopCostEntity.getDictTransferOption();
                platformRate = shopCostEntity.getPlatformRate();
                vatRate = shopCostEntity.getVatRate();
                transferRate = shopCostEntity.getTransferRate();

            }
        }

        DictBasicEntity dictPlatformOption = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SHOP_PLATFORM_COST.getType(), platformOption);

        DictBasicEntity dictVatOption = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SHOP_VAT_COST.getType(), vatOption);

        DictBasicEntity dictTransferOption = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SHOP_TRANSFER_COST.getType(), transferOption);

        //平台费
        BigDecimal dividePlatformRate = MathUtil.divide(platformRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictPlatformOption) && ShopPlatformCostEnum.MULTIPLY_PLATFORM_RATE.getCode().equals(dictPlatformOption.getValue())) {
            platformCost = MathUtil.multiplyWithTwo(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), dividePlatformRate);
            financialInfoDTO.setPlatformCostType(dictPlatformOption.getValue());
            financialInfoDTO.setPlatformRate(platformRate);
        }
        //转账费
        BigDecimal divideTransferRate = MathUtil.divide(transferRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictTransferOption) && ShopTransferCostEnum.MULTIPLY_TRANSFER_RATE.getCode().equals(dictTransferOption.getValue())) {
            paypalCost = MathUtil.multiplyWithTwo(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), divideTransferRate);
            financialInfoDTO.setTransferCostType(dictTransferOption.getValue());
            financialInfoDTO.setTransferRate(transferRate);
        }
        //vat费
        BigDecimal divideVatRate = MathUtil.divide(vatRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.MULTIPLY_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.multiplyWithTwo(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), divideVatRate);
        } else if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.MULTIPLY_ADD_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.multiplyWithTwo(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), MathUtil.add(BigDecimal.ONE, divideVatRate)).multiply(divideVatRate);
        } else if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.DIVISION_ADD_MULTIPLY_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.divide(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), MathUtil.add(BigDecimal.ONE, divideVatRate)).multiply(divideVatRate);
        }
        if (ObjectUtils.isNotEmpty(dictVatOption)) {
            financialInfoDTO.setVatCostType(dictVatOption.getValue());
        }
        financialInfoDTO.setVatRate(vatRate);

        //平台费,店铺计算
        if (MathUtil.compareTo(financialInfoDTO.getPlatformCost(), BigDecimal.ZERO) == 0) {
            financialInfoDTO.setPlatformCost(platformCost);
        }

        //转账费,店铺计算
        financialInfoDTO.setPaypalCost(paypalCost);
        //包装辅料费,包装辅料SKU*数量的成本价汇总
        BigDecimal accessoriesCost = BigDecimal.ZERO;
        String accessoriesSkuId = soB2cLogisticsEntity.getAccessoriesSkuId();
        if (StringUtils.isNotBlank(soB2cLogisticsEntity.getAccessoriesSkuId())) {
            List<SkuVO> list = plmTaskFeign.listSkuCostByIds(Arrays.asList(accessoriesSkuId));
            if (CollectionUtils.isNotEmpty(list)) {
                accessoriesCost = MathUtil.multiplyWithTwo(list.get(0).getTargetTaxCost(), soB2cLogisticsEntity.getAccessoriesQty());
            }
        }

        financialInfoDTO.setAccessoriesCost(accessoriesCost);
        //VAT税费,店铺计算
        financialInfoDTO.setVatCost(vatCost);
        //总利润,订单总金额+运费收入-商品成本-物流成本-平台费-转账费-包装辅料费-VAT税费
        BigDecimal profit = ObjectUtil.defaultIfNull(financialInfoDTO.getAmount(), BigDecimal.ZERO)
                .add(ObjectUtil.defaultIfNull(financialInfoDTO.getShippingCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getItemCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getLogisticsCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(platformCost, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(paypalCost, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getAccessoriesCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(vatCost, BigDecimal.ZERO));
        financialInfoDTO.setProfit(profit);
        //利润率,总利润/(订单总金额+运费收入)*100%
        BigDecimal itemCostProfitRate = MathUtil.divide(itemCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setItemCostProfitRate(MathUtil.compareTo(itemCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : itemCostProfitRate + "%");

        BigDecimal logisticsCostProfitRate = MathUtil.divide(financialInfoDTO.getLogisticsCost(), MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setLogisticsCostProfitRate(MathUtil.compareTo(logisticsCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : logisticsCostProfitRate + "%");

        BigDecimal paypalCostProfitRate = MathUtil.divide(paypalCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setPaypalCostProfitRate(MathUtil.compareTo(paypalCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : paypalCostProfitRate + "%");

        BigDecimal platformCostProfitRate = MathUtil.divide(platformCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setPlatformCostProfitRate(MathUtil.compareTo(platformCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : platformCostProfitRate + "%");

        BigDecimal accessoriesCostProfitRate = MathUtil.divide(accessoriesCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setAccessoriesCostProfitRate(MathUtil.compareTo(accessoriesCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : accessoriesCostProfitRate + "%");

        BigDecimal vatCostProfitRate = MathUtil.divide(vatCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setVatCostProfitRate(MathUtil.compareTo(vatCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : vatCostProfitRate + "%");

        BigDecimal profitRate = MathUtil.divide(profit, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);

        BigDecimal profitRateFlag = MathUtil.divide(profit, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()));
        financialInfoDTO.setProfitRateFlag(profitRateFlag);
        financialInfoDTO.setProfitRate(MathUtil.compareTo(profitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : profitRate + "%");
        return financialInfoDTO;
    }


    @Override
    public BatchResultDTO isNotNeedMerge(String id) {
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        lambdaUpdate()
                .eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getIsNotMerge, Boolean.TRUE)
                .update();
        //操作日志
        String msg = "销售订单【{}】标记不合并";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "销售订单不合并");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "标记不合并");
    }

    /**
     * @param soB2cEntity
     * @description: 新增财务信息
     * @author Will
     * @date: 2023/9/8 12:26
     */
    private void addSoB2cFinance(SoB2cEntity soB2cEntity) {
        //物流信息
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(soB2cEntity.getId());
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
        dto.setId(soB2cEntity.getId());
        dto.setIsCny(Boolean.FALSE);
        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(logisticsEntity);
        dto.setSoB2cDetailList(soB2cDetailList);
        SoB2cDTO.FinancialInfoDTO financialInfoDTO = getFinancialInfo(dto, Boolean.TRUE);
        SoB2cFinanceDTO.AddDTO addDTO = BeanMapperUtils.map(SoB2cFinanceDTO.AddDTO.class, financialInfoDTO);
        addDTO.setMainId(soB2cEntity.getId());
        soB2cFinanceService.add(addDTO);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cDTO.PullOrderResultDTO saveOrUpdateEntity(PlatformOrderDTO dto, ShopInfoEntity shopInfo) {
        SoB2cDTO.PullOrderResultDTO resultDTO = new SoB2cDTO.PullOrderResultDTO();
        if (dto.getInvalidStatus()) {
            dto.setRemark("平台取消");
        }
        // 平台来源币种为空取默认币种
        if (StringUtils.isBlank(dto.getCurrency())) {
            dto.setCurrency(shopInfo.getSettlementCurrency());
        }
        // 全托管币种取值为交易币种
        if (isFullyManagedOrder(dto.getDictPlatform())){
            dto.setCurrency(shopInfo.getTradeCurrency());
        }
        log.debug("===== start saveOrUpdateEntity:{}", dto);
        SoB2cEntity oldEntity = null;
        try {
            if(StringUtils.isNotBlank(dto.getThirdSystem())){
                oldEntity = this.getByThirdInfo(dto.getThirdCode(), dto.getDictPlatform(), dto.getShopId(), SourceTypeEnum.SO_B2C.getCode());
            }else{
                oldEntity = this.getByPlatformInfo(dto.getPlatformCode(), dto.getDictPlatform(), dto.getShopId(), SourceTypeEnum.SO_B2C.getCode());
            }
        } catch (Exception e) {
            log.error("查询订单异常：{}", e.getMessage());
        }
        if (null == oldEntity) {
            // 组合信息
            SoB2cEntity entity = new SoB2cEntity();
            BeanUtils.copyProperties(dto, entity);
            handleData(entity, false, false);
            if (StringUtils.isNotBlank(dto.getApproveStatusStr())) {
                ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(dto.getApproveStatusStr());
                if (null == approveStatusEnum) {
                    String msg = CharSequenceUtil.format("[{}]审核状态类型存在:{}", dto.getUniqueId(), dto.getApproveStatusStr());
                    throw new ServiceException(msg);
                }
                entity.setApproveStatus(approveStatusEnum);
            }
            // 检查新增自动作废
            // Shopify全退款的订单新增自动作废
            entity.setInvalidStatus(dto.checkInsertInvalidStatus());
            if(entity.getInvalidStatus()){
                entity.setInvalidType(SoB2cInvalidTypeEnum.ENUM_AUTOMATIC.getCode());
            }
            // 生成单号
            String businessNo = "";
            if (isFullyManagedOrder(dto.getDictPlatform())){
                businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSBH);
            }else {
                businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSDD);
            }
            entity.setCode(businessNo);
            boolean save = false;
            try {
                save = this.save(entity);
            } catch (Exception e) {
                log.error("报错实体：{}", entity);
                log.error("保存订单信息异常：PlatformCode：{},{}", dto.getPlatformCode(), e.getMessage());
            }
            if (!save) {
                throw new ServiceException("soB2c订单保存失败");
            }
            //记录平台拉取记录
            List<String> detailMsgList = new ArrayList<>();
            dto.getDetails().forEach(detail -> {
                detailMsgList.add(CharSequenceUtil.format("【平台SKU:{}平台ID:{}】*数量【{}】", detail.getPlatformSkuNo(), detail.getPlatformSpuNo(), detail.getQty())) ;
            });
            String msg = CharSequenceUtil.format("从【{}】平台下载订单成功,订单金额：【{}】，总计拉取【{}】个SKU,分别是：{}", dto.getDictPlatform(),entity.getAmount() + entity.getCurrency(), dto.getDetails().size(), String.join(";", detailMsgList));
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "新增操作");
            resultDTO.setSoB2cEntity(entity);
            // 记录是新增的订单
            resultDTO.setNewInsertOrder(true);
            return resultDTO;
        } else {
            // 是否是状态变更为取消状态：是=从非取消变更为取消
            resultDTO.setUpdateCancel(!oldEntity.getIsCancel() && null != dto.getIsCancel() && dto.getIsCancel());
            // 历史异常记录修复
            if (StringUtils.isBlank(oldEntity.getCode()) && !BusinessCommonConstants.hasProfile("prod")) {
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSDD);
                oldEntity.setCode(code);
            }
            if (StringUtils.isBlank(oldEntity.getShopId()) && !BusinessCommonConstants.hasProfile("prod")) {
                oldEntity.setShopId(dto.getShopId());
            }
            //

            if (0 == oldEntity.getExchangeRate().compareTo(BigDecimal.ZERO)) {
                handleData(oldEntity, false, false);
            }
            ApproveStatusEnum oldApproveStatus = oldEntity.getApproveStatus();
            // 自发货订单如果来源状态是带配货不更新状态, 审核状态也不更新
            if (!oldEntity.hasPlatformWarehouseOrder() && SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equalsIgnoreCase(dto.getBillStatus())) {
                dto.setApproveStatusStr("");
            }
            if (StringUtils.isNotBlank(dto.getApproveStatusStr())) {
                ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(dto.getApproveStatusStr());
                if (null == approveStatusEnum) {
                    String msg = CharSequenceUtil.format("[{}]审核状态类型存在:{}", dto.getUniqueId(), dto.getApproveStatusStr());
                    throw new ServiceException(msg);
                }
                oldEntity.setApproveStatus(approveStatusEnum);
            }
            dto.setNfeInvoiceStatus(oldEntity.getNfeInvoiceStatus());
            //平台订单状态
            String platformOrderStatus = dto.getPlatformOrderStatus();
            // 亚马逊作废保留以前状态
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform()) && dto.getInvalidStatus()) {
                oldEntity.setApproveStatus(oldApproveStatus);
                dto.setPayStatus(oldEntity.getPayStatus());
                dto.setPayTime(oldEntity.getPayTime());
                dto.setBillStatus(oldEntity.getBillStatus());
            }
            // Shopify作废保留以前状态
            if (PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dto.getDictPlatform()) &&
                    ("voided".equalsIgnoreCase(dto.getPlatformOrderStatus())) || ("partially_refunded".equalsIgnoreCase(dto.getPlatformOrderStatus()))) {
                oldEntity.setApproveStatus(oldApproveStatus);
                dto.setPayStatus(oldEntity.getPayStatus());
                dto.setPayTime(oldEntity.getPayTime());
                dto.setBillStatus(oldEntity.getBillStatus());
            }
            // SHOPEE作废保留以前状态
            if (!oldEntity.hasPlatformWarehouseOrder() && PlatformDictEnum.SHOPEE.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("PROCESSED".equalsIgnoreCase(dto.getPlatformOrderStatus())
                        || ("RETRY_SHIP".equalsIgnoreCase(dto.getPlatformOrderStatus()))
                        || ("SHIPPED".equalsIgnoreCase(dto.getPlatformOrderStatus()))
                ) {
                    dto.setBillStatus(oldEntity.getBillStatus());
                }
                if ("IN_CANCEL".equalsIgnoreCase(dto.getPlatformOrderStatus())
                        || ("CANCELLED".equalsIgnoreCase(dto.getPlatformOrderStatus()))) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setPayTime(oldEntity.getPayTime());
                    dto.setBillStatus(oldEntity.getBillStatus());
                    dto.setApproveStatusStr("");//取消和取消中不更新审核状态
                }
            }

            //速卖通
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("IN_CANCEL".equals(platformOrderStatus)
                        || "IN_FROZEN".equals(platformOrderStatus)
                        || "RISK_CONTROL".equals(platformOrderStatus)) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
                }
                if ("FINISH".equals(platformOrderStatus)) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                }
            }
            //全托管订单 存在历史订单信息，扩展字段数据不更新
            if (PlatformDictEnum.TIK_TOK_FULLY.getCode().equalsIgnoreCase(dto.getDictPlatform())){
                dto.setExtendData(oldEntity.getExtendData());
            }
            // 全托管作废保留以前状态
            if (PlatformDictEnum.TIK_TOK_FULLY.getCode().equalsIgnoreCase(dto.getDictPlatform()) && dto.getInvalidStatus()) {
                oldEntity.setApproveStatus(oldApproveStatus);
                dto.setPayStatus(oldEntity.getPayStatus());
                dto.setPayTime(oldEntity.getPayTime());
                dto.setBillStatus(oldEntity.getBillStatus());
            }
            //沃尔玛
            if (PlatformDictEnum.WALMART.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("Cancelled".equals(platformOrderStatus)
                        || "Refund".equals(platformOrderStatus)
                ) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                }
            }
            //TikTok
            if (PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("AWAITING_COLLECTION".equalsIgnoreCase(platformOrderStatus)
                        || "PARTIALLY_SHIPPING".equalsIgnoreCase(platformOrderStatus)
                        || "IN_TRANSIT".equalsIgnoreCase(platformOrderStatus)
                        || "DELIVERED".equalsIgnoreCase(platformOrderStatus)
                        || "COMPLETED".equalsIgnoreCase(platformOrderStatus)
                ) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                    dto.setInvalidStatus(oldEntity.getInvalidStatus());
                    if ("平台作废".equals(oldEntity.getRemark())) {
                        oldEntity.setRemark("");
                    }
                }
                if ("CANCELLED".equalsIgnoreCase(platformOrderStatus)) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                    dto.setInvalidStatus(oldEntity.getInvalidStatus());
                    dto.setIsCancel(Boolean.TRUE);
                }
            }
            // 自发货订单状态不更新(由ERP系统决定)
            if (!oldEntity.hasPlatformWarehouseOrder()) {
                //1、当ERP订单状态是待配货 或者 配货中  推过来冻结中的状态 就直接改订单状态为冻结中，增加冻结标识；
                //2、当ERP订单状态是除了待配货和配货中  就不改为冻结中的状态
                String oldStatus = oldEntity.getBillStatus();
                String newStatus = dto.getBillStatus();
                // 处理冻结状态转换
                if (newStatus.equals(SoB2cBillStatusEnum.ENUM_FROZEN.getCode())) {
                    // 只有待配货和配货中状态可以转为冻结
                    if (!oldStatus.equals(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode()) &&
                            !oldStatus.equals(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode())) {
                        // 非待配货/配货中状态，保持原状态
                        dto.setBillStatus(oldStatus);
                    }
                }
                // 处理解冻状态转换
                else if (oldStatus.equals(SoB2cBillStatusEnum.ENUM_FROZEN.getCode())) {
                    // 从冻结状态转出，默认回到待配货
                    dto.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                } else {
                    dto.setBillStatus(oldEntity.getBillStatus());
                }
            }

            // 自发货订单的平台状态作废：如果订单状态是(待发货/已发货/部分发货)=已有发货单不作废，只添加平台作废记录
            if (dto.getInvalidStatus() && oldEntity.hasB2cSelfDelivery()
            ) {
                // 查询是否是本平台发货
                dto.setInvalidStatus(false);
                dto.setInvalidType(oldEntity.getInvalidType());
                dto.setInvalidRemark("平台取消或退款");
            }else if (dto.getInvalidStatus()){
                dto.setInvalidType(oldEntity.getInvalidType());
            }
            // 保留历史作废状态
            if (oldEntity.getInvalidStatus()) {
                dto.setInvalidStatus(true);
                dto.setInvalidRemark(oldEntity.getInvalidRemark());
            }

            // 只替换更新信息
            SoB2cEntity entity = B2cOrderConsumerConverter.INSTANCE.convertUpdateMainOrder(oldEntity, dto);
            if (StringUtils.isNotBlank(dto.getSellerOrderCode())) {
                entity.setSellerOrderCode(dto.getSellerOrderCode());
            }
            if (!oldEntity.toString().equals(entity.toString())) {
                if (!this.updateById(entity)) {
                    throw new ServiceException("soB2c订单更新失败");
                }
            }

            resultDTO.setSoB2cEntity(entity);

            return resultDTO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cEntity getByPlatformInfo(String platformCode, String dictPlatform, String shopId, String sourceType) {
        return lambdaQuery()
                .eq(SoB2cEntity::getPlatformCode, platformCode)
                .eq(SoB2cEntity::getDictPlatform, dictPlatform)
                .eq(SoB2cEntity::getShopId, shopId)
                .eq(SoB2cEntity::getSourceType, sourceType)
                .last(SqlConstants.LIMIT_1)
                .one();
    }

    public SoB2cEntity getByThirdInfo(String thirdCode, String dictPlatform, String shopId, String sourceType) {
        return lambdaQuery()
                .eq(SoB2cEntity::getThirdCode, thirdCode)
                .eq(SoB2cEntity::getDictPlatform, dictPlatform)
                .eq(SoB2cEntity::getShopId, shopId)
                .eq(SoB2cEntity::getSourceType, sourceType)
                .last(SqlConstants.LIMIT_1)
                .one();
    }
    @Override
    public Map<String, Object> getJson(String id) {
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        Map<String, Object> obj = handleMatchJson(id, soB2cDetailList, new HashMap<>());
        return obj;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean matchSku(SoB2cDTO.MatchSkuDTO dto) {
        //详情id
        String detailId = dto.getId();
        SoB2cDetailEntity detailEntity = soB2cDetailService.getById(detailId);
        if (Objects.isNull(detailEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        SoB2cEntity entity = this.getById(detailEntity.getMainId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            // 未找到B2C销售订单明细信息
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //根据平台sku查询映射信息
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(Collections.singletonList(detailEntity.getPlatformSkuNo()));
        // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(entity.getDictPlatform()) ||
                PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(entity.getDictPlatform()) ||
                PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(entity.getDictPlatform()) ||
                PlatformDictEnum.SHOPEE.getCode().equalsIgnoreCase(entity.getDictPlatform()) ||
                PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(entity.getDictPlatform()) ||
                PlatformDictEnum.TE_MU.getCode().equalsIgnoreCase(entity.getDictPlatform()) ||
                PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(entity.getDictPlatform())) {
            listingInfoParamDTO.setPlatformSpuNoList(Collections.singletonList(detailEntity.getPlatformSpuNo()));
        }
        listingInfoParamDTO.setPlatform(entity.getDictPlatform());
        listingInfoParamDTO.setShopIdList(Collections.singletonList(entity.getShopId()));
        List<SkuMappingDTO.MappingSkuViewDTO> skuDTOS = skuMappingService.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
        List<SkuMappingDTO.MappingSkuViewDTO> collect = skuDTOS.stream()
                .filter(req -> req.getPlatformSkuNo().equals(detailEntity.getPlatformSkuNo())
                        && StringUtils.isNotBlank(req.getProductSkuNo()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.EXIST_SKU_MAPPING);
        }
        if (ObjectUtil.isEmpty(skuDTOS)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }

        List<String> skuIds = Collections.singletonList(dto.getSkuId());
        List<SkuInfoSimpleVO> skuList = new ArrayList<>();
        Map<String, SkuInfoSimpleVO> skuMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            skuList = plmTaskFeign.getSimpleSkuInfoByIds(skuIds);
            skuMap = skuList.stream().collect(Collectors.toMap(SkuInfoSimpleVO::getSkuId, Function.identity()));
        }
        SkuInfoSimpleVO simpleVO = skuMap.get(dto.getSkuId());
        if (null == simpleVO) {
            throw new ServiceException(ApiError.NOT_EXIST, "sku");
        }

        // 需要更新的明细
        SoB2cDetailEntity handleDetailEntity = detailList.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().orElse(null);
        if (null == handleDetailEntity) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        handleDetailEntity.setSkuId(simpleVO.getSkuId());
        handleDetailEntity.setSkuNo(simpleVO.getSkuNo());
        handleDetailEntity.setImageUrl(simpleVO.getSkuImagesUrl());
        handleDetailEntity.setCurrentNetWeight(simpleVO.getGrossWeight());
        // 设置其他处理
        soB2cDetailService.consumerHandleDetailList(detailList, entity, skuList);
        //长宽高计算
        BigDecimal maxLength = BigDecimal.ZERO;
        BigDecimal maxWidth = BigDecimal.ZERO;
        BigDecimal totalHeight = BigDecimal.ZERO;
        BigDecimal allNetWeight = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(skuList)) {
            //拆分明细
            List<SplitSkuDTO> splitSkuDTOS = splitBySoDetail(detailList, skuIds, entity.getCode(), false);
            //根据sku进行计算
            List<String> keyList = new ArrayList<>();
            keyList.add(CalculateSizeEnum.LENGTH.getCode());
            keyList.add(CalculateSizeEnum.WIDTH.getCode());
            keyList.add(CalculateSizeEnum.HEIGHT.getCode());
            keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
            List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
            Map<String, String> cfgCollect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
            maxLength = SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS, cfgCollect.get(CalculateSizeEnum.LENGTH.getCode()));
            maxWidth = SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS, cfgCollect.get(CalculateSizeEnum.WIDTH.getCode()));
            totalHeight = SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS, cfgCollect.get(CalculateSizeEnum.HEIGHT.getCode()));
            allNetWeight = SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, cfgCollect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode()));
        }
        //物流信息更新保存
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
        logisticsEntity.setWeight(allNetWeight);
        logisticsEntity.setLength(maxLength);
        logisticsEntity.setWidth(maxWidth);
        logisticsEntity.setHeight(totalHeight);
        soB2cLogisticsService.updateById(logisticsEntity);

        //更新明细
        soB2cDetailService.updateById(handleDetailEntity);

//        syncSoB2cService.syncDataToSdy(entity, Arrays.asList(handleDetailEntity), SyncOperateEnum.OPERATE_APPROVE.getCode());

        // 更新映射
        FbaShipmentDTO.SkuMappingParamDTO updateDTO = new FbaShipmentDTO.SkuMappingParamDTO();
        //映射sku
        updateDTO.setSkuNo(handleDetailEntity.getSkuNo());
        updateDTO.setSkuId(handleDetailEntity.getSkuId());
        updateDTO.setMsku(handleDetailEntity.getPlatformSkuNo());
        updateDTO.setShopId(entity.getShopId());
        updateDTO.setPlatform(entity.getDictPlatform());
        updateDTO.setId(skuDTOS.get(0).getId());
        Boolean flag = listingInfoService.skuMapping(updateDTO);
        if (flag) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("映射了一个sku【%s】", simpleVO.getSkuNo()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "编辑信息");
        }
        return flag;
    }

    @Override
    public void updateLingXingOrder(SoB2cEntity entity, List<SoB2cDetailEntity> detailEntityList) {
        // 校验sku是否存在领星
        List<String> skuNoList = detailEntityList.stream().map(e->LingxingApiUtils.convertLxSku(e.getSkuNo())).collect(Collectors.toList());
        LingxingApiUtils.checkSkuSyncLx(skuNoList);

        List<SoB2cDetailEntity> splitDetailList = detailEntityList.stream().filter(v->StringUtils.isNotBlank(v.getSplitDetailId())).collect(Collectors.toList());
        List<String> splitDetailIds =  splitDetailList.stream().map(SoB2cDetailEntity::getSplitDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //原来
        List<SoB2cDetailEntity> allOriginDetailList = soB2cDetailService.listContainDeleted(splitDetailIds);
        detailEntityList = detailEntityList.stream().filter(v->StringUtils.isBlank(v.getSplitDetailId())).collect(Collectors.toList());
        detailEntityList.addAll(allOriginDetailList);

        UpdateOrderDTO.OrderInfo orderInfo = new UpdateOrderDTO.OrderInfo();
        orderInfo.setGlobalOrderNo(entity.getThirdCode());
        List<UpdateOrderDTO.OrderItem> orderItemList = new ArrayList<>();
        detailEntityList.forEach(detailEntity -> {
            UpdateOrderDTO.OrderItem orderItem = new UpdateOrderDTO.OrderItem();
            orderItem.setMsku(detailEntity.getPlatformSkuNo());
            orderItem.setSku(LingxingApiUtils.convertLxSku(detailEntity.getSkuNo()));
            orderItem.setQuantity(detailEntity.getQty());
            orderItem.setId(detailEntity.getThirdDetailId());
            orderItem.setMark("更新订单");
            orderItem.setPrice(0);
            orderItem.setType(3);
            orderItemList.add(orderItem);
        });
        orderInfo.setOrderItemList(orderItemList);
        LingxingApiUtils.updateOrder(Arrays.asList(orderInfo));
    }

    /**
     * @param id
     * @return
     * @description 更改订单为发货
     * @author Lambda
     * @create 2023-12-13 17:49
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean orderShipped(String id) {
        Boolean updateResult = this.lambdaUpdate().set(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_SHIPPED.getCode()).
                eq(SoB2cEntity::getId, id).update();
        if (updateResult) {
            String msg = "销售订单已发货";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, id), ModuleTypeEnum.SO_B2C.getCode(), id, "已发货");
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(String soCode) {

        SoB2cEntity entity = this.getByCode(soCode);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单");
        }
        String id = entity.getId();
        return this.getSoOutstockInfoById(id);

    }

    @Override
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockByIdAndWarehouseId(String id, String warehouseId) {
        SoB2cEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单");
        }
        SoB2cLogisticsEntity soB2cLogistics = soB2cLogisticsService.getByMainId(id);
        SoB2cReceiverEntity soB2cReceiver = soB2cReceiverService.getByMainId(id);
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(entity.getShopId());
        if (Objects.isNull(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        CustomerInfoEntity customerInfo = customerInfoService.getById(shopInfoEntity.getCustomerId());
        SoOutstockDTO.GenerateB2cDTO dto = new SoOutstockDTO.GenerateB2cDTO();
        dto.setOrderType(OrderTypeEnum.B2C.getCode());
        dto.setSoId(entity.getId());
        dto.setShopId(entity.getShopId());
        dto.setDictPlatform(entity.getDictPlatform());
        dto.setSoCode(entity.getCode());
        dto.setPlanDeliveryDate(entity.getCreateTime().toLocalDate());
        String sourceId = "";
//        String sourceType = SourceTypeEnum.SO_B2C.getCode();
        String sourceCode = entity.getPlatformCode();
        dto.setSourceId(sourceId);
        dto.setSourceCode(sourceCode);
        String chargeId = shopInfoEntity.getChargeId();
        dto.setCustomerId(shopInfoEntity.getCustomerId());
        dto.setCustomerName(shopInfoEntity.getName());
        if (Objects.nonNull(customerInfo)) {
            dto.setSellerId(customerInfo.getSellerId());
            dto.setSellerName(customerInfo.getSellerName());
            dto.setSalesDeptId(customerInfo.getSalesDeptId());
        } else {
            dto.setSellerId(chargeId);
            dto.setSellerName(shopInfoEntity.getChargeName());
        }
        if (Objects.nonNull(soB2cReceiver)) {
            dto.setCountry(soB2cReceiver.getCountry());
        }
//        if (CharSequenceUtil.isBlank(dto.getSalesDeptId())) {
//            SysDepartmentUserNumberDTO deptUser = null;
//            if (!StringUtil.isEmpty(dto.getSellerId())) {
//                deptUser = sysUserFeign.getDeptByUserId(dto.getSellerId());
//            }
//            if (Objects.nonNull(deptUser)) {
//                dto.setSalesDeptId(deptUser.getDepartmentId());
//            }
//        }

        dto.setSalesOrgId(entity.getOrgId());
        dto.setSalesOrgName(entity.getOrgName());
        // 记录是否是平台仓订单
        dto.setHasPlatformWarehouseOrder(entity.hasPlatformWarehouseOrder());

        if (Objects.nonNull(soB2cLogistics)) {
            //渠道id
            String logisticsChannelId = soB2cLogistics.getLogisticsChannelId();
            if (StringUtils.isNotBlank(logisticsChannelId)) {
                LogisticsChannelDTO.BaseDTO logisticsBase = logisticsFeign.getChannelInfoById(logisticsChannelId);
                if (Objects.nonNull(logisticsBase)) {
                    dto.setCarrierId(logisticsBase.getLogisticsSupplierId());
                }
                dto.setLogisticsChannelId(logisticsChannelId);
                dto.setLogisticsChannelName(logisticsBase.getName());
            }
            String trackNo = soB2cLogistics.getTrackNo();
            if (StringUtils.isBlank(trackNo)) {
                trackNo = soB2cLogistics.getCode();
            }
            dto.setTrackNo(trackNo);
            dto.setTransportNo(soB2cLogistics.getCode());
        }

        //根据主表id 查询出库的信息
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        String sourceType;
        if (entity.hasPlatformWarehouseOrder()) {
            // 平台仓订单(平台销售出库单)(扣可用库存)
            sourceType = SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode();
        } else {
            List<String> warehouseIds = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
            List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIds);
            if (CollectionUtils.isNotEmpty(overseasWarehouseList)) {
                // 海外仓出库单 (扣可用库存)
                sourceType = SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode();
            } else {
                // B2C发货单 (扣冻结库存)
                sourceType = SourceTypeEnum.SO_B2C_DELIVERY.getCode();
            }
        }
        dto.setSourceType(sourceType);

        //是否中转
        Boolean isTransit = false;
        List<String> transferWarehouseIdList = null;
        // 平台仓/海外仓出库单不中转
        if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equalsIgnoreCase(sourceType)) {
            List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryFeign.listBySourceId(Collections.singletonList(entity.getId()));
            SoB2cDeliveryEntity soB2cDeliveryEntity = null;
            if (CollectionUtils.isNotEmpty(soB2cDeliveryEntities)) {
                soB2cDeliveryEntity = soB2cDeliveryEntities.stream().filter(e -> Objects.equals(e.getStatus(), SoB2cDeliveryStatusEnum.SHIPPED.getCode())).findFirst().orElse(null);
            }
            if (Objects.nonNull(soB2cDeliveryEntity)) {
                isTransit = Boolean.TRUE;
                String transferWarehouseIds = soB2cDeliveryEntity.getTransferWarehouseIds();
                transferWarehouseIdList = StrUtil.isBlank(transferWarehouseIds) ? null : StrUtil.split(transferWarehouseIds, ",");
            } else {
                // B2C订单根据中转规则判断是否中转
                CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
                ruleDTO.setType(StockOutTransferTypeEnum.B2C.getCode());
                ruleDTO.setReceiveCountry(soB2cReceiver.getCountry());
                String deliveryWarehouseId = StrUtil.isBlank(warehouseId) ? detailList.get(0).getWarehouseId() : warehouseId;
                ruleDTO.setFromWarehouse(deliveryWarehouseId);
                ruleDTO.setSalesOrgId(entity.getOrgId());
                ruleDTO.setDictPlatform(entity.getDictPlatform());
                CfgRuleOutDTO.MatchTransferResultDTO resultDTO = cfgRuleOutFeign.matchTransferRule(ruleDTO);
                isTransit = resultDTO.getIsTransit();
                transferWarehouseIdList = resultDTO.getTransferWarehouseIdList();
            }
        }
        if (isTransit && CollUtil.isNotEmpty(transferWarehouseIdList)) {
            warehouseId = transferWarehouseIdList.get(transferWarehouseIdList.size() - 1);
        } else {
            if (StringUtils.isBlank(warehouseId)) {
                warehouseId = detailList.get(0).getWarehouseId();
            }
        }

        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST_WAREHOUSE);
        }
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(warehouseList.get(0).getOrgId()));
        if (CollUtil.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG, warehouseList.get(0).getName());
        }

        dto.setWarehouseId(warehouseId);
        dto.setWarehouseName(warehouseList.get(0).getName());
        dto.setWarehouseOrgId(warehouseList.get(0).getOrgId());
        dto.setWarehouseOrgName(accountingCompanyList.get(0).getName());
        LinkedList<SoOutstockDetailDTO.AddDTO> wantDetailList = new LinkedList<>();

        //仓库经营类型
        String warehouseManageType = warehouseList.get(0).getWarehouseManageType();
        List<SoB2cDeliveryDTO.DeliverySkuDTO> deliverySkuList = listDeliverySku(entity.getShopId(), detailList, entity.getDictPlatform(), warehouseManageType, true);
        // 查询明细所有历史映射关系
        List<String> platformSkuList = detailList.stream()
                .map(SoB2cDetailEntity::getPlatformSkuNo)
                .distinct()
                .collect(Collectors.toList());
        // 查询明细所有历史映射关系
        List<String> platformSpuList = detailList.stream()
                .map(SoB2cDetailEntity::getPlatformSpuNo)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = skuMappingService.mapListingByPlatformSkuNo(platformSkuList, platformSpuList, entity.getDictPlatform(), entity.getShopId(), null, null);

        for (SoB2cDetailEntity detailItem : detailList) {
            String skuId = detailItem.getSkuId();
            if(StringUtils.isBlank(skuId)){
                throw new ServiceException("订单没有映射sku，无法出库");
            }
            //数量
            Integer qty = detailItem.getQty();
            String detailId = detailItem.getId();
            //中转无需传仓位
            String warehouseLocation = isTransit ? "" : detailItem.getWarehouseLocation();
            //查询到对应的数据
            List<SoB2cDeliveryDTO.DeliverySkuDTO> deliveryList = deliverySkuList.stream().filter(d -> detailItem.getPlatformSkuNo().equals(d.getPlatformSkuNo()) && skuId.equals(d.getSourceSkuId())).distinct().collect(Collectors.toList());

            if (!entity.hasPlatformWarehouseOrder() && CollectionUtils.isNotEmpty(deliveryList)) {
                // 自发货单有捆绑商品拆分
                String detailRemark = "B2C订单发货自动生成";
                for (SoB2cDeliveryDTO.DeliverySkuDTO deliverySku : deliveryList) {
                    //表示有啊
                    SoOutstockDetailDTO.AddDTO addDTO = new SoOutstockDetailDTO.AddDTO();
                    // 明细记录平台单号
                    addDTO.setPlatformCode(entity.getPlatformCode());
                    addDTO.setSkuId(deliverySku.getSkuId());
                    addDTO.setSkuNo(deliverySku.getSkuNo());
                    addDTO.setSourceDetailId(detailItem.getSourceDetailId());
                    addDTO.setSoDetailId(detailId);
                    Integer wantQty = qty * deliverySku.getQty();
                    addDTO.setPlanQty(wantQty);
                    addDTO.setActualQty(wantQty);
                    addDTO.setWarehouseLocation(warehouseLocation);
                    addDTO.setRemark(detailRemark);
                    wantDetailList.add(addDTO);
                }
            } else {
                // 自发货无捆绑商品拆分和平台仓不按捆绑商品拆分
                //表示有啊
                SoOutstockDetailDTO.AddDTO addDTO = new SoOutstockDetailDTO.AddDTO();
                // 明细记录平台单号
                addDTO.setPlatformCode(entity.getPlatformCode());
                addDTO.setSkuId(skuId);
                addDTO.setSkuNo(detailItem.getSkuNo());
                addDTO.setSourceDetailId(detailItem.getSourceDetailId());
                addDTO.setSoDetailId(detailId);
                addDTO.setPlanQty(qty);
                addDTO.setActualQty(qty);
                addDTO.setWarehouseId(detailItem.getWarehouseId());
                addDTO.setWarehouseLocation(warehouseLocation);
                addDTO.setRemark("B2C订单平台自动生成");
                addDTO.setPrice(detailItem.getPrice());
                addDTO.setAmount(detailItem.getAmount());
                addDTO.setTaxRate(detailItem.getTaxRate());
                addDTO.setExchangeRate(detailItem.getExchangeRate());
                addDTO.setCurrency(detailItem.getCurrency());
                // 平台订单记录历史映射
                if (entity.hasPlatformWarehouseOrder()) {
                    List<ListingInfoWithSkuMappingDTO> list;
                    if (PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(entity.getDictPlatform())
                            || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(entity.getDictPlatform())){
                        list = listingInfoWithSkuMappingDTOMap.getOrDefault(detailItem.getPlatformSpuNo(), Collections.emptyList());
                        list = list.stream().filter(v->v.getPlatformSkuNo().equals(detailItem.getPlatformSkuNo())).collect(Collectors.toList());
                        if(CollectionUtils.isEmpty(list)){
                            list = listingInfoWithSkuMappingDTOMap.getOrDefault(detailItem.getPlatformSpuNo(), Collections.emptyList());
                        }
                    }else{
                        list = listingInfoWithSkuMappingDTOMap.getOrDefault(detailItem.getPlatformSkuNo(), Collections.emptyList());
                    }
                    List<SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO> historyList = B2cOrderConverter.INSTANCE.skuMappingDTOListToGenDTOList(list);
                    // 按过期时间排序
                    List<SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO> sortList = historyList.stream()
                            .sorted(Comparator.comparing(SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO::getExpireTime))
                            .collect(Collectors.toList());
                    addDTO.setHistorySkuMappingList(new LinkedList<>(sortList));
                }
                wantDetailList.add(addDTO);
            }
        }
        dto.setDetailList(wantDetailList);
        return dto;
    }

    @Override
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(String id) {
        return getSoOutstockByIdAndWarehouseId(id, "");
    }

    @Override
    public SoB2cEntity getByCode(String soCode) {
        return this.lambdaQuery().eq(SoB2cEntity::getCode, soCode).last(SqlConstants.LIMIT_1).one();
    }

    /**
     * 运费测算 更改渠道
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean selectLogisticsChannel(SoB2cLogisticsDTO.SelectChannelDTO dto) {
        String id = dto.getId();
        String logisticsChannelId = dto.getLogisticsChannelId();
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待配货和配货中订单允许配货
        if (!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(entity.getBillStatus())
                && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_DISTRIBUTION, entity.getCode());
        }
        //只有已审核数据支持配货
        if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_APPROVE_NOT_DISTRIBUTION, entity.getCode());
        }
        //预报成功不支持更换渠道
        if (TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus())) {
            throw new ServiceException("已预报成功不支持更换渠道");
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //存在的物流渠道
        String existChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        //存在的物流单 code
        String code = soB2cLogisticsEntity.getCode();
        LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(logisticsChannelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(code)) {
            //取消物流单
            LogisticsBillDTO.CancelBillDTO cancelBillDTO = LogisticsBillDTO.CancelBillDTO.builder().
                    channelId(existChannelId).transportNo(code).platformCode(entity.getPlatformCode()).
                    referenceNumber(entity.getCode()).orderId(entity.getId()).shopId(entity.getShopId()).build();
            ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(cancelBillDTO);
            //取消失败
            if (!cancelResult.isSuccess() && cancelResult.getCode() != -1) {
                soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
                log.error("{}原因是：{}", ApiError.ERROR_SO_B2C_LOGISTICS_CANCEL_FAIL.msg, cancelResult.getMsg());
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_CANCEL_FAIL, entity.getCode());
            }
        }
        soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
        soB2cLogisticsEntity.setLogisticsChannelName(logisticsChannel.getName());
        soB2cLogisticsEntity.setCode("");
        soB2cLogisticsEntity.setTrackNo("");
        entity.setIsMatchLogisticsRule(Boolean.TRUE);
        this.updateById(entity);
        logisticsBillFeign.removeLogisticsBillBySourceId(Arrays.asList(id));
        soB2cErrorService.removeErrorOrder(id, SoB2cErrorTypeEnum.GET_LOGISTICS_LABEL.getCode());
        //物流信息更新
        return soB2cLogisticsService.updateById(soB2cLogisticsEntity);

    }

    /**
     * 平台仓订单处理
     * 走仓库规则 通过就是审核通过 并待发货
     * 没有通过就是审核通过有待配货
     *
     * @param
     * @param map
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-18 14:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public Boolean platformWarehouseOrderHandle(String id, Map<String, Object> map) {
        SoB2cEntity entity = this.getById(id);
        isExist(entity);
        //明细信息
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //校验是否被冻结
        if (entity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, entity.getCode());
        }

        //仓库匹配规则结果
        SoB2cDTO.RuleResultDTO ruleMatchResult = this.warehouseRule(entity.getId(), detailList, new HashMap<>());
        ApproveStatusEnum approveStatus = ApproveStatusEnum.APPROVE;
        entity.setApproveStatus(approveStatus);
        Boolean isRuleMatch = ruleMatchResult.getIsRuleMatch();
        if (isRuleMatch) {
            entity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        }
        //自动计算预估运费到订单的预估运费字段
        soB2cService.autoCalcEstimatedShippingCost(Collections.singletonList(entity.getId()));
        this.updateById(entity);
        return isRuleMatch;
    }

    @Override
    public Boolean updateSoB2cStatus(List<String> ids, String status, Boolean isManualDelivery) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.FALSE;
        }
        Boolean updateResult = lambdaUpdate().in(SoB2cEntity::getId, ids)
                .set(StrUtil.isNotBlank(status), SoB2cEntity::getBillStatus, status)
                .set(Objects.nonNull(isManualDelivery), SoB2cEntity::getIsManualDelivery, isManualDelivery)
                .update();
        if (StrUtil.isBlank(status)) {
            return updateResult;
        }
        String statusName = SoB2cBillStatusEnum.getName(status);
        String msg = "销售订单状态变更为:" + statusName;
        for (String id : ids) {
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, id), ModuleTypeEnum.SO_B2C.getCode(), id, "已发货");
        }
        return updateResult;
    }

    /**
     * 修改b2c销售单状态发货时间
     *
     * @param deliveryTimeDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     **/
    @Override
    public Boolean updateSoB2cStatusAndDeliveryTime(SoB2cDTO.UpdateDeliveryTimeDTO deliveryTimeDTO) {
        if (CollectionUtils.isEmpty(deliveryTimeDTO.getSoB2cIds())) {
            return Boolean.FALSE;
        }

        //修改状态
        Boolean updateResult = lambdaUpdate().in(SoB2cEntity::getId, deliveryTimeDTO.getSoB2cIds())
                .set(SoB2cEntity::getBillStatus, deliveryTimeDTO.getStatus())
                .update();
        if (CollectionUtils.isEmpty(deliveryTimeDTO.getSoB2cLogisticsList())) {
            //修改发货时间
            soB2cLogisticsService.updateDeliveryTimeByMainIds(deliveryTimeDTO.getSoB2cIds(), deliveryTimeDTO.getDeliveryTime());
        } else {
            deliveryTimeDTO.getSoB2cLogisticsList().forEach(obj -> obj.setDeliveryTime(deliveryTimeDTO.getDeliveryTime()));
            soB2cLogisticsService.updateBatchById(deliveryTimeDTO.getSoB2cLogisticsList());
        }

        String statusName = SoB2cBillStatusEnum.getName(deliveryTimeDTO.getStatus());
        String msg = "销售订单状态变更为:" + statusName;
        for (SoB2cDTO.SoDeliveryDTO soDeliveryDTO : deliveryTimeDTO.getSoDeliveryDTOList()) {
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("仓库发货成功，单号【{}】", soDeliveryDTO.getDeliveryCode()), ModuleTypeEnum.SO_B2C.getCode(), soDeliveryDTO.getSoId(), "仓库发货");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soDeliveryDTO.getSoId(), "已发货");
        }
        return updateResult;
    }

    @Override
    public List<PrintWayBillPdfDTO> printWayBillPdf(List<String> soIds) {
        List<SoB2cEntity> soB2cEntities = this.listByIds(soIds);

        //查询店铺信息
        List<ShopInfoEntity> shopInfoEntities = new ArrayList<>();
        List<String> list = soB2cEntities.stream().map(req -> req.getShopId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            shopInfoEntities = Collections.EMPTY_LIST;
        } else {
            shopInfoEntities = shopInfoService.listByIds(list);
        }

        //查询买家信息
        List<SoB2cReceiverEntity> soB2cReceiverEntities = soB2cReceiverService.listByMainIds(soIds);

        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cLogisticsService.listByMainIds(soIds);

        //标签信息
        List<SoB2cLabelEntity> soB2cLabelEntities = soB2cLabelService.listSoB2cLabelByMainIds(soIds);
        List<PrintWayBillPdfDTO> resultList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            PrintWayBillPdfDTO printWayBillPdfDTO = new PrintWayBillPdfDTO();
            printWayBillPdfDTO.setSoId(soB2cEntity.getId());
            printWayBillPdfDTO.setSoCode(soB2cEntity.getCode());
            printWayBillPdfDTO.setAmount(soB2cEntity.getAmount());
            printWayBillPdfDTO.setRemark(soB2cEntity.getRemark());
            List<String> base64List = soB2cLabelEntities.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).map(SoB2cLabelEntity::getLogisticsLabelBase64).collect(Collectors.toList());
            List<String> crossUrlList = soB2cLabelEntities.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).map(SoB2cLabelEntity::getCrossLabelUrl).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(crossUrlList)){
                crossUrlList.forEach(fileId ->{
                    byte[] bytes = fileFeign.downloadFile(fileId);
                    base64List.add("data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes));
                });
            }
            printWayBillPdfDTO.setLogisticsLabelBase64List(base64List);
            printWayBillPdfDTO.setPrintTime(cn.hutool.core.date.DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
            //店铺信息
            ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> req.getId().equals(soB2cEntity.getShopId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
                printWayBillPdfDTO.setShopName(shopInfoEntity.getName());
            }

            //买家信息
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverEntities.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cReceiverEntity)) {
                printWayBillPdfDTO.setCustomerId(soB2cReceiverEntity.getCustomerId());
            }

            //物流信息
            SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsEntities.stream().filter(req -> soB2cEntity.getId().equals(req.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsEntity)) {
                printWayBillPdfDTO.setTransportNo(logisticsEntity.getCode());
                printWayBillPdfDTO.setChannelName(logisticsEntity.getLogisticsChannelName());
                printWayBillPdfDTO.setWeight(logisticsEntity.getWeight());
                printWayBillPdfDTO.setLogisticsChannelId(logisticsEntity.getLogisticsChannelId());
            }
            //拦截标识
            printWayBillPdfDTO.setIsIntercept(soB2cEntity.getIsIntercept());
            resultList.add(printWayBillPdfDTO);
        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSoB2cStatusByParams(SoB2cDTO.UpdateStatusDTO dto) {
        String billStatus = dto.getBillStatus();
        if (StringUtils.isBlank(dto.getSoCode())) {
            return Boolean.FALSE;
        }
        // 记录跟踪号
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(dto.getSoId());
        if (null == logisticsEntity) {
            throw new ServiceException("物流信息为空");
        }
        logisticsEntity.setCode(CharSequenceUtil.isNotBlank(logisticsEntity.getCode()) ? logisticsEntity.getCode() : dto.getTrackNo());
        logisticsEntity.setTrackNo(CharSequenceUtil.isNotBlank(logisticsEntity.getTrackNo()) ? logisticsEntity.getTrackNo() : dto.getTrackNo());
        soB2cLogisticsService.updateById(logisticsEntity);
        if (dto.isAddOperationLog()) {
            operateLogService.addModuleOperateLog("海外仓发货成功", ModuleTypeEnum.SO_B2C.getCode(), dto.getSoId(), "海外仓发货");
        }
        return this.lambdaUpdate().eq(StringUtils.isNotBlank(dto.getSoCode()), SoB2cEntity::getCode, dto.getSoCode()).
                set(StringUtils.isNotBlank(billStatus), SoB2cEntity::getBillStatus, billStatus).
                update(new SoB2cEntity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public SoB2cDTO.RuleResultDTO orderRule(String id) {
        Map<String, Object> map = new HashMap<>();
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
        //自动匹配订单规则
        Map<String, Boolean> orderRule = soB2cService.approveRule(id, detailList, map);
        SoB2cDTO.RuleResultDTO ruleResult = new SoB2cDTO.RuleResultDTO();
        ruleResult.setId(id);
        ruleResult.setIsRuleMatch(orderRule.getOrDefault("isMatch", Boolean.FALSE));
        ruleResult.setIsPass(orderRule.getOrDefault("isPass", Boolean.FALSE));
        ruleResult.setMap(map);
        ruleResult.setSoB2cDetailList(detailList);
        return ruleResult;
    }

    /**
     * 仓库规则匹配
     *
     * @param id
     * @param detailList
     * @param map
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public SoB2cDTO.RuleResultDTO warehouseRule(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        SoB2cEntity entity = super.getById(id);
        isExist(entity);
        if (CollectionUtils.isEmpty(detailList)) {
            detailList = soB2cDetailService.listByMainId(id);
        }
        if (map.isEmpty()) {
            handleMatchJson(id, detailList, map);
        }
        //仓库匹配规则结果
        return ruleDeliveryWarehouseService.getRuleOrderMatchResult(entity, detailList, map);
    }

    /**
     * @param
     * @return
     * @description 正常订单规则
     * @author Lambda
     * @create 2023-12-18 14:54
     */
    @Override
    public Boolean pullOrderHandle(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        SoB2cDTO.RuleResultDTO ruleResultDTO = soB2cService.orderRule(id);
        Boolean isMatch = ruleResultDTO.getIsRuleMatch();
        Boolean isPass = ruleResultDTO.getIsPass();
        if (isMatch && isPass) {
            SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRule(id, detailList, map);
            Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
            if (warehouseRuleMatch) {
                SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRule(id, new HashMap<>(), false);
                if (logisticsRuleResult.getIsRuleMatch()) {
                    soB2cService.checkProductRegistrationAndUpdate(id, "");
                    //申报信息规则
                    declareRule(id, new HashMap<>(), Boolean.FALSE, false);
                }
                SoB2cEntity entity = soB2cService.getById(id);
                Boolean autoGetTrackNo = logisticsRuleResult.getAutoGetTrackNo();
                Boolean autoGetTrackNotOfRangeDelivery = logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery();
                Boolean isOutOfRangeDelivery = entity.getIsOutOfRangeDelivery();
                if ((Objects.nonNull(autoGetTrackNo) && Boolean.TRUE.equals(autoGetTrackNo))
                        || (Boolean.FALSE.equals(isOutOfRangeDelivery) && Objects.nonNull(autoGetTrackNotOfRangeDelivery) && Boolean.TRUE.equals(autoGetTrackNotOfRangeDelivery))) {
                    soB2cRuleService.handleAutoSubmitDelivery(id, logisticsRuleResult.getName());
                }
            }
        }
        //自动计算预估运费到订单的预估运费字段
        soB2cService.autoCalcEstimatedShippingCost(Collections.singletonList(id));
        return isMatch;
    }

    /**
     * 拦截打标识，冻结订单
     *
     * @param interceptUpdateOrderDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/17 18:54
     **/
    @Override
    public Boolean updateIntercept(SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO) {
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(CharSequenceUtil.format("用户【{}】更新", UserContext.getDefaultLoginUser().getUserName()));
        if (interceptUpdateOrderDTO.getIsFrozen() != null) {
            msgSb.append(CharSequenceUtil.format(" 冻结状态为{}，", interceptUpdateOrderDTO.getIsFrozen() ? "已冻结" : "未冻结"));
        }
        if (interceptUpdateOrderDTO.getIsIntercept() != null) {
            msgSb.append(CharSequenceUtil.format(" 拦截状态为{}，", interceptUpdateOrderDTO.getIsIntercept() ? "打标拦截" : "取消拦截标记"));
        }
        if (StringUtils.isNotBlank(interceptUpdateOrderDTO.getApproveStatus())) {
            msgSb.append(CharSequenceUtil.format(" 审核状态为{}，", EnumMessage.getNameByCode(ApproveStatusEnum.class, interceptUpdateOrderDTO.getApproveStatus())));
        }
        if (StringUtils.isNotBlank(interceptUpdateOrderDTO.getBillStatus())) {
            msgSb.append(CharSequenceUtil.format(" 单据状态为{}，", EnumMessage.getNameByCode(SoB2cBillStatusEnum.class, interceptUpdateOrderDTO.getBillStatus())));
        }
        if (Objects.nonNull(interceptUpdateOrderDTO.getInvalidStatus())) {
            msgSb.append(CharSequenceUtil.format(" 作废状态为{}，", interceptUpdateOrderDTO.getInvalidStatus() ? "已作废" : "未作废"));
        }
        //msgSb去掉最后一个字符
        msgSb.deleteCharAt(msgSb.length() - 1);
        List<Pair<String, String>> pairList = new ArrayList<>();
        interceptUpdateOrderDTO.getIds().forEach(v -> {
            Pair<String, String> pair = new Pair<>(v, "");
            pairList.add(pair);
        });
        operateLogService.batchAddModuleOperateLog(msgSb.toString(), ModuleTypeEnum.SO_B2C.getCode(), pairList, "状态变更");
        return lambdaUpdate().set(Objects.nonNull(interceptUpdateOrderDTO.getIsIntercept()), SoB2cEntity::getIsIntercept, interceptUpdateOrderDTO.getIsIntercept())
                .set(Objects.nonNull(interceptUpdateOrderDTO.getIsFrozen()), SoB2cEntity::getIsFrozen, interceptUpdateOrderDTO.getIsFrozen())
                .set(Objects.nonNull(interceptUpdateOrderDTO.getIsFrozen()) && interceptUpdateOrderDTO.getIsFrozen(), SoB2cEntity::getFrozenType, SoB2cFrozenTypeEnum.ENUM_AUTOMATIC.getCode())
                .set(StringUtils.isNotBlank(interceptUpdateOrderDTO.getApproveStatus()), SoB2cEntity::getApproveStatus, interceptUpdateOrderDTO.getApproveStatus())
                .set(StringUtils.isNotBlank(interceptUpdateOrderDTO.getBillStatus()), SoB2cEntity::getBillStatus, interceptUpdateOrderDTO.getBillStatus())
                .set(StringUtils.isNotBlank(interceptUpdateOrderDTO.getAbnormalType()), SoB2cEntity::getAbnormalType, interceptUpdateOrderDTO.getAbnormalType())
                .set(StringUtils.isNotBlank(interceptUpdateOrderDTO.getRemark()), SoB2cEntity::getRemark, interceptUpdateOrderDTO.getRemark())
                .set(Objects.nonNull(interceptUpdateOrderDTO.getInvalidStatus()), SoB2cEntity::getInvalidStatus, interceptUpdateOrderDTO.getInvalidStatus())
                .set(Objects.nonNull(interceptUpdateOrderDTO.getInvalidStatus()) && interceptUpdateOrderDTO.getInvalidStatus(), SoB2cEntity::getInvalidRemark, "平台订单取消,拦截成功自动作废")
                .in(SoB2cEntity::getId, interceptUpdateOrderDTO.getIds())
                .update();
    }

    @Override
    public Boolean updateAbnormalType(String id, String soB2cAbnormalType) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalType)
                .update();
    }

    @Override
    public List<TransferDeclareDetailDTO.AddDTO> listByLogisticsSupplier(String deliveryLogisticsSupplierId) {
        List<LogisticsChannelDTO.ListSelectDTO> listSelectDTOS = logisticsFeign.listLogisticsChannel(Arrays.asList(deliveryLogisticsSupplierId));
        List<String> channelIds = listSelectDTOS.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(channelIds)) {
            return Collections.emptyList();
        }
        List<TransferDeclareDetailDTO.AddDTO> addDTOList = baseMapper.listByLogisticsSupplier(channelIds);
        return addDTOList;
    }

    @Override
    public List<TransferDeclareDTO.AddDTO> generateTransferDeclareView(List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList) {
        List<TransferDeclareDTO.AddDTO> tdAddList = new ArrayList<>();
        for (TransferDeclareGenerationSettingDTO.ViewDTO viewDTO : viewDTOList) {
            List<String> deliveryLogisticsSupplierIdList = viewDTO.getDeliveryLogisticsSupplierIdList();
            for (String deliveryLogisticsSupplierId : deliveryLogisticsSupplierIdList) {
                TransferDeclareDTO.AddDTO tdAdd = new TransferDeclareDTO.AddDTO();
                tdAdd.setDeliveryLogisticsSupplierId(deliveryLogisticsSupplierId);
                tdAdd.setTransferLogisticsSupplierId(viewDTO.getTransferLogisticsSupplierId());
                tdAdd.setTransferChannelId(viewDTO.getTransferChannelId());
                List<TransferDeclareDetailDTO.AddDTO> detailList = this.listByLogisticsSupplier(deliveryLogisticsSupplierId);
                if (CollectionUtils.isEmpty(detailList)) {
                    continue;
                }
                tdAdd.setDetailList(detailList);
                tdAddList.add(tdAdd);
            }
        }
        return tdAddList;
    }

    @Override
    public Boolean updateTransferStatusBatch(List<String> soIds, String status) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Boolean.FALSE;
        }

        return this.lambdaUpdate()
                .set(SoB2cEntity::getTransferStatus, status)
                .in(SoB2cEntity::getId, soIds)
                .update(new SoB2cEntity());
    }

    @Override
    public PagingVO<PackageDTO.PagingViewDTO> packagePing(PagingDTO<PackageDTO.PagingParamDTO> dto) {
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        PackageDTO.PagingParamDTO pagingParam = dto.getParams();
        pagingParam.setBillStatusList(Arrays.asList(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode()));
        List<String> packageStatusList = new ArrayList<>(2);
        packageStatusList.add(PackageStatusEnum.NOT.getCode());
        packageStatusList.add(PackageStatusEnum.WAIT.getCode());
        pagingParam.setPackageStatusList(packageStatusList);
        IPage<PackageDTO.PagingViewDTO> pageData = this.baseMapper.packagePing(query, pagingParam);
        List<PackageDTO.PagingViewDTO> list = pageData.getRecords();
        List<String> logisticsChannelIdList = list.stream().map(PackageDTO.PagingViewDTO::getLogisticsChannelId).collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> baseList = CollectionUtils.isNotEmpty(logisticsChannelIdList) ? logisticsFeign.listChannelInfoById(logisticsChannelIdList) : Collections.emptyList();

        //查询对应的中转服务商
        List<String> transferLogisticsSupplierId = list.stream().map(req -> req.getTransferLogisticsSupplierId()).collect(Collectors.toList());
        List<TransferLogisticsSupplierEntity> transferLogisticsSupplierEntities = transferLogisticsFeign.listLogisticsSupplierByIds(transferLogisticsSupplierId);

        //查询中转服务商对应的渠道
        List<String> transferLogisticsChannelIds = list.stream().map(req -> req.getTransferLogisticsChannelId()).distinct().collect(Collectors.toList());
        List<TransferLogisticsChannelDTO.ListSelectDTO> logisticsChannelEntityList = transferLogisticsFeign.listByTransferChannelIds(transferLogisticsChannelIds);
        //wms配置
        CfgSettingEntity entity = cfgSettingFeign.getByKey(CfgSettingEnum.PACKAGE_SETTING.getCode());
        for (PackageDTO.PagingViewDTO item : list) {
            String logisticsChannelId = item.getLogisticsChannelId();
            LogisticsChannelDTO.BaseDTO base = baseList.stream().filter(b -> b.getId().equals(logisticsChannelId)).
                    findFirst().orElse(null);
            if (Objects.nonNull(base)) {
                item.setLogisticsChannelName(base.getName());
                item.setLogisticsSupplierId(base.getLogisticsSupplierId());
                item.setLogisticsSupplierShortName(base.getLogisticsSupplierShortName());
                item.setLogisticsSupplierName(base.getLogisticsSupplierName());
            }

            //查询对应的中转服务商
            TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierEntities.stream().filter(req -> item.getTransferLogisticsSupplierId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                item.setTransferLogisticsSupplierName(supplierEntity.getSupplierName());
            }

            //查询中转服务商对应的渠道
            TransferLogisticsChannelDTO.ListSelectDTO transferLogisticsChannelEntity = logisticsChannelEntityList.stream().filter(req -> item.getTransferLogisticsChannelId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(transferLogisticsChannelEntity)) {
                item.setTransferLogisticsChannelName(transferLogisticsChannelEntity.getName());
            }
            //判断是否是组包限制的发货物流商
            Boolean isPackageSupplier = getPackageSupplierSetting(item.getLogisticsSupplierId(), entity);
            item.setIsPackageSupplier(isPackageSupplier);
            item.setUniqueId(getPackageUniqueId(isPackageSupplier, item.getLogisticsSupplierId(), item.getTransferLogisticsChannelId(), item.getTransferLogisticsSupplierId(), item.getShopId()));
        }

        return new PagingVO(pageData);
    }

    public Boolean getPackageSupplierSetting(String logisticsSupplierId, CfgSettingEntity entity) {
        if (StringUtils.isBlank(logisticsSupplierId)) {
            return Boolean.FALSE;
        }
        if (ObjectUtil.isNotEmpty(entity) && ObjectUtil.isNotEmpty(entity.getDataJson())) {
            CfgSettingValueDTO.PackageSettingDTO dto = BeanUtil.toBean(entity.getDataJson(), CfgSettingValueDTO.PackageSettingDTO.class);
            if (CollectionUtils.isNotEmpty(dto.getSupplierIds()) && dto.getSupplierIds().contains(logisticsSupplierId)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private String getPackageUniqueId(Boolean isPackageSupplier, String logisticsSupplierId, String transferLogisticsChannelId, String transferLogisticsSupplierId, String shopId) {
        if (isPackageSupplier) {
            return logisticsSupplierId + "-" + transferLogisticsChannelId + "-" + transferLogisticsSupplierId + "-" + shopId;
        } else {
            return logisticsSupplierId + "-" + transferLogisticsChannelId + "-" + transferLogisticsSupplierId;
        }
    }

    @Override
    public BatchResultDTO checkLength(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(id);
        //存在的物流渠道
        String existChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        //筛选订单物流渠道 相同订单不能存在多个渠道
        List<String> channelIds = dto.getDetailList().stream().filter(e -> StringUtils.isNotBlank(e.getLogisticsChannelId())
                && id.equals(e.getId())).map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getLogisticsChannelId)
                .distinct().collect(Collectors.toList());
        /**
         * 是否覆盖
         * 是：按照新选择的物流渠道和仓库下推配货中；如果物流方式跟订单已有的物流不一致，清空物流单号信息，且更新明细仓库
         * 否：新选择的物流渠道和仓库只添加到物流方式和仓库为空的订单，已存在物流方式和仓库的订单不做更改
         */
        Boolean isCover = dto.getIsCover();
        String logisticsChannelId = "";
        if (CollectionUtils.isNotEmpty(channelIds)) {
            logisticsChannelId = channelIds.get(0);
        }
        //选择了渠道则更新
        if (Boolean.TRUE.equals(isCover)) {
            soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
        } else {
            //当为空就覆盖
            if (StringUtils.isBlank(existChannelId)) {
                soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
            }
        }
        String country = Objects.nonNull(soB2cReceiverEntity) ? soB2cReceiverEntity.getCountry() : "";
        if (country == null) {
            country = "";
        }
        //渠道不存在直接返回，不进行校验
        if (StrUtil.isEmpty(soB2cLogisticsEntity.getLogisticsChannelId())) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "渠道不存在，不进行尺寸校验");
        }
        LogisticsChannelDTO.LogisticsChannelConstraintDTO channelConstraintDTO = logisticsFeign.getLogisticsChannelConstraint(soB2cLogisticsEntity.getLogisticsChannelId(), country);
        BigDecimal maxWeight = channelConstraintDTO.getMaxWeight();
        if (channelConstraintDTO.getWeightUnit().equals("kg")) {
            maxWeight = maxWeight.multiply(BigDecimal.valueOf(1000));
        }
        //默认为cm
        BigDecimal maxLength = channelConstraintDTO.getMaxLength();
        if (channelConstraintDTO.getSizeUnit().equals("m")) {
            maxLength = maxLength.multiply(BigDecimal.valueOf(100));
        }
        BigDecimal maxWidth = channelConstraintDTO.getMaxWidth();
        if (channelConstraintDTO.getSizeUnit().equals("m")) {
            maxWidth = maxWidth.multiply(BigDecimal.valueOf(100));
        }
        BigDecimal maxHeight = channelConstraintDTO.getMaxHeight();
        if (channelConstraintDTO.getSizeUnit().equals("m")) {
            maxHeight = maxHeight.multiply(BigDecimal.valueOf(100));
        }
        //所有都清空就是初始值，其实就不用校验了。
        if (maxHeight.compareTo(BigDecimal.ZERO) == 0 && maxWidth.compareTo(BigDecimal.ZERO) == 0 && maxLength.compareTo(BigDecimal.ZERO) == 0 && maxWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "校验物流尺寸重量成功");
        }
        boolean result = true;
        String msg = "";
        if (soB2cLogisticsEntity.getLength().compareTo(maxLength) > 0 ||
                soB2cLogisticsEntity.getWidth().compareTo(maxWidth) > 0 ||
                soB2cLogisticsEntity.getHeight().compareTo(maxHeight) > 0) {
            if (maxHeight.compareTo(BigDecimal.ZERO) != 0 || maxWidth.compareTo(BigDecimal.ZERO) != 0 || maxLength.compareTo(BigDecimal.ZERO) != 0) {
                String orderDesc = String.format("长【%scm】*宽【%scm】*高【%scm】", soB2cLogisticsEntity.getLength(), soB2cLogisticsEntity.getWidth(), soB2cLogisticsEntity.getHeight());
                String logisticsDesc = String.format("长【%scm】*宽【%scm】*高【%scm】", maxLength, maxWidth, maxHeight);
                result = false;
                msg = msg + String.format("包装尺寸为%s，超出渠道配置尺寸%s", orderDesc, logisticsDesc);
            }
        }
        if (soB2cLogisticsEntity.getWeight().compareTo(maxWeight) > 0) {
            if (maxWeight.compareTo(BigDecimal.ZERO) != 0) {
                result = false;
                if (CharSequenceUtil.isNotBlank(msg)) {
                    msg = msg + " 。 ";
                }
                msg = msg + String.format("重量为【%s】g，超出渠道配置【%s】g", soB2cLogisticsEntity.getWeight(), maxWeight);
            }
        }

        if (result) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "校验物流尺寸重量成功");
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), msg);
        }
    }

    /**
     * 检查销售订单产品是否需要备案 且已备案
     *
     * @param id                 销售订单id
     * @param logisticsChannelId 物流渠道id
     */
    @Override
    public void checkProductRegistrationAndUpdate(String id, String logisticsChannelId) {
        SettingForecastDTO.CheckRegistrationResultDTO resultDTO = getCheckRegistrationResult(id, logisticsChannelId);
        String packageStatus = resultDTO.getPackageStatus();
        String transferStatus = resultDTO.getTransferStatus();
        //未备案的sku
        List<String> notRegistrationSkuNoList = resultDTO.getNotRegistrationSkuNoList();
        Boolean isRegistration = CollectionUtils.isEmpty(notRegistrationSkuNoList);
        soB2cService.updatePackageAndTransferStatus(id, packageStatus, transferStatus, isRegistration, true);
        //表示未备案
        if (!isRegistration) {
            String skuStr = String.join(",", notRegistrationSkuNoList);
            throw new ServiceException(ApiError.NOT_PRODUCT_REGISTRATION, skuStr, resultDTO.getDeclarePlatformName());
        }
    }

    /**
     * 修改组包和中转状态
     *
     * @param soId
     * @param packageStatus
     * @param transferStatus
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePackageAndTransferStatus(String soId, String packageStatus, String transferStatus, Boolean isRegistration, Boolean isUpdateTransferStatus) {
        if (isRegistration) {
            this.lambdaUpdate().set(SoB2cEntity::getPackageStatus, packageStatus).
                    set(isUpdateTransferStatus, SoB2cEntity::getTransferStatus, transferStatus).
                    set(SoB2cEntity::getAbnormalType, "").
                    set(SoB2cEntity::getIsMatchLogisticsRule, Boolean.TRUE).
                    eq(SoB2cEntity::getId, soId).update(new SoB2cEntity());
        }
        //未备案清除渠道
        if (!isRegistration) {
            soB2cLogisticsService.lambdaUpdate().
                    set(SoB2cLogisticsEntity::getLogisticsChannelId, "").
                    set(SoB2cLogisticsEntity::getLogisticsChannelName, "").
                    set(SoB2cLogisticsEntity::getCode, "").
                    set(SoB2cLogisticsEntity::getTrackNo, "").
                    eq(SoB2cLogisticsEntity::getMainId, soId).
                    update(new SoB2cLogisticsEntity());

            this.lambdaUpdate().eq(SoB2cEntity::getId, soId)
                    .set(SoB2cEntity::getAbnormalType, SoB2cAbnormalTypeEnum.PRODUCT_NOT_REGISTRATION.getCode())
                    .set(SoB2cEntity::getPackageStatus, PackageStatusEnum.NOT.getCode())
                    .set(SoB2cEntity::getIsMatchLogisticsRule, Boolean.FALSE)
                    .update(new SoB2cEntity());
        }

    }

    /**
     * 获取检查备案结果
     *
     * @param id
     * @param logisticsChannelId
     * @return
     */
    @Override
    public SettingForecastDTO.CheckRegistrationResultDTO getCheckRegistrationResult(String id, String logisticsChannelId) {
        SettingForecastDTO.CheckRegistrationResultDTO resultDTO = new SettingForecastDTO.CheckRegistrationResultDTO();
        SoB2cEntity soB2cEntity = this.getById(id);
        if (Objects.isNull(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (StringUtils.isBlank(logisticsChannelId)) {
            SoB2cLogisticsEntity soB2cLogistics = soB2cLogisticsService.getByMainId(id);
            logisticsChannelId = Objects.nonNull(soB2cLogistics) ? soB2cLogistics.getLogisticsChannelId() : "";
        }
        List<SplitSkuDTO> productList = this.getTransferDeclareProductBySoInfo(id);
        //渠道名称
        String logisticsChannelName = "";

        //不在备案列表的skuNo
        List<String> notRegistrationSkuNoList = new ArrayList<>();
        /**
         * 获取到对应的skuNo List  TODO 等接口
         */
        String packageStatus = PackageStatusEnum.NOT.getCode();
        String transferStatus = TransferStatusEnum.NOT.getCode();
        List<String> skuNoList = productList.stream().map(SplitSkuDTO::getSkuNo).distinct().collect(Collectors.toList());
        String declarePlatform = "";
        String declarePlatformName = "";
        if (StringUtils.isNotBlank(logisticsChannelId)) {
            SettingForecastDTO.FindSettingForecastDTO findSettingForecast = new SettingForecastDTO.FindSettingForecastDTO();
            findSettingForecast.setOrderTime(soB2cEntity.getCreateTime());
            findSettingForecast.setLogisticsChannelId(logisticsChannelId);
            SettingForecastDTO.ForecastStatusDTO forecastStatus = forecastFeign.getByLogisticsChannelId(findSettingForecast);
            if (Objects.nonNull(forecastStatus)) {
                declarePlatform = forecastStatus.getDeclarePlatform();
                declarePlatformName = forecastStatus.getDeclarePlatformName();
                logisticsChannelName = forecastStatus.getLogisticsChannelName();
                //中转状态
                transferStatus = forecastStatus.getTransferStatus();
                packageStatus = forecastStatus.getPackageStatus();
                //表示要中转
                if (!TransferStatusEnum.NOT.getCode().equals(transferStatus)) {
                    //检查是否备案
                    notRegistrationSkuNoList = listNotSkuRegistration(skuNoList, forecastStatus.getDeclarePlatform());
                }
            }
        }
        resultDTO.setDeclarePlatform(declarePlatform);
        resultDTO.setDeclarePlatformName(declarePlatformName);
        resultDTO.setLogisticsChannelName(logisticsChannelName);
        resultDTO.setNotRegistrationSkuNoList(notRegistrationSkuNoList);
        resultDTO.setPackageStatus(packageStatus);
        resultDTO.setTransferStatus(transferStatus);
        return resultDTO;
    }

    @Override
    public Boolean updateShippingOrderNo(List<TransferDeclareDTO.ShippingOrderDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        TransferDeclareDTO.ShippingOrderDTO dto = list.stream().filter(e -> StringUtils.isNotBlank(e.getType())).findFirst().orElse(null);
        if (Objects.isNull(dto) || StringUtils.isBlank(dto.getType())) {
            //数据类型必填，为空时直接返回
            return Boolean.TRUE;
        }
        List<String> soIds = list.stream().map(TransferDeclareDTO.ShippingOrderDTO::getSoId).collect(Collectors.toList());
        List<SoB2cErrorEntity> errorList = soB2cErrorService.getByMainIdsAndType(soIds, dto.getType());
        List<String> deleteErrorIds = new ArrayList<>();
        List<SoB2cErrorEntity> addOrUpdateErrors = new ArrayList<>();

        list.forEach(shippingOrderDTO -> {
            //成功还是失败
            if (StringUtils.isBlank(shippingOrderDTO.getSign())) {
                deleteErrorIds.add(shippingOrderDTO.getSoId());
            } else {
                SoB2cErrorEntity error = errorList.stream().filter(e -> e.getMainId().equals(shippingOrderDTO.getSoId())).findFirst().orElse(new SoB2cErrorEntity());
                error.setMainId(shippingOrderDTO.getSoId())
                        .setType(shippingOrderDTO.getType())
                        .setMessage(shippingOrderDTO.getMessage())
                        .setParamJson(shippingOrderDTO.getSoId());
                addOrUpdateErrors.add(error);
            }
        });
        //更新或添加记录
        if (CollectionUtils.isNotEmpty(addOrUpdateErrors)) {
            soB2cErrorService.saveOrUpdateBatch(addOrUpdateErrors);
        }
        //删除失败记录
        if (CollectionUtils.isNotEmpty(deleteErrorIds)) {
            soB2cErrorService.removeByIds(deleteErrorIds);
        }
        //批量更新订单信息
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(shippingOrderDTO -> {
                this.lambdaUpdate()
                        .set(StringUtils.isNotBlank(shippingOrderDTO.getShippingOrderNo()), SoB2cEntity::getShippingOrderNo, shippingOrderDTO.getShippingOrderNo())
                        .set(SoB2cEntity::getSignOrderError, shippingOrderDTO.getSign())
                        .eq(SoB2cEntity::getId, shippingOrderDTO.getSoId()).update();
            });
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateAliExpressOrderWarehouse(String soId, String shopId) {
        SoB2cEntity entity = this.getById(soId);
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpTaskFeign.getOutputTaskRecord(entity.getPlatformCode(),"DmpOutputAliExpressOrderRocketMQTaskHandler");
        if (CollectionUtils.isEmpty(dmpOutputTaskRecordEntityList)) {
            return Boolean.TRUE;
        }
        DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = dmpOutputTaskRecordEntityList.get(0);
        PlatformOrderDTO dto = JSONUtil.toBean(dmpOutputTaskRecordEntity.getRequestData(), PlatformOrderDTO.class);
        return SoB2cHandler.handleSoOutStock(dto, null, entity);
    }

    @Override
    public void batchRemoveSignError(List<String> mainIds, String type) {
        if (CollectionUtils.isEmpty(mainIds) || StringUtil.isEmpty(type)) {
            return;
        }
        this.lambdaUpdate().set(SoB2cEntity::getSignOrderError, "").
                in(SoB2cEntity::getId, mainIds).eq(SoB2cEntity::getSignOrderError, type).update();
    }

    @Override
    public Boolean updateShippingOrderNoBySoId(TransferDeclareDTO.ShippingOrderDTO shippingOrderDTO) {
        SoB2cEntity soB2cEntity = this.getById(shippingOrderDTO.getSoId());
        if (Objects.nonNull(soB2cEntity)) {
            this.lambdaUpdate()
                    .set(SoB2cEntity::getShippingOrderNo, shippingOrderDTO.getShippingOrderNo())
                    .set(soB2cEntity.getSignOrderError().equals(shippingOrderDTO.getSign()), SoB2cEntity::getSignOrderError, "")
                    .eq(SoB2cEntity::getId, shippingOrderDTO.getSoId()).update();
        }
        return Boolean.TRUE;
    }

    @Override
    public String checkSkuInventory(SoB2cDTO.AddDTO dto, List<SoB2cDetailDTO.AddDTO> resultDetailList) {
        StringBuffer errMsg = new StringBuffer("");

        //未录入仓库的无需校验库存
        List<SoB2cDetailDTO.AddDTO> detailList = resultDetailList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getWarehouseId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailList)) {
            return errMsg.toString();
        }

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : com.google.common.collect.Lists.newArrayList();

        Map<String, List<SoB2cDetailDTO.AddDTO>> multiTransferMap = detailList.stream().collect(
                Collectors.groupingBy(r -> r.getWarehouseId() + "-" + r.getSkuId(), Collectors.toList()));
        //仓库信息
        List<String> warehouseIds = detailList.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

        //产品信息
        List<String> skuIdList = detailList.stream().map(SoB2cDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIdList);

        //库存信息
        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode()));
        skuInventoryDTO.setWarehouseIdList(warehouseIds);
        skuInventoryDTO.setSkuIdList(skuIdList);
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);

        multiTransferMap.forEach((key, multiList) -> {
            String warehouseId = multiList.get(0).getWarehouseId();
            String skuId = multiList.get(0).getSkuId();
            //产品信息
            ProductDetailEntity productDetailEntity = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), skuId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            String skuNo = productDetailEntity.getSkuNo();
            // 合计销售数量
            int sumQty = multiList.stream().mapToInt(SoB2cDetailDTO.AddDTO::getQty).sum();
            log.warn("sku id【{}】库位【{}】 合计销售数量【{}】", warehouseId, skuId, "", sumQty);
            //即时库存
            Integer curInventoryQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(skuId)
                    && obj.getWarehouseId().equals(warehouseId)
                    && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                    .orElse(MathUtil.ZERO);

            log.warn("仓库id【{}】sku id【{}】库位【{}】 合计销售数量【{}】实时库存数量", warehouseId, skuId, "",
                    sumQty, curInventoryQty);
            Boolean isScarce = curInventoryQty < sumQty;
            if (isScarce && !ignoreInventorySkuIds.contains(skuId)) {
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), warehouseId)).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                String warehouseName = Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId()) ? warehouseDetail.getName() : "";
                String msg = CharSequenceUtil.format("仓库【{}】SKU【{}】【缺货：{}个】", warehouseName, skuNo, (sumQty - curInventoryQty));
                errMsg.append(msg).append("</br>");
            }
        });
        return errMsg.toString();
    }

    @Override
    public List<SoB2cEntity> getByPlatformCodeList(List<String> platformCodeList, String dictPlatform, String shopId, String sourceType) {
        if (CollectionUtils.isEmpty(platformCodeList)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(SoB2cEntity::getPlatformCode, platformCodeList)
                .eq(StringUtils.isNotBlank(shopId), SoB2cEntity::getShopId, shopId)
                .eq(SoB2cEntity::getDictPlatform, dictPlatform)
                .eq(StringUtils.isNotBlank(sourceType),SoB2cEntity::getSourceType, sourceType)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean checkAndFillBySoOutStock(PlatformSoOutStockDTO dto) {
        List<SoB2cEntity> soB2cEntityList = this.getByPlatformCodeList(Collections.singletonList(dto.getPlatformCode()), dto.getDictPlatform(), dto.getShopId(), SourceTypeEnum.SO_B2C.getCode());
        SoB2cEntity soB2cEntity = soB2cEntityList.stream().findFirst().orElse(null);
        if (null == soB2cEntity) {
            return false;
        }
        List<PlatformSoOutStockDetailDTO> detailList = dto.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            return false;
        }
        // 更新国家信息
        soB2cReceiverService.checkAndUpdateCountry(soB2cEntity.getId(), dto.getCountry());

        // 记录明细仓库
        List<SoB2cDetailEntity> detailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollectionUtils.isNotEmpty(detailEntityList)) {
            Map<String, PlatformSoOutStockDetailDTO> sourceDetailMap = dto.getDetailList().stream().collect(Collectors.toMap(PlatformSoOutStockDetailDTO::getPlatformOrderDetailId, Function.identity()));
            detailEntityList.forEach(e -> {
                PlatformSoOutStockDetailDTO detailDTO = sourceDetailMap.get(e.getSourceDetailId());
                if (null == detailDTO) {
                    return;
                }
                e.setWarehouseId(detailDTO.getWarehouseId());
                e.setWarehouseName(detailDTO.getWarehouseName());
                e.setWarehouseOrgId(detailDTO.getWarehouseOrgId());
                e.setWarehouseOrgName(detailDTO.getWarehouseOrgName());
            });
            soB2cDetailService.updateBatchById(detailEntityList);
        }

        LocalDateTime earliestDeliveryDateTime = detailList.stream()
                .map(e -> DateUtil.parseLocalDateTimeWithOffset(e.getPlatformDeliveryTime()))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        if (null == earliestDeliveryDateTime) {
            return true;
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(soB2cEntity.getId());
        if (null == logisticsEntity) {
            return true;
        }
        // 对比获取最早时间之前
        if (null != logisticsEntity.getDeliveryTime()) {
            // 取最早的时间
            if (!earliestDeliveryDateTime.isBefore(logisticsEntity.getDeliveryTime())) {
                return true;
            }
        }
        logisticsEntity.setDeliveryTime(earliestDeliveryDateTime);
        soB2cLogisticsService.updateById(logisticsEntity);
        return true;
    }

    /**
     * 同步订单到DMP
     */
    @Async("omsErpExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void syncOrderToDmp(String id, String syncOperate) {
        SoB2cDTO.ViewDTO view = this.view(id);
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id", id);
        resultMap.put("operate", syncOperate);
        resultMap.put("entity", view);

        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(view.getId());
        taskFeignDTO.setSourceCode(view.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_SO_B2C_ORDER_TO_DMP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.SO_B2C_TO_DMP_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc());
        taskFeignDTO.setSyncOperate(syncOperate);

        DmpPushTaskEntity dmpPushTaskEntity = dmpMqFeign.saveTask(taskFeignDTO);

        //推送DMP
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.delayLevel3SendTask(Collections.singletonList(dmpPushTaskEntity));
            }
        });
    }

    /**
     * 同步处理历史审核订单数据到订单表
     */
    @Override
    public void processOrderApproveData() {
        //获取已审核的销售订单(对于反审数据会清空审核记录)
        List<SoB2cEntity> list = lambdaQuery().select(SoB2cEntity::getId).eq(SoB2cEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus()).list();
        if (list.size() > 100) {
            List<List<SoB2cEntity>> partition = ListUtil.partition(list, 100);
            for (List<SoB2cEntity> entityList : partition) {
                buildOrderApproveData(entityList);
            }
        } else {
            buildOrderApproveData(list);
        }
    }

    @Override
    public List<SoB2cEntity> listByCreateTime(LocalDateTime startTime, LocalDateTime endTime) {
        List<SoB2cEntity> entityList = lambdaQuery()
                .gt(SoB2cEntity::getCreateTime, startTime)
                .lt(SoB2cEntity::getCreateTime, endTime)
                .eq(SoB2cEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                .eq(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .list();
        return entityList;
    }

    @Override
    public Boolean updateStatus(SoB2cEntity soB2cEntity) {
        boolean result = this.lambdaUpdate().eq(SoB2cEntity::getId, soB2cEntity.getId())
                .set(SoB2cEntity::getBillStatus, soB2cEntity.getBillStatus())
                .set(SoB2cEntity::getApproveStatus, soB2cEntity.getApproveStatus())
                .set(SoB2cEntity::getIsIntercept, soB2cEntity.getIsIntercept())
                .set(SoB2cEntity::getIsFrozen, soB2cEntity.getIsFrozen())
                .set(SoB2cEntity::getInvalidStatus, soB2cEntity.getInvalidStatus())
                .set(StringUtils.isNotBlank(soB2cEntity.getRemark()),SoB2cEntity::getRemark, soB2cEntity.getRemark())
                .set(StringUtils.isNotBlank(soB2cEntity.getInvalidRemark()),SoB2cEntity::getInvalidRemark, soB2cEntity.getInvalidRemark())
                .update();
        if (result) {
            String billStatusName = SoB2cBillStatusEnum.getName(soB2cEntity.getBillStatus());
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("单据状态变更为{},审核状态变更为{}", billStatusName, soB2cEntity.getApproveStatus().getName()), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "单据状态变更");
        }
        return result;
    }

    @Override
    public PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<SoB2cAbnormalDTO.ListDTO> page = this.baseMapper.abnormalExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        //数据赋值处理
        handleAbnormalList(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<SoB2cDTO.ExcelExportDTO> exportSoB2C(PagingDTO<SoB2cDTO.ExportParamDTO> dto) {
        Page<SoB2cDTO.ExcelExportDTO> page;
        List<SoB2cDTO.ExcelExportDTO> records;
        List<AdvanceQueryDTO> advanceQueryDTOList = dto.getParams().getAdvanceQueryDTOList();
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
        	dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        dto.getParams().setDynamicDataSource(dynamicDataSource);
        //是否缺货 过滤
        Boolean isOutStock = (Boolean) advanceQueryDTOList.stream().filter(v -> v.getField().equals("isOutStock")).findAny().orElse(new AdvanceQueryDTO()).getValue();
        try {
            if (Objects.nonNull(isOutStock)) {
                //必须选仓库而且只能选一个
                List<String> warehouseIdList = com.common.business.utils.CollectionUtils.convertStrClzToList(advanceQueryDTOList.stream().filter(v -> v.getField().equals("sb2cd.warehouse_id") && (v.getCompare().equals(QueryConditionEnum.EQ.getCompareCode()) || v.getCompare().equals(QueryConditionEnum.IN_LIST.getCompareCode()))).findFirst().orElse(new AdvanceQueryDTO()).getValue());
                if (warehouseIdList.size() != 1) {
                    throw new ServiceException("选择缺货条件必须选择仓库且只能选择一个仓库");
                }
                page = this.baseMapper.exportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), Boolean.TRUE);
                // 数据处理
                records = handleExport(page.getRecords(), dto.getParams().getExportType(), Boolean.TRUE);
                if (isOutStock) {
                    //缺货
                    records = records.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.getIsOutStock()) && obj.getIsOutStock()).collect(Collectors.toList());
                    ;
                } else {
                    //无缺货标识或不缺货
                    records = records.stream().filter(obj -> ObjectUtil.isEmpty(obj.getIsOutStock()) || !obj.getIsOutStock()).collect(Collectors.toList());
                }
            } else {
                page = this.baseMapper.exportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), null);
                // 数据处理
                records = handleExport(page.getRecords(), dto.getParams().getExportType(), Boolean.FALSE);
            }
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        return new PagingVO<>(records, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }


    @Override
    public List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoB2cDetail() {
        return null;
    }

    @Override
    public List<SoB2cDTO.GenerateSoB2cReturnViewDTO> generateSoB2cReturnView(List<String> ids) {
        List<SoB2cDTO.GenerateSoB2cReturnViewDTO> list = baseMapper.generateSoB2cReturnView(ids);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        if (list.stream().anyMatch(v -> !v.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode()))) {
            throw new ServiceException("只有已发货可以下推");
        }
        List<String> skuIds = list.stream().map(SoB2cDTO.GenerateSoB2cReturnViewDTO::getSkuId).filter(jodd.util.StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> soIds = list.stream().map(v -> v.getId()).collect(Collectors.toList());
        List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = soB2cReturnService.listDetailBySoIds(soIds);
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockFeign.listDetailBySoIds(soIds);
        for (SoB2cDTO.GenerateSoB2cReturnViewDTO generateSoB2cReturnViewDTO : list) {
            SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(generateSoB2cReturnViewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            generateSoB2cReturnViewDTO.setProductName(skuVO.getSkuName());
            List<SoOutstockDetailEntity> currentSoOutstockDetailEntityList = soOutstockDetailEntityList.stream().filter(v -> v.getSoDetailId().equals(generateSoB2cReturnViewDTO.getDetailId()) && v.getSkuId().equals(generateSoB2cReturnViewDTO.getSkuId())).collect(Collectors.toList());
            generateSoB2cReturnViewDTO.setOutQty(currentSoOutstockDetailEntityList.stream().map(v -> v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));
            List<SoB2cReturnDetailEntity> currentSoB2cReturnDetailEntityList = soB2cReturnDetailEntityList.stream().filter(v -> v.getSoId().equals(generateSoB2cReturnViewDTO.getId()) && v.getSkuId().equals(generateSoB2cReturnViewDTO.getSkuId())).collect(Collectors.toList());
            generateSoB2cReturnViewDTO.setAlreadyReturnQty(currentSoB2cReturnDetailEntityList.stream().map(v -> v.getReturnQty()).reduce(MathUtil.ZERO, Integer::sum));

        }

        return list;
    }

    @Override
    public Boolean generateSoB2cReturn(List<SoB2cDTO.GenerateSoB2cReturnViewDTO> list) {

        return soB2cReturnService.generateSoB2cReturnBySo(list);
    }

    @Override
    public List<SoB2cEntity> getByPlatformCode(String platformCode) {
        if (StringUtils.isBlank(platformCode)) {
            return new ArrayList<>();
        }
        return lambdaQuery().eq(SoB2cEntity::getPlatformCode, platformCode).list();
    }

    @Override
    public List<SoB2cDTO.ChangeDeliverySkuViewDTO> changeDeliverySkuView(List<String> ids) {
        List<SoB2cEntity> soB2cEntityList = listByIds(ids);
        //订单更换发货SKU操作只能在待提交和审核不通过状态操作
        List<SoB2cEntity> notChangeList = soB2cEntityList.stream().filter(e -> !(ApproveStatusEnum.WAIT_SUBMIT.equals(e.getApproveStatus()) || ApproveStatusEnum.REJECT.equals(e.getApproveStatus()))).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notChangeList)) {
            List<String> codeList = notChangeList.stream().map(SoB2cEntity::getCode).distinct().collect(Collectors.toList());
            String msg = getChangeSkuView(codeList);
            throw new ServiceException(ApiError.ERROR_92154, msg);
        }
        List<SoB2cDTO.ChangeDeliverySkuViewDTO> changeDeliverySkuViewDTOS = baseMapper.listChangeDeliverySkuView(ids);
        List<String> skuIds = changeDeliverySkuViewDTOS.stream().map(SoB2cDTO.ChangeDeliverySkuViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        changeDeliverySkuViewDTOS.forEach(e -> {
            if (StrUtil.isNotBlank(e.getSkuId())) {
                SkuVO skuVO = skuVOList.stream().filter(f -> Objects.equals(f.getSkuId(), e.getSkuId())).findFirst().orElse(new SkuVO());
                e.setProductName(skuVO.getSkuName());
                e.setSpuName(skuVO.getSpuName());
                e.setSpuNo(skuVO.getSpuNo());
            }
        });
        return changeDeliverySkuViewDTOS;
    }

    private String getChangeSkuView(List<String> codeList) {
        List<List<String>> partition = ListUtil.partition(codeList, 5);
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> stringList : partition) {
            if (CharSequenceUtil.isNotBlank(stringBuilder)) {
                stringBuilder.append(",</br>");
            }
            stringBuilder.append(String.join(",", stringList));
        }
        return stringBuilder.toString();
    }

    @Override
    public void updateIsChangeSku(List<String> ids, Boolean isChangeSku) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        this.lambdaUpdate()
                .in(SoB2cEntity::getId, ids).ne(SoB2cEntity::getIsChangeSku, isChangeSku)
                .set(SoB2cEntity::getIsChangeSku, isChangeSku).update();
    }

    @Override
    public SoB2cDTO.SoB2cDataDTO listSoB2cData(SoB2cDTO.SoB2cDataParamDTO paramDTO) {
        //校验必填
        if (CollectionUtils.isEmpty(paramDTO.getB2cSoIdList()) && CollectionUtils.isEmpty(paramDTO.getB2cSoCodeList())) {
            throw new ServiceException("销售订单id或编码不能为空");
        }

        SoB2cDTO.SoB2cDataDTO resultDTO = new SoB2cDTO.SoB2cDataDTO();
        //数据类型
        List<String> dataTypeList = paramDTO.getDataTypeList();
        //数据id集合
        List<String> b2cSoIdList = paramDTO.getB2cSoIdList();
        //主表信息
        List<SoB2cEntity> list = new ArrayList<>();
        //只有编号，没有id
        if (CollectionUtils.isEmpty(b2cSoIdList) && CollectionUtils.isNotEmpty(paramDTO.getB2cSoCodeList())) {
            list = this.listBySoCodeList(b2cSoIdList);
            b2cSoIdList = list.stream().map(SoB2cEntity::getId).collect(Collectors.toList());
        } else {
            list = this.listByIds(b2cSoIdList);
        }
        resultDTO.setList(list);

        //物流信息
        if (dataTypeList.contains(SoB2cDataTypeEnum.LOGISTIC.getCode())) {
            List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(b2cSoIdList);
            resultDTO.setLogisticsList(soB2cLogisticsList);
        }
        //买家信息
        if (dataTypeList.contains(SoB2cDataTypeEnum.RECEIVER.getCode())) {
            List<SoB2cReceiverEntity> soB2cReceiverlist = soB2cReceiverService.listByMainIds(b2cSoIdList);
            resultDTO.setReceiverList(soB2cReceiverlist);
        }
        return resultDTO;
    }

    /**
     * 根据销售订单编号集合查询
     *
     * @param soCodeList
     * @return List<SoB2cEntity>
     * @author will
     * @date 2024/7/1 11:37
     */
    private List<SoB2cEntity> listBySoCodeList(List<String> soCodeList) {
        if (CollectionUtils.isEmpty(soCodeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cEntity::getCode, soCodeList).list();
    }

    /**
     * 具体同步动作处理
     *
     * @param entityList
     */
    private void buildOrderApproveData(List<SoB2cEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        //根据订单获取审核记录
        List<String> soIds = entityList.stream().map(SoB2cEntity::getId).distinct().collect(Collectors.toList());
        List<OperateLogEntity> list = operateLogService.listLastLogBySoIds(soIds, "审核操作");
        //更新审核订单明细数据
        List<SoB2cEntity> updateList = new ArrayList<>();
        entityList.forEach(soB2cEntity -> {
            OperateLogEntity operateLogEntity = list.stream().filter(e -> e.getBusinessId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (Objects.nonNull(operateLogEntity)) {
                soB2cEntity.setApproveTime(operateLogEntity.getCreateTime());
                soB2cEntity.setApproveUserId(operateLogEntity.getCreateUserId());
                soB2cEntity.setApproveUserName(operateLogEntity.getCreateUserName());
                updateList.add(soB2cEntity);
            }
        });
        if (CollectionUtils.isNotEmpty(updateList)) {
            baseMapper.updateBatchApproveById(updateList);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO skuMappingBatch(String soId) {
        SoB2cEntity entity = this.getById(soId);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (!SourceTypeEnum.SO_B2C.getCode().equalsIgnoreCase(entity.getSourceType())) {
            // 非平台来源的B2C销售订单不可修改映射关系
            throw new ServiceException(ApiError.IS_NOT_B2C_NOT_UPDATE_MAPPING);
        }

        if (TransferStatusEnum.SUCCESS.getCode().equals(entity.getTransferStatus()) || SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(entity.getBillStatus()) || SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(entity.getBillStatus())) {
            if (!entity.hasPlatformWarehouseOrder()) {
                throw new ServiceException("订单预报状态为预报成功或订单发货状态待发货&已发货 不允许更新映射");
            }
        }
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            // 未找到B2C销售订单明细信息
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        detailList = detailList.stream().filter(v -> StringUtils.isBlank(v.getSplitDetailId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailList)) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新成功！");
        }
        // 检查
        skuMappingCheck(entity, detailList);

        List<String> platformSkuList = detailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).collect(Collectors.toList());
        // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        List<String> platformSpuList = new LinkedList<>();
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(entity.getDictPlatform())
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(entity.getDictPlatform())
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(entity.getDictPlatform())
                || PlatformDictEnum.SHOPEE.getCode().equalsIgnoreCase(entity.getDictPlatform())
                || PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(entity.getDictPlatform())
                || PlatformDictEnum.TE_MU.getCode().equalsIgnoreCase(entity.getDictPlatform())
                || PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(entity.getDictPlatform())) {
            platformSpuList = detailList.stream()
                    .map(SoB2cDetailEntity::getPlatformSpuNo)
                    .distinct()
                    .collect(Collectors.toList());
        }
        //根据平台sku查询Listing信息
        ListingInfoParamDTO paramDTO = skuMappingService.constructDto(platformSkuList,
                platformSpuList,
                entity.getDictPlatform(),
                Collections.singletonList(entity.getShopId()),
                entity.getPlatformOrderCreateTime(),
                false);
        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);

        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = listDto.stream()
                .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuNo));

        List<String> skuIds = listingInfoWithSkuMappingDTOMap.values().stream()
                .flatMap(List::stream)
                .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuInfoSimpleVO> skuList = new ArrayList<>();
        Map<String, SkuInfoSimpleVO> skuMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            skuList = plmTaskFeign.getSimpleSkuInfoByIds(skuIds);
            skuMap = skuList.stream().collect(Collectors.toMap(SkuInfoSimpleVO::getSkuId, Function.identity()));
        }

        // 设置其他处理
        soB2cDetailService.consumerHandleDetailList(detailList, entity, skuList);
        Boolean needRecalSize = Boolean.FALSE;//是否重算尺寸

        List<SoB2cDetailEntity> updateSoB2cDetailEntityList = new ArrayList<>();
        for (SoB2cDetailEntity detailEntity : detailList) {
            SoB2cDetailEntity old = new SoB2cDetailEntity();
            BeanMapper.copy(detailEntity, old);
            List<ListingInfoWithSkuMappingDTO> mappingDTOList;
            // 映射关系
            if(PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(entity.getDictPlatform())
                    || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(entity.getDictPlatform())){
                mappingDTOList = listDto.stream().filter(v->v.getPlatformSpuNo().equals(detailEntity.getPlatformSpuNo())).collect(Collectors.toList());
            }else{
                mappingDTOList = listingInfoWithSkuMappingDTOMap.get(detailEntity.getPlatformSkuNo());
            }
            // 检查和获取映射关系
            ListingInfoWithSkuMappingDTO skuDTO = skuMappingService.checkAndMappingDTO(mappingDTOList, detailEntity.getPlatformSpuNo(), entity.getDictPlatform(),detailEntity.getPlatformSkuNo());

            //校验对照表是否有对照关系
            if (ObjectUtil.isNotEmpty(skuDTO)) {
                if (StringUtils.isNotBlank(skuDTO.checkAndGetProductSkuId()) && !Objects.equals(skuDTO.checkAndGetProductSkuId(), detailEntity.getSkuId())) {
                    needRecalSize = Boolean.TRUE;
                }
                detailEntity.setSkuNo(skuDTO.checkAndGetProductSkuNo());
                detailEntity.setSkuId(skuDTO.checkAndGetProductSkuId());
                detailEntity.setImageUrl(skuDTO.checkAndGetProductImageUrl());
                SkuInfoSimpleVO simpleVO = skuMap.get(skuDTO.getProductSkuId());
                detailEntity.setCurrentNetWeight(null == simpleVO ? BigDecimal.ZERO : simpleVO.getGrossWeight());

                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
                }
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.SO_B2C.getCode(), detailEntity.getMainId(), "", String.format("【%s】", old.getSkuNo()));

                soB2cDetailService.updateById(detailEntity);
                updateSoB2cDetailEntityList.add(detailEntity);
            } else {
                return BatchResultDTO.fail(detailEntity.getId(), detailEntity.getPlatformSkuNo(), "更新失败，无对照关系！");
            }
        }

//        if(CollUtil.isNotEmpty(updateSoB2cDetailEntityList)) {
//        	syncSoB2cService.syncDataToSdy(entity, updateSoB2cDetailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode());
//        }

        if (needRecalSize) {
            //长宽高计算
            BigDecimal maxLength = BigDecimal.ZERO;
            BigDecimal maxWidth = BigDecimal.ZERO;
            BigDecimal totalHeight = BigDecimal.ZERO;
            BigDecimal allNetWeight = BigDecimal.ZERO;
            if (CollectionUtils.isNotEmpty(skuList)) {
                //拆分明细
                List<SplitSkuDTO> splitSkuDTOS = splitBySoDetail(detailList, skuIds, entity.getCode(), false);
                //根据sku进行计算
                List<String> keyList = new ArrayList<>();
                keyList.add(CalculateSizeEnum.LENGTH.getCode());
                keyList.add(CalculateSizeEnum.WIDTH.getCode());
                keyList.add(CalculateSizeEnum.HEIGHT.getCode());
                List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
                Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
                maxLength = SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS, collect.get(CalculateSizeEnum.LENGTH.getCode()));
                maxWidth = SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS, collect.get(CalculateSizeEnum.WIDTH.getCode()));
                totalHeight = SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS, collect.get(CalculateSizeEnum.HEIGHT.getCode()));
                allNetWeight = SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode()));
            }

            //物流信息更新保存
            SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
            logisticsEntity.setWeight(allNetWeight);
            logisticsEntity.setLength(maxLength);
            logisticsEntity.setWidth(maxWidth);
            logisticsEntity.setHeight(totalHeight);
            soB2cLogisticsService.updateById(logisticsEntity);
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新成功！");
    }

    @Override
    public List<SoB2cEntity> listWithIsIntercept() {
        return lambdaQuery()
                .in(SoB2cEntity::getIsIntercept, Boolean.TRUE)
                .list();
    }

    @Override
    public Boolean exportExcel(SoB2cDTO.ExportParamDTO params) {
        downloadTaskFeign.saveDownloadTask("B2C销售订单", EXPORT_OMS_SO_B2C.getCode(), params);
        return Boolean.TRUE;
    }

    @Override
    @DistributeLocker(businessType = RedisKeyConstant.SO_B2C_ORDER_KEY, keyName = "dto.ids", waiteTime = 60)
    public List<BatchResultDTO> orderForecast(SoB2cDTO.TransferDeclareDTO dto) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<SoB2cEntity> soB2cEntityList = listByIds(dto.getIds());
        List<String> ids = soB2cEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(ids);
        List<SoB2cReceiverEntity> soReceiiveEntityList = soB2cReceiverService.listByMainIds(ids);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIds);
        List<SplitSkuDTO> allTransferDeclareProductDTOList = getTransferDeclareProductBySoIds(ids);
        List<SoB2cEntity> updateList = new ArrayList<>();
        List<String> deleteErrorIds = new ArrayList<>();
        List<SoB2cErrorEntity> errorList = soB2cErrorService.getByMainIdsAndType(ids, SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
        List<SoB2cErrorEntity> addOrUpdateErrors = new ArrayList<>();
        List<SoB2cLogisticsEntity> updateLogisticList = new ArrayList<>();
        List<TransferDeclareDTO.UpdateForcastStatusDTO> updateInstockForcastList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            TransferDeclareDTO.UpdateForcastStatusDTO updateForcastStatusDTO = new TransferDeclareDTO.UpdateForcastStatusDTO();
            updateForcastStatusDTO.setSoId(soB2cEntity.getId());

            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v -> v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cLogisticsEntity)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "未找到物流信息"));
                continue;
            }
            if (StringUtils.isBlank(soB2cLogisticsEntity.getLogisticsChannelId()) || StringUtils.isBlank(soB2cLogisticsEntity.getCode())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "仅可操作有物流渠道+物流单号的单据"));
                continue;
            }
            SoB2cReceiverEntity soB2cReceiverEntity = soReceiiveEntityList.stream().filter(v -> v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cReceiverEntity)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "未找到买家信息"));
                continue;
            }
            List<SplitSkuDTO> transferDeclareProductDTOList = allTransferDeclareProductDTOList.stream().filter(v -> v.getSoId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(transferDeclareProductDTOList)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "sku明细为空"));
                continue;
            }
            if (!ApproveStatusEnum.APPROVE.equals(soB2cEntity.getApproveStatus()) || (!TransferStatusEnum.WAIT.getCode().equals(soB2cEntity.getTransferStatus()) && !TransferStatusEnum.FAILURE.getCode().equals(soB2cEntity.getTransferStatus()))) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "仅可操作已审核+中转状态为待中转/预报失败的单据"));
                continue;
            }
            soB2cLogisticsEntity.setTransferLogisticsSupplierId(dto.getTransferLogisticsSupplierId());
            soB2cLogisticsEntity.setTransferLogisticsChannelId(dto.getTransferLogisticsChannelId());
            updateLogisticList.add(soB2cLogisticsEntity);

            ShopInfoEntity shopInfo = shopInfoEntityList.stream().filter(v -> v.getId().equals(soB2cEntity.getShopId())).findFirst().orElse(new ShopInfoEntity());
            TransferDeclareDTO.B2cOrderForecastDTO b2cOrderForecastDTO = TransferDeclareDTO.B2cOrderForecastDTO.builder()
                    .soB2cEntity(soB2cEntity)
                    .soB2cLogisticsEntity(soB2cLogisticsEntity)
                    .soB2cReceiverEntity(soB2cReceiverEntity)
                    .shopInfoEntity(shopInfo)
                    .transferDeclareProductDTOList(transferDeclareProductDTOList)
                    .build();
            TransferDeclareDTO.ShippingOrderDTO shippingOrderDTO = transferDeclareFeign.b2cOrderForecast(b2cOrderForecastDTO);
            SoB2cErrorEntity error = errorList.stream().filter(e -> e.getMainId().equals(shippingOrderDTO.getSoId())).findFirst().orElse(new SoB2cErrorEntity());
            //成功--更新销售订单 第三方单号，删除异常信息 ；失败--更新销售订单异常标识，新增异常信息
            if (shippingOrderDTO.getSuccess()) {
                soB2cEntity.setShippingOrderNo(shippingOrderDTO.getShippingOrderNo());
                soB2cEntity.setTransferStatus(TransferStatusEnum.SUCCESS.getCode());
                updateForcastStatusDTO.setStatus(TransferStatusEnum.SUCCESS.getCode());
                if (StringUtils.isNotBlank(error.getId())) {
                    //避免存在一个订单多个相同异常
                    List<String> errorIds = errorList.stream().filter(e -> e.getMainId().equals(shippingOrderDTO.getSoId())).map(SoB2cErrorEntity::getId).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(errorIds)){
                        deleteErrorIds.addAll(errorIds);
                    }
                }
                operateLogService.addModuleOperateLog("订单预报", ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "订单预报");
            } else {
                soB2cEntity.setTransferStatus(TransferStatusEnum.FAILURE.getCode());
                updateForcastStatusDTO.setStatus(TransferStatusEnum.FAILURE.getCode());
                if (StringUtils.isNotBlank(shippingOrderDTO.getSign())) {
                    soB2cEntity.setSignOrderError(shippingOrderDTO.getSign());
                    error.setMainId(shippingOrderDTO.getSoId())
                            .setType(shippingOrderDTO.getType())
                            .setMessage(shippingOrderDTO.getMessage())
                            .setCode(shippingOrderDTO.getCode())
                            .setParamJson(shippingOrderDTO.getSoId());
                    addOrUpdateErrors.add(error);
                }
                resultDTOList.add(BatchResultDTO.fail(shippingOrderDTO.getSoId(), shippingOrderDTO.getCode(), shippingOrderDTO.getMessage()));
            }
            updateList.add(soB2cEntity);
            updateInstockForcastList.add(updateForcastStatusDTO);
        }

        //更新入库预报单详情的上传状态
        transferDeclareFeign.updateTransferStatusByBatch(updateInstockForcastList);

        //更新操作同个事务
        soB2cService.orderForecastUpdateSoAndError(updateList, deleteErrorIds, addOrUpdateErrors, updateLogisticList);
        if(CollectionUtils.isNotEmpty(addOrUpdateErrors)) {
            List<String> errorIds = addOrUpdateErrors.stream().map(SoB2cErrorEntity::getId).collect(Collectors.toList());
            Map<String,Map<String, Object>> map = soB2cErrorService.handleMatchJson(errorIds);
            Map<String,RulePromptWordEntity> rulePromptWordEntityMap = soB2cErrorService.getRulePromptWord(map);
            addOrUpdateErrors.forEach(v->{
                BatchResultDTO batchResultDTO = resultDTOList.stream().filter(t->t.getId().equals(v.getMainId())).findFirst().orElse(null);
                if(Objects.isNull(batchResultDTO)){
                    return;
                }
                if(rulePromptWordEntityMap.containsKey(v.getId())){
                    RulePromptWordEntity rulePromptWordEntity = rulePromptWordEntityMap.get(v.getId());
                    batchResultDTO.setMsg(StrUtil.format("失败原因：{},【解决方案】：{}",CharSequenceUtil.isNotBlank(rulePromptWordEntity.getTips()) ? rulePromptWordEntity.getTips() : v.getMessage(),rulePromptWordEntity.getSolution()));
                }
            });
        }

        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NEVER)
    public List<BatchResultDTO> autoOrderForecast(List<String> soIdList) {
        List<SoB2cEntity> soB2cEntityList = listByIds(soIdList);
        soB2cEntityList = soB2cEntityList.stream().filter(v -> (TransferStatusEnum.FAILURE.getCode().equals(v.getTransferStatus()) || TransferStatusEnum.WAIT.getCode().equals(v.getTransferStatus())) &&
                SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equalsIgnoreCase(v.getBillStatus()) && !Boolean.TRUE.equals(v.getInvalidStatus())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            return new ArrayList<>();
        }
        soIdList = soB2cEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(soIdList);
        soB2cLogisticsEntityList = soB2cLogisticsEntityList.stream().filter(v -> StringUtils.isNotBlank(v.getCode()) && StringUtils.isNotBlank(v.getLogisticsChannelId())).collect(Collectors.toList());
        List<String> channelIds = soB2cLogisticsEntityList.stream().map(SoB2cLogisticsEntity::getLogisticsChannelId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(channelIds) || CollectionUtils.isEmpty(soB2cLogisticsEntityList)) {
            return new ArrayList<>();
        }
        List<LogisticsChannelDTO.BaseDTO> channelList = logisticsFeign.listChannelInfoById(channelIds);
        List<String> logisticSupplierIds = channelList.stream().map(v -> v.getMainId()).distinct().collect(Collectors.toList());
        //预报设置
        List<SettingForecastEntity> settingForecastEntityList = forecastFeign.getSettingForecastByLogisticsSupplierIdList(logisticSupplierIds);
        settingForecastEntityList = settingForecastEntityList.stream().filter(v -> v.getIsAutoForecast() && StringUtils.isNotBlank(v.getTransferLogisticsChannelId()) && StringUtils.isNotBlank(v.getTransferLogisticsSupplierId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(settingForecastEntityList)) {
            return new ArrayList<>();
        }
        //根据物流商分类
        List<SoB2cDTO.TransferDeclareDTO> transferDeclareDTOList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v -> v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
            LogisticsChannelDTO.BaseDTO channelDTO = channelList.stream().filter(v -> v.getId().equals(soB2cLogisticsEntity.getLogisticsChannelId())).findFirst().orElse(new LogisticsChannelDTO.BaseDTO());
            SettingForecastEntity settingForecastEntity = settingForecastEntityList.stream().filter(v -> v.getLogisticsSupplierId().equals(channelDTO.getMainId())).findFirst().orElse(null);
            if (Objects.isNull(settingForecastEntity)) {
                continue;
            }
            SoB2cDTO.TransferDeclareDTO existDTO = transferDeclareDTOList.stream().filter(v -> v.getTransferLogisticsSupplierId().equals(settingForecastEntity.getTransferLogisticsSupplierId()) && v.getTransferLogisticsChannelId().equals(settingForecastEntity.getTransferLogisticsChannelId())).findFirst().orElse(null);
            if (Objects.isNull(existDTO)) {
                existDTO = new SoB2cDTO.TransferDeclareDTO();
                List<String> ids = new ArrayList<>();
                ids.add(soB2cEntity.getId());
                existDTO.setIds(ids);
                existDTO.setTransferLogisticsSupplierId(settingForecastEntity.getTransferLogisticsSupplierId());
                existDTO.setTransferLogisticsChannelId(settingForecastEntity.getTransferLogisticsChannelId());
                transferDeclareDTOList.add(existDTO);
            } else {
                existDTO.getIds().add(soB2cEntity.getId());
            }
        }
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        transferDeclareDTOList.forEach(e ->{
            List<BatchResultDTO> batchResultDTOS = soB2cService.orderForecast(e);
            if (CollUtil.isNotEmpty(batchResultDTOS)){
                batchResultDTOList.addAll(batchResultDTOS);
            }
        });
        return batchResultDTOList;
    }

    @Override
    public List<BatchResultDTO> cancelOrderForecast(List<String> ids, Boolean checkPackageStatus) {
        List<SoB2cEntity> soB2cEntityList = listByIds(ids);
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(ids);
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<String> soCodeList = soB2cEntityList.stream().map(SoB2cEntity::getCode).distinct().collect(Collectors.toList());
        List<TransferDeclareDetailEntity> transferDeclareDetailEntityList = transferDeclareFeign.listBySoCodeList(soCodeList);
        List<SoB2cEntity> updateList = new ArrayList<>();
        List<String> deleteErrorIds = new ArrayList<>();
        List<SoB2cErrorEntity> errorList = soB2cErrorService.getByMainIdsAndType(ids, SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode());
        List<SoB2cErrorEntity> addOrUpdateErrors = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {

            if (!TransferStatusEnum.SUCCESS.getCode().equals(soB2cEntity.getTransferStatus())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "仅可操作预报成功订单的单据"));
                continue;
            }
            if (PackageStatusEnum.ALREADY.getCode().equals(soB2cEntity.getPackageStatus()) && checkPackageStatus) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "已组包不可操作"));
                continue;
            }
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "已出库不可操作"));
                continue;
            }
            TransferDeclareDetailEntity transferDeclareDetailEntity = transferDeclareDetailEntityList.stream().filter(v -> v.getSoCode().equals(soB2cEntity.getCode())).findFirst().orElse(null);
            if (Objects.nonNull(transferDeclareDetailEntity)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "已生成入库预报不可操作"));
                continue;
            }
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v -> v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cLogisticsEntity)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "物流单为空"));
                continue;
            }
            if (StringUtils.isBlank(soB2cEntity.getShippingOrderNo())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "第三方平台号为空"));
                continue;
            }
            if (StringUtils.isBlank(soB2cLogisticsEntity.getTransferLogisticsSupplierId())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "中转报关商为空"));
                continue;
            }

            SoB2cErrorEntity error = errorList.stream().filter(e -> e.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(new SoB2cErrorEntity());

            TransferDeclareDTO.CancelOrderForecastDTO cancelOrderForecastDTO = TransferDeclareDTO.CancelOrderForecastDTO.builder()
                    .transferLogisticsSupplierId(soB2cLogisticsEntity.getTransferLogisticsSupplierId())
                    .transferCancelOrderReq(TransferCancelOrderReq.builder()
                            .thirdPlatformCode(soB2cEntity.getShippingOrderNo())
                            .reason("平台发货异常")
                            .build())
                    .build();
            ApiResult<String> cancelResult = transferDeclareFeign.cancelOrderForecast(cancelOrderForecastDTO);

            if (cancelResult.isSuccess()) {
                soB2cEntity.setTransferStatus(TransferStatusEnum.WAIT.getCode());
                if (SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode().equals(soB2cEntity.getSignOrderError())) {
                    soB2cEntity.setSignOrderError("");
                }
                updateList.add(soB2cEntity);
                if (StringUtils.isNotBlank(error.getId())) {
                    deleteErrorIds.add(error.getId());
                }
                operateLogService.addModuleOperateLog("订单取消预报", ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "取消预报");
                resultDTOList.add(BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), "取消预报成功"));
            } else {
                error.setMainId(soB2cEntity.getId())
                        .setType(SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode())
                        .setMessage(cancelResult.getMsg())
                        .setParamJson(soB2cEntity.getId());
                addOrUpdateErrors.add(error);
                soB2cEntity.setSignOrderError(SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode());
                updateList.add(soB2cEntity);
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), cancelResult.getMsg()));
            }
        }

        //更新操作同个事务
        soB2cService.orderForecastUpdateSoAndError(updateList, deleteErrorIds, addOrUpdateErrors, new ArrayList<>());

        return resultDTOList;
    }

    @Override
    public List<BatchResultDTO> retryOrderForecast(List<String> orderIds) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(orderIds)) {
            return new ArrayList<>();
        }
        List<SoB2cEntity> soB2cEntityList = listByIds(orderIds);
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            return new ArrayList<>();
        }
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(orderIds);
        //根据物流商分类
        List<SoB2cDTO.TransferDeclareDTO> transferDeclareDTOList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v -> v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cLogisticsEntity)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "物流单为空"));
                continue;
            }
            if (StringUtils.isBlank(soB2cLogisticsEntity.getCode()) || StringUtils.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "物流运单号或物流渠道为空"));
                continue;
            }
            if (StringUtils.isBlank(soB2cLogisticsEntity.getTransferLogisticsSupplierId()) || StringUtils.isBlank(soB2cLogisticsEntity.getTransferLogisticsChannelId())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "中转报关商为空"));
                continue;
            }
            SoB2cDTO.TransferDeclareDTO existDTO = transferDeclareDTOList.stream().filter(v -> v.getTransferLogisticsSupplierId().equals(soB2cLogisticsEntity.getTransferLogisticsSupplierId()) && v.getTransferLogisticsChannelId().equals(soB2cLogisticsEntity.getTransferLogisticsChannelId())).findFirst().orElse(null);
            if (Objects.isNull(existDTO)) {
                existDTO = new SoB2cDTO.TransferDeclareDTO();
                List<String> ids = new ArrayList<>();
                ids.add(soB2cEntity.getId());
                existDTO.setIds(ids);
                existDTO.setTransferLogisticsSupplierId(soB2cLogisticsEntity.getTransferLogisticsSupplierId());
                existDTO.setTransferLogisticsChannelId(soB2cLogisticsEntity.getTransferLogisticsChannelId());
                transferDeclareDTOList.add(existDTO);
            } else {
                existDTO.getIds().add(soB2cEntity.getId());
            }
        }
        transferDeclareDTOList.forEach(v -> resultDTOList.addAll(soB2cService.orderForecast(v)));
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderForecastUpdateSoAndError(List<SoB2cEntity> updateB2cList, List<String> deleteErrorIds, List<SoB2cErrorEntity> addOrUpdateErrors, List<SoB2cLogisticsEntity> updateLogisticList) {
        //更新或添加异常记录
        if (CollectionUtils.isNotEmpty(addOrUpdateErrors)) {
            soB2cErrorService.saveOrUpdateBatch(addOrUpdateErrors);
        }
        //删除失败异常记录
        if (CollectionUtils.isNotEmpty(deleteErrorIds)) {
            soB2cErrorService.removeByIds(deleteErrorIds);
        }
        if (CollectionUtils.isNotEmpty(updateB2cList)) {
            updateB2cList.forEach(v -> {
                if (TransferStatusEnum.SUCCESS.getCode().equals(v.getTransferStatus())
                        && (SoB2cErrorTypeEnum.ORDER_FORECAST.getCode().equals(v.getSignOrderError()))) {
                    v.setSignOrderError("");
                }
            });
            this.updateBatchById(updateB2cList);
        }
        if (CollectionUtils.isNotEmpty(updateLogisticList)) {
            soB2cLogisticsService.updateTransferInfo(updateLogisticList);
        }
    }

    @Override
    public PackageDTO.ScanResultDTO packageScanByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return baseMapper.packageScanByCode(code);
    }

    /**
     * 根据销售订单ids 获取到合并的数据
     *
     * @param ids
     * @return
     */
    @Override
    public List<PackageDTO.ScanResultDTO> listMergePackageBySoIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.listMergePackageBySoIds(ids);
    }

    @Override
    public Boolean autoCancelOrderForecast(SoB2cEntity mainEntity) {
        if (!TransferStatusEnum.SUCCESS.getCode().equals(mainEntity.getTransferStatus())) {
            return false;
        }
        if (PackageStatusEnum.ALREADY.getCode().equals(mainEntity.getPackageStatus())) {
            return false;
        }
        if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(mainEntity.getBillStatus())) {
            return false;
        }
        List<TransferDeclareDetailEntity> transferDeclareDetailEntityList = transferDeclareFeign.listBySoCodeList(Arrays.asList(mainEntity.getCode()));
        TransferDeclareDetailEntity transferDeclareDetailEntity = transferDeclareDetailEntityList.stream().filter(v -> v.getSoCode().equals(mainEntity.getCode())).findFirst().orElse(null);
        if (Objects.nonNull(transferDeclareDetailEntity)) {
            return false;
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(mainEntity.getId());
        if (Objects.isNull(soB2cLogisticsEntity) || StringUtils.isBlank(soB2cLogisticsEntity.getTransferLogisticsSupplierId()) || StringUtils.isBlank(mainEntity.getShippingOrderNo())) {
            return false;
        }

        List<SoB2cEntity> updateList = new ArrayList<>();
        List<String> deleteErrorIds = new ArrayList<>();
        List<SoB2cErrorEntity> addOrUpdateErrors = new ArrayList<>();
        SoB2cErrorEntity error = soB2cErrorService.getByMainIdAndType(mainEntity.getId(), SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode());
        if (Objects.isNull(error)) {
            error = new SoB2cErrorEntity();
        }
        TransferDeclareDTO.UpdateForcastStatusDTO updateForcastStatusDTO = new TransferDeclareDTO.UpdateForcastStatusDTO();
        updateForcastStatusDTO.setSoId(mainEntity.getId());

        List<TransferDeclareDTO.UpdateForcastStatusDTO> updateInstockForcastList = new ArrayList<>();
        TransferDeclareDTO.CancelOrderForecastDTO cancelOrderForecastDTO = TransferDeclareDTO.CancelOrderForecastDTO.builder()
                .transferLogisticsSupplierId(soB2cLogisticsEntity.getTransferLogisticsSupplierId())
                .transferCancelOrderReq(TransferCancelOrderReq.builder()
                        .thirdPlatformCode(mainEntity.getShippingOrderNo())
                        .reason("平台发货异常")
                        .build())
                .build();
        ApiResult<String> cancelResult = transferDeclareFeign.cancelOrderForecast(cancelOrderForecastDTO);

        if (cancelResult.isSuccess()) {
            mainEntity.setTransferStatus(TransferStatusEnum.WAIT.getCode());
            if (SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode().equals(mainEntity.getSignOrderError())) {
                mainEntity.setSignOrderError("");
            }
            updateForcastStatusDTO.setStatus(TransferStatusEnum.WAIT.getCode());
            updateList.add(mainEntity);
            if (StringUtils.isNotBlank(error.getId())) {
                deleteErrorIds.add(error.getId());
            }
            updateInstockForcastList.add(updateForcastStatusDTO);
        } else {
            error.setMainId(mainEntity.getId())
                    .setType(SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode())
                    .setMessage(cancelResult.getMsg())
                    .setParamJson(mainEntity.getId());
            addOrUpdateErrors.add(error);
            mainEntity.setSignOrderError(SoB2cErrorTypeEnum.CANCEL_ORDER_FORECAST.getCode());
            updateList.add(mainEntity);
        }

        operateLogService.addModuleOperateLog("平台订单取消后自动取消订单预报", ModuleTypeEnum.SO_B2C.getCode(), mainEntity.getId(), "取消预报");
        //更新操作同个事务
        soB2cService.orderForecastUpdateSoAndError(updateList, deleteErrorIds, addOrUpdateErrors, new ArrayList<>());

        //更新入库预报单详情的上传状态
        transferDeclareFeign.updateTransferStatusByBatch(updateInstockForcastList);
        return cancelResult.isSuccess();
    }

    @Override
    public PagingVO<SoB2cAbnormalDTO.ListDTO> abnormalPaging(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> pagingParamDTO) {
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        IPage<SoB2cAbnormalDTO.ListDTO> list = baseMapper.abnormalPaging(query, pagingParamDTO.getParams());
        handleAbnormalList(list.getRecords());
        return new PagingVO(list);
    }

    @Override
    public Boolean abnormalExportExcel(SoB2cAbnormalDTO.PagingParamDTO params) {
        downloadTaskFeign.saveDownloadTask("B2C异常销售订单", EXPORT_OMS_SO_B2C_ABNORMAL.getCode(), params);
        return Boolean.TRUE;
    }

    /**
     * @param records
     * @description:
     * @author Will
     * @date: 2024/4/25 16:10
     */
    private void handleAbnormalList(List<SoB2cAbnormalDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //店铺
        List<String> shopIdList = records.stream().map(SoB2cAbnormalDTO.ListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoService.listByIds(shopIdList);

        List<String> needPromptErrorIds = records.stream().filter(v->SoB2cErrorTypeEnum.needPrompt(v.getErrorType())).map(SoB2cAbnormalDTO.ListDTO::getErrorId).collect(Collectors.toList());
        Map<String,Map<String, Object>> map =  soB2cErrorService.handleMatchJson(needPromptErrorIds);
        Map<String, RulePromptWordEntity> rulePromptWordEntityMap = soB2cErrorService.getRulePromptWord(map);
        for (SoB2cAbnormalDTO.ListDTO listDTO : records) {
            //审核状态
            listDTO.setApproveStatusName(ApproveStatusEnum.getName(listDTO.getApproveStatus()));
            //订单状态
            listDTO.setBillStatusName(SoB2cBillStatusEnum.getName(listDTO.getBillStatus()));
            //错误标识名称
            listDTO.setSignOrderErrorName(SoB2cErrorTypeEnum.getName(listDTO.getSignOrderError()));

            //店铺
            ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> obj.getId().equals(listDTO.getShopId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(shopInfoEntity)) {
                listDTO.setShopName(shopInfoEntity.getName());
            }
            if(Objects.nonNull(rulePromptWordEntityMap.get(listDTO.getErrorId()))){
                RulePromptWordEntity rulePromptWordEntity = rulePromptWordEntityMap.get(listDTO.getErrorId());
                listDTO.setFailureReason(CharSequenceUtil.isNotBlank(rulePromptWordEntity.getTips()) ? rulePromptWordEntity.getTips() : listDTO.getMessage());
                listDTO.setSolution(rulePromptWordEntity.getSolution());
            }
        }

    }

    /**
     * @param records
     * @description: 导出数据处理
     * @author Will
     * @date: 2024/4/16 18:42
     */
    private List<SoB2cDTO.ExcelExportDTO> handleExport(List<SoB2cDTO.ExcelExportDTO> records, String exportType, Boolean isOutStock) {
        //返回集合
        List<SoB2cDTO.ExcelExportDTO> resultList = new ArrayList<>();

        if (CollectionUtils.isEmpty(records)) {
            return resultList;
        }
        //skuId集合
        List<String> skuIdList = records.stream().map(SoB2cDTO.ExcelExportDTO::getSkuId).distinct().collect(Collectors.toList());
        Map<String, SkuVO> skuVOMap = new HashMap<>();
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIdList);
        if (!CollectionUtils.isEmpty(skuList)) {
            skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }
        //仓位信息
        List<String> warehouseIdList = records.stream().map(SoB2cDTO.ExcelExportDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = FeignQuery.create(WarehouseLocationEntity.class).in(WarehouseLocationEntity::getWarehouseId, warehouseIdList).select(WarehouseLocationEntity::getWarehouseId, WarehouseLocationEntity::getCode, WarehouseLocationEntity::getName).list();
        //bom信息
        List<BomChildrenSkuDTO> bomChildrenList;
        //库存信息
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = new ArrayList<>();
        //无需计算库存sku
        List<String> ignoreInventorySkuIds = new ArrayList<>();
        // 国家信息
        List<String> countryList = records.stream()
                .filter(e -> StringUtils.isNotBlank(e.getCountry()))
                .map(SoB2cDTO.ExcelExportDTO::getCountry)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(countryList)) {
            countryNameMap = sysDictFeign.listCountryByIds(countryList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }

        //根据SKU查询BOM判断是否是组合SKU
        bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        List<String> childSkuIdList = bomChildrenList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getSkuId()) && CharSequenceUtil.equals(BomTypeEnum.COMBINATION.getType(), obj.getType()))
                .map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(childSkuIdList)) {
            skuIdList.addAll(childSkuIdList);
        }

        //产品信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //查询流程审核信息
        List<String> ids = records.stream().map(SoB2cDTO.ExcelExportDTO::getId).distinct().collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_B2C.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
        }

        //虚拟仓库存
        List<String> virtualWarehouseIdList = records.stream().map(SoB2cDTO.ExcelExportDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        paramDTO.setDictInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
        paramDTO.setSkuIdList(skuIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryFeign.listInventoryQty(paramDTO);
        //虚拟仓库信息
        List<VirtualWarehouseEntity> virtualWarehouseList = CollectionUtils.isEmpty(virtualWarehouseIdList) ? new ArrayList<>() : FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //是否缺货
        if (isOutStock) {
            // 忽略库存计算SKU
            List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
            ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                    ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : com.google.common.collect.Lists.newArrayList();

            //获取第三方仓海外信息
            InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
            skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode()));
            skuInventoryDTO.setWarehouseIdList(warehouseIdList);
            skuInventoryDTO.setSkuIdList(skuIdList);
            inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);
        }
        //销售出库
        List<String> soIds = records.stream().map(SoB2cDTO.ExcelExportDTO::getId).distinct().collect(Collectors.toList());
        List<List<String>> partionSoIds = com.google.common.collect.Lists.partition(soIds, 5000);
        List<SoOutstockEntity> soOutstockEntityList = new ArrayList<>();
        partionSoIds.forEach(v -> {
            soOutstockEntityList.addAll(FeignQuery.create(SoOutstockEntity.class).in(SoOutstockEntity::getSoId, v).select(SoOutstockEntity::getSoId, SoOutstockEntity::getBillDate).list());
        });
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);
        List<String> detailIds = records.stream().map(SoB2cDTO.ExcelExportDTO::getDetailId).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByIds(detailIds);
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryFeign.listBySourceId(ids);

        //B2C销售订单分类
        List<SoB2cRefCategoryDTO.CategoryNamesDTO> categoryNamesList = soB2cRefCategoryService.listCategoryNamesBySoIds(soIds);
        Map<String, String> categoryNamesMap = categoryNamesList.stream().collect(Collectors.toMap(SoB2cRefCategoryDTO.CategoryNamesDTO::getSoB2cId, SoB2cRefCategoryDTO.CategoryNamesDTO::getCategoryNames));

        List<String> codeList = new ArrayList<>();
        List<String> codeAndParentSkuIdList = new ArrayList<>();
        for (SoB2cDTO.ExcelExportDTO exportDTO : records) {
            exportDTO.setInvalidTypeName(SoB2cInvalidTypeEnum.getName(exportDTO.getInvalidType()));
            //B2C销售订单分类
            exportDTO.setCategoryNames(categoryNamesMap.getOrDefault(exportDTO.getId(),""));
            SoOutstockEntity soOutstock = soOutstockEntityList.stream().filter(v -> v.getSoId().equals(exportDTO.getId())).findFirst().orElse(new SoOutstockEntity());
            exportDTO.setSoOutStockTime(soOutstock.getBillDate());
            //审核状态
            exportDTO.setApproveStatusName(ApproveStatusEnum.getName(exportDTO.getApproveStatus()));
            //订单状态
            exportDTO.setBillStatusName(SoB2cBillStatusEnum.getName(exportDTO.getBillStatus()));

            exportDTO.setTaxCost(MathUtil.multiplyWithTwo(exportDTO.getTaxCost(), exportDTO.getQty()));
            //产品名称
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), exportDTO.getSkuId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                exportDTO.setProductName(productDetailEntity.getName());
                exportDTO.setVariantProperty(productDetailEntity.getVariantProperty());
            }
            String countryName = countryNameMap.getOrDefault(exportDTO.getCountry(), "");
            exportDTO.setCountryName(countryName);

            //仓位名称
            String warehouseLocationName = warehouseLocationEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCode(), exportDTO.getWarehouseLocation())
                    && CharSequenceUtil.equals(obj.getWarehouseId(), exportDTO.getWarehouseId()))
                    .map(WarehouseLocationEntity::getName).findFirst().orElse("");
            exportDTO.setWarehouseLocationName(warehouseLocationName);

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(exportDTO.getId()) && CharSequenceUtil.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                exportDTO.setApproveUserName(curApprove);
            }
            //是否缺货
            if (isOutStock) {
                Integer useableQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(inventoryList)) {
                    //可用库存
                    useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(exportDTO.getSkuId())
                            && obj.getWarehouseId().equals(exportDTO.getWarehouseId())
                            && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                            .mapToInt(obj -> obj.getInventoryTotal()).sum();
                }
                exportDTO.setUseableQty(useableQty);
                //存在仓库则需要判断是否缺货
                if (CharSequenceUtil.isNotBlank(exportDTO.getWarehouseId())) {
                    //缺货订单
                    if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus())
                            || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus()))) {
                        //bean转换
                        SoB2cDetailDTO.ListDTO detailDTO = BeanMapperUtils.map(SoB2cDetailDTO.ListDTO.class, exportDTO);
                        Boolean outStock = isOutStock(bomChildrenList, inventoryList, detailDTO, ignoreInventorySkuIds);
                        exportDTO.setIsOutStock(outStock);
                    }
                }
            }

            //存在虚拟仓库则判断是否缺货
            if (CharSequenceUtil.isNotBlank(exportDTO.getVirtualWarehouseId())) {
                String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), exportDTO.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
                exportDTO.setVirtualWarehouseName(virtualWarehouseName);
                Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), exportDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), exportDTO.getVirtualWarehouseId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), exportDTO.getWarehouseId()))
                        .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                        .findFirst().orElse(MathUtil.ZERO);
                exportDTO.setVirtualUsableQty(virtualUsableQty);
            }

            //销售套装bom子级信息
            List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), exportDTO.getSkuId())
                    && CharSequenceUtil.equals(BomTypeEnum.COMBINATION.getType(), obj.getType()))
                    .collect(Collectors.toList());
            //发货单--提交发货时间、面单打印时间
            List<SoB2cDeliveryEntity> collect = soB2cDeliveryEntities.stream()
                    .filter(e -> Objects.nonNull(e) && Objects.equals(e.getSourceId(), exportDTO.getId()))
                    .filter(v -> !v.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode()))
                    .sorted(Comparator.comparing(SoB2cDeliveryEntity::getCreateTime).reversed())//降序
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                exportDTO.setFinishPrintTime(collect.get(0).getFinishPrintTime());
                exportDTO.setCreateDeliveryTime(collect.get(0).getCreateTime());
            }
            SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryEntities.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getSourceId(), exportDTO.getId())).findFirst().orElse(null);
            //标签汇总
            exportDTO.setLabelOrderList(getLabelOrderList(exportDTO, soB2cRefList, childList, soB2cDeliveryEntity));
            SoB2cDetailEntity soB2cDetail = soB2cDetailEntityList.stream().filter(e -> Objects.equals(exportDTO.getDetailId(), e.getId())).findFirst().orElse(null);
            SoB2cDetailDTO.ListDTO detailDTO = BeanUtil.copyProperties(soB2cDetail, SoB2cDetailDTO.ListDTO.class);
            exportDTO.setLabelDetailList(getLabelDetailList(exportDTO, skuVOMap, bomChildrenList, inventoryList, ignoreInventorySkuIds, detailDTO, virtualInventoryList, virtualWarehouseList));
            //销售套装bom
            if (CollectionUtils.isNotEmpty(childList)) {
                List<Integer> qtyList = new ArrayList<>();
                for (BomChildrenSkuDTO bomChildrenSkuDTO : childList) {
                    SoB2cDTO.ExcelExportDTO resultDTO = new SoB2cDTO.ExcelExportDTO();
                    BeanMapperUtils.copy(exportDTO, resultDTO);
                    //记录原始sku
                    resultDTO.setParentSkuId(exportDTO.getSkuId());
                    resultDTO.setParentSkuNo(exportDTO.getSkuNo());
                    resultDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                    resultDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    resultDTO.setQty(resultDTO.getQty());
                    resultDTO.setSkuQty(resultDTO.getQty() * bomChildrenSkuDTO.getQuantity());
                    //导出子级SKU信息
                    ProductDetailEntity childEntity = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), bomChildrenSkuDTO.getSkuId()))
                            .findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(childEntity)) {
                        resultDTO.setProductName(childEntity.getName());
                        resultDTO.setVariantProperty(childEntity.getVariantProperty());
                    }
                    //虚拟仓可用库存
                    if (CharSequenceUtil.isNotBlank(exportDTO.getVirtualWarehouseId())) {
                        //虚拟仓是否缺货
                        Integer childVirtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), bomChildrenSkuDTO.getSkuId())
                                && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), exportDTO.getVirtualWarehouseId())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), exportDTO.getWarehouseId()))
                                .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                                .findFirst().orElse(MathUtil.ZERO);
                        resultDTO.setVirtualUsableQty(childVirtualUsableQty);

                        //针对父级可用数量
                        double floor = Math.floor((double) childVirtualUsableQty / bomChildrenSkuDTO.getQuantity());
                        Integer parentUsableQty = (int) floor;
                        qtyList.add(parentUsableQty);
                    }
                    //如果按子级导出则添加
                    if (SoB2cExportTypeEnum.CHILD_EXPORT.getCode().equals(exportType)) {
                        //根据订单维度还是bom维度清除已存在的记录
                        processRepeatData(resultDTO, codeList, codeAndParentSkuIdList);
                        resultList.add(resultDTO);
                    }
                }
                //父级可用取子级中最小可用数量
                if (SoB2cExportTypeEnum.PARENT_EXPORT.getCode().equals(exportType)) {
                    if (CollectionUtils.isNotEmpty(qtyList)) {
                        Integer bomUsableQty = qtyList.stream().min(Comparator.comparing(obj -> obj)).get();
                        exportDTO.setVirtualUsableQty(bomUsableQty);
                    }
                    exportDTO.setSkuQty(exportDTO.getQty());
                    //根据订单维度还是bom维度清除已存在的记录
                    processRepeatData(exportDTO, codeList, codeAndParentSkuIdList);
                    resultList.add(exportDTO);
                }
            } else {
                exportDTO.setSkuQty(exportDTO.getQty());
                //根据订单维度还是bom维度清除已存在的记录
                processRepeatData(exportDTO, codeList, codeAndParentSkuIdList);
                resultList.add(exportDTO);
            }
        }
        return resultList;
    }

    /**
     * 汇总明细标签
     *
     * @param exportDTO
     * @param skuVOMap
     * @param bomChildrenList
     * @param inventoryList
     * @param ignoreInventorySkuIds
     * @param detailDTO
     * @param virtualInventoryList
     * @param virtualWarehouseList
     * @return
     */
    private String getLabelDetailList(SoB2cDTO.ExcelExportDTO exportDTO, Map<String, SkuVO> skuVOMap, List<BomChildrenSkuDTO> bomChildrenList, List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList, List<String> ignoreInventorySkuIds, SoB2cDetailDTO.ListDTO detailDTO, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList, List<VirtualWarehouseEntity> virtualWarehouseList) {
        StringBuilder labelDetailStr = new StringBuilder();
        SkuVO skuVO = skuVOMap.get(exportDTO.getSkuId());
        SkuVO.PropertyDTO skuPropertyDTO = Objects.isNull(skuVO) ? new SkuVO.PropertyDTO() : Objects.isNull(skuVO.getPropertyDTO()) ? new SkuVO.PropertyDTO() : skuVO.getPropertyDTO();
        List<SoB2cDetailDTO.PropertyDTO> propertyDTOList = soB2cDetailService.handlePropertyDTOList(skuPropertyDTO);
        if (CollectionUtils.isNotEmpty(propertyDTOList)) {
            String propertyStr = propertyDTOList.stream().map(SoB2cDetailDTO.PropertyDTO::getName).distinct().collect(Collectors.joining(","));
            labelDetailStr.append(propertyStr).append(",");
        }
        //标签处理
        SoB2cDetailDTO.DetailLabelDTO detailLabelDTO = new SoB2cDetailDTO.DetailLabelDTO();
        String detailLabel = exportDTO.getLabelJson();
        if (StringUtils.isNotBlank(detailLabel)) {
            SoB2cDetailDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(detailLabel, SoB2cDetailDTO.LabelJsonDTO.class);
            labelDetailStr.append(Objects.equals("U_TAXED", labelJsonDTO.getAlreadyTaxed()) || Objects.equals("I_TAXED", labelJsonDTO.getAlreadyTaxed()) ? "速卖通已税," : "");
            labelDetailStr.append(Objects.equals("cainiaoInternationalWarehouse", labelJsonDTO.getLogisticsWarehouseType()) ? "菜鸟官方仓," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("AE_PLUS_RU") ? "AE_PLUS," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("HBA_UP_EXPRESS") ? "AE_合单," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("leadTimeTag#10") ? "十日达," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("leadTimeTag#12") ? "12日达," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("leadTimeTag#15") ? "15日达," : "");
            labelDetailStr.append(Objects.nonNull(labelJsonDTO.getIsRefunded()) && labelJsonDTO.getIsRefunded() ? "退款订单," : "");
        }
        //存在仓库则需要判断是否缺货
        if (StrUtil.isNotBlank(exportDTO.getWarehouseId())) {
            Integer useableQty = MathUtil.ZERO;
            Integer freezeQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(inventoryList)) {
                //可用库存
                useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                        && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                        && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                        .mapToInt(obj -> obj.getInventoryTotal()).sum();
                //冻结库存
                freezeQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                        && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                        && InventoryStatusEnum.FROZEN.getCode().equals(obj.getInventoryStatus()))
                        .mapToInt(obj -> obj.getInventoryTotal()).sum();
            }
            detailDTO.setUseableQty(useableQty);
            detailDTO.setFreezeQty(freezeQty);
            //缺货订单
            if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus())
                    || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus()))) {
                //实体仓缺货
                Boolean isOutStock = isOutStock(bomChildrenList, inventoryList, detailDTO, ignoreInventorySkuIds);
                detailLabelDTO.setIsOutStock(isOutStock);
            }
        }
        //存在虚拟仓库则判断是否缺货
        if (StrUtil.isNotBlank(detailDTO.getVirtualWarehouseId())) {
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailDTO.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            detailDTO.setVirtualWarehouseName(virtualWarehouseName);
            //虚拟仓缺货处理
            isVirtualOutStock(bomChildrenList, virtualInventoryList, detailLabelDTO, detailDTO);
            //缺货订单
            if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus())
                    || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus()))) {
                detailLabelDTO.setIsOutStock(Boolean.FALSE);
            }
        }
        labelDetailStr.append(Objects.nonNull(detailLabelDTO.getIsOutStock()) && detailLabelDTO.getIsOutStock() ? "缺货订单," : "");
        labelDetailStr.append(Objects.nonNull(detailLabelDTO.getIsVirtualOutStock()) && detailLabelDTO.getIsVirtualOutStock() ? "缺货订单(X缺)," : "");
        String labelDetail = labelDetailStr.toString();
        //移除字符串最后一个字符
        return StrUtil.isNotBlank(labelDetail) ? labelDetail.substring(0, labelDetail.length() - 1) : "";
    }

    /**
     * 整合销售订单标签
     *
     * @param data
     * @param soB2cRefList
     * @param childList
     * @param soB2cDeliveryEntity
     * @return
     */
    private String getLabelOrderList(SoB2cDTO.ExcelExportDTO data, List<SoB2cRefEntity> soB2cRefList, List<BomChildrenSkuDTO> childList, SoB2cDeliveryEntity soB2cDeliveryEntity) {
        StringBuilder labelOrderStr = new StringBuilder();
        //标签处理
        String label = data.getLabel();
        //主表标签
        Boolean isAddFrozenTag = Boolean.FALSE;
        if (StringUtils.isNotBlank(label)) {
            SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(label, SoB2cDTO.LabelJsonDTO.class);
            List<String> statusList = Arrays.asList("RISK_CONTROL", "IN_FROZEN", "Unfulfillable", "IN_CANCEL");
            if (Objects.equals(data.getBillStatus(), "frozen") && (statusList.contains(labelJsonDTO.getAliexpressStatus()) || statusList.contains(labelJsonDTO.getAmazonStatus()))) {
                labelOrderStr.append("冻结中,");
                isAddFrozenTag = Boolean.TRUE;
            }
            if (Objects.equals("AFN", labelJsonDTO.getFulfillmentChannel())) {
                labelOrderStr.append("FBA,");
            }
            List<String> shipNodeTypeList = Arrays.asList("WFSFulfilled", "3PLFulfilled");
            if (shipNodeTypeList.contains(labelJsonDTO.getShipNodeType())) {
                labelOrderStr.append("WFS,");
            }
            labelOrderStr.append(Objects.nonNull(labelJsonDTO.getIsRefunded()) && labelJsonDTO.getIsRefunded() ? "退款订单," : "");
        }
        String logisticsTypeName = OrderLogisticTypeEnum.getName(data.getLogisticType());
        labelOrderStr.append(CharSequenceUtil.isNotBlank(logisticsTypeName) ? logisticsTypeName + "," : "");
        labelOrderStr.append(CollectionUtils.isNotEmpty(childList) ? "组合产品," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsIntercept()) && data.getIsIntercept() ? "拦截订单," : "");
        labelOrderStr.append(Objects.equals(SourceTypeEnum.SELF_ADD.getCode(), data.getSourceType()) ? "手工订单," : "");
        if (CollectionUtils.isNotEmpty(soB2cRefList)) {
            //合并
            long mergeCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                    && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())
                    && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())).count();
            if (mergeCount > 0) {
                labelOrderStr.append("合并订单,");
            }
            //拆分
            long splitCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                    && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())
                    && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())).count();
            if (splitCount > 0) {
                labelOrderStr.append("拆分订单,");
            }
        }
        labelOrderStr.append(Objects.nonNull(soB2cDeliveryEntity) && Objects.equals("manual", soB2cDeliveryEntity.getShipmentMark()) ? "手动标发," : "");
        labelOrderStr.append(Objects.nonNull(data.getInvalidStatus()) && data.getInvalidStatus() ? "订单作废," : "");
//        labelOrderStr.append(Objects.nonNull(data.getIsCancel()) && data.getIsCancel() ? "订单取消," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsChangeReceiverAddress()) && data.getIsChangeReceiverAddress() ? "修改收货地址," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsChangeSku()) && data.getIsChangeSku() ? "更换发货SKU," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsNotOutbound()) && data.getIsNotOutbound() ? "不出库发货," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsFrozen()) && data.getIsFrozen() && !isAddFrozenTag ? "冻结中," : "");
        String labelOrder = labelOrderStr.toString();
        //移除字符串最后一个字符
        return StrUtil.isNotBlank(labelOrder) ? labelOrder.substring(0, labelOrder.length() - 1) : "";
    }

    private void processRepeatData(SoB2cDTO.ExcelExportDTO resultDTO, List<String> codeList, List<String> codeAndParentSkuIdList) {
        String code = resultDTO.getCode();
        if (CharSequenceUtil.isNotBlank(code) && codeList.contains(code)) {
            //清除订单维度数据
            resultDTO.setShippingCost(BigDecimal.ZERO);
            resultDTO.setAmount(BigDecimal.ZERO);
            resultDTO.setEstimatedShippingCost(BigDecimal.ZERO);
            resultDTO.setActualShippingCost(BigDecimal.ZERO);
            resultDTO.setLength(BigDecimal.ZERO);
            resultDTO.setWidth(BigDecimal.ZERO);
            resultDTO.setHeight(BigDecimal.ZERO);
            resultDTO.setWeight(BigDecimal.ZERO);
        }else if (CharSequenceUtil.isNotBlank(code)){
            codeList.add(code);
        }
        String parentSkuId = resultDTO.getParentSkuId();
        if (CharSequenceUtil.isNotBlank(code) && CharSequenceUtil.isNotBlank(parentSkuId) && codeAndParentSkuIdList.contains(code + "-" + parentSkuId)) {
            //清除bom拆分数据
            resultDTO.setTaxCost(BigDecimal.ZERO);
            resultDTO.setSourceAmount(BigDecimal.ZERO);
            resultDTO.setBaseAmount(BigDecimal.ZERO);
            resultDTO.setQty(MathUtil.ZERO);
        }else if (CharSequenceUtil.isNotBlank(code) && CharSequenceUtil.isNotBlank(parentSkuId)){
            codeAndParentSkuIdList.add(code + "-" + parentSkuId);
        }
    }


    /**
     * @return FinancialInfoDTO
     * @description: 导出财务信息，现未用，之后会单独导出
     * @author Will
     * @date: 2024/4/18 12:29
     */
   /* private SoB2cDTO.FinancialInfoDTO getExportFinancialInfo(List<SoB2cDTO.ExcelExportDTO> records,SoB2cDTO.ExcelExportDTO exportDTO,
                                            List<DictBasicEntity> dictList,Boolean isCny) {
        //平台费
        BigDecimal platformCost = BigDecimal.ZERO;
        //avt 费
        BigDecimal vatCost = BigDecimal.ZERO;
        //转账费
        BigDecimal paypalCost = BigDecimal.ZERO;

        SoB2cDTO.FinancialInfoDTO financialInfoDTO = new SoB2cDTO.FinancialInfoDTO();
        BeanMapperUtils.copy(exportDTO, financialInfoDTO);
        financialInfoDTO.setShippingCost(exportDTO.getFinanceShippingCost());
        financialInfoDTO.setAccessoriesCost(exportDTO.getFinanceAccessoriesCost());

        //商品成本,订单SKU*数量的含税成本价汇总
        BigDecimal itemCost = records.stream().map(obj -> MathUtil.multiply(obj.getTaxCost(), obj.getQty())).reduce(BigDecimal.ZERO, BigDecimal::add);

        //商品金额
        BigDecimal totalAmount = records.stream().map(SoB2cDTO.ExcelExportDTO::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        //判断是否是人民币
        if (isCny) {
            financialInfoDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            financialInfoDTO.setAmount(MathUtil.multiply(totalAmount, exportDTO.getExchangeRate()));
            financialInfoDTO.setItemCost(itemCost);
        } else {
            financialInfoDTO.setCurrency(exportDTO.getCurrency());
            financialInfoDTO.setAmount(totalAmount);
            financialInfoDTO.setItemCost(MathUtil.divide(itemCost, exportDTO.getExchangeRate()));
        }

        String platformOption = exportDTO.getPlatformCostType();
        String vatOption = exportDTO.getVatCostType();
        String transferOption = exportDTO.getTransferCostType();
        BigDecimal platformRate = exportDTO.getPlatformRate();
        BigDecimal vatRate = exportDTO.getVatRate();
        BigDecimal transferRate = exportDTO.getTransferRate();

        //平台费
        DictBasicEntity dictPlatformOption = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),DictBasicTypeEnum.SHOP_PLATFORM_COST.getType())
                && CharSequenceUtil.equals(obj.getValue(),platformOption)).findFirst().orElse(null);

        //vat费
        DictBasicEntity dictVatOption = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),DictBasicTypeEnum.SHOP_VAT_COST.getType())
                && CharSequenceUtil.equals(obj.getValue(),vatOption)).findFirst().orElse(null);

        //转账费
        DictBasicEntity dictTransferOption = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),DictBasicTypeEnum.SHOP_TRANSFER_COST.getType())
                && CharSequenceUtil.equals(obj.getValue(),transferOption)).findFirst().orElse(null);

        //平台费
        BigDecimal dividePlatformRate = MathUtil.divide(platformRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictPlatformOption) && ShopPlatformCostEnum.MULTIPLY_PLATFORM_RATE.getCode().equals(dictPlatformOption.getValue())) {
            platformCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), dividePlatformRate);
            financialInfoDTO.setPlatformCostType(dictPlatformOption.getValue());
            financialInfoDTO.setPlatformRate(platformRate);
        }
        //转账费
        BigDecimal divideTransferRate = MathUtil.divide(transferRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictTransferOption) && ShopTransferCostEnum.MULTIPLY_TRANSFER_RATE.getCode().equals(dictTransferOption.getValue())) {
            paypalCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), divideTransferRate);
            financialInfoDTO.setTransferCostType(dictTransferOption.getValue());
            financialInfoDTO.setTransferRate(transferRate);
        }
        //vat费
        BigDecimal divideVatRate = MathUtil.divide(vatRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.MULTIPLY_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), divideVatRate);
        } else if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.MULTIPLY_ADD_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), MathUtil.add(BigDecimal.ONE, divideVatRate)).multiply(divideVatRate);
        } else if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.DIVISION_ADD_MULTIPLY_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.divide(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), MathUtil.add(BigDecimal.ONE, divideVatRate)).multiply(divideVatRate);
        }
        if (ObjectUtils.isNotEmpty(dictVatOption)) {
            financialInfoDTO.setVatCostType(dictVatOption.getValue());
        }
        financialInfoDTO.setVatRate(vatRate);

        //平台费,店铺计算
        financialInfoDTO.setPlatformCost(platformCost);

        //转账费,店铺计算
        financialInfoDTO.setPaypalCost(paypalCost);

        //运费收入
        financialInfoDTO.setShippingCost(ObjectUtil.isEmpty(financialInfoDTO.getShippingCost()) ? BigDecimal.ZERO : financialInfoDTO.getShippingCost());
        //物流成本
        financialInfoDTO.setLogisticsCost(ObjectUtil.isEmpty(financialInfoDTO.getLogisticsCost()) ? BigDecimal.ZERO : financialInfoDTO.getLogisticsCost());

        //VAT税费,店铺计算
        financialInfoDTO.setVatCost(vatCost);
        //总利润,订单总金额+运费收入-商品成本-物流成本-平台费-转账费-包装辅料费-VAT税费
        BigDecimal profit = ObjectUtil.defaultIfNull(financialInfoDTO.getAmount(), BigDecimal.ZERO)
                .add(ObjectUtil.defaultIfNull(financialInfoDTO.getShippingCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getItemCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getLogisticsCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(platformCost, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(paypalCost, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getAccessoriesCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(vatCost, BigDecimal.ZERO));
        financialInfoDTO.setProfit(profit);

        BigDecimal profitRate = MathUtil.divide(profit, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setProfitRate(MathUtil.compareTo(profitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : profitRate + "%");
        return financialInfoDTO;
    }*/
    private void skuMappingCheck(SoB2cEntity entity, List<SoB2cDetailEntity> detailList) {
        if (entity.hasPlatformWarehouseOrder()) {
            // 平台订单校验
            // 是否已存在销售出库单
//            List<SoOutstockEntity> list = soOutstockFeign.listBySoIds(Collections.singletonList(entity.getId()));
//            if (!CollectionUtils.isEmpty(list)) {
//                throw new ServiceException(ApiError.IS_SO_OUT_STOCK_NOT_UPDATE_MAPPING);
//            }
        } else {
            // 自发货订单校验
//            List<SoB2cDeliveryEntity> list = soB2cDeliveryFeign.listBySourceId(Collections.singletonList(entity.getId()));
            // 查询来源明细ID
            List<SoB2cDeliveryEntity> list = FeignQuery.create(SoB2cDeliveryEntity.class)
                    .eq(SoB2cDeliveryEntity::getSourceId, entity.getId())
                    .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                    .list();
            if (!CollectionUtils.isEmpty(list)) {
                throw new ServiceException(ApiError.IS_B2C_DELIVERY_NOT_UPDATE_MAPPING);
            }
        }
    }

    @Override
    public List<WmsDataCompareTaskDTO.SoB2cDTO> getDataCompareByCondition(
            WmsDataCompareTaskDTO.SoOutstockDTO params) {
        return baseMapper.getDataCompareByCondition(params);
    }

    @Override
    public List<BatchResultDTO> deliveryWithNotOutbound(List<SoB2cDTO.DeliveryWithNotOutboundDTO> dtoList) {
        List<String> ids = dtoList.stream().map(SoB2cDTO.DeliveryWithNotOutboundDTO::getId).collect(Collectors.toList());
        List<String> warehouseIds = dtoList.stream().map(SoB2cDTO.DeliveryWithNotOutboundDTO::getWarehouseId).collect(Collectors.toList());
        List<String> logisticsChannelIds = dtoList.stream().map(SoB2cDTO.DeliveryWithNotOutboundDTO::getLogisticsChannelId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = this.listByIds(ids);
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(ids);
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(ids);
        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(warehouseIds);
        List<LogisticsChannelDTO.BaseDTO> channelInfoList = logisticsFeign.listChannelInfoById(logisticsChannelIds);
        List<SoB2cReceiverEntity> soB2cReceiverEntities = soB2cReceiverService.listByMainIds(ids);
        List<String> noInventorySkuIdList = plmTaskFeign.getNoInventorySku()
                .stream()
                .map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<SoB2cEntity> updateList = new ArrayList<>();
        List<SoB2cLogisticsEntity> updateLogisticList = new ArrayList<>();
        List<SoB2cDetailEntity> updateDetailList = new ArrayList<>();
        List<String> deleteErrorIds = new ArrayList<>();
        for (SoB2cDTO.DeliveryWithNotOutboundDTO dto : dtoList) {
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(v -> v.getId().equals(dto.getId())).findFirst().orElse(null);
            if(Objects.isNull(soB2cEntity)){
                resultDTOList.add(BatchResultDTO.fail(dto.getId(), dto.getId(), "销售订单未找到"));
                continue;
            }
            if (soB2cEntity.hasPlatformWarehouseOrder()) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "平台仓订单不允许操作不出库发货"));
                continue;
            }
            WarehouseDTO.UpdateDTO updateDTO = updateDTOS.stream().filter(v -> v.getId().equals(dto.getWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(updateDTO)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "平台仓库未找到"));
                return resultDTOList;
            }
            LogisticsChannelDTO.BaseDTO baseDTO = channelInfoList.stream().filter(v -> v.getId().equals(dto.getLogisticsChannelId())).findFirst().orElse(null);
            if (Objects.isNull(baseDTO)) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "渠道未找到"));
                return resultDTOList;
            }
            dto.setLogisticsChannelCode(baseDTO.getCode());
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v -> v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
            soB2cLogisticsEntity.setCode(dto.getTrackNo());
            soB2cLogisticsEntity.setLogisticsChannelId(dto.getLogisticsChannelId());
            soB2cLogisticsEntity.setLogisticsChannelName(baseDTO.getName());
            soB2cLogisticsEntity.setDeliveryTime(dto.getDeliveryTime());
            if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(soB2cEntity.getBillStatus()) || SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(soB2cEntity.getBillStatus()) || SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), CharSequenceUtil.format("订单状态为{},不允许操作不出库发货", SoB2cBillStatusEnum.getName(soB2cEntity.getBillStatus()))));
                continue;
            }
            soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            soB2cEntity.setAbnormalType("");
            soB2cEntity.setIsMatchLogisticsRule(true);
            soB2cEntity.setIsMatchOrderRule(true);
            soB2cEntity.setIsNotOutbound(true);
            soB2cEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            if (!dto.getPlatformShipFlag() && !soB2cEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE)) {
                ApproveOneDTO approveOneDTO = new ApproveOneDTO();
                approveOneDTO.setType(ApproveTypeEnum.PASS.getStatus());
                this.approveEnd(approveOneDTO, soB2cEntity, true);
                soB2cEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            }

            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getMainId(), soB2cEntity.getId())).collect(Collectors.toList());

            //将仓库会写到订单的明细发货仓库
            if (CollectionUtils.isNotEmpty(detailEntityList)) {
                detailEntityList.forEach(e -> {
                    e.setWarehouseId(dto.getWarehouseId());
                    e.setWarehouseName(updateDTOS.get(0).getName());
                });
            }
            //生成发货单和出库单
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverEntities.stream().filter(e -> Objects.equals(e.getMainId(), soB2cEntity.getId())).findFirst().orElse(new SoB2cReceiverEntity());
            try {
                UserContext.setIsUserSystem(true);
                soB2cCoreService.generateDeliveryAndOutStock(soB2cEntity,detailEntityList,dto,soB2cLogisticsEntity,soB2cReceiverEntity);
            } catch (Exception e) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), CharSequenceUtil.format("生成发货单，出库单失败:{}", e.getMessage())));
                continue;
            }finally {
                UserContext.clearIsUserSystem();
            }
            updateLogisticList.add(soB2cLogisticsEntity);
            if (dto.getPlatformShipFlag() && this.checkPlatformShipOrder(soB2cEntity.getId()) && !soB2cEntity.hasPlatformWarehouseOrder()) {
                //调用第三方平台SDK声明发货
                try {
                    PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
                    platformShipOrderDTO.setSoB2cId(soB2cEntity.getId());
                    platformShipOrderDTO.setSubmitPlatformUniqueKey(soB2cEntity.convertSubmitPlatformUniqueKey());
                    platformShipOrderDTO.setDictPlatform(soB2cEntity.getDictPlatform());
                    platformShipOrderDTO.setHasNotOutStock(true);
                    soB2cDeliveryFeign.shipOrder(platformShipOrderDTO);
                } catch (Exception e) {
                    resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), CharSequenceUtil.format("平台标发失败:{}", e.getMessage())));
                    continue;
                }
            }

            if(!dto.getPlatformShipFlag() && soB2cEntity.getThirdSystem().equals(PlatformDictEnum.LING_XING.getCode())){
                try {
                    LingxingApiUtils.cancelOrderByOrderList(Collections.singletonList(soB2cEntity.getThirdCode()));
                }catch (Exception e) {
                    resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), CharSequenceUtil.format("领星标记订单不发货失败:{}", e.getMessage())));
                    continue;
                }
            }
            // 不出库发货虚拟商品同步数帝云
            List<SoB2cDetailEntity> noInventorySkuDetailList = detailEntityList.stream().filter(e -> noInventorySkuIdList.contains(e.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(noInventorySkuDetailList)) {
                syncSoB2cService.syncDataToSdy(soB2cEntity, noInventorySkuDetailList, SyncOperateEnum.OPERATE_APPROVE.getCode());
            }
            operateLogService.addModuleOperateLog("订单操作不出库发货", ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "不出库发货");

        }
        if (CollectionUtils.isNotEmpty(updateLogisticList)) {
            soB2cLogisticsService.updateBatchById(updateLogisticList);
        }
        return resultDTOList;
    }

    /**
     * 申报信息规则匹配
     *
     * @param id
     * @param map
     * @param isUpdate              是否更新申报信息
     * @param isUpdatePackingWeight
     * @return
     */
    @Override
    public BatchResultDTO declareRule(String id, HashMap<String, Object> map, Boolean isUpdate, Boolean isUpdatePackingWeight) {
        //已审核 配货中才会进行 申报规则执行
        SoB2cEntity entity = super.getById(id);
        isExist(entity);
        //已存在申报信息 则不进行规则匹配
        List<SoB2cDeclareProductEntity> declareProductList = soB2cDeclareProductService.listBySoId(id);
        if (Objects.nonNull(isUpdate) && !isUpdate && CollectionUtils.isNotEmpty(declareProductList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_HAS_DECLARE);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        if (StrUtil.isBlank(logisticsEntity.getLogisticsChannelId())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_ID_NOT_NULL, entity.getCode());
        }
        if (isFullyManagedOrder(entity.getDictPlatform())){
            return BatchResultDTO.success(id, entity.getCode(), OperationTypeEnum.DECLARE_RULE);
        }
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(id);
        if (ObjectUtil.isEmpty(receiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        String country = Objects.nonNull(receiverEntity) ? receiverEntity.getCountry() : "";
        LogisticsChannelDTO.LogisticsChannelConstraintDTO channelConstraintDTO = logisticsFeign.getLogisticsChannelConstraint(logisticsEntity.getLogisticsChannelId(), country);
        //最高报关金额
        BigDecimal maxCustomsAmount = channelConstraintDTO.getMaxCustomsAmount();
        //最低报关金额
        BigDecimal minCustomsAmount = channelConstraintDTO.getMinCustomsAmount();
        //物流渠道下单平台
        String logisticsPlatform = channelConstraintDTO.getLogisticsPlatform();
        if(StringUtils.isBlank(logisticsPlatform)){
            logisticsPlatform = PlatformDictEnum.CUSTOMIZE.getCode();
        }
        if (StrUtil.isBlank(logisticsEntity.getLogisticsChannelId()) || StrUtil.isBlank(logisticsPlatform)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_PLATFORM_NOT_NULL, entity.getCode());
        }
        if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())
                || !ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus())
        ) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_DISTRIBUTION_DECLARE, entity.getCode());
        }
        if (map.isEmpty()) {
            List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
            handleDeclareMatchJson(entity, detailList, map, logisticsPlatform);
        }
        //规则结果
        cfgRuleDeclareService.getRuleDeclareMatchResult(map, maxCustomsAmount, minCustomsAmount, isUpdate, declareProductList);
        if (isUpdatePackingWeight) {
            //更新订单包装重量
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(id);
            List<SplitSkuDTO> splitSkuDTOS = this.splitBySoDetail(soB2cDetailEntityList, new ArrayList<>(), null, true);
            if (CollectionUtils.isNotEmpty(splitSkuDTOS)) {
                List<String> keyList = new ArrayList<>();
                keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
                List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
                Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
                BigDecimal packWeight = SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode()));
                soB2cLogisticsService.updateWeight(id, logisticsEntity.getId(), packWeight, "更新报关信息同步重量");
            }

        }
        return BatchResultDTO.success(id, entity.getCode(), OperationTypeEnum.DECLARE_RULE);
    }

    @Override
    public List<LogisticsDeclareProductDTO> splitLogisticsBySoDetail(List<SoB2cDetailEntity> detailList, SoB2cEntity soB2cEntity, String logisticsPlatform) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soB2cEntity.getId());
        String country = Objects.nonNull(receiverEntity) ? Objects.nonNull(receiverEntity.getCountry()) ? receiverEntity.getCountry() : "" : "";
        //判断是否是速卖通  来源平台
        Boolean isAliExpress = PlatformDictEnum.ALI_EXPRESS.getCode().equals(logisticsPlatform);

        List<LogisticsDeclareProductDTO> declareProductDTOS = new ArrayList<>();
        List<String> skuIds = detailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
//        List<BomChildrenSkuDTO> skuDTOS = plmTaskFeign.listBomBySkuIds(skuIds);
        //子sku列表
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //合并 子sku和父级sku获取 全量sku明细
        if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)) {
            skuIds = Stream.concat(skuIds.stream(), bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).filter(StrUtil::isNotEmpty))
                    .collect(Collectors.toList());
        }
        //全量sku的产品明细
        List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listLogisticsProduct(skuIds);
        //sku 产品物流信息map
        Map<String, LogisticsProductDTO.ProductDTO> skuMap = skuInfoList.stream().collect(Collectors.toMap(LogisticsProductDTO.ProductDTO::getSkuId, Function.identity()));
        //sku 父子 map
        Map<String, List<BomChildrenSkuDTO>> skuChildMap = bomChildrenSkuDTOS.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        //获取sku目的国海关编码映射关系
        List<ProductCustomsEntity> productCustomsList = plmTaskFeign.listProductCustomsBySkuIds(ProductCustomsSkuDTO.builder().skuIds(skuIds).country(country).build());
        detailList.forEach(soB2cDetailEntity -> {
            BomChildrenSkuDTO skuDTO = bomChildrenSkuDTOS.stream().filter(e -> StrUtil.isNotEmpty(e.getParentSkuId())
                    && StrUtil.isNotEmpty(e.getParentSkuNo()) && e.getParentSkuId().equals(soB2cDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            LogisticsProductDTO.ProductDTO productDTO = skuMap.get(soB2cDetailEntity.getSkuId());

            Boolean isCombination = Boolean.FALSE;
            //检查sku是否是组合产品
            if (Objects.nonNull(productDTO) && CombinationDeclareTypeEnums.SPLIT.getCode().equals(productDTO.getCombinationDeclareType())) {
                //申报类型
                isCombination = Boolean.TRUE;
            }
            //物流产品拆分区分 是否速卖通平台
            if (!isAliExpress && Objects.nonNull(skuDTO) && BomTypeEnum.COMBINATION.getType().equals(skuDTO.getType()) && isCombination) {
                if (Objects.nonNull(skuChildMap) && StrUtil.isNotEmpty(soB2cDetailEntity.getSkuId()) && CollectionUtils.isNotEmpty(skuChildMap.get(soB2cDetailEntity.getSkuId()))) {
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS1 = skuChildMap.get(soB2cDetailEntity.getSkuId());
                    //计算比例 汇总 子件 比例数量*实际含税成本 之和
                    BigDecimal totalPrice = bomChildrenSkuDTOS1.stream().map(e ->{
                        LogisticsProductDTO.ProductDTO productDTO1 = skuMap.get(e.getSkuId());
                        Integer quantity = Objects.nonNull(e.getQuantity()) ? e.getQuantity() : 0;
                        BigDecimal actualTaxCost = Objects.nonNull(productDTO1.getActualTaxCost()) ? productDTO1.getActualTaxCost() : BigDecimal.ZERO;
                        return MathUtil.multiplyWithTwo(actualTaxCost,quantity);
                    }).reduce(BigDecimal.ZERO,BigDecimal::add);
                    bomChildrenSkuDTOS1.forEach(bomChildrenSkuDTO -> {
                        LogisticsProductDTO.ProductDTO bomProduct = skuMap.get(bomChildrenSkuDTO.getSkuId());
                        Integer quantity = Objects.nonNull(bomChildrenSkuDTO.getQuantity()) ? bomChildrenSkuDTO.getQuantity() : 0;
                        BigDecimal actualTaxCost = Objects.nonNull(bomProduct.getActualTaxCost()) ? bomProduct.getActualTaxCost() : BigDecimal.ZERO;
                        BigDecimal price = Objects.nonNull(soB2cDetailEntity.getPrice()) ? soB2cDetailEntity.getPrice() : BigDecimal.ZERO;
                        LogisticsDeclareProductDTO declareProductDTO = LogisticsDeclareProductDTO.builder()
                                .soId(soB2cDetailEntity.getMainId())
                                .soCode(soB2cEntity.getCode())
                                .skuId(bomChildrenSkuDTO.getSkuId())
                                .skuNo(bomChildrenSkuDTO.getSkuNo())
                                .soDetailId(soB2cDetailEntity.getId())
                                .qty(soB2cDetailEntity.getQty() * quantity)
                                .weight(Objects.nonNull(bomProduct) ? bomProduct.getWeight() : 0)
                                .grossWeight(Objects.nonNull(bomProduct) ? bomProduct.getGrossWeight() : BigDecimal.ZERO)
                                .fromCurrency(Objects.nonNull(bomProduct) ? bomProduct.getDeclareCurrency() : "")
                                .fromCurrencySymbol(Objects.nonNull(bomProduct) ? bomProduct.getDeclareCurrencySymbol() : "")
                                .isElectric(Objects.nonNull(bomProduct) ? bomProduct.getIsElectric() : Boolean.FALSE)
                                .declareCn(Objects.nonNull(bomProduct) ? bomProduct.getDeclareChineseName() : "")
                                .declareEn(Objects.nonNull(bomProduct) ? bomProduct.getDeclareEnglishName() : "")
                                .fromDeclarePrice(Objects.nonNull(bomProduct) ? bomProduct.getDeclarePrice() : BigDecimal.ZERO)
                                .toDeclarePrice(Objects.nonNull(bomProduct) ? bomProduct.getDestDeclarePrice() : BigDecimal.ZERO)
                                .toCurrency(Objects.nonNull(bomProduct) ? bomProduct.getDestCurrency() : "")
                                .toCurrencySymbol(Objects.nonNull(bomProduct) ? bomProduct.getDestCurrencySymbol() : "")
                                .toCustomsCode(Objects.nonNull(bomProduct) ? bomProduct.getCustomsCode() : "")
                                .declareUnit(Objects.nonNull(bomProduct) ? bomProduct.getDeclareUnit() : "")
                                .declareModel(Objects.nonNull(bomProduct) ? bomProduct.getDeclareModel() : "")
                                .declareElement(Objects.nonNull(bomProduct) ? bomProduct.getDeclareElement() : "")
                                .englishMaterial(Objects.nonNull(bomProduct) ? bomProduct.getEnglishMaterial() : "")
                                .englishUsage(Objects.nonNull(bomProduct) ? bomProduct.getEnglishUsage() : "")
                                .exemption(Objects.nonNull(bomProduct) ? bomProduct.getExemption() : "")
                                .sourceCargo(Objects.nonNull(bomProduct) ? bomProduct.getSourceCargo() : "")
                                .sourceCountry(Objects.nonNull(bomProduct) ? bomProduct.getSourceCountry() : "")
                                .combinationDeclareType(Objects.nonNull(bomProduct) ? bomProduct.getCombinationDeclareType() : "")
                                .productProperty(Objects.nonNull(bomProduct) ? bomProduct.getProductProperty() : "")
                                .productPropertyId(Objects.nonNull(bomProduct) ? bomProduct.getProductPropertyId() : "")
                                .currency(soB2cDetailEntity.getCurrency())
                                .build();
                        //根据信息匹配目的国申报价 和海关编码
                        ProductCustomsEntity customs = this.getCustomsByCountry(country, bomChildrenSkuDTO.getSkuId(), productCustomsList);
                        if (Objects.nonNull(customs)) {
                            if (Objects.nonNull(customs.getToDeclarePrice()) && customs.getToDeclarePrice().compareTo(BigDecimal.ZERO) > 0) {
                                declareProductDTO.setToDeclarePrice(customs.getToDeclarePrice());
                                declareProductDTO.setToCurrency(customs.getToCurrency());
                                declareProductDTO.setToCurrencySymbol(customs.getToCurrencySymbol());
                            }
                            if (StringUtils.isNotBlank(customs.getCustomsCode())) {
                                declareProductDTO.setToCustomsCode(customs.getCustomsCode());
                            }
                        }
                        //按照比例计算实际金额
                        declareProductDTO.setPrice(MathUtil.divide(MathUtil.multiplyWithTwo(actualTaxCost,quantity), totalPrice).multiply(price));
                        declareProductDTOS.add(declareProductDTO);
                    });
                }
            } else {
                LogisticsDeclareProductDTO declareProductDTO = LogisticsDeclareProductDTO.builder()
                        .soId(soB2cDetailEntity.getMainId())
                        .soCode(soB2cEntity.getCode())
                        .skuId(soB2cDetailEntity.getSkuId())
                        .skuNo(soB2cDetailEntity.getSkuNo())
                        .soDetailId(soB2cDetailEntity.getId())
                        .qty(soB2cDetailEntity.getQty())
                        .weight(Objects.nonNull(productDTO) ? productDTO.getWeight() : 0)
                        .grossWeight(Objects.nonNull(productDTO) ? productDTO.getGrossWeight() : BigDecimal.ZERO)
                        .fromCurrency(Objects.nonNull(productDTO) ? productDTO.getDeclareCurrency() : "")
                        .fromCurrencySymbol(Objects.nonNull(productDTO) ? productDTO.getDeclareCurrencySymbol() : "")
                        .isElectric(Objects.nonNull(productDTO) ? productDTO.getIsElectric() : Boolean.FALSE)
                        .declareCn(Objects.nonNull(productDTO) ? productDTO.getDeclareChineseName() : "")
                        .declareEn(Objects.nonNull(productDTO) ? productDTO.getDeclareEnglishName() : "")
                        .fromDeclarePrice(Objects.nonNull(productDTO) ? productDTO.getDeclarePrice() : BigDecimal.ZERO)
                        .toDeclarePrice(Objects.nonNull(productDTO) ? productDTO.getDestDeclarePrice() : BigDecimal.ZERO)
                        .toCurrency(Objects.nonNull(productDTO) ? productDTO.getDestCurrency() : "")
                        .toCurrencySymbol(Objects.nonNull(productDTO) ? productDTO.getDestCurrencySymbol() : "")
                        .toCustomsCode(Objects.nonNull(productDTO) ? productDTO.getCustomsCode() : "")
                        .declareUnit(Objects.nonNull(productDTO) ? productDTO.getDeclareUnit() : "")
                        .declareModel(Objects.nonNull(productDTO) ? productDTO.getDeclareModel() : "")
                        .declareElement(Objects.nonNull(productDTO) ? productDTO.getDeclareElement() : "")
                        .englishMaterial(Objects.nonNull(productDTO) ? productDTO.getEnglishMaterial() : "")
                        .englishUsage(Objects.nonNull(productDTO) ? productDTO.getEnglishUsage() : "")
                        .exemption(Objects.nonNull(productDTO) ? productDTO.getExemption() : "")
                        .sourceCargo(Objects.nonNull(productDTO) ? productDTO.getSourceCargo() : "")
                        .sourceCountry(Objects.nonNull(productDTO) ? productDTO.getSourceCountry() : "")
                        .combinationDeclareType(Objects.nonNull(productDTO) ? productDTO.getCombinationDeclareType() : "")
                        .productProperty(Objects.nonNull(productDTO) ? productDTO.getProductProperty() : "")
                        .productPropertyId(Objects.nonNull(productDTO) ? productDTO.getProductPropertyId() : "")
                        .currency(soB2cDetailEntity.getCurrency())
                        .price(soB2cDetailEntity.getPrice())
                        .build();
                //根据信息匹配目的国申报价 和海关编码 排除速卖通订单
                ProductCustomsEntity customs = this.getCustomsByCountry(country, soB2cDetailEntity.getSkuId(), productCustomsList);
                if (Objects.nonNull(customs)) {
                    if (Objects.nonNull(customs.getToDeclarePrice()) && customs.getToDeclarePrice().compareTo(BigDecimal.ZERO) > 0) {
                        declareProductDTO.setToDeclarePrice(customs.getToDeclarePrice());
                        declareProductDTO.setToCurrency(customs.getToCurrency());
                        declareProductDTO.setToCurrencySymbol(customs.getToCurrencySymbol());
                    }
                    if (StringUtils.isNotBlank(customs.getCustomsCode())) {
                        declareProductDTO.setToCustomsCode(customs.getCustomsCode());
                    }
                }
                //是速卖通销售平台，sku是组合品时 需要重算申报重量
                if (isAliExpress && Objects.nonNull(skuDTO) && BomTypeEnum.COMBINATION.getType().equals(skuDTO.getType()) && isCombination) {
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS1 = skuChildMap.get(soB2cDetailEntity.getSkuId());
                    List<LogisticsDeclareProductDTO> dtoList = new ArrayList<>();
                    bomChildrenSkuDTOS1.forEach(bomChildrenSkuDTO -> {
                        LogisticsProductDTO.ProductDTO bomProduct = skuMap.get(bomChildrenSkuDTO.getSkuId());
                        if (Objects.nonNull(bomProduct)) {
                            Integer quantity = bomChildrenSkuDTO.getQuantity();
                            if (Objects.isNull(quantity)) {
                                quantity = 1;
                            }
                            LogisticsDeclareProductDTO dto = LogisticsDeclareProductDTO.builder()
                                    .weight(Objects.nonNull(bomProduct.getWeight()) ? bomProduct.getWeight() * quantity : 0)
                                    .grossWeight(Objects.nonNull(bomProduct.getGrossWeight()) ? bomProduct.getGrossWeight().multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO)
                                    .build();
                            dtoList.add(dto);
                        }
                    });
                    declareProductDTO.setGrossWeight(dtoList.stream().map(LogisticsDeclareProductDTO::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add));
                    declareProductDTO.setWeight(dtoList.stream().mapToInt(LogisticsDeclareProductDTO::getWeight).sum());
                }
                declareProductDTOS.add(declareProductDTO);
            }
        });
        return declareProductDTOS;
    }

    /**
     * 修复sku历史成本单价问题
     *
     * @param dto
     */
    @Override
    public void initCostPrice(SoB2cDTO.CostPriceDTO dto) {
        //之前替换sku查询sql时，由于采购单价计算导致了数据使用了plm成本价，现在修复数据
        if (CollectionUtils.isEmpty(dto.getIds()) && Objects.isNull(dto.getStartTime())) {
            return;
        }
        List<SoB2cDetailEntity> soB2cDetailEntities = null;
        //获取需要修复的销售订单
        if (CollectionUtils.isNotEmpty(dto.getIds())) {
            soB2cDetailEntities = soB2cDetailService.listByMainIds(dto.getIds());
        } else if (Objects.nonNull(dto.getStartTime())) {
            LambdaQueryWrapper<SoB2cDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.or(wrapper -> {
                wrapper.ge(SoB2cDetailEntity::getCreateTime, dto.getStartTime());
                wrapper.ge(SoB2cDetailEntity::getUpdateTime, dto.getStartTime());
            });
            soB2cDetailEntities = soB2cDetailService.list(queryWrapper);
        }
        if (CollectionUtils.isEmpty(soB2cDetailEntities)) {
            return;
        }
        List<String> skuIds = soB2cDetailEntities.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //根据sku获取主供应商
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPurchaseBySkuIds(skuIds);
        //根据sku进行map
        Map<String, SkuVO> listMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        //比较信息
        soB2cDetailEntities.forEach(soB2cDetailEntity -> {
            String skuId = soB2cDetailEntity.getSkuId();
            SkuVO skuVO = listMap.get(skuId);
            BigDecimal actualTaxCost = Objects.isNull(skuVO) ? BigDecimal.ZERO : Objects.isNull(skuVO.getActualTaxCost()) ? BigDecimal.ZERO : skuVO.getActualTaxCost();
            if (!Objects.equals(soB2cDetailEntity.getTaxCost(), actualTaxCost)) {
                LambdaUpdateWrapper<SoB2cDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(SoB2cDetailEntity::getTaxCost, actualTaxCost);
                updateWrapper.eq(SoB2cDetailEntity::getId, soB2cDetailEntity.getId());
                soB2cDetailService.update(updateWrapper);
            }
        });
    }

    /**
     * 获取目的国申报信息
     *
     * @param country
     * @param skuId
     * @param productCustomsList
     * @return
     */
    @Override
    public ProductCustomsEntity getCustomsByCountry(String country, String skuId, List<ProductCustomsEntity> productCustomsList) {
        ProductCustomsEntity customs = null;
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(country)) {
            customs = productCustomsList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSkuId()) && org.apache.commons.lang3.StringUtils.isNotEmpty(skuId) && skuId.equals(e.getSkuId())
                    && org.apache.commons.lang3.StringUtils.isNotEmpty(country) && e.getCountry().contains(country)).findFirst().orElse(null);

        }
        //存在默认值时，先取默认值
        if (Objects.isNull(customs)) {
            customs = productCustomsList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSkuId()) && org.apache.commons.lang3.StringUtils.isNotEmpty(skuId) && skuId.equals(e.getSkuId())
                    && org.apache.commons.lang3.StringUtils.isNotEmpty(e.getCountry()) && CommonConstants.DEFAULT.equals(e.getCountry())).findFirst().orElse(null);
        }
        //未匹配到时，获取空值
        if (Objects.isNull(customs)) {
            customs = productCustomsList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSkuId()) && org.apache.commons.lang3.StringUtils.isNotEmpty(skuId) && skuId.equals(e.getSkuId())
                    && org.apache.commons.lang3.StringUtils.isEmpty(e.getCountry())).findFirst().orElse(null);
        }
        return customs;
    }

    private void handleDeclareMatchJson(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> detailList, HashMap<String, Object> map, String logisticsPlatform) {
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (isFullyManagedOrder(soB2cEntity.getDictPlatform())){
            return;
        }
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soB2cEntity.getId());
        if (ObjectUtil.isEmpty(receiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(soB2cEntity.getId());
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        map.put("id", soB2cEntity.getId());
        map.put("code", soB2cEntity.getCode());
        //根据订单明细进行sku拆分
        List<LogisticsDeclareProductDTO> productDTOS = this.splitLogisticsBySoDetail(detailList, soB2cEntity, logisticsPlatform);
        List<Map<String, Object>> mapList = new ArrayList<>(detailList.size());
        productDTOS.forEach(dto -> {
            Map<String, Object> beanToMap = BeanUtil.beanToMap(dto);
            //产品sku信息
            beanToMap.put("skuId", dto.getSkuId());
            beanToMap.put("skuNo", dto.getSkuNo());
            //来源店铺
            beanToMap.put("shop", soB2cEntity.getShopId());
            //来源平台
            beanToMap.put("dictPlatform", soB2cEntity.getDictPlatform());
            //订单目的国家
            beanToMap.put("destCountry", receiverEntity.getCountry());
            //渠道id
            beanToMap.put("logisticsChannelId", logisticsEntity.getLogisticsChannelId());
            //本位币金额
            beanToMap.put("amount", MathUtil.multiplyWithTwo(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            mapList.add(beanToMap);
        });
        map.put("detailList", mapList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO addGift(SoB2cEntity entity, List<SoB2cDTO.GiftDTO> dtoList, SoB2cLogisticsEntity LogisticsEntity, List<SoB2cDetailEntity> detailEntityList) {
        SoB2cDTO.GiftDTO dto = dtoList.stream().filter(e -> StringUtils.isNotBlank(e.getId()) && StringUtils.isNotBlank(e.getCode())).findFirst().orElse(new SoB2cDTO.GiftDTO());
        //待提交和审核不通过的订单允许添加赠品
        if (!(ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) || ApproveStatusEnum.REJECT.equals(entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_STATUS_NOT_ALLOWED);
        }
        //订单明细数据整理
        List<SoB2cDetailEntity> detailList = B2cOrderConverter.INSTANCE.convertB2cDetailByGiftDto(dtoList);
        List<String> skuIds = dtoList.stream().map(SoB2cDTO.GiftDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailDTO.ProductDTO> productDTOS = plmTaskFeign.listProductBySkuIds(skuIds);
        if (CollectionUtils.isNotEmpty(detailList) && CollectionUtils.isNotEmpty(productDTOS)) {
            detailList.forEach(soB2cDetailEntity -> {
                ProductDetailDTO.ProductDTO productDTO = productDTOS.stream().filter(e -> e.getSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(new ProductDetailDTO.ProductDTO());
                soB2cDetailEntity.setImageUrl(productDTO.getImagesUrl());
            });
        }
        //新增订单明细
        soB2cDetailService.saveBatch(detailList);
        //合并订单明细
        List<SoB2cDetailEntity> soB2cDetailEntityList = Stream.concat(detailList.stream(), detailEntityList.stream()).distinct().collect(Collectors.toList());
        //重算尺寸
        calculateSize(LogisticsEntity, soB2cDetailEntityList);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "新增赠品成功");
    }

    /**
     * 计算物流尺寸
     *
     * @param logisticsEntity
     * @param detailList
     */
    private void calculateSize(SoB2cLogisticsEntity logisticsEntity, List<SoB2cDetailEntity> detailList) {
        if (Objects.isNull(logisticsEntity) || CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> skuIds = detailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        //根据明细进行sku拆分
        List<SplitSkuDTO> splitSkuDTOS = this.splitBySoDetail(detailList, skuIds, null, true);
        //拆分完成后根据拆分结果进行汇总
        List<String> keyList = new ArrayList<>();
        keyList.add(CalculateSizeEnum.LENGTH.getCode());
        keyList.add(CalculateSizeEnum.WIDTH.getCode());
        keyList.add(CalculateSizeEnum.HEIGHT.getCode());
        keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
        List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
        Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
        logisticsEntity.setLength(SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS, collect.get(CalculateSizeEnum.LENGTH.getCode())));
        logisticsEntity.setWidth(SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS, collect.get(CalculateSizeEnum.WIDTH.getCode())));
        logisticsEntity.setHeight(SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS, collect.get(CalculateSizeEnum.HEIGHT.getCode())));
        logisticsEntity.setWeight(SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode())));
        soB2cLogisticsService.updateById(logisticsEntity);
    }

    /**
     * 根据销售订单id获取买家信息
     *
     * @param ids
     * @return
     */
    @Override
    public List<SoB2cReceiverDTO.ViewDTO> getReceiverInfo(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        //step1:根据ids获取销售订单列表
        List<SoB2cEntity> soB2cEntities = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            return Collections.emptyList();
        }
        //step2:根据销售订单ids获取收货人信息
        List<SoB2cReceiverEntity> soB2cReceiverEntities = soB2cReceiverService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cReceiverEntities)) {
            return Collections.emptyList();
        }
        //step3：信息整合并返回
        //国家列表
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        List<SoB2cReceiverDTO.ViewDTO> viewDTOList = new ArrayList<>(soB2cReceiverEntities.size());
        soB2cReceiverEntities.forEach(soB2cReceiverEntity -> {
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(e -> e.getId().equals(soB2cReceiverEntity.getMainId())).findFirst().orElse(new SoB2cEntity());
            SoB2cReceiverDTO.ViewDTO viewDTO = B2cOrderConsumerConverter.INSTANCE.convertReceiverToView(soB2cReceiverEntity, soB2cEntity);
            //国家名称
            String countryName = countryList.stream().filter(c -> c.getId().equals(soB2cReceiverEntity.getCountry())).
                    map(DictCountryDTO.ListDTO::getNameCn).findFirst().orElse("");
            viewDTO.setCountryName(countryName);
            viewDTOList.add(viewDTO);
        });
        return viewDTOList;
    }

    /**
     * 更新买家信息
     *
     * @param dto
     * @return
     */
    @Override
    public BatchResultDTO updateReceiverInfo(SoB2cReceiverDTO.UpdateBaseDTO dto) {
        //step1 获取销售订单信息
        SoB2cEntity entity = this.getById(dto.getMainId());
        if (Objects.isNull(entity)) {
            return BatchResultDTO.fail(dto.getMainId(), dto.getMainId(), ApiError.ERROR_92016.msg);
        }
        //待提交和审核不通过的订单允许修改买家信息
        if (!(ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) || ApproveStatusEnum.REJECT.equals(entity.getApproveStatus()))) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "非待提交和审核不通过的订单不允许修改买家信息");
        }
        //step2 获取买家信息
        SoB2cReceiverEntity old = soB2cReceiverService.getById(dto.getId());
        if (Objects.isNull(old)) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), String.format(ApiError.NOT_EXIST_BILL.msg, "B2C销售订单买家信息表"));
        }
        //step3 更新买家物流信息
        SoB2cReceiverEntity receiver = B2cOrderConsumerConverter.INSTANCE.convertUpdateReceiverByDto(dto, old);
        soB2cReceiverService.updateFieldById(receiver);
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", entity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, receiver, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), null, msg, "修改买家信息");
        //step4 销售订单打标
        this.updateChangeReceiverAddressById(entity.getId(), Boolean.TRUE);
        return BatchResultDTO.success(dto.getMainId(), entity.getCode(), "修改买家信息成功");
    }

    /**
     * 更新销售订单是否修复地址标识
     *
     * @param id
     * @param flag
     */
    private void updateChangeReceiverAddressById(String id, Boolean flag) {
        if (CharSequenceUtil.isNotBlank(id) && Objects.nonNull(flag)) {
            this.lambdaUpdate().set(SoB2cEntity::getIsChangeReceiverAddress, flag).eq(SoB2cEntity::getId, id).update();
        }
    }

    /**
     * 获取忽略库存计算的sku
     *
     * @return 返回忽略的SKU ID列表
     */
    protected List<String> getIgnoreSkuIds() {
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if (CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        return ignoreInventorySkuIds;
    }

    /**
     * 拉取订单 -- dmp创建任务拉取
     *
     * @param ids
     * @author jack
     */
    @Override
    public List<BatchResultDTO> fetchOrder(List<String> ids) {
        ids = ids.stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException("ids不能为空");
        }
        if (ids.size() > 50) {
            throw new ServiceException("批量刷新限制50条");
        }
        // 查询实体列表
        List<SoB2cEntity> soB2cEntities = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            throw new ServiceException("未查询到订单数据");
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<SoB2cEntity> selfAddList = soB2cEntities.stream().filter(v -> v.getSourceType().equals(SourceTypeEnum.SELF_ADD.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(selfAddList)) {
            selfAddList.forEach(r -> {
                BatchResultDTO result = BatchResultDTO.fail(r.getId(), r.getCode(), "请勿选择手工订单");
                resultDTOS.add(result);
            });
        }
        //只有订单异常订单类型为“订单拉取失败”的订单允许手动刷新，触发订单重新拉取
        List<SoB2cErrorEntity> soB2cErrors = soB2cErrorService.getByMainIdsAndType(ids, SoB2cErrorTypeEnum.ORDER_FETCH.getCode());
        if (CollectionUtils.isEmpty(soB2cErrors)) {
            throw new ServiceException("只有订单异常订单类型为“订单拉取失败”的订单允许手动刷新");
        }
        Map<String, String> errorMap = soB2cErrors.stream().collect(Collectors.toMap(SoB2cErrorEntity::getMainId, SoB2cErrorEntity::getId));
        soB2cEntities = soB2cEntities.stream().filter(v -> !v.getSourceType().equals(SourceTypeEnum.SELF_ADD.getCode())).collect(Collectors.toList());


        List<SoB2cEntity> noErrorList = soB2cEntities.stream().filter(v -> !errorMap.containsKey(v.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noErrorList)) {
            noErrorList.forEach(r -> {
                BatchResultDTO result = BatchResultDTO.fail(r.getId(), r.getCode(), "只有订单异常订单类型为“订单拉取失败”的订单允许手动刷新");
                resultDTOS.add(result);
            });
        }

        soB2cEntities = soB2cEntities.stream().filter(v -> errorMap.containsKey(v.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            return resultDTOS;
        }

        // 构建 DTO 列表
        List<DmpInoutDTO.CreateInputDTO> createDTOList = SoB2cHandler.groupConvertCreateInputDTOList(soB2cEntities);

        // 调用远程任务接口
        if (Boolean.TRUE.equals(dmpInoutTaskFeign.doInputTask(createDTOList))) {
            soB2cEntities.forEach(r -> {
                BatchResultDTO result = BatchResultDTO.success(r.getId(), r.getCode(), "创建任务成功");
                resultDTOS.add(result);
            });
        } else {
            soB2cEntities.forEach(r -> {
                BatchResultDTO result = BatchResultDTO.fail(r.getId(), r.getCode(), "创建任务失败");
                resultDTOS.add(result);
            });
        }
        return resultDTOS;
    }


    @Override
    public Boolean tempTikTokOrderDate() {
        //查询待修复已删除数据
        List<SoB2cDetailEntity> soB2cDetailEntityList = baseMapper.listTikTokOrder();

        List<String> mainIds = soB2cDetailEntityList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());


        List<SoB2cDetailEntity> soB2cDetailEntityList1 = soB2cDetailService.listByMainIds(mainIds);


        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            List<SoB2cDetailEntity> collect = soB2cDetailEntityList1.stream().filter(req -> req.getMainId().equals(soB2cDetailEntity.getMainId())
                    && req.getSkuId().equals(soB2cDetailEntity.getSkuId())).collect(Collectors.toList());
            //多个重复
            List<SoB2cDetailEntity> aNull = collect.stream().filter(req -> CharSequenceUtil.isNotBlank(req.getPlatformLineNumber())
                    && CharSequenceUtil.isNotBlank(req.getPlatformPackageId())
            ).collect(Collectors.toList());

            if (CollUtil.isNotEmpty(aNull)) {
                soB2cDetailEntity.setSourceDetailId(aNull.get(0).getSourceDetailId());
                soB2cDetailEntity.setPlatformLineNumber(aNull.get(0).getPlatformLineNumber());
                soB2cDetailEntity.setPlatformPackageId(aNull.get(0).getPlatformPackageId());
                baseMapper.tikTokOrderUpdateDetail(soB2cDetailEntity.getId(),
                        soB2cDetailEntity.getSourceDetailId(),
                        soB2cDetailEntity.getPlatformLineNumber(),
                        soB2cDetailEntity.getPlatformPackageId());
            } else {
                baseMapper.tikTokOrderUpdate(soB2cDetailEntity.getId());
            }

            for (SoB2cDetailEntity b2cDetailEntity : collect) {
                soB2cDetailService.lambdaUpdate().eq(SoB2cDetailEntity::getId, b2cDetailEntity.getId()).remove();
            }

        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cDTO.LogisticsDTO> getB2cLogisticsByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        return baseMapper.getB2cLogisticsByIds(ids);
    }

    @Async("omsErpExecutor")
    @Override
    public void autoCalcEstimatedShippingCost(List<String> sob2cIds) {
        if(CollUtil.isEmpty(sob2cIds)){
            return;
        }
        List<SoB2cDTO.LogisticsDTO> list = this.getB2cLogisticsByIds(sob2cIds);
        if (CollUtil.isEmpty(list)){
            return;
        }
        //根据条件过滤
        List<SoB2cDTO.LogisticsDTO> logisticsDTOS = list.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getWarehouseId()) && CharSequenceUtil.isNotBlank(e.getLogisticsChannelId()) && Objects.nonNull(e.getWeight()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(logisticsDTOS)){
            return;
        }
        //根据发货仓库进行
        for (String soId : sob2cIds){
            SoB2cDTO.LogisticsDTO logisticsDTO = logisticsDTOS.stream().filter(e -> CharSequenceUtil.isNotBlank(soId) && soId.equals(e.getId())).findFirst().orElse(null);
            if (Objects.isNull(logisticsDTO)){
                continue;
            }
            if (!CharSequenceUtil.isAllNotBlank(logisticsDTO.getWarehouseId(),logisticsDTO.getCountry(),logisticsDTO.getLogisticsChannelId(),logisticsDTO.getWeightUnit())){
                continue;
            }
            if (Objects.isNull(logisticsDTO.getWeight())){
                continue;
            }
            if (CharSequenceUtil.isBlank(logisticsDTO.getWarehouseId()) || CharSequenceUtil.isBlank(logisticsDTO.getCountry())){
                continue;
            }
            ShippingCalculationDTO.PagingParamDTO pagingParamDTO = ShippingCalculationDTO.PagingParamDTO.builder()
                    .b2cSoId(logisticsDTO.getId()).fromWarehouseId(logisticsDTO.getWarehouseId())
                    .channelIdList(Collections.singletonList(logisticsDTO.getLogisticsChannelId()))
                    .weight(logisticsDTO.getWeight()).weightUnit(logisticsDTO.getWeightUnit()).postCode(logisticsDTO.getPostCode())
                    .length(logisticsDTO.getLength()).width(logisticsDTO.getWidth()).height(logisticsDTO.getHeight())
                    .toCountryList(Collections.singletonList(logisticsDTO.getCountry()))
                    .build();
            try {
                logisticsFeign.updateShippingCalculation(pagingParamDTO);
            }catch (Exception e){
                log.error("更新销售订单预估运费异常：{}", e.getMessage());
            }
        }
    }

    @Override
    public void updateOverEstimatedShipCost(String b2cSoId, Boolean isOverEstimatedShipCost) {
        if(Objects.isNull(isOverEstimatedShipCost) || CharSequenceUtil.isBlank(b2cSoId)){
            return;
        }
        this.lambdaUpdate().eq(SoB2cEntity::getId, b2cSoId).set(SoB2cEntity::getIsOverEstimatedShipCost, isOverEstimatedShipCost).update();
    }


    @Override
    public List<SoB2cEntity> queryToSdy(LocalDate startDate, LocalDate endDate, Integer pageSize, int offset, List<String> platformList) {
        return baseMapper.queryToSdy(startDate, endDate, pageSize, offset, platformList);
    }

    @Override
    public void writeBackSoOutstockDate(String soId, String soOutstockDate) {
        if(StringUtils.isBlank(soId) || null == soOutstockDate){
            return ;
        }
        lambdaUpdate()
                .set(SoB2cEntity::getSoOutstockDate,LocalDate.parse(soOutstockDate))
                .eq(SoB2cEntity::getId,soId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadLogisticLabel(SoB2cDTO.UploadFileDTO dto) throws IOException {
        SoB2cEntity soB2cEntity = this.getById(dto.getId());
        if(Objects.isNull(soB2cEntity)){
            throw new ServiceException("销售订单不存在");
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = this.soB2cLogisticsService.getByMainId(dto.getId());
        if(Objects.isNull(soB2cLogisticsEntity)){
            throw new ServiceException("销售订单物流信息不存在");
        }
        if(StringUtils.isNotBlank(soB2cLogisticsEntity.getCode()) && soB2cLogisticsEntity.getSourceSystem().equals(SoB2cLogisticSourceSystemEnum.THIRD.getCode())){
            throw new ServiceException("已获取第三方物流单号不支持上传物流面单");
        }
        MultipartFile multipartFile = dto.getFile();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }
        // 获取文件的内容类型并检查是否为PDF
        if(!"application/pdf".equals(multipartFile.getContentType())){
            throw new ServiceException("文件格式不正确，请上传PDF格式的文件");
        }
        String base64 = FileUtil.convertToBase64AndCheckIfPdf(multipartFile);
        String prefix = "data:application/pdf;base64,";
        soB2cLabelService.ManualUploadLabel(prefix + base64,dto.getId());
        String msg = CharSequenceUtil.format("用户【{}】上传文件名为【{}】的物流面单 ", UserContext.getDefaultLoginUser().getUserName(), multipartFile.getOriginalFilename());

        operateLogService.addModuleOperateLog(msg ,ModuleTypeEnum.SO_B2C.getCode(), dto.getId(), "上传面单");

        return "";
    }

    @Override
    public IPage<?> productSalesPaging(Page<T> query, ReportDTO.ProductSalesPagingParamDTO params, List<String> skuIdList) {
        return baseMapper.productSalesPaging(query, params, skuIdList);
    }

    @Override
    public Page<ReportDTO.ProductSalesPagingViewDTO> listProductSalesExport(Page<ReportDTO.ProductSalesPagingViewDTO> query, ReportDTO.ProductSalesPagingParamDTO params, List<String> skuIdList) {
        return baseMapper.listProductSalesExport(query, params, skuIdList);
    }

    @Override
    public void updateFbaNotVatInvoice(String shopId, LocalDateTime enableTime, String vatInvoiceStatus) {
        if (CharSequenceUtil.isBlank(shopId) || CharSequenceUtil.isBlank(vatInvoiceStatus) || Objects.isNull(enableTime)){
            return;
        }
        baseMapper.updateFbaNotVatInvoice(shopId, enableTime, vatInvoiceStatus);
    }

    @Override
    public void updateNfeInvoiceStatus(String soId, String nfeInvoiceStatus) {
        lambdaUpdate().eq(SoB2cEntity::getId,soId).set(SoB2cEntity::getNfeInvoiceStatus,nfeInvoiceStatus).update();
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新订单开票状态为：{}",SoB2cNfeStatusEnum.getName(nfeInvoiceStatus)), ModuleTypeEnum.SO_B2C.getCode(), soId,"更新开票状态");

    }

    @Override
    public String getPartitionId(String soId, String platform) {
        if (soB2cService.isFullyManagedOrder(platform)){
            SoB2cExtendEntity extendEntity = soB2cExtendService.getByMainId(soId);
            return Objects.nonNull(extendEntity)? extendEntity.getPartitionId():"";
        }else {
            SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soId);
            return Objects.nonNull(receiverEntity)? receiverEntity.getPartitionId():"";
        }
    }

    @Override
    public List<SoB2cDTO.DeliveryDTO> listDeliveryOrderByParam(List<String> billStatusList, List<String> platformStatusList, List<String> platformList, List<String> codeList) {
        if (CollUtil.isEmpty(platformList)){
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryOrderByParam(billStatusList, platformStatusList, platformList,codeList);
    }

    @Override
    public void updateExtendData(String id, SoB2cDTO.ExtendDataDTO extendDataDTO) {
        baseMapper.updateExtendData(id, JSONUtil.toJsonStr(extendDataDTO));
    }

    @Override
    public void uploadLogisticsStatus(SoB2cDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoB2cEntity entity = this.getById(id);
        if (Objects.isNull(entity)){
            return;
        }
        if (CharSequenceUtil.isNotBlank(dto.getLogisticsDTO().getLogisticsChannelId()) && !entity.getIsMatchLogisticsRule()){
            this.lambdaUpdate().set(SoB2cEntity::getIsMatchLogisticsRule,Boolean.TRUE).eq(SoB2cEntity::getId,id).update();
        }
    }

    @Override
    public List<SoB2cEntity> listWaitShipByWarehouseIds(List<String> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)){
            return Collections.emptyList();
        }
        return baseMapper.listWaitShipByWarehouseIds(warehouseIds);
    }

    @Override
    public void updateAmount(String id, BigDecimal amount) {
        if (CharSequenceUtil.isBlank(id) || Objects.isNull(amount)){
            return;
        }
        this.lambdaUpdate().set(SoB2cEntity::getAmount,amount).eq(SoB2cEntity::getId,id).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearOutDateBySoIds(List<String> clearOutDateSoIds) {
        if(CollectionUtils.isEmpty(clearOutDateSoIds) ){
            return ;
        }
        lambdaUpdate()
                .set(SoB2cEntity::getSoOutstockDate,null)
                .in(SoB2cEntity::getId,clearOutDateSoIds)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSoB2cDistribution(SoMultiChannelEntity soMultiChannelEntity) {
        SoB2cEntity entity = this.getById(soMultiChannelEntity.getSoId());
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单");
        }
        this.lambdaUpdate()
                .set(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode())
                .set(SoB2cEntity::getMultiChannelType, SoB2cMultiChannelTypeEnum.AMAZON_APPROVING.getCode())
                .set(SoB2cEntity::getIsMatchLogisticsRule, Boolean.TRUE)
                .eq(SoB2cEntity::getId,entity.getId()).update();
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新订单状态由【{}】改为【{}】",SoB2cBillStatusEnum.getName(entity.getBillStatus()),SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getName()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(),"亚马逊多渠道发货");
        //更新渠道
//        soB2cLogisticsService.lambdaUpdate().set(SoB2cLogisticsEntity::getLogisticsChannelId, soMultiChannelEntity.getLogisticsChannelId())
//                .set(SoB2cLogisticsEntity::getLogisticsChannelName, soMultiChannelEntity.getLogisticsChannelName())
//                .eq(SoB2cLogisticsEntity::getMainId, soMultiChannelEntity.getSoId()).update();
//        operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新订单物流渠道改为【{}】", soMultiChannelEntity.getLogisticsChannelName()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(),"亚马逊多渠道发货");
        //更新发货仓库
//        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(soMultiChannelEntity.getDeliveryWarehouseId()));
//        if (CollUtil.isEmpty(warehouseList)){
//            throw new ServiceException("发货仓库【{}】不存在", soMultiChannelEntity.getDeliveryWarehouseName());
//        }
//        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
//        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
//        soB2cDetailService.lambdaUpdate()
//                .set(SoB2cDetailEntity::getWarehouseId, soMultiChannelEntity.getDeliveryWarehouseId())
//                .set(SoB2cDetailEntity::getWarehouseName, soMultiChannelEntity.getDeliveryWarehouseName())
//                .set(SoB2cDetailEntity::getIsMatchWarehouseRule, Boolean.TRUE)
//                .set(CollUtil.isNotEmpty(accountingCompanyList), SoB2cDetailEntity::getWarehouseOrgId, accountingCompanyList.get(0).getId())
//                .set(CollUtil.isNotEmpty(accountingCompanyList), SoB2cDetailEntity::getWarehouseOrgName, accountingCompanyList.get(0).getName())
//                .eq(SoB2cDetailEntity::getMainId,soMultiChannelEntity.getSoId()).update();
//        operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新订单发货仓库改为【{}】,仓库组织【{}】",soMultiChannelEntity.getDeliveryWarehouseName(),CollUtil.isNotEmpty(accountingCompanyList) ? accountingCompanyList.get(0).getName()
//                : ""), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(),"亚马逊多渠道发货");

    }

    @Override
    public List<PackagePlanDTO.SoB2cDTO> packagePlanPreview(List<String> soIds) {
        List<PackagePlanDTO.SoB2cDTO> soB2cDTOS = baseMapper.packagePlanPreview(soIds);
        if (CollUtil.isEmpty(soB2cDTOS)){
            return Collections.emptyList();
        }
        List<String> shopIds = soB2cDTOS.stream().map(PackagePlanDTO.SoB2cDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.lambdaQuery().select(ShopInfoEntity::getId, ShopInfoEntity::getName).in(ShopInfoEntity::getId, shopIds).list();
        Map<String, String> shopInfoMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        soB2cDTOS.forEach(soB2cDTO -> {
            String shopName = shopInfoMap.get(soB2cDTO.getShopId());
            if (Objects.nonNull(shopName)){
                soB2cDTO.setShopName(shopName);
            }
        });
        return soB2cDTOS;
    }

    @Override
    public BatchResultDTO retryPackagePlan(String soId) {
        SoB2cEntity entity = this.getById(soId);
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单");
        }
        List<WorkflowTaskRecordEntity> workflowTaskRecordEntities = workflowTaskRecordService.listBySourceId(soId, WorkflowTaskRecordTypeEnum.PACKAGE_PLAN_GENERATE.getCode());
        if (CollUtil.isEmpty(workflowTaskRecordEntities)){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "订单不存在组包任务");
        }
        WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
        addTaskDTO.setSourceId(entity.getId());
        addTaskDTO.setSourceCode(entity.getCode());
        addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
        addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.PACKAGE_PLAN_GENERATE);
        addTaskDTO.setTraceId(workflowTaskRecordEntities.get(0).getTraceId());
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, soId,1);
        if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
            XxlJobHelper.log(StrUtil.format("展会订单任务节点记录补偿重试MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "重试组包任务");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO getLogisticsLabel(SoB2cEntity entity, SoB2cLogisticsEntity soB2cLogisticsEntity) {
        if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "仅支持配货中重新获取面单");
        }
        if (CharSequenceUtil.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "物流渠道不存在，获取失败");
        }
        if (CharSequenceUtil.isBlank(soB2cLogisticsEntity.getCode())){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "物流运单号不存在，获取失败");
        }
        LogisticsBillDTO.PrintLogisticsWaybillDTO dto = new LogisticsBillDTO.PrintLogisticsWaybillDTO();
        dto.setB2cSoId(entity.getId());
        dto.setDeliveryNo(entity.getPlatformCode());
        dto.setShopId(entity.getShopId());
        dto.setChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        dto.setTransportNo(soB2cLogisticsEntity.getCode());
        dto.setLogisticType(soB2cLogisticsEntity.getLogisticType());
        return logisticsBillFeign.getLogisticsLabel(dto);
    }

    @Override
    public void importB2cFile(MultipartFile excelFile, HttpServletResponse response) {
        B2CSoImportExcelListener excelListenerUtil = new B2CSoImportExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), B2CSoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<B2CSoImportExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
            if (CollectionUtils.isEmpty(excelDateList)) {
                throw new ServiceException(ApiError.ERROR_95123);
            }
            //错误的
            List<B2CSoImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            //数据验证
            List<B2CSoImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            //处理验证成功数据
            handleImportSuccessList(successList, errorList);
            if (errorList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/b2csoExportError.xlsx";
                String name = "B2CSo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    //排序
                    errorList.sort(Comparator.comparingInt(dto -> Integer.parseInt(dto.getNo())));
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.ERROR_95125);
                }
            }
        } catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
    }

    private void handleImportSuccessList(List<B2CSoImportExcelDTO> successList, List<B2CSoImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //去掉前后空格
        successList.forEach(dto -> {
            for (java.lang.reflect.Field field : B2CSoImportExcelDTO.class.getDeclaredFields()) {
                if (field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    try {
                        String value = (String) field.get(dto);
                        if (value != null) {
                            field.set(dto, value.trim());
                        }
                    } catch (IllegalAccessException e) {
                        log.error("Failed to trim field: {}", field.getName(), e);
                    }
                }
            }
        });

        //销售平台
        List<DictBasicDTO.ViewDTO> salesPlatformList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        Map<String, String> salesPlatformMap = salesPlatformList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getName, DictBasicDTO.ViewDTO::getValue, (oldValue, newValue) -> oldValue));

        //授权店铺
        List<String> salesPlatformNames = successList.stream().filter(v -> StringUtils.isNotBlank(v.getDictPlatformName())).map(B2CSoImportExcelDTO::getDictPlatformName).distinct().collect(Collectors.toList());
        List<String> salesPlatformIds = new ArrayList<>();
        for (String salesPlatformName : salesPlatformNames) {
            salesPlatformIds.add(salesPlatformMap.get(salesPlatformName));
        }
        List<String> shopNameList = successList.stream().map(B2CSoImportExcelDTO::getShopName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoDTO.ListDTO> shopInfoAuthList = shopInfoService.listShopByName(shopNameList);
        Map<String, ShopInfoDTO.ListDTO> shopInfoAuthMap = shopInfoAuthList.stream().collect(Collectors.toMap(ShopInfoDTO.ListDTO::getName, v -> v, (oldValue, newValue) -> oldValue));
        //币别
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        //订单分类
        List<OrderCategoryDetailDTO.ListDTO> orderCategoryList = orderCategoryDetailService.listOrderCategory();
        Map<String, String> orderCategoryMap = orderCategoryList.stream().collect(Collectors.toMap(OrderCategoryDetailDTO.ListDTO::getName, OrderCategoryDetailDTO.ListDTO::getId, (oldValue, newValue) -> oldValue));
        //物流渠道
        List<BaseDropDownDTO.DisabledDTO> logisticsList = logisticsFeign.listAll();
        Map<String, String> logisticsMap = logisticsList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getValue, BaseDropDownDTO.DisabledDTO::getCode, (oldValue, newValue) -> oldValue));
        //国家
        List<String> countryNames = successList.stream().map(B2CSoImportExcelDTO::getCountryName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByNames(countryNames);
        Map<String, String> countryMap = countryList.stream().collect(Collectors.toMap(DictCountryEntity::getNameCn, DictCountryEntity::getId, (oldValue, newValue) -> oldValue));
        //仓库
        List<String> warehouseNameList = successList.stream().map(B2CSoImportExcelDTO::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = wmsTaskFeign.listWarehouseByNameList(warehouseNameList);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, WarehouseDTO.ListDTO::getId, (oldValue, newValue) -> oldValue));
        //B2C客户
        List<String> customerNameList = successList.stream().map(B2CSoImportExcelDTO::getCustomerName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<CustomerB2CDTO.DropListDTO> b2cCustomerList = customerB2cService.customerListByName(customerNameList);
        Map<String, CustomerB2CDTO.DropListDTO> b2cCustomerMap = b2cCustomerList.stream().collect(Collectors.toMap(CustomerB2CDTO.DropListDTO::getCustomerName, v -> v, (oldValue, newValue) -> oldValue));

        //客户SKU
        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = successList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getSkuNo())).map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(),"","")).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> skuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);
        Map<String, SkuMappingDTO.ListSkuDTO> skuNoMap = new HashMap<>();
        Map<String, SkuVO> skuVoNoMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuMappingList)){
            skuNoMap = skuMappingList.stream().collect(Collectors.toMap(SkuMappingDTO.ListSkuDTO::getProductSkuNo, v -> v, (oldValue, newValue) -> oldValue));
        }
        List<String> skuNoList = successList.stream().map(B2CSoImportExcelDTO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNoList);
        if (CollUtil.isNotEmpty(skuVOS)){
            skuVoNoMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, v -> v, (oldValue, newValue) -> oldValue));
        }

        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();

        //校验整数或者小数正则
        String regex = "^[0-9]+(\\.[0-9]+)?$";

        //订单类型
        Map<String, String> orderSubTypeMap = dictBasicService.getByKey("orderSubType").stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getName, DictBasicDTO.ViewDTO::getValue, (oldValue, newValue) -> oldValue));


        // 根据序号分组
        Map<String, List<B2CSoImportExcelDTO>> groupedByNo = successList.stream().collect(Collectors.groupingBy(B2CSoImportExcelDTO::getNo));
        for (Map.Entry<String, List<B2CSoImportExcelDTO>> entry : groupedByNo.entrySet()) {
            List<String> errorMsgList = new ArrayList<>();
            String no = entry.getKey();
            if(!no.matches("\\d+") || no.equals("0")){
                errorMsgList.add("序号异常，请填入正整数");
            }

            List<B2CSoImportExcelDTO> list = entry.getValue();

            SoB2cEntity soB2cEntity = new SoB2cEntity();
            soB2cEntity.setId(IdWorker.getIdStr());
            // 生成单号
            String businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SO_B2C);
            soB2cEntity.setCode(businessNo);
            soB2cEntity.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
            //创建时间
            soB2cEntity.setCreateTime(nowTime);
            //单据时间
            soB2cEntity.setBillDate(nowDate);
            B2CSoImportExcelDTO mainInfo = list.get(0);
            //平台订单号
            int platformCodeCount = list.stream().map(B2CSoImportExcelDTO::getPlatformCode).distinct().collect(Collectors.toList()).size();
            if(platformCodeCount > 1){
                errorMsgList.add("不同平台单号不允许导入同一个订单，请修改序号");
            }else {
                soB2cEntity.setPlatformCode(mainInfo.getPlatformCode());
            }
            //销售平台
            String dictPlatformName = mainInfo.getDictPlatformName();
            if(StringUtils.isNotBlank(salesPlatformMap.get(dictPlatformName))){
                soB2cEntity.setDictPlatform(salesPlatformMap.get(dictPlatformName));
            }else {
                errorMsgList.add("平台未找到");
            }
            //店铺
            ShopInfoDTO.ListDTO shopInfoEntity = shopInfoAuthMap.get(mainInfo.getShopName());
            if(Objects.nonNull(shopInfoEntity)){
                soB2cEntity.setShopId(shopInfoEntity.getId());
                soB2cEntity.setShopName(shopInfoEntity.getName());
                soB2cEntity.setOrgId(shopInfoEntity.getSalesOrgId());
                soB2cEntity.setOrgName(shopInfoEntity.getSalesOrgName());
            }else {
                errorMsgList.add(ApiError.SHOP_NOT_EXIST_NO_PERMISSION.msg);
            }
            //订单金额
            if(mainInfo.getAmount().matches(regex)){
                soB2cEntity.setAmount(MathUtil.getBigDecimalByStr(mainInfo.getAmount()));
            }else {
                errorMsgList.add("订单金额包含非数字字符");
            }
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(soB2cEntity.getDictPlatform())){
                // 速卖通手工订单税后金额=订单金额
                soB2cEntity.setAfterTaxAmount(soB2cEntity.getAmount());
            } else {
                // 其他平台=无
                soB2cEntity.setAfterTaxAmount(BigDecimal.ZERO);
            }
            //币别
            String currencyStr = mainInfo.getCurrency();
            DictCurrencyEntity currencyEntity = currencyList.stream().filter(c -> c.getId().equals(currencyStr)).
                    findFirst().orElse(null);
            String currency = "";
            if (Objects.isNull(currencyEntity)) {
                errorMsgList.add("币种未找到");
            } else {
                currency = currencyEntity.getId();
                soB2cEntity.setCurrency(currency);
                // 人民币
                if(currency.equals(CurrencyEnum.CNY.getCurrencyCode())){
                    soB2cEntity.setExchangeRate(BigDecimal.ONE);
                }else {
                    BigDecimal exchangeRate = dmpTaskFeign.getRate(nowTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), currency);
                    if (MathUtil.compareTo(exchangeRate, MathUtil.ZERO) == MathUtil.ZERO) {
                        errorMsgList.add(String.format(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST.msg, soB2cEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency()));
                    }else {
                        soB2cEntity.setExchangeRate(null == exchangeRate ? BigDecimal.ZERO : exchangeRate);
                    }
                }
            }

            //付款时间
            String payTimeStr = mainInfo.getPayTime();
            if (StringUtils.isNotBlank(payTimeStr)) {
                soB2cEntity.setPayTime(LocalDateUtil.parseStrToLocalTime(payTimeStr));
                //付款状态（待付款、已付款）： 付款时间不为空则已付款
                soB2cEntity.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            }
            //卖家订单编号
            String sellerOrderCode = mainInfo.getSellerOrderCode();
            if(StringUtils.isNotBlank(sellerOrderCode)){
                soB2cEntity.setSellerOrderCode(sellerOrderCode);
            }

            //订单类型
            if(orderSubTypeMap.containsKey(mainInfo.getTransactionSubType())){
                soB2cEntity.setTransactionSubType(orderSubTypeMap.get(mainInfo.getTransactionSubType()));
            }else {
                errorMsgList.add("订单类型未找到");
            }

            //订单备注
            soB2cEntity.setRemark(mainInfo.getRemark());

            //买家
            SoB2cReceiverDTO.AddDTO receiverDTO = new SoB2cReceiverDTO.AddDTO();
            BeanMapper.copy(mainInfo, receiverDTO);
            if(StringUtils.isNotBlank(mainInfo.getCustomerName()) && b2cCustomerMap.containsKey(mainInfo.getCustomerName())) {
                CustomerB2CDTO.DropListDTO customerDTO = b2cCustomerMap.get(mainInfo.getCustomerName());
                receiverDTO.setCustomerId(customerDTO.getCustomerId());
            }
            //国家
            if(StringUtils.isNotBlank(countryMap.get(mainInfo.getCountryName()))){
                receiverDTO.setCountry(countryMap.get(mainInfo.getCountryName()));
            }else {
                errorMsgList.add("国家未找到");
            }

            //物流渠道
            SoB2cLogisticsDTO.AddDTO b2cLogisitics = new SoB2cLogisticsDTO.AddDTO();
            if(StringUtils.isNotBlank(mainInfo.getLogisticsChannelName()) && StringUtils.isNotBlank(logisticsMap.get(mainInfo.getLogisticsChannelName()))){
                b2cLogisitics.setLogisticsChannelId(logisticsMap.get(mainInfo.getLogisticsChannelName()));
            }
            //跟踪号
            b2cLogisitics.setCode(mainInfo.getTrackNo());
            //包装尺寸(长*宽*高)
            if(StringUtils.isNotBlank(mainInfo.getPackSize())){
                String[] split = mainInfo.getPackSize().split("\\*");
                if(split.length == 3){
                    boolean allNumeric = true;
                    for (String s : split) {
                        if (!s.trim().matches(regex)) { // 匹配整数或小数
                            allNumeric = false;
                            break;
                        }
                    }
                    if (allNumeric) {
                        b2cLogisitics.setLength(MathUtil.getBigDecimalByStr(split[0].trim()));
                        b2cLogisitics.setWidth(MathUtil.getBigDecimalByStr(split[1].trim()));
                        b2cLogisitics.setHeight(MathUtil.getBigDecimalByStr(split[2].trim()));
                    } else {
                        errorMsgList.add("包装尺寸(长*宽*高)包含非数字字符");
                    }
                }else {
                    errorMsgList.add("包装尺寸(长*宽*高)格式异常");
                }

            }
            //预估运费
            if(StringUtils.isNotBlank(mainInfo.getEstimatedShippingCost()) ){
                if(mainInfo.getEstimatedShippingCost().matches(regex)){
                    b2cLogisitics.setEstimatedShippingCost(MathUtil.getBigDecimalByStr(mainInfo.getEstimatedShippingCost()));
                }else {
                    errorMsgList.add("预估运费包含非数字字符");
                }

            }
            //实际运费
            if(StringUtils.isNotBlank(mainInfo.getActualShippingCost())){
                if(mainInfo.getActualShippingCost().matches(regex)){
                    b2cLogisitics.setActualShippingCost(MathUtil.getBigDecimalByStr(mainInfo.getActualShippingCost()));
                }else {
                    errorMsgList.add("实际运费包含非数字字符");
                }
            }

            //根据渠道和(国家+邮编）判断订单是否超范围配送
            Boolean isOutOfRangeDelivery = estimateIsOutOfRangeDelivery(soB2cEntity.getId(),
                    receiverDTO.getCountry(),
                    receiverDTO.getPostCode(),
                    b2cLogisitics.getLogisticsChannelId());
            soB2cEntity.setIsOutOfRangeDelivery(isOutOfRangeDelivery);

            //明细信息
            List<SoB2cDetailEntity> detailList = new ArrayList<>();
            for (B2CSoImportExcelDTO detail : list) {
                List<String> msgList = new ArrayList<>();
                SoB2cDetailEntity detailEntity = new SoB2cDetailEntity();
                String skuNo = detail.getSkuNo();
                SkuMappingDTO.ListSkuDTO skuVO = skuNoMap.get(skuNo);
                if(Objects.isNull(skuVO) || !skuVoNoMap.containsKey(skuNo)){
                    msgList.add("sku未找到");
                }else {
                    detailEntity.setSkuId(skuVO.getProductSkuId());
                    detailEntity.setSkuNo(skuVO.getProductSkuNo());
                    //正则判断qty是整整数
                    if(mainInfo.getQty().matches("\\d+")){
                        detailEntity.setQty(MathUtil.valueOfInteger(detail.getQty()));
                    }else {
                        msgList.add("数量包含非数字字符");
                    }
                    if(mainInfo.getDetailAmount().matches(regex)){
                        BigDecimal price = MathUtil.getBigDecimalByStr(detail.getDetailAmount());
                        detailEntity.setPrice(price);
                        BigDecimal qty = MathUtil.getBigDecimalByStr(detail.getQty());
                        detailEntity.setAmount(price.multiply(qty));
                    }else {
                        msgList.add("真实售价包含非数字字符");
                    }
                    detailEntity.setCurrency(currency);
                    detailEntity.setExchangeRate(soB2cEntity.getExchangeRate());
                    detailEntity.setAdvicePrice(skuVO.getAdvicePrice());
                    detailEntity.setTaxCost(skuVO.getTaxCost());
                    detailEntity.setCostSource(skuVO.getCostSource());
                    if(StringUtils.isNotBlank(detail.getWarehouseName())){
                        String warehouseId = warehouseMap.get(detail.getWarehouseName());
                        if(StringUtils.isNotBlank(warehouseId)){
                            detailEntity.setWarehouseId(warehouseId);
                            detailEntity.setWarehouseName(detail.getWarehouseName());
                        }else {
                            msgList.add(ApiError.WAREHOUSE_NOT_EXIST_NO_PERMISSION.msg);
                        }
                    }
                    detailEntity.setMainId(soB2cEntity.getId());
                    detailList.add(detailEntity);
                }
                if (CollectionUtils.isNotEmpty(msgList) || CollectionUtils.isNotEmpty(errorMsgList)) {
                    List<String> itemErrorList = Stream.concat(errorMsgList.stream(),msgList.stream()).distinct().collect(Collectors.toList());
                    detail.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                    errorList.add(detail);
                }
            }
            try {
                //错误的编号集合
                List<String> errorNoList = errorList.stream().map(B2CSoImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                //表示可以添加
                if (!errorNoList.contains(no)) {

                    soB2cService.save(soB2cEntity);
                    // 操作日志
                    String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C销售订单表", soB2cEntity.getCode());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "新增操作");

                    List<SoB2cDetailDTO.AddDTO> addDTOS = BeanMapper.copyList(detailList, SoB2cDetailDTO.AddDTO.class);
                    //计算物流尺寸
                    calculateSizeByAdd(b2cLogisitics, addDTOS);
                    //新增物流信息
                    soB2cLogisticsService.add(b2cLogisitics, soB2cEntity.getId());
                    if(StringUtils.isBlank(receiverDTO.getCustomerId())){
                        //新增B2C客户
//                        SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
//                        BeanMapper.copyNonNull(mainInfo, addDTO);
//                        addDTO.setReceiverDTO(receiverDTO);
//                        CustomerB2CDTO.AddDTO dto = buildB2cCustomerAddDTO(addDTO, soB2cEntity.getId());
//                        dto.setName(mainInfo.getCustomerName());
//                        String customerId = customerB2cService.add(dto);
                        CustomerB2cEntity dto = new CustomerB2cEntity();
                        String customerId = IdWorker.getIdStr();
                        dto.setId(customerId);
                        dto.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUSTC));
                        dto.setPlatformType(soB2cEntity.getDictPlatform());
                        dto.setName(mainInfo.getCustomerName());
                        dto.setCountryId(receiverDTO.getCountry());
                        dto.setCurrency(soB2cEntity.getCurrency());
                        dto.setSourceId(soB2cEntity.getId());
                        dto.setSourceType("soB2c");
                        dto.setApproveStatus(ApproveStatusEnum.APPROVE);
                        customerB2cService.save(dto);
                        receiverDTO.setCustomerId(customerId);
                    }
                    //新增买家信息
                    soB2cReceiverService.add(receiverDTO, soB2cEntity);
                    //新增明细
                    soB2cDetailService.saveBatch(detailList);
                    //新增财务信息
                    addSoB2cFinance(soB2cEntity);
                    //订单分类
                    List<SoB2cRefCategoryDTO.AddDTO> addList = new ArrayList<>();
                    String category = mainInfo.getCategory();
                    if(StringUtils.isNotBlank(category)){
                        List<String> categoryNameList = Arrays.asList(category.split(","));
                        for (String s : categoryNameList) {
                            addList.add(new SoB2cRefCategoryDTO.AddDTO(soB2cEntity.getId(), orderCategoryMap.get(s)));
                        }
                        soB2cRefCategoryService.add(addList, soB2cEntity.getId());
                    }
                    //检查是否备案并修改状态
                    soB2cService.checkProductRegistrationAndUpdate(soB2cEntity.getId(), "");
                    //速卖通平台仓订单不走任何规则
                    if ( !(PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform()) && soB2cEntity.hasPlatformWarehouseOrder())) {
                        SoB2cDTO.RuleResultDTO orderRuleResult = soB2cService.orderRule(soB2cEntity.getId());
                        //匹配成功
                        Boolean ruleMatch = orderRuleResult.getIsRuleMatch();
                        Boolean isPass = orderRuleResult.getIsPass();
                        if (ruleMatch && isPass) {
                            //仓库规则
                            SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRule(orderRuleResult.getId(), orderRuleResult.getSoB2cDetailList(), orderRuleResult.getMap());
                            Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
                            if (Boolean.TRUE.equals(warehouseRuleMatch)) {
                                SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRule(soB2cEntity.getId(), new HashMap<>(), false);
                                Boolean autoGetTrackNo = logisticsRuleResult.getAutoGetTrackNo();
                                Boolean autoGetTrackNotOfRangeDelivery = logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery();
                                Boolean isRuleMatch = logisticsRuleResult.getIsRuleMatch();
                                //表示成功
                                if(Boolean.TRUE.equals(isRuleMatch)){
                                    //检查是否备案并修改状态
                                    checkProductRegistrationAndUpdate(soB2cEntity.getId(), "");
                                    //申报信息规则
                                    declareRule(soB2cEntity.getId(), new HashMap<>(), Boolean.FALSE, false);
                                }
                                if ((Objects.nonNull(autoGetTrackNo) && Boolean.TRUE.equals(autoGetTrackNo))
                                        || (Boolean.FALSE.equals(isOutOfRangeDelivery) && Objects.nonNull(autoGetTrackNotOfRangeDelivery) && Boolean.TRUE.equals(autoGetTrackNotOfRangeDelivery))) {
                                    soB2cRuleService.handleAutoSubmitDelivery(soB2cEntity.getId(), logisticsRuleResult.getName());
                                }
                            }
                        }
                        //自动计算预估运费到订单的预估运费字段
                        soB2cService.autoCalcEstimatedShippingCost(Collections.singletonList(soB2cEntity.getId()));
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }

            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                errorMsgList = errorMsgList.stream().distinct().collect(Collectors.toList());
                list.get(0).setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.addAll(list);
            }
        }

    }
}
