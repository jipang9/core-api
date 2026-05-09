plugins {
	kotlin("jvm") version "2.1.21"
	kotlin("plugin.spring") version "2.1.21"
	kotlin("plugin.jpa") version "2.1.21"

	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"

	jacoco
}

group = "com.mobility"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	implementation("org.springframework.kafka:spring-kafka")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	runtimeOnly("com.mysql:mysql-connector-j")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.8"))

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("org.testcontainers:kafka")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {

	jvmToolchain(17)

	compilerOptions {
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xannotation-default-target=param-property"
		)
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {

	dependsOn(tasks.test)

	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

jacoco {
	toolVersion = "0.8.11"
}