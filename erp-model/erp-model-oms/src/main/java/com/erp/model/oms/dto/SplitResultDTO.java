package com.erp.model.oms.dto;

import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class SplitResultDTO implements Serializable {

    private List<SoB2cDetailEntity> detailList;

    private List<SoB2cEntity> allEntityList;

}