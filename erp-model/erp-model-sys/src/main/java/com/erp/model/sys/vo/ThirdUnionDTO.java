package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ThirdUnionDTO

 * @Date 2022-11-15 11:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ThirdUnionDTO implements Serializable {

    private String thirdUnionId;

    private String userId;

    private String userName;

    private String thirdPartyType;

    private String thirdUserId;

    private String thirdOpenId;
}
