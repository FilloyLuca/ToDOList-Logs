package org.ldv.AppStarter_ToDoList.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AdminController {

    @GetMapping
    fun adminPanel(model: Model): String {
        // Pour l’instant, aucun log n’est enregistré.
        // On passe une liste vide pour que la vue fonctionne sans erreur.
        model.addAttribute("logs", emptyList<Any>())

        // Les étudiants devront plus tard :
        //  - créer AuditLog/AuditLogRepository/AuditLogService
        //  - injecter AuditLogService ici
        //  - remplacer la liste vide par les vrais logs
        return "admin"
    }
}
