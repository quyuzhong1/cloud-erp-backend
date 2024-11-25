package com.erp.model.oms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SoInfoToSdyDTO {

    private String soId;

    private String operateEnum;

    private SoInfoDTO.ViewDTO viewDTO;
}
