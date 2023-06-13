package com.erp.server.dmp.controller.feign;

import cn.hutool.json.JSONObject;
import com.common.core.controller.BaseController;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @description: 远程调用控制层
 * @date: 2023/1/11 17:46
 */
@RestController
@RequestMapping("feign")
public class DmpFeignController extends BaseController {

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @PostMapping("/getShopById")
    public DmpShopInfoDTO getShopById(@RequestBody String shopId) {
        return dmpShopInfoService.getShopById(shopId);
    }

    @PostMapping("/getByKingdeeId")
    public JSONObject getByKingdeeId(@RequestBody KingdeeDTO dto) {
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(dto.getKingdeePushModuleCode());
        return kingdeeCommonService.view(apiUtils, dto.getId(), dto.getNumber());
    }


    /**
     * 生成销售变更单
     *
     * @param paramMap
     * @return cn.hutool.json.JSONObject
     * @author yl
     * @date 2023-06-07 10:44
     */
    @PostMapping("/createkingdeeSoChange")
    public String createkingdeeSoChange(@RequestBody Map<String, Object> paramMap) {
        return kingdeeCommonService.createkingdeeSoChange(paramMap);
    }

}
