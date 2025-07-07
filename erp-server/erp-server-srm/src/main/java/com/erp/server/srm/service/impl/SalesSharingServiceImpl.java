package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DwsDbErpDmpSkuSalesReportFDTO;
import com.erp.model.dmp.entity.doris.DwsDbErpDmpSkuSalesReportFEntity;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.rpc.dmp.feign.DmpSkuSaleReportFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.VirtualInventoryFeign;
import com.erp.server.srm.handler.SupplierSalesConditionHandler;
import com.erp.server.srm.mapper.SalesSharingMapper;
import com.erp.server.srm.service.SalesSharingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.SalesSharingDTO;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 销量共享表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@Service
public class SalesSharingServiceImpl extends SuperServiceImpl<SalesSharingMapper, SalesSharingEntity> implements SalesSharingService {

    @Resource
    private DmpSkuSaleReportFeign dmpSkuSaleReportFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SupplierSalesConditionHandler supplierSalesConditionHandler;
    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;

    @Override
    public PagingVO<SalesSharingDTO.ListDTO> paging(PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        //获取供应商id
        List<String> supplierIds = getSupplierIds();
        if(CollUtil.isEmpty(supplierIds)){
            return new PagingVO();
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = getCfgSupplierSalesEntities(supplierIds);
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            return new PagingVO();
        }
        CfgSupplierSalesEntity cfgSupplierSalesEntity = cfgSupplierSalesList.get(0);

        //拼接供应商id
        SalesSharingDTO.PagingParamDTO params = pagingParamDTO.getParams();
        Map<String, String> sqlMap = params.getSqlMap();
        String strSql = sqlMap.get("default");
        strSql = strSql + " and ss.supplier_id = '"+supplierIds.get(0)+"' ";
        sqlMap.put("default", strSql);
        params.setSqlMap(sqlMap);

        params.setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SalesSharingDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }

