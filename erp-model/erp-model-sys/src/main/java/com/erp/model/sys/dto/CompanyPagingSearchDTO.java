package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 核算公司 搜索条件
 * @Description TODO
 * @Date 2022-07-29 16:33
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CompanyPagingSearchDTO  implements Serializable {

    private String searchKeyword;


    //状态 1 正常  0 不正常
    @Range(min = 0, max = 1, message = "状态类型错误")
    private Integer state;

    @NotBlank(message = "搜索类型不能为空")
    //   @Pattern(regexp = "^[company_name contact_name contact_address currency]$", message = "搜素类型有误")
    private String searchType;
}
