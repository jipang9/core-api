package mobility.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class CoreApiApplication

fun main(args: Array<String>) {
	runApplication<CoreApiApplication>(*args)
}
