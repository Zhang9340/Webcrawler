package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final  class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  Boolean checkProfiled(Class<?> klass){
    Method[] methods = klass.getMethods();
    for (Method method:methods){
      if (method.getAnnotation(Profiled.class)!=null){
          return true;
      }
    }
    return false;
  }


  @Override
  public  <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // TODO:Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.
    if (!checkProfiled(klass)){
      throw new IllegalArgumentException();
    }
    Object proxy =  Proxy.newProxyInstance(
            ProfilerImpl.class.getClassLoader(),
            new Class[]{klass},
            new ProfilingMethodInterceptor(clock, delegate, state));


    return (T) proxy;
  }

  @Override
  public void writeData(Path path) throws IOException {
    // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
     Objects.requireNonNull(path);
     try( Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.APPEND )){
       writeData(writer);
     }catch (IOException exception){
       exception.printStackTrace();
     }

  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
