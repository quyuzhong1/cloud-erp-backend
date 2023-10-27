package com.erp.server.plm.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.BasicDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.service.SysTaskPhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品系统通用设置
 *
 * @Classname

 * @Date 2022-09-13 16:28
 * @Created by yl
 */

@RestController
@LogSystemModule("系统通用设置")
@RequestMapping("sys/taskPhase")
public class SysTaskPhaseController extends BaseController {

    @Autowired
    private SysTaskPhaseService sysTaskPhaseService;


    /**
     * 新建任务-批量保存或者修改阶段名
     *
     * @param list
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 10:27
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "批量保存或者修改阶段名:名称={name}")
    @PostMapping("/batchSaveOrUpdate")
    // @RequestPermissions("plm:sys:taskPhase:batchSaveOrUpdate")
    public ApiResult add(@RequestBody @Validated List<UpdateBasicNameDTO> list) {
        sysTaskPhaseService.batchSaveOrUpdate(list);
        return success();
    }

    /**
     * 系统阶段分页
     * @param
     * @return
     */
    @PostMapping("/paging")
    // @RequestPermissions("plm:sys:taskPhase:batchSaveOrUpdate")
    public ApiResult<PagingVO<SysTaskPhaseEntity>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SysTaskPhaseEntity> pagingVO= sysTaskPhaseService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新建任务-修改阶段名
     *
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 10:27
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改阶段名:id={id},名称={name}")
    @PostMapping("/update")
    //  @RequestPermissions("plm:sys:taskPhase:update")
    public ApiResult update(@RequestBody @Validated UpdateBasicNameDTO dto) {
        sysTaskPhaseService.updateTaskPhase(dto);
        return success();
    }

    /**
     * 新建任务-删除阶段名
     *
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 10:27
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除阶段名")
    @PostMapping("/remove")
    //  @RequestPermissions("plm:sys:taskPhase:remove")
    public ApiResult remove(@RequestParam(value = "id") String id) {
        boolean flag = sysTaskPhaseService.removeSysTaskPhase(id);
        return flag==true?success():failure();
    }


    /**
     * 新建任务-获取阶段名称列表
     *
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 10:27
     */
    @GetMapping("/list")
   // @RequestPermissions("plm:sys:taskPhase:list")
    public ApiResult<List<BasicDTO>> list() {
        return success(sysTaskPhaseService.getSysTaskPhaseList());
    }

}
