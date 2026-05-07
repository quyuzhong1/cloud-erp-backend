package com.erp.server.wms.controller.feign;

import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
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
     * 批量查询发货通知单
     *
     * @param ids 发货通知单id集合
     * @return 发货通知单集合
     * @throws RuntimeException 查询异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    @PostMapping("/listByIds")
    public List<SoDeliveryNoticeEntity> listByIds(@RequestBody List<String> ids) {
        return soDeliveryNoticeService.listByIds(ids);
    }

    /**
     * 按主表批量查询发货通知明细
     *
     * @param mainIds 发货通知单id集合
     * @return 发货通知明细集合
     * @throws RuntimeException 查询异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    @PostMapping("/listDetailByMainIds")
    public List<SoDeliveryNoticeDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds) {
        return soDeliveryNoticeDetailService.listDetailByMainIds(mainIds);
    }

    /**
     * 查询用于报关中间表生成的装箱明细
     *
     * @param ids 发货通知单id集合
     * @return 装箱明细集合
     * @throws RuntimeException 查询异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    @PostMapping("/listDeclarePackingDetail")
    public List<WmsCartonDetailDTO.ListPackingDetailDTO> listDeclarePackingDetail(@RequestBody List<String> ids) {
        return soDeliveryNoticeService.listDeclarePackingDetail(ids);
    }

    /**
     * 下推B2B报关单合并前明细
     **/
    @PostMapping("/listBeforePushB2bDeclare")
    public List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforePushB2bDeclare(@RequestBody TmsDeclareBillDTO.PushDeclareBeforeParamDTO dto) {
        return soDeliveryNoticeService.listBeforePushB2bDeclare(dto);
    }

    /**
     * 更新销售信息
     * @param soInfoEntity
     */
    @PostMapping("/updateSalesInfo")
    public void updateSalesInfo(@RequestBody SoInfoEntity soInfoEntity){
        soDeliveryNoticeService.updateSalesInfo(soInfoEntity);
    }

    /**
     * 通过明细idList获取通知记录详情
     *
     * @param idList
     * @return
     */
    @PostMapping("/getNoticeDetailByIdList")
    public List<SoDeliveryNoticeDetailEntity> getNoticeDetailByIdList(@RequestParam(value = "id") List<String> idList) {
        return soDeliveryNoticeDetailService.listByIds(idList);
    }


    /**
     * 根据id查询装箱明细信息
     * @author will
     * @date 2026/4/21 15:44
     * @param querySourceDTO
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SoOutDTO>
     */
    @PostMapping("/listPackingDetailByIdList")
    public List<TmsDeclareBillDTO.SoOutDTO> listPackingDetailByIdList(@RequestBody TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        return soDeliveryNoticeDetailService.listPackingDetailByIdList(querySourceDTO);
    }

    /**
     * 更新报关状态
     * @author will
     * @date 2026/4/30 11:47
     * @param dto
     * @return java.lang.Boolean
     */
    @PostMapping("/updateDeclareStatus")
    public Boolean updateDeclareStatus(@RequestBody SoDeliveryNoticeDTO.DeclareStatusDTO dto) {
        return soDeliveryNoticeService.updateDeclareStatus(dto);
    }
}
