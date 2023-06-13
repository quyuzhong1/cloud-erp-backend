package com.erp.server.wms.controller.feign;

import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.erp.server.wms.service.SoReturnReceiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soReturnReceive")
public class SoReturnReceiveFeignController {

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    /**
     * 根据详情id查询退货签收单详情
     * @Author Luo_WG
     * @Date 2023/5/19 12:03
     * @param ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    @PostMapping("/listDetailByIds")
    public List<SoReturnReceiveDetailEntity> listDetailByIds(@RequestBody List<String> ids) {
        List<SoReturnReceiveDetailEntity> list = soReturnReceiveDetailService.listDetailByIds(ids);
        return list;
    }

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeDetailEntity>
     **/
    @PostMapping("/listDetailBySourceIds")
    public List<SoReturnReceiveDetailEntity> listDetailBySourceIds(@RequestBody List<String> ids) {
        List<SoReturnReceiveDetailEntity> list = soReturnReceiveDetailService.listDetailBySourceIds(ids);
        return list;
    }

    /**
     * 根据来源id查询销售退货通知单主表
     * @Author Luo_WG
     * @Date 2023/5/15 12:29
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveEntity>
     **/
    @PostMapping("/listBySourceId")
    public List<SoReturnReceiveEntity> listBySourceId(@RequestBody List<String> sourceIds) {
        List<SoReturnReceiveEntity> list = soReturnReceiveService.listBySourceIds(sourceIds);
        return list;
    }
}
