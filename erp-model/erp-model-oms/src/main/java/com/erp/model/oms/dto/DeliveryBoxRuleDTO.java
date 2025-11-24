package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
*/
@Data
@NoArgsConstructor
public class DeliveryBoxRuleDTO implements Serializable {


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
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * sku名称
        */
        private String productName;


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

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表查询参数
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

    }
    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 明细id
         */
        private String  detailId;

        /**
         * skuId
         */
        private String  skuId;

        /**
         * sku编码
         */
        private String  skuNo;

        /**
         * sku名称
         */
        private String  productName;

        /**
         * 发货skuId
         */
        private String  deliverySkuId;

        /**
         * 发货sku编码
         */
        private String  deliverySkuNo;

        /**
         * 发货sku名称
         */
        private String  deliveryProductName;

        /**
         * 单箱数量
         */
        private Integer  perBoxQty;

        /**
         * 创建用户id
         */
        private String  createUserId;


        /**
         * 创建用户名称
         */
        private String  createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime  createTime;

        /**
         * 更新用户id
         */
        private String  updateUserId;

        /**
         * 更新用户名称
         */
        private String  updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 255,message = "skuId最大长度不能超过255位")
        private String skuId;

        /**
        * sku名称
        */
        @NotBlank(message = "sku名称不能为空")
        @Size(max = 255,message = "sku名称最大长度不能超过255位")
        private String productName;


    }


}