package cz.uhk.pprodemo.repository

import cz.uhk.pprodemo.entity.Todo
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<Todo, Long>