package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualInventoryTransactionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualInventoryTransactionDTO;
import javax.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;

/**
 * 虚拟仓库存事务表
 *
 * @author shukai
 * @since 2025-12-18
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓库存事务表")
@RequestMapping("/virtualInventoryTransaction")
public class VirtualInventoryTransactionController extends BaseController {

    @Resource
    private VirtualInventoryTransactionService virtualInventoryTransactionService;

    /**
     * 新增
     * @author shukai
     * @date:  2025-10-13
     * @param dto
     * @return ApiResult<String>
     */
     @PostMapping("/overrideDbInventory")
     @LogAction(value = LogActionEnum.INSERT, desc = "db库存重算")
     public ApiResult<?> overrideDbInventory(@RequestBody BaseIdsDTO.DateDTO dto) {
    	 LocalDate date = dto.getBillDate();
    	 if(date == null) {
    		date = LocalDate.parse("2000-01-01");
    	}
        return success(virtualInventoryTransactionService.overrideDbInventory(date, dto.getIds()));
     }
     
     /**
      * 新增
      * @author shukai
      * @date:  2025-10-13
      * @param dto
      * @return ApiResult<String>
      */
     @PostMapping("/overrideRedisInventory")
     @LogAction(value = LogActionEnum.INSERT, desc = "redis库存重算")
     public ApiResult<?> overrideRedisInventory(@RequestBody BaseIdsDTO.StatusDTO dto) {
    	 boolean isCheck = false;
    	 String status = dto.getStatus();
    	 if("check".equals(status)) {
    		 isCheck = true;
    	 }
    	 return success(virtualInventoryTransactionService.overrideRedisInventory(dto.getIds() , isCheck));
     }


}
