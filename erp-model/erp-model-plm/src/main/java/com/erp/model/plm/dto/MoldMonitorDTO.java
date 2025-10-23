package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 模具监控请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-22
*/
@Data
@NoArgsConstructor
public class MoldMonitorDTO implements Serializable {


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
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人id
         */
        private String createUserId;
        /**
         * 创建人名称
         */
        private String createUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 更新人id
         */
        private String updateUserId;
        /**
         * 更新人名称
         */
        private String updateUserName;


        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源json
         */
        private String sourceRuleJson;
        /**
         * 统计状态：counting=统计中 , finish=统计完成  枚举：MoldMonitorStatusEnum
         */
        private String status;
        private String statusName;
        /**
         * 返还状态：underachieved=未达量 , notReturned=未返 , returned=已返  枚举：MoldMonitorReturnStatusEnum
         */
        private String returnStatus;
        private String returnStatusName;
        /**
         * 寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽  枚举：MoldMonitorLifeStatusEnum
         */
        private String lifeStatus;
        private String lifeStatusName;
        /**
         * 采购下单数量
         */
        private Integer purchaseOrderQty;
        /**
         * 采购收货数量
         */
        private Integer warehouseReceiveQty;
        /**
         * 采购入库数量
         */
        private Integer poInstockQty;
        /**
         * 实际返还金额
         */
        private BigDecimal actualReturnPrice;
        /**
         * 返还人id
         */
        private String returnUserId;
        /**
         * 返还人
         */
        private String returnUserName;
        /**
         * 返还日期
         */
        private LocalDate returnDate;
        /**
         * 返还说明
         */
        private String remark;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class TabDTO extends PermissionsDTO {
        /**
         * 来源类型 cfgMoldReturnAlertrRule =模具返还策略 ， cfgMoldAlertrRule = 模具预警策略
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

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
        /**
         * 勾选的id集合
         */
        private List<String> ids;
        /**
         * 来源类型 cfgMoldReturnAlertrRule =模具返还策略 ， cfgMoldAlertrRule = 模具预警策略
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

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
        * 来源id
        */
        private String sourceId;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源json
        */
        private String sourceRuleJson;

        /**
        * 统计状态：counting=统计中 , finish=统计完成
        */
        private String status;

        /**
        * 返还状态：underachieved=未达量 , notReturned=未返 , returned=已返
        */
        private String returnStatus;

        /**
        * 寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽
        */
        private String lifeStatus;

        /**
        * 采购下单数量
        */
        private Integer purchaseOrderQty;

        /**
        * 采购收货数量
        */
        private Integer warehouseReceiveQty;

        /**
        * 采购入库数量
        */
        private Integer poInstockQty;

        /**
        * 实际返还金额
        */
        private BigDecimal actualReturnPrice;

        /**
        * 返还人id
        */
        private String returnUserId;

        /**
        * 返还人
        */
        private String returnUserName;

        /**
        * 返还日期
        */
        private LocalDate returnDate;

        /**
        * 返还说明
        */
        private String remark;


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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源json
        */
        @NotBlank(message = "来源json不能为空")
        private String sourceRuleJson;

        /**
        * 统计状态：counting=统计中 , finish=统计完成
        */
        @NotBlank(message = "统计状态：counting=统计中 , finish=统计完成不能为空")
        @Size(max = 32,message = "统计状态：counting=统计中 , finish=统计完成最大长度不能超过32位")
        private String status;

        /**
        * 返还状态：underachieved=未达量 , notReturned=未返 , returned=已返
        */
        @NotBlank(message = "返还状态：underachieved=未达量 , notReturned=未返 , returned=已返不能为空")
        @Size(max = 32,message = "返还状态：underachieved=未达量 , notReturned=未返 , returned=已返最大长度不能超过32位")
        private String returnStatus;

        /**
        * 寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽
        */
        @NotBlank(message = "寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽不能为空")
        @Size(max = 32,message = "寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽最大长度不能超过32位")
        private String lifeStatus;

        /**
        * 采购下单数量
        */
        @NotNull(message = "采购下单数量不能为空")
        private Integer purchaseOrderQty;

        /**
        * 采购收货数量
        */
        @NotNull(message = "采购收货数量不能为空")
        private Integer warehouseReceiveQty;

        /**
        * 采购入库数量
        */
        @NotNull(message = "采购入库数量不能为空")
        private Integer poInstockQty;

        /**
        * 实际返还金额
        */
        @NotNull(message = "实际返还金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际返还金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualReturnPrice;

        /**
        * 返还人id
        */
        @NotBlank(message = "返还人id不能为空")
        @Size(max = 32,message = "返还人id最大长度不能超过32位")
        private String returnUserId;

        /**
        * 返还人
        */
        @NotBlank(message = "返还人不能为空")
        @Size(max = 32,message = "返还人最大长度不能超过32位")
        private String returnUserName;

        /**
        * 返还日期
        */
        private LocalDate returnDate;

        /**
        * 返还说明
        */
        @NotBlank(message = "返还说明不能为空")
        @Size(max = 200,message = "返还说明最大长度不能超过200位")
        private String remark;


    }


}