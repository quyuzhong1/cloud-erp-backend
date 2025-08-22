package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "erp-wms", contextId = "soDeliveryNotice" ,configuration = {FeignErrorDecoder.class})
public interface SoDeliveryNoticeFeign {

    @PostMapping("feign/soDeliveryNotice/listDetailBySourceDetailId")
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailId);

    /**
     * 根据来源id 集合获取到审核通过的数据
     * @author yl
     * @date 2023-06-26 10:27
     * @param sourceIdList
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO.ListDTO>
     */
    @PostMapping("feign/soDeliveryNotice/listBySourceIdList")
    List<SoDeliveryNoticeDetailDTO.ListDTO> listBySourceIdList(@RequestBody List<String> sourceIdList);



    /**
     * 根据来源id list 查询已下推的发货通知单数量
     * @param soIds
     * @return Map<String,Long>
     * @author zhangchunlin
     * @date 2023-07-26 17:30
     */
    @PostMapping("feign/soDeliveryNotice/getPushDownDeliveryNoticeCnt")
    Map<String,Long> getPushDownDeliveryNoticeCnt(@RequestBody List<String> soIds);

    /**
     * 通过源id获取通知记录
     *
     * @param id
     * @return
     */
    @PostMapping("feign/soDeliveryNotice/getDeliveryNoticeById")
    SoDeliveryNoticeEntity getDeliveryNoticeBySourceId(@RequestParam(value = "id") String id);
    /**
     * 通过源id获取通知记录
     *
     * @param id
     * @return
     */
    @PostMapping("feign/soDeliveryNotice/getNoticeDetailById")
    SoDeliveryNoticeDetailEntity getNoticeDetailById(@RequestParam(value = "id") String id);
}
