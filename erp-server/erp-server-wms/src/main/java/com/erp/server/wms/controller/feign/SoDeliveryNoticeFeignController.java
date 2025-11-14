package com.erp.server.wms.controller.feign;

import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("feign/soDeliveryNotice")
public class SoDeliveryNoticeFeignController {
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    /**
     * 根据来源明细id查询出库表
     *
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     **/
    @PostMapping("/listDetailBySourceDetailId")
    public List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailId) {
        return soDeliveryNoticeDetailService.listDetailBySourceDetailIds(sourceDetailId);
    }


    /**
     * 根据销售 销售订单ids 获取是否有下推的单据
     *
     * @param soDetailIds
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:27
     */
    @PostMapping("/getPushDownBySoDetailIds")
    public List<SoDeliveryNoticeDetailDTO.PushDownDTO> getPushDownBySoDetailIds(@RequestBody List<String> soDetailIds) {
        return soDeliveryNoticeDetailService.getPushDownBySoDetailIds(soDetailIds);
    }

    /**
     * 根据销售 销售订单ids 获取是否有下推的单据
     *
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:27
     */
    @PostMapping("/getPushDownBySourceIds")
    public Integer getPushDownBySourceIds(@RequestBody List<String> soIds) {
        return soDeliveryNoticeService.getPushDownBySourceIds(soIds);
    }


    /**
     * 关闭关联单据的关闭状态
     *
     * @param soDetailIds
     * @return void
     * @author yl
     * @date 2023-05-25 19:25
     */
    @PostMapping("/closeBySoDetailIds")
    public void closeBySoDetailIds(@RequestBody List<String> soDetailIds) {
        soDeliveryNoticeDetailService.closeBySoDetailIds(soDetailIds);
    }

    /**
     * 根据来源id list 查询审核信息
     *
     * @param sourceIdList
     * @return void
     * @author yl
     * @date 2023-06-26 10:00
     */
    @PostMapping("/listBySourceIdList")
    public List<SoDeliveryNoticeDetailDTO.ListDTO> listBySourceIdList(@RequestBody List<String> sourceIdList) {
        List<SoDeliveryNoticeDetailDTO.ListDTO> resultList = soDeliveryNoticeDetailService.listBySourceIdList(sourceIdList);
        return resultList;
    }

    /**
     * 根据来源id list 查询已下推的发货通知单数量
     *
     * @param soIds
     * @return Map<String, Long>
     * @author zhangchunlin
     * @date 2023-07-26 17:30
     */
    @PostMapping("/getPushDownDeliveryNoticeCnt")
    public Map<String, Long> getPushDownDeliveryNoticeCnt(@RequestBody List<String> soIds) {
        return soDeliveryNoticeService.getPushDownDeliveryNoticeCnt(soIds);
    }

    /**
     * 根据销售订单查询发货通知单
     * @param soIds
     * @return
     */
    @PostMapping("/listDeliveryNoticeBySoIds")
    public List<SoDeliveryNoticeEntity> listDeliveryNoticeBySoIds(@RequestBody List<String> soIds) {
        return soDeliveryNoticeService.listDeliveryNoticeBySoIds(soIds);
    }
    /**
     * 通过源id获取通知记录
     *
     * @param id
     * @return
     */
    @PostMapping("/getDeliveryNoticeById")
    public SoDeliveryNoticeEntity getDeliveryNoticeBySourceId(@RequestParam(value = "id") String id) {
        return soDeliveryNoticeService.getById(id);
    }
    /**
     * 通过明细id获取通知记录详情
     *
     * @param id
     * @return
     */
    @PostMapping("/getNoticeDetailById")
    public SoDeliveryNoticeDetailEntity getNoticeDetailById(@RequestParam(value = "id") String id) {
        return soDeliveryNoticeDetailService.getById(id);
    }
    /**
     * 更新销售信息
     * @param soInfoEntity
     */
    @PostMapping("/updateSalesInfo")
    public void updateSalesInfo(@RequestBody SoInfoEntity soInfoEntity){
        soDeliveryNoticeService.updateSalesInfo(soInfoEntity);
    }
}
