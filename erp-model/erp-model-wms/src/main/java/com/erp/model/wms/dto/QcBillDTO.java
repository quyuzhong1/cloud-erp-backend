package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname QcBill
 * @Description TODO
 * @Date 2023-04-14 15:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcBillDTO implements Serializable {


    /**
     * 添加质检规则
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends PermissionsDTO {



    }


}
