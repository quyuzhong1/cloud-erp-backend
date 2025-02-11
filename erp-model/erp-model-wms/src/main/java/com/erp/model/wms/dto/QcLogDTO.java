package com.erp.model.wms.dto;

import com.erp.model.wms.entity.QcInfoEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class QcLogDTO extends QcInfoEntity implements Serializable {
        /**
         *
         */
        private String warehouseName;
        /**
         *
         */
        private String supplierName;
    }