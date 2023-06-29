package com.erp.server.dmp.pull.service.mabang;

import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.ShipmentEntity;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 马帮调拨发货列表
 * @CreateTime: 2023-06-29  16:02
 * @Author: zhangchunlin
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.MABANG_SHIPMENT)
public class MabangShipmentServiceImpl implements IReportSaveService<ShipmentEntity> {

    @Override
    public void pullDataSave(RequestDTO dto) {

    }

    @Override
    public void cleanDataSave(String tableName, int size) {

    }

    @Override
    public void updateAndSaveDb(ShipmentEntity mongoDatum) {

    }

}