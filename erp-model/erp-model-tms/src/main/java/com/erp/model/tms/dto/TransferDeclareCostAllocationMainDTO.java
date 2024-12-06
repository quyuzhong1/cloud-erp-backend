package com.erp.model.tms.dto;

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
 * 中转费用分摊主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
*/
@Data
@NoArgsConstructor
public class TransferDeclareCostAllocationMainDTO implements Serializable {




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
        * 中转报关id
        */
        private String transferDeclareId;

        /**
        * 核算期间
        */
        private String reportDate;

        /**
        * 会计
        */
        private String accountDate;

        /**
        * 核算状态：toBeConfirm=待确认，confirmed=已确认
        */
        private String reportStatus;

        /**
        * 大表状态：toDo=待生成，done=已生成
        */
        private String bigTableStatus;


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
        * 中转报关id
        */
        @NotBlank(message = "中转报关id不能为空")
        @Size(max = 19,message = "中转报关id最大长度不能超过19位")
        private String transferDeclareId;

        /**
        * 核算期间
        */
        @NotBlank(message = "核算期间不能为空")
        @Size(max = 50,message = "核算期间最大长度不能超过50位")
        private String reportDate;

        /**
        * 会计
        */
        @NotBlank(message = "会计不能为空")
        @Size(max = 50,message = "会计最大长度不能超过50位")
        private String accountDate;

        /**
        * 核算状态：toBeConfirm=待确认，confirmed=已确认
        */
        @NotBlank(message = "核算状态：toBeConfirm=待确认，confirmed=已确认不能为空")
        @Size(max = 20,message = "核算状态：toBeConfirm=待确认，confirmed=已确认最大长度不能超过20位")
        private String reportStatus;

        /**
        * 大表状态：toDo=待生成，done=已生成
        */
        @NotBlank(message = "大表状态：toDo=待生成，done=已生成不能为空")
        @Size(max = 20,message = "大表状态：toDo=待生成，done=已生成最大长度不能超过20位")
        private String bigTableStatus;


    }


}