package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     *  指定切入点
     */

    @Pointcut("execution(* com.sky.mapper.*.* (..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    /**
     * 前置通知
     * @param joinPoint
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开启公共字段自动填充");
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取方法上的 @AutoFill 注解
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //防止空指针异常
        if(operationType==null){
            return;
        }

        //通过反射自动赋值
        try {
            //获取当前时间和用户id
            LocalDateTime currentTime = LocalDateTime.now();
            Long currentId = BaseContext.getCurrentId();
            Object clazz = joinPoint.getArgs()[0];

            //当sql操作为insert时
            if(operationType == OperationType.INSERT){

                Method setCreateTime= clazz.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setUpdateTime= clazz.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setCreateUser= clazz.getClass().getMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateUser= clazz.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                //坑点：invoke要class创建实例
                setCreateTime.invoke(clazz,currentTime);
                setUpdateTime.invoke(clazz,currentTime);
                setCreateUser.invoke(clazz,currentId);
                setUpdateUser.invoke(clazz,currentId);
            }else{//当sql操作为update时
                Method setUpdateTime= clazz.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME);
                Method setUpdateUser= clazz.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER);


                setUpdateTime.invoke(clazz,currentTime);
                setUpdateUser.invoke(clazz,currentId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
