plugins {
	java
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.openapi.generator") version "7.6.0"
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
	// For open api codegen
	compileOnly("javax.servlet:javax.servlet-api:4.0.1")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	// End for open api codegen
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
	implementation("javax.validation:validation-api:2.0.0.Final")
}

openApiValidate {
	inputSpec.set("$rootDir/src/main/resources/static/api/swagger.yaml")
	recommend.set(true)
}

// Generate sources from swagger specs
openApiGenerate {
	generatorName.set("spring")
	inputSpec.set("$rootDir/src/main/resources/static/api/swagger.yaml")
	outputDir.set("$buildDir/generated/v1/")
	modelPackage.set("com.artemistechnica.federation.example.models")
	invokerPackage.set("com.artemistechnica.federation.example.invoker")
	apiPackage.set("com.artemistechnica.federation.example.api")
	// Only generating the [[SimpleData]] model
	globalProperties.put("models", "SimpleData")
}

// Add generated sources to classpath
configure<SourceSetContainer> {
	named("main") {
		java.srcDir("$buildDir/generated/v1/src/main/java")
	}
}

// Ensure spec is validated before generation
tasks.openApiGenerate {
	dependsOn(tasks.openApiValidate)
}

// Ensure sources are generated before compilation
tasks.compileJava {
	dependsOn(tasks.openApiGenerate)
}

// Sample task for generating an example javascript client
tasks.create<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("buildJavascriptClient") {
	generatorName = "javascript"
	inputSpec.set("$rootDir/src/main/resources/static/api/swagger.yaml")
	outputDir = "$buildDir/clients/javascript"
	apiPackage = "api"
	invokerPackage = "com.artemistechnica.federation.example.client"
	modelPackage = "model"
	groupId = "com.artemistechnica.federation.client.javascript.example"
	configOptions.put("usePromises", "true")
	configOptions.put("projectName", "template.internal-api-client-javascript")
	id = "client"
}

// Sample task for generating an example java client
tasks.create<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("buildJavaClient") {
	generatorName = "java"
	inputSpec.set("$rootDir/src/main/resources/static/api/swagger.yaml")
	outputDir = "$buildDir/clients/java"
	apiPackage = "com.artemistechnica.federation.example.client.api"
	invokerPackage = "com.artemistechnica.federation.example.client.invoker"
	modelPackage = "com.artemistechnica.federation.example.client.model"
	groupId = "com.artemistechnica.federation.client.java.example"
	configOptions.put("dateLibrary", "java8")
	configOptions.put("projectName", "template.internal-api-client-java")
	configOptions.put("generateClientAsBean", "true")
	id = "client"
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	buildInfo()
}
