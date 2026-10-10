/**
 * Microservicio de usuarios, profesores, catálogo de líneas de investigación y relación M:N
 * {@code Professor ↔ ResearchLine} (Entrega #1 — commons JAR).
 *
 * <p><b>Parte #1 — entidades:</b></p>
 * <ul>
 *   <li>Simples: {@link com.reservas.commons.entity.User}, {@link com.reservas.commons.entity.ResearchLine}</li>
 *   <li>M:N: {@link com.reservas.commons.entity.ProfessorResearchLine} (cabecera-detalle profesor / catálogo)</li>
 * </ul>
 *
 * <p>Capas: {@code controller → service → repository}; entidades y DTOs de API en {@code com.reservas.commons}.</p>
 */
package com.reservas.users;
