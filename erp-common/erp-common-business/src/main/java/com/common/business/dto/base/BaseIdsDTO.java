package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/22 11:34
 */
@Data
@NoArgsConstructor
public class BaseIdsDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class IdsDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

    }

    @Data
    @NoArgsConstructor
    public static class RemarkDTO extends IdsDTO{

        @NotBlank(message = "备注不能为空")
        private String remark;

    }


}
