package com.andresport.app_inventory.util

/**
 * Anotación que indica que una clase debe ser abierta para poder ser extendida o mockeada en tests.
 * Usado por el plugin `kotlin-allopen`.
 */
@Target(AnnotationTarget.CLASS)
annotation class OpenForTesting
