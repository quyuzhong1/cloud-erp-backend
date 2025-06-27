package com.erp.server.sys.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.EnumCacheUtils;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname CommonController

 * @Date 2023-03-23 18:54
 * @Created by yl
 */
@RestController
@LogSystemModule("SYS通用")
@RequestMapping("common")
public class CommonController extends BaseController {

    @Resource
    private FileFeign fileFeign;

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
    public ApiResult upload(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        List<String> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = fileFeign.uploadFile(file);
            list.add(filePath);
        }
        return this.success(list);
    }

    /**
     * 删除上传utl
     *
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 17:35
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除上传:路径={url}")
    @PostMapping("/deleteUrl")
    public ApiResult deleteUrl(@RequestParam("url") String url) {
        if (StringUtils.isNotBlank(url)) {
            int num = fileFeign.deleteFile(url);
            if (num == 0) {
                return success();
            }
        }
        return failure();

    }

    /**
     * 批量获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param types
     * @return
     */
    @GetMapping("enumDropDownBatch")
    public ApiResult<Map<String,List<Map<String,Object>>>> enumSelect(@RequestParam(value = "types")List<String> types) {
        Map<String,List<Map<String,Object>>> typeMaps = Maps.newHashMap();
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        if(CollectionUtil.isNotEmpty(types)) {
            types.stream().forEach(r-> typeMaps.put(r,enumMaps.get(r)));
        }
        return success(typeMaps);
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

    /**
     * 批量上传附件
     * @param multipartFile 文件数组
     * @return
     * @date: 2024-09-05
     * @author: tanmujin
     */
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传图片:文件名={name}")
    @PostMapping("/uploadBatch")
    public ApiResult<List<SysCommonDTO.AttachmentDTO>> uploadBatch(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        List<SysCommonDTO.AttachmentDTO> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = fileFeign.uploadFile(file);
            String fileName = file.getOriginalFilename();
            list.add(new SysCommonDTO.AttachmentDTO(fileName, filePath));
        }
        return this.success(list);
    }

}
