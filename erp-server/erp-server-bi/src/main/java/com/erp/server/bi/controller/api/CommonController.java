package com.erp.server.bi.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.EnumCacheUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 公共接口
 *
 * @Classname plm

 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@LogSystemModule("BI通用")
@RequestMapping("common")
public class CommonController extends BaseController {





    /**
     * 上传图片
     *
     * @param multipartFile 图片流
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 17:35
     **/
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传图片:文件名={name}")
    @PostMapping("/upload")
    public ApiResult<List<String>> upload(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        List<String> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = FastDFSClientUtil.uploadFile(file);
            list.add(filePath);
        }
        return this.success(list);
    }


    /**
     * 获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param type
     * @return
     */
    @GetMapping("enumDropDown")
    public ApiResult<List<Map<String,Object>>> enumSelect(@RequestParam(value = "type")String type) {
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        return success(enumMaps.get(type));
    }
}
