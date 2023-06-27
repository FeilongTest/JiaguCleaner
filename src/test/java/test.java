import org.junit.jupiter.api.Test;

public class test {
    public static void main(String[] args) {
        String source = ".method public final b()Lcom/alipay/android/phone/mrpc/core/ab;\n" +
                "    .registers 2\n" +
                "\n" +
                "    iget-object v0, p0, Lcom/alipay/android/phone/mrpc/core/i;->b:Lcom/alipay/android/phone/mrpc/core/h;\n" +
                "\n" +
                "    invoke-static {v0}, Lcom/alipay/android/phone/mrpc/core/h;->a(Lcom/alipay/android/phone/mrpc/core/h;)Landroid/content/Context;\n" +
                "\n" +
                "    move-result-object v0\n" +
                "\n" +
                "    invoke-virtual {v0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;\n" +
                "\n" +
                "    move-result-object v0\n" +
                "\n" +
                "    invoke-static/range {v0 .. v0}, Lcom/stub/StubApp;->getOrigApplicationContext(Landroid/content/Context;)Landroid/content/Context;\n" +
                "\n" +
                "    move-result-object v0\n" +
                "\n" +
                "    invoke-static {v0}, Lcom/alipay/android/phone/mrpc/core/l;->a(Landroid/content/Context;)Lcom/alipay/android/phone/mrpc/core/l;\n" +
                "\n" +
                "    move-result-object v0\n" +
                "\n" +
                "    return-object v0\n" +
                ".end method";
        source = source.replaceAll("\n\n    invoke-virtual \\{(.*)}, Landroid/content/Context;->getApplicationContext\\(\\)Landroid/content/Context;\n\n    move-result-object (.*)","");
        source = source.replaceAll("\n\n    invoke-static/range \\{(.*)}, Lcom/stub/StubApp;->getOrigApplicationContext\\(Landroid/content/Context;\\)Landroid/content/Context;\n\n    move-result-object (.*)",
                "");
        System.out.println(source);

    }
}
