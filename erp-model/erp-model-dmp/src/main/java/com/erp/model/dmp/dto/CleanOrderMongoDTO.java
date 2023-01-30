package com.erp.model.dmp.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author CLOUD
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CleanOrderMongoDTO {
    @Panno(findType = PannoEnum.IN,field = "fBillNo")
    private List<String> billNo;

    @Panno(findType = PannoEnum.IN,field = "fOrderNo")
    private List<String> orderNo;

    @Panno(findType = PannoEnum.IN,field = "code")
    private List<String> code;

}
