package cz.uhk.pprodemo.service

import cz.uhk.pprodemo.entity.Todo
import cz.uhk.pprodemo.repository.TodoRepository
import org.springframework.stereotype.Service

@Service
class TodoService(val repo: TodoRepository) {
    fun list(): List<Todo> = repo.findAll()
    fun get(id: Long): Todo = repo.findById(id).orElseThrow { RuntimeException("Not found") }
    fun save(todo: Todo): Todo = repo.save(todo)
    fun delete(id: Long) = repo.delete(get(id))
}
