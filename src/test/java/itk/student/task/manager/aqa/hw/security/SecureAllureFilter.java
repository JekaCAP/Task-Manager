package itk.student.task.manager.aqa.hw.security;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.List;
import java.util.regex.Pattern;

public class SecureAllureFilter implements Filter {

    private static final List<Pattern> SECRETS = List.of(
            Pattern.compile("(?i)(\"(?:password|access_?token|refresh_?token)\"\\s*:\\s*\")[^\"]*"),
            Pattern.compile("(?im)^(authorization:\\s*).*$")
    );

    private static String mask(String text) {
        for (Pattern p : SECRETS) {
            text = p.matcher(text).replaceAll("$1***");
        }
        return text;
    }

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {
        Response response = ctx.next(req, res);

        StringBuilder sb = new StringBuilder(req.getMethod() + " " + req.getURI() + "\n");
        for (Header h : req.getHeaders()) {
            sb.append(h.getName()).append(": ").append(h.getValue()).append('\n');
        }
        if (req.getBody() != null) {
            sb.append('\n').append(req.getBody().toString());
        }

        Allure.addAttachment("Request", "text/plain", mask(sb.toString()), ".txt");
        Allure.addAttachment("Response", "application/json", mask(response.asString()), ".json");
        return response;
    }
}
