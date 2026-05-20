package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * FBA拣货扩展表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-12-24
*/
@Data
@NoArgsConstructor
public class FbaShipmentExtendDTO implements Serializable {



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
        * 主表id
        */
        private String mainId;

        /**
        * 入库计划单号
        */
        private String planCode;

        /**
        * 发货单号
        */
        private String deliveryCode;

        /**
        * 发货单ID
        */
        private String deliveryId;

        /**
        * 地区偏好
        */
        private String preferredRegion;

        /**
        * 发货人
        */
        private String deliveryFromName;

        /**
        * 发货手机号
        */
        private String deliveryFromMobile;

        /**
        * 发货城市
        */
        private String deliveryFromCity;

        /**
        * 发货州/省
        */
        private String deliveryFromProvince;

        /**
        * 发货地区
        */
        private String deliveryFromArea;

        /**
        * 发货邮编
        */
        private String deliveryFromPostCode;

        /**
        * 发货目的仓（取值店铺绑定的AWD仓）
        */
        private String deliveryToWarehouseId;

        /**
        * 平台货件发货时间（拉取数据的日期）
        */
        private LocalDateTime shipmentDeliveryTime;

        /**
        * 收货电话号码
        */
        private String deliveryToMobile;

        /**
        * 收货人
        */
        private String deliveryToName;

        /**
        * 收货邮编
        */
        private String deliveryToPostCode;

        /**
        * 收货地区
        */
        private String deliveryToArea;

        /**
        * 收货州/省
        */
        private String deliveryToProvince;

        /**
        * 收货城市
        */
        private String deliveryToCity;

        /**
        * 收货国家
        */
        private String deliveryToCountryId;

        /**
        * 收货国家名称
        */
        private String deliveryToCountryName;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


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
        * 主表id
        */
        private String mainId;

        /**
        * 入库计划单号
        */
        private String planCode;

        /**
        * 发货单号
        */
        private String deliveryCode;

        /**
        * 发货单ID
        */
        private String deliveryId;

        /**
        * 地区偏好
        */
        private String preferredRegion;

        /**
        * 发货人
        */
        private String deliveryFromName;

        /**
        * 发货手机号
        */
        private String deliveryFromMobile;

        /**
        * 发货城市
        */
        private String deliveryFromCity;

        /**
        * 发货州/省
        */
        private String deliveryFromProvince;

        /**
        * 发货地区
        */
        private String deliveryFromArea;

        /**
        * 发货邮编
        */
        private String deliveryFromPostCode;

        /**
        * 发货目的仓（取值店铺绑定的AWD仓）
        */
        private String deliveryToWarehouseId;

        /**
        * 平台货件发货时间（拉取数据的日期）
        */
        private LocalDateTime shipmentDeliveryTime;

        /**
        * 收货电话号码
        */
        private String deliveryToMobile;

        /**
        * 收货人
        */
        private String deliveryToName;

        /**
        * 收货邮编
        */
        private String deliveryToPostCode;

        /**
        * 收货地区
        */
        private String deliveryToArea;

        /**
        * 收货州/省
        */
        private String deliveryToProvince;

        /**
        * 收货城市
        */
        private String deliveryToCity;

        /**
        * 收货国家
        */
        private String deliveryToCountryId;

        /**
        * 收货国家名称
        */
        private String deliveryToCountryName;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 入库计划单号
        */
        @NotBlank(message = "入库计划单号不能为空")
        @Size(max = 64,message = "入库计划单号最大长度不能超过64位")
        private String planCode;

        /**
        * 发货单号
        */
        @NotBlank(message = "发货单号不能为空")
        @Size(max = 64,message = "发货单号最大长度不能超过64位")
        private String deliveryCode;

        /**
        * 发货单ID
        */
        @NotBlank(message = "发货单ID不能为空")
        @Size(max = 19,message = "发货单ID最大长度不能超过19位")
        private String deliveryId;

        /**
        * 地区偏好
        */
        @NotBlank(message = "地区偏好不能为空")
        @Size(max = 64,message = "地区偏好最大长度不能超过64位")
        private String preferredRegion;

        /**
        * 发货人
        */
        @NotBlank(message = "发货人不能为空")
        @Size(max = 100,message = "发货人最大长度不能超过100位")
        private String deliveryFromName;

        /**
        * 发货手机号
        */
        @NotBlank(message = "发货手机号不能为空")
        @Size(max = 64,message = "发货手机号最大长度不能超过64位")
        private String deliveryFromMobile;

        /**
        * 发货城市
        */
        @NotBlank(message = "发货城市不能为空")
        @Size(max = 100,message = "发货城市最大长度不能超过100位")
        private String deliveryFromCity;

        /**
        * 发货州/省
        */
        @NotBlank(message = "发货州/省不能为空")
        @Size(max = 100,message = "发货州/省最大长度不能超过100位")
        private String deliveryFromProvince;

        /**
        * 发货地区
        */
        @NotBlank(message = "发货地区不能为空")
        @Size(max = 100,message = "发货地区最大长度不能超过100位")
        private String deliveryFromArea;

        /**
        * 发货邮编
        */
        @NotBlank(message = "发货邮编不能为空")
        @Size(max = 50,message = "发货邮编最大长度不能超过50位")
        private String deliveryFromPostCode;

        /**
        * 发货目的仓（取值店铺绑定的AWD仓）
        */
        @NotBlank(message = "发货目的仓（取值店铺绑定的AWD仓）不能为空")
        @Size(max = 19,message = "发货目的仓（取值店铺绑定的AWD仓）最大长度不能超过19位")
        private String deliveryToWarehouseId;

        /**
        * 平台货件发货时间（拉取数据的日期）
        */
        private LocalDateTime shipmentDeliveryTime;

        /**
        * 收货电话号码
        */
        @NotBlank(message = "收货电话号码不能为空")
        @Size(max = 64,message = "收货电话号码最大长度不能超过64位")
        private String deliveryToMobile;

        /**
        * 收货人
        */
        @NotBlank(message = "收货人不能为空")
        @Size(max = 100,message = "收货人最大长度不能超过100位")
        private String deliveryToName;

        /**
        * 收货邮编
        */
        @NotBlank(message = "收货邮编不能为空")
        @Size(max = 50,message = "收货邮编最大长度不能超过50位")
        private String deliveryToPostCode;

        /**
        * 收货地区
        */
        @NotBlank(message = "收货地区不能为空")
        @Size(max = 100,message = "收货地区最大长度不能超过100位")
        private String deliveryToArea;

        /**
        * 收货州/省
        */
        @NotBlank(message = "收货州/省不能为空")
        @Size(max = 100,message = "收货州/省最大长度不能超过100位")
        private String deliveryToProvince;

        /**
        * 收货城市
        */
        @NotBlank(message = "收货城市不能为空")
        @Size(max = 100,message = "收货城市最大长度不能超过100位")
        private String deliveryToCity;

        /**
        * 收货国家
        */
        @NotBlank(message = "收货国家不能为空")
        @Size(max = 10,message = "收货国家最大长度不能超过10位")
        private String deliveryToCountryId;

        /**
        * 收货国家名称
        */
        @NotBlank(message = "收货国家名称不能为空")
        @Size(max = 64,message = "收货国家名称最大长度不能超过64位")
        private String deliveryToCountryName;


    }


}