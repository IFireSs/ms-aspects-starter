package com.ms.aspects.aspect;

import com.ms.aspects.annotations.Cached;
import com.ms.aspects.autoconfigure.AspectsProperties;
import com.ms.aspects.cache.TtlCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class CachedAspect {

    private final TtlCacheManager cacheManager;
    private final AspectsProperties props;

    @Around("@annotation(cacheAnn)")
    public Object around(ProceedingJoinPoint pjp, Cached cacheAnn) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String cacheName = !cacheAnn.cache().isBlank() ? cacheAnn.cache() : sig.getDeclaringType().getSimpleName();
        long ttl = cacheAnn.ttlMs() > -1 ? cacheAnn.ttlMs() : props.getCache().getDefaultTtlMs();

        Object key;
        Object[] args = pjp.getArgs();
        if (args != null && args.length == 1 && isSimpleKey(args[0])) {
            key = args[0];
        } else {
            key = deepHash(args);
        }

        var hit = cacheManager.get(cacheName, key);
        if (hit.isPresent()) {
            Object v = hit.get();
            if (v != null || cacheAnn.cacheNull()) {
                return v;
            }
        }

        Object result = pjp.proceed();
        if (result != null || cacheAnn.cacheNull()) {
            cacheManager.put(cacheName, key, result, ttl);
        }
        return result;
    }

    private int deepHash(Object[] args) {
        if (args == null) return 0;
        int h = 1;
        for (Object a : args) {
            h = 31 * h + (a == null ? 0 : a.hashCode());
        }
        return h;
    }

    private boolean isSimpleKey(Object o) {
        if (o == null) return false;
        Class<?> c = o.getClass();
        return CharSequence.class.isAssignableFrom(c)
                || Number.class.isAssignableFrom(c)
                || c == Long.TYPE || c == Integer.TYPE || c == Short.TYPE || c == Byte.TYPE
                || c == java.util.UUID.class;
    }
}
