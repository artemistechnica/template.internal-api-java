plugins {
	java
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "com.artemistechnica.federation"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.github.classgraph:classgraph:4.8.172")
	implementation("org.mongodb:mongodb-driver-reactivestreams:5.0.0")
	implementation("org.mongodb:mongodb-driver-sync:5.0.1")
	implementation("com.jayway.jsonpath:json-path:2.9.0")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
//	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
