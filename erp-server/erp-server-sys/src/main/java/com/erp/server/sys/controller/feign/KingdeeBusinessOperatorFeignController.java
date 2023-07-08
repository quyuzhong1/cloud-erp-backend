package com.erp.server.sys.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 金蝶业务员 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@RestController
@RequestMapping("/feign/kingdeeBusinessOperator")
public class KingdeeBusinessOperatorFeignController extends BaseController {

    @Resource
    private KingdeeBusinessOperatorService kingdeeBusinessOperatorService;


    /**
     * 获取业务员信息
     */
    @PostMapping("/FindBusinessOperator")
    public KingdeeBusinessOperatorEntity list(@RequestBody KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto) {
        KingdeeBusinessOperatorEntity entity = kingdeeBusinessOperatorService.find(dto);
        return entity;
    }

}
