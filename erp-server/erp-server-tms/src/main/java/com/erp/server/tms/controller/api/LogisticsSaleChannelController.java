package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.TmsCarrierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 销售平台物流渠道表
 *
 * @author zdy
 * @since 2023-11-03
 */
@Slf4j
@RestController
@LogSystemModule("销售平台物流渠道表")
@RequestMapping("/logisticsSaleChannel")
public class LogisticsSaleChannelController extends BaseController {

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;
    @Resource
    private TmsCarrierService tmsCarrierService;

    /**
    * 新增
    * @author zdy
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "销售平台物流渠道表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsSaleChannelDTO.AddDTO dto) {
        return success(logisticsSaleChannelService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销售平台物流渠道表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsSaleChannel:update",
        serviceClass = LogisticsSaleChannelService.class,
        keyIdName = "id")
    public ApiResult<Object>update(@RequestBody @Validated LogisticsSaleChannelDTO.UpdateDTO dto) {
        logisticsSaleChannelService.update(dto);
        return success();
    }

    /**
     * 根据平台类型获取渠道列表
     * @return
     */
    @PostMapping("/listByType")
    public ApiResult<List<SaleChannelDTO>> listByType(@RequestBody LogisticsSaleChannelDTO.QueryDTO dto) {
        return success(logisticsSaleChannelService.listByType(dto.getPlatformType(), dto.getServicePlatform()));
    }
    /**
     * 根根据平台类型获取承运商列表远程搜索
     * @return
     */
    @PostMapping("/saleChannel/pagingSelect")
    public ApiResult<PagingVO<SaleChannelDTO>> saleChannelPagingSelect(@RequestBody @Validated PagingDTO<LogisticsSaleChannelDTO.QueryDTO> dto){
        return success(logisticsSaleChannelService.pagingSelect(dto));
    }

    /**
     * 根据平台类型获取承运商列表
     */
    @GetMapping("/carrier")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> carrierList(@RequestParam(value = "salesPlatform", defaultValue = "AliExpress") String salesPlatform) {
        return success(tmsCarrierService.listBySalesPlatform(salesPlatform));
    }

    /**
     * 根根据平台类型获取承运商列表远程搜索
     * @return
     */
    @PostMapping("/carrier/pagingSelect")
    public ApiResult<PagingVO<BaseDropDownDTO.CommonDTO>> carrierPagingSelect(@RequestBody @Validated PagingDTO<LogisticsSaleChannelDTO.SelectDTO> dto){
        return success(tmsCarrierService.pagingSelect(dto));
    }

}
