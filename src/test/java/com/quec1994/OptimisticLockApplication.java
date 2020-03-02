package com.quec1994;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * <P>ClassName: SpringBootSkeletonApplication
 * <P>Description: spring boot 启动类
 *
 * @author quec1994
 * @version V1.0, quec1994, 2020/03/02
 **/
@SpringBootApplication
@ServletComponentScan
@Slf4j
public class OptimisticLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(OptimisticLockApplication.class, args);
        log.info("optimistic-lock 服务启动完成");
    }
}
