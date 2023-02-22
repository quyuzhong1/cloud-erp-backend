package com.erp.model.bi.vo;

import lombok.*;

/**
 * 销售平台枚举下拉列表实体类
 *
 * @Author Cloud
 * @Date 2022/12/19 11:32
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SalesPlatformEnumVO {

    private String code;

    private String name;

    private String desc;

}
