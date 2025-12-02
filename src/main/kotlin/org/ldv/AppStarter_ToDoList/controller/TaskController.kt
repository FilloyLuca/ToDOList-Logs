package org.ldv.AppStarter_ToDoList.controller

import jakarta.servlet.http.HttpServletRequest
import org.ldv.AppStarter_ToDoList.entity.TaskStatus
import org.ldv.AppStarter_ToDoList.service.AuditLogService
import org.ldv.AppStarter_ToDoList.service.TaskService
import org.ldv.AppStarter_ToDoList.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
// AJOUT TP2 - import du logger SLF4J pour les logs techniques
import org.slf4j.LoggerFactory

@Controller
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService,
    private val userService: UserService,
    private val auditLogService: AuditLogService
) {
    // AJOUT TP2 - logger technique pour cette classe
    private val logger = LoggerFactory.getLogger(TaskController::class.java)

    @GetMapping
    fun listTasks(authentication: Authentication, model: Model): String {
        val user = userService.findByUsername(authentication.name)!!
        val tasks = taskService.getUserTasks(user)
//        model.addAttribute("tasks", tasks)
//        model.addAttribute("username", user.username)
//        return "tasks"

        // AJOUT TP2 - log technique lors de l’affichage de la liste des tâches
        logger.info("Affichage de la liste des tâches pour l'utilisateur {}",
            user.username)
        model.addAttribute("tasks", tasks)
        model.addAttribute("username", user.username)
        return "task"
    }

    @PostMapping("/create")
    fun createTask(
        @RequestParam title: String,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) dueDate: String?,
        authentication: Authentication,
        request: HttpServletRequest
    ): String {
        val user = userService.findByUsername(authentication.name)!!

        val parsedDueDate = dueDate?.takeIf { it.isNotBlank() }?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }

        taskService.createTask(title, description, parsedDueDate, user)

        auditLogService.log(
            username = user.username,
            action = "CREATE_TASK",
            details = "Création de la tâche : $title",
            request = request
        )

        // AJOUT TP2 - log technique lors de la création d’une tâche
        logger.info(
            "Création d'une tâche pour l'utilisateur {} : titre=\"{}\", échéance={}",
            user.username,
            title,
            parsedDueDate
        )

        return "redirect:/tasks"
    }

    @PostMapping("/update/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @RequestParam title: String,
        @RequestParam(required = false) description: String?,
        @RequestParam status: String,
        @RequestParam(required = false) dueDate: String?,
        authentication: Authentication,
        request: HttpServletRequest
    ): String {
        val task = taskService.getTaskById(id) ?: return "redirect:/tasks"

        if (task.user.username != authentication.name) {
            return "redirect:/tasks"
        }

        val parsedDueDate = dueDate?.takeIf { it.isNotBlank() }?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }

        taskService.updateTask(
            task,
            title,
            description,
            TaskStatus.valueOf(status),
            parsedDueDate
        )

        auditLogService.log(
            username = authentication.name,
            action = "UPDATE_TASK",
            details = "Modification tâche #$id (titre=$title, statut=$status)",
            request = request
        )

        return "redirect:/tasks"
    }

    @PostMapping("/delete/{id}")
    fun deleteTask(
        @PathVariable id: Long,
        authentication: Authentication,
        request: HttpServletRequest
    ): String {
        val task = taskService.getTaskById(id)

        if (task != null && task.user.username == authentication.name) {
            taskService.deleteTask(id)

            auditLogService.log(
                username = authentication.name,
                action = "DELETE_TASK",
                details = "Suppression tâche #$id",
                request = request
            )
        }

        return "redirect:/tasks"
    }
}
