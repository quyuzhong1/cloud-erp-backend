package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.oms.enums.RuleOrderHandleEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单处理规则表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-05-09
*/
@Data
@NoArgsConstructor
public class CfgRuleOrderHandleDTO implements Serializable {

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class LogDTO {
        /**
         * 规则名称
         */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50,message = "规则名称最大长度不能超过50位")
        private String name;

        /**
         * 禁用状态 false 未禁用
         */
        @NotNull(message = "禁用状态 false 未禁用不能为空")
        private Boolean disabled;

        /**
         * 规则描述
         */
        @Size(max = 255,message = "规则描述最大长度不能超过255位")
        private String remark;

        /**
         * 优先级
         */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
         * 地址处理
         */
        private AddressHandleContent addressHandlerContent;

        /**
         * 电话处理
         */
        private PhoneHandleContent phoneHandleContent;

        /**
         * 邮编处理
         */
        private ZipCodeHandleContent zipCodeHandleContent;

        /**
         * 收货人处理
         */
        private ReceiveHandleContent receiveHandleContent;

        private List<String> filterAddressOneTextList;
        private List<String> filterPhoneTextList;
        private List<String> filterZipCodeTextList;
        private List<String> filterReceiveTextList;
    }
    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id【可排序】
         */
        private String id;

        /**
         * 名称【可排序】
         */
        private String name;

        /**
         * 描述【可排序】
         */
        private String remark;

        /**
         * 优先级【可排序】
         */
        private Integer priority;

        /**
         * 禁用状态【可排序】
         */
        private Boolean disabled;

        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 修改人【可排序】
         */
        private String updateUserName;

        /**
         * 修改时间【可排序】
         */
        private LocalDateTime updateTime;

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
        * 规则名称
        */
        private String name;

        /**
        * 禁用状态 false 未禁用
        */
        private Boolean disabled;

        /**
        * 规则描述
        */
        private String remark;

        /**
        * 优先级
        */
        private Integer priority;

        /**
         * 规则内容
         */
        private RuleContent ruleContent;

        /**
         * 规则条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 规则条件
         */
        private List<RuleConditionDTO.AddDTO> conditionList;
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

        /**
         * 规则条件
         */
        private List<RuleConditionDTO.UpdateDTO> conditionList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 规则名称
        */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50,message = "规则名称最大长度不能超过50位")
        private String name;

        /**
        * 禁用状态 false 未禁用
        */
        @NotNull(message = "禁用状态 false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 规则描述
        */
        @Size(max = 255,message = "规则描述最大长度不能超过255位")
        private String remark;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
         * 规则内容
         */
        @NotNull(message = "规则内容不能为空")
        private RuleContent ruleContent;

    }

    /**
     * 规则匹配结果
     */
    @Data
    @NoArgsConstructor
    public static class RuleContent{

        /**
         * 地址处理
         */
        private AddressHandleContent addressHandlerContent;

        /**
         * 电话处理
         */
        private PhoneHandleContent phoneHandleContent;

        /**
         * 邮编处理
         */
        private ZipCodeHandleContent zipCodeHandleContent;

        /**
         * 收货人处理
         */
        private ReceiveHandleContent receiveHandleContent;
        /**
         * 订单号处理
         */
        private OrderCodeHandleContent orderCodeHandleContent;
    }

    /**
     * 地址处理
     */
    @Data
    @NoArgsConstructor
    public static class AddressHandleContent{

        /**
         * 州省开关
         */
        private boolean provinceSwitch;

        /**
         * 处理州省的规则 {@link RuleOrderHandleEnum.ProvinceRuleContentEnum}
         * oms/common/enumDropDown?type=ProvinceRuleContent
         */
        private String handleProvinceRule;

        /**
         * 省/州待替换文本
         */
        private String provinceWaitReplaceText;

        /**
         * 省/州替换为。。。
         */
        private String provinceReplaceText;
        /**
         * 省/州 转换列表
         */
        private List<TransferDTO> provinceTransferDTOList;
        /**
         * 省/州为空填充文本
         */
        private String provinceFillText;
        /**
         * 处理城市开关
         */
        private boolean citySwitch;

        /**
         * 处理城市的规则 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.CityRuleContentEnum}
         * oms/common/enumDropDown?type=CityRuleContent
         */
        private String handleCityRule;
        /**
         * 城市待替换文本
         */
        private String cityWaitReplaceText;
        /**
         * 城市 转换列表
         */
        private List<TransferDTO> cityTransferDTOList;

        /**
         * 城市替换为。。。
         */
        private String cityReplaceText;
        /**
         * 城市为空填充文本
         */
        private String cityFillText;
        /**
         * 收货地址1过滤开关
         */
        private boolean address1FilterSwitch;

        /**
         * 收货地址1过滤 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.Address1FilterEnum}
         *  oms/common/enumDropDown?type=Address1Filter
         */
        private List<String> filterAddress1TextList;

        /**
         * 收货地址1过滤 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.Address1FilterEnum}
         *  oms/common/enumDropDown?type=Address1Filter
         */
        private List<String> filterAddress1TextNameList;

        /**
         * 收货地址1替换开关
         */
        private boolean address1ReplaceSwitch;
        /**
         * 收货地址1待替换文本
         */
        private String address1WaitReplaceText;

        /**
         * 收货地址1替换为。。。
         */
        private String address1ReplaceText;
    }
    /**
     * 电话处理
     */
    @Data
    @NoArgsConstructor
    public static class PhoneHandleContent{

        /**
         * 电话过滤特殊符号开关
         */
        private boolean phoneFilterSwitch;
        /**
         * 电话过滤特殊符号 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.PhoneFilterEnum}
         * oms/common/enumDropDown?type=PhoneFilter
         */
        private List<String> filterPhoneTextList;

        /**
         * 电话过滤特殊符号 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.PhoneFilterEnum}
         * oms/common/enumDropDown?type=PhoneFilter
         */
        private List<String> filterPhoneTextNameList;
        /**
         * 电话号码截取开关
         */
        private boolean phoneInterceptSwitch;

        /**
         * 将电话号码从第。。。字符截取
         */
        private Integer phoneInterceptStartIndex;

        /**
         * 电话为空填充开关
         */
        private boolean phoneEmptyFillSwitch;

        /**
         * 电话为空填充字符
         */
        private String phoneEmptyFillText;
    }
    /**
     * 邮编处理
     */
    @Data
    @NoArgsConstructor
    public static class ZipCodeHandleContent{
        /**
         * 处理邮编的规则 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.ZipCodeContentEnum}
         * oms/common/enumDropDown?type=ZipCodeContent
         */
        private String handleZipCodeRule;
        /**
         * 邮编过滤特殊符号开关
         */
        private boolean zipCodeFilterSwitch;
        /**
         * 邮编过滤特殊符号 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.ZipCodeFilterEnum}
         * oms/common/enumDropDown?type=ZipCodeFilter
         */
        private List<String> filterZipCodeTextList;
        /**
         * 邮编过滤特殊符号 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.ZipCodeFilterEnum}
         * oms/common/enumDropDown?type=ZipCodeFilter
         */
        private List<String> filterZipCodeTextNameList;

        /**
         * 邮编为空填充开关
         */
        private boolean zipCodeEmptyFillSwitch;

        /**
         * 邮编为空填充字符
         */
        private String zipCodeEmptyFillText;
        /**
         * 处理邮编开关
         */
        private boolean zipCodeSwitch;
    }
    /**
     * 收货人处理
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveHandleContent{

        /**
         * 收货人为空填充开关
         */
        private boolean receiveEmptyFillSwitch;
        /**
         * 处理收货人为空的规则 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.ReceiveFillRuleContentEnum}
         * oms/common/enumDropDown?type=ReceiveFillRuleContent
         */
        private String handleReceiveEmptyFillRule;
        /**
         * 收货人为空填充文本
         */
        private String receiveFillText;

        /**
         * 收货人过滤特殊符号开关
         */
        private boolean receiveFilterSwitch;

        /**
         * 收货人过滤特殊符号 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.ReceiveFilterEnum}
         * oms/common/enumDropDown?type=ReceiveFilter
         */
        private List<String> filterReceiveTextList;


        /**
         * 收货人过滤特殊符号 {@link com.erp.model.oms.enums.RuleOrderHandleEnum.ReceiveFilterEnum}
         * oms/common/enumDropDown?type=ReceiveFilter
         */
        private List<String> filterReceiveTextNameList;
    }
    /**
     * 订单处理
     */
    @Data
    @NoArgsConstructor
    public static class OrderCodeHandleContent{
        /**
         * 订单号开关
         */
        private boolean orderCodeSwitch;
        /**
         * 处理订单号的规则 {@link RuleOrderHandleEnum.OrderCodeRuleContentEnum}
         * oms/common/enumDropDown?type=OrderCodeRuleContent
         */
        private String handleOrderCodeRule;

        /**
         * 订单号 待替换文本
         */
        private String orderCodeWaitReplaceText;

        /**
         * 订单号 替换为。。。
         */
        private String orderCodeReplaceText;
        /**
         * 订单号 转换列表
         */
        private List<TransferDTO> orderCodeTransferDTOList;
    }

    /**
     * 规则匹配结果
     */
    @Data
    @NoArgsConstructor
    public static class RuleMatchDTO{

        /**
         * 通过结果
         */
        private Boolean approveSuccess;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 规则处理
         */
        private RuleContent ruleContent;

    }
    @Data
    @NoArgsConstructor
    public static class TransferDTO{
        /**
         * 待转换文本
         */
        private String waitReplaceText;
        /**
         * 转换为文本
         */
        private String replaceText;
    }
}