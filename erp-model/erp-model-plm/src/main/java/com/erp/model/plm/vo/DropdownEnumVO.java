package com.erp.model.plm.vo;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DropdownEnumVO {

    private Integer code;

    private String name;

    private String desc;

}