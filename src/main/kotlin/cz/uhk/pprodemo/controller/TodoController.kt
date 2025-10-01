package cz.uhk.pprodemo.controller

import cz.uhk.pprodemo.entity.Todo
import cz.uhk.pprodemo.service.TodoService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/todos")
class TodoController(val service: TodoService) {

    @GetMapping
    fun list(model: Model): String {
        model.addAttribute("todos", service.list())
        return "index"
    }

    @GetMapping("/new")
    fun form(model: Model): String {
        model.addAttribute("todo", Todo())
        return "form"
    }

    @PostMapping
    fun create(todo: Todo): String {
        service.save(todo)
        return "redirect:/todos"
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        model.addAttribute("todo", service.get(id))
        return "detail"
    }

    @PostMapping("/{id}/delete")
    fun delete(@PathVariable id: Long): String {
        service.delete(id)
        return "redirect:/todos"
    }
}
