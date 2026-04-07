package com.erp.server.dmp.controller.api;


import com.common.business.constant.RedisCacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import com.common.core.controller.BaseController;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpSoRefundInfoService;

import cn.hutool.core.collection.CollUtil;

import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.dto.DmpSoRefundInfoDTO;
import com.erp.model.dmp.dto.DmpSoReturnInfoDTO;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.erp.model.dmp.gyy.GyyReturnOrderEntity;

/**
 * 中台销售退款单主表
 *
 * @author shukai
 * @since 2024-07-12
 */
@Slf4j
@RestController
@LogSystemModule("中台销售退款单主表")
@RequestMapping("/dmpSoRefundInfo")
public class DmpSoRefundInfoController extends BaseController {

    @Resource
    private DmpSoRefundInfoService dmpSoRefundInfoService;
    
    @Resource
    private MongoService mongoService;
    
    @Resource
    private RedisUtil redisUtil;

    /**
    * 新增
    * @author shukai
    * @date:  2024-07-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台销售退款单主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSoRefundInfoDTO.AddDTO dto) {
        return success(dmpSoRefundInfoService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-07-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台销售退款单主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSoRefundInfo:update",
        serviceClass = DmpSoRefundInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSoRefundInfoDTO.UpdateDTO dto) {
        dmpSoRefundInfoService.update(dto);
        return success();
    }

    @PostMapping("/addGyyRefundOrder")
    public ApiResult<?> addGyyRefundOrder(@RequestBody DmpSoReturnInfoDTO.AddGyyReturnOrderDTO dto) {
    	Integer page = dto.getPage();
    	String redisKey = RedisCacheConstants.ADD_GYY_REFUND_ORDER_KEY;
    	if(page == null) {
        	Object object = redisUtil.get(redisKey);
        	if(object != null) {
        		page = Integer.valueOf(object.toString());
        	}
    	}
    	if(page == null) {
    		page = 1;
    	}
    	
    	while(true) {
            List<GyyRefundEntity> mongoData = mongoService.findMongoData(dto, page, 1000, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
            if (CollUtil.isEmpty(mongoData)) {
            	redisUtil.del(redisKey);
            	log.warn("处理管易历史退款数据完成");
                break;
            }
            try {
            	dmpSoRefundInfoService.addGyyRefundOrder(mongoData);
				log.warn("处理管易历史退款第{}页数据成功" , page);
			} catch (Exception e) {
				log.error("处理管易历史退款第{}页数据失败" , page , e);
				return failure(page);
			}
            page = page + 1;
            redisUtil.set(redisKey, page);
    	}
        return success();
    }

}
