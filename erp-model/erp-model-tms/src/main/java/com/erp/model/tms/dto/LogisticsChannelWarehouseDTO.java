package com.erp.model.tms.dto;

import com.erp.model.wms.dto.WarehouseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 渠道仓库设置表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-05-23
*/
@Data
@NoArgsConstructor
public class LogisticsChannelWarehouseDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 渠道id
        */
        private String logisticsChannelId;

        /**
        * 类型，all全部，part部分
        */
        private String type;

        /**
         * 仓库集合
         */
        private List<WarehouseDTO.ListDTO> warehouseList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        public AddDTO (String warehouseId,String logisticsChannelId,String type) {
            this.setWarehouseId(warehouseId);
            this.setLogisticsChannelId(logisticsChannelId);
            this.setType(type);
        }
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
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 19,message = "渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 类型，all全部，part部分
        */
        @NotBlank(message = "类型，all全部，part部分不能为空")
        @Size(max = 32,message = "类型，all全部，part部分最大长度不能超过32位")
        private String type;


    }



    @Data
    @NoArgsConstructor
    public static class BatchUpdateDTO {

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 类型（all全部，part部分）
         */
        @NotBlank(message = "类型不能为空")
        private String type;

    }
}