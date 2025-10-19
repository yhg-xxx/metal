package com.example.mapper;

import com.example.entity.Counselors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.annotation.Resource;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试CounselorsMapper是否正常工作
 */
@SpringBootTest
public class CounselorsMapperTest {

    @Resource
    private CounselorsMapper counselorsMapper;

    @Test
    public void testSelectById() {
        // 测试BaseMapper的selectById方法是否能正常工作
        // 注意：这里使用id=1进行测试，如果数据库中没有该id的数据，可能会返回null
        // 实际测试时应使用数据库中存在的id
        Counselors counselor = counselorsMapper.selectById(1L);
        
        // 如果数据库中没有数据，这里可能会失败，这是正常的
        // 关键是要确认没有抛出"Invalid bound statement"异常
        System.out.println("查询结果: " + counselor);
    }
}