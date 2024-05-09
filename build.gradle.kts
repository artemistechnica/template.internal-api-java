plugins {
	java
	id("org.springframework.boot") version "3.2.4"
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
		url = uri("https://maven.pkg.github.com/artemistechnica/commons")
		credentials {
			username = project.findProperty("github.actor") as String? ?: System.getenv("GITHUB_ACTOR")
			password = project.findProperty("github.secret") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.artemistechnica.commons:commons-java:0.0.1-SNAPSHOT")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
