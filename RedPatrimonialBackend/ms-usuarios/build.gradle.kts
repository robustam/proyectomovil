// La configuración común está en el build.gradle.kts raíz.
dependencies {
    // Solo el módulo de cifrado de Spring Security (BCrypt para las contraseñas)
    implementation("org.springframework.security:spring-security-crypto")
}
