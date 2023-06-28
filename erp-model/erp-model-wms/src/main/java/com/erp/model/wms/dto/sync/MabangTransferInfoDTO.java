package com.erp.model.wms.dto.sync;

import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @CreateTime: 2023-06-28  12:29
 * @Author: zhangchunlin
 */
@Data
public class MabangTransferInfoDTO implements Serializable {

    private TransferInfoEntity transferInfo;

    private List<TransferInfoDetailEntity> transferList;

    private String operate;

    private String sourceType;

}