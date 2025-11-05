package com.erp.model.fms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 资产处置单实物明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-29
*/
@Data
@NoArgsConstructor
public class AssetDisposalPhysicalDetailDTO implements Serializable {




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
        * 来源明细ID
        */
        private String assetDisposalDetailId;

        /**
         * 来源明细ID
         */
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 资产位置ID
        */
        private String assetLocationId;

        /**
        * 资产位置名称
        */
        private String assetLocationName;

        /**
        * 数量
        */
        private Integer qty;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 来源明细ID
        */
        private String assetDisposalDetailId;

        /**
         * 来源明细ID
         */
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 资产编码
        */
        @NotBlank(message = "资产编码不能为空")
        private String assetCode;

        /**
        * 资产位置ID http://172.16.100.11:3002/project/163/interface/api/39465  /fms/assetLocation/drop/down/list
        */
        @NotBlank(message = "资产位置ID不能为空")
        private String assetLocationId;

        /**
        * 资产位置名称
        */
        private String assetLocationName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;


    }


}