        //处理字段显示
        List<SalesSharingDTO.ListDTO> records = pageData.getRecords();
        hideFieldsInRecord(records,cfgSupplierSalesEntity.getDisplayField());
        return new PagingVO(pageData);
    }


    @Override
    public String exportList(SalesSharingDTO.PagingParamDTO pagingDTO ,HttpServletResponse response) {
//        downloadTaskFeign.saveDownloadTask("销量共享导出", EXPORT_SRM_SALES_SHARING_REPORT.getCode(), pagingParamDTO);
        //获取供应商id
        List<String> supplierIds = getSupplierIds();
        if(CollUtil.isEmpty(supplierIds)){
            return "下载失败";
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = getCfgSupplierSalesEntities(supplierIds);
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            return "下载失败";
        }

        String permission = cfgSupplierSalesList.get(0).getPermission();
        if(!Objects.equals(permission, CfgSupplierSalesPermissionEnum.DOWNLOAD.getCode())){
            return ApiError.NO_PERMISSION.msg;
        }
        //供应商id
        pagingDTO.setSupplierId(supplierIds.get(0));
        List<SalesSharingDTO.ListDTO> list = this.baseMapper.listByParams( pagingDTO);

        //处理字段显示
        String displayField = cfgSupplierSalesList.get(0).getDisplayField();
        hideFieldsInRecord(list,displayField);

        String name = "销量共享列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/salesSharingExport.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销量共享列表导出出错 >>>>>{}", e);
            return "销量共享列表导出出错";
        }

        return "";
    }

    @Override
    public String getNoticeContent() {
        //获取供应商id
        List<String> supplierIds = getSupplierIds();
        if(CollUtil.isEmpty(supplierIds)){
            return "";
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = getCfgSupplierSalesEntities(supplierIds);
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            return "";
        }
        CfgSupplierSalesEntity cfgSupplierSalesEntity = cfgSupplierSalesList.get(0);

        //通知处理
        Boolean noticeEnabled = cfgSupplierSalesEntity.getNoticeEnabled();
        if(Boolean.TRUE.equals(noticeEnabled)){
            //查询通知配置逻辑
            List<CfgSupplierSalesConditionEntity> noticeConditionList = FeignQuery.create(CfgSupplierSalesConditionEntity.class)
                    .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, cfgSupplierSalesEntity.getId())
                    .eq(CfgSupplierSalesConditionEntity::getSourceType, RuleTypeEnum.NOTICE.getCode())
                    .list();

            //把他转为sql条件
            String condition = buildSqlCondition(noticeConditionList);
            condition = "and ("+condition + ") and supplier_id = '"+supplierIds.get(0)+"' ";

            List<SalesSharingEntity> list = lambdaQuery().last(condition).list();
            if(CollUtil.isNotEmpty(list)){
                String noticeContent = buildNoticeCondition(noticeConditionList);
                if(StringUtils.isNotBlank(noticeContent)){
                    noticeContent = "销量共享存在"+list.size()+"个SKU"+noticeContent+"，请及时查看，避免缺货哦。";
                    return noticeContent;
                }
            }
        }
        return "";
    }

    //处理字段显示
    public  void hideFieldsInRecord(List<SalesSharingDTO.ListDTO> records,String displayField) {
        List<CfgSupplierSalesDisplayFieldEnum> list = Arrays.asList(CfgSupplierSalesDisplayFieldEnum.values());

        List<String> allFieldList = list.stream().map(CfgSupplierSalesDisplayFieldEnum::getCode).collect(Collectors.toList());

        List<String> existFieldList = Arrays.asList(displayField.split(","));

        // 差集 = allFieldList - existFieldList
        List<String> diffFieldList = allFieldList.stream()
                .filter(field -> !existFieldList.contains(field))
                .collect(Collectors.toList());

        if(diffFieldList.contains(CfgSupplierSalesDisplayFieldEnum.SALE_STATE.getCode())){
            diffFieldList.add("saleStateName");
        }

        // 缓存 ListDTO 类中存在的字段
        Map<String, Field> fieldMap = new HashMap<>();
        for (String fieldName : diffFieldList) {
            try {
                Field field = SalesSharingDTO.ListDTO.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                fieldMap.put(fieldName, field);
            } catch (NoSuchFieldException ignored) {
                // 字段不存在，忽略
            }
        }

        // 遍历 records，设置不展示字段为默认值
        for (SalesSharingDTO.ListDTO dto : records) {
            for (Field field : fieldMap.values()) {
                try {
                    Class<?> type = field.getType();

                    if (type == BigDecimal.class) {
                        field.set(dto, BigDecimal.ZERO);
                    } else if (type == Integer.class || type == int.class) {
                        field.set(dto, 0);
                    } else if (type == Long.class || type == long.class) {
                        field.set(dto, 0L);
                    } else if (type == String.class) {
                        field.set(dto, "");
                    } else if (type == Boolean.class || type == boolean.class) {
                        field.set(dto, false);
                    }
                    // 可继续扩展其他类型
                } catch (IllegalAccessException e) {
                    e.printStackTrace(); // 处理异常或日志记录
                }
            }
        }
    }

    //获取供应商ids
    private  List<String> getSupplierIds() {
        LoginUser login = UserContext.getDefaultLoginUser();

        String uid = login.getUid();

        List<SupplierRefUserEntity> supplierRefUserList = FeignQuery.create(SupplierRefUserEntity.class)
                .eq(SupplierRefUserEntity::getUid, uid)
                .eq(SupplierRefUserEntity::getDisabled, Boolean.FALSE)
                .eq(SupplierRefUserEntity::getIsDeleted, Boolean.FALSE)
                .list();

        return supplierRefUserList.stream().map(SupplierRefUserEntity::getSupplierId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    //获取供应商的销量设置信息 (未删除+启用)
    private  List<CfgSupplierSalesEntity> getCfgSupplierSalesEntities(List<String> supplierIds) {
        return FeignQuery.create(CfgSupplierSalesEntity.class)
                .eq(CfgSupplierSalesEntity::getSupplierId, supplierIds.get(0))
                .eq(CfgSupplierSalesEntity::getIsDeleted, Boolean.FALSE)
                .eq(CfgSupplierSalesEntity::getDisabled, Boolean.FALSE)
                .list();
    }

    //条件转换为sql条件
    public String buildNoticeCondition(List<CfgSupplierSalesConditionEntity> conditionList) {
        if (conditionList == null || conditionList.isEmpty()) {
            return "";
        }

        List<CfgConditionEntity> cfgConditionList = FeignQuery.create(CfgConditionEntity.class).eq(CfgConditionEntity::getRuleType, RuleTypeEnum.NOTICE.getCode()).list();

        Map<String, String> cfgConditionMap = cfgConditionList.stream().collect(Collectors.toMap(CfgConditionEntity::getConditionField, CfgConditionEntity::getConditionFieldName, (o1, o2) -> o1));

        // 按顺序排序
        conditionList.sort(Comparator.comparing(CfgSupplierSalesConditionEntity::getIndex));

        StringBuilder sqlBuilder = new StringBuilder();

        for (int i = 0; i < conditionList.size(); i++) {
            CfgSupplierSalesConditionEntity cond = conditionList.get(i);
            String leftBracket = getSafe(cond.getLeftBracket());
            String field = cond.getField();
            String compare = cond.getCompare();
            String value = cond.getValue();
            String rightBracket = getSafe(cond.getRightBracket());
            String logic = getSafe(cond.getLogic()).trim();
            sqlBuilder.append(leftBracket)
                    .append(cfgConditionMap.get(field)).append(RuleCompareEnum.getName(compare)).append(value)
                    .append(rightBracket);
            // 拼接 AND/OR 逻辑连接符（最后一个条件不需要）
            if (i < conditionList.size() - 1 && !logic.isEmpty()) {
                sqlBuilder.append(logic.equals("or")?"或" : "且");
            }
        }
        return sqlBuilder.toString();
    }

    //条件转换为sql条件
    public String buildSqlCondition(List<CfgSupplierSalesConditionEntity> conditionList) {
        if (conditionList == null || conditionList.isEmpty()) {
            return "1 = 1";
        }

        // 按顺序排序
        conditionList.sort(Comparator.comparing(CfgSupplierSalesConditionEntity::getIndex));

        StringBuilder sqlBuilder = new StringBuilder();

        for (int i = 0; i < conditionList.size(); i++) {
            CfgSupplierSalesConditionEntity cond = conditionList.get(i);

            String leftBracket = getSafe(cond.getLeftBracket());
            String field = StrUtil.toUnderlineCase(cond.getField());
            String compare = cond.getCompare();
            String value = cond.getValue();
            String rightBracket = getSafe(cond.getRightBracket());
            String logic = getSafe(cond.getLogic()).trim();
            String valueType = Optional.ofNullable(cond.getValueType()).orElse("String");

            String formattedValue = formatValue(compare, value, valueType);

            sqlBuilder.append(leftBracket)
                    .append(field).append(" ").append(compare).append(" ").append(formattedValue)
                    .append(rightBracket);

            // 拼接 AND/OR 逻辑连接符（最后一个条件不需要）
            if (i < conditionList.size() - 1 && !logic.isEmpty()) {
                sqlBuilder.append(" ").append(logic).append(" ");
            }
        }

        return sqlBuilder.toString();
    }

    // 统一处理空值
    private String getSafe(String str) {
        return str == null ? "" : str;
    }

    // 格式化值（加引号、处理 IN 列表等）
    private String formatValue(String compare, String value, String valueType) {
        boolean isInList = RuleCompareEnum.IN_LIST.getCode().equalsIgnoreCase(compare)
                || RuleCompareEnum.NOT_IN_LIST.getCode().equalsIgnoreCase(compare);

        if ("String".equalsIgnoreCase(valueType)) {
            if (isInList) {
                return formatListValue(value, true);
            } else {
                return "'" + value + "'";
            }
        } else {
            if (isInList) {
                return formatListValue(value, false);
            } else {
                return value;
            }
        }
    }

    // 处理 IN/NOT IN 的 value（字符串或数字列表）
    private String formatListValue(String value, boolean quote) {
        return "(" + Arrays.stream(value.split(","))
                .map(String::trim)
                .map(v -> quote ? "'" + v + "'" : v)
                .collect(Collectors.joining(", ")) + ")";
    }


    /**
     *
     * 根据销量设置的配置，去拉取中台的销量数据，并且销量共享数据
     */
    @Override
    public void calSalesSharing(){
        //拉取销量设置的配置
        List<CfgSupplierSalesDTO.ListAllDTO> listAllDTOS = scmTaskFeign.listAll();
        if(CollUtil.isNotEmpty(listAllDTOS)){
            List<String> supplierIds = listAllDTOS.stream().map(CfgSupplierSalesDTO.ListAllDTO::getSupplierId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            //查询供应商下的skuNo集合
            List<PurchaseOrderDTO.SupplierSkuDTO> supplierSkuDTOS = scmTaskFeign.listSkuBySupplierIds(supplierIds);
            Map<String, List<PurchaseOrderDTO.SupplierSkuDTO>> listSkuBySupplierIds = supplierSkuDTOS.stream().collect(Collectors.groupingBy(PurchaseOrderDTO.SupplierSkuDTO::getSupplierId));

            for (CfgSupplierSalesDTO.ListAllDTO listAllDTO : listAllDTOS) {
                try {
                    //供应商
                    String supplierId = listAllDTO.getSupplierId();
                    if (StringUtils.isBlank(supplierId)) {
                        continue;
                    }
                    //批量删除
                    lambdaUpdate()
                            .eq(SalesSharingEntity::getSupplierId, supplierId)
                            .set(SalesSharingEntity::getIsDeleted, true)
                            .update();

                    //查询供应商采购比例
                    List<SupplierPurchaseQuantityEntity> supplierPurchaseQuantityList = FeignQuery.create(SupplierPurchaseQuantityEntity.class)
                            .eq(SupplierPurchaseQuantityEntity::getSupplierId, supplierId)
                            .list();

                    String supplierCode = listAllDTO.getSupplierCode();
                    String supplierName = listAllDTO.getSupplierName();

                    StringBuffer sb = new StringBuffer();
                    sb.append(" and ( ");

                    //日均销量类型
                    String dailySalesType = listAllDTO.getDailySalesType();
                    //统计维度
                    String dimension = listAllDTO.getDimension();
                    //销量比例类型
                    String salesRatioType = listAllDTO.getSalesRatioType();
                    //销量比例
                    BigDecimal salesRatio = listAllDTO.getSalesRatio().divide(new BigDecimal(100), 4, RoundingMode.DOWN);

                    //sku查看配置
                    List<CfgSupplierSalesConditionEntity> skuList = listAllDTO.getSkuList();
                    //可销库存配置
                    String warehouseType = listAllDTO.getWarehouseType();
                    List<CfgSupplierSalesConditionEntity> saleableStockList = listAllDTO.getSaleableStockList();
                    //销量统计配置
                    List<CfgSupplierSalesConditionEntity> salesStatisticList = listAllDTO.getSalesStatisticList();

                    //设置sku相关的配置条件
                    if (CollUtil.isNotEmpty(skuList)) {
                        String sqlWhere = supplierSalesConditionHandler.buildWhereClause(skuList, Boolean.TRUE);
                        if (StringUtils.isNotBlank(sqlWhere)) {
                            sb.append(" ");
                            sb.append(sqlWhere);
                        }
                    }
                    //设置销量统计相关的配置条件
                    if (CollUtil.isNotEmpty(salesStatisticList)) {
                        String sqlWhere = supplierSalesConditionHandler.buildWhereClause(salesStatisticList, Boolean.TRUE);
                        if (StringUtils.isNotBlank(sqlWhere)) {
                            sb.append(" ");
                            sb.append(sqlWhere);
                        }
                    }

                    //设置日均销量类型 和 统计维度
                    sb.append(" daily_sales_type = '");
                    sb.append(dailySalesType);
                    sb.append("' and dimension = '");
                    sb.append(dimension);
                    sb.append("' )");

                    DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO dto = new DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO();
                    dto.setConditionSql(sb.toString());
                    List<DwsDbErpDmpSkuSalesReportFEntity> reportList = dmpSkuSaleReportFeign.reportList(dto);

                    if (CollUtil.isNotEmpty(reportList)) {
                        //判断是否有设置sku黑名单，并排除指定的sku
                        if (Boolean.TRUE.equals(listAllDTO.getIsBlack()) && CollUtil.isNotEmpty(listAllDTO.getBlackList())) {
                            List<CfgSupplierSalesConditionEntity> blackList = listAllDTO.getBlackList();
                            CfgSupplierSalesConditionEntity cfgSupplierSalesConditionEntity = blackList.get(0);
                            if (StringUtils.isNotBlank(cfgSupplierSalesConditionEntity.getValue())) {
                                List<String> skuNoList = Arrays.asList(cfgSupplierSalesConditionEntity.getValue().split(","));

                                //过滤sku黑名单
                                reportList = reportList.stream().filter(e -> !skuNoList.contains(e.getSkuNo())).collect(Collectors.toList());
                            }
                        }

                        //过滤是否该供应商采购过
                        CfgSupplierSalesConditionEntity isPurchaseEntity = skuList.stream().filter(e -> e.getField().contains("isPurchase")).findFirst().orElse(null);
                        if (Objects.nonNull(isPurchaseEntity) && StringUtils.isNotBlank(isPurchaseEntity.getValue())) {
                            //采购过的sku集合
                            List<PurchaseOrderDTO.SupplierSkuDTO> supplierSku = listSkuBySupplierIds.get(supplierId);
                            List<String> purchaseSkuList = new ArrayList<>();
                            if (CollUtil.isNotEmpty(supplierSku)) {
                                purchaseSkuList = supplierSku.stream().map(PurchaseOrderDTO.SupplierSkuDTO::getSkuNo).collect(Collectors.toList());
                            }
                            List<String> finalPurchaseSkuList = purchaseSkuList;
                            if (Objects.equals(isPurchaseEntity.getValue(), "true")) {//采购过的
                                reportList = reportList.stream().filter(e -> finalPurchaseSkuList.contains(e.getSkuNo())).collect(Collectors.toList());
                            } else {//没采购过的
                                reportList = reportList.stream().filter(e -> !finalPurchaseSkuList.contains(e.getSkuNo())).collect(Collectors.toList());
                            }
                        }
                    }

                    //过滤完之后剩余的数据
                    if (CollUtil.isNotEmpty(reportList)) {
                        //根据可销库存配置查询即时存储数据
                        Map<String, Integer> skuQtyMap = new HashMap<>();
                        StringBuffer stringBuffer = new StringBuffer();
                        List<String> skuIds = reportList.stream().map(DwsDbErpDmpSkuSalesReportFEntity::getSkuId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(skuIds)) {
                            stringBuffer.append(" and sku_id in ('");
                            stringBuffer.append(String.join("','", skuIds));
                            stringBuffer.append("') and ");
                        }
                        if (CollUtil.isNotEmpty(saleableStockList)) {
                            String sqlWhere = supplierSalesConditionHandler.buildWhereClause(saleableStockList, Boolean.TRUE);
                            if (StringUtils.isNotBlank(sqlWhere)) {
                                stringBuffer.append(" ");
                                stringBuffer.append(sqlWhere);
                            }
                        }
                        stringBuffer.append(" 1 = 1 ");
                        String inventorySqlCondition = stringBuffer.toString();
                        if (StringUtils.isNotBlank(inventorySqlCondition)) {
                            if (Objects.equals(warehouseType, CfgSupplierSalesConditionWarehouseTypeEnum.VIRTUALWAREHOUSE.getCode())) {
                                //虚拟仓
                                List<VirtualInventoryEntity> inventoryList = FeignQuery.create(VirtualInventoryEntity.class).last(inventorySqlCondition).list();

                                skuQtyMap = inventoryList.stream()
                                        .collect(Collectors.groupingBy(
                                                VirtualInventoryEntity::getSkuId, // 按 skuId 分组
                                                Collectors.summingInt(VirtualInventoryEntity::getQty) // 对 qty 进行合计
                                        ));

                            } else {
                                //实体仓
                                List<InventoryEntity> inventoryList = FeignQuery.create(InventoryEntity.class).last(inventorySqlCondition).list();

                                skuQtyMap = inventoryList.stream()
                                        .collect(Collectors.groupingBy(
                                                InventoryEntity::getSkuId, // 按 skuId 分组
                                                Collectors.summingInt(InventoryEntity::getQty) // 对 qty 进行合计
                                        ));
                            }
                        }

                        List<SalesSharingEntity> salesSharingList = new ArrayList<>();
                        Map<String, List<DwsDbErpDmpSkuSalesReportFEntity>> map = reportList.stream().collect(Collectors.groupingBy(DwsDbErpDmpSkuSalesReportFEntity::getSkuId));
                        for (Map.Entry<String, List<DwsDbErpDmpSkuSalesReportFEntity>> entry : map.entrySet()) {
                            if (salesRatioType.equals(CfgSupplierSalesSalesRatioTypeEnum.PURCHASERATIO.getCode())) {
                                SupplierPurchaseQuantityEntity supplierPurchaseQuantityEntity = supplierPurchaseQuantityList.stream().filter(e -> Objects.equals(e.getSkuId(), entry.getKey())).findFirst().orElse(null);
                                if (Objects.nonNull(supplierPurchaseQuantityEntity)) {
                                    salesRatio = supplierPurchaseQuantityEntity.getPurchaseRatio();
                                } else {
                                    salesRatio = BigDecimal.ZERO;
                                }
                            }
                            List<DwsDbErpDmpSkuSalesReportFEntity> value = entry.getValue();
                            SalesSharingEntity salesSharingEntity = new SalesSharingEntity();
                            BeanMapper.copy(value.get(0), salesSharingEntity);
                            // 使用Stream API进行聚合计算
                            DwsDbErpDmpSkuSalesReportFEntity aggregatedReport = value.stream()
                                    .reduce((report1, report2) -> {
                                        report1.setSalesLast3Days(report1.getSalesLast3Days() + report2.getSalesLast3Days());
                                        report1.setSalesLast7Days(report1.getSalesLast7Days() + report2.getSalesLast7Days());
                                        report1.setSalesLast30Days(report1.getSalesLast30Days() + report2.getSalesLast30Days());
                                        report1.setSalesLast60Days(report1.getSalesLast60Days() + report2.getSalesLast60Days());
                                        report1.setSalesLast90Days(report1.getSalesLast90Days() + report2.getSalesLast90Days());
                                        return report1;
                                    }).orElseThrow(() -> new IllegalStateException("至少需要一个销售报告实体"));


                            //销量 * 销量比例
                            salesSharingEntity.setSalesLast3Days(new BigDecimal(aggregatedReport.getSalesLast3Days()).multiply(salesRatio).setScale(0, RoundingMode.DOWN).intValue());
                            salesSharingEntity.setSalesLast7Days(new BigDecimal(aggregatedReport.getSalesLast7Days()).multiply(salesRatio).setScale(0, RoundingMode.DOWN).intValue());
                            salesSharingEntity.setSalesLast30Days(new BigDecimal(aggregatedReport.getSalesLast30Days()).multiply(salesRatio).setScale(0, RoundingMode.DOWN).intValue());
                            salesSharingEntity.setSalesLast60Days(new BigDecimal(aggregatedReport.getSalesLast60Days()).multiply(salesRatio).setScale(0, RoundingMode.DOWN).intValue());
                            salesSharingEntity.setSalesLast90Days(new BigDecimal(aggregatedReport.getSalesLast90Days()).multiply(salesRatio).setScale(0, RoundingMode.DOWN).intValue());

                            if (Objects.equals(dailySalesType, CfgSupplierSalesDailySalesTypeEnum.DAILYAVG3DAYS.getCode())) {
                                salesSharingEntity.setDailySales(aggregatedReport.getSalesLast3Days() / 3);
                            }
                            if (Objects.equals(dailySalesType, CfgSupplierSalesDailySalesTypeEnum.DAILYAVG7DAYS.getCode())) {
                                salesSharingEntity.setDailySales(aggregatedReport.getSalesLast7Days() / 7);
                            }
                            if (Objects.equals(dailySalesType, CfgSupplierSalesDailySalesTypeEnum.DAILYAVG30DAYS.getCode())) {
                                salesSharingEntity.setDailySales(aggregatedReport.getSalesLast30Days() / 30);
                            }
                            if (Objects.equals(dailySalesType, CfgSupplierSalesDailySalesTypeEnum.DAILYAVG60DAYS.getCode())) {
                                salesSharingEntity.setDailySales(aggregatedReport.getSalesLast60Days() / 60);
                            }
                            if (Objects.equals(dailySalesType, CfgSupplierSalesDailySalesTypeEnum.DAILYAVG90DAYS.getCode())) {
                                salesSharingEntity.setDailySales(aggregatedReport.getSalesLast90Days() / 90);
                            }

                            String idStr = IdWorker.getIdStr();
                            salesSharingEntity.setId(idStr);
                            salesSharingEntity.setSupplierId(supplierId);
                            salesSharingEntity.setSupplierCode(supplierCode);
                            salesSharingEntity.setSupplierName(supplierName);

                            //原始比例
                            salesSharingEntity.setSalesRatio(salesRatio);

                            //即时库存
                            Integer saleableStock = skuQtyMap.getOrDefault(salesSharingEntity.getSkuId(), 0);
                            salesSharingEntity.setSaleableStock(saleableStock);

                            //计算可销天数
                            Integer dailySales = salesSharingEntity.getDailySales();
                            if (Objects.nonNull(dailySales) && Objects.nonNull(saleableStock) && dailySales > 0 && saleableStock > 0) {
                                //即时库存数量 / 日均销量
                                BigDecimal saleableDays = new BigDecimal(saleableStock).divide(new BigDecimal(dailySales), 0, RoundingMode.DOWN);
                                salesSharingEntity.setSaleableDays(saleableDays.intValue());
                            } else {
                                salesSharingEntity.setSaleableDays(saleableStock);
                            }

                            salesSharingList.add(salesSharingEntity);
                        }
                        //批量保存
                        saveBatch(salesSharingList);
                    }
                } catch (Exception e) {
                    // 记录错误日志，方便排查问题
                    log.error("处理供应商销量配置失败，supplierId={}", listAllDTO.getSupplierId(), e);
                    // 可选：记录失败指标、发送告警等
                }
            }
        }
    }
}
