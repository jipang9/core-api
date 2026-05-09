package mobility.core

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<CoreApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
