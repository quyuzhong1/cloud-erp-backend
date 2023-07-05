package com.erp.model.wms.dto.sync;

import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @CreateTime: 2023-07-03  11:38
 * @Author: zhangchunlin
 */
@Data
public class MabangMachineInfoDTO implements Serializable {

    private MachineInfoEntity machineInfoEntity;

    private List<MachineDetailEntity> machineDetailEntityList;

    private List<MachineSubComponentsEntity> machineSubComponentsEntityList;

    private String operate;

    private String sourceType;

}