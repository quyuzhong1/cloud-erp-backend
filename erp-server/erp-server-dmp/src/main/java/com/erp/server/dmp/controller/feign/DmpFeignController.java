package com.erp.server.dmp.controller.feign;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.controller.BaseController;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.DmpShopInfoService;
import com.erp.server.dmp.service.DmpSyncTaskService;
import com.erp.server.dmp.service.PlatformService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("feign")
public class DmpFeignController extends BaseController {

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private DmpSyncTaskService dmpSyncTaskService;

    @Resource
    private PlatformService platformService;


    @PostMapping("/getShopById")
    public DmpShopInfoDTO getShopById(@RequestBody String shopId) {
        return dmpShopInfoService.getShopById(shopId);
    }

    @PostMapping("/getByKingdeeId")
    public JSONObject getByKingdeeId(@RequestBody KingdeeDTO dto) {
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(dto.getKingdeePushModuleCode());
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)){
            log.error("第三方平台【{}】未找到！", PlatformEnum.KINGDEE.getDesc());
            throw new ServiceException(ApiError.ERROR_97022);
        }
        return kingdeeCommonService.view(apiUtils,platformEntity.getId(), dto.getId(), dto.getNumber());
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

    /**
     * 更新任务状态
     * @author Will
     * @date: 2023/6/30 10:00
     * @param paramDTO
     */
    @PostMapping("/updateSyncInfo")
    public void updateSyncInfo(@RequestBody DmpSyncMqDTO.ParamDTO paramDTO) {
         dmpSyncTaskService.updateSyncInfo(paramDTO.getDmpSyncTaskId(),paramDTO.getSyncStatus(),paramDTO.getResponseMsg());
    }

}
