package cz.uhk.pprodemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PproDemoApplication

fun main(args: Array<String>) {
    runApplication<PproDemoApplication>(*args)
}
