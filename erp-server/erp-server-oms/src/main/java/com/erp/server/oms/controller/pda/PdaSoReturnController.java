package com.erp.server.oms.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.service.SoReturnService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:销售退货单
 * @Author Luo_WG
 * @Date 2023/8/15 14:31
 **/
@RestController
@RequestMapping("/pdaSoReturn")
public class PdaSoReturnController extends BaseController {
    @Resource
    private SoReturnService soReturnService;

    /**
     * 获取所有已审核订单
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping(value = "/listSoReturnByApproveStatus")
    public ApiResult<List<SoReturnEntity>> listSoReturnByApproveStatus() {
        List<SoReturnEntity> entityList = soReturnService.listSoReturnByApproveStatus();
        return success(entityList);
    }
}
