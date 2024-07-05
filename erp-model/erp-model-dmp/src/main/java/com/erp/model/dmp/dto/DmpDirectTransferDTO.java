package com.erp.model.dmp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 中台直接调拨单请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
*/
@Data
@NoArgsConstructor
public class DmpDirectTransferDTO implements Serializable {




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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 第三方单据编号
        */
        private String thirdCode;

        /**
        * 销售平台原始单号
        */
        private String platformCode;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 状态 
waitSubmit：待提交 
approveIngt：审核中 
rejectt：审核不通过 
approvet：已审核
        */
        private String status;

        /**
        * 调拨类型
 InnerOrgTransfer：组织内调拨
OverOrgTransfer：跨组织调拨
        */
        private String transferType;

        /**
        * 调拨方向 
GENERAL：普通 
RETURN：退货
        */
        private String transferDirect;

        /**
        * 调出库存组织编码
        */
        private String outOrgCode;

        /**
        * 调出库存组织名称
        */
        private String outOrgName;

        /**
        * 调入库存组织编码
        */
        private String inOrgCode;

        /**
        * 调入库存组织名称
        */
        private String inOrgName;

        /**
        * 仓管员编码
        */
        private String warehouseKeeperCode;

        /**
        * 仓管员名称
        */
        private String warehouseKeeperName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        @NotBlank(message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...不能为空")
        @Size(max = 50,message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...最大长度不能超过50位")
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 25,message = "来源平台：gyy，kingdee，mabang最大长度不能超过25位")
        private String sourceSystem;

        /**
        * 第三方单据编号
        */
        @NotBlank(message = "第三方单据编号不能为空")
        @Size(max = 64,message = "第三方单据编号最大长度不能超过64位")
        private String thirdCode;

        /**
        * 销售平台原始单号
        */
        @NotBlank(message = "销售平台原始单号不能为空")
        @Size(max = 64,message = "销售平台原始单号最大长度不能超过64位")
        private String platformCode;


        /**
        * 调出库存组织编码
        */
        @NotBlank(message = "调出库存组织编码不能为空")
        @Size(max = 32,message = "调出库存组织编码最大长度不能超过32位")
        private String outOrgCode;

        /**
        * 调出库存组织名称
        */
        @NotBlank(message = "调出库存组织名称不能为空")
        @Size(max = 255,message = "调出库存组织名称最大长度不能超过255位")
        private String outOrgName;

        /**
        * 调入库存组织编码
        */
        @NotBlank(message = "调入库存组织编码不能为空")
        @Size(max = 32,message = "调入库存组织编码最大长度不能超过32位")
        private String inOrgCode;

        /**
        * 调入库存组织名称
        */
        @NotBlank(message = "调入库存组织名称不能为空")
        @Size(max = 255,message = "调入库存组织名称最大长度不能超过255位")
        private String inOrgName;

        /**
        * 仓管员编码
        */
        @NotBlank(message = "仓管员编码不能为空")
        @Size(max = 64,message = "仓管员编码最大长度不能超过64位")
        private String warehouseKeeperCode;

        /**
        * 仓管员名称
        */
        @NotBlank(message = "仓管员名称不能为空")
        @Size(max = 32,message = "仓管员名称最大长度不能超过32位")
        private String warehouseKeeperName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}