package com.erp.rpc.dmp.feign;


import cn.hutool.json.JSONObject;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.math.BigDecimal;
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
    DmpShopInfoDTO getShopById(@RequestBody String shopId);


    /**
     *  获取所有的店铺信息
     */
    @PostMapping("feign/listShop")
    List<DmpShopInfoEntity> listShop();

    /**
     * 根据条件获详情
     * @author yl
     * @date 2023-06-07 10:42
     * @param dto
     * @return cn.hutool.json.JSONObject
     */
    @PostMapping("feign/getByKingdeeId")
    JSONObject getByKingdeeId(@RequestBody KingdeeDTO dto );

    /**
     * 生成金蝶销售变更单
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
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    @PostMapping("feign/getRate")
    BigDecimal getRate(@RequestParam(value = "date") String date, @RequestParam(value = "sourceCurrencyCode") String sourceCurrencyCode);


    /**
     * 根据Map条件查询金蝶数据
     * @param conditon 查询条件
     *                 支持：id，is_deleted，source_type，source_code，source_id，status，mq_tag，return_msg
     *                 注：lastSql 用于表示扩展SQL
     * @return 返回Mq_data中的金蝶列表
     */
    @PostMapping("feign/getKingdeeSourceCode")
    List<String> getKingdeeSourceCode(@RequestBody Map<String,Object> conditon);

    /**
     * 发送MQ消息并保存任务
     * @param dto
     * @return
     */
    @PostMapping("feign/send/mq/save/task")
    Boolean sendMqAndSaveTask(@RequestBody @Valid DmpPullTaskFeignDTO dto);

    /**
     * 发送MQ消息并保存任务
     * @param dto
     * @return
     */
    @PostMapping("feign/save/pull/task")
    String savePullTask(@RequestBody @Valid DmpPullTaskFeignDTO dto);

    /**
     * 根据订单id删除订单
     * @param ids
     * @return
     */
    @PostMapping("feign/remove/orderByIds")
    Boolean removeDmpOrderByIds(@RequestBody @Valid List<String> ids);
}