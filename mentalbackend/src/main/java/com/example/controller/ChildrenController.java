package com.example.controller;

import com.example.entity.Children;
import com.example.service.ChildrenService;
import com.example.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 孩子信息控制器
 */
@RestController
@RequestMapping("/api/children")
public class ChildrenController {

    private static final Logger log = LoggerFactory.getLogger(ChildrenController.class);

    @Autowired
    private ChildrenService childrenService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 新增孩子信息
     */
    @PostMapping
    public Result addChild(@RequestBody Children children) {
        try {
            log.info("接收到新增孩子信息请求：{}", objectMapper.writeValueAsString(children));
            boolean result = childrenService.addChild(children);
            if (result) {
                return Result.success("新增孩子信息成功", null);
            } else {
                return Result.error(400, "新增孩子信息失败");
            }
        } catch (Exception e) {
            log.error("新增孩子信息异常：", e);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 修改孩子信息
     */
    @PutMapping
    public Result updateChild(@RequestBody Children children) {
        try {
            log.info("接收到修改孩子信息请求：{}", objectMapper.writeValueAsString(children));
            boolean result = childrenService.updateChild(children);
            if (result) {
                return Result.success("修改孩子信息成功", null);
            } else {
                return Result.error(400, "修改孩子信息失败");
            }
        } catch (Exception e) {
            log.error("修改孩子信息异常：", e);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 设置当前操作孩子
     */
    @PutMapping("/current")
    public Result setCurrentChild(@RequestParam Long userId, @RequestParam Long childId) {
        try {
            log.info("接收到设置当前操作孩子请求：用户ID={}，孩子ID={}", userId, childId);
            boolean result = childrenService.setCurrentChild(userId, childId);
            if (result) {
                return Result.success("设置当前操作孩子成功", null);
            } else {
                return Result.error(400, "设置当前操作孩子失败");
            }
        } catch (Exception e) {
            log.error("设置当前操作孩子异常：", e);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 获取当前操作孩子
     */
    @GetMapping("/current")
    public Result getCurrentChild(@RequestParam Long userId) {
        try {
            log.info("接收到获取当前操作孩子请求：用户ID={}", userId);
            Children currentChild = childrenService.getCurrentChild(userId);
            return Result.success("获取当前操作孩子成功", currentChild);
        } catch (Exception e) {
            log.error("获取当前操作孩子异常：", e);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取所有孩子信息
     */
    @GetMapping("/list")
    public Result getChildrenByUserId(@RequestParam Long userId) {
        try {
            log.info("接收到获取用户所有孩子请求：用户ID={}", userId);
            List<Children> childrenList = childrenService.getChildrenByUserId(userId);
            return Result.success("获取用户所有孩子成功", childrenList);
        } catch (Exception e) {
            log.error("获取用户所有孩子异常：", e);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }
}