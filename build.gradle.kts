plugins {
	java
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.hidetake.swagger.generator") version "2.19.2"
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
	maven {
		url = uri("https://maven.pkg.github.com/artemistechnica/commons-java")
		credentials {
			username = project.findProperty("github.actor") as String? ?: System.getenv("GITHUB_ACTOR")
			password = project.findProperty("github.secret") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

dependencies {
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	implementation("io.github.git-commit-id:git-commit-id-maven-plugin:8.0.2")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.artemistechnica.commons:commons-java:0.0.6-SNAPSHOT")
	implementation("io.micrometer:micrometer-registry-prometheus:1.13.1")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.webjars:bootstrap:5.3.3")
	implementation("org.webjars.npm:htmx.org:1.9.12")
	implementation("com.github.loki4j:loki-logback-appender:1.5.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	swaggerCodegen("io.swagger:swagger-codegen-cli:2.4.34")
	swaggerCodegen("io.swagger.codegen.v3:swagger-codegen-cli:3.0.47")
	swaggerCodegen("org.openapitools:openapi-generator-cli:3.3.4")
	swaggerUI("org.webjars:swagger-ui:4.1.3-1")
}

tasks.processResources {
	from("api").into("api")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

swaggerSources {
//	template {
//		inputFile = file("template.yaml")
//		code {
//			language = "spring"
//		}
//	}
}

//tasks.jar {
//	archiveFileName.set("${project.name}.jar")
//}

springBoot {
	buildInfo()
}
