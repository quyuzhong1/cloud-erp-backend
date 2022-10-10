package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Classname ProductPhaseDistrDTO
 * @Description TODO
 * @Date 2022-10-10 15:04
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductPhaseDistributeDTO implements Serializable {

    /**
     * 阶段名
     */
    private String phaseName;

    private List<Map<String,Object>> phaseDataList;


}
