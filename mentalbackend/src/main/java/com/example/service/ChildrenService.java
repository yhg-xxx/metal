package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Children;

import java.util.List;

/**
 * 孩子信息服务接口
 */
public interface ChildrenService extends IService<Children> {
    /**
     * 新增孩子信息
     * @param children 孩子信息实体
     * @return 是否新增成功
     */
    boolean addChild(Children children);
    
    /**
     * 修改孩子信息
     * @param children 孩子信息实体
     * @return 是否修改成功
     */
    boolean updateChild(Children children);
    
    /**
     * 设置当前操作孩子
     * @param userId 用户ID
     * @param childId 孩子ID
     * @return 是否设置成功
     */
    boolean setCurrentChild(Long userId, Long childId);
    
    /**
     * 获取当前操作孩子信息
     * @param userId 用户ID
     * @return 当前操作的孩子信息
     */
    Children getCurrentChild(Long userId);
    
    /**
     * 根据用户ID获取所有孩子信息
     * @param userId 用户ID
     * @return 孩子信息列表
     */
    List<Children> getChildrenByUserId(Long userId);
}