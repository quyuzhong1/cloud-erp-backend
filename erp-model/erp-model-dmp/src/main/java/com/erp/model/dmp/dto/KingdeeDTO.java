package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname KingdeeDTO
 * @Description TODO
 * @Date 2023-06-06 18:56
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class KingdeeDTO  implements Serializable {

    private String kingdeePushModuleCode;

    private String id;

    private String number;
}
