package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

import java.io.Serializable;

/**
 * @Classname ThirdUnionDTO
 * @Description TODO
 * @Date 2022-11-15 11:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ThirdUnionDTO implements Serializable {

    private String thirdUnionId;

    private String userId;

    private String thirdPartyType;
}
