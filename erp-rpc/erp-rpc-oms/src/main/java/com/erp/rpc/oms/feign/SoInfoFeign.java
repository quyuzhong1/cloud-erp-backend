package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.SoInfoToSdyDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soInfoFeign",configuration = {FeignErrorDecoder.class})
public interface SoInfoFeign {

    @PostMapping("feign/soInfo/getSoInfoById")
    SoInfoEntity getSoInfoById(@RequestBody String id);

    @PostMapping("feign/soInfo/listSoInfoByIds")
    List<SoInfoEntity> listSoInfoByIds(@RequestBody List<String> ids);

    @PostMapping("feign/soInfo/listSoDetailByIds")
    List<SoDetailEntity> listSoDetailByIds(@RequestBody List<String> ids);

    /**
     * 获取订单明细全量字段
     *
     * @param id
     * @return
     */
    @PostMapping("feign/soInfo/listSoDetailByMainId")
    List<SoDetailEntity> listSoDetailByMainId(@RequestBody String id);

    @PostMapping("feign/soInfo/listSoDetailByMainIds")
    List<SoDetailEntity> listSoDetailByMainIds(@RequestBody List<String> ids);

    @PostMapping("feign/soInfo/getSoBaseById")
    SoInfoDTO.CustomerDTO getSoBaseById(@RequestBody String id);

    @PostMapping("feign/soInfo/listSoCustomerByIds")
    List<SoInfoDTO.CustomerDTO> listSoCustomer(@RequestBody List<String> soIdList);

    /**
     * 更改销售订单发货状态
     *
     * @param paramList
     */
    @PostMapping("feign/soInfo/updateDeliveryStatus")
    void updateDeliveryStatus(@RequestBody List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList);

    /**
     * 销售订单审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     **/
    @PostMapping("feign/soInfo/approve")
    List<BatchResultDTO> approve(@RequestBody BaseApproveParamDTO dto);

    @PostMapping("feign/soInfo/listRepairHistoryDb")
    List<SoInfoDTO.ListDTO> listRepairHistoryDb();
    /**
     * 更新冻结数量
     * @author will
     * @date 2024/7/16 20:07
     * @param soParamList
     */
    @PostMapping("feign/soInfo/updateFrozenQty")
    void updateFrozenQty(@RequestBody List<SoDetailDTO.UpdateFrozenQtyDTO> soParamList);
    /**
     * 查询所有虚拟仓B2B销售订单数据
     * @author will
     * @date 2024/9/26 14:47
     * @return List<ViewDTO>
     */
    @GetMapping("feign/soInfo/listAllVirtualSoDetail")
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoDetail();

    /**
     * 同步速递云B2B订单
     * @param soInfoToSdyDTO
     */
    @PostMapping("feign/soInfo/sdyFieldOrderHandler")
    void sdyFieldOrderHandler(@RequestBody SoInfoToSdyDTO soInfoToSdyDTO);

    /**
     * 根据code批量查询
     * @param list
     */
    @PostMapping("feign/soInfo/listByCodes")
    List<SoInfoEntity> listByCodes(List<String> list);

    /**
     * 修改单据审批状态
     * @param updateApprovalStatusDTO
     */
    @PostMapping("feign/soInfo/updateApproveStatus")
    void updateApproveStatus(SoInfoDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
