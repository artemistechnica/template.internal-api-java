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
	implementation("io.github.git-commit-id:git-commit-id-maven-plugin:8.0.2")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")
	implementation("com.artemistechnica.commons:commons-java:0.0.4-SNAPSHOT")
	implementation("io.micrometer:micrometer-registry-prometheus:1.13.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	buildInfo()
}
