package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.scm.entity.CfgConditionEntity;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.enums.CfgSupplierSalesDisplayFieldEnum;
import com.erp.model.scm.enums.CfgSupplierSalesPermissionEnum;
import com.erp.model.scm.enums.RuleTypeEnum;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
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
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_SALES_SHARING_REPORT;

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
    private SpElServer spElServer;

    @Override
    public PagingVO<SalesSharingDTO.ListDTO> paging(PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        //获取供应商id
        List<String> supplierIds = getSupplierIds();
        if(CollUtil.isEmpty(supplierIds)){
            throw new ServiceException("供应商信息不存在");
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = getCfgSupplierSalesEntities(supplierIds);
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            throw new ServiceException("供应商的销量设置信息不存在");
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
            return "供应商信息不存在";
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = getCfgSupplierSalesEntities(supplierIds);
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            return "销量设置信息不存在";
        }

        String permission = cfgSupplierSalesList.get(0).getPermission();
        if(!Objects.equals(permission, CfgSupplierSalesPermissionEnum.DOWNLOAD.getCode())){
            return "仅查看数据";
        }

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
            throw new ServiceException("供应商信息不存在");
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = getCfgSupplierSalesEntities(supplierIds);
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            throw new ServiceException("供应商的销量设置信息不存在");
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

    //获取供应商的销量设置信息
    private  List<CfgSupplierSalesEntity> getCfgSupplierSalesEntities(List<String> supplierIds) {
        return FeignQuery.create(CfgSupplierSalesEntity.class)
                .eq(CfgSupplierSalesEntity::getSupplierId, supplierIds.get(0))
                .eq(CfgSupplierSalesEntity::getIsDeleted, Boolean.FALSE)
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







}
