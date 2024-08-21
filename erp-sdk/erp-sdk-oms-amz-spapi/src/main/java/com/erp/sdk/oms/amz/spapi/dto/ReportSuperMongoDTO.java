package com.erp.sdk.oms.amz.spapi.dto;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.MongoAbstractDTO;
import com.common.business.dto.MongoSuperDTO;
import com.common.business.dto.UniqueDto;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.ReflectUtils;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.sdk.oms.amz.spapi.enums.AmazonListingStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 亚马逊报表mongo超类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class ReportSuperMongoDTO extends MongoAbstractDTO {


    @Panno(findType = PannoEnum.IN,field = "reportMarketplaceIds")
    private List<String> reportMarketplaceIds;

    @Panno(findType = PannoEnum.EQ, field = "reportId")
    private String reportId;

    @Panno(findType = PannoEnum.EQ, field = "reportDataStartTime")
    private String reportDataStartTime;

    @Panno(findType = PannoEnum.EQ, field = "reportDataEndTime")
    private String reportDataEndTime;

    @Panno(findType = PannoEnum.EQ, field = "reportScheduleId")
    private String reportScheduleId;

    /**
     * 报告行号
     */
    @Panno(findType = PannoEnum.EQ, field = "reportRowNum")
    private Integer reportRowNum;

    /**
     * 亚马逊账号代号
     */
    @Panno(findType = PannoEnum.EQ, field = "platformShopCode")
    private String platformShopCode;

    @Panno(findType = PannoEnum.EQ, field = "requestShopId")
    private String requestShopId;



    /**
     * 填充报告信息
     *
     * @param sourceList     解析后的列表
     * @param report         报告信息
     * @param recordTypeEnum 报告类型
     * @return 组合报告信息后的列表
     */
    public static List<? extends ReportSuperMongoDTO> fillReportData(List<? extends ReportSuperMongoDTO> sourceList,
                                                                     DmpAmzReportInfoEntity report,
                                                                     AmazonReportRecordTypeEnum recordTypeEnum,
                                                                     String platformShopCode,
                                                                     String marketplaceId
    ) {
        return IntStream.range(0, sourceList.size())
                .mapToObj(i -> {
                    try {
                        Object o = sourceList.get(i);
                        if (o instanceof ReportListingMongoDTO) {
                            // LISTING 填充ASIN
                            ReportListingMongoDTO sourceDTO = (ReportListingMongoDTO) o;
                            if (null != sourceDTO.getProductIdType()
                                    && sourceDTO.getProductIdType().contains("1")
                                    && StringUtils.isBlank(sourceDTO.getAsin1())) {
                                sourceDTO.setAsin1(sourceDTO.getProductId());
                            }
                            // 已删除订单无明细
                            if ("1".equalsIgnoreCase(sourceDTO.getProductIdType()) && AmazonListingStatusEnum.INACTIVE.getCode().equalsIgnoreCase(sourceDTO.getStatus())) {
                                //ProductIdType=ASIN,停售无法更新明细
                                sourceDTO.setSupportsDetailDownload(false);
                            }
                            // 日本异常数据
                            if (AmazonMarketplaceEnum.JP.getMarketplaceId().equalsIgnoreCase(marketplaceId)) {
                                if ("4".equals(sourceDTO.getProductIdType())) {
                                    // 日本站点ProductIdType=4无法更新明细
                                    sourceDTO.setSupportsDetailDownload(false);
                                }
                            }

                        }
                        ReportSuperMongoDTO mongoDTO = (ReportSuperMongoDTO) (recordTypeEnum.getAndCheckMongoDTOClass().newInstance());
                        BeanUtils.copyProperties(o, mongoDTO);
                        mongoDTO.setReportDataStartTime(report.getDataStartTime());
                        mongoDTO.setReportDataEndTime(report.getDataEndTime());
                        mongoDTO.setReportMarketplaceIds(Arrays.stream(report.getMarketplaceIds().split(",")).collect(Collectors.toList()));
                        mongoDTO.setReportId(report.getReportId());
                        mongoDTO.setReportScheduleId(report.getReportScheduleId());
                        mongoDTO.setPlatformShopCode(platformShopCode);
                        mongoDTO.setReportRowNum(i + 1);
                        mongoDTO.setRequestShopId(report.getShopId());
                        String uniqueId = toUniqueMd5(mongoDTO);
                        mongoDTO.setUniqueId(uniqueId);
                        // 业务唯一ID
                        mongoDTO.setBusinessUniqueKey(mongoDTO.convertBusinessUniqueKey());
                        mongoDTO.setIsAddOrUpdate(true);
                        return mongoDTO;
                    } catch (Exception e) {
                        throw new ServiceException("转换mongoDTO失败, error=" + e.getMessage());
                    }
                }).collect(Collectors.toList());
    }


    /**
     * 忽略其他信息：报告内容 + 行号 + 报告ID 生成唯一md5
     *
     * @param reportSuperMongoDTO 报告来源内容
     * @return MD%
     */
    public static String toUniqueMd5(ReportSuperMongoDTO reportSuperMongoDTO) {
        // 忽略的字段列表
        Set<String> ignoredFields = new HashSet<>(Arrays.asList(
                "reportMarketplaceIds",
                "reportDataStartTime",
                "reportDataEndTime",
                "reportScheduleId",
                "id",
                "_id",
                "requestShopId",
                "uniqueId",
                "isAddOrUpdate",
                "downloadTime",
                "businessUniqueKey"
        ));
        Class<? extends ReportSuperMongoDTO> subClass = reportSuperMongoDTO.getClass();

        StringBuilder sb = new StringBuilder();
        sb.append(subClass.getSimpleName()).append("{");
        Arrays.stream(ReflectUtil.getFields(subClass)).forEach(field -> {
            String fieldName = field.getName();
            // 如果字段不在忽略列表中，则将其添加到字符串表示形式中
            if (!ignoredFields.contains(fieldName)) {
                Object value = ReflectUtils.getFieldValue(reportSuperMongoDTO, fieldName);
                sb.append(fieldName).append("=").append(value).append(", ");
            }
        });
        // 删除最后一个逗号和空格
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return DigestUtil.md5Hex(sb.toString());
    }
}
