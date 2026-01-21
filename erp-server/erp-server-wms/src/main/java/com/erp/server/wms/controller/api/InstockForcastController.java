package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.server.wms.service.InstockForcastService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 入库预报表 前端控制器
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@AllArgsConstructor
@RestController
@RequestMapping("/instockForcast")
public class InstockForcastController extends BaseController {

    private final InstockForcastService instockForcastService;


    /**
     */
    @PostMapping(value = "/fixData")
    public void fixData(@RequestBody BaseIdsDTO.IdsDTO idsDTO) {
        instockForcastService.fixData(idsDTO.getIds());
    }

}
