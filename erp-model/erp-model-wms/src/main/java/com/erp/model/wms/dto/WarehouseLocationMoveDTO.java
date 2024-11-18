package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓位移动主表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
*/
@Data
@NoArgsConstructor
public class WarehouseLocationMoveDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class PdaTabListDTO {

         /**
          * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
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
         * 审核状态
         */
         private List<String> ApproveStatusList;

         /**
         * 单据日期
         */
         private List<LocalDate> billDateList;

         /**
         * sku编号
         */
         private List<String> skuNoList;

         /**
         * 作废状态
         */
         private Boolean invalidStatus;

         /**
         * 仓库id
         */
         private List<String>  warehouseIdList;
         /**
         * 创建人id
         */
         private List<String>  createUserIdList;
         /**
         * 明细id
         */
         private List<String>  detailIds;
         /**
         * 创建时间
         */
         private List<String>  createTimeList;
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
        * 单据编号
        */
        private String code;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;
        /**
        * 仓库id--兼容移动端pda历史数据
        */
        private String infoWarehouseId;

        /**
        * 仓库名称--兼容移动端pda历史数据
        */
        private String infoWarehouseName;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;


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

        public String getWarehouseId() {
            if (StringUtils.isBlank(warehouseId)){
                return infoWarehouseId;
            }
            return warehouseId;
        }
        public String getWarehouseName() {
            if (StringUtils.isBlank(warehouseName)){
                return infoWarehouseName;
            }
            return warehouseId;
        }
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
        /**
        * 勾选的明细id集合
        */
        private List<String> detailIds;
        /**
         * 审核状态
         */
        private List<String> ApproveStatusList;

        /**
         * 单据日期
         */
        private List<LocalDate> billDateList;

        /**
         * sku编号
         */
        private List<String> skuNoList;

        /**
         * 仓库id
         */
        private List<String>  warehouseIdList;
        /**
         * 创建人id
         */
        private List<String>  createUserIdList;
        /**
         * 创建时间
         */
        private List<String>  createTimeList;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<WarehouseLocationMoveDTO.DetailViewDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
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
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核时间
        */
        private LocalDate billDate;

