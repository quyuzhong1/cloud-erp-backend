package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/22 11:34
 */
@Data
@NoArgsConstructor
public class BaseIdsDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class IdsDTO extends PermissionsDTO {

        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

    }

    @Data
    @NoArgsConstructor
    public static class DetailIdListDTO extends PermissionsDTO {

        /**
         * 明细表 detailIdList
         */
        @NotEmpty(message = "detailIdList不能为空")
        private List<String> detailIdList;

    }

    @Data
    @NoArgsConstructor
    public static class RemarkDTO extends IdsDTO{

        @NotBlank(message = "填写信息不能为空")
        @Size(max = 255,message = "填写信息不能超过255字符")
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class QtyDTO extends IdsDTO{

        @NotNull(message = "填写信息不能为空")
        private Integer qty;

    }

    @Data
    @NoArgsConstructor
    public static class DateDTO extends IdsDTO{

        @NotNull(message = "日期不能为空")
        private LocalDate LocalDate;
    }

    /**
     * 批量修改 对应记录的 多条明细值
     */
    @Data
    @NoArgsConstructor
    public static class ChangeDTO extends IdsDTO{
        /**
         * 修改明细可以为空
         */
        private List<String> changeIds;
    }
}
