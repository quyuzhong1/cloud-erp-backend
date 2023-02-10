package com.erp.server.dmp.controller;

import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.RocketMqTopic;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapValueDTO;
import com.erp.model.dmp.vo.CfgApiFieldMapVO;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.mq.MQProducerService;
import lombok.Data;
import org.apache.ibatis.annotations.Param;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API字段映射
 * @author Will
 * @version 1.0
 * @date 2023/1/11 11:47
 */
@RestController
@RequestMapping("dmp/cfgApiFieldMap")
public class CfgApiFieldMapController extends BaseController {

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private KingdeeProductDetailService kingdeeProductDetailService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/1/11 12:13
     * @param dto
     * @return ApiResult<PagingVO<CfgApiFieldMapVO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgApiFieldMapVO>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<CfgApiFieldMapVO> pagingVO = cfgApiFieldMapService.paging(dto);
        return success(pagingVO);
    }


   /**
    * 新增
    * @author Will
    * @date: 2023/1/11 12:13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated CfgApiFieldMapDTO dto) {
        Boolean flag = this.cfgApiFieldMapService.insert(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 批量新增
     * @author Will
     * @date: 2023/1/11 12:13
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchAdd")
    public ApiResult batchAdd(@RequestBody @Validated List<CfgApiFieldMapDTO> list) {
        Boolean flag = this.cfgApiFieldMapService.batchAdd(list);
        return flag == true ? success() : failure();
    }


    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 12:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated CfgApiFieldMapDTO dto) {
        this.cfgApiFieldMapService.update(dto);
        return success();
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/1/11 12:19
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    public ApiResult batchDelete(@RequestBody @Validated List<String> ids){
        this.cfgApiFieldMapService.batchDelete(ids);
        return success();
    }

    /**
     * 查询单条数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param id
     * @return ApiResult
     */
    @GetMapping("/getById")
    public ApiResult getById(@Param("id") String id){
        CfgApiFieldMapDTO dto = this.cfgApiFieldMapService.getCfgApiFieldMapById(id);
        return success(dto);
    }

    /**
     * 查询明细数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param fieldMapId
     * @return ApiResult
     */
    @GetMapping("/listDetails")
    public ApiResult listDetails(@Param("fieldMapId") String fieldMapId){
       List<CfgApiFieldMapValueDTO> list = this.cfgApiFieldMapService.listDetails(fieldMapId);
        return success(list);
    }

    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 12:13
     * @return ApiResult
     */
    @GetMapping("/pushProductDetail")
    public ApiResult pushProductDetail() {
        Map<String,Object> map = new HashMap<>();
        //id
        map.put("id","444");
        //sku
        map.put("skuNo","SKU1333333");
        //sku
        map.put("name","永诺YN300Air双色3200K-5500K可调色温超簿LED摄影灯");
        //spu
        map.put("spuNo","");
        //产品功能描述
        map.put("functionDesc","");
        //属性
        map.put("property","7777");
        //单位
        map.put("unitName","Pcs");
        //一级分类名称
        map.put("oneLevelCategory","手机");
        //一级分类编码
        map.put("oneLevelCategoryCode","M");
        //二级分类名称
        map.put("secondLevelCategory","耳机");
        //二级分类编码
        map.put("secondLevelCategoryCode","AE");
        //产品经理
        map.put("chargeName","王杰");
        //销售信息
        //上市时间
        map.put("listingTime","23");
        //物流信息
        //报关中文名
        map.put("declareChineseName","补光灯");
        //报关英文名
        map.put("declareEnglishName","light");
        //报关申报价
        map.put("declarePrice","7");
        //产品属性（是否带电）
        map.put("productProperty_electric",false);
        //产品属性（是否带磁）
        map.put("productProperty_magnetism",false);
        //海关编码
        map.put("customsCode","");
        //申报要素
        map.put("declareElement","");
        //毛重
        map.put("grossWeight", 517);
        //净重
        map.put("netWeight", 509);
        //产品尺寸
        //产品尺寸-长(cm)
        map.put("productSize_length",  13);
        //产品尺寸-宽(cm)
        map.put("productSize_width",  12);
        //产品尺寸-高(cm)
        map.put("productSize_height",  12);
        //单箱数量
        map.put("boxQty", 12);
        //单箱重量
        map.put("boxWeight", 12);
        //单箱尺寸
        //产品尺寸-长(cm)
        map.put("boxSize_length", 12);
        //产品尺寸-宽(cm)
        map.put("boxSize_width", 12);
        //产品尺寸-高(cm)
        map.put("boxSize_height", 12);
        //实际不含税成本
        map.put("actualNoTaxCost","");
        //实际含税成本
        map.put("actualTaxCost", "");
        map.put("moq","22");
        //采购员
        map.put("purchaseUser","王杰");
        map.put("mainSupplier","王杰");
        this.kingdeeProductDetailService.pushProductDetail(map);
        return success();
    }

    @Resource
    private MQProducerService producerService;
    @PostMapping("/push/mq")
    public void pushToRocket(@RequestBody JSONObject body){
        producerService.syncSendMsg("", RocketMqTopic.DMP_TOPIC, body.getString("tag"), body,"dmp test");
    }
    @PostMapping("/push/mq/batch")
    public SendResult pushToRocketBatch(@RequestBody TestMq body){
        return producerService.syncClassMsg(RocketMqTopic.DMP_TOPIC, body.getTag(), body, body.getKey());
    }

    @Data
    public static class TestMq{
        private String key;

//        @JsonSerialize(as = LocalDateTimeSerializer.class)
//        @JsonDeserialize(using = LocalDateTimeDeserializer.class, as = LocalDateTime.class)
//        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime time;
        private List<String> codeList;

        private String tag;
    }

}
