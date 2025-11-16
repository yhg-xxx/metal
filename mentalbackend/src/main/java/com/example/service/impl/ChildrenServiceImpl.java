package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Children;
import com.example.mapper.ChildrenMapper;
import com.example.service.ChildrenService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 孩子信息服务实现类
 */
@Service
public class ChildrenServiceImpl extends ServiceImpl<ChildrenMapper, Children> implements ChildrenService {

    private static final Logger log = LoggerFactory.getLogger(ChildrenServiceImpl.class);

    @Resource
    private ChildrenMapper childrenMapper;

    @Override
    @Transactional
    public boolean addChild(Children children) {
        try {
            // 检查必填字段
            if (children.getUserId() == null || children.getName() == null || children.getGender() == null) {
                log.error("新增孩子信息失败：缺少必填字段");
                return false;
            }
            
            // 转换性别：中文转数据库枚举值
            convertGender(children);
            
            // 设置是否为当前操作孩子，默认设置为true
            children.setIsCurrentOperation(true);
            
            // 如果是用户的第一个孩子，直接保存
            QueryWrapper<Children> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", children.getUserId());
            long count = this.count(queryWrapper);
            
            if (count == 0) {
                // 直接保存第一个孩子
                boolean result = this.save(children);
                if (result) {
                    log.info("新增孩子信息成功：用户ID={}，孩子姓名={}", children.getUserId(), children.getName());
                }
                return result;
            } else {
                // 如果不是第一个孩子，先将其他孩子的当前操作标识设为false
                Children updateChild = new Children();
                updateChild.setIsCurrentOperation(false);
                this.update(updateChild, queryWrapper);
                
                // 保存新孩子并设为当前操作孩子
                boolean result = this.save(children);
                if (result) {
                    log.info("新增孩子信息成功：用户ID={}，孩子姓名={}", children.getUserId(), children.getName());
                }
                return result;
            }
        } catch (Exception e) {
            log.error("新增孩子信息失败：用户ID={}，错误信息={}", children.getUserId(), e.getMessage());
            throw new RuntimeException("新增孩子信息失败", e);
        }
    }

    @Override
    @Transactional
    public boolean updateChild(Children children) {
        try {
            // 检查必填字段
            if (children.getId() == null) {
                log.error("修改孩子信息失败：缺少ID");
                return false;
            }
            
            // 查询孩子是否存在且属于当前用户
            QueryWrapper<Children> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", children.getId());
            Children existingChild = this.getOne(queryWrapper);
            
            if (existingChild == null) {
                log.error("修改孩子信息失败：孩子不存在，ID={}", children.getId());
                return false;
            }
            
            // 转换性别：中文转数据库枚举值
            convertGender(children);
            
            // 执行更新操作
            boolean result = this.updateById(children);
            if (result) {
                log.info("修改孩子信息成功：孩子ID={}，孩子姓名={}", children.getId(), children.getName());
            }
            return result;
        } catch (Exception e) {
            log.error("修改孩子信息失败：孩子ID={}，错误信息={}", children.getId(), e.getMessage());
            throw new RuntimeException("修改孩子信息失败", e);
        }
    }

    @Override
    @Transactional
    public boolean setCurrentChild(Long userId, Long childId) {
        try {
            // 检查参数
            if (userId == null || childId == null) {
                log.error("设置当前操作孩子失败：缺少用户ID或孩子ID");
                return false;
            }
            
            // 查询孩子是否存在且属于当前用户
            QueryWrapper<Children> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", childId).eq("user_id", userId);
            Children child = this.getOne(queryWrapper);
            
            if (child == null) {
                log.error("设置当前操作孩子失败：孩子不存在或不属于该用户，用户ID={}，孩子ID={}", userId, childId);
                return false;
            }
            
            // 先将用户的所有孩子的当前操作标识设为false
            Children updateChild = new Children();
            updateChild.setIsCurrentOperation(false);
            QueryWrapper<Children> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("user_id", userId);
            this.update(updateChild, updateWrapper);
            
            // 将指定的孩子设为当前操作孩子
            child.setIsCurrentOperation(true);
            boolean result = this.updateById(child);
            if (result) {
                log.info("设置当前操作孩子成功：用户ID={}，孩子ID={}", userId, childId);
            }
            return result;
        } catch (Exception e) {
            log.error("设置当前操作孩子失败：用户ID={}，孩子ID={}，错误信息={}", userId, childId, e.getMessage());
            throw new RuntimeException("设置当前操作孩子失败", e);
        }
    }
    
    @Override
    public Children getCurrentChild(Long userId) {
        try {
            // 检查参数
            if (userId == null) {
                log.error("获取当前操作孩子失败：缺少用户ID");
                return null;
            }
            
            // 查询用户的当前操作孩子
            QueryWrapper<Children> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("is_current_operation", true);
            Children currentChild = this.getOne(queryWrapper);
            
            if (currentChild != null) {
                log.info("获取当前操作孩子成功：用户ID={}，孩子ID={}，孩子姓名={}", 
                        userId, currentChild.getId(), currentChild.getName());
                
                // 将数据库枚举值转回中文显示
                if ("MALE".equals(currentChild.getGender())) {
                    currentChild.setGender("男");
                } else if ("FEMALE".equals(currentChild.getGender())) {
                    currentChild.setGender("女");
                }
            } else {
                log.warn("未找到用户的当前操作孩子：用户ID={}", userId);
            }
            
            return currentChild;
        } catch (Exception e) {
            log.error("获取当前操作孩子失败：用户ID={}，错误信息={}", userId, e.getMessage());
            throw new RuntimeException("获取当前操作孩子失败", e);
        }
    }
    
    @Override
    public List<Children> getChildrenByUserId(Long userId) {
        try {
            // 检查参数
            if (userId == null) {
                log.error("获取用户所有孩子失败：缺少用户ID");
                return null;
            }
            
            // 查询用户的所有孩子
            QueryWrapper<Children> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            List<Children> childrenList = this.list(queryWrapper);
            
            if (childrenList != null && !childrenList.isEmpty()) {
                log.info("获取用户所有孩子成功：用户ID={}，孩子数量={}", userId, childrenList.size());
                
                // 将数据库枚举值转回中文显示
                for (Children child : childrenList) {
                    if ("MALE".equals(child.getGender())) {
                        child.setGender("男");
                    } else if ("FEMALE".equals(child.getGender())) {
                        child.setGender("女");
                    }
                }
            } else {
                log.warn("未找到用户的孩子信息：用户ID={}", userId);
            }
            
            return childrenList;
        } catch (Exception e) {
            log.error("获取用户所有孩子失败：用户ID={}，错误信息={}", userId, e.getMessage());
            throw new RuntimeException("获取用户所有孩子失败", e);
        }
    }
    
    /**
     * 转换性别：中文转数据库枚举值
     * @param children 孩子对象
     */
    private void convertGender(Children children) {
        if (children.getGender() != null) {
            String gender = children.getGender();
            switch (gender) {
                case "男" -> children.setGender("MALE");
                case "女" -> children.setGender("FEMALE");
                case "未知" -> children.setGender("UNKNOWN");
            }
            // 其他情况保持不变，可能已经是数据库枚举值
        }
    }
}