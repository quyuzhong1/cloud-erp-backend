package com.erp.model.tms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 截单设置请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
*/
@Data
@NoArgsConstructor
public class TransferDeclareDeadlineSettingDTO implements Serializable {




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
         * 中转物流商id
         */
        private List<String> transferLogisticsSupplierIdList;

        /**
        * 截单时间
        */
        private LocalDateTime deadlineTime;

        /**
        * 生成时间
        */
        private LocalDateTime generateTime;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 中转物流商id
         */
        private List<String> transferLogisticsSupplierIdList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 截单时间
        */
        private LocalDateTime deadlineTime;

        /**
        * 生成时间
        */
        private LocalDateTime generateTime;


    }


}