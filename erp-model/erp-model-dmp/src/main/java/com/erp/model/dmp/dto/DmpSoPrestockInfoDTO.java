package com.erp.model.dmp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.erp.model.dmp.dto.DmpSoPrestockDetailDTO.PrestockDetailDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 销售预入库主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
*/
@Data
@NoArgsConstructor
public class DmpSoPrestockInfoDTO implements Serializable {




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
        * 审核时间
        */
        private LocalDateTime checkTime;

        /**
        * 来源系统
        */
        private String sourceSystem;

        /**
        * 第三方单号
        */
        private String thirdCode;

        /**
        * 物流单号
        */
        private String logisticsNo;

        /**
        * 物流名称
        */
        private String logisticsName;

        /**
        * 货品数量
        */
        private Integer goodsCount;

        /**
        * 货品种类数
        */
        private Integer goodsTypeCount;

        /**
        * 入库人姓名
        */
        private String operatorName;

        /**
        * 审核员姓名
        */
        private String checkerName;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库编号
        */
        private String warehouseNo;

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
     * 详情
     */
     @Data
     @NoArgsConstructor
     public static class PrestockDTO {

         /**
         * 平台创建时间
         */
         private LocalDateTime platformCreateTime;

         /**
         * 平台修改时间
         */
         private LocalDateTime platformUpdateTime;

         /**
         * 审核时间
         */
         private LocalDateTime checkTime;

         /**
         * 来源系统
         */
         private String sourceSystem;

         /**
         * 第三方单号
         */
         private String thirdCode;

         /**
         * 物流单号
         */
         private String logisticsNo;

         /**
         * 物流名称
         */
         private String logisticsName;

         /**
         * 货品数量
         */
         private Integer goodsCount;

         /**
         * 货品种类数
         */
         private Integer goodsTypeCount;

         /**
         * 入库人姓名
         */
         private String operatorName;

         /**
         * 审核员姓名
         */
         private String checkerName;

         /**
         * 仓库id
         */
         private String warehouseId;

         /**
         * 仓库编号
         */
         private String warehouseNo;

         /**
         * 备注
         */
         private String remark;

         /**
         * 明细
         */
        private List<PrestockDetailDTO> detailList;
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
        * 审核时间
        */
        private LocalDateTime checkTime;

        /**
        * 来源系统
        */
        @NotBlank(message = "来源系统不能为空")
        @Size(max = 32,message = "来源系统最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 第三方单号
        */
        @NotBlank(message = "第三方单号不能为空")
        @Size(max = 64,message = "第三方单号最大长度不能超过64位")
        private String thirdCode;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 40,message = "物流单号最大长度不能超过40位")
        private String logisticsNo;

        /**
        * 物流名称
        */
        @NotBlank(message = "物流名称不能为空")
        @Size(max = 40,message = "物流名称最大长度不能超过40位")
        private String logisticsName;

        /**
        * 货品数量
        */
        @NotNull(message = "货品数量不能为空")
        private Integer goodsCount;

        /**
        * 货品种类数
        */
        @NotNull(message = "货品种类数不能为空")
        private Integer goodsTypeCount;

        /**
        * 入库人姓名
        */
        @NotBlank(message = "入库人姓名不能为空")
        @Size(max = 40,message = "入库人姓名最大长度不能超过40位")
        private String operatorName;

        /**
        * 审核员姓名
        */
        @NotBlank(message = "审核员姓名不能为空")
        @Size(max = 40,message = "审核员姓名最大长度不能超过40位")
        private String checkerName;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 32,message = "仓库id最大长度不能超过32位")
        private String warehouseId;

        /**
        * 仓库编号
        */
        @NotBlank(message = "仓库编号不能为空")
        @Size(max = 32,message = "仓库编号最大长度不能超过32位")
        private String warehouseNo;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
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