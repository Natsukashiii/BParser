package com.parser.githubmining.config.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Aspect
@Component
public class ValidateInputAspect {
    private static final Logger logger = LoggerFactory.getLogger(ValidateInputAspect.class);

    @Before("@annotation(com.parser.githubmining.config.aspects.ValidateInput)")
    public void validateInput(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null || (arg instanceof String && !StringUtils.hasText((String) arg))) {
                logger.error("Invalid input: " + arg);
                throw new IllegalArgumentException("Invalid input: " + arg);
            }
        }
    }
}
