package itk.student.task.manager.config;

import itk.student.task.manager.entity.Project;
import itk.student.task.manager.entity.ProjectMember;
import itk.student.task.manager.entity.Tag;
import itk.student.task.manager.entity.Task;
import itk.student.task.manager.entity.User;
import itk.student.task.manager.enums.GlobalRole;
import itk.student.task.manager.enums.ProjectRole;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.repository.ProjectMemberRepository;
import itk.student.task.manager.repository.ProjectRepository;
import itk.student.task.manager.repository.TagRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            log.info("Seeding demo data for Task Manager sandbox");

            User admin = createUser("admin@demo.com", "Admin", "User", GlobalRole.ADMIN);
            User lead = createUser("lead@demo.com", "Project", "Lead", GlobalRole.USER);
            User qa = createUser("qa@demo.com", "QA", "Engineer", GlobalRole.USER);
            User viewer = createUser("viewer@demo.com", "View", "Only", GlobalRole.USER);

            Project demo = new Project();
            demo.setName("Demo Project");
            demo.setDescription("Sandbox project for API and UI testing practice");
            demo.setProjectKey("DEMO");
            demo.setArchived(false);
            demo.setOwnerId(admin.getId());
            projectRepository.save(demo);

            addMember(demo.getId(), admin.getId(), ProjectRole.OWNER);
            addMember(demo.getId(), lead.getId(), ProjectRole.ADMIN);
            addMember(demo.getId(), qa.getId(), ProjectRole.MEMBER);
            addMember(demo.getId(), viewer.getId(), ProjectRole.VIEWER);

            Tag bugTag = createTag(demo.getId(), "bug", "#EF4444");
            Tag featureTag = createTag(demo.getId(), "feature", "#3B82F6");

            createTask(demo.getId(), "Setup Postman collection", "Import env and auth scripts", TaskStatus.DONE,
                    TaskPriority.MEDIUM, qa.getId(), null);
            createTask(demo.getId(), "Write Rest Assured smoke tests", "Login + create task + assert 201",
                    TaskStatus.IN_PROGRESS, TaskPriority.HIGH, qa.getId(), bugTag.getId());
            createTask(demo.getId(), "Negative login cases", "401 for invalid password", TaskStatus.TODO,
                    TaskPriority.CRITICAL, lead.getId(), bugTag.getId());
            createTask(demo.getId(), "Kanban UI checks", "Selenium POM for board", TaskStatus.REVIEW,
                    TaskPriority.MEDIUM, qa.getId(), featureTag.getId());
            createTask(demo.getId(), "Bulk update API", "POST /tasks/bulk-update max 50 ids", TaskStatus.TODO,
                    TaskPriority.LOW, null, Instant.now().minus(1, ChronoUnit.DAYS), featureTag.getId());

            log.info("Demo users password for all accounts: Demo123!");
        };
    }

    private User createUser(String email, String firstName, String lastName, GlobalRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Demo123!"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setGlobalRole(role);
        return userRepository.save(user);
    }

    private void addMember(java.util.UUID projectId, java.util.UUID userId, ProjectRole role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        projectMemberRepository.save(member);
    }

    private Tag createTag(java.util.UUID projectId, String name, String color) {
        Tag tag = new Tag();
        tag.setProjectId(projectId);
        tag.setName(name);
        tag.setColor(color);
        return tagRepository.save(tag);
    }

    private void createTask(java.util.UUID projectId,
                            String title,
                            String description,
                            TaskStatus status,
                            TaskPriority priority,
                            java.util.UUID assigneeId,
                            java.util.UUID tagId) {
        createTask(projectId, title, description, status, priority, assigneeId, Instant.now().plus(7, ChronoUnit.DAYS), tagId);
    }

    private void createTask(java.util.UUID projectId,
                            String title,
                            String description,
                            TaskStatus status,
                            TaskPriority priority,
                            java.util.UUID assigneeId,
                            Instant dueDate,
                            java.util.UUID tagId) {
        Task task = new Task();
        task.setProjectId(projectId);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setAssigneeId(assigneeId);
        task.setDueDate(dueDate);
        taskRepository.save(task);
    }
}
