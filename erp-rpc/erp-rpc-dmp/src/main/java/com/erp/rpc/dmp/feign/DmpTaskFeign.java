package com.erp.rpc.dmp.feign;


import cn.hutool.json.JSONObject;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.SettingEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @description: DMP远程调用接口
 * @date: 2023/1/12 16:54
 */
@FeignClient("erp-dmp")
public interface DmpTaskFeign {

    /**
     * 店铺id查询店铺
     */
    @PostMapping("feign/getShopById")
    BiShopInfoDTO getShopById(@RequestBody String shopId);


    /**
     * 获取所有的店铺信息
     */
    @PostMapping("feign/listShop")
    List<BiShopInfoEntity> listShop();

    /**
     * 根据条件获详情
     *
     * @param dto
     * @return cn.hutool.json.JSONObject
     * @author yl
     * @date 2023-06-07 10:42
     */
    @PostMapping("feign/getByKingdeeId")
    JSONObject getByKingdeeId(@RequestBody KingdeeDTO dto);

    /**
     * 生成金蝶销售变更单
     *
     * @param paramMap
     */
    @PostMapping("feign/createkingdeeSoChange")
    String createkingdeeSoChange(@RequestBody Map<String, Object> paramMap);

    /**
     * 更新任务状态
     */
    @PostMapping("feign/updateSyncInfo")
    void updateSyncInfo(@RequestBody DmpSyncMqDTO.ParamDTO paramDTO);

    /**
     * 获取汇率
     *
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    @PostMapping("feign/getRate")
    BigDecimal getRate(@RequestParam(value = "date") String date, @RequestParam(value = "sourceCurrencyCode") String sourceCurrencyCode);


    /**
     * 根据Map条件查询金蝶数据
     *
     * @param conditon 查询条件
     *                 支持：id，is_deleted，source_type，source_code，source_id，status，mq_tag，return_msg
     *                 注：lastSql 用于表示扩展SQL
     * @return 返回Mq_data中的金蝶列表
     */
    @PostMapping("feign/getKingdeeSourceCode")
    List<String> getKingdeeSourceCode(@RequestBody Map<String, Object> conditon);


    /**
     * 根据id获取到第三方应用信息
     *
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     * @author yl
     * @date 2023-08-28 16:22
     */
    @PostMapping("feign/getCfgAppClient")
    CfgAppClientEntity getCfgAppClient(@RequestBody CfgAppClientDTO.FindDTO dto);

    /**
     * 新增第三方应用信息
     *
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     * @author yl
     * @date 2023-08-28 16:22
     */
    @PostMapping("feign/cfgAppClient/add")
    String addCfgAppClient(@RequestBody CfgAppClientDTO.AddDTO dto);

    /**
     * 修改第三方应用信息
     *
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     * @author yl
     * @date 2023-08-28 16:22
     */
    @PostMapping("feign/cfgAppClient/update")
    Boolean updateCfgAppClient(@RequestBody CfgAppClientDTO.UpdateDTO dto);

    /**
     * 创建平台任务
     *
     * @param dto
     * @return
     */
    @PostMapping("feign/dmp/createPlatformTask")
    Boolean createAndEnablePlatformTask(@RequestBody @Valid PlatformTaskDTO.AddDTO dto);

    /**
     * 删除平台任务
     *
     * @param dto
     * @return
     */
    @PostMapping("feign/dmp/removePlatformTask")
    Boolean removePlatformTask(@RequestBody @Valid PlatformTaskDTO.AddDTO dto);

    /**
     * 禁用启用
     *
     * @param disabledDTO
     * @return
     */
    @PostMapping("feign/dmp/disabledPlatformTask")
    Boolean disabledPlatformTask(@RequestBody @Valid PlatformTaskDTO.DisabledDTO disabledDTO);

    /**
     *
     */
    @PostMapping("feign/pull/sendWarnMsg")
    Boolean sendWarnMsg(@RequestBody String syncTaskId);

    /**
     * 记录拉取数据记录
     *
     * @param dmpPullTaskEntity 查询过滤条件
     * @return
     */
    @PostMapping("feign/saveOrUpdate/pull/task")
    String saveOrUpdateDmpPullTask(@RequestBody DmpPullTaskEntity dmpPullTaskEntity);

    /**
     * 记录推送数据记录
     *
     * @param dmpPushTaskEntity 查询过滤条件
     * @return
     */
    @PostMapping("feign/saveOrUpdate/push/task")
    String saveOrUpdateDmpPushTask(@RequestBody DmpPushTaskEntity dmpPushTaskEntity);

    /**
     * 创建第三方仓任务
     * @param dto
     * @return
     */
    @PostMapping("feign/dmp/createThirdWarehouseTask")
    Boolean createThirdWarehouseTask(@RequestBody @Valid ThirdWarehouseTaskDTO.AddDTO dto);


    /**
     * 创建第三方仓任务
     * @return
     */
    @PostMapping("feign/dmp/getPullTaskById")
    DmpPullTaskEntity getPullTaskById(@RequestBody String id);


    /**
     * 根据type查询所有配置
     */
    @PostMapping("feign/dmp/cfgSetting/list")
    Map<SettingEnum, String> getCfgSettingList(@RequestBody String type);

   /**
    * 查询sku成本数据
    * @author Will
    * @date: 2023/12/13 18:04
    * @param skuNoList
    * @return List<DmpSkuCostEntity>
    */
    @PostMapping("feign/listRedisBySkuNoList")
    List<DmpSkuCostEntity> listRedisBySkuNoList(@RequestBody @Valid List<String> skuNoList);

    /**
     * 根据type查询所有配置
     */
    @PostMapping("feign/pull/needPushMQ")
    Boolean needPushMQ(@RequestBody LocalDateTime lastTime);

    /**
     * 根据key值查询权限
     * @author Will
     * @date: 2023/12/19 12:02
     * @param feignDTO
     * @return CfgApiAuthEntity
     */
    @PostMapping("feign/cfgApiAuth/getByKey")
    CfgApiAuthEntity getByKey(@RequestBody @Valid CfgApiAuthDTO.FeignDTO feignDTO);

    /**
     * 更新任务禁用/启用和取消/开启报告计划
     *
     */
    @PostMapping("feign/dmp/allAddOrUpdateTaskAndSchedule")
    void allAddOrUpdateTaskAndSchedule(@RequestBody @Valid PlatformTaskDTO.DisabledDTO disabledDTO);

    /**
     * 获取飞书预警信息需要推送的(PushTask任务记录)
     * @return
     */
    @PostMapping("feign/dmp/getWarnPushTaskList")
    List<DmpPushTaskEntity> getWarnPushTaskList(@RequestBody List<String> statusList);

    /**
     * 获取飞书预警信息需要推送的(PullTask任务记录)
     * @return
     */

    @PostMapping("feign/dmp/getWarnPullTaskList")
    List<DmpPullTaskEntity> getWarnPullTaskList(@RequestBody List<String> statusList);
    /**
     * 获取飞书预警信息需要推送的(Task汇总报告)
     * @return
     */
    @PostMapping("feign/dmp/getWarnTaskReport")
    List<DmpTaskMsgDTO> getWarnTaskReport(@RequestBody List<String> statusList);
}