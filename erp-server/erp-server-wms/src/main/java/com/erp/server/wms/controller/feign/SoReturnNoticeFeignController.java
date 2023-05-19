package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.server.wms.service.SoReturnNoticeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soReturnNotice")
public class SoReturnNoticeFeignController {
    @Resource
    private SoReturnNoticeService soReturnNoticeService;

    /**
     * 根据来源id查询销售退货通知单主表
     * @Author Luo_WG
     * @Date 2023/5/15 12:29
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeEntity>
     **/
    @PostMapping("/listBySourceId")
    public List<SoReturnNoticeEntity> listBySourceId(@RequestBody List<String> sourceIds) {
        return soReturnNoticeService.listBySourceId(sourceIds);
    }
}
