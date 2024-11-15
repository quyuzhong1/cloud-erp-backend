package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 物流授权表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferLogisticsAuthDTO implements Serializable {




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
         * 物流平台
         */
        private String logisticsPlatform;

        /**
         * 物流商id
         */
        private String mainId;

        /**
         * name
         */
        private String name;



        private Map<String,String> fieldMap;

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
         * 物流平台
         * http://172.16.100.11:3002/project/128/interface/api/25522
         * key=logisticsPlatform
         */
        @NotBlank(message = "物流平台不能为空")
        @Size(max = 30,message = "物流平台最大长度不能超过30位")
        private String logisticsPlatform;

        /**
         * 物流商id
         */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 19,message = "物流商id最大长度不能超过19位")
        private String mainId;



        private Map<String,String> fieldMap;


    }

}