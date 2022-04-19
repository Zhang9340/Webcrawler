package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;

  private final ProfilingState state;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = delegate;
    this.state = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    Instant instant = Instant.now(clock);
    // Checking if the method is in a class
    if (method.getDeclaringClass().equals(Object.class)) {
      try {

      method.invoke(delegate, args);

      } catch (InvocationTargetException | IllegalArgumentException e) {
        throw e.getCause();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } finally {
        if (method.getAnnotation(Profiled.class)!=null){
          Duration elapsed=Duration.between(instant, clock.instant());
          state.record(method.getClass(),method,elapsed);
        }
      }


    }
    return method.invoke(delegate, args);
  }
}
