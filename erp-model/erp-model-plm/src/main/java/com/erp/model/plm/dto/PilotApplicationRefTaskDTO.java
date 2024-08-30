package com.erp.model.plm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 试产/量产 关联任务请求响应实体
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class PilotApplicationRefTaskDTO implements Serializable {
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
        * 试产/量产主表ID
        */
        private String mainId;

        /**
        * 任务ID
        */
        private String taskId;

        private String spu;

        private String skuId;

        private String skuNo;
        /**
         * 产品id
         */
        private String productId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 阶段ID
         */
        private String phaseId;

        /**
         * 阶段名称
         */
        private String phaseName;

        /**
         * 任务名称
         */
        private String name;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 负责人名称
         */
        private String chargeName;

        /**
         * 任务状态
         */
        private Integer status;

        /**
         * 任务状态名称
         * TaskStateEnum
         */
        private String statusName;

        /**
         * 附件名称集合
         */
        private List<String> attachNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachUrlList;
    }

    /**
    * 新增
    */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;
    }

    /**
    * 修改
    */
    @EqualsAndHashCode(callSuper = true)
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
        * 试产/量产主表ID
        */
        private String mainId;

        /**
        * 任务ID
        */
        private String taskId;
    }


}