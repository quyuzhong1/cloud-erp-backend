package com.erp.model.dmp.dto;

import java.util.List;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CLOUD
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DmpInputMongoUniqueDTO {
    @Panno(findType = PannoEnum.IN,field = "uniqueEncrypt")
    private List<String> uniqueEncrypt;

}
