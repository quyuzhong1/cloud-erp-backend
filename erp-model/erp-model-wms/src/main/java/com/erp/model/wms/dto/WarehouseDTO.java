package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname WarehouseDTO
 * @Description TODO
 * @Date 2023-03-16 16:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WarehouseDTO implements Serializable {


    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        @NotBlank(message = "金蝶仓库编号不能为空")
        @Size(max = 30, message = "金蝶仓库编号最大30字符")
        private String kingdeeWarehouseCode;

        /**
         * 名称
         */
        @Size(max = 200, message = "仓库名称最大200字符")
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        private String typeId;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 联系人
         */
        @Size(max = 20, message = "联系人最大20字符")
        private String contacts;


        private ApproveStatusEnum approveStatusEnum;


        /**
         * 单据状态
         */
        private String approveStatusCode;


        /**
         * 联系人电话
         */
        @Size(max = 20, message = "联系人电话最大20字符")
        private String contactTelNumber;


        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        @NotNull(message = "仓库启用状态不能为空")
        private Boolean disabled;


        /**
         * 地址
         */
        @Size(max = 200, message = "仓库地址最大200字符")
        private String address;

        /**
         * 组织id 对应 核算公司表id
         */
        @NotBlank(message = "组织id 不能为空")
        private String orgId;


        /**
         * 是否虚拟仓
         * true 是
         */
        @NotNull(message = "是否是虚拟仓不能为空")
        private Boolean isVirtual;
    }


    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        @NotBlank(message = "id不能为空")
        private String id;
    }

    /**
     * 仓库列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;


        /**
         * 组织id
         */
        private String orgId;


        /**
         * disabled
         * true 禁用
         */
        private Boolean disabled;
    }


    /**
     * 仓库分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 表id
         */
        private String id;

        /**
         * 金蝶仓库编号
         */
        private String kingdeeWarehouseCode;

        /**
         * 名称
         */
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        private String typeId;


        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 负责人名
         */
        private String chargeName;


        /**
         * 是否虚拟仓
         * true 是
         */
        private Boolean isVirtual;
        /**
         * 联系人
         */
        private String contacts;

        /**
         * 联系人电话
         */
        private String contactTelNumber;

        /**
         * 状态
         * false  开启
         * true 关闭
         */
        private Boolean disabled;

        /**
         * 地址
         */
        private String address;

        /**
         * 组织id 对应 核算公司表id
         */
        private String orgId;

        /**
         * 组织名称
         */
        private String orgName;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 审核状态name
         */
        private String approveStatusName;

        /**
         * 审核状态code
         */
        private String approveStatusCode;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;


        /**
         * 创建人
         */
        private String CreateUserName;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 仓库名
         */
        private String name;


        /**
         * 仓库地址
         */
        private String address;


        /**
         * 联系人
         */
        private String contacts;


        /**
         * 创建人id
         */
        private List<String> createUserIdList;


        /**
         * 审核状态
         */
        private List<String> approveStatusList;

        /**
         * 类型id 集合
         */
        private List<String> typeIdList;

    }


}
