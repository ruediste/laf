package com.github.ruediste.rise.nonReloadable.lambda.expression;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ruediste.rise.nonReloadable.lambda.Capture;
import com.github.ruediste.rise.nonReloadable.lambda.CapturingLambda;
import com.github.ruediste.rise.nonReloadable.lambda.LambdaExpression;
import com.github.ruediste.rise.nonReloadable.lambda.LambdaRunner;

@RunWith(LambdaRunner.class)
public class LambdaInformationWeaverTest {

    <T> void test(@Capture T lambda, String expectedToString, BiConsumer<T, Function<Object[], ?>> checker) {
        // test using capturing
        LambdaExpression<?> exp = LambdaInformationWeaver.getLambdaExpression(lambda);
        assertEquals(expectedToString, exp.toString());
        Function<Object[], ?> compiled = exp.compile();
        checker.accept(lambda, compiled);
    }

    public interface CaptureSupplier<T> extends Supplier<T>, CapturingLambda {

    }

    <T> T load(Class<? extends T> cls) throws Exception {
        return cls.newInstance();
    }

    @Test
    public void testSimple() throws Exception {

        test((CaptureSupplier<String>) () -> "Hello World", "()->{Hello World}", (l, f) -> {
            assertEquals("Hello World", f.apply(new Object[] {}));
            assertEquals("Hello World", f.apply(new Object[] {}));
        });
    }

    static String method() {
        return "Hello World";
    }

    @Test
    public void testMethodReference() throws Exception {
        CaptureSupplier<String> lambda = LambdaInformationWeaverTest::method;
        assertEquals("Hello World", lambda.get());
        LambdaExpression<?> exp = LambdaInformationWeaver.getLambdaExpression(lambda);
        assertEquals("()->{" + LambdaInformationWeaverTest.class.getName() + ".method()}", exp.toString());
        assertEquals("Hello World", exp.compile().apply(new Object[] {}));
    }

    public interface CaptureFunction<T, R> extends Function<T, R>, CapturingLambda {
    }

    @Test
    public void testWithParameters() throws Exception {
        String t = "7";
        CaptureFunction<String, String> lambda = arg -> "hello " + t + " " + arg;
        // test using capturing
        LambdaExpression<?> exp = LambdaInformationWeaver.getLambdaExpression(lambda);
        assertEquals(
                "(java.lang.Object P0)->{java.lang.StringBuilder.<new>(hello ).append(A0).append( ).append(P0).toString()}",
                exp.toString());
        assertEquals("hello 7 2", exp.compile().apply(new Object[] { "2" }));

    }

    String field = "fieldStr";

    @Test
    public void testWiththisReference() throws Exception {
        String t = "7";
        CaptureFunction<String, String> lambda = arg -> "hello " + t + " " + arg + " " + field;
        assertEquals("hello 7 2 fieldStr", lambda.apply("2"));
        LambdaExpression<?> exp = LambdaInformationWeaver.getLambdaExpression(lambda);
        assertEquals(
                "(java.lang.Object P0)->{java.lang.StringBuilder.<new>(hello ).append(A0).append( ).append(A1).append( ).append(this.field).toString()}",
                exp.toString());
    }

    public static class Api {
        private Supplier<String> supplier;

        public Api() {
        }

        public Api(@Capture Supplier<String> supplier) {
            this.supplier = supplier;

        }

        public void invoke(@Capture Supplier<String> supplier) {
            this.supplier = supplier;

        }
    }

    @Test
    public void testCaptureSupplier() throws Exception {
        Api api = new Api();
        api.invoke(() -> "Hello World");
        LambdaExpression<?> lamda = LambdaInformationWeaver.getLambdaExpression(api.supplier);
        assertEquals("()->{Hello World}", lamda.toString());
    }

    @Test
    public void testCaptureConstructor() throws Exception {
        Api api = new Api(() -> "Hello World");
        LambdaExpression<?> lamda = LambdaInformationWeaver.getLambdaExpression(api.supplier);
        assertEquals("()->{Hello World}", lamda.toString());
    }

    @Test
    public void testCaptureSupplierWithCaptured() throws Exception {
        Api api = new Api();
        int i = 1;
        api.invoke(() -> "Hello World " + i);
        LambdaExpression<?> lamda = LambdaInformationWeaver.getLambdaExpression(api.supplier);
        assertEquals("()->{java.lang.StringBuilder.<new>(Hello World ).append(A0).toString()}", lamda.toString());
    }

    private static class A {
        private <T> void add(List<Supplier<T>> result, Class<T> cls, @Capture Supplier<T> str) {
            result.add(str);
        }

        public Supplier<String> get() {
            List<Supplier<String>> list = new ArrayList<>();
            add(list, String.class, () -> "foo");
            return list.get(0);
        }
    }

    @Test
    public void testCaptureWithPrivateMethod() {
        LambdaExpression<?> lambda = LambdaInformationWeaver.getLambdaExpression(new A().get());
        assertEquals("()->{foo}", lambda.toString());
    }
}