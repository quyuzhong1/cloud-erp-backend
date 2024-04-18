package com.erp.server.oms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.oms.entity.SoB2cLabelEntity;
import com.erp.server.oms.service.SoB2cLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("b2c订单标签")
@RequestMapping("/feign/soB2cLabel")
public class SoB2cLabelFeignController {
    @Resource
    private SoB2cLabelService soB2cLabelService;

    /**
     * 新增b2c订单标签
     * @param dtoList
     * @return
     */
    @PostMapping("/saveSoB2cLabel")
    public Boolean saveSoB2cLabel(@RequestBody List<SoB2cLabelDTO.UpdateDTO> dtoList) {
        Boolean result = soB2cLabelService.saveSoB2cLabel(dtoList);
        return result;
    }

    /**
     * 根据订单id查询标签
     * @param mainIds
     * @return
     */
    @PostMapping("/listSoB2cLabelByMainIds")
    public List<SoB2cLabelEntity> listSoB2cLabel(@RequestBody List<String> mainIds) {
        List<SoB2cLabelEntity> result = soB2cLabelService.listSoB2cLabelByMainIds(mainIds);
        return result;
    }
}