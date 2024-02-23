package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerBean {
    /**
     * 用户id 例：191167
     */
    private int id;
    /**
     * 用户邮箱 例：john@example.com
     */
    private String email;
    /**
     * 电话 例：555-625-1199
     */
    private String phone;

}
