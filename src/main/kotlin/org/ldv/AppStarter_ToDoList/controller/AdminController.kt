package org.ldv.AppStarter_ToDoList.controller

import org.ldv.AppStarter_ToDoList.service.AuditLogService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AdminController(
    private val auditLogService: AuditLogService
) {

    @GetMapping
    fun adminPanel(model: Model): String {
        val logs = auditLogService.getAllLogs()
        model.addAttribute("logs", emptyList<Any>())
        return "admin"
    }
}
