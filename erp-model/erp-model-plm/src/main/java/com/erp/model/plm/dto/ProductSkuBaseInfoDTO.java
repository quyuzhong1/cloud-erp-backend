package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductSkuBaseInfoDTO {

    /**
     * 产品sku表id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品信息表主id
     */
    private String productId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 计划上市时间
     */
    private LocalDate planListingTime;

    /**
     * 首批量产入库时间
     */
    private LocalDate firstMassProductDate;

    /**
     * 单位表id
     */
    private String unitId;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "产品状态错误")
    private Integer productState;

    /**
     * 产品图片（逗号分隔的URL字符串，保持向后兼容）
     */
    private String imagesUrl;
    
    /**
     * 产品图片信息列表（包含URL和名称）
     */
    private List<ImageInfo> imageInfoList;

    /**
     * 产品负责人id
     */
    private String chargeId;

    /**
     * 产品负责人名称
     */
    private String chargeName;

    /**
     * 品名
     */
    @Size(max = 50,message = "产品名称最大50字符")
    private String name;

    /**
     * 品名(英文)
     */
    @Size(max = 500,message = "品名(英文)最大500字符")
    private String nameEn;

    /**
     * 是否已完成任务
     * 0 没有 1 已完成
     */
    private Integer isFinishTask;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过
     */
    private Integer status;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程表id
     */
    private String businessProcessId;

    /**
     * 推荐仓位(小货区)
     */
    private String warehouseLocation;

    /**
     * 推荐仓位(大货区)
     */
    private String warehouseLocationLarge;
    
    /**
     * 图片信息（名称+URL）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        /**
         * 图片名称（新增图片时传入，旧图片可为空）
         */
        private String imageName;
        
        /**
         * 图片URL
         */
        private String imageUrl;
    }
}
