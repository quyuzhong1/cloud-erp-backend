package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.ShopInfoMappingService;
import com.sdk.third.lingxing.dto.FbaShipmentReceiveDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 * 领星FBA签收记录
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxFbaShipmentReceivedApiInitHandler extends DmpInputInitHandler {

    @Resource
    private ShopInfoMappingService shopInfoMappingService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        if (StringUtils.isBlank(shopId)){
            ServiceException.runError("拉取领星货件签收明细异常:shopId为空");
        }
        LocalDate requestTime = dmpCfgInputDetailEntity.getLastTime().toLocalDate();
        // 查询映射关系
        ShopInfoMappingEntity mappingEntity = shopInfoMappingService.getByShopIdAndType(shopId, PlatformEnum.LINGXING.getName());
        if (null == mappingEntity){
            throw new ServiceException("数据异常:找不到领星映射关系, 店铺id=" + shopId);
        }
        String sid = mappingEntity.getThirdPlatformShopId();
        List<FbaShipmentReceiveDTO> dtoList = LingxingApiUtils.getAllReceivedInventory(Integer.parseInt(sid), requestTime);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("拉取领星货件签收明细数据睡眠异常:e={}", ExceptionUtil.stacktraceToString(e));
        }
        if (CollectionUtil.isEmpty(dtoList)) {
            log.info("拉取领星货件签收明细数据列表数据为空,sid={}, date={}", sid, requestTime);
            return Collections.emptyList();
        }
        dtoList.forEach(e->{
            e.setShopId(shopId);
        });
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(dtoList)));
    }

}
