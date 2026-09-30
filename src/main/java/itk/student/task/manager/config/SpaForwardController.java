package itk.student.task.manager.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("dev")
@Controller
public class SpaForwardController {

    @GetMapping({"/", "/login"})
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
