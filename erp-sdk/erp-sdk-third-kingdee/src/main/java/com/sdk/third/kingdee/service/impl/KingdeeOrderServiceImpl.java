package com.sdk.third.kingdee.service.impl;

import com.common.business.dto.PlatformOrderDataDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.service.IReportSaveService;

import java.util.List;

/**
 * 金蝶订单数据拉取服务
 *
 * @Author Cloud
 * @Date 2023/8/29 15:08
 **/
public class KingdeeOrderServiceImpl implements IReportSaveService<PlatformOrderDataDTO> {

    /**
     * 下载数据
     * @param dto
     */
    @Override
    public void pullDataSave(RequestDTO dto) {

    }

    @Override
    public void cleanDataSave(String tableName, int size) {

    }

    @Override
    public void updateAndSaveDb(PlatformOrderDataDTO mongoDatum) {

    }

    @Override
    public List<PlatformOrderDataDTO> downloadData(RequestDTO dto) {
        // 调用平台接口下载数据

        // 封装数据

        return null;
    }
}
