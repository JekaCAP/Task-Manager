package itk.student.task.manager.aqa.hw.security;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class MaskingPrintStream extends PrintStream {

    private static final List<Pattern> SECRETS = List.of(
            Pattern.compile("(?i)(\"(?:password|access_?token|refresh_?token)\"\\s*:\\s*\")[^\"]*"),
            Pattern.compile("(?im)^(authorization:\\s*).*$")
    );

    public MaskingPrintStream(OutputStream out) {
        super(out, true);
    }

    private static String mask(String s) {
        if (s == null) return null;
        for (Pattern p : SECRETS) {
            s = p.matcher(s).replaceAll("$1***");
        }
        return s;
    }

    @Override
    public void println(String x) {
        super.println(mask(x));
    }

    @Override
    public void print(String s) {
        super.print(mask(s));
    }
}