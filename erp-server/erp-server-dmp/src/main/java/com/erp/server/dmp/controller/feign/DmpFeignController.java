package com.erp.server.dmp.controller.feign;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.service.impl.TbTaskTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private BiDmpShopInfoService biDmpShopInfoService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private DmpPullTaskService dmpPullTaskService;

    @Resource
    private DmpPushMsgService dmpPushMsgService;

    @Resource
    private BiOrderInfoService biOrderInfoService;
    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private PlatformService platformService;

    @Autowired
    private BiSettlementExchangeRateService biSettlementExchangeRateService;

    @Resource
    private PlatformApiTaskService platformApiTaskService;


    @Resource
    private CfgAppClientService cfgAppClientService;


    @Resource
    private DmpSkuCostService dmpSkuCostService;


    @Resource
    private CfgApiAuthService cfgApiAuthService;
    @Resource
    private DmpOutputTaskRecordService dmpOutputTaskRecordService;

    @Resource
    private TbTaskTypeService tbTaskTypeService;

    @PostMapping("/getShopById")
    public BiShopInfoDTO getShopById(@RequestBody String shopId) {
        return biDmpShopInfoService.getShopById(shopId);
    }

    @PostMapping("/getByKingdeeId")
    public JSONObject getByKingdeeId(@RequestBody KingdeeDTO dto) {
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(dto.getKingdeePushModuleCode());
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            log.error("第三方平台【{}】未找到！", PlatformEnum.KINGDEE.getDesc());
            throw new ServiceException(ApiError.ERROR_97022);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("syncKingdeeId", dto.getId());
        map.put("code", dto.getNumber());
        return kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
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
     *
     * @param paramDTO
     * @author Will
     * @date: 2023/6/30 10:00
     */
    @PostMapping("/updateSyncInfo")
    public void updateSyncInfo(@RequestBody DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPullTaskService.updateSyncInfo(paramDTO.getDmpSyncTaskId(), paramDTO.getSyncStatus(), paramDTO.getResponseMsg());
    }


    /**
     * 获取到所有的店铺信息
     *
     * @param
     * @return
     * @author yl
     * @date 2023-07-06 12:26
     */
    @PostMapping("/listShop")
    public List<BiShopInfoEntity> listShop() {
        return biDmpShopInfoService.list();
    }

    /**
     * 获取汇率
     *
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    @PostMapping("/getRate")
    public BigDecimal getRate(@RequestParam(value = "date") String date, @RequestParam(value = "sourceCurrencyCode") String sourceCurrencyCode) {
        return biSettlementExchangeRateService.findByCurrencyAndDate(date, sourceCurrencyCode);
    }
    
    /**
     * 获取月度汇率
     *
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    @PostMapping("/getMonthRate")
    public BigDecimal getMonthRate(@RequestParam(value = "date") String date, @RequestParam(value = "sourceCurrencyCode") String sourceCurrencyCode) {
    	return biSettlementExchangeRateService.findByCurrencyAndMonth(date, sourceCurrencyCode);
    }

    /**
     * 从DmpSyncTask中查询金蝶的单据编号
     *
     * @param conditon 查询过滤条件
     *                 支持：id，is_deleted，source_type，source_code，source_id，status，mq_tag，return_msg
     *                 注：lastSql 用于表示扩展SQL(慎用)
     * @return
     */
    @PostMapping("/getKingdeeSourceCode")
    public List<String> getKingdeeSourceCode(@RequestBody Map<String, Object> conditon) {
        List<String> result = new ArrayList<>();

        result = dmpPullTaskService.listKingdeeCode(conditon);

        return result;
    }

    /**
     * 根据id获取到第三方应用信息
     *
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     * @author yl
     * @date 2023-08-28 16:22
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


    /**
     * 记录拉取数据记录
     *
     * @param dmpPullTaskEntity 查询过滤条件
     * @return
     */
    @PostMapping("/saveOrUpdate/pull/task")
    public String saveOrUpdateDmpPullTask(@RequestBody @Valid DmpPullTaskEntity dmpPullTaskEntity) {
        return dmpPullTaskService.saveOrUpdateDmpSyncTask(dmpPullTaskEntity);
    }

    /**
     * 记录推送数据记录
     *
     * @param dmpPushTaskEntity 查询过滤条件
     * @return
     */
    @PostMapping("/saveOrUpdate/push/task")
    public String saveOrUpdateDmpPushTask(@RequestBody @Valid DmpPushTaskEntity dmpPushTaskEntity) {
        return dmpPushTaskService.saveOrUpdateDmpSyncTask(dmpPushTaskEntity);
    }


    /**
     * @description: 拉取数据预警
     * @author Will
     * @date: 2023/11/17 14:35
     * @param syncTaskId
     * @return Boolean
     */
    @PostMapping("/pull/sendWarnMsg")
    public Boolean sendWarnMsg(@RequestBody String syncTaskId) {
        dmpPullTaskService.sendWarnMsg(syncTaskId);
        return Boolean.TRUE;
    }

    /**
     * 查询sku成本
     * @author Will
     * @date: 2023/12/13 18:02
     * @param skuNoList
     * @return List<DmpSkuCostEntity>
     */
    @PostMapping("/listRedisBySkuNoList")
    public List<DmpSkuCostEntity> listRedisBySkuNoList(@RequestBody List<String> skuNoList){
        return dmpSkuCostService.listRedisBySkuNoList(skuNoList);
    }


    /**
     * @description: 获取是否切换金蝶数据源
     * @author Will
     * @date: 2023/11/17 14:35
     * @param lastTime
     * @return Boolean
     */
    @PostMapping("/pull/needPushMQ")
    public Boolean needPushMQ(@RequestBody LocalDateTime lastTime) {
        KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils();
        return kingdeeApiUtils.notNeedPushMQ(lastTime);
    }

    /**
     * 查询权限设置
     * @author Will
     * @date: 2023/12/19 12:00
     * @param feignDTO
     * @return CfgApiAuthEntity
     */
    @PostMapping("/cfgApiAuth/getByKey")
    public CfgApiAuthEntity getByKey(@RequestBody CfgApiAuthDTO.FeignDTO feignDTO) {
        CfgApiAuthEntity authEntity = cfgApiAuthService.getByKey(feignDTO.getKey(), feignDTO.getApiGroup(), feignDTO.getApiPlatformId());
        return ObjectUtils.isEmpty(authEntity) ? new CfgApiAuthEntity() :authEntity ;
    }

    /**
     * 获取推送记录
     * @author zdy
     * @date: 2025/04/01 12:00
     * @param sourceCode
     * @param outputClass
     * @return Boolean
     */
    @GetMapping("/outputTaskRecord/getOutputTaskRecord")
    public List<DmpOutputTaskRecordEntity> getOutputTaskRecord(@RequestParam(value = "sourceCode") String sourceCode, @RequestParam(value = "outputClass") String outputClass) {
        if (CharSequenceUtil.isAllBlank(sourceCode,outputClass)){
            return new ArrayList<>();
        }
        return dmpOutputTaskRecordService.getOutputTaskRecord(sourceCode, outputClass);
    }

    /**
     * 获取推送记录
     * @author zdy
     * @date: 2025/04/01 12:00
     * @return Boolean
     */
    @GetMapping("/outputTaskRecord/getOutputTaskByIdAndType")
    public List<DmpOutputTaskRecordEntity> getOutputTaskByIdAndType(@RequestParam(value = "sourceIdList") List<String> sourceIdList, @RequestParam(value = "sourceType") String sourceType) {
        if (CollectionUtils.isEmpty(sourceIdList) || CharSequenceUtil.isAllBlank(sourceType)){
            return new ArrayList<>();
        }
        return dmpOutputTaskRecordService.getOutputTaskByIdAndType(sourceIdList, sourceType);
    }

    /**
     * 创建第三方任务
     */
    @PostMapping("/createThirdWarehouseTask")
    public void createThirdWarehouseTask(@RequestBody OverseasProviderEntity overseasProviderEntity){
        tbTaskTypeService.addNewDmpTask(overseasProviderEntity);
    }

    /**
     * 创建第三方任务
     */
    @PostMapping("/removeThirdWarehouseTask")
    public void removeThirdWarehouseTask(@RequestBody OverseasProviderEntity overseasProviderEntity){
        tbTaskTypeService.removeThirdWarehouseTask(overseasProviderEntity);
    }


    /**
     * 查询最新推送记录
     */
    @PostMapping("/pagingOutLatest")
    public PagingVO<DmpOutputTaskRecordDTO.PagingViewDTO> pagingOutLatest(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto){
       return dmpOutputTaskRecordService.pagingOutLatest(dto);
    }

    /**
     * 查询最新推送记录
     */
    @PostMapping("/batchCreateDmpPushMsg")
    public void batchCreateDmpPushMsg(@RequestBody List<DmpPushMsgEntity> msgList){
        if (CollectionUtils.isEmpty(msgList)){
            return;
        }
        dmpPushMsgService.saveBatch(msgList);
    }

    /**
     * 金蝶是否已审核
     * @author will
     * @date 2025/10/13 16:33
     * @param kingdeeDTO
     * @return String
     */
    @PostMapping("/checkKingdeeSyncApprove")
    public String checkKingdeeSyncApprove(@RequestBody KingdeeDTO kingdeeDTO){
        return kingdeeCommonService.checkKingdeeSyncApprove(kingdeeDTO);
    }

}
