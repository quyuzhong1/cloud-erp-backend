package com.erp.model.wms.dto;

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
 * 虚拟仓渠道分区关联表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-01-03
*/
@Data
@NoArgsConstructor
public class VirtualWarehouseChannelPartitionRefDTO implements Serializable {




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
        * 排序
        */
        private Integer index;

        /**
        * 虚拟仓库渠道表id
        */
        private String mainId;

        /**
        * 分区id(erp-sys.dict_partition主键)
        */
        private String partitionId;


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
        * 排序
        */
        @NotNull(message = "排序不能为空")
        private Integer index;

        /**
        * 虚拟仓库渠道表id
        */
        @NotBlank(message = "虚拟仓库渠道表id不能为空")
        @Size(max = 19,message = "虚拟仓库渠道表id最大长度不能超过19位")
        private String mainId;

        /**
        * 分区id(erp-sys.dict_partition主键)
        */
        @NotBlank(message = "分区id(erp不能为空")
        @Size(max = 19,message = "分区id(erp最大长度不能超过19位")
        private String partitionId;


    }


}