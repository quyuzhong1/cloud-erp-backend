package com.erp.model.dmp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 加工单请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@NoArgsConstructor
public class DmpMachineInfoDTO implements Serializable {


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
         private String searchType;

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
         * 搜索类型
         */
         private String  searchType;

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
        * 单据编号
        */
        private String code;

        /**
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 事务类型
        */
        private String workType;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;

        /**
        * 仓管员id
        */
        private String warehouseKeeperId;

        /**
        * 仓管员名称
        */
        private String warehouseKeeperName;

        /**
        * 领料人id
        */
        private String receiverId;

        /**
        * 领料人名称
        */
        private String receiverName;

        /**
        * 领料组织id
        */
        private String receiveOrgId;

        /**
        * 领料组织名称
        */
        private String receiveOrgName;

        /**
        * 单据类型
        */
        private String type;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 收货仓库id
        */
        private String warehouseId;

        /**
        * 收货仓库名称
        */
        private String warehouseName;

        /**
        * 来源平台，自研ERP 马帮 金蝶 管易
        */
        private String platformSign;

        /**
        * 来源id
        */
        private String sourceId;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 单据编号
        */
        private String code;

        /**
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 事务类型
        */
        private String workType;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;

        /**
        * 仓管员id
        */
        private String warehouseKeeperId;

        /**
        * 仓管员名称
        */
        private String warehouseKeeperName;

        /**
        * 领料人id
        */
        private String receiverId;

        /**
        * 领料人名称
        */
        private String receiverName;

        /**
        * 领料组织id
        */
        private String receiveOrgId;

        /**
        * 领料组织名称
        */
        private String receiveOrgName;

        /**
        * 单据类型
        */
        private String type;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 收货仓库id
        */
        private String warehouseId;

        /**
        * 收货仓库名称
        */
        private String warehouseName;

        /**
        * 来源平台，自研ERP 马帮 金蝶 管易
        */
        private String platformSign;

        /**
        * 来源id
        */
        private String sourceId;


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
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 事务类型
        */
        @NotBlank(message = "事务类型不能为空")
        @Size(max = 32,message = "事务类型最大长度不能超过32位")
        private String workType;

        /**
        * 库存组织id
        */
        @NotBlank(message = "库存组织id不能为空")
        @Size(max = 19,message = "库存组织id最大长度不能超过19位")
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        @NotBlank(message = "库存组织名称不能为空")
        @Size(max = 100,message = "库存组织名称最大长度不能超过100位")
        private String inventoryOrgName;

        /**
        * 仓管员id
        */
        @NotBlank(message = "仓管员id不能为空")
        @Size(max = 19,message = "仓管员id最大长度不能超过19位")
        private String warehouseKeeperId;

        /**
        * 仓管员名称
        */
        @NotBlank(message = "仓管员名称不能为空")
        @Size(max = 50,message = "仓管员名称最大长度不能超过50位")
        private String warehouseKeeperName;

        /**
        * 领料人id
        */
        @NotBlank(message = "领料人id不能为空")
        @Size(max = 19,message = "领料人id最大长度不能超过19位")
        private String receiverId;

        /**
        * 领料人名称
        */
        @NotBlank(message = "领料人名称不能为空")
        @Size(max = 50,message = "领料人名称最大长度不能超过50位")
        private String receiverName;

        /**
        * 领料组织id
        */
        @NotBlank(message = "领料组织id不能为空")
        @Size(max = 19,message = "领料组织id最大长度不能超过19位")
        private String receiveOrgId;

        /**
        * 领料组织名称
        */
        @NotBlank(message = "领料组织名称不能为空")
        @Size(max = 255,message = "领料组织名称最大长度不能超过255位")
        private String receiveOrgName;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 32,message = "单据类型最大长度不能超过32位")
        private String type;

        /**
        * 收货仓库id
        */
        @NotBlank(message = "收货仓库id不能为空")
        @Size(max = 19,message = "收货仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 收货仓库名称
        */
        @NotBlank(message = "收货仓库名称不能为空")
        @Size(max = 100,message = "收货仓库名称最大长度不能超过100位")
        private String warehouseName;

        /**
        * 来源平台，自研ERP 马帮 金蝶 管易
        */
        @NotBlank(message = "来源平台，自研ERP 马帮 金蝶 管易不能为空")
        @Size(max = 32,message = "来源平台，自研ERP 马帮 金蝶 管易最大长度不能超过32位")
        private String platformSign;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 32,message = "来源id最大长度不能超过32位")
        private String sourceId;


    }


}