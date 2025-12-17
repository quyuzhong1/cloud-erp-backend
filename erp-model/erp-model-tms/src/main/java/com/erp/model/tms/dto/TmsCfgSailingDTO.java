package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.tms.enums.TmsCfgSailingDateTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 截单开船配置请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-15
*/
@Data
@NoArgsConstructor
public class TmsCfgSailingDTO implements Serializable {

    /**
     * 分页查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;
    }

    /**
     * 分页查询列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id【可排序】
         */
        private String  id;

        /**
         * 物流商id【可排序】
         */
        private String  logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String  logisticsSupplierName;

        /**
         * 物流商渠道id【可排序】
         */
        private String  logisticsChannelId;

        /**
         * 物流商渠道名称
         */
        private String  logisticsChannelName;

        /**
         * 日期值
         */
        private Integer  dateValue;

        /**
         * 日期类型【可排序】
         */
        private String  dateType;

        private String  dateTypeName;

        /**
         * 开船日期【可排序】
         */
        private Integer  startDate;

        /**
         * 开船日期（名词）
         */
        private String  startDateName;

        /**
         * 开船日期时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        /**
         * 截单日期【可排序】
         */
        private Integer endDate;

        /**
         * 截单日期（名词）
         */
        private String  endDateName;

        /**
         * 截单日期时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;

        /**
         * 创建人名称【可排序】
         */
        private String  createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime  createTime;

        /**
         * 更新人名称【可排序】
         */
        private String  updateUserName;

        /**
         * 更新时间【可排序】
         */
        private LocalDateTime  updateTime;
        /**
         * 生效日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate  effectiveDate;
        private String  effectiveDateStr;
    }


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
         * 物流渠道id集合
         */
        private List<String> logisticsChannelIdList;

        /**
        * 日期值（每...周，每...月）
        */
        private Integer dateValue;

        /**
        * 日期类型
        */
        private String dateType;

        /**
        * 开船日期（周、月）
        */
        private Integer startDate;

        /**
        * 开船日期时间
        */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        /**
        * 截单日期（周、月）
        */
        private Integer endDate;

        /**
        * 截单日期时间
        */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;

        /**
         * 生效日期
         */
        private LocalDate effectiveDate;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 物流商渠道id
        */
        @NotEmpty(message = "物流商渠道id不能为空")
        private List<String> logisticsChannelIdList;

        /**
        * 日期值（每...周，每...月）
        */
        @NotNull(message = "日期值（每...周，每...月）不能为空")
        private Integer dateValue;

        /**
        * 日期类型，/tms/drop/down/dict/list?key=dateType
        */
        @NotBlank(message = "日期类型不能为空")
        @StateEnumValue(clazz = TmsCfgSailingDateTypeEnum.class, message = "日期类型录入有误")
        private String dateType;

        /**
        * 开船日期（周、月）
        */
        @NotNull(message = "开船日期（周、月）不能为空")
        private Integer startDate;

        /**
        * 开船日期时间
        */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        /**
        * 截单日期（周、月）
        */
        @NotNull(message = "截单日期（周、月）不能为空")
        private Integer endDate;

        /**
        * 截单日期时间
        */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;

        /**
         * 生效日期
         */
        private LocalDate effectiveDate;
    }


}