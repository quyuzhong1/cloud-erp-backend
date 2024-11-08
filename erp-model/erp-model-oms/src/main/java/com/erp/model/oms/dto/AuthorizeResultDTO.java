package com.erp.model.oms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
public class AuthorizeResultDTO {
    /**
     * 店铺id
     */
    private List<String> shopIdList;

    /**
     * 授权是否成功
     * true 返回授权成功 false 授权失败
     */
    private Boolean isAuthorize;
}
