package com.erp.server.dmp.controller.feign;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.controller.BaseController;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.*;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    private DmpPullTaskService dmpPullTaskService;

    @Resource
    private PlatformService platformService;

    @Autowired
    private BiSettlementExchangeRateService biSettlementExchangeRateService;

    @Resource
    private PlatformApiTaskService platformApiTaskService;


    @Resource
    private CfgAppClientService cfgAppClientService;


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
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("syncKingdeeId",dto.getId());
        map.put("code",dto.getNumber());
        return kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
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
         dmpPullTaskService.updateSyncInfo(paramDTO.getDmpSyncTaskId(),paramDTO.getSyncStatus(),paramDTO.getResponseMsg());
    }
    
    
    /**
     * 获取到所有的店铺信息
     * @author yl
     * @date 2023-07-06 12:26
     * @param
     * @return 
     */
    @PostMapping("/listShop")
    public List<DmpShopInfoEntity> listShop(){
        return dmpShopInfoService.list();
    }

    /**
     * 获取汇率
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    @PostMapping("/getRate")
    public BigDecimal getRate(@RequestParam(value = "date") String date, @RequestParam(value = "sourceCurrencyCode") String sourceCurrencyCode){
        return biSettlementExchangeRateService.findByCurrencyAndDate(date, sourceCurrencyCode);
    }

    /**
     * 从DmpSyncTask中查询金蝶的单据编号
     * @param conditon 查询过滤条件
     *                 支持：id，is_deleted，source_type，source_code，source_id，status，mq_tag，return_msg
     *                 注：lastSql 用于表示扩展SQL(慎用)
     * @return
     */
    @PostMapping("/getKingdeeSourceCode")
    public List<String> getKingdeeSourceCode(@RequestBody Map<String,Object> conditon){
        List<String> result=new ArrayList<>();

        result = dmpPullTaskService.listKingdeeCode(conditon);

        return result;
    }

    /**
     * 根据id获取到第三方应用信息
     * @author yl
     * @date 2023-08-28 16:22
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     */
    @PostMapping("/getCfgAppClient")
    public CfgAppClientEntity getCfgAppClient(@RequestBody CfgAppClientDTO.FindDTO dto){
        return cfgAppClientService.getCfgAppClient(dto);
    }

    /**
     * 新增第三方应用信息
     * @Author Luo_WG
     * @Date 2023/10/24 9:59
     * @param dto
     * @return java.lang.String
     **/
    @PostMapping("/cfgAppClient/add")
    public String addCfgAppClient(@RequestBody CfgAppClientDTO.AddDTO dto){
        return cfgAppClientService.add(dto);
    }

    /**
     * 修改第三方应用信息
     * @Author Luo_WG
     * @Date 2023/10/24 9:59
     * @param dto
     * @return java.lang.String
     **/
    @PostMapping("/cfgAppClient/update")
    public Boolean updateCfgAppClient(@RequestBody CfgAppClientDTO.UpdateDTO dto){
        return cfgAppClientService.update(dto);
    }

}
