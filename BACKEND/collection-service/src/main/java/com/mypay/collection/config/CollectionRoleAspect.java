package com.mypay.collection.config;

import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.CollectionMember;
import com.mypay.collection.error.CollectionErrorCode;
import com.mypay.collection.repository.CollectionMemberRepository;
import com.mypay.collection.repository.CollectionRepository;
import com.mypay.common.constant.CollectionRole;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.common.exception.ForbiddenException;
import com.mypay.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class CollectionRoleAspect {

    private final CollectionMemberRepository memberRepository;
    private final CollectionRepository collectionRepository;

    @Around("@annotation(requireCollectionRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireCollectionRole requireCollectionRole) throws Throwable {
        String userId = RequestContextHolder.currentUserId();

        String collectionId = extractCollectionId(joinPoint);
        if (collectionId == null) {
            return joinPoint.proceed();
        }

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CollectionErrorCode.COLLECTION_NOT_FOUND,
                        "Collection not found: " + collectionId));

        CollectionMember member = memberRepository.findByCollectionAndCollectionMemberUserId(collection, userId)
                .orElseThrow(() -> new ForbiddenException(CollectionErrorCode.COLLECTION_ACCESS_DENIED));

        CollectionRole[] required = requireCollectionRole.value();
        CollectionRole memberRole = member.getCollectionMemberRole();
        // ADMIN passes any requirement; EDITOR satisfies MEMBER requirements; others need exact match
        boolean hasRole = memberRole == CollectionRole.ADMIN
                || Arrays.stream(required).anyMatch(r -> r == memberRole)
                || (memberRole == CollectionRole.EDITOR
                    && Arrays.stream(required).anyMatch(r -> r == CollectionRole.MEMBER));

        if (!hasRole) {
            throw new ForbiddenException(
                    CollectionErrorCode.INSUFFICIENT_COLLECTION_ROLE,
                    "Insufficient role. Required: " + Arrays.toString(required));
        }

        return joinPoint.proceed();
    }

    private String extractCollectionId(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
            String pathName = pathVariable == null ? "" : !pathVariable.value().isBlank() ? pathVariable.value() : pathVariable.name();
            if ("collectionId".equals(name) || "cid".equals(name)
                    || "collectionId".equals(pathName) || "cid".equals(pathName)) {
                return args[i] != null ? args[i].toString() : null;
            }
        }
        for (Object arg : args) {
            if (arg instanceof String value && collectionRepository.existsById(value)) {
                return value;
            }
        }
        return null;
    }
}