        /**
         * 明细信息
         */
        private List<WarehouseLocationMoveDetailDTO.ViewDTO> detailList;
    }
    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class PcViewDTO {

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
        * 审核状态
        */
        private String approveStatusName;


        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核时间
        */
        private LocalDate billDate;

        /**
         * 明细信息
         */
        private List<DetailViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 仓位移动明细
         */
        @NotEmpty(message = "仓位移动明细不能为空")
        private List<WarehouseLocationMoveDetailDTO.AddDTO> detailList;
        /**
         * 是否是pc端访问
         */
        private Boolean pcShow = false;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源编号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class PcAddDTO  {

        /**
         * 仓位移动明细
         */
        @NotEmpty(message = "仓位移动明细不能为空")
        private List<WarehouseLocationMoveDetailDTO.AddDTO> detailList;
        /**
         * 仓位
         */
        private String inWarehouseLocation;

        /**
         * 取出仓位
         */
        private String outWarehouseLocation;
        /**
         * 是否是pc端访问
         */
        private Boolean pcShow = false;

        /**
         * 仓库id
         */
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;
        /**
         * 单据时间
         */
        private LocalDate billDate;
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
         * 仓位移动明细
         */
        @NotEmpty(message = "仓位移动明细不能为空")
        private List<WarehouseLocationMoveDetailDTO.UpdateDTO> detailList;
        /**
         * 是否是pc端访问
         */
        private Boolean pcShow = false;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class PcUpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 仓位移动明细
         */
        @NotEmpty(message = "仓位移动明细不能为空")
        private List<WarehouseLocationMoveDetailDTO.UpdateDTO> detailList;
        /**
         * 是否是pc端访问
         */
        private Boolean pcShow = false;

        /**
         * 仓库id
         */
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;
        /**
         * 单据时间
         */
        private LocalDate billDate;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

    }


    /**
     * PDA:分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PdaListDTO {

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
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 产品明细数量
         */
        private Integer detailCount;

        /**
         * 产品信息
         */
        private List<PdaItemDTO> itemList;
    }
    /**
     * PDA:分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PdaPcListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 明细id
         */
        private String  detailId;

        /**
         * 仓库id--兼容移动端pda历史数据
         */
        private String infoWarehouseId;

        /**
         * 仓库名称--兼容移动端pda历史数据
         */
        private String infoWarehouseName;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 审核人
         */
        private String approveUserId;
        /**
         * 审核人姓名
         */
        private String approveUserName;
        /**
         * 创建人
         */
        private String createUserId;
        /**
         * 创建人姓名
         */
        private String createUserName;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 产品明细数量
         */
        private Integer detailCount;
        /**
         * 产品明细数量
         */
        private String remark;

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 调拨数量
         */
        private Integer qty;
        /**
         * 仓位
         */
        private String inWarehouseLocation;
        /**
         * 仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 取出仓位
         */
        private String outWarehouseLocation;

        /**
         * 取出仓位名称
         */
        private String outWarehouseLocationName;
    }    /**
     * PDA:分页列表
     */
    @Data
    @NoArgsConstructor
    public static class DetailViewDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 明细id
         */
        private String detailId;
        /**
         * 单据编号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 审核人
         */
        private String approveUserId;
        /**
         * 审核人姓名
         */
        private String approveUserName;
        /**
         * 创建人
         */
        private String createUserId;
        /**
         * 创建人姓名
         */
        private String createUserName;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 仓库id--兼容移动端pda历史数据
         */
        private String infoWarehouseId;

        /**
         * 仓库名称--兼容移动端pda历史数据
         */
        private String infoWarehouseName;
        /**
         * 取货仓位库存状态
         */
        private String outInventoryStatus;
        /**
         * 取货仓位库存状态名字
         */
        private String outInventoryStatusName;
        /**
         * 上架仓位库存状态
         */
        private String inInventoryStatus;
        /**
         * 上架仓位库存状态名字
         */
        private String inInventoryStatusName;
        /**
         * 产品明细数量
         */
        private Integer detailCount;
        /**
         * 产品明细数量
         */
        private String remark;

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 调拨数量
         */
        private Integer qty;
        /**
         * 仓位
         */
        private String inWarehouseLocation;
        /**
         * 仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 取出仓位
         */
        private String outWarehouseLocation;

        /**
         * 取出仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 实际库存
         */
        private Integer realQty = 0;
        /**
         * 可用库存
         */
        private Integer usableQty = 0;
        /**
         * 冻结库存
         */
        private Integer frozenQty = 0;

    }

    /**
     * PDA:商品详情
     */
    @Data
    @NoArgsConstructor
    public static class PdaItemDTO {
        /**
         * 详情id
         */
        private String id;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 调拨数量
         */
        private String qty;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 仓位
         */
        private String inWarehouseLocation;
        /**
         * 仓位名称
         */
        private String inWarehouseLocationName;
    }
    /**
     * PDA:商品详情
     */
    @Data
    @NoArgsConstructor
    public static class PdaPcItemDTO {
        /**
         * 详情id
         */
        private String id;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 调拨数量
         */
        private String qty;
        /**
         * 仓位
         */
        private String inWarehouseLocation;
        /**
         * 仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 取出仓位
         */
        private String outWarehouseLocation;

        /**
         * 取出仓位名称
         */
        private String outWarehouseLocationName;
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class Group {
        /**
         * 取货仓位库存状态
         */
        private String outInventoryStatus;
        /**
         * 上架仓位库存状态
         */
        private String inInventoryStatus;
    }


    /**
     * 生成拣货单--缺货--仓位移动实体
     */
    @Data
    @NoArgsConstructor
    public static class GenPickToSkuMove{

        /**
         *
         */
        private String id;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 调拨数量
         */
        private Integer qty;
        /**
         * 仓位
         */
        private String inWarehouseLocation;
        /**
         * 仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 取出仓位
         */
        private String outWarehouseLocation;

        /**
         * 取出仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 取货仓位库存状态
         */
        private String outInventoryStatus;
        /**
         * 上架仓位库存状态
         */
        private String inInventoryStatus;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouse;
    }
}